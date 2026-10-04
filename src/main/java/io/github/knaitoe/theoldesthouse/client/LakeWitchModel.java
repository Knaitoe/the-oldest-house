package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
/** The remembered girl stands; the hunting body runs low on its hands and feet. */
public final class LakeWitchModel extends PlayerModel<LakeWitchEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_witch"),"main");
    public LakeWitchModel(ModelPart root){super(root,true);}
    public static LayerDefinition createBodyLayer(){return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,true),64,64);}
    @Override public void setupAnim(LakeWitchEntity e,float walk,float speed,float age,float yaw,float pitch){
        resetBody();super.setupAnim(e,walk,speed,age,yaw,pitch);
        if(e.memory()){leftArm.xRot=-.2F;rightArm.xRot=-.25F;head.xRot=.25F;copyClothes();}
        else {
            huntPose(walk,speed,age,e.striking());
            // The claw swipe of an actual hit, and a recoil when she is wounded.
            float swipe=(float)Math.sin(Math.sqrt(attackTime)*Math.PI);
            if(attackTime>0){rightArm.xRot-=swipe*1.7F;rightArm.zRot+=swipe*.45F;leftArm.xRot-=swipe*.6F;}
            if(e.hurtTime>0){float recoil=e.hurtTime/10F;head.xRot-=recoil*.7F;body.xRot-=recoil*.25F;leftArm.xRot+=recoil*.9F;rightArm.xRot+=recoil*.9F;}
            copyClothes();
        }
    }
    private void resetBody(){for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg,jacket,leftSleeve,rightSleeve,leftPants,rightPants})part.resetPose();}
    public void huntPose(float walk,float speed,float age,boolean striking){
        resetBody();float stride=(float)Math.cos(walk*1.4F)*Math.min(.24F,Math.max(.025F,speed*.4F));
        body.setPos(0,15,-6);body.xRot=(float)Math.PI/2;
        head.setPos(0,16,-9);head.xRot=striking?-.18F:.08F;head.zRot=(float)Math.sin(age*.045F)*.025F;
        leftArm.setPos(5,14,-6);rightArm.setPos(-5,14,-6);
        leftArm.xRot=stride;rightArm.xRot=-stride;leftArm.zRot=-.12F;rightArm.zRot=.12F;
        // Winding up and lunging, both claws reach for the throat.
        if(striking){leftArm.xRot=-1.15F+stride*.3F;rightArm.xRot=-1.25F-stride*.3F;leftArm.zRot=-.3F;rightArm.zRot=.3F;}
        leftLeg.setPos(2.8F,14,6);rightLeg.setPos(-2.8F,14,6);
        leftLeg.xRot=.58F-stride;rightLeg.xRot=.58F+stride;leftLeg.zRot=-.09F;rightLeg.zRot=.09F;
        copyClothes();
    }
    private void copyClothes(){hat.copyFrom(head);jacket.copyFrom(body);leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);}
}
