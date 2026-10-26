package io.github.knaitoe.theoldesthouse.client;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Vector3f;

/** Production native-pixel geometry, also used by the action close-ups and room cutaways. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class VignetteHandlingRenderer implements BlockEntityRenderer<VignetteHandlingBlockEntity> {
 public static final ResourceLocation ATLAS=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/handling/atlas.png");
 public VignetteHandlingRenderer(BlockEntityRendererProvider.Context c){}
 @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(LiteraryRegistry.HANDLING_ENTITY.get(),VignetteHandlingRenderer::new);}
 @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{VignetteHandlingBlockEntity.clientAudio=VignetteHandlingAudio::tick;VignetteHandlingBlockEntity.clientStop=VignetteHandlingAudio::stop;});}
 @Override public void render(VignetteHandlingBlockEntity be,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){draw(be.getBlockState(),be.view(),partial,p,buffers,light,overlay);}
 public static void draw(BlockState s,VignetteHandlingGeometry.View view,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
  p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-90*VignetteHandlingBlock.turns(s.getValue(VignetteHandlingBlock.FACING))));p.translate(-.5,0,-.5);
  var out=buffers.getBuffer(RenderType.entityCutoutNoCull(ATLAS));
  for(var b:VignetteHandlingGeometry.parts(s.getValue(VignetteHandlingBlock.KIND),view,partial)){
   p.pushPose();double cx=(b.x0()+b.x1())/32,cz=(b.z0()+b.z1())/32;
   if(b.yaw()!=0){p.translate(cx,0,cz);p.mulPose(Axis.YP.rotationDegrees(b.yaw()));p.translate(-cx,0,-cz);}
   box(p,out,b,b.lit()?LightTexture.FULL_BRIGHT:light,overlay);p.popPose();
  }p.popPose();
 }
 private static void box(PoseStack p,VertexConsumer out,VignetteHandlingGeometry.Part b,int light,int overlay){
  float x0=(float)b.x0()/16,y0=(float)b.y0()/16,z0=(float)b.z0()/16,x1=(float)b.x1()/16,y1=(float)b.y1()/16,z1=(float)b.z1()/16;
  face(p,out,b,light,overlay,.8F,0,0,-1,new float[][]{{x1,y0,z0,1-x1,1-y0},{x0,y0,z0,1-x0,1-y0},{x0,y1,z0,1-x0,1-y1},{x1,y1,z0,1-x1,1-y1}});
  face(p,out,b,light,overlay,.8F,0,0,1,new float[][]{{x0,y0,z1,x0,1-y0},{x1,y0,z1,x1,1-y0},{x1,y1,z1,x1,1-y1},{x0,y1,z1,x0,1-y1}});
  face(p,out,b,light,overlay,.6F,-1,0,0,new float[][]{{x0,y0,z0,z0,1-y0},{x0,y0,z1,z1,1-y0},{x0,y1,z1,z1,1-y1},{x0,y1,z0,z0,1-y1}});
  face(p,out,b,light,overlay,.6F,1,0,0,new float[][]{{x1,y0,z1,1-z1,1-y0},{x1,y0,z0,1-z0,1-y0},{x1,y1,z0,1-z0,1-y1},{x1,y1,z1,1-z1,1-y1}});
  float[][] top={{x0,y1,z0,x0,z0},{x0,y1,z1,x0,z1},{x1,y1,z1,x1,z1},{x1,y1,z0,x1,z0}};
  if(b.picture()){top[0][3]=1;top[0][4]=1;top[1][3]=1;top[1][4]=0;top[2][3]=0;top[2][4]=0;top[3][3]=0;top[3][4]=1;}
  face(p,out,b,light,overlay,1,0,1,0,top);
  face(p,out,b,light,overlay,.5F,0,-1,0,new float[][]{{x0,y0,z1,x0,z1},{x0,y0,z0,x0,z0},{x1,y0,z0,x1,z0},{x1,y0,z1,x1,z1}});
 }
 private static void face(PoseStack p,VertexConsumer out,VignetteHandlingGeometry.Part b,int light,int overlay,float shade,float nx,float ny,float nz,float[][] vertices){
  var normal=new Vector3f(nx,ny,nz).mul(p.last().normal()).normalize();int c=(int)(shade*255);float u=b.material()%8/8F,v=b.material()/8/4F;
  for(var a:vertices)out.addVertex(p.last().pose(),a[0],a[1],a[2]).setColor(c,c,c,255).setUv(u+a[3]/8F,v+a[4]/4F).setOverlay(overlay).setLight(light).setNormal(normal.x(),normal.y(),normal.z());
 }
}
