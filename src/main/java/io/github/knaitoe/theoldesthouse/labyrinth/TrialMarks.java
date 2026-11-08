package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.384, the seals (docs/HALLS_TRIALS_AND_TIME_DESIGN.md, R1; docs/SEALS_0_4_384.md). The owner: "I've found it too easy to get
 * to the staircase." A reader earns a mark for each kind of trial they come through by its far door, once per kind: the
 * physical trials (LabyrinthPacing.physicalTrial), a sibling counting as its original. Walking back out the way in earns
 * nothing. The great staircase is offered only to a reader with {@link #SEALS} marks and {@link #STORIES} stories heard to the
 * end of their own (FinaleProgress.eligible), deep in the House (LabyrinthPacing.STAIRCASE_DEPTH). Each reader's own.
 */
public final class TrialMarks {
    private TrialMarks(){}
    public static final String STATE="trial_marks_0384";
    /** Marks and resolved stories the staircase waits on. */
    public static final int SEALS=3,STORIES=4;
    /** Each kind of trial, by the id its mark is kept under, in the room's own words. */
    private static final Map<String,String> NAMES=Map.ofEntries(
        Map.entry("flooded_passage","the flooded passage"),Map.entry("fractured_walkway","the broken walkway"),Map.entry("light_sink","the room that ate light"),
        Map.entry("cold_store","the cold store"),Map.entry("listing_hall","the hall that listed"),Map.entry("quiet_hall","the quiet hall"),
        Map.entry("unwatched_maze","the maze that moved while nobody watched"),Map.entry("mirror_hall","the mirror hall"),Map.entry("heavy_and_light","the rooms of weight"),
        Map.entry("nested_rooms","the rooms inside rooms"),Map.entry("counting_stair","the stair that counted"),Map.entry("return_air","the passage that drew breath"),
        Map.entry("settling_cellar","the settling cellar"),Map.entry("yard_flood","the flooded yard"),Map.entry("yard_windbreak","the windbreak"),
        Map.entry("yard_ditch_flood","the ditch in flood"),Map.entry("yard_storm_field","the storm field"),
        Map.entry("still_light","the room that lit for stillness"),Map.entry("soft_floor","the floor that would not hold a pause"),
        Map.entry("rising_stair","the stair the water came up"),
        Map.entry("many_lengths","the hall that would not be hurried"),
        Map.entry("following_door","the door that would not be looked at"));

    /** The kind of trial a place is, if it is one: a sibling counts as the room it varies. */
    public static @Nullable LabyrinthPlace kind(@Nullable LabyrinthPlace place){
        if(place==null||!LabyrinthPacing.physicalTrial(place))return null;
        return HazardVariety.sibling(place)?HazardVariety.original(place):place;
    }
    public static String name(LabyrinthPlace kind){return NAMES.getOrDefault(kind.id(),"a room the House has not named");}
    static CompoundTag own(LabyrinthData d,UUID reader){return d.stateEntry(STATE,reader.toString());}
    /** The kinds this reader has marks for, in the order they earned them. */
    public static List<String> marks(LabyrinthData d,UUID reader){
        var out=new ArrayList<String>();for(var t:own(d,reader).getList("Marks",8))out.add(t.getAsString());return out;
    }
    public static int count(LabyrinthData d,UUID reader){return marks(d,reader).size();}
    public static boolean sealed(LabyrinthData d,UUID reader){return count(d,reader)>=SEALS&&WitnessAccount.count(d,reader)>=STORIES;}
    /** Records a mark for this kind of trial; returns whether it was new. */
    public static boolean mark(LabyrinthData d,UUID reader,LabyrinthPlace place){
        var kind=kind(place);if(kind==null)return false;
        var own=own(d,reader);var list=own.getList("Marks",8);
        for(var t:list)if(t.getAsString().equals(kind.id()))return false;
        list.add(StringTag.valueOf(kind.id()));own.put("Marks",list);d.setStateEntry(STATE,reader.toString(),own);return true;
    }
    /** A reader through a trial's far door: the mark, and what it means, in grey on the far side. */
    public static void crossed(ServerPlayer p,LabyrinthPlace place){
        var d=LabyrinthData.get(p.server);if(!mark(d,p.getUUID(),place))return;
        int n=count(d,p.getUUID());boolean stories=WitnessAccount.count(d,p.getUUID())>=STORIES;
        String line=switch(n){
            case 1->"Chalked on the frame as you pass through, a mark you did not make. One.";
            case 2->"Another mark on the frame. Two.";
            case 3->stories?"A third mark on the frame. "+OPEN
                :"A third mark on the frame. Somewhere below you, a stair waits until you have heard enough of the House's rooms to their end.";
            default->"Another mark on the frame. The House keeps count.";
        };
        if(n>=SEALS&&stories)tell(d,p.getUUID());
        p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
    }
    /** The first arrival after the stories make up the seal, when the third mark's own line could not say so. */
    public static void arrived(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var id=p.getUUID();
        if(p.isSpectator()||!sealed(d,id)||own(d,id).getBoolean("Told"))return;tell(d,id);
        p.displayClientMessage(Component.literal(OPEN).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
    }
    public static boolean told(LabyrinthData d,UUID reader){return own(d,reader).getBoolean("Told");}
    private static void tell(LabyrinthData d,UUID reader){var own=own(d,reader);own.putBoolean("Told",true);d.setStateEntry(STATE,reader.toString(),own);}
    static final String OPEN="Somewhere below you, a stair is no longer kept from you.";
    /** The account's pages on the marks (at most 180 characters each), or none before the first. */
    public static List<String> pages(LabyrinthData d,UUID reader){var text=page(d,reader);return text==null?List.of():HouseReadProse.pages(text);}
    static @Nullable String page(LabyrinthData d,UUID reader){
        var marks=marks(d,reader);if(marks.isEmpty())return null;
        var names=new ArrayList<String>();for(var id:marks){var place=LabyrinthPlace.byId(id);names.add(place==null?"a room the House has not named":name(place));}
        var b=new StringBuilder("THE MARKS\n\nI came through ");
        for(int i=0;i<names.size();i++){if(i>0)b.append(i==names.size()-1?" and ":", ");b.append(names.get(i));}
        b.append(" by the far door").append(names.size()==1?".":", each of them.");
        if(names.size()<SEALS)b.append("\n\nThe House keeps its deepest stair from anyone with fewer than three marks.");
        else if(WitnessAccount.count(d,reader)<STORIES)b.append("\n\nThe marks are enough. The stair still waits on the rooms I have not heard to their end.");
        return b.toString();
    }
}
