package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * An invisible, disposable anchor for vanilla's seated player pose. 0.4.172: on the porch's rope swing it also swings, from the
 * time the reader sat down (HouseReadSwing), on the client and the server alike. 0.4.443: after a jump it swings on empty for a
 * moment before the still seat comes back.
 */
public final class SeatEntity extends Entity {
    private static final EntityDataAccessor<Integer> SWING_START = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> SUPPORT = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.BLOCK_POS);
    private BlockPos support = BlockPos.ZERO;
    /** 0.4.423: where a reader who jumped off the swing let go, so they leave from the seat rather than step off beside it. */
    private Vec3 letGo;
    /**
     * 0.4.443: ticks the swing has gone on empty since a reader jumped off it. With the push forward a jump now carries them across where
     * the still seat hangs, so the empty seat keeps swinging until they have had time to land and it next comes through the bottom of
     * its arc. The still seat takes its place there, where the two are in the same place, and not under the jumper's feet.
     */
    private int empty;
    /** A jump is down within this many ticks; an empty swing is put back after this many whatever its arc. */
    public static final int CLEAR = 12, GIVE_UP = 60;
    public SeatEntity(EntityType<? extends SeatEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }
    public void support(BlockPos pos) { support = pos.immutable(); entityData.set(SUPPORT, support); }
    public BlockPos support() { return entityData.get(SUPPORT); }
    /** Starts the swing from this game tick. */
    public void swing(long now) { entityData.set(SWING_START, (int) now); }
    /** The reader is jumping off from here (HouseReadSwing.jump). */
    public void letGo(Vec3 at) { letGo = at; }
    public boolean swinging() { return entityData.get(SWING_START) >= 0; }
    /** Ticks since the reader sat on the swing, at this partial tick. */
    public double swingTicks(float partial) { return level().getGameTime() + partial - entityData.get(SWING_START); }
    @Override protected boolean canAddPassenger(Entity passenger) { return getPassengers().isEmpty(); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(SWING_START, -1); builder.define(SUPPORT, BlockPos.ZERO); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { support(BlockPos.of(tag.getLong("Support"))); if (tag.contains("SwingStart")) entityData.set(SWING_START, tag.getInt("SwingStart")); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putLong("Support", support.asLong()); if (swinging()) tag.putInt("SwingStart", entityData.get(SWING_START)); }
    @Override public void tick() {
        super.tick();
        if (swinging()) {
            var pair = io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.pair(level(), support());
            var at = io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.seat(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.pivot(pair), io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.forward(level().getBlockState(pair.getFirst())), io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.angle(swingTicks(0)));
            setPos(at.x, at.y, at.z);
        }
        if (!level().isClientSide() && (!isVehicle() || !HouseSitting.isSeat(level().getBlockState(support)))) {
            if (letGo != null && !isVehicle() && swinging() && HouseSitting.isSeat(level().getBlockState(support)) && !backAtRest()) return;
            if (swinging()) io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.restore(level(), support);
            ejectPassengers();
            discard();
        }
    }
    /**
     * After a jump: the jumper has had time to land, and this is the tick on which the empty seat is nearest the bottom of its arc
     * (or it has swung long enough).
     */
    private boolean backAtRest() {
        if (++empty < CLEAR) return false;
        double t = swingTicks(0);
        double now = Math.abs(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.angle(t)), before = Math.abs(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.angle(t - 1)), after = Math.abs(io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.angle(t + 1));
        return now <= before && now <= after || empty >= GIVE_UP;
    }
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (letGo != null) return letGo;
        for (BlockPos pos : new BlockPos[]{support.south(), support.east(), support.north(), support.west()}) {
            Vec3 point = Vec3.atBottomCenterOf(pos);
            if (level().getBlockState(pos.below()).isSolid()
                    && level().noCollision(passenger, passenger.getBoundingBox().move(point.subtract(passenger.position())))) return point;
        }
        return position().add(0, 0.6, 0);
    }
}
