package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.240: the title screen's splash as it is drawn (HouseSplash), at a fixed moment. Run the client with -PsplashSmoke; it saves
 * build/font-smoke/title-splash.png and closes.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class TitleSplashProof {
    private TitleSplashProof(){}
    private static int frames;
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.splashSmoke")||!(mc.screen instanceof TitleScreen)||mc.getOverlay()!=null)return;
        frames++;var dir=Path.of("../build/font-smoke");
        if(frames==120)HouseSplash.fixed=3.3;
        if(frames==130){Files.createDirectories(dir);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("title-splash.png"));}HouseSplash.fixed=null;mc.stop();}
    }
}
