package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;

public final class HouseBuilder {
    public static final int WIDTH = 15;
    public static final int DEPTH = 19;
    public static final int HEIGHT = 20;

    public static final int BASEMENT_FLOOR_Y = -5;
    public static final int SECOND_FLOOR_Y = 6;
    public static final int UPPER_WALL_TOP_Y = 10;

    private static final int CENTER_X = WIDTH / 2;
    private static final int REAR_HALL_START_Z = 10;
    private static final int REAR_WALL_Z = DEPTH - 1;
    private static final int ROOF_BASE_Y = 11;

    private static final ResourceKey<LootTable> BEDROOM_LOOT = lootTable("chests/bedroom");
    private static final ResourceKey<LootTable> STUDY_LOOT = lootTable("chests/study");
    private static final ResourceKey<LootTable> BASEMENT_LOOT = lootTable("chests/basement");

    private HouseBuilder() {
    }

    /**
     * Checks the complete above-ground silhouette used by the house, porch and
     * roof. The basement is deliberately allowed to excavate ordinary terrain
     * beneath an otherwise valid site.
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
     * Builds the stable domestic structure. The exterior remains fixed and
     * measurable; impossible architecture still begins only from the authored
     * rear ground-floor hall.
     */
    public static void build(ServerLevel level, BlockPos origin) {
        clearAboveGroundVolume(level, origin);
        buildBasement(level, origin);
        buildFloors(level, origin);
        buildExteriorWalls(level, origin);
        buildMainInteriorWalls(level, origin);
        buildUpperInteriorWalls(level, origin);
        installDoorsAndWindows(level, origin);
        buildCeilingsAndRoof(level, origin);
        buildStaircases(level, origin);
        buildPorch(level, origin);
        buildChimneyAndFireplace(level, origin);

        furnishLivingRoom(level, origin);
        furnishKitchenAndDining(level, origin);
        furnishGroundStudy(level, origin);
        furnishUtilityRoom(level, origin);
        furnishHall(level, origin);
        furnishUpperBedrooms(level, origin);
        furnishBasement(level, origin);
        installLighting(level, origin);

        applyDomesticLootTables(level, origin);
    }

