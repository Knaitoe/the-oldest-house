package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.333, State Route 9 (docs/STATE_ROUTE_0_4_333.md): a four-lane divided highway at night, under the outside sky, crossing the
 * reader's way from left to right. Every position is relative to the base, the floor inside the door the reader comes through.
 *
 * <p>Out of the door a short lane between cedar hedges runs north onto the wet gravel of the starting shoulder, along the back of which
 * stands a tall chain-link fence with a padlocked pedestrian gate (the way on). Then two eastbound lanes, the depressed median with its
 * dead weeds and bent guardrail posts, two westbound lanes, and the far shoulder: a galvanized mailbox with its flag up under the one
 * sodium lamp, the route shield, the mile marker. Past that the corn stands wet and dense up to a face of dark joists. The traffic comes
 * out of the hedges at either end of the road and goes back into them.
 */
public final class StateRouteSite {
    private StateRouteSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.STATE_ROUTE;
    public static final int WEST=-58,EAST=58,FAR=-29;
    /** The hedges the road runs out of at either end, and where traffic starts and ends, out of sight beyond them. */
    public static final int HEDGE=48,SPAWN=58;
    /** The fence along the back of the starting shoulder, and the shoulder itself. */
    public static final int FENCE_Z=-3;
    public static final double START_NEAR=-3,START_FAR=-5;
    /** The four lanes' middles: two eastbound, then the median, then two westbound. */
    public static final double[] LANE_Z={-6.5,-9.5,-14.5,-17.5};
    public static final double MEDIAN_NEAR=-11,MEDIAN_FAR=-13,FAR_NEAR=-19,FAR_FAR=-22;
    /** The far shoulder's mailbox under the lamp, the route shield, the mile marker, the lamp and its pole. */
    public static final BlockPos MAILBOX=new BlockPos(0,0,-22),SHIELD=new BlockPos(-3,1,-22),MILE=new BlockPos(-1,0,-22),POLE=new BlockPos(3,0,-22),LAMP=new BlockPos(3,6,-22);
    /** The padlocked gate's lower half in the fence, approached from the shoulder. */
    public static final BlockPos GATE=new BlockPos(5,0,-3);
    /** The scene's anchor under the median. */
    public static final BlockPos ANCHOR=new BlockPos(0,-4,-12);
    /** Where the struck come to, on the starting shoulder. */
    public static final Vec3 SHOULDER=new Vec3(.5,0,-3.9);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    public static int dir(int lane){return lane<2?1:-1;}
    public static double length(int kind){return switch(kind){case StateRouteScene.PICKUP->5;case StateRouteScene.SEMI->14;case StateRouteScene.WAGON->5.4;default->4.6;};}
    public static double height(int kind){return kind==StateRouteScene.SEMI?3.8:kind==StateRouteScene.PICKUP?2.1:1.9;}
    /** The lane a position is in, by its depth into the road; -1 off the asphalt. */
    public static int lane(double z){return z<START_FAR&&z>=-8?0:z<-8&&z>=MEDIAN_NEAR?1:z<MEDIAN_FAR&&z>=-16?2:z<-16&&z>=FAR_NEAR?3:-1;}
    public static boolean onStart(Vec3 r){return r.z<START_NEAR&&r.z>=START_FAR&&r.y>-.5;}
    public static boolean behindFence(Vec3 r){return r.z>=FENCE_Z;}
    public static boolean inMedian(Vec3 r){return r.z<MEDIAN_NEAR&&r.z>=MEDIAN_FAR;}
    public static boolean onFar(Vec3 r){return r.z<FAR_NEAR&&r.z>=FAR_FAR;}
    public static boolean onRoad(Vec3 r){return Math.abs(r.x)<HEDGE&&r.z<START_NEAR&&r.z>=FAR_FAR;}

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static BlockState mat(String name){return StateRouteRegistry.material(name).defaultBlockState();}
    static BlockState leaves(){return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    /** The road surface for a row: plain wet asphalt, or the row that carries a line. Dashes come and go every two blocks. */
    static BlockState surface(int x,int z){
        boolean dash=Math.floorMod(x,6)<3;
        return switch(z){
            case -6->mat("edge_white_s");
            case -8,-16->dash?mat("dash_n"):mat("asphalt");
            case -11->mat("edge_yellow_n");
            case -14->mat("edge_yellow_s");
            case -19->mat("edge_white_n");
            default->mat("asphalt");
        };
    }

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var rnd=new Random(9333);
        box(l,b,WEST,-5,FAR,EAST,14,0,air);
        box(l,b,WEST,-5,FAR,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,FAR,EAST,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        // The lane in from the door between cedar hedges, and the hedges along the back of the fence.
        for(int x=-HEDGE-2;x<=HEDGE+2;x++)for(int z=-2;z<=0;z++){if(Math.abs(x)<=1)set(l,b,x,-1,z,Blocks.COARSE_DIRT.defaultBlockState());else for(int y=0;y<=4;y++)set(l,b,x,y,z,leaves());}
        // The starting shoulder and the far shoulder: wet gravel.
        for(int x=-HEDGE;x<=HEDGE;x++){for(int z=-5;z<=-4;z++)set(l,b,x,-1,z,Blocks.GRAVEL.defaultBlockState());for(int z=-22;z<=-20;z++)set(l,b,x,-1,z,Blocks.GRAVEL.defaultBlockState());}
        // The four lanes, the stone under them, and the lines.
        for(int x=WEST;x<=EAST;x++)for(int z=-19;z<=-6;z++){if(z==-12||z==-13)continue;set(l,b,x,-1,z,surface(x,z));set(l,b,x,-2,z,Blocks.STONE.defaultBlockState());}
        // The median: two blocks wide and a step down, dead weeds in the ditch gravel, bent guardrail posts.
        for(int x=WEST;x<=EAST;x++)for(int z=-13;z<=-12;z++){
            set(l,b,x,-1,z,air);set(l,b,x,-2,z,(rnd.nextInt(3)==0?Blocks.GRAVEL:Blocks.COARSE_DIRT).defaultBlockState());
            if(Math.abs(x)<HEDGE&&rnd.nextInt(4)==0)set(l,b,x,-1,z,(rnd.nextBoolean()?Blocks.DEAD_BUSH:Blocks.SHORT_GRASS).defaultBlockState());
        }
        for(int x=-HEDGE+3;x<=HEDGE-3;x+=6)set(l,b,x,-1,-12,StateRouteBlock.of(StateRouteBlock.Kind.POST,Direction.SOUTH,Math.floorMod(x,18)==3?1:0));
        // The chain-link fence along the back of the starting shoulder, open where the lane comes through; the padlocked gate in it.
        for(int x=-HEDGE;x<=HEDGE;x++){if(Math.abs(x)<=1||x==GATE.getX())continue;set(l,b,x,0,FENCE_Z,StateRouteBlock.of(StateRouteBlock.Kind.CHAIN,Direction.NORTH));set(l,b,x,1,FENCE_Z,StateRouteBlock.of(StateRouteBlock.Kind.CHAIN,Direction.NORTH,1));}
        var gate=StateRouteRegistry.GATE.get().defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,GATE,gate.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,GATE.above(),gate.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        set(l,b,GATE.getX(),0,FENCE_Z+1,leaves());set(l,b,GATE.getX(),1,FENCE_Z+1,leaves());
        // Enough light on the starting shoulder to find the gate by.
        set(l,b,GATE.getX(),3,-4,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,6));
        // The far shoulder: the mailbox under the sodium lamp, the route shield on its post, the mile marker.
        set(l,b,MAILBOX,StateRouteBlock.of(StateRouteBlock.Kind.MAILBOX,Direction.SOUTH));
        for(int y=0;y<LAMP.getY();y++)set(l,b,POLE.getX(),y,POLE.getZ(),StateRouteBlock.of(StateRouteBlock.Kind.POLE,Direction.SOUTH));
        set(l,b,LAMP,StateRouteBlock.of(StateRouteBlock.Kind.LAMP,Direction.SOUTH));
        set(l,b,SHIELD.below(),StateRouteBlock.of(StateRouteBlock.Kind.POLE,Direction.SOUTH));set(l,b,SHIELD,StateRouteBlock.of(StateRouteBlock.Kind.SHIELD,Direction.SOUTH));
        set(l,b,MILE,StateRouteBlock.of(StateRouteBlock.Kind.MILE,Direction.SOUTH));
        // The corn, wet and close, up to a face of dark joists the whole width of the road.
        for(int x=-HEDGE-2;x<=HEDGE+2;x++)for(int z=-27;z<=-23;z++){set(l,b,x,-1,z,Blocks.MUD.defaultBlockState());set(l,b,x,0,z,StateRouteBlock.of(StateRouteBlock.Kind.CORN,Direction.from2DDataValue(rnd.nextInt(4))));set(l,b,x,1,z,StateRouteBlock.of(StateRouteBlock.Kind.CORN,Direction.from2DDataValue(rnd.nextInt(4)),1));}
        box(l,b,WEST,0,-28,EAST,9,-28,Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        box(l,b,WEST,-1,FAR,EAST,9,FAR,Blocks.DIRT.defaultBlockState());
        // The hedges the road runs out of at either end: the traffic comes from beyond them and goes back into them.
        for(int side:new int[]{-1,1})for(int x=HEDGE+1;x<=EAST;x++)for(int z=-27;z<=0;z++){if(z<=-6&&z>=-19)continue;int X=side*x;for(int y=0;y<=6;y++)set(l,b,X,y,z,leaves());}
        // Over the lanes the hedge is an arch for a few blocks, then closes; the traffic is drawn coming out of it.
        for(int side:new int[]{-1,1})for(int x=HEDGE+1;x<=EAST;x++)for(int z=-19;z<=-6;z++){int X=side*x;boolean median=z==-12||z==-13;
            for(int y=x>HEDGE+4||median?(median?-1:0):3;y<=6;y++)set(l,b,X,y,z,leaves());}
        set(l,b,ANCHOR,StateRouteRegistry.ANCHOR.get().defaultBlockState());
    }
}
