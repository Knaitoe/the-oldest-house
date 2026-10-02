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
 * Slots fill upwards while they fit under the top of the world, then
 * carry on downwards below the manor, so a manor built high has most (or
 * all) of its places below it. When that column is full, the next slots
 * take the same layers in a column further east (see {@link #column}):
 * those places are not over the manor's chunks, which a door's shift loads
 * before it moves anyone.
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
    /**
     * How far east of the first column each further one stands: wider than
     * any place (and its padding) and clear of the manor's own footprint.
     */
    public static final int COLUMN_SPACING = 96;
    // Slot 32 starts a fifth column at low/common manor heights; older slots never move.
    private static final int MAX_COLUMNS = 5;
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

    /** The bottom of the first slot above the manor. */
    private static int firstAbove(BlockPos origin) {
        return origin.getY() + HouseBetweenRoom.pocketDy(origin) + CLEAR_ABOVE_POCKET;
    }

    /** How many slots fit above the manor, under the top of the world. */
    public static int slotsAbove(BlockPos origin) {
        return Math.max(0, (MAX_Y + 1 - firstAbove(origin)) / SLOT_HEIGHT);
    }

    /** Whether the stack starts above the manor (else everything is below it). */
    public static boolean stackAbove(BlockPos origin) {
        return slotsAbove(origin) > 0;
    }

    /** How many slots fit below the manor, above the bottom of the world. */
    public static int slotsBelow(BlockPos origin) {
        return Math.max(0, (origin.getY() - SLOT_HEIGHT - MIN_Y) / SLOT_HEIGHT);
    }

    /** How many slots one column holds for this manor: all that fit above it and below it. */
    public static int slotsPerColumn(BlockPos origin) {
        return slotsAbove(origin) + slotsBelow(origin);
    }

    /**
     * Which column a slot stands in. The first column is over the manor;
     * once it is full, slots carry on in the same layers of another column,
     * {@link #COLUMN_SPACING} blocks further east, and so on.
     */
    public static int column(BlockPos origin, int slot) {
        int perColumn = slotsPerColumn(origin);
        return perColumn <= 0 ? MAX_COLUMNS : slot / perColumn;
    }

    private static int layer(BlockPos origin, int slot) {
        int perColumn = slotsPerColumn(origin);
        return perColumn <= 0 ? slot : slot % perColumn;
    }

    /**
     * The lowest y of a slot. In each column, slots fill upwards above the
     * manor while they fit, then carry on downwards below it.
     */
    public static int slotBottom(BlockPos origin, int slot) {
        int above = slotsAbove(origin);
        int layer = layer(origin, slot);
        if (layer < above) {
            return firstAbove(origin) + layer * SLOT_HEIGHT;
        }
        return origin.getY() - SLOT_HEIGHT - (layer - above + 1) * SLOT_HEIGHT;
    }

    /** Whether a slot lies within the world, above or below, in a column there is room for. */
    public static boolean fits(BlockPos origin, int slot) {
        return slot >= 0
                && column(origin, slot) < MAX_COLUMNS
                && slotBottom(origin, slot) >= MIN_Y
                && slotBottom(origin, slot) + SLOT_HEIGHT - 1 <= MAX_Y;
    }

    /**
     * A place's base (its entry door's floor level, under the door), or null
     * for the hallway's end or a place with no room left for it in the world.
     */
    @Nullable
    public static BlockPos base(BlockPos origin, LabyrinthPlace place) {
        if (!fits(origin, place.slot())) {
            return null;
        }
        return new BlockPos(
                origin.getX() + HouseLayout.CENTER_X + column(origin, place.slot()) * COLUMN_SPACING,
                slotBottom(origin, place.slot()) + (place == LabyrinthPlace.DROWNED_TOWN || place == LabyrinthPlace.BARN_WELL ? 14 : FLOOR_IN_SLOT),
                origin.getZ() + HouseLayout.CENTER_Z - (NovelRooms.outside(place) ? 4096 + place.slot()*192 : 0)
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
