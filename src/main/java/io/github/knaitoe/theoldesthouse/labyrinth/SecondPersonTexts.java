package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.274, Second Person (docs/SECOND_PERSON_0_4_274.md): the book on the shelf (its opening is the first paper; after that it opens at
 * its latest page) and the account. Original writing. The book and the account are in the second person, like the book's own kind;
 * they say what happened and what the reader did, never what it proves, and never name the reader's gender, age or colour.
 */
public final class SecondPersonTexts {
    private SecondPersonTexts(){}
    private static final int PAGE=150;
    static final String TITLE="The Flat Above the Launderette";
    static List<Component> printed(String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}

    /** Shown once after the opening: how to handle their things, which the opening does not say (the rest of what it says, it says already). */
    public static final String HINT="Right-click something of theirs to pick it up, and a shelf, a table or the floor to set it down.";

    /** The book's opening, the vignette's first paper. */
    public static final String OPENING=
        "1. You are standing in a hall that isn't yours. The boxes are not yours. In a minute somebody will come up the stairs with the "
        +"last of them, and they will not see you. They will never see you, not straight on.\n\n"
        +"You can do what people in houses like this have always done: move a thing from where it was put, open a door, shut one, turn a "
        +"lamp on, stand close enough to make the air go cold, knock on a wall. You can lift a phone that is ringing, and listen. "
        +"Breathe on cold glass and it will hold a word. Stand where glass faces you, and you may be seen in it.\n\n"
        +"When you want the time to pass, the airing cupboard at the end of the hall is warm. Go in and shut the door.\n\n"
        +"This book will keep up with you. Turn to its latest page when you need to know what you can do.";

    public static ItemStack source(){return HouseWriting.book(TITLE,"",printed(OPENING));}

    /** The account, written from the reader's record (filled in with the story). */
    public static ItemStack account(String reader,CompoundTag own){
        var pages=new ArrayList<Component>(printed(SecondPersonStory.account(own)));
        // 0.4.369: the pages the House turned in the reader's name come last, in a different type, apart from the reader's own.
        var house=SecondPersonStory.list(own,"HousePages");
        if(!house.isEmpty())for(String pg:LookoutTexts.pages("The pages you did not turn\n\n"+String.join("\n\n",house),PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,pg).copy().withStyle(net.minecraft.ChatFormatting.ITALIC));
        return HouseWriting.book("The account: the flat",reader,pages);
    }
}
