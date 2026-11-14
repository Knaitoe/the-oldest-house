package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.524, the Pell house (docs/PELL_HOUSE_PEOPLE_0_4_524.md): its people, from the proposal's "The people", standing where the
 * first visit puts them (the proposal's "1. The placement": "Gail drops them off. June shows them round, and Callum watches from the end
 * of the hall").
 *
 * <ul>
 * <li><b>June Pell</b>, at the foot of her front step, beside the mat and just short of the line (she never stands on it): a
 * cardigan with its sleeves pushed up, her reading glasses on their chain, paint at the quick of two nails.</li>
 * <li><b>Gail Mercer</b>, the caseworker, on the path, her folder held to her chest and the lanyard round her neck, turned to June.</li>
 * <li><b>Tilly</b>, beside Gail on the path, her cane in her hand, her sunhat on and her backpack on her back. In this slice she is
 * always one of the house's people; a reader who takes the cane comes later.</li>
 * <li><b>Callum</b>, at the far end of the hall, barefoot, watching the front door.</li>
 * </ul>
 *
 * <p>They turn their heads to a reader near them, every tick, a little at a time; Callum watches one anywhere in the house or on
 * the lot, from further off, and turns his whole body to keep them in sight. A hand on
 * June, Gail or Tilly gets the next of their lines, as a caption over them (never chat). Callum does not speak: a hand on him gets
 * what he does instead. Nothing else of the visits is here yet: no dinner, no night, no copying, no line-walking.
 *
 * <p>They are the house's for everyone at it: every reader at the same house sees the same four. Each is made where they stand on
 * the first visit if they are not there; one already there (after a reload, say) is taken up again, never made twice.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PellPeople {
    private PellPeople(){}
    /** Stranger phases, from this one up: June, Tilly, Callum, Gail. */
    public static final int FIRST=195,JUNE=0,TILLY=1,CALLUM=2,GAIL=3,COUNT=4,
        /** 0.4.540: Nessa, in the freezer (PellFreezer). Drawn as one of them, but not one of the four the visit moves (COUNT). */
        NESSA=4;
    private static final String[] SKINS={"june","tilly","callum","gail","nessa"};
    public static boolean cast(int phase){return phase>=FIRST&&phase<=FIRST+NESSA;}
    public static String skin(int phase){return "pell_"+SKINS[phase-FIRST];}
    /** How big each is: the two grown women at full size, the two children smaller, Callum the smaller of them. */
    public static double size(int phase){return switch(phase-FIRST){case TILLY,NESSA->.62;case CALLUM->.58;default->1;};}
    /** What each has in hand is drawn: Tilly's cane, Gail's folder, and (0.4.530, which found it was never drawn) whatever June is
     *  holding, the saucepan at dinner, the hairbrush at bedtime and the tin, the brush and the lantern at night. */
    public static boolean carries(int phase){return cast(phase);}
    public static boolean is(@Nullable Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static int who(LiteraryActor a){return a.phase()-FIRST;}
    /** What each is called over what they say. */
    public static String name(int who){return switch(who){case JUNE->"June";case TILLY->"Tilly";case CALLUM->"Callum";case NESSA->"Nessa";default->"Gail";};}
    /** Their gestures, for the renderer: Tilly's cane held out ahead of her, Gail's folder held to her chest; from 0.4.527, asleep (lying
     *  along a bed), June brushing Tilly's hair, and Tilly having it brushed; from 0.4.530, June kneeling at the line with the brush,
     *  and Callum crouched (as a reader crouches, a beat late). */
    /** 0.4.566: one stroke of June's hairbrush at bedtime, in ticks of the level's time: it starts down Tilly's hair as the time passes
     *  a multiple of it, so the brush's sound (PellVisit.brushing) and both of their poses (LiteraryRenderers) keep to one stroke. */
    public static final int BRUSH_STROKE=40;
    public static final int CANE=1,FOLDER=2,ASLEEP=3,BRUSH=4,BRUSHED=5,PAINT=6,CROUCH=7,
        // 0.4.532: June on the telephone, the receiver to her ear; and holding it out for a reader to take.
        PHONE=8,OFFER=9,
        // 0.4.533: June at first light, a week in: in her swimming things with her robe on, and in the water swimming her lengths.
        ROBE=10,SWIM=11,
        // 0.4.534: Callum struggling for the knife with a reader, and his fist coming at their face.
        STRUGGLE=12,LUNGE=13,
        // 0.4.538: Callum on the last night, sitting on the grass inside the line eating what he was given, and sat there after.
        EATING=14,SITTING=15,
        // 0.4.539, midnight in the pool: June in the water holding Tilly by the shoulders, pressing her under, and standing with her arms
        // down; June leading Tilly by the hand, in her robe and in her swimming things; and Tilly with her hand held out in someone's.
        HOLD=16,PRESS=17,WADE=18,LEADS=19,LEADS_SWIM=20,LED=21,
        // 0.4.540, the freezer: June with her hand held out in a reader's, in her swimming things; Nessa lying still in it; and June sat
        // on the stool beside it, her hand on the plastic.
        JUNE_LED=22,STILL=23,BY_HER=24,
        // 0.4.541, first light: Callum sat on the front step with his shoes on; Tilly at Nessa's window, waving.
        STEP=25,WAVE=26,
        // 0.4.544: Gail at first light holding a page out from her folder, the folder kept to her chest in her other hand (PellPages).
        SHOW=27,
        // 0.4.567: June walking a reader out of the shed by the arm, a step ahead of them, her right hand back to theirs.
        SHOWING=28;
    /** 0.4.535: the marks a look follows (LiteraryActor.marks): June's right hand wrapped, after the laundry (PellHand). */
    public static final int WRAPPED=1;
    /** 0.4.533: the sheet one of them wears, if it is not their own: June in her robe, and in the water. Her gesture picks it. */
    public static @Nullable String dressed(int phase,int gesture){return dressed(phase,gesture,0);}
    /** As above, with the marks they carry: by day, after the laundry, June with her hand wrapped. */
    public static @Nullable String dressed(int phase,int gesture,int marks){
        // 0.4.541: on the front step at first light, Callum has his shoes on.
        if(phase==FIRST+CALLUM)return gesture==STEP?"pell_callum_shoes":null;
        if(phase!=FIRST+JUNE)return null;
        // 0.4.539: in the water and leading Tilly down to it, in her swimming things; leading her in from the gate, in her robe.
        return gesture==ROBE||gesture==LEADS?"pell_june_robe":gesture==SWIM||gesture==HOLD||gesture==PRESS||gesture==WADE||gesture==LEADS_SWIM||gesture==JUNE_LED||gesture==BY_HER?"pell_june_swim":(marks&WRAPPED)!=0?"pell_june_hand":null;
    }

    /** Where each stands on the first visit, and which way they face (degrees; 0 is south, toward the street). */
    public record Spot(double x,double z,float look){}
    public static final Spot[] FIRST_VISIT={
        new Spot(1.5,-55.5,0),      // June: at the foot of her front step, beside the mat and short of the line, looking down the path
        new Spot(.5,-53,180),       // Tilly: on the path, turned to the house
        new Spot(-12.5,-65.5,-90),  // Callum: the far end of the hall, looking along it to the front door
        new Spot(1.5,-53.5,180)};   // Gail: on the path beside Tilly, turned to June
    /** How near a reader is for June, Gail and Tilly to turn their heads to them, and for Callum to watch them. */
    public static final double NEAR=8,WATCH=18;
    /** How far a head turns from where the body faces, in degrees, before the body comes round too (Callum's does). */
    public static final float HEAD_TURN=70;

    /** The four at one house: who they are by body; and, from 0.4.525, where the visit is (PellVisit). */
    static final class Household {
        final UUID[] ids=new UUID[COUNT];long lastTick=-1;final int[] said=new int[COUNT];
        /** Which way each faces when at rest, and who walked this tick (their heads are the walk's). */
        final float[] rest=new float[COUNT];final boolean[] moving=new boolean[COUNT];
        /** The visit: begun again from the start when fresh; how far it has got; when the next line may come. */
        boolean fresh=true,gailGone,tillyHome;int step,waypoint,gailStep,tillyStep,stuck,empty;long next;UUID car;
        /** 0.4.566: the visit June last sent the readers out on, once its part was over (PellHouse.leaving). */
        int sentOut;
        /** 0.4.567, the shed kept June's at night (PellNight.shutShed): when she asked whoever was in it to come out; who she has gone
         *  to walk out, since when, and whether she has hold of them yet; the way they are drawn along behind her and how far along it
         *  they are; how far she is along her own way out; whether her tin is down by the door while she does it, and whether the shed
         *  is shut and padlocked behind them. */
        long askedOut,outSince;UUID outWith;boolean outHeld,tinDown,shedShut;int outStep;double towed;
        final java.util.List<Vec3> tow=new java.util.ArrayList<>();
        /** Where June has walked, newest last, for Tilly to follow. */
        final java.util.ArrayDeque<Vec3> trail=new java.util.ArrayDeque<>();
        /** 0.4.526: the house's own hour, and whether it is going on; who has been sent it. */
        long time=PellVisit.AFTERNOON;int rate;final java.util.Set<UUID> told=new java.util.HashSet<>();
        /** 0.4.526: who is sitting, and in which of the table's places (PellVisit.TABLE); where each is walking to, and what they do
         *  when they get there (sit at a place, or stand facing a way). */
        final boolean[] seated=new boolean[COUNT];final int[] place={-1,-1,-1,-1};
        @SuppressWarnings("unchecked") final java.util.ArrayDeque<Vec3>[] way=new java.util.ArrayDeque[COUNT];
        final int[] sitAt={-1,-1,-1,-1};final float[] endLook=new float[COUNT];final int[] stuckFor=new int[COUNT];
        /** 0.4.526, dinner: each place's plate (PellVisit.NONE to SCRAPED), its body on the table, who eats there (a person, a reader,
         *  or nobody), the order June serves in, and when it was called, served and cleared. */
        final int[] plate=new int[4];final UUID[] plates=new UUID[4];final int[] diner={-1,-1,-1,-1};
        final java.util.List<Integer> order=new java.util.ArrayList<>();int serving;long calledAt,servedAt,clearedAt;
        /** 0.4.527, bedtime: who sits where away from the table (June's chair, the edge of Nessa's bed) and where a walker sits or lies
         *  down at the end of their way; who is asleep; which of the outside doors June has locked; whether she has asked for her chair
         *  back, how far the brushing and the locking up have got, and what she has said while the tape plays; when the tape's sound
         *  last began, the brushing began, and she last asked a reader to go out. */
        final PellVisit.Perch[] perch=new PellVisit.Perch[COUNT],perchTo=new PellVisit.Perch[COUNT];
        final Vec3[] sleepAt=new Vec3[COUNT];final boolean[] asleep=new boolean[COUNT];
        final java.util.Set<net.minecraft.core.BlockPos> locked=new java.util.HashSet<>();
        boolean chairSaid;int brushSaid,lockIndex,watchSaid;long tapeAt,brushedAt,nudgedAt;
        /** 0.4.530, the first night (PellNight): how far June has got along the thin stretch (an index into PellNight.CELLS) and when
         *  she knelt at the cell she is on; whether she has spoken to a reader who came near, about the paint under a foot, or to a reader
         *  standing where she is painting; what a hand on her has had; whether Callum is out on the line; and his copy of the reader
         *  nearest him, a beat behind: how far their look was turned off him, their pitch, whether they crouched, and whose. */
        int cell,nightSaid,feetSaid=-1;long paintedAt;boolean noticed,smearSaid,smearDue,callumOut;
        final float[] copyYaw=new float[PellNight.BEAT+1],copyPitch=new float[PellNight.BEAT+1];final boolean[] copyCrouch=new boolean[PellNight.BEAT+1];
        int copied;UUID copyOf;
        /** 0.4.531, the first morning (PellMorning): whether every reader at the house is asleep, so the night hurries on; when each
         *  reader lying on the camp bed lay down; whether first light has come; how far what is said at breakfast has got; when Tilly sat
         *  down to it and when it was called; whether June has called Callum in to it. */
        boolean hurried,dawned,callumCalled;int morningSaid;long tillySat,breakfastAt;
        final Map<UUID,Long> sleptAt=new HashMap<>();
        /** 0.4.532, Gail's call (PellCall): when the telephone began to ring, when June last held it out, and when the reader who has it
         *  took it; who that is, and who lifted it before June got to it; how far June's side of the call has got, and whether she told
         *  Gail somebody was up in the night; who has had a turn, and what each said. */
        long ringAt,offeredAt,heldAt;UUID caller,answered;int callSaid;boolean toldUp;
        final java.util.Set<UUID> spoke=new java.util.HashSet<>();final Map<UUID,String> toldGail=new HashMap<>();
        /** 0.4.533, a week in (PellWeek): which of the house's visits it is playing (1 the placement, 2 a week in); whether June has
         *  called hello to the first reader in, on the second; how far her swim at first light has got (PellSwim), how many lengths she
         *  has swum and when she began the one she is on, whether she is done and in, and when she came in. */
        int visit=1,swim,lengths;boolean welcomed,swum;long lengthAt,swumAt;
        /** 0.4.534, the knife (PellKnife): how far it has got and when that began; whether June has spoken after it, and how far her way
         *  to lock up has got; who has hold of Callum, and since when a reader has stood close to him; who June found in the kitchen. */
        int knife,knifeSaid,knifeWay;long knifeAt,closeSince;UUID fighter;final java.util.Set<UUID> found=new java.util.HashSet<>();
        /** 0.4.535, June's hand (PellHand): how far it has got, when she got to the laundry and when she last shut herself in; how many
         *  of her lines about it a hand on her has had; whom she will say did it; and, on the week's call, what each reader told Gail
         *  about it. */
        int hand,handSaid;long handAt,shutAt;UUID blamed;final Map<UUID,String> toldHand=new HashMap<>();
        /** 0.4.536, the tapes (PellTapes): which tape is in the shed's deck, how many of its lines have been said, and the reading pace
         *  they keep; whether it is playing and when it began; who has watched it since it began, and who has been sent it. */
        int deck=PellTapes.IN_DECK,tapeLine,tapePace=100;boolean tapePlaying;long tapeStart;
        final java.util.Set<UUID> tapeWatchers=new java.util.HashSet<>(),tapeTold=new java.util.HashSet<>();
        /** 0.4.538, the last night (PellLastNight, PellScrub): whether Callum is out on the line for it, has been fed, has said his name,
         *  when he sat down to eat, and which cell of the thin stretch he last stepped onto; where the line was first broken; each
         *  reader's work with the brush; and who has had a plate from the fridge. */
        boolean callumStrip,callumFed,callumSaid;long fedAt;int guardX=PellLastNight.STRIP_X;BlockPos gap;
        final Map<UUID,PellScrub.Work> scrub=new HashMap<>();final java.util.Set<UUID> plated=new java.util.HashSet<>();
        /** 0.4.539, midnight in the pool (PellRite): how far it has got; whether June has Tilly in her hands, how long Tilly has held out
         *  there, whether she is under and since when, and when June last called; whether Tilly owes an answer to the last call and
         *  whether June's next word follows her coming up; how many calls, replies and takings-back there have been; who has Tilly's
         *  hand, when it was let go and when the paint last stopped her; where June holds her; and every line said, for tests. */
        int rite,calls,replies;boolean inHold,under,reply,afterUnder,tookBack;long heldFor,underAt,callAt,letGoAt,lineToldAt;UUID leader;
        double holdX,holdZ;final java.util.List<String> riteLines=new java.util.ArrayList<>();
        /** 0.4.539: whose hands have had Tilly's tonight. */
        final java.util.Set<UUID> hands=new java.util.HashSet<>();
        /** 0.4.540, the freezer (PellFreezer, PellRite): whether June's lanyard is out of her robe's pocket; Nessa's body; who has June's
         *  hand, where they have walked since (for her to follow), and when she last said the name while led; whether she sits by the
         *  freezer. */
        boolean lanyardOut,juneSat;UUID nessa,juneLeader;long juneCalledAt;final java.util.ArrayDeque<Vec3> juneTrail=new java.util.ArrayDeque<>();
        /** 0.4.541, first light (PellDawn): where the morning has got to; the car's leg up the street; what has been said at the curb;
         *  who is where; when Gail came; the readers at the house when the night's end came, those gone to Gail's car, those whose black
         *  has begun, and when each one's black begins. */
        int dawn,carLeg,gailSaid;boolean endTaken,callumStepped,tillyWithGail,atWindow;long comeAt;
        final java.util.Set<UUID> atEnd=new java.util.HashSet<>(),inCar=new java.util.HashSet<>(),faded=new java.util.HashSet<>();
        final java.util.Map<UUID,Long> leaveAt=new java.util.HashMap<>();
        /** 0.4.544, the torn page (PellPages): whose page Gail is holding out to them, which page and which copy of it, since when, and
         *  whether they have put a hand on it to read it. */
        UUID showing;int showPage,showSerial;long showAt;boolean showRead;
        /** 0.4.545, a reader as Tilly (PellTilly): who is Tilly here now; where they last stood off the paint in the water; when they
         *  were last taken up by June's Tilly, last told of the paint, and first told June was at Nessa's bed; when the name was last put
         *  to them on the panel, whether they answered, and whether to the name; when bedtime was called; whether, kept, they have been
         *  set down in the street. */
        UUID tillyReader;Vec3 tillySafe;long tillyMounted,tillyPaintAt,tillyFetchAt,tillyAskedAt,bedAt;boolean tillyAnswered,tillySaidYes,tillySetDown;
        /** 0.4.546, Tilly's senses (PellSenses): when the reader as Tilly last tapped, and last heard the hum and the hiss; when the singing
         *  comes next; whether they stood on the line a moment ago; how many of June's quiet lines this call has had, and after which of
         *  her lines the last one came. */
        long tappedAt,hummedAt,hissedAt,singNext;boolean onLine;int hushed,hushAfter;
        /** 0.4.548, Gail's third mark (PellMark): how far the last night's framing has got; whom June will say broke the photograph;
         *  when it began (and each step since), when she put the receiver down, when Gail came up the path; how far through June's call
         *  and Gail's words; the car's leg up the street; who has Gail's hand to the shed, the way they walked and the way Gail has, how
         *  long she has watched a tape and since when she stood still; which of the page and the note she has; who has gone to her car,
         *  whose black has begun and when each begins; whom she took. */
        int mark,markSaid,gailNightSaid,nightLeg,gailHas;UUID marked,gailLeader;long markAt,hungUpAt,gailUpAt,gailWatched,gailStillAt;
        String markedName="";boolean gailOff,gailWalkedBack;
        final java.util.ArrayDeque<Vec3> gailTrail=new java.util.ArrayDeque<>(),gailWalked=new java.util.ArrayDeque<>();
        final java.util.Set<UUID> nightInCar=new java.util.HashSet<>(),nightFaded=new java.util.HashSet<>(),took=new java.util.HashSet<>();
        final java.util.Map<UUID,Long> nightLeave=new java.util.HashMap<>();
        Household(){for(int i=0;i<COUNT;i++){rest[i]=FIRST_VISIT[i].look();way[i]=new java.util.ArrayDeque<>();}}
    }
    private static final Map<BlockPos,Household> HOUSES=new HashMap<>();
    public static void clearAll(){for(var b:List.copyOf(HELD.keySet()))release(b);HOUSES.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){HELD.clear();}

    // ------------------------------------------------------------------------------------------------ 0.4.544, the lot held
    /**
     * While anybody is at the house its whole lot, street to back fence, is kept loaded, entities and all: the visit moves people across
     * all of it (June out to the pool, Callum to the back of the line), and somebody moved onto ground whose entities are not loaded drops
     * out of the house until it loads. Run 1578 found Callum so moved on the last night, and a second Callum made where the first visit
     * has him.
     */
    private static final TicketType<ChunkPos> TICKET=TicketType.create("pell_house",java.util.Comparator.comparingLong(ChunkPos::toLong));
    private record Held(ServerLevel level,List<ChunkPos> chunks){}
    private static final Map<BlockPos,Held> HELD=new HashMap<>();
    static void hold(ServerLevel l,BlockPos b){
        if(HELD.containsKey(b))return;var box=area(b);var chunks=new java.util.ArrayList<ChunkPos>();
        for(int x=Mth.floor(box.minX)>>4;x<=Mth.floor(box.maxX)>>4;x++)for(int z=Mth.floor(box.minZ)>>4;z<=Mth.floor(box.maxZ)>>4;z++){var c=new ChunkPos(x,z);l.getChunkSource().addRegionTicket(TICKET,c,2,c);chunks.add(c);}
        HELD.put(b.immutable(),new Held(l,List.copyOf(chunks)));
    }
    static void release(BlockPos b){var held=HELD.remove(b);if(held!=null)for(var c:held.chunks())held.level().getChunkSource().removeRegionTicket(TICKET,c,2,c);}
    /** Whether every part of the lot has its entities loaded: until it has, nobody is made and no visit begins. */
    static boolean ground(ServerLevel l,BlockPos b){return RoomActors.ready(l,area(b));}
    /** The house as if everybody had gone: its visit begins again from the start when a reader is next at it. */
    public static void vacate(BlockPos b){household(b).fresh=true;}
    static Household household(BlockPos b){return HOUSES.computeIfAbsent(b.immutable(),k->new Household());}

    public static AABB area(BlockPos b){return new AABB(b.getX()+PellSite.WEST,b.getY()-4,b.getZ()+PellSite.NORTH,b.getX()+PellSite.EAST+1,b.getY()+PellSite.ROOF+2,b.getZ()+2);}
    private static boolean live(ServerLevel l,Vec3 at){var cell=BlockPos.containing(at);return l.isLoaded(cell)&&l.areEntitiesLoaded(ChunkPos.asLong(cell));}
    static Vec3 world(BlockPos b,Spot s){return new Vec3(b.getX()+s.x(),b.getY(),b.getZ()+s.z());}

    /** One of them at this house, if their body is here. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var h=household(b);
        if(h.ids[who]!=null&&l.getEntity(h.ids[who]) instanceof LiteraryActor a&&a.isAlive()&&is(a)&&who(a)==who)return a;
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),PellPeople::is))if(who(old)==who&&old.isAlive()){h.ids[who]=old.getUUID();return old;}
        return null;
    }
    /**
     * Each of the four at the house, made where the first visit puts them if they are not there. Nothing is made while any of the lot's
     * ground has its entities unloaded (0.4.544: anywhere on it, not only where they are made), so nobody is made twice over one who has
     * yet to load.
     */
    /** 0.4.541: one of the four made at this place, looking this way, holding what they hold on the first visit; null if not made. */
    static @Nullable LiteraryActor make(ServerLevel l,BlockPos b,int who,Vec3 rel,float look){
        var at=PellVisit.world(b,rel);if(!ground(l,b)||!live(l,at))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PellHouse.PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
        a.moveTo(at.x,at.y,at.z,look,0);a.setYHeadRot(look);a.setYBodyRot(look);
        if(who==TILLY){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.CANE.get()));a.gesture(CANE);}
        if(who==GAIL){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.FOLDER.get()));a.gesture(FOLDER);}
        if(!l.addFreshEntity(a))return null;
        household(b).ids[who]=a.getUUID();
        return a;
    }
    public static void ensure(ServerLevel l,BlockPos b){
        var h=household(b);hold(l,b);if(!ground(l,b))return;
        // 0.4.533: a week in, Gail does not come with them (PellWeek); she is not made for a visit about to begin a week in.
        boolean noGail=h.fresh?PellWeek.visitFor(PellHouse.readers(l,b))>=2:h.gailGone;
        for(int who=0;who<COUNT;who++){
            // 0.4.525: Gail, once she has gone, is not made again until the visit begins again.
            if(who==GAIL&&noGail)continue;
            if(find(l,b,who)!=null)continue;
            var at=world(b,FIRST_VISIT[who]);if(!live(l,at))continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
            a.bind(PellHouse.PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
            float look=FIRST_VISIT[who].look();
            a.moveTo(at.x,at.y,at.z,look,0);a.setYHeadRot(look);a.setYBodyRot(look);
            if(who==TILLY){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.CANE.get()));a.gesture(CANE);}
            if(who==GAIL){a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.FOLDER.get()));a.gesture(FOLDER);}
            if(!l.addFreshEntity(a))continue;
            h.ids[who]=a.getUUID();
        }
        // 0.4.540: and Nessa, in the freezer.
        PellFreezer.ensure(l,b);
    }
    /** None of the four is at the house, nor Nessa: for a house built again from nothing, and for tests. */
    public static void reset(ServerLevel l,BlockPos b){
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),PellPeople::is))old.discard();
        PellVisit.clearPlates(l,b);PellWeek.rope(l,b,false);
        HOUSES.remove(b);
    }

    /**
     * Every game tick, each house with a reader at it. The scene's own reader tick comes a quarter second apart, and heads turned on
     * it would turn in steps.
     */
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        if(HOUSES.isEmpty())return;var l=e.getServer().getLevel(NovelRooms.dimension(PellHouse.PLACE));if(l==null)return;
        for(var entry:List.copyOf(HOUSES.entrySet())){
            var b=entry.getKey();var h=entry.getValue();var here=PellHouse.readers(l,b);
            // 0.4.525: a house left empty long enough begins its visit again when somebody next comes.
            if(here.isEmpty()){if(++h.empty>=PellVisit.EMPTY){h.fresh=true;release(b);}continue;}
            h.empty=0;hold(l,b);tick(l,b,here);
        }
    }
    /** Once a tick for the house, however many readers are at it: each of them looks at the nearest reader near enough to look at. */
    public static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=household(b);long now=l.getGameTime();if(h.lastTick==now)return;h.lastTick=now;
        // 0.4.542: while the house stands as its aftermath there is nobody in it, and no visit (PellAftermath).
        if(PellAftermath.tick(l,b,h,here))return;
        // 0.4.545: who is Tilly here, and what she is doing, which is what that reader is doing (PellTilly).
        PellTilly.before(l,b,h,here);
        // 0.4.525: the visit moves them first; whoever walked this tick has their head where the walk put it.
        PellVisit.tick(l,b,h,here);
        // 0.4.545: Tilly where that reader is, or that reader where June leads her.
        PellTilly.after(l,b,h,here,now);
        for(int who=0;who<COUNT;who++){
            // Anybody asleep is left as they lie (0.4.527); a reader as Tilly turns their own head.
            if(h.moving[who]||h.asleep[who]||who==TILLY&&h.tillyReader!=null)continue;
            var a=find(l,b,who);if(a==null)continue;
            double reach=who==CALLUM?WATCH:NEAR;ServerPlayer near=null;double best=reach*reach;
            for(var p:here){if(p.isSpectator()||!p.isAlive()||p.isSleeping())continue;double d=a.distanceToSqr(p);if(d<best){best=d;near=p;}}
            float rest=h.rest[who];
            if(near==null){turnHead(a,rest,rest);continue;}
            float toward=yawTo(a,near.getEyePosition());
            // Callum turns to follow whoever he is watching; the others turn only their heads, as far as a head turns, and their
            // bodies settle the way they stand. Anybody sitting at the table stays turned to it.
            float body=who==CALLUM&&!h.seated[who]?toward:rest;
            turnHead(a,body,toward);
        }
    }
    static float yawTo(Entity a,Vec3 at){var d=at.subtract(a.position());return (float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;}
    /** Body to this yaw, and the head toward the other, no more than HEAD_TURN off the body, a little at a time. */
    private static void turnHead(LiteraryActor a,float body,float head){
        float b=Mth.approachDegrees(a.getYRot(),body,6);a.setYRot(b);a.setYBodyRot(b);
        float want=b+Mth.clamp(Mth.wrapDegrees(head-b),-HEAD_TURN,HEAD_TURN);
        a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),want,12));
    }

    /** A hand on one of them: June, Gail and Tilly say the next of their lines; Callum does something instead. */
    public static void talk(ServerPlayer p,LiteraryActor a,net.minecraft.nbt.CompoundTag own){
        if(!is(a))return;var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null)return;
        int who=who(a);var h=household(b);
        // 0.4.540: Nessa, in the freezer.
        if(who==NESSA){PellHouse.cue(p,PellTexts.NESSA_HAND);own.putBoolean("SawNessa",true);return;}
        // 0.4.548: in the night, Gail come for the reader June named (PellMark).
        if(PellMark.talk(p,b,h,a,who))return;
        // 0.4.541: at first light, with Gail back (PellDawn).
        if(PellDawn.talk(p,b,h,a,who))return;
        // 0.4.527: asleep, they are only looked at.
        if(h.asleep[who]){PellHouse.cue(p,who==CALLUM?PellTexts.CALLUM_ASLEEP:PellTexts.TILLY_ASLEEP);return;}
        // 0.4.538: something to eat held out to him: on the last night he takes it (PellLastNight).
        if(who==CALLUM&&PellLastNight.feed(p,b,h,a,own))return;
        // 0.4.539: at midnight, a hand on Tilly in the water takes hers; June does not stop for anybody (PellRite).
        if(who==TILLY&&PellRite.hand(p,b,h,a,own))return;
        if(who==JUNE&&PellRite.june(p,b,h,a,own))return;
        // 0.4.534: at the sink with the knife, a hand on him takes his wrist.
        if(who==CALLUM&&PellKnife.hand(p,b,h,own))return;
        // 0.4.538: on the line on the last night he does not move for the hand; eating, he does not look up.
        if(who==CALLUM&&h.callumStrip){var lines=h.callumFed?PellTexts.CALLUM_EATING:PellTexts.CALLUM_ON_LINE;PellHouse.cue(p,lines[h.said[who]++%lines.length]);return;}
        // 0.4.530: on the line at night, Callum stands and does not move for the hand.
        if(who==CALLUM&&h.callumOut){PellHouse.cue(p,PellTexts.CALLUM_NIGHT[h.said[who]++%PellTexts.CALLUM_NIGHT.length]);own.putBoolean("TouchedCallumAtNight",true);return;}
        if(who==CALLUM){var lines=h.seated[who]?PellTexts.CALLUM_TABLE:PellTexts.CALLUM_HAND;PellHouse.cue(p,lines[h.said[who]++%lines.length]);return;}
        // 0.4.532: on the telephone to Gail she lifts a finger; holding it out, the reader takes it.
        if(who==JUNE&&PellCall.june(p,b,own))return;
        // 0.4.533: in the water at first light she swims on.
        if(who==JUNE&&PellSwim.inWater(b)){PellHouse.cue(p,PellTexts.JUNE_SWIMMING);return;}
        if(a.saying())return;
        // 0.4.533: a week in, June has other things to say by day than her first day's.
        var lines=switch(who){case JUNE->h.visit>=2?PellTexts.JUNE_WEEK_TALK:PellTexts.JUNE_TALK;case TILLY->h.visit>=2?PellTexts.TILLY_WEEK_TALK:PellTexts.TILLY_TALK;default->PellTexts.GAIL_TALK;};
        String line;
        // 0.4.527: in her chair with the tape on, June talks about the girl on it first.
        if(who==JUNE&&PellVisit.watching(b)&&h.watchSaid<PellTexts.JUNE_WATCHING.length)line=PellTexts.JUNE_WATCHING[h.watchSaid++];
        // 0.4.534: after the knife, on her way to lock up.
        else if(who==JUNE&&PellKnife.after(b))line=PellTexts.JUNE_KNIFE_HAND;
        // 0.4.530: up at night with the tin, June has her own lines, and the reader keeps that they spoke to her.
        else if(who==JUNE&&PellNight.up(b)){line=PellTexts.JUNE_NIGHT[h.nightSaid++%PellTexts.JUNE_NIGHT.length];own.putBoolean("SpokeToJuneAtNight",true);}
        // 0.4.531: up in the morning, from when she gets up until breakfast is cleared.
        else if(who==JUNE&&PellMorning.morning(b)){line=PellTexts.JUNE_MORNING[h.nightSaid++%PellTexts.JUNE_MORNING.length];}
        // 0.4.533: in her robe, on her way down to the pool or back from it.
        else if(who==JUNE&&a.gesture()==ROBE){line=PellTexts.JUNE_SWIM_TALK[h.nightSaid++%PellTexts.JUNE_SWIM_TALK.length];own.putBoolean("SpokeToJuneAtThePool",true);}
        // 0.4.535: her hand wrapped, she has a word about it for the first hands on her.
        else if(who==JUNE&&PellHand.hurt(b)&&h.handSaid<PellTexts.JUNE_HAND_TALK.length){line=PellTexts.JUNE_HAND_TALK[h.handSaid++];own.putBoolean("AskedJuneAboutHand",true);}
        else line=lines[h.said[who]++%lines.length];
        a.say(name(who),line);a.speaks(line);
        // 0.4.525: June's rules count however they are heard: from a hand on her too. The reader whose hand it is keeps it in the
        // record the talk saves after this (LiteraryVignettes.talk), so it is written there and not saved under it.
        PellVisit.heard(a,PellTexts.rule(line),PellHouse.readers(p.serverLevel(),b),p,own);
    }
}
