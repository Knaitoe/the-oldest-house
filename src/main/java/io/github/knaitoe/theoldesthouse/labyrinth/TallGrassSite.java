package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.349, In the Tall Grass (docs/TALL_GRASS_0_4_349.md): a two-lane county road in the late afternoon, under the outside sky. Every
 * position is relative to the base, the ground just inside the door the reader comes through; north is -z.
 *
 * <p>The House's door stands in the brush along the south side of the road. Across the road, on the north shoulder, the House's own
 * station wagon is pulled over with its hazards ticking and its driver's door open; the county's notice is on its post beside it. Past the
 * shoulder a ditch, and past the ditch the grass, higher than a head, as far as can be seen: thin for the first few rows, so the road
 * shows back through the stems, then close. A line of rotten fence posts stands a few rows in; past it, a reader has gone in. At the heart
 * of the field the grass has been trampled down in a ring. The road runs out of the trees to the east and back into them to the west. A
 * field gate in the brush on the south side, east of the door, is the way on.
 */
public final class TallGrassSite {
    private TallGrassSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.TALL_GRASS;
    public static final int WEST=-56,EAST=56,NORTH=-112;
    /** The road: the south shoulder, the asphalt (lanes either side of the centre line), the north shoulder, the ditch. */
    public static final int SOUTH_SHOULDER=0,ROAD_S=-2,ROAD_N=-8,CENTRE_Z=-5,NORTH_SHOULDER=-9,SHOULDER_N=-10,DITCH_S=-11,DITCH_N=-12;
    /** Where the road runs into the trees at either end. */
    public static final int ROAD_W=-50,ROAD_E=50;
    /** The grass: from the ditch's far bank to the north edge, the first thin rows, the old fence line (past it a reader has gone in). */
    public static final int GRASS_S=-13,FIRST_ROWS=-18,FENCE_Z=-21,GRASS_N=-106,FIELD_W=-52,FIELD_E=52;
    /** How far in the field moves readers to: never past these, and never closer to the road than the fence line once they have gone in. */
    public static final int DEEP=-96,MOVE_W=-44,MOVE_E=44;
    /** The heart of the field, a ring trampled flat round the boy's cap. */
    public static final BlockPos HEART=new BlockPos(-6,0,-62);
    public static final int HEART_R=4;
    /** The reader's wagon on the north shoulder, nose west: its nose, its tail, and the cabin over the back four. */
    public static final int WAGON_NOSE_X=-5,WAGON_TAIL_X=0,WAGON_Z1=-9,WAGON_Z2=-10;
    /** The county's notice (the first paper) on its post at the shoulder by the wagon's tail, and where an account waits (on the same post). */
    public static final BlockPos NOTICE=new BlockPos(2,0,-10),ENDING=new BlockPos(2,1,-10);
    /** The field gate in the brush on the south side (the way on). */
    public static final BlockPos GATE=new BlockPos(14,0,1);
    /** Where the next car stops on the north shoulder (its nose), and where the field lets readers out at the end, a row in from the ditch. */
    public static final int STOP_X=18,PICKUP_LENGTH=5,LET_OUT_Z=-14;
    /** Where the boy's voice comes from before anyone has gone in: a little way into the grass, across from the wagon. */
    public static final Vec3 FIRST_VOICE=new Vec3(-2.5,1.2,-27.5);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ where things are
    public static boolean onRoad(Vec3 r){return r.z<=SOUTH_SHOULDER+1&&r.z>DITCH_S&&r.y>-.6;}
    public static boolean inGrass(Vec3 r){return r.z<=GRASS_S+.999&&r.z>GRASS_N&&r.x>FIELD_W&&r.x<FIELD_E+1;}
    public static boolean inFirstRows(Vec3 r){return inGrass(r)&&r.z>FIRST_ROWS;}
    public static boolean pastFence(Vec3 r){return r.z<FENCE_Z;}
    public static boolean nearWagon(Vec3 r,double reach){return r.x>WAGON_NOSE_X-reach&&r.x<WAGON_TAIL_X+1+reach&&r.z>WAGON_Z2-reach&&r.z<WAGON_Z1+1+reach;}
    public static Vec3 wagon(){return new Vec3((WAGON_NOSE_X+WAGON_TAIL_X+1)/2.0,1,(WAGON_Z1+WAGON_Z2+1)/2.0);}
    public static boolean isWagon(BlockPos rel){return rel.getX()>=WAGON_NOSE_X&&rel.getX()<=WAGON_TAIL_X&&rel.getZ()>=WAGON_Z2&&rel.getZ()<=WAGON_Z1&&rel.getY()>=0&&rel.getY()<=1;}
    /** The cells of the next car, parked with its nose at {@code noseX}: nose, two body lengths under the cab, bed with the back wheel, tailgate. */
    public static List<Map.Entry<BlockPos,BlockState>> pickup(int noseX){
        var out=new ArrayList<Map.Entry<BlockPos,BlockState>>();
        for(int z=SHOULDER_N;z<=NORTH_SHOULDER;z++){
            out.add(Map.entry(new BlockPos(noseX,0,z),YardBlock.of(YardBlock.Kind.PICKUP_NOSE,Direction.WEST,0)));
            out.add(Map.entry(new BlockPos(noseX+1,0,z),YardBlock.of(YardBlock.Kind.PICKUP_BODY,Direction.WEST,1)));
            out.add(Map.entry(new BlockPos(noseX+2,0,z),YardBlock.of(YardBlock.Kind.PICKUP_BODY,Direction.WEST,0)));
            out.add(Map.entry(new BlockPos(noseX+3,0,z),YardBlock.of(YardBlock.Kind.PICKUP_BED,Direction.WEST,0)));
            out.add(Map.entry(new BlockPos(noseX+4,0,z),YardBlock.of(YardBlock.Kind.PICKUP_BED,Direction.WEST,1)));
            out.add(Map.entry(new BlockPos(noseX+1,1,z),YardBlock.of(YardBlock.Kind.PICKUP_CAB,Direction.WEST,0)));
            out.add(Map.entry(new BlockPos(noseX+2,1,z),YardBlock.of(YardBlock.Kind.PICKUP_CAB,Direction.WEST,0)));
        }
        return out;
    }
    /** Whether a grass column stands full height (a reader can be set down in it) at a local column. */
    public static boolean standing(ServerLevel l,BlockPos b,int x,int z){return YardBlock.is(l.getBlockState(b.offset(x,0,z)),YardBlock.Kind.GRASS)||YardBlock.is(l.getBlockState(b.offset(x,0,z)),YardBlock.Kind.TRAMPLED);}
    /** Whether a column is the heart's ring, trampled for good. */
    public static boolean heart(int x,int z){int dx=x-HEART.getX(),dz=z-HEART.getZ();return dx*dx+dz*dz<=HEART_R*HEART_R;}

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static BlockState leaves(){return Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    static BlockState grass(int third){return YardBlock.of(YardBlock.Kind.GRASS,Direction.NORTH,third);}
    /** A full column of grass, three high, turned by its column so the field does not repeat. */
    public static void column(ServerLevel l,BlockPos b,int x,int z,int flags){
        var facing=Direction.from2DDataValue(Math.floorMod(x*7+z*13,4));
        for(int y=0;y<3;y++)l.setBlock(b.offset(x,y,z),YardBlock.of(YardBlock.Kind.GRASS,facing,y),flags);
    }

    // ------------------------------------------------------------------------------------------------ the build
    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var rnd=new Random(3490);
        box(l,b,WEST,-5,NORTH,EAST,16,1,air);
        box(l,b,WEST,-5,NORTH,EAST,-3,1,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-2,NORTH,EAST,-2,1,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,NORTH,EAST,-1,1,Blocks.GRASS_BLOCK.defaultBlockState());
        road(l,b,rnd);
        field(l,b,rnd);
        edges(l,b,rnd);
        wagon(l,b);
        // The county's notice on its post at the shoulder, by the wagon's tail.
        set(l,b,NOTICE.getX(),0,NOTICE.getZ(),DeerRoadBlock.of(DeerRoadBlock.Kind.NOTICE,Direction.NORTH));
        // The field gate's frame in the brush across the road; the gate itself is the way on, hung by OnwardDoors.
        for(int y=0;y<=2;y++)set(l,b,GATE.getX(),y,GATE.getZ(),Blocks.AIR.defaultBlockState());
        for(int x=GATE.getX()-1;x<=GATE.getX()+1;x+=2)for(int y=0;y<=2;y++)set(l,b,x,y,GATE.getZ(),Blocks.SPRUCE_FENCE.defaultBlockState());
        set(l,b,GATE.getX(),2,GATE.getZ(),Blocks.SPRUCE_SLAB.defaultBlockState());
    }
    static void road(ServerLevel l,BlockPos b,Random rnd){
        for(int x=ROAD_W-6;x<=ROAD_E+6;x++){
            for(int z=SOUTH_SHOULDER;z>=SOUTH_SHOULDER-1;z--)set(l,b,x,-1,z,rnd.nextInt(5)==0?Blocks.COARSE_DIRT.defaultBlockState():Blocks.GRAVEL.defaultBlockState());
            for(int z=ROAD_S;z>=ROAD_N;z--){
                var k=z==ROAD_S||z==ROAD_N?RoadhouseBlock.Kind.ASPHALT_EDGE:z==CENTRE_Z&&Math.floorMod(x,4)!=0?RoadhouseBlock.Kind.ASPHALT_CENTRE:RoadhouseBlock.Kind.ASPHALT;
                var facing=z==ROAD_N?Direction.SOUTH:Direction.NORTH;
                set(l,b,x,-1,z,RoadhouseBlock.of(k,facing));set(l,b,x,-2,z,Blocks.STONE.defaultBlockState());
            }
            for(int z=NORTH_SHOULDER;z>=SHOULDER_N;z--)set(l,b,x,-1,z,rnd.nextInt(6)==0?Blocks.COARSE_DIRT.defaultBlockState():Blocks.GRAVEL.defaultBlockState());
            // The ditch: a step down off the shoulder, its bed damp, dry stems along both banks.
            for(int z=DITCH_S;z>=DITCH_N;z--){set(l,b,x,-1,z,Blocks.AIR.defaultBlockState());set(l,b,x,-2,z,Blocks.MUD.defaultBlockState());
                if(rnd.nextInt(4)==0)set(l,b,x,-1,z,Blocks.SHORT_GRASS.defaultBlockState());}
        }
    }
    static void field(ServerLevel l,BlockPos b,Random rnd){
        for(int z=GRASS_S;z>=GRASS_N;z--)for(int x=FIELD_W;x<=FIELD_E;x++){
            if(heart(x,z)){set(l,b,x,0,z,YardBlock.of(YardBlock.Kind.TRAMPLED,Direction.from2DDataValue(Math.floorMod(x+z,4)),0));continue;}
            // The first rows are thin: every other stem gone, more toward the ditch, so the road shows back through them.
            if(z>FIRST_ROWS){int gap=z-FIRST_ROWS;if(rnd.nextInt(6)<gap)continue;}
            column(l,b,x,z,F);
        }
        // The old fence line: a post every seven, some leaning, a strand of wire hanging off one or two.
        for(int x=FIELD_W+3;x<=FIELD_E-3;x+=7){
            int px=x+rnd.nextInt(3)-1;set(l,b,px,0,FENCE_Z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
            if(rnd.nextInt(3)>0)set(l,b,px,1,FENCE_Z,Blocks.SPRUCE_FENCE.defaultBlockState());else set(l,b,px,1,FENCE_Z,grass(1));
            set(l,b,px,2,FENCE_Z,grass(2));
        }
        // The boy's cap at the heart.
        set(l,b,HEART.getX(),0,HEART.getZ(),YardBlock.of(YardBlock.Kind.CAP,Direction.SOUTH,0));
    }
    static void edges(ServerLevel l,BlockPos b,Random rnd){
        // The brush along the south side of the road, the door's wall in it, and a tree or two standing out of it.
        for(int x=WEST;x<=EAST;x++){if(Math.abs(x)<=3)continue;for(int y=0;y<=3;y++)set(l,b,x,y,1,leaves());if(rnd.nextInt(3)==0)set(l,b,x,4,1,leaves());}
        for(int x=WEST+4;x<=EAST-4;x+=9+rnd.nextInt(5)){if(Math.abs(x)<=6||Math.abs(x-GATE.getX())<=3)continue;tree(l,b,x,1,7+rnd.nextInt(3),rnd);}
        // The road runs out of the trees at either end; trees close the field's far edge and its sides.
        for(int x=WEST;x<=ROAD_W-2;x++)for(int z=SOUTH_SHOULDER;z>=SHOULDER_N;z--)for(int y=0;y<=6;y++)set(l,b,x,y,z,y<2&&rnd.nextInt(5)==0?Blocks.OAK_LOG.defaultBlockState():leaves());
        for(int x=ROAD_E+2;x<=EAST;x++)for(int z=SOUTH_SHOULDER;z>=SHOULDER_N;z--)for(int y=0;y<=6;y++)set(l,b,x,y,z,y<2&&rnd.nextInt(5)==0?Blocks.OAK_LOG.defaultBlockState():leaves());
        for(int x=WEST;x<=EAST;x++)for(int z=NORTH;z<=GRASS_N-1;z++)for(int y=0;y<=4;y++)set(l,b,x,y,z,y<1&&rnd.nextInt(7)==0?Blocks.OAK_LOG.defaultBlockState():leaves());
        for(int z=NORTH;z<=DITCH_S;z++)for(int y=0;y<=4;y++){for(int x=WEST;x<FIELD_W;x++)set(l,b,x,y,z,leaves());for(int x=FIELD_E+1;x<=EAST;x++)set(l,b,x,y,z,leaves());}
    }
    /** The House's own wagon, nose west on the north shoulder, two wide, six long, its cabin over the back four, hazards on. */
    static void wagon(ServerLevel l,BlockPos b){
        for(int z=WAGON_Z2;z<=WAGON_Z1;z++){
            set(l,b,WAGON_NOSE_X,0,z,YardBlock.of(YardBlock.Kind.NOSE,Direction.WEST,1));
            for(int x=WAGON_NOSE_X+1;x<WAGON_TAIL_X;x++)set(l,b,x,0,z,YardBlock.of(YardBlock.Kind.BODY,Direction.WEST,x==WAGON_NOSE_X+1||x==WAGON_TAIL_X-1?1:0));
            set(l,b,WAGON_TAIL_X,0,z,YardBlock.of(YardBlock.Kind.TAIL,Direction.WEST,1));
            for(int x=WAGON_NOSE_X+2;x<=WAGON_TAIL_X;x++)set(l,b,x,1,z,YardBlock.of(YardBlock.Kind.CABIN,Direction.WEST,0));
        }
    }
    /** The hazards: on (stage 1) or off (0), at the nose and the tail. */
    public static void hazards(ServerLevel l,BlockPos b,boolean on){
        for(int z=WAGON_Z2;z<=WAGON_Z1;z++)for(int x:new int[]{WAGON_NOSE_X,WAGON_TAIL_X}){
            var at=b.offset(x,0,z);var st=l.getBlockState(at);
            if((YardBlock.is(st,YardBlock.Kind.NOSE)||YardBlock.is(st,YardBlock.Kind.TAIL))&&st.getValue(YardBlock.STAGE)!=(on?1:0))l.setBlock(at,st.setValue(YardBlock.STAGE,on?1:0),Block.UPDATE_CLIENTS);
        }
    }
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int h,Random rnd){
        for(int y=0;y<h;y++)set(l,b,x,y,z,Blocks.OAK_LOG.defaultBlockState());
        for(int y=3;y<=h;y++){int r=y<h-1?2:1;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if((dx!=0||dz!=0||y==h)&&Math.abs(dx)+Math.abs(dz)<=r+1&&rnd.nextInt(6)!=0){var at=b.offset(x+dx,y,z+dz);if(z+dz<=1&&BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,leaves(),F);}}
    }
}
