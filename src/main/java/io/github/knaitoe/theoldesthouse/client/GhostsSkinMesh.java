package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.geom.ModelPart;

/** Fits the unchanged painted atlas; eyelids use clean cheek skin, and mouth interiors never repeat the face. */
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
        walls(p,v,light,overlay,colour,-8,0,8,16);
        quad(p,v,light,overlay,colour,new float[][]{{-4,-1.35F,-4},{4,-1.35F,-4},{4,-8,-4},{-4,-8,-4}},8,8,16,14.65F,0,0,-1);
        quad(p,v,light,overlay,colour,new float[][]{{-4,-8,-4},{4,-8,-4},{4,-8,4},{-4,-8,4}},8,0,16,8,0,-1,0);
        quad(p,v,light,overlay,colour,new float[][]{{-4,0,4},{4,0,4},{4,0,-4},{-4,0,-4}},65,.5F,75,2.5F,0,1,0);
        // Pin the cheeks/chin/hair. Only the painted lower lip opens; no ear-to-ear slit or duplicated face.
        float open=(float)Math.max(0,Math.sin(jaw.xRot)*1.8),lip=-1.35F;
        float[] xs={-4,-1.55F,-1.1F,1.1F,1.55F,4},weight={0,0,1,1,0,0};
        for(int i=0;i<xs.length-1;i++){
            float x0=xs[i],x1=xs[i+1],y0=lip+open*weight[i],y1=lip+open*weight[i+1];
            quad(p,v,light,overlay,colour,new float[][]{{x0,0,-4},{x1,0,-4},{x1,y1,-4},{x0,y0,-4}},x0+12,14.65F,x1+12,16,0,0,-1);
            if(open>0&&(weight[i]>0||weight[i+1]>0))quad(p,v,light,overlay,0xff362122,new float[][]{{x0,y0,-3.998F},{x1,y1,-3.998F},{x1,lip,-3.998F},{x0,lip,-3.998F}},65,.5F,75,2.5F,0,0,-1);
        }
        if(blink>0){
            for(float x:new float[]{-3.05F,.55F}){
                for(int i=0;i<10;i++){
                    float a=i/10F,b=(i+1)/10F,ea=(a-.5F)*2,eb=(b-.5F)*2;
                    float topA=-4.75F+.55F*ea*ea,topB=-4.75F+.55F*eb*eb;
                    float bottomA=topA+(-3.35F-.45F*ea*ea-topA)*blink,bottomB=topB+(-3.35F-.45F*eb*eb-topB)*blink;
                    quad(p,v,light,overlay,colour,new float[][]{{x+a*2.5F,bottomA,-4.035F},{x+b*2.5F,bottomB,-4.035F},{x+b*2.5F,topB,-4.035F},{x+a*2.5F,topA,-4.035F}},9.8F+a*.6F,13.3F,9.8F+b*.6F,13.7F,0,0,-1);
                    if(blink>.75F&&i>0&&i<9){float lineA=-3.95F+.1F*ea*ea,lineB=-3.95F+.1F*eb*eb;quad(p,v,light,overlay,0xff79665b,new float[][]{{x+a*2.5F,lineA,-4.04F},{x+b*2.5F,lineB,-4.04F},{x+b*2.5F,lineB-.12F,-4.04F},{x+a*2.5F,lineA-.12F,-4.04F}},9.8F,13.3F,10.4F,13.4F,0,0,-1);}
                }
            }
        }
        p.popPose();
    }
    private static void walls(PoseStack p,VertexConsumer v,int light,int overlay,int colour,float top,float bottom,float uvTop,float uvBottom){
        quad(p,v,light,overlay,colour,new float[][]{{-4,bottom,4},{-4,bottom,-4},{-4,top,-4},{-4,top,4}},0,uvTop,8,uvBottom,-1,0,0);
        quad(p,v,light,overlay,colour,new float[][]{{4,bottom,-4},{4,bottom,4},{4,top,4},{4,top,-4}},16,uvTop,24,uvBottom,1,0,0);
        quad(p,v,light,overlay,colour,new float[][]{{4,bottom,4},{-4,bottom,4},{-4,top,4},{4,top,4}},24,uvTop,32,uvBottom,0,0,1);
    }
    private static void quad(PoseStack p,VertexConsumer v,int light,int overlay,int colour,float[][] points,float u0,float v0,float u1,float v1,float nx,float ny,float nz){
        float[][] uv={{u0,v1},{u1,v1},{u1,v0},{u0,v0}};
        for(int i=0;i<4;i++)v.addVertex(p.last(),points[i][0]/16,points[i][1]/16,points[i][2]/16).setColor(colour).setUv(uv[i][0]/128,uv[i][1]/64).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
    }
}
