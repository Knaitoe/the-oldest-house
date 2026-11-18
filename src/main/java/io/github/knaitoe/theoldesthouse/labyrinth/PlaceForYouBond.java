package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.695, A Place for You, slice 11 (docs/A_PLACE_FOR_YOU_0_4_695.md): friendship or romance with the guide (Alex or Ren), settled
 * between the two of them and never inferred.
 *
 * On the landing, a reader who says "Then let's have something of our own." is asked what kind, or, where the House's read points at
 * the guide and the two of them really stood under the eaves together, is offered more than friendship first. Either can say no, and
 * "I didn't mean it that way" is a friendship, not a refusal. A guide who doesn't know the reader well enough says so, and can be asked
 * again later in the evening. Whatever was settled, it doesn't decide where the guide goes at the end, and the guide says so.
 *
 * Everything here is the reader's own copy and record.
 */
public final class PlaceForYouBond {
    private PlaceForYouBond(){}
    static final String END="What we said on the landing doesn't decide where I go.";

    static CompoundTag own(ServerPlayer p){return PlaceForYou.own(p);}
    /** The guide offers first: the read points at them (MEN and Alex, WOMEN and Ren, or BOTH), and they stood under the eaves together. */
    public static boolean offers(CompoundTag own){
        String drawn=own.getCompound("Snap").getString("Drawn");String who=PlaceForYouOlder.guide(own);
        boolean toward=drawn.equals("BOTH")||drawn.equals("MEN")&&who.equals("Alex")||drawn.equals("WOMEN")&&who.equals("Ren");
        return toward&&PlaceForYouOlder.shared(own).equals("eaves");
    }

    // ------------------------------------------------------------------------------------------------ on the landing
    /** After "Then let's have something of our own.": what kind, settled between them. */
    static void ask(ServerPlayer p,PlaceForYou.House h,String who){
        if(offers(own(p))){
            PlaceForYou.line(p,h,who,"I'd like it to be more than friends. If you would.");
            PlaceForYou.edit(p,o->o.putBoolean("BondOffered",true));
            Talk.ask(p,Talk.reply("I'd like that too.",q->{PlaceForYou.line(q,h,who,"Then that's ours.");bond(q,"romance");})
                ,Talk.reply("I didn't mean it that way.",q->{PlaceForYou.line(q,h,who,"Then friends. That's no smaller.");bond(q,"friend");})
                ,Talk.reply("I don't know yet.",q->{PlaceForYou.line(q,h,who,"Then we don't have to know tonight.");bond(q,"unsure");}));
            return;
        }
        PlaceForYou.line(p,h,who,"As friends, or more than that?");
        Talk.ask(p,Talk.reply("As friends.",q->{PlaceForYou.line(q,h,who,"Friends, then.");bond(q,"friend");})
            ,Talk.reply("More than that, if you want it too.",q->{
                if(PlaceForYouOlder.shared(own(q)).equals("eaves")){PlaceForYou.line(q,h,who,"I do.");bond(q,"romance");}
                else{PlaceForYou.line(q,h,who,"I don't know you well enough to say yes to that yet.");PlaceForYou.line(q,h,who,"Ask me again before the night's out.");bond(q,"asked");}})
            ,Talk.reply("I don't know yet.",q->{PlaceForYou.line(q,h,who,"Then we don't have to know tonight.");bond(q,"unsure");}));
    }
    static void bond(ServerPlayer p,String how){
        PlaceForYou.edit(p,o->o.putString("Bond",how));Talk.end(p);PlaceForYou.cue(p,"Your picture is still up between theirs.");
    }

    // ------------------------------------------------------------------------------------------------ the evening's clock
    /** Whatever the two of them settled, it doesn't decide where the guide goes; once they have said where, they say so. */
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        if(!own.getString("Bond").equals("romance")||own.getBoolean("BondEndSaid"))return;
        String who=own.getString("OlderGuide");if(own.getString(who+"End").isEmpty())return;
        var a=PlaceForYou.actor(l,h,who);if(a==null||p.distanceToSqr(a)>100||Talk.waiting(p))return;
        PlaceForYou.above(h,a,END);PlaceForYou.edit(p,o->o.putBoolean("BondEndSaid",true));
    }

    // ------------------------------------------------------------------------------------------------ talking
    /**
     * Asked again, later in the evening (at dinner, or in the open house before the guide has said where they are going), by a reader
     * the guide didn't know well enough on the landing. The guide raises it once; "Not now" leaves it there.
     */
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=own(p);
        if(!own.getString("Bond").equals("asked")||own.getBoolean("AskedAgain")||!who.equals(own.getString("OlderGuide"))||Talk.waiting(p))return false;
        int ch=own.getInt("Chapter");if(ch!=PlaceForYou.DINNER&&!(ch==PlaceForYou.OPEN&&own.getString(who+"End").isEmpty()))return false;
        PlaceForYou.edit(p,o->o.putBoolean("AskedAgain",true));
        Talk.begin(p,"bond_again",who);
        PlaceForYou.line(p,h,who,"You were going to ask me something again.");
        Talk.ask(p,Talk.reply("I'm asking again.",q->{PlaceForYou.line(q,h,who,"All right. Yes.");PlaceForYou.line(q,h,who,"Ask me tomorrow what that means. I'd like there to be a tomorrow to ask in.");bondLater(q,"romance");})
            ,Talk.reply("Friends is enough.",q->{PlaceForYou.line(q,h,who,"Friends, then.");bondLater(q,"friend");})
            ,Talk.reply("Not now.",q->{PlaceForYou.line(q,h,who,"All right.");Talk.end(q);}));
        return true;
    }
    static void bondLater(ServerPlayer p,String how){PlaceForYou.edit(p,o->o.putString("Bond",how));Talk.end(p);}

    // ------------------------------------------------------------------------------------------------ the account
    /** After the landing: what kind of something, and how it was settled. */
    public static String account(CompoundTag own){
        String how=own.getString("Bond");if(how.isEmpty())return "";
        String who=own.getString("OlderGuide");String pron=PlaceForYouTexts.pron(who);
        var t=new StringBuilder();
        if(own.getBoolean("BondOffered")){
            t.append(who).append(" said, \"I'd like it to be more than friends. If you would.\"");
            switch(how){
                case "romance"->t.append(" I said I would.");
                case "friend"->t.append(" I said I hadn't meant it that way. Friends, then, ").append(who).append(" said, and that was no smaller.");
                default->t.append(" I said I didn't know yet. We didn't have to know tonight, ").append(who).append(" said.");
            }
        }else{
            t.append(who).append(" asked if I meant as friends, or more than that.");
            boolean again=own.getBoolean("AskedAgain");
            if(again||how.equals("asked")){
                t.append(" I said more than that, if ").append(who).append(" wanted it too. ").append(who).append(" didn't know me well enough to say yes to that yet, and said to ask again before the night was out.");
                t.append(!again?" I didn't.":switch(how){case "romance"->" Later I asked again, and "+who+" said yes.";case "friend"->" Later I said friends was enough.";default->" Later "+who+" reminded me, and I said not now.";});
            }else switch(how){
                case "friend"->t.append(" I said as friends.");
                case "romance"->t.append(" I said more than that, if ").append(who).append(" wanted it too, and ").append(pron).append(" did.");
                default->t.append(" I said I didn't know yet.");
            }
        }
        if(own.getBoolean("BondEndSaid"))t.append(" At the end ").append(who).append(" said that what we had said on the landing didn't decide where ").append(pron).append(" went.");
        return t.toString();
    }
}
