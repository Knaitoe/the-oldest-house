package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** One vacant, bounded finish of an existing set; native originals and subsequent edits are retained. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GhostsSetFinish {
    private static final String STATE="ghosts_finish0490";
    private static final Map<ServerLevel,BuildBlocks.Plan> PLANS=new WeakHashMap<>();
    private GhostsSetFinish(){}
    public static void markBuilt(ServerLevel l,BlockPos b){var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);t.putBoolean(Long.toString(b.asLong()),true);d.setState(STATE,t);}
    private static boolean safe(ServerLevel l,BlockPos b){
        var area=new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(-20,-1,-44)),net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(-12,9,-14)));
        for(int x=((int)area.minX)>>4;x<=((int)area.maxX)>>4;x++)for(int z=((int)area.minZ)>>4;z<=((int)area.maxZ)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(32).intersects(p.getCamera().getBoundingBox())))return false;
        return l.getEntitiesOfClass(LivingEntity.class,area.inflate(.2),LivingEntity::isAlive).isEmpty();
    }
    public static boolean repair(ServerLevel l,BlockPos b){
        if(LabyrinthData.get(l.getServer()).state(STATE).getBoolean(Long.toString(b.asLong())))return true;if(!safe(l,b))return false;
        var plan=PLANS.get(l);if(plan==null){plan=BuildBlocks.record(l,()->{
            for(int i=0;i<5;i++)for(int x=-18;x<=-16;x++){var at=b.offset(x,i,-16-i);if(l.getBlockState(at).is(Blocks.OAK_STAIRS))BuildBlocks.protectedSet(l,at,GhostsSetBlock.of(GhostsSetBlock.Kind.STAIR,Direction.NORTH),18,()->safe(l,b),Set.of());}
            for(int i=0;i<5;i++){var at=b.offset(-19,i+1,-16-i);if(l.getBlockState(at).isAir()||l.getBlockState(at).is(Blocks.OAK_FENCE))BuildBlocks.protectedSet(l,at,GhostsSetBlock.of(GhostsSetBlock.Kind.RAIL,Direction.NORTH),18,()->safe(l,b),Set.of());}
            for(int z=-22;z<=-16;z++){var at=b.offset(-19,5,z);if(l.getBlockState(at).is(Blocks.OAK_FENCE))BuildBlocks.protectedSet(l,at,z<=-21?GhostsSetBlock.of(GhostsSetBlock.Kind.RAIL,Direction.NORTH):Blocks.AIR.defaultBlockState(),18,()->safe(l,b),Set.of());}
            for(int y=0;y<=3;y++)for(int z=-44;z<=-15;z++){var at=b.offset(-14,y,z);if(z==-18&&y<2)continue;if(l.getBlockEntity(at)==null&&l.getBlockState(at).isAir())BuildBlocks.protectedSet(l,at,GhostsSetBlock.of(GhostsSetBlock.Kind.WALLPAPER,Direction.NORTH),18,()->safe(l,b),Set.of());}
        });PLANS.put(l,plan);}
        if(plan.tick()){PLANS.remove(l);markBuilt(l,b);return true;}return false;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%5!=2||LabyrinthBuilder.isCarving())return;var o=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(HouseDimensions.INTERIOR);if(o!=null&&l!=null&&LabyrinthBuilder.isPlaceReady(s,GhostsSet.PLACE))repair(l,LabyrinthPlaces.base(o,GhostsSet.PLACE));}
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent ignored){PLANS.clear();}
}
