package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthLoops;
import net.minecraft.core.BlockPos;

/** Client-only apparent hotel numbering for the local player. */
public final class HotelPlaqueClientState {
    private static BlockPos base;
    private static int laps;

    private HotelPlaqueClientState() {}

    public static void set(BlockPos hotelBase, int apparentLaps) {
        base = hotelBase.immutable();
        laps = apparentLaps;
    }

    public static int numberAt(BlockPos pos) {
        if (base == null) {
            return 0;
        }
        for (int k = 0; k <= 3; k++) {
            for (int side = 0; side <= 1; side++) {
                if (base.offset(LabyrinthLoops.signPos(k, side)).equals(pos)) {
                    return LabyrinthLoops.roomNumber(laps, k, side);
                }
            }
        }
        return 0;
    }
}
