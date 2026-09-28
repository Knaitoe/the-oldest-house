package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBetweenRoom;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Where the labyrinth's places stand: stacked in slots above the manor (above
 * the room between rooms' pocket) in the House dimension, over the manor's
 * own chunk columns, so a player in the manor already has every place
 * loaded and a door can shift them there with nothing to wait for. Each slot
 * is filled solid, so a tunnel out of any room only finds more wall, and
 * enough of it above that no weather is heard through the ceiling.
 *
 * A manor built high enough that the stack would not fit under the sky has
 * its slots below it instead.
 */
public final class LabyrinthPlaces {
    public static final int SLOT_HEIGHT = 24;
    /** How far the copied vestibule reaches behind an entry door, and how wide and tall it is. */
    public static final int VESTIBULE_DEPTH = 16;
    public static final int VESTIBULE_HALF_WIDTH = 7;
    public static final int VESTIBULE_TOP = 7;
    private static final int PAD = 8;
    private static final int FLOOR_IN_SLOT = 5;
    private static final int CLEAR_ABOVE_POCKET = 26;
    private static final int MAX_Y = 318;
    private static final int MIN_Y = -60;

    private LabyrinthPlaces() {
    }

    public static int slotCount() {
        int count = 0;
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            if (place.slot() >= 0) {
                count++;
            }
        }
        return count;
    }

    /** Whether the stack goes above the manor (else below it). */
    public static boolean stackAbove(BlockPos origin) {
        int top = origin.getY() + HouseBetweenRoom.pocketDy(origin) + CLEAR_ABOVE_POCKET + slotCount() * SLOT_HEIGHT;
        return top <= MAX_Y;
    }

    /** The lowest y of a slot. */
    public static int slotBottom(BlockPos origin, int slot) {
        if (stackAbove(origin)) {
            return origin.getY() + HouseBetweenRoom.pocketDy(origin) + CLEAR_ABOVE_POCKET + slot * SLOT_HEIGHT;
        }
        return Math.max(MIN_Y, origin.getY() - SLOT_HEIGHT - (slot + 1) * SLOT_HEIGHT);
    }

    /** A place's base (its entry door's floor level, under the door), or null for the hallway's end. */
    @Nullable
    public static BlockPos base(BlockPos origin, LabyrinthPlace place) {
        if (place.slot() < 0) {
            return null;
        }
        return new BlockPos(
                origin.getX() + HouseLayout.CENTER_X,
                slotBottom(origin, place.slot()) + FLOOR_IN_SLOT,
                origin.getZ() + HouseLayout.CENTER_Z
        );
    }

    /** The vestibule volume relative to a place's base: south of the door, door wall row included. */
    public static BoundingBox localVestibule() {
        return new BoundingBox(-VESTIBULE_HALF_WIDTH, -1, 1, VESTIBULE_HALF_WIDTH, VESTIBULE_TOP, 1 + VESTIBULE_DEPTH);
    }

    /** Room and vestibule together, absolute. */
    @Nullable
    public static BoundingBox placeBounds(BlockPos origin, LabyrinthPlace place) {
        BlockPos base = base(origin, place);
        BoundingBox room = place.room();
        if (base == null || room == null) {
            return null;
        }
        BoundingBox local = new BoundingBox(
                Math.min(room.minX(), -VESTIBULE_HALF_WIDTH), Math.min(room.minY(), -1), room.minZ(),
                Math.max(room.maxX(), VESTIBULE_HALF_WIDTH), Math.max(room.maxY(), VESTIBULE_TOP), 1 + VESTIBULE_DEPTH
        );
        return local.moved(base.getX(), base.getY(), base.getZ());
    }

    /** The whole solid slot a place is carved into, absolute. */
    @Nullable
    public static BoundingBox slotBounds(BlockPos origin, LabyrinthPlace place) {
        BoundingBox bounds = placeBounds(origin, place);
        if (bounds == null) {
            return null;
        }
        int bottom = slotBottom(origin, place.slot());
        return new BoundingBox(
                bounds.minX() - PAD, bottom, bounds.minZ() - PAD,
                bounds.maxX() + PAD, bottom + SLOT_HEIGHT - 1, bounds.maxZ() + PAD
        );
    }

    /** Whether a position lies in any slot of the stack. */
    public static boolean isInStack(BlockPos origin, BlockPos pos) {
        return placeAt(origin, pos) != null;
    }

    public static boolean isInStack(BlockPos origin, double x, double y, double z) {
        return isInStack(origin, BlockPos.containing(x, y, z));
    }

    /** The place whose slot holds a position, if any. */
    @Nullable
    public static LabyrinthPlace placeAt(BlockPos origin, BlockPos pos) {
        for (LabyrinthPlace place : LabyrinthPlace.values()) {
            BoundingBox slot = slotBounds(origin, place);
            if (slot != null && slot.isInside(pos)) {
                return place;
            }
        }
        return null;
    }
}
