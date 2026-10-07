package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Two small, saved geometry repairs; no actor, inventory, writing or story reconstruction. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SceneSupportRepairs {
    public static final String STATE="scene_supports_0453";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private SceneSupportRepairs(){}
    private static boolean applies(LabyrinthPlace p){return p==LabyrinthPlace.TED_CAVER||p==LabyrinthPlace.BARN_WELL;}
    public static AABB area(BlockPos b,LabyrinthPlace p){
        return p==LabyrinthPlace.TED_CAVER?new AABB(b.offset(-9,-4,-47),b.offset(0,5,-35))
                :new AABB(b.offset(3,-1,-36),b.offset(18,5,-3));
    }
    private static boolean safe(ServerLevel l,BlockPos at,BlockState before,BlockState next,List<LivingEntity> bodies){
        var added=Shapes.joinUnoptimized(next.getCollisionShape(l,at),before.getCollisionShape(l,at),BooleanOp.ONLY_FIRST);
        for(var box:added.toAabbs())for(var body:bodies)if(box.move(at).intersects(body.getBoundingBox()))return false;
        return true;
    }
    /** A native fence recomputes its sides without changing type, waterlogging or a gate's open state. */
    public static boolean barn(ServerLevel l,BlockPos b){
        var bodies=l.getEntitiesOfClass(LivingEntity.class,area(b,LabyrinthPlace.BARN_WELL),e->e.isAlive()&&!e.isSpectator());
        boolean ready=true;
        for(int x=4;x<=16;x++)for(int y=0;y<=3;y++)for(int z=-35;z<=-5;z++){
            var at=b.offset(x,y,z);var old=BuildBlocks.state(l,at);
            if(!(old.getBlock() instanceof FenceBlock)&&!(old.getBlock() instanceof FenceGateBlock))continue;
            var next=old;
            for(var d:Direction.Plane.HORIZONTAL)next=next.updateShape(d,BuildBlocks.state(l,at.relative(d)),l,at,at.relative(d));
            if(next.equals(old))continue;
            if(!safe(l,at,old,next,bodies)){ready=false;continue;}
            BuildBlocks.set(l,at,next,F);
        }
        return ready;
    }
    private static boolean change(ServerLevel l,BlockPos at,BlockState next,List<LivingEntity> bodies){
        var old=BuildBlocks.state(l,at);if(old.equals(next))return true;
        if(l.getBlockEntity(at)!=null||!safe(l,at,old,next,bodies))return false;
        BuildBlocks.set(l,at,next,F);return true;
    }
    /** Keep the actual marked stone at its interaction coordinate and tie it back into the cave wall. */
    public static boolean cave(ServerLevel l,BlockPos b){
        var bodies=l.getEntitiesOfClass(LivingEntity.class,area(b,LabyrinthPlace.TED_CAVER),e->e.isAlive()&&!e.isSpectator());
        boolean ready=true;
        if(BuildBlocks.state(l,b.offset(CaverCave.MARK)).is(Blocks.CHISELED_DEEPSLATE)){
            for(int x=-8;x<=-4;x++)for(int z=-41;z<=-40;z++)for(int y=-3;y<=-2;y++){
                if(x==-4&&y==-2&&z==-40)continue;
                var at=b.offset(x,y,z);
                // A player's existing torch or other placed block is retained.
                if(BuildBlocks.state(l,at).isAir())ready&=change(l,at,Blocks.STONE.defaultBlockState(),bodies);
            }
        }
        var node=b.offset(-6,0,-43);
        if(BuildBlocks.state(l,node).is(Blocks.CALCITE)){
            var above=node.above();var old=BuildBlocks.state(l,above);
            if(old.isAir()||old.is(Blocks.POINTED_DRIPSTONE)&&old.getValue(PointedDripstoneBlock.TIP_DIRECTION)==Direction.UP)
                ready&=change(l,above,Blocks.CALCITE.defaultBlockState(),bodies);
            // Its top now meets the real ceiling; the tip hangs down from the mineral crust.
            if(BuildBlocks.state(l,above).is(Blocks.CALCITE)&&BuildBlocks.state(l,node.below()).isAir())
                ready&=change(l,node.below(),Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION,Direction.DOWN),bodies);
        }
        return ready;
    }
    public static boolean repairOnce(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        if(!applies(p)||!l.dimension().equals(NovelRooms.dimension(p)))return false;
        var data=LabyrinthData.get(l.getServer());String key=origin.asLong()+":"+p.id();
        if(data.state(STATE).getBoolean(key))return true;
        var b=LabyrinthPlaces.base(origin,p);if(b==null)return false;var area=area(b,p);
        if(l.players().stream().anyMatch(player->area.inflate(12).intersects(player.getCamera().getBoundingBox())))return false;
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)
            for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)
                if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;
        boolean ready=p==LabyrinthPlace.TED_CAVER?cave(l,b):barn(l,b);
        if(ready){var done=data.state(STATE);done.putBoolean(key,true);data.setState(STATE,done);}return ready;
    }
    public static void forget(net.minecraft.server.MinecraftServer s,BlockPos origin,LabyrinthPlace p){
        if(!applies(p))return;var data=LabyrinthData.get(s);var done=data.state(STATE);done.remove(origin.asLong()+":"+p.id());data.setState(STATE,done);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var s=event.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=19||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(s);
        for(var p:List.of(LabyrinthPlace.TED_CAVER,LabyrinthPlace.BARN_WELL))if(data.door(p.entryDoorId())!=null&&LabyrinthBuilder.isPlaceReady(data,p)){
            var l=s.getLevel(NovelRooms.dimension(p));if(l!=null)repairOnce(l,origin,p);
        }
    }
}
