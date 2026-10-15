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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Explicit per-face material islands: no stock skin net can cross into another painted face. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BorrascaNativeArt {
    public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/entity/borrasca_atlas.png");
    public static final ModelLayerLocation CAST=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"borrasca_cast"),"main");
    private BorrascaNativeArt(){}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e){e.registerLayerDefinition(CAST,()->LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE,0),64,64));}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(BorrascaRegistry.MACHINE_ENTITY.get(),MachineRenderer::new);}
    public static final class CastRenderer extends MobRenderer<LiteraryActor,CastModel>{public CastRenderer(EntityRendererProvider.Context c){super(c,new CastModel(c.bakeLayer(CAST)),.25F);}@Override public ResourceLocation getTextureLocation(LiteraryActor a){return ATLAS;}}
    public static final class CastModel extends HumanoidModel<LiteraryActor>{private int person;public CastModel(ModelPart root){super(root);}
        @Override public void setupAnim(LiteraryActor a,float walk,float amount,float age,float yaw,float pitch){HumanoidMotion.reset(this);super.setupAnim(a,walk,amount,age,yaw,pitch);person=Math.max(0,Math.min(3,a.role()-LiteraryActor.BORRASCA_KYLE));var motion=HumanoidMotion.sample(a,age-a.tickCount);HumanoidMotion.gait(this,motion.phase(),motion.amount(),motion.running());body.xRot+=.015F*(float)Math.sin(age*.055F+a.getId());HumanoidMotion.connect(this);}
        public void preview(int person,float age,float stride){HumanoidMotion.reset(this);this.person=person;HumanoidMotion.gait(this,age*.22F,stride,false);head.yRot=.22F*(float)Math.sin(age*.02F);HumanoidMotion.connect(this);}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int colour){
            joint(head,p,()->{cube(p,out,-4,-8,-4,4,0,4,10,light,overlay,colour);cube(p,out,-4.06F,-8.08F,-3.98F,4.06F,person==1?2:-3,4.08F,16+person,light,overlay,colour);quad(p,out,new float[][]{{-4,-8,-4.09F},{4,-8,-4.09F},{4,0,-4.09F},{-4,0,-4.09F}},person,0,0,-1,light,overlay,colour);});
            joint(body,p,()->cube(p,out,-4.2F,0,-2.1F,4.2F,12,2.1F,4+person,light,overlay,colour));
            joint(rightArm,p,()->{cube(p,out,-3.05F,-2,-2,1.05F,7,2,4+person,light,overlay,colour);cube(p,out,-3,7,-2,1,10,2,10,light,overlay,colour);});
            joint(leftArm,p,()->{cube(p,out,-1.05F,-2,-2,3.05F,7,2,4+person,light,overlay,colour);cube(p,out,-1,7,-2,3,10,2,10,light,overlay,colour);});
            int trousers=person==0||person==3?8:9;for(var leg:new ModelPart[]{rightLeg,leftLeg})joint(leg,p,()->{cube(p,out,-2,0,-2,2,10,2,trousers,light,overlay,colour);cube(p,out,-2.06F,10,-2.6F,2.06F,12,2.05F,11,light,overlay,colour);});
        }
    }
    private static void joint(ModelPart part,PoseStack p,Runnable draw){p.pushPose();part.translateAndRotate(p);draw.run();p.popPose();}
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
