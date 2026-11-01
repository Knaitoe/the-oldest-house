package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.209, the closing hall: two hands. Daisy's drawing, with her brother's printing under it (the source); and Karen's interview,
 * given long afterwards, about how long the hallway was and who came out of it last (the account). The children's own words in
 * the hall are here too. All of it is original. House writing: never the reader's gender, never a colour of theirs, never an age;
 * the reader is named, or "you", never "he" or "she".
 */
public final class ClosingHallTexts {
    private ClosingHallTexts(){}
    private static final int PAGE=150;
    static List<Component> plain(String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}

    /** Daisy's drawing, on the floor between the two of them. */
    public static ItemStack source(){return HouseWriting.book("A drawing in crayon","Daisy, and her brother",plain("""
        Drawing paper, folded in four and opened out again, done in crayon pressed so hard it has torn in two places.

        A hallway: two long lines that meet at the far edge of the paper. At the near end, two small people holding hands, one a head taller. Printed beside them, in big letters that lean on each other:

        THE HALL IS LONGR EVRY TIME

        Under that, in neater capitals, slower:

        WE ARE STAYING RIGHT HERE LIKE DADDY SAID. DAISY STOP CRYING. HE IS COMING BACK.

        Along the bottom of the sheet the hallway's two lines have been drawn again, a little closer together, and again, closer, and again, until the crayon has gone through the paper.""".stripIndent()));}

    public static String accountText(String name,CompoundTag own){
        String outcome=own.getString("Outcome");boolean back=outcome.equals("went_back");
        String earlier=own.getString("LeftBehind209");
        var t=new StringBuilder();
        t.append("Transcript. The tape was made a long time afterwards, in a kitchen; you can hear a kettle. The interviewer's questions are not on it.\n\n");
        t.append("KAREN: How long was it? People always ask me that, as though there's a number. Will measured it once and got one number and measured it again and got another. I'll tell you how long it was. It was long enough that I couldn't hear them crying any more from the door, and I could hear them from anywhere in that house.\n\n");
        t.append("KAREN: They'd been told to stay put. That's the thing that I can't get past. They did exactly what they were told. They stood at the end of it and held hands and waited, and the hallway waited with them.\n\n");
        t.append("KAREN: And then somebody went in after them who wasn't us. ").append(name).append(". I didn't know the name then. I know it now. I've said it a lot.\n\n");
        if(!earlier.isEmpty())t.append("KAREN: Not the first time. I want that on the tape. The first time, ").append(earlier).append(" didn't come out, and the wall called for ").append(name).append(" until there was nobody left to hear it. And still ").append(name).append(" came back.\n\n");
        t.append("KAREN: The walls came in. Not all at once. A step, and then nothing, and then a step. Chad said afterwards it sounded like something breathing underneath, every time. And it would stop, right up against Daisy, a hand's width off her, and wait. It never touched them. It waited for them. I think that's worse. I think it wanted somebody to see it wait.\n\n");
        t.append("KAREN: There was a hole low in the wall near the door. Chad says ").append(name).append(" put them through it first. Daisy, then him. Or him, then Daisy; they don't agree, they've never agreed about anything. They agree about that part. First. Before. ").append(name).append(" didn't go in ahead of them.\n\n");
        if(back){String who=own.getString("WentBack209").isEmpty()?"Daisy":own.getString("WentBack209");String other=who.equals("Chad")?"Daisy":"Chad";String them=who.equals("Chad")?"him":"her";
            t.append("KAREN: You want to know who came out last. Nobody came out last. ").append(other).append(" was through and turned round in the dark, and says the hall was almost shut, a crack, and ").append(name).append(" had gone back for ").append(who).append(". Pushed ").append(them).append(" through. ").append(other).append(" heard the hall close, and says it was very quiet when it did. I asked how quiet. The quietest thing there ever was.\n\n");}
        else t.append("KAREN: You want to know who came out last. ").append(name).append(" did. On their stomach, through that hole, with the stone coming down behind. Chad says he heard the hall shut while ").append(name).append("'s feet were still in it.\n\n");
        t.append("KAREN: The children ran. Into the dark, the way you'd come. I could hear them from the door, getting further away, calling each other, for longer than there was house for them to run in. And then I couldn't.\n\n");
        t.append("KAREN: They came home. I'm told that's the end of it.\n\n");
        t.append("(The tape runs on a while with nobody speaking. Then the kettle.)");
        return t.toString();
    }
    public static ItemStack account(String name,CompoundTag own){return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.CLOSING_HALL),name,plain(accountText(name,own)));}
    public static final String HINT="They only come while you hold them. In the squeeze, a child has to go first.";

    // ------------------------------------------------------------------------------------------------ the children
    static final String[] WAITING={"Chad: \"Don't step on her picture.\"","Daisy: \"Is Daddy with you?\"","Chad: \"We're not supposed to move. We're staying right here.\"","Daisy, very small: \"It got longer again.\""};
    static final String[] START={"Chad: \"It's doing it again. It's doing it again!\"","Daisy is crying, the hiccupping kind, and won't stop."};
    static String hand(String who){return who.equals("Chad")?"Chad takes your hand hard enough to hurt. \"Okay. Okay.\"":"Daisy takes your hand in both of hers. \"Don't let go.\"";}
    static String back(String who){return who.equals("Chad")?"Chad climbs onto your back. \"I'm too big to be carried,\" he says, and holds on.":"Daisy climbs onto your back and puts her face in your neck. \"I can't run any more.\"";}
    static String squeeze(String who){return who.equals("Chad")?"Chad looks at the hole. \"I'll go first. Daisy, you come right after me.\" He goes in on his stomach.":"Daisy looks at the hole and then at you, and goes in, because you asked her to.";}
    static String refuse(String who){return who.equals("Chad")?"Chad lets go of you at the hole. \"Not after you. Not in there.\" He won't follow you in.":"Daisy lets go. She sits down where she is and won't follow you into the dark.";}
    static String waiting(String who){return "The wall stops a hand's width from "+who+", and waits.";}
    static String calling(String who,String name){return "From inside the wall, "+who+" is calling: \""+name+"? "+name+"!\"";}
    static String wentBack(String who){return "You go back for "+who+". The hall is a crack, and closing.";}
}
