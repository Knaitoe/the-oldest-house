package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing;
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
 * moment before the still seat comes back. 0.4.465: a swing its rider pumps (the park's) keeps how wide its arc is here.
 */
public final class SeatEntity extends Entity {
    private static final EntityDataAccessor<Integer> SWING_START = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> SUPPORT = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.BLOCK_POS);
    /** 0.4.465: how wide a pumped swing's arc is, in radians, to the hundredth; below zero, a swing nobody pumps (the porch's). */
    private static final EntityDataAccessor<Float> SWING_REACH = SynchedEntityData.defineId(SeatEntity.class, EntityDataSerializers.FLOAT);
    /** The server's own figure for it, of which clients are sent the hundredths. */
    private double reach;
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
    /** This swing is one its rider pumps: its arc starts from nothing and is as wide as they make it (HouseReadSwing). */
    public void pump() { reach = 0; entityData.set(SWING_REACH, 0F); }
    public boolean pumped() { return entityData.get(SWING_REACH) >= 0; }
    /** How wide a pumped swing's arc is now. */
    public double reach() { return level().isClientSide() ? entityData.get(SWING_REACH) : reach; }
    /** For tests: the arc as wide as this. */
    public void reach(double to) { reach = Math.max(0, Math.min(HouseReadSwing.HIGH, to)); entityData.set(SWING_REACH, (float) reach); }
    /** The swing's angle from where it hangs at rest, now or {@code ahead} ticks from now. */
    public double swingAngle(double ahead) {
        double t = swingTicks(0) + ahead;
        return pumped() ? reach() * Math.sin(Math.max(0, t) * HouseReadSwing.RATE) : HouseReadSwing.angle(t);
    }
    /** Ticks since the reader sat on the swing, at this partial tick. */
    public double swingTicks(float partial) { return level().getGameTime() + partial - entityData.get(SWING_START); }
    @Override protected boolean canAddPassenger(Entity passenger) { return getPassengers().isEmpty(); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(SWING_START, -1); builder.define(SUPPORT, BlockPos.ZERO); builder.define(SWING_REACH, -1F); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { support(BlockPos.of(tag.getLong("Support"))); if (tag.contains("SwingStart")) entityData.set(SWING_START, tag.getInt("SwingStart")); if (tag.contains("SwingReach")) reach(tag.getDouble("SwingReach")); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putLong("Support", support.asLong()); if (swinging()) tag.putInt("SwingStart", entityData.get(SWING_START)); if (pumped()) tag.putDouble("SwingReach", reach); }
    /**
     * 0.4.465: a tick of a pumped swing. Its rider leaning the way it is going widens the arc; leaning against it narrows it; left
     * alone it comes up, or eases down, to the gentle arc every swing has. With nobody on it, it dies down. Its chains are heard
     * each time it comes through the bottom.
     */
    private void pumping() {
        double t = swingTicks(0), going = Math.cos(t * HouseReadSwing.RATE);
        if (getFirstPassenger() instanceof net.minecraft.world.entity.player.Player rider) {
            float lean = rider.zza;
            if (lean > .1F && going > 0 || lean < -.1F && going < 0) reach += HouseReadSwing.PUMP;
            else if (lean > .1F || lean < -.1F) reach -= HouseReadSwing.PUMP;
            else if (reach < HouseReadSwing.REACH) reach = Math.min(HouseReadSwing.REACH, reach + HouseReadSwing.BUILD);
            else reach = Math.max(HouseReadSwing.REACH, reach - HouseReadSwing.EASE);
        } else reach *= .985;
        reach = Math.max(0, Math.min(HouseReadSwing.HIGH, reach));
        if (Math.abs(reach - entityData.get(SWING_REACH)) >= .01) entityData.set(SWING_REACH, (float) reach);
        if (t > 1 && reach > .12 && Math.sin(t * HouseReadSwing.RATE) * Math.sin((t - 1) * HouseReadSwing.RATE) <= 0)
            level().playSound(null, getX(), getY() + 1.5, getZ(), io.github.knaitoe.theoldesthouse.labyrinth.BikeNightRegistry.SWING_CREAK.get(), net.minecraft.sounds.SoundSource.BLOCKS,
                    (float) Math.min(.8, .2 + reach * .5), (float) (1.05 - reach * .12 + (random.nextFloat() - .5F) * .06F));
    }
    @Override public void tick() {
        super.tick();
        if (swinging()) {
            if (!level().isClientSide() && pumped()) pumping();
            var pair = HouseReadSwing.pair(level(), support());
            var at = HouseReadSwing.seat(HouseReadSwing.pivot(pair), HouseReadSwing.forward(level().getBlockState(pair.getFirst())), swingAngle(0));
            setPos(at.x, at.y, at.z);
        }
        if (!level().isClientSide() && (!isVehicle() || !HouseSitting.isSeat(level().getBlockState(support)))) {
            if (letGo != null && !isVehicle() && swinging() && HouseSitting.isSeat(level().getBlockState(support)) && !backAtRest()) return;
            if (swinging()) HouseReadSwing.restore(level(), support);
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
        double now = Math.abs(swingAngle(0)), before = Math.abs(swingAngle(-1)), after = Math.abs(swingAngle(1));
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
