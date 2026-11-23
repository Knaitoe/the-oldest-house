package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * 0.4.754, the cold room, part 1 (docs/COLD_ROOM_0_4_754.md): what the house's things say to a hand, what its three people say, and the
 * letter on the hall table, left by the priest for whoever he sent for. Nothing in the house speaks for itself yet. Every line is new in
 * this release and is flagged to the owner in docs/COLD_ROOM_0_4_754.md; no name, line or date is taken from any book or film.
 */
public final class ColdTexts {
    private ColdTexts(){}
    public static final String HINT="There is a letter on the hall table.";
    public static final String HALL_TABLE="A narrow table by the door, and a bowl on it for keys. There are no keys in it.",
        HOOKS="Coats on their hooks: a long one with snow still on its shoulders, and a small one that has not been out this winter.",
        KITCHEN_TABLE="The kitchen table. A cup of coffee gone cold, a full ashtray, and a list of doctors with every name crossed out.",
        STOVE="Every ring on the stove is lit. The kitchen is cold all the same.",
        SINK="The sink is full of cups, rinsed and left upside down.",
        CHAIR="A kitchen chair.",
        BEDSIDE_CHAIR="A kitchen chair, carried up the stairs and set by the bed. The seat is still warm.",
        BED="An iron bed. The straps are buckled to the frame on both sides. The sheets are damp, and very cold.",
        PILLOW="The pillow is damp through.",
        DRESSER="A chest of drawers. The drawers are shut.",
        DRESSER_OUT="Every drawer of the chest is pulled out, each to the same length.",
        BOOK="A small worn book with a ribbon in it, shut. The priest has not opened it yet.",
        CRUCIFIX="A cross on a nail over the bed.",
        CRUCIFIX_TURNED="The cross over the bed hangs upside down on its nail.",
        LAMP_TABLE="A bedside table with a lamp. The lamp is the only warm thing in the room.",
        FLASK_TABLE="A bedside table with a flask on it, stoppered with a cork. The water inside has not frozen.",
        RADIATOR="The radiator under the window ticks as it heats. It is cold to the touch.",
        BULB="A bare bulb on its flex.",
        STREETLAMP="The lamp at the foot of the steps.",
        WINDOW="Frost at the edges of the glass. Below the window the steps fall away down the hill, a long way, to a lamp at the bottom.";
    /** What a hand finds on each of the three, in the order of ColdRoom's cast. */
    private static final String[] TOUCH={
        "She does not look up from the table. \"He said somebody would come,\" she says. \"Go on up. I can't go up there again tonight.\"",
        "She is asleep, or something close to it. Her wrists are in the straps. Her breath stands in the air over the bed and does not go.",
        "He does not turn from the window. \"Not yet,\" he says. \"When it's time, I'll call you. Keep your coat on.\""};
    public static String touch(int i){return i>=0&&i<TOUCH.length?TOUCH[i]:TOUCH[0];}
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the priest's letter, left on the hall table for whoever he sent for. */
    public static ItemStack source(){
        return HouseWriting.book("A letter on the hall table","T. Aske",pages(
            "To whoever comes tonight.\n\nI asked for two, and word came back that somebody would be sent. I take it that is you. Let yourself in. The door does not lock.",
            "The mother is in the kitchen. Let her stay there. She has been up and down those stairs for eleven nights, and she would tell you all of it if you asked. Do not ask.",
            "Her daughter is in the back bedroom. The radiator does nothing. Keep your coat on.",
            "What talks in that room will know you. It will say things you have never told anybody. It did not learn them anywhere you could follow, so do not try.",
            "I have the book. When you come up, kneel where I can see you, and read when I read. Whatever it says to you, it says so that you will stop.",
            "I have done this once before, a long time ago, and it cost more than I was told it would. I am not counting on being lucky twice.\n\nT. Aske"));
    }
}
