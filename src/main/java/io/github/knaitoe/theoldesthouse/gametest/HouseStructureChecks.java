package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseLayout.Room;
import io.github.knaitoe.theoldesthouse.house.HouseLayout.Window;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Architectural invariants of a generated House, checked against the real
 * blocks. Each check returns human-readable failures (empty when it passes).
 */
final class HouseStructureChecks {
    private final ServerLevel level;
    private final BlockPos origin;

    HouseStructureChecks(ServerLevel level, BlockPos origin) {
        this.level = level;
        this.origin = origin;
    }

    List<String> runAll() {
        List<String> failures = new ArrayList<>();
        failures.addAll(sightlineIsClear());
        failures.addAll(thresholdIsOrdinaryWall());
        failures.addAll(noRoofInsideRooms());
        failures.addAll(roofsAreContinuous());
        failures.addAll(roomsAreEnclosed());
        failures.addAll(windowsAreGlazed());
        failures.addAll(doorsAreComplete());
        failures.addAll(everyRoomIsLit());
        failures.addAll(roomsAreReachable());
        return failures;
    }

    List<String> runAfterReveal() {
        List<String> failures = new ArrayList<>(sightlineIsClear());
        BlockState lower = get(HouseLayout.AXIS_X, 1, HouseLayout.THRESHOLD_Z);
        BlockState upper = get(HouseLayout.AXIS_X, 2, HouseLayout.THRESHOLD_Z);
        if (!(lower.getBlock() instanceof DoorBlock) || !(upper.getBlock() instanceof DoorBlock)) {
            failures.add("revealed threshold has no door: " + lower + " / " + upper);
        }
        return failures;
    }

    // ------------------------------------------------------------------

    /** Nothing with real collision on the hall axis between the doors, at eye level or below. */
    List<String> sightlineIsClear() {
        List<String> failures = new ArrayList<>();
        for (int z = HouseLayout.FRONT_DOOR_Z + 1; z < HouseLayout.THRESHOLD_Z; z++) {
            for (int y = 1; y <= 3; y++) {
                if (collisionHeight(HouseLayout.AXIS_X, y, z) > 0.1D) {
                    failures.add("sightline blocked at " + rel(HouseLayout.AXIS_X, y, z) + " by " + get(HouseLayout.AXIS_X, y, z));
                }
            }
        }
        return failures;
    }

    List<String> thresholdIsOrdinaryWall() {
        List<String> failures = new ArrayList<>();
        for (int x = HouseLayout.HALL_MIN_X; x <= HouseLayout.HALL_MAX_X; x++) {
            for (int y = 1; y <= 5; y++) {
                if (!get(x, y, HouseLayout.THRESHOLD_Z).is(Blocks.WHITE_TERRACOTTA)) {
                    failures.add("threshold wall is not plain plaster at " + rel(x, y, HouseLayout.THRESHOLD_Z)
                            + ": " + get(x, y, HouseLayout.THRESHOLD_Z));
                }
            }
        }
        return failures;
    }

    List<String> noRoofInsideRooms() {
        List<String> failures = new ArrayList<>();
        for (Room room : HouseLayout.ROOMS) {
            HouseLayout.Box box = room.box();
            for (int x = box.x0(); x <= box.x1(); x++) {
                for (int y = box.y0(); y <= box.y1(); y++) {
                    for (int z = box.z0(); z <= box.z1(); z++) {
                        if (!room.contains(x, y, z)) {
                            continue;
                        }
                        BlockState state = get(x, y, z);
                        if (state.is(Blocks.DEEPSLATE_TILES)
                                || state.is(Blocks.DEEPSLATE_TILE_STAIRS)
                                || state.is(Blocks.DEEPSLATE_TILE_SLAB)) {
                            failures.add("roof block inside " + room.name() + " at " + rel(x, y, z));
                        }
                    }
                }
            }
        }
        return limit(failures);
    }

