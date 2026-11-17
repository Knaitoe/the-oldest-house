package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.667, Last Seen, part 1 (docs/LAST_SEEN_0_4_667.md): what the office's and the park's things say to a hand, the three's own lines,
 * and the typed card on the counter, the first paper. The ten children's names, and where and on what day each was last seen, are set
 * here once: the posters show them, and so do their folders. Every line is flagged to the owner in its part's doc.
 * <p>0.4.668, part 2 (docs/LAST_SEEN_0_4_668.md): the folders, the shelves' letters, the day's pile, Nora's rule, Luz at the door, and
 * the first of them at the fountain.
 * <p>0.4.669, part 3 (docs/LAST_SEEN_0_4_669.md): the second day, the eight, the families, the city, and dusk.
 * <p>0.4.670, part 4 (docs/LAST_SEEN_0_4_670.md): the night, the men, a child's hand, the back door, and dawn.
 */
public final class LastSeenTexts {
    private LastSeenTexts(){}
    public static final String HINT="There is a typed card on the counter.";
    /** The office's three, in the order of LastSeen's phases. */
    public static final String[] NAMES={"Nora","Patricia","Luz"};
    /** The ten, in the order of their posters and their skins. */
    public static final String[] CHILDREN={"Rocío","Tomás","Micaela","Brian","Daiana","Lautaro","Abril","Joel","Yanina","Santino"};
    /** Where each was last seen: every place is in this park or on this avenue. */
    public static final String[] WHERE={"at the fountain in the park","at the bus stop on the avenue","at the park's north gate","outside the kiosk",
        "on the bench by the plane trees","on the swings","at the bandstand","on the corner past the bus stop","on the path to the playground",
        "on the far pavement of the avenue, with a ball"};
    /** And on what day. */
    public static final String[] DAY={"a Tuesday in March","a Friday in May","a Sunday in July","a Monday in April","a Thursday in June",
        "a Saturday in September","a Sunday in October","a Wednesday in August","a Friday in November","a Saturday in December"};

    public static final String COUNTER="The front desk. Its top is worn smooth where people have leaned on it to wait.",
        FLAP="The end of the counter, hinged so it lifts to let the staff through. It is down.",
        CHAIR="An office chair on steel legs. Its seat has taken the shape of whoever sits in it.",
        SHELF="Folders, spine out, floor to ceiling. On every spine is a name written by hand, and a date.",
        SHELF_GAP="Folders, spine out, and a gap among them where one has been taken down.",
        BOXES="Archive boxes on the top shelf, too high to reach without the steps. The labels on them give years, not names.",
        TABLE="The long table. Its top is scored where folders have been slid along it, open, to whoever is sitting across.",
        PILE="The day's folders, in a pile, waiting to go back on the shelves.",
        LAMP="A desk lamp, on, though it is morning.",
        DESK="Nora's desk. A sheet in the typewriter, half typed. The one drawer is locked.",
        DESK_OPEN="Nora's desk, its drawer open.",
        LOUVRE="A louvred window, its slats open on the park.",
        TUBE="A strip light. It hums very faintly.",
        RAILING="The park's railing. The paint on it is thick from years of coats, one over another.",
        POST="A railing post with a spear at the top, cold to the hand.",
        GATE="The park's gate. It was shut, but not locked, and it swings open on its hinges.",
        GATE_CHAINED="The park's gate, chained and padlocked.",
        GATE_OPEN="The park's gate, standing open.",
        LAMP_POST="A park lamp. Its globe is lit, though the sun is up.",
        LAMP_DARK="A park lamp, its globe dark.",
        BENCH="A park bench. Initials are cut into the back, and a heart, and a date.",
        FOUNTAIN="The fountain's rim. The basin inside is dry, with leaves blown into the corner.",
        BOWL="The fountain's bowl, dry. A coin lies in it.",
        KIOSK="The kiosk, its shutter down. The bills pasted on its side are years old, one over another.",
        TRUNK="A silk-floss tree. Its trunk swells like a bottle and is covered in thorns.",
        PLANE="The leaves of a plane tree, broad as a hand.",
        SILK="The silk-floss is in flower.",
        BANDSTAND="The bandstand. Nobody has played in it for a long time.",
        SHELTER="The bus shelter. The timetable has been torn out of its frame.",
        STOP="The stop's sign, on its pole at the curb.",
        SHUTTER="A window of one of the houses, its roller blind down.",
        HOUSE_DOOR="A front door. It is locked, and nobody comes.";
    /** The playground's parts, by their stage. */
    public static String play(int stage){
        return switch(stage){
            case 1->"A swing, hanging still on its chains.";
            case 2->"The slide's ladder. Its rungs are worn smooth in the middle.";
            case 3->"The slide. The chute is worn smooth from top to bottom.";
            case 4->"A see-saw, one end down on the ground.";
            case 5->"The playground's low fence.";
            default->"The swing frame.";
        };
    }
    /** A poster: the first ten are the ten, by name; the rest are older, and the faces have gone into the paper. */
    public static String poster(int i){
        if(i<CHILDREN.length)return "A poster. A photograph of "+CHILDREN[i]+", and under it: last seen "+WHERE[i]+", on "+DAY[i]+".";
        return "A poster, older than the rest. The face in it has faded into the paper, and the name with it.";
    }

