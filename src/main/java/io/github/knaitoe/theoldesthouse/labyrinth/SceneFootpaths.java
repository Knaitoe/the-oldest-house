package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;

/** Bounded foot routes around the actual set's collision, for the original scripted bodies. */
public final class SceneFootpaths {
    private SceneFootpaths(){}
    private static final Map<LivingEntity,Route> ROUTES=new WeakHashMap<>();
    private static final class Route {final Vec3 goal;final List<Vec3> points;int next;Route(Vec3 goal,List<Vec3> points){this.goal=goal;this.points=points;}}
    private record Node(int x,int z,double y,double cost,double score,Node previous){}
    private static long key(int x,int z){return (long)x<<32|(z&0xffffffffL);}
    public static void face(LivingEntity body,Vec3 target,float limit){
        var d=target.subtract(body.position());if(d.horizontalDistanceSqr()<.0001)return;float wanted=(float)Math.toDegrees(Math.atan2(-d.x,d.z));float yaw=net.minecraft.util.Mth.approachDegrees(body.getYRot(),wanted,limit);body.setYRot(yaw);body.setYHeadRot(yaw);body.yBodyRot=yaw;
    }
    private static boolean loaded(LivingEntity body,Vec3 at){var p=BlockPos.containing(at);return body.level().hasChunkAt(p)&&(!(body.level() instanceof ServerLevel l)||l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(p.getX()>>4,p.getZ()>>4)));}
    private static AABB box(LivingEntity body,double x,double y,double z){double half=body.getBbWidth()*.5+.001;return new AABB(x-half,y+.002,z-half,x+half,y+body.getBbHeight(),z+half);}
    /** Real support can rise over the fallen rail or carpet; an open ditch is never a floor. */
    private static Vec3 feet(LivingEntity body,double x,double y,double z){
        var at=new Vec3(x,y,z);if(!loaded(body,at))return null;
        double top=Double.NEGATIVE_INFINITY;
        for(var shape:body.level().getBlockCollisions(body,new AABB(x-body.getBbWidth()/2,y-.65,z-body.getBbWidth()/2,x+body.getBbWidth()/2,y+.61,z+body.getBbWidth()/2)))
            for(var part:shape.toAabbs())if(part.maxY<=y+.601&&part.maxY>=y-.651)top=Math.max(top,part.maxY);
        if(!Double.isFinite(top)||body.level().getBlockCollisions(body,box(body,x,top,z)).iterator().hasNext())return null;
        return new Vec3(x,top,z);
    }
    private static boolean clear(LivingEntity body,Vec3 from,Vec3 to){
        int samples=Math.max(1,(int)Math.ceil(from.subtract(to).horizontalDistance()/.3));if(samples>160)return false;
        double y=from.y;
        for(int i=1;i<=samples;i++){double t=(double)i/samples;var p=feet(body,net.minecraft.util.Mth.lerp(t,from.x,to.x),y,net.minecraft.util.Mth.lerp(t,from.z,to.z));if(p==null)return false;y=p.y;}
        return true;
    }
    private static List<Vec3> find(LivingEntity body,Vec3 goal){
        var start=body.position();int sx=(int)Math.round(start.x*2),sz=(int)Math.round(start.z*2);
        var queue=new PriorityQueue<Node>(Comparator.comparingDouble(Node::score));var costs=new HashMap<Long,Double>();
        var first=new Node(sx,sz,start.y,0,start.subtract(goal).horizontalDistance(),null);queue.add(first);costs.put(key(sx,sz),0D);Node best=first;
        for(int count=0;!queue.isEmpty()&&count<1800;count++){
            var n=queue.remove();if(n.cost>costs.getOrDefault(key(n.x,n.z),Double.POSITIVE_INFINITY)+.001)continue;
            double remaining=Math.hypot(n.x*.5-goal.x,n.z*.5-goal.z);if(remaining<Math.hypot(best.x*.5-goal.x,best.z*.5-goal.z))best=n;
            if(remaining<.55&&clear(body,new Vec3(n.x*.5,n.y,n.z*.5),goal)){best=n;break;}
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
                if(dx==0&&dz==0)continue;int x=n.x+dx,z=n.z+dz;if(Math.abs(x-sx)>48||Math.abs(z-sz)>48)continue;
                var p=feet(body,x*.5,n.y,z*.5);if(p==null||!clear(body,new Vec3(n.x*.5,n.y,n.z*.5),p))continue;
                double cost=n.cost+Math.hypot(dx,dz)*.5+Math.abs(p.y-n.y)*.5;long k=key(x,z);if(cost>=costs.getOrDefault(k,Double.POSITIVE_INFINITY))continue;
                costs.put(k,cost);queue.add(new Node(x,z,p.y,cost,cost+Math.hypot(p.x-goal.x,p.z-goal.z),n));
            }
        }
        var points=new ArrayList<Vec3>();for(var n=best;n.previous!=null;n=n.previous)points.add(new Vec3(n.x*.5,n.y,n.z*.5));Collections.reverse(points);return points;
    }
    public static boolean step(LivingEntity body,Vec3 target,double speed){
        return advance(body,target,speed,24,false);
    }
    public static boolean walk(LivingEntity body,Vec3 target,double speed){return advance(body,target,speed,10,true);}
    private static boolean advance(LivingEntity body,Vec3 target,double speed,float turn,boolean faceFirst){
        if(!loaded(body,target))return false;var before=body.position();var delta=target.subtract(before);
        if(delta.horizontalDistanceSqr()<.025&&Math.abs(delta.y)<.7){ROUTES.remove(body);return true;}
        Vec3 next=target;
        // Some authored seated originals begin inside their stool's collision.
        // Native motion can step out of that existing overlap; the planner only
        // accepts clear supported cells once the original body has stood up.
        boolean embedded=body.level().getBlockCollisions(body,body.getBoundingBox().deflate(.002)).iterator().hasNext();
        if(!embedded&&!clear(body,before,target)){
            var route=ROUTES.get(body);if(route==null||route.goal.subtract(target).horizontalDistanceSqr()>.56||route.next>=route.points.size()){route=new Route(target,find(body,target));ROUTES.put(body,route);}
            if(route.next>=route.points.size())return false;next=route.points.get(route.next);if(before.distanceToSqr(next)<.035){route.next++;return false;}
        }else ROUTES.remove(body);
        var floor=feet(body,next.x,before.y,next.z);if(floor!=null)next=new Vec3(next.x,floor.y,next.z);
        var d=next.subtract(before);double length=d.horizontalDistance();face(body,next,turn);
        if(faceFirst&&length>.01&&Math.abs(net.minecraft.util.Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(-d.x,d.z))-body.getYRot()))>35){body.setDeltaMovement(Vec3.ZERO);return false;}
        body.setOnGround(true);double dy=net.minecraft.util.Mth.clamp(d.y,-.25,.25);
        if(dy>0)body.move(MoverType.SELF,new Vec3(0,dy,0));
        body.move(MoverType.SELF,new Vec3(length>.001?d.x*Math.min(speed,length)/length:0,dy<=0?dy:0,length>.001?d.z*Math.min(speed,length)/length:0));
        if(body.position().distanceToSqr(before)<.00001)ROUTES.remove(body);
        body.walkAnimation.update((float)body.position().subtract(before).horizontalDistance()*4,.4F);body.setDeltaMovement(Vec3.ZERO);
        return body.position().subtract(target).horizontalDistanceSqr()<.025&&Math.abs(body.getY()-target.y)<.7;
    }
}
