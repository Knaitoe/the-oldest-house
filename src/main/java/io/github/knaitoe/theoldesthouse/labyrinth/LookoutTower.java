package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.202, the Whistlers in the Pines: the fire lookout, built whole (docs/WHISTLERS_0_4_202.md). An outdoor scene at a
 * fixed height in the outside dimension, so the tower has room to stand.
 *
 * <ul>
 * <li><b>The clearing.</b> Snow from the reader's door to the tower, black spruce all round so the woods are opaque, a broken
 * fence, chest-high snowbanks, the corrugated iron generator shed and the propane tank.</li>
 * <li><b>The tower.</b> Four creosoted stilts and, inside them, four timber flights rising round the frame with a lantern
 * at each landing, to a hatch in the cab floor.</li>
 * <li><b>The cab.</b> Nine by nine and five high inside a one-block catwalk, its four walls each a five-wide window with a
 * canvas roll blind and a cord beside it; the Osborne fire finder in the middle under the brass pendulum; the desk with the
 * protocol binder and the fire log; the teleprinter and the radio; the wood stove; the cot; the crank on its hook.</li>
 * <li><b>The forest of towers.</b> Far to the north and out of sight of the clearing, tower four again with nothing under its
 * stilts, and eight more cabs at thirty blocks round it at the same height: the fourth night.</li>
 * </ul>
 */
public final class LookoutTower {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WHISTLERS;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    /** The ground of the clearing stands at this height in the outside dimension, whatever the manor's. */
    public static final int BASE_Y=96;
    /** The tower's cab centre; tower four again in the void, to the north; the cab floor, and the roof over it. */
    public static final int CX=0,CZ=-34,VZ=-240,FLOOR=19,ROOF=25;
    /** How far the eight other lookouts stand from tower four in the void. */
    public static final int RING=30;
    /** The way on: a trail door at the treeline behind the tower. */
    public static final int ONWARD_Z=CZ-20;
    public static final BlockPos FINDER=new BlockPos(CX,20,CZ),VOID_FINDER=new BlockPos(CX,20,VZ);
    /** The protocol binder (the source) and the fire log (the account) on the desk by the west wall. */
    public static final BlockPos SOURCE=new BlockPos(CX-4,21,CZ-4),ENDING=new BlockPos(CX-4,21,CZ-3);
    public static final BlockPos TELEPRINTER=new BlockPos(CX+4,20,CZ-4),RADIO=new BlockPos(CX+4,21,CZ-3),STOVE=new BlockPos(CX+4,20,CZ+4);
    public static final BlockPos CRANK_HOOK=new BlockPos(CX+2,21,CZ+4),PENDULUM=new BlockPos(CX,24,CZ),LAMP=new BlockPos(CX-2,24,CZ-2),HATCH=new BlockPos(CX+2,ROOF,CZ+2);
    /** The two folded tarps for the third night, by the stove. */
    public static final List<BlockPos> TARPS=List.of(new BlockPos(CX+2,20,CZ+3),new BlockPos(CX+3,20,CZ+3));
    /**
     * The floor hatch to the stair: its three cells, and the stair's top tread in the floor beside them. 0.4.258 (owner: "players
     * still aren't able to enter"): with two cells the tread under the hatch's west edge rose while the reader's shoulders were still
     * under the floor beside it, and their head met the boards; the opening now starts over the tread before it.
     */
    public static final List<BlockPos> TRAPDOORS=List.of(new BlockPos(CX-2,FLOOR,CZ+4),new BlockPos(CX-1,FLOOR,CZ+4),new BlockPos(CX,FLOOR,CZ+4));
    /** The shed's generator, the lamp over its door, and where it stands on the shed's roof. */
    public static final BlockPos GENERATOR=new BlockPos(-14,0,-26),SHED_LAMP=new BlockPos(-10,2,-26);
    public static final Vec3 SHED_ROOF=new Vec3(-12.5,4,-25.5);
    /** Where a reader comes to on the fourth night, and where the trapdoor puts them back. */
    public static final Vec3 VOID_WAKE=new Vec3(CX+.5,20,VZ+.5),CAB_WAKE=new Vec3(CX+.5,20,CZ+2.5);
    /** The walls, in the order of their whistles: north, east, south, west. */
    public static final Direction[] WALLS={Direction.NORTH,Direction.EAST,Direction.SOUTH,Direction.WEST};
    public static final String[] WALL_NAMES={"north","east","south","west"};

