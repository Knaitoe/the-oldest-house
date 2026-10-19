package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Original writing. The childhood legend withholds the cause; the final form records ordinary work. */
public final class BorrascaTexts {
    private BorrascaTexts(){}
    private static ItemStack book(String title,String author,String... pages){
        var fitted=new ArrayList<String>();for(String original:pages){var page=new StringBuilder();for(String word:original.split("(?<=\\s)")){if(page.length()+word.length()>145){fitted.add(page.toString().strip());page.setLength(0);}page.append(word);}if(!page.isEmpty())fitted.add(page.toString().strip());}
        return HouseWriting.book(title,author,HouseWriting.WritingStyle.WILL,fitted);
    }
    public static ItemStack source(){return book("Triple Tree trail","County trails",
        "TRIPLE TREE\n\nThe first mile marker stands beside the three trunks. The fort is above it.\n\nThe public walk ends at Marker Two. Return before dusk.");}
    public static ItemStack contentNote(){return book("A note at the trailhead","The House",
        "CONTENT NOTE\n\nAbduction, coercion, sexual violence and death. These themes remain off-screen. No assault is shown or heard.",
        "The second visit begins an investigation into a child's disappearance. You may leave through the House door at any point. Leaving early does not finish a visit.");}
    public static ItemStack document(String key){return document(key,false);}
    public static ItemStack document(String key,boolean compassHeld){return switch(key){
        case "Evidence","Register","Service"->book("Ridge property transfer","County property office",
            "COUNTY PROPERTY\nRIDGE DEPOT\n\nReceipt: 41\nAuthority: Sheriff's office\nVehicle: County 17\nTransport: Prescott Ore Services\nReceived by: Prescott",
            "Tag: 41-A\nName: KIMBER\nCollection: Marker 3\nContents: teal pack; stitched name patch."+(compassHeld?"\nSupplement: brass compass; Sheriff receipt.":"")+"\nCase: closed.\nRelease: none.\nStore: sorting table.",
            "Transfer: D-417\nNight access: authorized\nFuel account: county maintenance\nReceived: Prescott\nOffice approval: filed\n\nNext collection: 42-B\nName: Mara\nRelease: none.");
        case "Map"->book("Pine trail notice","County trails","Triple Tree: Marker 1.\n\nThe second marker is the end of the public walk.\n\nStay on the marked trail. Return before dark.");
        case "Bedroom"->book("Property tag 41-A","County property office","KIMBER\n\nTeal pack. Red tape on brass case. Name stitched inside flap.\n\nReceived with the other effects.");
        case "Testimony"->book("Receiving copy","Prescott Ore Services","Receipt 41 acknowledged.\n\nCounty transport returned empty.\n\nThe original remains at the sorting table.");
        default->throw new IllegalArgumentException(key);
    };}
    public static ItemStack accountBook(String reader){return book("The name beside mine",reader,account());}
    public static ItemStack accountBook(String reader,CompoundTag own){return book("The name beside mine",reader,account(),own.getBoolean("RestoredName")?restored():"I carried her name out in the cloth.");}
    public static String account(){return "We cut our names into the three trees. She was beside me when the light came. I came back grown, and someone had freshly cut her name away. The Sheriff knew which marker we had reached. At the ridge, his office's papers and Prescott's signature lay beside her things. I stopped the machine. The wind was still in the pines.";}
    public static String restored(){return "I cut her name beside mine again. The gouges were still there.";}
    public static String witness(CompoundTag own){return WitnessAccount.Story.BORRASCA.text+(own.getBoolean("NameReturned")?" "+restored():"");}
}
