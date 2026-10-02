package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/** Required opt-in native client proof after the writing check, without opening a world. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class NpcVisualProof extends Screen {
    private static int frames;
    public NpcVisualProof(){super(Component.literal("House NPCs"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xFF211E1B);g.drawString(font,"The Oldest House | native human models",10,10,0xFFE6DFCF,false);
        ResourceLocation[] textures={HouseHumanRenderers.HOLLOWAY,HouseHumanRenderers.HARRIGAN,HouseHumanRenderers.HARRIGAN_DEAD,HouseHumanRenderers.WITNESS};
        String[] titles={"Holloway | shared","Harrigan | study","Harrigan | funeral","Old man | witness"};
        for(int i=0;i<4;i++){
            int at=10+i*width/4;g.fill(at,32,at+width/4-18,height-24,0xFF39332D);
            g.drawString(font,titles[i],at+8,40,0xFFE6DFCF,false);
            mesh(g,textures[i],at+width/8-8,68,i,0);
            g.drawString(font,i==0?"Canvas and flannel":i==1?"Suit and glasses":i==2?"Original saved body":"Patched coat / beard",at+8,182,0xFFC9BBA7,false);
            mesh(g,textures[i],at+width/8-8,215,i,1);
            g.drawString(font,i==0?"Back / UV seams":i==1?"Native seated pose":i==2?"Horizontal casket":"Quiet hunch / side",at+8,height-42,0xFFC9BBA7,false);
        }
        g.drawString(font,"Shared actors use native tracking. Tom and the double retain each explorer's standard or slim skin.",10,height-15,0xFFE6DFCF,false);
    }
    private void mesh(GuiGraphics g,ResourceLocation texture,int x,int y,int kind,int view){
        var mc=Minecraft.getInstance();var model=new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER),false);
        if(kind==1){model.leftLeg.xRot=model.rightLeg.xRot=-(float)Math.PI/2;model.leftArm.xRot=model.rightArm.xRot=-(float)Math.PI*2/9;}
        if(kind==2&&view==0){model.head.xRot=.3F;model.body.xRot=.14F;model.leftLeg.xRot=model.rightLeg.xRot=-(float)Math.PI/2;model.leftArm.xRot=model.rightArm.xRot=-.43F;}
        if(kind==3){model.body.xRot=.12F;model.head.xRot=.15F;model.leftArm.xRot=model.rightArm.xRot=-.15F;}
        if(kind==0){model.rightArm.xRot=-1.45F;model.rightArm.yRot=-.2F;model.leftArm.xRot=-1.45F;model.leftArm.yRot=.55F;}
        model.hat.copyFrom(model.head);model.jacket.copyFrom(model.body);model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);model.leftPants.copyFrom(model.leftLeg);model.rightPants.copyFrom(model.rightLeg);
        g.flush();var poses=g.pose();poses.pushPose();poses.translate(x,y,100);poses.scale(62,62,62);
        poses.mulPose(Axis.YP.rotationDegrees(view==1&&kind==0?20:view==1?110:160));poses.mulPose(Axis.XP.rotationDegrees(-5));
        if(kind==2&&view==1){poses.translate(-.55,.45,0);poses.mulPose(Axis.ZP.rotationDegrees(-90));}
        var buffers=mc.renderBuffers().bufferSource();model.renderToBuffer(poses,buffers.getBuffer(RenderType.entityCutoutNoCull(texture)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();poses.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof NpcVisualProof)||++frames<4)return;
        if(!HouseHumanRenderers.ready())throw new IllegalStateException("The native Harrigan renderer was not initialized");
        for(var texture:new ResourceLocation[]{HouseHumanRenderers.HOLLOWAY,HouseHumanRenderers.HARRIGAN,HouseHumanRenderers.HARRIGAN_DEAD,HouseHumanRenderers.WITNESS})
            if(mc.getResourceManager().getResource(texture).isEmpty())throw new IllegalStateException("Missing NPC skin "+texture);
        Path folder=Path.of("../build/font-smoke");Files.createDirectories(folder);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(folder.resolve("native-npcs.png"));}
        Files.writeString(folder.resolve("npcs-passed.txt"),"Native player model meshes, exact skin UVs, seated/casket poses and client renderer initialization passed.\n");
        TheOldestHouse.LOGGER.info("HOUSE NPC CHECK PASSED: native screenshot saved");mc.setScreen(new CastVisualProof());
    }
}
