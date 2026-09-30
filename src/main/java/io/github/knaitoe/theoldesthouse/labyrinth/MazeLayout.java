package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Seeded, connected floor plans. Fold sleeves are identical on both sides of their crossing plane. */
public final class MazeLayout {
    public record Cell(int x, int row) {}
    public record Sleeve(BlockPos center, int turn) {
        public Vec3 local(Vec3 point) { return rotate(point.subtract(Vec3.atBottomCenterOf(center)), -turn); }
        public Vec3 world(Vec3 point) { return Vec3.atBottomCenterOf(center).add(rotate(point, turn)); }
        public BlockPos block(BlockPos local) {
            Vec3 p = rotate(new Vec3(local.getX(), local.getY(), local.getZ()), turn);
            return center.offset((int) p.x, (int) p.y, (int) p.z);
        }
    }
    private final int size;
    private final Map<Cell, Set<Cell>> graph;
    private final Set<BlockPos> floor = new LinkedHashSet<>();
    private final List<Sleeve> sleeves = new ArrayList<>();

    private MazeLayout(int size, Map<Cell, Set<Cell>> graph) { this.size = size; this.graph = graph; }
    public int size() { return size; }
    public Cell start() { return new Cell(size / 2, 0); }
    public Cell goal() { return new Cell(size / 2, size - 1); }
    public Map<Cell, Set<Cell>> graph() { return graph; }
    public Set<BlockPos> floor() { return Collections.unmodifiableSet(floor); }
    public List<Sleeve> sleeves() { return List.copyOf(sleeves); }
    public BlockPos node(Cell cell) { return new BlockPos((cell.x - size / 2) * 5, 0, -3 - cell.row * 5); }
    public int pathLength() { return distance(graph, start(), goal()); }
    public long deadEnds() { return graph.values().stream().filter(n -> n.size() == 1).count(); }
    public long junctions() { return graph.values().stream().filter(n -> n.size() >= 3).count(); }
    public int cycles() { return graph.values().stream().mapToInt(Set::size).sum() / 2 - graph.size() + 1; }

    public static int size(LabyrinthPlace place) {
        return switch (place) {
            case GRAY_CORRIDOR -> 5;
            case FOLDED_MAZE -> 7;
            case DEEP_MAZE -> 9;
            case ABYSS_MAZE -> 11;
            default -> throw new IllegalArgumentException("Not a maze: " + place);
        };
    }
    public static int pairs(LabyrinthPlace place) {
        return switch (place) {
            case FOLDED_MAZE -> 1;
            case DEEP_MAZE -> 2;
            case ABYSS_MAZE -> 3;
            default -> 0;
        };
    }

