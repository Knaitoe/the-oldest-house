package io.github.knaitoe.theoldesthouse.house;

import java.util.List;

/**
 * Relative geometry of The Oldest House.
 *
 * Every system that needs to know where the house is (the builder, boundary
 * transitions, mirror reconciliation, the sightline renderer, spawn-site
 * checks and the structure tests) reads from here, so the architecture only
 * has to be described once.
 *
 * Coordinates are relative to the House origin. +X is east, +Z is south and
 * the front of the house faces north (-Z). "Left" and "right" follow the plan
 * drawn with the front at the top: left is west (low X), right is east.
 * y=0 is the ground-floor floor course; rooms start at y=1.
 */
public final class HouseLayout {
    /** Bumped whenever the generated architecture changes incompatibly. */
    public static final int LAYOUT_VERSION = 2;

    public static final int BASEMENT_FLOOR_Y = -5;
    public static final int GROUND_FLOOR_Y = 0;
    public static final int UPPER_FLOOR_Y = 6;

    /** Centre line of the front door, the long hall and the impossible door. */
    public static final int AXIS_X = 15;
    public static final int FRONT_DOOR_Z = 4;
    /** Rear exterior wall at the end of the hall; the first impossible door appears here. */
    public static final int THRESHOLD_Z = 26;

    public static final int HALL_MIN_X = AXIS_X - 1;
    public static final int HALL_MAX_X = AXIS_X + 1;

    // Bounds of every generated block (roofs, chimneys, bay, approach).
    public static final int MIN_X = -4;
    public static final int MAX_X = 29;
    public static final int MIN_Z = -8;
    public static final int MAX_Z = 29;
    public static final int MIN_Y = BASEMENT_FLOOR_Y;
    public static final int MAX_Y = 22;

    // Above-ground volume the house claims: cleared before building and
    // required to be empty of player blocks before a natural spawn.
    public static final int CLEAR_MIN_X = -4;
    public static final int CLEAR_MAX_X = 29;
    public static final int CLEAR_MIN_Z = -3;
    public static final int CLEAR_MAX_Z = 29;

    /** Rough centre of the whole house, used for spawn projection and observer range. */
    public static final int CENTER_X = 13;
    public static final int CENTER_Z = 12;

    private HouseLayout() {
    }

    // ------------------------------------------------------------------
    // Geometry records
    // ------------------------------------------------------------------

    public record Box(int x0, int y0, int z0, int x1, int y1, int z1) {
        public boolean contains(int x, int y, int z) {
            return x >= x0 && x <= x1 && y >= y0 && y <= y1 && z >= z0 && z <= z1;
        }
    }

    public enum RoofKind {
        /** Gable roof whose ridge runs north-south; gable ends face north/south. */
        RIDGE_Z,
        /** Gable roof whose ridge runs east-west; gable ends face east/west. */
        RIDGE_X,
        /** Hipped roof sloping on all four sides. */
        HIP
    }

    /**
     * One independent roof system. Surface height rises one block per block of
     * distance from the outer edge of its rectangle.
     *
     * @param baseY     surface height of the outermost course
     * @param wallTop   top course of the walls the roof sits on; gable-end walls
     *                  are filled from wallTop + 1 up to the roof surface
     * @param verge     overhang beyond each gable-end wall plane
     * @param startGable whether the low-coordinate end is an exposed gable
     * @param endGable   whether the high-coordinate end is an exposed gable
     * @param flared    whether the outermost course is a flatter slab kick
     */
    public record Roof(
            String name,
            RoofKind kind,
            int x0,
            int z0,
            int x1,
            int z1,
            int baseY,
            int wallTop,
            int verge,
            boolean startGable,
            boolean endGable,
            boolean flared
    ) {
        public boolean covers(int x, int z) {
            return x >= x0 && x <= x1 && z >= z0 && z <= z1;
        }

        public int depth(int x, int z) {
            return switch (kind) {
                case RIDGE_Z -> Math.min(x - x0, x1 - x);
                case RIDGE_X -> Math.min(z - z0, z1 - z);
                case HIP -> Math.min(Math.min(x - x0, x1 - x), Math.min(z - z0, z1 - z));
            };
        }

        public int height(int x, int z) {
            return baseY + depth(x, z);
        }
    }

    /**
     * An air volume of a room. Roof-bounded rooms (lofts, the long gallery)
     * extend up to the underside of their roof.
     */
    public record Room(String name, Box box, Roof roof) {
        public boolean contains(int x, int y, int z) {
            if (!box.contains(x, y, z)) {
                return false;
            }
            return roof == null || !roof.covers(x, z) || y < roof.height(x, z);
        }
    }

