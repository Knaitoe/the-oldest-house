package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.466, Bike night: a go down the park's slide. A reader who comes into the top of the chute sits and is carried down it,
 * faster all the way, along the level end, and let go off it with the speed they had. Where the ride is comes from when it began,
 * on the client and the server alike, so nothing is sent while it runs. Nothing is drawn of it: the reader is seen sitting, sliding.
 * It is never saved, and it is gone when its rider is.
 */
public class SlideRide extends Entity {
    private static final EntityDataAccessor<Integer> START=SynchedEntityData.defineId(SlideRide.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TOP=SynchedEntityData.defineId(SlideRide.class,EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Direction> FORWARD=SynchedEntityData.defineId(SlideRide.class,EntityDataSerializers.DIRECTION);
    /**
     * How fast a rider gains speed along the ground, in blocks a tick each tick; the chute's length along the ground and the
     * level end's; what a rider keeps of their speed when they are let go, and the little they are lifted.
     */
    public static final double GAIN=.018,CHUTE=CalderPlayBlock.CHUTES,LEVEL=1,KEEP=.9,LIFT=.1;
    /** Ticks down the chute, the speed at the bottom of it, and the ticks of the whole ride. */
    public static final double DOWN=Math.sqrt(2*CHUTE/GAIN),SPEED=GAIN*DOWN,WHOLE=DOWN+LEVEL/SPEED;
    public SlideRide(EntityType<? extends SlideRide> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}

    /** How far along the ground the ride is, {@code t} ticks after it began. */
    public static double along(double t){return t<=0?0:t<DOWN?.5*GAIN*t*t:Math.min(CHUTE+LEVEL,CHUTE+SPEED*(t-DOWN));}
    /** Where a rider sits, that far along: on the chute's surface, from the uphill edge of its top block. */
    public static Vec3 at(BlockPos top,Direction forward,double along){
        double fall=Math.min(along,CHUTE)*CalderPlayBlock.SLOPE;
        return new Vec3(top.getX()+.5+forward.getStepX()*(along-.5),top.getY()+1-fall,top.getZ()+.5+forward.getStepZ()*(along-.5));
    }
    /** A reader comes into the top of the chute: they go down it. Returns the ride, or null if they are not to. */
    public static SlideRide begin(ServerPlayer p,BlockPos top,Direction forward){
        if(p.isPassenger()||p.isSpectator()||!BikeNight.here(p))return null;
        var ride=BikeNightRegistry.SLIDE_RIDE.get().create(p.serverLevel());if(ride==null)return null;
        ride.entityData.set(TOP,top.immutable());ride.entityData.set(FORWARD,forward);ride.entityData.set(START,(int)p.serverLevel().getGameTime());
        var from=at(top,forward,0);ride.moveTo(from.x,from.y,from.z,forward.toYRot(),0);
        if(!p.serverLevel().addFreshEntity(ride)||!p.startRiding(ride,true)){ride.discard();return null;}
        p.setYRot(forward.toYRot());p.setYHeadRot(forward.toYRot());
        p.serverLevel().playSound(null,from.x,from.y,from.z,BikeNightRegistry.SLIDE.get(),SoundSource.PLAYERS,.7F,.95F+p.getRandom().nextFloat()*.1F);
        return ride;
    }
    public BlockPos top(){return entityData.get(TOP);}
    public Direction forward(){return entityData.get(FORWARD);}
    /** Ticks since the ride began. */
    public double ticks(float partial){return level().getGameTime()+partial-entityData.get(START);}
    @Override protected boolean canAddPassenger(Entity passenger){return getPassengers().isEmpty();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(START,0);builder.define(TOP,BlockPos.ZERO);builder.define(FORWARD,Direction.NORTH);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public void tick(){
        super.tick();
        double t=ticks(0);var at=at(top(),forward(),along(t));setPos(at.x,at.y,at.z);
        if(level().isClientSide())return;
        if(!(getFirstPassenger() instanceof ServerPlayer rider)){discard();return;}
        if(t<WHOLE)return;
        // Let go off the level end, with the speed they came down with.
        rider.stopRiding();
        rider.connection.teleport(at.x,at.y+.05,at.z,rider.getYRot(),rider.getXRot());
        rider.setDeltaMovement(forward().getStepX()*SPEED*KEEP,LIFT,forward().getStepZ()*SPEED*KEEP);rider.hurtMarked=true;rider.resetFallDistance();
        BikeNight.slid(rider);
        discard();
    }
    /**
     * Getting off part of the way down leaves a reader where they are, on the chute. In the top block of it they are put back on
     * the deck: a reader left inside the top of the chute would be taken down again at once, and again for as long as they held
     * the key that got them off.
     */
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){
        if(along(ticks(0))>=1)return position().add(0,.1,0);
        var deck=top().relative(forward().getOpposite());return new Vec3(deck.getX()+.5,deck.getY()+1,deck.getZ()+.5);
    }
}
