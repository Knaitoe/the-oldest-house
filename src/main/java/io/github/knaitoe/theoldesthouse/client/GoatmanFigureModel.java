package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanFigure;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanChild;

/**
 * A man with the head of a goat (0.4.53): a gaunt body, arms too long, furred legs ending in hooves, and a goat's skull
 * with muzzle, beard, drooping ears and swept-back horns. Box UVs match tools/generate_goatman_night_assets.py exactly.
 */
public final class GoatmanFigureModel<T extends LivingEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"goatman"),"main");
    private final ModelPart root,head,body,rightArm,leftArm,rightLeg,leftLeg;
    public GoatmanFigureModel(ModelPart baked){
        root=baked.getChild("root");head=root.getChild("head");body=root.getChild("body");
        rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");
    }
    @Override public ModelPart root(){return root;}
    public static LayerDefinition createBodyLayer(){
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot().addOrReplaceChild("root",CubeListBuilder.create(),PartPose.offset(0,24,0));
        root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(0,16).addBox(-3.5F,0,-1.5F,7,13,3),PartPose.offset(0,-27,0));
        PartDefinition head=root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3.5F,-7,-4,7,7,8),PartPose.offset(0,-27,0));
        head.addOrReplaceChild("muzzle",CubeListBuilder.create().texOffs(30,0).addBox(-2,-4.5F,-9,4,4,5),PartPose.ZERO);
        head.addOrReplaceChild("beard",CubeListBuilder.create().texOffs(58,6).addBox(-1,-.5F,-8.6F,2,4,1),PartPose.ZERO);
        head.addOrReplaceChild("right_horn",CubeListBuilder.create().texOffs(48,0).addBox(-1,-4,-1,2,4,2).texOffs(56,0).addBox(-1,-6.5F,.5F,2,3,2),
            PartPose.offsetAndRotation(-2.2F,-6.5F,-1,-.55F,0,-.25F));
        head.addOrReplaceChild("left_horn",CubeListBuilder.create().mirror().texOffs(48,0).addBox(-1,-4,-1,2,4,2).texOffs(56,0).addBox(-1,-6.5F,.5F,2,3,2),
            PartPose.offsetAndRotation(2.2F,-6.5F,-1,-.55F,0,.25F));
        head.addOrReplaceChild("right_ear",CubeListBuilder.create().texOffs(48,6).addBox(-3,-.5F,-1,3,1,2),PartPose.offsetAndRotation(-3.5F,-5,.5F,0,0,-.35F));
        head.addOrReplaceChild("left_ear",CubeListBuilder.create().mirror().texOffs(48,6).addBox(0,-.5F,-1,3,1,2),PartPose.offsetAndRotation(3.5F,-5,.5F,0,0,.35F));
        root.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(20,16).addBox(-1.5F,-1,-1.5F,3,15,3),PartPose.offset(-5,-26,0));
        root.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(32,16).addBox(-1.5F,-1,-1.5F,3,15,3),PartPose.offset(5,-26,0));
        root.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(0,34).addBox(-1.5F,0,-1.5F,3,14,3),PartPose.offset(-1.8F,-14,0));
        root.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(12,34).addBox(-1.5F,0,-1.5F,3,14,3),PartPose.offset(1.8F,-14,0));
        return LayerDefinition.create(mesh,64,64);
    }
    @Override public void setupAnim(T e,float swing,float amount,float age,float yaw,float pitch){
        float partial=age-e.tickCount;
        if(e instanceof GoatmanFigure f)pose(age,f.walk(partial),Math.min(1,Math.abs(f.speed())*2.5F),f.heaving(),yaw,pitch);
        else if(e instanceof GoatmanChild c)pose(age,c.walk(partial),Math.min(1,Math.abs(c.speed())),c.heaving(),yaw,pitch);
    }
    /** The whole pose from plain numbers, so the client proof can draw it without a world. */
    public void pose(float age,float walk,float pace,boolean heaving,float yaw,float pitch){
        root.getAllParts().forEach(ModelPart::resetPose);
        head.yRot=yaw*Mth.DEG_TO_RAD;head.xRot=pitch*Mth.DEG_TO_RAD;
        // The head sits a little crooked, and the arms hang forward of the body.
        head.zRot=.16F+.03F*Mth.sin(age*.05F);
        rightArm.xRot=-.12F+Mth.cos(walk*.62F)*.55F*pace;leftArm.xRot=-.12F-Mth.cos(walk*.62F)*.55F*pace;
        rightArm.zRot=.06F;leftArm.zRot=-.06F;
        rightLeg.xRot=-Mth.cos(walk*.62F)*.7F*pace;leftLeg.xRot=Mth.cos(walk*.62F)*.7F*pace;
        // Its walk jerks: a small hitch every stride.
        if(pace>0){float hitch=Mth.abs(Mth.sin(walk*1.24F));body.xRot+=.06F*hitch;head.xRot+=.1F*hitch;}
        if(heaving){
            // Laughing without a sound: the shoulders and head jerk together.
            float h=Mth.abs(Mth.sin(age*.9F))*Mth.abs(Mth.sin(age*.37F));
            body.xRot+=.12F+.18F*h;head.xRot+=.25F*h;
        }
        // The torso bends at its hip line; the skull and shoulders travel with it.
        if(attackTime>0)rightArm.xRot=-1.8F-Mth.sin(attackTime*Mth.PI)*.9F;
        float lean=body.xRot,top=-14-13*Mth.cos(lean),forward=-13*Mth.sin(lean);
        body.y=top;body.z=forward;head.y=top;head.z=forward;head.xRot+=lean;
        for(var arm:new ModelPart[]{rightArm,leftArm}){arm.y=top+Mth.cos(lean);arm.z=forward+Mth.sin(lean);arm.xRot+=lean;}
    }
}
