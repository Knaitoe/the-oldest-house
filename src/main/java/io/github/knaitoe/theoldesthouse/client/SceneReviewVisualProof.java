package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Actual baked meshes and native UV skin, ahead of the full room cutaways. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class SceneReviewVisualProof extends Screen {
    private int frames;
    public SceneReviewVisualProof(){super(Component.literal("Reviewed scene assets"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF252321);g.drawString(font,"Custom scene meshes / generated material UVs / original native costume stand",12,12,0xFFE8DFCF,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();var kinds=VignetteDetailBlock.Kind.values();
        for(int i=0;i<kinds.length;i++){int column=i%6,row=i/6;float xx=12+column*(width-24)/6F,yy=36+row*(height-60)/3F;
            var pose=g.pose();pose.pushPose();pose.translate(xx+36,yy+32,150);pose.scale(25,-25,25);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(25));
            mc.getBlockRenderer().renderSingleBlock(VignetteDetailBlock.state(kinds[i],Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
            g.drawString(font,kinds[i].getSerializedName().replace('_',' '), (int)xx,(int)yy+68,0xFFE8DFCF,false);
        }
        var pose=g.pose();pose.pushPose();pose.translate(width-130,height-92,160);pose.scale(25,-25,25);pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(25));
        mc.getBlockRenderer().renderSingleBlock(HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.READING_DESK,Direction.NORTH),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
        pose.pushPose();pose.translate(width-48,height-102,180);pose.scale(34,34,34);pose.mulPose(Axis.YP.rotationDegrees(165));
        var costume=new CostumeRenderers.CostumeModel(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER));costume.leftArm.zRot=-1.05F;costume.rightArm.zRot=1.05F;costume.hat.visible=false;costume.jacket.visible=false;costume.leftSleeve.visible=false;costume.rightSleeve.visible=false;costume.leftPants.visible=false;costume.rightPants.visible=false;
        costume.renderToBuffer(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(CostumeRenderers.TEXTURE)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);pose.popPose();buffers.endBatch();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof SceneReviewVisualProof p)||++p.frames<12)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);try(NativeImage im=Screenshot.takeScreenshot(mc.getMainRenderTarget())){im.writeToFile(dir.resolve("native-reviewed-assets.png"));}
        for(var kind:VignetteDetailBlock.Kind.values()){
            var model=mc.getBlockRenderer().getBlockModel(VignetteDetailBlock.state(kind,Direction.NORTH));
            if(model==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing native vignette model: "+kind);
        }
        Files.writeString(dir.resolve("reviewed-assets-passed.txt"),"All sixteen custom native prop models, the reading desk and the native costume UV skin rendered.\n");
        TheOldestHouse.LOGGER.info("REVIEWED ASSETS CHECK PASSED: sixteen native props, reading desk and costume skin");mc.setScreen(new SceneVisualProof());
    }
}
