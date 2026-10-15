package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import java.util.Optional;
import java.util.UUID;
/** The cow elk and her injury are a real, persistent native body. */
public final class LiteraryElk extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> WOUND=SynchedEntityData.defineId(LiteraryElk.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(LiteraryElk.class,EntityDataSerializers.OPTIONAL_UUID);
    public LiteraryElk(EntityType<? extends LiteraryElk> t,Level l){super(t,l);setPersistenceRequired();setNoAi(true);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.3);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(WOUND,false);b.define(OWNER,Optional.empty());}
    public Optional<UUID> owner(){return entityData.get(OWNER);}public void owner(UUID id){entityData.set(OWNER,Optional.of(id));}
    public boolean wounded(){return entityData.get(WOUND);}public void wound(){entityData.set(WOUND,true);}
    @Override public void tick(){super.tick();LiteraryMotion.tickElk(this);}
    @Override public boolean broadcastToPlayer(ServerPlayer p){return (owner().isEmpty()||owner().get().equals(p.getUUID()))&&super.broadcastToPlayer(p);}
    @Override public boolean hurt(DamageSource s,float a){if(owner().isPresent())return false;if(s.getEntity() instanceof ServerPlayer p&&LiteraryVignettes.inside(p,LabyrinthPlace.ELK_LOT)){if(getHealth()<=2)return false;boolean hit=super.hurt(s,Math.min(3,a));if(hit){wound();LiteraryVignettes.elkWound(p);}return hit;}return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putBoolean("Wound",wounded());owner().ifPresent(id->t.putUUID("HerdOwner0492",id));}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(WOUND,t.getBoolean("Wound"));entityData.set(OWNER,t.hasUUID("HerdOwner0492")?Optional.of(t.getUUID("HerdOwner0492")):Optional.empty());}
}
