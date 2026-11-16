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
    // ------------------------------------------------------------------------------------------------ 0.4.644, part 4
    // Every line below is new in 0.4.644 and flagged in docs/SLEEPING_KING_0_4_644.md.
    public static final String QUEEN_NAME="the Queen",GUARD_NAME="the Guard";
    public static final String QUEEN_FASTER="Faster! Faster! Don't try to talk.",
        QUEEN_DONT_TALK="Here, you see, it takes all the running you can do to keep in the same place.",
        QUEEN_LETS_GO="Here you are what you are counted as. Stand on the peg.",
        GUARD_TICKET="Ticket. Every thing on this train has a ticket.",
        GUARD_SECOND="Travelling the wrong way.",
        GUARD_THIRD="A thing ought to know which way it is going, even if it doesn't know its own name.";
    public static final String QUEEN_LET_GO="She let go, and the hands in front of you were drawn flat.",
        QUEEN_HAND="She is too busy to be held by the hand just now.",
        GUARD_HAND="He looks through his glass at the hand, and then at you, and does not take it.",
        COUNTED="Counted. Somebody is taking notice.",
        PEG="A peg, driven in at the near edge of the square.",
        TABLE_THROUGH="The hand goes down through the glass top and finds nothing to lift.",
        TABLE_TAKE="A little bottle and a very small cake, one for each hand.",
        TABLE_TAKEN="The table has nothing more on it for you.",
        SWALLOW="Less, for a while.",
        BITE="More, for a while.",
        DRAUGHT_NOTHING="Nothing happens. It is only something to drink, out here.",
        GOT_UP="The Guard lowers his glass, and the seat is only a seat.",
        JUMPED="The train took the brook at a jump. The road is under your feet on the far side.",
        WHEEL="A carriage wheel, standing in the grass.";

    // ------------------------------------------------------------------------------------------------ 0.4.645, part 5
    // Every line below is new in 0.4.645 and flagged in docs/SLEEPING_KING_0_4_645.md. "Who are you?" and the reply about not being
    // oneself are Carroll's.
    public static final String CATERPILLAR_NAME="the Caterpillar";
    public static final String CAT_WHO="Who are you?",
        CAT_YOU="You! You were more of a thing at the hill, and you will be less of one at the bridge. Which is it?",
        CAT_SIDES="One side of this will make you more, and the other side will make you less.",
        CAT_BRIDGE="The bridge is only drawn. A drawing bears a drawing.";
    public static final String R_TOO_MUCH="Too much of a thing, I think, just now.",
        R_A_THING="A thing, at the moment. I was less of one on the way here.",
        R_A_SORT="A sort of thing. I was a thing a little while ago.",
        R_HARDLY="Hardly anything. I have been several things since I came in.",
        R_NOT_MYSELF="I can't explain myself, because I'm not myself, you see.";
    public static final String STALK="A mushroom's stalk, as tall as a table.",
        MUSHROOM_MORE="A piece from this side. More.",
        MUSHROOM_LESS="A piece from that side. Less.",
        FELL_THROUGH="The bridge was only drawn, and would not bear a thing. The bank is under your feet again.",
        DRAWN_CROSSED="Over the drawn bridge, as a drawing.";

    // ------------------------------------------------------------------------------------------------ 0.4.646, part 6
    // Every line below is new in 0.4.646 and flagged in docs/SLEEPING_KING_0_4_646.md. "Nothing but a pack of cards" is Carroll's; the
    // Cat's first line is the proposal's.
    public static final String HEARTS_KING_NAME="the court",HEARTS_QUEEN_NAME="the court",HERALD_NAME="the herald",CAT_NAME="the Cat";
    public static final String HERALD_COME="Come before the court, and stand in the dock, and be looked at.",
        HERALD_TALK="Your name is on the scroll. That is all the scroll says.",
        KING_TALK="The court is sitting. It has been sitting a long while.",
        SENTENCE_FIRST="Sentence first.",
        SENTENCE_AFTER="What it was for, we'll find out afterwards. We have never got as far as afterwards.",
        WHAT_FOR="That comes afterwards. Nothing has ever been carried out.",
        ARGUE_KING="A head can't be taken off unless there's something to take it off.",
        ARGUE_QUEEN="Anything that has a head can be made to do without it.",
        ARGUE_HERALD="It's only grinning. It never does anything else.",
        ARGUE_KING_AGAIN="Then it must be sentenced. Sentence first.";
    public static final String R_WHAT_FOR="What is the sentence for?",R_PACK="You're nothing but a pack of cards!";
    public static final String CAT_HEAD="They can't take a head off a line. They have tried it on me.",
        CAT_OFFER="Shall I go and be looked at, instead of you? They can only look at one thing at a time.",
        CAT_LATER="I'll be here. Some of me, anyway.",
        CAT_THERE="They're looking at me now. Go on, while they are.",
        CAT_GOING="They'll stop looking at me soon. I'd be on the far side by then.";
    public static final String R_COME="Go and be looked at.",R_NOT_NOW="Not yet.";
    public static final String SUMMONED="Your name, read off a scroll. On this square you are a thing for as long as the court attends to you.",
        NOT_FINISHED="The guard closed up, and the court set you back in the dock. It has not finished looking at you.",
        EDGE_ON="Out between two of the guard, edge-on.",
        COURT_DONE="The court has done with you.",
        PACK_FELL="The pack went up, and came down as cards on the grass. A long way off, the King stirred.",
        CAT_OVER="A head is over the court, grinning, and every face in it has turned up to look.",
        CARD_STANDING="One of the court's pack, standing on its edge.",
        CARD_FALLEN="A card, lying flat in the grass.";

    // ------------------------------------------------------------------------------------------------ 0.4.647, part 7
    // Every line below is new in 0.4.647 and flagged in docs/SLEEPING_KING_0_4_647.md. The turn's request is the proposal's; the song
    // is original.
    public static final String KNIGHT_NAME="the Knight";
    public static final String KNIGHT_HELLO="Good day. I ride with anybody going this way, as far as the last brook. I stay on better if I'm looked at.",
        KNIGHT_AGAIN="Still going on? So am I, as long as you keep an eye on me.",
        KNIGHT_DOWN="Nobody was looking, and I came off. Look at me a while and I'll get up.",
        KNIGHT_UP="There. Up again. It's being looked at that does it.",
        KNIGHT_TURN_ASK="Would you watch me as far as the turn? I go better watched.",
        KNIGHT_RIDE_ON="On we go, then. Keep me in the corner of your eye.",
        KNIGHT_CAKE="I keep a cake in the box on my saddle, in case. Here. A bite, and you'll be more than you are.";
    public static final String R_SING="Would you sing something?",R_CAKE="Have you anything to eat?",R_RIDE_ON="Ride on.";
    /** His song: four lines, eight bars. */
    public static final String[] SONG_WORDS={
        "My horse is only drawn, and I am only drawn as well;",
        "look at me and I'm filled in. Look off, and off I fell.",
        "I fell off at the second brook, and fell off at the third,",
        "and somebody came back and looked, and never said a word."};
    public static final String KNIGHT_FELL="Nobody was looking at the Knight. He thinned to a drawing and came off his horse into the path.",
        KNIGHT_ROUND="He went round the turn filled in, and out of sight.",
        KNIGHT_THINNED="He was a drawing before he got to the turn. Then there was nothing at the turn but the road.",
        SANG="The Knight's song, under the breath, all four lines of it.",
        KNIGHT_LOOKED="A rider on a drawn horse, beside the road. He fills in as he is looked at.",
        SETTLED="He settled in his sleep, and breathed easier.",
        SETTLED_OUT="He settled in his sleep, and the filled land went out again from him.";

    static String words(int n){return switch(n){case 2->"two";case 3->"three";case 4->"four";case 5->"five";case 6->"six";case 7->"seven";default->String.valueOf(n);};}
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
        // 0.4.644: the hill, the table and the railway, as each reader went by them.
        if(own.getBoolean("KingQueenRan"))out.add("A queen in a long gown took me by the hand on the second square and ran with me as fast as we could go, and we were in the same place at the end of it. Then she let go and told me to stand on the peg.");
        int counted=Integer.bitCount(own.getInt("KingCounted"));
        if(counted>0)out.add(counted==1?"I stood on one peg, and it counted me.":"I stood on "+words(counted)+" pegs, and each of them counted me.");
        if(own.getInt("KingSwallows")>0||own.getInt("KingBites")>0)out.add(own.getInt("KingSwallows")>0&&own.getInt("KingBites")>0?"There was a bottle and a cake on a glass table. I had some of each.":own.getInt("KingSwallows")>0?"There was a bottle on a glass table, and I drank from it.":"There was a cake on a glass table, and I ate some of it.");
        if(own.getBoolean("KingRode"))out.add("I sat in a railway carriage in a field while a guard looked at me through three glasses, and the train took the brook at a jump.");
        if(own.getBoolean("KingHeard"))out.add("Two brothers in the wood took me to see him. They said he was dreaming, and that I was only a sort of thing in his dream. "
            +"The two of them had a longer name for the King.");
        if(own.getBoolean("KingRattle"))out.add("One of them had left a rattle under his tree. I went in to it between his breaths and picked it up, and after that there was nothing for them to fight about.");
        else if(own.getBoolean("KingSawBattle"))out.add("They dressed for a battle over a rattle and fought it beside him, and he turned over in his sleep. Something went over the wood like a cloud, and they ran.");
        // 0.4.645: the mushroom and the drawn bridge.
        if(own.getBoolean("KingAsked"))out.add("Somebody on a mushroom asked me who I was, and I could only tell it what I was at the time.");
        if(own.getBoolean("KingDrawnCrossed"))out.add(own.getBoolean("KingFellThrough")?"The bridge past the fifth square was only drawn. It let me through once, and bore me when I was less.":"The bridge past the fifth square was only drawn, and it bore me because I was less.");
        else if(own.getBoolean("KingFellThrough"))out.add("The bridge past the fifth square was only drawn, and it would not bear me.");
        // 0.4.646: the court of cards, and which way the reader left it.
        int way=own.getInt("KingCourtWay");
        if(own.getBoolean("KingSummoned"))out.add("A herald on the sixth square read my name off a scroll, and a court of cards sentenced me first and left it there.");
        switch(way){
            case 1->out.add("A head with no cat to it came over the court, and while they argued whether it could be taken off, I went out between the guard edge-on.");
            case 2->out.add("I drank, and went out between the guard edge-on while the court was still looking at where I had been.");
            case 3->out.add("I told the court what it was, and it was a pack of cards on the grass. He stirred, a long way off.");
            case 4->out.add("The court was a pack of cards on the grass by the time I came to it.");
            default->{}
        }
        // 0.4.647: the forest and the Knight.
        boolean met=own.getBoolean("KingKnightMet")||own.getBoolean("KingKnightFell")||own.getInt("KingKnightTurn")!=0||own.getBoolean("KingHeardSong")||own.getBoolean("KingKnightCake");
        if(met){
            out.add("A knight rode with me through the forest on the seventh square. He was only drawn in while somebody looked at him.");
            if(own.getBoolean("KingKnightFell"))out.add(own.getBoolean("KingKnightRaised")?"When nobody looked at him he came off his horse and lay in the path as a drawing, until I went back and looked at him, and he got up."
                :"When nobody looked at him he came off his horse and lay in the path as a drawing.");
            if(own.getBoolean("KingHeardSong"))out.add(own.getBoolean("KingSang")?"He sang me a song about a drawn horse, and I sang it afterwards, under my breath.":"He sang me a song about a drawn horse.");
            if(own.getBoolean("KingSangToHim"))out.add("I sang it beside the King as well, and he settled in his sleep.");
            if(own.getBoolean("KingKnightCake"))out.add("He gave me a cake out of a box on his saddle.");
            if((own.getInt("KingCounted")&1<<7)!=0)out.add("On the forest's peg I was looked at and fed, and it counted me.");
            switch(own.getInt("KingKnightTurn")){
                case 1->out.add("At the last brook he asked me to watch him as far as the turn in the road. I watched, and he went round it filled in.");
                case 2->out.add("At the last brook he asked me to watch him as far as the turn in the road. I did not watch him all the way, and he was a drawing before he got there.");
                default->{}
            }
        }
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
