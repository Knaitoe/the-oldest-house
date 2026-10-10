package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class GoatmanChildModel extends HumanoidModel<GoatmanChild> {
    public GoatmanChildModel(ModelPart root){super(root);}
    @Override public void setupAnim(GoatmanChild e,float walk,float amount,float age,float yaw,float pitch){
        HumanoidMotion.reset(this);
        boolean still=e.tell(GoatmanVignette.STILL)&&!e.stalkingAppearance();
        float speed=Math.abs(e.speed())/8,phase=e.walk(age-e.tickCount)*.78F;
        super.setupAnim(e,0,0,still?0:age,yaw,pitch);
        animatePose(phase,speed,age+e.skin()*17,e.seated(),e.cowering(),e.heaving(),still);
        if(e.getMainHandItem().is(GoatmanRegistry.BRAT.get())){rightArm.xRot=-1.2F-.16F*Mth.sin(age*.3F+e.skin());head.xRot+=.1F;}
        if(!e.stalkingAppearance()&&e.tell(GoatmanVignette.HEAD)&&Minecraft.getInstance().player!=null){
            var at=Minecraft.getInstance().player.position().subtract(e.position());
            head.yRot=Mth.wrapDegrees((float)(Math.atan2(at.z,at.x)*180/Math.PI)-90-e.yBodyRot)*Mth.DEG_TO_RAD;
        }
        limitLook();HumanoidMotion.connect(this);
    }
    /** Also used by native visual proofs, with the same joints as the live renderer. */
    public void pose(float phase,float speed,float age,boolean seated,boolean cower,boolean heave){
        HumanoidMotion.reset(this);young=false;animatePose(phase,speed,age,seated,cower,heave,false);HumanoidMotion.connect(this);
    }
    private void animatePose(float phase,float speed,float age,boolean seated,boolean cower,boolean heave,boolean still){
        if(!still){body.yRot=.025F*Mth.sin(age*.045F);head.xRot+=.025F*Mth.sin(age*.032F);}
        if(seated){
            head.y=hat.y=body.y=5;leftArm.y=rightArm.y=7;leftLeg.y=rightLeg.y=17;
            leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.yRot=.12F;rightLeg.yRot=-.12F;leftArm.xRot=-.6F;rightArm.xRot=-.5F;
        }else{head.y=hat.y=body.y=0;leftArm.y=rightArm.y=2;leftLeg.y=rightLeg.y=12;}
        if(!seated&&speed>.002F)HumanoidMotion.gait(this,phase,Math.min(1,speed/.11F),speed>.12F);
        if(cower){body.xRot=.45F;head.xRot=.4F;leftArm.xRot=rightArm.xRot=-2.3F;leftArm.zRot=-.25F;rightArm.zRot=.25F;leftLeg.xRot=rightLeg.xRot=-.55F;}
        if(heave){
            // Laughing with no sound coming out: the shoulders and head jerk together.
            float h=Mth.abs(Mth.sin(age*.9F))*Mth.abs(Mth.sin(age*.37F));
            body.xRot+=.1F+.16F*h;head.xRot+=.22F*h;
        }
        if(attackTime>0&&!seated&&!cower)rightArm.xRot=-1.8F-Mth.sin(attackTime*Mth.PI)*.65F;
    }
    public void cowerPose(){pose(0,0,0,false,true,false);}
    private void limitLook(){head.yRot=Mth.clamp(Mth.wrapDegrees(head.yRot*Mth.RAD_TO_DEG),-75,75)*Mth.DEG_TO_RAD;head.xRot=Mth.clamp(head.xRot,-60*Mth.DEG_TO_RAD,60*Mth.DEG_TO_RAD);}
    /** Exercise the same bounded look and torso composition in the native client proof. */
    public void lookPose(float yaw,float pitch,boolean cower){HumanoidMotion.reset(this);young=false;animatePose(0,0,0,false,cower,false,false);head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;limitLook();HumanoidMotion.connect(this);}
}
