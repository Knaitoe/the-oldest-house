package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.467, Bike night: a reader hanging from the park's monkey bars. It is the hold they have on a rung: it stays at the rungs'
 * height and the reader hangs under it by their hands, at their full length (they are not sat down, as on a seat). Nothing is
 * drawn of it. It is never saved, and it is gone when its rider is, or when the rungs are.
 *
 * <p>0.4.470, the verb as docs/BIKE_NIGHT_PROPOSAL.md has it: "The strafe keys swing the body, and each swing at the top of its arc
 * reaches the next bar. Letting go drops. Too early misses the bar, and the reader drops into the sand. It is a timing game, like
 * the pedals." A strafe key swings the body the way the reader is looking along the bars. A strafe key again while the swing is at
 * its top catches the next rung that way, and that catch is the next swing's beginning. Too early, the hand misses and they
 * drop. Once the swing has gone back, a strafe key swings them again and nothing is lost. (0.4.467 moved the hold by the walk
 * keys at a steady pace, which was not what the proposal said.)
 */
public class BarsHang extends Entity {
    /** Whether the bars run along x (east and west). */
    private static final EntityDataAccessor<Boolean> ALONG_X=SynchedEntityData.defineId(BarsHang.class,EntityDataSerializers.BOOLEAN);
    /** The game time the body's swing began, or nothing while it hangs still; and which way along the bars it swings (1 or -1). */
    private static final EntityDataAccessor<Integer> SWING_START=SynchedEntityData.defineId(BarsHang.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SWING_WAY=SynchedEntityData.defineId(BarsHang.class,EntityDataSerializers.INT);
    /**
     * A reader's fists over their feet with their arms straight up, in their own heights; how far short of the rungs their fists
     * can be and still catch them (a stretch); how far apart the rungs are; how far to the side of the bars a reader can be and
     * take hold, and how far past their last rung; how nearly square across the bars a look is that goes neither way.
     */
    public static final double REACH=.97,STRETCH=.3,HAND=.5,SIDE=.8,PAST=1.3,SQUARE=.15;
    /**
     * The swing, in ticks from when it began: when it is at its top; the first and the last tick a hand let go then catches the
     * next rung; and when it has died away and the body hangs still again. And how far the body swings at its top, in radians.
     */
    public static final int TOP=8,EARLY=6,LATE=13,STILL=24;
    public static final double SWING=.5;
    /** The first and last rungs along the bars; the least and most this hold has been along them. */
    private double lo,hi,least,most;
    /** The game time the swing began (less than nothing: hanging still); whether a strafe key was down on the last tick. */
    private long swingAt=-1;
    private boolean held,crossed;
    public BarsHang(EntityType<? extends BarsHang> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}

    public boolean alongX(){return entityData.get(ALONG_X);}
    /** Where the hold is along the bars. */
    public double along(){return alongX()?getX():getZ();}
    /** The first and last rungs it can be on. */
    public double lo(){return lo;}
    public double hi(){return hi;}
    /** Ticks since the body's swing began, or less than nothing while it hangs still. */
    public double swingTicks(float partial){int start=entityData.get(SWING_START);return start==0?-1:level().getGameTime()+partial-start;}
    /** Which way along the bars the body swings: 1 or -1. */
    public int swingWay(){return entityData.get(SWING_WAY);}
    /**
     * How far the body is swung, in radians, that many ticks into a swing: out to its top, back through the middle, a little the
     * other way, and still. More than nothing is the way it was swung.
     */
    public static double swung(double ticks){
        if(ticks<0||ticks>=STILL)return 0;
        return SWING*1.45*Math.sin(Math.PI*ticks/(2*TOP))*(1-ticks/STILL);
    }
    /** How high a hold on a block of rungs is. */
    public static double height(BlockPos rungs){return rungs.getY()+CalderPlayBlock.RUNG;}
    /** The run of rungs a block belongs to: where its first and last rungs are along the way it runs. A block has a rung a quarter of the way in from each end. */
    public static double[] run(Level l,BlockPos rungs){
        var axis=l.getBlockState(rungs).getValue(CalderPlayBlock.FACING).getAxis();var step=Direction.fromAxisAndDirection(axis,Direction.AxisDirection.POSITIVE);var a=rungs;var b=rungs;
        while(CalderPlayBlock.is(l.getBlockState(a.relative(step.getOpposite())),CalderPlayBlock.Kind.RUNGS))a=a.relative(step.getOpposite());
        while(CalderPlayBlock.is(l.getBlockState(b.relative(step)),CalderPlayBlock.Kind.RUNGS))b=b.relative(step);
        return new double[]{a.get(axis)+.25,b.get(axis)+.75};
    }
    /** Why a reader cannot take hold of these rungs from where they are, as the line they are told; null if they can. */
    public static @Nullable String cannot(ServerPlayer p,BlockPos rungs){
        var l=p.serverLevel();var s=l.getBlockState(rungs);if(!CalderPlayBlock.is(s,CalderPlayBlock.Kind.RUNGS))return BikeNightTexts.BARS_FAR;
        boolean x=s.getValue(CalderPlayBlock.FACING).getAxis()==Direction.Axis.X;double[] run=run(l,rungs);
        double side=Math.abs((x?p.getZ():p.getX())-((x?rungs.getZ():rungs.getX())+.5)),along=x?p.getX():p.getZ();
        if(side>SIDE||along<run[0]-PAST||along>run[1]+PAST)return BikeNightTexts.BARS_FAR;
        if(p.getY()>=rungs.getY())return BikeNightTexts.BARS_ON;
        if(p.getY()+p.getBbHeight()*REACH+STRETCH<height(rungs))return BikeNightTexts.BARS_LOW;
        return null;
    }
    /** A reader near enough under the rungs takes hold of the rung nearest over them. Returns the hold, or null if they cannot. */
    public static @Nullable BarsHang take(ServerPlayer p,BlockPos rungs){
        if(p.isPassenger()||p.isSpectator()||!BikeNight.here(p)||cannot(p,rungs)!=null)return null;
        var l=p.serverLevel();boolean x=l.getBlockState(rungs).getValue(CalderPlayBlock.FACING).getAxis()==Direction.Axis.X;double[] run=run(l,rungs);
        var hold=BikeNightRegistry.BARS_HANG.get().create(l);if(hold==null)return null;
        hold.lo=run[0];hold.hi=run[1];
        // The rung nearest over them: the rungs are a hand's reach apart from the first.
        double at=Mth.clamp(run[0]+Math.round(((x?p.getX():p.getZ())-run[0])/HAND)*HAND,run[0],run[1]),across=(x?rungs.getZ():rungs.getX())+.5;hold.least=hold.most=at;
        hold.entityData.set(ALONG_X,x);
        hold.moveTo(x?at:across,height(rungs),x?across:at,p.getYRot(),0);
        if(!l.addFreshEntity(hold)||!p.startRiding(hold,true)){hold.discard();return null;}
        l.playSound(null,hold.getX(),hold.getY(),hold.getZ(),BikeNightRegistry.BARS.get(),SoundSource.PLAYERS,.6F,.95F+p.getRandom().nextFloat()*.1F);
        BikeNight.hung(p);
        return hold;
    }
    @Override protected boolean canAddPassenger(Entity passenger){return getPassengers().isEmpty();}
    /** Whoever lets go, or misses, or loses the rungs: the night is told, so that they do not catch the bars again in the same fall. */
    @Override protected void removePassenger(Entity passenger){
        super.removePassenger(passenger);
        if(!level().isClientSide()&&passenger instanceof ServerPlayer p)BikeNight.letGoOfBars(p);
    }
    /** They hang at their full length. */
    @Override public boolean shouldRiderSit(){return false;}
    /** The reader hangs under the hold by their hands: their feet are their reach below it. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity rider,EntityDimensions dimensions,float partial){
        return new Vec3(0,rider.getVehicleAttachmentPoint(this).y-rider.getBbHeight()*REACH,0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(ALONG_X,true);builder.define(SWING_START,0);builder.define(SWING_WAY,1);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    /** The body swings that way, from now. */
    private void swing(int way,long now){swingAt=now;entityData.set(SWING_START,(int)now);entityData.set(SWING_WAY,way);}
    @Override public void tick(){
        super.tick();
        if(level().isClientSide())return;
        if(!(getFirstPassenger() instanceof ServerPlayer rider)||!CalderPlayBlock.is(level().getBlockState(blockPosition()),CalderPlayBlock.Kind.RUNGS)){discard();return;}
        long now=level().getGameTime(),since=swingAt<0?-1:now-swingAt;
        if(since>=STILL){swingAt=-1;since=-1;entityData.set(SWING_START,0);}
        // A strafe key, pressed (not held).
        boolean down=Math.abs(rider.xxa)>.2F,pressed=down&&!held;held=down;
        if(!pressed)return;
        // The way the reader is looking along the bars; a look square across them goes neither way.
        var look=rider.getLookAngle();double facing=alongX()?look.x:look.z;
        if(Math.abs(facing)<SQUARE)return;
        int way=facing>0?1:-1;
        // Hanging still, or the swing gone back: the body swings again.
        if(since<0||since>LATE){swing(way,now);return;}
        // Too early: the hand lets go before the swing has carried it to the next rung, and misses.
        if(since<EARLY){BikeNight.missed(rider);rider.stopRiding();return;}
        // At the top: the hand catches the next rung that way, if there is one; the catch is the next swing's beginning.
        double before=along(),next=before+way*HAND;
        if(next<lo-1e-6||next>hi+1e-6){swing(way,now);return;}
        setPos(alongX()?next:getX(),getY(),alongX()?getZ():next);
        level().playSound(null,getX(),getY(),getZ(),BikeNightRegistry.BARS.get(),SoundSource.PLAYERS,.45F,1.05F+rider.getRandom().nextFloat()*.15F);
        swing(way,now);
        // From the last rung one way to the last rung the other, on one hold: across.
        least=Math.min(least,next);most=Math.max(most,next);
        if(!crossed&&least<=lo+1e-6&&most>=hi-1e-6&&hi-lo>1){crossed=true;BikeNight.crossed(rider);}
    }
    /** Letting go drops a reader where they hang. */
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity rider){return new Vec3(getX(),getY()-rider.getBbHeight()*REACH,getZ());}
}
