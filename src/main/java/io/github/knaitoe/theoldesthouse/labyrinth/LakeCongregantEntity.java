package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
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
    private float seat=1,previousSeat=1;
    public LakeCongregantEntity(EntityType<? extends LakeCongregantEntity> type, Level level) {
        super(type, level); setNoAi(true); setNoGravity(true); setPersistenceRequired(); setSilent(true);
    }
    public static AttributeSupplier.Builder attributes() { return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(PREACHER, false); builder.define(SEATED, true);builder.define(ERA,0);builder.define(LYING,false);
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
    @Override public void tick() { super.tick(); setAirSupply(300);previousSeat=seat;seat=net.minecraft.util.Mth.approach(seat,seated()?1:0,.035F); }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override public boolean hurt(DamageSource source, float damage) { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putBoolean("Preacher", preacher()); tag.putBoolean("Seated", seated());tag.putInt("Era",preservedEra());tag.putBoolean("Lying",lying()); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); pose(tag.getBoolean("Preacher"), tag.getBoolean("Seated"));preservedEra(tag.getInt("Era"));lying(tag.getBoolean("Lying"));seat=previousSeat=seated()?1:0; }
}
