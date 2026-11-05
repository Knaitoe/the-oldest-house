package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * 0.4.310, A Deer in the Road (docs/DEER_ROAD_0_4_310.md): one stretch of county road at night, under the outside sky. Every position
 * is relative to the base, the service door's floor.
 *
 * <p>The road runs north between a wire fence and a field on the west and, on the east, a gravel shoulder, the drainage ditch and a
 * tree line. At its south end it bends west into the trees; the service door stands in the end wall of a concrete pump house on the
 * outside of the bend (the entry). The car comes up out of the bend. Brush on the east bank hides where the doe steps out. Behind
 * where the car stops the shoulder is hatched, with the county's notice on its post; the ditch runs on past the culvert and its
 * grate to the hinged drainage gate in its headwall (the scene's way on), and a marker post by the culvert puts the gate in sight.
 */
public final class DeerRoadSite {
    private DeerRoadSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.DEER_ROAD;
    public static final int WEST=-24,EAST=14,FAR=-94;
    /** The road: asphalt from {@link #ROAD_W} to {@link #ROAD_E}, its centre line at {@link #CENTRE}; the northbound lane's middle. */
    public static final int ROAD_W=-16,ROAD_E=-8,CENTRE=-12,LANE=-10;
    /** The shoulder, the ditch's bed (two down) and its banks. */
    public static final int SHOULDER=-7,DITCH=-4;
    /** The scene's anchor under the road. */
    public static final BlockPos ANCHOR=new BlockPos(-12,-4,-46);
    /** Where the car's root cell starts, coming out of the bend, and the line the doe crosses. */
    public static final int START_Z=-10,DOE_Z=-50;
    /** The hatched shoulder behind the wreck, where the triangle is set and the next vehicle is flagged. */
    public static final int HATCH_Z0=-44,HATCH_Z1=-34;
    /** The county's notice (the first paper), the marker post by the culvert, the culvert's grate. */
    public static final BlockPos NOTICE=new BlockPos(-6,0,-33),MARKER=new BlockPos(-2,0,-61),GRATE=new BlockPos(-5,-2,-63);
    /** The drainage gate's lower half in its headwall at the head of the ditch, approached from the south. */
    public static final BlockPos GATE=new BlockPos(-4,-2,-84);
    public static final BlockPos LAMP=new BlockPos(0,3,0);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static BlockState asphalt(RoadhouseBlock.Kind k){return RoadhouseBlock.of(k,Direction.EAST);}
    /** The road's centre for a row: straight up to the bend, then curving west into the trees. */
    static double centre(int z){if(z<=-12)return CENTRE;double t=(z+12)/13.0;return CENTRE-11*t*t;}
    /** On the road surface, the shoulder, in the ditch or out in the field: for ambient checks. */
    public static boolean inDitch(net.minecraft.world.phys.Vec3 r){return r.x>DITCH-1.6&&r.x<DITCH+1.6&&r.y<-.6&&r.z<-4&&r.z>GATE.getZ();}
    public static boolean onHatch(net.minecraft.world.phys.Vec3 r){return r.x>SHOULDER-1.2&&r.x<SHOULDER+1.6&&r.z>HATCH_Z0&&r.z<HATCH_Z1+1&&r.y>-.5&&r.y<2;}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();var rnd=new Random(3100);
        box(l,b,WEST,-5,FAR,EAST,15,0,air);
        box(l,b,WEST,-5,FAR,EAST,-3,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-2,FAR,EAST,-2,0,Blocks.DIRT.defaultBlockState());
        box(l,b,WEST,-1,FAR,EAST,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        // The road, its edges and centre line, and the bend at the south end.
        for(int z=FAR;z<=0;z++){
            double c=centre(z);
            for(int x=WEST;x<=-5;x++){
                double d=x-c;if(Math.abs(d)>4.5)continue;
                boolean straight=z<=-12;var k=Math.abs(d)>3.5?(straight?RoadhouseBlock.Kind.ASPHALT_EDGE:RoadhouseBlock.Kind.ASPHALT):Math.abs(d)<.5&&straight&&Math.floorMod(z,4)!=0?RoadhouseBlock.Kind.ASPHALT_CENTRE:RoadhouseBlock.Kind.ASPHALT;
                set(l,b,x,-1,z,asphalt(k));set(l,b,x,-2,z,Blocks.STONE.defaultBlockState());
            }
        }
        // The gravel shoulder; the hatched stretch behind where the car stops.
        for(int z=FAR;z<=-12;z++)for(int x=SHOULDER;x<=SHOULDER+1;x++)set(l,b,x,-1,z,z>=HATCH_Z0&&z<=HATCH_Z1?DeerRoadRegistry.material("hatch").defaultBlockState():Blocks.GRAVEL.defaultBlockState());
        // The ditch: a step down each side and the bed two below the road, running high with rain as far as the culvert, damp past it.
        for(int z=-11;z>=GATE.getZ()-1;z--){
            for(int x=DITCH-1;x<=DITCH+1;x++){set(l,b,x,-1,z,air);if(x!=DITCH)set(l,b,x,-2,z,Blocks.COARSE_DIRT.defaultBlockState());}
            set(l,b,DITCH,-2,z,z>GRATE.getZ()?Blocks.WATER.defaultBlockState():air);
            set(l,b,DITCH,-3,z,DeerRoadRegistry.material("mud").defaultBlockState());
            for(int x=DITCH-1;x<=DITCH+1;x+=2)if(rnd.nextInt(5)==0)set(l,b,x,-1,z,Blocks.SHORT_GRASS.defaultBlockState());
        }
        // The culvert: a pipe under the road from the ditch's west bank, its grate in the bank, dark behind it.
        box(l,b,ROAD_W-2,-4,GRATE.getZ()-1,DITCH-2,-1,GRATE.getZ()+1,Blocks.STONE.defaultBlockState());
        box(l,b,ROAD_W-1,-3,GRATE.getZ(),DITCH-2,-2,GRATE.getZ(),air);
        set(l,b,DITCH-1,-2,GRATE.getZ(),DeerRoadBlock.of(DeerRoadBlock.Kind.GRATE,Direction.EAST));
        set(l,b,DITCH-1,-1,GRATE.getZ(),Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        set(l,b,DITCH-1,-2,GRATE.getZ()-1,Blocks.STONE.defaultBlockState());set(l,b,DITCH-1,-2,GRATE.getZ()+1,Blocks.STONE.defaultBlockState());
        set(l,b,MARKER.getX(),MARKER.getY(),MARKER.getZ(),DeerRoadBlock.of(DeerRoadBlock.Kind.MARKER,Direction.WEST));
        // The headwall at the head of the ditch and the drainage gate in it.
        box(l,b,DITCH-3,-3,GATE.getZ()-1,DITCH+3,0,GATE.getZ()-1,Blocks.STONE.defaultBlockState());
        box(l,b,DITCH-3,0,GATE.getZ()-1,DITCH+3,0,GATE.getZ()-1,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        box(l,b,DITCH-3,-3,GATE.getZ()-6,DITCH+3,1,GATE.getZ()-2,Blocks.DIRT.defaultBlockState());
        var gate=DeerRoadRegistry.GATE.get().defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
        set(l,b,GATE.getX(),GATE.getY(),GATE.getZ(),gate.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,GATE.getX(),GATE.getY()+1,GATE.getZ(),gate.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        // The county's notice by the hatched shoulder, the crossing sign above it.
        set(l,b,NOTICE.getX(),0,NOTICE.getZ(),DeerRoadBlock.of(DeerRoadBlock.Kind.NOTICE,Direction.SOUTH));
        set(l,b,NOTICE.getX(),1,NOTICE.getZ(),DeerRoadBlock.of(DeerRoadBlock.Kind.SIGN,Direction.SOUTH));
        // The west: a verge, a wire fence on posts and the field beyond, a few trees standing in it.
        for(int z=FAR;z<=-14;z++){
            if(Math.floorMod(z,3)==0)set(l,b,ROAD_W-3,0,z,Blocks.SPRUCE_FENCE.defaultBlockState());
            else set(l,b,ROAD_W-3,0,z,Blocks.IRON_BARS.defaultBlockState());
            for(int x=WEST;x<=ROAD_W-4;x++)if(rnd.nextInt(4)==0)set(l,b,x,0,z,(rnd.nextBoolean()?Blocks.SHORT_GRASS:Blocks.FERN).defaultBlockState());
        }
        for(int z=FAR+3;z<=-18;z+=7+rnd.nextInt(5))tree(l,b,WEST+2+rnd.nextInt(3),z,7+rnd.nextInt(4),rnd);
        // The east: the bank, the tree line and the brush the doe comes out of.
        for(int z=FAR;z<=-8;z+=3+rnd.nextInt(2))tree(l,b,1+rnd.nextInt(4),z,8+rnd.nextInt(5),rnd);
        for(int z=FAR;z<=-8;z++)for(int x=DITCH+2;x<=EAST;x++)if(rnd.nextInt(3)==0&&BuildBlocks.state(l,b.offset(x,0,z)).isAir())set(l,b,x,0,z,leaves());
        for(int z=DOE_Z-4;z<=DOE_Z+6;z++)for(int x=DITCH+2;x<=DITCH+4;x++){set(l,b,x,0,z,leaves());if(rnd.nextBoolean())set(l,b,x,1,z,leaves());}
        // The bend into the trees at the south end, closing the road off; trees close the far north end too.
        for(int x=WEST;x<=-8;x++)for(int z=-2;z<=0;z++)for(int y=0;y<=5;y++)set(l,b,x,y,z,y<2&&rnd.nextInt(4)==0?Blocks.SPRUCE_LOG.defaultBlockState():leaves());
        for(int x=WEST;x<=EAST;x++)for(int z=FAR;z<=FAR+1;z++)for(int y=0;y<=7;y++)set(l,b,x,y,z,leaves());
        for(int z=FAR;z<=0;z++)for(int y=0;y<=6;y++){set(l,b,WEST,y,z,leaves());set(l,b,EAST,y,z,leaves());}
        // The pump house's end wall round the service door, its lamp, a gutter of a roof.
        box(l,b,-7,0,1,7,4,1,Blocks.STONE_BRICKS.defaultBlockState());box(l,b,-7,5,0,7,5,1,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        box(l,b,-4,-1,-3,4,-1,0,Blocks.SMOOTH_STONE.defaultBlockState());
        set(l,b,LAMP.getX(),LAMP.getY(),LAMP.getZ(),DeerRoadBlock.of(DeerRoadBlock.Kind.LAMP,Direction.NORTH));
        // A path of gravel from the service door to the shoulder, over the ditch's head on a slab.
        for(int x=-6;x<=-1;x++)set(l,b,x,-1,-5,Blocks.GRAVEL.defaultBlockState());
        set(l,b,ANCHOR.getX(),ANCHOR.getY(),ANCHOR.getZ(),DeerRoadRegistry.ANCHOR.get().defaultBlockState());
    }
    static BlockState leaves(){return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int h,Random rnd){
        for(int y=0;y<h;y++)set(l,b,x,y,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int y=3;y<=h;y++){int r=y<h-1?2:1;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if((dx!=0||dz!=0||y==h)&&Math.abs(dx)+Math.abs(dz)<=r+1&&rnd.nextInt(6)!=0){var at=b.offset(x+dx,y,z+dz);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,leaves(),F);}}
    }
}
