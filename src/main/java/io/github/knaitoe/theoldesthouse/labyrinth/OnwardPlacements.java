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
        r.put(WHEEL,5,0,-5,WEST,OnwardRegistry.PANEL); // 0.4.241: in the hall, not Mother's room: she is left by her own door
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
        r.put(PEOPLE_LIGHTS,PeopleLightsSite.WAY_X,PeopleLightsSite.YARD,PeopleLightsSite.WAY_Z,NORTH,()->Blocks.SPRUCE_DOOR); // 0.4.253: a door at the back of the garage
        r.put(STATE_ROUTE,5,0,-3,NORTH,StateRouteRegistry.GATE); // 0.4.333: the padlocked gate in the chain-link fence
        r.put(GROWING_THINGS,GrowingThingsSite.ONWARD.getX(),0,GrowingThingsSite.ONWARD.getZ(),NORTH,()->Blocks.DARK_OAK_DOOR); // 0.4.350: a garage's side door along the alley
        r.put(LAST_CROSSING,LastCrossingSite.ONWARD.getX(),0,LastCrossingSite.ONWARD.getZ(),SOUTH,()->Blocks.SPRUCE_DOOR); // 0.4.351: the shelter's door on the far bank
        r.put(PINEHOOK,PinehookSite.ONWARD.getX(),0,PinehookSite.ONWARD.getZ(),WEST,()->Blocks.SPRUCE_DOOR); // 0.4.364: the pump house at the west end of the apron
        r.put(TALL_GRASS,TallGrassSite.GATE.getX(),0,TallGrassSite.GATE.getZ(),NORTH,()->Blocks.SPRUCE_DOOR); // 0.4.349: the field gate in the brush across the road
        r.put(THE_DRAWING,DrawingSite.HALL_DOOR.getX(),0,DrawingSite.HALL_DOOR.getZ(),SOUTH,()->Blocks.SPRUCE_DOOR); // 0.4.407: the meeting hall's door off the square
        r.put(KEPT,KeptSite.ONWARD.getX(),0,KeptSite.ONWARD.getZ(),SOUTH,OnwardRegistry.SERVICE); // 0.4.410: a service door in the hall's north wall
        r.put(LAST_ORDERS,LastOrdersSite.ONWARD.getX(),0,LastOrdersSite.ONWARD.getZ(),EAST,OnwardRegistry.PANEL); // 0.4.416: the street door in the west wall by the front
        r.put(PELL_HOUSE,PellSite.ONWARD.getX(),0,PellSite.ONWARD.getZ(),NORTH,OnwardRegistry.SERVICE); // 0.4.518: a service door in the substation's wall, west of the door the reader came by
        r.put(ONES_INSIDE,LodgingSite.ONWARD.getX(),0,LodgingSite.ONWARD.getZ(),SOUTH,OnwardRegistry.SERVICE); // 0.4.549: the scullery's back door
        r.put(BIKE_NIGHT,BikeNightSite.ONWARD.getX(),0,BikeNightSite.ONWARD.getZ(),NORTH,OnwardRegistry.SERVICE); // 0.4.464: a service door in the park's back wall, until the far gate is built
        r.put(BLACK_CITY,CitySite.ONWARD.getX(),0,CitySite.ONWARD.getZ(),SOUTH,()->Blocks.SPRUCE_DOOR); // 0.4.409: the mess hut's door, facing the camp
        r.put(CORNER_HOUSE,CornerSite.ONWARD.getX(),0,CornerSite.ONWARD.getZ(),SOUTH,()->Blocks.DARK_OAK_DOOR); // 0.4.408: a door across the cross street, facing up the main street
        r.put(MARSH_SUPPER,MarshSupperSite.GATE.getX(),0,MarshSupperSite.GATE.getZ(),WEST,()->Blocks.SPRUCE_DOOR); // 0.4.348: the gate onto the county road past the oak
        r.put(DEER_ROAD,-4,-2,-84,SOUTH,DeerRoadRegistry.GATE); // 0.4.310: the drainage gate at the head of the ditch
        r.put(BIKE_NIGHT,CalderHollow.DOOR_X,CalderGraves.level(CalderHollow.EDGE),CalderHollow.WALL+BikeNightFold.EXIT,SOUTH,OnwardRegistry.PANEL); // 0.4.508: the door in the wall across the lane behind the cemetery (0.4.521: the town's, past the fold)
        r.put(SECOND_PERSON,-6,0,0,NORTH,()->Blocks.DARK_OAK_DOOR); // 0.4.274: a neighbour's front door in the terrace, opened from the street
        r.put(PLACE_FOR_YOU,8,0,0,NORTH,()->Blocks.SPRUCE_DOOR); // 0.4.257: a second door in the garden wall, further along the road, opened from the road
        r.put(VACANT_POSSESSION,VacantSite.BACK_X,VacantSite.GROUND,VacantSite.BACK_Z,EAST,()->Blocks.SPRUCE_DOOR); // 0.4.228: the back door at the end of the downstairs hall
        r.put(LAST_SWIMMER,SwimmerSite.EXIT_X,0,SwimmerSite.EXIT_Z,EAST,OnwardRegistry.PANEL); // 0.4.227: the pool hall's fire door, which the attendant holds
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
