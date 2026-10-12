package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryRooms;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Thin tape tracing a fallen deer's flank, head, antlers and four legs, private to the actual return. */
public final class ElkFanOutline {
    private static final ResourceLocation TAPE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private static final double[][] BODY={{-.4,-1.1},{-.75,-.65},{-.75,.4},{-.5,.85},{-.1,1.05},{.4,.9},{.75,.5},{.8,-.3},{.55,-.75},{.3,-1.05},{.25,-1.65},{.1,-1.95},{-.15,-1.92},{-.35,-1.7},{-.4,-1.1}};
    private static final double[][][] LIMBS={
        {{-.6,-.45},{-1.2,-.9},{-1.75,-.7},{-1.7,-.5},{-1.25,-.65},{-.75,-.2}},
        {{-.65,.2},{-1.1,.8},{-1.65,1.05},{-1.55,1.25},{-.95,1},{-.45,.75}},
        {{.65,-.45},{1.25,-.8},{1.8,-.45},{1.7,-.2},{1.2,-.55},{.75,-.15}},
        {{.65,.35},{1.3,.75},{1.7,1.2},{1.45,1.3},{1.1,.9},{.4,.8}},
        {{-.18,-1.75},{-.7,-2.15},{-1.1,-2.1},{-.9,-2.32},{-.72,-2.15}},
        {{.12,-1.82},{.6,-2.25},{.95,-2.2},{.75,-2.4},{.6,-2.25}},
        {{-.65,.62},{-.95,.75},{-.72,.82}}
    };
    private ElkFanOutline() {}
    public static void render(BlockPos fan,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var view=LiteraryView.models();int phase=view.getInt("FanOutline");if(phase<=0||fan.asLong()!=BlockPos.of(view.getLong("FanBase")).offset(LiteraryRooms.FAN).asLong())return;
        draw(phase,pose,out,light,overlay);
    }
    public static void draw(int phase,PoseStack pose,MultiBufferSource out,int light,int overlay){
        var v=out.getBuffer(RenderType.entityCutoutNoCull(TAPE));int colour=phase==1?0xffc5b791:0xff645d53;
        pose.pushPose();pose.translate(.5,-LiteraryRooms.FAN.getY()+.018,.5);line(BODY,pose,v,light,overlay,colour,phase==1?.035:.025);
        for(var limb:LIMBS)line(limb,pose,v,light,overlay,colour,phase==1?.035:.025);pose.popPose();
    }
    private static void line(double[][] points,PoseStack pose,VertexConsumer v,int light,int overlay,int colour,double width){
        for(int i=1;i<points.length;i++){var a=points[i-1];var b=points[i];double dx=b[0]-a[0],dz=b[1]-a[1],len=Math.hypot(dx,dz);if(len==0)continue;float x=(float)(-dz/len*width),z=(float)(dx/len*width);
            vertex(v,pose,(float)a[0]+x,(float)a[1]+z,0,0,light,overlay,colour);vertex(v,pose,(float)b[0]+x,(float)b[1]+z,0,1,light,overlay,colour);vertex(v,pose,(float)b[0]-x,(float)b[1]-z,1,1,light,overlay,colour);vertex(v,pose,(float)a[0]-x,(float)a[1]-z,1,0,light,overlay,colour);
        }
    }
    private static void vertex(VertexConsumer v,PoseStack p,float x,float z,float u,float texV,int light,int overlay,int colour){v.addVertex(p.last(),x,0,z).setColor(colour).setUv(u,texV).setOverlay(overlay).setLight(light).setNormal(p.last(),0,1,0);}
}
