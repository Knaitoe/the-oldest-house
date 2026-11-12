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
    public static boolean cast(int phase){return phase==WITCH;}
    public static String skin(int phase){return "hollow_witch";}
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

    /** One witch on her way across. */
    static final class Pass{
        final int row,side;@Nullable UUID actor;double f,df;boolean committed,over;
        Pass(int row,int side){this.row=row;this.side=side;}
    }
    /** What the specters keep for a night: who is being crossed, which rows each reader has had, and how many witches have gone over each. */
    static final class Flight{
        final Map<UUID,Pass> passes=new HashMap<>();final Map<UUID,Integer> had=new HashMap<>(),over=new HashMap<>();
    }

    /** No specter comes for anybody. For tests of the course that are not about them. */
    public static boolean keptAway;

    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var s=n.specters;if(keptAway)return;
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
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,BikeNightBullies.area(n.base).inflate(0,0,300),BikeNightSpecters::is))if(BikeNight.nightAt(l,a.blockPosition())==n)a.discard();
        n.specters=new Flight();
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** The witch that is crossing for a reader, if one is. */
    public static @Nullable LiteraryActor witch(ServerPlayer p){
        var n=BikeNight.night(p);if(n==null)return null;var pass=n.specters.passes.get(p.getUUID());
        return pass!=null&&pass.actor!=null&&p.serverLevel().getEntity(pass.actor) instanceof LiteraryActor a?a:null;
    }
    /** How many of her rows a reader has had a witch come at, and how many have gone over them. */
    public static int had(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:Integer.bitCount(n.specters.had.getOrDefault(p.getUUID(),0));}
    public static int over(ServerPlayer p){var n=BikeNight.night(p);return n==null?0:n.specters.over.getOrDefault(p.getUUID(),0);}
}
