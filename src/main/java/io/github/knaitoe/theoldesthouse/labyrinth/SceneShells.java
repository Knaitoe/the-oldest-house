package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.28 shells: the walls, ceilings and floors of the indoor vignettes, given
 * the same structural reading as the manor. Framing posts and pilasters,
 * cased doorways and openings, dado and picture rails, window reveals cut back
 * into the wall, fireplaces with chimney breasts, inset shelves, beamed or
 * coffered ceilings and bordered floors.
 *
 * Runs once per scene, in place, after the room's furnishings exist. It only
 * re-materials the room's own wall, ceiling and floor planes, or carves
 * outward into solid fill or empty space behind a wall. It never touches a
 * door, a block entity, a block another block or entity hangs on, a story
 * volume, the copied vestibule, or a cell anyone walks through.
 */
public final class SceneShells {
    /** Per-origin, per-scene checkpoint: the shell pass runs once, and only an explicit rebuild clears it. */
    public static final String STATE = "scene_shells_0428";
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /**
     * Materials and features for one room. Null blocks skip that element.
     * {@code railRow} counts up from the floor row; plaster covers the field
     * between the rail and the cornice.
     */
    record Palette(@Nullable Block post, @Nullable Block cornice, @Nullable Block rail, int railRow, @Nullable Block plaster,
                   @Nullable Block beam, int beamSpacing, boolean coffered, boolean dropBeams, @Nullable Block panel,
                   @Nullable Block border, @Nullable Block checker, @Nullable Block floorField,
                   Block sill, Block glass, @Nullable Block curtain, Block backing,
                   @Nullable Block hearth, int shelves, boolean outward, int postSpacing) {}

    /** The room's open volume, relative to the place's base, with its palette. */
    record Spec(int x0, int y0, int z0, int x1, int y1, int z1, Palette palette) {}

    private SceneShells() {}

    /** Whether the 0.4.28 pass reshapes this scene's shell, surfaces or outdoor edge. */
    public static boolean shapes(LabyrinthPlace place) {
        return spec(place) != null || WEATHERED.contains(place) || place == LabyrinthPlace.PLAIN
                || place == LabyrinthPlace.ZAMPANO_COURTYARD || place == LabyrinthPlace.BARN_WELL;
    }

    /** The open interior a scene's shell encloses, relative to its base, or null for scenes without one. */
    @Nullable
    public static net.minecraft.world.level.levelgen.structure.BoundingBox interior(LabyrinthPlace place) {
        Spec s = spec(place);
        return s == null ? null : new net.minecraft.world.level.levelgen.structure.BoundingBox(s.x0(), s.y0(), s.z0(), s.x1(), s.y1(), s.z1());
    }

