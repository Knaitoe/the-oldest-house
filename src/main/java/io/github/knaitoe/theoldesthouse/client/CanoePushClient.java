package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * The actual canoe stroke drives both native skin hands and the rider's third-person reach. 0.4.560: each bench its own hands, out
 * along the culvert's open troughs as well as under its roof, and the back bench's keys push too (the canoe is steered from the front).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class CanoePushClient {
    private CanoePushClient(){}
    private static float reach(LakeHouseCanoe canoe,Entity rider,float partial){return canoe.pushSide(rider)==0?0:Mth.sin(Mth.clamp(canoe.pushAge(rider,partial)/LakeHouseCanoe.STROKE_TICKS,0,1)*Mth.PI);}
    public static void pose(PlayerModel<?> model,LivingEntity entity,float age){
        if(!(entity.getVehicle() instanceof LakeHouseCanoe canoe)||!canoe.pushable())return;
        float w=reach(canoe,entity,age-entity.tickCount);int side=canoe.pushSide(entity);
        model.leftArm.xRot=model.rightArm.xRot=-.35F;model.leftArm.zRot=-.18F;model.rightArm.zRot=.18F;
        var arm=side<0?model.leftArm:model.rightArm;arm.xRot-=w;arm.zRot+=side*w*.7F;model.body.zRot-=side*w*.07F;
        model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);
    }
    @SubscribeEvent public static void hand(RenderHandEvent e){
        var mc=Minecraft.getInstance();if(mc.player==null||!(mc.player.getVehicle() instanceof LakeHouseCanoe canoe)||!canoe.pushable())return;
        e.setCanceled(true);if(e.getHand()!=InteractionHand.MAIN_HAND)return;
        var renderer=mc.getEntityRenderDispatcher().getRenderer(mc.player);if(!(renderer instanceof PlayerRenderer player))return;
        float reach=reach(canoe,mc.player,e.getPartialTick());var missing=BodyLoss.missing(mc.player);
        for(int side:new int[]{-1,1}){
            if(missing==(side<0?HumanoidArm.LEFT:HumanoidArm.RIGHT))continue;
            float w=canoe.pushSide(mc.player)==side?reach:0;var pose=e.getPoseStack();pose.pushPose();
            // Vanilla skin-arm basis, with the palm reaching out to the wall and drawing back as the hull moves away.
            pose.translate(side*(.44+.20*w),-.55+.13*w,-.88-.08*w);
            pose.mulPose(Axis.YP.rotationDegrees(side*(45-20*w)));pose.translate(-side,3.6,3.5);
            pose.mulPose(Axis.ZP.rotationDegrees(side*(120+18*w)));pose.mulPose(Axis.XP.rotationDegrees(200-25*w));
            pose.mulPose(Axis.YP.rotationDegrees(-side*135));pose.translate(side*5.6,0,0);
            if(side>0)player.renderRightHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);else player.renderLeftHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);
            pose.popPose();
        }
    }
    /** 0.4.560: the back bench pushes with its own keys; the front bench's are read by the canoe itself, where it is steered. */
    @SubscribeEvent public static void backSeat(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.isPaused()||!(mc.player.getVehicle() instanceof LakeHouseCanoe canoe)||canoe.seat(mc.player)!=1)return;
        var input=mc.player.input;canoe.backSeat(mc.player,input.left,input.right,input.up,input.down);
    }
}
