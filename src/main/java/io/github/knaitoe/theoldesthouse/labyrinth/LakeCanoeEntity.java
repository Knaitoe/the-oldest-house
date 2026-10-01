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

/** An authored canoe, using Minecraft's boat mesh; its phone is recovered by its actual owner. */
public final class LakeCanoeEntity extends Boat {
    public LakeCanoeEntity(EntityType<? extends Boat> type,Level level){super(type,level);setNoGravity(true);setInvulnerable(true);}
    @Override public void tick(){super.tick();setDeltaMovement(Vec3.ZERO);}
    @Override public void move(MoverType type,Vec3 movement) {}
    @Override protected boolean canAddPassenger(Entity entity){return getTags().contains(PhoneCanoe.CANOE)&&entity instanceof Player&&getPassengers().isEmpty();}
    @Override public boolean isPushable(){return false;}
    @Override public InteractionResult interact(Player player,InteractionHand hand){
        if(player instanceof ServerPlayer serverPlayer&&hand==InteractionHand.MAIN_HAND){
            if(getTags().contains(PhoneCanoe.CANOE))PhoneCanoe.board(serverPlayer,this);else PreservedCave.examine(serverPlayer,this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);}
}
