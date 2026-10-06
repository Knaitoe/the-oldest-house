package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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
    /** The way back is remembered this many doors deep; the great staircase waits at twenty. */
    private static final int MAX_RETURNS = 32;

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

    /** One player's current answer for a dealt physical door. */
    public record Deal(String place, boolean leak, boolean bark) {
    }

    /**
     * The physical labyrinth is shared, but its changing graph is not. Each
     * player keeps their own current deals, dry spell, Hillary scent and
     * remembered discoveries.
     */
    private static final class PlayerDealer {
        final Map<String, Deal> deals = new LinkedHashMap<>();
        final Set<String> visited = new LinkedHashSet<>();
        final Deque<String> recent = new ArrayDeque<>();
        int dryDeals;
        boolean hillaryScent;
        /** The doors of each place this player has stood in, keyed by the way they came: their map of the labyrinth. */
        final LinkedHashMap<Long, Map<String, Deal>> nodes = new LinkedHashMap<>(16, 0.75F, true);
    }

    /** Places remembered per player before the oldest is forgotten. */
    public static final int MAX_NODES = 1024;

    private final Map<String, Door> doors = new LinkedHashMap<>();
    private final Map<GlobalPos, String> index = new HashMap<>();
    private final Map<UUID, Deque<Waypoint>> returns = new HashMap<>();
    private final Map<UUID, PlayerDealer> playerDealers = new HashMap<>();
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
            for (PlayerDealer dealer : playerDealers.values()) {
                dealer.deals.remove(id);
            }
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

    private PlayerDealer playerDealer(UUID player) {
        return playerDealers.computeIfAbsent(player, id -> {
            PlayerDealer state = new PlayerDealer();
            // Existing worlds had one shared streak/scent. Let each player
            // inherit it once, then diverge from there.
            state.dryDeals = dryDeals;
            state.hillaryScent = hillaryScent;
            return state;
        });
    }

    /** This player's current destination and leak for a dealt physical door. */
    @Nullable
    public Deal deal(UUID player, Door door) {
        PlayerDealer state = playerDealers.get(player);
        Deal personal = state == null ? null : state.deals.get(door.id);
        if (personal != null) {
            return personal;
        }
        // Migration from saves made before deals became per-player.
        return door.dealt == null ? null : new Deal(door.dealt, door.leak, door.bark);
    }

    public void deal(UUID player, Door door, String place, boolean leak) {
        deal(player, door, place, leak, false);
    }

    public void deal(UUID player, Door door, String place, boolean leak, boolean bark) {
        VignetteGate.redealt(this,player,door);
        playerDealer(player).deals.put(door.id, new Deal(place, leak, bark));
        setDirty();
    }

    /**
     * Identifies a place as reached by this route: the doors on the player's
     * way back, in order, and the place itself. The same doors taken from the
     * same hallway always name the same spot, whoever takes them.
     */
    public long nodeKey(UUID player, LabyrinthPlace place) {
        long hash = 0xcbf29ce484222325L;
        Deque<Waypoint> stack = returns.get(player);
        if (stack != null) {
            var it = stack.descendingIterator();
            while (it.hasNext()) {
                Waypoint point = it.next();
                hash = mix(hash, point.dimension().location().toString());
                hash = mix(hash, Long.toString(BlockPos.containing(point.pos()).asLong()));
            }
        }
        return mix(hash, place.id());
    }

    private static long mix(long hash, String text) {
        for (int i = 0; i < text.length(); i++) {
            hash ^= text.charAt(i);
            hash *= 0x100000001b3L;
        }
        hash ^= '|';
        return hash * 0x100000001b3L;
    }

    /** What this player found behind each door of a remembered place, or null for somewhere new. */
    @Nullable
    public Map<String, Deal> node(UUID player, long key) {
        PlayerDealer state = playerDealers.get(player);
        return state == null ? null : state.nodes.get(key);
    }

    public void rememberNode(UUID player, long key, Map<String, Deal> deals) {
        PlayerDealer state = playerDealer(player);
        state.nodes.put(key, Map.copyOf(deals));
        while (state.nodes.size() > MAX_NODES) {
            var oldest = state.nodes.keySet().iterator();
            oldest.next();
            oldest.remove();
        }
        setDirty();
    }

    public int dryDeals(UUID player) {
        PlayerDealer state = playerDealers.get(player);
        return state == null ? dryDeals : state.dryDeals;
    }

    public void setDryDeals(UUID player, int value) {
        playerDealer(player).dryDeals = value;
        setDirty();
    }

    public boolean hillaryScent(UUID player) {
        PlayerDealer state = playerDealers.get(player);
        return state == null ? hillaryScent : state.hillaryScent;
    }

    public void setHillaryScent(UUID player, boolean scent) {
        PlayerDealer state = playerDealer(player);
        if (state.hillaryScent != scent) {
            state.hillaryScent = scent;
            setDirty();
        }
    }

    /** Remembers that this player has personally reached this place. */
    public void visit(UUID player, LabyrinthPlace place) {
        PlayerDealer state = playerDealer(player);
        state.visited.add(place.id());
        state.recent.addFirst(place.id());
        while (state.recent.size() > 8) state.recent.removeLast();
        setDirty();
    }

    /** Zero is the latest visit; -1 means outside the last eight crossings. */
    public int recentVisit(UUID player, LabyrinthPlace place) {
        PlayerDealer state = playerDealers.get(player);
        if (state == null) return -1;
        int age = 0;
        for (String id : state.recent) {
            if (id.equals(place.id())) return age;
            age++;
        }
        return -1;
    }

    public Set<String> visited(UUID player) {
        PlayerDealer state = playerDealers.get(player);
        return state == null ? Set.of() : Set.copyOf(state.visited);
    }

    /** Distinct vignettes this player has personally reached. */
    public int vignettesVisited(UUID player) {
        int count = 0;
        for (String id : visited(player)) {
            LabyrinthPlace place = LabyrinthPlace.byId(id);
            if (place != null && place.isVignette()) {
                count++;
            }
        }
        return count;
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
    public Waypoint peekReturn(UUID player) {
        Deque<Waypoint> stack = returns.get(player);
        return stack == null ? null : stack.peek();
    }

    /** A completed crossing consumes only the route it actually used. */
    public boolean consumeReturn(UUID player, Waypoint expected) {
        if (expected == null || !expected.equals(peekReturn(player))) return false;
        popReturn(player);
        return true;
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
    /** Translate saved graph positions when an existing landscape moves to an outdoor pocket. */
    public boolean hasReturn(java.util.function.Predicate<Waypoint> test){return returns.values().stream().flatMap(java.util.Collection::stream).anyMatch(test);}
    public void remapReturns(java.util.function.UnaryOperator<Waypoint> mapper){
        returns.replaceAll((id,stack)->{Deque<Waypoint> moved=new ArrayDeque<>();for(Waypoint point:stack)moved.addLast(mapper.apply(point));return moved;});setDirty();
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

    /** A personal snapshot without copying every other explorer's saved record. */
    public CompoundTag stateEntry(String vignette,String key){
        CompoundTag state=states.get(vignette);return state==null?new CompoundTag():state.getCompound(key).copy();
    }

    /** Replace one record without copying other readers' books and private inventories. */
    public void setStateEntry(String vignette, String key, CompoundTag tag) {
        CompoundTag state = states.computeIfAbsent(vignette, ignored -> new CompoundTag());
        if (tag.isEmpty()) state.remove(key);
        else state.put(key, tag.copy());
        if (state.isEmpty()) states.remove(vignette);
        setDirty();
    }

    /** Bounded shared discovery cache; personal saved routes remain authoritative after eviction. */
    public void setBoundedStateEntry(String name, String key, CompoundTag tag, int limit) {
        if (limit < 1) throw new IllegalArgumentException("limit");
        CompoundTag state = states.computeIfAbsent(name, ignored -> new CompoundTag());
        CompoundTag entry = tag.copy();
        if (state.contains(key)) entry.putLong("__order", state.getCompound(key).getLong("__order"));
        else {
            long order = state.getLong("__sequence") + 1;
            state.putLong("__sequence", order); entry.putLong("__order", order);
            if (state.getAllKeys().size() > limit) {
                String oldest = null; long first = Long.MAX_VALUE;
                for (String candidate : state.getAllKeys()) if (!candidate.equals("__sequence")) {
                    long seen = state.getCompound(candidate).getLong("__order");
                    if (seen < first) { first = seen; oldest = candidate; }
                }
                if (oldest != null) state.remove(oldest);
            }
        }
        state.put(key, entry); setDirty();
    }

    /** Includes offline explorers, whose unfinished visits must remain reachable. */
    public Set<UUID> visitorsTo(LabyrinthPlace place) {
        Set<UUID> readers = new HashSet<>();
        playerDealers.forEach((id, dealer) -> { if (dealer.visited.contains(place.id())) readers.add(id); });
        return Set.copyOf(readers);
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

    public static LabyrinthData load(CompoundTag tag, HolderLookup.Provider registries) {
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

        ListTag playerDealerList = tag.getList("PlayerDealers", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerDealerList.size(); i++) {
            CompoundTag p = playerDealerList.getCompound(i);
            PlayerDealer state = new PlayerDealer();
            state.dryDeals = p.getInt("DryDeals");
            state.hillaryScent = p.getBoolean("HillaryScent");
            ListTag deals = p.getList("Deals", Tag.TAG_COMPOUND);
            for (int j = 0; j < deals.size(); j++) {
                CompoundTag d = deals.getCompound(j);
                String door = d.getString("Door");
                String place = d.getString("Place");
                if (!door.isEmpty() && !place.isEmpty()) {
                    state.deals.put(door, new Deal(place, d.getBoolean("Leak"), d.getBoolean("Bark")));
                }
            }
            ListTag visited = p.getList("Visited", Tag.TAG_STRING);
            for (int j = 0; j < visited.size(); j++) {
                state.visited.add(visited.getString(j));
            }
            data.playerDealers.put(p.getUUID("Player"), state);
            ListTag recent = p.getList("Recent", Tag.TAG_STRING);
            for (int j = 0; j < Math.min(8, recent.size()); j++) state.recent.addLast(recent.getString(j));
            ListTag nodes = p.getList("Nodes", Tag.TAG_COMPOUND);
            for (int j = 0; j < nodes.size(); j++) {
                CompoundTag n = nodes.getCompound(j);
                Map<String, Deal> remembered = new LinkedHashMap<>();
                ListTag doorsTag = n.getList("Doors", Tag.TAG_COMPOUND);
                for (int k = 0; k < doorsTag.size(); k++) {
                    CompoundTag d = doorsTag.getCompound(k);
                    if (!d.getString("Door").isEmpty() && !d.getString("Place").isEmpty())
                        remembered.put(d.getString("Door"), new Deal(d.getString("Place"), d.getBoolean("Leak"), false));
                }
                state.nodes.put(n.getLong("Key"), remembered);
            }
        }

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

        ListTag playerDealerList = new ListTag();
        for (Map.Entry<UUID, PlayerDealer> entry : playerDealers.entrySet()) {
            CompoundTag p = new CompoundTag();
            p.putUUID("Player", entry.getKey());
            PlayerDealer state = entry.getValue();
            p.putInt("DryDeals", state.dryDeals);
            p.putBoolean("HillaryScent", state.hillaryScent);
            ListTag deals = new ListTag();
            for (Map.Entry<String, Deal> dealt : state.deals.entrySet()) {
                CompoundTag d = new CompoundTag();
                d.putString("Door", dealt.getKey());
                d.putString("Place", dealt.getValue().place());
                d.putBoolean("Leak", dealt.getValue().leak());
                d.putBoolean("Bark", dealt.getValue().bark());
                deals.add(d);
            }
            p.put("Deals", deals);
            ListTag visited = new ListTag();
            for (String place : state.visited) {
                visited.add(StringTag.valueOf(place));
            }
            p.put("Visited", visited);
            ListTag recent = new ListTag();
            for (String place : state.recent) recent.add(StringTag.valueOf(place));
            p.put("Recent", recent);
            ListTag nodes = new ListTag();
            for (Map.Entry<Long, Map<String, Deal>> node : state.nodes.entrySet()) {
                CompoundTag n = new CompoundTag();
                n.putLong("Key", node.getKey());
                ListTag doorsTag = new ListTag();
                for (Map.Entry<String, Deal> dealt : node.getValue().entrySet()) {
                    CompoundTag d = new CompoundTag();
                    d.putString("Door", dealt.getKey());
                    d.putString("Place", dealt.getValue().place());
                    d.putBoolean("Leak", dealt.getValue().leak());
                    doorsTag.add(d);
                }
                n.put("Doors", doorsTag);
                nodes.add(n);
            }
            p.put("Nodes", nodes);
            playerDealerList.add(p);
        }
        tag.put("PlayerDealers", playerDealerList);

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
