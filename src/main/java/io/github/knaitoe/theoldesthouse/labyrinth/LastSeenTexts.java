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

    /** The account. Nothing asks for it until part 5; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LAST_SEEN),name,pages(
            "There was an office at the edge of a park, with a counter, and posters of children on the partition behind it. In the archive, folders stood on steel shelves to the ceiling. "
                +"Outside, the park was empty in the morning, and the fountain was dry."));
    }
}
