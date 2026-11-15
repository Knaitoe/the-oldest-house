package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.602, The Ones Already Inside (docs/THE_ONES_ALREADY_INSIDE_PLAN.md), its third slice: "The four: Rae, Dan, Vera and Daniel
 * painted and standing, talking in the panel; the failed bodies drawn." Everybody is where the script's first scene has them.
 *
 * <ul>
 * <li><b>Rae</b>, in the front hall in the coat she came in, turned to the front door (N1: "Rae is in the hall when the first reader
 * comes in").</li>
 * <li><b>Dan</b>, on the hall bench in his coat, one hand in his pocket.</li>
 * <li><b>Vera</b>, upstairs in the workroom at the table of preparations, folding cloths. She comes down in N2.</li>
 * <li><b>Daniel</b>, in his room, held standing in the frame in its back wall, his wrists up at the straps either side of it.</li>
 * <li><b>Three failed bodies</b>: one sat at the foot of the dead post in the cellar, tied to it by the wrists (N9); one in the upper
 * east passage with a hand flat on a guest room's hatch; one in the ground floor's west passage at the jib door, its fingers round
 * the door's frame (N4: the first comes through from the passages).</li>
 * </ul>
 *
 * <p>Rae, Dan, Vera and Daniel turn their heads to a reader near them whom they can see, and not their bodies. A hand on any of
 * the four opens the panel with the first exchange the script gives them (N1 for Rae and Dan, N2's opening for Vera, N3 for
 * Daniel); what a reader answers is kept in that reader's own record and binds only them. Nothing acts on an answer yet: Rae does
 * not go up the stair, Vera does not come down, Daniel is not let out. A hand on a failed body gets what it is doing, in the grey of
 * the house's other lines. They do not turn to anybody.
 *
 * <p>They are the house's, for everyone in it: every reader in the same house sees the same seven. Each is made where they belong
 * if they are not there; one already there (after a reload) is taken up again, never made twice, and nobody is made until every
 * part of the house has its entities loaded.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LodgingPeople {
    private LodgingPeople(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.ONES_INSIDE;
    /** Stranger phases, from this one up: Rae, Dan, Vera, Daniel, then the failed bodies (the post's, the dress, the shirt). */
    public static final int FIRST=200,RAE=0,DAN=1,VERA=2,DANIEL=3,FAILED_POST=4,FAILED_DRESS=5,FAILED_SHIRT=6,COUNT=7;
    private static final String[] SKINS={"rae","dan","vera","daniel","failed_post","failed_dress","failed_shirt"};
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+COUNT;}
    public static String skin(int phase){return "lodging_"+SKINS[phase-FIRST];}
    public static boolean failed(int who){return who>=FAILED_POST;}
    /** Who wears a skirt to the ankle: Vera, and the failed body in the dress. */
    public static boolean gown(int phase){return phase==FIRST+VERA||phase==FIRST+FAILED_DRESS;}
    /** The failed bodies are a little the wrong size under their clothes: one short, one long. */
    public static double size(int phase){return switch(phase-FIRST){case FAILED_DRESS->.94;case FAILED_SHIRT->1.07;default->1;};}
    public static boolean is(@Nullable Entity e){return e instanceof LiteraryActor a&&a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static int who(LiteraryActor a){return a.phase()-FIRST;}
    /** What each of the four is called over what they say. Daniel has no name for a reader until he has given it. */
    public static String name(int who,CompoundTag own){return switch(who){case RAE->"Rae";case DAN->"Dan";case VERA->"Vera";default->own.getBoolean(KNOWS_DANIEL)?"Daniel":LodgingTexts.MAN_IN_FRAME;};}
    /**
     * Their gestures, for the renderer (client.LiteraryRenderers.CastModel.lodging): Rae's hands together; Dan's hand in his pocket,
     * and held out to be looked at; Vera's hands at work on the table; Daniel's wrists up at the frame's straps; a failed body's arms
     * up behind it to the post, a palm flat on a hatch, fingers round a door's frame.
     */
    public static final int WAIT=1,POCKET=2,SHOW=3,WORK=4,HELD=5,TIED=6,HATCH=7,FRAME=8,STRAIN=9;
    /**
     * 0.4.606 (N4, LodgingInjury): a failed body battering a door with both fists, walking with its arms out, walking somebody back by
     * the arm; Dan holding his cut hand to him; Vera with the pin raised to Dan's hand.
     */
    public static final int BATTER=10,REACH=11,DRAG=12,HURT=13,PIN=14;
    /** 0.4.606: the sheet a man's marks put him in: Dan's and Daniel's hands cut by the glass, or pricked by the pin. */
    public static @Nullable String dressed(int phase,int marks){
        int who=phase-FIRST;if(who!=DAN&&who!=DANIEL||marks==0)return null;
        return skin(phase)+((marks&LodgingInjury.CUT)!=0?"_cut":"_prick");
    }
    /** Somebody tied sitting at the foot of the post: on the floor, for the seat a body is drawn in (ActorSeat). */
    public static boolean onFloor(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==FIRST+FAILED_POST&&a.gesture()==TIED;}

    /** Where each is, from the house's base, which way they face (degrees; 0 is south, toward the front door), and what they are doing. */
    public record Spot(double x,double y,double z,float look,int gesture){}
    public static final Spot[] SPOTS={
        new Spot(1.5,LodgingSite.GROUND,-3.5,0,WAIT),                              // Rae: the middle of the hall, by Dan's bench, facing the door
        new Spot(3.6,LodgingSite.GROUND,-2.5,90,POCKET),                           // Dan: the bench's far end from the door, his back to the wall
        new Spot(-7.5,LodgingSite.UPPER,-21.55,180,WORK),                          // Vera: at the table of preparations, beside its chair
        new Spot(LodgingSite.DANIEL_FRAME.getX()+.5,LodgingSite.GROUND,LodgingSite.DANIEL_FRAME.getZ()+.68,0,HELD), // Daniel: in the frame, his back to its plaster
        new Spot(LodgingSite.DEAD_POST.getX()+1.15,LodgingSite.CELLAR,LodgingSite.DEAD_POST.getZ()+.5,-90,TIED),   // against the dead post's east side
        new Spot(LodgingSite.PASS_E+.5,LodgingSite.UPPER,LodgingSite.HATCH_Z[1]+.5,90,HATCH),                      // at room five's hatch, from the passage
        new Spot(LodgingSite.PASS_W+.5,LodgingSite.GROUND,LodgingSite.JIB.getZ()+.5,-90,FRAME)};                   // at the jib door, from the passage
    /** How near a reader is for one of the four to turn their head to them; how far a head turns from the body. */
    public static final double NEAR=8;
    public static final float HEAD_TURN=70;
    /** How long Dan holds his hand out once he has been asked to show it. */
    public static final int SHOWN=70;
    /** For this long after somebody has said a line aloud they do not say it aloud again: a second reader has it in their panel. */
    public static final int QUIET=240;

    /** Each reader's own record of what was said (LodgingHouse.own). */
    public static final String RAE_ASKED="RaeAsked0602",RAE_ANSWER="RaeAnswer0602",ASKED_WRONG="AskedWrong0602",ASKED_PLACE="AskedPlace0602",DAN_MET="DanMet0602",DAN_SLEEP="DanSleep0602",
        VERA_MET="VeraMet0602",DANIEL_MET="DanielMet0602",KNOWS_DANIEL="KnowsDaniel0602",TOLD_DAN_HERE="ToldDanHere0602",DANIEL_ANSWER="DanielAnswer0602";
    /** 0.4.607 (N5, N6): each reader's record of the two accounts and the weapon. */
    public static final String DANIEL_TOLD="DanielTold0607",DAN_TOLD="DanTold0607",HEARD_DAN_SIDE="HeardDanSide0607",TOLD_DAN_DRAINED="ToldDanDrained0607",
        SAID_DIDNT_KNOW="SaidDidntKnow0607",SAID_WAIT="SaidWait0607",GAVE_WEAPON="GaveWeapon0607",WARNED_DANIEL="WarnedDaniel0607";
    /** The house's: Daniel has a weapon; Dan has been told Daniel is still in the house, and asked to be shown. */
    public static final String ARMED="DanielArmed0607",DAN_KNOWS="DanKnows0607";
    /** What a reader told Rae: nothing yet, that they would stay with Dan, that they would see Vera first, that it was not their business. */
    public static final int UNDECIDED=0,STAY=1,VERA_FIRST=2,NOT_MINE=3;
    /** What a reader told Daniel: nothing yet, that they would get him out, that they would come back, nothing at all. */
    public static final int FREE_HIM=1,COME_BACK=2,SILENT=3;
    /** Whether this reader has promised to stay with Dan (to Rae), and to free Daniel (to Daniel). */
    public static boolean promisedDan(CompoundTag own){return own.getInt(RAE_ANSWER)==STAY;}
    public static boolean promisedDaniel(CompoundTag own){return own.getInt(DANIEL_ANSWER)==FREE_HIM;}
    public static final String TALK="lodging";

    /** The seven at one house: who they are by body, and until when Dan holds his hand out. */
    static final class Household {final UUID[] ids=new UUID[COUNT];long lastTick=-1,showUntil=-1;final Map<String,Long> said=new HashMap<>();}
    private static final Map<BlockPos,Household> HOUSES=new HashMap<>();
    public static void clearAll(){HOUSES.clear();LodgingDoubles.clearAll();LodgingStory.clearAll();LodgingDaniel.clearAll();}
    static Household household(BlockPos b){return HOUSES.computeIfAbsent(b.immutable(),k->new Household());}

    public static AABB area(BlockPos b){return new AABB(b.getX()+LodgingSite.WEST,b.getY()+LodgingSite.CELLAR_SLAB,b.getZ()+LodgingSite.BACK,b.getX()+LodgingSite.EAST+1,b.getY()+LodgingSite.ROOF+1,b.getZ()+LodgingSite.FRONT+1);}
    private static boolean live(ServerLevel l,Vec3 at){var cell=BlockPos.containing(at);return l.isLoaded(cell)&&l.areEntitiesLoaded(ChunkPos.asLong(cell));}
    public static Vec3 world(BlockPos b,Spot s){return new Vec3(b.getX()+s.x(),b.getY()+s.y(),b.getZ()+s.z());}

    /** One of them at this house, if their body is here. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var h=household(b);
        if(h.ids[who]!=null&&l.getEntity(h.ids[who]) instanceof LiteraryActor a&&a.isAlive()&&is(a)&&who(a)==who)return a;
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),LodgingPeople::is))if(who(old)==who&&old.isAlive()){h.ids[who]=old.getUUID();return old;}
        return null;
    }
    /**
     * Each of the seven at the house, made where they belong if they are not there. Nothing is made while any of the house has its
     * entities unloaded, so nobody is made twice over one who has yet to load.
     */
    public static void ensure(ServerLevel l,BlockPos b){
        var h=household(b);if(!RoomActors.ready(l,area(b)))return;int stage=LodgingStory.stage(l.getServer(),b);
        for(int who=0;who<COUNT;who++){
            if(find(l,b,who)!=null)continue;
            // 0.4.604: one made again (a house that lost a body) is made where the chapter has them.
            var spot=LodgingStory.spot(stage,who);var at=world(b,spot);if(!live(l,at))continue;
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
            a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);a.gesture(spot.gesture());
            a.moveTo(at.x,at.y,at.z,spot.look(),0);a.setYHeadRot(spot.look());a.setYBodyRot(spot.look());
            if(!l.addFreshEntity(a))continue;
            h.ids[who]=a.getUUID();
        }
    }
    /** None of the seven is at the house: for a house built again from nothing, and for tests. */
    public static void reset(ServerLevel l,BlockPos b){
        for(var old:l.getEntitiesOfClass(LiteraryActor.class,area(b),LodgingPeople::is))old.discard();
        HOUSES.remove(b);LodgingDoubles.reset(l,b);LodgingStory.reset(l.getServer(),b);LodgingDaniel.reset(b);
    }

    /** Every game tick, each house with a reader in it: heads turned on the scene's own quarter-second tick would turn in steps. */
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        if(HOUSES.isEmpty())return;var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var b:List.copyOf(HOUSES.keySet())){var here=LodgingHouse.readers(l,b);if(!here.isEmpty())tick(l,b,here);}
    }
    /** Once a tick for the house, however many readers are in it. */
    public static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=household(b);long now=l.getGameTime();if(h.lastTick==now)return;h.lastTick=now;
        // 0.4.604: the chapter goes on (Rae goes up, Vera comes down, the rooms), and the bodies in the beds.
        LodgingStory.tick(l,b,here);LodgingDoubles.tick(l,b,here);LodgingDaniel.tick(l,b);int stage=LodgingStory.stage(l.getServer(),b);
        for(int who=0;who<COUNT;who++){
            var a=find(l,b,who);if(a==null)continue;
            var spot=LodgingStory.spot(stage,who);float rest=spot.look();
            // Somebody on their way somewhere turns where they walk; heads turn to readers only for those standing in their place.
            if(!failed(who)&&a.position().distanceToSqr(world(b,spot))>.36)continue;
            // Vera's last line in the hall is to Dan.
            if(who==VERA&&stage==LodgingStory.ADDRESS&&LodgingStory.house(l.getServer(),b).getBoolean("DanUp")){var dan=find(l,b,DAN);if(dan!=null)rest=yawTo(a,dan.position());}
            // Dan puts his hand back in his pocket.
            if(who==DAN&&a.gesture()==SHOW&&now>=h.showUntil)a.gesture(POCKET);
            if(failed(who)){if(!(who==FAILED_SHIRT&&LodgingInjury.moving(l.getServer(),b)))turnHead(a,rest,rest);continue;}
            ServerPlayer near=null;double best=NEAR*NEAR;
            for(var p:here){if(p.isSpectator()||!p.isAlive())continue;double d=a.distanceToSqr(p);if(d<best&&a.hasLineOfSight(p)){best=d;near=p;}}
            turnHead(a,rest,near==null?rest:yawTo(a,near.getEyePosition()));
        }
    }
    /** Arrived where the chapter has them: turned the way the place faces, doing what they do there. */
    static void settle(LiteraryActor a,Spot to){if(a.gesture()!=to.gesture()&&!(to.gesture()==POCKET&&a.gesture()==SHOW)&&a.gesture()!=PIN)a.gesture(to.gesture());}
    /** 0.4.606: Dan holds his hand out until then (for the pin). */
    static void showHand(BlockPos b,long until){household(b).showUntil=until;}
    static float yawTo(Entity a,Vec3 at){var d=at.subtract(a.position());return (float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;}
    /** The body kept to this yaw, and the head toward the other, no more than HEAD_TURN off the body, a little at a time. */
    private static void turnHead(LiteraryActor a,float body,float head){
        a.setYRot(body);a.setYBodyRot(body);
        float want=body+Mth.clamp(Mth.wrapDegrees(head-body),-HEAD_TURN,HEAD_TURN);
        a.setYHeadRot(Mth.approachDegrees(a.getYHeadRot(),want,10));
    }

    // ------------------------------------------------------------------------------------------------ a hand on one of them
    /**
     * A line in the reader's panel, and over the speaker's head for anybody near, unless they said the same thing aloud a moment ago
     * (to another reader, or to this one in somebody else's exchange): then it is in the panel only.
     */
    private static void says(ServerPlayer p,LiteraryActor a,String name,String line){
        Talk.line(p,name,line);var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        var h=household(b);long now=p.serverLevel().getGameTime();String key=who(a)+"|"+line;Long last=h.said.get(key);
        if(last!=null&&now-last<QUIET)return;
        h.said.put(key,now);a.say(name,line);a.speaks(line);
        // 0.4.605: Dan's voice, where it carries, brings Daniel against the frame.
        if(who(a)==DAN)LodgingDaniel.danSpoke(p.serverLevel(),b,a);
    }
    /** 0.4.606: a blow on one of them. The failed body in the back passage, loose, takes it (LodgingInjury); the rest, nothing yet. */
    public static void struck(ServerPlayer p,LiteraryActor a){
        if(!is(a))return;
        if(who(a)==FAILED_SHIRT&&LodgingInjury.struck(p,a))return;
        if(failed(who(a)))LodgingHouse.cue(p,LodgingTexts.failed(who(a)));
    }
    /** A hand on one of them: the four speak in the panel; a failed body goes on with what it is doing. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!is(a))return;var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;var l=p.serverLevel();
        int who=who(a);
        if(failed(who)){LodgingHouse.cue(p,LodgingTexts.failed(who));return;}
        if(a.saying()&&Talk.key(p).equals(TALK+who))return;
        // 0.4.605: Daniel is spoken to from his room, through the open hatch, or crouched at his door; not through a shut door.
        if(who==DANIEL&&!LodgingDaniel.reach(p,b))return;
        switch(who){case RAE->rae(p,l,b,a,own);case DAN->dan(p,a,own);case VERA->vera(p,b,a,own);default->daniel(p,a,own);}
    }

    // Rae's request (N1). She asks each reader for themselves; one reader's yes is never the party's.
    private static void rae(ServerPlayer p,ServerLevel l,BlockPos b,LiteraryActor rae,CompoundTag own){
        if(LodgingStory.stage(p.server,b)!=LodgingStory.ASKING){LodgingHouse.cue(p,LodgingTexts.RAE_BUSY);return;}
        if(own.getInt(RAE_ANSWER)!=UNDECIDED){LodgingHouse.cue(p,LodgingTexts.RAE_WATCHES);return;}
        var dan=find(l,b,DAN);boolean first=!own.getBoolean(RAE_ASKED);
        Talk.begin(p,TALK+RAE,"Rae");
        if(first){
            own.putBoolean(RAE_ASKED,true);LodgingHouse.save(p,own);LodgingStory.asked(p);
            for(var line:LodgingTexts.RAE_REQUEST)says(p,rae,"Rae",line);
            // Dan has his say once to a reader: one who went to him first has heard the first of it.
            if(dan!=null){
                if(!own.getBoolean(DAN_MET)){own.putBoolean(DAN_MET,true);LodgingHouse.save(p,own);says(p,dan,"Dan",LodgingTexts.DAN_FINE[0]);says(p,dan,"Dan",LodgingTexts.DAN_FINE[1]);}
                says(p,dan,"Dan",LodgingTexts.DAN_FINE[2]);
            }
        }else says(p,rae,"Rae",LodgingTexts.RAE_REQUEST[LodgingTexts.RAE_REQUEST.length-1]);
        raeAsks(p);
    }
    private static void raeAsks(ServerPlayer p){
        var own=LodgingHouse.own(p);var replies=new java.util.ArrayList<Talk.Reply>();
        replies.add(Talk.reply(LodgingTexts.R_STAY,r->raeAnswered(r,STAY)));
        if(!own.getBoolean(ASKED_WRONG))replies.add(Talk.reply(LodgingTexts.R_WRONG,LodgingPeople::askedWrong));
        if(!own.getBoolean(ASKED_PLACE))replies.add(Talk.reply(LodgingTexts.R_PLACE,r->askedPlace(r,true)));
        replies.add(Talk.reply(LodgingTexts.R_VERA,r->raeAnswered(r,VERA_FIRST)));
        replies.add(Talk.reply(LodgingTexts.R_NOT,r->raeAnswered(r,NOT_MINE)));
        Talk.ask(p,replies);
    }
    /** 0.4.605: a reader at the hatch or the door speaks to one of them they cannot put a hand on. */
    public static void speakTo(ServerPlayer p,int who){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;var a=find(p.serverLevel(),b,who);if(a==null)return;
        if(a.saying()&&Talk.key(p).equals(TALK+who))return;
        talk(p,a,LodgingHouse.own(p));
    }
    private static @Nullable LiteraryActor near(ServerPlayer p,int who){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return null;var a=find(p.serverLevel(),b,who);
        if(a!=null&&who==DANIEL)return LodgingDaniel.reach(p,b)?a:null;
        return a!=null&&a.distanceToSqr(p)<=LiteraryActor.ADDRESS_RANGE*LiteraryActor.ADDRESS_RANGE?a:null;
    }
    private static void askedWrong(ServerPlayer p){
        var own=LodgingHouse.own(p);own.putBoolean(ASKED_WRONG,true);LodgingHouse.save(p,own);
        var rae=near(p,RAE);var dan=near(p,DAN);if(rae==null){Talk.close(p);return;}
        says(p,rae,"Rae",LodgingTexts.RAE_HAND);
        if(dan!=null){
            var b=LiteraryVignettes.base(p,PLACE);if(b!=null){dan.gesture(SHOW);household(b).showUntil=p.serverLevel().getGameTime()+SHOWN;}
            LodgingHouse.cue(p,LodgingTexts.DAN_SHOWS);says(p,dan,"Dan",LodgingTexts.DAN_SPRING);
        }
        says(p,rae,"Rae",LodgingTexts.RAE_ARRANGEMENT);
        raeAsks(p);
    }
    /** What the place is: Dan answers, whoever was asked. */
    private static void askedPlace(ServerPlayer p,boolean toRae){
        var own=LodgingHouse.own(p);own.putBoolean(ASKED_PLACE,true);LodgingHouse.save(p,own);
        var dan=near(p,DAN);if(dan==null){Talk.close(p);return;}
        says(p,dan,"Dan",LodgingTexts.DAN_PLACE);
        if(toRae)raeAsks(p);else danAsks(p);
    }
    private static void raeAnswered(ServerPlayer p,int answer){
        var own=LodgingHouse.own(p);own.putInt(RAE_ANSWER,answer);LodgingHouse.save(p,own);
        var rae=near(p,RAE);var dan=near(p,DAN);if(rae==null){Talk.close(p);return;}
        switch(answer){
            case STAY->says(p,rae,"Rae",LodgingTexts.RAE_THANKS);
            case VERA_FIRST->says(p,rae,"Rae",LodgingTexts.RAE_COME);
            default->{says(p,rae,"Rae",LodgingTexts.RAE_NO);if(dan!=null)says(p,dan,"Dan",LodgingTexts.DAN_GOOD);}
        }
        Talk.end(p);
    }

    // Dan, asked for himself.
    private static void dan(ServerPlayer p,LiteraryActor dan,CompoundTag own){
        // 0.4.607 (N6): once the glass has gone one way or the other, Dan has his own account for each reader, once.
        var b=LiteraryVignettes.base(p,PLACE);
        if(b!=null&&actTwo(p,b)&&!own.getBoolean(DAN_TOLD)){
            own.putBoolean(DAN_TOLD,true);own.putBoolean(DAN_MET,true);LodgingHouse.save(p,own);
            Talk.begin(p,TALK+DAN,"Dan");
            for(var line:LodgingTexts.DAN_DOG)says(p,dan,"Dan",line);
            if(promisedDan(own))says(p,dan,"Dan",LodgingTexts.DAN_STAND);
            danAsks(p);return;
        }
        if(own.getBoolean(DAN_MET)&&own.getBoolean(DAN_SLEEP)&&own.getBoolean(ASKED_PLACE)&&!canTellDan(p,own)){LodgingHouse.cue(p,LodgingTexts.DAN_SITS);return;}
        Talk.begin(p,TALK+DAN,"Dan");
        if(!own.getBoolean(DAN_MET)){own.putBoolean(DAN_MET,true);LodgingHouse.save(p,own);says(p,dan,"Dan",LodgingTexts.DAN_FINE[0]);says(p,dan,"Dan",LodgingTexts.DAN_FINE[1]);}
        else LodgingHouse.cue(p,LodgingTexts.DAN_LOOKS_UP);
        danAsks(p);
    }
    private static void danAsks(ServerPlayer p){
        var own=LodgingHouse.own(p);var replies=new java.util.ArrayList<Talk.Reply>();
        if(!own.getBoolean(DAN_SLEEP))replies.add(Talk.reply(LodgingTexts.R_WRONG_DAN,LodgingPeople::askedSleep));
        if(!own.getBoolean(ASKED_PLACE))replies.add(Talk.reply(LodgingTexts.R_PLACE,r->askedPlace(r,false)));
        if(canTellDan(p,own))replies.add(Talk.reply(LodgingTexts.R_STILL,LodgingPeople::toldDanDrained));
        if(replies.isEmpty()){Talk.end(p);return;}
        replies.add(Talk.act(LodgingTexts.R_LEAVE_DAN,Talk::close));
        Talk.ask(p,replies);
    }
    /** Act II has begun: the glass has broken or the body was kept off (N4 over). */
    static boolean actTwo(ServerPlayer p,BlockPos b){return LodgingStory.stage(p.server,b)>=LodgingStory.AFTER;}
    /** Whether this reader can tell Dan that Daniel is still being used: they know Daniel, and Dan has not seen him at the frame. */
    private static boolean canTellDan(ServerPlayer p,CompoundTag own){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null||!actTwo(p,b)||own.getBoolean(TOLD_DAN_DRAINED))return false;
        if(LodgingStory.stage(p.server,b)>=LodgingStory.PINNED)return false;
        return own.getBoolean(DANIEL_MET)||own.getBoolean(LodgingInjury.SAW_DANIEL_CUT);
    }
    /** N6: told Daniel is still being drained. What he says is what this reader has heard of Dan's side. */
    private static void toldDanDrained(ServerPlayer p){
        var own=LodgingHouse.own(p);own.putBoolean(TOLD_DAN_DRAINED,true);own.putBoolean(HEARD_DAN_SIDE,true);LodgingHouse.save(p,own);
        var dan=near(p,DAN);if(dan==null){Talk.close(p);return;}
        for(var line:LodgingTexts.DAN_NO)says(p,dan,"Dan",line);
        var b=LiteraryVignettes.base(p,PLACE);
        if(b!=null){var h=LodgingStory.house(p.server,b);h.putBoolean(DAN_KNOWS,true);LodgingStory.save(p.server,b,h);}
        Talk.end(p);
    }
    private static void askedSleep(ServerPlayer p){
        var own=LodgingHouse.own(p);own.putBoolean(DAN_SLEEP,true);LodgingHouse.save(p,own);
        var dan=near(p,DAN);if(dan==null){Talk.close(p);return;}
        says(p,dan,"Dan",LodgingTexts.DAN_SLEEPING);danAsks(p);
    }

    // Vera, found at her work before she has come down (N2's opening).
    private static void vera(ServerPlayer p,BlockPos b,LiteraryActor vera,CompoundTag own){
        // 0.4.604: once the rooms are given out, a reader with a room can ask her about what is in its bed (N2's replies "to Vera").
        int stage=LodgingStory.stage(p.server,b);
        if(stage>=LodgingStory.ADDRESS&&LodgingStory.room(p.server,b,p.getUUID())>=0&&LodgingDoubles.ask(p))return;
        if(stage>=LodgingStory.COMING_DOWN&&stage<LodgingStory.SETTLED){LodgingHouse.cue(p,LodgingTexts.VERA_BUSY);return;}
        if(own.getBoolean(VERA_MET)){LodgingHouse.cue(p,LodgingTexts.VERA_WORKS);return;}
        own.putBoolean(VERA_MET,true);LodgingHouse.save(p,own);
        Talk.begin(p,TALK+VERA,"Vera");
        for(var line:LodgingTexts.VERA_GUESTS)says(p,vera,"Vera",line);
        Talk.end(p);
    }

    // Daniel, in the frame (N3).
    private static void daniel(ServerPlayer p,LiteraryActor daniel,CompoundTag own){
        String name=name(DANIEL,own);
        Talk.begin(p,TALK+DANIEL,name);
        var b=LiteraryVignettes.base(p,PLACE);
        if(!own.getBoolean(DANIEL_MET)){own.putBoolean(DANIEL_MET,true);LodgingHouse.save(p,own);for(var line:LodgingTexts.DANIEL_LISTEN)says(p,daniel,name,line);}
        // 0.4.607 (N5): in Act II, a reader he has spoken to before has his account, once.
        else if(b!=null&&actTwo(p,b)&&!own.getBoolean(DANIEL_TOLD)){own.putBoolean(DANIEL_TOLD,true);LodgingHouse.save(p,own);for(var line:LodgingTexts.DANIEL_ACCOUNT)says(p,daniel,name,line);}
        else says(p,daniel,name,LodgingTexts.DANIEL_LISTEN[LodgingTexts.DANIEL_LISTEN.length-1]);
        if(armed(daniel))LodgingHouse.cue(p,LodgingTexts.DANIEL_ARMED);
        danielAsks(p);
    }
    private static void danielAsks(ServerPlayer p){
        var own=LodgingHouse.own(p);var replies=new java.util.ArrayList<Talk.Reply>();
        if(!own.getBoolean(TOLD_DAN_HERE))replies.add(Talk.reply(LodgingTexts.R_DAN_HERE,LodgingPeople::toldDanHere));
        if(!own.getBoolean(KNOWS_DANIEL))replies.add(Talk.reply(LodgingTexts.R_WHO,LodgingPeople::askedWho));
        if(!promisedDaniel(own))replies.add(Talk.reply(LodgingTexts.R_FREE,r->danielAnswered(r,FREE_HIM)));
        // 0.4.607 (N5): once his account is told.
        if(own.getBoolean(DANIEL_TOLD)){
            if(own.getBoolean(HEARD_DAN_SIDE)&&!own.getBoolean(SAID_DIDNT_KNOW))replies.add(Talk.reply(LodgingTexts.R_DIDNT_KNOW,LodgingPeople::saidDidntKnow));
            if(sawTheRule(own)&&!own.getBoolean(WARNED_DANIEL))replies.add(Talk.reply(LodgingTexts.R_HURT_SELF,LodgingPeople::warnedDaniel));
            var d=near(p,DANIEL);
            if(d!=null&&!armed(d)&&weapon(p.getMainHandItem()))replies.add(Talk.act(LodgingTexts.R_GIVE,LodgingPeople::gaveWeapon));
            if(!own.getBoolean(SAID_WAIT))replies.add(Talk.reply(LodgingTexts.R_WAIT,LodgingPeople::saidWait));
        }
        replies.add(Talk.reply(LodgingTexts.R_BACK,r->danielAnswered(r,COME_BACK)));
        replies.add(Talk.act(LodgingTexts.R_NOTHING,r->danielAnswered(r,SILENT)));
        Talk.ask(p,replies);
    }
    private static void toldDanHere(ServerPlayer p){
        var own=LodgingHouse.own(p);own.putBoolean(TOLD_DAN_HERE,true);LodgingHouse.save(p,own);
        var daniel=near(p,DANIEL);if(daniel==null){Talk.close(p);return;}
        says(p,daniel,name(DANIEL,own),LodgingTexts.DANIEL_VOICE);danielAsks(p);
    }
    private static void askedWho(ServerPlayer p){
        var own=LodgingHouse.own(p);var daniel=near(p,DANIEL);if(daniel==null){Talk.close(p);return;}
        // He gives his name under the name the reader has had for him; from the next line on he is Daniel.
        says(p,daniel,name(DANIEL,own),LodgingTexts.DANIEL_NAME);
        own.putBoolean(KNOWS_DANIEL,true);LodgingHouse.save(p,own);
        LodgingHouse.cue(p,LodgingTexts.DANIEL_LOOKS);says(p,daniel,"Daniel",LodgingTexts.DANIEL_COUNTING);
        danielAsks(p);
    }
    /** Something that cuts or strikes. */
    public static boolean weapon(net.minecraft.world.item.ItemStack s){
        var i=s.getItem();
        return i instanceof net.minecraft.world.item.SwordItem||i instanceof net.minecraft.world.item.AxeItem||i instanceof net.minecraft.world.item.TridentItem
            ||i instanceof net.minecraft.world.item.MaceItem||i instanceof net.minecraft.world.item.ShearsItem;
    }
    /** Whether Daniel has a weapon in his hand. */
    public static boolean armed(LiteraryActor daniel){return weapon(daniel.getMainHandItem());}
    /** Whether this reader has seen the rule shown: the cut on either man, or the pin. */
    static boolean sawTheRule(CompoundTag own){return own.getBoolean(LodgingInjury.SAW_DAN_CUT)||own.getBoolean(LodgingInjury.SAW_DANIEL_CUT)||own.getBoolean(LodgingInjury.SAW_PIN);}
    private static void saidDidntKnow(ServerPlayer p){
        var own=LodgingHouse.own(p);own.putBoolean(SAID_DIDNT_KNOW,true);LodgingHouse.save(p,own);
        if(near(p,DANIEL)==null){Talk.close(p);return;}
        LodgingHouse.cue(p,LodgingTexts.DANIEL_SILENT);danielAsks(p);
    }
    private static void warnedDaniel(ServerPlayer p){
        var own=LodgingHouse.own(p);own.putBoolean(WARNED_DANIEL,true);LodgingHouse.save(p,own);
        var daniel=near(p,DANIEL);if(daniel==null){Talk.close(p);return;}
        says(p,daniel,name(DANIEL,own),LodgingTexts.DANIEL_MAKE);danielAsks(p);
    }
    private static void saidWait(ServerPlayer p){var own=LodgingHouse.own(p);own.putBoolean(SAID_WAIT,true);LodgingHouse.save(p,own);Talk.close(p);}
    /** The reader hands him what they hold. It is the reader's own, and it is his now. */
    private static void gaveWeapon(ServerPlayer p){
        var daniel=near(p,DANIEL);var held=p.getMainHandItem();
        if(daniel==null||!weapon(held)||armed(daniel)){Talk.close(p);return;}
        var own=LodgingHouse.own(p);own.putBoolean(GAVE_WEAPON,true);LodgingHouse.save(p,own);
        daniel.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,held.split(1));daniel.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND,0);
        var b=LiteraryVignettes.base(p,PLACE);
        if(b!=null){var h=LodgingStory.house(p.server,b);h.putBoolean(ARMED,true);LodgingStory.save(p.server,b,h);}
        LodgingHouse.cue(p,LodgingTexts.DANIEL_TAKES);
        // A reader who knows nothing of the rule is told "Good."; one who has seen it may still warn him (the reply stays).
        if(!sawTheRule(own))says(p,daniel,name(DANIEL,own),LodgingTexts.DANIEL_GOOD);
        danielAsks(p);
    }
    private static void danielAnswered(ServerPlayer p,int answer){
        var own=LodgingHouse.own(p);
        // A promise to get him out is kept whatever is said after it.
        if(!promisedDaniel(own)){own.putInt(DANIEL_ANSWER,answer);LodgingHouse.save(p,own);}
        var daniel=near(p,DANIEL);if(daniel==null||answer!=FREE_HIM){Talk.close(p);return;}
        says(p,daniel,name(DANIEL,own),LodgingTexts.DANIEL_KEY);Talk.end(p);
    }
}
