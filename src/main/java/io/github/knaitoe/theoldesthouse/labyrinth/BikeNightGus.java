package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.476, Bike night (docs/BIKE_NIGHT_PROPOSAL.md): Gus. "Alone, a reader has one friend along, Gus, a character on his own bike,
 * who talks, rides the seesaw with them and keeps up in the chase." And of the playground: "Gus talks the whole time about nothing.
 * The readers are left alone long enough to play." This is Gus at the playground; his bike and the chase come with the chase.
 *
 * <p>He is in every night, one of him, whoever comes (the sheet by the door is his plan, and his is the cap the others will take;
 * a night's readers do not all arrive together, so "alone" could not be decided once). He waits on the sand by the side path. When
 * a reader comes near he says hello, and from then on:
 * <ul>
 * <li>he talks about nothing, a line every little while, never over a line of his own that is still up, and part way through he
 * tells why nobody cuts through the graveyard on Halloween, badly;</li>
 * <li>he says something about what he sees: the bike a reader took, a landing off the swings that is the furthest tonight, a swing
 * going past its widest and over the bar, the slide, the bars, somebody thrown off the seesaw or the merry-go-round, the top of
 * the dome, the horse, the fountain, the payphone, the scratches on the post, and a ball of sand that meets him;</li>
 * <li>he keeps near whoever is playing on the sand, without getting under their feet, and goes back to his place when nobody is;</li>
 * <li>a reader on one end of the seesaw with nobody on the other gets him on the other, and he pushes off the sand when his end
 * comes down, mostly as it lands, which is a bounce;</li>
 * <li>a reader on a swing with nobody else in the park to push them gets him behind it, pushing as it starts away from him. Most
 * of his pushes meet it as it turns, which makes each a kick (HouseReadSwing): with him at the back a reader has only the front end
 * of the arc to time to go over the bar. One in seven of his pushes is late.</li>
 * <li>a hand on him gets an answer.</li>
 * </ul>
 * What he says is a caption over his head and never chat. Every line is original.
 *
 * <p><b>0.4.483, his bike and the chase.</b> His bike stands by the bench off the side path: his brother's, with a flag on a whip
 * (PackBike.GUS). A hand on it gets a word from him. When the three go for their bikes he drops what he is doing and goes for
 * his, and from then on he rides: beside whichever reader is nearest who still has a bike, straight to them when he can see them
 * and by the town's ways (CalderRoutes, BikeNightHunt) when he cannot, stopping a little way off. He says what he sees of the
 * three: one of them close behind, one gone round to wait ahead. The three do not go for him: they have his cap, and it is the
 * readers' bikes they want.
 */
public final class BikeNightGus {
    private BikeNightGus(){}
    /** Stranger phases: Gus in his cap, and without it. */
    public static final int GUS=166,GUS_BARE=167;
    public static final String NAME="Gus";
    public static boolean cast(int phase){return phase==GUS||phase==GUS_BARE;}
    public static String skin(int phase){return phase==GUS_BARE?"bike_gus_bare":"bike_gus";}
    public static boolean is(@Nullable Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    /** His gestures: a wave; pointing; both arms up; both hands out to push; his hands on the seesaw's handle; a shrug; on his bike. */
    public static final int WAVE=1,POINT=2,CHEER=3,PUSH=4,SIT=5,SHRUG=6,RIDE=7;
    /** What he is doing: waiting for somebody to come; at a loose end; going to the seesaw; on it; going to push a swing; pushing it; going to his bike; riding it. */
    public static final int WAITING=0,FREE=1,TO_SEESAW=2,ON_SEESAW=3,TO_SWING=4,PUSHING=5,TO_BIKE=6,RIDING=7;
    /** 0.4.483. Where his bike stands, on the grass by the bench off the side path, and the way it points: at the gate. The middle of the side path. */
    public static final Vec3 BIKE_AT=new Vec3(-3.5,0,-22.6);
    public static final float BIKE_YAW=180;public static final double SIDE_PATH=BikeNightSite.SIDE_S;
    /**
     * In blocks: how near a reader he rides before he stops; how near one of the three riding at them is before he says so; how near
     * one who has gone round is; and how near them the corner he has gone round to.
     */
    public static final double KEEP=3.2,BEHIND_THEM=9,ROUND_THEM=30,AHEAD_THEM=18;
    /** Blocks a tick on his bike: what the three ride at. */
    public static final float RIDE_PACE=.3F;
    /** In ticks: how long he may take to get to his bike before he is on it anyway; and the gap between two things he calls out about the three. */
    public static final int MOUNT_MOST=160,WARN_GAP=160;
    /** Where he waits, on the sand by the side path, and the way he looks from there: toward the path. */
    public static final Vec3 HOME=new Vec3(-8.5,0,-22.5);
    public static final float HOME_YAW=-90;
    /**
     * In blocks: how near a reader comes before he says hello; how far off he lets one playing on the sand get before he goes after
     * them, and how near he then stands; how near a reader must be for him to speak; and how far behind a swing he stands to push it.
     */
    public static final double GREET=9,FAR=7.5,BY=3.5,HEARD=14,BEHIND=3;
    /**
     * In ticks: how long he waits for somebody before he gives up waiting; the gap after a line before he says another about
     * nothing, and how much longer it may be; the gap between two lines of his story; how long a reader is alone on the seesaw, or
     * on a swing, before he goes over; how long he sits on with nobody on the other end; how often he thinks again about where to
     * stand; how long a gesture lasts; how long he keeps trying to get somewhere; and how long he stands unable to get nearer
     * before he gives it up.
     */
    public static final int WAIT_MOST=600,BETWEEN=160,BETWEEN_MORE=220,STORY_GAP=30,ALONE=40,SWING_FIRST=50,OFF_AFTER=50,REPLAN=30,GESTURE=24,TRY=400,STUCK=30;
    /** How many lines about nothing he says before he starts on the graveyard. */
    public static final int STORY_AFTER=4;
    /** His pace, walking and hurrying, in blocks a tick. */
    public static final double WALK=.12,HURRY=.17;
    /** How many ticks after a swing has turned his push comes: one of these, chosen each time. The last is too late to be a kick. */
    static final int[] PUSH_LATE={0,1,1,2,2,3,6};
    /** For tests: he stays where he is and says nothing, so that the playground's own cases have it to themselves. */
    public static boolean keptHome;

    /** A night's Gus: where his body is, what he is doing, and what he has to say. */
    static final class Friend {
        @Nullable UUID id;
        int doing=WAITING,idle,story=-1,talk,sandHits,end;
        /** 0.4.483: his bike; his way through the town; whom he is riding beside; when he may next call out; and how much he has said on the ride. */
        @Nullable UUID bike,friend;final BikeNightHunt.Rider way=new BikeNightHunt.Rider();long warnAt;int rides,warned;int[] rideDeck={};
        /** Whether the last thing he called out was of one gone round; and, for tests, whether he has been told to ride without the three. */
        boolean lastRound,called;
        /** 0.4.507: how far along the Hollow's line he is (CalderHollow.TRAIL), or -1 while he is not on it. */
        double trail=-1;
        /** 0.4.509: he has taken his cap down off the back gate's post. */
        boolean capBack;
        long since,nextIdle,soonAt,replan,gestureUntil=-1,pushAt=-1,aloneSince=-1,swingSince=-1,seesawSince=-1,stuckSince=-1;
        boolean left,wasDown,walking;
        @Nullable Vec3 goal;final ArrayDeque<Vec3> route=new ArrayDeque<>();
        @Nullable UUID with;@Nullable BlockPos seat;
        /** Lines waiting to be said; what he has said once tonight; and whom he has said hello to. */
        final ArrayDeque<String> soon=new ArrayDeque<>();final Set<String> once=new HashSet<>();final Set<UUID> greeted=new HashSet<>();
        int[] deck={};
    }

    // ------------------------------------------------------------------------------------------------ his body
    static Vec3 at(BikeNight.Night n,Vec3 r){return new Vec3(n.base.getX()+r.x,n.base.getY()+r.y,n.base.getZ()+r.z);}
    static AABB area(BlockPos b){return new AABB(b.offset(BikeNightSite.WEST-2,-3,BikeNightSite.NORTH-2)).minmax(new AABB(b.offset(BikeNightSite.EAST+2,14,3)));}
    static double flat(Vec3 a,Vec3 b){double x=a.x-b.x,z=a.z-b.z;return x*x+z*z;}
    /** Gus, if he is in the park. For tests, and for whoever needs to find him. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b){var all=l.getEntitiesOfClass(LiteraryActor.class,area(b),BikeNightGus::is);return all.isEmpty()?null:all.get(0);}
    /** His body: the one this night has, or one found in the park, or a new one at his place once the park is there to stand in. */
    static @Nullable LiteraryActor body(ServerLevel l,BikeNight.Night n){
        var g=n.gus;if(g.id!=null&&l.getEntity(g.id) instanceof LiteraryActor a&&a.isAlive())return a;
        var home=at(n,HOME);var cell=BlockPos.containing(home);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return null;
        var found=l.getEntitiesOfClass(LiteraryActor.class,area(n.base),BikeNightGus::is);
        if(!found.isEmpty()){for(int i=1;i<found.size();i++)found.get(i).discard();g.id=found.get(0).getUUID();return found.get(0);}
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(BikeNight.PLACE,null);a.appearance(LiteraryActor.STRANGER,GUS);
        a.moveTo(home.x,home.y,home.z,HOME_YAW,0);a.setYHeadRot(HOME_YAW);a.setYBodyRot(HOME_YAW);
        if(!l.addFreshEntity(a))return null;
        g.id=a.getUUID();return a;
    }
    /** The night over, or beginning: nobody is left of him but one, at his place, with nothing said. */
    static void reset(ServerLevel l,BikeNight.Night n){
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(n.base),BikeNightGus::is)){old.stopRiding();old.discard();}
        // He may be out in the town, and his bike with him.
        if(n.gus.id!=null&&l.getEntity(n.gus.id) instanceof LiteraryActor out){out.stopRiding();out.discard();}
        if(n.gus.bike!=null&&l.getEntity(n.gus.bike) instanceof PackBike his)his.discard();
        n.gus=new Friend();
    }

    // ------------------------------------------------------------------------------------------------ what he says
    /** A line to say when he next can. He keeps three at most, the newest. */
    static void later(Friend g,BikeNight.Night n,String line,int wait){
        if(g.soon.isEmpty())g.soonAt=Math.max(g.soonAt,n.tick+wait);
        g.soon.add(line);while(g.soon.size()>3)g.soon.poll();
    }
    /** Something he has seen, said of once in a night. */
    static void saw(@Nullable BikeNight.Night n,String key,String line){if(n!=null&&!keptHome&&n.gus.once.add(key))later(n.gus,n,line,20);}
    /** Something he says of every time. */
    static void sawAgain(@Nullable BikeNight.Night n,String line){if(n!=null&&!keptHome)later(n.gus,n,line,20);}
    /** Whether he is near enough to somebody to have seen what they did. */
    static boolean sees(ServerLevel l,BikeNight.Night n,ServerPlayer p,double within){if(keptHome)return false;var a=body(l,n);return a!=null&&a.distanceToSqr(p)<=within*within;}
    /** Both arms up for a moment. */
    static void cheer(@Nullable BikeNight.Night n,ServerLevel l){if(n==null||keptHome)return;var a=body(l,n);if(a!=null&&n.gus.doing!=ON_SEESAW&&n.gus.doing!=PUSHING)gesture(n.gus,n,a,CHEER);}
    static void gesture(Friend g,BikeNight.Night n,LiteraryActor a,int which){if(g.doing==RIDING)return;a.gesture(which);g.gestureUntil=n.tick+GESTURE;}
    /** How he holds himself with nothing else to do. */
    static int rest(Friend g){return g.doing==ON_SEESAW?SIT:g.doing==RIDING?RIDE:0;}
    static int[] shuffled(net.minecraft.util.RandomSource r,int count){
        var out=new int[count];for(int i=0;i<count;i++)out[i]=i;
        for(int i=count-1;i>0;i--){int k=r.nextInt(i+1);int t=out[i];out[i]=out[k];out[k]=t;}
        return out;
    }
    /** His next line about nothing; or, once he has said a few, the next of his story. */
    static String idle(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a){
        if(g.doing==RIDING)return riding(l,g);
        var story=BikeNightTexts.GUS_STORY;
        if(g.story>=0&&g.story<story.length){
            String line=story[g.story++];
            if(g.story==story.length)for(var p:BikeNight.readers(l,n.base))if(p.distanceToSqr(a)<HEARD*HEARD)BikeNight.heardStory(p);
            return line;
        }
        if(g.story<0&&g.idle>=STORY_AFTER){g.story=1;return story[0];}
        if(g.deck.length==0)g.deck=shuffled(l.getRandom(),BikeNightTexts.GUS_IDLE.length);
        return BikeNightTexts.GUS_IDLE[g.deck[g.idle++%g.deck.length]];
    }
    static void speak(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a,@Nullable ServerPlayer near){
        // 0.4.490: at the row of small stones he says nothing, and what he had been about to say is not said later.
        if(BikeNightGraveyard.hushed(n,a.position())){g.soon.clear();return;}
        if(a.saying()||near==null||near.distanceToSqr(a)>HEARD*HEARD)return;
        String line=null;
        if(!g.soon.isEmpty()){if(n.tick>=g.soonAt)line=g.soon.poll();}
        else if(g.doing!=WAITING&&g.doing!=TO_BIKE&&n.tick>=g.nextIdle)line=idle(l,n,g,a);
        if(line==null)return;
        a.say(NAME,line);a.speaks(line);
        int up=Dialogue.ticks(line);boolean telling=g.story>0&&g.story<BikeNightTexts.GUS_STORY.length;
        g.nextIdle=n.tick+up+(telling?STORY_GAP:BETWEEN+l.getRandom().nextInt(BETWEEN_MORE));g.soonAt=n.tick+up+10;
        if(!g.walking&&g.doing!=ON_SEESAW&&g.doing!=PUSHING&&g.doing!=RIDING)SceneFootpaths.face(a,near.position(),180);
    }
    /** A hand on him. He answers, unless he is in the middle of saying something. */
    public static void talk(ServerPlayer p,LiteraryActor actor){
        var n=BikeNight.night(p);if(n==null||!is(actor)||keptHome)return;var g=n.gus;
        if(actor.saying()||BikeNightGraveyard.hushed(n,actor.position()))return;
        String line=g.doing==RIDING?riding(p.serverLevel(),g)
            :BikeNight.own(p).getString("Bike").isEmpty()&&g.once.add("nobike")?BikeNightTexts.GUS_TALK_NO_BIKE:BikeNightTexts.GUS_TALK[g.talk++%BikeNightTexts.GUS_TALK.length];
        actor.say(NAME,line);actor.speaks(line);
        int up=Dialogue.ticks(line);g.nextIdle=Math.max(g.nextIdle,n.tick+up+BETWEEN);g.soonAt=Math.max(g.soonAt,n.tick+up+10);
        if(!g.walking&&g.doing!=ON_SEESAW&&g.doing!=PUSHING&&g.doing!=RIDING)SceneFootpaths.face(actor,p.position(),180);
    }
    /** 0.4.479: Tully has his cap. */
    static void capTaken(ServerLevel l,BikeNight.Night n){var a=body(l,n);if(a!=null)a.appearance(LiteraryActor.STRANGER,GUS_BARE);if(!keptHome)later(n.gus,n,BikeNightTexts.GUS_CAP,20);}
    /**
     * 0.4.509: his cap is on the back gate's post, he has ridden up to it and his friend is there to see. He takes it down, has
     * it on, and says whose it is. It goes in the record of whoever was near enough to see him.
     */
    public static final double CAP_SEEN=24;
    static void capBack(ServerLevel l,BikeNight.Night n){
        var g=n.gus;if(g.capBack)return;g.capBack=true;
        CalderHollow.hungCap(l,n.base,false);
        var a=body(l,n);if(a!=null)a.appearance(LiteraryActor.STRANGER,GUS);
        if(!keptHome)for(var line:BikeNightTexts.GUS_CAP_BACK)later(g,n,line,6);
        var post=at(n,CalderHollow.CAP_BY);
        for(var p:l.players()){
            if(BikeNight.night(p)!=n||p.isSpectator()||p.position().distanceToSqr(post)>CAP_SEEN*CAP_SEEN)continue;
            var own=BikeNight.own(p);own.putBoolean("CapBack",true);BikeNight.save(p,own);
        }
    }
    /** Whether he has his cap back, in this park. For tests. */
    public static boolean capBack(BlockPos b){var n=BikeNight.night(b);return n!=null&&n.gus.capBack;}
    /** A ball of sand has met him. */
    static void sanded(ServerLevel l,LiteraryActor a){
        var n=BikeNight.nightAt(l,a.blockPosition());if(n==null||keptHome)return;var g=n.gus;g.sandHits++;
        if(g.sandHits<=BikeNightTexts.GUS_SAND.length)later(g,n,BikeNightTexts.GUS_SAND[g.sandHits-1],5);
        if(g.doing!=ON_SEESAW&&g.doing!=PUSHING)gesture(g,n,a,SHRUG);
    }

    // ------------------------------------------------------------------------------------------------ each tick
    static void tick(ServerLevel l,BikeNight.Night n,List<ServerPlayer> here){
        var g=n.gus;var a=body(l,n);if(a==null||keptHome)return;
        var bike=bike(l,n,a);
        if(g.gestureUntil>=0&&n.tick>=g.gestureUntil){g.gestureUntil=-1;a.gesture(rest(g));}
        ServerPlayer near=null;double best=Double.MAX_VALUE;
        for(var p:here){if(p.isSpectator())continue;double d=p.distanceToSqr(a);if(d<best){best=d;near=p;}}
        // Hello, to each as they first come near.
        for(var p:here)if(!p.isSpectator()&&!g.greeted.contains(p.getUUID())&&p.distanceToSqr(a)<GREET*GREET){
            boolean first=g.greeted.isEmpty();g.greeted.add(p.getUUID());
            later(g,n,first?BikeNightTexts.GUS_GREETING:BikeNightTexts.GUS_GREET_MORE[g.greeted.size()%BikeNightTexts.GUS_GREET_MORE.length],5);
            if(g.doing!=ON_SEESAW&&g.doing!=PUSHING)gesture(g,n,a,WAVE);
            BikeNight.metGus(p);
            if(g.doing==WAITING){g.doing=FREE;g.since=n.tick;g.nextIdle=n.tick+BETWEEN;}
        }
        if(g.doing==WAITING&&n.tick>=WAIT_MOST){g.doing=FREE;g.since=n.tick;g.nextIdle=n.tick+BETWEEN;}
        // What he sees for himself: a swing going past its widest arc.
        for(var p:here)if(p.getVehicle() instanceof SeatEntity seat&&seat.pumped()&&seat.kicks()>=2)saw(n,"past",BikeNightTexts.GUS_PAST);
        // 0.4.483: when the three go for their bikes, he goes for his.
        if(g.doing<TO_BIKE&&bike!=null&&(g.called||n.pack.stage==BikeNightBullies.GET_THEM&&n.pack.saidAt>=0||n.pack.stage==BikeNightBullies.CHASE))toHisBike(n,g,a);
        speak(l,n,g,a,near);
        switch(g.doing){
            case TO_BIKE->toBike(n,g,a,bike);
            case RIDING->ride(l,n,g,a,bike,here);
            case FREE->free(l,n,g,a,here,near);
            case TO_SEESAW->toSeesaw(l,n,g,a);
            case ON_SEESAW->onSeesaw(l,n,g,a);
            case TO_SWING->toSwing(l,n,g,a);
            case PUSHING->pushing(l,n,g,a);
            default->{}
        }
    }
    static void release(Friend g,LiteraryActor a){g.doing=FREE;g.goal=null;g.route.clear();g.with=null;g.seat=null;g.replan=0;g.walking=false;g.gestureUntil=-1;a.gesture(0);}
    static @Nullable SeesawBlock.Board board(ServerLevel l,BikeNight.Night n){return l.getBlockEntity(n.base.offset(BikeNightSite.SEESAW)) instanceof SeesawBlock.Board b?b:null;}
    static boolean readerOn(SeesawBlock.Board board,int end){var seat=board.seat(end);return seat!=null&&seat.getFirstPassenger() instanceof ServerPlayer;}
    static @Nullable ServerPlayer reader(ServerLevel l,BikeNight.Night n,@Nullable UUID id){
        if(id==null)return null;var p=l.getServer().getPlayerList().getPlayer(id);return p!=null&&BikeNight.night(p)==n?p:null;
    }
    /** On the sand, clear of its edging. */
    static Vec3 keep(Vec3 r){return new Vec3(Mth.clamp(r.x,BikeNightSite.SAND_W+1.5,BikeNightSite.SAND_E+.5),0,Mth.clamp(r.z,BikeNightSite.SAND_N+1.5,BikeNightSite.SAND_S-.5));}
    /**
     * A step toward where he is going, by way of whatever is left of his route. True when he has nowhere left to go: he is there, or
     * he has stood unable to get nearer for a while and has given it up.
     */
    static boolean walk(BikeNight.Night n,Friend g,LiteraryActor a,double pace){
        if(g.goal==null&&!g.route.isEmpty())g.goal=g.route.poll();
        if(g.goal==null){g.walking=false;return true;}
        var to=at(n,g.goal);var before=a.position();
        boolean there=SceneFootpaths.walk(a,new Vec3(to.x,a.getY(),to.z),pace)||flat(a.position(),to)<.3*.3;
        if(there){g.goal=g.route.poll();g.stuckSince=-1;g.walking=g.goal!=null;return g.goal==null;}
        g.walking=true;
        if(a.position().distanceToSqr(before)>1e-5)g.stuckSince=-1;
        else if(g.stuckSince<0)g.stuckSince=n.tick;
        else if(n.tick-g.stuckSince>STUCK){g.goal=null;g.route.clear();g.stuckSince=-1;g.walking=false;return true;}
        return false;
    }
    /** At a loose end: he goes where he is wanted, or keeps near whoever is playing. */
    static void free(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a,List<ServerPlayer> here,@Nullable ServerPlayer near){
        // The seesaw: a reader on one end, and nobody on the other for a moment.
        var board=board(l,n);
        if(board!=null){
            boolean on0=readerOn(board,0),on1=readerOn(board,1);
            if(on0!=on1&&!board.ridden(on0?1:0)){
                if(g.seesawSince<0)g.seesawSince=n.tick;
                else if(n.tick-g.seesawSince>=ALONE){g.end=on0?1:0;g.doing=TO_SEESAW;g.since=n.tick;g.seesawSince=-1;g.goal=null;g.route.clear();return;}
            }else g.seesawSince=-1;
        }
        // The swings: a reader on one, with nobody else in the park who might push them.
        if(here.size()==1&&here.get(0).getVehicle() instanceof SeatEntity seat&&seat.pumped()){
            if(g.swingSince<0)g.swingSince=n.tick;
            else if(n.tick-g.swingSince>=SWING_FIRST){
                g.with=here.get(0).getUUID();g.seat=seat.support();g.doing=TO_SWING;g.since=n.tick;g.swingSince=-1;
                // Round the south end of the frame, not through it: where its seats swing is no place to walk.
                var r=g.seat.subtract(n.base);var back=BikeNightSite.SWING_FORWARD.getOpposite();
                double behind=BikeNightSite.SWING_X+.5+back.getStepX()*BEHIND,south=BikeNightSite.SWING_POSTS[BikeNightSite.SWING_POSTS.length-1]+1.6;
                g.goal=null;g.route.clear();
                if(BikeNight.rel(a.position(),n.base).x>BikeNightSite.SWING_X+.5){g.route.add(new Vec3(BikeNightSite.SWING_X+2,0,south));g.route.add(new Vec3(behind,0,south));}
                g.route.add(new Vec3(behind,0,r.getZ()+.5));
                if(g.once.add("offer"))later(g,n,BikeNightTexts.GUS_SWING_OFFER,0);
                return;
            }
        }else g.swingSince=-1;
        // Otherwise: near whoever is playing on the sand, or back to his place.
        if(n.tick>=g.replan){
            g.replan=n.tick+REPLAN;Vec3 want=null;var me=BikeNight.rel(a.position(),n.base);
            if(near!=null){
                var r=BikeNight.rel(near.position(),n.base);
                boolean playing=r.x>=BikeNightSite.SAND_W-3&&r.x<=BikeNightSite.SAND_E+4&&r.z>=BikeNightSite.SAND_N-3&&r.z<=BikeNightSite.SAND_S+3;
                if(playing){
                    double d=Math.sqrt(flat(r,me));
                    // Once he has set off after somebody he keeps on until he is beside them, not only until they are in reach again.
                    if(d>(g.goal!=null?BY+.6:FAR)){
                        // Off to one side of somebody on a swing, out of the way they come off it.
                        if(near.getVehicle() instanceof SeatEntity seat&&seat.swinging())want=keep(new Vec3(BikeNightSite.SWING_X+2.5,0,r.z+(me.z>r.z?2.4:-2.4)));
                        else want=keep(new Vec3(r.x+(me.x-r.x)*BY/d,0,r.z+(me.z-r.z)*BY/d));
                    }
                }else if(flat(me,HOME)>1)want=HOME;
            }else if(flat(me,HOME)>1)want=HOME;
            g.route.clear();g.goal=want;g.stuckSince=-1;
        }
        boolean standing=walk(n,g,a,WALK);
        if(standing&&near!=null&&!a.saying())SceneFootpaths.face(a,near.position(),6);
    }
    /** To the free end of the seesaw, and onto it. */
    static void toSeesaw(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a){
        var board=board(l,n);
        if(board==null||!readerOn(board,1-g.end)||board.ridden(g.end)||n.tick-g.since>TRY){release(g,a);return;}
        var end=BikeNightSite.seesawEnd(g.end);var side=BikeNightSite.SEESAW_FACING.getClockWise();
        var beside=new Vec3(end.getX()+.5+side.getStepX()*SeesawSeat.ASIDE,0,end.getZ()+.5+side.getStepZ()*SeesawSeat.ASIDE);
        if(g.goal==null&&flat(BikeNight.rel(a.position(),n.base),beside)>.5*.5&&g.stuckSince<0)g.goal=beside;
        if(!walk(n,g,a,WALK+.02))return;
        var seat=flat(BikeNight.rel(a.position(),n.base),beside)<2*2?SeesawSeat.sit(a,n.base.offset(end)):null;
        if(seat==null){release(g,a);return;}
        g.doing=ON_SEESAW;g.since=n.tick;g.wasDown=false;g.pushAt=-1;g.aloneSince=-1;g.gestureUntil=-1;a.gesture(SIT);
        saw(n,"seesaw",BikeNightTexts.GUS_SEESAW_ON);
    }
    /** On the seesaw: he pushes off when his end comes down, and gets off when he has been left on it. */
    static void onSeesaw(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a){
        var seat=a.getVehicle() instanceof SeesawSeat s?s:null;var board=seat==null?null:seat.board();
        if(seat==null||board==null){a.stopRiding();release(g,a);return;}
        if(!readerOn(board,1-seat.end())){
            if(g.aloneSince<0)g.aloneSince=n.tick;
            else if(n.tick-g.aloneSince>OFF_AFTER){a.stopRiding();release(g,a);saw(n,"seesawoff",BikeNightTexts.GUS_SEESAW_OFF);}
            return;
        }
        g.aloneSince=-1;
        boolean down=board.down(seat.end());
        // As his end lands, most times, which is a bounce; now and then a moment after, which is only a push.
        if(down&&!g.wasDown)g.pushAt=n.tick+(l.getRandom().nextInt(10)<7?l.getRandom().nextInt(3):6+l.getRandom().nextInt(6));
        g.wasDown=down;
        if(down&&g.pushAt>=0&&n.tick>=g.pushAt){g.pushAt=-1;seat.pushed();}
    }
    /** Round behind the swing. */
    static void toSwing(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a){
        var rider=reader(l,n,g.with);
        if(rider==null||!(rider.getVehicle() instanceof SeatEntity seat)||!seat.pumped()||!seat.support().equals(g.seat)||n.tick-g.since>TRY){release(g,a);return;}
        if(!walk(n,g,a,HURRY))return;
        var r=g.seat.subtract(n.base);var back=BikeNightSite.SWING_FORWARD.getOpposite();
        if(flat(BikeNight.rel(a.position(),n.base),new Vec3(BikeNightSite.SWING_X+.5+back.getStepX()*BEHIND,0,r.getZ()+.5))>1.2*1.2){release(g,a);return;}
        g.doing=PUSHING;g.since=n.tick;g.left=false;g.pushAt=-1;
    }
    /** Behind the swing, pushing as it starts away from him. */
    static void pushing(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a){
        var rider=reader(l,n,g.with);
        if(rider==null||!(rider.getVehicle() instanceof SeatEntity seat)||!seat.pumped()||!seat.support().equals(g.seat)){release(g,a);return;}
        SceneFootpaths.face(a,rider.position(),20);
        boolean push=false;
        if(seat.arc()<HouseReadSwing.STILL)push=true;   // hanging still: he starts it
        else{
            boolean leaving=seat.turning(0)<0&&seat.swingAngle(0)<0&&Math.cos(seat.swung(0)*HouseReadSwing.RATE)>0;
            if(leaving&&!g.left){g.left=true;g.pushAt=n.tick+PUSH_LATE[l.getRandom().nextInt(PUSH_LATE.length)];}
            if(!leaving)g.left=false;
            if(leaving&&g.pushAt>=0&&n.tick>=g.pushAt){g.pushAt=-1;push=true;}
        }
        if(!push)return;
        if(seat.push(a.getUUID(),-1)!=SeatEntity.Push.SENT)return;
        gesture(g,n,a,PUSH);g.gestureUntil=n.tick+8;
        l.playSound(null,seat.getX(),seat.getY()+1.5,seat.getZ(),BikeNightRegistry.SWING_CREAK.get(),SoundSource.BLOCKS,.5F,1.15F);
        BikeNight.gusPushed(rider,seat.firstFrom(a.getUUID()));
        if(seat.reach()>=HouseReadSwing.HIGH-.02)saw(n,"high",BikeNightTexts.GUS_HIGH);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.483: his bike, and the chase
    /** His bike: the one this night has, or a new one where he leaves it. It is never saved, so one that has gone from under him is made again where he is. */
    static @Nullable PackBike bike(ServerLevel l,BikeNight.Night n,LiteraryActor a){
        var g=n.gus;if(g.bike!=null&&l.getEntity(g.bike) instanceof PackBike b&&b.isAlive())return b;
        var at=g.doing==RIDING?a.position():at(n,BIKE_AT);var cell=BlockPos.containing(at);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return null;
        var b=BikeNightRegistry.PACK_BIKE.get().create(l);if(b==null)return null;
        b.look(PackBike.GUS);b.moveTo(at.x,at.y,at.z,g.doing==RIDING?a.getYRot():BIKE_YAW,0);
        if(!l.addFreshEntity(b))return null;
        g.bike=b.getUUID();return b;
    }
    /** A hand on his bike while it stands there. He says whose it is, once. */
    static void hisBike(ServerPlayer p){saw(BikeNight.night(p),"hisbike",BikeNightTexts.GUS_HIS_BIKE);}
    /** He drops what he is doing and makes for his bike: off the sand by the side path if he is on it, and up behind it. */
    static void toHisBike(BikeNight.Night n,Friend g,LiteraryActor a){
        if(a.isPassenger())a.stopRiding();
        release(g,a);g.doing=TO_BIKE;g.since=n.tick;
        if(BikeNight.rel(a.position(),n.base).x<BikeNightSite.SAND_E+1)g.route.add(new Vec3(BikeNightSite.SAND_E+.4,0,SIDE_PATH));
        g.route.add(new Vec3(BIKE_AT.x-1.4,0,SIDE_PATH));g.route.add(new Vec3(BIKE_AT.x,0,BIKE_AT.z+1.2));
    }
    /** To his bike, and onto it. If he cannot get to it he is on it anyway, as the three are on theirs. */
    static void toBike(BikeNight.Night n,Friend g,LiteraryActor a,@Nullable PackBike bike){
        if(bike==null)return;
        if(flat(a.position(),bike.position())>1.6*1.6&&n.tick-g.since<MOUNT_MOST){
            if(walk(n,g,a,HURRY)){g.route.clear();g.goal=BikeNight.rel(bike.position(),n.base);}
            return;
        }
        g.goal=null;g.route.clear();g.walking=false;g.gestureUntil=-1;
        a.startRiding(bike,true);a.gesture(RIDE);g.doing=RIDING;g.since=n.tick;g.nextIdle=n.tick+80;g.warnAt=n.tick+60;
    }
    /** His next line about nothing, as he rides. */
    static String riding(ServerLevel l,Friend g){
        if(g.rideDeck.length==0)g.rideDeck=shuffled(l.getRandom(),BikeNightTexts.GUS_RIDE.length);
        return BikeNightTexts.GUS_RIDE[g.rideDeck[g.rides++%g.rideDeck.length]];
    }
    /**
     * On his bike: beside whichever reader is nearest who still has a bike (and the one he was beside before another the same
     * distance off), stopping a little way from them. In the park and on its street he rides at them, by the gate; in the town,
     * straight at them if he can see them and by the town's ways if he cannot. With nobody left to ride beside he stops where he is.
     */
    static void ride(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a,@Nullable PackBike bike,List<ServerPlayer> here){
        if(bike==null)return;
        if(a.getVehicle()!=bike){a.startRiding(bike,true);a.gesture(RIDE);}
        a.setYRot(bike.getYRot());a.setYHeadRot(bike.getYRot());a.setYBodyRot(bike.getYRot());
        ServerPlayer friend=null;double best=Double.MAX_VALUE;
        for(var p:BikeNightHunt.prey(n.pack,here)){double d=flat(BikeNightFold.near(n.base,bike.position(),p.position()),bike.position())*(p.getUUID().equals(g.friend)?.5:1);if(d<best){best=d;friend=p;}}
        if(friend==null){
            bike.toward(null,0);
            if(!n.pack.caught.isEmpty()&&g.once.add("nobody"))later(g,n,BikeNightTexts.GUS_NOBODY,40);
            return;
        }
        g.friend=friend.getUUID();
        // 0.4.521: his friend where he would get to them, through the cemetery's fold if they are on its other side.
        var at=bike.position();var there=BikeNightFold.near(n.base,at,friend.position());double d=Math.sqrt(flat(at,there));boolean level=Math.abs(at.y-there.y)<1.2;
        // 0.4.507: after a friend who is past the hill's edge, he rides the Hollow's plain way, and says nothing.
        if(BikeNightHollow.gus(l,n,g,bike,friend,there,d))return;
        // 0.4.490: in the cemetery, or after a friend who is, he goes through the stones (BikeNightGraveyard).
        if(BikeNightGraveyard.friend(l,n,g.way,bike,there,d)){warn(l,n,g,a,friend);return;}
        if(d<KEEP&&level)bike.toward(null,0);
        else if(!BikeNightHunt.inTown(n,at)&&!BikeNightHunt.inTown(n,there)){g.way.plan=null;BikeNightBullies.parkRide(n,g.way,bike,there,RIDE_PACE);}
        else if(d<BikeNightHunt.SIGHT&&level&&BikeNightHunt.clear(l,at,there)){g.way.plan=null;bike.toward(there,(float)Math.min(RIDE_PACE,Math.max(.12,(d-KEEP+.6)*.16)));}
        else{
            if(g.way.plan==null||g.way.step>=g.way.plan.length||n.tick>=g.way.planAt)BikeNightHunt.plan(l,n,n.pack,g.way,bike,friend,0);
            BikeNightHunt.follow(l,n,g.way,bike);
        }
        warn(l,n,g,a,friend);
    }
    /** What he sees of the three, called out as he rides: one of them riding at his friend and close, or one gone round to head them off. And the graveyard, when they are near it. */
    static void warn(ServerLevel l,BikeNight.Night n,Friend g,LiteraryActor a,ServerPlayer friend){
        var there=friend.position();
        if(flat(there,CalderRoutes.at(n.base,CalderRoutes.id("cemetery_gate")))<30*30)saw(n,"church",BikeNightTexts.GUS_CHURCH);
        if(n.tick<g.warnAt||a.saying()||!g.soon.isEmpty()||flat(a.position(),there)>HEARD*HEARD)return;
        var going=BikeNightHunt.going(n.pack,friend);boolean behind=false,round=false;
        for(var u:n.pack.b){
            var his=BikeNightBullies.bike(l,u);if(his==null||!u.riding||u.leaving||u.gone||n.tick<u.stunnedUntil)continue;
            double d=Math.sqrt(flat(his.position(),there));
            if(u.aim<0){behind|=d<BEHIND_THEM;continue;}
            // One of them who is near, making by the town's ways for a corner his friend is riding toward; or, if they are standing,
            // for any corner but the one behind them.
            var corner=CalderRoutes.at(n.base,u.aim);
            round|=d<ROUND_THEM&&flat(corner,there)<AHEAD_THEM*AHEAD_THEM&&(going!=null?BikeNightHunt.dot(corner.subtract(there),going)>0:u.aim!=BikeNightHunt.nearEnd(n,there));
        }
        if(!behind&&!round)return;
        // With both to say, not the same one twice running.
        boolean ofRound=round&&(!behind||!g.lastRound);var lines=ofRound?BikeNightTexts.GUS_ROUND:BikeNightTexts.GUS_BEHIND;
        g.lastRound=ofRound;g.warnAt=n.tick+WARN_GAP;later(g,n,lines[g.warned++%lines.length],0);
    }

    // ------------------------------------------------------------------------------------------------ for tests
    /** His bike in this park, if it is there. */
    public static @Nullable PackBike bikeOf(ServerLevel l,BlockPos b){var n=BikeNight.night(b);return n!=null&&n.gus.bike!=null&&l.getEntity(n.gus.bike) instanceof PackBike his&&his.isAlive()?his:null;}
    /** He goes for his bike now, whether or not the three have gone for theirs. */
    public static void rideNow(BlockPos b){var n=BikeNight.night(b);if(n!=null)n.gus.called=true;}
    /** The next thing he has to call out about the three is due now. */
    public static void warnNow(BlockPos b){var n=BikeNight.night(b);if(n!=null){n.gus.warnAt=n.tick;n.gus.soonAt=Math.min(n.gus.soonAt,n.tick);}}
    /** What he is doing in this park, or -1. */
    public static int doing(BlockPos b){var n=BikeNight.night(b);return n==null?-1:n.gus.doing;}
    /** He starts on his story with his next line about nothing. */
    public static void storyNow(BlockPos b){var n=BikeNight.night(b);if(n!=null&&n.gus.story<0)n.gus.idle=Math.max(n.gus.idle,STORY_AFTER);}
    /** His next line about nothing is due now. */
    public static void idleNow(BlockPos b){var n=BikeNight.night(b);if(n!=null){n.gus.nextIdle=n.tick;n.gus.soonAt=Math.min(n.gus.soonAt,n.tick);}}
}
