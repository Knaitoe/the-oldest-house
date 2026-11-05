package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.310, A Deer in the Road: the county's notice on its post by the hatched shoulder (the source), and the account the reader carries
 * off the road. The account says only what this reader's own record holds: what they could see from where they stood. Two readers
 * on the same road can disagree, and both be right; the owner's closing line is for a reader who saw both bodies in one look.
 * House writing: never the reader's gender, never a colour, never an age.
 */
public final class DeerRoadTexts {
    private DeerRoadTexts(){}
    public static final String TITLE="The account: the county road";
    public static final String HINT="Set the triangle behind the car. Keep off the road. Flag the next vehicle.";
    private static List<Component> pages(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    public static ItemStack source(){
        var pages=new ArrayList<Component>();
        pages.addAll(pages(HouseWriting.WritingStyle.PLAIN,
            "COUNTY ROAD 9\nDEER CROSSING, NEXT 2 MILES\n\nIn a collision:\n1. Hazard lights on.\n2. Set your warning triangle on the hatched shoulder behind the vehicle.\n3. Keep off the carriageway.\n4. Flag the next vehicle from the shoulder."));
        pages.addAll(pages(HouseWriting.WritingStyle.PLAIN,
            "Do not try to move the animal.\n\nIf the animal is carrying, keep clear of it.\n\nThe drainage gate at the head of the ditch is not locked. The pump house door at the bend is."));
        pages.addAll(pages(HouseWriting.WritingStyle.WILL,"Under it, in pencil: it isn't. Neither of them is. Don't sit in the car."));
        return HouseWriting.book("County Road 9","The county",pages);
    }

    /** The reader's own account, from their record only. */
    public static ItemStack account(String name,CompoundTag t){
        var text=new StringBuilder();
        int beat=t.getInt("DeerBeat");boolean drove=t.getBoolean("DeerDriver");
        if(drove)text.append(switch(beat){
            case DeerRoadScene.BRAKE->"I was driving. A doe came out broadside into the low beams and I stood on the brake. It wasn't enough. ";
            case DeerRoadScene.LEFT->"I was driving. A doe came out broadside into the low beams and I pulled across the line. We hit her anyway. ";
            case DeerRoadScene.RIGHT->"I was driving. A doe came out broadside into the low beams and I turned for the shoulder. We hit her anyway. ";
            default->"I was driving. A doe came out broadside into the low beams, heavy with young, and there was no time to do anything. ";
        });
        else text.append("I came onto the road in the rain, by a car with its hazards going. A doe had been hit. ");
        if(t.getBoolean("DeerSawHand"))text.append("Her belly moved against the road. A hand came out of it and took hold of the centre line. ");
        if(t.getBoolean("DeerSawRise"))text.append("Then a woman in a flowered nightgown, using the doe to get her feet under her, and she kept getting taller. Her neck was bent the way it had been in the tunnel. ");
        if(t.getBoolean("DeerKilled1"))text.append("I hit her until she went down in the road. ");
        else if(t.getBoolean("DeerStruck1"))text.append("I hit her. It didn't seem to matter to her. ");
        if(t.getBoolean("DeerSawDeath1"))text.append(t.getBoolean("DeerFirstAtDoor")?"She reached for the driver's door and her legs went, and she sagged against it and stayed there. ":"She went down on the wet road and stayed there. ");
        if(t.getBoolean("DeerSetTriangle"))text.append("I set the triangle out behind the car. ");
        if(t.getBoolean("DeerHeardReel"))text.append("In the car the back belt was winding itself tight, a tooth at a time. ");
        if(t.getBoolean("DeerSawBirth2"))text.append("The back seat split along its seam and she came up out of the foam, her head turned sideways to get between the seat and the door. ");
        else if(t.getBoolean("DeerWatchedFirst"))text.append("I was watching the one by the door when something climbed out of the back of the car. She never got up. ");
        else if(t.getBoolean("DeerHeardBirth2"))text.append(t.getBoolean("DeerFromDitch")?"From the ditch I heard the car door, and something heavy getting out. I didn't see where she came from. ":"I heard the car door, and something heavy getting out. I didn't see where she came from. ");
        if(t.getBoolean("DeerSawSecond"))text.append("It was the same neck. The same dress. She took the same long step the tunnel taught me. ");
        if(t.getBoolean("DeerHeldOff")&&!t.getBoolean("DeerKilled2"))text.append("I kept the car between us, and its doors. ");
        if(t.getBoolean("DeerKilled2"))text.append("I put her down a second time. ");
        if(t.getBoolean("DeerSawThird"))text.append("Going past the culvert something scraped twice behind the grate, and a hand tried the bars. ");
        else if(t.getBoolean("DeerHeardScrape"))text.append("Going past the culvert something scraped twice behind the grate. ");
        if(t.getBoolean("DeerVehicleStopped"))text.append(t.getBoolean("DeerFlagged")?"I flagged the next car from the shoulder. It stopped alongside with its lights on her, and she stood in them. ":"The next car stopped alongside with its lights on her, and she stood in them. ");
        text.append(switch(t.getString("DeerExit")){
            case "drain"->"I went down the ditch to the drainage gate and through it. ";
            default->"I went back along the shoulder to the door at the bend. ";
        });
        if(t.getBoolean("DeerBothAtOnce"))text.append("\n\nI left her lying against the driver's door. When the woman in the back seat got her feet under her, the one on the road was still lying there.".replace("against the driver's door",t.getBoolean("DeerFirstAtDoor")?"against the driver's door":"on the road"));
        var pages=new ArrayList<Component>();pages.addAll(pages(HouseWriting.WritingStyle.WILL,text.toString().trim()));
        return HouseWriting.book(TITLE,name,pages);
    }
}
