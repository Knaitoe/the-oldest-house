package io.github.knaitoe.theoldesthouse.opening;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

/**
 * One player's progress through the opening sequence, stored as a data
 * attachment on the player and copied across deaths.
 *
 * Mutable: the attachment holder keeps this instance, so changes made
 * through the setters persist without re-attaching.
 */
public final class OpeningPlayerState {
    /** Bounded so a player who opens hundreds of doors cannot grow their save without limit. */
    private static final int MAX_TRACKED_DOORS = 48;

    public record DoorUse(BlockPos pos, int count) {
        public static final Codec<DoorUse> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(DoorUse::pos),
                Codec.INT.fieldOf("count").forGetter(DoorUse::count)
        ).apply(instance, DoorUse::new));
    }

    /**
     * The home bed the reader last woke in on a morning (HomeBed): where, the House's day it became home, how many nights they have
     * slept in it, and the last of those nights.
     */
    public record Home(BlockPos pos, long since, int nights, long lastNight) {
        public static final Codec<Home> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Home::pos),
                Codec.LONG.optionalFieldOf("since", -1L).forGetter(Home::since),
                Codec.INT.optionalFieldOf("nights", 0).forGetter(Home::nights),
                Codec.LONG.optionalFieldOf("last_night", -1L).forGetter(Home::lastNight)
        ).apply(instance, Home::new));
    }

    public static final Codec<OpeningPlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OpeningStage.CODEC.optionalFieldOf("stage", OpeningStage.NONE).forGetter(OpeningPlayerState::stage),
            Codec.INT.optionalFieldOf("nights_slept", 0).forGetter(OpeningPlayerState::nightsSlept),
            Codec.LONG.optionalFieldOf("last_sleep_day", -1L).forGetter(OpeningPlayerState::lastSleepDay),
            DoorUse.CODEC.listOf().optionalFieldOf("door_use", List.of()).forGetter(OpeningPlayerState::doorUse),
            Codec.LONG.optionalFieldOf("first_join_day", -1L).forGetter(OpeningPlayerState::firstJoinDay),
            Codec.LONG.optionalFieldOf("eligible_day", -1L).forGetter(OpeningPlayerState::eligibleDay),
            Codec.LONG.optionalFieldOf("letter_day", -1L).forGetter(OpeningPlayerState::letterDay),
            Codec.LONG.optionalFieldOf("last_morning_day", -1L).forGetter(OpeningPlayerState::lastMorningDay),
            UUIDUtil.CODEC.optionalFieldOf("hillary").forGetter(state -> Optional.ofNullable(state.hillaryUuid)),
            Codec.BOOL.optionalFieldOf("entered_house", false).forGetter(OpeningPlayerState::enteredHouse),
            Codec.LONG.optionalFieldOf("hillary_day", -1L).forGetter(OpeningPlayerState::hillaryDay),
            Codec.LONG.optionalFieldOf("entered_day", -1L).forGetter(OpeningPlayerState::enteredDay),
            Codec.INT.optionalFieldOf("entered_time", -1).forGetter(OpeningPlayerState::enteredTime),
            Home.CODEC.optionalFieldOf("home").forGetter(state -> Optional.ofNullable(state.home))
    ).apply(instance, OpeningPlayerState::new));

    private OpeningStage stage = OpeningStage.NONE;
    private int nightsSlept;
    private long lastSleepDay = -1L;
    private final List<DoorUse> doorUse = new ArrayList<>();
    private long firstJoinDay = -1L;
    private long eligibleDay = -1L;
    private long letterDay = -1L;
    private long lastMorningDay = -1L;
    @Nullable
    private UUID hillaryUuid;
    private boolean enteredHouse;
    private long hillaryDay = -1L;
    /** 0.4.671: the House's own day (see HouseCalendar) and the hour of that day when it first took this reader in; -1 if unknown. */
    private long enteredDay = -1L;
    private int enteredTime = -1;
    @Nullable
    private Home home;

    public OpeningPlayerState() {
    }

    private OpeningPlayerState(
            OpeningStage stage,
            int nightsSlept,
            long lastSleepDay,
            List<DoorUse> doorUse,
            long firstJoinDay,
            long eligibleDay,
            long letterDay,
            long lastMorningDay,
            Optional<UUID> hillaryUuid,
            boolean enteredHouse,
            long hillaryDay,
            long enteredDay,
            int enteredTime,
            Optional<Home> home
    ) {
        this.stage = stage;
        this.nightsSlept = nightsSlept;
        this.lastSleepDay = lastSleepDay;
        this.doorUse.addAll(doorUse);
        this.firstJoinDay = firstJoinDay;
        this.eligibleDay = eligibleDay;
        this.letterDay = letterDay;
        this.lastMorningDay = lastMorningDay;
        this.hillaryUuid = hillaryUuid.orElse(null);
        this.enteredHouse = enteredHouse;
        this.hillaryDay = hillaryDay;
        this.enteredDay = enteredDay;
        this.enteredTime = enteredTime;
        this.home = home.orElse(null);
    }

    public OpeningStage stage() {
        return stage;
    }

    public int nightsSlept() {
        return nightsSlept;
    }

    public long lastSleepDay() {
        return lastSleepDay;
    }

    public List<DoorUse> doorUse() {
        return List.copyOf(doorUse);
    }

    public long firstJoinDay() {
        return firstJoinDay;
    }

    public long eligibleDay() {
        return eligibleDay;
    }

    public long letterDay() {
        return letterDay;
    }

    public long lastMorningDay() {
        return lastMorningDay;
    }

    @Nullable
    public UUID hillaryUuid() {
        return hillaryUuid;
    }

    public boolean enteredHouse() {
        return enteredHouse;
    }

    public long hillaryDay() { return hillaryDay; }
    public long enteredDay() { return enteredDay; }
    public int enteredTime() { return enteredTime; }

    /** The home bed the reader last woke in on a morning, or null before any. */
    @Nullable
    public Home home() {
        return home;
    }

    /**
     * A morning wake in a home bed. The bed already remembered counts one more night (once a day); any other becomes home today,
     * with this its first night.
     */
    public void wokeAtHome(BlockPos bed, long day) {
        if (home != null && home.pos().equals(bed)) {
            if (home.lastNight() != day) {
                home = new Home(home.pos(), home.since(), home.nights() + 1, day);
            }
            return;
        }
        home = new Home(bed.immutable(), day, 1, day);
    }

    /** The remembered home bed is gone: the House forgets it. */
    public void forgetHome() {
        home = null;
    }

    public boolean houseDue(long today) {
        return stage.isAtLeast(OpeningStage.HILLARY_ARRIVED)
                && today > (hillaryDay >= 0L ? hillaryDay : letterDay + 1L);
    }

    /** Counts one completed sleep per in-game day. */
    public boolean recordSleep(long day) {
        if (day == lastSleepDay) {
            return false;
        }
        lastSleepDay = day;
        nightsSlept++;
        return true;
    }

    /**
     * Earlier versions counted days from the world clock, which
     * {@code /time set} can wind back. Any stored day later than today is
     * brought back to today so no step waits on a day that already passed.
     */
    public void clampToToday(long day) {
        firstJoinDay = Math.min(firstJoinDay, day);
        eligibleDay = Math.min(eligibleDay, day);
        letterDay = Math.min(letterDay, day);
        lastSleepDay = Math.min(lastSleepDay, day);
        lastMorningDay = Math.min(lastMorningDay, day);
        hillaryDay = Math.min(hillaryDay, day);
    }

    public void noteFirstJoin(long day) {
        if (firstJoinDay < 0L) {
            firstJoinDay = day;
        }
    }

    public void markMorningHandled(long day) {
        lastMorningDay = day;
    }

    public void markEligible(long day) {
        stage = OpeningStage.ELIGIBLE;
        eligibleDay = day;
    }

    public void markLetterDelivered(long day) {
        stage = OpeningStage.LETTER_DELIVERED;
        letterDay = day;
    }

    public void markHillaryArrived() {
        stage = enteredHouse ? OpeningStage.ENTERED : OpeningStage.HILLARY_ARRIVED;
    }

    public void markHillaryArrived(long day) {
        hillaryDay = day;
        markHillaryArrived();
    }

    /** 0.4.671: the first entry also records the House's day and the hour of it; later entries leave the record as it was. */
    public void markEntered(long day, int time) {
        if (enteredDay < 0) {
            enteredDay = day;
            enteredTime = time;
        }
        markEntered();
    }

    public void markEntered() {
        enteredHouse = true;
        if (stage.isAtLeast(OpeningStage.HILLARY_ARRIVED)) {
            stage = OpeningStage.ENTERED;
        }
    }

    public void setHillary(@Nullable UUID uuid) {
        hillaryUuid = uuid;
    }

    /**
     * Counts one use of the door whose lower half is at {@code door}. Only
     * doors within {@code radius} of the reader's home are tracked, and
     * entries that no longer qualify are dropped.
     */
    public void recordDoorUse(BlockPos door, BlockPos home, int radius) {
        long radiusSquared = (long) radius * radius;
        doorUse.removeIf(use -> use.pos().distSqr(home) > radiusSquared);
        if (door.distSqr(home) > radiusSquared) {
            return;
        }

        for (int i = 0; i < doorUse.size(); i++) {
            DoorUse use = doorUse.get(i);
            if (use.pos().equals(door)) {
                doorUse.set(i, new DoorUse(use.pos(), use.count() + 1));
                return;
            }
        }

        if (doorUse.size() >= MAX_TRACKED_DOORS) {
            // Forget the least-used door to make room.
            DoorUse least = doorUse.get(0);
            for (DoorUse use : doorUse) {
                if (use.count() < least.count()) {
                    least = use;
                }
            }
            doorUse.remove(least);
        }
        doorUse.add(new DoorUse(door.immutable(), 1));
    }

    public void forgetDoor(BlockPos door) {
        doorUse.removeIf(use -> use.pos().equals(door));
    }

    /** Back to the start; Hillary, if present, is left in the world. */
    public void reset() {
        stage = OpeningStage.NONE;
        nightsSlept = 0;
        lastSleepDay = -1L;
        doorUse.clear();
        eligibleDay = -1L;
        letterDay = -1L;
        lastMorningDay = -1L;
        hillaryUuid = null;
        enteredHouse = false;
        hillaryDay = -1L;
        enteredDay = -1L;
        enteredTime = -1;
        home = null;
    }

    public void setStageForTesting(OpeningStage newStage, long day) {
        stage = newStage;
        if (newStage == OpeningStage.ELIGIBLE) {
            eligibleDay = day;
        } else if (newStage == OpeningStage.LETTER_DELIVERED) {
            letterDay = day;
        }
    }
}