    @Nullable
    static Spec spec(LabyrinthPlace place) {
        return switch (place) {
            // A small Victorian bedroom: dark timber, a cold grate, drawn red curtains.
            case FLOORBOARDS -> new Spec(-5, 0, -9, 5, 3, -1, new Palette(
                    Blocks.STRIPPED_DARK_OAK_LOG, null, null, 0, null,
                    Blocks.DARK_OAK_LOG, 3, false, false, null,
                    Blocks.DARK_OAK_PLANKS, null, null,
                    Blocks.DARK_OAK_SLAB, Blocks.GLASS_PANE, Blocks.RED_WOOL, Blocks.BROWN_TERRACOTTA,
                    Blocks.BRICKS, 0, true, 4));
            // A child's bedroom: pale birch framing, a dado rail, yellow curtains.
            case HIDE_AND_CLAP -> new Spec(-5, 0, -12, 5, 3, -1, new Palette(
                    Blocks.STRIPPED_BIRCH_LOG, null, Blocks.STRIPPED_BIRCH_WOOD, 1, null,
                    Blocks.BIRCH_LOG, 4, false, false, null,
                    Blocks.OAK_PLANKS, null, null,
                    Blocks.BIRCH_SLAB, Blocks.GLASS_PANE, Blocks.YELLOW_WOOL, Blocks.WHITE_CONCRETE,
                    null, 0, true, 4));
            // The show home is new and bright: no exposed timber, a quartz rail, frosted daylight in every outer wall.
            case MODEL_HOME -> new Spec(-8, 0, -18, 8, 5, -1, new Palette(
                    null, null, Blocks.SMOOTH_QUARTZ, 1, null,
                    null, 0, false, false, null,
                    null, null, null,
                    Blocks.SMOOTH_QUARTZ_SLAB, Blocks.WHITE_STAINED_GLASS_PANE, null, Blocks.WHITE_CONCRETE,
                    null, 0, true, 0));
            // A panelled study: wood to the rail, green plaster above, coffered ceiling, books built in.
            case HARRIGAN -> new Spec(-7, 0, -24, 7, 5, 0, new Palette(
                    Blocks.STRIPPED_DARK_OAK_LOG, null, Blocks.STRIPPED_DARK_OAK_WOOD, 2, Blocks.GREEN_TERRACOTTA,
                    Blocks.DARK_OAK_LOG, 4, true, true, null,
                    Blocks.DARK_OAK_PLANKS, null, null,
                    Blocks.DARK_OAK_SLAB, Blocks.GLASS_PANE, Blocks.GRAY_WOOL, Blocks.DEEPSLATE_BRICKS,
                    Blocks.POLISHED_BLACKSTONE_BRICKS, 2, true, 4));
            // An institution's common room: pilasters, a bumper rail, frosted panes, a tiled ceiling.
            case WHALE -> new Spec(-12, 0, -31, 12, 4, -1, new Palette(
                    Blocks.STONE_BRICKS, null, Blocks.POLISHED_ANDESITE, 1, null,
                    Blocks.SMOOTH_STONE, 3, true, false, Blocks.WHITE_CONCRETE,
                    Blocks.POLISHED_ANDESITE, null, null,
                    Blocks.SMOOTH_STONE_SLAB, Blocks.WHITE_STAINED_GLASS_PANE, null, Blocks.WHITE_CONCRETE,
                    null, 0, true, 6));
            // A ward: white pilasters, grey bumper rail, chequered tiles, a grid of ceiling panels.
            case HOSPITAL -> new Spec(-8, 0, -22, 8, 4, -1, new Palette(
                    Blocks.WHITE_CONCRETE, null, Blocks.LIGHT_GRAY_CONCRETE, 1, null,
                    Blocks.LIGHT_GRAY_CONCRETE, 3, true, false, Blocks.SMOOTH_QUARTZ,
                    null, Blocks.LIGHT_GRAY_CONCRETE, null,
                    Blocks.SMOOTH_QUARTZ_SLAB, Blocks.WHITE_STAINED_GLASS_PANE, null, Blocks.WHITE_CONCRETE,
                    null, 0, true, 5));
            // A lived-in bedroom: birch framing and beams, drawn curtains for the projector.
            case KAREN_ROOM -> new Spec(-8, 0, -17, 8, 4, -1, new Palette(
                    Blocks.STRIPPED_BIRCH_LOG, null, Blocks.STRIPPED_OAK_WOOD, 1, null,
                    Blocks.STRIPPED_BIRCH_LOG, 4, false, true, null,
                    Blocks.SPRUCE_PLANKS, null, null,
                    Blocks.BIRCH_SLAB, Blocks.GLASS_PANE, Blocks.LIGHT_GRAY_WOOL, Blocks.WHITE_CONCRETE,
                    null, 0, true, 4));
            // The archive, inside an outdoor courtyard: framing and beams only, nothing pushed into the alley.
            case ZAMPANO_COURTYARD -> new Spec(-11, 0, -38, 11, 6, -20, new Palette(
                    Blocks.STRIPPED_SPRUCE_LOG, null, Blocks.STRIPPED_SPRUCE_WOOD, 2, null,
                    Blocks.SPRUCE_LOG, 4, false, true, null,
                    Blocks.SPRUCE_PLANKS, null, null,
                    Blocks.SPRUCE_SLAB, Blocks.GLASS_PANE, null, Blocks.WHITE_CONCRETE,
                    null, 0, false, 5));
            // The trailer's single skin is also its outside: only its floor and ceiling change.
            case GOATMAN -> new Spec(-7, 1, -76, 7, 5, -56, new Palette(
                    null, null, null, 0, null,
                    null, 0, false, false, null,
                    null, Blocks.LIGHT_GRAY_CONCRETE, Blocks.WHITE_CONCRETE,
                    Blocks.OAK_SLAB, Blocks.GLASS_PANE, null, Blocks.WHITE_CONCRETE,
                    null, 0, false, 0));
            default -> null;
        };
    }

    /** Volumes beyond the shared story reservations that this pass must not alter. */
    static boolean reserved(LabyrinthPlace scene, int x, int y, int z) {
        if (VignetteArchitecture.storyReserved(scene, x, y, z)) return true;
        return switch (scene) {
            // The yard wall, the kid's window and the branch's path, and the chair stack over the table.
            case MODEL_HOME -> x <= -9 || (x <= -4 && z >= -16 && z <= -10)
                    || (x >= 2 && x <= 7 && z >= -16 && z <= -10 && y >= 1);
            // Bunks rise in both side bays as the gathering grows.
            case GOATMAN -> y >= 1 && (x <= -4 || x >= 4) && z >= -74 && z <= -56;
            case HOSPITAL -> Math.abs(x) <= 2 && z >= -16 && z <= -12;
            // The funeral doorway is restaged in planks on every visit.
            case HARRIGAN -> z == -13 && Math.abs(x) <= 1;
            default -> false;
        };
    }

    static void apply(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        Spec spec = spec(place);
        if (spec != null) new Room(level, base, place, spec).build();
        if (WEATHERED.contains(place)) weather(level, base, place);
        Landscapes.apply(level, base, place);
    }