    public static MazeLayout create(LabyrinthPlace place, long seed) {
        int size = size(place);
        Random random = new Random(seed);
        Map<Cell, Set<Cell>> graph = null;
        Cell start = new Cell(size / 2, 0), goal = new Cell(size / 2, size - 1);
        for (int attempt = 0; attempt < 128; attempt++) {
            graph = tree(size, random, start);
            if (distance(graph, start, goal) >= size * 2 && degreeCount(graph, 1) >= 5) break;
        }
        int wanted = switch (place) { case GRAY_CORRIDOR -> 1; case FOLDED_MAZE -> 3; case DEEP_MAZE -> 6; default -> 10; };
        int added = 0;
        List<Cell> cells = new ArrayList<>(graph.keySet());
        Collections.shuffle(cells, random);
        for (Cell cell : cells) {
            for (Cell neighbor : neighbors(cell, size)) {
                if (added >= wanted || graph.get(cell).contains(neighbor)) continue;
                graph.get(cell).add(neighbor); graph.get(neighbor).add(cell);
                if (distance(graph, start, goal) < size * 2 || degreeCount(graph, 1) < 4) {
                    graph.get(cell).remove(neighbor); graph.get(neighbor).remove(cell);
                } else added++;
            }
        }
        Map<Cell, Set<Cell>> frozen = new LinkedHashMap<>();
        graph.forEach((cell, neighbors) -> frozen.put(cell, Collections.unmodifiableSet(new LinkedHashSet<>(neighbors))));
        MazeLayout layout = new MazeLayout(size, Collections.unmodifiableMap(frozen));
        for (Cell cell : cells) {
            layout.carve(layout.node(cell));
            for (Cell neighbor : graph.get(cell)) layout.line(layout.node(cell), layout.node(neighbor));
        }
        layout.line(layout.node(start), new BlockPos(0, 0, -1));
        BlockPos northDoor = place.doors().stream().filter(d -> !d.name().equals("entry") && d.facing() == net.minecraft.core.Direction.SOUTH)
                .findFirst().orElseThrow().rel();
        layout.line(layout.node(goal), northDoor.south());
        for (LabyrinthPlace.DoorSpec door : place.doors()) {
            if (door.facing() == net.minecraft.core.Direction.EAST || door.facing() == net.minecraft.core.Direction.WEST) {
                Cell edge = new Cell(door.rel().getX() < 0 ? 0 : size - 1, size / 2);
                layout.line(layout.node(edge), door.rel().relative(door.facing()));
            }
        }
        int pairs = pairs(place);
        if (pairs > 0) {
            int sleeveZ = layout.node(goal).getZ() - 16;
            for (int i = 0; i < pairs * 2; i++) {
                int x = (2 * i - (pairs * 2 - 1)) * 6;
                int turn = place == LabyrinthPlace.FOLDED_MAZE ? 0 : i % 2;
                Sleeve sleeve = new Sleeve(new BlockPos(x, 0, sleeveZ), turn);
                layout.sleeves.add(sleeve);
                layout.sleeve(sleeve);
            }
            // Connect the ends around the sleeves' protected middle. This cannot widen a crossing's sightline.
            int halfWidth = place.room().maxX() - 1;
            for (Sleeve sleeve : layout.sleeves) {
                for (BlockPos tip : List.of(new BlockPos(-6, 0, 4), new BlockPos(6, 0, -4))) {
                    BlockPos from = sleeve.block(tip);
                    int nodeX = Math.max(0, Math.min(size - 1, (int) Math.round(from.getX() / 5.0) + size / 2));
                    layout.connect(from, layout.node(new Cell(nodeX, size - 1)), halfWidth, northDoor.getZ() + 2);
                }
            }
        }
        layout.floor.removeIf(p -> p.getX() <= place.room().minX() || p.getX() >= place.room().maxX()
                || p.getZ() <= place.room().minZ() || p.getZ() >= 0);
        return layout;
    }

