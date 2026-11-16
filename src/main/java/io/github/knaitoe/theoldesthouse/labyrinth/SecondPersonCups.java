package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPerson.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.SecondPersonSite.*;

/**
 * 0.4.636, Second Person, E4: two cups (docs/SECOND_PERSON_0_4_636.md). A reader who has let themselves be known gently by the spring
 * finds, at the last cupboard, not the last morning but the last evening: two cups on the kitchen table. The resident talks to the reader
 * through it, about Iris, the phone, Sam and the face in the glass, asks one thing back, and the glass offers one word to answer with.
 * Whether they stay or go, they say "You can stay," or "You can come." The book's last line: "They will get older here. You won't."
 */
public final class SecondPersonCups {
    private SecondPersonCups(){}
    /** The flat's last evening, its own season, compared only by equality like the others. */
    public static final int TWO_CUPS=6;
    /** How kind the year must have been at the least, and by how much kinder than frightening. */
    static final int GENTLE_AT_LEAST=6,GENTLE_OVER_FRIGHT=3;
    static final int SIT_WAIT=600,LINE_EVERY=100,ANSWER_WAIT=900;
    /** The cups stand where the cloth lies, on the kitchen table between the two chairs. */
    public static final net.minecraft.core.BlockPos CUPS_AT=CLOTH;

    public static final String EVENING="The last evening of the year. On the kitchen table there are two cups.";
    public static final List<String> OPENING=List.of("I made two. I don't know why I made two.","Sit down. If you sit.");
    public static final String QUESTION="Will you be here when I wake up?";
    public static final String ASK_CUE="Breathe on the glass. It holds one word.";
    public static final String STAY="You can stay.",COME="You can come.";
    public static final String LAST_LINE="They will get older here. You won't.";

    public static boolean cups(CompoundTag own){return seasonOf(own)==TWO_CUPS;}
    /** Known gently: a year far kinder than frightening, and once at least the reader let themselves be met. */
    public static boolean known(CompoundTag own){
        int g=SecondPersonStory.gentle(own),f=SecondPersonStory.fright(own);
        if(g<GENTLE_AT_LEAST||g<f+GENTLE_OVER_FRIGHT)return false;
        String seen=own.getString("Seen");
        if(seen.equals("still")||seen.equals("touch_kind")||seen.equals("word"))return true;
        if(!own.getString("SpringAnswer").isEmpty())return true;
        for(var w:SecondPersonStory.list(own,"WordsSeen"))if(w.equals("HELLO")||w.equals("STAY")||w.equals("SORRY"))return true;
        return false;
    }

    /** At the last cupboard the year turns into its last evening instead of its last morning. */
    static void begin(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own){
        if(own.getString("Decision").isEmpty())own.putString("Decision",SecondPersonStory.stays(own)?"stay":"go");
        own.putInt("Season",TWO_CUPS);go(own,0);
        open(l,f.base,FLAT_DOOR,false);
        cue(p,EVENING);
        SecondPersonStory.turned(own,"You went into the cupboard at the end of the spring, and came out into the last evening.");
    }

    static Mark residentMark(CompoundTag own){return seat(RESIDENT_CHAIR,EAST);}
    static long hour(CompoundTag own){return 13200;}

    /** What they tell the reader across the table, from the year as it was. */
    static List<String> talk(CompoundTag own){
        var out=new ArrayList<String>(OPENING);
        if(own.getBoolean("SleeveFinished"))out.add("I finished Iris's sleeve. It's the wrong length. I don't care.");
        else if(own.getBoolean("IrisKnocked"))out.add("Iris knew. She said I had company. I thought she meant a cat.");
        else out.add("Iris used to say this flat was never empty.");
        out.add(SecondPersonStory.haven(own)?"I rang home on Sunday. I said I was all right. I think I meant it.":"I rang back, in the end. We talked for an hour about nothing.");
        out.add(own.getBoolean("SamStayed")?"Sam comes at the weekends now. Sam always comes.":"Sam asked after you. I said you were fine. I don't know why I said that.");
        out.add(switch(own.getString("Seen")){
            case "still"->"You stood still for me, that night in the glass. I've thought about it a lot.";
            case "touch_kind"->"That night you put your hand on me. It was cold. It was kind.";
            case "word"->"You wrote on the glass, that night. I kept it.";
            default->"I think I've seen you in the glass. Or I've wanted to.";
        });
        return out;
    }
    static String reply(String word){return switch(word){
        case "YES"->"Good.";case "NO"->"No. No, I know.";case "HELLO"->"Hello.";case "STAY"->"All right.";case "SORRY"->"Don't be.";case "GO"->"...All right.";
        default->"That's all right. You don't have to.";};}
    static String closing(CompoundTag own){return own.getString("Decision").equals("stay")?STAY:COME;}

