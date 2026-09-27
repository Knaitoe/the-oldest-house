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
import net.minecraft.world.level.block.WallBannerBlock;
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

    public static final int DOMESTIC_MIN_X = -2;
    public static final int DOMESTIC_MAX_X = WIDTH + 1;
    public static final int DOMESTIC_MIN_Z = -10;
    public static final int DOMESTIC_MAX_Z = DEPTH + 1;

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
        // Fixed architecture only. The adaptive ground/dock approach deliberately
        // meets the existing terrain instead of requiring an empty runway.
        for (int x = -2; x <= WIDTH + 1; x++) {
            for (int z = -4; z <= DEPTH + 1; z++) {
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
        for (int x = -2; x <= WIDTH + 1; x++) {
            for (int z = -4; z <= DEPTH + 1; z++) {
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
        // One floor per story. No decorative slab sandwich between levels.
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

        // These projected volumes turn former exterior edge cells into real room.
        for (int x = 1; x <= 5; x++) {
            set(level, origin, x, 0, 0, Blocks.OAK_PLANKS.defaultBlockState());
        }
        for (int x = 9; x <= 13; x++) {
            set(level, origin, x, SECOND_FLOOR_Y, 0, Blocks.OAK_PLANKS.defaultBlockState());
        }

        // Entry/rear circulation uses a darker floor material as a subtle wayfinding
        // device instead of carpet pasted through every room.
        for (int z = 1; z <= 15; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        for (int z = 8; z <= 10; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildExteriorWalls(ServerLevel level, BlockPos origin) {
        buildExteriorWallBand(level, origin, 1, 5);
        buildExteriorWallBand(level, origin, 7, UPPER_WALL_TOP_Y);

        // Ground-floor living bay projects toward the road.
        for (int x = 1; x <= 5; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -1, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // The right upstairs bedroom projects independently, creating a second,
        // taller front mass without forcing the whole second story outward.
        for (int x = 9; x <= 13; x++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -1, Blocks.WHITE_TERRACOTTA.defaultBlockState());
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
        // The front half is intentionally not chopped into three little boxes.
        // Four posts and two headers establish a real central hall while keeping
        // long views between foyer, living room and kitchen.
        for (int[] post : new int[][]{{5, 1}, {9, 1}, {5, 8}, {9, 8}}) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, post[0], y, post[1],
                        Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            }
        }
        for (int x = 5; x <= 9; x++) {
            set(level, origin, x, 5, 1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            set(level, origin, x, 5, 8, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Front/rear division with a centered door from the stair hall.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y <= 5; y++) {
                if (x == CENTER_X && y <= 2) {
                    continue;
                }
                set(level, origin, x, y, 9, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // Rear hall remains the stable launch point for impossible architecture.
        for (int z = REAR_HALL_START_Z; z < REAR_WALL_Z; z++) {
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
        // Two front bedrooms flank a central stair/landing hall.
        for (int z = 1; z <= 9; z++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                boolean leftDoor = z == 8 && y <= 8;
                boolean rightDoor = z == 8 && y <= 8;

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

        placeDoor(level, origin, 5, 7, 8, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 7, 8, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, CENTER_X, 7, 10, Direction.SOUTH, Blocks.SPRUCE_DOOR.defaultBlockState());

        // Living-room bay: broad, low front glazing.
        for (int x : new int[]{2, 3, 4}) {
            placeWindowColumn(level, origin, x, -1, 2, 3);
        }

        // Kitchen remains on the original wall plane under the projecting bedroom.
        for (int x : new int[]{11, 12}) {
            placeWindowColumn(level, origin, x, 0, 2, 3);
        }

        // Side light is sparse and deliberately avoids the fireplace mass.
        for (int z : new int[]{2, 3}) {
            placeWindowColumn(level, origin, 0, z, 2, 3);
        }
        for (int z : new int[]{4, 5}) {
            placeWindowColumn(level, origin, WIDTH - 1, z, 2, 3);
        }

        // Upper-left bedroom stays recessed; right bedroom sits in the projecting gable.
        for (int x : new int[]{2, 3}) {
            placeWindowColumn(level, origin, x, 0, 8, 9);
        }
        for (int x : new int[]{11, 12}) {
            placeWindowColumn(level, origin, x, -1, 8, 9);
        }

        for (int z : new int[]{1, 2, 13, 14}) {
            placeWindowColumn(level, origin, 0, z, 8, 9);
        }
        for (int z : new int[]{4, 5, 13, 14}) {
            placeWindowColumn(level, origin, WIDTH - 1, z, 8, 9);
        }

        // Rear bedroom gets actual rear-facing daylight without touching the
        // impossible-door centerline.
        for (int x : new int[]{3, 4, 10, 11}) {
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
        // Main corners.
        for (int y = 1; y <= UPPER_WALL_TOP_Y; y++) {
            set(level, origin, 0, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 0, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        // Living bay framing. These lines explain the projection structurally.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 1, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 5, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 1; x <= 5; x++) {
            set(level, origin, x, 5, -1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Recessed entry frame.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 6, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 8, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 6; x <= 8; x++) {
            set(level, origin, x, 5, 0, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Projecting upper-right gable framing and its actual jetty.
        for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
            set(level, origin, 9, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 13, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 9; x <= 13; x++) {
            set(level, origin, x, 6, -1, Blocks.DARK_OAK_SLAB.defaultBlockState());
            set(level, origin, x, UPPER_WALL_TOP_Y, -1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Restrained side framing gives long walls rhythm without turning them
        // into graph paper.
        for (int z : new int[]{6, 12}) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, 0, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                set(level, origin, WIDTH - 1, y, z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            }
        }
    }

    private static void buildCeilingsAndRoof(ServerLevel level, BlockPos origin) {
        // The second-floor structure is the downstairs ceiling. Dark beams hang
        // one block below it only in the rooms, creating framed ceiling fields
        // instead of a duplicate full ceiling layer.
        for (int z : new int[]{2, 6}) {
            for (int x = 1; x <= 5; x++) {
                set(level, origin, x, 5, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
            for (int x = 9; x <= 13; x++) {
                set(level, origin, x, 5, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }

        // One upper ceiling layer beneath the roof.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, x, ROOF_BASE_Y, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }

        // Main roof ridge runs left-right. From the front you see an eave and
        // roof plane, which gives the front-facing cross gables something to
        // intersect rather than making the whole house one enormous triangle.
        for (int layer = 0; layer < 5; layer++) {
            int y = ROOF_BASE_Y + layer;
            int frontBlockZ = -2 + layer * 2;
            int frontStairZ = frontBlockZ + 1;
            int rearBlockZ = DEPTH + 1 - layer * 2;
            int rearStairZ = rearBlockZ - 1;

            for (int x = -1; x <= WIDTH; x++) {
                set(level, origin, x, y, frontBlockZ, Blocks.DEEPSLATE_TILES.defaultBlockState());
                set(level, origin, x, y, frontStairZ,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.SOUTH));

                set(level, origin, x, y, rearBlockZ, Blocks.DEEPSLATE_TILES.defaultBlockState());
                set(level, origin, x, y, rearStairZ,
                        Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.NORTH));
            }
        }

        for (int x = -1; x <= WIDTH; x++) {
            set(level, origin, x, ROOF_BASE_Y + 5, 8, Blocks.DEEPSLATE_TILES.defaultBlockState());
            set(level, origin, x, ROOF_BASE_Y + 5, 10, Blocks.DEEPSLATE_TILES.defaultBlockState());
            set(level, origin, x, ROOF_BASE_Y + 5, 9, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }

        // Low canopy over the living bay. It marks the one-story projection
        // without pretending to be another full gable.
        for (int x = 0; x <= 6; x++) {
            set(level, origin, x, 6, -2,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }

        // Small dormer above the recessed left bedroom.
        for (int z = -1; z <= 3; z++) {
            set(level, origin, 1, 12, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, 5, 12, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
            set(level, origin, 2, 13, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, 4, 13, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
            set(level, origin, 3, 14, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }
        for (int x = 2; x <= 4; x++) {
            set(level, origin, x, 12, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        }
        set(level, origin, 3, 13, 0, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        set(level, origin, 3, 12, 0, Blocks.GLASS_PANE.defaultBlockState());

        // Dominant front-facing cross gable over the projecting right bedroom.
        for (int z = -4; z <= 4; z++) {
            set(level, origin, 8, 11, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, 14, 11, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));

            set(level, origin, 9, 12, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, 13, 12, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));

            set(level, origin, 10, 13, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, 12, 13, z,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));

            set(level, origin, 11, 14, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }

        for (int y = 11; y <= 13; y++) {
            int inset = y - 11;
            for (int x = 9 + inset; x <= 13 - inset; x++) {
                set(level, origin, x, y, -1, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        set(level, origin, 9, 11, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 13, 11, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 10, 12, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 12, 12, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 11, 13, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 11, 11, -1, Blocks.GLASS_PANE.defaultBlockState());
    }

    private static void buildStaircases(ServerLevel level, BlockPos origin) {
        // A two-wide stair rises through the central hall, not through the living
        // room. The fireplace wall and seating group now get to exist without a
        // staircase dangling over them.
        for (int x = 7; x <= 8; x++) {
            for (int z = 3; z <= 7; z++) {
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.AIR.defaultBlockState());
            }
        }

        for (int step = 0; step < 5; step++) {
            int z = 3 + step;
            int y = 1 + step;
            for (int x = 7; x <= 8; x++) {
                set(level, origin, x, y, z,
                        Blocks.SPRUCE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.SOUTH));
            }
        }

        // Upper guard follows the actual opening only.
        for (int z = 3; z <= 7; z++) {
            set(level, origin, 6, 7, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, origin, 9, 7, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        }

        // Basement stair remains in the utility side of the rear plan.
        for (int z = 14; z <= 16; z++) {
            set(level, origin, 11, 0, z, Blocks.AIR.defaultBlockState());
        }
        for (int step = 0; step < 3; step++) {
            int z = 14 + step;
            int y = -1 - step;
            set(level, origin, 11, y, z,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }
        set(level, origin, 12, -4, 16,
                Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST));

        for (int z = 14; z <= 16; z++) {
            set(level, origin, 10, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, origin, 12, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        }
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
        // Layered hearth rug. Furniture replaces individual carpet cells where
        // needed, so the textile reads as one composition rather than confetti.
        placeCarpet(level, origin, Blocks.BROWN_CARPET.defaultBlockState(), 1,
                new int[][]{
                        {2, 3}, {3, 3}, {4, 3}, {5, 3},
                        {2, 4}, {3, 4}, {4, 4}, {5, 4},
                        {2, 5}, {3, 5}, {4, 5}, {5, 5},
                        {2, 6}, {3, 6}, {4, 6}, {5, 6},
                        {2, 7}, {3, 7}, {4, 7}, {5, 7}
                });
        placeCarpet(level, origin, Blocks.LIGHT_GRAY_CARPET.defaultBlockState(), 1,
                new int[][]{{3, 4}, {4, 4}, {3, 6}, {4, 6}});

        // Built-ins make the fireplace an architectural wall, not a brick object
        // standing alone in a blank room.
        for (int y = 1; y <= 3; y++) {
            set(level, origin, 1, y, 2, Blocks.BOOKSHELF.defaultBlockState());
            set(level, origin, 1, y, 8, Blocks.BOOKSHELF.defaultBlockState());
        }
        set(level, origin, 1, 1, 3, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 1, 1, 7, Blocks.CHISELED_BOOKSHELF.defaultBlockState());

        // Three-seat sofa and two chairs all face the hearth.
        for (int z = 4; z <= 6; z++) {
            set(level, origin, 5, 1, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
        }
        set(level, origin, 4, 1, 3,
                Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST));
        set(level, origin, 4, 1, 7,
                Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST));

        // Coffee table and a lit side table.
        set(level, origin, 3, 1, 5, Blocks.OAK_FENCE.defaultBlockState());
        set(level, origin, 3, 2, 5, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        set(level, origin, 5, 1, 2, Blocks.BARREL.defaultBlockState());
        set(level, origin, 5, 2, 2, Blocks.LANTERN.defaultBlockState());

        // Window seat turns the projecting bay into usable architecture.
        for (int x = 2; x <= 4; x++) {
            set(level, origin, x, 1, 0,
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
        set(level, origin, 11, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 12, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 10, 1, 12, Blocks.CAULDRON.defaultBlockState());
    }

    private static void furnishHall(ServerLevel level, BlockPos origin) {
        for (int z = 11; z <= 14; z++) {
            setQuiet(level, origin, CENTER_X, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }
        set(level, origin, 6, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 8, 1, 11, Blocks.CHISELED_BOOKSHELF.defaultBlockState());

        // Front foyer runner stops before the stair.
        for (int z = 1; z <= 2; z++) {
            set(level, origin, CENTER_X, 1, z, Blocks.BROWN_CARPET.defaultBlockState());
        }
    }

    private static void furnishUpperBedrooms(ServerLevel level, BlockPos origin) {
        // Left: reader/writer.
        placeBed(level, origin, 2, 7, 6, Direction.SOUTH);
        set(level, origin, 2, 7, 2, Blocks.CHEST.defaultBlockState());
        set(level, origin, 4, 7, 2, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 1, 7, 6, Blocks.BARREL.defaultBlockState());
        set(level, origin, 1, 8, 6, Blocks.CANDLE.defaultBlockState());
        set(level, origin, 4, 7, 5, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 4, 7, 6, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        placeCarpet(level, origin, Blocks.BLUE_CARPET.defaultBlockState(), 7,
                new int[][]{{2, 3}, {3, 3}, {4, 3}, {2, 4}, {3, 4}, {4, 4}, {3, 5}});

        // Right: music/maker.
        placeBed(level, origin, 12, 7, 6, Direction.SOUTH);
        set(level, origin, 12, 7, 2, Blocks.CHEST.defaultBlockState());
        set(level, origin, 10, 7, 2, Blocks.LOOM.defaultBlockState());
        set(level, origin, 12, 7, 4, Blocks.JUKEBOX.defaultBlockState());
        set(level, origin, 13, 7, 6, Blocks.OAK_PLANKS.defaultBlockState());
        set(level, origin, 13, 8, 6, Blocks.LANTERN.defaultBlockState());
        set(level, origin, 10, 7, 6, Blocks.NOTE_BLOCK.defaultBlockState());
        placeCarpet(level, origin, Blocks.GREEN_CARPET.defaultBlockState(), 7,
                new int[][]{{10, 3}, {11, 3}, {12, 3}, {10, 4}, {11, 4}, {12, 4}});

        // Rear: older/formal.
        placeBed(level, origin, CENTER_X, 7, 14, Direction.SOUTH);
        set(level, origin, 11, 7, 16, Blocks.CHEST.defaultBlockState());
        set(level, origin, 2, 7, 16, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 7, 16, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 5, 7, 14, Blocks.BARREL.defaultBlockState());
        set(level, origin, 5, 8, 14, Blocks.CANDLE.defaultBlockState());
        set(level, origin, 9, 7, 14, Blocks.CHEST.defaultBlockState());
        set(level, origin, 11, 7, 12, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        set(level, origin, 12, 7, 12, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 12, 7, 15, Blocks.LECTERN.defaultBlockState());

        placeCarpet(level, origin, Blocks.GRAY_CARPET.defaultBlockState(), 7,
                new int[][]{
                        {4, 12}, {5, 12}, {6, 12}, {7, 12}, {8, 12}, {9, 12}, {10, 12},
                        {5, 13}, {6, 13}, {7, 13}, {8, 13}, {9, 13}
                });

        for (int z = 8; z <= 9; z++) {
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
        // Main front door. Keep both the porch-side and room-side approach free.
        clearCollisionVolume(level, origin, 6, 8, 1, 3, -1, -1);
        clearCollisionVolume(level, origin, 6, 8, 1, 3, 1, 2);

        // Ground-floor central hall door. This is the route that the previous
        // stair/railing pass managed to obstruct.
        clearCollisionVolume(level, origin, 6, 8, 1, 3, 8, 8);
        clearCollisionVolume(level, origin, 6, 8, 1, 3, 10, 10);

        // Ground-floor side-room doors at x=5 and x=9, z=13.
        clearCollisionVolume(level, origin, 4, 4, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 6, 6, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 8, 8, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 10, 10, 1, 3, 12, 14);

        // Upstairs front-bedroom doors share a solid landing behind the
        // stairwell. Their approaches must never overlap the open shaft.
        clearCollisionVolume(level, origin, 4, 4, 7, 9, 7, 9);
        clearCollisionVolume(level, origin, 6, 6, 7, 9, 7, 9);
        clearCollisionVolume(level, origin, 8, 8, 7, 9, 7, 9);
        clearCollisionVolume(level, origin, 10, 10, 7, 9, 7, 9);

        // Upstairs rear-bedroom doorway.
        clearCollisionVolume(level, origin, 6, 8, 7, 9, 9, 9);
        clearCollisionVolume(level, origin, 6, 8, 7, 9, 11, 11);
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
        // Downstairs fixtures align with room centers rather than forming a grid.
        placeHangingLantern(level, origin, 3, 4, 5);
        placeHangingLantern(level, origin, CENTER_X, 4, 2);
        placeHangingLantern(level, origin, 11, 4, 5);
        placeHangingLantern(level, origin, 3, 4, 14);
        placeHangingLantern(level, origin, 11, 4, 13);
        placeHangingLantern(level, origin, CENTER_X, 4, 12);

        placeHangingLantern(level, origin, 3, 9, 5);
        placeHangingLantern(level, origin, 11, 9, 5);
        placeHangingLantern(level, origin, CENTER_X, 9, 13);

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


    public static void spawnDomesticPaintings(ServerLevel level, BlockPos origin) {
        // Paintings replace the old floating-banner approach and are positioned
        // only where a full backing wall exists.
        spawnPainting(level, origin.offset(3, 2, 8), Direction.NORTH, PaintingVariants.MATCH);
        spawnPainting(level, origin.offset(3, 2, 17), Direction.NORTH, PaintingVariants.PLANT);
        spawnPainting(level, origin.offset(1, 8, 4), Direction.EAST, PaintingVariants.BUST);
        spawnPainting(level, origin.offset(13, 8, 4), Direction.WEST, PaintingVariants.MATCH);
        spawnPainting(level, origin.offset(7, 8, 17), Direction.NORTH, PaintingVariants.BAROQUE);
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
