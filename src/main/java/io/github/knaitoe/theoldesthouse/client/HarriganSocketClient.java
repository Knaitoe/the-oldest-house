package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.gametest.LiveExpeditionProof;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.Files;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/** Checks the real tracked actor's renderer, skin and model, and saves the frame before acknowledging. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class HarriganSocketClient {
    private static int step,renders;private static boolean captured,clicked;private static UUID original;private static float[] frozenPose;
    private static String role,pending;private static LiteraryActor actor;
    private HarriganSocketClient(){}
    private static void require(boolean yes,String text){if(!yes)throw new IllegalStateException("LIVE HARRIGAN CLIENT: "+text);}
    public static boolean ready(Minecraft mc,int next,int age,String reader,BlockPos base){
        if(step!=next){step=next;renders=0;captured=false;clicked=false;pending=null;actor=null;frozenPose=null;role=reader;}
        mc.options.keyUp.setDown(false);mc.options.keyShift.setDown(false);mc.options.keyAttack.setDown(false);mc.options.keyUse.setDown(false);
        if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return false;
        mc.options.hideGui=true;
        for(var entity:mc.level.entitiesForRendering())if(entity instanceof LiteraryActor candidate&&candidate.role()==LiteraryActor.HARRIGAN&&candidate.distanceToSqr(mc.player)<100){actor=candidate;break;}
        if(actor==null||actor.phase()!=next-60)return false;
        if(original==null)original=actor.getUUID();require(original.equals(actor.getUUID()),"all visits retain the original tracked UUID");
        Vec3 point=actor.position().add(0,next==62?.03:1.25,0),delta=point.subtract(mc.player.getEyePosition());
        mc.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.hypot(delta.x,delta.z))));
        if(age>30&&!clicked){mc.gameMode.interact(mc.player,actor,InteractionHand.MAIN_HAND);clicked=true;}
        if(age>60&&renders>=8&&!captured&&pending==null)pending=next==60?"harrigan-alive":next==61?"harrigan-dead":"harrigan-casket";
        return captured;
    }
    @SubscribeEvent public static void drawn(RenderLivingEvent.Post<?,?> event){
        if(!LiveExpeditionProof.enabled()||step<60||step>62||actor==null||event.getEntity()!=actor||actor.phase()!=step-60)return;
        require(event.getRenderer() instanceof HouseHumanRenderers.ActorRenderer,"the actual scene actor uses Harrigan's renderer");
        var renderer=(HouseHumanRenderers.ActorRenderer)event.getRenderer();
        require(renderer.getTextureLocation(actor).equals(step==60?HouseHumanRenderers.HARRIGAN:HouseHumanRenderers.HARRIGAN_DEAD),"the actual renderer selects the living or closed-eye dead skin");
        var model=renderer.getModel();
        if(step==61)require(model.head.xRot>.3F&&model.head.zRot>0,"the dead chair pose has a slumped head");
        if(step==62)require(model.leftElbow.zRot>1&&model.rightElbow.zRot< -1&&model.leftKnee.xRot==0,"the actual casket body has folded hands and straight legs");
        if(step>=61){
            float[] pose={model.head.x,model.head.y,model.head.z,model.head.xRot,model.head.yRot,model.head.zRot,model.body.xRot,model.leftArm.xRot,model.rightArm.xRot,model.leftElbow.xRot,model.rightElbow.xRot};
            if(frozenPose==null)frozenPose=pose;
            else for(int i=0;i<pose.length;i++)require(Math.abs(pose[i]-frozenPose[i])<.00001F,"the corpse's rendered pose stays motionless across native frames and interaction");
        }
        renders++;
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post ignored)throws Exception{
        if(!LiveExpeditionProof.enabled()||pending==null)return;var mc=Minecraft.getInstance();if(mc.getOverlay()!=null)return;
        Files.createDirectories(LiveExpeditionProof.folder());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(LiveExpeditionProof.folder().resolve(role+"-"+pending+".png"));}
        Files.writeString(LiveExpeditionProof.folder().resolve(role+"-"+pending+".txt"),"Actual connected native renderer; original="+original+" phase="+actor.phase()+" position="+actor.position()+" texture="+(step==60?HouseHumanRenderers.HARRIGAN:HouseHumanRenderers.HARRIGAN_DEAD)+" renderedFrames="+renders+" interaction="+clicked+" camera="+mc.gameRenderer.getMainCamera().getPosition()+"\n");
        pending=null;captured=true;
    }
}
