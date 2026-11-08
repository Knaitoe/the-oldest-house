package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.364, Last Weekend at Pinehook (docs/PINEHOOK_0_4_364.md): what is said and written at the cabin. Original text, in the voice of each
 * of the four: Casey, who has planned a weekend nobody will forget; Jude, an actor doing a friend a favour; Nina, who knows why she was
 * asked; Len, who owns the place. The parts are named as parts; nothing here says who a reader is, what they look like, or how old.
 */
public final class PinehookTexts {
    private PinehookTexts(){}
    public static final String HINT="A cabin by a lake, a weekend with friends, and a host who has planned every minute of it.";

    /** The source: Casey's invitation, left on the guest book's stand on the porch. */
    public static ItemStack source(){
        return book("Last Weekend at Pinehook","Casey",List.of(
            "LAST WEEKEND AT PINEHOOK\n\nFriday to Sunday. The cabin on the lake past the general store, at the end of the gravel drive.\n\nBring a sleeping bag, something to swim in and a bad attitude.",
            "The owner's a guy called Len. He's letting us have the place for almost nothing, so be nice to him. He'll come by to check on things.\n\nNina's coming. Len asked for her specially. Don't make it weird.",
            "I have planned a weekend you will never forget. That's a promise.\n\n— Casey\n\nP.S. Somebody bring a first-aid kit. You know what we're like."));
    }
    static ItemStack book(String title,String author,List<String> pages){return io.github.knaitoe.theoldesthouse.house.HouseWriting.book(title,author,io.github.knaitoe.theoldesthouse.house.HouseWriting.WritingStyle.PLAIN,pages);}

    // ------------------------------------------------------------------------------------------------ the parts
    /** The six parts Casey offers, the reply that takes each, and what he says as he hands over its thing. */
    public enum Part {
        ATHLETE("The Athlete","The Athlete. I'll carry things.","Then you're on the cooler. It's on the porch, and it goes in the kitchen. It's heavy. Bend your knees."),
        SKEPTIC("The Skeptic","The Skeptic. None of this is real.","Then you'll want these. A camera, and a torch, for when you go looking for whatever's out there and it turns out to be a raccoon."),
        CHARMER("The Charmer","The Charmer. I'll keep everyone happy.","Drinks are in the cooler and the music's on you. Here. I made this one myself."),
        WILDCARD("The Wildcard","The Wildcard. Who knows.","Then this is yours. You left it in my car last summer. Batteries still work, mostly."),
        FAN("The Horror Fan","The Horror Fan. I know how these go.","You brought a film. Of course you did. Fine. We'll argue about it later."),
        RESPONSIBLE("The Responsible One","The Responsible One. Somebody has to be.","Thank you for bringing that. Nobody else would have.");
        public final String title,reply,hand;
        Part(String title,String reply,String hand){this.title=title;this.reply=reply;this.hand=hand;}
        public static Part of(String name){for(var q:values())if(q.name().equals(name))return q;return null;}
    }
    public static final String NO_PART="None of them. I'm just here for the weekend.",NO_PART_ANSWER="Suit yourself. Everybody's somebody by Sunday.";

