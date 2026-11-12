package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.532, Gail's call (docs/PELL_HOUSE_CALL_0_4_532.md), from the proposal's "1. The placement": "The morning: June has made
 * breakfast, and Gail calls to see how it went"; and from "Being framed": Gail "has always just spoken to June", and the older sibling
 * "answers her on the conversation panel".
 *
 * <ul>
 * <li><b>The ring.</b> A while after June has washed up, the telephone on the kitchen wall rings. "That'll be Gail." June goes to it.
 * A reader who gets there first and lifts it hears Gail ask for June first, and June takes it from them.</li>
 * <li><b>June's side.</b> June talks to Gail first, with a pause after each line while Gail talks: the pair of them slept like logs
 * (or, if June met a reader out in the night, one of them was up and she saw them back to bed), Tilly ate every bite, Callum has been
 * good as gold. Then she holds the receiver out: "It's Gail, love. She wants a word."</li>
 * <li><b>Each reader's word.</b> A hand on the receiver, or on June holding it out, puts the reader on the line, one reader at a time.
 * Gail has just been talking to June, and asks how the first night was. The reader answers on the conversation panel: it was fine;
 * they didn't sleep much; June was out in the garden in the night, or Callum was standing out in the dark (only if they saw it); they
 * don't want to stay; or they say nothing. Gail answers each with June's version already in her, says she'll ring tomorrow and asks
 * for June back. Every reader at the house gets a turn; June waits a while for any who have not come.</li>
 * <li><b>June's goodbye.</b> June has it back. What she says to Gail answers what was said in front of her, and once she has hung up
 * she has a word for the room if it needs one.</li>
 * </ul>
 *
 * <p>The call is the first visit's last scene for now: afterwards the morning waits. Nobody is hurt and nothing is forced; no answer
 * marks anybody yet (there is nothing on this first visit to answer for). What each reader said is kept for the account.
 */
public final class PellCall {
    private PellCall(){}
    /** After breakfast: the telephone ringing; June on it to Gail; June holding it out for the readers; June back on it; hung up. */
    public static final int RINGING=PellMorning.AFTER+1,ON_PHONE=RINGING+1,OFFERED=RINGING+2,BACK=RINGING+3,DONE=RINGING+4;
    /** The telephone, on the kitchen's east wall by the arch, and where June stands at it, facing it. */
    public static final BlockPos PHONE=new BlockPos(-7,1,-69);
    public static final PellPeople.Spot AT_PHONE=new PellPeople.Spot(-7.1,-68.5,-90);
    /** Which way June turns, holding the receiver out: to the kitchen and the arch from the hall. */
    static final float HOLDING_OUT=67;
    /** Her way to it from the sink. */
    static final Vec3[] TO_PHONE={PellMorning.v(-8.3,-69.1),PellMorning.v(AT_PHONE.x(),AT_PHONE.z())};
    /** How long after the washing up the telephone rings; how often it rings; how long June listens between her lines; how long she holds
     *  it out with nobody coming before she takes it back; how long a reader may keep it; how far from it they may go with it. */
    public static final int RINGS_AFTER=100,RING_EVERY=40,LISTEN=30,OFFER_FOR=900,HOLD_FOR=1200;
    public static final double REACH=8;
    /** The conversation panel's exchange with Gail. */
    static final String KEY="pell_gail",WHO="Gail, on the phone",GAIL="Gail";

    /** For tests: the stage the call has got to, from RINGING to DONE, or before it. */
    public static int stage(BlockPos b){return PellPeople.household(b).step;}
    /** For tests: who has the receiver now, if a reader does. */
    public static @Nullable UUID caller(BlockPos b){return PellPeople.household(b).caller;}

