package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.619, The Ones Already Inside, its nineteenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, "Personhood fragments"; the proposal's
 * "Personhood as an instrument of horror"). What the House has read of a reader changes, for that reader alone, what the lodging house
 * has made ready: never what anybody else sees, never what a mechanism needs, never which side anybody is on.
 *
 * <ul>
 * <li><b>HOME</b>, on first going upstairs: one of the doors up there is made ready the way the reader's home was read (turned down,
 * bolted from inside, the window unscrewed); and Vera, making her offer, frames it as rest, safety or a way out.</li>
 * <li><b>AGE</b> (settled), on first coming to the reader's own bed: one thing on the nightstand, of that stage of a life.</li>
 * <li><b>HAIR</b>, a hand on the reader's own body once its eyes are open: its hands try the reader's own routine and get it wrong.</li>
 * <li><b>SELF</b>, the hall stand's glass: as the reader left the coatroom's glass, covered or turned.</li>
 * <li><b>DRAWN_TO</b> (settled), privately in Vera's offer: company, or a room to oneself. Never said to anybody else; nobody named.</li>
 * <li><b>TONE and the House body</b>: the bodies in the beds already wear the reader's House body (0.4.604).</li>
 * </ul>
 * An unread, uncertain or turned-off read gives the whole neutral version: none of these lines, the hall glass as it always was.
 */
public final class LodgingSelf {
    private LodgingSelf(){}
    public static final String SNAP="Snap0619",HOME_SHOWN="HomeShown0619",AGE_SHOWN="AgeShown0619",HAIR_SHOWN="HairShown0619",GLASS_SHOWN="GlassShown0619",VERA_SHOWN="VeraShown0619";
    static final double BED=2.5;

    /** What the House had read, taken once on the reader's first arrival. Empty strings where it had read nothing it may use. */
    static CompoundTag snapshot(ServerPlayer p){
        var t=new CompoundTag();var d=LabyrinthData.get(p.server);var id=p.getUUID();
        t.putString("Home",HouseRead.value(d,id,HouseRead.Trait.HOME,false));
        t.putString("Age",HouseRead.value(d,id,HouseRead.Trait.AGE,true));
        t.putString("Hair",HouseRead.hair(p,false).orElse(""));
        t.putString("Drawn",HouseRead.drawnTo(p));
        t.putString("Glass",HouseRead.enabled(HouseRead.Trait.SELF)?HouseRead.seen(d,id).getCompound("coatroom").getString("Mirror"):"");
        return t;
    }
    static void arrive(ServerPlayer p,CompoundTag own){if(!own.contains(SNAP))own.put(SNAP,snapshot(p));}
    static CompoundTag snap(CompoundTag own){return own.getCompound(SNAP);}

