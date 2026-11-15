package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.613, The Ones Already Inside, its thirteenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, E1: "Return Daniel. Dan goes back
 * through the frame; Daniel is released; Dan is not anywhere any more. It differs by how: Dan's own agreement, the party's force, or
 * Vera using his panic.").
 *
 * <ul>
 * <li><b>His own agreement.</b> Once the first attempt and the taking are over, a reader who knows how the frame works (saw the rule,
 * heard Vera's truth, or learned the cellar's third way) can put it to Dan: "You could go back through. He'd walk out." If somebody
 * stood by him out loud, or he has been told what Rae did, he asks one thing and goes. Otherwise he says no.</li>
 * <li><b>The party's force.</b> After he has said no, each reader who knows can take hold of him. He is walked there pulling against
 * it, only while a holder is at his arm, faster with more of them; let go of for five seconds, he stops and stands where he is.</li>
 * <li><b>Vera using his panic.</b> If the party set itself against her and nobody came up to promise her their hands, and Dan has been
 * dragged into the walls once already, she calls down the stair that it is coming back and the frame is the one safe place, and he
 * runs for it. A hand on him on the way, and a word, stops him.</li>
 * <li><b>At the frame.</b> If Daniel is in it, its straps let go of him as Dan comes, and he steps out. Dan steps in, turns round,
 * and is drawn back into the plaster until he is not there. He is not made again. Then Daniel asks Rae what the old dog was called;
 * she tells a reader instead, and he hears it.</li>
 * </ul>
 */
public final class LodgingReturn {
    private LodgingReturn(){}
    public static final int NONE=0,GOING=1,STEPPING=2,ENTERING=3,SINKING=4,GONE=5,DONE=6;
    public static final int AGREED=1,FORCE=2,PANIC=3;
    public static final String STATE="Return0613";
    /** Each reader's own. */
    public static final String PUT_IT="PutItToDan0613",HELD="HeldDan0613",STOPPED="StoppedDan0613",SAW="SawReturn0613",HEARD_DOG="HeardDog0613";
    /** Paces: agreed, per holder (and at most), panicked; how long a holder can be away; how long he sinks; the coda's beats. */
    public static final double AGREED_PACE=.1,HOLD_PACE=.04,HOLD_MAX=.1,PANIC_PACE=.15,HOLD_REACH=2.6;
    public static final int LET_GO=100,SINK=100,CODA=80,CODA_GAP=60,PANIC_AFTER=600;
    /** Before the frame, in it, and where Daniel steps to out of it. */
    static final Vec3 FRONT=new Vec3(-7.5,0,-26.6),IN=new Vec3(-7.5,0,-28.32),STEP_OUT=new Vec3(-9.6,0,-26.2);
    public static final LodgingPeople.Spot DANIEL_OUT=new LodgingPeople.Spot(-9.6,LodgingSite.GROUND,-26.2,40,LodgingPeople.WAIT);

