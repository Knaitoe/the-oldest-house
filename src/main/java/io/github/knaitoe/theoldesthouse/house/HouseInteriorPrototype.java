package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class HouseInteriorPrototype {
    // High enough to be isolated from ordinary survival terrain while staying
    // in the same X/Z chunk column as the physical House.
    public static final int FLOOR_Y = 300;
    public static final int PLAYER_Y = FLOOR_Y + 1;

    // The copied collar includes several blocks of the ordinary rear hall on
    // the domestic side of the door. The actual return seam sits inside that
    // copied hall rather than in the doorway itself.
    public static final int COLLAR_MIN_Z_OFFSET = 10;
    public static final int DOOR_Z_OFFSET = 15;
    public static final int ENTRY_SEAM_MIN_Z_OFFSET = 16;
    public static final int RETURN_SEAM_Z_OFFSET = 12;
    private static final int END_Z_OFFSET = 64;

    private HouseInteriorPrototype() {
    }

    public static void build(ServerLevel level, BlockPos houseOrigin) {
        int centerX = centerBlockX(houseOrigin);

        // Build one continuous sealed corridor. The section from z=10 through
        // z=18 deliberately mirrors the real domestic hall closely enough that
        // looking back through the impossible door does not reveal a "portal room."
        for (int zOffset = COLLAR_MIN_Z_OFFSET; zOffset <= END_Z_OFFSET; zOffset++) {
            int z = houseOrigin.getZ() + zOffset;

            for (int x = centerX - 1; x <= centerX + 1; x++) {
                level.setBlock(new BlockPos(x, FLOOR_Y, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 3);
                level.setBlock(new BlockPos(x, PLAYER_Y + 4, z), Blocks.SPRUCE_SLAB.defaultBlockState(), 3);
            }

            for (int y = PLAYER_Y; y <= PLAYER_Y + 3; y++) {
                level.setBlock(new BlockPos(centerX - 2, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
                level.setBlock(new BlockPos(centerX + 2, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }

            // Clear the actual walking volume in case the prototype was rebuilt
            // over an earlier test version.
            for (int x = centerX - 1; x <= centerX + 1; x++) {
                for (int y = PLAYER_Y; y <= PLAYER_Y + 3; y++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }

            level.setBlock(new BlockPos(centerX, PLAYER_Y, z), Blocks.RED_CARPET.defaultBlockState(), 3);
        }

        // Recreate the ordinary hallway partition and door at the same X/Z as
        // the real one. The player crosses this door normally in either
        // direction; it is no longer the teleport trigger.
        int doorZ = houseOrigin.getZ() + DOOR_Z_OFFSET;
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = PLAYER_Y; y <= PLAYER_Y + 3; y++) {
                level.setBlock(new BlockPos(x, y, doorZ), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }
        }
        placeDoor(level, centerX, doorZ, false);

        // Reproduce the hall lantern on the domestic side of the doorway.
        int copiedHallLanternZ = houseOrigin.getZ() + 13;
        level.setBlock(new BlockPos(centerX, PLAYER_Y + 3, copiedHallLanternZ), Blocks.CHAIN.defaultBlockState(), 3);
        level.setBlock(
                new BlockPos(centerX, PLAYER_Y + 2, copiedHallLanternZ),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true),
                3
        );

        // Sparse lights deeper in the impossible corridor.
        for (int zOffset : new int[]{24, 36, 48, 60}) {
            int z = houseOrigin.getZ() + zOffset;
            level.setBlock(new BlockPos(centerX, PLAYER_Y + 3, z), Blocks.CHAIN.defaultBlockState(), 3);
            level.setBlock(
                    new BlockPos(centerX, PLAYER_Y + 2, z),
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true),
                    3
            );
        }

        int endZ = houseOrigin.getZ() + END_Z_OFFSET;
        for (int x = centerX - 2; x <= centerX + 2; x++) {
            for (int y = PLAYER_Y; y <= PLAYER_Y + 3; y++) {
                level.setBlock(new BlockPos(x, y, endZ), Blocks.WHITE_TERRACOTTA.defaultBlockState(), 3);
            }
        }
    }

    public static void syncDoorFromDomesticFloor(ServerLevel level, BlockPos houseOrigin) {
        int centerX = centerBlockX(houseOrigin);
        BlockPos domesticDoor = houseOrigin.offset(HouseBuilder.WIDTH / 2, 1, DOOR_Z_OFFSET);
        boolean open = level.getBlockState(domesticDoor).getOptionalValue(BlockStateProperties.OPEN).orElse(false);
        setDoorOpen(level, new BlockPos(centerX, PLAYER_Y, houseOrigin.getZ() + DOOR_Z_OFFSET), open);
    }

    public static void syncDoorToDomesticFloor(ServerLevel level, BlockPos houseOrigin) {
        int centerX = centerBlockX(houseOrigin);
        BlockPos hiddenDoor = new BlockPos(centerX, PLAYER_Y, houseOrigin.getZ() + DOOR_Z_OFFSET);
        boolean open = level.getBlockState(hiddenDoor).getOptionalValue(BlockStateProperties.OPEN).orElse(false);
        setDoorOpen(level, houseOrigin.offset(HouseBuilder.WIDTH / 2, 1, DOOR_Z_OFFSET), open);
    }

    private static void placeDoor(ServerLevel level, int x, int z, boolean open) {
        BlockState lower = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(BlockStateProperties.OPEN, open);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

        level.setBlock(new BlockPos(x, PLAYER_Y, z), lower, 3);
        level.setBlock(new BlockPos(x, PLAYER_Y + 1, z), upper, 3);
    }

    private static void setDoorOpen(ServerLevel level, BlockPos lowerPos, boolean open) {
        BlockState lower = level.getBlockState(lowerPos);
        BlockState upper = level.getBlockState(lowerPos.above());

        if (lower.hasProperty(BlockStateProperties.OPEN)) {
            level.setBlock(lowerPos, lower.setValue(BlockStateProperties.OPEN, open), 3);
        }
        if (upper.hasProperty(BlockStateProperties.OPEN)) {
            level.setBlock(lowerPos.above(), upper.setValue(BlockStateProperties.OPEN, open), 3);
        }
    }

    private static int centerBlockX(BlockPos houseOrigin) {
        return houseOrigin.getX() + HouseBuilder.WIDTH / 2;
    }
}
