package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

public final class HouseHumanRenderers {
    public static final ResourceLocation HOLLOWAY=texture("holloway"),HARRIGAN=texture("harrigan"),HARRIGAN_DEAD=texture("harrigan_dead");
    public static final ResourceLocation WITNESS=texture("finale_witness");
    public static final ModelLayerLocation HARRIGAN_LAYER=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"harrigan"),"main");
    private static ActorRenderer harrigan;
    private HouseHumanRenderers(){}
    public static boolean ready(){return harrigan!=null;}
    public static ResourceLocation mourner(int role){return texture("harrigan_mourner_"+Math.max(0,Math.min(2,role)));}
    private static ResourceLocation texture(String name){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/"+name+".png");}
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void definitions(EntityRenderersEvent.RegisterLayerDefinitions event){event.registerLayerDefinition(HARRIGAN_LAYER,HarriganModel::layer);}
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event){event.registerEntityRenderer(NovelRegistry.HUMAN.get(),HumanRenderer::new);}
        @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers event){harrigan=new ActorRenderer(event.getContext());}
    }
    public static final class HumanModel extends PlayerModel<HouseHuman> {
        public HumanModel(net.minecraft.client.model.geom.ModelPart part){super(part,false);}
        @Override public void setupAnim(HouseHuman human,float walk,float amount,float age,float yaw,float pitch){
            rightArmPose=human.hunting()?HumanoidModel.ArmPose.CROSSBOW_HOLD:HumanoidModel.ArmPose.EMPTY;
            leftArmPose=HumanoidModel.ArmPose.EMPTY;super.setupAnim(human,walk,amount,age,yaw,pitch);
        }
    }
    private static final class HumanRenderer extends MobRenderer<HouseHuman,HumanModel> {
        HumanRenderer(EntityRendererProvider.Context context){super(context,new HumanModel(context.bakeLayer(ModelLayers.PLAYER)),.3F);addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this,context.getItemInHandRenderer()));}
        @Override public ResourceLocation getTextureLocation(HouseHuman actor){return HOLLOWAY;}
        @Override protected boolean shouldShowName(HouseHuman actor){return false;}
    }
    /** Mr. Harrigan's art as a scene actor: 1 alive in his chair, 2 dead in it, 3 in his casket, 4 to 6 his mourners. */
    public static int variant(LiteraryActor actor){return actor.role()==LiteraryActor.HARRIGAN?1+Math.max(0,Math.min(2,actor.phase())):actor.role()==LiteraryActor.HARRIGAN_MOURNER?4+Math.max(0,Math.min(2,actor.phase())):0;}
    public static final class HarriganModel extends PlayerModel<LiteraryActor> {
        public final ModelPart leftElbow,rightElbow,leftKnee,rightKnee;
        private final ModelPart leftCuff,rightCuff,leftShin,rightShin;
        public HarriganModel(ModelPart part){super(part,false);leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");leftCuff=leftSleeve.getChild("elbow");rightCuff=rightSleeve.getChild("elbow");leftShin=leftPants.getChild("knee");rightShin=rightPants.getChild("knee");}
        private static void arm(PartDefinition root,String name,int u,int v,boolean left,CubeDeformation inflate){
            float x=left?-1:-3;
            var upper=root.addOrReplaceChild(name,inflate==CubeDeformation.NONE?SegmentedSkin.limb(u,v,0,x,-2,-2,4,6,4,false,true):CubeListBuilder.create().texOffs(u,v).addBox(x,-2,-2,4,6,4,inflate),PartPose.offset(left?5:-5,2,0));
            upper.addOrReplaceChild("elbow",inflate==CubeDeformation.NONE?SegmentedSkin.limb(u,v,6,x,0,-2,4,6,4,false,true):CubeListBuilder.create().texOffs(u,v+6).addBox(x,0,-2,4,6,4,inflate),PartPose.offset(0,4,0));
        }
        private static void leg(PartDefinition root,String name,int u,int v,boolean left,CubeDeformation inflate){
            var upper=root.addOrReplaceChild(name,inflate==CubeDeformation.NONE?SegmentedSkin.limb(u,v,0,-2,0,-2,4,6,4,false,false):CubeListBuilder.create().texOffs(u,v).addBox(-2,0,-2,4,6,4,inflate),PartPose.offset(left?1.9F:-1.9F,12,0));
            upper.addOrReplaceChild("knee",inflate==CubeDeformation.NONE?SegmentedSkin.limb(u,v,6,-2,0,-2,4,6,4,false,false):CubeListBuilder.create().texOffs(u,v+6).addBox(-2,0,-2,4,6,4,inflate),PartPose.offset(0,6,0));
        }
        public static LayerDefinition layer(){
            var mesh=PlayerModel.createMesh(CubeDeformation.NONE,false);var root=mesh.getRoot();var outer=new CubeDeformation(.25F);
            arm(root,"left_arm",32,48,true,CubeDeformation.NONE);arm(root,"right_arm",40,16,false,CubeDeformation.NONE);
            arm(root,"left_sleeve",48,48,true,outer);arm(root,"right_sleeve",40,32,false,outer);
            leg(root,"left_leg",16,48,true,CubeDeformation.NONE);leg(root,"right_leg",0,16,false,CubeDeformation.NONE);
            leg(root,"left_pants",0,48,true,outer);leg(root,"right_pants",0,32,false,outer);
            return LayerDefinition.create(mesh,64,64);
        }
        @Override public void setupAnim(LiteraryActor actor,float walk,float amount,float age,float yaw,float pitch){
            int role=variant(actor);preview(role);
            // Alive, his head follows whoever is nearest, and he breathes; dead, it stays where it fell.
            if(role==1){head.yRot=net.minecraft.util.Mth.clamp(yaw,-60,60)*(float)Math.PI/180;head.xRot=pitch*(float)Math.PI/180*.5F;head.y+=net.minecraft.util.Mth.sin(age*.06F)*.12F;copyClothing();}
        }
        public void preview(int role){
            for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg,jacket,leftSleeve,rightSleeve,leftPants,rightPants})part.getAllParts().forEach(ModelPart::resetPose);
            young=false;crouching=false;riding=false;
            if(role==3)resting(this);
            else if(role<3){leftLeg.xRot=rightLeg.xRot=-(float)Math.PI/2;leftKnee.xRot=rightKnee.xRot=(float)Math.PI/2;leftKnee.yScale=rightKnee.yScale=4F/3;leftArm.xRot=rightArm.xRot=role==2?-.43F:-.698F;leftElbow.xRot=rightElbow.xRot=.5F;if(role==2){head.xRot=.314F;head.zRot=.14F;body.xRot=.14F;}HumanoidMotion.connect(this);}
            // A mourner stands with the head a little bowed and the hands folded in front.
            else{head.xRot=.17F;leftArm.xRot=rightArm.xRot=-.31F;leftArm.zRot=-.16F;rightArm.zRot=.16F;leftElbow.xRot=rightElbow.xRot=-.6F;HumanoidMotion.connect(this);}
            copyClothing();
        }
        public void copyClothing(){hat.copyFrom(head);jacket.copyFrom(body);leftSleeve.copyFrom(leftArm);rightSleeve.copyFrom(rightArm);leftPants.copyFrom(leftLeg);rightPants.copyFrom(rightLeg);leftCuff.copyFrom(leftElbow);rightCuff.copyFrom(rightElbow);leftShin.copyFrom(leftKnee);rightShin.copyFrom(rightKnee);}
    }
    public static void resting(PlayerModel<?> model){
        model.head.z=-2.8F;model.head.xRot=-.025F;model.leftArm.z=model.rightArm.z=-.25F;model.leftArm.xRot=-.13F;model.rightArm.xRot=-.10F;model.leftArm.zRot=.04F;model.rightArm.zRot=-.04F;
        if(model instanceof HarriganModel fitted){fitted.leftElbow.zRot=1.12F;fitted.rightElbow.zRot=-1.12F;fitted.leftElbow.xRot=-.16F;fitted.rightElbow.xRot=-.28F;}
    }
    public static void casketPose(com.mojang.blaze3d.vertex.PoseStack p){p.translate(0,-.2125,.95);p.mulPose(Axis.YP.rotationDegrees(180));p.mulPose(Axis.XP.rotationDegrees(90));p.scale(.95F,.95F,.95F);}
    /** Draws Mr. Harrigan and his mourners for the scene actors' own renderer. */
    public static final class ActorRenderer extends LivingEntityRenderer<LiteraryActor,HarriganModel> {
        public ActorRenderer(EntityRendererProvider.Context context){super(context,new HarriganModel(context.bakeLayer(HARRIGAN_LAYER)),.3F);}
        @Override public ResourceLocation getTextureLocation(LiteraryActor actor){int role=variant(actor);return role>=4&&role<=6?mourner(role-4):role>=2?HARRIGAN_DEAD:HARRIGAN;}
        @Override protected boolean shouldShowName(LiteraryActor actor){return false;}
        @Override protected void setupRotations(LiteraryActor actor,com.mojang.blaze3d.vertex.PoseStack poses,float age,float yaw,float partial,float scale){
            super.setupRotations(actor,poses,age,yaw,partial,scale);
            if(variant(actor)==3)casketPose(poses);
        }
    }
}
