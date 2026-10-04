package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelRooms;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

/**
 * A report-only polish inspection of every built scene. It never fails a test:
 * it writes build/vignette-audit.txt, which CI publishes as annotations, listing
 * blocks that cannot survive where they stand, split doors, detached block
 * clusters, broken hanging entities, floating or buried creatures, dark floor,
 * sparse detail and outdoor edges that fall away into the void.
 */
public final class VignetteAudit {
    private VignetteAudit() {}

    private static final int SAMPLES = 4;

    private static String id(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
    }

    private static boolean airLike(BlockState state) {
        return state.isAir() || state.is(Blocks.LIGHT) || state.is(Blocks.BARRIER) || state.is(Blocks.STRUCTURE_VOID);
    }

    private static String rel(BlockPos base, BlockPos at) {
        return (at.getX() - base.getX()) + "," + (at.getY() - base.getY()) + "," + (at.getZ() - base.getZ());
    }

    private static final class Tally {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        final Map<String, List<String>> where = new HashMap<>();

        void add(String key, String at) {
            counts.merge(key, 1, Integer::sum);
            List<String> list = where.computeIfAbsent(key, k -> new ArrayList<>());
            if (list.size() < SAMPLES) list.add(at);
        }

        boolean isEmpty() {
            return counts.isEmpty();
        }

        String render() {
            StringBuilder out = new StringBuilder();
            counts.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(8).forEach(e ->
                    out.append(' ').append(e.getKey()).append('×').append(e.getValue()).append(" @").append(String.join(" ", where.get(e.getKey()))).append(';'));
            return out.toString();
        }
    }

    public static void write(MinecraftServer server, BlockPos origin) {
        StringBuilder report = new StringBuilder();
        report.append("Vignette audit (rel coords x,y,z from each scene base)\n");
        for (LabyrinthPlace scene : LabyrinthPlace.values()) {
            if (scene.room() == null || scene.slot() < 0 || scene == LabyrinthPlace.FAMILY_COPY || scene == LabyrinthPlace.OLD_CABIN) continue;
            try {
                BlockPos base = LabyrinthPlaces.base(origin, scene);
                if (base == null) continue;
                ServerLevel level = server.getLevel(NovelRooms.dimension(scene));
                if (level == null) continue;
                report.append(scene(level, base, scene));
            } catch (Exception e) {
                report.append("## ").append(scene.id()).append(": audit failed ").append(e).append('\n');
            }
        }
        try {
            Path folder = Path.of("../build");
            Files.createDirectories(folder);
            Files.writeString(folder.resolve("vignette-audit.txt"), report.toString());
        } catch (Exception ignored) {
        }
    }

    private static String scene(ServerLevel level, BlockPos base, LabyrinthPlace scene) {
        var r = scene.room();
        int minX = r.minX(), minY = r.minY(), minZ = r.minZ(), maxX = r.maxX(), maxY = r.maxY(), maxZ = r.maxZ();
        int sx = maxX - minX + 1, sy = maxY - minY + 1, sz = maxZ - minZ + 1;
        boolean outdoor = NovelRooms.outside(scene);
        BitSet solid = new BitSet(sx * sy * sz);
        Tally unsupported = new Tally(), doors = new Tally();
        List<BlockPos> emitters = new ArrayList<>();
        List<Integer> emission = new ArrayList<>();
        List<BlockPos> floor = new ArrayList<>();
        Map<String, Integer> materials = new HashMap<>();
        int detail = 0;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = 0; x < sx; x++) for (int y = 0; y < sy; y++) for (int z = 0; z < sz; z++) {
            at.set(base.getX() + minX + x, base.getY() + minY + y, base.getZ() + minZ + z);
            BlockState state = level.getBlockState(at);
            if (airLike(state)) continue;
            if (state.getLightEmission() > 0) {
                emitters.add(at.immutable());
                emission.add(state.getLightEmission());
            }
            if (!level.getFluidState(at).isEmpty() && state.getCollisionShape(level, at).isEmpty()) continue;
            solid.set((x * sy + y) * sz + z);
            BlockPos here = at.immutable();
            if (!state.canSurvive(level, here)) unsupported.add(id(state), rel(base, here));
            if (state.getBlock() instanceof DoorBlock) {
                boolean lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                BlockState other = level.getBlockState(lower ? here.above() : here.below());
                if (!other.is(state.getBlock()) || other.getValue(DoorBlock.HALF) == state.getValue(DoorBlock.HALF))
                    doors.add((lower ? "lower-without-upper:" : "upper-without-lower:") + id(state), rel(base, here));
            }
            boolean full = state.isCollisionShapeFullBlock(level, here);
            if (!full || state.hasBlockEntity()) detail++;
            else materials.merge(id(state), 1, Integer::sum);
            // A walkable floor cell is the clear space above a sturdy top face.
            if (y + 2 < sy && state.isFaceSturdy(level, here, Direction.UP)) {
                BlockPos up = here.above();
                BlockState a = level.getBlockState(up), b = level.getBlockState(up.above());
                if (a.getCollisionShape(level, up).isEmpty() && b.getCollisionShape(level, up.above()).isEmpty()
                        && level.getFluidState(up).isEmpty()) floor.add(up);
            }
        }