    /** One cell of the stair: where it is, relative to the base, and the height of its tread's top. */
    public record Tread(int x,int z,int h){public BlockPos cell(){return new BlockPos(x,h-1,z);}}
    /** The stair, from the ground at the frame's south-east corner up round the four sides to the floor of the cab. */
    public static final List<Tread> STAIR=stair();
    /** The four landings (the foot, then the three corners on the way up), each with its lantern hung above it. */
    public static final List<Tread> LANDINGS=List.of(new Tread(CX+4,CZ+4,0),new Tread(CX+4,CZ-4,5),new Tread(CX-4,CZ-4,10),new Tread(CX-4,CZ+4,15));
    private static List<Tread> stair(){
        var out=new ArrayList<Tread>();out.add(new Tread(CX+4,CZ+4,0));
        int[] run={1,2,3,4,5,5,5};
        for(int i=0;i<7;i++)out.add(new Tread(CX+4,CZ+3-i,run[i]));out.add(new Tread(CX+4,CZ-4,5));
        for(int i=0;i<7;i++)out.add(new Tread(CX+3-i,CZ-4,5+run[i]));out.add(new Tread(CX-4,CZ-4,10));
        for(int i=0;i<7;i++)out.add(new Tread(CX-4,CZ-3+i,10+run[i]));out.add(new Tread(CX-4,CZ+4,15));
        for(int i=0;i<5;i++)out.add(new Tread(CX-3+i,CZ+4,16+i));
        return List.copyOf(out);
    }
    /**
     * The lantern at a landing: hung from a beam over its outer rail, beside the way rather than in it. 0.4.220 (owner: "stairs
     * that don't allow the player to reach the opening"): it hung over the middle of the landing, in front of the face of
     * whoever turned onto the next flight.
     */
    public static BlockPos lantern(Tread t){return new BlockPos(t.x()+Integer.signum(t.x()-CX),t.h()+2,t.z());}
    public static AABB bounds(BlockPos b){return new AABB(b.offset(-40,-8,-280).getCenter(),b.offset(40,30,0).getCenter());}

    // ------------------------------------------------------------------------------------------------ geometry of a cab
    /** A wall's window cells, by wall (0 north ... 3 west), for the cab centred at cz. */
    public static List<BlockPos> window(int wall,int cz){
        var out=new ArrayList<BlockPos>(15);
        for(int a=-2;a<=2;a++)for(int y=21;y<=23;y++)out.add(switch(wall){case 0->new BlockPos(CX+a,y,cz-5);case 1->new BlockPos(CX+5,y,cz+a);case 2->new BlockPos(CX+a,y,cz+5);default->new BlockPos(CX-5,y,cz+a);});
        return out;
    }
    /** A wall's cord, low on the wall beside its window, where a reader looking at the floor can find it. */
    public static BlockPos cord(int wall,int cz){return switch(wall){case 0->new BlockPos(CX-3,20,cz-4);case 1->new BlockPos(CX+4,20,cz+3);case 2->new BlockPos(CX-3,20,cz+4);default->new BlockPos(CX-4,20,cz+3);};}
    /** The amber lamp above a wall's window. */
    public static BlockPos quadrant(int wall,int cz){return switch(wall){case 0->new BlockPos(CX,24,cz-4);case 1->new BlockPos(CX+4,24,cz);case 2->new BlockPos(CX,24,cz+4);default->new BlockPos(CX-4,24,cz);};}
    /** Which wall a cell of the cab centred at cz belongs to (its window, its lower panel or its cord), or -1. */
    public static int wallOf(BlockPos rel,int cz){
        for(int w=0;w<4;w++)if(rel.equals(cord(w,cz)))return w;
        int dx=rel.getX()-CX,dz=rel.getZ()-cz;if(rel.getY()<20||rel.getY()>23||Math.abs(dx)>5||Math.abs(dz)>5)return -1;
        if(dz==-5&&Math.abs(dx)<=4)return 0;if(dx==5&&Math.abs(dz)<=4)return 1;if(dz==5&&Math.abs(dx)<=4)return 2;if(dx==-5&&Math.abs(dz)<=4)return 3;return -1;
    }
    /** Inside the cab centred at cz: over its floor, under its roof, within its walls. */
    public static boolean inCab(Vec3 rel,int cz){return rel.x>=CX-4&&rel.x<CX+5&&rel.z>=cz-4&&rel.z<cz+5&&rel.y>=FLOOR+.5&&rel.y<ROOF;}
    /** The eight lookouts round tower four, by bearing (0 is north, the still one), their cab centres relative to the base. */
    public static BlockPos ring(int k){double a=Math.toRadians(k*45);return new BlockPos(CX+(int)Math.round(RING*Math.sin(a)),0,VZ-(int)Math.round(RING*Math.cos(a)));}

