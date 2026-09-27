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

/**
 * Authored domestic shell for The Oldest House.
 *
 * This builder intentionally does not begin from a rectangular two-story shell.
 * The visible building is a union of distinct wings, jetties and a stair tower.
 * The only rigid organizing line is the front-door -> impossible-door axis.
 */
public final class HouseBuilder {
    public static final int WIDTH = 19;
    public static final int DEPTH = 23;
    public static final int HEIGHT = 25;

    public static final int DOMESTIC_MIN_X = -2;
    public static final int DOMESTIC_MAX_X = 20;
    public static final int DOMESTIC_MIN_Z = -2;
    public static final int DOMESTIC_MAX_Z = 24;

    public static final int BASEMENT_FLOOR_Y = -5;
    public static final int SECOND_FLOOR_Y = 6;
    public static final int UPPER_WALL_TOP_Y = 11;
    public static final int HALL_CENTER_X = WIDTH / 2;
    public static final int IMPOSSIBLE_DOOR_Z = 18;

    private static final int ROOF_BASE_Y = 12;
    private static final int REAR_WALL_Z = DEPTH - 1;

    private static final ResourceKey<LootTable> BEDROOM_LOOT = lootTable("chests/bedroom");
    private static final ResourceKey<LootTable> STUDY_LOOT = lootTable("chests/study");
    private static final ResourceKey<LootTable> BASEMENT_LOOT = lootTable("chests/basement");

    private HouseBuilder() {
    }

    public static boolean canBuildAt(ServerLevel level, BlockPos origin) {
        for (int x = DOMESTIC_MIN_X; x <= DOMESTIC_MAX_X; x++) {
            for (int z = DOMESTIC_MIN_Z; z <= DOMESTIC_MAX_Z; z++) {
                // Do not reject terrain beneath the adaptive approach/overhang envelope.
                if (z < 0 && (x < 6 || x > 12)) {
                    continue;
                }
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

    public static int siteScore(ServerLevel level, BlockPos origin) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int water = 0;
        int samples = 0;

        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                if (!isGroundFootprint(x, z)) {
                    continue;
                }
                samples++;
                int h = surfaceHeight(level, origin.getX() + x, origin.getZ() + z);
                minY = Math.min(minY, h);
                maxY = Math.max(maxY, h);
                if (surfaceIsWater(level, origin.getX() + x, origin.getZ() + z, h)) {
                    water++;
                }
            }
        }

        if (samples == 0 || water > Math.max(5, samples / 30)) {
            return Integer.MAX_VALUE;
        }

        HouseSiteProfile site = HouseSiteProfile.capture(level, origin);
        return (maxY - minY) * 35
                + water * 90
                + Math.max(0, origin.getY() - site.frontGroundY()) * 4
                + (site.frontWater() ? 10 : 0);
    }

    public static void build(ServerLevel level, BlockPos origin) {
        HouseSiteProfile site = HouseSiteProfile.capture(level, origin);
        HouseStyle style = HouseStyle.capture(level, origin);

        clearBuildEnvelope(level, origin);
        buildBasement(level, origin);
        buildGroundFloor(level, origin, style);
        buildUpperFloor(level, origin, style);
        buildInteriorPlan(level, origin);
        buildExteriorFraming(level, origin, style);
        buildRoofSystem(level, origin, style);
        buildChimneyAndHearth(level, origin, style);
        buildStairTower(level, origin);
        buildEntry(level, origin, site);

        furnishLivingRoom(level, origin);
        furnishKitchen(level, origin);
        furnishStudy(level, origin);
        furnishServiceRoom(level, origin);
        furnishUpperFloor(level, origin);
        furnishBasement(level, origin);
        installLighting(level, origin);

        // Destructive clearance happens before authored openings are restored.
        enforceCirculation(level, origin);
        installDoorsAndWindows(level, origin);

        applyDomesticLootTables(level, origin);
        spawnDomesticPaintings(level, origin);
    }

    /** Used by dimension transition code so the irregular plan, not a box, is authoritative. */
    public static boolean isInsideDomesticFootprint(double relX, double relY, double relZ) {
        if (relY < BASEMENT_FLOOR_Y + 0.2D || relY > UPPER_WALL_TOP_Y + 1.0D) {
            return false;
        }

        int x = (int) Math.floor(relX);
        int z = (int) Math.floor(relZ);

        if (relY < 0.2D) {
            return x >= 1 && x <= 17 && z >= 1 && z <= 20;
        }
        if (relY < SECOND_FLOOR_Y + 0.25D) {
            return isGroundFootprint(x, z);
        }
        return isUpperFootprint(x, z);
    }

    private static boolean isGroundFootprint(int x, int z) {
        return inRect(x, z, 0, 7, 0, 10)
                || inRect(x, z, 7, 11, 0, 18)
                || inRect(x, z, 11, 18, 2, 11)
                || inRect(x, z, 2, 8, 10, 22)
                || inRect(x, z, 8, 11, 10, 18)
                || inRect(x, z, 12, 18, 10, 18);
    }

    private static boolean isUpperFootprint(int x, int z) {
        return inRect(x, z, -1, 8, -1, 10)
                || inRect(x, z, 7, 11, 0, 18)
                || inRect(x, z, 10, 19, 1, 11)
                || inRect(x, z, 1, 9, 10, 22)
                || inRect(x, z, 12, 19, 10, 19);
    }