    /** Caves and camps are read by their surfaces: a share of each exposed course weathers into its neighbours. */
    // The caver's cave is already weathered by its builder and restages its stone by position; the den is timber.
    private static final Set<LabyrinthPlace> WEATHERED = EnumSet.of(LabyrinthPlace.PRESERVED_CAVE,
            LabyrinthPlace.HOLLOWAY_CAMP, LabyrinthPlace.EXPLORER_CAMP);
    private static final Map<Block, Block[]> WEATHER = Map.of(
            Blocks.STONE, new Block[]{Blocks.ANDESITE, Blocks.TUFF, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.CALCITE},
            Blocks.STONE_BRICKS, new Block[]{Blocks.CRACKED_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS},
            Blocks.DEEPSLATE, new Block[]{Blocks.COBBLED_DEEPSLATE, Blocks.TUFF},
            Blocks.TUFF, new Block[]{Blocks.STONE, Blocks.ANDESITE, Blocks.DRIPSTONE_BLOCK},
            Blocks.SMOOTH_BASALT, new Block[]{Blocks.DEEPSLATE, Blocks.TUFF},
            Blocks.DIRT, new Block[]{Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.PACKED_MUD},
            Blocks.COBBLESTONE, new Block[]{Blocks.MOSSY_COBBLESTONE, Blocks.STONE},
            Blocks.SMOOTH_STONE, new Block[]{Blocks.STONE, Blocks.ANDESITE});

    private static void weather(ServerLevel l, BlockPos b, LabyrinthPlace place) {
        var r = place.room();
        int seed = place.slot() * 9781;
        for (BlockPos rel : BlockPos.betweenClosed(r.minX(), r.minY(), r.minZ(), r.maxX(), r.maxY(), r.maxZ())) {
            BlockPos at = b.offset(rel);
            var st = l.getBlockState(at);
            Block[] variants = WEATHER.get(st.getBlock());
            if (variants == null || l.getBlockEntity(at) != null || reserved(place, rel.getX(), rel.getY(), rel.getZ())) continue;
            int hash = Math.floorMod((rel.getX() * 73856093) ^ (rel.getY() * 19349663) ^ (rel.getZ() * 83492791) ^ seed, 1000);
            if (hash >= 160) continue;
            boolean exposed = false;
            for (Direction d : Direction.values()) if (l.getBlockState(at.relative(d)).isAir()) { exposed = true; break; }
            if (exposed) l.setBlock(at, variants[hash % variants.length].defaultBlockState(), F);
        }
    }

    /** A wall column facing into the room. */
    private record Face(int x, int z, Direction in) {
        Direction out() { return in.getOpposite(); }
        Direction run() { return in.getClockWise(); }
        int along() { return run().getAxis() == Direction.Axis.X ? x : z; }
    }

    private enum Role { PLAIN, POST, JAMB, FEATURE }

    private static final class Room {
        private final ServerLevel l;
        private final BlockPos b;
        private final LabyrinthPlace scene;
        private final Spec s;
        private final Palette p;
        private Block field = Blocks.AIR, floor = Blocks.AIR, ceiling = Blocks.AIR;
        private final Map<Long, Face> faces = new LinkedHashMap<>();
        private final Map<Long, Role> roles = new HashMap<>();

        Room(ServerLevel level, BlockPos base, LabyrinthPlace place, Spec spec) {
            l = level; b = base; scene = place; s = spec; p = spec.palette();
        }

        private static long key(int x, int z) { return BlockPos.asLong(x, 0, z); }
        private BlockPos at(int x, int y, int z) { return b.offset(x, y, z); }
        private BlockState state(int x, int y, int z) { return l.getBlockState(at(x, y, z)); }
        private boolean inside(int x, int y, int z) {
            return x >= s.x0() && x <= s.x1() && y >= s.y0() && y <= s.y1() && z >= s.z0() && z <= s.z1();
        }
        private boolean perimeter(int x, int z) {
            return x == s.x0() - 1 || x == s.x1() + 1 || z == s.z0() - 1 || z == s.z1() + 1;
        }
        private boolean air(int x, int y, int z) { return state(x, y, z).isAir(); }
        private boolean occupied(int x, int y, int z) {
            return !l.getEntitiesOfClass(Entity.class, new AABB(at(x, y, z))).isEmpty();
        }
        private boolean entity(int x, int y, int z) { return l.getBlockEntity(at(x, y, z)) != null; }
        private boolean isField(int x, int y, int z) {
            return !entity(x, y, z) && state(x, y, z).is(field) && !reserved(scene, x, y, z);
        }
        /** A plain, full, unreserved block of the room's walls, including earlier trim courses. */
        private boolean shell(int x, int y, int z) {
            var st = state(x, y, z);
            return !entity(x, y, z) && !reserved(scene, x, y, z) && !st.isAir() && st.isCollisionShapeFullBlock(l, at(x, y, z))
                    && !(st.getBlock() instanceof DoorBlock) && !(st.getBlock() instanceof LightBlock);
        }
        private boolean inVestibule(int x, int y, int z) {
            var v = LabyrinthPlaces.localVestibule();
            return x >= v.minX() - 1 && x <= v.maxX() + 1 && z >= v.minZ() && z <= v.maxZ() + 1 && y >= v.minY() - 1 && y <= v.maxY() + 1;
        }
        /** Solid fill or empty space that nobody stands in: safe to carve or build a backdrop into. */
        private boolean behind(int x, int y, int z) {
            if (inside(x, y, z) || inVestibule(x, y, z) || entity(x, y, z) || reserved(scene, x, y, z)) return false;
            var st = state(x, y, z);
            return st.isAir() || st.is(LabyrinthBuilder.SOLID.getBlock()) || st.is(field);
        }
        private void set(int x, int y, int z, BlockState st) { l.setBlock(at(x, y, z), st, F); }
        private void setIf(int x, int y, int z, BlockState st) { if (behind(x, y, z)) set(x, y, z, st); }
        private static BlockState log(Block block, Direction.Axis axis) {
            BlockState st = block.defaultBlockState();
            return st.hasProperty(RotatedPillarBlock.AXIS) ? st.setValue(RotatedPillarBlock.AXIS, axis) : st;
        }

