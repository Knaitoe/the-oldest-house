package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;

/**
 * 0.4.641, A sort of thing, part 1 (docs/SLEEPING_KING_PLAN.md): what each of the drawing-room's things and the country's says to a
 * hand. Nobody speaks yet. Every line is new in this release and is flagged to the owner in docs/SLEEPING_KING_0_4_641.md.
 */
public final class KingTexts {
    private KingTexts(){}
    public static final String HINT="There is a chess problem on the little table, printed small.";
    public static final String MANTEL="The fire is real enough to warm a hand. The marble over it has a crack, mended with something that has gone yellow.",
        GLASS="The glass shows the room the other way round. Near the bottom it is not quite glass any more, more like a mist, and it gives under the hand.",
        GLASS_BACK="The glass shows the room the other way round, the way you came.",
        RUG="A hearthrug, worn bald where feet have rested on it for years.",
        CHESSMEN="Chessmen in the hearth among the cinders, as if the game had been knocked over and nobody had picked it up. One king lies on his side.",
        TABLE="A little round table, polished until the grain shows like water.",
        PROBLEM_BACK="The type runs the wrong way. Held up to the glass, it might read.",
        PROBLEM="Here the type reads the right way round: a chess problem, and under it, \"White to play, and win in eleven moves.\"",
        ARMCHAIR="A deep armchair turned toward the fire. The seat is still a little warm.",
        WINDOW="Through the glass the country lies in squares, as far as the eye goes.",
        WINDOW_DRAWING="Rain has been on this glass. Beyond it the garden is the ordinary sort.",
        KING="An old man asleep under the tree, in a heap, in a long robe and a tall night-cap with a tassel. He is snoring.",
        RULED="Here the ground is only drawn: an outline on paper, and a little hatching where the shadow would be.";
    /** 0.4.642: a line who goes off the country is nowhere, and is put back on the last square that counted them. */
    public static final String NOWHERE="The ground comes back underfoot, on a square stood on before.";
    // ------------------------------------------------------------------------------------------------ 0.4.643, part 3
    // Every line below is new in 0.4.643 and flagged to the owner in docs/SLEEPING_KING_0_4_643.md. The dream's people may say his
    // colour; the House never does.
    public static final String DUM_NAME="Tweedledum",DEE_NAME="Tweedledee",BROTHERS="the two brothers",THE_KING="the King";
    public static final String DUM_DREAMING="That's the Red King. He's dreaming. Guess what about.",
        R_NOBODY="Nobody can guess that.",R_WHAT="What about?",
        DEE_SORT_OF_THING="Why, about you. You're only a sort of thing in his dream.",
        DUM_TREAD="Tread like that and he'll stop. Then where would you be?",
        DEE_NOT_EVEN="Not even there.",
        R_WHO="Who is he, then?",R_QUIET="I'll keep my voice down.",
        DUM_LONGER_NAME="The Red King, people call him, when they don't know him. He has a longer name than that.",
        DEE_HIS_NAME="His Red Majesty, King of the Fourth Square, who sleeps under the tree. All of it, mind, or it doesn't count.",
        DUM_RATTLE="And that's my rattle under his tree. Spoilt.",
        DEE_RATTLE="It isn't spoilt. It's old.",
        DUM_BATTLE="Then we'll have a battle about it. As soon as we're dressed.",
        DUM_GOT_IT="You've got it, then.",DEE_GOT_IT="Then there's nothing to fight about. A pity.",
        DUM_DRESSING="We're dressing. You can see we're dressing.",DEE_DRESSING="Hold this bolster.",
        DUM_AGAIN="Don't wake him, that's all.",DEE_AGAIN="Or do. Then see where you are.";
    /** What the reader says beside him, and the other answer. */
    public static final String SAY_HIS_NAME="His Red Majesty, King of the Fourth Square, who sleeps under the tree.",LET_HIM_SLEEP="Let him sleep.";
    public static final String ASLEEP_HERE="He is asleep in a heap at the foot of the tree, close enough to touch.",
        THROUGH_HIM="The hand goes into him as into a drawing of him.",
        EYES_OPEN="His eyes are open.",
        RATTLE_THROUGH="The hand closes on the rattle and comes away with nothing in it.",
        RATTLE_TAKEN="The rattle comes up off the grass without a sound.",
        STIRRED="He stirred in his sleep.",
        DRESSING="The two brothers are tying bolsters round each other for a battle.",
        NOTHING_TO_FIGHT="The two brothers stop dressing. There is nothing left to fight about.",
        CROW="Something went over the wood, as big as a cloud, and the two of them ran.",
        TOO_BUSY="The two of them are too busy to talk.",
        BREATHING_STOPPED="The breathing stopped in the middle of a breath.",
        WOKE_TOO_SOON="The breathing stopped in the middle of a breath. He is waking.",
        LINES="The country went over to lines, all of it at once, and then the lines began to go.",
        LINES_TOO_SOON="The country went over to lines, and the lines began to go.",
        WENT_OUT="He woke, and the country went out with you in it.",
        CAME_TO="The fire, the glass and the armchair. There is a book on the armchair.",
        LEFT_HIM="The last of the board let go. The drawing-room came round you, and behind the glass he is still breathing.";
    /** When he turns over and the filled land draws in to the next depth of his sleep. */
    public static String drewIn(int level){return level>=2?"He turned over again, and the filled land drew in to his own square.":"He turned over in his sleep, and the filled land drew in toward him.";}

    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the chess problem on the little table, printed the wrong way round. Behind the glass it reads. */
    public static ItemStack source(){
        return HouseWriting.book("A chess problem","",pages(
            ".sevom nevele ni niw dna ,yalp ot etihW\n\nnwap etihw a sa nigeb uoY",
            "The diagram above the words is a board of eight squares by eight, with the pieces drawn on it as little pictures. "
                +"Somebody has drawn a line in pencil from the second square up through the board to the eighth, and stopped."));
    }
    /**
     * 0.4.643: the account, from what this reader did: the room and the country, the brothers, the rattle or the battle, and the
     * ending that was theirs.
     */
    public static ItemStack account(String name,CompoundTag own){
        var out=new ArrayList<String>();
        out.add("There was a drawing-room with a fire in it, and a glass over the fire, and the same room on the other side of the glass. "
            +"Beyond it the country lay in squares, and an old man was asleep under a tree.");
        if(own.getBoolean("KingHeard"))out.add("Two brothers in the wood took me to see him. They said he was dreaming, and that I was only a sort of thing in his dream. "
            +"The two of them had a longer name for the King.");
        if(own.getBoolean("KingRattle"))out.add("One of them had left a rattle under his tree. I went in to it between his breaths and picked it up, and after that there was nothing for them to fight about.");
        else if(own.getBoolean("KingSawBattle"))out.add("They dressed for a battle over a rattle and fought it beside him, and he turned over in his sleep. Something went over the wood like a cloud, and they ran.");
        out.add(switch(own.getString("KingEnding")){
            case "said_his_whole_name"->"I said the whole of his name. He opened his eyes. One of us went out.";
            case "there_when_he_woke"->"Somebody else said the whole of his name. He opened his eyes, and the country went over to lines with me in it.";
            case "left_him_his_sleep"->"I left him his sleep. I walked until I was a line. He is still breathing behind me, or I am."
                +(own.getBoolean("KingWokeAfter")?" He woke after I had gone. I was not there to see it.":"");
            default->"I do not know how it ended. I was not there for that.";
        });
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.SLEEPING_KING),name,pages(out.toArray(String[]::new)));
    }
}
