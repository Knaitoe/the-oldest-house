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
    /** 0.4.465, the swings: a hand on the frame; what a reader on one is told once; coming down off one; a hand on the mark. */
    public static final String SWINGS="A steel frame with four swings on chains under it. The paint is worn through where hands go.";
    public static final String SWING_HINT="Lean with the swing to take it higher. Jump to let go.";
    public static final String JUMP="You come down %s paces out from the swings. The furthest tonight is %s.";
    public static final String JUMP_LEVEL="You come down %s paces out from the swings, level with the furthest tonight.";
    public static final String JUMP_BEST="You come down %s paces out from the swings. Nobody has landed further tonight.";
    public static final String JUMP_OFF="You come down off the sand. That does not count.";
    public static final String MARK="A heel mark dug into the sand: the furthest out anyone has landed off the swings tonight.";
    public static final String MARK_OWN=MARK+" It is yours.",MARK_THEIRS=MARK+" It is %s's.";
    /** 0.4.466, the slide: a hand on any part of it. */
    public static final String SLIDE="A slide. Its chute is worn bright down the middle and dull at the edges.";
    /**
     * 0.4.467, the monkey bars: a hand on their ladders; a hand on the rungs from the ground, from on top of them and from too
     * far off; what a reader hanging from them is told once; and going from one end to the other.
     */
    public static final String BARS="Monkey bars. The paint is gone from every rung where hands go.";
    public static final String BARS_LOW="Out of reach from down here. Jump for them, or go up the ladder at either end.";
    public static final String BARS_ON="You are standing on top of them. Nobody is about to tell you to get down.";
    public static final String BARS_FAR="Not from here. Get under them.";
    public static final String BARS_HINT="Walk to go hand over hand, the way you are looking. Sneak lets go.";
    public static final String BARS_ACROSS="Across, from one end to the other, and not a foot on the sand.";
    /** The sand: a ball packed; no more to be carried; no room for one; what is left of one carried out of the night. */
    public static final String SAND="You pack a ball of sand. It holds.";
    public static final String SAND_FULL="Your pockets are full of sand.";
    public static final String SAND_NO_ROOM="You have nowhere to put it.";
    public static final String SAND_GONE="The sand has run out through your fingers.";

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
        // The playground, as far as this reader went on it.
        int far=own.getInt("SwingBest");
        if(far>0)p.add("I got one of the swings as high as it would go and let go of it, and came down "+Stillness.words(far)+" paces out in the sand.");
        else if(own.getBoolean("Swung"))p.add("I sat on one of the swings for a while.");
        if(own.getInt("Slid")>0)p.add(own.getInt("Slid")>2?"I went down the slide, and then went round and did it again.":"I went down the slide.");
        if(own.getInt("Crossed")>0)p.add("I went all the way across the monkey bars, hand over hand, without a foot on the sand.");
        else if(own.getInt("Hung")>0)p.add("I hung from the monkey bars for a while, and dropped.");
        if(own.getInt("Sand")>0)p.add(own.getInt("SandHits")>0?"I packed the sand into balls and threw them, and not all of them missed.":"I packed the sand into balls and threw them.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.BIKE_NIGHT),name,pages(HouseWriting.WritingStyle.WILL,p.toArray(String[]::new)));
    }
}
