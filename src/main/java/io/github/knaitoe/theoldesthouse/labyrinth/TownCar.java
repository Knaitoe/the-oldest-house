package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.484, Bike night (the proposal's traffic: "a new, simple actor, a car driving its lane with its headlights on"): the body of one
 * of Calder Falls's cars. It is a puppet. Where it is and which way it points are the night's traffic's to say, every tick
 * (CalderTraffic), and it has no physics of its own: nothing moves it and it moves nothing. Left alone for two
 * seconds it is gone, so a car whose night has ended does not stand in the road. What its driver calls out is a caption, as a
 * person's line is (Voiced, client.SpeechCaptions). It is never saved, takes nobody, and a hand does nothing to it.
 */
public final class TownCar extends Entity implements Voiced {
    private static final EntityDataAccessor<Integer> KIND=SynchedEntityData.defineId(TownCar.class,EntityDataSerializers.INT);
    /**
     * The four that drive: a station wagon with wood panels, a pickup, a long sedan, and a muscle car with its windows down. And two
     * that stand parked all night (0.4.485): a sedan and a pickup in other paint.
     */
    public static final int WAGON=0,PICKUP=1,SEDAN=2,MUSCLE=3,BLUE=4,WHITE=5,MOVING=4,KINDS=6;
    public static final String[] NAMES={"wagon","pickup","sedan","muscle","blue","white"};
    /** Half a car's length and half its width, in blocks; and how long one goes untold where it is before it is gone, in ticks. */
    public static final double LONG=2.25,WIDE=.95;
    public static final int LEFT_ALONE=40;
    private long told;private int spokenFor;
    /** On the client: how far it has rolled, for its wheels. */
    private double rolled,rolledBefore;

    public TownCar(EntityType<? extends TownCar> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,0);}
    public int kind(){return entityData.get(KIND);}
    public void kind(int kind){entityData.set(KIND,Mth.clamp(kind,0,KINDS-1));}
    /** Where it is this tick, and the way it points. */
    public void place(Vec3 at,float yaw){told=level().getGameTime();setPos(at.x,at.y,at.z);setYRot(yaw);}
    /** Its driver calls something out. */
    public void say(String line){setCustomName(Component.literal(line));setCustomNameVisible(true);spokenFor=Dialogue.ticks(line);}
    public boolean saying(){return spokenFor>0;}
    @Override public String speaker(){return "";}
    @Override public void tick(){
        super.tick();
        if(level().isClientSide){rolledBefore=rolled;rolled+=Math.hypot(getX()-xo,getZ()-zo);return;}
        if(spokenFor>0&&--spokenFor==0)setCustomNameVisible(false);
        if(level().getGameTime()-told>LEFT_ALONE)discard();
    }
    /** How far it has rolled, on the client. */
    public float rolled(float partial){return (float)Mth.lerp(partial,rolledBefore,rolled);}
    /** Whether somewhere on the ground is under it, with that much to spare all round. */
    public boolean over(Vec3 at,double spare){
        double rad=Math.toRadians(getYRot()),dx=at.x-getX(),dz=at.z-getZ();
        double along=-Math.sin(rad)*dx+Math.cos(rad)*dz,across=Math.cos(rad)*dx+Math.sin(rad)*dz;
        return Math.abs(along)<LONG+spare&&Math.abs(across)<WIDE+spare;
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
    @Override public boolean isPickable(){return false;}
    @Override public boolean canBeHitByProjectile(){return false;}
    @Override public boolean shouldRenderAtSqrDistance(double distance){return distance<160*160;}
    @Override public AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(LONG,.4,LONG);}
}
