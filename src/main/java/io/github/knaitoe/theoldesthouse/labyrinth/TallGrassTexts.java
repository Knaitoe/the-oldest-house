package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.349, In the Tall Grass: the county's notice on its post by the wagon (the source), and the account each reader carries away from
 * the road. The account says only what that reader's own record holds: the wagon as they found it; whether they went in, saw the road
 * from the first rows, were moved and with whom; what they heard true and what from the wrong side; who called to them; whether they
 * found the heart of the field and the cap; how they came out; who shouted what, and which way the driver went. House writing: never the
 * reader's gender, never a colour, never an age.
 */
public final class TallGrassTexts {
    private TallGrassTexts(){}
    public static final String TITLE="The account: the tall grass";
    public static final String NOTICE_TITLE="County Road 6: notice";
    public static final String HINT="Stay where you can see the road. If you go in, keep track of your own way out.";
    private static List<Component> pages(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    /** The county's notice, stapled to its post on the north shoulder. */
    public static final String NOTICE="COUNTY ROAD 6\nNOTICE TO MOTORISTS\n\n"
        +"The verge on the north side of this road will not be cut this season.\n\n"
        +"Do not leave a vehicle standing on the shoulder.\n\n"
        +"If you hear someone calling in the grass, stay with your vehicle. Do not go in after them. Call the number below and wait.\n\n"
        +"The number has been torn off.";
    public static ItemStack source(){return HouseWriting.book(NOTICE_TITLE,"The County",pages(HouseWriting.WritingStyle.WILL,NOTICE));}

    /** The reader's own account, from their record only. */
    public static ItemStack account(String name,CompoundTag t){
        var text=new StringBuilder();
        text.append("A county road, late in the afternoon. The wagon was on the shoulder with its hazards going and its door open. ");
        if(t.getBoolean("HeardRadio"))text.append(t.getString("GrassRadio").equals("tape")?"The tape was playing, near the end of its side. ":"A phone on the dash was playing a station that kept losing its signal. ");
        if(t.getBoolean("Packed"))text.append("The back was packed to the roof. I had somewhere to be. ");
        text.append("Out of the grass across the ditch a boy was calling. He said he couldn't find the road. ");
        boolean in=t.getBoolean("Committed");
        if(t.getBoolean("SawRoad"))text.append(in?"From the first rows I could still see the road behind me through the stems. ":"I went a few rows in, far enough to see the road back through the stems, and no farther. ");
        if(!in){
            if(t.getInt("Called")>0)text.append("I stood by the wagon and called into the grass for whoever was in there. The last thing I called was “"+t.getString("CallLine")+"” ");
            else if(!t.getBoolean("Partway"))text.append("I didn't go in. ");
        }else{
            text.append("I went in past the old fence posts. ");
            int moved=t.getInt("Moved");
            if(moved>0)text.append(moved>=4?"The grass moved me, again and again: I would look up and be somewhere else, farther in, and the boy always a little ahead. ":"The grass moved me: I would look up and be somewhere else, farther in, and the boy always a little ahead. ");
            if(!t.getString("MovedWith").isEmpty())text.append("When it moved me it moved "+t.getString("MovedWith")+" with me. ");
            if(t.getBoolean("HeardWrong"))text.append("Whenever I walked, the road sounded from the wrong side. ");
            if(t.getBoolean("HeardTrue"))text.append("When I stood still I could hear the hazards, and they were where the road was. ");
            if(!t.getString("HeardCaller").isEmpty()){var c=t.getString("HeardCaller");
                text.append(t.getBoolean("FirstAhead")?c+" called to me from the road, and the first time I heard it from ahead of me. ":c+" called to me from the road. ");
                if(t.getInt("TrueCalls")>0)text.append("Later I heard "+c+" from the right side, while they stood still by the wagon. ");}
            if(!t.getString("HeardOther").isEmpty())text.append("I heard "+t.getString("HeardOther")+" somewhere in the grass, always from the wrong side. ");
            if(!t.getString("FoundOther").isEmpty())text.append("I found "+t.getString("FoundOther")+" a few stems away. ");
            if(t.getBoolean("SawHeart"))text.append("Deep in, the grass was trampled flat in a ring, as if something had turned round and round there. There was a small cap in the middle of it. ").append(t.getBoolean("TookCap")?"I took it. ":"I left it. ");
        }
        if(t.getBoolean("LetOut"))text.append("Then the grass thinned and I could see the road, close, and another car pulling over. ");
        else if(t.getBoolean("Partway"))text.append("I came to the road late. Another car was pulling over. ");
        else if(!t.getString("Outcome").isEmpty())text.append("Another car pulled over. ");
        if(!t.getString("Outcome").isEmpty()){
            text.append("The driver got out, looked at the grass, and asked if somebody had shouted. ");
            String by=t.getString("ShoutBy");boolean me=!t.getString("Shout").isEmpty();String who=me?"I":by;
            switch(t.getString("Outcome")){
                case TallGrass.WARNED->text.append(who+" shouted to them to stay by their car and not come in. The driver stood a moment, then got back in and drove on. ");
                case TallGrass.CALLED->text.append(who+" shouted for help, in here. The driver stepped down off the shoulder into the grass. I didn't see them come out. ");
                default->text.append((t.getBoolean("SaidNothing")?"I didn't say anything, and neither did anyone else. ":"Nobody said anything. ")+"The driver listened, and heard the boy instead, and went in after him. ");
            }
            if(!me&&!by.isEmpty()&&t.getBoolean("LetOut"))text.append(t.getBoolean("SaidNothing")?"I had said nothing. ":"I hadn't shouted yet. ");
        }
        text.append("\n\nI walked back to the wagon. The hazards were still going, and the House's door was in the brush across the road, where I had left it.");
        return HouseWriting.book(TITLE,name,pages(HouseWriting.WritingStyle.WILL,text.toString().trim()));
    }
}
