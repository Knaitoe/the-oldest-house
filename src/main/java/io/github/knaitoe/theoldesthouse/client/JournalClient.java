package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.JournalRegistry;
import io.github.knaitoe.theoldesthouse.journal.Journals;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** The journal's cover shows its wear: water rings, a dog-ear, swollen pages, a torn corner. Torn pages pinned in the world draw as paper. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class JournalClient {
    private JournalClient(){}
    @SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(JournalRegistry.PINNED_PAGE.get(),PinnedPageRenderer::new);}
    @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->ItemProperties.register(JournalRegistry.JOURNAL.get(),ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"wear"),(stack,level,entity,seed)->Math.min(Journals.wearOf(stack),Journals.WORN)/(float)Journals.WORN));}
}
