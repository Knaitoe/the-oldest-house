package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

/** Tom and the waking double use the explorer's actual skin, with private visibility. */
public final class NovelActor extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(NovelActor.class,EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> ROLE=SynchedEntityData.defineId(NovelActor.class,EntityDataSerializers.INT);
    public NovelActor(EntityType<? extends NovelActor> t,Level l){super(t,l);setNoAi(true);setPersistenceRequired();}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,0);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(OWNER,Optional.empty());b.define(ROLE,0);}
    public Optional<UUID> owner(){return entityData.get(OWNER);}public int role(){return entityData.get(ROLE);}
    public void appearance(UUID id,int role){entityData.set(OWNER,Optional.of(id));entityData.set(ROLE,role);}
    @Override protected InteractionResult mobInteract(Player p,InteractionHand hand){if(p instanceof ServerPlayer s&&hand==InteractionHand.MAIN_HAND&&owner().filter(s.getUUID()::equals).isPresent())NovelVignettes.meetTom(s,this);return InteractionResult.sidedSuccess(level().isClientSide);}
    @Override public boolean hurt(DamageSource s,float n){return false;}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);owner().ifPresent(id->t.putUUID("Explorer",id));t.putInt("Role",role());}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);if(t.hasUUID("Explorer"))appearance(t.getUUID("Explorer"),t.getInt("Role"));}
}
