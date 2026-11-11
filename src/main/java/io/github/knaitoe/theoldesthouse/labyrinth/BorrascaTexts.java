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
    /**
     * 0.4.437: a note made of letters cut out and pasted down. Each word is one cutting or a cutting a letter, and each cutting
     * keeps its own type, weight and ink, chosen the same way every time from the note's title, so no page has a hand. The last
     * page is the stamp, typed.
     */
    private static ItemStack pasted(String title,String author,String stamp,String... pages){
        var rng=new Random(title.hashCode());var out=new ArrayList<net.minecraft.network.chat.Component>();
        int[] ink={0x1A1A1A,0x3B1F14,0x5A1414,0x14285A,0x1F3D1F,0x4A3A10,0x2E2E2E,0x3A1A3A};
        var uniform=net.minecraft.resources.ResourceLocation.withDefaultNamespace("uniform");
        for(String page:pages){var c=net.minecraft.network.chat.Component.empty();
            for(String part:page.split("(?<=[ \n])|(?=[ \n])")){
                if(part.equals(" ")||part.equals("\n")){c.append(net.minecraft.network.chat.Component.literal(part));continue;}
                boolean whole=rng.nextFloat()<.35F;
                for(String cut:whole?List.of(part):Arrays.asList(part.split(""))){
                    String text=!whole&&Character.isLetter(cut.charAt(0))&&rng.nextFloat()<.3F?cut.toLowerCase(Locale.ROOT):cut;
                    var style=net.minecraft.network.chat.Style.EMPTY.withColor(ink[rng.nextInt(ink.length)]).withBold(rng.nextFloat()<.4F)
                        .withItalic(rng.nextFloat()<.15F).withUnderlined(rng.nextFloat()<.08F).withFont(rng.nextBoolean()?uniform:net.minecraft.network.chat.Style.DEFAULT_FONT);
                    c.append(net.minecraft.network.chat.Component.literal(text).withStyle(style));
                }
            }
            out.add(c);
        }
        out.add(net.minecraft.network.chat.Component.literal(stamp));
        return HouseWriting.book(title,author,out);
    }
    public static ItemStack source(){return typed("Triple Tree trail","County Trails",
        "TRIPLE TREE\n\nOne mile up, at the first marker, three pines grow out of one root. Children have built a fort in them, and rebuilt it, for as long as anyone in Drisking can remember.",
        "The public walk ends at Marker Two. Beyond it the ridge is county property.\n\nReturn before dusk.");}
    public static ItemStack contentNote(){return book("A note at the trailhead","The House",
        "CONTENT NOTE\n\nAbduction, coercion, sexual violence and death. Sexual violence stays off-screen. No assault is shown or heard.",
        // 0.4.439 (owner: the feeding scene is graphic gore, and the note must say so).
        "GRAPHIC GORE\n\nOn the last visit a woman is fed alive into a machine. It is slow, and it is shown: her body torn apart, her screaming, and what is left of her.",
        "The second visit begins an investigation into a child's disappearance. You may leave through the House door at any point. Leaving early does not finish a visit.");}
    public static ItemStack document(String key){return document(key,false);}
    public static ItemStack document(String key,boolean compassHeld){return switch(key){
        case "Evidence"->typed("Ridge property receipt","County property office",
            "COUNTY PROPERTY RECEIPT\nRidge Depot\n\nReceipt no. 263\nReleased by: Sheriff's office\nVehicle: County 17\nCarrier: Prescott Ore Services\nSigned for: Prescott",
            "Tag 263-A\nName: KELLY GARRITY\nCollected: Marker 3, 6:40 pm\nItems: teal pack; name patch, stitched inside the flap."+(compassHeld?"\nAdded: brass compass, from the Sheriff's desk.":""),
            "Case: closed.\nRelease to family: none.\nHeld at: sorting table.",
            "Kept above.\nNot to come down before eighteen.\nFuel: county maintenance account\nApproved: Sheriff's office",
            "Next tag: 264-A\nName: Kayla\nRelease to family: none.");
        // 0.4.237: the book on the desk under the depot. Nothing in it is described; it is kept like any other book of nights.
        // 0.4.437 (owner): the lower rooms hold grown women only, and the book keeps the town's reason. Nobody comes down before
        // eighteen; a child taken is "kept above", and what is born below is placed in town by the doctor.
        case "Ledger"->typed("Night book, lower rooms","Ridge Depot",
            "NIGHT BOOK\nLower rooms\n\nAccess at night by approval only. Sign in and out. Fees to the county maintenance account.",
            "No names in this book. Rooms by number, tags by number.\n\nNone to come down before eighteen. Births to the doctor. Placings by the doctor.",
            "1964\nRm 2. In 11:40. Out 1:05.\nRm 5. In 12:10. Out 12:50.\nApproved: Sheriff's office.\nCounted: E. Prescott",
            "1983\n141-A. Down. Rm 2.\n1985\nRm 2. Delivered, girl. Placed in town.\nApproved: Sheriff's office.\nCounted: W. Prescott",
            "1987\nRm 2. Letter withheld.\nRm 4. No visitors. Unwell.\nRm 4. Closed. To the yard.",
            "1993\n263-A. Received, 6:40 pm. Kept above.\n1999\n263-A. Down. Rm 6.\nRm 6. Will not eat. Asks for her friend by name.",
            "2001\nRm 6. Delivered, boy. Placed in town.\n2004\nRm 6. Delivered, girl. Placed in town.\nRm 6. Asks for nobody now.",
            "Rm 2. No more deliveries. Up before the shift.\n264-A. Received tonight. To the doctor first. Rm 7 made up.\nApproved: Sheriff's office.\nCounted: J. Prescott");
        // 0.4.437 (owner): room 2's note home, made of letters cut from the catalogues they are given and pasted down, so it has no
        // hand and gives no age. It is an adult's, from the year the book withholds it; it was taken off her and kept.
        case "Letter"->pasted("A pasted note, not sent","Rm 2","WITHHELD\nNot to be posted.\nRm 2. 141-A.\nW. P.",
            "TELL MY MOTHER\nI DID NOT\nRUN OFF",
            "I AM UNDER\nTHE DEPOT\nPAST THE\nSECOND MARKER",
            "THEY CALL ME\n141\nTHERE ARE\nOTHERS",
            "WE KNOCK\nON THE WALLS\nFOUR STOPPED\nKNOCKING",
            "THE BABIES\nGO UP TO TOWN\nASK THE DOCTOR\nWHOSE THEY ARE",
            "COME PAST\nTHE MARKER\nNOT THE SHERIFF");
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
        // 0.4.444 (owner: "Documents are not interactable."): the depot's ordinary paperwork. Nothing in it is described; it is the
        // town's names, and who is paid for what. The church's tables and its sale come off the same truck and the same table.
        case "Dispatch"->typed("Dispatch sheet","Prescott Ore Services",
            "PRESCOTT ORE SERVICES\nDISPATCH\n\nD-417: feed and linen to the depot, Monday and Thursday.\nD-422: folding tables to the church, Sunday. Bring them back.",
            "County 17: on the Sheriff's word only. No sheet.\n\nDr. Hale: Tuesday and Friday, by the lower door. He has his own key.",
            "Drivers do not go below.\nDrivers do not ask what is in a county vehicle.\n\nAnyone who asks is sent to Mr. Prescott.");
        case "Payroll"->typed("Payroll, October","Prescott Ore Services",
            "PAYROLL\nOctober\n\nYard: 6 men.\nDrivers: 4.\nKitchen and linen: 2.\nNights, lower rooms: 3.",
            "Retainers\n\nDutch. Merle. County, retired.\nMrs. Odell. The doctor's records.\nAmes. Feed, at cost.\nGus. Fridays.",
            "Summer hires\n\nP. Webb, driver's mate.\nP. Tate, kitchen.\n\nNew hires are told what the yard is for by their own fathers, not here.");
        case "SignIn"->typed("Sign-in sheet","Ridge Depot",
            "SIGN IN\nNights\n\n9:40  Sheriff's office\n9:55  Dr. Hale\n10:10  J. Prescott\n10:15  initials\n10:15  initials",
            "Members sign with initials. The book downstairs has the rooms.\n\nNo guests.\nNo sons under twenty-one.");
        case "Intake"->typed("Intake form","Ridge Depot",
            "INTAKE\n\nTag: 264-A\nReceived: tonight\nFrom: County 17\nCondition: walking\nTo: the doctor first, then Rm 7",
            "Effects: one purse, one ring. To the sorting table.\nHer coat goes down with her.\n\nName: not recorded here.\nReceipt to the Sheriff's office.");
        case "Tags"->typed("Unclaimed property","Ridge Depot",
            "UNCLAIMED\nSorting table\n\nHeld one year, then to the church sale.\n\n141-A: one bible, one pair of reading glasses.\n197-A: wedding band.\n230-A: bus ticket, unused.",
            "263-A: teal pack. HOLD.\nThe Sheriff's office says not to sell.\n\nSale money to the county maintenance account.");
        case "Forms"->typed("Report forms","Drisking County Sheriff's office",
            "MISSING PERSON REPORT\n\nName:\nLast seen:\n\nFinding: RUNAWAY\nNo further action.",
            "The finding is printed on every form in the stack, before anything else is filled in.");
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
            "MISSING\nKAYLA, 19\nLast seen walking home from the store. Call the Sheriff's office, 555-0117.\n\nThe paper is new and still white.",
            "Under it, older and softer, another sheet. Only one word is still legible: KELLY. Under that, the pin holes of sheets nobody kept.",
            "PRESCOTT ORE SERVICES\nNight shift tonight. Trucks on the ridge road after dark.");
        if(visit==3)return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper Sunday, six o'clock.\n\nFor sale: girl's bicycle, hardly ridden. Ask inside.",
            "MISSING\nKELLY GARRITY, 12\nLast seen on the pine trail near the second marker. Teal backpack.",
            "The sheet has gone soft and pale. Someone has written CLOSED across the bottom in marker.",
            "PRESCOTT ORE SERVICES\nCounty maintenance contract renewed. Night work available.");
        return childhood("Lost: brown dog, answers to Biscuit.");
    }
    /** The board of the two childhood visits. Its first sheet ends with the dog's notice, or with where that notice was. */
    private static ItemStack childhood(String dog){
        return typed("The noticeboard","Drisking",
            "DRISKING\n\nChurch supper Sunday, six o'clock. Bring a dish.\n\n"+dog,
            "CURFEW\nChildren off the trails by dusk.\nSheriff's office",
            "PRESCOTT ORE SERVICES\nNight work at the ridge depot. Good pay. Ask at the yard gate.\nFamily owned since the old mine.",
            "MISSING\nDANA PRUITT, 15\nLast seen on the pine trail.\n\nThe sheet is sun-faded. A newer flyer for a bake sale is pinned over half of it.",
            "Behind the curfew notice, the curled corner of a much older poster: a girl's school photograph, the paper gone brown. The name has worn away.");
    }
    /**
     * 0.4.462: the board on the second visit once Biscuit has been brought home (BorrascaDogs): his notice is down. It is its own
     * original, so a reader who read the board the day before keeps that copy as it was.
     */
    public static ItemStack boardNoDog(){return childhood("A pin hole in a paler square of cork, where a notice was.");}
    public static ItemStack board(int visit,boolean noticeDown){return noticeDown&&visit==2?boardNoDog():board(visit);}
    public static String boardKey(int visit,boolean noticeDown){return noticeDown&&visit==2?"BoardNoDog":boardKey(visit);}
    /** 0.4.451: the board after the ending, when the whole front of the store has been papered over. It is its own original. */
    public static ItemStack boardAfter(){return typed("The noticeboard","Drisking",
        "DRISKING\n\nEvery sheet on the board says LOST. They are pinned across the frame and across each other.",
        "The pins ran out. The newest are tucked under the corners of the ones before.",
        "On top, Kayla.\nUnder her, Kelly.\nUnder Kelly, Dana.\n\nUnder Dana the paper is brown, and the photographs are school photographs.");}
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
    /** 0.4.436: quarters spent at the drugstore's cabinets, and whether the reader's initials went over Patrick's. */
    private static String arcade(CompoundTag own){
        int plays=own.getInt("ArcadePlays0")+own.getInt("ArcadePlays1");if(plays==0)return "";
        return (plays==1?"I put a quarter in one of the games at the drugstore.":"I spent my quarters on the games at the drugstore.")+(own.getBoolean("OverPatrick")?" I got my initials over Patrick's.":"")+BorrascaArcadeFriends.account(own)+" ";
    }
    /** 0.4.435: whose bike this reader rode, and whether they came off. */
    private static String bikes(CompoundTag own){
        boolean his=own.getBoolean("BikeRode"+BorrascaBike.PATRICK),hers=own.getBoolean("BikeRode"+BorrascaBike.KENDRA);if(!his&&!hers)return "";int falls=own.getInt("BikeFalls");
        return (his&&hers?"I rode Patrick's bike and Kendra's around town.":his?"I rode Patrick's bike around town.":"I rode Kendra's bike around town.")
            +(falls==1?" I came off once, and it went down in the road. ":falls>1?" I came off more than once. ":" ");
    }
    /** The account, from what this reader did and heard. */
    public static String account(){return account(new CompoundTag());}
    public static String account(CompoundTag own){
        return (own.getBoolean("FoundNell")?"She called out which way to look when we played hide-and-seek. ":"")
            // 0.4.433: the two who came part of the way.
            +(own.getBoolean("AlongHeard")?"Patrick and Kendra came with us as far as the first marker. Patrick said he would go up by the second one some other time. ":"")
            // 0.4.462: the dog in the trees by the first marker.
            +BorrascaDogs.told(own)
            // 0.4.435: the bikes.
            +bikes(own)
            // 0.4.436: the drugstore's games.
            +arcade(own)
            // 0.4.461: the church's supper being set out, if the reader was at it.
            +BorrascaSupper.told(own)
            // 0.4.432: what she said on the walk up.
            +(own.getInt("WalkTalk")>=BorrascaStory.WALK_TALK.length?"She talked the whole way up to the Tree, and asked if I wanted to be best friends. ":own.getInt("WalkTalk")>0?"She talked on the way up to the Tree. ":"")
            // 0.4.445: the comic she left in it, before the ridge was heard.
            +(own.getBoolean("SharedWindow")?"In the fort she let me pick a window. ":"")+BorrascaComic.left(own)+(own.getBoolean("SharedWindow")?"We were still up there when the ridge rang. ":"")
            +"We cut our names into the three trees, mine beside hers."+(own.getBoolean("KnifeTaken")?" She lent me her knife to cut mine."+(own.getBoolean("KnifeKept")?" I nearly went home with it.":""):"")+(own.getInt("WalkTalkTwo")>=BorrascaStory.WALK_TALK_TWO.length?" The next time, on the way to the mill, she told me about Patrick and made me promise to run home if anything happened.":own.getInt("WalkTalkTwo")>0?" The next time, on the way to the mill, she told me about Patrick.":"")
            +(own.getBoolean("Sheltered6")||own.getBoolean("Sheltered10")?" When the noise came she pulled me down behind the timber.":"")
            +(own.getBoolean("SawGentleman")?" Past the second marker something tall stood in the pines and shone, and when the noise stopped it was gone.":"")
            // 0.4.440: going on past the second marker, and what came for the child there.
            +(own.getInt("Swallowed")>0?(own.getInt("Swallowed")>1?" More than once":" Once")+" I went on past the second marker. Each time the noise came it was nearer, and so was it, until it stood right over me and bent down. Then I was under the Triple Tree with my back to the trunk.":"")
            // 0.4.428: the historical society, got into as a child.
            +(own.getInt("SocietyChild")>0?(own.getInt("SocietyChild")==1?" While Mr. Rowe smoked out front of his shop, I went into the historical society behind it.":" Behind Mr. Rowe's back I got into the historical society.")
                +" On a map of the old workings someone had drawn a small square over the top shaft and written DEPOT."+(own.getBoolean("CaughtInSociety")?" He came back in and sent me out.":"")
                :own.getBoolean("CaughtInSociety")?" Mr. Rowe found me in the back room of his shop and sent me out.":"")
            // 0.4.459: the letter Mr. Dorsey gave for her family, that week and years on.
            +BorrascaGarritys.carried(own)
            +" She was beside me when the light came. Men walked down out of it, and then she was gone. I came back grown, and someone had freshly cut her name away."
            +BorrascaGarritys.found(own)
            +(own.getBoolean("HeardDenial")?" The Sheriff knew which marker we had reached. Nobody had ever told him.":" The Sheriff was still at the second marker, the same man, older.")
            +(own.getBoolean("Read_History")?" A framed page at the historical society said what Borrasca meant: the first mine to give out, the one that gives nothing back. The town's water had run red for two years, and after that the town grew every year.":"")
            +(own.getBoolean("Read_WrenPoster")?" Kendra had gone looking for Patrick. Her face was on the board too.":own.getBoolean("WrenRememberedEli")?" Kendra walked Patrick's bike back to his mom.":"")
            +" At the ridge, his office's papers and Prescott's signature lay beside her things."
            +(own.getBoolean("LowerRooms")?" Under the depot there were rooms with the bolts on the outside.":"")
            +(own.getBoolean("NellsRoom")?" In one of them her name was scratched into the wall beside mine."+(own.getBoolean("KellyFound")?" She was there, grown, and would not come up.":""):"")
            +BorrascaComic.asked(own)
            +(own.getBoolean("KellyAsked")?" She asked me to stop the machine, and to take her name up instead of her.":"")
            +(own.getBoolean("MarasRoom")?" The newest room was made up, with a coat from town on the hook.":"")
            +(own.getBoolean("LedgerRead")?" A book kept the nights there by room number: who came down and when, approved by the Sheriff's office and counted by a Prescott, father and son. Nobody came down until they were grown. What was born down there went up to town. Beside her number there were two births, both placed in town.":"")
            +(own.getBoolean("LetterRead")?" Someone down there had made a note home out of letters cut from catalogues. It was never sent.":"")
            +(own.getBoolean("GravesSeen")?" Behind the shed there were numbers in the ground.":"")
            // 0.4.427: what the reader did to the Sheriff, grown.
            +(own.getBoolean("SheriffDead")?" I killed the Sheriff on the ridge. He talked until he couldn't, and none of it was sorry.":own.getBoolean("SheriffBeaten")?" I beat the Sheriff down into the dirt and left him there, still talking.":"")
            // 0.4.439: the woman from room 2 on the belt, and whether the breaker came in time.
            +(own.getBoolean("DanaSaved")?" A woman was lying on the belt when I came up to the yard, two men standing by it to watch her go. I pulled the breaker before it took her in. She stayed where they had put her, looking up, and never once looked at me."+(own.getBoolean("DanaIgnored")?" I held out my hand. She didn't take it.":"")
              :own.getBoolean("DanaLate")?" A woman was lying on the belt when I came up to the yard, two men standing by it to watch her go. I pulled the breaker when it already had "+(own.getInt("DanaLateAt")<400?"her feet.":own.getInt("DanaLateAt")<800?"her legs.":"half of her.")+" She went quiet a little after the belt stopped."
              :own.getBoolean("DanaFed")?" A woman was lying on the belt when I came up to the yard, two men standing by it to watch her go. One of them said the Gentleman would look after her now. It took her into the machine feet first. It took a long time. She screamed until it had her chest. Pieces of her were coming out of the far end before it had her head."+(own.getBoolean("GentlemanAtMachine")?" For a second the machine wasn't there. "+(own.getBoolean("SawGentleman")?"The tall thing from the pines was":"Something tall and shining was")+", bent over the belt, its face split open around her.":"")+(own.getBoolean("BandRead")?" On the pile at the far end there was a felt slipper and the band from her wrist: 141-A.":" On the pile at the far end there was a felt slipper.")
              :"")
            +(own.getBoolean("DanaSaved")||own.getBoolean("DanaLate")?"":" I stopped the machine.")
            +(own.getBoolean("HeardSheriffShutdown")&&!own.getBoolean("SheriffDead")?" The Sheriff said he would make the call.":"")
            +" The wind was still in the pines."
            +(own.getBoolean("NameReturned")||own.getBoolean("RestoredName")?" "+restored():" I carried her name out in the cloth.");
    }
    public static ItemStack accountBook(String reader,CompoundTag own){return book("The name beside mine",reader,account(own));}
    public static String restored(){return "I cut her name beside mine again. The gouges were still there.";}
    public static String witness(CompoundTag own){return WitnessAccount.Story.BORRASCA.text+(own.getBoolean("SheriffDead")?" The Sheriff never came down off the ridge.":"")+(own.getBoolean("SharedWindow")?" We picked out a window before the ridge rang.":"")+(own.getBoolean("LedgerRead")?" The night book under the depot had the Sheriff's office on every page.":"")+(own.getBoolean("Read_WrenPoster")?" Kendra went looking for Patrick. Both names were on the store wall.":"")+(own.getBoolean("SheriffYielded")?" The Sheriff put his light down and let me pass.":own.getBoolean("HeardSheriffShutdown")?" When the machine stopped, he was still there.":"")+(own.getBoolean("NameReturned")?" "+restored():"");}
}
