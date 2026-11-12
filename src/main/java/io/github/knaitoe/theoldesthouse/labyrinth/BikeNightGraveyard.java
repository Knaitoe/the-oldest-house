package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.Bully;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeNightBullies.Pack;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.490, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Act three: the graveyard"): what happens on the hill. Its ground is
 * {@link CalderGraves}.
 *
 * <p><b>Getting in.</b> As answered: "The town's hunt leaves the north open, so the gate is the one way the three are not in. It has
 * been shut all evening and is standing open when the readers reach it. Nobody says why." It opens once the chase is on, and only
 * when nobody is near enough to be looking at it: there is nothing to hear, and nobody sees it move.
 *
 * <p><b>The three.</b> The proposal: "they come through the gate, slower; one stops at the fence ('I'm not going in there'); the other
 * two fall back in the mist, and then their jeers stop altogether." Duff stops outside the gate and says so. Tully and Ray-Ray come
 * in at two thirds of the pace they hunt at, weaving between the stones; each has only so much hill in him (Ray-Ray the first bank,
 * Tully the second), and at the end of it, or after twenty seconds in there, he stops, says one thing to the other, and says nothing
 * again. Until then they are after a reader as they were in the town, and "can still catch a reader who comes off in the first rows".
 *
 * <p>"Nothing is shown happening to them: their voices stop, and they are taken away only when nobody can see them go." One who has
 * stopped is gone, with his bike, the first moment after five seconds that nobody is near enough and turned his way to see it. The
 * one at the fence is the last.
 *
 * <p><b>Gus</b> comes in after his friend, through the stones, and says what he thinks of it twice. Past the last full row, where the
 * small stones are, "he says nothing": not what he had been about to say, and not to a hand on him.
 */
public final class BikeNightGraveyard {
    private BikeNightGraveyard(){}
    /** How one of the three stands with the graveyard: not come to it; coming to it or in it; stopped, at the fence or up the hill; taken. */
    public static final int NOT_YET=0,COMING=1,STOPPED=2,TAKEN=3;
    /** Which of the three stops at the fence. */
    public static final int AT_FENCE=2;
    /** The row each of the other two gets no further north than: Tully a little short of the second bank, Ray-Ray of the first. */
    public static final int[] NERVE={CalderGraves.BANKS[1]+2,CalderGraves.BANKS[0]+2};
    /** Blocks a tick in there ("slower"). */
    public static final float PACE=.2F;
    /** In ticks: the most one of them is in there before he stops; how long one who has stopped is left before he can be taken. */
    public static final int IN_MOST=400,TAKEN_AFTER=100;
    /** In blocks: how near somebody must be to see the gate move; to see one of them go; how far ahead a rider looks for a way between the stones. */
    public static final double GATE_SEEN=48,SEEN=40,LOOK=3;
    /** The middle of the gateway, and how far inside it a rider is in. */
    public static final double GATE_X=0,GATE_IN=CalderTown.RAILS-2.5;

    /**
     * 0.4.491, the mist, in a hundred: how thick it is just inside the gate, and past the last row; what is left of it beside a lit
     * Lamp; how near a Lamp must be, in blocks; and how often a reader is told, in ticks.
     */
    public static final int MIST_GATE=18,MIST_BACK=100,MIST_EVERY=10;
    public static final float LAMP_CUTS=.45F;
    public static final double LAMP_NEAR=7;
    /**
     * 0.4.491, the town's lights: the rows its streets' lamps are north of, from the cemetery down to the park's street (a lamp goes out
     * with the first of these it is north of); ticks between one street and the next; and how far its breaker is heard.
     */
    public static final int[] STREETS={-150,-110,-75,-46};
    public static final int DARK_EVERY=50;
    public static final float BREAKER_HEARD=5;

    /** A night's graveyard. */
    static final class Yard {
        boolean gateOpen;
        final int[] state=new int[3];final long[] since={-1,-1,-1};
        /** What each reader was last told of their mist. */
        final java.util.Map<java.util.UUID,Integer> mist=new java.util.HashMap<>();
        /** How many of the town's streets are dark, when the next goes, and where the town's lamps stand (found when the first goes). */
        int dark;long darkAt=-1;@javax.annotation.Nullable java.util.List<BlockPos> lamps;
        /** 0.4.498: somebody is out by the back gate, and the town's lamps are on again for the rest of the night. */
        boolean on;
    }

