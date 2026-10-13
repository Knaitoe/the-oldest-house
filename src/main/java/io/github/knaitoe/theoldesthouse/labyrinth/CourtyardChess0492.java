package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.Set;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Correct the two recognised old courtyard settings even when their earlier review was checkpointed. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CourtyardChess0492 {
    public static final String STATE="courtyard_chess0492";
    private CourtyardChess0492(){}
    public static boolean repair(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());var t=d.state(STATE);String key=Long.toString(b.asLong());if(t.getBoolean(key))return true;
        var area=new AABB(Vec3.atLowerCornerOf(b.offset(-9,0,-15)),Vec3.atLowerCornerOf(b.offset(10,3,-9)));
        for(int x=(int)area.minX>>4;x<=(int)area.maxX>>4;x++)for(int z=(int)area.minZ>>4;z<=(int)area.maxZ>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(p->area.inflate(32).intersects(p.getCamera().getBoundingBox())))return false;
        for(int x:new int[]{-8,8})for(var at:new BlockPos[]{b.offset(x,0,-12),b.offset(x,1,-12),b.offset(x,0,-10),b.offset(x,0,-14)})
            if(!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.02),LivingEntity::isAlive).isEmpty())return false;
        for(int x:new int[]{-8,8}){
            var at=b.offset(x,0,-12);var old=l.getBlockState(at);
            if(!old.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())||!Set.of(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,HouseholdFurnitureBlock.Kind.WALNUT_DESK,HouseholdFurnitureBlock.Kind.READING_DESK,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,HouseholdFurnitureBlock.Kind.CHESS_TABLE).contains(old.getValue(HouseholdFurnitureBlock.KIND)))continue;
            l.setBlock(at,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.SOUTH),18);
            var top=at.above();old=l.getBlockState(top);
            if(old.is(HouseBlocks.SCENE_DETAIL.get())&&Set.of(SceneDetailBlock.Kind.VASE,SceneDetailBlock.Kind.TABLE_LAMP).contains(old.getValue(SceneDetailBlock.KIND)))l.setBlock(top,VignetteDetailBlock.state(VignetteDetailBlock.Kind.CHESS,Direction.NORTH),18);
            for(int z:new int[]{-10,-14}){var seat=b.offset(x,0,z);old=l.getBlockState(seat);if(old.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&old.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.CANE_CHAIR)l.setBlock(seat,old.setValue(HouseholdFurnitureBlock.FACING,z==-10?Direction.NORTH:Direction.SOUTH),18);}
        }
        t.putBoolean(key,true);d.setState(STATE,t);return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=33||LabyrinthBuilder.isCarving()||!LabyrinthBuilder.isPlaceReady(s,LabyrinthPlace.ZAMPANO_COURTYARD))return;var o=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(NovelRooms.dimension(LabyrinthPlace.ZAMPANO_COURTYARD));if(o!=null&&l!=null)repair(l,LabyrinthPlaces.base(o,LabyrinthPlace.ZAMPANO_COURTYARD));}
}
