package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.435, the owner: "Let players ride bikes? Maintaining balance and pedaling left/right should be a function." A child's bike being
 * ridden. The bike block (BorrascaShopBlock BIKE) becomes this while someone is on it. When they get off or come off, it goes back to
 * being a block: on its kickstand, or lying on its side.
 * <p>
 * Pedalling and balance are one thing. The strafe keys are the pedals, and each press of left or right is one push on that pedal. A push
 * drives the bike on and tips it a little toward that side. Pushing the same pedal twice running drives it on less and tips it much more.
 * Left to itself, a moving bike leans further whichever way it already leans: quickly when it is slow, slowly when it is going well.
 * Turning leans it into the turn, uphill slows it and downhill speeds it. So a reader keeps it up by pedalling in turn and, when it starts
 * to go over, by pushing twice on the pedal on the other side. If it leans all the way over, they come off and the bike lies in the road.
 * Run into a wall going fast and they come off too. Stopped, a foot goes down and nothing falls. The back key brakes. It goes where the
 * rider looks, turning no faster than a bike can, and sneaking gets off.
 * <p>
 * The server decides everything. The client sends its strafe keys as any rider does, and draws the bike from what the server says. The
 * bike is never saved: an unridden bike is always a block, and anything that takes the rider off it (getting off, a fall, a door, a
 * teleport, logging out) puts the block back first.
 */
public final class BorrascaBike extends Entity {
    private static final EntityDataAccessor<Float> LEAN=SynchedEntityData.defineId(BorrascaBike.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED=SynchedEntityData.defineId(BorrascaBike.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> WHOSE=SynchedEntityData.defineId(BorrascaBike.class,EntityDataSerializers.INT);
    /** Which pedal was pushed last (-1 left, 1 right) times how many pushes so far, for the cranks. */
    private static final EntityDataAccessor<Integer> STROKES=SynchedEntityData.defineId(BorrascaBike.class,EntityDataSerializers.INT);
    public static final int PATRICK=0,KENDRA=1;
    /** Blocks a tick: the fastest a child's bike goes on the flat, what a push in turn adds, what a second push on the same pedal adds. */
    public static final float TOP=.32F,TURN_PUSH=.06F,SAME_PUSH=.02F;
    /** How far a push tips the bike toward its own side: in turn, and twice running on the same pedal. A lean of one is over. */
    public static final float TURN_TIP=.06F,SAME_TIP=.22F;
    /** Below STOPPED a foot is down. From STEADY up the bike tips slowest. Into a wall above CRASH, the rider comes off. */
    public static final float STOPPED=.03F,STEADY=.16F,CRASH=.2F;
    /** The most the bike can turn in a tick, in degrees, and how far over it is drawn at a lean of one. */
    public static final float TURN=6,LEAN_DEGREES=38;
    /** The wheels' radius in blocks, for how fast they turn. */
    public static final float WHEEL=5.5F/16F;
    private float lean,speed;private int lastSide,strokes;private boolean leftDown,rightDown,done;
    private @Nullable UUID reader;
    // The client's own drawing state: the wheels and cranks turning, the lean eased, and the server's position eased in.
    private float spin,spinO,crank,crankO,crankTo,shown,shownO;private int seenStrokes,lerpSteps;private double lerpX,lerpY,lerpZ;private float lerpYRot;

    public BorrascaBike(EntityType<? extends BorrascaBike> type,Level level){super(type,level);}

    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(LEAN,0F);b.define(SPEED,0F);b.define(WHOSE,PATRICK);b.define(STROKES,0);}
    // Never saved (the type is noSave): a bike nobody is on is a block.
    @Override protected void readAdditionalSaveData(CompoundTag t){}
    @Override protected void addAdditionalSaveData(CompoundTag t){}

    public int whose(){return entityData.get(WHOSE);}
    public float lean(){return entityData.get(LEAN);}
    public float speed(){return entityData.get(SPEED);}
    public int strokes(){return entityData.get(STROKES);}
    /** The lean the server is working with, between ticks' syncs. For tests. */
    public float leanNow(){return lean;}
    public float speedNow(){return speed;}
    /** Sets the lean and speed outright. For tests. */
    public void steady(float lean,float speed){this.lean=lean;this.speed=speed;}
    public void setUp(ServerPlayer p,int whose,float yaw){reader=p.getUUID();entityData.set(WHOSE,whose);setYRot(yaw);yRotO=yaw;}

    /** The saddle's top above the road, less what a body settles into it. */
    public static final double SADDLE=13.5/16-.03;
    /**
     * Legs straight down toward the pedals, not out in front as on a horse. 0.4.447 (owner: "Try to fix mild visual distortion when
     * mounting bike."): the rider sits on the saddle. They used to be set .3 up less the .6 a player is held at, which put a child's
     * feet at the road, their hips at the cranks and their eyes level with the handlebars and the top of the saddle: the bars filled the
     * top of the view and the saddle's top lay across the middle of it. A body's hips are 12 of its 32 pixels up, so the seat is set
     * for whoever is riding: hips on the saddle, eyes well above the bars, which now sit low in the view where a rider's are.
     */
    @Override public boolean shouldRiderSit(){return false;}
    @Override protected Vec3 getPassengerAttachmentPoint(Entity rider,EntityDimensions d,float scale){
        double s=rider instanceof net.minecraft.world.entity.LivingEntity le?le.getScale():1;
        return new Vec3(0,SADDLE-.675*s+.6*s,0);
    }
    @Override public float maxUpStep(){return 1F;}
    @Override public boolean isPickable(){return false;}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty()&&e instanceof Player;}