    /**
     * Every column under a roof must be capped at (or above) the highest roof
     * surface planned for it; anything lower is a visible hole in the roof.
     */
    List<String> roofsAreContinuous() {
        List<String> failures = new ArrayList<>();
        for (int x = HouseLayout.MIN_X; x <= HouseLayout.MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.MAX_Z; z++) {
                int highest = Integer.MIN_VALUE;
                for (HouseLayout.Roof roof : HouseLayout.ROOFS) {
                    if (roof.covers(x, z)) {
                        highest = Math.max(highest, roof.height(x, z));
                    }
                }
                if (highest == Integer.MIN_VALUE) {
                    continue;
                }

                int top = HouseLayout.MAX_Y;
                while (top > HouseLayout.MIN_Y && get(x, top, z).isAir()) {
                    top--;
                }
                if (top < highest) {
                    failures.add("roof hole at " + rel(x, highest, z) + " (column tops out at y=" + top + ")");
                }
            }
        }
        return limit(failures);
    }

    /**
     * Flood-fills air from every room. Reaching the edge of the envelope
     * means a hole to the outside (a gap in a roof, wall or floor).
     */
    List<String> roomsAreEnclosed() {
        List<String> failures = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        for (Room room : HouseLayout.ROOMS) {
            BlockPos seed = firstAir(room);
            if (seed == null) {
                failures.add("room " + room.name() + " has no air");
                continue;
            }
            if (visited.contains(seed.asLong())) {
                continue;
            }

            Deque<BlockPos> queue = new ArrayDeque<>();
            queue.add(seed);
            visited.add(seed.asLong());
            while (!queue.isEmpty()) {
                BlockPos cell = queue.poll();
                if (isOutsideEnvelope(cell)) {
                    failures.add("room " + room.name() + " leaks to the outside near " + cell);
                    break;
                }
                for (Direction direction : Direction.values()) {
                    BlockPos next = cell.relative(direction);
                    if (visited.add(next.asLong()) && get(next.getX(), next.getY(), next.getZ()).isAir()) {
                        queue.add(next);
                    }
                }
            }
        }
        return failures;
    }

    List<String> windowsAreGlazed() {
        List<String> failures = new ArrayList<>();
        for (Window window : HouseLayout.WINDOWS) {
            for (int a = window.a0(); a <= window.a1(); a++) {
                for (int y = window.y0(); y <= window.y1(); y++) {
                    BlockState state = get(window.x(a), y, window.z(a));
                    if (!state.is(Blocks.GLASS_PANE)) {
                        failures.add("window cell " + rel(window.x(a), y, window.z(a)) + " holds " + state);
                    }
                }
            }
        }
        return failures;
    }

    List<String> doorsAreComplete() {
        List<String> failures = new ArrayList<>();
        for (int x = HouseLayout.MIN_X; x <= HouseLayout.MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.MAX_Z; z++) {
                for (int y = HouseLayout.MIN_Y; y <= HouseLayout.MAX_Y; y++) {
                    BlockState state = get(x, y, z);
                    if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                        BlockState upper = get(x, y + 1, z);
                        if (!(upper.getBlock() instanceof DoorBlock)) {
                            failures.add("door at " + rel(x, y, z) + " is missing its upper half");
                        }
                    }
                }
            }
        }
        return failures;
    }

    List<String> everyRoomIsLit() {
        List<String> failures = new ArrayList<>();
        for (Room room : HouseLayout.ROOMS) {
            HouseLayout.Box box = room.box();
            boolean lit = false;
            for (int x = box.x0(); x <= box.x1() && !lit; x++) {
                for (int y = box.y0(); y <= box.y1() && !lit; y++) {
                    for (int z = box.z0(); z <= box.z1() && !lit; z++) {
                        lit = get(x, y, z).getLightEmission() > 0;
                    }
                }
            }
            if (!lit) {
                failures.add("room " + room.name() + " has no light source");
            }
        }
        return failures;
    }

    /**
     * Walks the house as a player would (0.6 step height, 1.8 blocks of
     * headroom, doors passable) from the front porch, and requires every
     * room to be reachable. This is what catches stairs that end a full
     * block short of the floor they serve.
     */
    List<String> roomsAreReachable() {
        record Target(String name, int x, int feetY, int z) {
        }
        List<Target> targets = List.of(
                new Target("great_room", 9, 1, 8),
                new Target("great_bay", 6, 1, 0),
                new Target("hall_end", HouseLayout.AXIS_X, 1, HouseLayout.THRESHOLD_Z - 1),
                new Target("kitchen", 22, 1, 7),
                new Target("yard", 26, 1, 16),
                new Target("study", 9, 1, 19),
                new Target("stair_landing", 20, 4, 18),
                new Target("scullery", 20, 1, 23),
                new Target("cellar", 22, -4, 18),
                new Target("upper_hall", HouseLayout.AXIS_X, 7, 10),
                new Target("principal_bedroom", 7, 7, 6),
                new Target("literary_bedroom", 5, 7, 12),
                new Target("long_gallery", 7, 7, 22),
                new Target("maker_loft", 22, 7, 6),
                new Target("box_room", 22, 7, 23)
        );

        Set<Long> reachedCells = walk(HouseLayout.AXIS_X * 2 + 1, 2 * 2 + 1, 1.0D);

        List<String> failures = new ArrayList<>();
        for (Target target : targets) {
            if (!reachedCells.contains(BlockPos.asLong(target.x(), target.feetY(), target.z()))) {
                failures.add(target.name() + " is not reachable on foot from the front porch");
            }
        }
        return failures;
    }

    /**
     * Breadth-first walk over half-block sample points. Returns the set of
     * block cells (relative, keyed by feet block Y) a player can stand in.
     */
    private Set<Long> walk(int startGx, int startGz, double startFeet) {
        Set<Long> visited = new HashSet<>();
        Set<Long> cells = new HashSet<>();
        Deque<double[]> queue = new ArrayDeque<>();
        queue.add(new double[]{startGx, startGz, startFeet});
        visited.add(nodeKey(startGx, startGz, startFeet));

        int minG = HouseLayout.MIN_X * 2;
        int maxG = HouseLayout.MAX_X * 2 + 1;
        int minGz = HouseLayout.CLEAR_MIN_Z * 2;
        int maxGz = HouseLayout.MAX_Z * 2 + 1;

        while (!queue.isEmpty()) {
            double[] node = queue.poll();
            int gx = (int) node[0];
            int gz = (int) node[1];
            double feet = node[2];
            cells.add(BlockPos.asLong(Math.floorDiv(gx, 2), (int) Math.floor(feet + 1.0E-4D), Math.floorDiv(gz, 2)));

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                int ngx = gx + direction.getStepX();
                int ngz = gz + direction.getStepZ();
                if (ngx < minG || ngx > maxG || ngz < minGz || ngz > maxGz) {
                    continue;
                }

                double px = ngx * 0.5D + 0.25D;
                double pz = ngz * 0.5D + 0.25D;
                double next = floorAt(px, pz, feet + 0.6D, feet - 3.5D);
                if (Double.isNaN(next)) {
                    continue;
                }

                double midX = (gx * 0.5D + 0.25D + px) / 2.0D;
                double midZ = (gz * 0.5D + 0.25D + pz) / 2.0D;
                if (!hasHeadroom(px, pz, next) || !hasHeadroom(midX, midZ, Math.max(feet, next))) {
                    continue;
                }

                if (visited.add(nodeKey(ngx, ngz, next))) {
                    queue.add(new double[]{ngx, ngz, next});
                }
            }
        }
        return cells;
    }

    private static long nodeKey(int gx, int gz, double feet) {
        return ((long) (gx + 1024) << 40) | ((long) (gz + 1024) << 20) | (long) Math.round((feet + 64.0D) * 16.0D);
    }

    /** Highest collision top at a point, within [minFeet, maxFeet]; NaN if none. */
    private double floorAt(double px, double pz, double maxFeet, double minFeet) {
        int bx = (int) Math.floor(px);
        int bz = (int) Math.floor(pz);
        double best = Double.NaN;
        for (int by = (int) Math.floor(maxFeet); by >= (int) Math.floor(minFeet) - 1; by--) {
            for (AABB box : collisionBoxes(bx, by, bz)) {
                if (px - bx < box.minX || px - bx > box.maxX || pz - bz < box.minZ || pz - bz > box.maxZ) {
                    continue;
                }
                double top = by + box.maxY;
                if (top <= maxFeet + 1.0E-6D && top >= minFeet && (Double.isNaN(best) || top > best)) {
                    best = top;
                }
            }
        }
        return best;
    }

    private boolean hasHeadroom(double px, double pz, double feet) {
        double half = 0.2D;
        AABB body = new AABB(px - half, feet + 0.01D, pz - half, px + half, feet + 1.79D, pz + half);
        for (int bx = (int) Math.floor(body.minX); bx <= (int) Math.floor(body.maxX); bx++) {
            for (int bz = (int) Math.floor(body.minZ); bz <= (int) Math.floor(body.maxZ); bz++) {
                for (int by = (int) Math.floor(body.minY); by <= (int) Math.floor(body.maxY); by++) {
                    for (AABB box : collisionBoxes(bx, by, bz)) {
                        if (box.move(bx, by, bz).intersects(body)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private List<AABB> collisionBoxes(int x, int y, int z) {
        BlockPos pos = origin.offset(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof DoorBlock) {
            return List.of();
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        return shape.isEmpty() ? List.of() : shape.toAabbs();
    }

    // ------------------------------------------------------------------

    private BlockPos firstAir(Room room) {
        HouseLayout.Box box = room.box();
        for (int y = box.y0(); y <= box.y1(); y++) {
            for (int x = box.x0(); x <= box.x1(); x++) {
                for (int z = box.z0(); z <= box.z1(); z++) {
                    if (room.contains(x, y, z) && get(x, y, z).isAir()) {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isOutsideEnvelope(BlockPos rel) {
        return rel.getX() <= HouseLayout.MIN_X || rel.getX() >= HouseLayout.MAX_X
                || rel.getZ() <= HouseLayout.CLEAR_MIN_Z || rel.getZ() >= HouseLayout.MAX_Z
                || rel.getY() >= HouseLayout.MAX_Y || rel.getY() <= HouseLayout.MIN_Y - 2;
    }

    private double collisionHeight(int x, int y, int z) {
        BlockPos pos = origin.offset(x, y, z);
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return shape.isEmpty() ? 0.0D : shape.max(Direction.Axis.Y);
    }

    private BlockState get(int x, int y, int z) {
        return level.getBlockState(origin.offset(x, y, z));
    }

    private static String rel(int x, int y, int z) {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    private static List<String> limit(List<String> failures) {
        if (failures.size() <= 12) {
            return failures;
        }
        List<String> limited = new ArrayList<>(failures.subList(0, 12));
        limited.add("... and " + (failures.size() - 12) + " more");
        return limited;
    }
}
