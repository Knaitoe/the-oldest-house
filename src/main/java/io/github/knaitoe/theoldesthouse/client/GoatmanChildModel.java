package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.util.Mth;

public final class GoatmanChildModel extends HumanoidModel<GoatmanChild> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"goatman_child"),"main");
    public final ModelPart leftElbow,rightElbow,leftKnee,rightKnee;
    public GoatmanChildModel(ModelPart root){super(root);leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");}
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();
        for(boolean left:new boolean[]{false,true}){
            int u=left?32:40,v=left?48:16;float x=left?-1:-3;
            var arm=root.addOrReplaceChild(left?"left_arm":"right_arm",SegmentedSkin.limb(u,v,0,x,-2,-2,4,6,4,false,true),PartPose.offset(left?5:-5,2,0));
            arm.addOrReplaceChild("elbow",SegmentedSkin.limb(u,v,6,x,0,-2,4,6,4,false,true),PartPose.offset(0,4,0));
            u=left?16:0;
            var leg=root.addOrReplaceChild(left?"left_leg":"right_leg",SegmentedSkin.limb(u,v,0,-2,0,-2,4,6,4,false,false),PartPose.offset(left?1.9F:-1.9F,12,0));
            leg.addOrReplaceChild("knee",SegmentedSkin.limb(u,v,6,-2,0,-2,4,6,4,false,false),PartPose.offset(0,6,0));
        }
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(GoatmanChild e,float walk,float amount,float age,float yaw,float pitch){
        HumanoidMotion.reset(this);
        if(e.enteringWindow()){windowPose(e.windowEntry(age-e.tickCount));return;}
        boolean still=e.tell(GoatmanVignette.STILL)&&!e.stalkingAppearance();
        float speed=Math.abs(e.speed())/8,phase=e.walk(age-e.tickCount)*.78F;
        super.setupAnim(e,0,0,still?0:age,yaw,pitch);
        animatePose(e.visibleSkin(),phase,speed,age+e.skin()*17,e.seated(),e.cowering(),e.heaving(),still);
        if(e.getMainHandItem().is(GoatmanRegistry.BRAT.get())){rightArm.xRot=-1.2F-.16F*Mth.sin(age*.3F+e.skin());head.xRot+=.1F;}
        if(!e.stalkingAppearance()&&e.tell(GoatmanVignette.HEAD)&&Minecraft.getInstance().player!=null){
            var at=Minecraft.getInstance().player.position().subtract(e.position());
            head.yRot=Mth.wrapDegrees((float)(Math.atan2(at.z,at.x)*180/Math.PI)-90-e.yBodyRot)*Mth.DEG_TO_RAD;
        }
        animateFear(e.fear(),e.fearStage(),age+e.skin()*17);
        limitLook();HumanoidMotion.connect(this);
    }
    /** Also used by native visual proofs, with the same joints as the live renderer. */
    public void pose(float phase,float speed,float age,boolean seated,boolean cower,boolean heave){
        pose(0,phase,speed,age,seated,cower,heave);
    }
    public void pose(int skin,float phase,float speed,float age,boolean seated,boolean cower,boolean heave){
        HumanoidMotion.reset(this);young=false;animatePose(skin,phase,speed,age,seated,cower,heave,false);HumanoidMotion.connect(this);
    }
    private void animatePose(int skin,float phase,float speed,float age,boolean seated,boolean cower,boolean heave,boolean still){
        if(!still){body.yRot=.025F*Mth.sin(age*.045F);head.xRot+=.025F*Mth.sin(age*.032F);}
        if(seated){
            head.y=hat.y=body.y=5;leftArm.y=rightArm.y=7;leftLeg.y=rightLeg.y=17;
            leftLeg.xRot=rightLeg.xRot=-1.45F;leftLeg.yRot=.12F;rightLeg.yRot=-.12F;leftArm.xRot=-.6F;rightArm.xRot=-.5F;
        }else{head.y=hat.y=body.y=0;leftArm.y=rightArm.y=2;leftLeg.y=rightLeg.y=12;}
        if(!seated&&speed>.002F){
            float amount=Math.min(1,speed/.11F);boolean running=speed>.12F;
            HumanoidMotion.gait(this,phase,amount,running);
            if(!cower&&!still)CousinGait.apply(this,skin,phase,amount,running);
        }
        if(cower){body.xRot=.45F;head.xRot=.4F;leftArm.xRot=rightArm.xRot=-2.3F;leftArm.zRot=-.25F;rightArm.zRot=.25F;leftLeg.xRot=rightLeg.xRot=-.55F;}
        if(heave){
            // Laughing with no sound coming out: the shoulders and head jerk together.
            float h=Mth.abs(Mth.sin(age*.9F))*Mth.abs(Mth.sin(age*.37F));
            body.xRot+=.1F+.16F*h;head.xRot+=.22F*h;
        }
        if(attackTime>0&&!seated&&!cower)rightArm.xRot=-1.8F-Mth.sin(attackTime*Mth.PI)*.65F;
    }
    public void cowerPose(){pose(0,0,0,false,true,false);}
    public static float ease(float from,float to,float progress){float t=Mth.clamp((progress-from)/(to-from),0,1);return t*t*(3-2*t);}
    /** Full horizontal fit only while passing the sash, with a gradual rise on the bathroom side. */
    public static float windowFold(float progress){return ease(.12F,.38F,progress)*(1-ease(.78F,1,progress));}
    public void windowPose(float progress){
        HumanoidMotion.reset(this);young=false;float fold=windowFold(progress),lean=1.43F*fold;
        float pull=ease(.38F,.61F,progress)*(1-ease(.61F,.85F,progress)),recover=1-ease(.78F,1,progress);
        float leg=-1.3F*fold,knee=2.8F*fold;
        leftLeg.y=rightLeg.y=24-6*Mth.cos(leg)-6*Mth.cos(leg+knee);leftLeg.z=rightLeg.z=10*fold;
        leftLeg.xRot=leg+.12F*pull;rightLeg.xRot=leg-.12F*pull;leftKnee.xRot=knee-.12F*pull;rightKnee.xRot=knee+.12F*pull;
        body.xRot=lean;body.zRot=.035F*Mth.sin(progress*Mth.PI)*fold;
        head.xRot=-lean-.55F*fold;head.yRot=.08F*Mth.sin(progress*Mth.PI*2)*fold;
        float reach=Mth.lerp(ease(0,.12F,progress),-.1F,-2.65F)*recover;
        rightArm.xRot=-lean+Mth.lerp(fold,reach,-1.25F+.5F*pull);leftArm.xRot=rightArm.xRot-.13F*pull;
        rightArm.zRot=.12F*fold;leftArm.zRot=-.12F*fold;
        rightElbow.xRot=(-.1F-.95F*pull)*fold;leftElbow.xRot=(-.1F-.7F*pull)*fold;
        HumanoidMotion.connect(this);
    }
    private void animateFear(int role,int stage,float age){
        if(stage==0||role==0)return;
        if(stage==GoatmanFear.CONFUSION){
            // Still at their ordinary places: look between cousins instead of hiding or bracing.
            head.yRot+=Mth.sin(age*.035F+role)*.18F;head.xRot+=.025F;
            if(role==GoatmanFear.COMFORT)leftArm.xRot=-.45F;
            return;
        }
        boolean terror=stage==GoatmanFear.TERROR;float tremor=Mth.sin(age*.83F)*(terror?.025F:.009F);
        switch(role){
            case GoatmanFear.BRACE->{body.xRot=terror?.12F:.06F;leftArm.xRot=rightArm.xRot=terror?-1.5F:-.85F;leftArm.zRot=-.28F;rightArm.zRot=.28F;}
            case GoatmanFear.WATCH->{head.xRot=-.08F;leftArm.xRot=-.45F;rightArm.xRot=-.25F;}
            case GoatmanFear.PACE->{body.xRot=.1F;head.xRot=.16F;}
            case GoatmanFear.COMFORT->{body.xRot=.22F;leftArm.xRot=-.9F;rightArm.xRot=-.5F;head.xRot=.18F;}
            case GoatmanFear.TREMBLE->{body.zRot=tremor;head.zRot=-tremor;leftArm.xRot=-.8F+tremor;rightArm.xRot=-.8F-tremor;}
            case GoatmanFear.EARS->{body.xRot=terror?.3F:.05F;head.xRot=terror?.3F:.08F;leftArm.xRot=rightArm.xRot=terror?-2.1F:-.6F;leftArm.zRot=terror?-.6F:-.1F;rightArm.zRot=terror?.6F:.1F;}
            case GoatmanFear.STARTLE->{body.xRot=.15F;leftArm.xRot=-.55F;rightArm.xRot=-.3F;head.xRot=.05F;head.yRot+=Mth.sin(age*.06F)*.1F;}
            default->{}
        }
    }
    /** The proof uses the same connected joints and fear gestures as the tracked actor. */
    public void fearPose(int role,float phase,float speed,float age){fearPose(role,GoatmanFear.TERROR,phase,speed,age);}
    public void fearPose(int role,int stage,float phase,float speed,float age){HumanoidMotion.reset(this);young=false;animatePose(0,phase,speed,age,stage<GoatmanFear.FEAR||role==GoatmanFear.TREMBLE,stage==GoatmanFear.TERROR&&role==GoatmanFear.EARS,false,false);animateFear(role,stage,age);limitLook();HumanoidMotion.connect(this);}
    private void limitLook(){head.yRot=Mth.clamp(Mth.wrapDegrees(head.yRot*Mth.RAD_TO_DEG),-75,75)*Mth.DEG_TO_RAD;head.xRot=Mth.clamp(head.xRot,-60*Mth.DEG_TO_RAD,60*Mth.DEG_TO_RAD);}
    /** Exercise the same bounded look and torso composition in the native client proof. */
    public void lookPose(float yaw,float pitch,boolean cower){HumanoidMotion.reset(this);young=false;animatePose(0,0,0,0,false,cower,false,false);head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;limitLook();HumanoidMotion.connect(this);}
}
