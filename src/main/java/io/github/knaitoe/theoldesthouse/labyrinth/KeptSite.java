package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.410, the Kept (docs/THE_KEPT_0_4_410.md): the inside of a machine, built whole in its interior slot. North from the door, along one
 * walk: a corridor of cabinets with screens set into them; a cave of ice the machine has made, badly, out of its own casing; a thicket of
 * wire with one crawl through it; and a dining hall with three long tables and a hundred chairs, a crate of tins at the head of it, and
 * four cells in its side walls whose doors open onto glare.
 *
 * <p>All positions are relative to the place's base, the floor under the entry door; north is negative z.
 */
public final class KeptSite {
    private KeptSite(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The shell: everything inside it is the machine. */
    public static final int WEST=-22,EAST=22,NORTH=-98,TOP=10;
    /** The corridor of cabinets: its width either side of the walk, its far end and its height. */
    public static final int CORRIDOR_HALF=4,CORRIDOR_N=-26,CORRIDOR_HIGH=6;
    /** The ice cave between its walls; the wire room between its walls, with the thicket's own rows; the dining hall between its walls. */
    public static final int CAVE_S=-28,CAVE_N=-49,JUNGLE_S=-51,THICKET_S=-54,THICKET_N=-66,JUNGLE_N=-69,HALL_S=-71,HALL_N=-96,HALL_HALF=14,HALL_HIGH=8;
    /** The printer (the source) and the tray it prints the account into, either side of the corridor near the door. */
    public static final BlockPos PRINTER=new BlockPos(3,0,-4),TRAY=new BlockPos(-3,0,-4);
    /** The crate of tins at the head of the hall, and the way on, in the hall's north wall. */
    public static final BlockPos CRATE=new BlockPos(0,0,-95),ONWARD=new BlockPos(8,0,-97);
    /** The crawl through the wire: one block high, one wide, turning twice; and the two cells it opens from. */
    public static final List<BlockPos> CRAWL;
    public static final BlockPos CRAWL_IN=new BlockPos(0,0,-53),CRAWL_OUT=new BlockPos(-1,0,-67);
    static{
        var c=new ArrayList<BlockPos>();
        for(int z=-54;z>=-57;z--)c.add(new BlockPos(0,0,z));
        c.add(new BlockPos(1,0,-57));c.add(new BlockPos(2,0,-57));
        for(int z=-58;z>=-61;z--)c.add(new BlockPos(2,0,z));
        c.add(new BlockPos(1,0,-61));c.add(new BlockPos(0,0,-61));c.add(new BlockPos(-1,0,-61));
        for(int z=-62;z>=-66;z--)c.add(new BlockPos(-1,0,z));
        CRAWL=List.copyOf(c);
    }
    /** The four cells, by the captive kept in each: their doors in the hall's side walls, two west and two east. */
    public static final BlockPos[] CELL_DOORS={new BlockPos(-15,0,-80),new BlockPos(15,0,-80),new BlockPos(-15,0,-88),new BlockPos(15,0,-88)};
    /** The three long tables down the hall, and the hundred chairs at them. */
    public static final int[] TABLE_X={-10,0,10};
    public static final int TABLE_S=-76,TABLE_N=-91;
    public static final List<BlockPos> CHAIRS;
    static{
        var c=new ArrayList<BlockPos>();
        for(int t:TABLE_X)for(int z=TABLE_S;z>=TABLE_N;z--){c.add(new BlockPos(t-1,0,z));c.add(new BlockPos(t+1,0,z));}
        c.add(new BlockPos(0,0,TABLE_S+1));c.add(new BlockPos(0,0,TABLE_N-1));c.add(new BlockPos(-10,0,TABLE_S+1));c.add(new BlockPos(10,0,TABLE_S+1));
        CHAIRS=List.copyOf(c);
    }
    /** Where the four sit when the reader first comes in: on the floor against the cabinets, facing across the corridor. */
    public static final Vec3[] SEATS={new Vec3(-3.3,0,-9.5),new Vec3(4.3,0,-11.5),new Vec3(-3.3,0,-13.5),new Vec3(4.3,0,-15.5)};
    public static final float[] SEAT_YAW={-90,90,-90,90};
    /** Where they wait at the mouth of the hall, having gone round the wire some way of their own; and at the crate. */
    public static final Vec3[] HALL_MOUTH={new Vec3(-2.5,0,-73.5),new Vec3(3.5,0,-73.5),new Vec3(-3.5,0,-74.5),new Vec3(4.5,0,-74.5)};
    public static final Vec3[] AT_CRATE={new Vec3(-1.0,0,-94.5),new Vec3(2.0,0,-94.5),new Vec3(-0.5,0,-93.0),new Vec3(1.5,0,-93.0)};
    /** Up the hall's east aisle, clear of the tables and chairs. */
    public static final Vec3 AISLE_S=new Vec3(5.5,0,-73.5),AISLE_N=new Vec3(5.5,0,-93.5);

    // ------------------------------------------------------------------------------------------------ where a body is
    public static boolean inCorridor(Vec3 r){return r.z<=1&&r.z>CORRIDOR_N-1&&Math.abs(r.x-.5)<=CORRIDOR_HALF+1;}
    public static boolean inCave(Vec3 r){return r.z<=CAVE_S&&r.z>CAVE_N-1;}
    public static boolean inJungle(Vec3 r){return r.z<=JUNGLE_S&&r.z>JUNGLE_N-1;}
    public static boolean inHall(Vec3 r){return r.z<=HALL_S&&r.z>HALL_N-1&&Math.abs(r.x-.5)<=HALL_HALF+1;}
    /** Down in the crawl, or at either mouth of it. */
    public static boolean crawling(BlockPos rel){return rel.getY()==0&&(CRAWL.contains(rel)||rel.equals(CRAWL_IN)||rel.equals(CRAWL_OUT));}
    /** In front of a cell's door, on the hall side, near enough to reach it. */
    public static boolean atCell(Vec3 r,int cell,double reach){var d=CELL_DOORS[cell];double fx=d.getX()+.5+(d.getX()<0?1:-1),fz=d.getZ()+.5;return Math.abs(r.y)<2&&(r.x-fx)*(r.x-fx)+(r.z-fz)*(r.z-fz)<=reach*reach;}
    public static int cellOf(BlockPos rel){for(int i=0;i<CELL_DOORS.length;i++)if(rel.equals(CELL_DOORS[i])||rel.equals(CELL_DOORS[i].above()))return i;return -1;}
    /** Inside a cell, past its door, in the glare. */
    public static boolean inCell(Vec3 r){return r.z<HALL_S&&r.z>HALL_N&&Math.abs(r.x-.5)>HALL_HALF+1.5;}
    /** Just outside a cell's door, standing where the captive stands to ask. */
    public static Vec3 cellFront(int cell){var d=CELL_DOORS[cell];return new Vec3(d.getX()+.5+(d.getX()<0?1.2:-1.2),0,d.getZ()+.5);}
    /** The far end of a cell, where the captive goes into the glare. */
    public static Vec3 cellEnd(int cell){var d=CELL_DOORS[cell];return new Vec3(d.getX()<0?-20.5:21.5,0,d.getZ()+.5);}
    /** A point on the walk, at a given z: down the middle of each room, through the middle of each doorway. */
    public static Vec3 walk(double z){return new Vec3(.5,0,z);}

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState mat(String name){return KeptRegistry.material(name).defaultBlockState();}
    static BlockState fit(KeptBlock.Kind kind,Direction facing){return KeptBlock.of(kind,facing,0);}
    static BlockState fit(KeptBlock.Kind kind,Direction facing,int stage){return KeptBlock.of(kind,facing,stage);}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    /** A small, fixed scatter, so the machine is built the same every time. */
    static int hash(int x,int z){int h=x*734287+z*912931;h^=h>>>13;h*=0x5bd1e995;return (h^h>>>15)&0x7fffffff;}

    public static void build(ServerLevel l,BlockPos b){
        box(l,b,WEST,-2,NORTH,EAST,TOP,0,mat("casing"));
        corridor(l,b);cave(l,b);jungle(l,b);hall(l,b);cells(l,b);
        // The three doorways along the walk, each three wide and three high.
        for(int z:new int[]{CORRIDOR_N-1,CAVE_N-1,JUNGLE_N-1})box(l,b,-1,0,z,1,2,z,AIR);
    }

    static void corridor(ServerLevel l,BlockPos b){
        int w=CORRIDOR_HALF;
        box(l,b,-w,-1,CORRIDOR_N,w,-1,0,mat("grating"));
        box(l,b,-w,0,CORRIDOR_N,w,CORRIDOR_HIGH,0,AIR);
        box(l,b,-w,CORRIDOR_HIGH+1,CORRIDOR_N,w,CORRIDOR_HIGH+1,0,mat("ceiling"));
        for(int z=CORRIDOR_N;z<=0;z++){
            for(int side:new int[]{-1,1}){
                int x=side*(w+1);var face=side<0?Direction.EAST:Direction.WEST;
                for(int y=0;y<=2;y++)set(l,b,x,y,z,z%4==-1&&y==1?fit(KeptBlock.Kind.SCREEN,face):fit(KeptBlock.Kind.CABINET,face));
                for(int y=3;y<=CORRIDOR_HIGH;y++)set(l,b,x,y,z,y==4&&(z&1)==0?fit(KeptBlock.Kind.CABINET,face):mat("casing"));
                if(z<-1&&!(z>=-6&&z<=-2))set(l,b,side*w,0,z,fit(KeptBlock.Kind.CABLE,Direction.NORTH));
            }
            if(z%6==-3)set(l,b,0,CORRIDOR_HIGH,z,fit(KeptBlock.Kind.TUBE,Direction.NORTH));
        }
        set(l,b,PRINTER,fit(KeptBlock.Kind.PRINTER,Direction.WEST));
        set(l,b,TRAY,fit(KeptBlock.Kind.TRAY,Direction.EAST));
    }

    /** An ice cave the machine has imitated out of its own casing: frost underfoot, rime on the walls, cabinets sealed in it. */
    static void cave(ServerLevel l,BlockPos b){
        double cz=(CAVE_S+CAVE_N)/2.0,rz=(CAVE_S-CAVE_N)/2.0+.5,rx=9.5;
        for(int z=CAVE_N;z<=CAVE_S;z++)for(int x=-9;x<=9;x++){
            double e=(x*x)/(rx*rx)+((z-cz)*(z-cz))/(rz*rz);boolean path=x>=-1&&x<=1;
            if(e>1&&!path)continue;
            int h=e>.75?3:4+hash(x,z)%3;if(path)h=Math.max(h,4);
            set(l,b,x,-1,z,mat("frost"));
            for(int y=0;y<=h;y++)set(l,b,x,y,z,AIR);
            set(l,b,x,h+1,z,mat("rime"));
            if(!path&&e>.55&&hash(x,z)%5==0)set(l,b,x,0,z,mat("frost"));
            if(!path&&hash(z,x)%4==0)set(l,b,x,h,z,fit(KeptBlock.Kind.ICICLE,Direction.NORTH));
        }
        // The casing the cave is cut from, where it shows: under rime, and here and there a cabinet sealed in it, facing in.
        var casing=KeptRegistry.material("casing");
        for(int z=CAVE_N-1;z<=CAVE_S+1;z++)for(int x=-10;x<=10;x++)for(int y=0;y<=7;y++){
            var at=b.offset(x,y,z);if(!BuildBlocks.state(l,at).is(casing))continue;
            Direction open=null;
            for(var d:Direction.Plane.HORIZONTAL){var n=at.relative(d);int nz=n.getZ()-b.getZ();if(nz<=CAVE_S&&nz>=CAVE_N&&BuildBlocks.state(l,n).isAir()){open=d;break;}}
            if(open==null)continue;
            if(y<=2&&hash(x,z)%3==0)BuildBlocks.set(l,at,fit(KeptBlock.Kind.CABINET,open,1),F);
            else BuildBlocks.set(l,at,mat("rime"),F);
        }
        for(int z=CAVE_S-3;z>=CAVE_N+2;z-=6)set(l,b,0,4,z,fit(KeptBlock.Kind.TUBE,Direction.NORTH));
    }

    /** A thicket of wire too dense to push through, with one crawl under it. */
    static void jungle(ServerLevel l,BlockPos b){
        box(l,b,-8,-1,JUNGLE_N,8,-1,JUNGLE_S,mat("grating"));
        box(l,b,-8,0,JUNGLE_N,8,5,JUNGLE_S,AIR);
        box(l,b,-8,6,JUNGLE_N,8,6,JUNGLE_S,mat("ceiling"));
        for(int z=THICKET_N;z<=THICKET_S;z++)for(int x=-8;x<=8;x++)for(int y=0;y<=5;y++){
            if(y==0&&CRAWL.contains(new BlockPos(x,0,z)))continue;
            set(l,b,x,y,z,fit(KeptBlock.Kind.WIRE,Direction.from2DDataValue(hash(x+y,z)%4),hash(x,z+y)%7==0?1:0));
        }
        // Strands hanging into both clearings from the thicket's edge.
        for(int x=-8;x<=8;x++){if(hash(x,1)%3==0)set(l,b,x,5,THICKET_S+1,fit(KeptBlock.Kind.WIRE,Direction.NORTH,1));if(hash(x,2)%3==0&&x!=-1)set(l,b,x,5,THICKET_N-1,fit(KeptBlock.Kind.WIRE,Direction.SOUTH));}
        set(l,b,0,5,JUNGLE_S-1,fit(KeptBlock.Kind.TUBE,Direction.NORTH));
        set(l,b,0,5,JUNGLE_N+1,fit(KeptBlock.Kind.TUBE,Direction.NORTH));
    }

    /** The dining hall: flocked paper, a chequered floor, three long tables laid for nobody, a hundred chairs, and the crate. */
    static void hall(ServerLevel l,BlockPos b){
        int w=HALL_HALF;
        box(l,b,-w,-1,HALL_N,w,-1,HALL_S,mat("checker"));
        box(l,b,-w,0,HALL_N,w,HALL_HIGH,HALL_S,AIR);
        box(l,b,-w,HALL_HIGH+1,HALL_N,w,HALL_HIGH+1,HALL_S,mat("ceiling"));
        for(int z=HALL_N;z<=HALL_S;z++)for(int y=0;y<=HALL_HIGH;y++)for(int side:new int[]{-1,1})set(l,b,side*(w+1),y,z,y<=0?mat("casing"):mat("flock"));
        for(int x=-w;x<=w;x++)for(int y=0;y<=HALL_HIGH;y++){set(l,b,x,y,HALL_N-1,y<=0?mat("casing"):mat("flock"));set(l,b,x,y,HALL_S+1,y<=0?mat("casing"):mat("flock"));}
        for(int t:TABLE_X)for(int z=TABLE_S;z>=TABLE_N;z--)set(l,b,t,0,z,fit(KeptBlock.Kind.TABLE,Direction.NORTH,1));
        for(var c:CHAIRS)set(l,b,c,fit(KeptBlock.Kind.CHAIR,facing(c)));
        set(l,b,CRATE,fit(KeptBlock.Kind.CRATE,Direction.SOUTH));
        // Screens set into the flocked walls between the cells; chandeliers of cable and bulbs down the aisles.
        for(int z:new int[]{-76,-84,-92})for(int side:new int[]{-1,1})set(l,b,side*(w+1),2,z,fit(KeptBlock.Kind.SCREEN,side<0?Direction.EAST:Direction.WEST));
        for(int x=-1;x<=1;x++)set(l,b,x,3,HALL_N-1,fit(KeptBlock.Kind.SCREEN,Direction.SOUTH));
        for(int z:new int[]{-78,-86,-94})for(int x:new int[]{-5,5})set(l,b,x,HALL_HIGH,z,fit(KeptBlock.Kind.CHANDELIER,Direction.NORTH));
        for(int z:new int[]{-80,-88})for(int x:new int[]{-13,13})set(l,b,x,HALL_HIGH,z,fit(KeptBlock.Kind.CHANDELIER,Direction.NORTH));
    }
    /** The way a chair stands when nobody is in the hall: drawn up to its table. */
    static Direction facing(BlockPos chair){
        for(int t:TABLE_X){if(chair.getX()==t-1)return Direction.EAST;if(chair.getX()==t+1)return Direction.WEST;}
        return chair.getZ()>TABLE_S?Direction.NORTH:Direction.SOUTH;
    }

    /** Four cells off the hall, glare from wall to wall inside, each behind an iron door. */
    static void cells(ServerLevel l,BlockPos b){
        for(int i=0;i<CELL_DOORS.length;i++){
            var d=CELL_DOORS[i];int side=d.getX()<0?-1:1;
            int x0=side<0?-21:16,x1=side<0?-16:21;
            box(l,b,side<0?-22:16,-1,d.getZ()-2,side<0?-16:22,4,d.getZ()+2,mat("glare"));
            box(l,b,x0,0,d.getZ()-1,x1,3,d.getZ()+1,AIR);
            door(l,b,i,false);
        }
    }
    /** A cell's iron door, shut or open, facing into the hall. */
    public static void door(ServerLevel l,BlockPos b,int cell,boolean open){
        var at=b.offset(CELL_DOORS[cell]);var facing=CELL_DOORS[cell].getX()<0?Direction.EAST:Direction.WEST;
        var lower=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.OPEN,open).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
        BuildBlocks.set(l,at,lower,F);BuildBlocks.set(l,at.above(),lower.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    public static boolean doorOpen(ServerLevel l,BlockPos b,int cell){var s=l.getBlockState(b.offset(CELL_DOORS[cell]));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}

    // ------------------------------------------------------------------------------------------------ at runtime
    /** Every chair turned toward whoever is nearest it, or drawn up to its table again when nobody is in the hall. */
    public static void chairs(ServerLevel l,BlockPos b,List<Vec3> readers){
        for(var c:CHAIRS){
            var at=b.offset(c);var st=l.getBlockState(at);if(!KeptBlock.is(st,KeptBlock.Kind.CHAIR))continue;
            Direction want=facing(c);double best=Double.MAX_VALUE;Vec3 near=null;
            for(var r:readers){double dx=r.x-(c.getX()+.5),dz=r.z-(c.getZ()+.5),dd=dx*dx+dz*dz;if(dd<best){best=dd;near=r;}}
            if(near!=null){double dx=near.x-(c.getX()+.5),dz=near.z-(c.getZ()+.5);want=Math.abs(dx)>Math.abs(dz)?(dx>0?Direction.EAST:Direction.WEST):(dz>0?Direction.SOUTH:Direction.NORTH);}
            if(st.getValue(KeptBlock.FACING)!=want)l.setBlock(at,st.setValue(KeptBlock.FACING,want),F);
        }
    }
    /** The screens near any reader awake; the rest dark. */
    public static int screens(ServerLevel l,BlockPos b,List<Vec3> readers,double reach){
        int woke=0;
        for(int z=0;z>=HALL_N-1;z--)for(int x=WEST;x<=EAST;x++)for(int y=1;y<=3;y++){
            if(Math.abs(x)!=CORRIDOR_HALF+1&&Math.abs(x)!=HALL_HALF+1&&!(z==HALL_N-1&&Math.abs(x)<=1))continue;
            var at=b.offset(x,y,z);var st=l.getBlockState(at);if(!KeptBlock.is(st,KeptBlock.Kind.SCREEN))continue;
            boolean near=false;for(var r:readers)if(r.distanceToSqr(x+.5,y,z+.5)<reach*reach){near=true;break;}
            int want=near?1:0;if(st.getValue(KeptBlock.STAGE)!=want){l.setBlock(at,st.setValue(KeptBlock.STAGE,want),F);if(near)woke++;}
        }
        return woke;
    }
    /** Every screen in the machine at once. */
    public static void allScreens(ServerLevel l,BlockPos b,int stage){
        for(int z=0;z>=HALL_N-1;z--)for(int x=WEST;x<=EAST;x++)for(int y=1;y<=3;y++){
            var at=b.offset(x,y,z);var st=l.getBlockState(at);if(KeptBlock.is(st,KeptBlock.Kind.SCREEN)&&st.getValue(KeptBlock.STAGE)!=stage)l.setBlock(at,st.setValue(KeptBlock.STAGE,stage),F);
        }
    }
    /** The hall as it was: every cell shut, every chair drawn up, every screen dark. */
    public static void reset(ServerLevel l,BlockPos b){for(int i=0;i<CELL_DOORS.length;i++)door(l,b,i,false);chairs(l,b,List.of());allScreens(l,b,0);}
}
