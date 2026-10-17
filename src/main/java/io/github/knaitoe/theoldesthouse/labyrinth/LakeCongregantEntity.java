package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;

/** Preserved, passive bodies. The preacher cannot be killed to bypass the church's sequence. */
public final class LakeCongregantEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> PREACHER = SynchedEntityData.defineId(LakeCongregantEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SEATED = SynchedEntityData.defineId(LakeCongregantEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ERA = SynchedEntityData.defineId(LakeCongregantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> LYING = SynchedEntityData.defineId(LakeCongregantEntity.class, EntityDataSerializers.BOOLEAN);
    /** 0.4.101: which of the four Shallows boys this is, his hold on her feet, and the swing they share. */
    private static final EntityDataAccessor<Integer> BOY_LOOK = SynchedEntityData.defineId(LakeCongregantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CARRY = SynchedEntityData.defineId(LakeCongregantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SWING = SynchedEntityData.defineId(LakeCongregantEntity.class, EntityDataSerializers.FLOAT);
    private float seat=1,previousSeat=1,swingO,swingNow;
    /** The one reader a Shallows carrier belongs to; nobody else is ever sent him. */
    @Nullable private UUID carrierOwner;
    public LakeCongregantEntity(EntityType<? extends LakeCongregantEntity> type, Level level) {
        super(type, level); setNoAi(true); setNoGravity(true); setPersistenceRequired(); setSilent(true);
    }
    public static AttributeSupplier.Builder attributes() { return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(PREACHER, false); builder.define(SEATED, true);builder.define(ERA,0);builder.define(LYING,false);
        builder.define(BOY_LOOK,0);builder.define(CARRY,0);builder.define(SWING,0F);
    }
    public boolean preacher() { return entityData.get(PREACHER); }
    public boolean seated() { return entityData.get(SEATED); }
    public boolean lying(){return entityData.get(LYING);}
    public void lying(boolean value){entityData.set(LYING,value);}
    public void pose(boolean preacher, boolean seated) { entityData.set(PREACHER, preacher); entityData.set(SEATED, seated); }
    public float seating(float partial){return net.minecraft.util.Mth.lerp(partial,previousSeat,seat);}
    public int preservedEra(){return entityData.get(ERA);}
    public void preservedEra(int era){entityData.set(ERA,Math.max(0,Math.min(4,era)));}
    public boolean memoryBoy(){return preservedEra()==4;}
    public void memoryBoy(boolean boy){preservedEra(boy?4:0);}
    public int boyLook(){return entityData.get(BOY_LOOK);}
    public void boyLook(int look){if(look!=boyLook())entityData.set(BOY_LOOK,Math.max(0,Math.min(3,look)));}
    public int carry(){return entityData.get(CARRY);}
    public void carry(int pose){if(pose!=carry())entityData.set(CARRY,pose);}
    public float swing(){return entityData.get(SWING);}
    public void swing(float angle){if(angle!=swing())entityData.set(SWING,angle);}
    public float swingAt(float partial){return net.minecraft.util.Mth.lerp(partial,swingO,swingNow);}
    public @Nullable UUID carrierOwner(){return carrierOwner;}
    public void carrierOwner(UUID owner){carrierOwner=owner;}
    @Override public boolean broadcastToPlayer(ServerPlayer player){
        return (carrierOwner==null||carrierOwner.equals(player.getUUID()))&&super.broadcastToPlayer(player);
    }
    @Override public void tick() {
        if(level().isClientSide()){swingO=swingNow;swingNow=swing();}
        super.tick(); setAirSupply(300);previousSeat=seat;seat=net.minecraft.util.Mth.approach(seat,seated()?1:0,.035F);
        if(carrierOwner!=null&&!level().isClientSide())Shallows.tickCarrier(this);
    }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override public boolean hurt(DamageSource source, float damage) { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putBoolean("Preacher", preacher()); tag.putBoolean("Seated", seated());tag.putInt("Era",preservedEra());tag.putBoolean("Lying",lying());
        tag.putInt("BoyLook0501",boyLook());if(carrierOwner!=null)tag.putUUID("ShallowsCarrierOwner",carrierOwner);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); pose(tag.getBoolean("Preacher"), tag.getBoolean("Seated"));preservedEra(tag.getInt("Era"));lying(tag.getBoolean("Lying"));seat=previousSeat=seated()?1:0;
        boyLook(tag.getInt("BoyLook0501"));carrierOwner=tag.hasUUID("ShallowsCarrierOwner")?tag.getUUID("ShallowsCarrierOwner"):null;
    }
}
