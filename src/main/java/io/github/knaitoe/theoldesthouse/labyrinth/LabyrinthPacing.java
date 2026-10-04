package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;

/** Budget unsettling discoveries per arrival, rather than independently for every door. */
public final class LabyrinthPacing {
    /** Crossings from the hallway root. Shared story progress never shortens this approach. */
    public static final int STORY_DEPTH = 6;
    public static final int STRANGE_DEPTH = 8;
    public static final int DEEP_DEPTH = 12;
    public static final int ABYSS_DEPTH = 16;
    /** The great staircase is only found this deep. */
    public static final int STAIRCASE_DEPTH = 20;
    private LabyrinthPacing() {}
    public static boolean domestic(int depth) { return depth < STORY_DEPTH; }
    public static boolean ordinary(LabyrinthPlace place) {
        return place==LabyrinthPlace.JUNCTION || place==LabyrinthPlace.GRAY_CORRIDOR
                || place==LabyrinthPlace.STRAIGHT_HALL || place==LabyrinthPlace.BENT_HALL
                || place==LabyrinthPlace.CROSS_HALL || StoneHalls.isStone(place);
    }
    public static boolean quiet(LabyrinthPlace place) {
        return place==LabyrinthPlace.QUIET_ROOM || place==LabyrinthPlace.EXPLORER_CAMP;
    }
    public static boolean physicalTrial(LabyrinthPlace place){return place==LabyrinthPlace.FLOODED_PASSAGE||place==LabyrinthPlace.FRACTURED_WALKWAY||place==LabyrinthPlace.LIGHT_SINK;}
    public static int trialChance(LabyrinthData data,UUID player){
        int depth=data.returnDepth(player);if(domestic(depth))return 0;
        for(var place:LabyrinthPlace.values()){int age=data.recentVisit(player,place);if(physicalTrial(place)&&age>=0&&age<2)return 0;}
        return depth<10?28:depth<16?35:42;
    }
    public static boolean anomaly(LabyrinthPlace place) {
        return switch(place) {
            case FOLDED_MAZE, DEEP_MAZE, ABYSS_MAZE, LONG_HALLWAY, SPIRAL_STAIR, HOTEL_HALLWAY,
                    FALSE_DISTANCE, MOVING_THRESHOLD, DUPLICATE_PASSAGE, GRAVITY_DRIFT, COMPRESSION_PASSAGE -> true;
            default -> false;
        };
    }
    public static boolean loop(LabyrinthPlace place) {
        return place==LabyrinthPlace.LONG_HALLWAY || place==LabyrinthPlace.HOTEL_HALLWAY
                || place==LabyrinthPlace.SPIRAL_STAIR || place==LabyrinthPlace.DUPLICATE_PASSAGE;
    }
    public static boolean loopDue(LabyrinthData data, UUID player) {
        if(data.returnDepth(player)<DEEP_DEPTH)return false;
        for(var place:LabyrinthPlace.values())if(loop(place)){
            int age=data.recentVisit(player,place);if(age>=0&&age<8)return false;
        }
        return true;
    }
    /** Three ordinary/story visits separate impossible discoveries, even at great depth. */
    public static int anomalyChance(LabyrinthData data, UUID player) {
        int depth=data.returnDepth(player);
        if(depth<STRANGE_DEPTH) return 0;
        for(LabyrinthPlace place:LabyrinthPlace.values()) {
            int age=data.recentVisit(player,place);
            if(anomaly(place) && age>=0 && age<3) return 0;
        }
        return depth<DEEP_DEPTH ? 2 : depth<ABYSS_DEPTH ? 3 : depth<24 ? 4 : 5;
    }
    public static boolean restDue(LabyrinthData data, UUID player) {
        if (domestic(data.returnDepth(player))) return false;
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
