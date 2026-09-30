package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/** A brief, private apparition. Her feet and nightdress use an authored model and UV atlas. */
public final class ClapGhostEntity extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> VIEWER = SynchedEntityData.defineId(
            ClapGhostEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public ClapGhostEntity(EntityType<? extends ClapGhostEntity> type, Level level) {
        super(type, level);
        setNoAi(true);
        setNoGravity(true);
        setSilent(true);
    }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 1).add(Attributes.MOVEMENT_SPEED, 0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VIEWER, Optional.empty());
    }
    public void setViewer(UUID player) { entityData.set(VIEWER, Optional.of(player)); }
    public Optional<UUID> viewer() { return entityData.get(VIEWER); }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean hurt(DamageSource source, float damage) { return false; }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount > 100) discard();
    }
}