    /**
     * Wall-inclusive volume of one storey of one mass. Used to decide when a
     * player has crossed into (or out of) the domestic interior.
     */
    public record Mass(String name, int x0, int z0, int x1, int z1, int floorY, int topY) {
    }

    public enum Face {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    /**
     * A glazed opening in an exterior wall.
     *
     * @param plane wall coordinate (z for north/south faces, x for east/west)
     * @param a0    first cell along the wall (x for north/south, z for east/west)
     * @param a1    last cell along the wall
     */
    public record Window(Face face, int plane, int a0, int a1, int y0, int y1) {
        public int x(int along) {
            return face == Face.NORTH || face == Face.SOUTH ? along : plane;
        }

        public int z(int along) {
            return face == Face.NORTH || face == Face.SOUTH ? plane : along;
        }
    }

    /** An exterior door; x/y/z is the lower door half. */
    public record ExteriorDoor(String name, int x, int y, int z, Face face) {
    }

    // ------------------------------------------------------------------
    // Roof systems
    // ------------------------------------------------------------------

    /** Dominant front-left great-room gable (tallest ordinary ridge). */
    public static final Roof ROOF_GREAT = new Roof(
            "great_room", RoofKind.RIDGE_Z, -1, -2, 14, 16, 10, 10, 1, true, true, false);
    /** Small, low gable over the recessed entrance and hall; runs into the rear cross-gable. */
    public static final Roof ROOF_ENTRY = new Roof(
            "entry", RoofKind.RIDGE_Z, 13, -1, 17, 18, 11, 10, 1, true, false, false);
    /** Front-right kitchen gable: lower eaves, one-and-a-half storeys. */
    public static final Roof ROOF_KITCHEN = new Roof(
            "kitchen", RoofKind.RIDGE_Z, 16, 0, 28, 13, 8, 8, 1, true, true, false);
    /** Perpendicular rear cross-gable over the study wing and rear hall. */
    public static final Roof ROOF_CROSS = new Roof(
            "cross", RoofKind.RIDGE_X, 0, 14, 18, 27, 9, 9, 1, true, true, false);
    /** Separate hipped roof of the stair tower, highest point of the house. */
    public static final Roof ROOF_TOWER = new Roof(
            "tower", RoofKind.HIP, 16, 11, 25, 21, 14, 14, 0, false, false, true);
    /** Low service-range roof behind the tower. */
    public static final Roof ROOF_SERVICE = new Roof(
            "service", RoofKind.RIDGE_X, 17, 19, 28, 26, 8, 8, 1, false, true, false);
    /**
     * Wall dormer rising from the great room's west wall above the literary
     * bedroom, breaking what would otherwise be one long roof plane.
     */
    public static final Roof ROOF_GREAT_DORMER = new Roof(
            "great_dormer", RoofKind.RIDGE_X, -1, 10, 6, 15, 12, 11, 1, true, false, false);
    /** Single dormer on the kitchen's east slope. */
    public static final Roof ROOF_KITCHEN_DORMER = new Roof(
            "kitchen_dormer", RoofKind.RIDGE_X, 25, 1, 28, 5, 8, 8, 1, false, true, false);

    /** Placement priority for equal-depth conflicts: earlier wins. */
    public static final List<Roof> ROOFS = List.of(
            ROOF_KITCHEN_DORMER,
            ROOF_GREAT_DORMER,
            ROOF_TOWER,
            ROOF_GREAT,
            ROOF_CROSS,
            ROOF_ENTRY,
            ROOF_KITCHEN,
            ROOF_SERVICE
    );

    // ------------------------------------------------------------------
    // Rooms
    // ------------------------------------------------------------------

    public static final Room GREAT_ROOM = new Room("great_room", new Box(1, 1, 1, 12, 5, 14), null);
    public static final Room GREAT_BAY = new Room("great_bay", new Box(5, 1, -1, 8, 5, 0), null);
    public static final Room HALL = new Room("hall", new Box(HALL_MIN_X, 1, FRONT_DOOR_Z + 1, HALL_MAX_X, 5, THRESHOLD_Z - 1), null);
    public static final Room KITCHEN = new Room("kitchen", new Box(18, 1, 2, 26, 5, 11), null);
    public static final Room STUDY = new Room("study", new Box(2, 1, 16, 12, 5, 25), null);
    public static final Room STAIR_TOWER = new Room("stair_tower", new Box(18, 1, 13, 23, 14, 19), null);
    public static final Room SCULLERY = new Room("scullery", new Box(18, 1, 21, 26, 5, 24), null);
    public static final Room CELLAR = new Room("cellar", new Box(18, -4, 13, 26, -1, 24), null);