    /** The three's lines: the first time, then the second each time after. */
    public static String line(int who,int heard){
        return switch(who){
            case LastSeen.NORA->heard==0?RULE:heard==1?CHORE:heard%2==0?"We keep them all. Nobody has ever asked us to throw one out."
                :"You can sit at the table with one, if you want. Nobody minds.";
            case LastSeen.PATRICIA->heard==0?"If you've come about someone, sit down and I'll take the details. If you've come to look, ask Nora."
                :"Everything you need to know is on the card. I typed it.";
            default->heard==0?"It's quiet in the mornings. People come in the afternoon, after work."
                :"You can see the whole park from the door. There's nobody out there yet.";
        };
    }

    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the typed card on the counter. */
    public static ItemStack source(){
        return HouseWriting.book("The card on the counter","The office",pages(
            "MISSING PERSONS: MINORS.\n\nOpen in the mornings. Bring a photograph if you have one. A recent one is best. Any one will do.",
            "We will need the name, the date, and the place they were last seen. Be as exact as you can about the place. People remember the day better than the place.",
            "Folders do not leave the archive. If you want to see one, ask at the desk, and someone will sit with you while you look.",
            "Nobody here will tell you to wait. We know you are already waiting."));
    }
    // ------------------------------------------------------------------------------------------------ 0.4.668, the archive and the first one
    /** A folder, by the child whose it is. */
    public static String folderName(int child){return CHILDREN[child]+"'s folder";}
    /** What the folder says when it is held open: its file number, the form's heading, the fields, and Nora's note in pencil. */
    public static String file(int child){return String.format("FILE %04d",1000+(child*389+217)%8999);}
    public static final String FORM="MISSING PERSONS: MINORS",NAME_FIELD="NAME",WHERE_FIELD="LAST SEEN",DAY_FIELD="ON",NOTE="Folder stays open.";
    /** The shelves are filed by the first letter of the name on each spine. */
    public static String spines(char letter){return "Folders, spine out. The names on these spines all begin with "+letter+".";}
    public static String wrongBay(char letter){return "The names on these spines all begin with "+letter+".";}
    public static String shelved(char letter){return "It goes back with the other "+letter+" names, spine out.";}
    public static String taken(int child,char letter){return folderName(child)+", with the other "+letter+" names.";}
    public static String fromPile(int child){return "A folder from the day's pile: "+CHILDREN[child]+".";}
    public static final String ONE_AT_A_TIME="One folder off the shelves at a time.",BACK_ON_SHELF="The folder is back on its shelf.",
        PILE_DONE="The day's pile is done. The table is bare.";
    /** Nora's rule, then the chore, then what she says while the morning goes on. */
    public static final String RULE="Every one of them has a folder. A folder stays open.",
        CHORE="The day's folders are on the table. They go back where their names say.",
        LEAVING="Most of the ones in here weren't taken. They went. Nobody ever asks what from.",
        NORA_WAITS="Go on. I'll stay here.",NORA_FIND="You've seen her face. Find hers.",NORA_AFTER="Put it back where it goes. It stays open.";
    /** Patricia, to a reader the House reads as young. */
    public static final String PATRICIA_YOUNG="Are you here with someone?";
    /** Luz at the front door, and her call. */
    public static final String LUZ_CALL="There's a girl at the fountain.",LUZ_NORA="Nora.";
    /** The first of them, at the fountain. */
    public static final String AT_RIM="She stands at the fountain's rim, facing the office. She does not blink.",
        COLD="Her hand is as cold as the fountain's stone.",NOT_HERS="She looks at the face on it, and then away.",
        WAITING="I was waiting where they told me to.",GIVES_BACK="She gives the folder back.";

