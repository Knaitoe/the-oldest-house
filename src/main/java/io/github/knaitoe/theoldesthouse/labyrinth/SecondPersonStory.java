package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * 0.4.274, Second Person (docs/SECOND_PERSON_0_4_274.md): the script, the book's latest page and the account, all written from the
 * reader's record. Original writing. Second person throughout, as the book's own kind is; nothing here says what any of it proves, and
 * nothing names the reader's gender, age or colour. The resident is "they", like everyone the House has not been told about.
 */
public final class SecondPersonStory {
    private SecondPersonStory(){}

    // ------------------------------------------------------------------------------------------------ the record's lists
    public static List<String> list(CompoundTag own,String key){var out=new ArrayList<String>();for(var t:own.getList(key,Tag.TAG_STRING))out.add(t.getAsString());return out;}
    static void add(CompoundTag own,String key,String value){var l=own.getList(key,Tag.TAG_STRING);for(var t:l)if(t.getAsString().equals(value))return;if(l.size()<32)l.add(StringTag.valueOf(value));own.put(key,l);}
    /** A page the reader turned: what they did, in the book's words, kept in order for the book and the account. */
    static void turned(CompoundTag own,String page){var l=own.getList("Pages",Tag.TAG_STRING);if(l.size()<40)l.add(StringTag.valueOf(page));own.put("Pages",l);}
    static boolean haven(CompoundTag own){return own.getCompound("Snap").getString("Home").equals("HAVEN");}
    static String routine(CompoundTag own){int r=own.getCompound("Snap").getInt("Routine");return r<0?"washcloth":HouseReadMoments.ROUTINES[Math.floorMod(r,HouseReadMoments.ROUTINES.length)];}
    static String thing(CompoundTag own,String thing){return switch(thing){case "photo"->"the photograph from home";case "drawing"->"the drawing";case "coat"->"the coat";default->"the "+routine(own);};}

    // ------------------------------------------------------------------------------------------------ the voices on the phone
    /** Who rings: a parent who is kind and starting to forget (a home the House read as a haven), or an old friend. */
    static String caller(CompoundTag own){return haven(own)?"Mum or Dad, on the line":"An old friend, on the line";}
    static List<String> call(CompoundTag own,int call){
        boolean tried=own.getString("Call1").equals("off");
        if(haven(own))return call==1?List.of("Hello? Love? It's only me.","I wanted to know you got in all right.","Did you say Tuesday? For the bed, I mean. Did you say Tuesday?")
                :tried?List.of("There you are. I tried and tried, you know. It just rang.","Listen, what's the new address? I've written it down somewhere.","Say it again. The number of the flat.")
                :List.of("Only me again. How is it?","Listen, what's the new address? I've written it down somewhere.","Say it again. The number of the flat.");
        return call==1?List.of("It's me. Just checking you're alive.","The new place. Is it awful?","It's awful, isn't it.")
                :tried?List.of("Oh, so you do exist. I tried and tried. It just rang and rang.","I'm coming to see it. Don't argue. A weekend.","You can show me round your awful flat.")
                :List.of("Me again. I've decided something.","I'm coming to see it. Don't argue. A weekend.","You can show me round your awful flat.");
    }
    /** What they say into the silence when the reader lifts the receiver and only listens. */
    static List<String> listened(CompoundTag own,int call){
        if(haven(own))return call==1?List.of("This line's terrible. I'll let you go.","Ring me when you're settled.","Did you say Tuesday?")
                :List.of("Never mind. I'll find it.","I've got it written on something.","Night, love. Night night.");
        return call==1?List.of("I can hear you breathing, you know.","You're doing the silent thing.","Ring me.")
                :List.of("Fine. Be mysterious.","I'm still coming.","Ring me, you coward.");
    }
    /** What the resident says when they catch the phone themselves. */
    static String caught(CompoundTag own,int call){
        if(haven(own))return call==1?"Hello? No, I'm here, I was in the bath. Tuesday, yes. Tuesday.":"It's flat two. Above the launderette. Two. Have you got a pen?";
        return call==1?"Alive. Just about. It's not awful. It's a bit awful.":"A weekend's fine. Bring your own pillow.";
    }

    // ------------------------------------------------------------------------------------------------ the words on glass
    static final List<String> WORDS=List.of("HELLO","SORRY","STAY","GO");
    static String sawWord(String word){return switch(word){case "HELLO"->"...Hello?";case "SORRY"->"Sorry for what?";case "STAY"->"I'm trying.";default->"Not yet.";};}

