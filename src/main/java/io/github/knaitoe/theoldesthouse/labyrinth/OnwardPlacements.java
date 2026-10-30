package io.github.knaitoe.theoldesthouse.labyrinth;

import static io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace.*;
import static net.minecraft.core.Direction.*;

import net.minecraft.world.level.block.Blocks;

/**
 * Where each scene's way on stands, chosen per environment (owner, October 28). Positions are relative to the
 * scene's base, the door's lower half; the facing is the side it is approached from. Rooms take a door in their
 * own joinery in a far or side wall; outdoor scenes take one in a bank, a hut or a shanty, or a door standing in
 * its own frame where the ground runs out.
 */
final class OnwardPlacements {
    private OnwardPlacements(){}
    static void register(OnwardDoors.Registrar r){
        // Houses and rooms.
        r.put(HIDE_AND_CLAP,0,0,-13,SOUTH,()->Blocks.BIRCH_DOOR);
        r.put(MODEL_HOME,7,0,-19,SOUTH,OnwardRegistry.PANEL);
        r.put(HARRIGAN,8,0,-2,WEST,()->Blocks.DARK_OAK_DOOR); // beside his chair in the study, in the foyer once it is a funeral home
        r.put(MOTHER_DEN,0,0,-25,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(KAREN_ROOM,0,0,-18,SOUTH,()->Blocks.OAK_DOOR);
        r.put(SEANCE,4,0,-32,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(WALLPAPER_NURSERY,0,0,-28,SOUTH,OnwardRegistry.PANEL);
        r.put(HILL_NURSERY,0,0,-43,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(MINIATURES,14,0,-27,WEST,()->Blocks.DARK_OAK_DOOR);
        r.put(USHER,3,0,-41,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(MASQUE,-6,0,-77,SOUTH,()->Blocks.DARK_OAK_DOOR); // out of the black room
        r.put(WINCHESTER,29,0,-77,SOUTH,OnwardRegistry.PANEL);
        r.put(CHILD_ROOM,11,-4,-10,WEST,OnwardRegistry.CELLAR); // from the brick basement
        r.put(CRIMSON_HALL,0,0,-43,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(BLY_ROUTE,0,0,-53,SOUTH,OnwardRegistry.PANEL); // beyond the conservatory pond
        r.put(ELK_FAN,4,0,-28,SOUTH,()->Blocks.OAK_DOOR);
        r.put(MAPPING_INTERIOR,0,0,-36,SOUTH,HouseReadRegistry.SCREEN);
        r.put(CONFESSION,0,0,-33,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(DEVILS_ROCK,0,0,-42,SOUTH,OnwardRegistry.CABIN);
        r.put(WHEEL,-3,0,-63,SOUTH,OnwardRegistry.PANEL);
        r.put(GHOSTS_SET,0,0,-26,SOUTH,OnwardRegistry.PANEL);
        r.put(NIGHTCLUB,-9,0,-49,SOUTH,OnwardRegistry.SERVICE);
        r.put(FLOORBOARDS,0,0,-10,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(THREE_FINGERS,-9,0,-7,EAST,OnwardRegistry.CABIN);
        r.put(END_WORLD_CABIN,-11,0,-35,SOUTH,OnwardRegistry.CABIN);
        r.put(GOATMAN,0,0,-80,SOUTH,OnwardRegistry.WEATHERED);
        // The uncanny rooms.
        r.put(PAINTED_INTERIOR,7,0,-28,SOUTH,OnwardRegistry.PANEL);
        r.put(TUNNEL_PERIPHERY,-2,0,-145,SOUTH,OnwardRegistry.SERVICE);
        r.put(MISSING_SLEEP,0,0,-29,SOUTH,OnwardRegistry.PANEL);
        r.put(LAYERED_MAN,-4,0,-30,SOUTH,()->Blocks.OAK_DOOR);
        r.put(MOTION_LIGHT,0,0,-25,SOUTH,OnwardRegistry.SERVICE);
        r.put(SECOND_WAITRESS,0,0,-32,SOUTH,OnwardRegistry.SERVICE);
        r.put(DARE_IN_THE_DARK,0,0,DareRooms.EXIT_Z,SOUTH,DareRegistry.STAIR_DOOR); // the stairwell at the end of the third-floor hall
        // The novel sites.
        r.put(WHALE,6,0,-32,SOUTH,NovelRegistry.WARD_DOOR);
        r.put(HOSPITAL,4,0,-23,SOUTH,NovelRegistry.NIGHT_WARD_DOOR);
        r.put(BARN_WELL,-15,0,-11,EAST,OnwardRegistry.WEATHERED); // the back of the tool shed
        r.put(PLAIN,-23,0,-63,EAST,OnwardRegistry.WEATHERED);
        // Caves and camps.
        r.put(TED_CAVER,6,0,-7,WEST,OnwardRegistry.CELLAR);
        r.put(HOLLOWAY_CAMP,4,0,-70,SOUTH,OnwardRegistry.CELLAR); // beside the service door, which still leads back
        r.put(PRESERVED_CAVE,-2,0,-47,SOUTH,OnwardRegistry.CELLAR);
        // Outdoors: a bank, a hut, a shanty, or a door standing at the end of the ground in its own frame.
        r.put(ELK_LOT,-15,0,-30,SOUTH,OnwardRegistry.SERVICE);
        r.put(SHALLOWS,17,0,-10,WEST,OnwardRegistry.WEATHERED);
        r.put(PHONE_CANOE,-23,0,0,NORTH,OnwardRegistry.CELLAR,()->Blocks.STRIPPED_SPRUCE_LOG); // a root cellar in the bank
        r.put(HOLY_RABBIT,0,0,-125,SOUTH,OnwardRegistry.WEATHERED,()->Blocks.SPRUCE_LOG); // where the trail runs out
        r.put(COSTUME_NIGHT,0,0,-74,SOUTH,OnwardRegistry.WEATHERED,()->Blocks.STRIPPED_SPRUCE_LOG); // the far side of the park
        r.put(MOVIE_NIGHT,3,0,-87,EAST,OnwardRegistry.PANEL); // the ticket hut's side door
        r.put(WINTER_LAKE,13,0,-37,SOUTH,OnwardRegistry.CABIN); // the back of the shanty, out on the ice
        r.put(CAMP_BLOOD,0,0,-103,SOUTH,OnwardRegistry.WEATHERED,()->Blocks.STRIPPED_DARK_OAK_LOG); // the camp gate at the end of the road
        r.put(LAKE_HOUSE,-6,0,-2,EAST,OnwardRegistry.CABIN); // the boathouse
        r.put(DROWNED_TOWN,31,0,-22,WEST,OnwardRegistry.PANEL); // the first room of the motel
        // The crew's gate at the head of the valley, beside the account, leads on now instead of back.
        r.adopt(ELK_CARCASSES,"service");
    }
}