        void build() {
            detect();
            if (field == Blocks.AIR) return;
            collectFaces();
            casings();
            placeFireplace();
            placePosts();
            if (p.outward()) {
                backExistingGlass();
                placeShelves();
                placeWindows();
            }
            treatWalls();
            treatCeiling();
            treatFloor();
        }

        /** The room's own field, floor and ceiling materials, as actually built. */
        private void detect() {
            Map<Block, Integer> walls = new HashMap<>(), floors = new HashMap<>(), ceilings = new HashMap<>();
            int eye = s.y0() + 1;
            for (int x = s.x0(); x <= s.x1(); x++) for (int z = s.z0(); z <= s.z1(); z++) {
                if (reserved(scene, x, eye, z)) continue;
                if (air(x, eye, z)) for (Direction d : Direction.Plane.HORIZONTAL) {
                    int nx = x + d.getStepX(), nz = z + d.getStepZ();
                    var st = state(nx, eye, nz);
                    if (!st.isAir() && !entity(nx, eye, nz) && st.isCollisionShapeFullBlock(l, at(nx, eye, nz)))
                        walls.merge(st.getBlock(), 1, Integer::sum);
                }
                if (air(x, s.y0(), z)) floors.merge(state(x, s.y0() - 1, z).getBlock(), 1, Integer::sum);
                if (air(x, s.y1(), z)) ceilings.merge(state(x, s.y1() + 1, z).getBlock(), 1, Integer::sum);
            }
            field = top(walls);
            floor = top(floors);
            ceiling = top(ceilings);
        }

        private static Block top(Map<Block, Integer> counts) {
            return counts.entrySet().stream().filter(e -> e.getKey() != Blocks.AIR)
                    .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(Blocks.AIR);
        }

        /** Every wall column, perimeter or partition, that faces open room at eye height. */
        private void collectFaces() {
            int eye = s.y0() + 1;
            for (int x = s.x0() - 1; x <= s.x1() + 1; x++) for (int z = s.z0() - 1; z <= s.z1() + 1; z++) {
                if (!isField(x, eye, z)) continue;
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    int nx = x + d.getStepX(), nz = z + d.getStepZ();
                    if (inside(nx, eye, nz) && air(nx, eye, nz)) {
                        faces.putIfAbsent(key(x, z), new Face(x, z, d));
                        roles.put(key(x, z), Role.PLAIN);
                        break;
                    }
                }
            }
        }

        private boolean opening(int x, int z) {
            var low = state(x, s.y0(), z);
            return low.getBlock() instanceof DoorBlock || (low.isAir() && air(x, s.y0() + 1, z));
        }

        /** Doors and archways get a cased surround: jambs either side and a lintel over the head. */
        private void casings() {
            Block casing = p.post() != null ? p.post() : p.rail();
            if (casing == null) return;
            for (Face f : faces.values()) {
                Direction r = f.run();
                for (int side : new int[]{-1, 1}) {
                    int ox = f.x() + r.getStepX() * side, oz = f.z() + r.getStepZ() * side;
                    if (!opening(ox, oz)) continue;
                    roles.put(key(f.x(), f.z()), Role.JAMB);
                    for (int y = s.y0(); y <= Math.min(s.y1(), s.y0() + 2); y++)
                        if (isField(f.x(), y, f.z()) || casingTrim(f.x(), y, f.z())) set(f.x(), y, f.z(), log(casing, Direction.Axis.Y));
                    // The head of the opening: the wall over a door, the first solid course over an archway.
                    for (int y = s.y0() + 2; y <= s.y1(); y++) {
                        if (isField(ox, y, oz)) { set(ox, y, oz, log(casing, r.getAxis())); break; }
                        if (!air(ox, y, oz) && !(state(ox, y, oz).getBlock() instanceof DoorBlock)) break;
                    }
                }
            }
        }

