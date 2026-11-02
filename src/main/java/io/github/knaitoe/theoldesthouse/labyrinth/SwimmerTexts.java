package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.227, the last swimmer. The closing checklist on the reception counter (the source), what the attendant says, and the closing
 * log handed over at the fire door (the account). All of it is original. It is played against Armand Hammer and The Alchemist's
 * "Falling Out the Sky" (2021), on the radio in the office while the pool closes: grief that never got cried, a ride with no reverse,
 * dead stars, a summer that ends on the road home, a daydream that comes apart, and a pool where someone went under the summer before
 * and voices in the deep end (0.4.233). None of the song's words are used here, and the song is not shipped (see
 * docs/LAST_SWIMMER_0_4_227.md).
 * House writing: never the reader's gender, never a colour of theirs, never an age; the reader is "you", or named. The attendant
 * and the swimmer are never given a gender either.
 */
public final class SwimmerTexts {
    private SwimmerTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    public static final String HINT="Two to pull, from the reel side. Whoever's still in at closing ends up in lane one.";
    /** 0.4.233: the closing against the song on the radio. */
    public static final String NO_REVERSE="The crank only turns the one way.";
    public static final String RADIO="A radio, turned low. Voices over a slow, warped loop, more talk than tune. Someone on it is talking about the stars.";
    public static final String RADIO_ENDED="The radio has gone to a hiss between stations.";
    public static final String STARS_OUT="The cover has taken the last of the sky off the water.";
    public static final String WAVER="All down the hall the lights waver, one bank and then another, and steady.";
    public static final String BREATH="The splashing stops. Nothing comes up. Then it goes on, further down the lane, as if it hadn't.";
    public static final String VOICES="Under the water at the deep end: voices. You can't make out what they're saying.";

    /** The checklist on the counter: the printed card, and what's been written under it over the years. */
    public static ItemStack source(){
        var pages=new ArrayList<Component>();
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,
            "CLOSING, WEEKNIGHTS\n\n"
            +"1. Call last swim half an hour before.\n2. Clear the pool.\n3. Cover on. Two to pull, from the reel side.\n4. Hall lights off, bank by bank.\n5. Out by the fire door. Check it shuts.\n\n"
            +"NEVER put the cover on with anyone in the water."));
        pages.addAll(styled(HouseWriting.WritingStyle.KAREN,
            "Under the card, in biro, in a hand that isn't the printer's:\n\n"
            +"If there's still someone in at closing, don't call them out. Don't wait for them to get out. They won't. They haven't since last summer.\n\n"
            +"Cover on, lights off, out by the fire door. Hold it for whoever helped you."));
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,
            "Under that, other hands, one after another, years of them: initials, and a tick.\n\n"
            +"Nobody has crossed out the printed line."));
        return HouseWriting.book("Closing checklist","the front desk",pages);
    }

    /** The attendant, as each lane goes under. */
    public static String haul(int k){
        return switch(k){
            case 1->"\"That's it. Together.\"";
            case 3->"\"Don't mind them. They always stay till the cover's on.\"";
            case 6->"\"Every night.\"";
            case 9->"\"Keep it straight. It snags on the ropes.\"";
            case 12->"\"Nearly.\"";
            case 15->"\"Keep going.\"";
            case 16->"\"All the way. It's all right.\"";
            default->null;
        };
    }

    /** The closing log, handed over at the fire door: tonight's line, and every line before it. */
    public static String log(String reader,CompoundTag own){
        var t=new StringBuilder("A page from the closing log in the office, ruled in columns: DATE, LAST SWIM, COVER ON, LIGHTS, IN AT CLOSE, INITIALS.\n\n");
        t.append("Every line, as far back as the page goes, says the same in the fourth column: 1.\n\n");
        t.append("The page goes back to the start of last summer. On its first line the fourth column was filled in, scored through, and filled in again: 1.\n\n");
        t.append("Tonight's line is filled in in the attendant's hand, all but the initials. Two sets go there. The first is written in. The second space is yours.");
        return t.toString();
    }
    public static String afterwards(String reader,CompoundTag own){
        var t=new StringBuilder();
        t.append("They were closing the pool when I came in. There was a radio on in the office, turned low: voices over a slow loop, more talk than tune, and it kept on all through.\n\n");
        t.append("The attendant asked me to take the other end of the cover.\n\n");
        t.append("Someone was still swimming. I heard every turn against the tiles before I saw anything: a pale shape under the water, there and then not. ");
        t.append(own.getBoolean("Clock227")?"I watched the pace clock. Each length came round on the quarter, exactly, every time.\n\n":"Each length took exactly as long as the one before.\n\n");
        t.append("We pulled the cover out a lane at a time. Each time it reached the lane they were in, the splashing was a lane further over at their next turn. They never once came up for air that I saw. ");
        t.append("Under the skylight there were stars on the water, and the cover put them out as it went.\n\n");
        if(own.getBoolean("Breath227"))t.append("Once the splashing stopped altogether, long enough that I thought it was over. Then it went on, further down, as if it hadn't.\n\n");
        if(own.getBoolean("Voices227"))t.append("I went under at the deep end, once. There were voices down there. I couldn't make out a word.\n\n");
        t.append("At the end there was one lane left, and the attendant said all the way, and I pulled.\n\n");
        t.append("The splashing stopped. The lights went off, one bank at a time. Then, from the far side, the cover rose and fell, one step at a time, coming toward me from underneath. ");
        t.append(own.getBoolean("Pressed227")?"I was standing on it when one came up under my feet.\n\n":"Wet, heavy steps, and not hurried.\n\n");
        t.append("The attendant was already at the fire door, holding it open. Neither of us said anything. I went through, and it shut behind me.\n\n");
        t.append(own.getBoolean("Read_Source")?"The card on the counter says never to put the cover on with anyone in the water. Under it, in other people's writing, it says to do it anyway, and to hold the door for whoever helped.":"I never read the card on the counter. I don't think it would have changed what we did.");
        return t.toString();
    }
    public static ItemStack account(String reader,CompoundTag own){
        var pages=new ArrayList<Component>();
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,log(reader,own)));
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,afterwards(reader,own)));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LAST_SWIMMER),reader,pages);
    }

    // 0.4.244: the pool coming back, as the reader left it (docs/AFTERMATH_0_4_244.md). Nothing to do here; nothing to earn.
    public static final String AFTERMATH="The hall is dark but for the two lamps by the fire door. The cover is on, wall to wall, and nothing moves under it.";
    public static final String AFTER_COVER="Taut from wall to wall. Nothing presses up under it.";
    public static final String AFTER_CLOCK="The sweep hand has stopped.";
    public static final String AFTER_RADIO="Switched off.";
    public static final String AFTER_LOCKER="The open locker. The towel is still folded square, and the shoes are still dry.";
}
