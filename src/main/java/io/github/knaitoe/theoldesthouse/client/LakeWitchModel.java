package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

/** The remembered girl stands; the same skin's hunting body scrabbles on jointed hands and feet. */
public final class LakeWitchModel extends PlayerModel<LakeWitchEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_witch"),"main");
    public final ModelPart leftElbow,rightElbow,leftKnee,rightKnee,jaw;
    private final ModelPart leftCuff,rightCuff,leftShinCloth,rightShinCloth;
    private final ModelPart jawOverlay;
    public LakeWitchModel(ModelPart root){
        super(root,true);leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");
        leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");
        leftCuff=leftSleeve.getChild("elbow");rightCuff=rightSleeve.getChild("elbow");
        leftShinCloth=leftPants.getChild("knee");rightShinCloth=rightPants.getChild("knee");
        jaw=head.getChild("jaw");jawOverlay=hat.getChild("jaw");
    }
    private static void arm(PartDefinition root,String name,int u,int v,boolean left,CubeDeformation inflate){
        float x=left?-1:-2;
        var upper=root.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(x,-2,-2,3,6,4,inflate),PartPose.offset(left?5:-5,2.5F,0));
        upper.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(u,v+6).addBox(x,0,-2,3,6,4,inflate),PartPose.offset(0,4,0));
    }
    private static void leg(PartDefinition root,String name,int u,int v,boolean left,CubeDeformation inflate){
        var upper=root.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(-2,0,-2,4,6,4,inflate),PartPose.offset(left?1.9F:-1.9F,12,0));
        upper.addOrReplaceChild("knee",CubeListBuilder.create().texOffs(u,v+6).addBox(-2,0,-2,4,6,4,inflate),PartPose.offset(0,6,0));
    }
    public static LayerDefinition createBodyLayer(){
        var mesh=PlayerModel.createMesh(CubeDeformation.NONE,true);var root=mesh.getRoot();var outer=new CubeDeformation(.25F);
        var head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4,-8,-4,8,6,8),PartPose.ZERO);
        head.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(0,6).addBox(-4,0,-7,8,2,8),PartPose.offset(0,-2,3));
        var hat=root.addOrReplaceChild("hat",CubeListBuilder.create().texOffs(32,0).addBox(-4,-8,-4,8,6,8,new CubeDeformation(.5F)),PartPose.ZERO);
        hat.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(32,6).addBox(-4,0,-7,8,2,8,new CubeDeformation(.5F)),PartPose.offset(0,-2,3));
        arm(root,"right_arm",40,16,false,CubeDeformation.NONE);arm(root,"left_arm",32,48,true,CubeDeformation.NONE);
        arm(root,"right_sleeve",40,32,false,outer);arm(root,"left_sleeve",48,48,true,outer);
        leg(root,"right_leg",0,16,false,CubeDeformation.NONE);leg(root,"left_leg",16,48,true,CubeDeformation.NONE);
        leg(root,"right_pants",0,32,false,outer);leg(root,"left_pants",0,48,true,outer);
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(LakeWitchEntity e,float walk,float speed,float age,float yaw,float pitch){
        resetBody();super.setupAnim(e,walk,speed,age,yaw,pitch);
        if(e.memory()){leftArm.xRot=-.2F;rightArm.xRot=-.25F;head.xRot=.25F;copyClothes();return;}
        huntPose(walk,speed,age,e.huntPhase(),e.striking());
        lookPose(yaw,pitch);
        attackPose(attackTime,e.biting(),e.striking());
        if(e.hurtTime>0){float recoil=e.hurtTime/10F;head.xRot-=recoil*.7F;body.xRot-=recoil*.25F;leftArm.xRot+=recoil*.9F;rightArm.xRot+=recoil*.9F;leftElbow.xRot+=recoil*.8F;rightElbow.xRot+=recoil*.8F;}
        copyClothes();
    }
    /** Reapply native look direction after the low hunting pose resets the model. */
    public void lookPose(float yaw,float pitch){
        head.yRot+=net.minecraft.util.Mth.clamp(yaw,-70F,70F)*(float)Math.PI/180F;
        head.xRot+=net.minecraft.util.Mth.clamp(pitch,-30F,45F)*(float)Math.PI/180F*.6F;
    }
    public void resetBody(){
        for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg,jacket,leftSleeve,rightSleeve,leftPants,rightPants,
                leftElbow,rightElbow,leftKnee,rightKnee,leftCuff,rightCuff,leftShinCloth,rightShinCloth,jaw,jawOverlay})part.resetPose();
    }
    /** Native swing progress drives either a hand rake or a braced, opening-jaw head thrust. */
    public void attackPose(float progress,boolean bite,boolean winding){
        if(progress<=0&&!winding)return;
        float strike=(float)Math.sin(Math.sqrt(Math.max(0,progress))*Math.PI);
        if(bite){
            jaw.xRot=(winding?.35F:0)+strike*.75F;head.z-=strike*5;head.y-=strike*.8F;head.xRot-=strike*.45F;body.z-=strike*1.4F;
            leftArm.xRot=-.35F-strike*.45F;rightArm.xRot=-.5F-strike*.35F;leftElbow.xRot=-.25F;rightElbow.xRot=-.35F;
        }else if(progress>0){
            rightArm.xRot-=strike*1.7F;rightArm.zRot+=strike*.75F;rightArm.yRot-=strike*.6F;
            rightElbow.xRot-=strike*.65F;leftArm.xRot-=strike*.6F;body.zRot-=strike*.12F;
        }
        copyClothes();
    }
    private static float phase(float value){return value-(float)Math.floor(value);}
    /** Abrupt reach, scraping pull, short plant, folded recovery; unequal offsets avoid a walking pendulum. */
    private static void claw(ModelPart upper,ModelPart elbow,float cycle,float amount,boolean left){
        float shoulder,bend;
        if(cycle<.14F){shoulder=-1.05F;bend=.15F;}
        else if(cycle<.46F){float drag=(cycle-.14F)/.32F;shoulder=-1.05F+drag*1.48F;bend=.15F-drag*.6F;}
        else if(cycle<.67F){shoulder=.43F;bend=-.45F;}
        else {shoulder=-.25F;bend=1.55F;}
        upper.xRot=shoulder*amount;elbow.xRot=bend*amount;
        upper.zRot=(left?-1:1)*(.16F+Math.max(0,cycle-.67F)*.6F*amount);
        elbow.yRot=(left?-1:1)*.08F*amount;
    }
    private static void hind(ModelPart thigh,ModelPart knee,float cycle,float amount,boolean left){
        boolean folded=cycle>.62F;
        thigh.xRot=.65F+(folded?.58F:-cycle*.25F)*amount;
        knee.xRot=-(folded?1.65F:.85F)*amount;
        thigh.zRot=(left?-1:1)*(.13F+(folded?.16F:0)*amount);
        knee.yRot=(left?-1:1)*.08F*amount;
    }
    public void huntPose(float walk,float speed,float age,boolean striking){
        huntPose(walk,speed,age,LakeWitchEntity.STALK,striking);
    }
    public void huntPose(float walk,float speed,float age,int huntPhase,boolean striking){
        boolean lunge=striking||huntPhase==LakeWitchEntity.LUNGE,withdraw=!lunge&&huntPhase==LakeWitchEntity.WITHDRAW;
        float crouch=!lunge&&!withdraw?1:0;
        resetBody();float moving=Math.min(1,Math.max(0,speed*1.7F));float cycle=phase(walk*.23F);
        float hitch=((int)Math.floor(age/3)%5-2)*.025F;
        body.setPos(moving>0?(cycle<.14F?.2F:-.15F):0,15+crouch+(cycle>.67F?.3F:0)*moving,-6);
        body.xRot=(float)Math.PI/2;body.zRot=(cycle<.46F?-.045F:.065F)*moving;
        head.setPos(0,16+crouch*.6F,-9);head.xRot=lunge?-.22F:withdraw?.38F:-.04F+hitch;head.zRot=hitch*1.5F;
        leftArm.setPos(5,13.5F+crouch*.3F,-6);rightArm.setPos(-5,13.5F+crouch*.3F,-6);
        claw(leftArm,leftElbow,cycle,moving,true);claw(rightArm,rightElbow,phase(cycle+.43F),moving,false);
        leftLeg.setPos(2.8F,13+crouch*.9F,6);rightLeg.setPos(-2.8F,13+crouch*.9F,6);
        hind(leftLeg,leftKnee,phase(cycle+.19F),moving,true);hind(rightLeg,rightKnee,phase(cycle+.77F),moving,false);
        if(lunge){leftArm.xRot=-1.2F;rightArm.xRot=-1.35F;leftElbow.xRot=.15F;rightElbow.xRot=.25F;leftArm.zRot=-.3F;rightArm.zRot=.3F;}
        plant(leftArm,leftElbow,false,moving==0&&!lunge);plant(rightArm,rightElbow,false,moving==0&&!lunge);
        plant(leftLeg,leftKnee,true,moving==0);plant(rightLeg,rightKnee,true,moving==0);
        copyClothes();
    }
    /** Keep jointed limbs above the floor; at rest the hands and feet actually carry her. */
    private void plant(ModelPart upper,ModelPart joint,boolean leg,boolean resting){
        var pose=new PoseStack();upper.translateAndRotate(pose);
        float x0=leg?-2:upper==leftArm?-1:-2,x1=leg?2:x0+3;
        float low=bottom(pose,x0,leg?0:-2,x1,leg?6:4);
        joint.translateAndRotate(pose);low=Math.max(low,bottom(pose,x0,0,x1,6));
        upper.y+=resting?24-low:Math.min(0,24-low);
    }
    private static float bottom(PoseStack pose,float x0,float y0,float x1,float y1){
        float low=-Float.MAX_VALUE;
        for(float x:new float[]{x0,x1})for(float y:new float[]{y0,y1})for(float z:new float[]{-2,2})
            low=Math.max(low,pose.last().pose().transformPosition(new Vector3f(x/16,y/16,z/16)).y*16);
        return low;
    }
    private void copyClothes(){
        hat.copyFrom(head);jacket.copyFrom(body);leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);
        leftCuff.copyFrom(leftElbow);rightCuff.copyFrom(rightElbow);leftShinCloth.copyFrom(leftKnee);rightShinCloth.copyFrom(rightKnee);
        jawOverlay.copyFrom(jaw);
    }
}
