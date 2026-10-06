package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Vector3f;

/** Shipped corpse transform and actual coffin meshes, with native pose bounds and face orientation. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CoffinVisualProof extends Screen {
    private int frames;
    public CoffinVisualProof(){super(Component.literal("The Usher casket"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private static void pose(PoseStack p){p.mulPose(Axis.YP.rotationDegrees(180));LiteraryRenderers.coffinPose(p);p.scale(-1,-1,1);p.translate(0,-1.501,0);}
    private static void bounds(){
        var p=new PoseStack();pose(p);var matrix=p.last().pose();
        for(float x:new float[]{-.5F,.5F})for(float y:new float[]{-.5F,1.5F})for(float z:new float[]{-.25F,.25F}){
            var v=matrix.transformPosition(new Vector3f(x,y,z));
            if(Math.abs(v.x)>.4376F||v.z<-1.5001F||v.z>.5001F||v.y<3/16F)throw new IllegalStateException("Native Usher body extends outside its casket: "+v);
        }
        if(matrix.transformDirection(new Vector3f(0,0,-1)).y<=0)throw new IllegalStateException("The native coffin face must point up");
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        bounds();var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF201D1A);
        g.drawString(font,"Native Usher casket / existing body rests inside, face up",12,12,0xFFE8DDC5,false);g.flush();
        var p=g.pose();p.pushPose();p.translate(width/2,height*.67,160);p.scale(85,-85,85);p.mulPose(Axis.XP.rotationDegrees(22));p.mulPose(Axis.YP.rotationDegrees(-30));
        var buffers=mc.renderBuffers().bufferSource();
        for(int z:new int[]{0,-1}){
            p.pushPose();p.translate(0,0,z);
            var s=LiteraryRegistry.PROP.get().defaultBlockState().setValue(LiteraryPropBlock.KIND,LiteraryPropBlock.Kind.COFFIN).setValue(LiteraryPropBlock.STAGE,0).setValue(LiteraryPropBlock.FACING,Direction.NORTH);
            mc.getBlockRenderer().renderSingleBlock(s,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();
        }
        p.translate(.5,0,.5);pose(p);
        var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));model.pose(LiteraryActor.COFFIN_WOMAN,1,frames);
        model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.COFFIN_WOMAN,1))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();p.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof CoffinVisualProof p)||++p.frames<6)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-coffin.png"));}
        Files.writeString(dir.resolve("coffin-passed.txt"),"Native two-block coffin and original corpse mesh rendered; transformed head, arms, gown and feet bounds fit the casket footprint with face up.\n");
        TheOldestHouse.LOGGER.info("COFFIN CHECK PASSED: native original-body transform fits the casket");mc.setScreen(new BedVisualProof());
    }
}
