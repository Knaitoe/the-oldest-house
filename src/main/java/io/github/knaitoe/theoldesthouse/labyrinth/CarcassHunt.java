package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.*;

/** One physical hunter clock. Sight and actual footsteps leave a last known search point. */
public final class CarcassHunt {
    private static final String STATE="CarcassSearch0455";
    private final Map<UUID,Vec3> lastPositions=new HashMap<>();
    private final Deque<BlockPos> route=new ArrayDeque<>();
    private BlockPos routeGoal;
    public static final BlockPos START=new BlockPos(-18,0,-46);
    private static final List<BlockPos> PATROL=List.of(START,new BlockPos(4,0,-39),new BlockPos(22,0,-66),
            new BlockPos(28,0,-104),new BlockPos(-16,0,-116),new BlockPos(-30,0,-77));

    public static boolean concealed(ServerPlayer p,BlockPos b){
        if(p.getY()<b.getY()-.25&&Math.abs(p.getX()-b.getX())<2&&p.getZ()>b.getZ()-38&&p.getZ()<b.getZ()-28)return true;
        if(!p.isShiftKeyDown())return false;
        for(int y=1;y<=2;y++){var s=p.level().getBlockState(p.blockPosition().above(y));
            if(s.is(HouseBlocks.FOREST_COVER.get())||s.getBlock() instanceof LeavesBlock)return true;}
        return false;
    }
    public static boolean sees(LiteraryActor a,ServerPlayer p){
        return a.distanceToSqr(p)<42*42&&a.level().clip(new ClipContext(a.getEyePosition(),p.getEyePosition(),
                ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,a)).getType()==HitResult.Type.MISS;
    }
    private static boolean eligible(ServerPlayer p){return !p.isCreative()&&LiteraryVignettes.inside(p,LabyrinthPlace.ELK_CARCASSES)
            &&LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.ELK_CARCASSES).getBoolean("Here");}
    private static CompoundTag state(LiteraryActor a){return a.getPersistentData().getCompound(STATE);}
    public static boolean tracks(LiteraryActor a,UUID id){var s=state(a);return s.getInt("Memory")>0&&s.hasUUID("Reader")&&s.getUUID("Reader").equals(id);}
    public static BlockPos searchPoint(LiteraryActor a){var s=state(a);return s.contains("Point")?BlockPos.of(s.getLong("Point")):null;}
    private static void remember(LiteraryActor a,ServerPlayer p){
        var s=state(a);s.putUUID("Reader",p.getUUID());s.putLong("Point",p.blockPosition().asLong());s.putInt("Memory",220);
        a.getPersistentData().put(STATE,s);
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.ELK_CARCASSES);
        own.putBoolean("Pursued",true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.ELK_CARCASSES,own);
    }
    /** Loud interactions remain positional; they never give the hunter an invisible live target. */
    public static void noise(ServerPlayer p,BlockPos at){
        if(!eligible(p))return;var world=LiteraryVignettes.shared(LabyrinthData.get(p.server),LabyrinthPlace.ELK_CARCASSES);
        if(world.hasUUID("Killer")&&p.serverLevel().getEntity(world.getUUID("Killer")) instanceof LiteraryActor a
                &&a.position().distanceToSqr(at.getCenter())<32*32){remember(a,p);var s=state(a);s.putLong("Point",at.asLong());a.getPersistentData().put(STATE,s);}
    }
    private static boolean walkable(LiteraryActor a,BlockPos b,BlockPos n){
        int x=n.getX()-b.getX(),z=n.getZ()-b.getZ();
        return Math.abs(x)<=52&&z>=-127&&z<=-7&&n.getY()==b.getY()
                &&!a.level().getBlockState(n.below()).getCollisionShape(a.level(),n.below()).isEmpty()
                &&a.level().noCollision(a,new AABB(n.getX()+.2,n.getY(),n.getZ()+.2,n.getX()+.8,n.getY()+1.3,n.getZ()+.8));
    }
    private static List<BlockPos> path(LiteraryActor a,BlockPos b,BlockPos start,BlockPos goal){
        if(!walkable(a,b,start)||!walkable(a,b,goal))return List.of();
        var open=new ArrayDeque<BlockPos>();var prev=new HashMap<BlockPos,BlockPos>();open.add(start);prev.put(start,start);
        while(!open.isEmpty()&&prev.size()<6000){var n=open.removeFirst();if(n.equals(goal)){
            var result=new LinkedList<BlockPos>();while(!n.equals(start)){result.addFirst(n);n=prev.get(n);}return result;}
            for(var d:Direction.Plane.HORIZONTAL){var next=n.relative(d);if(!prev.containsKey(next)&&walkable(a,b,next)){prev.put(next,n);open.add(next);}}
        }return List.of();
    }
    public void tick(LiteraryActor a){
        if(!(a.level() instanceof ServerLevel l)||!a.isAlive())return;var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(l.getServer()).houseOrigin();
        if(origin==null)return;var b=LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_CARCASSES);
        var readers=l.players().stream().filter(CarcassHunt::eligible).toList();
        lastPositions.keySet().retainAll(readers.stream().map(ServerPlayer::getUUID).toList());
        if(readers.isEmpty()){route.clear();return;}
        var s=state(a);if(s.getInt("AttackDelay")>0)s.putInt("AttackDelay",s.getInt("AttackDelay")-1);
        a.getPersistentData().put(STATE,s);
        ServerPlayer found=null;double nearest=Double.MAX_VALUE;
        for(var p:readers){var last=lastPositions.put(p.getUUID(),p.position());double moved=last==null?0:last.distanceToSqr(p.position());
            boolean heard=moved>.004&&(p.isSprinting()||!p.isShiftKeyDown())&&a.distanceToSqr(p)<(p.isSprinting()?32*32:12*12);
            if((sees(a,p)||heard)&&a.distanceToSqr(p)<nearest){found=p;nearest=a.distanceToSqr(p);}}
        if(found!=null){remember(a,found);s=state(a);}else if(s.getInt("Memory")>0)s.putInt("Memory",s.getInt("Memory")-1);
        boolean chasing=s.getInt("Memory")>0&&s.contains("Point");
        int stop=Math.floorMod(s.getInt("Patrol"),PATROL.size());
        var goal=chasing?new BlockPos(BlockPos.of(s.getLong("Point")).getX(),b.getY(),BlockPos.of(s.getLong("Point")).getZ()):b.offset(PATROL.get(stop));
        if(!walkable(a,b,goal)){
            var candidates=BlockPos.betweenClosed(goal.offset(-3,0,-3),goal.offset(3,0,3));double best=Double.MAX_VALUE;BlockPos fallback=null;
            for(var n:candidates)if(walkable(a,b,n)&&n.distSqr(goal)<best){best=n.distSqr(goal);fallback=n.immutable();}
            if(fallback!=null)goal=fallback;
        }
        if(a.tickCount%20==0||routeGoal==null||!routeGoal.equals(goal)){
            routeGoal=goal;route.clear();route.addAll(path(a,b,BlockPos.containing(a.getX(),b.getY(),a.getZ()),goal));}
        if(a.position().distanceToSqr(Vec3.atBottomCenterOf(goal))<1.5){
            if(!chasing)s.putInt("Patrol",stop+1);
            else if(a.tickCount%40==0){int side=(a.tickCount/40)%4;var around=goal.relative(Direction.from2DDataValue(side),3);if(walkable(a,b,around)){route.clear();route.addAll(path(a,b,BlockPos.containing(a.getX(),b.getY(),a.getZ()),around));}}
        }
        while(!route.isEmpty()&&a.position().distanceToSqr(Vec3.atBottomCenterOf(route.peekFirst()))<.12)route.removeFirst();
        if(!route.isEmpty()){
            var next=route.peekFirst();boolean low=l.getBlockState(next.above()).is(HouseBlocks.FOREST_COVER.get())||l.getBlockState(a.blockPosition().above()).is(HouseBlocks.FOREST_COVER.get());
            a.setPose(low?Pose.CROUCHING:Pose.STANDING);a.refreshDimensions();
            var delta=Vec3.atBottomCenterOf(next).subtract(a.position()).multiply(1,0,1);
            if(delta.lengthSqr()>.001){var before=a.position();a.move(MoverType.SELF,delta.normalize().scale(Math.min(chasing?.23:.12,delta.length())));
                a.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));a.yBodyRot=a.getYRot();a.walkAnimation.update((float)a.position().distanceTo(before)*4,.4F);}
        }
        if(found!=null&&a.distanceToSqr(found)<3.5&&s.getInt("AttackDelay")==0&&sees(a,found)){
            a.swing(InteractionHand.MAIN_HAND);found.hurt(found.damageSources().mobAttack(a),5);s.putInt("AttackDelay",30);}
        a.getPersistentData().put(STATE,s);
    }
}
