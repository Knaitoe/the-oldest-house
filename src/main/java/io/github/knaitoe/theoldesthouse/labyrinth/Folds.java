package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

/**
 * 0.4.394, R3 of docs/HALLS_TRIALS_AND_TIME_DESIGN.md, in the form the owner set (November 8): "looping doors need to work for every
 * player. They should teleport every player to the same general location, within a few hallways of each other. Or transfer all to the
 * same spot. But it can't teleport one player while letting the others progress. That could break group play." Built when the owner
 * asked for "the rest of the rules".
 * <p>
 * A fold is a door, not a chance. From {@link #FOLD_DEPTH}, about one ordinary door in {@link #FOLD_IN} of an ordinary hall, at most one
 * a hall, opens back onto a hall already passed on the same route, {@link #MIN_BACK} to {@link #MAX_BACK} crossings back, and depth drops
 * to match. Which door folds, and how far, is decided once for the hall as reached by its route (the shared halls key) and kept for
 * everyone, so whoever takes it, alone or together, lands in the same earlier hall, and taking it again lands where it did before.
 * <p>
 * Readers who go through one door together go together: whoever follows a fold through the same door within {@link #WINDOW} ticks lands
 * where the first did and loses as many crossings; whoever follows a reader who went on through it goes on too, never folded alone.
 * Never in a hall entered straight from a story, never through a story's, trial's or the staircase's door, never the hall's only way on.
 */
public final class Folds {
    public static final int FOLD_DEPTH = LabyrinthPacing.DEEP_DEPTH, FOLD_IN = 7, MIN_BACK = 2, MAX_BACK = 4;
    /** How long friends following through the same door go where the first went, in ticks (as GroupCrossings). */
    public static final int WINDOW = GroupCrossings.WINDOW;
    /** Shared: which door of a hall, as reached by its route, folds and how far. */
    public static final String STATE = "folds_0394";
    /** Personal: how many folds a reader has been through. */
    public static final String TAKEN = "folds_0394_taken";
    private static final long SALT = 0x0F01DED5EEDL;

    /** What one crossing of a door does: fold back {@code back} crossings to {@code landing}, or go on (to the leader's hall, if set). */
    public record Plan(int back, @Nullable LabyrinthData.Waypoint landing, @Nullable String withLeader) {
        public static final Plan ON = new Plan(0, null, null);
        public boolean folds() { return landing != null && back > 0; }
    }
    private record Crossing(UUID leader, int tick, @Nullable LabyrinthData.Waypoint landing, int back, @Nullable String place) {}
    private static final Map<String, Crossing> RECENT = new HashMap<>();
    private Folds() {}

    private static String key(LabyrinthData.Door door) { return door.dimension.location() + "|" + door.id + "|" + door.lower.asLong(); }
    private static String hallKey(LabyrinthData data, UUID player, LabyrinthPlace here, long salt) { return Long.toUnsignedString(data.nodeKey(player, here) ^ salt); }

    /** The doors a hall deals, in a fixed order. */
    private static List<LabyrinthData.Door> doors(LabyrinthData data, LabyrinthPlace here) {
        var out = new ArrayList<LabyrinthData.Door>();
        for (var spec : here.doors()) {
            var door = data.door(here.doorId(spec));
            if (door != null && LabyrinthData.DEALT.equals(door.destination) && !OnwardDoors.isOnward(door)) out.add(door);
        }
        out.sort(Comparator.comparing(d -> d.id));
        return out;
    }

    /** Whether this hall, as this reader reached it, may hold a fold: ordinary, deep enough, and not entered straight from a story. */
    static boolean hallMayFold(LabyrinthData data, UUID player, BlockPos origin, LabyrinthPlace here) {
        if (!LabyrinthPacing.ordinary(here) || LabyrinthPacing.loop(here) || data.returnDepth(player) < FOLD_DEPTH) return false;
        var top = data.peekReturn(player);
        if (top == null) return false;
        var before = LabyrinthPlaces.placeAt(origin, BlockPos.containing(top.pos()));
        return before == null || !before.isVignette();
    }

