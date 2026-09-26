package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class HouseInteriorPrototype {
    // High enough to be isolated from ordinary survival terrain while staying
    // in the same X/Z chunk column as the physical House.
    public static final int FLOOR_Y = 300;
    public static final int PLAYER_Y = FLOOR_Y + 1;

    private static final int START_Z_OFFSET = 12;
    private static final int DOOR_Z_OFFSET = 15;
    private static final int END_Z_OFFSET = 64;

    private HouseInteriorPrototype() {
    }

    public static void build(ServerLevel level, BlockPos houseOrigin) {
        int centerX = houseOrigin.getX() + HouseBuilder.WIDTH / 2;

        for (int zOffset = START_Z_OFFSET; zOffset <= END_Z_OFFSET; zOffset++) {
            int z = houseOrigin.getZ() + zOffset;

            for (int x = centerX - 2; x <= centerX + 2; x++) {
                level.setBlock(new BlockPos(x, FLOOR_Y, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, PLAYER_Y + 5, z), Blocks.SPRUCE_SLAB.defaultBlockState(), 3);
            }

            for (int y = PLAYER_Y; y <= PLAYER_Y + 4; y++) {
                level.setBlock(new BlockPos(centerX - 2, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
                level.setBlock(new BlockPos(centerX + 2, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }

            level.setBlock(new BlockPos(centerX, PLAYER_Y, z), Blocks.RED_CARPET.defaultBlockState(), 3);
        }

        int doorZ = houseOrigin.getZ() + DOOR_Z_OFFSET;

        // Matching partition and door. The player arrives on the far side with
        // identical X/Z so the teleport only changes vertical position.
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = PLAYER_Y; y <= PLAYER_Y + 4; y++) {
                level.setBlock(new BlockPos(x, y, doorZ), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }
        }

        BlockState lower = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        level.setBlock(new BlockPos(centerX, PLAYER_Y, doorZ), lower, 3);
        level.setBlock(new BlockPos(centerX, PLAYER_Y + 1, doorZ), upper, 3);

        for (int zOffset : new int[]{20, 32, 44, 56}) {
            int z = houseOrigin.getZ() + zOffset;
            level.setBlock(new BlockPos(centerX, PLAYER_Y + 4, z), Blocks.CHAIN.defaultBlockState(), 3);
            level.setBlock(
                    new BlockPos(centerX, PLAYER_Y + 3, z),
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true),
                    3
            );
        }

        int endZ = houseOrigin.getZ() + END_Z_OFFSET;
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = PLAYER_Y; y <= PLAYER_Y + 4; y++) {
                level.setBlock(new BlockPos(x, y, endZ), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }
        }
    }
}
