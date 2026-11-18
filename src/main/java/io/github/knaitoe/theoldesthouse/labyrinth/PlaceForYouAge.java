package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.697, A Place for You, slice 13 (docs/A_PLACE_FOR_YOU_0_4_697.md): the direction of care. The waiting room's settled read
 * (AGE) used to change one line of Mara's welcome. Now the household acts on what it assumes through the evening, the reader can
 * answer it, and the answer comes back when they sit down, at the table and in the account.
 *
 * DIGITAL, someone come back after starting a life elsewhere: the room kept, and a reason never to go back to that life. TRANSITIONAL,
 * someone expected to manage everyone: Ren and Alex hand the reader the job of telling Mara that Kit is going. ANALOG, someone the
 * household has decided to look after: Mara decides where the reader sits and what is done for them. Read but unsettled, a guest: Alex
 * says they will be one of the household by pudding. Nothing happens for a reader the House was asked not to read.
 *
 * The assumption is made aloud once, over the speaker's head, between supper and dinner. The exchange opens the next time the reader
 * talks to that person with nothing else to say to them (it never takes the place of a conversation that moves the evening on).
 * Leaving it unanswered lets the assumption stand. Nothing here changes what the reader can do.
 */
public final class PlaceForYouAge {
    private PlaceForYouAge(){}

    /** Which variant: DIGITAL, TRANSITIONAL, ANALOG, guest (read, unsettled), or empty for a reader the House was asked not to read. */
    public static String kind(CompoundTag own){
        var snap=own.getCompound("Snap");if(!snap.getBoolean("Read"))return "";
        return switch(snap.getString("Age")){case "DIGITAL","TRANSITIONAL","ANALOG"->snap.getString("Age");default->"guest";};
    }
    /** Who makes the assumption. */
    public static String speaker(String kind){return switch(kind){case "TRANSITIONAL"->"Ren";case "guest"->"Alex";default->"Mara";};}
    static String answer(CompoundTag own){return own.getString("AgeAnswer");}

