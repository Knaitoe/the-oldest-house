package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.RiddenBike;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * 0.4.519, somebody who is not a reader riding a bike: Bike night's three and Gus. The owner, playing it on 12 November: "the
 * animation for the boys on their bikes needs to be fixed". They had been carried as a reader is, which for them was a third of a
 * block too high (RiddenBike.getPassengerAttachmentPoint), so they stood upright on the saddle with their legs swinging out in front
 * of the bars. Now their hips are on the saddle, and here: they lean a little from the hips toward the bars, a hand on each grip;
 * each foot goes round with its own pedal; and the whole of them leans with the bike, about the line its wheels touch the road, as a
 * reader does (BikeClient).
 *
 * <p>A limb of this model has no knee and no elbow, so to put a foot on a pedal that has come up close, or gone down far, the leg is
 * shortened or lengthened a little (SHORTEST, LONGEST); likewise an arm to its grip. The numbers are the bikes' as
 * tools/draw_bike_night_art.py draws them, in the bikes' pixels, and the tool checks them against its drawing.
 */
public final class BikeRiderPose {
    private BikeRiderPose(){}
    /** The cranks (cranks()): each pedal's middle from the axle, along its crank, and out to its side. */
    public static final float PEDAL_DROP=2.625F,PEDAL_OUT=3.25F;
    /** The grips of the frame the three's bikes and Gus's have (bmx(), the Wasp's), from the bike's middle: ahead, up from the road, and out to each side. */
    public static final float GRIP_AHEAD=4.5F,GRIP_UP=14.9F,GRIP_OUT=3.6F;
    /** How far forward a rider leans from the hips, in radians; and how far out from the middle each leg is at the hip, in the model's pixels, so that they sit astride the frame. */
    public static final float LEAN=.3F,STRADDLE=2.3F;
    /** The least and most a leg or an arm is drawn at of its own length, to reach its pedal or its grip; and how far along an arm the hand holds. */
    public static final float SHORTEST=.78F,LONGEST=1.2F,HAND=9.5F;

    /**
     * Poses the model of somebody on a bike: their size; how far above the road their feet are, in the bike's pixels; where its crank
     * axle is (its pixels from the frame's back and the road); and how far its cranks have turned, in radians.
     */
    public static void pose(HumanoidModel<?> m,float size,float feet,float axleX,float axleY,float crank){
        float s=Math.max(.1F,size),c=Mth.cos(LEAN),sn=Mth.sin(LEAN);
        // The body leans forward from the hips, which stay where the legs meet it: it turns about its top, so the top comes forward.
        m.body.xRot=LEAN;m.body.yRot=m.body.zRot=0;m.body.x=0;m.body.y=12-12*c;m.body.z=-12*sn;
        m.head.x=0;m.head.y=m.body.y;m.head.z=m.body.z;
        for(int side=-1;side<=1;side+=2){
            // -1 is the rider's right (the model's -x, the bike's +z), 1 their left.
            var arm=side<0?m.rightArm:m.leftArm;
            arm.x=5*side;arm.y=m.body.y+2*c;arm.z=m.body.z+2*sn;
            reach(arm,model(GRIP_AHEAD,GRIP_UP,-side*GRIP_OUT,feet,s),HAND,false);
            // The right pedal is up while the cranks have not turned; each goes round clockwise as it is seen from the right.
            var leg=side<0?m.rightLeg:m.leftLeg;
            leg.x=STRADDLE*side;leg.y=12;leg.z=0;
            float along=side<0?PEDAL_DROP:-PEDAL_DROP;
            reach(leg,model(axleX-8+along*Mth.sin(crank),axleY+along*Mth.cos(crank),-side*PEDAL_OUT,feet,s),12,true);
        }
        m.hat.copyFrom(m.head);
    }
    /** A point of the bike in its pixels (ahead of its middle, up from the road, out to its right), in the rider's model's pixels. */
    static float[] model(float ahead,float up,float right,float feet,float s){return new float[]{-right/s,24-(up-feet)/s,-ahead/s};}
    /**
     * Turns a limb to point from where it hangs at a place, and draws it as long as the way there is (within SHORTEST and LONGEST of
     * its own length). A leg turns forward and out from the hip, which leaves its front facing forward; an arm, reaching forward,
     * swings out about the upright instead.
     */
    static void reach(ModelPart p,float[] to,float length,boolean leg){
        float dx=to[0]-p.x,dy=to[1]-p.y,dz=to[2]-p.z,far=Mth.sqrt(dx*dx+dy*dy+dz*dz);
        if(far<1e-3F)return;
        if(leg){p.xRot=(float)Math.asin(Mth.clamp(dz/far,-1,1));p.yRot=0;p.zRot=(float)Math.atan2(-dx,dy);}
        else{p.xRot=-(float)Math.acos(Mth.clamp(dy/far,-1,1));p.yRot=(float)Math.atan2(-dx,-dz);p.zRot=0;}
        p.yScale=Mth.clamp(far/length,SHORTEST,LONGEST);
    }

    /** The bike's yaw as it is drawn this frame. */
    public static float yaw(RiddenBike b,float partial){return Mth.rotLerp(partial,b.yRotO,b.getYRot());}
    /** How far above the road a rider's feet are this frame, in the bike's pixels. */
    public static float feet(LiteraryActor a,RiddenBike b,float partial){return (float)(Mth.lerp(partial,a.yo,a.getY())-Mth.lerp(partial,b.yo,b.getY()))*16;}
    /**
     * Leans a rider with their bike, about the line its wheels touch the road, as BikeClient leans a reader. The pose is at the rider's
     * feet and already scaled by their size, as LivingEntityRenderer has it when it turns a body.
     */
    public static void lean(PoseStack pose,LiteraryActor a,RiddenBike b,float partial,float size){
        float yaw=yaw(b,partial),up=feet(a,b,partial)/16/Math.max(.1F,size);
        pose.translate(0,-up,0);pose.mulPose(Axis.YP.rotationDegrees(-(yaw+90)));pose.mulPose(Axis.XP.rotationDegrees(b.shownLean(partial)*RiddenBike.LEAN_DEGREES));
        pose.mulPose(Axis.YP.rotationDegrees(yaw+90));pose.translate(0,up,0);
    }
}
