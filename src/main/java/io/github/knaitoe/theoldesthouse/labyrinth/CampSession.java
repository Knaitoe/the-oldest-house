package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Camp Blood's last session (0.4.121). Camp Wanaki's third session was closed the morning after a camper went missing at
 * lights out, and nobody told the director. He keeps its day still: one shared clock, running while anyone is in the camp,
 * goes through the six periods of the posted schedule, and at each the mess-hall bell rings and the PA reads the next line.
 * His walk follows the schedule, so a reader who has read it knows where he will be. Ringing the bell in the mess hall
 * calls him there, once for each reader. Under a bunk his boots go past; keeping still keeps them going.
 *
 * <p>The camp's papers keep the rest: the schedule on its stand and in every cabin, three counselors' journals, a letter
 * home in each footlocker, the nurse's log in the first-aid cabinets and the director's unsigned incident report. J.'s essay
 * is still the source.
 */
public final class CampSession {
    private static final int F = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final LabyrinthPlace PLACE = LabyrinthPlace.CAMP_BLOOD;
    private static final String STATE = "CampSession0121";
    /** One period of the schedule, in ticks of time spent in the camp; and how long the bell keeps him at the mess hall. */
    public static final int PERIOD = 900, BELL = 600;
    /** A period: its name on the schedule, where it is held, what the PA says, and where he walks during it. */
    public record Period(String name, String place, String call, List<BlockPos> walk) {}
    public static final List<Period> PERIODS = List.of(
            new Period("Reveille and flag", "Flagpole", "Reveille. All campers to the flagpole.",
                    List.of(new BlockPos(-5, 0, -16), new BlockPos(-10, 0, -10), new BlockPos(-3, 0, -10))),
            new Period("Breakfast", "Mess hall", "Breakfast. Cabins to the mess hall, in order.",
                    List.of(new BlockPos(3, 0, -70), new BlockPos(6, 0, -68), new BlockPos(3, 0, -66))),
            new Period("Cabin inspection", "West row", "Cabin inspection, west row. Beds made, trunks shut.",
                    List.of(new BlockPos(-20, 0, -26), new BlockPos(-20, 0, -57), new BlockPos(-20, 0, -83))),
            new Period("Crafts", "East row", "Crafts. East row.",
                    List.of(new BlockPos(20, 0, -26), new BlockPos(20, 0, -57), new BlockPos(20, 0, -83))),
            new Period("Campfire", "Fire circle", "Campfire. Fire circle.",
                    List.of(new BlockPos(4, 0, -45), new BlockPos(6, 0, -48), new BlockPos(8, 0, -43))),
            new Period("Lights out", "Rounds to the road", "Lights out. Counselors, rounds to the road.",
                    List.of(new BlockPos(0, 0, -30), new BlockPos(0, 0, -60), new BlockPos(0, 0, -88), new BlockPos(2, 0, -96))));
    /** The mess hall, its door and its bell; the flagpole and the schedule's stand; the PA pole. */
    public static final BlockPos BELL_AT = new BlockPos(3, 0, -68), STAND = new BlockPos(-5, 0, -14), FLAGPOLE = new BlockPos(-7, 0, -12),
            CLIPBOARD = new BlockPos(8, 1, -70), RADIO = new BlockPos(10, 0, -73), PA = new BlockPos(4, 0, -75);
    /** The three counselors' journals, in the first west cabin, the middle east cabin and the last west cabin. */
    public static final List<BlockPos> JOURNALS = List.of(new BlockPos(-20, 0, -21), new BlockPos(20, 0, -52), new BlockPos(-20, 0, -78));
    private CampSession() {}

    // ------------------------------------------------------------------------------------------------ the clock

    private static CompoundTag state(LiteraryActor a) { return a.getPersistentData().getCompound(STATE); }
    private static void state(LiteraryActor a, CompoundTag s) { a.getPersistentData().put(STATE, s); }
    public static int clock(LiteraryActor a) { return state(a).getInt("Clock"); }
    /** Sets the session's clock: for tests, which cannot wait out a period. */
    public static void clock(LiteraryActor a, int clock) { var s = state(a); s.putInt("Clock", clock); state(a, s); }
    public static int period(LiteraryActor a) { return Math.floorMod(clock(a) / PERIOD, PERIODS.size()); }
    public static boolean bellCalling(LiteraryActor a) { var s = state(a); return s.getInt("Clock") < s.getInt("BellUntil"); }