    @Override public void tick(){
        super.tick();
        if(level().isClientSide){drawn();return;}
        if(done)return;
        if(!(getFirstPassenger() instanceof ServerPlayer p)){park(null,false);return;}
        // The pedals: a strafe key going down is one push (xxa is positive for left).
        boolean left=p.xxa>.5F,right=p.xxa< -.5F;
        if(left&&!leftDown)pedal(-1);
        if(right&&!rightDown)pedal(1);
        leftDown=left;rightDown=right;
        ride(p,p.zza< -.5F);
    }

    /** One push on a pedal: -1 the left, 1 the right. */
    public void pedal(int side){
        if(done)return;boolean same=side==lastSide;
        speed=Math.min(TOP,speed+(same?SAME_PUSH:TURN_PUSH));
        lean+=side*(same?SAME_TIP:TURN_TIP);
        lastSide=side;strokes++;entityData.set(STROKES,side*strokes);
    }

    private void ride(ServerPlayer p,boolean braking){
        // Where the rider looks, as fast as a bike turns. Turning at speed leans it into the turn: a turn to the right (yaw going up) leans right.
        float want=Mth.wrapDegrees(p.getYRot()-getYRot());float turn=Mth.clamp(want,-TURN,TURN)*Math.min(1,.3F+speed/STEADY);
        setYRot(getYRot()+turn);lean+=turn*.0015F*speed/TOP;
        if(braking)speed*=.9F;
        if(isInWater())speed*=.6F;
        // Balance. Stopped, a foot goes down. Moving, it leans further the way it leans, faster when slow, with a little wobble of its own.
        if(speed<STOPPED)lean*=.85F;
        else{float steady=Math.min(1,speed/STEADY);lean+=lean*(.03F*(1-steady)+.006F*steady)+(float)random.nextGaussian()*(.004F+.008F*(1-steady));}
        // On along the road; uphill slows it, downhill speeds it.
        double rad=Math.toRadians(getYRot());var dir=new Vec3(-Math.sin(rad),0,Math.cos(rad));
        double fall=onGround()?-.08:getDeltaMovement().y-.08;double y0=getY(),x0=getX(),z0=getZ();
        move(MoverType.SELF,new Vec3(dir.x*speed,fall,dir.z*speed));
        setDeltaMovement(0,onGround()?0:Math.max(-2,fall*.98),0);
        double climbed=getY()-y0;
        if(climbed>.01)speed-=(float)climbed*.09F;else if(climbed< -.01)speed+=(float)-climbed*.04F;
        // Into something: a glance along a wall keeps most of the way on; head on at speed, the rider comes off.
        if(horizontalCollision&&speed>.001F){double kept=Math.hypot(getX()-x0,getZ()-z0)/speed;if(kept<.4&&speed>CRASH){fall(p,lean>=0?1:-1,true);return;}speed*=(float)Mth.clamp(kept,.3,1);}
        speed=Mth.clamp(speed*.988F,0,TOP*1.4F);
        entityData.set(LEAN,Mth.clamp(lean,-1,1));entityData.set(SPEED,speed);
        if(Math.abs(lean)>=1)fall(p,lean>0?1:-1,false);
        else BorrascaStory.bikeRidden(p,this);
    }

