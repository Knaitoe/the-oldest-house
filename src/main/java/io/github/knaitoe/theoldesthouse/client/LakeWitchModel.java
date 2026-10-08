package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LakeWitchEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
/** The remembered girl stands; the hunting body runs low on its hands and feet. */
public final class LakeWitchModel extends PlayerModel<LakeWitchEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_witch"),"main");
    private static final float DEG=(float)Math.PI/180;
    public LakeWitchModel(ModelPart root){super(root,true);}
    public static LayerDefinition createBodyLayer(){return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,true),64,64);}
    @Override public void setupAnim(LakeWitchEntity e,float walk,float speed,float age,float yaw,float pitch){
        resetBody();super.setupAnim(e,walk,speed,age,yaw,pitch);
        if(e.memory()){leftArm.xRot=-.2F;rightArm.xRot=-.25F;head.xRot=.25F;copyClothes();return;}
        huntPose(walk,speed,age,e.huntPhase(),e.striking());
        // She watches the one she is flanking: the server's look control, which the all-fours pose would otherwise throw away.
        head.yRot+=Mth.clamp(yaw,-70,70)*DEG;head.xRot+=Mth.clamp(pitch,-30,45)*DEG*.6F;
        // An actual hit rakes the near claw down and across; a wound makes her recoil.
        float rake=(float)Math.sin(Math.sqrt(attackTime)*Math.PI);
        if(attackTime>0){rightArm.xRot+=rake*.9F;rightArm.zRot-=rake*.55F;leftArm.xRot-=rake*.35F;head.xRot-=rake*.25F;}
        if(e.hurtTime>0){float recoil=e.hurtTime/10F;head.xRot-=recoil*.7F;body.xRot-=recoil*.25F;leftArm.xRot+=recoil*.9F;rightArm.xRot+=recoil*.9F;}
        copyClothes();
    }
    private void resetBody(){for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg,jacket,leftSleeve,rightSleeve,leftPants,rightPants})part.resetPose();}
    /**
     * On all fours, torso level, head held up ahead of the shoulders. Stalking she is lower and looks up from under her
     * brow; withdrawing she goes head-down at a scurry; lunging she rises onto reaching claws. The limb pivots sit on
     * her back line so hands and feet meet the ground through the stride (the native cast proof measures this).
     */
    public void huntPose(float walk,float speed,float age,int phase,boolean striking){
        resetBody();
        boolean lunge=striking||phase==LakeWitchEntity.LUNGE,withdraw=!lunge&&phase==LakeWitchEntity.WITHDRAW;
        float crouch=!lunge&&!withdraw?1:0;
        float stride=(float)Math.cos(walk*1.4F)*Math.min(lunge?.42F:withdraw?.34F:.3F,Math.max(.025F,speed*.5F));
        body.setPos(0,15+crouch,-6);body.xRot=(float)Math.PI/2;
        head.setPos(0,16+crouch*.6F,-9);head.xRot=lunge?-.22F:withdraw?.38F:-.04F;head.zRot=(float)Math.sin(age*.045F)*.025F;
        leftArm.setPos(5,13.5F+crouch*.3F,-6);rightArm.setPos(-5,13.5F+crouch*.3F,-6);
        leftArm.xRot=-.15F+stride;rightArm.xRot=-.15F-stride;leftArm.zRot=-.12F;rightArm.zRot=.12F;
        // Winding up and lunging, both claws reach for the throat.
        if(lunge){leftArm.xRot=-1.15F+stride*.3F;rightArm.xRot=-1.25F-stride*.3F;leftArm.zRot=-.3F;rightArm.zRot=.3F;}
        leftLeg.setPos(2.8F,13+crouch*.9F,6);rightLeg.setPos(-2.8F,13+crouch*.9F,6);
        float fold=.62F+crouch*.13F;
        leftLeg.xRot=fold-stride;rightLeg.xRot=fold+stride;leftLeg.zRot=-.09F;rightLeg.zRot=.09F;
        copyClothes();
    }
    private void copyClothes(){hat.copyFrom(head);jacket.copyFrom(body);leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);}
}
