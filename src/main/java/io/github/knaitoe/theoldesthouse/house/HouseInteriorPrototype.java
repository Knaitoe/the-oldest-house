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
    public static final int FLOOR_Y = 63;
    public static final int PLAYER_Y = 64;
    public static final int ENTRY_X = 0;

    private static final int MIN_Z = -4;
    private static final int MAX_Z = 48;

    private HouseInteriorPrototype() {
    }

    public static void build(ServerLevel level) {
        for (int z = MIN_Z; z <= MAX_Z; z++) {
            for (int x = -2; x <= 2; x++) {
                level.setBlock(new BlockPos(x, FLOOR_Y, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, PLAYER_Y + 5, z), Blocks.SPRUCE_SLAB.defaultBlockState(), 3);
            }

            for (int y = PLAYER_Y; y <= PLAYER_Y + 4; y++) {
                level.setBlock(new BlockPos(-2, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
                level.setBlock(new BlockPos(2, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }

            level.setBlock(new BlockPos(0, PLAYER_Y, z), Blocks.RED_CARPET.defaultBlockState(), 3);
        }

        for (int x = -2; x <= 2; x++) {
            for (int y = PLAYER_Y; y <= PLAYER_Y + 4; y++) {
                level.setBlock(new BlockPos(x, y, -2), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }
        }

        BlockState lower = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        level.setBlock(new BlockPos(0, PLAYER_Y, -2), lower, 3);
        level.setBlock(new BlockPos(0, PLAYER_Y + 1, -2), upper, 3);

        for (int z : new int[]{4, 16, 28, 40}) {
            level.setBlock(new BlockPos(0, PLAYER_Y + 4, z), Blocks.CHAIN.defaultBlockState(), 3);
            level.setBlock(
                    new BlockPos(0, PLAYER_Y + 3, z),
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true),
                    3
            );
        }

        for (int x = -2; x <= 2; x++) {
            for (int y = PLAYER_Y; y <= PLAYER_Y + 4; y++) {
                level.setBlock(new BlockPos(x, y, MAX_Z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }
        }
    }
}
