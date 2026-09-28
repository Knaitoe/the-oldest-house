package io.github.knaitoe.theoldesthouse.house;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persistent state of the House itself.
 *
 * Appearance eligibility/chance fields from the pre-0.3 opening are no longer
 * read or written. Old saves may retain those unused NBT keys harmlessly.
 */
public final class HouseSavedData extends SavedData {
    private static final String DATA_NAME = "the_oldest_house";

    private boolean spawned;
    private boolean hasHousePosition;
    private int houseX;
    private int houseY;
    private int houseZ;

    private int houseAge;
    private long lastHouseAgeDay = -1L;
    private boolean impossibleDoorRevealed;
    private boolean interiorInitialized;
    private int visitCount;
    private int layoutVersion;

    // Derived, not saved: hot paths read the origin every tick.
    @Nullable
    private BlockPos cachedHouseOrigin;

    public static final Factory<HouseSavedData> FACTORY =
            new Factory<>(HouseSavedData::new, HouseSavedData::load);

    public HouseSavedData() {
    }

    public static HouseSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        HouseSavedData data = new HouseSavedData();

        data.spawned = tag.getBoolean("Spawned");
        data.hasHousePosition = tag.getBoolean("HasHousePosition");
        data.houseX = tag.getInt("HouseX");
        data.houseY = tag.getInt("HouseY");
        data.houseZ = tag.getInt("HouseZ");

        data.houseAge = tag.getInt("HouseAge");
        data.lastHouseAgeDay = tag.contains("LastHouseAgeDay")
                ? tag.getLong("LastHouseAgeDay")
                : -1L;
        data.impossibleDoorRevealed = tag.getBoolean("ImpossibleDoorRevealed");
        data.interiorInitialized = tag.getBoolean("InteriorInitialized");
        data.visitCount = tag.getInt("VisitCount");
        data.layoutVersion = tag.contains("LayoutVersion") ? tag.getInt("LayoutVersion") : 1;
        data.refreshOriginCache();

        return data;
    }

    public static HouseSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("Spawned", spawned);
        tag.putBoolean("HasHousePosition", hasHousePosition);
        tag.putInt("HouseX", houseX);
        tag.putInt("HouseY", houseY);
        tag.putInt("HouseZ", houseZ);

        tag.putInt("HouseAge", houseAge);
        tag.putLong("LastHouseAgeDay", lastHouseAgeDay);
        tag.putBoolean("ImpossibleDoorRevealed", impossibleDoorRevealed);
        tag.putBoolean("InteriorInitialized", interiorInitialized);
        tag.putInt("VisitCount", visitCount);
        tag.putInt("LayoutVersion", layoutVersion);
        return tag;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public int houseAge() {
        return houseAge;
    }

    public boolean isImpossibleDoorRevealed() {
        return impossibleDoorRevealed;
    }

    public boolean isInteriorInitialized() {
        return interiorInitialized;
    }

    public int visitCount() {
        return visitCount;
    }

    public Optional<BlockPos> housePosition() {
        return Optional.ofNullable(cachedHouseOrigin);
    }

    /** The House origin without allocation, or null before it has spawned. */
    @Nullable
    public BlockPos houseOrigin() {
        return cachedHouseOrigin;
    }

    /** Architecture version the spawned House was generated with. */
    public int layoutVersion() {
        return layoutVersion;
    }

    public boolean isCurrentLayout() {
        return layoutVersion == HouseLayout.LAYOUT_VERSION;
    }

    private void refreshOriginCache() {
        cachedHouseOrigin = hasHousePosition ? new BlockPos(houseX, houseY, houseZ) : null;
    }

    public void markSpawned(BlockPos origin) {
        spawned = true;
        hasHousePosition = true;
        houseX = origin.getX();
        houseY = origin.getY();
        houseZ = origin.getZ();
        layoutVersion = HouseLayout.LAYOUT_VERSION;
        refreshOriginCache();
        setDirty();
    }

    public void setHouseAge(int days) {
        houseAge = Math.max(0, days);
        setDirty();
    }

    public int advanceHouseAge(int days) {
        if (days <= 0) {
            return houseAge;
        }

        houseAge += days;
        setDirty();
        return houseAge;
    }

    /**
     * Advances perceived age once per Minecraft morning, but only after at
     * least one real manor entry. Ignoring the invitation can therefore never
     * reveal the impossible threshold off-screen.
     */
    public boolean advanceHouseAgeForMorning(long currentDay) {
        if (!spawned || visitCount <= 0 || currentDay == lastHouseAgeDay) {
            return false;
        }

        lastHouseAgeDay = currentDay;
        houseAge++;
        setDirty();
        return true;
    }

    public void markImpossibleDoorRevealed() {
        impossibleDoorRevealed = true;
        setDirty();
    }

    public void markInteriorInitialized() {
        interiorInitialized = true;
        setDirty();
    }

    public void incrementVisitCount() {
        visitCount++;
        setDirty();
    }

    public void reset() {
        spawned = false;
        hasHousePosition = false;
        houseX = 0;
        houseY = 0;
        houseZ = 0;

        houseAge = 0;
        lastHouseAgeDay = -1L;
        impossibleDoorRevealed = false;
        interiorInitialized = false;
        visitCount = 0;
        layoutVersion = 0;
        refreshOriginCache();
        setDirty();
    }
}
