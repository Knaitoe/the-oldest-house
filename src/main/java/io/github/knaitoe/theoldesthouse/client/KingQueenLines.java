package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.King;
import io.github.knaitoe.theoldesthouse.labyrinth.KingCrownItem;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * 0.4.648, A sort of thing, part 8 (labyrinth.KingFeast, docs/SLEEPING_KING_0_4_648.md): the Queen's move. A reader wearing their own
 * crown in the King's country sees eight ruled lines run out from their feet across the ground, the eight ways a queen moves. Along them
 * nothing stops a queen (KingHedgeBlock, KingVergeBlock let a crowned reader through). One of the eight is turned to point at him; it is
 * drawn harder than the rest and ends a little short of where he lies, by his tree. The rest thin out as they go. Each is ruled: a
 * straight pencil line with a short tick across it every two blocks, like a rule's marks. Only the wearer sees their own lines.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class KingQueenLines {
    private KingQueenLines(){}
    /** How far the seven other lines run before they have thinned to nothing; how far short of him his line ends; the ticks' spacing and half-width. */
    static final double REACH=36,SHORT=2,TICK=2,BAR=.12;
    /** How near the King must be for the lines to be drawn at all: in his country and no further. */
    static final double COUNTRY=160;
    /** The pencil, as the King's people are drawn (KingClient), a little lighter for the seven. */
    private static final int R=58,G=54,B=50;

    @SubscribeEvent public static void draw(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
        var mc=Minecraft.getInstance();var p=mc.player;var level=mc.level;
        if(p==null||level==null||!KingCrownItem.crowned(p))return;
        LiteraryActor king=null;double best=COUNTRY*COUNTRY;
        for(var en:level.entitiesForRendering())if(en instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&a.phase()==King.PHASE){double d=a.distanceToSqr(p);if(d<best){best=d;king=a;}}
        if(king==null)return;
        float partial=e.getPartialTick().getGameTimeDeltaPartialTick(false);
        var feet=p.getPosition(partial);var cam=e.getCamera().getPosition();
        double dx=king.getX()-feet.x,dz=king.getZ()-feet.z,toHim=Math.sqrt(dx*dx+dz*dz),toward=Math.atan2(dz,dx);
        var buffers=mc.renderBuffers().bufferSource();var out=buffers.getBuffer(RenderType.lines());
        var pose=e.getPoseStack();pose.pushPose();
        // A hair above the ground the reader stands on, so the ground does not swallow the pencil.
        pose.translate(feet.x-cam.x,feet.y-cam.y+.03,feet.z-cam.z);
        lines(out,pose.last(),toward,toHim);
        pose.popPose();buffers.endBatch(RenderType.lines());
    }

    /** The eight lines from the feet (at the pose's origin, on its ground plane): the first toward him, ending short of him. */
    static void lines(VertexConsumer out,PoseStack.Pose at,double toward,double toHim){
        for(int i=0;i<8;i++){
            double a=toward+i*Math.PI/4;boolean his=i==0;
            ray(out,at,Math.cos(a),Math.sin(a),his?Math.max(0,toHim-SHORT):REACH,his);
        }
    }

    /** One ruled line from the feet: its length along (x,z), drawn hard to its end if it is his, thinning out to nothing if not. */
    private static void ray(VertexConsumer out,PoseStack.Pose at,double x,double z,double length,boolean his){
        if(length<.5)return;
        // In short lengths, so a line can thin along its run; the first half block is left clear under the reader's own feet.
        double step=.5;
        for(double s=.5;s<length;s+=step){
            double t=Math.min(length,s+step);
            line(out,at,x*s,z*s,x*t,z*t,alpha(s,length,his));
        }
        // The rule's ticks: short bars across the line every two blocks.
        for(double s=TICK;s<length;s+=TICK){
            int al=alpha(s,length,his);double cx=x*s,cz=z*s,px=-z*BAR,pz=x*BAR;
            line(out,at,cx-px,cz-pz,cx+px,cz+pz,al);
        }
    }
    private static int alpha(double s,double length,boolean his){
        if(his)return 235;
        return (int)(200*Mth.clamp(1-s/length,0,1));
    }
    private static void line(VertexConsumer out,PoseStack.Pose at,double x0,double z0,double x1,double z1,int alpha){
        if(alpha<=4)return;
        float dx=(float)(x1-x0),dz=(float)(z1-z0),len=Mth.sqrt(dx*dx+dz*dz);if(len<1e-5F)return;dx/=len;dz/=len;
        out.addVertex(at,(float)x0,0,(float)z0).setColor(R,G,B,alpha).setNormal(at,dx,0,dz);
        out.addVertex(at,(float)x1,0,(float)z1).setColor(R,G,B,alpha).setNormal(at,dx,0,dz);
    }
}
