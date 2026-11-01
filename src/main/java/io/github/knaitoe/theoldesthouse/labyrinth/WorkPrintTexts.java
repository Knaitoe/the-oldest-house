package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.203, Work print: the shop's printed rental card (the source, with the borrower's line in the reader's own name); the
 * film's papers in the edit bay and the booth (the call sheet, the script with its unshot last scene, the continuity
 * photographs, the reel labels, the sound log, the costume's tag); and the account, which is the shop's returns ledger with a
 * note from the man who played the monster folded into it. The film is <i>Hallstand</i>; it is original, as is every line here.
 * House writing: never the reader's gender, never a colour of theirs, never an age.
 */
public final class WorkPrintTexts {
    private WorkPrintTexts(){}
    private static final int PAGE=150;

    /** The rental card on the counter: the shop's print, and the borrower's name. */
    public static ItemStack source(String name){
        String card="""
            NIGHT OWL VIDEO. Rental card.

            Title: none. Format: VHS. No box, no label. Category: left blank.

            Out: after hours. Due back: seven days.

            Borrower: %s

            Notes: Return by the slot. Do not rewind.""".formatted(name);
        String back="""
            On the back, in the shop's biro: Came in through the slot on its own again. Put it in the bin with the returns. Do not put it out on the shelves. Do not watch it in the shop.

            Under that, in another hand: Too late.""";
        var pages=new ArrayList<Component>();
        for(String p:LookoutTexts.pages(card,PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));
        for(String p:LookoutTexts.pages(back,PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.KAREN,p));
        return HouseWriting.book("Rental card","Night Owl Video",pages);
    }
    public static ItemStack source(){return source("(your own name, in your own hand)");}

    /** The call sheet pinned in the edit bay. */
    public static final String CALL_SHEET="""
        HALLSTAND. Call sheet. Day 19 of 18.

        Location: the house on the corner. INT. HALL, upstairs. Crew call 6am. Suit fitting for W. Darrow at 5.

        Scenes: 86, 87, 88.

        Note to camera: the hall measured thirty-one feet on day one. Measure it again before you light it.

        Note to all: if the hall is longer than yesterday, do not mention it on set.

        88 was not shot.""";
    /** The script's last pages: the scenes that were shot, and the one that was not. */
    public static final String SCRIPT="""
        HALLSTAND. Shooting script. Blue pages.

        86. INT. HALL. NIGHT. A WOMAN writes at a table at the near end of the hall. She does not look up. At the far end, under the bulb, THE FIGURE.

        87. INT. HALL. LATER. The table is gone. The figure is nearer. We do not see it move.

        88. INT. HALL. NIGHT. THE FIGURE stands on its mark under the bulb, its back to us. It is very still for a long time. Then it TURNS ROUND.

        It turns round to face the camera. We never see what the camera sees. END.

        In the margin, in pencil: Darrow won't do the turn. Says the suit knows where it is going. Pick up 88 when we have someone.""";
    /** The continuity photographs on the edit bay's wall. */
    public static final String PHOTOS="""
        Continuity photographs, pinned in rows, each marked in grease pencil with a scene and a day.

        Every one is the same stretch of upstairs hall from the same place on the floor.

        In the first rows the hall is short, and the bulb is near. Day by day it goes back further, and by the last rows it goes back further than the house could be deep.

        Under the last row someone has written, small: HE IS CLOSER IN EVERY ONE. Nobody is in any of them.""";
    /** The cans on the rack. */
    public static final String REELS="""
        Film cans on the rack, labelled in marker.

        HALLSTAND. R1. Dailies.

        HALLSTAND. R2. Work print. Do not project. Telecined to tape for the director, one copy.

        HALLSTAND. R3. Empty. Somebody has written on the lid: for 88.""";
    /** The sound log in the booth. */
    public static final String SOUND_LOG="""
        Sound log. Room tone, upstairs hall.

        Day 6. Two minutes of room tone. On playback, breathing. Nobody was in the hall.

        Day 9. Room tone again, with the door shut. More breathing, nearer.

        Day 14. The playback is longer than the recording was.

        Day 18. Stopped taking room tone.""";
    /** The tag on the costume. */
    public static String tag(boolean theirs,String name){
        return theirs?"The tag reads NOT FOR HIRE, crossed out. Under it, in your own hand: "+name+"."
            :"A rental tag on the costume's wrist: NOT FOR HIRE. Worn by W. Darrow, HALLSTAND, the autumn of the shoot. The fingers of the gloves are a joint too long.";
    }

