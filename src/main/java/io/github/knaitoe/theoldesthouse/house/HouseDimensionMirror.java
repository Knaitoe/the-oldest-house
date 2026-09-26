package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class HouseDimensionMirror {
    public static final int VIEW_RADIUS = 32;
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

        if (data.isInteriorInitialized()) {
            return interior;
        }

        copyVisibleSurroundings(overworld, interior, origin);
        HouseBuilder.build(interior, origin);

        if (data.houseAge() >= HouseStageManager.FIRST_IMPOSSIBLE_DOOR_AGE) {
            HouseBuilder.revealImpossibleDoor(interior, origin);
        }

        data.markInteriorInitialized();
        TheOldestHouse.LOGGER.info(
                "Initialized The Oldest House interior dimension and mirrored its nearby Overworld surroundings."
        );
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

    private static void copyVisibleSurroundings(ServerLevel source, ServerLevel target, BlockPos origin) {
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
                    BlockState sourceState = source.getBlockState(pos);
                    target.setBlock(pos, sourceState, 2);
                }
            }
        }
    }
}
