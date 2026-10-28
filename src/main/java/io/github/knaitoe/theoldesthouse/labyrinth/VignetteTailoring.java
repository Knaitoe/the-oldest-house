package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

/** Authored changes to six existing moments. These methods assign no reader category. */
public final class VignetteTailoring {
    private VignetteTailoring(){}
    public static ItemStack childCard(ServerPlayer reader,ItemStack fallback){
        var hair=HouseRead.hair(reader,true);var book=fallback.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if(hair.isEmpty()||book==null)return fallback;
        String action=switch(hair.orElseThrow()){
            case "WRAPPED"->"I tucked the wrap over your hair after you climbed into bed. You held the edge while I tied it.";
            case "BRAIDED_AND_OILED"->"I worked a little oil along your part and finished the last braid. You leaned against my knee until I was done.";
            case "BRUSHED_DRY"->"I brushed your hair before I put the brush beside the lamp. You asked me to go over the same bit again.";
            case "WASHED_AND_BARBERED"->"I passed the damp comb through your hair. I left the washcloth by the basin and came back to pull up the covers.";
            default->throw new IllegalStateException("Validated hair value missing its authored card");
        };
        var pages=new ArrayList<>(book.pages());pages.add(Math.min(1,pages.size()),Filterable.passThrough(HouseWriting.page(HouseWriting.WritingStyle.WILL,action)));
        var original=fallback.copy();original.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(book.title(),book.author(),book.generation(),List.copyOf(pages),book.resolved()));return original;
    }
    public static String lastStop(ServerPlayer reader,String line){
        return HouseRead.address(reader,line,"Hey, man. "+line,"Hey, miss. "+line,"Hey, friend. "+line);
    }
    public static String caretakerGreeting(ServerPlayer reader){
        return HouseRead.address(reader,"Good evening, caretaker. Your keys are on their hook.","Good evening, sir. Your keys are on their hook.","Good evening, ma’am. Your keys are on their hook.","Good evening, friend. Your keys are on their hook.");
    }
    public static String usual(ServerPlayer reader){
        return HouseRead.address(reader,"Your usual.","Your usual, sir.","Your usual, ma’am.","Your usual, friend.");
    }
    public static String pianistGreeting(ServerPlayer reader){
        return HouseRead.address(reader,"There you are. We kept your place.","There you are, sir. We kept your place.","There you are, ma’am. We kept your place.","There you are, friend. We kept your place.");
    }
    public static String crew(ServerPlayer reader,String line){
        return HouseRead.address(reader,line,"All right, man. "+line,"All right, ma’am. "+line,"All right, friend. "+line);
    }
    public static int routine(String hair){return switch(hair){case "WRAPPED"->0;case "BRAIDED_AND_OILED"->3;case "BRUSHED_DRY"->6;case "WASHED_AND_BARBERED"->9;default->-1;};}
}
