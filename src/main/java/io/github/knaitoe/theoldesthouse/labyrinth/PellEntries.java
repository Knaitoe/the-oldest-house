package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.journal.OwnHand;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.537, the journal's false entries (docs/PELL_HOUSE_ENTRIES_0_4_537.md), from the proposal's "Being framed": "after each framed
 * incident, and once after each night in the garage", "a short entry appears in the older sibling's real House journal, in their own
 * handwriting and their own ink, as if they wrote it."
 *
 * <ul>
 * <li><b>When.</b> A reader June finds in the kitchen after the knife, or the one she says slammed the laundry door on her hand, has
 * that entry due when they go on Gail's file for it. A reader who slept in the garage a week in has that night's entry due when June
 * calls breakfast (the first visit's night gives none: the proposal's first false entries are a week in). Each entry, once, ever, for
 * each reader.</li>
 * <li><b>Where.</b> In the reader's journal, in their own lettering (journal.OwnHand): never on their words, only on a page begun in the
 * House, never on a page they have open. An entry waits while it cannot go in (no journal yet, or the page open), and goes in the first
 * moment it can while they are at the house.</li>
 * <li><b>Kept.</b> They stay in the journal for good, whichever way it ends. Torn out, a page keeps its entry; where it turns up again
 * comes later.</li>
 * <li><b>0.4.543, after a failure</b> (docs/PELL_HOUSE_FAILURE_0_4_543.md). An entry still due when the reader has left the house goes
 * in wherever they are in the House (PellAfterlife's tick). After Tilly answered, the entries go on: each story the reader resolves
 * later leaves one more due, a twisted line about what they did there (PellTexts.forged), and one goes in a House day at most. After a
 * success nothing new comes, and the old ones stay.</li>
 * </ul>
 * Nothing tells the reader it happened.
 */
public final class PellEntries {
    private PellEntries(){}
    /** The entries: after the knife, after June's hand, and after a night in the garage on each visit. */
    public static final String KNIFE="knife",HAND="hand",NIGHT="night";
    /** How often an entry that is due tries to go in, in server ticks (a multiple of the five readers are looked at on). */
    public static final int TRY=20;
    /** The first visit whose night in the garage gives an entry: a week in, when the proposal's first false entries appear. */
    static final int FIRST_NIGHT=2;

    /** An entry due for this reader, once: not again once it is due or written. The caller saves the record. */
    static void due(CompoundTag own,String id){
        if(id.isEmpty())return;
        var due=own.getList("FalseDue",Tag.TAG_STRING);
        if(has(due,id)||has(own.getList("FalseWritten",Tag.TAG_STRING),id))return;
        due.add(StringTag.valueOf(id));own.put("FalseDue",due);
    }
    /** That night's entry, for a reader who slept in the garage on this visit; none for the first visit's night. */
    static String night(int visit){return visit<FIRST_NIGHT?"":NIGHT+visit;}
    /** A reader at the house: the first entry due goes in if it can now. */
    static void reader(ServerPlayer p,CompoundTag own){
        if(p.server.getTickCount()%TRY!=0)return;
        var due=own.getList("FalseDue",Tag.TAG_STRING);if(due.isEmpty())return;
        String id=due.getString(0),text=PellTexts.falseEntry(id);
        if(!text.isEmpty()&&OwnHand.write(p,text)==0)return;
        due.remove(0);own.put("FalseDue",due);
        if(!text.isEmpty()){var done=own.getList("FalseWritten",Tag.TAG_STRING);done.add(StringTag.valueOf(id));own.put("FalseWritten",done);}
        PellHouse.save(p,own);
    }
    /** Whether the house ended in a failure for this reader: Tilly answered. (The older sibling taken away by Gail waits on the owner's
     *  open question 2, docs/PELL_HOUSE_PROPOSAL.md.) */
    public static boolean failed(CompoundTag own){return own.getBoolean("TillyStayed");}
    /**
     * A story this reader resolved (WitnessAccount.resolve, wherever it happens). After the house failed them, the House has something
     * new to put in their hand: the latest such story, waiting for the next House day it can go in. Never the house's own story.
     */
    static void resolved(net.minecraft.server.MinecraftServer server,java.util.UUID reader,WitnessAccount.Story story){
        resolved(LabyrinthData.get(server),reader,story);
    }
    static void resolved(LabyrinthData d,java.util.UUID reader,WitnessAccount.Story story){
        if(story==null||story.id.equals(PellHouse.PLACE.id())||PellTexts.forged(story.id).isEmpty())return;
        var own=LiteraryVignettes.personal(d,reader,PellHouse.PLACE);if(!failed(own))return;
        own.putString("ForgeNext",story.id);LiteraryVignettes.save(d,reader,PellHouse.PLACE,own);
    }
    /** The House day, as the journal counts them: the Overworld's, from one. */
    static long day(ServerPlayer p){return p.server.overworld().getDayTime()/24000L+1;}
    /**
     * A reader anywhere in the House, every TRY ticks: an entry still due goes in if it can (at the house itself PellHouse.reader looks
     * after those, so that one goes in at a time); otherwise, after a failure, the latest later story's line, if none has gone in today.
     */
    static void anywhere(ServerPlayer p){
        if(p.isSpectator())return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PellHouse.PLACE);
        if(!own.getList("FalseDue",Tag.TAG_STRING).isEmpty()){if(LiteraryVignettes.current(p)!=PellHouse.PLACE)reader(p,own);return;}
        if(!failed(own)||!own.contains("ForgeNext")||own.getLong("ForgedDay")==day(p))return;
        String story=own.getString("ForgeNext"),text=PellTexts.forged(story);
        if(!text.isEmpty()&&OwnHand.write(p,text)==0)return;
        own.remove("ForgeNext");own.putLong("ForgedDay",day(p));
        if(!text.isEmpty()){var done=own.getList("Forged",Tag.TAG_STRING);done.add(StringTag.valueOf(story));own.put("Forged",done);}
        PellHouse.save(p,own);
    }
    /** For tests: the later stories whose lines have gone into this reader's journal, in order. */
    public static List<String> forged(CompoundTag own){return strings(own.getList("Forged",Tag.TAG_STRING));}
    /** For tests: the entries due and written for this reader. */
    public static List<String> due(CompoundTag own){return strings(own.getList("FalseDue",Tag.TAG_STRING));}
    public static List<String> written(CompoundTag own){return strings(own.getList("FalseWritten",Tag.TAG_STRING));}

    private static boolean has(ListTag list,String id){for(int i=0;i<list.size();i++)if(list.getString(i).equals(id))return true;return false;}
    private static List<String> strings(ListTag list){var out=new java.util.ArrayList<String>();for(int i=0;i<list.size();i++)out.add(list.getString(i));return out;}
}
