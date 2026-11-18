package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Nights and mornings, as the mod sees them.
 *
 * A morning wake is a player getting out of bed within the first
 * {@link #WAKE_WINDOW} ticks of a day (leaving a bed at night does not
 * count), in the Overworld or in one of the manor's beds. Days are counted
 * by {@link HouseCalendar}, which {@code /time set} cannot send backwards.
 */
public final class HouseDays {
    public static final long WAKE_WINDOW = 1500L;

    private HouseDays() {
    }

    /*
     * 0.4.703: sleeping in the manor no longer ends the night by itself. The House used to pass its own night and carry the morning to
     * the Overworld (the House shares the Overworld's clock but cannot set it); now a sleeper in the House counts beside the Overworld's
     * players and the Overworld alone passes the night for both (HouseSleep).
     */

    /** Whether this wake-up ends a night's sleep. */
    public static boolean isMorningWake(ServerPlayer player) {
        return (player.serverLevel().dimension().equals(Level.OVERWORLD) || isInManor(player))
                && HouseCalendar.timeOfDay(player.server) <= WAKE_WINDOW;
    }

    /** In the House dimension, inside the manor itself (not past the threshold). */
    public static boolean isInManor(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) {
            return false;
        }
        HouseSavedData data = HouseSavedData.get(player.server);
        BlockPos origin = data.houseOrigin();
        return origin != null && HouseLayout.isInsideDomesticVolume(
                player.getX() - origin.getX(),
                player.getY() - origin.getY(),
                player.getZ() - origin.getZ()
        );
    }
}
