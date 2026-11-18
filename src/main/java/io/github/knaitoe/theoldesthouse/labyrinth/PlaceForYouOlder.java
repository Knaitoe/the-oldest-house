package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.691, A Place for You, slice 7 (docs/A_PLACE_FOR_YOU_0_4_691.md): the older pictures (the proposal's N6C, a familiar stranger and
 * what was actually shared). Alex or Ren, whichever the reader actually met, takes them up to the landing, where the household's old
 * photographs hang and the reader's own picture has been copied in among them. The guide tells the house's claim (the lake, the summer
 * they went every weekend) and then the part that happened: the eaves, the lantern, or only tonight. Taking the picture down makes the
 * claim falter in the middle of a sentence, and the guide starts a new one; hung back, the claim comes back word for word. The back
 * room's box takes the picture for good. Everything here is the reader's own copy and record.
 */
public final class PlaceForYouOlder {
    private PlaceForYouOlder(){}
    static final int UP=PlaceForYouSite.UP;
    /** The back room on the landing's west side, where the box is, opened once the picture has come down. */
    static final BlockPos STORE_DOOR=PlaceForYouSite.SHUT_DOORS.get(3);
    static final String[] CLAIMS={"You always had the end of the dock.","The summer at the lake. You remember.","You used to sit with your feet in the water."};

