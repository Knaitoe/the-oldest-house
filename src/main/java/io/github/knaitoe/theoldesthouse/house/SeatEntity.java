package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** An invisible, disposable anchor for vanilla's seated player pose. */
public final class SeatEntity extends Entity {
    private BlockPos support = BlockPos.ZERO;
    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }
    public void support(BlockPos pos) { support = pos.immutable(); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) { support = BlockPos.of(tag.getLong("Support")); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putLong("Support", support.asLong()); }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide() && (!isVehicle() || !HouseSitting.isSeat(level().getBlockState(support)))) {
            ejectPassengers();
            discard();
        }
    }
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        for (BlockPos pos : new BlockPos[]{support.south(), support.east(), support.north(), support.west()}) {
            Vec3 point = Vec3.atBottomCenterOf(pos);
            if (level().getBlockState(pos.below()).isSolid()
                    && level().noCollision(passenger, passenger.getBoundingBox().move(point.subtract(passenger.position())))) return point;
        }
        return position().add(0, 0.6, 0);
    }
}