        // Detached clusters: everything but the largest connected structure, if small.
        Tally floating = new Tally();
        BitSet seen = new BitSet(solid.size());
        List<int[]> components = new ArrayList<>();
        int[] queue = new int[Math.max(16, solid.cardinality())];
        List<List<Integer>> members = new ArrayList<>();
        for (int i = solid.nextSetBit(0); i >= 0; i = solid.nextSetBit(i + 1)) {
            if (seen.get(i)) continue;
            int head = 0, tail = 0;
            queue[tail++] = i;
            seen.set(i);
            List<Integer> mine = new ArrayList<>();
            while (head < tail) {
                int c = queue[head++];
                if (mine.size() < 400) mine.add(c);
                int z = c % sz, y = (c / sz) % sy, x = c / (sz * sy);
                int[][] n = {{x + 1, y, z}, {x - 1, y, z}, {x, y + 1, z}, {x, y - 1, z}, {x, y, z + 1}, {x, y, z - 1}};
                for (int[] p : n) {
                    if (p[0] < 0 || p[1] < 0 || p[2] < 0 || p[0] >= sx || p[1] >= sy || p[2] >= sz) continue;
                    int k = (p[0] * sy + p[1]) * sz + p[2];
                    if (solid.get(k) && !seen.get(k)) {
                        seen.set(k);
                        queue[tail++] = k;
                    }
                }
            }
            components.add(new int[]{tail, members.size()});
            members.add(mine);
        }
        int largest = components.stream().mapToInt(c -> c[0]).max().orElse(0);
        int detached = 0;
        for (int[] c : components) {
            if (c[0] == largest || c[0] > 300) continue;
            detached++;
            int first = members.get(c[1]).get(0);
            int z = first % sz, y = (first / sz) % sy, x = first / (sz * sy);
            BlockPos p = base.offset(minX + x, minY + y, minZ + z);
            Map<String, Integer> kinds = new LinkedHashMap<>();
            for (int m : members.get(c[1])) {
                int mz = m % sz, my = (m / sz) % sy, mx = m / (sz * sy);
                kinds.merge(id(level.getBlockState(base.offset(minX + mx, minY + my, minZ + mz))), 1, Integer::sum);
            }
            floating.add(c[0] + "blk[" + String.join("+", kinds.keySet().stream().limit(3).toList()) + "]", rel(base, p));
        }

        // Entities in the scene.
        Tally entities = new Tally();
        Map<String, Integer> census = new LinkedHashMap<>();
        AABB box = new AABB(base.getX() + minX, base.getY() + minY, base.getZ() + minZ,
                base.getX() + maxX + 1, base.getY() + maxY + 1, base.getZ() + maxZ + 1);
        for (Entity e : level.getEntitiesOfClass(Entity.class, box)) {
            String type = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath();
            census.merge(type, 1, Integer::sum);
            BlockPos p = e.blockPosition();
            if (e instanceof HangingEntity hanging && !hanging.survives()) entities.add("unsupported-" + type, rel(base, p));
            if (e instanceof LivingEntity living) {
                if (living.isInWall()) entities.add("buried-" + type, rel(base, p));
                boolean grounded = !level.noCollision(e, e.getBoundingBox().move(0, -0.3, 0)) || e.isInWater() || e.isPassenger();
                if (!grounded) entities.add((e.isNoGravity() ? "hovering-nogravity-" : "falling-") + type, rel(base, p));
            }
        }

        // Block light over the floor, propagated through the actual blocks (sky ignored).
        int darkPercent = (int) Math.round(100 * io.github.knaitoe.theoldesthouse.labyrinth.ScenePolish.darkFraction(level, base, scene));
        // Outdoor edges: perimeter columns whose ground ends at the void just past the edge.
        int voidEdges = 0, perimeter = 0, holes = 0;
        if (outdoor) {
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                boolean any = false;
                for (int y = minY; y <= maxY && !any; y++) any = !airLike(level.getBlockState(base.offset(x, y, z)));
                if (!any) holes++;
                if (!edge) continue;
                perimeter++;
                int ox = x == minX ? x - 1 : x == maxX ? x + 1 : x, oz = z == minZ ? z - 1 : z == maxZ ? z + 1 : z;
                boolean beyond = false;
                for (int y = minY - 8; y <= maxY && !beyond; y++) beyond = !airLike(level.getBlockState(base.offset(ox, y, oz)));
                if (any && !beyond) voidEdges++;
            }
        }

        StringBuilder out = new StringBuilder();
        out.append("## ").append(scene.id()).append(outdoor ? " [outdoor " : " [").append(sx).append('x').append(sy).append('x').append(sz).append("] ");
        out.append("floor=").append(floor.size()).append(" detail/floor=").append(floor.isEmpty() ? "-" : String.format("%.2f", detail / (double) floor.size()));
        out.append(" materials=").append(materials.size()).append(" lights=").append(emitters.size());
        if (!outdoor) out.append(" darkFloor=").append(floor.isEmpty() ? "-" : darkPercent + "%");
        if (outdoor) out.append(" voidEdge=").append(voidEdges).append('/').append(perimeter).append(" voidColumns=").append(holes);
        out.append('\n');
        if (!unsupported.isEmpty()) out.append("  unsupported:").append(unsupported.render()).append('\n');
        if (!doors.isEmpty()) out.append("  doors:").append(doors.render()).append('\n');
        if (detached > 0) out.append("  detached(").append(detached).append("):").append(floating.render()).append('\n');
        if (!entities.isEmpty()) out.append("  entities:").append(entities.render()).append('\n');
        if (!census.isEmpty()) out.append("  census: ").append(census).append('\n');
        return out.toString();
    }
}
