package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.213, your own light. No papers: a few words scratched into the lobby wall (the source), the note left on the reader's pillow
 * in their own hand, and the account, the reader's own, about the light they left behind, with the note pasted in. All of it is
 * original. House writing: never the reader's gender, never a colour of theirs, never an age; the reader is "you", or named.
 */
public final class OwnLightTexts {
    private OwnLightTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    /** Scratched into the ash at the height of a hand, with something small and hard. */
    public static ItemStack source(){return HouseWriting.book("Words scratched into the wall","whoever came before",styled(HouseWriting.WritingStyle.PLAIN,
        "Scratched into the ash at the height of a hand, with a key or a coin, the letters going over and over each other where whoever made them had to go back and make them deeper:\n\n"
        +"BRING YOUR OWN LIGHT\n\n"
        +"And under it, smaller, more carefully, as if the first line had been a mistake:\n\n"
        +"it will bring its own"));}
    /** The note on the pillow, in the reader's own hand. */
    public static ItemStack note(String name,String text){return HouseWriting.book("A note on your pillow",name,List.of(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,"In your own handwriting, on paper from your own house:\n\n"+text)));}

    public static String accountText(String name,CompoundTag own){
        var t=new StringBuilder();int placed=own.getInt("Placed213"),taken=own.getInt("Taken213"),set=own.getInt("SetBack213");String note=own.getString("Note213");
        t.append("There were two halls, side by side, and someone was walking the other one. I saw them through the gaps in the wall. They had a torch. They had my walk. When I stopped, they stopped. When I looked across at them, they had already turned their head to look at me.\n\n");
        t.append(placed==0?"I hadn't brought any light. They had. I followed theirs, through the gaps, for as long as it lasted.\n\n"
            :"I set down "+(placed==1?"one torch":Stillness.words(placed)+" torches")+" on my side. Every time, a beat later, a torch went down on theirs, across from mine. Then it stopped being a beat later.\n\n");
        t.append("Halfway, they stopped matching me. They came through a gap behind me, into my hall, and walked back the way I'd come.");
        if(own.getBoolean("Followed213"))t.append(" I went after them. I don't know what I thought I'd do. Every light in the place went out at once, and I was back at the fork with nothing but what was in my hands.\n\n");
        else t.append(taken>0?" I heard my torches go out behind me, one at a time, with that low sound the House makes. I didn't look back.\n\n":" I didn't look back.\n\n");
        if(set>0)t.append("At the end, in the far wall, there were my torches. All of them. In the order I'd put them down. Burning. As if someone had carried them there for me, or as if I had.\n\n");
        t.append("It went home before I did. I know because of the note. It was on my pillow, in my handwriting, on my paper:\n\n");
        t.append(note.isEmpty()?"(I haven't been home yet.)":"   "+note);
        if(note.equals("Not yet."))t.append("\n\nI keep thinking about what it is that hasn't happened yet.");
        else if(!note.isEmpty())t.append("\n\nIt was polite. That's the part I can't stop thinking about.");
        return t.toString();
    }
    public static ItemStack account(String name,CompoundTag own){return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.OWN_LIGHT),name,styled(HouseWriting.WritingStyle.PLAIN,accountText(name,own)));}
    public static final String HINT="Someone walks the other branch with your walk and your light. When they turn back, don't follow.";
}
