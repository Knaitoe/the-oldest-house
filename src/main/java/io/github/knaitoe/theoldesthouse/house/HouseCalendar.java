package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The mod's own count of days: every dawn the world passes, however it gets
 * there.
 *
 * The world's day number (day time / 24000) cannot be trusted for this.
 * {@code /time set} writes an absolute time, so {@code /time set night}
 * sends the world back to day 0 and anything counted in days (nights slept,
 * days since joining, perceived House age) silently stops advancing. This
 * counter only ever goes forward: it advances whenever the world's day
 * number rises, and by one whenever the time of day jumps back (the clock
 * was set from evening to morning, which is a new dawn as far as anyone in
 * the world can tell).
 *
 * Existing worlds start counting from the world's own day number, so days
 * already stored by earlier versions stay comparable.
 */
public final class HouseCalendar extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_calendar";
    public static final long TICKS_PER_DAY = 24000L;

    private long day = -1L;
    private long lastDayTime;

    public static final Factory<HouseCalendar> FACTORY = new Factory<>(HouseCalendar::new, HouseCalendar::load);

    public HouseCalendar() {
    }

    private static HouseCalendar load(CompoundTag tag, HolderLookup.Provider registries) {
        HouseCalendar calendar = new HouseCalendar();
        calendar.day = tag.contains("Day") ? tag.getLong("Day") : -1L;
        calendar.lastDayTime = tag.getLong("LastDayTime");
        return calendar;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("Day", day);
        tag.putLong("LastDayTime", lastDayTime);
        return tag;
    }

    public static HouseCalendar get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    /** Today, as the mod counts days. Catches up with the world clock first. */
    public static long today(MinecraftServer server) {
        HouseCalendar calendar = get(server);
        calendar.sync(server.overworld().getDayTime());
        return calendar.day;
    }

    /** Ticks since the last dawn, from the world clock. */
    public static long timeOfDay(MinecraftServer server) {
        return Math.floorMod(server.overworld().getDayTime(), TICKS_PER_DAY);
    }

    void sync(long dayTime) {
        if (day < 0L) {
            day = Math.max(0L, Math.floorDiv(dayTime, TICKS_PER_DAY));
            lastDayTime = dayTime;
            setDirty();
            return;
        }
        if (dayTime == lastDayTime) {
            return;
        }
        day += dawnsBetween(lastDayTime, dayTime);
        lastDayTime = dayTime;
        setDirty();
    }

    /**
     * How many dawns lie between two readings of the world clock. Forward
     * across day boundaries counts each one; any jump back to an earlier time
     * of day counts as one new dawn; anything else is none.
     */
    public static long dawnsBetween(long before, long after) {
        long days = Math.floorDiv(after, TICKS_PER_DAY) - Math.floorDiv(before, TICKS_PER_DAY);
        if (days > 0L) {
            return days;
        }
        return Math.floorMod(after, TICKS_PER_DAY) < Math.floorMod(before, TICKS_PER_DAY) ? 1L : 0L;
    }
}
