package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Shared child gait and connected upper joints, in the native humanoid's model coordinates. */
public final class HumanoidMotion {
    public record Motion(float phase,float amount,boolean running){}
    private static final WeakHashMap<LivingEntity,Sample> samples=new WeakHashMap<>();
    private static final class Sample {Vec3 at;double time;float phase,speed;Sample(Vec3 at,double time){this.at=at;this.time=time;}}
    private HumanoidMotion(){}
    /** Scripted moveTo actors still animate from interpolated distance actually covered by this client. */
    public static Motion sample(LivingEntity e,float partial){
        var at=e.getPosition(partial);double time=e.level().getGameTime()+partial;var s=samples.computeIfAbsent(e,k->new Sample(at,time));
        double dt=time-s.time,travel=Math.sqrt(Math.pow(at.x-s.at.x,2)+Math.pow(at.z-s.at.z,2));
        if(dt>0){
            if(dt<10&&travel<1.5){s.phase+=travel*6.24F;s.speed=(float)(travel/dt);}
            else s.speed=0;s.at=at;s.time=time;
        }
        return new Motion(s.phase,Mth.clamp(s.speed/.12F,0,1),s.speed>.13F);
    }
    public static void reset(HumanoidModel<?> m){
        for(var p:new ModelPart[]{m.head,m.hat,m.body,m.leftArm,m.rightArm,m.leftLeg,m.rightLeg})p.getAllParts().forEach(ModelPart::resetPose);
    }
    public static void gait(HumanoidModel<?> m,float phase,float amount,boolean running){
        float a=Mth.clamp(amount,0,1),s=Mth.cos(phase),stride=running?1.15F:.75F;
        m.rightLeg.xRot=s*stride*a;m.leftLeg.xRot=-s*stride*a;
        m.rightArm.xRot=-s*(running?1F:.55F)*a;m.leftArm.xRot=-m.rightArm.xRot;
        m.rightArm.zRot=.06F;m.leftArm.zRot=-.06F;
        m.body.xRot+=(running?.22F:.045F)*a;m.body.zRot=Mth.sin(phase)*.035F*a;
        float bob=-Mth.abs(Mth.sin(phase))*(running?.55F:.2F)*a;
        for(var p:new ModelPart[]{m.head,m.body,m.leftArm,m.rightArm,m.leftLeg,m.rightLeg})p.y+=bob;
    }
    /** Bend about the hips; the neck and shoulder pivots inherit the same torso transform. */
    public static void connect(HumanoidModel<?> m){
        float x=(m.leftLeg.x+m.rightLeg.x)/2,y=(m.leftLeg.y+m.rightLeg.y)/2,z=(m.leftLeg.z+m.rightLeg.z)/2;
        var torso=new Quaternionf().rotationZYX(m.body.zRot,m.body.yRot,m.body.xRot);
        place(m.body,torso,x,y,z,0,-12,0,false);
        place(m.head,torso,x,y,z,0,-12,0,true);
        place(m.rightArm,torso,x,y,z,m.rightArm.x,-10,0,true);
        place(m.leftArm,torso,x,y,z,m.leftArm.x,-10,0,true);
        m.hat.copyFrom(m.head);
        if(m instanceof net.minecraft.client.model.PlayerModel<?> player){player.jacket.copyFrom(m.body);player.leftSleeve.copyFrom(m.leftArm);player.rightSleeve.copyFrom(m.rightArm);player.leftPants.copyFrom(m.leftLeg);player.rightPants.copyFrom(m.rightLeg);}
    }
    private static void place(ModelPart p,Quaternionf q,float x,float y,float z,float dx,float dy,float dz,boolean inherit){
        var v=q.transform(new Vector3f(dx,dy,dz));p.setPos(x+v.x,y+v.y,z+v.z);
        if(inherit){var r=new Quaternionf(q).mul(new Quaternionf().rotationZYX(p.zRot,p.yRot,p.xRot)).getEulerAnglesZYX(new Vector3f());p.xRot=r.x;p.yRot=r.y;p.zRot=r.z;}
    }
}
