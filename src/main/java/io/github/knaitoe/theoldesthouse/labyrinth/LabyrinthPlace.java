package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * The places doors lead to.
 *
 * Every place is laid out the same way, relative to its base: its entry door
 * stands at (0, 0, +1) facing south, the room lies north of it (z at most
 * 0), and south of the door (z from 2) is its vestibule, the stretch of
 * wherever the player came from that is copied in behind them each time
 * they arrive (see {@link LabyrinthDoors}). Where a place is carved is
 * decided by {@link LabyrinthPlaces}: in a slot above the manor, in the
 * House dimension, so nothing ever has to load.
 *
 * The impossible hallway's far end is a place too: its door is set into the
 * hallway's far wall when the hallway opens.
 */
public enum LabyrinthPlace {
    /** The gray: where the hallway's far door leads, with three doors the dealer deals. */
    JUNCTION("junction", Kind.GRAY, 0, new BoundingBox(-5, -1, -13, 5, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("west", new BlockPos(-5, 0, -6), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(5, 0, -6), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("north", new BlockPos(0, 0, -13), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A long gray corridor: back the way you came, or on through a dealt door at the far end. */
    GRAY_CORRIDOR("gray_corridor", Kind.GRAY, 1, new BoundingBox(-2, -1, -28, 2, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** The Tell-Tale Heart: a one-shot vignette. */
    FLOORBOARDS("floorboards", Kind.ONE_SHOT, 2, new BoundingBox(-6, -3, -10, 6, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /**
     * Hill House's Red Room: a copy of the player's own room, recurring. Its
     * room box is the most a copy may take up (see {@link RedRoom}); the
     * copy's own doorway is at (0, 0, 0).
     */
    RED_ROOM("red_room", Kind.RECURRING, 3, new BoundingBox(-15, -3, -15, 15, 9, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** The Conjuring: hide-and-clap, a one-shot vignette. */
    HIDE_AND_CLAP("hide_and_clap", Kind.ONE_SHOT, 4, new BoundingBox(-6, -1, -13, 6, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** The Five and a Half Minute Hallway: a seamless loop that ends only when its time is up (see {@link LabyrinthLoops}). */
    LONG_HALLWAY("long_hallway", Kind.GRAY, 5, new BoundingBox(-2, -1, -49, 14, 3, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(12, 0, -45), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** The spiral staircase: longer to climb than to descend. */
    SPIRAL_STAIR("spiral_stair", Kind.GRAY, 6, new BoundingBox(-2, -1, -4, 2, 18, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 16, 0), Direction.NORTH, LabyrinthData.DEALT)
    )),
    /** The hotel hallway: numbered doors, all locked, the numbers climbing and repeating. */
    HOTEL_HALLWAY("hotel_hallway", Kind.GRAY, 7, new BoundingBox(-2, -1, -49, 14, 3, 0), hotelDoors()),
    /** The far end of the impossible hallway, in the manor itself. */
    HALLWAY_END("hallway_end", Kind.HALLWAY, -1, null, List.of());

    /** What a place is to the dealer. */
    public enum Kind {
        /** Connective: dealt freely, never counted as a vignette. */
        GRAY,
        /** A vignette that is finished once and never dealt again. */
        ONE_SHOT,
        /** A vignette that keeps being dealt. */
        RECURRING,
        /** Not a carved place at all. */
        HALLWAY
    }

    /**
     * A door built into a place. {@code rel} is its lower half relative to
     * the place's base; {@code facing} is the way it faces: towards the side
     * it is approached from.
     */
    public record DoorSpec(String name, BlockPos rel, Direction facing, String destination) {
    }

    private final String id;
    private final Kind kind;
    private final int slot;
    @Nullable
    private final BoundingBox room;
    private final List<DoorSpec> doors;

    LabyrinthPlace(String id, Kind kind, int slot, @Nullable BoundingBox room, List<DoorSpec> doors) {
        this.id = id;
        this.kind = kind;
        this.slot = slot;
        this.room = room;
        this.doors = doors;
    }

    public String id() {
        return id;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isVignette() {
        return kind == Kind.ONE_SHOT || kind == Kind.RECURRING;
    }

    public boolean isOneShot() {
        return kind == Kind.ONE_SHOT;
    }

    /**
     * Whether the place has to be made from something first (the Red Room,
     * from a room of the player's), and so is only dealt once it is ready.
     */
    public boolean needsMaking() {
        return this == RED_ROOM;
    }

    /** Which slot above the manor holds the place, or -1 for the hallway's end. */
    public int slot() {
        return slot;
    }

    /** The room with its walls, floor and ceiling, relative to the base; null for the hallway's end. */
    @Nullable
    public BoundingBox room() {
        return room;
    }

    public List<DoorSpec> doors() {
        return doors;
    }

    public String doorId(DoorSpec spec) {
        return id + "/" + spec.name();
    }

    public String entryDoorId() {
        return id + "/entry";
    }

    /** How often the dealer picks this gray place relative to the others: the plain gray most, the loops less. */
    public int grayWeight() {
        return this == JUNCTION || this == GRAY_CORRIDOR ? 3 : 1;
    }

    /** The hotel's entry door, and a locked room door either side of each of its four straights. */
    private static List<DoorSpec> hotelDoors() {
        List<DoorSpec> doors = new java.util.ArrayList<>();
        doors.add(new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN));
        for (int k = 0; k <= 3; k++) {
            int x = 3 + 3 * k;
            int z = -3 - 12 * k - 4;
            doors.add(new DoorSpec("room_" + k + "_west", new BlockPos(x - 2, 0, z), Direction.EAST, LabyrinthData.LOCKED));
            doors.add(new DoorSpec("room_" + k + "_east", new BlockPos(x + 2, 0, z), Direction.WEST, LabyrinthData.LOCKED));
        }
        return List.copyOf(doors);
    }

    /** Places the dealer can send a door to without it being a vignette: the gray. */
    public boolean isGray() {
        return kind == Kind.GRAY;
    }

    @Nullable
    public static LabyrinthPlace byId(String id) {
        for (LabyrinthPlace place : values()) {
            if (place.id.equals(id)) {
                return place;
            }
        }
        return null;
    }
}
