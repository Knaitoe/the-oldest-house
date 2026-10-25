package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Original writing. The childhood legend withholds the cause; the final form records ordinary work. */
public final class BorrascaTexts {
    private BorrascaTexts(){}
    private static ItemStack book(String title,String author,String... pages){return book(title,author,HouseWriting.WritingStyle.WILL,145,pages);}
    private static ItemStack typed(String title,String author,String... pages){return book(title,author,HouseWriting.WritingStyle.PLAIN,145,pages);}
    private static ItemStack book(String title,String author,HouseWriting.WritingStyle style,int limit,String... pages){
        var fitted=new ArrayList<String>();for(String original:pages){var page=new StringBuilder();for(String word:original.split("(?<=\\s)")){if(page.length()+word.length()>limit){fitted.add(page.toString().strip());page.setLength(0);}page.append(word);}if(!page.isEmpty())fitted.add(page.toString().strip());}
        return HouseWriting.book(title,author,style,fitted);
    }
    public static ItemStack source(){return typed("Triple Tree trail","County Trails",
        "TRIPLE TREE\n\nOne mile up, at the first marker, three pines grow out of one root. Children have built a fort in them.",
        "The public walk ends at Marker Two. Beyond it the ridge is county property.\n\nReturn before dusk.");}
    public static ItemStack contentNote(){return book("A note at the trailhead","The House",
        "CONTENT NOTE\n\nAbduction, coercion, sexual violence and death. These themes remain off-screen. No assault is shown or heard.",
        "The second visit begins an investigation into a child's disappearance. You may leave through the House door at any point. Leaving early does not finish a visit.");}
    public static ItemStack document(String key){return document(key,false);}
    public static ItemStack document(String key,boolean compassHeld){return switch(key){
        case "Evidence","Register","Service"->typed("Ridge property receipt","County property office",
            "COUNTY PROPERTY RECEIPT\nRidge Depot\n\nReceipt no. 41\nReleased by: Sheriff's office\nVehicle: County 17\nCarrier: Prescott Ore Services\nSigned for: Prescott",
            "Tag 41-A\nName: KIMBER\nCollected: Marker 3, 6:40 pm\nItems: teal pack; name patch, stitched inside the flap."+(compassHeld?"\nAdded: brass compass, from the Sheriff's desk.":""),
            "Case: closed.\nRelease to family: none.\nHeld at: sorting table.",
            "Transfer D-417\nNight access: authorized\nFuel: county maintenance account\nApproved: Sheriff's office",
            "Next tag: 42-A\nName: Mara\nRelease to family: none.");
        case "Map"->typed("Pine trail map","County Trails",
            "OZARK PINE TRAIL\n\nTrailhead to Triple Tree: one mile, Marker 1.\nTriple Tree to Marker 2: the public walk ends here.",
            "Beyond Marker 2: county property, no public access. Markers 3 and 4 are maintenance markers on the ridge road.",
            "Stay on the marked trail. Return before dark. When the wind is up, keep below Marker Two.");
        case "Bedroom"->typed("Property tag 41-A","County property office","KIMBER\n\nTeal pack. Red tape on brass case. Name stitched inside flap.\n\nReceived with the other effects.");
        case "Testimony"->typed("Receiving copy","Prescott Ore Services","Receipt 41 acknowledged.\n\nCounty transport returned empty.\n\nThe original remains at the sorting table.");
        default->throw new IllegalArgumentException(key);
    };}
    /** The store's noticeboard: the town's own paper, which changes between the reader's visits. Each visit's board is its own original. */
    public static String boardKey(int visit){return visit>=4?"Board4":visit==3?"Board3":"Board";}
    public static ItemStack board(int visit){
        if(visit>=4)return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper cancelled. Storm forecast for the ridge.",
            "MISSING\nMARA, 14\nLast seen walking home from the store. Call the Sheriff's office.\n\nThe paper is new and still white.",
            "Under it, older and softer, another sheet. Only one word is still legible: KIMBER.",
            "PRESCOTT ORE SERVICES\nNight shift tonight. Trucks on the ridge road after dark.");
        if(visit==3)return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper Sunday, six o'clock.\n\nFor sale: girl's bicycle, hardly ridden. Ask inside.",
            "MISSING\nKIMBER, 12\nLast seen on the pine trail near the second marker. Teal backpack.",
            "The sheet has gone soft and pale. Someone has written CLOSED across the bottom in marker.",
            "PRESCOTT ORE SERVICES\nCounty maintenance contract renewed. Night work available.");
        return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper Sunday, six o'clock. Bring a dish.\n\nLost: brown dog, answers to Biscuit.",
            "CURFEW\nChildren off the trails by dusk.\nSheriff's office",
            "PRESCOTT ORE SERVICES\nNight work at the ridge depot. Good pay. Ask at the yard gate.",
            "MISSING\nDANA PRUITT, 15\nLast seen on the pine trail.\n\nThe sheet is sun-faded. A newer flyer for a bake sale is pinned over half of it.");
    }
    private static final String[] NOTES={
        "TRIPLE TREE FORT\n\nRules:\nNo grown-ups.\nPassword changes on Sundays.\nNobody goes past Marker Two.",
        "If you hear it, you come down. You don't wait for anybody. You don't look back up.",
        "THE SHINY GENTLEMAN\nHe only comes past the second marker. He shines because he is metal or because he is wearing it.",
        "Dana Pruitt said she saw him. Then Dana moved away.",
        "The big kids call the top of the ridge Borrasca. Nobody will say what it means. Dad says it doesn't mean anything.",
        "The sound is like somebody hitting a pipe a long way off, three times. It comes when the wind turns. Mr. Prescott's trucks go up after.",
        "Kept here: my compass (the red tape is mine), the flashlight (dead), this.\nIf I lose the compass I'll never find the fort. That's a joke. It's three trees."};
    public static String notesKey(boolean aged){return aged?"NotesAged":"Notes";}
    /** Kimber's notes, pinned up in the fort. Years later they are still there, with the damp through them. */
    public static ItemStack notes(boolean aged){
        var pages=new String[NOTES.length];for(int i=0;i<NOTES.length;i++)pages[i]=aged?stained(NOTES[i],i):NOTES[i];
        return book(aged?"Kimber's notes, water-stained":"Kimber's notes","Kimber",HouseWriting.WritingStyle.CHILD,110,pages);
    }
    private static String stained(String page,int seed){
        var words=page.split(" ");var out=new StringBuilder();for(int i=0;i<words.length;i++){if(i>0)out.append(' ');boolean keep=i==0||(i*7+seed*3)%5!=0||words[i].contains("\n");out.append(keep?words[i]:"...");}return out.toString();
    }
    public static ItemStack accountBook(String reader){return book("The name beside mine",reader,account());}
    public static ItemStack accountBook(String reader,CompoundTag own){return book("The name beside mine",reader,account(),own.getBoolean("RestoredName")?restored():"I carried her name out in the cloth.");}
    public static String account(){return "We cut our names into the three trees. She was beside me when the light came. I came back grown, and someone had freshly cut her name away. The Sheriff knew which marker we had reached. At the ridge, his office's papers and Prescott's signature lay beside her things. I stopped the machine. The wind was still in the pines.";}
    public static String restored(){return "I cut her name beside mine again. The gouges were still there.";}
    public static String witness(CompoundTag own){return WitnessAccount.Story.BORRASCA.text+(own.getBoolean("NameReturned")?" "+restored():"");}
}
