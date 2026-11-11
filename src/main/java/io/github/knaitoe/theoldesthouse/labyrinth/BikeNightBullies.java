package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.479, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "The bullies arrive"): Tully, who leads; Ray-Ray; and Duff. Children, a little
 * bigger than the readers (the owner: "i'd probably assume him/the rest of the cast of playground kids and bullies as such").
 *
 * <p>After a few minutes of play (the night's own clock, which runs only while somebody is in the park) a car goes by on Park Street
 * and its headlights sweep the railings. Then the three ride up on their bikes (PackBike). The padlock on the gate is Tully's; he
 * undoes it, and the gate stands open from then on. They ride in and circle the playground, jeering. Then they get off: Tully takes
 * Gus's cap and wears it; Ray-Ray kicks sand; Duff sits on an end of the seesaw and keeps it down, so that anybody on the other end
 * is kept up.
 *
 * <p>A ball of sand that meets one of them stops him where he is, coughing, for a moment. Each one that lands makes them angrier. A
 * few hits, or enough time, and Tully says "Get them." They go for their bikes, and come after the readers.
 *
 * <p>Everything they say is a caption over their heads and never chat. Their threat is humiliation and never injury: nothing here
 * hurts anybody. What being caught costs comes with the town the chase goes through; until then they only give chase.
 */
public final class BikeNightBullies {
    private BikeNightBullies(){}
    /** Stranger phases: Tully, Ray-Ray, Duff; and Tully in Gus's cap. */
    public static final int TULLY=168,RAY=169,DUFF=170,TULLY_CAP=171;
    public static final String[] NAMES={"Tully","Ray-Ray","Duff"};
    public static boolean cast(int phase){return phase>=TULLY&&phase<=TULLY_CAP;}
    /** Which of the three: 0 Tully, 1 Ray-Ray, 2 Duff. */
    public static int who(int phase){return phase==TULLY_CAP?0:Mth.clamp(phase-TULLY,0,2);}
    public static String skin(int phase){return switch(phase){case RAY->"bike_ray";case DUFF->"bike_duff";case TULLY_CAP->"bike_tully_cap";default->"bike_tully";};}
    /** How big each is, against a grown body: the readers are .54. */
    public static double size(int phase){return switch(who(phase)){case 1->.6;case 2->.67;default->.63;};}
    public static boolean is(@Nullable Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    /** Their gestures: on a bike; pointing; coughing into both hands; kicking sand; arms folded; reaching for something. */
    public static final int RIDE=1,POINT=2,COUGH=3,KICK=4,ARMS=5,REACH=6;
    /** The night's stages: before them; the car; coming up the street; at the gate; circling and then on foot; told to go; the chase. */
    public static final int QUIET=0,SWEEP=1,COMING=2,AT_GATE=3,CIRCLING=4,LOITER=5,GET_THEM=6,CHASE=7;
    /** What one of them on foot is doing: going to where he will stand; there. */
    public static final int TO_POST=0,POSTED=1;
    /**
     * In ticks: how long the park is played in before they come; how long the car's lights take to go by, and the gap after; how long
     * they may take to reach the gate; how long Tully is at the padlock; how long a hit stops one of them; how long they stand about
     * before Tully has had enough; the gap between jeers and how much longer it may be; the gap between Ray-Ray's kicks; how long one
     * of them may take to walk somewhere, and to get back to his bike.
     */
    public static final int ARRIVE=3600,SWEEP_TICKS=70,SWEEP_GAP=30,COMING_MOST=320,UNLOCK=60,STUN=60,PATIENCE=1800,JEER_EVERY=110,JEER_MORE=70,KICK_EVERY=150,WALK_MOST=400,MOUNT_MOST=240;
    /** How many times round the playground they go, and how many balls of sand it takes. */
    public static final int LAPS=2,HITS=3;
    /** Blocks a tick: riding up, circling, and in the chase; walking, and running for a bike. */
    public static final float APPROACH=.27F,CIRCLE=.24F,HUNT=.3F;
    public static final double WALK=.12,RUN=.2;
    /** How near a reader must be for one of them to jeer at them, in blocks. */
    public static final double HEARD=26;
    /** For tests: they never come; and the two waits, which a case shortens. */
    public static boolean keptAway;
    public static int arriveAt=ARRIVE,patience=PATIENCE;

    /** Where they come from, east along Park Street; where each waits at the gate; and the way in. All relative to the park's base. */
    static final double START_X=30.5,START_Z=-50.5;
    static final Vec3[] WAIT={new Vec3(-.5,0,-46.7),new Vec3(.7,0,-49.4),new Vec3(-1.7,0,-51)};
    static final Vec3 GATE_OUT=new Vec3(-.5,0,-47.8),GATE_IN=new Vec3(-.5,0,-43.5);
    /** Round the playground: down the path's north end, west along the railings, south behind the swings, east below the sand, and up the path. */
    static final Vec3[] LOOP={new Vec3(-.5,0,-37.5),new Vec3(-29.5,0,-37.5),new Vec3(-29.5,0,-9.5),new Vec3(-.5,0,-9.5)};
    /** Where each leaves his bike: Tully on the side path, the other two at the path's east edge. */
    static final Vec3[][] PARK={{new Vec3(-.5,0,-17.5),new Vec3(-1.6,0,-20.4),new Vec3(-3.4,0,-20.5)},{new Vec3(.8,0,-30.5)},{new Vec3(.8,0,-13.5)}};
    /** Where Tully and Ray-Ray stand on the sand. Duff's place is the seesaw. */
    static final Vec3[] POST={new Vec3(-13.5,0,-23.5),new Vec3(-13.5,0,-28.5)};

    /** One of the three. */
    static final class Bully {
        final int who;@Nullable UUID id,bike;boolean riding;int doing=TO_POST,around=1;
        final ArrayDeque<Vec3> route=new ArrayDeque<>();@Nullable Vec3 goal;
        long startAt,stunnedUntil=-1,gestureUntil=-1,actAt=-1,walkSince=-1,soonAt,aroundUntil=-1;
        @Nullable String soon;
        Bully(int who){this.who=who;}
    }
    /** A night's three. */
    static final class Pack {
        int stage=QUIET,hits,jeer,jeers,touched;long since,nextJeer,saidAt=-1,loiterSince=-1;boolean open,capTaken;
        final Bully[] b={new Bully(0),new Bully(1),new Bully(2)};
        @Nullable BlockPos beam;int[] deck={};final Set<String> once=new HashSet<>();
    }

    // ------------------------------------------------------------------------------------------------ for tests, and for the night
    public static int stage(BlockPos b){var n=BikeNight.night(b);return n==null?QUIET:n.pack.stage;}
    public static int hits(BlockPos b){var n=BikeNight.night(b);return n==null?0:n.pack.hits;}
    public static boolean open(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.pack.open;}
    /** One of the three, if he is in the park: 0 Tully, 1 Ray-Ray, 2 Duff. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){var n=BikeNight.night(b);return n==null?null:actor(l,n.pack.b[who]);}
    public static @Nullable PackBike bikeOf(ServerLevel l,BlockPos b,int who){var n=BikeNight.night(b);return n==null?null:bike(l,n.pack.b[who]);}
    static @Nullable LiteraryActor actor(ServerLevel l,Bully u){return u.id!=null&&l.getEntity(u.id) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static @Nullable PackBike bike(ServerLevel l,Bully u){return u.bike!=null&&l.getEntity(u.bike) instanceof PackBike p&&p.isAlive()?p:null;}
    static AABB area(BlockPos b){return new AABB(b).inflate(110,70,310);}

    /** The night over, or beginning: none of them, no bikes of theirs, the car's light out, and the gate shut and chained. */
    static void reset(ServerLevel l,BikeNight.Night n){
        light(l,n.pack,null);
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(n.base),BikeNightBullies::is))if(BikeNight.nightAt(l,old.blockPosition())==n){old.stopRiding();old.discard();}
        for(var old:l.getEntitiesOfClass(PackBike.class,area(n.base)))if(BikeNight.nightAt(l,old.blockPosition())==n)old.discard();
        if(l.isLoaded(n.base.offset(0,0,BikeNightSite.NORTH))&&BikeNightSite.gateIsOpen(l,n.base))BikeNightSite.gateShut(l,n.base);
        n.pack=new Pack();
    }

    // ------------------------------------------------------------------------------------------------ each tick
    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var k=n.pack;if(keptAway)return;
        switch(k.stage){
            case QUIET->{if(n.tick>=arriveAt){k.stage=SWEEP;k.since=n.tick;
                l.playSound(null,n.base.offset(0,1,BikeNightSite.LINE),BikeNightRegistry.CAR_PASS.get(),SoundSource.AMBIENT,4F,1F);
                BikeNightGus.saw(n,"car",BikeNightTexts.GUS_CAR);}}
            case SWEEP->sweep(l,n,k);
            case COMING->coming(l,n,k);
            case AT_GATE->atGate(l,n,k,here);
            case CIRCLING,LOITER->playground(l,n,k,here);
            case GET_THEM->getThem(l,n,k,here);
            case CHASE->chase(l,n,k,here);
            default->{}
        }
        if(k.stage>=COMING)each(l,n,k,here);
    }

    /** The car: a light going west along the near side of the road, and gone. */
    static void sweep(ServerLevel l,BikeNight.Night n,Pack k){
        long t=n.tick-k.since;int from=BikeNightSite.FAR_EAST-4,to=BikeNightSite.FAR_WEST+4;
        light(l,k,t<=SWEEP_TICKS?n.base.offset(Mth.floor(from+(to-from)*(double)t/SWEEP_TICKS),1,BikeNightSite.LINE+2):null);
        if(t>=SWEEP_TICKS+SWEEP_GAP&&spawn(l,n,k)){k.stage=COMING;k.since=n.tick;}
    }
    static void light(ServerLevel l,Pack k,@Nullable BlockPos to){
        if(k.beam!=null&&!k.beam.equals(to)){if(l.isLoaded(k.beam)&&l.getBlockState(k.beam).is(BikeNightRegistry.BEAM.get()))l.setBlock(k.beam,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);k.beam=null;}
        if(to!=null&&k.beam==null&&l.isLoaded(to)&&l.getBlockState(to).isAir()){l.setBlock(to,BikeBeamBlock.of(14),Block.UPDATE_CLIENTS);k.beam=to;}
    }
    /** The three, on their bikes, east along the street; or at the gate itself, if the street that far off is not there to ride on. */
    static boolean spawn(ServerLevel l,BikeNight.Night n,Pack k){
        var far=BlockPos.containing(BikeNightGus.at(n,new Vec3(START_X+6,0,START_Z)));
        boolean street=l.isLoaded(far)&&l.areEntitiesLoaded(ChunkPos.asLong(far));
        for(var u:k.b){
            var at=BikeNightGus.at(n,street?new Vec3(START_X+u.who*2.6,0,START_Z-(u.who==1?1:0)):WAIT[u.who]);
            var bike=BikeNightRegistry.PACK_BIKE.get().create(l);var a=LiteraryRegistry.ACTOR.get().create(l);if(bike==null||a==null)return false;
            float yaw=street?90:0;
            bike.look(u.who);bike.moveTo(at.x,at.y,at.z,yaw,0);
            a.bind(BikeNight.PLACE,null);a.appearance(LiteraryActor.STRANGER,TULLY+u.who);a.moveTo(at.x,at.y,at.z,yaw,0);
            if(!l.addFreshEntity(bike)||!l.addFreshEntity(a))return false;
            a.startRiding(bike,true);a.gesture(RIDE);
            u.id=a.getUUID();u.bike=bike.getUUID();u.riding=true;u.route.clear();u.goal=null;
            if(street){u.route.add(new Vec3(3,0,START_Z-u.who*.5));u.route.add(WAIT[u.who]);}
        }
        return true;
    }
    static void coming(ServerLevel l,BikeNight.Night n,Pack k){
        boolean there=true;
        for(var u:k.b){
            var bike=bike(l,u);if(bike==null){there=false;continue;}
            boolean done=ride(n,u,bike,APPROACH);
            if(!done&&n.tick-k.since>COMING_MOST){var to=BikeNightGus.at(n,WAIT[u.who]);bike.moveTo(to.x,to.y,to.z,0,0);u.route.clear();u.goal=null;bike.toward(null,0);done=true;}
            there&=done;
        }
        if(n.tick-k.since==30)BikeNightGus.saw(n,"them",BikeNightTexts.GUS_THEM);
        if(there){k.stage=AT_GATE;k.since=n.tick;}
    }
    /** Tully undoes his padlock, and the gate is open. */
    static void atGate(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> here){
        long t=n.tick-k.since;var tully=actor(l,k.b[0]);
        if(t==5&&tully!=null){say(tully,0,BikeNightTexts.TULLY_GATE);tully.gesture(REACH);}
        if(t==UNLOCK-25)l.playSound(null,n.base.offset(0,1,BikeNightSite.NORTH),BikeNightRegistry.PADLOCK.get(),SoundSource.BLOCKS,2.5F,1F);
        if(t<UNLOCK)return;
        BikeNightSite.gateOpen(l,n.base);k.open=true;
        if(tully!=null)tully.gesture(RIDE);
        BikeNightGus.saw(n,"gate",BikeNightTexts.GUS_GATE);
        for(var p:here){BikeNight.cue(p,BikeNightTexts.GATE_OPENED);var own=BikeNight.own(p);if(!own.getBoolean("Bullies")){own.putBoolean("Bullies",true);BikeNight.save(p,own);}}
        for(var u:k.b){
            u.route.clear();u.goal=null;u.startAt=n.tick+u.who*25L;
            u.route.add(GATE_OUT);u.route.add(GATE_IN);
            for(int lap=0;lap<LAPS;lap++)for(var corner:LOOP)u.route.add(corner);
            for(var stop:PARK[u.who])u.route.add(stop);
        }
        k.stage=CIRCLING;k.since=n.tick;k.nextJeer=n.tick+50;
    }
    /** Round the playground, and then on it. */
    static void playground(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> here){
        boolean posted=true;
        for(var u:k.b){
            var a=actor(l,u);var bike=bike(l,u);if(a==null||bike==null)continue;
            if(u.riding){
                posted=false;
                if(n.tick<u.stunnedUntil||n.tick<u.startAt){bike.toward(null,0);continue;}
                if(ride(n,u,bike,CIRCLE)){dismount(u,a,bike);u.doing=TO_POST;u.walkSince=-1;}
                continue;
            }
            if(n.tick<u.stunnedUntil){posted&=u.doing==POSTED;continue;}
            switch(u.who){case 0->tully(l,n,k,u,a,here);case 1->ray(l,n,u,a,here);default->duff(l,n,k,u,a);}
            posted&=u.doing==POSTED;
        }
        if(k.stage==CIRCLING&&posted){k.stage=LOITER;k.loiterSince=n.tick;}
        if(k.hits>=HITS||k.stage==LOITER&&n.tick-k.loiterSince>=patience){k.stage=GET_THEM;k.since=n.tick;k.saidAt=-1;}
    }
    /** Tully: Gus's cap, and then the middle of the sand. */
    static void tully(ServerLevel l,BikeNight.Night n,Pack k,Bully u,LiteraryActor a,List<ServerPlayer> here){
        if(!k.capTaken){
            var gus=BikeNightGus.body(l,n);
            if(gus==null||u.walkSince>=0&&n.tick-u.walkSince>WALK_MOST){k.capTaken=true;u.walkSince=-1;return;}
            if(u.actAt<0&&a.distanceToSqr(gus)>1.8*1.8){walk(n,u,a,gus.position(),WALK);return;}
            SceneFootpaths.face(a,gus.position(),180);
            if(u.actAt<0){u.actAt=n.tick+26;gesture(n,u,a,REACH,26);say(a,0,BikeNightTexts.TULLY_HAT);return;}
            if(n.tick<u.actAt)return;
            k.capTaken=true;u.actAt=-1;u.walkSince=-1;a.appearance(LiteraryActor.STRANGER,TULLY_CAP);BikeNightGus.capTaken(l,n);
            u.soon=BikeNightTexts.TULLY_HAT_ON;u.soonAt=n.tick+80;
            for(var p:here){var own=BikeNight.own(p);if(!own.getBoolean("CapTaken")){own.putBoolean("CapTaken",true);BikeNight.save(p,own);}}
            return;
        }
        if(u.doing!=POSTED){if(walk(n,u,a,BikeNightGus.at(n,POST[0]),WALK)){u.doing=POSTED;a.gesture(ARMS);}return;}
        var near=nearest(here,a,HEARD);if(near!=null&&u.gestureUntil<0)SceneFootpaths.face(a,near.position(),12);
    }
    /** Ray-Ray: the sand, and kicking it. */
    static void ray(ServerLevel l,BikeNight.Night n,Bully u,LiteraryActor a,List<ServerPlayer> here){
        if(u.doing!=POSTED){if(walk(n,u,a,BikeNightGus.at(n,POST[1]),WALK)){u.doing=POSTED;u.actAt=n.tick+KICK_EVERY/3;}return;}
        var near=nearest(here,a,HEARD);if(near!=null&&u.gestureUntil<0)SceneFootpaths.face(a,near.position(),12);
        if(n.tick<u.actAt)return;
        u.actAt=n.tick+KICK_EVERY+l.getRandom().nextInt(80);gesture(n,u,a,KICK,16);
        double rad=Math.toRadians(a.getYRot());
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.SAND.defaultBlockState()),a.getX()-Math.sin(rad)*.7,a.getY()+.15,a.getZ()+Math.cos(rad)*.7,28,.35,.12,.35,.08);
        l.playSound(null,a.getX(),a.getY(),a.getZ(),BikeNightRegistry.SAND_KICK.get(),SoundSource.NEUTRAL,.8F,.9F+l.getRandom().nextFloat()*.2F);
    }
    /** Duff: an end of the seesaw, sat on and kept down. With a reader on the other end, they are kept up. */
    static void duff(ServerLevel l,BikeNight.Night n,Pack k,Bully u,LiteraryActor a){
        var board=BikeNightGus.board(l,n);
        if(a.getVehicle() instanceof SeesawSeat seat){
            u.doing=POSTED;
            // Said when he is not in the middle of saying something else: a line is not lost to a jeer.
            if(board!=null&&BikeNightGus.readerOn(board,1-seat.end())&&!a.saying()&&k.once.add("up"))say(a,2,BikeNightTexts.DUFF_UP);
            return;
        }
        if(board==null||u.walkSince>=0&&n.tick-u.walkSince>WALK_MOST){u.doing=POSTED;if(a.gesture()!=ARMS&&u.gestureUntil<0)a.gesture(ARMS);return;}
        boolean on0=board.ridden(0),on1=board.ridden(1);
        if(on0&&on1){
            // Both ends taken: he stands by it until one is not.
            var by=Vec3.atBottomCenterOf(n.base.offset(BikeNightSite.SEESAW)).add(1.7,0,0);
            if(walk(n,u,a,by,WALK)&&!a.saying()&&k.once.add("off"))say(a,2,BikeNightTexts.DUFF_OFF);
            return;
        }
        var end=n.base.offset(BikeNightSite.seesawEnd(on1?0:1));var at=Vec3.atBottomCenterOf(end);
        if(a.position().distanceToSqr(at.x,a.getY(),at.z)>1.9*1.9){walk(n,u,a,at.add(1.2,0,0),WALK);return;}
        if(SeesawSeat.sit(a,end)!=null){a.gesture(ARMS);u.doing=POSTED;u.walkSince=-1;if(k.once.add("mine"))say(a,2,BikeNightTexts.DUFF_MINE);}
    }
    /** "Get them." When Tully has his breath he says it; then each who is on foot runs for his bike. */
    static void getThem(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> here){
        var tully=actor(l,k.b[0]);
        if(k.saidAt<0){
            if(tully!=null&&n.tick<k.b[0].stunnedUntil||tully!=null&&tully.saying()&&n.tick-k.since<120)return;
            k.saidAt=n.tick;
            if(tully!=null){say(tully,0,BikeNightTexts.GET_THEM);if(!k.b[0].riding)gesture(n,k.b[0],tully,POINT,30);}
            BikeNightGus.sawAgain(n,BikeNightTexts.GUS_RUN);
            for(var p:here){BikeNight.cue(p,BikeNightTexts.GET_THEM_CUE);var own=BikeNight.own(p);if(!own.getBoolean("GetThem")){own.putBoolean("GetThem",true);BikeNight.save(p,own);}}
            return;
        }
        boolean up=true;
        for(var u:k.b){
            var a=actor(l,u);var bike=bike(l,u);if(a==null||bike==null)continue;
            if(u.riding){bike.toward(null,0);continue;}
            if(n.tick<u.stunnedUntil||n.tick<k.saidAt+20){up=false;continue;}
            if(a.isPassenger())a.stopRiding();
            if(a.distanceToSqr(bike)<1.5*1.5||n.tick-k.saidAt>MOUNT_MOST){mount(u,a,bike);continue;}
            if(u.gestureUntil<0)a.gesture(0);
            walk(n,u,a,bike.position(),RUN);up=false;
        }
        if(up){k.stage=CHASE;k.since=n.tick;k.nextJeer=n.tick+30;}
    }
    /**
     * The chase, as far as it goes without the town: each rides at a reader, by way of the gate when the railings are between them,
     * and round whatever holds him up. Nothing comes of being reached yet.
     */
    static void chase(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> here){
        var prey=new ArrayList<ServerPlayer>();for(var p:here)if(!p.isSpectator())prey.add(p);
        prey.sort(Comparator.comparing(ServerPlayer::getUUID));
        for(var u:k.b){
            var a=actor(l,u);var bike=bike(l,u);if(a==null||bike==null||!u.riding)continue;
            if(n.tick<u.stunnedUntil||prey.isEmpty()){bike.toward(null,0);continue;}
            // With enough readers each takes one of his own; with one, all three are after them.
            var to=prey.get(u.who%prey.size()).position();
            double fence=n.base.getZ()+BikeNightSite.NORTH+.5;boolean inside=bike.getZ()>fence,theirs=to.z>fence;
            if(inside!=theirs){var gate=BikeNightGus.at(n,inside?GATE_IN:GATE_OUT);to=BikeNightGus.flat(bike.position(),gate)<1.5*1.5?BikeNightGus.at(n,inside?GATE_OUT:GATE_IN):gate;}
            if(bike.heldUp()&&n.tick>=u.aroundUntil){u.aroundUntil=n.tick+30;u.around=-u.around;}
            if(n.tick<u.aroundUntil){double rad=Math.toRadians(bike.getYRot()+u.around*80);to=bike.position().add(-Math.sin(rad)*4,0,Math.cos(rad)*4);}
            bike.toward(to,HUNT);
        }
    }

    // ------------------------------------------------------------------------------------------------ what all of them do
    /** Gestures and coughs run out, a line kept for later is said, a rider looks where his bike points, and now and then one jeers. */
    static void each(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> here){
        for(var u:k.b){
            var a=actor(l,u);if(a==null)continue;
            if(u.gestureUntil>=0&&n.tick>=u.gestureUntil){u.gestureUntil=-1;if(n.tick>=u.stunnedUntil)a.gesture(rest(u,a));}
            if(u.stunnedUntil>=0&&n.tick>=u.stunnedUntil){u.stunnedUntil=-1;a.gesture(rest(u,a));}
            if(u.soon!=null&&n.tick>=u.soonAt&&!a.saying()&&n.tick>=u.stunnedUntil){say(a,u.who,u.soon);u.soon=null;}
            if(a.getVehicle() instanceof PackBike bike){a.setYRot(bike.getYRot());a.setYHeadRot(bike.getYRot());a.setYBodyRot(bike.getYRot());}
        }
        if(k.stage>=CIRCLING&&k.stage!=GET_THEM&&n.tick>=k.nextJeer)jeer(l,n,k,here);
    }
    /** How one of them holds himself with nothing else to do. */
    static int rest(Bully u,LiteraryActor a){return a.getVehicle() instanceof PackBike?RIDE:a.getVehicle() instanceof SeesawSeat||u.doing==POSTED&&u.who!=1?ARMS:0;}
    static void gesture(BikeNight.Night n,Bully u,LiteraryActor a,int which,int ticks){a.gesture(which);u.gestureUntil=n.tick+ticks;}
    static void say(LiteraryActor a,int who,String line){a.say(NAMES[who],line);a.speaks(line);}
    static @Nullable ServerPlayer nearest(List<ServerPlayer> here,Entity from,double within){
        ServerPlayer near=null;double best=within*within;
        for(var p:here){if(p.isSpectator())continue;double d=p.distanceToSqr(from);if(d<best){best=d;near=p;}}
        return near;
    }
    /** The next of them who can, at whoever is nearest him. */
    static void jeer(ServerLevel l,BikeNight.Night n,Pack k,List<ServerPlayer> here){
        k.nextJeer=n.tick+JEER_EVERY+l.getRandom().nextInt(JEER_MORE);
        for(int i=0;i<3;i++){
            var u=k.b[(k.jeer+i)%3];var a=actor(l,u);
            if(a==null||n.tick<u.stunnedUntil||a.saying()||u.soon!=null)continue;
            var near=nearest(here,a,HEARD);if(near==null)continue;
            k.jeer=(k.jeer+i+1)%3;
            say(a,u.who,line(l,k,near));
            if(!u.riding&&!a.isPassenger()&&u.gestureUntil<0)gesture(n,u,a,POINT,24);
            return;
        }
    }
    /** What a reader is on, for a jeer of its own; or nothing. */
    static @Nullable String on(ServerPlayer p){
        var v=p.getVehicle();
        return v instanceof SeatEntity?"swing":v instanceof NightBike?"bike":v instanceof SeesawSeat?"seesaw":v instanceof SlideRide?"slide":v instanceof BarsHang?"bars":v instanceof SpringSeat?"spring":null;
    }
    static String line(ServerLevel l,Pack k,ServerPlayer p){
        if(k.stage==CHASE)return BikeNightTexts.CHASE_JEERS[k.jeers++%BikeNightTexts.CHASE_JEERS.length];
        // The first is the proposal's own; then one for what the reader is on, once each; then the rest, in tonight's order.
        if(k.jeers==0){k.jeers++;return BikeNightTexts.JEERS[0];}
        String about=on(p);if(about!=null&&k.once.add("at_"+about))return BikeNightTexts.jeerAt(about);
        if(k.deck.length==0)k.deck=BikeNightGus.shuffled(l.getRandom(),BikeNightTexts.JEERS.length-1);
        return BikeNightTexts.JEERS[1+k.deck[k.jeers++%k.deck.length]];
    }

    // ------------------------------------------------------------------------------------------------ bikes, and feet
    /**
     * Along a bike's route, a place at a time: the next one is taken up a little before the last is reached, so a corner is ridden
     * round and not stopped at. True once it has reached the last and stopped.
     */
    static boolean ride(BikeNight.Night n,Bully u,PackBike bike,float speed){
        if(u.goal==null)u.goal=u.route.poll();
        if(u.goal==null){bike.toward(null,0);return bike.speedNow()<.02F;}
        var to=BikeNightGus.at(n,u.goal);double d=Math.sqrt(BikeNightGus.flat(bike.position(),to));boolean last=u.route.isEmpty();
        if(d<(last?.6:1.8)||bike.heldUp()&&!last){
            u.goal=u.route.poll();if(u.goal==null){bike.toward(null,0);return false;}
            to=BikeNightGus.at(n,u.goal);d=Math.sqrt(BikeNightGus.flat(bike.position(),to));last=u.route.isEmpty();
        }
        // Into its last place it comes slowly, so that it stops on it.
        bike.toward(to,last?Math.min(speed,(float)Math.max(.07,d*.1)):speed);
        return false;
    }
    /** Off the bike, on its left. */
    static void dismount(Bully u,LiteraryActor a,PackBike bike){
        a.stopRiding();double rad=Math.toRadians(bike.getYRot());
        a.moveTo(bike.getX()+Math.cos(rad)*.8,bike.getY(),bike.getZ()+Math.sin(rad)*.8,bike.getYRot(),0);
        a.gesture(0);u.riding=false;bike.toward(null,0);
    }
    static void mount(Bully u,LiteraryActor a,PackBike bike){
        if(a.isPassenger())a.stopRiding();
        a.startRiding(bike,true);a.gesture(RIDE);u.riding=true;u.gestureUntil=-1;u.route.clear();u.goal=null;u.walkSince=-1;
    }
    /** A step toward somewhere on foot. True when he is there. */
    static boolean walk(BikeNight.Night n,Bully u,LiteraryActor a,Vec3 to,double pace){
        if(u.walkSince<0)u.walkSince=n.tick;
        boolean there=SceneFootpaths.walk(a,new Vec3(to.x,a.getY(),to.z),pace)||BikeNightGus.flat(a.position(),to)<.35*.35;
        if(there)u.walkSince=-1;
        return there;
    }

    // ------------------------------------------------------------------------------------------------ what readers do to them
    /** A ball of sand has met one of them: he stops where he is, coughing, and they are all a little angrier. */
    static void sanded(ServerLevel l,LiteraryActor a,@Nullable Entity thrower){
        var n=BikeNight.nightAt(l,a.blockPosition());if(n==null)return;var k=n.pack;var u=k.b[who(a.phase())];
        if(n.tick<u.stunnedUntil)return;
        u.stunnedUntil=n.tick+STUN;u.gestureUntil=-1;a.gesture(COUGH);k.hits++;
        l.playSound(null,a.getX(),a.getY(),a.getZ(),BikeNightRegistry.COUGH.get(),SoundSource.NEUTRAL,1F,(u.who==2?.85F:u.who==1?1.1F:1F)+l.getRandom().nextFloat()*.08F);
        if(a.getVehicle() instanceof PackBike bike)bike.toward(null,0);
        say(a,u.who,BikeNightTexts.HIT[Math.min(k.hits,BikeNightTexts.HIT.length)-1]);
        if(thrower instanceof ServerPlayer p&&BikeNight.night(p)==n){var own=BikeNight.own(p);own.putInt("BullyHits",own.getInt("BullyHits")+1);BikeNight.save(p,own);}
        BikeNightGus.sawAgain(n,BikeNightTexts.GUS_HIT[Math.min(k.hits,BikeNightTexts.GUS_HIT.length)-1]);
    }
    /** A hand on one of them. */
    public static void talk(ServerPlayer p,LiteraryActor a){
        var n=BikeNight.night(p);if(n==null||!is(a)||a.saying())return;var u=n.pack.b[who(a.phase())];
        if(n.tick<u.stunnedUntil)return;
        say(a,u.who,BikeNightTexts.HANDS_OFF[n.pack.touched++%BikeNightTexts.HANDS_OFF.length]);
    }
}
