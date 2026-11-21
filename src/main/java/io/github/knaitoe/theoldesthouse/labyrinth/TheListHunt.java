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
    /** Sitting at their place; running; hidden; cornered with nothing left; 0.4.709: held by the slasher; on the hook; 0.4.711: held
     * down over a row of the stands; 0.4.713: on a riser of the sprinkler line; 0.4.714: in the fryer; 0.4.715: the head in a car's
     * door; 0.4.716: the head under a pumpjack's weight; 0.4.717: held under at the stock tank; dead. */
    public enum State{CALM,RUN,HIDE,COWER,HELD,FINISH,BOARDS,LINE,FRYER,CAR,JACK,TANK,AUGER,PRESS,DEAD,LEAVING,LEFT}
    /** Their gestures, drawn by the client: sat or stood at their place, running, crouched hidden, swinging, cowering; 0.4.709: fighting a
     * grip, flinching from the hook, on the knees on it, slumped over it, fallen; 0.4.711: on the knees over a row of the stands, the head
     * through the gap under the next, and hanging there dead. */
    public static final int STILL=0,RUNNING=1,HIDING=2,SWINGING=3,COWERING=4,STRUGGLING=5,RECOIL=6,KNEELING=7,SLUMPED=8,FALLEN=9,OVER=10,HUNG=11;
    /** 0.4.713, the line: on the back across the line, arched over the riser through them, the hands at it; and the same, dead. */
    public static final int IMPALED=12,SPITTED=13;
    /** 0.4.714, the fryer: bent over it with the face held down in the oil, fighting its edges; and the same, still. */
    public static final int FRYING=14,FRIED=15;
    /** 0.4.715, the car door: on the knees at a car with the head pushed in at its driver's door, the hands on the car's side pushing;
     * and slumped there dead, the door shut on the head. */
    public static final int DOORED=16,SLAMMED=17;
    /** 0.4.716, the pumpjack: on the knees beside its gearbox, bent over with the head down on the ground in the near weight's slot,
     * fighting to lift it; and the same, still, the weight down on the head. */
    public static final int PUMPED=18,PINNED=19;
    /** 0.4.717, the stock tank: forced over its rim with the head under; the head up out of it, gasping; still, slumped over it. */
    public static final int DUNKED=20,SURFACED=21,SUNK=22;
    /** 0.4.718, the gin's auger: stood in its trough with the screw drawing the legs down into it, the arms braced on its sides; and
     * slumped forward over it dead, down to the waist. */
    public static final int AUGERED=23,MANGLED=24;
    /** 0.4.719, the gin's press: stood in its chamber shoving up at the ram; bent double under it; folded in it, crushed. */
    public static final int PRESSED=25,BENT=26,FOLDED=27;
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
    /** 0.4.710: the hook's wound, drawn on them from the moment it goes in (LiteraryActor.marks); 0.4.711: the neck's, from the second
     * stamp on the boards. */
    public static final int WOUND=1,NECK=2;
    /** 0.4.713: where the riser went through them, in at the back and out at the belly. */
    public static final int GUT=4;
    /** 0.4.714: the face, scalded in the fryer. */
    public static final int BURN=8;
    /** 0.4.715: the back of the head, where the car door came down on it, from the second slam. */
    public static final int CRUSH=16;
    /** 0.4.716: the side of the head, where the pumpjack's weight came down on it. */
    public static final int FLAT=32;
    /** 0.4.717: the face of one drowned in the stock tank. */
    public static final int DROWNED=64;
    /** 0.4.715: how soon after one slam of the car door the next can come. */
    public static final int SLAM_GAP=12;
    /** 0.4.711, the boards: how near a plank one held must be for the slasher to force them over it, and how soon after one stamp the
     * next counts. */
    public static final double BOARDS_REACH=3;
    public static final int STAMP_GAP=10;
    /** Where the head hangs from where one forced over the row kneels, facing up the rows (west): under the next row's plank. */
    public static final Vec3 HEAD=new Vec3(-.95,-.2,0);
    /** 0.4.710: blood left where it lands, as the fitting's BLOOD stages: drops, a spatter, a smear, a pool. */
    public static final int DROPS=0,SPATTER=1,SMEAR=2,POOL=3;
    /** 0.4.710: how far one of the five sees one of the others lying dead, and a reader who is not the slasher, and how near to where
     * that reader is looking. */
    public static final double FINDS_BODY=16,READER_FINDS=10;
    public static final float READER_LOOK=35F;

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
    static Step[] steps(Runner r){return r.way<r.ways.length?r.ways[r.way]:r.back;}

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
        /** 0.4.710: which of the others they have come across dead, a bit for each. */
        int saw;
        /** 0.4.711: on the boards, the plank (in the town's coordinates), where they kneel on it, how many stamps and when the last was. */
        @Nullable BlockPos plank;@Nullable Vec3 kneel;int stamps,stampAt;
        /** Whether they have let fall what they had in hand (Mel's camcorder): the hook, or forced over the row. */
        boolean dropped;
        /** 0.4.713: on the line, the riser through them (in the town's coordinates), how long the water has run through them and
         * whether it has started to. */
        @Nullable BlockPos riser;int water;boolean flowed;
        /** 0.4.714: in the fryer, how long the hot oil has had them. */
        int fried;
        /** 0.4.715: at the car door: whether it was there they were put (still so dead), how many slams and when the last landed, and
         * whether one is on its way down. */
        boolean doored,slamming;int slams,slamAt;
        /** 0.4.716: at a pumpjack: which (-1 if none, kept when dead), and the well's own tick when last looked at. */
        int jack=-1;double crankAt;
        /** 0.4.717: at the stock tank: the wall cell they were put over (kept when dead) and the way into the water; breath left,
         * whether the head is under, how long it has been up, and how many times it has gone under. */
        @Nullable BlockPos tank;Direction tankIn=Direction.NORTH;int breath,surfaced,dunks;boolean under;
        /** 0.4.718: in the gin's auger: the cell of the run they were put in (kept when dead) and how long the screw has drawn them. */
        @Nullable BlockPos augerAt;int drawn;
        /** 0.4.719: in the gin's press (kept when dead), and how far the ram has come. */
        boolean pressed;int ram;
        /** 0.4.725: the ways they have tonight: their own, or on night three the field house's (TheListHuddle). */
        final Step[][] ways;final Step[] back;final boolean huddled;
        /** 0.4.729: who on Amber's side warned them off, and how long they have been on their way out of town. */
        @Nullable UUID warnedBy;int leaving;
        /** 0.4.730: whether a reader on Amber's side is keeping their heart quiet now, and who has, this hunt. */
        boolean hush;final Set<UUID> hushers=new HashSet<>();
        Runner(int who,UUID id,int night){this.who=who;this.id=id;huddled=TheListHuddle.huddled(night);ways=huddled?TheListHuddle.WAYS[who]:WAYS[who];back=huddled?TheListHuddle.BACK[who]:BACK[who];}
    }
    static final class Hunt{
        final BlockPos base;final Map<Integer,Runner> runners=new LinkedHashMap<>();final Set<BlockPos> bolted=new HashSet<>(),sprung=new HashSet<>();int tick;
        /** 0.4.710: which of the dead each reader has come across, a bit for each. */
        final Map<UUID,Integer> found=new HashMap<>();
        /** 0.4.714: one of the five has been forced into the fryer this hunt; Jenna stays down in the stand's corner until it ends. */
        boolean fryer;
        /** 0.4.718: which of the town's four nights this hunt is (one, for a hunt begun by hand without one); the gin runs on the fourth. */
        int night=1;
        Hunt(BlockPos b){base=b;}
    }
    static final Map<Long,Hunt> HUNTS=new HashMap<>();
    private static final TicketType<UUID> TICKET=TicketType.create("the_list_hunt",UUID::compareTo);

    public static boolean on(BlockPos b){return HUNTS.containsKey(b.asLong());}
    /** 0.4.714: whether one of the five has been forced into the fryer this hunt. */
    public static boolean fryerUsed(BlockPos b){var h=HUNTS.get(b.asLong());return h!=null&&h.fryer;}
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
    /** For a check: one of the five hidden where they stand, as at the end of a way. */
    public static void hideNow(ServerLevel l,BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);if(r!=null&&l.getEntity(r.id) instanceof LiteraryActor a){BorrascaCast.stop(a);hide(r,a,a.position(),Direction.NORTH);}}
    /** 0.4.730: whether one of the five has their heart kept quiet now (TheListAmber). */
    public static boolean hushed(Entity e){var r=runner(e);return r!=null&&r.hush;}
    static boolean gone(Runner r){return r.state==State.DEAD||r.state==State.LEFT||r.state==State.FINISH&&r.since>=OUT;}
    static @Nullable Runner runner(Entity e){
        if(!(e instanceof LiteraryActor a)||!TheListPeople.is(a))return null;
        for(var h:HUNTS.values()){var r=h.runners.get(TheListPeople.who(a));if(r!=null&&r.id.equals(a.getUUID()))return r;}
        return null;
    }
    /** 0.4.709: whether a reader has hold of one of the five (or the hook in them): they cannot leave by the House's door. */
    public static boolean holding(ServerPlayer p){for(var h:HUNTS.values())for(var r:h.runners.values())if(p.getUUID().equals(r.holder))return true;return false;}
    /** Whether one of them is dead. */
    public static boolean dead(BlockPos b,int who){return state(b,who)==State.DEAD;}
    /** 0.4.729: whether one of them has left town tonight, warned off by a reader on Amber's side; and who warned them. */
    public static boolean left(BlockPos b,int who){return state(b,who)==State.LEFT;}
    public static @Nullable UUID warnedBy(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?null:r.warnedBy;}
    /** 0.4.729: how long one warned off must be on their way before they can be gone, and how far they are seen. */
    public static final int LEAVE_MIN=100;
    public static final double LEAVE_SEEN=48;
    /** 0.4.729: where they make for, out of town: south down Avenue K, past the pasture. */
    public static final Vec3 OUT_OF_TOWN=new Vec3(TheListSite.AVE_LINE+.5,0,TheListSite.SOUTH-8.5);
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
    public static void start(ServerLevel l,BlockPos b){start(l,b,1);}
    /** 0.4.718: begun on one of the four nights. */
    public static void start(ServerLevel l,BlockPos b,int night){
        end(l,b);
        sweep(l,b);
        tidy(l,b);
        doors(l,b);
        TheListSite.line(l,b);
        TheListSite.fryer(l,b);
        TheListCar.reset(b);
        TheListJack.reset(l,b);
        TheListPeople.ensure(l,b);
        TheListGin.reset(b);
        var h=new Hunt(b);h.night=Math.max(1,Math.min(4,night));
        for(int who:TheListBody.LISTED){var a=TheListPeople.find(l,b,who);if(a!=null)h.runners.put(who,new Runner(who,a.getUUID(),h.night));}
        // 0.4.725: on night three they are in the field house with its doors bolted, and the field's lights are on.
        if(TheListHuddle.huddled(h.night))TheListHuddle.bolt(h);
        TheListHuddle.lights(l,b,TheListHuddle.huddled(h.night));
        HUNTS.put(b.asLong(),h);
        TheListPeople.ensure(l,b);
    }
    /** Ended: everyone stood up and sent back to their place, the doors put back. */
    public static void end(ServerLevel l,BlockPos b){
        var h=HUNTS.remove(b.asLong());if(h==null)return;
        TheListGin.reset(b);TheListAmber.clear(b);
        for(var r:h.runners.values()){
            release(l,r);
            if(r.holder!=null){var p=l.getServer().getPlayerList().getPlayer(r.holder);if(p!=null)TheListBody.holding(p,false);}
            if(l.getEntity(r.id) instanceof LiteraryActor a){BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.gesture(STILL);a.setNoGravity(true);a.holder(null);a.marks(0);
                // 0.4.719: one in the press is brought out of its chamber before it is shut again.
                if(r.pressed){var out=TheListGin.front(b);a.moveTo(out.x,out.y,out.z,a.getYRot(),0);}}
        }
        TheListGin.shut(l,b);
        TheListHuddle.lights(l,b,false);
        doors(l,b);
        sweep(l,b);
        tidy(l,b);
        TheListSite.line(l,b);
        TheListSite.fryer(l,b);
        TheListCar.reset(b);
        TheListJack.reset(l,b);
        TheListPeople.ensure(l,b);
    }
    /** The three doors as they were built: shut, and nobody's bolt on them. */
    static void doors(ServerLevel l,BlockPos b){
        TheListSite.steelDoor(l,b,FIELD_DOOR,Direction.NORTH,false);TheListSite.steelDoor(l,b,FIELD_DOOR.east(),Direction.NORTH,true);
        TheListSite.steelDoor(l,b,PRESS_DOOR,Direction.WEST,false);
        TheListSite.door(l,b,STAND_DOOR,Direction.EAST,true);
        // 0.4.725: the field house's street door, bolted on night three.
        TheListSite.steelDoor(l,b,TheListHuddle.STREET_DOOR,Direction.SOUTH,false);
    }

    // ------------------------------------------------------------------------------------------------ each tick
    /** The town's tick, while someone is in it. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=HUNTS.get(b.asLong());if(h==null)return;h.tick++;
        // 0.4.722: one of the five whose place was not loaded when the hunt began is taken on once they are there.
        if(h.tick%20==0)for(int who:TheListBody.LISTED)if(!h.runners.containsKey(who)){var a=TheListPeople.find(l,b,who);if(a!=null)h.runners.put(who,new Runner(who,a.getUUID(),h.night));}
        var slashers=new ArrayList<ServerPlayer>();
        for(var p:here){var live=TheListBody.LIVE.get(p.getUUID());if(live!=null&&live.down==0&&!p.isSpectator())slashers.add(p);}
        if(h.fryer)TheListPeople.cornered(l,b);
        // A door the slasher has broken is nobody's to bolt now.
        h.bolted.removeIf(at->{var st=l.getBlockState(b.offset(at));return !(st.getBlock() instanceof DoorBlock)||st.getValue(DoorBlock.OPEN)||st.is(TheListRegistry.BROKEN_DOOR.get());});
        for(var r:h.runners.values()){
            if(!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            r.since++;
            // 0.4.730: hidden or cornered, a heart kept quiet by a reader crouched beside them.
            r.hush=false;
            if(r.state==State.HIDE||r.state==State.COWER)for(var q:here)if(TheListAmber.hushes(q,a)){r.hush=true;if(r.hushers.add(q.getUUID()))TheListAmber.hushed(q,r.who);}
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
                case BOARDS->boarded(l,b,h,r,a,slashers,here);
                case LINE->lined(l,b,h,r,a,here);
                case FRYER->frying(l,b,h,r,a,slashers,here);
                case CAR->dooring(l,b,h,r,a,slashers,here);
                case JACK->jacking(l,b,h,r,a,slashers,here);
                case TANK->tanking(l,b,h,r,a,slashers,here);
                case AUGER->augering(l,b,h,r,a,here);
                case PRESS->pressing(l,b,h,r,a,here);
                case DEAD,LEFT->{}
                // 0.4.729: on their way out of town; caught up with, they fight once, then cower; out of everyone's sight, gone.
                case LEAVING->{
                    lease(l,r,a);r.leaving++;
                    var s=nearest(a,slashers);
                    if(s!=null&&a.distanceTo(s)<=CAUGHT){
                        if(!r.fought)fight(l,b,h,r,a,s,here);
                        else{r.state=State.COWER;BorrascaCast.stop(a);a.setPose(Pose.CROUCHING);a.gesture(COWERING);face(a,s.position());say(a,r.who,TheListTexts.COWER[r.who]);}
                    }else if(r.leaving>=LEAVE_MIN&&unseen(l,a))leave(l,b,h,r,a,here);
                    else if(!a.getPersistentData().contains("BorrascaSpeed"))BorrascaCast.walk(a,Vec3.atLowerCornerOf(b).add(OUT_OF_TOWN),1.0);
                }
            }
        }
        if(h.tick%5==1)bodies(l,b,h,here);
        TheListGin.tick(l,b,h.tick,here);
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
            if(seated&&!r.huddled){var up=UP[r.who];a.moveTo(b.getX()+up.x,b.getY()+up.y,b.getZ()+up.z,a.getYRot(),0);}
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
        // 0.4.710: the camcorder tears the sack; the dirt stays in its weave.
        if(r.who==TheListPeople.MEL)TheListBody.mark(s,TheListSack.TORN);
        if(r.who==TheListPeople.LESLEY)TheListBody.mark(s,TheListSack.DIRT);
        TheListBody.felled(s,TheListTexts.FELLED[r.who],a.position());
        if(r.state==State.HIDE){
            if(r.way+1<r.ways.length){r.way++;run(l,b,h,r,a,here,false);}
            else{r.state=State.COWER;a.setPose(Pose.CROUCHING);a.gesture(COWERING);}
        }else a.gesture(RUNNING);
    }
    /**
     * 0.4.729: warned off by a reader on Amber's side: up from wherever they are and away out of town, south down the highway. The
     * one who warned them is told so; the town's hearts lose that one once they are out of everyone's sight.
     */
    static void warn(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer p){
        boolean seated=r.state==State.CALM&&a.gesture()==STILL;
        r.state=State.LEAVING;r.warnedBy=p.getUUID();r.leaving=0;r.since=0;r.stuck=0;r.last=null;
        a.setPose(Pose.STANDING);a.gesture(RUNNING);a.setNoGravity(false);
        if(seated&&!r.huddled){var up=UP[r.who];a.moveTo(b.getX()+up.x,b.getY()+up.y,b.getZ()+up.z,a.getYRot(),0);}
        face(a,p.position());
        say(a,r.who,TheListTexts.LEAVING[r.who]);
        TheList.cue(p,String.format(TheListTexts.WARNED,TheListTexts.NAMES[r.who]));
        BorrascaCast.walk(a,Vec3.atLowerCornerOf(b).add(OUT_OF_TOWN),1.0);
    }
    /** 0.4.729: whether nobody in the level, reader or spectator, can see them. */
    static boolean unseen(ServerLevel l,LiteraryActor a){
        for(var q:l.players())if(q.distanceTo(a)<=LEAVE_SEEN&&q.hasLineOfSight(a))return false;
        return true;
    }
    /** 0.4.729: out of town. The one who warned them keeps it; the slasher hears one heart fewer. */
    static void leave(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        r.state=State.LEFT;BorrascaCast.stop(a);release(l,r);a.discard();
        var d=LabyrinthData.get(l.getServer());
        if(r.warnedBy!=null){var own=LiteraryVignettes.personal(d,r.warnedBy,TheList.PLACE);own.putInt("GotOut",own.getInt("GotOut")|1<<r.who);LiteraryVignettes.save(d,r.warnedBy,TheList.PLACE,own);}
        for(var q:here){
            if(TheListBody.LIVE.containsKey(q.getUUID()))TheList.cue(q,TheListTexts.HEART_GONE);
            else if(q.getUUID().equals(r.warnedBy))TheList.cue(q,String.format(TheListTexts.GOT_OUT,TheListTexts.NAMES[r.who]));
        }
    }
    /** A blow from the slasher on one of them: one at their place runs; one hiding is found; one running fights back if they have not. */
    public static void struck(ServerPlayer p,LiteraryActor a){
        var b=TheListBody.town(p);if(b==null)return;var h=HUNTS.get(b.asLong());if(h==null)return;
        // 0.4.727: Stace, once her plan has begun, is fought on her own terms.
        if(TheListPeople.who(a)==TheListPeople.STACE){if(TheListStace.on(b))TheListStace.struck(p,a,b);return;}
        var r=h.runners.get(TheListPeople.who(a));if(r==null||!r.id.equals(a.getUUID())||!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return;
        var l=p.serverLevel();var here=TheList.readers(l,b);
        switch(r.state){
            case CALM->run(l,b,h,r,a,here,true);
            case HIDE->found(l,b,h,r,a,p,here);
            case RUN,LEAVING->{if(!r.fought)fight(l,b,h,r,a,p,here);}
            // 0.4.709: a blow on one cornered takes hold of them; on one held, with the hook in hand, it is the hook.
            case COWER->take(l,b,h,r,a,p,here);
            case HELD->{if(p.getUUID().equals(r.holder)&&TheListBody.hookInHand(p))finish(r,a,p);}
            // 0.4.711: a blow on one held over the row is a stamp.
            case BOARDS->{if(p.getUUID().equals(r.holder))stamp(l,b,h,r,a,p,here);}
            // 0.4.713: one on the riser does not come off it.
            case LINE->TheList.cue(p,String.format(TheListTexts.PINNED,TheListTexts.NAMES[r.who]));
            // 0.4.715: a blow on one with the head in the car door is another slam.
            case CAR->{if(p.getUUID().equals(r.holder))slam(l,b,h,r,a,p);}
            // 0.4.717: a blow on one come up out of the tank puts the head back under.
            case TANK->{if(p.getUUID().equals(r.holder)&&!r.under)dunk(l,b,h,r,a,p);}
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
        // 0.4.713: nobody lifts one off the riser, the slasher included.
        if(r.state==State.LINE){TheList.cue(p,String.format(TheListTexts.PINNED,name));return true;}
        // 0.4.714: one held in the fryer is the slasher's; anyone else's hand does not get them out.
        if(r.state==State.FRYER){if(!p.getUUID().equals(r.holder))TheList.cue(p,String.format(TheListTexts.FRYER_HELD,name));return true;}
        // 0.4.715: one in the car door: the slasher's hand slams it again; anyone else's finds the head caught.
        if(r.state==State.CAR){if(p.getUUID().equals(r.holder))slam(p.serverLevel(),b,h,r,a,p);else TheList.cue(p,String.format(TheListTexts.DOOR_HELD,name));return true;}
        // 0.4.716: one under the weight: the slasher's hand knocks the brake off if it is on; anyone else's finds the head held down.
        // 0.4.717: one at the stock tank: the slasher's hand puts the head back under once it is up; anyone else's finds them held under.
        if(r.state==State.TANK){
            if(p.getUUID().equals(r.holder)){if(!r.under)dunk(p.serverLevel(),b,h,r,a,p);}
            else TheList.cue(p,String.format(TheListTexts.TANK_HELD,name));
            return true;
        }
        // 0.4.718: one in the gin's auger is the screw's; a hand on them, the slasher's or anyone's, finds them held down in it.
        if(r.state==State.AUGER){TheList.cue(p,String.format(TheListTexts.AUGER_HELD,name));return true;}
        // 0.4.719: one in the gin's press: the slasher's hand finds them held; anyone else's hauls the release.
        if(r.state==State.PRESS)return press(p,b);
        if(r.state==State.JACK){
            if(p.getUUID().equals(r.holder)){if(TheListJack.braked(p.serverLevel(),b,r.jack))TheListJack.brake(p.serverLevel(),b,r.jack,false,p);}
            else TheList.cue(p,String.format(TheListTexts.JACK_HELD,name));
            return true;
        }
        // 0.4.729: a hand from a reader on Amber's side, on one still able to run: warned off, and on their way out of town.
        if(!TheListBody.LIVE.containsKey(p.getUUID())&&LiteraryVignettes.participant(p)
            &&(r.state==State.CALM||r.state==State.RUN||r.state==State.HIDE||r.state==State.COWER)){warn(p.serverLevel(),b,h,r,a,p);return true;}
        if(r.state==State.LEAVING&&!TheListBody.LIVE.containsKey(p.getUUID())){TheList.cue(p,String.format(TheListTexts.ALREADY_LEAVING,name));return true;}
        if(!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return r.state!=State.CALM;
        var l=p.serverLevel();var here=TheList.readers(l,b);
        switch(r.state){
            case CALM->{if(sees(a,p))run(l,b,h,r,a,here,true);else take(l,b,h,r,a,p,here);}
            case RUN,LEAVING->TheList.cue(p,String.format(TheListTexts.TWIST,name));
            case HIDE->found(l,b,h,r,a,p,here);
            case COWER->take(l,b,h,r,a,p,here);
            case HELD->{if(p.getUUID().equals(r.holder)&&TheListBody.hookInHand(p))finish(r,a,p);}
            case BOARDS->{if(p.getUUID().equals(r.holder))stamp(l,b,h,r,a,p,here);}
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
        // 0.4.715: out of the car door, it is pushed wide as they pull the head out.
        if(r.doored&&TheListCar.angle(b)<TheListCar.OPEN-1)TheListCar.swing(b,null,new TheListCar.Leg(TheListCar.OPEN,4));
        r.holder=null;r.killer=null;a.holder(null);a.setNoGravity(false);r.plank=null;r.kneel=null;r.stamps=0;r.doored=false;r.slams=0;r.slamming=false;r.jack=-1;r.tank=null;r.under=false;r.dunks=0;r.surfaced=0;r.augerAt=null;r.drawn=0;r.pressed=false;r.ram=0;
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
    public static boolean dropped(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r!=null&&(r.dropped||r.state==State.FINISH||r.state==State.DEAD);}
    /** Anything let fall during a hunt, taken up when it ends or another begins. */
    static void sweep(ServerLevel l,BlockPos b){
        for(var it:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,TheListBody.bounds(b),e->e.getTags().contains(DROPPED)))it.discard();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.709: the hay hook
    /** The hook, on one held: they see it come up. */
    static void finish(Runner r,LiteraryActor a,ServerPlayer s){
        if(r.state!=State.HELD)return;
        r.state=State.FINISH;r.since=0;r.killer=s.getUUID();r.dropped=true;a.gesture(RECOIL);s.swing(InteractionHand.MAIN_HAND,true);
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
            // 0.4.710: the wound on them; drops under them and a spatter where the slasher stands; blood on the slasher.
            a.marks(a.marks()|WOUND);
            stain(l,b,a.position(),DROPS,toward(a,s));
            if(s!=null){stain(l,b,s.position(),SPATTER,toward(a,s));TheListBody.bleed(s);}
            if(s!=null)TheList.cue(s,TheListTexts.HOOK_IN);
            for(var q:here)if(q!=s&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
        }
        if(t==RAISE+5)stop(l,a,TheListRegistry.SCREAM.get());
        if(t>RAISE&&t<OUT&&there)pin(a,s,r.pin,KNELT_AT);
        if(t>RAISE&&t<FOLD&&t%2==0)drip(l,a);
        if(t==KNEES&&s!=null)TheList.cue(s,String.format(TheListTexts.HOOK_KNEES,name));
        if(t==KNEES)stain(l,b,a.position().add(Vec3.directionFromRotation(0,a.getYRot()).scale(.6)),DROPS,toward(a,s));
        if(t==OUT){
            l.playSound(null,a.getX(),a.getY()+1.2,a.getZ(),TheListRegistry.HOOK_OUT.get(),SoundSource.NEUTRAL,1F,1F);
            spray(l,a,s,14);a.gesture(SLUMPED);
            if(s!=null){s.swing(InteractionHand.MAIN_HAND,true);TheListBody.bloody(s);TheListBody.bleed(s);TheList.cue(s,String.format(TheListTexts.HOOK_OUT,name));
                stain(l,b,a.position().add(s.position()).scale(.5),SPATTER,toward(a,s));}
        }
        if(t==FOLD){
            // Face down, the knees where they knelt and the head toward the slasher's feet.
            // Dragged down off the knees as they go: a smear from where they knelt, the way they fall.
            stain(l,b,a.position(),SMEAR,toward(a,s));
            // The wider box the fallen take is set at the height they knelt at: the game would lift a body whose box grew to the middle
            // of the old one, and the cast do not fall of themselves.
            double ground=a.getY();
            a.gesture(FALLEN);a.setNoGravity(false);a.setPos(a.getX(),ground,a.getZ());
            if(s!=null){
                float yaw=r.pin;var to=new Vec3(s.getX()-Mth.sin(yaw*Mth.DEG_TO_RAD)*1.6,ground,s.getZ()+Mth.cos(yaw*Mth.DEG_TO_RAD)*1.6);
                if(l.noCollision(a,a.getDimensions(a.getPose()).makeBoundingBox(to).deflate(.05)))a.moveTo(to.x,to.y,to.z,a.getYRot(),0);
                TheListBody.holding(s,false);
            }
            r.holder=null;a.holder(null);
        }
        if(t>=DONE){
            r.state=State.DEAD;
            // A pool spreading out under the chest.
            pool(l,b,a.position().add(Vec3.directionFromRotation(0,a.getYRot()).scale(.4)),a.getDirection());
            // 0.4.710: the kill is theirs even if they have logged off since the hook went in.
            if(r.killer!=null)TheListBody.killed(l.getServer(),r.killer,r.who);
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

    // ------------------------------------------------------------------------------------------------ 0.4.711: the boards
    /** The home stands: their front, and the ends of their rows. Row r stands at x = STANDS_FRONT-1-r, its plank's top (r+1)/2 above
     * the ground. */
    static final int STANDS_FRONT=TheListSite.FIELD_X-22,STANDS_N=TheListSite.FIELD_Z-16,STANDS_S=TheListSite.FIELD_Z+16;
    /** Which row of the home stands a plank is, in the town's coordinates: 1 to 6, a row with a row above it and the dirt open under
     * that one (under row 1 the plank's own block fills the cell); otherwise -1. */
    public static int row(ServerLevel l,BlockPos b,BlockPos rel){
        int r=STANDS_FRONT-1-rel.getX();
        if(r<1||r>6||rel.getY()!=r/2||rel.getZ()<STANDS_N||rel.getZ()>STANDS_S)return -1;
        var st=l.getBlockState(b.offset(rel));
        return TheListBlock.is(st,TheListBlock.Kind.STANDS)&&st.getValue(TheListBlock.STAGE)==r%2?r:-1;
    }
    /**
     * A hand or a blow on a plank of the home stands (TheList.click, TheListBody.punched). The slasher's, with hold of one of the five
     * within reach of it, forces them down over that row; with one already over a row, it is a stamp, on that plank or on the next one up
     * beside where they lie. True when it was either; anyone else's, or a plank out of reach, is left to the town.
     */
    public static boolean boards(ServerPlayer p,BlockPos b,BlockPos rel){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator()||!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(r.state==State.BOARDS){
                if(r.plank==null||Math.abs(rel.getZ()-r.plank.getZ())>1||rel.getX()!=r.plank.getX()&&rel.getX()!=r.plank.getX()-1)return false;
                stamp(l,b,h,r,a,p,TheList.readers(l,b));
                return true;
            }
            if(r.state!=State.HELD)return false;
            int row=row(l,b,rel);
            if(row<0||Vec3.atCenterOf(b.offset(rel)).distanceTo(a.position())>BOARDS_REACH)return false;
            over(l,b,h,r,a,p,rel,row,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Forced over the row: down on the knees on the plank facing up the rows, bent over its far edge, the head through the gap under the
     * next row and hanging in the dark under the stands. Never across a post of the frame: the cell beside, toward where they were. Their
     * words and a scream; anyone else on the list near enough hears it. What they had in hand they let fall.
     */
    static void over(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,BlockPos rel,int row,List<ServerPlayer> here){
        if(Math.floorMod(rel.getZ(),4)==0)rel=rel.offset(0,0,a.getZ()>=b.getZ()+rel.getZ()+.5?1:-1);
        r.state=State.BOARDS;r.since=0;r.stamps=0;r.stampAt=0;r.plank=rel;r.dropped=true;
        r.kneel=new Vec3(b.getX()+rel.getX()+.6,b.getY()+(row+1)*.5,b.getZ()+rel.getZ()+.5);
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(OVER);drop(a);
        kneel(a,r);
        TheList.cue(s,String.format(TheListTexts.FORCED_OVER,TheListTexts.NAMES[r.who]));
        say(a,r.who,TheListTexts.OVER[r.who]);
        scream(l,a);
        l.playSound(null,r.kneel.x,r.kneel.y,r.kneel.z,TheListRegistry.STAMP.get(),SoundSource.NEUTRAL,.5F,1.25F);
        heard(l,b,h,r,a,here);
    }
    /** Kept where they kneel over the row, facing up the rows. */
    static void kneel(LiteraryActor a,Runner r){
        if(r.kneel==null)return;
        a.moveTo(r.kneel.x,r.kneel.y,r.kneel.z,90,0);a.setYBodyRot(90);a.setYHeadRot(90);a.setDeltaMovement(Vec3.ZERO);
    }
    /** Where the head hangs, under the next row. */
    public static @Nullable Vec3 head(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null||r.kneel==null?null:r.kneel.add(HEAD);}
    /** How many times they have been stamped on. */
    public static int stamps(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.stamps;}
    /**
     * Over the row, each tick: kept there. Left without a stamp they fight the plank as they fight a grip, longer while the slasher looks
     * at them, and after the first stamp it takes them twice as long; after the second they cannot. Bleeding from the head after the
     * first. The slasher gone from the town, they tear loose, unless it is past that.
     */
    static void boarded(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers,List<ServerPlayer> here){
        kneel(a,r);
        if(r.stamps>=1&&r.since%4==0&&r.kneel!=null){var at=r.kneel.add(HEAD);l.sendParticles(TheListRegistry.BLOOD.get(),at.x,at.y-.15,at.z,1,.04,.02,.04,0);}
        if(r.stamps>=2)return;
        var s=holder(r,slashers);
        if(s==null){loose(l,b,h,r,a,null,here);return;}
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+.5,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,1F,1F);
        float off=Math.abs(Mth.wrapDegrees(yawTo(s.position(),a.position())-s.getYHeadRot()));
        r.strength+=off>LOOK_AWAY?2:1;
        if(r.strength>=STRENGTH[r.who]*(r.stamps>=1?2:1))loose(l,b,h,r,a,s,here);
    }
    /**
     * A stamp, at least STAMP_GAP ticks after the last. The first: the row rings, they jerk, and blood drops from the head onto the dirt
     * under the stands. The second: something gives, blood sprays onto the dirt, more on the slasher, and the neck is marked. The third:
     * still, the scream stopped, anyone else in town near enough told from which way; hung over the row dead, a pool under the head, the
     * kill the slasher's.
     */
    static void stamp(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,List<ServerPlayer> here){
        if(r.stamps>=3||r.kneel==null||r.stamps>0&&r.since-r.stampAt<STAMP_GAP)return;
        r.stamps++;r.stampAt=r.since;r.killer=s.getUUID();
        s.swing(InteractionHand.MAIN_HAND,true);
        var head=r.kneel.add(HEAD);var ground=ground(l,b,head);
        l.playSound(null,r.kneel.x-.5,r.kneel.y,r.kneel.z,TheListRegistry.STAMP.get(),SoundSource.NEUTRAL,1.2F,.92F+l.getRandom().nextFloat()*.12F);
        var name=TheListTexts.NAMES[r.who];
        var facing=Direction.WEST;
        switch(r.stamps){
            case 1->{
                TheList.cue(s,TheListTexts.STAMPS[0]);
                l.sendParticles(TheListRegistry.BLOOD.get(),head.x,head.y-.15,head.z,6,.05,.05,.05,0);
                if(ground!=null)stain(l,b,ground,DROPS,facing);
            }
            case 2->{
                stop(l,a,TheListRegistry.STRUGGLE.get());
                l.playSound(null,head.x,head.y,head.z,TheListRegistry.CRACK.get(),SoundSource.NEUTRAL,1F,.95F+l.getRandom().nextFloat()*.1F);
                TheList.cue(s,TheListTexts.STAMPS[1]);
                for(int i=0;i<18;i++){var rand=l.getRandom();
                    l.sendParticles(TheListRegistry.BLOOD.get(),head.x,head.y,head.z,0,(rand.nextDouble()-.5)*.25,-.05-rand.nextDouble()*.15,(rand.nextDouble()-.5)*.25,1);}
                if(ground!=null)stain(l,b,ground,SPATTER,facing);
                a.marks(a.marks()|NECK);
                TheListBody.bleed(s);
            }
            default->{
                stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
                TheList.cue(s,String.format(TheListTexts.STAMPS[2],name));
                for(var q:here)if(q!=s&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
                a.gesture(HUNG);kneel(a,r);
                if(ground!=null)pool(l,b,ground,facing);
                TheListBody.holding(s,false);r.holder=null;a.holder(null);
                r.state=State.DEAD;
                TheListBody.killed(l.getServer(),r.killer,r.who);
            }
        }
    }
    /** The dirt under a point under the stands: the open cell over it, or the nearest beside it along the row if a post of the frame
     * stands there. */
    static @Nullable Vec3 ground(ServerLevel l,BlockPos b,Vec3 over){
        var cell=BlockPos.containing(over.x,b.getY(),over.z);
        for(int dz:new int[]{0,1,-1}){
            var at=cell.offset(0,0,dz);
            if(l.getBlockState(at).isAir()&&l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP))return new Vec3(over.x,b.getY(),over.z+dz);
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------------ 0.4.713: the line
    /**
     * A hand or a blow on a riser of the sprinkler line (TheListLine.hand). The slasher's, with hold of one of the five within reach of
     * it, forces them onto it. True when it did.
     */
    public static boolean riser(ServerPlayer p,BlockPos b,BlockPos rel){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator()||!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||r.state!=State.HELD||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            var top=Vec3.atBottomCenterOf(b.offset(rel)).add(0,.6,0);
            if(top.distanceTo(a.position().add(0,.6,0))>TheListLine.REACH)return false;
            onto(l,b,h,r,a,p,rel,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Onto the riser: forced over backward onto it, and it goes through them, the head out through the front. They lie on their back
     * across the line, arched over it, the head on the far side from the slasher, the hands at the head where it comes out. Their words
     * and a scream; anyone else on the list near enough hears it and runs. What they had in hand they let fall; the riser runs with
     * blood. The slasher's walk is given back, but the hold is still theirs until the one on the riser is dead.
     */
    static void onto(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,BlockPos rel,List<ServerPlayer> here){
        r.state=State.LINE;r.since=0;r.water=0;r.flowed=false;r.riser=rel;r.dropped=true;r.killer=s.getUUID();
        // Across the line, the head away from the slasher: the way they faced the slasher, squared to the line's east or west.
        r.pin=Mth.wrapDegrees(r.pin+180)>0?90:-90;
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(IMPALED);drop(a);
        spit(b,a,r);
        a.marks(a.marks()|GUT);
        TheListBody.holding(s,false);
        var at=b.offset(rel);var st=l.getBlockState(at);
        if(TheListBlock.is(st,TheListBlock.Kind.LINE))l.setBlock(at,st.setValue(TheListBlock.STAGE,TheListLine.BLOODY),3);
        var name=TheListTexts.NAMES[r.who];
        TheList.cue(s,String.format(TheListTexts.ONTO_RISER,name));
        say(a,r.who,TheListTexts.RISER[r.who]);
        scream(l,a);
        l.playSound(null,at.getX()+.5,at.getY()+.8,at.getZ()+.5,TheListRegistry.RISER.get(),SoundSource.NEUTRAL,1.2F,.95F+l.getRandom().nextFloat()*.1F);
        var belly=Vec3.atBottomCenterOf(at).add(0,.9,0);
        l.sendParticles(TheListRegistry.BLOOD.get(),belly.x,belly.y,belly.z,14,.15,.08,.15,.04);
        for(var side:new int[]{-1,1})stain(l,b,Vec3.atBottomCenterOf(at).add(side*.9,0,0),DROPS,side<0?Direction.WEST:Direction.EAST);
        heard(l,b,h,r,a,here);
    }
    /** Kept where they lie, across the line over the riser. */
    static void spit(BlockPos b,LiteraryActor a,Runner r){
        if(r.riser==null)return;
        double x=b.getX()+r.riser.getX()+.5,y=b.getY()+r.riser.getY(),z=b.getZ()+r.riser.getZ()+.5;
        a.moveTo(x,y,z,r.pin,0);a.setYBodyRot(r.pin);a.setYHeadRot(r.pin);a.setDeltaMovement(Vec3.ZERO);
    }
    /** Whether one of the five lies on this riser, living or dead: its head throws what comes out of them. */
    public static boolean onRiser(BlockPos b,BlockPos rel){
        var h=HUNTS.get(b.asLong());if(h==null)return false;
        for(var r:h.runners.values())if(rel.equals(r.riser)&&(r.state==State.LINE||r.state==State.DEAD))return true;
        return false;
    }
    /** How long the water has run through one of them. */
    public static int water(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.water;}
    /**
     * On the riser, each tick: kept there; bleeding, and fighting it, while the water is off. With the water on, the head inside them
     * turns, and what it throws out over the grass is theirs: the first time, a scream and everyone near told; stains where it falls;
     * after BLEEDS ticks of it, still.
     */
    static void lined(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        spit(b,a,r);
        if(r.riser==null)return;
        var at=Vec3.atBottomCenterOf(b.offset(r.riser));
        // Once, a moment after: where the valve is, to the one who put them there.
        if(r.since==40&&!r.flowed&&r.holder!=null){var s=l.getServer().getPlayerList().getPlayer(r.holder);if(s!=null)TheList.cue(s,TheListTexts.VALVE_HINT);}
        if(!TheListLine.open(l,b)){
            if(r.since%6==0)l.sendParticles(TheListRegistry.BLOOD.get(),at.x,at.y+.85,at.z,1,.12,.02,.12,0);
            if(r.since%STRUGGLE_EVERY==0)l.playSound(null,at.x,at.y+.8,at.z,TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,.8F,1.1F);
            return;
        }
        var name=TheListTexts.NAMES[r.who];
        if(!r.flowed){
            r.flowed=true;
            stop(l,a,TheListRegistry.STRUGGLE.get());
            scream(l,a);
            l.playSound(null,at.x,at.y+.9,at.z,TheListRegistry.GASP.get(),SoundSource.NEUTRAL,1F,.8F);
            for(var q:here)if(!q.isSpectator()&&q.position().distanceTo(at)<=TheListLine.HEARD)TheList.cue(q,String.format(TheListTexts.WATER_THROUGH,name));
        }
        r.water++;
        int i=TheListLine.riserAt(r.riser);
        if(i>=0&&r.water%15==0&&r.water<TheListLine.BLEEDS){
            // Where the spray carries it this time, on open ground it has not already reddened: the first such cell out along the
            // head's arc from a random reach (round to the nearest if none is further out); 0.4.716: and if there is none that way (the
            // arc over the stands, or all of it red already), the nearest such cell to either side of it, the way the wind carries it.
            double from=1.6+l.getRandom().nextDouble()*1.8,dir=TheListLine.angle(i,(int)l.getGameTime());var lands=TheListLine.fallsAt(b,i,dir,from);
            search:
            for(int turn=0;turn<=12;turn++)for(int side:turn==0?new int[]{0}:new int[]{1,-1}){
                double turned=dir+side*Math.toRadians(15*turn);
                for(double reach:new double[]{from,from+.9,from-.9,from+1.8,from-1.8,3.4,2.5,1.6}){
                    if(reach<1.2)continue;var at2=TheListLine.fallsAt(b,i,turned,reach);
                    if(fresh(l,at2)){lands=at2;break search;}
                }
            }
            var d=lands.subtract(at);
            stain(l,b,lands,r.water>=30?SPATTER:DROPS,Direction.getNearest(d.x,0,d.z));
        }
        if(r.water<TheListLine.BLEEDS)return;
        // Still.
        stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
        a.gesture(SPITTED);r.state=State.DEAD;
        var killer=r.killer==null?null:l.getServer().getPlayerList().getPlayer(r.killer);
        if(killer!=null)TheList.cue(killer,String.format(TheListTexts.LINE_STILL,name));
        for(var q:here)if(q!=killer&&!q.isSpectator()&&q.position().distanceTo(at)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),at)));
        for(var side:new int[]{-1,1})stain(l,b,at.add(side*.9,0,0),POOL,side<0?Direction.WEST:Direction.EAST);
        if(killer!=null)TheListBody.holding(killer,false);
        r.holder=null;a.holder(null);
        if(r.killer!=null)TheListBody.killed(l.getServer(),r.killer,r.who);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.714: the fryer
    /**
     * A hand or a blow on the fryer (TheListFryer.hand). The slasher's, with hold of one of the five within reach of it: into the oil
     * if it is hot, told it is not hot yet if it is heating. Off, it is the knob (false), turned with the other hand. With one already in
     * it, their own hand on it does nothing more.
     */
    public static boolean fryer(ServerPlayer p,BlockPos b){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator()||!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(r.state==State.FRYER)return true;
            if(r.state!=State.HELD)return false;
            if(Vec3.atCenterOf(b.offset(TheListFryer.AT)).distanceTo(a.position().add(0,.6,0))>TheListFryer.REACH)return false;
            var st=TheListFryer.state(l,b);if(st==null)return false;
            int heat=TheListFryer.heat(st);
            if(heat==TheListFryer.OFF)return false;
            if(heat==TheListFryer.WARM){TheList.cue(p,TheListTexts.FRYER_NOT_HOT);return true;}
            into(l,b,h,r,a,p,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Into the oil: stood in front of the fryer, bent over it, the face pushed down into the west vat. Their words as they go over, a
     * scream, the oil's roar; anyone else on the list near enough hears it and runs. What they had in hand they let fall. Jenna, in the
     * stand, shouts at the slasher. The hold is still the slasher's: their walk stays stilled while they hold them under.
     */
    static void into(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,List<ServerPlayer> here){
        r.state=State.FRYER;r.since=0;r.fried=0;r.dropped=true;r.killer=s.getUUID();h.fryer=true;
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(FRYING);drop(a);
        bend(b,a);
        a.marks(a.marks()|BURN);
        var name=TheListTexts.NAMES[r.who];
        TheList.cue(s,String.format(TheListTexts.INTO_FRYER,name));
        say(a,r.who,TheListTexts.FRYER[r.who]);
        scream(l,a);
        var vat=TheListFryer.vat(b);
        l.playSound(null,vat.x,vat.y,vat.z,TheListRegistry.FRYER.get(),SoundSource.NEUTRAL,1.6F,.9F+l.getRandom().nextFloat()*.1F);
        l.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,vat.x,vat.y+.2,vat.z,8,.15,.1,.15,.02);
        l.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,vat.x,vat.y,vat.z,14,.2,.05,.2,.05);
        var jenna=TheListPeople.find(l,b,TheListPeople.JENNA);
        if(jenna!=null&&jenna.distanceTo(a)<=8){say(jenna,TheListPeople.JENNA,String.format(TheListTexts.JENNA_FRYER,name));TheListPeople.backAway(jenna,b);}
        heard(l,b,h,r,a,here);
    }
    /** Kept where they are bent over the fryer, facing into it. */
    static void bend(BlockPos b,LiteraryActor a){
        var at=TheListFryer.STAND;float yaw=TheListFryer.FACING.getOpposite().toYRot();
        a.moveTo(b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setDeltaMovement(Vec3.ZERO);
    }
    /** How long the hot oil has had one of them. */
    public static int fried(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.fried;}
    /**
     * In the fryer, each tick: kept bent over it, fighting its edges. The slasher gone from the town (or put down), they come up out of
     * it, burned, and run. While the oil is hot it has them: smoke and spitting, the oil's roar now and then; after UNDER ticks of it,
     * still. Put out, the count stops until it is hot again.
     */
    static void frying(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers,List<ServerPlayer> here){
        bend(b,a);
        var s=holder(r,slashers);
        if(s==null){loose(l,b,h,r,a,null,here);return;}
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+.8,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,.9F,1F);
        if(!TheListFryer.hot(l,b))return;
        r.fried++;
        var vat=TheListFryer.vat(b);
        if(r.fried%4==0){
            l.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,vat.x,vat.y+.15,vat.z,3,.12,.06,.12,.01);
            l.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,vat.x,vat.y,vat.z,4,.18,.02,.18,.03);
        }
        if(r.fried%20==0&&r.fried<TheListFryer.UNDER)l.playSound(null,vat.x,vat.y,vat.z,TheListRegistry.FRYER.get(),SoundSource.NEUTRAL,.9F,.95F+l.getRandom().nextFloat()*.15F);
        if(r.fried<TheListFryer.UNDER)return;
        // Still.
        stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
        a.gesture(FRIED);r.state=State.DEAD;
        TheListFryer.darken(l,b);
        var name=TheListTexts.NAMES[r.who];
        var killer=r.killer==null?null:l.getServer().getPlayerList().getPlayer(r.killer);
        if(killer!=null)TheList.cue(killer,String.format(TheListTexts.FRYER_STILL,name));
        for(var q:here)if(q!=killer&&!q.isSpectator()&&q.position().distanceTo(a.position())<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
        if(killer!=null)TheListBody.holding(killer,false);
        r.holder=null;a.holder(null);
        if(r.killer!=null)TheListBody.killed(l.getServer(),r.killer,r.who);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.715: the car door
    /**
     * A hand or a blow on the drive-in car's driver's door (TheListCar.hand). With one of the five in it: the slasher's slams it again,
     * anyone else's finds the head caught. With one dead in it: it will not shut. Otherwise the slasher's, with hold of one of the five
     * near it and the door standing open, forces them down at it with the head in. True when it was any of these; a shut door, or a
     * hand that is none of them, is left to the door (TheListCar), which the slasher opens with the other hand.
     */
    public static boolean carDoor(ServerPlayer p,BlockPos b){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator())return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(r.state!=State.CAR||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(p.getUUID().equals(r.holder))slam(l,b,h,r,a,p);else TheList.cue(p,String.format(TheListTexts.DOOR_HELD,TheListTexts.NAMES[r.who]));
            return true;
        }
        for(var r:h.runners.values())if(r.state==State.DEAD&&r.doored){TheList.cue(p,String.format(TheListTexts.CAR_STOPPED,TheListTexts.NAMES[r.who]));return true;}
        if(!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||r.state!=State.HELD||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(a.position().distanceTo(TheListCar.kneel(b))>TheListCar.REACH||!TheListCar.open(b))return false;
            doored(l,b,h,r,a,p,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Forced down at the car: on the knees beside its open driver's door, the head pushed in at it, at its back edge. Their words and a
     * scream; anyone else on the list near enough hears it. What they had in hand they let fall. The hold is still the slasher's.
     */
    static void doored(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,List<ServerPlayer> here){
        r.state=State.CAR;r.since=0;r.slams=0;r.slamAt=0;r.slamming=false;r.doored=true;r.dropped=true;r.killer=s.getUUID();
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(DOORED);drop(a);
        atDoor(b,a);
        TheList.cue(s,String.format(TheListTexts.INTO_DOOR,TheListTexts.NAMES[r.who]));
        say(a,r.who,TheListTexts.DOOR[r.who]);
        scream(l,a);
        heard(l,b,h,r,a,here);
    }
    /** Kept on the knees at the door, facing into the car. */
    static void atDoor(BlockPos b,LiteraryActor a){
        var at=TheListCar.kneel(b);float yaw=TheListCar.KNEEL_YAW;
        a.moveTo(at.x,at.y,at.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setDeltaMovement(Vec3.ZERO);
    }
    /** How many times the car door has come down on one of them. */
    public static int slams(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.slams;}
    /**
     * A slam, at least SLAM_GAP ticks after the last has landed and never while one is on its way: the door pulled back wide and
     * slammed down on the head (TheListCar.slam). What it does is where it lands (slammed).
     */
    static void slam(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s){
        if(r.slams>=3||r.slamming||r.slams>0&&r.since-r.slamAt<SLAM_GAP||TheListCar.swinging(b))return;
        r.slamming=true;r.killer=s.getUUID();
        s.swing(InteractionHand.MAIN_HAND,true);
        var killer=s.getUUID();
        TheListCar.slam(b,()->slammed(l,b,h,r,a,killer));
    }
    /**
     * Where the door lands. The first: it stops on the head with a crack of steel and bounces, they jerk, and blood drops onto the lot
     * under the door. The second: something gives against the frame, blood sprays down the car's side and onto the slasher, and the back
     * of the head is marked. The third: still, the scream stopped, anyone else in town near enough told from which way; slumped at the
     * car with the door shut on the head, a pool under it, the kill the slasher's.
     */
    static void slammed(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,UUID killer){
        r.slamming=false;
        if(HUNTS.get(b.asLong())!=h||r.state!=State.CAR||!a.isAlive())return;
        r.slams++;r.slamAt=r.since;
        // Under the door every time: the lot there is open, and each slam's blood thickens what the last left (stain).
        var head=TheListCar.head(b);Vec3 ground=TheListCar.under(b);var here=TheList.readers(l,b);
        var s=l.getServer().getPlayerList().getPlayer(killer);
        l.playSound(null,head.x,head.y,head.z,TheListRegistry.CAR_DOOR.get(),SoundSource.NEUTRAL,1.4F,.78F+l.getRandom().nextFloat()*.08F);
        var name=TheListTexts.NAMES[r.who];
        // The blood runs in against the car's side, under the door.
        var facing=Direction.SOUTH;
        switch(r.slams){
            case 1->{
                if(s!=null)TheList.cue(s,TheListTexts.SLAMS[0]);
                l.sendParticles(TheListRegistry.BLOOD.get(),head.x,head.y,head.z,6,.05,.05,.05,0);
                if(ground!=null)stain(l,b,ground,DROPS,facing);
            }
            case 2->{
                stop(l,a,TheListRegistry.STRUGGLE.get());
                l.playSound(null,head.x,head.y,head.z,TheListRegistry.CRACK.get(),SoundSource.NEUTRAL,1F,.9F+l.getRandom().nextFloat()*.1F);
                if(s!=null)TheList.cue(s,TheListTexts.SLAMS[1]);
                for(int i=0;i<18;i++){var rand=l.getRandom();
                    l.sendParticles(TheListRegistry.BLOOD.get(),head.x,head.y,head.z,0,(rand.nextDouble()-.5)*.3,-.05-rand.nextDouble()*.15,-rand.nextDouble()*.2,1);}
                if(ground!=null)stain(l,b,ground,SPATTER,facing);
                a.marks(a.marks()|CRUSH);
                if(s!=null)TheListBody.bleed(s);
            }
            default->{
                stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
                if(s!=null)TheList.cue(s,String.format(TheListTexts.SLAMS[2],name));
                for(var q:here)if(q!=s&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
                a.gesture(SLAMMED);atDoor(b,a);
                if(ground!=null)pool(l,b,ground,facing);
                if(s!=null)TheListBody.holding(s,false);
                r.holder=null;a.holder(null);
                r.state=State.DEAD;
                TheListBody.killed(l.getServer(),killer,r.who);
            }
        }
    }
    /**
     * At the car door, each tick: kept on the knees with the head in it. Before the second slam they fight it as they fight a grip,
     * longer while the slasher looks at them, and after the first it takes them twice as long; after the second they cannot. Bleeding
     * from the head after the first. The slasher gone from the town (or put down), they pull the head out and run, unless it is past that.
     */
    static void dooring(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers,List<ServerPlayer> here){
        atDoor(b,a);
        if(r.slams>=1&&r.since%4==0){var at=TheListCar.head(b);l.sendParticles(TheListRegistry.BLOOD.get(),at.x,at.y-.1,at.z,1,.04,.02,.04,0);}
        if(r.slams>=2||r.slamming)return;
        var s=holder(r,slashers);
        if(s==null){loose(l,b,h,r,a,null,here);return;}
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+.6,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,1F,1F);
        float off=Math.abs(Mth.wrapDegrees(yawTo(s.position(),a.position())-s.getYHeadRot()));
        r.strength+=off>LOOK_AWAY?2:1;
        if(r.strength>=STRENGTH[r.who]*(r.slams>=1?2:1))loose(l,b,h,r,a,s,here);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.716: the pumpjack
    /**
     * A hand or a blow on a pumpjack's gearbox (TheListJack.hand). With one of the five under its weight: the slasher's knocks the brake
     * off if it is on (otherwise it does nothing more); anyone else's is the brake (false). With one dead under it: it is stalled on them,
     * and the brake makes no difference to anyone. Otherwise the slasher's, with hold of one of the five near the gearbox, forces them
     * down beside it with the head in the weight's slot, if the weight is clear of it and has a way still to come; false, and the brake,
     * for anything else.
     */
    public static boolean jack(ServerPlayer p,BlockPos b,int i){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator())return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(r.jack!=i)continue;
            if(r.state==State.DEAD){TheList.cue(p,String.format(TheListTexts.JACK_STALLED,TheListTexts.NAMES[r.who]));return true;}
            if(r.state!=State.JACK)continue;
            if(!p.getUUID().equals(r.holder))return false;
            if(TheListJack.braked(l,b,i))TheListJack.brake(l,b,i,false,p);
            return true;
        }
        if(!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||r.state!=State.HELD||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(a.position().distanceTo(TheListJack.kneel(b,i))>TheListJack.REACH)return false;
            double angle=TheListJack.angle(l,b,i);boolean braked=TheListJack.braked(l,b,i);
            if(TheListJack.inSlot(angle)){TheList.cue(p,braked?TheListTexts.JACK_IN_SLOT:TheListTexts.JACK_WAIT);return true;}
            if(!braked&&!TheListJack.clearOf(angle)){TheList.cue(p,TheListTexts.JACK_WAIT);return true;}
            pumped(l,b,h,r,a,p,i,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Forced down beside the gearbox: on the knees, bent over, the head pushed down onto the ground in the near weight's slot. Their words
     * and a scream; anyone else on the list near enough hears it. What they had in hand they let fall. The hold is still the slasher's,
     * and it holds them down there while the weight comes round.
     */
    static void pumped(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,int i,List<ServerPlayer> here){
        r.state=State.JACK;r.since=0;r.jack=i;r.strength=0;r.dropped=true;r.killer=s.getUUID();r.crankAt=TheListJack.tick(l,b,i);
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(PUMPED);drop(a);
        bowed(b,i,a);
        TheList.cue(s,String.format(TheListTexts.INTO_JACK,TheListTexts.NAMES[r.who]));
        say(a,r.who,TheListTexts.JACK[r.who]);
        scream(l,a);
        heard(l,b,h,r,a,here);
    }
    /** Kept on the knees beside the gearbox, bowed toward it. */
    static void bowed(BlockPos b,int i,LiteraryActor a){
        var at=TheListJack.kneel(b,i);float yaw=TheListJack.kneelYaw(i);
        a.moveTo(at.x,at.y,at.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setDeltaMovement(Vec3.ZERO);
    }
    /**
     * Under the weight, each tick: kept down, fighting to lift the head, as they fight a grip but twice as hard to break. The slasher gone
     * from the town (or put down), they get up and run. The brake on, the weight stands where it is. Running, the well's own tick is
     * watched: the moment the near weight comes round to the head (TheListJack.JAM) it comes down on it, and the well stops there.
     */
    static void jacking(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers,List<ServerPlayer> here){
        bowed(b,r.jack,a);
        double now=TheListJack.tick(l,b,r.jack);
        if(now>r.crankAt&&now-r.crankAt<TheListMachines.STROKE){
            var at=TheListSite.jackMachine(r.jack);var m=b.offset(at);
            double was=TheListMachines.crankAt(r.crankAt,m),then=TheListMachines.crankAt(now,m);
            double to=TheListJack.ahead(was,TheListJack.JAM),went=TheListJack.ahead(was,then);
            if(to>0&&to<=went){r.crankAt=now;pinned(l,b,h,r,a,r.crankAt-(went-to)/(2*Math.PI)*TheListMachines.STROKE,here);return;}
        }
        r.crankAt=now;
        var s=holder(r,slashers);
        if(s==null){loose(l,b,h,r,a,null,here);return;}
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+.4,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,1F,1F);
        float off=Math.abs(Mth.wrapDegrees(yawTo(s.position(),a.position())-s.getYHeadRot()));
        r.strength+=off>LOOK_AWAY?2:1;
        if(r.strength>=STRENGTH[r.who]*2)loose(l,b,h,r,a,s,here);
    }
    /**
     * The weight down on the head: a crack, the well stopped there with the motor straining, blood out from under it. Still, the scream
     * stopped, anyone else in town near enough told from which way; the side of the head marked, the kill the slasher's, the hold let go.
     */
    static void pinned(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,double met,List<ServerPlayer> here){
        int i=r.jack;var head=TheListJack.head(b,i);var under=TheListJack.under(b,i);
        stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
        l.playSound(null,head.x,head.y,head.z,TheListRegistry.CRACK.get(),SoundSource.NEUTRAL,1.3F,.8F+l.getRandom().nextFloat()*.08F);
        TheListJack.stall(l,b,i,met);
        for(int k=0;k<14;k++){var rand=l.getRandom();
            l.sendParticles(TheListRegistry.BLOOD.get(),head.x,head.y,head.z,0,(rand.nextDouble()-.5)*.4,.02+rand.nextDouble()*.08,(rand.nextDouble()-.5)*.4,1);}
        var facing=TheListJack.runs(i);
        stain(l,b,under,SPATTER,facing);pool(l,b,under,facing);
        a.marks(a.marks()|FLAT);a.gesture(PINNED);bowed(b,i,a);
        r.state=State.DEAD;
        var name=TheListTexts.NAMES[r.who];
        var killer=r.killer==null?null:l.getServer().getPlayerList().getPlayer(r.killer);
        if(killer!=null)TheList.cue(killer,String.format(TheListTexts.JACK_STILL,name));
        for(var q:here)if(q!=killer&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
        if(killer!=null){TheListBody.holding(killer,false);TheListBody.bleed(killer);}
        r.holder=null;a.holder(null);
        if(r.killer!=null)TheListBody.killed(l.getServer(),r.killer,r.who);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.717: the stock tank
    /**
     * A hand or a blow on a straight cell of the stock tank's wall (TheListTank.hand). Where one of the five is held under: the slasher's
     * puts the head back under once it is up; anyone else's finds them held there. Where one is dead: still. Otherwise the slasher's, with
     * hold of one of the five near it, forces them over the rim with the head under the water; false (a look) for anything else.
     */
    public static boolean tank(ServerPlayer p,BlockPos b,BlockPos rel,Direction in){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator())return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(r.tank==null||!r.tank.equals(rel))continue;
            var name=TheListTexts.NAMES[r.who];
            if(r.state==State.DEAD){TheList.cue(p,String.format(TheListTexts.TANK_STILLWATER,name));return true;}
            if(r.state!=State.TANK)continue;
            if(!p.getUUID().equals(r.holder)){TheList.cue(p,String.format(TheListTexts.TANK_HELD,name));return true;}
            if(!r.under&&l.getEntity(r.id) instanceof LiteraryActor a)dunk(l,b,h,r,a,p);
            return true;
        }
        if(!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||r.state!=State.HELD||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(a.position().distanceTo(TheListTank.stand(b,rel,in))>TheListTank.REACH)return false;
            dunked(l,b,h,r,a,p,rel,in,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Forced over the rim: pressed against the tank's wall and bent over it, the head pushed under the water. Their words, a scream cut
     * into the water; anyone else on the list near enough hears it. What they had in hand they let fall. The hold is still the slasher's.
     */
    static void dunked(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,BlockPos wall,Direction in,List<ServerPlayer> here){
        r.state=State.TANK;r.since=0;r.tank=wall.immutable();r.tankIn=in;r.breath=TheListTank.BREATH;r.dunks=1;r.under=true;r.surfaced=0;
        r.strength=0;r.dropped=true;r.killer=s.getUUID();
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(DUNKED);drop(a);
        over(b,r,a);
        TheList.cue(s,String.format(TheListTexts.INTO_TANK,TheListTexts.NAMES[r.who]));
        say(a,r.who,TheListTexts.TANK[r.who]);
        scream(l,a);
        heard(l,b,h,r,a,here);
        splash(l,b,r,true);
    }
    /** Kept pressed to the wall, bent over the rim toward the water. */
    static void over(BlockPos b,Runner r,LiteraryActor a){
        var at=TheListTank.stand(b,r.tank,r.tankIn);float yaw=TheListTank.yaw(r.tankIn);
        a.moveTo(at.x,at.y,at.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.setDeltaMovement(Vec3.ZERO);
    }
    /** The water over the head thrown up: hard when it goes in, a churn while it is held. */
    static void splash(ServerLevel l,BlockPos b,Runner r,boolean hard){
        var at=TheListTank.surface(b,r.tank,r.tankIn);
        l.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,at.x,at.y,at.z,hard?24:6,.25,.05,.25,.1);
        l.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,at.x,at.y,at.z,hard?8:3,.2,.02,.2,.02);
        if(hard)l.playSound(null,at.x,at.y,at.z,TheListRegistry.DUNK.get(),SoundSource.NEUTRAL,1.2F,.95F+l.getRandom().nextFloat()*.1F);
    }
    /**
     * The slasher's hand on one come up gasping: the head back under. The third time it goes under, it does not come up again.
     */
    static void dunk(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s){
        r.dunks++;r.under=true;r.surfaced=0;r.strength=0;
        a.gesture(DUNKED);over(b,r,a);splash(l,b,r,true);
        TheList.cue(s,r.dunks>=TheListTank.LAST?TheListTexts.TANK_LAST:TheListTexts.TANK_AGAIN);
    }
    /**
     * At the tank, each tick: kept bent over the rim. Under, the breath goes and the water churns over the head; they fight as they fight
     * a grip, and the count full brings the head up, gasping, with some breath back (the last time under, they cannot). Up, the slasher
     * has a moment to put it back under; let alone, they tear loose and run. The breath gone, they drown.
     */
    static void tanking(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> slashers,List<ServerPlayer> here){
        over(b,r,a);
        var s=holder(r,slashers);
        if(s==null){loose(l,b,h,r,a,null,here);return;}
        if(!r.under){
            if(--r.surfaced<=0)loose(l,b,h,r,a,s,here);
            return;
        }
        r.breath--;
        if(r.since%5==0)splash(l,b,r,false);
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+.9,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,.8F,.9F);
        if(r.breath<=0){drowned(l,b,h,r,a,here);return;}
        if(r.dunks>=TheListTank.LAST)return;
        float off=Math.abs(Mth.wrapDegrees(yawTo(s.position(),a.position())-s.getYHeadRot()));
        r.strength+=off>LOOK_AWAY?2:1;
        if(r.strength>=STRENGTH[r.who])surfaced(l,b,r,a,s);
    }
    /** The head up out of the water, gasping, a third of a breath back; the slasher told to put it back. */
    static void surfaced(ServerLevel l,BlockPos b,Runner r,LiteraryActor a,ServerPlayer s){
        r.under=false;r.surfaced=TheListTank.SURFACE;r.strength=0;r.breath=Math.min(TheListTank.BREATH,r.breath+TheListTank.BREATH/3);
        a.gesture(SURFACED);
        var at=TheListTank.surface(b,r.tank,r.tankIn);
        l.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,at.x,at.y+.2,at.z,16,.3,.1,.3,.15);
        l.playSound(null,a.getX(),a.getY()+1.2,a.getZ(),TheListRegistry.GASP.get(),SoundSource.NEUTRAL,1.1F,1F);
        TheList.cue(s,TheListTexts.TANK_UP);
    }
    /**
     * Drowned: the churn stops and the water settles round the head; slumped over the rim with the arms hanging into the water. The
     * scream stopped, anyone else in town near enough told from which way; the face marked, the kill the slasher's, the hold let go.
     */
    static void drowned(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
        var at=TheListTank.surface(b,r.tank,r.tankIn);
        l.sendParticles(net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,at.x,at.y,at.z,12,.25,.02,.25,.01);
        a.marks(a.marks()|DROWNED);a.gesture(SUNK);over(b,r,a);
        r.state=State.DEAD;r.under=false;
        var name=TheListTexts.NAMES[r.who];
        var killer=r.killer==null?null:l.getServer().getPlayerList().getPlayer(r.killer);
        if(killer!=null)TheList.cue(killer,String.format(TheListTexts.TANK_STILL,name));
        for(var q:here)if(q!=killer&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
        if(killer!=null)TheListBody.holding(killer,false);
        r.holder=null;a.holder(null);
        if(r.killer!=null)TheListBody.killed(l.getServer(),r.killer,r.who);
    }
    /** For tests: one at the tank, whether the head is under, how many times it has gone under, and the breath left (set). */
    public static boolean under(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r!=null&&r.under;}
    public static int dunks(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.dunks;}
    public static void breath(BlockPos b,int who,int left){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);if(r!=null)r.breath=left;}

    // ------------------------------------------------------------------------------------------------ 0.4.718: the gin's auger
    /** Which night this town's hunt is on, or nought with none. */
    public static int night(BlockPos b){var h=HUNTS.get(b.asLong());return h==null?0:h.night;}
    /**
     * A hand or a blow on a cell of the gin's auger run (TheListGin.hand). One already in it there: a hand finds them held down in the
     * screw, or, dead, still in it. Otherwise the slasher with hold of one of the five near enough puts them in, feet first, but only on
     * the fourth night, when the gin runs; before then the hand is told the screw takes nobody. False for anyone else's hand: a look.
     */
    public static boolean auger(ServerPlayer p,BlockPos b,BlockPos rel){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator())return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(r.augerAt==null||!r.augerAt.equals(rel))continue;
            var name=TheListTexts.NAMES[r.who];
            if(r.state==State.DEAD){TheList.cue(p,String.format(TheListTexts.AUGER_STILL_BODY,name));return true;}
            if(r.state!=State.AUGER)continue;
            TheList.cue(p,String.format(TheListTexts.AUGER_HELD,name));return true;
        }
        if(!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||r.state!=State.HELD||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(a.position().distanceTo(TheListGin.in(b,rel,0))>TheListGin.REACH)return false;
            if(h.night<TheListGin.NIGHT){TheList.cue(p,TheListTexts.GIN_NOT_TONIGHT);return true;}
            augered(l,b,h,r,a,p,rel,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Forced down into the auger's trough, feet first, where the screw takes the legs. The slasher lets go: it is the screw that holds
     * them now, and the slasher's hands and walk are free. Their words, a scream; anyone else on the list near enough hears it. What they
     * had in hand they let fall. The kill, when it comes, is still the slasher's.
     */
    static void augered(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,BlockPos cell,List<ServerPlayer> here){
        r.state=State.AUGER;r.since=0;r.augerAt=cell.immutable();r.drawn=0;r.strength=0;r.dropped=true;r.killer=s.getUUID();
        r.holder=null;a.holder(null);TheListBody.holding(s,false);
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(AUGERED);drop(a);
        sink(b,r,a);
        TheList.cue(s,String.format(TheListTexts.INTO_AUGER,TheListTexts.NAMES[r.who]));
        say(a,r.who,TheListTexts.AUGER[r.who]);
        scream(l,a);
        heard(l,b,h,r,a,here);
        l.playSound(null,a.getX(),a.getY()+.5,a.getZ(),TheListRegistry.AUGER.get(),SoundSource.NEUTRAL,1.3F,.7F);
    }
    /** Kept in the middle of the trough, as far down into the screw as it has drawn them, facing the feeder's end. */
    static void sink(BlockPos b,Runner r,LiteraryActor a){
        var at=TheListGin.in(b,r.augerAt,r.drawn/(double)TheListGin.DRAW);
        a.moveTo(at.x,at.y,at.z,0,0);a.setYBodyRot(0);a.setYHeadRot(0);a.setDeltaMovement(Vec3.ZERO);
    }
    /**
     * In the auger, each tick. While the line runs the screw draws them down; blood comes up off it and runs out onto the floor beside
     * the trough. While it runs they cannot get out of it. With the line stopped it no longer draws, and they fight their way out as they
     * fight a grip, at twice its pace. Drawn all the way down, they die there; the screw jams on them and the line stops for good.
     */
    static void augering(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        boolean on=TheListGin.running(b);
        if(on)r.drawn++;
        sink(b,r,a);
        if(on&&r.since%5==0){
            var at=TheListGin.in(b,r.augerAt,0);
            l.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.45F,.02F,.03F),1.4F),at.x,at.y+.5,at.z,6,.25,.1,.25,.02);
            if(r.drawn>=TheListGin.DRAW/4&&r.since%30==0)pool(l,b,TheListGin.beside(b,r.augerAt),Direction.EAST);
        }
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+1.2,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,1F,1F);
        if(on&&r.since%30==15)scream(l,a);
        if(r.drawn>=TheListGin.DRAW){mangled(l,b,h,r,a,here);return;}
        if(on)return;
        r.strength+=2;
        if(r.strength>=STRENGTH[r.who])loose(l,b,h,r,a,null,here);
    }
    /**
     * Drawn down to the waist: the screw jams on them and the line stops, the motor labouring. Slumped forward over the trough with the
     * arms hanging over its sides. The scream stopped, anyone else in town near enough told from which way; the kill the slasher's.
     */
    static void mangled(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
        TheListGin.jam(b);
        l.playSound(null,a.getX(),a.getY()+.8,a.getZ(),TheListRegistry.STRAIN.get(),SoundSource.BLOCKS,1.3F,.6F);
        pool(l,b,TheListGin.beside(b,r.augerAt),Direction.EAST);
        a.gesture(MANGLED);sink(b,r,a);
        r.state=State.DEAD;
        var name=TheListTexts.NAMES[r.who];
        var killer=r.killer==null?null:l.getServer().getPlayerList().getPlayer(r.killer);
        if(killer!=null)TheList.cue(killer,String.format(TheListTexts.AUGER_STILL,name));
        for(var q:here)if(q!=killer&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
        if(r.killer!=null)TheListBody.killed(l.getServer(),r.killer,r.who);
    }
    /** For tests: how long the screw has drawn one, and setting it. */
    public static int drawn(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.drawn;}
    public static void night(BlockPos b,int night){var h=HUNTS.get(b.asLong());if(h!=null)h.night=night;}
    public static int strengthIn(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.strength;}
    public static void drawn(BlockPos b,int who,int ticks){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);if(r!=null)r.drawn=ticks;}

    // ------------------------------------------------------------------------------------------------ 0.4.719: the gin's press
    /**
     * A hand or a blow on the gin's press (TheListGin.hand), or on one in it. One in its chamber with the ram coming: the hand that put
     * them there finds them held; anyone else's hauls the release and lets them out. One dead there: still. Otherwise the slasher with
     * hold of one of the five near enough shoves them in, on the fourth night only. False for anyone else's hand: a look.
     */
    public static boolean press(ServerPlayer p,BlockPos b){
        var h=HUNTS.get(b.asLong());if(h==null||p.isSpectator())return false;
        var l=p.serverLevel();
        for(var r:h.runners.values()){
            if(!r.pressed)continue;
            var name=TheListTexts.NAMES[r.who];
            if(r.state==State.DEAD){TheList.cue(p,String.format(TheListTexts.PRESS_STILL_BODY,name));return true;}
            if(r.state!=State.PRESS)continue;
            if(p.getUUID().equals(r.killer)){TheList.cue(p,String.format(TheListTexts.PRESS_HELD,name));return true;}
            if(l.getEntity(r.id) instanceof LiteraryActor a)released(l,b,h,r,a,p,TheList.readers(l,b));
            return true;
        }
        if(!TheListBody.LIVE.containsKey(p.getUUID())||TheListBody.down(p)>0)return false;
        for(var r:h.runners.values()){
            if(!p.getUUID().equals(r.holder)||r.state!=State.HELD||!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
            if(a.position().distanceTo(TheListGin.chamber(b))>TheListGin.REACH)return false;
            if(h.night<TheListGin.NIGHT){TheList.cue(p,TheListTexts.PRESS_NOT_TONIGHT);return true;}
            pressedIn(l,b,h,r,a,p,TheList.readers(l,b));
            return true;
        }
        return false;
    }
    /**
     * Shoved into the press's chamber, the latch dropped and the ram started down. The slasher lets go: the chamber holds them now.
     * Their words, a scream; anyone else on the list near enough hears it. What they had in hand they let fall.
     */
    static void pressedIn(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer s,List<ServerPlayer> here){
        r.state=State.PRESS;r.since=0;r.pressed=true;r.ram=0;r.strength=0;r.dropped=true;r.killer=s.getUUID();
        r.holder=null;a.holder(null);TheListBody.holding(s,false);
        BorrascaCast.stop(a);a.setPose(Pose.STANDING);a.setNoGravity(true);a.gesture(PRESSED);drop(a);
        TheListGin.open(l,b);chambered(b,a);
        TheList.cue(s,String.format(TheListTexts.INTO_PRESS,TheListTexts.NAMES[r.who]));
        say(a,r.who,TheListTexts.PRESS[r.who]);
        scream(l,a);
        heard(l,b,h,r,a,here);
        l.playSound(null,a.getX(),a.getY()+1.5,a.getZ(),TheListRegistry.STRAIN.get(),SoundSource.BLOCKS,1.3F,.55F);
    }
    /** Kept in the chamber, facing out of it toward the press door. */
    static void chambered(BlockPos b,LiteraryActor a){
        var at=TheListGin.chamber(b);a.moveTo(at.x,at.y,at.z,180,0);a.setYBodyRot(180);a.setYHeadRot(180);a.setDeltaMovement(Vec3.ZERO);
    }
    /**
     * In the press, each tick: the ram comes down on them over RAM ticks, the motor labouring; halfway they are bent double under it.
     * They cannot get out by themselves: the chamber is latched. All the way down, they are crushed.
     */
    static void pressing(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        chambered(b,a);
        r.ram++;
        if(r.ram==TheListGin.RAM/2)a.gesture(BENT);
        if(r.since%20==0)l.playSound(null,a.getX(),a.getY()+1.5,a.getZ(),TheListRegistry.STRAIN.get(),SoundSource.BLOCKS,1.1F,.5F+.3F*r.ram/TheListGin.RAM);
        if(r.since%STRUGGLE_EVERY==0)l.playSound(null,a.getX(),a.getY()+.8,a.getZ(),TheListRegistry.STRUGGLE.get(),SoundSource.NEUTRAL,1F,1F);
        if(r.since%30==15)scream(l,a);
        if(r.ram>=TheListGin.RAM)crushed(l,b,h,r,a,here);
    }
    /**
     * The release hauled by a hand that is not the slasher's: the ram stops and goes back up, the latch lifts, and they come out of the
     * chamber and run. The press is shut again behind them.
     */
    static void released(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,ServerPlayer by,List<ServerPlayer> here){
        var out=TheListGin.front(b);a.moveTo(out.x,out.y,out.z,180,0);
        TheListGin.shut(l,b);
        l.playSound(null,out.x,out.y+1,out.z,net.minecraft.sounds.SoundEvents.PISTON_CONTRACT,SoundSource.BLOCKS,1F,.6F);
        TheList.cue(by,String.format(TheListTexts.PRESS_RELEASE,TheListTexts.NAMES[r.who]));
        loose(l,b,h,r,a,null,here);
    }
    /**
     * Crushed: the ram all the way down, bones going; folded in the chamber, blood out of its foot onto the floor. The slasher is told;
     * anyone else in town near enough is told from which way the scream stopped; the kill the slasher's.
     */
    static void crushed(ServerLevel l,BlockPos b,Hunt h,Runner r,LiteraryActor a,List<ServerPlayer> here){
        stop(l,a,TheListRegistry.SCREAM.get());stop(l,a,TheListRegistry.STRUGGLE.get());
        l.playSound(null,a.getX(),a.getY()+.6,a.getZ(),TheListRegistry.CRACK.get(),SoundSource.NEUTRAL,1.4F,.7F);
        a.marks(a.marks()|CRUSH);a.gesture(FOLDED);chambered(b,a);
        pool(l,b,TheListGin.front(b),Direction.NORTH);
        r.state=State.DEAD;
        var name=TheListTexts.NAMES[r.who];
        var killer=r.killer==null?null:l.getServer().getPlayerList().getPlayer(r.killer);
        if(killer!=null)TheList.cue(killer,String.format(TheListTexts.PRESS_STILL,name));
        for(var q:here)if(q!=killer&&!q.isSpectator()&&q.distanceTo(a)<=SCREAM_REACH)TheList.cue(q,String.format(TheListTexts.STOPS_SHORT,compass(q.position(),a.position())));
        if(r.killer!=null)TheListBody.killed(l.getServer(),r.killer,r.who);
    }
    /** For tests: how far the ram has come, and setting it. */
    public static int ram(BlockPos b,int who){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r==null?0:r.ram;}
    public static void ram(BlockPos b,int who,int ticks){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);if(r!=null)r.ram=ticks;}

    // ------------------------------------------------------------------------------------------------ 0.4.710: blood where it lands
    /** The way from one on the hook toward the slasher (the way they face, with nobody holding them). */
    static Direction toward(LiteraryActor a,@Nullable ServerPlayer s){
        if(s==null)return a.getDirection();var d=s.position().subtract(a.position());
        return d.horizontalDistanceSqr()<1e-4?a.getDirection():Direction.getNearest(d.x,0,d.z);
    }
    /**
     * Blood left where it lands: in the open cell at a point, over something with a solid top, or over grass, which it replaces until it
     * is taken up. A heavier stain is never made lighter. Each is kept in the town's record with what was there, so that it is taken up
     * when the hunt ends, or at the next look after a restart.
     */
    /** Open ground blood can land on and has not yet: an empty or plant-grown cell over a solid top. */
    static boolean fresh(ServerLevel l,Vec3 at){
        var cell=BlockPos.containing(at.x,at.y+.1,at.z);
        if(!l.isLoaded(cell))return false;
        var st=l.getBlockState(cell);
        if(TheListBlock.is(st,TheListBlock.Kind.BLOOD)||!(st.isAir()||st.canBeReplaced()&&st.getFluidState().isEmpty()))return false;
        var under=cell.below();return l.getBlockState(under).isFaceSturdy(l,under,Direction.UP);
    }
    static void stain(ServerLevel l,BlockPos b,Vec3 at,int stage,Direction facing){
        var cell=BlockPos.containing(at.x,at.y+.1,at.z);
        if(!l.isLoaded(cell))return;
        var st=l.getBlockState(cell);
        if(TheListBlock.is(st,TheListBlock.Kind.BLOOD)){
            if(st.getValue(TheListBlock.STAGE)<stage)l.setBlock(cell,TheListBlock.of(TheListBlock.Kind.BLOOD,facing,stage),3);
            return;
        }
        if(!(st.isAir()||st.canBeReplaced()&&st.getFluidState().isEmpty()))return;
        var under=cell.below();if(!l.getBlockState(under).isFaceSturdy(l,under,Direction.UP))return;
        var d=LabyrinthData.get(l.getServer());int copy=TheListNight.copy(l.getServer(),b);var w=LiteraryVignettes.shared(d,TheList.PLACE,copy);
        var list=w.getList("Stains",net.minecraft.nbt.Tag.TAG_COMPOUND);
        var e=new net.minecraft.nbt.CompoundTag();e.putLong("At",cell.subtract(b).asLong());e.put("Was",net.minecraft.nbt.NbtUtils.writeBlockState(st));
        list.add(e);w.put("Stains",list);LiteraryVignettes.shared(d,TheList.PLACE,copy,w);
        l.setBlock(cell,TheListBlock.of(TheListBlock.Kind.BLOOD,facing,stage),3);
    }
    /**
     * The pool under the dead, which spreads out on both sides of them: the cell under the chest, and the cells beside it wherever the
     * chest lies within a third of a block of their edge, each turned a quarter further so that no two lie the same way.
     */
    static void pool(ServerLevel l,BlockPos b,Vec3 at,Direction facing){
        double fx=at.x-Math.floor(at.x),fz=at.z-Math.floor(at.z);
        int sx=fx<.33?-1:fx>.67?1:0,sz=fz<.33?-1:fz>.67?1:0;
        stain(l,b,at,POOL,facing);
        if(sx!=0)stain(l,b,at.add(sx*.5,0,0),POOL,facing.getClockWise());
        if(sz!=0)stain(l,b,at.add(0,0,sz*.5),POOL,facing.getCounterClockWise());
        if(sx!=0&&sz!=0)stain(l,b,at.add(sx*.5,0,sz*.5),SMEAR,facing.getOpposite());
    }
    /** Every stain the town's record holds taken up, as far as its ground is loaded, and what was there put back. */
    static void tidy(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());int copy=TheListNight.copy(l.getServer(),b);var w=LiteraryVignettes.shared(d,TheList.PLACE,copy);
        if(!w.contains("Stains"))return;
        var list=w.getList("Stains",net.minecraft.nbt.Tag.TAG_COMPOUND);var left=new net.minecraft.nbt.ListTag();
        for(int i=0;i<list.size();i++){
            var e=list.getCompound(i);var at=b.offset(BlockPos.of(e.getLong("At")));
            if(!l.isLoaded(at)){left.add(e);continue;}
            if(TheListBlock.is(l.getBlockState(at),TheListBlock.Kind.BLOOD))
                l.setBlock(at,net.minecraft.nbt.NbtUtils.readBlockState(l.holderLookup(net.minecraft.core.registries.Registries.BLOCK),e.getCompound("Was")),3);
        }
        if(left.isEmpty())w.remove("Stains");else w.put("Stains",left);
        LiteraryVignettes.shared(d,TheList.PLACE,copy,w);
    }
    /** How many stains the town holds now. */
    public static int stains(ServerLevel l,BlockPos b){
        return LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),TheList.PLACE,TheListNight.copy(l.getServer(),b)).getList("Stains",net.minecraft.nbt.Tag.TAG_COMPOUND).size();
    }
    /** Stains left from a hunt the server did not see end, taken up while no hunt is on (TheListPeople.ensure). */
    static void leftover(ServerLevel l,BlockPos b){if(!on(b))tidy(l,b);}

    // ------------------------------------------------------------------------------------------------ 0.4.710: the dead found
    /**
     * The dead, come across. Any of the five still on their feet who sees one of the others lying there says so, once for each; one
     * still at their place screams and runs as well. A reader who is not the slasher, near enough and looking that way with nothing
     * between, is told who it is, once a hunt for each, and their record keeps that they found them. Spectators find nothing.
     */
    static void bodies(ServerLevel l,BlockPos b,Hunt h,List<ServerPlayer> here){
        for(var d:h.runners.values()){
            if(!gone(d)||!(l.getEntity(d.id) instanceof LiteraryActor body)||!body.isAlive())continue;
            // 0.4.711: one hung over a row of the stands is seen by the head under the next plank as well as the body on it.
            boolean hung=body.gesture()==HUNG&&d.kneel!=null,spitted=body.gesture()==SPITTED,fried=body.gesture()==FRIED,slammed=body.gesture()==SLAMMED,pinned=body.gesture()==PINNED,sunk=body.gesture()==SUNK,mangled=body.gesture()==MANGLED,folded=body.gesture()==FOLDED;
            // 0.4.714: one dead in the fryer is seen by the back, bent over it; 0.4.715: one at the car door, by the back, slumped at it.
            // 0.4.716: one dead at a pumpjack, by the back, bowed over beside the gearbox.
            // 0.4.719: one crushed in the press is seen at the body itself: its eyes are up inside the press head.
            var at=hung?d.kneel.add(0,.4,0):body.position().add(0,spitted?.8:fried?1.0:slammed?.7:pinned?.45:sunk?.9:mangled?1.15:folded?.5:.3,0);var head=hung?d.kneel.add(HEAD):folded?at:null;var name=TheListTexts.NAMES[d.who];
            for(var r:h.runners.values()){
                if(r==d||(r.saw&1<<d.who)!=0||r.state!=State.CALM&&r.state!=State.RUN&&r.state!=State.HIDE&&r.state!=State.COWER)continue;
                if(!(l.getEntity(r.id) instanceof LiteraryActor a)||!a.isAlive())continue;
                if(!sight(l,a,a.getYHeadRot(),body,at,head,FINDS_BODY,75))continue;
                r.saw|=1<<d.who;
                if(r.state==State.CALM)run(l,b,h,r,a,here,true);
                say(a,r.who,String.format(hung?TheListTexts.FOUND_HUNG_BODY[r.who]:spitted?TheListTexts.FOUND_LINE_BODY[r.who]:fried?TheListTexts.FOUND_FRYER_BODY[r.who]
                    :slammed?TheListTexts.FOUND_CAR_BODY[r.who]:pinned?TheListTexts.FOUND_JACK_BODY[r.who]:sunk?TheListTexts.FOUND_TANK_BODY[r.who]:mangled?TheListTexts.FOUND_AUGER_BODY[r.who]:folded?TheListTexts.FOUND_PRESS_BODY[r.who]:TheListTexts.FOUND_BODY[r.who],name,name));
            }
            for(var q:here){
                if(q.isSpectator()||!q.isAlive()||TheListBody.live(q))continue;
                int seen=h.found.getOrDefault(q.getUUID(),0);if((seen&1<<d.who)!=0)continue;
                if(!sight(l,q,q.getYHeadRot(),body,at,head,READER_FINDS,READER_LOOK))continue;
                h.found.put(q.getUUID(),seen|1<<d.who);
                TheList.cue(q,String.format(hung?TheListTexts.FOUND_HUNG:spitted?TheListTexts.FOUND_LINE:fried?TheListTexts.FOUND_FRYER:slammed?TheListTexts.FOUND_CAR
                    :pinned?TheListTexts.FOUND_JACK:sunk?TheListTexts.FOUND_TANK:mangled?TheListTexts.FOUND_AUGER:folded?TheListTexts.FOUND_PRESS:TheListTexts.FOUND_DEAD,name));
                TheListBody.found(q,d.who);
            }
        }
    }
    /** Whether one looking from where they are sees a point (or a second, the head of one hung over the stands): near enough, in front
     * of them, nothing solid between. */
    static boolean sight(ServerLevel l,net.minecraft.world.entity.LivingEntity e,float yaw,LiteraryActor body,Vec3 at,@Nullable Vec3 also,double reach,float within){
        var eye=e.getEyePosition();
        if(also==null)return eye.distanceToSqr(at)<=reach*reach&&TheListBody.facing(yaw,eye,at,within)&&e.hasLineOfSight(body);
        for(var to:new Vec3[]{at,also}){
            if(eye.distanceToSqr(to)>reach*reach||!TheListBody.facing(yaw,eye,to,within))continue;
            if(l.clip(new net.minecraft.world.level.ClipContext(eye,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,e)).getType()==net.minecraft.world.phys.HitResult.Type.MISS)return true;
        }
        return false;
    }
    /** For tests: the dead looked for now, rather than on the town's next fifth tick. */
    public static void look(ServerLevel l,BlockPos b){var h=HUNTS.get(b.asLong());if(h!=null)bodies(l,b,h,TheList.readers(l,b));}
    /** Which of the dead a reader has come across this hunt, a bit for each. */
    public static int found(BlockPos b,ServerPlayer p){var h=HUNTS.get(b.asLong());return h==null?0:h.found.getOrDefault(p.getUUID(),0);}
    /** Whether one of the five has come across another dead. */
    public static boolean saw(BlockPos b,int who,int dead){var h=HUNTS.get(b.asLong());var r=h==null?null:h.runners.get(who);return r!=null&&(r.saw&1<<dead)!=0;}

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
    public static void clearAll(){HUNTS.clear();TheListCar.clearAll();TheListJack.clearAll();TheListGin.clearAll();}
    /** A word for the operator: each one's state, way and step. */
    public static String status(BlockPos b){
        var h=HUNTS.get(b.asLong());if(h==null)return "no hunt";
        var out=new StringJoiner("; ");
        for(var r:h.runners.values())out.add(TheListTexts.NAMES[r.who]+" "+r.state.name().toLowerCase(Locale.ROOT)+(r.state==State.RUN?" (way "+r.way+", step "+r.step+")":"")+(r.fought?", fought back":""));
        return out+"; bolted "+h.bolted.size();
    }
    static float yaw(Direction d){return Mth.wrapDegrees(d.toYRot());}
}
