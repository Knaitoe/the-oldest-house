package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.world.item.ItemStack;
/** Original hotel papers. Dates belong to the fiction; personal entries describe actual actions. */
public final class HotelTexts {
    private HotelTexts(){}
    private static ItemStack book(String title,String author,String... pages){return HouseWriting.book(title,author,HouseWriting.WritingStyle.PLAIN,List.of(pages));}
    public static ItemStack rules(){return book("Closing night","The desk",
        "Closing night.\n\nDinner is laid at the remaining table. The bar is open and the orchestra has been asked to stay.",
        "There is no charge tonight. Everything goes on your account.\n\nAnything you mislay is kept at this desk. Ask for it.",
        "Your room is 217, up the stair and along the corridor. The numbers rise toward its end. Should they begin again, walk back against them.",
        "The season closes when the snow does. On its last night your account will be presented here.\n\nThe grounds key is kept where the gardener left it.");}
    public static ItemStack housekeeping(){return book("Housekeeping, room 217","Night staff",
        "1911. The lamp failed before the guest arrived. The floor was patched from below.\n\nPlease do not stand on the pale boards.",
        "1936. One bed made. No luggage entered on the sheet. In the morning the guest's pockets had been put in order.",
        "1978. Candles out. Torches returned in one stack. I found the same name in the drawer twice, in different hands.",
        "The first night we put the pockets in order. After that, whatever is in them goes into the drawers, and we write down what it was before we empty them.\n\nSleeping here does not make this your home.");}
    private static final String[] LOG={
        "October 3. I promised a whole day. No glass before supper. We carried the children's trunks up together. Snow at the sill.",
        "October 9. The road is closed. We ate early. I tested the valve twice. Snow halfway up the glass.",
        "October 14. They asked me to come upstairs. I had only one gauge left to check. Snow above the latch.",
        "October 19. Heat, pressure, vent.\n\nOctober 20. Heat, pressure.\n\nOctober 21. Heat."};
    public static ItemStack log(){return book("One sober day","The caretaker",LOG);}
    /** The log once a guest has signed the second line: the next entry is theirs. */
    public static ItemStack log(String name){var pages=new ArrayList<>(List.of(LOG));pages.add("October 22. Heat, pressure, vent.\n\n"+name+".");return book("One sober day","The caretaker",pages.toArray(String[]::new));}
    public static ItemStack manuscript(){return book("A day's work","The caretaker",
        "I will go upstairs when the gauge is steady.\n\nI will go upstairs when the gauge is steady.",
        "I will go upstairs when the gauge is steady.\n\nI will go upstairs when the gauge is steady.");}
    /** The same page, typed again under a new name. */
    public static ItemStack manuscript(String name){return book("A day's work",name,
        "I will go upstairs when the gauge is steady.\n\nI will go upstairs when the gauge is steady.",
        "I will go upstairs when the gauge is steady.\n\n"+name);}
    public static ItemStack boiler(){return book("Heating plant","Maintenance",
        "The plant begins its shift when someone finds it. Pull the vent before three days of pressure have accumulated. The gauge has four faces.",
        "If the plant blows, the dining fire dies and 217 will not keep a sleeper. The restart switch stands beside the valve.\n\nThis circuit heats the hotel alone.");}
    public static ItemStack office(){return book("The winter position","The manager",
        "Interview notes. He asked whether the family could come up with him. I said the rooms over the kitchen are warm enough if the boiler is kept.",
        "He asked what the last man did. I said he kept the plant, wrote his log and never once came upstairs, and that we laid a place for him at closing all the same.\n\nHe took the position.",
        "The second place at the table is laid every closing night. It has been signed for once. I do not have the year.");}
    public static ItemStack family(){return book("Left on the table","W.",
        "We have the children's trunks up and the beds made. The snow is past the latch already.\n\nCome up when the gauge is steady. We kept your place.");}
    public static ItemStack party(String name,boolean there){return book("Closing night, 1921","The photographer",
        there?name+" is standing in the front row. The print is dry. Nobody remembers the flash.":"Closing night, 1921. The whole house in evening dress on the ballroom floor, the orchestra behind. In the front row, one place is empty. The floor is polished round it.");}