    /**
     * For tests: the morning as breakfast leaves it, without walking it. First light has dried the line; Callum is in and at the end of
     * the hall; Tilly is up, in Nessa's room with her cane; June has unlocked the outside doors, cleared the table and washed up; and it
     * is eight. The telephone rings when it would.
     */
    public static void afterBreakfast(ServerLevel l,BlockPos b){
        PellMorning.smallHours(l,b);var h=PellPeople.household(b);
        for(int x:PellNight.CELLS)PellNight.paint(l,b,x,PellLine.SECOND);
        h.dawned=true;h.callumOut=false;h.copied=0;h.copyOf=null;
        for(var door:PellVisit.LOCKS)if(PellVisit.locked(b,door))PellVisit.unlock(b,door);
        var tilly=PellPeople.find(l,b,PellPeople.TILLY);
        if(tilly!=null){h.asleep[PellPeople.TILLY]=false;PellVisit.dress(tilly,PellPeople.TILLY);PellVisit.stand(l,b,h,PellPeople.TILLY,PellVisit.TILLY_ROOM);}
        var callum=PellPeople.find(l,b,PellPeople.CALLUM);
        if(callum!=null){callum.setXRot(0);callum.gesture(0);h.asleep[PellPeople.CALLUM]=false;PellVisit.stand(l,b,h,PellPeople.CALLUM,PellPeople.FIRST_VISIT[PellPeople.CALLUM]);}
        var june=PellPeople.find(l,b,PellPeople.JUNE);
        if(june!=null){PellVisit.dress(june,PellPeople.JUNE);PellVisit.stand(l,b,h,PellPeople.JUNE,PellVisit.SINK);}
        h.time=PellMorning.EIGHT;h.step=PellMorning.AFTER;h.waypoint=2;h.next=0;
        // Washed up already: the telephone rings once the pause after it has gone by.
        h.clearedAt=l.getGameTime()-PellMorning.WASHING;
    }
    /** The call undone, for the visit beginning again: the receiver back on its hook and nobody on the line. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        if(h.caller!=null){var p=l.getServer().getPlayerList().getPlayer(h.caller);if(p!=null&&Talk.key(p).equals(KEY))Talk.close(p);}
        h.ringAt=0;h.offeredAt=0;h.heldAt=0;h.caller=null;h.answered=null;h.callSaid=0;h.toldUp=false;h.spoke.clear();h.toldGail.clear();
        phone(l,b,0);
    }

    // ------------------------------------------------------------------------------------------------ the call
    private static void step(PellPeople.Household h,int step){h.step=step;h.waypoint=0;h.nudgedAt=0;}

    /** A while after the washing up: the telephone rings, and June goes to it. */
    static void ring(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,long now){
        step(h,RINGING);h.ringAt=now;h.answered=null;h.callSaid=0;h.toldUp=false;h.spoke.clear();h.toldGail.clear();
        PellVisit.say(h,june,PellPeople.JUNE,PellTexts.JUNE_THATLL_BE_GAIL,now);
        PellVisit.go(h,PellPeople.JUNE,-1,AT_PHONE.look(),TO_PHONE);
    }
    /** Every tick for the house from the ring (PellMorning.tick hands it on). */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        switch(h.step-RINGING){
            case 0->ringing(l,b,h,june,here,now);
            case 1->onPhone(l,b,h,june,here,now);
            case 2->offered(l,b,h,june,here,now);
            case 3->back(l,b,h,june,here,now);
            default->{}
        }
    }
    /** The bell, until somebody lifts the receiver; June at the telephone takes it up, or takes it from whoever lifted it. */
    private static void ringing(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.answered==null&&(now-h.ringAt)%RING_EVERY==0)sound(l,b,WorkPrintRegistry.RING.get(),1F,1F);
        if(!h.way[PellPeople.JUNE].isEmpty()&&!PellVisit.near(june,b,PellMorning.v(AT_PHONE.x(),AT_PHONE.z()),.6))return;
        if(!h.way[PellPeople.JUNE].isEmpty()){h.way[PellPeople.JUNE].clear();h.rest[PellPeople.JUNE]=AT_PHONE.look();}
        if(h.answered==null){phone(l,b,1);sound(l,b,PhoneSounds.PICKUP.get(),.8F,1F);}
        else{var p=player(l,h.answered);if(p!=null&&Talk.key(p).equals(KEY))Talk.close(p);}
        toEar(h,june);step(h,ON_PHONE);h.callSaid=0;h.next=now+10;
    }
    /** June's side of it, a pause after each line while Gail talks; then she holds the receiver out to the room. */
    private static void onPhone(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(!PellVisit.ready(h,now,june))return;
        var lines=juneSide(h,here);
        if(h.callSaid<lines.size()){
            String line=lines.get(h.callSaid++);
            PellVisit.say(h,june,PellPeople.JUNE,line,now);h.next+=LISTEN;
            if(line.equals(PellTexts.JUNE_UP))h.toldUp=true;
            PellVisit.present(june,"HeardJuneOnPhone",here);
            return;
        }
        step(h,OFFERED);h.offeredAt=now;holdOut(h,june);
        PellVisit.say(h,june,PellPeople.JUNE,PellTexts.JUNE_HOLDS_OUT,now);
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&p.distanceToSqr(june)>PellVisit.HEAR*PellVisit.HEAR)PellHouse.cue(p,PellTexts.PHONE_FAR);
    }
    /** What June tells Gail, in order. Whether a reader was up in the night is hers to tell: she met them, or they spoke to her, or they
     *  stepped in her paint. */
    static List<String> juneSide(PellPeople.Household h,List<ServerPlayer> here){
        var out=new ArrayList<String>();
        if(h.answered!=null){out.add(PellTexts.JUNE_TAKES);out.add(PellTexts.JUNE_SORRY);}else out.add(PellTexts.JUNE_HELLO);
        out.add(PellTexts.JUNE_NO_TROUBLE);
        out.add(h.toldUp||upInTheNight(h,here)?PellTexts.JUNE_UP:PellTexts.JUNE_SLEPT);
        out.add(PellTexts.JUNE_TILLY_ATE);out.add(PellTexts.JUNE_CALLUM_GOOD);out.add(PellTexts.JUNE_PUT_ON);
        return out;
    }
    private static boolean upInTheNight(PellPeople.Household h,List<ServerPlayer> here){
        if(h.noticed)return true;
        for(var p:here){var own=PellHouse.own(p);if(own.getBoolean("SpokeToJuneAtNight")||own.getBoolean("Smeared"))return true;}
        return false;
    }
    /**
     * The receiver held out. A reader on the line keeps it until they give it back, go off with it or have had it long enough. Once every
     * reader at the house has had a turn, or nobody has come for it in a while, June has it back.
     */
    private static void offered(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(h.caller!=null){
            var p=player(l,h.caller);
            boolean gone=p==null||!p.isAlive()||p.isSpectator()||!here.contains(p)||p.position().distanceTo(Vec3.atCenterOf(b.offset(PHONE)))>REACH||now-h.heldAt>HOLD_FOR;
            if(!gone)return;
            // Off with it, or held too long without a word: the receiver is June's again, and what was not said stays unsaid.
            if(p!=null){
                if(Talk.key(p).equals(KEY))Talk.close(p);
                if(!h.toldGail.containsKey(p.getUUID())){h.toldGail.put(p.getUUID(),PellTexts.WALKED_OFF);var own=PellHouse.own(p);own.putString("ToldGail",PellTexts.WALKED_OFF);PellHouse.save(p,own);}
            }
            given(l,b,h,june,h.caller,here,now);
            return;
        }
        boolean everyone=true;
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&!h.spoke.contains(p.getUUID()))everyone=false;
        if(!everyone&&now-h.offeredAt<OFFER_FOR)return;
        step(h,BACK);toEar(h,june);h.callSaid=0;h.next=Math.max(h.next,now+10);
    }
    /** Back to June: her goodbye to Gail, answering what was said in front of her; the receiver back on its hook; a word to the room. */
    private static void back(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,List<ServerPlayer> here,long now){
        if(!PellVisit.ready(h,now,june))return;
        var lines=juneBack(h);
        if(h.callSaid<lines.size()){PellVisit.say(h,june,PellPeople.JUNE,lines.get(h.callSaid++),now);h.next+=LISTEN;return;}
        if(h.callSaid==lines.size()){
            h.callSaid++;
            phone(l,b,0);sound(l,b,PhoneSounds.PICKUP.get(),.6F,.8F);
            june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);june.gesture(0);h.rest[PellPeople.JUNE]=HOLDING_OUT;
            for(var p:here){if(!p.isAlive()||p.isSpectator())continue;var own=PellHouse.own(p);if(!own.getBoolean("GailRang")){own.putBoolean("GailRang",true);PellHouse.save(p,own);}}
            var said=h.toldGail.values();
            String room=said.contains(PellTexts.JUNE_OUT)?PellTexts.JUNE_INSIDE_LINE:said.contains(PellTexts.LEAVE)?PellTexts.JUNE_GIVE_IT_TIME:null;
            if(room!=null)PellVisit.say(h,june,PellPeople.JUNE,room,now);
            return;
        }
        // Hung up: back to the sink, and the morning waits.
        step(h,DONE);PellVisit.go(h,PellPeople.JUNE,-1,PellVisit.SINK.look(),PellMorning.v(-8.3,-69.1),PellMorning.v(PellVisit.SINK.x(),PellVisit.SINK.z()));
    }
    /** June's goodbye, in order: back on (or speaking for readers who never came), each thing said in front of her answered, and goodbye. */
    static List<String> juneBack(PellPeople.Household h){
        var out=new ArrayList<String>();var said=h.toldGail.values();
        out.add(h.spoke.isEmpty()?PellTexts.JUNE_NOBODY:PellTexts.JUNE_BACK);
        if(said.contains(PellTexts.JUNE_OUT))out.add(PellTexts.JUNE_ABOUT_LINE);
        if(said.contains(PellTexts.CALLUM))out.add(PellTexts.JUNE_ABOUT_CALLUM);
        if(said.contains(PellTexts.LEAVE))out.add(PellTexts.JUNE_EARLY_DAYS);
        boolean words=said.contains(PellTexts.FINE)||said.contains(PellTexts.NO_SLEEP)||said.contains(PellTexts.JUNE_OUT)||said.contains(PellTexts.CALLUM)||said.contains(PellTexts.LEAVE);
        if(!h.spoke.isEmpty()&&!words)out.add(PellTexts.JUNE_SHY);
        out.add(PellTexts.JUNE_BYE);
        return out;
    }

    // ------------------------------------------------------------------------------------------------ a reader's hand
    /** A hand on the telephone. True if the call answered it: lifting it as it rings, taking it from June, or being told it is busy. */
    public static boolean hand(ServerPlayer p,BlockPos b,CompoundTag own){
        if(p.isSpectator()||!p.isAlive())return false;
        var h=PellPeople.household(b);var l=p.serverLevel();
        switch(h.step-RINGING){
            case 0->{
                if(h.answered!=null){PellHouse.cue(p,p.getUUID().equals(h.answered)?PellTexts.HOLDING:PellTexts.PHONE_BUSY);return true;}
                // Lifted before June gets to it: Gail asks for June first, and June comes and takes it.
                h.answered=p.getUUID();phone(l,b,1);sound(l,b,PhoneSounds.PICKUP.get(),.8F,1F);own.putBoolean("AnsweredThePhone",true);
                Talk.begin(p,KEY,WHO);Talk.line(p,GAIL,PellTexts.GAIL_FOR_JUNE);Talk.end(p);
                return true;
            }
            case 1,3->{PellHouse.cue(p,PellTexts.JUNE_ON_PHONE);return true;}
            case 2->{
                if(p.getUUID().equals(h.caller)){if(!Talk.resume(p,KEY))PellHouse.cue(p,PellTexts.PHONE_BUSY);return true;}
                if(h.caller!=null){PellHouse.cue(p,PellTexts.PHONE_BUSY);return true;}
                if(h.spoke.contains(p.getUUID())){PellHouse.cue(p,PellTexts.HAD_A_TURN);return true;}
                var june=PellPeople.find(l,b,PellPeople.JUNE);if(june==null)return false;
                take(p,h,june,own,l.getGameTime());
                return true;
            }
            default->{return false;}
        }
    }
    /** A hand on June: on the telephone she lifts a finger; holding it out, the reader takes it from her. False otherwise. */
    public static boolean june(ServerPlayer p,BlockPos b,CompoundTag own){
        var h=PellPeople.household(b);
        return switch(h.step-RINGING){
            case 1,3->{PellHouse.cue(p,PellTexts.JUNE_ON_PHONE);yield true;}
            case 2->hand(p,b,own);
            default->false;
        };
    }
    /** The reader takes the receiver: Gail, who has just been talking to June, asks how the first night was. */
    private static void take(ServerPlayer p,PellPeople.Household h,LiteraryActor june,CompoundTag own,long now){
        h.caller=p.getUUID();h.heldAt=now;
        june.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);june.gesture(0);
        own.putBoolean("SpokeToGail",true);
        Talk.begin(p,KEY,WHO);Talk.line(p,GAIL,PellTexts.GAIL_HELLO);Talk.line(p,GAIL,PellTexts.GAIL_ASKS);
        var replies=new ArrayList<Talk.Reply>();
        replies.add(Talk.reply(PellTexts.TELL_FINE,q->answer(q,PellTexts.FINE)));
        replies.add(Talk.reply(PellTexts.TELL_NO_SLEEP,q->answer(q,PellTexts.NO_SLEEP)));
        if(own.getBoolean("SawJuneOut"))replies.add(Talk.reply(PellTexts.TELL_JUNE_OUT,q->answer(q,PellTexts.JUNE_OUT)));
        if(own.getBoolean("SawCallumOnLine"))replies.add(Talk.reply(PellTexts.TELL_CALLUM,q->answer(q,PellTexts.CALLUM)));
        replies.add(Talk.reply(PellTexts.TELL_LEAVE,q->answer(q,PellTexts.LEAVE)));
        replies.add(Talk.act(PellTexts.TELL_NOTHING,q->answer(q,PellTexts.NOTHING)));
        Talk.ask(p,replies);
    }
    /** What the reader told Gail: kept in their record; Gail answers it, says she'll ring tomorrow and asks for June back. */
    static void answer(ServerPlayer p,String told){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null){Talk.close(p);return;}
        var h=PellPeople.household(b);
        if(h.step!=OFFERED||!p.getUUID().equals(h.caller)){Talk.close(p);return;}
        h.toldGail.put(p.getUUID(),told);
        var own=PellHouse.own(p);own.putString("ToldGail",told);PellHouse.save(p,own);
        for(var line:PellTexts.gail(told,h.toldUp))Talk.line(p,GAIL,line);
        Talk.line(p,GAIL,PellTexts.GAIL_BYE);
        Talk.ask(p,Talk.act(PellTexts.HAND_BACK,PellCall::handBack));
    }
    /** The receiver given back to June. */
    static void handBack(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null)return;
        var h=PellPeople.household(b);if(h.step!=OFFERED||!p.getUUID().equals(h.caller))return;
        var l=p.serverLevel();var june=PellPeople.find(l,b,PellPeople.JUNE);if(june==null)return;
        given(l,b,h,june,p.getUUID(),PellHouse.readers(l,b),l.getGameTime());
    }
    /** The receiver back in June's hand from a reader: their turn done. If another reader at the house has not had one, she holds it out
     *  to them, and waits a while again. */
    private static void given(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,UUID from,List<ServerPlayer> here,long now){
        h.spoke.add(from);h.caller=null;h.offeredAt=now;holdOut(h,june);
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&!h.spoke.contains(p.getUUID())){PellVisit.say(h,june,PellPeople.JUNE,PellTexts.JUNE_NEXT,now);break;}
    }

    // ------------------------------------------------------------------------------------------------ the telephone
    /** The receiver to June's ear, facing the telephone. */
    private static void toEar(PellPeople.Household h,LiteraryActor june){
        june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.RECEIVER.get()));june.gesture(PellPeople.PHONE);h.rest[PellPeople.JUNE]=AT_PHONE.look();
    }
    /** The receiver held out to the room. */
    private static void holdOut(PellPeople.Household h,LiteraryActor june){
        june.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PellRegistry.RECEIVER.get()));june.gesture(PellPeople.OFFER);h.rest[PellPeople.JUNE]=HOLDING_OUT;
    }
    /** The telephone: its receiver on the hook (0) or off it (1). */
    static void phone(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(PHONE);var s=l.getBlockState(at);
        if(PellThing.is(s,PellThing.Kind.PHONE)&&s.getValue(PellThing.STAGE)!=stage)l.setBlock(at,s.setValue(PellThing.STAGE,stage),3);
    }
    /** For tests: whether the receiver is off the hook. */
    public static boolean off(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(PHONE));return PellThing.is(s,PellThing.Kind.PHONE)&&s.getValue(PellThing.STAGE)==1;}
    private static void sound(ServerLevel l,BlockPos b,SoundEvent s,float volume,float pitch){var at=Vec3.atCenterOf(b.offset(PHONE));l.playSound(null,at.x,at.y,at.z,s,SoundSource.BLOCKS,volume,pitch);}
    private static @Nullable ServerPlayer player(ServerLevel l,@Nullable UUID id){return id==null?null:l.getServer().getPlayerList().getPlayer(id);}
}
