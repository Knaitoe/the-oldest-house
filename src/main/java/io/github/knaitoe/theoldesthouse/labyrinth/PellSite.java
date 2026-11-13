package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.518, the Pell house (docs/PELL_HOUSE_PROPOSAL.md): a cul-de-sac in a dry outer suburb at the end of summer, under the outside
 * sky. Every position is relative to the base, the ground just inside the door the reader comes through; north is -z; people stand
 * at y 0 on ground at y -1.
 *
 * <p>Out of the House's door, in a brick wall across the mouth of the street, a road runs north between four closed houses with brown
 * lawns to a turning circle. At its head is June Pell's lot, forty blocks wide, the only green lawn in the street. A path goes up
 * the front garden to a security screen door. The bungalow is one storey of brick veneer: Nessa's room, Callum's room, the bathroom
 * and June's room along the front; a hall across the middle; the kitchen, the living room and the laundry along the back. A driveway
 * runs down the east side, past the house, to a garage standing by itself in the back corner: the older sibling's room. The pool is
 * the yard's west half behind its own fence, the shed is against the back fence, and between the shed's back and the fence is the
 * strip only the garage's window sees.
 *
 * <p>The ground, the walls and the doors (0.4.518); what stands in the rooms (0.4.522); the line round the lot, painted into the
 * ground it crosses (0.4.523). The people come after.
 */
