package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.GhostsSetBlock.Kind.*;

/** Vacant slices update authored empty cells and original furniture; no room or finite stock is rebuilt. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GhostsSetDressing {
    public static final String STATE="ghosts_dressing_0488";
    private static final Map<ServerLevel,BuildBlocks.Plan> PLANS=new IdentityHashMap<>();
    private GhostsSetDressing(){}
    public static void clear(){PLANS.clear();}
    public static void markBuilt(LabyrinthData data,BlockPos b){var t=data.state(STATE);t.putBoolean(Long.toString(b.asLong()),true);data.setState(STATE,t);}
    private static boolean complete(LabyrinthData data,BlockPos b){return data.state(STATE).getBoolean(Long.toString(b.asLong()));}
    private static void replace(ServerLevel l,BlockPos at,BlockState expected,BlockState next){if(l.getBlockEntity(at)==null&&l.getBlockState(at).equals(expected))l.setBlock(at,next,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS);}
    public static boolean repair(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());if(complete(data,b))return true;if(!GhostsSet.vacant(l,b))return false;
        var plan=PLANS.get(l);if(plan==null){plan=BuildBlocks.record(l,()->{
            for(int x:new int[]{-12,10})for(int z:new int[]{-41,-26}){var old=b.offset(x,10,z);var lowered=old.below();BuildBlocks.after(l,()->{var s=l.getBlockState(old);if(l.getBlockState(lowered).isAir()&&l.getBlockEntity(old)==null&&(s.equals(GhostsSetBlock.of(LAMP,Direction.NORTH))||s.equals(GhostsSetBlock.of(LAMP_OFF,Direction.NORTH)))){l.setBlock(lowered,s,3);l.setBlock(old,Blocks.AIR.defaultBlockState(),3);}});}
            for(var relative:BlockPos.betweenClosed(-14,10,-44,12,10,-15)){var at=b.offset(relative).immutable();BuildBlocks.after(l,()->replace(l,at,Blocks.AIR.defaultBlockState(),GhostsSetBlock.of(CEILING,Direction.NORTH)));}
            for(int x:new int[]{-13,9}){var at=b.offset(x,5,-41);var old=HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE).setValue(HouseholdFurnitureBlock.FACING,Direction.SOUTH);BuildBlocks.after(l,()->replace(l,at,old,GhostsSetBlock.of(COUNTER,Direction.SOUTH)));}
            BuildBlocks.after(l,()->replace(l,b.offset(-11,5,-41),Blocks.AIR.defaultBlockState(),GhostsSetBlock.of(COUNTER,Direction.SOUTH)));
            for(var relative:BlockPos.betweenClosed(-10,0,-21,-7,0,-19)){var at=b.offset(relative).immutable();BuildBlocks.after(l,()->replace(l,at,Blocks.AIR.defaultBlockState(),GhostsSetBlock.of(RUG,Direction.NORTH)));}
            for(var relative:List.of(new BlockPos(10,-4,-32),new BlockPos(18,0,-33),new BlockPos(17,0,-12))){var at=b.offset(relative);BuildBlocks.after(l,()->{var s=l.getBlockState(at);if(s.is(LiteraryRegistry.GHOST_SET.get())&&s.getValue(GhostsSetBlock.KIND)==CASE&&l.getBlockEntity(at)==null)replace(l,at,s,LiteraryRegistry.GHOST_CASE.get().defaultBlockState().setValue(GhostsCaseBlock.FACING,s.getValue(GhostsSetBlock.FACING)));});}
            BuildBlocks.after(l,()->markBuilt(LabyrinthData.get(l.getServer()),b));
        });PLANS.put(l,plan);}
        if(plan.tick()){PLANS.remove(l);return true;}return false;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var server=e.getServer();if(server instanceof net.minecraft.gametest.framework.GameTestServer||server.getTickCount()%20!=11||LabyrinthBuilder.isCarving())return;var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(server);var b=LabyrinthPlaces.base(origin,GhostsSet.PLACE);var l=server.getLevel(HouseDimensions.INTERIOR);if(l!=null&&b!=null&&GhostsSet.built(data,b)&&LabyrinthBuilder.isPlaceReady(data,GhostsSet.PLACE))repair(l,b);}
}
