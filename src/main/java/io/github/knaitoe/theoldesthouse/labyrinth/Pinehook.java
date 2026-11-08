package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PinehookBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.PinehookTexts.Part;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.364, Last Weekend at Pinehook (docs/PINEHOOK_0_4_364.md), from the owner's proposal "Last Weekend at Pinehook": "A classic adult
 * cabin weekend with recognizable horror archetypes, escalating danger, an actual mystery, and branching character outcomes." Casey has
 * invited everyone to a cabin and arranged a fake slasher prank with Jude, an actor and friend. Len, who owns the cabin, let Casey have it
 * cheaply on the condition that Nina came. Len is blackmailing Nina; when she learns about the prank she sees a way to kill Len and leave
 * the apparent killer's mask with Jude.
 *
 * <p>This first slice is the proposal's own: "arrival, one fake attack, an early unmasking branch, and the real attempted attack on Jude."
 * Casey greets each reader on the porch and offers the parts openly; each part hands over its thing. The evening is the party being a
 * party: the cooler carried to the kitchen (and Casey's secret told to whoever carries it), the music, the argument about the film, the
 * dock, the burgers; Len walks up the lane and Nina goes quiet; a bag behind the shed holds a second strap of elastic; Jude's duffel holds
 * a prop knife. At dusk Casey trips the breaker and Jude, masked, knocks at the window and rattles the back door. A reader who gets to him
 * can pull the mask off, and the evening turns into an awkward talk with Casey instead. Either way Jude goes to the shed to change. Nina
 * goes after him. If someone is with Jude she waits behind the shed and gives up; if she goes in, a reader who gets into the shed while
 * she is at him stops her and sees who it was; otherwise Jude comes back cut and without the costume, asks Casey who else he hired, and a
 * masked figure crosses the far end of the dock.
 *
 * <p>0.4.365, the night (docs/PINEHOOK_0_4_365.md): the proposal's parallel scenes. Jude in the kitchen, Casey talking over him until
 * someone tells him to let Jude talk; what the dock shows (spots along the T, a coverall's tie, wet prints up to the back step, the rowboat
 * moved with the costume under its tarp); Len locked in his boathouse, talking through the door; Casey's plan in his bag, which he burns
 * himself if nobody finds it first; the portable player, set down to record; and telling Casey what was found until he stops calling it
 * a bit. The rest is the next slices'.
 *
 * <p>One shared scene for everyone; each reader's own record keeps the part they played, what they did, saw, said and were told, for the
 * accounts the last slice writes. Nothing here grants Witness credit. Not dealt until the weekend can be finished; the operator's test
 * door reaches it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Pinehook {
    private Pinehook(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PINEHOOK;
    public static final String TALK="pinehook";
    /** The weekend's evening: arriving and taking parts; the party; the fake attack; the shed; the turn; the night held for the next slice. */
    public static final int ARRIVAL=0,EVENING=1,SCARE=2,SHED=3,TURN=4,HOLD=5,NIGHT=6;
    /** 0.4.365: the night begins this long into the hold; Casey goes for his plan this long into the night; Nina's wet return reaches the
     * back door this long after the splash; a player set down hears this far, and loud things from further. */
    public static final int NIGHT_AT=300,BURN_AT=2400,NINA_IN_DELAY=160,CASEY_FOLLOW=40,HEAR=14,HEAR_LOUD=28;
    /** What happened in the shed: nothing yet; Jude cut and the costume taken; the attack interrupted and seen; Nina never got in. */
    public static final int NONE=0,WOUNDED=1,WITNESSED=2,PREVENTED=3;
    /** Ticks: the wait for the rest after the first part is taken; Len's walk up the lane; the earliest and latest dusk; the burgers; the
     * steps of the scare; Jude changing; Nina's wait before she goes; her lurking; the struggle; the turn's lines; an empty cabin's reset. */
    public static final int GATHER_WAIT=600,LEN_AT=1800,DUSK_EARLY=3600,DUSK_LATE=6000,COOK=600,WINDOW_AT=60,BACK_AT=200,RETREAT_AT=340,CHANGE=600,NINA_AFTER=160,
        LURK_MAX=600,STRUGGLE=100,JUDE_GOES=400,LINE_GAP=60,FIGURE_AT=120,NINA_BACK=500,EMPTY_RESET=200;
    /** The cast's skins, on the stranger phases after Vera's: Casey, Jude in his own clothes, the costume masked, the costume with the
     * mask off, Nina, Len. */
    public static final int CASEY=117,JUDE=118,MASKED=119,COVERALL=120,NINA=121,LEN=122;
    public static boolean cast(int phase){return phase>=CASEY&&phase<=LEN;}
    public static String skin(int phase){return switch(phase){case CASEY->"pinehook_casey";case JUDE->"pinehook_jude";case MASKED->"pinehook_masked";case COVERALL->"pinehook_coverall";case NINA->"pinehook_nina";default->"pinehook_len";};}
    /** Gestures for the cast's poses (LiteraryRenderers): knocking on glass; holding a side, hurt; a hand held low with something in it. */
    public static final int KNOCKING=11,HURT=12,KNIFE=13;
    static final String ITEM_TAG="Pinehook";

    static final class Walker {final List<Vec3> route=new ArrayList<>();int leg,ticks;double speed=.12;}
    static final class Run {
        final BlockPos base;final long id;long tick;int empty,phase=ARRIVAL;boolean fresh=true;
        UUID casey,jude,nina,len;final Map<UUID,Walker> walkers=new HashMap<>();
        long firstPart=-1;int evening;boolean lenHere,lenWalking,ninaQuiet;int lenAt=-1;
        /** The evening's things: the cooler (on the porch, carried, or in the kitchen); the music; the film; the burgers. */
        String cooler="porch";@Nullable UUID coolerBy;boolean music,secretTold;int film,food;long foodAt=-1;
        /** The scare: its tick, whether the mask came off and by whom, who saw it. */
        int scare=-1,unmaskedAt=-1,judeLeft=-1;boolean unmasked,powerOff;@Nullable UUID unmaskedBy;
        /** The shed: its tick, Jude in it, Nina's progress, the struggle, the outcome. */
        int shed=-1,judeIn=-1,ninaStep,lurk,struggle=-1,outcome=NONE;
        /** The turn: its tick and the figure on the dock. */
        int turn=-1,turnWait;boolean figureCrossed,ninaBack;int hold=-1;
        boolean bandaged;
        final Map<UUID,Long> torches=new HashMap<>();
        /** 0.4.365, the night: its tick; whether Casey was told to let Jude talk; Len inside his boathouse; the plan taken, burned, in
         * Casey's hands; what Casey has been told and whether he has turned; his walk to burn the plan; the costume and the tie taken; the
         * spare player found; when Nina's wet return reaches the back door; the shed's back window broken. */
        int night=-1;boolean hushed,lenInside,planTaken,planBurned,caseyHasPlan,caseyTurned,costumeTaken,tieTaken,sparePlayer,evidence,shedWindowBroken;
        int caseyKnows,burnStep;long ninaInAt=-1;
        final List<Event> events=new ArrayList<>();final Map<Long,Rec> recorders=new HashMap<>();Set<UUID> present=new HashSet<>();
        Run(BlockPos b,long id){base=b;this.id=id;}
    }
    /** Something a set-down player could have heard: when, where and what. */
    record Event(long tick,BlockPos at,String key){}
    /** A player set down and recording: whose, and since when. */
    record Rec(UUID owner,long start){}
    private static final Map<Long,Run> RUNS=new HashMap<>();
    public static void clearAll(){RUNS.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Run run(BlockPos b){return RUNS.get(b.asLong());}
    public static int phase(BlockPos b){var r=run(b);return r==null?-1:r.phase;}
    public static int outcome(BlockPos b){var r=run(b);return r==null?-1:r.outcome;}
    public static boolean flag(BlockPos b,String name){var r=run(b);if(r==null)return false;return switch(name){case "len"->r.lenHere;case "music"->r.music;case "secret"->r.secretTold;case "unmasked"->r.unmasked;
        case "power"->!r.powerOff;case "figure"->r.figureCrossed;case "ninaBack"->r.ninaBack;case "judeIn"->r.judeIn>=0;case "bandaged"->r.bandaged;case "quiet"->r.ninaQuiet;
        case "hushed"->r.hushed;case "turned"->r.caseyTurned;case "burned"->r.planBurned;case "planTaken"->r.planTaken;case "lenInside"->r.lenInside;case "costume"->r.costumeTaken;case "evidence"->r.evidence;default->false;};}
    public static int caseyKnows(BlockPos b){var r=run(b);return r==null?-1:r.caseyKnows;}
    public static int burnStep(BlockPos b){var r=run(b);return r==null?-1:r.burnStep;}
    public static String cooler(BlockPos b){var r=run(b);return r==null?"":r.cooler;}
    public static int film(BlockPos b){var r=run(b);return r==null?-1:r.film;}
    public static int ninaStep(BlockPos b){var r=run(b);return r==null?-1:r.ninaStep;}
    @Nullable public static Entity actor(ServerLevel l,BlockPos b,String who){var r=run(b);if(r==null)return null;return actor(l,switch(who){case "casey"->r.casey;case "jude"->r.jude;case "nina"->r.nina;default->r.len;});}
    /** For tests: the clock moved on by this many ticks in whatever is counting now. */
    public static void skip(BlockPos b,int ticks){var r=run(b);if(r==null)return;switch(r.phase){
        case ARRIVAL->{if(r.firstPart>=0)r.firstPart-=ticks;}
        case EVENING->{r.evening+=ticks;if(r.foodAt>=0)r.foodAt-=ticks;}
        case SCARE->r.scare+=ticks;
        case SHED->{r.shed+=ticks;if(r.judeIn>=0)r.judeIn+=ticks;if(r.struggle>=0)r.struggle+=ticks;r.lurk+=ticks;}
        case TURN->r.turn+=ticks;
        case HOLD->r.hold+=ticks;
        case NIGHT->r.night+=ticks;
        default->{}
    }}
    public static void resetNow(ServerLevel l,BlockPos b){var r=run(b);if(r!=null){reset(l,r);RUNS.remove(b.asLong());}}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,BlockPos at,SoundEvent e,float volume,float pitch){l.playSound(null,at,e,SoundSource.BLOCKS,volume,pitch);}
    static String name(ServerPlayer p){return p.getGameProfile().getName();}
    static void mark(ServerPlayer p,String key){var own=own(p);if(own.getBoolean(key))return;own.putBoolean(key,true);save(p,own);}
    static void note(ServerPlayer p,String key,String value){var own=own(p);own.putString(key,value);save(p,own);}
    /** One of the cast says something: over their head, and on the action bar of every reader in earshot. */
    static void speak(ServerLevel l,Run r,@Nullable UUID who,String speaker,String line,double reach){
        var a=actor(l,who);if(a==null)return;a.say(line);a.speaks(line);
        for(var p:readers(l,r.base))if(p.position().distanceToSqr(a.position())<reach*reach)p.displayClientMessage(Component.literal(speaker+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static void speak(ServerLevel l,Run r,@Nullable UUID who,String speaker,String line){speak(l,r,who,speaker,line,16);}

    // ------------------------------------------------------------------------------------------------ who is where
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    static List<ServerPlayer> living(ServerLevel l,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var p:readers(l,b))if(!p.isSpectator()&&p.isAlive())out.add(p);return out;}
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    @Nullable static Run runOf(ServerPlayer p){if(LiteraryVignettes.current(p)!=PLACE)return null;var b=LiteraryVignettes.base(p,PLACE);return b==null?null:run(b);}
    /** Whether a reader could see a point: in reach, before their eyes, and nothing solid in the way. */
    static boolean sees(ServerPlayer p,Vec3 point,double reach,double cone){
        var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();if(d>reach)return false;if(d<.8)return true;
        if(p.getLookAngle().dot(to.scale(1/d))<cone)return false;
        var l=p.level();int steps=(int)(d/.2);
        for(int i=1;i<steps;i++){var at=eye.add(to.scale(i/(double)steps));var st=l.getBlockState(BlockPos.containing(at));
            if(st.isAir()||st.is(PinehookRegistry.FITTING.get())||!st.canOcclude())continue;return false;}
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the weekend's things
    static ItemStack item(Item kind,String what,String name){
        var s=new ItemStack(kind);var tag=new CompoundTag();tag.putString(ITEM_TAG,what);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        s.set(DataComponents.CUSTOM_NAME,Component.literal(name).withStyle(ChatFormatting.RESET));return s;
    }
    static String what(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?"":c.copyTag().getString(ITEM_TAG);}
    static boolean carries(ServerPlayer p,String what){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(what(inv.getItem(i)).equals(what))return true;return false;}
    static void take(ServerPlayer p,String what){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(what(inv.getItem(i)).equals(what)){inv.setItem(i,ItemStack.EMPTY);return;}}
    static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
    static ItemStack thing(String what){return switch(what){
        case "cooler"->item(PinehookRegistry.COOLER.get(),"cooler","Cooler");
        case "camera"->item(PinehookRegistry.CAMERA.get(),"camera","Camera");
        case "torch"->item(PinehookRegistry.TORCH.get(),"torch","Torch");
        case "mixtape"->item(PinehookRegistry.MIXTAPE.get(),"mixtape","Mixtape");
        case "player"->item(PinehookRegistry.PLAYER.get(),"player","Portable player");
        case "tape"->item(PinehookRegistry.TAPE.get(),"tape","Rented film");
        case "firstaid"->item(PinehookRegistry.FIRSTAID.get(),"firstaid","First-aid bag");
        case "kit"->item(PinehookRegistry.FIRSTAID.get(),"firstaid","First-aid kit");
        case "tie"->item(PinehookRegistry.TIE.get(),"tie","Coverall tie");
        case "costume"->item(PinehookRegistry.COSTUME.get(),"costume","Mask and coverall");
        case "plan"->item(PinehookRegistry.PLAN.get(),"plan","Casey's plan");
        default->item(PinehookRegistry.ELASTIC.get(),"elastic","Elastic strap");
    };}
    /** Every weekend thing a reader carries out of the cabin's world stays here: taken when they leave the scene. */
    static final List<String> THINGS=List.of("cooler","camera","torch","mixtape","player","tape","firstaid","elastic","tie","costume","plan");

    // ------------------------------------------------------------------------------------------------ arriving
    static final List<String> RUN_KEYS=List.of("Part","Greeted","Partway","Rejoined","CarriedCooler","SetCooler","KeptSecret","Objected","AskedJudeWho","PlayedMusic","Music","ChoseFilm","Film",
        "WalkedDock","SawBoathouse","StartedFood","ServedFood","SawNinaQuiet","AskedNina","SatWithNina","AskedNinaBefore","AskedJude","MockedJude","AskedOwner","TookLenSeriously","DismissedLen",
        "PredictedPower","CalledIt","FoundElastic","SawPropBlade","SawMask","SawRubber","PhotographedMask","Photographed","Unmasked","SawUnmasking","AskedToldUs","AskedPower","PraisedBit",
        "WithJude","SawNinaLurking","SawNinaEnter","HeardCrash","InterruptedNina","WitnessedAttack","SawNinaFlee","HeardTurn","SawFigure","PhotographedFigure","HeardNinaBack","HeardJudeHurt",
        "HelpedJude","Outcome","SawJudeHurt","InScare","SawBreaker","SawJudeChange","HeardJudeLeave",
        "HeardLenGo","CaseyHushed","HeardJudeAccount","StrapMismatch","HeardCaseyToldNina","HeardJudeSawNina","AskedCaseyWho","AskedCaseyPlan","KnowsPlanWhere","GavePlanBack",
        "SawWetPrints","SawBeachPrints","SawShedPrints","SawPrints","SawDockBlood","SawShedBlood","FoundTie","SawBoatMoved","FoundCostume","TookCostume","SawBrokenWindow",
        "TookPlan","ReadPlan","BurnedPlan","SawCaseyBurn","TurnedCasey","HeardCaseyTurn","ToldPRINTS","ToldBLOOD","ToldPLAN","ToldSTRAP","ToldCOSTUME","ToldTAPE",
        "KnockedLen","LenSlipped","SawSafe","AskedLenWho","WatchLen","NinaDenied","ConfrontedNina","AskedNinaBoathouse","SetPlayer","RecordedTape","HeardTape","TapeNina",
        "PhotoBlood","PhotoPrints","PhotoTie","PhotoBoat","CaseyAskedPlan","AskedLenSafe");
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var l=p.serverLevel();var r=RUNS.computeIfAbsent(b.asLong(),k->new Run(b,l.getGameTime()));
        if(own.getLong("PinehookRun")!=r.id){for(var key:RUN_KEYS)own.remove(key);own.putLong("PinehookRun",r.id);}
        else if(own.contains("Part"))own.putBoolean("Rejoined",true);
        if(r.phase>=EVENING&&!own.getBoolean("Greeted"))own.putBoolean("Partway",true);
        cue(p,"A woodshed at the end of a gravel drive. Past it, through the pines, a cabin with its porch lit, and a lake going dark behind it.");
    }
    /** 0.4.364: not dealt until the weekend can be finished. */
    public static boolean dealable(LabyrinthData d,UUID p){return false;}

    // ------------------------------------------------------------------------------------------------ the clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(RUNS.isEmpty())return;var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var r:List.copyOf(RUNS.values())){
            r.tick++;var here=readers(l,r.base);
            if(here.isEmpty()){if(++r.empty>=EMPTY_RESET&&l.isLoaded(r.base.offset(PinehookSite.STOVE))){reset(l,r);RUNS.remove(r.base.asLong());}continue;}
            r.empty=0;
            var anchor=r.base.offset(PinehookSite.STOVE);
            if(!l.isLoaded(anchor)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(anchor)))continue;
            if(r.fresh){r.fresh=false;reset(l,r);}
            ensureCast(l,r);
            if(r.tick%10==0)leavers(l,r,here);
            if(r.ninaInAt>=0&&r.tick>=r.ninaInAt){r.ninaInAt=-1;log(r,PinehookSite.BACK_DOOR,"nina_in");}
            for(var id:Arrays.asList(r.casey,r.jude,r.nina,r.len))walk(l,r,id);
            switch(r.phase){
                case ARRIVAL->arrival(l,r,here);
                case EVENING->evening(l,r,here);
                case SCARE->scare(l,r,here);
                case SHED->shed(l,r,here);
                case TURN->turn(l,r,here);
                case HOLD->hold(l,r,here);
                case NIGHT->night(l,r,here);
                default->{}
            }
            greet(l,r,here);
            carry(l,r,here);
            torches(l,r,here);
            dock(l,r,here);
            idle(l,r,here);
        }
    }

    // ------------------------------------------------------------------------------------------------ the cast
    static void ensureCast(ServerLevel l,Run r){
        if(actor(l,r.casey)==null)r.casey=spawn(l,r,CASEY,PinehookSite.CASEY_PORCH,180);
        if(actor(l,r.jude)==null)r.jude=spawn(l,r,JUDE,PinehookSite.JUDE_PORCH,180);
        if(actor(l,r.nina)==null)r.nina=spawn(l,r,NINA,PinehookSite.NINA_LIVING,90);
        if(actor(l,r.len)==null&&r.lenHere)r.len=spawn(l,r,LEN,r.lenInside?PinehookSite.BOAT_INSIDE:PinehookSite.LEN_PORCH,180);
    }
    @Nullable static UUID spawn(ServerLevel l,Run r,int phase,Vec3 at,float yaw){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,phase);place(a,r.base,at,yaw);
        return l.addFreshEntity(a)?a.getUUID():null;
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 rel,float yaw){a.moveTo(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static void skinOf(ServerLevel l,@Nullable UUID id,int phase){var a=actor(l,id);if(a!=null&&a.phase()!=phase)a.appearance(LiteraryActor.STRANGER,phase);}
    static void gesture(ServerLevel l,@Nullable UUID id,int g){var a=actor(l,id);if(a!=null&&a.gesture()!=g)a.gesture(g);}
    static void hide(ServerLevel l,@Nullable UUID id,boolean hidden){var a=actor(l,id);if(a!=null)a.setInvisible(hidden);}
    static void moveTo(ServerLevel l,Run r,@Nullable UUID id,Vec3 at,float yaw){var a=actor(l,id);if(a!=null){place(a,r.base,at,yaw);var w=r.walkers.get(id);if(w!=null)w.route.clear();}}
    /** Sends one of the cast along points (relative to the base). */
    static void route(Run r,@Nullable UUID id,double speed,Vec3... points){if(id==null)return;var w=new Walker();w.route.addAll(List.of(points));w.speed=speed;r.walkers.put(id,w);}
    static boolean walking(Run r,@Nullable UUID id){var w=r.walkers.get(id);return w!=null&&w.leg<w.route.size();}
    static void walk(ServerLevel l,Run r,@Nullable UUID id){
        var a=actor(l,id);if(a==null)return;var w=r.walkers.get(id);
        if(w==null||w.leg>=w.route.size()){if(!a.isInvisible()&&a.gesture()!=KNOCKING){var near=nearest(l,r,a.position(),10);if(near!=null)SceneFootpaths.face(a,near.getEyePosition(),20);}return;}
        var target=world(r.base,w.route.get(w.leg));
        openDoors(l,r,a.position(),id);
        if(SceneFootpaths.walk(a,target,w.speed)||a.position().distanceToSqr(target)<.09||++w.ticks>200){if(w.ticks>200)a.moveTo(target.x,target.y,target.z,a.getYRot(),0);w.leg++;w.ticks=0;}
    }
    /** A door one of the cast walks up to opens for them; doors they have passed shut again behind them. */
    static void openDoors(ServerLevel l,Run r,Vec3 at,@Nullable UUID who){
        for(var d:List.of(PinehookSite.FRONT_DOOR,PinehookSite.BACK_DOOR,PinehookSite.SHED_DOOR,PinehookSite.BATH_DOOR,PinehookSite.BED_DOOR)){
            var pos=r.base.offset(d);if(Vec3.atBottomCenterOf(pos).distanceToSqr(at)>2.6)continue;
            var st=l.getBlockState(pos);if(st.getBlock() instanceof DoorBlock door&&!st.getValue(DoorBlock.OPEN)){
                door.setOpen(null,l,st,pos,true);
                if(d.equals(PinehookSite.BACK_DOOR))log(r,d,who!=null&&who.equals(r.nina)?"back_door_nina":"back_door");
            }
        }
    }
    /** Something a set-down player could hear. */
    static void log(Run r,BlockPos rel,String key){r.events.add(new Event(r.tick,rel,key));if(r.events.size()>200)r.events.remove(0);}
    @Nullable static ServerPlayer nearest(ServerLevel l,Run r,Vec3 from,double reach){ServerPlayer best=null;double d=reach*reach;for(var p:living(l,r.base)){double q=p.position().distanceToSqr(from);if(q<d){d=q;best=p;}}return best;}

    // ------------------------------------------------------------------------------------------------ setting the cabin back
    static void reset(ServerLevel l,Run r){
        for(var id:new UUID[]{r.casey,r.jude,r.nina,r.len}){var a=actor(l,id);if(a!=null)a.discard();}
        r.casey=r.jude=r.nina=r.len=null;r.walkers.clear();
        var b=r.base;
        PinehookSite.lights(l,b,true);
        set(l,b,PinehookSite.STEREO,PinehookBlock.of(Kind.STEREO,Direction.SOUTH,0));set(l,b,PinehookSite.TV,PinehookBlock.of(Kind.TV,Direction.WEST,0));
        set(l,b,PinehookSite.BREAKER,PinehookBlock.of(Kind.BREAKER,Direction.SOUTH,0));set(l,b,PinehookSite.STOVE,PinehookBlock.of(Kind.STOVE,Direction.WEST,0));
        set(l,b,PinehookSite.DUFFEL,PinehookBlock.of(Kind.DUFFEL,Direction.EAST,0));set(l,b,PinehookSite.BAG,PinehookBlock.of(Kind.BAG,Direction.SOUTH,0));
        set(l,b,PinehookSite.KIT,PinehookBlock.of(Kind.KIT,Direction.WEST,0));
        set(l,b,PinehookSite.COOLER_START,PinehookBlock.of(Kind.COOLER,Direction.SOUTH,0));set(l,b,PinehookSite.COOLER_SPOT,Blocks.AIR.defaultBlockState());
        for(var d:List.of(PinehookSite.FRONT_DOOR,PinehookSite.BACK_DOOR,PinehookSite.SHED_DOOR,PinehookSite.BATH_DOOR,PinehookSite.BED_DOOR)){var pos=b.offset(d);var st=l.getBlockState(pos);if(st.getBlock() instanceof DoorBlock door&&st.getValue(DoorBlock.OPEN))door.setOpen(null,l,st,pos,false);}
        for(var cell:r.torches.values()){var at=BlockPos.of(cell);if(l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
        r.torches.clear();
        stopMusic(l,r);
        // The night's evidence and the boat, the bag, the safe, the shed's window and any player left recording.
        var decals=new ArrayList<BlockPos>();decals.addAll(PinehookSite.DOCK_BLOOD);decals.addAll(PinehookSite.WET_PRINTS);decals.addAll(PinehookSite.BEACH_PRINTS);decals.addAll(PinehookSite.SHED_PRINTS);
        decals.add(PinehookSite.DOCK_TIE);decals.add(PinehookSite.SHED_BLOOD);
        for(var rel:decals){var st=at(l,r,rel);if(PinehookBlock.is(st,Kind.PRINTS)||PinehookBlock.is(st,Kind.BLOOD)||PinehookBlock.is(st,Kind.TIE))set(l,b,rel,Blocks.AIR.defaultBlockState());}
        PinehookSite.moorBoat(l,b,true,0);
        set(l,b,PinehookSite.SUITCASE,PinehookBlock.of(Kind.SUITCASE,Direction.EAST,0));set(l,b,PinehookSite.SAFE,PinehookBlock.of(Kind.SAFE,Direction.WEST,0));
        set(l,b,PinehookSite.SHED_BACK,Blocks.GLASS_PANE.defaultBlockState());
        for(var key:r.recorders.keySet()){var rel=BlockPos.of(key);if(PinehookBlock.is(at(l,r,rel),Kind.RECORDER))set(l,b,rel,Blocks.AIR.defaultBlockState());}
        r.recorders.clear();r.events.clear();
        r.night=-1;r.hushed=r.lenInside=r.planTaken=r.planBurned=r.caseyHasPlan=r.caseyTurned=r.costumeTaken=r.tieTaken=r.sparePlayer=r.evidence=r.shedWindowBroken=false;
        r.caseyKnows=r.burnStep=0;r.ninaInAt=-1;
        r.phase=ARRIVAL;r.firstPart=-1;r.evening=0;r.lenHere=r.lenWalking=r.ninaQuiet=false;r.cooler="porch";r.coolerBy=null;r.music=r.secretTold=false;r.film=0;r.food=0;r.foodAt=-1;
        r.scare=r.unmaskedAt=r.judeLeft=-1;r.turnWait=0;r.unmasked=r.powerOff=false;r.unmaskedBy=null;r.shed=r.judeIn=r.struggle=r.turn=r.hold=-1;r.ninaStep=r.lurk=0;r.outcome=NONE;r.figureCrossed=r.ninaBack=r.bandaged=false;
    }
    static void set(ServerLevel l,BlockPos b,BlockPos rel,BlockState s){l.setBlock(b.offset(rel),s,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
    static BlockState at(ServerLevel l,Run r,BlockPos rel){return l.getBlockState(r.base.offset(rel));}
    static void stage(ServerLevel l,Run r,BlockPos rel,int stage){var st=at(l,r,rel);if(st.is(PinehookRegistry.FITTING.get())&&st.getValue(PinehookBlock.STAGE)!=stage)set(l,r.base,rel,st.setValue(PinehookBlock.STAGE,stage));}

    // ------------------------------------------------------------------------------------------------ arrival: Casey and the parts
    /** Casey greets each reader who comes up to the porch, once; the parts are offered in the panel. */
    static void greet(ServerLevel l,Run r,List<ServerPlayer> here){
        var casey=actor(l,r.casey);if(casey==null||r.phase>=SCARE)return;
        for(var p:here){
            if(p.isSpectator()||own(p).getBoolean("Greeted")||Talk.waiting(p))continue;
            if(p.position().distanceToSqr(casey.position())>6*6)continue;
            var own=own(p);own.putBoolean("Greeted",true);save(p,own);
            offerParts(p,r.phase>=EVENING?PinehookTexts.GREET_LATE:PinehookTexts.GREET);
        }
    }
    static void offerParts(ServerPlayer p,String opening){
        Talk.begin(p,TALK,"Casey");Talk.line(p,"Casey",opening);
        var replies=new ArrayList<Talk.Reply>();
        for(var part:Part.values())replies.add(Talk.reply(part.reply,q->takePart(q,part)));
        replies.add(Talk.reply(PinehookTexts.NO_PART,q->{note(q,"Part","NONE");Talk.line(q,"Casey",PinehookTexts.NO_PART_ANSWER);Talk.end(q);started(q);}));
        Talk.ask(p,replies);
    }
    static void takePart(ServerPlayer p,Part part){
        note(p,"Part",part.name());Talk.line(p,"Casey",part.hand);Talk.end(p);
        switch(part){
            case SKEPTIC->{give(p,thing("camera"));give(p,thing("torch"));}
            case CHARMER->give(p,thing("mixtape"));
            case WILDCARD->give(p,thing("player"));
            case FAN->give(p,thing("tape"));
            case RESPONSIBLE->give(p,thing("firstaid"));
            default->{}
        }
        started(p);
    }
    static void started(ServerPlayer p){var r=runOf(p);if(r!=null&&r.firstPart<0)r.firstPart=r.tick;}
    static void arrival(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.firstPart<0)return;
        boolean all=true;for(var p:here)if(!p.isSpectator()&&!own(p).contains("Part"))all=false;
        if(all||r.tick-r.firstPart>=GATHER_WAIT){r.phase=EVENING;r.evening=0;}
    }

    // ------------------------------------------------------------------------------------------------ the evening
    static void evening(ServerLevel l,Run r,List<ServerPlayer> here){
        r.evening++;
        if(r.evening>=LEN_AT&&!r.lenHere){r.lenHere=true;r.lenWalking=true;r.len=spawn(l,r,LEN,PinehookSite.LEN_GATE,90);route(r,r.len,.11,PinehookSite.LEN_WALK);}
        if(r.lenWalking&&r.len!=null&&!walking(r,r.len)){r.lenWalking=false;lenArrives(l,r);}
        if(r.foodAt>=0&&r.tick>=r.foodAt){r.foodAt=-1;r.food=2;stage(l,r,PinehookSite.STOVE,2);sound(l,r.base.offset(PinehookSite.STOVE),SoundEvents.CAMPFIRE_CRACKLE,.6F,1.2F);}
        if(r.food==1&&r.tick%10==0){var s=r.base.offset(PinehookSite.STOVE);l.sendParticles(ParticleTypes.SMOKE,s.getX()+.5,s.getY()+1.05,s.getZ()+.5,1,.15,.02,.15,.005);}
        // Dusk: once the cabin has settled in (the cooler in, something playing, the film chosen) or when the evening runs out.
        boolean settled="kitchen".equals(r.cooler)&&r.music&&r.film>0&&r.lenHere;
        // Before dusk Jude slips away to put the costume on; the scare begins once he's had time.
        if(r.judeLeft<0&&(r.evening>=DUSK_LATE-JUDE_GOES||settled&&r.evening>=DUSK_EARLY-JUDE_GOES)){
            r.judeLeft=r.evening;speak(l,r,r.jude,"Jude",PinehookTexts.JUDE_LAKE,14);route(r,r.jude,.12,PinehookSite.FRONT_TO_SHED);
            var jude=actor(l,r.jude);for(var p:living(l,r.base))if(jude!=null&&p.distanceTo(jude)<14)mark(p,"HeardJudeLeave");}
        if(r.judeLeft>=0&&r.evening-r.judeLeft>=JUDE_GOES)beginScare(l,r,here);
    }
    static void lenArrives(ServerLevel l,Run r){
        speak(l,r,r.len,"Len",PinehookTexts.LEN_ARRIVE);
        r.ninaQuiet=true;
        var nina=actor(l,r.nina);if(nina==null)return;
        for(var p:living(l,r.base))if(sees(p,nina.getEyePosition(),14,.4)){mark(p,"SawNinaQuiet");cue(p,"Nina stops talking halfway through a sentence. She doesn't look at the man on the porch.");}
    }
    static void beginScare(ServerLevel l,Run r,List<ServerPlayer> here){
        r.phase=SCARE;r.scare=0;r.powerOff=true;
        stage(l,r,PinehookSite.BREAKER,1);PinehookSite.lights(l,r.base,false);stopMusic(l,r);
        sound(l,r.base.offset(PinehookSite.BREAKER),PinehookRegistry.BREAKER.get(),1F,1F);log(r,PinehookSite.BREAKER,"power_off");
        speak(l,r,r.casey,"Casey",PinehookTexts.BREAKER,24);
        for(var p:here){mark(p,"InScare");if(own(p).getBoolean("PredictedPower")){mark(p,"CalledIt");cue(p,"The lights go out, the way you said they would.");}}
        // Casey goes to the box by the back door; Nina to the bathroom; Jude is already outside in the costume.
        route(r,r.casey,.13,PinehookSite.TO_BOX);
        route(r,r.nina,.1,PinehookSite.TO_BATHROOM);
        // Whoever followed Jude out to the shed watched him put it on.
        for(var p:living(l,r.base))if(PinehookSite.inShed(rel(p.position(),r.base))){mark(p,"SawJudeChange");cue(p,"Jude pulls the coverall up over his clothes and the rubber mask down over his face. He puts a finger to where his lips are under it.");}
        skinOf(l,r.jude,MASKED);hide(l,r.jude,true);moveTo(l,r,r.jude,PinehookSite.WINDOW_OUT,0);
        stage(l,r,PinehookSite.DUFFEL,Math.max(1,PinehookBlock.stage(at(l,r,PinehookSite.DUFFEL))));
    }

    // ------------------------------------------------------------------------------------------------ the fake attack
    static void scare(ServerLevel l,Run r,List<ServerPlayer> here){
        r.scare++;var jude=actor(l,r.jude);
        if(!r.unmasked){
            if(r.scare==WINDOW_AT){hide(l,r.jude,false);gesture(l,r.jude,KNOCKING);log(r,PinehookSite.FRONT_WINDOW,"knock");}
            if(r.scare>=WINDOW_AT&&r.scare<WINDOW_AT+60&&(r.scare-WINDOW_AT)%20==0)sound(l,r.base.offset(PinehookSite.FRONT_WINDOW),PinehookRegistry.KNOCK.get(),1F,.95F+l.random.nextFloat()*.1F);
            if(r.scare==WINDOW_AT+80){gesture(l,r.jude,0);route(r,r.jude,.16,PinehookSite.WINDOW_TO_BACK);}
            if(r.scare>=BACK_AT&&r.scare<BACK_AT+60&&(r.scare-BACK_AT)%15==0&&jude!=null&&jude.position().distanceToSqr(world(r.base,PinehookSite.BACK_OUT))<4)
                {sound(l,r.base.offset(PinehookSite.BACK_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,1F,.7F);if(r.scare==BACK_AT)log(r,PinehookSite.BACK_DOOR,"rattle");}
            if(r.scare==RETREAT_AT)route(r,r.jude,.16,PinehookSite.TO_SHED);
            // Who sees the masked figure, and who sees through it.
            if(jude!=null&&!jude.isInvisible()&&r.scare%5==0)for(var p:living(l,r.base)){
                if(!sees(p,jude.getEyePosition(),20,.55))continue;
                mark(p,"SawMask");
                if(holdingLitTorch(p)&&p.distanceTo(jude)<10&&!own(p).getBoolean("SawRubber")){mark(p,"SawRubber");cue(p,"In the torch's beam the face is rubber. The eyeholes are cut crooked, and someone is breathing behind them.");}
            }
        }
        // He goes in to change once he reaches the shed, unmasked or not.
        if(r.scare>RETREAT_AT||r.unmasked){if(jude!=null&&!walking(r,r.jude)&&PinehookSite.inShed(rel(jude.position(),r.base)))beginShed(l,r);}
        if(r.unmasked&&r.scare==r.unmaskedAt+100&&jude!=null&&!PinehookSite.inShed(rel(jude.position(),r.base))){
            speak(l,r,r.jude,"Jude",PinehookTexts.JUDE_CHANGE);route(r,r.jude,.12,rel(jude.position(),r.base).z>-27?PinehookSite.FRONT_TO_SHED:PinehookSite.TO_SHED);}
        if(r.scare>2400&&jude!=null)beginShed(l,r);
    }
    static boolean holdingLitTorch(ServerPlayer p){var s=p.getMainHandItem();if(!what(s).equals("torch"))s=p.getOffhandItem();return what(s).equals("torch")&&lit(s);}
    static boolean lit(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c!=null&&c.copyTag().getBoolean("Lit");}
    /** A hand on masked Jude: the mask comes off. */
    static void unmask(ServerPlayer p,Run r){
        var l=p.serverLevel();if(r.phase!=SCARE||r.unmasked)return;
        r.unmasked=true;r.unmaskedBy=p.getUUID();r.unmaskedAt=r.scare;mark(p,"Unmasked");
        skinOf(l,r.jude,COVERALL);gesture(l,r.jude,0);var w=r.walkers.get(r.jude);if(w!=null)w.route.clear();hide(l,r.jude,false);
        var jude=actor(l,r.jude);
        for(var q:living(l,r.base))if(q!=p&&jude!=null&&sees(q,jude.getEyePosition(),20,.4))mark(q,"SawUnmasking");
        cue(p,"You get a fistful of rubber and pull. It comes away. Under it, sweating, Jude.");
        speak(l,r,r.jude,"Jude",PinehookTexts.UNMASK_JUDE);
        // Casey comes round, owns up, and puts the power back.
        powerBack(l,r);
        moveTo(l,r,r.casey,jude==null?PinehookSite.CASEY_PORCH:rel(jude.position().add(1.2,0,.5),r.base),180);
        speak(l,r,r.casey,"Casey",PinehookTexts.UNMASK_CASEY,24);
    }
    static void powerBack(ServerLevel l,Run r){if(!r.powerOff)return;r.powerOff=false;stage(l,r,PinehookSite.BREAKER,0);PinehookSite.lights(l,r.base,true);sound(l,r.base.offset(PinehookSite.BREAKER),PinehookRegistry.BREAKER.get(),.8F,1.3F);log(r,PinehookSite.BREAKER,"power_on");}

    // ------------------------------------------------------------------------------------------------ the shed
    static void beginShed(ServerLevel l,Run r){
        if(r.phase!=SCARE)return;
        r.phase=SHED;r.shed=0;r.judeIn=0;
        var w=r.walkers.get(r.jude);if(w!=null)w.route.clear();
        moveTo(l,r,r.jude,PinehookSite.SHED_IN,90);skinOf(l,r.jude,COVERALL);hide(l,r.jude,false);gesture(l,r.jude,0);
        var door=r.base.offset(PinehookSite.SHED_DOOR);var st=l.getBlockState(door);if(st.getBlock() instanceof DoorBlock d&&st.getValue(DoorBlock.OPEN))d.setOpen(null,l,st,door,false);
        if(!r.unmasked){powerBack(l,r);speak(l,r,r.casey,"Casey",PinehookTexts.POWER_BACK,24);}
        // Casey waits on the back step for him.
        moveTo(l,r,r.casey,PinehookSite.BREAKER_SPOT,180);route(r,r.casey,.12,PinehookSite.BOX_TO_BACK);
        // Whoever walked out with him and is in the shed with him now.
        for(var p:living(l,r.base))if(PinehookSite.inShed(rel(p.position(),r.base)))mark(p,"WithJude");
    }
    static boolean anyoneInShed(ServerLevel l,Run r){for(var p:living(l,r.base))if(PinehookSite.inShed(rel(p.position(),r.base)))return true;return false;}
    static void shed(ServerLevel l,Run r,List<ServerPlayer> here){
        r.shed++;var nina=actor(l,r.nina);var shedDoor=world(r.base,PinehookSite.SHED_OUT).add(0,1,0);
        for(var p:living(l,r.base))if(PinehookSite.inShed(rel(p.position(),r.base))&&r.ninaStep<3)mark(p,"WithJude");
        switch(r.ninaStep){
            // 0: in the bathroom; then out of the back door and along the path.
            case 0->{if(r.shed>=NINA_AFTER){r.ninaStep=1;moveTo(l,r,r.nina,PinehookSite.BATHROOM,0);route(r,r.nina,.13,PinehookSite.NINA_OUT);}}
            // 1: walking out; at the shed door she looks in.
            case 1->{if(!walking(r,r.nina)){if(anyoneInShed(l,r)){r.ninaStep=2;r.lurk=0;route(r,r.nina,.1,new Vec3(-13.5,0,-44.5),PinehookSite.BEHIND_SHED);}else enter(l,r);}}
            // 2: behind the shed, waiting for Jude to be alone; giving up if he never is.
            case 2->{r.lurk++;
                if(nina!=null&&r.lurk%10==0)for(var p:living(l,r.base))if(sees(p,nina.getEyePosition(),16,.5)&&!own(p).getBoolean("SawNinaLurking")){mark(p,"SawNinaLurking");cue(p,"Behind the shed, in the dark between the pines, someone is standing very still.");}
                if(!walking(r,r.nina)&&!anyoneInShed(l,r)&&r.lurk>40)enter(l,r);
                else if(r.lurk>=LURK_MAX||r.judeIn>=CHANGE)prevented(l,r);}
            // 3: in the shed with him.
            case 3->{r.struggle++;
                if(r.struggle==1)log(r,PinehookSite.DUFFEL,"crash");
                if(r.struggle==1||r.struggle==60){sound(l,r.base.offset(PinehookSite.DUFFEL),PinehookRegistry.CRASH.get(),1.2F,r.struggle==1?1F:.8F);for(var p:living(l,r.base))if(p.position().distanceToSqr(shedDoor)<24*24)mark(p,"HeardCrash");}
                for(var p:living(l,r.base))if(PinehookSite.inShed(rel(p.position(),r.base))){witnessed(l,r,p);return;}
                if(r.struggle>=STRUGGLE)wounded(l,r);}
            default->{}
        }
        // Jude changing; done, if nobody came for him, he walks back with whoever stayed.
        if(r.judeIn>=0)r.judeIn++;
        if(r.outcome==PREVENTED&&r.judeIn>=CHANGE){r.judeIn=-1;skinOf(l,r.jude,JUDE);route(r,r.jude,.12,PinehookSite.FROM_SHED);r.phase=TURN;r.turn=0;}
    }
    static void enter(ServerLevel l,Run r){
        var nina=actor(l,r.nina);var door=world(r.base,PinehookSite.SHED_OUT).add(0,1.2,0);
        for(var p:living(l,r.base))if(sees(p,door,24,.55)){mark(p,"SawNinaEnter");cue(p,"Someone goes into the shed. The door shuts behind them.");}
        r.ninaStep=3;r.struggle=0;moveTo(l,r,r.nina,new Vec3(-16.5,0,-40.5),90);gesture(l,r.nina,KNIFE);
        var d=r.base.offset(PinehookSite.SHED_DOOR);var st=l.getBlockState(d);if(st.getBlock() instanceof DoorBlock dd&&st.getValue(DoorBlock.OPEN))dd.setOpen(null,l,st,d,false);
    }
    /** Somebody stayed with Jude: Nina gives up for tonight and goes back to the cabin. */
    static void prevented(ServerLevel l,Run r){
        r.outcome=PREVENTED;r.ninaStep=4;
        route(r,r.nina,.12,PinehookSite.NINA_IN);
        for(var p:living(l,r.base))note(p,"Outcome","prevented");
        for(var rel:PinehookSite.SHED_PRINTS)decal(l,r,rel,Kind.PRINTS,Direction.NORTH);r.evidence=true;
    }
    /** A reader got into the shed while she was at him: she runs, and they saw her. */
    static void witnessed(ServerLevel l,Run r,ServerPlayer by){
        r.outcome=WITNESSED;r.ninaStep=5;mark(by,"InterruptedNina");
        var nina=actor(l,r.nina);
        for(var p:living(l,r.base)){note(p,"Outcome","witnessed");if(nina!=null&&sees(p,nina.getEyePosition(),10,.3)){mark(p,"WitnessedAttack");cue(p,"Nina, in the dark of the shed, a knife in her hand. She looks at you. Then she's through the back window.");}}
        gesture(l,r.nina,0);moveTo(l,r,r.nina,PinehookSite.BEHIND_SHED,0);
        route(r,r.nina,.2,new Vec3(-6.5,0,-46.5),new Vec3(10.5,0,-46.5),new Vec3(11.5,0,-44.5));
        sound(l,r.base.offset(PinehookSite.SHED_BACK),SoundEvents.GLASS_BREAK,1F,1F);log(r,PinehookSite.SHED_BACK,"glass");
        set(l,r.base,PinehookSite.SHED_BACK,Blocks.AIR.defaultBlockState());r.shedWindowBroken=true;
        for(var rel:PinehookSite.BEACH_PRINTS)decal(l,r,rel,Kind.PRINTS,Direction.EAST);r.evidence=true;
        r.judeIn=-1;skinOf(l,r.jude,COVERALL);
        route(r,r.jude,.12,PinehookSite.FROM_SHED);
        r.phase=TURN;r.turn=0;
    }
    /** Nobody came: Jude is cut, the costume is gone, and Jude comes back to the cabin without it. */
    static void wounded(ServerLevel l,Run r){
        r.outcome=WOUNDED;r.ninaStep=6;
        for(var p:living(l,r.base))note(p,"Outcome","wounded");
        stage(l,r,PinehookSite.DUFFEL,2);decal(l,r,PinehookSite.SHED_BLOOD,Kind.BLOOD,Direction.NORTH);
        // Nina, in the costume now, out of sight until the dock.
        skinOf(l,r.nina,MASKED);gesture(l,r.nina,0);hide(l,r.nina,true);moveTo(l,r,r.nina,PinehookSite.DOCK_EAST,90);
        r.judeIn=-1;skinOf(l,r.jude,JUDE);gesture(l,r.jude,HURT);
        route(r,r.jude,.07,PinehookSite.FROM_SHED);
        r.phase=TURN;r.turn=0;
    }

    // ------------------------------------------------------------------------------------------------ the turn
    static void turn(ServerLevel l,Run r,List<ServerPlayer> here){
        var jude=actor(l,r.jude);var casey=actor(l,r.casey);
        // The lines wait until Jude has come back to Casey on the back step.
        if(r.turn==0){if(jude==null||walking(r,r.jude)&&++r.turnWait<900)return;}
        r.turn++;
        if(jude!=null&&jude.tickCount%20==0&&r.outcome==WOUNDED)l.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,Blocks.REDSTONE_BLOCK.defaultBlockState()),jude.getX(),jude.getY()+.9,jude.getZ(),2,.1,.1,.1,0);
        String[] lines=switch(r.outcome){case WITNESSED->new String[]{PinehookTexts.WITNESS_1,PinehookTexts.WITNESS_2,PinehookTexts.WITNESS_3};case PREVENTED->new String[]{PinehookTexts.PREVENT_1,PinehookTexts.PREVENT_2};default->new String[]{PinehookTexts.TURN_1,PinehookTexts.TURN_2,PinehookTexts.TURN_3};};
        for(int i=0;i<lines.length;i++)if(r.turn==1+i*LINE_GAP){
            boolean casey2=i==1;speak(l,r,casey2?r.casey:r.jude,casey2?"Casey":"Jude",lines[i],20);
            for(var p:living(l,r.base))if(jude!=null&&p.distanceTo(jude)<20){mark(p,"HeardTurn");if(r.outcome==WOUNDED){mark(p,"SawJudeHurt");}}
        }
        // The figure on the dock, as Jude asks who was in the shed.
        if(r.outcome==WOUNDED){
            if(r.turn==FIGURE_AT){hide(l,r.nina,false);route(r,r.nina,.09,PinehookSite.DOCK_WEST);}
            var nina=actor(l,r.nina);
            if(r.turn>FIGURE_AT&&!r.figureCrossed&&nina!=null&&!nina.isInvisible()&&nina.phase()==MASKED&&r.turn%5==0)for(var p:living(l,r.base))if(sees(p,nina.getEyePosition(),64,.6)&&!own(p).getBoolean("SawFigure")){mark(p,"SawFigure");cue(p,"Down at the far end of the dock a figure in a coverall and a rubber mask walks across the planks, without hurrying.");}
            if(r.turn>FIGURE_AT&&!r.figureCrossed&&!walking(r,r.nina)&&nina!=null&&!nina.isInvisible()){r.figureCrossed=true;hide(l,r.nina,true);moveTo(l,r,r.nina,PinehookSite.BATHROOM,180);sound(l,r.base.offset(2,0,-63),SoundEvents.GENERIC_SPLASH,.6F,.8F);
                log(r,new BlockPos(2,0,-63),"splash");dockEvidence(l,r);r.ninaInAt=r.tick+NINA_IN_DELAY;}
            if(r.turn>=FIGURE_AT+NINA_BACK&&!r.ninaBack){r.ninaBack=true;skinOf(l,r.nina,NINA);hide(l,r.nina,false);moveTo(l,r,r.nina,PinehookSite.BATHROOM,180);route(r,r.nina,.1,new Vec3(0.5,0,-29.5),PinehookSite.LIVING);}
            if(r.ninaBack&&!walking(r,r.nina)&&r.hold<0){speak(l,r,r.nina,"Nina",PinehookTexts.NINA_BACK);for(var p:living(l,r.base))if(nina!=null&&p.distanceTo(nina)<16)mark(p,"HeardNinaBack");r.phase=HOLD;r.hold=0;}
        }else if(r.turn>=1+lines.length*LINE_GAP){
            if(r.outcome==PREVENTED&&!r.ninaBack){r.ninaBack=true;skinOf(l,r.nina,NINA);hide(l,r.nina,false);}
            r.phase=HOLD;r.hold=0;
        }
    }

    // ------------------------------------------------------------------------------------------------ the night held
    static void hold(ServerLevel l,Run r,List<ServerPlayer> here){
        r.hold++;
        if(r.outcome==WITNESSED&&!walking(r,r.nina))hide(l,r.nina,true);
        if(r.outcome==WOUNDED&&r.hold==80)speak(l,r,r.jude,"Jude",PinehookTexts.JUDE_HURT,14);
        if(r.outcome==WOUNDED&&r.hold==80+LINE_GAP)speak(l,r,r.casey,"Casey",PinehookTexts.CASEY_MINIMISE,14);
        if(r.outcome==WOUNDED&&r.hold==80+2*LINE_GAP){speak(l,r,r.jude,"Jude",PinehookTexts.JUDE_NOT,14);var jude=actor(l,r.jude);for(var p:living(l,r.base))if(jude!=null&&p.distanceTo(jude)<14)mark(p,"HeardJudeHurt");}
        if(r.hold>=NIGHT_AT)beginNight(l,r);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.365, the night
    static final Vec3[] KITCHEN_TO_HEARTH={new Vec3(4.5,0,-26.5),new Vec3(-4.5,0,-26.5),PinehookSite.AT_HEARTH};
    static final Vec3[] HEARTH_TO_KITCHEN={new Vec3(-4.5,0,-26.5),new Vec3(4.5,0,-26.5),PinehookSite.CASEY_KITCHEN};
    static final Vec3[] CASEY_IN={PinehookSite.BACK_OUT,new Vec3(5.5,0,-33.5),PinehookSite.CASEY_KITCHEN};
    static void beginNight(ServerLevel l,Run r){
        r.phase=NIGHT;r.night=0;
        route(r,r.jude,.08,PinehookSite.INTO_KITCHEN);
        if(r.outcome==PREVENTED)route(r,r.nina,.1,new Vec3(0.5,0,-29.5),PinehookSite.LIVING);
        var len=actor(l,r.len);
        if(len!=null){speak(l,r,r.len,"Len",PinehookTexts.LEN_GOES,16);for(var p:living(l,r.base))if(p.distanceTo(len)<16)mark(p,"HeardLenGo");route(r,r.len,.12,PinehookSite.LEN_TO_BOAT);}
        else{r.lenInside=true;r.lenHere=true;r.len=spawn(l,r,LEN,PinehookSite.BOAT_INSIDE,180);}
    }
    static void night(ServerLevel l,Run r,List<ServerPlayer> here){
        r.night++;
        if(r.night==CASEY_FOLLOW)route(r,r.casey,.11,CASEY_IN);
        // Len reaches his boathouse and locks himself in.
        var len=actor(l,r.len);
        if(!r.lenInside&&len!=null&&!walking(r,r.len)){r.lenInside=true;sound(l,r.base.offset(PinehookSite.BOATHOUSE_DOOR),SoundEvents.IRON_DOOR_OPEN,.8F,1F);moveTo(l,r,r.len,PinehookSite.BOAT_INSIDE,180);sound(l,r.base.offset(PinehookSite.BOATHOUSE_DOOR),SoundEvents.IRON_DOOR_CLOSE,.8F,.9F);}
        gesture(l,r.jude,r.outcome==WOUNDED&&!r.bandaged?HURT:0);
        burn(l,r);
        if(r.night%10==0){var safe=Vec3.atCenterOf(r.base.offset(PinehookSite.SAFE));for(var p:here)if(!p.isSpectator()&&sees(p,safe,12,.6))mark(p,"SawSafe");}
    }
    /** Casey goes for his plan and puts it in the fire, unless he has turned or someone has it first. */
    static void burn(ServerLevel l,Run r){
        var casey=actor(l,r.casey);if(casey==null)return;
        switch(r.burnStep){
            case 0->{if(!r.caseyTurned&&!r.planTaken&&!r.planBurned&&r.night>=BURN_AT&&!walking(r,r.casey)){r.burnStep=1;route(r,r.casey,.11,PinehookSite.TO_SUITCASE);}}
            case 1->{if(walking(r,r.casey))return;
                if(!r.planTaken&&!r.planBurned&&PinehookBlock.stage(at(l,r,PinehookSite.SUITCASE))==0){stage(l,r,PinehookSite.SUITCASE,1);r.caseyHasPlan=true;r.burnStep=2;route(r,r.casey,.11,PinehookSite.SUITCASE_TO_HEARTH);}
                else{for(var p:living(l,r.base))if(sees(p,casey.getEyePosition(),12,.5))cue(p,PinehookTexts.CASEY_BAG_EMPTY_CUE);r.burnStep=3;route(r,r.casey,.11,PinehookSite.SUITCASE_TO_HEARTH[0],PinehookSite.SUITCASE_TO_HEARTH[1],PinehookSite.SUITCASE_TO_HEARTH[2],PinehookSite.SUITCASE_TO_HEARTH[3],new Vec3(4.5,0,-26.5),PinehookSite.CASEY_KITCHEN);}}
            case 2->{if(walking(r,r.casey))return;
                if(r.caseyHasPlan){r.caseyHasPlan=false;r.planBurned=true;var h=r.base.offset(PinehookSite.HEARTH);l.sendParticles(ParticleTypes.FLAME,h.getX()+.5,h.getY()+.6,h.getZ()+.5,12,.2,.2,.2,.02);sound(l,h,SoundEvents.FIRECHARGE_USE,.4F,1.4F);
                    for(var p:living(l,r.base))if(sees(p,casey.getEyePosition(),12,.5)||p.distanceTo(casey)<4){mark(p,"SawCaseyBurn");cue(p,PinehookTexts.CASEY_BURN_CUE);}}
                r.burnStep=3;route(r,r.casey,.11,HEARTH_TO_KITCHEN);}
            case 3->{if(!walking(r,r.casey))r.burnStep=4;}
            default->{}
        }
    }
    /** The dock after the figure has crossed it: spots along the T, the tie at the west post, the boat pulled round with the costume
     * under its tarp, and wet prints from the shallows up the path. */
    static void dockEvidence(ServerLevel l,Run r){
        for(var rel:PinehookSite.DOCK_BLOOD)decal(l,r,rel,Kind.BLOOD,Direction.NORTH);
        decal(l,r,PinehookSite.DOCK_TIE,Kind.TIE,Direction.NORTH);
        PinehookSite.moorBoat(l,r.base,false,1);
        for(var rel:PinehookSite.WET_PRINTS)decal(l,r,rel,Kind.PRINTS,Direction.SOUTH);
        r.evidence=true;
    }
    static void decal(ServerLevel l,Run r,BlockPos rel,Kind kind,Direction f){if(at(l,r,rel).isAir())set(l,r.base,rel,PinehookBlock.of(kind,f,0));}
    /** Whoever leaves the cabin's world leaves the weekend's things behind: the cooler goes back on the porch, the plan in Casey's bag, the
     * costume under the tarp, the tie on the dock. */
    static void leavers(ServerLevel l,Run r,List<ServerPlayer> here){
        var now=new HashSet<UUID>();for(var p:here)now.add(p.getUUID());
        for(var id:r.present)if(!now.contains(id)){var q=l.getServer().getPlayerList().getPlayer(id);if(q!=null)strip(l,r,q);}
        r.present=now;
    }
    static void strip(ServerLevel l,Run r,ServerPlayer q){
        var inv=q.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var what=what(inv.getItem(i));if(!THINGS.contains(what))continue;inv.setItem(i,ItemStack.EMPTY);
            switch(what){
                case "cooler"->{if("carried".equals(r.cooler)){set(l,r.base,PinehookSite.COOLER_START,PinehookBlock.of(Kind.COOLER,Direction.SOUTH,0));r.cooler="porch";r.coolerBy=null;}}
                case "plan"->{if(!r.planBurned){stage(l,r,PinehookSite.SUITCASE,0);r.planTaken=false;}}
                case "costume"->{r.costumeTaken=false;stage(l,r,PinehookSite.STERN_WEST,2);}
                case "tie"->{r.tieTaken=false;decal(l,r,PinehookSite.DOCK_TIE,Kind.TIE,Direction.NORTH);}
                default->{}
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ the dock, the cooler, the torches, the idle talk
    static void dock(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.tick%10!=0)return;
        for(var p:here){if(p.isSpectator())continue;var at=rel(p.position(),r.base);
            if(PinehookSite.atDockEnd(at)&&!own(p).getBoolean("WalkedDock")){mark(p,"WalkedDock");cue(p,"The lake is flat and dark to the far shore. The boathouse beside the dock has a padlock on its door and a sign that says to keep out.");}
        }
    }
    /** The cooler is heavy: whoever carries it goes slowly, and it stays in the cabin's world. */
    static void carry(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.tick%20!=0)return;
        for(var p:here)if(carries(p,"cooler"))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,1,false,false,true));
    }
    static void torches(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.tick%2!=0)return;
        var keep=new HashSet<UUID>();
        for(var p:here){
            if(p.isSpectator()||!holdingLitTorch(p))continue;keep.add(p.getUUID());
            var eye=p.getEyePosition();var hit=l.clip(new net.minecraft.world.level.ClipContext(eye,eye.add(p.getLookAngle().scale(8)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
            var cell=BlockPos.containing(hit.getLocation().subtract(p.getLookAngle().scale(.6)));
            Long old=r.torches.get(p.getUUID());
            if(old!=null&&old==cell.asLong())continue;
            if(old!=null){var o=BlockPos.of(old);if(l.getBlockState(o).is(Blocks.LIGHT))l.setBlock(o,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);r.torches.remove(p.getUUID());}
            if(l.getBlockState(cell).isAir()){l.setBlock(cell,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12),Block.UPDATE_CLIENTS);r.torches.put(p.getUUID(),cell.asLong());}
        }
        for(var id:List.copyOf(r.torches.keySet()))if(!keep.contains(id)){var o=BlockPos.of(r.torches.remove(id));if(l.getBlockState(o).is(Blocks.LIGHT))l.setBlock(o,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
    }
    static void idle(ServerLevel l,Run r,List<ServerPlayer> here){
        if(r.phase!=EVENING||r.tick%400!=200)return;
        int n=(int)(r.tick/400);
        switch(n%3){
            case 0->{var lines=PinehookTexts.CASEY_IDLE;speak(l,r,r.casey,"Casey",lines[n%lines.length],12);}
            case 1->{var lines=PinehookTexts.JUDE_IDLE;speak(l,r,r.jude,"Jude",lines[n%lines.length],12);}
            default->{if(!r.ninaQuiet){var lines=PinehookTexts.NINA_IDLE;speak(l,r,r.nina,"Nina",lines[n%lines.length],12);}}
        }
    }
    static void stopMusic(ServerLevel l,Run r){
        r.music=false;
        for(var p:readers(l,r.base))p.connection.send(new ClientboundStopSoundPacket(null,SoundSource.RECORDS));
    }

    // ------------------------------------------------------------------------------------------------ talking to the cast
    public static void talk(ServerPlayer p,LiteraryActor a){
        var r=runOf(p);if(r==null||p.isSpectator())return;var id=a.getUUID();
        if(id.equals(r.casey))casey(p,r);
        else if(id.equals(r.jude))jude(p,r,a);
        else if(id.equals(r.nina))nina(p,r);
        else if(id.equals(r.len))len(p,r);
    }
    static void casey(ServerPlayer p,Run r){
        if(r.phase==NIGHT){caseyNight(p,r);return;}
        if(!own(p).contains("Part")&&r.phase<SCARE){offerParts(p,r.phase>=EVENING?PinehookTexts.GREET_LATE:PinehookTexts.GREET);return;}
        Talk.begin(p,TALK,"Casey");Talk.line(p,"Casey",PinehookTexts.CASEY_TALK);
        var replies=new ArrayList<Talk.Reply>();
        if(r.phase<SCARE&&!own(p).getBoolean("PredictedPower"))replies.add(Talk.reply(PinehookTexts.POWER_ASK,q->{mark(q,"PredictedPower");Talk.line(q,"Casey",PinehookTexts.POWER_ANSWER);Talk.end(q);}));
        if(r.unmasked&&r.phase<=SHED){
            if(!own(p).getBoolean("AskedToldUs"))replies.add(Talk.reply(PinehookTexts.AWK_TOLD,q->{mark(q,"AskedToldUs");Talk.line(q,"Casey",PinehookTexts.AWK_TOLD_ANSWER);Talk.end(q);}));
            if(!own(p).getBoolean("AskedPower"))replies.add(Talk.reply(PinehookTexts.AWK_POWER,q->{mark(q,"AskedPower");Talk.line(q,"Casey",PinehookTexts.AWK_POWER_ANSWER);Talk.end(q);}));
            if(!own(p).getBoolean("PraisedBit"))replies.add(Talk.reply(PinehookTexts.AWK_GOOD,q->{mark(q,"PraisedBit");Talk.line(q,"Casey",PinehookTexts.AWK_GOOD_ANSWER);Talk.end(q);}));
        }
        replies.add(Talk.act("Leave him to it.",Talk::close));Talk.ask(p,replies);
    }
    static void jude(ServerPlayer p,Run r,LiteraryActor a){
        if(r.phase==NIGHT){judeNight(p,r);return;}
        if(r.phase==SCARE&&!r.unmasked&&!a.isInvisible()){
            if(p.distanceTo(a)>3.5){cue(p,"Too far to reach.");return;}
            Talk.begin(p,TALK,"The figure");Talk.line(p,"","A rubber face a hand's width from yours. It doesn't move.");
            Talk.ask(p,Talk.act("Pull the mask off.",q->{Talk.close(q);var rr=runOf(q);if(rr!=null)unmask(q,rr);}),Talk.act("Back away.",Talk::close));return;}
        if(r.phase>=TURN&&r.outcome==WOUNDED){
            Talk.begin(p,TALK,"Jude");Talk.line(p,"Jude",r.bandaged?PinehookTexts.JUDE_BANDAGED:PinehookTexts.JUDE_HURT);Talk.end(p);mark(p,"HeardJudeHurt");return;}
        Talk.begin(p,TALK,"Jude");Talk.line(p,"Jude",PinehookTexts.JUDE_TALK);
        var replies=new ArrayList<Talk.Reply>();
        if(!own(p).getBoolean("AskedJude")&&!own(p).getBoolean("MockedJude")){
            replies.add(Talk.reply(PinehookTexts.JUDE_ASK,q->{mark(q,"AskedJude");Talk.line(q,"Jude",PinehookTexts.JUDE_ASK_ANSWER);Talk.end(q);}));
            replies.add(Talk.reply(PinehookTexts.JUDE_MOCK,q->{mark(q,"MockedJude");Talk.line(q,"Jude",PinehookTexts.JUDE_MOCK_ANSWER);Talk.end(q);}));
        }
        if(own(p).getBoolean("KeptSecret")||own(p).getBoolean("Objected")||r.unmasked)if(!own(p).getBoolean("AskedOwner"))replies.add(Talk.reply(PinehookTexts.JUDE_OWNER,q->{mark(q,"AskedOwner");Talk.line(q,"Jude",PinehookTexts.JUDE_OWNER_ANSWER);Talk.end(q);}));
        replies.add(Talk.act("Leave him be.",Talk::close));Talk.ask(p,replies);
    }
    static void nina(ServerPlayer p,Run r){
        if(r.phase==NIGHT){ninaNight(p,r);return;}
        Talk.begin(p,TALK,"Nina");Talk.line(p,"Nina",r.ninaQuiet&&r.phase==EVENING?"…":PinehookTexts.NINA_TALK);
        var replies=new ArrayList<Talk.Reply>();
        if(r.phase==EVENING&&!own(p).getBoolean("SatWithNina"))replies.add(Talk.act(PinehookTexts.NINA_SIT,q->{mark(q,"SatWithNina");Talk.line(q,"Nina",PinehookTexts.NINA_SIT_ANSWER);Talk.end(q);}));
        if(!r.lenHere&&!own(p).getBoolean("AskedNinaBefore"))replies.add(Talk.reply(PinehookTexts.NINA_HOW,q->{mark(q,"AskedNinaBefore");Talk.line(q,"Nina",PinehookTexts.NINA_HOW_ANSWER);Talk.end(q);}));
        if(own(p).getBoolean("SawNinaQuiet")&&!own(p).getBoolean("AskedNina"))replies.add(Talk.reply(PinehookTexts.NINA_LEN,q->{mark(q,"AskedNina");Talk.line(q,"Nina",PinehookTexts.NINA_LEN_ANSWER);Talk.end(q);}));
        replies.add(Talk.act("Leave her be.",Talk::close));Talk.ask(p,replies);
    }
    static void len(ServerPlayer p,Run r){
        if(r.lenInside){lenDoor(p,r);return;}
        Talk.begin(p,TALK,"Len");Talk.line(p,"Len",PinehookTexts.LEN_TALK);
        var replies=new ArrayList<Talk.Reply>();
        if(!own(p).getBoolean("TookLenSeriously")&&!own(p).getBoolean("DismissedLen")){
            replies.add(Talk.reply(PinehookTexts.LEN_SERIOUS,q->{mark(q,"TookLenSeriously");Talk.line(q,"Len",PinehookTexts.LEN_SERIOUS_ANSWER);Talk.end(q);}));
            replies.add(Talk.reply(PinehookTexts.LEN_DISMISS,q->{mark(q,"DismissedLen");Talk.line(q,"Len",PinehookTexts.LEN_DISMISS_ANSWER);Talk.end(q);}));
        }
        replies.add(Talk.act("Leave him be.",Talk::close));Talk.ask(p,replies);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.365, talking in the night
    static boolean has(ServerPlayer p,String key){return own(p).getBoolean(key);}
    static void judeNight(ServerPlayer p,Run r){
        var l=p.serverLevel();var jude=actor(l,r.jude);var casey=actor(l,r.casey);
        boolean caseyNear=jude!=null&&casey!=null&&casey.distanceTo(jude)<8;
        if(r.outcome!=PREVENTED&&caseyNear&&!r.hushed){
            Talk.begin(p,TALK,"Jude");Talk.line(p,"Jude",PinehookTexts.JUDE_CUT);Talk.line(p,"Casey",PinehookTexts.CASEY_OVER);
            Talk.ask(p,Talk.reply(PinehookTexts.HUSH,q->{var rr=runOf(q);if(rr!=null)rr.hushed=true;mark(q,"CaseyHushed");Talk.line(q,"Casey",PinehookTexts.HUSH_ANSWER);Talk.end(q);}),Talk.act("Leave him be.",Talk::close));
            return;
        }
        Talk.begin(p,TALK,"Jude");
        Talk.line(p,"Jude",switch(r.outcome){case WITNESSED->PinehookTexts.JUDE_NIGHT_WITNESSED;case PREVENTED->PinehookTexts.JUDE_NIGHT_PREVENTED;default->r.bandaged?PinehookTexts.JUDE_BANDAGED:PinehookTexts.JUDE_NIGHT_WOUNDED;});
        var replies=new ArrayList<Talk.Reply>();
        if(!has(p,"HeardJudeAccount"))replies.add(Talk.reply(r.outcome==PREVENTED?PinehookTexts.JUDE_Q_WHO_PREVENTED:PinehookTexts.JUDE_Q_WHO,q->{mark(q,"HeardJudeAccount");var rr=runOf(q);
            Talk.line(q,"Jude",rr==null||rr.outcome==WOUNDED?PinehookTexts.JUDE_WHO_WOUNDED:rr.outcome==WITNESSED?PinehookTexts.JUDE_WHO_WITNESSED:PinehookTexts.JUDE_WHO_PREVENTED);Talk.end(q);}));
        if(!has(p,"StrapMismatch")&&(has(p,"FoundElastic")||carries(p,"elastic")))replies.add(Talk.reply(PinehookTexts.JUDE_Q_STRAP,q->{mark(q,"StrapMismatch");Talk.line(q,"Jude",PinehookTexts.JUDE_STRAP);Talk.line(q,"",PinehookTexts.STRAP_CUE);Talk.end(q);}));
        if(!has(p,"HeardCaseyToldNina"))replies.add(Talk.reply(PinehookTexts.JUDE_Q_KNEW,q->{Talk.line(q,"Jude",PinehookTexts.JUDE_KNEW);var rr=runOf(q);var c=rr==null?null:actor(q.serverLevel(),rr.casey);
            if(c!=null&&c.distanceTo(q)<10){Talk.line(q,"Casey",PinehookTexts.CASEY_TOLD);mark(q,"HeardCaseyToldNina");}Talk.end(q);}));
        if(!has(p,"HeardJudeSawNina"))replies.add(Talk.reply(PinehookTexts.JUDE_Q_BOAT,q->{mark(q,"HeardJudeSawNina");Talk.line(q,"Jude",PinehookTexts.JUDE_BOAT);Talk.end(q);}));
        replies.add(Talk.act("Leave him be.",Talk::close));Talk.ask(p,replies);
    }
    /** What the reader knows that Casey could be told. */
    static boolean knows(ServerPlayer p,Run r,PinehookTexts.Tell t){return switch(t){
        case PRINTS->has(p,"SawWetPrints")||has(p,"SawBeachPrints");
        case BLOOD->has(p,"SawDockBlood")||has(p,"SawShedBlood");
        case PLAN->has(p,"ReadPlan")&&r.figureCrossed;
        case STRAP->has(p,"StrapMismatch");
        case COSTUME->has(p,"FoundCostume");
        case TAPE->has(p,"TapeNina");
    };}
    static void caseyNight(ServerPlayer p,Run r){
        if(carries(p,"plan")&&!r.caseyTurned&&!has(p,"CaseyAskedPlan")){
            mark(p,"CaseyAskedPlan");Talk.begin(p,TALK,"Casey");Talk.line(p,"Casey",PinehookTexts.CASEY_WANTS_PLAN);
            Talk.ask(p,Talk.reply(PinehookTexts.GIVE_PLAN,q->{var rr=runOf(q);if(rr==null)return;take(q,"plan");rr.planTaken=false;rr.caseyHasPlan=true;mark(q,"GavePlanBack");Talk.line(q,"Casey",PinehookTexts.CASEY_GOT_PLAN);Talk.end(q);
                    rr.burnStep=2;route(rr,rr.casey,.11,KITCHEN_TO_HEARTH);}),
                Talk.reply(PinehookTexts.KEEP_PLAN,q->{Talk.line(q,"Casey","Please.");Talk.end(q);}));
            return;
        }
        Talk.begin(p,TALK,"Casey");
        Talk.line(p,"Casey",r.caseyTurned?PinehookTexts.CASEY_NIGHT_TURNED:switch(r.outcome){case WITNESSED->PinehookTexts.CASEY_NIGHT_WITNESSED;case PREVENTED->PinehookTexts.CASEY_NIGHT_PREVENTED;default->PinehookTexts.CASEY_NIGHT_WOUNDED;});
        var replies=new ArrayList<Talk.Reply>();
        if(!has(p,"AskedCaseyWho"))replies.add(Talk.reply(PinehookTexts.CASEY_Q_WHO,q->{mark(q,"AskedCaseyWho");mark(q,"HeardCaseyToldNina");Talk.line(q,"Casey",PinehookTexts.CASEY_WHO);Talk.end(q);}));
        if(!has(p,"AskedCaseyPlan")||r.caseyTurned&&!has(p,"KnowsPlanWhere")&&!r.planBurned&&!r.planTaken)replies.add(Talk.reply(PinehookTexts.CASEY_Q_PLAN,q->{mark(q,"AskedCaseyPlan");var rr=runOf(q);
            if(rr!=null&&rr.caseyTurned&&!rr.planBurned){mark(q,"KnowsPlanWhere");Talk.line(q,"Casey",PinehookTexts.CASEY_PLAN_YES);}else Talk.line(q,"Casey",PinehookTexts.CASEY_PLAN_NO);Talk.end(q);}));
        for(var tell:PinehookTexts.Tell.values()){int bit=1<<tell.ordinal();
            if((r.caseyKnows&bit)!=0||!knows(p,r,tell))continue;
            replies.add(Talk.reply(tell.line,q->{var rr=runOf(q);if(rr==null)return;rr.caseyKnows|=bit;mark(q,"Told"+tell.name());Talk.line(q,"Casey",tell.answer);
                if(!rr.caseyTurned&&(Integer.bitCount(rr.caseyKnows)>=2||rr.outcome==WITNESSED)){turn(q,rr);}
                Talk.end(q);}));
        }
        replies.add(Talk.act("Leave him to it.",Talk::close));Talk.ask(p,replies);
    }
    /** Casey stops calling it a bit. */
    static void turn(ServerPlayer p,Run r){
        var l=p.serverLevel();r.caseyTurned=true;mark(p,"TurnedCasey");Talk.line(p,"Casey",PinehookTexts.CASEY_TURNS);
        var casey=actor(l,r.casey);
        for(var q:living(l,r.base))if(casey!=null&&q.distanceTo(casey)<16)mark(q,"HeardCaseyTurn");
        if(casey!=null){casey.say(PinehookTexts.CASEY_TURNS);casey.speaks(PinehookTexts.CASEY_TURNS);}
        if(r.burnStep==1){var w=r.walkers.get(r.casey);if(w!=null)w.route.clear();r.burnStep=4;}
    }
    static void ninaNight(ServerPlayer p,Run r){
        Talk.begin(p,TALK,"Nina");Talk.line(p,"Nina",r.outcome==PREVENTED?PinehookTexts.NINA_NIGHT_PREVENTED:PinehookTexts.NINA_NIGHT_WOUNDED);
        var replies=new ArrayList<Talk.Reply>();
        if(!has(p,"NinaDenied"))replies.add(Talk.reply(PinehookTexts.NINA_Q_OUT,q->{mark(q,"NinaDenied");Talk.line(q,"Nina",PinehookTexts.NINA_OUT);Talk.end(q);}));
        if(!has(p,"ConfrontedNina")&&(has(p,"SawWetPrints")||has(p,"TapeNina")))replies.add(Talk.reply(PinehookTexts.NINA_Q_WET,q->{mark(q,"ConfrontedNina");Talk.line(q,"Nina",PinehookTexts.NINA_WET);Talk.end(q);}));
        if(!has(p,"AskedNinaBoathouse")&&has(p,"HeardJudeSawNina"))replies.add(Talk.reply(PinehookTexts.NINA_Q_BOAT,q->{mark(q,"AskedNinaBoathouse");Talk.line(q,"Nina",PinehookTexts.NINA_BOAT);Talk.end(q);}));
        replies.add(Talk.act("Leave her be.",Talk::close));Talk.ask(p,replies);
    }
    /** Len, talking through his boathouse door. */
    static void lenDoor(ServerPlayer p,Run r){
        Talk.begin(p,TALK,"Len, through the door");Talk.line(p,"Len",PinehookTexts.LEN_DOOR);
        var replies=new ArrayList<Talk.Reply>();
        replies.add(Talk.reply(PinehookTexts.LEN_Q_IN,q->{Talk.line(q,"Len",PinehookTexts.LEN_IN);Talk.end(q);}));
        if(!has(p,"AskedLenWho"))replies.add(Talk.reply(PinehookTexts.LEN_Q_WHO,q->{mark(q,"AskedLenWho");Talk.line(q,"Len",PinehookTexts.LEN_WHO);Talk.end(q);}));
        if(!has(p,"LenSlipped")&&(has(p,"SawWetPrints")||has(p,"SawBeachPrints")))replies.add(Talk.reply(PinehookTexts.LEN_Q_PRINTS,q->{mark(q,"LenSlipped");Talk.line(q,"Len",PinehookTexts.LEN_PRINTS);Talk.line(q,"Len",PinehookTexts.LEN_SLIP);Talk.end(q);}));
        if(!has(p,"AskedLenSafe")&&has(p,"SawSafe"))replies.add(Talk.reply(PinehookTexts.LEN_Q_SAFE,q->{mark(q,"AskedLenSafe");Talk.line(q,"Len",PinehookTexts.LEN_SAFE);Talk.end(q);}));
        if(!has(p,"WatchLen"))replies.add(Talk.reply(PinehookTexts.LEN_Q_WATCH,q->{mark(q,"WatchLen");Talk.line(q,"Len",PinehookTexts.LEN_WATCH);Talk.end(q);}));
        replies.add(Talk.act("Leave him be.",Talk::close));Talk.ask(p,replies);
    }
    /** A knock on the boathouse's iron door: Len answers through it, once he is inside. */
    @SubscribeEvent public static void knock(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator())return;
        var r=runOf(p);if(r==null)return;var rel=e.getPos().subtract(r.base);
        if(!rel.equals(PinehookSite.BOATHOUSE_DOOR)&&!rel.equals(PinehookSite.BOATHOUSE_DOOR.above()))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        knockAt(p,r);
    }
    public static void knockAt(ServerPlayer p,Run r){
        sound(p.serverLevel(),r.base.offset(PinehookSite.BOATHOUSE_DOOR),PinehookRegistry.KNOCK.get(),.9F,.8F);mark(p,"KnockedLen");
        if(!r.lenInside){cue(p,"Nobody answers. The boathouse is dark inside.");return;}
        lenDoor(p,r);
    }
    public static boolean knockAt(ServerPlayer p){var r=runOf(p);if(r==null)return false;knockAt(p,r);return true;}

    // ------------------------------------------------------------------------------------------------ hands on the cabin
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;var r=runOf(p);if(r==null)return false;var l=p.serverLevel();var relPos=pos.subtract(r.base);int stage=s.getValue(PinehookBlock.STAGE);
        switch(s.getValue(PinehookBlock.KIND)){
            case COOLER->{
                if(relPos.equals(PinehookSite.COOLER_SPOT)){cue(p,"The cooler, in the kitchen where it belongs. Ice and cans and a bag of burgers.");return true;}
                if(carries(p,"cooler")){cue(p,"You've got your hands full.");return true;}
                set(l,r.base,relPos,Blocks.AIR.defaultBlockState());give(p,thing("cooler"));r.cooler="carried";r.coolerBy=p.getUUID();mark(p,"CarriedCooler");
                cue(p,"You get your arms round the cooler and lift. It's very heavy.");
                if(r.phase<SCARE&&actor(l,r.casey)!=null){speak(l,r,r.casey,"Casey",PinehookTexts.COOLER_SHOW,10);route(r,r.casey,.11,PinehookSite.TO_KITCHEN);}
            }
            case STEREO->{
                if(r.powerOff){cue(p,"Dead. The power's out.");return true;}
                if(stage==1){stage(l,r,relPos,0);stopMusic(l,r);cue(p,"You turn the music off. The cabin is very quiet.");return true;}
                Talk.begin(p,TALK,"The stereo");Talk.line(p,"","A stereo with a tape deck and a stack of Casey's records beside it.");
                var replies=new ArrayList<Talk.Reply>();
                if(carries(p,"mixtape"))replies.add(Talk.act("Put the mixtape on.",q->play(q,"mixtape",SoundEvents.MUSIC_DISC_CHIRP.value())));
                replies.add(Talk.act("Put one of Casey's records on.",q->play(q,"records",SoundEvents.MUSIC_DISC_MALL.value())));
                replies.add(Talk.act("Find something on the radio.",q->play(q,"radio",SoundEvents.MUSIC_DISC_FAR.value())));
                replies.add(Talk.act("Leave it.",Talk::close));Talk.ask(p,replies);
            }
            case TV->{
                if(r.powerOff){cue(p,"Dark. The power's out.");return true;}
                if(stage>0){cue(p,stage==2?"The rented film: a camp, a lake, a long knife, a lot of screaming.":"Casey's film: people being funny at a wedding.");return true;}
                Talk.begin(p,TALK,"The television");Talk.line(p,"","A television on a cabinet, a tape deck under it. Casey's film is in its box on top.");
                var replies=new ArrayList<Talk.Reply>();
                replies.add(Talk.act("Put Casey's film on.",q->film(q,1)));
                if(carries(p,"tape"))replies.add(Talk.act("Put the rented film on.",q->film(q,2)));
                replies.add(Talk.act("Leave it.",Talk::close));Talk.ask(p,replies);
            }
            case TAPES->{
                if(r.film==0&&!carries(p,"tape")&&!anyoneCarries(l,r,"tape")){give(p,thing("tape"));cue(p,"A shelf of rented films, most of them comedies. One isn't: a camp, a lake, a long knife on the box. You take it.");}
                else if(!r.sparePlayer&&!anyoneCarries(l,r,"player")){r.sparePlayer=true;give(p,thing("player"));cue(p,"Behind the films, somebody's old portable player, a blank tape in it. You take it.");}
                else cue(p,"A shelf of rented films, most of them comedies, all with the general store's sticker on.");
            }
            case STOVE->{
                if(r.powerOff){cue(p,"The burners are dead. The power's out.");return true;}
                if(stage==0&&r.phase==EVENING){stage(l,r,relPos,1);r.food=1;r.foodAt=r.tick+COOK;mark(p,"StartedFood");cue(p,"You get the burgers on. The kitchen fills with the smell.");speak(l,r,r.casey,"Casey",PinehookTexts.FOOD,14);}
                else if(stage==1)cue(p,"They're not done yet.");
                else if(stage==2){stage(l,r,relPos,0);r.food=3;mark(p,"ServedFood");give(p,new ItemStack(Items.COOKED_BEEF));cue(p,"You put the burgers on a plate and take one.");}
                else cue(p,"A stove, cold.");
            }
            case BREAKER->{
                if(stage==1){cue(p,"The main switch is down. Somebody pulled it; it doesn't trip like that.");mark(p,"SawBreaker");return true;}
                cue(p,"The breaker box. Old switches, labels in pencil: KITCHEN, LIGHTS, PUMP, SHED.");
            }
            case DUFFEL->{
                if(stage==2){cue(p,"Jude's duffel, open. The coverall and the mask are gone. The rubber knife is still here.");return true;}
                stage(l,r,relPos,1);mark(p,"SawPropBlade");
                cue(p,r.phase<SCARE?"Jude's duffel. A rubber knife painted to look like steel, a bottle of stage blood, a coverall folded round a mask.":"Jude's duffel. A rubber knife painted to look like steel, a bottle of stage blood. The coverall and the mask are out.");
            }
            case BAG->{
                if(stage==1){cue(p,"A canvas bag thrown down behind the shed, empty but for a roll of tape.");return true;}
                stage(l,r,relPos,1);give(p,thing("elastic"));mark(p,"FoundElastic");
                cue(p,"A canvas bag thrown down behind the shed. In it, round a roll of tape, a length of elastic with a buckle at each end, cut to size.");
            }
            case KIT->{
                if(stage==1){cue(p,"The first-aid kit's hook. The kit's gone.");return true;}
                stage(l,r,relPos,1);give(p,thing("kit"));cue(p,"You take the first-aid kit down off the wall.");
            }
            case LAMP->cue(p,r.powerOff?"Dead.":"A lamp.");
            case PRINTS->{mark(p,"SawPrints");
                if(PinehookSite.WET_PRINTS.contains(relPos)){mark(p,"SawWetPrints");cue(p,"Bare footprints, wet, small, coming up out of the water and going up the path toward the back step.");}
                else if(PinehookSite.BEACH_PRINTS.contains(relPos)){mark(p,"SawBeachPrints");cue(p,"Shoe prints in the sand, far apart, running, along the beach toward the boathouse.");}
                else{mark(p,"SawShedPrints");cue(p,"Boot prints in the needles behind the shed, close together, as if someone stood here a long time.");}}
            case BLOOD->{if(relPos.equals(PinehookSite.SHED_BLOOD)){mark(p,"SawShedBlood");cue(p,"On the shed floor, dark, and spreading into the boards.");}
                else{mark(p,"SawDockBlood");cue(p,"Dark spots on the planks, a line of them along the T. Still tacky. Not paint.");}}
            case TIE->{set(l,r.base,relPos,Blocks.AIR.defaultBlockState());r.tieTaken=true;give(p,thing("tie"));mark(p,"FoundTie");cue(p,"A tie from a coverall, torn off and caught on the post. Wet through.");}
            case BOAT_BOW->{if(relPos.equals(PinehookSite.BOW_WEST)){mark(p,"SawBoatMoved");cue(p,"Len's rowboat, pulled round to this end of the dock and tied off with a knot nobody who ties boats would tie.");}
                else cue(p,"Len's rowboat, tied off the end of the dock, oars shipped.");}
            case BOAT_STERN->{
                if(stage==0){cue(p,"Len's rowboat, tied off the end of the dock, oars shipped.");return true;}
                mark(p,"SawBoatMoved");
                if(stage==1){stage(l,r,relPos,2);mark(p,"FoundCostume");cue(p,"Under the tarp, bundled: the rubber mask, and the coverall rolled round it, soaked.");}
                else if(stage==2){stage(l,r,relPos,3);r.costumeTaken=true;give(p,thing("costume"));mark(p,"TookCostume");cue(p,"You lift the bundle out of the boat. It's heavy with water.");}
                else cue(p,"The rowboat, empty but for the tarp.");}
            case SUITCASE->{
                if(stage==0&&!r.planBurned){stage(l,r,relPos,1);r.planTaken=true;give(p,thing("plan"));mark(p,"TookPlan");cue(p,"Casey's bag. On top, folded in four, a sheet of paper in his capitals. You take it.");}
                else cue(p,"Casey's bag, open. Clothes, a phone charger, a bag of sweets.");}
            case SAFE->cue(p,stage==0?"A safe, its door shut.":"The safe, open.");
            case RECORDER->{
                var rec=r.recorders.remove(relPos.asLong());set(l,r.base,relPos,Blocks.AIR.defaultBlockState());
                var heard=new ListTag();String last="";
                if(rec!=null)for(var ev:r.events){if(ev.tick()<rec.start())continue;boolean loud=ev.key().equals("crash")||ev.key().equals("glass")||ev.key().equals("splash")||ev.key().startsWith("power");
                    if(Vec3.atCenterOf(ev.at()).distanceTo(Vec3.atCenterOf(relPos))>(loud?HEAR_LOUD:HEAR))continue;if(ev.key().equals(last))continue;last=ev.key();heard.add(StringTag.valueOf(ev.key()));}
                var s2=thing("player");var tag=s2.get(DataComponents.CUSTOM_DATA).copyTag();tag.put("Heard",heard);tag.putBoolean("Recorded",true);s2.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
                give(p,s2);mark(p,"RecordedTape");sound(l,pos,SoundEvents.LEVER_CLICK,.5F,1.2F);cue(p,"You stop the tape and pick the player up.");}
        }
        return true;
    }
    static boolean anyoneCarries(ServerLevel l,Run r,String what){for(var q:readers(l,r.base))if(carries(q,what))return true;return false;}
    static void play(ServerPlayer p,String which,SoundEvent song){
        Talk.close(p);var r=runOf(p);if(r==null)return;var l=p.serverLevel();
        stopMusic(l,r);stage(l,r,PinehookSite.STEREO,1);r.music=true;
        l.playSound(null,r.base.offset(PinehookSite.STEREO),song,SoundSource.RECORDS,1.4F,1F);
        mark(p,"PlayedMusic");note(p,"Music",which);log(r,PinehookSite.STEREO,"music");
        speak(l,r,r.casey,"Casey",PinehookTexts.MUSIC_ON,16);
        if(r.lenHere)speak(l,r,r.len,"Len",PinehookTexts.LEN_MUSIC,16);
    }
    static void film(ServerPlayer p,int which){
        Talk.close(p);var r=runOf(p);if(r==null)return;var l=p.serverLevel();
        r.film=which;stage(l,r,PinehookSite.TV,which);mark(p,"ChoseFilm");note(p,"Film",which==2?"rented":"casey");
        sound(l,r.base.offset(PinehookSite.TV),PinehookRegistry.TAPE_IN.get(),.8F,1F);
        speak(l,r,r.casey,"Casey",which==2?PinehookTexts.FILM_FAN:PinehookTexts.FILM_CASEY,16);
        if(which==2)speak(l,r,r.jude,"Jude",PinehookTexts.JUDE_FILM,16);
    }

    // ------------------------------------------------------------------------------------------------ the weekend's things in hand
    public static boolean useThing(ServerPlayer p,ItemStack s,@Nullable net.minecraft.world.entity.LivingEntity on){
        var r=runOf(p);if(r==null||p.isSpectator())return false;var l=p.serverLevel();
        switch(what(s)){
            case "torch"->{if(on!=null)return false;var tag=s.get(DataComponents.CUSTOM_DATA).copyTag();boolean lit=!tag.getBoolean("Lit");tag.putBoolean("Lit",lit);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
                sound(l,p.blockPosition(),SoundEvents.LEVER_CLICK,.5F,lit?1.4F:1.1F);cue(p,lit?"You click the torch on.":"You click the torch off.");return true;}
            case "camera"->{photograph(p,r);return true;}
            case "player"->{if(on!=null)return false;var tag=s.get(DataComponents.CUSTOM_DATA).copyTag();
                if(!tag.getBoolean("Recorded")){cue(p,"You put the headphones on. Somebody's tape, half a song, then hiss. You take them off again.");return true;}
                var heard=tag.getList("Heard",Tag.TAG_STRING);mark(p,"HeardTape");
                Talk.begin(p,TALK,"The portable player");Talk.line(p,"","You rewind it and listen.");
                if(heard.isEmpty())Talk.line(p,"","Hiss. Your own breathing, once, close to it.");
                for(int i=0;i<heard.size();i++){var key=heard.getString(i);Talk.line(p,"",PinehookTexts.heard(key));if(key.equals("nina_in"))mark(p,"TapeNina");}
                Talk.end(p);return true;}
            case "plan"->{if(on!=null)return false;mark(p,"ReadPlan");Talk.begin(p,TALK,"Casey's plan");for(var line:PinehookTexts.PLAN)Talk.line(p,"",line);Talk.end(p);return true;}
            case "costume"->{if(on!=null)return false;cue(p,"The rubber mask and the coverall, wet through. The mask's strap is elastic with a buckle at each end.");return true;}
            case "tie"->{if(on!=null)return false;cue(p,"A coverall's tie, torn off. Wet.");return true;}
            case "firstaid"->{if(on instanceof LiteraryActor a&&a.getUUID().equals(r.jude)&&r.outcome==WOUNDED&&!r.bandaged){
                    r.bandaged=true;mark(p,"HelpedJude");take(p,"firstaid");cue(p,"You get the dressing on the cut and press, hard. Jude hisses through his teeth and holds still.");
                    speak(l,r,r.jude,"Jude",PinehookTexts.JUDE_BANDAGED,10);return true;}
                if(on==null){cue(p,"Dressings, tape, scissors, a cold pack. Everything a weekend needs.");return true;}return false;}
            case "elastic"->{if(on!=null)return false;cue(p,"Black elastic, a buckle at each end, cut to a length. The kind that holds a mask on.");return true;}
            case "mixtape"->{if(on!=null)return false;cue(p,"A tape in a hand-lettered case. The songs are written out in Casey's capitals.");return true;}
            case "tape"->{if(on!=null)return false;cue(p,"A rented horror film, the box sun-faded. A camp, a lake, a long knife.");return true;}
            case "cooler"->{if(on!=null)return false;cue(p,"Heavy. It goes in the kitchen.");return true;}
            default->{return false;}
        }
    }
    public static boolean useThingOn(ServerPlayer p,ItemStack s,BlockPos pos){
        var r=runOf(p);if(r==null||p.isSpectator())return false;var l=p.serverLevel();var relPos=pos.subtract(r.base);
        if(what(s).equals("cooler")){
            var spot=PinehookSite.COOLER_SPOT;boolean kitchen=relPos.getX()>=3&&relPos.getX()<=7&&relPos.getZ()<=-29&&relPos.getZ()>=-33;
            if(!kitchen){cue(p,"Not here. It goes in the kitchen.");return true;}
            if(!at(l,r,spot).isAir()){cue(p,"There's no room for it.");return true;}
            take(p,"cooler");set(l,r.base,spot,PinehookBlock.of(Kind.COOLER,Direction.WEST,0));r.cooler="kitchen";r.coolerBy=null;mark(p,"SetCooler");
            sound(l,r.base.offset(spot),SoundEvents.WOOD_PLACE,1F,.6F);cue(p,"You set the cooler down by the counter. Your arms ache.");
            var casey=actor(l,r.casey);
            if(!r.secretTold&&r.phase==EVENING&&casey!=null&&casey.distanceTo(p)<10){r.secretTold=true;secret(p);}
            return true;
        }
        if(what(s).equals("player")){
            var at=pos.above();
            if(!l.getBlockState(at).isAir()){cue(p,"There's no room to set it down there.");return true;}
            take(p,"player");l.setBlock(at,PinehookBlock.of(Kind.RECORDER,p.getDirection().getOpposite(),1),Block.UPDATE_CLIENTS);
            r.recorders.put(at.subtract(r.base).asLong(),new Rec(p.getUUID(),r.tick));mark(p,"SetPlayer");sound(l,at,SoundEvents.LEVER_CLICK,.5F,1.4F);
            cue(p,"You set the player down and press record. The little light comes on.");return true;
        }
        if(what(s).equals("plan")&&relPos.closerThan(PinehookSite.HEARTH,1.5)){
            take(p,"plan");r.planBurned=true;r.planTaken=false;mark(p,"BurnedPlan");var h=r.base.offset(PinehookSite.HEARTH);l.sendParticles(ParticleTypes.FLAME,h.getX()+.5,h.getY()+.6,h.getZ()+.5,10,.2,.2,.2,.02);
            cue(p,"The paper catches at a corner, curls, and goes.");return true;
        }
        if(what(s).equals("firstaid")||what(s).equals("torch")||what(s).equals("camera")||what(s).equals("plan")||what(s).equals("costume")||what(s).equals("tie"))return useThing(p,s,null);
        return false;
    }
    /** Casey's secret, told quietly to whoever carried the cooler in. */
    static void secret(ServerPlayer p){
        Talk.begin(p,TALK,"Casey");Talk.line(p,"Casey",PinehookTexts.SECRET);
        Talk.ask(p,Talk.reply(PinehookTexts.SECRET_KEEP,q->{mark(q,"KeptSecret");Talk.line(q,"Casey",PinehookTexts.SECRET_KEEP_ANSWER);Talk.end(q);}),
            Talk.reply(PinehookTexts.SECRET_OBJECT,q->{mark(q,"Objected");Talk.line(q,"Casey",PinehookTexts.SECRET_OBJECT_ANSWER);Talk.end(q);}),
            Talk.reply(PinehookTexts.SECRET_WHO,q->{mark(q,"AskedJudeWho");mark(q,"KeptSecret");Talk.line(q,"Casey",PinehookTexts.SECRET_WHO_ANSWER);Talk.end(q);}));
    }
    /** The camera's flash: what was in front of it is kept on the reader's record. */
    static void photograph(ServerPlayer p,Run r){
        var l=p.serverLevel();sound(l,p.blockPosition(),PinehookRegistry.FLASH.get(),.8F,1F);
        var eye=p.getEyePosition();l.sendParticles(ParticleTypes.FLASH,eye.x+p.getLookAngle().x,eye.y+p.getLookAngle().y,eye.z+p.getLookAngle().z,1,0,0,0,0);
        mark(p,"Photographed");
        var jude=actor(l,r.jude);var nina=actor(l,r.nina);
        if(jude!=null&&!jude.isInvisible()&&jude.phase()==MASKED&&sees(p,jude.getEyePosition(),20,.8)){mark(p,"PhotographedMask");cue(p,"The flash catches the mask full on: rubber, crooked eyeholes, a seam at the jaw.");return;}
        if(nina!=null&&!nina.isInvisible()&&nina.phase()==MASKED&&sees(p,nina.getEyePosition(),64,.85)){mark(p,"PhotographedFigure");cue(p,"The flash goes off across the water. Too far. Maybe the picture will show something.");return;}
        // The night's evidence, if it is in front of the lens: kept on the reader's record.
        boolean any=false;
        for(var e:List.of(Map.entry("PhotoBlood",PinehookSite.DOCK_BLOOD),Map.entry("PhotoPrints",PinehookSite.WET_PRINTS),Map.entry("PhotoPrints",PinehookSite.BEACH_PRINTS),Map.entry("PhotoTie",List.of(PinehookSite.DOCK_TIE)),Map.entry("PhotoBoat",List.of(PinehookSite.STERN_WEST)),Map.entry("PhotoBlood",List.of(PinehookSite.SHED_BLOOD))))
            for(var rel:e.getValue()){var st=at(l,r,rel);if(!st.is(PinehookRegistry.FITTING.get()))continue;if(e.getKey().equals("PhotoBoat")&&PinehookBlock.stage(st)==0)continue;
                if(sees(p,Vec3.atCenterOf(r.base.offset(rel)),12,.7)){mark(p,e.getKey());any=true;break;}}
        cue(p,any?"The flash goes off. Whatever was in front of it is on the film now.":"The flash goes off. The camera whirs.");
    }
}
