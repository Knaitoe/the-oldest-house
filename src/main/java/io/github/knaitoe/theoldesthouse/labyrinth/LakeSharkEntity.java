package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.MovieNightPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The shark from the film, out on the real lake (0.4.102). Only the reader it came for is ever sent it; it has no body to
 * collide with, takes and deals no damage and is never saved. It circles that reader's canoe with its fin up, goes under the
 * hull once (the canoe lifts, something knocks), and sinks away. There are no sharks in a lake.
 */
public final class LakeSharkEntity extends Entity {
    private static final EntityDataAccessor<Float> JAW=SynchedEntityData.defineId(LakeSharkEntity.class,EntityDataSerializers.FLOAT);
    public static final int CIRCLE=140,DIVE=40,SINK=60;
    private static final Map<UUID,LakeSharkEntity> OWNED=new HashMap<>();
    @Nullable private UUID owner;
    @Nullable private BlockPos base;
    private int age;
    private double angle,exitX,exitZ;
    private boolean seen,bumped;

    public LakeSharkEntity(EntityType<? extends LakeSharkEntity> type,Level level){super(type,level);noPhysics=true;setSilent(true);setNoGravity(true);setInvulnerable(true);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(JAW,0F);}
    public float jaw(){return entityData.get(JAW);}
    public @Nullable UUID owner(){return owner;}
    public int age(){return age;}
    public boolean seen(){return seen;}

    /** Sends one shark to circle this reader's canoe, unless one is already out there for them. */
    public static @Nullable LakeSharkEntity release(ServerPlayer reader,BlockPos base){
        var old=OWNED.get(reader.getUUID());if(old!=null&&!old.isRemoved())return null;
        var shark=MovieNightRegistry.SHARK.get().create(reader.serverLevel());if(shark==null)return null;
        shark.owner=reader.getUUID();shark.base=base.immutable();shark.angle=Math.atan2(reader.getZ()-base.getZ()+50,reader.getX()-base.getX())+Math.PI;
        Vec3 at=shark.ring(reader.position(),8);shark.moveTo(at.x,at.y,at.z,0,0);
        if(!reader.serverLevel().addFreshEntity(shark))return null;
        OWNED.put(reader.getUUID(),shark);return shark;
    }
    public static @Nullable LakeSharkEntity of(UUID reader){var s=OWNED.get(reader);return s==null||s.isRemoved()?null:s;}
    public static void clearAll(){OWNED.clear();}
    /** The water's surface here: where the fin breaks it, with the body a little under. */
    private double surface(){return base==null?getY():base.getY()-.12;}
    private Vec3 ring(Vec3 center,double radius){return new Vec3(center.x+Math.cos(angle)*radius,surface()-.85,center.z+Math.sin(angle)*radius);}

    @Override public void tick(){
        super.tick();
        if(!(level() instanceof ServerLevel level))return;
        var reader=owner==null?null:level.getServer().getPlayerList().getPlayer(owner);
        if(reader==null||!reader.isAlive()||reader.level()!=level||base==null||!LiteraryVignettes.inside(reader,LabyrinthPlace.MOVIE_NIGHT)||age>CIRCLE+DIVE+SINK){discard();return;}
        age++;Vec3 center=reader.getVehicle()!=null?reader.getVehicle().position():reader.position();Vec3 next;
        if(age<=CIRCLE){
            // Round the canoe, closing in, with the fin cutting the surface.
            angle+=.075;double radius=7.5-2*(age/(double)CIRCLE);next=ring(center,radius);
            if(age%3==0)level.sendParticles(reader,ParticleTypes.SPLASH,true,next.x,surface()+.1,next.z,4,.25,0,.25,0);
            if(!seen&&HouseWatchers.sees(reader,next.add(0,1.3,0))){seen=true;MovieNight.sharkSeen(reader);}
            entityData.set(JAW,0F);
            if(age==CIRCLE){exitX=center.x*2-next.x;exitZ=center.z*2-next.z;}
        }else if(age<=CIRCLE+DIVE){
            // Under the hull and out the far side, deep enough that nothing shows.
            double t=(age-CIRCLE)/(double)DIVE,startX=center.x*2-exitX,startZ=center.z*2-exitZ;
            next=new Vec3(startX+(exitX-startX)*t,surface()-.85-1.9*Math.sin(Math.min(1,t*2)*Math.PI*.5),startZ+(exitZ-startZ)*t);
            entityData.set(JAW,(float)Math.max(0,Math.sin(t*Math.PI)));
            if(!bumped&&t>=.5){bumped=true;HousePackets.send(reader,new MovieNightPayload(MovieNightPayload.ROCK,36));
                ThreeFingers.hear(reader,MovieNightRegistry.BUMP.get(),BlockPos.containing(center).below(),1,1);
                level.sendParticles(reader,ParticleTypes.SPLASH,true,center.x,surface()+.1,center.z,24,.8,0,.8,0);}
        }else{
            // Away and down into the dark.
            double t=(age-CIRCLE-DIVE)/(double)SINK;Vec3 heading=new Vec3(exitX-center.x,0,exitZ-center.z);heading=heading.lengthSqr()<.01?new Vec3(1,0,0):heading.normalize();
            next=new Vec3(getX()+heading.x*.18,surface()-2.75-4*t,getZ()+heading.z*.18);entityData.set(JAW,0F);
        }
        Vec3 step=next.subtract(position());
        if(step.horizontalDistanceSqr()>1e-6)setYRot((float)Math.toDegrees(Math.atan2(-step.x,step.z)));
        setXRot((float)Math.toDegrees(-Math.atan2(step.y,Math.max(.05,step.horizontalDistance())))*.6F);
        setPos(next.x,next.y,next.z);
    }
    @Override public void remove(RemovalReason reason){if(owner!=null&&OWNED.get(owner)==this)OWNED.remove(owner);super.remove(reason);}
    @Override public boolean broadcastToPlayer(ServerPlayer player){return owner!=null&&owner.equals(player.getUUID());}
    @Override public boolean isPickable(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean isPushedByFluid(){return false;}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override protected void doWaterSplashEffect(){}
    @Override public boolean shouldBeSaved(){return false;}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
}
