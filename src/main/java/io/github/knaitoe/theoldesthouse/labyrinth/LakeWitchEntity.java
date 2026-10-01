package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** Stacey Graves. A bounded shore pathfinder whose nodes and physical movement both reject refuges. */
public final class LakeWitchEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> STRIKING = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MEMORY_PHASE = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.INT);
    @Nullable private UUID memoryOwner;
    @Nullable private BlockPos memoryBase;
    @Nullable private BlockPos shoreBase;
    private final Deque<Vec3> route = new ArrayDeque<>();
    @Nullable private BlockPos routeGoal;
    private int windup, cooldown, visit;

    public LakeWitchEntity(EntityType<? extends LakeWitchEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setPathfindingMalus(PathType.WATER, -1);
        setPathfindingMalus(PathType.WATER_BORDER, -1);
        setPathfindingMalus(PathType.BREACH, -1);
    }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 36).add(Attributes.MOVEMENT_SPEED, .25)
                .add(Attributes.ATTACK_DAMAGE, 6).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.KNOCKBACK_RESISTANCE, .65);
    }
    @Override protected void registerGoals() {}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(STRIKING, false); builder.define(MEMORY_PHASE, -1); }
    public boolean memory() { return entityData.get(MEMORY_PHASE) >= 0; }
    public int memoryPhase() { return entityData.get(MEMORY_PHASE); }
    public void memoryPhase(int phase) { entityData.set(MEMORY_PHASE, phase); }
    public @Nullable UUID memoryOwner() { return memoryOwner; }
    public @Nullable BlockPos memoryBase() { return memoryBase; }
    public void recollection(UUID owner, BlockPos base) {
        memoryOwner=owner;memoryBase=base.immutable();shoreBase=null;memoryPhase(0);setNoAi(true);
    }
    @Override protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if(memory()) {
            if(player instanceof ServerPlayer serverPlayer && hand==net.minecraft.world.InteractionHand.MAIN_HAND) Shallows.lift(serverPlayer,this);
            return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player,hand);
    }
    @Override public boolean hurt(DamageSource source,float amount) { return !memory() && super.hurt(source,amount); }
    public boolean striking() { return entityData.get(STRIKING); }
    public void shore(BlockPos base, int visit) { shoreBase = base.immutable(); this.visit = visit; }
    public @Nullable BlockPos shoreBase() { return shoreBase; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean canBeLeashed() { return false; }

    /** Water and grass are rejected at every footprint corner, including knockback and inertia. */
    public static boolean safeGround(Level level, BlockPos feet) {
        return level.getFluidState(feet).is(FluidTags.WATER)
                || level.getBlockState(feet.below()).is(Blocks.GRASS_BLOCK);
    }
    public static boolean walkable(Level level, BlockPos base, BlockPos feet) {
        int x = feet.getX() - base.getX(), z = feet.getZ() - base.getZ();
        return feet.getY() == base.getY() && x >= -28 && x <= 28 && z >= -11 && z <= -1
                && !safeGround(level, feet)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP);
    }
    public static boolean canAttack(LakeWitchEntity witch, ServerPlayer player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator() && witch.level() == player.level()
                && witch.shoreBase != null && DrownedTown.contains(witch.shoreBase, player.position())
                && !safeGround(player.level(), player.blockPosition()) && !player.isInWaterOrBubble();
    }
    @Override public void move(MoverType type, Vec3 movement) {
        if (!level().isClientSide() && shoreBase != null && (Math.abs(movement.x) > .00001 || Math.abs(movement.z) > .00001)) {
            double half = getBbWidth() / 2.0 + .015;
            boolean allowed = true;
            // Sample a swept route, so fast external forces cannot jump across a grass patch.
            int steps = Math.max(1, (int)Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z)) * 4));
            for (int step = 1; step <= steps && allowed; step++) for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) {
                BlockPos at = BlockPos.containing(getX() + movement.x * step / steps + sx * half,
                        shoreBase.getY(), getZ() + movement.z * step / steps + sz * half);
                if (!walkable(level(), shoreBase, at)) allowed = false;
            }
            if (!allowed) { movement = new Vec3(0, movement.y, 0); route.clear(); setDeltaMovement(0, getDeltaMovement().y, 0); }
        }
        super.move(type, movement);
    }

    /** Four-neighbor breadth-first paths detour around grass, props and trees; water has no node. */
    public static List<BlockPos> shoreRoute(Level level, BlockPos base, BlockPos start, BlockPos goal) {
        if (!walkable(level, base, start) || !walkable(level, base, goal)) return List.of();
        Map<BlockPos, BlockPos> previous = new HashMap<>(); ArrayDeque<BlockPos> open = new ArrayDeque<>();
        previous.put(start, start); open.add(start);
        while (!open.isEmpty() && previous.size() <= 700) {
            BlockPos at = open.removeFirst();
            if (at.equals(goal)) {
                LinkedList<BlockPos> result = new LinkedList<>();
                while (!at.equals(start)) { result.addFirst(at); at = previous.get(at); }
                return result;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = at.relative(direction);
                if (!previous.containsKey(next) && walkable(level, base, next)) { previous.put(next, at); open.addLast(next); }
            }
        }
        return List.of();
    }
    @Override public void tick() {
        super.tick();
        if(memory()) { setAirSupply(300); Shallows.tickActor(this); return; }
        if (!(level() instanceof ServerLevel level) || shoreBase == null || !isAlive()) return;
        if (cooldown > 0) cooldown--;
        ServerPlayer target = level.players().stream().filter(p -> canAttack(this, p))
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (target == null || distanceToSqr(target) > 1600) { cancelStrike(); route.clear(); setDeltaMovement(0, getDeltaMovement().y, 0); return; }
        getLookControl().setLookAt(target, 30, 30);
        setYRot((float)(Math.atan2(target.getZ() - getZ(), target.getX() - getX()) * 180 / Math.PI) - 90); yBodyRot = getYRot();
        if (distanceToSqr(target) < 784 && getSensing().hasLineOfSight(target))
            IndianLakeProgress.hunted(LabyrinthData.get(level.getServer()), target.getUUID());
        if (windup > 0) {
            if (--windup == 0) {
                entityData.set(STRIKING, false); cooldown = 45;
                if (canAttack(this, target) && distanceToSqr(target) < 5.8 && getSensing().hasLineOfSight(target)) {
                    target.hurt(damageSources().mobAttack(this), 6);
                    Vec3 away = target.position().subtract(position()).multiply(1, 0, 1).normalize();
                    target.setDeltaMovement(away.scale(.3).add(0, .12, 0)); target.hurtMarked = true;
                }
            }
            return;
        }
        if (distanceToSqr(target) < 5.0 && cooldown == 0 && getSensing().hasLineOfSight(target)) {
            windup = 18; entityData.set(STRIKING, true);
            level.playSound(null, blockPosition(), DrownedTownRegistry.WITCH_VOICE.get(), SoundSource.HOSTILE, .8F, .9F); return;
        }
        BlockPos goal = BlockPos.containing(target.getX(), shoreBase.getY(), target.getZ());
        if (!walkable(level, shoreBase, goal)) return;
        if (tickCount % 20 == 0 || !goal.equals(routeGoal) || route.isEmpty()) {
            route.clear(); routeGoal = goal;
            for (BlockPos step : shoreRoute(level, shoreBase, BlockPos.containing(getX(), shoreBase.getY(), getZ()), goal))
                route.add(Vec3.atBottomCenterOf(step));
        }
        if (!route.isEmpty()) {
            Vec3 step = route.peek().subtract(position()).multiply(1, 0, 1);
            if (step.lengthSqr() < .02) route.removeFirst();
            else move(MoverType.SELF, step.normalize().scale(Math.min(.155, step.length())));
        }
        if (tickCount % 160 == 0 && distanceToSqr(target) < 400)
            level.playSound(null, blockPosition(), DrownedTownRegistry.WITCH_VOICE.get(), SoundSource.HOSTILE, .45F, .7F);
    }
    private void cancelStrike() { windup = 0; entityData.set(STRIKING, false); }
    @Override public void die(DamageSource source) {
        if (!memory() && level() instanceof ServerLevel level) {
            LabyrinthData data = LabyrinthData.get(level.getServer()); CompoundTag state = data.state(DrownedTown.ID);
            state.putInt("WitchDefeatedVisit", visit); data.setState(DrownedTown.ID, state);
        }
        super.die(source);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); if (shoreBase != null) tag.putLong("ShoreBase", shoreBase.asLong()); tag.putInt("TownVisit", visit);
        if(memoryOwner!=null&&memoryBase!=null){tag.putUUID("MemoryOwner",memoryOwner);tag.putLong("MemoryBase",memoryBase.asLong());tag.putInt("MemoryPhase",memoryPhase());}
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); shoreBase = tag.contains("ShoreBase") ? BlockPos.of(tag.getLong("ShoreBase")) : null;
        visit = tag.getInt("TownVisit"); cooldown = 30; cancelStrike(); route.clear();
        if(tag.hasUUID("MemoryOwner")){recollection(tag.getUUID("MemoryOwner"),BlockPos.of(tag.getLong("MemoryBase")));memoryPhase(tag.getInt("MemoryPhase"));setNoGravity(memoryPhase()==1||memoryPhase()==3);}
    }
}