    // ------------------------------------------------------------------------------------------------ the book's latest page
    /** The book opened at its latest page: where things stand, then by number what the reader can do now. */
    public static String latest(CompoundTag own){
        int season=own.getInt("Season"),step=own.getInt("Step");var b=new StringBuilder();List<String> can;
        if(season==SecondPerson.SUMMER){switch(step){
            case SecondPerson.ARRIVE,SecondPerson.UNPACK->{b.append("Late summer. Boxes in every room. Somebody is moving in.\n\n");
                can=List.of("Take a thing out of its box before they get to it.","Put it where it should go, or somewhere else, or where they will never look. The bed has an underneath. The cupboard has a dark.","Open a door. Shut one.");}
            case SecondPerson.GLASS_HUNG->{b.append("The boxes are flat. They have hung a glass in the hall.\n\n");
                can=List.of("Turn the glass to the wall.","Stand where the glass faces you.","Breathe on a cold pane.");}
            case SecondPerson.BATH->{b.append("Evening. They are running a bath, and the telephone is going to ring.\n\n");
                can=List.of("When it rings, lift it, and listen.","Or put it down. Or leave it off the hook.","Or let it ring.","The mirror will steam. Glass that steams holds a word.");}
            case SecondPerson.NIGHT->{b.append("Night. They have gone to bed and cannot sleep.\n\n");
                can=List.of("Stand in their doorway.","Run the kitchen tap.","Sit in the armchair.","Shut their door on them.");}
            default->{b.append("Morning, thin through the street window. They slept, in the end.\n\n");
                can=List.of("When you want the time to pass, the airing cupboard at the end of the hall is warm. Go in and shut the door.");}
        }}
        else if(season==SecondPerson.AUTUMN){switch(step){
            case SecondPerson.IRIS_VISIT->{b.append(whileAway(own,1)).append("\n\n");
                can=List.of("Knock on the wall at the end of the passage. Somebody lives behind it.","Lift the phone if it rings.");}
            case SecondPerson.ROUTINE->{b.append("Somebody is coming for supper. They are getting ready.\n\n");
                can=List.of("If they cannot find what they need, set it out where they will see it.");}
            case SecondPerson.EVENING->{b.append("The guest is here. They are at the kitchen table.\n\n");
                can=List.of("Help: light the lamp, straighten the cloth.","Interfere: stand close enough to make the air go cold, knock, bang a door.","Be seen: stand in the dark window behind their chair.","Leave them alone: go into the bedroom and shut the door.");}
            case SecondPerson.PHOTO->{b.append("They are taking a picture of the room, to send to somebody.\n\n");
                can=List.of("Stand in the room, or out of it.");}
            case SecondPerson.CALL->{b.append("The telephone again.\n\n");
                can=List.of("Lift it, and listen.","Put it down. Leave it off the hook.","Let it ring.");}
            case SecondPerson.STAY->{b.append("It is late. The guest has asked if they can stay.\n\n");
                can=List.of("Make the flat cold.","Make it warm: the lamp.","Do nothing.");}
            default->{b.append("The flat is quiet.\n\n");
                can=List.of("The airing cupboard is warm. Go in and shut the door.");}
        }}
        else{b.append(whileAway(own,2)).append("\n\nIt is morning. The front door has been left on the latch.\n\n");
            can=own.getString("LastAct").isEmpty()?List.of("One last thing first, if you like: lay their "+routine(own)+" out where they will see it.","Or turn the glass round.","Or write a word on the window.","Then go out by the front door and down to the street."):List.of("Go out by the front door and down to the street.");}
        for(int i=0;i<can.size();i++)b.append(i+1).append(". ").append(can.get(i)).append("\n");
        var pages=list(own,"Pages");
        if(!pages.isEmpty()){b.append("\nThe pages you have turned:\n");for(int i=Math.max(0,pages.size()-6);i<pages.size();i++)b.append("— ").append(pages.get(i)).append("\n");}
        return b.toString();
    }
    /** "While you were in the cupboard...": what happened in the season the reader let pass. */
    public static String whileAway(CompoundTag own,int season){
        if(season==1){var b=new StringBuilder("While you were in the cupboard, the summer went on without you. They have a job now; the rota is on the fridge. There is a rug, and the radio on for company.");
            var placed=list(own,"PlacedThings");if(!placed.isEmpty())b.append(" Things you put where they live are still where you put them.");
            if(own.getString("Call1").equals("off"))b.append(" The phone was off the hook for a week before they noticed.");
            return b.toString();}
        return "While you were in the cupboard, the year went on without you: the frost, the short days, then a letter about the tenancy.";
    }

    // ------------------------------------------------------------------------------------------------ the decision
    /** At the last morning they stay or go. Not a score to reach: what the year was like, read off the record. */
    public static boolean stays(CompoundTag own){
        int stay=0,go=0;
        if(own.getBoolean("SamStayed"))stay++;
        if(own.getString("Photo").equals("in_kept"))stay++;else if(own.getString("Photo").equals("in_deleted"))go++;
        if(own.getBoolean("RoutineSet"))stay++;
        if(own.getBoolean("IrisKnocked"))stay++;
        switch(own.getString("Night")){case "shut","doorway"->stay++;case "tap"->go++;default->{}}
        switch(own.getString("Evening")){case "helped","alone"->stay++;case "seen","interfered"->go++;default->{}}
        for(var w:list(own,"WordsSeen")){if(w.equals("HELLO")||w.equals("STAY"))stay++;else if(w.equals("GO"))go++;}
        if(own.getString("Call1").equals("put_down")||own.getString("Call2").equals("put_down"))go++;
        return stay>=go;
    }

