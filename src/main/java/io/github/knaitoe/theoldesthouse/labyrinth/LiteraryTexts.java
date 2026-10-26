package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
/** Original fictional documents. No extracted novel passages or invented tribal history. */
public final class LiteraryTexts {
    private LiteraryTexts(){}
    private static ItemStack book(String title,String author,String... pages){return HouseWriting.book(title,author,HouseWriting.WritingStyle.WILL,List.of(pages));}
    public static ItemStack source(LabyrinthPlace p){if(UncannyRooms.isUncanny(p))return UncannyTexts.source(p);return switch(p){
        case BORRASCA->BorrascaTexts.source();
        case HILL_NURSERY->book("A mother's night book","O.",
            "Tuesday\n\nI measured the children's beds. Both will fit in the room at the end. The room has no outside wall. I think they will be warm there.",
            "Thursday\n\nHe says I am making a plan out of a dream. When I wake, I still know which stairs will take us away from morning.",
            "Sunday\n\nI heard them grown. One called for me. A mother should answer, even if the voice comes from years she has not lived.\n\nThe cellar answers a knock exactly. It has never asked who is there.");
        case MINIATURES->book("Workshop measurements","A.",
            "Keep the room small enough to hold in both hands. Paint the bed after the wall is dry. A figure makes the scale readable. Give it the visitor's face.",
            "The latest table came back with a second figure. I checked my photographs. There is no place for it to stand.\n\nI go back to each table three times before I trust it. The boards only take the shape of rooms somebody has really walked through.");
        case MASQUE->book("The last invitation","The steward",
            "Seven rooms have been opened for the guests. The clock remains in the last. The musicians are paid to stop whenever it speaks.",
            "At the chime, stand still. Let the dancers be still with you. Someone has counted the footsteps between the notes.\n\nThe clock can be stopped by hand.");
        case USHER->book("The vault register","R.",
            "We carried her down before the physician returned. My brother listened at the lid. He said the sound was the wood settling.",
            "I left the hinges oiled. A living person should not have to shout through a bad hinge.\n\nTonight I will close the lid for good, or leave it as it is. In the morning I will go down and see what I have kept in, or let out into the wing.");
        case WINCHESTER->book("Construction ledger","S. W.",
            "Pay the carpenters by the day. Do not ask them to estimate the end. The landing needs a room even if the room does not need a landing.",
            "The new door opens above the courtyard. There is hay below it. I have ordered another staircase.\n\nThe long switchback reaches the upper ledger. The short stair reaches only plaster.");
        case CHILD_ROOM->book("The bedtime card","Dad",
            "I left the little lamp on. Your mum says it makes the curtains look yellow.\n\nThe train was on the rug when I came in. I have put it back there.",
            "You used to lose things under that bed. I could never get my shoulder far enough in to fetch them.\n\nIf you hear us, wait until morning. We are asleep downstairs.");
        case CRIMSON_HALL->book("Three cylinders","The housekeeper",
            "The snow falls through the roof, then stays where it lands. A shovel finds the old red floor. We were told it was the clay beneath the house.",
            "Three women left cylinders in different rooms. Play all three on the hall's jukebox. Let each finish before replacing it.\n\nOne is upstairs. The balcony is still sound.");
        case BLY_ROUTE->book("A wet route","The gardener",
            "The boards dry by afternoon. Each morning the same narrow line is wet again: the door, the corridor, the room with the water.",
            "The cupboards have catches on the inside. Shut one when the dripping approaches. Wait until she has reached the lake room, then come out.\n\nDo not stand in her way.");
        case THREE_FINGERS->book("A page from a pocketbook","Sgt. Coyle",
            "Bought from a holy man at Ranipur for the price of a tin of tobacco. He would not touch it after. He said a man should not meddle with what is fated, and that those who do are sorry for it.",
            "Three men. Three wishes each. The first had his. I have had mine. I shan't write down what they were.\n\nIf it is in the fire, leave it in the fire.");
        case LAKE_HOUSE->book("Canoe rental log","The boathouse",
            "Green canoe, brown trim. Two paddles.\n\nSat. Out 9:10. In 6:40.\nSun. Out 8:55. In 7:15.\nSat. Out 9:00. In after dark. Paint off the bow.\nSun. Out 8:30.",
            "There is a culvert under the service drive. Kids lie down in the canoe and push through it with their hands. Please don't.\n\nThe water on the other side is not on the map. Whoever brings this canoe back, write down where you went.");
        case ELK_LOT->book("Till receipt","Last Stop",
            "Two drinks. One broken headlamp. Someone went out to move the trucks and left the till open.",
            "The light found an eye above the hood. Everybody had a reason to shout. Nobody had a reason to listen.\n\nThe field begins beyond the parked pickups.");
        case ELK_FAN->book("An unfinished repair","The tenant",
            "The light flickers at the same point in each turn. I brought the old folding ladder in from the shed. The catches still hold. There is a screwdriver and a replacement bulb in the tray.\n\nI keep my hands below the blades.",
            "From the top step the carpet seems farther away. Something lies between the turns. I cannot make it out from the chair.\n\nThe room was taped off when I came back. Whoever did it had already gone.");
        case MAPPING_INTERIOR->book("A floor plan","The older brother",
            "The trailer ends at the back bedroom. I know because I have walked around it. Under the hatch, the dirt keeps going.\n\nMy brother sleeps through breakfast now.",
            "I saw someone cross the kitchen. He looked more like a shadow than a man. Each time I return, I can make out another part of his face.",
            "His things are below the house. Break the pile, or take what he left. I do not know which one means letting him go.");
        // 0.4.101: the child's own words; the meat and the trousers are noticed, not yet understood.
        case HOLY_RABBIT->book("The hollow","A child",
            "He put me where the needles kept the snow out. His shoulders would not fit. He said we would wait for morning.\n\nMy own food was hard as the ground.",
            "He came back with the rabbit already open and told me not to chew.\n\nHis trousers were wet to the hip. He said the creek was good news: follow water and you find people.",
            "He flicks my cheek when my eyes close. He says sleeping is how the snow gets you.\n\nThis morning Slaney was out by the snow again.\n\nWhen the snow lets us go, we go fast and don't stop.");
        case CONFESSION->book("Terms of the interview","The listener",
            "He asked for a chair and a book with blank pages. He did not ask for forgiveness. I thought this meant the account would be easier to take down.",
            "He stops when I leave. When I return, he finds the exact word.\n\nThere are clean books on the low shelf. I put one on the table between our chairs. By the time I reached the last page, I recognised the handwriting.");
        // 0.4.50: found aboard the yacht. Readers who took the earlier note keep their own saved original.
        case ELK_CARCASSES->book("Morning aboard","A deckhand",
            "Nobody rang for breakfast. The party was still on the tables, and the doors below stood open. Nobody answered from any of them.",
            "He is still aboard. Do not wait for the boat. Go over the side and make for the trees.",
            "The road crew dragged their elk to a cut under the ridge. There is a hollow under the pile. Watch the boots through the gap and do not move until they are gone.\n\nThe crew's path runs on from the cut to their gate.");
        // 0.4.103: Hank's card for the town's Lake Witch contest; he has judged it every year since the shallows.
        case COSTUME_NIGHT->book("Judge's card","Hank, for the committee",
            "LAKE WITCH CONTEST\nJudge's card\n\nTwelve entered this year. They wear their numbers pinned on. Score each one out of ten.",
            "What I look for: a good one holds still when you look at it. She always did. You look away a moment, and it's closer.",
            "Keep them on the sand.\n\nIf it ever comes to anything, the park grass. She never would cross it.\n\nH.");
        // 0.4.102: the film plays on a screen moored out on the lake; the program is the projectionist's.
        case MOVIE_NIGHT->book("Tonight on the lake","Ray, the projectionist",
            "TEETH OF SUMMER\n\nThe screen stands on a raft out on the lake, so you watch it from the water. Take a canoe from the dock.",
            "The barge sends up a burst every little while, and it lights the whole lake. Look for her when it does. While the light is on her, she cannot move.",
            "Don't sit in the beam. Your head goes up on the screen.\n\nIf the speakers cut out, she is close. Row.\n\nThe far pier is ours. She can't stand on it.");
        case WINTER_LAKE->book("A note passed in class","Bud",
            "Tonight at the lake. Hank still has her ribbon. Whoever walks it out to the hole where she went down, that ends it.\n\nWalk soft. Dad says sound carries a mile on ice.\n\nBud");
        case CAMP_BLOOD->book("Last camp essay","J.",
            "A cabin is safe only while you know where its doors are. A path through the woods is safe only while somebody else is willing to be first.",
            "The killer follows noise and the last place he saw you. Put timber between you and him. Keep moving when he loses the line.\n\nThe service road is behind the last cabins.");
        case DEVILS_ROCK->book("The watch log","The mother",
            "I slept by the bedroom door. In the morning there were two pages on the floor. The paper was dry. The window was shut.",
            "I tried string, plates and a camera. Each showed that something happened, after it had happened.\n\nNow I leave each page where it lands and read it there. Another comes when I have stopped looking. It took three nights for the last of them.");
        case WHEEL->book("House numbers","The daughter",
            "Room two comes after room one. I wrote that so I would not forget. When I went back through two, the chair in one was smaller.",
            "The front door takes me to the beginning. Mother's room is farther in than the numbers suggest.\n\nWhen I found her, I wanted none of the numbered doors after that. I left by the one with no number, and I was my own height again.");
        case GHOSTS_SET->HouseWriting.book("Call sheet","The producer",HouseWriting.WritingStyle.WILL,GhostsSet.pages(
            "The Possession. Interior. Camera operator: collect your camera and speak with Barry. Put the camera away to talk to the family. Earlier incidents are reconstructed from their accounts.\n\n"+
            "Hold use to record where you look. Crouch beside the dinner table to take a low shot. The monitor plays your recorded frames; Barry keeps the cut and the camera log.\n\nWhen filming ends, leave through the door. The following day begins only on a later return. An unfinished day waits for you. The booth keeps your own words, or your chosen silence."));
        case END_WORLD_CABIN->book("Guest book","Those who stayed",
            "June\n\nLoons at dusk. A storm sat on the far shore all week and never crossed. The kids counted four people down on the beach at sundown. Nobody we'd seen in town. They waved as if they knew us.",
            "August\n\nLovely week. Someone knocked the last night, late, and kept on. We didn't answer. In the morning there were wet footprints out along the jetty, and none coming back.",
            "No date\n\nThe storm is on this side of the lake now. They were very polite. They said it had to be my choice, and it was.\n\nI kept everything I came with. I can't stop looking at the jetty.");
        case FAMILY_COPY->book("A familiar address","The visitor",
            "I remembered putting that window in. I also remembered taking it out. It was there when the father answered the door.",
            "I knocked and asked, and they let me in and showed me how they remembered the rooms. They remembered a little more each time I came back. It was eight visits before the welcome ended. It was a house I had slept in.");
        case OLD_CABIN->book("A bed address","The old man",
            "This was the first bed the House recorded for you. It cannot remember a night it did not see.\n\nI have had many names. The wood has had only this shape.",
            "Bring the weapon you have used most. I can tell whether it has a past, or whether the House has made another one.\n\nThe lamp speaks my name slowly.");
        default->throw new IllegalArgumentException(p.id());};}
    public static ItemStack ending(ServerPlayer p,LabyrinthPlace place,CompoundTag own){return ending(p.getGameProfile().getName(),place,own);}
    public static ItemStack ending(String name,LabyrinthPlace place,CompoundTag own){if(UncannyRooms.isUncanny(place))return UncannyTexts.account(name,place,own);if(place==LabyrinthPlace.BORRASCA)return BorrascaTexts.accountBook(name,own);if(place==LabyrinthPlace.END_WORLD_CABIN&&own.getBoolean("Bargain0451"))return cabin(name,own);String text=switch(place){
        case HILL_NURSERY->"I knocked "+own.getInt("Knocks")+" times. The cellar returned each one. Beside the cold door hung a photograph of the two children grown, in clothes from years the house had not reached. The diary called the same thing protection that I had called a locked room.";
        case MINIATURES->"The tables held rooms I had walked through, a little figure standing where I had stood. In the latest, a second figure stood just behind mine, close enough to touch its shoulder. I had crossed that room alone.";
        case MASQUE->"I put my hand on the clock. The next chime did not come. Seven rooms of people had been waiting for it; now there was only the sound of my hand leaving the wood.";
        case USHER->own.getBoolean("LidClosed")?"I returned to the closed coffin. Scratching had crossed the stone along with the crack. I could no longer call it the wood settling.":"I left the lid open. When I returned, the coffin was empty. I found her in the wing, standing where there had been no chair.";
        case WINCHESTER->"The switchbacks brought me one floor up, to the upper ledger. Beside every room it kept a name, and none of the names were the builders'. The last name had no room yet. That was why the work could not stop.";
        case CHILD_ROOM->"The windows went first. Then the doors. I crawled beneath the bed and found a strip of floor that still led somewhere. I stood up only after I had left it.";
        case CRIMSON_HALL->"I played the three voices all the way through. Under the snow, the floor was red. A house can keep a voice much longer than it keeps a person.";
        case BLY_ROUTE->own.getBoolean("Dragged")?"The first time I stood where she walks and came up in the lake room's water. Then I shut a cupboard from inside. The catches are inside because a child once hid from her in one. I think she was looking for that child when she found me.":"I shut a cupboard from inside while the drips passed on their way to the lake room. The catches are inside because a child once hid from her in one. She has never opened one. I think she is still looking for that child.";
        case THREE_FINGERS->ThreeFingers.account(own);
        case LAKE_HOUSE->LakeHouse.account(own);
        case BORRASCA->BorrascaTexts.account();
        case ELK_LOT->ElkLotEnding.account(name,own);
        case ELK_FAN->"From above the ladder I could see what lay below the moving blades. I stayed up there longer than I meant to. The blade caught me; when I could see again I was outside the room. A tooth lay in my hand. When I returned, tape held the shape of the deer.";
        case MAPPING_INTERIOR->own.getBoolean("PileBroken")?"I broke the pile under the trailer. The boy woke when I came back. The crawlspace still ran beyond the walls, but the things at its end could no longer hold him there.":"I took what lay under the trailer. The photograph came with me. When I returned, the boy was harder to wake. Keeping a father's things was not the same as keeping a brother.";
        case HOLY_RABBIT->rabbitAccount(own);
        case CONFESSION->"The last chapter addressed the person holding the pen.\n\n"+own.getString("Statistics")+"\n\n"+confessionLast(own)+"The signature was mine. The chair opposite me had not changed.";
        case ELK_CARCASSES->"I held still beneath the hides. His boots passed the opening twice and did not slow: nobody looks twice at a pile of things already dead. When the boots stopped coming back, I followed the service path.";
        case COSTUME_NIGHT->CostumeNight.account(own);
        case MOVIE_NIGHT->MovieNight.account(own);
        case WINTER_LAKE->WinterLake.account(own);
        case CAMP_BLOOD->"I read the essay, crossed the cabins and lost him among the trees. The service road had no gate. I had spent the night looking for one.";
        case DEVILS_ROCK->"I laid the pages in order. The camera had times for each arrival, but never a face. The last page asked me to tell him the path was still there. It did not say who he was, or how the paper had entered the room.";
        case WHEEL->"I reached Mother's room. She was the only thing in the house still the size I remembered; every room before hers had measured me against her. The front door opened onto an outside I could stand up in. I kept the key.";
        case GHOSTS_SET->own.getBoolean("Filming0485")?GhostsSet.account(name,own):"TRANSCRIPT\n\n"+(own.getString("Confession").isEmpty()?"[The booth recorded silence.]":own.getString("Confession"))+"\n\nThe lights had shown a family waiting on its marks. Off the carpet, another conversation had gone on behind my back.";
        case END_WORLD_CABIN->own.getBoolean("Offered")?"I chose what went to the keeper. The visitors thanked me by name. On her shelf it would remain exactly what I gave up. I could see it; I would not carry it again.":"I refused. One visitor walked into the lake. On the screen, "+StaircaseProse.place(own.getString("ClosedRoom"))+" went dark. My hands were still full.";
        case FAMILY_COPY->"They knew my rooms better than I did: which stair complained, where the window had been. The weapon on their wall had my old scratches, but none of its days. When the welcome ended, the father thanked me for minding the place while they were away.";
        case OLD_CABIN->"Three times the lamp finished a name, and three times I said it back. None of them was his. He had taken his sword down from a kitchen wall that looked like his own, every nick in place. He never found out where his own had gone.";
        default->throw new IllegalArgumentException(place.id());};if(place==LabyrinthPlace.GHOSTS_SET&&own.getBoolean("Filming0485"))return HouseWriting.book("The tape and the cut",name,HouseWriting.WritingStyle.WILL,GhostsSet.pages(text));
        // 0.4.105: each account ends on its own last line. The shared folded-paper afterword closed some twenty-five of them.
        if(place==LabyrinthPlace.HOLY_RABBIT)return book(WitnessAccount.Story.HOLY_RABBIT.title,name,GhostsSet.pages(text).toArray(String[]::new));
        String title=switch(place){case MOVIE_NIGHT->"The account: after the show";case COSTUME_NIGHT->"The account: the ribbon";case WINTER_LAKE->"The account: the dare";default->accountTitle(place);};
        // The longer generated accounts are paged (Confession's count is written out now); every other keeps its one page.
        boolean paged=place==LabyrinthPlace.CONFESSION||place==LabyrinthPlace.MOVIE_NIGHT||place==LabyrinthPlace.COSTUME_NIGHT||place==LabyrinthPlace.WINTER_LAKE||place==LabyrinthPlace.ELK_LOT;
        return paged?book(title,name,GhostsSet.pages(text).toArray(String[]::new)):book(title,name,text);}
    /** 0.4.105: the rule a source paper used to state outright, given once in plain words after its paper is read; the paper stays in its writer's voice. */
    public static String hint(LabyrinthPlace place){return switch(place){
        case MINIATURES->"Examine the models on three separate visits.";
        case USHER->"Close the coffin lid or leave it open, then come back on a later visit.";
        case DEVILS_ROCK->"Read each loose page where it lies. More appear over three visits.";
        case WHEEL->"Find Mother's room, then leave by the door with no number.";
        case FAMILY_COPY->"Knock and ask to be let in. The family's welcome lasts eight visits.";
        default->null;};}
    /** 0.4.105: an account is titled with its room's prose name, never its internal id. A book title holds 32 characters, so the longest names are shortened. */
    public static String accountTitle(LabyrinthPlace place){
        if(UncannyRooms.isUncanny(place))return UncannyTexts.title(place);
        String title="The account: "+StaircaseProse.place(place.id());if(title.length()<=32)return title;
        return "The account: "+switch(place){case HILL_NURSERY->"the nursery";case MINIATURES->"the workshop";case ELK_FAN->"the fan room";case MAPPING_INTERIOR->"the crawlspace";
            case ELK_CARCASSES->"the ridge";case END_WORLD_CABIN->"the cabin";case FAMILY_COPY->"the copied home";case THREE_FINGERS->"the cottage";case LAKE_HOUSE->"the lake house";default->"the room";};
    }
    /** 0.4.101: what the child saw, in order, with the rabbit's own name; the carving only if they looked up at it. */
    static String rabbitAccount(CompoundTag own){
        String rabbit=own.getString("RabbitName").isBlank()?"Slaney":own.getString("RabbitName");
        return rabbit+" was by the snow every morning. Every night the meat came in the same skin. Father said he had been through the creek again. His tracks never went near it. Both his legs were dark to the hip.\n\n"
            +(own.getBoolean("CarvingRead")?"He had cut my initials into the tree beside my mother's name, and under them a little rabbit with "+rabbit+" at its feet. He had gone round her name so many times the bark nearly came away.\n\n":"")
            +"A searcher found me with a light and a radio. Back in the trees, Father got the poles under his arms and went the other way. One of the men asked where he was going. I said hunting.\n\n"
            +"Years later a rabbit sat in the flowers outside a window, brown at the tips, and I kept my mouth shut tight the way I had under that tree, so I would not lose anything he gave me.";
    }
    /** The bargain's account (0.4.51), in the reader's own words and the room's prose name. */
    static ItemStack cabin(String name,CompoundTag own){
        int given=own.getInt("Given");String room=own.getString("ClosedRoom");
        if(own.getBoolean("Sacrificed"))return book("An account of the visitors",name,
            "Leonard asked for my life, and I gave it. It cost a heart. Adriane asked for more, and it cost another. Sabrina tied a cord above my elbow and told me to look at her, not at it.",
            "I looked at her.\n\nWhen I could see again, the rain had stopped. They walked down the jetty together, and the lake closed over them like a door.",
            "On the screen my roof was dry.\n\nThe world in the glass did not end. It is still snowing in it, a little.");
        String asked=given==0?"Leonard asked for my life. I said no. Leonard said that was all right,":given==1?"Leonard asked for my life, and I gave it. It cost a heart. When Adriane asked for more, I said no. Adriane told me to look after what I had left,":"I gave two hearts. When Sabrina asked for my arm, I said no. Sabrina told me to keep my hands,";
        return book("An account of the visitors",name,
            asked+" walked down the jetty in the rain, and did not stop at the end.",
            (room.isEmpty()?"On the screen a room went dark.":"On the screen, "+StaircaseProse.place(room)+" went dark. I will not find it again.")+(given==0?" I still have every heart.":" What I gave stays given."),
            "The glass they left me is cracked.");
    }
    public static List<ItemStack> specimens(){var all=new ArrayList<ItemStack>();for(String key:List.of("Bedroom","Register","Map","Service","Evidence","Testimony"))all.add(BorrascaTexts.document(key));for(int visit:new int[]{1,3,4})all.add(BorrascaTexts.board(visit));all.add(BorrascaTexts.notes(false));all.add(BorrascaTexts.notes(true));for(var place:LabyrinthPlace.values())if(LiteraryRooms.isLiterary(place)){all.add(source(place));if(UncannyRooms.isUncanny(place)){for(int i=0;i<5;i++)all.add(UncannyReading.excerpt(place,i,new CompoundTag()));var hall=new CompoundTag();hall.putString("FirstView","hall");all.add(UncannyReading.excerpt(place,3,hall));}var own=new CompoundTag();own.putString("Statistics",reckoning(999999,999999,999999,999999));own.putString("Confession","I came back because I remembered where I had left the light.");own.putString("ClosedRoom","wallpaper_nursery");all.add(ending("Reader_1234567890",place,own));if(place==LabyrinthPlace.USHER||place==LabyrinthPlace.MAPPING_INTERIOR||place==LabyrinthPlace.END_WORLD_CABIN){own.putBoolean("LidClosed",true);own.putBoolean("PileBroken",true);own.putBoolean("Offered",true);all.add(ending("Reader_1234567890",place,own));}}for(int given=0;given<=3;given++){var cabin=new CompoundTag();cabin.putBoolean("Bargain0451",true);cabin.putInt("Given",given);cabin.putBoolean(given==3?"Sacrificed":"Refused",true);cabin.putString("ClosedRoom","winchester");all.add(ending("Reader_1234567890",LabyrinthPlace.END_WORLD_CABIN,cabin));}var movie=new CompoundTag();for(String fact:List.of("ShadowSeen0502","WitchShadow0502","Quiet0502","SharkSeen0502","Struck0502","MetDot0502"))movie.putBoolean(fact,true);movie.putInt("Missing0502",15);all.add(ending("Reader_1234567890",LabyrinthPlace.MOVIE_NIGHT,movie));var costume=new CompoundTag();for(String fact:List.of("Noticed0503","HankStory0503","Shallows0503","Struck0503"))costume.putBoolean(fact,true);all.add(ending("Reader_1234567890",LabyrinthPlace.COSTUME_NIGHT,costume));
        var winter=new CompoundTag();for(String fact:List.of("Shallows0504","Lured0504","SawHer0504","Knocked0504","FellThrough0504","CameUp0504"))winter.putBoolean(fact,true);all.add(ending("Reader_1234567890",LabyrinthPlace.WINTER_LAKE,winter));for(int i=0;i<8;i++)all.add(diary(i));for(int i=0;i<3;i++)all.add(cylinder(i));return all;}
    public static String counts(ServerPlayer p){return reckoning(p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.VILLAGER)),p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.WOLF)),p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.IRON_GOLEM)),p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.WANDERING_TRADER)));}
    /**
     * 0.4.105: the last chapter's count, written out by the listener rather than tabled: only what there is to count, in
     * words, and its own page for a listener whose hands stayed empty after chapters that accused them.
     */
    public static String reckoning(int villagers,int wolves,int golems,int traders){
        int[] n={villagers,wolves,golems,traders};String[] one={"villager","wolf","iron golem","wandering trader"},many={"villagers","wolves","iron golems","wandering traders"};
        var parts=new ArrayList<String>();for(int i=0;i<n.length;i++)if(n[i]>0)parts.add(number(n[i])+" "+(n[i]==1?one[i]:many[i]));
        if(parts.isEmpty())return "I counted with my own hands, and they stayed empty: no villager, no wolf, no iron golem, no trader. He looked at the blank lines for a long time. Then he said the chapter had been written for somebody else, and that he was glad it had not been me.";
        String list=parts.size()==1?parts.getFirst():String.join(", ",parts.subList(0,parts.size()-1))+" and "+parts.getLast();
        return "I counted with my own hands: "+list+". He said nothing about the number. He wrote it under the chapter, where a name would go.";
    }
    private static String number(int n){String[] words={"one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve"};return n>=1&&n<=12?words[n-1]:Integer.toString(n);}
    /** 0.4.121: the account ends on the reader's own last answer, or on the line that says they gave none. */
    static String confessionLast(CompoundTag own){String last=ConfessionAnswers.last(own);if(last.isEmpty())return "";return last.equals(ConfessionAnswers.SILENCE)?"Under his last question, the line said I did not answer.\n\n":"Under his last question, in my hand: \u201c"+(last.length()>90?last.substring(0,90)+"\u2026":last)+"\u201d\n\n";}
    public static final String[] CONFESSION={"I came to a town that had room for another name. ","I learned which doors would open when I knocked, and which people would step aside. ","I mistook those things for permission. ","The first account left out everyone who could no longer answer it. ","I kept a chair for a listener. I thought the distance between our chairs would keep the story mine. ","Now I want you to put down the pen and count with your own hands. ","There are names the world remembers without a witness. There are numbers it remembers without a name. ","This chapter belongs to the person who has been writing. "};
    public static ItemStack diary(int i){String[] pages={"We went up to the rocks before dark. I told him the path would still be there when we came back.","He asked whether a missing person knows they are missing. I laughed because I thought it was a joke.","The house was lit when we got home. There was one chair nobody sat in.","I left a page by the door so someone would know where I had stopped looking.","I heard the floor from the wrong side. I could not make it happen a second time.","If the camera saw me, it must have seen the room I was in. The time on the print is later than the time on my watch.","I have put the pages down where you can find them. I do not know how to put myself there.","This is the last page. If you see him, tell him the path was still there. I went back for it, and it was."};return book("Loose diary page "+(i+1),"T.",pages[Math.floorMod(i,pages.length)],"The paper is dry. There is no fold where it would have fitted through the door.");}
    public static ItemStack cylinder(int i){return book("Cylinder transcript "+(i+1),"A former wife",new String[]{"I thought the roof was being repaired. The snow came through it the next winter too. I learned to move my chair before it fell.","He kept the house warm in the rooms where people were expected. I walked farther to find a room where I could speak without being heard.","I have left this voice where a hand might find it. Listen to the whole cylinder. Do not let him tell you what I meant."}[i]);}
}
