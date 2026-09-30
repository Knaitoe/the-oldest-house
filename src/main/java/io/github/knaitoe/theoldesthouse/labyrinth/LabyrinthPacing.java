package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;

/** Budget unsettling discoveries per arrival, rather than independently for every door. */
public final class LabyrinthPacing {
    private LabyrinthPacing() {}
    public static boolean ordinary(LabyrinthPlace place) {
        return place==LabyrinthPlace.JUNCTION || place==LabyrinthPlace.GRAY_CORRIDOR
                || place==LabyrinthPlace.STRAIGHT_HALL || place==LabyrinthPlace.BENT_HALL
                || place==LabyrinthPlace.CROSS_HALL;
    }
    public static boolean quiet(LabyrinthPlace place) {
        return place==LabyrinthPlace.QUIET_ROOM || place==LabyrinthPlace.EXPLORER_CAMP;
    }
    public static boolean anomaly(LabyrinthPlace place) {
        return switch(place) {
            case FOLDED_MAZE, DEEP_MAZE, ABYSS_MAZE, LONG_HALLWAY, SPIRAL_STAIR, HOTEL_HALLWAY,
                    FALSE_DISTANCE, MOVING_THRESHOLD, DUPLICATE_PASSAGE, GRAVITY_DRIFT, COMPRESSION_PASSAGE -> true;
            default -> false;
        };
    }
    /** Three ordinary/story visits separate impossible discoveries, even at great depth. */
    public static int anomalyChance(LabyrinthData data, UUID player) {
        int depth=data.returnDepth(player);
        if(depth<3) return 0;
        for(LabyrinthPlace place:LabyrinthPlace.values()) {
            int age=data.recentVisit(player,place);
            if(anomaly(place) && age>=0 && age<3) return 0;
        }
        return depth<6 ? 3 : depth<9 ? 5 : depth<18 ? 8 : 10;
    }
    public static boolean restDue(LabyrinthData data, UUID player) {
        for(LabyrinthPlace place:LabyrinthPlace.values()) {
            int age=data.recentVisit(player,place);
            if(quiet(place) && age>=0 && age<4) return false;
        }
        for(LabyrinthPlace place:LabyrinthPlace.values()) {
            int age=data.recentVisit(player,place);
            if((anomaly(place) || place==LabyrinthPlace.FLOODED_PASSAGE || place==LabyrinthPlace.FRACTURED_WALKWAY
                    || place==LabyrinthPlace.LIGHT_SINK) && age>=0 && age<3) return true;
        }
        return data.returnDepth(player)>=8;
    }
}
