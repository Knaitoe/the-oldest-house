package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.549, The Ones Already Inside: what is written. The story is the owner's own (docs/THE_ONES_ALREADY_INSIDE_PROPOSAL.md); every
 * line here is new. Nothing here says what the reader is like, how they feel, or what they are called.
 */
public final class LodgingTexts {
    private LodgingTexts(){}
    public static final String HINT="The terms of the house are on the hall table.";
    public static final String FRAME="A doorcase set into the outside wall. Behind it there is only plaster, a hand's depth back, and the plaster is warm.",
        DANIELS_FRAME="A doorcase in the back wall. The plaster inside it is worn smooth at the height of a pair of shoulders.",
        SHUTTERS="The shutters are screwed shut from this side. The screws are painted over.",
        GLASS="The hall glass. It is a moment late, or the light in here is.",
        GRATE="The grate is laid and has never been lit. The coal is painted.",
        RATCHET="An iron wheel with a pawl in its teeth, and a lever to lift the pawl. A chain goes up from it into the floor above.",
        POST="A post under one of the rooms upstairs, its foot in an iron shoe. A wedge is driven in beside it.",
        DEAD_POST="A post with its own shoe and wedge, standing on the bare floor. Nothing above it. Rope burns go round it at the height of a pair of wrists.",
        TALLY="Marks in fives, scratched with something blunt. Then marks that are not in fives. Then marks that are only lines.",
        BOLT_DRAWN="The bolt is on this side. It slides back easily; it is used.";
    // 0.4.550: what is in the rooms.
    public static final String BED="A bed, made and turned down for the night.",
        BED_VERA="A narrow bed, made so tight the blanket does not crease.",
        WASHSTAND="A basin, a jug of water and a glass. The water is fresh.",
        CHAIR="A plain chair.",
        GUEST_CHAIR="A hard chair, set close to the bed's head.",
        DANIEL_CHAIR="A plain chair, turned to face the doorcase in the back wall. The boards under it are worn in arcs where it has been turned on the one spot.",
        ARMCHAIR="An armchair, its arms worn smooth where hands have rested.",
        SETTEE="A settee for two, sunk in the middle.",
        SIDE_TABLE="A small table with a ring on it where a cup has stood.",
        LONG_TABLE="The long table with all its leaves in. In front of each chair the polish is worn where a plate goes.",
        SIDEBOARD="A sideboard. Its drawers hold cutlery laid in rows, every place the same.",
        COT="A narrow cot. The mattress is worn through in one place, and the blanket at its foot is folded as square as it will go.",
        SHELF="A shelf the height of a tray, under the hatch. Tin plates on it, washed and stacked, and a cup with its handle mended.",
        PAIL="A pail with a lid, scrubbed.",
        SCULLERY_PAIL="A pail of clean water.",
        RANGE="The range is in, low. The kettle on it is warm.",
        FLUE="The range's flue. It is warm to the hand.",
        SINK="A stone sink, worn hollow in the middle.",
        DRESSER="A dresser with its plates standing in the rack, all of them alike.",
        TRAY="A tray laid for one: a tin plate, a tin cup, a spoon. Nothing on the plate yet.",
        TABLE="A scrubbed table.",
        PREPARATIONS="Cloths folded in squares, a basin, thread wound on cards, and a roll of oilcloth tied shut. Everything is clean, and nothing has been put away.",
        RECORD="A cabinet of little drawers, each full of cards. Every card has a room number and a date on it in the same hand; most have a second date under the first. None has a name.",
        KEY="A hook by the door with a key on it, long and plain. Its tag is blank.",
        KEY_GONE="An empty hook by the door. The paint is worn under it where something has hung a long time.",
        CUPBOARD="A tall cupboard, locked.",
        WARDROBE="A wardrobe, locked. It smells of camphor.",
        DRESSING="A brush and a dish of pins before the glass. Nothing else.",
        CARD_HOLDER="A brass holder beside the door, for a card. Empty.",
        ROPE="A coil of rope, its end cut, not frayed.",
        CRATE="A crate, nailed shut.",
        COAL="A heap of coal.";
    /** What a thing in the rooms says to a hand, by what it is and where it stands. */
    public static String thing(BlockState s,BlockPos rel){
        var r=Vec3.atCenterOf(rel);int stage=s.getValue(LodgingThing.STAGE);
        return switch(s.getValue(LodgingThing.KIND)){
            case BED->stage>=2?BED_VERA:BED;
            case COT->COT;
            case WASHSTAND->WASHSTAND;
            case CHAIR->LodgingSite.inDaniels(r)?DANIEL_CHAIR:LodgingSite.guestRoom(r)>=0?GUEST_CHAIR:CHAIR;
            case ARMCHAIR->ARMCHAIR;
            case SETTEE->SETTEE;
            case SIDE_TABLE->SIDE_TABLE;
            case LONG_TABLE->LONG_TABLE;
            case SIDEBOARD->SIDEBOARD;
            case WORK_TABLE->PREPARATIONS;
            case KEY_HOOK->stage==0?KEY:KEY_GONE;
            case CARD_INDEX->RECORD;
            case WARDROBE->LodgingSite.inWorkroom(r)?CUPBOARD:WARDROBE;
            case DRESSING_TABLE->DRESSING;
            case CARD_HOLDER->CARD_HOLDER;
            case SINK->SINK;
            case RANGE->stage==0?RANGE:FLUE;
            case DRESSER->DRESSER;
            case SCULLERY_TABLE->(stage&2)!=0?TRAY:TABLE;
            case SHELF->SHELF;
            case PAIL->LodgingSite.inDaniels(r)?PAIL:SCULLERY_PAIL;
            case ROPE->ROPE;
            case CRATE->CRATE;
            case COAL->COAL;
        };
    }
    private static List<Component> pages(String... texts){var out=new ArrayList<Component>();for(String text:texts)for(String p:LookoutTexts.pages(text,150))out.add(HouseWriting.page(HouseWriting.WritingStyle.PLAIN,p));return out;}
    /** The first paper: the terms of the house, printed on a card, with additions in ink. */
    public static ItemStack source(){
        return HouseWriting.book("Terms of the house","V.",pages(
            "TERMS\n\nRooms by the week. A week is seven nights.\n\nGuests are asked to keep to their own rooms after eleven.\n\nThe hatches are for the house's use.",
            "Do not wake the sleeper in your room. It is not yours yet, and it is not used to being looked at.\n\nNothing in this house is to be touched that is not your own. You will find that easy.",
            "Anyone who is tired may rest. Anyone who rests will be looked after, and missed by nobody.\n\nThe house does not keep a register. The house remembers.\n\nV."));
    }
    /**
     * The account. This slice the story cannot be finished, so nothing asks for this; it says only what a reader could have seen of the
     * house, and later slices write the rest from the reader's own record.
     */
    public static ItemStack account(String name,CompoundTag own){
        var p=new ArrayList<String>();
        p.add("There was a lodging house with its shutters screwed shut, and a card of terms in the hall.");
        if(own.getBoolean("WentUp"))p.add("Upstairs every room had a number, and every outside wall had a doorway in it that went nowhere.");
        if(own.getBoolean("WentDown"))p.add("Under the house the whole of it stood on posts, and the posts stood on a beam, and the beam was held by one wheel.");
        return HouseWriting.book(LiteraryTexts.accountTitle(LabyrinthPlace.ONES_INSIDE),name,pages(String.join("\n\n",p)));
    }
}
