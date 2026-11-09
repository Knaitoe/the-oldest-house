package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.217, Polybius. The notice board by the door (the source: a clipping from the local paper, and a card under it), the men's
 * clipboard (the account), and the lines in the arcade. All of it is original, after the urban legend of an unbranded arcade
 * cabinet, men in suits who came to read its scores, and the children who stopped playing. House writing: never the reader's gender,
 * never a colour of theirs, never an age; the reader is "you", or named.
 */
public final class PolybiusTexts {
    private PolybiusTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    /** A clipping pinned to the cork by the door, gone yellow, and an index card under it in marker. */
    public static ItemStack source(){
        var pages=new ArrayList<Component>();
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,
            "A clipping from the local paper, pinned to the cork by the door. The column has gone the colour of weak tea.\n\n"
            +"GAME TAKEN OUT OF MALL ARCADE\n\n"
            +"A coin-operated video game was removed from the arcade at the end of the strip mall on Tuesday, after two boys who had played it were taken ill the same week. One had headaches and could not sleep. The other has not been able to say what he did on the evening he played it.\n\n"
            +"The machine carried no maker's name. The owner told this paper it had been installed in the spring by a company whose name he could not now find on any paperwork, and that two men in suits had come by every week to read its scores.\n\n"
            +"\"They never played it,\" he said. \"They wrote things down. I thought it was market research.\"\n\n"
            +"The men collected the cabinet on Tuesday evening. Nobody has seen it since."));
        pages.addAll(styled(HouseWriting.WritingStyle.KAREN,
            "And under the clipping, an index card, in marker:\n\nFREE PLAY TONIGHT\n\nhigh scores stay up\n\nif the black one is back DON'T"));
        return HouseWriting.book("A clipping on the notice board","the arcade",pages);
    }
    public static final String HINT="Every table is the whole server's. The black cabinet keeps no table. Three times is enough; it will ask for a fourth.";

    /** The men's clipboard, left on the change machine: their form, and then the reader's own words. */
    public static String form(String name,CompoundTag own){
        var t=new StringBuilder("A clipboard, a carbon form, typed. No company name at the top: only a number.\n\n");
        t.append("SUBJECT: ").append(name).append("\nSESSIONS: ").append(Math.max(PolybiusSite.BLACK,own.getInt("Plays217"))).append("\nRESPONSE: ");
        t.append(own.getInt("Taken217")>0?"stopped (second evening)":"stopped");
        t.append("\n\nOTHER MACHINES:\n");
        boolean any=false;
        for(int g=0;g<PolybiusSite.BLACK;g++){int best=own.getInt("Best217_"+g),rank=own.getInt("Rank217_"+g);if(best<=0)continue;any=true;
            t.append(PolybiusScores.NAMES[g]).append("  ").append(best).append(rank>0?"  (table, "+Polybius.ordinal(rank)+")":"").append('\n');}
        if(!any)t.append("none played\n");
        t.append("\nAt the bottom, in pen, in a hand that presses too hard: no further visits required.");
        // 0.4.413: the key the reader is given with the account (ServiceKeyItem).
        t.append("\n\nTaped under the clip, a small round key on a ring. Its paper tag says FREE PLAY.");
        return t.toString();
    }
    public static String afterwards(String name,CompoundTag own){
        var t=new StringBuilder();
        int games=own.getInt("Games217");
        t.append(games==0?"I didn't play anything else. I went straight to the back, to the black one, as if I'd come for it.\n\n"
            :games==1?"I played one of the others first, once, to have something to do with my hands.\n\n"
            :"I played the others first. My name went up on their tables, and I liked seeing it there, and I think that was the point.\n\n");
        t.append("The black one had no name on it and no table. It had a spiral that turned, and a dot, and words that came up in the middle too quickly to read, except that one of them was mine. The two men wrote something down every time I finished. They never looked at the screen. They looked at me.\n\n");
        if(own.getInt("Taken217")>0)t.append("The first night I played it a fourth time. I don't remember the fourth time. I remember someone putting the stools up, and my name gone from every table, as if I'd never been there.\n\n");
        t.append("The third time, it asked for me by name. I walked to the front of the arcade with it saying my name behind me, and I didn't turn round until it stopped.\n\n");
        t.append("I don't know what they were measuring. I know I stopped.");
        return t.toString();
    }
    public static ItemStack account(String name,CompoundTag own){
        var pages=new ArrayList<Component>();
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,form(name,own)));
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,afterwards(name,own)));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.POLYBIUS),name,pages);
    }
}
