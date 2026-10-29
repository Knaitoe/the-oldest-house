package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.nbt.CompoundTag;

/**
 * The account's opening (0.4.159): the House recites the reader's habits back to them from what the five quiet rooms saw, in one
 * breath, the way a person remembers a night. One clause per room, in room order; skipped rooms drop out. Only observations are
 * recited, never categories, and the reader is always "you".
 */
public final class HouseReadProse {
    private HouseReadProse(){}
    /** One room's clause. At the start of a sentence a clause with a locator opens with its lead-in instead ("In the hall, you"). */
    public record Clause(int room,String phrase,String lead,String locator){
        String head(){return lead!=null&&phrase.contains(locator)?lead+", you "+phrase.replaceFirst(java.util.regex.Pattern.quote(locator),""):"You "+phrase;}
    }
    public static final String[] ITEMS={"put the silk on","worked in the curl cream","reached for the wide-tooth pick","worked the oil into your part","drew a part with the rat-tail comb","took a hair tie from the dish","ran the bristles through","reached for the dry shampoo","pinned your hair up with the claw clip","ran the pocket comb through","opened the tin of pomade","reached for the washcloth on the faucet",
        "tied on the satin bonnet","laid your edges with the little brush","set your hair in the hot rollers","brushed your waves down"};
    private static String coat(String set){return switch(set){case "wool"->"the wool overcoat";case "trench"->"the trench coat";case "barn"->"the barn jacket";default->"";};}
    private static String boots(String set){return switch(set){case "wool"->"the heavy leather boots";case "trench"->"the ankle boots";case "barn"->"the work boots";default->"";};}
    private static String who(String figure,boolean older){return figure.equals("man")?older?"the man":"the boy":older?"the woman":"the girl";}

