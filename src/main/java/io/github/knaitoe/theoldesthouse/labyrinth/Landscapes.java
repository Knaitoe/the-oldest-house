package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;

/**
 * 0.4.28 outdoor edges. The outside scenes are islands; their edges used to be
 * invisible walls or a flat curtain of leaves. Now the land itself closes
 * them: dunes stepped too steeply to climb, a ravine before the plain's
 * unapproachable figure, apartment facades around Zampano's courtyard, and a
 * mixed forest on the barn's bank. {@link OutdoorBounds} returns anyone who
 * gets past them anyway.
 */
public final class Landscapes {
    private static final int F = LabyrinthBuilder.flags();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private Landscapes() {}

    static void apply(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        switch (place) {
            case PLAIN -> plain(level, base);
            case ZAMPANO_COURTYARD -> courtyard(level, base);
            case BARN_WELL -> barnForest(level, base);
            default -> {}
        }
    }

    /** Smooth, continuous terrain variation in roughly [-1, 1]; no grids or stripes. */
    static double noise(int x, int z, int seed) {
        return (Math.sin(x * 0.131 + seed) * 0.35 + Math.sin(z * 0.113 + seed * 1.7) * 0.35
                + Math.sin((x + z) * 0.071 + seed * 0.3) * 0.2 + Math.sin((x - z) * 0.193 + seed * 2.1) * 0.1);
    }

