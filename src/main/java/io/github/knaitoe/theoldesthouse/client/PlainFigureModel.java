package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** The distant child squats on folded legs, with the shoulders over the knees. */
public final class PlainFigureModel extends HumanoidModel<LiteraryActor> {
    public static final float SCALE=.58F;
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"plain_figure"),"main");
    public final ModelPart leftKnee,rightKnee,leftElbow,rightElbow;

    public PlainFigureModel(ModelPart root){
        super(root);leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");
        leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");crouch();
    }
    public static LayerDefinition layer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();
        for(boolean left:new boolean[]{false,true}){
            var leg=root.addOrReplaceChild(left?"left_leg":"right_leg",SegmentedSkin.limb(0,16,0,-2,0,-2,4,6,4,left,false),PartPose.offset(left?1.9F:-1.9F,12,0));
            leg.addOrReplaceChild("knee",SegmentedSkin.limb(0,16,6,-2,0,-2,4,6,4,left,false),PartPose.offset(0,6,0));
            float x=left?-1:-3;
            var arm=root.addOrReplaceChild(left?"left_arm":"right_arm",SegmentedSkin.limb(40,16,0,x,-2,-2,4,6,4,left,true),PartPose.offset(left?5:-5,2,0));
            arm.addOrReplaceChild("elbow",SegmentedSkin.limb(40,16,6,x,0,-2,4,6,4,left,true),PartPose.offset(0,4,0));
        }
        return LayerDefinition.create(mesh,128,64);
    }
    public void crouch(){
        for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);
        young=false;crouching=false;riding=false;attackTime=0;
        leftLeg.setPos(3,17.58F,1);rightLeg.setPos(-3,17.58F,1);
        leftLeg.xRot=rightLeg.xRot=-1.18F;leftKnee.xRot=rightKnee.xRot=2.36F;
        body.xRot=1.04F;head.xRot=-.54F;
        leftArm.xRot=rightArm.xRot=-1.14F;leftArm.zRot=.18F;rightArm.zRot=-.18F;
        leftElbow.xRot=rightElbow.xRot=.6F;leftElbow.zRot=.14F;rightElbow.zRot=-.14F;
        HumanoidMotion.connect(this);
    }
    @Override public void setupAnim(LiteraryActor actor,float walk,float amount,float age,float yaw,float pitch){crouch();}
    /** Same feet, facing and native child scale as the actor; also used by the long-lens fallback. */
    static PoseStack atFeet(){return atFeet(Vec3.ZERO);}
    static PoseStack atFeet(Vec3 feet){var pose=new PoseStack();pose.translate(feet.x,feet.y,feet.z);pose.scale(SCALE,-SCALE,-SCALE);pose.translate(0,-1.501,0);return pose;}
    static boolean matches(int role,int phase){return role==LiteraryActor.SILHOUETTE&&phase==4;}
    public static final class Renderer extends MobRenderer<LiteraryActor,PlainFigureModel> {
        public Renderer(EntityRendererProvider.Context context){super(context,new PlainFigureModel(context.bakeLayer(LAYER)),.14F);}
        @Override public ResourceLocation getTextureLocation(LiteraryActor actor){return LiteraryRenderers.skin(LiteraryActor.SILHOUETTE,4);}
    }
}