    public static final Room PRINCIPAL_BEDROOM = new Room("principal_bedroom", new Box(1, 7, 0, 12, 10, 8), null);
    public static final Room LITERARY_BEDROOM = new Room("literary_bedroom", new Box(1, 7, 10, 12, 10, 14), null);
    /** Includes its ceiling course (y 10) so no roof course is ever placed there. */
    public static final Room UPPER_HALL = new Room("upper_hall", new Box(HALL_MIN_X, 7, 1, HALL_MAX_X, 10, 25), null);
    public static final Room MAKER_LOFT = new Room("maker_loft", new Box(18, 7, 2, 26, 14, 11), ROOF_KITCHEN);
    public static final Room LONG_GALLERY = new Room("long_gallery", new Box(2, 7, 16, 12, 15, 25), ROOF_CROSS);
    public static final Room BOX_ROOM = new Room("box_room", new Box(18, 7, 21, 26, 11, 24), ROOF_SERVICE);

    public static final List<Room> ROOMS = List.of(
            GREAT_ROOM,
            GREAT_BAY,
            HALL,
            KITCHEN,
            STUDY,
            STAIR_TOWER,
            SCULLERY,
            CELLAR,
            PRINCIPAL_BEDROOM,
            LITERARY_BEDROOM,
            UPPER_HALL,
            MAKER_LOFT,
            LONG_GALLERY,
            BOX_ROOM
    );

    // ------------------------------------------------------------------
    // Masses (wall-inclusive, per storey) for boundary transitions
    // ------------------------------------------------------------------

    public static final List<Mass> MASSES = List.of(
            new Mass("great_room_ground", 0, 0, 13, 15, 0, 5),
            new Mass("great_room_bay", 4, -2, 9, 0, 0, 5),
            new Mass("great_room_upper", 0, -1, 13, 15, 6, 10),
            new Mass("hall_ground", 13, FRONT_DOOR_Z, 17, THRESHOLD_Z, 0, 5),
            new Mass("hall_upper", 13, 0, 17, THRESHOLD_Z, 6, 10),
            new Mass("kitchen_wing", 17, 1, 27, 12, 0, 8),
            new Mass("study_wing", 1, 15, 13, 26, 0, 9),
            new Mass("stair_tower", 17, 12, 24, 20, 0, 14),
            new Mass("service_range", 17, 20, 27, 25, 0, 8),
            new Mass("cellar", 17, 12, 27, 25, BASEMENT_FLOOR_Y, -1)
    );

    // ------------------------------------------------------------------
    // Openings
    // ------------------------------------------------------------------

    public static final ExteriorDoor FRONT_DOOR = new ExteriorDoor("front", AXIS_X, 1, FRONT_DOOR_Z, Face.NORTH);
    public static final ExteriorDoor BACK_DOOR = new ExteriorDoor("back", 26, 1, 12, Face.SOUTH);
    public static final List<ExteriorDoor> EXTERIOR_DOORS = List.of(FRONT_DOOR, BACK_DOOR);

    public static final List<Window> WINDOWS = List.of(
            // Front elevation.
            new Window(Face.NORTH, 0, 1, 2, 2, 3),
            new Window(Face.NORTH, 0, 11, 12, 2, 3),
            new Window(Face.NORTH, -2, 5, 8, 2, 3),
            new Window(Face.NORTH, -1, 2, 4, 8, 9),
            new Window(Face.NORTH, -1, 9, 11, 8, 9),
            new Window(Face.NORTH, -1, 6, 7, 12, 13),
            new Window(Face.NORTH, 0, 14, 16, 8, 9),
            new Window(Face.NORTH, FRONT_DOOR_Z, 14, 14, 2, 3),
            new Window(Face.NORTH, FRONT_DOOR_Z, 16, 16, 2, 3),
            new Window(Face.NORTH, FRONT_DOOR_Z, 15, 15, 3, 3),
            new Window(Face.NORTH, 1, 19, 21, 2, 3),
            new Window(Face.NORTH, 1, 24, 25, 2, 3),
            new Window(Face.NORTH, 1, 21, 23, 8, 9),
            new Window(Face.NORTH, 1, 22, 22, 11, 11),
            new Window(Face.NORTH, 12, 20, 21, 13, 14),
            new Window(Face.NORTH, 20, 25, 26, 2, 3),
            // East elevation.
            new Window(Face.EAST, 9, -1, -1, 2, 3),
            new Window(Face.EAST, 13, 2, 2, 2, 3),
            new Window(Face.EAST, 27, 3, 4, 2, 3),
            new Window(Face.EAST, 27, 10, 11, 2, 3),
            new Window(Face.EAST, 27, 3, 3, 9, 9),
            new Window(Face.EAST, 24, 18, 19, 5, 7),
            new Window(Face.EAST, 24, 15, 16, 9, 11),
            new Window(Face.EAST, 27, 22, 23, 2, 3),
            new Window(Face.EAST, 27, 22, 23, 8, 9),
            // West elevation.
            new Window(Face.WEST, 4, -1, -1, 2, 3),
            new Window(Face.WEST, 0, 4, 4, 3, 4),
            new Window(Face.WEST, 0, 11, 11, 3, 4),
            new Window(Face.WEST, 0, 2, 3, 8, 9),
            new Window(Face.WEST, 0, 12, 13, 8, 9),
            new Window(Face.WEST, 0, 12, 13, 12, 13),
            new Window(Face.WEST, 1, 17, 18, 2, 3),
            new Window(Face.WEST, 1, 22, 23, 2, 3),
            new Window(Face.WEST, 1, 19, 22, 8, 9),
            new Window(Face.WEST, 1, 20, 21, 11, 12)
            // No south-facing glazing: the impossible hallway eventually runs
            // south from the threshold and must never be visible from an
            // ordinary window.
    );

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    private static final double BOUNDARY_MARGIN = 0.55D;

