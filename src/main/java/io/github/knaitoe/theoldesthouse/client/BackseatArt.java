package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BackseatCarMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Production car geometry with native 16-pixel repeating materials, no baked light. */
public final class BackseatArt {
    public static final String[] NEW_MATERIALS={"cream","vinyl","rubber","gauges","chrome","mirror","headliner"};
    private BackseatArt() {}
    public static ResourceLocation texture(String material){
        String path=switch(material){case "wood"->"uncanny/dark_wood";case "glass"->"uncanny/glass";case "lamp"->"uncanny/lamp";case "tail"->"uncanny/vinyl";default->"backseat/"+material;};
        return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/block/"+path+".png");
    }
    public static void draw(PoseStack p,MultiBufferSource out,int light,int overlay,float door,float wiper){
        draw(p,out,light,overlay,door,wiper,true);
    }
    static void draw(PoseStack p,MultiBufferSource out,int light,int overlay,float door,float wiper,boolean mirror){
        draw(p,out,light,overlay,door,wiper,mirror,-1);
    }
    static void draw(PoseStack p,MultiBufferSource out,int light,int overlay,float door,float wiper,boolean mirror,float shatter){
        for(var box:BackseatCarMesh.SHELL)if((mirror||!box.material().equals("mirror"))&&!(shatter>=0&&box.material().equals("glass")&&box.x0()==-17&&box.z0()==-22))box(p,out,box,light,overlay);
        p.pushPose();p.translate(-19/16D,0,-20/16D);p.mulPose(Axis.YP.rotationDegrees(-door*90));p.translate(19/16D,0,20/16D);
        for(var box:BackseatCarMesh.DRIVER_DOOR)box(p,out,box,light,overlay);p.popPose();
        for(var box:BackseatCarMesh.STEERING)box(p,out,box,light,overlay);
        for(int x:new int[]{-12,6}){
            p.pushPose();p.translate(x/16D,25/16D,-22/16D);p.mulPose(Axis.ZP.rotationDegrees(wiper));
            box(p,out,new BackseatCarMesh.Box(0,0,-.6F,1,9,-.2F,"rubber"),light,overlay);p.popPose();
        }
        if(shatter>=0){float t=Math.min(1,shatter);for(int i=0;i<8;i++){
            p.pushPose();float x=(i%4*8-12)/16F,y=(27+i/4*4)/16F;
            p.translate(x*(1-t)-.35F*t,y*(1-t)+(1.3F+(i%3)*.12F)*t,(-21.4F/16)*(1-t)-.05F*t);
            p.mulPose(Axis.YP.rotationDegrees((i*47+80)*t));p.mulPose(Axis.ZP.rotationDegrees((i*31-80)*t));
            var shard=out.getBuffer(RenderType.entityCutoutNoCull(texture("glass")));
            // The window tile is mostly transparent. Sample one of its actual opaque
            // reflection pixels so a tiny native fragment cannot disappear in an empty UV patch.
            float u=(i%2==0?6.5F:14.5F)/16,v=(i%2==0?2.5F:10.5F)/16;
            for(int corner=0;corner<4;corner++)shard.addVertex(p.last(),corner==0||corner==3?-1.5F/16:1.5F/16,corner<2?-2/16F:2/16F,0).setColor(-1).setUv(u,v).setOverlay(overlay).setLight(light).setNormal(p.last(),0,0,1);
            p.popPose();
        }}
    }
    private static void box(PoseStack p,MultiBufferSource buffers,BackseatCarMesh.Box b,int light,int overlay){
        var out=buffers.getBuffer(RenderType.entityCutoutNoCull(texture(b.material())));
        face(p,out,b.x0(),b.y0(),b.z0(),b.x1()-b.x0(),b.y1()-b.y0(),0,0,-1,light,overlay);
        face(p,out,b.x1(),b.y0(),b.z1(),b.x1()-b.x0(),b.y1()-b.y0(),0,0,1,light,overlay);
        face(p,out,b.x0(),b.y0(),b.z1(),b.z1()-b.z0(),b.y1()-b.y0(),-1,0,0,light,overlay);
        face(p,out,b.x1(),b.y0(),b.z0(),b.z1()-b.z0(),b.y1()-b.y0(),1,0,0,light,overlay);
        face(p,out,b.x0(),b.y0(),b.z1(),b.x1()-b.x0(),b.z1()-b.z0(),0,-1,0,light,overlay);
        face(p,out,b.x0(),b.y1(),b.z0(),b.x1()-b.x0(),b.z1()-b.z0(),0,1,0,light,overlay);
    }
    /** Tile each face instead of stretching a 16px sheet across the whole car. */
    private static void face(PoseStack p,VertexConsumer out,float x,float y,float z,float width,float height,int nx,int ny,int nz,int light,int overlay){
        for(float v=0;v<height;v+=16)for(float u=0;u<width;u+=16){
            float w=Math.min(16,width-u),h=Math.min(16,height-v);
            for(int i=0;i<4;i++){
                float du=(i==1||i==2)?w:0,dv=i>=2?h:0,a=u+du,b=v+dv;
                float X=x+(nx==0?(nz==1?-a:a):0);
                float Y=y+(ny==0?b:0);
                float Z=z+(nx!=0?(nx<0?-a:a):ny!=0?(ny<0?-b:b):0);
                out.addVertex(p.last(),X/16,Y/16,Z/16).setColor(-1).setUv(du/16,dv/16).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
            }
        }
    }
}