    static CompoundTag state(CompoundTag house){if(!house.contains(STATE))house.put(STATE,new CompoundTag());return house.getCompound(STATE);}
    public static int phase(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Phase");}
    public static int way(MinecraftServer s,BlockPos b){return state(LodgingStory.house(s,b)).getInt("Way");}
    /** Whether Dan has gone back through: LodgingPeople does not make him again. */
    public static boolean gone(MinecraftServer s,BlockPos b){return phase(s,b)>=GONE;}
    private static void go(CompoundTag in,int phase,int clock){in.putInt("Phase",phase);in.putInt("At",clock);}

    /** Where one of the four is kept by this slice, or null. */
    static @Nullable LodgingPeople.Spot spot(MinecraftServer s,BlockPos b,int who){
        var in=state(LodgingStory.house(s,b));if(who==LodgingPeople.DANIEL&&in.getBoolean("SteppedOut"))return DANIEL_OUT;
        return null;
    }
    /** Whether the house is settled enough for it: the first attempt done, the taking over and nobody left in the walls. */
    static boolean ready(MinecraftServer s,BlockPos b){
        var h=LodgingStory.house(s,b);if(h.getInt(LodgingMeeting.STAGE)!=LodgingMeeting.DONE)return false;
        var t=LodgingTaken.state(h);int tp=t.getInt("Phase");
        if(tp!=LodgingTaken.NONE&&!(tp==LodgingTaken.OVER&&t.getBoolean("Shut")))return false;
        return t.getCompound("InWall").isEmpty()&&t.getCompound("Going").isEmpty();
    }
    /** Whether a reader knows enough to put it to him: the rule seen, Vera's truth heard, or the third way learned. */
    static boolean knows(CompoundTag own){return LodgingPeople.sawTheRule(own)||own.getBoolean(LodgingVera.TRUTH)||own.getBoolean(LodgingCellar.KNOWS_THIRD);}

    // ------------------------------------------------------------------------------------------------ every tick
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var s=l.getServer();var h=LodgingStory.house(s,b);var in=state(h);int clock=h.getInt("Clock");int ph=in.getInt("Phase");
        var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);var daniel=LodgingPeople.find(l,b,LodgingPeople.DANIEL);
        boolean changed=false;
        switch(ph){
            case NONE->changed=panic(l,b,h,in,dan,clock);
            case GOING->{
                if(dan==null){go(in,NONE,clock);changed=true;break;}
                int way=in.getInt("Way");double pace=way==AGREED?AGREED_PACE:way==PANIC?PANIC_PACE:0;
                if(way==FORCE){
                    int near=0;var holders=in.getList("Holders",Tag.TAG_STRING);
                    for(var p:here){if(p.isSpectator()||!p.isAlive())continue;for(int i=0;i<holders.size();i++)if(holders.getString(i).equals(p.getUUID().toString())&&p.distanceTo(dan)<=HOLD_REACH)near++;}
                    if(near>0)in.putInt("HeldAt",clock);
                    else if(clock-in.getInt("HeldAt")>=LET_GO){
                        in.remove("Holders");go(in,NONE,clock);dan.gesture(LodgingPeople.WAIT);LodgingStory.speak(l,b,dan,"Dan",LodgingTexts.DAN_LET_GO);changed=true;break;
                    }
                    pace=Math.min(HOLD_MAX,HOLD_PACE*near);
                    if(dan.gesture()!=LodgingPeople.STRAIN)dan.gesture(LodgingPeople.STRAIN);
                }
                if(pace>0&&LodgingTaken.walk(l,b,in,"Leg",dan,route(b,in),doors(in),pace)){
                    dan.gesture(LodgingPeople.WAIT);face(dan,180);
                    if(!LodgingKey.free(s,b)){LodgingKey.release(l,b,h,false);h.putBoolean(LodgingKey.IN,true);in.putInt("DanielLeg",0);go(in,STEPPING,clock);}
                    else go(in,ENTERING,clock);
                    in.putInt("Leg",0);
                }else if(way!=FORCE&&dan.gesture()!=LodgingPeople.WAIT)dan.gesture(LodgingPeople.WAIT);
                changed=true;
            }
            case STEPPING->{
                // The straps hang open; Daniel steps out of the frame and aside, and stands where he can see it.
                if(daniel==null||LodgingTaken.walk(l,b,in,"DanielLeg",daniel,List.of(new Vec3(-7.5,0,-27.2),STEP_OUT),Map.of(),.08)){
                    in.putBoolean("SteppedOut",true);if(daniel!=null){daniel.gesture(LodgingPeople.WAIT);face(daniel,DANIEL_OUT.look());}
                    go(in,ENTERING,clock);
                }
                changed=true;
            }
            case ENTERING->{
                if(dan==null){go(in,GONE,clock);changed=true;break;}
                if(LodgingTaken.walk(l,b,in,"Leg",dan,List.of(IN),Map.of(),.05)){
                    face(dan,0);dan.gesture(LodgingPeople.HELD);dan.noPhysics=true;dan.setNoGravity(true);
                    l.playSound(null,dan.blockPosition(),SoundEvents.CHAIN_PLACE,SoundSource.NEUTRAL,.5F,.7F);
                    for(var p:here){
                        if(p.isSpectator()||p.distanceTo(dan)>16||!p.hasLineOfSight(dan))continue;
                        var own=LodgingHouse.own(p);own.putBoolean(SAW,true);own.putInt("ReturnWay0613",in.getInt("Way"));LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.DAN_SINKS);
                    }
                    go(in,SINKING,clock);
                }
                changed=true;
            }
            case SINKING->{
                if(dan==null){go(in,GONE,clock);changed=true;break;}
                // Drawn back into the plaster, a little at a time, his face last.
                face(dan,0);dan.setDeltaMovement(Vec3.ZERO);
                int into=clock-in.getInt("At");var at=LodgingInjury.abs(b,IN);
                dan.setPos(at.x,at.y,at.z-.75*Math.min(1,into/(double)SINK));
                if(into%25==0)l.playSound(null,dan.blockPosition(),SoundEvents.GRAVEL_STEP,SoundSource.NEUTRAL,.35F,.5F);
                if(into>=SINK){dan.discard();go(in,GONE,clock);l.playSound(null,BlockPos.containing(at),SoundEvents.SAND_PLACE,SoundSource.NEUTRAL,.6F,.6F);}
                changed=true;
            }
            case GONE->changed=coda(l,b,in,daniel,here,clock);
            default->{}
        }
        if(changed)LodgingStory.save(s,b,h);
    }
    /** Vera, set against and with nobody's hands promised her, uses what the walls did to him. */
    private static boolean panic(ServerLevel l,BlockPos b,CompoundTag h,CompoundTag in,@Nullable LiteraryActor dan,int clock){
        if(dan==null||in.getBoolean("PanicStopped")||!ready(l.getServer(),b)||LodgingComplete.begun(l.getServer(),b)||LodgingSever.severed(l.getServer(),b))return false;
        var v=LodgingTaken.vera(h);var t=LodgingTaken.state(h);
        if(v.getInt("Phase")!=LodgingTaken.V_CALLED||v.getInt("Agreed")>0||!t.getBoolean("DanWalled")||clock-v.getInt("At")<PANIC_AFTER)return false;
        var vera=LodgingPeople.find(l,b,LodgingPeople.VERA);
        if(vera!=null){for(var line:LodgingTexts.VERA_PANIC){vera.say("Vera",line);vera.speaks(line);}
            for(var p:LodgingHouse.readers(l,b))if(!p.isSpectator())for(var line:LodgingTexts.VERA_PANIC)Talk.line(p,"Vera",line);}
        LodgingStory.speak(l,b,dan,"Dan",LodgingTexts.DAN_PANICS);
        begin(l,b,in,dan,PANIC,clock);return true;
    }
    /** Daniel asks after the old dog; Rae tells a reader instead; he hears it. */
    private static boolean coda(ServerLevel l,BlockPos b,CompoundTag in,@Nullable LiteraryActor daniel,List<ServerPlayer> here,int clock){
        int into=clock-in.getInt("At");int beat=in.getInt("Beat");
        if(beat==0&&into>=CODA){if(daniel!=null)LodgingStory.speak(l,b,daniel,"Daniel",LodgingTexts.DANIEL_DOG);in.putInt("Beat",1);return true;}
        if(beat==1&&into>=CODA+CODA_GAP){
            var rae=LodgingPeople.find(l,b,LodgingPeople.RAE);ServerPlayer near=null;double best=Double.MAX_VALUE;
            if(rae!=null)for(var p:here){if(p.isSpectator())continue;double d=p.distanceToSqr(rae);if(d<best){best=d;near=p;}}
            if(rae!=null&&near!=null){face(rae,LodgingPeople.yawTo(rae,near.getEyePosition()));LodgingPeople.says(near,rae,"Rae",LodgingTexts.RAE_DOG);}
            for(var p:here)if(!p.isSpectator()&&(rae!=null&&p.distanceTo(rae)<=LodgingDaniel.HEARD||daniel!=null&&p.distanceTo(daniel)<=LodgingDaniel.HEARD)){var own=LodgingHouse.own(p);own.putBoolean(HEARD_DOG,true);LodgingHouse.save(p,own);}
            in.putInt("Beat",2);return true;
        }
        if(beat==2&&into>=CODA+CODA_GAP*2){
            if(daniel!=null){LodgingStory.speak(l,b,daniel,"Daniel",LodgingTexts.DANIEL_HEARS);
                for(var p:here)if(!p.isSpectator()&&p.distanceTo(daniel)<=10)LodgingHouse.cue(p,LodgingTexts.DANIEL_HEARS_CUE);}
            go(in,DONE,clock);return true;
        }
        return false;
    }
    private static void begin(ServerLevel l,BlockPos b,CompoundTag in,LiteraryActor dan,int way,int clock){
        in.putInt("Way",way);in.putInt("Leg",0);in.putInt("HeldAt",clock);in.putString("From",zone(b,dan));go(in,GOING,clock);
    }
    /** Where he sets out from: the parlor, Daniel's room, or the hall and passage. */
    private static String zone(BlockPos b,LiteraryActor dan){var r=LodgingInjury.rel(b,dan);return LodgingSite.inParlor(r)?"parlor":LodgingSite.inDaniels(r)?"daniels":"hall";}
    private static List<Vec3> route(BlockPos b,CompoundTag in){return route(in.getString("From"));}
    /** From a zone (parlor, daniels, hall) to in front of the frame, through the real doors. */
    static List<Vec3> route(String from){
        return switch(from){
            // Out past the armchair's corner first, then the door; into Daniel's room clear of his chair.
            case "parlor"->List.of(new Vec3(-5.6,0,-7.6),new Vec3(-5.6,0,-8.5),new Vec3(-2.6,0,-8.5),new Vec3(0,0,-8.5),new Vec3(0,0,-14),new Vec3(-7.5,0,-14),new Vec3(-7.5,0,-18.5),new Vec3(-5.6,0,-20),new Vec3(-5.6,0,-26.6),FRONT);
            case "daniels"->List.of(FRONT);
            default->List.of(new Vec3(0,0,-5.5),new Vec3(0,0,-14),new Vec3(-7.5,0,-14),new Vec3(-7.5,0,-18.5),new Vec3(-5.6,0,-20),new Vec3(-5.6,0,-26.6),FRONT);
        };
    }
    private static Map<Integer,BlockPos> doors(CompoundTag in){return doors(in.getString("From"));}
    static Map<Integer,BlockPos> doors(String from){
        return switch(from){
            case "parlor"->Map.of(2,LodgingSite.PARLOR_DOOR,6,LodgingSite.DANIEL_DOOR);
            case "daniels"->Map.of();
            default->Map.of(3,LodgingSite.DANIEL_DOOR);
        };
    }
    private static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}

    // ------------------------------------------------------------------------------------------------ a hand on Dan
    /** A hand on Dan. True if this slice answered it. */
    static boolean talk(ServerPlayer p,BlockPos b,LiteraryActor a,int who,CompoundTag own){
        if(who!=LodgingPeople.DAN)return false;
        var s=p.server;var in=state(LodgingStory.house(s,b));int ph=in.getInt("Phase");
        var replies=new ArrayList<Talk.Reply>();
        if(ph==GOING&&in.getInt("Way")==PANIC)replies.add(Talk.reply(LodgingTexts.R_STOP_DAN,LodgingReturn::stop));
        else if(ph==NONE&&ready(s,b)&&knows(own)&&!LodgingComplete.begun(s,b)&&!LodgingSever.severed(s,b)){
            if(!in.getBoolean("Refused"))replies.add(Talk.reply(LodgingTexts.R_GO_BACK,LodgingReturn::putIt));
            else if(!holding(in,p))replies.add(Talk.reply(LodgingTexts.R_TAKE_HIM,LodgingReturn::take));
        }else if(ph==GOING&&in.getInt("Way")==FORCE&&knows(own)&&!holding(in,p))replies.add(Talk.reply(LodgingTexts.R_TAKE_HIM,LodgingReturn::take));
        if(replies.isEmpty())return false;
        Talk.begin(p,LodgingPeople.TALK+"return","Dan");replies.add(Talk.act(LodgingTexts.R_NOTHING,Talk::close));Talk.ask(p,replies);
        return true;
    }
    private static boolean holding(CompoundTag in,ServerPlayer p){var l=in.getList("Holders",Tag.TAG_STRING);for(int i=0;i<l.size();i++)if(l.getString(i).equals(p.getUUID().toString()))return true;return false;}
    /** "You could go back through. He'd walk out." */
    private static void putIt(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);
        if(dan==null||in.getInt("Phase")!=NONE||in.getBoolean("Refused")){Talk.close(p);return;}
        var own=LodgingHouse.own(p);own.putBoolean(PUT_IT,true);LodgingHouse.save(p,own);
        boolean willing=h.getBoolean(LodgingMeeting.SUPPORTED)||h.getBoolean(LodgingMeeting.TOLD_RAE)||own.getBoolean(LodgingKey.TOLD_DAN_RAE);
        if(willing){for(var line:LodgingTexts.DAN_AGREES)LodgingPeople.says(p,dan,"Dan",line);begin(l,b,in,dan,AGREED,h.getInt("Clock"));}
        else{in.putBoolean("Refused",true);LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_REFUSES);}
        LodgingStory.save(s,b,h);Talk.end(p);
    }
    /** "Then we'll take you." The reader's hand on his arm. */
    private static void take(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);
        int ph=in.getInt("Phase");if(dan==null||!(ph==NONE||ph==GOING&&in.getInt("Way")==FORCE)||holding(in,p)){Talk.close(p);return;}
        var list=in.getList("Holders",Tag.TAG_STRING);list.add(StringTag.valueOf(p.getUUID().toString()));in.put("Holders",list);
        var own=LodgingHouse.own(p);own.putBoolean(HELD,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.YOU_HOLD_DAN);
        if(ph==NONE){LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_FORCED);begin(l,b,in,dan,FORCE,h.getInt("Clock"));}
        LodgingStory.save(s,b,h);Talk.end(p);
    }
    /** "It isn't coming. Stop." He stops where he is. */
    private static void stop(ServerPlayer p){
        var b=LiteraryVignettes.base(p,LodgingPeople.PLACE);if(b==null){Talk.close(p);return;}
        var l=p.serverLevel();var s=p.server;var h=LodgingStory.house(s,b);var in=state(h);var dan=LodgingPeople.find(l,b,LodgingPeople.DAN);
        if(dan==null||in.getInt("Phase")!=GOING||in.getInt("Way")!=PANIC){Talk.close(p);return;}
        go(in,NONE,h.getInt("Clock"));in.putBoolean("PanicStopped",true);dan.gesture(LodgingPeople.WAIT);
        var own=LodgingHouse.own(p);own.putBoolean(STOPPED,true);LodgingHouse.save(p,own);
        LodgingPeople.says(p,dan,"Dan",LodgingTexts.DAN_STOPS);LodgingStory.save(s,b,h);Talk.end(p);
    }
    /** For a house built again. */
    public static void clear(MinecraftServer s,BlockPos b){LodgingStory.house(s,b).remove(STATE);}
}
