package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.358, the long drive's figure (docs/YARD_FINISHING_0_4_358.md; the owner's "The House Has a Yard": "on a long straight, a silhouette
 * under a distant streetlight that keeps its distance as the reader comes on").
 * <p>
 * Coming along the long straight toward the streetlight, a reader sees someone standing under it, facing them. Walk on and it gives
 * ground, a step back for every step forward, out of the light and round the far bend into the dark; stop and it stops. It never comes
 * nearer. Each reader sees their own, once, and nobody else sees it.
 */
public final class YardFigure {
    private YardFigure(){}
    /** How far ahead along the road it stands; how far off it first shows; the most it gives in a tick; how long it may stand unattended. */
    public static final double GAP=28,SHOW=40,STRIDE=.34;
    public static final int IDLE=400;
    private static final Map<UUID,Figure> FIGURES=new HashMap<>();
    static final class Figure {final UUID actor;double s,lastReader;int idle;Figure(UUID actor,double s,double reader){this.actor=actor;this.s=s;lastReader=reader;}}

    // ------------------------------------------------------------------ the road, by distance along it
    private static double[] lengths(){
        var loop=YardCrossings.LOOP;var out=new double[loop.length];
        for(int i=1;i<loop.length;i++)out[i]=out[i-1]+Math.hypot(loop[i][0]-loop[i-1][0],loop[i][1]-loop[i-1][1]);
        return out;
    }
    /** How far along the loop's centre line the nearest point to (x, z) lies, and how far off the line it is. */
    public static double[] along(double x,double z){
        var loop=YardCrossings.LOOP;var len=lengths();double best=1e9,s=0;
        for(int i=0;i+1<loop.length;i++){
            double ax=loop[i][0]+.5,az=loop[i][1]+.5,dx=loop[i+1][0]-loop[i][0],dz=loop[i+1][1]-loop[i][1],l2=dx*dx+dz*dz;
            double t=Math.max(0,Math.min(1,((x-ax)*dx+(z-az)*dz)/l2)),d=Math.hypot(x-ax-t*dx,z-az-t*dz);
            if(d<best){best=d;s=len[i]+t*Math.sqrt(l2);}
        }
        return new double[]{s,best};
    }
    /** The point on the centre line this far along it. */
    public static Vec3 at(double s){
        var loop=YardCrossings.LOOP;var len=lengths();
        for(int i=0;i+1<loop.length;i++)if(s<=len[i+1]||i+2==loop.length){
            double t=Math.max(0,Math.min(1,(s-len[i])/(len[i+1]-len[i])));
            return new Vec3(loop[i][0]+.5+t*(loop[i+1][0]-loop[i][0]),0,loop[i][1]+.5+t*(loop[i+1][1]-loop[i][1]));
        }
        return Vec3.ZERO;
    }
    /** Under the streetlight; where the straight begins; past the far bend, where it is gone. */
    public static double light(){return along(YardCrossings.STREETLIGHT.getX()+.5,YardCrossings.STREETLIGHT.getZ()+1.5)[0];}
    public static double straight(){return lengths()[2];}
    public static double gone(){return lengths()[5]+8;}

