package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BarsHang;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * 0.4.467, Bike night: a reader hanging from the park's monkey bars, as the others see them. They hang at their full length
 * (BarsHang does not sit them down), both arms straight up to the rungs, one hand ahead of the other by turns, a rung to a hand.
 * 0.4.470: their body swings under their hands as BarsHang says it does, about the rung they hold. A reader does not see
 * themself, and their own view is not swung.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BarsClient {
    private BarsClient(){}
    private static final Set<Player> SWUNG=Collections.newSetFromMap(new IdentityHashMap<>());
    /** Straight up; how far a hand is ahead of or behind the shoulder; how far the legs trail the swing. */
    private static final float UP=-Mth.PI,AHEAD=.32F,TRAIL=.5F;
    /** How far the body is swung now, in radians: more than nothing, toward the greater end of the bars' axis. */
    private static float swung(BarsHang hold,float partial){return (float)(BarsHang.swung(hold.swingTicks(partial))*hold.swingWay());}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void swing(RenderPlayerEvent.Pre e){
        if(!(e.getEntity().getVehicle() instanceof BarsHang hold))return;
        // About the hands: the rung is their reach over their feet, and the body goes under it along the bars.
        float hands=(float)(e.getEntity().getBbHeight()*BarsHang.REACH);var pose=e.getPoseStack();pose.pushPose();
        pose.translate(0,hands,0);pose.mulPose(new org.joml.Quaternionf().rotateAxis(swung(hold,e.getPartialTick()),hold.alongX()?0:-1,0,hold.alongX()?1:0));pose.translate(0,-hands,0);
        SWUNG.add(e.getEntity());
    }
    @SubscribeEvent public static void unswing(RenderPlayerEvent.Post e){if(SWUNG.remove(e.getEntity()))e.getPoseStack().popPose();}
    /** Both hands up on the rungs, one ahead of the other by turns; the legs trailing the swing (PlayerPatMixin). */
    public static void pose(PlayerModel<?> model,LivingEntity entity,float age){
        if(!(entity.getVehicle() instanceof BarsHang hold))return;
        // A rung to a hand: the hands change over at each rung.
        float turn=Mth.sin((float)(hold.along()/BarsHang.HAND*Math.PI));
        // The legs lag the body's swing: bent back the way it came from, as seen from the way the body faces.
        float yaw=entity.yBodyRot*Mth.DEG_TO_RAD,way=hold.alongX()?-Mth.sin(yaw):Mth.cos(yaw);
        float lag=TRAIL*swung(hold,1)*way;
        model.rightArm.xRot=UP+AHEAD*turn;model.leftArm.xRot=UP-AHEAD*turn;model.rightArm.yRot=model.leftArm.yRot=0;model.rightArm.zRot=model.leftArm.zRot=0;
        model.rightLeg.xRot=model.leftLeg.xRot=lag;model.rightLeg.yRot=model.leftLeg.yRot=0;model.rightLeg.zRot=model.leftLeg.zRot=0;
        model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);model.leftPants.copyFrom(model.leftLeg);model.rightPants.copyFrom(model.rightLeg);
    }
}
