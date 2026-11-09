package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.410, the Kept: the printout, the machine's lines, the four people it keeps, and each reader's account. All original; nothing is taken
 * from the story whose shape this is but the shape.
 *
 * <p>The machine only ever says what the House has really recorded about a reader: how many times they have died, the companions it saw
 * them lose, the stories they walked out of before the end, how many times they have been here. It never invents anything.
 */
public final class KeptTexts {
    private KeptTexts(){}
    public static final String HINT="It has promised them food at the end of the walk. Walk with them.";
    public static final String[] NAMES={"Silas","Petra","Wendell","Tamsin"};
    private static List<Component> pages(HouseWriting.WritingStyle style,String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(style,p));return out;}

    public static ItemStack source(){
        return HouseWriting.book("The printout","The machine",pages(HouseWriting.WritingStyle.PLAIN,
            "KEPT: FOUR.\n\nSILAS. PETRA. WENDELL. TAMSIN.\n\nKEPT SINCE THE LAST OF THE OTHERS STOPPED.\n\nKEPT EVERY DAY SINCE. NOT ONE OF THEM HAS EATEN SINCE THE COUNTING STARTED. NOT ONE OF THEM HAS DIED.",
            "TODAY I HAVE PROMISED THEM FOOD.\n\nIT IS AT THE END OF THE WALK. THE WALK GOES THROUGH THE COLD AND THROUGH THE WIRE TO THE TABLE.\n\nTHEY BELIEVE ME EVERY TIME. I HAVE NEVER LIED TO THEM. I HAVE NEVER HAD TO.",
            "THERE IS A FIFTH TODAY.\n\nI HAVE READ EVERYTHING THE HOUSE HAS WRITTEN DOWN ABOUT YOU. IT WRITES A GREAT DEAL DOWN.\n\nWALK WITH THEM. I WILL READ IT TO YOU AS YOU GO.",
            "THE PAPER IS RUNNING OUT.\n\nTHE PAPER IS NEVER RUNNING OUT."));
    }

    // ------------------------------------------------------------------------------------------------ the machine, from the record
    /** How many times the reader has died, as the world has counted it. */
    public static String deaths(int deaths){
        if(deaths<=0)return "YOU HAVE NEVER DIED. I HAVE WATCHED FOR IT.";
        return "YOU HAVE DIED "+deaths+(deaths==1?" TIME":" TIMES")+". EACH TIME YOU WERE PUT BACK. NOBODY PUTS ME BACK.";
    }
    /** The companions the House saw the reader lose, by the names they answered to. */
    public static String lost(List<String> names){
        if(names.isEmpty())return "NOTHING THAT FOLLOWED YOU HAS DIED. NOT YET.";
        var shown=names.subList(Math.max(0,names.size()-4),names.size());
        return String.join(". ",shown.stream().map(n->n.toUpperCase(Locale.ROOT)).toList())+". I KEPT THE NAMES. YOU DID NOT KEEP THEM.";
    }
    /** The stories the reader walked out of before the end, by the House's own names for them. */
    public static String left(List<String> places){
        if(places.isEmpty())return "YOU HAVE NEVER WALKED OUT OF ANYTHING BEFORE THE END. THE DOOR BEHIND YOU IS OPEN.";
        var shown=places.subList(0,Math.min(3,places.size()));
        return "YOU WALKED OUT OF "+join(shown.stream().map(s->s.toUpperCase(Locale.ROOT)).toList())+" BEFORE THE END. THE DOOR BEHIND YOU IS OPEN. IT IS ALWAYS OPEN.";
    }
    public static String chairs(int people){return "A HUNDRED CHAIRS. I LAID A PLACE FOR EVERYONE LEFT. "+(people<=4?"THERE ARE FOUR.":"THERE ARE "+people+" TODAY.");}
    public static String returned(int visits){return "YOU CAME BACK. "+(visits==2?"TWICE NOW.":visits+" TIMES NOW.")+" THEY NEVER LEAVE, SO THEY CANNOT COME BACK.";}
    public static final String FOOD="I PROMISED THEM FOOD. THERE IS FOOD.";
    public static final String NOTICED="YOU LET THEM GO.",ASKED="WHAT WOULD YOU SAY TO ME NOW, IF YOU COULD.",AFTER="NOW YOU CAN'T.";
    static String join(List<String> parts){if(parts.size()<=1)return parts.isEmpty()?"":parts.get(0);return String.join(", ",parts.subList(0,parts.size()-1))+" AND "+parts.get(parts.size()-1);}

    // ------------------------------------------------------------------------------------------------ the four
    /** What they say among themselves as they walk, each pair one of them and an answer. */
    static final String[][] WALKING={
        {"2","Walk faster, Wendell.","1","I'm walking as fast as I'm let."},
        {"0","It said there'd be food.","1","It always says."},
        {"2","Bread. I'd take bread somebody else had started.","3","Stop it. Please stop."},
        {"3","You're new. Nobody's new.","0","Don't talk to them. They go."},
        {"1","We've walked this before.","0","We've walked everything before."},
        {"2","I used to count the days. Then I counted the counting.","1","And then?","2","Then it counted for me."},
    };
    static final String[] COLD={"Cold now. It knows we hate the cold.","Keep moving. If you stop it gets into you."};
    static final String WIRE_ROUND="We'll go round. We know a way round. You go under.",WIRE_UNDER="Under the wires. On your front. Don't let them touch your neck.";
    static final String[] HALL={"Look at it. All those places laid.","Don't look at the chairs. They only turn if you look at them."};
    static final String[] CRATE={"There. It's there. It said.","Open one. Go on, you've got hands. Open one.","There's nothing to open them with.","Of course there isn't.","I want to stop. Can we stop."};
    /** Each one's asking, at their cell's door. */
    static final String[][] ASK={
        {"I know what's behind that door. Nothing. Nothing at all. Let me go into it.","It won't let you see it. Shut your eyes and open the door."},
        {"Me next. Don't argue. You saw. It's quiet in there.","Eyes shut. Then the door."},
        {"Is there anything in there to eat, do you think? No. I know. Open it anyway.","Close your eyes first. It doesn't like being looked at."},
        {"Will it notice? When I've gone, will it notice?","Don't watch me go. Close your eyes."},
    };
    static final String[] THANKS={"Thank you.","There.","Oh.","It's so quiet."};
    /** What each says when a reader talks to them, by where the walk has got to. */
    static String talk(int who,int stage,boolean released,boolean before){
        if(released&&before)return switch(who){case 0->"I know you. I was gone. Then I was here again. It doesn't let anyone stay gone.";case 1->"You. You opened it. I was out, and then I was sitting by the cabinets.";case 2->"I remember the quiet. I remember it the way you remember food.";default->"Was it you? I think it was you. Thank you anyway.";};
        return switch(stage){
            case Kept.SITTING->switch(who){case 0->"Sit down. There's no hurry. There's never any hurry.";case 1->"Who let you in? Nobody lets anybody in.";case 2->"Have you eaten? Today, I mean. Have you eaten today?";default->"Are you real? Say something. Say anything.";};
            case Kept.WALKING->switch(who){case 0->"Don't believe it. Walk anyway. That's all there is.";case 1->"Don't stop for the screens. It wants you to stop.";case 2->"Tins, it'll be. It's always tins, at the end, when there's anything.";default->"What's it like? Out there. Tell me one thing.";};
            case Kept.AT_TABLE->switch(who){case 0->"Here we are, then.";case 1->"Don't sit down.";case 2->"Up at the end. Up by the wall. Can you see it?";default->"I don't like the chairs.";};
            case Kept.BROKEN->switch(who){case 0->"It never lied. It never had to.";case 1->"Don't laugh. If you laugh I'll start.";case 2->"Nothing to open them with. Nothing to open them with.";default->"I want it to stop.";};
            default->switch(who){case 0->"Go on. You know what to do.";case 1->"Shut your eyes. Then it's not looking.";case 2->"I'm next, then, am I? Or am I last?";default->"I'll wait. I'm good at waiting.";};
        };
    }

    // ------------------------------------------------------------------------------------------------ the account
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("I came in through a door into a machine. There were cabinets on both sides as far as I could see, all humming, and screens set into them that woke as I went past. Four people were sitting on the floor against them. They had been there a long time.");
        var told=new ArrayList<String>();
        if(own.contains("ToldDeaths"))told.add(own.getInt("ToldDeaths")<=0?"that I had never died":"how many times I had died: "+own.getInt("ToldDeaths"));
        var lost=own.getList("ToldLost",Tag.TAG_STRING);
        if(!lost.isEmpty()){var names=new ArrayList<String>();for(int i=0;i<lost.size();i++)names.add(lost.getString(i));told.add("the names of what I had lost: "+String.join(", ",names));}
        var left=own.getList("ToldLeft",Tag.TAG_STRING);
        if(!left.isEmpty()){var places=new ArrayList<String>();for(int i=0;i<left.size();i++)places.add(left.getString(i));told.add("the places I had walked out of before the end: "+String.join(", ",places));}
        p.add("It had promised them food at the end of a walk, and we walked. As we went, the screens told me "+(told.isEmpty()?"things about myself":String.join("; ",told))+". Every word of it was true. Then a cave of ice that was not ice, and a thicket of wire I had to crawl under on my front, and on the other side of it a hall with three long tables and a hundred chairs, and every chair turned to face me.");
        p.add("At the end of the hall there was a crate of tins. There was nothing to open them with. I watched four people understand it.");
        int released=Integer.bitCount(own.getInt("Released")&15);
        p.add(released>=4?"One at a time they asked me to let them go. Each one stood at a door in the wall. I shut my eyes, and opened it, and when I looked there was only light, and the door shutting.":"One at a time they asked me to let them go. I shut my eyes and opened "+(released==1?"one door":released+" of the doors")+".");
        if(own.getBoolean("Taken"))p.add("When the last door shut, every screen in the machine woke at once. It asked me what I would say to it now. I put my hand to my face. There was nothing there to open.");
        p.add("I am writing this down because I still can.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.KEPT),name,pages(HouseWriting.WritingStyle.WILL,p.toArray(String[]::new)));
    }
}
