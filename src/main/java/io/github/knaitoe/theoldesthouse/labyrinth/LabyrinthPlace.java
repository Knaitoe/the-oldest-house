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
    /** The first physical maze: branching halls, dead ends and a way onward. */
    GRAY_CORRIDOR("gray_corridor", Kind.GRAY, 1, new BoundingBox(-13, -1, -28, 13, 4, 0), List.of(
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
    /** Poltergeist: the model home, with the kid's-room window onto a yard (x -23..-9). See {@link ModelHome}. */
    MODEL_HOME("model_home", Kind.MULTI_VISIT, 8, new BoundingBox(-23, -1, -21, 9, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** Mr Harrigan's Phone: reading, the funeral, then the artifact that follows the player home. */
    HARRIGAN("harrigan", Kind.MULTI_VISIT, 9, new BoundingBox(-8, -1, -25, 8, 7, 1), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** A submerged gray corridor with deliberately spaced air chimneys. */
    FLOODED_PASSAGE("flooded_passage", Kind.GRAY, 10, new BoundingBox(-8, -3, -28, 8, 6, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A raised broken route: missing the span hurts, but a lower recovery path remains. */
    FRACTURED_WALKWAY("fractured_walkway", Kind.GRAY, 11, new BoundingBox(-4, -1, -28, 4, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 7, -28), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A broad corridor that audibly presses inward until only its center line remains. */
    COMPRESSION_PASSAGE("compression_passage", Kind.GRAY, 12, new BoundingBox(-3, -1, -28, 3, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A corridor whose far door refuses to become as close as it looks. */
    FALSE_DISTANCE("false_distance", Kind.GRAY, 13, new BoundingBox(-2, -1, -34, 2, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -34), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A dim stretch that consumes the light sources brought into it. */
    LIGHT_SINK("light_sink", Kind.GRAY, 14, new BoundingBox(-3, -1, -28, 3, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** One exit, three possible walls; it moves only when nobody sees it. */
    MOVING_THRESHOLD("moving_threshold", Kind.GRAY, 15, new BoundingBox(-8, -1, -15, 8, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-8, 0, -8), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(8, 0, -8), Direction.WEST, LabyrinthData.DEALT)
    )),
    /** Three identical exits: two return to the same room, one actually advances. */
    DUPLICATE_PASSAGE("duplicate_passage", Kind.GRAY, 16, new BoundingBox(-8, -1, -15, 8, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-8, 0, -8), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(8, 0, -8), Direction.WEST, LabyrinthData.DEALT)
    )),
    /** Sideways gravity drags the player toward a shallow recovery trench. */
    GRAVITY_DRIFT("gravity_drift", Kind.GRAY, 17, new BoundingBox(-5, -6, -28, 4, 6, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -28), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** An abandoned explorer camp: finite supplies and one real chance to recover. */
    EXPLORER_CAMP("explorer_camp", Kind.GRAY, 18, new BoundingBox(-6, -1, -15, 6, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** The Mother's den: a recurring anchor, never exhausted by completion. */
    MOTHER_DEN("mother_of_strays", Kind.RECURRING, 19, new BoundingBox(-10, -4, -25, 10, 13, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** A larger maze, with one pair of distant, identical bends sewn together. */
    FOLDED_MAZE("folded_maze", Kind.GRAY, 20, new BoundingBox(-19, -1, -66, 19, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -66), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-19, 0, -18), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(19, 0, -18), Direction.WEST, LabyrinthData.DEALT)
    )),
    /** Two fold pairs rotate the player's route as well as changing adjacency. */
    DEEP_MAZE("deep_maze", Kind.GRAY, 21, new BoundingBox(-25, -1, -76, 25, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -76), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-25, 0, -23), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(25, 0, -23), Direction.WEST, LabyrinthData.DEALT)
    )),
    /** The widest maze: six hidden crossings, more cycles and longer backtracking. */
    ABYSS_MAZE("abyss_maze", Kind.GRAY, 22, new BoundingBox(-37, -1, -86, 37, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("north", new BlockPos(0, 0, -86), Direction.SOUTH, LabyrinthData.DEALT),
            new DoorSpec("west", new BlockPos(-37, 0, -28), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(37, 0, -28), Direction.WEST, LabyrinthData.DEALT)
    )),
    /** Ordinary connected hallways. Doorways belong at destinations, rather than filling every wall. */
    STRAIGHT_HALL("straight_hall", Kind.GRAY, 23, new BoundingBox(-7, -1, -36, 7, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -36), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    BENT_HALL("bent_hall", Kind.GRAY, 24, new BoundingBox(-17, -1, -33, 7, 4, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(-15, 0, -33), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    CROSS_HALL("cross_hall", Kind.GRAY, 25, new BoundingBox(-15, -1, -32, 15, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("west", new BlockPos(-15, 0, -15), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(15, 0, -15), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("far", new BlockPos(0, 0, -32), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A quiet waiting room, with chairs and a small finite cache. */
    QUIET_ROOM("quiet_room", Kind.GRAY, 26, new BoundingBox(-6, -1, -15, 6, 5, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, 0, -15), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** Indian Lake: dive for the essays, then return for the church. */
    DROWNED_TOWN("drowned_town", Kind.MULTI_VISIT, 27, new BoundingBox(-29, -13, -64, 29, 8, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** Indian Lake: a congregation kept fresh by the lake. */
    PRESERVED_CAVE("preserved_cave", Kind.MULTI_VISIT, 28, new BoundingBox(-15, -3, -47, 15, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** A personal recollection, available only after the shore hunt. */
    SHALLOWS("shallows", Kind.ONE_SHOT, 29, new BoundingBox(-18, -4, -36, 18, 10, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN)
    )),
    /** The recording that survives its recorder. Personal, like the Shallows recollection. */
    PHONE_CANOE("phone_canoe", Kind.ONE_SHOT, 30, new BoundingBox(-26,-4,-60,26,10,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)
    )),
    /** Anansi's Goatman: the latecomer's path, one extra child, and a closed trailer door. */
    GOATMAN("goatman",Kind.ONE_SHOT,31,new BoundingBox(-23,-1,-82,23,13,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)
    )),
    /** A worked opening, a tight squeeze, and the line that leads back. */
    TED_CAVER("ted_caver",Kind.MULTI_VISIT,32,new BoundingBox(-9,-4,-60,9,8,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN)
    )),
    ZAMPANO_COURTYARD("zampano_courtyard",Kind.RECURRING,33,new BoundingBox(-15,-1,-41,15,16,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("apartment_west",new BlockPos(-12,0,-8),Direction.EAST,LabyrinthData.DEALT),new DoorSpec("apartment_east",new BlockPos(12,0,-8),Direction.WEST,LabyrinthData.DEALT))),
    WHALE("whale",Kind.MULTI_VISIT,34,new BoundingBox(-14,-1,-33,14,17,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    BARN_WELL("barn_well",Kind.ONE_SHOT,35,new BoundingBox(-17,-13,-39,17,8,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    PLAIN("plain",Kind.ONE_SHOT,36,new BoundingBox(-30,-1,-65,30,16,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    HOSPITAL("hospital",Kind.MULTI_VISIT,37,new BoundingBox(-10,-1,-24,10,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    KAREN_ROOM("karen_room",Kind.RECURRING,38,new BoundingBox(-10,-1,-19,10,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    HOLLOWAY_CAMP("holloway_camp",Kind.MULTI_VISIT,39,new BoundingBox(-11,-1,-71,11,7,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("service",new BlockPos(-7,0,-70),Direction.NORTH,LabyrinthData.RETURN))),
    SEANCE("seance",Kind.ONE_SHOT,40,new BoundingBox(-11,-1,-32,11,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    WALLPAPER_NURSERY("wallpaper_nursery",Kind.MULTI_VISIT,41,new BoundingBox(-10,-1,-28,10,7,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    BLIND_STRETCH("blind_stretch",Kind.GRAY,42,new BoundingBox(-13,-1,-42,13,4,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("far",new BlockPos(0,0,-41),Direction.SOUTH,LabyrinthData.DEALT))),
    HOTEL("hotel",Kind.RECURRING,43,new BoundingBox(-29,-5,-68,29,14,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),new DoorSpec("numbers",new BlockPos(-14,0,-65),Direction.SOUTH,"place:hotel_hallway"),new DoorSpec("grounds",new BlockPos(28,0,-37),Direction.WEST,"place:hotel_grounds"))),
    HOTEL_GROUNDS("hotel_grounds",Kind.GRAY,44,new BoundingBox(-28,-3,-68,28,14,0),List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN))),
    HILL_NURSERY("hill_nursery",Kind.ONE_SHOT,45,new BoundingBox(-15,-5,-44,15,12,0),literaryEntry()),
    MINIATURES("miniatures",Kind.MULTI_VISIT,46,new BoundingBox(-15,-1,-31,15,9,0),literaryEntry()),
    MASQUE("masque",Kind.ONE_SHOT,47,new BoundingBox(-12,-1,-78,12,11,0),literaryEntry()),
    USHER("usher",Kind.MULTI_VISIT,48,new BoundingBox(-18,-4,-42,18,13,0),literaryEntry()),
    WINCHESTER("winchester",Kind.ONE_SHOT,49,new BoundingBox(-31,-1,-78,31,17,0),literaryEntry()),
    CHILD_ROOM("child_room",Kind.ONE_SHOT,50,new BoundingBox(-12,-3,-25,12,8,0),literaryEntry()),
    CRIMSON_HALL("crimson_hall",Kind.MULTI_VISIT,51,new BoundingBox(-19,-1,-44,19,15,0),literaryEntry()),
    BLY_ROUTE("bly_route",Kind.RECURRING,52,new BoundingBox(-13,-5,-54,13,10,0),literaryEntry()),
    ELK_LOT("elk_lot",Kind.ONE_SHOT,53,new BoundingBox(-35,-3,-113,35,17,0),literaryEntry()),
    ELK_FAN("elk_fan",Kind.MULTI_VISIT,54,new BoundingBox(-12,-1,-29,12,11,0),literaryEntry()),
    MAPPING_INTERIOR("mapping_interior",Kind.MULTI_VISIT,55,new BoundingBox(-22,-6,-79,22,15,0),literaryEntry()),
    HOLY_RABBIT("holy_rabbit",Kind.ONE_SHOT,56,new BoundingBox(-35,-5,-126,35,26,0),literaryEntry()),
    CONFESSION("confession",Kind.MULTI_VISIT,57,new BoundingBox(-13,-1,-34,13,11,0),literaryEntry()),
    /** Two stages (0.4.50): the yacht moored under the bluff, then the stream woods and the cave of carcasses; the crew's gate leads back. */
    ELK_CARCASSES("elk_carcasses",Kind.ONE_SHOT,58,new BoundingBox(-64,-9,-250,64,24,0),List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("service",new BlockPos(44,11,-247),Direction.NORTH,LabyrinthData.RETURN))),
    COSTUME_NIGHT("costume_night",Kind.ONE_SHOT,59,new BoundingBox(-33,-6,-77,33,18,0),literaryEntry()),
    MOVIE_NIGHT("movie_night",Kind.ONE_SHOT,60,new BoundingBox(-35,-12,-93,35,18,0),literaryEntry()),
    WINTER_LAKE("winter_lake",Kind.ONE_SHOT,61,new BoundingBox(-35,-12,-87,35,18,0),literaryEntry()),
    CAMP_BLOOD("camp_blood",Kind.ONE_SHOT,62,new BoundingBox(-33,-3,-104,33,19,0),literaryEntry()),
    DEVILS_ROCK("devils_rock",Kind.MULTI_VISIT,63,new BoundingBox(-17,-1,-43,17,11,0),literaryEntry()),
    WHEEL("wheel",Kind.MULTI_VISIT,64,new BoundingBox(-27,-1,-65,27,13,0),literaryEntry()),
    GHOSTS_SET("ghosts_set",Kind.ONE_SHOT,65,new BoundingBox(-21,-1,-47,21,12,0),literaryEntry()),
    END_WORLD_CABIN("end_world_cabin",Kind.ONE_SHOT,66,new BoundingBox(-31,-9,-81,31,19,0),literaryEntry()),
    FAMILY_COPY("family_copy",Kind.MULTI_VISIT,67,new BoundingBox(-34,-17,-71,34,33,0),literaryEntry()),
    OLD_CABIN("old_cabin",Kind.RECURRING,68,new BoundingBox(-12,-9,-29,12,17,0),literaryEntry()),
    /** The deep tier turns to stone: a long vaulted gallery with a side way. */
    STONE_GALLERY("stone_gallery", Kind.GRAY, 69, new BoundingBox(-5, -1, -42, 5, 7, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("side", new BlockPos(4, 0, -21), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("far", new BlockPos(0, 0, -42), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A pillared stone crossing around a dry fountain. */
    STONE_CROSSING("stone_crossing", Kind.GRAY, 70, new BoundingBox(-12, -1, -26, 12, 8, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("west", new BlockPos(-12, 0, -13), Direction.EAST, LabyrinthData.DEALT),
            new DoorSpec("east", new BlockPos(12, 0, -13), Direction.WEST, LabyrinthData.DEALT),
            new DoorSpec("far", new BlockPos(0, 0, -26), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** A stone stair that physically descends before the way goes on. */
    STONE_DESCENT("stone_descent", Kind.GRAY, 71, new BoundingBox(-4, -5, -34, 4, 6, 0), List.of(
            new DoorSpec("entry", new BlockPos(0, 0, 1), Direction.SOUTH, LabyrinthData.RETURN),
            new DoorSpec("far", new BlockPos(0, -4, -34), Direction.SOUTH, LabyrinthData.DEALT)
    )),
    /** Familiar additions keep their own physical addresses; existing halls are never rebuilt. */
    ALCOVE_HALL("alcove_hall", Kind.GRAY, 72, new BoundingBox(-8,-1,-42,8,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(-8,0,-17),Direction.EAST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-42),Direction.SOUTH,LabyrinthData.DEALT))),
    OFFSET_HALL("offset_hall", Kind.GRAY, 73, new BoundingBox(-5,-1,-40,10,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(4,0,-40),Direction.SOUTH,LabyrinthData.DEALT))),
    SERVICE_LANDING("service_landing", Kind.GRAY, 74, new BoundingBox(-11,-1,-31,11,6,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(-11,0,-16),Direction.EAST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-31),Direction.SOUTH,LabyrinthData.DEALT))),
    STONE_ARCADE("stone_arcade", Kind.GRAY, 75, new BoundingBox(-6,-1,-40,6,8,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(0,0,-40),Direction.SOUTH,LabyrinthData.DEALT))),
    STONE_BEND("stone_bend", Kind.GRAY, 76, new BoundingBox(-12,-1,-36,6,7,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("far",new BlockPos(-9,0,-36),Direction.SOUTH,LabyrinthData.DEALT))),
    STONE_LANDING("stone_landing", Kind.GRAY, 77, new BoundingBox(-12,-1,-28,12,8,0), List.of(
            new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN),
            new DoorSpec("side",new BlockPos(12,0,-16),Direction.WEST,LabyrinthData.DEALT),
            new DoorSpec("far",new BlockPos(0,0,-28),Direction.SOUTH,LabyrinthData.DEALT))),
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
        /**
         * A vignette played one beat per visit, resuming where it was left,
         * until its last beat finishes it. Its saved state keeps the visit
         * it is on under {@code "Visit"} (0 before the first).
         */
        MULTI_VISIT,
        /** Not a carved place at all. */
        HALLWAY
    }

    private static List<DoorSpec> literaryEntry(){return List.of(new DoorSpec("entry",new BlockPos(0,0,1),Direction.SOUTH,LabyrinthData.RETURN));}

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
