package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.549, The Ones Already Inside: what is written. The story is the owner's own (docs/THE_ONES_ALREADY_INSIDE_PROPOSAL.md); every
 * line here is new. Nothing here says what the reader is like, how they feel, or what they are called.
 */
public final class LodgingTexts {
    private LodgingTexts(){}
    public static final String HINT="The terms of the house are on the hall table.";
    public static final String FRAME="A doorcase set into the outside wall. Behind it there is only plaster, a hand's depth back, and the plaster is warm.",
        DANIELS_FRAME="A doorcase in the back wall. The plaster inside it is worn smooth at the height of a pair of shoulders.",
        SHUTTERS="The shutters are screwed shut from this side. The screws are painted over.",
        GLASS="The hall glass. It is a moment late, or the light in here is.",
        GRATE="The grate is laid and has never been lit. The coal is painted.",
        RATCHET="An iron wheel with a pawl in its teeth, and a lever to lift the pawl. A chain goes up from it into the floor above.",
        POST="A post under one of the rooms upstairs, its foot in an iron shoe. A wedge is driven in beside it.",
        DEAD_POST="A post with its own shoe and wedge, standing on the bare floor. Nothing above it. Rope burns go round it at the height of a pair of wrists.",
        TALLY="Marks in fives, scratched with something blunt. Then marks that are not in fives. Then marks that are only lines.",
        BOLT_DRAWN="The bolt is on this side. It slides back easily; it is used.";
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the terms of the house, printed on a card, with additions in ink. */
    public static ItemStack source(){
        return HouseWriting.book("Terms of the house","V.",pages(
            "TERMS\n\nRooms by the week. A week is seven nights.\n\nGuests are asked to keep to their own rooms after eleven.\n\nThe hatches are for the house's use.",
            "Do not wake the sleeper in your room. It is not yours yet, and it is not used to being looked at.\n\nNothing in this house is to be touched that is not your own. You will find that easy.",
            "Anyone who is tired may rest. Anyone who rests will be looked after, and missed by nobody.\n\nThe house does not keep a register. The house remembers.\n\nV."));
    }
    /**
     * The account. This slice the story cannot be finished, so nothing asks for this; it says only what a reader could have seen of the
     * house, and later slices write the rest from the reader's own record.
     */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("There was a lodging house with its shutters screwed shut, and a card of terms in the hall.");
        if(own.getBoolean("WentUp"))p.add("Upstairs every room had a number, and every outside wall had a doorway in it that went nowhere.");
        if(own.getBoolean("WentDown"))p.add("Under the house the whole of it stood on posts, and the posts stood on a beam, and the beam was held by one wheel.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.ONES_INSIDE),name,pages(String.join("\n\n",p)));
    }
}
