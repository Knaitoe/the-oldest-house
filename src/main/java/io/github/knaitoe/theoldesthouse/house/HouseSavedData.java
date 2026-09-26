package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Optional;

public final class HouseSavedData extends SavedData {
    private static final String DATA_NAME = "the_oldest_house";

    public static final int MIN_SPAWN_CHANCE_PERCENT = 5;
    public static final int MAX_SPAWN_CHANCE_PERCENT = 100;

    private boolean eligible;
    private long eligibleSinceDay = -1L;

    private boolean spawned;
    private boolean hasHousePosition;
    private int houseX;
    private int houseY;
    private int houseZ;

    private boolean hasAnchorPosition;
    private int anchorX;
    private int anchorY;
    private int anchorZ;

    private int settlementNights;
    private long lastCountedSettlementDay = -1L;
    private long lastSpawnRollDay = -1L;
    private int spawnChancePercent = MIN_SPAWN_CHANCE_PERCENT;

    private int houseAge;
    private int visitCount;

    public static final Factory<HouseSavedData> FACTORY =
            new Factory<>(HouseSavedData::new, HouseSavedData::load);

    public HouseSavedData() {
    }

    public static HouseSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        HouseSavedData data = new HouseSavedData();

        data.eligible = tag.getBoolean("Eligible");
        data.eligibleSinceDay = tag.getLong("EligibleSinceDay");

        data.spawned = tag.getBoolean("Spawned");
        data.hasHousePosition = tag.getBoolean("HasHousePosition");
        data.houseX = tag.getInt("HouseX");
        data.houseY = tag.getInt("HouseY");
        data.houseZ = tag.getInt("HouseZ");

        data.hasAnchorPosition = tag.getBoolean("HasAnchorPosition");
        data.anchorX = tag.getInt("AnchorX");
        data.anchorY = tag.getInt("AnchorY");
        data.anchorZ = tag.getInt("AnchorZ");

        data.settlementNights = tag.getInt("SettlementNights");
        data.lastCountedSettlementDay = tag.contains("LastCountedSettlementDay")
                ? tag.getLong("LastCountedSettlementDay")
                : -1L;
        data.lastSpawnRollDay = tag.contains("LastSpawnRollDay")
                ? tag.getLong("LastSpawnRollDay")
                : -1L;
        data.spawnChancePercent = tag.contains("SpawnChancePercent")
                ? clampSpawnChance(tag.getInt("SpawnChancePercent"))
                : MIN_SPAWN_CHANCE_PERCENT;

        data.houseAge = tag.getInt("HouseAge");
        data.visitCount = tag.getInt("VisitCount");

