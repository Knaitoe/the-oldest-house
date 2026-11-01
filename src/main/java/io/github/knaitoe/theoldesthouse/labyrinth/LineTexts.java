package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.212, the line: a tag tied to the line where it is knotted round the doorframe (the source), and the tape on the recorder at
 * the far end: a transcript in the reader's own handwriting, dated days from now, describing this room, the last entry describing
 * the reader finding the pack (the account). All of it is original. House writing: never the reader's gender, never a colour of
 * theirs, never an age; the reader is "you", or named.
 */
public final class LineTexts {
    private LineTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    /** The tag on the knot: whoever tied the line, and what they told whoever came after. */
    public static ItemStack source(){return HouseWriting.book("A tag tied to the line","nobody signed it",styled(HouseWriting.WritingStyle.PLAIN,
        "A luggage tag, knotted onto the line where it is tied round the doorframe. Both sides written on, small, in pencil.\n\n"
        +"RULES FOR THE LINE\n\n"
        +"1. Tie off at the door. Keep one hand on it. Always one hand.\n\n"
        +"2. If you lose it, don't go looking for it out there. Come back the way you came. It will be where you left it. It always is.\n\n"
        +"3. Don't run. You can't run and hold it.\n\n"
        +"4. If it pulls, it's me.\n\n"
        +"(On the back, in the same pencil, pressed harder:)\n\n"
        +"If it pulls twice, it isn't.\n\n"
        +"If it pulls and doesn't stop, let go."));}

    /** The day the tape is dated: some days after the day the reader found it. */
    static long dated(CompoundTag own){return own.getLong("Found212")+4;}
    public static String accountText(String name,CompoundTag own){
        var t=new StringBuilder();long day=dated(own);int reeled=own.getInt("Reeled212"),letGo=own.getInt("LetGo212"),dragged=own.getInt("Dragged212");String gone=own.getString("Gone212");
        t.append("A cassette, wound to the end. Someone has transcribed it onto the inside of the pack's flap, in pencil, in a hand you know better than any other. Yours. Every entry is dated. Every date is days from now.\n\n");
        t.append("DAY "+day+". Tied off at the frame. Can't see a thing past it, not the floor, not my hand. The line goes out into it at about the height you'd hold a railing. Following it.\n\n");
        t.append("DAY "+day+", later. Columns out here. You walk into them before you know they're there. The line goes round them. Whoever laid it knew where they were.\n\n");
        if(letGo>0)t.append("DAY "+day+", later. Lost it"+(letGo==1?" once":" "+Stillness.words(letGo)+" times")+". The dark when you haven't got it isn't like any dark I know. You go back the way you came, like the tag says. It was there.\n\n");
        t.append("DAY "+day+", later. It tugged. From behind. I thought it was "+(own.getBoolean("Warned212")?"the others":"the knot slipping")+". Then it tugged again. Then it didn't stop.\n\n");
        if(dragged>0)t.append("DAY "+day+", later. It was close enough to drag me. I went over on the floor and it pulled me back along it on my knees and I held on because the tag says hold on, and then I remembered the back of the tag.\n\n");
        if(reeled>0)t.append("DAY "+day+", later. It got me"+(reeled==1?" once":" "+Stillness.words(reeled)+" times")+". The line went slack and there wasn't any floor. I woke up doors back with it round my arm so tight I couldn't feel my hand."+(gone.isEmpty()?"":" My "+gone.toLowerCase(Locale.ROOT)+" was gone.")+"\n\n");
        t.append("DAY "+(day+1)+". Walked. Didn't stop. Every time I stopped it came, so I didn't stop.\n\n");
        t.append("DAY "+(day+1)+", later. There's something up ahead. It's a pack. The line's tied to the strap. There's a tape recorder on it. I'm going to pick it up. I'm going to press play and I already know whose voice it is.\n\n");
        t.append("That is the last entry. There is room on the flap for more, and the pencil is in the pack, and it is still sharp.");
        return t.toString();
    }
    public static ItemStack account(String name,CompoundTag own){return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LINE),name,styled(HouseWriting.WritingStyle.PLAIN,accountText(name,own)));}
    public static final String HINT="Keep one hand on the line. It is the only way to the end, and it is how it reels you in.";
}
