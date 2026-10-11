package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** One bounded saved-room repair; no actor, paper, supply or personal progress is rebuilt. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ElkFanRoom {
    public static final String STATE="elk_fan_ladder_0480";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private ElkFanRoom(){}
    public static java.util.Map<BlockPos,BlockState> frame(BlockPos b){
        var cells=new LinkedHashMap<BlockPos,BlockState>();
        for(int part=0;part<30;part++)if(FanLadderGeometry.BOXES[part].length>0)
            cells.put(b.offset(0,part/5,-17+part%5-2),FanLadderBlock.part(part));
        return cells;
    }
    public static void build(ServerLevel l,BlockPos b){frame(b).forEach((at,s)->BuildBlocks.set(l,at,s,F));BuildBlocks.after(l,()->{
        var origin=HouseSavedData.get(l.getServer()).houseOrigin();if(origin!=null&&b.equals(LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_FAN))){var done=new net.minecraft.nbt.CompoundTag();done.putBoolean("Done",true);LabyrinthData.get(l.getServer()).setStateEntry(STATE,Long.toString(origin.asLong()),done);}
    });}
    public static boolean standing(ServerLevel l,BlockPos b){return frame(b).entrySet().stream().allMatch(e->l.getBlockState(e.getKey()).equals(e.getValue()));}
    public static boolean onLadder(net.minecraft.server.level.ServerPlayer p,BlockPos b){
        return p.getY()>=b.getY()+4.5&&p.getY()<b.getY()+6&&Math.abs(p.getX()-b.getX()-.5)<1.2
                &&Math.abs(p.getZ()-b.getZ()+16.5)<1.2&&standing(p.serverLevel(),b);
    }
    public static boolean repair(ServerLevel l,BlockPos b){
        var area=new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(-1,-1,-21)),net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(2,9,-12)));
        if(!ScenePolish.loaded(l,area,b)||l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox()))
                ||!l.getEntitiesOfClass(Entity.class,area,e->e.isAlive()).isEmpty())return false;
        var cells=new LinkedHashMap<BlockPos,BlockState>();
        for(int y=0;y<4;y++){
            var rail=b.offset(0,y,-17);if(l.getBlockState(rail).is(Blocks.LADDER))cells.put(rail,Blocks.AIR.defaultBlockState());
            var post=b.offset(0,y,-18);if(l.getBlockState(post).is(Blocks.DARK_OAK_PLANKS))cells.put(post,Blocks.AIR.defaultBlockState());
        }
        for(var entry:frame(b).entrySet()){
            var old=l.getBlockState(entry.getKey());
            if(!old.isAir()&&!old.equals(entry.getValue())&&!cells.containsKey(entry.getKey()))return false;
            cells.put(entry.getKey(),entry.getValue());
        }
        cells.forEach((at,s)->l.setBlock(at,s,F));return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(e.getServer().getTickCount()%40!=0)return;var origin=HouseSavedData.get(e.getServer()).houseOrigin();
        if(origin==null)return;var d=LabyrinthData.get(e.getServer());if(!LabyrinthBuilder.isPlaceReady(d,LabyrinthPlace.ELK_FAN))return;
        var key=Long.toString(origin.asLong());if(d.stateEntry(STATE,key).getBoolean("Done"))return;
        var l=e.getServer().getLevel(HouseDimensions.INTERIOR);var b=LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_FAN);
        if(l!=null&&b!=null&&repair(l,b)){var done=new net.minecraft.nbt.CompoundTag();done.putBoolean("Done",true);d.setStateEntry(STATE,key,done);}
    }
}
