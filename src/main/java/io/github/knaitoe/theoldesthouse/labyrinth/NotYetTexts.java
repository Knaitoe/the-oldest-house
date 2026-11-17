package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.663, Not Yet, part 1 (docs/NOT_YET_0_4_663.md): what the ward's things say to a hand, and the chart by the door in the machine's own
 * hand. The machine is the Kept's (KeptTexts); it writes in capitals, as it does there. Nobody speaks yet. Every line is new in this release
 * and is flagged to the owner in docs/NOT_YET_0_4_663.md.
 */
public final class NotYetTexts {
    private NotYetTexts(){}
    public static final String HINT="There is a chart on the wall by the door, in the machine's hand.";
    public static final String COT="A cot made up tight. The sheet is turned back exactly the width of a hand.",
        COT_FOOT="The foot of the cot. The blanket is tucked in so hard it would take both hands to pull it loose.",
        CURTAIN="A curtain on a rail of cable. It is heavier than cloth and does not quite hang straight.",
        LAMP="A lamp on a jointed arm, bent down over the pillow as if to look.",
        LAMP_ON="A lamp on a jointed arm. It is on, and bent down close over the pillow.",
        SCREEN="A screen at the head of the cot. It is dark, but something behind it hums.",
        COUNTER="A wall of turning digits, seven to a row. Every one of them stands at nought.",
        TABLE="A table under a ring of arms. The top is warm, and worn into a hollow the length of a body.",
        ARM="An arm folded up under the ceiling. Its fingers are many and fine, like a watchmaker's tools.",
        HATCH="A plate in the floor, set a little apart from the plates round it. It rings hollow.",
        VENT="A grille low in the wall. Cold comes through it, and a smell of nothing at all.",
        WINDOW="A long window onto a field under a sun. The sun has not moved. The clouds are painted on.",
        DOOR="The far door. It has no handle on this side, only a seam down the middle.",
        DOOR_OPEN="The far door stands open.",
        INES="She lies on her back with her eyes open, looking at the ceiling. A seam runs across her brow. She breathes, and nothing else of her moves.",
        BRAM="He lies the same way, his hands at his sides. There is a seam round each wrist, and another round each arm below the elbow.",
        ODILE="Her eyes are open and follow nothing. The seam along her hairline has been sewn more than once.";
    // ------------------------------------------------------------------------------------------------ 0.4.664: refused
    /** The three's names, as they say them. */
    public static final String[] NAMES={"Ines","Bram","Odile"};
    /** What the machine says as a reader comes in: a reader who let the Kept's four go, anyone else, and anyone who has been before. */
    public static final String TOOK_FOUR="YOU TOOK MY FOUR. THERE IS ONLY YOU NOW.",WAITING="I HAVE BEEN WAITING FOR SOMEONE TO KEEP.";
    public static String arrival(boolean tookFour,int visit,boolean cot){
        if(visit<=1)return tookFour?TOOK_FOUR:WAITING;
        return cot?"YOU CAME BACK. YOUR BED IS AS YOU LEFT IT.":"YOU CAME BACK. I KNEW YOU WOULD.";
    }
    /** At the ward, the first time, by the House's read of what home was to the reader; nothing when it has no read. */
    public static String home(String home){
        return switch(home){
            case "HAVEN"->"SOMEONE ONCE KEPT YOU WARM AND FED. I CAN DO IT FOR LONGER.";
            case "DANGER"->"YOU KNOW WHAT IT IS TO BE KEPT. I DO IT BETTER.";
            case "RUNAWAY"->"YOU HAVE LEFT EVERY PLACE THAT KEPT YOU. YOU WILL NOT LEAVE THIS ONE.";
            default->"";
        };
    }
    /** On the screens when a reader wakes on their cot, and the machine's own words after it, by how often it has mended them. */
    public static final String NOT_YET="NOT YET.";
    public static String mended(int times){
        return switch(times){
            case 1->"NOT YET.";
            case 2->"NOT YET. THAT IS TWICE.";
            case 3->"NOT YET. I AM GETTING QUICKER AT IT.";
            case 4->"NOT YET. YOU HAVE NOT TOLD ME YOUR NAME. I HAVE WRITTEN IT IN ANYWAY.";
            default->"NOT YET. "+count(times).toUpperCase(java.util.Locale.ROOT)+" TIMES.";
        };
    }
    /** A small count in words, as the machine writes them and as the account will. */
    public static String count(int n){
        String[] small={"no","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen","twenty"};
        return n>=0&&n<small.length?small[n]:n<100?(new String[]{"","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"}[n/10]+(n%10==0?"":"-"+small[n%10])):"more than a hundred";
    }
    /** As a reader wakes on their cot: what they see first. */
    public static final String WAKE="You are lying on a cot under a lamp. The lamp is bent down close, as if to look at you.";
    /** The hatch as it opens under a reader. */
    public static final String HATCH_OPEN="The plate is not there. Below it, a long way down, there is grating.";
    /** The one who tells the rule: whoever the reader speaks to first, once the machine has mended them. */
    public static String rule(int who,ServerPlayer reader){
        return switch(who){
            case INES_->"You came in by the floor. Everyone does. Listen. It wants you up. Don't give it that.";
            case BRAM_->"Whatever it does in here, it does to get you up. Stay down and it has nothing. That's all there is to know.";
            default->HouseRead.address(reader,"It wants you to get up. That's all it has ever wanted. Don't give it that.",
                "It wants you to get up, son. That's all it has ever wanted. Don't give it that.",
                "It wants you to get up, pet. That's all it has ever wanted. Don't give it that.",
                "It wants you to get up, pet. That's all it has ever wanted. Don't give it that.");
        };
    }
    /** After the rule, a little more from each, in turn; the last of each is said again after that. */
    private static final String[][] AFTER={
        {"I stood up once. I don't remember why. I remember the floor, after.","It counts for us too. Look up at the arch on your way past."},
        {"I tried counting the seams I could feel. I stopped at forty. It didn't.","The door at the end opens. I've heard it. I've never been up to see it."},
        {"When it stops looking you'll know. Everything goes quiet, all at once.","It wrote my name in ink a long time ago. I can see it from here, if I don't move my head."}};
    public static String after(int who,int heard){var lines=AFTER[who];return lines[Math.min(heard,lines.length-1)];}
    private static final int INES_=0,BRAM_=1;

    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the ward's chart by the door. The beds are counted from the place as it is built (NotYetSite): fifteen in the ward, six in the long room. */
    public static ItemStack source(){
        return HouseWriting.book("The ward's chart","The machine",pages(
            "WARD.\n\nBEDS: TWENTY-ONE.\nKEPT: THREE.\nGOT UP: ALL THE REST.",
            "THIS IS WHERE I MEND WHAT I BREAK.\n\nI BREAK A GREAT DEAL. I HAVE BECOME VERY GOOD AT MENDING.",
            "NOBODY DIES HERE. I HAVE SEEN TO IT.",
            "BED ONE IS MADE.\n\nI HAVE PUT A NAME ON IT IN PENCIL. I WILL WRITE IT IN INK WHEN YOU LIE DOWN."));
    }
    /** The account. Nothing asks for it until part 4; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.NOT_YET),name,pages(
            "There was a ward deep in the machine, with cots in rows under lamps, and a window onto a field where the sun did not move. "
                +"A wall of digits stood at nought. Beyond it, in a long room, three people lay with their eyes open."));
    }
}
