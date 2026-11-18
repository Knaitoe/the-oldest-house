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
    /** Sitting at their place; running; hidden; cornered with nothing left. */
    public enum State{CALM,RUN,HIDE,COWER}
    /** Their gestures, drawn by the client: sat or stood at their place, running, crouched hidden, swinging, cowering. */
    public static final int STILL=0,RUNNING=1,HIDING=2,SWINGING=3,COWERING=4;
    /** How far one of them sees the body in light, and in the dark; how near they hear it whichever way they face. */
    public static final double SEES_LIT=24,SEES_DARK=12,HEARS=6;
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
                w(-121.5,0,-105.5),w(-120.5,0,-105.5),w(-118.5,0,-104.5),w(-114.5,0,-104.5),hide(-114.5,0,-106.5,Direction.WEST)},
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

    // ------------------------------------------------------------------------------------------------ the town's hunt
    /** One of the five, on the hunt: what they are doing, which way and how far along it, whether they have fought back yet. */
    static final class Runner{
        final int who;final UUID id;State state=State.CALM;int way,step,held,since,stuck;boolean fought,yelled;@Nullable Vec3 last;@Nullable ChunkPos ticket;
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
        if(!(e instanceof LiteraryActor a)||!TheListPeople.is(a))return false;
        for(var h:HUNTS.values()){var r=h.runners.get(TheListPeople.who(a));if(r!=null&&r.id.equals(a.getUUID()))return r.state!=State.CALM;}
        return false;
    }
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
            if(l.getEntity(r.id) instanceof LiteraryActor a){BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.gesture(STILL);a.setNoGravity(true);}
        }
        doors(l,b);
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
                    for(var s:slashers)if(!r.fought&&a.distanceTo(s)<=CAUGHT){fight(l,b,h,r,a,s,here);break;}
                    if(r.state==State.RUN)step(l,b,h,r,a,slashers);
                }
                case HIDE->{if(h.tick%5==0)for(var s:slashers)if(finds(a,s)){found(l,b,h,r,a,s,here);break;}}
                case COWER->{var s=nearest(a,slashers);if(s!=null)face(a,s.position());}
            }
        }
    }
    /** Whether one sitting at their place has noticed the slasher: seen in front of them (further in light), or heard close by. */
    static boolean notices(ServerLevel l,LiteraryActor a,ServerPlayer s){
        double d=a.distanceTo(s);if(d<=HEARS)return true;
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
        a.setPose(Pose.STANDING);a.gesture(RUNNING);a.setNoGravity(false);
        if(first){
            var up=UP[r.who];a.moveTo(b.getX()+up.x,b.getY()+up.y,b.getZ()+up.z,a.getYRot(),0);
            l.playSound(null,a.getX(),a.getEyeY(),a.getZ(),TheListRegistry.SCREAM.get(),SoundSource.NEUTRAL,2.2F,.92F+l.getRandom().nextFloat()*.16F);
            say(a,r.who,TheListTexts.RUN[r.who]);
            for(var other:h.runners.values()){
                if(other==r||other.state!=State.CALM||!(l.getEntity(other.id) instanceof LiteraryActor o))continue;
                if(o.distanceTo(a)<=SCREAM_REACH)run(l,b,h,other,o,here,true);
            }
        }
        go(b,r,a);
    }
    /** Toward the step they are on now. */
    static void go(BlockPos b,Runner r,LiteraryActor a){var st=WAYS[r.who][r.way][r.step];BorrascaCast.walk(a,Vec3.atLowerCornerOf(b).add(st.at()),1.0);}
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
        var steps=WAYS[r.who][r.way];var st=steps[r.step];
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
            var steps=WAYS[o.who][o.way];
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
            default->{}
        }
    }

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
