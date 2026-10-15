package io.github.knaitoe.theoldesthouse.client;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
/** Shared beat, individual gestures, connected elbows/knees/wrists and contemporary club clothes. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class NightclubRenderers {
    public static final ModelLayerLocation CAST=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"nightclub_cast"),"main");
    public static final ModelLayerLocation MALE=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"nightclub_cast_male"),"main");
    public static final ModelLayerLocation MALE_DESPAIR=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"nightclub_cast_male_despair"),"main");
    public static final int LOOKS=9;
    private static final String[] SKINS={"dancer_male","dancer_female","dancer_vest","dancer_copper","dancer_mustard","dancer_silver","dancer_brick","dancer_braids","dj"};
    public static ResourceLocation skin(int look){return skin(look,false);}
    public static ResourceLocation skin(int look,boolean despair){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/nightclub_"+SKINS[Math.floorMod(look,SKINS.length)]+(despair?"_despair":"")+".png");}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,CastModel::layer);e.registerLayerDefinition(MALE,()->CastModel.layer(80,40));e.registerLayerDefinition(MALE_DESPAIR,()->CastModel.layer(80,40));}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(NightclubRegistry.ACTOR.get(),Renderer::new);}
    @SubscribeEvent public static void setup(FMLClientSetupEvent e){e.enqueueWork(()->{
        net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(NightclubRegistry.BLOCK.get(),net.minecraft.client.renderer.RenderType.cutoutMipped());
        net.minecraft.client.renderer.item.ItemProperties.register(NightclubRegistry.GLASS.get(),ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"empty"),(stack,level,entity,seed)->NightclubGlassItem.empty(stack)?1:0);
    });}
    public static final class CastModel extends HumanoidModel<NightclubActor>{
        private final ModelPart leftElbow,rightElbow,leftKnee,rightKnee,leftWrist,rightWrist,skirt,headphones;
        public CastModel(ModelPart r){super(r);leftElbow=leftArm.getChild("elbow");rightElbow=rightArm.getChild("elbow");leftKnee=leftLeg.getChild("knee");rightKnee=rightLeg.getChild("knee");leftWrist=leftElbow.getChild("wrist");rightWrist=rightElbow.getChild("wrist");skirt=body.getChild("skirt");headphones=head.getChild("headphones");}
        public static LayerDefinition layer(){return layer(128,64);}
        public static LayerDefinition layer(int textureWidth,int textureHeight){
            var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);var r=mesh.getRoot();
            for(String side:new String[]{"left","right"}){
                boolean left=side.equals("left");float x=left?-1:-3;
                var arm=r.addOrReplaceChild(side+"_arm",CubeListBuilder.create().texOffs(40,16).mirror(left).addBox(x,-2,-2,4,7,4),PartPose.offset(left?5:-5,2,0));
                var elbow=arm.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(40,23).mirror(left).addBox(x,0,-2,4,4,4),PartPose.offset(0,5,0));
                elbow.addOrReplaceChild("wrist",CubeListBuilder.create().texOffs(40,27).mirror(left).addBox(x,0,-2,4,1,4),PartPose.offset(0,4,0));
                var leg=r.addOrReplaceChild(side+"_leg",CubeListBuilder.create().texOffs(0,16).mirror(left).addBox(-2,0,-2,4,6,4),PartPose.offset(left?1.9F:-1.9F,12,0));
                leg.addOrReplaceChild("knee",CubeListBuilder.create().texOffs(0,22).mirror(left).addBox(-2,0,-2,4,6,4),PartPose.offset(0,6,0));
            }
            r.getChild("body").addOrReplaceChild("skirt",CubeListBuilder.create().texOffs(64,41).addBox(-4.5F,10,-2.5F,9,6,5),PartPose.ZERO);
            r.getChild("head").addOrReplaceChild("headphones",CubeListBuilder.create().texOffs(96,0).addBox(-5,-5,-1.5F,1,3,3).addBox(4,-5,-1.5F,1,3,3).addBox(-4.5F,-8.5F,-.5F,9,1,1),PartPose.ZERO);
            return LayerDefinition.create(mesh,textureWidth,textureHeight);
        }
        public void preview(int slot,float time){preview(slot,time,0);}
        public void preview(int slot,float time,float despair){for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);young=false;crouching=false;riding=false;pose(slot,time);morph(despair,time);}
        private void morph(float despair,float time){
            if(despair<=0)return;
            float lean=.5F*despair;body.xRot=lean;body.y+=12*(1-(float)Math.cos(lean));body.z-=12*(float)Math.sin(lean);
            head.y=body.y;head.z=body.z;head.yScale=1+.10F*despair;head.xScale=1-.035F*despair;
            head.xRot=.58F*despair;head.zRot=(float)Math.sin(time*.09F)*.1F*despair;
            for(var arm:new ModelPart[]{leftArm,rightArm}){arm.y=body.y+2*(float)Math.cos(lean);arm.z=body.z+2*(float)Math.sin(lean);arm.xRot=net.minecraft.util.Mth.lerp(despair,arm.xRot,.08F);}
            leftArm.zRot=net.minecraft.util.Mth.lerp(despair,leftArm.zRot,-.04F);rightArm.zRot=net.minecraft.util.Mth.lerp(despair,rightArm.zRot,.04F);
            leftElbow.xRot=net.minecraft.util.Mth.lerp(despair,leftElbow.xRot,-.08F);rightElbow.xRot=net.minecraft.util.Mth.lerp(despair,rightElbow.xRot,-.08F);
            leftWrist.xRot=despair*.23F;rightWrist.xRot=despair*.23F;hat.copyFrom(head);
        }
        private void pose(int slot,float time){
            int look=NightclubActor.look(slot);skirt.visible=look==1||look==7;head.visible=hat.visible=look!=8;headphones.visible=false;
            float beat=(float)Math.sin(time*Math.PI/5),sway=(float)Math.sin(time*Math.PI/20+slot*1.73);
            for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y-=Math.abs(beat)*.45F;
            head.xRot+=beat*.075F;
            if(slot==Nightclub.DANCERS){
                body.xRot=.13F;head.xRot+=.14F;leftArm.xRot=-.75F;rightArm.xRot=-.88F;leftElbow.xRot=-.55F;rightElbow.xRot=-.65F;
                leftWrist.xRot=(float)Math.sin(time*.18F)*.17F;rightWrist.yRot=(float)Math.sin(time*.14F)*.22F;
            }else{
                body.yRot=sway*.08F;body.zRot=sway*.075F;leftLeg.xRot=beat*.18F;rightLeg.xRot=-beat*.18F;
                leftKnee.xRot=Math.max(0,-beat)*.4F;rightKnee.xRot=Math.max(0,beat)*.4F;leftLeg.zRot=sway*.035F;rightLeg.zRot=sway*.035F;
                switch(slot%3){
                    case 0->{leftArm.xRot=-.35F+beat*.22F;rightArm.xRot=-.35F-beat*.22F;leftElbow.xRot=-.8F;rightElbow.xRot=-.9F;leftArm.zRot=-.15F;rightArm.zRot=.15F;}
                    case 1->{leftArm.xRot=-2.45F+sway*.16F;rightArm.xRot=-2.4F-sway*.16F;leftArm.zRot=-.32F;rightArm.zRot=.32F;leftElbow.xRot=-.35F-beat*.18F;rightElbow.xRot=-.35F+beat*.18F;}
                    default->{leftArm.xRot=-.7F;rightArm.xRot=-1.25F;leftArm.zRot=-.28F;rightArm.zRot=.3F;leftElbow.xRot=-.75F-sway*.2F;rightElbow.xRot=-.45F+sway*.2F;}
                }
                leftWrist.zRot=sway*.12F;rightWrist.zRot=-sway*.12F;skirt.xRot=sway*.035F;
            }hat.copyFrom(head);
        }
        @Override public void setupAnim(NightclubActor a,float walk,float amount,float age,float yaw,float pitch){
            for(var part:new ModelPart[]{head,hat,body,leftArm,rightArm,leftLeg,rightLeg})part.getAllParts().forEach(ModelPart::resetPose);young=false;crouching=false;riding=false;
            head.yRot=yaw*(float)Math.PI/180;head.xRot=pitch*(float)Math.PI/180;
            float time=(a.level().getGameTime()%24000)+(age-a.tickCount);pose(a.slot(),time);morph(a.despair(age-a.tickCount),time);
        }
    }
    /** Dedicated painted face coordinates; the chin is no longer sampled from the torso UV net. */
    public static void renderDjHead(CastModel model,PoseStack p,MultiBufferSource out,int light){
        p.pushPose();model.head.translateAndRotate(p);var v=out.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/nightclub_dj_head0490.png")));
        face(v,p,light,new float[][]{{-.25F,0,-.25F},{.25F,0,-.25F},{.25F,-.5F,-.25F},{-.25F,-.5F,-.25F}},218,143,382,360,0,0,-1);
        face(v,p,light,new float[][]{{-.25F,0,.25F},{-.25F,0,-.25F},{-.25F,-.5F,-.25F},{-.25F,-.5F,.25F}},14,143,215,303,-1,0,0);
        face(v,p,light,new float[][]{{.25F,0,-.25F},{.25F,0,.25F},{.25F,-.5F,.25F},{.25F,-.5F,-.25F}},385,143,554,303,1,0,0);
        face(v,p,light,new float[][]{{.25F,0,.25F},{-.25F,0,.25F},{-.25F,-.5F,.25F},{.25F,-.5F,.25F}},221,0,392,140,0,0,1);
        face(v,p,light,new float[][]{{-.25F,-.5F,-.25F},{.25F,-.5F,-.25F},{.25F,-.5F,.25F},{-.25F,-.5F,.25F}},221,0,392,72,0,-1,0);
        face(v,p,light,new float[][]{{-.25F,0,.25F},{.25F,0,.25F},{.25F,0,-.25F},{-.25F,0,-.25F}},415,308,505,350,0,1,0);
        model.headphones.visible=true;model.headphones.render(p,out.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png"))),light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);model.headphones.visible=false;p.popPose();
    }
    private static void face(com.mojang.blaze3d.vertex.VertexConsumer v,PoseStack p,int light,float[][] points,float x0,float y0,float x1,float y1,float nx,float ny,float nz){float[][] uv={{x0,y1},{x1,y1},{x1,y0},{x0,y0}};for(int i=0;i<4;i++)v.addVertex(p.last(),points[i][0],points[i][1],points[i][2]).setColor(-1).setUv(uv[i][0]/1774,uv[i][1]/887).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p.last(),nx,ny,nz);}
    private static final class Renderer extends MobRenderer<NightclubActor,CastModel>{
        private final CastModel male,maleDespair,female;
        Renderer(EntityRendererProvider.Context c){super(c,new CastModel(c.bakeLayer(MALE)),.2F);male=model;maleDespair=new CastModel(c.bakeLayer(MALE_DESPAIR));female=new CastModel(c.bakeLayer(CAST));addLayer(new net.minecraft.client.renderer.entity.layers.RenderLayer<NightclubActor,CastModel>(this){@Override public void render(PoseStack p,MultiBufferSource out,int light,NightclubActor a,float walk,float amount,float partial,float age,float yaw,float pitch){if(a.dj())renderDjHead(getParentModel(),p,out,light);}});}
        @Override public void render(NightclubActor a,float yaw,float partial,PoseStack p,MultiBufferSource out,int light){model=a.look()==0?(a.despair(partial)>=.4F?maleDespair:male):female;super.render(a,yaw,partial,p,out,light);}
        @Override public ResourceLocation getTextureLocation(NightclubActor a){return skin(a.look(),a.despair(1)>=.4F);}
        @Override protected boolean shouldShowName(NightclubActor a){return false;}
    }
}
