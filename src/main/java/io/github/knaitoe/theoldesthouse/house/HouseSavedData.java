package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Optional;

public final class HouseSavedData extends SavedData {
    private static final String DATA_NAME = "the_oldest_house";

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

    public void markEligible(BlockPos anchor, long currentDay) {
        eligible = true;
        eligibleSinceDay = currentDay;
        hasAnchorPosition = true;
        anchorX = anchor.getX();
        anchorY = anchor.getY();
        anchorZ = anchor.getZ();
        setDirty();
    }

    public void markIneligible() {
        eligible = false;
        eligibleSinceDay = -1L;
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

        houseAge = 0;
        visitCount = 0;
        setDirty();
    }
}
