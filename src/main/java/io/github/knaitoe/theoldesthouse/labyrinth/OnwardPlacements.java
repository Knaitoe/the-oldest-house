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
        r.put(WINCHESTER,22,7,-77,SOUTH,OnwardRegistry.PANEL); // 0.4.201: the back wall of the last room, upstairs
        r.put(CHILD_ROOM,11,-4,-10,WEST,OnwardRegistry.CELLAR); // from the brick basement
        r.put(CRIMSON_HALL,0,0,-43,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(BLY_ROUTE,0,0,-53,SOUTH,OnwardRegistry.PANEL); // beyond the conservatory pond
        r.put(ELK_FAN,4,0,-28,SOUTH,()->Blocks.OAK_DOOR);
        r.put(MAPPING_INTERIOR,7,2,-28,NORTH,HouseReadRegistry.SCREEN);
        r.put(CONFESSION,0,0,-33,SOUTH,()->Blocks.DARK_OAK_DOOR);
        r.put(DEVILS_ROCK,-13,0,-24,EAST,()->Blocks.BIRCH_DOOR); // 0.4.201: Tommy's closet, "open just a crack"
        r.put(WHEEL,-3,0,-63,SOUTH,OnwardRegistry.PANEL);
        r.put(GHOSTS_SET,-2,0,-26,SOUTH,OnwardRegistry.PANEL); // 0.4.220: the back door, clear of the dining window
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
        r.put(WAY_OUT,0,0,-76,SOUTH,OnwardRegistry.PANEL); // the cottage's front door: home, which is the House
        r.put(WHISTLERS,0,0,LookoutTower.ONWARD_Z,SOUTH,OnwardRegistry.WEATHERED,()->LookoutRegistry.material("timber")); // 0.4.202: a trail door in the treeline behind the tower
        r.put(OWN_LIGHT,OwnLightSite.ONWARD.getX(),0,OwnLightSite.ONWARD.getZ(),SOUTH,()->Blocks.DARK_OAK_DOOR); // 0.4.213: the far room's door
        r.put(POLYBIUS,PolybiusSite.EXIT_X,0,PolybiusSite.EXIT_Z,SOUTH,OnwardRegistry.PANEL); // 0.4.217: the arcade's fire door, back left
        r.put(REPLACEMENT,ReplacementSite.EXIT_X,0,ReplacementSite.EXIT_Z,SOUTH,OnwardRegistry.PANEL); // 0.4.218: the open house's kitchen door
        r.put(LINE,LineSite.ONWARD.getX(),0,LineSite.ONWARD.getZ(),SOUTH,()->Blocks.DARK_OAK_DOOR); // 0.4.212: the far wall behind the pack
        r.put(STILLNESS,StillnessSite.ONWARD.getX(),0,StillnessSite.ONWARD.getZ(),SOUTH,()->Blocks.DARK_OAK_DOOR); // 0.4.211: the far door at the end of the low hall
        r.put(STOREROOM,StoreroomSite.ONWARD.getX(),0,StoreroomSite.ONWARD.getZ(),WEST,()->Blocks.DARK_OAK_DOOR); // 0.4.210: a side door in the dark hall before the storeroom
        r.put(CLOSING_HALL,ClosingHallSite.ONWARD.getX(),0,ClosingHallSite.ONWARD.getZ(),EAST,()->Blocks.DARK_OAK_DOOR); // 0.4.209: out of the room past the squeeze
        r.put(PITT,4,0,0,NORTH,()->Blocks.DARK_OAK_DOOR); // 0.4.207: a door in the staging ledge's rock
        r.put(DEAD_AIR,4,0,0,NORTH,()->Blocks.DARK_OAK_DOOR); // 0.4.206: a second door in the basement's paneling
        r.put(LEFT_RIGHT,LeftRightRoad.EXIT.getX(),0,LeftRightRoad.EXIT.getZ(),WEST,OnwardRegistry.SERVICE); // 0.4.205: the maintenance shed beside the toll
        r.put(POLAROID_TRAIL,0,PolaroidSite.PLATEAU,PolaroidSite.EXIT_Z,SOUTH,OnwardRegistry.WEATHERED,()->PolaroidRegistry.material("siding")); // 0.4.204: the garden shed at the far end of the pines
        r.put(WORK_PRINT,WorkPrintShop.EXIT_X,0,WorkPrintShop.EXIT_Z,SOUTH,OnwardRegistry.PANEL); // 0.4.203: the set's back wall, the door the crew used
        r.put(FEET_UP,0,FeetUpRooms.ATTIC,FeetUpRooms.Z0-1,SOUTH,OnwardRegistry.PANEL); // the attic's north gable, reached once the ladder is down
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
