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
    }
    private static void tree(ServerLevel l,BlockPos b,int x,int z,int h){
        box(l,b,x,0,z,x,h,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int y=3;y<=h+2;y++){int radius=Math.min(3,Math.max(0,(h+2-y)/3));for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(dx==0&&dz==0&&y<=h||Math.abs(dx)+Math.abs(dz)>radius+1)continue;
            BuildBlocks.set(l,b.offset(x+dx,y,z+dz),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
        }}
    }
}
