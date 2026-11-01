package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.202, the Whistlers in the Pines: three hands. The service's protocol binder on the desk (typed, the source); Tower
 * Seven's tape off the teleprinter, which on the third night begins to print what the reader is doing; and the fire log
 * in the reader's own hand (the account), with Seven's last tape pasted in. House writing: never the reader's gender,
 * never a colour of theirs, never an age.
 */
public final class LookoutTexts {
    private LookoutTexts(){}
    /** The binder, as the service printed it. */
    public static final String PROTOCOL="""
        LOOKOUT PROTOCOL. TOWER FOUR.

        Check in with Tower Seven on the set when you arrive and at first light. Seven is three kilometres north-east of you. If the set is quiet, the teleprinter is not.

        Smoke and flares are logged by bearing. Take the alidade on the finder and turn with it until the sight lies on the smoke. You do not need the window to hold a bearing once it is given. Read it off the ring. Log it.

        If you hear whistling outside the glass, do not look toward it. Look at the floor. Walk to that window with your eyes on the boards and pull its cord. When the canvas is down you may look up again.

        The pendulum swings toward the wall it is on, and the lamp over that window lights. You do not need to see anything to know which wall.

        It does not come up the stairs while you keep to these rules. Each time you look it is nearer.

        If the power fails, the crank hangs by the hatch. The generator is in the shed at the foot of the tower. Prime it before you crank it.

        On the stairs, if a landing lantern burns blue, get down behind the rail or a bank and stay down until it burns yellow again. Do not stand to see why.

        If a blind tears, a repair tarp will hold the frame. There are two. There are not three.

        Do not answer anything on the set that you did not call. Do not open the roof hatch from inside.""";
    private static final int PAGE=145;
    static List<String> pages(String text,int limit){
        var out=new ArrayList<String>();
        for(String paragraph:text.strip().split("\\n\\s*\\n")){
            String rest=paragraph.strip().replaceAll("[ \\t\\r\\n]+"," ");
            while(rest.length()>limit){int at=rest.lastIndexOf(' ',limit);if(at<1)at=limit;out.add(rest.substring(0,at));rest=rest.substring(at).stripLeading();}
            if(!rest.isEmpty())out.add(rest);
        }
        return List.copyOf(out);
    }
    public static ItemStack source(){
        var pages=new ArrayList<Component>();
        for(String page:pages(PROTOCOL,PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,page));
        return HouseWriting.book("Lookout protocol","The service",pages);
    }

    // ------------------------------------------------------------------------------------------------ Seven's tape
    public static final String WELCOME="FOUR THIS IS SEVEN. WELCOME UP. BINDER ON THE DESK. CHECK IN ON THE SET WHEN YOU ARE SETTLED.",
        CHECKED_IN="COPY FOUR. CLEAR NIGHT HERE. WILL SEND YOU FLARES FROM THE CREWS. LOG THEM.",
        THREE="THREE FOR THREE. THE LOG IS YOURS TONIGHT FOUR. SEVEN OUT.",
        POWER="THERE YOU ARE FOUR. LOST YOU AN HOUR. GET BACK UP BEFORE IT CLOSES IN.",
        WARM="WARM UP. NOTHING ELSE TONIGHT.",
        STORM="FOUR GALE WARNING. KEEP YOUR TARPS WHERE YOU CAN REACH THEM.",
        DAWN="LIGHT IN AN HOUR FOUR. YOU DID WELL. SEVEN",
        LAST="FOUR. I CAN SEE YOUR LIGHT FROM HERE. I ALWAYS COULD.";
    /** A flare: where Seven says it is, roughly; the bearing is for the finder to give. */
    public static String flare(int i){return switch(i){case 0->"FLARE FROM THE CREW NORTH EAST OF YOU. GET ME A BEARING.";case 1->"SECOND FLARE. SOUTH. BEARING.";default->"LAST ONE. WEST OF YOU. BEARING.";};}
    public static String logged(int i,int bearing){return String.format(Locale.ROOT,"FLARE %d AT %03d. LOGGED.",i+1,bearing);}
    /** The third night: the tape prints what the reader does, as they do it. */
    public static String watching(String what){return "FOUR "+what.toUpperCase(Locale.ROOT)+".";}
    /** Seven, wrong: the words come out right but not from Seven. */
    public static final String[] WRONG={"FOUR DO YOU COPY. FOUR LOOK OUT THE NORTH WINDOW. I AM WAVING.","FOUR THE WHISTLING IS THE WIND IN THE GUYS. IT IS ONLY THE WIND.","FOUR OPEN THE HATCH. I HAVE COFFEE."};
    /** The tape kept on the reader's own record, most recent last, a few dozen lines at most. */
    public static void keep(CompoundTag own,String line){var tape=own.getList("Tape202",8);tape.add(StringTag.valueOf(line));while(tape.size()>30)tape.remove(0);own.put("Tape202",tape);}
    public static String lastTape(CompoundTag own){var tape=own.getList("Tape202",8);return tape.isEmpty()?LAST:tape.getString(tape.size()-1);}

