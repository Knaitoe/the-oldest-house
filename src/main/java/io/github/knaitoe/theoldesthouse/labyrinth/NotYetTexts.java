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
    /**
     * 0.4.666: the account, in the drawer under the reader's own cot once they have stayed down. It is written from what the House kept:
     * how the machine met them, how often it mended them, what it called and what it told them (and whether that was so), whom it hurt
     * to get them up, what it had read of home and of their years, and who lay down beside them. Its last page is in the machine's hand.
     */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add((own.getBoolean("TookFour")?"I came down through the machine to a ward. Further up I had let four of its people go, and it knew. It said there was only me now."
            :"I came down through the machine to a ward. It said it had been waiting for someone to keep.")
            +" There were cots in rows under lamps, and a window onto a field where the sun did not move.");
        int mended=own.getInt("Mended");
        p.add("The floor gave under me on the way in, and I fell further than anyone lives through. I woke on a cot with a seam on me I had not had."
            +(mended>1?" It did that "+count(mended)+" times in all. Every time there was another seam, and the screens said NOT YET.":" The screens said NOT YET."));
        if(own.contains("RuleFrom")){int who=Math.max(0,Math.min(2,own.getInt("RuleFrom")));
            p.add("Past the ward, in a long room, three people lay with their eyes open. "+NAMES[who]+" told me the only rule there is: it wants you up.");}
        int gotUp=own.getInt("GotUp");
        p.add("It asked me to lie down, and said it would keep me."+(gotUp<=0?" I lay down on the cot it had given me.":" I got up "+times(gotUp)+" before I stayed down. Every time, it said that everyone gets up, and the next time was longer."));
        var did=new ArrayList<String>();
        did.add("it heated the cot under me until the coils glowed");
        var called=NotYetTrial.names(own,"Called");called.removeIf(String::isEmpty);
        if(!called.isEmpty()&&own.getBoolean("CalledOthers"))did.add("it called "+CoopAccounts.names(called)+" from the far end of the ward, low down, as if from the floor");
        else if(!called.isEmpty())did.add("something past the arch whistled and called "+CoopAccounts.names(called)+", and waited for "+(called.size()==1?"it":"them")+" to come");
        else did.add("it said my own name by the far door, and then nearer");
        p.add("While I lay there "+String.join(", and ",did)+".");
        for(var row:NotYetTrial.lies(own)){
            String about=row.getString("About");boolean so=row.getBoolean("True");String line=row.getString("Line");
            if(line.equals(DOOR_IS_OPEN))p.add("The screens said the door at the end was open and nothing was keeping me. "+(so?"That time it was.":"It was not."));
            else if(line.startsWith(up(about)+" GOT UP. THEY ARE AT THE DOOR"))p.add("The screens said "+about+" had got up and was standing at the door. "+(so?about+" had.":about+" had not."));
            else if(line.startsWith(up(about)+" GOT UP. DO YOU WANT"))p.add("When "+about+" got up, it told me so. That was true.");
        }
        var used=NotYetTrial.names(own,"Used");var hurtFor=NotYetTrial.names(own,"HurtFor");
        if(!used.isEmpty())p.add("It hurt "+CoopAccounts.names(used)+" where they lay, and told me it would stop if I got up. I did not get up.");
        else if(own.getBoolean("ScreenMan"))p.add("It showed me the man from the long room on the screen over my cot, with something being done to him, and said it would stop if I got up. He said to stay down.");
        if(!hurtFor.isEmpty())p.add("It hurt me where I lay, and told "+CoopAccounts.names(hurtFor)+" that they could stop it.");
        if(own.getBoolean("Cold"))p.add("Then it let the cold in through the grille, and said I could get up and get warm."+(own.getInt("MendedLying")>0?" Then it killed me, and would not let that count either.":""));
        var beside=NotYetTrial.names(own,"Beside");
        p.add("I stayed down. Everything went quiet at once. The lamp lifted away from my face, and at the far end of the long room a door slid open."
            +(beside.isEmpty()?"":" "+CoopAccounts.names(beside)+(beside.size()==1?" was":" were")+" lying in the ward with me."));
        String home=switch(own.getString("HomeRead")){
            case "HAVEN"->"that someone once kept me warm and fed, and that it could do it for longer";
            case "DANGER"->"that I knew what it was to be kept, and that it did it better";
            case "RUNAWAY"->"that I had left every place that ever kept me, and would not leave this one";
            default->"";};
        String counter=switch(own.getInt("CounterKind")){case NotYet.FLAP->"cards that fell one over another";case NotYet.SEGMENT->"bars of light";default->"drums that turned";};
        p.add((home.isEmpty()?"":"When I first came into the ward it had told me "+home+". ")
            +"On the way out I passed under the counter over the arch. It was counting my years, on "+counter+". It kept counting after I left.");
        p.add("My cot was made when I looked back, tight, the way the others were. This was in the drawer under it.");
        p.add(up(name)+". IN INK.\n\nMENDED: "+up(count(Math.max(mended,1)))+".\nGOT UP: "+up(count(gotUp))+".\nSTAYED DOWN.\n\nNOT YET.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.NOT_YET),name,pages(p.toArray(String[]::new)));
    }
    private static String times(int n){return n==1?"once":n==2?"twice":count(n)+" times";}
    /** 0.4.666: as the drawer under the reader's own cot comes open. */
    public static final String DRAWER="The drawer under your cot slides out a little way on its own. There is a book in it.";
    /** 0.4.666: what each of the long room's three says to a reader who stayed down, and says again after. */
    public static String stayed(int who,ServerPlayer reader){
        return switch(who){
            case INES_->"It went quiet. I heard it from here. I didn't know it could.";
            case BRAM_->"The door's open for you. Don't stand in it long. It'll take that as asking.";
            default->HouseRead.address(reader,"Go on, then. It won't look at you again.","Go on, son. It won't look at you again.",
                "Go on, pet. It won't look at you again.","Go on, pet. It won't look at you again.");
        };
    }

    // ------------------------------------------------------------------------------------------------ 0.4.665: lie down
    /** The machine's invitation, once a visit to a reader it has mended who has heard the rule; and to one who stayed down. */
    public static final String INVITE="LIE DOWN. I WILL KEEP YOU.",NOT_YOU="NOT YOU. NOT NOW.";
    /** A hand on a cot that is not theirs. */
    public static final String SOMEONE_ELSES="This cot is made up for someone else. There is a name on the card at its foot, in pencil.";
    /** Lying down; and lying down again after getting up. */
    public static final String LIE="You lie down on the cot. The lamp bends down close over your face, and stays there.",AGAIN="AGAIN, THEN.";
    /** 1. The cot heats. */
    public static final String HEAT="The frame under you is warm. Then it is hot. The coils under the mattress have begun to glow.";
    /** 2. A name called: another reader's, a companion the House saw them lose, or, with neither, their own. */
    public static String callOther(String name){return "From the far end of the ward, low down, as if from the floor, someone calls \u201c"+name+"\u201d.";}
    public static String callLost(String name){return "Past the arch, something whistles and calls \u201c"+name+"\u201d, and waits for it to come.";}
    public static final String CALL_SELF="By the far door something says your name. Then it says it again, nearer.";
    /** 3. The screens: the door open, or another reader at it. */
    public static final String DOOR_IS_OPEN="THE DOOR AT THE END IS OPEN. NOTHING IS KEEPING YOU.";
    public static String gotUpLie(String name){return up(name)+" GOT UP. THEY ARE AT THE DOOR. THEY ARE NOT WAITING FOR YOU.";}
    /** 4. Someone else is hurt: another reader lying down, or, alone, the man in the long room on the screen. */
    public static String thatIs(String name){return "THAT IS "+up(name)+". GET UP AND I WILL STOP.";}
    public static String couldStop(String name){return up(name)+" COULD STOP THIS. THEY ARE LYING THERE.";}
    public static final String I_WILL_STOP="GET UP AND I WILL STOP.";
    public static final String ON_THE_SCREEN="On the screen over your cot: the man from the long room, in his cot. Something is being done to him. His face does not move.";
    public static final String STAY_DOWN_HEARD="From the screen, the man's voice, quite clearly: \u201cStay down.\u201d";
    /** 5. The cold, and what it says with it. */
    public static final String COLD="Cold comes in through the grille by the floor. The blanket stiffens over you.",WARM="YOU COULD GET UP AND GET WARM.";
    /** Getting up: to the reader, and to everyone still lying down. */
    public static final String THERE="THERE. EVERYONE GETS UP.";
    public static String gotUpTrue(String name){return up(name)+" GOT UP. DO YOU WANT TO BE THE ONE I KEEP?";}
    /** It looks away. */
    public static final String QUIET="Then everything goes quiet at once. The lamp lifts away from your face. At the far end of the long room, a door slides open.";
    private static String up(String name){return name.toUpperCase(java.util.Locale.ROOT);}
}
