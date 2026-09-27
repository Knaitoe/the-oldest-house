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
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(HouseDimensions.INTERIOR)) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.housePosition().orElse(null);
        if (origin == null || !data.isImpossibleDoorRevealed()) {
            return;
        }

        event.getAffectedBlocks().removeIf(
                pos -> HouseImpossibleHallway.isProtectedStructureBlock(origin, pos)
        );
    }

    public static void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(HouseDimensions.INTERIOR)) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(level.getServer());
        BlockPos origin = data.housePosition().orElse(null);
        if (origin == null || !data.isImpossibleDoorRevealed()) {
            return;
        }

        var resolver = event.getStructureHelper();
        if (resolver != null && resolver.resolve()) {
            boolean touchesProtected = resolver.getToPush().stream()
                    .anyMatch(pos -> HouseImpossibleHallway.isProtectedStructureBlock(origin, pos));

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
        }
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

        if (PENDING.isEmpty()) {
            return;
        }

        Set<PendingSync> work = new LinkedHashSet<>(PENDING);
        PENDING.clear();

        if (!data.isSpawned() || !data.isInteriorInitialized()) {
            return;
        }

        BlockPos origin = data.housePosition().orElse(null);
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null) {
            return;
        }

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

            HouseDimensionMirror.copyState(source, target, pending.pos());
        }
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

    private static void add(ResourceKey<Level> source, BlockPos pos) {
        PENDING.add(new PendingSync(source, pos.immutable()));
    }

    private record PendingSync(ResourceKey<Level> source, BlockPos pos) {
    }
}