    static CompoundTag own(ServerPlayer p){return PlaceForYou.own(p);}
    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}
    static boolean live(CompoundTag own){return own.contains("Snap")&&!own.getBoolean("Completed");}

    /** Who takes the reader up: the one actually met under the eaves; else the first of the two the reader talked to; else Alex. */
    public static String guide(CompoundTag own){
        var snap=own.getCompound("Snap");String stood=snap.getString("Stood");boolean read=snap.getBoolean("Read");
        if(read&&(stood.equals("man")||stood.equals("middle")))return "Alex";
        if(read&&stood.equals("woman"))return "Ren";
        return own.getString("First").equals("Ren")?"Ren":"Alex";
    }
    /** What actually happened between the reader and the guide: the eaves, the lantern, or tonight. */
    public static String shared(CompoundTag own){
        var snap=own.getCompound("Snap");String stood=snap.getString("Stood");if(!snap.getBoolean("Read"))return "tonight";
        if(stood.equals("lantern"))return "lantern";
        boolean alex=guide(own).equals("Alex");
        if(stood.equals("middle")||alex&&stood.equals("man")||!alex&&stood.equals("woman"))return "eaves";
        return "tonight";
    }
    /** Where the guide stands: on the landing while the pictures are being shown, at the top of the row. */
    static PlaceForYou.Mark mark(CompoundTag own){
        int stage=own.getInt("OlderStage");if(stage<1||stage>3)return null;
        return new PlaceForYou.Mark(new Vec3(-1.5,UP,-22.6),20,true);
    }
    static boolean onLanding(Vec3 r){return r.x<-.3&&r.x>-3&&r.z>-22.9&&r.z<-15.6&&r.y>=UP-.5&&r.y<UP+3;}

    // ------------------------------------------------------------------------------------------------ at the sink
    /** At the sink, the guide (and only the guide) asks the reader up; Mara and the back passage come first. */
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=own(p);if(!live(own)||!who.equals(guide(own)))return false;int stage=own.getInt("OlderStage");
        if(stage==0&&!own.getBoolean("OlderDeclined")&&own.getInt("Chapter")==PlaceForYou.SINK){ask(p,h,who);return true;}
        if(stage==1){PlaceForYou.above(h,a,"Up here.");return true;}
        if(stage>=3&&own.getString("Portrait").equals("hung")){PlaceForYou.above(h,a,CLAIMS[(int)(h.tick/40%CLAIMS.length)]);return true;}
        return false;
    }
    static void ask(ServerPlayer p,PlaceForYou.House h,String who){
        Talk.begin(p,"older_ask",who);
        PlaceForYou.line(p,h,who,"Come and look at something with me? It's on the landing.");
        Talk.ask(p,Talk.reply("Show me.",q->go(q,h,who)),Talk.reply("Not now.",q->{PlaceForYou.line(q,h,who,"It'll keep. Everything here does.");
            PlaceForYou.edit(q,o->o.putBoolean("OlderDeclined",true));Talk.end(q);}));
    }
    static void go(ServerPlayer p,PlaceForYou.House h,String who){
        Talk.end(p);var l=p.serverLevel();var b=h.base;var own=own(p);
        PlaceForYouSite.portrait(l,b,own.getCompound("Snap").getCompound("Drawing"),true);
        PlaceForYou.edit(p,o->{o.putInt("OlderStage",1);o.putString("OlderGuide",who);o.putString("Portrait","hung");});
        PlaceForYou.line(p,h,who,"It's at the top of the stairs.");
        cue(p,who+" goes up ahead of you.");
    }

    // ------------------------------------------------------------------------------------------------ on the landing
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        int stage=own.getInt("OlderStage");String who=own.getString("OlderGuide");
        if(stage==1&&onLanding(r)&&!Talk.waiting(p)){own.putInt("OlderStage",2);PlaceForYou.save(p,own);tour(p,h,who,own);return;}
        // The claim, now and then, while the picture hangs and the reader is near it.
        if(stage==3&&own.getString("Portrait").equals("hung")&&h.tick%200==0&&!Talk.waiting(p)&&onLanding(r)){
            var a=PlaceForYou.actor(l,h,who);if(a!=null&&a.distanceToSqr(p)<64)PlaceForYou.above(h,a,CLAIMS[(int)(h.tick/200%CLAIMS.length)]);
        }
    }
    static void tour(ServerPlayer p,PlaceForYou.House h,String who,CompoundTag own){
        Talk.begin(p,"older",who);
        PlaceForYou.line(p,h,who,"That's Mara's mother at the lake. She went every summer, Mara says, and sat with her shoes on.");
        PlaceForYou.line(p,h,who,"That's the house before the porch was closed in. Nobody knows who those people are. Mara says they came with it.");
        PlaceForYou.line(p,h,who,"Mara and Theo on the hill behind the school. The winter the creek froze.");
        Talk.line(p,"","Your picture hangs between theirs, in a frame like theirs.");
        PlaceForYou.line(p,h,who,"And that's us. At the lake, the summer we went every weekend. You had the end of the dock and wouldn't give it up.");
        PlaceForYou.line(p,h,who,switch(shared(own)){
            case "eaves"->"That isn't right. We stood under the eaves in the rain, you and me. That happened. I remember the gutter running over.";
            case "lantern"->"That isn't right. I watched you walk out to the lantern in the rain. That happened.";
            default->"That isn't right. I met you tonight, at the table. That's the part that happened.";});
        PlaceForYou.line(p,h,who,shared(own).equals("tonight")?"I liked sitting across from you. That part happened. I don't know where she got the rest."
            :"I liked being there with you. That part happened. I don't know where she got the rest.");
        Talk.ask(p,Talk.reply("Then let's have something of our own.",q->said(q,h,who,"close","All right. Something of our own. Starting tonight."))
            ,Talk.reply("I'd like to be your friend. That's all.",q->said(q,h,who,"friend","That's plenty, in here."))
            ,Talk.reply("None of that happened. She put me there.",q->said(q,h,who,"challenge","I know. I can hear her doing it while I talk. I'm sorry."))
            ,Talk.reply("I need some room from all this.",q->said(q,h,who,"space","Then take it. I'll go down first. I won't ask you up again."))
            ,Talk.reply("What do you want tomorrow?",q->said(q,h,who,"tomorrow",who.equals("Alex")?"To wake up somewhere I didn't fall asleep.":"To walk out the front door and have it be a street.")));
    }
    static void said(ServerPlayer p,PlaceForYou.House h,String who,String said,String answer){
        PlaceForYou.line(p,h,who,answer);h.sinkAt=(int)h.tick;
        PlaceForYou.edit(p,o->{o.putInt("OlderStage",3);o.putString("OlderSaid",said);});
        Talk.end(p);cue(p,"Your picture is still up between theirs.");
    }

    // ------------------------------------------------------------------------------------------------ the picture
    /** A hand on the copied picture, its nail, the box in the back room, the older pictures or the back room's door. */
    static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,int held){
        var h=PlaceForYou.house(b);var own=own(p);if(h==null||!live(own))return false;int stage=own.getInt("OlderStage");var l=p.serverLevel();
        if(rel.equals(PlaceForYouSite.OLDER_A)){cue(p,"A woman at a lake, sitting with her shoes on.");return true;}
        if(rel.equals(PlaceForYouSite.OLDER_B)){cue(p,"A family on the porch before it was closed in. Nobody you know.");return true;}
        if(rel.equals(PlaceForYouSite.OLDER_C)){cue(p,"Two children on a sled at the top of a hill.");return true;}
        if((rel.equals(STORE_DOOR)||rel.equals(STORE_DOOR.above()))&&stage>=4){cue(p,"The back room.");return true;}
        if(rel.equals(PlaceForYouSite.PORTRAIT)){
            if(stage<2){cue(p,"Your picture, in a frame like theirs.");return true;}
            if(stage==2){cue(p,"Not while "+own.getString("OlderGuide")+" is showing you.");return true;}
            String now=own.getString("Portrait");
            if(now.equals("hung")||now.equals("rehung")){down(p,h,own);return true;}
            if(held==PlaceForYouRegistry.PORTRAIT){up(p,h);return true;}
            cue(p,"A bare nail where your picture was.");return true;
        }
        if(rel.equals(PlaceForYouSite.PHOTO_BOX)){
            if(held==PlaceForYouRegistry.PORTRAIT&&stage>=4){box(p,h);return true;}
            cue(p,PlaceForYouBlock.stage(l.getBlockState(b.offset(PlaceForYouSite.PHOTO_BOX)))==1?"Your picture on top of the others.":"A box of photographs nobody has hung.");return true;
        }
        return false;
    }
    /** The picture comes down: the guide loses the sentence partway through and starts another. */
    static void down(ServerPlayer p,PlaceForYou.House h,CompoundTag own){
        var l=p.serverLevel();String who=own.getString("OlderGuide");boolean again=own.getBoolean("Faltered");
        PlaceForYouSite.portrait(l,h.base,own.getCompound("Snap").getCompound("Drawing"),false);
        l.playSound(null,h.base.offset(PlaceForYouSite.PORTRAIT),SoundEvents.PAINTING_BREAK,SoundSource.BLOCKS,.5F,1.3F);
        PlaceForYou.take(p,PlaceForYouRegistry.PORTRAIT,"Your picture");
        boolean opened=own.getInt("OlderStage")<4;
        PlaceForYou.edit(p,o->{o.putInt("OlderStage",4);o.putString("Portrait","down");o.putBoolean("Faltered",true);});
        if(opened)PlaceForYouSite.storeDoor(l,h.base,true);
        Talk.begin(p,"older_falter",who);
        if(!again){
            PlaceForYou.line(p,h,who,"You always had the end of the dock, with your feet in the—");
            PlaceForYou.line(p,h,who,"...");
            PlaceForYou.line(p,h,who,"I had something there. It's gone.");
            PlaceForYou.line(p,h,who,own.getString("OlderSaid").equals("space")?"I don't know where you like to sit. You can tell me or not."
                :"I don't know where you like to sit. I'd like to find out.");
            PlaceForYou.line(p,h,who,"There's a box in the back room. The ones nobody hangs go in there.");
        }else{
            PlaceForYou.line(p,h,who,"The summer at the—");
            PlaceForYou.line(p,h,who,"No. Gone again.");
        }
        Talk.ask(p,Talk.act("Go on.",Talk::end));
    }
    /** Hung back on its nail: the claim comes back as it was. */
    static void up(ServerPlayer p,PlaceForYou.House h){
        var l=p.serverLevel();var own=own(p);String who=own.getString("OlderGuide");PlaceForYou.release(p,null);
        PlaceForYouSite.portrait(l,h.base,own.getCompound("Snap").getCompound("Drawing"),true);
        l.playSound(null,h.base.offset(PlaceForYouSite.PORTRAIT),SoundEvents.PAINTING_PLACE,SoundSource.BLOCKS,.5F,1.1F);
        PlaceForYou.edit(p,o->{o.putInt("OlderStage",3);o.putString("Portrait","rehung");o.putInt("Rehung",o.getInt("Rehung")+1);});
        cue(p,"Back on its nail.");
        var a=PlaceForYou.actor(l,h,who);PlaceForYou.above(h,a,"You always had the end of the dock, with your feet in the water.");
    }
    /** Laid in the box in the back room: the claim stays gone. */
    static void box(ServerPlayer p,PlaceForYou.House h){
        var l=p.serverLevel();PlaceForYou.release(p,null);PlaceForYouSite.photoBox(l,h.base,true);
        l.playSound(null,h.base.offset(PlaceForYouSite.PHOTO_BOX),SoundEvents.BUNDLE_INSERT,SoundSource.BLOCKS,.6F,.9F);
        PlaceForYou.edit(p,o->{o.putInt("OlderStage",5);o.putString("Portrait","boxed");});
        cue(p,"You lay your picture in the box with the others nobody hangs.");
    }
    /** The back room's door shuts again once the box has the picture and nobody is looking at the door. */
    static void after(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        if(own.getInt("OlderStage")==5&&PlaceForYouSite.storeDoorOpen(l,h.base)&&r.x>-2.9&&!PlaceForYou.looking(p,h.base,STORE_DOOR,24))PlaceForYouSite.storeDoor(l,h.base,false);
    }

    // ------------------------------------------------------------------------------------------------ the account
    public static String account(CompoundTag own){
        String who=own.getString("OlderGuide");var t=new StringBuilder();
        t.append(who).append(" took me up to the pictures on the landing: Mara's mother at the lake, the house before the porch was closed in, Mara and Theo on a sled. Mine was up there with them, in a frame like theirs. ");
        t.append(who).append(" said it was the two of us at the lake, the summer we went every weekend, and then said that wasn't right. ");
        t.append(switch(shared(own)){case "eaves"->"What happened was the two of us under the eaves, in the rain.";case "lantern"->"What happened was "+who+" watching me walk out to the lantern.";default->"What happened was supper, that night.";});
        t.append(switch(own.getString("OlderSaid")){
            case "close"->" I said we should have something of our own.";
            case "friend"->" I said I'd like to be friends, and that was all.";
            case "challenge"->" I said none of it had happened, and that Mara had put me there.";
            case "space"->" I said I needed some room from all of it.";
            case "tomorrow"->who.equals("Alex")?" I asked what Alex wanted tomorrow. To wake up somewhere he hadn't fallen asleep.":" I asked what Ren wanted tomorrow. To walk out the front door and have it be a street.";
            default->"";});
        switch(own.getString("Portrait")){
            case "boxed"->t.append(" When I took my picture down, ").append(who).append(" lost a sentence halfway through and started a new one. I put the picture in the box in the back room, with the ones nobody hangs.");
            case "rehung"->t.append(" I took my picture down and put it back up. ").append(who).append(" lost the lake and found it again.");
            case "down"->t.append(" When I took my picture down, ").append(who).append(" lost a sentence halfway through and started a new one.");
            default->t.append(" I left my picture where it was.");
        }
        return t.toString();
    }
}
