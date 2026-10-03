package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
/** Native tailored coat, apron, skirt, instrumental hands and sixteen articulated hose segments. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class HotelRenderers {
    public static final ModelLayerLocation CAST=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hotel_cast"),"main"),HOSE=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"hotel_hose"),"main");
    public static ResourceLocation skin(int role){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/hotel_"+new String[]{"porter","bartender","scarred_guest","pianist","dancer","dancer_dress"}[Math.max(0,Math.min(5,role))]+".png");}
    public static ResourceLocation rubber(){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/hotel_hose.png");}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,CastModel::layer);e.registerLayerDefinition(HOSE,HoseModel::layer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(HotelRegistry.ACTOR.get(),CastRenderer::new);e.registerEntityRenderer(HotelRegistry.HOSE.get(),HoseRenderer::new);}
    public static final class CastModel extends HumanoidModel<HotelActor> {
        private final ModelPart tails,apron,skirt,cap;
        public CastModel(ModelPart r){super(r);tails=body.getChild("tails");apron=body.getChild("apron");skirt=body.getChild("skirt");cap=head.getChild("cap");}
        public static LayerDefinition layer(){var m=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=m.getRoot();
            r.getChild("body").addOrReplaceChild("tails",CubeListBuilder.create().texOffs(64,30).addBox(-4.5F,1,-2.4F,9,16,4.8F).texOffs(64,52).addBox(-4.5F,15,1.4F,4,6,1.2F).addBox(.5F,15,1.4F,4,6,1.2F),PartPose.ZERO);
            r.getChild("body").addOrReplaceChild("apron",CubeListBuilder.create().texOffs(96,0).addBox(-4.2F,3,-2.8F,8.4F,13,1),PartPose.ZERO);
            r.getChild("body").addOrReplaceChild("skirt",CubeListBuilder.create().texOffs(64,30).addBox(-5,9,-3,10,12,6),PartPose.ZERO);
            r.getChild("head").addOrReplaceChild("cap",CubeListBuilder.create().texOffs(64,0).addBox(-4.6F,-9,-4.6F,9.2F,2,9.2F),PartPose.ZERO);
            return LayerDefinition.create(m,128,64);}
        public void pose(int role,boolean dancing,float time){tails.visible=role==0||role==3||role==4;apron.visible=role==1;skirt.visible=role==5;cap.visible=role==0;
            if(role==3){leftArm.xRot=rightArm.xRot=-1.18F;leftArm.yRot=.22F;rightArm.yRot=-.22F;leftArm.zRot=(float)Math.sin(time*.25)*.08F;rightArm.zRot=(float)Math.cos(time*.25)*.08F;head.xRot=.16F;}
            if(dancing){leftArm.xRot=rightArm.xRot=-1;leftArm.zRot=-.6F;rightArm.zRot=.6F;body.yRot=(float)Math.sin(time*.045)*.12F;leftLeg.xRot=(float)Math.sin(time*.09)*.2F;rightLeg.xRot=-leftLeg.xRot;}}
        @Override public void setupAnim(HotelActor a,float walk,float amount,float time,float yaw,float pitch){head.getAllParts().forEach(ModelPart::resetPose);body.getAllParts().forEach(ModelPart::resetPose);leftArm.resetPose();rightArm.resetPose();leftLeg.resetPose();rightLeg.resetPose();super.setupAnim(a,walk,amount,time,yaw,pitch);pose(a.role(),a.dancing(),time);}
    }
    private static final class CastRenderer extends MobRenderer<HotelActor,CastModel>{CastRenderer(EntityRendererProvider.Context c){super(c,new CastModel(c.bakeLayer(CAST)),.2F);}@Override public ResourceLocation getTextureLocation(HotelActor a){return skin(a.role());}@Override protected boolean shouldShowName(HotelActor a){return false;}}
    public static final class HoseModel extends EntityModel<HotelHose>{private final ModelPart root;public HoseModel(ModelPart r){root=r;}
        public static LayerDefinition layer(){var m=new MeshDefinition();var r=m.getRoot();for(int i=0;i<16;i++)r.addOrReplaceChild("segment"+i,CubeListBuilder.create().texOffs(i==0?16:0,0).addBox(-1.9F,-1.8F,-1.6F,3.8F,3.6F,3.2F),PartPose.offset(0,22,i*2.6F-20));return LayerDefinition.create(m,32,16);}
        public void pose(float time){for(int i=0;i<16;i++){var p=root.getChild("segment"+i);p.x=(float)Math.sin(time*.13-i*.38)*2.2F;p.yRot=(float)Math.cos(time*.13-i*.38)*.16F;}}
        @Override public void setupAnim(HotelHose a,float walk,float amount,float time,float yaw,float pitch){pose(time);}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){root.render(p,out,light,overlay,color);}}
    private static final class HoseRenderer extends MobRenderer<HotelHose,HoseModel>{HoseRenderer(EntityRendererProvider.Context c){super(c,new HoseModel(c.bakeLayer(HOSE)),.15F);}@Override public ResourceLocation getTextureLocation(HotelHose e){return rubber();}}
}