    // ------------------------------------------------------------------------------------------------ Casey
    public static final String GREET="You made it! Okay, house rules. It's a horror weekend, so everybody gets a part. Pick one.";
    public static final String GREET_LATE="There you are. We started without you. It's a horror weekend: pick a part, any part.";
    public static final String[] CASEY_IDLE={"Somebody put the music on. It's too quiet.","The lake's nice, right? Len says it's deeper than it looks.","Has anybody seen my lighter? Never mind.","Tonight's going to be a good night. Trust me."};
    public static final String CASEY_TALK="What's up?";
    public static final String POWER_ASK="Bet you the power goes out tonight.",POWER_ANSWER="Ha. Don't jinx it.";
    public static final String COOLER_SHOW="Kitchen's through there. I'll show you.";
    public static final String SECRET="Hey. Don't tell the others. Jude's going to give everybody a scare later, after dark. Mask, the whole thing. It's going to be great.";
    public static final String SECRET_KEEP="I won't say a word.",SECRET_KEEP_ANSWER="Knew I could count on you.";
    public static final String SECRET_OBJECT="That's a terrible idea, Casey.",SECRET_OBJECT_ANSWER="It's going to be fine. It's a bit. Nobody gets hurt in a bit.";
    public static final String SECRET_WHO="Who's Jude, exactly?",SECRET_WHO_ANSWER="Jude's an actor. A real one. He's doing me a favour.";
    public static final String MUSIC_ON="Now it's a weekend.";
    public static final String FILM_CASEY="Good. Something everybody likes.",FILM_FAN="Oh, come on. Fine. Fine! But when it's terrible I'm talking through it.";
    public static final String FOOD="Burgers! Somebody's useful.";
    public static final String BREAKER="Whoa. Power's out. It's the box, it does this. Everybody stay put, I'll go look.";
    public static final String POWER_BACK="And we're back. Old wiring. Told you.";
    public static final String UNMASK_CASEY="Okay. Okay! Surprise. That's Jude. It was a bit. It was a good bit.";
    public static final String UNMASK_JUDE="Hi. Sorry. It was Casey's idea.";
    public static final String AWK_TOLD="Were you going to tell us?",AWK_TOLD_ANSWER="Eventually! That's the whole point. You find out at the end and everybody laughs.";
    public static final String AWK_POWER="You cut the power for this?",AWK_POWER_ANSWER="It's one switch. It's fine. It's back on.";
    public static final String AWK_GOOD="That was actually good.",AWK_GOOD_ANSWER="Thank you! Somebody appreciates it.";
    public static final String JUDE_CHANGE="I'm going to go get out of this thing.";

    // ------------------------------------------------------------------------------------------------ Jude
    public static final String JUDE_TALK="Hey. Casey's friend. Jude.";
    public static final String JUDE_ASK="What have you been in?",JUDE_ASK_ANSWER="Things you've never seen. A dead body on a cop show. Twice. Same body.";
    public static final String JUDE_MOCK="Should I know your face?",JUDE_MOCK_ANSWER="No. That's sort of the job.";
    public static final String JUDE_OWNER="Does the owner know you're here?",JUDE_OWNER_ANSWER="Casey said he told him. Why?";
    public static final String[] JUDE_IDLE={"Nice place. Smells like pine and mouse.","I'm going to look at the lake later. I grew up near one like this.","Casey talks a lot. He means well."};
    public static final String JUDE_LAKE="I'm going to go look at the lake for a bit.";
    public static final String JUDE_FILM="I was in one of these. Not this one. One like it.";

    // ------------------------------------------------------------------------------------------------ Nina
    public static final String NINA_TALK="Hi.";
    public static final String NINA_SIT="Sit with her.",NINA_SIT_ANSWER="You don't have to keep me company. It's nice, though.";
    public static final String NINA_HOW="Have you been here before?",NINA_HOW_ANSWER="Once. A long time ago. It hasn't changed.";
    public static final String NINA_LEN="What did Len say to you?",NINA_LEN_ANSWER="He said the lake's low this year. That's all.";
    public static final String[] NINA_IDLE={"Is there more ice?","I like it when it gets dark out here. You can hear everything."};
    public static final String NINA_QUIET="";
    public static final String NINA_BACK="What's going on? I was in the bathroom.";

    // ------------------------------------------------------------------------------------------------ Len
    public static final String LEN_ARRIVE="Evening. Just checking on the place. Don't mind me.";
    public static final String LEN_TALK="Boathouse stays locked. There's nothing in there for you. And the lake's deeper off the end of the dock than it looks.";
    public static final String LEN_SERIOUS="We'll stay out of it.",LEN_SERIOUS_ANSWER="Good. That's all I ask.";
    public static final String LEN_DISMISS="Sure, Len.",LEN_DISMISS_ANSWER="I mean it.";
    public static final String LEN_MUSIC="Turn that down a little, would you? Sound carries on the water.";

