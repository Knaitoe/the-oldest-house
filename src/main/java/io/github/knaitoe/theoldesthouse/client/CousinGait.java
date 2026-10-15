package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

/** Small, fixed differences in the cousins' carriage; phase still comes from actual distance. */
final class CousinGait {
    private record Style(float stride,float arms,float sway,float bounce,float lean,float bias,float toes){}
    private static final Style[] STYLES={
        new Style(1.02F,.96F,1.05F,.95F,.006F,.025F,.012F),
        new Style(.97F,1.07F,.85F,.90F,-.008F,-.030F,.020F),
        new Style(1.04F,.93F,1.15F,1.10F,.012F,.035F,.008F),
        new Style(.99F,.90F,.75F,.80F,-.004F,-.020F,.015F),
        new Style(.96F,1.03F,1.10F,1.05F,.008F,-.040F,.025F),
        new Style(1.03F,1.08F,.90F,.95F,.003F,.020F,.010F),
        new Style(1.00F,.95F,1.20F,.85F,-.006F,.040F,.018F),
        new Style(1.01F,1.01F,.80F,1.15F,.010F,-.025F,.030F),
        new Style(1.00F,1.00F,1.00F,1.00F,0,0,.015F)
    };
    private CousinGait(){}

    /** Apply before the torso connects its shoulders and neck. A copied face copies that cousin's gait, too. */
    static void apply(HumanoidModel<?> m,int skin,float phase,float amount,boolean running){
        var s=STYLES[Math.floorMod(skin,STYLES.length)];float a=Mth.clamp(amount,0,1);
        m.leftLeg.xRot*=s.stride;m.rightLeg.xRot*=s.stride;
        m.rightArm.xRot*=s.arms*(1+s.bias);m.leftArm.xRot*=s.arms*(1-s.bias);
        m.body.zRot*=s.sway;m.body.xRot+=s.lean*a;
        m.leftLeg.yRot+=s.toes*a;m.rightLeg.yRot-=s.toes*a;
        float bob=-Mth.abs(Mth.sin(phase))*(running?.55F:.2F)*a*(s.bounce-1);
        // All upper pivots inherit this hip height in HumanoidMotion.connect.
        m.leftLeg.y+=bob;m.rightLeg.y+=bob;
    }
}