    // ------------------------------------------------------------------------------------------------ the fire log
    private static String times(int n){return switch(n){case 0->"never";case 1->"once";case 2->"twice";case 3->"three times";default->"more than I like to write";};}
    /** The log, night by night, from what the reader did; Seven's last tape pasted in at the end. */
    public static String account(CompoundTag own){
        var b=new StringBuilder();
        b.append("Tower Four. Fire log.\n\n");
        b.append(own.getBoolean("Read_Source")?"Read the binder through on the first night. I kept to it.":"I never read the binder through. I learned the rules from the glass.").append("\n\n");
        var flares=own.getList("Flares202",3);
        if(!flares.isEmpty()){b.append("First night. Checked in with Seven. Flares logged at ");for(int i=0;i<flares.size();i++){if(i>0)b.append(i==flares.size()-1?" and ":", ");b.append(String.format(Locale.ROOT,"%03d",flares.getInt(i)));}b.append(". It whistled twice, north and then west, and I pulled the cords with my eyes on the boards.\n\n");}
        if(own.getBoolean("Powered202"))b.append("Second night. Power out, stove out, frost coming in at the edges. Took the crank down to the shed and got the diesel going. When the lanterns on the landings burned cold I got down behind the rail and stayed down.\n\n");
        if(own.getBoolean("Tarps202"))b.append("Third night. The gale took three blinds. Two tarps for three frames, so I moved them round the room to wherever it whistled from, and never looked up while I did it. At the end, three knocks on the roof hatch. I did not open it.").append(own.getInt("Watched202")>0?" The tape printed what I was doing as I did it.":"").append("\n\n");
        int looked=own.getInt("Looked202"),broke=own.getInt("Broke202");
        if(looked>0)b.append("I looked toward it ").append(times(looked)).append(". Each time it was nearer.").append(broke>0?" The glass went "+times(broke)+", and it was pressed against the frame when it went.":"").append("\n\n");
        if(own.getBoolean("North202"))b.append("Fourth night. No storm and no ground. Eight towers round mine at cab height, a figure in each one standing where I stood, turning when I turned. The north one did not move. It had binoculars on me. I put the finder on it, 000, and the hatch let me down.\n\n");
        b.append("Seven's last tape, pasted in:\n\n").append(lastTape(own));
        return b.toString();
    }
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.WHISTLERS),name,HouseWriting.WritingStyle.WILL,pages(account(own),PAGE));
    }
    /** The Witness line. */
    public static final String WITNESS="I kept my eyes on the boards whenever it whistled and pulled the cords without looking. When the storm stopped there were eight towers round mine, and the one to the north was watching me through binoculars.";
    public static final String HINT="Read the binder on the desk. When something whistles outside the glass, look at the floor, walk to that wall and pull its cord.";
    static ListTag ints(int... v){var t=new ListTag();for(int i:v)t.add(net.minecraft.nbt.IntTag.valueOf(i));return t;}
}
