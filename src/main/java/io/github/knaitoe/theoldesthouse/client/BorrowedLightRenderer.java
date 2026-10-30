package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrowedLightBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.PuzzleRegistry;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 0.4.199: Borrowed Light's beam, drawn from the lamp through each turn to where it stops, warm, soft-edged and unlit by the room. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class BorrowedLightRenderer implements BlockEntityRenderer<BorrowedLightBlock.Beam> {
    public static final ResourceLocation BEAM=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/borrowed_light/beam.png");
    public BorrowedLightRenderer(BlockEntityRendererProvider.Context ignored){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(PuzzleRegistry.BEAM.get(),BorrowedLightRenderer::new);}
    @Override public void render(BorrowedLightBlock.Beam beam,float partial,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var points=beam.points();if(points.size()<2)return;
        var origin=Vec3.atLowerCornerOf(beam.getBlockPos());var path=new java.util.ArrayList<Vec3>();for(var at:points)path.add(Vec3.atCenterOf(at).subtract(origin));
        draw(pose,out,path,beam.getLevel()==null?0:(beam.getLevel().getGameTime()+partial)*.02F);
    }
    /** The beam along block centres, relative to the pose: from the lamp, through each turn, to what stopped it. */
    static void draw(PoseStack pose,MultiBufferSource out,java.util.List<Vec3> points,float time){
        var buffer=out.getBuffer(RenderType.entityTranslucentEmissive(BEAM));
        for(int i=0;i+1<points.size();i++){
            var a=points.get(i);var b=points.get(i+1);
            var dir=b.subtract(a).normalize();
            // The first leg leaves the lamp's lens; the last stops at the face of what stopped it.
            if(i==0)a=a.add(dir.scale(.3));
            if(i+2==points.size())b=b.subtract(dir.scale(.5));
            for(float[] layer:new float[][]{{.07F,150},{.16F,60}})shaft(pose,buffer,a,b,dir,layer[0],(int)layer[1],time);
        }
    }
    /** A square shaft from a to b, w across, its texture drifting slowly along it like dust in the light. */
    private static void shaft(PoseStack pose,VertexConsumer buffer,Vec3 a,Vec3 b,Vec3 dir,float w,int alpha,float time){
        var side=Math.abs(dir.y)>.9?new Vec3(1,0,0):new Vec3(0,1,0).cross(dir).normalize();var up=dir.cross(side).normalize();
        float length=(float)a.distanceTo(b);var m=pose.last();
        Vec3[] corners={side.scale(w).add(up.scale(w)),side.scale(-w).add(up.scale(w)),side.scale(-w).add(up.scale(-w)),side.scale(w).add(up.scale(-w))};
        for(int k=0;k<4;k++){var c0=corners[k];var c1=corners[(k+1)%4];var n=c0.add(c1).normalize();
            vertex(buffer,m,a.add(c0),0,time,alpha,n);vertex(buffer,m,a.add(c1),1,time,alpha,n);
            vertex(buffer,m,b.add(c1),1,time+length,alpha,n);vertex(buffer,m,b.add(c0),0,time+length,alpha,n);}
    }
    private static void vertex(VertexConsumer buffer,PoseStack.Pose m,Vec3 at,float u,float v,int alpha,Vec3 n){
        buffer.addVertex(m,(float)at.x,(float)at.y,(float)at.z).setColor(255,238,196,alpha).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(m,(float)n.x,(float)n.y,(float)n.z);
    }
    @Override public boolean shouldRenderOffScreen(BorrowedLightBlock.Beam beam){return true;}
    @Override public int getViewDistance(){return 96;}
    @Override public AABB getRenderBoundingBox(BorrowedLightBlock.Beam beam){return AABB.INFINITE;}
}