    /**
     * One tick of the session while somebody is in the camp. At each new period the bell rings and the PA reads the next
     * line to everyone there. Returns whether a period began, so the walk starts over.
     */
    public static boolean advance(ServerLevel l, BlockPos b, LiteraryActor a, List<ServerPlayer> readers) {
        var s = state(a);
        int clock = s.getInt("Clock") + 1, before = Math.floorMod((clock - 1) / PERIOD, PERIODS.size()), now = Math.floorMod(clock / PERIOD, PERIODS.size());
        s.putInt("Clock", clock);
        state(a, s);
        if (clock % PERIOD != 0 && clock != 1) return false;
        var bell = b.offset(BELL_AT);
        l.playSound(null, bell, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 4, 1);
        if (l.getBlockEntity(bell) instanceof BellBlockEntity entity) entity.onHit(Direction.EAST);
        var period = PERIODS.get(now);
        for (var p : readers) p.displayClientMessage(Component.literal("The PA crackles, and a tired voice reads from a list: “" + period.call() + "”").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
        return before != now || clock == 1;
    }

    /** Where he walks now: the period's places, or the mess hall while the bell is calling him. */
    public static List<BlockPos> walk(LiteraryActor a) { return bellCalling(a) ? PERIODS.get(1).walk() : PERIODS.get(period(a)).walk(); }

    /** Under a bunk, his boots going by: heard close, never seen; a reader who keeps still is passed. */
    public static void boots(LiteraryActor a, List<ServerPlayer> readers, boolean moving) {
        if (!moving) return;
        for (var p : readers) {
            if (!CampBedCrawl.under(p) || a.distanceToSqr(p) > 5 * 5) continue;
            if (a.tickCount % 8 == 0) ElkDrag.sound(p, a.position(), SoundEvents.WOOD_STEP, 1, .55F);
            if (a.tickCount % 80 == 0) p.displayClientMessage(Component.literal("His boots go past the edge of the blanket. Keep still."), true);
        }
    }

    // ------------------------------------------------------------------------------------------------ the camp's papers and the bell

    private static @Nullable LiteraryActor killer(ServerPlayer p) {
        var world = LiteraryVignettes.shared(LabyrinthData.get(p.server), PLACE);
        return world.hasUUID("Killer") && p.serverLevel().getEntity(world.getUUID("Killer")) instanceof LiteraryActor a ? a : null;
    }

    /** The right-clicks the camp answers beyond its source and ending. The caller saves the record. */
    public static boolean click(ServerPlayer p, BlockPos b, BlockPos rel, CompoundTag own) {
        var l = p.serverLevel();
        var at = b.offset(rel);
        var s = l.getBlockState(at);
        if (s.is(Blocks.BELL) && rel.equals(BELL_AT)) { ring(p, b, own); return true; }
        if (CampSessionBlock.is(s, CampSessionBlock.Kind.SCHEDULE_STAND) || s.is(LiteraryRegistry.CAMP_DECOR.get()) && s.getValue(CampDecorBlock.KIND) == CampDecorBlock.Kind.NOTICE) {
            own.putBoolean("Schedule", true);
            LiteraryVignettes.open(p, PLACE, own, "Schedule", schedule(), at);
            return true;
        }
        if (CampSessionBlock.is(s, CampSessionBlock.Kind.JOURNAL)) {
            int i = JOURNALS.indexOf(rel);
            if (i < 0) return false;
            LiteraryVignettes.open(p, PLACE, own, "Journal" + i, journal(i), at);
            return true;
        }
        if (CampSessionBlock.is(s, CampSessionBlock.Kind.CLIPBOARD) || CampSessionBlock.is(s, CampSessionBlock.Kind.RADIO)) {
            LiteraryVignettes.open(p, PLACE, own, "Incident", incident(), at);
            return true;
        }
        if (s.is(LiteraryRegistry.CAMP_DECOR.get())) {
            var kind = s.getValue(CampDecorBlock.KIND);
            if (kind == CampDecorBlock.Kind.TRUNK) { int i = s.getValue(CampDecorBlock.STAGE); LiteraryVignettes.open(p, PLACE, own, "Letter" + i, letter(i), at); return true; }
            if (kind == CampDecorBlock.Kind.FIRST_AID) { LiteraryVignettes.open(p, PLACE, own, "NurseLog", nurse(), at); return true; }
        }
        return false;
    }

    /** The mess-hall bell: it calls him there, once for each reader. After that it is only a bell. */
    public static void ring(ServerPlayer p, BlockPos b, CompoundTag own) {
        var l = p.serverLevel();
        var bell = b.offset(BELL_AT);
        l.playSound(null, bell, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 4, .9F);
        if (l.getBlockEntity(bell) instanceof BellBlockEntity entity) entity.onHit(Direction.EAST);
        if (own.getBoolean("RangBell")) { p.displayClientMessage(Component.literal("It rings, and nothing in the camp answers it this time."), true); return; }
        own.putBoolean("RangBell", true);
        var a = killer(p);
        if (a != null) {
            var s = state(a);
            s.putInt("BellUntil", s.getInt("Clock") + BELL);
            state(a, s);
            CarcassHunt.divert(a);
        }
        p.displayClientMessage(Component.literal("The bell carries across the whole camp. Somewhere a whistle answers it, and footsteps turn toward the mess hall."), true);
    }

    public static ItemStack schedule() {
        var lines = new StringBuilder("CAMP WANAKI\nSESSION THREE\nDAILY SCHEDULE\n\n");
        for (var period : PERIODS) lines.append(period.name()).append("\n  ").append(period.place()).append('\n');
        return HouseWriting.book("Daily schedule", "R. Hale, Director", HouseWriting.WritingStyle.PLAIN, List.of(lines.toString(),
                "The bell in the mess hall is for emergencies only. Do not ring it for a joke.\n\nR. Hale, Director\n\n"
                        + "Someone has crossed out THREE and written it in again, darker, and dated it every day since."));
    }

    static ItemStack journal(int i) {
        return switch (i) {
            case 0 -> HouseWriting.book("Counselor's journal", "Dana", HouseWriting.WritingStyle.WILL, List.of(
                    "Week two. Mr. Hale runs the day off his clipboard like a train timetable. Reveille, flag, breakfast, inspection. If a cabin is late to the flagpole he stands there with the whistle until it comes.",
                    "The kids call this place Camp Blood. He doesn't think that's funny. Ray says Hale had a boy who drowned here the first summer. I asked Hale about him and he told me the time. Not the time of anything. Just the time, and what we were meant to be doing."));
            case 1 -> HouseWriting.book("Counselor's journal", "Ray", HouseWriting.WritingStyle.WILL, List.of(
                    "Lights out, then rounds. Hale does the rounds himself now, all the way to the service road and back, with a flashlight he never switches on. He says the road is where they get out. I said get out of what.",
                    "Missing at lights out: one camper, Cabin Six. Hale rang the bell. He never rings the bell. Everybody came to the mess hall and sat in the dark, and he walked the cabins alone, slowly, in the right order."));
            default -> HouseWriting.book("Counselor's journal", "Pat", HouseWriting.WritingStyle.WILL, List.of(
                    "They've closed the session. The parents are coming in the morning. Nobody has told Hale, or somebody has and he didn't hear. The PA still goes at reveille. There is nobody at the microphone.",
                    "I'm writing this under the bunk. He's doing inspection. If I keep still he goes past; you can see his boots under the edge of the blanket, very clean. Reveille, flag, breakfast. I know where he'll be. It's the only thing here I know."));
        };
    }

    static ItemStack letter(int i) {
        String[] letters = {
                "Dear Mom,\n\nThe lake is cold and I passed the deep end test. We have a schedule for everything, even being bored. Mr. Hale says a camper who knows the schedule is never lost.\n\nLove, Kevin",
                "Dear Dad,\n\nPlease come and get me. Not because of anything. Just come early. You don't have to tell Mom.\n\nTina",
                "Dear Grandma,\n\nWe sing at the campfire. Mr. Hale doesn't sing, he watches the road. Thank you for the stamps.\n\nLove from Ben",
                "Mom and Dad,\n\nA boy from Cabin Six didn't come back from the latrine, and they rang the bell and we all sat in the mess hall in the dark. The counselors said it was a drill.\n\nMolly",
                "Dear Aunt Ruth,\n\nI've learned that if you hide under the bunk you have to keep still, because the boards tell on you. Ha ha. See you in August.\n\nSam",
                "Dear Mom,\n\nThey say the session's over but nobody has come for us. The PA still says reveille. I went to the flagpole because everybody did."};
        return HouseWriting.book("Letter home", "A camper", HouseWriting.WritingStyle.CHILD, List.of(letters[Math.floorMod(i, letters.length)]));
    }

    static ItemStack nurse() {
        return HouseWriting.book("Infirmary log", "The camp nurse", HouseWriting.WritingStyle.WILL, List.of(
                "Aug 2: splinter, Cabin Two.\nAug 3: homesick, Cabin Five; back to bed after cocoa.\nAug 5: two blisters, one sprained wrist (crafts).\nAug 7: Mr. Hale, a cut across the palm. Would not say how. Dressed it. He asked me the time twice.",
                "Aug 9: no entries after lights out.\nAug 10:\nAug 11:\nAug 12:\n\nThe lines are empty, but somebody has gone on dating them, in the same pencil, to the foot of the page and onto the next."));
    }

    static ItemStack incident() {
        return HouseWriting.book("Incident report", "Camp Wanaki", HouseWriting.WritingStyle.PLAIN, List.of(
                "INCIDENT REPORT\nSession Three, night of August 9.\n\nAt lights out one camper (Cabin Six) was not in bed. The director rang the emergency bell at 10:40. Campers were held in the mess hall. The director searched the cabins and the service road alone.",
                "At six the director rang reveille as usual and was found at the flagpole. The camper was not found. Session Three was closed by the board on August 10.\n\nDirector's signature: ________"));
    }

    // ------------------------------------------------------------------------------------------------ building

    private static void set(ServerLevel l, BlockPos b, int x, int y, int z, BlockState s) { BuildBlocks.set(l, b.offset(x, y, z), s, F); }
    private static void set(ServerLevel l, BlockPos b, BlockPos r, BlockState s) { BuildBlocks.set(l, b.offset(r), s, F); }
    private static void piece(ServerLevel l, BlockPos b, BlockPos r, CampSessionBlock.Kind k, Direction f) { set(l, b, r, CampSessionBlock.of(k, f)); }

    /** What the last session left: the mess hall's inside, the bell, the flagpole and its schedule, the PA, journals and carved planks. */
    public static void build(ServerLevel l, BlockPos b) {
        // The mess hall (its walls and roof are LiteraryRooms'): two long benches either side of a table, the radio, the report.
        for (int z = -72; z <= -68; z++) {
            piece(l, b, new BlockPos(8, 0, z), CampSessionBlock.Kind.MESS_TABLE, Direction.EAST);
            piece(l, b, new BlockPos(7, 0, z), CampSessionBlock.Kind.BENCH, Direction.EAST);
            piece(l, b, new BlockPos(9, 0, z), CampSessionBlock.Kind.BENCH, Direction.EAST);
        }
        piece(l, b, CLIPBOARD, CampSessionBlock.Kind.CLIPBOARD, Direction.EAST);
        piece(l, b, new BlockPos(8, 1, -72), CampSessionBlock.Kind.FLASHLIGHT, Direction.NORTH);
        piece(l, b, RADIO, CampSessionBlock.Kind.RADIO, Direction.WEST);
        set(l, b, BELL_AT, Blocks.BELL.defaultBlockState().setValue(BellBlock.FACING, Direction.WEST));
        // The PA horn on its pole by the mess hall, pointing down the camp.
        for (int y = 0; y < 3; y++) set(l, b, PA.above(y), Blocks.SPRUCE_FENCE.defaultBlockState());
        piece(l, b, PA.above(3), CampSessionBlock.Kind.PA_HORN, Direction.SOUTH);
        // The flagpole at the gate, its flag, and the schedule on its stand.
        for (int y = 0; y < 7; y++) set(l, b, FLAGPOLE.above(y), Blocks.SPRUCE_FENCE.defaultBlockState());
        set(l, b, FLAGPOLE.above(6).east(), Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.EAST));
        piece(l, b, STAND, CampSessionBlock.Kind.SCHEDULE_STAND, Direction.SOUTH);
        // A journal on a crate by the door in three of the cabins, initials cut into every cabin's walls.
        for (var at : JOURNALS) piece(l, b, at, CampSessionBlock.Kind.JOURNAL, Direction.NORTH);
        for (int x : new int[]{-20, 20}) for (int z : new int[]{-26, -57, -83}) {
            piece(l, b, new BlockPos(x - 3, 1, z - 6), CampSessionBlock.Kind.INITIALS, Direction.SOUTH);
            piece(l, b, new BlockPos(x + 2, 1, z + 6), CampSessionBlock.Kind.INITIALS, Direction.NORTH);
        }
        // A flashlight dropped on the gravel near the road, switched off.
        piece(l, b, new BlockPos(1, 0, -92), CampSessionBlock.Kind.FLASHLIGHT, Direction.EAST);
    }
}
