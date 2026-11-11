package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.484, Bike night: the traffic. The proposal:
 * <ul>
 * <li>"The cars: a new, simple actor, a car driving its lane with its headlights on."</li>
 * <li>"Who drives them: a station wagon with wood panels, a pickup, a long sedan, a muscle car with its windows down."</li>
 * <li>"Honks: they honk at a rider in their lane and slow down. They never hit anyone, but a rider who swerves into one comes off."</li>
 * <li>"One car slows beside the bullies and yells at them, which buys the readers a few seconds."</li>
 * </ul>
 *
 * <p><b>Where they drive.</b> Two rounds of the town's streets, each driven both ways: Park Street, West Avenue, Main Street and East
 * Avenue; and Main Street, West Avenue, Elm Street and East Avenue. A car keeps {@link #LANE} to the right of the middle of the
 * road, so the two ways round a round are two lanes. Church Lane, under the cemetery, has no cars on it.
 *
 * <p><b>A car is the night's, and its body is only what is seen of it.</b> Where each car is on its round is kept here and moved
 * here, whether or not anybody is near enough for that part of the town to be awake. Its body (TownCar, never saved) is put where
 * the car is each tick while that place is awake, and made again when it is not there.
 *
 * <p><b>How one drives.</b> It looks along its lane ahead of its nose. With a rider, one of the three, Gus or another car in it, it
 * slows, and stops short of them; a rider gets its horn, and after a while what its driver thinks. With somebody under it (they rode
 * into its side) it stays where it is. A reader who rides into one faster than a bike comes off at a wall comes off (RiddenBike.struck).
 * The three's bikes and Gus's are put to one side of a car that is where they are: they go round it.
 *
 * <p><b>Telling off.</b> Twice a night at most, and once for each car, a driver who comes by one of the three in the town slows beside
 * him and says what they think of him. He stops for it, as he does for a ball of sand, and answers back.
 *
 * <p>They start when the gate is opened, which is when a reader can first be out of the park: until then, as Gus says, cars do not
 * come down here this late. Two more stand parked in the burger place's lot all night (0.4.485).
 */
public final class CalderTraffic {
    private CalderTraffic(){}
    /** Blocks a tick: a car's pace through the town; what it gains and loses of it in a tick; and its crawl beside somebody it is telling off. */
    public static final float CRUISE=.34F,GAIN=.012F,BRAKE=.04F,CRAWL=.05F;
    /**
     * In blocks: how far a car keeps to the right of the middle of the road; how far ahead of its nose it looks along its lane, and how
     * near the middle of its lane something is to be in its way; how far short of it the car stops; how near a rider in its lane is
     * before it sounds its horn; how near one of the three a driver is to tell him off; and how far a car is heard.
     */
    public static final double LANE=1.35,LOOK=11,IN_LANE=1.25,SHORT=1.2,HORN=7,BESIDE=5,HEARD=48;
    /**
     * In ticks: between two soundings of a horn; how long a driver has sat behind somebody before saying so; how long one of the three
     * stops when a driver tells him off; how long after that he answers; and between two beats of an engine.
     */
    public static final int HORN_GAP=45,WAITED=110,TOLD=56,REPLY=24,ENGINE_EVERY=18;
    /** How many of the three a night's drivers tell off. */
    public static final int TOLD_MOST=2;
    private static final float[] ENGINE_PITCH={.85F,.72F,.92F,1.12F};

    /** The two rounds' corners, along the middle of the roads: CalderTown's streets, at the middle of their middle rows. */
    private static final double[][] SOUTH={{50.5,-51.5},{-49.5,-51.5},{-49.5,-91.5},{49.7,-91.5},{50.5,-89.5}};
    private static final double[][] NORTH={{-49.5,-91.5},{-49.5,-103.5},{-45.5,-115.5},{-45.5,-128.5},{-11.5,-128.5},{10.5,-120.5},{46.5,-120.5},{46.5,-99.5},{49.7,-91.5}};
    /** A way round a round: its lane's corners (the last is the first again), how far along it each is, and how long it is. */
    public record Round(double[] x,double[] z,double[] at,double length){}
    private static final Round[] ROUNDS={round(SOUTH,false),round(SOUTH,true),round(NORTH,false),round(NORTH,true)};
    /** Which way round each car goes, and where on it it starts, as a part of the whole way: none of them near the park's gate. */
    private static final int[] ROUND_OF={0,1,2,3};
    private static final double[] START={.57,.22,.5,.3};

    private static Round round(double[][] corners,boolean back){
        int m=corners.length;var c=new double[m][];for(int i=0;i<m;i++)c[i]=corners[back?m-1-i:i];
        var x=new double[m+1];var z=new double[m+1];
        for(int i=0;i<m;i++){
            var a=c[(i+m-1)%m];var p=c[i];var q=c[(i+1)%m];
            double ux=p[0]-a[0],uz=p[1]-a[1],ul=Math.hypot(ux,uz),vx=q[0]-p[0],vz=q[1]-p[1],vl=Math.hypot(vx,vz);
            // The right of somebody going (dx, dz) is (-dz, dx). Each corner is moved to where the two lanes on either side of it meet.
            double ax=-uz/ul,az=ux/ul,bx=-vz/vl,bz=vx/vl,k=LANE/(1+ax*bx+az*bz);
            x[i]=p[0]+(ax+bx)*k;z[i]=p[1]+(az+bz)*k;
        }
        x[m]=x[0];z[m]=z[0];
        var at=new double[m+1];for(int i=0;i<m;i++)at[i+1]=at[i]+Math.hypot(x[i+1]-x[i],z[i+1]-z[i]);
        return new Round(x,z,at,at[m]);
    }
    public static int rounds(){return ROUNDS.length;}
    public static double length(int round){return ROUNDS[round].length;}
    /** Where a way round is, that far along it. */
    public static Vec3 point(BlockPos base,int round,double along){
        var r=ROUNDS[round];double s=along%r.length;if(s<0)s+=r.length;
        int i=0;while(i<r.x.length-2&&s>r.at[i+1])i++;
        double t=(s-r.at[i])/Math.max(1e-9,r.at[i+1]-r.at[i]);
        return new Vec3(base.getX()+r.x[i]+(r.x[i+1]-r.x[i])*t,base.getY(),base.getZ()+r.z[i]+(r.z[i+1]-r.z[i])*t);
    }
    /** The way a car points there: from a little behind it to a little ahead, so that it comes round a corner and does not snap round it. */
    public static float yaw(BlockPos base,int round,double along){
        var a=point(base,round,along-1.4);var b=point(base,round,along+1.4);
        return (float)Math.toDegrees(Math.atan2(-(b.x-a.x),b.z-a.z));
    }
    static double flat(Vec3 a,Vec3 b){double x=a.x-b.x,z=a.z-b.z;return x*x+z*z;}
    /** Whether somewhere is under a car that is at a place pointing a way, with that much to spare; and how far to its right it is. */
    static boolean over(Vec3 car,float yaw,Vec3 at,double spare){
        double rad=Math.toRadians(yaw),dx=at.x-car.x,dz=at.z-car.z;
        double along=-Math.sin(rad)*dx+Math.cos(rad)*dz;
        return Math.abs(along)<TownCar.LONG+spare&&Math.abs(across(car,yaw,at))<TownCar.WIDE+spare;
    }
    static double across(Vec3 car,float yaw,Vec3 at){double rad=Math.toRadians(yaw);return -Math.cos(rad)*(at.x-car.x)-Math.sin(rad)*(at.z-car.z);}

    /** One of the cars: which it is, its way round and how far along it is, how fast it is going, and its body and the light before it. */
    static final class Car {
        final int kind,round;double at;float speed;
        @Nullable UUID id;@Nullable BlockPos beam;
        /** When it may next sound its horn; since when somebody has been in its way; whether its driver has said so; until when it crawls; whether it has told one of the three off, and who answers when. */
        long hornAt,waitSince=-1,crawlUntil=-1,replyAt=-1;boolean said,told;int replyWho;
        Car(int kind){this.kind=kind;round=ROUND_OF[kind];at=START[kind]*ROUNDS[round].length;}
    }
    /** A night's traffic. */
    static final class Traffic {
        final Car[] cars=new Car[TownCar.MOVING];boolean going;int told;
        /** The bodies of the cars that stand parked. */
        final UUID[] parked=new UUID[PARKED.length];
        Traffic(){for(int i=0;i<cars.length;i++)cars[i]=new Car(i);}
    }
    /**
     * 0.4.485: the cars that stand in the burger place's lot all night, nose out toward Park Street: which each is, and where. The
     * proposal: "Teenagers sit on the hoods of their cars in the lot."
     */
    public static final double[][] PARKED={{TownCar.BLUE,11,-63,0},{TownCar.WHITE,17,-63,0}};
    public static Vec3 parkedAt(BlockPos base,int i){return new Vec3(base.getX()+PARKED[i][1],base.getY(),base.getZ()+PARKED[i][2]);}

    // ------------------------------------------------------------------------------------------------ each tick
    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var t=n.traffic;
        // From when the gate is opened: a reader can be out of the park from then on.
        if(!t.going){if(!n.pack.open&&n.pack.stage<BikeNightBullies.CHASE)return;t.going=true;}
        for(int i=0;i<PARKED.length;i++){
            var at=parkedAt(n.base,i);float yaw=(float)PARKED[i][3];
            if(t.parked[i]!=null&&l.getEntity(t.parked[i]) instanceof TownCar body&&body.isAlive()){body.place(at,yaw);continue;}
            var body=make(l,(int)PARKED[i][0],at,yaw);t.parked[i]=body==null?null:body.getUUID();
        }
        // What is on the roads: the readers (where their bikes are, if they are on them), and the bikes of the three and Gus.
        var riders=new ArrayList<Vec3>();
        for(var p:here)if(!p.isSpectator())riders.add(p.getVehicle()!=null?p.getVehicle().position():p.position());
        var packs=packs(l,n);
        var inWay=new ArrayList<>(riders);for(var b:packs)inWay.add(b.position());
        int count=t.cars.length;var where=new Vec3[count];var yaws=new float[count];
        for(int i=0;i<count;i++){where[i]=point(n.base,t.cars[i].round,t.cars[i].at);yaws[i]=yaw(n.base,t.cars[i].round,t.cars[i].at);}
        // How far each car's lane is clear ahead of its nose, and what is in it: a rider (-2), another car (which), or nothing (-1).
        var gap=new double[count];var what=new int[count];
        for(int i=0;i<count;i++){
            var c=t.cars[i];gap[i]=LOOK;what[i]=-1;
            look:for(double d=0;d<LOOK;d+=.5){
                var q=point(n.base,c.round,c.at+TownCar.LONG+d);
                for(var v:inWay)if(flat(v,q)<IN_LANE*IN_LANE){gap[i]=d;what[i]=-2;break look;}
                for(int j=0;j<count;j++){
                    if(j==i)continue;double rad=Math.toRadians(yaws[j]);
                    for(int e=-1;e<=1;e++){
                        var end=where[j].add(-Math.sin(rad)*e*TownCar.LONG,0,Math.cos(rad)*e*TownCar.LONG);
                        if(flat(end,q)<IN_LANE*IN_LANE){gap[i]=d;what[i]=j;break look;}
                    }
                }
            }
        }
        // Two cars each in the other's way where two lanes meet: the first of them goes.
        for(int i=0;i<count;i++){int j=what[i];if(j>i&&what[j]==i){gap[i]=LOOK;what[i]=-1;}}
        boolean heard=false;for(var v:riders)for(var w:where)heard|=flat(v,w)<HEARD*HEARD;
        for(int i=0;i<count;i++){
            var c=t.cars[i];var at=where[i];float yaw=yaws[i];
            boolean under=false;for(var v:riders)under|=over(at,yaw,v,.3);
            float want=under||gap[i]<SHORT?0:(float)Math.min(CRUISE,.03+(gap[i]-SHORT)*.1);
            if(n.tick<c.crawlUntil)want=Math.min(want,CRAWL);
            c.speed=c.speed<want?Math.min(want,c.speed+GAIN):Math.max(want,c.speed-BRAKE);
            c.at=(c.at+c.speed)%ROUNDS[c.round].length;
            var body=body(l,c,at,yaw);
            if(body!=null)body.place(at,yaw);
            beam(l,c,body==null?null:BlockPos.containing(point(n.base,c.round,c.at+TownCar.LONG+2)));
            if(heard&&(n.tick+i*5L)%ENGINE_EVERY==0)l.playSound(null,at.x,at.y+.8,at.z,BikeNightRegistry.ENGINE.get(),SoundSource.AMBIENT,.45F,ENGINE_PITCH[c.kind]+c.speed*.5F);
            // A rider in its lane: its horn; and, when it has sat behind them a while, what its driver thinks.
            if(what[i]==-2&&gap[i]<HORN){
                if(c.waitSince<0)c.waitSince=n.tick;
                if(n.tick>=c.hornAt){c.hornAt=n.tick+HORN_GAP;l.playSound(null,at.x,at.y+1,at.z,BikeNightRegistry.HORNS.get(c.kind).get(),SoundSource.NEUTRAL,2.5F,1F);}
                if(!c.said&&body!=null&&c.speed<.02F&&n.tick-c.waitSince>=WAITED){c.said=true;body.say(BikeNightTexts.GET_OFF[c.kind]);}
            }else{c.waitSince=-1;c.said=false;}
            // A reader who rides into it comes off, as at a wall.
            for(var p:here)if(!p.isSpectator()&&p.getVehicle() instanceof NightBike bike&&bike.getFirstPassenger()==p&&over(at,yaw,bike.position(),.25)&&bike.struck())
                c.crawlUntil=Math.max(c.crawlUntil,n.tick+60);
            // The three's bikes and Gus's go round it: one that is where the car is is put to that side of it.
            for(var b:packs)if(over(at,yaw,b.position(),.2)){
                double off=across(at,yaw,b.position()),side=off<0?-1:1,move=side*(TownCar.WIDE+.45)-off,rad=Math.toRadians(yaw);
                b.setPos(b.getX()-Math.cos(rad)*move,b.getY(),b.getZ()-Math.sin(rad)*move);
            }
            tellOff(l,n,t,c,body,at);
        }
    }
    /** The bikes that are not readers': the three's, any they are wheeling off, and Gus's. Asked for by name, not looked for across the town. */
    private static List<PackBike> packs(ServerLevel l,BikeNight.Night n){
        var out=new ArrayList<PackBike>();
        for(var u:n.pack.b)for(var id:new UUID[]{u.bike,u.towing})if(id!=null&&l.getEntity(id) instanceof PackBike b&&b.isAlive())out.add(b);
        if(n.gus.bike!=null&&l.getEntity(n.gus.bike) instanceof PackBike b&&b.isAlive())out.add(b);
        return out;
    }
    /** A driver coming by one of the three in the town slows beside him and says what they think; he stops for it, and answers. */
    private static void tellOff(ServerLevel l,BikeNight.Night n,Traffic t,Car c,@Nullable TownCar body,Vec3 at){
        if(c.replyAt>=0&&n.tick>=c.replyAt){
            c.replyAt=-1;var a=BikeNightBullies.actor(l,n.pack.b[c.replyWho]);
            if(a!=null)BikeNightBullies.say(a,c.replyWho,BikeNightTexts.TOLD_BACK[c.replyWho]);
        }
        if(c.told||t.told>=TOLD_MOST||body==null||c.speed<.1F||n.pack.stage!=BikeNightBullies.CHASE)return;
        for(var u:n.pack.b){
            var his=BikeNightBullies.bike(l,u);
            if(his==null||!u.riding||u.leaving||u.gone||n.tick<u.stunnedUntil||!BikeNightHunt.inTown(n,his.position())||flat(his.position(),at)>BESIDE*BESIDE)continue;
            c.told=true;t.told++;c.crawlUntil=n.tick+TOLD;c.replyAt=n.tick+REPLY;c.replyWho=u.who;
            u.stunnedUntil=n.tick+TOLD;his.toward(null,0);
            body.say(BikeNightTexts.TOLD_OFF[u.who]);
            return;
        }
    }
    /** A car's body: the one it has, or a new one where the car is, if that part of the town is awake. */
    private static @Nullable TownCar body(ServerLevel l,Car c,Vec3 at,float yaw){
        if(c.id!=null&&l.getEntity(c.id) instanceof TownCar body&&body.isAlive())return body;
        var body=make(l,c.kind,at,yaw);c.id=body==null?null:body.getUUID();return body;
    }
    static @Nullable TownCar make(ServerLevel l,int kind,Vec3 at,float yaw){
        var cell=BlockPos.containing(at);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell))||!l.isPositionEntityTicking(cell))return null;
        var body=BikeNightRegistry.TOWN_CAR.get().create(l);if(body==null)return null;
        body.kind(kind);body.moveTo(at.x,at.y,at.z,yaw,0);body.place(at,yaw);
        return l.addFreshEntity(body)?body:null;
    }
    /** The light before a car: on the road ahead of its nose, and nowhere when it has no body. */
    private static void beam(ServerLevel l,Car c,@Nullable BlockPos to){
        if(c.beam!=null&&!c.beam.equals(to)){
            if(l.isLoaded(c.beam)&&l.getBlockState(c.beam).is(BikeNightRegistry.BEAM.get()))l.setBlock(c.beam,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
            c.beam=null;
        }
        if(to!=null&&c.beam==null&&l.isLoaded(to)&&l.getBlockState(to).isAir()){l.setBlock(to,BikeBeamBlock.of(13),Block.UPDATE_CLIENTS);c.beam=to;}
    }
    /** The night over: no car has a body or a light, and each is back where it starts. */
    static void reset(ServerLevel l,BikeNight.Night n){
        for(var c:n.traffic.cars){beam(l,c,null);if(c.id!=null&&l.getEntity(c.id) instanceof TownCar body)body.discard();}
        for(var body:l.getEntitiesOfClass(TownCar.class,BikeNightBullies.area(n.base)))if(BikeNight.nightAt(l,body.blockPosition())==n)body.discard();
        n.traffic=new Traffic();
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** The cars start now, whether or not there is a chase. */
    public static void startNow(BlockPos b){var n=BikeNight.night(b);if(n!=null)n.traffic.going=true;}
    public static boolean going(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.traffic.going;}
    /** Which way round a car goes, how far along it is, and how fast it is going. */
    public static int roundOf(int car){return ROUND_OF[car];}
    public static double along(BlockPos b,int car){var n=BikeNight.night(b);return n==null?0:n.traffic.cars[car].at;}
    public static float speed(BlockPos b,int car){var n=BikeNight.night(b);return n==null?0:n.traffic.cars[car].speed;}
    /** A car is put that far along its way, standing. */
    public static void put(BlockPos b,int car,double along){put(b,car,along,0);}
    /** A car is put that far along its way, going that fast. */
    public static void put(BlockPos b,int car,double along,float speed){
        var n=BikeNight.night(b);if(n==null)return;var c=n.traffic.cars[car];double length=ROUNDS[c.round].length;
        c.at=((along%length)+length)%length;c.speed=speed;
    }
    public static @Nullable TownCar body(ServerLevel l,BlockPos b,int car){var n=BikeNight.night(b);return n!=null&&n.traffic.cars[car].id!=null&&l.getEntity(n.traffic.cars[car].id) instanceof TownCar body&&body.isAlive()?body:null;}
    public static @Nullable BlockPos light(BlockPos b,int car){var n=BikeNight.night(b);return n==null?null:n.traffic.cars[car].beam;}
    public static int told(BlockPos b){var n=BikeNight.night(b);return n==null?0:n.traffic.told;}
    public static @Nullable TownCar parked(ServerLevel l,BlockPos b,int i){var n=BikeNight.night(b);return n!=null&&n.traffic.parked[i]!=null&&l.getEntity(n.traffic.parked[i]) instanceof TownCar body&&body.isAlive()?body:null;}
}