    // ------------------------------------------------------------------------------------------------ 0.4.669, more of them
    /** The second day: the desk lamp left on since the morning; the park lamps at dusk; Luz at the door, once she has said the rest. */
    public static final String LAMP_LATE="A desk lamp, on. Nobody has turned it off since the morning.",
        LAMP_POST_DUSK="A park lamp. Its globe is lit.",
        LUZ_DOOR="They don't come to the door. Not one of them has.";
    /** Which of the ten are girls, for the few lines that need she or he. */
    static final boolean[] GIRL={true,false,true,false,true,false,true,false,true,false};
    static String pronoun(int child){return GIRL[child]?"She":"He";}
    /** Coming near each of the eight on the second day, once. The first of them keeps her own line. */
    public static String near(int child){
        return switch(child){
            case LastSeen.ROCIO->AT_RIM;
            case 1->"Tomás stands at the railing across from the bus stop, facing it. He does not blink.";
            case 2->"Micaela stands by the gate, facing it. She does not blink.";
            case 3->"Brian stands outside the kiosk, its shutter down behind him. He does not blink.";
            case 4->"Daiana stands by the bench under the plane trees. She does not blink.";
            case 5->"Lautaro stands at the playground, facing the swings. One knee is scraped, and it is still wet. He does not blink.";
            case 6->"Abril stands alone on the bandstand. She does not blink.";
            case 7->"Joel stands at the corner of the railing, facing the far side of the avenue. He does not blink.";
            default->CHILDREN[child]+" is not here.";
        };
    }
    /** What each says, given their own folder, once they have looked at it: the one thing. */
    public static String oneThing(int child,boolean ran){
        return switch(child){
            case LastSeen.ROCIO->WAITING;
            case 1->"Is it still the same day?";
            case 2->"This is the gate. I went out this way.";
            case 3->ran?"Nobody's coming for me. You know how that is.":"Nobody's coming for me. I knew that before.";
            case 4->"Does he know I'm here?";
            case 5->"I fell on the way. It hasn't stopped.";
            case 6->"We waited in a line. Then they said we could go.";
            case 7->"Is that my mom, over there?";
            default->"";
        };
    }
    public static String cold(int child,boolean coat){
        if(child==LastSeen.ROCIO)return COLD;
        return coat?"Under the coat, Tomás's hand is just as cold.":CHILDREN[child]+"'s hand is colder than the day.";
    }
    public static String notTheirs(int child){return child==LastSeen.ROCIO?NOT_HERS:CHILDREN[child]+" looks at the face on it, and then away.";}
    public static String givesBack(int child){return child==LastSeen.ROCIO?GIVES_BACK:CHILDREN[child]+" gives the folder back.";}
    /** Food held out: taken, held, never eaten. */
    public static String fed(int child){return CHILDREN[child]+" takes it and holds it, and does not eat.";}
    public static String stillHolding(int child){return CHILDREN[child]+" is still holding what someone gave "+(GIRL[child]?"her":"him")+", and has not eaten it.";}
    /** Nora on the second day: her rule to a reader who has not heard it, then the day's two lines; after the man from the city, her answer;
     * and holding a folder whose child has not come back. */
    public static final String NORA_EIGHT="There are eight of them out there today. Every one of them has a folder in here.",
        NORA_EACH="Take each one their own. Put it back after.",
        NORA_CLOSED="He wants them closed. A folder stays open.",
        NORA_NOT_BACK="That one hasn't come back. Put it back on its shelf. It stays open.",
        NORA_OPEN="They stay open.";
    public static final String PATRICIA_PHONE="The phone hasn't stopped. Everyone wants to know if one of them is theirs.",
        LUZ_COUNTED="I counted eight from the door this morning. Then I counted again.",
        LUZ_COAT="A grandmother came in by the gate. She brought a coat.";
    /** The families. Tomás's grandmother; Daiana's father; Joel's mother, on the far pavement. Brian's never come. */
    public static final String[] FAMILY_NAMES={"Mr. Ocampo","Tomás's grandmother","Daiana's father","Joel's mother"};
    public static String family(int who,int heard){
        return switch(who){
            case LastSeen.OCAMPO->heard==0?"I'm from the city. You'll want to keep away from them.":"Don't feed them. It's on the notices.";
            case LastSeen.GRAN->heard%2==0?"He was always cold, even in summer. I brought his coat.":"His hands are like ice. They'll warm up.";
            case LastSeen.FATHER->heard==0?"I'm taking her home for an hour. I'll bring her back.":"I said I'd bring her back.";
            default->heard%2==0?"Is he eating? Tell me if he's eating.":"I can see him from here.";
        };
    }
    public static final String COAT_ON="Tomás's grandmother puts the coat round his shoulders. He lets her.",
        FATHER_COME="Come on.",
        FATHER_TAKES="Daiana's father takes her by the wrist and walks her out of the park.",
        FATHER_BACK="Daiana's father brings her back to the gate, and leaves her there.",
        OCAMPO_FILE="A child who comes back is a closed file.",
        OCAMPO_CLOSE="Close them, Nora. All of them.",
        DUSK="The light is going. In the park, the children have all turned to face the avenue.";
    /** The city's fence round the playground, and its notice on the lamp posts. */
    public static final String FENCE="A wire fence on concrete feet, put up round the playground. It is too high to climb.",
        NOTICE="A notice taped round the post: DO NOT APPROACH. DO NOT FEED. It is signed by the city.";

