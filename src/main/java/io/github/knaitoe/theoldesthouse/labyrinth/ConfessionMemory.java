package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseConfig;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;

/**
 * 0.4.275, the listener's questions answered from what the House remembers (docs/CONFESSION_0_4_275.md). Each question is offered
 * in the conversation panel with answers drawn from the reader's own record: the quiet rooms (what the House saw them do there), the
 * companions it saw them lose, the people they have talked to and their own replies (TalkHistory), and the count. One answer is
 * true. Another is not, and when the House knows better, a line in another hand is written under it in the journal. Saying nothing
 * is always offered and is written down as that; the House never fills a silence. Where the House has no memory to go on, it
 * corrects nothing. Everything here is the reader's own; no reader's record reaches another's journal.
 */
public final class ConfessionMemory {
    private ConfessionMemory(){}
    /** One offered answer: the reader's words in the panel, what goes into the journal, and the House's line under it (empty for none). */
    public record Offer(String label,String written,String correction){
        CompoundTag save(){var t=new CompoundTag();t.putString("L",label);t.putString("W",written);t.putString("C",correction);return t;}
        static Offer load(CompoundTag t){return new Offer(t.getString("L"),t.getString("W"),t.getString("C"));}
    }
    public static final String SAY_NOTHING="Say nothing.";
    /** The House's own word for its line: the journal shows it under the answer. */
    public static String under(String correction){return "(Under it, in another hand: "+correction+")";}

    /** The answers offered for this chapter's question, true first, then the untrue, then silence. */
    public static List<Offer> offers(ServerPlayer p,int chapter){
        var d=LabyrinthData.get(p.server);UUID id=p.getUUID();
        var out=new ArrayList<Offer>();
        switch(chapter){
            case 0->name(p,out);
            case 1->firstDoor(d,id,out);
            case 2->taken(d,id,out);
            case 3->missing(d,id,out);
            case 4->toldTo(p,out);
            case 5->count(p,out);
            case 6->unwritten(p,out);
            default->ownWords(p,d,id,out);
        }
        out.add(new Offer(SAY_NOTHING,ConfessionAnswers.SILENCE,""));
        return out;
    }

    // ------------------------------------------------------------------------------------------------ the eight questions
    private static void name(ServerPlayer p,List<Offer> out){
        String name=p.getGameProfile().getName();
        out.add(new Offer(name+".",name+".",""));
        out.add(new Offer("A name that isn't mine.","A name that isn't mine.",name+"."));
    }
    private static void firstDoor(LabyrinthData d,UUID id,List<Offer> out){
        var first=quiet(d,id).stream().findFirst();
        if(first.isEmpty()){out.add(new Offer("The front door.","The front door.",""));out.add(new Offer("I don't remember.","I don't remember.",""));return;}
        var c=first.get();
        out.add(new Offer(ROOM[c.room()],ROOM[c.room()]+" I "+sentence(firstPerson(c.phrase())),""));
        out.add(new Offer("I don't remember.","I don't remember.","You "+sentence(c.phrase())));
    }
    private static void taken(LabyrinthData d,UUID id,List<Offer> out){
        String thing=thing(d,id);
        if(thing.isEmpty()){out.add(new Offer("Nothing anyone missed.","Nothing anyone missed.",""));out.add(new Offer("Only what was offered.","Only what was offered.",""));return;}
        out.add(new Offer(capital(thing)+".",capital(thing)+". Nobody stopped me.",""));
        out.add(new Offer("Nothing.","Nothing.","You took "+thing+"."));
    }
    private static void missing(LabyrinthData d,UUID id,List<Offer> out){
        String lost=ThreeFingers.lostCompanion(d,id);
        if(lost==null){out.add(new Offer("Nobody.","Nobody.",""));out.add(new Offer("Everyone I left behind.","Everyone I left behind.",""));return;}
        String mine=capital(called(lost,"my"))+".";
        out.add(new Offer(mine,mine,""));
        out.add(new Offer("Nobody.","Nobody.",capital(called(lost,"your"))+"."));
    }
    private static void toldTo(ServerPlayer p,List<Offer> out){
        var speakers=speakers(p);
        if(speakers.isEmpty()){out.add(new Offer("You.","You.",""));out.add(new Offer("Nobody. I keep it.","Nobody. I keep it.",""));return;}
        String most=speakers.getFirst();
        out.add(new Offer(most+".",most+".",""));
        out.add(new Offer("Nobody. I keep it.","Nobody. I keep it.","You talked to "+most+" more than to anyone."));
    }
    private static void count(ServerPlayer p,List<Offer> out){
        int n=killed(p);String said=capital(number(n))+".";
        out.add(new Offer(said,said,""));
        if(n>0)out.add(new Offer("None.","None.",said));
        out.add(new Offer("I stopped counting.","I stopped counting.",said));
    }
    private static void unwritten(ServerPlayer p,List<Offer> out){
        var speakers=speakers(p);var recent=recentSpeakers(p);
        for(String who:recent.subList(0,Math.min(2,recent.size())))out.add(new Offer(who+"'s.",who+".",""));
        if(recent.isEmpty()&&!speakers.isEmpty())out.add(new Offer(speakers.getFirst()+"'s.",speakers.getFirst()+".",""));
        out.add(new Offer("My own.",p.getGameProfile().getName()+".",""));
    }
    private static void ownWords(ServerPlayer p,LabyrinthData d,UUID id,List<Offer> out){
        for(String said:ownReplies(p,2))out.add(new Offer("“"+said+"”",said,""));
        String saw=saw(d,id);
        if(!saw.isEmpty())out.add(new Offer("I'll tell it as it happened.",firstPerson(saw),""));
        out.add(new Offer("I did what I was told.","I did what I was told.",saw));
    }