    // ------------------------------------------------------------------------------------------------ the turn
    public static final String TURN_1="Who else did you hire?",TURN_2="Nobody.",TURN_3="Then tell me who was in the shed.";
    public static final String WITNESS_1="It was Nina. She had a knife. A real one.",WITNESS_2="Nina? No. That's — no.",WITNESS_3="Then where is she?";
    public static final String PREVENT_1="Thanks for the company. That shed's creepy in the dark.",PREVENT_2="Everyone okay? Good. Good.";
    public static final String JUDE_HURT="I was getting out of the suit. Someone came in behind me. I thought it was Casey. Then I was on the floor.";
    public static final String CASEY_MINIMISE="You probably tripped. It's dark in there.";
    public static final String JUDE_NOT="I didn't trip.";
    public static final String JUDE_BANDAGED="Thanks. That's — thanks. Keep pressing. It's not as bad as it looks. I think.";

    // ------------------------------------------------------------------------------------------------ the account (provisional)
    /** What a reader's evening comes to, from their own record: the part they took, what they did and saw, and how the shed went. The last
     * slice writes the whole weekend's account; until then this is the evening as far as it goes. */
    public static ItemStack account(String name,net.minecraft.nbt.CompoundTag own){
        var lines=new ArrayList<String>();
        var part=Part.of(own.getString("Part"));
        lines.add(part==null?"Casey wanted everyone to play a part. I came as myself.":"Casey wanted everyone to play a part. I said I'd be "+part.title.toLowerCase(Locale.ROOT).replaceFirst("^the ","the ")+".");
        var did=new ArrayList<String>();
        if(own.getBoolean("SetCooler"))did.add("carried the cooler in");if(own.getBoolean("PlayedMusic"))did.add("put the music on");if(own.getBoolean("ChoseFilm"))did.add("chose the film");
        if(own.getBoolean("ServedFood"))did.add("got the burgers out");if(own.getBoolean("WalkedDock"))did.add("walked out to the end of the dock");
        if(!did.isEmpty())lines.add("I "+join(did)+".");
        if(own.getBoolean("KeptSecret"))lines.add("Casey told me about the mask, and I kept it to myself.");else if(own.getBoolean("Objected"))lines.add("Casey told me about the mask. I told him it was a bad idea.");
        if(own.getBoolean("SawNinaQuiet"))lines.add(own.getBoolean("AskedNina")?"When the owner came up the lane Nina stopped talking. I asked her what he'd said. She said the lake was low.":"When the owner came up the lane Nina stopped talking.");
        if(own.getBoolean("FoundElastic"))lines.add("Behind the shed, in a bag somebody had thrown down, there was a strap of elastic cut to fit a mask.");
        if(own.getBoolean("Unmasked"))lines.add("When the lights went out I got hold of the mask at the window and pulled it off. It was Jude.");
        else if(own.getBoolean("SawRubber"))lines.add("When the lights went out there was a face at the window. In the torch it was rubber.");
        else if(own.getBoolean("SawMask"))lines.add("When the lights went out there was a face at the window.");
        switch(own.getString("Outcome")){
            case "wounded"->lines.add(own.getBoolean("SawFigure")?"Jude came back from the shed cut and asked Casey who else he'd hired. Down at the end of the dock someone in the costume walked across the planks.":"Jude came back from the shed cut, and asked Casey who else he'd hired.");
            case "witnessed"->lines.add(own.getBoolean("WitnessedAttack")?"I got into the shed while she was at him. It was Nina, with a knife.":"Someone got into the shed in time. Jude said it was Nina.");
            case "prevented"->lines.add(own.getBoolean("WithJude")?"I stayed with Jude while he changed. Nothing happened. Nobody came.":"Jude changed in the shed and came back. Nothing happened, that I saw.");
            default->{}
        }
        if(own.getBoolean("HelpedJude"))lines.add("I held a dressing on his cut until it stopped.");
        lines.add("That was the first night.");
        return book("The account: the weekend at Pinehook",name,List.of(String.join(" ",lines)));
    }
    static String join(List<String> parts){if(parts.size()==1)return parts.get(0);return String.join(", ",parts.subList(0,parts.size()-1))+" and "+parts.get(parts.size()-1);}
}
