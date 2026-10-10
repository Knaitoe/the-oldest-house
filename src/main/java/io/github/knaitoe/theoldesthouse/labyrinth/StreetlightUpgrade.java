package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.LinkedHashMap;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Replace only the authored old lamp parts once; preserve omissions, actors and town progress. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StreetlightUpgrade {
    public static final String STATE="streetlights_0469";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private StreetlightUpgrade(){}
    public static boolean oldPart(BlockPos b,BlockPos at,BlockState old,BlockState next){
        var kind=next.getValue(TownStreetlightBlock.KIND);boolean wet=next.getValue(TownStreetlightBlock.WATERLOGGED);
        if(wet)return kind==TownStreetlightBlock.Kind.HEAD?old.is(Blocks.LIGHT)&&old.getValue(LightBlock.WATERLOGGED)&&old.getValue(LightBlock.LEVEL)==9:old.is(Blocks.IRON_BARS)&&old.getValue(IronBarsBlock.WATERLOGGED);
        boolean pier=at.getZ()==b.getZ()-110;
        if(kind==TownStreetlightBlock.Kind.HEAD)return old.is(Blocks.LANTERN)&&old.getValue(LanternBlock.HANGING)!=pier;
        var facing=next.getValue(TownStreetlightBlock.FACING);
        return old.equals(ProofrockTown.fence(pier?Blocks.SPRUCE_FENCE:Blocks.DARK_OAK_FENCE,kind==TownStreetlightBlock.Kind.TOP?facing:kind==TownStreetlightBlock.Kind.ARM?facing.getOpposite():null));
    }
    public static boolean apply(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var done=data.state(STATE);String key=Long.toString(b.asLong());if(done.getBoolean(key))return true;
        var edits=new LinkedHashMap<BlockPos,BlockState>();
        for(var e:ProofrockTown.streetlights(b).entrySet()){
            var at=e.getKey();if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;
            if(oldPart(b,at,l.getBlockState(at),e.getValue()))edits.put(at,e.getValue());
        }
        for(var e:edits.entrySet()){
            var at=e.getKey();var bounds=new AABB(at).inflate(.02);
            if(l.getBlockEntity(at)!=null||!l.getEntitiesOfClass(Entity.class,bounds).isEmpty()||l.players().stream().anyMatch(p->new AABB(at).inflate(48).intersects(p.getCamera().getBoundingBox()))||!SceneHuntReview.safe(l,at,e.getValue(),false))return false;
        }
        for(var e:edits.entrySet())l.setBlock(e.getKey(),e.getValue(),F);
        done.putBoolean(key,true);data.setState(STATE,done);return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=23||LabyrinthBuilder.isCarving()||!LabyrinthBuilder.isPlaceReady(s,LabyrinthPlace.DROWNED_TOWN))return;
        var origin=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(NovelRooms.dimension(LabyrinthPlace.DROWNED_TOWN));var b=origin==null?null:LabyrinthPlaces.base(origin,LabyrinthPlace.DROWNED_TOWN);
        if(l!=null&&b!=null&&!l.getBlockState(b.offset(0,-1,-3)).isAir())apply(l,b);
    }
}
