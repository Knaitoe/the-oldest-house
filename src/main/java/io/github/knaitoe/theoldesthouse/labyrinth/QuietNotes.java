package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.261, the notes in the rest rooms (docs/REST_ROOMS_0_4_261.md). Sixteen, left by people who stopped to rest before the reader,
 * read in order, one more on each rest (QuietRooms). The first is the old note from the quiet room's stand, kept word for word; the
 * rest answer it, and sometimes each other. Nobody here knows the reader, so nothing here says who the reader is.
 */
public final class QuietNotes {
    private QuietNotes(){}
    record Note(String title,String author,List<String> pages){}
    static final List<Note> NOTES=List.of(
        new Note("A place to stop","No name",List.of(
            "Sit down. Eat something. The corridor will still be there when you stand.\n\nTie the string at each door. Compare your marks with the chipped pillars, not with another mark.",
            "Let the animal listen before you follow. If it waits, catch up.\n\nI found a cat here. It was more frightened than I was.")),
        new Note("Bread","R.",List.of(
            "There were two loaves when I came through. I took one. If there's one left, it's yours.",
            "If there isn't, somebody was hungrier than both of us, and I hope it helped.")),
        new Note("Count","No name",List.of(
            "I count the doors. Forty-one since the kitchen, I think. I don't know which kitchen.\n\nIf you count too, put your number under mine.",
            "41\n17\n63, but I lost count twice\n9\n112\nI stopped counting")),
        new Note("Sleeping","M.",List.of(
            "You can sleep here. Nothing came while I slept.\n\nI put the chair against the door first.",
            "When I woke the chair was back where it had been, facing the room, and the door was shut the way I left it.\n\nI slept well. I don't know what that means.")),
        new Note("If you have an animal with you","No name",List.of(
            "Let it go through the doors first. Mine won't go through some of them. I've learned to listen to it.",
            "The doors it won't go through, I don't go through either, and I'm still here to write this.")),
        new Note("Going back","J.",List.of(
            "I went back the way I came for eleven doors and every room was different.",
            "On the twelfth it was the room I started in, as if it had been waiting for me to give up.\n\nDon't give up for that reason.")),
        new Note("List","No name",List.of(
            "matches\nstring, more of it\nsomething for my feet\nthe name of our street\na pencil that works\nbread")),
        new Note("My coat","No name",List.of(
            "I left my coat over the back of the chair. If it's still there and it fits you, keep it.",
            "If it's gone, somebody needed it more. I'm trying to think like that about everything in here.")),
        new Note("The quiet","No name",List.of(
            "This is the only room where I can't hear the house. Everywhere else there's something in the walls, like pipes, like breathing.",
            "Here, nothing.\n\nStay as long as you like. It doesn't count against you. I asked.")),
        new Note("What I'd tell myself","D.",List.of(
            "Mark the doors you've used. Chalk if you have it, string if you don't.\n\nEat when you find food, not when you're hungry.",
            "If a room is too nice, it isn't for you.\n\nThis one is all right.")),
        new Note("The photograph","No name",List.of(
            "There was a photograph in a drawer two rooms back. People round a table. I knew the table. I didn't know the people.",
            "I put it back face down. If you find it, put it back face down too.")),
        new Note("To whoever asked","No name",List.of(
            "To whoever wrote \"I asked\": who did you ask?",
            "I've been asking for days and nothing answers me. Write it here. I'll come back for it.")),
        new Note("Still asking","No name",List.of(
            "I don't think they came back. I think we all write that. I wrote it too.",
            "the one who asked who you asked")),
        new Note("The window","S.",List.of(
            "In the room before this one there was a window with nothing behind it but more wall.",
            "I put my hand on the glass and it was warm, as if somebody had just taken theirs away from the other side.\n\nIt didn't hurt. I'd still rather you didn't.")),
        new Note("Address","No name",List.of(
            "I keep writing the address on things so I don't lose it. It's on the back of this page.",
            "")),
        new Note("Leaving","No name",List.of(
            "I'm going on now. I've rested enough, which is a thing I didn't believe in before I came here.",
            "Leave the room the way you found it. Somebody after you will need the quiet more than you do.")));
    public static final int COUNT=NOTES.size();
    public static String title(int i){return NOTES.get(Math.floorMod(i,COUNT)).title();}
    public static ItemStack book(int i){var n=NOTES.get(Math.floorMod(i,COUNT));return HouseWriting.book(n.title(),n.author(),HouseWriting.WritingStyle.PLAIN,n.pages());}
    /** Every page, for the writing checks. */
    public static List<ItemStack> all(){var out=new java.util.ArrayList<ItemStack>();for(int i=0;i<COUNT;i++)out.add(book(i));return out;}
}
