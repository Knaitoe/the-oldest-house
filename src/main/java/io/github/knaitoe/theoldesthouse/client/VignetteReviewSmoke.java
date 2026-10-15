package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Direct native render pass gives the review its images without waiting for the complete corpus. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class VignetteReviewSmoke {
    private static boolean started;
    private VignetteReviewSmoke(){}
    public static boolean active(){return Boolean.getBoolean("the_oldest_house.vignetteReviewSmoke");}
    @SubscribeEvent public static void frame(RenderFrameEvent.Pre event){
        var mc=Minecraft.getInstance();if(!active()||started||!(mc.screen instanceof TitleScreen)||mc.getOverlay()!=null)return;
        started=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new CharacterPhaseVisualProof());
    }
}
