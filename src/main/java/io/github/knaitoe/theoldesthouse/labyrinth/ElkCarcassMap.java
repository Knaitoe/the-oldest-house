package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * The elk carcasses (0.4.50), after "My Heart Is a Chainsaw": two stages on one shore.
 *
 * <p>The reader comes through the door into a guest cabin of a yacht moored stern-to under a
 * granite bluff, the morning after its party. The passengers are dead in their cabins, the
 * saloon and on the decks; the man who killed them is still aboard. The only way off is the
 * water. Across the lake the second stage is a stream valley running south-west to north-east
 * between two wedges of woods, with the road crew's half-cut site in the open ground east of
 * them. On the outer, north-western edge of the larger wood, under a rocky knoll, is a cave. The
 * crew hid their elk carcasses there, and someone has hidden the crew among them. A hollow under
 * the pile is the one place to lie still while the boots go by. A footpath leaves the cave along
 * the foot of the ridge to the crew's service gate.
 *
 * <p>Everything here is relative to the place's base, with the entry door at (0, 0, +1). The
 * ground is a deterministic function of position, so the story logic can stand the killer on it
 * without reading the world.
 */
public final class ElkCarcassMap {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public static final int MIN_X = -64, MAX_X = 64, MIN_Z = -250;
    /** The scene owns its ground this far past its room box, so no generic border digs a ditch at its edge. */
    static final int SKIRT_X = 72, SKIRT_SOUTH = 21, SKIRT_NORTH = -258;
    // Decks, in stand levels (the y a player's feet are at).
    public static final int LOWER = 0, MAIN = 4, SUN = 9;
    // The cave: mouth, chamber floor, and the hollow under the pile.
    public static final int MOUTH_Y = 8, CAVE_Y = 5, GATE_Y = 11, SITE_TOP = 2;
    public static final BlockPos MOUTH = new BlockPos(-11, MOUTH_Y, -179), CHAMBER = new BlockPos(-21, CAVE_Y, -189);
    public static final BlockPos CAVE_ENTRY = new BlockPos(-15, CAVE_Y, -185), PASS_WEST = new BlockPos(-28, CAVE_Y, -187),
            PASS_EAST = new BlockPos(-15, CAVE_Y, -187), PEER = new BlockPos(-23, CAVE_Y, -188), CAVE_EXIT = new BlockPos(8, 0, -202);
    /** The crawl space beneath the carcasses: x -24..-22, z -192..-191, one block high. */
    public static final int HOLLOW_X0 = -24, HOLLOW_X1 = -22, HOLLOW_Z0 = -192, HOLLOW_Z1 = -191;
    /** The gap at the front of the pile the hollow is watched through (and crawled into). */
    public static final int GAP_X0 = -23, GAP_X1 = -22, GAP_Z = -190;
    public static final BlockPos SERVICE_DOOR = new BlockPos(44, GATE_Y, -247), ENDING = new BlockPos(41, GATE_Y + 1, -245),
            SOURCE = new BlockPos(-4, 0, -6);
    // The yacht: where the killer waits, where he climbs out, where he comes ashore.
    public static final BlockPos KILLER_START = new BlockPos(0, LOWER, -29), HATCH = new BlockPos(0, 3, -34),
            HATCH_DECK = new BlockPos(0, MAIN, -32), LANDING_WATER = new BlockPos(30, -2, -60), LANDING = new BlockPos(34, 0, -72);
    /** Where each murdered guest lies, so a reader's discoveries can be counted. */
    public static final List<BlockPos> GUESTS = List.of(new BlockPos(7, 1, -3), new BlockPos(-5, 0, -10), new BlockPos(-5, 0, -17),
            new BlockPos(3, 0, -16), new BlockPos(0, 0, -28), new BlockPos(-3, MAIN, -2), new BlockPos(0, MAIN, -6),
            new BlockPos(5, MAIN, -12), new BlockPos(1, MAIN, -13), new BlockPos(2, MAIN, -16), new BlockPos(0, MAIN, -24),
            new BlockPos(0, MAIN + 1, -30), new BlockPos(3, SUN + 1, 12));
    /** A search through the open valley and both woods; y is filled in from the ground. */
    private static final int[][] PATROL = {{34, -74}, {22, -96}, {8, -112}, {-18, -104}, {-44, -124}, {-34, -150}, {-14, -160},
            {6, -184}, {24, -196}, {46, -186}, {50, -146}, {40, -112}, {16, -134}, {-2, -146}};

    private ElkCarcassMap() {}

    // ------------------------------------------------------------------
    // The ground

    private static double smooth(double e0, double e1, double x) {
        double t = Math.max(0, Math.min(1, (x - e0) / (e1 - e0)));
        return t * t * (3 - 2 * t);
    }

    /** Below one: the lake. A rounded basin around the yacht. */
    public static double lake(double x, double z) {
        double m = Math.pow(Math.abs(x) / 46.0, 2.5) + Math.pow(Math.abs(z + 22) / 46.0, 2.5);
        return m * (1 + .06 * Math.sin(x * .13) + .05 * Math.cos(z * .11));
    }

    private static final double SX = -27, SZ = -63, EX = 55, EZ = -252;
    private static final double SLEN = Math.hypot(EX - SX, EZ - SZ), DX = (EX - SX) / SLEN, DZ = (EZ - SZ) / SLEN;

    /** Distance along the stream from its mouth. */
    public static double along(double x, double z) {
        return (x - SX) * DX + (z - SZ) * DZ;
    }

    /** Signed distance from the stream's meandering line: positive is the west bank. */
    public static double side(double x, double z) {
        double t = along(x, z);
        return (x - SX) * DZ - (z - SZ) * DX - (2.5 * Math.sin(t * .06) + 1.2 * Math.sin(t * .17));
    }

    /** The stream's water surface: five steps down from the spring to the lake. */
    public static int streamLevel(double t) {
        return -1 + (int) Math.floor(5 * Math.max(0, Math.min(1, t / SLEN)) + .5);
    }

    public static boolean stream(int x, int z) {
        double t = along(x, z);
        // It rises from a spring against the valley head, inside the scene, so no water ever runs off its edge.
        return z >= -251 && t > -4 && t < SLEN + 12 && Math.abs(side(x, z)) <= (t < 18 ? 2.2 : 1.6);
    }

    private static final double AX = -60.8, AZ = -133, BX = 40.5, BZ = -234;
    private static final double RLEN = Math.hypot(BX - AX, BZ - AZ), UX = (BX - AX) / RLEN, UZ = (BZ - AZ) / RLEN;

    /** Signed distance outward (north-west) from the larger wood's outer edge. */
    static double ridgeDistance(double x, double z) {
        return (x - AX) * UZ - (z - AZ) * UX;
    }

    static double ridgeAlong(double x, double z) {
        return ((x - AX) * UX + (z - AZ) * UZ) / RLEN;
    }

    private static final double[][] WEST_WOOD = {{40.5, -234}, {-60.8, -133}, {-32.9, -84}}, EAST_WOOD = {{49.5, -225}, {-6.8, -89}, {21, -68}};

    private static double edge(double px, double pz, double[] a, double[] b) {
        return (px - b[0]) * (a[1] - b[1]) - (a[0] - b[0]) * (pz - b[1]);
    }

    private static boolean inside(double[][] t, double x, double z) {
        double d1 = edge(x, z, t[0], t[1]), d2 = edge(x, z, t[1], t[2]), d3 = edge(x, z, t[2], t[0]);
        boolean neg = d1 < 0 || d2 < 0 || d3 < 0, pos = d1 > 0 || d2 > 0 || d3 > 0;
        return !(neg && pos);
    }

    /** One of the two wedges of woods either side of the stream. */
    public static boolean wooded(double x, double z) {
        return inside(WEST_WOOD, x, z) || inside(EAST_WOOD, x, z);
    }

