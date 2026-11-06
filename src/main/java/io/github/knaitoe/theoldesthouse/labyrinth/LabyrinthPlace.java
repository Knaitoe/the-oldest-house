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
    ), Made.DRESSED),
    /** The first physical maze: branching halls, dead ends and a way onward. */
    GRAY_CORRIDOR("gray_corridor", Kind.GRAY, 1, new BoundingBox(-13, -1, -28, 13, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** The Tell-Tale Heart: a one-shot vignette. */
    FLOORBOARDS("floorboards", Kind.ONE_SHOT, 2, new BoundingBox(-6, -3, -10, 6, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    ), Made.DRESSED),
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
    ), Made.DRESSED),
    /** The long hallway: a testable seamless loop with a bounded occupied-time fallback (see {@link LabyrinthLoops}). */
    LONG_HALLWAY("long_hallway", Kind.GRAY, 5, new BoundingBox(-2, -1, -49, 14, 3, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(12, 0, -45), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** The spiral staircase: longer to climb than to descend. */
    SPIRAL_STAIR("spiral_stair", Kind.GRAY, 6, new BoundingBox(-2, -1, -4, 2, 18, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 16, 0), Direction.NORTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** The hotel hallway: numbered doors, all locked, the numbers climbing and repeating. */
    HOTEL_HALLWAY("hotel_hallway", Kind.GRAY, 7, new BoundingBox(-2, -1, -49, 14, 3, 0), hotelDoors(), Made.DRESSED),
    /** Poltergeist: the model home, with the kid's-room window onto a yard (x -23..-9). See {@link ModelHome}. */
    MODEL_HOME("model_home", Kind.MULTI_VISIT, 8, new BoundingBox(-23, -1, -21, 9, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** Mr Harrigan's Phone: reading, the funeral, then the artifact that follows the player home. */
    HARRIGAN("harrigan", Kind.MULTI_VISIT, 9, new BoundingBox(-8, -1, -25, 8, 7, 1), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    ), Made.DRESSED),
    /** A submerged gray corridor with deliberately spaced air chimneys. */
    FLOODED_PASSAGE("flooded_passage", Kind.GRAY, 10, new BoundingBox(-8, -3, -28, 8, 6, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** A raised broken route: missing the span hurts, but a lower recovery path remains. */
    FRACTURED_WALKWAY("fractured_walkway", Kind.GRAY, 11, new BoundingBox(-4, -1, -28, 4, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 7, -28), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** A broad corridor that audibly presses inward until only its center line remains. */
    COMPRESSION_PASSAGE("compression_passage", Kind.GRAY, 12, new BoundingBox(-3, -1, -28, 3, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** A corridor whose far door refuses to become as close as it looks. */
    FALSE_DISTANCE("false_distance", Kind.GRAY, 13, new BoundingBox(-2, -1, -34, 2, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -34), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** A dim stretch that consumes the light sources brought into it. */
    LIGHT_SINK("light_sink", Kind.GRAY, 14, new BoundingBox(-3, -1, -28, 3, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** One exit, three possible walls; it moves only when nobody sees it. */
    MOVING_THRESHOLD("moving_threshold", Kind.GRAY, 15, new BoundingBox(-8, -1, -15, 8, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-8, 0, -8), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(8, 0, -8), Direction.WEST, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** Three identical exits: two return to the same room, one actually advances. */
    DUPLICATE_PASSAGE("duplicate_passage", Kind.GRAY, 16, new BoundingBox(-8, -1, -15, 8, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-8, 0, -8), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(8, 0, -8), Direction.WEST, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** Sideways gravity drags the player toward a shallow recovery trench. */
    GRAVITY_DRIFT("gravity_drift", Kind.GRAY, 17, new BoundingBox(-5, -6, -28, 4, 6, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** An abandoned explorer camp: finite supplies and one real chance to recover. */
    EXPLORER_CAMP("explorer_camp", Kind.GRAY, 18, new BoundingBox(-6, -1, -15, 6, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** The Mother's den: a recurring anchor, never exhausted by completion. */
    MOTHER_DEN("mother_of_strays", Kind.RECURRING, 19, new BoundingBox(-10, -4, -25, 10, 13, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    ), Made.DRESSED),
    /** A larger maze, with one pair of distant, identical bends sewn together. */
    FOLDED_MAZE("folded_maze", Kind.GRAY, 20, new BoundingBox(-19, -1, -66, 19, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -66), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-19, 0, -18), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(19, 0, -18), Direction.WEST, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** Two fold pairs rotate the player's route as well as changing adjacency. */
    DEEP_MAZE("deep_maze", Kind.GRAY, 21, new BoundingBox(-25, -1, -76, 25, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -76), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-25, 0, -23), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(25, 0, -23), Direction.WEST, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** The widest maze: six hidden crossings, more cycles and longer backtracking. */
    ABYSS_MAZE("abyss_maze", Kind.GRAY, 22, new BoundingBox(-37, -1, -86, 37, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -86), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-37, 0, -28), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(37, 0, -28), Direction.WEST, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** Ordinary connected hallways. Doorways belong at destinations, rather than filling every wall. */
    STRAIGHT_HALL("straight_hall", Kind.GRAY, 23, new BoundingBox(-7, -1, -36, 7, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -36), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    BENT_HALL("bent_hall", Kind.GRAY, 24, new BoundingBox(-17, -1, -33, 7, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(-15, 0, -33), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    CROSS_HALL("cross_hall", Kind.GRAY, 25, new BoundingBox(-15, -1, -32, 15, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("west", new BlockPos(-15, 0, -15), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(15, 0, -15), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("far", new BlockPos(0, 0, -32), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** A quiet waiting room, with chairs and a small finite cache. */
    QUIET_ROOM("quiet_room", Kind.GRAY, 26, new BoundingBox(-6, -1, -15, 6, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** Indian Lake: Proofrock on the bank, its high school's essays, then the drowned church (0.4.67: the whole town). */
    DROWNED_TOWN("drowned_town", Kind.MULTI_VISIT, 27, new BoundingBox(-64, -13, -128, 64, 8, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    ), Made.DRESSED),
    /** Indian Lake: a congregation kept fresh by the lake. */
    PRESERVED_CAVE("preserved_cave", Kind.MULTI_VISIT, 28, new BoundingBox(-15, -3, -47, 15, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    ), Made.DRESSED),
    /** A personal recollection, available only after the shore hunt. */
    SHALLOWS("shallows", Kind.ONE_SHOT, 29, new BoundingBox(-18, -4, -36, 18, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    ), Made.DRESSED),
    /** The recording that survives its recorder. Personal, like the Shallows recollection. */
    PHONE_CANOE("phone_canoe", Kind.ONE_SHOT, 30, new BoundingBox(-26,-4,-60,26,10,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)
    ), Made.DRESSED),
    /** Anansi's Goatman: the latecomer's path, one extra child, and a closed trailer door. */
    GOATMAN("goatman",Kind.ONE_SHOT,31,new BoundingBox(-23,-1,-82,23,13,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)
    ), Made.DRESSED),
    /** A worked opening, a tight squeeze, and the line that leads back. */
    TED_CAVER("ted_caver",Kind.MULTI_VISIT,32,new BoundingBox(-9,-4,-60,9,8,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)
    ), Made.DRESSED),
    ZAMPANO_COURTYARD("zampano_courtyard",Kind.RECURRING,33,new BoundingBox(-15,-1,-41,15,16,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("apartment_west",new BlockPos(-12,0,-8),Direction.EAST,LabyrinthData.DEALT),new DoorSpec("apartment_east",new BlockPos(12,0,-8),Direction.WEST,LabyrinthData.DEALT))),
    WHALE("whale",Kind.MULTI_VISIT,34,new BoundingBox(-14,-1,-33,14,17,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)), Made.DRESSED),
    BARN_WELL("barn_well",Kind.ONE_SHOT,35,new BoundingBox(-17,-13,-39,17,8,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    PLAIN("plain",Kind.ONE_SHOT,36,new BoundingBox(-30,-1,-65,30,16,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)), Made.DRESSED),
    HOSPITAL("hospital",Kind.MULTI_VISIT,37,new BoundingBox(-10,-1,-24,10,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    KAREN_ROOM("karen_room",Kind.RECURRING,38,new BoundingBox(-10,-1,-19,10,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    HOLLOWAY_CAMP("holloway_camp",Kind.MULTI_VISIT,39,new BoundingBox(-11,-1,-71,11,7,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("service",new BlockPos(-7,0,-70),Direction.NORTH,LabyrinthData.RETURN)), Made.DRESSED),
    SEANCE("seance",Kind.ONE_SHOT,40,new BoundingBox(-11,-1,-32,11,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)), Made.DRESSED),
    WALLPAPER_NURSERY("wallpaper_nursery",Kind.MULTI_VISIT,41,new BoundingBox(-10,-1,-28,10,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)), Made.DRESSED),
    BLIND_STRETCH("blind_stretch",Kind.GRAY,42,new BoundingBox(-13,-1,-42,13,4,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("far",new BlockPos(0,0,-41),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    /** The hotel (0.4.105): its guest corridor's end door into the numbered corridor, the back door, and the staff entrance out. */
    HOTEL("hotel",Kind.RECURRING,43,new BoundingBox(-29,-5,-68,29,14,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("numbers",new BlockPos(-14,6,-45),Direction.SOUTH,"place:hotel_hallway"),new DoorSpec("grounds",new BlockPos(0,0,-67),Direction.SOUTH,"place:hotel_grounds"),new DoorSpec("service",new BlockPos(-10,0,-67),Direction.SOUTH,LabyrinthData.DEALT))),
    HOTEL_GROUNDS("hotel_grounds",Kind.GRAY,44,new BoundingBox(-28,-3,-68,28,14,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    HILL_NURSERY("hill_nursery",Kind.ONE_SHOT,45,new BoundingBox(-15,-5,-44,15,12,0),literaryEntry(), Made.DRESSED),
    MINIATURES("miniatures",Kind.MULTI_VISIT,46,new BoundingBox(-15,-1,-31,15,9,0),literaryEntry(), Made.DRESSED),
    MASQUE("masque",Kind.ONE_SHOT,47,new BoundingBox(-12,-1,-78,12,11,0),literaryEntry()),
    USHER("usher",Kind.MULTI_VISIT,48,new BoundingBox(-18,-4,-42,18,13,0),literaryEntry()),
    WINCHESTER("winchester",Kind.ONE_SHOT,49,new BoundingBox(-31,-1,-78,31,17,0),literaryEntry()),
    CHILD_ROOM("child_room",Kind.ONE_SHOT,50,new BoundingBox(-12,-5,-25,12,8,0),literaryEntry()), // 0.4.74: down to the basement
    CRIMSON_HALL("crimson_hall",Kind.MULTI_VISIT,51,new BoundingBox(-19,-1,-44,19,15,0),literaryEntry(), Made.DRESSED),
    BLY_ROUTE("bly_route",Kind.RECURRING,52,new BoundingBox(-13,-5,-54,13,10,0),literaryEntry()),
    ELK_LOT("elk_lot",Kind.ONE_SHOT,53,new BoundingBox(-35,-3,-113,35,17,0),literaryEntry()),
    ELK_FAN("elk_fan",Kind.MULTI_VISIT,54,new BoundingBox(-12,-1,-29,12,11,0),literaryEntry(), Made.DRESSED),
    MAPPING_INTERIOR("mapping_interior",Kind.MULTI_VISIT,55,new BoundingBox(-22,-6,-79,22,15,0),literaryEntry()),
    HOLY_RABBIT("holy_rabbit",Kind.ONE_SHOT,56,new BoundingBox(-35,-5,-126,35,26,0),literaryEntry(), Made.DRESSED),
    CONFESSION("confession",Kind.MULTI_VISIT,57,new BoundingBox(-13,-1,-34,13,11,0),literaryEntry(), Made.DRESSED),
    /** Two stages (0.4.50): the yacht moored under the bluff, then the stream woods and the cave of carcasses; the crew's gate leads back. */
    ELK_CARCASSES("elk_carcasses",Kind.ONE_SHOT,58,new BoundingBox(-64,-9,-250,64,24,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("service",new BlockPos(44,11,-247),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    COSTUME_NIGHT("costume_night",Kind.ONE_SHOT,59,new BoundingBox(-33,-6,-77,33,18,0),literaryEntry()),
    MOVIE_NIGHT("movie_night",Kind.ONE_SHOT,60,new BoundingBox(-35,-12,-93,35,18,0),literaryEntry()),
    WINTER_LAKE("winter_lake",Kind.ONE_SHOT,61,new BoundingBox(-35,-12,-87,35,18,0),literaryEntry()),
    CAMP_BLOOD("camp_blood",Kind.ONE_SHOT,62,new BoundingBox(-33,-3,-104,33,19,0),literaryEntry(), Made.DRESSED),
    DEVILS_ROCK("devils_rock",Kind.MULTI_VISIT,63,new BoundingBox(-34,-6,-124,34,18,0),literaryEntry()), // 0.4.201: the Sandersons' house and the park behind it
    WHEEL("wheel",Kind.MULTI_VISIT,64,new BoundingBox(-27,-1,-65,27,13,0),literaryEntry()),
    GHOSTS_SET("ghosts_set",Kind.MULTI_VISIT,65,new BoundingBox(-12,-5,-27,12,12,0),literaryEntry()),
    END_WORLD_CABIN("end_world_cabin",Kind.ONE_SHOT,66,new BoundingBox(-31,-9,-81,31,19,0),literaryEntry(), Made.DRESSED),
    FAMILY_COPY("family_copy",Kind.MULTI_VISIT,67,new BoundingBox(-34,-17,-71,34,33,0),literaryEntry()),
    OLD_CABIN("old_cabin",Kind.RECURRING,68,new BoundingBox(-12,-9,-29,12,17,0),literaryEntry()),
    /** The deep tier turns to stone: a long vaulted gallery with a side way. */
    STONE_GALLERY("stone_gallery", Kind.GRAY, 69, new BoundingBox(-5, -1, -42, 5, 7, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("side", new BlockPos(4, 0, -21), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("far", new BlockPos(0, 0, -42), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** A pillared stone crossing around a dry fountain. */
    STONE_CROSSING("stone_crossing", Kind.GRAY, 70, new BoundingBox(-12, -1, -26, 12, 8, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("west", new BlockPos(-12, 0, -13), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(12, 0, -13), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("far", new BlockPos(0, 0, -26), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** A stone stair that physically descends before the way goes on. */
    STONE_DESCENT("stone_descent", Kind.GRAY, 71, new BoundingBox(-4, -5, -34, 4, 6, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, -4, -34), Direction.SOUTH, LabyrinthData.DEALT)
    ), Made.DRESSED),
    /** Familiar additions keep their own physical addresses; existing halls are never rebuilt. */
    ALCOVE_HALL("alcove_hall", Kind.GRAY, 72, new BoundingBox(-8,-1,-42,8,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(-8,0,-17),Direction.EAST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-42),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    OFFSET_HALL("offset_hall", Kind.GRAY, 73, new BoundingBox(-5,-1,-40,10,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(4,0,-40),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    SERVICE_LANDING("service_landing", Kind.GRAY, 74, new BoundingBox(-11,-1,-31,11,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(-11,0,-16),Direction.EAST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-31),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    STONE_ARCADE("stone_arcade", Kind.GRAY, 75, new BoundingBox(-6,-1,-40,6,8,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-40),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    STONE_BEND("stone_bend", Kind.GRAY, 76, new BoundingBox(-12,-1,-36,6,7,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(-9,0,-36),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    STONE_LANDING("stone_landing", Kind.GRAY, 77, new BoundingBox(-12,-1,-28,12,8,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(12,0,-16),Direction.WEST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-28),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    /** 0.4.80: appended hazards; every older physical address remains exact. */
    COLD_STORE("cold_store",Kind.GRAY,78,new BoundingBox(-5,-2,-43,5,6,0),grayDoors(0,0,-43)),
    LISTING_HALL("listing_hall",Kind.GRAY,79,new BoundingBox(-5,-3,-43,5,5,0),grayDoors(0,0,-43)),
    QUIET_HALL("quiet_hall",Kind.GRAY,80,new BoundingBox(-5,-3,-43,5,7,0),grayDoors(0,0,-43)),
    UNWATCHED_MAZE("unwatched_maze",Kind.GRAY,81,new BoundingBox(-16,-1,-35,16,5,0),grayDoors(2,0,-35)),
    MIRROR_HALL("mirror_hall",Kind.GRAY,82,new BoundingBox(-25,-1,-42,8,5,0),threeDoors(-42,-4,0,4)),
    HEAVY_AND_LIGHT("heavy_and_light",Kind.GRAY,83,new BoundingBox(-7,-4,-44,7,7,0),grayDoors(0,0,-44)),
    NESTED_ROOMS("nested_rooms",Kind.GRAY,84,new BoundingBox(-7,-1,-43,7,7,0),grayDoors(0,0,-43)),
    COUNTING_STAIR("counting_stair",Kind.GRAY,85,new BoundingBox(-6,-1,-47,6,17,0),countingDoors()),
    BENT_CISTERN("bent_cistern",Kind.GRAY,86,new BoundingBox(-11,-4,-41,11,7,0),grayDoors(0,0,-41)),
    SPIRAL_WALKWAY("spiral_walkway",Kind.GRAY,87,new BoundingBox(-12,-4,-30,12,17,0),grayDoors(0,10,-30)),
    CORNER_PRESSURE("corner_pressure",Kind.GRAY,88,new BoundingBox(-19,-1,-18,4,5,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("far",new BlockPos(-19,0,-14),Direction.EAST,LabyrinthData.DEALT))),
    RECEDING_HALL("receding_hall",Kind.GRAY,89,new BoundingBox(-3,-1,-66,3,5,0),grayDoors(0,0,-34)),
    LAST_SCONCE("last_sconce",Kind.GRAY,90,new BoundingBox(-9,-1,-35,9,5,0),grayDoors(0,0,-35)),
    WATCHED_THRESHOLD("watched_threshold",Kind.GRAY,91,new BoundingBox(-11,-1,-23,11,6,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("north",new BlockPos(0,0,-23),Direction.SOUTH,LabyrinthData.DEALT),new DoorSpec("west",new BlockPos(-11,0,-12),Direction.EAST,LabyrinthData.DEALT),new DoorSpec("east",new BlockPos(11,0,-12),Direction.WEST,LabyrinthData.DEALT))),
    DUPLICATE_ROOMS("duplicate_rooms",Kind.GRAY,92,new BoundingBox(-7,-1,-43,7,6,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("first",new BlockPos(7,0,-9),Direction.WEST,LabyrinthData.DEALT),new DoorSpec("second",new BlockPos(7,0,-23),Direction.WEST,LabyrinthData.DEALT),new DoorSpec("third",new BlockPos(7,0,-37),Direction.WEST,LabyrinthData.DEALT))),
    TURNING_GRAVITY("turning_gravity",Kind.GRAY,93,new BoundingBox(-13,-4,-29,13,6,0),grayDoors(0,0,-29)),
    /** Three Fingers (0.4.81): a cottage on the mill road, a soldier's withered paw and three wishes. Interior, multi-visit. */
    THREE_FINGERS("three_fingers",Kind.MULTI_VISIT,94,new BoundingBox(-14,-1,-34,14,10,0),literaryEntry()),
    /** An endless recurring dance room, without an authored Witness resolution. */
    NIGHTCLUB("nightclub",Kind.RECURRING,95,new BoundingBox(-16,-1,-49,16,9,0),literaryEntry()),
    /** The house at the bottom of the lake: a boathouse, a culvert and a house standing on a lake's bed. Outdoor, multi-visit. */
    LAKE_HOUSE("lake_house",Kind.MULTI_VISIT,96,new BoundingBox(LakeHouse.MIN_X,LakeHouse.MIN_Y,LakeHouse.MIN_Z,LakeHouse.MAX_X,LakeHouse.MAX_Y,0),literaryEntry()),
    /** A shared empty-lake aftermath; internal, never offered as another story. */
    LAKE_HOUSE_EMPTY("lake_house_empty",Kind.HALLWAY,97,new BoundingBox(LakeHouse.MIN_X,LakeHouse.MIN_Y,LakeHouse.MIN_Z,LakeHouse.MAX_X,LakeHouse.MAX_Y,0),literaryEntry()),
    /** Borrasca: one reader's four returns to the same town, never another reader's later town. */
    BORRASCA("borrasca",Kind.MULTI_VISIT,98,new BoundingBox(-44,-5,-220,44,26,0),literaryEntry()),
    /** Six original uncanny rooms (0.4.113); every earlier address remains unchanged. */
    PAINTED_INTERIOR("painted_interior",Kind.ONE_SHOT,99,new BoundingBox(-15,-3,-39,15,11,0),literaryEntry()),
    SECOND_WAITRESS("second_waitress",Kind.ONE_SHOT,100,new BoundingBox(-28,-4,-57,28,12,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("till_exit",new BlockPos(-10,0,-6),Direction.WEST,LabyrinthData.RETURN))),
    TUNNEL_PERIPHERY("tunnel_periphery",Kind.ONE_SHOT,101,new BoundingBox(-5,-2,-147,5,8,0),literaryEntry()),
    MISSING_SLEEP("missing_sleep",Kind.ONE_SHOT,102,new BoundingBox(-11,-2,-31,11,12,0),literaryEntry()),
    LAYERED_MAN("layered_man",Kind.ONE_SHOT,103,new BoundingBox(-9,-7,-32,9,6,0),literaryEntry()),
    MOTION_LIGHT("motion_light",Kind.ONE_SHOT,104,new BoundingBox(-9,-2,-27,10,9,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("service_exit",new BlockPos(8,0,-5),Direction.EAST,LabyrinthData.RETURN))),
    BACKSEAT("backseat",Kind.ONE_SHOT,105,new BoundingBox(-14,-3,-58,14,18,4),literaryEntry()),
    SILO("silo",Kind.ONE_SHOT,106,new BoundingBox(-12,-3,-12,12,69,16),List.of(new DoorSpec("entry",new BlockPos(0,0,13),Direction.SOUTH,LabyrinthData.RETURN))),
    READ_COATROOM("hall_coatrack",Kind.INTERLUDE,107,new BoundingBox(-8,-2,-16,8,6,4),List.of()),
    READ_PORCH("screen_door",Kind.INTERLUDE,108,new BoundingBox(-25,-4,-48,25,18,16),List.of()),
    READ_WAITING("waiting_chair",Kind.INTERLUDE,109,new BoundingBox(-11,-3,-19,11,7,5),List.of()),
    READ_BATHROOM("night_sink",Kind.INTERLUDE,110,new BoundingBox(-8,-3,-16,8,7,5),List.of()),
    READ_SHELTER("jukebox_shelter",Kind.INTERLUDE,111,new BoundingBox(-24,-6,-30,24,10,8),List.of()),
    /** 0.4.168: the kitchen table, the first quiet room. */
    READ_KITCHEN("kitchen_table",Kind.INTERLUDE,112,new BoundingBox(-7,-3,-17,8,6,5),List.of()),
    /** 0.4.173: the owner's Bloody Mary vignette, "Third Floor Girls'": a school hallway and the girls' bathroom off it. */
    DARE_IN_THE_DARK("dare_in_the_dark",Kind.ONE_SHOT,113,new BoundingBox(-4,-2,-29,14,5,0),literaryEntry()),
    /**
     * 0.4.197 (owner: "Design more hallways that are just hallways."): four plain plaster halls with nothing bled into them, built
     * by {@link HallVariations}. A hall with one jog and a side niche door; a hall that forks into two; a broad hall down a row
     * of pillars with a door off its side; and a hall that zigzags twice.
     */
    JOGGED_HALL("jogged_hall", Kind.GRAY, 114, new BoundingBox(-3,-1,-56,4,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(-3,0,-14),Direction.EAST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(2,0,-56),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    FORKED_HALL("forked_hall", Kind.GRAY, 115, new BoundingBox(-12,-1,-31,12,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("left",new BlockPos(-10,0,-31),Direction.SOUTH,LabyrinthData.DEALT),
            new DoorSpec("right",new BlockPos(10,0,-31),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    PILLAR_HALL("pillar_hall", Kind.GRAY, 116, new BoundingBox(-3,-1,-46,3,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(3,0,-24),Direction.WEST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-46),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    ZIGZAG_HALL("zigzag_hall", Kind.GRAY, 117, new BoundingBox(-2,-1,-44,10,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-44),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    /**
     * 0.4.199 (owner: "You can add further variant hallways too - say, one with a massive ceiling and one with a crumbled floor
     * players have to 'parkour' across"). A plaster hall whose ceiling is fifteen blocks up, lamps hung on long chains below a
     * dark coffered ceiling; and a hall whose floor has fallen into a shallow cellar, crossed on what is left of the boards.
     */
    VAULTED_HALL("vaulted_hall", Kind.GRAY, 118, new BoundingBox(-4,-1,-42,4,17,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(-4,0,-21),Direction.EAST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-42),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    CRUMBLED_HALL("crumbled_hall", Kind.GRAY, 119, new BoundingBox(-3,-5,-39,3,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-39),Direction.SOUTH,LabyrinthData.DEALT)), Made.DRESSED),
    /**
     * 0.4.199, the first puzzle room (BorrowedLight): a dim dressing room whose far door has no handle until a lamp's beam,
     * turned by a dressing mirror on a scraped track, is laid on the brass sun beside it. It needs a sixteenth column.
     */
    BORROWED_LIGHT("borrowed_light", Kind.GRAY, 120, new BoundingBox(-7,-1,-22,7,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-22),Direction.SOUTH,LabyrinthData.DEALT))),
    /**
     * 0.4.199, the rest of the puzzle rooms (PuzzleRooms), each after Borrowed Light so no earlier address moves. The Service
     * Lift: a pantry whose dumbwaiter cage, raised by kitchen weights set in its counterweight pan, uncovers the way on.
     */
    SERVICE_LIFT("service_lift", Kind.GRAY, 121, new BoundingBox(-6,-1,-21,6,8,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-21),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The Turning Landing: a square landing in a stairwell, turned a quarter at a time by a heavy handle, between the stairs and a door above. */
    TURNING_LANDING("turning_landing", Kind.GRAY, 122, new BoundingBox(-5,-4,-18,5,12,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,4,-18),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The lamp rooms, Borrowed Light's deep room: one beam through three rooms, seen through interior windows, reached the long way round. */
    LAMP_ROOMS("lamp_rooms", Kind.GRAY, 123, new BoundingBox(-12,-1,-29,12,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(8,0,-29),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The twin lifts, the Service Lift's deep room: two goods lifts on one counterweight, three floors, the door seen from the start. */
    TWIN_LIFT("twin_lift", Kind.GRAY, 124, new BoundingBox(-10,-3,-15,10,14,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,10,-15),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The turning stair, the Turning Landing's deep room: two landings in a tall stairwell, geared together. */
    TURNING_STAIR("turning_stair", Kind.GRAY, 125, new BoundingBox(-10,-4,-22,10,15,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(3,10,-22),Direction.SOUTH,LabyrinthData.DEALT))),
    /** 0.4.199, the way out: a meadow outside that is not outside (WayOut). Dealt once to each reader; no Witness source. */
    WAY_OUT("way_out",Kind.ONE_SHOT,126,new BoundingBox(-60,-8,-116,60,24,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    /** 0.4.200, Feet Up: the family house where the floor is lava, played from the big chair to the attic (FeetUp). Survival. */
    FEET_UP("feet_up",Kind.ONE_SHOT,127,new BoundingBox(-11,-1,-27,11,14,0),literaryEntry()),
    /** 0.4.202, the Whistlers in the Pines: a fire lookout over a snowbound clearing, and the forest of towers far to its north (LookoutTower, LookoutNight). Survival, four visits. */
    WHISTLERS("whistlers",Kind.MULTI_VISIT,128,new BoundingBox(-40,-8,-280,40,30,0),literaryEntry()),
    /** 0.4.203, Work print: a closed video rental shop, a tape with no label in its returns bin, and behind the back room the edit bay and the set of the film it holds (WorkPrintShop, WorkPrint). Survival, over seven House days. */
    WORK_PRINT("work_print",Kind.MULTI_VISIT,129,new BoundingBox(-18,-2,-42,12,7,0),literaryEntry()),
    /** 0.4.204, the Polaroid Trail: a back yard, the crawlspace next door, a neighbourhood cinema and a pine wood, each place a visit, every one photographed from where something stood and watched (PolaroidSite, PolaroidTrail). Understanding, four visits. */
    POLAROID_TRAIL("polaroid_trail",Kind.MULTI_VISIT,130,new BoundingBox(-26,-5,-152,26,18,0),literaryEntry()),
    /** 0.4.205, the Left-Right Game: the road past the Bristol Ferry toll, forking and forking; left, then right, never twice, and the salt for a wrong turn (LeftRightRoad, LeftRightGame). Survival, four visits. */
    LEFT_RIGHT("left_right",Kind.MULTI_VISIT,131,new BoundingBox(-16,-5,-60,47,9,0),literaryEntry()),
    /** 0.4.206, the Dead Air Broadcast: a 1983 basement standing as a set in a black studio, a hallway that is whatever the receiver is tuned to, the salt marsh relay and the master booth (DeadAirSet, DeadAirGame). Understanding, four visits. */
    DEAD_AIR("dead_air",Kind.MULTI_VISIT,132,new BoundingBox(-26,-5,-70,26,26,0),literaryEntry()),
    /** 0.4.207, the God of the Pitt: Ted the Caver's deeper passages, dealt only after the squeeze is retraced; four visits, no Witness story of its own (PittCave, PittGame). Stands at its own height (PittCave.BASE_Y). */
    PITT("god_of_the_pitt",Kind.MULTI_VISIT,133,new BoundingBox(-24,-38,-64,24,16,0),literaryEntry()),
    /** 0.4.208, the dark halls (DarkHalls): the third series of ordinary halls, from depth sixteen, the only ones from twenty-one. The long hall. */
    DARK_LONG("dark_long",Kind.GRAY,134,new BoundingBox(-3,-1,-106,3,9,0),grayDoors(0,0,-105)),
    /** The forking hall: three branches, one through. */
    DARK_FORK("dark_fork",Kind.GRAY,135,new BoundingBox(-19,-1,-60,19,8,0),grayDoors(16,0,-59)),
    /** The ash maze: a small labyrinth with two ways out. */
    DARK_MAZE("dark_maze",Kind.GRAY,136,new BoundingBox(-13,-1,-27,13,6,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far_west",new BlockPos(-10,0,-26),Direction.SOUTH,LabyrinthData.DEALT),new DoorSpec("far_east",new BlockPos(10,0,-26),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The spiral that goes down round itself and comes out under where it started. */
    DARK_SPIRAL("dark_spiral",Kind.GRAY,137,new BoundingBox(-5,-12,-17,5,5,0),grayDoors(0,-10,-16)),
    /** The edge of a great dark room: a walkway along a drop nobody can see the bottom of. */
    DARK_EDGE("dark_edge",Kind.GRAY,138,new BoundingBox(-3,-13,-66,21,9,0),grayDoors(0,0,-65)),
    /** 0.4.209, the closing hall, the first of the dark halls' horrors (ClosingHall, ClosingHallSite): Chad and Daisy at the end of a hall that closes. Survival, dealt only from depth sixteen. */
    CLOSING_HALL("closing_hall",Kind.ONE_SHOT,139,new BoundingBox(-19,-1,-48,5,7,0),literaryEntry()),
    /** 0.4.210, the storeroom, the second of the dark halls' horrors (Storeroom, StoreroomSite): Johnny's storeroom, the list in the reader's own hand, and don't turn round. Survival, dealt only from depth sixteen. */
    STOREROOM("storeroom",Kind.ONE_SHOT,140,new BoundingBox(-4,-1,-35,4,5,0),literaryEntry()),
    /** 0.4.211, the stillness, the third of the dark halls' horrors (Stillness, StillnessSite): the growl made flesh in a wide, low hall; it hunts by sound and stands over the still. Survival, dealt only from depth sixteen. */
    STILLNESS("stillness",Kind.ONE_SHOT,141,new BoundingBox(-7,-1,-51,7,4,0),literaryEntry()),
    /** 0.4.212, the line, the fourth of the dark halls' horrors (Line, LineSite): a line in a space with no walls, and something that takes hold of it behind the reader. Survival, dealt only from depth sixteen. */
    LINE("line",Kind.ONE_SHOT,142,new BoundingBox(-25,-1,-90,25,6,0),literaryEntry()),
    /** 0.4.213, your own light, the last of the dark halls' horrors (OwnLight, OwnLightSite): twin branches, and someone walking the other one with your walk and your light. Understanding, dealt only from depth sixteen. */
    OWN_LIGHT("own_light",Kind.ONE_SHOT,143,new BoundingBox(-5,-1,-50,5,4,0),literaryEntry()),
    /** 0.4.217, Polybius (Polybius, PolybiusSite): an arcade with three real games, their tables kept for the whole server, and a black cabinet with no maker's name. Survival, over as many evenings as it takes. */
    POLYBIUS("polybius",Kind.MULTI_VISIT,144,new BoundingBox(-10,-1,-24,10,6,0),literaryEntry()),
    /** 0.4.218, the replacement (Replacement, ReplacementSite): a street of boarded houses, dealt only to a reader with a companion following them, and something at their heels in its place. Understanding, outside, over as many visits as it takes. */
    REPLACEMENT("replacement",Kind.MULTI_VISIT,145,new BoundingBox(-18,-2,-64,18,12,0),literaryEntry()),
    /** 0.4.227, the last swimmer (LastSwimmer, SwimmerSite): a small municipal pool at closing time, the cover pulled across a lane at a time over someone still swimming, and the fire door held open. Understanding, one visit. */
    LAST_SWIMMER("last_swimmer",Kind.ONE_SHOT,146,new BoundingBox(-15,-7,-46,15,10,0),literaryEntry()),
    /** 0.4.228, Vacant Possession (VacantPossession, VacantSite): a first-floor flat on moving day, every piece carried down to the van, the tenant fading with each, and one chair left at the end. Understanding, one visit. */
    VACANT_POSSESSION("vacant_possession",Kind.ONE_SHOT,147,new BoundingBox(-7,-7,-19,18,8,0),literaryEntry()),
    /** 0.4.230, drafts and pressure (DraftRooms): the drying room, a laundry and its scullery, where the air from a vent opens the far door once it has no other way out. An early puzzle room, family AIR. */
    DRAFT_ROOM("draft_room", Kind.GRAY, 148, new BoundingBox(-7,-1,-20,7,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-20),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The vent rooms, the drying room's deep room: one draft through three rooms, and a door in plain plaster found only by what the air does at it. */
    VENT_ROOMS("vent_rooms", Kind.GRAY, 149, new BoundingBox(-12,-1,-28,12,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(11,0,-28),Direction.SOUTH,LabyrinthData.DEALT))),
    /** 0.4.231, traces (TraceRooms): the shut-up rooms, an ordinary hall under dust, whose floor keeps footprints and whose one loose chair stays where it is dragged. */
    DUST_HALL("dust_hall", Kind.GRAY, 150, new BoundingBox(-4,-1,-36,4,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(4,0,-21),Direction.WEST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-36),Direction.SOUTH,LabyrinthData.DEALT))),
    /** 0.4.231, the door wedge (TraceRooms): a cloakroom whose inner door shuts itself, a long passage, and a far door that gives only while the way behind stands open. An early puzzle room, family HOLD; the wedge is on a hook by the door. */
    PROPPED_DOOR("propped_door", Kind.GRAY, 151, new BoundingBox(-6,-1,-22,6,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(4,0,-22),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The cloakroom's deep room: three rooms under dust and two doors that shut themselves, held by the wedge and a chair dragged into a doorway. */
    SPRUNG_ROOMS("sprung_rooms", Kind.GRAY, 152, new BoundingBox(-8,-1,-30,8,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-30),Direction.SOUTH,LabyrinthData.DEALT))),
    /** 0.4.232, the familiar room (FamiliarRoom): one kitchen, the same for everyone, dealt like a hall through whatever door; it keeps what is left in it, its clock is heard through its doors, and its window looks down on a yard laid out like the halls each reader last walked. */
    FAMILIAR_KITCHEN("familiar_kitchen", Kind.GRAY, 153, new BoundingBox(-31,-5,-15,6,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(6,0,-6),Direction.WEST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(3,0,-15),Direction.SOUTH,LabyrinthData.DEALT))),
    /** 0.4.234, traces in the stone (TraceRooms): a disused stone gallery whose vault sheds grit, which keeps footprints as the shut-up rooms' dust does; an ordinary stone hall. */
    STONE_DUST("stone_dust", Kind.GRAY, 154, new BoundingBox(-4,-1,-40,4,7,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(4,0,-20),Direction.WEST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-40),Direction.SOUTH,LabyrinthData.DEALT))),
    /**
     * 0.4.245, the airing rooms (DraftRooms, TraceRooms): the vent rooms' draft, through two doors on closers under dust. A wedge or a
     * chair dragged into a doorway holds each door, and the air goes where they let it, to a panel in plain plaster. A deep puzzle room
     * offered only once the vent rooms and the sprung rooms are solved.
     */
    AIRING_ROOMS("airing_rooms", Kind.GRAY, 155, new BoundingBox(-12,-1,-28,12,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(11,0,-28),Direction.SOUTH,LabyrinthData.DEALT))),
    /**
     * 0.4.250, the Folding Rooms (FoldingRooms, docs/FOLDING_ROOMS_0_4_250.md): a dining room cut in two by a partition of hinged panels on
     * floor and ceiling tracks, standing in front of the far door, with the pantry round the side. An early puzzle room, family FOLD.
     */
    FOLDING_ROOM("folding_room", Kind.GRAY, 156, new BoundingBox(-9,-1,-17,4,5,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(4,0,-13),Direction.WEST,LabyrinthData.DEALT))),
    /** The folding room's deep room: a long dining room in three by two partitions, a recess where the sideboard stands, and a morning room off it. */
    FOLDED_ROOMS("folded_rooms", Kind.GRAY, 157, new BoundingBox(-9,-1,-26,3,5,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-26),Direction.SOUTH,LabyrinthData.DEALT))),
    /**
     * 0.4.251, the Window Cleaner (WindowCleaner, docs/WINDOW_CLEANER_0_4_251.md): a corridor, a stair up four storeys, and a plant room
     * over a light well where a cleaner is stranded in a cradle; let down past lit windows to the corridor the reader came in by, the
     * cleaner comes back round and leaves by the staff door. A puzzle room, family WINCH, early only.
     */
    WINDOW_CLEANER("window_cleaner", Kind.GRAY, 158, new BoundingBox(-2,-1,-14,17,18,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(17,11,-6),Direction.WEST,LabyrinthData.DEALT))),
    /**
     * 0.4.252, the Night Operator (NightOperator, docs/NIGHT_OPERATOR_0_4_252.md): a switchroom at night, where the reader listens on the
     * jacks for the room a lost caller describes and puts them through, until their footsteps cross the room to the far door. A puzzle
     * room, family LINE, early only.
     */
    NIGHT_OPERATOR("night_operator", Kind.GRAY, 159, new BoundingBox(-6,-1,-12,6,4,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-12),Direction.SOUTH,LabyrinthData.DEALT))),
    /**
     * 0.4.254, the Fuse Board (FuseRooms, docs/FUSE_BOARD_0_4_254.md): a basement workshop, dark but for a fuse board's pilot lamp and the
     * light under a steel shutter; one fuse, a work light and the shutter, and a catch that can only be found in light. An early puzzle
     * room, family POWER.
     */
    FUSE_ROOM("fuse_room", Kind.GRAY, 161, new BoundingBox(-6,-1,-16,6,5,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-15),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The fuse room's deep room: three rooms in a row with windows between, two fuses, four circuits, a latched door that can be wedged, a hoist over a hatch, and a shutter. */
    FUSE_ROOMS("fuse_rooms", Kind.GRAY, 162, new BoundingBox(-6,-6,-30,6,5,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-29),Direction.SOUTH,LabyrinthData.DEALT))),
    /**
     * 0.4.256, the Header Tank (HeaderTanks, docs/HEADER_TANK_0_4_256.md): a utility room with a flooded pit under an attic tank, a
     * platform floating on the water, a valve and a pump; level with the gallery, the platform carries the reader to the doorway. An
     * early puzzle room, family WATER.
     */
    HEADER_TANK("header_tank", Kind.GRAY, 163, new BoundingBox(-6,-7,-11,6,12,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-10),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The header tank's deep rooms: two pits in a row with a landing between, the far one deeper, the transfer valve on the landing. */
    HEADER_TANKS("header_tanks", Kind.GRAY, 164, new BoundingBox(-6,-9,-17,6,12,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-16),Direction.SOUTH,LabyrinthData.DEALT))),
    /** 0.4.253, After the People Lights Have Gone Off (PeopleLights, PeopleLightsSite): a guest for one evening and night in the narrow, tall house Stan built, after the title story of Stephen Graham Jones's collection. Understanding, one visit. */
    PEOPLE_LIGHTS("people_lights",Kind.ONE_SHOT,160,new BoundingBox(-16,-3,-31,17,15,0),literaryEntry()),
    /** 0.4.257, A Place for You, the first slice (PlaceForYou, PlaceForYouSite): a private evening in Mara's house, which has prepared for the reader's return; the owner's own proposal. Understanding, one visit, built whole. */
    PLACE_FOR_YOU("place_for_you",Kind.ONE_SHOT,165,new BoundingBox(-18,-3,-34,18,18,0),literaryEntry()),
    /** 0.4.261, the rest rooms (QuietRooms, docs/REST_ROOMS_0_4_261.md): the quiet room's siblings, each built whole. The reading room, round a cold hearth. */
    QUIET_READING("quiet_reading", Kind.GRAY, 166, new BoundingBox(-6,-1,-16,6,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-16),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The boot room: long and low, coats on the hooks, the sink and the machine. */
    QUIET_BOOTROOM("quiet_bootroom", Kind.GRAY, 167, new BoundingBox(-4,-1,-20,4,5,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(-2,0,-20),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The garden room: frosted panes and plants on the sill, cane chairs round a table; the way on in its west wall. */
    QUIET_SUNROOM("quiet_sunroom", Kind.GRAY, 168, new BoundingBox(-8,-1,-13,8,7,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(-8,0,-6),Direction.EAST,LabyrinthData.DEALT))),
    /** The spare room under the eaves, the bed made up. */
    QUIET_SPARE("quiet_spare", Kind.GRAY, 169, new BoundingBox(-5,-1,-12,5,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(3,0,-11),Direction.SOUTH,LabyrinthData.DEALT))),
    /** 0.4.264, Johnny Truant's places (TruantPlaces, docs/TRUANT_0_4_264.md), each built whole. The held door: a service passage, a heavy door on a closer. */
    TRUANT_DOOR("truant_door", Kind.GRAY, 170, new BoundingBox(-3,-1,-24,3,4,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-24),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The crossing: a ledge along a shaft wall, no rail. */
    TRUANT_LEDGE("truant_ledge", Kind.GRAY, 171, new BoundingBox(-5,-9,-16,5,4,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-16),Direction.SOUTH,LabyrinthData.DEALT))),
    /** The lamp: a low corridor round a corner to his alcove. */
    TRUANT_LAMP("truant_lamp", Kind.GRAY, 172, new BoundingBox(-12,-1,-16,2,3,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(-10,0,-16),Direction.SOUTH,LabyrinthData.DEALT))),
    /**
     * 0.4.274, Second Person, the first slice (SecondPerson, SecondPersonSite, docs/SECOND_PERSON_0_4_274.md): a street and the flat above a
     * closed launderette, where somebody moves in who cannot see the reader. Late summer, autumn and a last morning. Understanding, one
     * story over several visits, built whole, a copy for each reader.
     */
    SECOND_PERSON("second_person",Kind.ONE_SHOT,173,new BoundingBox(-17,-3,-31,17,12,0),literaryEntry()),
    RETURN_AIR("return_air",Kind.GRAY,179,new BoundingBox(-6,-1,-33,6,4,0),grayDoors(0,0,-33)),
    SETTLING_CELLAR("settling_cellar",Kind.GRAY,180,new BoundingBox(-6,-5,-36,6,4,0),grayDoors(0,0,-36)),
    /**
     * 0.4.306, the Tension Room (TensionRooms, docs/TENSION_ROOM_0_4_306.md): a low utility loft, one cable from an entry wheel over a
     * counterweight's shaft, through a cleat, to a far wheel and the pin that holds a platform down in front of the far door. An early
     * puzzle room, family TENSION.
     */
    TENSION_ROOM("tension_room",Kind.GRAY,181,new BoundingBox(-6,-5,-16,6,5,0),grayDoors(0,0,-15)),
    /** 0.4.306, the Sphinx's first crossing (Sphinx, docs/SPHINX_0_4_306.md): a tiled vestibule and a hall of three latched doors; a rare detour, the way onward always open past the hall. */
    SPHINX_VESTIBULE("sphinx_vestibule",Kind.GRAY,182,new BoundingBox(-12,-1,-22,12,18,0),grayDoors(10,0,-21)),
    /** 0.4.307, Johnny Truant's service halls (TruantService, docs/TRUANT_SERVICE_0_4_307.md): the chalk junction, the steam elbow and the service lift, from the stone halls. */
    TRUANT_CHALK("truant_chalk",Kind.GRAY,183,new BoundingBox(-8,-5,-20,8,4,0),grayDoors(-5,0,-19)),
    TRUANT_KNOCK("truant_knock",Kind.GRAY,184,new BoundingBox(-9,-2,-22,3,4,0),grayDoors(-6,0,-21)),
    TRUANT_LIFT("truant_lift",Kind.GRAY,185,new BoundingBox(-6,-1,-17,6,13,0),grayDoors(0,0,-16)),
    /** 0.4.309, the House's yard, first connector (YardRooms, docs/YARD_TURNAROUND_0_4_309.md): the gravel turn-around behind the back door, outside. */
    YARD_TURNAROUND("yard_turnaround",Kind.GRAY,186,new BoundingBox(-20,-4,-40,48,14,4),grayDoors(-7,0,-34)),
    /** 0.4.310, A Deer in the Road (DeerRoad, docs/DEER_ROAD_0_4_310.md): a county road at night, a doe, and the bent-neck woman twice. Outside, shared, after the tunnel. */
    DEER_ROAD("deer_road",Kind.ONE_SHOT,187,new BoundingBox(-24,-5,-94,14,15,0),literaryEntry()),
    TENSION_ROOMS("tension_rooms",Kind.GRAY,188,new BoundingBox(-6,-5,-40,6,5,0),grayDoors(0,0,-39)),
    YARD_DITCH("yard_ditch",Kind.GRAY,189,new BoundingBox(-7,-2,-36,7,10,0),grayDoors(0,0,-35)),
    YARD_CLOTHESLINE("yard_clothesline",Kind.GRAY,190,new BoundingBox(-7,-2,-36,7,10,0),grayDoors(0,0,-35)),
    YARD_SURVEY("yard_survey",Kind.GRAY,191,new BoundingBox(-16,-3,-43,16,10,0),grayDoors(8,0,-42)),
    YARD_FLOOD("yard_flood",Kind.GRAY,192,new BoundingBox(-8,-3,-39,8,10,0),grayDoors(0,0,-38)),
    YARD_REUNION("yard_reunion",Kind.GRAY,193,new BoundingBox(-20,-4,-40,48,14,4),List.of(
        new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
        new DoorSpec("arrival_left",new BlockPos(-6,0,1),Direction.SOUTH,LabyrinthData.RETURN),
        new DoorSpec("arrival_right",new BlockPos(6,0,1),Direction.SOUTH,LabyrinthData.RETURN),
        new DoorSpec("exit",new BlockPos(-7,0,-34),Direction.NORTH,LabyrinthData.DEALT))),
    /** The far end of the impossible hallway, in the manor itself. */
    HALLWAY_END("hallway_end", Kind.HALLWAY, -1, null, List.of()),
    TRUANT_SHED("truant_shed",Kind.GRAY,194,new BoundingBox(-10,-3,-35,10,12,0),grayDoors(0,0,-34)),
    TRUANT_LANTERN("truant_lantern",Kind.GRAY,195,new BoundingBox(-10,-4,-38,12,14,0),grayDoors(0,0,-37)),
    TRUANT_STOCK_GATE("truant_stock_gate",Kind.GRAY,196,new BoundingBox(-10,-3,-39,10,12,0),grayDoors(0,0,-38));

    /** What a place is to the dealer. */
    public enum Kind {
        /** Connective: dealt freely, never counted as a vignette. */
        GRAY,
        /** Private ordinary moments; never a story, source or random gray deal. */
        INTERLUDE,
        /** A vignette that is finished once and never dealt again. */
        ONE_SHOT,
        /** A vignette that keeps being dealt. */
        RECURRING,
        /**
         * A vignette played one beat per visit, resuming where it was left,
         * until its last beat finishes it. Its saved state keeps the visit
         * it is on under {@code "Visit"} (0 before the first).
         */
        MULTI_VISIT,
        /** Not a carved place at all. */
        HALLWAY
    }

    private static List<DoorSpec> grayDoors(int x,int y,int z){return List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("far",new BlockPos(x,y,z),Direction.SOUTH,LabyrinthData.DEALT));}
    private static List<DoorSpec> threeDoors(int z,int... xs){var doors=new java.util.ArrayList<DoorSpec>();doors.add(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN));for(int i=0;i<xs.length;i++)doors.add(new DoorSpec("door_"+i,new BlockPos(xs[i],0,z),Direction.SOUTH,LabyrinthData.DEALT));return List.copyOf(doors);}
    private static List<DoorSpec> countingDoors(){var doors=new java.util.ArrayList<DoorSpec>();doors.add(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN));for(int i=1;i<=7;i++)doors.add(new DoorSpec("landing_"+i,new BlockPos(6,i*2,-3-i*6),Direction.WEST,LabyrinthData.DEALT));return List.copyOf(doors);}

    private static List<DoorSpec> literaryEntry(){return List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN));}

    /**
     * A door built into a place. {@code rel} is its lower half relative to
     * the place's base; {@code facing} is the way it faces: towards the side
     * it is approached from.
     */
    public record DoorSpec(String name, BlockPos rel, Direction facing, String destination) {
    }

    /**
     * How a place is made (0.4.226). The one property every generic pass reads: polish and its dressing, composition
     * ({@link SceneCraft}), the scene reviews, the playtest repairs, the vignette architecture and its shells and exteriors. Each
     * of those used to keep its own list of rooms to leave alone, and a room missing from one list was altered by that pass.
     */
    public enum Made {
        /**
         * Built whole by its own builder: walls, floors, furniture, lights, grounds and papers. No generic pass touches it. A
         * place that does not say otherwise is made this way, so a new room is protected unless it asks for the passes.
         */
        WHOLE,
        /** Built, then dressed by the generic passes: the older halls, hazards and scenes that rely on them for their lights and furniture. */
        DRESSED
    }

    private final String id;
    private final Kind kind;
    private final int slot;
    @Nullable
    private final BoundingBox room;
    private final List<DoorSpec> doors;
    private final Made made;

    LabyrinthPlace(String id, Kind kind, int slot, @Nullable BoundingBox room, List<DoorSpec> doors) {
        this(id, kind, slot, room, doors, Made.WHOLE);
    }

    LabyrinthPlace(String id, Kind kind, int slot, @Nullable BoundingBox room, List<DoorSpec> doors, Made made) {
        this.id = id;
        this.kind = kind;
        this.slot = slot;
        this.room = room;
        this.doors = doors;
        this.made = made;
    }

    /** How the place is made: whole by its own builder, or dressed by the generic passes afterwards. */
    public Made made() {
        return made;
    }

    /**
     * Built whole by its own builder ({@link Made#WHOLE}): no generic furnishing, lighting, shell, composition, review or repair
     * pass may change it. Every such pass asks this, and nothing else, before touching a room.
     */
    public boolean authored() {
        return made == Made.WHOLE;
    }

    public String id() {
        return id;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isVignette() {
        return kind == Kind.ONE_SHOT || kind == Kind.RECURRING || kind == Kind.MULTI_VISIT;
    }

    public boolean isOneShot() {
        return kind == Kind.ONE_SHOT;
    }

    public boolean isMultiVisit() {
        return kind == Kind.MULTI_VISIT;
    }

    /** A vignette that is finished at some point and then never dealt again: a one-shot or a multi-visit. */
    public boolean isFinishable() {
        return kind == Kind.ONE_SHOT || kind == Kind.MULTI_VISIT;
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

    private List<DoorSpec> withOnward;

    /** The authored doors, and the scene's fitted way on into the halls when it has one ({@link OnwardDoors}). */
    public List<DoorSpec> doors() {
        if (withOnward == null) {
            DoorSpec onward = OnwardDoors.doorSpec(this);
            if (onward == null) {
                withOnward = doors;
            } else {
                List<DoorSpec> all = new java.util.ArrayList<>(doors);
                all.add(onward);
                withOnward = List.copyOf(all);
            }
        }
        return withOnward;
    }

    public String doorId(DoorSpec spec) {
        return id + "/" + spec.name();
    }

    public String entryDoorId() {
        return id + "/entry";
    }

    /** Every authored return, including a service exit, belongs to this visit. */
    public boolean isReturnDoor(String doorId) {
        String plain = SceneInstances.plain(doorId);
        return doors.stream().anyMatch(d -> (id + "/" + d.name()).equals(plain)
                && LabyrinthData.RETURN.equals(d.destination()));
    }

    /** How often the dealer picks this gray place relative to the others: the plain gray most, the loops less. */
    public int grayWeight() {
        if (this == JUNCTION || this == GRAY_CORRIDOR) {
            return 3;
        }
        // The camp should feel found, not scheduled.
        return this == EXPLORER_CAMP ? 1 : 1;
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
        if ("mother".equals(id) || "mother_of_lost_things".equals(id)) return MOTHER_DEN;
        for (LabyrinthPlace place : values()) {
            if (place.id.equals(id)) {
                return place;
            }
        }
        return null;
    }
}