        /** A base course laid by the earlier dressing may still be cased where it meets a door. */
        private boolean casingTrim(int x, int y, int z) {
            return y == s.y0() && !entity(x, y, z) && !reserved(scene, x, y, z) && state(x, y, z).isCollisionShapeFullBlock(l, at(x, y, z))
                    && !(state(x, y, z).getBlock() instanceof DoorBlock) && faces.containsKey(key(x, z));
        }

        private boolean doorNear(Face f, int reach) {
            Direction r = f.run();
            for (int i = -reach; i <= reach; i++) {
                int x = f.x() + r.getStepX() * i, z = f.z() + r.getStepZ() * i;
                if (opening(x, z) || roles.get(key(x, z)) == Role.JAMB) return true;
            }
            return false;
        }

        /** Contiguous plain perimeter columns along each wall, in order. */
        private List<List<Face>> runs(int minimum) {
            List<List<Face>> result = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
            for (Face f : faces.values()) {
                if (seen.contains(key(f.x(), f.z())) || !perimeter(f.x(), f.z()) || roles.get(key(f.x(), f.z())) != Role.PLAIN) continue;
                // Walk back to the start of this run, then forward to its end.
                Face start = f;
                while (true) {
                    Face prev = faces.get(key(start.x() - start.run().getStepX(), start.z() - start.run().getStepZ()));
                    if (prev == null || prev.in() != f.in() || roles.get(key(prev.x(), prev.z())) != Role.PLAIN) break;
                    start = prev;
                }
                List<Face> run = new ArrayList<>();
                for (Face c = start; c != null && c.in() == f.in() && roles.get(key(c.x(), c.z())) == Role.PLAIN;
                     c = faces.get(key(c.x() + c.run().getStepX(), c.z() + c.run().getStepZ()))) {
                    run.add(c);
                    seen.add(key(c.x(), c.z()));
                }
                if (run.size() >= minimum) result.add(run);
            }
            result.sort(Comparator.comparingInt((List<Face> run) -> -run.size()).thenComparingInt(run -> run.get(0).along()));
            return result;
        }

        /** Whether a wall column can open or carve between {@code low} and {@code high}, with clear room in front. */
        private boolean carvable(Face f, int low, int high, int frontLow, int frontHigh) {
            if (doorNear(f, 1)) return false;
            for (int y = low; y <= high; y++) if (!isField(f.x(), y, f.z())) return false;
            int fx = f.x() + f.in().getStepX(), fz = f.z() + f.in().getStepZ();
            for (int y = frontLow; y <= frontHigh; y++)
                if (!air(fx, y, fz) || occupied(fx, y, fz) || reserved(scene, fx, y, fz)) return false;
            int ox = f.x() + f.out().getStepX(), oz = f.z() + f.out().getStepZ();
            for (int y = low - 1; y <= high + 1; y++) {
                if (!behind(ox, y, oz)) return false;
                if (!behind(ox + f.out().getStepX(), y, oz + f.out().getStepZ())) return false;
            }
            return true;
        }

        /** A chimney breast in the wall, a grate set back into it, a hearth in the floor, a mantel shelf. */
        private void placeFireplace() {
            if (p.hearth() == null) return;
            int top = Math.min(s.y1(), s.y0() + 2);
            for (List<Face> run : runs(5)) {
                for (int i = 2; i + 2 < run.size(); i++) {
                    boolean ok = true;
                    for (int j = i - 2; j <= i + 2 && ok; j++) {
                        Face f = run.get(j);
                        boolean opening = j >= i - 1 && j <= i + 1;
                        // The field between the earlier base and cornice courses; those courses must be plain shell too.
                        for (int y = s.y0() + 1; y < s.y1(); y++) if (!isField(f.x(), y, f.z())) ok = false;
                        for (int y : new int[]{s.y0(), s.y1()}) if (!shell(f.x(), y, f.z())) ok = false;
                        if (opening && (top - 1 < s.y0() + 1 || !carvable(f, s.y0() + 1, top - 1, s.y0(), top))) ok = false;
                    }
                    if (!ok) continue;
                    buildFireplace(run, i, top);
                    return;
                }
            }
        }

