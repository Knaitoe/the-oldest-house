package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.properties.*;
/** Twenty-four append-only structures. Furnishings and originals are constructed once. */
public final class LiteraryRooms {
    public static final BlockPos SOURCE=new BlockPos(3,0,-4),HILL_KNOCK=new BlockPos(-10,-4,-36),HILL_WALL=new BlockPos(-14,2,-17),
        TABLE=new BlockPos(0,0,-18),CLOCK=MasqueSuite.FACE,COFFIN=new BlockPos(0,-3,-29),FAN=new BlockPos(0,7,-17),FAN_REACH=new BlockPos(0,4,-17),
        JOURNAL=new BlockPos(-8,1,-25),CAMERA=new BlockPos(0,0,-28),BOOTH=new BlockPos(10,0,-11),TV=new BlockPos(7,1,-24);
    /** The cabin (0.4.52): its front door, the two beds a reader wakes in (heads), and the shed door that is the only way out. */
    public static final BlockPos CABIN_DOOR=new BlockPos(0,0,-13),SHED_DOOR=new BlockPos(0,0,-4);
    public static final List<BlockPos> CABIN_BEDS=List.of(new BlockPos(-10,0,-34),new BlockPos(-6,0,-34));
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private LiteraryRooms(){}
    public static boolean isLiterary(LabyrinthPlace p){return p!=null&&(p.slot()>=45&&p.slot()<=68||p==LabyrinthPlace.THREE_FINGERS||p==LabyrinthPlace.LAKE_HOUSE||p==LabyrinthPlace.BORRASCA||UncannyRooms.isUncanny(p)||DareRooms.is(p)||FeetUpRooms.is(p)||p==LabyrinthPlace.WHISTLERS||p==LabyrinthPlace.WORK_PRINT||p==LabyrinthPlace.POLAROID_TRAIL||p==LabyrinthPlace.LEFT_RIGHT||p==LabyrinthPlace.DEAD_AIR||p==LabyrinthPlace.PITT||p==LabyrinthPlace.POLYBIUS||p==LabyrinthPlace.REPLACEMENT||p==LabyrinthPlace.LAST_SWIMMER||p==LabyrinthPlace.VACANT_POSSESSION||p==LabyrinthPlace.PEOPLE_LIGHTS||p==LabyrinthPlace.PLACE_FOR_YOU||p==LabyrinthPlace.SECOND_PERSON||p==LabyrinthPlace.DEER_ROAD||p==LabyrinthPlace.STATE_ROUTE||p==LabyrinthPlace.MARSH_SUPPER||p==LabyrinthPlace.THE_DRAWING||p==LabyrinthPlace.CORNER_HOUSE||p==LabyrinthPlace.BLACK_CITY||p==LabyrinthPlace.KEPT||p==LabyrinthPlace.TALL_GRASS||p==LabyrinthPlace.GROWING_THINGS||p==LabyrinthPlace.LAST_CROSSING||p==LabyrinthPlace.PINEHOOK||DarkHalls.horror(p));}
    public static boolean outside(LabyrinthPlace p){return switch(p){case SECOND_WAITRESS,PLACE_FOR_YOU,SECOND_PERSON,DEER_ROAD,STATE_ROUTE,MARSH_SUPPER,THE_DRAWING,CORNER_HOUSE,BLACK_CITY,TALL_GRASS,GROWING_THINGS,LAST_CROSSING,PINEHOOK,WHISTLERS,POLAROID_TRAIL,REPLACEMENT,LEFT_RIGHT,DEAD_AIR,PITT,ELK_LOT,MAPPING_INTERIOR,DEVILS_ROCK,HOLY_RABBIT,ELK_CARCASSES,COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE,CAMP_BLOOD,END_WORLD_CABIN,FAMILY_COPY,OLD_CABIN,LAKE_HOUSE,BORRASCA->true;default->false;};}
    public static BlockPos source(LabyrinthPlace p){if(p==LabyrinthPlace.POLYBIUS)return PolybiusSite.NOTICE;if(p==LabyrinthPlace.REPLACEMENT)return ReplacementSite.POSTER;if(p==LabyrinthPlace.LAST_SWIMMER)return SwimmerSite.CHECKLIST;if(p==LabyrinthPlace.VACANT_POSSESSION)return VacantSite.LIST;if(p==LabyrinthPlace.PEOPLE_LIGHTS)return PeopleLightsSite.OFFER_AT;if(p==LabyrinthPlace.PLACE_FOR_YOU)return PlaceForYouSite.CARD;if(p==LabyrinthPlace.SECOND_PERSON)return SecondPersonSite.BOOK;if(p==LabyrinthPlace.DEER_ROAD)return DeerRoadSite.NOTICE;if(p==LabyrinthPlace.STATE_ROUTE)return StateRouteSite.MAILBOX;if(p==LabyrinthPlace.MARSH_SUPPER)return MarshSupperSite.CARD;if(p==LabyrinthPlace.THE_DRAWING)return DrawingSite.NOTICE;if(p==LabyrinthPlace.CORNER_HOUSE)return CornerSite.NOTEBOOK;if(p==LabyrinthPlace.BLACK_CITY)return CitySite.LOGBOOK;if(p==LabyrinthPlace.KEPT)return KeptSite.PRINTER;if(p==LabyrinthPlace.TALL_GRASS)return TallGrassSite.NOTICE;if(p==LabyrinthPlace.GROWING_THINGS)return GrowingThingsSite.NOTE;if(p==LabyrinthPlace.LAST_CROSSING)return LastCrossingSite.SLATE;if(p==LabyrinthPlace.PINEHOOK)return PinehookSite.SOURCE;if(p==LabyrinthPlace.CLOSING_HALL)return ClosingHallSite.DRAWING;if(p==LabyrinthPlace.STOREROOM)return StoreroomSite.LIST;if(p==LabyrinthPlace.STILLNESS)return StillnessSite.LOG;if(p==LabyrinthPlace.LINE)return LineSite.TAG;if(p==LabyrinthPlace.OWN_LIGHT)return OwnLightSite.SCRATCH;if(UncannyRooms.isUncanny(p))return UncannyRooms.source(p);if(DareRooms.is(p))return DareRooms.NOTE;if(FeetUpRooms.is(p))return FeetUpRooms.RULES;if(p==LabyrinthPlace.WHISTLERS)return LookoutTower.SOURCE;if(p==LabyrinthPlace.WORK_PRINT)return WorkPrintShop.CARD;if(p==LabyrinthPlace.POLAROID_TRAIL)return PolaroidSite.TAG1;if(p==LabyrinthPlace.LEFT_RIGHT)return LeftRightRoad.TTY;if(p==LabyrinthPlace.DEAD_AIR)return DeadAirSet.PRINTOUTS;if(p==LabyrinthPlace.PITT)return PittCave.NOTE;return switch(p){
        case BORRASCA->BorrascaRooms.SOURCE;
        case HILL_NURSERY->new BlockPos(11,1,-21);case MINIATURES->new BlockPos(-12,1,-25);case MASQUE->new BlockPos(7,1,-4);case USHER->UsherHouse.REGISTER;case WINCHESTER->WinchesterHouse.SOURCE;case CHILD_ROOM->ChildRoom.CARD;case CRIMSON_HALL->new BlockPos(10,1,-12);case BLY_ROUTE->new BlockPos(8,1,-36);case ELK_LOT->ElkLot.REGISTER;case THREE_FINGERS->ThreeFingers.POCKETBOOK;case LAKE_HOUSE->LakeHouse.SOURCE;case ELK_FAN->new BlockPos(-7,1,-13);case MAPPING_INTERIOR->MappingHouse.SOURCE;case HOLY_RABBIT->new BlockPos(2,0,-22);case CONFESSION->JOURNAL;case ELK_CARCASSES->ElkCarcassMap.SOURCE;case COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE->new BlockPos(4,1,-6);case CAMP_BLOOD->new BlockPos(-20,1,-23);case DEVILS_ROCK->DevilsRockHouse.SOURCE;case WHEEL->WheelHouse.NOTEBOOK;case GHOSTS_SET->new BlockPos(9,0,-2);case END_WORLD_CABIN->new BlockPos(-8,1,-31);default->SOURCE;};}
    public static BlockPos ending(LabyrinthPlace p){if(p==LabyrinthPlace.POLYBIUS)return PolybiusSite.CLIPBOARD;if(p==LabyrinthPlace.REPLACEMENT)return ReplacementSite.MAILBOX;if(p==LabyrinthPlace.LAST_SWIMMER)return SwimmerSite.LOG;if(p==LabyrinthPlace.VACANT_POSSESSION)return VacantSite.DOCKET;if(p==LabyrinthPlace.PEOPLE_LIGHTS)return PeopleLightsSite.ENDING;if(p==LabyrinthPlace.PLACE_FOR_YOU)return PlaceForYouSite.MAILBOX;if(p==LabyrinthPlace.SECOND_PERSON)return SecondPersonSite.BENCH_BOOK;if(p==LabyrinthPlace.DEER_ROAD)return DeerRoadSite.NOTICE.above();if(p==LabyrinthPlace.STATE_ROUTE)return StateRouteSite.GATE.above();if(p==LabyrinthPlace.MARSH_SUPPER)return MarshSupperSite.ENDING;if(p==LabyrinthPlace.THE_DRAWING)return DrawingSite.ACCOUNT;if(p==LabyrinthPlace.CORNER_HOUSE)return CornerSite.NOTE;if(p==LabyrinthPlace.BLACK_CITY)return CitySite.PAD;if(p==LabyrinthPlace.KEPT)return KeptSite.TRAY;if(p==LabyrinthPlace.TALL_GRASS)return TallGrassSite.ENDING;if(p==LabyrinthPlace.GROWING_THINGS)return GrowingThingsSite.ENDING;if(p==LabyrinthPlace.LAST_CROSSING)return LastCrossingSite.ENDING;if(p==LabyrinthPlace.PINEHOOK)return PinehookSite.ENDING;if(p==LabyrinthPlace.CLOSING_HALL)return ClosingHallSite.TRANSCRIPT_DOOR;if(p==LabyrinthPlace.STOREROOM)return StoreroomSite.FOOTNOTE;if(p==LabyrinthPlace.STILLNESS)return StillnessSite.TALLY;if(p==LabyrinthPlace.LINE)return LineSite.RECORDER;if(p==LabyrinthPlace.OWN_LIGHT)return OwnLightSite.STUB;if(UncannyRooms.isUncanny(p))return UncannyRooms.account(p);if(DareRooms.is(p))return DareRooms.ACCOUNT;if(FeetUpRooms.is(p))return FeetUpRooms.NOTEBOOK_AT;if(p==LabyrinthPlace.WHISTLERS)return LookoutTower.ENDING;if(p==LabyrinthPlace.WORK_PRINT)return WorkPrintShop.LEDGER;if(p==LabyrinthPlace.POLAROID_TRAIL)return PolaroidSite.ALBUM;if(p==LabyrinthPlace.LEFT_RIGHT)return LeftRightRoad.LOGBOOK;if(p==LabyrinthPlace.DEAD_AIR)return DeadAirSet.MONITOR;if(p==LabyrinthPlace.PITT)return PittCave.JOURNAL;return switch(p){case BORRASCA->BorrascaRooms.ACCOUNT;case HILL_NURSERY->new BlockPos(8,-3,-36);case MINIATURES->new BlockPos(0,1,-25);case MASQUE->MasqueSuite.ACCOUNT;case USHER->UsherHouse.ACCOUNT;case WINCHESTER->WinchesterHouse.ENDING;case CHILD_ROOM->ChildRoom.ACCOUNT;case CRIMSON_HALL->new BlockPos(0,1,-39);case BLY_ROUTE->new BlockPos(8,1,-38);case ELK_LOT->ElkLot.BOARD;case THREE_FINGERS->ThreeFingers.BIBLE;case LAKE_HOUSE->LakeHouse.ACCOUNT;case ELK_FAN->new BlockPos(0,0,-17);case MAPPING_INTERIOR->MappingHouse.ENDING;case HOLY_RABBIT->new BlockPos(0,0,-120);case CONFESSION->new BlockPos(10,0,-28);case ELK_CARCASSES->ElkCarcassMap.ENDING;case COSTUME_NIGHT->new BlockPos(0,0,-67);case MOVIE_NIGHT->new BlockPos(0,0,-85);case WINTER_LAKE->new BlockPos(-3,0,-6);case CAMP_BLOOD->new BlockPos(0,0,-97);case DEVILS_ROCK->DevilsRockHouse.ENDING;case WHEEL->WheelHouse.ACCOUNT;case GHOSTS_SET->new BlockPos(10,0,-9);case END_WORLD_CABIN->new BlockPos(2,1,-30);case FAMILY_COPY->new BlockPos(0,0,-5);case OLD_CABIN->new BlockPos(0,0,-5);default->throw new IllegalArgumentException(p.id());};}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        if(UncannyRooms.isUncanny(p)){UncannyRooms.build(l,b,p);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);BuildBlocks.after(l,()->UncannyStories.prepare(l,b,p));return;}
        if(p==LabyrinthPlace.BORRASCA){BorrascaRooms.build(l,b);return;}
        // 0.4.173: the school's third floor is built whole by DareRooms, its folded note and account its own.
        if(DareRooms.is(p)){DareRooms.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,DareRegistry.material("wall").defaultBlockState(),DareRegistry.material("hall_tile").defaultBlockState(),DareRegistry.material("trim").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        // 0.4.200: the family house is built whole by FeetUpRooms, the rules on its hall table and the notebook in its attic its own.
        // 0.4.202: the fire lookout is built whole by LookoutTower; the binder and the fire log lie on its cab desk.
        if(p==LabyrinthPlace.WHISTLERS){LookoutTower.build(l,b);LabyrinthBuilder.entrance(l,b,LookoutRegistry.material("timber").defaultBlockState(),Blocks.SNOW_BLOCK.defaultBlockState(),LookoutRegistry.material("roof").defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        // 0.4.203: the video shop and the film's rooms behind it are built whole by WorkPrintShop; the rental card on the counter, the ledger behind it.
        // 0.4.209: the closing hall is built whole in the dark halls' ash, open, the children at its end, the squeeze shut.
        // 0.4.213: your own light is built whole in the dark halls' ash: the lobby, the twin branches and the far room.
        // 0.4.218: the street is built whole under the outside sky: the road, the boarded houses, the open one, the hedge.
        if(p==LabyrinthPlace.REPLACEMENT){ReplacementSite.build(l,b);LabyrinthBuilder.entrance(l,b,ReplacementRegistry.material("siding_a").defaultBlockState(),Blocks.SMOOTH_STONE.defaultBlockState(),Blocks.DARK_OAK_SLAB.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        // 0.4.217: the arcade is built whole: its one long room, the cabinets, the counter, and the tables over the cabinets.
        // 0.4.227: the pool is built whole: the lobby and its counter, the office, the lockers, the hall, the pool and its cover on the reel.
        // 0.4.228: the flat is built whole: the landing and the stair, the flat, the downstairs hall, the street and the van.
        // 0.4.253: the house Stan built, whole: the walk, the three floors, the garage and the driveway, under the night.
        // 0.4.274: the street and the flat above the launderette, whole; the reader comes out of a front door in the terrace opposite.
        // 0.4.310: the county road, whole, under the night: the bend, the pump house, the ditch and its gate; the car is the anchor's.
        // 0.4.333: the state route, whole, under the night: the hedge lane, the shoulder and its fence, four lanes and the median, the mailbox, the corn.
        // 0.4.348: the Marsh farm, whole, at dusk: the corn maze, the clearing, the farmhouse and its kitchen, the drive and the oak across it.
        if(p==LabyrinthPlace.GROWING_THINGS){GrowingThingsSite.build(l,b);LabyrinthBuilder.entrance(l,b,Blocks.BRICKS.defaultBlockState(),Blocks.GRAVEL.defaultBlockState(),Blocks.BRICK_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,Blocks.BRICKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        // 0.4.364: the cabin at Pinehook, whole, at dusk: the woodshed, the drive, the cabin, the shed in the pines, the dock and the boathouse.
        if(p==LabyrinthPlace.PINEHOOK){PinehookSite.build(l,b);LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,Blocks.SPRUCE_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.LAST_CROSSING){LastCrossingSite.build(l,b);LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.COARSE_DIRT.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,Blocks.SPRUCE_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.TALL_GRASS){TallGrassSite.build(l,b);LabyrinthBuilder.entrance(l,b,Blocks.MOSS_BLOCK.defaultBlockState(),Blocks.COARSE_DIRT.defaultBlockState(),Blocks.OAK_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,Blocks.MOSS_BLOCK.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        // 0.4.410: the machine, whole: the corridor of cabinets, the cave of ice, the wire, the hall of chairs and the four cells.
        if(p==LabyrinthPlace.KEPT){KeptSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,KeptSite.mat("casing"),KeptSite.mat("grating"),KeptSite.mat("ceiling"));LabyrinthBuilder.doors(l,b,p);return;}
        // 0.4.409: the camp on the ice, the pass, and the city with its pit and tunnel, whole.
        if(p==LabyrinthPlace.BLACK_CITY){CitySite.build(l,b);LabyrinthBuilder.entrance(l,b,CitySite.mat("boards"),CitySite.mat("boards"),Blocks.SPRUCE_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,CitySite.mat("boards"));LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        // 0.4.408: the street at siesta and the house on the corner, whole.
        if(p==LabyrinthPlace.CORNER_HOUSE){CornerSite.build(l,b);LabyrinthBuilder.entrance(l,b,CornerSite.mat("stucco"),CornerSite.mat("paving"),Blocks.BRICK_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,CornerSite.mat("stucco"));LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        // 0.4.407: the village square, whole, on a bright summer morning: the lane, the square, the box on its stool, the post office board.
        if(p==LabyrinthPlace.THE_DRAWING){DrawingSite.build(l,b);LabyrinthBuilder.entrance(l,b,Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),Blocks.DIRT_PATH.defaultBlockState(),Blocks.STONE_BRICK_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,Blocks.MOSSY_STONE_BRICKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.MARSH_SUPPER){MarshSupperSite.build(l,b);LabyrinthBuilder.entrance(l,b,YardRooms.mat("clapboard"),Blocks.COARSE_DIRT.defaultBlockState(),Blocks.DARK_OAK_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,YardRooms.mat("clapboard"));LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.STATE_ROUTE){StateRouteSite.build(l,b);LabyrinthBuilder.entrance(l,b,YardRooms.mat("clapboard"),Blocks.COARSE_DIRT.defaultBlockState(),Blocks.DARK_OAK_SLAB.defaultBlockState(),false);NovelRooms.box(l,b,-3,0,1,3,4,1,YardRooms.mat("clapboard"));LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.DEER_ROAD){DeerRoadSite.build(l,b);LabyrinthBuilder.entrance(l,b,Blocks.STONE_BRICKS.defaultBlockState(),Blocks.SMOOTH_STONE.defaultBlockState(),Blocks.SMOOTH_STONE.defaultBlockState(),false);LabyrinthBuilder.doors(l,b,p);NovelRooms.door(l,b.offset(0,0,1),net.minecraft.core.Direction.SOUTH,OnwardRegistry.SERVICE.get(),false);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.SECOND_PERSON){SecondPersonSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,SecondPersonRegistry.material("brick").defaultBlockState(),SecondPersonRegistry.material("paving").defaultBlockState(),SecondPersonRegistry.material("brick").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        // 0.4.257: Mara's house, whole: the garden wall and the road, the yard, both floors, the side porch, under the evening.
        if(p==LabyrinthPlace.PLACE_FOR_YOU){PlaceForYouSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,net.minecraft.world.level.block.Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        if(p==LabyrinthPlace.PEOPLE_LIGHTS){PeopleLightsSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,PeopleLightsRegistry.material("siding").defaultBlockState(),PeopleLightsRegistry.material("drive").defaultBlockState(),net.minecraft.world.level.block.Blocks.BLACK_CONCRETE.defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        if(p==LabyrinthPlace.VACANT_POSSESSION){VacantSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,VacantRegistry.material("plaster").defaultBlockState(),VacantRegistry.material("stair_carpet").defaultBlockState(),VacantRegistry.material("plaster").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        if(p==LabyrinthPlace.LAST_SWIMMER){SwimmerSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,SwimmerRegistry.material("blockwork").defaultBlockState(),SwimmerRegistry.material("lino").defaultBlockState(),Blocks.SMOOTH_STONE_SLAB.defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        if(p==LabyrinthPlace.POLYBIUS){PolybiusSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,PolybiusRegistry.material("wall").defaultBlockState(),PolybiusRegistry.material("carpet").defaultBlockState(),PolybiusRegistry.material("ceiling").defaultBlockState());LabyrinthBuilder.doors(l,b,p);PolybiusSite.boards(l,b);return;}
        if(p==LabyrinthPlace.OWN_LIGHT){OwnLightSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,DarkHallRegistry.material("ash").defaultBlockState(),DarkHallRegistry.material("floor").defaultBlockState(),DarkHallRegistry.material("ceiling").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        // 0.4.212: the line is built whole in the dark halls' ash: a short hall, the doorframe, the space with its columns, the pack.
        if(p==LabyrinthPlace.LINE){LineSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,DarkHallRegistry.material("ash").defaultBlockState(),DarkHallRegistry.material("floor").defaultBlockState(),DarkHallRegistry.material("ceiling").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        // 0.4.211: the stillness is built whole in the dark halls' ash: a wide, low hall with pillars and a door at each end.
        if(p==LabyrinthPlace.STILLNESS){StillnessSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,DarkHallRegistry.material("ash").defaultBlockState(),DarkHallRegistry.material("floor").defaultBlockState(),DarkHallRegistry.material("ceiling").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        // 0.4.210: the storeroom is built whole in the dark halls' ash: the hall, the spring door, the shelving, the bulb.
        if(p==LabyrinthPlace.STOREROOM){StoreroomSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,DarkHallRegistry.material("ash").defaultBlockState(),DarkHallRegistry.material("floor").defaultBlockState(),DarkHallRegistry.material("ceiling").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        if(p==LabyrinthPlace.CLOSING_HALL){ClosingHallSite.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,DarkHallRegistry.material("ash").defaultBlockState(),DarkHallRegistry.material("floor").defaultBlockState(),DarkHallRegistry.material("ceiling").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        if(p==LabyrinthPlace.WORK_PRINT){WorkPrintShop.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,WorkPrintRegistry.material("wall").defaultBlockState(),WorkPrintRegistry.material("carpet").defaultBlockState(),WorkPrintRegistry.material("ceiling").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        // 0.4.204: the yard, the crawlspace, the cinema and the pines are built whole by PolaroidSite; the first balloon tag on the porch rail, the album in the deer blind.
        // 0.4.205: the road is built whole by LeftRightRoad and dressed for a first night; the teletype on the booth's desk, the convoy log beside it.
        // 0.4.206: the set is built whole by DeadAirSet and left at rest (the hallway a drain, the bulkhead open); the printouts on the folding table, the centre monitor in the booth.
        // 0.4.207: the deeper passages are built whole by PittCave at their own height, left in the first visit's shape; Brad's note on the staging ledge, the field journal on the moor.
        if(p==LabyrinthPlace.PITT){PittCave.build(l,b);LabyrinthBuilder.entrance(l,b,PittRegistry.material("limestone").defaultBlockState(),PittRegistry.material("clay").defaultBlockState(),Blocks.STONE_SLAB.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.DEAD_AIR){DeadAirSet.build(l,b);LabyrinthBuilder.entrance(l,b,DeadAirRegistry.material("walnut").defaultBlockState(),DeadAirRegistry.material("shag").defaultBlockState(),Blocks.DARK_OAK_SLAB.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(p==LabyrinthPlace.LEFT_RIGHT){LeftRightRoad.build(l,b);LabyrinthBuilder.entrance(l,b,LeftRightRegistry.material("concrete").defaultBlockState(),LeftRightRegistry.material("asphalt").defaultBlockState(),Blocks.SMOOTH_STONE_SLAB.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);LeftRightRoad.dress(l,b,1,1,LeftRightRoad.JUNCTION);return;}
        if(p==LabyrinthPlace.POLAROID_TRAIL){PolaroidSite.build(l,b);LabyrinthBuilder.entrance(l,b,PolaroidRegistry.material("siding").defaultBlockState(),Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState());LabyrinthBuilder.doors(l,b,p);ScenePlaytestRepairs.arrivalGround(l,b);return;}
        if(FeetUpRooms.is(p)){FeetUpRooms.build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,FeetUpRegistry.material("paper").defaultBlockState(),FeetUpRegistry.material("hall_tile").defaultBlockState(),FeetUpRegistry.material("plaster").defaultBlockState());LabyrinthBuilder.doors(l,b,p);return;}
        var r=p.room();if(p==LabyrinthPlace.ELK_LOT)BuildBlocks.after(l,()->ElkLot.empty(l,b));box(l,b,r.minX(),r.minY(),r.minZ(),r.maxX(),r.maxY(),r.maxZ(),Blocks.AIR);
        switch(p){case HILL_NURSERY->hill(l,b);case MINIATURES->miniatures(l,b);case MASQUE->MasqueSuite.build(l,b);case USHER->UsherHouse.build(l,b);case WINCHESTER->winchester(l,b);case CHILD_ROOM->ChildRoom.build(l,b);case CRIMSON_HALL->crimson(l,b);case BLY_ROUTE->BlyManor.build(l,b);case ELK_LOT->ElkLot.build(l,b);case THREE_FINGERS->ThreeFingers.build(l,b);case LAKE_HOUSE->LakeHouse.build(l,b);case ELK_FAN->fan(l,b);case MAPPING_INTERIOR->MappingHouse.build(l,b);case HOLY_RABBIT->rabbit(l,b);case CONFESSION->confession(l,b);case ELK_CARCASSES->carcasses(l,b);case WINTER_LAKE->WinterLake.build(l,b);case COSTUME_NIGHT->CostumeNight.build(l,b);case MOVIE_NIGHT->MovieNight.build(l,b);case CAMP_BLOOD->camp(l,b);case DEVILS_ROCK->DevilsRockHouse.build(l,b);case WHEEL->WheelHouse.build(l,b);case GHOSTS_SET->GhostsSetRooms.build(l,b);case END_WORLD_CABIN->endCabin(l,b);case FAMILY_COPY,OLD_CABIN->copyLanding(l,b,p);default->throw new IllegalArgumentException(p.id());}
        // Papers are original scene-specific texts; reading surfaces generate immutable reader originals.
        // The child's room (0.4.74) sets its own bedtime card and exercise book, and the Last Stop (0.4.78) its till receipt and
        // noticeboard, and Three Fingers (0.4.81) the soldier's pocketbook and the family Bible, and the Wheel (0.4.121) the daughter's notebook; the Masque's invitation and Bly's note
        // (0.4.105) are their builders' own; every other room gets a lectern and a ledger.
        var note=source(p);if(p!=LabyrinthPlace.CHILD_ROOM&&p!=LabyrinthPlace.WHEEL&&p!=LabyrinthPlace.ELK_LOT&&p!=LabyrinthPlace.THREE_FINGERS&&p!=LabyrinthPlace.MASQUE&&p!=LabyrinthPlace.BLY_ROUTE){if(note.getY()>0&&BuildBlocks.state(l,b.offset(note.below())).isAir())furniture(l,b,note.getX(),note.getY()-1,note.getZ(),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);paper(l,b,note,LiteraryTexts.source(p),holder(p));}
        if(p!=LabyrinthPlace.OLD_CABIN&&p!=LabyrinthPlace.FAMILY_COPY&&p!=LabyrinthPlace.CHILD_ROOM&&p!=LabyrinthPlace.ELK_LOT&&p!=LabyrinthPlace.THREE_FINGERS){var end=ending(p);if(end.getY()>0&&BuildBlocks.state(l,b.offset(end.below())).isAir())furniture(l,b,end.getX(),end.getY()-1,end.getZ(),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);prop(l,b,end,LiteraryPropBlock.Kind.LEDGER,Direction.SOUTH);}
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,wall(p).defaultBlockState(),floor(p).defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());LabyrinthBuilder.doors(l,b,p);
        if(p==LabyrinthPlace.ELK_CARCASSES)ElkCarcassMap.deckhouse(l,b);
        // The elk arrival is aboard a yacht: its hull is the arrival's support, never earth heaped in the lake.
        if(outside(p)&&p!=LabyrinthPlace.FAMILY_COPY&&p!=LabyrinthPlace.OLD_CABIN&&p!=LabyrinthPlace.ELK_CARCASSES)ScenePlaytestRepairs.arrivalGround(l,b);
        StaceyCover.fresh(l,b,p);
    }
    public static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,block instanceof LeavesBlock?block.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true):block.defaultBlockState());}
    public static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){BuildBlocks.set(l,b.offset(x,y,z),block.defaultBlockState(),F);}
    public static void prop(ServerLevel l,BlockPos b,BlockPos rel,LiteraryPropBlock.Kind kind,Direction facing){BuildBlocks.set(l,b.offset(rel),LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,kind).setValue(LiteraryPropBlock.FACING,facing),F);}
    public static void paper(ServerLevel l,BlockPos b,BlockPos rel,ItemStack book){paper(l,b,rel,book,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true));}
    /** The paper on whatever native lectern holds it: a vanilla lectern, or a scene's own reading stand. */
    public static void paper(ServerLevel l,BlockPos b,BlockPos rel,ItemStack book,net.minecraft.world.level.block.state.BlockState holder){BuildBlocks.set(l,b.offset(rel),holder,F);BuildBlocks.after(l,()->{if(l.getBlockEntity(b.offset(rel)) instanceof LecternBlockEntity d){d.setBook(book);d.setChanged();}});}
    /** 0.4.164 (owner: "podiums like this need new textures suited to their vignettes"): the scenes whose paper stood as a vanilla lectern on a table, and the stand each reads from now. */
    public static net.minecraft.world.level.block.state.BlockState holder(LabyrinthPlace p){
        return switch(p){
            case MOVIE_NIGHT->ReadingStandBlock.of(ReadingStandBlock.Kind.PROGRAM,Direction.SOUTH);
            case COSTUME_NIGHT->ReadingStandBlock.of(ReadingStandBlock.Kind.SCORECARD,Direction.SOUTH);
            case WINTER_LAKE,DEVILS_ROCK->ReadingStandBlock.of(ReadingStandBlock.Kind.FOLDED_NOTE,Direction.SOUTH);
            case WINCHESTER->ReadingStandBlock.of(ReadingStandBlock.Kind.LEDGER_REST,Direction.EAST);
            case GHOSTS_SET->ReadingStandBlock.of(ReadingStandBlock.Kind.SCRIPT_STAND,Direction.NORTH);
            case BORRASCA->ReadingStandBlock.of(ReadingStandBlock.Kind.TRAIL_RACK,Direction.SOUTH);
            case USHER->ReadingStandBlock.of(ReadingStandBlock.Kind.VAULT_REGISTER,Direction.SOUTH);
            case MAPPING_INTERIOR->ReadingStandBlock.of(ReadingStandBlock.Kind.FLOOR_PLAN,Direction.SOUTH);
            default->Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true);};
    }
    private static Block wall(LabyrinthPlace p){return switch(p){case USHER->Blocks.DEEPSLATE_BRICKS;case MAPPING_INTERIOR,DEVILS_ROCK->LiteraryRegistry.SIDING.get();case CRIMSON_HALL->Blocks.POLISHED_BLACKSTONE_BRICKS;case MINIATURES,GHOSTS_SET,WHEEL->Blocks.CALCITE;case LAKE_HOUSE->LakeHouseRegistry.SIDING.get();case WINCHESTER->LiteraryRegistry.WINCHESTER_WALLPAPER.get();default->LiteraryRegistry.DARK_PANEL.get();};}
    private static Block floor(LabyrinthPlace p){return switch(p){case USHER->Blocks.POLISHED_DEEPSLATE;case CRIMSON_HALL->LiteraryRegistry.RED_FLOOR.get();case MAPPING_INTERIOR->Blocks.BIRCH_PLANKS;case DEVILS_ROCK->Blocks.SMOOTH_STONE;case LAKE_HOUSE->LakeHouseRegistry.DOCK.get();case WINCHESTER->LiteraryRegistry.WINCHESTER_PARQUET.get();default->Blocks.DARK_OAK_PLANKS;};}
    private static void room(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int y,int h,Block walls,Block flooring){
        NovelRooms.room(l,b,x0,x1,z0,z1,y,h,walls,flooring);
        // Timber bays, sills and a recessed coffer construction rather than featureless boxes.
        for(int z=z0+2;z<z1;z+=5)for(int x:new int[]{x0,x1}){box(l,b,x,y,z,x,y+h,z,Blocks.STRIPPED_DARK_OAK_LOG);at(l,b,x,y+h-1,z,Blocks.DARK_OAK_SLAB);}
        for(int x=x0+2;x<x1;x+=5)for(int z:new int[]{z0,z1})box(l,b,x,y,z,x,y+h,z,Blocks.STRIPPED_DARK_OAK_LOG);
        for(int x=x0+1;x<x1;x++)for(int z=z0+1;z<z1;z++)if((x-x0)%5==0||(z-z0)%5==0)at(l,b,x,y+h,z,Blocks.DARK_OAK_PLANKS);
        for(int z=z0+1;z<z1;z++)for(int x:new int[]{x0,x1})at(l,b,x,y,z,Blocks.DARK_OAK_PLANKS);
    }
    private static void door(ServerLevel l,BlockPos b,int x,int y,int z,Direction f,boolean open){box(l,b,x,y,z,x,y+1,z,Blocks.AIR);at(l,b,x,y+2,z,Blocks.STRIPPED_DARK_OAK_LOG);NovelRooms.door(l,b.offset(x,y,z),f,Blocks.DARK_OAK_DOOR,open);var across=f.getAxis()==Direction.Axis.X?Direction.NORTH:Direction.EAST;for(int a:new int[]{-1,1})for(int dy=0;dy<3;dy++)BuildBlocks.set(l,b.offset(x,y+dy,z).relative(across,a),Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),F);}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction f){NovelRooms.furniture(l,b.offset(x,y,z),k,f);}
    private static void desk(ServerLevel l,BlockPos b,int x,int y,int z){furniture(l,b,x,y,z,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);furniture(l,b,x,y,z+2,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);}
    private static void light(ServerLevel l,BlockPos b,int x,int y,int z){at(l,b,x,y+1,z,Blocks.CHAIN);BuildBlocks.set(l,b.offset(x,y,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);}
    private static void steps(ServerLevel l,BlockPos b,int x,int z,int y,int count,boolean down){for(int i=0;i<count;i++){int yy=y+(down?-i:i);box(l,b,x-1,yy-2,z-i,x+1,yy-1,z-i,Blocks.STONE_BRICKS);box(l,b,x-1,yy,z-i,x+1,yy+2,z-i,Blocks.AIR);for(int xx=x-1;xx<=x+1;xx++)BuildBlocks.set(l,b.offset(xx,yy-1,z-i),Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,down?Direction.SOUTH:Direction.NORTH),F);}}
    private static void hill(ServerLevel l,BlockPos b){
        room(l,b,-3,3,-43,0,0,5,Blocks.CALCITE,Blocks.DARK_OAK_PLANKS);
        for(int side:new int[]{-1,1}){room(l,b,side<0?-14:4,side<0?-4:14,-24,-10,0,6,Blocks.CALCITE,Blocks.DARK_OAK_PLANKS);door(l,b,side*4,0,-16,side<0?Direction.EAST:Direction.WEST,true);NovelRooms.bed(l,b.offset(side*10,0,-22),Blocks.WHITE_BED,Direction.NORTH);furniture(l,b,side*11,0,-12,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);box(l,b,side*14,2,-22,side*14,3,-19,Blocks.BLACK_STAINED_GLASS);}
        box(l,b,-4,-1,-16,-4,2,-16,Blocks.POWDER_SNOW);door(l,b,-4,0,-16,Direction.EAST,true);
        // The corridor's own wall stands behind each nursery door: open it, or the nurseries are sealed.
        for(int x:new int[]{-3,3})box(l,b,x,0,-16,x,1,-16,Blocks.AIR);
        room(l,b,-13,13,-42,-30,-4,3,Blocks.MOSSY_STONE_BRICKS,Blocks.STONE_BRICKS);steps(l,b,0,-25,0,5,true);box(l,b,-1,-4,-33,1,-2,-30,Blocks.AIR);box(l,b,-1,-5,-33,1,-5,-30,Blocks.STONE_BRICKS);
        // The cellar stair runs between its own walls: nothing beyond the house shows from the steps.
        for(int z=-29;z<=-24;z++)for(int y=-6;y<=-1;y++)for(int x:new int[]{-2,2})if(l.getBlockState(b.offset(x,y,z)).isAir())at(l,b,x,y,z,Blocks.MOSSY_STONE_BRICKS);
        prop(l,b,HILL_WALL,LiteraryPropBlock.Kind.PHOTO,Direction.EAST);BuildBlocks.set(l,b.offset(HILL_WALL),BuildBlocks.state(l,b.offset(HILL_WALL)).setValue(LiteraryPropBlock.STAGE,3),F);at(l,b,-10,-4,-36,Blocks.CHISELED_STONE_BRICKS);desk(l,b,8,-4,-36);light(l,b,0,3,-8);light(l,b,0,3,-36);
    }
    private static void miniatures(ServerLevel l,BlockPos b){
        room(l,b,-14,14,-30,0,0,7,Blocks.CALCITE,Blocks.BIRCH_PLANKS);
        for(int x:new int[]{-8,0,8}){furniture(l,b,x,0,-18,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);prop(l,b,new BlockPos(x,1,-18),LiteraryPropBlock.Kind.MINIATURE,Direction.SOUTH);BuildBlocks.set(l,b.offset(x,1,-18),BuildBlocks.state(l,b.offset(x,1,-18)).setValue(LiteraryPropBlock.STAGE,x<0?0:x==0?1:2),F);furniture(l,b,x,0,-15,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH);light(l,b,x,4,-18);}
        for(int z=-26;z<=-7;z+=4){at(l,b,13,0,z,Blocks.BARREL);at(l,b,13,1,z,Blocks.BOOKSHELF);furniture(l,b,-12,0,z,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);}
        desk(l,b,0,0,-25);box(l,b,-8,2,-30,8,4,-30,Blocks.GLASS);box(l,b,-8,2,-31,8,4,-31,Blocks.LIGHT_GRAY_CONCRETE);
    }
    private static void winchester(ServerLevel l,BlockPos b){
        // 0.4.201: the shell in damask on parquet; WinchesterHouse builds the whole house inside it.
        room(l,b,-30,30,-77,0,0,15,LiteraryRegistry.WINCHESTER_WALLPAPER.get(),LiteraryRegistry.WINCHESTER_PARQUET.get());
        WinchesterHouse.build(l,b);
    }
    private static void crimson(ServerLevel l,BlockPos b){
        room(l,b,-18,18,-43,0,0,13,Blocks.POLISHED_BLACKSTONE_BRICKS,LiteraryRegistry.RED_FLOOR.get());box(l,b,-3,13,-27,3,15,-21,Blocks.AIR);
        for(int x:new int[]{-14,14})for(int z=-38;z<-6;z+=8)box(l,b,x,0,z,x,11,z,Blocks.POLISHED_BASALT);
        box(l,b,-17,6,-41,17,6,-35,Blocks.DARK_OAK_PLANKS);box(l,b,-17,6,-35,-14,6,-6,Blocks.DARK_OAK_PLANKS);box(l,b,14,6,-35,17,6,-6,Blocks.DARK_OAK_PLANKS);
        // 0.4.77: eight treads up to the gallery's own level, a landing beyond the last, and a gap in the railing onto the gallery.
        for(int z=-34;z<-6;z++){if(z<-17||z>-15)at(l,b,-14,7,z,Blocks.DARK_OAK_FENCE);at(l,b,14,7,z,Blocks.DARK_OAK_FENCE);}steps(l,b,-11,-7,0,8,false);box(l,b,-13,6,-17,-10,6,-15,Blocks.DARK_OAK_PLANKS);
        CrimsonGallery.markBuilt(LabyrinthData.get(l.getServer()),b);
        at(l,b,0,0,-30,Blocks.JUKEBOX);for(var q:List.of(new BlockPos(-12,0,-35),new BlockPos(12,0,-35),new BlockPos(0,7,-39)))prop(l,b,q,LiteraryPropBlock.Kind.CYLINDER,Direction.SOUTH);
        desk(l,b,0,0,-39);furniture(l,b,9,0,-12,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.WEST);light(l,b,0,8,-10);
    }
    private static void terrain(ServerLevel l,BlockPos b,LabyrinthPlace p,Block ground){var r=p.room();if(ground==Blocks.SAND)box(l,b,r.minX(),-4,r.minZ(),r.maxX(),-4,0,Blocks.SANDSTONE);box(l,b,r.minX(),-3,r.minZ(),r.maxX(),-1,0,ground);for(int x=r.minX()+1;x<r.maxX();x+=6)for(int z=r.minZ()+3;z<-10;z+=9){if(Math.abs(x)<9)continue;int ox=Math.floorMod(x*31+z*17,5)-2,oz=Math.floorMod(x*13-z*7,5)-2;if(buildingClearing(p,x+ox,z+oz))continue;tree(l,b,x+ox,0,z+oz,4+(Math.abs(x+z)%3));}}
    /** Reserve the whole canopy around roofs and walls before planting the forest. */
    static boolean buildingClearing(LabyrinthPlace p,int x,int z){return switch(p){
        case CAMP_BLOOD->{boolean cabin=false;for(int cx:new int[]{-20,20})for(int cz:new int[]{-26,-57,-83})if(Math.abs(x-cx)<=12&&Math.abs(z-cz)<=11)cabin=true;yield cabin;}
        case MAPPING_INTERIOR->Math.abs(x)<=17&&z>=-41&&z<=-3;
        case END_WORLD_CABIN->Math.abs(x)<=17&&z>=-40&&z<=-8;
        case ELK_LOT->(x>=-27&&x<=-3&&z>=-28&&z<=0)||((Math.abs(x-9)<=6||Math.abs(x-21)<=6)&&z>=-33&&z<=-16);
        default->false;
    };}
    private static void tree(ServerLevel l,BlockPos b,int x,int y,int z,int height){box(l,b,x,y,z,x,y+height,z,Blocks.SPRUCE_LOG);for(int h=2;h<=height+2;h++){int w=Math.max(1,(height+3-h)/2);box(l,b,x-w,y+h,z-w,x+w,y+h,z+w,Blocks.SPRUCE_LEAVES);}}
    private static void roof(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int eaves){
        // Foundations skirt the building. They must not overwrite its floor or cellar ladder.
        for(int x=x0-1;x<=x1+1;x++)for(int z=z0-1;z<=z1+1;z++)if(x<x0||x>x1||z<z0||z>z1)
            box(l,b,x,-2,z,x,-1,z,Blocks.STONE_BRICKS);
        int mid=(x0+x1)/2;for(int x=x0-1;x<=x1+1;x++){int y=eaves+Math.min(x-x0+1,x1+1-x)/2;for(int z=z0-1;z<=z1+1;z++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x<=mid?Direction.EAST:Direction.WEST),F);if(x>=x0&&x<=x1)for(int zz:new int[]{z0,z1})box(l,b,x,eaves,zz,x,y-1,zz,Blocks.SPRUCE_PLANKS);}
        for(int z=z0-1;z<=z1+1;z++)at(l,b,mid,eaves+(x1-x0)/4+1,z,Blocks.DARK_OAK_SLAB);
    }
    private static void window(ServerLevel l,BlockPos b,int x,int z,boolean east){
        for(int w=-2;w<=2;w++)for(int y=1;y<=4;y++){var at=east?b.offset(x,y,z+w):b.offset(x+w,y,z);BuildBlocks.set(l,at,(Math.abs(w)==2||y==1||y==4?Blocks.STRIPPED_SPRUCE_LOG:Blocks.GLASS_PANE).defaultBlockState(),F);}
    }
    private static void fan(ServerLevel l,BlockPos b){
        room(l,b,-11,11,-28,0,0,9,LiteraryRegistry.SIDING.get(),Blocks.BIRCH_PLANKS);prop(l,b,FAN,LiteraryPropBlock.Kind.FAN,Direction.SOUTH);ElkFanRoom.build(l,b);
        furniture(l,b,-5,0,-15,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);BuildBlocks.set(l,b.offset(SceneHuntReview.TOOLS),io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.state(io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.Kind.TOOL_TRAY,Direction.SOUTH),F);
        box(l,b,-4,-1,-21,4,-1,-12,Blocks.GRAY_WOOL);box(l,b,6,0,-23,9,2,-23,Blocks.STONE_BRICKS);at(l,b,7,0,-22,Blocks.CAMPFIRE);furniture(l,b,-7,0,-21,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,-7,0,-13,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.EAST);box(l,b,6,2,-28,9,4,-28,Blocks.BLACK_STAINED_GLASS);BuildBlocks.after(l,()->ElkFanSequence.markDressed(l,b));
    }
    private static void rabbit(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.HOLY_RABBIT,Blocks.SNOW_BLOCK);tree(l,b,0,0,-25,19);box(l,b,-5,0,-29,5,4,-20,Blocks.SPRUCE_LEAVES);box(l,b,-3,0,-28,3,0,-21,Blocks.AIR);box(l,b,-3,-1,-28,3,-1,-21,Blocks.PODZOL);box(l,b,-2,0,-20,2,2,-18,Blocks.AIR);
        HolyRabbit.shelter(l,b);
        PlaytestScenes0488.fresh(l,b,LabyrinthPlace.HOLY_RABBIT);
        for(int y=0;y<=4;y++)at(l,b,0,y,-25,y==2?LiteraryRegistry.CARVED_TRUNK.get():Blocks.OAK_LOG);prop(l,b,new BlockPos(0,3,-25),LiteraryPropBlock.Kind.TRUNK_MARK,Direction.SOUTH);at(l,b,0,3,-24,Blocks.AIR);
        // The drag trench is physical: three blocks deep, with no side route over the drifts.
        box(l,b,-3,0,-118,3,3,-33,Blocks.SNOW_BLOCK);box(l,b,-1,0,-118,1,2,-30,Blocks.AIR);box(l,b,-1,-1,-118,1,-1,-30,LiteraryRegistry.DRAG_SNOW.get());for(int z=-119;z<=-34;z++)for(int x:new int[]{-2,2})box(l,b,x,0,z,x,3,z,Blocks.SNOW_BLOCK);
        at(l,b,0,0,-120,Blocks.BARREL);box(l,b,-3,-1,-123,3,-1,-119,Blocks.SNOW_BLOCK);
    }
    private static void confession(ServerLevel l,BlockPos b){
        room(l,b,-12,12,-33,0,0,9,LiteraryRegistry.DARK_PANEL.get(),Blocks.DARK_OAK_PLANKS);for(int x:new int[]{-11,11})for(int z=-29;z<-8;z++)at(l,b,x,0,z,Blocks.BOOKSHELF);
        desk(l,b,-8,0,-25);paper(l,b,JOURNAL,LiteraryTexts.source(LabyrinthPlace.CONFESSION));furniture(l,b,1,0,-22,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.SOUTH);furniture(l,b,1,0,-17,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH);box(l,b,-3,2,-33,4,5,-33,Blocks.BLACK_STAINED_GLASS);light(l,b,0,4,-21);box(l,b,9,0,-29,11,3,-29,Blocks.DARK_OAK_PLANKS);
    }
    private static void carcasses(ServerLevel l,BlockPos b){ElkCarcassMap.build(l,b);}
    private static void camp(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.CAMP_BLOOD,Blocks.PODZOL);for(int x:new int[]{-20,20})for(int z:new int[]{-26,-57,-83}){room(l,b,x-8,x+8,z-7,z+7,0,5,Blocks.SPRUCE_PLANKS,Blocks.SPRUCE_PLANKS);door(l,b,x+(x<0?8:-8),0,z,Direction.EAST,true);for(int dx:new int[]{-5,5}){NovelRooms.bed(l,b.offset(x+dx,0,z-4),Blocks.RED_BED,Direction.NORTH);furniture(l,b,x+dx,0,z+3,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.NORTH);}for(int zz=z-8;zz<=z+8;zz++)box(l,b,x-9,5,zz,x+9,5,zz,Blocks.SPRUCE_SLAB);roof(l,b,x-8,x+8,z-7,z+7,5);window(l,b,x,z-7,false);light(l,b,x,3,z);}
        // 0.4.284: a white sand road, as the Pine Barrens' roads are.
        for(int z=-8;z>=-98;z--)box(l,b,-2,-1,z,2,-1,z,Blocks.SAND);at(l,b,6,0,-45,Blocks.CAMPFIRE);NovelRooms.sign(l,b.offset(4,1,-97),Direction.SOUTH,new String[]{"SERVICE ROAD","No camp traffic","",""});
        // 0.4.121: the mess hall the schedule sends everyone to, west of the path between the middle and last cabins.
        room(l,b,5,11,-74,-66,0,5,Blocks.SPRUCE_PLANKS,Blocks.SPRUCE_PLANKS);door(l,b,5,0,-70,Direction.WEST,true);for(int zz=-75;zz<=-65;zz++)box(l,b,4,5,zz,12,5,zz,Blocks.SPRUCE_SLAB);roof(l,b,5,11,-74,-66,5);window(l,b,8,-74,false);light(l,b,7,3,-67);
        CampBloodDecor.build(l,b);CampSession.build(l,b);
    }
    /**
     * A lake cabin at dusk (0.4.52). A front door onto a deep porch; a bedroom with two beds against the lake; a kitchen;
     * a living room with the television, the hearth and the dining table where the account will lie; windows on every
     * side, so the storm is always in view. A small shed stands at the edge of the yard: its door is the only way out.
     */
    private static void endCabin(ServerLevel l,BlockPos b){
        terrain(l,b,LabyrinthPlace.END_WORLD_CABIN,Blocks.GRASS_BLOCK);box(l,b,-27,-8,-76,27,-1,-42,Blocks.WATER);box(l,b,-28,-9,-77,28,-9,-41,Blocks.GRAVEL);
        room(l,b,-12,12,-35,-13,0,6,Blocks.SPRUCE_PLANKS,Blocks.SPRUCE_PLANKS);door(l,b,0,0,-13,Direction.SOUTH,false);
        // Bedroom (north-west): two beds under the lake window, a lamp between them, the visitors' request on its stand.
        box(l,b,-4,0,-34,-4,5,-25,Blocks.SPRUCE_PLANKS);box(l,b,-11,0,-25,-4,5,-25,Blocks.SPRUCE_PLANKS);for(int y=0;y<=5;y++){at(l,b,-4,y,-25,Blocks.STRIPPED_SPRUCE_LOG);at(l,b,-4,y,-34,Blocks.STRIPPED_SPRUCE_LOG);}
        box(l,b,-4,0,-29,-4,1,-29,Blocks.AIR);NovelRooms.door(l,b.offset(-4,0,-29),Direction.EAST,Blocks.SPRUCE_DOOR,false);at(l,b,-4,2,-29,Blocks.STRIPPED_SPRUCE_LOG);
        NovelRooms.bed(l,b.offset(-10,0,-33),Blocks.WHITE_BED,Direction.NORTH);NovelRooms.bed(l,b.offset(-6,0,-33),Blocks.LIGHT_GRAY_BED,Direction.NORTH);
        furniture(l,b,-8,0,-34,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);
        furniture(l,b,-11,0,-27,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);light(l,b,-8,4,-29);
        // Kitchen (south-west), open to the living room.
        BuildBlocks.set(l,b.offset(-11,0,-23),Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.EAST),F);at(l,b,-11,0,-22,Blocks.CAULDRON);
        for(int z=-21;z<=-15;z++){at(l,b,-11,0,z,Blocks.SPRUCE_PLANKS);BuildBlocks.set(l,b.offset(-11,3,z),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP),F);}
        for(int z:new int[]{-20,-16})at(l,b,-11,1,z,Blocks.FLOWER_POT);
        furniture(l,b,-8,0,-18,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);furniture(l,b,-8,0,-17,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH);furniture(l,b,-8,0,-19,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.SOUTH);light(l,b,-8,4,-19);
        // Living room: the television on its cabinet, seats facing it, the hearth, and the dining table.
        furniture(l,b,7,0,-24,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);prop(l,b,TV,LiteraryPropBlock.Kind.TELEVISION,Direction.SOUTH);
        furniture(l,b,5,0,-20,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH);furniture(l,b,7,0,-20,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.NORTH);furniture(l,b,9,0,-20,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.NORTH);furniture(l,b,7,0,-22,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.NORTH);
        furniture(l,b,2,0,-30,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH);furniture(l,b,2,0,-29,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);furniture(l,b,2,0,-31,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        furniture(l,b,1,0,-30,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST);furniture(l,b,3,0,-30,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        box(l,b,10,0,-34,11,11,-33,Blocks.BRICKS);box(l,b,9,0,-32,11,2,-32,Blocks.BRICKS);at(l,b,10,0,-32,Blocks.CAMPFIRE);at(l,b,10,1,-32,Blocks.AIR);
        for(int x=9;x<=11;x++)BuildBlocks.set(l,b.offset(x,3,-32),Blocks.DARK_OAK_SLAB.defaultBlockState(),F);
        light(l,b,2,4,-30);light(l,b,7,4,-21);light(l,b,0,4,-17);
        // Windows on every side: the lake behind, the porch in front, the woods either side.
        glaze(l,b,true,-35,-9,-7);glaze(l,b,true,-35,1,7);glaze(l,b,false,-12,-31,-28);glaze(l,b,false,-12,-21,-18);
        glaze(l,b,false,12,-29,-26);glaze(l,b,false,12,-21,-17);glaze(l,b,true,-13,-8,-6);glaze(l,b,true,-13,4,8);
        // The porch, deep and roofed, with the jar of grasshoppers still on it.
        box(l,b,-12,-1,-12,12,-1,-8,Blocks.SPRUCE_PLANKS);for(int x:new int[]{-12,12})box(l,b,x,0,-8,x,5,-8,Blocks.SPRUCE_LOG);box(l,b,-12,6,-12,12,6,-8,Blocks.SPRUCE_SLAB);prop(l,b,new BlockPos(-7,0,-9),LiteraryPropBlock.Kind.JAR,Direction.SOUTH);
        roof(l,b,-12,12,-35,-13,6);
        // A trodden path from the porch to the shed.
        for(int z=-7;z<=-5;z++)for(int x=-1;x<=1;x++)at(l,b,x,-1,z,Blocks.DIRT_PATH);
        shed(l,b);
        cabinSurfaces(l,b);
    }
    /**
     * 0.4.406, the texture pass's eleventh release (docs/TEXTURE_PASS_CABIN_0_4_406.md): the cabin's own surfaces, laid once its
     * frame stands. The outer walls face out, weathered shiplap outside and the pine it had within, on a fieldstone foot; hewn
     * corner posts; wide floorboards, leaving the kitchen and the hearth the boards their tile and stone are laid over later; the
     * ceiling's boards between its beams; the porch decked and shaked, and the foundation ring a fieldstone apron. The timber bays,
     * the windows and the door stay as they are.
     * The roof is the scene pass's (SceneCraft), in the cabin's shakes.
     */
    private static void cabinSurfaces(ServerLevel l,BlockPos b){
        int x0=-12,x1=12,z0=-35,z1=-13;
        for(int y=0;y<=6;y++)for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            boolean xw=x==x0||x==x1,zw=z==z0||z==z1;if(!xw&&!zw)continue;var at=b.offset(x,y,z);var old=BuildBlocks.state(l,at);
            // The posts run up to the eave: the roof frame's gable fill reaches the corners' top course, where no stair is left to replace.
            if(xw&&zw){if(old.is(Blocks.SPRUCE_PLANKS))BuildBlocks.set(l,at,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),F);continue;}
            Direction out=x==x0?Direction.WEST:x==x1?Direction.EAST:z==z0?Direction.NORTH:Direction.SOUTH;
            if(old.is(Blocks.SPRUCE_PLANKS)||y==0&&old.is(Blocks.DARK_OAK_PLANKS))
                BuildBlocks.set(l,at,(y==0?RoomShell.CABIN_SILL:RoomShell.CABIN_WALL).get().defaultBlockState().setValue(RoomShell.Skin.FACING,out),F);
        }
        for(int x=x0+1;x<x1;x++)for(int z=z0+1;z<z1;z++){
            boolean kitchen=x>=-11&&x<=-5&&z>=-24&&z<=-14,hearth=x>=9&&z>=-32&&z<=-31;
            var floor=b.offset(x,-1,z);if(!kitchen&&!hearth&&BuildBlocks.state(l,floor).is(Blocks.SPRUCE_PLANKS))BuildBlocks.set(l,floor,RoomShell.CABIN_FLOOR.get().defaultBlockState(),F);
            var over=b.offset(x,6,z);var top=BuildBlocks.state(l,over);
            if(top.is(Blocks.BIRCH_PLANKS))BuildBlocks.set(l,over,RoomShell.CABIN_CEILING.get().defaultBlockState(),F);
            else if(top.is(Blocks.DARK_OAK_PLANKS))BuildBlocks.set(l,over,RoomShell.CABIN_BEAM.get().defaultBlockState(),F);
        }
        // The foundation ring round the walls shows at the ground as a fieldstone apron; along the porch it is the deck's first board.
        for(int x=x0-1;x<=x1+1;x++)for(int z=z0-1;z<=z1+1;z++){
            if(x>=x0&&x<=x1&&z>=z0&&z<=z1)continue;var at=b.offset(x,-1,z);if(!BuildBlocks.state(l,at).is(Blocks.STONE_BRICKS))continue;
            Direction out=x<x0?Direction.WEST:x>x1?Direction.EAST:z<z0?Direction.NORTH:Direction.SOUTH;
            BuildBlocks.set(l,at,z==z1+1&&x>=x0&&x<=x1?RoomShell.CABIN_PORCH.get().defaultBlockState():RoomShell.CABIN_SILL.get().defaultBlockState().setValue(RoomShell.Skin.FACING,out),F);
        }
        for(int x=x0;x<=x1;x++)for(int z=-12;z<=-8;z++){
            var deck=b.offset(x,-1,z);if(BuildBlocks.state(l,deck).is(Blocks.SPRUCE_PLANKS))BuildBlocks.set(l,deck,RoomShell.CABIN_PORCH.get().defaultBlockState(),F);
            var roof=b.offset(x,6,z);if(BuildBlocks.state(l,roof).is(Blocks.SPRUCE_SLAB))BuildBlocks.set(l,roof,RoomShell.CABIN_SHAKE_SLAB.get().defaultBlockState(),F);
        }
    }
    /** Glass panes from {@code from} to {@code to} along a wall, three high, in a stripped spruce frame. */
    private static void glaze(ServerLevel l,BlockPos b,boolean alongX,int wall,int from,int to){
        for(int a=from-1;a<=to+1;a++)for(int y=0;y<=4;y++){boolean frame=a<from||a>to||y==0||y==4;var at=alongX?b.offset(a,y,wall):b.offset(wall,y,a);
            if(frame){if(y>0)BuildBlocks.set(l,at,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),F);}else BuildBlocks.set(l,at,Blocks.GLASS_PANE.defaultBlockState(),F);}
    }
    /** The way out: a small shed at the edge of the yard, built against the House's own door at its back. */
    private static void shed(ServerLevel l,BlockPos b){
        box(l,b,-3,-1,-4,3,-1,0,Blocks.SPRUCE_PLANKS);box(l,b,-3,0,-4,3,3,0,Blocks.AIR);
        for(int z=-4;z<=0;z++)for(int x:new int[]{-3,3})box(l,b,x,0,z,x,3,z,Blocks.SPRUCE_PLANKS);box(l,b,-3,0,-4,3,3,-4,Blocks.SPRUCE_PLANKS);
        for(int x:new int[]{-3,3})box(l,b,x,0,-4,x,3,-4,Blocks.STRIPPED_SPRUCE_LOG);box(l,b,-2,4,-4,2,4,-4,Blocks.SPRUCE_PLANKS);box(l,b,-1,5,-4,1,5,-4,Blocks.SPRUCE_PLANKS);
        box(l,b,0,0,-4,0,1,-4,Blocks.AIR);NovelRooms.door(l,b.offset(0,0,-4),Direction.NORTH,Blocks.SPRUCE_DOOR,false);
        for(int z=-5;z<=0;z++){
            for(int x:new int[]{-4,-3})BuildBlocks.set(l,b.offset(x,x==-4?3:4,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST),F);
            for(int x:new int[]{3,4})BuildBlocks.set(l,b.offset(x,x==4?3:4,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST),F);
            for(int x:new int[]{-2,-1})BuildBlocks.set(l,b.offset(x,5,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST),F);
            for(int x:new int[]{1,2})BuildBlocks.set(l,b.offset(x,5,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST),F);
            BuildBlocks.set(l,b.offset(0,6,z),Blocks.DARK_OAK_SLAB.defaultBlockState(),F);
        }
        for(int x:new int[]{-3,3})BuildBlocks.set(l,b.offset(x,1,-2),Blocks.GLASS_PANE.defaultBlockState(),F);
        at(l,b,-2,0,-1,Blocks.CRAFTING_TABLE);at(l,b,2,0,-1,Blocks.OAK_LOG);at(l,b,2,1,-1,Blocks.OAK_LOG);at(l,b,-2,0,-3,Blocks.COMPOSTER);
        at(l,b,0,5,-2,Blocks.CHAIN);at(l,b,0,4,-2,Blocks.CHAIN);BuildBlocks.set(l,b.offset(0,3,-2),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),F);
    }
    private static void copyLanding(ServerLevel l,BlockPos b,LabyrinthPlace p){box(l,b,-5,-1,-8,5,-1,0,Blocks.SMOOTH_STONE);room(l,b,-5,5,-8,0,0,5,Blocks.GRAY_TERRACOTTA,Blocks.SMOOTH_STONE);}
}
