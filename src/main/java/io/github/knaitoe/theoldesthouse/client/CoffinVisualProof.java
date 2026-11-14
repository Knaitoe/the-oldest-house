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

/**
 * Shipped corpse transform and actual coffin meshes, with native pose bounds and face orientation. 0.4.587: the coffin is the
 * house's own (one box two blocks long, drawn open and shut), she must lie inside its boards and under its rim, and whatever
 * the idle sway does to a figure's arms, she is the same at every moment.
 */
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
            // Inside the boards, not merely inside the block: fourteen wide, between the two end boards, from the coffin's floor to under its rim.
            if(Math.abs(v.x)>.4376F||v.z<-1.3751F||v.z>.3751F||v.y<1/16F||v.y>9/16F)throw new IllegalStateException("Native Usher body extends outside its casket: "+v);
        }
        if(matrix.transformDirection(new Vector3f(0,0,-1)).y<=0)throw new IllegalStateException("The native coffin face must point up");
    }
    /** Whether the part at this path below a limb is drawn at all: the cast's model carries other people's coats, hoods and tools, hidden. */
    private static boolean drawn(net.minecraft.client.model.geom.ModelPart top,String path){
        var at=top;if(!at.visible)return false;
        for(String name:path.split("/")){if(name.isEmpty())continue;at=at.getChild(name);if(!at.visible)return false;}
        return !at.skipDraw;
    }
    /**
     * 0.4.587: her own boxes, as posed, against the coffin's boards. The check above tests the transform with a figure's outline;
     * this one walks every box she is drawn with (her head, hair, gown, arms and legs), so a limb the pose moves is caught too.
     * A box's own size is measured, without the fraction of a pixel a second layer stands off the first.
     */
    private static void mesh(LiteraryRenderers.CastModel model){
        var p=new PoseStack();pose(p);float[] far={Float.MAX_VALUE,-Float.MAX_VALUE};
        for(var limb:new net.minecraft.client.model.geom.ModelPart[]{model.head,model.hat,model.body,model.rightArm,model.leftArm,model.rightLeg,model.leftLeg})
            limb.visit(p,(pose,path,index,cube)->{
                if(!drawn(limb,path))return;
                for(float x:new float[]{cube.minX,cube.maxX})for(float y:new float[]{cube.minY,cube.maxY})for(float z:new float[]{cube.minZ,cube.maxZ}){
                    var v=pose.pose().transformPosition(new Vector3f(x/16,y/16,z/16));far[0]=Math.min(far[0],v.z);far[1]=Math.max(far[1],v.z);
                    if(Math.abs(v.x)>.4376F||v.z<-1.3751F||v.z>.3751F||v.y<1/16F||v.y>9/16F)throw new IllegalStateException("The woman in the coffin is outside its boards at "+(path.isEmpty()?"a limb":path)+": "+v);
                }
            });
        // And she is her whole length in it: a figure drawn half-size would pass every bound above.
        if(far[1]-far[0]<1.6F)throw new IllegalStateException("The woman in the coffin is drawn "+(far[1]-far[0])+" long, and she is 1.7");
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){
        bounds();var mc=Minecraft.getInstance();g.fill(0,0,width,height,0xFF201D1A);
        g.drawString(font,"Native Usher casket / existing body rests inside, face up",12,12,0xFFE8DDC5,false);g.flush();
        var buffers=mc.renderBuffers().bufferSource();
        // The coffin shut, to the right of the open one.
        var p=g.pose();p.pushPose();p.translate(width*.72,height*.7,160);p.scale(120,-120,120);p.mulPose(Axis.XP.rotationDegrees(28));p.mulPose(Axis.YP.rotationDegrees(-38));
        for(int z:new int[]{0,-1}){p.pushPose();p.translate(0,0,z);mc.getBlockRenderer().renderSingleBlock(UsherBlock.of(z==0?UsherBlock.Kind.COFFIN:UsherBlock.Kind.COFFIN_HEAD,Direction.NORTH,1),p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();}
        p.popPose();
        p.pushPose();p.translate(width*.34,height*.7,160);p.scale(150,-150,150);p.mulPose(Axis.XP.rotationDegrees(28));p.mulPose(Axis.YP.rotationDegrees(-38));
        for(int z:new int[]{0,-1}){
            p.pushPose();p.translate(0,0,z);
            var s=UsherBlock.of(z==0?UsherBlock.Kind.COFFIN:UsherBlock.Kind.COFFIN_HEAD,Direction.NORTH,0);
            mc.getBlockRenderer().renderSingleBlock(s,p,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);buffers.endBatch();p.popPose();
        }
        p.translate(.5,0,.5);pose(p);
        var model=new LiteraryRenderers.CastModel(mc.getEntityModels().bakeLayer(LiteraryRenderers.CAST));
        // A model is born young, and drawn young she is half her length: this frame then showed a child lying in the middle of the box.
        model.young=false;
        float[] first=null;
        for(float age:new float[]{0,13,57,frames}){
            net.minecraft.client.model.AnimationUtils.bobArms(model.rightArm,model.leftArm,age);model.pose(LiteraryActor.COFFIN_WOMAN,1,age);
            float[] now={model.rightArm.xRot,model.rightArm.yRot,model.rightArm.zRot,model.leftArm.xRot,model.leftArm.yRot,model.leftArm.zRot,model.head.xRot,model.head.yRot,model.head.zRot,model.body.xRot,model.body.zScale,model.rightLeg.xRot,model.leftLeg.xRot};
            if(first==null)first=now;else if(!java.util.Arrays.equals(first,now))throw new IllegalStateException("The woman in the coffin moves: "+java.util.Arrays.toString(first)+" then "+java.util.Arrays.toString(now));
        }
        if(model.rightArm.zRot>0||model.leftArm.zRot<0)throw new IllegalStateException("Her arms lie out from her sides, toward the coffin's boards");
        mesh(model);
        model.renderToBuffer(p,buffers.getBuffer(RenderType.entityCutoutNoCull(LiteraryRenderers.skin(LiteraryActor.COFFIN_WOMAN,1))),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);buffers.endBatch();p.popPose();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!Boolean.getBoolean("the_oldest_house.fontSmoke")||!(mc.screen instanceof CoffinVisualProof p)||++p.frames<6)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-coffin.png"));}
        Files.writeString(dir.resolve("coffin-passed.txt"),"The house's own coffin, open and shut, and the original corpse mesh rendered; her head, arms, gown and feet lie inside its boards and under its rim, face up, and she is the same at every moment.\n");
        TheOldestHouse.LOGGER.info("COFFIN CHECK PASSED: native original-body transform fits the casket");mc.setScreen(new BedVisualProof());
    }
}
