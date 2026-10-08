package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
/** Use the same evergreen tint as native spruce leaves, including breaking particles. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ForestCoverColors {
    @SubscribeEvent public static void blocks(RegisterColorHandlersEvent.Block e){e.register((state,level,pos,index)->0x619961,HouseBlocks.FOREST_COVER.get());}
}
