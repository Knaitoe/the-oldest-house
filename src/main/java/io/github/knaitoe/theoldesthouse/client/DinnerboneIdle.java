package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

/** Long held poses, a slight lean and one delayed hand; feet stay on the ceiling. */
public final class DinnerboneIdle {
    private DinnerboneIdle(){}
    private static float ramp(float t,float from,float to){float v=Mth.clamp((t-from)/(to-from),0,1);return v*v*(3-2*v);}
    public static void pose(HumanoidModel<?> model,float clock,int seed){
        HumanoidMotion.reset(model);model.young=false;model.crouching=false;model.riding=false;model.attackTime=0;
        int period=1530+Math.floorMod(seed,211);float t=(clock+Math.floorMod(seed,997))%period;
        float turn=ramp(t,280,420)-ramp(t,760,1000);
        float lean=ramp(t,430,610)-ramp(t,1020,1280);
        float hand=ramp(t,870,990)-ramp(t,1160,1330);
        model.body.zRot=.027F*lean;model.body.xRot=.018F*lean;
        model.head.xRot=.035F+.048F*turn;model.head.yRot=.06F*turn;model.head.zRot=-.018F*lean;
        model.leftArm.zRot=-.018F;model.rightArm.zRot=.018F+.035F*hand;
        model.rightArm.xRot=.065F*hand;model.rightArm.yRot=.025F*hand;
        HumanoidMotion.connect(model);
    }
}