    // ------------------------------------------------------------------------------------------------ for tests, and for the night
    public static boolean gateOpen(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.yard.gateOpen;}
    public static int state(BlockPos b,int who){var n=BikeNight.night(b);return n==null?NOT_YET:n.yard.state[who];}
    static boolean stopped(BikeNight.Night n,Bully u){return n.yard.state[u.who]>=STOPPED;}
    /** Whether somewhere is inside the cemetery's railings. */
    public static boolean in(BlockPos b,Vec3 at){
        double x=at.x-b.getX(),z=at.z-b.getZ();
        return z<CalderTown.RAILS+.5&&z>CalderGraves.BACK&&x>CalderGraves.W&&x<CalderGraves.E+1;
    }
    /** Whether somewhere is past the last full row, where the small stones are. */
    public static boolean hushed(BlockPos b,Vec3 at){return in(b,at)&&at.z-b.getZ()<CalderGraves.ROWS[CalderGraves.ROWS.length-1]-.5;}
    static boolean hushed(BikeNight.Night n,Vec3 at){return hushed(n.base,at);}
    /** Whether somebody could be watching a place: near enough, and it is in front of them or right beside them. */
    static boolean seen(ServerPlayer p,Vec3 at,double within){
        var to=at.subtract(p.getEyePosition());double d=to.length();
        return d<=within&&(d<2.5||to.normalize().dot(p.getLookAngle())>.2);
    }
    static void reset(ServerLevel l,BikeNight.Night n){
        if(CalderGraves.gateIsOpen(l,n.base))CalderGraves.gateShut(l,n.base);
        if(n.yard.lamps!=null)for(var at:n.yard.lamps)BikeNightSite.lantern(l,n.base,at,true);
        n.yard=new Yard();
    }
    public static int dark(BlockPos b){var n=BikeNight.night(b);return n==null?0:n.yard.dark;}