    // ------------------------------------------------------------------ the figure
    public static @Nullable LiteraryActor figure(ServerLevel l,ServerPlayer p){var f=FIGURES.get(p.getUUID());return f==null?null:l.getEntity(f.actor) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    public static double distance(ServerPlayer p){var f=FIGURES.get(p.getUUID());return f==null?-1:f.s-along(p.getX()-base(p).getX(),p.getZ()-base(p).getZ())[0];}
    private static BlockPos base(ServerPlayer p){var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();return origin==null?BlockPos.ZERO:LabyrinthPlaces.base(origin,YardCrossings.LONG_DRIVE);}

    /** Each tick of the long drive, for every reader there. */
    static void tick(ServerLevel l,BlockPos b,net.minecraft.world.level.levelgen.structure.BoundingBox reach,List<ServerPlayer> here,long now){
        var present=new HashSet<UUID>();
        for(var p:here){
            present.add(p.getUUID());
            double[] r=along(p.getX()-b.getX(),p.getZ()-b.getZ());double sr=r[0];var f=FIGURES.get(p.getUUID());
            if(f==null){
                var own=YardCrossings.reader(l,p,YardCrossings.LONG_DRIVE);
                // Once to a reader: coming along the straight toward the light, with the light still a long way off.
                if(own.getBoolean("Figure")||r[1]>2.5||sr<straight()-4||light()-sr>SHOW||light()-sr<GAP)continue;
                var a=spawn(l,b,p,light());if(a==null)continue;
                FIGURES.put(p.getUUID(),new Figure(a.getUUID(),light(),sr));
                own.putBoolean("Figure",true);YardCrossings.saveReader(l,p,YardCrossings.LONG_DRIVE,own);
                continue;
            }
            var a=l.getEntity(f.actor) instanceof LiteraryActor actor&&actor.isAlive()?actor:null;
            if(a==null){FIGURES.remove(p.getUUID());continue;}
            // It gives ground as the reader comes on, never more than a long stride a tick; it never comes back toward them.
            double want=Math.max(f.s,sr+GAP);f.s=Math.min(want,f.s+STRIDE);
            if(Math.abs(sr-f.lastReader)<.02)f.idle++;else f.idle=0;f.lastReader=sr;
            var at=at(f.s);var eye=p.position();
            float yaw=(float)(Math.toDegrees(Math.atan2(eye.z-(b.getZ()+at.z),eye.x-(b.getX()+at.x)))-90);
            a.moveTo(b.getX()+at.x,b.getY(),b.getZ()+at.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
            // Round the far bend and out of sight, or left standing too long behind a reader who turned away: it is gone.
            boolean seen=sees(p,a.getEyePosition());
            if(f.s>=gone()&&!seen||f.s>=gone()+12||sr<f.s-GAP-24&&!seen||f.idle>IDLE&&!seen)end(l,p,f,a);
        }
        // A reader who has left the drive leaves their figure behind; it is gone too.
        for(var it=FIGURES.entrySet().iterator();it.hasNext();){var e=it.next();if(present.contains(e.getKey()))continue;
            if(l.getEntity(e.getValue().actor) instanceof LiteraryActor a)a.discard();it.remove();}
        // A figure saved with the world from before a restart belongs to nobody now.
        if(now%100==0){
            var tracked=new HashSet<UUID>();for(var f:FIGURES.values())tracked.add(f.actor);
            for(var a:l.getEntitiesOfClass(LiteraryActor.class,net.minecraft.world.phys.AABB.of(reach),a->a.role()==LiteraryActor.SILHOUETTE&&a.scene().equals(YardCrossings.LONG_DRIVE.id())))
                if(!tracked.contains(a.getUUID()))a.discard();
        }
    }
    private static void end(ServerLevel l,ServerPlayer p,Figure f,LiteraryActor a){
        a.discard();FIGURES.remove(p.getUUID());
        var own=YardCrossings.reader(l,p,YardCrossings.LONG_DRIVE);own.putBoolean("FigureGone",true);YardCrossings.saveReader(l,p,YardCrossings.LONG_DRIVE,own);
    }
    static @Nullable LiteraryActor spawn(ServerLevel l,BlockPos b,ServerPlayer p,double s){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(YardCrossings.LONG_DRIVE,p.getUUID());a.appearance(LiteraryActor.SILHOUETTE,0);
        var at=at(s);a.moveTo(b.getX()+at.x,b.getY(),b.getZ()+at.z,90,0);
        return l.addFreshEntity(a)?a:null;
    }
    /** Whether the reader has it in view: in front of them and nothing solid between. */
    static boolean sees(ServerPlayer p,Vec3 at){
        var eye=p.getEyePosition();var d=at.subtract(eye);if(d.length()<.5)return true;
        if(p.getLookAngle().dot(d.normalize())<.45)return false;
        return p.level().clip(new ClipContext(eye,at,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
    }
    public static void clearAll(){FIGURES.clear();}
}