    // ------------------------------------------------------------------------------------------------ the assumption, aloud
    /** Once, between supper and dinner, in the kitchen or the sitting room, within ten blocks of the speaker. */
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        String kind=kind(own);if(kind.isEmpty()||own.getBoolean("AgeShown"))return;
        int ch=own.getInt("Chapter");if(ch<PlaceForYou.SUPPER||ch>=PlaceForYou.DINNER||Talk.waiting(p))return;
        if(!PlaceForYouSite.inKitchen(r)&&!PlaceForYouSite.inSitting(r))return;
        var a=PlaceForYou.actor(l,h,speaker(kind));if(a==null||p.distanceToSqr(a)>100)return;
        PlaceForYou.above(h,a,shown(kind));PlaceForYou.edit(p,o->o.putBoolean("AgeShown",true));
    }
    public static String shown(String kind){
        return switch(kind){
            case "DIGITAL"->"Your coat's on the back of your door, where you left it.";
            case "TRANSITIONAL"->"When you've a minute. Alex and I need you to settle something.";
            case "ANALOG"->"Don't get up. I'll bring it to you.";
            default->"First night's the strangest. After that you're one of us.";
        };
    }

    // ------------------------------------------------------------------------------------------------ the exchange
    /** In place of the speaker's idle word, between supper and dinner, once the assumption has been made: the exchange, once. */
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=PlaceForYou.own(p);String kind=kind(own);
        if(kind.isEmpty()||!own.getBoolean("AgeShown")||own.getBoolean("AgeAsked")||!who.equals(speaker(kind))||Talk.waiting(p))return false;
        int ch=own.getInt("Chapter");if(ch<PlaceForYou.SUPPER||ch>PlaceForYou.DINNER)return false;
        PlaceForYou.edit(p,o->o.putBoolean("AgeAsked",true));
        Talk.begin(p,"age",who);
        switch(kind){
            case "DIGITAL"->{PlaceForYou.line(p,h,"Mara","You don't have to go back to that place of yours, you know. Whatever it is. Your room's here.");
                Talk.ask(p,Talk.reply("I have a place of my own now.",q->answered(q,h,"Mara","Yes. Of course you do. I won't say it again.","corrected"))
                    ,Talk.reply("I might stay a while.",q->answered(q,h,"Mara","A while. Good. That's all I meant.","accepted"))
                    ,Talk.reply("Leave my room as it is.",q->answered(q,h,"Mara","I always have.","kept")));}
            case "TRANSITIONAL"->{PlaceForYou.line(p,h,"Ren","Kit's going, and somebody has to tell Mara. Alex says it should be you. You always know how to put things.");
                PlaceForYou.line(p,h,"Alex","Ren says so too. We agree on that much.");
                Talk.ask(p,Talk.reply("You two tell her. Together.",q->{PlaceForYou.line(q,h,"Ren","...All right. Together.");answered(q,h,"Alex","Together, then.","corrected");})
                    ,Talk.reply("I'll tell her.",q->answered(q,h,"Ren","Thank you. I knew you would.","accepted"))
                    ,Talk.reply("Why is it always me?",q->{PlaceForYou.line(q,h,"Ren","Because it always has been.");answered(q,h,"Alex","That isn't an answer, Ren.","asked");}));}
            case "ANALOG"->{PlaceForYou.line(p,h,"Mara","I've put you nearest the stove, it's warmest. And I'll cut your supper up for you. It's easier.");
                Talk.ask(p,Talk.reply("I can manage, thank you.",q->answered(q,h,"Mara","Of course you can. I forget.","corrected"))
                    ,Talk.reply("Thank you.",q->answered(q,h,"Mara","There. Isn't that better.","accepted"))
                    ,Talk.reply("Let me choose where I sit.",q->answered(q,h,"Mara","...Choose, then.","chose")));}
            default->{PlaceForYou.line(p,h,"Alex","You'll be one of us by pudding. That's how it goes here. It went that way for me.");
                Talk.ask(p,Talk.reply("I'm only here for supper.",q->answered(q,h,"Alex","Course. Everyone is, the first time.","corrected"))
                    ,Talk.reply("I'd like that.",q->answered(q,h,"Alex","See? Easy.","accepted"))
                    ,Talk.reply("How long have you been here?",q->answered(q,h,"Alex","Since a supper. I couldn't tell you which.","asked")));}
        }
        return true;
    }
    static void answered(ServerPlayer p,PlaceForYou.House h,String who,String line,String how){
        PlaceForYou.line(p,h,who,line);PlaceForYou.edit(p,o->o.putString("AgeAnswer",how));Talk.end(p);
    }

    // ------------------------------------------------------------------------------------------------ sitting down
    /** At dinner, once the spoon is out of Mara's hand and everyone sits, near the table: the assumption again, or the answer kept. */
    static void dinner(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        String kind=kind(own);if(kind.isEmpty()||!own.getString("AgeTableSaid").isEmpty()||own.getInt("Chapter")!=PlaceForYou.DINNER)return;
        if(!own.getBoolean("SpoonTaken")||!PlaceForYouSite.inKitchen(r)||Talk.waiting(p))return;
        String who=tableSpeaker(own);var a=PlaceForYou.actor(l,h,who);if(a==null||p.distanceToSqr(a)>100)return;
        String line=table(own);PlaceForYou.above(h,a,line);PlaceForYou.edit(p,o->{o.putString("AgeTableSaid",line);o.putString("AgeTableWho",who);});
    }
    public static String tableSpeaker(CompoundTag own){
        String a=answer(own);
        return switch(kind(own)){case "TRANSITIONAL"->a.equals("corrected")?"Ren":"Alex";case "guest"->a.equals("corrected")?"Alex":"Mara";default->"Mara";};
    }
    public static String table(CompoundTag own){
        String a=answer(own);
        return switch(kind(own)){
            case "DIGITAL"->a.equals("corrected")?"This place of yours. Is it warm? Tell me it's warm.":"I've put clean sheets on your bed. For tonight, and after.";
            case "TRANSITIONAL"->a.equals("corrected")?"We told her, Alex and me. She took it better than she would have from you.":"Go on. Everyone's waiting for you to say it.";
            case "ANALOG"->switch(a){case "corrected"->"Help yourself. You said you could manage.";case "chose"->"Sit where you like. You wanted to choose.";default->"I've cut it up for you. Eat.";};
            default->a.equals("corrected")?"Only supper, you said. I'll hold you to it.":"There. You're not a guest any more.";
        };
    }

    // ------------------------------------------------------------------------------------------------ at the table
    /** Mara, as she opens the confrontation, once the assumption has been made: it used as a lever, or the reader's answer turned back on them. */
    static void confront(ServerPlayer p,PlaceForYou.House h,CompoundTag own){
        if(kind(own).isEmpty()||!own.getBoolean("AgeShown")||!own.getString("AgeLeverSaid").isEmpty())return;
        String line=lever(own);PlaceForYou.line(p,h,"Mara",line);PlaceForYou.edit(p,o->o.putString("AgeLeverSaid",line));
    }
    public static String lever(CompoundTag own){
        String a=answer(own);
        return switch(kind(own)){
            case "DIGITAL"->a.equals("corrected")?"You've a place of your own. I know. I'm asking anyway.":"You came home. Don't make me watch you leave it twice.";
            case "TRANSITIONAL"->a.equals("corrected")?"Ren and Alex told me about Kit. I know you put them up to it.":"Go on. You're the one who decides things here. Decide this.";
            case "ANALOG"->switch(a){case "corrected"->"You can manage. You told me. Then manage this.";case "chose"->"You wanted to choose. Then choose.";default->"Don't you worry about any of this. We'll decide it. You sit.";};
            case "guest"->a.equals("corrected")?"You said only supper. It's after supper.":"You're one of us now. Alex said so.";
            default->"";
        };
    }

    // ------------------------------------------------------------------------------------------------ the account
    /** What the household assumed, what the reader said, and how it came back. Empty if unread or never made aloud. */
    public static String account(CompoundTag own){
        String kind=kind(own);if(kind.isEmpty()||!own.getBoolean("AgeShown"))return "";
        String a=answer(own);var t=new StringBuilder();
        switch(kind){
            case "DIGITAL"->t.append("Mara had kept my coat on the back of a door she called mine, and told me I didn't have to go back to the place I lived now.").append(switch(a){
                case "corrected"->" I told her I had a place of my own. She said she wouldn't say it again.";
                case "accepted"->" I said I might stay a while, and she said that was all she had meant.";
                case "kept"->" I asked her to leave my room as it was. She said she always had.";
                default->" I never answered her.";});
            case "TRANSITIONAL"->t.append("Ren and Alex wanted me to be the one to tell Mara that Kit was going, because I always knew how to put things.").append(switch(a){
                case "corrected"->" I said Ren and Alex should tell her together, and that was agreed.";
                case "accepted"->" I said I would tell her.";
                case "asked"->" I asked why it was always me. Ren said it always had been, and Alex said that wasn't an answer.";
                default->" I never answered either of them.";});
            case "ANALOG"->t.append("Mara put me nearest the stove and said she would cut my supper up for me.").append(switch(a){
                case "corrected"->" I said I could manage. She said she forgot.";
                case "accepted"->" I thanked her.";
                case "chose"->" I asked to choose where I sat, and she let me.";
                default->" I didn't say anything.";});
            default->t.append("Alex said I would be one of them by pudding, the way he had been.").append(switch(a){
                case "corrected"->" I said I was only there for supper. Everyone was, the first time, he said.";
                case "accepted"->" I said I'd like that.";
                case "asked"->" I asked how long he had been there. Since a supper, he said; he couldn't tell me which.";
                default->" I didn't answer him.";});
        }
        String table=own.getString("AgeTableSaid");
        if(!table.isEmpty())t.append(" Later, at the table, ").append(own.getString("AgeTableWho")).append(" said, \"").append(table).append("\"");
        String lever=own.getString("AgeLeverSaid");
        if(!lever.isEmpty())t.append(" When it came to deciding, Mara said, \"").append(lever).append("\"");
        return t.toString();
    }

    // ------------------------------------------------------------------------------------------------ the Witness line
    /** The Witness line, with what the household assumed and, if the reader answered it, the answer. */
    public static String witness(CompoundTag own,String text){
        String kind=kind(own);if(kind.isEmpty()||!own.getBoolean("AgeShown"))return text;
        String a=answer(own);
        return text+" "+switch(kind){
            case "DIGITAL"->a.equals("corrected")?"They had kept my room, and I told them I had a place of my own.":"They had kept my room for me.";
            case "TRANSITIONAL"->a.equals("corrected")?"They wanted me to be the one who told her, and I gave it back to them.":"They wanted me to be the one who told her.";
            case "ANALOG"->switch(a){case "corrected"->"They meant to look after me, and I told them I could manage.";case "chose"->"They meant to look after me, and I chose where I sat.";default->"They meant to look after me.";};
            default->a.equals("corrected")?"Someone there said I'd be one of them by pudding. I said I was only there for supper.":"Someone there said I'd be one of them by pudding.";
        };
    }
}
