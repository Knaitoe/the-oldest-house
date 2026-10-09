package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class GoatmanChildModel extends HumanoidModel<GoatmanChild> {
    public GoatmanChildModel(ModelPart root){super(root);}
    @Override public void setupAnim(GoatmanChild e,float walk,float amount,float age,float yaw,float pitch){
        boolean still=e.tell(GoatmanVignette.STILL);
        super.setupAnim(e,e.walk(age-e.tickCount),Math.min(.8F,Math.abs(e.speed())*2),still?0:age,yaw,pitch);
        if(!still&&!e.girl()){body.yRot=.025F*Mth.sin(age*.045F+e.skin());head.xRot+=.025F*Mth.sin(age*.032F);}
        if(e.tell(GoatmanVignette.HEAD)&&Minecraft.getInstance().player!=null){
            var at=Minecraft.getInstance().player.position().subtract(e.position());
            head.yRot=Mth.wrapDegrees((float)(Math.atan2(at.z,at.x)*180/Math.PI)-90-e.yBodyRot)*Mth.DEG_TO_RAD;
        }
        if(e.seated()){
            head.y=hat.y=body.y=5;leftArm.y=rightArm.y=7;leftLeg.y=rightLeg.y=17;
            leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.yRot=.12F;rightLeg.yRot=-.12F;leftArm.xRot=-.6F;rightArm.xRot=-.5F;
        }else{head.y=hat.y=body.y=0;leftArm.y=rightArm.y=2;leftLeg.y=rightLeg.y=12;}
        if(e.cowering())cowerPose();
        if(e.heaving()){
            // Laughing with no sound coming out: the shoulders and head jerk together.
            float h=Mth.abs(Mth.sin(age*.9F))*Mth.abs(Mth.sin(age*.37F));
            body.xRot+=.1F+.16F*h;head.xRot+=.22F*h;head.y+=1.1F*h;hat.y=head.y;leftArm.y+=1.2F*h;rightArm.y+=1.2F*h;
        }
        hat.copyFrom(head);
    }
    public void cowerPose(){body.xRot=.45F;head.xRot=.4F;leftArm.xRot=rightArm.xRot=-2.3F;leftArm.zRot=-.25F;rightArm.zRot=.25F;leftLeg.xRot=rightLeg.xRot=-.55F;hat.copyFrom(head);}
}
