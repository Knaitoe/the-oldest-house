package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.663, Not Yet, part 1 (docs/NOT_YET_0_4_663.md): what the ward's things say to a hand, and the chart by the door in the machine's own
 * hand. The machine is the Kept's (KeptTexts); it writes in capitals, as it does there. Nobody speaks yet. Every line is new in this release
 * and is flagged to the owner in docs/NOT_YET_0_4_663.md.
 */
public final class NotYetTexts {
    private NotYetTexts(){}
    public static final String HINT="There is a chart on the wall by the door, in the machine's hand.";
    public static final String COT="A cot made up tight. The sheet is turned back exactly the width of a hand.",
        COT_FOOT="The foot of the cot. The blanket is tucked in so hard it would take both hands to pull it loose.",
        CURTAIN="A curtain on a rail of cable. It is heavier than cloth and does not quite hang straight.",
        LAMP="A lamp on a jointed arm, bent down over the pillow as if to look.",
        LAMP_ON="A lamp on a jointed arm. It is on, and bent down close over the pillow.",
        SCREEN="A screen at the head of the cot. It is dark, but something behind it hums.",
        COUNTER="A wall of turning digits, seven to a row. Every one of them stands at nought.",
        TABLE="A table under a ring of arms. The top is warm, and worn into a hollow the length of a body.",
        ARM="An arm folded up under the ceiling. Its fingers are many and fine, like a watchmaker's tools.",
        HATCH="A plate in the floor, set a little apart from the plates round it. It rings hollow.",
        VENT="A grille low in the wall. Cold comes through it, and a smell of nothing at all.",
        WINDOW="A long window onto a field under a sun. The sun has not moved. The clouds are painted on.",
        DOOR="The far door. It has no handle on this side, only a seam down the middle.",
        DOOR_OPEN="The far door stands open.",
        INES="She lies on her back with her eyes open, looking at the ceiling. A seam runs across her brow. She breathes, and nothing else of her moves.",
        BRAM="He lies the same way, his hands at his sides. There is a seam round each wrist, and another round each arm below the elbow.",
        ODILE="Her eyes are open and follow nothing. The seam along her hairline has been sewn more than once.";
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the ward's chart by the door. The beds are counted from the place as it is built (NotYetSite): fifteen in the ward, six in the long room. */
    public static ItemStack source(){
        return HouseWriting.book("The ward's chart","The machine",pages(
            "WARD.\n\nBEDS: TWENTY-ONE.\nKEPT: THREE.\nGOT UP: ALL THE REST.",
            "THIS IS WHERE I MEND WHAT I BREAK.\n\nI BREAK A GREAT DEAL. I HAVE BECOME VERY GOOD AT MENDING.",
            "NOBODY DIES HERE. I HAVE SEEN TO IT.",
            "BED ONE IS MADE.\n\nI HAVE PUT A NAME ON IT IN PENCIL. I WILL WRITE IT IN INK WHEN YOU LIE DOWN."));
    }
    /** The account. Nothing asks for it until part 4; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.NOT_YET),name,pages(
            "There was a ward deep in the machine, with cots in rows under lamps, and a window onto a field where the sun did not move. "
                +"A wall of digits stood at nought. Beyond it, in a long room, three people lay with their eyes open."));
    }
}
