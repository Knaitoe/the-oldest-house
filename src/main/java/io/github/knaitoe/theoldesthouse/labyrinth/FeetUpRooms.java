package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.FeetUpBlock.Kind.*;

/**
 * 0.4.200, Feet Up: the family house the children's game is played in, built whole. Every position is relative to the
 * scene's base; the entry door stands at (0,0,1) like every place's.
 *
 * Ground floor (y 0..4): the hall down the middle (x -2..2) with the stair at its back, the front room west and the
 * parlour behind it (the adults' room, its door shut), the dining room east and the kitchen behind that. First floor
 * (standing on y 5, air 6..9): the landing at the back of the hall, the void over the stair with the rail along its east
 * side, the upstairs hall at the front with the hatch; the children's room, the bathroom and the parents' room down the
 * west side. Attic (standing on y 10, air 11..13): the blankets, the lantern, the tally on the gable and the notebook,
 * and the way on at the north gable.
 *
 * The route the game is played over is {@link #ROUTE}, in order, each cell the reader stands on; FeetUpTests measures
 * every step of it against the built collision.
 */
public final class FeetUpRooms {
    private FeetUpRooms(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.FEET_UP;
    public static boolean is(LabyrinthPlace p){return p==PLACE;}
    /** The interior: x -10..10, z -26..0 on the ground floor and -26..-1 above, under the roof at 14. */
    public static final int X0=-10,X1=10,Z0=-26,ROOF=14;
    /** Standing levels: the ground floor's floor is at -1, the first floor's at 5, the attic's at 10. */
    public static final int GROUND=0,FIRST=6,ATTIC=11;
    /** The hall, between its walls at x -3 and 3; the stair rises north from z -10 to -14 and the landing begins at -15. */
    public static final int HALL_X0=-2,HALL_X1=2,STAIR_Z0=-10,STAIR_Z1=-14,LANDING_Z=-15;
    /** The rooms either side: the front room and the parlour west, the dining room and the kitchen east, parted at z -9. */
    public static final int WEST_X0=-10,WEST_X1=-4,EAST_X0=4,EAST_X1=10,FRONT_Z0=-8,PART_Z=-9,BACK_Z1=-10;
    /** The arches the game goes through, four high so a jump through them is never cut short: cells in the walls. */
    public static final int ARCH_HEIGHT=4;
    /** The parlour's door onto the hall, the adults behind it. */
    public static final BlockPos PARLOUR_DOOR=new BlockPos(-3,0,-12);
    /** The rules page on the hall table (the source) and the notebook under the attic lantern (the account). */
    public static final BlockPos RULES=new BlockPos(0,1,-5),NOTEBOOK_AT=new BlockPos(0,12,-22);
    /** The bases, in order: the big chair, the sideboard, the window seat, the children's bed, the top of the linen press. */
    public static final BlockPos BIG_CHAIR=new BlockPos(-4,0,-5),SIDEBOARD=new BlockPos(10,0,-4),WINDOW_SEAT_AT=new BlockPos(1,6,-26),BED=new BlockPos(-6,6,-26),PRESS_TOP=new BlockPos(2,7,-4);
    public static final List<BlockPos> BASES=List.of(BIG_CHAIR,SIDEBOARD,WINDOW_SEAT_AT,BED,PRESS_TOP);
    /** The two armchairs that come closer while nobody looks: where each stands at first and where it stops. */
    public static final BlockPos HELP_A_FAR=new BlockPos(-5,0,-1),HELP_A_NEAR=new BlockPos(-6,0,-1),HELP_B_FAR=new BlockPos(10,0,-8),HELP_B_NEAR=new BlockPos(10,0,-7);
    /** The fan over the dining table, its blades a little above a standing reader's head. */
    public static final BlockPos FAN=new BlockPos(7,2,-4);
    /** The fire in the front room's chimney breast, and the stove in the kitchen. */
    public static final BlockPos FIRE=new BlockPos(-10,0,-2),STOVE_AT=new BlockPos(10,0,-14);
    /** The dumbwaiter: its shaft in the wall between the kitchen and the hall, the cage resting at 2 and rising to 5. */
    public static final int SHAFT_X=3,SHAFT_Z=-18,CAGE_LOW=2,CAGE_HIGH=5;
    /** The rail along the void's east side, fence tops a sneaking reader walks from the landing to the upstairs hall. */
    public static final int RAIL_X=2;
    /** The hatch in the upstairs hall's ceiling, its folding ladder down the wall beside the press. */
    public static final BlockPos HATCH=new BlockPos(2,10,-2);
    public static final int LADDER_Y0=6,LADDER_Y1=9;
    /** The chalk, each with its words (FeetUpTexts.chalk): on walls, facing the room. */
    public record Chalk(BlockPos at,Direction facing,int stage,int line){}
    public static final List<Chalk> CHALK_MARKS=List.of(
        new Chalk(new BlockPos(-4,1,-6),Direction.WEST,FeetUpBlock.CHALK_BASE,0),      // the big chair
        new Chalk(new BlockPos(-5,2,-7),Direction.NORTH,FeetUpBlock.CHALK_ARROW,1),    // JUMP FROM THE ARM NOT THE SEAT
        new Chalk(new BlockPos(-8,2,0),Direction.SOUTH,FeetUpBlock.CHALK_SCRAWL,2),    // DON'T LOOK AT THE CHAIRS
        new Chalk(new BlockPos(10,1,-5),Direction.WEST,FeetUpBlock.CHALK_BASE,0),      // the sideboard
        new Chalk(new BlockPos(1,7,-25),Direction.NORTH,FeetUpBlock.CHALK_BASE,0),     // the window seat (on the back wall)
        new Chalk(new BlockPos(2,7,-15),Direction.WEST,FeetUpBlock.CHALK_SCRAWL,3),    // SNEAK ON THE RAIL
        new Chalk(new BlockPos(-6,7,-25),Direction.NORTH,FeetUpBlock.CHALK_BASE,0),    // the children's bed
        new Chalk(new BlockPos(-7,7,-14),Direction.SOUTH,FeetUpBlock.CHALK_SCRAWL,4),  // THE BOARDS GO. KEEP GOING
        new Chalk(new BlockPos(2,8,-4),Direction.WEST,FeetUpBlock.CHALK_BASE,0),       // the press
        new Chalk(new BlockPos(2,8,-3),Direction.WEST,FeetUpBlock.CHALK_WAIT,5));      // WAIT FOR ME
    /**
     * The required line, each cell the reader stands on, from the big chair to the press, with the helping chairs where
     * they stop. Arches and the ladder are not footings: the game goes through them in the air.
     */
    public static final List<BlockPos> ROUTE=List.of(
        // The front room: the big chair, the footstool, the sofa, the bookcase, the mantel over the fire, the chair that comes, the side table, and back to the big chair.
        BIG_CHAIR,new BlockPos(-6,0,-5),new BlockPos(-7,0,-5),new BlockPos(-8,0,-5),new BlockPos(-9,1,-5),new BlockPos(-9,2,-3),new BlockPos(-9,2,-2),new BlockPos(-9,2,-1),HELP_A_NEAR,new BlockPos(-5,0,-3),BIG_CHAIR,
        // Across the hall on the bench, the table and the stool, through the arch to the dining chair.
        new BlockPos(-2,0,-5),new BlockPos(-1,0,-5),new BlockPos(0,0,-5),new BlockPos(1,0,-5),new BlockPos(2,0,-5),new BlockPos(4,0,-5),
        // The dining table under the fan, the sideboard, the chair that comes, the radiator, and through the arch to the kitchen table.
        new BlockPos(5,0,-4),new BlockPos(6,0,-4),new BlockPos(7,0,-4),new BlockPos(8,0,-4),new BlockPos(9,0,-4),SIDEBOARD,HELP_B_NEAR,new BlockPos(8,0,-8),new BlockPos(7,0,-10),
        // The kitchen: the table, the counters, over the stove, the fridge, the pots and the cage.
        new BlockPos(8,0,-11),new BlockPos(10,0,-12),new BlockPos(10,0,-13),new BlockPos(10,0,-15),new BlockPos(10,0,-16),new BlockPos(10,0,-17),new BlockPos(10,1,-18),
        new BlockPos(9,2,-18),new BlockPos(8,2,-18),new BlockPos(7,2,-18),new BlockPos(6,2,-18),new BlockPos(5,2,-18),new BlockPos(4,2,-18),new BlockPos(SHAFT_X,CAGE_LOW,SHAFT_Z),
        // The landing, from the cage at the top of the shaft: the trunk, the console, the chair, the window seat.
        new BlockPos(SHAFT_X,CAGE_HIGH,SHAFT_Z),new BlockPos(2,6,-18),new BlockPos(1,6,-20),new BlockPos(1,6,-23),WINDOW_SEAT_AT,
        // Back along the landing, the plant stand, then the rail along the void and the boards of the upstairs hall.
        new BlockPos(1,6,-23),new BlockPos(1,6,-20),new BlockPos(2,6,-18),new BlockPos(2,6,-16),
        new BlockPos(2,5,-14),new BlockPos(2,5,-13),new BlockPos(2,5,-12),new BlockPos(2,5,-11),new BlockPos(2,5,-10),
        new BlockPos(2,5,-9),new BlockPos(2,5,-8),new BlockPos(2,5,-7),new BlockPos(2,5,-6),new BlockPos(2,5,-5),new BlockPos(1,6,-4),PRESS_TOP);
    /** The long way round, from the landing through the children's room, the bathroom and the parents' room to the same chair by the press. */
    public static final List<BlockPos> LONG_WAY=List.of(
        new BlockPos(1,6,-20),new BlockPos(-1,6,-20),new BlockPos(-4,6,-20),new BlockPos(-6,6,-21),new BlockPos(-6,6,-23),new BlockPos(-6,6,-25),BED,
        new BlockPos(-6,6,-25),new BlockPos(-8,7,-24),new BlockPos(-9,6,-22),new BlockPos(-8,6,-20),new BlockPos(-7,6,-18),new BlockPos(-7,6,-16),
        new BlockPos(-7,5,-14),new BlockPos(-7,5,-13),new BlockPos(-8,6,-12),new BlockPos(-7,5,-11),new BlockPos(-7,5,-10),
        new BlockPos(-7,5,-8),new BlockPos(-7,6,-6),new BlockPos(-7,6,-5),new BlockPos(-5,6,-4),new BlockPos(-2,5,-4),new BlockPos(-1,5,-4),new BlockPos(0,5,-4),new BlockPos(1,6,-4),PRESS_TOP);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    private static Block mat(String name){return FeetUpRegistry.material(name);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,Block block){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,block.defaultBlockState());}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){BuildBlocks.set(l,b.offset(rel),s,F);}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,FeetUpBlock.Kind k,Direction f){set(l,b,x,y,z,FeetUpBlock.of(k,f));}
    private static void fit(ServerLevel l,BlockPos b,int x,int y,int z,FeetUpBlock.Kind k,Direction f,int stage){set(l,b,x,y,z,FeetUpBlock.of(k,f,stage));}
    private static void furniture(ServerLevel l,BlockPos b,int x,int y,int z,HouseholdFurnitureBlock.Kind k,Direction f){NovelRooms.furniture(l,b.offset(x,y,z),k,f);}
    private static void tall(ServerLevel l,BlockPos b,int x,int y,int z,FeetUpBlock.Kind k,Direction f){fit(l,b,x,y,z,k,f,0);fit(l,b,x,y+1,z,k,f,1);}
    /** A four-high opening in a wall, two cells wide along the wall. */
    private static void arch(ServerLevel l,BlockPos b,int x0,int y,int z0,int x1,int z1){box(l,b,x0,y,z0,x1,y+ARCH_HEIGHT-1,z1,Blocks.AIR);}
    private static BlockState fence(boolean north,boolean south){return Blocks.DARK_OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,north).setValue(FenceBlock.SOUTH,south);}
    static BlockState cage(){return FeetUpBlock.of(CAGE,Direction.WEST);}
    static BlockState ladder(){return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST);}
    static BlockState hatch(boolean open){return Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.BOTTOM).setValue(TrapDoorBlock.FACING,Direction.WEST).setValue(TrapDoorBlock.OPEN,open);}
    /** The armchair that comes across the front room, and the dining chair that comes along the sideboard wall. */
    public static BlockState helperState(){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR).setValue(HouseholdFurnitureBlock.FACING,Direction.SOUTH);}
    public static BlockState chairHelperState(){return HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.CANE_CHAIR).setValue(HouseholdFurnitureBlock.FACING,Direction.NORTH);}

    public static void build(ServerLevel l,BlockPos b){
        // The whole house is papered wall, then every room is carved from it; the floors are laid as the carving goes.
        box(l,b,X0-1,-1,Z0-1,X1+1,ROOF,0,mat("paper"));
        ground(l,b);first(l,b);attic(l,b);
    }

    // ------------------------------------------------------------------------------------------------ the ground floor
    private static void ground(ServerLevel l,BlockPos b){
        // The hall: tiles, the stair up its back, the parlour door in its west wall.
        box(l,b,HALL_X0,0,Z0,HALL_X1,4,0,Blocks.AIR);box(l,b,HALL_X0,-1,Z0,HALL_X1,-1,0,mat("hall_tile"));
        for(int i=0;i<=4;i++){int z=STAIR_Z0-i;if(i>0)box(l,b,-1,0,z,1,i-1,z,mat("board"));box(l,b,-1,i,z,1,i,z,LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,Direction.NORTH));}
        box(l,b,-1,0,LANDING_Z,1,4,Z0,mat("paper_dark"));box(l,b,HALL_X0,0,LANDING_Z,HALL_X1,4,Z0,mat("paper_dark")); // the cupboard under the landing
        box(l,b,-2,0,STAIR_Z0,-2,4,STAIR_Z1,Blocks.AIR);box(l,b,2,0,STAIR_Z0,2,4,STAIR_Z1,Blocks.AIR);
        furniture(l,b,-2,0,-1,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);fit(l,b,-2,1,-1,LAMP,Direction.EAST);
        furniture(l,b,-2,0,-5,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);furniture(l,b,-1,0,-5,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);furniture(l,b,0,0,-5,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);
        furniture(l,b,1,0,-5,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);furniture(l,b,2,0,-5,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);
        set(l,b,RULES,FeetUpBlock.of(RULES_PAGE,Direction.SOUTH));
        NovelRooms.door(l,b.offset(PARLOUR_DOOR),Direction.EAST,Blocks.DARK_OAK_DOOR,false);
        // The front room: boards, the chimney breast with the fire in it, the mantel over it, the bookcases, the seats.
        box(l,b,WEST_X0,0,FRONT_Z0,WEST_X1,4,0,Blocks.AIR);box(l,b,WEST_X0,-1,FRONT_Z0,WEST_X1,-1,0,mat("floor"));
        arch(l,b,-3,0,-5,-3,-4);
        box(l,b,-10,0,-3,-10,4,-1,mat("brick"));set(l,b,FIRE,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.FACING,Direction.EAST));set(l,b,-10,1,-2,Blocks.AIR.defaultBlockState());
        for(int z=-3;z<=-1;z++)set(l,b,-9,2,z,Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        for(int y=0;y<=1;y++)set(l,b,-9,y,-5,Blocks.BOOKSHELF.defaultBlockState());
        furniture(l,b,BIG_CHAIR.getX(),0,BIG_CHAIR.getZ(),HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);
        furniture(l,b,-6,0,-5,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.NORTH);
        furniture(l,b,-7,0,-5,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.NORTH);furniture(l,b,-8,0,-5,HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.NORTH);
        set(l,b,HELP_A_FAR,helperState());
        furniture(l,b,-5,0,-3,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);
        tall(l,b,-10,0,-7,FLOOR_LAMP,Direction.EAST);
        // The parlour behind it: shut, lit, the adults' room; its own door and nothing of the game.
        box(l,b,WEST_X0,0,Z0,WEST_X1,4,BACK_Z1,Blocks.AIR);box(l,b,WEST_X0,-1,Z0,WEST_X1,-1,BACK_Z1,mat("floor"));
        set(l,b,-5,1,-12,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12));set(l,b,-5,1,-13,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12));
        furniture(l,b,-7,0,-13,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.EAST);furniture(l,b,-7,0,-15,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.EAST);furniture(l,b,-9,0,-14,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);
        fit(l,b,-9,1,-14,LAMP,Direction.EAST);
        // The dining room: the long table under the fan, the sideboard, the chair that comes, the radiator under the window.
        box(l,b,EAST_X0,0,FRONT_Z0,EAST_X1,4,0,Blocks.AIR);box(l,b,EAST_X0,-1,FRONT_Z0,EAST_X1,-1,0,mat("floor"));
        arch(l,b,3,0,-5,3,-4);
        furniture(l,b,4,0,-5,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        for(int x=5;x<=9;x++){furniture(l,b,x,0,-4,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);furniture(l,b,x,0,-3,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);furniture(l,b,x,0,-5,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);}
        LiteraryRooms.prop(l,b,FAN,LiteraryPropBlock.Kind.FAN,Direction.NORTH);set(l,b,7,3,-4,Blocks.CHAIN.defaultBlockState());set(l,b,7,4,-4,Blocks.CHAIN.defaultBlockState());
        furniture(l,b,10,0,-3,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);furniture(l,b,SIDEBOARD.getX(),0,SIDEBOARD.getZ(),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);fit(l,b,10,1,-3,LAMP,Direction.WEST);
        set(l,b,HELP_B_FAR,chairHelperState());
        furniture(l,b,8,0,-8,HouseholdFurnitureBlock.Kind.RADIATOR,Direction.NORTH);
        // The kitchen: the table, the counters along the east wall, the stove, the fridge, the rack of pots and the dumbwaiter.
        box(l,b,EAST_X0,0,Z0,EAST_X1,4,BACK_Z1,Blocks.AIR);box(l,b,EAST_X0,-1,Z0,EAST_X1,-1,BACK_Z1,mat("floor"));
        arch(l,b,6,0,PART_Z,7,PART_Z);
        for(int x=7;x<=8;x++)for(int z=-11;z<=-10;z++)furniture(l,b,x,0,z,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.NORTH);
        for(int z=-12;z>=-17;z--)if(z!=STOVE_AT.getZ())fit(l,b,10,0,z,COUNTER,Direction.WEST);
        set(l,b,STOVE_AT,FeetUpBlock.of(STOVE,Direction.WEST));
        tall(l,b,10,0,-18,FRIDGE,Direction.WEST);
        for(int x=4;x<=9;x++){fit(l,b,x,3,-18,POT_RAIL,Direction.EAST);fit(l,b,x,2,-18,POT,Direction.NORTH);}
        fit(l,b,7,4,-14,CEILING_LIGHT,Direction.NORTH);fit(l,b,7,4,-22,CEILING_LIGHT,Direction.NORTH);
        // The shaft: the wall cell hollow from the kitchen floor to above the landing, the cage resting at the bottom of its travel.
        box(l,b,SHAFT_X,0,SHAFT_Z,SHAFT_X,7,SHAFT_Z,Blocks.AIR);set(l,b,SHAFT_X,-1,SHAFT_Z,mat("brick").defaultBlockState());
        set(l,b,SHAFT_X,CAGE_LOW,SHAFT_Z,cage());
    }

    // ------------------------------------------------------------------------------------------------ the first floor
    private static void first(ServerLevel l,BlockPos b){
        // The slab between the floors, whole, and the rooms over it; the void over the stair is cut after.
        box(l,b,X0,5,Z0,X1,5,0,mat("board"));box(l,b,SHAFT_X,5,SHAFT_Z,SHAFT_X,5,SHAFT_Z,Blocks.AIR);
        box(l,b,HALL_X0,6,Z0,HALL_X1,9,-1,Blocks.AIR);
        box(l,b,WEST_X0,6,Z0,WEST_X1,9,-1,Blocks.AIR);box(l,b,EAST_X0,6,Z0,EAST_X1,9,-1,Blocks.AIR);
        // The void over the stair, and the rail along its east side: fence tops from the landing to the upstairs hall.
        box(l,b,-2,5,STAIR_Z0,1,5,STAIR_Z1,Blocks.AIR);
        for(int z=STAIR_Z0;z>=STAIR_Z1;z--)set(l,b,RAIL_X,5,z,fence(z>STAIR_Z1,z<STAIR_Z0));
        // The landing: the trunk by the shaft, the plant stand, the console, the hall chair, the window seat under the back window.
        furniture(l,b,2,6,-18,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        furniture(l,b,2,6,-16,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.WEST);
        furniture(l,b,1,6,-20,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH);furniture(l,b,-1,6,-20,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);
        furniture(l,b,1,6,-23,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        set(l,b,WINDOW_SEAT_AT,FeetUpBlock.of(WINDOW_SEAT,Direction.SOUTH));fit(l,b,-1,6,-26,LAMP,Direction.SOUTH);
        // The upstairs hall: loose boards, but for the press, the chair beside it and the cells under the hatch; the hatch shut, its ladder up.
        for(int x=HALL_X0;x<=HALL_X1;x++)for(int z=-1;z>=-9;z--)set(l,b,x,5,z,FeetUpBlock.of(LOOSE_BOARD,Direction.NORTH));
        for(var fixed:List.of(new BlockPos(2,5,-4),new BlockPos(1,5,-4),new BlockPos(2,5,-2)))set(l,b,fixed,mat("board").defaultBlockState());
        tall(l,b,2,6,-4,PRESS,Direction.WEST);furniture(l,b,1,6,-4,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        fit(l,b,-2,9,-6,CEILING_LIGHT,Direction.NORTH);
        // The children's room, the bathroom and the parents' room down the west side, their arches and their boards.
        box(l,b,WEST_X0,5,-15,WEST_X1,9,-15,mat("paper"));box(l,b,WEST_X0,5,PART_Z,WEST_X1,9,PART_Z,mat("paper"));
        box(l,b,WEST_X0,5,-15,WEST_X1,5,-15,mat("board"));box(l,b,WEST_X0,5,PART_Z,WEST_X1,5,PART_Z,mat("board"));
        arch(l,b,-3,6,-20,-3,-19);arch(l,b,-7,6,-15,-6,-15);arch(l,b,-7,6,PART_Z,-6,PART_Z);arch(l,b,-3,6,-5,-3,-4);
        furniture(l,b,-4,6,-20,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.EAST);
        for(int z=-21;z>=-25;z--)set(l,b,-6,6,z,FeetUpBlock.of(MATTRESS,Direction.NORTH));set(l,b,BED,FeetUpBlock.of(MATTRESS,Direction.NORTH));
        tall(l,b,-8,6,-24,PRESS,Direction.EAST);
        // The chest, the footstool, the desk and the toy box before the arch: the long way's footings, each a walking jump from the last.
        furniture(l,b,-9,6,-22,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);furniture(l,b,-8,6,-20,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.EAST);
        furniture(l,b,-7,6,-18,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);furniture(l,b,-7,6,-16,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.SOUTH);
        furniture(l,b,-9,6,-24,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);fit(l,b,-9,7,-24,LAMP,Direction.EAST);
        for(int x=WEST_X0;x<=WEST_X1;x++)for(int z=-10;z>=-14;z--)set(l,b,x,5,z,FeetUpBlock.of(LOOSE_BOARD,Direction.NORTH));
        set(l,b,-9,6,-12,FeetUpBlock.of(BATH,Direction.NORTH,0));set(l,b,-8,6,-12,FeetUpBlock.of(BATH,Direction.NORTH,1)); // the tub's foot and its tap end
        for(int x=WEST_X0;x<=WEST_X1;x++)for(int z=-1;z>=-8;z--)set(l,b,x,5,z,FeetUpBlock.of(LOOSE_BOARD,Direction.NORTH));
        for(var fixed:List.of(new BlockPos(-7,5,-6),new BlockPos(-7,5,-5),new BlockPos(-8,5,-6),new BlockPos(-8,5,-5),new BlockPos(-5,5,-4)))set(l,b,fixed,mat("board").defaultBlockState());
        for(int z=-6;z<=-5;z++){set(l,b,-7,6,z,FeetUpBlock.of(MATTRESS,Direction.EAST));set(l,b,-8,6,z,FeetUpBlock.of(MATTRESS,Direction.EAST));}
        furniture(l,b,-5,6,-4,HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.EAST);fit(l,b,-9,9,-3,CEILING_LIGHT,Direction.NORTH);fit(l,b,-7,9,-12,CEILING_LIGHT,Direction.NORTH);fit(l,b,-7,9,-22,CEILING_LIGHT,Direction.NORTH);
        // The east rooms upstairs are the back of the house: a box room over the kitchen, its door shut, nothing of the game.
        box(l,b,EAST_X0,6,Z0,EAST_X1,9,-1,mat("paper"));
        // The chalk.
        for(var c:CHALK_MARKS)set(l,b,c.at(),FeetUpBlock.of(CHALK,c.facing(),c.stage()));
        set(l,b,HATCH,hatch(false));
    }

    // ------------------------------------------------------------------------------------------------ the attic
    private static void attic(ServerLevel l,BlockPos b){
        box(l,b,X0,10,Z0,X1,10,-1,mat("attic"));
        box(l,b,X0,ATTIC,Z0,X1,ROOF-1,-1,Blocks.AIR);box(l,b,X0-1,ROOF,Z0-1,X1+1,ROOF,0,mat("roof"));
        set(l,b,HATCH,hatch(false));
        // The rafters across under the roof, high enough to stand under, and the boxes the house keeps up here.
        for(int z=-4;z>=Z0+2;z-=4)for(int x=X0;x<=X1;x++)set(l,b,x,ROOF-1,z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        furniture(l,b,-9,11,-12,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.EAST);furniture(l,b,-9,11,-13,HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.EAST);
        furniture(l,b,9,11,-17,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.WEST);furniture(l,b,8,11,-24,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        for(int x=-1;x<=1;x++)set(l,b,x,11,-23,FeetUpBlock.of(BLANKETS,Direction.NORTH));
        furniture(l,b,0,11,-22,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);set(l,b,NOTEBOOK_AT,FeetUpBlock.of(NOTEBOOK,Direction.SOUTH));
        fit(l,b,-1,12,-22,LAMP,Direction.SOUTH,1);
        fit(l,b,-1,12,-26,TALLY,Direction.SOUTH);
        // Cobwebs in the attic's corners (the web hangs across the cell's corner behind its facing) and two under the roof.
        set(l,b,-10,13,-26,FeetUpBlock.of(COBWEB_CORNER,Direction.SOUTH));set(l,b,10,13,-26,FeetUpBlock.of(COBWEB_CORNER,Direction.WEST));
        set(l,b,-10,13,-1,FeetUpBlock.of(COBWEB_CORNER,Direction.EAST));set(l,b,10,13,-1,FeetUpBlock.of(COBWEB_CORNER,Direction.NORTH));
        set(l,b,4,13,-2,FeetUpBlock.of(COBWEB_CORNER,Direction.NORTH));set(l,b,-3,13,-10,FeetUpBlock.of(COBWEB_CORNER,Direction.EAST));
        set(l,b,0,11,-25,Blocks.COBWEB.defaultBlockState());set(l,b,-4,11,-8,Blocks.COBWEB.defaultBlockState());
    }

    // ------------------------------------------------------------------------------------------------ where things are
    public static AABB bounds(BlockPos b){var r=PLACE.room();return new AABB(Vec3.atLowerCornerOf(b.offset(r.minX(),r.minY(),r.minZ())),Vec3.atLowerCornerOf(b.offset(r.maxX()+1,r.maxY()+1,r.maxZ()+1)));}
    /** The floor, which is lava: the ground floor's boards and tiles, the stair, the first floor's boards that hold, and a fallen board lying downstairs. */
    public static boolean floor(ServerLevel l,BlockPos b,BlockPos at){
        var s=l.getBlockState(at);
        if(s.is(mat("floor"))||s.is(mat("hall_tile"))||s.is(mat("board"))||s.is(Blocks.DARK_OAK_STAIRS))return true;
        return FeetUpBlock.is(s,LOOSE_BOARD)&&at.getY()<b.getY()+5;
    }
    /** The attic: standing on the attic's boards, or anything above them. */
    public static boolean inAttic(BlockPos b,Vec3 at){return at.y>=b.getY()+ATTIC-.01&&inside(b,at);}
    public static boolean inside(BlockPos b,Vec3 at){double x=at.x-b.getX(),z=at.z-b.getZ();return x>=X0-1&&x<X1+2&&z>=Z0-1&&z<2;}
    /** The dining room, where the fan turns. */
    public static boolean inDining(BlockPos b,Vec3 at){double x=at.x-b.getX(),z=at.z-b.getZ();return x>=EAST_X0&&x<EAST_X1+1&&z>=FRONT_Z0&&z<1&&at.y<b.getY()+5;}
    /** Where a reader stands on a base: on its top, in the middle of the cell. */
    public static Vec3 standing(ServerLevel l,BlockPos b,BlockPos base){
        var at=b.offset(base);var shape=l.getBlockState(at).getCollisionShape(l,at);double top=shape.isEmpty()?0:shape.max(Direction.Axis.Y);
        return new Vec3(at.getX()+.5,at.getY()+top+.01,at.getZ()+.5);
    }
}