    private static int hash(int x, int z, int seed) {
        return Math.floorMod((x * 73856093) ^ (z * 19349663) ^ (seed * 83492791), 1 << 20);
    }

    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState state) { l.setBlock(b.offset(x, y, z), state, F); }
    private static BlockState get(ServerLevel l, BlockPos b, int x, int y, int z) { return l.getBlockState(b.offset(x, y, z)); }

    private static void removeBarriers(ServerLevel l, BlockPos b, int x0, int x1, int y0, int y1, int z0, int z1) {
        for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++)
            if (get(l, b, x, y, z).is(Blocks.BARRIER)) set(l, b, x, y, z, AIR);
    }

    // ------------------------------------------------------------------
    // The plain

    /** The walkable core of the plain: everything past it is dune or ravine. */
    public static final int PLAIN_HALF_WIDTH = 22, PLAIN_FAR = -64;

    static void plain(ServerLevel l, BlockPos b) {
        removeBarriers(l, b, -31, 31, 0, 17, -66, 1);
        BlockState sand = Blocks.SAND.defaultBlockState(), stone = Blocks.SANDSTONE.defaultBlockState();
        for (int x = -46; x <= 46; x++) for (int z = -180; z <= 16; z++) {
            int ax = Math.abs(x);
            // Past the ravine the open ground runs on beyond the haze, so the figure's horizon has no edge.
            if (ax <= PLAIN_HALF_WIDTH && z < -72) {
                for (int y = -3; y <= -1; y++) set(l, b, x, y, z, y == -1 ? sand : stone);
                for (int y = 0; y <= 3; y++) if (dune(get(l, b, x, y, z)) && !(ax <= 1 && z == -93)) set(l, b, x, y, z, AIR);
                continue;
            }
            boolean core = ax <= PLAIN_HALF_WIDTH && z >= PLAIN_FAR && z <= 0;
            boolean shed = ax <= 8 && z >= 1 && z <= 10;
            if (core || shed) continue;
            // A ravine across the far end, before the figure's ground, as wide as the walkable core.
            if (ax <= PLAIN_HALF_WIDTH && z < PLAIN_FAR) {
                if (z >= -68) {
                    for (int y = -4; y <= 3; y++) set(l, b, x, y, z, AIR);
                    for (int y = -6; y <= -5; y++) set(l, b, x, y, z, y == -5 ? sand : stone);
                    if (hash(x, z, 5) % 9 == 0) set(l, b, x, -4, z, Blocks.DEAD_BUSH.defaultBlockState());
                } else {
                    for (int y = -6; y <= -1; y++) set(l, b, x, y, z, y == -1 ? sand : stone);
                    for (int y = 0; y <= 3; y++) if (dune(get(l, b, x, y, z))) set(l, b, x, y, z, AIR);
                }
                continue;
            }
            int d = Math.max(Math.max(ax - PLAIN_HALF_WIDTH, 0), ax <= 8 ? z - 10 : Math.max(z, 0));
            if (z < PLAIN_FAR) d = Math.max(d, 1);
            // Dunes climb to a crest and fall away again past it; every rise is two blocks or more.
            double rise = Math.min(d, 46 - ax + 6) * 0.9 + 1.5 + noise(x, z, 11) * 1.6;
            int h = Math.max(0, Math.min(12, (int) Math.round(rise)));
            h = (h / 2) * 2;
            for (int y = -3; y < h; y++) set(l, b, x, y, z, y == h - 1 ? sand : stone);
            for (int y = Math.max(h, -1); y <= 8; y++) {
                var st = get(l, b, x, y, z);
                if (y < h) continue;
                if (dune(st)) set(l, b, x, y, z, AIR);
            }
            if (h > 0 && hash(x, z, 3) % 23 == 0 && get(l, b, x, h, z).isAir()) set(l, b, x, h, z, Blocks.DEAD_BUSH.defaultBlockState());
        }
    }

    private static boolean dune(BlockState st) {
        return st.is(Blocks.SANDSTONE) || st.is(Blocks.SAND) || st.is(Blocks.SANDSTONE_SLAB) || st.is(Blocks.DEAD_BUSH)
                || st.is(Blocks.SMOOTH_SANDSTONE) || st.is(Blocks.BARRIER);
    }

    // ------------------------------------------------------------------
    // Zampano's courtyard

    static void courtyard(ServerLevel l, BlockPos b) {
        // The former invisible box becomes the facades of the surrounding blocks of flats.
        for (int x = -15; x <= 15; x++) for (int z = -41; z <= 0; z++) {
            boolean side = Math.abs(x) == 15, back = z == -41, front = z == 0;
            if (!side && !back && !front) continue;
            int u = side ? z : x;
            Direction out = side ? (x > 0 ? Direction.EAST : Direction.WEST) : back ? Direction.NORTH : Direction.SOUTH;
            for (int y = 0; y <= 17; y++) {
                var st = get(l, b, x, y, z);
                if (y == 17) {
                    if (st.isAir() && get(l, b, x, 16, z).is(Blocks.STONE_BRICKS)) set(l, b, x, 17, z, parapet(side ? Direction.Axis.Z : Direction.Axis.X));
                    continue;
                }
                if (!st.is(Blocks.BARRIER)) continue;
                set(l, b, x, y, z, facade(u, y, front && Math.abs(x) <= 8));
                if (window(u, y, front && Math.abs(x) <= 8)) {
                    int bx = x + out.getStepX(), bz = z + out.getStepZ();
                    if (get(l, b, bx, y, bz).isAir()) set(l, b, bx, y, bz, Blocks.BLACK_CONCRETE.defaultBlockState());
                }
            }
        }
        // The archive's flat top gets a stone cornice and a parapet where it meets the sky.
        for (int x = -12; x <= 12; x++) for (int z = -39; z <= -19; z++) {
            boolean edge = Math.abs(x) == 12 || z == -39 || z == -19;
            if (!edge || !get(l, b, x, 7, z).is(Blocks.BIRCH_PLANKS) || !get(l, b, x, 8, z).isAir()) continue;
            set(l, b, x, 7, z, Blocks.STONE_BRICKS.defaultBlockState());
            set(l, b, x, 8, z, parapet(Math.abs(x) == 12 ? Direction.Axis.Z : Direction.Axis.X));
        }
    }

    private static boolean window(int u, int y, boolean blindFront) {
        if (blindFront) return false;
        int row = y % 5;
        return Math.floorMod(u + 4, 5) == 0 && y < 15 && (row == 2 || row == 3);
    }

    private static BlockState facade(int u, int y, boolean blindFront) {
        if (y == 0) return Blocks.POLISHED_ANDESITE.defaultBlockState();
        if (y == 5 || y == 10 || y == 16) return Blocks.STONE_BRICKS.defaultBlockState();
        if (y == 15) return Blocks.SMOOTH_STONE.defaultBlockState();
        if (window(u, y, blindFront)) return Blocks.GLASS_PANE.defaultBlockState();
        // Lintels and sills frame each window; pilasters divide the bays.
        if (!blindFront && Math.floorMod(u + 4, 5) == 0 && (y % 5 == 1 || y % 5 == 4)) return Blocks.SMOOTH_STONE.defaultBlockState();
        if (Math.floorMod(u + 2, 5) == 0) return Blocks.STONE_BRICKS.defaultBlockState();
        return Blocks.BRICKS.defaultBlockState();
    }

    private static BlockState parapet(Direction.Axis axis) {
        BlockState wall = Blocks.STONE_BRICK_WALL.defaultBlockState();
        return axis == Direction.Axis.X
                ? wall.setValue(WallBlock.EAST_WALL, WallSide.LOW).setValue(WallBlock.WEST_WALL, WallSide.LOW)
                : wall.setValue(WallBlock.NORTH_WALL, WallSide.LOW).setValue(WallBlock.SOUTH_WALL, WallSide.LOW);
    }

    // ------------------------------------------------------------------
    // The barn's bank

    /** How far into the barn's earth bank a column lies (0 or less inside the farm). */
    public static int barnEdge(int x, int z) { return Math.max(Math.abs(x) - 15, Math.max(-z - 37, z + 1)); }

    /** Replaces the old leaf curtain and the evenly spaced spruces with a mixed wood on the same bank. */
    static void barnForest(ServerLevel l, BlockPos b) {
        for (int x = -23; x <= 23; x++) for (int z = -45; z <= 4; z++) {
            if (barnEdge(x, z) < 1 || (Math.abs(x) < 4 && z >= -1)) continue;
            for (int y = 0; y <= 12; y++) {
                var st = get(l, b, x, y, z);
                if (st.getBlock() instanceof LeavesBlock || st.is(Blocks.SPRUCE_LOG) || st.is(Blocks.OAK_LOG) || st.is(Blocks.BIRCH_LOG)
                        || st.is(Blocks.SHORT_GRASS) || st.is(Blocks.FERN)) set(l, b, x, y, z, AIR);
            }
        }
        // The well's cube-crowned spruces inside the farm become real trees where they stood.
        for (int z = -4; z >= -33; z -= 6) {
            for (int x = -14; x <= -12; x++) for (int dz = -1; dz <= 1; dz++) for (int y = 0; y <= 8; y++) {
                var st = get(l, b, x, y, z + dz);
                if (st.getBlock() instanceof LeavesBlock || st.is(Blocks.SPRUCE_LOG)) set(l, b, x, y, z + dz, AIR);
            }
            int seed = hash(-13, z, 23);
            tree(l, b, -13, z, seed % 3, 5 + seed % 3, (seed >> 2) % 3 - 1, 0);
        }
        // Two loose rows: smaller trees at the foot of the bank, taller ones behind them.
        for (int side : new int[]{-1, 1}) {
            for (int z = 1; z > -44; ) {
                int seed = hash(z, side, 17);
                tree(l, b, side * (17 + seed % 3), z, seed % 4, 4 + seed % 4, (seed >> 3) % 3 - 1, 0);
                tree(l, b, side * (21 + (seed >> 2) % 2), z - 2, (seed >> 5) % 3, 7 + (seed >> 4) % 4, 0, (seed >> 6) % 3 - 1);
                z -= 4 + seed % 4;
            }
        }
        for (int x = -20; x <= 20; ) {
            int seed = hash(x, 0, 29);
            tree(l, b, x, -39 - seed % 2, seed % 4, 5 + seed % 4, (seed >> 3) % 3 - 1, 0);
            tree(l, b, x + 2, -43, (seed >> 5) % 3, 8 + (seed >> 4) % 3, 0, 0);
            x += 5 + seed % 4;
        }
        // Undergrowth on the bank's open ground.
        for (int x = -23; x <= 23; x++) for (int z = -45; z <= 4; z++) {
            if (barnEdge(x, z) < 1 || (Math.abs(x) < 4 && z >= -1) || hash(x, z, 41) % 5 != 0) continue;
            int ground = ground(l, b, x, z);
            if (ground == Integer.MIN_VALUE || !get(l, b, x, ground + 1, z).isAir()) continue;
            set(l, b, x, ground + 1, z, (hash(x, z, 43) % 3 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState());
        }
    }

    private static int ground(ServerLevel l, BlockPos b, int x, int z) {
        for (int y = 10; y >= -3; y--) {
            var s = get(l, b, x, y, z);
            if (s.is(Blocks.PODZOL) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.GRASS_BLOCK)) return y;
            if (!s.isAir()) return Integer.MIN_VALUE;
        }
        return Integer.MIN_VALUE;
    }

    /** One tree: spruce, oak, birch or a dead snag, with its own height and lean, into open air only. */
    static void tree(ServerLevel l, BlockPos b, int x, int z, int type, int height, int leanX, int leanZ) {
        int ground = ground(l, b, x, z);
        if (ground == Integer.MIN_VALUE) return;
        Block log = type == 2 ? Blocks.BIRCH_LOG : type == 1 ? Blocks.OAK_LOG : Blocks.SPRUCE_LOG;
        Block leaves = type == 2 ? Blocks.BIRCH_LEAVES : type == 1 ? Blocks.OAK_LEAVES : Blocks.SPRUCE_LEAVES;
        for (int y = 1; y <= height; y++) {
            int dx = y > height / 2 ? leanX : 0, dz = y > height / 2 ? leanZ : 0;
            if (get(l, b, x + dx, ground + y, z + dz).isAir()) set(l, b, x + dx, ground + y, z + dz, log.defaultBlockState());
        }
        if (type == 3) {
            if (get(l, b, x + 1, ground + height - 1, z).isAir()) set(l, b, x + 1, ground + height - 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            return;
        }
        int low = type == 0 ? height / 2 : height - 3;
        for (int y = low; y <= height + 1; y++) {
            int radius = type == 0 ? Math.max(0, (height + 1 - y) / 2) : y == height + 1 ? 1 : 2 + (type == 1 && y == height - 1 ? 1 : 0);
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + 1 || Math.floorMod(dx * 31 + dz * 17 + y * 7 + x, 11) == 0) continue;
                int px = x + leanX + dx, pz = z + leanZ + dz, py = ground + y;
                if (get(l, b, px, py, pz).isAir())
                    set(l, b, px, py, pz, leaves.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            }
        }
    }
}
