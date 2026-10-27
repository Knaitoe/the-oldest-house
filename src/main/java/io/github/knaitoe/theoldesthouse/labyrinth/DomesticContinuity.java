package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

/** A one-time match of the expanded homes to their existing fragments, preserving every original container. */
public final class DomesticContinuity {
    public static final String STATE="domestic_continuity134";
    private static final Map<String,Map<BlockPos,BlockState>> BEFORE=new HashMap<>();
    private DomesticContinuity(){}
    private static boolean safe(ServerLevel l,AABB area){
        for(int x=(int)Math.floor(area.minX)>>4;x<=((int)Math.ceil(area.maxX)-1)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=((int)Math.ceil(area.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        return l.players().stream().noneMatch(p->area.inflate(16).intersects(p.getCamera().getBoundingBox()))&&l.getEntitiesOfClass(LivingEntity.class,area,LivingEntity::isAlive).isEmpty();
    }
    private static List<BlockPos> small(LabyrinthPlace hall){return switch(hall){case STRAIGHT_HALL->List.of(new BlockPos(-3,0,-20));case CROSS_HALL->List.of(new BlockPos(-10,0,-9));default->List.of();};}
    public static void freshSmall(ServerLevel l,BlockPos b,LabyrinthPlace hall){for(var rel:small(hall))if(BuildBlocks.state(l,b.offset(rel)).isAir())BuildBlocks.set(l,b.offset(rel),UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.FRIDGE,hall==LabyrinthPlace.STRAIGHT_HALL?Direction.WEST:Direction.SOUTH),LabyrinthBuilder.flags());}
    public static void complete(ServerLevel l,BlockPos b,DomesticLives.Home h){var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.putBoolean(DomesticLives.key(b,h),true);d.setState(STATE,t);}
    public static boolean step(ServerLevel l,BlockPos b,DomesticLives.Home h){
        var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);String key=DomesticLives.key(b,h);if(t.getBoolean(key)||!d.state(DomesticLives.STATE).getBoolean(key)||!safe(l,DomesticLives.workArea(b,h)))return false;
        var before=BEFORE.computeIfAbsent(h.id(),id->{var out=new HashMap<BlockPos,BlockState>();var old=DomesticLives0133.HOMES.stream().filter(x->x.id().equals(id)).findFirst().orElseThrow();for(var e:DomesticLives0133.plan(old))out.put(e.at(),e.next());return out;});
        var plan=DomesticLives.plan(h);int cursor=t.getInt(key+":cursor"),visits=0;long until=System.nanoTime()+3_000_000;
        while(cursor<plan.size()&&visits++<256&&(visits==1||System.nanoTime()<until)){
            var e=plan.get(cursor++);var old=before.get(e.at());if(old==null||old.equals(e.next()))continue;var at=b.offset(e.at());if(l.getBlockEntity(at)!=null||!l.getBlockState(at).equals(old))continue;
            l.setBlock(at,e.next(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            if(e.next().is(DomesticRegistry.RECORD.get()))DomesticStories.seed(l,b,h);
        }
        t.putInt(key+":cursor",cursor);if(cursor>=plan.size())t.putBoolean(key,true);d.setState(STATE,t);return true;
    }
    public static void tick(MinecraftServer s){
        if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;var origin=HouseSavedData.get(s).houseOrigin();if(origin==null||!origin.equals(LabyrinthData.get(s).builtOrigin()))return;var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return;
        for(var h:DomesticLives.HOMES){if(LabyrinthData.get(s).door(h.hall().entryDoorId())==null)continue;var b=LabyrinthPlaces.base(origin,h.hall());if(DomesticLives.selected(l,b,h)&&step(l,b,h))return;}
        var d=LabyrinthData.get(s);var state=d.state(STATE);
        for(var hall:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.CROSS_HALL)){
            if(d.door(hall.entryDoorId())==null)continue;var b=LabyrinthPlaces.base(origin,hall);String key="fragment:"+b.asLong();if(state.getBoolean(key))continue;
            boolean clear=true;for(var at:small(hall))clear&=safe(l,new AABB(b.offset(at)).expandTowards(0,2,0));if(!clear)continue;
            for(var rel:small(hall)){var at=b.offset(rel);if(l.isEmptyBlock(at)&&l.getBlockEntity(at)==null)l.setBlock(at,UncannyFurnitureBlock.of(UncannyFurnitureBlock.Kind.FRIDGE,hall==LabyrinthPlace.STRAIGHT_HALL?Direction.WEST:Direction.SOUTH),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
            state.putBoolean(key,true);d.setState(STATE,state);return;
        }
    }
}
