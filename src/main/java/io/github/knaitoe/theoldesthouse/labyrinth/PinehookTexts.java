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

    // ------------------------------------------------------------------------------------------------ 0.4.365, the night
    public static final String LEN_GOES="I'm going down to the boat. Lock your doors tonight. All of you.";
    // Jude in the kitchen, with Casey talking over him until someone stops him.
    public static final String JUDE_CUT="I was getting out of the—",CASEY_OVER="He's in shock. He doesn't know what he saw. Give him some air.";
    public static final String HUSH="Casey. Let him talk.",HUSH_ANSWER="Fine. Fine. Talk.";
    public static final String JUDE_NIGHT_WOUNDED="Okay. Ask me. While I can still think straight.",JUDE_NIGHT_WITNESSED="She had a knife. Nina had a real knife.",
        JUDE_NIGHT_PREVENTED="Nothing happened. Somebody walked round behind the shed while I was changing. Then they went away.";
    public static final String JUDE_Q_WHO="Who came into the shed?",JUDE_Q_WHO_PREVENTED="Who was behind the shed?";
    public static final String JUDE_WHO_WOUNDED="Somebody small, in a coat and gloves. They didn't say a word. They went for my bag, not me. I got in the way.",
        JUDE_WHO_WITNESSED="Nina. You saw her. She didn't even look surprised to see me.",JUDE_WHO_PREVENTED="I don't know. Len, maybe. They stood there a long time.";
    public static final String JUDE_Q_STRAP="Do you tie the mask on, or buckle it?",JUDE_STRAP="Tie it. The buckle broke in the spring, so I knot it at the back. Why?";
    public static final String STRAP_CUE="The strap from behind the shed has a buckle at each end, and it's cut to fit somebody's head. Not his.";
    public static final String JUDE_Q_KNEW="Who knew you'd change in the shed?",JUDE_KNEW="Casey. And whoever Casey told.";
    public static final String CASEY_TOLD="I didn't tell anyone. I— Nina asked what was in your bag. I told her. That's all.";
    public static final String JUDE_Q_BOAT="Did you see anyone by the boathouse today?",JUDE_BOAT="Nina. Before we ate. Trying the door. I thought she was looking for the toilet.";
    // Casey, in the night.
    public static final String CASEY_NIGHT_WOUNDED="People trip. It's dark. Everybody calm down.",CASEY_NIGHT_WITNESSED="That was not part of it. Okay? That was not the plan.",
        CASEY_NIGHT_PREVENTED="See? Everyone's fine. It's fine.",CASEY_NIGHT_TURNED="I didn't hire anyone else. I swear I didn't.";
    public static final String CASEY_Q_WHO="Who else knew about the prank?",CASEY_WHO="Nobody. Jude. Me. ...Nina asked what was in Jude's bag. I told her it was a surprise. Then I told her what the surprise was.";
    public static final String CASEY_Q_PLAN="Did you write any of this down?",CASEY_PLAN_NO="No. Why would I write it down?",CASEY_PLAN_YES="In my bag. In the bedroom. Take it. It's fine.";
    public static final String CASEY_WANTS_PLAN="Is that my— give me that. Please. That's private.",GIVE_PLAN="Give it to him.",KEEP_PLAN="Not yet.",CASEY_GOT_PLAN="Thank you. It's nothing. It's just a list.";
    /** What a reader can tell Casey, and what he says to each before it adds up. */
    public enum Tell {
        PRINTS("There are wet footprints from the lake up to the back door.","Somebody went swimming. People swim."),
        BLOOD("There's blood on the dock. Real blood.","Jude's got stage blood in his bag. Hasn't he?"),
        PLAN("Your plan puts a midnight scare on the end of the dock. Jude was standing right in front of you when someone walked out there.","That was Jude's bit. Jude was with me. Jude was with me."),
        STRAP("Someone cut their own strap for that mask. Jude ties his.","Why would anyone cut a—"),
        COSTUME("The mask and the coverall are in the rowboat, under the tarp.","Then who was wearing it?"),
        TAPE("The player was running by the back door. Someone went out, and came back in wet, while Nina was in the bathroom.","Nina was in the bathroom. She said so.");
        public final String line,answer;
        Tell(String line,String answer){this.line=line;this.answer=answer;}
    }
    public static final String CASEY_TURNS="Okay. Okay. That's not a bit. That's not anything I did. I didn't hire anyone else. I swear to you.";
    public static final String CASEY_BURN_CUE="Casey crouches at the fire and feeds a folded sheet of paper into it, and watches it go.",CASEY_BAG_EMPTY_CUE="Casey looks in his bag, then round the room, and doesn't say anything.";
    // Len, through the boathouse door.
    public static final String LEN_DOOR="Who's there? Go back to the cabin. Lock the doors. And nobody touches anything down here.";
    public static final String LEN_Q_IN="Let me in.",LEN_IN="No. Nobody comes in. Not tonight.";
    public static final String LEN_Q_WHO="Who would want to hurt you?",LEN_WHO="Nobody. Kids. I don't know. Go back up.";
    public static final String LEN_Q_PRINTS="Somebody came up out of the water right by here.",LEN_PRINTS="Then she's been round the back.",LEN_SLIP="I didn't say she. I didn't say anything.";
    public static final String LEN_Q_SAFE="What's in the safe?",LEN_SAFE="Nothing that's yours.";
    public static final String LEN_Q_WATCH="We'll keep watch.",LEN_WATCH="All right. Thank you. Don't come in.";
    // Nina, in the night.
    public static final String NINA_NIGHT_WOUNDED="Is Jude okay? What happened out there?",NINA_NIGHT_PREVENTED="Can we put the film back on. Please.";
    public static final String NINA_Q_OUT="Did you go outside tonight?",NINA_OUT="No. I was in the bathroom when it happened. I didn't hear a thing.";
    public static final String NINA_Q_WET="Somebody came in the back door soaking wet.",NINA_WET="Then somebody else went swimming.";
    public static final String NINA_Q_BOAT="Jude saw you trying the boathouse door.",NINA_BOAT="I was looking for the toilet. It isn't my place.";
    /** Casey's plan, in his capitals. */
    public static final List<String> PLAN=List.of("THE PLAN (DON'T LET ANYONE SEE THIS)",
        "1. After dark. Breaker by the back door, main switch DOWN. Say it's the wiring.",
        "2. J at the front window. Three knocks. Not too long.",
        "3. J round the back. Rattle the back door. DON'T open it.",
        "4. J to the shed. Change. Back in normal clothes. Say he went to look at the lake.",
        "5. MIDNIGHT. J on the end of the dock in the mask. Just standing. Torch on him from the porch. Big finish.",
        "Mask, coverall, rubber knife in J's bag. Stage blood for no. 5 only.");
    /** What the portable player hears, set down and recording. */
    public static String heard(String key){return switch(key){
        case "knock"->"Three knocks on glass.";
        case "rattle"->"A door handle, rattled hard.";
        case "power_off"->"A heavy click. Everything that was humming stops.";
        case "power_on"->"A click, and the fridge starts up again.";
        case "crash"->"Far off, something heavy going over.";
        case "glass"->"Glass breaking, a long way off.";
        case "splash"->"Water. Something going into it, not far.";
        case "back_door"->"The back door opening, and shutting.";
        case "nina_in"->"The back door again, slowly. Someone breathing hard. Water dripping on the boards.";
        case "music"->"Music, through a wall.";
        default->"Hiss.";
    };}

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
        if(own.getBoolean("CaseyHushed"))lines.add("When Casey kept talking over him I told him to let Jude talk.");
        if(own.getBoolean("StrapMismatch"))lines.add("Jude knots his mask on. The strap from behind the shed had a buckle at each end.");
        if(own.getBoolean("HeardCaseyToldNina"))lines.add("Casey said he'd told Nina what was in Jude's bag.");
        if(own.getBoolean("SawWetPrints"))lines.add("There were wet footprints from the lake to the back step.");
        if(own.getBoolean("FoundCostume"))lines.add("The mask and the coverall were in Len's rowboat, under the tarp.");
        if(own.getBoolean("ReadPlan"))lines.add(own.getBoolean("BurnedPlan")?"I read Casey's plan, and then I burned it.":"I read Casey's plan. The dock was meant for midnight.");
        else if(own.getBoolean("SawCaseyBurn"))lines.add("I watched Casey put a sheet of paper in the fire.");
        if(own.getBoolean("TapeNina"))lines.add("On the tape the back door opened, and a long time later someone came back in, dripping.");
        if(own.getBoolean("LenSlipped"))lines.add("Through the boathouse door Len said she'd been round the back, and then said he hadn't said she.");
        if(own.getBoolean("TurnedCasey"))lines.add("I told Casey enough that he stopped saying it was a bit.");
        lines.add("That was the first night.");
        return book("The account: the weekend at Pinehook",name,List.of(String.join(" ",lines)));
    }
    static String join(List<String> parts){if(parts.size()==1)return parts.get(0);return String.join(", ",parts.subList(0,parts.size()-1))+" and "+parts.get(parts.size()-1);}
}
