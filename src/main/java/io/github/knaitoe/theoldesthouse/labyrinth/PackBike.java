package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.479, Bike night: a bike ridden by somebody who is not a reader. The three who come to the park ride these
 * (BikeNightBullies); Gus will. The proposal: "How they ride: on the same bike physics, simpler."
 *
 * <p>It is drawn as any bike is, and it keeps to the ground as a reader's does: it falls, rolls up a block, and rides a ramp's or
 * the chute's sheet (BikeSurface). What is simpler is the riding. It goes where it is told ({@link #toward}), at the speed it is
 * told, turning no faster than a bike can and slowing into a sharp turn. It has no balance to keep, and nobody comes off it. With
 * nobody on it, it stands where it was left.
 *
 * <p>It is never saved and is nobody's to take: a hand on one of the three's does nothing, and a hand on Gus's gets a word from him.
 *
 * <p>0.4.509: one may lie on its side where it was dropped ({@link #lay}). It goes nowhere, nobody gets on it, and a wheel of it
 * may still be going round when somebody comes on it.
 */
public final class PackBike extends RiddenBike {
    private static final EntityDataAccessor<Integer> LOOK=SynchedEntityData.defineId(PackBike.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LAID=SynchedEntityData.defineId(PackBike.class,EntityDataSerializers.INT);
    /**
     * 0.4.509, lying on its side: how far over it is drawn, in degrees, and how far above the ground the line it turns about, so
     * that its lower pedal and the end of its bars are what rest on the ground; how fast its wheels may still be going round, in
     * blocks a tick; what that loses in each tick somebody is near it; and how near that is, in blocks.
     */
    public static final float LAID_DEGREES=80,LAID_LIFT=.16F,SPIN=.05F,SPIN_EASE=.00035F;
    /** 0.4.510, leaning on something beside it: how far over it is drawn, in degrees. */
    public static final float PROPPED_DEGREES=14;
    public static final double SPIN_NEAR=14;
    /** The looks: the three who come to the park; Gus's (0.4.483); from MAKES up, one of Bike night's six makes; (0.4.510) from RUSTED up, one of the six left out and rusted. */
    public static final int TULLY=0,RAY=1,DUFF=2,GUS=3,MAKES=10,RUSTED=20;
    /** Degrees it turns in a tick; what it gains and loses of its speed in a tick; how far round it still has to come before it slows for the turn, and to how much. */
    public static final float TURN=9,GAIN=.02F,EASE=.03F,SHARP=50,SHARP_SPEED=.55F;
    /** Ticks of going nowhere before it counts as held up; and how far it rolls for each half turn of its cranks. */
    public static final int HELD=12;public static final double STROKE=1.1;
    private @Nullable Vec3 toward;private float wanted;private double rolled;private int side=1,held;

    public PackBike(EntityType<? extends PackBike> type,Level level){super(type,level);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(LOOK,0);b.define(LAID,0);}
    public int look(){return entityData.get(LOOK);}
    public void look(int look){entityData.set(LOOK,look);}
    /** 0.4.509: it lies on that side (-1 its left, 1 its right) where it is, its wheels going round that fast, and is not ridden again. */
    public void lay(int side,float spin){entityData.set(LAID,side<0?-1:1);laidSpin=spin;ejectPassengers();}
    /** 0.4.510: it leans that way (-1 its left, 1 its right) on what is beside it, where it is, and is not ridden again. */
    public void prop(int side){entityData.set(LAID,side<0?-2:2);laidSpin=0;ejectPassengers();}
    /** The side it lies on (-1, 1) or leans to (-2, 2), or nothing if it stands. */
    public int laid(){return entityData.get(LAID);}
    private float laidSpin;
    @Override protected Stats stats(){return Stats.PLAIN;}
    @Override protected void parkAs(ServerLevel l,@Nullable BlockPos at,boolean lying){}
    @Override protected boolean takes(Entity e){return laid()==0&&e instanceof LiteraryActor;}
    /** Gus's, standing where he left it, can be touched: he says whose it is. Nobody else's can, and nobody takes any of them. */
    @Override public boolean isPickable(){return !isRemoved()&&look()==GUS&&getPassengers().isEmpty();}
    @Override public boolean canBeHitByProjectile(){return false;}
    @Override public InteractionResult interact(Player p,InteractionHand hand){
        if(look()!=GUS||!getPassengers().isEmpty())return InteractionResult.PASS;
        if(level().isClientSide)return InteractionResult.SUCCESS;
        if(hand==InteractionHand.MAIN_HAND&&p instanceof ServerPlayer reader)BikeNightGus.hisBike(reader);
        return InteractionResult.CONSUME;
    }

    /**
     * 0.4.507: set down this tick where it is told, pointing as it is told and turning its cranks at the speed it is told, and
     * not ridden there: over ground it has no way of riding (it has no hop, and the Hollow is all gaps).
     */
    public void carry(Vec3 to,float yaw,float shown){carried=to;carriedYaw=yaw;carriedSpeed=shown;}
    private @Nullable Vec3 carried;private float carriedYaw,carriedSpeed;
    /** Where it is to go, in the world, and how fast. With nowhere to go it rolls to a stop. */
    public void toward(@Nullable Vec3 to,float speed){toward=to;wanted=to==null?0:speed;}
    /** Whether it has been trying to go and getting nowhere: something is in its way. */
    public boolean heldUp(){return held>HELD;}

    @Override protected void driven(){
        if(laid()!=0){
            // Dropped: it lies where it is. Its wheels run down once somebody is near enough to have seen them going.
            setDeltaMovement(Vec3.ZERO);toward=null;wanted=0;held=0;carried=null;
            if(laidSpin>0&&level().getNearestPlayer(this,SPIN_NEAR)!=null)laidSpin=Math.max(0,laidSpin-SPIN_EASE);
            if(laidSpin>STOPPED&&tickCount%14==0)level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.FREEWHEEL.get(),SoundSource.NEUTRAL,.25F,.8F);
            show(0,laidSpin);return;
        }
        if(carried!=null){
            double went=Math.hypot(carried.x-getX(),carried.z-getZ());
            setYRot(carriedYaw);setPos(carried.x,carried.y,carried.z);setDeltaMovement(Vec3.ZERO);setOnGround(true);resetFallDistance();
            speed=carriedSpeed;lean*=.7F;toward=null;wanted=0;held=0;carried=null;
            rolled+=went;if(rolled>=STROKE){rolled=0;side=-side;stroke(side);}
            if(speed>STOPPED&&tickCount%14==0)level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.FREEWHEEL.get(),SoundSource.NEUTRAL,.3F,.9F+Math.min(.5F,speed));
            show(lean,speed);return;
        }
        boolean grounded=onGround(),going=toward!=null&&wanted>0&&!getPassengers().isEmpty();float want=0;
        if(going){
            double dx=toward.x-getX(),dz=toward.z-getZ();
            if(dx*dx+dz*dz>.01)want=Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(-dx,dz))-getYRot());else going=false;
        }
        float turn=going?Mth.clamp(want,-TURN,TURN):0;
        float goal=going?wanted*(Math.abs(want)>SHARP?SHARP_SPEED:1):0;
        speed=speed<goal?Math.min(goal,speed+GAIN):Math.max(goal,speed-EASE);
        setYRot(getYRot()+turn);
        // It leans into its turns, for the look of it: nothing depends on the lean.
        lean+=(Mth.clamp(turn/TURN,-1,1)*.4F*Math.min(1,speed/TOP)-lean)*.3F;
        double rad=Math.toRadians(getYRot()),x0=getX(),z0=getZ();
        double fall=grounded?-.08:getDeltaMovement().y-.08;
        move(MoverType.SELF,new Vec3(-Math.sin(rad)*speed,fall,Math.cos(rad)*speed));
        // A ramp or the chute: on the sheet itself, as a reader's bike is.
        double top=sheetUnder();
        if(!Double.isNaN(top)){double gap=top-getY();if(gap>=0||(grounded&&gap>-STICK&&!onGround())){setPos(getX(),top,getZ());setOnGround(true);}}
        setDeltaMovement(0,onGround()?0:Math.max(-2,fall*.98),0);
        double went=Math.hypot(getX()-x0,getZ()-z0);
        held=speed>.05F&&went<speed*.3?held+1:0;
        if(horizontalCollision)speed*=.6F;
        rolled+=went;if(rolled>=STROKE){rolled=0;side=-side;stroke(side);}
        if(speed>STOPPED&&tickCount%14==0)level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.FREEWHEEL.get(),SoundSource.NEUTRAL,.3F,.9F+Math.min(.5F,speed));
        show(lean,speed);
    }
}
