package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.211, the stillness: a surveyor's page nailed by the way in (the source), and the reader's own count, scratched into the dust
 * before the far door (the account). All of it is original. House writing: never the reader's gender, never a colour of theirs,
 * never an age; the reader is "you", or named.
 */
public final class StillnessTexts {
    private StillnessTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}
    public static String capital(String s){return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}

    /** The surveyor's page: somebody else's count, and what they learned from it. */
    public static ItemStack source(){return HouseWriting.book("A page from a survey log, nailed up","Leg 14, unsigned",styled(HouseWriting.WritingStyle.PLAIN,
        "Leg 14. Wide hall, low. 11 across, 48 to the far door by tape. Ceiling at reach. No fixtures, no draught, no echo worth the name.\n\n"
        +"Noted the floor dust is disturbed in places. Took them for our own prints until I put my boot beside one.\n\n"
        +"Leg 14, later. Write this down while my hands will do it.\n\n"
        +"About halfway across we heard it come in at the far end. The door did not open. It came in anyway. Big. The floor knew before we did.\n\n"
        +"D. ran. I didn't, because I couldn't. That's the only reason.\n\n"
        +"It went after the running. Then it came back and found me by smell, and it stood over me, and it breathed on me. I counted. Thirty-one. It pushed me with its head twice to see if I would go over. I didn't go over.\n\n"
        +"Then it left. I don't know where D. is.\n\n"
        +"Whoever reads this: when you hear the hoofs, stop where you are. Not at the next pillar. Where you are. It doesn't see. It listens, and then it smells, and then it waits for you to move.\n\n"
        +"Don't."));}

    public static String accountText(String name,CompoundTag own){
        var t=new StringBuilder();int breaths=own.getInt("Breaths211"),pushes=own.getInt("Pushes211"),gored=own.getInt("Gored211");
        String drawn=own.getString("Drawn211"),drew=own.getString("Drew211");
        t.append("I counted. It was the only thing I could do with my body that it couldn't hear, so I did it.\n\n");
        if(gored>0)t.append(gored==1?"The first time, I moved. I don't remember deciding to. Then the horns, and I was back out of the door I came in by, and I had to walk that hall again knowing.\n\n"
            :"I moved, the first "+Stillness.words(gored)+" times. Each time I was put back out of the door I came in by, and each time I walked back in.\n\n");
        if(!drawn.isEmpty())t.append(capital(Stillness.words(breaths))+" breaths, and it had pushed me "+(pushes==1?"once":Stillness.words(pushes)+" times")+", when "+drawn+" made a noise down the hall on purpose and it lifted its head off me and went. I have never been so grateful to anyone, or so sure I had got someone killed.\n\n");
        else{t.append(capital(Stillness.words(breaths))+(breaths==1?" breath":" breaths")+". ");
            t.append(pushes==0?"It never pushed me. It only stood there, and breathed, and waited.\n\n":"It pushed me with its head "+(pushes==1?"once":Stillness.words(pushes)+" times")+", the way you push at something to see if it is dead. I let it.\n\n");}
        if(!drew.isEmpty())t.append("It was standing over "+drew+". I made a noise. I don't know why I did that. Then I heard it coming and I made myself stop, and it found me, and I stood there while it breathed on me instead.\n\n");
        t.append("Its breath was hot and wet and smelled of iron, and it came down on me from somewhere above my head in a hall I could touch the ceiling of. I never saw it. I don't know how big it was. I know how big it was.\n\n");
        t.append("When it went the floor stopped shaking a long time before I believed it. Then I walked to the far door, and I knelt down, and I scratched the count into the dust in rows of five, so that there would be a record that it had been ");
        t.append(breaths==0?"nothing.":Stillness.words(breaths)+" and not one more.");
        return t.toString();
    }
    public static ItemStack account(String name,CompoundTag own){return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.STILLNESS),name,styled(HouseWriting.WritingStyle.PLAIN,accountText(name,own)));}
    public static final String HINT="When the hoofs come, stop where you are. It doesn't see. It listens, then it smells, then it waits for you to move.";
}
