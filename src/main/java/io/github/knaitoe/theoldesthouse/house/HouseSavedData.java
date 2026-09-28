package io.github.knaitoe.theoldesthouse.house;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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

    // After the first visit: the rugs, then the room between rooms.
    private boolean sleptInManor;
    private int rugsShiftedAge = -1;
    private int roomRevealedAge = -1;
    private int roomMissedMornings;
    private int roomDoorX = -1;
    private boolean roomPocketBuilt;

    // Subtle changes (see HouseShifts) and the hallway's roll.
    private int shiftsTriggered;
    private int shiftDryMornings;
    private int hallwayMissedMornings;
    private boolean hallDeepened;
    private int notesWritten;
    private boolean echoPending;
    @Nullable
    private BlockPos windowLight;
    private final List<String> shiftHistory = new ArrayList<>();

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
        data.sleptInManor = tag.getBoolean("SleptInManor");
        data.rugsShiftedAge = tag.contains("RugsShiftedAge") ? tag.getInt("RugsShiftedAge") : -1;
        data.roomRevealedAge = tag.contains("RoomRevealedAge") ? tag.getInt("RoomRevealedAge") : -1;
        data.roomMissedMornings = tag.getInt("RoomMissedMornings");
        data.roomDoorX = tag.contains("RoomDoorX") ? tag.getInt("RoomDoorX") : -1;
        data.roomPocketBuilt = tag.getBoolean("RoomPocketBuilt");
        data.shiftsTriggered = tag.getInt("ShiftsTriggered");
        data.shiftDryMornings = tag.getInt("ShiftDryMornings");
        data.hallwayMissedMornings = tag.getInt("HallwayMissedMornings");
        data.hallDeepened = tag.getBoolean("HallDeepened");
        data.notesWritten = tag.getInt("NotesWritten");
        data.echoPending = tag.getBoolean("EchoPending");
        data.windowLight = tag.contains("WindowLight") ? BlockPos.of(tag.getLong("WindowLight")) : null;
        ListTag history = tag.getList("ShiftHistory", Tag.TAG_STRING);
        for (int i = 0; i < history.size(); i++) {
            data.shiftHistory.add(history.getString(i));
        }
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
        tag.putBoolean("SleptInManor", sleptInManor);
        tag.putInt("RugsShiftedAge", rugsShiftedAge);
        tag.putInt("RoomRevealedAge", roomRevealedAge);
        tag.putInt("RoomMissedMornings", roomMissedMornings);
        tag.putInt("RoomDoorX", roomDoorX);
        tag.putBoolean("RoomPocketBuilt", roomPocketBuilt);
        tag.putInt("ShiftsTriggered", shiftsTriggered);
        tag.putInt("ShiftDryMornings", shiftDryMornings);
        tag.putInt("HallwayMissedMornings", hallwayMissedMornings);
        tag.putBoolean("HallDeepened", hallDeepened);
        tag.putInt("NotesWritten", notesWritten);
        tag.putBoolean("EchoPending", echoPending);
        if (windowLight != null) {
            tag.putLong("WindowLight", windowLight.asLong());
        }
        ListTag history = new ListTag();
        for (String entry : shiftHistory) {
            history.add(StringTag.valueOf(entry));
        }
        tag.put("ShiftHistory", history);
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
     * Advances perceived age once per morning (a day of {@link HouseCalendar}), but only after at
     * least one real manor entry. Ignoring the invitation can therefore never
     * reveal the impossible threshold off-screen.
     */
    public boolean advanceHouseAgeForMorning(long currentDay) {
        if (lastHouseAgeDay > currentDay) {
            // Stored by an earlier version from a world clock since wound back.
            lastHouseAgeDay = currentDay - 1L;
        }
        if (!spawned || visitCount <= 0 || currentDay == lastHouseAgeDay) {
            return false;
        }

        lastHouseAgeDay = currentDay;
        houseAge++;
        setDirty();
        return true;
    }

    public boolean sleptInManor() {
        return sleptInManor;
    }

    public void markSleptInManor() {
        if (!sleptInManor) {
            sleptInManor = true;
            setDirty();
        }
    }

    public boolean areRugsShifted() {
        return rugsShiftedAge >= 0;
    }

    /** Perceived age when the rugs changed colour, or -1. */
    public int rugsShiftedAge() {
        return rugsShiftedAge;
    }

    public void markRugsShifted() {
        rugsShiftedAge = houseAge;
        setDirty();
    }

    public boolean isRoomRevealed() {
        return roomRevealedAge >= 0;
    }

    /** Perceived age when the room between rooms appeared, or -1. */
    public int roomRevealedAge() {
        return roomRevealedAge;
    }

    /** Eligible mornings on which the room did not appear. */
    public int roomMissedMornings() {
        return roomMissedMornings;
    }

    public void noteRoomMissedMorning() {
        roomMissedMornings++;
        setDirty();
    }

    /** House-relative x of the room's door in the bedroom partition, or -1. */
    public int roomDoorX() {
        return roomDoorX;
    }

    /** Whether the room's pocket above the manor has been built (older rooms were elsewhere). */
    public boolean isRoomPocketBuilt() {
        return roomPocketBuilt;
    }

    public void markRoomPocketBuilt() {
        roomPocketBuilt = true;
        setDirty();
    }

    public void setRoomDoorX(int doorX) {
        roomDoorX = doorX;
        setDirty();
    }

    public void markRoomRevealed(int doorX) {
        roomRevealedAge = houseAge;
        roomDoorX = doorX;
        setDirty();
    }

    /** Subtle changes that have happened so far (see HouseShifts). */
    public int shiftsTriggered() {
        return shiftsTriggered;
    }

    /** Eligible mornings since the last subtle change. */
    public int shiftDryMornings() {
        return shiftDryMornings;
    }

    public void noteShiftDryMorning() {
        shiftDryMornings++;
        setDirty();
    }

    /** Records a subtle change by name, oldest first. */
    public void recordShift(String name) {
        shiftsTriggered++;
        shiftDryMornings = 0;
        shiftHistory.add(name + "@" + houseAge);
        while (shiftHistory.size() > 32) {
            shiftHistory.remove(0);
        }
        setDirty();
    }

    /** Recent subtle changes as "name@age", oldest first. */
    public List<String> shiftHistory() {
        return List.copyOf(shiftHistory);
    }

    public boolean hasShifted(String name) {
        for (String entry : shiftHistory) {
            if (entry.startsWith(name + "@")) {
                return true;
            }
        }
        return false;
    }

    /** The most recent subtle change's name, or null. */
    @Nullable
    public String lastShift() {
        if (shiftHistory.isEmpty()) {
            return null;
        }
        String last = shiftHistory.get(shiftHistory.size() - 1);
        int at = last.indexOf('@');
        return at < 0 ? last : last.substring(0, at);
    }

    /** Eligible mornings on which the hallway did not open. */
    public int hallwayMissedMornings() {
        return hallwayMissedMornings;
    }

    public void noteHallwayMissedMorning() {
        hallwayMissedMornings++;
        setDirty();
    }

    /** Whether the end of the hall has been moved back a block (until the hallway opens). */
    public boolean isHallDeepened() {
        return hallDeepened;
    }

    public void markHallDeepened() {
        hallDeepened = true;
        setDirty();
    }

    public int notesWritten() {
        return notesWritten;
    }

    /** Bit {@code index} set: that note of the Navidsons' has been left on a shelf. */
    public boolean isNoteWritten(int index) {
        return (notesWritten & (1 << index)) != 0;
    }

    public void markNoteWritten(int index) {
        notesWritten |= 1 << index;
        setDirty();
    }

    public boolean isEchoPending() {
        return echoPending;
    }

    public void setEchoPending(boolean pending) {
        echoPending = pending;
        setDirty();
    }

    /** The Overworld-only light behind one of the manor's upper windows, or null. */
    @Nullable
    public BlockPos windowLight() {
        return windowLight;
    }

    public void setWindowLight(@Nullable BlockPos pos) {
        windowLight = pos == null ? null : pos.immutable();
        setDirty();
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
        sleptInManor = false;
        rugsShiftedAge = -1;
        roomRevealedAge = -1;
        roomMissedMornings = 0;
        roomDoorX = -1;
        roomPocketBuilt = false;
        shiftsTriggered = 0;
        shiftDryMornings = 0;
        hallwayMissedMornings = 0;
        hallDeepened = false;
        notesWritten = 0;
        echoPending = false;
        windowLight = null;
        shiftHistory.clear();
        refreshOriginCache();
        setDirty();
    }
}
