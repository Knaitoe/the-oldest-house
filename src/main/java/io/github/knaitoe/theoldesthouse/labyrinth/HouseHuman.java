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
    private int stagger,attackClock,patrolIndex,patrolClock,speechClock;
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
        if(level().isClientSide||!(source.getEntity() instanceof ServerPlayer p)||!HollowayVignette.inside(p))return false;
        HollowayVignette.provoke(p,true);stagger=40;getNavigation().stop();
        // Native damage and red hurt animation, with the persistent encounter actor kept alive.
        float injury=Math.min(amount,Math.max(0,getHealth()-4));boolean hurt=injury>0&&super.hurt(source,injury);
        if(!hurt){hurtTime=10;hurtDuration=10;level().broadcastEntityEvent(this,(byte)2);playSound(SoundEvents.PLAYER_HURT,.7F,.8F);}
        if(level() instanceof net.minecraft.server.level.ServerLevel l)l.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,getX(),getY()+1,getZ(),5,.2,.25,.2,.03);
        return true;
    }
    public boolean staggered(){return stagger>0;}
    public boolean attackReady(){return attackClock<=0;}
    public void attacked(){attackClock=45;}
    public void blocked(){stagger=35;getNavigation().stop();}
    public void say(String words){
        if(!(level() instanceof net.minecraft.server.level.ServerLevel l))return;
        var display=EntityType.TEXT_DISPLAY.create(l);if(display==null)return;
        for(var old:l.getEntitiesOfClass(Display.TextDisplay.class,getBoundingBox().inflate(5),e->e.getTags().contains("HouseHollowaySpeech")))if(old.getPersistentData().hasUUID("Speaker")&&old.getPersistentData().getUUID("Speaker").equals(getUUID()))old.discard();
        var data=new CompoundTag();display.saveWithoutId(data);data.putString("text",net.minecraft.network.chat.Component.Serializer.toJson(net.minecraft.network.chat.Component.literal(words),l.registryAccess()));data.putString("billboard","center");data.putInt("line_width",180);data.putInt("background",0x66000000);data.putBoolean("see_through",false);data.putInt("teleport_duration",3);display.load(data);
        display.moveTo(getX(),getY()+2.35,getZ());display.addTag("HouseHollowaySpeech");display.getPersistentData().putUUID("Speaker",getUUID());display.getPersistentData().putLong("Until",l.getGameTime()+110);l.addFreshEntity(display);
    }
    public void patrol(net.minecraft.core.BlockPos base,java.util.List<ServerPlayer> visitors){
        if(staggered()){getNavigation().stop();return;}
        int[][] stops={{-3,-9},{3,-6},{4,-10},{0,-16},{-6,-20},{6,-20},{0,-15}};
        var nearest=visitors.stream().min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if(nearest!=null&&distanceToSqr(nearest)<144)getLookControl().setLookAt(nearest,25,25);
        if(patrolClock--<=0||getNavigation().isDone()){
            int[] stop=stops[patrolIndex++%stops.length];patrolClock=100;
            getNavigation().moveTo(base.getX()+stop[0]+.5,base.getY(),base.getZ()+stop[1]+.5,.65);
        }
        if(speechClock--<=0){speechClock=180+getRandom().nextInt(100);
            String[] lines={"Sixteen. There were sixteen torches.","That wall was farther away yesterday.","Don't move my things. I put them where I can see them.","I heard you before the door opened. I heard someone behind you, too.","The map isn't wrong. The rooms are wrong.","If you touch the barrel again, I will know."};
            if(nearest!=null&&distanceToSqr(nearest)<225)say(lines[(patrolIndex/2)%lines.length]);
        }
    }
    @Override public void tick(){super.tick();if(!level().isClientSide){if(stagger>0)stagger--;if(attackClock>0)attackClock--;}}
    @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putInt("Stagger",stagger);tag.putInt("AttackClock",attackClock);tag.putInt("PatrolIndex",patrolIndex);tag.putInt("SpeechClock",speechClock);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);stagger=tag.getInt("Stagger");attackClock=tag.getInt("AttackClock");patrolIndex=Math.max(0,tag.getInt("PatrolIndex"));speechClock=tag.getInt("SpeechClock");pursue(null);}
}