    /**
     * The door this reader came out of {@code back} crossings ago, if a fold that far lands in an ordinary hall other than this one:
     * every crossing between is a door of the House's interior, and none was held by the quiet rooms.
     */
    @Nullable
    public static LabyrinthData.Waypoint landing(LabyrinthData data, UUID player, BlockPos origin, LabyrinthPlace here, int back) {
        if (back < 1) return null;
        var way = data.returns(player);
        if (way.size() < back) return null;
        for (int i = 0; i < back; i++) {
            var w = way.get(i);
            if (!w.door() || w.held() || !w.dimension().equals(HouseDimensions.INTERIOR)) return null;
        }
        var w = way.get(back - 1);
        var place = LabyrinthPlaces.placeAt(origin, BlockPos.containing(w.pos()));
        if (place == null || place == here || !LabyrinthPacing.ordinary(place) || LabyrinthPacing.loop(place)) return null;
        return w;
    }

    /** Whether a reader's own deal behind this door is an ordinary hall it could stand in for, and the hall has another way on. */
    private static boolean foldable(LabyrinthData data, UUID player, LabyrinthPlace here, LabyrinthData.Door door) {
        var deal = data.deal(player, door);
        var place = deal == null ? null : LabyrinthPlace.byId(deal.place());
        if (place == null || !LabyrinthPacing.ordinary(place) || Wedges.pin(data, player, door.id) != null || VignetteGate.dormant(data, player, door)) return false;
        return LabyrinthDealer.hasWayOn(data, player, here, door);
    }

    /**
     * Which door of this hall folds, and how far, decided by the first reader to use one of its doors and kept for everyone who comes
     * the same way. Spectators never decide (they read what is kept).
     */
    public static CompoundTag decide(LabyrinthData data, UUID player, BlockPos origin, LabyrinthPlace here, long salt, boolean participant) {
        String key = hallKey(data, player, here, salt);
        var kept = data.stateEntry(STATE, key);
        // Shallower than the folds, nothing is decided or kept: the same route reaches the same depth, so it never folds there.
        if (kept.getBoolean("Decided") || !participant || data.returnDepth(player) < FOLD_DEPTH) return kept;
        var tag = new CompoundTag();
        tag.putBoolean("Decided", true);
        if (hallMayFold(data, player, origin, here)) {
            var random = RandomSource.create(data.nodeKey(player, here) ^ salt ^ SALT);
            for (var door : doors(data, here)) {
                int roll = random.nextInt(FOLD_IN), want = MIN_BACK + random.nextInt(MAX_BACK - MIN_BACK + 1);
                if (roll != 0 || !foldable(data, player, here, door)) continue;
                int back = want;
                while (back >= MIN_BACK && landing(data, player, origin, here, back) == null) back--;
                if (back < MIN_BACK) continue;
                tag.putString("Door", door.id);
                tag.putInt("Back", back);
                break;
            }
        }
        data.setBoundedStateEntry(STATE, key, tag, 2048);
        return tag;
    }

    /** How many crossings back this door folds for this reader in this hall, or 0. */
    public static int back(LabyrinthData data, UUID player, BlockPos origin, LabyrinthPlace here, LabyrinthData.Door door, long salt, boolean participant) {
        var tag = decide(data, player, origin, here, salt, participant);
        if (!door.id.equals(tag.getString("Door")) || Wedges.pin(data, player, door.id) != null) return 0;
        int back = tag.getInt("Back");
        return landing(data, player, origin, here, back) == null ? 0 : back;
    }

