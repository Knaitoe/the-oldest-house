package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
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
    public static final int HEIGHT = 23;

    public static final int DOMESTIC_MIN_X = -3;
    public static final int DOMESTIC_MAX_X = WIDTH + 2;
    public static final int DOMESTIC_MIN_Z = -10;
    public static final int DOMESTIC_MAX_Z = DEPTH + 2;

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
        for (int x = -3; x <= WIDTH + 2; x++) {
            for (int z = -5; z <= DEPTH + 2; z++) {
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

        // This is the last destructive pass. Doors/windows are authored afterward
        // so "clear the doorway" can never delete the doorway again.
        enforceDoorwayClearance(level, origin);
        installDoorsAndWindows(level, origin);

        applyDomesticLootTables(level, origin);
        spawnDomesticPaintings(level, origin);
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
        for (int x = -3; x <= WIDTH + 2; x++) {
            for (int z = -5; z <= DEPTH + 2; z++) {
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
                BlockState ground = (x == 0 || x == WIDTH - 1 || z == 0 || z == REAR_WALL_Z)
                        ? Blocks.STONE_BRICKS.defaultBlockState()
                        : Blocks.OAK_PLANKS.defaultBlockState();
                set(level, origin, x, 0, z, ground);

                BlockState upper = (x == 0 || x == WIDTH - 1 || z == 0 || z == REAR_WALL_Z)
                        ? Blocks.DARK_OAK_PLANKS.defaultBlockState()
                        : Blocks.OAK_PLANKS.defaultBlockState();
                set(level, origin, x, SECOND_FLOOR_Y, z, upper);
            }
        }

        // Ground floor projections.
        for (int x = 1; x <= 5; x++) {
            for (int z = -2; z <= 0; z++) {
                set(level, origin, x, 0, z, Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        for (int x = 9; x <= 13; x++) {
            for (int z = -1; z <= 0; z++) {
                set(level, origin, x, 0, z, Blocks.OAK_PLANKS.defaultBlockState());
            }
        }

        // Upper stories jetty one additional block beyond the ground-floor wall.
        for (int x = 1; x <= 5; x++) {
            for (int z = -3; z <= 0; z++) {
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        for (int x = 9; x <= 13; x++) {
            for (int z = -2; z <= 0; z++) {
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.OAK_PLANKS.defaultBlockState());
            }
        }

        for (int z = 11; z <= 16; z++) {
            set(level, origin, 15, 0, z, Blocks.STONE_BRICKS.defaultBlockState());
            set(level, origin, 15, SECOND_FLOOR_Y, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        for (int z = 1; z <= 15; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
        for (int z = 1; z <= 16; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildExteriorWalls(ServerLevel level, BlockPos origin) {
        buildExteriorWallBand(level, origin, 1, 5);
        buildExteriorWallBand(level, origin, 7, UPPER_WALL_TOP_Y);

        // Left wing: two-block ground projection, three-block upper projection.
        for (int x = 0; x <= 5; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -2, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -3, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int z = -1; z <= 0; z++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, 0, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int z = -2; z <= 0; z++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, 0, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // Right wing: one-block ground projection, two-block upper jetty.
        for (int x = 9; x <= 14; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -1, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -2, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 9, y, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            set(level, origin, 14, y, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        }
        for (int z = -1; z <= 0; z++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, 14, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // Rear-right stair bay.
        for (int z = 11; z <= 16; z++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, 14, y, z, Blocks.AIR.defaultBlockState());
                set(level, origin, 15, y, z, Blocks.STONE_BRICKS.defaultBlockState());
            }
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, 14, y, z, Blocks.AIR.defaultBlockState());
                set(level, origin, 15, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int x = 14; x <= 15; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 10, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, origin, x, y, 17, Blocks.STONE_BRICKS.defaultBlockState());
            }
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, 17, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
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
        for (int z = 1; z <= 9; z++) {
            for (int y = 1; y <= 5; y++) {
                boolean frontArch = z >= 3 && z <= 6 && y <= 3;
                if (!frontArch) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        for (int x = 1; x <= 5; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int x = 9; x <= 13; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        for (int z = 10; z < REAR_WALL_Z; z++) {
            for (int y = 1; y <= 5; y++) {
                boolean doorway = z == 13 && y <= 2;
                if (!doorway) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        for (int x = 6; x <= 8; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 15, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
    }

    private static void buildUpperInteriorWalls(ServerLevel level, BlockPos origin) {
        for (int z = 1; z <= 17; z++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                boolean leftDoor = (z == 7 || z == 13) && y <= 8;
                boolean rightOpening = (z == 7 || z == 13 || z == 14) && y <= 8;

                if (!leftDoor) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
                if (!rightOpening) {
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        for (int x = 1; x <= 5; x++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                if (!(x == 3 && y <= 8)) {
                    set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }
        for (int x = 9; x <= 13; x++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                if (!(x == 11 && y <= 8)) {
                    set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }
    }

    private static void installDoorsAndWindows(ServerLevel level, BlockPos origin) {
        placeDoor(level, origin, CENTER_X, 1, 0, Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());
        placeDoor(level, origin, 5, 1, 13, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 1, 13, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());

        placeDoor(level, origin, 5, 7, 7, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 7, 7, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 5, 7, 13, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());

        for (int x : new int[]{2, 3, 4}) {
            placeWindowColumn(level, origin, x, -2, 2, 3);
            placeWindowColumn(level, origin, x, -3, 8, 9);
        }

        for (int x : new int[]{11, 12}) {
            placeWindowColumn(level, origin, x, -1, 2, 3);
            placeWindowColumn(level, origin, x, -2, 8, 9);
        }

        for (int z : new int[]{2, 3, 7, 8}) {
            placeWindowColumn(level, origin, 0, z, 2, 3);
        }
        for (int z : new int[]{3, 4, 7}) {
            placeWindowColumn(level, origin, WIDTH - 1, z, 2, 3);
        }

        for (int z : new int[]{2, 3, 13, 14}) {
            placeWindowColumn(level, origin, 0, z, 8, 9);
        }
        for (int z : new int[]{3, 4}) {
            placeWindowColumn(level, origin, WIDTH - 1, z, 8, 9);
        }

        for (int z : new int[]{12, 15}) {
            placeWindowColumn(level, origin, 15, z, 2, 3);
            placeWindowColumn(level, origin, 15, z, 8, 9);
        }

        for (int x : new int[]{2, 3, 4}) {
            placeWindowColumn(level, origin, x, REAR_WALL_Z, 8, 9);
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
        for (int x = 0; x < WIDTH; x++) {
            set(level, origin, x, 0, REAR_WALL_Z, Blocks.STONE_BRICKS.defaultBlockState());
        }

        // Left wing lower frame.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 0, y, -2, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 5, y, -2, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        // Left jetty and upper frame.
        for (int x = 0; x <= 5; x++) {
            set(level, origin, x, 6, -3, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
        for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
            set(level, origin, 0, y, -3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 5, y, -3, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 0; x <= 5; x++) {
            set(level, origin, x, 10, -3, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Recessed center entry.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 6, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 8, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 6; x <= 8; x++) {
            set(level, origin, x, 5, 0, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Right wing lower frame and upper jetty.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 9, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 14, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 9; x <= 14; x++) {
            set(level, origin, x, 6, -2, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
        for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
            set(level, origin, 9, y, -2, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 14, y, -2, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 9; x <= 14; x++) {
            set(level, origin, x, 10, -2, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        for (int y = 7; y <= 12; y++) {
            set(level, origin, 15, y, 11, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 15, y, 16, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int z = 11; z <= 16; z++) {
            set(level, origin, 15, 10, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        for (int z : new int[]{10, 17}) {
            for (int y = 1; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, 0, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            }
        }
    }

    private static void buildCeilingsAndRoof(ServerLevel level, BlockPos origin) {
        RoofProfile roof = RoofProfile.capture(level, origin);

        for (int z : new int[]{2, 6}) {
            for (int x = 1; x <= 5; x++) {
                set(level, origin, x, 5, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
            for (int x = 9; x <= 13; x++) {
                set(level, origin, x, 5, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }

        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, x, ROOF_BASE_Y, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }

        buildGableAlongZ(level, origin, -2, 7, -4, 10, ROOF_BASE_Y, roof.leftRise());
        buildGableAlongZ(
                level,
                origin,
                7,
                16,
                -3,
                10,
                ROOF_BASE_Y + roof.rightBaseLift(),
                roof.rightRise()
        );
        buildGableAlongX(level, origin, -2, 16, 6, 21, ROOF_BASE_Y, roof.rearRise());

        fillFrontGable(level, origin, -1, 6, -3, ROOF_BASE_Y, roof.leftRise(), 2);
        fillFrontGable(
                level,
                origin,
                8,
                15,
                -2,
                ROOF_BASE_Y + roof.rightBaseLift(),
                roof.rightRise(),
                11
        );

        for (int z = -4; z <= 10; z++) {
            set(level, origin, -3, ROOF_BASE_Y - 1, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
            set(level, origin, 8, ROOF_BASE_Y - 1, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
            set(level, origin, 6, ROOF_BASE_Y - 1, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
            set(level, origin, 17, ROOF_BASE_Y - 1, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
        }

        for (int y = 11; y <= 12; y++) {
            for (int z = 11; z <= 16; z++) {
                set(level, origin, 15, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        buildGableAlongZ(level, origin, 11, 17, 9, 18, 13, roof.towerRise());

        int dormerX = roof.dormerOnLeft() ? 2 : 11;
        int dormerZ = 8;
        for (int z = dormerZ - 2; z <= dormerZ + 1; z++) {
            set(level, origin, dormerX - 2, 14, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, dormerX + 2, 14, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
            set(level, origin, dormerX - 1, 15, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, dormerX + 1, 15, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
            set(level, origin, dormerX, 16, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }
        set(level, origin, dormerX, 14, dormerZ - 1, Blocks.GLASS_PANE.defaultBlockState());
    }


    private static void buildGableAlongZ(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int baseY,
            int rise
    ) {
        for (int layer = 0; layer < rise; layer++) {
            int y = baseY + layer;
            int leftX = minX + layer;
            int rightX = maxX - layer;

            for (int z = minZ; z <= maxZ; z++) {
                set(level, origin, leftX, y, z,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.WEST));
                set(level, origin, rightX, y, z,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.EAST));
            }

            set(level, origin, leftX, y, minZ - 1,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, rightX, y, minZ - 1,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
            set(level, origin, leftX, y, maxZ + 1,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, rightX, y, maxZ + 1,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
        }

        int ridgeY = baseY + rise;
        int ridgeMinX = minX + rise;
        int ridgeMaxX = maxX - rise;
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = ridgeMinX; x <= ridgeMaxX; x++) {
                set(level, origin, x, ridgeY, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
            }
        }
    }

    private static void buildGableAlongX(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int baseY,
            int rise
    ) {
        for (int layer = 0; layer < rise; layer++) {
            int y = baseY + layer;
            int frontZ = minZ + layer;
            int rearZ = maxZ - layer;

            for (int x = minX; x <= maxX; x++) {
                set(level, origin, x, y, frontZ,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.NORTH));
                set(level, origin, x, y, rearZ,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.SOUTH));
            }
        }

        int ridgeY = baseY + rise;
        int ridgeMinZ = minZ + rise;
        int ridgeMaxZ = maxZ - rise;
        for (int x = minX; x <= maxX; x++) {
            for (int z = ridgeMinZ; z <= ridgeMaxZ; z++) {
                set(level, origin, x, ridgeY, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
            }
        }
    }

    private static void fillFrontGable(
            ServerLevel level,
            BlockPos origin,
            int wallMinX,
            int wallMaxX,
            int z,
            int baseY,
            int rise,
            int centerX
    ) {
        for (int layer = 0; layer < rise; layer++) {
            int y = baseY + layer;
            int left = wallMinX + layer;
            int right = wallMaxX - layer;
            if (left > right) {
                break;
            }

            for (int x = left; x <= right; x++) {
                set(level, origin, x, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }

            set(level, origin, left, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, right, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        for (int y = baseY; y < baseY + rise; y++) {
            set(level, origin, centerX, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        if (rise >= 4) {
            set(level, origin, centerX, baseY + 1, z, Blocks.GLASS_PANE.defaultBlockState());
        }
    }

    private static void buildStaircases(ServerLevel level, BlockPos origin) {
        for (int x = 9; x <= 12; x++) {
            for (int z = 13; z <= 16; z++) {
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.AIR.defaultBlockState());
            }
        }

        for (int step = 0; step < 3; step++) {
            int z = 16 - step;
            int y = 1 + step;
            for (int x = 11; x <= 12; x++) {
                set(level, origin, x, y, z,
                        Blocks.SPRUCE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.NORTH));
            }
        }

        for (int x = 10; x <= 12; x++) {
            for (int z = 13; z <= 14; z++) {
                set(level, origin, x, 4, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }

        for (int z = 13; z <= 14; z++) {
            set(level, origin, 10, 4, z,
                    Blocks.SPRUCE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, 9, 5, z,
                    Blocks.SPRUCE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
        }

        for (int z = 13; z <= 16; z++) {
            set(level, origin, 13, 7, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        for (int x = 10; x <= 12; x++) {
            set(level, origin, x, 7, 16, Blocks.SPRUCE_FENCE.defaultBlockState());
        }

        for (int z = 14; z <= 16; z++) {
            set(level, origin, 11, 0, z, Blocks.AIR.defaultBlockState());
        }
        for (int step = 0; step < 3; step++) {
            set(level, origin, 11, -1 - step, 14 + step,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }
        set(level, origin, 12, -4, 16,
                Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST));
    }

    private static void buildPorch(
            ServerLevel level,
            BlockPos origin,
            HouseSiteProfile site
    ) {
        // Small recessed-entry porch. It is intentionally subordinate to the
        // two front gables instead of adding a third competing triangle.
        for (int x = 6; x <= 8; x++) {
            for (int z = -3; z <= -1; z++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        for (int x : new int[]{6, 8}) {
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

        for (int x = 6; x <= 8; x++) {
            set(level, origin, x, 4, -3, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            set(level, origin, x, 5, -1,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
            set(level, origin, x, 4, -2,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
            set(level, origin, x, 3, -3,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }

        if (site.frontWater()) {
            buildWaterApproach(level, origin, site.frontGroundY());
        } else {
            buildGroundApproach(level, origin, site.frontGroundY());
        }

        // The chain replaces the center of the porch header, visibly anchoring it.
        placeHangingLantern(level, origin, CENTER_X, 3, -3);
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

    private static void buildWaterApproach(
            ServerLevel level,
            BlockPos origin,
            int absoluteWaterSurfaceY
    ) {
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
                    Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
            );
        }

        for (int z = -4; z >= -7; z--) {
            set(level, origin, 6, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            if (z != -6) {
                set(level, origin, 8, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            }
        }
        set(level, origin, 7, 1, -7, Blocks.SPRUCE_FENCE.defaultBlockState());

        // Dock light is a real post with a bracket and hanging lantern.
        for (int y = 1; y <= 4; y++) {
            set(level, origin, 6, y, -7, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        set(level, origin, 7, 4, -7, Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(level, origin, 7, 3, -7,
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));

        extendSupportToTerrain(
                level,
                origin,
                8,
                -6,
                -1,
                Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
        );

        int waterRelativeY = absoluteWaterSurfaceY - origin.getY();
        int ladderBottomY = Math.max(-6, Math.min(-1, waterRelativeY - 2));
        for (int y = 0; y >= ladderBottomY; y--) {
            set(level, origin, 9, y, -6,
                    Blocks.LADDER.defaultBlockState()
                            .setValue(LadderBlock.FACING, Direction.EAST));
        }
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
        // Full-height exterior chimney mass.
        for (int y = 1; y <= 18; y++) {
            for (int z = 4; z <= 6; z++) {
                set(level, origin, 0, y, z, Blocks.BRICKS.defaultBlockState());
            }
        }

        // Fireplace opening and surround inside the living room.
        set(level, origin, 1, 1, 4, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 1, 1, 5, Blocks.CAMPFIRE.defaultBlockState());
        set(level, origin, 1, 1, 6, Blocks.BRICKS.defaultBlockState());

        for (int y = 2; y <= 3; y++) {
            set(level, origin, 1, y, 4, Blocks.BRICKS.defaultBlockState());
            set(level, origin, 1, y, 6, Blocks.BRICKS.defaultBlockState());
        }
        for (int z = 4; z <= 6; z++) {
            set(level, origin, 1, 4, z, Blocks.BRICKS.defaultBlockState());
            set(level, origin, 2, 1, z, Blocks.STONE_BRICK_SLAB.defaultBlockState());
            set(level, origin, 1, 4, z, Blocks.DARK_OAK_SLAB.defaultBlockState());
        }
    }

    private static void furnishLivingRoom(ServerLevel level, BlockPos origin) {
        placeCarpet(level, origin, Blocks.BROWN_CARPET.defaultBlockState(), 1,
                new int[][]{
                        {2, 3}, {3, 3}, {4, 3},
                        {2, 4}, {3, 4}, {4, 4},
                        {2, 5}, {3, 5}, {4, 5},
                        {2, 6}, {3, 6}, {4, 6},
                        {2, 7}, {3, 7}, {4, 7}
                });
        placeCarpet(level, origin, Blocks.LIGHT_GRAY_CARPET.defaultBlockState(), 1,
                new int[][]{{3, 4}, {3, 6}});

        for (int y = 1; y <= 3; y++) {
            set(level, origin, 1, y, 2, Blocks.BOOKSHELF.defaultBlockState());
            set(level, origin, 1, y, 8, Blocks.BOOKSHELF.defaultBlockState());
        }
        set(level, origin, 1, 1, 3, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 1, 1, 7, Blocks.CHISELED_BOOKSHELF.defaultBlockState());

        for (int z = 4; z <= 6; z++) {
            set(level, origin, 4, 1, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
        }
        set(level, origin, 4, 1, 3,
                Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.EAST));
        set(level, origin, 4, 1, 7,
                Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.EAST));

        set(level, origin, 3, 1, 5, Blocks.OAK_FENCE.defaultBlockState());
        set(level, origin, 3, 2, 5, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        set(level, origin, 5, 1, 2, Blocks.BARREL.defaultBlockState());
        set(level, origin, 5, 2, 2, Blocks.LANTERN.defaultBlockState());

        for (int x = 2; x <= 4; x++) {
            set(level, origin, x, 1, -1,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH));
        }
    }

    private static void furnishKitchenAndDining(ServerLevel level, BlockPos origin) {
        // Built-in work wall.
        set(level, origin, 13, 1, 2, Blocks.BARREL.defaultBlockState());
        set(level, origin, 13, 1, 3, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 13, 1, 4, Blocks.FURNACE.defaultBlockState());
        set(level, origin, 13, 1, 5, Blocks.SMOKER.defaultBlockState());
        set(level, origin, 13, 1, 6, Blocks.BARREL.defaultBlockState());
        set(level, origin, 13, 1, 7, Blocks.CAULDRON.defaultBlockState());

        // Small preparation island.
        set(level, origin, 10, 1, 4, Blocks.BARREL.defaultBlockState());
        set(level, origin, 11, 1, 4, Blocks.BARREL.defaultBlockState());
        set(level, origin, 10, 2, 4, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        set(level, origin, 11, 2, 4, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());

        // Dining table tucked toward the rear of the room with opposed chairs.
        for (int x = 10; x <= 12; x++) {
            set(level, origin, x, 1, 7, Blocks.OAK_FENCE.defaultBlockState());
            set(level, origin, x, 2, 7, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        }
        set(level, origin, 11, 1, 6,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
        set(level, origin, 11, 1, 8,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));

        set(level, origin, 10, 1, 2, Blocks.COMPOSTER.defaultBlockState());
    }

    private static void furnishGroundStudy(ServerLevel level, BlockPos origin) {
        // Library wall.
        for (int x = 1; x <= 4; x++) {
            set(level, origin, x, 1, 11, Blocks.BOOKSHELF.defaultBlockState());
        }
        set(level, origin, 2, 2, 11, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 2, 11, Blocks.BOOKSHELF.defaultBlockState());

        // Desk and document storage.
        set(level, origin, 2, 1, 15, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        set(level, origin, 3, 1, 15, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 3, 1, 16, Blocks.CHEST.defaultBlockState());
        set(level, origin, 4, 1, 16, Blocks.BARREL.defaultBlockState());

        placeCarpet(level, origin, Blocks.RED_CARPET.defaultBlockState(), 1,
                new int[][]{{2, 13}, {3, 13}, {4, 13}, {2, 14}, {3, 14}, {4, 14}});
    }

    private static void furnishUtilityRoom(ServerLevel level, BlockPos origin) {
        set(level, origin, 10, 1, 11, Blocks.CAULDRON.defaultBlockState());
        set(level, origin, 11, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 13, 1, 12, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 13, 1, 13, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishHall(ServerLevel level, BlockPos origin) {
        for (int z = 1; z <= 14; z++) {
            setQuiet(level, origin, CENTER_X, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }

        set(level, origin, 5, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 9, 1, 11, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
    }

    private static void furnishUpperBedrooms(ServerLevel level, BlockPos origin) {
        placeBed(level, origin, 2, 7, 6, Direction.SOUTH);
        set(level, origin, 2, 7, 1, Blocks.CHEST.defaultBlockState());
        set(level, origin, 4, 7, 2, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 1, 7, 6, Blocks.BARREL.defaultBlockState());
        set(level, origin, 1, 8, 6, Blocks.CANDLE.defaultBlockState());
        set(level, origin, 4, 7, 5, Blocks.LECTERN.defaultBlockState());
        placeCarpet(level, origin, Blocks.BLUE_CARPET.defaultBlockState(), 7,
                new int[][]{{2, 3}, {3, 3}, {4, 3}, {2, 4}, {3, 4}, {4, 4}});

        placeBed(level, origin, 12, 7, 6, Direction.SOUTH);
        set(level, origin, 12, 7, 1, Blocks.CHEST.defaultBlockState());
        set(level, origin, 10, 7, 2, Blocks.LOOM.defaultBlockState());
        set(level, origin, 12, 7, 4, Blocks.JUKEBOX.defaultBlockState());
        set(level, origin, 13, 7, 6, Blocks.OAK_PLANKS.defaultBlockState());
        set(level, origin, 13, 8, 6, Blocks.LANTERN.defaultBlockState());
        set(level, origin, 10, 7, 6, Blocks.NOTE_BLOCK.defaultBlockState());
        placeCarpet(level, origin, Blocks.GREEN_CARPET.defaultBlockState(), 7,
                new int[][]{{10, 3}, {11, 3}, {12, 3}, {10, 4}, {11, 4}, {12, 4}});

        placeBed(level, origin, 3, 7, 14, Direction.SOUTH);
        set(level, origin, 2, 7, 16, Blocks.CHEST.defaultBlockState());
        set(level, origin, 1, 7, 12, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 2, 7, 12, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 4, 7, 12, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        set(level, origin, 4, 7, 16, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 1, 7, 15, Blocks.BARREL.defaultBlockState());
        set(level, origin, 1, 8, 15, Blocks.CANDLE.defaultBlockState());
        placeCarpet(level, origin, Blocks.GRAY_CARPET.defaultBlockState(), 7,
                new int[][]{{2, 13}, {3, 13}, {4, 13}, {2, 14}, {3, 14}, {4, 14}});

        set(level, origin, 11, 7, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 12, 7, 11, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 13, 7, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 12, 7, 12,
                Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.NORTH));

        for (int z = 1; z <= 12; z++) {
            set(level, origin, CENTER_X, 7, z, Blocks.RED_CARPET.defaultBlockState());
        }
    }

    private static void placeCarpet(
            ServerLevel level,
            BlockPos origin,
            BlockState carpet,
            int y,
            int[][] cells
    ) {
        for (int[] cell : cells) {
            set(level, origin, cell[0], y, cell[1], carpet);
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
            for (int y = 1; y <= 5; y++) {
                setQuiet(level, origin, x, y, 15, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // The new door appears closed. Opening it is the player's decision.
        placeDoor(
                level,
                origin,
                CENTER_X,
                1,
                15,
                Direction.NORTH,
                Blocks.SPRUCE_DOOR.defaultBlockState()
        );

        for (int z = 16; z <= 17; z++) {
            for (int x = 6; x <= 8; x++) {
                setQuiet(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                for (int y = 1; y <= 4; y++) {
                    setQuiet(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
                setQuiet(level, origin, x, 5, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }

            for (int y = 1; y <= 4; y++) {
                setQuiet(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                setQuiet(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }

            setQuiet(level, origin, CENTER_X, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }

        clearCollisionVolume(level, origin, 6, 8, 1, 3, 14, 14);
        clearCollisionVolume(level, origin, 6, 8, 1, 3, 16, 16);
    }

    private static void enforceDoorwayClearance(ServerLevel level, BlockPos origin) {
        clearCollisionVolume(level, origin, 6, 8, 1, 3, 1, 14);

        clearCollisionVolume(level, origin, 4, 4, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 6, 6, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 8, 8, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 10, 10, 1, 3, 12, 14);

        clearCollisionVolume(level, origin, 4, 4, 7, 9, 6, 8);
        clearCollisionVolume(level, origin, 6, 6, 7, 9, 6, 8);
        clearCollisionVolume(level, origin, 8, 8, 7, 9, 6, 8);
        clearCollisionVolume(level, origin, 10, 10, 7, 9, 6, 8);
        clearCollisionVolume(level, origin, 4, 4, 7, 9, 12, 14);
        clearCollisionVolume(level, origin, 6, 6, 7, 9, 12, 14);
    }

    private static void clearCollisionVolume(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ
    ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    // Preserve carpets and other zero-collision decoration; only
                    // structural/furniture blocks capable of physically blocking
                    // the passage are removed.
                    if (!state.isAir()
                            && !state.getCollisionShape(level, pos).isEmpty()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void installLighting(ServerLevel level, BlockPos origin) {
        placeHangingLantern(level, origin, 3, 4, 5);
        placeHangingLantern(level, origin, 11, 4, 5);
        placeHangingLantern(level, origin, 3, 4, 14);
        placeHangingLantern(level, origin, 12, 4, 12);

        placeHangingLantern(level, origin, 6, 4, 5);
        placeHangingLantern(level, origin, 8, 4, 11);

        placeHangingLantern(level, origin, 3, 9, 5);
        placeHangingLantern(level, origin, 11, 9, 5);
        placeHangingLantern(level, origin, 3, 9, 14);
        placeHangingLantern(level, origin, 12, 9, 12);

        placeHangingLantern(level, origin, 3, -2, 8);
        placeHangingLantern(level, origin, 11, -2, 8);
        placeHangingLantern(level, origin, CENTER_X, -2, 15);
    }

    public static void applyDomesticLootTables(ServerLevel level, BlockPos origin) {
        assignLoot(level, origin.offset(3, 1, 16), STUDY_LOOT, 0x51A7D11L);

        assignLoot(level, origin.offset(2, 7, 1), BEDROOM_LOOT, 0xBED001L);
        assignLoot(level, origin.offset(12, 7, 1), BEDROOM_LOOT, 0xBED002L);
        assignLoot(level, origin.offset(2, 7, 16), BEDROOM_LOOT, 0xBED003L);

        assignLoot(level, origin.offset(3, -4, 4), BASEMENT_LOOT, 0xBA5E01L);
        assignLoot(level, origin.offset(11, -4, 4), BASEMENT_LOOT, 0xBA5E02L);
    }


    public static void spawnDomesticPaintings(ServerLevel level, BlockPos origin) {
        spawnPainting(level, origin.offset(4, 2, 9), Direction.NORTH, PaintingVariants.MATCH);
        spawnPainting(level, origin.offset(3, 2, 17), Direction.NORTH, PaintingVariants.PLANT);
        spawnPainting(level, origin.offset(1, 8, 5), Direction.EAST, PaintingVariants.BUST);
        spawnPainting(level, origin.offset(13, 8, 5), Direction.WEST, PaintingVariants.MATCH);
        spawnPainting(level, origin.offset(4, 8, 17), Direction.NORTH, PaintingVariants.BAROQUE);
    }

    private static void spawnPainting(
            ServerLevel level,
            BlockPos pos,
            Direction facing,
            ResourceKey<PaintingVariant> variantKey
    ) {
        Holder<PaintingVariant> variant = level.registryAccess()
                .lookupOrThrow(Registries.PAINTING_VARIANT)
                .getOrThrow(variantKey);

        Painting painting = new Painting(level, pos, facing, variant);
        if (painting.survives()) {
            level.addFreshEntity(painting);
        }
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


    private record RoofProfile(
            int leftRise,
            int rightRise,
            int rightBaseLift,
            int rearRise,
            int towerRise,
            boolean dormerOnLeft
    ) {
        static RoofProfile capture(ServerLevel level, BlockPos origin) {
            long hash = level.getSeed() ^ origin.asLong() ^ 0x5A17C0DEL;
            return new RoofProfile(
                    4,
                    4,
                    0,
                    5 + (int) ((hash >>> 2) & 1L),
                    3,
                    ((hash >>> 4) & 1L) == 0L
            );
        }
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
        BlockState closed = baseState
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.OPEN, false);

        set(level, origin, x, y, z,
                closed.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, origin, x, y + 1, z,
                closed.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private static void setQuiet(
            ServerLevel level,
            BlockPos origin,
            int x,
            int y,
            int z,
            BlockState state
    ) {
        level.setBlock(
                origin.offset(x, y, z),
                state,
                Block.UPDATE_CLIENTS
                        | Block.UPDATE_KNOWN_SHAPE
                        | Block.UPDATE_SUPPRESS_DROPS
        );
    }

    private static void set(ServerLevel level, BlockPos origin, int x, int y, int z, BlockState state) {
        level.setBlock(origin.offset(x, y, z), state, 3);
    }
}