    // ------------------------------------------------------------------------------------------------ what the House remembers
    /** What the quiet rooms saw, in their order; nothing when the read is turned off. */
    static List<HouseReadProse.Clause> quiet(LabyrinthData d,UUID id){
        if(!HouseConfig.readEnabled(false))return List.of();
        return HouseReadProse.clauses(HouseRead.seen(d,id),HouseRead.older(d,id));
    }
    /** The House's first breath about the reader (their quiet rooms, in its second person), or empty. */
    static String saw(LabyrinthData d,UUID id){
        var clauses=quiet(d,id);if(clauses.isEmpty())return "";
        String opening=HouseReadProse.opening(clauses,"");int end=opening.indexOf(". ");
        return end<0?opening:opening.substring(0,end+1);
    }
    /** How each quiet room is named when the reader answers with it. */
    private static final String[] ROOM={"The kitchen.","The waiting room.","The bathroom.","The hall with the coats.","The shelter under the eaves.","The porch."};
    /** The one thing the House saw the reader take for themselves: the coat off the peg, the thing they kept hold of in the waiting room, the bathroom counter's. */
    static String thing(LabyrinthData d,UUID id){
        if(!HouseConfig.readEnabled(false))return "";
        var seen=HouseRead.seen(d,id);
        var coat=HouseReadWardrobe.byId(HouseReadWardrobe.coatOf(seen.getCompound("coatroom")));
        if(coat!=null&&!coat.boots())return "the "+coat.name()+" off "+coat.place();
        var waiting=seen.getCompound("waiting");
        for(String key:new String[]{waiting.getString("Kept"),waiting.getString("First")}){
            String held=switch(key){case "paper"->"the local paper";case "magazine"->"the gardening magazine";case "glasses"->"the reading glasses";case "phone"->"the phone from the armrest";default->"";};
            if(!held.isEmpty())return held;
        }
        var bath=seen.getCompound("bathroom");
        if(bath.contains("Used")||bath.contains("Item")){int item=Math.floorMod(bath.contains("Used")?bath.getInt("Used"):bath.getInt("Item"),HouseReadMoments.ROUTINES.length);return "the "+HouseReadMoments.ROUTINES[item]+" from the bathroom counter";}
        return "";
    }
    /** Everyone who has spoken to the reader in the panel, most often first (the listener himself left out). */
    static List<String> speakers(ServerPlayer p){
        var counts=new LinkedHashMap<String,Integer>();
        for(var talk:talks(p))for(var line:lines(talk)){String s=line.getString("S");if(person(s))counts.merge(s,1,Integer::sum);}
        var out=new ArrayList<>(counts.keySet());out.sort((a,b)->counts.get(b)-counts.get(a));return out;
    }
    /** The same, the most recently heard first. */
    static List<String> recentSpeakers(ServerPlayer p){
        var out=new ArrayList<String>();var talks=talks(p);
        for(int i=talks.size()-1;i>=0;i--){var lines=lines(talks.get(i));for(int j=lines.size()-1;j>=0;j--){String s=lines.get(j).getString("S");if(person(s)&&!out.contains(s))out.add(s);}}
        return out;
    }
    /** The reader's own earlier replies, most recent first, as they chose them (silences and very short ones left out). */
    static List<String> ownReplies(ServerPlayer p,int most){
        var out=new ArrayList<String>();var talks=talks(p);
        for(int i=talks.size()-1;i>=0&&out.size()<most;i--){
            if(LISTENER.equals(talks.get(i).getString("Who")))continue;
            var lines=lines(talks.get(i));
            for(int j=lines.size()-1;j>=0&&out.size()<most;j--){var l=lines.get(j);String t=l.getString("T").trim();
                if(l.getBoolean("R")&&t.length()>=12&&!t.equals(SAY_NOTHING)&&!out.contains(t))out.add(t.length()>120?t.substring(0,120).trim()+"…":t);}
        }
        return out;
    }
    public static final String LISTENER="The listener";
    private static boolean person(String s){return !s.isBlank()&&!s.equals(LISTENER);}
    private static List<CompoundTag> talks(ServerPlayer p){var all=TalkHistory.conversations(p.server,p.getUUID());var out=new ArrayList<CompoundTag>();for(int i=0;i<all.size();i++)out.add(all.getCompound(i));return out;}
    private static List<CompoundTag> lines(CompoundTag talk){ListTag l=talk.getList("Lines",Tag.TAG_COMPOUND);var out=new ArrayList<CompoundTag>();for(int i=0;i<l.size();i++)out.add(l.getCompound(i));return out;}
    /** The count the last chapter asks for: the four kinds the listener's book counts. */
    static int killed(ServerPlayer p){
        var stats=p.getStats();int n=0;
        for(var type:List.of(EntityType.VILLAGER,EntityType.WOLF,EntityType.IRON_GOLEM,EntityType.WANDERING_TRADER))n+=stats.getValue(Stats.ENTITY_KILLED.get(type));
        return n;
    }

    // ------------------------------------------------------------------------------------------------ words
    /** The House's second person turned into the reader's first ("You took the coat" becomes "I took the coat"). */
    static String firstPerson(String s){
        return s.replaceAll("\\bYourself\\b","Myself").replaceAll("\\byourself\\b","myself").replaceAll("\\bYour\\b","My").replaceAll("\\byour\\b","my").replaceAll("\\bYou\\b","I").replaceAll("\\byou\\b","I");
    }
    private static String sentence(String s){s=s.trim();return s.endsWith(".")?s:s+".";}
    static String capital(String s){return s.isEmpty()?s:Character.toUpperCase(s.charAt(0))+s.substring(1);}
    /** A companion as the reader calls it: by its name, or "my wolf" for one that never had one; the House says "your wolf". */
    private static String called(String name,String whose){return isKind(name)?whose+" "+name.toLowerCase(Locale.ROOT):name;}
    private static boolean isKind(String name){return Set.of("Wolf","Cat","Parrot","Horse","Donkey","Mule","Llama","Trader Llama","Camel","Ocelot","Fox","Skeleton Horse","Zombie Horse").contains(name);}
    static String number(int n){String[] words={"none","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve"};return n>=0&&n<=12?words[n]:Integer.toString(n);}
}
