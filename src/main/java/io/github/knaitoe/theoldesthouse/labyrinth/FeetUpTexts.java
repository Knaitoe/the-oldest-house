package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.200, Feet Up: the rules on the hall table in a child's hand, the chalk's words, the count, and the account of the
 * run, built from what the reader did. House writing: never the reader's gender, never a colour, never an age.
 */
public final class FeetUpTexts {
    private FeetUpTexts(){}
    public static final String TITLE="Feet Up";
    /** The rules, in the hand that pressed too hard. */
    public static final String RULES="""
        THE RULES

        1. The floor is lava. All of it. Stairs are floor.

        2. Bases: the big chair. The sideboard. The window seat on the landing. Our bed. The top of the wardrobe. You can rest on a base.

        3. If you touch, you go back to your base and it counts. Three and you're out.

        4. If they start shouting, don't go down. Go up.

        5. The attic is safe. Pull the ladder up after.""";
    /** What the chalk says, by FeetUpRooms.Chalk.line. */
    private static final String[] CHALK={"BASE","JUMP FROM THE ARM NOT THE SEAT","DON'T LOOK AT THE CHAIRS. THEY COME IF YOU DON'T LOOK","SNEAK ON THE RAIL","THE BOARDS GO. KEEP GOING","WAIT FOR ME"};
    public static String chalk(int line){return "Chalk on the wall: "+CHALK[Math.floorMod(line,CHALK.length)];}
    public static final String[] COUNT={"One.","Two.","Out."};
    /** What whoever kept the count whispers after an out, for where the reader last stood off the floor. */
    public static String hint(String area){return switch(area){
        case "front"->"The chair by the mantel comes if you look away from it.";
        case "hall"->"Stairs are floor. Go round by the rooms.";
        case "dining"->"Keep low under the fan.";
        case "kitchen"->"The pots hold if you don't stop on them. Pull the rope and the cage goes up.";
        case "landing"->"From the cage, the trunk and the table. The window seat is a base.";
        case "rail"->"Sneak on the rail. Or go the long way, through our room.";
        case "long_way"->"Our bed throws you higher than you come down.";
        case "boards"->"The boards go. Keep going. The press is a base.";
        default->"Start on the big chair.";};}
    private static final int CHILD_PAGE=110,PAGE=145;
    static List<String> pages(String text,int limit){
        var out=new ArrayList<String>();
        for(String paragraph:text.strip().split("\\n\\s*\\n")){
            String rest=paragraph.strip().replaceAll("[ \\t\\r\\n]+"," ");
            while(rest.length()>limit){int at=rest.lastIndexOf(' ',limit);if(at<1)at=limit;out.add(rest.substring(0,at));rest=rest.substring(at).stripLeading();}
            if(!rest.isEmpty())out.add(rest);
        }
        return List.copyOf(out);
    }
    public static ItemStack source(){
        var pages=new ArrayList<Component>();
        for(String page:pages(RULES,CHILD_PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.CHILD,page));
        return HouseWriting.book("The rules","The hall table",pages);
    }
    private static String times(int n){return switch(n){case 0->"never";case 1->"once";case 2->"twice";case 3->"three times";case 4->"four times";case 5->"five times";default->"more times than I counted";};}
    /** The run, in order: the rules if read, the chairs, the fan, where the floor had them, the way up, the attic and the tally. */
    public static String account(CompoundTag own){
        var b=new StringBuilder();
        b.append(own.getBoolean("Read_Source")?"The rules were on the hall table in a hand that pressed too hard, and the floor was lava, all of it.":"Nobody had left me the rules. The chalk on the walls said enough: the floor was lava, all of it.").append("\n\n");
        b.append("I went up the big chair's arm as the chalk said and along the shelves over the fire, and when I looked back from the mantelpiece the second chair had come closer to the sofa than I had left it. Nobody had moved it. The fan over the dining table kept the clock's time and I kept under it.\n\n");
        int touches=own.getInt(FeetUp.RUN_TOUCHES),attempts=own.getInt(FeetUp.ATTEMPTS);
        if(attempts>0)b.append("I was put out ").append(times(attempts)).append(": three touches, and the parlour door opened and the lamps went down, and I was at the front door again with the count at nothing.").append(attempts==1?" Whoever kept the count whispered where I had gone wrong.":" Each time, whoever kept the count whispered where I had gone wrong.").append("\n\n");
        if(touches==0)b.append("This time the floor never had me.");
        else b.append("The floor had me ").append(times(touches)).append(own.getBoolean(FeetUp.FELL_BOARDS)?", the last when the boards went under me upstairs and I came down into the evening again":"").append(", and each time the count was kept by someone who whispered it, and I was back on my base before I had finished falling.");
        b.append("\n\n");
        b.append(own.getBoolean(FeetUp.TOOK_RAIL)?"I went along the rail over the stair with my hands out and the dark under me, and did not look down.":"I went the long way, through the children's room and the bath, over the boards that went as soon as they were stood on, and the bed threw me higher than I had come down.").append("\n\n");
        b.append("By the time I was on top of the press the voices below had stopped being words. ").append(own.getBoolean(FeetUp.OPENED_HATCH)?"The ladder was up. I reached the catch, and it came down.":"The ladder was already down when I got there. Someone had gone up before me.").append("\n\n");
        b.append("There were blankets, a lantern with nothing in it, and a tally on the rafter: Ada, Wil, and a third column with no name and nothing in it. I added my own.");
        if(own.getBoolean(FeetUp.LEFT_DOWN))b.append(" Someone was still counting below me, so I left the ladder down. The rules had not said what to do about that.");
        else if(own.getBoolean(FeetUp.PULLED_UP))b.append(" Then I pulled the ladder up after me, the way the rules said.");
        return b.toString();
    }
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.FEET_UP),name,HouseWriting.WritingStyle.WILL,pages(account(own),PAGE));
    }
    /** The Witness line. */
}
