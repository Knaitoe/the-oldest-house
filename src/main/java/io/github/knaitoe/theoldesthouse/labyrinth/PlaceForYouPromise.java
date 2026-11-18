package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.693, A Place for You, slice 9 (docs/A_PLACE_FOR_YOU_0_4_693.md): two more of the proposal's consequence branches.
 *
 * The broken promise. A reader who told Mara "I'll sit with you tonight" and then went to work at the back door with Kit: once they
 * have been gone from the kitchen a while, Mara comes out into the hall and finds them at the back of it. When the bag is out
 * and the reader is back in the kitchen, she says the promise back to them word for word. The reader can say it was only for tonight
 * and tonight isn't over, apologise, or say getting Kit out mattered more. Whatever they say, the break stays in the account; it changes
 * how she asks, or whether she asks, when she decides.
 *
 * The confession used. At the table, a reader who has heard about Theo can tell Mara to tell the others, or they will, and then tell them.
 * Ren says she told the reader that on her own, and it didn't have to be done like that. Mara lets everyone go, and asks the reader not
 * to sit with her after.
 *
 * Everything here is the reader's own copy and record.
 */
public final class PlaceForYouPromise {
    private PlaceForYouPromise(){}
    static CompoundTag own(ServerPlayer p){return PlaceForYou.own(p);}
    /** Ticks the reader is away at the back before Mara comes looking; ticks she stands in the doorway. */
    public static final int AWAY=160,LOOKS=100;
    /** Mara out in the hall, below the foot of the stair, looking down it to the back door. Not in the kitchen archway: from the back
     *  door the archway is behind the wall and the stair (seen in the 0.4.693 frames), and the reader would never see her there. */
    static final PlaceForYou.Mark HALL=PlaceForYou.stand(0,-15.6,180);
    private static final Map<UUID,Integer> AWAY_FOR=new HashMap<>();
    static void clear(){AWAY_FOR.clear();}

    static boolean promised(CompoundTag own){return own.getString("Promise").equals("company");}
    /** The reader working the back way with Kit: shown it, not refusing, the bag not yet out. */
    static boolean atTheBack(CompoundTag own){int s=own.getInt("BackStage");return s>=1&&s<3&&!own.getString("Quiet").equals("refused");}

    // ------------------------------------------------------------------------------------------------ where people stand
    static void marks(CompoundTag own,Map<String,PlaceForYou.Mark> m){if(own.getInt("MissedStage")==1)m.put("Mara",HALL);}

