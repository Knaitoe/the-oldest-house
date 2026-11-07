package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
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
import net.minecraft.world.phys.AABB;

/**
 * 0.4.342, the Sphinx in the dream (docs/SPHINX_DREAM_0_4_342.md). The owner: "The dream should build the sphinx's whole body as a
 * massive entity", and "It should be so large as to occupy most of the player's screen." Her whole body: a lion's body some fifty
 * blocks long, so that the dream's house is small under her, wings that open to nearly two hundred blocks, and the face from the first
 * crossing some twenty blocks across. Only the reader whose dream it is is ever sent her; she has no body to
 * touch, takes no harm and is never saved. SphinxDream moves her: her forepaws on the ridge of the dream's house and her body sloping down
 * behind it, then up over the yard and away over the fields. She animates herself from her phase: folded and breathing while she crouches, wings beating once she flies.
 */
public final class SphinxDreamEntity extends Entity {
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(SphinxDreamEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CONDUCT=SynchedEntityData.defineId(SphinxDreamEntity.class,EntityDataSerializers.INT);
    /** How high the roof's ridge her forepaws rest on stands above the yard, in blocks: it sets how steeply her body slopes down behind the house. */
    private static final EntityDataAccessor<Float> PERCH=SynchedEntityData.defineId(SphinxDreamEntity.class,EntityDataSerializers.FLOAT);
    /** Crouched on the roof, looking down at the yard; and flying. */
    public static final int PERCHED=0,FLYING=1;
    @Nullable private UUID owner;
    /** Client only: when this body was seen to start its current phase, for the wings' fold and beat. */
    public int phaseSeenAt;

    public SphinxDreamEntity(EntityType<? extends SphinxDreamEntity> type,Level level){super(type,level);noPhysics=true;setNoGravity(true);setInvulnerable(true);setSilent(true);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(PHASE,PERCHED);b.define(CONDUCT,0);b.define(PERCH,8F);}
    public int phase(){return entityData.get(PHASE);}
    public int conduct(){return entityData.get(CONDUCT);}
    public void phase(int phase){entityData.set(PHASE,phase);}
    public void conduct(int conduct){entityData.set(CONDUCT,conduct);}
    public float perch(){return entityData.get(PERCH);}
    public void perch(float height){entityData.set(PERCH,height);}
    /** Server only: where her flight began. */
    private net.minecraft.world.phys.Vec3 launch=net.minecraft.world.phys.Vec3.ZERO;
    public void launch(net.minecraft.world.phys.Vec3 at){launch=at;}
    public net.minecraft.world.phys.Vec3 launchPoint(){return launch;}
    public @Nullable UUID owner(){return owner;}
    public void owner(UUID owner){this.owner=owner;}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(PHASE.equals(key))phaseSeenAt=tickCount;}

    @Override public void tick(){
        super.tick();
        // With nobody to dream her she is gone; SphinxDream stands her again if the dream resumes.
        if(level() instanceof ServerLevel level){var p=owner==null?null:level.getServer().getPlayerList().getPlayer(owner);if(p==null||p.level()!=level)discard();}
    }
    @Override public boolean broadcastToPlayer(ServerPlayer player){return owner!=null&&owner.equals(player.getUUID());}
    /** Her wings reach far past her body: she is drawn whenever any of her could be on screen. */
    @Override public AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(160);}
    @Override public boolean shouldRenderAtSqrDistance(double d){return d<320*320;}
    @Override public boolean isPickable(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean isPushedByFluid(){return false;}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean hurt(DamageSource source,float amount){return false;}
    @Override public boolean shouldBeSaved(){return false;}
    @Override protected void readAdditionalSaveData(CompoundTag tag){}
    @Override protected void addAdditionalSaveData(CompoundTag tag){}
}
