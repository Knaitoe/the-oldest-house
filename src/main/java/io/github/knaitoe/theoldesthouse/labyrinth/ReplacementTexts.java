package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.218, the replacement. The posters on the telephone pole (the source, the newest in the reader's own hand), the noises it makes
 * at their heels, and the page in the mailbox (the account): a page torn from a ledger kept in the open house, and the reader's own
 * words after it. All of it is original. House writing: never the reader's gender, never a colour of theirs, never an age; the reader
 * is "you", or named. The companion is named when it has a name.
 */
public final class ReplacementTexts {
    private ReplacementTexts(){}
    private static final int PAGE=150;
    static List<Component> styled(HouseWriting.WritingStyle style,String text){var out=new ArrayList<Component>();for(String p:LookoutTexts.pages(text,PAGE))out.add(HouseWriting.page(style,p));return out;}

    /** What it says at the reader's heels: a cat's noises, or a dog's, in a person's mouth. */
    public static final String[] CAT={"\"Mrrow.\"","\"Mrrp?\"","\"Mew.\"","It purrs. It isn't a purr. It's someone saying the word under their breath.","\"Meow,\" it says, the way you'd read it off a page."};
    public static final String[] DOG={"\"Woof.\"","\"Wuff.\"","It whines, low, through its teeth.","It pants, close behind you. Its breath smells of tins.","\"Woof,\" it says, the way you'd read it off a page."};
    public static final String HINT="It isn't yours. Yours is somewhere on this street, and every house is boarded but one.";

    /** The posters on the pole: years of them, and the newest in the reader's own hand. */
    public static ItemStack source(String reader,CompoundTag own){
        boolean dog=own.getString("Species218").equals("dog");String name=own.getString("Name218");
        var pages=new ArrayList<Component>();
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,
            "Posters stapled to the pole, one over another, so many that the wood doesn't show. LOST, all of them. Cats, dogs, a rabbit, a bird. The photographs have gone pale in the weather until every animal on them is the same pale shape.\n\n"
            +"Someone has gone round with a pen and put a small tick in the corner of every one."));
        pages.addAll(styled(HouseWriting.WritingStyle.KAREN,
            "The newest is on top, and it's in your own handwriting:\n\nLOST\n\n"+(name.isEmpty()?(dog?"our dog":"our cat"):name.toUpperCase(Locale.ROOT))
            +"\n\nWas right behind me.\nComes when called.\n\nIf you see "+(name.isEmpty()?"it":name)+", don't let anyone else take "+(name.isEmpty()?"it":"them")+" in."));
        return HouseWriting.book("Posters on the pole",reader,pages);
    }

    /** The page in the mailbox: torn from the ledger in the open house, in a careful hand, and the reader's own words after it. */
    public static String ledger(String reader,CompoundTag own){
        String name=Replacement.name(own);boolean dog=own.getString("Species218").equals("dog");
        var t=new StringBuilder("A page torn from a ledger, ruled in two columns, in a careful hand that gets less careful down the page.\n\n");
        String[][] rows={{"Biscuit","stayed"},{"Pepper","stayed"},{"Rufus","stayed"},{"Mouse","stayed"},{"Duchess","stayed"},{"Bear","stayed"},{"Smudge","stayed"}};
        for(var r:rows)t.append(r[0]).append(" .......... ").append(r[1]).append('\n');
        t.append(name.equals("the cat")||name.equals("the dog")?"(no name) .......... went home":name+" .......... went home");
        t.append("\n\nThe last line is in a different pen. It's been gone over twice.");
        return t.toString();
    }
    public static String afterwards(String reader,CompoundTag own){
        String name=Replacement.name(own);boolean dog=own.getString("Species218").equals("dog");boolean named=!(name.equals("the cat")||name.equals("the dog"));
        var t=new StringBuilder();
        t.append("I didn't see it go. I looked round and "+(named?name:"mine")+" wasn't there, and something else was, at my heels, on its hands and knees, saying "+(dog?"\"woof\"":"\"mrrow\"")+" as if it had learned the word from a book.\n\n");
        t.append("It kept up. I couldn't lose it. When I stopped, it sat. When I crouched, it lay down and rolled half over, the way "+(named?name:"mine")+" does, and I hated that it knew that.\n\n");
        t.append("Every house was boarded but one. Inside it there were bowls everywhere, and blankets over shapes I didn't lift, and collars on hooks, a long row of them, every one with a name.\n\n");
        if(own.getInt("TimesLeft218")>0)t.append("The first time, I left without going in. "+(named?name:"Mine")+" was waiting at home when I got there, sitting where I'd have told it to. It wouldn't look at me for a day.\n\n");
        t.append(own.getInt("Freed218Count")>0?(named?name:"Mine")+" was in a crate in the back bedroom. When I opened it, the thing behind me stood up, and said "+(named?name+"'s name":"a name")+" in its own voice, as if it was trying it out. Then it went and lay down with the others.\n\n"
            :"The crate in the back bedroom was empty, and the blanket in it was warm.\n\n");
        t.append("I don't know how long it had been keeping house there. I know the last line in its book has "+(named?name+"'s name":"no name")+" on it, and that it says went home.");
        return t.toString();
    }
    public static ItemStack account(String reader,CompoundTag own){
        var pages=new ArrayList<Component>();
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,ledger(reader,own)));
        pages.addAll(styled(HouseWriting.WritingStyle.PLAIN,afterwards(reader,own)));
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.REPLACEMENT),reader,pages);
    }
}
