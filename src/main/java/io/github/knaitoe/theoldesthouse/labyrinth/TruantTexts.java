package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * 0.4.264, what Johnny Truant says (docs/TRUANT_0_4_264.md). He is tired and plain and helps before he asks. Over his head and in the
 * conversation panel, never in chat. He never says what the reader is, only what they did and said; he names himself once, with
 * "Johnny" and nothing after it.
 * <p>
 * 0.4.282 (docs/TRUANT_0_4_282.md): the claimant and the collector. What he claims is always his, spoken in his voice; it either
 * matches what the reader actually said or is plainly his own allegation, and nothing in the narration agrees with him. His papers
 * retell what a reader told him, in the first person, from the answer they actually gave.
 */
public final class TruantTexts {
    private TruantTexts(){}
    static final String REFRESHMENT="I've got bread. Coffee's still hot if you'd rather. The cap's a cup.";
    static final String COFFEE="Hold it by the rim. I always forget how hot the bottom gets.";
    static final String BREAD="Here. I tore off the part I'd already touched.";
    // ------------------------------------------------------------------ what he says while he helps
    public static final String HOLDING="Go on. I've got it.";
    public static final String STEADY="Easy. I've got you.";
    public static final String CAUGHT="Got you.";
    public static final String THERE="There.";
    public static final String LAMP="Stay a minute. Listen before you go.";
    public static final String STRUCK="Don't.";
    // ------------------------------------------------------------------ the panel
    public static final String ASK_DOOR="What was behind you?";
    public static final String ASK="What did you find?";
    public static final String DECLINED="All right. You don't owe me it.";
    /** Asked who he is: a pause, the name, and no more. */
    public static final List<String> NAME=List.of("Me?","Johnny.","That'll do. It's what's on my papers.");
    public static final String PROMISE="When you get through, tell me what was in there. Will you?";
    public static final String GOODBYE="Go on, then. Mind how you go.";
    public static String promised(String said){
        return switch(said){
            case "agreed"->"Good. I'll be about.";
            case "maybe"->"Maybe's fine. Maybe's more than most.";
            default->"Fair enough.";
        };
    }
    /** Turned to again once the meeting's talk is done. */
    public static final String INTERRUPTED="You were gone a while. We can leave it there.";
    public static String again(CompoundTag own){return own.getString("Promise").equals("agreed")&&!own.getBoolean("Kept")?"Go on. You know where I am.":"Go on. I'll be about.";}

    /**
     * What he says first: at the first talk, something about the place he is in; afterwards, what that reader actually promised (only
     * if they did; nothing is claimed after a refusal), thanks once for a promise kept, and the last place they told him about.
     */
    public static List<String> remembers(CompoundTag own,boolean first,@Nullable LabyrinthPlace place){
        List<String> out=new ArrayList<>();
        if(first){
            out.add(place==LabyrinthPlace.TRUANT_DOOR?"It shuts on people. I don't know who set it like that."
                :place==LabyrinthPlace.TRUANT_LEDGE?"You didn't look down. Most people look down."
                :"Sit if you like. The floor's dry.");
            return out;
        }
        String promise=own.getString("Promise");boolean kept=own.getBoolean("Kept");
        if(kept&&!own.getBoolean("Thanked"))out.add("You came back and told me, like you said. People don't, in here.");
        else if(!kept&&promise.equals("agreed"))out.add("You said you'd tell me.");
        else if(!kept&&promise.equals("maybe"))out.add("You said maybe.");
        var told=own.getList("Told",Tag.TAG_STRING);
        if(!told.isEmpty())out.add("Last time it was "+StaircaseProse.place(told.getString(told.size()-1))+".");
        if(out.isEmpty())out.add("You again. Good.");
        return out;
    }
    /** Told about a place: he says its name back. */
    public static String heard(String placeId){var name=StaircaseProse.place(placeId);return Character.toUpperCase(name.charAt(0))+name.substring(1)+".";}

