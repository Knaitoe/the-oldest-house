package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.BarsHang;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * 0.4.467, Bike night: a reader hanging from the park's monkey bars, as the others see them. They hang at their full length
 * (BarsHang does not sit them down), both arms straight up to the rungs; going along, one hand is ahead of the other by turns, a
 * rung to a hand, and their legs trail behind the way they are going. A reader does not see themself.
 */
public final class BarsClient {
    private BarsClient(){}
    /** Straight up; how far a hand is ahead of or behind the shoulder as they go; how far the legs trail, and how far they swing apart. */
    private static final float UP=-Mth.PI,AHEAD=.32F,TRAIL=.3F,LEGS=.1F;
    /** Both hands up on the rungs, one ahead of the other by turns (PlayerPatMixin). */
    public static void pose(PlayerModel<?> model,LivingEntity entity,float age){
        if(!(entity.getVehicle() instanceof BarsHang hold))return;
        // A rung to a hand: the hands change over once in the reach of two rungs.
        float turn=Mth.sin((float)(hold.along()/BarsHang.HAND*Math.PI));
        // How much of the hold's last step was the way the body faces: one going that way, minus one going backwards, nothing hanging still.
        float yaw=entity.yBodyRot*Mth.DEG_TO_RAD;
        float going=(float)Mth.clamp(((hold.getX()-hold.xo)*-Mth.sin(yaw)+(hold.getZ()-hold.zo)*Mth.cos(yaw))/BarsHang.PACE,-1,1);
        model.rightArm.xRot=UP+AHEAD*turn;model.leftArm.xRot=UP-AHEAD*turn;model.rightArm.yRot=model.leftArm.yRot=0;model.rightArm.zRot=model.leftArm.zRot=0;
        model.rightLeg.xRot=TRAIL*going+LEGS*turn;model.leftLeg.xRot=TRAIL*going-LEGS*turn;model.rightLeg.yRot=model.leftLeg.yRot=0;model.rightLeg.zRot=model.leftLeg.zRot=0;
        model.leftSleeve.copyFrom(model.leftArm);model.rightSleeve.copyFrom(model.rightArm);model.leftPants.copyFrom(model.leftLeg);model.rightPants.copyFrom(model.rightLeg);
    }
}
