package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Vector3f;

/** The shipped child pose and native two-part bed, with measured bounds above the mattress. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BedVisualProof extends Screen {
    private int frames;
    public BedVisualProof(){super(Component.literal("The brother's bed"));}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}
    private static void pose(PoseStack p){p.scale(.58F,.58F,.58F);p.mulPose(Axis.YP.rotationDegrees(180));LiteraryRenderers.bedPose(p,.58F);p.scale(-1,-1,1);p.translate(0,-1.501,0);}
    private static void bounds(){
        var p=new PoseStack();pose(p);var m=p.last().pose();
        for(float x:new float[]{-.5F,.5F})for(float y:new float[]{-.5F,1.5F})for(float z:new float[]{-.25F,.25F}){
            var v=m.transformPosition(new Vector3f(x,y,z));
            if(Math.abs(v.x)>.5001F||v.z<-1.5001F||v.z>.5001F||v.y<.5624F)throw new IllegalStateException("Native brother pose leaves the bed or sinks into its mattress: "+v);
        }
        if(m.transformDirection(new Vector3f(0,0,-1)).y<=0)throw new IllegalStateException("The native sleeping face must point up");
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        bounds();var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF201D1A);
        g.drawString(font,"Native brother pose / above the mattress, face up",12,12,0xFFE8DDC5,false);g.flush();
        var p=g.pose();p.pushPose();p.translate(width/2,height*.67,160);p.scale(85,-85,85);p.mulPose(Axis.XP.rotationDegrees(22));p.mulPose(Axis.YP.rotationDegrees(-30));
        var buffers=mc.renderBuffers().bufferSource();
        p.pushPose();p.translate(1,0,0);p.mulPose(Axis.YP.rotationDegrees(180));
        var bed=(BedBlockEntity)((BedBlock)Blocks.BLUE_BED).newBlockEntity(BlockPos.ZERO,Blocks.BLUE_BED.defaultBlockState());
        var renderer=mc.getBlockEntityRenderDispatcher().getRenderer(bed);
        if(renderer==null)throw new IllegalStateException("Native bed renderer is unregistered");
        renderer.render(bed,0,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();
        p.translate(.5,0,.5);pose(p);var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));model.pose(LiteraryActor.BROTHER,1,frames);
        model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.BROTHER,1))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();p.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof BedVisualProof p)||++p.frames<6)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-bed.png"));}
        Files.writeString(dir.resolve("bed-passed.txt"),"Native child pose fits the two-block bed footprint, stays above the 9/16 mattress, and faces up.\n");
        TheOldestHouse.LOGGER.info("BED CHECK PASSED: native child pose stays above the mattress");mc.setScreen(new LiteraryEffectsVisualProof());
    }
}
