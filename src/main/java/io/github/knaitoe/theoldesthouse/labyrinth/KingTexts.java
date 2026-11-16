package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;

/**
 * 0.4.641, A sort of thing, part 1 (docs/SLEEPING_KING_PLAN.md): what each of the drawing-room's things and the country's says to a
 * hand. Nobody speaks yet. Every line is new in this release and is flagged to the owner in docs/SLEEPING_KING_0_4_641.md.
 */
public final class KingTexts {
    private KingTexts(){}
    public static final String HINT="There is a chess problem on the little table, printed small.";
    public static final String MANTEL="The fire is real enough to warm a hand. The marble over it has a crack, mended with something that has gone yellow.",
        GLASS="The glass shows the room the other way round. Near the bottom it is not quite glass any more, more like a mist, and it gives under the hand.",
        GLASS_BACK="The glass shows the room the other way round, the way you came.",
        RUG="A hearthrug, worn bald where feet have rested on it for years.",
        CHESSMEN="Chessmen in the hearth among the cinders, as if the game had been knocked over and nobody had picked it up. One king lies on his side.",
        TABLE="A little round table, polished until the grain shows like water.",
        PROBLEM_BACK="The type runs the wrong way. Held up to the glass, it might read.",
        PROBLEM="Here the type reads the right way round: a chess problem, and under it, \"White to play, and win in eleven moves.\"",
        ARMCHAIR="A deep armchair turned toward the fire. The seat is still a little warm.",
        WINDOW="Through the glass the country lies in squares, as far as the eye goes.",
        WINDOW_DRAWING="Rain has been on this glass. Beyond it the garden is the ordinary sort.",
        KING="An old man asleep under the tree, in a heap, in a long robe and a tall night-cap with a tassel. He is snoring.",
        RULED="Here the ground is only drawn: an outline on paper, and a little hatching where the shadow would be.";
    /** 0.4.642: a line who goes off the country is nowhere, and is put back on the last square that counted them. */
    public static final String NOWHERE="The ground comes back underfoot, on a square stood on before.";
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the chess problem on the little table, printed the wrong way round. Behind the glass it reads. */
    public static ItemStack source(){
        return HouseWriting.book("A chess problem","",pages(
            ".sevom nevele ni niw dna ,yalp ot etihW\n\nnwap etihw a sa nigeb uoY",
            "The diagram above the words is a board of eight squares by eight, with the pieces drawn on it as little pictures. "
                +"Somebody has drawn a line in pencil from the second square up through the board to the eighth, and stopped."));
    }
    /** The account. Nothing asks for it until part 3; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.SLEEPING_KING),name,pages(
            "There was a drawing-room with a fire in it, and a glass over the fire, and the same room on the other side of the glass. "
                +"Beyond it the country lay in squares, and an old man was asleep under a tree."));
    }
}
