package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.210, the storeroom: two hands, the second of them the reader's. The list pinned beside the door is in the reader's own
 * handwriting (the source); the account is a footnote in Johnny's hand that runs off its page and, partway through, turns into the
 * reader's own hand describing the same room. All of it is original. House writing: never the reader's gender, never a colour of
 * theirs, never an age; the reader is "you", or named.
 */
public final class StoreroomTexts {
    private StoreroomTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    /** The list, in the reader's own hand: two of the shop's things and one of theirs. */
    public static ItemStack list(String name,String thing){return HouseWriting.book("A list, in your own hand",name,styled(HouseWriting.WritingStyle.PLAIN,
        "A page torn out of a notebook like yours and pinned beside the door, on its inside. The handwriting is yours. You would know it anywhere; you have never written this.\n\n"
        +"GET:\n1. ink. black. the big bottle\n2. needles (the sterile ones, in the packs)\n3. "+thing.toLowerCase(Locale.ROOT)+" (mine)\n\n"
        +"Underneath, pressed so hard the pen has gone through:\n\nDON'T TURN ROUND."));}
    /** The list as a source before the reader's own thing is known (for the catalogue and the architecture case). */
    public static ItemStack source(){return list("(your own name)","(something of yours)");}

    public static String johnny(String name,CompoundTag own){
        var t=new StringBuilder();
        t.append("²¹ I keep going back to the storeroom, which is stupid, because I know exactly what's in it. Ink. Needles. Boxes nobody has opened since the shop changed hands. One bulb on a chain halfway down, the kind you pull and it swings. I've been in there a hundred times for a hundred bottles and nothing ever happened, and then one night I went in for black and the door swung shut behind me on its spring like it always does, and I knew, the way you know a stair is missing in the dark, that the room was not empty.\n\n");
        t.append("Nothing touched me. That's not what this is. What it is, is that the room was behind me. I don't know how else to put it. Not something in the room. The room. Every time I didn't look, it got nearer, and every time I looked it got smaller, and I understood, standing there with the bottle in my hand, that the trick was not to win. The trick was to");
        t.append(" (The footnote goes on in the margin, and then off the edge of the page, and the next lines are not his. They are yours.)\n\n");
        return t.toString();
    }
    public static String yours(String name,CompoundTag own){
        var t=new StringBuilder();int looks=own.getInt("Looks210"),knocked=own.getInt("Knocked210");String gone=own.getString("Gone210");
        t.append("— get out without giving it what it wanted. I went in for three things. Two of them were the shop's. The third was mine, on the end shelf, with my name on a tag in my own writing, and I have never been in that shop in my life.\n\n");
        if(knocked>0)t.append(knocked==1?"The first time, I didn't make it. A hand on my shoulder, and my head turned for me, slowly, towards the door, and the light went out just before I could see. I woke up in the hall. ":"I didn't make it the first "+knocked+" times. ").append(gone.isEmpty()?"\n\n":"My "+gone.toLowerCase(Locale.ROOT)+" was gone. I have stopped expecting it back.\n\n");
        t.append(switch(looks){
            case 0->"I never looked back. Not once. I could hear it breathing an arm's length behind me and I did not look.\n\n";
            case 1->"I looked back once. The light died, and when it came back the shelves were a block nearer, and I did not look again.\n\n";
            default->"I looked back "+looks+" times. The second time the shelves were on my shoulders and I couldn't get a breath. I don't know why it let me out.\n\n";
        });
        t.append("I got all three, and I backed out. The door opened behind me as I backed into it. I didn't turn round. I didn't turn round until I was out, and then I didn't turn round for a long time after that either.\n\n");
        t.append("I still have the thing with my name on it. I don't know which of us wrote the tag.");
        return t.toString();
    }
    public static String accountText(String name,CompoundTag own){return johnny(name,own)+yours(name,own);}
    public static ItemStack account(String name,CompoundTag own){
        var pages=new ArrayList<Component>();pages.addAll(styled(HouseWriting.WritingStyle.JOHNNY,johnny(name,own)));pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,yours(name,own)));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.STOREROOM),name,pages);
    }
    public static final String HINT="Every time you look back at the door, the room gets smaller. Every time you don't, it gets nearer.";
}