    // ------------------------------------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        // The outside is an empty void: only the clearing needs clearing; the forest of towers stands in untouched air.
        box(l,b,-40,-8,-72,40,30,0,air);
        clearing(l,b);
        tower(l,b);
        cab(l,b,CZ,true);
        // The forest of towers: tower four's cab on stilts over nothing, and the eight round it, dark until the fourth night.
        cab(l,b,VZ,false);stilts(l,b,CX,VZ,7);
        for(int k=0;k<8;k++){var c=ring(k);cabAt(l,b,c.getX(),c.getZ());stilts(l,b,c.getX(),c.getZ(),7);}
    }

    static void clearing(ServerLevel l,BlockPos b){
        var snow=Blocks.SNOW_BLOCK.defaultBlockState();var dirt=Blocks.DIRT.defaultBlockState();
        // The ground, a little thick, from the door to the woods' far side; the vestibule stands on its own floor.
        box(l,b,-40,-3,-72,40,-2,0,dirt);box(l,b,-40,-1,-72,40,-1,0,snow);
        var rng=RandomSource.create(202);
        // Black spruce all round, thick enough that nothing past the treeline shows.
        for(int x=-38;x<=38;x+=4)for(int z=-70;z<=-2;z+=4){
            int tx=x+rng.nextInt(3)-1,tz=z+rng.nextInt(3)-1;double d=Math.hypot(tx-CX,tz-CZ);
            if(d<19||Math.abs(tx)<=4&&tz>CZ||Math.abs(tx)<=4&&tz<=ONWARD_Z+2&&tz>=ONWARD_Z-4||Math.abs(tx)<=9&&tz>-6||tx<=-8&&tx>=-17&&tz>=-29&&tz<=-21||tx>=9&&tx<=15&&tz>=-29&&tz<=-23)continue;
            spruce(l,b,tx,tz,6+rng.nextInt(4));
        }
        // Snow drifted against the trunks and across the open ground, never on the path, under the tower or on the trail to the
        // door in the treeline behind it.
        for(int i=0;i<520;i++){
            int x=-36+rng.nextInt(73),z=-68+rng.nextInt(66);double d=Math.hypot(x-CX,z-CZ);
            if(d<8||Math.abs(x)<=3&&z>CZ||Math.abs(x)<=8&&z>-3||Math.abs(x)<=3&&z<CZ&&z>=ONWARD_Z-3)continue;var at=b.offset(x,0,z);
            if(BuildBlocks.state(l,at).isAir())set(l,b,x,0,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,1+rng.nextInt(2)));
        }
        // Chest-high snowbanks: cover from the treeline on the way between the shed and the stair.
        int[][] banks={{-8,-16,-5,-15},{5,-20,8,-19},{-9,-44,-6,-43},{6,-47,9,-46},{-9,-31,-8,-29},{-6,-24,-5,-22},{8,-36,8,-32}};
        for(var k:banks)for(int x=k[0];x<=k[2];x++)for(int z=k[1];z<=k[3];z++){set(l,b,x,0,z,snow);set(l,b,x,1,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,5+((x+z)&1)*2));}
        // A broken fence round the clearing, with its gaps.
        for(int a=0;a<360;a+=5){
            if(a%60<15||a>=150&&a<=210)continue;double r=Math.toRadians(a);int x=CX+(int)Math.round(17*Math.sin(r)),z=CZ-(int)Math.round(17*Math.cos(r));
            if(Math.abs(x)<=3&&z>CZ)continue;set(l,b,x,0,z,Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        shed(l,b);
        // The propane tank on its blocks, frosted.
        set(l,b,11,0,-26,LookoutBlock.of(LookoutBlock.Kind.PROPANE,Direction.NORTH,0));set(l,b,12,0,-26,LookoutBlock.of(LookoutBlock.Kind.PROPANE,Direction.NORTH,1));
    }
    /** The corrugated iron shed with the diesel generator in it, its door toward the tower and its lamp over the door. */
    static void shed(ServerLevel l,BlockPos b){
        var iron=LookoutRegistry.material("iron").defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        box(l,b,-15,0,-28,-11,2,-24,iron);box(l,b,-14,0,-27,-12,2,-25,air);box(l,b,-15,3,-28,-11,3,-24,iron);
        for(int y=0;y<=1;y++)set(l,b,-11,y,-26,air);
        set(l,b,GENERATOR.getX(),0,GENERATOR.getZ(),LookoutBlock.of(LookoutBlock.Kind.GENERATOR,Direction.EAST,0));
        set(l,b,-14,1,-27,Blocks.CHAIN.defaultBlockState());
        set(l,b,SHED_LAMP.getX(),SHED_LAMP.getY(),SHED_LAMP.getZ(),LookoutBlock.of(LookoutBlock.Kind.SHED_LAMP,Direction.EAST,0));
        // The exhaust stack through the roof.
        for(int y=3;y<=4;y++)set(l,b,-14,y,-25,Blocks.IRON_BARS.defaultBlockState());
    }
    static void spruce(ServerLevel l,BlockPos b,int x,int z,int height){
        var log=Blocks.SPRUCE_LOG.defaultBlockState();var leaves=Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int y=0;y<height;y++)set(l,b,x,y,z,log);
        for(int y=2;y<=height;y++){int r=y>=height-1?0:y>=height-3?1:2;if((height-y)%2==0&&r>1)r=1;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if((dx!=0||dz!=0)&&Math.abs(dx)+Math.abs(dz)>r+1)continue;if(dx==0&&dz==0&&y<height)continue;var at=b.offset(x+dx,y,z+dz);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,leaves,F);}}
        set(l,b,x,height,z,leaves);set(l,b,x,height+1,z,leaves);
    }
    /** The stilts, the frame between them, and the stair inside them with its rails and its four lanterns. */
    static void tower(ServerLevel l,BlockPos b){
        stilts(l,b,CX,CZ,0);
        var timber=LookoutRegistry.material("timber").defaultBlockState();var boards=LookoutRegistry.material("boards").defaultBlockState();
        // A girt round the frame halfway up, and the joists under the cab.
        for(int a=-4;a<=4;a++){set(l,b,CX+a,9,CZ-5,timber);set(l,b,CX+a,9,CZ+5,timber);set(l,b,CX-5,9,CZ+a,timber);set(l,b,CX+5,9,CZ+a,timber);}
        for(int i=0;i<STAIR.size();i++){
            var t=STAIR.get(i);if(t.h()<=0)continue;var next=i+1<STAIR.size()?STAIR.get(i+1):null;
            boolean rising=next!=null&&next.h()>t.h();var prev=STAIR.get(i-1);boolean step=t.h()>prev.h();
            BlockState s;
            if(step){var up=Direction.getNearest(t.x()-prev.x(),0,t.z()-prev.z());s=Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,up);}
            else s=boards;
            set(l,b,t.x(),t.h()-1,t.z(),s);
        }
        // The rails: both sides of every tread, where nothing else already stands. Where the next tread is a step up, the post
        // stands two high, so that it meets the next post at the height they share and the rail runs on unbroken up the flight.
        // 0.4.258 (owner: "banisters are still disconnected"): the posts stood at walking height beside the treads with air under
        // them, a rail floating free of the stair. Each now stands on a timber stringer level with its tread, and the stringer
        // steps up with the flight, so every post joins the next post or the next piece of stringer.
        // The stringer goes in whole first, so each post joins the stringer beside it as well as the posts.
        for(var t:STAIR){if(t.h()<=0||t.h()>=FLOOR)continue;
            for(var side:Direction.Plane.HORIZONTAL){int x=t.x()+side.getStepX(),z=t.z()+side.getStepZ();if(onStair(x,z))continue;
                var foot=b.offset(x,t.h()-1,z);if(BuildBlocks.state(l,foot).isAir())BuildBlocks.set(l,foot,timber,F);}}
        for(int i=0;i<STAIR.size();i++){var t=STAIR.get(i);if(t.h()<=0||t.h()>=FLOOR)continue;
            boolean rises=i+1<STAIR.size()&&STAIR.get(i+1).h()>t.h();
            for(var side:Direction.Plane.HORIZONTAL){int x=t.x()+side.getStepX(),z=t.z()+side.getStepZ();if(onStair(x,z))continue;
                for(int y=t.h();y<=t.h()+(rises?1:0);y++){var at=b.offset(x,y,z);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,Blocks.SPRUCE_FENCE.defaultBlockState(),F);}}}
        // Posts under the three corner landings, down to the snow.
        for(var t:LANDINGS){if(t.h()<=0)continue;for(int y=0;y<t.h()-1;y++)set(l,b,t.x(),y,t.z(),Blocks.SPRUCE_FENCE.defaultBlockState());}
        // A lantern hung over each landing from a beam.
        for(var t:LANDINGS){var at=lantern(t);set(l,b,at.getX(),at.getY(),at.getZ(),LookoutBlock.of(LookoutBlock.Kind.LANTERN,Direction.NORTH,0));set(l,b,at.getX(),at.getY()+1,at.getZ(),timber);}
    }
    private static boolean onStair(int x,int z){for(var t:STAIR)if(t.x()==x&&t.z()==z)return true;return false;}
    static void stilts(ServerLevel l,BlockPos b,int cx,int cz,int from){
        var timber=LookoutRegistry.material("timber").defaultBlockState();
        for(int dx:new int[]{-5,5})for(int dz:new int[]{-5,5})for(int y=from;y<FLOOR;y++)set(l,b,cx+dx,y,cz+dz,timber);
    }
    /** One of the eight cabs round tower four: the same cab, nobody's hatch. */
    static void cabAt(ServerLevel l,BlockPos b,int cx,int cz){
        var shift=b.offset(cx-CX,0,0);cab(l,shift,cz,false);
    }
    /**
     * A cab centred at (CX, cz): floor and catwalk, rail, walls with their five-wide windows and blinds rolled up, the roof,
     * and everything inside it. The tower in the clearing has its floor hatch onto the stair; the void's have a closed one.
     */
    static void cab(ServerLevel l,BlockPos b,int cz,boolean clearing){
        var timber=LookoutRegistry.material("timber").defaultBlockState();var boards=LookoutRegistry.material("boards").defaultBlockState();
        var panel=LookoutRegistry.material("panel").defaultBlockState();var roof=LookoutRegistry.material("roof").defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        box(l,b,CX-7,FLOOR,cz-7,CX+7,FLOOR+10,cz+7,air);
        box(l,b,CX-6,FLOOR,cz-6,CX+6,FLOOR,cz+6,boards);
        for(int a=-7;a<=7;a++)for(int[] e:new int[][]{{a,-7},{a,7},{-7,a},{7,a}})set(l,b,CX+e[0],20,cz+e[1],Blocks.SPRUCE_FENCE.defaultBlockState());
        for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++){
            if(Math.abs(dx)!=5&&Math.abs(dz)!=5)continue;boolean corner=Math.abs(dx)==5&&Math.abs(dz)==5;
            for(int y=20;y<=24;y++)set(l,b,CX+dx,y,cz+dz,corner||y==24?timber:panel);
        }
        for(int w=0;w<4;w++)for(var c:window(w,cz))set(l,b,c.getX(),c.getY(),c.getZ(),LookoutBlock.of(LookoutBlock.Kind.PANE,WALLS[w],0));
        // The roof: a low pyramid over the cab and the catwalk.
        box(l,b,CX-7,ROOF,cz-7,CX+7,ROOF,cz+7,roof);box(l,b,CX-4,ROOF+1,cz-4,CX+4,ROOF+1,cz+4,roof);box(l,b,CX-2,ROOF+2,cz-2,CX+2,ROOF+2,cz+2,roof);set(l,b,CX,ROOF+3,cz,timber);
        set(l,b,HATCH.getX(),ROOF,cz+(HATCH.getZ()-CZ),LookoutBlock.of(LookoutBlock.Kind.HATCH,Direction.NORTH,0));
        // The fire finder in the middle, the pendulum over it, the lamp, the amber lamps over the windows.
        put(l,b,FINDER,cz,LookoutBlock.of(LookoutBlock.Kind.FIRE_FINDER,Direction.NORTH,0));
        put(l,b,PENDULUM,cz,LookoutBlock.of(LookoutBlock.Kind.PENDULUM,Direction.NORTH,0));
        put(l,b,LAMP,cz,LookoutBlock.of(LookoutBlock.Kind.CAB_LAMP,Direction.NORTH,clearing?0:1));
        for(int w=0;w<4;w++){var q=quadrant(w,CZ);put(l,b,q,cz,LookoutBlock.of(LookoutBlock.Kind.QUADRANT_LAMP,WALLS[w].getOpposite(),0));var c=cord(w,CZ);put(l,b,c,cz,LookoutBlock.of(LookoutBlock.Kind.CORD,WALLS[w].getOpposite(),0));}
        // The desk by the west wall with the binder and the log; the teleprinter and the radio by the east.
        put(l,b,SOURCE.below(),cz,LookoutBlock.of(LookoutBlock.Kind.DESK,Direction.EAST,0));put(l,b,ENDING.below(),cz,LookoutBlock.of(LookoutBlock.Kind.DESK,Direction.EAST,0));
        put(l,b,SOURCE,cz,LookoutBlock.of(LookoutBlock.Kind.BINDER,Direction.EAST,0));put(l,b,ENDING,cz,LookoutBlock.of(LookoutBlock.Kind.FIRE_LOG,Direction.EAST,0));
        put(l,b,TELEPRINTER,cz,LookoutBlock.of(LookoutBlock.Kind.TELEPRINTER,Direction.WEST,clearing?0:2));put(l,b,RADIO.below(),cz,LookoutBlock.of(LookoutBlock.Kind.DESK,Direction.WEST,0));
        put(l,b,RADIO,cz,LookoutBlock.of(LookoutBlock.Kind.RADIO,Direction.WEST,0));
        // The stove in the south-east corner and its pipe up through the roof.
        put(l,b,STOVE,cz,LookoutBlock.of(LookoutBlock.Kind.STOVE,Direction.WEST,clearing?0:1));
        for(int y=21;y<=ROOF+1;y++)set(l,b,STOVE.getX(),y,cz+(STOVE.getZ()-CZ),Blocks.IRON_BARS.defaultBlockState());
        put(l,b,new BlockPos(CX-4,20,CZ),cz,LookoutBlock.of(LookoutBlock.Kind.COT,Direction.EAST,0));put(l,b,new BlockPos(CX-4,20,CZ+1),cz,LookoutBlock.of(LookoutBlock.Kind.COT,Direction.EAST,1));
        // The hatch onto the stair, railed on its open sides (one straight rail and its corner, nothing joined only at a diagonal); in
        // the void, a hatch that will not lift.
        // 0.4.220 (owner: "stairs that don't allow the player to reach the opening"): the clearing's hatch stands open against the
        // south wall, so the stair comes up into the cab through a railed opening rather than under a lid.
        for(var t:TRAPDOORS)put(l,b,t,cz,clearing?Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.FACING,Direction.NORTH).setValue(TrapDoorBlock.OPEN,true):LookoutBlock.of(LookoutBlock.Kind.HATCH,Direction.NORTH,0));
        if(clearing){
            var top=STAIR.get(STAIR.size()-1);set(l,b,top.x(),FLOOR,cz+(top.z()-CZ),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            for(var at:new BlockPos[]{new BlockPos(CX-3,20,CZ+3),new BlockPos(CX-2,20,CZ+3),new BlockPos(CX-1,20,CZ+3),new BlockPos(CX,20,CZ+3),new BlockPos(CX-3,20,CZ+4)})put(l,b,at,cz,Blocks.SPRUCE_FENCE.defaultBlockState());
            put(l,b,CRANK_HOOK,cz,LookoutBlock.of(LookoutBlock.Kind.CRANK_HOOK,Direction.NORTH,0));
        }
    }
    private static void put(ServerLevel l,BlockPos b,BlockPos rel,int cz,BlockState s){set(l,b,rel.getX(),rel.getY(),cz+(rel.getZ()-CZ),s);}

    // ------------------------------------------------------------------------------------------------ the night's changes
    /** Every window of a cab back to its blind rolled up and its glass clear. */
    public static void clearWindows(ServerLevel l,BlockPos b,int cz){for(int w=0;w<4;w++)setWall(l,b,w,cz,LookoutBlock.Kind.PANE,0);}
    public static void setWall(ServerLevel l,BlockPos b,int wall,int cz,LookoutBlock.Kind kind,int stage){
        for(var c:window(wall,cz))l.setBlock(b.offset(c),LookoutBlock.of(kind,WALLS[wall],stage),F);
    }
    /** What a wall's blind is now: read from its middle cell. */
    public static LookoutBlock.Kind wall(ServerLevel l,BlockPos b,int wall,int cz){var k=LookoutBlock.kind(l.getBlockState(b.offset(window(wall,cz).get(7))));return k==null?LookoutBlock.Kind.BROKEN:k;}
    public static void stage(ServerLevel l,BlockPos b,BlockPos rel,int stage){var at=b.offset(rel);var s=l.getBlockState(at);if(s.is(LookoutRegistry.FITTING.get())&&s.getValue(LookoutBlock.STAGE)!=stage)l.setBlock(at,s.setValue(LookoutBlock.STAGE,stage),F);}
    public static void facing(ServerLevel l,BlockPos b,BlockPos rel,Direction d,int stage){var at=b.offset(rel);var s=l.getBlockState(at);if(s.is(LookoutRegistry.FITTING.get()))l.setBlock(at,s.setValue(LookoutBlock.FACING,d).setValue(LookoutBlock.STAGE,stage),F);}
    public static void kind(ServerLevel l,BlockPos b,BlockPos rel,LookoutBlock.Kind kind,Direction facing,int stage){l.setBlock(b.offset(rel),LookoutBlock.of(kind,facing,stage),F);}

    // ------------------------------------------------------------------------------------------------ helpers
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),s,F);}
}
