package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

/**
 * Every door that leads somewhere else, where it leads, and how each player
 * gets back.
 *
 * A door is an ordinary wooden door the mod keeps shut and intercepts. Its
 * destination is one of:
 * <ul>
 *   <li>{@code place:<id>}: always that place (see {@link LabyrinthPlace});</li>
 *   <li>{@code dealt}: wherever the dealer last dealt it ({@code Door.dealt});</li>
 *   <li>{@code return}: back through the last door the player came through;</li>
 *   <li>{@code hallway_or_return}: the impossible hallway if it exists, else as return;</li>
 *   <li>{@code locked}: nowhere; it never opens.</li>
 * </ul>
 * Rearranging the house means changing these, never moving blocks.
 */
public final class LabyrinthData extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_labyrinth";
    private static final int MAX_RETURNS = 16;

    public static final String DEALT = "dealt";
    public static final String RETURN = "return";
    public static final String HALLWAY_OR_RETURN = "hallway_or_return";
    /** A door that never opens (the hotel's room doors). */
    public static final String LOCKED = "locked";

    public static String toPlace(LabyrinthPlace place) {
        return "place:" + place.id();
    }

    public static final class Door {
        public final String id;
        public final ResourceKey<Level> dimension;
        public final BlockPos lower;
        /** The way the door faces: into the room it is used from. */
        public final Direction facing;
        public String destination;
        @Nullable
        public String dealt;
        public boolean leak;
        /** Its leak is Hillary barking from behind it: the door she found. */
        public boolean bark;
        /** Placed by a command rather than part of the labyrinth. */
        public final boolean command;

        public Door(String id, ResourceKey<Level> dimension, BlockPos lower, Direction facing, String destination, boolean command) {
            this.id = id;
            this.dimension = dimension;
            this.lower = lower.immutable();
            this.facing = facing;
            this.destination = destination;
            this.command = command;
        }

        public GlobalPos globalPos() {
            return GlobalPos.of(dimension, lower);
        }

        /** Standing in front of this door, facing away from it. */
        public Waypoint inFront() {
            return new Waypoint(dimension, Vec3.atBottomCenterOf(lower.relative(facing)), facing.toYRot());
        }
    }

    /**
     * Somewhere to go back to. A {@code door} waypoint is the door the player
     * came through ({@code pos} its lower half, {@code yaw} the way it
     * faces); crossing back out shifts them to in front of it. Any other is
     * a plain spot and facing.
     */
    public record Waypoint(ResourceKey<Level> dimension, Vec3 pos, float yaw, boolean door) {
        public Waypoint(ResourceKey<Level> dimension, Vec3 pos, float yaw) {
            this(dimension, pos, yaw, false);
        }
    }

    private final Map<String, Door> doors = new LinkedHashMap<>();
    private final Map<GlobalPos, String> index = new HashMap<>();
    private final Map<UUID, Deque<Waypoint>> returns = new HashMap<>();
    private final Set<String> completed = new LinkedHashSet<>();
    private final Set<String> ready = new LinkedHashSet<>();
    private final Map<String, CompoundTag> states = new HashMap<>();
    private int dryDeals;
    private boolean hillaryScent;
    private int builtVersion;
    @Nullable
    private BlockPos builtOrigin;
    private int nextCommandId = 1;

    public static final Factory<LabyrinthData> FACTORY = new Factory<>(LabyrinthData::new, LabyrinthData::load);

    public LabyrinthData() {
    }

    public static LabyrinthData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    // ------------------------------------------------------------------
    // Doors

    public Collection<Door> doors() {
        return List.copyOf(doors.values());
    }

    @Nullable
    public Door door(String id) {
        return doors.get(id);
    }

    /** The door with a half at {@code pos}, if any. */
    @Nullable
    public Door doorAt(ResourceKey<Level> dimension, BlockPos pos) {
        String id = index.get(GlobalPos.of(dimension, pos));
        if (id == null) {
            id = index.get(GlobalPos.of(dimension, pos.below()));
        }
        return id == null ? null : doors.get(id);
    }

    /** Adds or replaces a door, keeping what it was last dealt. */
    public void putDoor(Door door) {
        Door old = doors.get(door.id);
        if (old != null) {
            index.remove(old.globalPos());
            if (door.dealt == null) {
                door.dealt = old.dealt;
                door.leak = old.leak;
                door.bark = old.bark;
            }
        }
        doors.put(door.id, door);
        index.put(door.globalPos(), door.id);
        setDirty();
    }

    public void removeDoor(String id) {
        Door old = doors.remove(id);
        if (old != null) {
            index.remove(old.globalPos());
            setDirty();
        }
    }

    public String nextCommandDoorId() {
        setDirty();
        return "command/" + nextCommandId++;
    }

    public void deal(Door door, String place, boolean leak) {
        deal(door, place, leak, false);
    }

    public void deal(Door door, String place, boolean leak, boolean bark) {
        door.dealt = place;
        door.leak = leak;
        door.bark = bark;
        setDirty();
    }

    // ------------------------------------------------------------------
    // The way back

    public void pushReturn(UUID player, Waypoint waypoint) {
        Deque<Waypoint> stack = returns.computeIfAbsent(player, id -> new ArrayDeque<>());
        stack.push(waypoint);
        while (stack.size() > MAX_RETURNS) {
            stack.removeLast();
        }
        setDirty();
    }

    @Nullable
    public Waypoint popReturn(UUID player) {
        Deque<Waypoint> stack = returns.get(player);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        setDirty();
        return stack.pop();
    }

    public void clearReturns(UUID player) {
        if (returns.remove(player) != null) {
            setDirty();
        }
    }

    public int returnDepth(UUID player) {
        Deque<Waypoint> stack = returns.get(player);
        return stack == null ? 0 : stack.size();
    }

    // ------------------------------------------------------------------
    // The dealer and the vignettes

    public int dryDeals() {
        return dryDeals;
    }

    public void setDryDeals(int dryDeals) {
        this.dryDeals = dryDeals;
        setDirty();
    }

    public boolean isCompleted(String vignette) {
        return completed.contains(vignette);
    }

    public void setCompleted(String vignette, boolean done) {
        if (done ? completed.add(vignette) : completed.remove(vignette)) {
            setDirty();
        }
    }

    public Set<String> completed() {
        return Set.copyOf(completed);
    }

    /** Whether a place that has to be made first (see {@link LabyrinthPlace#needsMaking()}) has been. */
    public boolean isReady(String place) {
        return ready.contains(place);
    }

    public void setReady(String place, boolean isReady) {
        if (isReady ? ready.add(place) : ready.remove(place)) {
            setDirty();
        }
    }

    /** A vignette's own saved state (where things were left, what was found); empty if none. */
    public CompoundTag state(String vignette) {
        CompoundTag tag = states.get(vignette);
        return tag == null ? new CompoundTag() : tag.copy();
    }

    public void setState(String vignette, CompoundTag tag) {
        if (tag.isEmpty()) {
            states.remove(vignette);
        } else {
            states.put(vignette, tag.copy());
        }
        setDirty();
    }

    /** Hillary has taken a scent: the next dealing that can have a vignette door will, and she'll be heard behind it. */
    public boolean hillaryScent() {
        return hillaryScent;
    }

    public void setHillaryScent(boolean scent) {
        if (hillaryScent != scent) {
            hillaryScent = scent;
            setDirty();
        }
    }

    public int builtVersion() {
        return builtVersion;
    }

    /** The manor the places were carved for, or null. */
    @Nullable
    public BlockPos builtOrigin() {
        return builtOrigin;
    }

    public void setBuilt(int version, BlockPos origin) {
        builtVersion = version;
        builtOrigin = origin.immutable();
        setDirty();
    }

    /** Forgets doors in dimensions that no longer exist (an earlier version's labyrinth). */
    public void pruneDoors(MinecraftServer server) {
        List<String> gone = new ArrayList<>();
        for (Door door : doors.values()) {
            if (server.getLevel(door.dimension) == null) {
                gone.add(door.id);
            }
        }
        gone.forEach(this::removeDoor);
    }

    // ------------------------------------------------------------------
    // Saving

    private static ResourceKey<Level> dimension(String id) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(id));
    }

    private static LabyrinthData load(CompoundTag tag, HolderLookup.Provider registries) {
        LabyrinthData data = new LabyrinthData();
        ListTag doorList = tag.getList("Doors", Tag.TAG_COMPOUND);
        for (int i = 0; i < doorList.size(); i++) {
            CompoundTag d = doorList.getCompound(i);
            Door door = new Door(
                    d.getString("Id"),
                    dimension(d.getString("Dim")),
                    BlockPos.of(d.getLong("Pos")),
                    Direction.from3DDataValue(d.getInt("Facing")),
                    d.getString("Dest"),
                    d.getBoolean("Command"));
            door.dealt = d.contains("Dealt") ? d.getString("Dealt") : null;
            door.leak = d.getBoolean("Leak");
            door.bark = d.getBoolean("Bark");
            data.doors.put(door.id, door);
            data.index.put(door.globalPos(), door.id);
        }
        ListTag returnList = tag.getList("Returns", Tag.TAG_COMPOUND);
        for (int i = 0; i < returnList.size(); i++) {
            CompoundTag r = returnList.getCompound(i);
            Deque<Waypoint> stack = new ArrayDeque<>();
            ListTag points = r.getList("Stack", Tag.TAG_COMPOUND);
            for (int j = 0; j < points.size(); j++) {
                CompoundTag p = points.getCompound(j);
                stack.addLast(new Waypoint(dimension(p.getString("Dim")),
                        new Vec3(p.getDouble("X"), p.getDouble("Y"), p.getDouble("Z")), p.getFloat("Yaw"), p.getBoolean("Door")));
            }
            data.returns.put(r.getUUID("Player"), stack);
        }
        ListTag done = tag.getList("Completed", Tag.TAG_STRING);
        for (int i = 0; i < done.size(); i++) {
            data.completed.add(done.getString(i));
        }
        ListTag readyList = tag.getList("Ready", Tag.TAG_STRING);
        for (int i = 0; i < readyList.size(); i++) {
            data.ready.add(readyList.getString(i));
        }
        CompoundTag stateTag = tag.getCompound("States");
        for (String key : stateTag.getAllKeys()) {
            data.states.put(key, stateTag.getCompound(key));
        }
        data.dryDeals = tag.getInt("DryDeals");
        data.hillaryScent = tag.getBoolean("HillaryScent");
        data.builtVersion = tag.getInt("BuiltVersion");
        data.builtOrigin = tag.contains("BuiltOrigin") ? BlockPos.of(tag.getLong("BuiltOrigin")) : null;
        data.nextCommandId = Math.max(1, tag.getInt("NextCommandId"));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag doorList = new ListTag();
        for (Door door : doors.values()) {
            CompoundTag d = new CompoundTag();
            d.putString("Id", door.id);
            d.putString("Dim", door.dimension.location().toString());
            d.putLong("Pos", door.lower.asLong());
            d.putInt("Facing", door.facing.get3DDataValue());
            d.putString("Dest", door.destination);
            d.putBoolean("Command", door.command);
            if (door.dealt != null) {
                d.putString("Dealt", door.dealt);
            }
            d.putBoolean("Leak", door.leak);
            d.putBoolean("Bark", door.bark);
            doorList.add(d);
        }
        tag.put("Doors", doorList);

        ListTag returnList = new ListTag();
        for (Map.Entry<UUID, Deque<Waypoint>> entry : returns.entrySet()) {
            CompoundTag r = new CompoundTag();
            r.putUUID("Player", entry.getKey());
            ListTag points = new ListTag();
            for (Waypoint point : new ArrayList<>(entry.getValue())) {
                CompoundTag p = new CompoundTag();
                p.putString("Dim", point.dimension().location().toString());
                p.putDouble("X", point.pos().x);
                p.putDouble("Y", point.pos().y);
                p.putDouble("Z", point.pos().z);
                p.putFloat("Yaw", point.yaw());
                p.putBoolean("Door", point.door());
                points.add(p);
            }
            r.put("Stack", points);
            returnList.add(r);
        }
        tag.put("Returns", returnList);

        ListTag done = new ListTag();
        for (String vignette : completed) {
            done.add(StringTag.valueOf(vignette));
        }
        tag.put("Completed", done);
        ListTag readyList = new ListTag();
        for (String place : ready) {
            readyList.add(StringTag.valueOf(place));
        }
        tag.put("Ready", readyList);
        CompoundTag stateTag = new CompoundTag();
        states.forEach((key, value) -> stateTag.put(key, value.copy()));
        tag.put("States", stateTag);
        tag.putInt("DryDeals", dryDeals);
        tag.putBoolean("HillaryScent", hillaryScent);
        tag.putInt("BuiltVersion", builtVersion);
        if (builtOrigin != null) {
            tag.putLong("BuiltOrigin", builtOrigin.asLong());
        }
        tag.putInt("NextCommandId", nextCommandId);
        return tag;
    }
}