    /** The one thing he asks after a place, the reader's three answers to it, and what he says to each. */
    public record Follow(String question,String[] answers,String[] replies){}
    static final List<Follow> FOLLOWS=List.of(
        new Follow("How did you get out?",
            new String[]{"The way I came in.","Another door.","I'm not sure I did."},
            new String[]{"That door doesn't always stay. You were lucky.","That's how it goes. Never the same door twice.","No. Nobody's ever sure."}),
        new Follow("Was there anyone else in there?",
            new String[]{"There was.","Only me.","I couldn't tell."},
            new String[]{"Then you weren't on your own. Hold on to that.","Only you. All right.","That's the worst of it, not being able to tell."}),
        new Follow("How long were you in there, do you think?",
            new String[]{"Not long.","Too long.","I lost track."},
            new String[]{"It's always longer than that.","It always is.","Everyone does. I've stopped keeping count myself."}),
        new Follow("Did it look like anywhere you know?",
            new String[]{"It did.","Not really.","I'd rather not say."},
            new String[]{"They do that. It's how they keep you.","Good. The ones you know are worse.","Fair enough."}));
    // ------------------------------------------------------------------ 0.4.282: the claimant and the collector
    public static final String CAREFUL="Careful.";
    public static final String NOT_YET="Not yet.";
    public static final String STEP_ASIDE="Go on, then.";
    public static final String GIVE_WAY="All right. All right. Go on.";
    public static final String WAIT="Then you can wait.";
    public static final String HANDS_OFF="I won't touch you. I'm right here if you slip.";
    public static final String WRIST="You first. Then tell me.";
    public static final String LEAN="You skipped something.";
    public static final String STILL_MINE="You still have something of mine.";
    public static final String WATCHING="Like that. Just like that.";
    public static final String WELL="Well? What's on the other side?";
    public static final String OUT="There. Now I can see.";
    public static final String BEHIND="Go on. I'll be behind you.";
    public static final String FOR_NOW="That'll do. For now.";
    public static final String PAGE_NOTE="He has a page in his hands, close to the light. He reads it out to you.";
    public static final String LAMP_OUT_NOTE="You reach past him and put the lamp out.";
    public static final String LANTERN_OUT_NOTE="You reach across the desk and put the lantern out.";
    public static final List<String> CORRECTED=List.of("I got you through. Half of it's mine.","I'm only getting it down.");
    public static final List<String> RETOLD=List.of("Not that part.","I didn't ask for the part you could tell anybody.");
    public static final List<String> CHALLENGED=List.of("Did you? Then you left something out.");
    public static final List<String> CHALLENGED_LUCID=List.of("I know. I know you did.","Tell it again anyway. It comes out different every time.");

    /** What he claims, and the lines he claims it in. */
    public record Claim(String id,List<String> lines){}
    /**
     * His claim, from the record: a promise kept still leaves "what you left behind"; an unkept promise is said back as it was made
     * (a "maybe" he now hears as yes); a refusal or no promise at all becomes a debt for help he really gave; with no help, his places
     * themselves. When he has promised to ask for nothing, he remembers saying it and asks anyway.
     */
    public static Claim claim(CompoundTag own){
        String promise=own.getString("Promise");List<String> out=new ArrayList<>();
        var claims=own.getList("Claims",Tag.TAG_COMPOUND);int lastClaim=claims.isEmpty()?0:claims.getCompound(claims.size()-1).getInt("At");
        if(own.getInt("HisPromise")>lastClaim)out.add("I said I wouldn't ask. I know I said it.");
        if(own.getBoolean("Kept")){out.add("You came back and told me about "+StaircaseProse.place(own.getString("KeptPlace"))+". You told me how you got out.");out.add("You didn't tell me what you left behind.");return new Claim("the_rest",out);}
        if(promise.equals("agreed")){out.add("You said you'd come back.");return new Claim("come_back",out);}
        if(promise.equals("maybe")){out.add("You said maybe. Maybe means yes, in here.");return new Claim("maybe_was_yes",out);}
        if(own.getInt("Help")!=0){if(promise.equals("refused"))out.add("You said no. I heard you.");out.add("I got you through, didn't I?");out.add("That's worth something. In here it's worth a lot.");return new Claim("owed_for_help",out);}
        out.add("You keep coming through my places.");out.add("Every time, you take a bit of something with you.");return new Claim("my_places",out);
    }
    /** The collector's question: about a place they already told him of, wanting more of it. */
    public static String collect(String placeId,int meeting){
        String name=StaircaseProse.place(placeId);
        return switch(Math.floorMod(meeting,3)){
            case 0->"Start before "+name+". You skipped something.";
            case 1->"Tell me what happened in "+name+" after the light went.";
            default->cap(name)+". I haven't had the ending.";
        };
    }
    static String cap(String s){return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}

