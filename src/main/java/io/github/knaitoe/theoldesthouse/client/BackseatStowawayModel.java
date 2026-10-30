package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.*;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Low, uneven shoulders and two bent neck segments under a long, narrow skull. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BackseatStowawayModel extends EntityModel<LivingEntity> {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"backseat_stowaway"),"main");
    public static final ResourceLocation SKIN=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/backseat_stowaway.png");
    private final ModelPart root,torso,neck,upperNeck,head,leftShoulder,rightShoulder,leftArm,rightArm,leftElbow,rightElbow;
    public BackseatStowawayModel(ModelPart root){
        this.root=root;torso=root.getChild("torso");neck=torso.getChild("neck");upperNeck=neck.getChild("upper_neck");head=upperNeck.getChild("head");
        leftShoulder=torso.getChild("left_shoulder");rightShoulder=torso.getChild("right_shoulder");leftArm=leftShoulder.getChild("arm");rightArm=rightShoulder.getChild("arm");leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(LAYER,BackseatStowawayModel::layer);}
    private static LayerDefinition layer(){
        var mesh=new MeshDefinition();var torso=mesh.getRoot().addOrReplaceChild("torso",CubeListBuilder.create().texOffs(0,29).addBox(-4.5F,0,-2,9,7,4),PartPose.offset(0,11.5F,0));
        torso.addOrReplaceChild("belly",CubeListBuilder.create().texOffs(28,29).addBox(-2.5F,0,-1.5F,5,7,3),PartPose.offset(0,6.5F,.5F));
        var neck=torso.addOrReplaceChild("neck",CubeListBuilder.create().texOffs(24,0).addBox(-1.5F,-6,-1.5F,3,6,3),PartPose.offset(.5F,0,-.1F));
        var upper=neck.addOrReplaceChild("upper_neck",CubeListBuilder.create().texOffs(38,0).addBox(-1,-5,-1,2,5,2),PartPose.offset(0,-5.5F,0));
        var head=upper.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-3,-8,-2.5F,6,10,5),PartPose.offset(0,-4.8F,-.5F));
        head.addOrReplaceChild("jaw",CubeListBuilder.create().texOffs(24,43).addBox(-2,0,-2,4,3,4),PartPose.offset(0,1,-.4F));
        for(boolean left:new boolean[]{true,false}){
            var shoulder=torso.addOrReplaceChild(left?"left_shoulder":"right_shoulder",CubeListBuilder.create().texOffs(left?0:26,18).addBox(left?0:-6,-1,-2.5F,left?7:6,3,5),PartPose.offset(left?3.5F:-3.5F,left?.2F:1.8F,0));
            var arm=shoulder.addOrReplaceChild("arm",CubeListBuilder.create().texOffs(48,18).addBox(-1.5F,0,-1.5F,3,10,3),PartPose.offset(left?6:-5,1,0));
            var elbow=arm.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(48,33).addBox(-1,0,-1,2,11,2),PartPose.offset(0,9,0));
            for(int i=0;i<3;i++)elbow.addOrReplaceChild("finger_"+i,CubeListBuilder.create().texOffs(42,48).addBox(-.5F,0,-.5F,1,4,1),PartPose.offsetAndRotation((i-1)*.8F,10.5F,0,-.35F,0,(i-1)*.15F));
            var thigh=torso.addOrReplaceChild(left?"left_thigh":"right_thigh",CubeListBuilder.create().texOffs(0,43).addBox(-1.5F,0,-1.5F,3,8,3),PartPose.offsetAndRotation(left?2.3F:-2.3F,12,0,-1.65F,0,left?.25F:-.25F));
            thigh.addOrReplaceChild("shin",CubeListBuilder.create().texOffs(14,43).addBox(-1,0,-1,2,9,2),PartPose.offsetAndRotation(0,7.5F,0,1.4F,0,0));
        }
        return LayerDefinition.create(mesh,64,64);
    }
    public ModelPart root(){return root;}
    @Override public void setupAnim(LivingEntity entity,float walk,float amount,float age,float yaw,float pitch){}
    public void pose(float rise){
        root.getAllParts().forEach(ModelPart::resetPose);
        torso.xRot=.07F*rise;neck.zRot=-.17F;neck.xRot=-.12F;upperNeck.zRot=.31F;upperNeck.xRot=.17F;
        head.zRot=-.08F;head.xRot=.05F;leftShoulder.zRot=-.4F;rightShoulder.zRot=.15F;
        leftArm.xRot=-1.55F*rise;rightArm.xRot=-1.32F*rise;leftElbow.xRot=-.45F*rise;rightElbow.xRot=-.72F*rise;
    }
    @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){root.render(p,out,light,overlay,color);}
    public static void carPose(PoseStack p,float rise){float q=Mth.clamp(rise,0,1);q=q*q*(3-2*q);p.translate(-.625,.565,1.1-.85*q);p.scale(.7F,-Mth.lerp(q,.055F,.8F),.7F);p.translate(0,-1.5,0);}
    /**
     * Sat up, outside the car (0.4.190): the manor's night glass shows it behind a reader's shoulder. Its feet are at the pose's
     * origin, facing the pose's +z; it sits as high as it sat on the back seat.
     */
    public void sitting(PoseStack p,MultiBufferSource out,int light,int overlay){
        pose(1);p.pushPose();p.translate(0,.565,0);p.scale(.7F,-.8F,.7F);p.translate(0,-1.5,0);
        renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(SKIN)),light,overlay,-1);p.popPose();
    }
    /** The actual cabin and live mirror share this floorboard-to-shoulder pose. */
    public void draw(PoseStack p,MultiBufferSource out,int light,int overlay,float rise){
        float q=Mth.clamp(rise,0,1);q=q*q*(3-2*q);pose(q);p.pushPose();carPose(p,rise);
        renderToBuffer(p,out.getBuffer(RenderType.entityCutoutNoCull(SKIN)),light,overlay,-1);p.popPose();
    }
}
