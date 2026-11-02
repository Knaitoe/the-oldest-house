package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The Drisking cast on their own skins, and the machine house's fan drawn from explicit per-face islands of the pixel atlas. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BorrascaNativeArt {
    public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_cast_pixels.png");
    public static final ModelLayerLocation CAST=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_cast"),"main");
    private BorrascaNativeArt(){}
    private static final String[] PEOPLE={"kyle","kimber","sheriff","prescott","worker","worker_two"};
    /** Shared cast painter: six 256-pixel exports on the original 64x64 humanoid UV layout; 4 and 5 are the two plant workers (0.4.262: no longer one face twice). */
    public static ResourceLocation skin(int person){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_"+PEOPLE[Math.max(0,Math.min(PEOPLE.length-1,person))]+".png");}
    public static ResourceLocation skin(int person,boolean worker){return skin(worker?4:Math.min(3,person));}
    /** Which painting an actor wears: a worker's phase says which of the two men he is. */
    public static int person(LiteraryActor a){return a.phase()>=BorrascaStory.WORKER?(a.phase()==BorrascaStory.WORKER+1?5:4):Math.max(0,Math.min(3,a.role()-LiteraryActor.BORRASCA_KYLE));}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,BorrascaNativeArt::castLayer);}
    private static LayerDefinition castLayer(){
        var mesh=HumanoidModel.createMesh(CubeDeformation.NONE,0);
        // Head accessories inherit the actual head transform and the standard hat UV island.
        mesh.getRoot().getChild("head").addOrReplaceChild("painted_hat",CubeListBuilder.create().texOffs(32,0).addBox(-4,-8,-4,8,8,8,new CubeDeformation(.5F)),PartPose.ZERO);
        return LayerDefinition.create(mesh,64,64);
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BorrascaRegistry.MACHINE_ENTITY.get(),MachineRenderer::new);e.registerBlockEntityRenderer(BorrascaRegistry.CARVING_ENTITY.get(),BorrascaCarvingRenderer::new);}
    public static final class CastRenderer extends MobRenderer<LiteraryActor,CastModel>{public CastRenderer(EntityRendererProvider.Context c){super(c,new CastModel(c.bakeLayer(CAST)),.25F);addLayer(new ItemInHandLayer<>(this,c.getItemInHandRenderer()));}@Override public ResourceLocation getTextureLocation(LiteraryActor a){return skin(person(a));}@Override protected boolean shouldShowName(LiteraryActor a){return a.isCustomNameVisible()&&!a.isInvisible()&&super.shouldShowName(a);}}
    public static final class CastModel extends HumanoidModel<LiteraryActor>{public CastModel(ModelPart root){super(root);hat.visible=false;}
        /**
         * 0.4.262 (owner: the workers "need proper walking animations"). The pose is eased between gestures as before, but the stride is laid
         * on afterwards, so the easing never damps a step: before, it smoothed the leg swing away to a shuffle and the cadence was twice a
         * walk's. Now a full stride takes about two blocks, the legs swing their whole way, and an arm a gesture is not using swings opposite
         * its leg.
         */
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){HumanoidMotion.reset(this);young=false;super.setupAnim(a,walk,amount,age,yaw,pitch);var motion=HumanoidMotion.sample(a,age-a.tickCount);body.xRot+=.015F*(float)Math.sin(age*.055F+a.getId());
            pose(a.gesture(),a.role()==LiteraryActor.BORRASCA_SHERIFF,age);HumanoidMotion.connect(this);RecentActorMotion.blend(a,age,this);stride(a.gesture(),motion.phase()*.45F,motion.amount());}
        /** Legs and free arms swinging with the distance actually walked; a sheltering crouch keeps its legs. */
        void stride(int gesture,float phase,float amount){
            if(amount<=.01F)return;float c=net.minecraft.util.Mth.cos(phase),swing=.72F*amount;
            if(gesture!=5){rightLeg.xRot+=c*swing;leftLeg.xRot-=c*swing;}
            boolean right=gesture==0||gesture==8,left=gesture!=3&&gesture!=5&&gesture!=8;
            if(right)rightArm.xRot-=c*.5F*amount;if(left)leftArm.xRot+=c*.5F*amount;
            float bob=-Math.abs(net.minecraft.util.Mth.sin(phase))*.22F*amount;for(var part:new ModelPart[]{head,body,leftArm,rightArm,leftLeg,rightLeg})part.y+=bob;
        }
        private void pose(int gesture,boolean sheriff,float age){
            switch(gesture){
                case 1->{rightArm.xRot=-2.1F;rightArm.zRot=-.25F;head.xRot+=.15F;}
                case 2->{rightArm.xRot=-1.4F;rightArm.yRot=-.25F;}
                case 3->{body.yRot=-.15F;head.xRot=.5F;leftArm.xRot=-1.1F;rightArm.xRot=-1.3F+.18F*(float)Math.sin(age*.5F);}
                case 4->{rightArm.xRot=sheriff?-1.35F:-1.1F;rightArm.zRot=-.1F;}
                case 5->{body.xRot=.42F;leftLeg.xRot=-.3F;rightLeg.xRot=-.3F;leftArm.xRot=-.35F;rightArm.xRot=-.35F;head.xRot=-.1F;}
                // 0.4.235: led by the arm, the right arm drawn forward and up, the head turned back over the shoulder.
                case 6->{rightArm.xRot=-1.25F;rightArm.zRot=.15F;leftArm.xRot=.35F;head.yRot=2.1F;head.xRot=-.1F;body.xRot=.08F;}
                // A taker's hand held out, low, the other arm at his side.
                case 7->{rightArm.xRot=-1.05F;rightArm.zRot=-.05F;body.xRot=.06F;head.xRot=.25F;}
                // 0.4.262: walking her by the arm, his hand on her upper arm: she is on his left (8) or his right (9); his head turned to her.
                case 8->{leftArm.xRot=-.62F;leftArm.zRot=-.3F;body.yRot=.1F;head.yRot=.35F;head.xRot=.12F;}
                case 9->{rightArm.xRot=-.62F;rightArm.zRot=.3F;body.yRot=-.1F;head.yRot=-.35F;head.xRot=.12F;}
                default->{}
            }}
        /** Keep the approved child review poses; adults use the same full-length body as their live renderer. */
        public void preview(int person,float age,float stride){HumanoidMotion.reset(this);young=person<2;crouching=false;riding=false;attackTime=0;HumanoidMotion.gait(this,age*.22F,stride,false);head.yRot=.22F*(float)Math.sin(age*.02F);HumanoidMotion.connect(this);}
        public void previewPose(int person,int gesture,float age){preview(person,age,0);pose(gesture,person==2,age);HumanoidMotion.connect(this);}
    }
    public static void cube(PoseStack p,VertexConsumer out,float x0,float y0,float z0,float x1,float y1,float z1,int tile,int light,int overlay,int colour){
        quad(p,out,new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}},tile,0,0,-1,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}},tile,0,0,1,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}},tile,-1,0,0,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}},tile,1,0,0,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y0,z1},{x1,y0,z1},{x1,y0,z0},{x0,y0,z0}},tile,0,-1,0,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}},tile,0,1,0,light,overlay,colour);
    }
    private static void quad(PoseStack p,VertexConsumer out,float[][] q,int tile,float nx,float ny,float nz,int light,int overlay,int colour){boolean hair=tile>=16;if(hair)tile-=16;float u0=(tile%4+.006F)/4,v0=(tile/4+.006F)/4,u1=(tile%4+.994F)/4,v1=(tile/4+(hair?.20F:.994F))/4;float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};for(int i=0;i<4;i++)out.addVertex(p.last(),q[i][0]/16,q[i][1]/16,q[i][2]/16).setColor(colour).setUv(uv[i][0],uv[i][1]).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);}
    public static final class MachineRenderer implements BlockEntityRenderer<BorrascaMachineBlock.Machine>{public MachineRenderer(BlockEntityRendererProvider.Context ignored){}
        @Override public void render(BorrascaMachineBlock.Machine e,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){drawMachine(p,buffers,light,overlay,e.angle(partial));}
        @Override public boolean shouldRenderOffScreen(BorrascaMachineBlock.Machine e){return true;}
    }
    public static void drawMachine(PoseStack p,MultiBufferSource buffers,int light,int overlay,float angle){var out=buffers.getBuffer(RenderType.entityCutoutNoCull(ATLAS));p.pushPose();p.translate(.5,.7,.5);p.mulPose(Axis.XP.rotation(angle));for(int i=0;i<8;i++){p.pushPose();p.mulPose(Axis.XP.rotation((float)(i*Math.PI/4)));cube(p,out,-11,-7,-2,11,-5,2,13,light,overlay,0xffffffff);p.popPose();}cube(p,out,-13,-2,-2,13,2,2,14,light,overlay,0xffffffff);p.popPose();}
}
