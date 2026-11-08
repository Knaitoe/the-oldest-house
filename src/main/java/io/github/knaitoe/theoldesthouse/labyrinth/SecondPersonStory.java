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
    static String thing(CompoundTag own,String thing){return switch(thing){case "photo"->"the photograph from home";case "drawing"->"the drawing";case "coat"->"the coat";case "ticket"->"the train ticket";case "sleeve"->"Iris's sleeve";default->"the "+routine(own);};}

    // ------------------------------------------------------------------------------------------------ the voices on the phone
    /** Who rings: a parent who is kind and starting to forget (a home the House read as a haven), or an old friend. */
    static String caller(CompoundTag own){return caller(own,1);}
    /** 0.4.369: the third call, at the end of the winter, is the hospital ward for a reader whose home is a haven. */
    static String caller(CompoundTag own,int call){return haven(own)?call==3?"The ward, on the line":"Mum or Dad, on the line":"An old friend, on the line";}
    static List<String> call(CompoundTag own,int call){
        if(call==3)return haven(own)?List.of("Hello? Is this the right number? This is the ward.","We've someone here asking for you. Asking and asking.","If you can come. Even for an hour.")
                :List.of("You didn't come. We waited for both trains.","It's fine. It's fine. Happy new year.","Ring me. Or don't. Just be alive.");
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
        if(call==3)return haven(own)?List.of("Hello? ...I'll put it down that I rang.","I'll tell them you're coming.","I'll tell them something."):List.of("I know you're there.","I'm not angry. I was. I'm not.","Happy new year, you idiot.");
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
    /** 0.4.369: from winter the glass also holds a yes and a no. */
    static List<String> words(CompoundTag own){return own.getInt("Season")>=SecondPerson.WINTER?List.of("HELLO","SORRY","STAY","GO","YES","NO"):WORDS;}
    static String sawWord(String word){return switch(word){case "HELLO"->"...Hello?";case "SORRY"->"Sorry for what?";case "STAY"->"I'm trying.";case "YES"->"Yes what?";case "NO"->"No to what?";default->"Not yet.";};}
    /** What they say to a word written while they look at the reader in the glass. */
    static String seenWord(String word){return switch(word){case "HELLO"->"...Hello. Hello.";case "SORRY"->"What for? What are you sorry for?";case "STAY"->"I wasn't going anywhere.";case "GO"->"This is my flat.";case "YES"->"Yes. All right. Yes.";case "NO"->"No what? No what?";default->"...";};}

    // ------------------------------------------------------------------------------------------------ 0.4.369, the winter ticket
    /** What was in the post on the hall table. */
    static String ticketDesc(CompoundTag own){return haven(own)?"a letter from home with a train ticket in it, and on the envelope, in a hand they know: They're in hospital. Come when you can."
            :"a card from the old friend with a train ticket folded inside: You're coming for New Year. It's non-refundable.";}
    static String ticketLine(CompoundTag own){return haven(own)?"They're asking for me.":"New Year. You absolute— New Year.";}
    static String backLine(CompoundTag own){return haven(own)?"They knew me. Not the first day. Then they did.":"I'd forgotten I liked people.";}

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
        else if(season==SecondPerson.WINTER){String r=routine(own);switch(step){
            case SecondPerson.W_FUNERAL->{b.append(whileAway(own,2)).append("\n\n");
                can=List.of("Iris's tin is on the kitchen counter. Open it, if you like.","Knock on her wall.");}
            case SecondPerson.W_MORNING->{b.append("A winter morning. In the worst of it their ").append(r).append(own.getCompound("Thing_routine").getString("State").equals("drawer")?" has gone into the drawer under the basin.\n\n":" stays where it is.\n\n");
                can=List.of("Lay their "+r+" out where they will see it, before they give up on the morning.","Stand behind them where the mirror faces them.");}
            case SecondPerson.W_DAY->{
                if(own.getInt("Care")==0){b.append("The post has come. They have put it on the hall table without opening it. In it there is ").append(ticketDesc(own)).append("\n\n");
                    can=List.of("Put the ticket on the mat by the door, where it cannot be missed.","Hide it.","Leave it where it is.");}
                else if(own.getString("Ticket").equals("mat")){b.append("They found the ticket on the mat.\n\n");can=List.of("Wait.");}
                else{b.append("A short grey day. They are home all of it.\n\n");can=List.of("Breathe on the street window. It is cold all winter, and holds a word.");}}
            case SecondPerson.W_EVENING->{b.append("Evening. They sit in the armchair with the television off.\n\n");
                can=own.getBoolean("TinOpened")&&!own.getBoolean("SleeveOut")&&!own.getBoolean("SleeveFound")?List.of("Stand behind them where the dark screen faces them.","Lay Iris's sleeve on the arm of the chair.")
                        :List.of("Stand behind them where the dark screen faces them.","Breathe on the street window.");}
            case SecondPerson.W_NIGHT->{b.append(own.getInt("Care")==0?"Night. They are asleep. Or they were.\n\n":"Night.\n\n");
                can=own.getInt("Care")==0?List.of("If they get up, bring them back: the lamp, a knock, a hand.","Or let them be."):List.of("Let them sleep.");}
            default->{b.append("The last of the winter.\n\n");
                can=List.of("The airing cupboard is warm. Go in and shut the door.");}
        }}
        else{b.append(whileAway(own,3)).append("\n\nIt is morning. The front door has been left on the latch.\n\n");
            can=own.getString("LastAct").isEmpty()?List.of("One last thing first, if you like: lay their "+routine(own)+" out where they will see it.","Or turn the glass round.","Or write a word on the window.","Then go out by the front door and down to the street."):List.of("Go out by the front door and down to the street.");}
        for(int i=0;i<can.size();i++)b.append(i+1).append(". ").append(can.get(i)).append("\n");
        var pages=list(own,"Pages");
        if(!pages.isEmpty()){b.append("\nThe pages you have turned:\n");for(int i=Math.max(0,pages.size()-6);i<pages.size();i++)b.append("— ").append(pages.get(i)).append("\n");}
        var house=list(own,"HousePages");
        if(!house.isEmpty()){b.append("\nThe pages you did not turn:\n");for(var h:house)b.append(h).append("\n");}
        return b.toString();
    }
    /** "While you were in the cupboard...": what happened in the season the reader let pass. */
    public static String whileAway(CompoundTag own,int season){
        if(season==1){var b=new StringBuilder("While you were in the cupboard, the summer went on without you. They have a job now; the rota is on the fridge. There is a rug, and the radio on for company.");
            var placed=list(own,"PlacedThings");if(!placed.isEmpty())b.append(" Things you put where they live are still where you put them.");
            if(own.getString("Call1").equals("off"))b.append(" The phone was off the hook for a week before they noticed.");
            return b.toString();}
        if(season==2){var b=new StringBuilder("While you were in the cupboard, the autumn went on without you, and in the first of the frost Iris died. They went to the funeral in ")
                .append(own.getBoolean("BorrowedCoat")?"a borrowed coat":"the coat from the hook").append(". You were not there.");
            var back=list(own,"Returned");if(!back.isEmpty())b.append(" Afterwards, looking for their good shoes, they found ").append(thing(own,back.get(0))).append(" under the bed, where you had put it.");
            return b.toString();}
        var b=new StringBuilder("While you were in the cupboard, the rest of the winter went on without you, then a letter came about the tenancy.");
        if(!list(own,"HousePages").isEmpty())b.append(" In the morning they told you about it, as if you had been there.");
        return b.toString();
    }

    // ------------------------------------------------------------------------------------------------ 0.4.369, the House's pages
    /** How gently the reader has kept the flat: what they set right, helped with, let be, or let be known kindly. */
    static int gentle(CompoundTag own){
        int g=list(own,"PutAway").size()+own.getInt("LaidCount");
        if(own.getBoolean("RoutineSet"))g++;
        switch(own.getString("Night")){case "shut","doorway"->g++;default->{}}
        switch(own.getString("Evening")){case "helped","alone"->g++;default->{}}
        if(own.getString("StayChoice").equals("warm"))g++;
        for(var w:list(own,"WordsSeen"))if(w.equals("HELLO")||w.equals("STAY")||w.equals("SORRY"))g++;
        if(own.getString("Sleepwalk").equals("brought"))g++;
        if(own.getBoolean("SleeveOut"))g++;
        switch(own.getString("Seen")){case "still","touch_kind"->g++;default->{}}
        return g;
    }
    /** How frightening: what they did that made the flat cold, loud or watched. */
    static int fright(CompoundTag own){
        int f=list(own,"Hidden").size()+list(own,"Cold").size()+list(own,"Touched").size();
        if(own.getString("Night").equals("tap"))f++;
        switch(own.getString("Evening")){case "interfered","seen"->f++;default->{}}
        if(own.getString("StayChoice").equals("cold"))f++;
        for(var w:list(own,"WordsSeen"))if(w.equals("GO"))f++;
        if(own.getString("Call1").equals("put_down")||own.getString("Call2").equals("put_down"))f++;
        switch(own.getString("Seen")){case "gone","touch_afraid"->f++;default->{}}
        return f;
    }
    /** 1 gentle, -1 frightening, 0 neither. The House follows the reader's own pattern, and is a little worse than them when they frighten. */
    static int pattern(CompoundTag own){int g=gentle(own),f=fright(own);return g>f?1:f>g?-1:0;}
    /** While the reader is in the cupboard at the winter's end, the House turns two pages in their name, kept apart from the reader's own. */
    static void housePages(CompoundTag own){
        if(!list(own,"HousePages").isEmpty())return;
        var pages=switch(pattern(own)){
            case 1->List.of("74. You sat on the end of their bed while they slept. In the morning they said they had slept right through, for once. Turn to 80.",
                    "80. You put the kettle on before they were up, every morning for a week. They thanked the kitchen.");
            case -1->List.of("74. You stood at the end of their bed all night. They lay very still so you would think they were asleep. Turn to 80.",
                    "80. You opened every door in the flat, one after another, while they lay awake and counted them.");
            default->List.of("74. You stood at the street window most nights, watching nothing. Turn to 80.",
                    "80. Once they saw you there from the street, and came up the stairs slowly.");
        };
        var l=new ListTag();for(var page:pages)l.add(StringTag.valueOf(page));own.put("HousePages",l);own.putInt("HousePattern",pattern(own));
    }
    /** On the last morning they speak of what the House did as if the reader had done it. */
    static String houseSays(CompoundTag own){return switch(own.getInt("HousePattern")){case 1->"You put the kettle on. Every morning. Didn't you.";case -1->"You were there all night. At the end of the bed.";default->"You were at the window again. I saw you from the street.";};}
    /** A House page as the account quotes it: the number and the turn taken off. */
    static String bare(String page){return page.replaceFirst("^\\d+\\. ","").replaceFirst(" Turn to \\d+\\.$","");}

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
        // 0.4.369, the winter.
        if(own.getInt("LaidCount")>=2)stay++;
        if(own.getBoolean("SleeveFinished"))stay++;
        if(own.getBoolean("Went"))stay++;
        switch(own.getString("Sleepwalk")){case "brought"->stay++;case "left"->go++;default->{}}
        if(own.getString("Ticket").equals("hidden"))go++;
        switch(own.getString("Seen")){case "still","touch_kind"->stay++;case "gone","touch_afraid"->go++;default->{}}
        if(own.getString("SeenWord").equals("STAY")||own.getString("SeenWord").equals("HELLO"))stay++;else if(own.getString("SeenWord").equals("GO"))go++;
        go+=own.getInt("HousePattern")<0?1:0;
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
        // 0.4.369, the winter.
        if(own.getInt("Season")>=SecondPerson.WINTER)winter(b,own,routine);
        // The last morning.
        b.append(own.getString("Decision").equals("stay")?"On the last morning they had decided to stay. When you looked back from the street there was a light in the window, and they were standing at it."
                :"On the last morning they had decided to go. You watched from the street as they came out of the side door with one bag and walked to the bus stop and away.");
        String last=switch(own.getString("LastAct")){case "routine"->"you laid their "+routine+" out where they would see it.";case "glass"->"you turned the glass round.";case "word"->"you wrote one more word on the window.";default->"";};
        if(!last.isEmpty())b.append(" Before you went, ").append(last);
        b.append("\n\nThe door was on the latch the whole time. You went out by it.");
        return b.toString();
    }
    /** The winter, as the account tells it: Iris, the tin, the mornings, the ticket, the night they walked, the glass; then the House's pages, apart. */
    static void winter(StringBuilder b,CompoundTag own,String routine){
        b.append("In the winter Iris died. They went to the funeral in ").append(own.getBoolean("BorrowedCoat")?"a borrowed coat":"the coat from the hook").append(", and you were not there. ")
         .append("She left them her sewing tin, with a note: For you, and for your lodger. They turned it over. \"What lodger?\"");
        var back=list(own,"Returned");if(!back.isEmpty())b.append(" Looking for their good shoes afterwards, they found ").append(thing(own,back.get(0))).append(" under the bed, where you had put it in the summer. \"There you are,\" they said.");
        b.append("\n\n");
        if(own.getBoolean("SleeveFinished"))b.append("You took the half-finished sleeve out of her tin and laid it on the arm of the armchair. They found it, and sat up in the evenings finishing it, badly, and finished it.\n\n");
        else if(own.getBoolean("SleeveOut"))b.append("You laid Iris's half-finished sleeve on the arm of the armchair. It was still there at the end of the winter.\n\n");
        else if(own.getBoolean("TinOpened"))b.append("You opened Iris's tin and left it open. Nobody finished the sleeve.\n\n");
        else b.append("Iris's tin stayed shut on the kitchen counter all winter.\n\n");
        b.append(switch(own.getInt("LaidCount")){
            case 0->"In the worst of it their "+routine+" stayed in the drawer under the basin, and you let it be.";
            case 1->"In the worst of it their "+routine+" went into the drawer under the basin. Once you laid it out, and they looked at it a long time, and left it.";
            case 2->"In the worst of it their "+routine+" went into the drawer under the basin. Twice you laid it out. The first time they left it. The second time they used it, and cried, and kept going.";
            default->"In the worst of it their "+routine+" went into the drawer under the basin. Three mornings you laid it out. The first they left it. The second they used it, and cried, and kept going. The third they said \"All right. All right.\"";
        }).append("\n\n");
        var walk=list(own,"WalkList");
        if(!walk.isEmpty()){
            var acts=new ArrayList<String>();
            for(var station:walk.subList(0,Math.min(walk.size(),own.getInt("WalkDone"))))acts.add(switch(station){
                case "kitchen"->"drew at the kitchen table without a light on";case "waiting"->"sat on the hall chair as if waiting to be called";
                case "bathroom"->own.getBoolean("WalkEmptyHands")?"did their "+routine+" twice at the basin in the dark, with nothing in their hands":"did their "+routine+" twice at the basin in the dark";
                case "coatroom"->"put on the coat";case "shelter"->"stood at the street window as if waiting out rain";
                case "lamp"->"turned the lamp on, the way you always did";case "tap"->"ran the kitchen tap, the way you did";case "glass"->"turned the hall glass, the way you did";case "knock"->"knocked on the wall, the way you did";
                default->"went to the front door and held the latch";});
            b.append("One night they got up asleep");
            if(!acts.isEmpty())b.append(" and ").append(acts.size()==1?acts.get(0):String.join(", ",acts.subList(0,acts.size()-1))+", and "+acts.get(acts.size()-1));
            b.append(".");
            if(acts.size()>0&&walk.subList(0,Math.min(walk.size(),own.getInt("WalkDone"))).stream().anyMatch(st->List.of("kitchen","waiting","bathroom","coatroom","shelter").contains(st)))b.append(" Some of it was what you had done in the rooms before this one, in the order you did it.");
            switch(own.getString("Sleepwalk")){
                case "brought"->b.append(" You brought them back with ").append(switch(own.getString("BroughtBy")){case "lamp"->"the lamp";case "knock"->"a knock";default->"your hand";}).append(", and they stood blinking where they were and went back to bed.");
                case "left"->b.append(haven(own)?" You let them be. They opened the front door for somebody who was not coming, and woke in the doorway, cold.":" You let them be. They woke in the open doorway, cold.");
                default->{}
            }
            if(own.getBoolean("DrewAtNight"))b.append(" In the morning there was a drawing on the kitchen table: a face. The one you drew at a kitchen table, before you ever came here.");
            b.append("\n\n");
        }
        switch(own.getString("Ticket")){
            case "mat"->b.append("When the post brought a train ticket you put it on the mat by the door, where it could not be missed. They went. ").append(haven(own)?"They came back saying they had been known, in the end.":"They came back saying they had forgotten they liked people.");
            case "left"->b.append("When the post brought a train ticket you left it on the hall table. They put it in a pocket and said \"Next week.\"");
            case "hidden"->b.append("When the post brought a train ticket you hid it, and they never knew it had come.");
            default->{}
        }
        if(!own.getString("Ticket").isEmpty()){
            switch(own.getString("Call3")){case "lifted"->b.append(" At the end of the winter the phone rang and they did not answer it. You lifted it, and listened.");case "put_down"->b.append(" At the end of the winter the phone rang and they did not answer it. You lifted it, and put it down.");
                case "off"->b.append(" At the end of the winter the phone rang and they did not answer it. You left it off the hook.");case "unanswered"->b.append(" At the end of the winter the phone rang, and they did not answer it.");default->{}}
            b.append("\n\n");
        }
        String seen=own.getString("Seen");
        if(!seen.isEmpty()){
            b.append("Once, in ").append(own.getString("SeenIn").equals("mirror")?"the bathroom mirror":"the dark television").append(", they saw you. \"...That's my face,\" they said.");
            switch(seen){
                case "still"->{b.append(" You stayed still, and they asked if you were them.");if(!own.getString("SeenAnswer").isEmpty())b.append(" You wrote ").append(own.getString("SeenAnswer")).append(" on the glass.");}
                case "gone"->b.append(" You stepped out of the glass, and there was only the room.");
                case "touch_kind"->b.append(" You touched them, and they said: \"It's you. It's been you.\"");
                case "touch_afraid"->b.append(" You touched them, and they backed away from the glass until the wall stopped them.");
                default->b.append(" You wrote ").append(own.getString("SeenWord")).append(" on the glass, and they read it.");
            }
            b.append("\n\n");
        }
        if(own.getBoolean("KnockedWinter"))b.append("You knocked on Iris's wall in the winter, and nobody knocked back.\n\n");
        var house=list(own,"HousePages");
        if(!house.isEmpty()){b.append("Then you went into the cupboard, and the House carried on in your name. These are not your pages. They told you about them in the morning as if they were:");
            for(var page:house)b.append(" ").append(bare(page));b.append("\n\n");}
    }
    static String callLine(String what,int call,CompoundTag own){
        String which=call==1?"The first time":"The second time";
        return switch(what){case "lifted"->which+", you lifted the receiver and listened, and said nothing back.";case "put_down"->which+", you lifted it and put it down again.";
            case "off"->which+", you left it off the hook.";case "left"->which+", you let it ring, and they caught it themselves.";default->"";};
    }
}
