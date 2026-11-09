package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.407, The Drawing (docs/THE_DRAWING_0_4_407.md): the notice on the post office board (the source, typed by the office), and the
 * account each reader carries out of the square. The account says only what that reader's own record holds: how many years they have
 * stood in the square, what they were given and whose name was read, what was on their slip, whose household drew the mark, and what
 * they did with the stone, without saying which was right. House writing: never the reader's gender, never a colour, never an age, and
 * never how the reader felt.
 */
public final class DrawingTexts {
    private DrawingTexts(){}
    public static final String TITLE="The account: the drawing";
    public static final String NOTICE_TITLE="Notice";
    public static final String HINT="Be there at ten. Draw when your name is read, and do not open it until everyone has.";
    private static List<Component> pages(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    /** The notice, typed and pinned on the post office board. */
    public static final String NOTICE="NOTICE\n\nThe drawing will be held in the square at ten o'clock this morning, as every year. All households are to be present.\n\n"
        +"The head of each household will draw for the household. Where the head is absent another grown member may draw in their place.\n\n"
        +"Slips are not to be opened until the last has been drawn.\n\n"
        +"New households will be read at the end of the list.\n\n"
        +"The office will be closed for the morning.";
    public static ItemStack source(){return HouseWriting.book(NOTICE_TITLE,"The post office",pages(HouseWriting.WritingStyle.PLAIN,NOTICE));}

    /** How many years, in words. */
    static String years(int n){return switch(n){case 1->"once";case 2->"twice";case 3->"three times";case 4->"four times";case 5->"five times";default->n+" times";};}

    /** The reader's own account, from their record only. */
    public static ItemStack account(String name,CompoundTag t){
        var text=new StringBuilder();
        int year=Math.max(1,t.getInt("Years"));
        text.append("It was a bright morning in a village square, flagstones with grass growing up between them, and everyone in the village came. ");
        if(year>1)text.append("I have stood in that square "+years(year)+" now. Each year there are fewer of them, and the box is in a worse state. ");
        if(t.getBoolean("Lemonade")){String by=t.getString("PouredBy");
            text.append(by.isEmpty()||by.equals("Nell Garrow")?"A woman who came in late, laughing at herself, poured me a glass of lemonade from her jug. ":by+" came in late this year, with the jug, and poured me a glass of lemonade. ");}
        if(t.getInt("Talked")>0)text.append(t.getInt("Talked")==1?"I talked with one of them while we waited. It was all rain and feed prices. ":"I talked with some of them while we waited. It was all rain and feed prices and somebody's new tractor. Nobody said what the drawing was for. ");
        if(t.getBoolean("Called"))text.append("The man with the list read every household in turn, and at the end of the list he read my name, as a new household. ");
        text.append(switch(t.getString("Drew")){
            case "self"->"I went up and put my hand in the box and took a slip, and held it folded like everyone else. ";
            case "for"->"I didn't go up. He drew for me and somebody put the slip in my hand. ";
            default->"";
        });
        if(t.getBoolean("Opened"))text.append("When we were told to look, mine was blank. ");
        String marked=t.getString("Marked"),who=t.getString("MarkedName");int count=t.getInt("MarkedCount");
        if(!marked.isEmpty()){
            text.append(count>1?"It was the "+marked+" household. ":"It was "+who+". ");
            String before=t.getString("InPlaceOf");
            if(!before.isEmpty())text.append(t.getBoolean("InPlaceTalked")?"I had talked with "+before+" the year before. ":"I remembered "+before+" from the year before. ");
            text.append(count>1?"They came up one at a time and drew again, and "+who+" drew the slip with the mark on it and held it out, and nobody took it. ":who+" came up alone and drew again, and held out the slip with the mark on it, and nobody took it. ");
        }
        if(t.getBoolean("Stone")){String from=t.getString("StoneFrom");text.append(from.isEmpty()||from.equals("child")?"A child put a stone in my hand. It was smooth and warm from a pocket. ":from+" put a stone in my hand. It was smooth, and cold. ");}
        text.append(switch(t.getString("Choice")){
            case Drawing.THREW->"I threw it.";
            case Drawing.DROPPED->"I dropped it on the flagstones and it rolled a little way and stopped.";
            case Drawing.WALKED->"I kept it in my hand and walked back down the lane, and they stood aside to let me through. Nobody said anything. Nobody stopped me.";
            case Drawing.STOOD->"I stood where I was and held it, and they went forward without me.";
            default->"";
        });
        text.append("\n\nWhen I looked back from the head of the lane the square was empty. The box was on its stool where it had been.");
        if(t.getBoolean("Stone")&&!t.getString("Choice").equals(Drawing.THREW)&&!t.getString("Choice").equals(Drawing.DROPPED))text.append(" The stone was still in my pocket.");
        var pages=new ArrayList<Component>();pages.addAll(pages(HouseWriting.WritingStyle.WILL,text.toString().trim()));
        return HouseWriting.book(TITLE,name,pages);
    }
}
