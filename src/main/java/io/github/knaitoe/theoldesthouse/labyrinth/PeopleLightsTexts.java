package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.253, After the People Lights Have Gone Off (docs/PEOPLE_LIGHTS_0_4_253.md): Stan's offer (the only paper anyone has to read), the
 * reader's account of the evening, and the lines around it. All of it is new writing after the title story of Stephen Graham Jones's
 * collection; nothing is quoted from it. The account says only what the reader was there for, and says so when it is Mark's word
 * instead; it never names a cause.
 */
public final class PeopleLightsTexts {
    private PeopleLightsTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    public static final String HINT="Stay for supper. The house has two floors, and they're not the same.";
    public static final String AFTER="The porch light is off. Nobody laughs inside. You know the house well enough not to knock.";
    public static String stanGreeting(){return "Evening. Don't get up. Nobody get up.";}

    /** Stan's offer, folded once, left on the kitchen table: plain, fair, and in his hand. */
    public static ItemStack source(){
        var hand=HouseWriting.WritingStyle.WILL;var pages=new ArrayList<Component>();
        pages.add(HouseWriting.page(hand,"Kel, Mark.\n\nI'll buy the house back off you at what you paid, plus what you've put in. Cash. No inspection. I know what's in the walls."));
        pages.add(HouseWriting.page(hand,"I've got a place in town on one floor. Ramp's already in. You can have it as long as you want it, rent or no rent, your call."));
        pages.add(HouseWriting.page(hand,"I'll do the moving. I'll do the boxes. You don't have to come back here for anything.\n\nPhone any time. I'll answer.\n\nDad"));
        pages.add(HouseWriting.page(hand,"Under that, smaller, crossed out but still readable:\n\nI should have put the rail up first."));
        return HouseWriting.book("Stan's offer","Stan",pages);
    }

    /** What the reader sees in the corner of their screen when it is over. */
    public static String ended(String outcome){
        return switch(outcome){
            case "left_together"->"Stan's tail lights at the end of the drive. Behind you, the house with its lights still on and nobody in it.";
            case "shared_the_downstairs"->"The two of them at the kitchen table, his case open, her cards. Neither looks up when you go. That's the point.";
            case "gave_them_the_night"->"You let yourself out through the garage. The bedroom door stays shut. Whatever happens in there is theirs.";
            default->"They're holding hands across the gap between the bed and the chair. You let yourself out.";
        };
    }

    /** The account, in the reader's own words: only what they were there for. */
    public static String afterwards(String reader,CompoundTag own){
        var t=new StringBuilder();String outcome=own.getString("Outcome");
        t.append("I was a guest for supper at a narrow, tall house on a lot everybody said should have stayed empty. Kelly lived downstairs, in her chair. Mark worked upstairs, over a rail.\n\n");
        if(own.getBoolean("AccidentTold253"))t.append("They told me about the loft: the first night, the air mattress, before there was a rail. She remembered floating.\n\n");
        if(own.getBoolean("Redirected253"))t.append("Her father came by with an offer and talked to Mark about her until I asked him to talk to her. He did.\n\n");
        else if(own.getBoolean("ReadOffer253"))t.append("Her father left an offer on the kitchen table. Fair. In his own hand.\n\n");
        var did=new ArrayList<String>();
        if(own.getBoolean("Mirror253"))did.add("I lowered the bathroom mirror to where she could see herself");
        if(own.getBoolean("CaseDown253"))did.add("I carried Mark's work case down to the kitchen table");
        if("rolled".equals(own.getString("Runner253")))did.add("I rolled up the hall runner, because he wanted to hear her, after asking them both");
        if("kept".equals(own.getString("Runner253")))did.add("I left the hall runner down, because she wanted the quiet, after asking them both");
        if(own.getBoolean("Cushion253"))did.add("I passed her a cushion");
        if(!did.isEmpty())t.append(String.join(". ",did)).append(".\n\n");
        if(own.getBoolean("HeardOverhead253"))t.append("While Mark was at his desk, someone walked across the floor above the study.\n\n");
        if(own.getBoolean("SawLikeness253"))t.append("While Kelly was downstairs, I saw a standing figure at the loft rail. It looked like her. That isn't the same as it being her.\n\n");
        if(own.getBoolean("SawRound253"))t.append("In the night the chair went quiet and she went round the hall and the newel post on the floor, asleep, again and again.\n\n");
        else if(own.getBoolean("SawWake253"))t.append("In the night she was on the floor, asleep. Mark told me she goes round the hall like that.\n\n");
        if(own.getBoolean("WokeHer253"))t.append("We kept saying her name until she woke. She didn't remember the route.\n\n");
        else if(own.getBoolean("SawWake253"))t.append("She woke. She didn't remember the route.\n\n");
        if(own.getBoolean("AskBefore253"))t.append("Mark told me it wasn't the first time. He had a bandage across his knuckles.\n\n");
        switch(outcome){
            case "left_together"->t.append("They called Stan together, from the kitchen. She took the photograph from the foyer and he took his case, and they went out through the garage to her father on the drive. I don't know what they'll say on the way.");
            case "shared_the_downstairs"->t.append("They decided to stay, for now, but not the way they had been. "+changed(own)+" Mark brought his work down. They were playing cards when I left.");
            case "gave_them_the_night"->t.append("They wanted one more night, and to call Stan in the morning. They shared the bedroom and shut the door. I gave them the night and left. I don't know what happened in there.");
            default->{
                t.append("They wanted one more night, and to call Stan in the morning. They shared the bedroom and shut the door. I stayed.\n\n");
                t.append("In the morning Mark said his legs were numb. Kelly's foot moved under the blanket. ");
                if(own.getBoolean("BroughtChair253"))t.append("I brought the spare chair down from upstairs for him. ");
                t.append("They held hands.");
            }
        }
        if(own.getBoolean("CalledThroughDoor253"))t.append("\n\nOnce, in the night, I called through their door, and they came out and we talked again.");
        if(own.getBoolean("Uncertain253"))t.append("\n\nI told them I couldn't say what the house was doing. Nobody could.");
        return t.toString();
    }
    static String changed(CompoundTag own){return "She chose the change that mattered tonight.";}
    public static ItemStack account(String reader,CompoundTag own){
        var pages=new ArrayList<Component>(styled(HouseWriting.WritingStyle.PLAIN,afterwards(reader,own)));
        pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,"After \"After the People Lights Have Gone Off,\" by Stephen Graham Jones."));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.PEOPLE_LIGHTS),reader,pages);
    }
}
