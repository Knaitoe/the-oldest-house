package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HallCupboardBlock;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** The photographed barrel row and its fitted fronts, drawn by the production block renderer. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HallCupboardVisualProof extends Screen {
    private int frames;
    public HallCupboardVisualProof(){super(Component.literal("Hall cupboard fittings"));}
    @Override public void renderBackground(GuiGraphics graphics,int x,int y,float partial){}
    @Override public void render(GuiGraphics graphics,int x,int y,float partial){
        var mc=Minecraft.getInstance();graphics.fill(0,0,width,height,0xFF252321);
        graphics.drawString(font,"Hall storage / native barrel inventories behind fitted spruce fronts",12,12,0xFFE8DFCF,false);
        graphics.flush();var buffers=mc.renderBuffers().bufferSource();var pose=graphics.pose();
        for(int fitted=0;fitted<2;fitted++){
            pose.pushPose();pose.translate(width*(fitted==0?.26:.74),height*.54,180);
            pose.scale(70,-70,70);pose.mulPose(Axis.XP.rotationDegrees(25));pose.mulPose(Axis.YP.rotationDegrees(-45));
            pose.translate(-1,-.5,-1.5);
            for(int z=0;z<3;z++){
                for(int xx=0;xx<3;xx++){
                    pose.pushPose();pose.translate(xx,-1,z);mc.getBlockRenderer().renderSingleBlock(Blocks.SPRUCE_PLANKS.defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
                }
                pose.pushPose();pose.translate(0,1,z);mc.getBlockRenderer().renderSingleBlock(Blocks.WHITE_TERRACOTTA.defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
                pose.pushPose();pose.translate(0,0,z);mc.getBlockRenderer().renderSingleBlock(Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,Direction.EAST),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
                if(fitted==1){pose.pushPose();pose.translate(1,0,z);mc.getBlockRenderer().renderSingleBlock(HouseBlocks.HALL_CUPBOARD.get().defaultBlockState().setValue(HallCupboardBlock.FACING,Direction.EAST),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
            }
            pose.popPose();
        }
        buffers.endBatch();
        graphics.drawString(font,"Before: exposed barrel lids",(int)(width*.26)-72,height-45,0xFFE8DFCF,false);
        graphics.drawString(font,"After: shallow cupboard fronts",(int)(width*.74)-82,height-45,0xFFE8DFCF,false);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof HallCupboardVisualProof proof)||++proof.frames<8)return;
        for(var facing:Direction.Plane.HORIZONTAL){
            var state=HouseBlocks.HALL_CUPBOARD.get().defaultBlockState().setValue(HallCupboardBlock.FACING,facing);
            if(mc.getBlockRenderer().getBlockModel(state)==mc.getModelManager().getMissingModel())
                throw new IllegalStateException("Missing native cupboard model: "+facing);
        }
        var folder=Path.of("../build/font-smoke");Files.createDirectories(folder);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve("native-hall-cupboards.png"));}
        Files.writeString(folder.resolve("hall-cupboards-passed.txt"),"Native original barrel row and fitted fronts rendered; all four oriented cupboard models resolved.\n");
        TheOldestHouse.LOGGER.info("HALL CUPBOARDS CHECK PASSED: native before/after row and four oriented models");
        mc.setScreen(new SceneVisualProof());
    }
}