        private void buildFireplace(List<Face> run, int i, int top) {
            Face c = run.get(i);
            Direction out = c.out(), r = c.run();
            BlockState brick = p.hearth().defaultBlockState();
            BlockState mantel = p.sill().defaultBlockState().hasProperty(SlabBlock.TYPE)
                    ? p.sill().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP) : p.sill().defaultBlockState();
            for (int j = i - 2; j <= i + 2; j++) {
                Face f = run.get(j);
                roles.put(key(f.x(), f.z()), Role.FEATURE);
                boolean opening = j >= i - 1 && j <= i + 1;
                int ox = f.x() + out.getStepX(), oz = f.z() + out.getStepZ();
                int bx = ox + out.getStepX(), bz = oz + out.getStepZ();
                for (int y = s.y0(); y <= s.y1(); y++) {
                    if (opening && y < top) {
                        set(f.x(), y, f.z(), AIR);
                        // The firebox: brick sides and back, the grate at its centre.
                        set(ox, y, oz, j == i && y == s.y0() ? Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false)
                                .setValue(CampfireBlock.FACING, c.in()) : AIR);
                        set(bx, y, bz, brick);
                    } else {
                        set(f.x(), y, f.z(), brick);
                        if (behind(ox, y, oz)) set(ox, y, oz, brick);
                    }
                }
                setIf(ox, s.y0() - 1, oz, brick);
                setIf(ox, top, oz, brick);
                setIf(bx, top, bz, brick);
                // A hearth stone in front of the grate.
                int hx = f.x() + c.in().getStepX(), hz = f.z() + c.in().getStepZ();
                if (opening && state(hx, s.y0() - 1, hz).is(floor) && !entity(hx, s.y0() - 1, hz)) set(hx, s.y0() - 1, hz, brick);
                if (opening && top <= s.y1() && air(hx, top, hz) && !occupied(hx, top, hz)) set(hx, top, hz, mantel);
            }
            // Close the firebox's ends behind the wall.
            for (int side : new int[]{-2, 2}) {
                Face f = run.get(i + side);
                int ox = f.x() + out.getStepX(), oz = f.z() + out.getStepZ();
                for (int y = s.y0() - 1; y <= top; y++) if (behind(ox, y, oz)) set(ox, y, oz, brick);
            }
        }

        /** Posts at the ends of runs and at a rhythm along them, offset per wall so facing walls never mirror. */
        private void placePosts() {
            if (p.post() == null || p.postSpacing() <= 0) return;
            int seed = Math.floorMod(scene.slot() * 7, 13);
            for (Face f : faces.values()) {
                long k = key(f.x(), f.z());
                if (roles.get(k) != Role.PLAIN) continue;
                Direction r = f.run();
                Face next = faces.get(key(f.x() + r.getStepX(), f.z() + r.getStepZ()));
                Face prev = faces.get(key(f.x() - r.getStepX(), f.z() - r.getStepZ()));
                boolean end = next == null || prev == null || next.in() != f.in() || prev.in() != f.in();
                int wall = f.in().getAxis() == Direction.Axis.X ? f.x() : f.z();
                boolean rhythm = Math.floorMod(f.along() + wall * 3 + seed, p.postSpacing()) == 0;
                if (!(end || rhythm) || doorNear(f, 1)) continue;
                if (roleNear(f, Role.POST) || roleNear(f, Role.FEATURE)) continue;
                roles.put(k, Role.POST);
            }
        }

        private boolean roleNear(Face f, Role role) {
            Direction r = f.run();
            for (int i : new int[]{-1, 1}) if (roles.get(key(f.x() + r.getStepX() * i, f.z() + r.getStepZ() * i)) == role) return true;
            return false;
        }

        /** Posts full height between base and cornice; rails, plaster and cornice on the plain field. */
        private void treatWalls() {
            for (Face f : faces.values()) {
                Role role = roles.get(key(f.x(), f.z()));
                if (role == Role.JAMB || role == Role.FEATURE) continue;
                for (int y = s.y0(); y <= s.y1(); y++) {
                    if (!isField(f.x(), y, f.z())) continue;
                    int row = y - s.y0();
                    if (role == Role.POST && y > s.y0() && y < s.y1()) set(f.x(), y, f.z(), log(p.post(), Direction.Axis.Y));
                    else if (y == s.y1() && p.cornice() != null) set(f.x(), y, f.z(), log(p.cornice(), f.run().getAxis()));
                    else if (p.rail() != null && row == p.railRow()) set(f.x(), y, f.z(), log(p.rail(), f.run().getAxis()));
                    else if (p.plaster() != null && row > p.railRow() && y < s.y1()) set(f.x(), y, f.z(), p.plaster().defaultBlockState());
                }
            }
        }

        /** Shelves set back into the wall above the dado, books at the back, a sill and a lintel. */
        private void placeShelves() {
            if (p.shelves() <= 0) return;
            int low = s.y0() + 1, high = Math.min(s.y1() - 1, s.y0() + 3);
            if (high < low) return;
            int placed = 0;
            for (List<Face> run : runs(3)) {
                if (placed >= p.shelves()) return;
                int width = Math.min(3, run.size());
                List<Face> bay = firstBay(run, width, f -> carvable(f, low, high, low, high));
                if (bay == null) continue;
                for (Face f : bay) {
                    roles.put(key(f.x(), f.z()), Role.FEATURE);
                    int ox = f.x() + f.out().getStepX(), oz = f.z() + f.out().getStepZ();
                    for (int y = low; y <= high; y++) {
                        set(f.x(), y, f.z(), y == low ? slab(SlabType.BOTTOM) : AIR);
                        set(ox, y, oz, y == low ? Blocks.CHISELED_BOOKSHELF.defaultBlockState()
                                .setValue(ChiseledBookShelfBlock.FACING, f.in()) : Blocks.BOOKSHELF.defaultBlockState());
                    }
                    setIf(ox, low - 1, oz, p.backing().defaultBlockState());
                    setIf(ox, high + 1, oz, p.backing().defaultBlockState());
                }
                closeSides(bay, low, high, p.backing().defaultBlockState(), 1);
                placed++;
            }
        }

        /** Window reveals: the opening in the wall, glass set back a block, a sill, and daylight or a drawn curtain behind. */
        private void placeWindows() {
            int low = s.y0() + 1, high = s.y0() + 2;
            if (high + 1 > s.y1() + 1) return;
            Map<Direction, Integer> perWall = new EnumMap<>(Direction.class);
            for (List<Face> whole : runs(3)) {
              Direction in = whole.get(0).in();
              int width = whole.size() >= 5 ? 2 : 1;
              for (int from = 0; from + width <= whole.size(); ) {
                if (perWall.getOrDefault(in, 0) >= 3) break;
                List<Face> bay = firstBay(whole.subList(from, whole.size()), width,
                        f -> carvable(f, low, high, low, high) && behindDeep(f, low - 1, high + 1, 3));
                if (bay == null) break;
                from = whole.indexOf(bay.get(bay.size() - 1)) + 4;
                for (Face f : bay) {
                    roles.put(key(f.x(), f.z()), Role.FEATURE);
                    int ox = f.x() + f.out().getStepX(), oz = f.z() + f.out().getStepZ();
                    int bx = ox + f.out().getStepX(), bz = oz + f.out().getStepZ();
                    for (int y = low; y <= high; y++) {
                        set(f.x(), y, f.z(), y == low ? slab(SlabType.BOTTOM) : AIR);
                        set(ox, y, oz, p.glass().defaultBlockState());
                        backdrop(bx, y, bz, f.out());
                    }
                    set(ox, low - 1, oz, trim(f));
                    set(ox, high + 1, oz, trim(f));
                    for (int y = low - 1; y <= high + 1; y += high + 2 - low) setIf(bx, y, bz, p.backing().defaultBlockState());
                }
                closeSides(bay, low, high, p.backing().defaultBlockState(), 2);
                perWall.merge(in, 1, Integer::sum);
              }
            }
        }

        /** The first run of {@code width} adjacent columns, away from the run's ends, where every column qualifies. */
        @Nullable
        private List<Face> firstBay(List<Face> run, int width, java.util.function.Predicate<Face> ok) {
            int margin = run.size() >= width + 2 ? 1 : 0;
            for (int i = margin; i + width <= run.size() - margin; i++) {
                List<Face> bay = run.subList(i, i + width);
                if (bay.stream().allMatch(ok)) return bay;
            }
            return null;
        }

        private boolean behindDeep(Face f, int low, int high, int depth) {
            for (int d = 1; d <= depth; d++) {
                int x = f.x() + f.out().getStepX() * d, z = f.z() + f.out().getStepZ() * d;
                for (int y = low; y <= high; y++) if (!behind(x, y, z)) return false;
            }
            return true;
        }

        /** Light behind frosted glass reads as daylight; otherwise a curtain is drawn across it. */
        private void backdrop(int x, int y, int z, Direction out) {
            if (p.curtain() != null) { if (behind(x, y, z)) set(x, y, z, p.curtain().defaultBlockState()); return; }
            if (behind(x, y, z)) set(x, y, z, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 12));
            setIf(x + out.getStepX(), y, z + out.getStepZ(), p.backing().defaultBlockState());
        }

        private BlockState trim(Face f) {
            Block t = p.rail() != null ? p.rail() : p.post() != null ? p.post() : p.backing();
            return log(t, f.run().getAxis());
        }

        private BlockState slab(SlabType type) {
            BlockState st = p.sill().defaultBlockState();
            return st.hasProperty(SlabBlock.TYPE) ? st.setValue(SlabBlock.TYPE, type) : st;
        }

        /** Close a recess's ends behind the wall so nothing beyond it shows. */
        private void closeSides(List<Face> bay, int low, int high, BlockState with, int depth) {
            Face first = bay.get(0), last = bay.get(bay.size() - 1);
            Direction r = first.run(), out = first.out();
            for (int d = 1; d <= depth; d++) for (int y = low - 1; y <= high + 1; y++) {
                setIf(first.x() - r.getStepX() + out.getStepX() * d, y, first.z() - r.getStepZ() + out.getStepZ() * d, with);
                setIf(last.x() + r.getStepX() + out.getStepX() * d, y, last.z() + r.getStepZ() + out.getStepZ() * d, with);
            }
        }

        /** Glass already set into a wall that faces empty space gets the same backdrop as a new window. */
        private void backExistingGlass() {
            for (int x = s.x0() - 1; x <= s.x1() + 1; x++) for (int z = s.z0() - 1; z <= s.z1() + 1; z++) {
                if (!perimeter(x, z)) continue;
                for (int y = s.y0(); y <= s.y1(); y++) {
                    var st = state(x, y, z);
                    if (!glass(st)) continue;
                    Direction out = x == s.x0() - 1 ? Direction.WEST : x == s.x1() + 1 ? Direction.EAST : z == s.z0() - 1 ? Direction.NORTH : Direction.SOUTH;
                    int ox = x + out.getStepX(), oz = z + out.getStepZ();
                    if (!state(ox, y, oz).isAir() || !behind(ox, y, oz)) continue;
                    backdrop(ox, y, oz, out);
                    for (Direction d : Direction.values()) {
                        if (d == out.getOpposite() || d == out) continue;
                        int nx = ox + d.getStepX(), ny = y + d.getStepY(), nz = oz + d.getStepZ();
                        // Another pane's own backdrop cell stays open for its light or curtain.
                        if (glass(state(nx - out.getStepX(), ny, nz - out.getStepZ()))) continue;
                        if (state(nx, ny, nz).isAir()) setIf(nx, ny, nz, p.backing().defaultBlockState());
                    }
                }
            }
        }

        private static boolean glass(BlockState st) {
            return st.is(Blocks.GLASS_PANE) || st.is(Blocks.GLASS) || st.getBlock() instanceof StainedGlassPaneBlock
                    || st.getBlock() instanceof StainedGlassBlock;
        }

        /** Beams across the short span, a coffered grid where called for, dropped below the ceiling in tall rooms. */
        private void treatCeiling() {
            if (p.beam() == null || p.beamSpacing() <= 0 || ceiling == Blocks.AIR) return;
            if (p.panel() != null) for (int x = s.x0(); x <= s.x1(); x++) for (int z = s.z0(); z <= s.z1(); z++)
                if (state(x, s.y1() + 1, z).is(ceiling) && !entity(x, s.y1() + 1, z) && !reserved(scene, x, s.y1() + 1, z) && air(x, s.y1(), z))
                    set(x, s.y1() + 1, z, p.panel().defaultBlockState());
            Block surface = p.panel() != null ? p.panel() : ceiling;
            boolean longZ = (s.z1() - s.z0()) >= (s.x1() - s.x0());
            int y = s.y1() + 1;
            boolean tall = s.y1() - s.y0() + 1 >= 5;
            for (int x = s.x0(); x <= s.x1(); x++) for (int z = s.z0(); z <= s.z1(); z++) {
                boolean primary = longZ ? Math.floorMod(z - s.z1() - 1, p.beamSpacing()) == 0 : Math.floorMod(x - s.x0() + 1, p.beamSpacing()) == 0;
                boolean cross = p.coffered() && (longZ ? Math.floorMod(x - s.x0() + 1, p.beamSpacing()) == 0 : Math.floorMod(z - s.z1() - 1, p.beamSpacing()) == 0);
                if (!(primary || cross) || !state(x, y, z).is(surface) || entity(x, y, z) || reserved(scene, x, y, z) || !air(x, s.y1(), z)) continue;
                Direction.Axis axis = primary ? (longZ ? Direction.Axis.X : Direction.Axis.Z) : (longZ ? Direction.Axis.Z : Direction.Axis.X);
                set(x, y, z, log(p.beam(), axis));
                if (primary && p.dropBeams() && tall && air(x, s.y1() - 1, z) && air(x, s.y1() - 2, z)
                        && !occupied(x, s.y1(), z) && !occupied(x, s.y1() - 1, z) && !reserved(scene, x, s.y1(), z))
                    set(x, s.y1(), z, log(p.beam(), axis));
            }
        }

        /** A darker border along the walls, or chequered tiles; carpets, story blocks and anything with contents are left alone. */
        private void treatFloor() {
            if (floor == Blocks.AIR || (p.border() == null && p.checker() == null)) return;
            int y = s.y0() - 1;
            for (int x = s.x0(); x <= s.x1(); x++) for (int z = s.z0(); z <= s.z1(); z++) {
                if (!state(x, y, z).is(floor) || entity(x, y, z) || reserved(scene, x, y, z)) continue;
                boolean edge = false;
                for (Direction d : Direction.Plane.HORIZONTAL) if (faces.containsKey(key(x + d.getStepX(), z + d.getStepZ()))) edge = true;
                if (p.checker() != null && Math.floorMod(x + z, 2) == 0) set(x, y, z, p.checker().defaultBlockState());
                else if (p.border() != null && edge) set(x, y, z, p.border().defaultBlockState());
                else if (p.floorField() != null) set(x, y, z, p.floorField().defaultBlockState());
            }
        }
    }
}
