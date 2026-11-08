package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import io.github.knaitoe.theoldesthouse.network.CrossingPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.351, The Last Crossing (docs/LAST_CROSSING_0_4_351.md), from the owner's design "The Last Crossing": "The crew arrives at a flood
 * station where a keeper is trying to get the last cable ferry across. On the first day she takes everyone's name."
 *
 * <p>This first slice is the road, the station and its five places, Vera and her roll, the crew's reservation, the day's clock, Day One's
 * jobs, the speaking pipes and the handprint plate. A crew signs the roll on the porch; Vera calls each name aloud, waits for an answer,
 * and calls Ellis after the last of them; then she shows them the plate and puts the kettle on, and the day runs on its own clock while
 * anyone is at the station. The jobs are stations anyone can walk up to, trade or help with: the intake (the bleed screw below, the
 * matching valve when the gauge reader calls it, the brake held open above until the pump takes the load), the cable house (a roller to
 * seat, the hatch, the jam at the fairlead to report), the landing (the loose line brought in, the wheel and the pin checked and the two
 * knocks rehearsed through the speaking pipe), and with a fifth and sixth reader the lamp carried between the intake and the gauge room,
 * the spillway walked with the lookout, the bell tried and the line staged. Then Vera sends them round the whole route once, and at dusk
 * the station holds: the night is the next slice. Each reader's record keeps what they did, learned, saw and were handed, for the
 * accounts the last slice writes. Nobody here is hurt; the flood's current puts a swimmer back on the landing. Not dealt until the
 * crossing can be finished.
 *
 * <p>0.4.352, Night One (docs/LAST_CROSSING_0_4_352.md): from dusk the night runs on LastCrossingNight: the surge, the authored first
 * infection and its rescue, the things from the water, the cable house's bar, the first rise, the carrier's private beat; at dawn the
 * station holds in the morning light until Day Two is built.
 *
 * <p>0.4.353, Day Two (docs/LAST_CROSSING_0_4_353.md), on LastCrossingDay: the slate, the cable tools, the winch, the branches and the
 * call by day. 0.4.354, Night Two (docs/LAST_CROSSING_0_4_354.md), on LastCrossingNightTwo: the turn at the second dusk, the roll from the
 * cable house's doorway, the pull, the things up through the branches, the turned reader's water, claws, call and broken speech, the
 * choking grate and the plate, the grapple and the second slot; at dawn the station holds for Day Three. 0.4.355, Day Three
 * (docs/LAST_CROSSING_0_4_355.md), on LastCrossingDayThree: the depth, the roll at the ramp and the question, the launch made ready, the
 * feed traced to the lower gallery and the gallery made ready, the master bypass and the Other Breath's offer; at the third dusk the
 * station holds for Night Three. 0.4.356, Night Three (docs/LAST_CROSSING_0_4_356.md), on LastCrossingNightThree: the last turns, the
 * roll in the first day's order, the final rise, the launch and the ferry's crossing, the gallery and the source, the master bypass and the
 * Other Breath, the three endings and the morning on the far bank (END).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastCrossing {
    private LastCrossing(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_CROSSING;
    public static final String TALK="last_crossing";
    /** The station's day: the crew gathering before the roll; Vera calling it; Day One; Night One, from dusk; dawn, held until Day Two is
     * built. */
    public static final int GATHERING=0,ROLL=1,DAY=2,DUSK=3,DAWN=4,END=5;
    /** Ticks: a day of seven minutes and a night of four; how long Vera waits for the rest of the crew after the first name; how long
     * she waits for an answer, and after saying a name again, and after Ellis's; how long a turned valve holds before it creeps back;
     * how long the brake must be held open; the gauge's dip after priming, how often and for how long; how long a hand on the sluice
     * wheel holds it; how close together two knocks must come; how long the pin stays seated in practice; the current's hold on a
     * swimmer; an empty station's reset. */
    public static final int DAY_TICKS=8400,NIGHT_TICKS=4800,GATHER_WAIT=600,ANSWER=100,AGAIN=60,ELLIS_WAIT=120,VALVE_DRIFT=600,BRAKE_HOLD=100,
        DIP_EVERY=600,DIP_FOR=40,WHEEL_HOLD=100,KNOCK_GAP=40,PIN_RESET=100,CURRENT=60,EMPTY_RESET=200;
    /** A crew is four to six readers; the sheet takes six names. */
    public static final int MIN_CREW=4,MAX_CREW=6;
    /** Vera's skin, on the stranger phase after the Tall Grass driver's. */
    public static final int VERA=116;
    public static boolean cast(int phase){return phase==VERA;}
    public static String skin(int phase){return "crossing_vera";}
    static final String ITEM_TAG="Crossing";

    static final class Run {
        final BlockPos base;final long id;long tick;int empty,phase=GATHERING;boolean fresh=true;
        UUID vera;
        /** The roll: names in signing order, who answered, who is being called and since when. */
        final LinkedHashMap<UUID,String> crew=new LinkedHashMap<>();final Set<UUID> answered=new HashSet<>();
        int calling=-1,callTicks;boolean calledTwice;long firstSigned=-1,answerAt=-1;int day;
        /** The intake: the bleed screw open, who opened it (the intake worker), the needle's band, the valve turned and when, the brake
         * held and by whom, primed and when. */
        boolean bleedOpen;UUID intakeBy;int needle=-1,turned;long turnedAt=-1;UUID turnedBy;UUID brakeBy;int brakeHeld;boolean primed;long primedAt=-1;int called;
        /** The cable house and the landing. */
        boolean rollerSeated,jamReported,lineIn,lineReturned,lineStaged;UUID wheelBy;long wheelUntil;boolean pinSeated;long pinAt=-1,twoKnocksB=-1;boolean rehearsed,rehearsePrompted;long rehearseAt=-1;
        /** The speaking pipes' knocks: per mouth, the last knock and the run of knocks. */
        final Map<String,long[]> knocks=new HashMap<>();
        /** The plate: where it stands (relative), or who carries it; the fog on it until when, and what showed. */
        @Nullable BlockPos plateAt=LastCrossingSite.PLATE_HOME;@Nullable UUID plateBy;long fogUntil=-1;
        boolean lampHome=true,wrenchHome=true;UUID wrenchDisplay;long tapAt=-1;
        boolean routeCalled,dusk;int demo;long demoAt;boolean kettle;
        final List<Vec3> route=new ArrayList<>();int leg,legTicks;
        final Map<UUID,Long> greeted=new HashMap<>();final Map<UUID,Integer> current=new HashMap<>();
        /** Night One, and those of the crew who have died at the station this crossing. */
        LastCrossingNight.Night night=new LastCrossingNight.Night();final Set<UUID> dead=new HashSet<>();
        /** 0.4.353: which day it is (Day One until the morning after Night One), and Day Two's state. */
        int dayNo=1;LastCrossingDay.Day dayTwo=new LastCrossingDay.Day();
        /** 0.4.354: Night Two's state (and what it leaves for the morning: tears, prints, the second slot). */
        LastCrossingNightTwo.Night night2=new LastCrossingNightTwo.Night();
        /** 0.4.355: Day Three's state. */
        LastCrossingDayThree.Day dayThree=new LastCrossingDayThree.Day();
        /** 0.4.356: Night Three's own state (its turns, roll, things and grate run on night2). */
        LastCrossingNightThree.Night night3=new LastCrossingNightThree.Night();
        Run(BlockPos b,long id){base=b;this.id=id;}
    }
    private static final Map<Long,Run> RUNS=new HashMap<>();
    public static void clearAll(){RUNS.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Run run(BlockPos b){return RUNS.get(b.asLong());}
    static Collection<Run> runs(){return RUNS.values();}
    public static int phase(BlockPos b){var r=run(b);return r==null?-1:r.phase;}
    public static int day(BlockPos b){var r=run(b);return r==null?-1:r.day;}
    public static int needle(BlockPos b){var r=run(b);return r==null?-1:r.needle;}
    public static List<String> crew(BlockPos b){var r=run(b);return r==null?List.of():List.copyOf(r.crew.values());}
    public static boolean flag(BlockPos b,String name){var r=run(b);if(r==null)return false;return switch(name){case "bleed"->r.bleedOpen;case "primed"->r.primed;case "roller"->r.rollerSeated;case "reported"->r.jamReported;
        case "line"->r.lineIn;case "returned"->r.lineReturned;case "staged"->r.lineStaged;case "pin"->r.pinSeated;case "rehearsed"->r.rehearsed;case "route"->r.routeCalled;case "dusk"->r.dusk;case "kettle"->r.kettle;
        case "lamp"->r.lampHome;case "wrench"->r.wrenchHome;case "plate"->r.plateBy!=null;
        case "surged"->r.night.surged;case "grip"->r.night.gripped!=null;case "freed"->r.night.freed;case "flooded"->r.night.lowerFlooded;case "isolated"->r.night.isolated;
        case "barred"->r.night.barred;case "rose"->r.night.rose;case "held"->r.night.held;case "loose"->r.night.loose;case "fouled"->r.night.fouled;case "dawn"->r.night.dawn;
        case "tools"->r.dayTwo.toolsTaken;case "scraped"->r.dayTwo.scraped;case "catch"->r.dayTwo.catchSet;case "lined"->r.dayTwo.lined;case "winchOpen"->r.dayTwo.winchOpen;
        case "rampOpen"->r.dayTwo.rampOpen;case "briefed"->r.dayTwo.voiced;
        case "rolled"->r.night2.rollDone;case "pulled"->r.night2.pull==LastCrossingNightTwo.PULLED;case "pullFailed"->r.night2.pull==LastCrossingNightTwo.FAILED;
        case "jam"->r.night2.jamCleared;case "pinned"->r.night2.pinned;case "gateHeld"->r.night2.gateBy!=null;case "dawn2"->r.night2.dawn;
        case "pinStaged"->r.dayThree.pinStaged;case "wheelTested"->r.dayThree.wheelTested;case "launchReady"->r.dayThree.launchReady;case "doorOpen"->r.dayThree.doorOpen;
        case "barStaged"->r.dayThree.barStaged;case "pathCleared"->r.dayThree.pathCleared;case "galleryReady"->r.dayThree.galleryReady;case "briefed3"->r.dayThree.briefed;
        case "risen"->r.night3.risen;case "pinned3"->r.night3.pinned;case "floating"->r.night3.floating;case "released"->r.night3.released;case "arrived"->r.night3.arrived;
        case "gateBarred"->r.night3.barred;case "cutoff"->r.night3.cutoff;case "drained"->r.night3.drained;case "destroyed"->r.night3.destroyed;case "mustered"->r.night3.mustered;
        case "masterOpen"->r.night3.masterOpen;case "bargained"->r.night3.bargained;case "denied"->r.night3.denied;case "ended"->r.night3.ended;default->false;};}
    /** For tests: the night's clock, how many of the things are up, and who carries the first slot. */
    public static int night(BlockPos b){var r=run(b);return r==null?-1:r.night.tick;}
    public static int undertows(BlockPos b){var r=run(b);return r==null?0:r.night.mobs.size();}
    @Nullable public static UUID carrier(BlockPos b){var r=run(b);return r==null?null:r.night.carrier;}
    /** For tests: which day it is, and how many notches the drum's paint sits short of the witness mark (past it if negative). */
    public static int dayNo(BlockPos b){var r=run(b);return r==null?-1:r.dayNo;}
    public static int offset(BlockPos b){var r=run(b);return r==null?0:r.dayTwo.offset;}
    /** For tests: a reader who carries the first slot, as the hand at the grate would have left them. */
    public static void carryForTest(ServerPlayer p){var r=runOf(p);if(r==null)return;r.night.carrier=p.getUUID();var own=own(p);own.putBoolean("Infected",true);own.putInt("Slot",1);save(p,own);}
    /** One of the crew carrying the source: the things from the water pass them by. */
    public static boolean spared(ServerPlayer p){return !p.isSpectator()&&runOf(p)!=null&&own(p).getBoolean("Infected");}
    /** A hand reached down through the landing's rail (a click on one of its bars); a blow on the hand in the grate. */
    public static boolean reachDown(ServerPlayer p,BlockPos rel){return LastCrossingNight.reachDown(p,rel);}
    public static boolean hit(ServerPlayer p){return LastCrossingNight.strike(p);}
    @Nullable public static BlockPos plateAt(BlockPos b){var r=run(b);return r==null?null:r.plateAt;}
    @Nullable public static LiteraryActor vera(ServerLevel l,BlockPos b){var r=run(b);return r==null?null:actor(l,r.vera);}
    /** For tests: the day moved on by this many ticks, or the gathering wait run out. */
    public static void skip(BlockPos b,int ticks){var r=run(b);if(r==null)return;if(r.phase==DAY)r.day+=ticks;else if(r.phase==DUSK&&r.dayNo>=2)r.night2.tick+=ticks;else if(r.phase==DUSK)r.night.tick+=ticks;else if(r.phase==DAWN&&r.dayNo<3)r.dayTwo.morning+=ticks;else if(r.phase==DAWN&&r.dayNo==3)r.dayThree.morning+=ticks;else if(r.phase==GATHERING&&r.firstSigned>=0)r.firstSigned-=ticks;else if(r.phase<DAY)r.callTicks+=ticks;}
    public static void resetNow(ServerLevel l,BlockPos b){var r=run(b);if(r!=null){reset(l,r);RUNS.remove(b.asLong());}}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,BlockPos at,SoundEvent e,float volume,float pitch){l.playSound(null,at,e,SoundSource.BLOCKS,volume,pitch);}
    static String name(ServerPlayer p){return p.getGameProfile().getName();}
    /** One of the crew's records: a fact marked once. */
    static void mark(ServerPlayer p,String key){var own=own(p);if(own.getBoolean(key))return;own.putBoolean(key,true);save(p,own);}
    /** A job the reader has worked, in the order they worked them. */
    static void job(ServerPlayer p,String job){var own=own(p);var list=new ArrayList<>(Arrays.asList(own.getString("Jobs").isEmpty()?new String[0]:own.getString("Jobs").split(",")));
        if(!list.contains(job)){list.add(job);own.putString("Jobs",String.join(",",list));if(own.getString("Job").isEmpty())own.putString("Job",job);save(p,own);}}
    static void addName(ServerPlayer p,String key,String who){var own=own(p);var list=new ArrayList<>(Arrays.asList(own.getString(key).isEmpty()?new String[0]:own.getString(key).split(",")));
        if(!list.contains(who)){list.add(who);own.putString(key,String.join(",",list));save(p,own);}}
    /** Vera says something: over her head, and on the action bar of every reader in reach of her voice. */
    static void speak(ServerLevel l,Run r,String line,double reach){
        var a=actor(l,r.vera);if(a!=null){a.say(line);a.speaks(line);}
        var from=a==null?world(r.base,LastCrossingSite.VERA_PORCH):a.position();
        for(var p:readers(l,r.base))if(p.position().distanceToSqr(from)<reach*reach)p.displayClientMessage(Component.literal("Vera: “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static void speak(ServerLevel l,Run r,String line){speak(l,r,line,14);}
    /** Something said down a speaking pipe: heard at its other mouth by whoever is near it. */
    static void downPipe(ServerLevel l,Run r,BlockPos mouth,String who,String line){
        var at=Vec3.atCenterOf(r.base.offset(mouth));
        for(var p:readers(l,r.base))if(p.position().distanceToSqr(at)<6*6)p.displayClientMessage(Component.literal("From the pipe, "+who+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }

    // ------------------------------------------------------------------------------------------------ who is where
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    @Nullable static ServerPlayer player(ServerLevel l,UUID id){return id==null?null:l.getServer().getPlayerList().getPlayer(id);}
    static boolean here(ServerLevel l,Run r,UUID id){var p=player(l,id);return p!=null&&readers(l,r.base).contains(p);}
    /** Whether a reader could see a point: in reach, before their eyes, and nothing solid in the way. */
    static boolean sees(ServerPlayer p,Vec3 point,double reach,double cone){
        var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();if(d>reach)return false;if(d<.8)return true;
        if(p.getLookAngle().dot(to.scale(1/d))<cone)return false;
        var l=p.level();int steps=(int)(d/.2);
        for(int i=1;i<steps;i++){var at=eye.add(to.scale(i/(double)steps));var st=l.getBlockState(BlockPos.containing(at));
            if(st.isAir()||st.is(CrossingRegistry.FITTING.get())||!st.canOcclude())continue;return false;}
        return true;
    }
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable static Run runOf(ServerPlayer p){if(LiteraryVignettes.current(p)!=PLACE)return null;var b=LiteraryVignettes.base(p,PLACE);return b==null?null:run(b);}

    // ------------------------------------------------------------------------------------------------ the station's things carried about
    static ItemStack item(Item kind,String what,String name){
        var s=new ItemStack(kind);var tag=new CompoundTag();tag.putString(ITEM_TAG,what);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        if(name!=null)s.set(DataComponents.CUSTOM_NAME,Component.literal(name).withStyle(ChatFormatting.RESET));return s;
    }
    static String what(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?"":c.copyTag().getString(ITEM_TAG);}
    static boolean holds(ServerPlayer p,String what){return what(p.getMainHandItem()).equals(what);}
    static boolean carries(ServerPlayer p,String what){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(what(inv.getItem(i)).equals(what))return true;return false;}
    static void drop(ServerPlayer p,String what){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(what(inv.getItem(i)).equals(what))inv.setItem(i,ItemStack.EMPTY);}
    static boolean give(ServerPlayer p,ItemStack s){return p.getInventory().add(s);}

    // ------------------------------------------------------------------------------------------------ arriving
    /** The keys of one run's record, cleared when a reader comes to a station that has started over. */
    static final List<String> RUN_KEYS=List.of("Signed","RollOrder","Answered","Unanswered","HeardEllis","AskedEllis","Committed","Job","Jobs","OpenedBleed","TurnedValve","WrongValve",
        "GaugeRead","GaugeDip","Primed","BothSides","BrokenCorner","Tapped","SawGrates","TookWrench","HandedIntake","TookIntake","SeatedRoller","HatchTried","SawJam","Fairlead","Reported",
        "DryLedge","LineIn","LineReturned","TookLineToStage","StagedLine","CheckedWheel","CheckedPin","Rehearsed","RehearsedWith","Knocked","HeardKnocks","TookLamp","LampBelow","LampCarried",
        "SpillwayWalked","Crossed","RangBell","PlateReads","PlateClean","Readings","CarriedPlate","SetPlate","Handed","GotFrom","Route","RouteWalked","DayOne","Partway","Rejoined","SawVeraPlate","Swept",
        "HeardSurge","Gripped","Infected","Slot","Freed","FoughtFree","StruckHand","PulledUp","TookHand","Beat","Symptoms","SawClaw","SawSoaked","SawPressure","Latched","HeldBrake",
        "LowerFlooded","Isolated","BarredDoor","UnbarredDoor","SawUndertow","Wounded","Wounds","SawFouled","SawSill","HeldFerry","SawFerryLoose","LampStair","Told","ToldBy","Exposed",
        "SecondPrints","NightOne","Died","DiedNightOne","Watching",
        "HeardSlate","TookTools","FarEnd","ScrapedMarks","ReadMarks","CalledMarks","EasedDrum","CrankedDrum","SetCatch","CatchSlipped","LinedUp","LinedWith","RangWorkBell",
        "OpenedBypass","ShutBypass","Bypasses","SawBypassBy","SawBypassDip","SawWetGrate","SawStain","HeardScrape","DayCalls","CallWarded","PassedBy","SawPassedBy",
        "VeraAdmitted","HeardEllisVoice","DayTwo",
        "Turned","TurnedWhere","TurnedUnseen","SawTurn","AnsweredNightRoll","AbsentNightRoll","NotedAbsent","SpokeTurned","HeardRasp","HeldPull","CaughtBrake","LetGoBrake",
        "SawRunBack","Pulled","SawPull","PullFailed","WorkedJam","ClearedJam","ClearedJamBelow","SawJamCleared","HeldGate","PinnedGate","PinnedFor","PinnedBy","PulledPin",
        "Choked","Gilled","NightCalls","DrewThem","DrewIntoBranch","ShutThemIn","FeltGrateKnock","WentBelow","WentBelowHatch","WentBelowGrate","CameUp","CameUpGrate",
        "ThroughBranch","SawGoBelow","SawComeUp","SawPrints","SawGrateRise","Claws","Clawed","ClawedBy","ClawedReader","SawClaws","Grappled","SawGrapple","BrokeGrapple",
        "BrokenFreeBy","FoughtGrapple","PulledToLedge","PulledUpBy","SecondSlot","SecondAt","DeepCut","NightTwo","BackAtDawn","DawnWhere","DiedNightTwo",
        "HeardDepth","AnsweredDayRoll","AbsentDayRoll","NotedAbsentDay","HeardEllisAnswer","Plan","HeardBrief","DayThree","HeavedRoller","ResetRoller","SeatedLastRoller",
        "TookPin","StagedPin","PinHand","TestedWheel","LaunchHand","LaunchReady","TracedFeedPart","TracedFeed","ShovedDoor","OpenedGallery","TookBar","StagedBar","SawGate",
        "WorkedDebris","ClearedPath","ReachedCutoff","GalleryReady","KnowsMaster","SawMaster","SawDownstream","OfferedBreath","SawSource","AskedCross","Explained","ExplainedBy",
        "DiedDayThree",
        "SawRise","HeldWheel","HeldWheelToPin","SeatedLaunchPin","WentOverSide","PulledRelease","Aboard","LeftOnStation","AboardWith","SawTurnedAboard","CrossedOver","CrossedTurned",
        "LetGoGate","HeldFeedGate","BarredGate","HeldGateToBar","BarredWith","ClosedCutoff","SawSourceBare","StruckSource","DestroyedSource","SawSourceDie","ReleasedByFight",
        "RangMuster","OutOfGallery","HeardMuster","OpenedMaster","SawMasterOpened","LetGoMaster","HeardHammer","SawApproachFlood","SidedWithSource","OtherBreath",
        "ShutDownstream","BargainDenied","DeniedBy","DeniedBargain","DrainedLanding","SawLandingRise","Ending","BroughtAcross","UnansweredFar","HeardUnanswered","HeardAnsweredFor",
        "HeardEllisFar","HeardEllisLate","AnsweredFar","BackAtMorning","NightThree","DiedNightThree");
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var l=p.serverLevel();var r=RUNS.computeIfAbsent(b.asLong(),k->new Run(b,l.getGameTime()));
        if(own.getLong("CrossingRun")!=r.id){for(var key:RUN_KEYS)own.remove(key);own.putLong("CrossingRun",r.id);}
        // One of the crew who died here watches the rest of it from the far bank.
        if(r.dead.contains(p.getUUID())){own.putBoolean("Watching",true);var to=world(b,LastCrossingSite.FAR_WATCH);p.teleportTo(l,to.x,to.y,to.z,180,0);
            cue(p,"You watch from the far bank, across the water.");HousePackets.send(p,payload(r));return;}
        if(r.crew.containsKey(p.getUUID())){own.putBoolean("Rejoined",true);cue(p,"Vera looks up from the clipboard. “"+LastCrossingTexts.BACK+"”");}
        else if(r.phase>=DAY)own.putBoolean("Partway",true);
        cue(p,"A farm road, running north and down into floodwater. Beside it a power line on poles goes up a levee toward a station by a river.");
        HousePackets.send(p,payload(r));
    }
    /** Not dealt until a crew can finish the crossing (its last slice); reached by the operator's test door until then. */
    public static boolean dealable(LabyrinthData d,UUID p){return false;}

    // ------------------------------------------------------------------------------------------------ the station's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(RUNS.isEmpty())return;var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var r:List.copyOf(RUNS.values())){
            r.tick++;var here=readers(l,r.base);
            if(here.isEmpty()){if(++r.empty>=EMPTY_RESET&&l.isLoaded(r.base.offset(LastCrossingSite.CLIPBOARD))){reset(l,r);RUNS.remove(r.base.asLong());}continue;}
            r.empty=0;
            var anchor=r.base.offset(LastCrossingSite.PLATE_HOME);
            if(!l.isLoaded(anchor)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(anchor)))continue;
            if(r.fresh){r.fresh=false;reset(l,r);}
            ensureVera(l,r);ensureWrench(l,r);
            walk(l,r);
            switch(r.phase){
                case GATHERING->gather(l,r,here);
                case ROLL->roll(l,r,here);
                case DAY->{if(r.dayNo==1)day(l,r,here);else if(r.dayNo==2)LastCrossingDay.tick(l,r,here);else LastCrossingDayThree.tick(l,r,here);}
                case DUSK->{if(r.dayNo==1)LastCrossingNight.tick(l,r,here);else if(r.dayNo==2)LastCrossingNightTwo.tick(l,r,here);else LastCrossingNightThree.tick(l,r,here);}
                case DAWN->{if(r.dayNo<3)LastCrossingDay.morning(l,r,here);else LastCrossingDayThree.morning(l,r,here);}
                default->{}
            }
            LastCrossingDay.always(l,r,here);
            demo(l,r);
            intake(l,r,here);
            landing(l,r);
            plate(l,r,here);
            sweep(l,r,here);
            if(r.tapAt>=0&&r.tick>=r.tapAt){r.tapAt=-1;sound(l,r.base.offset(LastCrossingSite.GRATE),CrossingRegistry.TAP.get(),.8F,1F);}
            if(r.kettle&&r.tick%20==0)l.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,r.base.getX()+LastCrossingSite.STOVE.getX()+.35,r.base.getY()+1.0,r.base.getZ()+LastCrossingSite.STOVE.getZ()+.5,1,.02,.05,.02,.005);
            if(r.tick%40==0)for(var p:here)HousePackets.send(p,payload(r));
        }
    }
    /** What each reader's client is told: the sky's hour, who is turned tonight and whose clothes are torn. */
    static CrossingPayload payload(Run r){return new CrossingPayload(skyTime(r),List.copyOf(r.night2.turned),List.copyOf(r.night2.torn));}
    /** Sent at once to everyone at the station, when someone turns or comes back to themselves, or is torn. */
    static void sync(ServerLevel l,Run r){var payload=payload(r);for(var p:readers(l,r.base))HousePackets.send(p,payload);}
    /** The sky's hour: morning until the roll; Day One running from morning to dusk; Night One from dusk to first light; the morning after. */
    static long skyTime(Run r){int night=r.dayNo>=2?r.night2.tick:r.night.tick;int len=r.dayNo>=3?LastCrossingNightThree.NIGHT3_TICKS:NIGHT_TICKS;
        return r.phase<DAY?1000:r.phase==DAY?1000+11500L*Math.min(DAY_TICKS,r.day)/DAY_TICKS:r.phase==DUSK?12500+11000L*Math.min(len,night)/len:24500;}
    /** Vera, on her porch unless she is on her way somewhere. */
    static void ensureVera(ServerLevel l,Run r){
        if(actor(l,r.vera)!=null)return;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,VERA);
        place(a,r.base,LastCrossingSite.VERA_PORCH,180);
        if(l.addFreshEntity(a))r.vera=a.getUUID();
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 rel,float yaw){a.moveTo(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static void ensureWrench(ServerLevel l,Run r){
        if(!r.wrenchHome)return;if(r.wrenchDisplay!=null&&l.getEntity(r.wrenchDisplay)!=null)return;
        var at=world(r.base,new Vec3(LastCrossingSite.WRENCH_RUN.getX()+.5,LastCrossingSite.WRENCH_RUN.getY()+.66,LastCrossingSite.WRENCH_RUN.getZ()+.45));
        var e=KitchenFittings.lay(l,at,item(CrossingRegistry.WRENCH.get(),"wrench",null),30,"crossing_wrench",.5F);if(e!=null)r.wrenchDisplay=e.getUUID();
    }
    /** Sends Vera along points (relative to the station's base). */
    static void route(Run r,Vec3... points){r.route.clear();r.route.addAll(List.of(points));r.leg=0;r.legTicks=0;}
    static boolean walking(Run r){return r.leg<r.route.size();}
    static void walk(ServerLevel l,Run r){
        var a=actor(l,r.vera);if(a==null)return;
        if(!walking(r)){var near=nearest(l,r,a.position(),12);if(near!=null)SceneFootpaths.face(a,near.getEyePosition(),20);return;}
        var target=world(r.base,r.route.get(r.leg));
        // A leg the footpaths cannot finish (a body caught on a step or a door frame) is finished with a step, so she never stands stuck.
        if(SceneFootpaths.walk(a,target,.12)||a.position().distanceToSqr(target)<.09||++r.legTicks>160){if(r.legTicks>160)a.moveTo(target.x,target.y,target.z,a.getYRot(),0);r.leg++;r.legTicks=0;}
    }
    @Nullable static ServerPlayer nearest(ServerLevel l,Run r,Vec3 from,double reach){ServerPlayer best=null;double d=reach*reach;for(var p:readers(l,r.base)){double q=p.position().distanceToSqr(from);if(q<d&&!p.isSpectator()){d=q;best=p;}}return best;}

    // ------------------------------------------------------------------------------------------------ the roll
    static void gather(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.crew.isEmpty())return;
        boolean all=true;for(var p:here)if(!p.isSpectator()&&!r.crew.containsKey(p.getUUID()))all=false;
        if(all||r.tick-r.firstSigned>=GATHER_WAIT){r.phase=ROLL;r.calling=-1;r.callTicks=0;speak(l,r,LastCrossingTexts.ROLL,64);}
    }
    static void roll(ServerLevel l,Run r,List<ServerPlayer> here){
        r.callTicks++;var names=new ArrayList<>(r.crew.entrySet());
        if(r.calling<0){if(r.callTicks>=30)callNext(l,r);return;}
        if(r.calling<names.size()){
            var id=names.get(r.calling).getKey();
            if(r.answered.contains(id)){if(r.tick-r.answerAt>=20)callNext(l,r);}
            else if(!r.calledTwice&&r.callTicks>=ANSWER){r.calledTwice=true;r.callTicks=0;speak(l,r,names.get(r.calling).getValue()+"?",64);offer(l,r,id,names.get(r.calling).getValue()+"?");}
            else if(r.calledTwice&&r.callTicks>=AGAIN){var p=player(l,id);if(p!=null){mark(p,"Unanswered");Talk.close(p);}callNext(l,r);}
            return;
        }
        // Ellis, after the last of them: she waits, and nobody answers.
        if(r.callTicks>=ELLIS_WAIT){
            for(var p:here){var own=own(p);own.putBoolean("HeardEllis",true);save(p,own);}
            r.phase=DAY;r.day=0;r.demo=1;speak(l,r,LastCrossingTexts.AFTER_ROLL,64);
            route(r,new Vec3(15.5,0,-36.5),new Vec3(15.5,0,-39.5),LastCrossingSite.VERA_PLATE);
        }
    }
    static void callNext(ServerLevel l,Run r){
        r.calling++;r.callTicks=0;r.calledTwice=false;var names=new ArrayList<>(r.crew.entrySet());
        if(r.calling<names.size()){var e=names.get(r.calling);speak(l,r,e.getValue()+".",64);offer(l,r,e.getKey(),e.getValue()+".");}
        else speak(l,r,LastCrossingTexts.ELLIS,64);
    }
    /** The name called: the reader it belongs to is asked to answer. */
    static void offer(ServerLevel l,Run r,UUID id,String called){
        var p=player(l,id);if(p==null||!readers(l,r.base).contains(p))return;
        Talk.begin(p,TALK,"Vera");Talk.line(p,"Vera",called);Talk.ask(p,Talk.reply("Here.",LastCrossing::answer));
    }
    static void answer(ServerPlayer p){
        Talk.close(p);var r=runOf(p);if(r==null||!r.crew.containsKey(p.getUUID()))return;
        r.answered.add(p.getUUID());r.answerAt=r.tick;mark(p,"Answered");
    }
    /** Signing the roll: the reader's name on Vera's sheet, and their place in the crew kept for the whole crossing. */
    static void sign(ServerPlayer p){
        Talk.close(p);var r=runOf(p);if(r==null)return;var l=p.serverLevel();
        if(r.crew.containsKey(p.getUUID())){cue(p,"Your name's already on it.");return;}
        if(r.crew.size()>=MAX_CREW){cue(p,"The sheet's full. Six names, and nowhere to write a seventh.");return;}
        r.crew.put(p.getUUID(),name(p));if(r.firstSigned<0)r.firstSigned=r.tick;
        var own=own(p);own.putBoolean("Signed",true);own.putInt("RollOrder",r.crew.size());own.putBoolean("Committed",true);own.remove("Partway");save(p,own);
        l.setBlock(r.base.offset(LastCrossingSite.CLIPBOARD),LastCrossingSite.fit(Kind.CLIPBOARD,Direction.SOUTH,1),Block.UPDATE_CLIENTS);
        cue(p,"You write your name under the others.");
        // Signed once the roll has been taken: Vera calls the name straight away.
        if(r.phase>=DAY){speak(l,r,name(p)+".");offer(l,r,p.getUUID(),name(p)+".");}
    }

    // ------------------------------------------------------------------------------------------------ Day One
    static void day(ServerLevel l,Run r,List<ServerPlayer> here){
        r.day++;
        // The rehearsal: once the wheel and the pin have both been tried, Vera goes down to the ramp to have them practise.
        if(!r.rehearsePrompted&&!r.rehearsed&&r.demo>=5&&anyone(l,r,"CheckedWheel")&&anyone(l,r,"CheckedPin")){r.rehearsePrompted=true;r.rehearseAt=-1;
            route(r,new Vec3(13.5,0,-35.5),new Vec3(26.5,0,-35.5),new Vec3(26.5,0,-50.5),LastCrossingSite.VERA_RAMP);}
        if(r.rehearsePrompted&&r.rehearseAt<0&&!walking(r)){r.rehearseAt=r.tick;speak(l,r,LastCrossingTexts.REHEARSE);}
        if(r.rehearseAt>=0&&(r.rehearsed||r.tick-r.rehearseAt>1200)&&!walking(r)&&r.route.size()>0&&r.route.get(r.route.size()-1).equals(LastCrossingSite.VERA_RAMP)){
            route(r,new Vec3(26.5,0,-50.5),new Vec3(26.5,0,-35.5),new Vec3(13.5,0,-35.5),LastCrossingSite.VERA_PORCH);}
        // Round the route once before dark: when the day's work is done, or two thirds of the way to dusk.
        if(!r.routeCalled&&(r.primed&&r.rollerSeated&&r.jamReported&&r.lineReturned||r.day>=DAY_TICKS*2/3)){r.routeCalled=true;speak(l,r,LastCrossingTexts.ROUTE,64);}
        if(r.day>=DAY_TICKS)dusk(l,r,here);
    }
    static boolean anyone(ServerLevel l,Run r,String key){for(var p:readers(l,r.base))if(own(p).getBoolean(key))return true;return false;}
    /** Dusk: the light going. If the intake still isn't primed, Vera calls the valve down the pipe herself, and goes in to keep the night in
     * the gauge room (LastCrossingNight). */
    static void dusk(ServerLevel l,Run r,List<ServerPlayer> here){
        r.phase=DUSK;r.dusk=true;speak(l,r,LastCrossingTexts.DUSK,64);LastCrossingNight.begin(l,r);
        if(!r.primed&&r.bleedOpen&&r.needle>=0)downPipe(l,r,LastCrossingSite.PIPE_A_DOWN,"Vera",String.format(LastCrossingTexts.DUSK_CALL,r.needle+1));
        for(var p:here)if(r.crew.containsKey(p.getUUID()))mark(p,"DayOne");
    }
    /** After the roll: Vera at the plate, then the kettle, then back to her porch. */
    static void demo(ServerLevel l,Run r){
        if(r.demo==0||r.demo>=5||walking(r))return;
        switch(r.demo){
            case 1->{r.demo=2;r.demoAt=r.tick;
                if(LastCrossingSite.PLATE_HOME.equals(r.plateAt)){fog(l,r,false);speak(l,r,LastCrossingTexts.PLATE_SHOWN);
                    for(var p:readers(l,r.base))if(sees(p,Vec3.atCenterOf(r.base.offset(LastCrossingSite.PLATE_HOME)),10,.4))mark(p,"SawVeraPlate");}
                else speak(l,r,"Somebody's got the plate. Bring it back here when you're done with it.");}
            case 2->{if(r.tick-r.demoAt>=80){r.demo=3;route(r,LastCrossingSite.VERA_STOVE);}}
            case 3->{r.demo=4;r.kettle=true;speak(l,r,LastCrossingTexts.KETTLE);route(r,new Vec3(15.5,0,-39.5),new Vec3(15.5,0,-36.5),LastCrossingSite.VERA_PORCH);}
            case 4->r.demo=5;
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ the intake
    static void intake(ServerLevel l,Run r,List<ServerPlayer> here){
        var b=r.base;
        // The brake, held open from above until the intake settles.
        if(r.brakeBy!=null){var p=player(l,r.brakeBy);
            if(p==null||!here.contains(p)||p.position().distanceToSqr(Vec3.atCenterOf(b.offset(LastCrossingSite.BRAKE)))>2.5*2.5){
                r.brakeBy=null;r.brakeHeld=0;l.setBlock(b.offset(LastCrossingSite.BRAKE),LastCrossingSite.fit(Kind.BRAKE,Direction.WEST),Block.UPDATE_CLIENTS);if(p!=null&&!r.primed)cue(p,"You let the brake go too soon. The needle drops back.");}
            else if(++r.brakeHeld>=BRAKE_HOLD)prime(l,r,p);
        }
        // A turned valve creeps back if nobody takes the load in time.
        if(!r.primed&&r.turnedAt>=0&&r.brakeBy==null&&r.tick-r.turnedAt>VALVE_DRIFT){r.turned=0;r.turnedAt=-1;
            for(var p:here){var q=rel(p.position(),b);if(LastCrossingSite.inIntake(q)||LastCrossingSite.inGaugeRoom(q))cue(p,"Below, the valve creeps back. The needle sinks in its band.");}}
        // Once primed, the needle dips now and then though the pump's note never changes.
        // From Day Two, an open bypass draws the same dip more often.
        if(r.primed){long since=r.tick-r.primedAt;int every=LastCrossingDay.open(r)?DIP_EVERY/3:DIP_EVERY;boolean dip=since>=every&&since%every<DIP_FOR;int night=LastCrossingNight.gauge(r);
            var want=LastCrossingSite.fit(Kind.GAUGE,Direction.WEST,night>=0?night:dip?Math.floorMod(r.needle+1,3):3);var at=b.offset(LastCrossingSite.GAUGE);
            if(!l.getBlockState(at).equals(want))l.setBlock(at,want,Block.UPDATE_CLIENTS);
            if(dip)for(var p:here){if(p.position().distanceToSqr(Vec3.atCenterOf(at))>3.5*3.5)continue;var own=own(p);
                if(r.dayNo>=2&&LastCrossingDay.open(r)&&!own.getBoolean("SawBypassDip")){own.putBoolean("SawBypassDip",true);own.putBoolean("GaugeDip",true);save(p,own);
                    cue(p,"The needle dips again, sooner than it did yesterday. The pump's note hasn't changed.");continue;}
                if(own.getBoolean("GaugeDip"))continue;own.putBoolean("GaugeDip",true);save(p,own);
                cue(p,"The needle dips, though the pump's note hasn't changed. Then it creeps back.");}}
    }
    /** The pump takes the load: the needle settles, the pump runs, and everyone who had a hand in it remembers. */
    static void prime(ServerLevel l,Run r,ServerPlayer brake){
        var b=r.base;r.primed=true;r.primedAt=r.tick;r.brakeBy=null;r.brakeHeld=0;
        l.setBlock(b.offset(LastCrossingSite.GAUGE),LastCrossingSite.fit(Kind.GAUGE,Direction.WEST,3),Block.UPDATE_CLIENTS);
        l.setBlock(b.offset(LastCrossingSite.PUMP),LastCrossingSite.fit(Kind.PUMP,Direction.SOUTH,1),Block.UPDATE_CLIENTS);
        l.setBlock(b.offset(LastCrossingSite.BRAKE),LastCrossingSite.fit(Kind.BRAKE,Direction.WEST),Block.UPDATE_CLIENTS);
        sound(l,b.offset(LastCrossingSite.PUMP),CrossingRegistry.PUMP.get(),1F,1F);
        for(var p:readers(l,b)){var q=rel(p.position(),b);if(LastCrossingSite.inIntake(q)||LastCrossingSite.inGaugeRoom(q))cue(p,"The pump takes the load. On the gauge the needle climbs and settles.");}
        for(var id:new UUID[]{r.intakeBy,r.turnedBy,brake.getUUID()}){var p=player(l,id);if(p==null)continue;mark(p,"Primed");}
        job(brake,"gauge");
    }

    // ------------------------------------------------------------------------------------------------ the landing
    static void landing(ServerLevel l,Run r){
        var b=r.base;
        if(r.wheelBy!=null&&!r.pinSeated){var p=player(l,r.wheelBy);
            if(p==null||r.tick>r.wheelUntil||!readers(l,b).contains(p)||p.position().distanceToSqr(Vec3.atCenterOf(b.offset(LastCrossingSite.WHEEL)))>2.5*2.5){
                r.wheelBy=null;l.setBlock(b.offset(LastCrossingSite.WHEEL),LastCrossingSite.fit(Kind.WHEEL,Direction.SOUTH),Block.UPDATE_CLIENTS);}}
        // In practice the pin is knocked back out, ready for the night.
        if(r.pinSeated&&!LastCrossingNightThree.keepsPin(r)&&r.tick-r.pinAt>=PIN_RESET){r.pinSeated=false;r.wheelBy=null;
            l.setBlock(b.offset(LastCrossingSite.PIN),LastCrossingSite.fit(Kind.PIN,Direction.SOUTH),Block.UPDATE_CLIENTS);
            l.setBlock(b.offset(LastCrossingSite.WHEEL),LastCrossingSite.fit(Kind.WHEEL,Direction.SOUTH),Block.UPDATE_CLIENTS);
            sound(l,b.offset(LastCrossingSite.PIN),SoundEvents.IRON_TRAPDOOR_OPEN,.8F,.8F);
            for(var p:readers(l,b)){var q=rel(p.position(),b);if(LastCrossingSite.inPit(q)||q.distanceToSqr(Vec3.atCenterOf(LastCrossingSite.PIN))<5*5)cue(p,"The pin is knocked back out of its socket, ready for the night.");}}
    }

    // ------------------------------------------------------------------------------------------------ the plate
    /** The plate's glass shows a reading for a moment: a clean palm's fog, or a second print on the far side. */
    static void fog(ServerLevel l,Run r,boolean second){
        if(r.plateAt==null)return;var at=r.base.offset(r.plateAt);var s=l.getBlockState(at);if(!CrossingBlock.plate(s))return;
        l.setBlock(at,s.setValue(CrossingBlock.STAGE,second?3:1),Block.UPDATE_CLIENTS);r.fogUntil=r.tick+40;
    }
    static void plate(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.fogUntil>=0&&r.tick>=r.fogUntil){r.fogUntil=-1;if(r.plateAt!=null){var at=r.base.offset(r.plateAt);var s=l.getBlockState(at);if(CrossingBlock.plate(s))l.setBlock(at,s.setValue(CrossingBlock.STAGE,0),Block.UPDATE_CLIENTS);}}
        // A carrier who has left the station, or gone: the plate goes back to its bracket in the office.
        if(r.plateBy!=null){var p=player(l,r.plateBy);if(p==null||!here.contains(p)){if(p!=null)drop(p,"plate");r.plateBy=null;home(l,r);}}
    }
    static void home(ServerLevel l,Run r){
        var at=r.base.offset(LastCrossingSite.PLATE_HOME);
        if(r.plateAt!=null&&!r.plateAt.equals(LastCrossingSite.PLATE_HOME)){var old=r.base.offset(r.plateAt);var s=l.getBlockState(old);
            if(CrossingBlock.is(s,Kind.PLATE))l.setBlock(old,LastCrossingSite.fit(Kind.BRACKET,s.getValue(CrossingBlock.FACING)),Block.UPDATE_CLIENTS);else if(CrossingBlock.is(s,Kind.PLATE_SET))l.setBlock(old,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
        l.setBlock(at,LastCrossingSite.fit(Kind.PLATE,Direction.SOUTH),Block.UPDATE_CLIENTS);r.plateAt=LastCrossingSite.PLATE_HOME;
    }
    static void touch(ServerPlayer p){
        Talk.close(p);var r=runOf(p);if(r==null||r.plateAt==null)return;var l=p.serverLevel();
        boolean infected=own(p).getBoolean("Infected");fog(l,r,infected);
        var own=own(p);own.putInt("PlateReads",own.getInt("PlateReads")+1);if(!infected)own.putBoolean("PlateClean",true);else own.putBoolean("Exposed",true);save(p,own);
        cue(p,infected?"A breath of fog on the near side, and on the far side a second print comes up and closes round your hand.":"Your palm leaves a breath of fog on the near side of the glass. Nothing comes up on the far side.");
        if(infected)LastCrossingDayThree.exposed(p);
        var at=Vec3.atCenterOf(r.base.offset(r.plateAt));
        for(var q:readers(l,r.base)){if(q==p||!sees(q,at,8,.4))continue;
            cue(q,name(p)+" lays a palm on the plate: fog on the near side, "+(infected?"and a second print on the far.":"nothing on the far."));addName(q,"Readings",name(p));if(infected)addName(q,"SecondPrints",name(p));}
    }
    static void lift(ServerPlayer p){
        Talk.close(p);var r=runOf(p);if(r==null||r.plateAt==null)return;var l=p.serverLevel();
        if(!p.getOffhandItem().isEmpty()||!p.getMainHandItem().isEmpty()){cue(p,"You'll need both hands for it.");return;}
        var at=r.base.offset(r.plateAt);var s=l.getBlockState(at);if(!CrossingBlock.plate(s))return;
        l.setBlock(at,CrossingBlock.is(s,Kind.PLATE)?LastCrossingSite.fit(Kind.BRACKET,s.getValue(CrossingBlock.FACING)):Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        p.getInventory().setItem(p.getInventory().selected,item(CrossingRegistry.PLATE.get(),"plate",null));
        r.plateAt=null;r.plateBy=p.getUUID();r.fogUntil=-1;mark(p,"CarriedPlate");
        cue(p,"You lift the plate out. It takes both hands.");
    }
    /** Where the plate is set: a bracket's name, or the place a floor is in. */
    static String placeName(Vec3 rel){
        if(LastCrossingSite.inOffice(rel))return "office";if(LastCrossingSite.inGaugeRoom(rel))return "gauge room";if(LastCrossingSite.inIntake(rel))return "intake";
        if(LastCrossingSite.inCableHouse(rel))return "cable house";if(LastCrossingSite.atRamp(rel)||LastCrossingSite.inPit(rel))return "ramp";if(LastCrossingSite.onFerry(rel))return "ferry";return "station";
    }
    static void setDown(ServerLevel l,Run r,ServerPlayer p,BlockPos rel,BlockState state){
        l.setBlock(r.base.offset(rel),state,Block.UPDATE_CLIENTS);drop(p,"plate");r.plateAt=rel;r.plateBy=null;
        var own=own(p);var list=new ArrayList<>(Arrays.asList(own.getString("SetPlate").isEmpty()?new String[0]:own.getString("SetPlate").split(",")));var where=placeName(Vec3.atCenterOf(rel));
        if(!list.contains(where)){list.add(where);own.putString("SetPlate",String.join(",",list));}save(p,own);
    }
    /** The plate set down where its carrier points: on a sturdy floor at the station, with room above. */
    public static boolean setDownOn(ServerPlayer p,BlockPos pos,Direction face){
        var r=runOf(p);if(r==null||!p.getUUID().equals(r.plateBy)||!holds(p,"plate"))return false;var l=p.serverLevel();
        var above=pos.above();var rel=above.subtract(r.base);
        if(face!=Direction.UP||!l.getBlockState(above).isAir()||!l.getBlockState(pos).isFaceSturdy(l,pos,Direction.UP)||!LastCrossingSite.atStation(Vec3.atBottomCenterOf(rel))){cue(p,"It won't stand there.");return false;}
        setDown(l,r,p,rel,LastCrossingSite.fit(Kind.PLATE_SET,p.getDirection().getOpposite()));cue(p,"You stand the plate on its foot.");return true;
    }
    /** The plate put down near a reader who can no longer carry it (it takes both hands): on the floor where they stand, or home. */
    static void putDownNear(ServerLevel l,Run r,ServerPlayer p){
        var feet=p.blockPosition();
        for(int dy=0;dy<=1;dy++)for(int dx=0;dx<=2;dx++)for(var off:new int[][]{{0,0},{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,-1},{1,-1},{-1,1}}){
            var at=feet.offset(off[0]*dx,dy,off[1]*dx);if(dx==0&&(off[0]!=0||off[1]!=0))continue;
            if(l.getBlockState(at).isAir()&&l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP)&&LastCrossingSite.atStation(Vec3.atBottomCenterOf(at.subtract(r.base)))){
                setDown(l,r,p,at.subtract(r.base),LastCrossingSite.fit(Kind.PLATE_SET,p.getDirection().getOpposite()));return;}}
        drop(p,"plate");r.plateBy=null;home(l,r);
    }

    // ------------------------------------------------------------------------------------------------ what was carried away
    /** Anything of the station's carried off it goes back where it belongs: the lamp to its hook, the wrench to the pipe, the line to the ramp. */
    static void sweep(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.tick%20!=0)return;
        for(var p:l.getServer().getPlayerList().getPlayers()){if(here.contains(p))continue;
            for(var w:List.of("lamp","wrench","line","plate","tools","pin","lockbar"))if(carries(p,w)){drop(p,w);restore(l,r,w);}}
        // Whatever nobody holds and nobody dropped goes home too.
        boolean lamp=false,wrench=false,line=false,tools=false,pin=false,bar=false;
        for(var p:here){lamp|=carries(p,"lamp");wrench|=carries(p,"wrench");line|=carries(p,"line");tools|=carries(p,"tools");pin|=carries(p,"pin");bar|=carries(p,"lockbar");}
        var area=new AABB(r.base.offset(LastCrossingSite.WEST,LastCrossingSite.LOW,LastCrossingSite.NORTH)).minmax(new AABB(r.base.offset(LastCrossingSite.EAST,LastCrossingSite.HIGH,1)));
        for(var e:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area)){var w=what(e.getItem());lamp|=w.equals("lamp");wrench|=w.equals("wrench");line|=w.equals("line");tools|=w.equals("tools");pin|=w.equals("pin");bar|=w.equals("lockbar");}
        if(!r.lampHome&&!lamp)restore(l,r,"lamp");
        if(!r.wrenchHome&&!wrench)restore(l,r,"wrench");
        if(r.lineIn&&!r.lineReturned&&!line)restore(l,r,"line");
        if(r.dayTwo.toolsTaken&&!tools)restore(l,r,"tools");
        if(LastCrossingDayThree.out(r,"pin")&&!pin)restore(l,r,"pin");
        if(LastCrossingDayThree.out(r,"lockbar")&&!bar)restore(l,r,"lockbar");
    }
    static void restore(ServerLevel l,Run r,String w){
        var b=r.base;
        switch(w){
            case "lamp"->{r.lampHome=true;l.setBlock(b.offset(LastCrossingSite.LAMP_HOOK),LastCrossingSite.fit(Kind.HOOK,Direction.EAST,1),Block.UPDATE_CLIENTS);}
            case "wrench"->{r.wrenchHome=true;r.wrenchDisplay=null;}
            case "line"->{if(!r.lineReturned){r.lineIn=false;l.setBlock(b.offset(LastCrossingSite.RAMP_COIL),LastCrossingSite.fit(Kind.COIL,Direction.NORTH),Block.UPDATE_CLIENTS);}
                else if(!r.lineStaged)l.setBlock(b.offset(LastCrossingSite.PORCH_COIL),LastCrossingSite.fit(Kind.COIL,Direction.NORTH,1),Block.UPDATE_CLIENTS);}
            case "plate"->{r.plateBy=null;home(l,r);}
            case "tools"->{r.dayTwo.toolsTaken=false;LastCrossingDay.tools(l,r);}
            case "pin","lockbar"->LastCrossingDayThree.restore(l,r,w);
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ each reader
    static final int OFFICE=1,GAUGE_ROOM=2,INTAKE=4,CABLE=8,RAMP=16;
    /** Every five ticks, for each reader at the station. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var r=run(b);var own=own(p);
        if(r==null){refresh(held,own);return;}
        var l=p.serverLevel();var rel=rel(p.position(),b);
        // Vera greets each reader who comes up to her porch, once.
        var vera=actor(l,r.vera);
        if(vera!=null&&r.phase==GATHERING&&!r.greeted.containsKey(p.getUUID())&&p.distanceToSqr(vera)<10*10){r.greeted.put(p.getUUID(),r.tick);speak(l,r,LastCrossingTexts.GREET);}
        // The plate takes both hands: switching it out of the hand puts it down; nothing else can be held with it.
        if(carries(p,"plate")){
            if(!p.getUUID().equals(r.plateBy))drop(p,"plate");
            else if(!holds(p,"plate")){putDownNear(l,r,p);cue(p,"It takes both hands. You set it down.");}
            else if(!p.getOffhandItem().isEmpty()){var off=p.getOffhandItem().copy();p.getInventory().offhand.set(0,ItemStack.EMPTY);if(!give(p,off))p.drop(off,false);cue(p,"You can't hold anything else with the plate.");}
        }
        own=own(p);
        // What a reader learns by being somewhere.
        if(LastCrossingSite.onLedge(rel)&&!own.getBoolean("DryLedge")){own.putBoolean("DryLedge",true);cue(p,"A dry ledge along the wall, out of the drum's way. Somewhere to stand guard during a pull.");}
        if(LastCrossingSite.onBridge(rel)&&!own.getBoolean("Crossed"))own.putBoolean("Crossed",true);
        if(LastCrossingSite.onBridge(rel)&&!own.getBoolean("SpillwayWalked")){
            for(var q:readers(l,b))if(q!=p&&q.distanceToSqr(p)<8*8&&own(q).getString("Jobs").contains("lookout")){own.putBoolean("SpillwayWalked",true);save(p,own);job(p,"spillway");own=own(p);
                cue(p,"You walk the spillway with "+name(q)+", the water up to the lip of its channel.");break;}}
        if(LastCrossingSite.inGaugeRoom(rel)&&!own.getBoolean("SawGrates")&&p.getXRot()>40&&rel.x>LastCrossingSite.LANDING_W-2.5&&rel.x<LastCrossingSite.LANDING_E+2.5&&rel.z<LastCrossingSite.LANDING_S+2.5&&rel.z>LastCrossingSite.LANDING_N-2.5){
            own.putBoolean("SawGrates",true);cue(p,"Through the landing you can see the intake below: the bleed screw, the valves, the service grate with its broken corner and the black water under it.");}
        // The station lamp, carried from the intake up to the gauge room.
        if(carries(p,"lamp")){if(LastCrossingSite.inIntake(rel))own.putBoolean("LampBelow",true);
            else if(LastCrossingSite.inGaugeRoom(rel)&&own.getBoolean("LampBelow")&&!own.getBoolean("LampCarried")){own.putBoolean("LampCarried",true);save(p,own);job(p,"lamp");own=own(p);
                cue(p,"You've carried the lamp from the intake up to the gauge room. You'd find the way back in the dark.");}}
        if(own.getBoolean("GaugeRead")&&own.getBoolean("TurnedValve")&&!own.getBoolean("BothSides"))own.putBoolean("BothSides",true);
        // Round the route once, after Vera asks.
        if(r.routeCalled&&r.crew.containsKey(p.getUUID())&&!own.getBoolean("RouteWalked")){
            int bits=own.getInt("Route");
            if(LastCrossingSite.inOffice(rel))bits|=OFFICE;if(LastCrossingSite.inGaugeRoom(rel))bits|=GAUGE_ROOM;if(LastCrossingSite.inIntake(rel))bits|=INTAKE;
            if(LastCrossingSite.inCableHouse(rel))bits|=CABLE;if(LastCrossingSite.atRamp(rel)||LastCrossingSite.inPit(rel))bits|=RAMP;
            own.putInt("Route",bits);
            if(bits==31){own.putBoolean("RouteWalked",true);cue(p,"You've been round all of it once: the office, the pump room and the intake under it, the cable house, the ramp.");}
        }
        // Nobody swims the flood: the current takes a swimmer along the river wall and back to the landing.
        boolean wet=p.isInWater()||l.getFluidState(p.blockPosition()).is(net.minecraft.tags.FluidTags.WATER);
        if(LastCrossingSite.inRiver(rel)&&wet&&!p.isSpectator()){int t=r.current.merge(p.getUUID(),5,Integer::sum);
            p.setDeltaMovement(p.getDeltaMovement().add(.08,0,.02));p.hurtMarked=true;
            if(t>=CURRENT){r.current.remove(p.getUUID());var to=world(b,LastCrossingSite.LANDING_SPOT);p.teleportTo(l,to.x,to.y,to.z,p.getYRot(),p.getXRot());
                cue(p,"The current drags you along the river wall until you get a hand on the landing.");}}
        else r.current.remove(p.getUUID());
        save(p,own);refresh(held,own);
    }

    // ------------------------------------------------------------------------------------------------ hands on things
    static void begin(ServerPlayer p,String header){Talk.begin(p,TALK,header);}
    /** Clicks on the station's fittings. */
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        var r=runOf(p);if(r==null)return false;var b=r.base;var rel=pos.subtract(b);var l=p.serverLevel();
        var kind=s.getValue(CrossingBlock.KIND);int stage=s.getValue(CrossingBlock.STAGE);
        switch(kind){
            case CLIPBOARD->{
                begin(p,"Vera's clipboard");
                Talk.line(p,"","Today's roll: a ruled sheet with the station's name at the top"+(r.crew.isEmpty()?", and nobody's name on it yet.":", and under it, in pencil: "+String.join(", ",r.crew.values())+"."));
                if(r.crew.containsKey(p.getUUID())){Talk.line(p,"","Your name's on it.");Talk.ask(p,Talk.act("Put it down.",Talk::close));}
                else Talk.ask(p,Talk.act("Sign the roll.",LastCrossing::sign),Talk.act("Not yet.",Talk::close));
                return true;}
            case PLATE,PLATE_SET->{
                if(!p.getMainHandItem().isEmpty()){cue(p,"Your hands are full.");return true;}
                begin(p,"The handprint plate");
                Talk.line(p,"","Thick glass in a brass frame, a palm etched on this side.");
                Talk.ask(p,Talk.act("Lay your palm on it.",LastCrossing::touch),Talk.act(kind==Kind.PLATE?"Lift it out of its bracket.":"Pick it up.",LastCrossing::lift),Talk.act("Leave it.",Talk::close));
                return true;}
            case BRACKET->{
                if(!holds(p,"plate")||!p.getUUID().equals(r.plateBy))return false;
                setDown(l,r,p,rel,LastCrossingSite.fit(Kind.PLATE,s.getValue(CrossingBlock.FACING)));cue(p,"You set the plate in the bracket.");return true;}
            case RUN->{
                if(!rel.equals(LastCrossingSite.WRENCH_RUN)||!r.wrenchHome)return false;
                if(!give(p,item(CrossingRegistry.WRENCH.get(),"wrench",null))){cue(p,"Your hands are full.");return true;}
                r.wrenchHome=false;var e=r.wrenchDisplay==null?null:l.getEntity(r.wrenchDisplay);if(e!=null)e.discard();r.wrenchDisplay=null;mark(p,"TookWrench");
                cue(p,"You take the wrench off the pipe.");return true;}
            case BLEED->{return bleed(p,r,stage);}
            case FLOOR_GRATE->{return LastCrossingNightTwo.floorGrate(p,stage);}
            case BRANCH->{return LastCrossingNightTwo.branchGrate(p,rel);}
            case UNDERSIDE->{return LastCrossingNightTwo.underside(p,stage);}
            case GRATE->{
                if(stage==2){cue(p,"A hand, up through the broken corner, holding on. Hit it.");return true;}
                var own=own(p);own.putBoolean("BrokenCorner",true);
                if(holds(p,"wrench")){own.putBoolean("Tapped",true);save(p,own);r.tapAt=r.tick+20;sound(l,pos,SoundEvents.ANVIL_LAND,.25F,1.8F);
                    cue(p,"The wrench slips and rings on the grate. A moment later, something taps back.");return true;}
                save(p,own);cue(p,"One corner of the grate is broken off. Below it, black water.");return true;}
            case GAUGE->{return gauge(p,r);}
            case VALVE->{return valve(p,r,stage);}
            case BRAKE->{
                if(rel.equals(LastCrossingSite.CABLE_BRAKE)){if(LastCrossingNightTwo.brake(p)||LastCrossingDayThree.brake(p)||LastCrossingDay.brake(p))return true;cue(p,"The brake's set. Nobody's pulling today.");return true;}
                if(LastCrossingNight.brakeUse(p))return true;
                if(r.primed){cue(p,"The brake's off. The pump's holding.");return true;}
                if(r.needle>=0&&r.turned==r.needle+1&&r.turnedAt>=0&&r.tick-r.turnedAt<=VALVE_DRIFT){
                    r.brakeBy=p.getUUID();r.brakeHeld=0;l.setBlock(pos,LastCrossingSite.fit(Kind.BRAKE,Direction.WEST,1),Block.UPDATE_CLIENTS);sound(l,pos,SoundEvents.IRON_DOOR_OPEN,.7F,.7F);
                    job(p,"gauge");cue(p,"You lean on the brake and hold it open. Hold it till the intake settles.");return true;}
                cue(p,"You lean on the brake. Nothing below is ready to take the load.");return true;}
            case PIPE->{knock(p,r,rel);return true;}
            case ROLLER->{
                if(LastCrossingDayThree.roller(p,rel,stage))return true;
                if(stage==0)return false;
                l.setBlock(pos,LastCrossingSite.fit(Kind.ROLLER,s.getValue(CrossingBlock.FACING)),Block.UPDATE_CLIENTS);sound(l,pos,SoundEvents.ANVIL_PLACE,.35F,1.4F);
                r.rollerSeated=true;mark(p,"SeatedRoller");job(p,"lookout");cue(p,"You heave the roller back into its cradle. The cable settles over it.");return true;}
            case HATCH->{if(LastCrossingNightTwo.hatch(p))return true;mark(p,"HatchTried");job(p,"lookout");sound(l,pos,SoundEvents.IRON_TRAPDOOR_CLOSE,.7F,.8F);
                cue(p,"The hatch lifts an inch on black water and drops back. Under the rollers the passage is flooded to the roof.");return true;}
            case FAIRLEAD->{
                if(LastCrossingNightTwo.fairlead(p,stage))return true;
                if(stage!=0)return false;var own=own(p);own.putBoolean("SawJam",true);own.putBoolean("Fairlead",true);save(p,own);job(p,"lookout");
                cue(p,"The cable's jumped the fairlead and kinked against the housing. Under it the fairlead's open to the water: someone below could reach it.");return true;}
            case WHEEL->{
                if(r.pinSeated){cue(p,"It's pinned. It won't turn now.");return true;}
                LastCrossingDayThree.wheel(p);
                boolean first=!own(p).getBoolean("CheckedWheel");mark(p,"CheckedWheel");job(p,"landing");
                r.wheelBy=p.getUUID();r.wheelUntil=r.tick+WHEEL_HOLD;l.setBlock(pos,LastCrossingSite.fit(Kind.WHEEL,Direction.SOUTH,1),Block.UPDATE_CLIENTS);
                cue(p,first?"The wheel's stiff, but it turns. It wants holding shut while someone pins it from above.":"You lean on the wheel and hold it shut.");return true;}
            case PIN->{return LastCrossingDayThree.pin(p,stage)||pin(p,r,pos);}
            case COIL->{
                if(LastCrossingNight.holdLine(p,rel,stage))return true;
                if(rel.equals(LastCrossingSite.RAMP_COIL)&&stage==0){
                    if(!give(p,item(CrossingRegistry.LINE.get(),"line",null))){cue(p,"Your hands are full.");return true;}
                    l.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);r.lineIn=true;mark(p,"LineIn");job(p,"landing");
                    cue(p,"You haul the loose line in, hand over hand, and coil it over your shoulder.");return true;}
                if(rel.equals(LastCrossingSite.PORCH_COIL)&&stage==1&&!r.lineStaged){
                    if(!give(p,item(CrossingRegistry.LINE.get(),"line",null))){cue(p,"Your hands are full.");return true;}
                    l.setBlock(pos,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);mark(p,"TookLineToStage");
                    cue(p,"You take the coiled line to stage it at the head of the ramp.");return true;}
                return false;}
            case HOOK->{
                if(stage==1){if(!give(p,item(CrossingRegistry.LAMP.get(),"lamp",null))){cue(p,"Your hands are full.");return true;}
                    l.setBlock(pos,LastCrossingSite.fit(Kind.HOOK,s.getValue(CrossingBlock.FACING),0),Block.UPDATE_CLIENTS);r.lampHome=false;mark(p,"TookLamp");
                    cue(p,"You lift the station lamp off its hook.");return true;}
                if(holds(p,"lamp")){drop(p,"lamp");l.setBlock(pos,LastCrossingSite.fit(Kind.HOOK,s.getValue(CrossingBlock.FACING),1),Block.UPDATE_CLIENTS);r.lampHome=true;
                    if(rel.equals(LastCrossingSite.STAIR_HOOK)){if(r.phase==DUSK)mark(p,"LampStair");cue(p,"You hang the lamp at the foot of the stair. It lights the way up.");}
                    else cue(p,"You hang the lamp back on its hook.");return true;}
                return false;}
            case BAR->{return LastCrossingNight.bar(p,rel,stage);}
            case ISOLATOR->{return LastCrossingNight.isolate(p,pos,stage);}
            case BYPASS->{return LastCrossingDay.bypass(p,rel,stage);}
            case CATCH->{return LastCrossingNightTwo.catchBlocked(p)||LastCrossingDayThree.catchHeld(p)||LastCrossingDay.catchUse(p,stage);}
            case WITNESS->{return LastCrossingDay.witness(p,stage);}
            case TOOLS->{return LastCrossingDay.takeTools(p,pos);}
            case FEED->{return LastCrossingDayThree.feed(p,rel,stage);}
            case GALLERY_DOOR->{return LastCrossingDayThree.door(p,stage);}
            case RACK->{return LastCrossingDayThree.rack(p,rel,stage);}
            case FEED_GATE->{return LastCrossingNightThree.gate(p,stage)||LastCrossingDayThree.gate(p,stage);}
            case DEBRIS->{return LastCrossingDayThree.debris(p,rel,stage);}
            case CUTOFF->{return LastCrossingNightThree.cutoff(p,stage)||LastCrossingDayThree.cutoff(p,stage);}
            case MASTER->{return LastCrossingNightThree.masterUse(p,stage)||LastCrossingDayThree.master(p,stage);}
            case RELEASE->{return LastCrossingNightThree.release(p)||LastCrossingDayThree.release(p,stage);}
            case DOWNSTREAM->{return LastCrossingNightThree.downstream(p,stage)||LastCrossingDayThree.downstream(p,stage);}
            default->{return false;}
        }
    }
    static boolean bleed(ServerPlayer p,Run r,int stage){
        var l=p.serverLevel();var at=r.base.offset(LastCrossingSite.BLEED);
        if(stage==0){
            if(!holds(p,"wrench")){cue(p,"The screw's too tight for fingers. There's a wrench on the pipe.");return true;}
            l.setBlock(at,LastCrossingSite.fit(Kind.BLEED,Direction.SOUTH,1),Block.UPDATE_CLIENTS);sound(l,at,CrossingRegistry.BLEED.get(),1F,1F);
            r.bleedOpen=true;if(r.intakeBy==null)r.intakeBy=p.getUUID();
            r.needle=Math.floorMod((int)(r.id^r.base.asLong())+r.crew.size(),3);
            l.setBlock(r.base.offset(LastCrossingSite.GAUGE),LastCrossingSite.fit(Kind.GAUGE,Direction.WEST,r.needle),Block.UPDATE_CLIENTS);
            mark(p,"OpenedBleed");job(p,"intake");
            cue(p,"You back the screw off a turn. Air hisses out, sputters, then water. Now the valve the gauge calls for.");return true;}
        // The intake worker may hand the job to someone near them before dark.
        if(!p.getUUID().equals(r.intakeBy)){cue(p,"The screw's open. Water spits from it in a thin thread.");return true;}
        var near=new ArrayList<ServerPlayer>();for(var q:readers(l,r.base))if(q!=p&&!q.isSpectator()&&q.distanceToSqr(p)<6*6)near.add(q);
        if(near.isEmpty()){cue(p,"The screw's open. The intake's yours until you hand it to someone.");return true;}
        begin(p,"The intake");Talk.line(p,"","The screw's open, water spitting from it. The intake's yours until you hand it to someone.");
        var replies=new ArrayList<Talk.Reply>();
        for(var q:near){var id=q.getUUID();replies.add(Talk.reply("Take the intake, "+name(q)+".",x->handIntake(x,id)));}
        replies.add(Talk.act("Keep it.",Talk::close));Talk.ask(p,replies);return true;
    }
    static void handIntake(ServerPlayer p,UUID to){
        Talk.close(p);var r=runOf(p);if(r==null||!p.getUUID().equals(r.intakeBy))return;var q=player(p.serverLevel(),to);if(q==null)return;
        r.intakeBy=to;var own=own(p);own.putString("HandedIntake",name(q));save(p,own);var theirs=own(q);theirs.putString("TookIntake",name(p));save(q,theirs);job(q,"intake");
        cue(q,name(p)+" hands you the intake.");
    }
    static boolean gauge(ServerPlayer p,Run r){
        if(!r.bleedOpen){cue(p,"The needle lies at nothing. Nothing will come up until the bleed screw's open below.");return true;}
        if(r.primed){cue(p,"The needle's settled. The pump's holding.");return true;}
        String[] band={"first","second","third"},call={"One!","Two!","Three!"};
        begin(p,"The gauge");Talk.line(p,"","The needle sits in the "+band[r.needle]+" band.");
        int n=r.needle;Talk.ask(p,Talk.reply("Call it down the pipe: “"+call[n]+"”",q->callValve(q,n)),Talk.act("Watch it.",Talk::close));
        return true;
    }
    static void callValve(ServerPlayer p,int band){
        Talk.close(p);var r=runOf(p);if(r==null)return;r.called=band+1;
        downPipe(p.serverLevel(),r,LastCrossingSite.PIPE_A_DOWN,name(p),new String[]{"One!","Two!","Three!"}[band]);
        mark(p,"GaugeRead");job(p,"gauge");
    }
    static boolean valve(ServerPlayer p,Run r,int stage){
        var l=p.serverLevel();if(stage>=3){cue(p,"A red bypass wheel. It won't turn.");return true;}
        if(r.primed){cue(p,"It's set. The pump's holding.");return true;}
        if(!r.bleedOpen){cue(p,"The bleed screw's still shut. Nothing will come up until the air's out.");return true;}
        int n=stage+1;r.turned=n;r.turnedAt=r.tick;r.turnedBy=p.getUUID();sound(l,r.base.offset(LastCrossingSite.VALVES.get(stage)),SoundEvents.IRON_TRAPDOOR_OPEN,.6F,.6F);
        mark(p,"TurnedValve");job(p,"intake");
        if(n==r.needle+1){cue(p,"The valve comes round. Somewhere above, the needle should be settling. Now somebody hold the brake open.");
            for(var q:readers(l,r.base))if(q.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(LastCrossingSite.GAUGE)))<5*5)cue(q,"The needle steadies in its band. Hold the brake open and it'll settle.");}
        else{mark(p,"WrongValve");cue(p,"The valve comes round. In the pipe something knocks, and the pump's note sours.");}
        return true;
    }
    /** A knock on a speaking pipe, heard at both its mouths; two in a row are a signal. */
    static void knock(ServerPlayer p,Run r,BlockPos rel){
        var l=p.serverLevel();BlockPos other;String key;
        if(rel.equals(LastCrossingSite.PIPE_A_UP)){other=LastCrossingSite.PIPE_A_DOWN;key="a_up";}
        else if(rel.equals(LastCrossingSite.PIPE_A_DOWN)){other=LastCrossingSite.PIPE_A_UP;key="a_down";}
        else if(rel.equals(LastCrossingSite.PIPE_B_UP)){other=LastCrossingSite.PIPE_B_DOWN;key="b_up";}
        else{other=LastCrossingSite.PIPE_B_DOWN.equals(rel)?LastCrossingSite.PIPE_B_UP:null;key="b_down";}
        if(other==null)return;
        var k=r.knocks.computeIfAbsent(key,x->new long[]{-100,0});k[1]=r.tick-k[0]<=KNOCK_GAP?k[1]+1:1;k[0]=r.tick;
        sound(l,r.base.offset(rel),CrossingRegistry.KNOCK.get(),.8F,1F);sound(l,r.base.offset(other),CrossingRegistry.KNOCK.get(),1F,.95F);
        mark(p,"Knocked");if(key.startsWith("b"))job(p,"landing");
        var at=Vec3.atCenterOf(r.base.offset(other));boolean two=k[1]>=2;
        if(two&&key.equals("b_down"))r.twoKnocksB=r.tick;
        for(var q:readers(l,r.base)){if(q.position().distanceToSqr(at)>5*5)continue;cue(q,two?"Two knocks in the pipe.":"A knock in the pipe.");if(two)mark(q,"HeardKnocks");}
    }
    /** The locking pin: it seats only while someone below holds the wheel shut; seated at the second knock, it's the signal they practised. */
    static boolean pin(ServerPlayer p,Run r,BlockPos pos){
        var l=p.serverLevel();
        if(r.pinSeated){cue(p,"The pin's seated.");return true;}
        boolean first=!own(p).getBoolean("CheckedPin");mark(p,"CheckedPin");job(p,"landing");
        if(r.wheelBy==null){cue(p,first?"The pin slides in its sleeve. It'll seat when someone below holds the wheel shut.":"The pin won't seat. Nobody's holding the wheel.");return true;}
        r.pinSeated=true;r.pinAt=r.tick;if(LastCrossingNightThree.active(r)){mark(p,"SeatedLaunchPin");r.night3.pinnedBy=p.getUUID();}
        l.setBlock(pos,LastCrossingSite.fit(Kind.PIN,Direction.SOUTH,1),Block.UPDATE_CLIENTS);l.setBlock(r.base.offset(LastCrossingSite.WHEEL),LastCrossingSite.fit(Kind.WHEEL,Direction.SOUTH,2),Block.UPDATE_CLIENTS);
        sound(l,pos,SoundEvents.IRON_TRAPDOOR_CLOSE,1F,.7F);
        var below=player(l,r.wheelBy);
        if(r.twoKnocksB>=0&&r.tick-r.twoKnocksB<=100){
            r.rehearsed=true;
            for(var who:new ServerPlayer[]{p,below}){if(who==null)continue;var own=own(who);own.putBoolean("Rehearsed",true);var other=who==p?below:p;if(other!=null&&other!=who)own.putString("RehearsedWith",name(other));save(who,own);
                cue(who,"The pin drops into its socket at the second knock. Below, the wheel stops fighting.");}
            if(actor(l,r.vera)!=null&&actor(l,r.vera).position().distanceToSqr(Vec3.atCenterOf(pos))<20*20)speak(l,r,LastCrossingTexts.REHEARSED);
        }else cue(p,"The pin drops into its socket. Nobody knocked.");
        return true;
    }

    /** Right-clicking Vera. */
    public static void talk(ServerPlayer p,LiteraryActor a){
        var r=runOf(p);if(r==null)return;var l=p.serverLevel();
        if(r.phase==GATHERING){speak(l,r,r.crew.containsKey(p.getUUID())?"We'll give the rest of you a minute.":"Sign the roll first. It's on the table.");return;}
        if(r.phase==ROLL){speak(l,r,"Wait for your name.");return;}
        if(r.phase>=DUSK){
            begin(p,"Vera");Talk.line(p,"Vera",r.phase==DUSK?"What is it?":r.phase==END?"You're across.":"You made it to the light, then.");
            var replies=new ArrayList<Talk.Reply>();
            if(r.phase==DUSK)replies.add(Talk.reply("What do we do?",q->{Talk.line(q,"Vera",r.dayNo>=3?LastCrossingTexts.NIGHT_ANSWER3:r.dayNo>=2?LastCrossingTexts.NIGHT_ANSWER2:LastCrossingTexts.NIGHT_ANSWER);Talk.end(q);}));
            if(own(p).getBoolean("Infected")&&!own(p).getBoolean("Told"))replies.add(Talk.reply("It got a claw in me.",q->{LastCrossingNight.told(q);Talk.line(q,"Vera",LastCrossingTexts.TOLD_ANSWER);Talk.end(q);}));
            replies.add(Talk.act("Leave her be.",Talk::close));Talk.ask(p,replies);return;}
        if(r.dayNo>=3){
            begin(p,"Vera");Talk.line(p,"Vera","What is it?");
            var replies=new ArrayList<Talk.Reply>();
            replies.add(Talk.reply("What needs doing?",q->{Talk.line(q,"Vera",LastCrossingTexts.DAY3_JOBS);Talk.end(q);}));
            if(own(p).getBoolean("AskedEllis"))replies.add(Talk.reply(LastCrossingTexts.CROSS_ASK,q->{mark(q,"AskedCross");cue(q,"Vera puts the clipboard down.");Talk.line(q,"Vera",LastCrossingTexts.CROSS_ANSWER);Talk.end(q);}));
            if(own(p).getBoolean("Infected")&&!own(p).getBoolean("Told"))replies.add(Talk.reply("It got a claw in me.",q->{LastCrossingNight.told(q);Talk.line(q,"Vera",LastCrossingTexts.TOLD_ANSWER);Talk.end(q);}));
            if(own(p).getBoolean("Turned"))replies.add(Talk.reply(LastCrossingTexts.LAST_NIGHT_ASK,q->{explained(q);Talk.line(q,"Vera",LastCrossingTexts.LAST_NIGHT_ANSWER);Talk.end(q);}));
            replies.add(Talk.act("Leave her be.",Talk::close));Talk.ask(p,replies);return;}
        if(r.dayNo>=2){
            begin(p,"Vera");Talk.line(p,"Vera","What is it?");
            var replies=new ArrayList<Talk.Reply>();
            replies.add(Talk.reply("What needs doing?",q->{Talk.line(q,"Vera",LastCrossingTexts.DAY2_JOBS);Talk.end(q);}));
            replies.add(Talk.reply(LastCrossingTexts.ADMIT_ASK,q->{mark(q,"VeraAdmitted");mark(q,"AskedEllis");Talk.line(q,"Vera",LastCrossingTexts.VERA_ADMITS);Talk.end(q);}));
            if(own(p).getBoolean("Infected")&&!own(p).getBoolean("Told"))replies.add(Talk.reply("It got a claw in me.",q->{LastCrossingNight.told(q);Talk.line(q,"Vera",LastCrossingTexts.TOLD_ANSWER);Talk.end(q);}));
            replies.add(Talk.act("Leave her be.",Talk::close));Talk.ask(p,replies);return;}
        begin(p,"Vera");Talk.line(p,"Vera","What is it?");
        var replies=new ArrayList<Talk.Reply>();
        replies.add(Talk.reply("What needs doing?",q->{Talk.line(q,"Vera",LastCrossingTexts.JOBS+(r.crew.size()>=5?" "+LastCrossingTexts.JOBS_FIVE:"")+(r.crew.size()>=6?" "+LastCrossingTexts.JOBS_SIX:""));Talk.end(q);}));
        replies.add(Talk.reply("Who's Ellis?",q->{mark(q,"AskedEllis");Talk.line(q,"Vera",LastCrossingTexts.ELLIS_ANSWER);Talk.end(q);}));
        replies.add(Talk.reply("Should someone go and look for him?",q->{mark(q,"AskedEllis");Talk.line(q,"Vera",LastCrossingTexts.LOOK_ANSWER);Talk.end(q);}));
        if(carries(p,"line")&&!r.lineReturned)replies.add(Talk.act("Hand her the line.",LastCrossing::returnLine));
        if(own(p).getBoolean("SawJam")&&!r.jamReported)replies.add(Talk.reply("The cable's jumped the fairlead.",q->{var rr=runOf(q);if(rr!=null)rr.jamReported=true;mark(q,"Reported");job(q,"lookout");Talk.line(q,"Vera",LastCrossingTexts.JAM_ANSWER);Talk.end(q);}));
        replies.add(Talk.act("Leave her be.",Talk::close));
        Talk.ask(p,replies);
    }
    /** The turned reader tells it, aloud, by Vera: whoever is near hears them. */
    static void explained(ServerPlayer p){
        var r=runOf(p);if(r==null)return;mark(p,"Explained");
        for(var q:readers(p.serverLevel(),r.base))if(q!=p&&q.distanceToSqr(p)<12*12)addName(q,"ExplainedBy",name(p));
    }
    static void returnLine(ServerPlayer p){
        var r=runOf(p);if(r==null||!carries(p,"line")||r.lineReturned){Talk.close(p);return;}var l=p.serverLevel();
        drop(p,"line");r.lineReturned=true;l.setBlock(r.base.offset(LastCrossingSite.PORCH_COIL),LastCrossingSite.fit(Kind.COIL,Direction.NORTH,1),Block.UPDATE_CLIENTS);
        mark(p,"LineReturned");job(p,"landing");Talk.line(p,"Vera",LastCrossingTexts.LINE_THANKS);Talk.end(p);
    }

    // ------------------------------------------------------------------------------------------------ events
    /** The station's line staged at the ramp; the lamp, the wrench or the line handed to another of the crew; the bell rung. */
    @SubscribeEvent public static void click(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getHand()!=InteractionHand.MAIN_HAND)return;var r=runOf(p);if(r==null)return;
        var rel=e.getPos().subtract(r.base);
        if(rel.equals(LastCrossingSite.BELL)){bell(p);return;}
        if(rel.equals(LastCrossingSite.WORK_BELL)){mark(p,"RangWorkBell");return;}
        if(p.serverLevel().getBlockState(e.getPos()).is(Blocks.IRON_BARS)&&LastCrossingNight.reachDown(p,rel)){e.setCanceled(true);return;}
        if(stageLine(p,rel))e.setCanceled(true);
    }
    /** Night One: the hand in the grate struck. */
    @SubscribeEvent public static void strike(PlayerInteractEvent.LeftClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START)return;var r=runOf(p);if(r==null)return;
        var s=p.serverLevel().getBlockState(e.getPos());if(!CrossingBlock.is(s,Kind.GRATE)||s.getValue(CrossingBlock.STAGE)!=2)return;
        if(LastCrossingNight.strike(p))e.setCanceled(true);
    }
    /** Night One: a clean reader cut by one of the things from the water. */
    @SubscribeEvent public static void cut(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){
        if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof Undertow&&runOf(p)!=null)LastCrossingNight.wounded(p);
    }
    /** The bell beside the ramp rung (it rings itself). */
    public static void bell(ServerPlayer p){if(runOf(p)==null)return;mark(p,"RangBell");job(p,"spillway");LastCrossingNightThree.bell(p);}
    /** The coiled line, carried back from Vera's porch, made fast at the head of the ramp for the night. */
    public static boolean stageLine(ServerPlayer p,BlockPos rel){
        var r=runOf(p);if(r==null||!holds(p,"line")||!(r.lineReturned||r.phase==DUSK)||r.lineStaged||!LastCrossingSite.atRamp(Vec3.atCenterOf(rel.above())))return false;
        drop(p,"line");r.lineStaged=true;r.lineReturned=true;p.serverLevel().setBlock(r.base.offset(LastCrossingSite.STAGED_COIL),LastCrossingSite.fit(Kind.COIL,Direction.NORTH,2),Block.UPDATE_CLIENTS);
        mark(p,"StagedLine");job(p,r.phase==DUSK?"landing":"spillway");cue(p,"You make the line fast on the cleat at the head of the ramp"+(r.phase==DUSK?".":", ready for the night."));
        LastCrossingNight.madeFast(p,r);return true;
    }
    @SubscribeEvent public static void hand(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof ServerPlayer q)||e.getHand()!=InteractionHand.MAIN_HAND)return;
        if(handTo(p,q))e.setCanceled(true);
    }
    /** The lamp, the wrench or the line in one reader's hand passed to another of the crew. */
    public static boolean handTo(ServerPlayer p,ServerPlayer q){
        var r=runOf(p);if(r==null||runOf(q)!=r)return false;
        var w=what(p.getMainHandItem());if(!List.of("lamp","wrench","line","tools","pin","lockbar").contains(w))return false;
        var stack=p.getMainHandItem().copy();
        if(!give(q,stack)){cue(p,name(q)+"'s hands are full.");return true;}
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        String thing=switch(w){case "lamp"->"the lamp";case "wrench"->"the wrench";case "tools"->"the cable tools";case "pin"->"the launch pin";case "lockbar"->"the locking bar";default->"the line";};
        cue(p,"You hand "+name(q)+" "+thing+".");cue(q,name(p)+" hands you "+thing+".");addName(p,"Handed",name(q));addName(q,"GotFrom",name(p));return true;
    }
    /** Nobody fights with the plate in their hands. */
    @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&holds(p,"plate")&&runOf(p)!=null){e.setCanceled(true);cue(p,"Your hands are full with the plate.");}}
    /** The plate is set down rather than thrown; the wrench dropped by the grate rings on it, and something taps back. */
    @SubscribeEvent public static void toss(ItemTossEvent e){
        if(!(e.getPlayer() instanceof ServerPlayer p))return;var w=what(e.getEntity().getItem());var r=runOf(p);if(r==null||w.isEmpty())return;
        if(w.equals("plate")){e.setCanceled(true);r.plateBy=p.getUUID();var l=p.serverLevel();p.getInventory().setItem(p.getInventory().selected,item(CrossingRegistry.PLATE.get(),"plate",null));putDownNear(l,r,p);cue(p,"You don't throw it. You set it down.");return;}
        if(w.equals("wrench")&&p.position().distanceToSqr(Vec3.atCenterOf(r.base.offset(LastCrossingSite.GRATE)))<3*3){r.tapAt=r.tick+20;var own=own(p);own.putBoolean("Tapped",true);own.putBoolean("BrokenCorner",true);save(p,own);
            cue(p,"The wrench rings on the grate. A moment later, something taps back.");}
    }
    @SubscribeEvent public static void died(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var r=runOf(p);
        if(r!=null&&r.crew.containsKey(p.getUUID())){r.dead.add(p.getUUID());var own=own(p);own.putBoolean("Died",true);if(r.phase==DUSK&&r.dayNo==1)own.putBoolean("DiedNightOne",true);if(r.phase==DUSK&&r.dayNo==2)own.putBoolean("DiedNightTwo",true);if(r.phase==DUSK&&r.dayNo>=3)own.putBoolean("DiedNightThree",true);if(r.phase==DAY&&r.dayNo>=3)own.putBoolean("DiedDayThree",true);save(p,own);}
        if(carries(p,"plate")){if(r!=null&&p.getUUID().equals(r.plateBy))putDownNear(p.serverLevel(),r,p);else drop(p,"plate");}
    }
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p&&carries(p,"plate")){var r=runOf(p);if(r!=null&&p.getUUID().equals(r.plateBy))putDownNear(p.serverLevel(),r,p);else drop(p,"plate");}}

    // ------------------------------------------------------------------------------------------------ the reset
    /** Nobody at the station: everything as it was in the morning, the roll blank, Vera back on her porch. */
    static void reset(ServerLevel l,Run r){
        var b=r.base;
        var area=new AABB(b.offset(LastCrossingSite.WEST,LastCrossingSite.LOW,LastCrossingSite.NORTH)).minmax(new AABB(b.offset(LastCrossingSite.EAST,LastCrossingSite.HIGH,1)));
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area,a->a.role()==LiteraryActor.STRANGER&&a.phase()==VERA))if(a.owner().isEmpty())a.discard();
        for(var e:l.getEntitiesOfClass(Display.ItemDisplay.class,area,e->e.getTags().contains("crossing_wrench")))e.discard();
        for(var e:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area,e->!what(e.getItem()).isEmpty()))e.discard();
        // Wherever the plate was set down, it is gone from there; it hangs in the office again.
        for(var at:BlockPos.betweenClosed(b.offset(LastCrossingSite.MOUND_W,-8,LastCrossingSite.FERRY_N),b.offset(LastCrossingSite.MOUND_E,4,LastCrossingSite.MOUND_S))){var s=l.getBlockState(at);
            if(CrossingBlock.is(s,Kind.PLATE_SET))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);else if(CrossingBlock.is(s,Kind.PLATE))l.setBlock(at,LastCrossingSite.fit(Kind.BRACKET,s.getValue(CrossingBlock.FACING)),Block.UPDATE_CLIENTS);}
        LastCrossingSite.living(l,b);
        var door=b.offset(LastCrossingSite.OFFICE_DOOR);var ds=l.getBlockState(door);if(ds.getBlock() instanceof DoorBlock)for(int y=0;y<=1;y++){var at=door.above(y);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock)l.setBlock(at,s.setValue(DoorBlock.OPEN,true),Block.UPDATE_CLIENTS);}
        r.vera=null;r.phase=GATHERING;r.crew.clear();r.answered.clear();r.calling=-1;r.callTicks=0;r.calledTwice=false;r.firstSigned=-1;r.answerAt=-1;r.day=0;
        r.bleedOpen=false;r.intakeBy=null;r.needle=-1;r.turned=0;r.turnedAt=-1;r.turnedBy=null;r.brakeBy=null;r.brakeHeld=0;r.primed=false;r.primedAt=-1;r.called=0;
        r.rollerSeated=false;r.jamReported=false;r.lineIn=false;r.lineReturned=false;r.lineStaged=false;r.wheelBy=null;r.pinSeated=false;r.pinAt=-1;r.twoKnocksB=-1;r.rehearsed=false;r.rehearsePrompted=false;r.rehearseAt=-1;
        r.knocks.clear();r.plateAt=LastCrossingSite.PLATE_HOME;r.plateBy=null;r.fogUntil=-1;r.lampHome=true;r.wrenchHome=true;r.wrenchDisplay=null;r.tapAt=-1;
        r.routeCalled=false;r.dusk=false;r.demo=0;r.kettle=false;r.route.clear();r.leg=0;r.legTicks=0;r.greeted.clear();r.current.clear();
        LastCrossingNight.reset(l,r);LastCrossingNightTwo.reset(l,r);LastCrossingDay.reset(l,r);LastCrossingDayThree.reset(l,r);LastCrossingNightThree.reset(l,r);r.dead.clear();
        var cable=b.offset(LastCrossingSite.CABLE_DOOR);for(int y=0;y<=1;y++){var at=cable.above(y);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN))l.setBlock(at,s.setValue(DoorBlock.OPEN,false),Block.UPDATE_CLIENTS);}
    }
}