    // ------------------------------------------------------------------------------------------------ the lines (every one new)
    public static String homeUpstairs(String home){return switch(home){
        case "HAVEN"->"Along the landing one bed has been turned down, the sheet folded back square, as if somebody were expected tonight.";
        case "DANGER"->"One of the doors along the landing has a new bolt on the inside of it, and the screws are still bright.";
        case "RUNAWAY"->"In one of the rooms along the landing the shutter screws have been taken out and laid in a row on the sill.";
        default->"";};}
    public static String ageNightstand(String age){return switch(age){
        case "ANALOG"->"On the nightstand, a pair of reading glasses, folded, the lenses wiped.";
        case "TRANSITIONAL"->"On the nightstand, a wristwatch, wound, lying face up.";
        case "DIGITAL"->"On the nightstand, a charger plugged into the wall, the cable running to nothing.";
        default->"";};}
    public static String hairRoutine(String hair){return switch(hair){
        case "WRAPPED"->"Its hands go up to its head, again and again, to tie a wrap it is not wearing.";
        case "BRAIDED_AND_OILED"->"Its fingers work at a braid that is not there, and start it again from the top.";
        case "BRUSHED_DRY"->"It draws a hand through its hair from the ends up, the wrong way, and does it again.";
        case "WASHED_AND_BARBERED"->"It runs a hand back over its head, slowly, as if the cut were new and it had not got used to it.";
        default->"";};}
    public static String glass(String mirror){
        if(mirror.startsWith("covered"))return "The hall glass has a cloth over it already, tucked in at the corners.";
        if(mirror.startsWith("turned"))return "The hall glass has been turned a little away from the door, so it catches the wall instead.";
        return "";
    }
    public static List<String> veraOffer(String home,String drawn){
        var out=new ArrayList<String>();
        switch(home){
            case "HAVEN"->out.add("You could rest. That's all it would be. Somebody else to get up in the mornings.");
            case "DANGER"->out.add("Nothing would get in. I'd see to the doors myself.");
            case "RUNAWAY"->out.add("You could go whenever you liked. Nobody here would come after you.");
            default->{}
        }
        switch(drawn){
            case "MEN","WOMEN","BOTH"->out.add("And you'd not be on your own in it. There'd be someone at the table glad to see you.");
            case "NO_ONE"->out.add("And nobody would ask anything of you. You'd have the room to yourself.");
            default->{}
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------------ where they land
    /** Every reader tick (LodgingHouse.reader): the landing on first going up, and the nightstand by the reader's own bed. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own,boolean firstUp){
        var s=snap(own);
        if(firstUp&&!own.getBoolean(HOME_SHOWN)){var line=homeUpstairs(s.getString("Home"));if(!line.isEmpty()){own.putBoolean(HOME_SHOWN,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,line);}}
        if(!own.getBoolean(AGE_SHOWN)){
            var line=ageNightstand(s.getString("Age"));if(line.isEmpty())return;
            int room=LodgingStory.room(p.server,b,p.getUUID());if(room<0)return;
            if(p.position().distanceToSqr(LodgingDoubles.lying(b,room))>BED*BED)return;
            own.putBoolean(AGE_SHOWN,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,line);
        }
    }
    /** A hand on the reader's own body once it is awake: the routine, if the House read one; null for the neutral line. */
    public static String touchedAwake(ServerPlayer p,CompoundTag own){
        var line=hairRoutine(snap(own).getString("Hair"));if(line.isEmpty())return null;
        if(!own.getBoolean(HAIR_SHOWN)){own.putBoolean(HAIR_SHOWN,true);LodgingHouse.save(p,own);}
        return line;
    }
    /** The hall stand's glass, as this reader left the coatroom's; null for the neutral line. */
    public static String hallGlass(ServerPlayer p,CompoundTag own){
        var line=glass(snap(own).getString("Glass"));if(line.isEmpty())return null;
        if(!own.getBoolean(GLASS_SHOWN)){own.putBoolean(GLASS_SHOWN,true);LodgingHouse.save(p,own);}
        return line;
    }
    /** Vera's offer, said to this reader alone, after the script's lines. */
    public static void offer(ServerPlayer p,LiteraryActor vera,CompoundTag own){
        var s=snap(own);var lines=veraOffer(s.getString("Home"),s.getString("Drawn"));if(lines.isEmpty())return;
        own.putBoolean(VERA_SHOWN,true);LodgingHouse.save(p,own);
        for(var line:lines)LodgingPeople.says(p,vera,"Vera",line);
    }
    /** The account's lines for what this reader was shown. */
    static void account(List<String> p,CompoundTag own){
        var s=snap(own);
        if(own.getBoolean(AGE_SHOWN))p.add(switch(s.getString("Age")){
            case "ANALOG"->"Somebody had left reading glasses by that bed.";
            case "TRANSITIONAL"->"Somebody had wound a watch and left it by that bed.";
            default->"Somebody had left a charger by that bed, plugged in to nothing.";});
        if(own.getBoolean(HAIR_SHOWN))p.add("Its hands kept trying what yours do every morning, and getting it wrong.");
        if(own.getBoolean(VERA_SHOWN))p.add(switch(s.getString("Home")){
            case "HAVEN"->"Vera made it sound like rest.";
            case "DANGER"->"Vera made it sound like being safe.";
            case "RUNAWAY"->"Vera made it sound like a way out.";
            default->"Vera made it sound like something you might want.";});
    }
}
