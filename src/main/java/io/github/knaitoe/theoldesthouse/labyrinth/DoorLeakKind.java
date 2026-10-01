package io.github.knaitoe.theoldesthouse.labyrinth;

/** Sensory hints, never destination labels. Gray doors can borrow an unrelated hint. */
public enum DoorLeakKind {
    HEARTBEAT, WARM_TV, CLOTH, PHONE, MOTHER, WATER, HOTEL, LAKE;

    public static DoorLeakKind forDestination(LabyrinthPlace place, int lieSeed) {
        if (place == null) return HEARTBEAT;
        return switch (place) {
            case FLOORBOARDS -> HEARTBEAT;
            case RED_ROOM, MODEL_HOME -> WARM_TV;
            case HIDE_AND_CLAP -> CLOTH;
            case HARRIGAN -> PHONE;
            case MOTHER_DEN -> MOTHER;
            case FLOODED_PASSAGE -> WATER;
            case DROWNED_TOWN -> LAKE;
            case HOTEL_HALLWAY -> HOTEL;
            default -> values()[Math.floorMod(lieSeed, values().length)];
        };
    }
}
