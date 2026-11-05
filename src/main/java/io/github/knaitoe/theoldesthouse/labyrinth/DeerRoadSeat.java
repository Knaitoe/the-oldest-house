package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 0.4.310: a native riding anchor in the sedan, the driver's seat (0) or the front passenger's (1), bound to the road's anchor. */
public final class DeerRoadSeat extends Entity {
    private static final EntityDataAccessor<BlockPos> ANCHOR=SynchedEntityData.defineId(DeerRoadSeat.class,EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> SLOT=SynchedEntityData.defineId(DeerRoadSeat.class,EntityDataSerializers.INT);
    public DeerRoadSeat(EntityType<? extends DeerRoadSeat> type,Level level){super(type,level);setNoGravity(true);noPhysics=true;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(ANCHOR,BlockPos.ZERO);b.define(SLOT,0);}
    public BlockPos anchor(){return entityData.get(ANCHOR);}
    public int slot(){return entityData.get(SLOT);}
    public void bind(BlockPos anchor,int slot){entityData.set(ANCHOR,anchor.immutable());entityData.set(SLOT,slot);}
    public DeerRoadScene scene(){return level().getBlockEntity(anchor()) instanceof DeerRoadScene s?s:null;}
    public Vec3 offset(){return slot()==0?DeerCarMesh.DRIVER_SEAT:DeerCarMesh.PASSENGER_SEAT;}
    @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty();}
    @Override protected void positionRider(Entity passenger,Entity.MoveFunction move){if(hasPassenger(passenger)){var at=position().add(offset());move.accept(passenger,at.x,at.y,at.z);}}
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){var s=scene();return s==null?position().add(0,0,0):s.dismount(slot());}
    @Override public void tick(){super.tick();if(!level().isClientSide&&level().isLoaded(anchor())&&scene()==null){ejectPassengers();discard();}}
    @Override protected void readAdditionalSaveData(CompoundTag t){entityData.set(ANCHOR,BlockPos.of(t.getLong("Anchor")));entityData.set(SLOT,t.getInt("Slot"));}
    @Override protected void addAdditionalSaveData(CompoundTag t){t.putLong("Anchor",anchor().asLong());t.putInt("Slot",slot());}
}
