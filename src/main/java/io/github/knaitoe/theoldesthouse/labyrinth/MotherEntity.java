package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** A real noncombatant entity, with her own model rather than a disguised vanilla monster. */
public final class MotherEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> WAITING =
            SynchedEntityData.defineId(MotherEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CORRUPTION =
            SynchedEntityData.defineId(MotherEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> CARRYING =
            SynchedEntityData.defineId(MotherEntity.class, EntityDataSerializers.BOOLEAN);
    private float shownCorruption;
    @Nullable private UUID following;

    public MotherEntity(EntityType<? extends MotherEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.14D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 48.0F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WAITING, false);
        builder.define(CORRUPTION, 0.0F);
        builder.define(CARRYING, false);
    }

    public boolean isWaiting() { return entityData.get(WAITING); }
    public void setWaiting(boolean value) { entityData.set(WAITING, value); }
    @Nullable public UUID following() { return following; }
    public void follow(@Nullable UUID player) { following = player; setWaiting(player != null); }
    public float corruption() { return entityData.get(CORRUPTION); }
    public float shownCorruption() { return shownCorruption; }
    public void setCorruption(float value) { entityData.set(CORRUPTION, Math.max(0, Math.min(1, value))); }
    public boolean isCarrying() { return entityData.get(CARRYING); }
    public void setCarrying(boolean value) { entityData.set(CARRYING, value); }

    @Override public void tick() {
        super.tick();
        // Growing wrong is gradual. After violence the ordinary mask comes back at once.
        shownCorruption = corruption() < shownCorruption ? corruption()
                : Math.min(corruption(), shownCorruption + 0.006F);
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean canBeLeashed() { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (following != null) tag.putUUID("MotherFollowing", following);
        tag.putBoolean("MotherWaiting", isWaiting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        following = tag.hasUUID("MotherFollowing") ? tag.getUUID("MotherFollowing") : null;
        setWaiting(tag.getBoolean("MotherWaiting"));
    }
}
