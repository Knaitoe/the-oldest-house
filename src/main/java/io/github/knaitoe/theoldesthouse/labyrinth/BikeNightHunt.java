package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.Bully;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.Pack;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.482, Bike night: the hunt through the town. The proposal: "they split up. One stays on the readers' tail, and the others
 * take the town's back ways to head them off at a corner", and "Caught: a bully within arm's reach of a rider for two seconds
 * has caught them."
 *
 * <p><b>How each rides.</b> At a reader he can see, straight. At one he cannot, along the town's ways (CalderRoutes), thinking
 * again about his way once a second. The first after a reader is on their tail: he makes for the end of the way they are on
 * that is nearer them. The second makes for the end they are riding toward (or, if they are standing, its far end), and the
 * third for the place past that, each by a way that does not go through the reader, and waits there. Nobody heads a reader off
 * to the north: that way is left open, and it ends at the cemetery's gate.
 *
 * <p><b>A reader behind something.</b> The one on their tail does not wait at the end of their way for them to come out. From
 * there he goes in after them, round whatever is between, for ten seconds; then he goes back to the corner for five, and comes
 * again. The others keep their corners.
 *
 * <p><b>Caught.</b> "He takes the bike, the rider is shoved into the leaves, and the bullies ride off laughing with it." A
 * reader within reach of one of them for two seconds, on a bike or off it. Nobody is hurt. The bike goes with him: the one
 * under the reader, or the one they left standing. The one who has it rides off with it at once; the others go on after
 * whoever is left, and when nobody is, they go too.
 *
 * <p><b>Out.</b> "In co-op, the reader is not out yet: the Hopper can carry them on its pegs. Only a reader nobody carries is
 * out." A reader who has been caught is out when nobody is left to carry them, or when half a minute has gone by and nobody
 * has. What being out closes to them comes with the endings.
 */
public final class BikeNightHunt {
    private BikeNightHunt(){}
    /** In ticks: how often one of them thinks again about his way; how long within reach is caught; how long a caught reader has to be taken up; how long one of them may take to be gone. */
    public static final int THINK=20,CAUGHT=40,AFOOT=600,GONE_MOST=600;
    /** In ticks: how long the one on a reader's tail goes in after a reader he cannot see before he gives it up, and how long he then lets it be. */
    public static final int CLOSE_MOST=200,REST=100;
    /** In blocks: how far off one of them rides straight at a reader he can see; arm's reach; how near a place counts as there, and how near on a tight way; how far a reader must have come in half a second to be going somewhere. */
    public static final double SIGHT=14,REACH=1.7,AT_PLACE=1.6,AT_TIGHT=.8,GOING=1;
    /** How much further north than a reader a place must be for none of them to head the reader off there, in blocks. */
    public static final double NORTH_OPEN=4;
    /** Blocks a tick along a tight way. */
    static final float TIGHT=.2F;
    /** Where they go when they have what they came for: the far ends of the town. */
    static final String[] AWAY={"park_west","park_east","west_church","east_church"};

    /**
     * What somebody riding the town's ways has in mind: the way, as places, and how far along it he is; where it ends; when he thinks
     * again; until when he cannot ride straight at anybody; which side he is going round something, and until when; and whether he is
     * going in after a reader he cannot see from the end of their way, from which place, until when, and until when he lets it be.
     * The three have one each (BikeNightBullies.Bully), and Gus has one (0.4.483).
     */
    static class Rider {
        @Nullable int[] plan;int step,aim=-1,around=1;long planAt,blindUntil=-1,aroundUntil=-1;
        boolean closing;int closeFrom=-1;long closeBy=-1,restUntil=-1;
    }

    /** Whether somewhere is in the town, north of the park's own street: the hunt is there, and the park keeps the chase it had. */
    static boolean inTown(BikeNight.Night n,Vec3 at){return at.z<n.base.getZ()+BikeNightSite.LINE-4.5;}
    static double dot(Vec3 a,Vec3 b){return a.x*b.x+a.z*b.z;}