    // ------------------------------------------------------------------------------------------------ 0.4.670, the night
    static String his(int child){return GIRL[child]?"her":"his";}
    /** Nora at night: before the chain, once it is on, once the men are over, and in the morning. */
    public static final String NORA_STAY="I'm not going home tonight. Stay if you're staying.",
        NORA_CHAINED="They've chained the gates. Bring them in here, by the back door.",
        NORA_BRING="Bring them in by the back door. The front is locked.",
        NORA_SHELF="Each one knows where their folder is. Let them go to it.",
        NORA_DOOR="Don't open the door.",
        NORA_DAWN="I didn't take them down. I was here all night.";
    public static final String FRONT_LOCKED="The front door is locked. Patricia locked it when she went home.",
        CHAINED="Out on the avenue, someone winds a chain round the park's gates, and padlocks it.",
        MAN_TALK="He turns his light on you, and then back to the park.";
    /** The men on the avenue, one line at a time while they wait; at the back door, the one who tries it. */
    public static final String[] MEN_LINES={"Gate's done.","They haven't moved. Not one of them.","Wait for the office to go dark.",
        "Somebody's in there with them.","It isn't going to go dark.","Then we don't wait."};
    public static final String MAN_LOCKED="Locked.",MAN_PAPER="Nothing in here but paper.";
    /** A child's hand. */
    public static String takeHand(int child){return CHILDREN[child]+" takes your hand. "+(GIRL[child]?"Hers":"His")+" is cold, and it holds on.";}
    public static String letGo(int child){return "You let go of "+CHILDREN[child]+"'s hand.";}
    public static String heldOther(int child){return CHILDREN[child]+" is holding someone else's hand.";}
    public static final String HAND_FULL="You already have one of them by the hand.";
    public static String atShelf(int child){return CHILDREN[child]+" lets go of your hand and goes to the shelf where "+his(child)+" folder is, and stands facing it.";}
    public static String atFolder(int child){return CHILDREN[child]+" stands at "+his(child)+" folder, facing it, and does not turn round.";}
    /** Coming near each of the eight at night, once. */
    public static String nearNight(int child){return CHILDREN[child]+" stands facing the avenue. "+pronoun(child)+" does not blink.";}
    /** The back door: kept, tried and held, tried and opened. */
    public static final String KEEP="You put your weight against the back door.",
        KEPT="The handle turns under your hand. You hold it, and the door holds.",
        TRY_HELD="Someone tries the back door. It holds.",
        TRY_OPEN="The back door opens. A light goes along the shelves and does not stop.";
    /** The men over the railing. */
    public static final String EMPTY_HAND="Your hand is empty.",
        OVER_PARK="The lamps go out, all of them at once. When you can see again, there is nobody where the children were.",
        OVER_OFFICE="Out in the park, the lamps go out all at once. Lights are coming over the railing.";
    /** Dawn. */
    public static final String DAWN="Light comes up over the avenue. It is morning, and the park is empty.",
        DAWN_TABLE="Light comes in at the louvres. Nobody stands at the shelves. On the long table, their folders lie open at the first page.",
        DAWN_EMPTY="Light comes in at the louvres. It is morning. The archive is as it was.";
    public static String tableOpen(int child){return folderName(child)+", lying open at the first page.";}

    /** The account. Nothing asks for it until part 5; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LAST_SEEN),name,pages(
            "There was an office at the edge of a park, with a counter, and posters of children on the partition behind it. In the archive, folders stood on steel shelves to the ceiling. "
                +"Outside, the park was empty in the morning, and the fountain was dry."));
    }
}
