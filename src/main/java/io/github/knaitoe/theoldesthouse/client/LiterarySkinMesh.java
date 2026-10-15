package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;

/** Fit exceptional painted nets in the mesh; never resample or repaint the approved PNGs. */
final class LiterarySkinMesh {
    private LiterarySkinMesh(){}
    static float dennyLegV(float v){float pixel=v*64;return (pixel<=20?pixel+20:40+(pixel-20)*(23F/12))/64;}
    /** The four-sided denim/boot net is v36..63; the final three PNG rows are transparent export padding. */
    static VertexConsumer dennyLeg(VertexConsumer out){
        return new VertexConsumer(){
            public VertexConsumer addVertex(float x,float y,float z){out.addVertex(x,y,z);return this;}
            public VertexConsumer setColor(int r,int g,int b,int a){out.setColor(r,g,b,a);return this;}
            public VertexConsumer setUv(float u,float v){out.setUv(u,dennyLegV(v));return this;}
            public VertexConsumer setUv1(int u,int v){out.setUv1(u,v);return this;}
            public VertexConsumer setUv2(int u,int v){out.setUv2(u,v);return this;}
            public VertexConsumer setNormal(float x,float y,float z){out.setNormal(x,y,z);return this;}
        };
    }
    /** His painted vest back is u36..44; the stock u32..40 crosses into a bare-arm island. */
    static void dennyBody(ModelPart body,PoseStack p,VertexConsumer out,int light,int overlay,int colour){
        if(!body.visible)return;p.pushPose();body.translateAndRotate(p);
        quad(p,out,new float[][]{{-4,0,-2},{4,0,-2},{4,12,-2},{-4,12,-2}},20,20,28,32,0,0,-1,light,overlay,colour);
        quad(p,out,new float[][]{{4,0,2},{-4,0,2},{-4,12,2},{4,12,2}},36,20,44,32,0,0,1,light,overlay,colour);
        quad(p,out,new float[][]{{-4,0,2},{-4,0,-2},{-4,12,-2},{-4,12,2}},16,20,20,32,-1,0,0,light,overlay,colour);
        quad(p,out,new float[][]{{4,0,-2},{4,0,2},{4,12,2},{4,12,-2}},28,20,32,32,1,0,0,light,overlay,colour);
        quad(p,out,new float[][]{{-4,0,2},{4,0,2},{4,0,-2},{-4,0,-2}},20,16,28,20,0,-1,0,light,overlay,colour);
        quad(p,out,new float[][]{{-4,12,-2},{4,12,-2},{4,12,2},{-4,12,2}},28,20,36,16,0,1,0,light,overlay,colour);
        p.popPose();
    }
    private static void quad(PoseStack p,VertexConsumer out,float[][] points,float u0,float v0,float u1,float v1,float nx,float ny,float nz,int light,int overlay,int colour){
        float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        for(int i=0;i<4;i++)out.addVertex(p.last(),points[i][0]/16,points[i][1]/16,points[i][2]/16).setColor(colour).setUv(uv[i][0]/128,uv[i][1]/64).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
    }
}
