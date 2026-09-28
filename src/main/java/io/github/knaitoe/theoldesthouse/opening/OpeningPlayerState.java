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

    public static final Codec<OpeningPlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OpeningStage.CODEC.optionalFieldOf("stage", OpeningStage.NONE).forGetter(OpeningPlayerState::stage),
            Codec.INT.optionalFieldOf("nights_slept", 0).forGetter(OpeningPlayerState::nightsSlept),
            Codec.LONG.optionalFieldOf("last_sleep_day", -1L).forGetter(OpeningPlayerState::lastSleepDay),
            DoorUse.CODEC.listOf().optionalFieldOf("door_use", List.of()).forGetter(OpeningPlayerState::doorUse),
            Codec.LONG.optionalFieldOf("first_join_day", -1L).forGetter(OpeningPlayerState::firstJoinDay),
            Codec.LONG.optionalFieldOf("eligible_day", -1L).forGetter(OpeningPlayerState::eligibleDay),
            Codec.LONG.optionalFieldOf("letter_day", -1L).forGetter(OpeningPlayerState::letterDay),
            Codec.LONG.optionalFieldOf("last_morning_day", -1L).forGetter(OpeningPlayerState::lastMorningDay),
            Codec.INT.optionalFieldOf("failed_door_nights", 0).forGetter(OpeningPlayerState::failedDoorNights),
            BlockPos.CODEC.optionalFieldOf("entrance_door").forGetter(state -> Optional.ofNullable(state.entranceDoorPos)),
            UUIDUtil.CODEC.optionalFieldOf("hillary").forGetter(state -> Optional.ofNullable(state.hillaryUuid))
    ).apply(instance, OpeningPlayerState::new));

    private OpeningStage stage = OpeningStage.NONE;
    private int nightsSlept;
    private long lastSleepDay = -1L;
    private final List<DoorUse> doorUse = new ArrayList<>();
    private long firstJoinDay = -1L;
    private long eligibleDay = -1L;
    private long letterDay = -1L;
    private long lastMorningDay = -1L;
    private int failedDoorNights;
    @Nullable
    private BlockPos entranceDoorPos;
    @Nullable
    private UUID hillaryUuid;

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
            int failedDoorNights,
            Optional<BlockPos> entranceDoorPos,
            Optional<UUID> hillaryUuid
    ) {
        this.stage = stage;
        this.nightsSlept = nightsSlept;
        this.lastSleepDay = lastSleepDay;
        this.doorUse.addAll(doorUse);
        this.firstJoinDay = firstJoinDay;
        this.eligibleDay = eligibleDay;
        this.letterDay = letterDay;
        this.lastMorningDay = lastMorningDay;
        this.failedDoorNights = failedDoorNights;
        this.entranceDoorPos = entranceDoorPos.orElse(null);
        this.hillaryUuid = hillaryUuid.orElse(null);
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

    public int failedDoorNights() {
        return failedDoorNights;
    }

    @Nullable
    public BlockPos entranceDoorPos() {
        return entranceDoorPos;
    }

    @Nullable
    public UUID hillaryUuid() {
        return hillaryUuid;
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

    public void markDoorPlaced(BlockPos lower) {
        stage = OpeningStage.DOOR_PLACED;
        entranceDoorPos = lower.immutable();
        failedDoorNights = 0;
    }

    public void markEntered() {
        stage = OpeningStage.ENTERED;
    }

    public int recordFailedDoorNight() {
        return ++failedDoorNights;
    }

    public void setHillary(@Nullable UUID uuid) {
        hillaryUuid = uuid;
    }

    /**
     * Counts one use of the door whose lower half is at {@code door}. Only
     * doors within {@code radius} of the respawn position are tracked, and
     * entries that no longer qualify are dropped.
     */
    public void recordDoorUse(BlockPos door, BlockPos respawn, int radius) {
        long radiusSquared = (long) radius * radius;
        doorUse.removeIf(use -> use.pos().distSqr(respawn) > radiusSquared);
        if (door.distSqr(respawn) > radiusSquared) {
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

    /** Back to the start; the door and Hillary, if any, are left in the world. */
    public void reset() {
        stage = OpeningStage.NONE;
        nightsSlept = 0;
        lastSleepDay = -1L;
        doorUse.clear();
        eligibleDay = -1L;
        letterDay = -1L;
        lastMorningDay = -1L;
        failedDoorNights = 0;
        entranceDoorPos = null;
        hillaryUuid = null;
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
