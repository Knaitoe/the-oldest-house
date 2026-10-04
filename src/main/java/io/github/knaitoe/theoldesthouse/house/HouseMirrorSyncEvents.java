package io.github.knaitoe.theoldesthouse.house;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class HouseMirrorSyncEvents {
    private static final int ATMOSPHERE_INTERVAL = 20;
    private static final int AUTHORITATIVE_RECONCILE_INTERVAL = 20;
    private static final double OBSERVER_RADIUS = 96.0D;
    private static final double OBSERVER_RADIUS_SQUARED = OBSERVER_RADIUS * OBSERVER_RADIUS;
    /** Pistons push up to 12 blocks; anything further away cannot reach the house. */
    private static final int PISTON_REACH = 13;

    // Double-buffered so the tick can swap instead of copying the set.
    private static Set<PendingSync> pending = new LinkedHashSet<>();
    private static Set<PendingSync> draining = new LinkedHashSet<>();
    private static int atmosphereTicker;
    private static int authoritativeTicker;

    private HouseMirrorSyncEvents() {
    }

    // ------------------------------------------------------------------
    // World events
    // ------------------------------------------------------------------

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.houseOrigin();
        if (origin == null || !isMirroredDimension(level)) {
            return;
        }

        if (protectsImpossibleStructure(level, data, origin, event.getPos())
                || isProtectedOverworldProxy(level, origin, event.getPos())) {
            event.setCanceled(true);
            return;
        }

        queue(level, origin, event.getPos());
    }

    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isMirroredDimension(level)) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.houseOrigin();
        if (origin == null) {
            return;
        }

        List<BlockPos> affected = event.getAffectedBlocks();
        affected.removeIf(pos -> protectsImpossibleStructure(level, data, origin, pos)
                || HouseImpossibleHallway.isShielded(level, pos)
                || isProtectedOverworldProxy(level, origin, pos));

        // Detonate fires before the affected blocks are removed. Queue them
        // now; the post-tick mirror reads the committed result.
        for (BlockPos pos : affected) {
            queue(level, origin, pos);
        }
    }

    /** Withers, dragons and other block-breaking creatures leave the impossible hallway standing. */
    public static void onMobDestroy(net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent event) {
        if (HouseImpossibleHallway.isShielded(event.getEntity().level(), event.getPos())) event.setCanceled(true);
    }

    public static void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isMirroredDimension(level)) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.houseOrigin();
        if (origin == null
                || !HouseDimensionMirror.isNearHouse(origin, event.getPos(), HouseDimensionMirror.VIEW_RADIUS + PISTON_REACH)) {
            // Redstone elsewhere in the world never pays for structure resolution.
            return;
        }

        Direction direction = event.getDirection();
        PistonStructureResolver resolver = event.getStructureHelper();
        boolean resolved = resolver != null && resolver.resolve();
        BlockPos face = event.getFaceOffsetPos();

        boolean touchesProtected = protectsImpossibleStructure(level, data, origin, face)
                || protectsImpossibleStructure(level, data, origin, face.relative(direction));
        if (!touchesProtected && resolved) {
            for (BlockPos pos : resolver.getToPush()) {
                if (protectsImpossibleStructure(level, data, origin, pos)
                        || protectsImpossibleStructure(level, data, origin, pos.relative(direction))) {
                    touchesProtected = true;
                    break;
                }
            }
        }
        if (touchesProtected) {
            event.setCanceled(true);
            return;
        }

        // Queue source and destination cells before movement. Post-tick
        // reconciliation sees the final piston result.
        if (resolved) {
            for (BlockPos pos : resolver.getToPush()) {
                queue(level, origin, pos);
                queue(level, origin, pos.relative(direction));
            }
            for (BlockPos pos : resolver.getToDestroy()) {
                queue(level, origin, pos);
            }
        }
        queue(level, origin, face);
        queue(level, origin, face.relative(direction));
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isMirroredDimension(level)) {
            return;
        }
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        if (origin == null) {
            return;
        }

        queue(level, origin, event.getPos());
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multi) {
            multi.getReplacedBlockSnapshots().forEach(snapshot -> queue(level, origin, snapshot.getPos()));
        }
    }

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!isMirroredDimension(level)) {
            return;
        }
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        if (origin == null) {
            return;
        }

        if (isProtectedOverworldProxy(level, origin, event.getPos())) {
            // Overworld containers are hollow proxies of the real ones inside.
            event.setCanceled(true);
            return;
        }

        queue(level, origin, event.getPos());
    }

    // ------------------------------------------------------------------
    // Tick
    // ------------------------------------------------------------------

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();

        if (!data.isSpawned() || origin == null) {
            pending.clear();
            return;
        }

        HouseInteriorInitializer.tick(server, data);

        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            pending.clear();
            return;
        }

        if (++atmosphereTicker >= ATMOSPHERE_INTERVAL) {
            atmosphereTicker = 0;
            if (data.isInteriorInitialized()) {
                HouseDimensionMirror.syncAtmosphere(server.overworld(), interior);
            }
        }

        // Explicit player/world events are applied first, so Overworld edits
        // such as breaking an exterior wall reach the authoritative House
        // dimension before the periodic authority pass runs.
        if (!pending.isEmpty()) {
            Set<PendingSync> work = pending;
            pending = draining;
            draining = work;
            applyPending(server, data, origin, interior, work);
            work.clear();
        }

        if (++authoritativeTicker < AUTHORITATIVE_RECONCILE_INTERVAL) {
            return;
        }
        authoritativeTicker = 0;

        // Only Overworld viewers ever see the proxy; players inside the
        // House dimension see the real thing. Reconcile when someone could
        // notice, plus once on every exit (see HouseTransitionEvents).
        if (data.isInteriorInitialized() && hasOverworldObserver(server, origin)) {
            HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, server.overworld(), origin);
        }
    }

    private static void applyPending(
            MinecraftServer server,
            HouseSavedData data,
            BlockPos origin,
            ServerLevel interior,
            Set<PendingSync> work
    ) {
        for (PendingSync sync : work) {
            ServerLevel source;
            ServerLevel target;
            if (sync.source().equals(Level.OVERWORLD)) {
                // Edits made while the interior is still being seeded are
                // forwarded only for chunks the seeding copy has passed.
                if (!HouseInteriorInitializer.isSeeded(data, sync.pos())) {
                    continue;
                }
                source = server.overworld();
                target = interior;
            } else if (data.isInteriorInitialized()) {
                source = interior;
                target = server.overworld();
            } else {
                continue;
            }

            HouseDimensionMirror.copyStateAndBlockEntity(source, target, sync.pos());
        }
    }

    private static boolean hasOverworldObserver(MinecraftServer server, BlockPos origin) {
        double centerX = origin.getX() + HouseLayout.CENTER_X + 0.5D;
        double centerY = origin.getY() + 4.0D;
        double centerZ = origin.getZ() + HouseLayout.CENTER_Z + 0.5D;

        for (ServerPlayer player : server.overworld().players()) {
            if (player.distanceToSqr(centerX, centerY, centerZ) <= OBSERVER_RADIUS_SQUARED) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Protection
    // ------------------------------------------------------------------

    private static boolean isMirroredDimension(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        return dimension.equals(Level.OVERWORLD) || dimension.equals(HouseDimensions.INTERIOR);
    }

    private static boolean protectsImpossibleStructure(
            ServerLevel level,
            HouseSavedData data,
            BlockPos origin,
            BlockPos pos
    ) {
        if (HouseBetweenRoom.isProtectedHousePosition(data, origin, pos)) {
            // The door to the room between rooms and the wall around it, on
            // both sides of the mirror.
            return true;
        }
        return level.dimension().equals(HouseDimensions.INTERIOR)
                && data.isImpossibleDoorRevealed()
                && HouseImpossibleHallway.isProtectedStructureBlock(origin, pos);
    }

    /**
     * Inventory-bearing blocks of the Overworld shell cannot be used, broken
     * or destroyed: their contents live in the House dimension.
     */
    private static boolean isProtectedOverworldProxy(ServerLevel level, BlockPos origin, BlockPos pos) {
        if (!level.dimension().equals(Level.OVERWORLD) || !HouseDimensionMirror.isDomesticPosition(origin, pos)) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && HouseDimensionMirror.isInventoryBearing(level, blockEntity);
    }

    // ------------------------------------------------------------------
    // Queue
    // ------------------------------------------------------------------

    private static void queue(LevelAccessor level, @Nullable BlockPos origin, BlockPos pos) {
        if (origin == null
                || !(level instanceof ServerLevel serverLevel)
                || !HouseDimensionMirror.isNearHouse(origin, pos, HouseDimensionMirror.VIEW_RADIUS + 1)) {
            return;
        }

        ResourceKey<Level> dimension = serverLevel.dimension();
        add(origin, dimension, pos);
        add(origin, dimension, pos.above());
        add(origin, dimension, pos.below());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            add(origin, dimension, pos.relative(direction));
        }
    }

    private static void add(BlockPos origin, ResourceKey<Level> source, BlockPos pos) {
        if (HouseDimensionMirror.isSharedPosition(origin, pos)) {
            pending.add(new PendingSync(source, pos.immutable()));
        }
    }

    /** Drops all queued work and timers (world reset, server stop). */
    public static void clearPending() {
        pending.clear();
        draining.clear();
        atmosphereTicker = 0;
        authoritativeTicker = 0;
    }

    private record PendingSync(ResourceKey<Level> source, BlockPos pos) {
    }
}