    // ------------------------------------------------------------------------------------------------ the mist
    /**
     * How thick a reader's own mist is, in a hundred. The proposal: "It rises off the grass and thickens with each row. The Lamp's
     * headlight cuts it." Nothing outside the railings; inside, more for each row of stones they are past; and less than half of
     * that for the rider of a Lamp that is throwing its light, "and whoever rides close".
     */
    public static int mist(ServerLevel l,BlockPos b,ServerPlayer p){
        double z=p.getZ()-b.getZ();float m=0;
        if(in(b,p.position())){
            int rows=0;for(int row:CalderGraves.ROWS)if(z<row+.5)rows++;
            m=MIST_GATE+(MIST_BACK-MIST_GATE)*rows/(float)CalderGraves.ROWS.length;
        }
        // 0.4.506: the Hollow's dark way lies in a bank of it, thicker than any in the cemetery.
        if(CalderHollow.dark(p.getX()-b.getX(),z))m=Math.max(m,DARK_MIST);
        if(m>0&&lamp(l,p))m*=LAMP_CUTS;
        return Math.round(m);
    }
    /**
     * 0.4.506: how thick the dark way's bank is, in a hundred of what the cemetery's thickest is: somebody in it sees two blocks
     * and a half (BikeNightMistClient.sight), and beside a Lamp that is throwing its light, eighteen.
     */
    public static final int DARK_MIST=140;
    /** Whether a Lamp that is ridden and making light is near somebody. Its dynamo makes light only while the wheel turns: stopped, it cuts nothing. */
    public static boolean lamp(ServerLevel l,ServerPlayer p){
        return !l.getEntitiesOfClass(NightBike.class,p.getBoundingBox().inflate(LAMP_NEAR),bike->bike.make()==NightBike.Make.LAMP&&bike.lit()>0&&!bike.getPassengers().isEmpty()).isEmpty();
    }
    private static void mists(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        if(n.tick%MIST_EVERY!=0)return;
        var y=n.yard;
        for(var p:here){
            int m=mist(l,n.base,p);Integer was=y.mist.put(p.getUUID(),m);
            // Told while they are in it, and once more when they are out of it.
            if(m>0||was!=null&&was>0)io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.BikeNightMistPayload(m,0));
        }
    }

    // ------------------------------------------------------------------------------------------------ the town's lights
    /**
     * The proposal's notes: "the city starts to melt away". Once every reader who is still in the night is on the hill, past its
     * first bank, the town's street lamps go out a street at a time, from Church Lane down to the park's own street, each with its
     * breaker. With a reader still down in the town nothing goes out: it is their town too. The park's lamps are the park's.
     */
    /** Where the town's street lamps stand: every lamp's foot north of the park's street that has a lantern over it. (A street's name hangs on a post with the same foot and no lantern.) */
    public static java.util.List<BlockPos> townLamps(ServerLevel l,BlockPos b){
        var out=new java.util.ArrayList<BlockPos>();
        for(int x=CalderTown.W;x<=CalderTown.E;x++)for(int z=CalderTown.N;z<STREETS[STREETS.length-1];z++)for(int at=0;at<=2;at++){
            var s=l.getBlockState(b.offset(x,at,z));
            if(!CalderBlock.is(s,CalderBlock.Kind.LAMP)||s.getValue(CalderBlock.STAGE)!=CalderBlock.FOOT)continue;
            var top=l.getBlockState(b.offset(x,at+BikeNightSite.LAMP_TOP,z));
            if(CalderBlock.is(top,CalderBlock.Kind.LAMP)&&top.getValue(CalderBlock.STAGE)>=CalderBlock.LIT)out.add(new BlockPos(x,at,z));
        }
        return out;
    }
    /** 0.4.498: "with the lights of Calder Falls on again below". Every lamp that went out is lit, and none goes out again tonight. */
    static void lightsOn(ServerLevel l,BikeNight.Night n){
        var y=n.yard;if(y.on)return;y.on=true;y.dark=0;relight(l,n);
    }
    /** The lamps that went out, lit again where the town is loaded: the rest are lit as it comes back in. */
    private static void relight(ServerLevel l,BikeNight.Night n){
        if(n.yard.lamps!=null)for(var at:n.yard.lamps)if(l.isLoaded(n.base.offset(at)))BikeNightSite.lantern(l,n.base,at,true);
    }
    /** Every street's lamps go out now, as they do one street at a time behind riders who are in the cemetery. For tests. */
    public static void darkNow(ServerLevel l,BlockPos b){
        var n=BikeNight.night(b);if(n==null||n.yard.on)return;var y=n.yard;
        if(y.lamps==null)y.lamps=townLamps(l,b);
        for(var at:y.lamps)BikeNightSite.lantern(l,b,at,false);
        y.dark=STREETS.length;
    }
    /** How many of the lamps that the town's lights rule knows are lit, and how many it knows. For tests. */
    public static int[] lit(ServerLevel l,BlockPos b){
        var n=BikeNight.night(b);if(n==null||n.yard.lamps==null)return new int[]{0,0};int lit=0;
        for(var at:n.yard.lamps)if(BikeNightSite.lit(l,b,at))lit++;
        return new int[]{lit,n.yard.lamps.size()};
    }
    private static void lights(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        if(n.yard.on){if(n.tick%20==0)relight(l,n);return;}
        var y=n.yard;var b=n.base;
        if(y.dark>=STREETS.length||n.tick<y.darkAt)return;
        if(y.dark==0){
            var prey=BikeNightHunt.prey(n.pack,here);if(prey.isEmpty()||!y.gateOpen)return;
            for(var p:prey)if(!in(b,p.position())||p.getZ()-b.getZ()>=CalderGraves.BANKS[0])return;
        }
        if(y.lamps==null)y.lamps=townLamps(l,b);
        int south=STREETS[y.dark],north=y.dark==0?Integer.MIN_VALUE:STREETS[y.dark-1];
        for(var at:y.lamps)if(at.getZ()<south&&at.getZ()>=north)BikeNightSite.lantern(l,b,at,false);
        l.playSound(null,b.getX()+.5,b.getY()+3,b.getZ()+(y.dark==0?CalderTown.RAILS+8:(south+north)/2.0),BikeNightRegistry.BREAKER.get(),net.minecraft.sounds.SoundSource.BLOCKS,BREAKER_HEARD,.8F);
        y.dark++;y.darkAt=n.tick+DARK_EVERY;
    }

    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var y=n.yard;
        mists(l,n,here);
        lights(l,n,here);
        // The gate: open once they are hunted, and only when nobody could be looking.
        if(!y.gateOpen&&n.pack.stage==BikeNightBullies.CHASE){
            var gate=new Vec3(n.base.getX()+GATE_X,n.base.getY()+1,n.base.getZ()+CalderTown.RAILS+.5);boolean watched=false;
            for(var p:l.players())watched|=seen(p,gate,GATE_SEEN);
            if(!watched){CalderGraves.gateOpen(l,n.base);y.gateOpen=true;}
        }
        // Whoever has stopped is taken when nobody can see him go; the one at the fence after the others.
        for(var u:n.pack.b){
            if(u.gone||y.state[u.who]!=STOPPED||n.tick-y.since[u.who]<TAKEN_AFTER)continue;
            if(u.who==AT_FENCE&&(waits(n,0)||waits(n,1)))continue;
            var a=BikeNightBullies.actor(l,u);var bike=BikeNightBullies.bike(l,u);
            var at=a!=null?a.position():bike!=null?bike.position():null;boolean watched=false;
            if(at!=null)for(var p:l.players())watched|=seen(p,at.add(0,.8,0),SEEN);
            if(watched)continue;
            if(a!=null){a.stopRiding();a.discard();}
            if(bike!=null)bike.discard();
            u.gone=true;u.riding=false;u.plan=null;y.state[u.who]=TAKEN;
        }
    }
    /** Whether one of the two who go in is still to be taken: he is in there, or stopped there. */
    private static boolean waits(BikeNight.Night n,int who){var s=n.yard.state[who];return !n.pack.b[who].gone&&(s==COMING||s==STOPPED);}

    // ------------------------------------------------------------------------------------------------ the three
    /**
     * One of the three after a reader who is in the cemetery, or himself in it: true if this has said where he rides this tick. With
     * the reader and him both outside it, the town's hunt has him.
     */
    static boolean ride(ServerLevel l,BikeNight.Night n,Pack k,Bully u,LiteraryActor a,PackBike bike,ServerPlayer reader){
        var y=n.yard;var b=n.base;var here=bike.position();var there=reader.position();
        boolean readerIn=in(b,there),himIn=in(b,here);
        if(!y.gateOpen||(!readerIn&&!himIn)){if(y.state[u.who]==COMING)y.state[u.who]=NOT_YET;return false;}
        if(y.state[u.who]==NOT_YET){y.state[u.who]=COMING;y.since[u.who]=-1;u.plan=null;u.aim=-1;u.closing=false;}
        var gate=CalderRoutes.at(b,CalderRoutes.id("cemetery_gate"));var inside=new Vec3(b.getX()+GATE_X,b.getY(),b.getZ()+GATE_IN);
        if(!himIn){
            // To the gate by the town's ways, then (but for the one who will not) through it.
            double d=Math.sqrt(BikeNightGus.flat(here,gate));
            if(d>BikeNightHunt.AT_PLACE+.6){toGate(l,n,u,bike);return true;}
            if(u.who==AT_FENCE){stop(n,u,a,bike,BikeNightTexts.NOT_GOING_IN);return true;}
            u.plan=null;bike.toward(inside,PACE);return true;
        }
        if(y.since[u.who]<0)y.since[u.who]=n.tick;
        // In there. A reader who has gone out again is followed out by the gate.
        if(!readerIn){
            boolean atGate=Math.sqrt(BikeNightGus.flat(here,inside))<2.2;
            bike.toward(atGate?gate:weave(l,b,bike,inside,u),PACE);return true;
        }
        if(u.who!=AT_FENCE&&(here.z-b.getZ()<=NERVE[u.who]||n.tick-y.since[u.who]>=IN_MOST)){stop(n,u,a,bike,BikeNightTexts.FALL_BACK[u.who]);return true;}
        double d=Math.sqrt(BikeNightGus.flat(here,there));
        bike.toward(weave(l,b,bike,there,u),(float)Math.min(PACE,Math.max(.1,d*.16)));
        return true;
    }
    private static void toGate(ServerLevel l,BikeNight.Night n,BikeNightHunt.Rider u,PackBike bike){
        int gate=CalderRoutes.id("cemetery_gate");
        if(u.plan==null||u.aim!=gate||u.step>=u.plan.length)BikeNightHunt.send(l,n,u,bike,gate);
        BikeNightHunt.follow(l,n,u,bike);
    }
    private static void stop(BikeNight.Night n,Bully u,LiteraryActor a,PackBike bike,String line){
        bike.toward(null,0);u.plan=null;
        n.yard.state[u.who]=STOPPED;n.yard.since[u.who]=n.tick;
        BikeNightBullies.say(a,u.who,line);
    }

    // ------------------------------------------------------------------------------------------------ Gus
    /**
     * Gus after his friend, when either of them is in the cemetery: true if this has said where he rides this tick. He keeps the
     * distance from them he keeps anywhere.
     */
    static boolean friend(ServerLevel l,BikeNight.Night n,BikeNightHunt.Rider way,PackBike bike,Vec3 there,double d){
        var b=n.base;var here=bike.position();boolean theyIn=in(b,there),himIn=in(b,here);
        if(!n.yard.gateOpen||(!theyIn&&!himIn))return false;
        var gate=CalderRoutes.at(b,CalderRoutes.id("cemetery_gate"));var inside=new Vec3(b.getX()+GATE_X,b.getY(),b.getZ()+GATE_IN);
        if(!himIn){
            if(Math.sqrt(BikeNightGus.flat(here,gate))>BikeNightHunt.AT_PLACE+.6){toGate(l,n,way,bike);return true;}
            way.plan=null;bike.toward(inside,BikeNightGus.RIDE_PACE);return true;
        }
        BikeNightGus.saw(n,"yard",BikeNightTexts.GUS_YARD);
        if(here.z-b.getZ()<CalderGraves.BANKS[0])BikeNightGus.saw(n,"yard_on",BikeNightTexts.GUS_YARD_ON);
        if(!theyIn){bike.toward(Math.sqrt(BikeNightGus.flat(here,inside))<2.2?gate:weave(l,b,bike,inside,way),BikeNightGus.RIDE_PACE);return true;}
        if(d<BikeNightGus.KEEP){bike.toward(null,0);return true;}
        bike.toward(weave(l,b,bike,there,way),(float)Math.min(BikeNightGus.RIDE_PACE,Math.max(.12,(d-BikeNightGus.KEEP+.6)*.16)));
        return true;
    }

    // ------------------------------------------------------------------------------------------------ between the stones
    /**
     * Whether a bike gets from one place on the hill straight to another: nothing standing in its way, at the height the hill is at
     * each step of it, and ground under it all the way. A bank is not in its way, and neither is a low marker; the open grave is.
     */
    public static boolean clear(ServerLevel l,BlockPos b,Vec3 from,Vec3 to){
        double dx=to.x-from.x,dz=to.z-from.z,len=Math.hypot(dx,dz);int steps=Math.max(1,(int)Math.ceil(len/.5));
        for(int i=1;i<=steps;i++){
            double t=i/(double)steps,x=from.x+dx*t,z=from.z+dz*t;int row=(int)Math.floor(z-b.getZ());
            double y=b.getY()+CalderGraves.level(row)+(CalderGraves.bank(row)?.5:0);
            if(!l.noCollision(new AABB(x-.3,y+.6,z-.3,x+.3,y+1.2,z+.3)))return false;
            if(l.noCollision(new AABB(x-.2,y-.6,z-.2,x+.2,y-.05,z+.2)))return false;
        }
        return true;
    }
    /**
     * Where to steer for, to get to somewhere through the stones: straight at it if the next three blocks that way are clear, or
     * else along the nearest heading to either side that is, the side he last went round first. Boxed in, straight at it: the bike's
     * own being held up turns it.
     */
    static Vec3 weave(ServerLevel l,BlockPos b,PackBike bike,Vec3 to,BikeNightHunt.Rider u){
        var here=bike.position();double far=Math.sqrt(BikeNightGus.flat(here,to));
        if(far<=LOOK&&clear(l,b,here,to))return to;
        double want=Math.atan2(-(to.x-here.x),to.z-here.z);
        for(int k=0;k<=5;k++)for(int side:k==0?new int[]{0}:new int[]{u.around,-u.around}){
            double at=want+side*k*Math.toRadians(20);
            var step=new Vec3(here.x-Math.sin(at)*LOOK,here.y,here.z+Math.cos(at)*LOOK);
            if(!clear(l,b,here,step))continue;
            if(k>0)u.around=side;
            return step;
        }
        return to;
    }
}
