package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.485, Bike night: the people on the pavements. The proposal:
 * <ul>
 * <li>"trick-or-treaters in costume on the sidewalks with pillowcases";</li>
 * <li>"Voices: people call from windows and sidewalks, in captions: 'Get off the road!', 'Go, kid, go!', 'Leave 'em alone, Tully!'";</li>
 * <li>of the theater: "A line of costumed teenagers waits outside. As the readers pass, the line cheers and parts for them, then closes
 * on the bullies";</li>
 * <li>of the burger place: "Teenagers sit on the hoods of their cars in the lot. They cheer the readers and boo the bullies."</li>
 * </ul>
 *
 * <p><b>Who.</b> Fourteen, each in a skin of their own (tools/paint_character_skins.py). The proposal would have had Costume Night's
 * skins "where they fit", and they do not: every one of those is the same drowned girl, with a contest number pinned on. Four
 * children out for sweets, each with a pillowcase: a bedsheet ghost and a skeleton, who go from door to door round Elm Court and say
 * "Trick or treat!" at each; a pumpkin and a vampire, who go along Main Street's north pavement and stop at the laundromat's window
 * and the video shop's. Six teenagers in costume in a line down the middle of the pavement in front of the theater. Two who are not
 * dressed up on the hood of each of the two cars parked in the burger place's lot.
 *
 * <p><b>What they do.</b> Any of them a reader rides by cheers, and one of them calls. Any of them one of the three rides by boos,
 * and one of them says what they think of him. The line parts for a reader riding along the pavement, every other one of them to the
 * wall and the rest to the kerb, and is back in its places behind them: one of the three who rides into it is stopped, told there is
 * no cutting, and walked out into the road. A hand on any of them gets a word.
 *
 * <p>They are there from when the gate is opened, as the cars are. All of them are people (LiteraryActor), so what they say is a
 * caption and never chat. Every line is original.
 */
