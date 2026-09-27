package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
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
import net.minecraft.world.level.levelgen.Heightmap;

public final class HouseBuilder {
    public static final int WIDTH = 15;
    public static final int DEPTH = 19;
    public static final int HEIGHT = 21;

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
        HouseSiteProfile site = HouseSiteProfile.capture(level, origin);

        clearAboveGroundVolume(level, origin);
        buildBasement(level, origin);
        buildFloors(level, origin);
        buildExteriorWalls(level, origin);
        buildMainInteriorWalls(level, origin);
        buildUpperInteriorWalls(level, origin);
        installDoorsAndWindows(level, origin);
        buildExteriorDetailing(level, origin);
        buildCeilingsAndRoof(level, origin);
        buildStaircases(level, origin);
        buildPorch(level, origin, site);
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

    /**
     * Scores an otherwise buildable site. Lower is better. The footprint itself
     * strongly prefers dry ground, while water in front is allowed because the
     * porch can adapt into a supported landing.
     */
    public static int siteScore(ServerLevel level, BlockPos origin) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int footprintWater = 0;

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;
                int height = surfaceHeight(level, worldX, worldZ);

                minY = Math.min(minY, height);
                maxY = Math.max(maxY, height);

                if (surfaceIsWater(level, worldX, worldZ, height)) {
                    footprintWater++;
                }
            }
        }

        if (footprintWater > 4) {
            return Integer.MAX_VALUE;
        }

        HouseSiteProfile site = HouseSiteProfile.capture(level, origin);
        int relief = maxY - minY;
        int porchDrop = Math.max(0, origin.getY() - site.frontGroundY());

        return relief * 30
                + footprintWater * 80
                + Math.min(8, porchDrop) * 5
                + (site.frontWater() ? 12 : 0);
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

    private static void buildExteriorDetailing(ServerLevel level, BlockPos origin) {
        // Strong vertical bays are the main difference between "box with windows"
        // and a facade that reads as timber-framed architecture.
        for (int x : new int[]{5, 9}) {
            for (int y = 1; y <= UPPER_WALL_TOP_Y; y++) {
                if (y != SECOND_FLOOR_Y) {
                    set(level, origin, x, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                }
            }
        }

        for (int z : new int[]{6, 12}) {
            for (int y = 1; y <= UPPER_WALL_TOP_Y; y++) {
                if (y != SECOND_FLOOR_Y) {
                    set(level, origin, 0, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                    set(level, origin, WIDTH - 1, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                }
            }
        }

        // Projecting sills/headers add one block of facade depth around the
        // major front window bays.
        for (int[] band : new int[][]{{1, 4}, {7, 10}}) {
            int sillY = band[0];
            int headerY = band[1];

            for (int x = 1; x <= 4; x++) {
                set(level, origin, x, sillY, -1, Blocks.DARK_OAK_SLAB.defaultBlockState());
                set(level, origin, x, headerY, -1, Blocks.DARK_OAK_SLAB.defaultBlockState());
            }

            for (int x = 10; x <= 13; x++) {
                set(level, origin, x, sillY, -1, Blocks.DARK_OAK_SLAB.defaultBlockState());
                set(level, origin, x, headerY, -1, Blocks.DARK_OAK_SLAB.defaultBlockState());
            }
        }

        // Stone plinth at the front corners keeps the taller two-story facade
        // visually grounded.
        for (int x : new int[]{0, WIDTH - 1}) {
            set(level, origin, x, 0, -1, Blocks.STONE_BRICKS.defaultBlockState());
            set(level, origin, x, 0, -2, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        }
    }

    private static void buildCeilingsAndRoof(ServerLevel level, BlockPos origin) {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, x, 5, z, Blocks.SPRUCE_SLAB.defaultBlockState());
                set(level, origin, x, ROOF_BASE_Y, z, Blocks.SPRUCE_SLAB.defaultBlockState());
            }
        }

        // Main roof: a complete steep gable with a one-block overhang, a
        // deepslate body, and dark-oak verge boards at the front/back. The old
        // version exposed each stair tread from below and read like teeth.
        for (int z = -1; z <= DEPTH; z++) {
            for (int layer = 0; layer <= 7; layer++) {
                int y = ROOF_BASE_Y + layer;
                int leftX = -1 + layer;
                int rightX = WIDTH - layer;

                set(level, origin, leftX, y, z,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.WEST));
                set(level, origin, rightX, y, z,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.EAST));
            }

            set(level, origin, CENTER_X, ROOF_BASE_Y + 8, z,
                    Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }

        // Front/back bargeboards visually bind the stair courses into one roof
        // edge instead of a row of isolated stair noses.
        for (int z : new int[]{-2, DEPTH + 1}) {
            for (int layer = 0; layer <= 7; layer++) {
                int y = ROOF_BASE_Y + layer;
                int leftX = -1 + layer;
                int rightX = WIDTH - layer;

                set(level, origin, leftX, y, z,
                        Blocks.DARK_OAK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.WEST));
                set(level, origin, rightX, y, z,
                        Blocks.DARK_OAK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.EAST));
            }

            set(level, origin, CENTER_X, ROOF_BASE_Y + 8, z,
                    Blocks.DARK_OAK_SLAB.defaultBlockState());
        }

        // Filled plaster gables with a central timber post and twin attic panes.
        for (int layer = 0; layer <= 7; layer++) {
            int y = ROOF_BASE_Y + layer;
            int leftEdge = -1 + layer;
            int rightEdge = WIDTH - layer;

            for (int x = Math.max(0, leftEdge + 1);
                    x <= Math.min(WIDTH - 1, rightEdge - 1);
                    x++) {
                set(level, origin, x, y, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, REAR_WALL_Z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        for (int y = ROOF_BASE_Y; y <= ROOF_BASE_Y + 7; y++) {
            set(level, origin, CENTER_X, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, CENTER_X, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        for (int x : new int[]{CENTER_X - 1, CENTER_X + 1}) {
            for (int y = ROOF_BASE_Y + 3; y <= ROOF_BASE_Y + 4; y++) {
                set(level, origin, x, y, 0, Blocks.GLASS_PANE.defaultBlockState());
            }
        }

        // One horizontal tie breaks up the huge triangular plaster field.
        for (int x = 3; x <= WIDTH - 4; x++) {
            set(level, origin, x, ROOF_BASE_Y + 2, 0, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
    }

    private static void buildStaircases(ServerLevel level, BlockPos origin) {
        // A one-wide open stair gives the living room back most of its volume.
        // The previous two-wide flight dominated the room visually.
        for (int z = 4; z <= 8; z++) {
            set(level, origin, 6, 5, z, Blocks.AIR.defaultBlockState());
            set(level, origin, 6, SECOND_FLOOR_Y, z, Blocks.AIR.defaultBlockState());
        }

        for (int step = 0; step < 5; step++) {
            int z = 8 - step;
            int y = 1 + step;

            set(level, origin, 6, y, z,
                    Blocks.SPRUCE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));

            // A rising rail on the open side keeps the stair readable as one
            // architectural element instead of a stack of blocks.
            set(level, origin, 7, y + 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
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

    private static void buildPorch(
            ServerLevel level,
            BlockPos origin,
            HouseSiteProfile site
    ) {
        for (int x = 4; x <= 10; x++) {
            for (int z = -3; z <= -1; z++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        // Front posts and header.
        for (int x : new int[]{4, 10}) {
            for (int y = 1; y <= 4; y++) {
                set(level, origin, x, y, -3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            }

            extendSupportToTerrain(
                    level,
                    origin,
                    x,
                    -3,
                    -1,
                    site.frontWater()
                            ? Blocks.STONE_BRICKS.defaultBlockState()
                            : Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
            );
        }

        for (int x = 4; x <= 10; x++) {
            set(level, origin, x, 4, -3, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // A separate pitched porch roof gives the facade a second scale and
        // breaks up the huge flat front wall.
        for (int z = -3; z <= 0; z++) {
            for (int layer = 0; layer <= 3; layer++) {
                int y = 5 + layer;
                int leftX = 3 + layer;
                int rightX = 11 - layer;

                set(level, origin, leftX, y, z,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.WEST));
                set(level, origin, rightX, y, z,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.EAST));
            }

            set(level, origin, CENTER_X, 9, z,
                    Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }

        // Dark-oak front verge on the porch roof.
        for (int layer = 0; layer <= 3; layer++) {
            int y = 5 + layer;
            int leftX = 3 + layer;
            int rightX = 11 - layer;

            set(level, origin, leftX, y, -4,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, rightX, y, -4,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
        }
        set(level, origin, CENTER_X, 9, -4, Blocks.DARK_OAK_SLAB.defaultBlockState());

        // Small plaster gable behind the porch verge.
        for (int layer = 0; layer <= 3; layer++) {
            int y = 5 + layer;
            int left = 3 + layer;
            int right = 11 - layer;
            for (int x = left + 1; x < right; x++) {
                set(level, origin, x, y, -3, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int y = 5; y <= 8; y++) {
            set(level, origin, CENTER_X, y, -3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        if (site.frontWater()) {
            buildWaterApproach(level, origin);
        } else {
            buildGroundApproach(level, origin, site.frontGroundY());
        }

        placeHangingLantern(level, origin, CENTER_X, 3, -2);
    }

    private static void buildGroundApproach(
            ServerLevel level,
            BlockPos origin,
            int absoluteGroundY
    ) {
        int drop = Math.max(0, origin.getY() - absoluteGroundY);
        int courses = Math.max(1, Math.min(6, drop + 1));

        for (int step = 0; step < courses; step++) {
            int z = -4 - step;
            int y = -step;

            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, y, z,
                        Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                                // The approach descends northward away from the
                                // porch, so the stair backs/ascending direction
                                // face south toward The Oldest House.
                                .setValue(StairBlock.FACING, Direction.SOUTH));
                extendSupportToTerrain(
                        level,
                        origin,
                        x,
                        z,
                        y - 1,
                        Blocks.STONE_BRICKS.defaultBlockState()
                );
            }
        }
    }

    private static void buildWaterApproach(ServerLevel level, BlockPos origin) {
        // If the front approach is water, do not generate stairs that terminate
        // underwater. A short three-wide landing reads as a modest dock instead.
        for (int z = -4; z >= -7; z--) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        for (int x : new int[]{6, 8}) {
            extendSupportToTerrain(
                    level,
                    origin,
                    x,
                    -7,
                    -1,
                    Blocks.STONE_BRICKS.defaultBlockState()
            );
            set(level, origin, x, 1, -7, Blocks.SPRUCE_FENCE.defaultBlockState());
        }

        placeHangingLantern(level, origin, CENTER_X, 2, -7);
    }

    private static void extendSupportToTerrain(
            ServerLevel level,
            BlockPos origin,
            int relX,
            int relZ,
            int startRelY,
            BlockState supportState
    ) {
        for (int relY = startRelY; relY >= startRelY - 12; relY--) {
            BlockPos pos = origin.offset(relX, relY, relZ);
            BlockState current = level.getBlockState(pos);

            if (!current.isAir()
                    && !current.canBeReplaced()
                    && level.getFluidState(pos).isEmpty()) {
                return;
            }

            level.setBlock(pos, supportState, 3);
        }
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

        // Short landing runners stop at the open stairwell instead of
        // trying to float carpet over the two-wide opening.
        for (int z = 1; z <= 3; z++) {
            set(level, origin, CENTER_X, 7, z, Blocks.RED_CARPET.defaultBlockState());
        }
        set(level, origin, CENTER_X, 7, 9, Blocks.RED_CARPET.defaultBlockState());
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

    private static int surfaceHeight(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    private static boolean surfaceIsWater(
            ServerLevel level,
            int x,
            int z,
            int surfaceHeight
    ) {
        BlockPos surfaceBlock = new BlockPos(x, surfaceHeight - 1, z);
        return level.getFluidState(surfaceBlock).is(FluidTags.WATER);
    }

    private record HouseSiteProfile(
            int frontGroundY,
            boolean frontWater
    ) {
        static HouseSiteProfile capture(ServerLevel level, BlockPos origin) {
            int[] heights = new int[12];
            int index = 0;
            int waterSamples = 0;

            for (int z = -4; z >= -7; z--) {
                for (int x = 6; x <= 8; x++) {
                    int worldX = origin.getX() + x;
                    int worldZ = origin.getZ() + z;
                    int height = surfaceHeight(level, worldX, worldZ);

                    heights[index++] = height;
                    if (surfaceIsWater(level, worldX, worldZ, height)) {
                        waterSamples++;
                    }
                }
            }

            java.util.Arrays.sort(heights);
            int median = heights[heights.length / 2];

            return new HouseSiteProfile(
                    median,
                    waterSamples >= 4
            );
        }
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
