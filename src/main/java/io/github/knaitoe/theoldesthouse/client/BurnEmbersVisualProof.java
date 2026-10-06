package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.network.BurnEmbersPayload;
import java.nio.file.*;
import java.util.List;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Native font proof; the separate connected-client expedition captures the actual smoke in-world. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BurnEmbersVisualProof extends Screen {
    private int frames;
    public BurnEmbersVisualProof(){super(Component.literal("A burning leaf"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial) {
        g.fill(0,0,width,height,0xFF211C18);g.drawString(font,"The words remain for a moment in the smoke.",14,12,0xFFD6BB9B,false);
        int i=0;
        for(var hand:List.of(HouseWriting.WritingStyle.WILL,HouseWriting.WritingStyle.KAREN,HouseWriting.WritingStyle.ZAMPANO,HouseWriting.WritingStyle.PELAFINA)) {
            var text=BurnEmbersClient.words(new BurnEmbersPayload(BlockPos.ZERO,"I walked 1,234 metres. The House kept the measure when I had forgotten it.",hand.font(),1));
            var lines=font.split(text,150);if(lines.isEmpty()||lines.size()>7)throw new IllegalStateException("Ember handwriting does not fit: "+hand);
            int left=24+(i%2)*(width/2),top=50+(i/2)*150;
            g.drawString(font,hand.name(),left,top,0xFF877869,false);int row=top+24;
            for(var line:lines){g.drawString(font,line,left,row,0xDDF0BD85,false);row+=9;}i++;
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception {
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof BurnEmbersVisualProof s)||++s.frames<4)return;
        var folder=Path.of("../build/font-smoke");try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve("native-embers.png"));}
        Files.writeString(folder.resolve("embers-passed.txt"),"Four original narrator hands wrap and render as native smoke excerpts.\n");
        TheOldestHouse.LOGGER.info("EMBER FONT CHECK PASSED: four native narrator hands");mc.setScreen(new NpcVisualProof());
    }
}
