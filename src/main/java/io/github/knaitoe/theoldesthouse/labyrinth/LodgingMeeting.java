package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.611, The Ones Already Inside, its eleventh slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N10 and N11: "the confrontation's
 * skeleton with its substitutions; the first attempt").
 *
 * <ul>
 * <li><b>Put in the same room.</b> Daniel free and in the parlor: Rae goes in, Dan after her. Daniel still held: a reader Dan has
 * asked to be shown ("Come and see him.") walks him there, Rae following, to the frame.</li>
 * <li><b>The script.</b> Five lines, the last to Rae: "Then why did you?" Then whoever is near can speak: tell them what Rae said,
 * what Vera said, about the cellar; keep a promise out loud; or let her answer. Each telling is said once in the house; a promise is
 * each reader's own.</li>
 * <li><b>The first attempt.</b> One of them acts on their plan, chosen from what was said: Vera, exposed, goes for the frame; Daniel,
 * armed, free and not backed, goes for Dan; Dan, told and supported, asks for a minute; Rae, still trusted, takes Dan's arm toward the
 * stair; otherwise Dan, cornered, takes hold of Daniel and calls for Vera. It begins aloud, and a hand on the one acting, before it is
 * done, stops it. Where it ends up is the next slice's.</li>
 * </ul>
 */
public final class LodgingMeeting {
    private LodgingMeeting(){}
    /** The house's: where it is (parlor, frame), how far along, when, the line reached; what has been said aloud; the attempt. */
    public static final String STAGE="Meeting0611",WHERE="MeetWhere0611",AT="MeetAt0611",LINE="MeetLine0611",
        TOLD_RAE="MeetToldRae0611",TOLD_VERA="MeetToldVera0611",TOLD_CELLAR="MeetToldCellar0611",SUPPORTED="MeetSupported0611",BACKED="MeetBacked0611",
        ANSWERED="MeetRaeAnswered0611",ATTEMPT="Attempt0611",ATTEMPT_AT="AttemptAt0611",STOPPED="AttemptStopped0611",LANDED="AttemptLanded0611";
    /** Each reader's own: brought Dan, what they said, stopped the attempt. */
    public static final String BROUGHT="BroughtDan0611",SPOKE="MeetSpoke0611",KEPT_DAN="KeptDan0611",KEPT_DANIEL="KeptDaniel0611",STOPPED_IT="StoppedIt0611";
    public static final int NONE=0,GATHER=1,LINES=2,ASKED=3,ATTEMPTING=4,DONE=5;
    public static final int PARLOR=1,FRAME=2;
    public static final int RAE_TAKES=1,DANIEL_GOES=2,DAN_MINUTE=3,DAN_HOLDS=4,VERA_GOES=5;
    /** Between lines; how long the room waits on Rae before somebody acts; after she answers. */
    public static final int GAP=70,WAIT=500,AFTER_ANSWER=120;

