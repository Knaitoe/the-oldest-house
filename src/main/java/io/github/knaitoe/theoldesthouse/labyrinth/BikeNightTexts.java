package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.464, Bike night (docs/BIKE_NIGHT_PROPOSAL.md): what is written and said. This slice: the sheet taped up by the park's door (the
 * first paper), what the reader is told about the park and the bikes, and as much of the account as there is a night to give.
 * Every name, place and line is original.
 */
public final class BikeNightTexts {
    private BikeNightTexts(){}
    public static final String HINT="Whichever bike you take from the rack is yours for the night.";
    public static final String ARRIVE="A town park after dark, on the last night of October. A path runs from the shed to the gate, and by the gate six bikes stand in a rack.";
    /** A bike taken; a bike given back for another; a bike that is someone else's (their name goes in); one's own, already had. */
    public static final String YOURS="It is yours for the night.",SWAPPED="You leave the other one for whoever wants it. This one is yours for the night.";
    public static final String THEIRS="%s has that one tonight.",SOMEONES="Somebody has that one tonight.";
    /** A hand on the park's own things. */
    public static final String GATE="The gate is chained, and the padlock on the chain is new.";
    public static final String RAILINGS="Iron railings, taller than you are, with points on them.";
    public static final String LAMP="A park lamp. It gives a small light and a hum.",LAMP_FAILING="This one keeps going out. Each time it comes back a little later.";
    public static final String BENCH="A park bench. Somebody has cut initials into the top slat, and somebody else has cut them out again.";
    public static final String BIN="A wire bin, a third full: wrappers, a paper bag, a burst balloon.";
    public static final String RACK="A bike rack bolted to the path. Six places.";
    public static final String BORDER="Squared timbers round the sand, worn pale where people step over them.";

    private static List<Component> pages(HouseWriting.WritingStyle style,String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}
    /** A sheet from a school pad, in pencil. (Not the House's child's hand, which is a small child's and loses letters: Gus can spell.) */
    public static ItemStack source(){
        return HouseWriting.book("A page from a school pad","Gus",pages(HouseWriting.WritingStyle.PLAIN,
            "HALLOWEEN PLAN. Secret.\n\n1. They say we are too old for trick or treat. Fine.\n\n2. Meet at Calder Park when it is dark. The gate will be chained. Come through the tool shed. The lock on it has been broken since summer.",
            "3. Bring your bike. If you have not got one there are always some in the rack that nobody comes back for.\n\n4. Bring a flashlight. Bring candy if you have any.",
            "5. If Tully and those guys turn up do NOT say anything. Remember last time.\n\n6. Do NOT cut through the graveyard. I mean it. You know why.\n\nGus\n\nP.S. The one with the card in its spokes is mine. Kidding. Take any."));
    }

    /** What a bike is called in the account. */
    static String bike(String make){
        return switch(make){case "wasp"->"the Wasp";case "hare"->"the Hare";case "comet"->"the Comet";case "hopper"->"the Hopper";case "lamp"->"the Lamp";case "rattler"->"the Rattler";default->"";};
    }
    /** The account, from the reader's own record. There is only the park to tell of yet. */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("It was the last night of October in a town called Calder Falls, and I was in the park after it had shut.");
        String bike=bike(own.getString("Bike"));
        p.add(bike.isEmpty()?"There were six bikes in the rack by the gate. I did not take one.":"There were six bikes in the rack by the gate. I took "+bike+".");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.BIKE_NIGHT),name,pages(HouseWriting.WritingStyle.WILL,p.toArray(String[]::new)));
    }
}
