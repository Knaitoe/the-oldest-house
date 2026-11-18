package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.696, A Place for You, slice 12 (docs/A_PLACE_FOR_YOU_0_4_696.md): the aftermath. A reader who comes back after the account is
 * read finds the house as their ending left it, and nothing in it can be decided again.
 *
 * The house opened (E1): the front door stands propped; whoever stayed is at the table; Kit and Alex are gone, and Mara and Ren too if
 * they went to the gate, and then the kitchen light is off. The place kept (E2): the evening laid again, each of them saying word for
 * word what they said at that table. The past let in (E3): Theo at the table, and nobody knows the reader. The road (E4): the
 * household going on without the reader, the reader's chair gone from the table, Mara at the sink with her back to the door.
 *
 * Each of them says one thing a visit. No source, no Witness, no keepsake: nothing in the record changes but the visit count and who
 * has spoken this visit.
 */
public final class PlaceForYouAfter {
    private PlaceForYouAfter(){}

    /** Whether this is a visit after the account was read. */
    public static boolean after(CompoundTag own){return own.getBoolean("Completed")&&own.getInt("AfterVisits")>0;}
    static String outcome(CompoundTag own){return own.getString("Outcome");}
    /** In the opened house, who went: Kit and Alex always; Mara and Ren when they went to the gate. */
    static boolean gone(CompoundTag own,String who){
        return switch(who){
            case "Kit","Alex"->true;
            case "Mara"->own.getString("MaraEnd").equals("gate");
            case "Ren"->own.getString("RenEnd").equals("goes");
            default->false;
        };
    }
    static boolean everyoneWent(CompoundTag own){return gone(own,"Mara")&&gone(own,"Ren");}

    // ------------------------------------------------------------------------------------------------ arriving again
    /** A visit after the account: counted, who has spoken cleared, and the house as it was left named once. The caller saves the record. */
    public static void arrive(ServerPlayer p,CompoundTag own){
        own.putInt("AfterVisits",own.getInt("AfterVisits")+1);own.remove("AfterSaid");
        PlaceForYou.cue(p,cue(own));
    }
    public static String cue(CompoundTag own){
        return switch(outcome(own)){
            case "opened_the_house"->everyoneWent(own)?"The front door stands open. The kitchen light is off.":"The front door stands open, the hall chair against it.";
            case "kept_a_place"->"Your place is laid. It was laid before you reached the door.";
            case "invited_the_past"->"Keys under the hall glass. Two voices from the kitchen.";
            default->PlaceForYouTexts.AFTER;
        };
    }

    // ------------------------------------------------------------------------------------------------ where they are
    /** Last in the chain: who is in the house on a visit after the account. */
    static void marks(CompoundTag own,Map<String,PlaceForYou.Mark> m){
        if(!after(own))return;
        switch(outcome(own)){
            case "opened_the_house"->{for(var who:new String[]{"Kit","Mara","Ren","Alex"})if(gone(own,who))m.put(who,PlaceForYou.Mark.gone());
                if(!gone(own,"Mara"))m.put("Mara",PlaceForYou.seat(PlaceForYouSite.MARA_CHAIR));if(!gone(own,"Ren"))m.put("Ren",PlaceForYou.seat(PlaceForYouSite.REN_CHAIR));}
            case "took_the_road"->{if(own.getBoolean("KitLeft"))m.put("Kit",PlaceForYou.Mark.gone());m.put("Mara",PlaceForYou.stand(5.5,-20.5,180));}
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ the house
    /** The house kept as it was left, changed only where the reader isn't looking. */
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        if(!after(own))return;var b=h.base;
        switch(outcome(own)){
            // Shut by a hand on the way out last time: propped again, unseen, with nobody standing in the way.
            case "opened_the_house"->{if(!PlaceForYouSite.doorOpen(l,b)&&r.distanceToSqr(1.5,0,-12.5)>9&&!PlaceForYou.looking(p,b,PlaceForYouSite.FRONT_DOOR,24))PlaceForYouSite.propped(l,b,true);}
            case "took_the_road"->{
                if(PlaceForYouSite.readerChair(l,b)&&r.distanceToSqr(7.5,0,-15.5)>4&&!PlaceForYou.looking(p,b,PlaceForYouSite.READER_CHAIR,14))PlaceForYouSite.readerChair(l,b,false);
                if(PlaceForYouSite.doorOpen(l,b)&&!PlaceForYouSite.inHouse(r)&&r.distanceToSqr(.5,0,-11.5)>4&&!PlaceForYou.looking(p,b,PlaceForYouSite.FRONT_DOOR,24))PlaceForYouSite.frontDoor(l,b,false);
            }
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ one thing each
    /** Talking on a visit after the account: one thing a visit from each of them, then nothing more until next time. */
    public static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=PlaceForYou.own(p);if(!after(own))return false;
        String said=own.getString("AfterSaid");String line=line(own,who);
        if(line==null||(","+said+",").contains(","+who+",")){PlaceForYou.above(h,a,"...");return true;}
        PlaceForYou.edit(p,o->o.putString("AfterSaid",said.isEmpty()?who:said+","+who));
        if(line.startsWith("*")){PlaceForYou.cue(p,line.substring(1));return true;}
        PlaceForYou.above(h,a,line);
        return true;
    }
    /** What each of them says on a visit after the account; a leading star is a cue, not speech. */
    public static String line(CompoundTag own,String who){
        return switch(outcome(own)){
            case "opened_the_house"->gone(own,who)?null:switch(who){
                case "Mara"->"Sit anywhere you like. I mean it now.";
                case "Ren"->"She sleeps most nights now. I leave the radio on low.";
                default->null;};
            // Word for word what each of them said at the kept table.
            case "kept_a_place"->switch(who){
                case "Mara"->"Sit down. It's still warm.";
                case "Ren"->"Told you she'd make too much.";
                case "Alex"->"Drink that before she fills it again.";
                case "Kit"->"I'm still going.";
                default->null;};
            case "invited_the_past"->switch(who){
                case "Mara"->"Sorry. You've been here before, haven't you? I'm no good with faces.";
                case "Theo"->"Evening. You'll want the gate. It sticks.";
                default->null;};
            default->switch(who){
                case "Mara"->"*Mara doesn't turn round from the sink.";
                case "Ren"->"She laid a place anyway. Then she put it away.";
                case "Alex"->"You took the road. Good. One of us should have.";
                case "Kit"->"You went before I did.";
                default->null;};
        };
    }
}