    /** What the guest has run up, in the desk's words. */
    private static String charges(int dinner,int drinks,int debt){
        var items=new ArrayList<String>();if(dinner>0)items.add("dinner at the remaining table");if(drinks>0)items.add(drinks==1?"one drink at the bar":drinks+" drinks at the bar");
        return (items.isEmpty()?"Nothing has been charged to the account yet.":"Charged to the account: "+String.join(" and ",items)+".")+"\n\nUncollected places: "+debt+".";
    }
    /** The account before the last night: what is on it and what the desk holds. */
    public static ItemStack account(String name,int dinner,int drinks,int debt,int held,int drawers){return book("The closing account","The desk",
        "Guest: "+name+".\n\n"+charges(dinner,drinks,debt),
        "Held for the guest: "+held+" at this desk, "+drawers+" in the drawers of 217.\n\nThe account is presented on the last night of the season. It is not the last night.");}
    /** The account on the last night: due, and the three ways out of it, as the desk puts them. */
    public static ItemStack due(String name,int dinner,int drinks,int debt,int held,int drawers){return book("The closing account","The desk",
        "Guest: "+name+".\n\n"+charges(dinner,drinks,debt),
        "Held for the guest: "+held+" at this desk, "+drawers+" in the drawers of 217.\n\nThe season closes tonight. The account is due.",
        "It may be settled at this desk: ring, and the porter will see to it.\n\nThe register is open, and one line in it is blank.\n\nThe service door is for staff.");}
    /** The settled account, read through: the first ending. */
    public static ItemStack settled(String name,int paid){return book("The closing account","The desk",
        "Guest: "+name+".\n\nSettled in full, in kind. "+(paid==0?"Nothing was owing when it was presented.":paid==1?"One thing is kept by the house.":paid+" things are kept by the house."),
        "The bed, the table, the bar and the floor have been entered. You read the account through. What the desk holds of yours is released to you.\n\nNo one has signed the second place.");}
    public static ItemStack register(){return book("The register","The desk",
        "Closing night, 1921.\n\nMr and Mrs Aldous Venn. Dr Hollis Crane. The Misses Pell. Capt. R. Ward and party. The orchestra.",
        "Under the last name a line has been ruled, and under it another, and the second has been left blank, as if someone had been expected to sign after the party.");}
    /** Signing the second line: the second ending. */
    public static ItemStack sign(String name){return book("The register","The desk",
        "The book is open at the last night of the season. Under the caretaker's name the line is still blank. The pen is wet.",
        "You write "+name+" on the second line, in your own hand.\n\nThe porter does not look up. In the dining room the music begins again from the start.");}
    public static ItemStack signed(String name){return book("The register","The desk",
        "Closing night, 1921.\n\nMr and Mrs Aldous Venn. Dr Hollis Crane. The Misses Pell. Capt. R. Ward and party. The orchestra.",
        "On the second line, in your own hand: "+name+".");}
    /** What the reader's Witness account says of the hotel, by the way they left it. */
    public static String witness(String outcome){return switch(outcome){
        case "signed_the_second_place"->"I signed the second line. The photograph has me in its front row now, and the log goes on in my hand: heat, pressure, vent. The staff call me caretaker.";
        case "walked_out_before_closing"->"I went out by the staff door before the account was presented. The hotel kept what it held of mine. The second place is still laid.";
        default->"I was in the photograph, at a party I never went to. The desk had kept everything I had lost, down to the animals. The account was settled, except for a second place setting no one has signed for.";
    };}
    public static ItemStack blind(){return book("Before the unlit stretch","An electrician",
        "The lamps were removed. A bell remains at each turn. Follow its sound until it stops.\n\nThe printed arrow worked for the first two turns.",
        "Then it pointed through a wall. The bell had not moved.\n\nKeep a hand on the wall if you must return. There is no prize for believing a sign.");}
    public static List<ItemStack> specimens(){String reader="Reader_1234567890";return List.of(rules(),housekeeping(),log(),log(reader),manuscript(),manuscript(reader),boiler(),office(),family(),party(reader,true),party(reader,false),
        account(reader,1,12,12,3,9),due(reader,1,12,12,3,9),settled(reader,12),register(),sign(reader),signed(reader),blind());}
}
