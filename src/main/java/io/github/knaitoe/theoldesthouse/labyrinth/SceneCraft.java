package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.RugFloorBlock;
import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import static io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.*;
import static io.github.knaitoe.theoldesthouse.house.RugFloorBlock.Tone.*;

/**
 * 0.4.46: the literary rooms and outdoor scenes composed to the standard of the
 * manor itself, once per scene and in place. Rooms get what a lived-in house has:
 * a hearth with a mantel, built-in shelving, rugs under real seating groups,
 * chandeliers on the room's axis and lamps on its posts, in place of the generic
 * pass's scattered pendants and checkerboard of carpet squares. Outdoor scenes
 * lose their planted grids of identical spruces for mixed woods with undergrowth,
 * their crenellated banks for graded ones, and their buildings gain porches,
 * shutters, corner posts and the small things people leave outside.
 *
 * It only adds into empty space (or replaces ordinary floor, wall and ground
 * material), never touches doors, block entities, story volumes, the copied
 * vestibule or anyone standing there, and every piece of furniture is undone
 * again if it would cut off any part of a room a visitor could reach before.
 */
public final class SceneCraft {
    public static final String STATE = "scene_craft_0446";
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    static final Set<LabyrinthPlace> ROOMS = EnumSet.of(LabyrinthPlace.HILL_NURSERY, LabyrinthPlace.MINIATURES, LabyrinthPlace.MASQUE,
            LabyrinthPlace.USHER, LabyrinthPlace.WINCHESTER, LabyrinthPlace.CHILD_ROOM, LabyrinthPlace.CRIMSON_HALL, LabyrinthPlace.BLY_ROUTE,
            LabyrinthPlace.ELK_FAN, LabyrinthPlace.CONFESSION, LabyrinthPlace.DEVILS_ROCK, LabyrinthPlace.WHEEL, LabyrinthPlace.GHOSTS_SET);
    static final Set<LabyrinthPlace> GROUNDS = EnumSet.of(LabyrinthPlace.ELK_LOT, LabyrinthPlace.MAPPING_INTERIOR, LabyrinthPlace.HOLY_RABBIT,
            LabyrinthPlace.ELK_CARCASSES, LabyrinthPlace.COSTUME_NIGHT, LabyrinthPlace.MOVIE_NIGHT, LabyrinthPlace.WINTER_LAKE,
            LabyrinthPlace.CAMP_BLOOD, LabyrinthPlace.END_WORLD_CABIN, LabyrinthPlace.SHALLOWS, LabyrinthPlace.PHONE_CANOE,
            LabyrinthPlace.DROWNED_TOWN, LabyrinthPlace.HOTEL_GROUNDS);
    /** How far outside its room box a scene's banks and woods reach. */
    private static final int REACH = 6;

    public static boolean applies(LabyrinthPlace place) {
        return ROOMS.contains(place) || GROUNDS.contains(place);
    }

    private static String key(BlockPos origin, LabyrinthPlace place) {
        return origin.asLong() + ":" + place.id();
    }

    public static boolean done(MinecraftServer server, BlockPos origin, LabyrinthPlace place) {
        return LabyrinthData.get(server).state(STATE).getBoolean(key(origin, place));
    }

    /** Composes a freshly polished scene, unless somebody is in it; the in-place pass finishes it later. */
    public static void craftOnce(ServerLevel level, BlockPos origin, LabyrinthPlace place) {
        if (!applies(place)) return;
        LabyrinthData data = LabyrinthData.get(level.getServer());
        CompoundTag done = data.state(STATE);
        if (done.getBoolean(key(origin, place))) return;
        BlockPos base = LabyrinthPlaces.base(origin, place);
        if (base == null) return;
        AABB area = area(base, place);
        if (level.players().stream().anyMatch(p -> area.intersects(p.getBoundingBox()))) return;
        apply(level, base, place);
        done.putBoolean(key(origin, place), true);
        data.setState(STATE, done);
    }

    /** An explicit rebuild authors the room again, so it is composed again. */
    public static void forget(MinecraftServer server, BlockPos origin, LabyrinthPlace place) {
        LabyrinthData data = LabyrinthData.get(server);
        CompoundTag done = data.state(STATE);
        if (!done.contains(key(origin, place))) return;
        done.remove(key(origin, place));
        data.setState(STATE, done);
    }