    static void evening(ServerLevel l,Flat f,ServerPlayer p,CompoundTag own,Vec3 r,@Nullable LiteraryActor a){
        var b=f.base;int sub=sub(own),dt=dt(own);
        switch(sub){
            case 0->{
                if(dt==1){lamp(l,b,true);SecondPersonSite.set(l,b,CUPS_AT,SecondPersonBlock.of(SecondPersonBlock.Kind.CUPS,Direction.NORTH,0));sound(p,SoundEvents.DECORATED_POT_PLACE,b,CUPS_AT,.6F,1.2F);}
                boolean atTable=r.distanceTo(Vec3.atCenterOf(GUEST_CHAIR))<2.5;
                if(dt>40&&(atTable||dt>=SIT_WAIT))next(own,1);
            }
            case 1->{
                var lines=talk(own);int i=dt/LINE_EVERY;
                if(dt%LINE_EVERY==10&&i<lines.size())say(f,a,lines.get(i));
                if(i>=lines.size())next(own,2);
            }
            case 2->{
                if(dt==10)say(f,a,QUESTION);
                if(dt==40)cue(p,ASK_CUE);
                if(own.contains("CupsAnswer")&&dt>40)next(own,3);
                else if(dt>=ANSWER_WAIT){own.putString("CupsAnswer","");next(own,3);}
            }
            case 3->{
                if(dt==20)say(f,a,reply(own.getString("CupsAnswer")));
                if(dt==110)say(f,a,closing(own));
                if(dt==170){SecondPersonSite.set(l,b,CUPS_AT,SecondPersonBlock.of(SecondPersonBlock.Kind.CUPS,Direction.NORTH,1));sound(p,SoundEvents.GENERIC_DRINK,b,CUPS_AT,.5F,1);}
                if(dt==220){cue(p,LAST_LINE);SecondPersonStory.turned(own,"They set out two cups, and talked to you through the last evening.");}
                if(dt>=300){open(l,b,FLAT_DOOR,true);benchBook(l,b,true);LiteraryVignettes.ready(p,SecondPerson.PLACE,own,"two_cups");
                    cue(p,"On the bench at the bus stop, where nobody waits, there is a book.");next(own,4);}
            }
            default->{}
        }
    }

    /** While they wait for their one answer, a word on the glass is read at once. */
    static boolean asking(CompoundTag own){return cups(own)&&sub(own)==2&&!own.contains("CupsAnswer");}
    static void answered(CompoundTag own,String word){
        own.putString("CupsAnswer",word);
        SecondPersonStory.turned(own,"They asked if you would be here when they woke up. You wrote "+word+" on the glass.");
    }

    static String page(CompoundTag own){
        return switch(sub(own)){
            case 0->EVENING+"\n\n1. Sit with them.\n";
            case 1->"They are talking to you.\n\n1. Listen.\n";
            case 2->"They have asked you one thing.\n\n1. "+ASK_CUE+"\n2. Or say nothing.\n";
            case 3->"They have finished their cup.\n\n";
            default->LAST_LINE+"\n\n1. Go out by the front door and down to the street.\n";
        };
    }

    static void account(StringBuilder b,CompoundTag own){
        b.append("On the last evening of the year they set out two cups, and talked to you across the kitchen table: about Iris, about the phone, about Sam, about the face in the glass.");
        b.append(" They asked if you would be here when they woke up.");
        String w=own.getString("CupsAnswer");
        b.append(w.isEmpty()?" You did not answer, and they said that was all right.":" You wrote "+w+" on the glass.");
        b.append(" They said, \"").append(closing(own)).append("\"");
        b.append("\n\n").append(LAST_LINE);
    }
}
