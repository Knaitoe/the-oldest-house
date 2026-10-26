package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** An actual native riding anchor, bound to one car and one driver across saves. */
public final class BackseatSeat extends Entity {
    private static final EntityDataAccessor<BlockPos> ROOT=SynchedEntityData.defineId(BackseatSeat.class,EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Optional<UUID>> OWNER=SynchedEntityData.defineId(BackseatSeat.class,EntityDataSerializers.OPTIONAL_UUID);
    public BackseatSeat(EntityType<? extends BackseatSeat> type,Level level){super(type,level);setNoGravity(true);noPhysics=true;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(ROOT,BlockPos.ZERO);b.define(OWNER,Optional.empty());}
    public BlockPos root(){return entityData.get(ROOT);}
    public Optional<UUID> owner(){return entityData.get(OWNER);}
    public void bind(BlockPos root,UUID owner){entityData.set(ROOT,root.immutable());entityData.set(OWNER,Optional.of(owner));moveTo(root.getX()+.5,root.getY(),root.getZ()+.5);}
    public BackseatCarBlock.Car car(){return level().getBlockEntity(root()) instanceof BackseatCarBlock.Car c?c:null;}
    @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty()&&owner().filter(e.getUUID()::equals).isPresent();}
    @Override protected void positionRider(Entity passenger,Entity.MoveFunction move){if(hasPassenger(passenger)){var at=position().add(BackseatCarMesh.DRIVER);move.accept(passenger,at.x,at.y,at.z);}}
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){return Vec3.atBottomCenterOf(root().offset(-2,0,0));}
    @Override public void tick(){super.tick();if(!level().isClientSide&&level().hasChunkAt(root())&&car()==null){ejectPassengers();discard();}}
    @Override protected void readAdditionalSaveData(CompoundTag t){entityData.set(ROOT,BlockPos.of(t.getLong("Car")));entityData.set(OWNER,t.hasUUID("Owner")?Optional.of(t.getUUID("Owner")):Optional.empty());}
    @Override protected void addAdditionalSaveData(CompoundTag t){t.putLong("Car",root().asLong());owner().ifPresent(id->t.putUUID("Owner",id));}
}
