package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Once a minute on the GameTest server, the heap in use and the chunks each level holds, so a
 * suite that runs out of memory shows whether it grew steadily or spiked in one batch.
 *
 * Does nothing outside the GameTest server.
 */
public final class SuiteMemory {
    private static final int EVERY_TICKS = 1200;

    private SuiteMemory() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (!(event.getServer() instanceof GameTestServer server) || server.getTickCount() % EVERY_TICKS != 0) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        StringBuilder chunks = new StringBuilder();
        for (ServerLevel level : server.getAllLevels()) {
            int loaded = level.getChunkSource().getLoadedChunksCount();
            if (loaded > 0) {
                chunks.append(' ').append(level.dimension().location().getPath()).append('=').append(loaded);
            }
        }
        TheOldestHouse.LOGGER.info("OTH suite memory at tick {}: {} MB used of {} MB; chunks{}", server.getTickCount(),
                (runtime.totalMemory() - runtime.freeMemory()) >> 20, runtime.maxMemory() >> 20, chunks);
    }
}