    private static double raw(double x, double z) {
        double s = Math.max(0, Math.min(1, (-z - 60) / 190.0));
        double h = -1 + 7 * Math.pow(s, 1.1) + 1.2 * Math.sin(x * .09 + z * .03) + .8 * Math.cos(z * .07 - x * .05);
        h += 6 * smooth(54, 66, Math.abs(x)) + 7 * smooth(-240, -254, z);
        double r = ridgeDistance(x, z), t = ridgeAlong(x, z);
        double g = Math.exp(-Math.pow((r - 7) / 6.5, 2)) * smooth(-.05, .12, t) * smooth(1.08, .9, t);
        h += (12 + 3 * Math.exp(-Math.pow((t - .47) / .12, 2))) * g + 1.5 * Math.sin(x * .4 + z * .3) * g;
        h += 6 * Math.exp(-(Math.pow(x + 21, 2) + Math.pow(z + 189, 2)) / 144.0); // the knoll over the cave
        double st = along(x, z), sd = Math.abs(side(x, z));
        if (st > -5 && st < SLEN + 20 && sd < 7) h = Math.min(h, Math.max(h - 2.5 * (1 - sd / 7), streamLevel(st) + 1));
        double m = lake(x, z);
        if (m < 1.3 && z > -80) h = Math.max(-1, -1 + (h + 1) * smooth(1.0, 1.3, m));
        // Level pads where people built: the crew's site, their service gate and the cave mouth.
        h = pad(h, x, z, 49, -142, 13, 20, SITE_TOP, 4);
        h = pad(h, x, z, 44, -245, 6, 4, GATE_Y - 1, 3);
        h = pad(h, x, z, MOUTH.getX() + 1, MOUTH.getZ() + 1, 3.5, 3.5, MOUTH_Y - 1, 2);
        return h;
    }

    private static double pad(double h, double x, double z, double cx, double cz, double rx, double rz, double level, double blend) {
        double d = Math.max(Math.abs(x - cx) - rx, Math.abs(z - cz) - rz);
        if (d >= blend) return h;
        return d <= 0 ? level : level + (h - level) * smooth(0, blend, d);
    }

    private static final int W = SKIRT_X * 2 + 1, D = SKIRT_SOUTH - SKIRT_NORTH + 1;
    private static int[] heights;

    /** The top solid block of the ground (before the cave and buildings are cut into it). */
    public static int surface(int x, int z) {
        if (heights == null) {
            int[] h = new int[W * D];
            for (int zz = SKIRT_NORTH; zz <= SKIRT_SOUTH; zz++)
                for (int xx = -SKIRT_X; xx <= SKIRT_X; xx++) h[(zz - SKIRT_NORTH) * W + xx + SKIRT_X] = (int) Math.floor(raw(xx, zz) + .5);
            outcrop(h);
            heights = h;
        }
        x = Math.max(-SKIRT_X, Math.min(SKIRT_X, x));
        z = Math.max(SKIRT_NORTH, Math.min(SKIRT_SOUTH, z));
        return heights[(z - SKIRT_NORTH) * W + x + SKIRT_X];
    }

    /** Where a walker stands: one above the ground, or on the stream bed, or on the lake bed. */
    public static int stand(int x, int z) {
        if (lakeWater(x, z)) return bed(x, z) + 1;
        if (stream(x, z)) return streamLevel(along(x, z));
        return surface(x, z) + 1;
    }

    public static BlockPos standAt(int x, int z) {
        return new BlockPos(x, stand(x, z), z);
    }

    static boolean lakeWater(int x, int z) {
        return z <= 18 && lake(x, z) < 1;
    }

    static int bed(int x, int z) {
        return -9 + (int) Math.round(6 * Math.max(0, Math.min(1, (lake(x, z) - .55) / .45)));
    }

    /** The knoll's rock face above the cave passage and around its mouth, and enough rock over the chamber. */
    private static void outcrop(int[] h) {
        double nx = -0.706, nz = -0.708; // into the ridge
        for (int z = -200; z <= -170; z++)
            for (int x = -34; x <= -4; x++) {
                int i = (z - SKIRT_NORTH) * W + x + SKIRT_X;
                double ax = x - MOUTH.getX() - .5, az = z - MOUTH.getZ() - .5;
                double in = ax * nx + az * nz, across = Math.abs(ax * nz - az * nx);
                if (in > .5 && in < 12 && across < 3.5 + in * .3) h[i] = Math.max(h[i], MOUTH_Y + 3 + (int) (in * .6));
                double e = chamber(x, z);
                if (e < 1.25) h[i] = Math.max(h[i], ceiling(e) + 3);
            }
    }

    /** Below one: inside the cave chamber. */
    static double chamber(double x, double z) {
        return Math.pow((x - CHAMBER.getX()) / 9.0, 2) + Math.pow((z - CHAMBER.getZ()) / 6.0, 2) + .06 * Math.sin(x * 1.3 + z * .7);
    }

    static int ceiling(double e) {
        return CAVE_Y + 2 + (int) Math.round(3 * Math.sqrt(Math.max(0, 1 - e)));
    }

    public static List<BlockPos> patrol() {
        var out = new ArrayList<BlockPos>();
        for (int[] p : PATROL) out.add(standAt(p[0], p[1]));
        return out;
    }

    // ------------------------------------------------------------------
    // Where the reader is

    public static boolean aboard(Vec3 rel) {
        double z = rel.z;
        if (z > 18.5 || z < -42.5 || rel.y < -1.5) return false;
        return Math.abs(rel.x) <= halfBeam((int) Math.floor(z)) + .7;
    }

    public static int halfBeam(int z) {
        if (z > 18 || z < -42) return -1;
        if (z >= -24) return 9;
        return Math.max(1, (int) Math.round(9 * Math.sqrt((z + 42) / 17.0)));
    }

    public static boolean inHollow(Vec3 rel) {
        return rel.x >= HOLLOW_X0 && rel.x < HOLLOW_X1 + 1 && rel.z >= HOLLOW_Z0 && rel.z < HOLLOW_Z1 + 1
                && rel.y > CAVE_Y - .5 && rel.y < CAVE_Y + .6;
    }

    /** The hollow, its gap and the floor just before it, where a reader gets down to crawl. */
    public static boolean crawlZone(Vec3 rel) {
        return rel.x >= HOLLOW_X0 && rel.x < HOLLOW_X1 + 1 && rel.z >= HOLLOW_Z0 && rel.z < GAP_Z + 2
                && rel.y > CAVE_Y - .5 && rel.y < CAVE_Y + 1.2;
    }

    public static boolean inCave(Vec3 rel) {
        return chamber(rel.x, rel.z) < 1.1 && rel.y > CAVE_Y - 1 && rel.y < CAVE_Y + 6
                || passage(rel.x, rel.z) && rel.y > CAVE_Y - 1 && rel.y < MOUTH_Y + 4;
    }

    static boolean passage(double x, double z) {
        double ax = x - MOUTH.getX() - .5, az = z - MOUTH.getZ() - .5, nx = -0.706, nz = -0.708;
        double in = ax * nx + az * nz, across = Math.abs(ax * nz - az * nx);
        return in > -1 && in < 9 && across < 1.6;
    }

    public static boolean nearEnding(Vec3 rel) {
        return rel.distanceToSqr(Vec3.atBottomCenterOf(ENDING)) < 49;
    }

    // ------------------------------------------------------------------
    // Construction

    public static void build(ServerLevel l, BlockPos b) {
        new Builder(l, b).run();
    }

    private static final class Builder {
        final ServerLevel l;
        final BlockPos b;
        final Random random = new Random(0x0450E1CL);

        Builder(ServerLevel l, BlockPos b) {
            this.l = l;
            this.b = b;
        }

        BlockState st(Block block) {
            BlockState s = block.defaultBlockState();
            return block instanceof LeavesBlock ? s.setValue(LeavesBlock.PERSISTENT, true) : s;
        }

        void set(int x, int y, int z, BlockState s) {
            if (vestibule(x, y, z)) return;
            BuildBlocks.set(l, b.offset(x, y, z), s, F);
        }

        void set(int x, int y, int z, Block block) {
            set(x, y, z, st(block));
        }

        void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState s) {
            if (x0 > x1 || y0 > y1 || z0 > z1) return;
            BuildBlocks.box(l, b.offset(x0, y0, z0), b.offset(x1, y1, z1), s, F);
        }

