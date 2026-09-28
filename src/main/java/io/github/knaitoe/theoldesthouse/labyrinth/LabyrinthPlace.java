package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The places doors lead to. Each labyrinth place is carved into the solid
 * labyrinth dimension far from every other (thousands of blocks), so no
 * tunnel ever finds one from another. A player always arrives in front of a
 * place's entry door, facing into the room.
 *
 * The impossible hallway is a place too, in the House dimension; its door
 * is set into the hallway's far wall when the hallway opens.
 */
public enum LabyrinthPlace {
    /** The gray: where the hallway's far door leads, with three doors the dealer deals. */
    JUNCTION("junction", false, new BlockPos(0, 64, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, -1), Direction.SOUTH, LabyrinthData.HALLWAY_OR_RETURN),
            new DoorSpec("west", new BlockPos(-5, 0, 6), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(5, 0, 6), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("south", new BlockPos(0, 0, 13), Direction.NORTH, LabyrinthData.DEALT)
    )),
    /** A long gray corridor: back the way you came, or on through a dealt door at the far end. */
    GRAY_CORRIDOR("gray_corridor", false, new BlockPos(4096, 64, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, -1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, 29), Direction.NORTH, LabyrinthData.DEALT)
    )),
    /** The Tell-Tale Heart: a one-shot vignette. */
    FLOORBOARDS("floorboards", true, new BlockPos(8192, 64, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, -1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** The far end of the impossible hallway, in the House dimension. */
    HALLWAY_END("hallway_end", false, null, List.of());

    /** A door built into a place: {@code rel} is its lower half relative to the place's base. */
    public record DoorSpec(String name, BlockPos rel, Direction facing, String destination) {
    }

    private final String id;
    private final boolean vignette;
    @Nullable
    private final BlockPos base;
    private final List<DoorSpec> doors;

    LabyrinthPlace(String id, boolean vignette, @Nullable BlockPos base, List<DoorSpec> doors) {
        this.id = id;
        this.vignette = vignette;
        this.base = base;
        this.doors = doors;
    }

    public String id() {
        return id;
    }

    public boolean isVignette() {
        return vignette;
    }

    /** Where the place is carved in the labyrinth dimension; null for the hallway's end. */
    @Nullable
    public BlockPos base() {
        return base;
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

    /** Places the dealer can send a door to without it being a vignette: the gray. */
    public boolean isGray() {
        return this == JUNCTION || this == GRAY_CORRIDOR;
    }

    /** The labyrinth place a position belongs to (within 64 blocks of its base), if any. */
    @Nullable
    public static LabyrinthPlace containing(ResourceKey<Level> dimension, BlockPos pos) {
        if (!dimension.equals(HouseDimensions.LABYRINTH)) {
            return null;
        }
        for (LabyrinthPlace place : values()) {
            BlockPos base = place.base;
            if (base != null && Math.abs(pos.getX() - base.getX()) <= 64 && Math.abs(pos.getZ() - base.getZ()) <= 64
                    && Math.abs(pos.getY() - base.getY()) <= 32) {
                return place;
            }
        }
        return null;
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
