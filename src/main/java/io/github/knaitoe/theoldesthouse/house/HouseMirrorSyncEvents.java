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
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class HouseMirrorSyncEvents {
    private static final Set<PendingSync> PENDING = new LinkedHashSet<>();
    private static int atmosphereTicker;

    private HouseMirrorSyncEvents() {
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        queue(event.getLevel(), event.getPos());
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
