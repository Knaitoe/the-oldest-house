package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;

/** Ordinary human bodies. The path apparition is private; every cousin is shared. */
public final class GoatmanChild extends PathfinderMob {
    private static final EntityDataAccessor<Integer> SKIN=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TELLS=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> VIEWER=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> WALK=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SEATED=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.BOOLEAN);
    /** 0.4.53: laughing without a sound, shoulders jerking. */
    private static final EntityDataAccessor<Boolean> COWER=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEAVE=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FEAR=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DISGUISE=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FEAR_STAGE=SynchedEntityData.defineId(GoatmanChild.class,EntityDataSerializers.INT);
    /** A seated child's hips sit this far above its feet (seven model pixels at child scale). */
    public static final double HIPS=7/16D*.7;
    public GoatmanChild(EntityType<? extends GoatmanChild> type,Level level){
        super(type,level);setNoAi(true);setNoGravity(true);setPersistenceRequired();setSilent(true);
    }
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.SCALE,.7).add(Attributes.MOVEMENT_SPEED,0);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){
        super.defineSynchedData(b);b.define(SKIN,0);b.define(TELLS,0);b.define(VIEWER,Optional.empty());b.define(WALK,0F);b.define(SPEED,0F);b.define(SEATED,false);b.define(HEAVE,false);b.define(COWER,false);b.define(DISGUISE,-1);b.define(FEAR,0);b.define(FEAR_STAGE,0);
    }
    public int fear(){return entityData.get(FEAR);}
    public void fear(int role){entityData.set(FEAR,Math.max(0,Math.min(7,role)));}
    public int fearStage(){return entityData.get(FEAR_STAGE);}
    public void fearStage(int stage){entityData.set(FEAR_STAGE,Math.max(0,Math.min(3,stage)));}
    public int skin(){return entityData.get(SKIN);}
    public int visibleSkin(){return entityData.get(DISGUISE)>=0?entityData.get(DISGUISE):skin();}
    public boolean monsterAppearance(){return entityData.get(DISGUISE)==-2;}
    public boolean stalkingAppearance(){return entityData.get(DISGUISE)!=-1;}
    public void disguise(int skin){entityData.set(DISGUISE,Math.max(-2,Math.min(8,skin)));}
    public int tells(){return entityData.get(TELLS);}
    public boolean tell(int flag){return (tells()&flag)!=0;}
    public boolean girl(){return viewer().isPresent();}
    public Optional<UUID> viewer(){return entityData.get(VIEWER);}
    public void appearance(int skin,int tells,UUID viewer){entityData.set(SKIN,Math.floorMod(skin,9));entityData.set(TELLS,tells);entityData.set(VIEWER,Optional.ofNullable(viewer));}
    public float walk(float partial){return entityData.get(WALK)+entityData.get(SPEED)*partial;}
    public float speed(){return entityData.get(SPEED);}
    public boolean seated(){return entityData.get(SEATED);}
    public boolean cowering(){return entityData.get(COWER);}
    public void cower(boolean on){entityData.set(COWER,on);}
    public boolean heaving(){return entityData.get(HEAVE);}
    public void heave(boolean on){if(heaving()!=on)entityData.set(HEAVE,on);}
    /** Stands, sits or walks without a step: the animation phase stays where it is. */
    public void pose(boolean seated){entityData.set(SPEED,0F);entityData.set(SEATED,seated);}
    public void animate(double distance,boolean backwards,boolean seated){
        float step=(float)Math.min(1.6,distance*8)*(backwards?-1:1);
        entityData.set(WALK,entityData.get(WALK)+step);entityData.set(SPEED,step);entityData.set(SEATED,seated);
    }
    @Override public boolean isPushable(){return false;}
    @Override public EntityDimensions getDefaultDimensions(Pose pose){return pose==Pose.SWIMMING?EntityDimensions.scalable(.6F,.6F).withEyeHeight(.4F):super.getDefaultDimensions(pose);}
    @Override public boolean canBeLeashed(){return false;}
    @Override public boolean hurt(DamageSource source,float damage){return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void addAdditionalSaveData(CompoundTag t){
        super.addAdditionalSaveData(t);t.putInt("Skin",skin());t.putInt("Tells",tells());t.putFloat("Walk",entityData.get(WALK));t.putBoolean("Seated",seated());t.putBoolean("Cowering",cowering());t.putBoolean("Heaving",heaving());t.putInt("Disguise0465",entityData.get(DISGUISE));t.putInt("Fear0472",fear());t.putInt("FearStage0472",fearStage());viewer().ifPresent(id->t.putUUID("Viewer",id));
    }
    @Override public void readAdditionalSaveData(CompoundTag t){
        super.readAdditionalSaveData(t);fear(t.getInt("Fear0472"));fearStage(t.getInt("FearStage0472"));appearance(t.getInt("Skin"),t.getInt("Tells"),t.hasUUID("Viewer")?t.getUUID("Viewer"):null);entityData.set(WALK,t.getFloat("Walk"));entityData.set(SEATED,t.getBoolean("Seated"));entityData.set(COWER,t.getBoolean("Cowering"));heave(t.getBoolean("Heaving"));disguise(t.contains("Disguise0465")?t.getInt("Disguise0465"):-1);
    }
}
