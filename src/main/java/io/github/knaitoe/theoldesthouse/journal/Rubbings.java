package io.github.knaitoe.theoldesthouse.journal;

import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.BorrascaRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.DrownedTownRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganFinishBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.NovelRegistry;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The authored objects a reader can take a pencil rubbing of with their journal (JOURNAL_DESIGN.md, step 3): carved,
 * cut, cast or pressed things already standing in the rooms. Each gives a charcoal rubbing and one line about what the
 * paper caught. A rubbing is no reward, story step or advancement and never changes the scene; the art is drawn by
 * tools/draw_journal_art.py. Shared by the server, which places them, and the screen, which draws them.
 */
public final class Rubbings {
    public record Kind(String id, String observation, Predicate<BlockState> matches) {
        public ResourceLocation texture(){return ResourceLocation.fromNamespaceAndPath(io.github.knaitoe.theoldesthouse.TheOldestHouse.MOD_ID,"textures/gui/rubbing/"+id+".png");}
    }
    private Rubbings(){}

    public static final List<Kind> ALL=List.of(
        new Kind("gouges","Four grooves, evenly spaced. The fifth would be under the hand.",s->s.is(NovelRegistry.GOUGES.get())),
        new Kind("well_initials","Two pairs of initials. One pair has been touched so often it is nearly smooth.",s->s.is(NovelRegistry.CARVINGS.get())),
        new Kind("sealed_window","Painted shut, then painted again over the first coat. Nothing of the glass came through.",s->s.is(NovelRegistry.SEALED_WINDOW.get())),
        new Kind("scribble","Writing pressed so hard it went through the paper and into the plaster.",s->s.is(HouseBlocks.VIGNETTE_DETAIL.get())&&s.getValue(VignetteDetailBlock.KIND).name().startsWith("SCRIBBLE_")),
        new Kind("room_number","The number stands proud of its plate. One figure is worn smoother than the rest.",s->s.is(HouseBlocks.HOTEL_ROOM_PLAQUE.get())),
        new Kind("town_sign","The letters are cut deep. Paint has been laid into them more than once.",s->s.is(HouseBlocks.TOWN_SIGN.get())),
        new Kind("bark","Letters cut into bark. The tree has grown over the first of them.",s->s.is(BorrascaRegistry.CARVING.get())),
        new Kind("ward_notice","Raised letters on a plate. The screws at its corners are newer than the plate.",s->s.is(NovelRegistry.SIGN.get())),
        new Kind("pasted_paper","Paper pasted over paper. The top sheet lifts at one corner, and there is writing under it.",s->s.is(NovelRegistry.PAPER.get())),
        new Kind("funeral_plate","Raised letters on a plate. Someone has polished the name and nothing else.",s->s.is(LabyrinthRegistry.HARRIGAN_FINISH.get())&&s.getValue(HarriganFinishBlock.KIND)==HarriganFinishBlock.Kind.PLAQUE),
        new Kind("cave_marks","Notches in rows, cut and not worn. Someone was counting something down here.",s->s.is(LabyrinthRegistry.CAVE_MARKS.get())),
        new Kind("maker_mark","A maker's mark cast into the iron, and a dent at the height of a knee.",s->s.is(DrownedTownRegistry.STREETLIGHT.get())));

    /** The rubbing this block gives, or null. */
    public static Kind of(BlockState state){for(var k:ALL)if(k.matches().test(state))return k;return null;}
    public static Kind byId(String id){for(var k:ALL)if(k.id().equals(id))return k;return null;}
}