    private static boolean inRect(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    private static boolean groundBoundary(int x, int z) {
        return isGroundFootprint(x, z)
                && (!isGroundFootprint(x - 1, z)
                || !isGroundFootprint(x + 1, z)
                || !isGroundFootprint(x, z - 1)
                || !isGroundFootprint(x, z + 1));
    }

    private static boolean upperBoundary(int x, int z) {
        return isUpperFootprint(x, z)
                && (!isUpperFootprint(x - 1, z)
                || !isUpperFootprint(x + 1, z)
                || !isUpperFootprint(x, z - 1)
                || !isUpperFootprint(x, z + 1));
    }

    private static void clearBuildEnvelope(ServerLevel level, BlockPos origin) {
        for (int x = DOMESTIC_MIN_X; x <= DOMESTIC_MAX_X; x++) {
            for (int z = DOMESTIC_MIN_Z; z <= DOMESTIC_MAX_Z; z++) {
                for (int y = 0; y <= HEIGHT; y++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildBasement(ServerLevel level, BlockPos origin) {
        for (int x = 1; x <= 17; x++) {
            for (int z = 1; z <= 20; z++) {
                set(level, origin, x, BASEMENT_FLOOR_Y, z, basementStone(x, z));
                for (int y = BASEMENT_FLOOR_Y + 1; y <= -1; y++) {
                    boolean edge = x == 1 || x == 17 || z == 1 || z == 20;
                    set(level, origin, x, y, z,
                            edge ? basementStone(x + y, z) : Blocks.AIR.defaultBlockState());
                }
            }
        }

        for (int[] pier : new int[][]{{5, 6}, {13, 6}, {5, 15}, {13, 15}}) {
            for (int y = BASEMENT_FLOOR_Y + 1; y <= -1; y++) {
                set(level, origin, pier[0], y, pier[1], basementStone(pier[0], pier[1] + y));
            }
        }
    }

    private static BlockState basementStone(int x, int z) {
        int v = Math.floorMod(x * 31 + z * 17, 11);
        if (v == 0) {
            return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        }
        if (v <= 2) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        }
        return Blocks.STONE_BRICKS.defaultBlockState();
    }

    private static void buildGroundFloor(ServerLevel level, BlockPos origin, HouseStyle style) {
        for (int x = -1; x <= 19; x++) {
            for (int z = 0; z <= 22; z++) {
                if (!isGroundFootprint(x, z)) {
                    continue;
                }

                set(level, origin, x, 0, z,
                        groundBoundary(x, z)
                                ? style.foundation()
                                : Blocks.OAK_PLANKS.defaultBlockState());

                if (!groundBoundary(x, z)) {
                    continue;
                }

                set(level, origin, x, 1, z, style.foundation());
                for (int y = 2; y <= 5; y++) {
                    set(level, origin, x, y, z, style.plaster());
                }
            }
        }

        for (int z = 1; z < IMPOSSIBLE_DOOR_Z; z++) {
            for (int x = HALL_CENTER_X - 1; x <= HALL_CENTER_X + 1; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
    }

    private static void buildUpperFloor(ServerLevel level, BlockPos origin, HouseStyle style) {
        for (int x = -1; x <= 19; x++) {
            for (int z = -1; z <= 22; z++) {
                if (!isUpperFootprint(x, z)) {
                    continue;
                }

                set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.OAK_PLANKS.defaultBlockState());

                if (!upperBoundary(x, z)) {
                    continue;
                }

                for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                    set(level, origin, x, y, z, style.plaster());
                }
            }
        }

        for (int z = 1; z < IMPOSSIBLE_DOOR_Z; z++) {
            for (int x = HALL_CENTER_X - 1; x <= HALL_CENTER_X + 1; x++) {
                if (isUpperFootprint(x, z)) {
                    set(level, origin, x, SECOND_FLOOR_Y, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                }
            }
        }
    }

    private static void buildInteriorPlan(ServerLevel level, BlockPos origin) {
        for (int z = 1; z < IMPOSSIBLE_DOOR_Z; z++) {
            for (int y = 1; y <= 5; y++) {
                boolean leftArch = z >= 3 && z <= 7 && y <= 3;
                boolean rightArch = z >= 4 && z <= 8 && y <= 3;
                boolean leftRearDoor = z == 14 && y <= 2;
                boolean rightRearOpening = z >= 13 && z <= 15 && y <= 3;

                if (!leftArch && !leftRearDoor) {
                    set(level, origin, 7, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
                if (!rightArch && !rightRearOpening) {
                    set(level, origin, 11, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        wallLineX(level, origin, 0, 7, 10, 1, 5, Blocks.WHITE_TERRACOTTA.defaultBlockState());
        wallLineX(level, origin, 11, 18, 10, 1, 5, Blocks.WHITE_TERRACOTTA.defaultBlockState());

        for (int x = HALL_CENTER_X - 2; x <= HALL_CENTER_X + 2; x++) {
            for (int y = 1; y <= 5; y++) {
                set(level, origin, x, y, IMPOSSIBLE_DOOR_Z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        for (int z = 1; z <= 17; z++) {
            for (int y = 7; y <= UPPER_WALL_TOP_Y; y++) {
                boolean leftFrontDoor = z == 7 && y <= 8;
                boolean rightFrontDoor = z == 7 && y <= 8;
                boolean leftRearDoor = z == 14 && y <= 8;
                boolean stairOpening = z >= 13 && z <= 15 && y <= 9;

                if (!leftFrontDoor && !leftRearDoor) {
                    set(level, origin, 7, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
                if (!rightFrontDoor && !stairOpening) {
                    set(level, origin, 11, y, z, Blocks.WHITE_TERRACOTTA.defaultBlockState());
                }
            }
        }

        wallLineX(level, origin, -1, 7, 10, 7, UPPER_WALL_TOP_Y,
                Blocks.WHITE_TERRACOTTA.defaultBlockState());
        wallLineX(level, origin, 11, 19, 10, 7, UPPER_WALL_TOP_Y,
                Blocks.WHITE_TERRACOTTA.defaultBlockState());

        clear(level, origin, 5, 7, 10, 3, 2, 1);
        clear(level, origin, 13, 7, 10, 3, 2, 1);
    }

    private static void buildExteriorFraming(ServerLevel level, BlockPos origin, HouseStyle style) {
        int[][] groundPosts = {
                {0, 0}, {7, 0}, {0, 10},
                {11, 2}, {18, 2}, {18, 11},
                {2, 22}, {8, 22}, {2, 10},
                {12, 10}, {18, 10}, {18, 18}, {12, 18},
                {7, 18}, {11, 18}
        };
        for (int[] p : groundPosts) {
            framePost(level, origin, p[0], p[1], 1, 5, style.timber());
        }

        frameBeamX(level, origin, -1, 8, -1, 6, style.timber());
        frameBeamZ(level, origin, -1, -1, 10, 6, style.timber());
        frameBeamZ(level, origin, 8, -1, 10, 6, style.timber());

        frameBeamX(level, origin, 10, 19, 1, 6, style.timber());
        frameBeamZ(level, origin, 10, 1, 11, 6, style.timber());
        frameBeamZ(level, origin, 19, 1, 11, 6, style.timber());

        int[][] upperPosts = {
                {-1, -1}, {8, -1}, {-1, 10},
                {10, 1}, {19, 1}, {19, 11},
                {1, 22}, {9, 22}, {1, 10},
                {12, 10}, {19, 10}, {19, 19}, {12, 19}
        };
        for (int[] p : upperPosts) {
            framePost(level, origin, p[0], p[1], 7, UPPER_WALL_TOP_Y, style.timber());
        }

        frameBeamX(level, origin, -1, 8, -1, 10, style.timber());
        frameBeamX(level, origin, 10, 19, 1, 10, style.timber());
        frameBeamX(level, origin, 1, 9, 22, 10, style.timber());
        frameBeamZ(level, origin, 19, 10, 19, 10, style.timber());

        framePost(level, origin, 7, 3, 1, 4, style.timber());
        framePost(level, origin, 7, 8, 1, 4, style.timber());
        frameBeamZ(level, origin, 7, 3, 8, 4, style.timber());

        framePost(level, origin, 11, 4, 1, 4, style.timber());
        framePost(level, origin, 11, 9, 1, 4, style.timber());
        frameBeamZ(level, origin, 11, 4, 9, 4, style.timber());

        for (int z : new int[]{2, 6, 9}) {
            frameBeamX(level, origin, 1, 6, z, 5, style.timber());
        }
        for (int z : new int[]{4, 8}) {
            frameBeamX(level, origin, 12, 17, z, 5, style.timber());
        }
    }

    private static void buildRoofSystem(ServerLevel level, BlockPos origin, HouseStyle style) {
        HouseStyle.RoofProfile roof = style.roof();

        buildFlaredGableZ(level, origin, -2, 9, -2, 12,
                ROOF_BASE_Y, roof.leftRise(), style.roofBlock(), style.roofStair(), style.timber());
        fillGableFace(level, origin, -1, 8, -1, ROOF_BASE_Y,
                roof.leftRise(), HALL_CENTER_X - 6, style);

        int rightBase = ROOF_BASE_Y + roof.rightLift();
        buildFlaredGableZ(level, origin, 9, 20, 0, 13,
                rightBase, roof.rightRise(), style.roofBlock(), style.roofStair(), style.timber());
        fillGableFace(level, origin, 10, 19, 1, rightBase,
                roof.rightRise(), HALL_CENTER_X + 5, style);

        buildFlaredGableX(level, origin, 0, 11, 9, 24,
                ROOF_BASE_Y, roof.rearRise(), style.roofBlock(), style.roofStair(), style.timber());

        buildHipRoof(level, origin, 11, 20, 9, 20,
                ROOF_BASE_Y + 1, roof.towerRise(), style);

        int dormerX = roof.dormerLeft() ? 3 : 7;
        buildDormer(level, origin, dormerX, 17, ROOF_BASE_Y + 3, style);
    }

    private static void buildFlaredGableZ(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int baseY,
            int rise,
            BlockState roofBlock,
            BlockState roofStair,
            BlockState trim
    ) {
        for (int layer = 0; layer < rise; layer++) {
            int y = baseY + layer;
            int inset = Math.max(0, layer - 1);
            int left = minX + inset;
            int right = maxX - inset;

            for (int z = minZ; z <= maxZ; z++) {
                BlockState leftState = roofStair.setValue(StairBlock.FACING, Direction.WEST);
                BlockState rightState = roofStair.setValue(StairBlock.FACING, Direction.EAST);
                set(level, origin, left, y, z, leftState);
                set(level, origin, right, y, z, rightState);

                if (layer == 0 && z % 3 == 0) {
                    set(level, origin, left - 1, y, z, roofBlock);
                    set(level, origin, right + 1, y, z, roofBlock);
                }
            }

            set(level, origin, left, y, minZ - 1, trim);
            set(level, origin, right, y, minZ - 1, trim);
            set(level, origin, left, y, maxZ + 1, trim);
            set(level, origin, right, y, maxZ + 1, trim);
        }

        int topInset = Math.max(0, rise - 2);
        int ridgeY = baseY + rise;
        for (int x = minX + topInset; x <= maxX - topInset; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                set(level, origin, x, ridgeY, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
            }
        }
    }

    private static void buildFlaredGableX(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int baseY,
            int rise,
            BlockState roofBlock,
            BlockState roofStair,
            BlockState trim
    ) {
        for (int layer = 0; layer < rise; layer++) {
            int y = baseY + layer;
            int inset = Math.max(0, layer - 1);
            int front = minZ + inset;
            int rear = maxZ - inset;

            for (int x = minX; x <= maxX; x++) {
                set(level, origin, x, y, front,
                        roofStair.setValue(StairBlock.FACING, Direction.NORTH));
                set(level, origin, x, y, rear,
                        roofStair.setValue(StairBlock.FACING, Direction.SOUTH));
                if (layer == 0 && x % 3 == 0) {
                    set(level, origin, x, y, front - 1, roofBlock);
                    set(level, origin, x, y, rear + 1, roofBlock);
                }
            }

            set(level, origin, minX - 1, y, front, trim);
            set(level, origin, minX - 1, y, rear, trim);
            set(level, origin, maxX + 1, y, front, trim);
            set(level, origin, maxX + 1, y, rear, trim);
        }

        int topInset = Math.max(0, rise - 2);
        int ridgeY = baseY + rise;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ + topInset; z <= maxZ - topInset; z++) {
                set(level, origin, x, ridgeY, z, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
            }
        }
    }

    private static void buildHipRoof(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            int baseY,
            int rise,
            HouseStyle style
    ) {
        for (int layer = 0; layer < rise; layer++) {
            int y = baseY + layer;
            int a = minX + layer;
            int b = maxX - layer;
            int c = minZ + layer;
            int d = maxZ - layer;
            if (a > b || c > d) {
                break;
            }

            for (int x = a; x <= b; x++) {
                set(level, origin, x, y, c, style.roofStair().setValue(StairBlock.FACING, Direction.NORTH));
                set(level, origin, x, y, d, style.roofStair().setValue(StairBlock.FACING, Direction.SOUTH));
            }
            for (int z = c + 1; z < d; z++) {
                set(level, origin, a, y, z, style.roofStair().setValue(StairBlock.FACING, Direction.WEST));
                set(level, origin, b, y, z, style.roofStair().setValue(StairBlock.FACING, Direction.EAST));
            }
        }

        set(level, origin, (minX + maxX) / 2, baseY + rise, (minZ + maxZ) / 2,
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
    }

    private static void fillGableFace(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int z,
            int baseY,
            int rise,
            int centerX,
            HouseStyle style
    ) {
        for (int layer = 0; layer < rise; layer++) {
            int inset = Math.max(0, layer - 1);
            int left = minX + inset;
            int right = maxX - inset;
            if (left > right) {
                break;
            }
            for (int x = left; x <= right; x++) {
                set(level, origin, x, baseY + layer, z, style.plaster());
            }
            set(level, origin, left, baseY + layer, z, style.timber());
            set(level, origin, right, baseY + layer, z, style.timber());
        }

        for (int y = baseY; y < baseY + rise; y++) {
            set(level, origin, centerX, y, z, style.timber());
        }

        if (rise >= 4) {
            set(level, origin, centerX, baseY + 1, z, Blocks.GLASS_PANE.defaultBlockState());
            set(level, origin, centerX, baseY + 2, z, Blocks.GLASS_PANE.defaultBlockState());
        }
    }

    private static void buildDormer(
            ServerLevel level,
            BlockPos origin,
            int x,
            int z,
            int baseY,
            HouseStyle style
    ) {
        for (int dz = -2; dz <= 1; dz++) {
            set(level, origin, x - 2, baseY, z + dz,
                    style.roofStair().setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, x + 2, baseY, z + dz,
                    style.roofStair().setValue(StairBlock.FACING, Direction.EAST));
            set(level, origin, x - 1, baseY + 1, z + dz,
                    style.roofStair().setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, x + 1, baseY + 1, z + dz,
                    style.roofStair().setValue(StairBlock.FACING, Direction.EAST));
            set(level, origin, x, baseY + 2, z + dz, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        }
        set(level, origin, x, baseY, z - 1, Blocks.GLASS_PANE.defaultBlockState());
    }

    private static void buildChimneyAndHearth(ServerLevel level, BlockPos origin, HouseStyle style) {
        for (int y = 1; y <= 20; y++) {
            for (int x = 0; x <= 1; x++) {
                for (int z = 5; z <= 6; z++) {
                    set(level, origin, x, y, z,
                            Math.floorMod(x + z + y, 7) == 0
                                    ? Blocks.MUD_BRICKS.defaultBlockState()
                                    : Blocks.BRICKS.defaultBlockState());
                }
            }
        }

        set(level, origin, 2, 1, 5, Blocks.BRICKS.defaultBlockState());
        set(level, origin, 2, 1, 6, Blocks.CAMPFIRE.defaultBlockState());
        set(level, origin, 2, 1, 7, Blocks.BRICKS.defaultBlockState());
        for (int y = 2; y <= 3; y++) {
            set(level, origin, 2, y, 5, Blocks.BRICKS.defaultBlockState());
            set(level, origin, 2, y, 7, Blocks.BRICKS.defaultBlockState());
        }
        for (int z = 5; z <= 7; z++) {
            set(level, origin, 2, 4, z, style.timber());
            set(level, origin, 3, 1, z, Blocks.STONE_BRICK_SLAB.defaultBlockState());
        }

        for (int x = -1; x <= 2; x++) {
            for (int z = 4; z <= 7; z++) {
                set(level, origin, x, 20, z, Blocks.BRICK_SLAB.defaultBlockState());
            }
        }
    }

    private static void buildStairTower(ServerLevel level, BlockPos origin) {
        clear(level, origin, 12, SECOND_FLOOR_Y, 12, 6, 1, 6);

        for (int step = 0; step < 4; step++) {
            int z = 17 - step;
            int y = 1 + step;
            for (int x = 15; x <= 16; x++) {
                set(level, origin, x, y, z,
                        Blocks.SPRUCE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.NORTH));
            }
        }

        for (int x = 13; x <= 16; x++) {
            for (int z = 12; z <= 13; z++) {
                set(level, origin, x, 4, z, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }

        for (int z = 12; z <= 13; z++) {
            set(level, origin, 15, 4, z,
                    Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
            set(level, origin, 14, 5, z,
                    Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
        }

        for (int z = 12; z <= 17; z++) {
            set(level, origin, 17, 7, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        for (int x = 13; x <= 16; x++) {
            set(level, origin, x, 7, 17, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }

        for (int step = 0; step < 4; step++) {
            set(level, origin, 13, -1 - step, 16 + step,
                    Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.SOUTH));
        }
    }

    private static void buildEntry(ServerLevel level, BlockPos origin, HouseSiteProfile site) {
        for (int x = 8; x <= 10; x++) {
            for (int z = -2; z <= -1; z++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        for (int x : new int[]{8, 10}) {
            for (int y = 1; y <= 4; y++) {
                set(level, origin, x, y, -2, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            }
            extendSupportToTerrain(level, origin, x, -2, -1,
                    site.frontWater()
                            ? Blocks.STONE_BRICKS.defaultBlockState()
                            : Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        frameBeamX(level, origin, 8, 10, -2, 4, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        for (int x = 7; x <= 11; x++) {
            set(level, origin, x, 5, -2,
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }
        placeHangingLantern(level, origin, HALL_CENTER_X, 3, -2);

        if (site.frontWater()) {
            buildWaterApproach(level, origin, site.frontGroundY());
        } else {
            buildGroundApproach(level, origin, site.frontGroundY());
        }
    }

    private static void buildGroundApproach(ServerLevel level, BlockPos origin, int absoluteGroundY) {
        int drop = Math.max(0, origin.getY() - absoluteGroundY);
        int courses = Math.max(1, Math.min(6, drop + 1));

        for (int step = 0; step < courses; step++) {
            int z = -3 - step;
            int y = -step;
            for (int x = HALL_CENTER_X - 1; x <= HALL_CENTER_X + 1; x++) {
                set(level, origin, x, y, z,
                        Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.SOUTH));
                extendSupportToTerrain(level, origin, x, z, y - 1,
                        Blocks.STONE_BRICKS.defaultBlockState());
            }
        }
    }

    private static void buildWaterApproach(ServerLevel level, BlockPos origin, int absoluteWaterSurfaceY) {
        for (int z = -3; z >= -7; z--) {
            for (int x = HALL_CENTER_X - 1; x <= HALL_CENTER_X + 1; x++) {
                set(level, origin, x, 0, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }

        for (int x : new int[]{HALL_CENTER_X - 1, HALL_CENTER_X + 1}) {
            extendSupportToTerrain(level, origin, x, -7, -1,
                    Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
            for (int z = -3; z >= -7; z--) {
                if (!(x == HALL_CENTER_X + 1 && z == -6)) {
                    set(level, origin, x, 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
                }
            }
        }
        set(level, origin, HALL_CENTER_X, 1, -7, Blocks.SPRUCE_FENCE.defaultBlockState());

        for (int y = 1; y <= 4; y++) {
            set(level, origin, HALL_CENTER_X - 1, y, -7,
                    Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        set(level, origin, HALL_CENTER_X, 4, -7, Blocks.DARK_OAK_FENCE.defaultBlockState());
        set(level, origin, HALL_CENTER_X, 3, -7,
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));

        int waterRelativeY = absoluteWaterSurfaceY - origin.getY();
        int ladderBottomY = Math.max(-6, Math.min(-1, waterRelativeY - 2));
        extendSupportToTerrain(level, origin, HALL_CENTER_X + 1, -6, -1,
                Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        for (int y = 0; y >= ladderBottomY; y--) {
            set(level, origin, HALL_CENTER_X + 2, y, -6,
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
        }
    }

    private static void furnishLivingRoom(ServerLevel level, BlockPos origin) {
        carpet(level, origin, Blocks.BROWN_CARPET.defaultBlockState(), 1,
                new int[][]{
                        {3, 3}, {4, 3}, {5, 3}, {6, 3},
                        {3, 4}, {4, 4}, {5, 4}, {6, 4},
                        {3, 5}, {4, 5}, {5, 5}, {6, 5},
                        {3, 6}, {4, 6}, {5, 6}, {6, 6},
                        {3, 7}, {4, 7}, {5, 7}, {6, 7}
                });
        carpet(level, origin, Blocks.LIGHT_GRAY_CARPET.defaultBlockState(), 1,
                new int[][]{{4, 4}, {5, 4}, {4, 6}, {5, 6}});

        for (int y = 1; y <= 3; y++) {
            set(level, origin, 1, y, 3, Blocks.BOOKSHELF.defaultBlockState());
            set(level, origin, 1, y, 9, Blocks.BOOKSHELF.defaultBlockState());
        }
        set(level, origin, 2, 1, 3, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 2, 1, 9, Blocks.CHISELED_BOOKSHELF.defaultBlockState());

        for (int z = 5; z <= 7; z++) {
            set(level, origin, 6, 1, z,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.EAST));
        }
        set(level, origin, 5, 1, 4,
                Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
        set(level, origin, 5, 1, 8,
                Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));

        set(level, origin, 4, 1, 6, Blocks.OAK_FENCE.defaultBlockState());
        set(level, origin, 4, 2, 6, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());

        for (int x = 2; x <= 5; x++) {
            set(level, origin, x, 1, 1,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH));
        }
        set(level, origin, 6, 1, 2, Blocks.BARREL.defaultBlockState());
        set(level, origin, 6, 2, 2, Blocks.LANTERN.defaultBlockState());
    }

    private static void furnishKitchen(ServerLevel level, BlockPos origin) {
        for (int z = 4; z <= 9; z++) {
            BlockState state = switch (z) {
                case 5 -> Blocks.CRAFTING_TABLE.defaultBlockState();
                case 6 -> Blocks.FURNACE.defaultBlockState();
                case 7 -> Blocks.SMOKER.defaultBlockState();
                default -> Blocks.BARREL.defaultBlockState();
            };
            set(level, origin, 17, 1, z, state);
        }
        set(level, origin, 17, 1, 3, Blocks.CAULDRON.defaultBlockState());

        for (int z = 5; z <= 6; z++) {
            set(level, origin, 14, 1, z, Blocks.BARREL.defaultBlockState());
            set(level, origin, 14, 2, z, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        }

        for (int x = 13; x <= 16; x++) {
            set(level, origin, x, 1, 9, Blocks.OAK_FENCE.defaultBlockState());
            set(level, origin, x, 2, 9, Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
        }
        set(level, origin, 14, 1, 8,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
        set(level, origin, 15, 1, 10,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
    }

    private static void furnishStudy(ServerLevel level, BlockPos origin) {
        for (int z = 12; z <= 18; z++) {
            set(level, origin, 3, 1, z, Blocks.BOOKSHELF.defaultBlockState());
        }
        set(level, origin, 3, 2, 14, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 2, 17, Blocks.CHISELED_BOOKSHELF.defaultBlockState());

        set(level, origin, 5, 1, 19, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        set(level, origin, 6, 1, 19, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 7, 1, 20, Blocks.CHEST.defaultBlockState());
        set(level, origin, 6, 1, 20, Blocks.BARREL.defaultBlockState());

        carpet(level, origin, Blocks.RED_CARPET.defaultBlockState(), 1,
                new int[][]{{4, 14}, {5, 14}, {6, 14}, {4, 15}, {5, 15}, {6, 15}});
    }

    private static void furnishServiceRoom(ServerLevel level, BlockPos origin) {
        set(level, origin, 13, 1, 11, Blocks.CAULDRON.defaultBlockState());
        set(level, origin, 14, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 15, 1, 11, Blocks.BARREL.defaultBlockState());
        set(level, origin, 17, 1, 12, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 17, 1, 13, Blocks.BARREL.defaultBlockState());
    }

    private static void furnishUpperFloor(ServerLevel level, BlockPos origin) {
        placeBed(level, origin, 2, 7, 6, Direction.SOUTH);
        set(level, origin, 2, 7, 1, Blocks.CHEST.defaultBlockState());
        set(level, origin, 5, 7, 2, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 5, 7, 7, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 1, 7, 7, Blocks.BARREL.defaultBlockState());
        set(level, origin, 1, 8, 7, Blocks.CANDLE.defaultBlockState());
        carpet(level, origin, Blocks.BLUE_CARPET.defaultBlockState(), 7,
                new int[][]{{2, 3}, {3, 3}, {4, 3}, {5, 3}, {3, 4}, {4, 4}, {3, 5}, {4, 5}});

        placeBed(level, origin, 16, 7, 7, Direction.SOUTH);
        set(level, origin, 16, 7, 3, Blocks.CHEST.defaultBlockState());
        set(level, origin, 13, 7, 3, Blocks.LOOM.defaultBlockState());
        set(level, origin, 15, 7, 5, Blocks.JUKEBOX.defaultBlockState());
        set(level, origin, 13, 7, 7, Blocks.NOTE_BLOCK.defaultBlockState());
        set(level, origin, 18, 7, 7, Blocks.OAK_PLANKS.defaultBlockState());
        set(level, origin, 18, 8, 7, Blocks.LANTERN.defaultBlockState());
        carpet(level, origin, Blocks.GREEN_CARPET.defaultBlockState(), 7,
                new int[][]{{13, 4}, {14, 4}, {15, 4}, {16, 4}, {14, 5}, {15, 5}});

        placeBed(level, origin, 4, 7, 17, Direction.SOUTH);
        set(level, origin, 3, 7, 20, Blocks.CHEST.defaultBlockState());
        set(level, origin, 2, 7, 12, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 3, 7, 12, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 7, 7, 19, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        set(level, origin, 7, 7, 20, Blocks.LECTERN.defaultBlockState());
        set(level, origin, 2, 7, 18, Blocks.BARREL.defaultBlockState());
        set(level, origin, 2, 8, 18, Blocks.CANDLE.defaultBlockState());
        carpet(level, origin, Blocks.GRAY_CARPET.defaultBlockState(), 7,
                new int[][]{{3, 14}, {4, 14}, {5, 14}, {6, 14}, {3, 15}, {4, 15}, {5, 15}, {6, 15}});

        set(level, origin, 13, 7, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 14, 7, 11, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        set(level, origin, 15, 7, 11, Blocks.BOOKSHELF.defaultBlockState());
        set(level, origin, 14, 7, 12,
                Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));

        for (int z = 1; z <= 16; z++) {
            setQuiet(level, origin, HALL_CENTER_X, 7, z, Blocks.RED_CARPET.defaultBlockState());
        }
    }

    private static void furnishBasement(ServerLevel level, BlockPos origin) {
        set(level, origin, 3, -4, 4, Blocks.CHEST.defaultBlockState());
        set(level, origin, 15, -4, 4, Blocks.CHEST.defaultBlockState());
        set(level, origin, 3, -4, 15, Blocks.BARREL.defaultBlockState());
        set(level, origin, 4, -4, 15, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(level, origin, 5, -4, 15, Blocks.FURNACE.defaultBlockState());
        set(level, origin, 6, -4, 15, Blocks.BLAST_FURNACE.defaultBlockState());
        set(level, origin, 15, -4, 15, Blocks.CAULDRON.defaultBlockState());
        set(level, origin, 16, -4, 15, Blocks.COAL_BLOCK.defaultBlockState());
    }

    private static void installDoorsAndWindows(ServerLevel level, BlockPos origin) {
        placeDoor(level, origin, HALL_CENTER_X, 1, 0,
                Direction.NORTH, Blocks.OAK_DOOR.defaultBlockState());

        placeDoor(level, origin, 7, 1, 14,
                Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 7, 7, 7,
                Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 11, 7, 7,
                Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());
        placeDoor(level, origin, 7, 7, 14,
                Direction.EAST, Blocks.SPRUCE_DOOR.defaultBlockState());

        window(level, origin, 2, 0, 2, 3);
        window(level, origin, 4, 0, 2, 3);
        window(level, origin, 6, 0, 2, 3);
        window(level, origin, 0, 3, 2, 3);
        window(level, origin, 0, 8, 2, 3);

        window(level, origin, 13, 2, 2, 3);
        window(level, origin, 16, 2, 2, 3);
        window(level, origin, 18, 5, 2, 3);
        window(level, origin, 18, 9, 2, 3);

        window(level, origin, 2, 14, 2, 3);
        window(level, origin, 2, 19, 2, 3);
        window(level, origin, 4, 22, 2, 3);
        window(level, origin, 7, 22, 2, 3);

        window(level, origin, 18, 13, 2, 3);
        window(level, origin, 18, 16, 4, 5);
        window(level, origin, 19, 13, 8, 9);
        window(level, origin, 19, 17, 8, 9);

        for (int x : new int[]{2, 4, 6}) {
            window(level, origin, x, -1, 8, 9);
        }
        for (int x : new int[]{13, 16}) {
            window(level, origin, x, 1, 8, 9);
        }
        window(level, origin, 1, 4, 8, 9);
        window(level, origin, 18, 5, 8, 9);
        window(level, origin, 3, 22, 8, 9);
        window(level, origin, 6, 22, 8, 9);
    }

    public static void revealImpossibleDoor(ServerLevel level, BlockPos origin) {
        for (int x = HALL_CENTER_X - 2; x <= HALL_CENTER_X + 2; x++) {
            for (int y = 1; y <= 5; y++) {
                setQuiet(level, origin, x, y, IMPOSSIBLE_DOOR_Z,
                        Blocks.WHITE_TERRACOTTA.defaultBlockState());
            }
        }

        placeDoor(level, origin, HALL_CENTER_X, 1, IMPOSSIBLE_DOOR_Z,
                Direction.NORTH, Blocks.SPRUCE_DOOR.defaultBlockState());
    }

    private static void enforceCirculation(ServerLevel level, BlockPos origin) {
        clearCollisionVolume(
                level,
                origin,
                HALL_CENTER_X - 1,
                HALL_CENTER_X + 1,
                1,
                3,
                1,
                IMPOSSIBLE_DOOR_Z - 1
        );

        clearCollisionVolume(level, origin, 6, 8, 1, 3, 13, 15);

        clearCollisionVolume(level, origin, 6, 8, 7, 9, 6, 8);
        clearCollisionVolume(level, origin, 10, 12, 7, 9, 6, 8);
        clearCollisionVolume(level, origin, 6, 8, 7, 9, 13, 15);
        clearCollisionVolume(level, origin, 10, 13, 7, 9, 13, 15);
    }

    private static void installLighting(ServerLevel level, BlockPos origin) {
        placeHangingLantern(level, origin, 4, 4, 6);
        placeHangingLantern(level, origin, 15, 4, 6);
        placeHangingLantern(level, origin, 5, 4, 16);
        placeHangingLantern(level, origin, 15, 4, 14);

        placeHangingLantern(level, origin, 8, 4, 5);
        placeHangingLantern(level, origin, 10, 4, 12);

        placeHangingLantern(level, origin, 4, 10, 5);
        placeHangingLantern(level, origin, 15, 10, 6);
        placeHangingLantern(level, origin, 5, 10, 16);
        placeHangingLantern(level, origin, 15, 10, 14);

        placeHangingLantern(level, origin, 5, -2, 8);
        placeHangingLantern(level, origin, 14, -2, 8);
        placeHangingLantern(level, origin, 9, -2, 16);
    }

    public static void applyDomesticLootTables(ServerLevel level, BlockPos origin) {
        assignLoot(level, origin.offset(7, 1, 20), STUDY_LOOT, 0x51A7D11L);
        assignLoot(level, origin.offset(2, 7, 1), BEDROOM_LOOT, 0xBED001L);
        assignLoot(level, origin.offset(16, 7, 3), BEDROOM_LOOT, 0xBED002L);
        assignLoot(level, origin.offset(3, 7, 20), BEDROOM_LOOT, 0xBED003L);
        assignLoot(level, origin.offset(3, -4, 4), BASEMENT_LOOT, 0xBA5E01L);
        assignLoot(level, origin.offset(15, -4, 4), BASEMENT_LOOT, 0xBA5E02L);
    }

    public static void spawnDomesticPaintings(ServerLevel level, BlockPos origin) {
        spawnPainting(level, origin.offset(6, 2, 9), Direction.NORTH, PaintingVariants.MATCH);
        spawnPainting(level, origin.offset(6, 2, 21), Direction.NORTH, PaintingVariants.PLANT);
        spawnPainting(level, origin.offset(1, 8, 6), Direction.EAST, PaintingVariants.BUST);
        spawnPainting(level, origin.offset(18, 8, 7), Direction.WEST, PaintingVariants.MATCH);
        spawnPainting(level, origin.offset(6, 8, 21), Direction.NORTH, PaintingVariants.BAROQUE);
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

    private static void framePost(
            ServerLevel level,
            BlockPos origin,
            int x,
            int z,
            int minY,
            int maxY,
            BlockState state
    ) {
        for (int y = minY; y <= maxY; y++) {
            set(level, origin, x, y, z, state);
        }
    }

    private static void frameBeamX(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int z,
            int y,
            BlockState state
    ) {
        for (int x = minX; x <= maxX; x++) {
            set(level, origin, x, y, z, state);
        }
    }

    private static void frameBeamZ(
            ServerLevel level,
            BlockPos origin,
            int x,
            int minZ,
            int maxZ,
            int y,
            BlockState state
    ) {
        for (int z = minZ; z <= maxZ; z++) {
            set(level, origin, x, y, z, state);
        }
    }

    private static void wallLineX(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int maxX,
            int z,
            int minY,
            int maxY,
            BlockState state
    ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                set(level, origin, x, y, z, state);
            }
        }
    }

    private static void window(ServerLevel level, BlockPos origin, int x, int z, int minY, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            set(level, origin, x, y, z, Blocks.GLASS_PANE.defaultBlockState());
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

    private static void clear(
            ServerLevel level,
            BlockPos origin,
            int minX,
            int minY,
            int minZ,
            int width,
            int height,
            int depth
    ) {
        for (int x = minX; x < minX + width; x++) {
            for (int y = minY; y < minY + height; y++) {
                for (int z = minZ; z < minZ + depth; z++) {
                    set(level, origin, x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
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
                    if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
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
        for (int relY = startRelY; relY >= startRelY - 14; relY--) {
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

    private static int surfaceHeight(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    private static boolean surfaceIsWater(ServerLevel level, int x, int z, int surfaceHeight) {
        return level.getFluidState(new BlockPos(x, surfaceHeight - 1, z)).is(FluidTags.WATER);
    }

    private static void assignLoot(
            ServerLevel level,
            BlockPos pos,
            ResourceKey<LootTable> table,
            long salt
    ) {
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(table, level.getSeed() ^ pos.asLong() ^ salt);
            chest.setChanged();
        }
    }

    private static ResourceKey<LootTable> lootTable(String path) {
        return ResourceKey.create(
                Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, path)
        );
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

    private record HouseSiteProfile(int frontGroundY, boolean frontWater) {
        static HouseSiteProfile capture(ServerLevel level, BlockPos origin) {
            int[] heights = new int[15];
            int i = 0;
            int water = 0;
            for (int z = -3; z >= -7; z--) {
                for (int x = HALL_CENTER_X - 1; x <= HALL_CENTER_X + 1; x++) {
                    int wx = origin.getX() + x;
                    int wz = origin.getZ() + z;
                    int h = surfaceHeight(level, wx, wz);
                    heights[i++] = h;
                    if (surfaceIsWater(level, wx, wz, h)) {
                        water++;
                    }
                }
            }
            java.util.Arrays.sort(heights);
            return new HouseSiteProfile(heights[heights.length / 2], water >= 5);
        }
    }

    private record HouseStyle(
            BlockState plaster,
            BlockState timber,
            BlockState foundation,
            BlockState roofBlock,
            BlockState roofStair,
            RoofProfile roof
    ) {
        static HouseStyle capture(ServerLevel level, BlockPos origin) {
            long hash = level.getSeed() ^ origin.asLong() ^ 0x0D1D35A5EL;
            boolean warmer = (hash & 1L) != 0L;
            BlockState plaster = warmer
                    ? Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState()
                    : Blocks.WHITE_TERRACOTTA.defaultBlockState();
            return new HouseStyle(
                    plaster,
                    Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),
                    Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.DEEPSLATE_TILES.defaultBlockState(),
                    Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),
                    new RoofProfile(
                            6,
                            5,
                            ((hash >>> 1) & 1L) == 0L ? 0 : 1,
                            6,
                            5,
                            ((hash >>> 2) & 1L) == 0L
                    )
            );
        }

        private record RoofProfile(
                int leftRise,
                int rightRise,
                int rightLift,
                int rearRise,
                int towerRise,
                boolean dormerLeft
        ) {
        }
    }
}
