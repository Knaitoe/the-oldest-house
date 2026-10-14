package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.geom.ModelPart;

/** Fits the unchanged painted atlas; eyelids use its reserved skin, and mouth interiors never repeat the face. */
final class GhostsSkinMesh {
    private GhostsSkinMesh(){}
    static float atlasScale(int role,int phase){return role==LiteraryActor.GHOST_MARJORIE&&(phase==GhostsSet.VOMIT||phase==GhostsSet.EATING||phase==GhostsSet.CHOKING||phase==GhostsSet.DEAD)?128F*18/1774:1;}
    static VertexConsumer fit(VertexConsumer out,int role,int phase){
        float scale=atlasScale(role,phase);if(scale==1)return out;
        return new VertexConsumer(){
            public VertexConsumer addVertex(float x,float y,float z){out.addVertex(x,y,z);return this;}
            public VertexConsumer setColor(int r,int g,int b,int a){out.setColor(r,g,b,a);return this;}
            public VertexConsumer setUv(float u,float v){out.setUv(u*scale,v*scale);return this;}
            public VertexConsumer setUv1(int u,int v){out.setUv1(u,v);return this;}
            public VertexConsumer setUv2(int u,int v){out.setUv2(u,v);return this;}
            public VertexConsumer setNormal(float x,float y,float z){out.setNormal(x,y,z);return this;}
        };
    }
    static void head(ModelPart head,ModelPart jaw,float blink,PoseStack p,VertexConsumer v,int light,int overlay,int colour){
        if(!head.visible)return;p.pushPose();head.translateAndRotate(p);
        sides(p,v,light,overlay,colour,-8,-2,8,14);
        quad(p,v,light,overlay,colour,new float[][]{{-4,-8,-4},{4,-8,-4},{4,-8,4},{-4,-8,4}},8,0,16,8,0,-1,0);
        // The upper palate and hinged jaw top both sample only the reserved skin strip, darkened inside.
        quad(p,v,light,overlay,0xff362122,new float[][]{{-4,-2,4},{4,-2,4},{4,-2,-4},{-4,-2,-4}},65,.5F,75,2.5F,0,1,0);
        if(blink>0){
            for(float x:new float[]{-3.05F,.55F}){
                float bottom=-4.85F+1.7F*blink;
                quad(p,v,light,overlay,colour,new float[][]{{x,bottom,-4.035F},{x+2.5F,bottom,-4.035F},{x+2.5F,-4.85F,-4.035F},{x,-4.85F,-4.035F}},66,.65F,70,1.9F,0,0,-1);
                if(blink>.75F)quad(p,v,light,overlay,0xff79665b,new float[][]{{x+.15F,bottom,-4.04F},{x+2.35F,bottom,-4.04F},{x+2.35F,bottom-.045F,-4.04F},{x+.15F,bottom-.045F,-4.04F}},66,1,70,1.1F,0,0,-1);
            }
        }
        p.pushPose();jaw.translateAndRotate(p);sides(p,v,light,overlay,colour,0,2,14,16);
        quad(p,v,light,overlay,0xff362122,new float[][]{{-4,0,-4},{4,0,-4},{4,0,4},{-4,0,4}},65,.5F,75,2.5F,0,-1,0);
        quad(p,v,light,overlay,colour,new float[][]{{-4,2,4},{4,2,4},{4,2,-4},{-4,2,-4}},65,.5F,75,2.5F,0,1,0);
        p.popPose();p.popPose();
    }
    private static void sides(PoseStack p,VertexConsumer v,int light,int overlay,int colour,float top,float bottom,float uvTop,float uvBottom){
        quad(p,v,light,overlay,colour,new float[][]{{-4,bottom,-4},{4,bottom,-4},{4,top,-4},{-4,top,-4}},8,uvTop,16,uvBottom,0,0,-1);
        quad(p,v,light,overlay,colour,new float[][]{{-4,bottom,4},{-4,bottom,-4},{-4,top,-4},{-4,top,4}},0,uvTop,8,uvBottom,-1,0,0);
        quad(p,v,light,overlay,colour,new float[][]{{4,bottom,-4},{4,bottom,4},{4,top,4},{4,top,-4}},16,uvTop,24,uvBottom,1,0,0);
        quad(p,v,light,overlay,colour,new float[][]{{4,bottom,4},{-4,bottom,4},{-4,top,4},{4,top,4}},24,uvTop,32,uvBottom,0,0,1);
    }
    private static void quad(PoseStack p,VertexConsumer v,int light,int overlay,int colour,float[][] points,float u0,float v0,float u1,float v1,float nx,float ny,float nz){
        float[][] uv={{u0,v1},{u1,v1},{u1,v0},{u0,v0}};
        for(int i=0;i<4;i++)v.addVertex(p.last(),points[i][0]/16,points[i][1]/16,points[i][2]/16).setColor(colour).setUv(uv[i][0]/128,uv[i][1]/64).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
    }
}