public final class PellSite {
    private PellSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PELL_HOUSE;
    public static final int WEST=-30,EAST=30,NORTH=-101;
    /** The street: the road between its curbs, the footpaths, the neighbours' fronts; where the straight ends; the turning circle. */
    public static final int ROAD=3,CURB=4,PATH=6,FRONT=12,STRAIGHT=-30,CIRCLE_Z=-38,CIRCLE_R=9;
    /** June's lot: its sides, the front boundary at the curb, and its back fence. */
    public static final int LOT_W=-20,LOT_E=20,LOT_S=-50,LOT_N=-98;
    /** The house's outer walls, the wall between the front rooms and the hall, and the wall between the hall and the back rooms. */
    public static final int HOUSE_W=-14,HOUSE_E=10,HOUSE_S=-58,HOUSE_N=-72,HALL_S=-64,HALL_N=-67,ROOF=3;
    /** The driveway down the east side, the garage at its end, and the shed. */
    public static final int DRIVE_W=12,DRIVE_E=18,GARAGE_W=12,GARAGE_E=18,GARAGE_S=-86,GARAGE_N=-96,SHED_W=4,SHED_E=9,SHED_S=-90,SHED_N=-94;
    /** The pool's fence (its south run and its east run; the lot's fences are its other two sides), and the water. */
    public static final int POOL_FENCE_S=-76,POOL_FENCE_E=2,WATER_W=-11,WATER_E=-3,WATER_S=-80,WATER_N=-93,DEEP_S=-86,SHALLOW=-3,DEEP=-5;
    /** 0.4.523: the deck along the pool's west side, where the chair stands. */
    public static final int DECK_W=-19,DECK_E=-13,DECK_S=-77,DECK_N=-96;
    /**
     * 0.4.523, the line (PellLine): its four runs. Along the front at the step, under the mat; up the west side under the side gate;
     * along the back fence, behind the shed; down the east side, through the kennel. The drive's wheels ran at the two cells WHEELS.
     */
    public static final int LINE_W=-17,LINE_E=19,LINE_S=-57,LINE_N=-97;
    public static final int[] WHEELS={14,16};
    /** The front door (the screen), June's door, and the rest of the house's doors: each a lower cell and the way its outside faces. */
    public static final BlockPos FRONT_DOOR=new BlockPos(0,0,-58),NESSA_DOOR=new BlockPos(-10,0,-64),CALLUM_DOOR=new BlockPos(-4,0,-64),BATH_DOOR=new BlockPos(4,0,-64),
        JUNE_DOOR=new BlockPos(8,0,-64),LAUNDRY_DOOR=new BlockPos(7,0,-67),BACK_DOOR=new BlockPos(8,0,-72),SLIDER=new BlockPos(0,0,-72),
        GARAGE_DOOR=new BlockPos(13,0,-86),SHED_DOOR=new BlockPos(6,0,-90),SIDE_GATE=new BlockPos(-17,0,-58),POOL_GATE=new BlockPos(-4,0,-76);
    /** 0.4.533: the pool's details: the band's missing tile (the shallow end's east wall), its odd one (the deep end's far wall), and the
     *  deep end's drain. */
    public static final BlockPos BAND_MISSING=new BlockPos(-2,-2,-84),BAND_ODD=new BlockPos(-6,-2,-94),DRAIN=new BlockPos(-7,-5,-90);
    /** The garage's window, in its west wall: the only one that looks along the strip behind the shed. */
    public static final BlockPos GARAGE_WINDOW=new BlockPos(12,1,-95);
    /** The way on: a door in the brick wall across the street's mouth, west of the one the reader came by. */
    public static final BlockPos ONWARD=new BlockPos(-6,0,1);
    /** Where the first paper is (the letterbox's place, by the path at the curb) and where each reader's account waits (the doormat). */
    public static final BlockPos SOURCE=new BlockPos(2,0,-51),ENDING=new BlockPos(0,0,-57);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    private static boolean in(Vec3 r,double x0,double z0,double x1,double z1){return r.x>=x0&&r.x<x1+1&&r.z>=z0&&r.z<z1+1;}
    /** Inside the house's walls. */
    public static boolean inHouse(Vec3 r){return in(r,HOUSE_W+1,HOUSE_N+1,HOUSE_E-1,HOUSE_S-1)&&r.y>-.5&&r.y<ROOF;}
    /** Inside the garage. */
    public static boolean inGarage(Vec3 r){return in(r,GARAGE_W+1,GARAGE_N+1,GARAGE_E-1,GARAGE_S-1)&&r.y>-.5&&r.y<ROOF;}
    public static boolean inShed(Vec3 r){return in(r,SHED_W+1,SHED_N+1,SHED_E-1,SHED_S-1)&&r.y>-.5&&r.y<ROOF;}
    /** Inside the pool's fence. */
    public static boolean inPoolYard(Vec3 r){return in(r,LOT_W+1,LOT_N+1,POOL_FENCE_E-1,POOL_FENCE_S-1);}
    /** In the pool's water. */
    public static boolean inWater(Vec3 r){return in(r,WATER_W,WATER_N,WATER_E,WATER_S)&&r.y<0;}
    /** On June's lot, front or back. */
    public static boolean onLot(Vec3 r){return in(r,LOT_W,LOT_N,LOT_E,LOT_S);}
    /** Behind the house: the patio, the yard, the pool, the shed, the garage. */
    public static boolean inBackYard(Vec3 r){return onLot(r)&&r.z<HOUSE_N;}
    /** The strip between the shed's back and the back fence. */
    public static boolean behindShed(Vec3 r){return in(r,SHED_W-1,LOT_N+1,GARAGE_W-1,SHED_N-1);}
    /** Out on the street, off the lot. */
    public static boolean inStreet(Vec3 r){return r.z>LOT_S&&r.z<1.2&&r.x>WEST&&r.x<EAST+1;}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,Math.min(x0,x1),Math.min(y0,y1),Math.min(z0,z1),Math.max(x0,x1),Math.max(y0,y1),Math.max(z0,z1),s);}
    static BlockState mat(String name){return PellRegistry.material(name).defaultBlockState();}
    static BlockState fit(PellBlock.Kind kind,Direction facing,int stage){return PellBlock.of(kind,facing,stage);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    public static void build(ServerLevel l,BlockPos b){
        box(l,b,WEST,-7,NORTH,EAST,14,0,AIR);
        box(l,b,WEST,-7,NORTH,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,NORTH,EAST,-1,0,mat("lawn_dry"));
        street(l,b);
        neighbours(l,b);
        lot(l,b);
        house(l,b);
        garage(l,b);
        shed(l,b);
        poolYard(l,b);
        pool(l,b,true);
        fences(l,b);
        doors(l,b);
        set(l,b,SOURCE,fit(PellBlock.Kind.LETTERBOX,Direction.SOUTH,0));
        set(l,b,ENDING,fit(PellBlock.Kind.DOORMAT,Direction.SOUTH,0));
        things(l,b);
        line(l,b);
    }
    /**
     * The House's door is in a brick substation that stands across the street's mouth: its front, where the vestibule behind the
     * door has been built, bricked up to its top and capped.
     */
    static void substation(ServerLevel l,BlockPos b){
        int half=LabyrinthPlaces.VESTIBULE_HALF_WIDTH,top=LabyrinthPlaces.VESTIBULE_TOP;
        box(l,b,-half,1,1,half,top,1,mat("brick"));box(l,b,-half,0,1,half,0,1,mat("brick_low"));
        box(l,b,-half,top+1,1,half,top+1,1,mat("fascia"));
    }

    /** The road between its curbs, the footpaths, the turning circle, and the brick wall across the street's mouth. */
    static void street(ServerLevel l,BlockPos b){
        box(l,b,-PATH,-1,STRAIGHT,PATH,-1,0,mat("concrete"));
        box(l,b,-CURB,-1,STRAIGHT,CURB,-1,0,mat("curb"));
        box(l,b,-ROAD,-1,STRAIGHT,ROAD,-1,0,mat("asphalt"));
        // The circle: road to its radius, a ring of curb, a ring of footpath.
        for(int x=-CIRCLE_R-3;x<=CIRCLE_R+3;x++)for(int z=CIRCLE_Z-CIRCLE_R-3;z<=CIRCLE_Z+CIRCLE_R+3;z++){
            double d=Math.sqrt(x*x+(z-CIRCLE_Z)*(double)(z-CIRCLE_Z));
            if(z>STRAIGHT&&Math.abs(x)<=PATH)continue;
            if(d<=CIRCLE_R+.3)set(l,b,x,-1,z,mat("asphalt"));
            else if(d<=CIRCLE_R+1.3)set(l,b,x,-1,z,mat("curb"));
            else if(d<=CIRCLE_R+3.2)set(l,b,x,-1,z,mat("concrete"));
        }
        // Where the straight meets the circle the road runs through.
        box(l,b,-ROAD,-1,STRAIGHT-2,ROAD,-1,STRAIGHT,mat("asphalt"));
        // The wall the House's door is in, and the way on's: brick, its lowest course stained, a capping course.
        box(l,b,-PATH-3,1,1,PATH+3,4,1,mat("brick"));box(l,b,-PATH-3,0,1,PATH+3,0,1,mat("brick_low"));box(l,b,-PATH-3,5,1,PATH+3,5,1,mat("fascia"));
    }

    /** Four houses, closed up: brick boxes under tiled roofs, blinds down in every window, a door nobody answers, a concrete drive. */
    static void neighbours(ServerLevel l,BlockPos b){
        for(int side:new int[]{-1,1})for(int z0:new int[]{-4,-17}){
            int near=side*FRONT,far=side*(FRONT+11),z1=z0-9;boolean newer=(side<0)==(z0==-4);
            String wall=newer?"brick_new":"brick";
            box(l,b,near,0,z0,far,ROOF,z1,mat(wall));box(l,b,near,0,z0,far,0,z1,mat(newer?"brick_new":"brick_low"));
            box(l,b,near+side,0,z0-1,far-side,2,z1+1,AIR);
            box(l,b,near+side,-1,z0-1,far-side,-1,z1+1,mat("slab"));
            roof(l,b,Math.min(near,far),z1,Math.max(near,far),z0);
            // Its front faces the street: two windows with their blinds down, and a door.
            Direction out=side<0?Direction.EAST:Direction.WEST;
            for(int z:new int[]{z0-2,z0-3,z0-7})set(l,b,near,1,z,fit(PellBlock.Kind.WINDOW,out,1));
            set(l,b,near,0,z0-5,fit(PellBlock.Kind.DOOR,out,0));set(l,b,near,1,z0-5,fit(PellBlock.Kind.DOOR,out,2));
            // A step, a path to the footpath, and a drive beside it.
            box(l,b,side*(PATH+1),-1,z0-5,near-side,-1,z0-5,mat("concrete"));
            box(l,b,side*(PATH+1),-1,z0-8,near-side,-1,z0-9,mat("concrete"));
        }
    }

    /**
     * A low roof over a box whose walls come up to the ceiling's height: the ceiling inside them, eaves a block out all round at
     * that height, tiles over both, and two courses stepping in to a ridge.
     */
    static void roof(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1){
        for(int x=x0-1;x<=x1+1;x++)for(int z=z0-1;z<=z1+1;z++)if(x<x0||x>x1||z<z0||z>z1)set(l,b,x,ROOF,z,fit(PellBlock.Kind.EAVE,Direction.NORTH,0));
        box(l,b,x0+1,ROOF,z0+1,x1-1,ROOF,z1-1,mat("ceiling"));
        box(l,b,x0-1,ROOF+1,z0-1,x1+1,ROOF+1,z1+1,mat("roof"));
        if(x1-x0>=4&&z1-z0>=4)box(l,b,x0+1,ROOF+2,z0+1,x1-1,ROOF+2,z1-1,mat("roof"));
        if(x1-x0>=8&&z1-z0>=8)box(l,b,x0+3,ROOF+3,z0+3,x1-3,ROOF+3,z1-3,mat("roof"));
    }

    /** June's lot: the only watered lawn in the street, the path up the front garden, the drive down the east side, the patio. */
    static void lot(ServerLevel l,BlockPos b){
        box(l,b,LOT_W,-1,LOT_N,LOT_E,-1,LOT_S,mat("lawn"));
        // The curb's footpath runs across the front of the lot.
        box(l,b,LOT_W,-1,LOT_S,LOT_E,-1,LOT_S,mat("concrete"));
        // The path: two slabs wide, from the footpath to the step.
        box(l,b,0,-1,LOT_S,1,-1,HOUSE_S+1,mat("concrete"));
        // The drive, from the garage's doors to the footpath, and its apron across the verge to the circle.
        box(l,b,DRIVE_W,-1,LOT_S,DRIVE_E,-1,GARAGE_S+1,mat("concrete"));
        box(l,b,8,-1,LOT_S+1,DRIVE_E,-1,-43,mat("concrete"));
        // The patio behind the house, pavers from the side passage to the drive.
        box(l,b,LOT_W+1,-1,HOUSE_N-1,DRIVE_W-1,-1,POOL_FENCE_S+1,mat("paver"));
        // A garden bed along the front wall, either side of the step, two rows deep: the line runs along the row by the wall, the
        // stakes stand in the other.
        box(l,b,HOUSE_W,-1,HOUSE_S+1,-1,-1,HOUSE_S+2,mat("mulch"));box(l,b,2,-1,HOUSE_S+1,HOUSE_E,-1,HOUSE_S+2,mat("mulch"));
    }

    /** The bungalow: brick outside, plaster inside; carpet in the hall and the rooms, lino where there is water. */
    static void house(ServerLevel l,BlockPos b){
        // The outside walls are veneer: brick to the weather and plaster to the rooms, the lowest course stained.
        for(int y=0;y<=ROOF;y++)ring(l,b,HOUSE_W,HOUSE_N,HOUSE_E,HOUSE_S,y,y==0?1:0);
        box(l,b,HOUSE_W+1,0,HOUSE_N+1,HOUSE_E-1,2,HOUSE_S-1,AIR);
        box(l,b,HOUSE_W+1,-1,HOUSE_N+1,HOUSE_E-1,-1,HOUSE_S-1,mat("carpet"));
        roof(l,b,HOUSE_W,HOUSE_N,HOUSE_E,HOUSE_S);
        var wall=mat("plaster");
        // The two long walls, and the walls between rooms.
        box(l,b,HOUSE_W+1,0,HALL_S,HOUSE_E-1,2,HALL_S,wall);box(l,b,HOUSE_W+1,0,HALL_N,HOUSE_E-1,2,HALL_N,wall);
        for(int x:new int[]{-7,-1,2,6})box(l,b,x,0,HOUSE_S-1,x,2,HALL_S+1,wall);
        for(int x:new int[]{-6,5})box(l,b,x,0,HALL_N-1,x,2,HOUSE_N+1,wall);
        // The entry runs from the front door to the hall: no wall across it.
        box(l,b,0,0,HALL_S,1,2,HALL_S,AIR);
        // The kitchen and the living room are open to the hall by an arch two wide.
        box(l,b,-10,0,HALL_N,-9,1,HALL_N,AIR);box(l,b,-1,0,HALL_N,0,1,HALL_N,AIR);
        // Lino in the kitchen, the laundry and the bathroom; the hall's carpet worn in one path, from the front door to Nessa's.
        box(l,b,HOUSE_W+1,-1,HALL_N-1,-7,-1,HOUSE_N+1,mat("lino"));box(l,b,6,-1,HALL_N-1,HOUSE_E-1,-1,HOUSE_N+1,mat("lino"));box(l,b,3,-1,HOUSE_S-1,5,-1,HALL_S+1,mat("lino"));
        box(l,b,0,-1,HOUSE_S-1,0,-1,HALL_S,mat("carpet_worn"));box(l,b,NESSA_DOOR.getX(),-1,HALL_S-1,0,-1,HALL_S-1,mat("carpet_worn"));
        // Windows. Along the front: Nessa's (clear), Callum's (painted over from inside), the bathroom's and June's (blinds down).
        for(int x:new int[]{-11,-10})set(l,b,x,1,HOUSE_S,fit(PellBlock.Kind.WINDOW,Direction.SOUTH,0));
        set(l,b,-4,1,HOUSE_S,fit(PellBlock.Kind.WINDOW,Direction.SOUTH,2));
        set(l,b,4,1,HOUSE_S,fit(PellBlock.Kind.WINDOW,Direction.SOUTH,1));set(l,b,8,1,HOUSE_S,fit(PellBlock.Kind.WINDOW,Direction.SOUTH,1));
        // Along the back: the kitchen's, over the sink, onto the pool; and the living room's, where the window seat is.
        for(int x:new int[]{-10,-9})set(l,b,x,1,HOUSE_N,fit(PellBlock.Kind.WINDOW,Direction.NORTH,0));
        for(int x:new int[]{2,3})set(l,b,x,1,HOUSE_N,fit(PellBlock.Kind.WINDOW,Direction.NORTH,0));
        // In the west wall, a window bricked up: newer bricks that do not match.
        for(int z:new int[]{-62,-61})set(l,b,HOUSE_W,1,z,fit(PellBlock.Kind.WALL,Direction.WEST,2));
    }
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,PellThing.Kind kind,Direction f,int stage){set(l,b,x,y,z,PellThing.of(kind,f,stage));}
    /** A thing two cells wide: half 0 here, half 1 in the cell to its right as one faces it. */
    private static void pair(ServerLevel l,BlockPos b,int x,int y,int z,PellThing.Kind kind,Direction f,int stage0,int stage1){
        var at=new BlockPos(x,y,z);set(l,b,at,PellThing.of(kind,f,stage0));set(l,b,PellThing.right(at,f),PellThing.of(kind,f,stage1));
    }
    /** A thing two cells long: its head here, its foot in the cell it faces. */
    private static void lengthwise(ServerLevel l,BlockPos b,int x,int y,int z,PellThing.Kind kind,Direction f){
        var at=new BlockPos(x,y,z);set(l,b,at,PellThing.of(kind,f,0));set(l,b,PellThing.ahead(at,f),PellThing.of(kind,f,1));
    }
    /**
     * 0.4.522, what is in each room as it is on the first visit (docs/PELL_HOUSE_THINGS_0_4_522.md, the proposal's "The house, room
     * by room"). The marks on the floors and Nessa's ceiling are materials of the house's own.
     */
    static void things(ServerLevel l,BlockPos b){
        Direction n=Direction.NORTH,s=Direction.SOUTH,e=Direction.EAST,w=Direction.WEST;
        // The kitchen: the fridge in the corner, the benches along the back wall with the sink under the window onto the pool, the
        // cupboards over them; the table and four chairs; the chore chart and the calendar on the west wall; the lino worn from the
        // door to the sink, and burnt in front of the stove.
        put(l,b,-13,0,-71,PellThing.Kind.FRIDGE,s,0);put(l,b,-13,1,-71,PellThing.Kind.FRIDGE_TOP,s,0);
        put(l,b,-12,0,-71,PellThing.Kind.LUNCHBOXES,s,0);put(l,b,-11,0,-71,PellThing.Kind.KNIFE_BLOCK,s,0);put(l,b,-10,0,-71,PellThing.Kind.SINK,s,0);
        put(l,b,-9,0,-71,PellThing.Kind.COUNTER,s,0);put(l,b,-8,0,-71,PellThing.Kind.STOVE,s,0);put(l,b,-7,0,-71,PellThing.Kind.COUNTER,s,0);
        for(int x:new int[]{-13,-12,-11,-8,-7})put(l,b,x,2,-71,PellThing.Kind.CUPBOARD_UPPER,s,0);
        pair(l,b,-11,0,-69,PellThing.Kind.TABLE,n,0,1);
        put(l,b,-12,0,-70,PellThing.Kind.CHAIR,s,0);put(l,b,-11,0,-70,PellThing.Kind.CHAIR,s,0);put(l,b,-12,0,-68,PellThing.Kind.CHAIR,n,0);put(l,b,-11,0,-68,PellThing.Kind.CHAIR,n,0);
        pair(l,b,-13,1,-69,PellThing.Kind.CHORE_CHART,e,0,1);put(l,b,-13,1,-68,PellThing.Kind.CALENDAR,e,0);
        // 0.4.532: the telephone, on the east wall by the arch, a strip of tape beside it with GAIL and a number on it in pencil.
        put(l,b,PellCall.PHONE.getX(),PellCall.PHONE.getY(),PellCall.PHONE.getZ(),PellThing.Kind.PHONE,w,0);
        for(int z=-70;z<=-68;z++)set(l,b,-10,-1,z,mat("lino_worn"));
        set(l,b,-8,-1,-70,mat("lino_burn"));
        // The living room: the television against the west wall with the recliner turned to it, the side table at its arm and the
        // standard lamp behind that, off; the window seat under the window onto the pool, the mantel on the east wall.
        put(l,b,-5,0,-70,PellThing.Kind.TV_CABINET,e,0);put(l,b,-2,0,-70,PellThing.Kind.RECLINER,w,0);
        put(l,b,-2,0,-71,PellThing.Kind.SIDE_TABLE,s,0);put(l,b,-3,0,-71,PellThing.Kind.FLOOR_LAMP,s,0);
        put(l,b,2,0,-71,PellThing.Kind.WINDOW_SEAT,s,0);put(l,b,3,0,-71,PellThing.Kind.WINDOW_SEAT,s,0);
        put(l,b,4,0,-69,PellThing.Kind.MANTEL,w,0);
        // The hall: the school photographs along the back wall, a year to each, read left to right; after the last, the empty
        // hooks; inside the front door, the hook board.
        for(int i=0;i<4;i++)put(l,b,-8+i,1,-66,PellThing.Kind.PHOTOS,s,i);
        for(int x=-4;x<=-2;x++)put(l,b,x,1,-66,PellThing.Kind.HOOKS,s,0);
        put(l,b,1,1,-60,PellThing.Kind.COAT_HOOK,w,0);
        // Nessa's room: the bed with its head to the west wall; the drawers with the clock, the desk under the window, the large
        // books, the wardrobe; the taped switch and the height marks by the door; the border round the top of the walls, put back
        // crooked behind the bed; the stars on the ceiling.
        lengthwise(l,b,-13,0,-61,PellThing.Kind.BED,e);
        put(l,b,-8,0,-63,PellThing.Kind.DRAWERS,s,0);put(l,b,-11,0,-59,PellThing.Kind.DESK,n,0);put(l,b,-13,0,-63,PellThing.Kind.BOOKSHELF,e,0);
        put(l,b,-8,0,-59,PellThing.Kind.WARDROBE,w,0);put(l,b,-8,1,-59,PellThing.Kind.WARDROBE,w,1);
        put(l,b,-9,1,-63,PellThing.Kind.SWITCH,s,0);put(l,b,-11,0,-63,PellThing.Kind.MARKS,s,0);put(l,b,-11,1,-63,PellThing.Kind.MARKS,s,3);
        for(int z=-63;z<=-59;z++)put(l,b,-13,2,z,PellThing.Kind.BORDER,e,z>=-62&&z<=-60?1:0);
        for(int x=-12;x<=-8;x++)put(l,b,x,2,-63,PellThing.Kind.BORDER,s,0);
        for(int x=-12;x<=-9;x++)put(l,b,x,2,-59,PellThing.Kind.BORDER,n,0);
        for(int z=-62;z<=-59;z++)put(l,b,-8,2,z,PellThing.Kind.BORDER,w,0);
        box(l,b,-13,ROOF,-63,-8,ROOF,-59,mat("ceiling_stars"));
        // Callum's room: the circle painted on the floor with the toys on its line, the mattress inside it; the shoes at the door;
        // the drawer of cutlery.
        for(int i=0;i<12;i++)set(l,b,-5+i%3,-1,-62+i/3,PellThing.circle(i));
        lengthwise(l,b,-4,0,-60,PellThing.Kind.MATTRESS,n);
        put(l,b,-5,0,-63,PellThing.Kind.SHOES,s,0);put(l,b,-6,0,-61,PellThing.Kind.CUTLERY,e,0);
        // The bathroom: the bath under the window, the basin and its mirror, the toilet.
        lengthwise(l,b,5,0,-59,PellThing.Kind.BATH,w);
        put(l,b,3,0,-61,PellThing.Kind.VANITY,e,0);put(l,b,3,1,-61,PellThing.Kind.MIRROR,e,0);put(l,b,5,0,-62,PellThing.Kind.TOILET,w,0);
        // The laundry: the freezer against the east wall, its cord taped across the floor from the power point; the stool by it;
        // the trough and the washing machine; the bucket and the chlorine.
        pair(l,b,9,0,-70,PellThing.Kind.FREEZER,w,0,1);
        put(l,b,6,0,-70,PellThing.Kind.POWER,e,0);put(l,b,7,0,-70,PellThing.Kind.CORD,e,0);put(l,b,8,0,-70,PellThing.Kind.CORD,e,0);
        put(l,b,8,0,-69,PellThing.Kind.STOOL,e,0);put(l,b,6,0,-71,PellThing.Kind.TROUGH,s,0);put(l,b,7,0,-71,PellThing.Kind.WASHER,s,0);
        put(l,b,6,0,-68,PellThing.Kind.BUCKET,s,0);put(l,b,9,0,-68,PellThing.Kind.CHLORINE,w,0);
        // The garage: the camp bed over the oil stain, the bulb over it; the three boxes along the east wall, read from the room left
        // to right, WINTER, BOOKS, then DON'T by the camp bed's head; the pegboard.
        lengthwise(l,b,15,0,-89,PellThing.Kind.CAMP_BED,n);put(l,b,15,2,-89,PellThing.Kind.BULB,n,0);
        pair(l,b,17,0,-93,PellThing.Kind.BOX_WINTER,w,0,1);pair(l,b,17,0,-91,PellThing.Kind.BOX_BOOKS,w,0,1);pair(l,b,17,0,-89,PellThing.Kind.BOX_DONT,w,0,1);
        pair(l,b,14,1,-95,PellThing.Kind.PEGBOARD,s,0,1);
        // The shed: the tapes along the back wall, the workbench with the television, the deck and the tin on it and the empty tins
        // under it; the transcript on the east wall, the calendar on the west.
        for(int x=5;x<=6;x++){put(l,b,x,0,-93,PellThing.Kind.TAPES,s,0);put(l,b,x,1,-93,PellThing.Kind.TAPES,s,1);}
        put(l,b,7,0,-93,PellThing.Kind.WORKBENCH,s,1);put(l,b,8,0,-93,PellThing.Kind.WORKBENCH,s,0);put(l,b,7,1,-93,PellThing.Kind.SHED_TV,s,0);put(l,b,8,1,-93,PellThing.Kind.TIN,s,0);
        put(l,b,8,1,-91,PellThing.Kind.TRANSCRIPT,w,0);put(l,b,8,1,-92,PellThing.Kind.TRANSCRIPT,w,1);put(l,b,8,2,-92,PellThing.Kind.TRANSCRIPT,w,2);
        put(l,b,5,1,-92,PellThing.Kind.SHED_CALENDAR,e,0);
        // Outside: the stakes in the front bed, the newest nearest the step; the kennel and its bowl by the drive; the pool chair
        // turned to the water and the pump; the handprint in the path, and the stain on the drive. The kennel is two cells wide, so
        // its name reads over the opening.
        for(int x=-12;x<=-4;x+=2)put(l,b,x,0,-56,PellThing.Kind.STAKE,s,0);
        put(l,b,-2,0,-56,PellThing.Kind.STAKE,s,1);
        pair(l,b,19,0,-80,PellThing.Kind.KENNEL,w,0,1);put(l,b,18,0,-79,PellThing.Kind.BOWL,w,0);
        put(l,b,-14,0,-86,PellThing.Kind.POOL_CHAIR,e,0);put(l,b,1,0,-90,PellThing.Kind.PUMP,w,0);
        set(l,b,1,-1,-54,mat("concrete_print"));set(l,b,17,-1,-68,mat("concrete_oil"));
    }

    /**
     * 0.4.523, the line (docs/PELL_HOUSE_LINE_0_4_523.md): painted round the whole lot into whatever ground it crosses, unbroken, as on
     * the first visit. It crosses the path at the step, under the mat; goes under the side gate; over the pool fence's footing twice;
     * across the deck and the pavers; behind the shed, where it is thinnest; down the east side through the kennel; and across the
     * drive, where the tyres' marks on it have been painted over.
     */
    static void line(ServerLevel l,BlockPos b){
        var route=route();int n=route.size();
        for(int i=0;i<n;i++){
            var at=route.get(i);
            var state=PellLine.of(toward(at,route.get((i+n-1)%n)),toward(at,route.get((i+1)%n)),surface(l,b,at),PellLine.FIRST,thin(at));
            set(l,b,at.getX(),-1,at.getZ(),state);
        }
    }
    /**
     * The line's cells in order, at the height people stand (the paint is in the ground under each): up the west side from the front's
     * west corner, along the back, down the east side, and back along the front, under the mat at the step.
     */
    public static List<BlockPos> route(){
        var out=new ArrayList<BlockPos>();
        for(int z=LINE_S;z>LINE_N;z--)out.add(new BlockPos(LINE_W,0,z));
        for(int x=LINE_W;x<LINE_E;x++)out.add(new BlockPos(x,0,LINE_N));
        for(int z=LINE_N;z<LINE_S;z++)out.add(new BlockPos(LINE_E,0,z));
        for(int x=LINE_E;x>LINE_W;x--)out.add(new BlockPos(x,0,LINE_S));
        return out;
    }
    /** 0.4.533: the whole line as one stage (the first visit's, or a week on, gone over again), each cell keeping its shape and what it lies on. */
    public static void repaint(ServerLevel l,BlockPos b,int stage){
        for(var at:route()){
            var cell=b.offset(at.getX(),-1,at.getZ());var s=l.getBlockState(cell);
            if(PellLine.is(s)&&s.getValue(PellLine.STAGE)!=stage)l.setBlock(cell,s.setValue(PellLine.STAGE,stage),3);
        }
    }
    /** Whether a cell is on the line's route. */
    public static boolean onLine(BlockPos at){
        int x=at.getX(),z=at.getZ();
        return (x==LINE_W||x==LINE_E)&&z<=LINE_S&&z>=LINE_N||(z==LINE_S||z==LINE_N)&&x>=LINE_W&&x<=LINE_E;
    }
    /** Behind the shed, the line at its thinnest. */
    public static boolean thin(BlockPos at){return at.getZ()==LINE_N&&at.getX()>=SHED_W&&at.getX()<=SHED_E;}
    /** What the line lies on at a cell of its route: the ground laid there, and on the drive where the wheels ran, their marks. */
    static PellLine.Surface surface(ServerLevel l,BlockPos b,BlockPos at){
        var ground=l.getBlockState(b.offset(at.getX(),-1,at.getZ()));
        boolean wheel=at.getZ()==LINE_S&&Arrays.stream(WHEELS).anyMatch(x->x==at.getX());
        for(var surface:PellLine.Surface.values())if(surface!=PellLine.Surface.TYRE&&ground.is(PellRegistry.material(surface.material())))
            return surface==PellLine.Surface.CONCRETE&&wheel?PellLine.Surface.TYRE:surface;
        return PellLine.Surface.LAWN;
    }
    private static Direction toward(BlockPos from,BlockPos to){int dx=to.getX()-from.getX(),dz=to.getZ()-from.getZ();return dx>0?Direction.EAST:dx<0?Direction.WEST:dz>0?Direction.SOUTH:Direction.NORTH;}

    /** One course of the house's outside wall round a box: each cell turns its brick outward, and the corners have two brick faces. */
    static void ring(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,int y,int stage){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            boolean w=x==x0,e=x==x1,n=z==z0,s=z==z1;if(!(w||e||n||s))continue;
            // A corner's brick is on its north and west faces as drawn: turned so that they are the two faces to the weather.
            if(n&&w)set(l,b,x,y,z,fit(PellBlock.Kind.WALL_CORNER,Direction.NORTH,stage));
            else if(n&&e)set(l,b,x,y,z,fit(PellBlock.Kind.WALL_CORNER,Direction.EAST,stage));
            else if(s&&e)set(l,b,x,y,z,fit(PellBlock.Kind.WALL_CORNER,Direction.SOUTH,stage));
            else if(s&&w)set(l,b,x,y,z,fit(PellBlock.Kind.WALL_CORNER,Direction.WEST,stage));
            else set(l,b,x,y,z,fit(PellBlock.Kind.WALL,n?Direction.NORTH:s?Direction.SOUTH:w?Direction.WEST:Direction.EAST,stage));
        }
    }

    /** The garage, by itself in the back corner: brick, a roller door that is never opened, a walk door, one window. */
    static void garage(ServerLevel l,BlockPos b){
        box(l,b,GARAGE_W,0,GARAGE_N,GARAGE_E,ROOF,GARAGE_S,mat("brick"));box(l,b,GARAGE_W,0,GARAGE_N,GARAGE_E,0,GARAGE_S,mat("brick_low"));
        box(l,b,GARAGE_W+1,0,GARAGE_N+1,GARAGE_E-1,2,GARAGE_S-1,AIR);
        box(l,b,GARAGE_W+1,-1,GARAGE_N+1,GARAGE_E-1,-1,GARAGE_S-1,mat("slab"));
        // Under where the camp bed will stand, oil from the car that used to be kept here.
        set(l,b,15,-1,-90,mat("slab_oil"));
        roof(l,b,GARAGE_W,GARAGE_N,GARAGE_E,GARAGE_S);
        for(int x=15;x<=17;x++)for(int y=0;y<=1;y++)set(l,b,x,y,GARAGE_S,fit(PellBlock.Kind.ROLLER,Direction.SOUTH,0));
        set(l,b,GARAGE_WINDOW,fit(PellBlock.Kind.WINDOW,Direction.WEST,0));
    }

    /** The shed against the back fence: corrugated iron on a slab. */
    static void shed(ServerLevel l,BlockPos b){
        box(l,b,SHED_W,0,SHED_N,SHED_E,2,SHED_S,mat("iron"));
        box(l,b,SHED_W+1,0,SHED_N+1,SHED_E-1,2,SHED_S-1,AIR);
        box(l,b,SHED_W,-1,SHED_N,SHED_E,-1,SHED_S,mat("slab"));
        box(l,b,SHED_W,ROOF,SHED_N,SHED_E,ROOF,SHED_S+1,mat("iron"));
    }

    /**
     * The pool's yard inside its fence: pavers to the fence, and along the pool's west side a deck where the chair stands. Under the
     * fence, its concrete footing. Laid once, so that covering and uncovering the pool never touches the ground round it (the line
     * crosses it).
     */
    static void poolYard(ServerLevel l,BlockPos b){
        box(l,b,LOT_W+1,-1,LOT_N+1,POOL_FENCE_E-1,-1,POOL_FENCE_S-1,mat("paver"));
        box(l,b,DECK_W,-1,DECK_N,DECK_E,-1,DECK_S,mat("deck"));
        box(l,b,LOT_W+1,-1,POOL_FENCE_S,POOL_FENCE_E,-1,POOL_FENCE_S,mat("concrete"));box(l,b,POOL_FENCE_E,-1,LOT_N+1,POOL_FENCE_E,-1,POOL_FENCE_S,mat("concrete"));
    }
    /**
     * The pool. Coping round the water, tiles down the walls with the darker band at the waterline. The south end is shallow and the
     * north end deep. Covered, a tarpaulin lies where the top of the water would be and has sagged in its middle; uncovered, there is
     * water to the brim.
     */
    public static void pool(ServerLevel l,BlockPos b,boolean covered){
        box(l,b,WATER_W-1,-1,WATER_N-1,WATER_E+1,-1,WATER_S+1,mat("coping"));
        // The shell: tile from the bottom up, with the band as its top course; then the shallow end and the deep end cut out of it.
        box(l,b,WATER_W-1,DEEP,WATER_N-1,WATER_E+1,-3,WATER_S+1,mat("pool_tile"));
        box(l,b,WATER_W-1,-2,WATER_N-1,WATER_E+1,-2,WATER_S+1,mat("pool_band"));
        // 0.4.533: in the band, one tile gone and one that does not match; in the deep end's floor, the drain (seen once the cover is off).
        set(l,b,BAND_MISSING,mat("pool_band_missing"));set(l,b,BAND_ODD,mat("pool_band_odd"));set(l,b,DRAIN,mat("pool_drain"));
        box(l,b,WATER_W,SHALLOW+1,DEEP_S+1,WATER_E,-1,WATER_S,AIR);
        box(l,b,WATER_W,DEEP+1,WATER_N,WATER_E,-1,DEEP_S,AIR);
        var water=Blocks.WATER.defaultBlockState();
        for(int x=WATER_W;x<=WATER_E;x++)for(int z=WATER_N;z<=WATER_S;z++){
            for(int y=floor(z)+1;y<=-2;y++)set(l,b,x,y,z,water);
            set(l,b,x,-1,z,covered?cover(x,z):water);
        }
    }
    /**
     * The cover at one cell of the pool (0.4.529). Where it sags, five cells by eight in the middle, it holds one pond: water across
     * the pond's inside (stage 1), the pond's edge along its sides with the dry cloth on the side the cell faces (stage 2), and a
     * rounded corner at each of its corners (stage 3, facing north at the north-west corner, then east, south and west going round).
     * Everywhere else it lies dry (stage 0). The dry cloth and the pond's inside are turned at random by their block states.
     */
    static BlockState cover(int x,int z){
        int w=(WATER_W+WATER_E)/2-2,e=w+4,n=WATER_N+3,s=WATER_S-3;
        if(x<w||x>e||z<n||z>s)return fit(PellBlock.Kind.TARP,Direction.NORTH,0);
        boolean west=x==w,east=x==e,north=z==n,south=z==s;
        if(north&&west)return fit(PellBlock.Kind.TARP,Direction.NORTH,3);
        if(north&&east)return fit(PellBlock.Kind.TARP,Direction.EAST,3);
        if(south&&east)return fit(PellBlock.Kind.TARP,Direction.SOUTH,3);
        if(south&&west)return fit(PellBlock.Kind.TARP,Direction.WEST,3);
        if(north||east||south||west)return fit(PellBlock.Kind.TARP,north?Direction.NORTH:east?Direction.EAST:south?Direction.SOUTH:Direction.WEST,2);
        return fit(PellBlock.Kind.TARP,Direction.NORTH,1);
    }
    /** The pool's floor under a row of its water: the shallow end's, or the deep end's. */
    public static int floor(int z){return z<=DEEP_S?DEEP:SHALLOW;}

    /** The lot's paling fences down both sides and across the back, and the pool's own fence. */
    static void fences(ServerLevel l,BlockPos b){
        for(int y=0;y<=1;y++){
            for(int z=HOUSE_S;z>LOT_N;z--){set(l,b,LOT_W,y,z,fit(PellBlock.Kind.FENCE,Direction.EAST,0));set(l,b,LOT_E,y,z,fit(PellBlock.Kind.FENCE,Direction.EAST,0));}
            for(int x=LOT_W+1;x<LOT_E;x++)set(l,b,x,y,LOT_N,fit(PellBlock.Kind.FENCE,Direction.NORTH,0));
            // The corners: a corner's arms go west and north as it is drawn, so each is turned to its own pair.
            set(l,b,LOT_W,y,LOT_N,fit(PellBlock.Kind.FENCE,Direction.SOUTH,1));set(l,b,LOT_E,y,LOT_N,fit(PellBlock.Kind.FENCE,Direction.WEST,1));
            // Across the side passage, from the fence to the house's corner, with the gate in it.
            for(int x=LOT_W+1;x<HOUSE_W;x++)if(x!=SIDE_GATE.getX())set(l,b,x,y,HOUSE_S,fit(PellBlock.Kind.FENCE,Direction.NORTH,0));
            set(l,b,LOT_W,y,HOUSE_S,fit(PellBlock.Kind.FENCE,Direction.EAST,1));
        }
        // The pool's fence: along the patio from the west fence, then up the yard to the back fence.
        for(int x=LOT_W+1;x<POOL_FENCE_E;x++)if(x!=POOL_GATE.getX())set(l,b,x,0,POOL_FENCE_S,fit(PellBlock.Kind.POOL_FENCE,Direction.NORTH,0));
        for(int z=POOL_FENCE_S-1;z>LOT_N;z--)set(l,b,POOL_FENCE_E,0,z,fit(PellBlock.Kind.POOL_FENCE,Direction.EAST,0));
        set(l,b,POOL_FENCE_E,0,POOL_FENCE_S,fit(PellBlock.Kind.POOL_FENCE,Direction.NORTH,1));
    }

    /** Every door, shut; the side gate shut; the pool's gate shut and padlocked. */
    static void doors(ServerLevel l,BlockPos b){
        leaf(l,b,FRONT_DOOR,PellBlock.Kind.SCREEN,Direction.SOUTH,false);
        for(var at:List.of(NESSA_DOOR,CALLUM_DOOR,BATH_DOOR,JUNE_DOOR))leaf(l,b,at,PellBlock.Kind.DOOR,Direction.NORTH,false);
        leaf(l,b,LAUNDRY_DOOR,PellBlock.Kind.DOOR,Direction.SOUTH,false);
        leaf(l,b,BACK_DOOR,PellBlock.Kind.DOOR,Direction.NORTH,false);
        // The sliding door: its west half is fixed glass, its east half slides.
        leaf(l,b,SLIDER.west(),PellBlock.Kind.SLIDER,Direction.NORTH,false);leaf(l,b,SLIDER,PellBlock.Kind.SLIDER,Direction.NORTH,false);
        // The garage's walk door: its bolt is on the yard's side.
        leaf(l,b,GARAGE_DOOR,PellBlock.Kind.GARAGE_DOOR,Direction.SOUTH,false);
        leaf(l,b,SHED_DOOR,PellBlock.Kind.SHED_DOOR,Direction.SOUTH,false);
        set(l,b,SIDE_GATE,fit(PellBlock.Kind.GATE,Direction.NORTH,0));
        set(l,b,POOL_GATE,fit(PellBlock.Kind.POOL_GATE,Direction.NORTH,1));
    }
    /** A door of two cells, shut or open, in place of whatever wall is there; the course over it is left as it is. */
    public static void leaf(ServerLevel l,BlockPos b,BlockPos at,PellBlock.Kind kind,Direction outside,boolean open){
        set(l,b,at,fit(kind,outside,open?1:0));set(l,b,at.above(),fit(kind,outside,open?3:2));
    }
    /** Whether the door whose lower cell is here stands open. */
    public static boolean open(ServerLevel l,BlockPos b,BlockPos at){return PellBlock.open(l.getBlockState(b.offset(at)));}
    /** The lower cell of the door, gate or slider a cell belongs to, or null if it is none. */
    public static BlockPos doorAt(ServerLevel l,BlockPos b,BlockPos rel){
        var s=l.getBlockState(b.offset(rel));if(!s.is(PellRegistry.FITTING.get()))return null;
        var kind=s.getValue(PellBlock.KIND);
        if(kind.leaf())return (s.getValue(PellBlock.STAGE)&2)==2?rel.below():rel;
        return kind==PellBlock.Kind.GATE||kind==PellBlock.Kind.POOL_GATE?rel:null;
    }
}
