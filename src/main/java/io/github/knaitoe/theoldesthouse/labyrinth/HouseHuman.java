package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** A real, persistent shared human. Target and activity use native tracked data. */
public final class HouseHuman extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> PURSUED=SynchedEntityData.defineId(HouseHuman.class,EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> HUNTING=SynchedEntityData.defineId(HouseHuman.class,EntityDataSerializers.BOOLEAN);
    private int stagger,attackClock;
    public HouseHuman(EntityType<? extends HouseHuman> type,Level level){
        super(type,level);setPersistenceRequired();setCanPickUpLoot(false);
        setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.CROSSBOW));
        setDropChance(EquipmentSlot.MAINHAND,0);
    }
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,30)
            .add(Attributes.MOVEMENT_SPEED,.25).add(Attributes.FOLLOW_RANGE,48).add(Attributes.ATTACK_DAMAGE,3);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PURSUED,Optional.empty());b.define(HUNTING,false);}
    public Optional<UUID> pursued(){return entityData.get(PURSUED);}
    public boolean hunting(){return entityData.get(HUNTING);}
    public void pursue(ServerPlayer player){entityData.set(PURSUED,Optional.ofNullable(player).map(ServerPlayer::getUUID));entityData.set(HUNTING,player!=null);setTarget(player);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean hurt(DamageSource source,float amount){
        if(!level().isClientSide&&source.getEntity() instanceof ServerPlayer p&&HollowayVignette.pursued(p)){
            stagger=40;getNavigation().stop();playSound(SoundEvents.PLAYER_HURT,.7F,.8F);
        }
        return false; // The scene is an escape, not a repeatable mob farm.
    }
    public boolean staggered(){return stagger>0;}
    public boolean attackReady(){return attackClock<=0;}
    public void attacked(){attackClock=45;}
    public void blocked(){stagger=35;getNavigation().stop();}
    @Override public void tick(){super.tick();if(!level().isClientSide){if(stagger>0)stagger--;if(attackClock>0)attackClock--;}}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("Stagger",stagger);tag.putInt("AttackClock",attackClock);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);stagger=tag.getInt("Stagger");attackClock=tag.getInt("AttackClock");pursue(null);}
}
