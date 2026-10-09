package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The Goatman as it is when it is not pretending (0.4.53): seen from behind in the woods, at the edge of a haunted
 * reader's view, going into the trees. Always private to one viewer, never saved, never hurt, moved only by its scene.
 */
public final class GoatmanFigure extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> VIEWER=SynchedEntityData.defineId(GoatmanFigure.class,EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> HEAVE=SynchedEntityData.defineId(GoatmanFigure.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> WALK=SynchedEntityData.defineId(GoatmanFigure.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPEED=SynchedEntityData.defineId(GoatmanFigure.class,EntityDataSerializers.FLOAT);
    /** Server-side only: when this appearance ends, and the scene that owns it. */
    public long until;public String purpose="";
    public GoatmanFigure(EntityType<? extends GoatmanFigure> type,Level level){
        super(type,level);setNoAi(true);setNoGravity(true);setSilent(true);setInvulnerable(true);noPhysics=true;
    }
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.MOVEMENT_SPEED,0);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(VIEWER,Optional.empty());b.define(HEAVE,false);b.define(WALK,0F);b.define(SPEED,0F);}
    public Optional<UUID> viewer(){return entityData.get(VIEWER);}
    public void viewer(UUID id){entityData.set(VIEWER,Optional.of(id));}
    public boolean heaving(){return entityData.get(HEAVE);}
    public void heave(boolean on){entityData.set(HEAVE,on);}
    public float walk(float partial){return entityData.get(WALK)+entityData.get(SPEED)*partial;}
    public float speed(){return entityData.get(SPEED);}
    /** Moves it exactly, and turns its legs by the distance covered. */
    public void step(Vec3 at,float yaw){
        double distance=position().distanceTo(at);moveTo(at.x,at.y,at.z,yaw,0);setYHeadRot(yaw);yBodyRot=yaw;
        float stride=(float)Math.min(1,distance*3);entityData.set(WALK,entityData.get(WALK)+stride);entityData.set(SPEED,stride);
    }
    public void face(float yaw){setYRot(yaw);setYHeadRot(yaw);yBodyRot=yaw;entityData.set(SPEED,0F);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public boolean isPushable(){return false;}
    @Override public boolean canBeLeashed(){return false;}
    @Override public boolean isPickable(){return false;}
    @Override public boolean hurt(DamageSource source,float damage){return false;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected void doPush(Entity entity){}
}
