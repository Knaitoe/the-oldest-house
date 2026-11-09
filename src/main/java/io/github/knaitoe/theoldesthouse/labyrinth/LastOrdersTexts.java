package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.416, Last orders: the house rules on the corkboard, what the people in the bar say, and the morning shift's note under the till.
 * All original. The shapes are from three public-domain stories (a saloon booth where a stranger plays a young man for a roll of notes,
 * a silent company whose drink takes years, and two who cast dice for a crew); nothing else is taken from them, and no title appears.
 *
 * <p>The next booth only ever says what the House has really recorded about the reader who is playing: deaths as the world counted
 * them, companions it saw them lose, stories they walked out of, the glasses they took, and their name. It never genders them.
 */
public final class LastOrdersTexts {
    private LastOrdersTexts(){}
    public static final String HINT="The man in booth four wants a game. Ask him.";
    /** The cast, by phase order: the stranger, the two in the next booth, the three at the board, and the one at the bar. */
    public static final String[] NAMES={"The man in booth four","Ferris","Mags","Hob","Ott","Cass","Benny"};
    /** How the stranger is chalked up at the table and the board. */
    public static final String CHALK="Booth four";
    private static List<Component> pages(HouseWriting.WritingStyle style,String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    public static ItemStack source(){
        return HouseWriting.book("The house rules","The management",pages(HouseWriting.WritingStyle.PLAIN,
            "HOUSE RULES\n\n1. No credit.\n\n2. Last orders at ten to.\n\n3. Booth four is taken.",
            "4. Play anyone who asks you. Pay what you lose.\n\n5. What is won at the tables stays on the tables.",
            "6. Don't drink with the regulars unless you're stopping.\n\n7. The clock is right.\n\nTHE MANAGEMENT"));
    }

    public static final String ARRIVE="A long bar, nearly empty. The clock over the door says ten to. Someone is playing darts at the far end, and nobody is talking.";
    public static final String AGAIN="The bar again. The clock over the door says ten to.";

    // ------------------------------------------------------------------------------------------------ the man in booth four
    public static final String OFFER="Evening. You've the look of someone who plays. Pool or darts? I'll put the money up. Your company's stake enough.";
    public static final String OFFER_AGAIN="You came back. Pool or darts?";
    public static final String NOT_TONIGHT="Another night, then. I'm always here.";
    public static final String POOL_ASK="Pool, then. I'll see you at the table.";
    public static final String DARTS_ASK="Darts, then. I'll see you at the board.";
    public static final String POOL_START="Pool, then. Your break. One on the table to start.";
    public static final String DARTS_START="Darts, then. A short leg, a hundred and one. You throw first. One on the table to start.";
    public static final String BUSY_YOURS="Your shot.";
    public static final String BUSY_MINE="Mine, I think.";
    public static final String WATCH="I'm playing. Watch, if you like.";
    public static final String DOUBLE="Well played. Double it.";
    public static final String DOUBLE_MINE="One to me. Again, and double it.";
    public static final String EVERYTHING="You're good. Then let's have all of it on the table.";
    public static final String EVERYTHING_MINE="Close. Let's have all of it on the table, then.";
    public static final String WON="It's done. I've won.";
    public static final String YOURS="It's yours. Take it.";
    public static final String TAKE_IT="Go on. It's yours.";
    public static final String ANOTHER_TIME="Another time, then.";
    public static final String NOT_YOURS="That's not yours to take.";
    public static final String STARING="He doesn't look away from the seat across from him.";

    // ------------------------------------------------------------------------------------------------ the next booth
    /** One line of the next booth's talk: who says it (1 Ferris, 2 Mags) and what. */
    public record Said(int who,String line){}
    /** What the next booth says once the stakes are high, about a man who won everything at that table, and then about the reader. */
    public static List<Said> nextBooth(String name,int deaths,List<String> lost,List<String> left,int glasses){
        var out=new ArrayList<Said>();
        out.add(new Said(2,"That's his table, that one. Booth four."));
        out.add(new Said(1,"Somebody sat there one night and won the lot off him."));
        out.add(new Said(2,"All of it. Every note on the table."));
        out.add(new Said(1,"And never went home."));
        out.add(new Said(2,"Came in from somewhere else, you could tell. Through the wrong door, I always thought."));
        out.add(new Said(1,deaths<=0?"Never died once before that. Not once.":deaths==1?"Died once before, I heard.":"Died a good few times before, I heard. "+deaths+" times."));
        out.add(new Said(2,lost.isEmpty()?"Never kept an animal. Nothing waiting at home.":"Had an animal. "+lost.get(0)+". Lost it somewhere."));
        out.add(new Said(1,left.isEmpty()?"Stayed to the end of everything. That kind.":"Walked out of "+left.get(0)+" before the end. Didn't stop for it."));
        out.add(new Said(2,glasses<=0?"Wouldn't take a drink off Benny. Not one.":glasses==1?"Took a drink off Benny, though.":"Took "+glasses+" drinks off Benny."));
        out.add(new Said(1,"Name of "+name+"."));
        out.add(new Said(2,"Was it."));
        return out;
    }
    public static String nextBoothTalk(int who,boolean said){
        if(said)return who==1?"We weren't talking about you.":"Go and finish your game.";
        return who==1?"We're not talking to you.":"He'll ask you for a game. Everybody plays him in the end.";
    }

    // ------------------------------------------------------------------------------------------------ the regulars and Benny
    public static String regular(int who){
        return switch(who){case 3->"Hob chalks a score and doesn't look round.";case 4->"Ott is watching the board. He doesn't answer.";default->"Cass weighs a dart in one hand and says nothing.";};
    }
    public static final String QUEUED="Ott taps the chalk with one finger: next game.";
    public static final String GLASS_OFFER="Go on. Steadies the hand.";
    public static final String GLASS_REFUSED="Suit yourself.";
    public static final String GLASS_DRUNK="That's the way.";
    public static final String GLASS_TASTE="It tastes of very little. Over by the board, someone chalks up another week.";
    public static final String GLASS_NOT_YOURS="It was poured for someone else.";
    public static String benny(boolean told){
        return told?"Don't mind them two. They've been telling that one for years.":"I've been on this stool since they put it in. He'll play you for anything you like. Mind what you let him win.";
    }

    // ------------------------------------------------------------------------------------------------ endings
    public static final String[] FEAST={"Outside, every window on the street is lit.","Every table is laid and every door stands open, and they wave you in.","You eat. It goes on for a long time."};
    public static final String TOOK="The bar. The clock over the door says ten to. Your pockets are empty, and the man in booth four is counting his roll.";
    public static final String LOST="Three whistles, and the lamps go down. When they come up the clock over the door still says ten to. Your glass is gone and the chalk has been wiped. You don't remember the rest of the evening.";
    public static final String WALKED="Behind you, nobody calls you back.";

    /** A week chalked on the league board: the week, the name, and who they are down as beating. */
    public static String[] leagueSign(int week,String name,String beat){
        String n=name.length()>15?name.substring(0,15):name;
        return new String[]{"Week "+week,n,"beat "+beat,""};
    }
    public static final String[] REGULARS={"Hob","Ott","Cass"};

    /** The morning shift's note, under the till: what was in booth four at opening on each night this reader was in it. */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("Under the till, for whoever opens tomorrow.");
        int took=own.getInt("Took"),lost=own.getInt("Lost"),glasses=own.getInt("Glasses");
        if(took>0)p.add("Booth four when I opened up: two of them this time, sat across the table from each other, staring. Neither of them breathing. The old one, and the one who was playing him at closing. Same coat, same everything. I'd know it. The money stacked up between them, every note of it.");
        if(took>1)p.add("And again, "+(took==2?"the night after":"for "+took+" nights now")+". I've stopped telling the brewery.");
        if(lost>0)p.add("The chalk on the league board was wiped again. Somebody lost an evening in here. They always leave the glass behind.");
        if(glasses>0)p.add((glasses==1?"Another week":glasses+" more weeks")+" up on the league board under "+name+". I don't remember "+name+" playing one of them. The window's on another season again.");
        if(own.getBoolean("Walked"))p.add("Booth four when I opened up: just the old one, sat staring at the seat across from him as if somebody was still in it. All his money stacked on the table in front of him. He hadn't touched it. I didn't either. Whoever was sat across from him last night left it there, and left.");
        p.add("Clock's still stuck at ten to. I'm leaving it.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LAST_ORDERS),"The morning shift",pages(HouseWriting.WritingStyle.PLAIN,p.toArray(String[]::new)));
    }
}