    // ------------------------------------------------------------------------------------------------ where they stand
    /** In the parlor: Daniel stepped back from the door toward the windows, Dan just inside it clear of the side table and the armchair,
     *  Rae by the door between them; at the frame, Dan clear of Daniel's chair, Vera at the frame's far side. */
    public static final LodgingPeople.Spot DANIEL_MEET=new LodgingPeople.Spot(-5.8,LodgingSite.GROUND,-4.6,165,LodgingPeople.WAIT),
        DAN_PARLOR=new LodgingPeople.Spot(-6.6,LodgingSite.GROUND,-7.6,-15,LodgingPeople.WAIT),
        RAE_PARLOR=new LodgingPeople.Spot(-5.0,LodgingSite.GROUND,-6.2,-27,LodgingPeople.WAIT),
        DAN_FRAME=new LodgingPeople.Spot(-5.4,LodgingSite.GROUND,-25.6,129,LodgingPeople.WAIT),
        VERA_FRAME=new LodgingPeople.Spot(-4.8,LodgingSite.GROUND,-27.0,100,LodgingPeople.WORK),
        STAIR_FOOT=new LodgingPeople.Spot(2.6,LodgingSite.GROUND,-5.5,180,LodgingPeople.DRAG);
    /** From the hall into the parlor (its door on leg 3). */
    private static final List<Vec3> INTO_PARLOR=List.of(new Vec3(0,0,-5),new Vec3(0,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(-5.6,0,-8.5));
    /** Dan's way in: through the door and straight on a pace before turning, clear of the armchair's corner. */
    private static final List<Vec3> DAN_INTO_PARLOR=List.of(new Vec3(0,0,-5),new Vec3(0,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(-5.6,0,-8.5),new Vec3(-5.6,0,-7.6));
    /** From the parlor back out to the stair foot (its door on leg 1). */
    private static final List<Vec3> OUT_OF_PARLOR=List.of(new Vec3(-5.6,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(0,0,-8.5),new Vec3(0,0,-5));
    /** From the hall to Daniel's room (his door on leg 3). */
    static final List<Vec3> TO_FRAME=List.of(new Vec3(0,0,-5),new Vec3(0,0,-14),new Vec3(-7.5,0,-14),new Vec3(-7.5,0,-18.5),new Vec3(-5.6,0,-20),new Vec3(-5.6,0,-24));
    /** From Daniel's room back to the stair foot (his door on leg 2). */
    private static final List<Vec3> FROM_FRAME=List.of(new Vec3(-5.6,0,-24),new Vec3(-5.6,0,-20),new Vec3(-7.5,0,-18.5),new Vec3(-7.5,0,-14),new Vec3(0,0,-14),new Vec3(0,0,-5));
    /** Vera down from between the rooms upstairs (the workroom door on leg 1), then to Daniel's room. */
    static final List<Vec3> VERA_DOWN=List.of(new Vec3(.5,5,-19.5),new Vec3(.5,5,-15),new Vec3(3.5,5,-12.5),new Vec3(3.5,0,-5.5),new Vec3(0,0,-5),new Vec3(0,0,-14));

    static CompoundTag house(ServerLevel l,BlockPos b){return LodgingStory.house(l.getServer(),b);}
    public static int stage(ServerLevel l,BlockPos b){return house(l,b).getInt(STAGE);}
    public static int attempt(ServerLevel l,BlockPos b){return house(l,b).getInt(ATTEMPT);}

    /** Where one of the four stands while the meeting holds them, or null for the chapter's own place. */
    static @Nullable LodgingPeople.Spot spot(net.minecraft.server.MinecraftServer s,BlockPos b,int who){
        var h=LodgingStory.house(s,b);int st=h.getInt(STAGE);if(st==NONE)return null;boolean parlor=h.getInt(WHERE)==PARLOR;
        int att=h.getInt(ATTEMPT);boolean landed=h.getBoolean(LANDED),stopped=h.getBoolean(STOPPED);
        if(who==LodgingPeople.VERA&&att==VERA_GOES&&!stopped)return VERA_FRAME;
        if((who==LodgingPeople.RAE||who==LodgingPeople.DAN)&&att==RAE_TAKES&&landed)return STAIR_FOOT;
        if(who==LodgingPeople.DAN)return parlor?DAN_PARLOR:DAN_FRAME;
        if(who==LodgingPeople.DANIEL&&parlor&&att!=DANIEL_GOES)return DANIEL_MEET;
        if(who==LodgingPeople.RAE)return parlor?RAE_PARLOR:LodgingKey.RAE_AT_FRAME;
        return null;
    }

    // ------------------------------------------------------------------------------------------------ beginning it
    /** Daniel out and in the parlor, the chapter settled, Rae about nothing else: they go in to him. */
    private static boolean parlorReady(ServerLevel l,BlockPos b,CompoundTag h){
        int st=h.getInt("Stage");if(st!=LodgingStory.AFTER&&st!=LodgingStory.PINNED)return false;
        if(!h.getBoolean(LodgingKey.FREE)||!h.getBoolean(LodgingKey.IN))return false;
        int task=h.getInt(LodgingKey.TASK);if(task!=LodgingKey.NONE&&task!=LodgingKey.DOORWAY)return false;
        // Rae in the parlor's door has stood there and been seen there first.
        if(task==LodgingKey.DOORWAY){var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);if(rae==null||rae.position().distanceToSqr(LodgingPeople.world(b,LodgingKey.RAE_DOORWAY))>.1)return false;}
        return true;
    }
    /** How long Daniel is in the parlor (and Rae in its door) before they go in to him. */
    public static final int SETTLE=200;
    /** Whether this reader can take Dan to Daniel at the frame: Dan has asked to be shown, Daniel is still held, nothing else under way. */
    static boolean canBring(ServerPlayer p,BlockPos b){
        var h=LodgingStory.house(p.server,b);int st=h.getInt("Stage");
        return (st==LodgingStory.AFTER||st==LodgingStory.PINNED)&&h.getBoolean(LodgingPeople.DAN_KNOWS)&&!h.getBoolean(LodgingKey.FREE)&&h.getInt(STAGE)==NONE
            &&h.getInt(LodgingKey.TASK)==LodgingKey.NONE;
    }
    /** "Come and see him." Dan goes, and Rae after him. */
    static void bring(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null||!canBring(p,b)){Talk.close(p);return;}
        var dan=LodgingPeople.near(p,LodgingPeople.DAN);if(dan==null){Talk.close(p);return;}
        LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_SHOW_ME);
        var own=LodgingHouse.own(p);own.putBoolean(BROUGHT,true);LodgingHouse.save(p,own);
        begin(p.serverLevel(),b,FRAME);Talk.end(p);
    }
    private static void begin(ServerLevel l,BlockPos b,int where){
        var s=l.getServer();var h=LodgingStory.house(s,b);
        h.putInt(STAGE,GATHER);h.putInt(WHERE,where);h.putInt(AT,h.getInt("Clock"));
        // Rae already in the parlor's door goes straight in.
        h.putInt("MeetLegR",where==PARLOR&&h.getInt(LodgingKey.TASK)==LodgingKey.DOORWAY?2:0);h.putInt("MeetLegD",0);h.putInt("MeetLegN",0);
        LodgingStory.save(s,b,h);
    }