        return data;
    }

    public static HouseSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("Eligible", eligible);
        tag.putLong("EligibleSinceDay", eligibleSinceDay);

        tag.putBoolean("Spawned", spawned);
        tag.putBoolean("HasHousePosition", hasHousePosition);
        tag.putInt("HouseX", houseX);
        tag.putInt("HouseY", houseY);
        tag.putInt("HouseZ", houseZ);

        tag.putBoolean("HasAnchorPosition", hasAnchorPosition);
        tag.putInt("AnchorX", anchorX);
        tag.putInt("AnchorY", anchorY);
        tag.putInt("AnchorZ", anchorZ);

        tag.putInt("SettlementNights", settlementNights);
        tag.putLong("LastCountedSettlementDay", lastCountedSettlementDay);
        tag.putLong("LastSpawnRollDay", lastSpawnRollDay);
        tag.putInt("SpawnChancePercent", spawnChancePercent);

        tag.putInt("HouseAge", houseAge);
        tag.putInt("VisitCount", visitCount);
        return tag;
    }

    public boolean isEligible() {
        return eligible;
    }

    public long eligibleSinceDay() {
        return eligibleSinceDay;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public int settlementNights() {
        return settlementNights;
    }

    public int spawnChancePercent() {
        return spawnChancePercent;
    }

    public int houseAge() {
        return houseAge;
    }

    public int visitCount() {
        return visitCount;
    }

    public Optional<BlockPos> housePosition() {
        return hasHousePosition
                ? Optional.of(new BlockPos(houseX, houseY, houseZ))
                : Optional.empty();
    }

    public Optional<BlockPos> anchorPosition() {
        return hasAnchorPosition
                ? Optional.of(new BlockPos(anchorX, anchorY, anchorZ))
                : Optional.empty();
    }

    /**
     * Records one successful night at a candidate settlement.
     *
     * The first sleep establishes the candidate anchor. Sleeping outside the
     * configured settlement radius starts a new candidate settlement rather
     * than allowing unrelated beds across the world to accumulate progress.
     */
    public int recordSettlementNight(BlockPos sleepPosition, long currentDay, int settlementRadius, int requiredNights) {
        if (currentDay == lastCountedSettlementDay) {
            return settlementNights;
        }

        if (!hasAnchorPosition || !isWithinHorizontalRadius(sleepPosition, settlementRadius)) {
            hasAnchorPosition = true;
            anchorX = sleepPosition.getX();
            anchorY = sleepPosition.getY();
            anchorZ = sleepPosition.getZ();
            settlementNights = 1;
        } else {
            settlementNights++;
        }

        lastCountedSettlementDay = currentDay;

        if (!eligible && settlementNights >= requiredNights) {
            eligible = true;
            eligibleSinceDay = currentDay;
            spawnChancePercent = MIN_SPAWN_CHANCE_PERCENT;
        }

        setDirty();
        return settlementNights;
    }

    private boolean isWithinHorizontalRadius(BlockPos position, int radius) {
        long dx = (long) position.getX() - anchorX;
        long dz = (long) position.getZ() - anchorZ;
        long radiusSquared = (long) radius * radius;
        return dx * dx + dz * dz <= radiusSquared;
    }

    public boolean claimSpawnRoll(long currentDay) {
        if (currentDay == lastSpawnRollDay) {
            return false;
        }

        lastSpawnRollDay = currentDay;
        setDirty();
        return true;
    }

    /**
     * Chooses a direction with equal probability. Upward nights gain 2-5
     * percentage points; downward nights lose 1-2 points. The asymmetry gives
     * the system a gentle long-term upward drift without becoming a countdown.
     */
    public int adjustSpawnChance(RandomSource random) {
        if (random.nextBoolean()) {
            spawnChancePercent += 2 + random.nextInt(4);
        } else {
            spawnChancePercent -= 1 + random.nextInt(2);
        }

        spawnChancePercent = clampSpawnChance(spawnChancePercent);
        setDirty();
        return spawnChancePercent;
    }

    private static int clampSpawnChance(int value) {
        return Math.max(MIN_SPAWN_CHANCE_PERCENT, Math.min(MAX_SPAWN_CHANCE_PERCENT, value));
    }

    public void markEligible(BlockPos anchor, long currentDay) {
        eligible = true;
        eligibleSinceDay = currentDay;
        hasAnchorPosition = true;
        anchorX = anchor.getX();
        anchorY = anchor.getY();
        anchorZ = anchor.getZ();
        settlementNights = Math.max(settlementNights, HouseLifecycleEvents.REQUIRED_SETTLEMENT_NIGHTS);
        spawnChancePercent = MIN_SPAWN_CHANCE_PERCENT;
        setDirty();
    }

    public void markIneligible() {
        eligible = false;
        eligibleSinceDay = -1L;
        spawnChancePercent = MIN_SPAWN_CHANCE_PERCENT;
        setDirty();
    }

    public void markSpawned(BlockPos origin) {
        spawned = true;
        hasHousePosition = true;
        houseX = origin.getX();
        houseY = origin.getY();
        houseZ = origin.getZ();
        setDirty();
    }

    public void setHouseAge(int days) {
        houseAge = Math.max(0, days);
        setDirty();
    }

    public void incrementVisitCount() {
        visitCount++;
        setDirty();
    }

    public void reset() {
        eligible = false;
        eligibleSinceDay = -1L;

        spawned = false;
        hasHousePosition = false;
        houseX = 0;
        houseY = 0;
        houseZ = 0;

        hasAnchorPosition = false;
        anchorX = 0;
        anchorY = 0;
        anchorZ = 0;

        settlementNights = 0;
        lastCountedSettlementDay = -1L;
        lastSpawnRollDay = -1L;
        spawnChancePercent = MIN_SPAWN_CHANCE_PERCENT;

        houseAge = 0;
        visitCount = 0;
        setDirty();
    }
}