    // ------------------------------------------------------------------------------------------------ where a reader is going
    /** Each reader's last half second, a place a tick, for which way they are going. */
    static void trail(BikeNight.Night n,Pack k,List<ServerPlayer> prey){
        for(var p:prey){var t=k.trail.computeIfAbsent(p.getUUID(),id->new ArrayDeque<>());t.addLast(p.position());while(t.size()>10)t.removeFirst();}
    }
    /** Which way a reader is going, as a direction along the ground; or null if they are not going anywhere much. */
    static @Nullable Vec3 going(Pack k,ServerPlayer p){
        var t=k.trail.get(p.getUUID());if(t==null||t.size()<10)return null;
        var d=p.position().subtract(t.peekFirst());d=new Vec3(d.x,0,d.z);
        return d.lengthSqr()<GOING*GOING?null:d.normalize();
    }

    // ------------------------------------------------------------------------------------------------ riding
    /** One of them after a reader, this tick: {@code part} is 0 for the one on their tail, 1 and 2 for those who head them off. */
    static void ride(ServerLevel l,BikeNight.Night n,Pack k,Bully u,PackBike bike,ServerPlayer prey,int part){
        var here=bike.position();var there=prey.position();
        double d=Math.sqrt(BikeNightGus.flat(here,there));boolean level=Math.abs(here.y-there.y)<1.2;
        // Up to them he slows, to stay beside them: he wants the bike, and is not trying to ride through it.
        float pace=(float)Math.min(BikeNightBullies.HUNT,Math.max(.1,d*.16));
        // Something has stopped him riding straight at them: for a while he goes by the ways instead.
        if(bike.heldUp()&&u.plan==null&&!u.closing)u.blindUntil=n.tick+60;
        if(n.tick>=u.blindUntil&&d<SIGHT&&level&&clear(l,here,there)){u.plan=null;u.aim=-1;u.closing=false;bike.toward(there,pace);return;}
        if(u.closing){
            // Going in after a reader behind something. He gives it up when they are on another way, or when it has got him nowhere.
            boolean late=n.tick>=u.closeBy;
            if(late||!level||part!=0||n.tick%THINK==0&&nearEnd(n,there)!=u.closeFrom){u.closing=false;if(late)u.restUntil=n.tick+REST;}
            else{
                var to=there;
                if(bike.heldUp()&&n.tick>=u.aroundUntil){u.aroundUntil=n.tick+24;u.around=-u.around;}
                if(n.tick<u.aroundUntil){double rad=Math.toRadians(bike.getYRot()+u.around*70);to=here.add(-Math.sin(rad)*3,0,Math.cos(rad)*3);}
                bike.toward(to,pace);return;
            }
        }
        if(u.plan==null||u.step>=u.plan.length||n.tick>=u.planAt)plan(l,n,k,u,bike,prey,part);
        boolean arrived=follow(l,n,u,bike);
        // At the end of the reader's way, and they are still not in plain sight: the one on their tail goes in after them.
        if(arrived&&part==0&&level&&n.tick>=u.restUntil&&u.aim==nearEnd(n,there)){u.closing=true;u.closeFrom=u.aim;u.closeBy=n.tick+CLOSE_MOST;u.plan=null;u.aim=-1;}
    }
    /** The end of the way a reader is on that is nearer them. */
    static int nearEnd(BikeNight.Night n,Vec3 at){
        var way=CalderRoutes.nearestWay(n.base,at);
        return BikeNightGus.flat(at,CalderRoutes.at(n.base,way.a()))<=BikeNightGus.flat(at,CalderRoutes.at(n.base,way.b()))?way.a():way.b();
    }
    /** Where one of them is on the town's ways: the gate's inner place if he is still in the park, or the nearest place he can ride straight to. */
    static int start(ServerLevel l,BikeNight.Night n,PackBike bike){
        var b=n.base;var here=bike.position();
        if(here.z>b.getZ()+BikeNightSite.NORTH+.5)return CalderRoutes.id("gate_in");
        int best=CalderRoutes.nearest(b,here);double least=Double.MAX_VALUE;boolean any=false;
        for(int i=0;i<CalderRoutes.places();i++){
            var at=CalderRoutes.at(b,i);double d=BikeNightGus.flat(here,at);
            if(d>=least||d>24*24||Math.abs(at.y-here.y)>.6||!clear(l,here,at))continue;
            least=d;best=i;any=true;
        }
        return any?best:CalderRoutes.nearest(b,here);
    }
    /** Thinks again: where his part sends him, and the way there. */
    static void plan(ServerLevel l,BikeNight.Night n,Pack k,Rider u,PackBike bike,ServerPlayer prey,int part){
        var b=n.base;u.planAt=n.tick+THINK;
        boolean under=u.plan!=null&&u.step<u.plan.length;
        int from=under?u.plan[u.step]:start(l,n,bike),before=under&&u.step>0?u.plan[u.step-1]:-1;
        var at=prey.position();var way=CalderRoutes.nearestWay(b,at);
        var pa=CalderRoutes.at(b,way.a());var pb=CalderRoutes.at(b,way.b());
        int near=BikeNightGus.flat(at,pa)<=BikeNightGus.flat(at,pb)?way.a():way.b(),ahead=near;
        var going=going(k,prey);
        // A reader going somewhere is headed off at the end of their way they are riding toward; one who is standing, at its far end.
        ahead=going!=null?dot(pa.subtract(at),going)>=dot(pb.subtract(at),going)?way.a():way.b():way.other(near);
        int target=part==0?near:ahead;
        if(part==2){
            // The place past the one they are riding toward: the one most the way they are going, and never back along their own way.
            int best=-1;double most=-Double.MAX_VALUE;var ap=CalderRoutes.at(b,ahead);
            for(var w:CalderRoutes.from(ahead)){
                int next=w.other(ahead);if(next==way.other(ahead))continue;
                var d=CalderRoutes.at(b,next).subtract(ap);double score=going==null?-d.lengthSqr():dot(d.normalize(),going);
                if(score>most){most=score;best=next;}
            }
            if(best>=0)target=best;
        }
        // The proposal: "the chase steers the readers toward the edge of town, where the only way the bullies aren't blocking is the
        // graveyard gate." Nobody heads a reader off to the north: one whose part would send him there closes the way back instead,
        // at the other end of the reader's way, so that north is the way left open.
        if(part>0&&CalderRoutes.at(b,target).z<at.z-NORTH_OPEN){
            int other=target==ahead||target==way.a()||target==way.b()?way.other(ahead):ahead;
            target=CalderRoutes.at(b,other).z<at.z-NORTH_OPEN?near:other;
        }
        // Those who head a reader off do not ride through them to do it.
        int avoid=part==0?-1:target==way.a()?way.b():target==way.b()?way.a():way.other(ahead);
        var path=CalderRoutes.path(from,target,avoid);
        // Turning round is done where he is, not at the place he was making for.
        if(path.length>1&&path[1]==before)path=Arrays.copyOfRange(path,1,path.length);
        u.plan=path;u.step=0;u.aim=target;
    }
    /** Somewhere to go, by the town's ways, from where he is. */
    static void send(ServerLevel l,BikeNight.Night n,Rider u,PackBike bike,int to){
        u.plan=CalderRoutes.path(start(l,n,bike),to,-1);u.step=0;u.aim=to;u.planAt=Long.MAX_VALUE;
    }
    private static boolean tight(int a,int b){for(var w:CalderRoutes.from(a))if(w.other(a)==b)return w.tight();return false;}
    /** Along the way he has in mind, a place at a time. True when he is at the end of it. */
    static boolean follow(ServerLevel l,BikeNight.Night n,Rider u,PackBike bike){
        if(u.plan==null||u.step>=u.plan.length){bike.toward(null,0);return true;}
        var b=n.base;
        for(;;){
            var to=CalderRoutes.at(b,u.plan[u.step]);
            boolean on=u.step>0&&tight(u.plan[u.step-1],u.plan[u.step]),into=u.step+1<u.plan.length&&tight(u.plan[u.step],u.plan[u.step+1]);
            double d=Math.sqrt(BikeNightGus.flat(bike.position(),to));
            if(d<(on||into?AT_TIGHT:AT_PLACE)){
                if(++u.step>=u.plan.length){bike.toward(null,0);return true;}
                continue;
            }
            // Something is in his way on a way that should be clear: round it, one side and then the other.
            if(bike.heldUp()&&n.tick>=u.aroundUntil){u.aroundUntil=n.tick+24;u.around=-u.around;}
            if(n.tick<u.aroundUntil){double rad=Math.toRadians(bike.getYRot()+u.around*70);to=bike.position().add(-Math.sin(rad)*3,0,Math.cos(rad)*3);}
            bike.toward(to,on||into&&d<4?TIGHT:BikeNightBullies.HUNT);
            return false;
        }
    }
    /** Whether a bike gets from one point straight to another without touching anything, on ground all the way. */
    public static boolean clear(ServerLevel l,Vec3 from,Vec3 to){
        double dx=to.x-from.x,dz=to.z-from.z,len=Math.hypot(dx,dz);int steps=Math.max(1,(int)Math.ceil(len/.5));
        for(int i=1;i<=steps;i++){
            double t=i/(double)steps,x=from.x+dx*t,z=from.z+dz*t,y=from.y;
            if(!l.noCollision(new AABB(x-.3,y+.3,z-.3,x+.3,y+.9,z+.3)))return false;
            if(i<steps&&l.noCollision(new AABB(x-.2,y-1.2,z-.2,x+.2,y-.05,z+.2)))return false;
        }
        return true;
    }

