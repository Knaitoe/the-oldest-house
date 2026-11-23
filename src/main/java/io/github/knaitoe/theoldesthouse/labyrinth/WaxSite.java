package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.749, the waxworks (docs/WAXWORKS_0_4_749.md): Merriman's Wax Museum after closing, built whole in its interior slot. North from
 * the House's door: the staff corridor, with the time clock, the notice and the guard's lockers; the security office, its desk under a
 * bank of six monitors and the clock over its far door; the ring corridor round the galleries; inside the ring, four galleries (the
 * Hall of Statesmen, the kitchen, the chamber behind its rope, the workshop); north of the ring, the foyer with the ticket booth and the
 * locked front doors. There are no lights. The screens and the cameras' small lamps are all there is.
 *
 * <p>All positions are relative to the place's base, the floor under the entry door; north is negative z.
 */
public final class WaxSite {
    private WaxSite(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The shell: brick, everything inside it the museum. */
    public static final int WEST=-17,EAST=17,NORTH=-75,TOP=5,BOTTOM=-1;
    /** An area of the museum: its floor cells from (x0,z0) to (x1,z1), its height, and whether it is the public's side or the staff's. */
    public record Area(String name,int x0,int z0,int x1,int z1,int high,boolean publicSide){
        public boolean contains(Vec3 r){return r.x>=x0&&r.x<x1+1&&r.z>=z0&&r.z<z1+1&&r.y>-1&&r.y<high+1;}
    }
    public static final Area CORRIDOR=new Area("corridor",-1,-9,1,0,2,false),OFFICE=new Area("office",-5,-17,5,-11,2,false);
    /** The ring corridor's four runs. */
    public static final Area RING_S=new Area("ring",-16,-21,16,-19,2,true),RING_N=new Area("ring",-16,-61,16,-59,2,true),
        RING_W=new Area("ring",-16,-61,-14,-19,2,true),RING_E=new Area("ring",14,-61,16,-19,2,true);
    /** The five galleries, in the order of their cameras and of the rounds. */
    public static final Area FOYER=new Area("foyer",-6,-73,6,-63,3,true),STATESMEN=new Area("statesmen",-12,-39,-1,-23,3,true),
        KITCHEN=new Area("kitchen",1,-39,12,-23,3,true),CHAMBER=new Area("chamber",-12,-57,-1,-41,3,true),WORKSHOP=new Area("workshop",1,-57,12,-41,3,true);
    public static final List<Area> GALLERIES=List.of(FOYER,STATESMEN,KITCHEN,CHAMBER,WORKSHOP);
    public static final List<Area> AREAS=List.of(CORRIDOR,OFFICE,RING_S,RING_N,RING_W,RING_E,FOYER,STATESMEN,KITCHEN,CHAMBER,WORKSHOP);
    /** The gallery index of somewhere, or -1. The ring is not a gallery. */
    public static int gallery(Vec3 r){for(int i=0;i<GALLERIES.size();i++)if(GALLERIES.get(i).contains(r))return i;return -1;}
    public static boolean inOffice(Vec3 r){return OFFICE.contains(r);}
    public static boolean inside(Vec3 r){for(var a:AREAS)if(a.contains(r))return true;return false;}

    /** The notice pinned by the time clock (the source); the time clock; the two lockers, lower cells. */
    public static final BlockPos NOTICE=new BlockPos(-1,1,-5),TIMECLOCK=new BlockPos(-1,1,-3);
    public static final BlockPos[] LOCKERS={new BlockPos(1,0,-6),new BlockPos(1,0,-7)};
    /** The desk's eight cells along the office's north wall; the log (the account, part 3) and the telephone on two of them. */
    public static final BlockPos[] DESK={new BlockPos(-4,0,-17),new BlockPos(-3,0,-17),new BlockPos(-2,0,-17),new BlockPos(-1,0,-17),new BlockPos(1,0,-17),new BlockPos(2,0,-17),new BlockPos(3,0,-17),new BlockPos(4,0,-17)};
    public static final BlockPos LOG=new BlockPos(1,0,-17),PHONE=new BlockPos(-1,0,-17);
    /** The six monitors standing on the desk, in the order of their feeds: the five galleries, then the ring. */
    public static final BlockPos[] MONITORS={new BlockPos(-4,1,-17),new BlockPos(-3,1,-17),new BlockPos(-2,1,-17),new BlockPos(2,1,-17),new BlockPos(3,1,-17),new BlockPos(4,1,-17)};
    public static final BlockPos[] CHAIRS={new BlockPos(-3,0,-16),new BlockPos(3,0,-16)};
    /** The clock over the office's far door. */
    public static final BlockPos CLOCK=new BlockPos(0,2,-17);
    /** A camera in its corner, and the way it looks: the five galleries' and the ring's, in the order of the monitors. */
    public record Camera(BlockPos at,Direction facing,float yaw){}
    public static final List<Camera> CAMERAS=List.of(
        new Camera(new BlockPos(6,3,-63),Direction.NORTH,135),new Camera(new BlockPos(-12,3,-23),Direction.NORTH,-135),
        new Camera(new BlockPos(12,3,-23),Direction.NORTH,135),new Camera(new BlockPos(-12,3,-57),Direction.SOUTH,-45),
        new Camera(new BlockPos(12,3,-57),Direction.SOUTH,45),new Camera(new BlockPos(16,2,-19),Direction.WEST,90));
    /** The front doors' lower cells, and the painted board over them. */
    public static final BlockPos FRONT_LEFT=new BlockPos(-1,0,-73),FRONT_RIGHT=new BlockPos(0,0,-73);
    /** The way on: a door in the office's west wall, the staff's way out. */
    public static final BlockPos ONWARD=new BlockPos(-6,0,-14);
    /** The ticket booth's two counter cells. */
    public static final BlockPos[] BOOTH={new BlockPos(4,0,-66),new BlockPos(4,0,-67)};

    // ------------------------------------------------------------------------------------------------ the figures
    /**
     * A figure: which gallery it is in, where it stands (a plinth's cell, or the floor of a tableau), whether it stands on a plinth,
     * the way it faces, the plaque that names it (null for the one that has none), and its pose.
     */
    public record Figure(String key,int gallery,BlockPos at,boolean plinth,float yaw,BlockPos plaque,Direction plaqueFacing,int pose){}
    /** Poses (the actor's gesture). */
    public static final int STAND=0,LAPEL=1,POINT=2,DISH=3,SEATED=4,WORKING=5,CANDLE=6,SPADE=7,RISING=8,CLASPED=9,UNFINISHED=10,COVERED=11,TICKETS=12,DOORMAN=13;
    private static float yaw(Direction d){return d.toYRot();}
    private static final Direction N=Direction.NORTH,S=Direction.SOUTH,E=Direction.EAST,W=Direction.WEST;
    public static final List<Figure> FIGURES=List.of(
        // The foyer: the founder by the doors, the doorman, the ticket woman in her booth.
        new Figure("founder",0,new BlockPos(-4,0,-69),true,yaw(E),new BlockPos(-3,0,-69),E,CLASPED),
        new Figure("doorman",0,new BlockPos(3,0,-72),true,yaw(S),new BlockPos(3,0,-71),S,DOORMAN),
        new Figure("tickets",0,new BlockPos(5,0,-66),true,yaw(W),new BlockPos(3,0,-67),W,TICKETS),
        // The Hall of Statesmen: five down the east wall, two on the north. The third of the five stands across from the door.
        new Figure("statesman_lapel",1,new BlockPos(-2,0,-25),true,yaw(W),new BlockPos(-3,0,-25),W,LAPEL),
        new Figure("statesman_scroll",1,new BlockPos(-2,0,-28),true,yaw(W),new BlockPos(-3,0,-28),W,CLASPED),
        new Figure("statesman_mirror",1,new BlockPos(-2,0,-31),true,yaw(W),new BlockPos(-3,0,-31),W,STAND),
        new Figure("statesman_general",1,new BlockPos(-2,0,-34),true,yaw(W),new BlockPos(-3,0,-34),W,POINT),
        new Figure("statesman_judge",1,new BlockPos(-2,0,-37),true,yaw(W),new BlockPos(-3,0,-36),W,CLASPED),
        new Figure("statesman_lady",1,new BlockPos(-9,0,-38),true,yaw(S),new BlockPos(-9,0,-37),S,CLASPED),
        new Figure("statesman_orator",1,new BlockPos(-6,0,-38),true,yaw(S),new BlockPos(-6,0,-37),S,POINT),
        // The kitchen: a family at supper, behind the rope.
        new Figure("father",2,new BlockPos(4,0,-32),false,yaw(S),new BlockPos(10,0,-32),E,SEATED),
        new Figure("mother",2,new BlockPos(6,0,-30),false,yaw(W),new BlockPos(10,0,-30),E,DISH),
        new Figure("daughter",2,new BlockPos(3,0,-30),false,yaw(E),new BlockPos(10,0,-29),E,SEATED),
        new Figure("son",2,new BlockPos(3,0,-31),false,yaw(E),new BlockPos(10,0,-31),E,SEATED),
        // The chamber: a crypt by night, behind its rope.
        new Figure("risen",3,new BlockPos(-3,0,-49),false,yaw(W),new BlockPos(-10,0,-49),W,RISING),
        new Figure("gravedigger",3,new BlockPos(-6,0,-45),false,yaw(W),new BlockPos(-10,0,-46),W,SPADE),
        new Figure("sleepwalker",3,new BlockPos(-6,0,-53),false,yaw(W),new BlockPos(-10,0,-52),W,CANDLE),
        new Figure("hooded",3,new BlockPos(-3,0,-54),false,yaw(W),new BlockPos(-10,0,-54),W,CLASPED),
        new Figure("count",3,new BlockPos(-3,0,-44),false,yaw(W),new BlockPos(-10,0,-44),W,POINT),
        // The workshop: the modeller and his apprentice at work, a figure half made and one under its sheet.
        new Figure("apprentice",4,new BlockPos(3,0,-44),false,yaw(W),new BlockPos(5,0,-44),E,WORKING),
        new Figure("modeller",4,new BlockPos(3,0,-50),false,yaw(W),new BlockPos(5,0,-50),E,WORKING),
        new Figure("unfinished",4,new BlockPos(8,0,-46),true,yaw(E),new BlockPos(9,0,-46),E,UNFINISHED),
        new Figure("covered",4,new BlockPos(8,0,-53),true,yaw(E),new BlockPos(9,0,-53),E,COVERED),
        // The twenty-fourth: in the corner of the Hall of Statesmen, on the floor, with no plinth and no plaque.
        new Figure("stranger",1,new BlockPos(-12,0,-23),false,-135,null,null,STAND));
    /** The figure with no plaque. */
    public static final int STRANGER=23;
    /** The one who copies a reader (part 2), across from the Hall's door. */
    public static final int MIRROR=5;
    /** How many the plaques name. */
    public static final int NAMED=23;
    /** Where a figure's feet are. */
    public static Vec3 feet(Figure f){return new Vec3(f.at().getX()+.5,f.at().getY()+(f.plinth()?WaxBlock.PLINTH_TOP/16.0:0),f.at().getZ()+.5);}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    public static BlockState mat(String name){return WaxRegistry.material(name).defaultBlockState();}
    static BlockState fit(WaxBlock.Kind kind,Direction facing){return WaxBlock.of(kind,facing,0);}
    static BlockState fit(WaxBlock.Kind kind,Direction facing,int stage){return WaxBlock.of(kind,facing,stage);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    public static void build(ServerLevel l,BlockPos b){
        box(l,b,WEST,BOTTOM,NORTH,EAST,TOP,0,mat("brick"));
        for(var a:AREAS)box(l,b,a.x0(),0,a.z0(),a.x1(),a.high(),a.z1(),AIR);
        doorways(l,b);
        var painted=new java.util.HashSet<BlockPos>();
        for(var a:AREAS)face(l,b,a,painted);
        staff(l,b);office(l,b);foyer(l,b);statesmen(l,b);kitchen(l,b);chamber(l,b);workshop(l,b);
        for(var c:CAMERAS)set(l,b,c.at(),fit(WaxBlock.Kind.CAMERA,c.facing()));
        for(var f:FIGURES){
            if(f.plinth())set(l,b,f.at(),fit(WaxBlock.Kind.PLINTH,Direction.NORTH));
            if(f.plaque()!=null)set(l,b,f.plaque(),fit(WaxBlock.Kind.PLAQUE,f.plaqueFacing()));
        }
    }
    /** Every opening between areas, a block thick, as x0,y0,z0,x1,y1,z1: the office's two doors, the galleries' doors onto the ring, the foyer's. */
    static final int[][] DOORWAYS={
        {0,0,-10,0,1,-10},                                    // the staff corridor into the office
        {0,0,-18,0,1,-18},                                    // the office onto the ring
        {-13,0,-32,-13,2,-30},                                // the Hall of Statesmen, from the ring's west run
        {13,0,-32,13,2,-30},                                  // the kitchen, from the east run
        {-13,0,-50,-13,2,-48},                                // the chamber, from the west run
        {13,0,-50,13,2,-48},                                  // the workshop, from the east run
        {-1,0,-62,1,2,-62}};                                  // the foyer, from the ring's north run
    static void doorways(ServerLevel l,BlockPos b){for(var d:DOORWAYS)box(l,b,d[0],d[1],d[2],d[3],d[4],d[5],AIR);}
    /** Whether a cell is open by the plan: inside an area's air or a doorway. The build is worked out from the plan, never read back from the world, which may not have it yet. */
    static boolean open(int x,int y,int z){
        for(var a:AREAS)if(x>=a.x0()&&x<=a.x1()&&z>=a.z0()&&z<=a.z1()&&y>=0&&y<=a.high())return true;
        for(var d:DOORWAYS)if(x>=d[0]&&x<=d[3]&&y>=d[1]&&y<=d[4]&&z>=d[2]&&z<=d[5])return true;
        return false;
    }
    /**
     * Each area's own surfaces, wherever its air meets the brick: the staff side painted block over linoleum under a tiled ceiling;
     * the public side dark panelling to shoulder height and paper above it, parquet in the galleries and the runner in the ring.
     */
    static void face(ServerLevel l,BlockPos b,Area a,java.util.Set<BlockPos> painted){
        boolean ring=a.name().equals("ring");
        var floor=a.publicSide()?mat(ring?"runner":"parquet"):mat("lino");var ceiling=a.publicSide()?mat("plaster"):mat("tile");
        for(int x=a.x0()-1;x<=a.x1()+1;x++)for(int z=a.z0()-1;z<=a.z1()+1;z++){
            boolean in=x>=a.x0()&&x<=a.x1()&&z>=a.z0()&&z<=a.z1();
            if(in){paint(l,b,painted,x,-1,z,floor);paint(l,b,painted,x,a.high()+1,z,ceiling);continue;}
            for(int y=0;y<=a.high();y++)paint(l,b,painted,x,y,z,a.publicSide()?mat(y<=1?"panel":"paper"):mat("block"));
        }
    }
    /** The first area to reach a wall cell paints it; open cells are never painted. */
    private static void paint(ServerLevel l,BlockPos b,java.util.Set<BlockPos> painted,int x,int y,int z,BlockState s){if(!open(x,y,z)&&painted.add(new BlockPos(x,y,z)))set(l,b,x,y,z,s);}

    /** The time clock, the notice beside it, the two lockers; nothing else in a corridor nobody but the guard uses. */
    static void staff(ServerLevel l,BlockPos b){
        set(l,b,TIMECLOCK,fit(WaxBlock.Kind.TIMECLOCK,Direction.EAST));
        set(l,b,NOTICE,fit(WaxBlock.Kind.NOTICE,Direction.EAST));
        for(var at:LOCKERS){set(l,b,at,fit(WaxBlock.Kind.LOCKER,Direction.WEST));set(l,b,at.above(),fit(WaxBlock.Kind.LOCKER_TOP,Direction.WEST));}
    }
    /** The desk under the monitors, the log on it and the telephone, two chairs, the clock over the far door. The screens are on. */
    static void office(ServerLevel l,BlockPos b){
        for(var at:DESK)set(l,b,at,fit(WaxBlock.Kind.DESK,Direction.SOUTH,at.equals(LOG)?1:at.equals(PHONE)?2:0));
        for(var at:MONITORS)set(l,b,at,fit(WaxBlock.Kind.MONITOR,Direction.SOUTH,1));
        for(var at:CHAIRS)set(l,b,at,fit(WaxBlock.Kind.CHAIR,Direction.NORTH));
        set(l,b,CLOCK,fit(WaxBlock.Kind.CLOCK,Direction.SOUTH,0));
    }
    /** The front doors, locked, and the board over them; the ticket booth by the east wall. */
    static void foyer(ServerLevel l,BlockPos b){
        set(l,b,FRONT_LEFT,fit(WaxBlock.Kind.FRONT,Direction.SOUTH,0));set(l,b,FRONT_LEFT.above(),fit(WaxBlock.Kind.FRONT,Direction.SOUTH,1));
        set(l,b,FRONT_RIGHT,fit(WaxBlock.Kind.FRONT,Direction.SOUTH,2));set(l,b,FRONT_RIGHT.above(),fit(WaxBlock.Kind.FRONT,Direction.SOUTH,3));
        set(l,b,FRONT_LEFT.above(2),fit(WaxBlock.Kind.SIGN,Direction.SOUTH,0));set(l,b,FRONT_RIGHT.above(2),fit(WaxBlock.Kind.SIGN,Direction.SOUTH,1));
        for(var at:BOOTH){set(l,b,at,fit(WaxBlock.Kind.BOOTH,Direction.WEST));set(l,b,at.above(),fit(WaxBlock.Kind.BOOTH_TOP,Direction.WEST));}
        // The booth's back and sides, so the ticket woman stands inside it.
        box(l,b,5,0,-65,6,1,-65,mat("panel"));box(l,b,5,0,-68,6,1,-68,mat("panel"));
    }
    /** Nothing in the Hall of Statesmen but its figures: plinths, plaques and the dark. */
    static void statesmen(ServerLevel l,BlockPos b){}
    /** The kitchen behind its rope: the table, the chairs, the dresser, the stove. */
    static void kitchen(ServerLevel l,BlockPos b){
        set(l,b,4,0,-30,fit(WaxBlock.Kind.TABLE,Direction.NORTH,0));set(l,b,5,0,-30,fit(WaxBlock.Kind.TABLE,Direction.NORTH,1));
        set(l,b,4,0,-31,fit(WaxBlock.Kind.TABLE,Direction.NORTH,2));set(l,b,5,0,-31,fit(WaxBlock.Kind.TABLE,Direction.NORTH,3));
        set(l,b,4,0,-32,fit(WaxBlock.Kind.CHAIR,Direction.SOUTH));set(l,b,3,0,-30,fit(WaxBlock.Kind.CHAIR,Direction.EAST));set(l,b,3,0,-31,fit(WaxBlock.Kind.CHAIR,Direction.EAST));
        for(int z:new int[]{-28,-33}){set(l,b,1,0,z,fit(WaxBlock.Kind.DRESSER,Direction.EAST,0));set(l,b,1,1,z,fit(WaxBlock.Kind.DRESSER,Direction.EAST,1));}
        set(l,b,1,0,-31,fit(WaxBlock.Kind.STOVE,Direction.EAST));
        rope(l,b,8,-23,-39);
    }
    /** The chamber behind its rope: the open coffin, the stones. */
    static void chamber(ServerLevel l,BlockPos b){
        set(l,b,-3,0,-49,fit(WaxBlock.Kind.COFFIN,Direction.WEST));set(l,b,-4,0,-49,fit(WaxBlock.Kind.COFFIN_FOOT,Direction.WEST));
        set(l,b,-2,0,-47,fit(WaxBlock.Kind.GRAVESTONE,Direction.WEST));set(l,b,-2,0,-51,fit(WaxBlock.Kind.GRAVESTONE,Direction.WEST));
        rope(l,b,-8,-41,-57);
    }
    /** The workshop: two vats, the bench in three lengths, the shelves of heads. */
    static void workshop(ServerLevel l,BlockPos b){
        for(int z:new int[]{-43,-45})set(l,b,2,0,z,fit(WaxBlock.Kind.VAT,Direction.EAST));
        set(l,b,2,0,-49,fit(WaxBlock.Kind.BENCH,Direction.EAST,1));set(l,b,2,0,-50,fit(WaxBlock.Kind.BENCH,Direction.EAST,0));set(l,b,2,0,-51,fit(WaxBlock.Kind.BENCH,Direction.EAST,2));
        for(int z:new int[]{-54,-55}){set(l,b,2,0,z,fit(WaxBlock.Kind.HEADS,Direction.EAST,0));set(l,b,2,1,z,fit(WaxBlock.Kind.HEADS,Direction.EAST,1));}
    }
    /** A rope run north along x from the south stanchion to the north one: every stanchion carries the rope on to the next but the last. */
    static void rope(ServerLevel l,BlockPos b,int x,int south,int north){
        for(int z=south;z>=north;z--)set(l,b,x,0,z,fit(WaxBlock.Kind.ROPE,Direction.NORTH,z==north?1:0));
    }
}