    private static void clearAboveGroundVolume(ServerLevel level, BlockPos origin) {
        for (int x = -2; x <= WIDTH + 1; x++) {
            for (int z = -4; z <= DEPTH; z++) {
                for (int y = 0; y <= HEIGHT; y++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildBasement(ServerLevel level, BlockPos origin) {
        // Seal the footprint first, then excavate the interior. This prevents
        // surrounding cave fluids from immediately flowing into the new room.
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                set(level, origin, x, BASEMENT_FLOOR_Y, z, Blocks.STONE_BRICKS.defaultBlockState());
            }
        }

        for (int y = BASEMENT_FLOOR_Y + 1; y <= -1; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, origin, x, y, 0, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, origin, x, y, REAR_WALL_Z, Blocks.STONE_BRICKS.defaultBlockState());
            }

            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, 0, y, z, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, origin, WIDTH - 1, y, z, Blocks.STONE_BRICKS.defaultBlockState());
            }
        }

        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                for (int y = BASEMENT_FLOOR_Y + 1; y <= -1; y++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }

        // A few structural piers make the basement feel load-bearing rather
        // than like a rectangular cave someone happened to carpet.
        for (int y = BASEMENT_FLOOR_Y + 1; y <= -1; y++) {
            for (int x : new int[]{5, 9}) {
                for (int z : new int[]{6, 12}) {
                    set(level, origin, x, y, z, Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
        }
    }

    private static void buildFloors(ServerLevel level, BlockPos origin) {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                if (x == 0 || x == WIDTH - 1 || z == 0 || z == REAR_WALL_Z) {
                    set(level, origin, x, 0, z, Blocks.STONE_BRICKS.defaultBlockState());
                    set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
                } else {
                    set(level, origin, x, 0, z, Blocks.OAK_PLANKS.defaultBlockState());
                    set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.OAK_PLANKS.defaultBlockState());
                }
            }
        }

        for (int x = 1; x < WIDTH - 1; x++) {
            set(level, origin, x, 0, 9, Blocks.SPRUCE_PLANKS.defaultBlockState());
        }

        for (int z = REAR_HALL_START_Z; z < REAR_WALL_Z; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        // Dark wood upstairs hall runner base.
        for (int z = 1; z <= 10; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildExteriorWalls(ServerLevel level, BlockPos origin) {
        buildExteriorWallBand(level, origin, 1, 5);
        buildExteriorWallBand(level, origin, 7, UPPER_WALL_TOP_Y);

        // A dark horizontal belt visually separates the two domestic floors.
        for (int x = 0; x < WIDTH; x++) {
            set(level, origin, x, SECOND_FLOOR_Y, 0, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            set(level, origin, x, SECOND_FLOOR_Y, REAR_WALL_Z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
        for (int z = 1; z < REAR_WALL_Z; z++) {
            set(level, origin, 0, SECOND_FLOOR_Y, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            set(level, origin, WIDTH - 1, SECOND_FLOOR_Y, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        for (int y = 1; y <= UPPER_WALL_TOP_Y; y++) {
            if (y == SECOND_FLOOR_Y) {
                continue;
            }

            set(level, origin, 0, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 0, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
    }

    private static void buildExteriorWallBand(
            ServerLevel level,
            BlockPos origin,
            int minY,
            int maxY
    ) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, origin, x, y, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, REAR_WALL_Z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }

            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, 0, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, WIDTH - 1, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
    }

    private static void buildMainInteriorWalls(ServerLevel level, BlockPos origin) {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y <= 4; y++) {
                if (x == CENTER_X && y <= 2) {
                    continue;
                }
                set(level, origin, x, y, 9, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        for (int z = REAR_HALL_START_Z; z < REAR_WALL_Z; z++) {
            for (int y = 1; y <= 4; y++) {
                boolean doorway = z == 13 && y <= 2;

                if (!doorway) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        // The ground-floor hall still ends before the fixed rear facade.
        for (int x = 6; x <= 8; x++) {
            for (int y = 1; y <= 4; y++) {
                set(level, origin, x, y, 15, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        for (int x = 1; x < WIDTH - 1; x++) {
            if (x != CENTER_X) {
                set(level, origin, x, 1, 9, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildUpperInteriorWalls(ServerLevel level, BlockPos origin) {
        // Two front bedrooms flank a central stair/landing hall.
        for (int z = 1; z <= 9; z++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                boolean leftDoor = z == 4 && y <= 8;
                boolean rightDoor = z == 4 && y <= 8;

                if (!leftDoor) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
                if (!rightDoor) {
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        // A larger rear bedroom sits above the ordinary rear rooms and the
        // beginning of the impossible-hall buffer.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                if (x == CENTER_X && y <= 8) {
                    continue;
                }
                set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
    }

    private static void installDoorsAndWindows(ServerLevel level, BlockPos origin) {
        placeDoor(level, origin, CENTER_X, 1, 0, Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());
        placeDoor(level, origin, CENTER_X, 1, 9, Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());
        placeDoor(level, origin, 5, 1, 13, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 1, 13, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());

        placeDoor(level, origin, 5, 7, 4, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 7, 4, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, CENTER_X, 7, 10, Direction.SOUTH, Blocks.SPRUCE_DOOR.defaultBlockState());

        // Ground floor front windows.
        for (int x : new int[]{2, 3, 11, 12}) {
            placeWindowColumn(level, origin, x, 0, 2, 3);
        }

        // The fireplace occupies the western middle wall, so the main-floor
        // side windows are deliberately asymmetric.
        for (int z : new int[]{7, 8}) {
            placeWindowColumn(level, origin, 0, z, 2, 3);
        }
        for (int z : new int[]{4, 5}) {
            placeWindowColumn(level, origin, WIDTH - 1, z, 2, 3);
        }

        // Every upstairs bedroom gets real exterior windows. Rear-facing panes
        // remain omitted because the impossible hallway eventually projects
        // from the center of that facade below them.
        for (int x : new int[]{2, 3, 11, 12}) {
            placeWindowColumn(level, origin, x, 0, 8, 9);
        }

        for (int z : new int[]{2, 3}) {
            placeWindowColumn(level, origin, 0, z, 8, 9);
        }
        for (int z : new int[]{4, 5}) {
            placeWindowColumn(level, origin, WIDTH - 1, z, 8, 9);
        }

        for (int z : new int[]{13, 14}) {
            placeWindowColumn(level, origin, 0, z, 8, 9);
            placeWindowColumn(level, origin, WIDTH - 1, z, 8, 9);
        }
    }

    private static void placeWindowColumn(
            ServerLevel level,
            BlockPos origin,
            int x,
            int z,
            int minY,
            int maxY
    ) {
        for (int y = minY; y <= maxY; y++) {
            set(level, origin, x, y, z, Blocks.GLASS_PANE.defaultBlockState());
        }
    }

    private static void buildCeilingsAndRoof(ServerLevel level, BlockPos origin) {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, x, 5, z, Blocks.SPRUCE_SLAB.defaultBlockState());
                set(level, origin, x, ROOF_BASE_Y, z, Blocks.SPRUCE_SLAB.defaultBlockState());
            }
        }

        for (int z = -1; z <= DEPTH; z++) {
            set(level, origin, -1, ROOF_BASE_Y, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, WIDTH, ROOF_BASE_Y, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));

            for (int layer = 0; layer <= 6; layer++) {
                int y = ROOF_BASE_Y + layer;
                int leftX = layer;
                int rightX = WIDTH - 1 - layer;

                set(level, origin, leftX, y, z,
                        Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
                set(level, origin, rightX, y, z,
                        Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
            }

            set(level, origin, CENTER_X, ROOF_BASE_Y + 7, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
        }

        for (int layer = 0; layer <= 6; layer++) {
            int y = ROOF_BASE_Y + layer;
            int leftEdge = layer;
            int rightEdge = WIDTH - 1 - layer;

            for (int x = leftEdge + 1; x < rightEdge; x++) {
                set(level, origin, x, y, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, REAR_WALL_Z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        set(level, origin, CENTER_X, ROOF_BASE_Y + 2, 0, Blocks.GLASS_PANE.defaultBlockState());
    }

    private static void buildStaircases(ServerLevel level, BlockPos origin) {
        // Two-wide central stair climbs from the living floor into the upstairs
        // landing. Ceiling/floor openings are carved only where headroom needs it.
        for (int x = 6; x <= 7; x++) {
            for (int z = 4; z <= 8; z++) {
                set(level, origin, x, 5, z, Blocks.AIR.defaultBlockState());
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.AIR.defaultBlockState());
            }
        }

        for (int step = 0; step < 5; step++) {
            int z = 8 - step;
            int y = 1 + step;
            for (int x = 6; x <= 7; x++) {
                set(level, origin, x, y, z,
                        Blocks.SPRUCE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.NORTH));
            }
        }

        // Basement access descends from the rear-right utility room.
        for (int z = 13; z <= 16; z++) {
            set(level, origin, 11, 0, z, Blocks.AIR.defaultBlockState());
        }

        for (int step = 0; step < 4; step++) {
            int z = 13 + step;
            int y = -1 - step;
            set(level, origin, 11, y, z,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }

        for (int z = 13; z <= 16; z++) {
            set(level, origin, 10, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, origin, 12, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        }
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

        for (int x = 6; x <= 8; x++) {
            set(level, origin, x, 0, -4,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        }

        placeHangingLantern(level, origin, CENTER_X, 3, -2);
    }

    private static void buildChimneyAndFireplace(ServerLevel level, BlockPos origin) {
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

        for (int y = 5; y <= 16; y++) {
            set(level, origin, 0, y, 5, Blocks.BRICKS.defaultBlockState());
        }
    }

    private static void furnishLivingRoom(ServerLevel level, BlockPos origin) {
        for (int x = 2; x <= 5; x++) {
            for (int z = 3; z <= 6; z++) {
                set(level, origin, x, 1, z, Blocks.BROWN_CARPET.defaultBlockState());
            }
        }

        set(level, origin, 2, 1, 2, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 2, 2, 2, Blocks.BOOKSHELF.defaultBlockState());

        for (int x = 3; x <= 5; x++) {
            set(level, origin, x, 1, 7,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        }

        set(level, origin, 2, 1, 7, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishKitchenAndDining(ServerLevel level, BlockPos origin) {
        set(level, origin, 12, 1, 2, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 3, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 12, 1, 4, Blocks.FURNACE.defaultBlockState());
        set(level, origin, 12, 1, 5, Blocks.SMOKER.defaultBlockState());
        set(level, origin, 12, 1, 6, Blocks.BARREL.defaultBlockState());

        set(level, origin, 9, 1, 6, Blocks.OAK_FENCE.defaultBlockState());
        set(level, origin, 9, 2, 6, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        set(level, origin, 10, 1, 6,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
        set(level, origin, 8, 1, 6,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
    }

    private static void furnishGroundStudy(ServerLevel level, BlockPos origin) {
        set(level, origin, 2, 1, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 2, 2, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 1, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 2, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 1, 15, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 3, 1, 16, Blocks.CHEST.defaultBlockState());
        set(level, origin, 4, 1, 16, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishUtilityRoom(ServerLevel level, BlockPos origin) {
        set(level, origin, 11, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 17, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 10, 1, 17, Blocks.CAULDRON.defaultBlockState());
    }

    private static void furnishHall(ServerLevel level, BlockPos origin) {
        for (int z = 11; z <= 14; z++) {
            set(level, origin, CENTER_X, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }

        set(level, origin, 6, 1, 11, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishUpperBedrooms(ServerLevel level, BlockPos origin) {
        // Front-left bedroom.
        placeBed(level, origin, 2, 7, 6, Direction.SOUTH);
        set(level, origin, 2, 7, 2, Blocks.CHEST.defaultBlockState());
        set(level, origin, 4, 7, 2, Blocks.BOOKSHELF.defaultBlockState());
        for (int z = 3; z <= 5; z++) {
            set(level, origin, 3, 7, z, Blocks.BLUE_CARPET.defaultBlockState());
        }

        // Front-right bedroom.
        placeBed(level, origin, 12, 7, 6, Direction.SOUTH);
        set(level, origin, 12, 7, 2, Blocks.CHEST.defaultBlockState());
        set(level, origin, 10, 7, 2, Blocks.BOOKSHELF.defaultBlockState());
        for (int z = 3; z <= 5; z++) {
            set(level, origin, 11, 7, z, Blocks.GREEN_CARPET.defaultBlockState());
        }

        // Larger rear bedroom.
        placeBed(level, origin, CENTER_X, 7, 14, Direction.SOUTH);
        set(level, origin, 11, 7, 16, Blocks.CHEST.defaultBlockState());
        set(level, origin, 2, 7, 16, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 7, 16, Blocks.BOOKSHELF.defaultBlockState());

        for (int x = 5; x <= 9; x++) {
            set(level, origin, x, 7, 12, Blocks.GRAY_CARPET.defaultBlockState());
        }

        // Simple landing runner.
        for (int z = 1; z <= 9; z++) {
            set(level, origin, CENTER_X, 7, z, Blocks.RED_CARPET.defaultBlockState());
        }
    }

    private static void furnishBasement(ServerLevel level, BlockPos origin) {
        set(level, origin, 3, -4, 4, Blocks.CHEST.defaultBlockState());
        set(level, origin, 11, -4, 4, Blocks.CHEST.defaultBlockState());

        set(level, origin, 2, -4, 12, Blocks.BARREL.defaultBlockState());
        set(level, origin, 3, -4, 12, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 4, -4, 12, Blocks.FURNACE.defaultBlockState());
        set(level, origin, 5, -4, 12, Blocks.BLAST_FURNACE.defaultBlockState());

        set(level, origin, 11, -4, 11, Blocks.CAULDRON.defaultBlockState());
        set(level, origin, 12, -4, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, -4, 12, Blocks.COAL_BLOCK.defaultBlockState());
    }

    public static void revealImpossibleDoor(ServerLevel level, BlockPos origin) {
        for (int x = 6; x <= 8; x++) {
            for (int y = 1; y <= 4; y++) {
                set(level, origin, x, y, 15, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        placeDoor(level, origin, CENTER_X, 1, 15, Direction.NORTH, Blocks.SPRUCE_DOOR.defaultBlockState());

        for (int z = 16; z <= 17; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                for (int y = 1; y <= 4; y++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }

            for (int y = 1; y <= 4; y++) {
                set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }

            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 5, z, Blocks.SPRUCE_SLAB.defaultBlockState());
            }

            set(level, origin, CENTER_X, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }
    }

    private static void installLighting(ServerLevel level, BlockPos origin) {
        placeHangingLantern(level, origin, 5, 3, 4);
        placeHangingLantern(level, origin, 10, 3, 4);
        placeHangingLantern(level, origin, CENTER_X, 3, 13);
        placeHangingLantern(level, origin, 3, 3, 14);
        placeHangingLantern(level, origin, 11, 3, 14);

        placeHangingLantern(level, origin, CENTER_X, 9, 2);
        placeHangingLantern(level, origin, 3, 9, 6);
        placeHangingLantern(level, origin, 11, 9, 6);
        placeHangingLantern(level, origin, CENTER_X, 9, 14);

        placeHangingLantern(level, origin, 3, -2, 8);
        placeHangingLantern(level, origin, 11, -2, 8);
        placeHangingLantern(level, origin, CENTER_X, -2, 15);
    }

    public static void applyDomesticLootTables(ServerLevel level, BlockPos origin) {
        assignLoot(level, origin.offset(3, 1, 16), STUDY_LOOT, 0x51A7D11L);

        assignLoot(level, origin.offset(2, 7, 2), BEDROOM_LOOT, 0xBED001L);
        assignLoot(level, origin.offset(12, 7, 2), BEDROOM_LOOT, 0xBED002L);
        assignLoot(level, origin.offset(11, 7, 16), BEDROOM_LOOT, 0xBED003L);

        assignLoot(level, origin.offset(3, -4, 4), BASEMENT_LOOT, 0xBA5E01L);
        assignLoot(level, origin.offset(11, -4, 4), BASEMENT_LOOT, 0xBA5E02L);
    }

    private static void assignLoot(
            ServerLevel level,
            BlockPos pos,
            ResourceKey<LootTable> table,
            long salt
    ) {
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            long seed = level.getSeed() ^ pos.asLong() ^ salt;
            chest.setLootTable(table, seed);
            chest.setChanged();
        }
    }

    private static ResourceKey<LootTable> lootTable(String path) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, path)
        );
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
