package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.house.HouseLayout.Roof;
import io.github.knaitoe.theoldesthouse.house.HouseLayout.RoofKind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Builds the House's independent, intersecting roof systems.
 *
 * Each roof is a simple prism. Where roofs overlap, every roof still places
 * its own surface so valleys and overhangs stay watertight; when two roofs
 * claim the same cell, the one for which that cell lies deeper inside its own
 * footprint wins (a gable's ridge beats a neighbour's eave). Roof blocks are
 * never placed inside rooms or over existing walls.
 */
final class HouseRoofs {
    private static final BlockState RIDGE_SLAB =
            HouseCanvas.slab(Blocks.DEEPSLATE_TILE_SLAB, SlabType.BOTTOM);
    private static final BlockState PLASTER = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState TIMBER_POST =
            HouseCanvas.log(Blocks.STRIPPED_DARK_OAK_LOG, Direction.Axis.Y);

    private HouseRoofs() {
    }

    static void build(HouseCanvas canvas, List<Roof> roofs) {
        // Gable-end walls first: they are wall, not roof, and must not be
        // displaced by a neighbouring roof's surface.
        for (Roof roof : roofs) {
            fillGableEnds(canvas, roof);
        }

        Map<Long, Candidate> surface = new HashMap<>();
        for (int priority = 0; priority < roofs.size(); priority++) {
            Roof roof = roofs.get(priority);
            for (int x = roof.x0(); x <= roof.x1(); x++) {
                for (int z = roof.z0(); z <= roof.z1(); z++) {
                    int y = roof.height(x, z);
                    long key = BlockPos.asLong(x, y, z);
                    Candidate candidate = new Candidate(roof, x, y, z, roof.depth(x, z), priority);
                    Candidate existing = surface.get(key);
                    if (existing == null || candidate.beats(existing)) {
                        surface.put(key, candidate);
                    }
                }
            }
        }

        for (Candidate candidate : surface.values()) {
            if (HouseLayout.isRoomInterior(candidate.x, candidate.y, candidate.z)) {
                continue;
            }
            canvas.setIfAir(candidate.x, candidate.y, candidate.z, surfaceState(candidate.roof, candidate.x, candidate.z));
        }

        for (Candidate candidate : surface.values()) {
            lineUnderside(canvas, candidate.x, candidate.y, candidate.z);
        }

        for (Roof roof : roofs) {
            placeVergeBoards(canvas, roof);
        }
    }

    /**
     * Rooms open to the roof would otherwise see the stepped underside of the
     * tile stairs. Boarding each step with an upside-down plank stair turns
     * it into a continuous sloped ceiling.
     */
    private static void lineUnderside(HouseCanvas canvas, int x, int y, int z) {
        BlockState tile = canvas.get(x, y, z);
        if (!tile.is(Blocks.DEEPSLATE_TILE_STAIRS)
                || !HouseLayout.isRoomInterior(x, y - 1, z)
                || !canvas.isAir(x, y - 1, z)) {
            return;
        }
        Direction uphill = tile.getValue(StairBlock.FACING);
        canvas.set(x, y - 1, z, HouseCanvas.stairsTop(Blocks.SPRUCE_STAIRS, uphill.getOpposite()));
    }

    private static BlockState surfaceState(Roof roof, int x, int z) {
        int depth = roof.depth(x, z);
        if (roof.flared() && depth == 0) {
            return RIDGE_SLAB;
        }

        Direction facing = slopeFacing(roof, x, z);
        return facing == null ? RIDGE_SLAB : HouseCanvas.stairs(Blocks.DEEPSLATE_TILE_STAIRS, facing);
    }

    /**
     * Direction in which the roof rises at this column (stairs ascend toward
     * their FACING side), or null on a single-column ridge.
     */
    private static Direction slopeFacing(Roof roof, int x, int z) {
        int west = x - roof.x0();
        int east = roof.x1() - x;
        int north = z - roof.z0();
        int south = roof.z1() - z;

        return switch (roof.kind()) {
            case RIDGE_Z -> west < east ? Direction.EAST : west > east ? Direction.WEST : null;
            case RIDGE_X -> north < south ? Direction.SOUTH : north > south ? Direction.NORTH : null;
            case HIP -> {
                int min = Math.min(Math.min(west, east), Math.min(north, south));
                if (west == min && west < east) {
                    yield Direction.EAST;
                }
                if (east == min && east < west) {
                    yield Direction.WEST;
                }
                if (north == min && north < south) {
                    yield Direction.SOUTH;
                }
                if (south == min && south < north) {
                    yield Direction.NORTH;
                }
                yield null;
            }
        };
    }

