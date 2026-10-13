package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Native baked meshes, animated water sprite, plain item silhouettes and the unchanged handwriting tiles. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class FinishingVisualProof extends Screen {
    private int frames,page;
    public FinishingVisualProof(){super(Component.literal("October scene finishing"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private void block(GuiGraphics g,VignetteDetailBlock.Kind kind,int x,int y,float scale,boolean flat){
        var mc=Minecraft.getInstance();var pose=g.pose();pose.pushPose();pose.translate(x,y,150);pose.scale(scale,-scale,scale);
        if(!flat){pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(35));}
        mc.getBlockRenderer().renderSingleBlock(VignetteDetailBlock.state(kind,Direction.NORTH),pose,mc.renderBuffers().bufferSource(),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xff24272a);g.drawString(font,page==0?"Field case / carved stone bowl / animated water / chalk and linen reel":"Original blind handwriting / dense supported wall",12,12,0xffe8dfcf,false);g.flush();
        if(page==0){
            block(g,VignetteDetailBlock.Kind.PLAIN_NOTES,width/5,height/3,88,false);
            block(g,VignetteDetailBlock.Kind.FOUNTAIN_BOWL,width*3/5,height/3,88,false);
            var pose=g.pose();pose.pushPose();pose.translate(width/4f,height*2/3f,180);pose.scale(4,4,4);g.renderItem(new ItemStack(LabyrinthRegistry.CHALK.get()),0,0);pose.popPose();
            pose.pushPose();pose.translate(width*3/5f,height*2/3f,180);pose.scale(4,4,4);g.renderItem(new ItemStack(LabyrinthRegistry.TRAIL_SPOOL.get()),0,0);pose.popPose();
        }else{
            for(int row=0;row<3;row++)for(int col=0;col<7;col++)block(g,VignetteDetailBlock.Kind.valueOf("SCRIBBLE_"+(8+Math.floorMod(col*3+row*5,8))),24+col*(width-48)/7,55+row*(height-70)/3,(width-48)/7f,true);
        }
        mc.renderBuffers().bufferSource().endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof FinishingVisualProof s)||++s.frames<10)return;
        var dir=Path.of("../build/font-smoke");try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            var colors=new java.util.HashSet<Integer>();for(int y=50;y<image.getHeight()-10;y++)for(int x=10;x<image.getWidth()-10;x++)colors.add(image.getPixelRGBA(x,y));
            if(colors.size()<16)throw new IllegalStateException("Native finishing view lacks its meshes/materials: "+s.page);
            image.writeToFile(dir.resolve(s.page==0?"native-finishing-props.png":"native-finishing-wall.png"));
        }
        if(++s.page<2){s.frames=0;return;}
        Files.writeString(dir.resolve("finishing-passed.txt"),"Native field-case and fountain meshes, animated tinted water, chalk/string item models and dense original handwriting wall rendered.\n");mc.setScreen(new LakeHouseVisualProof());
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Colors {
        @SubscribeEvent public static void blocks(RegisterColorHandlersEvent.Block e){e.register((s,l,p,i)->i==0&&s.getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.FOUNTAIN_BOWL?0x3f76e4:-1,HouseBlocks.VIGNETTE_DETAIL.get());}
    }
}
