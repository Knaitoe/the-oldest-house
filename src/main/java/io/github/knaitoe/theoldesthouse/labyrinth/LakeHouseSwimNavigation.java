package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/** Small, loaded-only swimming routes for the original Tess. No warps, chunk loads or wall crossings. */
public final class LakeHouseSwimNavigation {
    private record Route(Vec3 goal,ArrayDeque<Vec3> points){}
    private record Node(BlockPos cell,double cost,double score){}
    private static final Map<LiteraryActor,Route> ROUTES=new WeakHashMap<>();
    private LakeHouseSwimNavigation(){}
    private static boolean loaded(ServerLevel l,BlockPos at){return l.hasChunkAt(at)&&l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4));}
    private static Vec3 centre(BlockPos cell){return new Vec3(cell.getX()+.5,cell.getY()+.15,cell.getZ()+.5);}
    private static boolean clear(LiteraryActor a,Vec3 at){
        var l=(ServerLevel)a.level();var box=a.getBoundingBox().move(at.subtract(a.position()));
        if(!loaded(l,BlockPos.containing(box.minX,box.minY,box.minZ))||!loaded(l,BlockPos.containing(box.maxX,box.maxY,box.maxZ)))return false;
        return l.noCollision(a,box);
    }
    private static boolean stepClear(LiteraryActor a,Vec3 delta){
        var l=(ServerLevel)a.level();var box=a.getBoundingBox().expandTowards(delta);
        return loaded(l,BlockPos.containing(box.minX,box.minY,box.minZ))&&loaded(l,BlockPos.containing(box.maxX,box.maxY,box.maxZ))&&l.noCollision(a,box);
    }
    private static Route search(LiteraryActor a,Vec3 goal){
        var start=a.blockPosition();var target=BlockPos.containing(goal);var open=new PriorityQueue<Node>(Comparator.comparingDouble(Node::score));
        var cost=new HashMap<BlockPos,Double>();var parent=new HashMap<BlockPos,BlockPos>();cost.put(start,0D);open.add(new Node(start,0,start.distManhattan(target)));
        BlockPos best=start;double nearest=start.distSqr(target);int visited=0;
        while(!open.isEmpty()&&visited++<512){
            var node=open.remove();if(node.cost()>cost.getOrDefault(node.cell(),Double.MAX_VALUE))continue;
            double distance=node.cell().distSqr(target);if(distance<nearest){best=node.cell();nearest=distance;}if(distance<=1){best=node.cell();break;}
            for(var side:Direction.values()){
                var next=node.cell().relative(side);if(next.distManhattan(start)>48||!clear(a,centre(next)))continue;
                double g=node.cost()+1;if(g>=cost.getOrDefault(next,Double.MAX_VALUE))continue;
                cost.put(next,g);parent.put(next,node.cell());open.add(new Node(next,g,g+next.distManhattan(target)));
            }
        }
        var points=new ArrayDeque<Vec3>();for(var cell=best;!cell.equals(start)&&parent.containsKey(cell);cell=parent.get(cell))points.addFirst(centre(cell));
        return new Route(goal,points);
    }
    public static void forget(LiteraryActor a){ROUTES.remove(a);}
    public static void move(LiteraryActor a,Vec3 goal,double maximum){
        var route=ROUTES.get(a);if(route!=null&&route.goal().distanceToSqr(goal)>4){ROUTES.remove(a);route=null;}
        if(route!=null){while(!route.points().isEmpty()&&route.points().getFirst().distanceToSqr(a.position())<.08)route.points().removeFirst();if(route.points().isEmpty()){ROUTES.remove(a);route=null;}}
        var aim=route==null?goal:route.points().getFirst();var delta=aim.subtract(a.position());double length=delta.length();if(length<.05){a.setDeltaMovement(Vec3.ZERO);return;}
        var step=delta.scale(Math.min(maximum,length)/length);
        if(!stepClear(a,step)){
            // A search is bounded and retry-pacing follows actual actor ticks, never a timeout teleport.
            long now=a.level().getGameTime();if(now-a.getPersistentData().getLong("LakeRouteSearch")<20)return;
            a.getPersistentData().putLong("LakeRouteSearch",now);route=search(a,goal);ROUTES.put(a,route);if(route.points().isEmpty())return;
            delta=route.points().getFirst().subtract(a.position());length=delta.length();if(length<.01)return;step=delta.scale(Math.min(maximum,length)/length);if(!stepClear(a,step))return;
        }
        a.move(MoverType.SELF,step);a.setDeltaMovement(Vec3.ZERO);
        if(delta.horizontalDistanceSqr()>.002){float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));a.setYRot(Mth.rotateIfNecessary(a.getYRot(),yaw,30));a.setYHeadRot(a.getYRot());a.yBodyRot=a.getYRot();}
        float pitch=a.phase()==2||a.phase()==6?(float)-Math.toDegrees(Math.atan2(delta.y,Math.max(.01,delta.horizontalDistance()))):0;
        a.setXRot(Mth.lerp(.35F,a.getXRot(),Mth.clamp(pitch,-70,70)));
    }
}
