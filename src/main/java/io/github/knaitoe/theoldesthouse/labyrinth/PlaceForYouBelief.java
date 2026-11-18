package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.694, A Place for You, slice 10 (docs/A_PLACE_FOR_YOU_0_4_694.md): two more of the proposal's consequence branches.
 *
 * The believed past. On the landing, after the guide has said the summer at the lake never happened, the reader can say they remember
 * it. Later the guide asks for something of it: the name of the dog that swam out to the dock. The reader can make one up, admit they
 * wanted it to be true, or say there was no dog and no summer. A name made up stays: the guide says it back on the landing while the
 * picture hangs, it goes when the picture comes down, and at the kept table (E2) it is part of the evening.
 *
 * The reassurance. While somebody is at the door, the reader can tell Mara it is him. She asks them to stand with her at the door;
 * at the door, with the voice by the gate, Ren (or Alex) asks whether the reader believes it or wants it to be true. If he is let in (E3),
 * Mara says the reader was right before she stops knowing them. If the reader lets the voice go quiet instead, she says so to them, and
 * they answer for it before she decides anything.
 *
 * Everything here is the reader's own copy and record.
 */
public final class PlaceForYouBelief {
    private PlaceForYouBelief(){}
    /** Ticks after the landing before the guide asks for the dog's name; after Mara calls before the voice answers; before the question. */
    public static final int DETAIL_AFTER=200,ANSWER=30,QUESTION=70;
    /** The name the reader can give the dog that never swam out to the dock. */
    public static final String DOG="Sailor";
    /** Mara inside the front door, by the hooks, facing it. */
    static final PlaceForYou.Mark BY_DOOR=PlaceForYou.stand(1.3,-13.6,0);

    static CompoundTag own(ServerPlayer p){return PlaceForYou.own(p);}
    static boolean believed(CompoundTag own){return own.getString("OlderSaid").equals("believe");}
    /** The dog's name made up and still remembered: the picture has not come down since. */
    public static boolean dogKept(CompoundTag own){return own.getString("BeliefAnswer").equals("invented")&&!own.getBoolean("DogLost");}
    /** The reassurance owed: the reader said it was him, and the voice has gone quiet without him let in. */
    static boolean owed(CompoundTag own){return own.getBoolean("Reassured")&&own.getBoolean("TheoSilent")&&own.getString("ReassureAnswer").isEmpty();}

    // ------------------------------------------------------------------------------------------------ where people stand
    static void marks(CompoundTag own,Map<String,PlaceForYou.Mark> m){
        int stage=own.getInt("ReassureStage");if(own.getInt("Chapter")==PlaceForYou.OPEN&&(stage==1||stage==2))m.put("Mara",BY_DOOR);
    }

