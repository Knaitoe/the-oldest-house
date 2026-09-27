package io.github.knaitoe.theoldesthouse.house;

import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.log;
import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.slab;
import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.stairs;
import static io.github.knaitoe.theoldesthouse.house.HouseCanvas.stairsTop;

import io.github.knaitoe.theoldesthouse.house.HouseLayout.Face;
import io.github.knaitoe.theoldesthouse.house.HouseLayout.Window;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Architecture of The Oldest House: masses, floors, walls, the stair tower,
 * cellar, chimneys, timber framing, glazing, porch and foundations.
 *
 * The house reads as several distinct masses that accumulated over time:
 * a dominant jettied great-room wing (front left), a recessed entrance and
 * long hall, a lower kitchen wing (front right), an older study wing under a
 * rear cross-gable (rear left), and a heavy stair tower with a service range
 * and yard (rear right).
 */
final class HouseShell {
    static final BlockState PLASTER = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    static final BlockState MOSSY_STONE = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    static final BlockState BRICK = Blocks.BRICKS.defaultBlockState();
    static final BlockState POST = log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y);
    static final BlockState OAK_FLOOR = Blocks.OAK_PLANKS.defaultBlockState();
    static final BlockState SPRUCE_FLOOR = Blocks.SPRUCE_PLANKS.defaultBlockState();
    static final BlockState CEILING = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private HouseShell() {
    }

    // ------------------------------------------------------------------
    // Site
    // ------------------------------------------------------------------

    static void clearSite(HouseCanvas c) {
        c.clearBox(
                HouseLayout.CLEAR_MIN_X, 0, HouseLayout.CLEAR_MIN_Z,
                HouseLayout.CLEAR_MAX_X, HouseLayout.MAX_Y, HouseLayout.CLEAR_MAX_Z
        );
    }

    // ------------------------------------------------------------------
    // Masses, floors and walls
    // ------------------------------------------------------------------

    static void buildStructure(HouseCanvas c) {
        buildCellar(c);

        // Ground course under every mass, porch and yard.
        c.fill(0, 0, 0, 13, 0, 15, STONE);
        c.fill(4, 0, -2, 9, 0, -1, STONE);
        c.fill(13, 0, 4, 17, 0, 26, STONE);
        c.fill(17, 0, 1, 27, 0, 12, STONE);
        c.fill(1, 0, 15, 13, 0, 26, STONE);
        c.fill(17, 0, 12, 24, 0, 20, STONE);
        c.fill(17, 0, 20, 27, 0, 25, STONE);
        c.fill(14, 0, 0, 16, 0, 3, STONE);
        c.fill(25, 0, 12, 28, 0, 20, STONE);

        // Ground floors.
        c.fill(1, 0, 1, 12, 0, 14, OAK_FLOOR);
        c.fill(5, 0, -1, 8, 0, 0, OAK_FLOOR);
        c.fill(HouseLayout.HALL_MIN_X, 0, 5, HouseLayout.HALL_MAX_X, 0, 25, SPRUCE_FLOOR);
        c.fill(18, 0, 2, 26, 0, 6, SPRUCE_FLOOR);
        c.fill(18, 0, 7, 26, 0, 11, STONE);
        c.fill(2, 0, 16, 12, 0, 25, OAK_FLOOR);
        c.fill(18, 0, 13, 23, 0, 19, SPRUCE_FLOOR);
        c.fill(18, 0, 21, 26, 0, 24, STONE);

        // Ground-storey walls (y 1..5). Shared walls are simply built twice.
        c.walls(0, 0, 13, 15, 1, 5, PLASTER);
        c.walls(13, HouseLayout.FRONT_DOOR_Z, 17, HouseLayout.THRESHOLD_Z, 1, 5, PLASTER);
        c.walls(17, 1, 27, 12, 1, 5, PLASTER);
        c.walls(1, 15, 13, 26, 1, 5, PLASTER);
        c.walls(17, 20, 27, 25, 1, 5, PLASTER);
        c.walls(17, 12, 24, 20, 1, 14, PLASTER);

        // Deep bay on the great room's front: projects two blocks beyond the
        // main wall and opens fully into the room.
        c.fill(4, 1, -2, 9, 4, -2, PLASTER);
        c.fill(4, 1, -1, 4, 5, -1, PLASTER);
        c.fill(9, 1, -1, 9, 5, -1, PLASTER);
        c.clearBox(5, 1, 0, 8, 5, 0);
        for (int x = 4; x <= 9; x++) {
            c.set(x, 5, -2, stairs(Blocks.DEEPSLATE_TILE_STAIRS, Direction.SOUTH));
        }

        // Upper floors (y 6) with their storey walls.
        c.fill(0, 6, -1, 13, 6, 15, CEILING);
        c.fill(1, 6, 0, 12, 6, 14, OAK_FLOOR);
        c.walls(0, -1, 13, 15, 7, 10, PLASTER);

        c.fill(13, 6, 0, 17, 6, HouseLayout.THRESHOLD_Z, CEILING);
        c.fill(HouseLayout.HALL_MIN_X, 6, 1, HouseLayout.HALL_MAX_X, 6, 25, SPRUCE_FLOOR);
        c.walls(13, 0, 17, HouseLayout.THRESHOLD_Z, 7, 10, PLASTER);

        c.fill(17, 6, 1, 27, 6, 12, CEILING);
        c.fill(18, 6, 2, 26, 6, 11, SPRUCE_FLOOR);
        c.walls(17, 1, 27, 12, 7, 8, PLASTER);

        c.fill(1, 6, 15, 13, 6, 26, CEILING);
        c.fill(2, 6, 16, 12, 6, 25, OAK_FLOOR);
        c.walls(1, 15, 13, 26, 7, 9, PLASTER);

        c.fill(17, 6, 20, 27, 6, 25, CEILING);
        c.fill(18, 6, 21, 26, 6, 24, SPRUCE_FLOOR);
        c.walls(17, 20, 27, 25, 7, 8, PLASTER);

        // The rear half of the upper hall rises into the cross-gable; carry
        // its west partition up to the roof so it does not open into the
        // long gallery's roof space.
        for (int z = 16; z <= 25; z++) {
            int top = HouseLayout.ROOF_CROSS.height(13, z) - 1;
            c.fill(13, 11, z, 13, top, z, PLASTER);
        }

        // Upper-storey partition between the principal and literary bedrooms.
        c.fill(1, 7, 9, 12, 10, 9, PLASTER);

        // Great-room ceiling beams: a summer beam along the room and two
        // cross beams, hanging below the plastered ceiling.
        c.fill(7, 5, 1, 7, 5, 14, log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        for (int x = 1; x <= 12; x++) {
            if (x != 7) {
                c.set(x, 5, 4, log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
                c.set(x, 5, 11, log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
            }
        }

        // Hall cross beams frame the long perspective without touching eye level.
        for (int z : new int[]{11, 17}) {
            c.fill(HouseLayout.HALL_MIN_X, 5, z, HouseLayout.HALL_MAX_X, 5, z,
                    log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        }

        // Kitchen beam divides the dining end from the working end.
        c.fill(18, 5, 6, 26, 5, 6, log(Blocks.DARK_OAK_LOG, Direction.Axis.X));

        carveOpenings(c);
        buildStairTower(c);
        buildCellarStair(c);
    }

    /** Ceilings go in after the roofs so a roof course always wins its cell. */
    static void buildCeilings(HouseCanvas c) {
        fillIfAir(c, 1, 11, 0, 12, 11, 14, CEILING);
        fillIfAir(c, HouseLayout.HALL_MIN_X, 11, 1, HouseLayout.HALL_MAX_X, 11, 15, CEILING);
        fillIfAir(c, 18, 15, 13, 23, 15, 19, CEILING);
    }

    private static void fillIfAir(HouseCanvas c, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    c.setIfAir(x, y, z, state);
                }
            }
        }
    }

    private static void buildCellar(HouseCanvas c) {
        c.fill(17, HouseLayout.BASEMENT_FLOOR_Y, 12, 27, HouseLayout.BASEMENT_FLOOR_Y, 25, STONE);
        c.walls(17, 12, 27, 25, HouseLayout.BASEMENT_FLOOR_Y + 1, -1, STONE);
        c.clearBox(18, -4, 13, 26, -1, 24);

        // Timber beams carry the tower's east wall and the tower/service wall
        // across the cellar, landing on stone piers.
        c.fill(24, -1, 13, 24, -1, 19, log(Blocks.DARK_OAK_LOG, Direction.Axis.Z));
        c.fill(18, -1, 20, 26, -1, 20, log(Blocks.DARK_OAK_LOG, Direction.Axis.X));
        c.fill(24, -4, 16, 24, -2, 16, STONE);
        c.fill(24, -4, 20, 24, -2, 20, STONE);
        c.fill(20, -4, 20, 20, -2, 20, STONE);
    }

    private static void carveOpenings(HouseCanvas c) {
        // Ground floor. Nothing ever crosses the hall axis.
        c.clearBox(13, 1, 7, 13, 3, 8);   // hall -> great room, aligned with the hearth
        c.clearBox(17, 1, 5, 17, 3, 6);   // hall -> dining end of the kitchen
        c.clearBox(17, 1, 13, 17, 3, 14); // hall -> stair tower
        c.clearBox(13, 1, 20, 13, 2, 20); // hall -> study (door)
        c.clearBox(17, 1, 23, 17, 2, 23); // hall -> scullery (door)
        c.clearBox(HouseLayout.AXIS_X, 1, HouseLayout.FRONT_DOOR_Z,
                HouseLayout.AXIS_X, 2, HouseLayout.FRONT_DOOR_Z);
        c.clearBox(26, 1, 12, 26, 2, 12);  // kitchen back door to the yard

        // Timber lintels over the wide ground-floor openings.
        c.fill(13, 4, 6, 13, 4, 9, log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        c.fill(17, 4, 4, 17, 4, 7, log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));
        c.fill(17, 4, 12, 17, 4, 15, log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Z));

        // Upper floor.
        c.clearBox(13, 7, 4, 13, 8, 4);    // principal bedroom (door)
        c.clearBox(13, 7, 12, 13, 8, 12);  // literary bedroom (door)
        c.clearBox(13, 7, 19, 13, 9, 20);  // long gallery
        c.clearBox(17, 7, 7, 17, 8, 7);    // maker loft (door)
        c.clearBox(17, 7, 13, 17, 9, 14);  // stair tower landing
        c.clearBox(17, 7, 22, 17, 8, 22);  // box room (door)
    }

    /**
     * A substantial dog-leg stair in its own tower, entered from the side of
     * the hall so nothing ever interrupts the front-door sightline.
     *
     * The six-block rise is split into two flights of three steps. Flight A
     * climbs south along the west side to a half landing across the tower;
     * flight B returns north along the east side, its top step sitting in the
     * floor course so it finishes flush with the upper landing. A panelled
     * spine separates the flights and rises into a newel and balustrade.
     */
    private static void buildStairTower(HouseCanvas c) {
        BlockState tread = SPRUCE_FLOOR;
        BlockState fence = Blocks.DARK_OAK_FENCE.defaultBlockState();

        // Flight A: y1..y3 rising south, each step on a solid string.
        for (int step = 0; step < 3; step++) {
            int z = 15 + step;
            int y = 1 + step;
            if (y > 1) {
                c.fill(18, 1, z, 19, y - 1, z, tread);
            }
            c.fill(18, y, z, 19, y, z, stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH));
        }

        // Half landing (top at y4) across the tower.
        c.fill(18, 1, 18, 23, 3, 19, tread);

        // Flight B: y4..y6 rising north on a solid base.
        for (int step = 0; step < 3; step++) {
            int z = 17 - step;
            int y = 4 + step;
            c.fill(22, 1, z, 23, y - 1, z, tread);
            c.fill(22, y, z, 23, y, z, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH));
        }

        // Upper landing, level with the upper floor.
        c.fill(18, 6, 13, 23, 6, 14, tread);

        // Spine: panelling that steps up with flight B, a newel post at the
        // turn, and a fence rail one block above each step.
        c.fill(20, 1, 17, 21, 5, 17, POST);
        c.fill(20, 1, 16, 21, 5, 16, CEILING);
        c.fill(20, 6, 16, 21, 6, 16, fence);
        c.fill(20, 1, 15, 21, 6, 15, CEILING);
        c.fill(20, 7, 15, 21, 7, 15, fence);

        // Guard along the upper landing where it overlooks flight A.
        c.fill(18, 7, 15, 19, 7, 15, fence);
    }

    /**
     * Straight stone stair from the scullery down to the cellar. The top
     * step sits in the floor course so the descent starts flush.
     */
    private static void buildCellarStair(HouseCanvas c) {
        for (int step = 0; step < 5; step++) {
            int x = 21 + step;
            int y = -step;
            if (y < 0 && y > -4) {
                c.set(x, 0, 24, AIR); // headroom over the upper steps
            }
            c.set(x, y, 24, stairs(Blocks.STONE_BRICK_STAIRS, Direction.WEST));
            if (y - 1 >= HouseLayout.BASEMENT_FLOOR_Y + 1) {
                c.fill(x, HouseLayout.BASEMENT_FLOOR_Y + 1, 24, x, y - 1, 24, STONE);
            }
        }

        c.fill(22, 1, 23, 24, 1, 23, Blocks.SPRUCE_FENCE.defaultBlockState());
    }

    // ------------------------------------------------------------------
    // Chimneys
    // ------------------------------------------------------------------

    static void buildChimneys(HouseCanvas c) {
        buildGreatChimney(c);
        buildStudyChimney(c);
        buildKitchenChimney(c);
    }

    /**
     * The great-room stack: a broad external chimney on the west wall that
     * steps in twice and rises well clear of the tallest ridge.
     */
    private static void buildGreatChimney(HouseCanvas c) {
        c.fill(-3, 0, 5, -1, 0, 10, STONE);
        c.fill(-3, 1, 6, -1, 5, 9, BRICK);

        for (int z = 6; z <= 9; z++) {
            c.set(-3, 6, z, stairs(Blocks.BRICK_STAIRS, Direction.EAST));
        }
        c.fill(-2, 6, 6, -1, 11, 9, BRICK);

        for (int x = -2; x <= -1; x++) {
            c.set(x, 12, 6, stairs(Blocks.BRICK_STAIRS, Direction.SOUTH));
            c.set(x, 12, 9, stairs(Blocks.BRICK_STAIRS, Direction.NORTH));
        }
        c.fill(-2, 12, 7, -1, 18, 8, BRICK);

        // Corbelled cap and a pair of pots.
        c.fill(-3, 19, 6, 0, 19, 9, BRICK);
        chimneyPot(c, -2, 20, 7);
        chimneyPot(c, -1, 20, 8);

        // Inside: a full-height chimney breast on both floors.
        c.fill(1, 1, 6, 2, 5, 9, BRICK);
        c.fill(1, 6, 6, 2, 10, 9, BRICK);
    }

    /** A rear stack for the study fireplace; it also anchors the rear elevation. */
    private static void buildStudyChimney(HouseCanvas c) {
        c.fill(4, 0, 27, 9, 0, 28, STONE);
        c.fill(5, 1, 27, 8, 9, 28, BRICK);
        for (int z = 27; z <= 28; z++) {
            c.set(5, 10, z, stairs(Blocks.BRICK_STAIRS, Direction.EAST));
            c.set(8, 10, z, stairs(Blocks.BRICK_STAIRS, Direction.WEST));
        }
        c.fill(6, 10, 27, 7, 16, 28, BRICK);
        c.fill(5, 17, 26, 8, 17, 29, BRICK);
        chimneyPot(c, 6, 18, 27);
        chimneyPot(c, 7, 18, 28);

        c.fill(5, 1, 25, 8, 5, 25, BRICK);
    }

    /** A plainer kitchen stack on the east wall behind the range. */
    private static void buildKitchenChimney(HouseCanvas c) {
        c.fill(28, 0, 6, 29, 0, 10, STONE);
        c.fill(28, 1, 7, 29, 8, 9, BRICK);
        for (int z = 7; z <= 9; z++) {
            c.set(29, 9, z, stairs(Blocks.BRICK_STAIRS, Direction.WEST));
        }
        c.fill(28, 9, 7, 28, 15, 9, BRICK);
        c.fill(28, 16, 7, 29, 16, 9, BRICK);
        chimneyPot(c, 28, 17, 8);
    }

    private static void chimneyPot(HouseCanvas c, int x, int y, int z) {
        c.set(x, y, z, Blocks.BRICK_WALL.defaultBlockState());
        c.set(x, y + 1, z, Blocks.FLOWER_POT.defaultBlockState());
    }

    // ------------------------------------------------------------------
    // Facade: masonry courses, timber framing
    // ------------------------------------------------------------------

    static void buildFacade(HouseCanvas c) {
        masonryCourses(c);
        floorBeams(c);
        cornerPosts(c);
        explicitFraming(c);
        towerMasonry(c);
        porchTimber(c);
    }

    private static boolean isExteriorCell(HouseCanvas c, int x, int y, int z) {
        return HouseLayout.isWithinEnvelope(x, y, z)
                && c.isAir(x, y, z)
                && !HouseLayout.isRoomInterior(x, y, z);
    }

    private static boolean facesOutward(HouseCanvas c, int x, int y, int z, Face face) {
        return switch (face) {
            case NORTH -> isExteriorCell(c, x, y, z - 1);
            case SOUTH -> isExteriorCell(c, x, y, z + 1);
            case EAST -> isExteriorCell(c, x + 1, y, z);
            case WEST -> isExteriorCell(c, x - 1, y, z);
        };
    }

    private static boolean exposedAlongX(HouseCanvas c, int x, int y, int z) {
        return isExteriorCell(c, x - 1, y, z) || isExteriorCell(c, x + 1, y, z);
    }

    private static boolean exposedAlongZ(HouseCanvas c, int x, int y, int z) {
        return isExteriorCell(c, x, y, z - 1) || isExteriorCell(c, x, y, z + 1);
    }

    private static boolean isThresholdWall(int x, int y, int z) {
        return z == HouseLayout.THRESHOLD_Z
                && x >= HouseLayout.HALL_MIN_X
                && x <= HouseLayout.HALL_MAX_X
                && y >= 1
                && y <= 5;
    }

    /** Stone ground course and a stone plinth under every exterior wall. */
    private static void masonryCourses(HouseCanvas c) {
        for (int x = HouseLayout.MIN_X; x <= HouseLayout.MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.MAX_Z; z++) {
                BlockState ground = c.get(x, 0, z);
                if (!ground.isAir() && (exposedAlongX(c, x, 0, z) || exposedAlongZ(c, x, 0, z))
                        && (ground.is(Blocks.OAK_PLANKS) || ground.is(Blocks.SPRUCE_PLANKS))) {
                    c.set(x, 0, z, STONE);
                }

                if (c.get(x, 1, z).is(Blocks.WHITE_TERRACOTTA)
                        && !isThresholdWall(x, 1, z)
                        && (exposedAlongX(c, x, 1, z) || exposedAlongZ(c, x, 1, z))) {
                    c.set(x, 1, z, weathered(x, 1, z));
                }
            }
        }
    }

    /** Dark bressumer/floor beams wherever a floor line meets the outside. */
    private static void floorBeams(HouseCanvas c) {
        for (int x = HouseLayout.MIN_X; x <= HouseLayout.MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.MAX_Z; z++) {
                BlockState state = c.get(x, 6, z);
                if (state.isAir() || !(state.is(Blocks.WHITE_TERRACOTTA) || state.is(Blocks.DARK_OAK_PLANKS))) {
                    continue;
                }

                boolean alongX = exposedAlongZ(c, x, 6, z);
                boolean alongZ = exposedAlongX(c, x, 6, z);
                if (!alongX && !alongZ) {
                    continue;
                }

                Direction.Axis axis = alongX && alongZ ? Direction.Axis.Y
                        : alongX ? Direction.Axis.X : Direction.Axis.Z;
                c.set(x, 6, z, log(Blocks.DARK_OAK_LOG, axis));
            }
        }
    }

    /** Timber posts at every exterior corner of every mass. */
    private static void cornerPosts(HouseCanvas c) {
        for (int x = HouseLayout.MIN_X; x <= HouseLayout.MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.MAX_Z; z++) {
                for (int y = 1; y <= 14; y++) {
                    if (y == 6) {
                        continue;
                    }
                    BlockState state = c.get(x, y, z);
                    if (!state.is(Blocks.WHITE_TERRACOTTA) && !state.is(Blocks.STONE_BRICKS)
                            && !state.is(Blocks.MOSSY_STONE_BRICKS)) {
                        continue;
                    }
                    if (y == 1 && !state.is(Blocks.WHITE_TERRACOTTA)) {
                        continue;
                    }
                    if (exposedAlongX(c, x, y, z) && exposedAlongZ(c, x, y, z)) {
                        c.set(x, y, z, POST);
                    }
                }
            }
        }
    }

    /**
     * Authored studs and rails per elevation. Only exterior plaster is
     * replaced, so interior partitions and openings are left alone.
     */
    private static void explicitFraming(HouseCanvas c) {
        // Great room.
        frame(c, Face.NORTH, 0, 0, 13, 1, 5, new int[]{0, 3, 10, 13}, new int[]{4});
        frame(c, Face.NORTH, -2, 4, 9, 1, 4, new int[]{4, 9}, new int[]{4});
        frame(c, Face.WEST, 4, -1, -1, 1, 5, new int[]{}, new int[]{4});
        frame(c, Face.EAST, 9, -1, -1, 1, 5, new int[]{}, new int[]{4});
        frame(c, Face.NORTH, -1, 0, 13, 7, 10, new int[]{0, 5, 8, 13}, new int[]{7, 10});
        frame(c, Face.WEST, 0, 0, 15, 1, 5, new int[]{0, 3, 5, 10, 12, 15}, new int[]{2});
        frame(c, Face.WEST, 0, -1, 15, 7, 10, new int[]{-1, 1, 4, 11, 14, 15}, new int[]{7, 10});
        frame(c, Face.EAST, 13, 0, 3, 1, 5, new int[]{0, 3}, new int[]{4});

        // Entrance and hall.
        frame(c, Face.NORTH, HouseLayout.FRONT_DOOR_Z, 13, 17, 4, 5, new int[]{}, new int[]{4});
        frame(c, Face.WEST, 17, 1, 3, 1, 5, new int[]{1, 3}, new int[]{4});
        frame(c, Face.NORTH, 0, 13, 17, 7, 10, new int[]{13, 17}, new int[]{7, 10});
        frame(c, Face.SOUTH, HouseLayout.THRESHOLD_Z, 13, 17, 7, 10, new int[]{13, 15, 17}, new int[]{10});
        frame(c, Face.SOUTH, HouseLayout.THRESHOLD_Z, 13, 13, 1, 5, new int[]{13}, new int[]{});
        frame(c, Face.SOUTH, HouseLayout.THRESHOLD_Z, 17, 17, 1, 5, new int[]{17}, new int[]{});

        // Kitchen wing.
        frame(c, Face.NORTH, 1, 17, 27, 1, 5, new int[]{17, 18, 22, 23, 26, 27}, new int[]{4});
        frame(c, Face.NORTH, 1, 17, 27, 7, 8, new int[]{17, 20, 24, 27}, new int[]{7});
        frame(c, Face.EAST, 27, 1, 12, 1, 5, new int[]{1, 2, 5, 6, 9, 12}, new int[]{4});
        frame(c, Face.EAST, 27, 1, 12, 7, 8, new int[]{1, 6, 10, 12}, new int[]{8});
        frame(c, Face.SOUTH, 12, 25, 27, 1, 8, new int[]{25, 27}, new int[]{3, 8});

        // Stair tower above the lower roofs.
        frame(c, Face.EAST, 24, 12, 20, 7, 14, new int[]{12, 14, 17, 20}, new int[]{8, 14});
        frame(c, Face.NORTH, 12, 17, 24, 9, 14, new int[]{17, 19, 22, 24}, new int[]{14});
        frame(c, Face.WEST, 17, 12, 20, 11, 14, new int[]{12, 16, 20}, new int[]{14});
        frame(c, Face.SOUTH, 20, 17, 24, 9, 14, new int[]{17, 20, 24}, new int[]{14});

        // Study wing.
        frame(c, Face.WEST, 1, 15, 26, 1, 5, new int[]{16, 19, 21, 24, 26}, new int[]{4});
        frame(c, Face.WEST, 1, 15, 26, 7, 9, new int[]{15, 18, 23, 26}, new int[]{7, 9});
        frame(c, Face.SOUTH, 26, 1, 13, 1, 5, new int[]{1, 3, 4, 9, 11, 13}, new int[]{3});
        frame(c, Face.SOUTH, 26, 1, 13, 7, 9, new int[]{1, 3, 4, 9, 11, 13}, new int[]{9});

        // Service range.
        frame(c, Face.EAST, 27, 20, 25, 1, 5, new int[]{20, 21, 24, 25}, new int[]{4});
        frame(c, Face.EAST, 27, 20, 25, 7, 8, new int[]{20, 21, 24, 25}, new int[]{8});
        frame(c, Face.NORTH, 20, 24, 27, 1, 5, new int[]{27}, new int[]{4});
        frame(c, Face.SOUTH, 25, 17, 27, 1, 5, new int[]{22, 27}, new int[]{4});
        frame(c, Face.SOUTH, 25, 17, 27, 7, 8, new int[]{22, 27}, new int[]{8});
    }

    private static void frame(
            HouseCanvas c,
            Face face,
            int plane,
            int a0,
            int a1,
            int y0,
            int y1,
            int[] posts,
            int[] rails
    ) {
        boolean horizontal = face == Face.NORTH || face == Face.SOUTH;
        BlockState rail = log(Blocks.STRIPPED_DARK_OAK_LOG, horizontal ? Direction.Axis.X : Direction.Axis.Z);

        for (int y : rails) {
            for (int a = a0; a <= a1; a++) {
                int x = horizontal ? a : plane;
                int z = horizontal ? plane : a;
                if (c.get(x, y, z).is(Blocks.WHITE_TERRACOTTA) && facesOutward(c, x, y, z, face)) {
                    c.set(x, y, z, rail);
                }
            }
        }

        for (int a : posts) {
            for (int y = y0; y <= y1; y++) {
                int x = horizontal ? a : plane;
                int z = horizontal ? plane : a;
                BlockState state = c.get(x, y, z);
                boolean timberable = state.is(Blocks.WHITE_TERRACOTTA)
                        || state.is(Blocks.STRIPPED_DARK_OAK_LOG);
                if (timberable && facesOutward(c, x, y, z, face)) {
                    c.set(x, y, z, POST);
                }
            }
        }
    }

    /** The stair tower's exposed yard face is masonry up to the floor line. */
    private static void towerMasonry(HouseCanvas c) {
        for (int z = 12; z <= 20; z++) {
            for (int y = 1; y <= 6; y++) {
                if (facesOutward(c, 24, y, z, Face.EAST)) {
                    c.set(24, y, z, weathered(24, y, z));
                }
            }
        }
        // A projecting drip course where the masonry gives way to timber,
        // interrupted by the landing window.
        for (int z = 13; z <= 19; z++) {
            if (z == 18 || z == 19) {
                continue;
            }
            if (isExteriorCell(c, 25, 7, z)) {
                c.set(25, 7, z, stairsTop(Blocks.STONE_BRICK_STAIRS, Direction.WEST));
            }
        }
    }

    /** Carved corner post and knee braces framing the recessed porch. */
    private static void porchTimber(HouseCanvas c) {
        c.fill(17, 1, 0, 17, 5, 0, POST);
        c.set(16, 5, 0, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.EAST));
        c.set(14, 5, 0, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.WEST));

        // Jetty brackets under the great room's projecting upper floor.
        for (int x : new int[]{0, 3, 10, 13}) {
            c.setIfAir(x, 5, -1, stairsTop(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));
        }
    }

    static BlockState weathered(int x, int y, int z) {
        return Math.floorMod(x * 7 + z * 13 + y * 3, 6) == 0 ? MOSSY_STONE : STONE;
    }

    // ------------------------------------------------------------------
    // Glazing
    // ------------------------------------------------------------------

    static void glaze(HouseCanvas c) {
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
        for (Window window : HouseLayout.WINDOWS) {
            for (int a = window.a0(); a <= window.a1(); a++) {
                for (int y = window.y0(); y <= window.y1(); y++) {
                    c.set(window.x(a), y, window.z(a), pane);
                }
            }
        }
    }

    /**
     * Sills and drip hoods give the glazing depth. They are added after the
     * roofs so they can never take a roof course's cell.
     */
    static void trimWindows(HouseCanvas c) {
        for (Window window : HouseLayout.WINDOWS) {
            if (window.face() == Face.NORTH && window.plane() == HouseLayout.FRONT_DOOR_Z) {
                continue; // The door surround is kept clear.
            }

            Direction outward = switch (window.face()) {
                case NORTH -> Direction.NORTH;
                case SOUTH -> Direction.SOUTH;
                case EAST -> Direction.EAST;
                case WEST -> Direction.WEST;
            };

            for (int a = window.a0(); a <= window.a1(); a++) {
                int x = window.x(a) + outward.getStepX();
                int z = window.z(a) + outward.getStepZ();

                int sillY = window.y0() - 1;
                if (isExteriorCell(c, x, sillY, z)) {
                    c.set(x, sillY, z, stairsTop(Blocks.DARK_OAK_STAIRS, outward.getOpposite()));
                }

                int hoodY = window.y1() + 1;
                if (hoodY <= 5 && isExteriorCell(c, x, hoodY, z)) {
                    c.set(x, hoodY, z, slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Roof accents
    // ------------------------------------------------------------------

    static void roofAccents(HouseCanvas c) {
        HouseRoofs.placeEaveBrackets(c, HouseLayout.ROOF_TOWER, 2);
        c.set(20, 19, 16, Blocks.LIGHTNING_ROD.defaultBlockState());
    }

    // ------------------------------------------------------------------
    // Porch, approach and yard
    // ------------------------------------------------------------------

    static void buildPorchAndYard(HouseCanvas c, HouseBuilder.SiteProfile site) {
        c.fill(HouseLayout.HALL_MIN_X, 0, 0, HouseLayout.HALL_MAX_X, 0, 3, STONE);
        c.set(HouseLayout.HALL_MIN_X, 1, 1, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        c.set(HouseLayout.HALL_MIN_X, 1, 2, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));

        if (site.frontWater()) {
            buildWaterApproach(c, site.frontGroundY());
        } else {
            buildGroundApproach(c, site.frontGroundY());
        }

        // Service yard: paved over the cellar, closed by a low garden wall
        // with a gate on the east side.
        c.fill(25, 0, 13, 27, 0, 19, STONE);
        c.fill(28, 1, 12, 28, 1, 20, Blocks.STONE_BRICK_WALL.defaultBlockState());
        c.set(28, 1, 16, Blocks.SPRUCE_FENCE_GATE.defaultBlockState()
                .setValue(FenceGateBlock.FACING, Direction.EAST));
    }

    private static void buildGroundApproach(HouseCanvas c, int absoluteGroundY) {
        int drop = Math.max(0, c.origin.getY() - absoluteGroundY);
        int courses = Math.max(1, Math.min(6, drop + 1));

        for (int step = 0; step < courses; step++) {
            int z = -1 - step;
            int y = -step;
            for (int x = HouseLayout.HALL_MIN_X; x <= HouseLayout.HALL_MAX_X; x++) {
                c.set(x, y, z, stairs(Blocks.STONE_BRICK_STAIRS, Direction.SOUTH));
                extendSupportToTerrain(c, x, z, y - 1, STONE);
            }
        }
    }

    private static void buildWaterApproach(HouseCanvas c, int absoluteWaterSurfaceY) {
        int minX = HouseLayout.HALL_MIN_X;
        int maxX = HouseLayout.HALL_MAX_X;

        c.fill(minX, 0, -4, maxX, 0, -1, SPRUCE_FLOOR);
        for (int x : new int[]{minX, maxX}) {
            extendSupportToTerrain(c, x, -4, -1, log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
        }

        BlockState fence = Blocks.SPRUCE_FENCE.defaultBlockState();
        for (int z = -1; z >= -4; z--) {
            c.set(minX, 1, z, fence);
            if (z != -3) {
                c.set(maxX, 1, z, fence);
            }
        }
        c.set(HouseLayout.AXIS_X, 1, -4, fence);

        c.fill(minX, 1, -4, minX, 3, -4, POST);
        c.set(minX, 4, -4, Blocks.LANTERN.defaultBlockState());

        extendSupportToTerrain(c, maxX, -3, -1, log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y));
        int waterRelativeY = absoluteWaterSurfaceY - c.origin.getY();
        int ladderBottomY = Math.max(-6, Math.min(-1, waterRelativeY - 2));
        for (int y = 0; y >= ladderBottomY; y--) {
            c.set(maxX + 1, y, -3, Blocks.LADDER.defaultBlockState()
                    .setValue(LadderBlock.FACING, Direction.EAST));
        }
    }

    private static void extendSupportToTerrain(HouseCanvas c, int x, int z, int startY, BlockState support) {
        for (int y = startY; y >= startY - 12; y--) {
            BlockState current = c.get(x, y, z);
            if (!current.isAir() && !current.canBeReplaced() && current.getFluidState().isEmpty()) {
                return;
            }
            c.set(x, y, z, support);
        }
    }

    // ------------------------------------------------------------------
    // Foundations
    // ------------------------------------------------------------------

    /**
     * Carries every ground-level course down to real terrain so the house
     * never floats on a slope: masonry is visible wherever the ground falls.
     */
    static void buildFoundations(HouseCanvas c) {
        for (int x = HouseLayout.MIN_X; x <= HouseLayout.MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.MAX_Z; z++) {
                if (z < 0 && x >= HouseLayout.HALL_MIN_X && x <= HouseLayout.HALL_MAX_X) {
                    continue; // The front approach carries its own supports.
                }

                int start;
                if (!c.isAir(x, HouseLayout.BASEMENT_FLOOR_Y, z)
                        && x >= 17 && x <= 27 && z >= 12 && z <= 25) {
                    start = HouseLayout.BASEMENT_FLOOR_Y - 1;
                } else if (isFoundationCourse(c.get(x, 0, z))) {
                    start = -1;
                } else {
                    continue;
                }

                for (int y = start; y >= start - 12; y--) {
                    BlockState current = c.get(x, y, z);
                    if (!current.isAir() && !current.canBeReplaced() && current.getFluidState().isEmpty()) {
                        break;
                    }
                    c.set(x, y, z, weathered(x, y, z));
                }
            }
        }
    }

    private static boolean isFoundationCourse(BlockState state) {
        return state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.MOSSY_STONE_BRICKS)
                || state.is(Blocks.OAK_PLANKS)
                || state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.BRICKS);
    }
}
