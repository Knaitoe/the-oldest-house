package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 0.4.767: the place the reader at this screen stands in, looked up once for each block they move to. The weather of four
 * stories and the lake's sky each asked for it afresh whenever they were asked anything, and the game asks whether it rains
 * once for every column of rain it might draw, every frame: with rain overhead that was hundreds to thousands of lookups a
 * frame over every place in the House. The answer only changes when the reader moves a block, changes world, or the House moves.
 */
public final class HerePlace {
    private static Level level;
    private static BlockPos at, origin;
    @Nullable private static LabyrinthPlace place;
    private HerePlace() {}

    /** The place under the local player by the House's own origin, or null with no player, no world or no House. */
    @Nullable public static LabyrinthPlace get() {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !HouseSightlineState.hasOrigin()) return null;
        BlockPos o = HouseSightlineState.origin(), p = mc.player.blockPosition();
        if (level != mc.level || !p.equals(at) || !o.equals(origin)) {
            level = mc.level; at = p; origin = o;
            place = LabyrinthPlaces.placeAt(o, p);
        }
        return place;
    }
}
