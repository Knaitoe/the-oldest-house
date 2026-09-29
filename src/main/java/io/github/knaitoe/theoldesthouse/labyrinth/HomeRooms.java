package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Where each player spends their time at home, and the room of theirs the
 * Red Room is copied from.
 *
 * Time is tallied in cells four blocks on a side: every few seconds a player
 * spends indoors near their bed adds to the cell they stand in. The cell
 * with the most time is "their room"; while they stand in it, it is copied
 * (see {@link RedRoom#capture}), again each day, so the copy follows the room
 * as it changes. Old habits fade: when a cell grows very large, every cell
 * is halved.
 */
public final class HomeRooms extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_home_rooms";
    private static final int MAX_CELLS = 32;
    private static final int HALVE_AT = 36_000;

    public static final Factory<HomeRooms> FACTORY = new Factory<>(HomeRooms::new, HomeRooms::load);

    /** Time spent in one cell, and where in it the player last stood. */
    public static final class Cell {
        int seconds;
        BlockPos last;

        Cell(int seconds, BlockPos last) {
            this.seconds = seconds;
            this.last = last.immutable();
        }

        public int seconds() {
            return seconds;
        }

        public BlockPos last() {
            return last;
        }
    }

    private static final class Rooms {
        final Map<Long, Cell> cells = new HashMap<>();
        @Nullable
        RoomSnapshot snapshot;
    }

    /** A snapshot and whose room it is. */
    public record Choice(UUID owner, RoomSnapshot snapshot) {
    }

    private final Map<UUID, Rooms> players = new HashMap<>();
    @Nullable
    private UUID placedOwner;
    private long placedStamp = Long.MIN_VALUE;

    public HomeRooms() {
    }

    public static HomeRooms get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    /** The cell a position falls in. */
    public static long cellOf(BlockPos pos) {
        return BlockPos.asLong(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);
    }

    // ------------------------------------------------------------------
    // The tally

    /** Adds time spent at {@code pos}. */
    public void record(UUID player, BlockPos pos, int seconds) {
        Rooms rooms = players.computeIfAbsent(player, id -> new Rooms());
        long key = cellOf(pos);
        Cell cell = rooms.cells.get(key);
        if (cell == null) {
            if (rooms.cells.size() >= MAX_CELLS) {
                long smallest = 0L;
                int least = Integer.MAX_VALUE;
                for (Map.Entry<Long, Cell> entry : rooms.cells.entrySet()) {
                    if (entry.getValue().seconds < least) {
                        least = entry.getValue().seconds;
                        smallest = entry.getKey();
                    }
                }
                rooms.cells.remove(smallest);
            }
            cell = new Cell(0, pos);
            rooms.cells.put(key, cell);
        }
        cell.seconds += seconds;
        cell.last = pos.immutable();
        if (cell.seconds >= HALVE_AT) {
            rooms.cells.values().forEach(c -> c.seconds /= 2);
            rooms.cells.values().removeIf(c -> c.seconds <= 0);
        }
        setDirty();
    }

    /** The cell the player has spent the most time in, or null. */
    @Nullable
    public Map.Entry<Long, Cell> topCell(UUID player) {
        Rooms rooms = players.get(player);
        if (rooms == null) {
            return null;
        }
        Map.Entry<Long, Cell> best = null;
        for (Map.Entry<Long, Cell> entry : rooms.cells.entrySet()) {
            if (best == null || entry.getValue().seconds > best.getValue().seconds) {
                best = entry;
            }
        }
        return best;
    }

    public int cellCount(UUID player) {
        Rooms rooms = players.get(player);
        return rooms == null ? 0 : rooms.cells.size();
    }

    // ------------------------------------------------------------------
    // Snapshots

    @Nullable
    public RoomSnapshot snapshot(UUID player) {
        Rooms rooms = players.get(player);
        return rooms == null ? null : rooms.snapshot;
    }

    public void setSnapshot(UUID player, RoomSnapshot snapshot) {
        players.computeIfAbsent(player, id -> new Rooms()).snapshot = snapshot;
        setDirty();
    }

    /** Whether the room should be copied now: never copied, a different room, or not copied today. */
    public boolean needsCapture(UUID player, long cell, long today) {
        RoomSnapshot snapshot = snapshot(player);
        return snapshot == null || snapshot.cell() != cell || snapshot.day() < today;
    }

    /** The player's own room, or else the most recently copied room of anyone's. */
    @Nullable
    public Choice choiceFor(UUID player) {
        RoomSnapshot own = snapshot(player);
        return own != null ? new Choice(player, own) : latest();
    }

    /** The most recently copied room of anyone's. */
    @Nullable
    public Choice latest() {
        Choice latest = null;
        for (Map.Entry<UUID, Rooms> entry : players.entrySet()) {
            RoomSnapshot snapshot = entry.getValue().snapshot;
            if (snapshot != null && (latest == null || snapshot.stamp() > latest.snapshot().stamp())) {
                latest = new Choice(entry.getKey(), snapshot);
            }
        }
        return latest;
    }

    public boolean isPlaced(Choice choice) {
        return choice.owner().equals(placedOwner) && choice.snapshot().stamp() == placedStamp;
    }

    public void setPlaced(@Nullable Choice choice) {
        placedOwner = choice == null ? null : choice.owner();
        placedStamp = choice == null ? Long.MIN_VALUE : choice.snapshot().stamp();
        setDirty();
    }

    /** The snapshot standing in the Red Room now, if it is still known. */
    @Nullable
    public Choice placed() {
        if (placedOwner == null) {
            return null;
        }
        RoomSnapshot snapshot = snapshot(placedOwner);
        return snapshot != null && snapshot.stamp() == placedStamp ? new Choice(placedOwner, snapshot) : null;
    }

    @Nullable
    public UUID placedOwner() {
        return placedOwner;
    }

    public void forget(UUID player) {
        if (players.remove(player) != null) {
            setDirty();
        }
    }

    public boolean hasAnySnapshot() {
        for (Rooms rooms : players.values()) {
            if (rooms.snapshot != null) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Saving

    private static HomeRooms load(CompoundTag tag, HolderLookup.Provider registries) {
        HomeRooms data = new HomeRooms();
        ListTag list = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag p = list.getCompound(i);
            Rooms rooms = new Rooms();
            ListTag cells = p.getList("Cells", Tag.TAG_COMPOUND);
            for (int j = 0; j < cells.size(); j++) {
                CompoundTag c = cells.getCompound(j);
                rooms.cells.put(c.getLong("Key"), new Cell(c.getInt("Seconds"), BlockPos.of(c.getLong("Last"))));
            }
            if (p.contains("Snapshot", Tag.TAG_COMPOUND)) {
                rooms.snapshot = RoomSnapshot.load(p.getCompound("Snapshot"), registries.lookupOrThrow(Registries.BLOCK));
            }
            data.players.put(p.getUUID("Player"), rooms);
        }
        if (tag.hasUUID("PlacedOwner")) {
            data.placedOwner = tag.getUUID("PlacedOwner");
            data.placedStamp = tag.getLong("PlacedStamp");
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Rooms> entry : players.entrySet()) {
            CompoundTag p = new CompoundTag();
            p.putUUID("Player", entry.getKey());
            ListTag cells = new ListTag();
            for (Map.Entry<Long, Cell> cell : entry.getValue().cells.entrySet()) {
                CompoundTag c = new CompoundTag();
                c.putLong("Key", cell.getKey());
                c.putInt("Seconds", cell.getValue().seconds);
                c.putLong("Last", cell.getValue().last.asLong());
                cells.add(c);
            }
            p.put("Cells", cells);
            if (entry.getValue().snapshot != null) {
                p.put("Snapshot", entry.getValue().snapshot.save());
            }
            list.add(p);
        }
        tag.put("Players", list);
        if (placedOwner != null) {
            tag.putUUID("PlacedOwner", placedOwner);
            tag.putLong("PlacedStamp", placedStamp);
        }
        return tag;
    }

    /** For status: each player's tally, most time first. */
    public List<String> describe(UUID player) {
        List<String> lines = new ArrayList<>();
        Map.Entry<Long, Cell> top = topCell(player);
        if (top == null) {
            lines.add("No time counted at home yet (it counts indoors, within 40 blocks of your bed).");
        } else {
            BlockPos at = top.getValue().last;
            lines.add("Most time at home: " + top.getValue().seconds + " s around " + at.getX() + " " + at.getY() + " " + at.getZ()
                    + " (" + cellCount(player) + " spot(s) counted).");
        }
        return lines;
    }
}
