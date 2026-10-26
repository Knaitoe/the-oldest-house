package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import static io.github.knaitoe.theoldesthouse.client.UncannyTextures.*;

/** Low-poly physical fittings, with exactly the same native shape, pixel grid and orientation in every preview. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class UncannyPropRenderer implements BlockEntityRenderer<UncannyPropBlock.Fitting> {
    private final Font font;
    public UncannyPropRenderer(BlockEntityRendererProvider.Context context){font=context.getFont();}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(UncannyRegistry.FITTING.get(),UncannyPropRenderer::new);}
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent e){e.enqueueWork(()->{
        ItemBlockRenderTypes.setRenderLayer(UncannyRegistry.FURNITURE.get(),RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(DomesticRegistry.FITTING.get(),RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(LiteraryRegistry.PROP.get(),RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(UncannyRegistry.DINER_DOOR.get(),RenderType.cutout());
    });}
    @Override public void render(UncannyPropBlock.Fitting fitting,float partial,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        draw(fitting.getBlockState(),p,buffers,light,overlay);
        var kind=fitting.getBlockState().getValue(UncannyPropBlock.KIND);
        if(kind==UncannyPropBlock.Kind.MONITOR)UncannyCameras.monitor(p,buffers,light);
        if(kind==UncannyPropBlock.Kind.SIGN||kind==UncannyPropBlock.Kind.SIGNAL){
            var line=kind==UncannyPropBlock.Kind.SIGNAL?(UncannyClient.green()?"||  GREEN":"—  RED"):UncannyClient.sign();
            if(line.isEmpty())line="BLUE LIGHT";
            p.pushPose();p.translate(.5,.58,.63);p.mulPose(Axis.YP.rotationDegrees(-fitting.getBlockState().getValue(UncannyPropBlock.FACING).toYRot()));p.scale(.008F,-.008F,.008F);
            font.drawInBatch(line,-font.width(line)*.5F,0,0xffe2dfc1,false,p.last().pose(),buffers,Font.DisplayMode.NORMAL,0,LightTexture.FULL_BRIGHT);p.popPose();
        }
    }
    public static void draw(BlockState state,PoseStack p,MultiBufferSource buffers,int light,int overlay){
        if(state.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FLESH){Minecraft.getInstance().getBlockRenderer().renderSingleBlock(UncannyRegistry.FLESH.get().defaultBlockState(),p,buffers,light,overlay);return;}
        var out=buffers.getBuffer(RenderType.entityCutoutNoCull(UncannyTextures.atlas()));p.pushPose();p.translate(.5,0,.5);p.mulPose(Axis.YP.rotationDegrees(-state.getValue(UncannyPropBlock.FACING).toYRot()));p.translate(-.5,0,-.5);
        switch(state.getValue(UncannyPropBlock.KIND)){
            case FLESH->{box(p,out,0,0,0,16,16,16,FLESH,light,overlay);box(p,out,1,5,0,15,7,1,FLESH,light,overlay);}
            case PAINTED_BOARD->box(p,out,0,0,6,16,16,10,PAINT,light,overlay);
            case FALSE_DOOR->{if(state.getValue(UncannyPropBlock.STAGE)>=2){box(p,out,0,0,6,16,16,10,BLACK,light,overlay);p.translate(.05,0,.5);p.mulPose(Axis.YP.rotationDegrees(90));p.translate(-.05,0,-.5);}box(p,out,0,0,6,16,16,10,WOOD,light,overlay);box(p,out,3,2,10,13,14,10.25F,WOOD,light,overlay);if(UncannyClient.mirror())box(p,out,0,0,10.3F,1,16,10.5F,FROST,LightTexture.FULL_BRIGHT,overlay);}
            case PAPER,RECEIPT,CLIPBOARD,LEDGER,CLEAVER->{box(p,out,1,0,2,15,.5F,14,state.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.CLEAVER?METAL:PAPER,light,overlay);if(state.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.LEDGER)box(p,out,1,0,2,3,1,14,WOOD,light,overlay);}
            case SCRAWL->{box(p,out,2,3,6,14,15,6.4F,PAPER,light,overlay);}
            case MONITOR->{box(p,out,1,0,2,15,13,14,BLACK,light,overlay);box(p,out,2,2,13.9F,14,11,14.1F,CRT,LightTexture.FULL_BRIGHT,overlay);}
            case CAMERA->{box(p,out,4,8,6,12,13,12,METAL,light,overlay);box(p,out,6,9,12,10,12,14,BLACK,light,overlay);}
            case SIGNAL->{box(p,out,2,3,6,14,15,10,BLACK,light,overlay);box(p,out,5,6,10,11,12,10.2F,UncannyClient.green()?GREEN:RED,LightTexture.FULL_BRIGHT,overlay);}
            case STOPLIGHT->{
                boolean green=UncannyClient.room().equals(LabyrinthPlace.TUNNEL_PERIPHERY.id())?UncannyClient.green():state.getValue(UncannyPropBlock.STAGE)==1;
                box(p,out,4,0,6,12,16,10,METAL,light,overlay);box(p,out,5,1,10,11,15,10.5F,BLACK,light,overlay);
                box(p,out,5.5F,10.5F,10.5F,10.5F,14,11,green?BLACK:RED,LightTexture.FULL_BRIGHT,overlay);
                box(p,out,5.5F,6.5F,10.5F,10.5F,9.5F,11,BLACK,light,overlay);
                box(p,out,5.5F,2,10.5F,10.5F,5.5F,11,green?GREEN:BLACK,LightTexture.FULL_BRIGHT,overlay);
                for(float y:new float[]{5.5F,9.5F,14})box(p,out,4.5F,y,10.5F,11.5F,y+.5F,13,BLACK,light,overlay);
            }
            case SIGN->{box(p,out,0,4,6,16,14,10,METAL,light,overlay);box(p,out,1,5,10,15,13,10.2F,BLACK,light,overlay);}
            case FLOODLIGHT->{box(p,out,6,9,7,10,10,16,METAL,light,overlay);box(p,out,2,7,6,14,15,10,METAL,light,overlay);box(p,out,3,8,10,13,14,10.2F,state.getValue(UncannyPropBlock.STAGE)==0?BONE:BLACK,state.getValue(UncannyPropBlock.STAGE)==0?LightTexture.FULL_BRIGHT:light,overlay);}
            case BUZZER->{box(p,out,3,3,6,13,14,9,METAL,light,overlay);box(p,out,6,5,9,10,8,10,BONE,light,overlay);box(p,out,5,10,9,11,12,9.5F,BLACK,light,overlay);}
            case REGISTER->{box(p,out,1,0,2,15,5,14,METAL,light,overlay);box(p,out,3,5,3,13,13,10,BLACK,light,overlay);box(p,out,4,8,10,12,11,10.2F,CRT,LightTexture.FULL_BRIGHT,overlay);box(p,out,0,.5F,10,7,1,15,PAPER,light,overlay);}
            case PIE_CASE->{box(p,out,0,0,0,16,2,16,METAL,light,overlay);box(p,out,0,14,0,16,15,16,METAL,light,overlay);for(int x:new int[]{0,15})for(int z:new int[]{0,15})box(p,out,x,2,z,x+1,14,z+1,METAL,light,overlay);box(p,out,3,3,4,13,6,12,BONE,light,overlay);}
            case PLATE->{box(p,out,2,0,2,14,1,14,BONE,light,overlay);box(p,out,4,1,4,12,2,12,FLESH,light,overlay);}
            case BOX->{box(p,out,2,0,2,14,9,14,WOOD,light,overlay);box(p,out,7,3,14,9,6,14.2F,BONE,light,overlay);}
        }p.popPose();
    }
    public static void box(PoseStack p,VertexConsumer out,float x,float y,float z,float X,float Y,float Z,int tile,int light,int overlay){
        float u0=(tile%4*16+.5F)/64,v0=(tile/4*16+.5F)/64,u1=(tile%4*16+15.5F)/64,v1=(tile/4*16+15.5F)/64;
        face(p,out,new float[][]{{x,y,z},{x,Y,z},{X,Y,z},{X,y,z}},0,0,-1,u0,v0,u1,v1,light,overlay);
        face(p,out,new float[][]{{X,y,Z},{X,Y,Z},{x,Y,Z},{x,y,Z}},0,0,1,u0,v0,u1,v1,light,overlay);
        face(p,out,new float[][]{{x,y,Z},{x,Y,Z},{x,Y,z},{x,y,z}},-1,0,0,u0,v0,u1,v1,light,overlay);
        face(p,out,new float[][]{{X,y,z},{X,Y,z},{X,Y,Z},{X,y,Z}},1,0,0,u0,v0,u1,v1,light,overlay);
        face(p,out,new float[][]{{x,Y,z},{x,Y,Z},{X,Y,Z},{X,Y,z}},0,1,0,u0,v0,u1,v1,light,overlay);
        face(p,out,new float[][]{{x,y,Z},{x,y,z},{X,y,z},{X,y,Z}},0,-1,0,u0,v0,u1,v1,light,overlay);
    }
    private static void face(PoseStack p,VertexConsumer out,float[][] points,int nx,int ny,int nz,float u,float v,float U,float V,int light,int overlay){
        for(int i=0;i<4;i++)out.addVertex(p.last(),points[i][0]/16,points[i][1]/16,points[i][2]/16).setColor(0xffffffff).setUv(i==0||i==1?u:U,i==0||i==3?V:v).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
    }
}
