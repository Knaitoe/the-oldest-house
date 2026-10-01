package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** One short encounter. Its empty, fixed boss bar describes no conventional health fight. */
public final class MinotaurEntity extends PathfinderMob {
    public static final int WATCHING=0, WINDUP=1, CHARGING=2, STUNNED=3, WOUNDED=4, RELEASED=5;
    private static final EntityDataAccessor<Integer> MOTION=SynchedEntityData.defineId(MinotaurEntity.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new ServerBossEvent(Component.empty(),BossEvent.BossBarColor.WHITE,BossEvent.BossBarOverlay.PROGRESS);
    @Nullable private UUID owner;
    private int remaining=35;
    private Vec3 charge=Vec3.ZERO;
    public MinotaurEntity(EntityType<? extends MinotaurEntity> type,Level level){super(type,level);setPersistenceRequired();bar.setProgress(1);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,100)
            .add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,48).add(Attributes.KNOCKBACK_RESISTANCE,1);}
    @Override protected void registerGoals(){}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(MOTION,WATCHING);}
    public int motion(){return entityData.get(MOTION);}
    public void owner(UUID id){owner=id;}
    public @Nullable UUID owner(){return owner;}
    private void motion(int motion,int duration){entityData.set(MOTION,motion);remaining=duration;}
    public void stagger(){motion(STUNNED,65);setDeltaMovement(Vec3.ZERO);}
    public void wounded(){motion(WOUNDED,240);bar.removeAllPlayers();setDeltaMovement(Vec3.ZERO);}
    public void released(){motion(RELEASED,0);bar.removeAllPlayers();setDeltaMovement(Vec3.ZERO);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean canBeLeashed(){return false;}
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);if(owner!=null&&owner.equals(player.getUUID())&&motion()!=WOUNDED&&motion()!=RELEASED)bar.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bar.removePlayer(player);}
    @Override public void tick(){
        super.tick();if(!(level() instanceof net.minecraft.server.level.ServerLevel level))return;
        ServerPlayer player=owner==null?null:level.getServer().getPlayerList().getPlayer(owner);
        if(motion()==RELEASED){
            bar.removeAllPlayers();
            if(player==null||player.level()!=level||!player.isAlive()||FinaleProgress.phase(level.getServer(),owner)!=FinaleProgress.Phase.RELEASE){getNavigation().stop();return;}
            WitnessEnding.tickCreature(this,player);return;
        }
        if(motion()==WOUNDED){
            Vec3 cell=FinaleController.cellCenter(level.getServer());
            if(cell!=null){Vec3 step=cell.subtract(position()).multiply(1,0,1);if(step.lengthSqr()>.25)move(MoverType.SELF,step.normalize().scale(.12));}
            if(--remaining<=0){if(player!=null)FinaleController.beginEscape(player);discard();}return;
        }
        if(player==null||player.level()!=level||!player.isAlive()||FinaleProgress.phase(level.getServer(),owner)!=FinaleProgress.Phase.FIGHT){
            bar.removeAllPlayers();motion(WATCHING,35);setDeltaMovement(Vec3.ZERO);return;
        }
        bar.addPlayer(player);
        if(motion()==WATCHING||motion()==WINDUP){getLookControl().setLookAt(player,30,30);setYRot((float)(Math.atan2(player.getZ()-getZ(),player.getX()-getX())*180/Math.PI)-90);yBodyRot=getYRot();}
        if(motion()==CHARGING){
            Vec3 before=position();move(MoverType.SELF,charge);setDeltaMovement(Vec3.ZERO);
            if(getBoundingBox().inflate(.35).intersects(player.getBoundingBox())){
                Vec3 toward=position().subtract(player.position()).multiply(1,0,1).normalize();
                if(player.isBlocking()&&player.getViewVector(1).multiply(1,0,1).normalize().dot(toward)>.25){
                    ItemStack shield=player.getUseItem();shield.hurtAndBreak(8,player,player.getUsedItemHand()==net.minecraft.world.InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
                    level.playSound(null,blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.HOSTILE,1.5F,.65F);stagger();
                    player.setDeltaMovement(charge.scale(.12));player.hurtMarked=true;
                }else{player.hurt(damageSources().genericKill(),Float.MAX_VALUE);motion(WATCHING,40);}return;
            }
            if(horizontalCollision||position().distanceToSqr(before)<.01){level.playSound(null,blockPosition(),SoundEvents.RAVAGER_STEP,SoundSource.HOSTILE,1,.5F);motion(WATCHING,40);return;}
        }
        if(--remaining>0)return;
        if(motion()==WATCHING||motion()==STUNNED){motion(WINDUP,30);playSound(SoundEvent.createVariableRangeEvent(Growl.Kind.NEAR.sound()),1,.8F);}
        else if(motion()==WINDUP){Vec3 target=player.position().subtract(position()).multiply(1,0,1);charge=target.lengthSqr()<.01?new Vec3(0,0,-.65):target.normalize().scale(.65);motion(CHARGING,35);}
        else motion(WATCHING,40);
    }
    @Override public boolean hurt(DamageSource source,float amount){
        if(!(level() instanceof net.minecraft.server.level.ServerLevel level)||motion()!=STUNNED||!(source.getEntity() instanceof ServerPlayer player)
                ||owner==null||!owner.equals(player.getUUID()))return false;
        CompoundTag state=FinaleProgress.player(level.getServer(),owner);
        UUID required=state.hasUUID("Weapon")?state.getUUID("Weapon"):null;
        boolean original=WeaponHistory.wounds(player.getMainHandItem(),required);
        if(source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile){
            CompoundTag provenance=projectile.getPersistentData();original=required!=null&&provenance.hasUUID(WeaponHistory.ORIGINAL)&&required.equals(provenance.getUUID(WeaponHistory.ORIGINAL));
        }
        if(!original){FinaleController.words(player,blockPosition().above(),"The edge passes through. This is not the one you used.");return false;}
        FinaleController.wound(player,this);return true;
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("FinaleOwner",owner);tag.putInt("FinaleMotion",motion());tag.putInt("FinaleRemaining",remaining);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("FinaleOwner")?tag.getUUID("FinaleOwner"):null;
        // Reloads never resume an untelegraphed lethal dash.
        int saved=tag.getInt("FinaleMotion");motion(saved==WOUNDED?WOUNDED:saved==RELEASED?RELEASED:WATCHING,saved==WOUNDED?Math.max(1,tag.getInt("FinaleRemaining")):35);}
    @Override public void remove(RemovalReason reason){bar.removeAllPlayers();super.remove(reason);}
}