    /** A page of his papers: the place, and what he has written of it. */
    public record Page(String place,List<String> lines){}
    /** For each of his four questions, each of the three answers, retold as his own. */
    static final String[][] PAGES={
        {"\"I got out of {place} the way I came in. The door stayed for me. It doesn't, always.\"",
         "\"I got out of {place} by another door. Never the same door twice. I knew that going in.\"",
         "\"I'm not sure I got out of {place}. Some mornings I'm sure.\""},
        {"\"There was somebody else in {place} with me. I held on to that.\"",
         "\"There was nobody in {place} but me. I looked.\"",
         "\"I couldn't tell who else was in {place}. That was the worst of it.\""},
        {"\"I wasn't long in {place}. It was longer than that.\"",
         "\"I was in {place} too long.\"",
         "\"I lost track of the time in {place}. I stopped counting.\""},
        {"\"{Place} looked like somewhere I knew. That's how it kept me.\"",
         "\"{Place} didn't look like anywhere I knew. I was glad.\"",
         "\"I won't say what {place} looked like. Not to anybody.\""}};
    /** The latest place the reader told him of and answered his question about, in the first person; null if none. */
    public static @Nullable Page page(CompoundTag own){
        var told=own.getList("Told",Tag.TAG_STRING);var answers=own.getCompound("Answers");
        for(int i=told.size()-1;i>=0;i--){
            String id=told.getString(i);if(!answers.contains(id))continue;
            int k=Math.max(0,Math.min(2,answers.getInt(id)));int f=Math.floorMod(id.hashCode(),FOLLOWS.size());String name=StaircaseProse.place(id);
            return new Page(id,List.of(PAGES[f][k].replace("{place}",name).replace("{Place}",cap(name))));
        }
        return null;
    }
    /** After he has harmed them: what he did, in his words, and that he is asking for nothing. */
    public static List<String> afterHarm(CompoundTag own){
        String nothing="Sit, if you want. I'm not asking you for anything.";
        return switch(own.getString("Harm")){
            case "shove"->List.of("The ledge.","I don't know what that was. I've gone over it and I don't know.",nothing);
            case "doorway"->List.of("I stood in your way, last time. I won't today.",nothing);
            case "cover"->List.of("I put your light out. I had hold of you.","I shouldn't have.",nothing);
            case "shed"->List.of("I dropped the hasp. I heard you trying it.","The wedge is staying in today.",nothing);
            case "false_wind_signal"->List.of("You could see the trees. I knew you were watching my hand instead.","I won't do that again.",nothing);
            case "stock_gate"->List.of("I let the chain go while you were in there.","Go ahead. I'll hold it this time.",nothing);
            case "coffee"->List.of("You could have gone. I should have let you.",nothing);
            case "sentence"->List.of("I left a line out, reading it to them. I know which one.",nothing);
            default->List.of(nothing);
        };
    }
    /**
     * How worn he is, in one line at an ordinary meeting past twenty: he asks again a question they have answered; he tells their story
     * as his own; or he remembers the help he gave better than what he said afterwards. Null when there is nothing to wear on.
     */
    public static @Nullable String worn(CompoundTag own,int worn){
        var told=own.getList("Told",Tag.TAG_STRING);if(worn<1||told.isEmpty())return null;
        String last=told.getString(told.size()-1);var page=page(own);
        return switch(worn%3){
            case 1->follow(last).question();
            case 2->page!=null?page.lines().get(0):follow(last).question();
            default->own.getInt("HisPromise")>0?"I got you through, once. I remember that much. I don't remember what I said after.":collect(last,worn);
        };
    }
    /** The favour he asks, fitted to the place. */
    public static List<String> favour(String what){
        return switch(what){
            case "go_back"->List.of("Then do something for me.","Go back through. Shut it behind you, and tell me what's on the other side when it's shut.");
            case "cross_alone"->List.of("Then do something for me.","Go back over on your own. I want to watch how you do it.");
            case "shed_hasp"->List.of("Lift the hasp for me before you go. From out there, you can reach it.");
            case "read_wind"->List.of("Look at those leaves for me. Let me know when it's dropped.");
            case "stock_chain"->List.of("Put the chain back through when you're clear. Don't leave them a gap.");
            case "lantern_out"->List.of("Then do something for me.","Put the lantern out. I know these by heart. Do you?");
            default->List.of("Then do something for me.","Put the lamp out. I want to see what's past it.");
        };
    }
    public static String agreed(String what){return what.equals("go_back")?"Go on, then. I won't hold it for you.":what.equals("cross_alone")?"I'll watch.":"Good.";}
    /** The same question for the same place, every time. */
    public static Follow follow(String placeId){return FOLLOWS.get(Math.floorMod(placeId.hashCode(),FOLLOWS.size()));}
}
