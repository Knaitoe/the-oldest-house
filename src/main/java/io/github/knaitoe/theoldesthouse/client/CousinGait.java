package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

/**
 * Each cousin's own walk, recognisable across the yard; phase still comes from actual distance.
 * 0.4.118 makes them distinct carriages rather than small variations of one gait: a skipper, a shuffler, a swagger,
 * a stiff marcher, hands in pockets, a timid toe-in, a head-first hurrier and a loper.
 */
final class CousinGait {
    /**
     * stride/arms scale the native swing; spread holds the arms out (or in, negative); sway/roll rock the torso side to
     * side and round; bounce scales the native bob; lean and nod pitch the torso and head; toes turn the feet;
     * pockets (0..1) pins the arms to the body; lag delays the arm swing behind the legs.
     */
    private record Style(float stride,float arms,float spread,float sway,float roll,float bounce,float lean,float nod,float toes,float pockets,float lag){}
    private static final Style[] STYLES={
        // Skipper: springy, a high bob and loose light arms.
        new Style(1.05F,1.15F,.10F,1.0F,.03F,2.10F,.00F,-.05F,.00F,0,0),
        // Shuffler: short scuffing steps, head down, arms barely moving.
        new Style(.55F,.35F,.02F,.6F,.00F,.40F,.14F,.22F,.02F,0,0),
        // Swagger: wide sway and a shoulder roll, arms out from the body.
        new Style(1.00F,1.10F,.22F,2.6F,.16F,.90F,-.04F,-.08F,-.10F,0,.15F),
        // Marcher: stiff straight swing, upright, arms locked high.
        new Style(1.15F,1.45F,.03F,.3F,.00F,.70F,-.07F,-.04F,.00F,0,0),
        // Hands in pockets: arms pinned, a slight hunch and a rolling walk.
        new Style(.85F,.10F,-.06F,1.5F,.06F,.80F,.08F,.10F,-.06F,1,0),
        // Timid: small toe-in steps, arms held close, head bowed.
        new Style(.62F,.45F,-.10F,.7F,.00F,.60F,.10F,.28F,.22F,0,0),
        // Hurrier: head first, a long fast stride and a strong forward lean.
        new Style(1.30F,1.00F,.06F,.8F,.04F,1.10F,.24F,-.12F,.00F,0,0),
        // Loper: long slow-looking strides, dangling arms that trail the legs.
        new Style(1.35F,.80F,.08F,1.3F,.08F,.55F,.04F,.04F,-.08F,0,.45F),
        new Style(1.00F,1.00F,.06F,1.0F,.00F,1.00F,.00F,.00F,.00F,0,0)
    };
    private CousinGait(){}

    /** Apply before the torso connects its shoulders and neck. A copied face copies that cousin's gait, too. */
    static void apply(HumanoidModel<?> m,int skin,float phase,float amount,boolean running){
        var s=STYLES[Math.floorMod(skin,STYLES.length)];float a=Mth.clamp(amount,0,1);
        // A run is already near full reach; the long-legged walkers only stretch it a little further (under 1.2 radians).
        float stride=running?Math.min(s.stride,1.04F):s.stride;
        m.leftLeg.xRot*=stride;m.rightLeg.xRot*=stride;
        // Arms: scaled, delayed behind the legs for the loose walkers, or pinned for hands in pockets.
        float swing=-Mth.cos(phase-s.lag)*(running?1F:.55F)*a*s.arms;
        m.rightArm.xRot=swing*(1-s.pockets)+.18F*s.pockets*a;m.leftArm.xRot=-swing*(1-s.pockets)+.18F*s.pockets*a;
        m.rightArm.zRot=.06F+s.spread;m.leftArm.zRot=-.06F-s.spread;
        m.body.zRot*=s.sway;m.body.yRot+=Mth.sin(phase)*s.roll*a;
        m.body.xRot+=s.lean*a;m.head.xRot+=s.nod*a;
        m.leftLeg.yRot-=s.toes*a;m.rightLeg.yRot+=s.toes*a;
        float bob=-Mth.abs(Mth.sin(phase))*(running?.55F:.2F)*a*(s.bounce-1);
        // All upper pivots inherit this hip height in HumanoidMotion.connect.
        m.leftLeg.y+=bob;m.rightLeg.y+=bob;
    }
}
