package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 0.4.708, The list, part 4a (docs/THE_LIST_0_4_708.md): the hunt. The five on the list as people with somewhere to run. Each sits at
 * their place until they see the slasher (or hear them close, or hear one of the others scream), and then they scream, get up and run a
 * way of their own to a place to hide: Deek and Wes off the home bench, across the field and in at the field house's back door, which
 * Deek holds for Wes and then shuts and bolts, to the showers and the coach's office; Mel down the front of the stands and round under
 * the north end; Lesley into the press box, bolting its door behind her; Shannon off the bus bench, all the way down Main Street to the
 * drive-in, where Jenna lets her into the stand and she bolts the door. A few seconds into the run each yells for help, and anyone else in
 * town is told from which way. Hidden, they crouch, and their hearts race for the slasher, who hears them through the walls.
 * <p>
 * Found (the slasher close enough to touch, and in sight), or caught up with on the way, each fights back once with what they have: a
 * shoulder, a wild swing, the camcorder, a fistful of dirt, both hands. The body goes down for its five seconds, and they run on, from
 * their first place to a second. Found again, they have nothing left and cower where they are. What comes next, the catch, is part 4a's
 * second half.
 * <p>
 * A bolted door opens to nobody's hand. The slasher can strike it: a house door gives at the third blow as any of the town's do, a steel
 * door at the fifth. A door sprung or broken is left as it is; nobody bolts it again.
 * <p>
 * Begun by an operator ({@code /ghosthouse test the_list hunt}) until the nights drive it (part 5). It is the town's, kept while the server
 * runs; ended, everyone goes back to their place and the doors are put back.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TheListHunt {
    private TheListHunt(){}
    /** Sitting at their place; running; hidden; cornered with nothing left; 0.4.709: held by the slasher; on the hook; dead. */
    public enum State{CALM,RUN,HIDE,COWER,HELD,FINISH,DEAD}
    /** Their gestures, drawn by the client: sat or stood at their place, running, crouched hidden, swinging, cowering; 0.4.709: fighting a
     * grip, flinching from the hook, on the knees on it, slumped over it, fallen. */
    public static final int STILL=0,RUNNING=1,HIDING=2,SWINGING=3,COWERING=4,STRUGGLING=5,RECOIL=6,KNEELING=7,SLUMPED=8,FALLEN=9;
    /** How far one of them sees the body in light, and in the dark; how near they hear it whichever way they face; 0.4.709: how near
     * they hear it creeping, crouched. */
    public static final double SEES_LIT=24,SEES_DARK=12,HEARS=6,HEARS_CROUCHED=1;
    /** How far a scream carries to the others on the list, and to the rest of the town. */
    public static final double SCREAM_REACH=40,HELP_REACH=96;
    /** Close enough to find one hiding, and to be caught up with running. */
    public static final double FOUND=2.4,CAUGHT=1.6;
    /** How long a runner yells for help after starting to run; how long they hold a door for another coming behind them. */
    public static final int HELP_AFTER=60,HOLD=120;
    /** How much quicker than a walk they run: as quick as the body walks, so it never loses ground and never gains it in the open. */
    public static final float PACE=1.3F;
    /** The slasher this near a held door: it is slammed whoever is still coming. */
    public static final double SLAM=5;
    /** 0.4.709: how far in front of the slasher one held is kept, and one on the hook drawn in to. */
    public static final double HELD_AT=.8,KNELT_AT=.65;
    /** How long each of them takes to tear loose from the slasher's grip, by who they are; twice as quick while the slasher looks more
     * than LOOK_AWAY degrees off them. */
    static final int[] STRENGTH=new int[TheListPeople.CAST];
    static{STRENGTH[TheListPeople.DEEK]=60;STRENGTH[TheListPeople.WES]=90;STRENGTH[TheListPeople.MEL]=80;STRENGTH[TheListPeople.LESLEY]=100;STRENGTH[TheListPeople.SHANNON]=110;}
    public static int strength(int who){return STRENGTH[who];}
    public static final float LOOK_AWAY=50F;
    /** The hay hook's finish, by the tick from its start: raised; in; the knees give; out; slumped over it; fallen and let go; dead. */
    public static final int RAISE=20,KNEES=32,OUT=70,FOLD=86,DONE=110;
    /** How often one held is heard fighting the grip; how far the scream cut short by the hook is told of. */
    static final int STRUGGLE_EVERY=40;

    /** The doors the five bolt behind them: the field house's back door (its west leaf), the press box's, the drive-in stand's. */
    public static final BlockPos FIELD_DOOR=new BlockPos(TheListSite.FIELD_X-1,0,TheListSite.FIELD_HOUSE_S),
        PRESS_DOOR=new BlockPos(TheListSite.FIELD_X-31,4,TheListSite.PRESS_DOOR_Z),STAND_DOOR=new BlockPos(41,0,-138);

    // ------------------------------------------------------------------------------------------------ the ways they run
    /** A step on a way: walk to a point; open a door there; shut and bolt a door from there; or hide there, facing a way. */
    record Step(int kind,Vec3 at,@Nullable BlockPos door,Direction facing){}
    static final int WALK=0,OPEN=1,SHUT=2,HIDE=3;
    static Step w(double x,double y,double z){return new Step(WALK,new Vec3(x,y,z),null,Direction.NORTH);}
    static Step open(BlockPos door,double x,double y,double z){return new Step(OPEN,new Vec3(x,y,z),door,Direction.NORTH);}
    static Step shut(BlockPos door,double x,double y,double z){return new Step(SHUT,new Vec3(x,y,z),door,Direction.NORTH);}
    static Step hide(double x,double y,double z,Direction facing){return new Step(HIDE,new Vec3(x,y,z),null,facing);}
    /** Where each gets up to when they start to run (off the bench, the plank, the bus bench), and each one's two ways: from their place
     * to their first hiding place, and from there to their second. By who they are (TheListPeople). */
    static final Vec3[] UP=new Vec3[TheListPeople.CAST];
    static final Step[][][] WAYS=new Step[TheListPeople.CAST][][];
    static {
        UP[TheListPeople.DEEK]=new Vec3(-136.5,0,-59.5);
        WAYS[TheListPeople.DEEK]=new Step[][]{
            {w(-130.5,0,-62.5),w(-130.5,0,-93.5),w(-124.5,0,-97.5),open(FIELD_DOOR,-124.5,0,-98.6),w(-124.5,0,-101.5),shut(FIELD_DOOR,-122.5,0,-101.5),
                w(-121.5,0,-101.5),w(-120.5,0,-101.5),w(-119.5,0,-100.5),hide(-113.5,0,-100.5,Direction.WEST)},
            {w(-119.5,0,-100.5),w(-120.5,0,-101.5),w(-121.5,0,-101.5),w(-121.5,0,-106.5),hide(-133.5,0,-106.5,Direction.EAST)}};
        UP[TheListPeople.WES]=new Vec3(-136.5,0,-60.5);
        WAYS[TheListPeople.WES]=new Step[][]{
            {w(-129.5,0,-63.5),w(-129.5,0,-93.5),w(-124.5,0,-97.5),open(FIELD_DOOR,-124.5,0,-98.6),w(-124.5,0,-101.5),shut(FIELD_DOOR,-122.5,0,-103.5),
                w(-121.5,0,-105.5),w(-120.5,0,-105.5),w(-118.5,0,-104.5),w(-114.5,0,-104.5),hide(-114.5,0,-106.5,Direction.EAST)},
            {w(-114.5,0,-104.5),w(-118.5,0,-104.5),w(-120.5,0,-105.5),w(-121.5,0,-105.5),w(-121.5,0,-103.5),hide(-123.5,0,-103.5,Direction.WEST)}};
        UP[TheListPeople.MEL]=new Vec3(-152.5,3.5,-60.5);
        WAYS[TheListPeople.MEL]=new Step[][]{
            {w(-145.5,0,-60.5),w(-145.5,0,-75.5),w(-152,0,-75.5),hide(-152,0,-73.5,Direction.NORTH)},
            {w(-152,0,-71.5),w(-152,0,-46.5),hide(-152,0,-44.5,Direction.NORTH)}};
        UP[TheListPeople.LESLEY]=new Vec3(-152.5,3.5,-53.5);
        WAYS[TheListPeople.LESLEY]=new Step[][]{
            {w(-153.5,4,-52.5),open(PRESS_DOOR,-153.5,4,-52.5),w(-155.5,4,-52.5),w(-156.5,4,-52.5),shut(PRESS_DOOR,-157.5,4,-54.5),
                w(-157.5,4,-62.5),hide(-157.5,4,-62.5,Direction.EAST)},
            {w(-157.5,4,-54.5),open(PRESS_DOOR,-156.5,4,-52.5),w(-155.5,4,-52.5),w(-153.5,4,-52.5),w(-145.5,0,-52.5),w(-145.5,0,-40.5),
                w(-151,0,-40.5),hide(-151,0,-46.5,Direction.NORTH)}};
        UP[TheListPeople.SHANNON]=new Vec3(-35.5,0,-110.5);
        WAYS[TheListPeople.SHANNON]=new Step[][]{
            {w(26.5,0,-110.5),w(26.5,0,-127.5),w(43.5,0,-127.5),w(43.5,0,-137.5),open(STAND_DOOR,42.5,0,-137.5),w(40.5,0,-137.5),w(39.5,0,-137.5),
                shut(STAND_DOOR,37.5,0,-138.5),hide(34.5,0,-139.5,Direction.EAST)},
            {open(STAND_DOOR,39.5,0,-137.5),w(40.5,0,-137.5),w(42.5,0,-137.5),w(42.5,0,-143.5),hide(36.5,0,-143.5,Direction.NORTH)}};
    }

    /** 0.4.709: back the way they came, for one torn loose where they cowered: their second way run backward to their first hiding
     * place. A door on it is opened as they come to it (by then it has been sprung or broken, or they left it open). */
    static final int BACK_WAY=2;
    static final Step[][] BACK=new Step[TheListPeople.CAST][];
    static {
        for(int who:new int[]{TheListPeople.DEEK,TheListPeople.WES,TheListPeople.MEL,TheListPeople.LESLEY,TheListPeople.SHANNON}){
            var second=WAYS[who][1];var back=new ArrayList<Step>();
            for(int i=second.length-2;i>=0;i--){var st=second[i];back.add(st.door()!=null?new Step(OPEN,st.at(),st.door(),Direction.NORTH):w(st.at().x,st.at().y,st.at().z));}
            var first=WAYS[who][0];back.add(first[first.length-1]);
            BACK[who]=back.toArray(Step[]::new);
        }
    }
    /** The steps of the way one is on now: their first, their second, or back. */
    static Step[] steps(Runner r){return r.way<WAYS[r.who].length?WAYS[r.who][r.way]:BACK[r.who];}

    // ------------------------------------------------------------------------------------------------ the town's hunt
    /** One of the five, on the hunt: what they are doing, which way and how far along it, whether they have fought back yet. */
    static final class Runner{
        final int who;final UUID id;State state=State.CALM;int way,step,held,since,stuck;boolean fought,yelled;@Nullable Vec3 last;@Nullable ChunkPos ticket;
        /** 0.4.709: who has hold of them, how far they are to tearing loose, and what they were doing when caught. */
        @Nullable UUID holder;int strength;State caught=State.CALM;
        /** Who used the hook on them: kept past the fall, when the holder lets go, so that the death is theirs. */
        @Nullable UUID killer;
        /** Which way the slasher faced when they took hold: the held one is kept that way from them, wherever the slasher looks after. */
        float pin;
        Runner(int who,UUID id){this.who=who;this.id=id;}
    }
    static final class Hunt{
        final BlockPos base;final Map<Integer,Runner> runners=new LinkedHashMap<>();final Set<BlockPos> bolted=new HashSet<>(),sprung=new HashSet<>();int tick;
        Hunt(BlockPos b){base=b;}
    }
    static final Map<Long,Hunt> HUNTS=new HashMap<>();
    private static final TicketType<UUID> TICKET=TicketType.create("the_list_hunt",UUID::compareTo);

    public static boolean on(BlockPos b){return HUNTS.containsKey(b.asLong());}
    /** Whether the hunt has one of them now, so that the town's own pass leaves them where they are. */
    public static boolean holds(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r!=null&&r.state!=State.CALM;}
    public static @Nullable State state(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?null:r.state;}
    /** Which way they are on (0 to the first hiding place, 1 to the second) and how far along it. */
    public static int way(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?-1:r.way;}
    public static int step(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?-1:r.step;}
    /** The body of one the hunt holds, wherever it is. */
    public static @Nullable LiteraryActor actor(ServerLevel l,BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r!=null&&l.getEntity(r.id) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    public static boolean fought(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r!=null&&r.fought;}
    /** Where each one's way ends: their first hiding place (0) or their second (1), in the town's own coordinates. */
    public static Vec3 hidingPlace(int who,int way){var steps=WAYS[who][way];return steps[steps.length-1].at();}
    /** A door one of them has bolted behind them, by where its lower half stands in the town. */
    public static boolean bolted(BlockPos b,BlockPos lower){var h=HUNTS.get(b.asLong());return h!=null&&h.bolted.contains(lower);}
    /** Whether one of the five is running, hidden or cornered now: their heart races. */
    public static boolean scared(Entity e){
        var r=runner(e);return r!=null&&r.state!=State.CALM&&!gone(r);
    }
    /** 0.4.709: whether one of the five is dead, or as good as: the hook out of them. Their heart is not heard, they see nothing, and
     * nothing draws the slasher to them. */
    public static boolean gone(Entity e){var r=runner(e);return r!=null&&gone(r);}
    static boolean gone(Runner r){return r.state==State.DEAD||r.state==State.FINISH&&r.since>=OUT;}
    static @Nullable Runner runner(Entity e){
        if(!(e instanceof LiteraryActor a)||!TheListPeople.is(a))return null;
        for(var h:HUNTS.values()){var r=h.runners.get(TheListPeople.who(a));if(r!=null&&r.id.equals(a.getUUID()))return r;}
        return null;
    }
    /** 0.4.709: whether a reader has hold of one of the five (or the hook in them): they cannot leave by the House's door. */
    public static boolean holding(ServerPlayer p){for(var h:HUNTS.values())for(var r:h.runners.values())if(p.getUUID().equals(r.holder))return true;return false;}
    /** Whether one of them is dead. */
    public static boolean dead(BlockPos b,int who){return state(b,who)==State.DEAD;}
    /** How far one of them turns in a tick while running, on top of the walk's own turn: a body running for its life pivots at a
     * corner. At the walk's twelve degrees a tick, a runner turning a corner in a doorway circled the next point and never reached it. */
    public static final float TURN=45F;
    /** Turned toward the point they are running to, before the walk's own steering (LiteraryActor.travel). */
    public static void steer(LiteraryActor a){
        if(a.gesture()!=RUNNING)return;var t=a.getPersistentData();if(!t.contains("BorrascaSpeed"))return;
        double dx=t.getDouble("BorrascaGoalX")-a.getX(),dz=t.getDouble("BorrascaGoalZ")-a.getZ();if(dx*dx+dz*dz<.01)return;
        float yaw=Mth.approachDegrees(a.getYRot(),(float)Math.toDegrees(Math.atan2(-dx,dz)),TURN);a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
    }
    /** How much quicker than a walk this one moves now. */
    public static float pace(LiteraryActor a){return TheListPeople.is(a)&&a.gesture()==RUNNING?PACE:1F;}

    /** The hunt begun in a town: the night put away, the doors put back, the five found where they sit. */
    public static void start(ServerLevel l,BlockPos b){
        end(l,b);
        sweep(l,b);
        doors(l,b);
        TheListPeople.ensure(l,b);
        var h=new Hunt(b);
        for(int who:TheListBody.LISTED){var a=TheListPeople.find(l,b,who);if(a!=null)h.runners.put(who,new Runner(who,a.getUUID()));}
        HUNTS.put(b.asLong(),h);
        TheListPeople.ensure(l,b);
    }
    /** Ended: everyone stood up and sent back to their place, the doors put back. */
    public static void end(ServerLevel l,BlockPos b){
        var h=HUNTS.remove(b.asLong());if(h==null)return;
        for(var r:h.runners.values()){
            release(l,r);
            if(r.holder!=null){var p=l.getServer().getPlayerList().getPlayer(r.holder);if(p!=null)TheListBody.holding(p,false);}
            if(l.getEntity(r.id) instanceof LiteraryActor a){BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.gesture(STILL);a.setNoGravity(true);a.holder(null);}
        }
        doors(l,b);
        sweep(l,b);
        TheListPeople.ensure(l,b);
    }
    /** The three doors as they were built: shut, and nobody's bolt on them. */
    static void doors(ServerLevel l,BlockPos b){
        TheListSite.steelDoor(l,b,FIELD_DOOR,Direction.NORTH,false);TheListSite.steelDoor(l,b,FIELD_DOOR.east(),Direction.NORTH,true);
        TheListSite.steelDoor(l,b,PRESS_DOOR,Direction.WEST,false);
        TheListSite.door(l,b,STAND_DOOR,Direction.EAST,true);
    }

    // ------------------------------------------------------------------------------------------------ each tick
    /** The town's tick, while someone is in it. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=HUNTS.get(b.asLong());if(h==null)return;h.tick++;
        var slashers=new ArrayList<ServerPlayer>();
        for(var p:here){var live=TheListBody.LIVE.get(p.getUUID());if(live!=null&&live.down==0&&!p.isSpectator())slashers.add(p);}
        // A door the slasher has broken is nobody's to bolt now.
        h.bolted.removeIf(at->{var st=l.getBlockState(b.offset(at));return !(st.getBlock() instanceof DoorBlock)||st.getValue(DoorBlock.OPEN)||st.is(TheListRegistry.BROKEN_DOOR.get());});
        for(var r:h.runners.values()){
            if(!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            r.since++;
            switch(r.state){
                case CALM->{if(h.tick%5==0)for(var s:slashers)if(notices(l,a,s)){run(l,b,h,r,a,here,true);break;}}
                case RUN->{
                    lease(l,r,a);
                    if(!r.yelled&&r.since>=HELP_AFTER)yell(l,b,r,a,here);
                    // 0.4.709: one just torn loose is a second clear of the slasher before they could turn on them.
                    for(var s:slashers)if(!r.fought&&r.since>=0&&a.distanceTo(s)<=CAUGHT){fight(l,b,h,r,a,s,here);break;}
                    if(r.state==State.RUN)step(l,b,h,r,a,slashers);
                }
                case HIDE->{if(h.tick%5==0)for(var s:slashers)if(finds(a,s)){found(l,b,h,r,a,s,here);break;}}
                case COWER->{var s=nearest(a,slashers);if(s!=null)face(a,s.position());}
                case HELD->held(l,b,h,r,a,slashers,here);
                case FINISH->finishing(l,b,h,r,a,slashers,here);
                case DEAD->{}
            }
        }
    }
    /** Whether one sitting at their place has noticed the slasher: seen in front of them (further in light), or heard close by. */
    static boolean notices(ServerLevel l,LiteraryActor a,ServerPlayer s){
        double d=a.distanceTo(s);if(d<=(s.isCrouching()?HEARS_CROUCHED:HEARS))return true;
        double sees=TheListBody.lit(s,l)?SEES_LIT:SEES_DARK;
        return d<=sees&&TheListBody.facing(a.getYHeadRot(),a.getEyePosition(),s.getEyePosition(),75)&&a.hasLineOfSight(s);
    }
    /** Whether the slasher has found one hiding: near enough to touch, and nothing between them. */
    static boolean finds(LiteraryActor a,ServerPlayer s){return a.distanceTo(s)<=FOUND&&Math.abs(a.getY()-s.getY())<2&&a.hasLineOfSight(s);}
    static @Nullable ServerPlayer nearest(LiteraryActor a,List<ServerPlayer> of){ServerPlayer best=null;double d=Double.MAX_VALUE;for(var s:of){double e=a.distanceToSqr(s);if(e<d){d=e;best=s;}}return best;}

    /**
     * Up and running: off the seat to their feet, a scream, their own words, and away on their first way (or on to the next, after
     * fighting back). Anyone else on the list near enough to hear the scream runs too.
     */
    static void run(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here,boolean first){
        r.state=State.RUN;r.step=0;r.held=0;r.since=0;r.stuck=0;r.last=null;
        boolean seated=a.gesture()==STILL;
        a.setPose(Pose.STANDING);a.gesture(RUNNING);a.setNoGravity(false);
        if(first){
            if(seated){var up=UP[r.who];a.moveTo(b.getX()+up.x,b.getY()+up.y,b.getZ()+up.z,a.getYRot(),0);}
            scream(l,a);
            say(a,r.who,TheListTexts.RUN[r.who]);
            heard(l,b,h,r,a,here);
        }
        go(b,r,a);
    }
    /** A scream, where they are. */
    static void scream(ServerLevel l,LiteraryActor a){l.playSound(null,a.getX(),a.getEyeY(),a.getZ(),TheListRegistry.SCREAM.get(),SoundSource.NEUTRAL,2.2F,.92F+l.getRandom().nextFloat()*.16F);}
    /** Anyone else on the list near enough to hear one of them scream, still at their place, runs too. */
    static void heard(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        for(var other:h.runners.values()){
            if(other==r||other.state!=State.CALM||!(l.getEntity(other.id) instanceof LiteraryActor o))continue;
            if(o.distanceTo(a)<=SCREAM_REACH)run(l,b,h,other,o,here,true);
        }
    }
    /** Toward the step they are on now. */
    static void go(BlockPos b,Runner r,LiteraryActor a){var st=steps(r)[r.step];BorrascaCast.walk(a,Vec3.atLowerCornerOf(b).add(st.at()),1.0);}
    /** A few seconds into the run, they yell for help; everyone else in town who is not the slasher is told from which way it came. */
    static void yell(ServerLevel l,BlockPos b,Runner r,LiteraryActor a,List<ServerPlayer> here){
        r.yelled=true;say(a,r.who,TheListTexts.HELP[r.who]);
        for(var q:here){
            if(TheListBody.LIVE.containsKey(q.getUUID())||q.distanceTo(a)>HELP_REACH)continue;
            TheList.cue(q,String.format(TheListTexts.HELP_HEARD,compass(q.position(),a.position()),TheListTexts.NAMES[r.who]));
        }
    }
    /** Which way one point lies from another, in the eight words of the compass. */
    public static String compass(Vec3 from,Vec3 to){
        double yaw=Math.toDegrees(Math.atan2(-(to.x-from.x),to.z-from.z));
        return TheListTexts.COMPASS[Math.floorMod((int)Math.round(yaw/45),8)];
    }
    static void say(LiteraryActor a,int who,@Nullable String line){if(line==null)return;a.say(TheListTexts.NAMES[who],line);a.speaks(line);}

    /** Along the way: on to the next point when they reach this one; a door opened, a door shut and bolted behind them, or the end. */
    static void step(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers){
        var steps=steps(r);var st=steps[r.step];
        var at=Vec3.atLowerCornerOf(b).add(st.at());
        var to=at.subtract(a.position());
        boolean there=to.horizontalDistance()<.4&&Math.abs(to.y)<.75;
        if(!there){
            // Stuck on something: a hop, and if that does not do it, they go to ground where they are.
            var here=a.position();
            if(r.last!=null&&here.distanceToSqr(r.last)<.0004)r.stuck++;else r.stuck=0;
            r.last=here;
            if(r.stuck==30&&a.onGround())a.setDeltaMovement(a.getDeltaMovement().x,.42,a.getDeltaMovement().z);
            if(r.stuck>=160){TheOldestHouse.LOGGER.warn("The list: {} stuck on way {} step {} at {} (bound for {})",TheListTexts.NAMES[r.who],r.way,r.step,here.subtract(Vec3.atLowerCornerOf(b)),st.at());hide(r,a,a.position(),st.facing());}
            else if(!a.getPersistentData().contains("BorrascaSpeed"))go(b,r,a);
            return;
        }
        switch(st.kind()){
            case OPEN->{BorrascaCast.stop(a);if(!open(l,b,h,st.door(),a))return;}
            case SHUT->{
                BorrascaCast.stop(a);face(a,Vec3.atCenterOf(b.offset(st.door())));
                if(!shut(l,b,h,r,st.door(),a,slashers)&&++r.held<HOLD)return;
            }
            case HIDE->{hide(r,a,at,st.facing());return;}
            default->{}
        }
        r.step++;r.held=0;r.stuck=0;
        if(r.step>=steps.length){hide(r,a,a.position(),Direction.NORTH);return;}
        go(b,r,a);
    }
    /** A door opened by one of them coming to it (a sprung or broken one is open already). Jenna lets Shannon into the stand. */
    static boolean open(ServerLevel l,BlockPos b,Hunt h,BlockPos door,LiteraryActor a){
        var at=b.offset(door);var st=l.getBlockState(at);
        if(!(st.getBlock() instanceof DoorBlock d))return true;
        if(!st.getValue(DoorBlock.OPEN)){d.setOpen(a,l,st,at,true);h.bolted.remove(door);}
        if(door.equals(STAND_DOOR)&&TheListPeople.who(a)==TheListPeople.SHANNON){var jenna=TheListPeople.find(l,b,TheListPeople.JENNA);if(jenna!=null&&!jenna.saidWithin(TheListTexts.JENNA_IN,200))say(jenna,TheListPeople.JENNA,TheListTexts.JENNA_IN);}
        return true;
    }
    /**
     * A door shut and bolted behind them, unless it was sprung or broken, somebody is standing in it, or one of the others is still
     * coming through it (it is held for them a while, but slammed if the slasher comes near). True when they can go on.
     */
    static boolean shut(ServerLevel l,BlockPos b,Hunt h,Runner r,BlockPos door,LiteraryActor a,List<ServerPlayer> slashers){
        var at=b.offset(door);var st=l.getBlockState(at);
        if(!(st.getBlock() instanceof DoorBlock d)||st.is(TheListRegistry.BROKEN_DOOR.get())||h.sprung.contains(door))return true;
        if(!st.getValue(DoorBlock.OPEN)){h.bolted.add(door);return true;}
        var cells=new AABB(at).expandTowards(0,1,0);
        if(!l.getEntitiesOfClass(LivingEntity.class,cells,e->e.isAlive()&&!e.isSpectator()).isEmpty())return false;
        boolean near=false;for(var s:slashers)if(s.position().distanceTo(Vec3.atCenterOf(at))<=SLAM)near=true;
        if(!near)for(var o:h.runners.values()){
            if(o==r||o.state!=State.RUN||!(l.getEntity(o.id) instanceof LiteraryActor oa)||oa.distanceTo(a)>24)continue;
            var steps=steps(o);
            for(int i=o.step;i<steps.length;i++)if(steps[i].kind()==OPEN&&door.equals(steps[i].door()))return false;
        }
        d.setOpen(a,l,st,at,false);h.bolted.add(door);
        l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,TheListRegistry.BOLT.get(),SoundSource.BLOCKS,1.6F,1.15F);
        for(var s:slashers)if(s.position().distanceTo(Vec3.atCenterOf(at))<=24)TheList.cue(s,st.is(TheListRegistry.STEEL_DOOR.get())?TheListTexts.BOLTED_STEEL:TheListTexts.BOLTED_DOOR);
        if(door.equals(STAND_DOOR)){var jenna=TheListPeople.find(l,b,TheListPeople.JENNA);if(jenna!=null)say(jenna,TheListPeople.JENNA,TheListTexts.JENNA_DOWN);}
        return true;
    }
    /** Hidden: crouched where the way ends, turned the way it says. */
    static void hide(Runner r,LiteraryActor a,Vec3 at,Direction facing){
        BorrascaCast.stop(a);release(a.level() instanceof ServerLevel s?s:null,r);
        r.state=State.HIDE;r.since=0;
        a.moveTo(at.x,a.getY(),at.z,facing.toYRot(),0);face(a,a.position().add(Vec3.atLowerCornerOf(facing.getNormal())));
        a.setPose(Pose.CROUCHING);a.gesture(HIDING);
    }
    static void face(LiteraryActor a,Vec3 to){
        var d=to.subtract(a.position());if(d.horizontalDistanceSqr()<1e-4)return;
        float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
    }

    // ------------------------------------------------------------------------------------------------ found
    /** Found where they hid: the first time they fight back and run for their next place; after that, nothing left. */
    static void found(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,List<ServerPlayer> here){
        if(!r.fought){fight(l,b,h,r,a,s,here);return;}
        r.state=State.COWER;BorrascaCast.stop(a);a.setPose(Pose.CROUCHING);a.gesture(COWERING);face(a,s.position());
        say(a,r.who,TheListTexts.COWER[r.who]);
    }
    /**
     * Fighting back, once: a blow with whatever is to hand, and the body goes down for its five seconds. Lesley's dirt goes through the
     * eyeholes, and for a moment the slasher sees nothing. Then they run: on from a hiding place to their next, or on along the way they
     * were already running.
     */
    static void fight(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,List<ServerPlayer> here){
        r.fought=true;face(a,s.position());a.setPose(Pose.STANDING);a.gesture(SWINGING);a.swing(InteractionHand.MAIN_HAND,true);
        say(a,r.who,TheListTexts.FIGHT[r.who]);
        l.playSound(null,s.getX(),s.getEyeY(),s.getZ(),SoundEvents.PLAYER_ATTACK_KNOCKBACK,SoundSource.NEUTRAL,1F,.8F);
        if(r.who==TheListPeople.LESLEY)s.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,60,0,false,false,false));
        TheListBody.felled(s,TheListTexts.FELLED[r.who],a.position());
        if(r.state==State.HIDE){
            if(r.way+1<WAYS[r.who].length){r.way++;run(l,b,h,r,a,here,false);}
            else{r.state=State.COWER;a.setPose(Pose.CROUCHING);a.gesture(COWERING);}
        }else a.gesture(RUNNING);
    }
    /** A blow from the slasher on one of them: one at their place runs; one hiding is found; one running fights back if they have not. */
    public static void struck(ServerPlayer p,LiteraryActor a){
        var b=TheListBody.town(p);if(b==null)return;var h=HUNTS.get(b.asLong());if(h==null)return;
        var r=h.runners.get(TheListPeople.who(a));if(r==null||!r.id.equals(a.getUUID())||!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return;
        var l=p.serverLevel();var here=TheList.readers(l,b);
        switch(r.state){
            case CALM->run(l,b,h,r,a,here,true);
            case HIDE->found(l,b,h,r,a,p,here);
            case RUN->{if(!r.fought)fight(l,b,h,r,a,p,here);}
            // 0.4.709: a blow on one cornered takes hold of them; on one held, with the hook in hand, it is the hook.
            case COWER->take(l,b,h,r,a,p,here);
            case HELD->{if(p.getUUID().equals(r.holder)&&TheListBody.hookInHand(p))finish(r,a,p);}
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ 0.4.709: the catch
    /**
     * A hand on one of the five, from TheListPeople.talk. The slasher's: one at their place who does not see it coming (from behind,
     * creeping) is taken hold of, and one who sees it runs; one running twists out of reach; one hiding is found; one cornered is taken
     * hold of; one held, with the hook in hand, gets the hook. Anyone else's hand: the calm ones talk as before (false), the rest are
     * past talking. Anyone's on one dead: they do not move. Spectators touch nothing.
     */
    public static boolean hand(ServerPlayer p,LiteraryActor a){
        var b=TheListBody.town(p);if(b==null)return false;var h=HUNTS.get(b.asLong());if(h==null)return false;
        var r=h.runners.get(TheListPeople.who(a));if(r==null||!r.id.equals(a.getUUID()))return false;
        if(p.isSpectator())return true;
        var name=TheListTexts.NAMES[r.who];
        if(r.state==State.DEAD||gone(r)){TheList.cue(p,String.format(TheListTexts.STILL_BODY,name));return true;}
        if(!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return r.state!=State.CALM;
        var l=p.serverLevel();var here=TheList.readers(l,b);
        switch(r.state){
            case CALM->{if(sees(a,p))run(l,b,h,r,a,here,true);else take(l,b,h,r,a,p,here);}
            case RUN->TheList.cue(p,String.format(TheListTexts.TWIST,name));
            case HIDE->found(l,b,h,r,a,p,here);
            case COWER->take(l,b,h,r,a,p,here);
            case HELD->{if(p.getUUID().equals(r.holder)&&TheListBody.hookInHand(p))finish(r,a,p);}
            default->{}
        }
        return true;
    }
    /** Whether one of them has the slasher in front of them, in sight. */
    public static boolean sees(LiteraryActor a,ServerPlayer s){return TheListBody.facing(a.getYHeadRot(),a.getEyePosition(),s.getEyePosition(),75)&&a.hasLineOfSight(s);}
    /**
     * Taken hold of: stood up in front of the slasher, facing them, fighting the grip. Their words and a scream (anyone else on the list
     * near enough hears it and runs); the slasher stands where they are while they hold on. Nobody is taken while the slasher holds another.
     */
    static void take(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,List<ServerPlayer> here){
        if(holding(s))return;
        r.caught=r.state;r.state=State.HELD;r.holder=s.getUUID();r.since=0;r.strength=0;r.pin=yawTo(s.position(),a.position());
        release(l,r);BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(STRUGGLING);a.holder(s.getUUID());
        pin(a,s,r.pin,HELD_AT);
        TheListBody.holding(s,true);
        TheList.cue(s,String.format(TheListTexts.HOLD,TheListTexts.NAMES[r.who]));
        say(a,r.who,TheListTexts.CAUGHT[r.who]);
        scream(l,a);
        l.playSound(null,a.getX(),a.getY()+1,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,1F,1F);
        heard(l,b,h,r,a,here);
    }
    /** Held: kept in front of the slasher wherever they turn, fighting the grip until they tear loose, or until the slasher is put down
     * or gone. */
    static void held(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers,List<ServerPlayer> here){
        var s=holder(r,slashers);
        if(s==null){loose(l,b,h,r,a,null,here);return;}
        pin(a,s,r.pin,HELD_AT);
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+1,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,1F,1F);
        float off=Math.abs(Mth.wrapDegrees(yawTo(s.position(),a.position())-s.getYHeadRot()));
        r.strength+=off>LOOK_AWAY?2:1;
        if(r.strength>=STRENGTH[r.who])loose(l,b,h,r,a,s,here);
    }
    /** The one with hold of them, if they are still in town with the body up. */
    static @Nullable ServerPlayer holder(Runner r,List<ServerPlayer> slashers){for(var s:slashers)if(s.getUUID().equals(r.holder))return s;return null;}
    static float yawTo(Vec3 from,Vec3 to){var d=to.subtract(from);return (float)Math.toDegrees(Math.atan2(-d.x,d.z));}
    /**
     * Torn loose: a shove that puts the slasher back a step, their words, a scream, and they run. Caught at their place, they run their
     * first way; caught cornered, back the way they came to their first hiding place.
     */
    static void loose(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,@Nullable ServerPlayer s,List<ServerPlayer> here){
        var was=r.holder==null?null:l.getServer().getPlayerList().getPlayer(r.holder);
        if(was!=null)TheListBody.holding(was,false);
        r.holder=null;r.killer=null;a.holder(null);a.setNoGravity(false);
        stop(l,a,TheListRegistry.STRUGGLE.get());
        if(s!=null){
            s.knockback(.8,a.getX()-s.getX(),a.getZ()-s.getZ());s.hurtMarked=true;
            l.playSound(null,s.getX(),s.getEyeY(),s.getZ(),SoundEvents.PLAYER_ATTACK_KNOCKBACK,SoundSource.NEUTRAL,1F,1.1F);
            TheList.cue(s,String.format(TheListTexts.TORE,TheListTexts.NAMES[r.who]));
        }
        say(a,r.who,TheListTexts.LOOSE[r.who]);
        r.way=r.caught==State.CALM?0:BACK_WAY;
        r.state=State.RUN;r.step=0;r.held=0;r.since=-20;r.stuck=0;r.last=null;
        a.setPose(Pose.STANDING);a.gesture(RUNNING);
        scream(l,a);heard(l,b,h,r,a,here);
        go(b,r,a);
    }
    /** Kept so far from the slasher, the way they were taken, facing them; where something is in the way they stay where they are. */
    static void pin(LiteraryActor a,ServerPlayer s,float yaw,double by){
        var to=new Vec3(s.getX()-Mth.sin(yaw*Mth.DEG_TO_RAD)*by,s.getY(),s.getZ()+Mth.cos(yaw*Mth.DEG_TO_RAD)*by);
        if(!a.level().noCollision(a,a.getDimensions(a.getPose()).makeBoundingBox(to).deflate(.05)))to=new Vec3(a.getX(),s.getY(),a.getZ());
        a.moveTo(to.x,to.y,to.z,yaw+180,0);a.setYBodyRot(yaw+180);a.setYHeadRot(yaw+180);a.setDeltaMovement(Vec3.ZERO);
    }
    /** A sound one of them has been making, stopped for everyone near. */
    static void stop(ServerLevel l,LiteraryActor a,net.minecraft.sounds.SoundEvent sound){
        var packet=new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(sound.getLocation(),SoundSource.NEUTRAL);
        for(var p:l.players())if(p.distanceTo(a)<=64)p.connection.send(packet);
    }

    /** What one of them has in hand (Mel's camcorder) let fall as the hook comes up: it lies where it lands, nobody's to pick up, until
     * the hunt ends. */
    public static final String DROPPED="the_list_dropped";
    static void drop(LiteraryActor a){
        var held=a.getItemInHand(InteractionHand.MAIN_HAND);if(held.isEmpty())return;
        a.setItemInHand(InteractionHand.MAIN_HAND,net.minecraft.world.item.ItemStack.EMPTY);
        var it=new net.minecraft.world.entity.item.ItemEntity(a.level(),a.getX(),a.getY()+1.1,a.getZ(),held.copy());
        var out=Vec3.directionFromRotation(0,a.getYRot()+90);
        it.setDeltaMovement(out.x*.12,.12,out.z*.12);it.setNeverPickUp();it.setUnlimitedLifetime();it.addTag(DROPPED);
        a.level().addFreshEntity(it);
    }
    /** Whether one of them has let go of what they held (Mel keeps the camcorder through the hunt until then). */
    public static boolean dropped(BlockPos b,int who){var st=state(b,who);return st==State.FINISH||st==State.DEAD;}
    /** Anything let fall during a hunt, taken up when it ends or another begins. */
    static void sweep(ServerLevel l,BlockPos b){
        for(var it:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,TheListBody.bounds(b),e->e.getTags().contains(DROPPED)))it.discard();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.709: the hay hook
    /** The hook, on one held: they see it come up. */
    static void finish(Runner r,LiteraryActor a,ServerPlayer s){
        if(r.state!=State.HELD)return;
        r.state=State.FINISH;r.since=0;r.killer=s.getUUID();a.gesture(RECOIL);s.swing(InteractionHand.MAIN_HAND,true);
        drop(a);
    }
    /**
     * The hook, tick by tick. Until it goes in they can still be let go of. In: under the collarbone, blood thrown at the slasher, the
     * scream cut off, and anyone else in town near enough told where it stopped. Drawn in close on it, down on the knees, bleeding. Out:
     * blood again, the hook bloodied, and they slump over it, then fall. Let go, they lie where they fell, dead.
     */
    static void finishing(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers,List<ServerPlayer> here){
        int t=r.since;// the town's tick has counted this one
        var s=r.holder==null?null:l.getServer().getPlayerList().getPlayer(r.holder);
        boolean there=s!=null&&slashers.contains(s);
        if(t<RAISE){
            if(!there){loose(l,b,h,r,a,null,here);return;}
            pin(a,s,r.pin,HELD_AT);if(t==10)s.swing(InteractionHand.MAIN_HAND,true);
            return;
        }
        var name=TheListTexts.NAMES[r.who];
        if(t==RAISE){
            stop(l,a,TheListRegistry.STRUGGLE.get());
            if(there)pin(a,s,r.pin,KNELT_AT);
            a.gesture(KNEELING);
            l.playSound(null,a.getX(),a.getY()+1.2,a.getZ(),TheListRegistry.HOOK_IN.get(),SoundSource.NEUTRAL,1F,1F);
            l.playSound(null,a.getX(),a.getEyeY(),a.getZ(),TheListRegistry.GASP.get(),SoundSource.NEUTRAL,1F,.95F+l.getRandom().nextFloat()*.1F);
            scream(l,a);
            spray(l,a,s,24);
            if(s!=null)TheList.cue(s,TheListTexts.HOOK_IN);
            for(var q:here)if(q!=s&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
        }
        if(t==RAISE+5)stop(l,a,TheListRegistry.SCREAM.get());
        if(t>RAISE&&t<OUT&&there)pin(a,s,r.pin,KNELT_AT);
        if(t>RAISE&&t<FOLD&&t%2==0)drip(l,a);
        if(t==KNEES&&s!=null)TheList.cue(s,String.format(TheListTexts.HOOK_KNEES,name));
        if(t==OUT){
            l.playSound(null,a.getX(),a.getY()+1.2,a.getZ(),TheListRegistry.HOOK_OUT.get(),SoundSource.NEUTRAL,1F,1F);
            spray(l,a,s,14);a.gesture(SLUMPED);
            if(s!=null){s.swing(InteractionHand.MAIN_HAND,true);TheListBody.bloody(s);TheList.cue(s,String.format(TheListTexts.HOOK_OUT,name));}
        }
        if(t==FOLD){
            // Face down, the knees where they knelt and the head toward the slasher's feet.
            a.gesture(FALLEN);a.setNoGravity(false);
            if(s!=null){
                float yaw=r.pin;var to=new Vec3(s.getX()-Mth.sin(yaw*Mth.DEG_TO_RAD)*1.6,a.getY(),s.getZ()+Mth.cos(yaw*Mth.DEG_TO_RAD)*1.6);
                if(l.noCollision(a,a.getDimensions(a.getPose()).makeBoundingBox(to).deflate(.05)))a.moveTo(to.x,to.y,to.z,a.getYRot(),0);
                TheListBody.holding(s,false);
            }
            r.holder=null;a.holder(null);
        }
        if(t>=DONE){
            r.state=State.DEAD;
            var by=r.killer==null?null:l.getServer().getPlayerList().getPlayer(r.killer);
            if(by!=null)TheListBody.killed(by,r.who);
        }
    }
    /** Blood thrown off the hook: toward the slasher's face, up and out, falling where it falls. */
    static void spray(ServerLevel l,LiteraryActor a,@Nullable ServerPlayer s,int n){
        var from=new Vec3(a.getX(),a.getY()+1.25,a.getZ());
        var dir=s!=null?s.getEyePosition().subtract(from).normalize():Vec3.directionFromRotation(0,a.getYRot());
        var rand=l.getRandom();
        for(int i=0;i<n;i++){
            var v=dir.scale(.15+rand.nextDouble()*.22).add((rand.nextDouble()-.5)*.2,.06+rand.nextDouble()*.16,(rand.nextDouble()-.5)*.2);
            l.sendParticles(TheListRegistry.BLOOD.get(),from.x,from.y,from.z,0,v.x,v.y,v.z,1);
        }
    }
    /** A drop down the front of them. */
    static void drip(ServerLevel l,LiteraryActor a){l.sendParticles(TheListRegistry.BLOOD.get(),a.getX(),a.getY()+1.0,a.getZ(),1,.1,.05,.1,0);}

    // ------------------------------------------------------------------------------------------------ doors struck
    /** A bolted steel door struck at its fifth blow: the bolt tears out and the door swings in, and nobody bolts it again. */
    static void spring(ServerLevel l,BlockPos b,BlockPos lower){
        var h=HUNTS.get(b.asLong());if(h!=null){h.bolted.remove(lower);h.sprung.add(lower);}
        var at=b.offset(lower);var st=l.getBlockState(at);
        if(st.getBlock() instanceof DoorBlock d&&!st.getValue(DoorBlock.OPEN))d.setOpen(null,l,st,at,true);
        l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,SoundSource.BLOCKS,1.4F,.7F);
    }
    /** Whether a door is the lower half of one the hunt has bolted, from either of its halves. */
    static @Nullable BlockPos lower(ServerLevel l,BlockPos b,BlockPos rel){
        var st=l.getBlockState(b.offset(rel));if(!(st.getBlock() instanceof DoorBlock))return null;
        return st.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?rel.below():rel;
    }

    // ------------------------------------------------------------------------------------------------ keeping a runner's ground loaded
    /** A runner's ground kept loaded under them while they run, wherever the readers are. */
    static void lease(ServerLevel l,Runner r,LiteraryActor a){
        var c=a.chunkPosition();if(c.equals(r.ticket))return;
        release(l,r);l.getChunkSource().addRegionTicket(TICKET,c,3,r.id);r.ticket=c;
    }
    static void release(@Nullable ServerLevel l,Runner r){if(r.ticket!=null&&l!=null)l.getChunkSource().removeRegionTicket(TICKET,r.ticket,3,r.id);r.ticket=null;}

    @SubscribeEvent public static void stopped(ServerStoppedEvent e){HUNTS.clear();}
    public static void clearAll(){HUNTS.clear();}
    /** A word for the operator: each one's state, way and step. */
    public static String status(BlockPos b){
        var h=HUNTS.get(b.asLong());if(h==null)return "no hunt";
        var out=new StringJoiner("; ");
        for(var r:h.runners.values())out.add(TheListTexts.NAMES[r.who]+" "+r.state.name().toLowerCase(Locale.ROOT)+(r.state==State.RUN?" (way "+r.way+", step "+r.step+")":"")+(r.fought?", fought back":""));
        return out+"; bolted "+h.bolted.size();
    }
    static float yaw(Direction d){return Mth.wrapDegrees(d.toYRot());}
}
