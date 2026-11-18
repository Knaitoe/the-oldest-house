package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.692, A Place for You, slice 8 (docs/A_PLACE_FOR_YOU_0_4_692.md): the first two of the proposal's consequence branches.
 *
 * The letter. A reader who agreed to keep the back way quiet and then told Mara before the bag was out: by the time the bag is outside,
 * Mara has had Kit's letter off the clasp and put it in her top drawer. Kit, back from the gate, says so in the back passage. The reader
 * can go up and fetch it back, stand by having told her, or tell Kit to go without it; Kit trusts the reader after only what was done.
 *
 * The porch. A reader who told Mara openly, with Kit there: when Kit goes, Mara has the bag and carries it out onto the porch, with Kit
 * at the open gate and the road behind. What she does with it follows what the reader did with her earlier: having heard her out in her
 * room, or asked her what Kit wanted, she goes down the path and puts it in Kit's hands; having been promised company, she asks the reader
 * to make Kit wait till morning; otherwise she stands on the porch holding it, and says she can't go past it.
 *
 * Everything here is the reader's own copy and record.
 */
public final class PlaceForYouLetter {
    private PlaceForYouLetter(){}
    static CompoundTag own(ServerPlayer p){return PlaceForYou.own(p);}
    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}
    /** Mara on the porch with the bag, beside the door so the way down the path is clear; Kit at the open gate, the road behind. */
    static final PlaceForYou.Mark PORCH=PlaceForYou.stand(1.6,-9.5,20),GATE=PlaceForYou.stand(.5,-5.5,180);
    /** Her chest of drawers, upstairs. */
    static final BlockPos DRAWER=PlaceForYouSite.DRESSER;

    // ------------------------------------------------------------------------------------------------ where people stand
    static int stage(CompoundTag own){return own.getInt("LetterStage");}
    /** True while Kit has something to say about the letter, the reader is fetching it, or the porch is happening: supper waits. */
    public static boolean holding(CompoundTag own){int s=stage(own);return s>=1&&s<=3||own.getInt("PorchStage")==1;}
    /** Kit by the back door until the letter is settled; Mara and Kit at the front while the porch is happening. */
    static void marks(CompoundTag own,Map<String,PlaceForYou.Mark> m){
        int s=stage(own);if(s>=1&&s<=3&&!own.getBoolean("KitGoing"))m.put("Kit",PlaceForYouBack.WAITS);
        if(own.getInt("PorchStage")==1){m.put("Mara",PORCH);m.put("Kit",GATE);}
    }

    // ------------------------------------------------------------------------------------------------ the evening's clock
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        // The bag out after the quiet was broken: the letter has been taken off the clasp.
        if(stage(own)==0&&own.getInt("Chapter")==PlaceForYou.SINK&&own.getBoolean("BrokeQuiet")&&own.getInt("BackStage")>=3&&!own.getBoolean("KitGoing")){
            PlaceForYou.edit(p,o->{o.putInt("LetterStage",1);o.putString("LetterAt","drawer");});return;}
        if(stage(own)==1&&!Talk.waiting(p)){var kit=PlaceForYou.actor(l,h,"Kit");
            if(kit!=null&&kit.distanceToSqr(p)<16)gone(p,h);
            else if(kit!=null&&h.tick%300==100)PlaceForYou.above(h,kit,"Back here. Please.");}
        // The letter out of the drawer: her door shuts again once the reader is out of the room and not looking at it.
        int room=own.getInt("RoomStage");
        if(stage(own)>=3&&!(room>=1&&room<=3)&&PlaceForYouSite.maraDoorOpen(l,h.base)&&!PlaceForYouRoom.inRoom(r)&&!PlaceForYou.looking(p,h.base,PlaceForYouRoom.DOOR,24))PlaceForYouSite.maraDoor(l,h.base,false);
        // The porch begins once Mara is out on it and Kit is at the gate, with the reader near.
        if(own.getInt("PorchStage")==1&&!Talk.waiting(p)){var mara=PlaceForYou.actor(l,h,"Mara");var kit=PlaceForYou.actor(l,h,"Kit");
            if(mara!=null&&mara.distanceToSqr(p)<20&&at(h,mara,PORCH,1)&&(kit==null||at(h,kit,GATE,2.25)))porch(p,h);}
    }

    static boolean at(PlaceForYou.House h,LiteraryActor a,PlaceForYou.Mark m,double within){
        return a.position().subtract(new Vec3(h.base.getX()+m.at().x,h.base.getY(),h.base.getZ()+m.at().z)).horizontalDistanceSqr()<within;}

    // ------------------------------------------------------------------------------------------------ the letter
    static void gone(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"letter","Kit");
        PlaceForYou.line(p,h,"Kit","The letter's gone. I wrapped it round the clasp, and it isn't there.");
        PlaceForYou.line(p,h,"Kit","I heard her on the stairs while the bag was by the door.");
        PlaceForYou.line(p,h,"Kit","The address was on it. The room above the workshop.");
        Talk.ask(p,Talk.reply("I'll get it back.",q->{PlaceForYou.line(q,h,"Kit","Her top drawer. That's where she puts things.");
                PlaceForYouSite.maraDoor(q.serverLevel(),h.base,true);PlaceForYou.edit(q,o->o.putInt("LetterStage",2));Talk.end(q);cue(q,"Upstairs, Mara's door is open.");})
            ,Talk.reply("She needed to hear it from somebody.",q->{PlaceForYou.line(q,h,"Kit","Maybe she did. It was still mine to say.");
                PlaceForYou.line(q,h,"Kit","I'll ask at the workshop. They'll know my name or they won't.");settled(q,"defended");})
            ,Talk.reply("Go without it. Ask when you get there.",q->{PlaceForYou.line(q,h,"Kit","I was always going to have to ask.");settled(q,"without");}));
    }
    static void settled(ServerPlayer p,String how){PlaceForYou.edit(p,o->{o.putInt("LetterStage",4);o.putString("KitTrust",how);});Talk.end(p);}

    /** Her top drawer, while the reader is fetching the letter. */
    static boolean click(ServerPlayer p,BlockPos b,BlockPos rel){
        var h=PlaceForYou.house(b);if(h==null)return false;var own=own(p);int s=stage(own);
        if((rel.equals(PlaceForYouRoom.DOOR)||rel.equals(PlaceForYouRoom.DOOR.above()))&&(s==2||s==3)){cue(p,"Mara's room.");return true;}
        if(!rel.equals(DRAWER)||s!=2)return false;var l=p.serverLevel();
        l.playSound(null,b.offset(DRAWER),SoundEvents.CHISELED_BOOKSHELF_PICKUP,SoundSource.BLOCKS,.6F,.9F);
        PlaceForYou.take(p,PlaceForYouRegistry.LETTER,"Kit's letter");PlaceForYou.edit(p,o->{o.putInt("LetterStage",3);o.putString("LetterAt","reader");});
        cue(p,own.getBoolean("RoomTold")?"In the top drawer, under the work shirt, Kit's letter, folded in four. You shut the drawer."
            :"In the top drawer, under a folded shirt, a letter folded in four with Kit's name on it. You shut the drawer.");
        return true;
    }
    /** The letter put back in Kit's hand. */
    static void give(ServerPlayer p,PlaceForYou.House h){
        PlaceForYou.release(p,null);PlaceForYou.edit(p,o->o.putString("LetterAt","kit"));
        // Kit holds it open a moment, then puts it away.
        var kit=PlaceForYou.actor(p.serverLevel(),h,"Kit");if(kit!=null)kit.setItemInHand(InteractionHand.MAIN_HAND,PlaceForYou.carried(p,PlaceForYouRegistry.LETTER,"Kit's letter"));
        Talk.begin(p,"letter_back","Kit");
        PlaceForYou.line(p,h,"Kit","That's it. That's the one.");
        PlaceForYou.line(p,h,"Kit","I'll keep it on me this time.");
        Talk.ask(p,Talk.act("Go on.",q->{var k=PlaceForYou.actor(q.serverLevel(),h,"Kit");if(k!=null)k.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);settled(q,"returned");}));
    }

    // ------------------------------------------------------------------------------------------------ the porch
    /**
     * As Kit is about to go. A letter still unsettled stays where it is. A reader who told Mara openly gets the porch first: Mara has the
     * bag and carries it out. True while the porch is happening, so Kit does not go yet.
     */
    static boolean hold(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);var l=p.serverLevel();var b=h.base;
        int s=stage(own);if(s>=1&&s<=3)PlaceForYou.edit(p,o->{o.putInt("LetterStage",4);if(o.getString("KitTrust").isEmpty())o.putString("KitTrust","left");});
        int porch=own.getInt("PorchStage");
        if(porch==2)return false;
        if(porch==1)return true;
        if(!own.getBoolean("ToldOpen")||own.getString("Confront").equals("kit"))return false;
        PlaceForYouSite.bag(l,b,"none");PlaceForYouSite.frontDoor(l,b,true);PlaceForYouSite.gate(l,b,true);
        var mara=PlaceForYou.actor(l,h,"Mara");if(mara!=null)mara.setItemInHand(InteractionHand.MAIN_HAND,PlaceForYou.carried(p,PlaceForYouRegistry.BAG,"Kit's bag"));
        PlaceForYou.edit(p,o->o.putInt("PorchStage",1));
        cue(p,"Mara has Kit's bag. She takes it out onto the porch.");
        return true;
    }
    /** What Mara does with the bag, from what the reader did with her earlier. */
    static String stance(CompoundTag own){
        if(own.getBoolean("RoomTold")||own.getString("Promise").equals("kit"))return "aside";
        if(own.getString("Promise").equals("company"))return "keep";
        return "threshold";
    }
    static void porch(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);String stance=stance(own);PlaceForYou.edit(p,o->o.putString("BagPorch",stance));
        Talk.begin(p,"porch","Mara and Kit");
        PlaceForYou.line(p,h,"Mara","I brought it round from the back. It was going to get damp.");
        PlaceForYou.line(p,h,"Kit","I'll take it from here.");
        switch(stance){
            case "aside"->{PlaceForYou.line(p,h,"Mara","Here.");Talk.line(p,"","Mara goes down the path and puts the bag in Kit's hands.");
                PlaceForYou.line(p,h,"Mara","Go on, then. Before I'm sensible.");
                Talk.ask(p,Talk.reply("Thank you, Mara.",q->{PlaceForYou.line(q,h,"Mara","Don't. Go on.");porchDone(q,h,"kit");})
                    ,Talk.reply("Kit, write to her.",q->{PlaceForYou.line(q,h,"Kit","I'll try.");porchDone(q,h,"kit");})
                    ,Talk.act("Say nothing.",q->porchDone(q,h,"kit")));}
            case "keep"->{PlaceForYou.line(p,h,"Mara","Tell Kit to wait till morning. Kit will listen to you.");
                Talk.ask(p,Talk.reply("Kit, wait till morning.",q->{PlaceForYou.line(q,h,"Kit","No.");PlaceForYou.line(q,h,"Kit","Keep it, then.");porchDone(q,h,"left");})
                    ,Talk.reply("Give Kit the bag, Mara.",q->{PlaceForYou.line(q,h,"Mara","You said you'd sit with me.");PlaceForYou.line(q,h,"Mara","...Here.");
                        Talk.line(q,"","Mara holds the bag out, and Kit comes up the path and takes it.");porchDone(q,h,"kit");}));}
            default->{PlaceForYou.line(p,h,"Mara","I can't go past the porch. I've tried.");
                Talk.ask(p,Talk.reply("Kit, come and take it.",q->{Talk.line(q,"","Kit comes up the path and takes the bag. Mara lets go of it.");porchDone(q,h,"kit");})
                    ,Talk.act("Carry the bag down to Kit.",q->{Talk.line(q,"","You take the bag from Mara and carry it down the path to Kit.");porchDone(q,h,"carried");}));}
        }
    }
    static void porchDone(ServerPlayer p,PlaceForYou.House h,String bag){
        var l=p.serverLevel();Talk.end(p);
        var mara=PlaceForYou.actor(l,h,"Mara");if(mara!=null)mara.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        // Kept back: Mara takes it in and stands it by Kit's chair again.
        if(bag.equals("left"))PlaceForYouSite.bag(l,h.base,"kitchen");
        else{var kit=PlaceForYou.actor(l,h,"Kit");if(kit!=null)kit.setItemInHand(InteractionHand.MAIN_HAND,PlaceForYou.carried(p,PlaceForYouRegistry.BAG,"Kit's bag"));}
        PlaceForYou.edit(p,o->{o.putInt("PorchStage",2);o.putString("PorchBag",bag);});
        PlaceForYou.kitGoes(p,h);
    }
    /** What Kit says going, after the letter and the porch. */
    static String parting(CompoundTag own){
        if(own.getString("PorchBag").equals("left"))return "I'll manage.";
        if(own.getString("KitTrust").equals("returned"))return "I'll write. I've got the address on me now.";
        return "I'll write. I probably won't. But I'll mean to.";
    }

    // ------------------------------------------------------------------------------------------------ the account
    /** After the back passage: what became of the letter. */
    public static String letterAccount(CompoundTag own){
        return switch(own.getString("KitTrust")){
            case "returned"->"When the bag was out, Kit's letter was gone from the clasp. Mara had put it in her top drawer. I went up and got it back, and gave it to Kit.";
            case "defended"->"When the bag was out, Kit's letter was gone from the clasp; Mara had taken it upstairs. I said she had needed to hear it from somebody. Kit said it was still theirs to say.";
            case "without"->"When the bag was out, Kit's letter was gone from the clasp; Mara had taken it upstairs. I told Kit to go without it and ask when they got there.";
            case "left"->"When the bag was out, Kit's letter was gone from the clasp. Mara had taken it upstairs, and it was still in her drawer when Kit went.";
            default->"";
        };
    }
    /** Kit going, from the porch, in place of the plain sentence. */
    public static String porchAccount(CompoundTag own){
        var t=new StringBuilder("When Kit went, Mara carried the bag out onto the porch, and Kit waited at the open gate.");
        String stance=own.getString("BagPorch"),bag=own.getString("PorchBag");
        switch(stance){
            case "aside"->t.append(" She went down the path and put it in Kit's hands.");
            case "keep"->t.append(bag.equals("left")?" She asked me to tell Kit to wait till morning, and I did. Kit said no, and went without the bag."
                :" She asked me to tell Kit to wait till morning. I told her to give Kit the bag, and she did.");
            default->t.append(bag.equals("carried")?" She said she couldn't go past the porch. I carried the bag down the path to Kit."
                :" She said she couldn't go past the porch. Kit came up the path and took it from her.");
        }
        return t.toString();
    }

    // ------------------------------------------------------------------------------------------------ talking
    /** Spoken to about the letter or on the porch. */
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=own(p);int s=stage(own);
        if(who.equals("Kit")&&s==1&&!Talk.waiting(p)){gone(p,h);return true;}
        if(who.equals("Kit")&&s==2){PlaceForYou.above(h,a,"Her top drawer.");return true;}
        if(own.getInt("PorchStage")==1&&(who.equals("Mara")||who.equals("Kit"))&&!Talk.waiting(p)){porch(p,h);return true;}
        return false;
    }
}
