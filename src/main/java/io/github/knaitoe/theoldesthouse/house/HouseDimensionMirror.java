package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class HouseDimensionMirror {
    public static final int VIEW_RADIUS = 48;
    public static final int VIEW_BELOW_FLOOR = 6;
    public static final int VIEW_ABOVE_FLOOR = 24;

    private HouseDimensionMirror() {
    }

    public static ServerLevel ensureInitialized(MinecraftServer server, HouseSavedData data) {
        ServerLevel overworld = server.overworld();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = data.housePosition().orElse(null);

        if (interior == null || origin == null) {
            return interior;
        }

        syncAtmosphere(overworld, interior);

        if (!data.isInteriorInitialized()) {
            // Copy the real scene directly with client-update-only flags. We do
            // not copy the House and then clear/rebuild it, because neighbor
            // updates from that process can turn attached blocks into item debris.
            copyInitialSnapshot(overworld, interior, origin);

            // BlockState mirroring intentionally does not copy block-entity
            // NBT. Re-attach the authored domestic loot tables to the mirrored
            // chests before the interior becomes authoritative.
            HouseBuilder.applyDomesticLootTables(interior, origin);
            data.markInteriorInitialized();

            if (data.houseAge() >= HouseStageManager.FIRST_IMPOSSIBLE_DOOR_AGE) {
                HouseStageManager.applyCurrentStage(server, data);
            }

            TheOldestHouse.LOGGER.info(
                    "Initialized The Oldest House interior over matching native Overworld terrain."
            );
        }

        return interior;
    }

    public static void syncAtmosphere(ServerLevel overworld, ServerLevel interior) {
        interior.setDayTime(overworld.getDayTime());
        interior.setWeatherParameters(
                0,
                6000,
                overworld.isRaining(),
                overworld.isThundering()
        );
    }

    public static boolean isSharedPosition(BlockPos origin, BlockPos pos) {
        if (HouseImpossibleHallway.isInteriorOnlyPosition(origin, pos)) {
            return false;
        }

        return pos.getX() >= origin.getX() - VIEW_RADIUS
                && pos.getX() <= origin.getX() + HouseBuilder.WIDTH - 1 + VIEW_RADIUS
                && pos.getZ() >= origin.getZ() - VIEW_RADIUS
                && pos.getZ() <= origin.getZ() + HouseBuilder.DEPTH - 1 + VIEW_RADIUS
                && pos.getY() >= origin.getY() - VIEW_BELOW_FLOOR
                && pos.getY() <= origin.getY() + VIEW_ABOVE_FLOOR;
    }

    public static void syncSharedDomesticRegion(
            ServerLevel source,
            ServerLevel target,
            BlockPos origin
    ) {
        int minY = Math.max(
                target.getMinBuildHeight(),
                origin.getY() + HouseBuilder.BASEMENT_FLOOR_Y
        );
        int maxY = Math.min(
                target.getMaxBuildHeight() - 1,
                origin.getY() + HouseBuilder.HEIGHT
        );

        for (int x = origin.getX(); x < origin.getX() + HouseBuilder.WIDTH; x++) {
            for (int z = origin.getZ(); z < origin.getZ() + HouseBuilder.DEPTH; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (!isSharedPosition(origin, pos)) {
                        continue;
                    }

                    copyState(source, target, pos);
                }
            }
        }
    }

    public static void copyState(ServerLevel source, ServerLevel target, BlockPos pos) {
        BlockState state = source.getBlockState(pos);

        // Native terrain in both dimensions already matches because the House
        // dimension now uses the same Overworld noise generator and server seed.
        // Avoid rewriting matching terrain, especially fluids: native generation
        // owns their fluid states, lighting, heightmaps, and biome rendering.
        if (state.equals(target.getBlockState(pos))) {
            return;
        }

        // Flag 2 updates clients without cascading neighbor physics. Exact
        // player edits and the authored House are overlaid onto native terrain.
        target.setBlock(pos, state, 2);
    }

    private static void copyInitialSnapshot(ServerLevel source, ServerLevel target, BlockPos origin) {
        int minX = origin.getX() - VIEW_RADIUS;
        int maxX = origin.getX() + HouseBuilder.WIDTH - 1 + VIEW_RADIUS;
        int minZ = origin.getZ() - VIEW_RADIUS;
        int maxZ = origin.getZ() + HouseBuilder.DEPTH - 1 + VIEW_RADIUS;
        int minY = Math.max(target.getMinBuildHeight(), origin.getY() - VIEW_BELOW_FLOOR);
        int maxY = Math.min(target.getMaxBuildHeight() - 1, origin.getY() + VIEW_ABOVE_FLOOR);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    copyState(source, target, pos);
                }
            }
        }
    }
}
