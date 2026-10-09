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
    public static ItemStack source(LabyrinthPlace p){return switch(p){
        case HILL_NURSERY->book("A mother's night book","O.",
            "Tuesday\n\nI measured the children's beds. Both will fit in the room at the end. The room has no outside wall. I think they will be warm there.",
            "Thursday\n\nHe says I am making a plan out of a dream. When I wake, I still know which stairs will take us away from morning.",
            "Sunday\n\nI heard them grown. One called for me. A mother should answer, even if the voice comes from years she has not lived.\n\nThe cellar answers a knock exactly. It has never asked who is there.");
        case MINIATURES->book("Workshop measurements","A.",
            "Keep the room small enough to hold in both hands. Paint the bed after the wall is dry. A figure makes the scale readable. Give it the visitor's face.",
            "The latest table came back with a second figure. I checked my photographs. There is no place for it to stand.\n\nExamine a model on three separate visits. The boards remember rooms you have actually entered.");
        case MASQUE->book("The last invitation","The steward",
            "Seven rooms have been opened for the guests. The clock remains in the last. The musicians are paid to stop whenever it speaks.",
            "At the chime, stand still. Let the dancers be still with you. Someone has counted the footsteps between the notes.\n\nThe clock can be stopped by hand.");
        case USHER->book("The vault register","R.",
            "We carried her down before the physician returned. My brother listened at the lid. He said the sound was the wood settling.",
            "I left the hinges oiled. A living person should not have to shout through a bad hinge.\n\nChoose the lid. Return to see what that choice has kept inside, or let into the wing.");
        case WINCHESTER->book("Construction ledger","S. W.",
            "Pay the carpenters by the day. Do not ask them to estimate the end. The landing needs a room even if the room does not need a landing.",
            "The new door opens above the courtyard. There is hay below it. I have ordered another staircase.\n\nThe long switchback reaches the upper ledger. The short stair reaches only plaster.");
        case CHILD_ROOM->book("The bedtime card","Dad",
            "You can leave the lamp on. If you need us, the door is where it always was.\n\nYour toys are not supposed to be up there.",
            "If the door has gone, look below the bed. There is room to crawl. You do not have to ask whatever is speaking from the ceiling.");
        case CRIMSON_HALL->book("Three cylinders","The housekeeper",
            "The snow falls through the roof, then stays where it lands. A shovel finds the old red floor. We were told it was the clay beneath the house.",
            "Three women left cylinders in different rooms. Play all three on the hall's jukebox. Let each finish before replacing it.\n\nOne is upstairs. The balcony is still sound.");
        case BLY_ROUTE->book("A wet route","The gardener",
            "The boards dry by afternoon. Each morning the same narrow line is wet again: the door, the corridor, the room with the water.",
            "The cupboards have catches on the inside. Shut one when the dripping approaches. Wait until she has reached the lake room, then come out.\n\nDo not stand in her way.");
        case ELK_LOT->book("Till receipt","Last Stop",
            "Two drinks. One broken headlamp. Someone went out to move the trucks and left the till open.",
            "The light found an eye above the hood. Everybody had a reason to shout. Nobody had a reason to listen.\n\nThe field begins beyond the parked pickups.");
        case ELK_FAN->book("An unfinished repair","The tenant",
            "The light flickers at the same point in each turn. The ladder is two rungs short. Bring ordinary blocks to make a platform; keep the blades above your hands.",
            "From up there you can see the carpet between the blades. Do not stay under them.\n\nCome back twice after the repair. The outline will change before the tooth appears.");
        case MAPPING_INTERIOR->book("A floor plan","The older brother",
            "The trailer ends at the back bedroom. I know because I have walked around it. Under the hatch, the dirt keeps going.\n\nMy brother sleeps through breakfast now.",
            "I saw someone cross the kitchen. He looked more like a shadow than a man. Each time I return, I can make out another part of his face.",
            "His things are below the house. Break the pile, or take what he left. I do not know which one means letting him go.");
        case HOLY_RABBIT->book("The hollow","A child",
            "He put me where the needles kept the snow out. His shoulders would not fit. He said we would wait for morning.\n\nMy own food was hard as the ground.",
            "He brought food down from above my head. The first time, there was a hide. Afterward there was only the food. His leg made a line when he walked.",
            "Stay awake. Eat what he brings, or wait for another night. When the trail opens, follow it quickly.\n\nThe white rabbit is still outside every morning.");
        case CONFESSION->book("Terms of the interview","The listener",
            "He asked for a chair and a book with blank pages. He did not ask for forgiveness. I thought this meant the account would be easier to take down.",
            "He stops when I leave. When I return, he finds the exact word.\n\nCarry a book and quill. The final chapter counts what the listener has actually done.");
        // 0.4.50: found aboard the yacht. Readers who took the earlier note keep their own saved original.
        case ELK_CARCASSES->book("Morning aboard","A deckhand",
            "Nobody rang for breakfast. The party was still on the tables, and the doors below stood open. Nobody answered from any of them.",
            "He is still aboard. Do not wait for the boat. Go over the side and make for the trees.",
            "The road crew dragged their elk to a cut under the ridge. There is a hollow under the pile. Watch the boots through the gap and do not move until they are gone.\n\nThe crew's path runs on from the cut to their gate.");
        case COSTUME_NIGHT->book("Costume inventory","The camp office",
            "Nine stands. Nine coats. We counted them before we turned out the lights.\n\nOne of the coats is not where its stand was.",
            "Walk toward the far grass. Watch what follows you. Living grass ends the chase. Sand does not.");
        case MOVIE_NIGHT->book("After the show","The projectionist",
            "We put the screen on the bank and the speakers in the trees. The fireworks were meant for the end. They are still going off, though there is nobody left to watch them.",
            "Take the canoe toward the far pier. Each flash shows the water for a moment. Keep rowing between flashes.\n\nThe bank is real.");
        case WINTER_LAKE->book("An ice report","The groundskeeper",
            "Snow covers the grass. The surface will bear her weight wherever it bears yours.\n\nA patch of open water would be a better place to wait.",
            "Break actual ice, go below the surface and let her pass. Then reach the far bank.\n\nDo not mistake a shallow hole for being submerged.");
        case CAMP_BLOOD->book("Last camp essay","J.",
            "A cabin is safe only while you know where its doors are. A path through the woods is safe only while somebody else is willing to be first.",
            "The killer follows noise and the last place he saw you. Put timber between you and him. Keep moving when he loses the line.\n\nThe service road is behind the last cabins.");
        case DEVILS_ROCK->book("The watch log","The mother",
            "I slept by the bedroom door. In the morning there were two pages on the floor. The paper was dry. The window was shut.",
            "I tried string, plates and a camera. Each showed that something happened, after it had happened.\n\nRead the pages where they appear. Three visits will bring the last of them.");
        case WHEEL->book("House numbers","The daughter",
            "Room two comes after room one. I wrote that so I would not forget. When I went back through two, the chair in one was smaller.",
            "The front door takes me to the beginning. Mother's room is farther in than the numbers suggest.\n\nFind her room, then choose the unnumbered door. Leave at your own height.");
        case GHOSTS_SET->book("Call sheet","The producer",
            "Family scene. Interior. Keep the camera off the loose boards. The marks are on the carpet. The lamps should come up when the guest reaches an X.",
            "Off the mark, the camera sees nothing useful.\n\nThe booth records the words you actually type. Read the transcript after the performance. Take the disc if you want to hear the room again.");
        case END_WORLD_CABIN->book("Guest book","Those who stayed",
            "June\n\nLoons at dusk. A storm sat on the far shore all week and never crossed. The kids counted four people down on the beach at sundown. Nobody we'd seen in town. They waved as if they knew us.",
            "August\n\nLovely week. Someone knocked the last night, late, and kept on. We didn't answer. In the morning there were wet footprints out along the jetty, and none coming back.",
            "No date\n\nThe storm is on this side of the lake now. They were very polite. They said it had to be my choice, and it was.\n\nI kept everything I came with. I can't stop looking at the jetty.");
        case FAMILY_COPY->book("A familiar address","The visitor",
            "I remembered putting that window in. I also remembered taking it out. It was there when the father answered the door.",
            "There are eight visits in the family's welcome. Knock, ask to enter, and learn how they remember the rooms. The house itself is a frozen copy of somewhere you actually slept.");
        case OLD_CABIN->book("A bed address","The old man",
            "This was the first bed the House recorded for you. It cannot remember a night it did not see.\n\nI have had many names. The wood has had only this shape.",
            "Bring the weapon you have used most. I can tell whether it has a past, or whether the House has made another one.\n\nThe lamp speaks my name slowly.");
        default->throw new IllegalArgumentException(p.id());};}
    public static ItemStack ending(ServerPlayer p,LabyrinthPlace place,CompoundTag own){return ending(p.getGameProfile().getName(),place,own);}
    public static ItemStack ending(String name,LabyrinthPlace place,CompoundTag own){if(place==LabyrinthPlace.END_WORLD_CABIN&&own.getBoolean("Bargain0451"))return cabin(name,own);String text=switch(place){
        case HILL_NURSERY->"I knocked "+own.getInt("Knocks")+" times. The cellar returned each one. Above it, somebody had written my name as if I lived there. The diary called the same thing protection that I had called a locked room.";
        case MINIATURES->"The tables held rooms I had walked through. Each little figure had my face. Behind the latest one there was room for somebody else, although I did not remember making that space.";
        case MASQUE->"I put my hand on the clock. The next chime did not come. Seven rooms of people had been waiting for it; now there was only the sound of my hand leaving the wood.";
        case USHER->own.getBoolean("LidClosed")?"I returned to the closed coffin. Scratching had crossed the stone along with the crack. I could no longer call it the wood settling.":"I left the lid open. When I returned, the coffin was empty. I found her in the wing, standing where there had been no chair.";
        case WINCHESTER->"The switchbacks brought me one floor up. The ledger bought another day of work. A door looked down on the place where I had begun; the short stair ended against plaster.";
        case CHILD_ROOM->"The windows went first. Then the doors. I crawled beneath the bed and found a strip of floor that still led somewhere. I stood up only after I had left it.";
        case CRIMSON_HALL->"I played the three voices all the way through. Under the snow, the floor was red. A house can keep a voice much longer than it keeps a person.";
        case BLY_ROUTE->"I shut the cupboard from inside. Drips passed the crack under its door. I waited for the last footstep to enter the lake room, then came out onto the same wet boards.";
        case ELK_LOT->"FIELD DEATH\n\nThe name in the clipping was "+name+". I remembered the lights in the parking lot and the eyes beyond it. The report gave the field an ending that I had not stayed to see.";
        case ELK_FAN->"From above the ladder, the blades kept removing parts of what lay below. On my next return, tape held the shape. On the third, there was only a tooth and the marks where the tape had been.";
        case MAPPING_INTERIOR->own.getBoolean("PileBroken")?"I broke the pile under the trailer. The boy woke when I came back. The crawlspace still ran beyond the walls, but the things at its end could no longer hold him there.":"I took what lay under the trailer. The photograph came with me. When I returned, the boy was harder to wake. Keeping a father's things was not the same as keeping a brother.";
        case HOLY_RABBIT->"The rabbit was alive each morning. After the first night there were no more hides. At the end of the drag trail I found the knife and a foot. I had eaten enough to reach them. He had left enough for me to leave.";
        case CONFESSION->"The last chapter addressed the person holding the pen.\n\n"+own.getString("Statistics")+"\n\nThe signature was mine. The chair opposite me had not changed.";
        case ELK_CARCASSES->"I held still beneath the hides. His boots passed the opening twice. When they no longer came back, I followed the service path. The pile stayed where the camp had put it.";
        case COSTUME_NIGHT->"The coats remained on the beach. I kept one of them in view until my feet found grass. It stopped with the sand still beneath it.";
        case MOVIE_NIGHT->"Each flash found it closer. I kept my hands on the paddle. The last flash came after the canoe reached the pier; the water was behind me.";
        case WINTER_LAKE->"I broke the surface and went below it. I watched her feet through the ice. After she passed, I came up through the hole I had made and crossed to the bank.";
        case CAMP_BLOOD->"I read the essay, crossed the cabins and lost him among the trees. The service road had no gate. I had spent the night looking for one.";
        case DEVILS_ROCK->"I laid the pages in order. The camera had times for each arrival, but never a face. The last page ended in this room. It did not tell me how the paper had entered it.";
        case WHEEL->"I reached Mother's room. The front door opened onto an outside I could stand up in. The numbers remained on the doors behind me. I kept the key without choosing another room.";
        case GHOSTS_SET->"TRANSCRIPT\n\n"+(own.getString("Confession").isEmpty()?"[The booth recorded silence.]":own.getString("Confession"))+"\n\nThe lights had shown a family waiting on its marks. Off the carpet, another conversation had gone on behind my back.";
        case END_WORLD_CABIN->own.getBoolean("Offered")?"I chose what went to the keeper. The visitors thanked me by name. On her shelf it would remain exactly what I gave up. I could see it; I would not carry it again.":"I refused. One visitor walked into the lake. On the screen, "+StaircaseProse.place(own.getString("ClosedRoom"))+" went dark. My hands were still full.";
        case FAMILY_COPY->"They had shown me my house as if they had lived there first. The family changed, and the photograph changed with it. The weapon had my old scratches, but none of its days. I left when the welcome ended.";
        case OLD_CABIN->"The lamp finished a name. The man beside it said another. He did not need either one to recognize what I had brought.";
        default->throw new IllegalArgumentException(place.id());};return book("The account: "+place.id().replace('_',' '),name,text,"I folded the paper along its old crease. Someone had carried it before there was anything written on this side.\n\nI could still hear the room while I put it away.");}
    /** The bargain's account (0.4.51), in the reader's own words and the room's prose name. */
    static ItemStack cabin(String name,CompoundTag own){
        int given=own.getInt("Given");String room=own.getString("ClosedRoom");
        if(own.getBoolean("Sacrificed"))return book("An account of the visitors",name,
            "Leonard asked for my life, and I gave it. It cost a heart. Adriane asked again, and it cost another. Sabrina tied a cord above my elbow and told me to look at her, not at it.",
            "I looked at her.\n\nWhen I could see again, the rain had stopped. They walked down the jetty together, and the lake closed over them like a door.",
            "On the screen my roof was dry.\n\nThe world in the glass did not end. It is still snowing in it, a little.");
        String asked=given==0?"Leonard asked for my life. I said no. Leonard said that was all right,":given==1?"Leonard asked for my life, and I gave it. It cost a heart. When Adriane asked again, I said no. Adriane told me to look after what I had left,":"I gave two hearts. When Sabrina asked for my arm, I said no. Sabrina told me to keep my hands,";
        return book("An account of the visitors",name,
            asked+" walked down the jetty in the rain, and did not stop at the end.",
            (room.isEmpty()?"On the screen a room went dark.":"On the screen, "+StaircaseProse.place(room)+" went dark. I will not find it again.")+(given==0?" I still have every heart.":" What I gave stays given."),
            "The glass they left me is cracked.");
    }
    public static List<ItemStack> specimens(){var all=new ArrayList<ItemStack>();for(var place:LabyrinthPlace.values())if(LiteraryRooms.isLiterary(place)){all.add(source(place));var own=new CompoundTag();own.putString("Statistics","Villagers: 999999\nWolves: 999999\nIron golems: 999999\nWandering traders: 999999");own.putString("Confession","I came back because I remembered where I had left the light.");own.putString("ClosedRoom","wallpaper_nursery");all.add(ending("Reader_1234567890",place,own));if(place==LabyrinthPlace.USHER||place==LabyrinthPlace.MAPPING_INTERIOR||place==LabyrinthPlace.END_WORLD_CABIN){own.putBoolean("LidClosed",true);own.putBoolean("PileBroken",true);own.putBoolean("Offered",true);all.add(ending("Reader_1234567890",place,own));}}for(int given=0;given<=3;given++){var cabin=new CompoundTag();cabin.putBoolean("Bargain0451",true);cabin.putInt("Given",given);cabin.putBoolean(given==3?"Sacrificed":"Refused",true);cabin.putString("ClosedRoom","winchester");all.add(ending("Reader_1234567890",LabyrinthPlace.END_WORLD_CABIN,cabin));}for(int i=0;i<8;i++)all.add(diary(i,"Reader_1234567890"));for(int i=0;i<3;i++)all.add(cylinder(i));return all;}
    public static String counts(ServerPlayer p){return "Villagers: "+p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.VILLAGER))+"\nWolves: "+p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.WOLF))+"\nIron golems: "+p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.IRON_GOLEM))+"\nWandering traders: "+p.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.WANDERING_TRADER));}
    public static final String[] CONFESSION={"I came to a town that had room for another name. ","I learned which doors would open when I knocked, and which people would step aside. ","I mistook those things for permission. ","The first account left out everyone who could no longer answer it. ","I kept a chair for a listener. I thought the distance between our chairs would keep the story mine. ","Now I want you to put down the pen and count with your own hands. ","There are names the world remembers without a witness. There are numbers it remembers without a name. ","This chapter belongs to the person who has been writing. "};
    public static ItemStack diary(int i,String name){String[] pages={"We went up to the rocks before dark. I told him the path would still be there when we came back.","He asked whether a missing person knows they are missing. I laughed because I thought it was a joke.","The house was lit when we got home. There was one chair nobody sat in.","I left a page by the door so someone would know where I had stopped looking.","I heard the floor from the wrong side. I could not make it happen a second time.","If the camera saw me, it must have seen the room I was in. The time on the print is later than the time on my watch.","I have put the pages down where you can find them. I do not know how to put myself there.","This is the last page. "+name+" can leave it on the floor, or carry it. The room will not need another."};return book("Loose diary page "+(i+1),"T.",pages[Math.floorMod(i,pages.length)],"The paper is dry. There is no fold where it would have fitted through the door.");}
    public static ItemStack cylinder(int i){return book("Cylinder transcript "+(i+1),"A former wife",new String[]{"I thought the roof was being repaired. The snow came through it the next winter too. I learned to move my chair before it fell.","He kept the house warm in the rooms where people were expected. I walked farther to find a room where I could speak without being heard.","I have left this voice where a hand might find it. Listen to the whole cylinder. Do not let him tell you what I meant."}[i]);}
}
