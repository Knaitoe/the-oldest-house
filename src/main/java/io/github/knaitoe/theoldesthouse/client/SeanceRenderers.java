package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Native custom meshes: veil, dress folds, cuffs, lap poses and a small daughter. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SeanceRenderers {
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"seance_family"),"main");
    public static ResourceLocation skin(int role){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/seance_"+new String[]{"medium","mother","father","daughter"}[Math.max(0,Math.min(3,role))]+".png");}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(LAYER,FamilyModel::layer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(ClassicsRegistry.ACTOR.get(),FamilyRenderer::new);}
    public static final class FamilyModel extends HumanoidModel<SeanceActor> {
        private final ModelPart veil,skirt,collar;
        public final ModelPart leftKnee,rightKnee;
        public FamilyModel(ModelPart root){super(root);veil=head.getChild("veil");skirt=body.getChild("skirt");collar=body.getChild("collar");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");}
        public static LayerDefinition layer(){var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var root=mesh.getRoot();
            for(boolean left:new boolean[]{false,true}){var leg=root.addOrReplaceChild(left?"left_leg":"right_leg",SegmentedSkin.limb(0,16,0,-2,0,-2,4,6,4,left,false),PartPose.offset(left?1.9F:-1.9F,12,0));leg.addOrReplaceChild("knee",SegmentedSkin.limb(0,16,6,-2,0,-2,4,6,4,left,false),PartPose.offset(0,6,0));}
            root.getChild("head").addOrReplaceChild("veil",CubeListBuilder.create().texOffs(64,0).addBox(-4.5F,-8.5F,-4.4F,9,9,1).texOffs(64,12).addBox(-4.4F,-7.5F,3.2F,8.8F,10,1),PartPose.ZERO);
            root.getChild("body").addOrReplaceChild("skirt",CubeListBuilder.create().texOffs(64,28).addBox(-4.5F,8,-2.6F,9,7,5.2F).texOffs(64,42).addBox(-5.5F,15,-3.2F,11,7,6.4F),PartPose.ZERO);
            root.getChild("body").addOrReplaceChild("collar",CubeListBuilder.create().texOffs(96,0).addBox(-3.8F,0,-2.6F,7.6F,2,1.2F),PartPose.ZERO);
            root.getChild("right_arm").addOrReplaceChild("cuff",CubeListBuilder.create().texOffs(96,8).addBox(-3.3F,7,-2.3F,4.6F,2,4.6F),PartPose.ZERO);
            root.getChild("left_arm").addOrReplaceChild("cuff",CubeListBuilder.create().texOffs(96,8).addBox(-1.3F,7,-2.3F,4.6F,2,4.6F),PartPose.ZERO);return LayerDefinition.create(mesh,128,64);}
        public void pose(int role,boolean seated,boolean scared,float time){
            leftKnee.resetPose();rightKnee.resetPose();
            veil.visible=role==0;skirt.visible=role!=2;collar.visible=true;
            if(seated){leftLeg.xRot=rightLeg.xRot=-(float)Math.PI/2;leftKnee.xRot=rightKnee.xRot=(float)Math.PI/2;leftKnee.yScale=rightKnee.yScale=4F/3;rightLeg.yRot=.12F;leftLeg.yRot=-.12F;leftArm.xRot=rightArm.xRot=-.72F;leftArm.zRot=-.12F;rightArm.zRot=.12F;skirt.xRot=-.4F;}
            else if(scared){leftArm.xRot=-1.1F;rightArm.xRot=-.9F;leftArm.zRot=-.35F;rightArm.zRot=.35F;head.xRot=.14F+(float)Math.sin(time*.35)*.04F;}
            else {leftArm.xRot=rightArm.xRot=-.62F;leftArm.zRot=-.1F;rightArm.zRot=.1F;}
            // Standing at the table, the medium leans in with both hands flat on the wood; the others rest theirs on it.
            if(role==0&&!scared){head.xRot=.22F;leftArm.xRot=rightArm.xRot=-.95F;leftArm.yRot=.3F;rightArm.yRot=-.3F;}
        }
        @Override public void setupAnim(SeanceActor actor,float walk,float amount,float time,float yaw,float pitch){HumanoidMotion.reset(this);super.setupAnim(actor,walk,amount,time,yaw,pitch);pose(actor.role(),actor.seated(),actor.afraid(),time);
            if(actor.role()==3){var motion=HumanoidMotion.sample(actor,time-actor.tickCount);if(!actor.seated())HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());body.yRot+=.025F*(float)Math.sin(time*.04F+actor.getId());HumanoidMotion.connect(this);}}
    }
    private static final class FamilyRenderer extends MobRenderer<SeanceActor,FamilyModel> {
        FamilyRenderer(EntityRendererProvider.Context context){super(context,new FamilyModel(context.bakeLayer(LAYER)),.2F);}
        @Override protected void scale(SeanceActor actor,PoseStack poses,float partial){if(actor.role()==3)poses.scale(.72F,.72F,.72F);}
        @Override public ResourceLocation getTextureLocation(SeanceActor actor){return skin(actor.role());}
        /** Seated, the hips rest on the seat rather than the feet: the model sits down by its leg length. */
        @Override public net.minecraft.world.phys.Vec3 getRenderOffset(SeanceActor actor,float partial){
            if(!actor.seated())return super.getRenderOffset(actor,partial);
            var at=actor.blockPosition();var state=actor.level().getBlockState(at);double seat=.5;
            if(state.getBlock() instanceof io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock)
                seat=state.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND).seatHeight/16D;
            double scale=actor.role()==3?.72:1;
            return new net.minecraft.world.phys.Vec3(0,at.getY()+seat-actor.getY()-.751*scale,0);
        }
        @Override protected boolean shouldShowName(SeanceActor actor){return false;}
        @Override public boolean shouldRender(SeanceActor actor,Frustum frustum,double x,double y,double z){return !SeanceView.emptyRoom()&&super.shouldRender(actor,frustum,x,y,z);}
    }
}
