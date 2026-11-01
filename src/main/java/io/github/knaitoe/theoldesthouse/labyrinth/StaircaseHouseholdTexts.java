package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.nbt.CompoundTag;

/**
 * 0.4.214: the household's words in its own rooms: the door left in the shaft wall, what is said at the table, and the sheet the
 * household writes about the reader afterwards, in the voice of whoever was not in the scene. House writing: the reader is named or
 * "you", never given a gender, a colour or an age.
 */
public final class StaircaseHouseholdTexts {
    private StaircaseHouseholdTexts(){}
    public static String doorLeft(StaircaseLeakRooms.Kind kind){
        return switch(kind){
            case DINING->"A door has been left in the wall beside the landing. Painted, with a brass knob. Behind it, someone is laying a table.";
            case BEDROOM->"A door has been left in the wall beside the landing. Painted, with a brass knob. It smells of clean washing.";
            case MENDING->"A door has been left in the wall beside the landing. Painted, with a brass knob. There's lamplight under it.";
            case DAWN->"A door has been left in the wall beside the landing. Painted, with a brass knob. Behind it, very quietly, a kettle.";
            default->"A door has been left in the wall beside the landing, painted, with a brass knob.";
        };
    }
    public static String arrive(StaircaseLeakRooms.Kind kind,boolean unlaid){
        return switch(kind){
            case DINING->unlaid?"Sunday. Five places to lay this time. There's no chair at the table for you."
                :"Sunday. Somebody's busy in the kitchen. The table isn't laid yet.";
            case BEDROOM->"Washing day's over. The bed's stripped, and the basket's full and warm.";
            case MENDING->"Thursday, by the lamp. The blue coat has lost a button, and its sleeve wants a stitch.";
            case DAWN->"Before it's light. Somebody's asleep on the settee. Be quiet.";
            default->"";
        };
    }
    /** Sunday lunch, said around the table while the reader sits: speaker, then the line. {name} is the reader. */
    public static final List<String[]> LUNCH=List.of(
        new String[]{"Ruth","Pass the salt along, would you."},
        new String[]{"Jonah","It's meant to clear by four."},
        new String[]{"Bell","My knee says it won't."},
        new String[]{"Mara","Your knee said that last Sunday."},
        new String[]{"Marsh","Is there more of the gravy?"},
        new String[]{"Ruth","There's always more gravy."},
        new String[]{"Jonah","The man two doors down has painted his gate."},
        new String[]{"Bell","What colour?"},
        new String[]{"Jonah","He called it sage."},
        new String[]{"Mara","It's green."},
        new String[]{"Ruth","Eat while it's hot, {name}."},
        new String[]{"Marsh","Who laid the table? The forks match for once."},
        new String[]{"Bell","We should have the good cups out more often."},
        new String[]{"Ruth","That's what they're for."});
    public static String lunchLine(int i,String name){var l=LUNCH.get(i);return l[0]+": \""+l[1].replace("{name}",name)+"\"";}

    /** The household's sheet about the reader, kept with the sheet that opened the room. */
    public static String account(StaircaseLeakRooms.Kind kind,String result,String name,CompoundTag chore){
        boolean ok=result.equals("success");
        return switch(kind){
            case DINING->chore.getBoolean("Unlaid")
                ?(ok?"Sunday\n\nWe laid five places and "+name+" sat at the end, in the visitor's chair, and ate there with the rest of us. Nobody said anything about it. Ruth put the good cup by "+name+"'s hand herself.\n\nSabine"
                    :"Sunday\n\nFive places again, and "+name+" at the end in the visitor's chair, and then not there.\n\nSabine")
                :ok?"Sunday\n\n"+name+" laid the table this week. Forks first, the way we do it. Ruth changed two of them while "+name+" was at the cupboard, and nothing was said about it, which is how it should be. The cracked cup went to the flowers. There was a place at the end for our visitor, with a good cup, and nobody mentioned that either.\n\n"+name+" sat through to the end. When the chairs went back, the bowl in front of "+name+" was as empty as anyone's.\n\nSabine"
                :"Sunday\n\n"+name+" laid the table this week, and laid it well. Then "+name+" got up while the rest of us were still eating. Nobody said anything. The bowl stayed where it was until Ruth cleared it with the others.\n\nWe will see how many places get laid next Sunday.\n\nSabine";
            case BEDROOM->ok?"Fresh sheets\n\n"+name+" folded the sheets with me on Saturday, and waited for my end every time, though they could have been folded alone. The older ones went underneath. The striped pillowcase turned up after all. "+name+" found the odd sock in the fitted sheet and put it on the basket to wait for its partner.\n\nThe bed was made properly. I slept in it.\n\nA. Bell"
                :"Fresh sheets\n\n"+name+" made the bed on Saturday and it was a good bed, but one of the sheets was folded alone, and the crease is still in it. Ruth saw. She didn't say. It's in the airing cupboard now, on top, where we'll all see it until it's used.\n\nA. Bell";
            case MENDING->ok?"Thursday\n\nRuth showed "+name+" how to turn the sleeve before sewing it, and "+name+" turned it. The lamp was moved out of the light. The needle went on the third try, which is Ruth's number too. "+name+" chose "+chore.getString("Button")+" from the tin, and the coat has it now.\n\nE. Marsh"
                :"Thursday\n\n"+name+" sewed the sleeve of Ruth's coat shut, and Ruth unpicked it, and "+name+" sewed it shut again. Ruth hung the coat up as it was. She wears the other one now. Nobody minds. We have the tin, and Thursdays.\n\nE. Marsh";
            case DAWN->ok?"First light\n\n"+name+" was up before any of us. The kettle was taken off before it sang. The first cup was for warm hands and the second was by Ruth's head when she woke, still hot. Bell's plate was wrapped, with his name on it, and an extra potato under the paper.\n\nRuth slept through it all.\n\nMara"
                :"First light\n\nRuth woke before it was light and asked what time it was. "+name+" told her. Ten to six. She lay a while looking at the ceiling and then got up anyway. There was no sense going back to sleep by then.\n\nMara";
            default->"";
        };
    }
}
