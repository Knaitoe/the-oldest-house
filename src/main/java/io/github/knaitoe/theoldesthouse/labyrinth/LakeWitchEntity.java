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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;

/** Stacey Graves: a physical, concealed flank, a short rush, then a withdrawal to cover. */
public final class LakeWitchEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> STRIKING = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MEMORY_PHASE = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNT_PHASE = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BITING = SynchedEntityData.defineId(LakeWitchEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int STALK=0, LUNGE=1, WITHDRAW=2;
    public static final double HUNT_SPEED_FACTOR=2;
    public static final int COVER_SEARCH_LIMIT=80, DOOR_BREAK_TICKS=60;
    @Nullable private UUID memoryOwner;
    @Nullable private BlockPos memoryBase;
    @Nullable private BlockPos shoreBase;
    @Nullable private BlockPos literaryBase;
    @Nullable private LabyrinthPlace literaryPlace;
    private final Deque<Vec3> route = new ArrayDeque<>();
    @Nullable private BlockPos routeGoal;
    private int windup, cooldown, visit, lungeTicks, settleTicks;
    private double closestRush=Double.MAX_VALUE;
    private int strikeAttempts;
    private boolean lastStrikeAccepted;
    private int failedCoverTicks, doorBreakTicks, lastDoorTick=-1;
    private int passingSwipeTicks;
    private boolean exposedHunt, coverPause, emerging;
    @Nullable private BlockPos breakingDoor;
    public String huntDiagnostic(){return "closestRush="+closestRush+", attempts="+strikeAttempts+", accepted="+lastStrikeAccepted+", route="+route.size()+", node="+routeGoal+", failedCover="+failedCoverTicks+", exposed="+exposedHunt+", doorTicks="+doorBreakTicks+", hidden="+coverPause+", emerging="+emerging+", delay="+cooldown;}
    public int failedCoverTicks(){return failedCoverTicks;}
    public boolean exposedHunt(){return exposedHunt;}
    public int doorBreakTicks(){return doorBreakTicks;}
    public boolean hiding(){return coverPause;}
    public boolean emerging(){return emerging;}
    public int passingSwipeCooldown(){return passingSwipeTicks;}

    public LakeWitchEntity(EntityType<? extends LakeWitchEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setPathfindingMalus(PathType.WATER, -1);
        setPathfindingMalus(PathType.WATER_BORDER, -1);
        setPathfindingMalus(PathType.BREACH, -1);
    }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 36).add(Attributes.MOVEMENT_SPEED, .5)
                .add(Attributes.ATTACK_DAMAGE, 6).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.KNOCKBACK_RESISTANCE, .65);
    }
    @Override protected void registerGoals() {}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(STRIKING, false); builder.define(MEMORY_PHASE, -1);builder.define(HUNT_PHASE,STALK);builder.define(BITING,false); }
    public boolean memory() { return entityData.get(MEMORY_PHASE) >= 0; }
    public int memoryPhase() { return entityData.get(MEMORY_PHASE); }
    public void memoryPhase(int phase) { entityData.set(MEMORY_PHASE, phase);refreshDimensions(); }
    public int huntPhase(){return entityData.get(HUNT_PHASE);}
    public boolean biting(){return !memory()&&entityData.get(BITING);}
    public void literaryHunt(BlockPos base,LabyrinthPlace place){
        literaryBase=base.immutable();literaryPlace=place;setNoGravity(true);setInvulnerable(false);
    }
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
            beginWithdrawal(source.getEntity() instanceof ServerPlayer p?p:null);
            failedCoverTicks=0;exposedHunt=false;clearDoorBreak();
        }
        return true;
    }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) { return memory() ? null : DrownedTownRegistry.WITCH_VOICE.get(); }
    @Override public float getVoicePitch() { return 0.8F + getRandom().nextFloat() * 0.2F; }
    public boolean striking() { return entityData.get(STRIKING); }
    public void shore(BlockPos base, int visit) { shoreBase = base.immutable(); this.visit = visit;settleTicks=60;setNoGravity(true); }
    public @Nullable BlockPos shoreBase() { return shoreBase; }
    public void relocateLandscape(BlockPos delta){if(shoreBase!=null)shoreBase=shoreBase.offset(delta);if(memoryBase!=null)memoryBase=memoryBase.offset(delta);if(literaryBase!=null)literaryBase=literaryBase.offset(delta);route.clear();routeGoal=null;}
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean canBeLeashed() { return false; }

    /** Water and grass are rejected at every footprint corner, including knockback and inertia. */
    public static boolean safeGround(Level level, BlockPos feet) {
        return level.getFluidState(feet).is(FluidTags.WATER)
                || level.getBlockState(feet.below()).is(Blocks.GRASS_BLOCK);
    }
    /** Inside the town's ground, one cell in from its edge (0.4.67: Proofrock's whole plot, not the old 59-block box). */
    static boolean onTownGround(BlockPos base,BlockPos feet){
        var r=LabyrinthPlace.DROWNED_TOWN.room();int x=feet.getX()-base.getX(),z=feet.getZ()-base.getZ();
        return feet.getY()==base.getY()&&x>r.minX()&&x<r.maxX()&&z>r.minZ()&&z<=-1;
    }
    public static boolean walkable(Level level, BlockPos base, BlockPos feet) {
        var point=crawlPoint(level,feet,.78,.94);
        return onTownGround(base,feet)
                && !safeGround(level, feet)
                && level.noCollision(crawlBox(point,.78,.94))
                && (level.getFluidState(feet.below()).is(FluidTags.WATER)||level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP));
    }
    private static AABB crawlBox(Vec3 point,double width,double height){
        double half=width*.5+.015;return new AABB(point.x-half,point.y,point.z-half,point.x+half,point.y+height,point.z+half);
    }
    /** The open door leaf occupies three pixels of the cell; aim at the remaining opening. */
    private static Vec3 crawlPoint(Level l,BlockPos node,double width,double height){
        var center=Vec3.atBottomCenterOf(node);var state=l.getBlockState(node);
        if(state.getBlock() instanceof DoorBlock&&state.getValue(DoorBlock.OPEN)){
            for(var point:List.of(center,center.add(-.09375,0,0),center.add(.09375,0,0),center.add(0,0,-.09375),center.add(0,0,.09375)))
                if(l.noCollision(crawlBox(point,width,height)))return point;
        }return center;
    }
    /** Doors on the hunt's ground are traversable search nodes, but never traversed until physically broken. */
    private boolean huntWalkable(BlockPos feet){
        if(shoreBase!=null&&walkable(level(),shoreBase,feet))return true;
        if(shoreBase==null||!breakableDoor(feet))return false;
        return onTownGround(shoreBase,feet)&&!safeGround(level(),feet)
                &&level().getBlockState(feet.below()).isFaceSturdy(level(),feet.below(),Direction.UP);
    }
    public boolean breakableDoor(BlockPos at){
        if(memory()||!(level() instanceof ServerLevel l)||!l.hasChunkAt(at)||!l.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_MOBGRIEFING))return false;
        var s=l.getBlockState(at);if(!(s.getBlock() instanceof DoorBlock)||!s.is(BlockTags.WOODEN_DOORS)||s.getValue(DoorBlock.OPEN))return false;
        var lower=s.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?at.below():at;
        var upper=l.getBlockState(lower.above());if(!upper.is(s.getBlock())||upper.getValue(DoorBlock.HALF)!=DoubleBlockHalf.UPPER)return false;
        if(LabyrinthData.get(l.getServer()).doorAt(l.dimension(),lower)!=null)return false;
        if(shoreBase!=null){
            if(lower.equals(shoreBase.offset(DrownedTown.CHURCH_DOOR)))return false;
            return onTownGround(shoreBase,lower)&&lower.getZ()-shoreBase.getZ()<=-12;
        }
        if(literaryBase!=null&&literaryPlace!=null){var r=literaryPlace.room();int x=lower.getX()-literaryBase.getX(),z=lower.getZ()-literaryBase.getZ();
            return lower.getY()==literaryBase.getY()&&x>r.minX()+2&&x<r.maxX()-2&&z>r.minZ()+2&&z<-10;
        }
        return false;
    }
    private void clearDoorBreak(){
        if(breakingDoor!=null&&level() instanceof ServerLevel l)l.destroyBlockProgress(getId(),breakingDoor,-1);
        breakingDoor=null;doorBreakTicks=0;lastDoorTick=-1;
    }
    /** One native actor advances one cracking clock. Cancellation, opening or leaving cancels the attempt. */
    public boolean workDoor(BlockPos at){
        if(!breakableDoor(at)){clearDoorBreak();return false;}
        var s=level().getBlockState(at);var lower=s.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?at.below():at;
        if(distanceToSqr(lower.getCenter())>5){clearDoorBreak();return false;}
        if(!lower.equals(breakingDoor)){clearDoorBreak();breakingDoor=lower.immutable();}
        if(lastDoorTick==tickCount)return true;lastDoorTick=tickCount;
        var l=(ServerLevel)level();doorBreakTicks++;
        if(doorBreakTicks%20==1){swing(net.minecraft.world.InteractionHand.MAIN_HAND);l.levelEvent(1019,lower,0);}
        l.destroyBlockProgress(getId(),lower,Math.min(9,doorBreakTicks*10/DOOR_BREAK_TICKS));
        if(doorBreakTicks<DOOR_BREAK_TICKS)return true;
        var before=l.getBlockState(lower);
        if(net.neoforged.neoforge.event.EventHooks.onEntityDestroyBlock(this,lower,before)&&l.destroyBlock(lower,true,this))l.levelEvent(1021,lower,0);
        clearDoorBreak();routeGoal=null;return true;
    }
    private boolean doorOnStep(BlockPos node){
        if(breakableDoor(node))return workDoor(node);
        if(breakingDoor!=null){
            // A replan may prepend the current cell's center. That centering node
            // must not erase work on the same closed door still ahead in this route.
            if(route.stream().anyMatch(point->BlockPos.containing(point).equals(breakingDoor)))return workDoor(breakingDoor);
            clearDoorBreak();
        }return false;
    }
    /** No hiding place for four occupied seconds commits this body to pursuit, including under a gaze. */
    private void coverAttempt(boolean reachableCover){
        if(reachableCover){failedCoverTicks=0;return;}
        if(++failedCoverTicks>=COVER_SEARCH_LIMIT){
            failedCoverTicks=COVER_SEARCH_LIMIT;exposedHunt=true;coverPause=false;cooldown=Math.min(cooldown,20);routeGoal=null;
        }
    }
    private boolean withdrawing(){return cooldown>0&&!exposedHunt&&!emerging;}
    private boolean coverReachable;
    private void beginWithdrawal(@Nullable ServerPlayer target){
        cancelStrike();cooldown=target!=null&&target.getHealth()<=6?20:20+getRandom().nextInt(41);
        entityData.set(HUNT_PHASE,WITHDRAW);coverPause=false;emerging=false;route.clear();routeGoal=null;coverReachable=false;failedCoverTicks=0;
    }
    /** The short hiding interval starts on arrival, rather than expiring during the retreat. */
    private void huntClocks(boolean critical){
        if(passingSwipeTicks>0)passingSwipeTicks--;
        if(critical&&cooldown>20)cooldown=20;
        if(cooldown>0&&(!withdrawing()||coverPause||critical))cooldown--;
        if(coverPause&&cooldown==0){coverPause=false;emerging=true;route.clear();routeGoal=null;}
    }
    private void coverProgress(boolean watched,Vec3 before){
        boolean reached=coverReachable&&routeGoal!=null&&position().subtract(crawlPoint(level(),routeGoal,getBbWidth(),getBbHeight())).multiply(1,0,1).lengthSqr()<.0064;
        if(reached&&!watched){coverPause=true;failedCoverTicks=0;route.clear();clearDoorBreak();return;}
        coverAttempt(coverReachable&&position().distanceToSqr(before)>.0004);
    }
    private boolean literaryTarget(ServerPlayer p){
        return p.isAlive()&&!p.isSpectator()&&!p.isCreative()&&LiteraryVignettes.inside(p,literaryPlace)
                &&!(p.isUnderWater()&&p.getY()+p.getBbHeight()<literaryBase.getY())
                &&(literaryPlace!=LabyrinthPlace.COSTUME_NIGHT||!level().getBlockState(p.blockPosition().below()).is(Blocks.GRASS_BLOCK));
    }
    /** A native arm swing can catch one reader alongside the swept retreat, without stopping it. */
    private void passingSwipe(Vec3 before){
        if(huntPhase()!=WITHDRAW||coverPause||passingSwipeTicks>0||settleTicks>0||!(level() instanceof ServerLevel l))return;
        Vec3 step=position().subtract(before);double length=step.lengthSqr();if(length<.000001)return;
        var victim=l.players().stream().filter(p->shoreBase!=null?canAttack(this,p):literaryBase!=null&&literaryTarget(p))
                .filter(p->{double t=net.minecraft.util.Mth.clamp(p.position().subtract(before).dot(step)/length,0,1);
                    return p.position().distanceToSqr(before.add(step.scale(t)))<1.96&&hasLineOfSight(p);})
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if(victim==null)return;
        entityData.set(BITING,false);swing(net.minecraft.world.InteractionHand.MAIN_HAND);strikeAttempts++;lastStrikeAccepted=victim.hurt(damageSources().mobAttack(this),shoreBase!=null?6:4);
        passingSwipeTicks=20;
    }
    /** Refuge/support checks use the actual footprint; native movement handles walls and low roofs. */
    private boolean supportedFootprint(BlockPos base,double x,double z,boolean literary){
        double half=getBbWidth()*.5+.00001;
        for(int sx:new int[]{-1,1})for(int sz:new int[]{-1,1}){
            var feet=BlockPos.containing(x+sx*half,base.getY(),z+sz*half);
            if(!level().hasChunkAt(feet))return false;
            if(literary){var r=literaryPlace.room();int dx=feet.getX()-base.getX(),dz=feet.getZ()-base.getZ();
                if(dx<=r.minX()+2||dx>=r.maxX()-2||dz<=r.minZ()+2||dz>=-10)return false;
                if(literaryPlace==LabyrinthPlace.COSTUME_NIGHT&&level().getBlockState(feet.below()).is(Blocks.GRASS_BLOCK))return false;
            }else if(!onTownGround(base,feet)||safeGround(level(),feet))return false;
            if(!level().getFluidState(feet.below()).is(FluidTags.WATER)
                    &&level().getBlockState(feet.below()).getCollisionShape(level(),feet.below()).isEmpty())return false;
        }return true;
    }
    /** Native travel must not apply swimming drag or gravity to the surface hunter. Memory keeps normal physics. */
    @Override public void travel(Vec3 input){if((shoreBase!=null||literaryBase!=null)&&!memory()){setDeltaMovement(Vec3.ZERO);return;}super.travel(input);}
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
            movement=new Vec3(movement.x,net.minecraft.util.Mth.clamp(supportHeight(level(),shoreBase,getX()+movement.x,getZ()+movement.z,getBbWidth())-getY(),-.35,.35),movement.z);
            setDeltaMovement(Vec3.ZERO);
        }
        if (!level().isClientSide() && !memory() && (shoreBase!=null||literaryBase!=null) && (Math.abs(movement.x) > .00001 || Math.abs(movement.z) > .00001)) {
            var base=shoreBase!=null?shoreBase:literaryBase;
            boolean allowed = true;
            // Sample a swept route, so fast external forces cannot jump across a grass patch.
            int steps = Math.max(1, (int)Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z)) * 4));
            for (int step = 1; step <= steps && allowed; step++)
                allowed=supportedFootprint(base,getX()+movement.x*step/steps,getZ()+movement.z*step/steps,shoreBase==null);
            if (!allowed) { movement = new Vec3(0, movement.y, 0); route.clear(); setDeltaMovement(0, getDeltaMovement().y, 0); }
        }
        super.move(type, movement);
        // Native collision resolves Y before X/Z. Settle again after leaving a dry bank,
        // otherwise the first full water footprint remains visibly above the surface for a tick.
        if(shoreBase!=null&&!memory())super.move(type,new Vec3(0,net.minecraft.util.Mth.clamp(supportHeight(level(),shoreBase,getX(),getZ(),getBbWidth())-getY(),-.35,.35),0));
    }

    /** Four-neighbor physical surface paths detour around living grass, props and trees. */
    public static List<BlockPos> shoreRoute(Level level, BlockPos base, BlockPos start, BlockPos goal) {
        return aStar(start,goal,at->walkable(level,base,at),12000);
    }
    /**
     * Shortest four-neighbour path by A* (0.4.67). A town ten times the old shore needs directed search: breadth-first
     * search ran out of nodes before reaching a reader across two streets.
     */
    static List<BlockPos> aStar(BlockPos start,BlockPos goal,java.util.function.Predicate<BlockPos> open,int budget){
        if(!open.test(start)||!open.test(goal))return List.of();
        Map<BlockPos,BlockPos> previous=new HashMap<>();Map<BlockPos,Integer> cost=new HashMap<>();
        PriorityQueue<long[]> frontier=new PriorityQueue<>(Comparator.<long[]>comparingLong(e->e[0]).thenComparingLong(e->e[2]));
        previous.put(start,start);cost.put(start,0);frontier.add(new long[]{start.distManhattan(goal),start.asLong(),0});long order=0;
        while(!frontier.isEmpty()&&previous.size()<=budget){
            var entry=frontier.poll();BlockPos at=BlockPos.of(entry[1]);int g=cost.get(at);
            if(entry[0]>g+at.distManhattan(goal))continue;
            if(at.equals(goal)){LinkedList<BlockPos> result=new LinkedList<>();while(!at.equals(start)){result.addFirst(at);at=previous.get(at);}return result;}
            for(Direction side:Direction.Plane.HORIZONTAL){BlockPos next=at.relative(side);Integer known=cost.get(next);
                if(known!=null&&known<=g+1||!open.test(next))continue;cost.put(next,g+1);previous.put(next,at);frontier.add(new long[]{g+1+next.distManhattan(goal),next.asLong(),++order});}
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
        var watchers=level instanceof ServerLevel l?l.players().stream().filter(p->p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&DrownedTown.contains(base,p.position())).toList():List.<ServerPlayer>of();
        BlockPos best=null;double score=-Double.MAX_VALUE;
        while(!open.isEmpty()&&seen.size()<6000){
            BlockPos at=open.removeFirst();Vec3 point=Vec3.atBottomCenterOf(at).add(0,.6,0);double distance=point.distanceTo(target.position());
            if(distance>3.5&&distance<17&&at.getZ()<base.getZ()-11){
                boolean visible=inView(target,point);double value=behindScore(target,point)*12+(visible?-32:18)
                        -Math.abs(distance-(withdraw?12:6))*2-level.getBrightness(LightLayer.BLOCK,at)*1.5
                        -Math.sqrt(at.distSqr(start))*.22;
                if((!withdraw||!visible&&watchers.stream().noneMatch(p->inView(p,point)))&&value>score){score=value;best=at;}
            }
            for(Direction side:Direction.Plane.HORIZONTAL){BlockPos next=at.relative(side);if(seen.add(next)&&walkable(level,base,next))open.add(next);}
        }
        return best;
    }
    private void routeTo(BlockPos goal){
        route.clear();routeGoal=goal;
        var start=BlockPos.containing(getX(),shoreBase.getY(),getZ());
        addRoute(start,goal,false);
    }
    private void addRoute(BlockPos start,BlockPos goal,boolean literary){
        var nodes=physicalRoute(start,goal,literary);
        if(nodes.isEmpty()&&!start.equals(goal))return;
        // Center the current cell before entering the next one. At a one-block corner a
        // skipped .2-block waypoint is enough to pin the .78-block body against the wall.
        route.add(crawlPoint(level(),start,getBbWidth(),getBbHeight()));
        for(var node:nodes)route.add(crawlPoint(level(),node,getBbWidth(),getBbHeight()));
    }
    private List<BlockPos> physicalRoute(BlockPos start,BlockPos goal,boolean literary){
        java.util.function.Predicate<BlockPos> open=at->level().hasChunkAt(at)&&(literary?literaryWalkable(at):huntWalkable(at));
        return aStar(start,goal,open,literary?7000:12000);
    }
    private void follow(double speed){
        double remaining=speed*HUNT_SPEED_FACTOR;int nodes=0;
        while(!route.isEmpty()&&remaining>.001&&nodes++<5){
            Vec3 toward=route.peek().subtract(position()).multiply(1,0,1);
            if(toward.lengthSqr()<.00001){route.removeFirst();continue;}
            if(doorOnStep(BlockPos.containing(route.peek())))return;
            var before=position();double oldX=getX(),oldZ=getZ();move(MoverType.SELF,toward.normalize().scale(Math.min(remaining,toward.length())));
            setYRot((float)(Math.atan2(getZ()-oldZ,getX()-oldX)*180/Math.PI)-90);yBodyRot=getYRot();
            double travelled=Math.sqrt((getX()-oldX)*(getX()-oldX)+(getZ()-oldZ)*(getZ()-oldZ));
            passingSwipe(before);walkAnimation.update((float)travelled*4,.4F);remaining-=travelled;if(travelled<.001)return;
        }
    }
    private void conceal(ServerLevel level){
        cancelStrike();entityData.set(HUNT_PHASE,WITHDRAW);
        if(route.isEmpty()||tickCount%40==0){
            var observer=level.players().stream().filter(p->p.isAlive()&&!p.isSpectator()&&DrownedTown.contains(shoreBase,p.position())).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            BlockPos start=BlockPos.containing(getX(),shoreBase.getY(),getZ());
            BlockPos cover=observer==null?null:ambushGoal(level,shoreBase,start,observer,true);
            if(cover==null)for(BlockPos lurk:ProofrockTown.LURKS){var at=shoreBase.offset(lurk);if(walkable(level,shoreBase,at)){cover=at;break;}}
            if(cover!=null&&!cover.equals(start))routeTo(cover);
        }
        follow(.26);
    }
    @Override public void tick() {
        super.tick();
        if(memory()) { setAirSupply(300); Shallows.tickActor(this); return; }
        if(literaryBase!=null&&literaryPlace!=null){tickLiterary();return;}
        if (!(level() instanceof ServerLevel level) || shoreBase == null || !isAlive()) return;
        setAirSupply(300);move(MoverType.SELF,Vec3.ZERO);
        if(settleTicks>0){settleTicks--;if(getZ()>shoreBase.getZ()-14&&Math.abs(getX()-shoreBase.getX())<6)conceal(level);return;}
        ServerPlayer target = level.players().stream().filter(p -> canAttack(this, p))
                .filter(p->p.getZ()<shoreBase.getZ()-12||Math.abs(p.getX()-shoreBase.getX())>5)
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (target == null || distanceToSqr(target) > 1600) { cancelStrike();route.clear();routeGoal=null;clearDoorBreak();coverReachable=false;return; }
        boolean critical=target.getHealth()<=6;huntClocks(critical);
        getLookControl().setLookAt(target, 30, 30);
        if (distanceToSqr(target) < 784 && hasLineOfSight(target))
            IndianLakeProgress.hunted(LabyrinthData.get(level.getServer()), target.getUUID());
        boolean observed=level.players().stream().filter(p->canAttack(this,p)).anyMatch(p->inView(p,getEyePosition()));
        if(!critical&&!exposedHunt&&!emerging&&observed&&(huntPhase()==LUNGE||cooldown==0&&distanceToSqr(target)<72))beginWithdrawal(target);
        if(coverPause&&withdrawing()&&!critical){entityData.set(HUNT_PHASE,WITHDRAW);return;}
        if(windup>0){windup--;return;}
        BlockPos goal = BlockPos.containing(target.getX(), shoreBase.getY(), target.getZ());
        if(!huntWalkable(goal)){cancelStrike();clearDoorBreak();return;}
        if(huntPhase()==LUNGE){
            if(--lungeTicks<=0){beginWithdrawal(target);return;}
            if(routeGoal==null||!goal.equals(routeGoal)||route.isEmpty())routeTo(goal);
            follow(.62);
            closestRush=Math.min(closestRush,distanceToSqr(target));
            if(canAttack(this,target)&&distanceToSqr(target)<3.8&&hasLineOfSight(target)){
                swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                strikeAttempts++;lastStrikeAccepted=target.hurt(damageSources().mobAttack(this),6);Vec3 away=target.position().subtract(position()).multiply(1,0,1).normalize();
                target.setDeltaMovement(away.scale(.3).add(0,.12,0));target.hurtMarked=true;
                beginWithdrawal(target);
            }return;
        }
        boolean watched=inView(target,position().add(0,.6,0));
        if(cooldown==0&&distanceToSqr(target)<72&&hasLineOfSight(target)
                &&(!observed||critical||exposedHunt||emerging)){
            entityData.set(HUNT_PHASE,LUNGE);entityData.set(STRIKING,true);entityData.set(BITING,getRandom().nextBoolean());windup=4;lungeTicks=24;routeTo(goal);
            level.playSound(null,blockPosition(),DrownedTownRegistry.WITCH_VOICE.get(),SoundSource.HOSTILE,.55F,1.2F);return;
        }
        boolean withdraw=withdrawing()&&!critical;entityData.set(HUNT_PHASE,withdraw?WITHDRAW:STALK);
        if(routeGoal==null||route.isEmpty()||!withdraw&&tickCount%24==0){
            BlockPos cover=exposedHunt||emerging?null:ambushGoal(level,shoreBase,BlockPos.containing(getX(),shoreBase.getY(),getZ()),target,withdraw);
            if(cover!=null){routeTo(cover);coverReachable=!route.isEmpty()||blockPosition().equals(cover);}
            else {coverReachable=false;routeTo(goal);}
        }
        var beforeCover=position();
        follow(withdraw?.34:critical?.34:.24);
        if(withdraw)coverProgress(observed,beforeCover);
        else if(!hasLineOfSight(target))coverAttempt(position().distanceToSqr(beforeCover)>.0004);
    }
    private void cancelStrike() { windup = 0;lungeTicks=0;entityData.set(STRIKING, false);entityData.set(HUNT_PHASE,STALK); }
    public int attackCooldown(){return cooldown;}
    private boolean literaryWalkable(BlockPos node){
        var r=literaryPlace.room();int x=node.getX()-literaryBase.getX(),z=node.getZ()-literaryBase.getZ();
        if(x<=r.minX()+2||x>=r.maxX()-2||z<=r.minZ()+2||z>=-10)return false;
        var point=crawlPoint(level(),node,getBbWidth(),getBbHeight());
        if(!supportedFootprint(literaryBase,point.x,point.z,true))return false;
        double y=supportHeight(level(),literaryBase,point.x,point.z,getBbWidth());
        return breakableDoor(node)||level().noCollision(this,crawlBox(new Vec3(point.x,y,point.z),getBbWidth(),getBbHeight()));
    }
    private void literaryRoute(BlockPos goal){
        route.clear();routeGoal=goal;var start=BlockPos.containing(getX(),literaryBase.getY(),getZ());
        if(!literaryWalkable(start))return;
        if(!literaryWalkable(goal)){
            BlockPos nearest=null;double distance=Double.MAX_VALUE;
            for(var candidate:BlockPos.betweenClosed(goal.offset(-1,0,-1),goal.offset(1,0,1)))
                if(literaryWalkable(candidate)&&candidate.distSqr(start)<distance){distance=candidate.distSqr(start);nearest=candidate.immutable();}
            if(nearest==null)return;goal=nearest;
        }
        addRoute(start,goal,true);
    }
    private BlockPos literaryCover(ServerLevel l,ServerPlayer target){
        BlockPos best=null;double score=-Double.MAX_VALUE;
        var start=BlockPos.containing(getX(),literaryBase.getY(),getZ());
        var queue=new ArrayDeque<BlockPos>();var visited=new HashSet<BlockPos>();queue.add(start);visited.add(start);
        var readers=l.players().stream().filter(p->p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&LiteraryVignettes.inside(p,literaryPlace)).toList();
        while(!queue.isEmpty()&&visited.size()<4000){var n=queue.removeFirst();var point=Vec3.atBottomCenterOf(n).add(0,.45,0);double distance=point.distanceTo(target.position());
            if(distance>4&&distance<17&&!breakableDoor(n)&&readers.stream().noneMatch(p->inView(p,point))){
                double value=behindScore(target,point)*12-l.getBrightness(LightLayer.BLOCK,n)-position().distanceTo(point)*.4;
                if(value>score){score=value;best=n;}
            }
            for(var side:Direction.Plane.HORIZONTAL){var next=n.relative(side);if(!visited.contains(next)&&l.hasChunkAt(next)&&literaryWalkable(next)){visited.add(next);queue.add(next);}}
        }return best;
    }
    /** Physical approach and retreat, a one-to-three-second hide, then another committed rush. */
    private void tickLiterary(){
        if(!(level() instanceof ServerLevel level)||!isAlive())return;setAirSupply(300);
        var target=level.players().stream().filter(this::literaryTarget)
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if(target==null||distanceToSqr(target)>3600){cancelStrike();route.clear();routeGoal=null;clearDoorBreak();coverReachable=false;return;}
        boolean critical=target.getHealth()<=6;huntClocks(critical);
        boolean watched=level.players().stream().filter(p->p.isAlive()&&!p.isSpectator()&&!p.isCreative()&&LiteraryVignettes.inside(p,literaryPlace)).anyMatch(p->inView(p,getEyePosition()));
        if(!critical&&!exposedHunt&&!emerging&&watched&&(huntPhase()==LUNGE||cooldown==0&&distanceToSqr(target)<72))beginWithdrawal(target);
        if(coverPause&&withdrawing()&&!critical){entityData.set(HUNT_PHASE,WITHDRAW);return;}
        if(windup>0){windup--;return;}
        var goal=BlockPos.containing(target.getX(),literaryBase.getY(),target.getZ());
        if(huntPhase()==LUNGE){
            if(routeGoal==null||!routeGoal.equals(goal)||tickCount%8==0)literaryRoute(goal);
            literaryFollow(.46);
            if(distanceToSqr(target)<3.8&&hasLineOfSight(target)){
                swing(net.minecraft.world.InteractionHand.MAIN_HAND);strikeAttempts++;lastStrikeAccepted=target.hurt(damageSources().mobAttack(this),4);
                beginWithdrawal(target);
            }else if(--lungeTicks<=0){beginWithdrawal(target);}return;
        }
        if(cooldown==0&&distanceToSqr(target)<72&&hasLineOfSight(target)&&(!watched||critical||exposedHunt||emerging)){
            entityData.set(HUNT_PHASE,LUNGE);entityData.set(STRIKING,true);entityData.set(BITING,getRandom().nextBoolean());windup=critical?2:6;lungeTicks=30;literaryRoute(goal);return;}
        boolean withdraw=withdrawing()&&!critical;entityData.set(HUNT_PHASE,withdraw?WITHDRAW:STALK);
        if(routeGoal==null||route.isEmpty()||!withdraw&&tickCount%20==0){var cover=withdraw?literaryCover(level,target):goal;
            coverReachable=cover!=null;if(cover!=null){literaryRoute(cover);coverReachable=!route.isEmpty()||blockPosition().equals(cover);}else literaryRoute(goal);}
        var beforeCover=position();
        literaryFollow(withdraw?.34:critical?.32:.19);
        if(withdraw)coverProgress(watched,beforeCover);
        else if(!hasLineOfSight(target))coverAttempt(position().distanceToSqr(beforeCover)>.0004);
    }
    private void literaryFollow(double speed){
        double remaining=speed*HUNT_SPEED_FACTOR;int nodes=0;
        while(!route.isEmpty()&&remaining>.001&&nodes++<5){
            var point=route.peekFirst();var delta=point.subtract(position()).multiply(1,0,1);
            if(delta.lengthSqr()<.00001){route.removeFirst();continue;}
            if(doorOnStep(BlockPos.containing(point)))return;
            var step=delta.normalize().scale(Math.min(remaining,delta.length()));var node=BlockPos.containing(getX()+step.x,literaryBase.getY(),getZ()+step.z);
            if(!literaryWalkable(node)){route.clear();return;}
            var before=position();move(MoverType.SELF,step);setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));yBodyRot=getYRot();
            super.move(MoverType.SELF,new Vec3(0,net.minecraft.util.Mth.clamp(supportHeight(level(),literaryBase,getX(),getZ(),getBbWidth())-getY(),-.25,.25),0));
            double travelled=position().subtract(before).multiply(1,0,1).length();passingSwipe(before);walkAnimation.update((float)travelled*4,.4F);remaining-=travelled;if(travelled<.001)return;
        }
    }
    @Override public void die(DamageSource source) {
        clearDoorBreak();
        if (!memory() && literaryBase==null && level() instanceof ServerLevel level) {
            LabyrinthData data = LabyrinthData.get(level.getServer()); CompoundTag state = data.state(DrownedTown.ID);
            state.putInt("WitchDefeatedVisit", visit); data.setState(DrownedTown.ID, state);
        }
        super.die(source);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); if (shoreBase != null) tag.putLong("ShoreBase", shoreBase.asLong()); tag.putInt("TownVisit", visit);tag.putInt("HuntCooldown",cooldown);
        tag.putInt("FailedCoverTicks0459",failedCoverTicks);tag.putBoolean("ExposedHunt0459",exposedHunt);
        tag.putBoolean("CoverPause0459",coverPause);tag.putBoolean("Emerging0459",emerging);tag.putInt("PassingSwipeTicks0459",passingSwipeTicks);
        tag.putInt("HuntPhase0459",huntPhase()==WITHDRAW?WITHDRAW:STALK);
        if(literaryBase!=null&&literaryPlace!=null){tag.putLong("LiteraryBase",literaryBase.asLong());tag.putString("LiteraryScene",literaryPlace.id());}
        if(memoryOwner!=null&&memoryBase!=null){tag.putUUID("MemoryOwner",memoryOwner);tag.putLong("MemoryBase",memoryBase.asLong());tag.putInt("MemoryPhase",memoryPhase());}
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); shoreBase = tag.contains("ShoreBase") ? BlockPos.of(tag.getLong("ShoreBase")) : null;
        visit = tag.getInt("TownVisit"); cooldown = tag.contains("HuntCooldown")?Math.min(60,Math.max(0,tag.getInt("HuntCooldown"))):30; cancelStrike(); route.clear();routeGoal=null;coverReachable=false;
        coverPause=tag.getBoolean("CoverPause0459")&&cooldown>0;emerging=tag.getBoolean("Emerging0459");passingSwipeTicks=Math.min(20,Math.max(0,tag.getInt("PassingSwipeTicks0459")));
        entityData.set(HUNT_PHASE,tag.contains("HuntPhase0459")&&tag.getInt("HuntPhase0459")==WITHDRAW||!tag.contains("HuntPhase0459")&&cooldown>0?WITHDRAW:STALK);
        failedCoverTicks=Math.min(COVER_SEARCH_LIMIT,Math.max(0,tag.getInt("FailedCoverTicks0459")));exposedHunt=tag.getBoolean("ExposedHunt0459");clearDoorBreak();
        if(shoreBase!=null){setNoGravity(true);settleTicks=40;}
        if(tag.contains("LiteraryBase")){var place=LabyrinthPlace.byId(tag.getString("LiteraryScene"));if(place!=null)literaryHunt(BlockPos.of(tag.getLong("LiteraryBase")),place);}
        if(tag.hasUUID("MemoryOwner")){recollection(tag.getUUID("MemoryOwner"),BlockPos.of(tag.getLong("MemoryBase")));memoryPhase(tag.getInt("MemoryPhase"));setNoGravity(memoryPhase()==1||memoryPhase()==3);}
    }
}
