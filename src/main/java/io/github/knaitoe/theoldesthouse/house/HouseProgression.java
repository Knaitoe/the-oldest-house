package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/**
 * What the House does with its mornings once somebody has been inside.
 *
 * <ol>
 *   <li><b>The rugs:</b> the first completed night somebody actually spends
 *   in a manor bed, pairs of authored rugs trade colours between rooms.</li>
 *   <li><b>The room between rooms:</b> from House morning
 *   {@link HouseConfig#ROOM_FIRST_AGE} (and at least
 *   {@link HouseConfig#ROOM_MIN_MORNINGS_AFTER_RUGS} after the rugs), each
 *   morning has a chance of arming the room behind the study door: 50%, then
 *   75%, then certain, by default. Armed, it starts routing the first time
 *   nobody is in or looking at the doorway.</li>
 *   <li><b>Subtle changes</b> ({@link HouseShifts}): from the rugs on, on any
 *   morning when nothing larger happens, a rising chance of one small change
 *   nobody saw happen.</li>
 *   <li><b>The hallway:</b> once someone has gone through the room
 *   (in one side, out the other) and
 *   {@link HouseConfig#SHIFTS_BEFORE_HALLWAY} subtle changes have happened,
 *   the door at the end of the hall gets a heavily weighted daily chance
 *   ({@link HouseConfig#HALLWAY_BASE_CHANCE}, rising by
 *   {@link HouseConfig#HALLWAY_CHANCE_STEP}) of opening onto the impossible
 *   hallway, where it always has. It is rolled before the subtle changes.</li>
 * </ol>
 *
 * At most one of these happens on any morning.
 *
 * The first night slept in the manor is a milestone (it brings the rugs);
 * after that the clock is the House's mornings, wherever anyone slept, so
 * sleeping at home can never stall it.
 *
 * A morning is a new day of {@link HouseCalendar} on which a player wakes
 * after sleeping, in the Overworld or in the manor. The House's perceived
 * age advances by one on each (see {@link HouseSavedData#advanceHouseAgeForMorning}).
 */
public final class HouseProgression {
    private HouseProgression() {
    }

    /**
     * A player woke from a night's sleep. {@code inManor}: in one of the
     * manor's own beds.
     *
     * @return what changed, for anyone watching (commands, logs)
     */
    public static List<String> onMorningWake(MinecraftServer server, long day, boolean inManor) {
        HouseSavedData data = HouseSavedData.get(server);
        if (inManor && data.visitCount() > 0) {
            data.markSleptInManor();
        }
        if (data.advanceHouseAgeForMorning(day)) {
            return morning(server, data);
        }
        // Someone else's wake-up already made this morning; a later sleeper
        // in the manor can still be the first night spent there.
        List<String> changes = new ArrayList<>();
        if (inManor && shiftRugsIfDue(server, data)) {
            changes.add("the rugs changed colour");
        }
        return changes;
    }

    /** Everything that happens on a new morning of the House's perceived age: at most one change. */
    public static List<String> morning(MinecraftServer server, HouseSavedData data) {
        List<String> changes = new ArrayList<>();
        if (shiftRugsIfDue(server, data)) {
            changes.add("the rugs changed colour");
            return changes;
        }

        int roomChance = roomChance(data, data.houseAge());
        if (roomChance >= 0) {
            if (server.overworld().getRandom().nextInt(100) < roomChance && HouseBetweenRoom.arm(server, data)) {
                changes.add("the room between rooms is ready behind the study door; it opens once nobody is looking ("
                        + roomChance + "% chance)");
                return changes;
            }
            data.noteRoomMissedMorning();
            changes.add("the study door stayed an ordinary door (" + roomChance + "% chance)");
        }

        int hallwayChance = hallwayChance(data);
        if (hallwayChance >= 0) {
            if (server.overworld().getRandom().nextInt(100) < hallwayChance) {
                HouseStageManager.revealHallway(server, data);
                if (data.isImpossibleDoorRevealed()) {
                    changes.add("the door at the end of the hall opened onto the impossible hallway (" + hallwayChance + "% chance)");
                    return changes;
                }
            }
            data.noteHallwayMissedMorning();
            changes.add("the end of the hall stayed a wall (" + hallwayChance + "% chance)");
        }

        String shift = HouseShifts.morning(server, data);
        if (shift != null) {
            changes.add(shift);
        }
        return changes;
    }

    // ------------------------------------------------------------------
    // The rugs

    public static boolean isRugShiftDue(HouseSavedData data) {
        return data.isSpawned()
                && !data.areRugsShifted()
                && data.visitCount() > 0
                && data.sleptInManor();
    }

    private static boolean shiftRugsIfDue(MinecraftServer server, HouseSavedData data) {
        return isRugShiftDue(data) && shiftRugs(server, data);
    }

    /** Shifts the rugs now, due or not. */
    public static boolean shiftRugs(MinecraftServer server, HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = origin == null ? null : HouseInteriorInitializer.ensureInitialized(server, data);
        if (interior == null) {
            return false;
        }
        int changed = HouseRugs.shift(interior, origin);
        HouseDimensionMirror.reconcileAuthoritativeDomestic(interior, server.overworld(), origin);
        data.markRugsShifted();
        TheOldestHouse.LOGGER.info("The rugs of The Oldest House changed colour ({} carpet blocks) at perceived age {}.",
                changed, data.houseAge());
        return true;
    }

    // ------------------------------------------------------------------
    // The room between rooms

