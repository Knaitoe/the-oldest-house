package io.github.knaitoe.theoldesthouse.house;

import com.mojang.datafixers.util.Pair;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.ClassicsRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.NurseryBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.WallpaperPanelBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount;
import io.github.knaitoe.theoldesthouse.labyrinth.WitnessAccount.Story;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LiteraryViewPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The House remembers a room (0.4.190). The success-and-failure audit gives eight experience rooms, neither won nor lost, a
 * subtle change that plays only in the manor once the room is resolved. Each echoes its own room, is seen or heard by that
 * reader alone, never reaches their own world and never blocks a way: every block is a view sent to that reader's client and
 * every figure exists only in packets, so nothing in the manor changes for anyone else and nothing is saved in the world.
 * <ul>
 * <li><b>The pattern (wallpaper):</b> the great room's west wall takes the yellow paper, two strips a House day, and a figure
 * moves in it at the edge of sight, gone when looked at.</li>
 * <li><b>The window (model home):</b> a tree stands outside the great room's front window. It is not there from outside.</li>
 * <li><b>The upper ledger (Winchester):</b> a door against the study's west wall that opens onto plaster.</li>
 * <li><b>The disappearing exits (child's room):</b> the teddy from the ceiling sits on the stair tower's stairs, a step higher
 * each House day.</li>
 * <li><b>The congregation (preserved cave):</b> now and then, walking into the hall, a single pew stands turned to face the
 * way they came, and is gone when they leave the hall.</li>
 * <li><b>The little rooms (miniatures):</b> a small figure of them stands where they last stood on an earlier House day.</li>
 * <li><b>The returned knocks (Hill nursery):</b> now and then pounding comes on the manor wall nearest them; knocking back the
 * same count on that wall is answered knock for knock, and the wall is quiet until the next day.</li>
 * <li><b>Twenty seconds (backseat):</b> at night, standing still before a window, the stowaway from the car sits up behind their
 * shoulder in the black glass, as it rose in the mirror; it is gone when they turn.</li>
 * </ul>
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class HouseRemembers {
    public static final String STATE = "house_remembers_0190";
    /** Wallpaper strips per House day, and the strips on the great room's west wall (z 1 to 14). */
    public static final int STRIPS_A_DAY = 2, STRIPS = 14;
    /** The Hill pounding: blows per bout at least and at most, the ticks between them, and how long the wall listens. */
    public static final int POUNDS_MIN = 2, POUNDS_MAX = 4, POUND_GAP = 12, LISTEN = 300, KNOCK_SETTLE = 30;
    /** The backseat: ticks still before a night window, and how long the stowaway stays at most. */
    public static final int STILL = 40, SEAT_LEASE = 100;
    /** The study's west wall cells a door may stand against, in order of preference; the windows are at z 17-18 and 22-23. */
    static final int[] DOOR_Z = {20, 19, 21, 16, 24, 25};

    private static final Map<UUID, View> VIEWS = new HashMap<>();
    private HouseRemembers() {}

    /** What one reader's client is being shown now; nothing here is saved. */
    public static final class View {
        ServerLevel level;
        BlockPos origin;
        int ticks;
        final Map<BlockPos, BlockState> blocks = new HashMap<>();
        BlockPos edge; int edgeUntil;
        BlockPos door; int doorOpenUntil;
        /** Recompute the block views on this tick: the edge figure came or went, or the door opened or shut. */
        boolean dirty;
        int toy = -1; BlockPos toyAt;
        int figure = -1; BlockPos figureAt;
        int pew = -1; boolean inHall; Vec3 last;
        // The Hill pounding.
        int hill; BlockPos wall; int pounds, pounded, knocks, hillAt, lastKnock, hillNext = 200;
        // The backseat.
        int still; Vec3 stillAt; float stillYaw; boolean seat; int seatUntil; Vec3 seatFrom;
        List<BlockPos> stairs;
    }
    static final int HILL_IDLE = 0, HILL_POUNDING = 1, HILL_LISTENING = 2, HILL_ANSWERING = 3;

    public static CompoundTag record(ServerPlayer p) {
        return LabyrinthData.get(p.server).stateEntry(STATE, p.getUUID().toString());
    }
    private static void save(ServerPlayer p, CompoundTag own) {
        LabyrinthData.get(p.server).setStateEntry(STATE, p.getUUID().toString(), own);
    }

    public static boolean present(ServerPlayer p) {
        var house = HouseSavedData.get(p.server);
        return p.isAlive() && !p.isSpectator() && house.houseOrigin() != null && !house.isOutdated() && !house.isCollapsed()
                && HouseExperience.inManor(p, p.blockPosition());
    }

    /** House days since the reader's resolution of the story was first seen here, or -1 if it is not resolved. */
    static long days(ServerPlayer p, CompoundTag own, Story story, long today) {
        if (!WitnessAccount.has(LabyrinthData.get(p.server), p.getUUID(), story)) return -1;
        String key = "Since_" + story.id;
        if (!own.contains(key)) own.putLong(key, today);
        return Math.max(0, today - own.getLong(key));
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e) {
        for (var p : e.getServer().getPlayerList().getPlayers()) tickPlayer(p);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) { VIEWS.remove(e.getEntity().getUUID()); }

    public static void tickPlayer(ServerPlayer p) {
        View view = VIEWS.get(p.getUUID());
        // A client that changed level has already dropped every block view and packet figure.
        if (view != null && view.level != p.level()) { VIEWS.remove(p.getUUID()); view = null; }
        if (!present(p)) { if (view != null) clear(p, view); return; }
        var origin = HouseSavedData.get(p.server).houseOrigin();
        if (view != null && !view.origin.equals(origin)) { clear(p, view); view = null; }
        if (view == null) { view = new View(); view.level = p.serverLevel(); view.origin = origin; VIEWS.put(p.getUUID(), view); }
        view.ticks++;
        CompoundTag own = record(p), before = own.copy();
        long today = HouseCalendar.today(p.server);
        long pattern = days(p, own, Story.WALLPAPER, today), window = days(p, own, Story.MODEL_HOME, today),
                ledger = days(p, own, Story.WINCHESTER, today), exits = days(p, own, Story.CHILD_ROOM, today),
                pews = days(p, own, Story.PRESERVED_CAVE, today), rooms = days(p, own, Story.MINIATURES, today),
                knocks = days(p, own, Story.HILL_NURSERY, today), backseat = days(p, own, Story.BACKSEAT, today);
        if (pattern >= 0) edge(p, view);
        if (view.ticks % 20 == 1 || view.dirty) {
            var wanted = new HashMap<BlockPos, BlockState>();
            if (pattern >= 0) wanted.putAll(wallpaper(p.serverLevel(), origin, pattern));
            if (view.edge != null && wanted.containsKey(view.edge)) wanted.put(view.edge, wanted.get(view.edge).setValue(WallpaperPanelBlock.FIGURE, true));
            if (window >= 0) wanted.putAll(tree(p.serverLevel(), origin));
            if (ledger >= 0) wanted.putAll(door(p, view, origin));
            show(p, view, wanted, view.ticks % 100 == 1 || view.dirty);
            view.dirty = false;
        }
        if (view.door != null && view.doorOpenUntil > 0 && view.ticks >= view.doorOpenUntil) { view.doorOpenUntil = 0; view.dirty = true; }
        if (exits >= 0 && view.ticks % 20 == 1) toy(p, view, origin, exits);
        if (rooms >= 0) figure(p, view, own, origin, today);
        if (pews >= 0) hall(p, view, own, origin, today);
        else view.inHall = false;
        if (knocks >= 0) hill(p, view, own, today);
        if (backseat >= 0) backseat(p, view, own, origin, today);
        view.last = p.position();
        if (!own.equals(before)) save(p, own);
    }

    /** Takes back every view and packet figure from this reader's client. */
    static void clear(ServerPlayer p, View view) {
        if (view.level == p.level()) {
            for (var pos : view.blocks.keySet()) p.connection.send(new ClientboundBlockUpdatePacket(view.level, pos));
            var ids = new ArrayList<Integer>();
            for (int id : new int[]{view.toy, view.figure, view.pew}) if (id >= 0) ids.add(id);
            if (!ids.isEmpty()) p.connection.send(new ClientboundRemoveEntitiesPacket(ids.stream().mapToInt(Integer::intValue).toArray()));
            if (view.seat) seat(p, null, 0);
        }
        VIEWS.remove(p.getUUID());
    }

    /** Sends each wanted block view that the client does not already hold, and the real block where a view is withdrawn. */
    static void show(ServerPlayer p, View view, Map<BlockPos, BlockState> wanted, boolean all) {
        for (var it = view.blocks.entrySet().iterator(); it.hasNext(); ) {
            var shown = it.next();
            if (!wanted.containsKey(shown.getKey())) { p.connection.send(new ClientboundBlockUpdatePacket(view.level, shown.getKey())); it.remove(); }
        }
        for (var want : wanted.entrySet()) {
            if (!all && want.getValue().equals(view.blocks.get(want.getKey()))) continue;
            p.connection.send(new ClientboundBlockUpdatePacket(want.getKey(), want.getValue()));
            view.blocks.put(want.getKey(), want.getValue());
        }
    }

    // ------------------------------------------------------------------
    // The pattern
    // ------------------------------------------------------------------

    /** The great room's west wall (x 0, z 1 to 14), papered two strips a day from the front; only plain solid wall is papered. */
    public static Map<BlockPos, BlockState> wallpaper(ServerLevel level, BlockPos origin, long days) {
        var out = new HashMap<BlockPos, BlockState>();
        int strips = (int) Math.min(STRIPS, STRIPS_A_DAY * (days + 1));
        BlockState paper = ClassicsRegistry.WALLPAPER.get().defaultBlockState();
        for (int i = 0; i < strips; i++) for (int y = 1; y <= 5; y++) {
            var pos = origin.offset(0, y, 1 + i);
            if (!level.isLoaded(pos)) continue;
            var real = level.getBlockState(pos);
            if (real.hasBlockEntity() || !real.isSolidRender(level, pos) || !real.isCollisionShapeFullBlock(level, pos)) continue;
            out.put(pos, paper);
        }
        return out;
    }

    /** A figure in the paper at the edge of sight: it comes where the reader is not looking and goes when they look. */
    static void edge(ServerPlayer p, View view) {
        var eye = p.getEyePosition();var look = p.getLookAngle();
        if (view.edge != null) {
            double angle = angle(look, view.edge.getCenter().subtract(eye));
            if (angle < 35 || view.ticks >= view.edgeUntil || !view.blocks.containsKey(view.edge)) { view.edge = null; view.dirty = true; }
            return;
        }
        if (view.ticks % 20 != 7) return;
        var at = new ArrayList<BlockPos>();
        for (var pos : view.blocks.keySet()) {
            if (!view.blocks.get(pos).is(ClassicsRegistry.WALLPAPER.get()) || pos.distToCenterSqr(eye) > 196) continue;
            double angle = angle(look, pos.getCenter().subtract(eye));
            if (angle >= 45 && angle <= 70) at.add(pos);
        }
        if (at.isEmpty() || p.getRandom().nextInt(3) != 0) return;
        view.edge = at.get(p.getRandom().nextInt(at.size()));view.edgeUntil = view.ticks + 30;view.dirty = true;
    }
    static double angle(Vec3 a, Vec3 b) {
        double len = a.length() * b.length();
        return len < 1e-6 ? 180 : Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, a.dot(b) / len))));
    }

    // ------------------------------------------------------------------
    // The window
    // ------------------------------------------------------------------

    /** The oak outside the great room's front window (x 1-2, z 0), set on the ground three blocks out; only into empty air. */
    public static Map<BlockPos, BlockState> tree(ServerLevel level, BlockPos origin) {
        var out = new HashMap<BlockPos, BlockState>();
        var root = origin.offset(2, 0, -3);
        if (!level.isLoaded(root)) return out;
        int ground = 0;
        for (int y = 0; y >= -4; y--) if (!level.getBlockState(root.atY(origin.getY() + y)).isAir()) { ground = y; break; }
        var base = root.atY(origin.getY() + ground + 1);
        BlockState log = Blocks.OAK_LOG.defaultBlockState(), leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dy = 0; dy < 5; dy++) put(level, out, base.above(dy), log);
        for (int dy = 3; dy <= 6; dy++) {
            int r = dy >= 5 ? 1 : 2;
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                if (r == 2 && Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                if (dx == 0 && dz == 0 && dy < 5) continue;
                if (dy == 6 && Math.abs(dx) + Math.abs(dz) > 1) continue;
                put(level, out, base.offset(dx, dy, dz), leaves);
            }
        }
        return out;
    }
    private static void put(ServerLevel level, Map<BlockPos, BlockState> out, BlockPos pos, BlockState state) {
        if (level.isLoaded(pos) && level.getBlockState(pos).canBeReplaced()) out.put(pos.immutable(), state);
    }

    // ------------------------------------------------------------------
    // The upper ledger
    // ------------------------------------------------------------------

    /** A spruce door standing against the study's west wall, flush to the plaster; nothing is behind it but the wall. */
    static Map<BlockPos, BlockState> door(ServerPlayer p, View view, BlockPos origin) {
        var level = p.serverLevel();
        if (view.door == null || !doorFits(level, view.door)) {
            view.door = null;
            for (int z : DOOR_Z) { var at = origin.offset(2, 1, z); if (doorFits(level, at)) { view.door = at; break; } }
        }
        if (view.door == null) return Map.of();
        boolean open = view.doorOpenUntil > 0;
        return Map.of(view.door, door(DoubleBlockHalf.LOWER, open), view.door.above(), door(DoubleBlockHalf.UPPER, open));
    }
    /** A closed door facing east sits on the west edge of its cell, against the wall; opened, it swings to the side. */
    public static BlockState door(DoubleBlockHalf half, boolean open) {
        return Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HALF, half)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT).setValue(DoorBlock.OPEN, open);
    }
    static boolean doorFits(ServerLevel level, BlockPos at) {
        if (!level.isLoaded(at) || !level.isLoaded(at.west())) return false;
        for (int y = 0; y < 2; y++) {
            var cell = at.above(y);var wall = cell.west();var front = cell.east();
            if (!level.getBlockState(cell).isAir() || !level.getBlockState(front).isAir()) return false;
            var w = level.getBlockState(wall);
            if (w.hasBlockEntity() || !w.isSolidRender(level, wall) || !w.isCollisionShapeFullBlock(level, wall)) return false;
        }
        return level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP);
    }

    /** Opening the door: the client swings it, the server has nothing there; it stays open on plaster a while, then shuts. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void use(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        View view = VIEWS.get(p.getUUID());
        if (view == null || view.door == null || view.level != p.level() || !(e.getPos().equals(view.door) || e.getPos().equals(view.door.above()))) return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if (view.doorOpenUntil > 0) return;
        opened(p, view);
    }
    static void opened(ServerPlayer p, View view) {
        view.doorOpenUntil = view.ticks + 60;view.dirty = true;
        p.displayClientMessage(Component.literal("It opens onto plaster.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }

    // ------------------------------------------------------------------
    // The disappearing exits
    // ------------------------------------------------------------------

    /** The stair tower's treads from the bottom: stairs set the right way up with room above them. */
    public static List<BlockPos> stairs(ServerLevel level, BlockPos origin) {
        var out = new ArrayList<BlockPos>();
        var box = HouseLayout.STAIR_TOWER.box();
        for (int y = box.y0() - 1; y <= box.y1(); y++) for (int x = box.x0(); x <= box.x1(); x++) for (int z = box.z0(); z <= box.z1(); z++) {
            var pos = origin.offset(x, y, z);
            if (!level.isLoaded(pos)) continue;
            var s = level.getBlockState(pos);
            if (s.getBlock() instanceof StairBlock && s.getValue(StairBlock.HALF) == Half.BOTTOM && level.getBlockState(pos.above()).isAir()) out.add(pos);
        }
        out.sort(Comparator.<BlockPos>comparingInt(BlockPos::getY).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
        return out;
    }
    /** The tread the teddy sits on after so many House days: one step higher each day, and then the top. */
    public static int step(int treads, long days) { return treads <= 0 ? -1 : (int) Math.min(treads - 1, days); }

    static void toy(ServerPlayer p, View view, BlockPos origin, long days) {
        if (view.stairs == null || view.stairs.isEmpty()) view.stairs = stairs(p.serverLevel(), origin);
        int i = step(view.stairs.size(), days);
        if (i < 0) return;
        var tread = view.stairs.get(i);
        if (tread.equals(view.toyAt) && view.toy >= 0) return;
        if (view.toy >= 0) p.connection.send(new ClientboundRemoveEntitiesPacket(view.toy));
        var facing = p.serverLevel().getBlockState(tread).getValue(StairBlock.FACING);
        var teddy = LiteraryRegistry.NURSERY.get().defaultBlockState().setValue(NurseryBlock.KIND, NurseryBlock.Kind.TEDDY)
                .setValue(NurseryBlock.FACING, facing.getOpposite());
        // On the low half of the tread, away from the riser.
        view.toy = display(p, teddy, new Vec3(tread.getX() - .25 * facing.getStepX(), tread.getY() + .5, tread.getZ() - .25 * facing.getStepZ()));
        view.toyAt = tread;
    }

    // ------------------------------------------------------------------
    // The congregation
    // ------------------------------------------------------------------

    static boolean inHall(BlockPos origin, Vec3 at) {
        var b = HouseLayout.HALL.box();
        double x = at.x - origin.getX(), y = at.y - origin.getY(), z = at.z - origin.getZ();
        return x >= b.x0() && x < b.x1() + 1 && z >= b.z0() && z < b.z1() + 1 && y >= b.y0() - .5 && y < b.y1() + 1;
    }
    static void hall(ServerPlayer p, View view, CompoundTag own, BlockPos origin, long today) {
        boolean now = inHall(origin, p.position());
        if (now && !view.inHall && view.last != null) {
            var step = p.position().subtract(view.last);
            if (step.horizontalDistanceSqr() < 1e-4) step = p.getLookAngle();
            var way = Direction.getNearest(step.x, 0, step.z);
            if (own.getLong("PewDay") != today || !own.contains("PewDay")) {
                own.putLong("PewDay", today);
                // The first day it always comes; after that, now and then.
                if (!own.getBoolean("PewSeen") || p.getRandom().nextInt(3) == 0) {
                    if (pew(p, view, origin, p.blockPosition(), way)) own.putBoolean("PewSeen", true);
                }
            }
        } else if (!now && view.inHall && view.pew >= 0) {
            p.connection.send(new ClientboundRemoveEntitiesPacket(view.pew));view.pew = -1;
        }
        view.inHall = now;
    }
    /** A single dark oak pew in the hall, its seat turned to face the way the reader came in. */
    public static boolean pew(ServerPlayer p, View view, BlockPos origin, BlockPos entry, Direction way) {
        var level = p.serverLevel();var b = HouseLayout.HALL.box();
        var candidates = new ArrayList<BlockPos>();
        int y = origin.getY() + b.y0();
        if (way.getAxis() == Direction.Axis.X) {
            int x = origin.getX() + (way == Direction.EAST ? b.x1() : b.x0());
            for (int dz : new int[]{0, 1, -1, 2, -2}) candidates.add(new BlockPos(x, y, Math.max(origin.getZ() + b.z0() + 1, Math.min(origin.getZ() + b.z1() - 1, entry.getZ() + dz))));
        } else {
            int z = Math.max(origin.getZ() + b.z0() + 1, Math.min(origin.getZ() + b.z1() - 1, entry.getZ() + 4 * way.getStepZ()));
            for (int x : new int[]{b.x0(), b.x1()}) for (int dz : new int[]{0, way.getStepZ()}) candidates.add(new BlockPos(origin.getX() + x, y, z + dz));
        }
        for (var at : candidates) {
            if (!level.isLoaded(at) || !level.getBlockState(at).isAir() || !level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) continue;
            // A stair's high side is its back: the seat faces away from it, toward the reader.
            var pew = Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, way);
            if (view.pew >= 0) p.connection.send(new ClientboundRemoveEntitiesPacket(view.pew));
            view.pew = display(p, pew, new Vec3(at.getX(), at.getY(), at.getZ()));
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // The little rooms
    // ------------------------------------------------------------------

    /**
     * Where the reader stands still is kept; on a later House day the place they last stood becomes the figure's. The figure is
     * a small carved stand wearing their own head.
     */
    static void figure(ServerPlayer p, View view, CompoundTag own, BlockPos origin, long today) {
        boolean still = p.onGround() && p.getDeltaMovement().horizontalDistanceSqr() < 1e-4 && view.last != null && view.last.distanceToSqr(p.position()) < 1e-4;
        if (still) {
            var rel = p.blockPosition().subtract(origin);
            if (own.contains("Stood") && own.getLong("StoodDay") < today) { own.putLong("Last", own.getLong("Stood"));own.putFloat("LastYaw", own.getFloat("StoodYaw")); }
            if (own.getLong("Stood") != rel.asLong() || own.getLong("StoodDay") != today || !own.contains("Stood")) {
                own.putLong("Stood", rel.asLong());own.putLong("StoodDay", today);own.putFloat("StoodYaw", p.getYRot());
            }
        }
        if (!own.contains("Last")) return;
        var at = origin.offset(BlockPos.of(own.getLong("Last")));
        if (at.equals(view.figureAt) && view.figure >= 0) return;
        if (view.figure >= 0) p.connection.send(new ClientboundRemoveEntitiesPacket(view.figure));
        view.figure = figure(p, at, own.getFloat("LastYaw"));view.figureAt = at;
    }
    static int figure(ServerPlayer p, BlockPos at, float yaw) {
        var stand = EntityType.ARMOR_STAND.create(p.serverLevel());
        stand.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, yaw, 0);stand.setYHeadRot(yaw);stand.setYBodyRot(yaw);
        stand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, (byte) (ArmorStand.CLIENT_FLAG_SMALL | ArmorStand.CLIENT_FLAG_SHOW_ARMS | ArmorStand.CLIENT_FLAG_NO_BASEPLATE));
        var scale = stand.getAttribute(Attributes.SCALE);scale.setBaseValue(.6);
        var head = new ItemStack(Items.PLAYER_HEAD);head.set(DataComponents.PROFILE, new ResolvableProfile(p.getGameProfile()));
        add(p, stand);
        p.connection.send(new ClientboundSetEquipmentPacket(stand.getId(), List.of(Pair.of(EquipmentSlot.HEAD, head))));
        p.connection.send(new ClientboundUpdateAttributesPacket(stand.getId(), List.of(scale)));
        return stand.getId();
    }

    // ------------------------------------------------------------------
    // The returned knocks
    // ------------------------------------------------------------------

    /** The full, plain wall nearest the reader at head height, within four blocks. */
    public static BlockPos nearestWall(ServerPlayer p) {
        var level = p.serverLevel();var head = p.blockPosition().above();
        BlockPos best = null;int bestDistance = Integer.MAX_VALUE;
        for (var dir : Direction.Plane.HORIZONTAL) for (int n = 1; n <= 4; n++) {
            var pos = head.relative(dir, n);
            if (!level.isLoaded(pos)) break;
            var s = level.getBlockState(pos);
            if (s.isAir()) continue;
            if (!s.hasBlockEntity() && s.isCollisionShapeFullBlock(level, pos) && HouseExperience.inManor(p, pos.relative(dir.getOpposite())) && n < bestDistance) { best = pos;bestDistance = n; }
            break;
        }
        return best;
    }
    /** Starts a bout of pounding on the nearest wall: so many blows, then the wall listens. */
    public static boolean pound(ServerPlayer p, int count) {
        View view = VIEWS.get(p.getUUID());
        if (view == null) return false;
        var wall = nearestWall(p);
        if (wall == null) return false;
        view.hill = HILL_POUNDING;view.wall = wall;view.pounds = count;view.pounded = 0;view.knocks = 0;view.hillAt = view.ticks;
        return true;
    }
    static void hill(ServerPlayer p, View view, CompoundTag own, long today) {
        switch (view.hill) {
            case HILL_IDLE -> {
                if (own.getLong("HillDay") == today && own.contains("HillDay")) return;
                if (own.getLong("HillTryDay") != today) { own.putLong("HillTryDay", today);own.putInt("HillTries", 0); }
                if (own.getInt("HillTries") >= 3 || view.ticks < view.hillNext || view.ticks % 20 != 0 || p.getRandom().nextInt(8) != 0) return;
                if (!pound(p, POUNDS_MIN + p.getRandom().nextInt(POUNDS_MAX - POUNDS_MIN + 1))) view.hillNext = view.ticks + 200;
            }
            case HILL_POUNDING -> {
                if ((view.ticks - view.hillAt) % POUND_GAP != 0) return;
                heard(p, view.wall, .6F, .75F);
                if (++view.pounded >= view.pounds) {
                    view.hill = HILL_LISTENING;view.hillAt = view.ticks;view.knocks = 0;
                    if (!own.getBoolean("HillHint")) { own.putBoolean("HillHint", true);p.displayClientMessage(Component.literal("The pounding stops, as if it is listening."), true); }
                }
            }
            case HILL_LISTENING -> {
                boolean settled = view.knocks > 0 && view.ticks - view.lastKnock >= KNOCK_SETTLE;
                if (settled && view.knocks == view.pounds) {
                    // Answered: the wall returns each knock, softly, and is quiet until the next day.
                    own.putLong("HillDay", today);own.putInt("HillAnswered", own.getInt("HillAnswered") + 1);
                    view.hill = HILL_ANSWERING;view.hillAt = view.ticks + 15;view.pounded = 0;
                } else if (settled || view.ticks - view.hillAt > LISTEN) {
                    own.putInt("HillTries", own.getInt("HillTries") + 1);
                    view.hill = HILL_IDLE;view.hillNext = view.ticks + 600;
                }
            }
            case HILL_ANSWERING -> {
                if (view.ticks < view.hillAt || (view.ticks - view.hillAt) % 10 != 0) return;
                heard(p, view.wall, .4F, .9F);
                if (++view.pounded >= view.pounds) view.hill = HILL_IDLE;
            }
            default -> view.hill = HILL_IDLE;
        }
    }
    /** A knock on the listening wall (a struck block, the House keeps it whole). */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void knock(PlayerInteractEvent.LeftClickBlock e) {
        if (e.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || !(e.getEntity() instanceof ServerPlayer p)) return;
        knocked(p, e.getPos());
    }
    public static boolean knocked(ServerPlayer p, BlockPos pos) {
        View view = VIEWS.get(p.getUUID());
        if (view == null || view.hill != HILL_LISTENING || view.wall == null || view.level != p.level() || pos.distManhattan(view.wall) > 1) return false;
        view.knocks++;view.lastKnock = view.ticks;
        return true;
    }
    private static void heard(ServerPlayer p, BlockPos at, float volume, float pitch) {
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR),
                SoundSource.BLOCKS, at.getX() + .5, at.getY() + .5, at.getZ() + .5, volume, pitch, p.getRandom().nextLong()));
    }

    // ------------------------------------------------------------------
    // Twenty seconds
    // ------------------------------------------------------------------

    static boolean night(ServerPlayer p) { long t = HouseCalendar.timeOfDay(p.server); return t >= 13000 && t < 23000; }
    /** The manor window glass the reader stands before and looks into, within three and a half blocks, or null. */
    static HouseLayout.Window facing(ServerPlayer p, BlockPos origin, double degrees) {
        var eye = p.getEyePosition();var look = p.getLookAngle();
        for (var w : HouseLayout.WINDOWS) {
            var glass = glass(origin, w);
            if (glass.distanceToSqr(eye) > 12.25 || Math.abs(glass.y - eye.y) > 1.6) continue;
            if (angle(look, glass.subtract(eye)) <= degrees) return w;
        }
        return null;
    }
    /** The middle of a window's glass. */
    static Vec3 glass(BlockPos origin, HouseLayout.Window w) {
        double a = (w.a0() + w.a1() + 1) / 2.0, y = (w.y0() + w.y1() + 1) / 2.0, plane = w.plane() + .5;
        return switch (w.face()) {
            case NORTH, SOUTH -> new Vec3(origin.getX() + a, origin.getY() + y, origin.getZ() + plane);
            default -> new Vec3(origin.getX() + plane, origin.getY() + y, origin.getZ() + a);
        };
    }
    static void backseat(ServerPlayer p, View view, CompoundTag own, BlockPos origin, long today) {
        if (view.seat) {
            boolean turned = facing(p, origin, 40) == null;
            if (turned || view.ticks >= view.seatUntil || p.position().distanceToSqr(view.seatFrom) > .36) seat(p, view, 0);
            return;
        }
        boolean still = view.stillAt != null && p.position().distanceToSqr(view.stillAt) < .01 && Math.abs(net.minecraft.util.Mth.wrapDegrees(p.getYRot() - view.stillYaw)) < 15;
        view.still = still ? view.still + 1 : 0;
        if (!still) { view.stillAt = p.position();view.stillYaw = p.getYRot(); }
        if (view.still < STILL || !night(p) || (own.contains("SeatDay") && own.getLong("SeatDay") == today)) return;
        var w = facing(p, origin, 30);
        if (w == null) return;
        own.putLong("SeatDay", today);own.putInt("Seats", own.getInt("Seats") + 1);
        stowaway(p, view, origin, w);
    }
    /** Puts the stowaway behind the reader's shoulder as the black glass shows it: the point behind them, reflected in the pane. */
    public static Vec3 stowaway(ServerPlayer p, View view, BlockPos origin, HouseLayout.Window w) {
        float yaw = p.getYRot();
        var forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        var right = new Vec3(-forward.z, 0, forward.x);
        var behind = p.position().subtract(forward.scale(.9)).add(right.scale(.4));
        var glass = glass(origin, w);
        Vec3 seen;float seenYaw;
        if (w.face() == HouseLayout.Face.NORTH || w.face() == HouseLayout.Face.SOUTH) { seen = new Vec3(behind.x, behind.y, 2 * glass.z - behind.z);seenYaw = 180 - yaw; }
        else { seen = new Vec3(2 * glass.x - behind.x, behind.y, behind.z);seenYaw = -yaw; }
        view.seat = true;view.seatUntil = view.ticks + SEAT_LEASE;view.seatFrom = p.position();
        var tag = new CompoundTag();tag.putDouble("X", seen.x);tag.putDouble("Y", seen.y);tag.putDouble("Z", seen.z);tag.putFloat("Yaw", seenYaw);tag.putInt("Lease", SEAT_LEASE + 20);
        seat(p, view, 1, tag);
        return seen;
    }
    private static void seat(ServerPlayer p, View view, int on) { seat(p, view, on, null); }
    private static void seat(ServerPlayer p, View view, int on, CompoundTag stowaway) {
        if (view != null) view.seat = on > 0;
        var remembers = new CompoundTag();if (stowaway != null) remembers.put("Stowaway", stowaway);
        var tag = new CompoundTag();tag.putUUID("Reader", p.getUUID());tag.put("Remembers", remembers);
        HousePackets.send(p, new LiteraryViewPayload(-1, 1, 0, tag));
    }

    // ------------------------------------------------------------------
    // Packet figures
    // ------------------------------------------------------------------

    /** A block shown in the air for this reader only, its lower north-west corner at {@code at}; it has no collision. */
    static int display(ServerPlayer p, BlockState state, Vec3 at) {
        var display = EntityType.BLOCK_DISPLAY.create(p.serverLevel());
        var tag = new CompoundTag();display.saveWithoutId(tag);tag.put("block_state", NbtUtils.writeBlockState(state));display.load(tag);
        display.moveTo(at.x, at.y, at.z, 0, 0);
        add(p, display);
        return display.getId();
    }
    private static void add(ServerPlayer p, Entity entity) {
        p.connection.send(new ClientboundAddEntityPacket(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(),
                entity.getXRot(), entity.getYRot(), entity.getType(), 0, Vec3.ZERO, entity.getYHeadRot()));
        var values = entity.getEntityData().getNonDefaultValues();
        if (values != null) p.connection.send(new ClientboundSetEntityDataPacket(entity.getId(), values));
    }

    /** What this reader is being shown now, for tests and the status line. */
    public static List<String> status(ServerPlayer p) {
        View view = VIEWS.get(p.getUUID());
        if (view == null) return List.of("Nothing remembered in view.");
        return List.of("Remembered in view: " + view.blocks.size() + " blocks; door " + view.door + (view.doorOpenUntil > 0 ? " (open)" : "")
                + "; toy " + view.toyAt + "; figure " + view.figureAt + "; pew " + (view.pew >= 0) + "; hill " + view.hill + "/" + view.pounds + "/" + view.knocks
                + "; stowaway " + view.seat + ".");
    }
    /** Test access to the reader's live view. */
    public static View view(ServerPlayer p) { return VIEWS.get(p.getUUID()); }
    public static Map<BlockPos, BlockState> shown(ServerPlayer p) { var v = VIEWS.get(p.getUUID()); return v == null ? Map.of() : Map.copyOf(v.blocks); }
    public static BlockPos door(ServerPlayer p) { var v = VIEWS.get(p.getUUID()); return v == null ? null : v.door; }
    public static BlockPos toy(ServerPlayer p) { var v = VIEWS.get(p.getUUID()); return v == null ? null : v.toyAt; }
    public static BlockPos figure(ServerPlayer p) { var v = VIEWS.get(p.getUUID()); return v == null ? null : v.figureAt; }
    public static boolean pewShown(ServerPlayer p) { var v = VIEWS.get(p.getUUID()); return v != null && v.pew >= 0; }
    public static boolean seated(ServerPlayer p) { var v = VIEWS.get(p.getUUID()); return v != null && v.seat; }
    public static int hillState(ServerPlayer p) { var v = VIEWS.get(p.getUUID()); return v == null ? -1 : v.hill; }
    public static void forget(ServerPlayer p) { VIEWS.remove(p.getUUID()); }
}
