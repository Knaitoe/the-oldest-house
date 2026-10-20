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
    public static ResourceLocation skin(int role){if(role==6)return HotelBathRenderer.skin();return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/stanley_"+new String[]{"porter","bartender","scarred_guest","pianist","dancer","dancer_dress","bath_ghost"}[Math.max(0,Math.min(6,role))]+".png");}
    public static ResourceLocation rubber(){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/hotel_hose.png");}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,CastModel::layer);e.registerLayerDefinition(HotelBathRenderer.LAYER,HotelBathRenderer.Model::layer);e.registerLayerDefinition(HOSE,HoseModel::layer);}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(HotelRegistry.ACTOR.get(),CastRenderer::new);e.registerEntityRenderer(HotelRegistry.HOSE.get(),HoseRenderer::new);e.registerBlockEntityRenderer(HotelRegistry.PHOTO_ENTITY.get(),HotelPhotoRenderer::new);}
    public static final class CastModel extends HumanoidModel<HotelActor> {
        private final ModelPart tails,apron,skirt,cap,leftElbow,rightElbow,leftKnee,rightKnee,glass,bottleGrip,bottleAim,bottle,cloth;
        private final ModelPart[] leftFingers=new ModelPart[3],rightFingers=new ModelPart[3];
        public CastModel(ModelPart r){super(r);tails=body.getChild("tails");apron=body.getChild("apron");skirt=body.getChild("skirt");cap=head.getChild("cap");leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");glass=leftElbow.getChild("glass");bottleGrip=rightElbow.getChild("bottle_grip");bottleAim=bottleGrip.getChild("bottle_aim");bottle=bottleAim.getChild("bottle");cloth=rightElbow.getChild("cloth");for(int i=0;i<3;i++){leftFingers[i]=leftElbow.getChild("finger"+i);rightFingers[i]=rightElbow.getChild("finger"+i);}}
        public static LayerDefinition layer(){var m=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=m.getRoot();
            for(boolean left:new boolean[]{false,true}){
                var arm=r.addOrReplaceChild(left?"left_arm":"right_arm",SegmentedSkin.limb(40,16,0,left?-1:-3,-2,-2,4,6,4,left,true),PartPose.offset(left?5:-5,2,0));
                var elbow=arm.addOrReplaceChild("elbow",SegmentedSkin.limb(40,16,6,left?-1:-3,0,-2,4,6,4,left,true),PartPose.offset(0,4,0));
                for(int i=0;i<3;i++)elbow.addOrReplaceChild("finger"+i,CubeListBuilder.create().texOffs(96,24).addBox(-.4F,0,-.45F,.8F,2,.9F),PartPose.offset((left?-.4F:-2.4F)+i,5.5F,-1.7F));
                var leg=r.addOrReplaceChild(left?"left_leg":"right_leg",SegmentedSkin.limb(0,16,0,-2,0,-2,4,6,4,left,false),PartPose.offset(left?1.9F:-1.9F,12,0));
                leg.addOrReplaceChild("knee",SegmentedSkin.limb(0,16,6,-2,0,-2,4,6,4,left,false),PartPose.offset(0,6,0));
            }
            // Match the painted shirt/lapels on the torso instead of covering them with the skirt material.
            r.getChild("body").addOrReplaceChild("tails",CubeListBuilder.create().texOffs(16,16).addBox(-4,0,-2,8,12,4,new CubeDeformation(.15F)).texOffs(64,52).addBox(-4.3F,11.5F,1.6F,3.8F,8,1).addBox(.5F,11.5F,1.6F,3.8F,8,1),PartPose.ZERO);
            r.getChild("body").addOrReplaceChild("apron",CubeListBuilder.create().texOffs(96,0).addBox(-4.2F,3,-2.8F,8.4F,13,1),PartPose.ZERO);
            r.getChild("body").addOrReplaceChild("skirt",CubeListBuilder.create().texOffs(64,30).addBox(-5,9,-3,10,12,6),PartPose.ZERO);
            r.getChild("head").addOrReplaceChild("cap",CubeListBuilder.create().texOffs(64,0).addBox(-4.6F,-9,-4.6F,9.2F,2,9.2F),PartPose.ZERO);
            r.getChild("left_arm").getChild("elbow").addOrReplaceChild("glass",CubeListBuilder.create().texOffs(104,24).addBox(-1,-4,-1,2,4,2).texOffs(104,34).addBox(-.3F,0,-.3F,.6F,2,.6F).texOffs(104,37).addBox(-1.4F,2,-1.4F,2.8F,.4F,2.8F),PartPose.offset(1,5,-1.5F));
            var grip=r.getChild("right_arm").getChild("elbow").addOrReplaceChild("bottle_grip",CubeListBuilder.create(),PartPose.offset(-2,5,-1.5F));
            grip.addOrReplaceChild("bottle_aim",CubeListBuilder.create(),PartPose.ZERO).addOrReplaceChild("bottle",CubeListBuilder.create().texOffs(116,24).addBox(-1.2F,-5,-1.2F,2.4F,5,2.4F).texOffs(116,33).addBox(-.6F,-7,-.6F,1.2F,2,1.2F),PartPose.ZERO);
            r.getChild("right_arm").getChild("elbow").addOrReplaceChild("cloth",CubeListBuilder.create().texOffs(100,44).addBox(-2.5F,0,-2.5F,5,.3F,5),PartPose.offset(-2,6,-1));
            return LayerDefinition.create(m,128,64);}
        private void reset(){for(var p:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})p.getAllParts().forEach(ModelPart::resetPose);young=false;crouching=false;riding=false;}
        public void pose(int role,boolean dancing,float time){pose(role,dancing,time,HotelActor.IDLE,0);}
        public void preview(int role,boolean dancing,float time,int motion,float progress){reset();pose(role,dancing,time,motion,progress);}
        private void pose(int role,boolean dancing,float time,int motion,float progress){
            tails.visible=role==0||role==3||role==4;apron.visible=role==1;skirt.visible=role==5;cap.visible=role==0;glass.visible=bottle.visible=cloth.visible=false;
            for(int i=0;i<3;i++){leftFingers[i].visible=rightFingers[i].visible=role==3;}
            float breath=(float)Math.sin(time*.035F)*.012F,beat=(float)Math.sin(time*.13F);body.xRot+=breath;
            if(role==0){rightElbow.xRot=-.2F;leftElbow.xRot=-.1F;if(motion==HotelActor.GREET){float bow=(float)Math.sin(Math.PI*progress);body.xRot+=bow*.23F;head.xRot+=bow*.2F;rightArm.xRot=-.65F*bow;rightElbow.xRot=-1.2F*bow;rightArm.zRot=-.2F*bow;}}
            if(role==1){
                leftArm.xRot=-.65F;leftElbow.xRot=-1.02F;rightArm.xRot=-.8F;rightElbow.xRot=-.85F;
                if(motion==HotelActor.POUR){float pour=(float)Math.sin(Math.PI*progress);glass.visible=bottle.visible=true;rightArm.xRot-=.3F*pour;rightArm.yRot=-.5F;rightElbow.xRot=-1.25F;leftArm.yRot=.3F;bottleGrip.xRot=-rightArm.xRot-rightElbow.xRot;bottleAim.yRot=-rightArm.yRot;bottle.zRot=.95F*pour;glass.xRot=-leftArm.xRot-leftElbow.xRot;}
                else{float polish=(float)Math.sin(time*.08F);cloth.visible=true;rightArm.yRot=polish*.2F;rightElbow.xRot=-.6F-polish*.12F;cloth.xRot=-rightArm.xRot-rightElbow.xRot;leftArm.xRot=-.12F;leftElbow.xRot=-.3F;}
                head.xRot+=.2F;
            }
            if(role==2||role==4&&!dancing){head.zRot=.11F;rightArm.xRot=-.1F;rightElbow.xRot=-.3F;if(motion==HotelActor.LISTEN){float lean=(float)Math.sin(Math.PI*progress);head.xRot+=.18F*lean;head.zRot+=.12F*lean;leftArm.xRot=-.25F*lean;leftElbow.xRot=-.8F*lean;}}
            if(role==3){leftArm.xRot=rightArm.xRot=-.84F;leftArm.yRot=.15F;rightArm.yRot=-.15F;leftElbow.xRot=-.64F-beat*.05F;rightElbow.xRot=-.64F+beat*.05F;leftArm.zRot=(float)Math.sin(time*.11F)*.045F;rightArm.zRot=(float)Math.cos(time*.11F)*.045F;head.xRot=.23F;body.xRot=.06F;for(int i=0;i<3;i++){leftFingers[i].xRot=-.2F-(float)Math.sin(time*.3F+i*1.7F)*.18F;rightFingers[i].xRot=-.2F-(float)Math.cos(time*.3F+i*1.7F)*.18F;}}
            if(dancing){boolean dress=role==5;leftArm.xRot=-.9F;rightArm.xRot=-.8F;leftArm.zRot=dress?-.37F:-.58F;rightArm.zRot=dress?.58F:.37F;leftElbow.xRot=dress?-.5F:-.8F;rightElbow.xRot=dress?-.8F:-.5F;body.yRot=(float)Math.sin(time*.04F)*.09F;head.yRot=-body.yRot;leftLeg.xRot=beat*.19F;rightLeg.xRot=-leftLeg.xRot;leftKnee.xRot=Math.max(0,-beat)*.28F;rightKnee.xRot=Math.max(0,beat)*.28F;skirt.xRot=beat*.035F;tails.xRot=-beat*.025F;}
            if(role==6){skirt.visible=true;body.y=7;body.xRot=.22F;head.y=6;head.xRot=.24F;head.zRot=.12F;leftArm.y=rightArm.y=8;leftArm.xRot=-.35F;rightArm.xRot=-.5F;leftElbow.xRot=-.6F;rightElbow.xRot=-.4F;leftLeg.xRot=rightLeg.xRot=-1.25F;leftKnee.xRot=rightKnee.xRot=1.55F;leftLeg.y=rightLeg.y=18;}
            hat.copyFrom(head);
        }
        @Override public void setupAnim(HotelActor a,float walk,float amount,float time,float yaw,float pitch){reset();super.setupAnim(a,walk,amount,time,yaw,pitch);pose(a.role(),a.dancing(),time,a.gesture(),a.gestureProgress(time-a.tickCount));}
    }
    private static final class CastRenderer extends MobRenderer<HotelActor,CastModel>{private final HotelBathRenderer bath;CastRenderer(EntityRendererProvider.Context c){super(c,new CastModel(c.bakeLayer(CAST)),.2F);bath=new HotelBathRenderer(c);}@Override public void render(HotelActor a,float yaw,float partial,PoseStack p,net.minecraft.client.renderer.MultiBufferSource buffers,int light){if(a.role()==6){bath.render(a,yaw,partial,p,buffers,light);return;}super.render(a,yaw,partial,p,buffers,light);}@Override public ResourceLocation getTextureLocation(HotelActor a){return skin(a.role());}@Override protected boolean shouldShowName(HotelActor a){return false;}}
    public static final class HoseModel extends EntityModel<HotelHose>{private final ModelPart root;public HoseModel(ModelPart r){root=r;}
        public static final int SEGMENTS=32;
        public static LayerDefinition layer(){var m=new MeshDefinition();var r=m.getRoot();for(int i=0;i<SEGMENTS;i++)r.addOrReplaceChild("segment"+i,CubeListBuilder.create().texOffs(i==0?16:0,0).addBox(-.75F,-.5F,-1.5F,1.5F,1,3),PartPose.offset(0,23.4F,0));return LayerDefinition.create(m,32,16);}
        public void pose(float time){pose(time,2);}
        public static float[] point(float time,int phase,int i){if(phase==0){float angle=i*(float)(Math.PI*4/SEGMENTS),radius=1.3F+i*.16F;return new float[]{(float)Math.sin(angle)*radius,(float)Math.cos(angle)*radius};}return new float[]{(float)Math.sin((phase==2?time*.09:0)-i*.20)*1.4F,-12+i*.75F};}
        public void pose(float time,int phase){for(int i=0;i<SEGMENTS;i++){var p=root.getChild("segment"+i);p.resetPose();var a=point(time,phase,i);var b=point(time,phase,i+1);float dx=b[0]-a[0],dz=b[1]-a[1];p.x=(a[0]+b[0])*.5F;p.z=(a[1]+b[1])*.5F;p.yRot=(float)Math.atan2(dx,dz);p.zScale=((float)Math.hypot(dx,dz)+.16F)/3;}}
        @Override public void setupAnim(HotelHose a,float walk,float amount,float time,float yaw,float pitch){pose(time,a.phase());}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){root.render(p,out,light,overlay,color);}}
    private static final class HoseRenderer extends MobRenderer<HotelHose,HoseModel>{HoseRenderer(EntityRendererProvider.Context c){super(c,new HoseModel(c.bakeLayer(HOSE)),.15F);}@Override public ResourceLocation getTextureLocation(HotelHose e){return rubber();}}
}
