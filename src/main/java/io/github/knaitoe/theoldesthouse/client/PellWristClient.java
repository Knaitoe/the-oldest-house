package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BodyLoss;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.PellPeople;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

/**
 * 0.4.567 (owner, of the knife a week in at the Pell house: "there should be an actual animation for grabbing his wrist"; until now
 * the reader was only told "You take hold of his wrist." and nothing of theirs moved). While Callum struggles for the knife
 * (PellKnife, PellPeople.STRUGGLE), whoever has hold of him (LiteraryActor.holder, set by the server) has both hands on his.
 * <ul>
 * <li>In their own view: both hands out in front of them and in toward the middle, the right closed round his wrist and the left on
 * his forearm just past it, pulled toward them and back with each of his jerks and turned with his twisting.</li>
 * <li>Seen by anybody else, or from behind: both arms out to him at the height of his hands, turned to wherever he stands from their
 * body, jerking with him.</li>
 * </ul>
 * His jerks are his own pose's (LiteraryRenderers, STRUGGLE), on his own clock, so the two keep together. His fist coming (LUNGE)
 * throws the hold off, and their hands go back to what they were. A reader without one arm holds on with the other.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PellWristClient {
    private PellWristClient(){}
    /** How far from him a holder can be and still be drawn holding on. */
    private static final double REACH=4;

    /** Callum, struggling, with this player's hold on him; or nobody. */
    static @Nullable LiteraryActor held(LivingEntity who){
        if(!(who instanceof Player p))return null;
        for(var a:p.level().getEntitiesOfClass(LiteraryActor.class,p.getBoundingBox().inflate(REACH),
                a->a.role()==LiteraryActor.STRANGER&&a.phase()==PellPeople.FIRST+PellPeople.CALLUM&&a.gesture()==PellPeople.STRUGGLE))
            if(a.holder().filter(p.getUUID()::equals).isPresent())return a;
        return null;
    }
    /** His pull and his twist, as his own pose takes them. */
    private static float jerk(LiteraryActor a,float partial){return Mth.sin((a.tickCount+partial)*.95F);}
    private static float twist(LiteraryActor a,float partial){return Mth.sin((a.tickCount+partial)*.61F);}

    /** Seen by anybody else, or from behind: both arms out to him at the height of his hands, pulled with him. */
    public static void pose(PlayerModel<?> model,LivingEntity entity,float age){
        var a=held(entity);if(a==null)return;
        float partial=age-entity.tickCount;
        // Where he is from their body: a positive yaw turns a raised arm to the body's right.
        var d=a.position().subtract(entity.position());
        float toward=(float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;
        float body=Mth.rotLerp(partial,entity.yBodyRotO,entity.yBodyRot);
        float turn=Mth.clamp(Mth.wrapDegrees(toward-body),-55,55)*Mth.DEG_TO_RAD;
        holdArms(model,turn,jerk(a,partial),twist(a,partial),BodyLoss.missing((Player)entity));
    }
    /** Both arms out to him, turned {@code turn} radians to the body's right, at his pull {@code j} and twist {@code t}; a missing arm
     *  is left as it hangs. */
    public static void holdArms(PlayerModel<?> model,float turn,float j,float t,@Nullable HumanoidArm missing){
        if(missing!=HumanoidArm.RIGHT){model.rightArm.xRot=-1.42F+j*.16F;model.rightArm.yRot=turn-.2F+t*.08F;model.rightArm.zRot=0;}
        if(missing!=HumanoidArm.LEFT){model.leftArm.xRot=-1.3F-j*.14F;model.leftArm.yRot=turn+.32F-t*.06F;model.leftArm.zRot=0;}
        model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);
    }

    /** In their own view: both hands out in front and in toward the middle where his wrist is, pulled and turned with him. */
    @SubscribeEvent public static void hand(RenderHandEvent e){
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        var a=held(mc.player);if(a==null)return;
        e.setCanceled(true);if(e.getHand()!=InteractionHand.MAIN_HAND)return;
        if(!(mc.getEntityRenderDispatcher().getRenderer(mc.player) instanceof PlayerRenderer player))return;
        float partial=e.getPartialTick(),j=jerk(a,partial),t=twist(a,partial);var missing=BodyLoss.missing(mc.player);
        for(int side:new int[]{-1,1}){
            if(missing==(side<0?HumanoidArm.LEFT:HumanoidArm.RIGHT))continue;
            var pose=e.getPoseStack();pose.pushPose();
            // The vanilla skin-arm basis, as the Shallows' carry has it, brought out and in to the middle: the right hand on his wrist,
            // the left a little further on, on his forearm; the pair pulled toward the reader and back, and turned, with him.
            float out=side>0?1.1F:1.22F,up=side>0?-.42F:-.46F;
            pose.translate(side*.22F+t*.04F,up+j*.03F,-out+j*.07F);
            pose.mulPose(Axis.YP.rotationDegrees(side*18));pose.mulPose(Axis.ZP.rotationDegrees(t*6));
            pose.translate(-side,3.6,3.5);
            pose.mulPose(Axis.ZP.rotationDegrees(side*120));pose.mulPose(Axis.XP.rotationDegrees(188));
            pose.mulPose(Axis.YP.rotationDegrees(-side*135));pose.translate(side*5.6,0,0);
            if(side>0)player.renderRightHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);
            else player.renderLeftHand(pose,e.getMultiBufferSource(),e.getPackedLight(),mc.player);
            pose.popPose();
        }
    }
}
