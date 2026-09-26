package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class HouseBuilder {
    public static final int WIDTH = 15;
    public static final int DEPTH = 19;
    public static final int HEIGHT = 13;

    private static final int CENTER_X = WIDTH / 2;
    private static final int REAR_HALL_START_Z = 10;
    private static final int REAR_WALL_Z = DEPTH - 1;

    private HouseBuilder() {
    }

    /**
     * Checks the full above-ground volume used by the house, porch and roof.
     * Natural generation only builds when this volume is already replaceable.
     */
    public static boolean canBuildAt(ServerLevel level, BlockPos origin) {
        for (int x = -2; x <= WIDTH + 1; x++) {
            for (int z = -4; z <= DEPTH; z++) {
                for (int y = 0; y <= HEIGHT; y++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Places the v0.1.2 static domestic House.
     *
     * origin is the north-west corner of the finished floor at floor level.
     * The exterior is intentionally fixed and conventional. The impossible
     * architecture will eventually begin behind the blank wall at the end of
     * the rear central hall.
     */
    public static void build(ServerLevel level, BlockPos origin) {
        clearBuildVolume(level, origin);
        buildFoundationAndFloors(level, origin);
        buildExteriorWalls(level, origin);
        buildInteriorWalls(level, origin);
        installDoorsAndWindows(level, origin);
        buildCeilingAndRoof(level, origin);
        buildPorch(level, origin);
        buildChimneyAndFireplace(level, origin);
        furnishLivingRoom(level, origin);
        furnishKitchenAndDining(level, origin);
        furnishBedroom(level, origin);
        furnishStudy(level, origin);
        furnishHall(level, origin);
        installLighting(level, origin);
    }

    private static void clearBuildVolume(ServerLevel level, BlockPos origin) {
        for (int x = -2; x <= WIDTH + 1; x++) {
            for (int z = -4; z <= DEPTH; z++) {
                for (int y = 0; y <= HEIGHT; y++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildFoundationAndFloors(ServerLevel level, BlockPos origin) {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                set(level, origin, x, -1, z, Blocks.STONE_BRICKS.defaultBlockState());

                // The exterior wall sits on a visible masonry ring.
                if (x == 0 || x == WIDTH - 1 || z == 0 || z == DEPTH - 1) {
                    set(level, origin, x, 0, z, Blocks.STONE_BRICKS.defaultBlockState());
                } else {
                    set(level, origin, x, 0, z, Blocks.OAK_PLANKS.defaultBlockState());
                }

                // Short supports keep the fixed floor from hovering over tiny
                // terrain dips without excavating a basement.
                BlockPos support = origin.offset(x, -2, z);
                for (int depth = 0; depth < 4 && level.isEmptyBlock(support); depth++) {
                    level.setBlock(support, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    support = support.below();
                }
            }
        }

        // Spruce thresholds give the rooms a subtle authored floor plan without
        // the prototype's old checkerboard pattern.
        for (int x = 1; x < WIDTH - 1; x++) {
            set(level, origin, x, 0, 9, Blocks.SPRUCE_PLANKS.defaultBlockState());
        }
        for (int z = REAR_HALL_START_Z; z < REAR_WALL_Z; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildExteriorWalls(ServerLevel level, BlockPos origin) {
        for (int y = 1; y <= 4; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, origin, x, y, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, REAR_WALL_Z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }

            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, 0, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, WIDTH - 1, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // Dark timber corners make the silhouette readable without turning the
        // whole interior into exposed framing.
        for (int y = 1; y <= 4; y++) {
            set(level, origin, 0, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 0, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
    }

    private static void buildInteriorWalls(ServerLevel level, BlockPos origin) {
        // Front rooms remain open-plan. A cross wall creates a rear hall and
        // makes the future impossible doorway feel like an ordinary hall end.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y <= 4; y++) {
                if (x == CENTER_X && y <= 2) {
                    continue;
                }
                set(level, origin, x, y, 9, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // Bedroom / hall / study divisions.
        for (int z = REAR_HALL_START_Z; z < REAR_WALL_Z; z++) {
            for (int y = 1; y <= 4; y++) {
                boolean leftDoor = z == 13 && y <= 2;
                boolean rightDoor = z == 13 && y <= 2;

                if (!leftDoor) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
                if (!rightDoor) {
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        // Simple dark wood base trim gives the interior walls more deliberate
        // scale without needing custom blocks.
        for (int x = 1; x < WIDTH - 1; x++) {
            if (x != CENTER_X) {
                set(level, origin, x, 1, 9, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void installDoorsAndWindows(ServerLevel level, BlockPos origin) {
        placeDoor(level, origin, CENTER_X, 1, 0, Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());
        placeDoor(level, origin, CENTER_X, 1, 9, Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());
        placeDoor(level, origin, 5, 1, 13, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 1, 13, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());

        // Front and rear paired windows.
        for (int x : new int[]{2, 3, 11, 12}) {
            set(level, origin, x, 2, 0, Blocks.GLASS_PANE.defaultBlockState());
            set(level, origin, x, 3, 0, Blocks.GLASS_PANE.defaultBlockState());
            set(level, origin, x, 2, REAR_WALL_Z, Blocks.GLASS_PANE.defaultBlockState());
            set(level, origin, x, 3, REAR_WALL_Z, Blocks.GLASS_PANE.defaultBlockState());
        }

        // Side paired windows.
        for (int z : new int[]{4, 5, 14, 15}) {
            set(level, origin, 0, 2, z, Blocks.GLASS_PANE.defaultBlockState());
            set(level, origin, 0, 3, z, Blocks.GLASS_PANE.defaultBlockState());
            set(level, origin, WIDTH - 1, 2, z, Blocks.GLASS_PANE.defaultBlockState());
            set(level, origin, WIDTH - 1, 3, z, Blocks.GLASS_PANE.defaultBlockState());
        }
    }

    private static void buildCeilingAndRoof(ServerLevel level, BlockPos origin) {
        // Flat interior ceiling beneath the attic.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, x, 5, z, Blocks.SPRUCE_SLAB.defaultBlockState());
            }
        }

        // Gabled dark-oak roof. Each stair course rises toward the center.
        for (int z = -1; z <= DEPTH; z++) {
            // A one-block eave.
            set(level, origin, -1, 5, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, WIDTH, 5, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));

            for (int layer = 0; layer <= 6; layer++) {
                int y = 5 + layer;
                int leftX = layer;
                int rightX = WIDTH - 1 - layer;

                set(level, origin, leftX, y, z,
                        Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
                set(level, origin, rightX, y, z,
                        Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
            }

            set(level, origin, CENTER_X, 12, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
        }

        // Fill both gable ends with plaster beneath the roof slopes.
        for (int layer = 0; layer <= 6; layer++) {
            int y = 5 + layer;
            int leftEdge = layer;
            int rightEdge = WIDTH - 1 - layer;

            for (int x = leftEdge + 1; x < rightEdge; x++) {
                set(level, origin, x, y, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, REAR_WALL_Z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // Small attic windows give the facade a believable second visual scale.
        set(level, origin, CENTER_X, 7, 0, Blocks.GLASS_PANE.defaultBlockState());
        set(level, origin, CENTER_X, 7, REAR_WALL_Z, Blocks.GLASS_PANE.defaultBlockState());
    }

    private static void buildPorch(ServerLevel level, BlockPos origin) {
        for (int x = 4; x <= 10; x++) {
            for (int z = -3; z <= -1; z++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                set(level, origin, x, 4, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
            }
        }

        for (int y = 1; y <= 3; y++) {
            set(level, origin, 4, y, -3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 10, y, -3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        // A shallow front step.
        for (int x = 6; x <= 8; x++) {
            set(level, origin, x, 0, -4,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        }

        placeHangingLantern(level, origin, CENTER_X, 3, -2);
    }

    private static void buildChimneyAndFireplace(ServerLevel level, BlockPos origin) {
        // Dedicated brickwork now reads as a fireplace instead of an inexplicable
        // stripe around every room.
        for (int y = 1; y <= 4; y++) {
            for (int z = 4; z <= 6; z++) {
                set(level, origin, 0, y, z, Blocks.BRICKS.defaultBlockState());
            }
        }

        set(level, origin, 1, 1, 4, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 1, 1, 5, Blocks.CAMPFIRE.defaultBlockState());
        set(level, origin, 1, 1, 6, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 1, 2, 4, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 1, 2, 6, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 1, 3, 4, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 1, 3, 5, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 1, 3, 6, Blocks.BRICKS.defaultBlockState());

        // Chimney stack continues through the western roof slope.
        for (int y = 5; y <= 9; y++) {
            set(level, origin, 1, y, 5, Blocks.BRICKS.defaultBlockState());
        }
    }

    private static void furnishLivingRoom(ServerLevel level, BlockPos origin) {
        // Rug and reading area.
        for (int x = 3; x <= 6; x++) {
            for (int z = 3; z <= 6; z++) {
                set(level, origin, x, 1, z, Blocks.BROWN_CARPET.defaultBlockState());
            }
        }

        set(level, origin, 2, 1, 2, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 2, 2, 2, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 1, 2, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 2, 2, Blocks.BOOKSHELF.defaultBlockState());

        set(level, origin, 3, 1, 7,
                Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        set(level, origin, 4, 1, 7,
                Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        set(level, origin, 5, 1, 7,
                Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));

        set(level, origin, 2, 1, 7, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishKitchenAndDining(ServerLevel level, BlockPos origin) {
        // Functional kitchen counter.
        set(level, origin, 12, 1, 2, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 3, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 12, 1, 4, Blocks.FURNACE.defaultBlockState());
        set(level, origin, 12, 1, 5, Blocks.SMOKER.defaultBlockState());
        set(level, origin, 12, 1, 6, Blocks.BARREL.defaultBlockState());

        // Small dining table.
        set(level, origin, 9, 1, 6, Blocks.OAK_FENCE.defaultBlockState());
        set(level, origin, 9, 2, 6, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        set(level, origin, 10, 1, 6,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
        set(level, origin, 8, 1, 6,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));

        set(level, origin, 10, 1, 2, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishBedroom(ServerLevel level, BlockPos origin) {
        placeBed(level, origin, 2, 1, 14, Direction.SOUTH);
        set(level, origin, 3, 1, 11, Blocks.CHEST.defaultBlockState());
        set(level, origin, 2, 1, 11, Blocks.BARREL.defaultBlockState());

        for (int x = 2; x <= 4; x++) {
            for (int z = 16; z <= 17; z++) {
                set(level, origin, x, 1, z, Blocks.GRAY_CARPET.defaultBlockState());
            }
        }
    }

    private static void furnishStudy(ServerLevel level, BlockPos origin) {
        set(level, origin, 11, 1, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 11, 2, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 12, 1, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 12, 2, 11, Blocks.BOOKSHELF.defaultBlockState());

        set(level, origin, 11, 1, 16, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 12, 1, 16, Blocks.CHEST.defaultBlockState());
        set(level, origin, 11, 1, 14, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishHall(ServerLevel level, BlockPos origin) {
        // A runner deliberately guides the eye toward the currently blank end
        // wall. That wall is reserved for the first impossible door event.
        for (int z = 11; z < REAR_WALL_Z; z++) {
            set(level, origin, CENTER_X, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }

        set(level, origin, 6, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 8, 1, 16, Blocks.BARREL.defaultBlockState());
    }

    private static void installLighting(ServerLevel level, BlockPos origin) {
        placeHangingLantern(level, origin, 5, 3, 4);
        placeHangingLantern(level, origin, 10, 3, 4);
        placeHangingLantern(level, origin, CENTER_X, 3, 7);
        placeHangingLantern(level, origin, CENTER_X, 3, 13);
        placeHangingLantern(level, origin, 3, 3, 14);
        placeHangingLantern(level, origin, 11, 3, 14);
    }

    private static void placeHangingLantern(ServerLevel level, BlockPos origin, int x, int y, int z) {
        set(level, origin, x, y + 1, z, Blocks.CHAIN.defaultBlockState());
        set(level, origin, x, y, z,
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    private static void placeBed(
            ServerLevel level,
            BlockPos origin,
            int x,
            int y,
            int z,
            Direction facing
    ) {
        BlockState foot = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        BlockState head = foot.setValue(BedBlock.PART, BedPart.HEAD);

        set(level, origin, x, y, z, foot);
        BlockPos headPos = origin.offset(x, y, z).relative(facing);
        level.setBlock(headPos, head, 3);
    }

    private static void placeDoor(
            ServerLevel level,
            BlockPos origin,
            int x,
            int y,
            int z,
            Direction facing,
            BlockState baseState
    ) {
        set(level, origin, x, y, z,
                baseState
                        .setValue(DoorBlock.FACING, facing)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, origin, x, y + 1, z,
                baseState
                        .setValue(DoorBlock.FACING, facing)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private static void set(ServerLevel level, BlockPos origin, int x, int y, int z, BlockState state) {
        level.setBlock(origin.offset(x, y, z), state, 3);
    }
}
