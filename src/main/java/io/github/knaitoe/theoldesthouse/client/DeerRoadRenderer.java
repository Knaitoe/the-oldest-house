package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.List;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 0.4.310, A Deer in the Road: the road's anchor draws the car (DeerCarMesh, the same boxes its cells collide with), the doe and the
 * next vehicle, each where the shared scene says, smoothly between updates. Lamps that are lit are drawn at full brightness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class DeerRoadRenderer implements BlockEntityRenderer<DeerRoadScene> {
    public DeerRoadRenderer(BlockEntityRendererProvider.Context ignored){}
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(DeerRoadRegistry.SCENE.get(),DeerRoadRenderer::new);e.registerEntityRenderer(DeerRoadRegistry.SEAT.get(),NoopRenderer::new);}
    static ResourceLocation texture(String m){
        String path=switch(m){case "glass"->"uncanny/glass";case "headliner"->"backseat/headliner";case "gauges"->"backseat/gauges";default->"deer_road/"+m;};
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/"+path+".png");
    }
    @Override public int getViewDistance(){return 160;}
    @Override public boolean shouldRenderOffScreen(DeerRoadScene s){return true;}
    @Override public AABB getRenderBoundingBox(DeerRoadScene s){var b=s.base();return new AABB(b.getX()+DeerRoadSite.WEST,b.getY()-3,b.getZ()+DeerRoadSite.FAR,b.getX()+DeerRoadSite.EAST,b.getY()+8,b.getZ()+4);}
    @Override public void render(DeerRoadScene s,float partial,PoseStack p,MultiBufferSource out,int light,int overlay){
        if(s.getLevel()==null)return;float now=s.getLevel().getGameTime()+partial;float f=Mth.clamp(now-s.syncedAt,0,1);
        var a=DeerRoadSite.ANCHOR;
        // The car.
        double cx=Mth.lerp(f,s.pCarX,s.carX),cz=Mth.lerp(f,s.pCarZ,s.carZ);float yaw=Mth.lerp(f,s.pCarYaw,s.carYaw);
        int at=net.minecraft.client.renderer.LevelRenderer.getLightColor(s.getLevel(),s.base().offset((int)Math.floor(cx),1,(int)Math.floor(cz)));
        p.pushPose();p.translate(cx-a.getX(),-a.getY(),cz-a.getZ());p.mulPose(Axis.YP.rotationDegrees(-yaw));car(s,p,out,at,overlay,now);p.popPose();
        // The doe.
        if(s.doe!=DeerRoadScene.DOE_HIDDEN){
            double dx=Mth.lerp(f,s.pDoeX,s.doeX),dz=Mth.lerp(f,s.pDoeZ,s.doeZ),dy=Mth.lerp(f,s.pDoeY,s.doeY);float dyaw=s.doe==DeerRoadScene.DOE_THROWN?s.doeYaw+31*f:s.doeYaw;
            int dl=net.minecraft.client.renderer.LevelRenderer.getLightColor(s.getLevel(),s.base().offset((int)Math.floor(dx),1,(int)Math.floor(dz)));
            p.pushPose();p.translate(dx-a.getX(),dy-a.getY(),dz-a.getZ());p.mulPose(Axis.YP.rotationDegrees(-dyaw));doe(s,p,out,dl,overlay,now);p.popPose();
        }
        // The next vehicle.
        if(s.van!=DeerRoadScene.VAN_NONE){
            double vx=Mth.lerp(f,s.pVanX,s.vanX),vz=Mth.lerp(f,s.pVanZ,s.vanZ);
            int vl=net.minecraft.client.renderer.LevelRenderer.getLightColor(s.getLevel(),s.base().offset((int)Math.floor(vx),1,(int)Math.floor(vz)));
            p.pushPose();p.translate(vx-a.getX(),-a.getY(),vz-a.getZ());van(p,out,vl,overlay);p.popPose();
        }
    }
    static float swing(DeerRoadScene s,int door,float now){float t=Mth.clamp((now-s.doorAt[door])/6F,0,1);return s.open(door)?t:1-t;}
    private static void car(DeerRoadScene s,PoseStack p,MultiBufferSource out,int light,int overlay,float now){
        for(var b:DeerCarMesh.SHELL)box(p,out,b,light,overlay);
        for(var b:s.bench==0?DeerCarMesh.BENCH_WHOLE:s.bench==1?DeerCarMesh.BENCH_PUSHED:DeerCarMesh.BENCH_SPLIT)box(p,out,b,light,overlay);
        for(var b:DeerCarMesh.WHEEL)box(p,out,b,light,overlay);
        for(int i=0;i<DeerCarMesh.DOORS.size();i++){
            var d=DeerCarMesh.DOORS.get(i);float open=swing(s,i,now);
            p.pushPose();p.translate(d.hingeX()/16,0,d.hingeZ()/16);p.mulPose(Axis.YP.rotationDegrees(d.out()*90*open));p.translate(-d.hingeX()/16,0,-d.hingeZ()/16);
            for(var b:d.boxes())box(p,out,b,light,overlay);p.popPose();
        }
        float boot=Mth.clamp((now-s.bootAt)/8F,0,1);boot=s.boot?boot:1-boot;
        p.pushPose();p.translate(0,20/16F,30/16F);p.mulPose(Axis.XP.rotationDegrees(boot*70));p.translate(0,-20/16F,-30/16F);for(var b:DeerCarMesh.BOOT_LID)box(p,out,b,light,overlay);p.popPose();
        if(s.boot&&!s.triangleTaken){box(p,out,new DeerCarMesh.Box(-6,11,31,6,14,36,"triangle_case"),light,overlay);}
        // The rear belt: hanging loose over the empty seat, then drawn across it, a tooth at a time.
        if(s.beltTeeth==0)box(p,out,new DeerCarMesh.Box(-15,15,25.2F,-13,29,25.6F,"belt"),light,overlay);
        else{float lift=Math.min(7,s.beltTeeth)*.35F;box(p,out,new DeerCarMesh.Box(-15,14+lift,15,13,15+lift,17,"belt"),light,overlay);box(p,out,new DeerCarMesh.Box(-15,15,24,-13,29,25,"belt"),light,overlay);}
        int bright=LightTexture.FULL_BRIGHT;
        boolean lit=s.car!=DeerRoadScene.FRESH||true;
        if(lit){for(var b:DeerCarMesh.HEADLAMPS)box(p,out,b,bright,overlay);for(var b:DeerCarMesh.TAILS)box(p,out,b,bright,overlay);}
        boolean blink=s.hazardFrom>=0&&((long)(now-s.hazardFrom))%20<10;
        for(var b:DeerCarMesh.HAZARDS)box(p,out,b,blink?bright:light,overlay);
        if(s.doors!=0)box(p,out,DeerCarMesh.DOME,bright,overlay);
    }
    private static void doe(DeerRoadScene s,PoseStack p,MultiBufferSource out,int light,int overlay,float now){
        boolean lying=s.doe>=DeerRoadScene.DOE_LYING;
        if(lying||s.doe==DeerRoadScene.DOE_THROWN){p.translate(0,5/16F,0);p.mulPose(Axis.ZP.rotationDegrees(lying?90:Mth.sin(now*.7F)*80));p.translate(0,-11/16F,0);}
        float step=s.doe==DeerRoadScene.DOE_STEP?Mth.sin(now*.6F)*25:0;
        // The belly moves in little starts before she comes out, then lies open.
        float twitch=s.story<=DeerRoadScene.BIRTH1&&s.stopped()&&((int)now%17<3)?1.2F:0;
        box(p,out,new DeerCarMesh.Box(-5,14,-9,5,23,9,"fur"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-6-twitch*.4F,11-twitch,-6,6+twitch*.4F,16,6,"fur_belly"),light,overlay);
        if(s.doe==DeerRoadScene.DOE_OPEN||s.stopped()&&s.story>DeerRoadScene.BIRTH1)box(p,out,new DeerCarMesh.Box(-4,10.9F,-4,4,11.4F,4,"wound"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-2,20,-14,2,28,-8,"fur"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-2.5F,25,-20,2.5F,30,-13,"doe_face"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-3.5F,29,-15,-1.5F,33,-14,"fur"),light,overlay);box(p,out,new DeerCarMesh.Box(1.5F,29,-15,3.5F,33,-14,"fur"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-1.5F,18,9,1.5F,23,11,"fur_belly"),light,overlay);
        for(int i=0;i<4;i++){float x=i%2==0?-4:2,z=i<2?-7:5,rot=(i==0||i==3?step:-step);
            p.pushPose();p.translate((x+1)/16F,14/16F,(z+1)/16F);p.mulPose(Axis.XP.rotationDegrees(lying?(i<2?-25:25):rot));p.translate(-(x+1)/16F,-14/16F,-(z+1)/16F);
            box(p,out,new DeerCarMesh.Box(x,0,z,x+2,14,z+2,"fur"),light,overlay);p.popPose();}
        if(!lying)box(p,out,new DeerCarMesh.Box(-2,28,-20.1F,2,29,-20,"eyeshine"),LightTexture.FULL_BRIGHT,overlay);
    }
    private static void van(PoseStack p,MultiBufferSource out,int light,int overlay){
        box(p,out,new DeerCarMesh.Box(-19,4,-44,19,34,44,"van"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-17,22,-44.2F,17,32,-44.05F,"glass_dark"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-19.2F,22,-40,-19.05F,32,-26,"glass_dark"),light,overlay);box(p,out,new DeerCarMesh.Box(19.05F,22,-40,19.2F,32,-26,"glass_dark"),light,overlay);
        for(int side:new int[]{-1,1})for(int z:new int[]{-30,30})box(p,out,new DeerCarMesh.Box(side<0?-20:16,0,z-5,side<0?-16:20,9,z+5,"tyre"),light,overlay);
        box(p,out,new DeerCarMesh.Box(-17,10,-44.4F,-10,15,-44.2F,"headlamp"),LightTexture.FULL_BRIGHT,overlay);
        box(p,out,new DeerCarMesh.Box(10,10,-44.4F,17,15,-44.2F,"headlamp"),LightTexture.FULL_BRIGHT,overlay);
        box(p,out,new DeerCarMesh.Box(-17,12,44.05F,-12,17,44.2F,"tail"),LightTexture.FULL_BRIGHT,overlay);box(p,out,new DeerCarMesh.Box(12,12,44.05F,17,17,44.2F,"tail"),LightTexture.FULL_BRIGHT,overlay);
    }
    static void box(PoseStack p,MultiBufferSource buffers,DeerCarMesh.Box b,int light,int overlay){
        var out=buffers.getBuffer(RenderType.entityCutoutNoCull(texture(b.material())));
        face(p,out,b.x0(),b.y0(),b.z0(),b.x1()-b.x0(),b.y1()-b.y0(),0,0,-1,light,overlay);
        face(p,out,b.x1(),b.y0(),b.z1(),b.x1()-b.x0(),b.y1()-b.y0(),0,0,1,light,overlay);
        face(p,out,b.x0(),b.y0(),b.z1(),b.z1()-b.z0(),b.y1()-b.y0(),-1,0,0,light,overlay);
        face(p,out,b.x1(),b.y0(),b.z0(),b.z1()-b.z0(),b.y1()-b.y0(),1,0,0,light,overlay);
        face(p,out,b.x0(),b.y0(),b.z1(),b.x1()-b.x0(),b.z1()-b.z0(),0,-1,0,light,overlay);
        face(p,out,b.x0(),b.y1(),b.z0(),b.x1()-b.x0(),b.z1()-b.z0(),0,1,0,light,overlay);
    }
    /** Tiled at sixteen pixels to the block, as BackseatArt does, rather than stretched. */
    private static void face(PoseStack p,VertexConsumer out,float x,float y,float z,float width,float height,int nx,int ny,int nz,int light,int overlay){
        for(float v=0;v<height;v+=16)for(float u=0;u<width;u+=16){
            float w=Math.min(16,width-u),h=Math.min(16,height-v);
            for(int i=0;i<4;i++){
                float du=(i==1||i==2)?w:0,dv=i>=2?h:0,a=u+du,c=v+dv;
                float X=x+(nx==0?(nz==1?-a:a):0),Y=y+(ny==0?c:0),Z=z+(nx!=0?(nx<0?-a:a):ny!=0?(ny<0?-c:c):0);
                out.addVertex(p.last(),X/16,Y/16,Z/16).setColor(-1).setUv(du/16,dv/16).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
            }
        }
    }
    static final List<String> MATERIALS=List.of("under","paint","chrome","grille","vinyl","carpet","tyre","hub","dash","foam","belt","headlamp","amber","tail","dome","triangle_case","fur","fur_belly","doe_face","wound","eyeshine","van","glass_dark");
}
