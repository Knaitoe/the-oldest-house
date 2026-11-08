package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Once a minute on the GameTest server, the heap in use and the chunks each level holds, so a
 * suite that runs out of memory shows whether it grew steadily or spiked in one batch.
 *
 * Between those reports, a level whose loaded chunks pass {@value #SPIKE} (and each further {@value #SPIKE_STEP}) is reported at
 * once, so a spike shows the batch it came in (run 1481: eighteen staircase fixtures at once held some 22,000).
 *
 * Does nothing outside the GameTest server.
 */
public final class SuiteMemory {
    private static final int EVERY_TICKS = 1200;
    private static final int SPIKE = 10000, SPIKE_STEP = 4000;
    private static final java.util.Map<String, Integer> REPORTED = new java.util.HashMap<>();

    private SuiteMemory() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (!(event.getServer() instanceof GameTestServer server)) {
            return;
        }
        if (server.getTickCount() % 20 == 0) {
            spikes(server);
        }
        if (server.getTickCount() % EVERY_TICKS != 0) {
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

    private static void spikes(GameTestServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            String name = level.dimension().location().getPath();
            int loaded = level.getChunkSource().getLoadedChunksCount();
            int reported = REPORTED.getOrDefault(name, 0);
            if (loaded < SPIKE) {
                if (reported > 0 && loaded < SPIKE / 2) REPORTED.remove(name);
                continue;
            }
            if (loaded < Math.max(SPIKE, reported + SPIKE_STEP)) continue;
            REPORTED.put(name, loaded);
            TheOldestHouse.LOGGER.info("OTH suite chunk spike at tick {}: {}={}", server.getTickCount(), name, loaded);
        }
    }
}
