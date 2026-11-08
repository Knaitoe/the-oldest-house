package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.348, The Supper: the card tacked to the post at the mouth of the maze (the source, in Ida's hand), and the account each reader
 * carries away from the farm. The account says only what that reader's own record holds: whether they saw the oak across the drive;
 * what they were asked and poured; how the guest went out and how her chair was gone; whether and how they saw into the kitchen; who
 * said her name and what was answered; and what they did when the meat came: ate, left, or laid her keys on the table, or watched
 * someone else do it. House writing: never the reader's gender, never a colour, never an age.
 */
public final class MarshSupperTexts {
    private MarshSupperTexts(){}
    public static final String TITLE="The account: the supper";
    public static final String CARD_TITLE="Supper at the Marsh place";
    public static final String HINT="Let the meal be ordinary. Keep an eye on the guest, and on the kitchen door.";
    private static List<Component> pages(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    /** The supper card, in Ida's hand. */
    public static final String CARD="Supper at six, or when you hear the bell.\n\n"
        +"Follow the cut rows toward the windows. Del cut them wide this year, so nobody need get lost, and if you do, stand still and listen for the bell.\n\n"
        +"Mind the corn. It holds on to you.\n\n"
        +"Come round by the porch, not the back. There is always a plate.\n\n"
        +"Wipe your boots.";
    public static ItemStack source(){return HouseWriting.book(CARD_TITLE,"Ida Marsh",pages(HouseWriting.WritingStyle.WILL,CARD));}

    /** The reader's own account, from their record only. */
    public static ItemStack account(String name,CompoundTag t){
        var text=new StringBuilder();
        boolean partway=t.getBoolean("Partway");
        text.append("The corn had paths cut through it, and the windows of a farmhouse showed over the tassels the whole way. ");
        if(t.getBoolean("SawTree"))text.append("The only drive was blocked by an oak that had come down across it, roots and all, years ago by the moss on it. Nothing on wheels could have got past. ");
        if(t.getBoolean("Greeted"))text.append(t.getBoolean("Wiped")?"Before I was up the porch steps somebody inside said, Wipe your boots. I did. ":"Before I was up the porch steps somebody inside said, Wipe your boots. ");
        if(t.getBoolean("KeptSaid"))text.append("Ida said she had kept my place. She had. ");
        if(partway)text.append("I came in late. Supper was half over, and there was a gap at the table where a chair had been. ");
        else if(t.getBoolean("Seated")||t.getBoolean("HeardRide")){
            text.append("There were five at the table: Ida, who did the talking; Royce, who didn't; their son Del; Nana, who said she was deaf; and a guest called Corinne, whose car had quit on the county road. ");
            switch(t.getString("Answer")){
                case "road"->text.append("Ida asked where I'd come from. I said the road, and she said I had found them. ");
                case "house"->text.append("Ida asked where I'd come from. I said from a house a long way back, and she said I'd be hungry, then. ");
                case "quiet"->text.append("Ida asked where I'd come from. I didn't say, and she let it go. ");
                default->{}
            }
            if(t.contains("Poured"))text.append("She filled my glass from the jug without asking. ").append(t.getString("Poured").equals("tea")?"Tea. ":"Milk. ");
            if(t.getBoolean("TalkedCorinne"))text.append("Corinne asked if I had passed her car. Her brother was coming for her. He had said an hour. ");
            text.append("It was an ordinary supper. Corinne kept getting up to look out at the porch light. ");
        }
        if(t.getBoolean("HeardRide")&&!partway){
            text.append("Between courses she went out to look at the light again. When I came back to the table her chair was gone, not pushed in but taken away, and the places closed up round where it had been. Ida said, Her ride came. ");
            if(t.getBoolean("Doubted"))text.append("I had seen the drive. ");
        }
        if(t.getBoolean("SawRound"))text.append("I had seen her go round the side of the house toward the back door, not down the steps. ");
        if(t.getInt("TurnedBack")>0)text.append(t.getInt("TurnedBack")==1?"Ida turned me back from the kitchen door. Not in my kitchen, she said. ":"Ida turned me back from the kitchen door more than once. ");
        if(!t.getString("CoveredFor").isEmpty())text.append("I kept Ida talking about the farm while "+t.getString("CoveredFor")+" went through the kitchen door. ");
        if(t.getBoolean("SawKitchen")){
            text.append(switch(t.getString("KitchenHow")){
                case "door"->"When the swing door opened I saw into the kitchen. ";
                case "hatch"->"Through the serving hatch I could see into the kitchen. ";
                case "window"->"From the porch I looked in at the kitchen window. ";
                default->t.getBoolean("SlippedIn")?"I got through the kitchen door while Ida wasn't looking. ":"I went into the kitchen. ";
            });
            text.append("Her car keys were on the side by the butcher's block, on their fob, and her coat was over the back of a chair. ");
        }
        if(t.getBoolean("TookKeys"))text.append("I took her keys. ");
        if(t.getBoolean("SaidName"))text.append(t.getString("NameLine").equals("here")?"At the table I said that Corinne was still here. ":"At the table I asked where Corinne was. ");
        else if(t.getBoolean("HeardName"))text.append((t.getString("NameAsker").isEmpty()?"Somebody":t.getString("NameAsker"))+" said her name at the table. ");
        if(t.getBoolean("SaidName")||t.getBoolean("HeardName"))text.append("Del said he had heard her calling, from the kitchen. Nana said she'd thought the voice came from the corn. Ida said the meat was ready. ");
        if(t.getBoolean("RoyceBehind"))text.append("Royce carved standing behind my chair. I heard the knife, and not him. ");
        else if(t.getBoolean("Served"))text.append("Royce brought in the meat and carved it at the table. ");
        String by=t.getString("KeysBy");
        switch(t.getString("Outcome")){
            case MarshSupper.ATE->text.append("I ate what was served. I woke in the House with the taste of it still in my mouth.");
            case MarshSupper.LEFT->text.append(switch(t.getString("LeftBy")){
                case "corn"->"I got up and left without saying anything, back into the corn in the dark. Behind me the windows went out one by one. The House's door was where I had left it.";
                case "drive","gate"->"I got up and left without saying anything, along the drive, past the roots of the oak, to the gate onto the county road. The porch light was still burning behind me.";
                default->"I got up and left without saying anything. The House's door was where I had left it.";
            });
            case MarshSupper.WITNESS->{
                if(by.isEmpty())text.append("I put her keys on the table in front of Ida. Nobody moved. Nobody looked at them. Outside, the porch light went out.");
                else{text.append(by+" put her keys on the table in front of Ida. Nobody moved. Nobody looked at them. Outside, the porch light went out.");
                    if(t.getBoolean("TookKeys"))text.append(" I had brought them out of the kitchen myself.");}
            }
            default->{}
        }
        text.append("\n\nOn the post by the porch steps the dinner bell had lost its clapper. I found it in my pocket.");
        var pages=new ArrayList<Component>();pages.addAll(pages(HouseWriting.WritingStyle.WILL,text.toString().trim()));
        return HouseWriting.book(TITLE,name,pages);
    }
}