    // ------------------------------------------------------------------------------------------------ the evening's clock
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        // Gone from the kitchen, at the back with Kit, after promising to sit with her.
        if(promised(own)&&!own.getBoolean("Missed")&&own.getInt("Chapter")==PlaceForYou.DINNER&&atTheBack(own)){
            boolean back=PlaceForYouSite.inHouse(r)&&r.z<-22||r.z<-28.5;
            int away=back&&!PlaceForYouSite.inKitchen(r)?AWAY_FOR.merge(p.getUUID(),1,Integer::sum):0;
            if(away==0)AWAY_FOR.remove(p.getUUID());
            if(away>=AWAY){AWAY_FOR.remove(p.getUUID());
                PlaceForYou.edit(p,o->{o.putBoolean("Missed",true);o.putInt("MissedStage",1);o.putLong("MissedAt",l.getGameTime());});
                PlaceForYou.cue(p,"Mara is in the hall, looking down it at you.");}
            return;
        }
        // She stands there a moment, and goes back in.
        if(own.getInt("MissedStage")==1){var mara=PlaceForYou.actor(l,h,"Mara");
            if(mara!=null&&!own.getBoolean("MissedSaid")&&mara.position().subtract(new Vec3(h.base.getX()+HALL.at().x,h.base.getY(),h.base.getZ()+HALL.at().z)).horizontalDistanceSqr()<1){
                PlaceForYou.edit(p,o->o.putBoolean("MissedSaid",true));PlaceForYou.above(h,mara,"Oh.");}
            if(l.getGameTime()-own.getLong("MissedAt")>LOOKS)PlaceForYou.edit(p,o->o.putInt("MissedStage",2));
            return;
        }
        // Back in the kitchen, with the bag out: she says it back to them.
        if(own.getBoolean("Missed")&&own.getString("PromiseAnswer").isEmpty()&&own.getString("MaraEnd").isEmpty()&&!own.getBoolean("MaraAsked")&&(own.getInt("BackStage")>=3||own.getBoolean("KitGoing"))
                &&PlaceForYouSite.inKitchen(r)&&!Talk.waiting(p)){var mara=PlaceForYou.actor(l,h,"Mara");
            if(mara!=null&&mara.distanceToSqr(p)<20)reunion(p,h);}
    }

    // ------------------------------------------------------------------------------------------------ the promise
    static void reunion(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"promise_broken","Mara");
        PlaceForYou.line(p,h,"Mara","\"I'll sit with you tonight.\" That's what you said.");
        PlaceForYou.line(p,h,"Mara","I came to find you, and you were at the back door with Kit.");
        Talk.ask(p,Talk.reply("I said tonight. It isn't over yet.",q->{PlaceForYou.line(q,h,"Mara","No. It isn't.");PlaceForYou.line(q,h,"Mara","Then I'll hold you to the rest of it.");answered(q,"limited");})
            ,Talk.reply("I'm sorry. I said it, and then I went.",q->{PlaceForYou.line(q,h,"Mara","You did.");PlaceForYou.line(q,h,"Mara","Thank you for saying so. It doesn't undo it.");answered(q,"sorry");})
            ,Talk.reply("Getting Kit out mattered more.",q->{PlaceForYou.line(q,h,"Mara","Yes. I expect it did.");PlaceForYou.line(q,h,"Mara","Then don't promise me things.");answered(q,"kit");}));
    }
    static void answered(ServerPlayer p,String how){PlaceForYou.edit(p,o->o.putString("PromiseAnswer",how));Talk.end(p);}

    /** How Mara asks, when she decides, after a broken promise; null where she does not ask at all. */
    public static String ask(CompoundTag own){
        return switch(own.getString("PromiseAnswer")){
            case "limited"->"You said tonight. It's still tonight. Will you sit with me a minute?";
            case "sorry"->"I'll ask again anyway. Will you sit with me a minute? Before I decide.";
            case "kit"->null;
            default->"Will you sit with me a minute? Before I decide.";
        };
    }

    // ------------------------------------------------------------------------------------------------ the confession used
    /** The extra answer at the table, for a reader who has heard about Theo. */
    static Talk.Reply confession(PlaceForYou.House h){
        return Talk.reply("Mara, tell them about Theo. Or I will.",q->{
            var own=own(q);
            PlaceForYou.line(q,h,"Mara","...");
            Talk.line(q,"",own.getBoolean("RoomTold")?"You tell them: Theo, the last ordinary evening, and how she keeps the evening from getting to the part where he isn't here."
                :"You tell them what she told you at the sink: Theo, who used to dry, and the empty kitchen she won't talk in again.");
            PlaceForYou.line(q,h,"Kit","Is that true?");
            PlaceForYou.line(q,h,"Mara","Yes.");
            PlaceForYou.line(q,h,"Ren","She told you that on her own. You didn't have to do it like this.");
            Talk.ask(q,Talk.reply("It was the only thing that would work.",z->{PlaceForYou.line(z,h,"Ren","It worked. That isn't the same as it being all right.");used(z,h,"worked");})
                ,Talk.reply("You're right. I shouldn't have.",z->{PlaceForYou.line(z,h,"Ren","Say that to her, not to me.");used(z,h,"wrong");}));
        });
    }
    static void used(ServerPlayer p,PlaceForYou.House h,String how){
        PlaceForYou.line(p,h,"Mara","Open the door, then. I won't stop anyone.");
        PlaceForYou.line(p,h,"Mara","Don't sit with me after.");
        PlaceForYou.edit(p,o->{o.putBoolean("UsedConfession",true);o.putString("RenSaid",how);});
        PlaceForYou.confronted(p,h,"open");
    }

    // ------------------------------------------------------------------------------------------------ talking
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=own(p);if(!who.equals("Mara"))return false;
        if(own.getBoolean("Missed")&&own.getString("PromiseAnswer").isEmpty()){
            if(own.getInt("BackStage")>=3||own.getBoolean("KitGoing")){if(!Talk.waiting(p))reunion(p,h);}
            else PlaceForYou.above(h,a,"Go on. Finish it.");
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the account
    /** After the back passage: the promise, broken. */
    public static String promiseAccount(CompoundTag own){
        if(!own.getBoolean("Missed"))return "";
        var t=new StringBuilder("Mara came to find me, and I was at the back door with Kit.");
        switch(own.getString("PromiseAnswer")){
            case "limited"->t.append(" I said I'd said tonight, and it wasn't over yet. She said she'd hold me to the rest of it.");
            case "sorry"->t.append(" I said I was sorry. She said it didn't undo it.");
            case "kit"->t.append(" I said getting Kit out had mattered more. She said not to promise her things, then.");
            default->{}
        }
        return t.toString();
    }
    /** The confrontation, when the confession was used. */
    public static String confessionAccount(CompoundTag own){
        var t=new StringBuilder("At the table I told Mara to tell them about Theo, or I would, and then I told them myself. Kit asked if it was true, and she said yes. Ren said she had told me that on her own, and I hadn't had to do it like that.");
        t.append(own.getString("RenSaid").equals("wrong")?" I said Ren was right. Ren said to say that to her."
            :" I said it was the only thing that would work. Ren said working wasn't the same as it being all right.");
        t.append(" Mara said to open the door, and that she wouldn't stop anyone. She asked me not to sit with her after.");
        return t.toString();
    }
}
