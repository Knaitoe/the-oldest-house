package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.server.level.ServerLevel;

public final class HouseBuilder {
    public static final int WIDTH = 13;
    public static final int DEPTH = 17;
    public static final int HEIGHT = 7;

    private static final int DOOR_X = WIDTH / 2;

    private HouseBuilder() {
    }

    /**
     * Places the deliberately simple v0.1 test house.
     *
     * origin is the north-west corner of the finished floor at floor level.
     * The build volume is cleared first, so this method is intentionally
     * suitable for development commands rather than normal world generation.
     */
    public static void build(ServerLevel level, BlockPos origin) {
        clearBuildVolume(level, origin);
        buildFoundation(level, origin);
        buildFloor(level, origin);
        buildExteriorWalls(level, origin);
        buildInteriorPartition(level, origin);
        buildRoof(level, origin);
        installDoorAndWindows(level, origin);
        furnish(level, origin);
    }

    private static void clearBuildVolume(ServerLevel level, BlockPos origin) {
        for (int x = -1; x <= WIDTH; x++) {
            for (int z = -2; z <= DEPTH; z++) {
                for (int y = 0; y <= HEIGHT + 1; y++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildFoundation(ServerLevel level, BlockPos origin) {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                set(level, origin, x, -1, z, Blocks.STONE_BRICKS.defaultBlockState());

                BlockPos support = origin.offset(x, -2, z);
                for (int depth = 0; depth < 4 && level.isEmptyBlock(support); depth++) {
                    level.setBlock(support, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    support = support.below();
                }
            }
        }
    }

    private static void buildFloor(ServerLevel level, BlockPos origin) {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                BlockState floor = ((x + z) & 1) == 0
                        ? Blocks.OAK_PLANKS.defaultBlockState()
                        : Blocks.SPRUCE_PLANKS.defaultBlockState();
                set(level, origin, x, 0, z, floor);
            }
        }

        // Tiny porch. The final exterior art pass will replace this whole shell.
        for (int x = DOOR_X - 1; x <= DOOR_X + 1; x++) {
            set(level, origin, x, 0, -1, Blocks.OAK_PLANKS.defaultBlockState());
        }
    }

    private static void buildExteriorWalls(ServerLevel level, BlockPos origin) {
        for (int y = 1; y <= 4; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, origin, x, y, 0, wallBlock(x, y, 0));
                set(level, origin, x, y, DEPTH - 1, wallBlock(x, y, DEPTH - 1));
            }

            for (int z = 1; z < DEPTH - 1; z++) {
                set(level, origin, 0, y, z, wallBlock(0, y, z));
                set(level, origin, WIDTH - 1, y, z, wallBlock(WIDTH - 1, y, z));
            }
        }

        // Dark timber corners give the test shell a fixed silhouette.
        for (int y = 1; y <= 4; y++) {
            set(level, origin, 0, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 0, y, DEPTH - 1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, DEPTH - 1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
    }

    private static BlockState wallBlock(int x, int y, int z) {
        if (y == 1 || y == 4) {
            return Blocks.BRICKS.defaultBlockState();
        }
        return Blocks.WHITE_TERRACOTTA.defaultBlockState();
    }

    private static void buildInteriorPartition(ServerLevel level, BlockPos origin) {
        int partitionZ = 10;
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y <= 3; y++) {
                if (x == DOOR_X && y <= 2) {
                    continue;
                }
                set(level, origin, x, y, partitionZ, Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildRoof(ServerLevel level, BlockPos origin) {
        for (int x = -1; x <= WIDTH; x++) {
            for (int z = -1; z <= DEPTH; z++) {
                set(level, origin, x, 5, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }

        // A raised center strip makes the prototype recognizable at a glance.
        for (int x = 2; x < WIDTH - 2; x++) {
            for (int z = 1; z < DEPTH - 1; z++) {
                set(level, origin, x, 6, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
            }
        }
    }

    private static void installDoorAndWindows(ServerLevel level, BlockPos origin) {
        set(level, origin, DOOR_X, 1, 0, Blocks.AIR.defaultBlockState());
        set(level, origin, DOOR_X, 2, 0, Blocks.AIR.defaultBlockState());

        BlockState lower = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.NORTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

        set(level, origin, DOOR_X, 1, 0, lower);
        set(level, origin, DOOR_X, 2, 0, upper);

        // Front and back windows.
        for (int x : new int[]{2, 3, WIDTH - 4, WIDTH - 3}) {
            set(level, origin, x, 2, 0, Blocks.GLASS.defaultBlockState());
            set(level, origin, x, 2, DEPTH - 1, Blocks.GLASS.defaultBlockState());
        }

        // Side windows.
        for (int z : new int[]{4, 5, 12, 13}) {
            set(level, origin, 0, 2, z, Blocks.GLASS.defaultBlockState());
            set(level, origin, WIDTH - 1, 2, z, Blocks.GLASS.defaultBlockState());
        }
    }

    private static void furnish(ServerLevel level, BlockPos origin) {
        // Front living/work space.
        set(level, origin, 2, 1, 3, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 3, 1, 3, Blocks.FURNACE.defaultBlockState());
        set(level, origin, 4, 1, 3, Blocks.BARREL.defaultBlockState());

        set(level, origin, 9, 1, 4, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 9, 2, 4, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 10, 1, 4, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 10, 2, 4, Blocks.BOOKSHELF.defaultBlockState());

        // Lighting keeps the prototype functionally safe without special spawn hooks.
        for (BlockPos relative : new BlockPos[]{
                new BlockPos(3, 1, 7),
                new BlockPos(9, 1, 7),
                new BlockPos(3, 1, 13),
                new BlockPos(9, 1, 13)
        }) {
            level.setBlock(origin.offset(relative), Blocks.LANTERN.defaultBlockState(), 3);
        }

        // Rear room storage. Later this will be a stable domestic anchor rather than
        // something the topology system is allowed to regenerate.
        set(level, origin, 2, 1, 13, Blocks.BARREL.defaultBlockState());
        set(level, origin, 3, 1, 13, Blocks.BARREL.defaultBlockState());
        set(level, origin, 10, 1, 13, Blocks.CHEST.defaultBlockState());

        // A plain wall section reserved for the eventual first impossible door.
        for (int x = 5; x <= 7; x++) {
            set(level, origin, x, 1, DEPTH - 1, Blocks.OAK_PLANKS.defaultBlockState());
            set(level, origin, x, 2, DEPTH - 1, Blocks.OAK_PLANKS.defaultBlockState());
        }
    }

    private static void set(ServerLevel level, BlockPos origin, int x, int y, int z, BlockState state) {
        level.setBlock(origin.offset(x, y, z), state, 3);
    }
}
