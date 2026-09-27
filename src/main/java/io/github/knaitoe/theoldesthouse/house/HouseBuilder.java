package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.Arrays;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;

public final class HouseBuilder {
    public static final int WIDTH = 15;
    public static final int DEPTH = 19;
    public static final int HEIGHT = 19;

    // Everything the authored domestic build may occupy, including porch,
    // adaptive approach, roof overhangs, and the one-story study wing.
    public static final int DOMESTIC_MIN_X = -3;
    public static final int DOMESTIC_MAX_X = WIDTH + 1;
    public static final int DOMESTIC_MIN_Z = -10;
    public static final int DOMESTIC_MAX_Z = DEPTH;

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

    public static boolean canBuildAt(ServerLevel level, BlockPos origin) {
        for (int x = -3; x <= WIDTH + 1; x++) {
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

    public static void build(ServerLevel level, BlockPos origin) {
        HouseSiteProfile site = HouseSiteProfile.capture(level, origin);

        clearAboveGroundVolume(level, origin);
        buildFoundationAndBasement(level, origin);
        buildDomesticFloors(level, origin);
        buildGroundShell(level, origin);
        buildUpperShell(level, origin);
        buildGroundPartitions(level, origin);
        buildUpperPartitions(level, origin);

        buildMainStair(level, origin);
        buildBasementStair(level, origin);
        buildFireplaceAndChimney(level, origin);

        installDoors(level, origin);
        restoreWindows(level, origin);
        buildExteriorFraming(level, origin);
        buildMainRoof(level, origin);
        buildStudyWingRoof(level, origin);
        buildProjectedUpperGable(level, origin);
        buildPorch(level, origin, site);

        furnishLivingRoom(level, origin);
        furnishKitchenAndDining(level, origin);
        furnishGroundStudy(level, origin);
        furnishUtilityRoom(level, origin);
        furnishHall(level, origin);
        furnishUpperBedrooms(level, origin);
        furnishBasement(level, origin);
        installLighting(level, origin);

        enforceDoorwayClearance(level, origin);
        restoreWindows(level, origin);
        applyDomesticLootTables(level, origin);
        spawnDomesticPaintings(level, origin);
    }

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
        // Clear only the fixed architectural envelope. The adaptive stair/dock
        // approach extends farther forward and must meet the existing terrain
        // rather than bulldozing a ten-block runway through it.
        for (int x = -3; x <= WIDTH + 1; x++) {
            for (int z = -4; z <= DEPTH; z++) {
                for (int y = 0; y <= HEIGHT; y++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildFoundationAndBasement(ServerLevel level, BlockPos origin) {
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                set(level, origin, x, BASEMENT_FLOOR_Y, z, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, origin, x, 0, z, Blocks.STONE_BRICKS.defaultBlockState());
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

        // Sparse, intentional structural piers instead of a grid of obstacles.
        for (int y = BASEMENT_FLOOR_Y + 1; y <= -1; y++) {
            set(level, origin, 3, y, 10, Blocks.STONE_BRICKS.defaultBlockState());
            set(level, origin, 11, y, 10, Blocks.STONE_BRICKS.defaultBlockState());
        }
    }

    private static void buildDomesticFloors(ServerLevel level, BlockPos origin) {
        // Ground and second floor. The second-floor blocks themselves are the
        // downstairs ceiling; there is deliberately no decorative slab layer
        // trapped between stories.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                set(level, origin, x, 0, z, Blocks.OAK_PLANKS.defaultBlockState());
                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.OAK_PLANKS.defaultBlockState());
            }
        }

        // The front-left bay is real room area rather than a hollow facade.
        for (int x = 1; x <= 4; x++) {
            set(level, origin, x, 0, 0, Blocks.OAK_PLANKS.defaultBlockState());
        }

        // The study grows one block through the west wall into a one-story wing.
        for (int z = 11; z <= 16; z++) {
            set(level, origin, 0, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
        }

        // Front edge remains solid beneath the upper-story projection.
        for (int x = 1; x < WIDTH - 1; x++) {
            set(level, origin, x, SECOND_FLOOR_Y, 0, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Rear hall gets a darker runner base.
        for (int z = REAR_HALL_START_Z; z <= 15; z++) {
            for (int x = 6; x <= 8; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildGroundShell(ServerLevel level, BlockPos origin) {
        wallBand(level, origin, 1, 5, 0, REAR_WALL_Z);

        // Front-left bay: move the exterior wall one block forward and open the
        // old wall plane into the living room.
        for (int x = 1; x <= 4; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -1, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 1, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 4, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        // Rear-left study wing: a true one-story side projection. The old west
        // wall is opened between z=11..16, while the new wall moves to x=-1.
        for (int z = 11; z <= 16; z++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, 0, y, z, Blocks.AIR.defaultBlockState());
                set(level, origin, -1, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int x = -1; x <= 0; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, 17, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int y = 1; y <= 5; y++) {
            set(level, origin, -1, y, 10, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, -1, y, 17, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
    }

    private static void buildUpperShell(ServerLevel level, BlockPos origin) {
        wallBand(level, origin, 7, UPPER_WALL_TOP_Y, 0, REAR_WALL_Z);

        // The right-hand bedroom projects toward the front, counterbalancing the
        // lower left bay and rear study wing.
        for (int x = 9; x <= 13; x++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                set(level, origin, x, y, 0, Blocks.AIR.defaultBlockState());
                set(level, origin, x, y, -1, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
        for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
            set(level, origin, 9, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 13, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        // A short jetty only under the projecting room. Keeping this local avoids
        // recreating the old mysterious second band around the entire facade.
        for (int x = 9; x <= 13; x++) {
            set(level, origin, x, 6, -1, Blocks.DARK_OAK_SLAB.defaultBlockState());
        }
    }

    private static void wallBand(
            ServerLevel level,
            BlockPos origin,
            int minY,
            int maxY,
            int frontZ,
            int backZ
    ) {
        for (int y = minY; y <= maxY; y++) {
            for (int x = 0; x < WIDTH; x++) {
                set(level, origin, x, y, frontZ, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, x, y, backZ, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
            for (int z = frontZ + 1; z < backZ; z++) {
                set(level, origin, 0, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                set(level, origin, WIDTH - 1, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
    }

    private static void buildGroundPartitions(ServerLevel level, BlockPos origin) {
        // Front/rear division with central door.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y <= 5; y++) {
                if (x == CENTER_X && y <= 2) {
                    continue;
                }
                set(level, origin, x, y, 9, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // Central rear hall.
        for (int z = REAR_HALL_START_Z; z < REAR_WALL_Z; z++) {
            for (int y = 1; y <= 5; y++) {
                boolean doorway = z == 13 && y <= 2;
                if (!doorway) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        // Ordinary threshold wall. The impossible door later replaces its center.
        for (int x = 6; x <= 8; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, 15, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
    }

    private static void buildUpperPartitions(ServerLevel level, BlockPos origin) {
        // Front bedrooms flank a central landing/corridor.
        for (int z = 1; z <= 9; z++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                if (!(z == 8 && y <= 8)) {
                    set(level, origin, 5, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                    set(level, origin, 9, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        // Rear bedroom.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                if (x == CENTER_X && y <= 8) {
                    continue;
                }
                set(level, origin, x, y, 10, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }
    }

    private static void buildMainStair(ServerLevel level, BlockPos origin) {
        // Stair is tucked against the left side of the central circulation bay.
        // It is a single visual mass with no parallel fence staircase.
        for (int z = 3; z <= 7; z++) {
            set(level, origin, 5, SECOND_FLOOR_Y, z, Blocks.AIR.defaultBlockState());
        }

        for (int step = 0; step < 5; step++) {
            int z = 7 - step;
            int y = 1 + step;
            set(level, origin, 5, y, z,
                    Blocks.SPRUCE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }

        // Upper guard only where there is an actual drop.
        for (int z = 3; z <= 7; z++) {
            set(level, origin, 6, 7, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        set(level, origin, 5, 7, 8, Blocks.SPRUCE_FENCE.defaultBlockState());
    }

    private static void buildBasementStair(ServerLevel level, BlockPos origin) {
        // Three-step descent, landing, then a 90-degree turn into the cellar.
        for (int z = 14; z <= 16; z++) {
            set(level, origin, 11, 0, z, Blocks.AIR.defaultBlockState());
        }

        for (int step = 0; step < 3; step++) {
            set(level, origin, 11, -1 - step, 14 + step,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }

        set(level, origin, 11, -4, 17, Blocks.STONE_BRICKS.defaultBlockState());
        set(level, origin, 12, -4, 17,
                Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST));
        set(level, origin, 13, -4, 17, Blocks.STONE_BRICK_SLAB.defaultBlockState());

        // Rails only around the opening upstairs.
        for (int z = 14; z <= 16; z++) {
            set(level, origin, 10, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, origin, 12, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        }
    }

    private static void buildFireplaceAndChimney(ServerLevel level, BlockPos origin) {
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

        for (int y = 5; y <= 18; y++) {
            set(level, origin, 0, y, 5, Blocks.BRICKS.defaultBlockState());
        }
    }

    private static void installDoors(ServerLevel level, BlockPos origin) {
        placeDoor(level, origin, CENTER_X, 1, 0, Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());
        placeDoor(level, origin, CENTER_X, 1, 9, Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());
        placeDoor(level, origin, 5, 1, 13, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 1, 13, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());

        placeDoor(level, origin, 5, 7, 8, Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 9, 7, 8, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, CENTER_X, 7, 10, Direction.SOUTH, Blocks.SPRUCE_DOOR.defaultBlockState());
    }

    private static void restoreWindows(ServerLevel level, BlockPos origin) {
        // Ground-floor front glazing.
        for (int x : new int[]{11, 12}) {
            paneColumn(level, origin, x, 0, 2, 3);
        }

        // Upper-left bedroom stays on the original facade plane.
        for (int x : new int[]{2, 3}) {
            paneColumn(level, origin, x, 0, 8, 9);
        }

        // Projected ground-left bay and upper-right gable room.
        for (int x : new int[]{2, 3}) {
            paneColumn(level, origin, x, -1, 2, 3);
        }
        for (int x : new int[]{11, 12}) {
            paneColumn(level, origin, x, -1, 8, 9);
        }

        // Living room and kitchen side windows.
        for (int z : new int[]{7, 8}) {
            paneColumn(level, origin, 0, z, 2, 3);
        }
        for (int z : new int[]{4, 5}) {
            paneColumn(level, origin, WIDTH - 1, z, 2, 3);
        }

        // Study-wing side window on the actual projected wall.
        for (int z : new int[]{13, 14}) {
            paneColumn(level, origin, -1, z, 2, 3);
        }

        // Upper side windows. The west-rear pair is intentionally omitted because
        // the lower study roof now intersects that elevation.
        for (int z : new int[]{2, 3}) {
            paneColumn(level, origin, 0, z, 8, 9);
        }
        for (int z : new int[]{4, 5, 13, 14}) {
            paneColumn(level, origin, WIDTH - 1, z, 8, 9);
        }

        // Rear bedroom gets its light from the back wall instead.
        for (int x : new int[]{6, 7}) {
            paneColumn(level, origin, x, REAR_WALL_Z, 8, 9);
        }
    }

    private static void paneColumn(
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

    private static void buildExteriorFraming(ServerLevel level, BlockPos origin) {
        // Main-house corners.
        for (int y = 1; y <= UPPER_WALL_TOP_Y; y++) {
            set(level, origin, 0, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 0, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, WIDTH - 1, y, REAR_WALL_Z, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        // Front facade framing is sparse and structural rather than a grid pasted
        // over every wall block.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 5, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 9, y, 0, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }

        // Living-room bay frame and header.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, 1, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 4, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 1; x <= 4; x++) {
            set(level, origin, x, 5, -1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // Upper projecting room gets a distinct lower and upper frame.
        for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
            set(level, origin, 9, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, 13, y, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int x = 9; x <= 13; x++) {
            set(level, origin, x, UPPER_WALL_TOP_Y, -1, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }

        // One-story study wing frame. Its roof will sit directly on this beam.
        for (int y = 1; y <= 5; y++) {
            set(level, origin, -1, y, 10, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            set(level, origin, -1, y, 17, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        for (int z = 10; z <= 17; z++) {
            set(level, origin, -1, 5, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        }
    }

    private static void buildMainRoof(ServerLevel level, BlockPos origin) {
        // Broad front/rear planes keep the main mass readable from the front.
        // The side wing and front gable break this silhouette later.
        for (int layer = 0; layer < 5; layer++) {
            int y = ROOF_BASE_Y + layer;

            int frontBlockZ = -2 + layer * 2;
            int frontStairZ = frontBlockZ + 1;
            int rearBlockZ = DEPTH - layer * 2;
            int rearStairZ = rearBlockZ - 1;

            for (int x = -2; x <= WIDTH + 1; x++) {
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

        for (int x = -2; x <= WIDTH + 1; x++) {
            set(level, origin, x, ROOF_BASE_Y + 5, 8, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
            set(level, origin, x, ROOF_BASE_Y + 5, 9, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }

        // One upstairs ceiling layer, and only one.
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int z = 1; z < REAR_WALL_Z; z++) {
                if (level.getBlockState(origin.offset(x, ROOF_BASE_Y, z)).isAir()) {
                    set(level, origin, x, ROOF_BASE_Y, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
                }
            }
        }
    }

    private static void buildProjectedUpperGable(ServerLevel level, BlockPos origin) {
        // Front-facing gable over the projecting right bedroom. It deliberately
        // has its own roof direction so the main roof is not one uninterrupted cap.
        for (int z = -4; z <= 2; z++) {
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

        // Dark timber traces the stepped gable edge without filling the whole face.
        set(level, origin, 9, 11, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 13, 11, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 10, 12, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 12, 12, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        set(level, origin, 11, 13, -1, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());

        set(level, origin, 11, 11, -1, Blocks.GLASS_PANE.defaultBlockState());
    }


    private static void buildStudyWingRoof(ServerLevel level, BlockPos origin) {
        // Lower cross-gable over the one-story study wing. The roof runs on a
        // different axis and at a different height from the main roof, giving the
        // rear-left quarter a separate mass instead of decorative trim.
        for (int x = -3; x <= 1; x++) {
            set(level, origin, x, 6, 9,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH));
            set(level, origin, x, 6, 18,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));

            set(level, origin, x, 7, 10,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH));
            set(level, origin, x, 7, 17,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));

            set(level, origin, x, 8, 11,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH));
            set(level, origin, x, 8, 16,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));

            set(level, origin, x, 9, 12,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH));
            set(level, origin, x, 9, 15,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));

            set(level, origin, x, 10, 13, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
            set(level, origin, x, 10, 14, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }

        // Western gable face above the study wall.
        for (int y = 6; y <= 9; y++) {
            int inset = y - 6;
            for (int z = 10 + inset; z <= 17 - inset; z++) {
                set(level, origin, -1, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        for (int y = 6; y <= 9; y++) {
            set(level, origin, -1, y, 13, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        set(level, origin, -1, 7, 14, Blocks.GLASS_PANE.defaultBlockState());
    }

    private static void buildPorch(
            ServerLevel level,
            BlockPos origin,
            HouseSiteProfile site
    ) {
        for (int x = 5; x <= 9; x++) {
            for (int z = -3; z <= -1; z++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        for (int x : new int[]{5, 9}) {
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

        // Simple lean-to roof. It complements the main roof instead of competing
        // with it as another miniature front gable.
        for (int x = 4; x <= 10; x++) {
            set(level, origin, x, 6, -1,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
            set(level, origin, x, 5, -2,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
            set(level, origin, x, 4, -3,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }

        if (site.frontWater()) {
            buildWaterApproach(level, origin, site.frontGroundY());
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

        // Railing circuit with one deliberate east-side ladder opening.
        for (int z = -4; z >= -7; z--) {
            set(level, origin, 6, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            if (z != -6) {
                set(level, origin, 8, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            }
        }
        set(level, origin, 7, 1, -7, Blocks.SPRUCE_FENCE.defaultBlockState());

        // Crooked dock lamp: a post and arm physically support the hanging light.
        for (int y = 1; y <= 4; y++) {
            set(level, origin, 6, y, -7, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        set(level, origin, 7, 4, -7, Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(level, origin, 7, 3, -7,
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));

        // Ladder backed by its own support column.
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

    private static void furnishLivingRoom(ServerLevel level, BlockPos origin) {
        // Hearth rug belongs on the ground floor. This used to route through a
        // helper that hardcoded y=7, which quietly dumped the living-room carpet
        // into the bedrooms upstairs.
        carpet(level, origin, Blocks.BROWN_CARPET.defaultBlockState(), 1,
                new int[][]{
                        {2, 3}, {3, 3}, {4, 3},
                        {2, 4}, {3, 4}, {4, 4},
                        {2, 5}, {3, 5}, {4, 5},
                        {2, 6}, {3, 6}, {4, 6}
                });
        carpet(level, origin, Blocks.LIGHT_GRAY_CARPET.defaultBlockState(), 1,
                new int[][]{{2, 4}, {2, 5}, {4, 4}, {4, 5}});

        // Sofa and both chairs face the west-wall fireplace.
        for (int z = 4; z <= 6; z++) {
            set(level, origin, 4, 1, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.WEST));
        }
        set(level, origin, 3, 1, 3,
                Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST));
        set(level, origin, 3, 1, 7,
                Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST));

        set(level, origin, 3, 1, 5, Blocks.OAK_FENCE.defaultBlockState());
        set(level, origin, 3, 2, 5, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());

        set(level, origin, 2, 1, 2, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 2, 2, 2, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 4, 1, 2, Blocks.BARREL.defaultBlockState());
        set(level, origin, 4, 2, 2, Blocks.LANTERN.defaultBlockState());
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

        set(level, origin, 11, 1, 8, Blocks.COMPOSTER.defaultBlockState());
        set(level, origin, 12, 1, 8, Blocks.POTTED_FERN.defaultBlockState());
    }

    private static void furnishGroundStudy(ServerLevel level, BlockPos origin) {
        set(level, origin, 2, 1, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 2, 2, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 1, 11, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 2, 11, Blocks.BOOKSHELF.defaultBlockState());

        set(level, origin, 3, 1, 15, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 3, 1, 16, Blocks.CHEST.defaultBlockState());
        set(level, origin, 4, 1, 16, Blocks.BARREL.defaultBlockState());
        set(level, origin, 2, 1, 15, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());

        carpet(level, origin, Blocks.RED_CARPET.defaultBlockState(), 1,
                new int[][]{{1, 13}, {2, 13}, {3, 13}, {4, 13}, {2, 14}, {3, 14}});
    }

    private static void furnishUtilityRoom(ServerLevel level, BlockPos origin) {
        set(level, origin, 11, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 12, 1, 12, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 10, 1, 12, Blocks.CAULDRON.defaultBlockState());
    }

    private static void furnishHall(ServerLevel level, BlockPos origin) {
        for (int z = 11; z <= 14; z++) {
            set(level, origin, CENTER_X, 1, z, Blocks.RED_CARPET.defaultBlockState());
        }
        set(level, origin, 6, 1, 11, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishUpperBedrooms(ServerLevel level, BlockPos origin) {
        // Left room: reader/writer. Books, maps and a candle make the room read as
        // a person rather than the same bedroom with a different flower.
        placeBed(level, origin, 2, 7, 6, Direction.SOUTH);
        set(level, origin, 2, 7, 2, Blocks.CHEST.defaultBlockState());
        set(level, origin, 4, 7, 2, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 1, 7, 6, Blocks.BARREL.defaultBlockState());
        set(level, origin, 1, 8, 6, Blocks.CANDLE.defaultBlockState());
        set(level, origin, 4, 7, 5, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 4, 7, 6, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());

        carpet(level, origin, Blocks.BLUE_CARPET.defaultBlockState(), 7,
                new int[][]{{2, 3}, {3, 3}, {4, 3}, {2, 4}, {3, 4}, {4, 4}, {3, 5}});
        carpet(level, origin, Blocks.LIGHT_BLUE_CARPET.defaultBlockState(), 7,
                new int[][]{{2, 5}, {4, 5}});

        // Right room: music/making. No decorative plant clone; the accessory set
        // is jukebox, note block, loom, and a small lit bedside stand.
        placeBed(level, origin, 12, 7, 6, Direction.SOUTH);
        set(level, origin, 12, 7, 2, Blocks.CHEST.defaultBlockState());
        set(level, origin, 10, 7, 2, Blocks.LOOM.defaultBlockState());
        set(level, origin, 12, 7, 4, Blocks.JUKEBOX.defaultBlockState());
        set(level, origin, 13, 7, 6, Blocks.OAK_PLANKS.defaultBlockState());
        set(level, origin, 13, 8, 6, Blocks.LANTERN.defaultBlockState());
        set(level, origin, 10, 7, 6, Blocks.NOTE_BLOCK.defaultBlockState());

        carpet(level, origin, Blocks.GREEN_CARPET.defaultBlockState(), 7,
                new int[][]{{10, 3}, {11, 3}, {12, 3}, {10, 4}, {11, 4}, {12, 4}});
        carpet(level, origin, Blocks.LIME_CARPET.defaultBlockState(), 7,
                new int[][]{{11, 2}, {11, 5}});

        // Rear room: older/formal. Wardrobe, writing surface and asymmetric
        // bedside pieces distinguish it from both front rooms.
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

        carpet(level, origin, Blocks.GRAY_CARPET.defaultBlockState(), 7,
                new int[][]{
                        {4, 12}, {5, 12}, {6, 12}, {7, 12}, {8, 12}, {9, 12}, {10, 12},
                        {5, 13}, {6, 13}, {7, 13}, {8, 13}, {9, 13}
                });
        carpet(level, origin, Blocks.LIGHT_GRAY_CARPET.defaultBlockState(), 7,
                new int[][]{{4, 13}, {10, 13}, {6, 14}, {8, 14}});

        // Landing runner only on real floor.
        for (int z = 8; z <= 9; z++) {
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

        set(level, origin, 9, -4, 12, Blocks.SMITHING_TABLE.defaultBlockState());
        set(level, origin, 10, -4, 12, Blocks.GRINDSTONE.defaultBlockState());
        set(level, origin, 11, -4, 12, Blocks.CAULDRON.defaultBlockState());
        set(level, origin, 12, -4, 12, Blocks.BARREL.defaultBlockState());

        // Cellar shelving tucked against a wall, not in circulation.
        set(level, origin, 2, -4, 16, Blocks.BARREL.defaultBlockState());
        set(level, origin, 3, -4, 16, Blocks.BARREL.defaultBlockState());
        set(level, origin, 4, -4, 16, Blocks.BARREL.defaultBlockState());
    }

    private static void installLighting(ServerLevel level, BlockPos origin) {
        placeHangingLantern(level, origin, 3, 4, 4);
        placeHangingLantern(level, origin, 11, 4, 4);
        placeHangingLantern(level, origin, CENTER_X, 4, 12);
        placeHangingLantern(level, origin, 3, 4, 14);
        placeHangingLantern(level, origin, 11, 4, 13);

        placeHangingLantern(level, origin, CENTER_X, 9, 8);
        placeHangingLantern(level, origin, 3, 9, 5);
        placeHangingLantern(level, origin, 11, 9, 5);
        placeHangingLantern(level, origin, CENTER_X, 9, 13);

        placeHangingLantern(level, origin, 3, -2, 8);
        placeHangingLantern(level, origin, 11, -2, 8);
        placeHangingLantern(level, origin, CENTER_X, -2, 15);
    }

    public static void revealImpossibleDoor(ServerLevel level, BlockPos origin) {
        for (int x = 6; x <= 8; x++) {
            for (int y = 1; y <= 5; y++) {
                setQuiet(level, origin, x, y, 15, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        // It appears closed. The player gets the ominous new door first, then
        // chooses to open it and expose the impossible sightline.
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

    public static void applyDomesticLootTables(ServerLevel level, BlockPos origin) {
        assignLoot(level, origin.offset(3, 1, 16), STUDY_LOOT, 0x51A7D11L);

        assignLoot(level, origin.offset(2, 7, 2), BEDROOM_LOOT, 0xBED001L);
        assignLoot(level, origin.offset(12, 7, 2), BEDROOM_LOOT, 0xBED002L);
        assignLoot(level, origin.offset(11, 7, 16), BEDROOM_LOOT, 0xBED003L);

        assignLoot(level, origin.offset(3, -4, 4), BASEMENT_LOOT, 0xBA5E01L);
        assignLoot(level, origin.offset(11, -4, 4), BASEMENT_LOOT, 0xBA5E02L);
    }

    public static void spawnDomesticPaintings(ServerLevel level, BlockPos origin) {
        spawnPainting(level, origin.offset(1, 8, 4), Direction.EAST, PaintingVariants.MATCH);
        spawnPainting(level, origin.offset(13, 8, 4), Direction.WEST, PaintingVariants.BUST);
        spawnPainting(level, origin.offset(3, 8, 17), Direction.NORTH, PaintingVariants.BAROQUE);
        spawnPainting(level, origin.offset(4, 2, 17), Direction.NORTH, PaintingVariants.PLANT);
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

    private static void enforceDoorwayClearance(ServerLevel level, BlockPos origin) {
        clearCollisionVolume(level, origin, 6, 8, 1, 3, -1, 2);
        clearCollisionVolume(level, origin, 6, 8, 1, 3, 8, 10);

        clearCollisionVolume(level, origin, 4, 4, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 6, 6, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 8, 8, 1, 3, 12, 14);
        clearCollisionVolume(level, origin, 10, 10, 1, 3, 12, 14);

        clearCollisionVolume(level, origin, 4, 4, 7, 9, 7, 9);
        clearCollisionVolume(level, origin, 6, 6, 7, 9, 7, 9);
        clearCollisionVolume(level, origin, 8, 8, 7, 9, 7, 9);
        clearCollisionVolume(level, origin, 10, 10, 7, 9, 7, 9);

        clearCollisionVolume(level, origin, 6, 8, 7, 9, 9, 11);
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
                    if (!state.isAir()
                            && !state.getCollisionShape(level, pos).isEmpty()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static void carpet(
            ServerLevel level,
            BlockPos origin,
            BlockState state,
            int y,
            int[][] cells
    ) {
        for (int[] cell : cells) {
            set(level, origin, cell[0], y, cell[1], state);
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

    private static void placeHangingLantern(
            ServerLevel level,
            BlockPos origin,
            int x,
            int y,
            int z
    ) {
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
        level.setBlock(origin.offset(x, y, z).relative(facing), head, 3);
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

    private static void set(
            ServerLevel level,
            BlockPos origin,
            int x,
            int y,
            int z,
            BlockState state
    ) {
        level.setBlock(origin.offset(x, y, z), state, 3);
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

            Arrays.sort(heights);
            return new HouseSiteProfile(
                    heights[heights.length / 2],
                    waterSamples >= 4
            );
        }
    }
}