    /** Existing worlds: one polished, empty, loaded scene at a time. */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 40 != 23 || server instanceof net.minecraft.gametest.framework.GameTestServer
                || LabyrinthBuilder.isCarving()) return;
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        if (origin == null) return;
        LabyrinthData data = LabyrinthData.get(server);
        if (!origin.equals(data.builtOrigin())) return;
        CompoundTag done = data.state(STATE), polished = data.state(ScenePolish.STATE);
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (!applies(place) || done.getBoolean(key(origin, place)) || !polished.getBoolean(key(origin, place))) continue;
            if (data.door(place.entryDoorId()) == null || !LabyrinthBuilder.isPlaceReady(data, place)) continue;
            ServerLevel level = server.getLevel(NovelRooms.dimension(place));
            BlockPos base = LabyrinthPlaces.base(origin, place);
            if (level == null || base == null) continue;
            AABB area = area(base, place);
            if (level.players().stream().anyMatch(p -> area.intersects(p.getBoundingBox()))) continue;
            if (!loaded(level, area, base)) return;
            long started = System.nanoTime();
            apply(level, base, place);
            done.putBoolean(key(origin, place), true);
            data.setState(STATE, done);
            TheOldestHouse.LOGGER.info("Composed {} in place ({} ms).", place.id(), (System.nanoTime() - started) / 1_000_000L);
            return;
        }
    }

    private static AABB area(BlockPos base, LabyrinthPlace place) {
        BoundingBox r = place.room();
        int margin = NovelRooms.outside(place) ? REACH + 1 : 1;
        return new AABB(base.getX() + r.minX() - margin, base.getY() + r.minY() - 1, base.getZ() + r.minZ() - margin,
                base.getX() + r.maxX() + margin + 1, base.getY() + r.maxY() + 2, base.getZ() + r.maxZ() + 1);
    }

    private static boolean loaded(ServerLevel level, AABB area, BlockPos ticket) {
        boolean ready = true;
        for (int x = ((int) Math.floor(area.minX)) >> 4; x <= ((int) Math.ceil(area.maxX) - 1) >> 4; x++)
            for (int z = ((int) Math.floor(area.minZ)) >> 4; z <= ((int) Math.ceil(area.maxZ) - 1) >> 4; z++) {
                ChunkPos chunk = new ChunkPos(x, z);
                level.getChunkSource().addRegionTicket(TicketType.PORTAL, chunk, 3, ticket);
                ready &= level.isLoaded(new BlockPos(x << 4, ticket.getY(), z << 4)) && level.areEntitiesLoaded(chunk.toLong());
            }
        return ready;
    }

    /** Composes one scene. Returns how many blocks it changed. */
    public static int apply(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        SceneCraft craft = new SceneCraft(level, base, place);
        if (ROOMS.contains(place)) craft.interior();
        else craft.grounds();
        return craft.changed;
    }

    // ------------------------------------------------------------------

    private record Change(BlockPos pos, BlockState old) {}
    /** A room as its builder framed it: walls on x0/x1/z0/z1, floor below y, ceiling at y + h. */
    private record Rect(int x0, int x1, int z0, int z1, int y, int h) {
        boolean inside(int x, int yy, int z) { return x > x0 && x < x1 && z > z0 && z < z1 && yy >= y && yy < y + h; }
    }
    private enum Tree { SPRUCE, PINE, OAK, BIRCH, SNAG }

    private final ServerLevel l;
    private final BlockPos b;
    private final LabyrinthPlace scene;
    private final BoundingBox r;
    private final boolean outdoor;
    private final List<Change> journal = new ArrayList<>();
    private final Set<BlockPos> placed = new HashSet<>();
    private BitSet before;
    private int changed;

    private SceneCraft(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        l = level;
        b = base;
        scene = place;
        r = place.room();
        outdoor = NovelRooms.outside(place);
    }

    private BlockPos p(int x, int y, int z) { return b.offset(x, y, z); }
    private BlockState at(int x, int y, int z) { return l.getBlockState(p(x, y, z)); }

    private void set(BlockPos pos, BlockState state) {
        journal.add(new Change(pos.immutable(), l.getBlockState(pos)));
        l.setBlock(pos, state, F);
        changed++;
    }

    private void remove(BlockPos pos) {
        l.setBlock(pos, AIR, F | Block.UPDATE_SUPPRESS_DROPS);
        changed++;
    }

    private int hash(int x, int z, int salt) {
        return Math.floorMod((x * 73856093) ^ (z * 19349663) ^ ((salt + scene.ordinal() * 131) * 83492791), 1 << 20);
    }

    // ------------------------------------------------------------------
    // What may and may not be built on.

    /** Cells left exactly as authored: door approaches, story volumes, and the papers each story is read from. */
    private boolean kept(int x, int y, int z) {
        for (var door : scene.doors())
            if (Math.abs(x - door.rel().getX()) <= 1 && Math.abs(z - door.rel().getZ()) <= 2
                    && y >= door.rel().getY() - 1 && y <= door.rel().getY() + 2) return true;
        if (VignetteArchitecture.storyReserved(scene, x, y, z)) return true;
        if (outdoor && Math.abs(x) <= 10 && z >= -1) return true; // the arrival vestibule stays as the door copies it
        if (LiteraryRooms.isLiterary(scene))
            for (BlockPos k : List.of(LiteraryRooms.source(scene), LiteraryRooms.ending(scene)))
                if (Math.abs(k.getX() - x) <= 1 && Math.abs(k.getZ() - z) <= 1 && y >= k.getY() - 1 && y <= k.getY() + 1) return true;
        return false;
    }

    /** Lanes each story needs open: walks, actor routes, crawls and lines of sight. Rows are x0,x1,z0,z1,y0,y1. */
    private int[][] lanes() {
        return switch (scene) {
            case HILL_NURSERY -> new int[][]{{-2, 2, -42, -1, 0, 4}, {-13, -5, -18, -15, 0, 3}, {-1, 1, -34, -24, -5, 3}, {-11, -9, -37, -35, -5, -1}};
            case MINIATURES -> new int[][]{{-10, -6, -19, -14, 0, 3}, {-2, 2, -19, -14, 0, 3}, {6, 10, -19, -14, 0, 3}, {-2, 2, -13, -1, 0, 3}};
            case MASQUE -> new int[][]{{-2, 2, -78, 0, 0, 4}, {-6, -4, -78, 0, 0, 3}, {4, 6, -78, 0, 0, 3}};
            case USHER -> new int[][]{{-2, 2, -24, -13, -4, 3}, {-6, 6, -33, -22, -4, 2}, {-2, 2, -12, -1, 0, 3}, {10, 14, -36, -32, 0, 3}};
            case WINCHESTER -> new int[][]{{-28, 28, -77, -1, 0, 17}};
            case CHILD_ROOM -> new int[][]{{-5, 5, -23, -21, 0, 4}, {-2, 2, -19, -13, -3, 3}, {-10, -7, -13, -9, 0, 3}, {-1, 1, -12, -1, 0, 3}};
            case CRIMSON_HALL -> new int[][]{{-3, 3, -31, -1, 0, 3}, {-13, -9, -18, -6, 0, 8}, {-17, 17, -41, -35, 6, 9}, {-17, -14, -35, -6, 6, 9}, {14, 17, -35, -6, 6, 9}};
            case BLY_ROUTE -> new int[][]{{-1, 1, -39, -1, 0, 3}};
            case ELK_FAN -> new int[][]{{-2, 2, -20, -14, 0, 8}, {-1, 1, -13, -1, 0, 3}};
            case CONFESSION -> new int[][]{{0, 2, -23, -16, 0, 3}, {-1, 1, -15, -1, 0, 3}};
            case DEVILS_ROCK -> new int[][]{{-15, 15, -19, -17, 0, 2}, {-2, 2, -30, -26, 0, 3}, {-3, 1, -22, -18, -1, 2}, {-1, 1, -16, -1, 0, 3}};
            case WHEEL -> new int[][]{{-2, 2, -64, -1, 0, 3}, {11, 17, -62, -55, 0, 3}, {12, 16, -10, -6, 0, 3}, {-16, -12, -10, -6, 0, 3},
                    {12, 16, -27, -23, 0, 3}, {-16, -12, -27, -23, 0, 3}, {12, 16, -44, -40, 0, 3}, {-16, -12, -44, -40, 0, 3}};
            case GHOSTS_SET -> new int[][]{{-3, 3, -31, -25, 0, 3}, {-6, 6, -21, -19, 0, 2}, {-7, 7, -36, -30, 0, 3}, {1, 8, -32, -20, 0, 3}, {-1, 1, -13, -1, 0, 3}};
            default -> new int[0][];
        };
    }

    /** Where nothing solid may stand: kept cells, story lanes, and the reach of anything a story or mechanism uses. */
    private boolean clearZone(int x, int y, int z) {
        if (kept(x, y, z)) return true;
        if (LiteraryRooms.isLiterary(scene))
            for (BlockPos k : List.of(LiteraryRooms.source(scene), LiteraryRooms.ending(scene)))
                if (Math.abs(k.getX() - x) <= 2 && Math.abs(k.getZ() - z) <= 2 && Math.abs(k.getY() - y) <= 2) return true;
        for (int[] lane : lanes())
            if (x >= lane[0] && x <= lane[1] && z >= lane[2] && z <= lane[3] && y >= lane[4] && y <= lane[5]) return true;
        if (scene == LabyrinthPlace.DEVILS_ROCK)
            for (int i = 0; i < 8; i++) {
                BlockPos page = LiteraryVignettes.diaryPosition(i);
                if (Math.abs(page.getX() - x) <= 1 && Math.abs(page.getZ() - z) <= 1 && y <= 2) return true;
            }
        BlockPos at = p(x, y, z);
        for (BlockPos near : BlockPos.betweenClosed(at.offset(-2, -1, -2), at.offset(2, 2, 2))) {
            if (placed.contains(near)) continue;
            BlockState s = l.getBlockState(near);
            int dx = near.getX() - at.getX(), dz = near.getZ() - at.getZ();
            if (s.getBlock() instanceof DoorBlock) {
                Direction facing = s.getValue(DoorBlock.FACING);
                int along = facing.getAxis() == Direction.Axis.Z ? dz : dx, across = facing.getAxis() == Direction.Axis.Z ? dx : dz;
                if (Math.abs(across) <= 1) return true;
                continue;
            }
            if (Math.abs(dx) > 1 || Math.abs(dz) > 1) continue;
            Block block = s.getBlock();
            if ((s.hasBlockEntity() && !(block instanceof net.minecraft.world.level.block.BedBlock) && !(block instanceof net.minecraft.world.level.block.AbstractBannerBlock))
                    || block instanceof LadderBlock || block instanceof TripWireBlock || block instanceof BasePressurePlateBlock
                    || s.is(Blocks.POWDER_SNOW) || s.is(Blocks.SCULK_SENSOR) || s.is(LiteraryRegistry.PROP.get()) || s.is(NovelRegistry.PROP.get()))
                return true;
        }
        return false;
    }

    private static boolean plant(BlockState s) {
        return s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.SNOW) || s.is(Blocks.DEAD_BUSH) || s.is(Blocks.DANDELION)
                || s.is(Blocks.POPPY) || s.is(Blocks.AZURE_BLUET) || s.is(Blocks.BROWN_MUSHROOM) || s.is(Blocks.RED_MUSHROOM) || s.is(Blocks.MOSS_CARPET);
    }

    private boolean free(BlockPos at) {
        BlockState s = l.getBlockState(at);
        return (s.isAir() || outdoor && plant(s)) && l.getBlockEntity(at) == null;
    }

    private boolean canPut(BlockPos at, BlockState s) {
        int x = at.getX() - b.getX(), y = at.getY() - b.getY(), z = at.getZ() - b.getZ();
        if (!free(at) || kept(x, y, z)) return false;
        if (s.getCollisionShape(l, at).isEmpty()) return true;
        return !clearZone(x, y, z) && l.getEntitiesOfClass(LivingEntity.class, new AABB(at)).isEmpty();
    }

    /** Adds one block into empty space, if nothing forbids it. */
    private boolean put(int x, int y, int z, BlockState s) {
        BlockPos at = p(x, y, z);
        if (!canPut(at, s)) return false;
        set(at, s);
        placed.add(at.immutable());
        if (!s.canSurvive(l, at)) {
            undo(journal.size() - 1);
            return false;
        }
        return true;
    }

    private boolean put(int x, int y, int z, Block block) { return put(x, y, z, block.defaultBlockState()); }

    /** A group of blocks that only makes sense whole: all of it, or none. */
    private final class Plan {
        private final List<BlockPos> cells = new ArrayList<>();
        private final List<BlockState> states = new ArrayList<>();
        Plan add(int x, int y, int z, BlockState s) { cells.add(p(x, y, z)); states.add(s); return this; }
        Plan add(int x, int y, int z, Block block) { return add(x, y, z, block.defaultBlockState()); }
        boolean place() {
            for (int i = 0; i < cells.size(); i++) if (!canPut(cells.get(i), states.get(i))) return false;
            int mark = journal.size();
            for (int i = 0; i < cells.size(); i++) {
                set(cells.get(i), states.get(i));
                placed.add(cells.get(i));
            }
            for (int i = 0; i < cells.size(); i++)
                if (!states.get(i).canSurvive(l, cells.get(i))) {
                    undo(mark);
                    return false;
                }
            return true;
        }
    }

    private void undo(int mark) {
        for (int i = journal.size() - 1; i >= mark; i--) {
            Change c = journal.remove(i);
            l.setBlock(c.pos(), c.old(), F | Block.UPDATE_SUPPRESS_DROPS);
            placed.remove(c.pos());
            changed--;
        }
    }

    /** Ordinary floor a rug or hearth may replace. */
    private static boolean floorMaterial(BlockState s) {
        return s.is(BlockTags.PLANKS) || s.is(HouseBlocks.RUG_FLOOR.get()) || s.is(Blocks.SMOOTH_STONE) || s.is(Blocks.STONE_BRICKS)
                || s.is(Blocks.POLISHED_DEEPSLATE) || s.is(Blocks.DEEPSLATE_TILES) || s.is(LiteraryRegistry.RED_FLOOR.get())
                || s.is(LiteraryRegistry.DARK_PANEL.get()) || s.is(LiteraryRegistry.SIDING.get());
    }

    /** Replaces the floor block under a standing cell, if it is ordinary floor. */
    private boolean floor(int x, int y, int z, BlockState s) {
        BlockPos at = p(x, y - 1, z);
        if (!floorMaterial(l.getBlockState(at)) || l.getBlockEntity(at) != null || VignetteArchitecture.storyReserved(scene, x, y - 1, z)) return false;
        set(at, s);
        return true;
    }

    // ------------------------------------------------------------------
    // Reachability: furniture that would seal off part of a room is taken away again.

    private int index(int x, int y, int z) {
        return ((x - r.minX()) * (r.maxY() - r.minY() + 1) + (y - r.minY())) * (r.maxZ() - r.minZ() + 1) + (z - r.minZ());
    }

    private static boolean passable(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        return state.getCollisionShape(level, pos).isEmpty() || block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock;
    }

    private BitSet reach() {
        int sx = r.maxX() - r.minX() + 1, sy = r.maxY() - r.minY() + 1, sz = r.maxZ() - r.minZ() + 1;
        BitSet seen = new BitSet(sx * sy * sz);
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        for (var door : scene.doors())
            for (Direction side : new Direction[]{door.facing(), door.facing().getOpposite()})
                for (int up = 0; up <= 1; up++) {
                    BlockPos start = door.rel().relative(side).above(up);
                    if (!r.isInside(start)) continue;
                    if (passable(l, b.offset(start), l.getBlockState(b.offset(start))) && !seen.get(index(start.getX(), start.getY(), start.getZ()))) {
                        seen.set(index(start.getX(), start.getY(), start.getZ()));
                        queue.add(new int[]{start.getX(), start.getY(), start.getZ()});
                    }
                }
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        while (!queue.isEmpty()) {
            int[] c = queue.poll();
            for (Direction d : Direction.values()) {
                int x = c[0] + d.getStepX(), y = c[1] + d.getStepY(), z = c[2] + d.getStepZ();
                if (x < r.minX() || x > r.maxX() || y < r.minY() || y > r.maxY() || z < r.minZ() || z > r.maxZ()) continue;
                int i = index(x, y, z);
                if (seen.get(i)) continue;
                at.set(b.getX() + x, b.getY() + y, b.getZ() + z);
                if (!passable(l, at, l.getBlockState(at))) continue;
                seen.set(i);
                queue.add(new int[]{x, y, z});
            }
        }
        return seen;
    }

    /** Runs one piece of furnishing, and takes it away again if it cut off anything that was reachable. */
    private void guard(Runnable work) {
        int mark = journal.size();
        work.run();
        boolean solid = false;
        for (int i = mark; i < journal.size() && !solid; i++) {
            BlockPos pos = journal.get(i).pos();
            solid = !l.getBlockState(pos).getCollisionShape(l, pos).isEmpty();
        }
        if (!solid || before == null) return;
        BitSet now = reach();
        int sy = r.maxY() - r.minY() + 1, sz = r.maxZ() - r.minZ() + 1;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = before.nextSetBit(0); i >= 0; i = before.nextSetBit(i + 1)) {
            if (now.get(i)) continue;
            int z = i % sz + r.minZ(), y = (i / sz) % sy + r.minY(), x = i / (sz * sy) + r.minX();
            at.set(b.getX() + x, b.getY() + y, b.getZ() + z);
            if (passable(l, at, l.getBlockState(at))) {
                undo(mark);
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Fittings.

    private static BlockState fence(Block block, Direction... sides) {
        BlockState s = block.defaultBlockState();
        for (Direction d : sides) s = s.setValue(side(d), true);
        return s;
    }

    private static Property<Boolean> side(Direction d) {
        return switch (d) {
            case NORTH -> CrossCollisionBlock.NORTH;
            case SOUTH -> CrossCollisionBlock.SOUTH;
            case EAST -> CrossCollisionBlock.EAST;
            default -> CrossCollisionBlock.WEST;
        };
    }

    private static BlockState lantern(boolean hanging) { return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, hanging); }
    private static BlockState candle(int n) { return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, n).setValue(CandleBlock.LIT, true); }
    private static BlockState slab(Block block, SlabType type) { return block.defaultBlockState().setValue(SlabBlock.TYPE, type); }
    private static BlockState stairs(Block block, Direction facing) { return block.defaultBlockState().setValue(StairBlock.FACING, facing); }
    private static BlockState log(Block block, Direction.Axis axis) { return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis); }
    private static BlockState leaves(Block block) { return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true); }

    private boolean solid(int x, int y, int z) {
        BlockPos at = p(x, y, z);
        return l.getBlockState(at).isFaceSturdy(l, at, Direction.UP) && l.getBlockState(at).isCollisionShapeFullBlock(l, at);
    }

    private boolean furniture(int x, int y, int z, HouseholdFurnitureBlock.Kind kind, Direction facing) {
        return put(x, y, z, HouseholdFurnitureBlock.state(kind, facing));
    }

    private boolean detail(int x, int y, int z, SceneDetailBlock.Kind kind, Direction facing) {
        BlockState s = SceneDetailBlock.state(kind, facing);
        return free(p(x, y, z)) && SceneDetailBlock.supported(l, p(x, y, z), s) && put(x, y, z, s);
    }

    /** A cabinet or table with something standing on it. */
    private void piece(int x, int y, int z, HouseholdFurnitureBlock.Kind kind, Direction facing, SceneDetailBlock.Kind top) {
        guard(() -> {
            if (furniture(x, y, z, kind, facing) && top != null) detail(x, y + 1, z, top, facing);
        });
    }

    private void seat(int x, int y, int z, HouseholdFurnitureBlock.Kind kind, Direction facing) {
        guard(() -> furniture(x, y, z, kind, facing));
    }

    /** A standing lamp: a turned post with a lantern on it. */
    private void lamp(int x, int y, int z) {
        guard(() -> new Plan().add(x, y, z, Blocks.DARK_OAK_FENCE).add(x, y + 1, z, lantern(false)).place());
    }

    /** A candlestand of three lit candles. */
    private void candlestand(int x, int y, int z) {
        guard(() -> new Plan().add(x, y, z, Blocks.DARK_OAK_FENCE).add(x, y + 1, z, candle(3)).place());
    }

    private void banner(int x, int y, int z, Direction facing, Block banner) {
        put(x, y, z, banner.defaultBlockState().setValue(WallBannerBlock.FACING, facing));
    }

    /** A rug laid into the floor, with its border, under whatever already stands there. */
    private void rug(int x0, int x1, int z0, int z1, int y, RugFloorBlock.Tone tone) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) {
                String ns = z == z0 ? "NORTH" : z == z1 ? "SOUTH" : "", ew = x == x0 ? "WEST" : x == x1 ? "EAST" : "";
                RugFloorBlock.Rim rim = RugFloorBlock.Rim.valueOf(ns.isEmpty() ? ew.isEmpty() ? "CENTER" : ew : ew.isEmpty() ? ns : ns + "_" + ew);
                if (!floor(x, y, z, HouseBlocks.RUG_FLOOR.get().defaultBlockState().setValue(RugFloorBlock.TONE, tone).setValue(RugFloorBlock.RIM, rim))) continue;
                BlockState above = at(x, y, z);
                if (above.is(BlockTags.WOOL_CARPETS)) remove(p(x, y, z));
            }
    }

    /**
     * A chandelier on a short drop from the ceiling: a four-armed fitting with a
     * lantern under each arm. Lower rooms get the fitting close under the ceiling.
     */
    private boolean chandelier(int x, int z, int y, Block metal) {
        int ceiling = Integer.MIN_VALUE;
        for (int h = 2; h <= 16; h++) {
            BlockState s = at(x, y + h, z);
            if (s.isAir()) continue;
            if (!s.is(Blocks.CHAIN) && Block.canSupportCenter(l, p(x, y + h, z), Direction.DOWN)) ceiling = y + h;
            break;
        }
        if (ceiling == Integer.MIN_VALUE || ceiling - y < 5) return false;
        int drop = Math.min(3, Math.max(0, ceiling - y - 6)), hub = ceiling - 1 - drop;
        Plan plan = new Plan();
        for (int c = hub + 1; c < ceiling; c++) plan.add(x, c, z, Blocks.CHAIN);
        plan.add(x, hub, z, fence(metal, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        for (Direction d : Direction.Plane.HORIZONTAL) {
            plan.add(x + d.getStepX(), hub, z + d.getStepZ(), fence(metal, d.getOpposite()));
            plan.add(x + d.getStepX(), hub - 1, z + d.getStepZ(), lantern(true));
        }
        return plan.place();
    }

    /** A bracket lamp: a short arm out from a post with a lantern hanging from it. */
    private boolean sconce(int x, int z, Direction toWall, int lampY) {
        return new Plan().add(x, lampY + 1, z, fence(Blocks.DARK_OAK_FENCE, toWall)).add(x, lampY, z, lantern(true)).place();
    }

    /** Bracket lamps on every other timber post of a framed room. */
    private void sconces(Rect room) {
        int lampY = room.y() + (room.h() >= 8 ? 3 : 2);
        if (lampY + 1 >= room.y() + room.h()) return;
        int k = 0;
        for (int z = room.z0() + 2; z < room.z1(); z += 5, k++) {
            if (k % 2 != 0) continue;
            if (post(room.x0(), room.y() + 2, z)) sconce(room.x0() + 1, z, Direction.WEST, lampY);
            if (post(room.x1(), room.y() + 2, z)) sconce(room.x1() - 1, z, Direction.EAST, lampY);
        }
        k = 0;
        for (int x = room.x0() + 2; x < room.x1(); x += 5, k++) {
            if (k % 2 != 1) continue;
            if (post(x, room.y() + 2, room.z0())) sconce(x, room.z0() + 1, Direction.NORTH, lampY);
            if (post(x, room.y() + 2, room.z1())) sconce(x, room.z1() - 1, Direction.SOUTH, lampY);
        }
    }

    private boolean post(int x, int y, int z) {
        return at(x, y, z).is(Blocks.STRIPPED_DARK_OAK_LOG);
    }

    /**
     * A fireplace against a wall: a chimney breast to the ceiling, a lit grate,
     * a hearthstone in front, a mantel shelf with candles and a picture above it.
     * It is tried along the wall either side of where it was asked for; if every
     * place is taken, the generic pass's ornaments and themed furniture make way.
     */
    private boolean fireplace(int x, int z, Direction out, int y, int top, Block breast, Block hearth) {
        Direction along = out.getClockWise();
        for (boolean displace : new boolean[]{false, true})
            for (int shift : new int[]{0, 1, -1, 2, -2, 3, -3, 4, -4})
                if (hearth(x + along.getStepX() * shift, z + along.getStepZ() * shift, out, y, top, breast, hearth, displace)) return true;
        return false;
    }

    /** What the generic dressing pass put down, which a composed fitting may move aside. */
    private boolean displaceable(BlockPos at) {
        BlockState s = l.getBlockState(at);
        if (l.getBlockEntity(at) != null) return false;
        if (s.is(HouseBlocks.SCENE_DETAIL.get()) || s.is(Blocks.COBWEB)) return true;
        var theme = SceneDressing.theme(scene);
        if (theme == null) return false;
        if (s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())) return theme.furniture().contains(s.getValue(HouseholdFurnitureBlock.KIND));
        return theme.shelves() && s.is(Blocks.BOOKSHELF);
    }

    private boolean hearth(int x, int z, Direction out, int y, int top, Block breast, Block hearth, boolean displace) {
        if (!solid(x - out.getStepX(), y + 1, z - out.getStepZ())) return false;
        Direction along = out.getClockWise();
        int ax = along.getStepX(), az = along.getStepZ(), mx = x + out.getStepX(), mz = z + out.getStepZ();
        Plan plan = new Plan();
        for (int a = -1; a <= 1; a++)
            for (int h = y; h < top; h++) {
                if (a == 0 && h <= y + 1) continue;
                plan.add(x + ax * a, h, z + az * a, breast);
            }
        plan.add(x, y, z, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true).setValue(CampfireBlock.FACING, out));
        for (int a = -1; a <= 1; a++) plan.add(mx + ax * a, y + 2, mz + az * a, slab(Blocks.DARK_OAK_SLAB, SlabType.TOP));
        boolean[] built = {false};
        guard(() -> {
            int mark = journal.size();
            if (displace)
                for (BlockPos cell : plan.cells)
                    if (!free(cell) && displaceable(cell) && !kept(cell.getX() - b.getX(), cell.getY() - b.getY(), cell.getZ() - b.getZ())) set(cell, AIR);
            BlockPos opening = p(x, y + 1, z);
            if (displace && displaceable(opening)) set(opening, AIR);
            built[0] = free(opening) && plan.place();
            if (!built[0]) undo(mark);
        });
        if (!built[0]) return false;
        for (int a = -1; a <= 1; a++) floor(mx + ax * a, y, mz + az * a, hearth.defaultBlockState());
        for (int a : new int[]{-1, 1}) put(mx + ax * a, y + 3, mz + az * a, candle(a < 0 ? 2 : 3));
        detail(mx, y + 3, mz, SceneDetailBlock.Kind.FRAME, out);
        return true;
    }

    /** Built-in shelving along a wall, stacked on the floor or on what is already there, under a slab cornice. */
    private void shelves(int x, int z, Direction along, int length, Direction toWall, int y, int height) {
        for (int i = 0; i < length; i++) {
            int cx = x + along.getStepX() * i, cz = z + along.getStepZ() * i;
            if (!solid(cx + toWall.getStepX(), y + 1, cz + toWall.getStepZ())) continue;
            guard(() -> {
                boolean any = false;
                for (int h = 0; h < height; h++) {
                    BlockState below = at(cx, y + h - 1, cz);
                    if (h > 0 && !below.is(Blocks.BOOKSHELF) && !below.is(Blocks.BARREL)) break;
                    if (free(p(cx, y + h, cz))) any |= put(cx, y + h, cz, Blocks.BOOKSHELF);
                }
                if (any && at(cx, y + height - 1, cz).is(Blocks.BOOKSHELF)) put(cx, y + height, cz, slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
            });
        }
    }

    // ------------------------------------------------------------------
    // Interiors.

    private List<Rect> rooms() {
        return switch (scene) {
            case HILL_NURSERY -> List.of(new Rect(-3, 3, -43, 0, 0, 5), new Rect(-14, -4, -24, -10, 0, 6), new Rect(4, 14, -24, -10, 0, 6), new Rect(-13, 13, -42, -30, -4, 3));
            case MINIATURES -> List.of(new Rect(-14, 14, -30, 0, 0, 7));
            case MASQUE -> {
                List<Rect> chambers = new ArrayList<>();
                for (int i = 0; i < 7; i++) chambers.add(new Rect(-10, 10, -11 * (i + 1), -11 * (i + 1) + 10, 0, 8));
                yield chambers;
            }
            case USHER -> List.of(new Rect(-17, 17, -41, 0, 0, 10), new Rect(-8, 8, -35, -20, -3, 3));
            case WINCHESTER -> List.of(new Rect(-30, 30, -77, 0, 0, 15));
            case CHILD_ROOM -> List.of(new Rect(-11, 11, -24, 0, 0, 6));
            case CRIMSON_HALL -> List.of(new Rect(-18, 18, -43, 0, 0, 13));
            case BLY_ROUTE -> List.of(new Rect(-12, 12, -53, 0, 0, 7));
            case ELK_FAN -> List.of(new Rect(-11, 11, -28, 0, 0, 9));
            case CONFESSION -> List.of(new Rect(-12, 12, -33, 0, 0, 9));
            case DEVILS_ROCK -> List.of(new Rect(-16, 16, -42, 0, 0, 8), new Rect(-14, -3, -38, -20, 0, 4));
            case WHEEL -> List.of(new Rect(-26, 26, -64, 0, 0, 9));
            case GHOSTS_SET -> List.of(new Rect(-20, 20, -46, 0, 0, 10), new Rect(-16, 9, -39, -14, 0, 5), new Rect(12, 18, -42, -31, 0, 4));
            default -> List.of();
        };
    }

    /** The lanterns each room was authored with; every other hanging lantern came from the generic lighting pass. */
    private List<BlockPos> authoredLamps() {
        return switch (scene) {
            case HILL_NURSERY -> List.of(new BlockPos(0, 3, -8), new BlockPos(0, 3, -36));
            case MINIATURES -> List.of(new BlockPos(-8, 4, -18), new BlockPos(0, 4, -18), new BlockPos(8, 4, -18));
            case MASQUE -> List.of(new BlockPos(0, 5, -6));
            case USHER -> List.of(new BlockPos(0, 5, -8), new BlockPos(0, 2, -25));
            case WINCHESTER -> List.of(new BlockPos(0, 11, -8), new BlockPos(0, 11, -26), new BlockPos(0, 11, -44), new BlockPos(0, 11, -60));
            case CHILD_ROOM -> List.of(new BlockPos(7, 3, -6));
            case CRIMSON_HALL -> List.of(new BlockPos(0, 8, -10));
            case BLY_ROUTE -> List.of(new BlockPos(0, 4, -6), new BlockPos(0, 4, -17), new BlockPos(0, 4, -28));
            case CONFESSION -> List.of(new BlockPos(0, 4, -21));
            case DEVILS_ROCK -> List.of(new BlockPos(0, 4, -8), new BlockPos(8, 4, -32));
            case WHEEL -> List.of(new BlockPos(0, 4, -9), new BlockPos(0, 4, -29), new BlockPos(0, 4, -48));
            case GHOSTS_SET -> List.of(new BlockPos(0, 5, -8));
            default -> List.of();
        };
    }

    private void interior() {
        tidy();
        before = reach();
        switch (scene) {
            case HILL_NURSERY -> hill();
            case MINIATURES -> miniatures();
            case MASQUE -> masque();
            case USHER -> usher();
            case WINCHESTER -> { }
            case CHILD_ROOM -> child();
            case CRIMSON_HALL -> crimson();
            case BLY_ROUTE -> bly();
            case ELK_FAN -> fan();
            case CONFESSION -> confession();
            case DEVILS_ROCK -> diary();
            case WHEEL -> wheel();
            case GHOSTS_SET -> studio();
            default -> { }
        }
        if (scene != LabyrinthPlace.MASQUE) for (Rect room : rooms()) sconces(room);
        before = null;
        // Whatever the composed lighting leaves too dark is lit as before.
        changed += ScenePolish.light(l, b, scene, ScenePolish.darkAllowed(scene));
    }

    /**
     * Takes away what the generic pass left that does not belong: the isolated
     * squares of carpet it laid in a lattice, its scattered ceiling pendants, and
     * anything it stood on top of a ceiling.
     */
    private void tidy() {
        var theme = SceneDressing.theme(scene);
        Block rug = theme == null ? null : theme.rug();
        Set<BlockPos> authored = new HashSet<>();
        for (BlockPos lamp : authoredLamps()) authored.add(b.offset(lamp));
        for (BlockPos pos : BlockPos.betweenClosed(p(r.minX(), r.minY(), r.minZ()), p(r.maxX(), r.maxY(), r.maxZ()))) {
            BlockState s = l.getBlockState(pos);
            if (rug != null && s.is(rug) && isolated(pos, rug) && !l.getBlockState(pos.below()).is(Blocks.SCULK_SENSOR)) remove(pos.immutable());
            else if (s.is(Blocks.LANTERN) && s.getValue(LanternBlock.HANGING) && !authored.contains(pos)
                    && !VignetteArchitecture.storyReserved(scene, pos.getX() - b.getX(), pos.getY() - b.getY(), pos.getZ() - b.getZ())) {
                BlockPos lamp = pos.immutable();
                remove(lamp);
                for (BlockPos up = lamp.above(); l.getBlockState(up).is(Blocks.CHAIN); up = up.above()) remove(up);
            }
        }
        for (Rect room : rooms())
            for (int x = room.x0(); x <= room.x1(); x++)
                for (int z = room.z0(); z <= room.z1(); z++) {
                    BlockPos above = p(x, room.y() + room.h() + 1, z);
                    BlockState s = l.getBlockState(above);
                    if (insideAny(x, room.y() + room.h() + 1, z)) continue;
                    if (s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get()) || s.is(HouseBlocks.SCENE_DETAIL.get()) || s.is(BlockTags.WOOL_CARPETS))
                        remove(above);
                }
    }

    private boolean insideAny(int x, int y, int z) {
        for (Rect room : rooms()) if (room.inside(x, y, z)) return true;
        return false;
    }

    private boolean isolated(BlockPos pos, Block block) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                if ((dx != 0 || dz != 0) && l.getBlockState(pos.offset(dx, 0, dz)).is(block)) return false;
        return true;
    }

    private void hill() {
        // The ending ledger was authored on top of the cellar's roof, outside every room:
        // it moves down into the cellar the counted knocks come from.
        if (at(8, 1, -36).is(LiteraryRegistry.PROP.get()) && at(8, 1, -36).getValue(LiteraryPropBlock.KIND) == LiteraryPropBlock.Kind.LEDGER) remove(p(8, 1, -36));
        for (BlockPos old : List.of(new BlockPos(8, 0, -36), new BlockPos(8, 0, -34)))
            if (at(old.getX(), old.getY(), old.getZ()).is(HouseBlocks.HOUSEHOLD_FURNITURE.get())) remove(b.offset(old));
        BlockPos ending = LiteraryRooms.ending(scene);
        if (at(ending.getX(), ending.getY(), ending.getZ()).isAir() && at(ending.getX(), ending.getY() - 1, ending.getZ()).isAir()) {
            set(p(ending.getX(), ending.getY() - 1, ending.getZ()), HouseholdFurnitureBlock.state(WALNUT_DESK, Direction.SOUTH));
            if (at(ending.getX(), ending.getY() - 1, ending.getZ() + 2).isAir())
                set(p(ending.getX(), ending.getY() - 1, ending.getZ() + 2), HouseholdFurnitureBlock.state(CANE_CHAIR, Direction.NORTH));
            set(b.offset(ending), LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND, LiteraryPropBlock.Kind.LEDGER)
                    .setValue(LiteraryPropBlock.FACING, Direction.SOUTH));
        }
        // Whatever the generic pass stood out on the cellar roof goes.
        for (BlockPos pos : BlockPos.betweenClosed(p(-13, 0, -42), p(13, 3, -25))) {
            int x = pos.getX() - b.getX();
            if (Math.abs(x) <= 3) continue;
            BlockState s = l.getBlockState(pos);
            if (s.is(HouseBlocks.HOUSEHOLD_FURNITURE.get()) || s.is(HouseBlocks.SCENE_DETAIL.get()) || s.is(BlockTags.WOOL_CARPETS) || s.is(Blocks.CHAIN)
                    || s.is(Blocks.LANTERN)) remove(pos.immutable());
        }
        rug(-1, 1, -23, -2, 0, RED);
        for (int s : new int[]{-1, 1}) {
            Direction in = s < 0 ? Direction.EAST : Direction.WEST;
            rug(s < 0 ? -12 : 7, s < 0 ? -7 : 12, -22, -13, 0, s < 0 ? GRAY : BROWN);
            piece(s * 6, 0, -23, CHEST_OF_DRAWERS, Direction.SOUTH, SceneDetailBlock.Kind.TOYS);
            shelves(s < 0 ? -6 : 6, -11, Direction.EAST, 2, Direction.SOUTH, 0, 2);
            chandelier(s * 9, -17, 0, Blocks.DARK_OAK_FENCE);
            detail(s * 13, 2, -14, SceneDetailBlock.Kind.FRAME, in);
            detail(s * 13, 2, -20, SceneDetailBlock.Kind.CLOCK, in);
        }
        piece(-9, 0, -23, BEDSIDE_TABLE, Direction.SOUTH, SceneDetailBlock.Kind.TABLE_LAMP);
        seat(-12, 0, -21, CANE_CHAIR, Direction.EAST);
        // The cellar: casks along the far wall, two high, with a lamp on each stack's end.
        for (int x = -11; x <= 11; x++) {
            if (Math.abs(x) < 4) continue;
            int xx = x;
            guard(() -> {
                if (put(xx, -4, -41, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.SOUTH)))
                    put(xx, -3, -41, Blocks.BARREL.defaultBlockState().setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.SOUTH));
            });
        }
        for (int x : new int[]{-4, 4}) put(x, -2, -41, lantern(false));
        for (int x : new int[]{-12, 12}) for (int z : new int[]{-41, -31}) put(x, -2, z, Blocks.COBWEB);
        for (int z : new int[]{-38, -34}) detail(11, -4, z, SceneDetailBlock.Kind.CRATE, Direction.WEST);
    }

    private void miniatures() {
        for (int x : new int[]{-8, 0, 8}) rug(x - 2, x + 2, -20, -13, 0, BROWN);
        rug(-2, 2, -27, -22, 0, RED);
        // A long bench under the display window, cabinets between the desks.
        SceneDetailBlock.Kind[] tops = {SceneDetailBlock.Kind.INK_PAPERS, SceneDetailBlock.Kind.TOOLS, SceneDetailBlock.Kind.BOTTLES, SceneDetailBlock.Kind.BOOKS};
        for (int x = -7; x <= 7; x++) piece(x, 0, -29, x % 2 == 0 ? WALNUT_DESK : CHEST_OF_DRAWERS, Direction.SOUTH, Math.abs(x) == 7 ? SceneDetailBlock.Kind.TABLE_LAMP : tops[Math.floorMod(x, 4)]);
        // Shelving fills the wall behind the west cabinets and joins the east wall's barrels into one run.
        shelves(-13, -28, Direction.SOUTH, 20, Direction.WEST, 0, 3);
        shelves(13, -27, Direction.SOUTH, 19, Direction.EAST, 0, 3);
        // The workshop hearth and a reading corner before it.
        rug(9, 12, -27, -23, 0, BROWN);
        fireplace(11, -29, Direction.SOUTH, 0, 7, Blocks.BRICKS, Blocks.SMOOTH_STONE);
        seat(10, 0, -25, GREEN_ARMCHAIR, Direction.NORTH);
        seat(12, 0, -25, FLORAL_ARMCHAIR, Direction.NORTH);
        piece(11, 0, -25, BEDSIDE_TABLE, Direction.NORTH, SceneDetailBlock.Kind.TEA_SET);
        lamp(9, 0, -28);
        chandelier(0, -8, 0, Blocks.DARK_OAK_FENCE);
        chandelier(0, -26, 0, Blocks.DARK_OAK_FENCE);
    }

    private void masque() {
        Block[] carpets = {Blocks.BLUE_CARPET, Blocks.PURPLE_CARPET, Blocks.GREEN_CARPET, Blocks.ORANGE_CARPET, Blocks.WHITE_CARPET, Blocks.MAGENTA_CARPET};
        for (int i = 0; i < 7; i++) {
            int z0 = -11 * (i + 1);
            if (i < carpets.length)
                for (int x = -3; x <= 3; x++) for (int z = z0 + 2; z <= z0 + 8; z++) put(x, 0, z, carpets[i]);
            // The last chamber keeps only its own red light.
            if (i == 6) continue;
            for (int x : new int[]{-8, 8}) for (int z : new int[]{z0 + 2, z0 + 8}) candlestand(x, 0, z);
            detail(7, 1, z0 + 7, i % 2 == 0 ? SceneDetailBlock.Kind.BOTTLES : SceneDetailBlock.Kind.VASE, Direction.WEST);
        }
    }

    private void usher() {
        // The crypt's roof stands a step above the hall floor: make it a dais, resurfaced and stepped.
        for (int x = -7; x <= 7; x++)
            for (int z = -34; z <= -21; z++) {
                BlockState s = at(x, 0, z);
                if ((s.is(Blocks.BIRCH_PLANKS) || s.is(Blocks.DARK_OAK_PLANKS)) && l.getBlockEntity(p(x, 0, z)) == null) set(p(x, 0, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
        rug(-5, 5, -32, -23, 1, SLATE);
        for (int z = -35; z <= -20; z++) {
            int zz = z;
            guard(() -> put(-9, 0, zz, stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.EAST)));
            guard(() -> put(9, 0, zz, stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.WEST)));
        }
        for (int x = -8; x <= 8; x++) {
            int xx = x;
            guard(() -> put(xx, 0, -36, stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.SOUTH)));
            if (Math.abs(x) > 2) guard(() -> put(xx, 0, -19, stairs(Blocks.DEEPSLATE_BRICK_STAIRS, Direction.NORTH)));
        }
        for (int x : new int[]{-7, 7}) for (int z : new int[]{-34, -21}) candlestand(x, 1, z);
        // A hearth on the west wall with chairs drawn up to it.
        rug(-14, -10, -31, -25, 0, BROWN);
        fireplace(-16, -28, Direction.EAST, 0, 10, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.DEEPSLATE_TILES);
        seat(-12, 0, -29, GREEN_ARMCHAIR, Direction.WEST);
        seat(-12, 0, -27, GREEN_ARMCHAIR, Direction.WEST);
        piece(-12, 0, -28, BEDSIDE_TABLE, Direction.WEST, SceneDetailBlock.Kind.BOOKS);
        // Hangings on the long walls.
        Block[] hangings = {Blocks.BLACK_WALL_BANNER, Blocks.GRAY_WALL_BANNER};
        int k = 0;
        for (int z : new int[]{-37, -27, -17, -7}) banner(16, 5, z, Direction.WEST, hangings[k++ % 2]);
        for (int x : new int[]{-12, -6, 6}) banner(x, 5, -40, Direction.SOUTH, hangings[k++ % 2]);
        chandelier(-8, -10, 0, Blocks.IRON_BARS);
        chandelier(8, -10, 0, Blocks.IRON_BARS);
    }

    private void child() {
        rug(-4, 4, -10, -4, 0, BROWN);
        // A kitchenette along the west wall, a table with stools, and a chest for clothes.
        SceneDetailBlock.Kind[] tops = {SceneDetailBlock.Kind.CROCK, SceneDetailBlock.Kind.DISH_RACK, SceneDetailBlock.Kind.TEA_SET, SceneDetailBlock.Kind.BOTTLES};
        for (int z = -5; z <= -2; z++) piece(-10, 0, z, CHEST_OF_DRAWERS, Direction.EAST, tops[z + 5]);
        piece(-7, 0, -7, FORMICA_TABLE, Direction.SOUTH, SceneDetailBlock.Kind.TEA_SET);
        seat(-7, 0, -6, KITCHEN_STOOL, Direction.NORTH);
        seat(-6, 0, -7, KITCHEN_STOOL, Direction.WEST);
        piece(10, 0, -7, CHEST_OF_DRAWERS, Direction.WEST, SceneDetailBlock.Kind.BLANKET);
        piece(10, 0, -5, BEDSIDE_TABLE, Direction.WEST, SceneDetailBlock.Kind.BOOKS);
        put(8, 0, -23, Blocks.POTTED_FERN);
        detail(-10, 2, -15, SceneDetailBlock.Kind.FRAME, Direction.EAST);
    }

    private void crimson() {
        rug(-2, 2, -26, -2, 0, RED);
        // The hearth rises the full height of the north wall, behind the gallery.
        rug(-11, -5, -40, -36, 0, RED);
        fireplace(-8, -42, Direction.SOUTH, 0, 13, Blocks.BRICKS, Blocks.POLISHED_BLACKSTONE);
        seat(-10, 0, -38, FLORAL_ARMCHAIR, Direction.NORTH);
        seat(-6, 0, -38, FLORAL_ARMCHAIR, Direction.NORTH);
        piece(-8, 0, -38, BEDSIDE_TABLE, Direction.NORTH, SceneDetailBlock.Kind.TEA_SET);
        // A second chair across from the authored one, with a table between them.
        rug(4, 10, -15, -9, 0, BROWN);
        seat(5, 0, -12, FLORAL_ARMCHAIR, Direction.EAST);
        piece(7, 0, -12, BEDSIDE_TABLE, Direction.NORTH, SceneDetailBlock.Kind.VASE);
        for (int x : new int[]{-12, -6, 6, 12}) banner(x, 10, -42, Direction.SOUTH, Blocks.RED_WALL_BANNER);
        for (int z : new int[]{-30, -22, -14}) {
            detail(-17, 3, z, SceneDetailBlock.Kind.FRAME, Direction.EAST);
            detail(17, 3, z, SceneDetailBlock.Kind.FRAME, Direction.WEST);
        }
        chandelier(-7, -14, 0, Blocks.IRON_BARS);
        chandelier(7, -14, 0, Blocks.IRON_BARS);
        chandelier(0, -33, 0, Blocks.IRON_BARS);
    }

    private void bly() {
        rug(-2, 2, -38, -2, 0, RED);
        // The garden pool gets a kerb, plants at its corners and benches facing it.
        for (int z = -51; z <= -40; z++) for (int x : new int[]{-2, 2}) put(x, 0, z, slab(Blocks.SMOOTH_STONE_SLAB, SlabType.BOTTOM));
        for (int x = -1; x <= 1; x++) for (int z : new int[]{-51, -40}) put(x, 0, z, slab(Blocks.SMOOTH_STONE_SLAB, SlabType.BOTTOM));
        for (int x : new int[]{-3, 3}) for (int z : new int[]{-51, -40}) put(x, 0, z, Blocks.POTTED_FERN);
        for (int z : new int[]{-46, -45}) {
            int zz = z;
            guard(() -> put(-4, 0, zz, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST)));
            guard(() -> put(4, 0, zz, stairs(Blocks.SPRUCE_STAIRS, Direction.EAST)));
        }
        // Each small bedroom gets a bed against its far wall and a lamp on its chest.
        for (int x : new int[]{-6, 6})
            for (int z : new int[]{-9, -20, -31}) {
                int bed = x + (x < 0 ? -1 : 1), zz = z;
                rug(x - 1, x + 1, z - 1, z + 1, 0, GRAY);
                guard(() -> new Plan()
                        .add(bed, 0, zz, Blocks.WHITE_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, Direction.NORTH))
                        .add(bed, 0, zz - 1, Blocks.WHITE_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, Direction.NORTH)
                                .setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD)).place());
                detail(x, 1, z - 2, SceneDetailBlock.Kind.TABLE_LAMP, Direction.SOUTH);
            }
        // A study wall of books by the desk, and a hearth with chairs opposite.
        shelves(11, -44, Direction.SOUTH, 11, Direction.EAST, 0, 3);
        rug(-10, -6, -41, -35, 0, BROWN);
        fireplace(-11, -38, Direction.EAST, 0, 7, Blocks.STONE_BRICKS, Blocks.SMOOTH_STONE);
        seat(-8, 0, -39, GREEN_ARMCHAIR, Direction.WEST);
        seat(-8, 0, -37, GREEN_ARMCHAIR, Direction.WEST);
        piece(-8, 0, -38, BEDSIDE_TABLE, Direction.WEST, SceneDetailBlock.Kind.BOOKS);
    }

    private void fan() {
        // The free-standing hearth becomes a chimney with a mantel.
        for (int y = 3; y < 9; y++) for (int x = 7; x <= 8; x++) put(x, y, -23, Blocks.STONE_BRICKS);
        for (int x = 6; x <= 9; x++) put(x, 3, -22, slab(Blocks.STONE_BRICK_SLAB, SlabType.TOP));
        for (int x : new int[]{6, 9}) put(x, 4, -22, candle(2));
        // A sofa and a footstool facing the fire.
        rug(5, 9, -21, -16, 0, BROWN);
        for (int x = 6; x <= 8; x++) seat(x, 0, -18, BLUE_SOFA, Direction.NORTH);
        seat(7, 0, -20, FOOTSTOOL, Direction.NORTH);
        // The armchair gets a lamp table; the kitchen table gets stools.
        piece(-7, 0, -22, BEDSIDE_TABLE, Direction.EAST, SceneDetailBlock.Kind.TABLE_LAMP);
        lamp(-9, 0, -23);
        seat(-6, 0, -13, KITCHEN_STOOL, Direction.WEST);
        seat(-7, 0, -12, KITCHEN_STOOL, Direction.NORTH);
        seat(-7, 0, -14, KITCHEN_STOOL, Direction.SOUTH);
        shelves(-10, -9, Direction.SOUTH, 6, Direction.WEST, 0, 2);
        lamp(9, 0, -14);
    }

    private void confession() {
        // The low shelving along both walls rises into full-height bookcases.
        for (int z = -29; z <= -9; z++)
            for (int x : new int[]{-11, 11}) {
                if (!at(x, 0, z).is(Blocks.BOOKSHELF)) continue;
                int xx = x, zz = z;
                guard(() -> {
                    for (int y = 1; y <= 3; y++) if (!put(xx, y, zz, Blocks.BOOKSHELF)) return;
                    put(xx, 4, zz, slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
                });
            }
        rug(-1, 3, -24, -15, 0, RED);
        piece(3, 0, -20, BEDSIDE_TABLE, Direction.WEST, SceneDetailBlock.Kind.TABLE_LAMP);
        fireplace(-8, -32, Direction.SOUTH, 0, 9, Blocks.DEEPSLATE_BRICKS, Blocks.SMOOTH_STONE);
        chandelier(0, -8, 0, Blocks.DARK_OAK_FENCE);
        lamp(-11, 0, -31);
        lamp(9, 0, -3);
    }

    private void diary() {
        rug(-12, -7, -36, -31, 0, GRAY);
        piece(-9, 0, -34, BEDSIDE_TABLE, Direction.SOUTH, SceneDetailBlock.Kind.TABLE_LAMP);
        shelves(-13, -24, Direction.SOUTH, 3, Direction.WEST, 0, 2);
        for (int[] corner : new int[][]{{-15, -2}, {15, -2}, {15, -41}, {-1, -41}}) lamp(corner[0], 0, corner[1]);
        chandelier(-8, -10, 0, Blocks.DARK_OAK_FENCE);
        chandelier(8, -12, 0, Blocks.DARK_OAK_FENCE);
        detail(15, 2, -24, SceneDetailBlock.Kind.CLOCK, Direction.WEST);
        detail(-15, 2, -10, SceneDetailBlock.Kind.FRAME, Direction.EAST);
    }

    private void wheel() {
        for (int s : new int[]{-1, 1}) {
            Direction in = s > 0 ? Direction.WEST : Direction.EAST, outward = in.getOpposite();
            // The entry bay: a hall table, a lamp, a bench and a coat on the wall.
            piece(s * 22, 0, -2, CHEST_OF_DRAWERS, Direction.NORTH, SceneDetailBlock.Kind.VASE);
            lamp(s * 24, 0, -2);
            for (int z = -11; z <= -9; z++) {
                int zz = z;
                guard(() -> put(s * 25, 0, zz, stairs(Blocks.SPRUCE_STAIRS, outward)));
            }
            detail(s * 25, 2, -6, SceneDetailBlock.Kind.COAT, in);
            // The sitting room: a sofa against the outer wall, a footstool, an armchair across.
            rug(s > 0 ? 19 : -24, s > 0 ? 24 : -19, -31, -20, 0, BROWN);
            for (int z = -27; z <= -25; z++) seat(s * 24, 0, z, BLUE_SOFA, in);
            seat(s * 22, 0, -26, FOOTSTOOL, in);
            seat(s * 20, 0, -26, GREEN_ARMCHAIR, outward);
            lamp(s * 24, 0, -29);
            piece(s * 24, 0, -23, BEDSIDE_TABLE, in, SceneDetailBlock.Kind.TABLE_LAMP);
            // The study: a wall of books and a reading chair.
            shelves(s * 25, -49, Direction.SOUTH, 14, outward, 0, 3);
            rug(s > 0 ? 19 : -23, s > 0 ? 23 : -19, -44, -37, 0, RED);
            seat(s * 21, 0, -40, GREEN_ARMCHAIR, outward);
            piece(s * 21, 0, -42, BEDSIDE_TABLE, outward, SceneDetailBlock.Kind.BOOKS);
            // The bedroom: drawers and a rug by the bed.
            for (int z : new int[]{-62, -61}) piece(s * 25, 0, z, CHEST_OF_DRAWERS, in, z == -62 ? SceneDetailBlock.Kind.TOWELS : SceneDetailBlock.Kind.VASE);
            rug(s > 0 ? 12 : -16, s > 0 ? 16 : -12, -63, -57, 0, GRAY);
            for (int z : new int[]{-9, -25, -42, -58}) chandelier(s * 20, z, 0, Blocks.DARK_OAK_FENCE);
        }
    }

    private void studio() {
        // A lighting truss across the studio, lamps hung from it.
        for (int z : new int[]{-6, -43})
            for (int x = -18; x <= 18; x++) {
                Direction[] sides = x == -18 ? new Direction[]{Direction.EAST} : x == 18 ? new Direction[]{Direction.WEST} : new Direction[]{Direction.EAST, Direction.WEST};
                if (put(x, 9, z, fence(Blocks.IRON_BARS, sides)) && Math.floorMod(x, 4) == 2) put(x, 8, z, lantern(true));
            }
        // Crew chairs by the set door, catering by the entrance, flight cases in the corner.
        seat(-5, 0, -10, CANE_CHAIR, Direction.NORTH);
        seat(-3, 0, -10, CANE_CHAIR, Direction.NORTH);
        piece(14, 0, -3, FORMICA_TABLE, Direction.NORTH, SceneDetailBlock.Kind.TEA_SET);
        piece(15, 0, -3, FORMICA_TABLE, Direction.NORTH, SceneDetailBlock.Kind.BOTTLES);
        seat(14, 0, -5, KITCHEN_STOOL, Direction.SOUTH);
        for (int[] crate : new int[][]{{-19, -3}, {-19, -4}, {-18, -3}, {-19, -44}, {-18, -44}, {19, -44}}) detail(crate[0], 0, crate[1], SceneDetailBlock.Kind.CRATE, Direction.SOUTH);
        for (int[] coil : new int[][]{{-15, -9}, {15, -25}, {-15, -25}}) detail(coil[0], 0, coil[1], SceneDetailBlock.Kind.ROPE_COIL, Direction.SOUTH);
        // The family's set: a rug under the sitting group, a lamp, shelves on its flats.
        rug(-12, -2, -35, -25, 0, BROWN);
        lamp(-14, 0, -37);
        shelves(-15, -37, Direction.SOUTH, 4, Direction.WEST, 0, 2);
        for (int z : new int[]{-30, -22}) detail(-15, 2, z, SceneDetailBlock.Kind.FRAME, Direction.EAST);
        chandelier(-6, -21, 0, Blocks.DARK_OAK_FENCE);
    }

    // ------------------------------------------------------------------
    // Outdoors.

    private void grounds() {
        switch (scene) {
            case ELK_LOT -> { fellGrid(); meadow(); woods(5, new int[]{70, 0, 0, 30, 0}, false); undergrowth(); bar(); }
            case MAPPING_INTERIOR -> { fellGrid(); woods(5, new int[]{60, 20, 0, 20, 0}, false); undergrowth(); lodge(); }
            case HOLY_RABBIT -> { fellGrid(); woods(5, new int[]{75, 20, 0, 0, 5}, true); undergrowth(); }
            case ELK_CARCASSES -> { fellGrid(); woods(4, new int[]{65, 20, 0, 0, 15}, false); undergrowth(); }
            case CAMP_BLOOD -> { fellGrid(); woods(5, new int[]{30, 0, 40, 30, 0}, false); undergrowth(); cabins(); campGround(); }
            case END_WORLD_CABIN -> { fellGrid(); woods(5, new int[]{55, 10, 10, 25, 0}, false); undergrowth(); endCabin(); }
            case COSTUME_NIGHT, MOVIE_NIGHT, WINTER_LAKE -> {
                fellGrid();
                beach();
                shores();
                woods(4, scene == LabyrinthPlace.WINTER_LAKE ? new int[]{90, 10, 0, 0, 0} : new int[]{40, 0, 35, 25, 0}, scene == LabyrinthPlace.WINTER_LAKE);
                undergrowth();
                reeds();
            }
            case SHALLOWS, PHONE_CANOE, DROWNED_TOWN -> banks();
            case HOTEL_GROUNDS -> hotelGrounds();
            default -> { }
        }
    }

    /** The source and ending of a literary scene, which its woods and features keep clear of. */
    private boolean nearStory(int x, int z, int radius) {
        if (!LiteraryRooms.isLiterary(scene)) return false;
        for (BlockPos k : List.of(LiteraryRooms.source(scene), LiteraryRooms.ending(scene)))
            if (Math.abs(k.getX() - x) <= radius && Math.abs(k.getZ() - z) <= radius) return true;
        return false;
    }

    /** Where trees, boulders and fallen logs may not go: the walks, clearings and story ground of each scene. */
    private boolean open(int x, int z) {
        if (Math.abs(x) <= 7 || z >= -9 || nearStory(x, z, 6) || LiteraryRooms.buildingClearing(scene, x, z)) return true;
        return switch (scene) {
            case ELK_LOT -> z >= -38 || Math.abs(x) <= 18 && z >= -102;
            case HOLY_RABBIT -> Math.abs(x) <= 12 && z >= -42 && z <= -8;
            case ELK_CARCASSES -> Math.abs(x) <= 12 && z >= -50 && z <= -18 || Math.abs(x) <= 4 && z <= -60;
            case CAMP_BLOOD -> Math.abs(x - 6) <= 5 && Math.abs(z + 45) <= 5 || Math.abs(x - 12) <= 4 && Math.abs(z + 40) <= 4;
            case END_WORLD_CABIN -> x >= 3 && x <= 10 && z <= -38 && z >= -52;
            default -> false;
        };
    }

    private static boolean natural(BlockState s) {
        return s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.PODZOL) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.MOSS_BLOCK);
    }

    /** The natural ground surface of a column, or MIN_VALUE where it is water, path, floor or nothing. */
    private int ground(int x, int z) {
        for (int y = 10; y >= -6; y--) {
            BlockState s = at(x, y, z);
            if (s.isAir() || plant(s)) continue;
            return natural(s) ? y : Integer.MIN_VALUE;
        }
        return Integer.MIN_VALUE;
    }

    /** Takes down the planted grid of identical spruces the scene was built with. */
    private void fellGrid() {
        for (int x = r.minX() + 1; x < r.maxX(); x += 6)
            for (int z = r.minZ() + 3; z < -10; z += 9) {
                if (Math.abs(x) < 9) continue;
                int ox = Math.floorMod(x * 31 + z * 17, 5) - 2, oz = Math.floorMod(x * 13 - z * 7, 5) - 2;
                if (LiteraryRooms.buildingClearing(scene, x + ox, z + oz)) continue;
                fell(x + ox, z + oz, 4 + Math.abs(x + z) % 3);
            }
        if (scene == LabyrinthPlace.COSTUME_NIGHT || scene == LabyrinthPlace.MOVIE_NIGHT || scene == LabyrinthPlace.WINTER_LAKE)
            for (int x = -26; x <= 26; x += 5) fell(x, r.minZ() + 5, 5);
    }

    private void fell(int x, int z, int height) {
        for (int y = 0; y <= height; y++) if (at(x, y, z).is(Blocks.SPRUCE_LOG)) remove(p(x, y, z));
        for (int h = 2; h <= height + 2; h++) {
            int w = Math.max(1, (height + 3 - h) / 2);
            for (int dx = -w; dx <= w; dx++)
                for (int dz = -w; dz <= w; dz++) {
                    if (holyCanopy(x + dx, h, z + dz)) continue;
                    if (at(x + dx, h, z + dz).is(Blocks.SPRUCE_LEAVES)) remove(p(x + dx, h, z + dz));
                }
        }
    }

    /** The holy rabbit's great tree and its hollow keep every leaf they were grown with. */
    private boolean holyCanopy(int x, int y, int z) {
        if (scene != LabyrinthPlace.HOLY_RABBIT) return false;
        if (Math.abs(x) <= 5 && z >= -29 && z <= -20 && y <= 4) return true;
        if (y < 2 || y > 21) return false;
        int w = Math.max(1, (22 - y) / 2);
        return Math.abs(x) <= w && Math.abs(z + 25) <= w;
    }

    private Tree pick(int[] mix, int roll) {
        int total = 0;
        for (int weight : mix) total += weight;
        int at = Math.floorMod(roll, Math.max(1, total));
        for (int i = 0; i < mix.length; i++) {
            if (at < mix[i]) return Tree.values()[i];
            at -= mix[i];
        }
        return Tree.SPRUCE;
    }

    /**
     * A mixed wood: trees at irregular spacing, in clumps and clearings, of the
     * kinds the place would have (mix weights SPRUCE, PINE, OAK, BIRCH, SNAG).
     */
    private void woods(int spacing, int[] mix, boolean snowy) {
        woods(r.minX() + 1, r.maxX() - 1, r.minZ() + 1, -10, spacing, mix, snowy, this::open);
    }

    private void woods(int x0, int x1, int z0, int z1, int spacing, int[] mix, boolean snowy, BiPredicate<Integer, Integer> barred) {
        List<int[]> roots = new ArrayList<>();
        int min = Math.max(3, spacing - 1);
        for (int gx = x0; gx <= x1; gx += spacing)
            for (int gz = z0; gz <= z1; gz += spacing) {
                int h = hash(gx, gz, 211);
                if (h % 100 < 18) continue;
                int x = gx + h % spacing, z = gz + (h >> 7) % spacing;
                if (x > x1 || z > z1 || barred.test(x, z) || Landscapes.noise(x, z, 41 + scene.ordinal()) < -0.6) continue;
                boolean crowded = false;
                for (int[] root : roots) crowded |= (root[0] - x) * (root[0] - x) + (root[1] - z) * (root[1] - z) < min * min;
                if (crowded) continue;
                Tree kind = pick(mix, h >> 11);
                int height = switch (kind) {
                    case SPRUCE -> 6 + (h >> 4) % 6;
                    case PINE -> 9 + (h >> 4) % 4;
                    case OAK -> 4 + (h >> 4) % 3;
                    case BIRCH -> 5 + (h >> 4) % 3;
                    case SNAG -> 4 + (h >> 4) % 4;
                };
                if (grow(x, z, kind, height, snowy, h)) roots.add(new int[]{x, z});
            }
    }

    private void tree(BlockPos at, BlockState s) {
        if (l.getBlockState(at).isAir() || plant(l.getBlockState(at))) set(at, s);
    }

    /** Grows one tree from natural ground into open air only. */
    private boolean grow(int x, int z, Tree kind, int height, boolean snowy, int seed) {
        int g = ground(x, z);
        if (g == Integer.MIN_VALUE) return false;
        for (int y = 1; y <= height + 2; y++) {
            BlockState s = at(x, g + y, z);
            if (!s.isAir() && !(y == 1 && plant(s))) return false;
        }
        Block log = kind == Tree.BIRCH ? Blocks.BIRCH_LOG : kind == Tree.OAK ? Blocks.OAK_LOG : Blocks.SPRUCE_LOG;
        Block leaf = kind == Tree.BIRCH ? Blocks.BIRCH_LEAVES : kind == Tree.OAK ? Blocks.OAK_LEAVES : Blocks.SPRUCE_LEAVES;
        if (plant(at(x, g + 1, z))) remove(p(x, g + 1, z));
        if (at(x, g, z).is(Blocks.GRASS_BLOCK)) set(p(x, g, z), Blocks.DIRT.defaultBlockState());
        for (int y = 1; y <= height; y++) tree(p(x, g + y, z), log.defaultBlockState());
        List<int[]> crown = new ArrayList<>();
        switch (kind) {
            case SPRUCE -> {
                for (int y = Math.max(2, height / 4); y <= height; y++) {
                    int fromTop = height - y;
                    crown.add(new int[]{y, fromTop % 2 == 0 ? Math.min(3, 1 + fromTop / 3) : Math.max(1, fromTop / 3)});
                }
                crown.add(new int[]{height + 1, 0});
                crown.add(new int[]{height + 2, 0});
            }
            case PINE -> {
                for (int y = height - 4; y <= height; y++) crown.add(new int[]{y, y <= height - 3 ? 2 : 1});
                crown.add(new int[]{height + 1, 0});
            }
            case OAK -> {
                crown.add(new int[]{height - 2, 2});
                crown.add(new int[]{height - 1, 2});
                crown.add(new int[]{height, 2});
                crown.add(new int[]{height + 1, 1});
            }
            case BIRCH -> {
                crown.add(new int[]{height - 2, 2});
                crown.add(new int[]{height - 1, 2});
                crown.add(new int[]{height, 1});
                crown.add(new int[]{height + 1, 1});
            }
            case SNAG -> {
                Direction side = Direction.from2DDataValue(seed & 3);
                put(x + side.getStepX(), g + height - 1, z + side.getStepZ(), fence(Blocks.SPRUCE_FENCE, side.getOpposite()));
            }
        }
        for (int[] ring : crown) {
            int y = ring[0], radius = ring[1];
            for (int dx = -radius; dx <= radius; dx++)
                for (int dz = -radius; dz <= radius; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > radius * radius + (radius > 1 ? 1 : 0)) continue;
                    // Corners and a few edge leaves are left open, so no two crowns are the same block for block.
                    if (radius > 0 && d2 >= radius * radius && Math.floorMod(seed + dx * 7 + dz * 13 + y * 5, 3) == 0) continue;
                    tree(p(x + dx, g + y, z + dz), leaves(leaf));
                }
        }
        if (snowy)
            for (int[] ring : crown)
                for (int dx = -ring[1]; dx <= ring[1]; dx++)
                    for (int dz = -ring[1]; dz <= ring[1]; dz++) {
                        BlockPos top = p(x + dx, g + height + 3, z + dz);
                        while (top.getY() > b.getY() + g && l.getBlockState(top).isAir()) top = top.below();
                        if (l.getBlockState(top).getBlock() instanceof LeavesBlock && l.getBlockState(top.above()).isAir()) set(top.above(), Blocks.SNOW.defaultBlockState());
                    }
        return true;
    }

    /** Ground cover by what the ground is: grasses and flowers, ferns and fungus, drifted snow; now and then a bush, a boulder or a fallen trunk. */
    private void undergrowth() {
        for (int x = r.minX() + 1; x < r.maxX(); x++)
            for (int z = r.minZ() + 1; z < -1; z++) {
                int g = ground(x, z);
                if (g == Integer.MIN_VALUE || !at(x, g + 1, z).isAir() || kept(x, g + 1, z) || nearStory(x, z, 2)) continue;
                BlockState top = at(x, g, z);
                int h = hash(x, z, 307) % 1000;
                boolean feature = !open(x, z);
                if (feature && h >= 990) {
                    boulder(x, g, z, h);
                    continue;
                }
                if (feature && h >= 985) {
                    fallen(x, g, z, h);
                    continue;
                }
                if (top.is(Blocks.GRASS_BLOCK)) {
                    if (h < 110) put(x, g + 1, z, Blocks.SHORT_GRASS);
                    else if (h < 150) put(x, g + 1, z, Blocks.FERN);
                    else if (h < 160) tall(x, g + 1, z, Blocks.TALL_GRASS);
                    else if (h < 168) put(x, g + 1, z, h % 3 == 0 ? Blocks.OXEYE_DAISY : h % 3 == 1 ? Blocks.CORNFLOWER : Blocks.DANDELION);
                    else if (feature && h < 176) bush(x, g, z, Blocks.OAK_LEAVES, h);
                } else if (top.is(Blocks.PODZOL) || top.is(Blocks.COARSE_DIRT)) {
                    if (h < 100) put(x, g + 1, z, Blocks.FERN);
                    else if (h < 115) tall(x, g + 1, z, Blocks.LARGE_FERN);
                    else if (h < 140) put(x, g + 1, z, Blocks.MOSS_CARPET);
                    else if (h < 154) put(x, g + 1, z, Blocks.BROWN_MUSHROOM);
                    else if (h < 158) put(x, g + 1, z, Blocks.RED_MUSHROOM);
                    else if (feature && h < 166) bush(x, g, z, Blocks.SPRUCE_LEAVES, h);
                } else if (top.is(Blocks.SNOW_BLOCK)) {
                    if (h < 160) put(x, g + 1, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + h % 2));
                    else if (feature && h < 166) bush(x, g, z, Blocks.SPRUCE_LEAVES, h);
                }
            }
    }

    private void tall(int x, int y, int z, Block plant) {
        if (!at(x, y + 1, z).isAir()) return;
        new Plan().add(x, y, z, plant.defaultBlockState()).add(x, y + 1, z, plant.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER)).place();
    }

    private void bush(int x, int g, int z, Block leaf, int h) {
        if (!put(x, g + 1, z, leaves(leaf))) return;
        if (h % 2 == 0) put(x, g + 2, z, leaves(leaf));
        Direction side = Direction.from2DDataValue(h & 3);
        if (ground(x + side.getStepX(), z + side.getStepZ()) == g) put(x + side.getStepX(), g + 1, z + side.getStepZ(), leaves(leaf));
        if (at(x, g, z).is(Blocks.SNOW_BLOCK) || scene == LabyrinthPlace.HOLY_RABBIT || scene == LabyrinthPlace.WINTER_LAKE)
            put(x, at(x, g + 2, z).isAir() ? g + 2 : g + 3, z, Blocks.SNOW.defaultBlockState());
    }

    private void boulder(int x, int g, int z, int h) {
        Block[] stone = {Blocks.MOSSY_COBBLESTONE, Blocks.ANDESITE, Blocks.STONE, Blocks.TUFF};
        for (int[] o : new int[][]{{0, 1, 0}, {1, 1, 0}, {0, 1, 1}, {0, 2, 0}, {-1, 1, 0}}) {
            if (o[0] == -1 && h % 2 == 0) continue;
            if (ground(x + o[0], z + o[2]) != g) continue;
            put(x + o[0], g + o[1], z + o[2], stone[Math.floorMod(h + o[0] * 3 + o[2] * 5 + o[1], stone.length)]);
        }
        if (at(x, g, z).is(Blocks.SNOW_BLOCK)) put(x, g + 3, z, Blocks.SNOW.defaultBlockState());
        else put(x, g + 3, z, Blocks.MOSS_CARPET);
    }

    private void fallen(int x, int g, int z, int h) {
        Direction along = h % 2 == 0 ? Direction.EAST : Direction.SOUTH;
        Plan plan = new Plan();
        for (int i = -1; i <= 1; i++) {
            if (ground(x + along.getStepX() * i, z + along.getStepZ() * i) != g) return;
            plan.add(x + along.getStepX() * i, g + 1, z + along.getStepZ() * i, log(h % 3 == 0 ? Blocks.BIRCH_LOG : Blocks.SPRUCE_LOG, along.getAxis()));
        }
        if (plan.place()) put(x, g + 2, z, at(x, g, z).is(Blocks.SNOW_BLOCK) ? Blocks.SNOW : Blocks.MOSS_CARPET);
    }

    // ------------------------------------------------------------------

    /** The elk lot's back field: a meadow in drifts instead of a grid of tufts. */
    private void meadow() {
        for (int z = -46; z > -105; z -= 7)
            for (int x = -22; x <= 22; x += 4) {
                if (at(x, 1, z).is(Blocks.TALL_GRASS)) remove(p(x, 1, z));
                if (at(x, 0, z).is(Blocks.TALL_GRASS)) remove(p(x, 0, z));
            }
        for (int x = -32; x <= 32; x++)
            for (int z = -108; z <= -40; z++) {
                int g = ground(x, z);
                if (g == Integer.MIN_VALUE || !at(x, g + 1, z).isAir() || !at(x, g, z).is(Blocks.GRASS_BLOCK)) continue;
                double drift = Landscapes.noise(x, z, 97), bloom = Landscapes.noise(x * 2, z * 2, 61);
                int h = hash(x, z, 401) % 100;
                if (drift > 0.3 && h < 35) tall(x, g + 1, z, Blocks.TALL_GRASS);
                else if (drift > -0.1 && h < 55) put(x, g + 1, z, Blocks.SHORT_GRASS);
                else if (bloom > 0.55 && h < 30) put(x, g + 1, z, h % 2 == 0 ? Blocks.OXEYE_DAISY : Blocks.CORNFLOWER);
                else if (h < 8) put(x, g + 1, z, Blocks.FERN);
            }
    }

    /** The bar: a canopy and a lamp over its door, painted bays in the lot, a dumpster and two street lights. */
    private void bar() {
        for (int y = 0; y <= 2; y++) for (int z : new int[]{-17, -13}) put(-5, y, z, Blocks.DARK_OAK_FENCE);
        for (int x = -6; x <= -5; x++) for (int z = -17; z <= -13; z++) put(x, 3, z, slab(Blocks.DARK_OAK_SLAB, SlabType.BOTTOM));
        put(-6, 2, -17, lantern(true));
        for (int z = -11; z <= -9; z++) put(-6, 0, z, stairs(Blocks.SPRUCE_STAIRS, Direction.WEST));
        for (int x : new int[]{-5, 4, 15, 26})
            for (int z = -28; z <= -20; z++)
                if (at(x, -1, z).is(Blocks.GRAY_CONCRETE) && at(x, 0, z).isAir()) set(p(x, -1, z), Blocks.WHITE_CONCRETE.defaultBlockState());
        new Plan().add(-5, 0, -26, Blocks.GREEN_TERRACOTTA).add(-4, 0, -26, Blocks.GREEN_TERRACOTTA)
                .add(-5, 1, -26, Blocks.IRON_TRAPDOOR).add(-4, 1, -26, Blocks.IRON_TRAPDOOR).place();
        for (int x : new int[]{3, 25}) {
            Plan pole = new Plan();
            for (int y = 0; y <= 6; y++) pole.add(x, y, -35, Blocks.SPRUCE_LOG);
            pole.add(x, 6, -34, fence(Blocks.SPRUCE_FENCE, Direction.NORTH)).add(x, 5, -34, lantern(true));
            pole.place();
        }
        for (int[] corner : new int[][]{{-23, -24}, {-7, -24}, {-23, -5}, {-7, -5}})
            for (int y = 0; y <= 5; y++) wallTrim(corner[0], y, corner[1], log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y), LiteraryRegistry.SIDING.get());
    }

    /** Replaces one block of a building's own wall material, and nothing else. */
    private void wallTrim(int x, int y, int z, BlockState s, Block... materials) {
        BlockPos at = p(x, y, z);
        BlockState old = l.getBlockState(at);
        if (l.getBlockEntity(at) != null) return;
        for (Block material : materials)
            if (old.is(material)) {
                set(at, s);
                return;
            }
    }

    /** Open shutters either side of a window, lying flat against the wall. */
    private void shutters(int x, int z, Direction out, int y0, int y1, Block trapdoor) {
        for (int y = y0; y <= y1; y++)
            put(x, y, z, trapdoor.defaultBlockState().setValue(TrapDoorBlock.FACING, out).setValue(TrapDoorBlock.OPEN, true).setValue(TrapDoorBlock.HALF, Half.BOTTOM));
    }

    /** A painted board on a wall, its words installed with it. */
    private void sign(int x, int y, int z, Direction facing, String... lines) {
        BlockPos at = p(x, y, z);
        BlockState s = Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, facing);
        if (!canPut(at, s) || !s.canSurvive(l, at)) return;
        String[] text = {"", "", "", ""};
        for (int i = 0; i < Math.min(4, lines.length); i++) text[i] = lines[i];
        journal.add(new Change(at.immutable(), l.getBlockState(at)));
        NovelRooms.sign(l, at, facing, text);
        placed.add(at.immutable());
        changed++;
    }

    /** A railing: posts joined to each other and to whatever solid stands beside them. */
    private void rail(int x, int y, int z, Block fence) {
        put(x, y, z, fence.defaultBlockState());
    }

    private void join(int x0, int x1, int y, int z0, int z1) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++) {
                BlockPos at = p(x, y, z);
                BlockState s = l.getBlockState(at);
                if (!(s.getBlock() instanceof net.minecraft.world.level.block.FenceBlock) || !placed.contains(at)) continue;
                BlockState want = s.getBlock().defaultBlockState();
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    BlockPos n = at.relative(d);
                    BlockState ns = l.getBlockState(n);
                    if (ns.getBlock() instanceof net.minecraft.world.level.block.FenceBlock || ns.is(BlockTags.LOGS) || ns.isFaceSturdy(l, n, d.getOpposite()))
                        want = want.setValue(side(d), true);
                }
                if (want != s) set(at, want);
            }
    }

    /** The lodge: a porch roof and railing, a chair and lamp on it, shutters, a chimney and a woodpile. */
    private void lodge() {
        for (int x = -5; x <= 5; x++) for (int z = -7; z <= -3; z++) put(x, 5, z, slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        for (int x : new int[]{-3, -2, 2, 3}) rail(x, 0, -4, Blocks.SPRUCE_FENCE);
        for (int x : new int[]{-4, 4}) for (int z = -7; z <= -5; z++) rail(x, 0, z, Blocks.SPRUCE_FENCE);
        join(-4, 4, 0, -7, -4);
        put(-3, 0, -6, HouseholdFurnitureBlock.state(CANE_CHAIR, Direction.SOUTH));
        put(-2, 0, -6, HouseholdFurnitureBlock.state(BEDSIDE_TABLE, Direction.SOUTH));
        put(0, 4, -5, lantern(true));
        shutters(-14, -21, Direction.WEST, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        shutters(-14, -15, Direction.WEST, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        shutters(14, -33, Direction.EAST, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        shutters(14, -27, Direction.EAST, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        for (int x : new int[]{-12, -6, 6, 12}) shutters(x, -7, Direction.SOUTH, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        for (int x : new int[]{8, 10, -8, -10}) put(x, 0, -7, Blocks.POTTED_AZALEA);
        // A brick chimney against the north gable, its breast under the eave and its stack clear of it.
        Plan chimney = new Plan();
        for (int x = -11; x <= -10; x++) {
            for (int y = 0; y <= 5; y++) chimney.add(x, y, -37, Blocks.BRICKS);
            for (int y = 0; y <= 10; y++) chimney.add(x, y, -38, Blocks.BRICKS);
        }
        chimney.add(-11, 11, -38, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true)).add(-10, 11, -38, slab(Blocks.BRICK_SLAB, SlabType.BOTTOM));
        chimney.place();
        woodpile(14, -14, -10);
        for (int[] corner : new int[][]{{-13, -36}, {13, -36}, {-13, -8}, {13, -8}})
            for (int y = 0; y <= 4; y++) wallTrim(corner[0], y, corner[1], log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y), LiteraryRegistry.SIDING.get());
    }

    /** Split logs stacked against a wall under a slab roof. */
    private void woodpile(int x, int z0, int z1) {
        for (int z = z0; z <= z1; z++)
            if (new Plan().add(x, 0, z, log(Blocks.OAK_LOG, Direction.Axis.X)).add(x, 1, z, log(Blocks.OAK_LOG, Direction.Axis.X)).place())
                put(x, 2, z, slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
    }

    /** The cabin at the end of the world: porch railing and posts, chairs, a lamp, shutters, a woodpile and a jetty. */
    private void endCabin() {
        for (int x : new int[]{-5, 5}) {
            Plan post = new Plan();
            for (int y = 0; y <= 5; y++) post.add(x, y, -8, Blocks.SPRUCE_LOG);
            post.place();
        }
        for (int x = -11; x <= 11; x++) if (Math.abs(x) > 1 && Math.abs(x) != 5) rail(x, 0, -8, Blocks.SPRUCE_FENCE);
        join(-11, 11, 0, -8, -8);
        put(-4, 0, -10, HouseholdFurnitureBlock.state(CANE_CHAIR, Direction.SOUTH));
        put(4, 0, -10, HouseholdFurnitureBlock.state(CANE_CHAIR, Direction.SOUTH));
        put(5, 0, -10, HouseholdFurnitureBlock.state(BEDSIDE_TABLE, Direction.SOUTH));
        put(0, 5, -10, lantern(true));
        shutters(-13, -30, Direction.WEST, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        shutters(-13, -24, Direction.WEST, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        shutters(1, -36, Direction.NORTH, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        shutters(7, -36, Direction.NORTH, 2, 3, Blocks.DARK_OAK_TRAPDOOR);
        woodpile(-13, -18, -15);
        for (int[] corner : new int[][]{{-12, -35}, {12, -35}, {-12, -13}, {12, -13}})
            for (int y = 0; y <= 5; y++) wallTrim(corner[0], y, corner[1], log(Blocks.STRIPPED_SPRUCE_LOG, Direction.Axis.Y), Blocks.SPRUCE_PLANKS);
        // A jetty out over the lake on pilings.
        for (int z = -49; z <= -42; z++) for (int x = 6; x <= 7; x++) if (at(x, -1, z).is(Blocks.WATER)) set(p(x, -1, z), Blocks.SPRUCE_PLANKS.defaultBlockState());
        for (int x : new int[]{5, 8})
            for (int z : new int[]{-45, -49})
                for (int y = -8; y <= -1; y++) if (at(x, y, z).is(Blocks.WATER)) set(p(x, y, z), Blocks.SPRUCE_LOG.defaultBlockState());
        for (int x : new int[]{5, 8}) put(x, 0, -49, Blocks.SPRUCE_FENCE);
        put(8, 1, -49, lantern(false));
    }

    /** Camp cabins: each its own porch, number, shutters and corner posts; two re-roofed so the row is not six of one. */
    private void cabins() {
        int n = 0;
        for (int cx : new int[]{-20, 20})
            for (int cz : new int[]{-26, -57, -83}) {
                n++;
                int s = cx < 0 ? 1 : -1, door = cx + 8 * s;
                Direction out = s > 0 ? Direction.EAST : Direction.WEST;
                // A plank deck flush with the ground before the door, under a slab roof on two posts.
                for (int i = 1; i <= 3; i++)
                    for (int z = cz - 2; z <= cz + 2; z++) {
                        if (!natural(at(door + s * i, -1, z))) continue;
                        set(p(door + s * i, -1, z), Blocks.SPRUCE_PLANKS.defaultBlockState());
                        if (plant(at(door + s * i, 0, z))) remove(p(door + s * i, 0, z));
                    }
                for (int z : new int[]{cz - 2, cz + 2}) for (int y = 0; y <= 2; y++) put(door + 3 * s, y, z, Blocks.SPRUCE_FENCE);
                for (int i = 1; i <= 3; i++) for (int z = cz - 2; z <= cz + 2; z++) put(door + s * i, 3, z, slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
                put(door + 2 * s, 2, cz, lantern(true));
                put(door + s, 0, cz - 2, stairs(Blocks.SPRUCE_STAIRS, out.getOpposite()));
                sign(door + s, 2, cz + 1, out, "CABIN " + n, "", "", "");
                shutters(cx - 3, cz - 8, Direction.NORTH, 2, 3, Blocks.SPRUCE_TRAPDOOR);
                shutters(cx + 3, cz - 8, Direction.NORTH, 2, 3, Blocks.SPRUCE_TRAPDOOR);
                for (int x : new int[]{cx - 8, cx + 8}) for (int z : new int[]{cz - 7, cz + 7})
                    for (int y = 0; y <= 4; y++) wallTrim(x, y, z, log(Blocks.SPRUCE_LOG, Direction.Axis.Y), Blocks.SPRUCE_PLANKS);
                for (int x = cx - 8; x <= cx + 8; x++) for (int z = cz - 7; z <= cz + 7; z++)
                    if (x == cx - 8 || x == cx + 8 || z == cz - 7 || z == cz + 7) wallTrim(x, 0, z, (n % 2 == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE).defaultBlockState(), Blocks.SPRUCE_PLANKS, Blocks.DARK_OAK_PLANKS);
                if (n == 2 || n == 5) reroof(cx, cz);
            }
    }

    /** Re-covers one cabin's roof in spruce, keeping every stair's shape exactly. */
    private void reroof(int cx, int cz) {
        for (int x = cx - 9; x <= cx + 9; x++)
            for (int z = cz - 8; z <= cz + 8; z++)
                for (int y = 5; y <= 13; y++) {
                    BlockState s = at(x, y, z);
                    if (s.is(Blocks.DARK_OAK_STAIRS))
                        set(p(x, y, z), Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, s.getValue(StairBlock.FACING))
                                .setValue(StairBlock.HALF, s.getValue(StairBlock.HALF)).setValue(StairBlock.SHAPE, s.getValue(StairBlock.SHAPE)));
                    else if (s.is(Blocks.DARK_OAK_SLAB)) set(p(x, y, z), Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, s.getValue(SlabBlock.TYPE)));
                }
    }

    /** The camp's fire ring with log benches, a picnic table, a sign at the gate and lamps along the road. */
    private void campGround() {
        for (int x = 5; x <= 7; x++)
            for (int z = -46; z <= -44; z++)
                if ((x != 6 || z != -45) && natural(at(x, -1, z))) set(p(x, -1, z), (Math.floorMod(x + z, 2) == 0 ? Blocks.COBBLESTONE : Blocks.STONE).defaultBlockState());
        for (int x = 5; x <= 7; x++) { put(x, 0, -48, log(Blocks.OAK_LOG, Direction.Axis.X)); put(x, 0, -42, log(Blocks.OAK_LOG, Direction.Axis.X)); }
        for (int z = -46; z <= -44; z++) { put(3, 0, z, log(Blocks.OAK_LOG, Direction.Axis.Z)); put(9, 0, z, log(Blocks.OAK_LOG, Direction.Axis.Z)); }
        for (int x = 11; x <= 13; x++) {
            put(x, 0, -41, slab(Blocks.SPRUCE_SLAB, SlabType.TOP));
            put(x, 0, -42, slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
            put(x, 0, -40, slab(Blocks.SPRUCE_SLAB, SlabType.BOTTOM));
        }
        Plan post = new Plan();
        for (int y = 0; y <= 2; y++) post.add(4, y, -10, Blocks.SPRUCE_LOG);
        if (post.place()) sign(4, 2, -9, Direction.SOUTH, "CAMP", "Cabins 1 - 6", "Lights out", "at ten");
        for (int z : new int[]{-20, -40, -60, -80})
            for (int x : new int[]{-4, 4})
                if (!(x == 4 && z == -40)) new Plan().add(x, 0, z, Blocks.SPRUCE_FENCE).add(x, 1, z, Blocks.SPRUCE_FENCE).add(x, 2, z, lantern(false)).place();
        for (int z = -98; z <= -8; z++)
            for (int x : new int[]{-3, 3})
                if (natural(at(x, -1, z)) && hash(x, z, 503) % 3 == 0) set(p(x, -1, z), Blocks.COARSE_DIRT.defaultBlockState());
    }

    // ------------------------------------------------------------------
    // Lakes.

    private boolean lake(int x, int z) {
        BlockState s = at(x, -1, z);
        return s.is(Blocks.WATER) || s.is(Blocks.ICE);
    }

    /** The costume-night beach was a single sheet of sand over the lake: it is given ground beneath it. */
    private void beach() {
        if (scene != LabyrinthPlace.COSTUME_NIGHT) return;
        for (int x = -28; x <= 28; x++)
            for (int z = -48; z <= -13; z++) {
                if (!at(x, -1, z).is(Blocks.SAND)) continue;
                for (int y = -2; y >= -11; y--) if (at(x, y, z).is(Blocks.WATER)) set(p(x, y, z), Blocks.SANDSTONE.defaultBlockState());
            }
    }

    /** Fills a run of shallows into bank, top to bed, so the shoreline is no longer ruled straight. */
    private void bank(int x, int z, boolean grassy) {
        boolean winter = scene == LabyrinthPlace.WINTER_LAKE;
        for (int y = -1; y >= -11; y--) {
            BlockState s = at(x, y, z);
            if (!s.is(Blocks.WATER) && !s.is(Blocks.ICE)) break;
            set(p(x, y, z), (y == -1 ? grassy ? Blocks.GRASS_BLOCK : Blocks.SAND : grassy ? Blocks.DIRT : Blocks.SANDSTONE).defaultBlockState());
        }
        if (winter && at(x, 0, z).isAir()) set(p(x, 0, z), Blocks.SNOW.defaultBlockState());
    }

    private void shores() {
        for (int x = -27; x <= 27; x++) {
            if (Math.abs(x) <= 5) continue;
            int near = Integer.MIN_VALUE, far = Integer.MIN_VALUE;
            for (int z = -12; z >= -76; z--) if (lake(x, z)) { near = z; break; }
            for (int z = r.minZ() + 4; z <= -12; z++) if (lake(x, z)) { far = z; break; }
            int dn = (int) Math.round((Landscapes.noise(x, 0, 31) + 1) * 1.5), df = (int) Math.round((Landscapes.noise(x, 0, 57) + 1) * 1.5);
            if (near != Integer.MIN_VALUE) for (int i = 0; i < dn; i++) bank(x, near - i, false);
            if (far != Integer.MIN_VALUE && far < near - 6) for (int i = 0; i < df; i++) bank(x, far + i, true);
        }
        for (int z = -72; z <= -13; z++)
            for (int side : new int[]{-1, 1}) {
                int d = (int) Math.round((Landscapes.noise(0, z, 83 + side) + 1) * 1.5);
                for (int i = 0; i < d; i++) if (lake(side * (28 - i), z)) bank(side * (28 - i), z, false);
            }
    }

    /** Reeds and lily pads along the shallows; on the frozen lake, dead stalks in the snow. */
    private void reeds() {
        boolean winter = scene == LabyrinthPlace.WINTER_LAKE;
        for (int x = -28; x <= 28; x++)
            for (int z = r.minZ() + 4; z <= -2; z++) {
                if (Math.abs(x) <= 6) continue;
                int h = hash(x, z, 607) % 100;
                BlockState top = at(x, -1, z);
                boolean shore = false;
                for (Direction d : Direction.Plane.HORIZONTAL) shore |= at(x + d.getStepX(), -1, z + d.getStepZ()).is(Blocks.WATER);
                if (!winter && shore && (top.is(Blocks.SAND) || top.is(Blocks.GRASS_BLOCK)) && at(x, 0, z).isAir() && h < 30) {
                    int height = 2 + h % 2;
                    Plan cane = new Plan();
                    for (int y = 0; y < height; y++) cane.add(x, y, z, Blocks.SUGAR_CANE);
                    cane.place();
                } else if (!winter && top.is(Blocks.WATER) && at(x, 0, z).isAir() && h < 7 && nearShore(x, z)) put(x, 0, z, Blocks.LILY_PAD);
                else if (winter && top.is(Blocks.SAND) && at(x, 0, z).is(Blocks.SNOW) && h < 4) {
                    remove(p(x, 0, z));
                    put(x, 0, z, Blocks.DEAD_BUSH);
                }
            }
    }

    private boolean nearShore(int x, int z) {
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) if (!lake(x + dx, z + dz) && at(x + dx, -1, z + dz).isSolidRender(l, p(x + dx, -1, z + dz))) return true;
        return false;
    }

    /**
     * Indian Lake's banks rose in a checkerboard of single steps behind a flat
     * wall of leaves: they are regraded to a smooth rise, and the leaf wall is
     * replaced with a close wood and undergrowth on the bank.
     */
    private static boolean earth(BlockState s) {
        return s.is(Blocks.PODZOL) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.ROOTED_DIRT);
    }

    private void banks() {
        int seed = 113 + scene.ordinal();
        // Why each bank column was left alone, reported once per scene so a skipped bank is explained.
        int[] tally = new int[6]; // trunk/story, no earth top, built under, unchanged, regraded, considered
        for (int x = r.minX() - 4; x <= r.maxX() + 4; x++)
            for (int z = r.minZ() - 4; z <= -2; z++) {
                int edge = Math.max(Math.max(r.minX() + 2 - x, x - r.maxX() + 2), r.minZ() + 2 - z);
                if (edge < 1) continue;
                tally[5]++;
                if (nearLog(x, z) || VignetteArchitecture.storyReserved(scene, x, 0, z)) { tally[0]++; continue; }
                int top = Integer.MIN_VALUE;
                for (int y = 8; y >= -3; y--) {
                    BlockState s = at(x, y, z);
                    if (earth(s)) { top = y; break; }
                    if (!s.isAir() && !(s.getBlock() instanceof LeavesBlock) && !plant(s)) break;
                }
                if (top == Integer.MIN_VALUE) { tally[1]++; continue; }
                // Earth at least two deep: a bank, not a floor laid over something built.
                if (!earth(at(x, top - 1, z)) || !earth(at(x, top - 2, z))) { tally[2]++; continue; }
                if (edge >= 4) for (int y = top + 1; y <= 9; y++) if (at(x, y, z).is(Blocks.SPRUCE_LEAVES)) remove(p(x, y, z));
                int target = Math.max(0, Math.min(6, (int) Math.round(edge * 0.6 + Landscapes.noise(x, z, seed) * 1.1)));
                // Never raised into a crown that overhangs the column.
                for (int y = top + 1; y <= target; y++)
                    if (!at(x, y, z).isAir() && !plant(at(x, y, z))) {
                        target = y - 1;
                        break;
                    }
                if (target == top) { tally[3]++; continue; }
                tally[4]++;
                if (plant(at(x, top + 1, z))) remove(p(x, top + 1, z));
                for (int y = Math.min(top, target); y <= Math.max(top, target); y++)
                    set(p(x, y, z), y < target ? Blocks.DIRT.defaultBlockState() : y == target ? Blocks.PODZOL.defaultBlockState() : AIR);
            }
        TheOldestHouse.LOGGER.info("Regraded {} bank: {} columns, {} regraded, {} already in grade, {} by a trunk or story, {} without earth on top, {} over built ground.",
                scene.id(), tally[5], tally[4], tally[3], tally[0], tally[1], tally[2]);
        // A close wood on the bank beyond the walkable rim.
        woods(r.minX() - 4, r.maxX() + 4, r.minZ() - 4, -3, 3, new int[]{60, 10, 20, 10, 0}, false,
                (x, z) -> Math.max(Math.max(r.minX() + 2 - x, x - r.maxX() + 2), r.minZ() + 2 - z) < 3);
        for (int x = r.minX() - 4; x <= r.maxX() + 4; x++)
            for (int z = r.minZ() - 4; z <= -3; z++) {
                int edge = Math.max(Math.max(r.minX() + 2 - x, x - r.maxX() + 2), r.minZ() + 2 - z);
                if (edge < 3) continue;
                int g = ground(x, z);
                if (g == Integer.MIN_VALUE || !at(x, g + 1, z).isAir()) continue;
                int h = hash(x, z, 709) % 100;
                if (edge >= 4 && h < 35) bush(x, g, z, h % 3 == 0 ? Blocks.OAK_LEAVES : Blocks.SPRUCE_LEAVES, h);
                else if (h < 60) put(x, g + 1, z, h % 2 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS);
            }
    }

    /** A column a trunk stands in keeps its ground exactly. */
    private boolean nearLog(int x, int z) {
        for (int y = -1; y <= 7; y++) if (at(x, y, z).is(BlockTags.LOGS)) return true;
        return false;
    }

    // ------------------------------------------------------------------

    /**
     * The Overlook's grounds: drifts in place of stepped snow terraces, snowy
     * spruces on them, the hedge animals of the novel by the maze, and lamps
     * along the approach.
     */
    private void hotelGrounds() {
        for (int x = -27; x <= 27; x++)
            for (int z = -67; z <= -4; z++) {
                if (Math.abs(x) < 21 && z > -60) continue;
                if (x >= -26 && x <= -16 && z >= -27 && z <= -16 || x >= -23 && x <= -16 && z >= -62 && z <= -58) continue;
                boolean snowOnly = true;
                for (int y = 0; y <= 8 && snowOnly; y++) {
                    BlockState s = at(x, y, z);
                    snowOnly = s.isAir() || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.SNOW);
                }
                if (!snowOnly || !at(x, -1, z).is(Blocks.DIRT)) continue;
                double rise = Math.max((Math.abs(x) - 20) * 0.75, (-59 - z) * 0.75);
                double depth = Math.max(0, Math.min(7.5, rise + Landscapes.noise(x, z, 7) * 1.4));
                int full = (int) depth, layers = (int) Math.round((depth - full) * 8);
                if (layers == 8) { full++; layers = 0; }
                for (int y = 0; y <= 8; y++) {
                    BlockState want = y < full ? Blocks.SNOW_BLOCK.defaultBlockState() : y == full && layers > 0
                            ? Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers) : y == 0 ? Blocks.SNOW.defaultBlockState() : AIR;
                    if (!at(x, y, z).equals(want)) set(p(x, y, z), want);
                }
            }
        woods(-27, 27, -67, -4, 5, new int[]{85, 15, 0, 0, 0}, true,
                (x, z) -> !(Math.abs(x) >= 23 || z <= -62) || x >= -27 && x <= -15 && z >= -28 && z <= -15 || x >= -24 && x <= -15 && z >= -63 && z <= -57);
        topiary(-5, -24, LION);
        topiary(5, -24, LION);
        topiary(-10, -14, RABBIT);
        topiary(10, -14, DOG);
        for (int z : new int[]{-6, -16})
            for (int x : new int[]{-4, 4}) new Plan().add(x, 0, z, Blocks.DARK_OAK_FENCE).add(x, 1, z, Blocks.DARK_OAK_FENCE).add(x, 2, z, lantern(false)).place();
        put(-21, 3, -21, lantern(true));
    }

    private static final int[][] LION = {{-1, 0, -1}, {0, 0, -1}, {1, 0, -1}, {-1, 0, 0}, {0, 0, 0}, {1, 0, 0}, {-1, 0, 1}, {1, 0, 1},
            {-1, 1, -1}, {0, 1, -1}, {1, 1, -1}, {-1, 1, 0}, {0, 1, 0}, {1, 1, 0}, {-1, 2, 0}, {0, 2, 0}, {1, 2, 0}, {0, 2, 1}, {-1, 3, 0}, {1, 3, 0}, {0, 1, -2}};
    private static final int[][] DOG = {{-1, 0, -1}, {1, 0, -1}, {-1, 0, 1}, {1, 0, 1}, {-1, 1, -1}, {0, 1, -1}, {1, 1, -1}, {-1, 1, 0}, {0, 1, 0}, {1, 1, 0},
            {-1, 1, 1}, {0, 1, 1}, {1, 1, 1}, {0, 2, 1}, {0, 2, 2}, {0, 3, 1}, {0, 2, -2}};
    private static final int[][] RABBIT = {{0, 0, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 1, 1}, {0, 2, 1}, {-1, 3, 1}, {1, 3, 1}};

    /** One of the hedge animals, clipped from spruce, with snow on its back. */
    private void topiary(int x, int z, int[][] shape) {
        Plan plan = new Plan();
        for (int[] o : shape) plan.add(x + o[0], o[1], z + o[2], leaves(Blocks.SPRUCE_LEAVES));
        if (!plan.place()) return;
        for (int[] o : shape) if (at(x + o[0], o[1] + 1, z + o[2]).isAir()) put(x + o[0], o[1] + 1, z + o[2], Blocks.SNOW.defaultBlockState());
    }
}