        void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
            fill(x0, y0, z0, x1, y1, z1, st(block));
        }

        static boolean vestibule(int x, int y, int z) {
            return Math.abs(x) <= 7 && y >= -1 && y <= 7 && z >= 1 && z <= 17;
        }

        long hash(int x, int z) {
            long h = x * 73856093L ^ z * 19349663L ^ 0x5DEECE66DL;
            h ^= h >>> 17;
            h *= 0xED5AD4BBL;
            return h ^ h >>> 11;
        }

        double noise(int x, int z) {
            return (hash(x, z) & 1023) / 1023.0;
        }

        void prop(int x, int y, int z, LiteraryPropBlock.Kind kind, Direction facing, int stage) {
            set(x, y, z, LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND, kind)
                    .setValue(LiteraryPropBlock.FACING, facing).setValue(LiteraryPropBlock.STAGE, stage));
        }

        void run() {
            // The skirt beyond the room box: clear whatever an older scene left standing there.
            fill(MAX_X + 1, -9, SKIRT_NORTH, SKIRT_X, 24, SKIRT_SOUTH, Blocks.AIR);
            fill(-SKIRT_X, -9, SKIRT_NORTH, MIN_X - 1, 24, SKIRT_SOUTH, Blocks.AIR);
            fill(MIN_X, -9, SKIRT_NORTH, MAX_X, 24, MIN_Z - 1, Blocks.AIR);
            fill(MIN_X, -9, 1, MAX_X, 24, SKIRT_SOUTH, Blocks.AIR);
            ground();
            lakeAndStream();
            woods();
            bluff();
            site();
            trails();
            gate();
            cave();
            yacht();
        }

        // --------------------------------------------------------------
        // Land, water and growth

        boolean rocky(int x, int z) {
            double r = ridgeDistance(x, z), t = ridgeAlong(x, z);
            boolean ridge = t > .02 && t < 1.04 && r > 1.5 && r < 13;
            return ridge && noise(x, z) < .75 || chamber(x, z) < 2.2 && noise(x, z) < .85;
        }

        BlockState topsoil(int x, int z, int h) {
            double n = noise(x, z);
            double m = lake(x, z);
            if (m < 1.12 && z > -80) return st(n < .6 ? Blocks.SAND : n < .85 ? Blocks.GRAVEL : Blocks.COARSE_DIRT);
            if (rocky(x, z)) return st(n < .3 ? Blocks.STONE : n < .5 ? Blocks.ANDESITE : n < .62 ? Blocks.COBBLESTONE
                    : n < .7 ? Blocks.MOSSY_COBBLESTONE : n < .78 ? Blocks.TUFF : Blocks.COARSE_DIRT);
            if (wooded(x, z)) return st(n < .45 ? Blocks.PODZOL : n < .6 ? Blocks.COARSE_DIRT : n < .72 ? Blocks.MOSS_BLOCK : Blocks.GRASS_BLOCK);
            if (Math.abs(side(x, z)) < 3.2 && along(x, z) > -4) return st(n < .5 ? Blocks.MUD : n < .8 ? Blocks.COARSE_DIRT : Blocks.GRASS_BLOCK);
            return st(n < .07 ? Blocks.COARSE_DIRT : Blocks.GRASS_BLOCK);
        }

        void ground() {
            for (int z = SKIRT_NORTH; z <= SKIRT_SOUTH; z++)
                for (int x = -SKIRT_X; x <= SKIRT_X; x++) {
                    if (z >= 19) continue; // the bluff
                    int h = surface(x, z);
                    boolean rock = rocky(x, z);
                    if (lakeWater(x, z)) {
                        int bed = bed(x, z);
                        fill(x, -9, z, x, bed - 1, z, Blocks.STONE);
                        double n = noise(x, z);
                        set(x, bed, z, n < .45 ? Blocks.CLAY : n < .7 ? Blocks.GRAVEL : n < .9 ? Blocks.SAND : Blocks.MUD);
                        continue;
                    }
                    // Rock only as deep as any face of it lies open: to the lowest neighbouring
                    // ground, stream bed or lake bed, and down past the cave under the knoll.
                    int open = h;
                    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) open = Math.min(open, exposed(x + dx, z + dz));
                    int bottom = Math.max(-9, Math.min(h - 4, open - 1));
                    if (underKnoll(x, z)) bottom = Math.min(bottom, CAVE_Y - 3);
                    fill(x, bottom, z, x, h - 3, z, Blocks.STONE);
                    fill(x, Math.max(bottom, h - 2), z, x, h - 1, z, rock ? Blocks.STONE : Blocks.DIRT);
                    set(x, h, z, topsoil(x, z, h));
                }
        }

        /** The lowest level open to air or water in a column, before buildings and the cave are cut. */
        int exposed(int x, int z) {
            if (z >= 19) return Integer.MAX_VALUE; // the bluff is built whole
            if (lakeWater(x, z)) return bed(x, z);
            if (stream(x, z)) return streamLevel(along(x, z)) - 1;
            return surface(x, z);
        }

        /** The knoll's rock around the cave chamber, and under the passage from the mouth. */
        boolean underKnoll(int x, int z) {
            if (chamber(x, z) < 2.4) return true;
            double ax = x - MOUTH.getX() - .5, az = z - MOUTH.getZ() - .5;
            double in = ax * -0.706 + az * -0.708, across = Math.abs(ax * -0.708 - az * -0.706);
            return in > -3 && in < 14 && across < 6 + in * .3;
        }

        void lakeAndStream() {
            for (int z = SKIRT_NORTH; z <= 18; z++)
                for (int x = -SKIRT_X; x <= SKIRT_X; x++) {
                    if (lakeWater(x, z)) {
                        fill(x, bed(x, z) + 1, z, x, -1, z, Blocks.WATER);
                        if (noise(x, z) < .05 && bed(x, z) < -3) set(x, bed(x, z) + 1, z, Blocks.SEAGRASS);
                        continue;
                    }
                    if (!stream(x, z)) continue;
                    int w = streamLevel(along(x, z)), h = surface(x, z);
                    set(x, w - 1, z, noise(x, z) < .6 ? Blocks.GRAVEL : Blocks.CLAY);
                    set(x, w, z, Blocks.WATER);
                    fill(x, w + 1, z, x, Math.max(w + 1, h), z, Blocks.AIR);
                }
        }

        boolean clearing(int x, int z) {
            if (lake(x, z) < 1.35 && z > -90) return true;
            if (Math.abs(side(x, z)) < 4 && along(x, z) > -4) return true;
            if (Math.abs(x - 49) < 16 && Math.abs(z + 142) < 23) return true;
            if (Math.abs(x - 44) < 9 && Math.abs(z + 245) < 7) return true;
            if (Math.abs(x - MOUTH.getX()) < 6 && Math.abs(z - MOUTH.getZ()) < 6) return true;
            if (chamber(x, z) < 1.6) return true;
            if (onTrail(x, z, 2.5) || onPath(x, z, 2) || onRoad(x, z, 2.5)) return true;
            return rocky(x, z);
        }

        void woods() {
            // Trees on a jittered grid: thick inside the two wedges, a few strays at their edges,
            // and dense thicket along the scene's own boundary so it ends in woods, not a wall.
            for (int gz = SKIRT_NORTH; gz <= 14; gz += 4)
                for (int gx = -SKIRT_X; gx <= SKIRT_X; gx += 4) {
                    long h = hash(gx, gz);
                    int x = gx + (int) (h & 3), z = gz + (int) (h >>> 2 & 3);
                    if (Math.abs(x) > SKIRT_X || z < SKIRT_NORTH) continue;
                    double n = (h >>> 8 & 1023) / 1023.0;
                    boolean border = Math.abs(x) >= 58 || z <= -243 && Math.abs(x - 44) > 12;
                    boolean wood = wooded(x, z), bank = !border && !wood && z > -95 && lake(x, z) > 1.45;
                    double chance = border ? .95 : wood ? .82 : bank ? .55 : wooded(x + 4, z) || wooded(x - 4, z) || wooded(x, z + 4) || wooded(x, z - 4) ? .22 : .035;
                    if (n > chance || clearing(x, z) && !border || lakeWater(x, z) || stream(x, z) || Builder.vestibule(x, 0, z)) continue;
                    if (z >= 17 || Math.abs(x) <= 12 && z > -46) continue; // nothing over the yacht or the bluff
                    // Beyond where anyone can walk, the undergrowth wall and the polish's border close the view.
                    if (Math.abs(x) > MAX_X + 2 || z < MIN_Z - 2) continue;
                    int y = surface(x, z) + 1;
                    double kind = (h >>> 20 & 1023) / 1023.0;
                    if (kind < .62) spruce(x, y, z, 8 + (int) (h >>> 32 & 7), h);
                    else if (kind < .84) birch(x, y, z, 6 + (int) (h >>> 32 & 3), h);
                    else if (kind < .92 && wood) snag(x, y, z, 5 + (int) (h >>> 32 & 3));
                    else bigSpruce(x, y, z, 13 + (int) (h >>> 32 & 5), h);
                }
            // Undergrowth and fallen timber.
            for (int z = SKIRT_NORTH; z <= 16; z++)
                for (int x = -SKIRT_X; x <= SKIRT_X; x++) {
                    if (lakeWater(x, z) || stream(x, z) || Builder.vestibule(x, 0, z) || onTrail(x, z, 1.4) || onPath(x, z, 1.1) || onRoad(x, z, 1.6)) continue;
                    if (Math.abs(x - 49) < 14 && Math.abs(z + 142) < 21 || Math.abs(x - 44) < 7 && Math.abs(z + 246) < 5) continue;
                    if (chamber(x, z) < 1.3 || passage(x, z)) continue;
                    int y = surface(x, z) + 1;
                    double n = noise(x * 7 + 3, z * 5 - 1);
                    boolean border = Math.abs(x) >= 61 || z <= -246 && Math.abs(x - 44) > 10;
                    if (border && n < .7) { fill(x, y, z, x, y + 2, z, Blocks.SPRUCE_LEAVES); continue; }
                    if (rocky(x, z)) {
                        if (n < .08) set(x, y, z, Blocks.SHORT_GRASS);
                        else if (n > .985) boulder(x, y, z);
                        continue;
                    }
                    if (wooded(x, z)) {
                        if (n < .17) set(x, y, z, Blocks.FERN);
                        else if (n < .21) tall(x, y, z, Blocks.LARGE_FERN);
                        else if (n < .27) set(x, y, z, Blocks.MOSS_CARPET);
                        else if (n < .28) set(x, y, z, Blocks.BROWN_MUSHROOM);
                        else if (n < .285) set(x, y, z, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 1));
                        else if (n > .9975) log(x, y, z, hash(x, z));
                        continue;
                    }
                    if (lake(x, z) < 1.12 && z > -80) { if (n < .05) set(x, y, z, Blocks.DEAD_BUSH); continue; }
                    if (n < .33) set(x, y, z, Blocks.SHORT_GRASS);
                    else if (n < .37) tall(x, y, z, Blocks.TALL_GRASS);
                    else if (n < .385) set(x, y, z, n < .377 ? Blocks.OXEYE_DAISY : Blocks.CORNFLOWER);
                    else if (n < .39) set(x, y, z, Blocks.DANDELION);
                    else if (n > .996) boulder(x, y, z);
                }
        }

        void tall(int x, int y, int z, Block block) {
            set(x, y, z, block.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
            set(x, y + 1, z, block.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        }

        void spruce(int x, int y, int z, int height, long h) {
            fill(x, y, z, x, y + height - 1, z, Blocks.SPRUCE_LOG);
            BlockState leaves = st(Blocks.SPRUCE_LEAVES);
            for (int k = 0; k <= height - 2; k++) {
                int yy = y + height - k;
                int r = Math.min(3, (k + 1) / 2 - (k % 2 == 1 && k > 2 ? 1 : 0));
                for (int dx = -r; dx <= r; dx++)
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx == 0 && dz == 0 && k > 0) continue;
                        int d = Math.abs(dx) + Math.abs(dz);
                        if (d > r + (r > 1 ? 1 : 0) || d == r + 1 && (hash(x + dx * 3, z + dz * 5 + k) & 3) == 0) continue;
                        set(x + dx, yy, z + dz, leaves);
                    }
            }
            set(x, y + height, z, leaves);
        }

        void bigSpruce(int x, int y, int z, int height, long h) {
            fill(x, y, z, x + 1, y + height - 1, z + 1, Blocks.SPRUCE_LOG);
            BlockState leaves = st(Blocks.SPRUCE_LEAVES);
            for (int k = 0; k <= height - 4; k++) {
                int yy = y + height + 1 - k;
                int r = Math.min(4, 1 + k / 2 - (k % 3 == 2 ? 1 : 0));
                for (int dx = -r; dx <= r + 1; dx++)
                    for (int dz = -r; dz <= r + 1; dz++) {
                        double cx = dx - .5, cz = dz - .5;
                        if (cx * cx + cz * cz > (r + .6) * (r + .6)) continue;
                        if ((dx == 0 || dx == 1) && (dz == 0 || dz == 1) && yy < y + height) continue;
                        set(x + dx, yy, z + dz, leaves);
                    }
            }
        }

        void birch(int x, int y, int z, int height, long h) {
            fill(x, y, z, x, y + height - 1, z, Blocks.BIRCH_LOG);
            BlockState leaves = st(Blocks.BIRCH_LEAVES);
            for (int yy = y + height - 3; yy <= y + height; yy++) {
                int r = yy >= y + height - 1 ? 1 : 2;
                for (int dx = -r; dx <= r; dx++)
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) == r && Math.abs(dz) == r && (hash(x + dx, z + dz + yy) & 1) == 0) continue;
                        if (dx == 0 && dz == 0 && yy < y + height) continue;
                        set(x + dx, yy, z + dz, leaves);
                    }
            }
        }

        void snag(int x, int y, int z, int height) {
            fill(x, y, z, x, y + height - 1, z, Blocks.SPRUCE_LOG);
            set(x, y + height, z, Blocks.STRIPPED_SPRUCE_LOG);
        }

        void log(int x, int y, int z, long h) {
            boolean alongX = (h & 1) == 0;
            int length = 4 + (int) (h >>> 3 & 3);
            for (int i = 0; i < length; i++) {
                int xx = alongX ? x + i : x, zz = alongX ? z : z + i;
                if (surface(xx, zz) + 1 != y || stream(xx, zz) || onTrail(xx, zz, 1.5) || onPath(xx, zz, 1.2)) break;
                set(xx, y, zz, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, alongX ? Direction.Axis.X : Direction.Axis.Z));
                if ((hash(xx, zz) & 3) == 0) set(xx, y + 1, zz, Blocks.MOSS_CARPET);
            }
        }

        void boulder(int x, int y, int z) {
            long h = hash(x, z);
            Block[] rocks = {Blocks.STONE, Blocks.ANDESITE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE};
            set(x, y, z, rocks[(int) (h & 3)]);
            if ((h & 4) != 0) set(x + 1, y, z, rocks[(int) (h >>> 3 & 3)]);
            if ((h & 8) != 0) set(x, y, z + 1, rocks[(int) (h >>> 5 & 3)]);
            if ((h & 16) != 0) set(x, y + 1, z, Blocks.MOSSY_COBBLESTONE);
        }

        /** The granite bluff the yacht is moored stern-to under: it closes the lake's south shore. */
        void bluff() {
            for (int z = 19; z <= SKIRT_SOUTH; z++)
                for (int x = -SKIRT_X; x <= SKIRT_X; x++) {
                    int top = 12 + (int) (4 * noise(x / 3, z)) + (z - 19) * 2;
                    for (int y = -9; y <= top; y++) {
                        double n = noise(x * 3 + y, z * 7 - y * 2);
                        set(x, y, z, n < .45 ? Blocks.STONE : n < .65 ? Blocks.ANDESITE : n < .75 ? Blocks.TUFF : n < .85 ? Blocks.COBBLESTONE : Blocks.MOSSY_COBBLESTONE);
                    }
                    if (z == 19 && noise(x, 99) < .3)
                        for (int y = top - 1; y > top - 5 - (int) (noise(x, 7) * 6); y--) set(x, y, z - 1, Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true));
                    set(x, top + 1, z, noise(x, z * 3) < .55 ? Blocks.SPRUCE_LEAVES : Blocks.GRASS_BLOCK);
                }
        }

        // --------------------------------------------------------------
        // The crew's site, their trails and the gate

        static final int[][] TRAIL = {{38, -142}, {31, -149}, {21, -156}, {12, -161}, {2, -167}, {-6, -174}, {-10, -178}};
        static final int[][] PATH = {{-10, -180}, {-5, -189}, {5, -199}, {15, -209}, {25, -219}, {34, -229}, {40, -237}, {43, -244}};
        static final int[][] ROAD = {{50, -160}, {53, -178}, {56, -198}, {57, -218}, {56, -233}, {50, -240}, {45, -244}};

        static double polyline(int[][] p, double x, double z) {
            double best = 1e9;
            for (int i = 0; i + 1 < p.length; i++) {
                double ax = p[i][0], az = p[i][1], bx = p[i + 1][0], bz = p[i + 1][1];
                double vx = bx - ax, vz = bz - az, t = Math.max(0, Math.min(1, ((x - ax) * vx + (z - az) * vz) / (vx * vx + vz * vz)));
                best = Math.min(best, Math.hypot(x - ax - t * vx, z - az - t * vz));
            }
            return best;
        }

        static boolean onTrail(int x, int z, double width) {
            return polyline(TRAIL, x, z) <= width;
        }

        static boolean onPath(int x, int z, double width) {
            return polyline(PATH, x, z) <= width;
        }

        static boolean onRoad(int x, int z, double width) {
            return polyline(ROAD, x, z) <= width;
        }

        void trails() {
            for (int z = -250; z <= -130; z++)
                for (int x = -20; x <= 62; x++) {
                    if (lakeWater(x, z)) continue;
                    int h = surface(x, z);
                    if (stream(x, z)) {
                        // Flat stepping stones where the trail and the path ford the stream; a plank bridge for the road.
                        int w = streamLevel(along(x, z));
                        if (onTrail(x, z, 1.2) && (x + z & 1) == 0) set(x, w, z, Blocks.MOSSY_COBBLESTONE);
                        if (onRoad(x, z, 1.6)) { set(x, Math.max(w + 2, h), z, Blocks.SPRUCE_PLANKS); }
                        continue;
                    }
                    if (onTrail(x, z, 1.1) && !passage(x, z) && !(x >= 39 && x <= 45 && z >= -145 && z <= -139)) {
                        set(x, h, z, LiteraryRegistry.DRAG_MUD.get().defaultBlockState());
                        if ((hash(x, z) & 31) == 0) prop(x, h + 1, z, LiteraryPropBlock.Kind.STAIN, Direction.from2DDataValue((int) (hash(x, z) >>> 6 & 3)), 1);
                    } else if (onPath(x, z, .8)) set(x, h, z, Blocks.DIRT_PATH);
                    else if (onRoad(x, z, 1.6)) set(x, h, z, (hash(x, z) & 3) == 0 ? Blocks.COARSE_DIRT : Blocks.GRAVEL);
                    else continue;
                    if (Math.abs(x - 49) < 14 && Math.abs(z + 142) < 21) continue; // the site's own things stand on it
                    // Nothing growing on the walked ground.
                    set(x, h + 1, z, Blocks.AIR);
                    set(x, h + 2, z, Blocks.AIR);
                }
            prop(2, surface(2, -167) + 1, -166, LiteraryPropBlock.Kind.HARD_HAT, Direction.WEST, 1);
            prop(-7, surface(-7, -176) + 1, -174, LiteraryPropBlock.Kind.STAIN, Direction.NORTH, 3);
        }

        void site() {
            int y = SITE_TOP + 1;
            // The graded pad.
            for (int z = -162; z <= -122; z++)
                for (int x = 36; x <= 62; x++) {
                    if (Math.abs(x - 49) > 13 || Math.abs(z + 142) > 20) continue;
                    double n = noise(x, z);
                    set(x, SITE_TOP, z, n < .5 ? Blocks.COARSE_DIRT : n < .8 ? Blocks.GRAVEL : Blocks.DIRT);
                    fill(x, y, z, x, y + 3, z, Blocks.AIR);
                }
            // The site trailer, door ajar, steps, and a window with the blind half down.
            fill(52, y, -154, 59, y + 3, -149, LiteraryRegistry.SITE_SIDING.get().defaultBlockState());
            fill(53, y, -153, 58, y + 2, -150, Blocks.AIR);
            fill(52, y - 1, -154, 59, y - 1, -149, Blocks.SPRUCE_PLANKS);
            fill(52, y + 4, -154, 59, y + 4, -149, Blocks.LIGHT_GRAY_CONCRETE);
            NovelRooms.door(l, b.offset(52, y, -151), Direction.WEST, Blocks.IRON_DOOR, true);
            set(51, y - 1, -151, Blocks.SPRUCE_SLAB.defaultBlockState());
            for (int z : new int[]{-153, -150}) set(59, y + 1, z, Blocks.GLASS_PANE);
            set(55, y + 1, -154, Blocks.GLASS_PANE);
            NovelRooms.furniture(l, b.offset(57, y, -152), HouseholdFurnitureBlock.Kind.WALNUT_DESK, Direction.WEST);
            NovelRooms.furniture(l, b.offset(56, y, -152), HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.EAST);
            set(54, y, -153, Blocks.BARREL);
            prop(55, y, -150, LiteraryPropBlock.Kind.HARD_HAT, Direction.NORTH, 0);
            NovelRooms.sign(l, b.offset(51, y + 2, -153), Direction.WEST, new String[]{"ROAD CREW", "Elk to the cut", "below the ridge.", "Sign in at gate"});
            // A flatbed with what the crew were hauling off the road, and the drag marks leaving it.
            fill(40, y, -145, 44, y, -139, Blocks.BLACK_CONCRETE);
            fill(39, y + 1, -145, 45, y + 1, -139, Blocks.SPRUCE_PLANKS);
            for (int x : new int[]{39, 45}) fill(x, y + 2, -145, x, y + 2, -139, Blocks.SPRUCE_FENCE);
            prop(41, y + 2, -143, LiteraryPropBlock.Kind.CARCASS, Direction.SOUTH, 0);
            prop(43, y + 2, -141, LiteraryPropBlock.Kind.CARCASS, Direction.NORTH, 3);
            prop(39, y, -141, LiteraryPropBlock.Kind.STAIN, Direction.WEST, 1);
            // Culverts waiting by the trench, stacked lumber, the safety fence along the cut.
            for (int x = 44; x <= 50; x += 3) {
                for (int dy = 0; dy < 2; dy++) for (int dz = 0; dz < 2; dz++) set(x, y + dy, -126 - dz * 2, Blocks.LIGHT_GRAY_CONCRETE);
                set(x + 1, y, -126, Blocks.LIGHT_GRAY_CONCRETE);
            }
            fill(55, y, -134, 59, y + 1, -132, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            // The trench is two deep with an earth step at each end, so nobody is kept in it.
            fill(40, SITE_TOP - 1, -131, 52, SITE_TOP, -129, Blocks.AIR);
            fill(40, SITE_TOP - 2, -131, 52, SITE_TOP - 2, -129, Blocks.MUD);
            for (int x : new int[]{40, 52}) fill(x, SITE_TOP - 1, -131, x, SITE_TOP - 1, -129, Blocks.DIRT);
            for (int x = 39; x <= 53; x++) for (int z : new int[]{-132, -128}) set(x, y, z, LiteraryRegistry.SAFETY_FENCE.get().defaultBlockState());
            for (int z = -131; z <= -129; z++) for (int x : new int[]{39, 53}) set(x, y, z, LiteraryRegistry.SAFETY_FENCE.get().defaultBlockState());
            // A small skid steer, parked where it stopped, bucket down.
            fill(46, y, -158, 48, y, -156, Blocks.BLACK_CONCRETE);
            fill(46, y + 1, -158, 48, y + 2, -157, Blocks.YELLOW_CONCRETE);
            set(47, y + 1, -156, Blocks.AIR);
            set(47, y + 2, -156, Blocks.BLACK_STAINED_GLASS_PANE);
            fill(45, y, -159, 49, y, -159, Blocks.YELLOW_TERRACOTTA);
            // A portable toilet, door shut.
            fill(60, y, -138, 60, y + 2, -138, Blocks.BLUE_CONCRETE);
            set(59, y, -138, Blocks.BLUE_TERRACOTTA);
            set(60, y + 3, -138, Blocks.WHITE_CONCRETE);
            lamp(50, y, -136);
            lamp(42, y, -152);
        }

        void lamp(int x, int y, int z) {
            fill(x, y, z, x, y + 3, z, Blocks.SPRUCE_FENCE);
            set(x, y + 4, z, Blocks.SPRUCE_FENCE);
            set(x + 1, y + 4, z, Blocks.SPRUCE_FENCE);
            set(x + 1, y + 3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        /** The crew's service gate across the road at the valley head: the House's other door. */
        void gate() {
            int g = GATE_Y - 1;
            for (int x = 30; x <= 58; x++) {
                if (stream(x, -247) || Math.abs(x - SERVICE_DOOR.getX()) <= 1) continue;
                int top = Math.max(surface(x, -247), g);
                set(x, top + 1, -247, LiteraryRegistry.SAFETY_FENCE.get().defaultBlockState());
                if (x % 4 == 0) fill(x, top + 1, -247, x, top + 2, -247, Blocks.SPRUCE_FENCE);
            }
            int x = SERVICE_DOOR.getX(), z = SERVICE_DOOR.getZ();
            fill(x - 1, GATE_Y, z, x - 1, GATE_Y + 2, z, Blocks.STRIPPED_SPRUCE_LOG);
            fill(x + 1, GATE_Y, z, x + 1, GATE_Y + 2, z, Blocks.STRIPPED_SPRUCE_LOG);
            set(x, GATE_Y + 2, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            set(x - 2, GATE_Y + 2, z, Blocks.SPRUCE_SLAB);
            set(x + 2, GATE_Y + 2, z, Blocks.SPRUCE_SLAB);
            // Clear ground through and beyond the door.
            fill(x - 1, g, z - 3, x + 1, g, z + 2, Blocks.GRAVEL);
            for (int zz = z - 3; zz <= z + 2; zz++) if (zz != z) fill(x - 1, GATE_Y, zz, x + 1, GATE_Y + 3, zz, Blocks.AIR);
            fill(x, GATE_Y, z, x, GATE_Y + 1, z, Blocks.AIR);
            NovelRooms.sign(l, b.offset(x + 1, GATE_Y + 1, z + 1), Direction.SOUTH, new String[]{"SERVICE ROAD", "Crew only", "Sign out here", ""});
            set(ENDING.getX(), GATE_Y, ENDING.getZ(), Blocks.BARREL);
            set(ENDING.getX() + 1, GATE_Y, ENDING.getZ(), Blocks.BARREL);
            lamp(x + 3, GATE_Y, z + 3);
        }

        // --------------------------------------------------------------
        // The cave

        void cave() {
            Block[] walls = {Blocks.STONE, Blocks.ANDESITE, Blocks.TUFF, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.STONE, Blocks.DEEPSLATE};
            // Chamber.
            for (int z = CHAMBER.getZ() - 8; z <= CHAMBER.getZ() + 8; z++)
                for (int x = CHAMBER.getX() - 11; x <= CHAMBER.getX() + 11; x++) {
                    double e = chamber(x, z);
                    if (e < 1.5) {
                        int top = surface(x, z);
                        for (int y = CAVE_Y - 2; y <= Math.min(top - 1, ceiling(Math.min(e, 1)) + 2); y++)
                            set(x, y, z, walls[(int) (hash(x * 5 + y, z * 3 - y) % walls.length + walls.length) % walls.length]);
                    }
                    if (e >= 1) continue;
                    double n = noise(x, z);
                    set(x, CAVE_Y - 1, z, n < .4 ? Blocks.GRAVEL : n < .65 ? Blocks.COARSE_DIRT : n < .8 ? Blocks.TUFF
                            : LiteraryRegistry.DRAG_MUD.get());
                    fill(x, CAVE_Y, z, x, ceiling(e), z, Blocks.AIR);
                }
            // Passage from the mouth, three wide, stepping down into the chamber.
            double nx = -0.706, nz = -0.708;
            for (int z = MOUTH.getZ() - 12; z <= MOUTH.getZ() + 2; z++)
                for (int x = MOUTH.getX() - 12; x <= MOUTH.getX() + 2; x++) {
                    if (!passage(x, z)) continue;
                    double ax = x - MOUTH.getX() - .5, az = z - MOUTH.getZ() - .5, in = ax * nx + az * nz;
                    int floor = MOUTH_Y - (int) Math.max(0, Math.min(3, Math.floor((in - 1.5) / 2)));
                    set(x, floor - 1, z, noise(x, z) < .5 ? LiteraryRegistry.DRAG_MUD.get() : Blocks.GRAVEL);
                    fill(x, floor, z, x, floor + 2 + (in > 4 ? 1 : 0), z, Blocks.AIR);
                    if (in > 0) set(x, floor + 3 + (in > 4 ? 1 : 0), z, walls[(int) (hash(x, z) & 3)]);
                }
            // A fallen spruce half across the mouth and growth at its foot: it is found, not shown.
            int mx = MOUTH.getX(), mz = MOUTH.getZ();
            for (int i = 0; i < 6 && surface(mx + 3 + i, mz + 2 - i / 3) == MOUTH_Y - 1; i++)
                set(mx + 3 + i, MOUTH_Y, mz + 2 - i / 3, Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            set(mx + 3, MOUTH_Y + 1, mz + 2, Blocks.MOSS_CARPET);
            for (int[] f : new int[][]{{3, 0}, {2, 3}, {-2, 3}, {4, -2}, {1, 4}}) tall(mx + f[0], MOUTH_Y, mz + f[1], Blocks.LARGE_FERN);
            set(mx + 4, MOUTH_Y, mz + 1, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 2));
            for (int y = MOUTH_Y + 2; y >= MOUTH_Y + 1; y--) set(mx - 1, y, mz - 1, Blocks.HANGING_ROOTS);
            // A crack to the surface over the pile lets a little morning in.
            int sx = -20, sz = -190;
            for (int y = ceiling(chamber(sx, sz)); y <= surface(sx, sz) + 1; y++) set(sx, y, sz, Blocks.AIR);
            set(sx + 1, ceiling(chamber(sx, sz)), sz, Blocks.HANGING_ROOTS);
            for (int[] d : new int[][]{{-26, -184}, {-17, -192}, {-13, -188}, {-28, -190}})
                if (chamber(d[0], d[1]) < .9) set(d[0], ceiling(chamber(d[0], d[1])), d[1], Blocks.POINTED_DRIPSTONE.defaultBlockState()
                        .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN));
            pile();
        }

        /** The carcasses and the road crew under them, and the hollow a reader can lie in. */
        void pile() {
            int y = CAVE_Y;
            var C = LiteraryPropBlock.Kind.CARCASS;
            // Roof over the hollow and its gap.
            for (int x = -25; x <= -21; x++) for (int z = -193; z <= -190; z++) prop(x, y + 1, z, C, Direction.from2DDataValue((x * 3 + z) & 3), (x + z & 1) == 0 ? 0 : 1);
            // Crest of the mound.
            for (int x = -24; x <= -22; x++) for (int z = -193; z <= -191; z++) if ((x + z & 3) != 3) prop(x, y + 2, z, C, Direction.from2DDataValue((x + z * 3) & 3), x == -23 ? 2 : 3);
            prop(-23, y + 3, -192, C, Direction.EAST, 0);
            // Flanks of the mound at ground level, and the hollow's sides.
            for (int z = -193; z <= -190; z++) {
                prop(-25, y, z, C, Direction.NORTH, z == -190 ? 3 : 1);
                prop(-21, y, z, C, Direction.SOUTH, z == -191 ? 2 : 0);
                prop(-26, y, z, C, Direction.WEST, 0);
                prop(-20, y, z, C, Direction.EAST, 3);
                if (z <= -191) { prop(-26, y + 1, z, C, Direction.EAST, 1); prop(-20, y + 1, z, C, Direction.WEST, 0); }
            }
            prop(-24, y, -190, C, Direction.WEST, 3); // left of the gap
            prop(-23, y, -193, C, Direction.SOUTH, 3);
            // The pile is heaped against the chamber's back wall: nothing gets round behind it.
            for (int x = -27; x <= -19; x++) for (int z = -196; z <= -194; z++) fill(x, y - 1, z, x, y + 2, z, (x + z & 1) == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.STONE);
            // The hollow and its gap stay open; the crew lie along its back.
            for (int x = HOLLOW_X0; x <= HOLLOW_X1; x++) for (int z = HOLLOW_Z0; z <= HOLLOW_Z1; z++) set(x, y, z, Blocks.AIR);
            for (int x = GAP_X0; x <= GAP_X1; x++) set(x, y, GAP_Z, Blocks.AIR);
            prop(-24, y, -193, LiteraryPropBlock.Kind.CREW_BODY, Direction.EAST, 0);
            prop(-22, y, -193, LiteraryPropBlock.Kind.CREW_BODY, Direction.WEST, 3);
            prop(-27, y, -190, LiteraryPropBlock.Kind.CREW_BODY, Direction.SOUTH, 1);
            prop(-19, y, -191, LiteraryPropBlock.Kind.CREW_BODY, Direction.NORTH, 0);
            // What fell out of the pile, and what the crew dropped.
            prop(-17, y, -188, C, Direction.SOUTH, 2);
            prop(-27, y, -186, C, Direction.EAST, 3);
            prop(-25, y, -188, LiteraryPropBlock.Kind.HARD_HAT, Direction.SOUTH, 2);
            prop(-19, y, -186, LiteraryPropBlock.Kind.HARD_HAT, Direction.WEST, 3);
            for (int[] s : new int[][]{{-22, -188, 0}, {-18, -186, 1}, {-15, -185, 1}, {-24, -187, 2}, {-20, -189, 3}})
                prop(s[0], y, s[1], LiteraryPropBlock.Kind.STAIN, Direction.from2DDataValue(s[0] & 3), s[2]);
            // The crew's lantern, knocked over, still lit.
            set(-16, y, -190, Blocks.LANTERN);
        }

        // --------------------------------------------------------------
        // The yacht

        BlockState shell(int y) {
            return (y <= -2 ? LiteraryRegistry.YACHT_ANTIFOUL : y == -1 ? LiteraryRegistry.YACHT_BOOT : y == 0 ? LiteraryRegistry.YACHT_STRIPE
                    : LiteraryRegistry.YACHT_HULL).get().defaultBlockState();
        }

        int keel(int x, int z) {
            int bottom = -6 + Math.min(3, x * x / 27);
            if (z < -30) bottom += (-30 - z) / 4;
            return Math.min(-3, bottom);
        }

        boolean inHull(int x, int z) {
            int hw = halfBeam(z);
            return hw >= 0 && Math.abs(x) <= hw;
        }

        boolean outer(int x, int z) {
            return !inHull(x + 1, z) || !inHull(x - 1, z) || !inHull(x, z + 1) || !inHull(x, z - 1);
        }

        void yacht() {
            BlockState fillHull = LiteraryRegistry.YACHT_HULL.get().defaultBlockState(), teak = LiteraryRegistry.YACHT_TEAK.get().defaultBlockState();
            // Hull, solid to the gunwale; the deckhouse aft encloses the arrival behind the cabin door.
            for (int z = -42; z <= 18; z++)
                for (int x = -9; x <= 9; x++) {
                    if (!inHull(x, z)) continue;
                    int top = z >= 1 ? 7 : 3;
                    for (int y = keel(x, z); y <= top; y++) {
                        if (Builder.vestibule(x, y, z)) continue;
                        boolean skin = outer(x, z) || y == keel(x, z);
                        BlockState s = skin ? (z >= 1 && y >= 4 && y <= 6 && Math.abs(x) == 9 ? LiteraryRegistry.YACHT_WINDOW.get().defaultBlockState()
                                : z >= 1 && y == 7 ? fillHull : shell(y)) : fillHull;
                        if (z >= 1 && Math.abs(x) == 8 && y >= 4 && y <= 6) s = Blocks.BLACK_CONCRETE.defaultBlockState();
                        set(x, y, z, s);
                    }
                }
            // Decks: the main deck at y 3, the sun deck at y 8 over the deckhouse, the aft deck and the saloon.
            for (int z = -42; z <= 0; z++) for (int x = -9; x <= 9; x++) if (inHull(x, z) && !outer(x, z)) set(x, 3, z, teak);
            fill(-9, 8, -19, 9, 8, 17, teak);
            fill(-9, 8, 18, 9, 8, 18, fillHull);
            lowerDeck();
            mainDeck();
            sunDeck();
            rails();
            // Anchor chain over the bow, and a boarding ladder amidships to port.
            for (int y = -8; y <= 3; y++) set(0, y, -43, Blocks.CHAIN.defaultBlockState());
            set(0, 4, -41, Blocks.IRON_BLOCK);
            for (int y = -1; y <= 3; y++) set(-10, y, -12, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
            set(-9, 4, -12, Blocks.AIR);
            prop(-8, 5, -14, LiteraryPropBlock.Kind.LIFE_RING, Direction.WEST, 0); // on the saloon wall over the side deck
            NovelRooms.sign(l, b.offset(-10, 2, -2), Direction.WEST, new String[]{"", "SECOND SUMMER", "Indian Lake", ""});
        }

        void cabinWalls(int x0, int x1, int z0, int z1) {
            BlockState panel = LiteraryRegistry.YACHT_PANEL.get().defaultBlockState();
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) if (x == x0 || x == x1 || z == z0 || z == z1)
                if (inHull(x, z) && !outer(x, z)) fill(x, 0, z, x, 2, z, panel);
        }

        void room(int x0, int x1, int z0, int z1, Block floor) {
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) {
                if (!inHull(x, z) || outer(x, z)) continue;
                set(x, -1, z, floor);
                fill(x, 0, z, x, 2, z, Blocks.AIR);
            }
        }

        void porthole(int x, int z) {
            set(x, 1, z, LiteraryRegistry.YACHT_PORTHOLE.get().defaultBlockState());
        }

        void hang(int x, int y, int z) {
            set(x, y, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        void mattress(int x0, int x1, int z0, int z1) {
            fill(x0, 0, z0, x1, 0, z1, LiteraryRegistry.YACHT_CUSHION.get());
        }

        void lowerDeck() {
            Block carpet = LiteraryRegistry.YACHT_CARPET.get();
            var G = LiteraryPropBlock.Kind.GUEST_BODY;
            // The guest cabin the reader wakes in, behind the door they came through.
            room(-8, 8, -6, 0, carpet);
            cabinWalls(-8, 8, -7, -7);
            NovelRooms.door(l, b.offset(0, 0, -7), Direction.NORTH, Blocks.DARK_OAK_DOOR, true);
            NovelRooms.bed(l, b.offset(-7, 0, -2), Blocks.WHITE_BED, Direction.NORTH);
            NovelRooms.bed(l, b.offset(-6, 0, -2), Blocks.WHITE_BED, Direction.NORTH);
            mattress(7, 8, -4, -2);
            prop(7, 1, -3, G, Direction.NORTH, 0);
            prop(6, 0, -3, LiteraryPropBlock.Kind.STAIN, Direction.NORTH, 0);
            set(-8, 0, -5, Blocks.BARREL);
            set(6, 0, -6, Blocks.AIR);
            for (int y = 0; y <= 2; y++) set(6, y, -6, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            set(6, 3, -6, Blocks.AIR); // the hatch up to the aft deck
            for (int z : new int[]{-1, -3, -5}) { porthole(-9, z); porthole(9, z); }
            hang(0, 2, -3);
            // Corridor and four cabins.
            room(-1, 1, -19, -8, carpet);
            room(-8, -3, -12, -8, carpet);
            room(3, 8, -12, -8, carpet);
            room(-8, -3, -18, -14, carpet);
            room(3, 8, -18, -14, carpet);
            cabinWalls(-2, -2, -19, -8);
            cabinWalls(2, 2, -19, -8);
            fill(-8, 0, -13, -3, 2, -13, LiteraryRegistry.YACHT_PANEL.get());
            fill(3, 0, -13, 8, 2, -13, LiteraryRegistry.YACHT_PANEL.get());
            fill(-8, 0, -19, -3, 2, -19, LiteraryRegistry.YACHT_PANEL.get());
            fill(3, 0, -19, 8, 2, -19, LiteraryRegistry.YACHT_PANEL.get());
            for (int z : new int[]{-10, -16}) {
                NovelRooms.door(l, b.offset(-2, 0, z), Direction.WEST, Blocks.DARK_OAK_DOOR, true);
                NovelRooms.door(l, b.offset(2, 0, z), Direction.EAST, Blocks.DARK_OAK_DOOR, true);
                porthole(-9, z);
                porthole(9, z);
                hang(0, 2, z);
            }
            mattress(-8, -7, -12, -9);
            prop(-5, 0, -10, G, Direction.WEST, 0);
            prop(-4, 0, -11, LiteraryPropBlock.Kind.STAIN, Direction.SOUTH, 0);
            mattress(7, 8, -12, -9);
            set(7, 1, -11, Blocks.RED_CARPET);
            prop(4, 0, -10, LiteraryPropBlock.Kind.STAIN, Direction.WEST, 1);
            mattress(-8, -7, -18, -15);
            prop(-5, 0, -17, G, Direction.NORTH, 3);
            set(-4, 0, -14, Blocks.BARREL);
            mattress(7, 8, -18, -15);
            prop(3, 0, -16, G, Direction.EAST, 1);
            prop(0, 0, -15, LiteraryPropBlock.Kind.STAIN, Direction.NORTH, 3);
            // The lobby and the companionway up to the wheelhouse.
            room(-3, 3, -23, -20, carpet);
            for (int i = 0; i < 4; i++) {
                int z = -20 - i;
                for (int x = -1; x <= 1; x++) {
                    if (i > 0) fill(x, 0, z, x, i - 1, z, LiteraryRegistry.YACHT_PANEL.get());
                    set(x, i, z, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
                }
            }
            fill(-1, 3, -22, 1, 3, -20, Blocks.AIR);
            fill(-8, 0, -24, 8, 2, -24, LiteraryRegistry.YACHT_PANEL.get());
            NovelRooms.door(l, b.offset(3, 0, -24), Direction.NORTH, Blocks.DARK_OAK_DOOR, true);
            hang(3, 2, -21);
            // The crew's quarters forward, where he waited.
            room(-8, 8, -34, -25, Blocks.SPRUCE_PLANKS);
            for (int z = -33; z <= -26; z += 3) { mattress(-halfBeam(z) + 1, -halfBeam(z) + 2, z, z + 1); mattress(halfBeam(z) - 2, halfBeam(z) - 1, z, z + 1); }
            prop(0, 0, -28, G, Direction.SOUTH, 3);
            prop(1, 0, -30, LiteraryPropBlock.Kind.STAIN, Direction.EAST, 2);
            for (int y = 0; y <= 2; y++) set(0, y, -35, LiteraryRegistry.YACHT_PANEL.get());
            for (int y = 0; y <= 2; y++) set(0, y, -34, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
            set(HATCH.getX(), HATCH.getY(), HATCH.getZ(), Blocks.AIR);
            hang(-3, 2, -29);
        }

        void mainDeck() {
            BlockState window = LiteraryRegistry.YACHT_WINDOW.get().defaultBlockState(), white = LiteraryRegistry.YACHT_HULL.get().defaultBlockState();
            Block carpet = LiteraryRegistry.YACHT_CARPET.get(), cushion = LiteraryRegistry.YACHT_CUSHION.get(), panel = LiteraryRegistry.YACHT_PANEL.get();
            var G = LiteraryPropBlock.Kind.GUEST_BODY;
            // The aft deck, where the party was.
            fill(-8, 4, -7, 8, 7, 0, Blocks.AIR);
            fill(-1, 4, -5, 1, 4, -3, panel);
            set(0, 5, -4, Blocks.CAKE.defaultBlockState().setValue(CakeBlock.BITES, 4));
            set(-1, 5, -3, Blocks.WHITE_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3));
            set(1, 5, -5, Blocks.BLUE_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 2));
            set(1, 5, -3, Blocks.RED_CANDLE);
            fill(5, 4, -1, 8, 4, -1, cushion);
            fill(8, 4, -5, 8, 4, -2, cushion);
            NovelRooms.furniture(l, b.offset(2, 4, -4), HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.WEST);
            NovelRooms.furniture(l, b.offset(-2, 4, -2), HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.EAST);
            prop(-3, 4, -2 + 0, G, Direction.WEST, 1);
            prop(0, 4, -6, G, Direction.SOUTH, 2);
            prop(-4, 4, -4, LiteraryPropBlock.Kind.STAIN, Direction.EAST, 1);
            prop(3, 4, -6, LiteraryPropBlock.Kind.STAIN, Direction.NORTH, 2);
            for (int x = -6; x <= 6; x += 4) { set(x, 7, 0, Blocks.CHAIN); hang(x, 6, 0); hang(x, 7, -7); }
            // Stairs up to the sun deck along the port side.
            for (int i = 0; i < 5; i++) {
                int z = -7 + i, y = 4 + i;
                for (int x = -8; x <= -7; x++) {
                    if (i > 0) fill(x, 4, z, x, y - 1, z, panel);
                    set(x, y, z, Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
                }
            }
            fill(-8, 8, -7, -7, 8, -4, Blocks.AIR);
            // Side decks.
            fill(-8, 4, -27, -8, 7, -8, Blocks.AIR);
            fill(8, 4, -27, 8, 7, -8, Blocks.AIR);
            // The saloon and the wheelhouse: one long room behind tinted glass.
            for (int z = -26; z <= -8; z++) for (int x = -7; x <= 7; x++) {
                boolean wall = Math.abs(x) == 7 || z == -8 || z == -26;
                if (!(Math.abs(x) <= 1 && z >= -23 && z <= -20)) set(x, 3, z, carpet); // the companionway comes up here
                if (!wall) { fill(x, 4, z, x, 7, z, Blocks.AIR); continue; }
                set(x, 4, z, white);
                set(x, 5, z, window);
                set(x, 6, z, window);
                set(x, 7, z, white);
            }
            fill(-7, 8, -26, 7, 8, -20, white); // the wheelhouse roof, forward of the sun deck
            fill(-2, 4, -8, 2, 6, -8, Blocks.AIR); // the doors to the aft deck stand open
            for (int x : new int[]{-7, 7}) fill(x, 4, -22, x, 5, -22, Blocks.AIR); // wheelhouse side doors
            fill(-6, 4, -19, -3, 6, -19, panel); // a half bulkhead between saloon and wheelhouse
            fill(3, 4, -19, 6, 6, -19, panel);
            // Saloon: sofa, table, the bar.
            fill(-6, 4, -14, -6, 4, -10, cushion);
            fill(-5, 4, -15, -3, 4, -15, cushion);
            set(-4, 4, -12, panel);
            fill(2, 4, -14, 4, 4, -11, panel);
            NovelRooms.furniture(l, b.offset(1, 4, -11), HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.EAST);
            NovelRooms.furniture(l, b.offset(5, 4, -14), HouseholdFurnitureBlock.Kind.CANE_CHAIR, Direction.WEST);
            prop(5, 4, -12, G, Direction.WEST, 2);
            prop(1, 4, -13, G, Direction.EAST, 2);
            set(3, 5, -12, Blocks.GREEN_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 4));
            fill(4, 4, -18, 6, 4, -16, panel);
            for (int x = 4; x <= 6; x++) set(x, 5, -17, Blocks.BROWN_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 1 + (x & 3)));
            prop(2, 4, -16, G, Direction.NORTH, 0);
            prop(1, 4, -15, LiteraryPropBlock.Kind.STAIN, Direction.NORTH, 0);
            prop(0, 4, -10, LiteraryPropBlock.Kind.STAIN, Direction.SOUTH, 1);
            hang(-3, 7, -11);
            hang(3, 7, -15);
            hang(0, 7, -23);
            // Wheelhouse: the helm, the captain at it, the companionway coming up beside him.
            fill(-2, 4, -25, 2, 4, -25, panel);
            set(-1, 5, -25, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR));
            set(1, 5, -25, Blocks.DARK_OAK_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, AttachFace.FLOOR));
            prop(0, 4, -24, G, Direction.NORTH, 2);
            for (int z = -22; z <= -20; z++) for (int x : new int[]{-2, 2}) set(x, 4, z, LiteraryRegistry.YACHT_RAIL.get().defaultBlockState());
            // Foredeck: the sunpad, a guest on it, the hatch down to the crew's quarters.
            fill(-2, 4, -31, 1, 4, -29, cushion);
            prop(0, 5, -30, G, Direction.SOUTH, 0);
            set(HATCH.getX(), 4, HATCH.getZ() - 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.OPEN, true).setValue(TrapDoorBlock.FACING, Direction.SOUTH));
            prop(2, 4, -33, LiteraryPropBlock.Kind.STAIN, Direction.SOUTH, 3);
        }

        void sunDeck() {
            Block cushion = LiteraryRegistry.YACHT_CUSHION.get();
            // Loungers over the deckhouse, a body face down on one of them.
            for (int x : new int[]{-5, -3, 3, 5}) fill(x, 9, 9, x, 9, 13, cushion);
            prop(3, 10, 12, LiteraryPropBlock.Kind.GUEST_BODY, Direction.SOUTH, 1);
            // The flybridge: a helm and a bimini on stainless posts.
            fill(-2, 9, -18, 2, 9, -18, LiteraryRegistry.YACHT_PANEL.get());
            for (int x : new int[]{-6, 6}) for (int z : new int[]{-8, -18}) fill(x, 9, z, x, 11, z, LiteraryRegistry.YACHT_RAIL.get());
            fill(-7, 12, -19, 7, 12, -7, LiteraryRegistry.YACHT_CANVAS.get());
            NovelRooms.furniture(l, b.offset(0, 9, -16), HouseholdFurnitureBlock.Kind.KITCHEN_STOOL, Direction.NORTH);
            prop(-3, 9, 4, LiteraryPropBlock.Kind.STAIN, Direction.EAST, 2);
        }

        void rails() {
            BlockState rail = LiteraryRegistry.YACHT_RAIL.get().defaultBlockState();
            // Main deck: around the hull's gunwale from the deckhouse forward, open at the boarding ladder.
            for (int z = -42; z <= 0; z++) for (int x = -9; x <= 9; x++) if (inHull(x, z) && outer(x, z) && !(x == -9 && z == -12)) set(x, 4, z, rail);
            // Sun deck: its edges, the stair well and the open stern.
            for (int z = -19; z <= 17; z++) { set(-9, 9, z, rail); set(9, 9, z, rail); }
            for (int x = -9; x <= 9; x++) { set(x, 9, 17, rail); set(x, 9, -19, rail); }
            for (int z = -8; z <= -3; z++) set(-6, 9, z, rail);
            set(-7, 9, -3, Blocks.AIR);
            set(-8, 9, -3, Blocks.AIR);
            set(-7, 9, -8, rail);
            set(-8, 9, -8, rail);
        }
    }
}
