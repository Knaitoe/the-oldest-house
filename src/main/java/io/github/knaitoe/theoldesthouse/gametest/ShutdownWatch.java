package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * When the GameTest server is still shutting down long after its tests are
 * done, log what Minecraft's last chunk wait (before "Saving chunks for
 * level") is still waiting on: the tickets, the chunks that will not unload,
 * the generation still under way. CI once stalled there intermittently, and
 * a thread dump only shows that the server thread is going round that loop,
 * not why. The report runs on the server thread itself: the loop polls
 * queued tasks between its turns, so nothing is read while it is changing.
 *
 * Does nothing outside the GameTest server.
 */
public final class ShutdownWatch {
    private static final long[] REPORT_AFTER_MS = {20_000L, 50_000L};
    private static final int SHOWN = 12;
    private static volatile boolean stopped;

    private ShutdownWatch() {
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        if (!(event.getServer() instanceof GameTestServer)) {
            return;
        }
        MinecraftServer server = event.getServer();
        stopped = false;
        Thread watch = new Thread(() -> watch(server), "The Oldest House shutdown watch");
        watch.setDaemon(true);
        watch.start();
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        stopped = true;
    }

    private static void watch(MinecraftServer server) {
        long waited = 0L;
        for (long at : REPORT_AFTER_MS) {
            try {
                Thread.sleep(at - waited);
            } catch (InterruptedException e) {
                return;
            }
            waited = at;
            if (stopped) {
                return;
            }
            CountDownLatch reported = new CountDownLatch(1);
            server.execute(() -> {
                if (server.isSameThread()) {
                    report(server, at);
                }
                reported.countDown();
            });
            try {
                if (!reported.await(5L, TimeUnit.SECONDS) && !stopped) {
                    TheOldestHouse.LOGGER.warn("OTH shutdown watch: still stopping after {}s, and the server thread is not "
                            + "taking tasks: it is blocked, not looping. See the thread dump.", at / 1000L);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static void report(MinecraftServer server, long after) {
        TheOldestHouse.LOGGER.warn("OTH shutdown watch: still stopping after {}s", after / 1000L);
        for (ServerLevel level : server.getAllLevels()) {
            try {
                reportLevel(level);
            } catch (RuntimeException e) {
                TheOldestHouse.LOGGER.warn("OTH shutdown watch: could not read {}", level.dimension().location(), e);
            }
        }
    }

    private static void reportLevel(ServerLevel level) {
        ChunkMap map = level.getChunkSource().chunkMap;
        if (!map.hasWork()) {
            return;
        }
        Object tickets = read(read(map, "distanceManager"), "tickets");
        Object updating = read(map, "updatingChunkMap");
        Object pendingUnloads = read(map, "pendingUnloads");
        Object toDrop = read(map, "toDrop");
        Object tasks = read(map, "pendingGenerationTasks");
        TheOldestHouse.LOGGER.warn("OTH shutdown watch: {} has chunk work: light={} poi={} sorter={} tickets={} updating={} "
                        + "pendingUnloads={} toDrop={} unloadQueue={} generationTasks={}",
                level.dimension().location(),
                level.getChunkSource().getLightEngine().hasLightWork(),
                call(read(map, "poiManager"), "hasWork"),
                call(read(map, "queueSorter"), "hasWork"),
                size(tickets), size(updating), size(pendingUnloads), size(toDrop), size(read(map, "unloadQueue")), size(tasks));

        int shown = 0;
        if (tickets instanceof Map<?, ?> byChunk) {
            for (Map.Entry<?, ?> entry : byChunk.entrySet()) {
                if (shown++ == SHOWN) {
                    break;
                }
                TheOldestHouse.LOGGER.warn("  ticket at {}: {}", chunk(entry.getKey()), entry.getValue());
            }
        }
        shown = 0;
        if (pendingUnloads instanceof Map<?, ?> byChunk) {
            for (Object holder : byChunk.values()) {
                if (shown++ == SHOWN) {
                    break;
                }
                TheOldestHouse.LOGGER.warn("  unloading {}", holder(holder));
            }
        }
        shown = 0;
        if (toDrop instanceof Collection<?> dropping && updating instanceof Map<?, ?> byChunk) {
            for (Object key : dropping) {
                Object holder = byChunk.get(key);
                if (holder != null && shown++ < SHOWN) {
                    TheOldestHouse.LOGGER.warn("  kept from unloading {}", holder(holder));
                }
            }
        }
        shown = 0;
        if (tasks instanceof Collection<?> generating) {
            for (Object task : generating) {
                if (shown++ == SHOWN) {
                    break;
                }
                TheOldestHouse.LOGGER.warn("  generating {} to {} (scheduled {}, cancelled {})", read(task, "pos"),
                        read(task, "targetStatus"), read(task, "scheduledStatus"), read(task, "markedForCancellation"));
            }
        }
    }

    /** The tickets held on one chunk, as the level's distance manager lists them ("null" for none). */
    static String ticketsAt(ServerLevel level, ChunkPos chunk) {
        Object tickets = read(read(level.getChunkSource().chunkMap, "distanceManager"), "tickets");
        return tickets instanceof Map<?, ?> byChunk ? String.valueOf(byChunk.get(chunk.toLong())) : String.valueOf(tickets);
    }

    private static String holder(Object holder) {
        return call(holder, "getPos") + " level " + call(holder, "getTicketLevel") + " generation refs "
                + call(holder, "getGenerationRefCount") + " status " + call(holder, "getLatestStatus");
    }

    private static String chunk(Object key) {
        return key instanceof Long packed ? new ChunkPos(packed).toString() : String.valueOf(key);
    }

    private static String size(Object value) {
        if (value instanceof Map<?, ?> map) {
            return String.valueOf(map.size());
        }
        if (value instanceof Collection<?> collection) {
            return String.valueOf(collection.size());
        }
        return String.valueOf(value);
    }

    /** A field by its Mojang name, looked up through the superclasses. */
    private static Object read(Object target, String name) {
        if (target == null) {
            return null;
        }
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                // Declared further up.
            } catch (ReflectiveOperationException | RuntimeException e) {
                return "<" + e + ">";
            }
        }
        return "<no " + name + ">";
    }

    /** A no-argument method by its Mojang name, looked up through the superclasses. */
    private static Object call(Object target, String name) {
        if (target == null) {
            return null;
        }
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Method method = type.getDeclaredMethod(name);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (NoSuchMethodException e) {
                // Declared further up.
            } catch (ReflectiveOperationException | RuntimeException e) {
                return "<" + e + ">";
            }
        }
        return "<no " + name + "()>";
    }
}