    /**
     * True when a relative position lies inside the house: at least
     * {@link #BOUNDARY_MARGIN} inside the union of all storeys.
     *
     * Masses share walls, so each mass is not shrunk on its own; that left a
     * sliver in every doorway between two masses that counted as outside and
     * bounced players out of the House dimension and straight back in.
     * Instead the player's footprint corners are sampled against the
     * unshrunk union, which only trims the true exterior faces.
     */
    public static boolean isInsideDomesticVolume(double relX, double relY, double relZ) {
        for (double dx = -BOUNDARY_MARGIN; dx <= BOUNDARY_MARGIN; dx += BOUNDARY_MARGIN) {
            for (double dz = -BOUNDARY_MARGIN; dz <= BOUNDARY_MARGIN; dz += BOUNDARY_MARGIN) {
                if (!isWithinAnyMass(relX + dx, relY, relZ + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Vertical ranges overlap between stacked masses (cellar to ground,
     * ground to upper) so stairs never pass through a gap.
     */
    private static boolean isWithinAnyMass(double relX, double relY, double relZ) {
        for (Mass mass : MASSES) {
            if (relX >= mass.x0() && relX <= mass.x1() + 1
                    && relZ >= mass.z0() && relZ <= mass.z1() + 1
                    && relY >= mass.floorY() - 0.1D
                    && relY <= mass.topY() + 1.0D) {
                return true;
            }
        }
        return false;
    }

    /** The exterior door a relative position is passing through, or null. */
    public static ExteriorDoor doorAt(double relX, double relY, double relZ) {
        for (ExteriorDoor door : EXTERIOR_DOORS) {
            double along;
            double normal;
            double centre;
            if (door.face() == Face.NORTH || door.face() == Face.SOUTH) {
                along = relX;
                centre = door.x() + 0.5D;
                normal = relZ - (door.z() + 0.5D);
            } else {
                along = relZ;
                centre = door.z() + 0.5D;
                normal = relX - (door.x() + 0.5D);
            }

            if (Math.abs(normal) <= 1.1D
                    && Math.abs(along - centre) <= 0.52D
                    && relY >= door.y() - 0.45D
                    && relY <= door.y() + 2.2D) {
                return door;
            }
        }
        return null;
    }

    /** True when a relative position is passing through a glazed opening. */
    public static boolean isAtWindow(double relX, double relY, double relZ) {
        for (Window window : WINDOWS) {
            boolean horizontal = window.face() == Face.NORTH || window.face() == Face.SOUTH;
            double normal = horizontal ? relZ : relX;
            double along = horizontal ? relX : relZ;

            if (Math.abs(normal - (window.plane() + 0.5D)) <= 1.1D
                    && along >= window.a0() - 0.25D
                    && along <= window.a1() + 1.25D
                    && relY >= window.y0() - 0.65D
                    && relY <= window.y1() + 1.35D) {
                return true;
            }
        }
        return false;
    }

    /** True when a relative block position lies inside any room's air volume. */
    public static boolean isRoomInterior(int x, int y, int z) {
        for (Room room : ROOMS) {
            if (room.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    /** True inside any storey's wall-inclusive volume (rooms, walls and openings). */
    public static boolean isWithinMass(int x, int y, int z) {
        for (Mass mass : MASSES) {
            if (x >= mass.x0() && x <= mass.x1()
                    && z >= mass.z0() && z <= mass.z1()
                    && y >= mass.floorY() && y <= mass.topY()) {
                return true;
            }
        }
        return false;
    }

    /** True when a relative block position lies within the generated envelope. */
    public static boolean isWithinEnvelope(int x, int y, int z) {
        return x >= MIN_X && x <= MAX_X
                && y >= MIN_Y && y <= MAX_Y
                && z >= CLEAR_MIN_Z && z <= MAX_Z;
    }
}
