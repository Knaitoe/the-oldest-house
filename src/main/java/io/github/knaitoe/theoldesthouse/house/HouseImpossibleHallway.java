package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

public final class HouseImpossibleHallway {
    public static final int START_Z_OFFSET = 16;
    public static final int END_Z_OFFSET = 72;

    private static final int LEFT_WALL_X_OFFSET = 5;
    private static final int RIGHT_WALL_X_OFFSET = 9;
    private static final int INNER_MIN_X_OFFSET = 6;
    private static final int INNER_MAX_X_OFFSET = 8;

    private HouseImpossibleHallway() {
    }

    public static void build(ServerLevel level, BlockPos origin) {
        // Clear only the interior-dimension corridor envelope and do it without
        // neighbor-update physics so copied scenery cannot explode into drops.
        for (int x = LEFT_WALL_X_OFFSET; x <= RIGHT_WALL_X_OFFSET; x++) {
            for (int z = START_Z_OFFSET; z <= END_Z_OFFSET; z++) {
                for (int y = 0; y <= 5; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        for (int z = START_Z_OFFSET; z <= END_Z_OFFSET; z++) {
            for (int x = INNER_MIN_X_OFFSET; x <= INNER_MAX_X_OFFSET; x++) {
                level.setBlock(origin.offset(x, 0, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                level.setBlock(origin.offset(x, 5, z), Blocks.SPRUCE_SLAB.defaultBlockState(), 2);
            }

            for (int y = 1; y <= 4; y++) {
                level.setBlock(origin.offset(LEFT_WALL_X_OFFSET, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 2);
                level.setBlock(origin.offset(RIGHT_WALL_X_OFFSET, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 2);
            }

            level.setBlock(origin.offset(HouseBuilder.WIDTH / 2, 1, z), Blocks.RED_CARPET.defaultBlockState(), 2);
        }

        // A deliberately sparse, monotonous first impossible space.
        for (int z : new int[]{24, 36, 48, 60}) {
            level.setBlock(origin.offset(HouseBuilder.WIDTH / 2, 4, z), Blocks.CHAIN.defaultBlockState(), 2);
            level.setBlock(
                    origin.offset(HouseBuilder.WIDTH / 2, 3, z),
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true),
                    2
            );
        }

        for (int x = LEFT_WALL_X_OFFSET; x <= RIGHT_WALL_X_OFFSET; x++) {
            for (int y = 1; y <= 4; y++) {
                level.setBlock(origin.offset(x, y, END_Z_OFFSET), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 2);
            }
        }
    }

    public static boolean isInteriorOnlyPosition(BlockPos origin, BlockPos pos) {
        int relX = pos.getX() - origin.getX();
        int relY = pos.getY() - origin.getY();
        int relZ = pos.getZ() - origin.getZ();

        return relX >= LEFT_WALL_X_OFFSET
                && relX <= RIGHT_WALL_X_OFFSET
                && relY >= 0
                && relY <= 5
                && relZ >= 15
                && relZ <= END_Z_OFFSET;
    }

    public static boolean isInsideWalkableVolume(BlockPos origin, double x, double y, double z) {
        double minX = origin.getX() + INNER_MIN_X_OFFSET + 0.15D;
        double maxX = origin.getX() + INNER_MAX_X_OFFSET + 0.85D;
        double minY = origin.getY() + 0.35D;
        double maxY = origin.getY() + 5.45D;
        double minZ = origin.getZ() + START_Z_OFFSET - 0.25D;
        double maxZ = origin.getZ() + END_Z_OFFSET - 0.25D;

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }
}