    public static String waiting(CompoundTag t){
        String first=t.getString("First"),kept=t.getString("Kept");
        if(first.isEmpty())return t.getBoolean("Sat")?"sat in a waiting chair until the door clicked":t.getBoolean("Waited")?"waited out the clock with empty hands":"";
        if(first.equals("paper")&&kept.equals("paper"))return "read the local page until the door clicked";
        if(first.equals("magazine")&&kept.equals("magazine"))return "leafed through the gardening magazine until the door clicked";
        if(first.equals("glasses")&&kept.equals("glasses"))return "put on the reading glasses and kept them on until the door clicked";
        if(first.equals("phone")&&kept.equals("phone"))return "kept the phone from the armrest in your hand until the door clicked";
        if(first.equals("tv")&&kept.equals("channels"))return "turned the little television through its channels";
        String opened=switch(first){case "paper"->"unfolded the local paper";case "magazine"->"opened the gardening magazine";case "glasses"->"put on the reading glasses";case "tv"->"turned the knob on the little television";case "cooler"->"filled a paper cone at the water cooler";default->"picked up the phone from the armrest";};
        String rest=switch(kept){case "paper"->"read the local page until the door clicked";case "magazine"->"leafed through the magazine until the door clicked";case "glasses"->"kept the glasses on until the door clicked";case "phone"->"kept the phone in your hand until the door clicked";case "cart"->"stayed by the cart until the door clicked";case "chair"->"sat in a waiting chair until the door clicked";case "channels"->"flipped through the channels";default->"";};
        return rest.isEmpty()?opened+(t.getBoolean("Waited")?" while you waited":""):opened+" and "+rest;
    }
    /**
     * 0.4.160: the reader picks one routine item up at a time and uses it at the mirror, which puts the light out; what was
     * used is what they did, and anything picked up first and put back is only where their hand went.
     */
    public static String bathroom(CompoundTag t){
        if(t.contains("Used")){
            int used=Math.floorMod(t.getInt("Used"),ITEMS.length);String did=ITEMS[used];
            if(t.contains("Item")&&Math.floorMod(t.getInt("Item"),ITEMS.length)!=used)did="reached for the "+HouseReadMoments.ROUTINES[Math.floorMod(t.getInt("Item"),ITEMS.length)]+", then "+did;
            else if(t.getBoolean("Tap")&&used!=11)did="ran the hot tap and "+did;
            return did+(t.getBoolean("LightOff")?" before the bathroom light went out":" at the bathroom mirror");
        }
        if(t.contains("Item"))return "picked up the "+HouseReadMoments.ROUTINES[Math.floorMod(t.getInt("Item"),ITEMS.length)]+(t.getBoolean("LightOff")?" and turned out the bathroom light":" at the bathroom sink");
        return t.getBoolean("Untouched")?"turned the bathroom light off without touching the counter":t.getBoolean("Tap")?"ran the hot tap at the bathroom sink":"";
    }
    public static String coatroom(CompoundTag t){
        String c=coat(t.getString("Coat")),b=boots(t.getString("Boots")),mirror=t.getString("Mirror");
        String core=!c.isEmpty()?"took "+c+" off the peg in the hall"+(b.isEmpty()?"":t.getString("Boots").equals(t.getString("Coat"))?" and pulled on "+b:" and "+b+" from the mat"):!b.isEmpty()?"pulled on "+b+" in the hall":"";
        if(mirror.endsWith("_before")){String glass=mirror.startsWith("covered")?"hung the towel over the hall glass":"turned the hall glass to the wall";return core.isEmpty()?glass+" and went on still dripping":glass+" before you "+core.replace(" in the hall","");}
        if(core.isEmpty())return "";
        return core+switch(mirror){case "looked"->" and stood a while at the glass";case "turned_after"->" and turned the glass to the wall";case "covered_after"->" and hung the towel over the glass";default->c.isEmpty()?"":" without looking in the glass";};
    }
    public static String shelter(CompoundTag t,boolean older){
        String stood=t.getString("Stood"),looked=t.getString("Looked");boolean song=t.getBoolean("Song");
        return switch(stood){
            case "man","woman"->song?"stood by "+who(stood,older)+" under the eaves until the song finished":"stood a while beside "+who(stood,older)+" under the eaves";
            case "middle"->song?"stood in the middle of the floorboards under the eaves until the song finished":"stood in the middle of the floorboards under the eaves";
            case "lantern"->song?"went out to the lantern on the dock and stayed until the song finished":"went out past the two strangers to the lantern on the dock";
            default->switch(looked){case "man","woman"->"kept glancing at "+who(looked,older)+" under the eaves";case "both"->"looked from one stranger to the other under the eaves";default->"";};
        };
    }
    public static String porch(CompoundTag t){
        return switch(t.getString("First")){
            case "in"->"went straight in when the whistle blew"+(t.getBoolean("Table")?" and sat down to the warm plate":"");
            case "lattice"->"went under the porch behind the lattice when the whistle blew"+(t.getBoolean("Hidden")?" and stayed there until the kitchen light went off":"");
            case "window"->"looked in at the lit window when the whistle blew and stayed out on the lawn"+(t.getBoolean("Hidden")?", then hid under the porch until the kitchen light went off":"");
            case "road"->"walked down the gravel to the road when the whistle blew"+(t.getBoolean("Trees")?" and kept on past the mailbox into the trees without looking back":"");
            default->switch(t.getString("Out")){case "kitchen"->"went in by the back door and out through the front hall";case "gate"->"left the yard by the side gate";case "road"->"took the dirt road out through the trees";default->"";};
        };
    }
    /**
     * 0.4.168: the kitchen table. Only acts, never colours: whether the face and the hands were drawn in the same crayon, and
     * that the picture went up on the fridge.
     */
    public static String kitchen(CompoundTag t){
        if(!t.getBoolean("Hung"))return "";
        return t.getBoolean("Alike")?"drew yourself at the kitchen table, face and hands alike, and put the picture up on the fridge":"drew yourself at the kitchen table and put the picture up on the fridge";
    }
    /** The rooms in the order they are met (0.4.168: the kitchen first); the clause's room is its place in that order. */
    public static List<Clause> clauses(CompoundTag seen,boolean older){
        var out=new ArrayList<Clause>();
        String[] phrases={kitchen(seen.getCompound("kitchen")),waiting(seen.getCompound("waiting")),bathroom(seen.getCompound("bathroom")),coatroom(seen.getCompound("coatroom")),shelter(seen.getCompound("shelter"),older),porch(seen.getCompound("porch"))};
        for(int i=0;i<phrases.length;i++)if(!phrases[i].isEmpty())out.add(new Clause(i,phrases[i],i==COATROOM?"In the hall":i==COATROOM+1?"Under the eaves":null,i==COATROOM?" in the hall":i==COATROOM+1?" under the eaves":null));
        return out;
    }
    /** The coatroom's place in the order: the second breath begins there. */
    public static final int COATROOM=3;
    /** The closing line, chosen by what home was. */
    public static String close(String home){return switch(home){case "HAVEN"->"Somewhere in the House, a plate is being kept warm for you.";case "DANGER"->"The House has learned how you listen to a hallway.";case "RUNAWAY"->"The House has noticed how quickly you find the road.";default->"";};}
    /**
     * Stitches the clauses into one or two sentences with commas and a final "and". With more than two clauses a new sentence
     * begins at the coatroom (or the first room after it), so the night reads as two breaths.
     */
    public static String opening(List<Clause> clauses,String close){
        if(clauses.isEmpty())return "";int split=clauses.size();
        if(clauses.size()>2&&clauses.getFirst().room()<COATROOM)for(int i=1;i<clauses.size();i++)if(clauses.get(i).room()>=COATROOM){split=i;break;}
        var text=new StringBuilder(sentence(clauses.subList(0,split)));if(split<clauses.size())text.append(' ').append(sentence(clauses.subList(split,clauses.size())));
        if(!close.isEmpty())text.append("\n\n").append(close);return text.toString();
    }
    private static String sentence(List<Clause> list){
        var s=new StringBuilder(list.getFirst().head());
        for(int i=1;i<list.size();i++)s.append(i==list.size()-1?", and ":", ").append(list.get(i).phrase());
        return s.append('.').toString();
    }
    public static String opening(CompoundTag seen,boolean older,String home){return opening(clauses(seen,older),close(home));}
    public static String opening(LabyrinthData d,UUID id){return opening(HouseRead.seen(d,id),HouseRead.older(d,id),HouseRead.value(d,id,HouseRead.Trait.HOME,false));}
    /** Book pages of at most 180 characters, broken at a sentence where one fits and otherwise at a word. */
    public static List<String> pages(String text){
        var out=new ArrayList<String>();if(text.isBlank())return out;
        for(String paragraph:text.split("\n\n")){
            while(paragraph.length()>180){int end=paragraph.lastIndexOf(". ",179);end=end>90?end+1:paragraph.lastIndexOf(' ',180);out.add(paragraph.substring(0,end).trim());paragraph=paragraph.substring(end).trim();}
            if(!out.isEmpty()&&out.getLast().length()+paragraph.length()+2<=180&&!out.getLast().contains("\n\n"))out.set(out.size()-1,out.getLast()+"\n\n"+paragraph);else out.add(paragraph);
        }
        return out;
    }
}
