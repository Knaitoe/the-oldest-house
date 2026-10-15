package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

/** Keep the unchanged lower-face islands on the jaw; its opening must not repeat the eyes. */
final class LakeWitchJaw {
    private LakeWitchJaw(){}
    static void render(ModelPart head,ModelPart jaw,float inflation,float uOffset,PoseStack p,VertexConsumer out,int light,int overlay,int colour){
        if(!head.visible||!jaw.visible)return;
        p.pushPose();head.translateAndRotate(p);jaw.translateAndRotate(p);
        float x0=-4-inflation,x1=4+inflation,y0=-inflation,y1=2+inflation,z0=-7-inflation,z1=1+inflation;
        quad(p,out,new float[][]{{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0}},uOffset+8,14,uOffset+16,16,0,0,-1,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z1},{x0,y0,z1},{x0,y1,z1},{x1,y1,z1}},uOffset+24,14,uOffset+32,16,0,0,1,light,overlay,colour);
        quad(p,out,new float[][]{{x0,y0,z1},{x0,y0,z0},{x0,y1,z0},{x0,y1,z1}},uOffset,14,uOffset+8,16,-1,0,0,light,overlay,colour);
        quad(p,out,new float[][]{{x1,y0,z0},{x1,y0,z1},{x1,y1,z1},{x1,y1,z0}},uOffset+16,14,uOffset+24,16,1,0,0,light,overlay,colour);
        // Shaded lower-face pigment, not the old forehead/eye island, is the inner mouth.
        int dark=(colour&0xff000000)|((colour>>>16&255)/4<<16)|((colour>>>8&255)/4<<8)|(colour&255)/4;
        quad(p,out,new float[][]{{x0,y0,z1},{x0,y0,z0},{x1,y0,z0},{x1,y0,z1}},uOffset+11.5F,15.2F,uOffset+11.55F,15.25F,0,-1,0,light,overlay,dark);
        quad(p,out,new float[][]{{x0,y1,z0},{x0,y1,z1},{x1,y1,z1},{x1,y1,z0}},uOffset+8,15,uOffset+16,16,0,1,0,light,overlay,colour);
        p.popPose();
    }
    private static void quad(PoseStack p,VertexConsumer out,float[][] points,float u0,float v0,float u1,float v1,float nx,float ny,float nz,int light,int overlay,int colour){
        float[][] uv={{u0,v0},{u1,v0},{u1,v1},{u0,v1}};
        for(int i=0;i<4;i++)out.addVertex(p.last(),points[i][0]/16,points[i][1]/16,points[i][2]/16).setColor(colour).setUv(uv[i][0]/64,uv[i][1]/64).setOverlay(overlay).setLight(light).setNormal(p.last(),nx,ny,nz);
    }
}
