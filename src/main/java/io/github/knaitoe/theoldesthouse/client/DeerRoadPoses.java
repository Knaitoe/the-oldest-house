package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.DeerRoad;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;

/**
 * 0.4.310, A Deer in the Road: how the woman's body is carried on the county road, over and above the tunnel's bent pose.
 * <ul>
 * <li>Out of the doe: everything still under the doe is under the road surface, so the asphalt hides it. Prone, the hands first
 * on the centre line (right, then left), the shoulders and the nightgown, the head coming up on its neck, the brace on the carcass,
 * and the rise, past where a body ought to stop.</li>
 * <li>Out of the bench: the head first through the split seam, turned sideways; then doubled over toward the rear door, under the
 * roof, until the scene stands her outside it.</li>
 * <li>The fall, and the two deaths: sat against the driver's door with her legs out on the road, or face down on it.</li>
 * </ul>
 * Progress comes from the client's own count since her appearance last changed: a late joiner sees the movement from its start.
 */
public final class DeerRoadPoses {
    private record Since(int phase,int tick){}
    private static final WeakHashMap<LiteraryActor,Since> SINCE=new WeakHashMap<>();
    private DeerRoadPoses(){}
    static float duration(int phase){return phase==DeerRoad.EMERGE?DeerRoad.BIRTH1_TICKS:phase==DeerRoad.SEAT?DeerRoad.BIRTH2_TICKS:phase==DeerRoad.FALL?DeerRoad.FALL_TICKS:1;}
    public static float progress(LiteraryActor a,float partial){
        var s=SINCE.get(a);if(s==null||s.phase!=a.phase()){s=new Since(a.phase(),a.tickCount);SINCE.put(a,s);}
        return Mth.clamp((a.tickCount+partial-s.tick)/duration(a.phase()),0,1);
    }
    static float span(float p,float a,float b){return Mth.clamp((p-a)/(b-a),0,1);}
    static float ease(float t){return t*t*(3-2*t);}
    /** Sinks the whole body (blocks) and tips it forward ({@code pitch}) and to one side ({@code roll}) about her feet. */
    public static void body(LiteraryActor a,PoseStack p,float t,float scale){
        float sink=0,pitch=0,roll=0;
        switch(a.phase()){
            case DeerRoad.EMERGE->{
                if(t<.15F){sink=4;pitch=90;}
                else if(t<.42F){sink=Mth.lerp(ease(span(t,.15F,.42F)),1.05F,.62F);pitch=90;}
                else if(t<.6F){sink=Mth.lerp(ease(span(t,.42F,.6F)),.62F,.12F);pitch=Mth.lerp(span(t,.42F,.6F),90,82);}
                else if(t<.72F){sink=.12F;pitch=82;}
                else if(t<.85F){float q=ease(span(t,.72F,.85F));sink=Mth.lerp(q,.12F,0);pitch=Mth.lerp(q,82,42);}
                else{float q=ease(span(t,.85F,1));pitch=Mth.lerp(q,42,3);}
            }
            case DeerRoad.SEAT->{
                if(t<.3F){sink=5;}
                else if(t<.55F){sink=Mth.lerp(ease(span(t,.3F,.55F)),2.25F,1.55F);roll=-8;}
                else{float q=ease(span(t,.55F,1));sink=Mth.lerp(q,1.55F,.75F);pitch=Mth.lerp(q,10,52);roll=Mth.lerp(q,-8,-24);}
            }
            case DeerRoad.FALL->{float q=ease(t);if(a.gesture()==2){sink=q*1.15F;pitch=-q*16;}else{pitch=q*88;sink=-q*.18F;}}
            case DeerRoad.DEAD_DOOR,DeerRoad.DEAD_DOOR_OPEN->{sink=1.15F;pitch=-16;roll=6;}
            case DeerRoad.DEAD_ROAD->{pitch=88;sink=-.18F;roll=4;}
            default->{}
        }
        if(sink!=0)p.translate(0,-sink/scale,0);
        if(pitch!=0)p.mulPose(Axis.XP.rotationDegrees(-pitch));
        if(roll!=0)p.mulPose(Axis.ZP.rotationDegrees(roll));
    }
    /** The limbs, in her natural proportions, before she is drawn at her height. */
    public static void limbs(UncannyActorRenderer.Model m,int phase,float t){
        switch(phase){
            case DeerRoad.EMERGE->{
                // Overhead is forward, prone: the right hand reaches out onto the road first, then the left.
                float right=ease(span(t,.15F,.3F)),left=ease(span(t,.3F,.42F)),brace=ease(span(t,.72F,.85F)),up=ease(span(t,.85F,1));
                m.rightArm.xRot=Mth.lerp(right,0,-2.85F);m.leftArm.xRot=Mth.lerp(left,0,-2.85F);
                if(brace>0){m.rightArm.xRot=Mth.lerp(brace,-2.85F,-1.55F);m.leftArm.xRot=Mth.lerp(brace,-2.85F,-.6F);m.leftArm.zRot=-.5F*brace;}
                if(up>0){m.rightArm.xRot=Mth.lerp(up,-1.55F,-.12F);m.leftArm.xRot=Mth.lerp(up,-.6F,-.12F);m.leftArm.zRot=Mth.lerp(up,-.5F,-.04F);}
                float knee=brace*(1-up);m.rightLeg.xRot=-1.3F*knee;m.leftLeg.xRot=-.2F*knee;
                // Face down until the head comes up on its neck.
                if(t<.6F)m.head.xRot=1.2F;else if(t<.72F)m.head.xRot=Mth.lerp(span(t,.6F,.72F),1.2F,m.head.xRot);
            }
            case DeerRoad.SEAT->{m.head.yRot=1.35F;m.head.zRot=1.2F;m.rightArm.xRot=-2.4F;m.rightArm.zRot=.3F;m.leftArm.xRot=-.6F;m.rightLeg.xRot=-1.2F;m.leftLeg.xRot=-1.2F;}
            case DeerRoad.FALL->{if(t>.3F){m.rightLeg.xRot=-1.5F*span(t,.3F,1);m.leftLeg.xRot=-1.4F*span(t,.3F,1);}m.rightArm.zRot=.3F*t;m.leftArm.zRot=-.35F*t;}
            case DeerRoad.DEAD_DOOR,DeerRoad.DEAD_DOOR_OPEN->{m.rightLeg.xRot=-1.5F;m.leftLeg.xRot=-1.35F;m.leftLeg.yRot=-.2F;m.rightArm.xRot=-.1F;m.rightArm.zRot=.35F;m.leftArm.xRot=.05F;m.leftArm.zRot=-.45F;m.head.xRot=.6F;}
            case DeerRoad.DEAD_ROAD->{m.rightArm.xRot=-2.9F;m.rightArm.zRot=.2F;m.leftArm.xRot=-.2F;m.leftArm.zRot=-.25F;m.leftLeg.zRot=-.12F;m.head.yRot=1.1F;}
            case DeerRoad.HELD->{m.rightArm.xRot=-.05F;m.leftArm.xRot=-.05F;}
            default->{}
        }
    }
}
