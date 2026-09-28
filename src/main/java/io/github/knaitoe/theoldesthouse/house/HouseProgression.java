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
 *   <li><b>The rugs:</b> the morning after someone first sleeps in a manor
 *   bed (or by {@link HouseConfig#RUGS_FALLBACK_AGE} regardless), pairs of
 *   rugs trade colours between rooms.</li>
 *   <li><b>The room between rooms:</b> from
 *   {@link HouseConfig#ROOM_MIN_MORNINGS_AFTER_RUGS} mornings after that,
 *   each morning has a chance of a door appearing in the bedroom partition.
 *   The chance starts at {@link HouseConfig#ROOM_BASE_CHANCE} and rises by
 *   {@link HouseConfig#ROOM_CHANCE_STEP} for every morning it does not.</li>
 *   <li><b>The hallway:</b> {@link HouseConfig#HALLWAY_MORNINGS_AFTER_ROOM}
 *   mornings after the room, the door at the end of the hall opens onto the
 *   impossible hallway, where it always has.</li>
 * </ol>
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

    /** Everything that happens on a new morning of the House's perceived age. */
    public static List<String> morning(MinecraftServer server, HouseSavedData data) {
        List<String> changes = new ArrayList<>();
        if (shiftRugsIfDue(server, data)) {
            changes.add("the rugs changed colour");
        }

        int chance = roomChance(data, data.houseAge());
        if (chance >= 0) {
            int roll = server.overworld().getRandom().nextInt(100);
            if (roll < chance && HouseBetweenRoom.reveal(server, data)) {
                changes.add("a door appeared in the principal bedroom (" + chance + "% chance)");
            } else {
                data.noteRoomMissedMorning();
                changes.add("no door this morning (" + chance + "% chance)");
            }
        }

        boolean hallwayBefore = data.isImpossibleDoorRevealed();
        HouseStageManager.applyCurrentStage(server, data);
        if (!hallwayBefore && data.isImpossibleDoorRevealed()) {
            changes.add("the door at the end of the hall opened onto the impossible hallway");
        }
        return changes;
    }

    // ------------------------------------------------------------------
    // The rugs

    public static boolean isRugShiftDue(HouseSavedData data) {
        return data.isSpawned()
                && !data.areRugsShifted()
                && data.visitCount() > 0
                && (data.sleptInManor() || data.houseAge() >= HouseConfig.RUGS_FALLBACK_AGE.getAsInt());
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
        if (!data.isSpawned() || data.isRoomRevealed() || !data.areRugsShifted()) {
            return -1;
        }
        if (age - data.rugsShiftedAge() < HouseConfig.ROOM_MIN_MORNINGS_AFTER_RUGS.getAsInt()) {
            return -1;
        }
        long chance = HouseConfig.ROOM_BASE_CHANCE.getAsInt()
                + (long) HouseConfig.ROOM_CHANCE_STEP.getAsInt() * data.roomMissedMornings();
        return (int) Math.min(100L, chance);
    }

    // ------------------------------------------------------------------
    // The hallway

    public static boolean isHallwayDue(HouseSavedData data) {
        return data.isRoomRevealed()
                && data.houseAge() >= data.roomRevealedAge() + HouseConfig.HALLWAY_MORNINGS_AFTER_ROOM.getAsInt();
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
            int fallback = HouseConfig.RUGS_FALLBACK_AGE.getAsInt();
            lines.add("Rugs: shift the morning after someone sleeps in a manor bed, or at age " + fallback
                    + (nextAge >= fallback ? " (next morning)." : " (" + (fallback - age) + " mornings from now)."));
        }

        if (data.isRoomRevealed()) {
            lines.add("Room between rooms: appeared at age " + data.roomRevealedAge()
                    + ", door at x+" + data.roomDoorX() + " in the principal bedroom's south wall.");
        } else if (!data.areRugsShifted()) {
            lines.add("Room between rooms: 0% next morning (waiting for the rugs).");
        } else {
            int chance = roomChance(data, nextAge);
            if (chance >= 0) {
                lines.add("Room between rooms: " + chance + "% chance next morning"
                        + (data.roomMissedMornings() > 0 ? " (missed " + data.roomMissedMornings() + ")." : "."));
            } else {
                int wait = data.rugsShiftedAge() + HouseConfig.ROOM_MIN_MORNINGS_AFTER_RUGS.getAsInt() - age;
                lines.add("Room between rooms: 0% next morning; chances start in " + wait + " morning(s) at "
                        + HouseConfig.ROOM_BASE_CHANCE.getAsInt() + "%.");
            }
        }

        if (data.isImpossibleDoorRevealed()) {
            lines.add("Impossible hallway: open.");
        } else if (!data.isRoomRevealed()) {
            lines.add("Impossible hallway: waiting for the room between rooms.");
        } else {
            int due = data.roomRevealedAge() + HouseConfig.HALLWAY_MORNINGS_AFTER_ROOM.getAsInt();
            lines.add("Impossible hallway: opens at age " + due
                    + (nextAge >= due ? " (next morning)." : " (" + (due - age) + " mornings from now)."));
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
                return HouseBetweenRoom.reveal(server, data) ? null : "The House interior or the between dimension is not available.";
            }
            case "hallway" -> {
                if (data.isImpossibleDoorRevealed()) {
                    return "The impossible hallway is already open.";
                }
                HouseStageManager.revealHallway(server, data);
                return data.isImpossibleDoorRevealed() ? null : "The House interior is not available yet.";
            }
            default -> {
                return "Unknown stage " + what + ".";
            }
        }
    }
}
