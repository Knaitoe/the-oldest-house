package io.github.knaitoe.theoldesthouse.house;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class HouseMirrorSyncEvents {
    private static final Set<PendingSync> PENDING = new LinkedHashSet<>();
    private static int atmosphereTicker;
    private static int authoritativeTicker;
    private static final int AUTHORITATIVE_RECONCILE_INTERVAL = 20;
    private static final double ACTIVE_RECONCILE_RADIUS_SQUARED = 96.0D * 96.0D;

    private HouseMirrorSyncEvents() {
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (protectImpossibleStructure(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
            return;
        }

        queue(event.getLevel(), event.getPos());
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        if (!dimension.equals(Level.OVERWORLD)
                && !dimension.equals(HouseDimensions.INTERIOR)) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.housePosition().orElse(null);

        if (dimension.equals(HouseDimensions.INTERIOR)
                && origin != null
                && data.isImpossibleDoorRevealed()) {
            event.getAffectedBlocks().removeIf(
                    pos -> HouseImpossibleHallway.isProtectedStructureBlock(origin, pos)
            );
        }

        // Detonate fires before the affected blocks are removed. Queue them now;
        // the post-tick mirror reads the committed result.
        for (BlockPos pos : event.getAffectedBlocks()) {
            queue(level, pos);
        }
    }

    public static void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        if (!dimension.equals(Level.OVERWORLD)
                && !dimension.equals(HouseDimensions.INTERIOR)) {
            return;
        }

        var resolver = event.getStructureHelper();
        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.housePosition().orElse(null);

        if (dimension.equals(HouseDimensions.INTERIOR)
                && origin != null
                && data.isImpossibleDoorRevealed()) {
            if (resolver != null && resolver.resolve()) {
                boolean touchesProtected = resolver.getToPush().stream()
                        .anyMatch(pos ->
                                HouseImpossibleHallway.isProtectedStructureBlock(origin, pos)
                                        || HouseImpossibleHallway.isProtectedStructureBlock(
                                                origin,
                                                pos.relative(event.getDirection())
                                        )
                        );

                if (touchesProtected) {
                    event.setCanceled(true);
                    return;
                }
            }

            BlockPos face = event.getFaceOffsetPos();
            if (HouseImpossibleHallway.isProtectedStructureBlock(origin, face)
                    || HouseImpossibleHallway.isProtectedStructureBlock(
                            origin,
                            face.relative(event.getDirection())
                    )) {
                event.setCanceled(true);
                return;
            }
        }

        // Queue the source and destination cells before movement. Post-tick
        // reconciliation sees the final piston result, including moved block
        // entities on implementations that support them.
        if (resolver != null && resolver.resolve()) {
            for (BlockPos pos : resolver.getToPush()) {
                queue(level, pos);
                queue(level, pos.relative(event.getDirection()));
            }

            for (BlockPos pos : resolver.getToDestroy()) {
                queue(level, pos);
            }
        }

        queue(level, event.getFaceOffsetPos());
        queue(level, event.getFaceOffsetPos().relative(event.getDirection()));
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        queue(event.getLevel(), event.getPos());

        if (event instanceof BlockEvent.EntityMultiPlaceEvent multi) {
            multi.getReplacedBlockSnapshots().forEach(snapshot ->
                    queue(snapshot.getLevel(), snapshot.getPos())
            );
        }
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            queue(player.serverLevel(), event.getPos());
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        HouseSavedData data = HouseSavedData.get(server);

        atmosphereTicker++;
        if (atmosphereTicker >= 20) {
            atmosphereTicker = 0;
            if (data.isInteriorInitialized()) {
                ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
                if (interior != null) {
                    HouseDimensionMirror.syncAtmosphere(server.overworld(), interior);
                }
            }
        }

        BlockPos origin = data.housePosition().orElse(null);
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);

        // Explicit player/world events are applied first. Overworld edits such
        // as breaking an exterior wall therefore reach the authoritative House
        // dimension before the periodic authority pass runs.
        if (!PENDING.isEmpty()) {
            Set<PendingSync> work = new LinkedHashSet<>(PENDING);
            PENDING.clear();

            if (data.isSpawned()
                    && data.isInteriorInitialized()
                    && origin != null
                    && interior != null) {
                for (PendingSync pending : work) {
                    if (!HouseDimensionMirror.isSharedPosition(origin, pending.pos())) {
                        continue;
                    }

                    ServerLevel source;
                    ServerLevel target;

                    if (pending.source().equals(Level.OVERWORLD)) {
                        source = server.overworld();
                        target = interior;
                    } else if (pending.source().equals(HouseDimensions.INTERIOR)) {
                        source = interior;
                        target = server.overworld();
                    } else {
                        continue;
                    }

                    HouseDimensionMirror.copyStateAndBlockEntity(
                            source,
                            target,
                            pending.pos()
                    );
                }
            }
        }

        authoritativeTicker++;
        if (authoritativeTicker < AUTHORITATIVE_RECONCILE_INTERVAL) {
            return;
        }
        authoritativeTicker = 0;

        if (!data.isSpawned()
                || !data.isInteriorInitialized()
                || origin == null
                || interior == null
                || !isHouseActivelyObserved(server, interior, origin)) {
            return;
        }

        // Once initialized, the House dimension owns domestic persistence.
        // This catches furnace progress, container inventory/menu changes,
        // environmental ticks, redstone state and other mutations that do not
        // reliably emit a placement/break event.
        HouseDimensionMirror.reconcileAuthoritativeDomestic(
                interior,
                server.overworld(),
                origin
        );
    }

    private static boolean isHouseActivelyObserved(
            MinecraftServer server,
            ServerLevel interior,
            BlockPos origin
    ) {
        double centerX = origin.getX() + HouseBuilder.WIDTH / 2.0D;
        double centerY = origin.getY() + 4.0D;
        double centerZ = origin.getZ() + HouseBuilder.DEPTH / 2.0D;

        for (ServerPlayer player : server.overworld().players()) {
            if (player.distanceToSqr(centerX, centerY, centerZ)
                    <= ACTIVE_RECONCILE_RADIUS_SQUARED) {
                return true;
            }
        }

        for (ServerPlayer player : interior.players()) {
            if (player.distanceToSqr(centerX, centerY, centerZ)
                    <= ACTIVE_RECONCILE_RADIUS_SQUARED) {
                return true;
            }
        }

        return false;
    }

    private static boolean protectImpossibleStructure(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)
                || !serverLevel.dimension().equals(HouseDimensions.INTERIOR)) {
            return false;
        }

        HouseSavedData data = HouseSavedData.get(serverLevel.getServer());
        if (!data.isImpossibleDoorRevealed()) {
            return false;
        }

        BlockPos origin = data.housePosition().orElse(null);
        return origin != null
                && HouseImpossibleHallway.isProtectedStructureBlock(origin, pos);
    }

    private static void queue(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ResourceKey<Level> dimension = serverLevel.dimension();
        if (!dimension.equals(Level.OVERWORLD) && !dimension.equals(HouseDimensions.INTERIOR)) {
            return;
        }

        add(dimension, pos);
        add(dimension, pos.above());
        add(dimension, pos.below());

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            add(dimension, pos.relative(direction));
        }
    }

    public static void clearPending() {
        PENDING.clear();
        atmosphereTicker = 0;
        authoritativeTicker = 0;
    }

    private static void add(ResourceKey<Level> source, BlockPos pos) {
        PENDING.add(new PendingSync(source, pos.immutable()));
    }

    private record PendingSync(ResourceKey<Level> source, BlockPos pos) {
    }
}