    // ------------------------------------------------------------------------------------------------ the evening's clock
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        int ch=own.getInt("Chapter");long now=l.getGameTime();
        // The guide asks for something of the summer the reader said they remembered.
        if(believed(own)&&own.getString("BeliefAnswer").isEmpty()&&own.getInt("OlderStage")>=3&&(ch==PlaceForYou.SINK||ch==PlaceForYou.DINNER||ch==PlaceForYou.OPEN)
                &&now-own.getLong("BelievedAt")>=DETAIL_AFTER&&!Talk.waiting(p)){
            var a=PlaceForYou.actor(l,h,own.getString("OlderGuide"));
            if(a!=null&&a.distanceToSqr(p)<16&&a.hasLineOfSight(p))detail(p,h);
        }
        int stage=own.getInt("ReassureStage");
        // The evening gone on (kept, or the past let in) while she was still at the door: she leaves it there.
        if((stage==1||stage==2)&&ch!=PlaceForYou.OPEN){PlaceForYou.edit(p,o->{o.putInt("ReassureStage",3);if(stage==1)o.putBoolean("ReassureWaited",true);});return;}
        // Gone quiet while Mara still waited by the door for the reader: she waited, and they didn't come.
        if(stage==1&&own.getBoolean("TheoSilent")){PlaceForYou.edit(p,o->{o.putInt("ReassureStage",3);o.putBoolean("ReassureWaited",true);});return;}
        // By the door, the reader beside her: she opens it, if it's shut, and calls him.
        if(stage==1){var mara=PlaceForYou.actor(l,h,"Mara");
            if(mara!=null&&at(h,mara,BY_DOOR,1)&&p.distanceToSqr(mara)<6.25&&!Talk.waiting(p)){
                var b=h.base;
                if(!PlaceForYouSite.doorOpen(l,b)){PlaceForYouSite.frontDoor(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.8F,1F);}
                PlaceForYou.above(h,mara,"Theo?");h.lastVoice=h.tick;
                PlaceForYou.edit(p,o->{o.putInt("ReassureStage",2);o.putLong("ReassureAt",now);o.putBoolean("ReassureStood",true);});
            }
            return;
        }
        // The voice by the gate; then whoever is in the house asks the reader the one thing.
        if(stage==2){long since=now-own.getLong("ReassureAt");
            if(since>=ANSWER&&!own.getBoolean("ReassureVoice")){PlaceForYou.edit(p,o->o.putBoolean("ReassureVoice",true));cue(p,"From by the gate: \"Who else would it be?\"");h.lastVoice=h.tick;}
            if(since>=QUESTION&&!Talk.waiting(p))question(p,h);
            return;
        }
        // The voice gone quiet and he never came in: the reader answers for it, in the kitchen, before she decides.
        if(owed(own)&&PlaceForYouSite.inKitchen(r)&&!Talk.waiting(p)){var mara=PlaceForYou.actor(l,h,"Mara");
            if(mara!=null&&mara.distanceToSqr(p)<20)answer(p,h);}
    }
    static boolean at(PlaceForYou.House h,LiteraryActor a,PlaceForYou.Mark m,double within){
        return a.position().subtract(new Vec3(h.base.getX()+m.at().x,h.base.getY(),h.base.getZ()+m.at().z)).horizontalDistanceSqr()<within;
    }
    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}

    // ------------------------------------------------------------------------------------------------ the believed past
    /** The landing's sixth answer: the reader remembers the summer at the lake. */
    static Talk.Reply remember(PlaceForYou.House h,String who){
        return Talk.reply("I remember. I had the end of the dock.",q->{
            PlaceForYou.line(q,h,who,"You do?");
            PlaceForYouOlder.said(q,h,who,"believe","Then I'd like to have it too.");
            PlaceForYou.edit(q,o->o.putLong("BelievedAt",q.serverLevel().getGameTime()));
        });
    }
    static void detail(ServerPlayer p,PlaceForYou.House h){
        String who=own(p).getString("OlderGuide");
        Talk.begin(p,"believe_detail",who);
        PlaceForYou.line(p,h,who,"I keep trying to see it. The dock.");
        PlaceForYou.line(p,h,who,"There was a dog that swam out to us. What was its name?");
        Talk.ask(p,Talk.reply(DOG+". It shook itself all over you.",q->{
                PlaceForYou.line(q,h,who,DOG+".");PlaceForYou.line(q,h,who,"Yes. I can see it now. I couldn't before you said it.");detailed(q,"invented");})
            ,Talk.reply("I don't know. I wanted it to be true.",q->{
                PlaceForYou.line(q,h,who,"So did I. I was going to make one up and say it to you.");PlaceForYou.line(q,h,who,"Then neither of us knows. We could start from there.");detailed(q,"admitted");})
            ,Talk.reply("There wasn't a dog. There wasn't a summer.",q->{
                PlaceForYou.line(q,h,who,"No. I know.");PlaceForYou.line(q,h,who,"I'd still have liked the dog.");detailed(q,"retracted");}));
    }
    static void detailed(ServerPlayer p,String how){PlaceForYou.edit(p,o->o.putString("BeliefAnswer",how));Talk.end(p);}

    // ------------------------------------------------------------------------------------------------ the reassurance
    /** Mara's extra answer while somebody is at the door, once the voice has been heard. */
    static Talk.Reply reassure(PlaceForYou.House h){
        return Talk.reply("It's him. I'm sure.",q->{
            PlaceForYou.line(q,h,"Mara","Nobody can be sure of that.");
            PlaceForYou.line(q,h,"Mara","Will you stand with me at the door?");
            Talk.ask(q,Talk.reply("I'll stand with you.",z->{PlaceForYou.line(z,h,"Mara","By the door, then.");
                    PlaceForYou.edit(z,o->{o.putBoolean("Reassured",true);o.putInt("ReassureStage",1);});Talk.end(z);
                    cue(z,"Mara goes to the front door, and waits for you beside it.");})
                ,Talk.reply("No. I shouldn't have said that.",z->{PlaceForYou.line(z,h,"Mara","No. You shouldn't have.");
                    PlaceForYou.edit(z,o->{o.putBoolean("ReassureTookBack",true);o.putInt("ReassureStage",3);});Talk.end(z);}));
        });
    }
    /** At the door, the voice answering: whoever is in the house asks the reader whether they believe it. */
    static void question(ServerPlayer p,PlaceForYou.House h){
        var l=p.serverLevel();String who=PlaceForYou.actor(l,h,"Ren")!=null?"Ren":"Alex";
        Talk.begin(p,"reassure",who);
        PlaceForYou.line(p,h,who,"Do you believe it's him, or do you want it to be?");
        Talk.ask(p,Talk.reply("I believe it.",q->{PlaceForYou.line(q,h,who,"Then I hope you're right. She'll stand there all night now.");asked(q,who,"believe");})
            ,Talk.reply("I want it to be.",q->{PlaceForYou.line(q,h,who,"So does she. That's why she believed you.");asked(q,who,"want");})
            ,Talk.reply("I don't know.",q->{PlaceForYou.line(q,h,who,"Then don't tell her you're sure.");asked(q,who,"unsure");}));
    }
    static void asked(ServerPlayer p,String who,String said){PlaceForYou.edit(p,o->{o.putInt("ReassureStage",3);o.putString("ReassureAsker",who);o.putString("ReassureSaid",said);});Talk.end(p);}
    /** The voice gone quiet: Mara says so to the reader, who answers for it. */
    static void answer(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);
        Talk.begin(p,"reassure_owed","Mara");
        PlaceForYou.line(p,h,"Mara","You told me it was him.");
        PlaceForYou.line(p,h,"Mara",own.getBoolean("ReassureStood")?"I stood at that door because you said so.":"I waited by the door for you. You didn't come.");
        PlaceForYou.line(p,h,"Mara","And then you let him stop.");
        Talk.ask(p,Talk.reply("I wanted it to be him. I shouldn't have said I was sure.",q->{PlaceForYou.line(q,h,"Mara","No. You shouldn't have.");answered(q,"wrong");})
            ,Talk.reply("It was him. That's why I couldn't let him in.",q->{PlaceForYou.line(q,h,"Mara","Then you heard him, and shut him out anyway.");answered(q,"him");})
            ,Talk.reply("I'm sorry.",q->{PlaceForYou.line(q,h,"Mara","I stood there because of you. Sorry doesn't change where I stood.");answered(q,"sorry");}));
    }
    static void answered(ServerPlayer p,String how){PlaceForYou.edit(p,o->o.putString("ReassureAnswer",how));Talk.end(p);}

    // ------------------------------------------------------------------------------------------------ talking
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=own(p);
        if(who.equals(own.getString("OlderGuide"))&&believed(own)&&own.getString("BeliefAnswer").isEmpty()&&own.getInt("OlderStage")>=3&&!Talk.waiting(p)){
            int ch=own.getInt("Chapter");if(ch==PlaceForYou.SINK||ch==PlaceForYou.DINNER||ch==PlaceForYou.OPEN){detail(p,h);return true;}}
        if(!who.equals("Mara"))return false;
        if(own.getInt("ReassureStage")==1){PlaceForYou.above(h,a,"Here. Stand by me.");return true;}
        if(own.getInt("ReassureStage")==2)return true;
        if(owed(own)){if(!Talk.waiting(p))answer(p,h);return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the account
    /** After the landing: the dog's name asked for, and what the reader said. */
    public static String beliefAccount(CompoundTag own){
        if(!believed(own))return "";String who=own.getString("OlderGuide");
        var t=new StringBuilder();
        switch(own.getString("BeliefAnswer")){
            case "invented"->{t.append("Later ").append(who).append(" asked me the name of the dog that swam out to us that summer. I said ").append(DOG)
                .append(", and that it shook itself all over us. ").append(who).append(" said, \"Yes. I can see it now. I couldn't before you said it.\"");
                if(own.getBoolean("DogLost"))t.append(" When the picture came down, the dog went with the rest.");}
            case "admitted"->t.append("Later ").append(who).append(" asked me the name of the dog that swam out to us that summer. I said I didn't know, and that I had wanted it to be true. ")
                .append(who).append(" said, \"So did I. Then neither of us knows. We could start from there.\"");
            case "retracted"->t.append("Later ").append(who).append(" asked me the name of the dog that swam out to us that summer. I said there hadn't been a dog, or a summer. ")
                .append(who).append(" said, \"No. I know. I'd still have liked the dog.\"");
            default->{}
        }
        return t.toString();
    }
    /** While somebody was at the door: what the reader told Mara, and what it cost. */
    public static String reassureAccount(CompoundTag own){
        if(!own.getBoolean("Reassured")&&!own.getBoolean("ReassureTookBack"))return "";
        if(own.getBoolean("ReassureTookBack"))return " I told Mara it was him, and that I was sure. Then I took it back. She said I shouldn't have said it.";
        var t=new StringBuilder(" I told Mara it was him, and that I was sure. She asked me to stand with her at the door.");
        if(own.getBoolean("ReassureWaited"))t.append(" She waited there for me, and I didn't go.");
        else if(own.getBoolean("ReassureStood")){
            t.append(" I stood with her, and she called his name, and the voice by the gate said, \"Who else would it be?\"");
            String who=own.getString("ReassureAsker");
            switch(own.getString("ReassureSaid")){
                case "believe"->t.append(" ").append(who).append(" asked whether I believed it was him or wanted it to be. I said I believed it.");
                case "want"->t.append(" ").append(who).append(" asked whether I believed it was him or wanted it to be. I said I wanted it to be. So did she, ").append(who).append(" said.");
                case "unsure"->t.append(" ").append(who).append(" asked whether I believed it was him or wanted it to be. I said I didn't know. Then I shouldn't tell her I was sure, ").append(who).append(" said.");
                default->{}
            }
        }
        return t.toString();
    }
    /** After the voice stopped: the reader answering for having said it was him. */
    public static String owedAccount(CompoundTag own){
        return switch(own.getString("ReassureAnswer")){
            case "wrong"->" Mara said I had told her it was him, and then let him stop. I said I had wanted it to be him, and shouldn't have said I was sure. She agreed.";
            case "him"->" Mara said I had told her it was him, and then let him stop. I said it was him, and that was why I couldn't let him in. She said then I had heard him and shut him out anyway.";
            case "sorry"->" Mara said I had told her it was him, and then let him stop. I said I was sorry. She said sorry didn't change where she had stood.";
            default->"";
        };
    }
}