    private static Map<Cell, Set<Cell>> tree(int size, Random random, Cell start) {
        Map<Cell, Set<Cell>> graph = new LinkedHashMap<>();
        for (int row = 0; row < size; row++) for (int x = 0; x < size; x++) graph.put(new Cell(x, row), new LinkedHashSet<>());
        Set<Cell> seen = new LinkedHashSet<>();
        ArrayDeque<Cell> stack = new ArrayDeque<>();
        seen.add(start); stack.push(start);
        while (!stack.isEmpty()) {
            Cell cell = stack.peek();
            List<Cell> options = new ArrayList<>(neighbors(cell, size));
            options.removeIf(seen::contains);
            if (options.isEmpty()) { stack.pop(); continue; }
            Cell next = options.get(random.nextInt(options.size()));
            graph.get(cell).add(next); graph.get(next).add(cell);
            seen.add(next); stack.push(next);
        }
        return graph;
    }
    private static List<Cell> neighbors(Cell cell, int size) {
        List<Cell> result = new ArrayList<>();
        for (int[] delta : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            Cell n = new Cell(cell.x + delta[0], cell.row + delta[1]);
            if (n.x >= 0 && n.x < size && n.row >= 0 && n.row < size) result.add(n);
        }
        return result;
    }
    private static long degreeCount(Map<Cell, Set<Cell>> graph, int degree) {
        return graph.values().stream().filter(n -> n.size() == degree).count();
    }
    private static int distance(Map<Cell, Set<Cell>> graph, Cell start, Cell end) {
        Map<Cell, Integer> distances = new LinkedHashMap<>();
        ArrayDeque<Cell> queue = new ArrayDeque<>();
        distances.put(start, 0); queue.add(start);
        while (!queue.isEmpty()) {
            Cell cell = queue.remove();
            if (cell.equals(end)) return distances.get(cell);
            for (Cell n : graph.get(cell)) if (!distances.containsKey(n)) {
                distances.put(n, distances.get(cell) + 1); queue.add(n);
            }
        }
        return Integer.MAX_VALUE;
    }
    private void carve(BlockPos center) {
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) floor.add(center.offset(x, 0, z));
    }
    private void line(BlockPos from, BlockPos to) {
        if (from.getX() != to.getX() && from.getZ() != to.getZ()) throw new IllegalArgumentException("Diagonal corridor");
        int length = from.distManhattan(to);
        int dx = Integer.signum(to.getX() - from.getX()), dz = Integer.signum(to.getZ() - from.getZ());
        for (int i = 0; i <= length; i++) carve(from.offset(dx * i, 0, dz * i));
    }
    private void sleeve(Sleeve sleeve) {
        for (int z = -4; z <= 4; z++) carve(sleeve.block(new BlockPos(0, 0, z)));
        for (int x = -6; x <= 0; x++) carve(sleeve.block(new BlockPos(x, 0, 4)));
        for (int x = 0; x <= 6; x++) carve(sleeve.block(new BlockPos(x, 0, -4)));
    }
    private boolean reserved(BlockPos pos) {
        for (Sleeve sleeve : sleeves) {
            Vec3 local = sleeve.local(Vec3.atBottomCenterOf(pos));
            if (Math.abs(local.x) <= 4 && Math.abs(local.z) <= 4) return true;
        }
        return false;
    }
    private void connect(BlockPos from, BlockPos to, int halfWidth, int north) {
        Map<BlockPos, BlockPos> previous = new LinkedHashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        previous.put(from, from); queue.add(from);
        while (!queue.isEmpty() && !previous.containsKey(to)) {
            BlockPos pos = queue.remove();
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockPos n = pos.relative(direction);
                if (Math.abs(n.getX()) > halfWidth || n.getZ() < north || n.getZ() > node(goal()).getZ()
                        || reserved(n) || previous.containsKey(n)) continue;
                previous.put(n, pos); queue.add(n);
            }
        }
        if (!previous.containsKey(to)) throw new IllegalStateException("Unreachable fold sleeve tip " + from);
        for (BlockPos pos = to; !pos.equals(from); pos = previous.get(pos)) carve(pos);
        carve(from);
    }
    public static Vec3 rotate(Vec3 v, int turn) {
        return switch (Math.floorMod(turn, 4)) {
            case 1 -> new Vec3(-v.z, v.y, v.x);
            case 2 -> new Vec3(-v.x, v.y, -v.z);
            case 3 -> new Vec3(v.z, v.y, -v.x);
            default -> v;
        };
    }
    public static Vec3 fold(Sleeve from, Sleeve to, Vec3 point) { return to.world(from.local(point)); }
    public static boolean crosses(Sleeve sleeve, Vec3 previous, Vec3 current) {
        Vec3 a = sleeve.local(previous), b = sleeve.local(current);
        if (!((a.z >= 0 && b.z < 0) || (a.z < 0 && b.z >= 0)) || previous.distanceToSqr(current) > 256) return false;
        double t = -a.z / (b.z - a.z);
        Vec3 at = a.lerp(b, t);
        return Math.abs(at.x) < 1.15 && at.y >= -.1 && at.y < 2.1;
    }
}
