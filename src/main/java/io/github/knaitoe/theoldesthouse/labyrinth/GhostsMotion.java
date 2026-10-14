package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/** The five-tick script selects targets; original cast bodies move through collision every native tick. */
public final class GhostsMotion {
    private static final WeakHashMap<LiteraryActor,Motion> moves=new WeakHashMap<>();
    private static final WeakHashMap<LiteraryActor,Look> looks=new WeakHashMap<>();
    private static final class Motion {
        Vec3 target;double speed,fall;long until;boolean falling;
        Motion(Vec3 target,double speed,long until){this.target=target;this.speed=speed;this.until=until;}
    }
    private record Look(Vec3 at,long until){}
    private GhostsMotion(){}
    static void toward(LiteraryActor actor,Vec3 target,double fiveTickDistance){
        moves.put(actor,new Motion(target,fiveTickDistance/5,actor.level().getGameTime()+6));
    }
    static void look(LiteraryActor actor,Vec3 target){looks.put(actor,new Look(target,actor.level().getGameTime()+6));}
    static void fall(LiteraryActor actor,double floor){
        var motion=moves.get(actor);
        if(motion==null||!motion.falling){motion=new Motion(new Vec3(actor.getX(),floor,actor.getZ()),0,0);motion.falling=true;moves.put(actor,motion);}
        motion.until=actor.level().getGameTime()+6;
    }
    public static void stop(LiteraryActor actor){moves.remove(actor);looks.remove(actor);actor.setDeltaMovement(Vec3.ZERO);}
    public static void tick(LiteraryActor actor){
        if(actor.role()<LiteraryActor.GHOST_MARJORIE||actor.role()>LiteraryActor.GHOST_PRIEST||actor.owner().isEmpty())return;
        var player=actor.level().getPlayerByUUID(actor.owner().get());long now=actor.level().getGameTime();
        if(player==null||!player.isAlive()||player.isSpectator()){stop(actor);return;}
        if(GhostsJump.tick(actor))return;
        var move=moves.get(actor);
        if(move!=null){
            if(now>move.until){moves.remove(actor);}
            else{
                Vec3 delta=move.target.subtract(actor.position());
                if(move.falling){move.fall=Math.min(.8,move.fall+.08);delta=new Vec3(0,-Math.min(move.fall,Math.max(0,actor.getY()-move.target.y)),0);}
                else if(delta.length()>move.speed)delta=delta.normalize().scale(move.speed);
                actor.move(MoverType.SELF,delta);actor.setDeltaMovement(Vec3.ZERO);
                if(!move.falling&&delta.horizontalDistanceSqr()>.000001){
                    float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));
                    actor.setYRot(Mth.approachDegrees(actor.getYRot(),yaw,9));actor.setYBodyRot(actor.getYRot());actor.setYHeadRot(actor.getYRot());
                }
                if(actor.position().distanceToSqr(move.target)<.000025)moves.remove(actor);
            }
        }
        var look=looks.get(actor);
        if(look!=null){
            if(now>look.until)looks.remove(actor);
            else{var delta=look.at.subtract(actor.getEyePosition());actor.setYHeadRot(Mth.approachDegrees(actor.getYHeadRot(),(float)Math.toDegrees(Math.atan2(-delta.x,delta.z)),7));actor.setXRot(Mth.lerp(.07F,actor.getXRot(),(float)-Math.toDegrees(Math.atan2(delta.y,Math.max(.01,delta.horizontalDistance())))));}
        }
    }
}
