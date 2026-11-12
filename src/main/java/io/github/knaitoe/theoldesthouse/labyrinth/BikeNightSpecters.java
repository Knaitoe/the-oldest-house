package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.499, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Act three"): the Hollow's specters. The proposal: "Specters chase", and "Being
 * caught by any of them is the Hollow's failure." As answered: the Hollow is one shared course, and each specter is private to
 * whoever it is after, as the House's other private actors are.
 *
 * <p>0.4.503: and what jumps out, which is nobody's own and catches nobody (see {@link Pop}).
 *
 * <p>0.4.500: the ghosts are here too, which catch nobody (see {@link Haunt}). 0.4.501: and the werewolf (see {@link Wolf}). 0.4.502: and the headless rider (see {@link Rider}).
 *
 * <p>The first of them is here: "witches swoop low on brooms across the path, to be ducked or dodged", and "ducking a witch is the
 * hop's crouch, so a witch just before a gap is a choice." A witch crosses the plain way at each of {@link #SWOOPS}: two of them a
 * little short of a brink, one on the vine's top where it is two blocks wide and swinging. She is there for one reader: she comes for them when they are within APPROACH of her row, from one side or the other,
 * high, cackling; drops to a rider's head height as she crosses the way and climbs away on the other side. Until they are within
 * COMMIT of her row she times herself to cross as they reach it, at whatever speed they are making; after that she does not
 * change. Whoever is in her row and in her reach as she crosses, and is not down over their bars (the hop held, {@link #DUCKED}; on
 * foot, sneaking), is caught (BikeNightHollow.taken). Whoever brakes short of her row, or is past it, is not. Each of her rows is
 * crossed once for each reader in a night.
 */
public final class BikeNightSpecters {
    private BikeNightSpecters(){}
    /** The witch's phase among the strangers, and her gesture on her broom. */
    public static final int WITCH=187,FLY=1;
    /** 0.4.500: the ghosts, three of them, on the phases after hers; and how they hang in the air. */
    public static final int GHOST=188,GHOSTS=3,DRIFT=2;
    /** 0.4.501: the werewolf, and what he is doing: running, gathered to leap, in the air. */
    public static final int WOLF=191,RUN=3,CROUCH=4,LEAP=5;
    /** 0.4.502: the headless rider, and his horse going flat out. */
    public static final int RIDER=192,GALLOP=6;
    /** 0.4.503: what jumps out, a ghoul and a goblin; and how they are once they are out, with their arms up. */
    public static final int GHOUL=193,GOBLIN=194,UP=7;
    public static boolean cast(int phase){return phase>=WITCH&&phase<=GOBLIN;}
    public static boolean ghost(int phase){return phase>=GHOST&&phase<GHOST+GHOSTS;}
    public static String skin(int phase){return phase==GHOUL?"hollow_ghoul":phase==GOBLIN?"hollow_goblin":phase==RIDER?"hollow_rider":phase==WOLF?"hollow_wolf":ghost(phase)?"hollow_ghost_"+(char)('a'+phase-GHOST):"hollow_witch";}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    /** A row a witch crosses the plain way on (along its middle): the row, the middle of the way there, and how high its ground is. */
    public record Swoop(int z,double x,double top){}
    /** A little short of the three (which wants the hop held anyway), on the vine's top, and on the top itself. */
    public static final List<Swoop> SWOOPS=swoops(-312,-356,-392);
    private static List<Swoop> swoops(int... rows){
        var out=new ArrayList<Swoop>();
        for(int z:rows)for(var p:CalderHollow.WAY)if(z<=p.zSouth()&&z>=p.zNorth()){out.add(new Swoop(z,(p.x0()+p.x1()+1)/2.0,p.top()));break;}
        return List.copyOf(out);
    }
    /**
     * Blocks: how near her row a reader is when she comes; how near when she stops timing herself to them; how far to the side she
     * comes from and goes to; how high she is there, and as she crosses; and her reach across the way and along it.
     */
    public static final double APPROACH=10,COMMIT=4,FROM=12,HIGH=5,LOW=.55,REACH=.9,ALONG=1;
    /** Ticks: the soonest and the latest she will time her crossing for. And the least of her pass she makes in a tick once she has stopped timing. */
    public static final int SOONEST=3,LATEST=60;public static final double LEAST=.02;
    /** How far down over the bars a rider is before she passes over them, of the whole of the hop's crouch. */
    public static final float DUCKED=.3F;

    /**
     * 0.4.500, the ghosts. "**ghosts** drift through the course and through the riders, chilling their view." Three stretches of
     * the plain way are haunted ({@link #HAUNTS}: the three graves, the grave past the lids, the stairs). While a reader is on one,
     * ghosts come for them one after another, never more than AT_ONCE: each from one side, OUT blocks off and a little ahead of
     * where the reader is, drifting across the way at DRIFT_PACE, a little over the ground, and on out the other side. They are
     * half there (GHOST_FADE), and come and go by thinning. They catch nobody. One that passes within THROUGH of its reader chills
     * their view once: the mist is over them by CHILL_SHARE of a hundred for CHILL ticks (as much as their comfort setting lets
     * through), and they hear it go by.
     */
    public record Haunt(int zSouth,int zNorth){}
    public static final List<Haunt> HAUNTS=List.of(new Haunt(-264,-288),new Haunt(-333,-338),new Haunt(-371,-388));
    public static final int AT_ONCE=2,EVERY=50,CHILL=30,CHILL_SHARE=60,GHOST_FADE=55,THIN=20;
    public static final double OUT=9,AHEAD=5,DRIFT_PACE=.07,OVER_GROUND=.3,THROUGH=1.2;
    /** One ghost on its way across. */
    static final class Drift{
        final UUID actor;final int side;final double z,y;double x;int age;boolean chilled;
        Drift(UUID actor,int side,double x,double y,double z){this.actor=actor;this.side=side;this.x=x;this.y=y;this.z=z;}
    }

    /**
     * 0.4.501, the werewolf. "a **werewolf** runs alongside on a parallel island and leaps across at gaps", and "The werewolf leaps
     * at gaps, so a gap is timed." He has two of the plain way's gaps (CalderHollow.LEAPS). He comes for a reader who is within
     * WOLF_NEAR of one, at the far end of his run beside the way, and runs up it keeping WOLF_LEAD ahead of them as far as his
     * perch beside the gap. From there he leaps across the way, over the gap, to his other perch, and back, every WOLF_EVERY ticks:
     * WOLF_CROUCH ticks gathered, WOLF_AIR in the air. They hear him howl when he takes his perch. Whoever is over the gap, between
     * its brinks, within WOLF_REACH of him as he crosses is caught (BikeNightHollow.taken). Whoever is on the ground is not his,
     * however near. So the gap is crossed just after he lands. He goes when his reader is well past the gap or well back from it.
     */
    public static final double WOLF_NEAR=14,WOLF_LEAD=6,WOLF_PACE=.5,WOLF_REACH=1.3,WOLF_HIGH=2.2,WOLF_PAST=4;
    public static final int WOLF_EVERY=36,WOLF_CROUCH=10,WOLF_AIR=14;
    /** One werewolf at his gap. */
    static final class Wolf{
        final int gap;final UUID actor;int side=-1;double z;boolean perched;long nextLeap,leapAt=-1;
        Wolf(int gap,UUID actor,double z){this.gap=gap;this.actor=actor;this.z=z;}
    }

    /**
     * 0.4.502, the headless rider. "a **headless rider on a headless horse** comes up behind on the last stretch", and "The
     * headless rider on the last stretch is a check on pace." He comes for a reader who rides onto the last stretch (north of
     * RIDER_FROM), RIDER_BEHIND blocks behind them and no further back than the top, and rides after them up the middle of the way
     * at RIDER_PACE: slower than a bike is pedalled, so somebody who keeps going draws away from him, and somebody who stops, or
     * waits too long at the gap, or goes down and is set back, has him come up. His horse does not mind the gap. They hear its
     * hooves, louder as he closes. Whoever he comes within RIDER_REACH of is caught (BikeNightHollow.taken). He pulls up and is gone
     * when his reader is on the far side's ground.
     */
    public static final double RIDER_BEHIND=10,RIDER_PACE=.22,RIDER_REACH=1.2;
    public static final int RIDER_FROM=-396,RIDER_START=-386,HOOVES_EVERY=10;
    /** One rider coming. */
    static final class Rider{
        final UUID actor;double z,y;
        Rider(UUID actor,double z,double y){this.actor=actor;this.z=z;this.y=y;}
    }
    /**
     * 0.4.503, what jumps out. "**Ghouls and goblins** jump out: from behind stones, from under coffin lids and from inside the
     * vine; they are pop-up obstacles in the rider's path, swerved round or hopped over; they never touch anyone, only startle and
     * block." They are the course's, not anybody's: everybody in the Hollow sees the same one come out, and it is in every bike's
     * way. Each has its place on the plain way ({@link #POPS}). One comes out when somebody is coming up to it from the south, on
     * its piece, within POP_NEAR blocks or POP_LEAD ticks' riding, whichever is further, and no nearer than POP_LEAST. It waits
     * POP_WAIT ticks where it hides, is out in POP_OUT (a goblin jumps from behind its stone into the way; a ghoul comes up out of
     * the ground to its chest), stays POP_STAY with its arms up, and goes back in POP_BACK; then that place is quiet for POP_REST.
     * Everybody near hears it come.
     *
     * <p>While it is out it is in the way as a wall is. A bike that is ridden into it (within POP_REACH of it, pointing at it, with
     * its wheels less than POP_CLEAR over the ground there) is stopped: going faster than a bike rides into a wall at, its rider
     * comes off, as off any wall (RiddenBike.struck); slower, it stops dead (RiddenBike.balk) and is kept at the thing's edge. A bike that is POP_CLEAR over the
     * ground as it passes goes over it: a tap of the hop, well timed, or a held one. Somebody on foot it only startles.
     */
    public record Pop(String name,int phase,double x,double z,double top,double fromX,double fromZ,double fromY,double outY,double leap){}
    public static final double POP_NEAR=6,POP_LEAST=1.5,POP_ASIDE=4,POP_REACH=.6,POP_CLEAR=.4;
    public static final int POP_LEAD=24,POP_WAIT=6,POP_OUT=4,POP_STAY=44,POP_BACK=8,POP_REST=200;
    /** How far under the ground a ghoul waits, and how far under it stands once it is out; and how high a goblin's jump is. */
    public static final double GHOUL_UNDER=2,GHOUL_OUT=.95,GOBLIN_LEAP=.5;
    /**
     * Where they are: a goblin behind the second grave's cross, and one behind a stone on the third; a ghoul that comes up between
     * the second lid and the third, where there is no going round; a goblin behind a stone on the grave past the lids; and two
     * ghouls in the vine, on its first level rows and on its last.
     */
    public static final List<Pop> POPS=List.of(
        goblin("the goblin behind the cross",-1,-275,2.2,-275.5),goblin("the goblin on the third grave",-4,-284,-1.5,-284.5),
        new Pop("the ghoul between the lids",GHOUL,0,-322.5,0,0,-322.5,-GHOUL_UNDER,-GHOUL_OUT,0),
        goblin("the goblin past the lids",-3,-335,-.3,-335.5),ghoul("the ghoul in the vine",-346),ghoul("the vine's second ghoul",-365));
    /** A goblin: the stone it is behind (on its north side, as a rider comes), and where in the way it jumps to. */
    private static Pop goblin(String name,int stoneX,int stoneZ,double x,double z){
        double top=CalderHollow.top(Mth.floor(x),Mth.floor(z));
        return new Pop(name,GOBLIN,x,z,top,stoneX+.5,stoneZ+.1,top,top,GOBLIN_LEAP);
    }
    /** A ghoul in the middle of the plain way's piece at a row. */
    private static Pop ghoul(String name,int row){
        for(var p:CalderHollow.WAY)if(row<=p.zSouth()&&row>=p.zNorth()){double x=(p.x0()+p.x1()+1)/2.0;return new Pop(name,GHOUL,x,row+.5,p.top(),x,row+.5,p.top()-GHOUL_UNDER,p.top()-GHOUL_OUT,0);}
        throw new IllegalStateException("no piece of the plain way at row "+row);
    }
    /** One of them out: its actor, who it came for, when, and who has been over it and who it has stopped. */
    static final class Sprung{
        final UUID actor,reader;final long at;final java.util.Set<UUID> over=new java.util.HashSet<>(),stopped=new java.util.HashSet<>();
        Sprung(UUID actor,UUID reader,long at){this.actor=actor;this.reader=reader;this.at=at;}
    }
    /** Which of the specters come at all, or null for all of them: "witch", "ghosts", "wolf", "rider", "pops". For tests of one of them. */
    public static @Nullable java.util.Set<String> only;
    private static boolean comes(String which){return only==null||only.contains(which);}

    /** One witch on her way across. */
    static final class Pass{
        final int row,side;@Nullable UUID actor;double f,df;boolean committed,over;
        Pass(int row,int side){this.row=row;this.side=side;}
    }
    /** What the specters keep for a night: who is being crossed, which rows each reader has had, and how many witches have gone over each. */
    static final class Flight{
        final Map<UUID,Pass> passes=new HashMap<>();final Map<UUID,Integer> had=new HashMap<>(),over=new HashMap<>();
        /** 0.4.500: each reader's ghosts, when the next may come, and how many have gone through them. */
        /** 0.4.501: each reader's werewolf, which gaps they have had him at, and how many times he has leapt for them. */
        /** 0.4.502: each reader's rider, and who has had him. */
        final Map<UUID,Rider> riders=new HashMap<>();final java.util.Set<UUID> ridden=new java.util.HashSet<>();
        /** 0.4.503: which of what jumps out is out, when each place may next have one, and how many each reader has been stopped by and gone over. */
        final Map<Integer,Sprung> sprung=new HashMap<>();final Map<Integer,Long> rest=new HashMap<>();final Map<UUID,Integer> stopped=new HashMap<>(),hopped=new HashMap<>();
        final Map<UUID,Wolf> wolves=new HashMap<>();final Map<UUID,Integer> wolfHad=new HashMap<>(),leaps=new HashMap<>();
        final Map<UUID,List<Drift>> drifts=new HashMap<>();final Map<UUID,Long> nextGhost=new HashMap<>();final Map<UUID,Integer> chills=new HashMap<>(),ghosts=new HashMap<>();
    }

    /** No specter comes for anybody. For tests of the course that are not about them. */
    public static boolean keptAway;

    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var s=n.specters;if(keptAway)return;
        if(comes("pops"))spring(l,n,here);
        if(comes("ghosts"))haunt(l,n,here);
        if(comes("wolf"))prowl(l,n,here);
        if(comes("rider"))chase(l,n,here);
        if(!comes("witch"))return;
        for(var p:here){
            var id=p.getUUID();
            if(!p.isAlive()||p.isSpectator()||n.hollow.taken.contains(id)||n.hollow.through.contains(id)){end(l,s,id);continue;}
            Entity body=p.getVehicle() instanceof NightBike bike?bike:p;var at=BikeNightHollow.from(n,body.position());
            var pass=s.passes.get(id);
            if(pass!=null){cross(l,n,p,pass,body,at);continue;}
            int had=s.had.getOrDefault(id,0);
            for(int i=0;i<SWOOPS.size();i++){
                var w=SWOOPS.get(i);double ahead=at.z-w.z()-.5;
                if((had>>i&1)!=0||ahead<=0||ahead>APPROACH||Math.abs(at.x-w.x())>4||Math.abs(at.y-w.top())>1.5)continue;
                s.had.put(id,had|1<<i);begin(l,n,p,s,i);break;
            }
        }
        // A witch whose reader has gone is gone.
        for(var id:List.copyOf(s.passes.keySet())){boolean there=false;for(var p:here)there|=p.getUUID().equals(id);if(!there)end(l,s,id);}
    }
    /** What jumps out: coming out for whoever is coming, in every bike's way while it is out, going back. */
    private static void spring(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var s=n.specters;double bx=n.base.getX(),by=n.base.getY(),bz=n.base.getZ();
        for(int i=0;i<POPS.size();i++){
            var pop=POPS.get(i);var up=s.sprung.get(i);
            if(up==null){
                if(n.tick<s.rest.getOrDefault(i,0L))continue;
                for(var p:here){
                    var id=p.getUUID();if(!p.isAlive()||p.isSpectator()||n.hollow.taken.contains(id)||n.hollow.through.contains(id))continue;
                    Entity body=p.getVehicle() instanceof NightBike bike?bike:p;var at=BikeNightHollow.from(n,body.position());
                    double speed=body instanceof RiddenBike bike?bike.speedNow():.15,ahead=at.z-pop.z();
                    if(ahead<POP_LEAST||ahead>Math.max(POP_NEAR,speed*POP_LEAD)||Math.abs(at.x-pop.x())>POP_ASIDE||Math.abs(at.y-pop.top())>1.5)continue;
                    var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)break;
                    a.bind(BikeNight.PLACE,null);a.appearance(LiteraryActor.STRANGER,pop.phase());a.gesture(0);a.setNoGravity(true);a.noPhysics=true;
                    a.moveTo(bx+pop.fromX(),by+pop.fromY(),bz+pop.fromZ(),0,0);a.setYHeadRot(0);a.setYBodyRot(0);
                    if(l.addFreshEntity(a))s.sprung.put(i,new Sprung(a.getUUID(),id,n.tick));
                    break;
                }
                continue;
            }
            var a=l.getEntity(up.actor) instanceof LiteraryActor found?found:null;long age=n.tick-up.at;int out=POP_WAIT+POP_OUT,back=out+POP_STAY;
            if(a==null||age>=back+POP_BACK){
                if(a!=null)a.discard();s.sprung.remove(i);s.rest.put(i,n.tick+POP_REST);
                for(var id:up.over)if(!up.stopped.contains(id))s.hopped.merge(id,1,Integer::sum);
                continue;
            }
            // How far out it is: nothing where it hides, the whole of it in the way.
            double f=age<POP_WAIT?0:age<out?(age-POP_WAIT+1)/(double)POP_OUT:age<back?1:1-(age-back+1)/(double)POP_BACK;
            double x=Mth.lerp(f,pop.fromX(),pop.x()),z=Mth.lerp(f,pop.fromZ(),pop.z()),y=Mth.lerp(f,pop.fromY(),pop.outY())+pop.leap()*4*f*(1-f);
            // It looks at whoever it came for, while they are there to be looked at.
            float yaw=a.getYRot();for(var p:here)if(p.getUUID().equals(up.reader))yaw=(float)Math.toDegrees(Math.atan2(-(p.getX()-bx-x),p.getZ()-bz-z));
            a.gesture(f>=1?UP:0);a.moveTo(bx+x,by+y,bz+z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
            if(age==POP_WAIT)l.playSound(null,bx+pop.x(),by+pop.top()+.5,bz+pop.z(),BikeNightRegistry.POP.get(),SoundSource.HOSTILE,1F,(pop.phase()==GOBLIN?1.25F:.8F)+l.random.nextFloat()*.1F);
            if(f<1)continue;
            for(var p:here){
                if(!(p.getVehicle() instanceof NightBike bike)||bike.getFirstPassenger()!=p)continue;
                var at=BikeNightHollow.from(n,bike.position());double dx=pop.x()-at.x,dz=pop.z()-at.z;
                if(dx*dx+dz*dz>=POP_REACH*POP_REACH||at.y<pop.top()-1)continue;
                if(at.y-pop.top()>=POP_CLEAR){up.over.add(p.getUUID());continue;}
                // Under that, and pointing at it: it is in the way.
                float turned=bike.getYRot()*Mth.DEG_TO_RAD;if(-Mth.sin(turned)*dx+Mth.cos(turned)*dz<=0)continue;
                if(up.stopped.add(p.getUUID()))s.stopped.merge(p.getUUID(),1,Integer::sum);
                if(bike.struck())continue;
                // Too slow to come off at: stopped, and kept at its edge for as long as they push at it.
                bike.balk();double off=Math.sqrt(dx*dx+dz*dz);
                if(off>1e-3)bike.setPos(bx+pop.x()-dx/off*POP_REACH,bike.getY(),bz+pop.z()-dz/off*POP_REACH);
            }
        }
    }
    /** The headless rider: coming up behind whoever is on the last stretch. */
    private static void chase(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var s=n.specters;
        for(var p:here){
            var id=p.getUUID();boolean out=!p.isAlive()||p.isSpectator()||n.hollow.taken.contains(id)||n.hollow.through.contains(id);
            Entity body=p.getVehicle() instanceof NightBike bike?bike:p;var at=BikeNightHollow.from(n,body.position());
            var r=s.riders.get(id);
            if(r==null){
                double top=CalderHollow.top(Mth.floor(at.x),Mth.floor(at.z));
                if(out||s.ridden.contains(id)||at.z>=RIDER_FROM||at.z<=CalderHollow.FAR_SIDE+1||Double.isNaN(top)||Math.abs(at.y-top)>1.5||Math.abs(at.x-.5)>3)continue;
                double z=Math.min(RIDER_START,at.z+RIDER_BEHIND),y=height(z,top);
                var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(BikeNight.PLACE,id);a.appearance(LiteraryActor.STRANGER,RIDER);a.gesture(GALLOP);a.setNoGravity(true);a.noPhysics=true;
                a.moveTo(n.base.getX()+.5,n.base.getY()+y,n.base.getZ()+z,180,0);a.setYHeadRot(180);a.setYBodyRot(180);
                if(l.addFreshEntity(a)){s.riders.put(id,new Rider(a.getUUID(),z,y));s.ridden.add(id);}
                continue;
            }
            var a=l.getEntity(r.actor) instanceof LiteraryActor found?found:null;
            // He pulls up when they are on the far side's ground, or he is.
            if(a==null||out||at.z<=CalderHollow.FAR_SIDE+1||r.z<=CalderHollow.FAR_SIDE+2){if(a!=null)a.discard();s.riders.remove(id);continue;}
            r.z-=RIDER_PACE;r.y=height(r.z,r.y);
            a.moveTo(n.base.getX()+.5,n.base.getY()+r.y,n.base.getZ()+r.z,180,0);a.setYHeadRot(180);a.setYBodyRot(180);
            double off=Math.abs(at.z-r.z);
            if(n.tick%HOOVES_EVERY==0)p.playNotifySound(BikeNightRegistry.HOOVES.get(),SoundSource.HOSTILE,(float)Mth.clamp(1.2-off/14,.15,1),1F);
            if(off<RIDER_REACH&&Math.abs(at.x-.5)<2.5&&Math.abs(at.y-r.y)<3){a.discard();s.riders.remove(id);BikeNightHollow.taken(l,n,p,"rider");}
        }
        for(var id:List.copyOf(s.riders.keySet())){boolean there=false;for(var p:here)there|=p.getUUID().equals(id);if(!there){if(l.getEntity(s.riders.get(id).actor) instanceof LiteraryActor a)a.discard();s.riders.remove(id);}}
    }
    /** How high the plain way is at a row on the gate's line; over a gap, what it was. */
    private static double height(double z,double last){double top=CalderHollow.top(0,Mth.floor(z));return Double.isNaN(top)?last:top;}
    /** The werewolf: coming, running up beside his reader, leaping the gap on his rhythm, going. */
    private static void prowl(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var s=n.specters;
        for(var p:here){
            var id=p.getUUID();boolean out=!p.isAlive()||p.isSpectator()||n.hollow.taken.contains(id)||n.hollow.through.contains(id);
            Entity body=p.getVehicle() instanceof NightBike bike?bike:p;var at=BikeNightHollow.from(n,body.position());
            var w=s.wolves.get(id);
            if(w==null){
                if(out)continue;int had=s.wolfHad.getOrDefault(id,0);
                for(int i=0;i<CalderHollow.LEAPS.size();i++){
                    var g=CalderHollow.LEAPS.get(i);double ahead=at.z-g.south();
                    if((had>>i&1)!=0||ahead<=0||ahead>WOLF_NEAR||Math.abs(at.x-g.x())>5||Math.abs(at.y-g.top())>2.5)continue;
                    var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)break;
                    a.bind(BikeNight.PLACE,id);a.appearance(LiteraryActor.STRANGER,WOLF);a.gesture(RUN);a.setNoGravity(true);a.noPhysics=true;
                    a.moveTo(n.base.getX()+g.west()-.5,n.base.getY()+g.top(),n.base.getZ()+g.run(),180,0);a.setYHeadRot(180);a.setYBodyRot(180);
                    if(l.addFreshEntity(a)){s.wolves.put(id,new Wolf(i,a.getUUID(),g.run()));s.wolfHad.put(id,had|1<<i);}
                    break;
                }
                continue;
            }
            var g=CalderHollow.LEAPS.get(w.gap);var a=l.getEntity(w.actor) instanceof LiteraryActor found?found:null;
            if(a==null||out||at.z<g.north()-WOLF_PAST||at.z>g.south()+WOLF_NEAR+6){if(a!=null)a.discard();s.wolves.remove(id);continue;}
            double bx=n.base.getX(),by=n.base.getY(),bz=n.base.getZ();
            if(!w.perched){
                // Up his run, ahead of them, as far as his perch.
                double to=Math.min(g.run(),Math.max(g.z(),at.z-WOLF_LEAD));
                w.z+=Mth.clamp(to-w.z,-WOLF_PACE,WOLF_PACE);boolean moving=Math.abs(to-w.z)>.01;
                a.gesture(moving?RUN:CROUCH);a.moveTo(bx+g.west()-.5,by+g.top(),bz+w.z,180,0);a.setYHeadRot(180);a.setYBodyRot(180);
                if(Math.abs(w.z-g.z())<.6){
                    w.perched=true;w.z=g.z();w.nextLeap=n.tick+WOLF_CROUCH*2;
                    p.playNotifySound(BikeNightRegistry.HOWL.get(),SoundSource.HOSTILE,1F,.95F+l.random.nextFloat()*.1F);
                }
                continue;
            }
            double from=w.side<0?g.west():g.east(),over=w.side<0?g.east():g.west();float yaw=w.side<0?-90:90;
            if(w.leapAt<0){
                // On his perch, facing the way; gathered before he goes.
                a.gesture(n.tick>=w.nextLeap-WOLF_CROUCH?CROUCH:RUN);a.moveTo(bx+from,by+g.top(),bz+g.z(),yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
                if(n.tick>=w.nextLeap){w.leapAt=n.tick;a.gesture(LEAP);}
                continue;
            }
            double f=(n.tick-w.leapAt)/(double)WOLF_AIR;
            if(f>=1){w.side=-w.side;w.leapAt=-1;w.nextLeap=n.tick+WOLF_EVERY;s.leaps.merge(id,1,Integer::sum);a.gesture(RUN);a.moveTo(bx+over,by+g.top(),bz+g.z(),-yaw,0);a.setYHeadRot(-yaw);a.setYBodyRot(-yaw);continue;}
            double x=Mth.lerp(f,from,over),y=g.top()+WOLF_HIGH*4*f*(1-f);
            a.moveTo(bx+x,by+y,bz+g.z(),yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
            // Over the gap, between its brinks, in his reach: theirs is the one time a gap costs everything.
            if(at.z<g.south()&&at.z>g.north()&&Math.abs(at.x-x)<WOLF_REACH&&Math.abs(at.y-g.top())<3){a.discard();s.wolves.remove(id);BikeNightHollow.taken(l,n,p,"wolf");}
        }
        for(var id:List.copyOf(s.wolves.keySet())){boolean there=false;for(var p:here)there|=p.getUUID().equals(id);if(!there){if(l.getEntity(s.wolves.get(id).actor) instanceof LiteraryActor a)a.discard();s.wolves.remove(id);}}
    }
    /** The ghosts: coming for whoever is on a haunted stretch, drifting, chilling, going. */
    private static void haunt(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var s=n.specters;
        for(var p:here){
            var id=p.getUUID();var mine=s.drifts.computeIfAbsent(id,k->new ArrayList<>());
            boolean out=!p.isAlive()||p.isSpectator()||n.hollow.taken.contains(id)||n.hollow.through.contains(id);
            Entity body=p.getVehicle() instanceof NightBike bike?bike:p;var at=BikeNightHollow.from(n,body.position());
            // Another one, if they are on a haunted stretch and it is time.
            if(!out&&mine.size()<AT_ONCE&&n.tick>=s.nextGhost.getOrDefault(id,0L))for(var h:HAUNTS){
                if(at.z>h.zSouth()+1||at.z<h.zNorth()||Math.abs(at.x)>8)continue;
                double top=CalderHollow.top(Mth.floor(at.x),Mth.floor(at.z));if(Double.isNaN(top)||Math.abs(at.y-top)>1.5)continue;
                int side=l.random.nextBoolean()?1:-1,which=s.ghosts.merge(id,1,Integer::sum)-1;
                var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)break;
                a.bind(BikeNight.PLACE,id);a.appearance(LiteraryActor.STRANGER,GHOST+which%GHOSTS);a.gesture(DRIFT);a.setNoGravity(true);a.noPhysics=true;a.fade(100);
                // Over the ground where it crosses the way, which may not be the height its reader is at.
                double z=at.z-AHEAD-l.random.nextDouble()*3,there=CalderHollow.top(Mth.floor(at.x),Mth.floor(z));
                var d=new Drift(a.getUUID(),side,at.x+side*OUT,(Double.isNaN(there)?top:there)+OVER_GROUND,z);
                float yaw=side>0?90:-90;a.moveTo(n.base.getX()+d.x,n.base.getY()+d.y,n.base.getZ()+d.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
                if(l.addFreshEntity(a)){mine.add(d);s.nextGhost.put(id,n.tick+EVERY);}
                break;
            }
            for(var it=mine.iterator();it.hasNext();){
                var d=it.next();d.age++;d.x-=d.side*DRIFT_PACE;
                int life=(int)Math.ceil(2*OUT/DRIFT_PACE);
                var a=l.getEntity(d.actor) instanceof LiteraryActor found?found:null;
                if(a==null||out||d.age>=life){if(a!=null)a.discard();it.remove();continue;}
                float yaw=d.side>0?90:-90;a.moveTo(n.base.getX()+d.x,n.base.getY()+d.y+Math.sin(d.age*.08)*.08,n.base.getZ()+d.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
                // Thinning in and out.
                int edge=Math.min(d.age,life-d.age);a.fade(edge>=THIN?GHOST_FADE:100-(100-GHOST_FADE)*edge/THIN);
                if(!d.chilled&&new Vec3(d.x,d.y+.8,d.z).distanceTo(at.add(0,.8,0))<THROUGH){
                    d.chilled=true;s.chills.merge(id,1,Integer::sum);
                    io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.BikeNightMistPayload(BikeNightGraveyard.mist(l,n.base,p),CHILL,CHILL_SHARE));
                    p.playNotifySound(BikeNightRegistry.GHOST_BY.get(),SoundSource.HOSTILE,.8F,.9F+l.random.nextFloat()*.2F);
                }
            }
        }
        for(var id:List.copyOf(s.drifts.keySet())){boolean there=false;for(var p:here)there|=p.getUUID().equals(id);if(!there)clear(l,s,id);}
    }
    private static void clear(ServerLevel l,Flight s,UUID reader){
        var mine=s.drifts.remove(reader);if(mine==null)return;
        for(var d:mine)if(l.getEntity(d.actor) instanceof LiteraryActor a)a.discard();
    }
    private static Vec3 where(BikeNight.Night n,Swoop w,Pass pass){
        double x=w.x()+pass.side*FROM*(1-2*pass.f),y=w.top()+LOW+(HIGH-LOW)*Math.pow(2*pass.f-1,2);
        return new Vec3(n.base.getX()+x,n.base.getY()+y,n.base.getZ()+w.z()+.5);
    }
    private static void begin(ServerLevel l,BikeNight.Night n,ServerPlayer p,Flight s,int row){
        var pass=new Pass(row,l.random.nextBoolean()?1:-1);var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(BikeNight.PLACE,p.getUUID());a.appearance(LiteraryActor.STRANGER,WITCH);a.gesture(FLY);a.setNoGravity(true);a.noPhysics=true;
        var at=where(n,SWOOPS.get(row),pass);float yaw=pass.side>0?90:-90;
        a.moveTo(at.x,at.y,at.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);
        if(!l.addFreshEntity(a))return;
        pass.actor=a.getUUID();s.passes.put(p.getUUID(),pass);
        p.playNotifySound(BikeNightRegistry.CACKLE.get(),SoundSource.HOSTILE,1F,.95F+l.random.nextFloat()*.1F);
    }
    private static void cross(ServerLevel l,BikeNight.Night n,ServerPlayer p,Pass pass,Entity body,Vec3 at){
        var w=SWOOPS.get(pass.row);double ahead=at.z-w.z()-.5;
        if(!pass.committed){
            if(pass.f>=.5||ahead<=COMMIT){pass.committed=true;pass.df=Math.max(LEAST,pass.df);}
            else{
                double speed=body instanceof RiddenBike bike?bike.speedNow():.15;
                pass.df=(.5-pass.f)/Mth.clamp(ahead/Math.max(speed,.01),SOONEST,LATEST);
            }
        }
        pass.f=Math.min(1,pass.f+pass.df);
        var to=where(n,w,pass);float yaw=pass.side>0?90:-90;
        if(pass.actor!=null&&l.getEntity(pass.actor) instanceof LiteraryActor a){a.moveTo(to.x,to.y,to.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);}
        // In her row and in her reach as she crosses: over them if they are down, and otherwise they are hers.
        if(Math.abs(to.x-n.base.getX()-at.x)<REACH&&Math.abs(ahead)<ALONG&&Math.abs(at.y-w.top())<1.2){
            boolean down=body instanceof RiddenBike bike?bike.crouch()>DUCKED:p.isShiftKeyDown();
            if(!down){end(l,n.specters,p.getUUID());BikeNightHollow.taken(l,n,p,"witch");return;}
            if(!pass.over){pass.over=true;n.specters.over.merge(p.getUUID(),1,Integer::sum);}
        }
        if(pass.f>=1)end(l,n.specters,p.getUUID());
    }
    private static void end(ServerLevel l,Flight s,UUID reader){
        var pass=s.passes.remove(reader);
        if(pass!=null&&pass.actor!=null&&l.getEntity(pass.actor) instanceof LiteraryActor a)a.discard();
    }
    static void reset(ServerLevel l,BikeNight.Night n){
        for(var id:List.copyOf(n.specters.passes.keySet()))end(l,n.specters,id);
        for(var id:List.copyOf(n.specters.drifts.keySet()))clear(l,n.specters,id);
        for(var w:n.specters.wolves.values())if(l.getEntity(w.actor) instanceof LiteraryActor a)a.discard();
        for(var r:n.specters.riders.values())if(l.getEntity(r.actor) instanceof LiteraryActor a)a.discard();
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,BikeNightBullies.area(n.base).inflate(0,0,300),BikeNightSpecters::is))if(BikeNight.nightAt(l,a.blockPosition())==n)a.discard();
        n.specters=new Flight();
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** What has jumped out at one of the places, if something is out there; and how many a reader has been stopped by, and gone over. */
    public static @Nullable LiteraryActor sprung(ServerLevel l,BlockPos base,int place){
        var n=BikeNight.nightAt(l,base);if(n==null)return null;var up=n.specters.sprung.get(place);
        return up!=null&&l.getEntity(up.actor) instanceof LiteraryActor a?a:null;
    }
    public static int stopped(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.specters.stopped.getOrDefault(p.getUUID(),0);}
    public static int hopped(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.specters.hopped.getOrDefault(p.getUUID(),0);}
    /** The witch that is crossing for a reader, if one is. */
    public static @Nullable LiteraryActor witch(ServerPlayer p){
        var n=BikeNight.night(p);if(n==null)return null;var pass=n.specters.passes.get(p.getUUID());
        return pass!=null&&pass.actor!=null&&p.serverLevel().getEntity(pass.actor) instanceof LiteraryActor a?a:null;
    }
    /** The headless rider that is after a reader, if one is. */
    public static @Nullable LiteraryActor rider(ServerPlayer p){
        var n=BikeNight.night(p);if(n==null)return null;var r=n.specters.riders.get(p.getUUID());
        return r!=null&&p.serverLevel().getEntity(r.actor) instanceof LiteraryActor a?a:null;
    }
    /** The werewolf that is at a gap for a reader, if one is; and how many times he has leapt for them tonight. */
    public static @Nullable LiteraryActor wolf(ServerPlayer p){
        var n=BikeNight.night(p);if(n==null)return null;var w=n.specters.wolves.get(p.getUUID());
        return w!=null&&p.serverLevel().getEntity(w.actor) instanceof LiteraryActor a?a:null;
    }
    public static int leaps(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.specters.leaps.getOrDefault(p.getUUID(),0);}
    /** The ghosts that are drifting for a reader, how many have come for them tonight, and how many have gone through them. */
    public static List<LiteraryActor> ghosts(ServerPlayer p){
        var out=new ArrayList<LiteraryActor>();var n=BikeNight.night(p);if(n==null)return out;
        for(var d:n.specters.drifts.getOrDefault(p.getUUID(),List.of()))if(p.serverLevel().getEntity(d.actor) instanceof LiteraryActor a)out.add(a);
        return out;
    }
    public static int ghostsCome(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.specters.ghosts.getOrDefault(p.getUUID(),0);}
    public static int chills(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.specters.chills.getOrDefault(p.getUUID(),0);}
    /** How many of her rows a reader has had a witch come at, and how many have gone over them. */
    public static int had(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:Integer.bitCount(n.specters.had.getOrDefault(p.getUUID(),0));}
    public static int over(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.specters.over.getOrDefault(p.getUUID(),0);}
}
