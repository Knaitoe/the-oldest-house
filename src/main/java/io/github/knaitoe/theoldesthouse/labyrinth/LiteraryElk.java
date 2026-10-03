package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
/** The cow elk and her injury are a real, persistent native body. */
public final class LiteraryElk extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> WOUND=SynchedEntityData.defineId(LiteraryElk.class,EntityDataSerializers.BOOLEAN);
    public LiteraryElk(EntityType<? extends LiteraryElk> t,Level l){super(t,l);setPersistenceRequired();setNoAi(true);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,.3);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(WOUND,false);}
    public boolean wounded(){return entityData.get(WOUND);}public void wound(){entityData.set(WOUND,true);}
    @Override public boolean hurt(DamageSource s,float a){if(s.getEntity() instanceof ServerPlayer p&&LiteraryVignettes.inside(p,LabyrinthPlace.ELK_LOT)){if(getHealth()<=2)return false;boolean hit=super.hurt(s,Math.min(3,a));if(hit){wound();LiteraryVignettes.elkWound(p);}return hit;}return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putBoolean("Wound",wounded());}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(WOUND,t.getBoolean("Wound"));}
}
