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
 */
public final class TruantTexts {
    private TruantTexts(){}
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
    /** The same question for the same place, every time. */
    public static Follow follow(String placeId){return FOLLOWS.get(Math.floorMod(placeId.hashCode(),FOLLOWS.size()));}
}