    /** Over it goes, or into something head on: the rider comes off on that side, and the bike lies in the road. */
    private void fall(ServerPlayer p,int side,boolean crashed){
        done=true;p.stopRiding();
        double rad=Math.toRadians(getYRot());var right=new Vec3(-Math.cos(rad),0,-Math.sin(rad));
        p.setDeltaMovement(right.scale(side*.3).add(0,.2,0));p.hurtMarked=true;
        if(p.getHealth()>4)p.hurt(level().damageSources().fall(),1);
        park(p,true);BorrascaStory.bikeFell(p,whose(),crashed);
    }

    @Override protected void removePassenger(Entity e){super.removePassenger(e);if(!level().isClientSide&&!done&&e instanceof ServerPlayer p)park(p,false);}

    /** Back to a block where it stopped, on its kickstand or lying on its side, and gone as an entity. */
    private void park(@Nullable ServerPlayer p,boolean lying){
        if(!(level() instanceof ServerLevel l)||isRemoved())return;done=true;
        var at=place(l,blockPosition());
        if(at!=null)l.setBlock(at,BorrascaShopBlock.of(BorrascaShopBlock.Kind.BIKE,Direction.fromYRot(getYRot()).getCounterClockWise(),lying?1:0),3);
        BorrascaStory.bikeParked(l,reader,whose(),at);
        discard();
    }
    /** The nearest free ground for the bike to stand on: its own cell, else one beside it. */
    private static @Nullable BlockPos place(ServerLevel l,BlockPos from){
        for(int r=0;r<=2;r++)for(int dy=1;dy>=-2;dy--)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
            if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;var at=from.offset(dx,dy,dz);
            BlockState here=l.getBlockState(at),below=l.getBlockState(at.below());
            if((here.isAir()||here.canBeReplaced())&&here.getFluidState().isEmpty()&&below.isFaceSturdy(l,at.below(),Direction.UP))return at;
        }
        return null;
    }

    // ------------------------------------------------------------------ drawing (client)
    private void drawn(){
        if(lerpSteps>0){double f=1D/lerpSteps;setPos(getX()+(lerpX-getX())*f,getY()+(lerpY-getY())*f,getZ()+(lerpZ-getZ())*f);setYRot(getYRot()+Mth.wrapDegrees(lerpYRot-getYRot())*(float)f);lerpSteps--;}
        spinO=spin;spin+=speed()/WHEEL;
        // Each push brings that pedal round to the bottom: the left is down at a turn of 0, the right at half a turn.
        int s=strokes();if(s!=seenStrokes){seenStrokes=s;float down=s<0?0:Mth.PI;crankTo=down+(float)Math.ceil((crankTo+.01F-down)/Mth.TWO_PI)*Mth.TWO_PI;}
        crankO=crank;crank+=(crankTo-crank)*.3F;
        shownO=shown;shown+=(lean()-shown)*.5F;
    }
    @Override public void lerpTo(double x,double y,double z,float yRot,float xRot,int steps){lerpX=x;lerpY=y;lerpZ=z;lerpYRot=yRot;lerpSteps=3;}
    @Override public double lerpTargetX(){return lerpSteps>0?lerpX:getX();}
    @Override public double lerpTargetY(){return lerpSteps>0?lerpY:getY();}
    @Override public double lerpTargetZ(){return lerpSteps>0?lerpZ:getZ();}
    @Override public float lerpTargetYRot(){return lerpSteps>0?lerpYRot:getYRot();}
    /** How far over it is drawn, from -1 (left) to 1 (right). */
    public float shownLean(float partial){return Mth.lerp(partial,shownO,shown);}
    /** How far round the wheels and the cranks have turned, in radians. */
    public float spin(float partial){return Mth.lerp(partial,spinO,spin);}
    public float crank(float partial){return Mth.lerp(partial,crankO,crank);}
}
