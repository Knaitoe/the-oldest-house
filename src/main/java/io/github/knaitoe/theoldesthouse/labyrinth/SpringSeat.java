package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.471, Bike night: a reader's seat on the park's spring rider. The rocking puts it where the saddle is each tick, on the
 * client and the server alike (SpringBlock.Rocker), and its rider goes with it. Nothing is drawn of it. It is never saved, and it
 * is gone when its rider is, or when the spring rider is.
 */
public class SpringSeat extends Entity {
    private static final EntityDataAccessor<BlockPos> FOOT=SynchedEntityData.defineId(SpringSeat.class,EntityDataSerializers.BLOCK_POS);
    /** How far to the side of the horse a reader is put when they get off. */
    public static final double ASIDE=.8;
    public SpringSeat(EntityType<? extends SpringSeat> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}

    public BlockPos foot(){return entityData.get(FOOT);}
    public @Nullable SpringBlock.Rocker rocker(){
        return SpringBlock.is(level().getBlockState(foot()),SpringBlock.Part.FOOT)&&level().getBlockEntity(foot()) instanceof SpringBlock.Rocker r?r:null;
    }
    /** A hand on the horse sits on it. Returns the seat, or null if they cannot: somebody is on it, or they are riding something. */
    public static @Nullable SpringSeat sit(ServerPlayer p,BlockPos foot){
        var l=p.serverLevel();
        if(!SpringBlock.is(l.getBlockState(foot),SpringBlock.Part.FOOT)||!(l.getBlockEntity(foot) instanceof SpringBlock.Rocker rocker)||p.isPassenger()||p.isSpectator()||!BikeNight.here(p))return null;
        if(rocker.ridden()||rocker.seat()!=null)return null;
        var seat=BikeNightRegistry.SPRING_SEAT.get().create(l);if(seat==null)return null;
        seat.entityData.set(FOOT,foot.immutable());
        var at=SpringBlock.saddle(foot,rocker.facing(),rocker.angle());float yaw=rocker.facing().toYRot();
        seat.moveTo(at.x,at.y,at.z,yaw,0);
        if(!l.addFreshEntity(seat)||!p.startRiding(seat,true)){seat.discard();return null;}
        rocker.sat(seat);
        p.setYRot(yaw);p.setYHeadRot(yaw);
        return seat;
    }
    /** The rocking puts the seat where the saddle is, and its rider with it. */
    void place(double angle){
        var rocker=rocker();if(rocker==null)return;
        var at=SpringBlock.saddle(foot(),rocker.facing(),angle);setPos(at.x,at.y,at.z);
        for(var rider:getPassengers())positionRider(rider);
    }
    @Override protected boolean canAddPassenger(Entity passenger){return getPassengers().isEmpty();}
    /** They sit on the saddle, which is where this is. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity rider,EntityDimensions dimensions,float partial){return Vec3.ZERO;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){builder.define(FOOT,BlockPos.ZERO);}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public void tick(){
        super.tick();
        var rocker=rocker();
        if(!level().isClientSide()&&(rocker==null||getPassengers().isEmpty())){discard();return;}
        // It tells the rocking it is there; the rocking moves it (after this, in the same tick).
        if(rocker!=null)rocker.sat(this);
    }
    /** Getting off puts a reader on the sand beside the horse. */
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity rider){
        var rocker=rocker();if(rocker==null)return position();
        var side=rocker.facing().getClockWise();
        return new Vec3(foot().getX()+.5+side.getStepX()*ASIDE,foot().getY(),foot().getZ()+.5+side.getStepZ()*ASIDE);
    }
}