    // ------------------------------------------------------------------------------------------------ the account
    public static String account(CompoundTag own){
        var b=new StringBuilder();
        // The first thing they found moved.
        var found=list(own,"Found");
        if(!found.isEmpty()){var first=found.get(0);var parts=first.split(":",2);String what=thing(own,parts[0]),how=parts.length>1?parts[1]:"";
            b.append(switch(how){case "put_away"->"The first thing they found was "+what+", already where it belonged. \"I didn't put that there,\" they said, and then: \"I must have.\"";
                case "moved"->"The first thing they found was "+what+", somewhere they would not have put it. They left it there.";
                default->"The first thing they missed was "+what+". \"It was here,\" they said. \"It was in this box.\" It did not turn up for a long time.";});}
        else b.append("You moved nothing that first afternoon. They unpacked the way people do, one box and then the next, and said \"Right\" to nobody.");
        b.append("\n\n");
        // The routine and where it lived.
        String routine=routine(own);
        b.append(own.getBoolean("RoutineSet")?"Their "+routine+" went missing once, the evening it mattered, and you set it out where they would see it. \"Thanks,\" they said, to the room."
                :"Their "+routine+" lived "+(own.getString("RoutineWhere").isEmpty()?"on the bathroom shelf":own.getString("RoutineWhere"))+".").append("\n\n");
        // The calls.
        b.append(haven(own)?"The one who rang was a parent, kind, and starting to forget. ":"The one who rang was an old friend. ");
        b.append(callLine(own.getString("Call1"),1,own)).append(" ").append(callLine(own.getString("Call2"),2,own)).append("\n\n");
        // The first night.
        b.append(switch(own.getString("Night")){case "doorway"->"The first night you stood in their doorway, and they said \"...hello?\" into the dark.";case "tap"->"The first night you ran the kitchen tap, and they got up and turned it off, and stood a long time at the sink.";
            case "armchair"->"The first night you sat in the armchair, and nothing happened, which was its own answer.";case "shut"->"The first night you shut their door on them, and they slept.";default->"The first night you left them alone, and they slept at last, near dawn.";}).append("\n\n");
        // Sam and the evening.
        b.append(switch(own.getString("Evening")){case "helped"->"When Sam came for supper you lit the lamp and straightened the cloth. ";case "interfered"->"When Sam came for supper you made yourself felt, and Sam laughed and said the flat was haunted. ";
            case "seen"->"When Sam came for supper you stood in the dark window behind their chair, and Sam saw somebody standing there, and the evening ended. ";case "alone"->"When Sam came for supper you went into the bedroom and shut the door on yourself. ";default->"When Sam came for supper you let the evening be. ";});
        if(!own.getString("Evening").equals("seen"))b.append(own.getBoolean("SamStayed")?"Sam stayed.":"Sam went home.");
        b.append("\n\n");
        // Iris.
        if(own.getBoolean("IrisKnocked"))b.append(own.getBoolean("IrisTold")?"You knocked on Iris's wall, and she knocked back, and later she told them: \"You've got company. I don't mind it.\"":"You knocked on Iris's wall, and she knocked back.");
        else b.append("Iris across the landing brought the ends of things she had cooked. She said the flat had never been empty in her time.");
        b.append("\n\n");
        // The photograph.
        switch(own.getString("Photo")){case "in_kept"->b.append("They took a picture of the room, and you were in it. They looked at it a long time, and kept it.\n\n");case "in_deleted"->b.append("They took a picture of the room, and you were in it. They looked at it a long time, and deleted it.\n\n");default->{}}
        // The words.
        var words=list(own,"Words");var seen=list(own,"WordsSeen");
        if(!words.isEmpty()){b.append("You wrote on glass: ").append(String.join(", ",words)).append(".");if(!seen.isEmpty())b.append(" They read ").append(seen.size()==words.size()?"every one":String.join(", ",seen)).append(".");b.append("\n\n");}
        // The last morning.
        b.append(own.getString("Decision").equals("stay")?"On the last morning they had decided to stay. When you looked back from the street there was a light in the window, and they were standing at it."
                :"On the last morning they had decided to go. You watched from the street as they came out of the side door with one bag and walked to the bus stop and away.");
        String last=switch(own.getString("LastAct")){case "routine"->"you laid their "+routine+" out where they would see it.";case "glass"->"you turned the glass round.";case "word"->"you wrote one more word on the window.";default->"";};
        if(!last.isEmpty())b.append(" Before you went, ").append(last);
        b.append("\n\nThe door was on the latch the whole time. You went out by it.");
        return b.toString();
    }
    static String callLine(String what,int call,CompoundTag own){
        String which=call==1?"The first time":"The second time";
        return switch(what){case "lifted"->which+", you lifted the receiver and listened, and said nothing back.";case "put_down"->which+", you lifted it and put it down again.";
            case "off"->which+", you left it off the hook.";case "left"->which+", you let it ring, and they caught it themselves.";default->"";};
    }
}
