package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;

/**
 * 0.4.621, A Little Longer (docs/A_LITTLE_LONGER_PLAN.md): the work order (the first paper), and what each of the shop's and the
 * flat's things says to a hand. Nothing here is Rowan's speech; that comes with Rowan. Every line is new in this release and is
 * flagged to the owner in docs/A_LITTLE_LONGER_0_4_621.md.
 */
public final class LongerTexts {
    private LongerTexts(){}
    public static final String HINT="There is a work order on the spike over the bench.";
    public static final String LAMP_ON="The lamp comes on. Its shade has been mended with tape, carefully.",
        LAMP_OFF="The lamp goes off. The bulb ticks as it cools.",
        BENCH_RADIO="A table set with its back off. Somebody has been in it with a screwdriver tonight; the screws are in a saucer, all but one.",
        FLAT_RADIO_ON="The kitchen radio clicks and warms up into a hiss that never finds a station.",
        FLAT_RADIO_OFF="The hiss stops.",
        TRAYS="Parts trays, every compartment labelled in the same pencil: valves, knobs, grub screws, \"odd\".",
        CHAIR="The chair's front leg has been mended twice: once with a splint, once with a better splint.",
        SHELF="Sets waiting their turn, each with a tag tied to the dial. Most of the tags give a day of the week and nothing else.",
        COUNTER="The counter has been wiped so often the varnish has gone in a pale oval where elbows go.",
        FRONT_DOOR="The shop door will not open. Through its glass the street goes on in the rain: a lamp, a curb, the far pavement, nobody.",
        WINDOW="Rain on the glass. Beyond it the street is exactly where it was.",
        FLAT_WINDOW="Rain runs down the window. The same lamp, the same curb. Nothing goes by.",
        KETTLE="A heavy old kettle, the kind somebody brings with them from one job to the next.",
        CUPS="Two cups that were never a pair. One has a crack down from the rim that has been glued.",
        TOASTER_DOWN="The toaster's lever goes down and stays down. Nothing is in it yet.",
        TOASTER_UP="The toaster gives the lever back with a clack.",
        TABLE="A small table under the window, wiped down, with room for two and no more.",
        ARMCHAIR="The armchair by the table, the good chair, sat into the shape of one person.",
        KITCHEN_CHAIR="A kitchen chair, hard, set where the draught from the window comes in.",
        HOOK="A coat on the hook at the top of the stairs, its pockets weighed down with small things.",
        WORKTOP="The worktop is clear except for crumbs nobody has got round to.",
        SINK="One plate in the rack, one fork, one knife.",
        BASIN="A toothbrush, a tin of tooth powder, a razor on the shelf. A towel folded over the edge to dry.",
        LAVATORY="An ordinary bathroom, cleaner than the rest of the flat.",
        BENCH="A bench under the awning, dry. From here a scrap of sky shows between the tops of the buildings.",
        TANK="A water tank on its stand, rain drumming on its lid.",
        AWNING="Corrugated sheet on a frame, rain running off its front edge in a line.",
        ORDER="A work order on the spike.";
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the unfinished work order on the spike over the bench, in Rowan's pencil. */
    public static ItemStack source(){
        return HouseWriting.book("Work order","R.",pages(
            "WORK ORDER\n\nSet: table radio, walnut case.\nFault: hums. Will not hold a station.\nPromised: Friday.",
            "Valves tested, all good. Back panel off. One screw short; it went somewhere.\n\nIt finds something late at night and then loses it again. Not interference. Not the aerial.",
            "Shop closed until further notice.\n\nR."));
    }
    /** The account. Nothing asks for it until the evening can end; it says only what anybody could see of the place. */
    public static ItemStack account(String name,CompoundTag own){
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.LITTLE_LONGER),name,pages(
            "There was a radio repair shop with a flat over it, and rain on every window, and a street outside that nobody walked along."));
    }
}