    /**
     * Plaster gable triangle between the wall top and the roof surface, with
     * a central king post so the end reads as timber-framed.
     */
    private static void fillGableEnds(HouseCanvas canvas, Roof roof) {
        if (roof.kind() == RoofKind.HIP) {
            return;
        }

        boolean ridgeAlongZ = roof.kind() == RoofKind.RIDGE_Z;
        int acrossMin = ridgeAlongZ ? roof.x0() : roof.z0();
        int acrossMax = ridgeAlongZ ? roof.x1() : roof.z1();
        int centre = (acrossMin + acrossMax) / 2;
        boolean evenWidth = (acrossMax - acrossMin) % 2 == 1;

        int[] planes = {
                roof.startGable() ? (ridgeAlongZ ? roof.z0() : roof.x0()) + roof.verge() : Integer.MIN_VALUE,
                roof.endGable() ? (ridgeAlongZ ? roof.z1() : roof.x1()) - roof.verge() : Integer.MIN_VALUE
        };

        for (int plane : planes) {
            if (plane == Integer.MIN_VALUE) {
                continue;
            }

            for (int across = acrossMin; across <= acrossMax; across++) {
                int x = ridgeAlongZ ? across : plane;
                int z = ridgeAlongZ ? plane : across;
                int top = roof.height(x, z) - 1;
                boolean kingPost = across == centre || (evenWidth && across == centre + 1);

                for (int y = roof.wallTop() + 1; y <= top; y++) {
                    if (HouseLayout.isRoomInterior(x, y, z)) {
                        continue;
                    }
                    canvas.setIfAir(x, y, z, kingPost ? TIMBER_POST : PLASTER);
                }
            }
        }
    }

    /**
     * Pronounced dark-oak barge boards under the verge overhang, following the
     * slope all the way down to the eaves.
     */
    private static void placeVergeBoards(HouseCanvas canvas, Roof roof) {
        if (roof.kind() == RoofKind.HIP || roof.verge() <= 0) {
            return;
        }

        boolean ridgeAlongZ = roof.kind() == RoofKind.RIDGE_Z;
        int acrossMin = ridgeAlongZ ? roof.x0() : roof.z0();
        int acrossMax = ridgeAlongZ ? roof.x1() : roof.z1();

        for (int end = 0; end < 2; end++) {
            boolean enabled = end == 0 ? roof.startGable() : roof.endGable();
            if (!enabled) {
                continue;
            }

            int alongStart = end == 0
                    ? (ridgeAlongZ ? roof.z0() : roof.x0())
                    : (ridgeAlongZ ? roof.z1() : roof.x1()) - roof.verge() + 1;

            for (int along = alongStart; along < alongStart + roof.verge(); along++) {
                for (int across = acrossMin; across <= acrossMax; across++) {
                    int x = ridgeAlongZ ? across : along;
                    int z = ridgeAlongZ ? along : across;
                    int y = roof.height(x, z) - 1;
                    if (HouseLayout.isRoomInterior(x, y, z)) {
                        continue;
                    }

                    Direction facing = slopeFacing(roof, x, z);
                    BlockState board = facing == null
                            ? HouseCanvas.slab(Blocks.DARK_OAK_SLAB, SlabType.TOP)
                            : HouseCanvas.stairsTop(Blocks.DARK_OAK_STAIRS, facing);
                    canvas.setIfAir(x, y, z, board);
                }
            }
        }
    }

    /**
     * Dark-oak brackets tucked under a flared eave so the kick reads as a
     * deliberate overhang rather than a floating slab.
     */
    static void placeEaveBrackets(HouseCanvas canvas, Roof roof, int spacing) {
        for (int x = roof.x0(); x <= roof.x1(); x++) {
            for (int z = roof.z0(); z <= roof.z1(); z++) {
                if (roof.depth(x, z) != 0) {
                    continue;
                }
                if (Math.floorMod(x + z, spacing) != 0) {
                    continue;
                }

                Direction toWall = inwardDirection(roof, x, z);
                if (toWall == null) {
                    continue;
                }

                int y = roof.baseY() - 1;
                if (HouseLayout.isRoomInterior(x, y, z)) {
                    continue;
                }
                canvas.setIfAir(x, y, z, HouseCanvas.stairsTop(Blocks.DARK_OAK_STAIRS, toWall));
            }
        }
    }

    private static Direction inwardDirection(Roof roof, int x, int z) {
        if (x == roof.x0() && z > roof.z0() && z < roof.z1()) {
            return Direction.EAST;
        }
        if (x == roof.x1() && z > roof.z0() && z < roof.z1()) {
            return Direction.WEST;
        }
        if (z == roof.z0() && x > roof.x0() && x < roof.x1()) {
            return Direction.SOUTH;
        }
        if (z == roof.z1() && x > roof.x0() && x < roof.x1()) {
            return Direction.NORTH;
        }
        return null;
    }

    private record Candidate(Roof roof, int x, int y, int z, int depth, int priority) {
        boolean beats(Candidate other) {
            if (depth != other.depth) {
                return depth > other.depth;
            }
            return priority < other.priority;
        }
    }
}
