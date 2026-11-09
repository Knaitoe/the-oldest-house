package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.409, the city on the ice: the radio operator's log (the first paper), what each carved panel shows, and the account. The source is
 * treated as protected: no names, no text and no title from it; every line here is original.
 */
public final class CityTexts {
    private CityTexts(){}
    public static final String HINT="The log says they went up the pass. Someone could follow them.";
    /** What a reader makes out in each panel, once they have stood and looked at it, in the order they go down the ramp. */
    static final String[] PANELS={
        "Shapes come down out of a sky crowded with stars, on wings folded like fans.",
        "They settle into a warm sea. Land rises under them out of the water.",
        "They raise terraces, five steps to each. The carving is very careful here.",
        "The city finished, under a large sun. Tall plants like ferns grow between the terraces.",
        "Something poured out of a vat, without a shape. They lift it out on poles.",
        "The shapeless things carry stones for them, in long lines, up the terraces.",
        "One of the shapeless things has eyes. In the next it has a mouth. The carver has drawn it over and over, with more each time.",
        "The shapeless things rise against them. Beams of light, and terraces broken in half.",
        "The shapeless things driven down under the city, smaller now. This carving is hurried.",
        "Ice comes down over the terraces. The figures stand and look up at it.",
        "They go down into a sea under the city, by a ramp that turns, like this one.",
        "A tunnel, and something filling it from wall to wall. Under the shape the stone has been cut away, deep, as though with a chisel."};
    /** How each panel is remembered in the account. */
    static final String[] REMEMBERED={
        "They came down out of the stars,","into a warm sea,","and built terraces in fives","until there was a city.",
        "They made things without a shape to work for them,","and the things carried their stones.","Then the things began to have eyes, and mouths,",
        "and rose against them,","and were driven down under the city.","Then the ice came,","and they went down to a sea under the city.",
        "The last one showed a tunnel full from wall to wall, and the stone under it had been cut out."};

    private static List<Component> pages(HouseWriting.WritingStyle style,String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}
    public static ItemStack source(){
        return HouseWriting.book("The radio log","Set operator",pages(HouseWriting.WritingStyle.PLAIN,
            "Day 1. Camp made on the ice under the range. All well. Wind light. Dogs in good heart.\n\nDay 4. Cores from the east face. The rock is older than anything we were told to expect.",
            "Day 6. Found them in a cave in the ice, eight, frozen. Brought three back to the table under the tarp. The dogs won't go near the tarp.\n\nDay 7. Storm in the night.",
            "Day 7, later. The straps on the table are cut. Not torn. Cut. All three gone. The dogs gone too, with their harnesses still done up.\n\nThe aeroplane's fabric opened in long cuts.",
            "Day 8. The others say the peaks to the north are too regular to be peaks. Going up the pass to look. I am to stay with the set.\n\nDay 9. Walls over the pass, they think. Steps. Not back yet. Calling every hour.",
            "Day 10. Calling.\n\nDay 11. Calling.\n\nDay 12. If anyone reads this: they went up the pass. The aeroplane won't fly. I am going after them."));
    }

    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("I came out of a door into a hut on the ice, in a wind that didn't stop. The radio was on. The camp outside had nobody in it: torn tents, an aeroplane with its fabric cut open, and the dogs' harnesses lying on the snow with the buckles still done up.");
        p.add("I followed them up the pass, between peaks too regular to be peaks, and the wind tried to put me back down it. From the top there was a city of dark stone, built in terraces of five, with a ramp turning down into the middle of it.");
        int read=Math.min(PANELS.length,own.getInt("Murals"));
        if(read>0){var told=new StringBuilder("Along the ramp there were carvings, and I read them in order going down. ");for(int i=0;i<read;i++)told.append(i==0?"":" ").append(REMEMBERED[i]);p.add(told.toString());}
        String tunnel="At the bottom a tunnel went on to a warm shore by dark water. There were prints in the grit of birds I never saw, and wings in the dark, and something piping a long way off over the water.";
        if(own.getBoolean("Survived"))tunnel+=" Then something came up the tunnel, filling it from wall to wall, with eyes that opened and closed in it. I ran. The lamps went out behind me all the way up the ramp, and I didn't stop.";
        p.add(tunnel);
        int caught=own.getInt("Caught");
        if(caught>0)p.add((caught==1?"Once":"More than once")+" I stopped, and it had me, and I woke on the ice in front of the hut with the wind on my face. I still knew what the carvings said.");
        if(own.getBoolean("Looked"))p.add("At the top of the pass I looked back at the far peaks. The mist on them was moving against the wind.");
        p.add("The radio was still on in the hut when I came back. Nobody was calling on it.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.BLACK_CITY),name,pages(HouseWriting.WritingStyle.WILL,p.toArray(String[]::new)));
    }
}
