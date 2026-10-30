package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.Artifacts;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** The twenty artifacts of build 7 (labyrinth/Artifacts), as their registered items and baked models draw them. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class ArtifactVisualProof extends Screen {
    private int frames;
    public ArtifactVisualProof(){super(Component.literal("Artifacts"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float delta){}
    @Override public void render(GuiGraphics g,int x,int y,float delta){
        var mc=Minecraft.getInstance();var kinds=Artifacts.Kind.values();g.fill(0,0,width,height,0xFF27241F);
        g.drawString(font,"Native artifacts / "+kinds.length+" items",12,10,0xFFE4DACA,false);
        int cellW=width/5,cellH=(height-30)/4;
        for(int i=0;i<kinds.length;i++){
            var stack=new ItemStack(kinds[i].item());
            if(mc.getItemRenderer().getModel(stack,null,null,0)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing artifact model: "+kinds[i].id);
            int left=(i%5)*cellW+5,top=28+(i/5)*cellH;
            g.fill(left,top,left+cellW-10,top+cellH-5,0xFF3B352D);var pose=g.pose();pose.pushPose();pose.translate(left+cellW/2F-21,top+4,0);pose.scale(2.5F,2.5F,2.5F);g.renderItem(stack,0,0);pose.popPose();
            g.drawString(font,font.plainSubstrByWidth(stack.getHoverName().getString(),cellW-18),left+4,top+cellH-18,0xFFE4DACA,false);
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof ArtifactVisualProof p)||++p.frames<4)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-artifacts.png"));}
        Files.writeString(dir.resolve("artifacts-passed.txt"),"All twenty artifacts rendered with native baked models.\n");mc.setScreen(new CompanionVisualProof());
    }
}
