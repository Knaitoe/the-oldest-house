package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Small rooted rock and timber recesses, added once without repairing player removals or moving residents. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaceyCover {
    public static final String STATE="stacey_cover_0459";
    public static final List<LabyrinthPlace> SITES=List.of(LabyrinthPlace.DROWNED_TOWN,LabyrinthPlace.COSTUME_NIGHT,LabyrinthPlace.MOVIE_NIGHT,LabyrinthPlace.WINTER_LAKE);
    private StaceyCover(){}
    public static List<BlockPos> centers(LabyrinthPlace p){return p==LabyrinthPlace.DROWNED_TOWN
            ?List.of(new BlockPos(-13,0,-29),new BlockPos(22,0,-49),new BlockPos(-23,0,-53))
            :List.of(new BlockPos(-23,0,-29),new BlockPos(23,0,-49),new BlockPos(-20,0,-62));}
    public static AABB area(BlockPos b,LabyrinthPlace p){
        var r=p.room();return new AABB(b.getX()+r.minX(),b.getY()-13,b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+5,b.getZ()+1);
    }
    private static String key(BlockPos b,LabyrinthPlace p){return b.asLong()+":"+p.id();}
    private static boolean safe(ServerLevel l,BlockPos at,BlockState expected){
        if(!l.isLoaded(at)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4))
                ||l.getBlockEntity(at)!=null||!l.getBlockState(at).equals(expected))return false;
        var box=new AABB(at);if(!l.getEntitiesOfClass(LivingEntity.class,box.inflate(.02),LivingEntity::isAlive).isEmpty())return false;
        return l.players().stream().noneMatch(p->box.inflate(48).intersects(p.getCamera().getBoundingBox()));
    }
    private static boolean add(ServerLevel l,BlockPos at,BlockState next){
        var before=BuildBlocks.state(l,at);if(!before.isAir()&&!before.is(Blocks.WATER))return true;
        if(l.getBlockEntity(at)!=null)return true;
        return BuildBlocks.guardedSet(l,at,next,LabyrinthBuilder.flags(),()->l.getBlockState(at).equals(next)||safe(l,at,before));
    }
    /** Leaves a two-wide open pocket and two ways around the rock; the central route and grass refuges stay clear. */
    public static boolean build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        if(!SITES.contains(p))return true;boolean complete=true;
        for(var relative:centers(p)){
            var center=b.offset(relative);var support=BuildBlocks.state(l,center.below());
            if(support.is(Blocks.GRASS_BLOCK)||!(support.is(Blocks.WATER)||support.isCollisionShapeFullBlock(l,center.below())))continue;
            for(int dx=-1;dx<=1;dx++)for(int dz=0;dz<=2;dz++){
                if(dz<2&&dx>-1)continue;var foot=center.offset(dx,0,dz);
                var ground=BuildBlocks.state(l,foot.below());if(ground.is(Blocks.GRASS_BLOCK))continue;
                if(ground.is(Blocks.WATER)){
                    int floor=-1;while(floor>-12&&BuildBlocks.state(l,foot.offset(0,floor,0)).is(Blocks.WATER))floor--;
                    if(BuildBlocks.state(l,foot.offset(0,floor,0)).getCollisionShape(l,foot.offset(0,floor,0)).isEmpty())continue;
                    for(int y=floor+1;y<0;y++)complete&=add(l,foot.offset(0,y,0),Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }else if(!ground.isCollisionShapeFullBlock(l,foot.below()))continue;
                for(int y=0;y<=2;y++){
                    var rock=(dx==-1&&dz==0?Blocks.STRIPPED_SPRUCE_WOOD:y==2&&dx==1?Blocks.MOSSY_COBBLESTONE_SLAB:Blocks.MOSSY_COBBLESTONE).defaultBlockState();
                    complete&=add(l,foot.above(y),rock);
                }
            }
            var branch=center.offset(-1,3,1);complete&=add(l,branch,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
            complete&=add(l,branch.east(),Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        }
        return complete;
    }
    public static void fresh(ServerLevel l,BlockPos b,LabyrinthPlace p){
        if(!SITES.contains(p))return;
        if(build(l,b,p))BuildBlocks.after(l,()->{var d=LabyrinthData.get(l.getServer());var own=d.state(STATE);own.putBoolean(key(b,p),true);d.setState(STATE,own);});
    }
    public static boolean upgrade(ServerLevel l,BlockPos b,LabyrinthPlace p){
        if(!SITES.contains(p))return false;var d=LabyrinthData.get(l.getServer());var own=d.state(STATE);
        if(own.getBoolean(key(b,p))||!SceneVacancy.ready(l,area(b,p),48))return false;
        if(!build(l,b,p))return false;own.putBoolean(key(b,p),true);d.setState(STATE,own);return true;
    }
    public static void forget(ServerLevel l,BlockPos b,LabyrinthPlace p){var d=LabyrinthData.get(l.getServer());var own=d.state(STATE);own.remove(key(b,p));d.setState(STATE,own);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(e.getServer().getTickCount()%20!=0)return;var origin=HouseSavedData.get(e.getServer()).houseOrigin();if(origin==null)return;
        var d=LabyrinthData.get(e.getServer());var l=e.getServer().getLevel(HouseDimensions.OUTSIDE);if(l==null)return;
        for(var p:SITES)if(LabyrinthBuilder.isPlaceReady(d,p))upgrade(l,LabyrinthPlaces.base(origin,p),p);
    }
}
