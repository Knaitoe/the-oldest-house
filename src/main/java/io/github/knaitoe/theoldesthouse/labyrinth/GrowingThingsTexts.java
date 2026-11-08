package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.350, Growing Things: the Saturday list tied to the Barretts' gate post (the source), and the account each reader carries away from
 * the yard. The account says only what that reader's own record holds: the washing, the table and whose hands were on it, the window, what
 * they saw of John going out and of his spade, the floor, the question at supper, what they carried up and who carried his plate, and how
 * it ended for them. House writing: never the reader's gender, never a colour, never an age.
 */
public final class GrowingThingsTexts {
    private GrowingThingsTexts(){}
    public static final String TITLE="The account: growing things";
    public static final String LIST_TITLE="Saturday";
    public static final String HINT="Help with the chores. When supper comes, what you carry up is your answer.";
    private static List<Component> pages(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    /** The list in their mother's hand, with a line squeezed in at the bottom in pencil. */
    public static final String LIST="SATURDAY\n\n"
        +"Merry: washing in off the line before it rains. ALL of it, not just yours.\n\n"
        +"Marjorie: help your sister.\n\n"
        +"John: the table away from that window before it goes. And PLEASE the back step.\n\n"
        +"Supper's on the stove. Don't wait for me.\n— S.\n\n"
        +"(in pencil, smaller) they came up in the night again. more by the step. Marjorie says dont pull them";
    public static ItemStack source(){return HouseWriting.book(LIST_TITLE,"The Barretts",pages(HouseWriting.WritingStyle.WILL,LIST));}

    static String carried(String list){
        var out=new ArrayList<String>();for(var w:list.split(","))switch(w){case "pot"->out.add("the pot");case "loaf"->out.add("the loaf");default->{}}
        return out.size()==2?out.get(0)+" and "+out.get(1):out.isEmpty()?"":out.get(0);
    }
    /** The reader's own account, from their record only. */
    public static ItemStack account(String name,CompoundTag t){
        var text=new StringBuilder();
        text.append("An alley behind a row of houses, and a gate with its latch tied shut with twine. Behind it, a family's yard on a Saturday afternoon. ");
        if(t.getBoolean("SawShoots"))text.append("There were shoots coming up in the lawn where nothing had been planted, more of them toward the house. ");
        int washing=t.getInt("Washing");
        if(washing>0)text.append(washing==1?"Merry asked me to help get the washing in, and I took a piece off the line. ":"Merry asked me to help get the washing in, and I took "+washing+" things off the line. ");
        if(!t.getString("TableWith").isEmpty())text.append("The kitchen window's frame was bowing in. I took one end of the table and "+t.getString("TableWith")+" took the other, and we moved it away from the window. ");
        else if(t.getBoolean("TableAlone"))text.append("The kitchen window's frame was bowing in. John dragged the table away from it on his own. ");
        if(t.getBoolean("Kept"))text.append("Merry kept the place beside her for me. ");
        if(t.getBoolean("Inside"))text.append("Inside, Marjorie stayed at the window, watching the yard. Merry talked. ");
        if(t.getBoolean("SawJohnGo"))text.append("In the afternoon John took a spade out to clear the back step. ");
        if(t.getBoolean("SawJohnWalk"))text.append("I watched him leave the spade standing and walk round the side of the house. ");
        if(t.getBoolean("JohnGone")||t.getBoolean("SawJohnWalk"))text.append("He didn't come back in. ");
        if(t.getBoolean("SawSpade"))text.append("Later his spade was standing in the shoots where the back step had been. ");
        if(t.getBoolean("JohnGone"))text.append("Marjorie said he was out with them. Nobody said anything else about it. ");
        if(t.getBoolean("SawFrame"))text.append(t.getBoolean("Shielded")?"When the window frame split, Marjorie stepped between me and it. ":"The window frame split, and the shoots came through it. ");
        int floor=t.getInt("SawFloor");
        if(floor>=3)text.append("All afternoon the kitchen floor came up, the boards tenting, then breaking, shoots through every gap. ");
        else if(floor>0)text.append("The kitchen floor was coming up, the boards tenting, shoots in the gaps. ");
        String outcome=t.getString("Outcome");
        if(t.getBoolean("WentBack")){
            text.append(t.getBoolean("HeardAsk")?"Merry asked whether they should bring Dad's plate, and I went back out through the gate before supper was up. ":"I went back out through the gate before supper was up. ");
            text.append("Nobody called after me. Later I found a dry seed head caught in my sleeve.");
        }else if(!outcome.isEmpty()){
            if(t.getBoolean("Partway"))text.append("I came in late, when the floor was already lifting. ");
            text.append("By supper the kitchen floor couldn't hold the table, so supper was carried up to the girls' room. ");
            if(t.getBoolean("HeardAsk"))text.append("Merry asked whether we should bring Dad's plate. ");
            var mine=t.getString("Carried");boolean myPlate=Arrays.asList(mine.split(",")).contains("dad");var rest=carried(mine);
            if(myPlate)text.append(rest.isEmpty()?"I carried his plate up and set it at the place by the window. ":"I carried "+rest+" up, and his plate, and set it at the place by the window. ");
            else if(!rest.isEmpty())text.append("I carried "+rest+" up. ");
            else text.append("I didn't carry anything. ");
            String by=t.getString("DadBy");
            if(!myPlate&&!by.isEmpty())text.append(by+" carried his plate up and set it at the place by the window. ");
            if(outcome.equals(GrowingThings.CARRIED))text.append("Merry and Marjorie sat with it. Marjorie said he would come up for it. ");
            else text.append("His plate stayed downstairs on the counter. Merry looked back down the stairs toward the kitchen, and didn't say anything else. ");
            text.append("\n\nMarjorie put a seed head in my hand, dry, off the kitchen sill. Through the girls' window the yard was shoots from the fence to the house, and the spade stood in them.");
        }
        return HouseWriting.book(TITLE,name,pages(HouseWriting.WritingStyle.WILL,text.toString().trim()));
    }
}