public final class CalderPeople {
    private CalderPeople(){}
    /** Stranger phases, from this one up, in this order: the two pairs of trick-or-treaters, the six in the line, the two on each parked car's hood. */
    public static final int FIRST=172,TRICK=4,LINE_FIRST=4,LINE=6,HOOD_FIRST=10,HOOD=4,COUNT=14;
    private static final String[] SKINS={"ghost","skeleton","pumpkin","vampire","monster","skull","robot","mummy","cat","scarecrow","letterman","denim","sweater","cap"};
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+COUNT;}
    public static String skin(int phase){return "calder_"+SKINS[phase-FIRST];}
    /** The trick-or-treaters are children; the rest are teenagers. */
    public static double size(int phase){return phase-FIRST<TRICK?.56:.8;}
    /** The ghost's sheet hangs to the shins. */
    public static boolean gown(int phase){return phase==FIRST;}
    /** The trick-or-treaters have a pillowcase in one hand, and only the other to wave. */
    public static boolean carries(int phase){return cast(phase)&&phase-FIRST<TRICK;}
    public static boolean hood(int phase){return cast(phase)&&phase-FIRST>=HOOD_FIRST;}
    public static boolean is(@Nullable Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static int who(LiteraryActor a){return a.phase()-FIRST;}
    /** What they are called over what they say. */
    public static String name(int who){return who<TRICK?"Trick-or-treater":who<HOOD_FIRST?"In the line":"On the hood";}
    /** Their gestures: an arm up, or both, for a rider going by; a hand cupped to shout at one of the three, or both. */
    public static final int CHEER=1,BOO=2;
    /** The lots of them, each of which cheers and boos together: the Elm Court pair, the Main Street pair, the line, the hood. */
    public static final int LOTS=4;
    static int lot(int who){return who<2?0:who<TRICK?1:who<HOOD_FIRST?2:3;}
    static int first(int lot){return lot==0?0:lot==1?2:lot==2?LINE_FIRST:HOOD_FIRST;}
    static int count(int lot){return lot<2?2:lot==2?LINE:HOOD;}

    /** A place somebody goes to, and whether they stop there and look that way (in degrees), or NaN to walk on. */
    public record Stop(double x,double z,float look,int say){
        public boolean stops(){return !Float.isNaN(look);}
    }
    private static Stop on(double x,double z){return new Stop(x,z,Float.NaN,-1);}
    private static Stop at(double x,double z,float look){return new Stop(x,z,look,0);}
    private static Stop at(double x,double z,float look,int say){return new Stop(x,z,look,say);}
    /**
     * Where the two pairs go, round and round. Round Elm Court: up the west pavement to the first house's walk and its door, on to
     * the second's, across the turning circle to the house opposite, back down the east pavement by the last, and across the road
     * to where they began. Along Main Street's north pavement: the laundromat's window, the video shop's, the barber's corner, and back.
     */
    public static final Stop[][] ROUTES={
        {on(18,-127),on(18,-132.5),at(10.6,-132.5,90),on(17.5,-132.5),on(16.5,-144.5),at(7.6,-144.5,90),on(16.5,-144.5),on(28,-144.5),at(33.4,-144.5,-90),on(27.5,-144.5),
         on(27,-132.5),at(31.4,-132.5,-90),on(27,-132.5),on(27,-127)},
        {on(7.5,-98.5),at(13,-98.5,180,1),on(20,-98.5),at(23.5,-98.5,180,2),on(30.5,-98.5),on(39.5,-98.5),on(30.5,-98.5),on(20,-98.5)}};
    /**
     * The line: down the middle of the pavement in front of the theater, each looking east at the back of the next. To let a rider
     * by, every other one of them steps to the wall and the rest to the kerb.
     */
    public static final double LINE_Z=-85.1,LINE_WEST=-18.5,LINE_GAP=1.6,WALL_Z=-84.3,KERB_Z=-86.3;
    public static final float LINE_LOOK=-90;
    public static Vec3 lineSpot(int i){return new Vec3(LINE_WEST+i*LINE_GAP,0,LINE_Z);}
    public static Vec3 asideSpot(int i){return new Vec3(LINE_WEST+i*LINE_GAP,0,i%2==0?WALL_Z:KERB_Z);}
    /**
     * Where they sit: two on the hood of each parked car, side by side, looking the way it does, their legs out along it. A
     * hood's top is thirteen pixels up; somebody their size sitting has their hips half a block over where they are.
     */
    public static final double HOOD_TOP=13/16D,HIPS=.5;
    public static Vec3 hoodSpot(int i){var car=CalderTraffic.PARKED[i/2];return new Vec3(car[1]+(i%2==0?-.42:.42),HOOD_TOP-HIPS,car[2]+1.15);}
    public static float hoodLook(int i){return (float)CalderTraffic.PARKED[i/2][3];}

    /**
     * In blocks: how near a rider is for each lot of them to cheer, or one of the three for them to boo; how near the line's ends
     * a rider on the pavement is for it to part, and how far out from the theater's wall "on the pavement" reaches; how near one of
     * the line one of the three is to be stopped by it, and how far he is walked out into the road a tick; how far behind the first
     * of a pair the second walks, how far to its side they stand at a door, and how far behind the second is for the first to wait.
     */
    public static final double[] CHEER_WITHIN={9,9,10,14};
    public static final double BOO_WITHIN=9,PART_WITHIN=7,PAVEMENT_Z=-86.7,HOLD_WITHIN=1.1,OUT_STEP=.07,TRAIL=.8,SIDE=.7,WAIT_FOR=2.2;
    /**
     * In ticks: how long arms stay up; between two cheers from the same people, and two boos; how long a pair stands at a door or a
     * window, how long after they stop one of them speaks, and how long they will try for a place before giving it up for the
     * next, and how long the first of a pair waits for the second; how long the line holds one of the three, and how long before
     * it will hold the same one again.
     */
    public static final int ARMS=36,CHEER_GAP=160,BOO_GAP=200,STAND=90,SAY_AFTER=20,WALK_MOST=500,WAIT_MOST=100,HELD=40,HOLD_GAP=300;
    /** A child's pace along a pavement, and one of the line's stepping aside, in blocks a tick; and the least a bike is doing to be "riding by". */
    public static final double WALK=.085,SHUFFLE=.16;
    public static final float RIDING=.08F;

    /** One of them: their body; where it was last, to tell one that is gone from one that is out of sight; and until when their arms are up. */
    static final class Person {
        final int who;@Nullable UUID id;@Nullable Vec3 last;long armsUntil=-1;
        Person(int who){this.who=who;}
    }
    /**
     * A night's people: each of them; where each pair is on its round, since when it has been making for there, until when it stands,
     * and whether it has spoken; when each lot of them may next cheer or boo, and which line is next; whether the line is parted, and
     * until when it holds each of the three and may next; and how fast the pairs go and how long they stand (for tests).
     */
    static final class People {
        final Person[] all=new Person[COUNT];
        final int[] next={1,1},waited={0,0};final long[] since={0,0},standUntil={-1,-1};final boolean[] said={false,false};final int[] stops={0,0};
        final long[] cheerAt=new long[LOTS],booAt=new long[LOTS];final int[] calls=new int[LOTS];
        boolean parted;final long[] heldUntil={-1,-1,-1},holdAt=new long[3];int talk;
        double pace=WALK;int stand=STAND;
        People(){for(int i=0;i<COUNT;i++)all[i]=new Person(i);}
    }

    // ------------------------------------------------------------------------------------------------ their bodies
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static Vec3 world(BlockPos b,Stop s){return new Vec3(b.getX()+s.x,b.getY(),b.getZ()+s.z);}
    /** Where one of them starts: the pairs at the first place of their round, the second behind the first; the line and the hood in their places. */
    public static Vec3 start(int who){
        if(who<TRICK){var s=ROUTES[who/2][0];var to=ROUTES[who/2][1];double d=Math.hypot(to.x-s.x,to.z-s.z);
            return who%2==0?new Vec3(s.x,0,s.z):new Vec3(s.x-(to.x-s.x)/d*TRAIL,0,s.z-(to.z-s.z)/d*TRAIL);}
        return who<HOOD_FIRST?lineSpot(who-LINE_FIRST):hoodSpot(who-HOOD_FIRST);
    }
    private static boolean live(ServerLevel l,Vec3 at){var cell=BlockPos.containing(at);return l.isLoaded(cell)&&l.areEntitiesLoaded(ChunkPos.asLong(cell))&&l.isPositionEntityTicking(cell);}
    static @Nullable LiteraryActor body(ServerLevel l,BikeNight.Night n,Person p){
        if(p.id!=null&&l.getEntity(p.id) instanceof LiteraryActor a&&a.isAlive()){p.last=a.position();return a;}
        // Not to be found: out of sight where they were last, and still there; or gone, and to be made again where they began.
        if(p.last!=null&&!live(l,p.last)||(n.tick+p.who)%10!=0)return null;
        var at=world(n.base,start(p.who));if(!live(l,at))return null;
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,BikeNightBullies.area(n.base),CalderPeople::is))if(who(old)==p.who&&BikeNight.nightAt(l,old.blockPosition())==n){p.id=old.getUUID();p.last=old.position();return old;}
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(BikeNight.PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+p.who);
        float look=p.who<TRICK?0:p.who<HOOD_FIRST?LINE_LOOK:hoodLook(p.who-HOOD_FIRST);
        a.moveTo(at.x,at.y,at.z,look,0);a.setYHeadRot(look);a.setYBodyRot(look);
        if(p.who>=HOOD_FIRST){a.setNoGravity(true);a.noPhysics=true;}
        if(p.who<TRICK)a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BikeNightRegistry.TREAT_SACK.get()));
        if(!l.addFreshEntity(a))return null;
        p.id=a.getUUID();p.last=at;return a;
    }
    /** The night over, or beginning: none of them is anywhere. */
    static void reset(ServerLevel l,BikeNight.Night n){
        for(var p:n.people.all)if(p.id!=null&&l.getEntity(p.id) instanceof LiteraryActor a)a.discard();
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,BikeNightBullies.area(n.base),CalderPeople::is))if(BikeNight.nightAt(l,old.blockPosition())==n)old.discard();
        n.people=new People();
    }

    // ------------------------------------------------------------------------------------------------ each tick
    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        if(!n.traffic.going)return;
        var k=n.people;var bodies=new LiteraryActor[COUNT];
        for(var p:k.all){
            var a=bodies[p.who]=body(l,n,p);if(a==null)continue;
            if(p.armsUntil>=0&&n.tick>=p.armsUntil){p.armsUntil=-1;a.gesture(0);}
        }
        for(int pair=0;pair<2;pair++)walk(n,k,pair,bodies);
        line(l,n,k,bodies,here);
        for(int i=HOOD_FIRST;i<COUNT;i++)if(bodies[i]!=null){var at=world(n.base,hoodSpot(i-HOOD_FIRST));float look=hoodLook(i-HOOD_FIRST);bodies[i].setPos(at.x,at.y,at.z);bodies[i].setDeltaMovement(Vec3.ZERO);bodies[i].setYRot(look);bodies[i].setYBodyRot(look);}
        calls(l,n,k,bodies,here);
    }
    /** A pair on its round: one behind the other to the next place; at a door or a window they stand side by side a while, and one of them says something. */
    private static void walk(BikeNight.Night n,People k,int pair,LiteraryActor[] bodies){
        var a=bodies[pair*2];var b=bodies[pair*2+1];if(a==null||b==null)return;
        var route=ROUTES[pair];var stop=route[k.next[pair]];var to=world(n.base,stop);
        if(k.standUntil[pair]>=0){
            double rad=Math.toRadians(stop.look);var ahead=new Vec3(-Math.sin(rad),0,Math.cos(rad));var beside=to.add(ahead.z*SIDE,0,-ahead.x*SIDE);
            SceneFootpaths.face(a,a.position().add(ahead.scale(4)),12);
            if(BikeNightGus.flat(b.position(),beside)>.2*.2&&SceneFootpaths.walk(b,beside,k.pace)||BikeNightGus.flat(b.position(),beside)<=.2*.2)SceneFootpaths.face(b,b.position().add(ahead.scale(4)),12);
            if(!k.said[pair]&&n.tick>=k.standUntil[pair]-k.stand+Math.min(SAY_AFTER,k.stand/2)){
                // They take it in turns.
                var one=k.stops[pair]%2==0?a:b;if(!one.saying()){k.said[pair]=true;say(one,BikeNightTexts.AT_A_STOP[stop.say]);}
            }
            if(n.tick>=k.standUntil[pair]){k.standUntil[pair]=-1;k.stops[pair]++;k.next[pair]=(k.next[pair]+1)%route.length;k.since[pair]=n.tick;}
            return;
        }
        // The first waits for one who has fallen behind, for a while; the second hurries to catch up.
        boolean far=BikeNightGus.flat(a.position(),b.position())>WAIT_FOR*WAIT_FOR;
        if(!far)k.waited[pair]=0;
        boolean wait=far&&k.waited[pair]++<WAIT_MOST;
        if(wait){SceneFootpaths.face(a,b.position(),12);k.since[pair]++;}
        boolean there=!wait&&SceneFootpaths.walk(a,to,k.pace);
        double yaw=Math.toRadians(a.getYRot());var behind=wait?a.position():a.position().add(Math.sin(yaw)*TRAIL,0,-Math.cos(yaw)*TRAIL);
        if(BikeNightGus.flat(b.position(),behind)>(wait?TRAIL*TRAIL:.25*.25))SceneFootpaths.walk(b,behind,k.pace*(far?1.5:1.2));
        if(there&&stop.stops()){k.standUntil[pair]=n.tick+k.stand;k.said[pair]=false;return;}
        // A place they cannot get to (somebody's bike across a walk) is given up for the next.
        if(there||n.tick-k.since[pair]>WALK_MOST){k.next[pair]=(k.next[pair]+1)%route.length;k.since[pair]=n.tick;}
    }
    /** The line: parted for a rider on the pavement, back in its places behind them, and no way through for one of the three. */
    private static void line(ServerLevel l,BikeNight.Night n,People k,LiteraryActor[] bodies,List<ServerPlayer> here){
        var b=n.base;double west=b.getX()+LINE_WEST,east=west+(LINE-1)*LINE_GAP;
        boolean rider=false;
        for(var p:here){
            if(p.isSpectator()||!(p.getVehicle() instanceof RiddenBike))continue;
            double x=p.getX(),z=p.getZ()-b.getZ();
            rider|=z>PAVEMENT_Z&&z<WALL_Z+.4&&x>west-PART_WITHIN&&x<east+PART_WITHIN;
        }
        k.parted=rider;
        for(int i=0;i<LINE;i++){
            var a=bodies[LINE_FIRST+i];if(a==null)continue;
            var to=world(b,k.parted?asideSpot(i):lineSpot(i));
            if(BikeNightGus.flat(a.position(),to)>.12*.12)SceneFootpaths.step(a,to,SHUFFLE);
            else{a.setYRot(LINE_LOOK);a.setYBodyRot(LINE_LOOK);a.setYHeadRot(LINE_LOOK);}
        }
        for(var u:n.pack.b){
            var his=BikeNightBullies.bike(l,u);if(his==null||!u.riding||u.leaving||u.gone)continue;
            // One they have stopped is walked out into the road.
            if(n.tick<k.heldUntil[u.who]){his.toward(null,0);if(his.getZ()-b.getZ()>PAVEMENT_Z-1.2)his.setPos(his.getX(),his.getY(),his.getZ()-OUT_STEP);continue;}
            if(k.parted||n.tick<u.stunnedUntil||n.tick<k.holdAt[u.who])continue;
            for(int i=0;i<LINE;i++){
                var a=bodies[LINE_FIRST+i];if(a==null||BikeNightGus.flat(a.position(),his.position())>HOLD_WITHIN*HOLD_WITHIN)continue;
                k.heldUntil[u.who]=n.tick+HELD;k.holdAt[u.who]=n.tick+HOLD_GAP;u.stunnedUntil=Math.max(u.stunnedUntil,n.tick+HELD);his.toward(null,0);
                say(a,BikeNightTexts.NO_CUTTING[u.who]);u.soon=BikeNightTexts.CUT_BACK[u.who];u.soonAt=n.tick+HELD;
                break;
            }
        }
    }
    /** Cheering a rider who goes by, and booing one of the three: each lot of them at most every so often, arms up or hands cupped, one of them calling. */
    private static void calls(ServerLevel l,BikeNight.Night n,People k,LiteraryActor[] bodies,List<ServerPlayer> here){
        for(int lot=0;lot<LOTS;lot++){
            int first=first(lot),count=count(lot);
            LiteraryActor near=null;double least=Double.MAX_VALUE;boolean reader=false;int which=-1;
            for(int i=first;i<first+count;i++){
                var a=bodies[i];if(a==null)continue;
                for(var p:here){
                    if(p.isSpectator()||!(p.getVehicle() instanceof RiddenBike bike)||bike.speedNow()<RIDING)continue;
                    double d=BikeNightGus.flat(a.position(),p.position());
                    if(d<CHEER_WITHIN[lot]*CHEER_WITHIN[lot]&&d<least){least=d;near=a;reader=true;}
                }
            }
            if(near==null)for(int i=first;i<first+count;i++){
                var a=bodies[i];if(a==null)continue;
                for(var u:n.pack.b){
                    var his=BikeNightBullies.bike(l,u);if(his==null||!u.riding||u.leaving||u.gone)continue;
                    double d=BikeNightGus.flat(a.position(),his.position());
                    if(d<BOO_WITHIN*BOO_WITHIN&&d<least){least=d;near=a;which=u.who;}
                }
            }
            if(near==null||n.tick<(reader?k.cheerAt[lot]:k.booAt[lot]))continue;
            if(reader)k.cheerAt[lot]=n.tick+CHEER_GAP;else k.booAt[lot]=n.tick+BOO_GAP;
            for(int i=first;i<first+count;i++){
                var a=bodies[i];if(a==null)continue;
                a.gesture(reader?CHEER:BOO);k.all[i].armsUntil=n.tick+ARMS+(i-first)*3L;
            }
            if(!near.saying()){
                var lines=reader?BikeNightTexts.CHEERS[lot]:BikeNightTexts.BOOS[which];
                say(near,lines[k.calls[lot]++%lines.length]);
            }
        }
    }
    static void say(LiteraryActor a,String line){a.say(name(who(a)),line);a.speaks(line);}
    /** A hand on one of them. */
    public static void talk(ServerPlayer p,LiteraryActor a){
        var n=BikeNight.night(p);if(n==null||!is(a)||a.saying())return;
        var lines=BikeNightTexts.PEOPLE_TALK[lot(who(a))];
        say(a,lines[n.people.talk++%lines.length]);
    }

    // ------------------------------------------------------------------------------------------------ for tests
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){var n=BikeNight.night(b);return n!=null&&n.people.all[who].id!=null&&l.getEntity(n.people.all[who].id) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    public static boolean parted(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.people.parted;}
    /** Which place of its round a pair is going to; whether it is standing at one; and how many it has stood at. */
    public static int next(BlockPos b,int pair){var n=BikeNight.night(b);return n==null?-1:n.people.next[pair];}
    public static boolean standing(BlockPos b,int pair){var n=BikeNight.night(b);return n!=null&&n.people.standUntil[pair]>=0;}
    public static int stops(BlockPos b,int pair){var n=BikeNight.night(b);return n==null?-1:n.people.stops[pair];}
    /** The pairs go this fast and stand this long. */
    public static void hurry(BlockPos b,double pace,int stand){var n=BikeNight.night(b);if(n!=null){n.people.pace=pace;n.people.stand=stand;}}
    public static boolean held(BlockPos b,int who){var n=BikeNight.night(b);return n!=null&&n.tick<n.people.heldUntil[who];}
}
