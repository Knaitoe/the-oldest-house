package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.686, A Place for You, slice 2 (docs/A_PLACE_FOR_YOU_0_4_686.md): somebody at the door (the proposal's N8 and N9). Once the house
 * is being opened and Kit is out through the gate, the front door swings shut and somebody knocks: a man's voice asks whether the
 * spare key is still where it was. The reader can ask through the door, open it (nobody is on the step; the voice is by the gate), or
 * leave it. While it stands open the house offers its bargain, a stage at a time, each where the reader can see it: the gap in the
 * household's pictures fills with a man in a work shirt; the reader's chair goes from the table; the person they first sat with asks if
 * they have met. Ren says aloud what letting him in would cost. Shut, the door lets things go back as they were. With the way out open
 * and the reader's picture down, the voice says her name once more and stops; only then do Mara and Alex decide. Before that, the
 * private questions: Kit what help they want, Mara whether she wants people to stay or to be unable to leave, Alex and Ren whether they
 * go together. Nothing here can hurt, and nobody has to decide while a clock runs.
 */
final class PlaceForYouDoor {
    private PlaceForYouDoor(){}
    /** Ticks from the door shutting to the knock, from the door opening to the voice by the gate, per stage of the bargain, before the
     * house lets things go back once the door is shut, before the voice stops once the house is open, and between knocks while shut. */
    static final int KNOCK_DELAY=30,VOICE_DELAY=60,STAGE=200,SETTLE=100,LAST_WORD=100,AGAIN=600;
    static final String KEY="Mara? Is the spare key still where it was? It isn't under the step.";
    static final String COST="If she lets him in, she gets the old evening back. We weren't in it, and neither were you. Nobody would remember tonight.";

    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}
    /** The voice's words in the reader's open exchange. */
    static void voice(ServerPlayer p,PlaceForYou.House h,String text){Talk.line(p,"A voice",text);h.lastVoice=h.tick;}
    static boolean ready(CompoundTag own){return own.getBoolean("Propped")&&own.getBoolean("PictureDown");}

    /** Each tick of the open house, for this reader's evening. */
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;long now=h.tick;
        if(!own.getBoolean("Knocked")){
            boolean kitOut=own.getBoolean("KitLeft")||h.kitGoneAt>=0&&now-h.kitGoneAt>=240;
            if(h.knockAt<0){
                if(kitOut&&PlaceForYouSite.inHouse(r)&&!Talk.waiting(p)){
                    if(!own.getBoolean("Propped")&&PlaceForYouSite.doorOpen(l,b)){PlaceForYouSite.frontDoor(l,b,false);l.playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,.9F);}
                    h.knockAt=(int)now+KNOCK_DELAY;
                }
            }else if(now>=h.knockAt){
                h.knockAt=-1;knock(l,h);
                own.putBoolean("Knocked",true);PlaceForYou.save(p,own);
                if(PlaceForYouSite.doorOpen(l,b))cue(p,"Three knocks on the frame of the open door. There's nobody on the step.");
                else cue(p,"Three knocks at the front door.");
                PlaceForYou.above(h,PlaceForYou.actor(l,h,"Mara"),"Nobody knocks here. Everybody just comes in.");
            }
            return;
        }
        if(own.getBoolean("TheoSilent")){restore(l,h,p,own,r);return;}
        boolean open=PlaceForYouSite.doorOpen(l,b);
        // The voice, from by the gate, a moment after the door is opened: what it asks the first time, after that only that it's him.
        if(open&&h.voiceAt<0&&!own.getBoolean("VoiceHeard"))h.voiceAt=(int)now+VOICE_DELAY;
        if(open&&h.voiceAt>=0&&now>=h.voiceAt){
            h.voiceAt=-1;h.lastVoice=now;
            if(!own.getBoolean("VoiceHeard")){own.putBoolean("VoiceHeard",true);PlaceForYou.save(p,own);cue(p,"From by the gate, a man's voice: \""+KEY+"\"");}
            else cue(p,"From by the gate, the same voice: \"Tell her it's only me.\"");
        }
        if(open){h.shutFor=0;if(!ready(own)){h.openFor++;bargain(l,h,p,own,r);}}
        else{h.shutFor++;
            if(own.getBoolean("VoiceHeard")&&h.shutFor%AGAIN==0&&h.knocks<3){h.knocks++;knock(l,h);if(PlaceForYouSite.inHouse(r))cue(p,"Knocking at the front door again.");}
            if(h.shutFor>=SETTLE)restore(l,h,p,own,r);}
        // The cost, said aloud by Ren once the reader's chair has gone, as soon as the reader is near enough to hear it.
        own=PlaceForYou.own(p);
        if(own.getInt("Bargain")>=2&&!own.getBoolean("CostHeard")&&!Talk.waiting(p)){
            var ren=PlaceForYou.actor(l,h,"Ren");
            if(ren!=null&&p.distanceTo(ren)<8&&p.hasLineOfSight(ren)){PlaceForYou.above(h,ren,COST);PlaceForYou.edit(p,o->o.putBoolean("CostHeard",true));}
        }
        // With the way out open and the reader's picture down, the voice says her name once more, and stops.
        own=PlaceForYou.own(p);
        if(ready(own)&&own.getBoolean("VoiceHeard")&&!own.getBoolean("TheoUp")&&now-h.lastVoice>=LAST_WORD&&!Talk.waiting(p)){
            cue(p,"From somewhere past the gate: \"...Mara?\" Then nothing more.");
            PlaceForYou.edit(p,o->o.putBoolean("TheoSilent",true));
        }
    }
    static void knock(ServerLevel l,PlaceForYou.House h){
        var at=h.base.offset(PlaceForYouSite.FRONT_DOOR);
        l.playSound(null,at,DoorSounds.KNOCK.get(),SoundSource.BLOCKS,1F,1F);
    }

    /** The house's offer, a stage at a time while the door stands open, each where the reader can see it happen or come to it. */
    static void bargain(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;int stage=own.getInt("Bargain");
        if(stage==0&&h.openFor>=STAGE){
            if(h.openFor==STAGE)PlaceForYou.above(h,PlaceForYou.actor(l,h,"Ren"),"Mara. The pictures.");
            // The gap in the household's row fills as the reader looks at it, or later, unwatched.
            boolean looking=PlaceForYou.looking(p,b,PlaceForYouSite.PICTURES,10);
            if(looking||h.openFor>=STAGE*3&&!PlaceForYouSite.inSitting(r)){
                PlaceForYouSite.pictures(l,b,true);step(p,1);
                if(looking)cue(p,"There's somebody in the gap in the household's row now: a man in a work shirt.");
            }
        }else if(stage==1&&h.openFor>=STAGE*2&&!PlaceForYouSite.inKitchen(r)){
            // The reader's chair goes from the table while they are out of the kitchen.
            PlaceForYouSite.readerChair(l,b,false);step(p,2);
        }else if(stage==2&&h.openFor>=STAGE*3&&!Talk.waiting(p)){
            // The first person the reader sat with asks whether they have met.
            String who=forgetter(l,h,own);var a=PlaceForYou.actor(l,h,who);
            if(a!=null&&p.distanceTo(a)<6&&p.hasLineOfSight(a)){
                PlaceForYou.above(h,a,"Sorry. Have we met?");h.forgetAt=(int)h.tick+40;h.forgetter=who;
                step(p,3);PlaceForYou.edit(p,o->{o.putBoolean("Forgot",true);o.putString("Forgetter",who);});
            }
        }
        if(h.forgetAt>=0&&h.tick>=h.forgetAt){h.forgetAt=-1;PlaceForYou.above(h,PlaceForYou.actor(l,h,h.forgetter),"No. I know you. I don't know why I said that.");}
        // The empty place at the table, once, the next time the reader is in the kitchen.
        own=PlaceForYou.own(p);
        if(own.getInt("Bargain")>=2&&!own.getBoolean("ChairSeen")&&PlaceForYouSite.inKitchen(r)&&!PlaceForYouSite.readerChair(l,b)){
            PlaceForYou.edit(p,o->o.putBoolean("ChairSeen",true));cue(p,"Your chair is gone from the table. Theo's place is still set.");}
    }
    static void step(ServerPlayer p,int stage){PlaceForYou.edit(p,o->{o.putInt("Bargain",stage);if(o.getInt("BargainMax")<stage)o.putInt("BargainMax",stage);});}
    /** Who forgets: the first of them the reader talked with, if they are still in the house; otherwise whoever helped; otherwise Mara. */
    static String forgetter(ServerLevel l,PlaceForYou.House h,CompoundTag own){
        for(String who:new String[]{own.getString("First"),own.getString("Helper"),"Mara"})if(!who.isEmpty()&&!who.equals("Kit")&&PlaceForYou.actor(l,h,who)!=null)return who;
        return "Mara";
    }
    /** Shut, or the voice gone quiet: the pictures and the reader's chair go back as they were, each when the reader is not looking. */
    static void restore(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;if(own.getInt("Bargain")==0&&!PlaceForYouSite.picturesFilled(l,b)&&PlaceForYouSite.readerChair(l,b))return;
        if(PlaceForYouSite.picturesFilled(l,b)&&!PlaceForYou.looking(p,b,PlaceForYouSite.PICTURES,16))PlaceForYouSite.pictures(l,b,false);
        if(!PlaceForYouSite.readerChair(l,b)&&!PlaceForYouSite.inKitchen(r))PlaceForYouSite.readerChair(l,b,true);
        if(!PlaceForYouSite.picturesFilled(l,b)&&PlaceForYouSite.readerChair(l,b)){h.openFor=0;PlaceForYou.edit(p,o->{o.putInt("Bargain",0);o.remove("ChairSeen");});}
    }

    // ------------------------------------------------------------------------------------------------ the front door
    /** A hand on the front door after the knock, before the voice has stopped. */
    static void door(ServerPlayer p,PlaceForYou.House h){
        var l=p.serverLevel();var b=h.base;var own=PlaceForYou.own(p);
        if(!PlaceForYouSite.inHouse(PlaceForYou.rel(p.position(),b))){cue(p,"There's nobody on the step. Whoever knocked is somewhere past the gate.");return;}
        if(PlaceForYouSite.doorOpen(l,b)){
            Talk.begin(p,"door_open","The front door");
            // 0.4.688: with Theo's photograph up where the reader's was, and the cost said aloud, the reader can tell him to come in.
            if(PlaceForYouInvite.canInvite(own)){Talk.ask(p,Talk.reply("Come in, Theo.",q->PlaceForYouInvite.invite(q,h)),Talk.act("Shut it.",q->{PlaceForYouSite.frontDoor(q.serverLevel(),b,false);
                    q.serverLevel().playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,1F);PlaceForYou.edit(q,o->o.putBoolean("Shut",true));Talk.end(q);cue(q,"You shut it.");})
                ,Talk.act("Leave it open.",Talk::end));return;}
            Talk.ask(p,Talk.act("Set the hall chair against it.",q->{PlaceForYouSite.propped(q.serverLevel(),b,true);PlaceForYou.edit(q,o->o.putBoolean("Propped",true));Talk.end(q);
                    cue(q,"You set the hall chair against it. It stays.");PlaceForYou.checkOpen(q,h);})
                ,Talk.act("Shut it.",q->{PlaceForYouSite.frontDoor(q.serverLevel(),b,false);q.serverLevel().playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,1F);
                    PlaceForYou.edit(q,o->o.putBoolean("Shut",true));Talk.end(q);cue(q,"You shut it.");})
                ,Talk.act("Leave it open.",Talk::end));
            return;
        }
        Talk.begin(p,"door","Through the door");
        if(!own.getBoolean("VoiceHeard")){voice(p,h,KEY);PlaceForYou.edit(p,o->o.putBoolean("VoiceHeard",true));}
        else voice(p,h,"It's only me. It's cold out here.");
        doorReplies(p,h);
    }
    static void doorReplies(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);var b=h.base;var replies=new ArrayList<Talk.Reply>();
        if(!own.getBoolean("AskedWho"))replies.add(Talk.reply("Who is this?",q->{voice(q,h,"It's Theo. Who's that? She didn't say anyone was over.");PlaceForYou.edit(q,o->o.putBoolean("AskedWho",true));doorReplies(q,h);}));
        if(!own.getBoolean("AskedSupper"))replies.add(Talk.reply("What's for supper?",q->{voice(q,h,"Whatever she's made. She always makes too much.");PlaceForYou.edit(q,o->o.putBoolean("AskedSupper",true));doorReplies(q,h);}));
        if(!own.getBoolean("AskedWhere"))replies.add(Talk.reply("Where have you been?",q->{voice(q,h,"Out. I said I'd be back for supper. I'm back for supper.");PlaceForYou.edit(q,o->o.putBoolean("AskedWhere",true));doorReplies(q,h);}));
        if(PlaceForYouInvite.canInvite(own))replies.add(Talk.reply("Come in, Theo.",q->PlaceForYouInvite.invite(q,h)));
        replies.add(Talk.act("Open the door.",q->{var l=q.serverLevel();PlaceForYouSite.frontDoor(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.8F,1F);
            PlaceForYou.edit(q,o->o.putBoolean("DoorAnswered",true));h.voiceAt=(int)h.tick+VOICE_DELAY;Talk.end(q);
            cue(q,"Nobody on the step. The path, the gate, the road.");}));
        replies.add(Talk.act("Leave it shut.",Talk::end));
        Talk.ask(p,replies);
    }

    // ------------------------------------------------------------------------------------------------ before the threshold (N9)
    /** Kit, on the way out, asked what help they want. */
    static void kit(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"kit_gate","Kit");PlaceForYou.line(p,h,"Kit","This is me going, then.");
        Talk.ask(p,Talk.reply("What do you need from me?",q->{PlaceForYou.line(q,h,"Kit","Leave the gate open behind me. Tonight, that's all.");PlaceForYou.edit(q,o->{o.putBoolean("AskedKit",true);o.putString("KitWants","gate");});Talk.end(q);})
            ,Talk.reply("Want me to walk you to the road?",q->{PlaceForYou.line(q,h,"Kit","No. I want to do this bit myself.");PlaceForYou.edit(q,o->{o.putBoolean("AskedKit",true);o.putString("KitWants","alone");});Talk.end(q);})
            ,Talk.reply("Go on.",q->{PlaceForYou.line(q,h,"Kit","Going.");Talk.end(q);}));
    }
    /** Mara, while somebody is at the door. */
    static void mara(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"mara_knock","Mara");PlaceForYou.line(p,h,"Mara","He never knocked. He'd come straight in and drop his keys in the drawer.");
        maraReplies(p,h);
    }
    static void maraReplies(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);var replies=new ArrayList<Talk.Reply>();
        if(own.getString("AskedMara").isEmpty())replies.add(Talk.reply("Do you want them to stay, or do you want them unable to leave?",q->{
            String how=PlaceForYou.own(q).getString("Confront");
            PlaceForYou.line(q,h,"Mara",switch(how){
                case "private"->"Unable to leave. That's the true answer. I wish it were the other one.";
                case "kit"->"Kit's already gone. I knew Kit would go. I wanted it to be next year, every year.";
                default->"I wanted them to want to stay. I stopped asking whether they did.";});
            PlaceForYou.edit(q,o->o.putString("AskedMara",how.isEmpty()?"open":how));maraReplies(q,h);}));
        if(!own.getBoolean("AskedIsHim"))replies.add(Talk.reply("Is it him?",q->{PlaceForYou.line(q,h,"Mara","It sounds like him. That's all I know.");PlaceForYou.edit(q,o->o.putBoolean("AskedIsHim",true));maraReplies(q,h);}));
        // 0.4.694: the reassurance, once she has said it only sounds like him.
        if(own.getBoolean("AskedIsHim")&&own.getBoolean("VoiceHeard")&&own.getInt("ReassureStage")==0)replies.add(PlaceForYouBelief.reassure(h));
        replies.add(Talk.reply("I'll leave you be.",Talk::end));
        Talk.ask(p,replies);
    }
    /** Alex and Ren together, while somebody is at the door. */
    static void peers(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"peers","Alex and Ren");PlaceForYou.line(p,h,"Ren","Is that...");PlaceForYou.line(p,h,"Alex","Don't. If we say it, it's him.");
        peerReplies(p,h);
    }
    static void peerReplies(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);var replies=new ArrayList<Talk.Reply>();
        if(!own.getBoolean("AskedPeers"))replies.add(Talk.reply("Will you two go together, or each your own way?",q->{
            PlaceForYou.line(q,h,"Alex","I'm going to the coast. I've said.");PlaceForYou.line(q,h,"Ren","And I'm staying near her. We're allowed to want different things.");
            PlaceForYou.edit(q,o->o.putBoolean("AskedPeers",true));peerReplies(q,h);}));
        if(!own.getBoolean("CostHeard"))replies.add(Talk.reply("What happens if she lets him in?",q->{
            PlaceForYou.line(q,h,"Ren",COST);PlaceForYou.line(q,h,"Alex","It was a good evening. That's the trouble with it.");
            PlaceForYou.edit(q,o->o.putBoolean("CostHeard",true));peerReplies(q,h);}));
        replies.add(Talk.reply("Stay with me a minute.",q->{PlaceForYou.line(q,h,"Ren","All right.");Talk.end(q);}));
        Talk.ask(p,replies);
    }
}
