package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.MinotaurEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Original articulated geometry: heavy shoulders, a bull jaw, curling horns, hands, and split hooves. */
public final class MinotaurModel extends HierarchicalModel<MinotaurEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"minotaur"),"main");
    private final ModelPart root,body,head,jaw,leftArm,rightArm,leftLeg,rightLeg;
    public MinotaurModel(ModelPart baked){root=baked.getChild("root");body=root.getChild("body");head=body.getChild("head");jaw=head.getChild("jaw");
        leftArm=body.getChild("left_arm");rightArm=body.getChild("right_arm");leftLeg=root.getChild("left_leg");rightLeg=root.getChild("right_leg");}
    @Override public ModelPart root(){return root;}
    public static LayerDefinition createBodyLayer(){
        MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot().addOrReplaceChild("root",CubeListBuilder.create(),PartPose.offset(0,24,0));
        PartDefinition body=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,128).addBox(-10,-23,-6,20,15,13)
                .texOffs(0,0).addBox(-6,-8,-4,12,10,9).texOffs(0,128).addBox(-8,-24,3,16,17,7),PartPose.offset(0,-18,0));
        PartDefinition head=body.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-6,-10,-8,12,11,10)
                .texOffs(0,128).addBox(-7,-11,-5,14,5,10).texOffs(0,0).addBox(-5,-3,-13,10,5,6),PartPose.offset(0,-21,-4));
        head.addOrReplaceChild("eyes",CubeListBuilder.create().texOffs(128,128).addBox(-5.4F,-5,-8.2F,3,1.5F,.5F).addBox(2.4F,-5,-8.2F,3,1.5F,.5F),PartPose.ZERO);
        head.addOrReplaceChild("nostrils",CubeListBuilder.create().texOffs(128,128).addBox(-3.7F,-1,-13.1F,2,1.5F,.3F).addBox(1.7F,-1,-13.1F,2,1.5F,.3F),PartPose.ZERO);
        head.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(0,0).addBox(-4,-.5F,-7,8,3,7).texOffs(128,128).addBox(-3.8F,-.6F,-6.9F,7.6F,.3F,6),PartPose.offset(0,2,-6));
        for(int side:new int[]{-1,1}){
            PartDefinition horn=head.addOrReplaceChild(side<0?"right_horn":"left_horn",CubeListBuilder.create().texOffs(128,0).addBox(-2,-8,-2,4,9,4),PartPose.offsetAndRotation(side*6,-8,0,0,0,side*.8F));
            PartDefinition turn=horn.addOrReplaceChild("turn",CubeListBuilder.create().texOffs(128,0).addBox(-1.5F,-7,-1.5F,3,7,3),PartPose.offsetAndRotation(0,-7,0,.45F,0,-side*1.1F));
            turn.addOrReplaceChild("tip",CubeListBuilder.create().texOffs(128,0).addBox(-.75F,-6,-.75F,1.5F,6,1.5F),PartPose.offsetAndRotation(0,-6,0,.5F,0,-side*.35F));
            PartDefinition arm=body.addOrReplaceChild(side<0?"right_arm":"left_arm",CubeListBuilder.create().texOffs(0,128).addBox(-4,-3,-4,8,12,8),PartPose.offsetAndRotation(side*12,-20,0,0,0,-side*.12F));
            PartDefinition forearm=arm.addOrReplaceChild("forearm",CubeListBuilder.create().texOffs(0,0).addBox(-3.5F,0,-3.5F,7,11,7),PartPose.offset(0,8,0));
            forearm.addOrReplaceChild("hand",CubeListBuilder.create().texOffs(0,0).addBox(-4,0,-4,8,5,7).texOffs(128,0).addBox(-3.6F,3,-4.7F,2,3,1).addBox(-.7F,3,-4.7F,2,3,1).addBox(2.1F,3,-4.7F,2,3,1),PartPose.offset(0,9,0));
            PartDefinition leg=root.addOrReplaceChild(side<0?"right_leg":"left_leg",CubeListBuilder.create().texOffs(0,128).addBox(-3.5F,0,-3,7,9,7),PartPose.offset(side*5,-18,1));
            PartDefinition shin=leg.addOrReplaceChild("shin",CubeListBuilder.create().texOffs(0,0).addBox(-2.8F,0,-2,5.6F,7,5),PartPose.offset(0,8,1));
            shin.addOrReplaceChild("hoof",CubeListBuilder.create().texOffs(128,128).addBox(-3.5F,0,-5,3.2F,3,7).addBox(.3F,0,-5,3.2F,3,7),PartPose.offset(0,7,0));
        }
        return LayerDefinition.create(mesh,256,512);
    }
    @Override public void setupAnim(MinotaurEntity entity,float walk,float amount,float age,float yaw,float pitch){
        root.getAllParts().forEach(ModelPart::resetPose);int state=entity.motion();float breath=Mth.sin(age*.08F)*.025F;
        body.xRot=.12F+breath;head.yRot=yaw*Mth.DEG_TO_RAD*.45F;head.xRot=pitch*Mth.DEG_TO_RAD*.4F; jaw.xRot=.05F+breath;
        leftLeg.xRot=Mth.cos(walk*.65F)*amount*.6F;rightLeg.xRot=Mth.cos(walk*.65F+Mth.PI)*amount*.6F;
        if(state==MinotaurEntity.WINDUP){body.xRot=.36F;head.xRot=-.25F;leftArm.xRot=rightArm.xRot=-.25F;jaw.xRot=.3F;}
        if(state==MinotaurEntity.CHARGING){body.xRot=.55F;head.xRot=-.4F;leftArm.xRot=rightArm.xRot=-.7F;float stride=Mth.sin(age*1.3F)*.8F;leftLeg.xRot=stride;rightLeg.xRot=-stride;}
        if(state==MinotaurEntity.STUNNED){head.xRot=.55F;head.zRot=Mth.sin(age*.7F)*.09F;body.xRot=.15F;jaw.xRot=.35F;leftArm.xRot=rightArm.xRot=.1F;}
        if(state==MinotaurEntity.WOUNDED){root.y+=9;body.xRot=1.0F;head.xRot=-.65F;leftArm.xRot=-1.0F+Mth.sin(age*.12F)*.08F;rightArm.xRot=-1.0F-Mth.sin(age*.12F)*.08F;leftLeg.xRot=-.7F;rightLeg.xRot=-.7F;jaw.xRot=.18F;}
        if(state==MinotaurEntity.RELEASED){body.xRot=.07F+breath;head.xRot=.18F;leftArm.xRot=Mth.cos(walk*.65F+Mth.PI)*amount*.15F;rightArm.xRot=Mth.cos(walk*.65F)*amount*.15F;jaw.xRot=.02F;}
    }
}
