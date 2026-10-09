package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Native terrain navigation with one bounded, physical foliage fallback per private killer. */
public final class KillerNavigation {
    private static final String STATE="ElkMovement0460";
    public static final int BLOCKED_LIMIT=60;
    /** 5.34 blocks/second: just below the ordinary player's 5.61-block sprint. */
    public static final double CHASE_STEP=.267;
    private final Deque<BlockPos> route=new ArrayDeque<>();
    private Vec3 goal;
    private int plannedAt=-20;

    private static CompoundTag state(LiteraryActor a){return a.getPersistentData().getCompound(STATE);}
    public static void request(LiteraryActor a,Vec3 target,double speed){
        var s=state(a);
        if(!s.getBoolean("Moving")||target.distanceToSqr(target(s))>.01){
            if(!s.getBoolean("Moving"))s.putInt("Blocked",0);s.putBoolean("Failed",false);
            a.getNavigation().stop();
        }
        s.putBoolean("Moving",true);s.putDouble("X",target.x);s.putDouble("Y",target.y);s.putDouble("Z",target.z);s.putDouble("Speed",speed);
        a.getPersistentData().put(STATE,s);
    }
    public static void stop(LiteraryActor a){
        a.getNavigation().stop();a.setDeltaMovement(a.getDeltaMovement().multiply(0,1,0));
        KillerDoors.clear(a);
        YachtRailVault.cancel(a);
        var s=state(a);s.putBoolean("Moving",false);s.putInt("Blocked",0);a.getPersistentData().put(STATE,s);
    }
    public static boolean failed(LiteraryActor a){var s=state(a);if(!s.getBoolean("Failed"))return false;s.putBoolean("Failed",false);a.getPersistentData().put(STATE,s);return true;}
    public static boolean moving(LiteraryActor a){return state(a).getBoolean("Moving");}
    public static int recoveries(LiteraryActor a){return state(a).getInt("Recoveries0461");}
    private static Vec3 target(CompoundTag s){return new Vec3(s.getDouble("X"),s.getDouble("Y"),s.getDouble("Z"));}
    private static AABB body(LiteraryActor a,Vec3 foot,double height){return new AABB(foot.x-a.getBbWidth()/2,foot.y,foot.z-a.getBbWidth()/2,foot.x+a.getBbWidth()/2,foot.y+height,foot.z+a.getBbWidth()/2);}
    private static ServerPlayer reader(LiteraryActor a){
        if(!(a.level() instanceof ServerLevel l)||a.owner().isEmpty())return null;
        var p=l.getServer().getPlayerList().getPlayer(a.owner().get());
        return p!=null&&p.serverLevel()==l&&LiteraryVignettes.participant(p)&&LiteraryVignettes.inside(p,LabyrinthPlace.ELK_CARCASSES)
                &&LiteraryVignettes.personal(LabyrinthData.get(l.getServer()),p.getUUID(),LabyrinthPlace.ELK_CARCASSES).getBoolean("Here")?p:null;
    }
    private void plan(LiteraryActor a,CompoundTag s,BlockPos avoid){
        plannedAt=a.tickCount;route.clear();a.getNavigation().stop();
        var origin=HouseSavedData.get(((ServerLevel)a.level()).getServer()).houseOrigin();
        if(origin!=null&&Math.abs(goal.y-a.getY())<.6){
            int plane=BlockPos.containing(a.position()).getY();var base=LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_CARCASSES);
            var start=BlockPos.containing(a.position());var end=BlockPos.containing(goal.x,plane,goal.z);
            route.addAll(CarcassHunt.pathOnPlane(a,base,start,end,plane,false,avoid));
            if(route.isEmpty())route.addAll(CarcassHunt.pathOnPlane(a,base,start,end,plane,true,avoid));
        }
        if(route.isEmpty())a.getNavigation().moveTo(goal.x,goal.y,goal.z,s.getDouble("Speed"));
    }
    /** Empty/offline/observer readers never advance a private movement or failed-route clock. */
    public boolean prepare(LiteraryActor a){
        var owner=reader(a);
        if(owner==null){a.getNavigation().stop();a.setDeltaMovement(a.getDeltaMovement().multiply(0,1,0));route.clear();goal=null;return false;}
        var s=state(a);
        if(!s.getBoolean("Moving")){route.clear();goal=null;KillerDoors.clear(a);return true;}
        var next=target(s);
        if(goal==null||goal.distanceToSqr(next)>.01){goal=next;route.clear();plannedAt=-20;}
        if(YachtRailVault.active(a)){YachtRailVault.prepare(a);return true;}
        if(a.position().distanceToSqr(goal)<.16){stop(a);route.clear();return true;}
        if((route.isEmpty()&&a.getNavigation().isDone()&&a.tickCount-plannedAt>=20)||plannedAt==-20)plan(a,s,null);
        var door=KillerDoors.approach(a,goal);
        if(door!=null&&KillerDoors.work(a,door,java.util.List.of(owner)))return true;
        if(!route.isEmpty()&&a.onGround()&&YachtRailVault.canCross(a,a.blockPosition(),route.peekFirst())){
            YachtRailVault.begin(a,route.peekFirst());YachtRailVault.prepare(a);return true;
        }
        Vec3 aim=null;
        if(!route.isEmpty())aim=Vec3.atBottomCenterOf(route.peekFirst());
        else{var path=a.getNavigation().getPath();if(path!=null&&!path.isDone())aim=path.getNextEntityPos(a);}
        if(aim!=null){
            var delta=aim.subtract(a.position()).multiply(1,0,1);var step=delta.lengthSqr()<.001?Vec3.ZERO:delta.normalize().scale(Math.min(CHASE_STEP,delta.length()));
            var standing=body(a,a.position(),a.getDimensions(Pose.STANDING).height());
            boolean low=!CarcassHunt.fits(a,standing.minmax(standing.move(step)));
            a.setPose(low?Pose.CROUCHING:Pose.STANDING);a.refreshDimensions();CarcassHunt.clearLeaves(a,step);
        }
        return true;
    }
    public void tick(LiteraryActor a,Vec3 before){
        var s=state(a);if(!s.getBoolean("Moving"))return;
        if(YachtRailVault.active(a)){s.putInt("Blocked",0);a.getPersistentData().put(STATE,s);return;}
        if(KillerDoors.active(a)){s.putInt("Blocked",0);a.getPersistentData().put(STATE,s);return;}
        while(!route.isEmpty()&&a.position().distanceToSqr(Vec3.atBottomCenterOf(route.peekFirst()))<.12)route.removeFirst();
        if(!route.isEmpty()){
            var delta=Vec3.atBottomCenterOf(route.peekFirst()).subtract(a.position()).multiply(1,0,1);
            if(delta.lengthSqr()>.001){var step=delta.normalize().scale(Math.min(Math.min(CHASE_STEP,.24*s.getDouble("Speed")),delta.length()));
                CarcassHunt.clearLeaves(a,step);a.move(MoverType.SELF,step);a.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));a.yBodyRot=a.getYRot();
                a.walkAnimation.update((float)a.position().distanceTo(before)*4,.4F);
            }
        }
        boolean waiting=a.position().distanceToSqr(target(s))>.16&&a.position().subtract(before).multiply(1,0,1).lengthSqr()<.0001;
        int blocked=waiting?s.getInt("Blocked")+1:0;s.putInt("Blocked",blocked);
        if(blocked>=BLOCKED_LIMIT){
            BlockPos avoid=route.peekFirst();var nativePath=a.getNavigation().getPath();
            if(avoid==null&&nativePath!=null&&!nativePath.isDone())avoid=nativePath.getNextNodePos();
            plan(a,s,avoid);s.putInt("Recoveries0461",s.getInt("Recoveries0461")+1);s.putInt("Blocked",0);
            if(route.isEmpty()&&(a.getNavigation().getPath()==null||!a.getNavigation().getPath().canReach())){
                a.getNavigation().stop();route.clear();goal=null;s.putBoolean("Moving",false);s.putBoolean("Failed",true);
            }
        }
        a.getPersistentData().put(STATE,s);
    }
}