    /** The phone's count: only a number, and no voice. */
    public static String count(int left){
        String[] words={"…nothing. Only the line, open.","…one.","…two.","…three.","…four.","…five.","…six.","…seven."};
        return words[Math.max(0,Math.min(7,left))];
    }

    // ------------------------------------------------------------------------------------------------ the account
    private static String times(int n){return switch(n){case 1->"once";case 2->"twice";case 3->"three times";default->n+" times";};}
    /** The ledger, from what the reader did; Darrow's note folded into it. */
    public static String accountText(String name,CompoundTag own){
        var b=new StringBuilder();
        b.append("NIGHT OWL VIDEO. Returns ledger.\n\n");
        b.append("No title. No box. Out: ").append(name).append(". Watched ").append(times(Math.max(1,own.getInt("Viewings")))).append(" in the back room.");
        if(own.getInt("Viewings")>1)b.append(" Each time, one more shot of it was ").append(name).append(", from behind, in a hall they had walked.");
        b.append("\n\n");
        var read=new ArrayList<String>();
        if(own.getBoolean("Read_CallSheet"))read.add("the call sheet");if(own.getBoolean("Read_Script"))read.add("the script");
        if(own.getBoolean("Read_Photos"))read.add("the continuity photographs");if(own.getBoolean("Read_Reels"))read.add("the cans");if(own.getBoolean("Read_Log"))read.add("the sound log");
        if(!read.isEmpty())b.append("Went through ").append(String.join(", ",read)).append(" in the edit bay.\n\n");
        if(own.getBoolean("Lapsed"))b.append("Seven days ran out. Came to in the hallway on the set, with lines across everything, and walked out of the shot. It followed for a while after that: a screen, a ring, the grain.\n\n");
        switch(own.getString("Outcome")){
            case "finished_the_film"->{
                b.append("Returned: never. Finished the film instead. Wore the suit on the set and stood on the mark with my back to the camera, and turned round. Spliced 88 into the work print and ran it through to the end.");
                if(own.contains("Partner"))b.append(" ").append(own.getString("Partner")).append(" held the camera.");
                b.append(" The count stopped.\n\n");
                b.append("Folded into the ledger, in a careful old hand:\n\nWhoever did the turn: thank you. I could not. I wore that suit for eighteen days and it was always about to turn round, and I knew if I let it I would see the camera from the other side. Now it has, and it has someone else's name on it. Keep it. It fits whoever finishes. W. Darrow.");
            }
            case "passed_it_on"->{
                b.append("Returned: by copy. Dubbed it on the two decks and passed it on.");
                if(own.contains("PassedTo"))b.append(" ").append(own.getString("PassedTo")).append(" watched it, and the count was theirs.");
                else if(own.getBoolean("Villager"))b.append(" Gave it to someone who could not say no. They stood and watched nothing for a while.");
                b.append(" The count stopped.\n\n");
                b.append("Folded into the ledger, in a careful old hand:\n\nSomebody always passes it on. I did, the year after the shoot, to a man who asked what was on it. I still think about what I could have said. If you are reading this you said nothing, too. Keep the suit. You earned it the way I did. W. Darrow.");
            }
            default->b.append("Returned: not yet.");
        }
        return b.toString();
    }
    public static ItemStack account(String name,CompoundTag own){
        var pages=new ArrayList<Component>();
        String text=accountText(name,own);int note=text.indexOf("Folded into the ledger");
        String ledger=note<0?text:text.substring(0,note),letter=note<0?"":text.substring(note);
        for(String p:LookoutTexts.pages(ledger,PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));
        for(String p:LookoutTexts.pages(letter,PAGE))pages.add(HouseWriting.page(HouseWriting.WritingStyle.ZAMPANO,p));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.WORK_PRINT),name,pages);
    }
    /** The Witness line. */
    public static String witness(){return WitnessAccount.Story.WORK_PRINT.text;}
    public static final String HINT="Read the rental card on the counter. The tape is in the returns bin by the door; the decks are in the back room.";
}
