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
        "TRIPLE TREE\n\nOne mile up, at the first marker, three pines grow out of one root. Children have built a fort in them, and rebuilt it, for as long as anyone in Drisking can remember.",
        "The public walk ends at Marker Two. Beyond it the ridge is county property.\n\nReturn before dusk.");}
    public static ItemStack contentNote(){return book("A note at the trailhead","The House",
        "CONTENT NOTE\n\nAbduction, coercion, sexual violence and death. These themes remain off-screen. No assault is shown or heard.",
        "The second visit begins an investigation into a child's disappearance. You may leave through the House door at any point. Leaving early does not finish a visit.");}
    public static ItemStack document(String key){return document(key,false);}
    public static ItemStack document(String key,boolean compassHeld){return switch(key){
        case "Evidence"->typed("Ridge property receipt","County property office",
            "COUNTY PROPERTY RECEIPT\nRidge Depot\n\nReceipt no. 263\nReleased by: Sheriff's office\nVehicle: County 17\nCarrier: Prescott Ore Services\nSigned for: Prescott",
            "Tag 263-A\nName: KELLY GARRITY\nCollected: Marker 3, 6:40 pm\nItems: teal pack; name patch, stitched inside the flap."+(compassHeld?"\nAdded: brass compass, from the Sheriff's desk.":""),
            "Case: closed.\nRelease to family: none.\nHeld at: sorting table.",
            "Transfer D-417\nNight access: authorized\nFuel: county maintenance account\nApproved: Sheriff's office",
            "Next tag: 264-A\nName: Kayla\nRelease to family: none.");
        // 0.4.237: the book on the desk under the depot. Nothing in it is described; it is kept like any other book of nights.
        case "Ledger"->typed("Night book, lower rooms","Ridge Depot",
            "NIGHT BOOK\nLower rooms\n\nAccess at night by approval only. Sign in and out. Fees to the county maintenance account.",
            "No names in this book. Rooms by number, tags by number.",
            "1964\nRm 2. In 11:40. Out 1:05.\nRm 5. In 12:10. Out 12:50.\nApproved: Sheriff's office.\nCounted: E. Prescott",
            "1979\nRm 1. In 11:15. Out 12:30.\nRm 5. Transfer, D-118.\nApproved: Sheriff's office.\nCounted: W. Prescott",
            "1993\nRm 3. In 10:50. Out 12:00.\nRm 4. No visitors. Unwell.\nRm 4. Closed. To the yard.",
            "Rm 2. Letter withheld.\nApproved: Sheriff's office.\nCounted: W. Prescott",
            "263-A. Rm 6. Received, 6:40 pm.\nRm 6. No visitors. Will not eat.\nRm 6. Asks for her friend by name.\nRm 6. Transfer, D-417. Night.",
            "264-A. Rm 7. Received.\nNight shift tonight.\nApproved: Sheriff's office.\nCounted: J. Prescott");
        // 0.4.237: a girl's letter home from room 2, taken off her and kept. She does not say the rest.
        case "Letter"->book("A letter, not sent","Dana",HouseWriting.WritingStyle.CHILD,110,
            "Mom,\n\nThey said I could write if I was good. They said you think I went to Aunt Ruth's. I didn't.",
            "I'm under the depot past the second marker.",
            "There are other girls. We knock on the walls at night so we know who's still here. The girl in four stopped knocking.",
            "Men come down at night. Mr. Prescott writes in a book. The Sheriff comes too.",
            "I'm not allowed to say the rest and I don't want to.",
            "Please come up past the marker. Please tell Ellie not to go to the fort.\n\nDana",
            "WITHHELD\nNot to be posted.\nW. P.");
        case "EliPoster"->typed("Missing poster for Patrick","The Maddox family",
            "MISSING\nPATRICK MADDOX\n\nLast seen at Marker Two. His bicycle was found behind the store. Please call the Maddox house.",
            "Written along the bottom in pencil\nPatrick, your bike is in the shed. I put it away for you. Mom");
        case "WrenPoster"->typed("Missing poster for Kendra","The Bell family",
            "MISSING\nKENDRA BELL\n\nLast seen on the ridge road. She was looking for Patrick Maddox. Please call the Bell house.",
            "The phone number is crossed out. The sheet underneath has Patrick's name and the same staple holes.");
        case "GateLog"->typed("Ridge gate log","Prescott Ore Services",
            "RIDGE DEPOT\nGATE LOG\n\nAfter dusk, admit county vehicles on the Sheriff's word. Keep the chain on between trucks.",
            "Vehicle 17. In 6:48 pm. One passenger.\nOut 6:52 pm. Driver only.\n\nNo mileage recorded. County account.");
        // 0.4.419: what the word means, and what the town owes. The page says nothing about what the depot does.
        case "History"->typed("Drisking, an iron town","Drisking Historical Society",
            "DRISKING\nAn iron town\n\nOre was found on the ridge in 1887. By 1900 there were four shafts and a refinery on the hill, and the town had grown up around the company store.",
            "The first shaft to give out was No. 1, the north working at the top of the ridge. Miners had a word for a mine that gives nothing back: borrasca. The name stayed with the ridge after the shaft was sealed.",
            "In 1953 children went into the old workings and did not come out. The county closed the mines and blasted the shafts shut the next spring.",
            "The blasting broke into the water table. For two years the town's water ran red, and most families left.",
            "The Prescotts stayed. Prescott Ore Services took the county maintenance contract and the old depot on the ridge, and in time brought Drisking back.",
            "Since 1961 the town has grown every year.\n\nDonated by the Prescott family.");
        case "Map"->typed("Pine trail map","County Trails",
            "OZARK PINE TRAIL\n\nTrailhead to Triple Tree: one mile, Marker 1.\nTriple Tree to Marker 2: the public walk ends here.",
            "Beyond Marker 2: county property, no public access. Markers 3 and 4 are maintenance markers on the ridge road.",
            "Stay on the marked trail. Return before dark. When the wind is up, keep below Marker Two.");
        default->throw new IllegalArgumentException(key);
    };}
    /** The store's noticeboard: the town's own paper, which changes between the reader's visits. Each visit's board is its own original. */
    public static String boardKey(int visit){return visit>=4?"Board4":visit==3?"Board3":"Board";}
    public static ItemStack board(int visit){
        if(visit>=4)return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper cancelled. Storm forecast for the ridge.",
            "MISSING\nKAYLA, 14\nLast seen walking home from the store. Call the Sheriff's office, 555-0117.\n\nThe paper is new and still white.",
            "Under it, older and softer, another sheet. Only one word is still legible: KELLY. Under that, the pin holes of sheets nobody kept.",
            "PRESCOTT ORE SERVICES\nNight shift tonight. Trucks on the ridge road after dark.");
        if(visit==3)return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper Sunday, six o'clock.\n\nFor sale: girl's bicycle, hardly ridden. Ask inside.",
            "MISSING\nKELLY GARRITY, 12\nLast seen on the pine trail near the second marker. Teal backpack.",
            "The sheet has gone soft and pale. Someone has written CLOSED across the bottom in marker.",
            "PRESCOTT ORE SERVICES\nCounty maintenance contract renewed. Night work available.");
        return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper Sunday, six o'clock. Bring a dish.\n\nLost: brown dog, answers to Biscuit.",
            "CURFEW\nChildren off the trails by dusk.\nSheriff's office",
            "PRESCOTT ORE SERVICES\nNight work at the ridge depot. Good pay. Ask at the yard gate.\nFamily owned since the old mine.",
            "MISSING\nDANA PRUITT, 15\nLast seen on the pine trail.\n\nThe sheet is sun-faded. A newer flyer for a bake sale is pinned over half of it.",
            "Behind the curfew notice, the curled corner of a much older poster: a girl's school photograph, the paper gone brown. The name has worn away.");
    }
    private static final String[] NOTES={
        "TRIPLE TREE FORT\n\nRules:\nNo grown-ups.\nPassword changes on Sundays.\nNobody goes past Marker Two.",
        "My mom says the fort was here when she was little, and the rules were the same. She won't say anything else about it.",
        "If you hear it, you come down. You don't wait for anybody. You don't look back up.",
        "Someone put a chair at the window to watch the ridge. We keep putting it back even though it wobbles.",
        "Dana's name is where the trunks join. Her mom showed me which one.",
        "The big kids call the top of the ridge Borrasca. Nobody will say what it means. Dad says it doesn't mean anything.",
        "The sound is like somebody hitting a pipe a long way off, three times. It comes when the wind turns. Mr. Prescott's trucks go up after.",
        "Kept here: the flashlight (dead), this.\nMy compass stays on me. The red tape is mine. If I lose it I'll never find the fort. That's a joke. It's three trees.",
        // 0.4.419: the tag rhyme. The tree is home, and the rhyme says what a name on it is for.
        "Tag: the tree is home. You have to be touching it when the rhyme ends or you're it.\n\nOne for the root and two for the bark,\nthree for the ridge that rings after dark.\nLeave your name where the three trunks meet\nand he'll know you by the sound of your feet."};
    public static String notesKey(boolean aged){return aged?"NotesAged":"Notes";}
    /** Her notes, pinned up in the fort. Years later they are still there, with the damp through them. */
    public static ItemStack notes(boolean aged){
        var pages=new String[NOTES.length];for(int i=0;i<NOTES.length;i++)pages[i]=aged?stained(NOTES[i],i):NOTES[i];
        return book(aged?BorrascaStory.FRIEND+"'s notes, water-stained":BorrascaStory.FRIEND+"'s notes",BorrascaStory.FRIEND,HouseWriting.WritingStyle.CHILD,110,pages);
    }
    private static String stained(String page,int seed){
        var words=page.split(" ");var out=new StringBuilder();for(int i=0;i<words.length;i++){if(i>0)out.append(' ');boolean keep=i==0||(i*7+seed*3)%5!=0||words[i].contains("\n");out.append(keep?words[i]:"...");}return out.toString();
    }
    /** The account, from what this reader did and heard. */
    public static String account(){return account(new CompoundTag());}
    public static String account(CompoundTag own){
        return (own.getBoolean("FoundNell")?"She called out which way to look when we played hide-and-seek. ":"")
            +(own.getBoolean("SharedWindow")?"In the fort she let me pick a window. We were still up there when the ridge rang. ":"")
            +"We cut our names into the three trees, mine beside hers."+(own.getBoolean("KnifeTaken")?" She lent me her knife to cut mine."+(own.getBoolean("KnifeKept")?" I nearly went home with it.":""):"")+(own.getBoolean("Sheltered6")||own.getBoolean("Sheltered10")?" When the noise came she pulled me down behind the timber.":"")
            +(own.getBoolean("SawGentleman")?" Past the second marker something tall stood in the pines and shone, and when the noise stopped it was gone.":"")
            // 0.4.428: the historical society, got into as a child.
            +(own.getInt("SocietyChild")>0?(own.getInt("SocietyChild")==1?" While Mr. Rowe smoked out front of his shop, I went into the historical society behind it.":" Behind Mr. Rowe's back I got into the historical society.")
                +" On a map of the old workings someone had drawn a small square over the top shaft and written DEPOT."+(own.getBoolean("CaughtInSociety")?" He came back in and sent me out.":"")
                :own.getBoolean("CaughtInSociety")?" Mr. Rowe found me in the back room of his shop and sent me out.":"")
            +" She was beside me when the light came. Men walked down out of it, and then she was gone. I came back grown, and someone had freshly cut her name away."
            +(own.getBoolean("HeardDenial")?" The Sheriff knew which marker we had reached. Nobody had ever told him.":" The Sheriff was still at the second marker, the same man, older.")
            +(own.getBoolean("Read_History")?" A framed page at the historical society said what Borrasca meant: the first mine to give out, the one that gives nothing back. The town's water had run red for two years, and after that the town grew every year.":"")
            +(own.getBoolean("Read_WrenPoster")?" Kendra had gone looking for Patrick. Her face was on the board too.":own.getBoolean("WrenRememberedEli")?" Kendra walked Patrick's bike back to his mom.":"")
            +" At the ridge, his office's papers and Prescott's signature lay beside her things."
            +(own.getBoolean("LowerRooms")?" Under the depot there were rooms with the bolts on the outside.":"")
            +(own.getBoolean("NellsRoom")?" In one of them her name was scratched into the wall beside mine. The room was empty.":"")
            +(own.getBoolean("MarasRoom")?" The newest room was still warm.":"")
            +(own.getBoolean("LedgerRead")?" A book kept the nights there by room number: who came down and when, approved by the Sheriff's office and counted by a Prescott, father and son. Beside her number it said transfer, and nothing after.":"")
            +(own.getBoolean("LetterRead")?" A girl had written home from down there. Her letter was never sent.":"")
            +(own.getBoolean("GravesSeen")?" Behind the shed there were numbers in the ground.":"")
            // 0.4.427: what the reader did to the Sheriff, grown.
            +(own.getBoolean("SheriffDead")?" I killed the Sheriff on the ridge. He talked until he couldn't, and none of it was sorry.":own.getBoolean("SheriffBeaten")?" I beat the Sheriff down into the dirt and left him there, still talking.":"")
            +" I stopped the machine."
            +(own.getBoolean("HeardSheriffShutdown")&&!own.getBoolean("SheriffDead")?" The Sheriff said he would make the call.":"")
            +" The wind was still in the pines."
            +(own.getBoolean("NameReturned")||own.getBoolean("RestoredName")?" "+restored():" I carried her name out in the cloth.");
    }
    public static ItemStack accountBook(String reader,CompoundTag own){return book("The name beside mine",reader,account(own));}
    public static String restored(){return "I cut her name beside mine again. The gouges were still there.";}
    public static String witness(CompoundTag own){return WitnessAccount.Story.BORRASCA.text+(own.getBoolean("SheriffDead")?" The Sheriff never came down off the ridge.":"")+(own.getBoolean("SharedWindow")?" We picked out a window before the ridge rang.":"")+(own.getBoolean("LedgerRead")?" The night book under the depot had the Sheriff's office on every page.":"")+(own.getBoolean("Read_WrenPoster")?" Kendra went looking for Patrick. Both names were on the store wall.":"")+(own.getBoolean("SheriffYielded")?" The Sheriff put his light down and let me pass.":own.getBoolean("HeardSheriffShutdown")?" When the machine stopped, he was still there.":"")+(own.getBoolean("NameReturned")?" "+restored():"");}
}