    /**
     * The chance, in percent, that the room appears on a morning when the
     * House's perceived age is {@code age}; -1 when it cannot appear then.
     */
    public static int roomChance(HouseSavedData data, int age) {
        if (!data.isSpawned() || data.isRoomArmed() || !data.areRugsShifted()) {
            return -1;
        }
        if (age < HouseConfig.ROOM_FIRST_AGE.getAsInt()
                || age - data.rugsShiftedAge() < HouseConfig.ROOM_MIN_MORNINGS_AFTER_RUGS.getAsInt()) {
            return -1;
        }
        long chance = HouseConfig.ROOM_BASE_CHANCE.getAsInt()
                + (long) HouseConfig.ROOM_CHANCE_STEP.getAsInt() * data.roomMissedMornings();
        return (int) Math.min(100L, chance);
    }

    // ------------------------------------------------------------------
    // The hallway

    /**
     * The chance, in percent, that the hallway opens next morning; -1 while
     * it cannot (nobody has gone through the room yet, or too few subtle
     * changes).
     */
    public static int hallwayChance(HouseSavedData data) {
        if (!data.isSpawned() || data.isImpossibleDoorRevealed() || !data.isRoomTraversed()
                || data.shiftsTriggered() < HouseConfig.SHIFTS_BEFORE_HALLWAY.getAsInt()) {
            return -1;
        }
        long chance = HouseConfig.HALLWAY_BASE_CHANCE.getAsInt()
                + (long) HouseConfig.HALLWAY_CHANCE_STEP.getAsInt() * data.hallwayMissedMornings();
        return (int) Math.min(100L, chance);
    }

    // ------------------------------------------------------------------
    // Status

    /** One line per stage, as {@code /oldesthouse status} shows them. */
    public static List<String> describe(HouseSavedData data) {
        List<String> lines = new ArrayList<>();
        int age = data.houseAge();
        int nextAge = age + 1;

        if (data.areRugsShifted()) {
            lines.add("Rugs: shifted at age " + data.rugsShiftedAge() + ".");
        } else if (data.visitCount() <= 0) {
            lines.add("Rugs: waiting for someone to enter the manor.");
        } else if (data.sleptInManor()) {
            lines.add("Rugs: shift on the next morning (someone has slept in the manor).");
        } else {
            lines.add("Rugs: waiting for the first completed night in a manor bed.");
        }

        if (data.isRoomRevealed()) {
            lines.add("Room between rooms: the study door has led through it since age " + data.roomRevealedAge() + "; "
                    + (data.isRoomTraversed() ? "someone has gone through it." : "nobody has gone through it yet."));
        } else if (data.isRoomArmed()) {
            lines.add("Room between rooms: armed at age " + data.roomArmedAge()
                    + "; the study door starts leading through it once nobody is in or looking at the doorway.");
        } else if (!data.areRugsShifted()) {
            lines.add("Room between rooms: 0% next morning (waiting for the rugs).");
        } else {
            int chance = roomChance(data, nextAge);
            if (chance >= 0) {
                lines.add("Room between rooms: " + chance + "% chance next morning"
                        + (data.roomMissedMornings() > 0 ? " (missed " + data.roomMissedMornings() + ")." : "."));
            } else {
                int first = Math.max(HouseConfig.ROOM_FIRST_AGE.getAsInt(),
                        data.rugsShiftedAge() + HouseConfig.ROOM_MIN_MORNINGS_AFTER_RUGS.getAsInt());
                lines.add("Room between rooms: 0% next morning; chances start at House morning " + first + " at "
                        + HouseConfig.ROOM_BASE_CHANCE.getAsInt() + "%.");
            }
        }

        lines.add(HouseShifts.describe(data));

        int needed = HouseConfig.SHIFTS_BEFORE_HALLWAY.getAsInt();
        if (data.isImpossibleDoorRevealed()) {
            lines.add("Impossible hallway: open.");
        } else if (!data.isRoomTraversed() || data.shiftsTriggered() < needed) {
            lines.add("Impossible hallway: 0% next morning; it waits for someone to go through the room between rooms ("
                    + (data.isRoomTraversed() ? "done" : "not yet") + ") and " + needed + " subtle changes ("
                    + data.shiftsTriggered() + " so far).");
        } else {
            lines.add("Impossible hallway: " + hallwayChance(data) + "% chance next morning"
                    + (data.hallwayMissedMornings() > 0 ? " (stayed shut " + data.hallwayMissedMornings() + " time(s))." : "."));
        }
        return lines;
    }

    /** Forces one stage now. Returns an error, or null on success. */
    @Nullable
    public static String reveal(MinecraftServer server, String what) {
        HouseSavedData data = HouseSavedData.get(server);
        if (!data.isSpawned()) {
            return "The Oldest House has not spawned yet.";
        }
        switch (what) {
            case "rugs" -> {
                if (data.areRugsShifted()) {
                    return "The rugs have already shifted.";
                }
                return shiftRugs(server, data) ? null : "The House interior is not available yet.";
            }
            case "room" -> {
                if (data.isRoomRevealed()) {
                    return "The room between rooms is already there.";
                }
                if (!data.areRugsShifted()) {
                    shiftRugs(server, data);
                }
                // A test command: no waiting for nobody to look.
                return HouseBetweenRoom.arm(server, data) && HouseBetweenRoom.activate(server, data)
                        ? null : "The House interior is not available.";
            }
            case "hallway" -> {
                if (data.isImpossibleDoorRevealed()) {
                    return "The impossible hallway is already open.";
                }
                HouseStageManager.revealHallway(server, data);
                HouseShifts.refreshCache(data);
                return data.isImpossibleDoorRevealed() ? null : "The House interior is not available yet.";
            }
            default -> {
                return "Unknown stage " + what + ".";
            }
        }
    }
}
