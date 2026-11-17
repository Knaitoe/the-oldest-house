package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

/** 0.4.662: the feed's monitor, which no block state names, is loaded with the other models. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class HallFeedModel {
    private HallFeedModel(){}
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional e){e.register(HallFeedClient.MODEL);}
}
