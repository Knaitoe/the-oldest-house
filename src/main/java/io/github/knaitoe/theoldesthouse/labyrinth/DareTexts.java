package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.173, "Third Floor Girls'": the folded notebook square the owner wrote, kept word for word in a child's hand in purple
 * gel pen, and the account of the dare, built from what the reader did.
 */
public final class DareTexts {
    private DareTexts(){}
    public static final String TITLE="Third Floor Girls’";
    /** The owner's note, exactly as supplied. */
    public static final String NOTE="""
        You have to close the door all the way until the latch clicks, or it doesn’t count. And you can’t look at the sink or your shoes—you have to look right at your own eyes in the glass the whole time you say it, or we’re making you go back in.

        Everybody knows nothing actually happens on the first two. That’s just your eyes playing tricks on you because the candle’s too low, the way your own nose starts looking like belongs to somebody else if you don’t blink for a minute.

        It’s only when you get to the third one and you hear your friends stop laughing on the other side of the wood that you realize how heavy a bathroom door is when somebody’s holding the handle from the hall.""";
    /** Purple gel pen. */
    static final int GEL=0x6a2fa0;
    /** The child's hand is wide: a native page holds about this many of its characters. */
    private static final int CHILD_PAGE=110,PAGE=145;
    /** Splits each paragraph at word boundaries into pages of at most {@code limit} characters, never changing a word. */
    static List<String> pages(String text,int limit){
        var out=new ArrayList<String>();
        for(String paragraph:text.strip().split("\\n\\s*\\n")){
            String rest=paragraph.strip().replaceAll("[ \\t\\r\\n]+"," ");
            while(rest.length()>limit){int at=rest.lastIndexOf(' ',limit);if(at<1)at=limit;out.add(rest.substring(0,at));rest=rest.substring(at).stripLeading();}
            if(!rest.isEmpty())out.add(rest);
        }
        return List.copyOf(out);
    }
    public static ItemStack source(){
        var pages=new ArrayList<Component>();
        for(String page:pages(NOTE,CHILD_PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.CHILD,page).copy().withStyle(s->s.withColor(TextColor.fromRgb(GEL))));
        return HouseWriting.book("A folded notebook square","Third floor",pages);
    }
    private static String times(int n){return switch(n){case 1->"once";case 2->"twice";case 3->"three times";case 4->"four times";case 5->"five times";default->"more times than I counted";};}
    /** What the reader did, in order: the note if read, the latch, every count lost, the three counts, the handle and the glass. */
    public static String account(CompoundTag own){
        var b=new StringBuilder();
        b.append(own.getBoolean("Read_Source")?"The note said nothing happens on the first two.":"Nobody told me what would happen. They only said to shut it all the way.").append("\n\n");
        b.append("I shut the door until the latch clicked, and the light from the hall went with it. I stood on the mat with the candle and looked at my own eyes.\n\n");
        int lost=own.getInt(DareInTheDark.LOST);
        if(lost>0)b.append("I lost the count and started again ").append(times(lost)).append(own.getBoolean(DareInTheDark.OPENED_EARLY)?". Once I opened the door, and they said it didn't count.":".").append("\n\n");
        b.append("On the first, my eyes moved in the glass. On the second, nobody laughed outside any more, and in the mirror the stall behind me stood open a little. In the room it was shut.\n\n");
        b.append("On the third, the candle was gone from my reflection's hand, and its arms hung at its sides. A second flame stood behind its ear. A woman stood behind it with her chin on its shoulder and her fingers on its collarbones, looking at me.\n\n");
        b.append("The handle would not turn. Someone breathed against the back of my neck. Then the door came open all at once, and the hall was empty except for two sheets on the floor.\n\n");
        b.append("The glass broke behind me as I went through. I did not turn round.");
        return b.toString();
    }
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.DARE_IN_THE_DARK),name,HouseWriting.WritingStyle.WILL,pages(account(own),PAGE));
    }
}
