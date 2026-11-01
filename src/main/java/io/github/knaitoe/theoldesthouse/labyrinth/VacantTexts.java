package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.228, Vacant Possession. The tenant's list taped up by the door (the source), what they say as the flat empties, what each
 * piece was hiding, and the removal docket on the van floor (the account). All of it is original.
 * House writing: never the reader's gender, never a colour of theirs, never an age; the reader is "you", or named. The tenant is
 * never given a gender or an age either.
 */
public final class VacantTexts {
    private VacantTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    public static final String HINT="Everything on the list goes down to the van. Anything can go back where it stood.";

    /** The list by the door, in the tenant's hand. */
    public static ItemStack source(){
        // A list keeps its lines: each page is written as it stands.
        var pages=new ArrayList<Component>();var hand=HouseWriting.WritingStyle.WILL;
        pages.add(HouseWriting.page(hand,"Dresser\nRug (roll it)\nBed\nWardrobe (leave the hangers)\nTable\nArmchair\nBoxes x4\nLamp"));
        pages.add(HouseWriting.page(hand,"and under a line, on its own:\n\nChair"));
        pages.add(HouseWriting.page(hand,"On the back, smaller:\n\nThe letter says the flat is to be left with vacant possession. I had to look it up. It means nobody in it."));
        return HouseWriting.book("The list","the tenant",pages);
    }

    /** The tenant's first words, when the reader comes into the flat. */
    public static final String GREETING="\"Oh, you came. It all goes down to the van, everything on the list. I can't manage the stairs with it any more.\"";
    /** After each piece is set down in the van: shorter, quieter, then broken off. */
    public static Component after(int loaded){
        String line=switch(loaded){
            case 1->"\"That's one. Mind the turn on the stairs, it's tighter than it looks.\"";
            case 2->"\"The fourth stair creaks. It always has.\"";
            case 3->"\"We had everyone round once. All in this room.\"";
            case 4->"\"You needn't be careful with that one.\"";
            case 5->"\"It's going quicker than I thought.\"";
            case 6->"\"Nearly...\"";
            case 7->"\"The light's...\"";
            default->"...";
        };
        var c=Component.literal(line);
        return loaded<=2?c:loaded<=5?c.withStyle(ChatFormatting.GRAY):c.withStyle(ChatFormatting.DARK_GRAY,ChatFormatting.ITALIC);
    }
    public static final String SITTING="There's one chair left in the room. They're sitting in it, facing the window.";
    public static final String LAST="\"That's the last of it.\"";
    public static Component faint(String line){return Component.literal(line).withStyle(ChatFormatting.DARK_GRAY,ChatFormatting.ITALIC);}

    /** What the reader finds where a piece stood. */
    public static String trace(VacantBlock.Kind kind){
        return switch(kind){
            case MARKS->"Pencil lines up the wall where the dresser stood, one above another, each with initials and a date beside it. The top one is the last.";
            case SCRATCHES->"Four worn places in the boards where the bed's legs stood, from years of getting in and out of it.";
            case SQUARE->"A brighter square of wallpaper where the wardrobe stood: the pattern as it was when it went up.";
            default->"Under where the rug lay, the boards are the colour they must have been when they were new.";
        };
    }

    /** The removal docket, filled in, and the reader's own account of the evening. */
    /** The docket's two pages, each as it stands: the form, and what went in the van. */
    public static List<String> docket(String reader,CompoundTag own){
        boolean carried="carried_out_the_last_chair".equals(own.getString("Outcome"));
        return List.of("REMOVAL DOCKET\n\nFrom: the first-floor flat\nTo:\n\nCarried by: "+reader,
            "Dresser, rug, bed, wardrobe, table, armchair, boxes (4), lamp"+(carried?", chair.":".")+(carried?"\n\nPremises left with vacant possession.":"\n\nOne chair left in place at the tenant's request."));
    }
    public static String afterwards(String reader,CompoundTag own){
        boolean carried="carried_out_the_last_chair".equals(own.getString("Outcome"));
        var t=new StringBuilder();
        t.append("The door of the flat was open when I came up the stairs. Someone was waiting there for help, and everything on their list had to go down to the van.\n\n");
        var seen=new StringBuilder();
        if(own.getBoolean("Marks228"))seen.append("Behind the dresser, pencil marks climbed the wall, each with initials and a date. ");
        if(own.getBoolean("Floor228"))seen.append("Under the rug the boards were new. ");
        if(own.getBoolean("Scratch228"))seen.append("The bed had worn four places in the floor. ");
        if(own.getBoolean("Square228"))seen.append("Where the wardrobe stood, the wallpaper was bright, a square of how the whole room used to look. ");
        if(seen.length()>0)t.append(seen.toString().trim()).append("\n\n");
        t.append("Each time I came back up, they were harder to hear. By the end I had to look for them to be sure they were there.\n\n");
        t.append("When everything else had gone, they were sitting in the last chair. They stood up when I came close and said that was the last of it. ");
        t.append(carried
            ?"I carried it down myself. Nobody came down the stairs after me, and when I looked up from the van, the window was dark."
            :"I picked it up, and then I put it back where it had stood, and they sat down in it again. When I got down to the street, the light was still on in the window.");
        if(own.getBoolean("Read_Source"))t.append("\n\nTheir list was taped up by the door: everything, and under a line, on its own, the chair.");
        return t.toString();
    }
    public static ItemStack account(String reader,CompoundTag own){
        var pages=new ArrayList<Component>();
        for(String page:docket(reader,own))pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,page));
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,afterwards(reader,own)));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.VACANT_POSSESSION),reader,pages);
    }
}
