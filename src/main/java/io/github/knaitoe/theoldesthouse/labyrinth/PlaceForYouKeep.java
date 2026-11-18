package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.687, A Place for You, slice 3 (docs/A_PLACE_FOR_YOU_0_4_687.md): keep a place (the proposal's E2). With the house being opened
 * and the reader's picture down off the fridge, the reader can put it back up instead. Mara pulls their chair out, the supper is warm,
 * and somebody familiar sits down beside them: the first of the household they sat with who is still staying, never Kit, never anyone
 * who has said they are going. Then the cost, shown and not said: a new question gets the answer Mara gave when the reader came in;
 * the familiar gesture comes again, the same way, with the same words; the reader's things are laid out in the bathroom again,
 * untouched. Who already left stays gone; nobody who said they were going is sat back down. Nothing locks: the reader can still walk
 * out of the story, and the account says what was kept.
 */
final class PlaceForYouKeep {
    private PlaceForYouKeep(){}
    /** Ticks seated before Mara speaks, before the gesture, before the question, between the answer and the gesture again, and after it before the end. */
    static final int SPEAK=40,GESTURE=100,ASK=120,AGAIN=80,END=120;
    /** The least gap between the gesture and its repeat, past the cast checks' window for a line said again in earshot. */
    static final int REPEAT_GAP=260;

    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}

    // ------------------------------------------------------------------------------------------------ the fridge
    /** A hand on the fridge with the house being opened and the reader's picture already down. */
    static void fridge(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"fridge","The fridge");Talk.line(p,"","The fridge door, bare where your picture was.");
        Talk.ask(p,Talk.act("Put your picture back up.",q->keep(q,h)),Talk.act("Leave it down.",Talk::end));
    }
    /** The picture back up: the evening kept, with the reader in it. */
    static void keep(ServerPlayer p,PlaceForYou.House h){
        Talk.end(p);var l=p.serverLevel();var b=h.base;var own=PlaceForYou.own(p);
        PlaceForYouSite.picture(l,b,own.getCompound("Snap").getCompound("Drawing"),true);
        boolean knocking=own.getBoolean("Knocked")&&!own.getBoolean("TheoSilent");
        PlaceForYou.edit(p,o->{o.putBoolean("PictureDown",false);o.putInt("Chapter",PlaceForYou.KEEP);o.putInt("KeepStage",0);o.putBoolean("Kept",true);if(knocking)o.putBoolean("VoiceStopped",true);});
        cue(p,"You put your picture back up on the fridge."+(knocking?" The knocking stops.":""));
        // The bargain's two changes the reader could see put back at once: the chair is needed, and Mara pulls it out.
        if(!PlaceForYouSite.readerChair(l,b)){PlaceForYouSite.readerChair(l,b,true);cue(p,"Mara pulls your chair back out from the table.");}
        h.keepAt=-1;h.gestureAt=-1;
        PlaceForYou.above(h,PlaceForYou.actor(l,h,"Mara"),"There. That's where it goes. Sit down, it's still warm.");
        if(own.getBoolean("CostHeard"))PlaceForYou.above(h,PlaceForYou.actor(l,h,"Ren"),"All right. I won't say it again.");
    }

    // ------------------------------------------------------------------------------------------------ the kept evening
    static boolean seated(ServerPlayer p,BlockPos b){return p.getVehicle() instanceof SeatEntity&&p.getVehicle().position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(PlaceForYouSite.READER_CHAIR)))<1.5;}

    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;long now=h.tick;int stage=own.getInt("KeepStage");
        // What the house puts back out of sight: the household's pictures as they were, the supper on the table, the hall chair in the hall.
        if(PlaceForYouSite.picturesFilled(l,b)&&!PlaceForYou.looking(p,b,PlaceForYouSite.PICTURES,16))PlaceForYouSite.pictures(l,b,false);
        if(!own.getBoolean("TableSet")&&!PlaceForYou.looking(p,b,PlaceForYouSite.READER_CHAIR,12)){PlaceForYouSite.table(l,b,true);PlaceForYou.edit(p,o->o.putBoolean("TableSet",true));}
        if(own.getBoolean("Propped")&&!PlaceForYou.looking(p,b,PlaceForYouSite.FRONT_DOOR,24)){
            PlaceForYouSite.propped(l,b,false);PlaceForYouSite.frontDoor(l,b,false);PlaceForYou.edit(p,o->{o.putBoolean("Propped",false);o.putBoolean("DoorClosed",true);});}
        // Laid out again in the bathroom, the moment the reader is not there; seen when the reader looks at it.
        if(stage>=4&&!PlaceForYouSite.routineOut(l,b)&&!PlaceForYouSite.inBathroom(r)){int item=own.getCompound("Snap").getInt("Routine");PlaceForYouSite.routine(l,b,item<0?11:item);}
        if(stage>=4&&!own.getBoolean("RoutineAgain")&&PlaceForYouSite.routineOut(l,b)&&PlaceForYou.looking(p,b,PlaceForYouSite.ROUTINE,8)){
            PlaceForYou.edit(p,o->o.putBoolean("RoutineAgain",true));cue(p,"Laid out again, untouched. Ready for the same moment.");}
        if(stage>=5)return;
        if(stage==0){
            if(!seated(p,b))return;
            String who=beside(own);PlaceForYou.edit(p,o->{o.putString("Beside",who);o.putInt("KeepStage",1);});h.keepAt=(int)now;return;
        }
        if(h.keepAt<0){h.keepAt=(int)now;return;}
        int since=(int)(now-h.keepAt);String who=own.getString("Beside");
        switch(stage){
            case 1->{
                if(since==SPEAK)PlaceForYou.above(h,PlaceForYou.actor(l,h,"Mara"),"Eat. There's more.");
                if(since>=GESTURE&&!who.isEmpty()){gesture(l,h,p,who);h.gestureAt=(int)now;PlaceForYou.edit(p,o->o.putInt("KeepStage",2));h.keepAt=(int)now;}
                else if(since>=GESTURE){PlaceForYou.edit(p,o->o.putInt("KeepStage",2));h.keepAt=(int)now;}
            }
            case 2->{if(since>=ASK&&!Talk.waiting(p))ask(p,h);}
            case 3->{
                if(since>=AGAIN&&(h.gestureAt<0||now-h.gestureAt>=REPEAT_GAP)){
                    if(!who.isEmpty()){gesture(l,h,p,who);PlaceForYou.edit(p,o->o.putBoolean("GestureAgain",true));}
                    PlaceForYou.edit(p,o->o.putInt("KeepStage",4));h.keepAt=(int)now;}
            }
            case 4->{if(since==SPEAK)PlaceForYou.above(h,PlaceForYou.actor(l,h,"Mara"),"Same time tomorrow.");
                // 0.4.694: the dog the reader named on the landing, at the table as if it had always been part of the evening.
                if(since>=SPEAK*2&&PlaceForYouBelief.dogKept(own)&&!own.getBoolean("DogAtTable")){String guide=own.getString("OlderGuide");var g=PlaceForYou.actor(l,h,guide);
                    if(g!=null&&staying(own,guide)){PlaceForYou.above(h,g,PlaceForYouBelief.DOG+" would have had the scraps by now.");PlaceForYou.edit(p,o->o.putBoolean("DogAtTable",true));}}
                if(since>=END&&!Talk.waiting(p)){PlaceForYou.edit(p,o->o.putInt("KeepStage",5));PlaceForYou.finish(p,"kept_a_place");}}
            default->{}
        }
    }
    /** After it is over, the routine is still laid out again for a reader who goes and looks. */
    static void after(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;
        if(!PlaceForYouSite.routineOut(l,b)&&!PlaceForYouSite.inBathroom(r)){int item=own.getCompound("Snap").getInt("Routine");PlaceForYouSite.routine(l,b,item<0?11:item);}
        if(!own.getBoolean("RoutineAgain")&&PlaceForYouSite.routineOut(l,b)&&PlaceForYou.looking(p,b,PlaceForYouSite.ROUTINE,8)){
            PlaceForYou.edit(p,o->o.putBoolean("RoutineAgain",true));cue(p,"Laid out again, untouched. Ready for the same moment.");}
    }

    /** Who sits down beside the reader: the first of them the reader sat with, if they are staying; otherwise whoever helped; Mara; Ren; Alex. Never Kit. */
    static String beside(CompoundTag own){
        for(String who:new String[]{own.getString("First"),own.getString("Helper"),"Mara","Ren","Alex"})if(staying(own,who))return who;
        return "";
    }
    static boolean staying(CompoundTag own,String who){
        return switch(who){
            case "Mara"->!own.getString("MaraEnd").equals("gate");
            case "Ren"->!own.getString("RenEnd").equals("goes");
            case "Alex"->own.getString("AlexEnd").isEmpty();
            default->false;
        };
    }
    /** Where the one beside the reader sits: Alex already sits beside them; anyone else takes Kit's chair, or Alex's if Alex has gone. */
    static void seat(CompoundTag own,Map<String,PlaceForYou.Mark> m){
        String who=own.getString("Beside");if(who.isEmpty()||who.equals("Alex"))return;
        if(!own.getBoolean("ChairTaken"))m.put(who,PlaceForYou.seat(PlaceForYouSite.KIT_CHAIR));
        else if(!own.getString("AlexEnd").isEmpty())m.put(who,PlaceForYou.seat(PlaceForYouSite.ALEX_CHAIR));
    }

    /** The familiar gesture, the same each time it comes. */
    static void gesture(ServerLevel l,PlaceForYou.House h,ServerPlayer p,String who){
        cue(p,switch(who){
            case "Mara"->"Mara rests her hand on your arm while she talks.";
            case "Ren"->"Ren nudges your elbow.";
            default->"Alex tips the last of the jug into your glass before his own.";
        });
        PlaceForYou.above(h,PlaceForYou.actor(l,h,who),line(who));
        l.playSound(null,p.blockPosition(),SoundEvents.WOOL_STEP,SoundSource.PLAYERS,.3F,1F);
    }
    static String line(String who){
        return switch(who){
            case "Mara"->"I'm glad it's you at this table.";
            case "Ren"->"Told you she'd make too much.";
            default->"Drink that before she fills it again.";
        };
    }

    /** A new question for Mara, and the answer she gave when the reader came in. */
    static void ask(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);
        Talk.begin(p,"keep_ask","Mara");
        var replies=new ArrayList<Talk.Reply>();
        replies.add(Talk.reply("What are we doing tomorrow?",q->answer(q,h,"tomorrow")));
        if(own.getBoolean("KitGoing"))replies.add(Talk.reply("Where's Kit tonight?",q->answer(q,h,"kit")));
        if(own.getBoolean("Knocked"))replies.add(Talk.reply("Who was at the door?",q->answer(q,h,"door")));
        Talk.ask(p,replies);
    }
    static void answer(ServerPlayer p,PlaceForYou.House h,String question){
        var own=PlaceForYou.own(p);var old=PlaceForYouTexts.detailLine(own.getString("Detail"),own.getCompound("Snap"));
        PlaceForYou.line(p,h,"Mara",old);
        PlaceForYou.edit(p,o->{o.putString("KeepAsked",question);o.putString("OldAnswer",old);o.putInt("KeepStage",3);});h.keepAt=(int)h.tick;
        Talk.end(p);
    }

    // ------------------------------------------------------------------------------------------------ talking
    static void talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=PlaceForYou.own(p);
        if(who.equals("Kit")){PlaceForYou.above(h,a,"I'm still going.");return;}
        if(who.equals("Mara")){PlaceForYou.above(h,a,own.getInt("KeepStage")==0?"Sit down. It's still warm.":"Eat something.");return;}
        PlaceForYou.above(h,a,own.getInt("KeepStage")==0?"Sit with us.":"Mm?");
    }
}
