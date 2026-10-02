package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;

/** Scenery repairs never reconstruct the well, its writing or its finite barrel. */
public final class BarnFarm {
    private static final int F=LabyrinthBuilder.flags();
    private BarnFarm(){}
    private static void at(ServerLevel l,BlockPos b,int x,int y,int z,Block block){l.setBlock(b.offset(x,y,z),block.defaultBlockState(),F);}
    public static void dress(ServerLevel l,BlockPos b){
        // Remove only the old invisible perimeter and the barn's flat upper walls.
        for(int x=-17;x<=17;x++)for(int z=-39;z<=0;z++)for(int y=0;y<=16;y++)
            if(l.getBlockState(b.offset(x,y,z)).is(Blocks.BARRIER))at(l,b,x,y,z,Blocks.AIR);
        for(int x=5;x<=15;x++)for(int z=-34;z<=-17;z++)for(int y=4;y<=7;y++)
            if(l.getBlockState(b.offset(x,y,z)).is(Blocks.SPRUCE_PLANKS))at(l,b,x,y,z,Blocks.AIR);
        // Pitched roof, structural posts, a hay loft and two open barn entrances.
        for(int z=-35;z<=-16;z++)for(int x=4;x<=16;x++){
            int y=4+Math.min(x-4,16-x)/2;
            l.setBlock(b.offset(x,y,z),LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,x<10?Direction.EAST:Direction.WEST),F);
        }
        for(int z:new int[]{-34,-17})for(int x=5;x<=15;x++)for(int y=4;y<=4+Math.min(x-4,16-x)/2;y++)at(l,b,x,y,z,Blocks.SPRUCE_PLANKS);
        for(int x:new int[]{5,15})for(int z:new int[]{-34,-25,-17})for(int y=0;y<=4;y++)at(l,b,x,y,z,Blocks.SPRUCE_LOG);
        NovelRooms.box(l,b,8,0,-17,11,3,-17,Blocks.AIR.defaultBlockState());
        NovelRooms.box(l,b,5,0,-26,5,2,-24,Blocks.AIR.defaultBlockState());
        for(int x=6;x<=8;x++)for(int z=-33;z<=-30;z++)at(l,b,x,0,z,Blocks.HAY_BLOCK);
        at(l,b,7,1,-32,Blocks.HAY_BLOCK);at(l,b,12,3,-25,Blocks.LANTERN);
        for(int z=-33;z<=-27;z++){at(l,b,11,0,z,Blocks.SPRUCE_FENCE);if(z!=-29)at(l,b,11,1,z,Blocks.SPRUCE_FENCE);}
        at(l,b,14,0,-30,Blocks.WATER_CAULDRON);
        // Worn tracks split toward the barn and the covered well. No loose quest notes.
        for(int z=-2;z>=-23;z--){int cx=z>-12?0:(-z-12)/5;for(int dx=-1;dx<=1;dx++)at(l,b,cx+dx,-1,z,Blocks.DIRT_PATH);}
        for(int x=0;x<=9;x++)at(l,b,x,-1,-18,Blocks.DIRT_PATH);
        at(l,b,-2,0,-21,Blocks.MOSSY_COBBLESTONE_WALL);at(l,b,-2,1,-21,Blocks.LANTERN);
        at(l,b,-1,0,-20,Blocks.GRAVEL);at(l,b,0,0,-20,Blocks.GRAVEL);
        // Supported rolling ground and dense trees hide the scene boundary in actual terrain.
        for(int x=-23;x<=23;x++)for(int z=-45;z<=4;z++){
            if(x>=-15&&x<=15&&z>=-37&&z<=-1)continue;
            if(Math.abs(x)<4&&z>=-1)continue;
            int edge=Math.max(Math.abs(x)-15,Math.max(-z-37,z+1));
            int height=Math.min(5,Math.max(0,edge/2))+Math.floorMod(x*13+z*7,2);
            for(int y=-2;y<=height;y++)at(l,b,x,y,z,y==height?Blocks.PODZOL:Blocks.DIRT);
            if(edge>=4){for(int y=height+1;y<=8;y++)l.setBlock(b.offset(x,y,z),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);}
        }
        for(int x:new int[]{-16,17})for(int z=-5;z>=-37;z-=7){
            for(int y=1;y<=6;y++)at(l,b,x,y,z,Blocks.SPRUCE_LOG);
            for(int y=4;y<=8;y++)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)
                if(Math.abs(dx)+Math.abs(dz)<=9-y)l.setBlock(b.offset(x+dx,y,z+dz),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
        }
        NovelRooms.safeApproach(l,b);
    }
    public static void upgrade(ServerLevel l,BlockPos origin){
        var data=LabyrinthData.get(l.getServer());var state=data.state("barn_farm_0426");
        if(state.getLong("Origin")==origin.asLong()&&state.getBoolean("Done"))return;
        dress(l,LabyrinthPlaces.base(origin,LabyrinthPlace.BARN_WELL));state.putLong("Origin",origin.asLong());state.putBoolean("Done",true);data.setState("barn_farm_0426",state);
    }
    /** Stock once after native entity chunks load, including across restarts/deaths. */
    public static void animals(ServerPlayer p){
        var data=LabyrinthData.get(p.server);var state=data.state("barn_farm_0426");if(state.getBoolean("AnimalsMade"))return;
        var b=IndianLakeRooms.base(p.server,LabyrinthPlace.BARN_WELL);if(b==null)return;
        for(int i=0;i<7;i++){
            Mob animal=(i<2?EntityType.COW:i<4?EntityType.SHEEP:EntityType.CHICKEN).create(p.serverLevel());if(animal==null)continue;
            animal.moveTo(b.getX()+7.5+i%3*2,b.getY(),b.getZ()-27.5-i/3*2,90,0);animal.setPersistenceRequired();animal.getPersistentData().putBoolean("HouseBarnAnimal",true);
            if(i==1&&animal instanceof AgeableMob calf)calf.setBaby(true);p.serverLevel().addFreshEntity(animal);
        }
        state.putBoolean("AnimalsMade",true);data.setState("barn_farm_0426",state);
    }
}