    // ------------------------------------------------------------------------------------------------ every tick
    /** Once a tick for the house (LodgingPeople.tick). */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();var h=LodgingStory.house(s,b);int st=h.getInt(STAGE);int clock=h.getInt("Clock");
        switch(st){
            case NONE->{
                if(!parlorReady(l,b,h)){if(h.contains("MeetReadyAt0611")){h.remove("MeetReadyAt0611");LodgingStory.save(s,b,h);}return;}
                if(!h.contains("MeetReadyAt0611")){h.putInt("MeetReadyAt0611",clock);LodgingStory.save(s,b,h);return;}
                if(clock-h.getInt("MeetReadyAt0611")>=SETTLE)begin(l,b,PARLOR);
            }
            case GATHER->{
                boolean parlor=h.getInt(WHERE)==PARLOR;boolean raeThere,danThere;
                if(parlor){
                    raeThere=LodgingStory.walkVia(l,b,h,"MeetLegR",LodgingPeople.RAE,INTO_PARLOR,RAE_PARLOR,LodgingSite.PARLOR_DOOR,3);
                    danThere=h.getInt("MeetLegR")>=4&&LodgingStory.walkVia(l,b,h,"MeetLegD",LodgingPeople.DAN,DAN_INTO_PARLOR,DAN_PARLOR,LodgingSite.PARLOR_DOOR,3);
                    // Daniel steps back from the door as they come in.
                    danThere&=LodgingStory.walkVia(l,b,h,"MeetLegN",LodgingPeople.DANIEL,List.of(),DANIEL_MEET,LodgingSite.PARLOR_DOOR,-1);
                }else{
                    danThere=LodgingStory.walkVia(l,b,h,"MeetLegD",LodgingPeople.DAN,TO_FRAME,DAN_FRAME,LodgingSite.DANIEL_DOOR,3);
                    raeThere=h.getInt("MeetLegD")>=2&&LodgingStory.walkVia(l,b,h,"MeetLegR",LodgingPeople.RAE,TO_FRAME,LodgingKey.RAE_AT_FRAME,LodgingSite.DANIEL_DOOR,3);
                }
                if(raeThere&&danThere){h.putInt(STAGE,LINES);h.putInt(AT,clock);h.putInt(LINE,0);}
                LodgingStory.save(s,b,h);
            }
            case LINES->{
                if(clock-h.getInt(AT)<GAP)return;int i=h.getInt(LINE);
                if(i<LodgingTexts.MEET.length){
                    var line=LodgingTexts.MEET[i];var a=LodgingPeople.find(l,b,who(line[0]));if(a!=null)LodgingStory.speak(l,b,a,line[0],line[1]);
                    h.putInt(LINE,i+1);h.putInt(AT,clock);
                }else{
                    h.putInt(STAGE,ASKED);h.putInt(AT,clock);
                    var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
                    for(var p:here)if(daniel!=null&&!p.isSpectator()&&p.distanceToSqr(daniel)<=LodgingDaniel.HEARD*LodgingDaniel.HEARD)LodgingHouse.cue(p,LodgingTexts.MEET_ASKED);
                }
                LodgingStory.save(s,b,h);
            }
            case ASKED->{
                int waited=clock-h.getInt(AT);
                if(waited>=WAIT||h.getBoolean(ANSWERED)&&clock-h.getInt("AnsweredAt0611")>=AFTER_ANSWER){act(l,b,h,choose(h));LodgingStory.save(s,b,h);}
            }
            case ATTEMPTING->{if(step(l,b,h))land(l,b,h);LodgingStory.save(s,b,h);}
            default->{}
        }
    }
    private static int who(String name){return switch(name){case "Rae"->LodgingPeople.RAE;case "Dan"->LodgingPeople.DAN;case "Vera"->LodgingPeople.VERA;default->LodgingPeople.DANIEL;};}

    /** Who acts first, from what has been said aloud (N11's order). */
    public static int choose(CompoundTag h){
        if(h.getBoolean(TOLD_VERA))return VERA_GOES;
        if(h.getBoolean(LodgingPeople.ARMED)&&h.getBoolean(LodgingKey.FREE)&&!h.getBoolean(BACKED))return DANIEL_GOES;
        if(h.getBoolean(SUPPORTED)&&(h.getBoolean(TOLD_RAE)||h.getBoolean(TOLD_CELLAR)))return DAN_MINUTE;
        if(!h.getBoolean(TOLD_RAE))return RAE_TAKES;
        return DAN_HOLDS;
    }
    /** The one who acts in this attempt. */
    static int actor(int attempt){return switch(attempt){case RAE_TAKES->LodgingPeople.RAE;case DANIEL_GOES->LodgingPeople.DANIEL;case VERA_GOES->LodgingPeople.VERA;default->LodgingPeople.DAN;};}
    private static void act(ServerLevel l,BlockPos b,CompoundTag h,int attempt){
        h.putInt(STAGE,ATTEMPTING);h.putInt(ATTEMPT,attempt);h.putInt(ATTEMPT_AT,h.getInt("Clock"));h.putInt("ActLeg",0);h.putInt("ActLeg2",0);h.putInt("ActLegD",0);
        var a=LodgingPeople.find(l,b,actor(attempt));if(a==null)return;
        LodgingStory.speak(l,b,a,name(actor(attempt)),LodgingTexts.ATTEMPT_START[attempt-1]);
        if(attempt==DAN_MINUTE){a.gesture(LodgingPeople.PALMS);land(l,b,h);}
    }
    private static String name(int who){return switch(who){case LodgingPeople.RAE->"Rae";case LodgingPeople.DAN->"Dan";case LodgingPeople.VERA->"Vera";default->"Daniel";};}

    /** The attempt a step further; true once it has got where it was going. */
    private static boolean step(ServerLevel l,BlockPos b,CompoundTag h){
        boolean parlor=h.getInt(WHERE)==PARLOR;
        switch(h.getInt(ATTEMPT)){
            case RAE_TAKES->{
                var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);var legs=parlor?OUT_OF_PARLOR:FROM_FRAME;var door=parlor?LodgingSite.PARLOR_DOOR:LodgingSite.DANIEL_DOOR;int dl=parlor?1:2;
                boolean there=LodgingStory.walkVia(l,b,h,"ActLeg",LodgingPeople.RAE,legs,STAIR_FOOT,door,dl);
                if(rae!=null)rae.gesture(LodgingPeople.DRAG);
                // Dan a step behind her, by the arm.
                if(h.getInt("ActLeg")>=1)LodgingStory.walkVia(l,b,h,"ActLegD",LodgingPeople.DAN,legs,DAN_STAIR,door,dl);
                return there&&h.getInt("ActLegD")>legs.size();
            }
            case DANIEL_GOES->{
                var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(daniel==null||dan==null)return true;
                var to=toward(b,daniel.position(),dan.position(),1.0,LodgingPeople.RAISE);
                boolean there=LodgingStory.walkVia(l,b,h,"ActLeg",LodgingPeople.DANIEL,List.of(),to,LodgingSite.PARLOR_DOOR,-1);
                daniel.gesture(LodgingPeople.RAISE);return there;
            }
            case DAN_HOLDS->{
                var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(daniel==null||dan==null)return true;
                var to=toward(b,dan.position(),daniel.position(),.85,LodgingPeople.GRAB);
                boolean there=LodgingStory.walkVia(l,b,h,"ActLeg",LodgingPeople.DAN,List.of(),to,LodgingSite.PARLOR_DOOR,-1);
                dan.gesture(LodgingPeople.GRAB);return there;
            }
            case VERA_GOES->{
                if(!LodgingStory.walkVia(l,b,h,"ActLeg",LodgingPeople.VERA,VERA_DOWN,new LodgingPeople.Spot(0,LodgingSite.GROUND,-14,180,LodgingPeople.WAIT),LodgingSite.WORK_DOOR,1))return false;
                return LodgingStory.walkVia(l,b,h,"ActLeg2",LodgingPeople.VERA,TO_FRAME.subList(2,TO_FRAME.size()),VERA_FRAME,LodgingSite.DANIEL_DOOR,1);
            }
            default->{return true;}
        }
    }
    /** A place this far short of the other, facing them, from the house's base. */
    private static LodgingPeople.Spot toward(BlockPos b,Vec3 from,Vec3 at,double short_,int gesture){
        var d=new Vec3(at.x-from.x,0,at.z-from.z);double len=Math.max(.01,d.length());var stop=at.subtract(d.scale(Math.min(short_,len)/len));
        float yaw=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;
        return new LodgingPeople.Spot(stop.x-b.getX(),LodgingSite.GROUND,stop.z-b.getZ(),yaw,gesture);
    }
    public static final LodgingPeople.Spot DAN_STAIR=new LodgingPeople.Spot(3.0,LodgingSite.GROUND,-4.4,180,LodgingPeople.WAIT);
    private static void land(ServerLevel l,BlockPos b,CompoundTag h){
        int att=h.getInt(ATTEMPT);h.putBoolean(LANDED,true);h.putInt(STAGE,DONE);
        var a=LodgingPeople.find(l,b,actor(att));if(a==null)return;
        var line=LodgingTexts.ATTEMPT_LANDED[att-1];if(line!=null)LodgingStory.speak(l,b,a,name(actor(att)),line);
        var cue=LodgingTexts.ATTEMPT_SEEN[att-1];
        if(cue!=null)for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator()&&p.distanceToSqr(a)<=LodgingDaniel.HEARD*LodgingDaniel.HEARD&&p.hasLineOfSight(a))LodgingHouse.cue(p,cue);
        if(att==DAN_HOLDS){var d=LodgingPeople.find(l,b,LodgingPeople.DANIEL);if(d!=null&&LodgingKey.free(l.getServer(),b))d.gesture(LodgingPeople.HELD);}
    }

    // ------------------------------------------------------------------------------------------------ a hand on one of them
    /** A hand on Rae, Dan or Daniel while Rae is asked, or on whoever is acting before it is done. True if it was answered here. */
    static boolean talk(ServerPlayer p,BlockPos b,LiteraryActor a,int who,CompoundTag own){
        var h=LodgingStory.house(p.server,b);int st=h.getInt(STAGE);
        if(st==ASKED&&who!=LodgingPeople.VERA){
            Talk.begin(p,LodgingPeople.TALK+"meet",name(who));LodgingHouse.cue(p,LodgingTexts.MEET_ASKED);asks(p);return true;
        }
        if(st==ATTEMPTING&&who==actor(h.getInt(ATTEMPT))){
            Talk.begin(p,LodgingPeople.TALK+"stop",name(who));
            Talk.ask(p,List.of(Talk.reply(LodgingTexts.R_STOP,LodgingMeeting::stop),Talk.act(LodgingTexts.R_NOTHING,Talk::close)));
            return true;
        }
        return false;
    }
    private static void asks(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null||LodgingStory.house(p.server,b).getInt(STAGE)!=ASKED){Talk.close(p);return;}
        var h=LodgingStory.house(p.server,b);var own=LodgingHouse.own(p);var replies=new ArrayList<Talk.Reply>();
        if(own.getBoolean(LodgingKey.HEARD_CONFESSION)&&!h.getBoolean(TOLD_RAE))replies.add(Talk.reply(LodgingTexts.R_SAY_RAE,r->said(r,TOLD_RAE)));
        if(own.getBoolean(LodgingVera.TRUTH)&&!h.getBoolean(TOLD_VERA))replies.add(Talk.reply(LodgingTexts.R_SAY_VERA,r->said(r,TOLD_VERA)));
        if(own.getBoolean(LodgingCellar.KNOWS_THIRD)&&!h.getBoolean(TOLD_CELLAR))replies.add(Talk.reply(LodgingTexts.R_SAY_CELLAR,r->said(r,TOLD_CELLAR)));
        if(LodgingPeople.promisedDan(own)&&!own.getBoolean(KEPT_DAN))replies.add(Talk.reply(LodgingTexts.R_KEEP_DAN,r->said(r,KEPT_DAN)));
        if(LodgingPeople.promisedDaniel(own)&&!own.getBoolean(KEPT_DANIEL))replies.add(Talk.reply(LodgingTexts.R_KEEP_DANIEL,r->said(r,KEPT_DANIEL)));
        if(!h.getBoolean(ANSWERED))replies.add(Talk.reply(LodgingTexts.R_LET_HER,r->said(r,ANSWERED)));
        replies.add(Talk.act(LodgingTexts.R_NOTHING,Talk::close));
        Talk.ask(p,replies);
    }
    /** What a reader said into the room, and what it gets back. */
    private static void said(ServerPlayer p,String what){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);if(h.getInt(STAGE)!=ASKED){Talk.close(p);return;}
        var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        var own=LodgingHouse.own(p);own.putBoolean(SPOKE,true);
        switch(what){
            case TOLD_RAE->{h.putBoolean(TOLD_RAE,true);if(dan!=null)LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_YOU_KNEW);if(rae!=null)LodgingPeople.says(p,rae,"Rae",LodgingTexts.RAE_SO_YOU_CAME);}
            case TOLD_VERA->{h.putBoolean(TOLD_VERA,true);if(dan!=null)LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_WHICH);if(daniel!=null)LodgingPeople.says(p,daniel,"Daniel",LodgingTexts.DANIEL_YOU_KNOW);}
            case TOLD_CELLAR->{h.putBoolean(TOLD_CELLAR,true);if(dan!=null)LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_ME_AS_WELL);LodgingHouse.cue(p,LodgingTexts.NOBODY_ANSWERS);}
            case KEPT_DAN->{own.putBoolean(KEPT_DAN,true);h.putBoolean(SUPPORTED,true);if(dan!=null)LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_ALL_RIGHT);}
            case KEPT_DANIEL->{own.putBoolean(KEPT_DANIEL,true);h.putBoolean(BACKED,true);if(daniel!=null)LodgingPeople.says(p,daniel,"Daniel",LodgingTexts.DANIEL_NOT_NEAR);}
            default->{h.putBoolean(ANSWERED,true);h.putInt("AnsweredAt0611",h.getInt("Clock"));if(rae!=null)for(var line:LodgingTexts.RAE_WHY)LodgingPeople.says(p,rae,"Rae",line);}
        }
        LodgingHouse.save(p,own);LodgingStory.save(s,b,h);
        asks(p);
    }
    /** "Stop." The one acting stops where they are. */
    private static void stop(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);
        if(h.getInt(STAGE)!=ATTEMPTING||h.getBoolean(LANDED)){Talk.close(p);return;}
        int att=h.getInt(ATTEMPT);h.putBoolean(STOPPED,true);h.putInt(STAGE,DONE);LodgingStory.save(s,b,h);
        var own=LodgingHouse.own(p);own.putBoolean(STOPPED_IT,true);LodgingHouse.save(p,own);
        var a=LodgingPeople.find(l,b,actor(att));
        if(a!=null){a.gesture(LodgingPeople.WAIT);LodgingPeople.says(p,a,name(actor(att)),LodgingTexts.ATTEMPT_STOPPED[att-1]);}
        if(att==RAE_TAKES){var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);if(dan!=null)dan.gesture(LodgingPeople.WAIT);}
        Talk.end(p);
    }
}
