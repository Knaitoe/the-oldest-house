package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.*;

/** An authored canoe, using Minecraft's boat mesh; its phone is recovered by its actual owner. */
public final class LakeCanoeEntity extends Boat {
    private static final EntityDataAccessor<Boolean> MOBILE=SynchedEntityData.defineId(LakeCanoeEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FIXED_VIEW=SynchedEntityData.defineId(LakeCanoeEntity.class,EntityDataSerializers.BOOLEAN);
    public LakeCanoeEntity(EntityType<? extends Boat> type,Level level){super(type,level);setNoGravity(true);setInvulnerable(true);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MOBILE,false);b.define(FIXED_VIEW,false);}
    public void mobile(boolean mobile){entityData.set(MOBILE,mobile);setNoGravity(!mobile);}
    public void fixedView(boolean fixed){entityData.set(FIXED_VIEW,fixed);if(fixed)setDeltaMovement(Vec3.ZERO);}
    public boolean rowing(){return entityData.get(MOBILE)&&!entityData.get(FIXED_VIEW);}
    @Override public void tick(){setNoGravity(!rowing());super.tick();if(!rowing())setDeltaMovement(Vec3.ZERO);}
    @Override public void move(MoverType type,Vec3 movement){if(rowing())super.move(type,movement);}
    @Override protected boolean canAddPassenger(Entity entity){return entityData.get(MOBILE)&&entity instanceof Player&&getPassengers().isEmpty();}
    @Override public boolean isPushable(){return false;}
    @Override public InteractionResult interact(Player player,InteractionHand hand){
        if(player instanceof ServerPlayer serverPlayer&&hand==InteractionHand.MAIN_HAND){
            if(getTags().contains("LiteraryMovieCanoe"))LiteraryVignettes.boardMovie(serverPlayer,this);else if(getTags().contains(PhoneCanoe.CANOE))PhoneCanoe.board(serverPlayer,this);else PreservedCave.examine(serverPlayer,this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putBoolean("Mobile",entityData.get(MOBILE));tag.putBoolean("FixedPhoneView",entityData.get(FIXED_VIEW));}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);mobile(tag.getBoolean("Mobile")||getTags().contains(PhoneCanoe.CANOE));fixedView(tag.getBoolean("FixedPhoneView"));}
}
