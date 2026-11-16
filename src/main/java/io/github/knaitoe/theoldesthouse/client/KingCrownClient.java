package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.KingCrownItem;
import io.github.knaitoe.theoldesthouse.labyrinth.KingRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 0.4.649, the keepsake crown as it is drawn (labyrinth.KingKeepsake, docs/SLEEPING_KING_0_4_649.md): filled in, the crown of the
 * eighth square; only drawn, its own outline in pencil with nothing filled (models/item/king_crown_drawn.json, tools/draw_king_art.py).
 * Which it is, is on the crown itself, so a worn crown, a held one and one lying in a pack all show it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class KingCrownClient {
    private KingCrownClient(){}
    public static final ResourceLocation DRAWN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"drawn");
    @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->
        ItemProperties.register(KingRegistry.CROWN.get(),DRAWN,(stack,level,entity,seed)->KingCrownItem.drawn(stack)?1F:0F));}
}
