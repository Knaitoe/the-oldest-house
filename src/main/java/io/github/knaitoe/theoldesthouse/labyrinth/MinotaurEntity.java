package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** One short encounter. Its empty, fixed boss bar describes no conventional health fight. */
public final class MinotaurEntity extends PathfinderMob {
    public static final int WATCHING=0, WINDUP=1, CHARGING=2, STUNNED=3, WOUNDED=4, RELEASED=5, CAGED=6, LOOSE=7;
    /** A raised shield stops the charge three times. The third breaks it, whatever it is made of or enchanted with. */
    public static final int SHIELD_BLOCKS=3;
    /** Two hearts through the shield and any armour, and a throw backwards. */
    public static final float BLOCKED_DAMAGE=4;
    public static final String BLOCKS_TAG="MinotaurBlocks";
    public static final ResourceKey<DamageType> CHARGE=ResourceKey.create(Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID,"minotaur_charge"));
    private static final EntityDataAccessor<Integer> MOTION=SynchedEntityData.defineId(MinotaurEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CHILD=SynchedEntityData.defineId(MinotaurEntity.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar=new ServerBossEvent(Component.empty(),BossEvent.BossBarColor.WHITE,BossEvent.BossBarOverlay.PROGRESS);
    @Nullable private UUID owner;
    private int remaining=35;
    private Vec3 charge=Vec3.ZERO;
    private long nextSlowReport;
    /** Loose (0.4.84): 0 waits by the bars for the one it killed to wake inside, 1 lumbers off, 2 keeps to the stairs. */
    private int looseStage,looseStep=-1;private long looseSince;
    public MinotaurEntity(EntityType<? extends MinotaurEntity> type,Level level){super(type,level);setPersistenceRequired();bar.setProgress(1);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,100)
            .add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.FOLLOW_RANGE,48).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.STEP_HEIGHT,.6);}
    @Override protected void registerGoals(){}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(MOTION,WATCHING);builder.define(CHILD,false);}
    public int motion(){return entityData.get(MOTION);}
    public void owner(UUID id){owner=id;}
    public @Nullable UUID owner(){return owner;}
    public boolean childAppearance(){return entityData.get(CHILD);}
    @Override public Component getName(){return childAppearance()&&!hasCustomName()?Component.translatable("entity.the_oldest_house.caged_child"):super.getName();}
    @Override protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player visitor,net.minecraft.world.InteractionHand hand){
        if(motion()==CAGED&&visitor instanceof ServerPlayer p&&p.isAlive()&&!p.isSpectator()&&distanceToSqr(p)<25){
            var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();
            if(origin!=null&&FinaleArchitecture.contains(origin,p.blockPosition())
                    &&p.level().dimension().equals(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR)){
                boolean started=p.isShiftKeyDown()&&WitnessAccount.ready(LabyrinthData.get(p.server),p.getUUID())?WitnessEnding.begin(p):FinaleController.start(p);
                if(started)return net.minecraft.world.InteractionResult.CONSUME;
            }
        }return super.mobInteract(visitor,hand);
    }
    public void caged(){entityData.set(CHILD,true);motion(CAGED,0);}
    public void awaken(UUID owner){this.owner=owner;entityData.set(CHILD,false);motion(WATCHING,35);}
    @Override public EntityDimensions getDefaultDimensions(Pose pose){return childAppearance()?EntityDimensions.scalable(.48F,1.26F).withEyeHeight(1.12F):super.getDefaultDimensions(pose);}
    // Phase animations do not change the collision body. Reconcile dimensions
    // only for the actual child/adult change, rather than every attack phase.
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(CHILD.equals(key))refreshDimensions();}
    public boolean observed(){return observedBesides(null);}
    /** Seen by any camera but the given player's: the reader dying in front of it is not the one it must hide its change from. */
    public boolean observedBesides(@Nullable ServerPlayer besides){
        if(!(level() instanceof net.minecraft.server.level.ServerLevel l))return false;
        for(var observer:l.players()){if(observer==besides)continue;
            Entity camera=observer.getCamera();
            for(Vec3 point:java.util.List.of(position().add(0,.2,0),position().add(0,getBbHeight()*.55,0),getEyePosition())){
                Vec3 delta=point.subtract(camera.getEyePosition());double distance=delta.length();if(distance>48)continue;
                if(distance<1.5)return true;
                if(camera.getViewVector(1).dot(delta.scale(1/distance))<.26)continue;
                if(l.clip(new net.minecraft.world.level.ClipContext(camera.getEyePosition(),point,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,camera)).getType()==net.minecraft.world.phys.HitResult.Type.MISS)return true;
            }
        }return false;
    }
    private void motion(int motion,int duration){entityData.set(MOTION,motion);remaining=duration;}
    public void stagger(){motion(STUNNED,65);setDeltaMovement(Vec3.ZERO);}
    public void wounded(){motion(WOUNDED,240);bar.removeAllPlayers();setDeltaMovement(Vec3.ZERO);}
    public void released(){motion(RELEASED,0);bar.removeAllPlayers();setDeltaMovement(Vec3.ZERO);getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1);}
    /** It has killed the one who opened the cell, and is out (0.4.84). It waits for them to wake behind the bars, then goes. */
    public void loose(UUID killed,long now){owner=killed;entityData.set(CHILD,false);motion(LOOSE,0);bar.removeAllPlayers();setDeltaMovement(Vec3.ZERO);getNavigation().stop();
        getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1);looseStage=0;looseStep=-1;looseSince=now;}
    public int looseStage(){return looseStage;}public void looseStage(int stage,long now){looseStage=stage;looseSince=now;}
    public int looseStep(){return looseStep;}public void looseStep(int step){looseStep=step;}
    public long looseSince(){return looseSince;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean canBeLeashed(){return false;}
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);if(owner!=null&&owner.equals(player.getUUID())&&motion()!=WOUNDED&&motion()!=RELEASED&&motion()!=LOOSE)bar.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bar.removePlayer(player);}
    @Override public void tick(){
        long started=System.nanoTime();try{tickEncounter();}finally{
            long elapsed=System.nanoTime()-started;
            if(!level().isClientSide&&elapsed>100_000_000L&&level().getGameTime()>=nextSlowReport){
                nextSlowReport=level().getGameTime()+200;
                io.github.knaitoe.theoldesthouse.TheOldestHouse.LOGGER.warn("Minotaur tick took {} ms: phase={}, child={}, position={}, owner={}",elapsed/1_000_000,motion(),childAppearance(),position(),owner);
            }
        }
    }
    private void tickEncounter(){
        super.tick();if(!(level() instanceof net.minecraft.server.level.ServerLevel level))return;
        ServerPlayer player=owner==null?null:level.getServer().getPlayerList().getPlayer(owner);
        if(motion()==CAGED){bar.removeAllPlayers();setDeltaMovement(Vec3.ZERO);getNavigation().stop();
            if(player!=null&&player.level()==level&&FinaleProgress.phase(level.getServer(),owner)==FinaleProgress.Phase.FIGHT){
                var center=FinaleController.cellCenter(level.getServer());
                if(center!=null){Vec3 hiding=center.add(3.8,0,-.5);Vec3 delta=hiding.subtract(position()).multiply(1,0,1);
                    if(delta.lengthSqr()>.09)move(MoverType.SELF,delta.normalize().scale(Math.min(.13,delta.length())));
                    if(!observed()&&Math.abs(getX()-hiding.x)<.65)awaken(owner);
                }return;
            }
            var visitor=level.players().stream().filter(p->p.isAlive()&&!p.isSpectator()).min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if(visitor!=null&&distanceToSqr(visitor)<225)getLookControl().setLookAt(visitor,20,20);return;}
        if(motion()==LOOSE){bar.removeAllPlayers();MinotaurLoose.tick(this,level);return;}
        if(motion()==RELEASED){
            bar.removeAllPlayers();
            if(player==null||player.level()!=level||!player.isAlive()||FinaleProgress.phase(level.getServer(),owner)!=FinaleProgress.Phase.RELEASE){getNavigation().stop();return;}
            WitnessEnding.tickCreature(this,player);return;
        }
        if(motion()==WOUNDED){
            Vec3 cell=FinaleController.cellCenter(level.getServer());
            if(cell!=null){Vec3 step=cell.subtract(position()).multiply(1,0,1);if(step.lengthSqr()>.25){setYRot((float)(Math.atan2(step.z,step.x)*180/Math.PI)-90);yBodyRot=getYRot();move(MoverType.SELF,step.normalize().scale(.12));}}
            if(remaining>0)remaining--;
            // A wounded prisoner remains alive, saved in its cell. The explorer owns the collapse clock.
            return;
        }
        // One whose killer-to-be died while it was unloaded is out all the same (0.4.84).
        if(owner!=null&&FinaleProgress.phase(level.getServer(),owner)==FinaleProgress.Phase.IMPRISONED){loose(owner,level.getGameTime());MinotaurLoose.register(level.getServer(),getUUID());return;}
        if(player==null||player.level()!=level||!player.isAlive()||FinaleProgress.phase(level.getServer(),owner)!=FinaleProgress.Phase.FIGHT){
            bar.removeAllPlayers();motion(WATCHING,35);setDeltaMovement(Vec3.ZERO);return;
        }
        bar.addPlayer(player);
        if(motion()==WATCHING||motion()==WINDUP){getLookControl().setLookAt(player,30,30);setYRot((float)(Math.atan2(player.getZ()-getZ(),player.getX()-getX())*180/Math.PI)-90);yBodyRot=getYRot();}
        // It paws the floor before it comes, and kicks the floor up behind it as it does.
        if(motion()==WINDUP&&remaining%6==0)hoof(level,false,remaining%12==0);
        if(motion()==CHARGING){
            Vec3 before=position();move(MoverType.SELF,charge);setDeltaMovement(Vec3.ZERO);
            if(tickCount%2==0)hoof(level,true,tickCount%4==0);
            if(getBoundingBox().inflate(.35).intersects(player.getBoundingBox())){
                Vec3 toward=position().subtract(player.position()).multiply(1,0,1).normalize();
                if(player.isBlocking()&&player.getViewVector(1).multiply(1,0,1).normalize().dot(toward)>.25)blocked(level,player);
                else{player.hurt(damageSources().genericKill(),Float.MAX_VALUE);motion(WATCHING,40);}return;
            }
            if(horizontalCollision||position().distanceToSqr(before)<.01){
                level.playSound(null,blockPosition(),FinaleRegistry.HOOF.get(),SoundSource.HOSTILE,2.2F,.6F);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,floor(level)),getX()+charge.x*1.5,getY()+1.2,getZ()+charge.z*1.5,30,.6,.8,.6,.2);
                motion(WATCHING,40);return;
            }
        }
        if(--remaining>0)return;
        if(motion()==WATCHING||motion()==STUNNED){motion(WINDUP,30);playSound(SoundEvent.createVariableRangeEvent(Growl.Kind.NEAR.sound()),1,.8F);}
        else if(motion()==WINDUP){Vec3 target=player.position().subtract(position()).multiply(1,0,1);charge=target.lengthSqr()<.01?new Vec3(0,0,-.65):target.normalize().scale(.65);motion(CHARGING,35);bellow(level,3F);}
        else motion(WATCHING,40);
    }
    private BlockState floor(net.minecraft.server.level.ServerLevel level){
        BlockState floor=level.getBlockState(BlockPos.containing(getX(),getY()-.3,getZ()));return floor.isAir()?Blocks.DEEPSLATE_TILES.defaultBlockState():floor;
    }
    /** Grit and dust from under a hoof: the front one as it paws, both behind it as it runs. */
    private void hoof(net.minecraft.server.level.ServerLevel level,boolean running,boolean stamp){
        float yaw=getYRot()*Mth.DEG_TO_RAD;double fx=-Mth.sin(yaw),fz=Mth.cos(yaw),side=(tickCount%4<2?.45:-.45);
        double x=getX()+(running?-fx*.8:fx*.75)+fz*side,z=getZ()+(running?-fz*.8:fz*.75)-fx*side;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,floor(level)),x,getY()+.15,z,running?12:7,.35,.08,.35,.18);
        level.sendParticles(ParticleTypes.POOF,x,getY()+.2,z,running?3:1,.25,.05,.25,.02);
        if(stamp)level.playSound(null,x,getY(),z,FinaleRegistry.HOOF.get(),SoundSource.HOSTILE,running?1.8F:1.2F,running?.75F+random.nextFloat()*.15F:.95F);
    }
    /** The bellow as it lowers its horns, with its breath steaming from its nostrils. */
    void bellow(net.minecraft.server.level.ServerLevel level,float volume){
        level.playSound(null,getX(),getEyeY(),getZ(),FinaleRegistry.BELLOW.get(),SoundSource.HOSTILE,volume,.92F+random.nextFloat()*.1F);
        float yaw=getYRot()*Mth.DEG_TO_RAD;level.sendParticles(ParticleTypes.CLOUD,getX()-Mth.sin(yaw)*1.1,getEyeY()-.35,getZ()+Mth.cos(yaw)*1.1,8,.12,.08,.12,.03);
    }
    /** The charge meets a raised shield: the shield takes it, the one behind it still takes two hearts and is thrown back. */
    private void blocked(net.minecraft.server.level.ServerLevel level,ServerPlayer player){
        var hand=player.getUsedItemHand();ItemStack shield=player.getUseItem();
        boolean broke=wearShield(level,player,shield,hand==net.minecraft.world.InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
        player.stopUsingItem();if(!broke&&!shield.isEmpty())player.getCooldowns().addCooldown(shield.getItem(),30);
        DamageSource source=level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(CHARGE).map(type->new DamageSource(type,this)).orElse(damageSources().generic());
        player.hurt(source,BLOCKED_DAMAGE);
        Vec3 push=(charge.lengthSqr()<1e-4?player.position().subtract(position()):charge).multiply(1,0,1);push=push.lengthSqr()<1e-4?new Vec3(0,0,-1):push.normalize();
        player.setDeltaMovement(push.x*1.6,.55,push.z*1.6);player.hurtMarked=true;
        level.playSound(null,player.blockPosition(),SoundEvents.SHIELD_BLOCK,SoundSource.HOSTILE,2F,.55F);
        level.playSound(null,blockPosition(),FinaleRegistry.HOOF.get(),SoundSource.HOSTILE,2.4F,.55F);
        Vec3 at=player.position().add(position()).scale(.5).add(0,1.1,0);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,floor(level)),getX(),getY()+.2,getZ(),26,.8,.1,.8,.25);
        level.sendParticles(ParticleTypes.CRIT,at.x,at.y,at.z,16,.3,.4,.3,.35);
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(player,new io.github.knaitoe.theoldesthouse.network.NovelScenePayload(13,0,"",0,1F));
        stagger();
    }
    /**
     * A blocked charge wears the shield by a share of what it has left, so its durability bar drops with each block. The
     * count is kept on the shield, so the third block breaks that shield however much it had and whatever protects it.
     */
    public static boolean wearShield(net.minecraft.server.level.ServerLevel level,ServerPlayer player,ItemStack shield,EquipmentSlot slot){
        if(shield.isEmpty())return false;
        var data=shield.get(DataComponents.CUSTOM_DATA);int blocks=(data==null?0:data.copyTag().getInt(BLOCKS_TAG))+1;
        if(blocks>=SHIELD_BLOCKS){
            shield.shrink(1);level.broadcastEntityEvent(player,slot==EquipmentSlot.MAINHAND?(byte)47:(byte)48);// the native break animation for that hand
            level.playSound(null,player.blockPosition(),SoundEvents.SHIELD_BREAK,SoundSource.PLAYERS,1.4F,.8F);return true;
        }
        CustomData.update(DataComponents.CUSTOM_DATA,shield,tag->tag.putInt(BLOCKS_TAG,blocks));
        if(shield.isDamageableItem()){int max=shield.getMaxDamage(),left=SHIELD_BLOCKS-blocks+1,remaining=max-shield.getDamageValue();
            shield.setDamageValue(Math.min(max-1,shield.getDamageValue()+Math.max(1,(int)Math.ceil(remaining/(double)left))));}
        return false;
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
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("FinaleOwner",owner);tag.putInt("FinaleMotion",motion());tag.putBoolean("ChildAppearance",childAppearance());tag.putInt("FinaleRemaining",remaining);
        if(motion()==LOOSE){tag.putInt("LooseStage",looseStage);tag.putInt("LooseStep",looseStep);tag.putLong("LooseSince",looseSince);}}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("FinaleOwner")?tag.getUUID("FinaleOwner"):null;
        // Reloads never resume an untelegraphed lethal dash.
        int saved=tag.getInt("FinaleMotion");entityData.set(CHILD,tag.getBoolean("ChildAppearance")||saved==CAGED);motion(saved==CAGED?CAGED:saved==WOUNDED?WOUNDED:saved==RELEASED?RELEASED:saved==LOOSE?LOOSE:WATCHING,saved==WOUNDED?Math.max(1,tag.getInt("FinaleRemaining")):35);
        if(saved==LOOSE){entityData.set(CHILD,false);looseStage=tag.getInt("LooseStage");looseStep=tag.contains("LooseStep")?tag.getInt("LooseStep"):-1;looseSince=tag.getLong("LooseSince");getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1);}
        refreshDimensions();}
    @Override public void remove(RemovalReason reason){bar.removeAllPlayers();super.remove(reason);}
}
