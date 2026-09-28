package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

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

    /**
     * Sleeping in the manor ends the night. The House dimension shares the
     * Overworld's clock but cannot set it (vanilla skips the night by setting
     * the sleepers' own dimension's time, which does nothing outside the
     * Overworld), so without this, everyone in a manor bed was woken at night
     * and the night never passed.
     */
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(HouseDimensions.INTERIOR)) {
            return;
        }
        ServerLevel overworld = level.getServer().overworld();
        overworld.setDayTime(event.getNewTime());
        if (overworld.isRaining() && overworld.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            overworld.setWeatherParameters(0, 0, false, false);
        }
    }

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
