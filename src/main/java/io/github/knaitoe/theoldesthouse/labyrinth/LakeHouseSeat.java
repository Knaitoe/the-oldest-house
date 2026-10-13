package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** An invisible native seat; the dry chair, rather than a breathing potion, is what gives the seated reader air. */
public final class LakeHouseSeat extends Entity {
    public LakeHouseSeat(EntityType<? extends LakeHouseSeat> type,Level l){super(type,l);setNoGravity(true);setInvulnerable(true);}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
    @Override protected void readAdditionalSaveData(CompoundTag t){}
    @Override protected void addAdditionalSaveData(CompoundTag t){}
    @Override public void tick(){super.tick();if(!level().isClientSide&&getPassengers().isEmpty())discard();}
    @Override public boolean isPickable(){return false;}
}
