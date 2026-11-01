package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.207, the God of the Pitt: two hands. Brad's card on its piton at the staging ledge, in pencil capitals (the source); and
 * the reader's own field journal, four logs in the way cavers kept them online in 2001, each with a survey drawn in letters,
 * written from what the reader did down there (the account). All of it is original. House writing: never the reader's gender,
 * never a colour of theirs, never an age.
 */
public final class PittTexts {
    private PittTexts(){}
    private static final int PAGE=150;
    static List<Component> plain(String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}

    /** Brad's card, on a piton driven into the ledge's wall. */
    public static ItemStack source(){return HouseWriting.book("A card on a piton","B.",plain("""
        SAT.

        YOU SAID YOU'D COME BACK DOWN. GEAR IS IN THE CRATE: HAMMER, LAMP, THE BAROMETER.

        THE TOMB HAS FOUR SEAMS ACROSS IT. CUT ONLY WHILE THE DRAFT BLOWS.

        WHEN THE NEEDLE GOES RED YOU LIE FLAT, LAMP AWAY, AND YOU DO NOT MOVE TILL IT COMES BACK. IT COMES BACK.

        I'LL BE ON THE BELAY. TWO FLASHES IS GOOD. AMBER IS NOT.

        R.'S LINE IS STILL TIED OFF AT THE LADDER UP TOP. NOBODY HAS UNTIED IT. LEAVE IT.

        B.

        (On the back, smaller:) LAST TIME IT BREATHED IN. THIS TIME I THINK IT IS HOLDING ITS BREATH.""".stripIndent()));}

    // ------------------------------------------------------------------------------------------------ the field journal
    private static String times(int n){return switch(n){case 1->"once";case 2->"twice";case 3->"three times";default->n+" times";};}
    public static String accountText(String name,CompoundTag own){
        var b=new StringBuilder();
        b.append("FIELD JOURNAL. The pages are swollen with damp and the pencil has gone through in places.\n\n");
        b.append("LOG #1 - 5/19/01\n\nBack down with B. Four seams across the Tomb, cut by the barometer, only while the needle sat in the green.");
        if(own.getInt("Strokes207")>0)b.append(" ").append(own.getInt("Strokes207")).append(" strokes of the hammer, all told.");
        if(own.getInt("Pinned207")>0)b.append(" I moved in the still air ").append(times(own.getInt("Pinned207"))).append(" and the ceiling came down on my back and stayed there until the draft came back.");
        if(own.getInt("Reached207")>0)b.append(" There is something in the crack beside the Tomb. It reached out of it ").append(times(own.getInt("Reached207"))).append(" and pressed down.");
        else if(own.getBoolean("Eyes207"))b.append(" There is something in the crack beside the Tomb, and it watches.");
        if(own.getBoolean("Hole207"))b.append(" At the end, a hole the size of a fist. Air out of it, steady, smelling of wet copper.");
        b.append("\n\n[1]=TOMB=o\n        hole\n\n");
        b.append("LOG #2 - 5/26/01\n\nBroke the fist hole through. A wind tunnel a crouch high, then a round room.");
        if(Integer.bitCount(own.getInt("Anchors207"))>0)b.append(" Clipped the orange line in at ").append(Integer.bitCount(own.getInt("Anchors207"))).append(" bolts.");
        b.append(" A round stone in the west wall, taller than me, on copper grooves worn bright.");
        if(own.getBoolean("Grooves207"))b.append(" I knelt to look at the track and the room started coming down. Lamp out. I crawled back along the line in the dark");
        if(own.getBoolean("Escaped207"))b.append(", with the ceiling coming down behind me the whole way, and B. hauled the last of it in hand over hand.");
        else if(own.getBoolean("Grooves207"))b.append(".");
        if(!own.getString("BelayFor207").isEmpty())b.append(" ").append(own.getString("BelayFor207")).append(" went in; I held the belay.");
        if(!own.getString("HauledBy207").isEmpty())b.append(" It was ").append(own.getString("HauledBy207")).append(" on the belay that day, not B. They hauled me out from under it.");
        if(!own.getString("Hauled207").isEmpty())b.append(" I hauled ").append(own.getString("Hauled207")).append(" out from under the slab.");
        b.append("\n\n[1]=A=[2]\n       O stone\n\n");
        b.append("LOG #3 - 6/2/01\n\nNobody at the belay. B.'s line hung off the post, untied.");
        b.append(" The stone had been pushed out along its track, from the inside, and there was a hole behind it.");
        if(own.getBoolean("Helmet207"))b.append(" B.'s helmet on the floor, cracked across, lamp blinking amber, pointed down the hole.");
        b.append(" The chimney was not the chimney I had seen. Three drops on his ropes and three squeezes.");
        if(own.getBoolean("Cut207"))b.append(" His line went down ahead of me and into a crack two fingers wide, frayed.");
        if(own.getBoolean("Eyes207"))b.append(" Eyes in the cracks beside me, keeping pace.");
        if(own.getBoolean("Landing207"))b.append(" At the bottom, a ledge over nothing.");
        b.append("\n\n[2]-O\n    |\n    B\n    |\n   [3]\n\n");
        b.append("LOG #4 - 6/9/01\n\nThe draft steady and warm. On the ledge I heard my own footsteps before I took them, and saw my prints in the dust two steps ahead of me, going out over the nothing. I walked in them.");
        if(own.getInt("Falls207")>0)b.append(" I fell ").append(times(own.getInt("Falls207"))).append(", and woke on the ledge each time, and never hit anything.");
        b.append(" There were bridges I could not see. At the far wall, a ladder, rusted, and a hatch, and grass, and the sky going grey.");
        if(own.getInt("Blackouts207")>0)b.append(" (I lost ").append(times(own.getInt("Blackouts207"))).append(" down there and woke on the ledge where we started. I don't count those.)");
        b.append("\n\nI don't know whose prints they were. They fit me.\n\nB. isn't coming up.");
        return b.toString();
    }
    public static ItemStack account(String name,CompoundTag own){return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.PITT),name,plain(accountText(name,own)));}
    public static final String HINT="Cut only while the draft blows. When the needle goes red, lie flat with the lamp put away until the air moves again.";
}