    /**
     * What this reader's use of the door does now. Friends who go through one door together go together: a fold the first took takes the
     * rest, and a door the first went on through takes the rest on.
     */
    public static Plan plan(LabyrinthData data, UUID player, BlockPos origin, LabyrinthPlace here, LabyrinthData.Door door, long salt, int now, boolean participant) {
        RECENT.values().removeIf(c -> now - c.tick() > WINDOW || now < c.tick());
        var led = RECENT.get(key(door));
        if (led != null && !led.leader().equals(player)) {
            if (led.landing() != null) return new Plan(led.back(), led.landing(), null);
            if (back(data, player, origin, here, door, salt, participant) == 0) return Plan.ON;
            var their = led.place() == null ? null : LabyrinthPlace.byId(led.place());
            return new Plan(0, null, their != null && LabyrinthPacing.ordinary(their) ? their.id() : null);
        }
        int back = back(data, player, origin, here, door, salt, participant);
        return back == 0 ? Plan.ON : new Plan(back, landing(data, player, origin, here, back), null);
    }

    /** The first through a door holds what it did for the friends behind them. */
    public static void record(UUID player, LabyrinthData.Door door, Plan plan, @Nullable String place, int now) {
        var led = RECENT.get(key(door));
        if (led == null || now - led.tick() > WINDOW || now < led.tick() || led.leader().equals(player))
            RECENT.put(key(door), new Crossing(player, now, plan.folds() ? plan.landing() : null, plan.folds() ? plan.back() : 0, plan.folds() ? null : place));
    }

    /** Using a dealt door of an ordinary hall: folds the reader back if it is a fold (or a friend just folded through it). */
    public static boolean use(ServerPlayer p, LabyrinthData.Door door) {
        if (p.isSpectator() || door.command || !LabyrinthData.DEALT.equals(door.destination) || !door.dimension.equals(HouseDimensions.INTERIOR)) return false;
        BlockPos origin = HouseSavedData.get(p.server).houseOrigin();
        if (origin == null) return false;
        var here = LabyrinthPlaces.placeAt(origin, door.lower);
        if (here == null || !LabyrinthPacing.ordinary(here)) return false;
        var data = LabyrinthData.get(p.server);
        int now = p.server.getTickCount();
        var plan = plan(data, p.getUUID(), origin, here, door, origin.asLong(), now, true);
        if (plan.withLeader() != null) data.deal(p.getUUID(), door, plan.withLeader(), false, false);
        var deal = data.deal(p.getUUID(), door);
        record(p.getUUID(), door, plan, deal == null ? null : deal.place(), now);
        return plan.folds() && LabyrinthDoors.fold(p, door, plan.landing(), plan.back());
    }

    /**
     * After the fold's fade: the crossings folded over are gone from the way back, and the hall landed in keeps what lay behind its
     * doors on this route. Public for the tests.
     */
    public static void landed(LabyrinthData data, UUID player, BlockPos origin, LabyrinthData.Waypoint landing, int back) {
        for (int i = 0; i < back; i++) if (data.popReturn(player) == null) break;
        var place = LabyrinthPlaces.placeAt(origin, BlockPos.containing(landing.pos()));
        if (place != null) LabyrinthDealer.arriveAt(data, player, place, origin.asLong(), true);
        var own = data.stateEntry(TAKEN, player.toString());
        own.putInt("Taken", own.getInt("Taken") + 1);
        data.setStateEntry(TAKEN, player.toString(), own);
    }

    static void arrived(ServerPlayer p, LabyrinthData.Waypoint landing, int back) {
        BlockPos origin = HouseSavedData.get(p.server).houseOrigin();
        if (origin == null) return;
        var data = LabyrinthData.get(p.server);
        landed(data, p.getUUID(), origin, landing, back);
        // Said once, the first time: a hall already walked, by the door it was left by.
        if (data.stateEntry(TAKEN, p.getUUID().toString()).getInt("Taken") == 1)
            p.displayClientMessage(Component.literal("This hall again, by the door it was left by.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
        io.github.knaitoe.theoldesthouse.house.PlaytestLog.event(p, "fold", "back", back, "depth", data.returnDepth(p.getUUID()));
    }

    /** Fixtures only. */
    public static void forget() { RECENT.clear(); }
}
