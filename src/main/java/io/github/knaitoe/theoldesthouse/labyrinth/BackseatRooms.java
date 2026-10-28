package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;

/** Wet blacktop held between real pines. All ground, foliage and road paint use existing native materials. */
public final class BackseatRooms {
    public static final BlockPos CAR=new BlockPos(0,0,-16);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private BackseatRooms(){}
    public static AABB bounds(BlockPos b){return new AABB(b.getX()-14,b.getY()-3,b.getZ()-58,b.getX()+15,b.getY()+18,b.getZ()+18);}
    private static void box(ServerLevel l,BlockPos b,int x,int y,int z,int X,int Y,int Z,net.minecraft.world.level.block.state.BlockState s){BuildBlocks.box(l,b.offset(x,y,z),b.offset(X,Y,Z),s,F);}
    public static void build(ServerLevel l,BlockPos b){
        box(l,b,-14,-3,-58,14,17,4,Blocks.AIR.defaultBlockState());
        box(l,b,-14,-3,-58,14,-2,4,Blocks.STONE.defaultBlockState());box(l,b,-14,-1,-58,14,-1,4,Blocks.PODZOL.defaultBlockState());
        for(int z=-58;z<=4;z++)for(int x=-4;x<=4;x++){
            var kind=Math.abs(x)==4?RoadhouseBlock.Kind.ASPHALT_EDGE:x==0?RoadhouseBlock.Kind.ASPHALT_CENTRE:RoadhouseBlock.Kind.ASPHALT;
            BuildBlocks.set(l,b.offset(x,-1,z),RoadhouseBlock.of(kind,Direction.EAST),F);
        }
        for(int z=-54;z<=0;z+=7)for(int side:new int[]{-1,1}){
            int x=side*(8+Math.floorMod(z,3));tree(l,b,x,z,11+Math.floorMod(z,4));
            for(int dz=2;dz<=4;dz++)BuildBlocks.set(l,b.offset(side*6,0,z+dz),Blocks.FERN.defaultBlockState(),F);
        }
        // A real stand of timber closes the road ahead; its native collision causes the crash.
        for(int x=-4;x<=4;x++)tree(l,b,x,-53+Math.floorMod(x,2),13+Math.floorMod(x,3));
        NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,Blocks.SPRUCE_PLANKS.defaultBlockState(),Blocks.STONE.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());
        NovelRooms.door(l,b.offset(0,0,1),Direction.SOUTH,Blocks.DARK_OAK_DOOR,false);
        passingScenery(l,b);
    }
    /** Three depths outside the glass: near reflectors/brush, broken trunks and the slower far tree line.
     * Append to the original construction order; the lane, collision stand and original car are untouched. */
    private static void passingScenery(ServerLevel l,BlockPos b){
        for(int z=-49;z<=-12;z++)for(int side:new int[]{-1,1}){
            // A worn gravel shoulder gives the white road edge something darker and rougher to pass against.
            var ground=Math.floorMod(z+side,5)==0?Blocks.COARSE_DIRT:Blocks.GRAVEL;
            BuildBlocks.set(l,b.offset(side*5,-1,z),ground.defaultBlockState(),F);
            if(Math.floorMod(z+side,6)==0)BuildBlocks.set(l,b.offset(side*6,0,z),Blocks.FERN.defaultBlockState(),F);
        }
        for(int side:new int[]{-1,1}){
            var facing=side<0?Direction.EAST:Direction.WEST;
            // The established native reflector already has small emissive faces; no full-bright woods or overlay.
            for(int z=-46+(side>0?2:0);z<=-14;z+=6){
                BuildBlocks.set(l,b.offset(side*6,0,z),RoadhouseBlock.of(RoadhouseBlock.Kind.POLE,facing),F);
                BuildBlocks.set(l,b.offset(side*6,1,z),RoadhouseBlock.of(RoadhouseBlock.Kind.REFLECTOR,facing),F);
            }
            // Interrupted low rails and a fallen trunk cross the side view without forming an enclosing wall.
            for(int z=-38;z<=-33;z++)BuildBlocks.set(l,b.offset(side*7,0,z),Blocks.DARK_OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,z>-38).setValue(FenceBlock.SOUTH,z< -33),F);
            for(int z=-26;z<=-23;z++)BuildBlocks.set(l,b.offset(side*10,0,z),Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z),F);
            for(int z=-44+(side>0?3:0);z<=-17;z+=9){
                int x=side*(8+Math.floorMod(z,2)),height=4+Math.floorMod(z+side,4);
                for(int y=0;y<height;y++)if(BuildBlocks.state(l,b.offset(x,y,z)).isAir())BuildBlocks.set(l,b.offset(x,y,z),Blocks.BIRCH_LOG.defaultBlockState(),F);
                for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(Math.abs(dx)+Math.abs(dz)<=1){var at=b.offset(x+dx,1,z+dz+2);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);}
            }
            // A second, offset line remains inside the original saved road's footprint and chunk lease.
            for(int z=-47+(side>0?2:0);z<=-13;z+=6){
                int x=side*13,height=8+Math.floorMod(z+side,5);
                for(int y=0;y<height;y++)if(BuildBlocks.state(l,b.offset(x,y,z)).isAir())BuildBlocks.set(l,b.offset(x,y,z),Blocks.SPRUCE_LOG.defaultBlockState(),F);
                for(int y=4;y<=height+1;y++)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
                    if(dx==0&&dz==0&&y<height)continue;var at=b.offset(x+dx,y,z+dz);
                    if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
                }
            }
        }
    }
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int h){
        box(l,b,x,0,z,x,h,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int y=3;y<=h+2;y++){int radius=Math.min(3,Math.max(0,(h+2-y)/3));for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(dx==0&&dz==0&&y<=h||Math.abs(dx)+Math.abs(dz)>radius+1)continue;
            BuildBlocks.set(l,b.offset(x+dx,y,z+dz),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
        }}
    }
}
