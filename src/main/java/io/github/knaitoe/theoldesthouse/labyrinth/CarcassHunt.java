package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.EventHooks;

/** One physical hunter clock. Sight and real footsteps leave a last known point, never a hidden live target. */
public final class CarcassHunt {
    private static final String STATE="CarcassSearch0455";
    public static final int BLOCKED_LIMIT=80;
    private final Map<UUID,Vec3> lastPositions=new HashMap<>();
    private final Deque<BlockPos> route=new ArrayDeque<>();
    private BlockPos routeGoal;
    public static final BlockPos START=new BlockPos(-18,0,-46);
    private static final List<BlockPos> PATROL=List.of(START,new BlockPos(4,0,-39),new BlockPos(22,0,-66),
            new BlockPos(28,0,-104),new BlockPos(-16,0,-116),new BlockPos(-30,0,-77));

    public static boolean concealed(ServerPlayer p,BlockPos b){
        if(p.getY()<b.getY()-.25&&Math.abs(p.getX()-b.getX())<2&&p.getZ()>b.getZ()-38&&p.getZ()<b.getZ()-28)return true;
        if(!p.isShiftKeyDown()||p.getPose()!=Pose.CROUCHING)return false;
        for(int y=1;y<=2;y++)if(foliage(p.level().getBlockState(p.blockPosition().above(y))))return true;
        return false;
    }
    private static LabyrinthPlace scene(LiteraryActor a){return a.scene().equals(LabyrinthPlace.CAMP_BLOOD.id())?LabyrinthPlace.CAMP_BLOOD:LabyrinthPlace.ELK_CARCASSES;}
    public static boolean sees(LiteraryActor a,ServerPlayer p){
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();
        if(origin!=null&&a.distanceToSqr(p)>9&&concealed(p,LabyrinthPlaces.base(origin,scene(a))))return false;
        return a.distanceToSqr(p)<42*42&&a.level().clip(new ClipContext(a.getEyePosition(),p.getEyePosition(),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a)).getType()==HitResult.Type.MISS;
    }
    private static boolean eligible(ServerPlayer p,LabyrinthPlace scene){return p.isAlive()&&!p.isSpectator()&&!p.isCreative()&&LiteraryVignettes.inside(p,scene)
            &&LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),scene).getBoolean("Here");}
    private static CompoundTag state(LiteraryActor a){return a.getPersistentData().getCompound(STATE);}
    public static boolean tracks(LiteraryActor a,UUID id){var s=state(a);return s.getInt("Memory")>0&&s.hasUUID("Reader")&&s.getUUID("Reader").equals(id);}
    public static BlockPos searchPoint(LiteraryActor a){var s=state(a);return s.contains("Point")?BlockPos.of(s.getLong("Point")):null;}
    public static int blockedTicks(LiteraryActor a){return state(a).getInt("BlockedTicks0459");}
    private static void remember(LiteraryActor a,ServerPlayer p){
        var s=state(a);s.putUUID("Reader",p.getUUID());s.putLong("Point",p.blockPosition().asLong());s.putInt("Memory",220);
        a.getPersistentData().put(STATE,s);
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),scene(a));
        own.putBoolean("Pursued",true);LiteraryVignettes.save(d,p.getUUID(),scene(a),own);
    }
    /** Loud interactions remain positional; they never give the hunter an invisible live target. */
    public static void noise(ServerPlayer p,BlockPos at){
        var scene=LiteraryVignettes.current(p);if(scene==LabyrinthPlace.ELK_CARCASSES){ElkHunt.noise(p,at);return;}if(scene!=LabyrinthPlace.CAMP_BLOOD||!eligible(p,scene))return;var world=LiteraryVignettes.shared(LabyrinthData.get(p.server),scene);
        if(world.hasUUID("Killer")&&p.serverLevel().getEntity(world.getUUID("Killer")) instanceof LiteraryActor a
                &&a.position().distanceToSqr(at.getCenter())<32*32){remember(a,p);var s=state(a);s.putLong("Point",at.asLong());a.getPersistentData().put(STATE,s);}
    }
    private static boolean foliage(BlockState s){return s.is(HouseBlocks.FOREST_COVER.get())||s.getBlock() instanceof LeavesBlock;}
    private static AABB box(Vec3 foot,double height){return new AABB(foot.x-.3,foot.y,foot.z-.3,foot.x+.3,foot.y+height,foot.z+.3);}
    /** Check the actual body shape; only leaves can be cleared, and only when native griefing permits it. */
    static boolean fits(LiteraryActor a,AABB body){
        boolean breakLeaves=a.level() instanceof ServerLevel l&&l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        for(var at:BlockPos.betweenClosed(BlockPos.containing(body.minX,body.minY,body.minZ),BlockPos.containing(body.maxX-.0001,body.maxY-.0001,body.maxZ-.0001))){
            if(!a.level().hasChunkAt(at))return false;var s=a.level().getBlockState(at);
            if(breakLeaves&&foliage(s)&&a.level().getBlockEntity(at)==null)continue;
            for(var shape:s.getCollisionShape(a.level(),at).toAabbs())if(shape.move(at).intersects(body))return false;
        }return true;
    }
    private static boolean walkable(LiteraryActor a,BlockPos b,BlockPos n){return walkable(a,b,n,b.getY());}
    private static boolean walkable(LiteraryActor a,BlockPos b,BlockPos n,int plane){
        return walkable(a,b,n,plane,false);
    }
    private static boolean planningFits(LiteraryActor a,AABB body,boolean doors){
        if(!doors&&a.owner().isEmpty())return fits(a,body);
        for(var at:BlockPos.betweenClosed(BlockPos.containing(body.minX,body.minY,body.minZ),BlockPos.containing(body.maxX-.0001,body.maxY-.0001,body.maxZ-.0001))){
            if(!a.level().hasChunkAt(at))return false;var s=a.level().getBlockState(at);
            if(doors&&KillerDoors.breakable(a,at))continue;
            if(a.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)&&foliage(s)&&a.level().getBlockEntity(at)==null&&!occupiedPeerCover(a,at))continue;
            for(var shape:s.getCollisionShape(a.level(),at).toAabbs())if(shape.move(at).intersects(body))return false;
        }return true;
    }
    private static boolean occupiedPeerCover(LiteraryActor a,BlockPos at){
        return a.owner().isPresent()&&a.level() instanceof ServerLevel l&&l.players().stream().anyMatch(p->p.isAlive()&&!p.isSpectator()
                &&!p.getUUID().equals(a.owner().get())&&p.getBoundingBox().inflate(2,2,2).intersects(new AABB(at)));
    }
    private static boolean walkable(LiteraryActor a,BlockPos b,BlockPos n,int plane,boolean doors){
        var bounds=scene(a).room();int x=n.getX()-b.getX(),z=n.getZ()-b.getZ();if(x<bounds.minX()+1||x>bounds.maxX()-1||z<bounds.minZ()+1||z>bounds.maxZ()-1||n.getY()!=plane||!a.level().hasChunkAt(n))return false;
        var floor=a.level().getBlockState(n.below());if(foliage(floor)||floor.getCollisionShape(a.level(),n.below()).isEmpty())return false;
        var foot=Vec3.atBottomCenterOf(n);return planningFits(a,box(foot,a.getDimensions(Pose.STANDING).height()),doors)||planningFits(a,box(foot,1.3),doors);
    }
    private record Node(BlockPos pos,int cost,int score){}
    private static int distance(BlockPos a,BlockPos b){return Math.abs(a.getX()-b.getX())+Math.abs(a.getZ()-b.getZ());}
    /** Directed bounded search reaches distant trails without scanning the entire forest first. */
    private static List<BlockPos> path(LiteraryActor a,BlockPos b,BlockPos start,BlockPos goal){return pathOnPlane(a,b,start,goal,b.getY());}
    static List<BlockPos> pathOnPlane(LiteraryActor a,BlockPos b,BlockPos start,BlockPos goal,int plane){
        return pathOnPlane(a,b,start,goal,plane,false,null);
    }
    static List<BlockPos> pathOnPlane(LiteraryActor a,BlockPos b,BlockPos start,BlockPos goal,int plane,boolean doors,BlockPos avoid){
        if(!walkable(a,b,goal,plane,doors))return List.of();
        var open=new PriorityQueue<Node>(Comparator.comparingInt(Node::score).thenComparingInt(Node::cost));
        var prev=new HashMap<BlockPos,BlockPos>();var costs=new HashMap<BlockPos,Integer>();var clear=new HashMap<BlockPos,Boolean>();
        open.add(new Node(start,0,distance(start,goal)));prev.put(start,start);costs.put(start,0);int examined=0;
        while(!open.isEmpty()&&examined++<8500){var node=open.remove();var n=node.pos();if(node.cost()!=costs.get(n))continue;
            if(n.equals(goal)){var result=new LinkedList<BlockPos>();while(!n.equals(start)){result.addFirst(n);n=prev.get(n);}return result;}
            for(var d:Direction.Plane.HORIZONTAL){var next=n.relative(d);int cost=node.cost()+1;
                if(!next.equals(avoid)&&cost<costs.getOrDefault(next,Integer.MAX_VALUE)&&clear.computeIfAbsent(next,p->walkable(a,b,p,plane,doors))){
                    prev.put(next,n);costs.put(next,cost);open.add(new Node(next,cost,cost+distance(next,goal)));}}
        }return List.of();
    }
    /** Native destruction happens only in the body's next physical step, once per actor tick. */
    static void clearLeaves(LiteraryActor a,Vec3 step){
        if(!(a.level() instanceof ServerLevel l)||!l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))return;
        var body=a.getBoundingBox();var sweep=body.minmax(body.move(step));if(!fits(a,sweep))return;int removed=0;
        for(var at:BlockPos.betweenClosed(BlockPos.containing(sweep.minX,sweep.minY,sweep.minZ),BlockPos.containing(sweep.maxX-.0001,sweep.maxY-.0001,sweep.maxZ-.0001))){
            var s=l.getBlockState(at);if(removed>=4||!l.hasChunkAt(at)||!foliage(s)||l.getBlockEntity(at)!=null)continue;
            if(s.getCollisionShape(l,at).toAabbs().stream().noneMatch(shape->shape.move(at).intersects(sweep)))continue;
            // A private sighting cannot tear away another reader's occupied cover.
            // The shared Camp Blood actor remains visible and physical for everyone.
            if(occupiedPeerCover(a,at))continue;
            if(EventHooks.onEntityDestroyBlock(a,at,s)&&l.destroyBlock(at,true,a))removed++;
        }
    }
    public void tick(LiteraryActor a){
        if(!(a.level() instanceof ServerLevel l)||!a.isAlive())return;var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(l.getServer()).houseOrigin();
        if(origin==null)return;var b=LabyrinthPlaces.base(origin,scene(a));
        var readers=l.players().stream().filter(p->eligible(p,scene(a))).toList();lastPositions.keySet().retainAll(readers.stream().map(ServerPlayer::getUUID).toList());
        if(readers.isEmpty()){route.clear();routeGoal=null;return;}
        var s=state(a);if(s.getInt("AttackDelay")>0)s.putInt("AttackDelay",s.getInt("AttackDelay")-1);
        if(s.getInt("AvoidTicks0459")>0)s.putInt("AvoidTicks0459",s.getInt("AvoidTicks0459")-1);a.getPersistentData().put(STATE,s);
        ServerPlayer found=null;double nearest=Double.MAX_VALUE;
        for(var p:readers){var last=lastPositions.put(p.getUUID(),p.position());double moved=last==null?0:last.distanceToSqr(p.position());
            boolean heard=moved>.004&&(p.isSprinting()||!p.isShiftKeyDown())&&a.distanceToSqr(p)<(p.isSprinting()?32*32:12*12);
            boolean rejected=s.getInt("AvoidTicks0459")>0&&s.contains("RejectedPoint0459")&&p.blockPosition().distSqr(BlockPos.of(s.getLong("RejectedPoint0459")))<=4;
            if(!rejected&&(sees(a,p)||heard)&&a.distanceToSqr(p)<nearest){found=p;nearest=a.distanceToSqr(p);}}
        if(found!=null){remember(a,found);s=state(a);}else if(s.getInt("Memory")>0)s.putInt("Memory",s.getInt("Memory")-1);
        boolean chasing=s.getInt("Memory")>0&&s.contains("Point");a.appearance(LiteraryActor.KILLER,chasing?ElkHunt.RUN:ElkHunt.WALK);int stop=Math.floorMod(s.getInt("Patrol"),PATROL.size());
        var goal=chasing?new BlockPos(BlockPos.of(s.getLong("Point")).getX(),b.getY(),BlockPos.of(s.getLong("Point")).getZ()):b.offset(PATROL.get(stop));
        if(!walkable(a,b,goal)){double best=Double.MAX_VALUE;BlockPos fallback=null;
            for(var n:BlockPos.betweenClosed(goal.offset(-3,0,-3),goal.offset(3,0,3)))if(walkable(a,b,n)&&n.distSqr(goal)<best){best=n.distSqr(goal);fallback=n.immutable();}
            if(fallback!=null)goal=fallback;}
        if(a.tickCount%20==0||routeGoal==null||!routeGoal.equals(goal)){routeGoal=goal;route.clear();route.addAll(path(a,b,BlockPos.containing(a.getX(),b.getY(),a.getZ()),goal));}
        boolean arrived=a.position().distanceToSqr(Vec3.atBottomCenterOf(goal))<1.5;
        if(arrived){if(!chasing)s.putInt("Patrol",stop+1);
            else if(a.tickCount%40==0){int side=(a.tickCount/40)%4;var around=goal.relative(Direction.from2DDataValue(side),3);
                if(walkable(a,b,around)){route.clear();route.addAll(path(a,b,BlockPos.containing(a.getX(),b.getY(),a.getZ()),around));}}}
        while(!route.isEmpty()&&a.position().distanceToSqr(Vec3.atBottomCenterOf(route.peekFirst()))<.12)route.removeFirst();
        var before=a.position();
        if(!route.isEmpty()){var next=route.peekFirst();var delta=Vec3.atBottomCenterOf(next).subtract(before).multiply(1,0,1);
            if(delta.lengthSqr()>.001){var movement=delta.normalize().scale(Math.min(chasing?.23:.12,delta.length()));
                var standing=box(before,a.getDimensions(Pose.STANDING).height());boolean low=!fits(a,standing.minmax(standing.move(movement)));
                a.setPose(low?Pose.CROUCHING:Pose.STANDING);a.refreshDimensions();clearLeaves(a,movement);a.move(MoverType.SELF,movement);
                a.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));a.yBodyRot=a.getYRot();a.walkAnimation.update((float)a.position().distanceTo(before)*4,.4F);}}
        boolean blocked=!arrived&&a.position().distanceToSqr(before)<.0001;
        int failures=blocked?s.getInt("BlockedTicks0459")+1:0;s.putInt("BlockedTicks0459",failures);
        if(failures>=BLOCKED_LIMIT){if(chasing){s.putLong("RejectedPoint0459",s.getLong("Point"));s.putInt("AvoidTicks0459",100);s.putInt("Memory",0);s.remove("Point");}
            s.putInt("Patrol",stop+1);s.putInt("BlockedTicks0459",0);route.clear();routeGoal=null;}
        if(found!=null&&a.distanceToSqr(found)<3.5&&s.getInt("AttackDelay")==0&&sees(a,found)){
            a.swing(InteractionHand.MAIN_HAND);found.hurt(found.damageSources().mobAttack(a),5);s.putInt("AttackDelay",30);}
        a.getPersistentData().put(STATE,s);
    }
}
