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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;

/** Stacey Graves: a physical, concealed flank, a short rush, then a withdrawal to cover. */
public final class LakeWitchEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> STRIKING = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MEMORY_PHASE = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNT_PHASE = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.INT);
    public static final int STALK=0, LUNGE=1, WITHDRAW=2;
    @Nullable private UUID memoryOwner;
    @Nullable private BlockPos memoryBase;
    @Nullable private BlockPos shoreBase;
    private final Deque<Vec3> route = new ArrayDeque<>();
    @Nullable private BlockPos routeGoal;
    private int windup, cooldown, visit, lungeTicks, settleTicks;
    private double closestRush=Double.MAX_VALUE;
    private int strikeAttempts;
    private boolean lastStrikeAccepted;
    public String huntDiagnostic(){return "closestRush="+closestRush+", attempts="+strikeAttempts+", accepted="+lastStrikeAccepted+", route="+route.size()+", node="+routeGoal;}

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
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(STRIKING, false); builder.define(MEMORY_PHASE, -1);builder.define(HUNT_PHASE,STALK); }
    public boolean memory() { return entityData.get(MEMORY_PHASE) >= 0; }
    public int memoryPhase() { return entityData.get(MEMORY_PHASE); }
    public void memoryPhase(int phase) { entityData.set(MEMORY_PHASE, phase);refreshDimensions(); }
    public int huntPhase(){return entityData.get(HUNT_PHASE);}
    @Override public EntityDimensions getDefaultDimensions(Pose pose){return memory()?EntityDimensions.scalable(.6F,1.8F).withEyeHeight(1.6F):super.getDefaultDimensions(pose);}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(MEMORY_PHASE.equals(key))refreshDimensions();}
    public @Nullable UUID memoryOwner() { return memoryOwner; }
    public @Nullable BlockPos memoryBase() { return memoryBase; }
    public void recollection(UUID owner, BlockPos base) {
        // No goals or hunt logic run in memory mode; leave native travel enabled for the throw's gravity.
        memoryOwner=owner;memoryBase=base.immutable();shoreBase=null;memoryPhase(0);setNoAi(false);
    }
    @Override protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if(memory()) {
            if(player instanceof ServerPlayer serverPlayer && hand==net.minecraft.world.InteractionHand.MAIN_HAND) Shallows.lift(serverPlayer,this);
            return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide());
        }
        return super.mobInteract(player,hand);
    }
    @Override public boolean hurt(DamageSource source,float amount) {
        if (memory() || !super.hurt(source, amount)) return false;
        // A wound breaks the strike: she recoils and withdraws before coming again.
        if (!level().isClientSide()) {
            cancelStrike();
            cooldown = Math.max(cooldown, 40);
            entityData.set(HUNT_PHASE, WITHDRAW);
            route.clear();
        }
        return true;
    }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) { return memory() ? null : DrownedTownRegistry.WITCH_VOICE.get(); }
    @Override public float getVoicePitch() { return 0.8F + getRandom().nextFloat() * 0.2F; }
    public boolean striking() { return entityData.get(STRIKING); }
    public void shore(BlockPos base, int visit) { shoreBase = base.immutable(); this.visit = visit;settleTicks=60;setNoGravity(true); }
    public @Nullable BlockPos shoreBase() { return shoreBase; }
    public void relocateLandscape(BlockPos delta){if(shoreBase!=null)shoreBase=shoreBase.offset(delta);if(memoryBase!=null)memoryBase=memoryBase.offset(delta);route.clear();routeGoal=null;}
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean canBeLeashed() { return false; }

    /** Water and grass are rejected at every footprint corner, including knockback and inertia. */
    public static boolean safeGround(Level level, BlockPos feet) {
        return level.getFluidState(feet).is(FluidTags.WATER)
                || level.getBlockState(feet.below()).is(Blocks.GRASS_BLOCK);
    }
    public static boolean walkable(Level level, BlockPos base, BlockPos feet) {
        int x = feet.getX() - base.getX(), z = feet.getZ() - base.getZ();
        return feet.getY() == base.getY() && x >= -28 && x <= 28 && z >= -63 && z <= -1
                && !safeGround(level, feet)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && (level.getFluidState(feet.below()).is(FluidTags.WATER)||level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP));
    }
    /** Native travel must not apply swimming drag or gravity to the surface hunter. Memory keeps normal physics. */
    @Override public void travel(Vec3 input){if(shoreBase!=null&&!memory()){setDeltaMovement(Vec3.ZERO);return;}super.travel(input);}
    public static double surfaceHeight(Level level,BlockPos base,BlockPos node){
        var fluid=level.getFluidState(node.below());
        return fluid.is(FluidTags.WATER)?node.getY()-1+fluid.getHeight(level,node.below())+.004:base.getY();
    }
    public static double supportHeight(Level level,BlockPos base,double x,double z,double width){
        double y=-Double.MAX_VALUE,half=width*.5+.00001;
        for(int sx:new int[]{-1,1})for(int sz:new int[]{-1,1})y=Math.max(y,surfaceHeight(level,base,BlockPos.containing(x+sx*half,base.getY(),z+sz*half)));
        return y;
    }
    public static boolean canAttack(LakeWitchEntity witch, ServerPlayer player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator() && witch.level() == player.level()
                && witch.shoreBase != null && DrownedTown.contains(witch.shoreBase, player.position())
                && !safeGround(player.level(), player.blockPosition()) && !player.isInWaterOrBubble();
    }
    @Override public void move(MoverType type, Vec3 movement) {
        if(shoreBase!=null&&!memory()){
            BlockPos node=BlockPos.containing(getX()+movement.x,shoreBase.getY(),getZ()+movement.z);
            movement=new Vec3(movement.x,net.minecraft.util.Mth.clamp(supportHeight(level(),shoreBase,getX()+movement.x,getZ()+movement.z,getBbWidth())-getY(),-.35,.35),movement.z);
            setDeltaMovement(Vec3.ZERO);
        }
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
        // Native collision resolves Y before X/Z. Settle again after leaving a dry bank,
        // otherwise the first full water footprint remains visibly above the surface for a tick.
        if(shoreBase!=null&&!memory())super.move(type,new Vec3(0,net.minecraft.util.Mth.clamp(supportHeight(level(),shoreBase,getX(),getZ(),getBbWidth())-getY(),-.35,.35),0));
    }

    /** Four-neighbor physical surface paths detour around living grass, props and trees. */
    public static List<BlockPos> shoreRoute(Level level, BlockPos base, BlockPos start, BlockPos goal) {
        if (!walkable(level, base, start) || !walkable(level, base, goal)) return List.of();
        Map<BlockPos, BlockPos> previous = new HashMap<>(); ArrayDeque<BlockPos> open = new ArrayDeque<>();
        previous.put(start, start); open.add(start);
        while (!open.isEmpty() && previous.size() <= 4000) {
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
    public static boolean inView(ServerPlayer player,Vec3 point){
        Vec3 direction=point.subtract(player.getEyePosition());if(direction.lengthSqr()<.01)return true;
        return player.getViewVector(1).dot(direction.normalize())>.35
                &&player.level().clip(new ClipContext(player.getEyePosition(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getType()==HitResult.Type.MISS;
    }
    public static double behindScore(ServerPlayer target,Vec3 point){
        Vec3 toward=point.subtract(target.position()).multiply(1,0,1).normalize();
        return -target.getViewVector(1).multiply(1,0,1).normalize().dot(toward);
    }
    /** One reachable search favors the back of the player, occlusion and genuinely unlit ground. */
    public static BlockPos ambushGoal(Level level,BlockPos base,BlockPos start,ServerPlayer target,boolean withdraw){
        var open=new ArrayDeque<BlockPos>();var seen=new HashSet<BlockPos>();open.add(start);seen.add(start);
        BlockPos best=null;double score=-Double.MAX_VALUE;
        while(!open.isEmpty()&&seen.size()<4000){
            BlockPos at=open.removeFirst();Vec3 point=Vec3.atBottomCenterOf(at).add(0,.6,0);double distance=point.distanceTo(target.position());
            if(distance>3.5&&distance<17&&at.getZ()<base.getZ()-11){
                boolean visible=inView(target,point);double value=behindScore(target,point)*12+(visible?-32:18)
                        -Math.abs(distance-(withdraw?12:6))*2-level.getBrightness(LightLayer.BLOCK,at)*1.5
                        -Math.sqrt(at.distSqr(start))*.22;
                if(value>score){score=value;best=at;}
            }
            for(Direction side:Direction.Plane.HORIZONTAL){BlockPos next=at.relative(side);if(seen.add(next)&&walkable(level,base,next))open.add(next);}
        }
        return best;
    }
    private void routeTo(BlockPos goal){
        route.clear();routeGoal=goal;
        for(BlockPos node:shoreRoute(level(),shoreBase,BlockPos.containing(getX(),shoreBase.getY(),getZ()),goal))route.add(Vec3.atBottomCenterOf(node));
    }
    private void follow(double speed){
        while(!route.isEmpty()){
            Vec3 toward=route.peek().subtract(position()).multiply(1,0,1);
            if(toward.lengthSqr()<.025){route.removeFirst();continue;}
            double oldX=getX(),oldZ=getZ();move(MoverType.SELF,toward.normalize().scale(Math.min(speed,toward.length())));
            setYRot((float)(Math.atan2(getZ()-oldZ,getX()-oldX)*180/Math.PI)-90);yBodyRot=getYRot();
            walkAnimation.update((float)Math.sqrt((getX()-oldX)*(getX()-oldX)+(getZ()-oldZ)*(getZ()-oldZ))*4,.4F);break;
        }
    }
    private void conceal(ServerLevel level){
        cancelStrike();entityData.set(HUNT_PHASE,WITHDRAW);
        if(route.isEmpty()||tickCount%40==0){
            var observer=level.players().stream().filter(p->p.isAlive()&&!p.isSpectator()&&DrownedTown.contains(shoreBase,p.position())).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            BlockPos start=BlockPos.containing(getX(),shoreBase.getY(),getZ());
            BlockPos cover=observer==null?null:ambushGoal(level,shoreBase,start,observer,true);
            if(cover==null)for(BlockPos at:List.of(shoreBase.offset(-24,0,-40),shoreBase.offset(25,0,-57),shoreBase.offset(-13,0,-51)))if(walkable(level,shoreBase,at)){cover=at;break;}
            if(cover!=null&&!cover.equals(start))routeTo(cover);
        }
        follow(.26);
    }
    @Override public void tick() {
        super.tick();
        if(memory()) { setAirSupply(300); Shallows.tickActor(this); return; }
        if (!(level() instanceof ServerLevel level) || shoreBase == null || !isAlive()) return;
        setAirSupply(300);move(MoverType.SELF,Vec3.ZERO);
        if(settleTicks>0){settleTicks--;if(getZ()>shoreBase.getZ()-14&&Math.abs(getX()-shoreBase.getX())<6)conceal(level);return;}
        if (cooldown > 0) cooldown--;
        ServerPlayer target = level.players().stream().filter(p -> canAttack(this, p))
                .filter(p->p.getZ()<shoreBase.getZ()-12||Math.abs(p.getX()-shoreBase.getX())>5)
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (target == null || distanceToSqr(target) > 1600) { conceal(level); return; }
        getLookControl().setLookAt(target, 30, 30);
        if (distanceToSqr(target) < 784 && getSensing().hasLineOfSight(target))
            IndianLakeProgress.hunted(LabyrinthData.get(level.getServer()), target.getUUID());
        if(windup>0){windup--;return;}
        BlockPos goal = BlockPos.containing(target.getX(), shoreBase.getY(), target.getZ());
        if(!walkable(level,shoreBase,goal)){cancelStrike();return;}
        if(huntPhase()==LUNGE){
            if(--lungeTicks<=0){cancelStrike();cooldown=65;entityData.set(HUNT_PHASE,WITHDRAW);route.clear();return;}
            if(routeGoal==null||!goal.equals(routeGoal)||route.isEmpty())routeTo(goal);
            follow(.62);
            closestRush=Math.min(closestRush,distanceToSqr(target));
            if(canAttack(this,target)&&distanceToSqr(target)<3.8&&getSensing().hasLineOfSight(target)){
                swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                strikeAttempts++;lastStrikeAccepted=target.hurt(damageSources().mobAttack(this),6);Vec3 away=target.position().subtract(position()).multiply(1,0,1).normalize();
                target.setDeltaMovement(away.scale(.3).add(0,.12,0));target.hurtMarked=true;
                cancelStrike();cooldown=75;entityData.set(HUNT_PHASE,WITHDRAW);route.clear();
            }return;
        }
        boolean watched=inView(target,position().add(0,.6,0));
        if(cooldown==0&&distanceToSqr(target)<72&&getSensing().hasLineOfSight(target)
                &&(!watched||behindScore(target,position())>.25||distanceToSqr(target)<5)){
            entityData.set(HUNT_PHASE,LUNGE);entityData.set(STRIKING,true);windup=4;lungeTicks=24;routeTo(goal);
            level.playSound(null,blockPosition(),DrownedTownRegistry.WITCH_VOICE.get(),SoundSource.HOSTILE,.55F,1.2F);return;
        }
        boolean withdraw=cooldown>0;entityData.set(HUNT_PHASE,withdraw?WITHDRAW:STALK);
        if(tickCount%24==0||routeGoal==null||route.isEmpty()){
            BlockPos cover=ambushGoal(level,shoreBase,BlockPos.containing(getX(),shoreBase.getY(),getZ()),target,withdraw);
            if(cover!=null)routeTo(cover);
            else if(!watched||distanceToSqr(target)<100)routeTo(goal);
        }
        follow(withdraw?.34:.24);
    }
    private void cancelStrike() { windup = 0;lungeTicks=0;entityData.set(STRIKING, false);entityData.set(HUNT_PHASE,STALK); }
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
        if(shoreBase!=null){setNoGravity(true);settleTicks=40;}
        if(tag.hasUUID("MemoryOwner")){recollection(tag.getUUID("MemoryOwner"),BlockPos.of(tag.getLong("MemoryBase")));memoryPhase(tag.getInt("MemoryPhase"));setNoGravity(memoryPhase()==1||memoryPhase()==3);}
    }
}
