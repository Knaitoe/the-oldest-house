package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.client.model.geom.ModelPart;

/** Fits the native pixel atlas; eyelids use clean cheek skin, and mouth interiors never repeat the face. */
final class GhostsSkinMesh {
    private GhostsSkinMesh(){}
    // Every painted phase now shares the exact native 128x64 layout.
    static VertexConsumer fit(VertexConsumer out,int role,int phase){return out;}
    static void head(ModelPart head,ModelPart jaw,float blink,float[] face,PoseStack p,VertexConsumer v,int light,int overlay,int colour){
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
        if(face!=null)expression(p,v,light,overlay,colour,face[0],face[1],face[2],1);
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
    /**
     * 0.4.242: the painted brows (face row 6, three pixels each side) go under forehead skin and are drawn again, each pixel lifted or
     * lowered: all of them by the lift, the inner ones further by the tilt. Mouth corners are one lip-coloured pixel either side of the
     * lip, above for a smile and below for a frown. Every colour is sampled from the reader's own painted sheet.
     */
    static void standardExpression(ModelPart head,PoseStack p,VertexConsumer v,int light,int overlay,int colour,float lift,float tilt,float mouth){
        p.pushPose();head.translateAndRotate(p);
        expression(p,v,light,overlay,colour,lift,tilt,mouth,2);
        p.popPose();
    }
    private static void expression(PoseStack p,VertexConsumer v,int light,int overlay,int colour,float lift,float tilt,float mouth,float uScale){
        if(Math.abs(lift)<.04F&&Math.abs(tilt)<.04F&&Math.abs(mouth)<.3F)return;
        for(float x0:new float[]{-2.5F,1F})quad(p,v,light,overlay,colour,rect(x0,-5,x0+1.5F,-4.5F,-4.02F),11F*uScale,10.5F,12F*uScale,11F,0,0,-1);
        for(int side=0;side<2;side++){
            float x0=side==0?-2.5F:1F;
            for(int i=0;i<3;i++){
                float inner=side==0?i/2F:(2-i)/2F,y=-5-lift*.5F-tilt*.5F*inner,x=x0+i*.5F;
                quad(p,v,light,overlay,colour,rect(x,y,x+.5F,y+.5F,-4.03F),10F*uScale,11F,10.5F*uScale,11.5F,0,0,-1);
            }
        }
        if(Math.abs(mouth)>=.3F){float y=mouth>0?-2F:-1F;for(float x:new float[]{-1.5F,1F})quad(p,v,light,overlay,colour,rect(x,y,x+.5F,y+.5F,-4.03F),11.5F*uScale,14.5F,12F*uScale,15F,0,0,-1);}
    }
    /**
     * 0.4.326 (owner: "Both of the dad's hands are injured, which isn't accurate to events"): both arms sample one painted net, so the
     * injury painted on John's right hand showed on his left as well. On the left forearm the two marked pixels are covered with clean
     * skin from the same rows of his own sheet; the right hand keeps the injury.
     */
    static void coverLeftHandMark(ModelPart arm,ModelPart elbow,PoseStack p,VertexConsumer v,int light,int overlay,int colour){
        if(!arm.visible)return;p.pushPose();arm.translateAndRotate(p);elbow.translateAndRotate(p);
        quad(p,v,light,overlay,colour,rect(0,3,2,4,-2.02F),47.25F,29.25F,47.75F,29.75F,0,0,-1);
        quad(p,v,light,overlay,colour,rect(0,4,2,5,-2.02F),47.25F,30.25F,47.75F,30.75F,0,0,-1);
        p.popPose();
    }
    private static float[][] rect(float x0,float y0,float x1,float y1,float z){return new float[][]{{x0,y1,z},{x1,y1,z},{x1,y0,z},{x0,y0,z}};}
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
