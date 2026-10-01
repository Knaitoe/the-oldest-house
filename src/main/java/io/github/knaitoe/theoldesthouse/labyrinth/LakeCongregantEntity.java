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
    public LakeCongregantEntity(EntityType<? extends LakeCongregantEntity> type, Level level) {
        super(type, level); setNoAi(true); setNoGravity(true); setPersistenceRequired(); setSilent(true);
    }
    public static AttributeSupplier.Builder attributes() { return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(PREACHER, false); builder.define(SEATED, true);
    }
    public boolean preacher() { return entityData.get(PREACHER); }
    public boolean seated() { return entityData.get(SEATED); }
    public void pose(boolean preacher, boolean seated) { entityData.set(PREACHER, preacher); entityData.set(SEATED, seated); }
    @Override public void tick() { super.tick(); setAirSupply(300); }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override public boolean hurt(DamageSource source, float damage) { return false; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putBoolean("Preacher", preacher()); tag.putBoolean("Seated", seated()); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); pose(tag.getBoolean("Preacher"), tag.getBoolean("Seated")); }
}