    // ------------------------------------------------------------------------------------------------ caught
    /** Who is within reach of one of them, and for how long: two seconds is caught. */
    static void reach(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> prey){
        for(var p:prey){
            Bully by=null;
            for(var u:k.b){
                var bike=BikeNightBullies.bike(l,u);if(bike==null||!u.riding||u.leaving||n.tick<u.stunnedUntil)continue;
                if(BikeNightGus.flat(bike.position(),p.position())<REACH*REACH&&Math.abs(bike.getY()-p.getY())<1.5){by=u;break;}
            }
            int count=Math.max(0,k.closing.getOrDefault(p.getUUID(),0)+(by!=null?1:-1));
            if(count==0)k.closing.remove(p.getUUID());else k.closing.put(p.getUUID(),count);
            if(by!=null&&count>=CAUGHT)caught(l,n,k,by,p);
        }
    }
    /** How long a reader has been within reach of one of them, in ticks. */
    static int closing(Pack k,ServerPlayer p){return k.closing.getOrDefault(p.getUUID(),0);}

    /** One of them has a reader: the bike is his, the reader is in the leaves, and he is off with it. */
    static void caught(ServerLevel l,BikeNight.Night n,Pack k,Bully by,ServerPlayer p){
        k.closing.remove(p.getUUID());k.caught.put(p.getUUID(),n.tick);
        var a=BikeNightBullies.actor(l,by);var bike=BikeNightBullies.bike(l,by);
        // Where it was: the ground under them, not the saddle or the pegs they are about to come off.
        var where=p.getVehicle()!=null?p.getVehicle().position():p.position();
        // The reader's bike: the one under them, or the one they left standing somewhere.
        NightBike.Make make=null;
        if(p.getVehicle() instanceof NightBike nb){
            // From under them: it is not stood anywhere, as it would be if they had got off it.
            if(nb.getFirstPassenger()==p){make=nb.make();nb.taken();}else p.stopRiding();
        }
        if(make==null)for(var m:NightBike.Make.values())if(p.getUUID().equals(n.owner[m.ordinal()])){
            make=m;var at=n.at[m.ordinal()];
            if(at!=null&&l.isLoaded(at)&&l.getBlockState(at).getBlock() instanceof NightBikeBlock)l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        }
        if(make!=null){
            n.at[make.ordinal()]=null;
            if(bike!=null){
                var tow=BikeNightRegistry.PACK_BIKE.get().create(l);
                if(tow!=null){tow.look(PackBike.MAKES+make.ordinal());var at=beside(bike);tow.moveTo(at.x,at.y,at.z,bike.getYRot(),0);if(l.addFreshEntity(tow))by.towing=tow.getUUID();}
            }
        }
        // Into the leaves: off, and down, and not hurt.
        var from=bike!=null?bike.position():p.position().add(0,0,1);var away=new Vec3(p.getX()-from.x,0,p.getZ()-from.z);
        away=away.lengthSqr()<1e-4?new Vec3(1,0,0):away.normalize();
        p.setDeltaMovement(away.x*.45,.32,away.z*.45);p.hurtMarked=true;p.fallDistance=0;
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,BikeNightRegistry.LEAVES.get().defaultBlockState()),p.getX(),p.getY()+.4,p.getZ(),36,.5,.3,.5,.05);
        l.playSound(null,p.getX(),p.getY(),p.getZ(),BikeNightRegistry.LEAVES_FALL.get(),SoundSource.PLAYERS,1F,.95F+l.getRandom().nextFloat()*.1F);
        if(a!=null){BikeNightBullies.say(a,by.who,BikeNightTexts.CAUGHT[by.who]);l.playSound(null,a.getX(),a.getY(),a.getZ(),BikeNightRegistry.LAUGH.get(),SoundSource.NEUTRAL,1.6F,by.who==2?.85F:by.who==1?1.12F:1F);}
        BikeNight.cue(p,make==null?BikeNightTexts.CAUGHT_NO_BIKE_CUE:String.format(BikeNightTexts.CAUGHT_CUE,BikeNightBullies.NAMES[by.who]));
        var own=BikeNight.own(p);own.putBoolean("Caught",true);own.putInt("CaughtBy",by.who);
        own.putString("CaughtOn",CalderRoutes.place(CalderRoutes.nearest(n.base,where)).street());BikeNight.save(p,own);
        if(make!=null)BikeNightGus.sawAgain(n,BikeNightTexts.GUS_CAUGHT);
        by.leaving=true;by.plan=null;by.closing=false;by.goneBy=n.tick+GONE_MOST;
    }
    /** Where a bike led beside another one is: a little to its right. */
    static Vec3 beside(PackBike bike){double rad=Math.toRadians(bike.getYRot());return bike.position().add(-Math.cos(rad)*.8,0,-Math.sin(rad)*.8);}

    /** One who has what he came for: off by the town's ways to its far end, the bike he took wheeled beside him, and gone when nobody is near to see. */
    static void leave(ServerLevel l,BikeNight.Night n,Pack k,Bully u,List<ServerPlayer> here){
        var a=BikeNightBullies.actor(l,u);var bike=BikeNightBullies.bike(l,u);
        if(a==null||bike==null){gone(l,u);return;}
        if(n.tick<u.stunnedUntil){bike.toward(null,0);}
        else{
            if(u.plan==null){
                // The far end of town that is furthest from everybody.
                int best=CalderRoutes.id(AWAY[0]);double most=-1;
                for(var name:AWAY){int id=CalderRoutes.id(name);var at=CalderRoutes.at(n.base,id);double least=Double.MAX_VALUE;
                    for(var p:here)least=Math.min(least,BikeNightGus.flat(p.position(),at));
                    if(least>most){most=least;best=id;}}
                send(l,n,u,bike,best);
            }
            boolean there=follow(l,n,u,bike);
            double nearest=Double.MAX_VALUE;for(var p:here)if(!p.isSpectator())nearest=Math.min(nearest,BikeNightGus.flat(p.position(),bike.position()));
            if(there||n.tick>=u.goneBy||nearest>44*44){gone(l,u);return;}
        }
        if(u.towing!=null&&l.getEntity(u.towing) instanceof PackBike tow){var at=beside(bike);tow.moveTo(at.x,at.y,at.z,bike.getYRot(),0);tow.setDeltaMovement(Vec3.ZERO);}
    }
    /** He is gone, and his bike, and the one he took. */
    static void gone(ServerLevel l,Bully u){
        var a=BikeNightBullies.actor(l,u);var bike=BikeNightBullies.bike(l,u);
        if(a!=null){a.stopRiding();a.discard();}
        if(bike!=null)bike.discard();
        if(u.towing!=null&&l.getEntity(u.towing)!=null)l.getEntity(u.towing).discard();
        u.id=null;u.bike=null;u.towing=null;u.riding=false;u.leaving=false;u.plan=null;u.gone=true;
    }

    /** Readers who have been caught: carried on a Hopper's pegs they are still in it; with nobody to carry them, or left too long, they are out. */
    static void afoot(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> here,boolean anyLeft){
        for(var p:here){
            Long at=k.caught.get(p.getUUID());if(at==null||k.out.contains(p.getUUID()))continue;
            boolean carried=p.getVehicle() instanceof NightBike nb&&nb.getFirstPassenger()!=p;
            if(carried){k.caught.put(p.getUUID(),n.tick);continue;}
            if(anyLeft&&n.tick-at<AFOOT)continue;
            k.out.add(p.getUUID());
            var own=BikeNight.own(p);own.putBoolean("Out",true);BikeNight.save(p,own);
            BikeNight.cue(p,BikeNightTexts.OUT_CUE);
        }
    }
    /** Those of a night's readers the three are still after: not watching, and not caught. */
    static List<ServerPlayer> prey(Pack k,List<ServerPlayer> here){
        var out=new ArrayList<ServerPlayer>();for(var p:here)if(!p.isSpectator()&&!k.caught.containsKey(p.getUUID()))out.add(p);
        out.sort(java.util.Comparator.comparing(ServerPlayer::getUUID));
        return out;
    }
}
