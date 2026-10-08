package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
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
 * When the thread is held inside one turn and never polls, the report is read
 * from the watch thread instead, with every other busy thread's stack.
 *
 * That held turn is vanilla's unload loop (ChunkMap.processUnloads), which at
 * shutdown runs with unlimited time: an unload whose chunk still lends itself
 * to a neighbour's generation reschedules itself there, and the generation
 * needs that same thread's turn to finish, so the loop never ends. Fixtures
 * that load a chunk area and release it at once can leave such generation
 * under way when the last test passes. So before the shutdown begins, the
 * server thread first runs its chunk tasks until no generation is left
 * (bounded; shutdown then goes on exactly as before).
 *
 * Does nothing outside the GameTest server.
 */
public final class ShutdownWatch {
    private static final long[] REPORT_AFTER_MS = {20_000L, 50_000L, 120_000L};
    private static final int SHOWN = 12;
    private static final long SETTLE_SECONDS = 30L;
    private static volatile boolean stopped;

    private ShutdownWatch() {
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        if (!(event.getServer() instanceof GameTestServer)) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server.isSameThread()) {
            settle(server);
        }
        stopped = false;
        Thread watch = new Thread(() -> watch(server), "The Oldest House shutdown watch");
        watch.setDaemon(true);
        watch.start();
    }

    /** Runs the levels' chunk tasks until no chunk generation is under way, or for at most thirty seconds. */
    private static void settle(MinecraftServer server) {
        long started = System.nanoTime();
        long deadline = started + TimeUnit.SECONDS.toNanos(SETTLE_SECONDS);
        int before = generating(server);
        int left = before;
        while (left > 0 && System.nanoTime() < deadline) {
            boolean ran = false;
            for (ServerLevel level : server.getAllLevels()) {
                for (int i = 0; i < 4096 && level.getChunkSource().pollTask(); i++) {
                    ran = true;
                }
            }
            if (!ran) {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(2));
            }
            left = generating(server);
        }
        if (before > 0) {
            TheOldestHouse.LOGGER.info("OTH shutdown watch: {} chunks were still generating as the tests ended; {} after {} ms",
                    before, left == 0 ? "all settled" : left + " still are", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
        }
    }

    /** Generation tasks queued, plus chunks still lending themselves to a neighbour's generation. */
    private static int generating(MinecraftServer server) {
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            ChunkMap map = level.getChunkSource().chunkMap;
            if (read(map, "pendingGenerationTasks") instanceof Collection<?> tasks) {
                count += tasks.size();
            }
            if (read(map, "updatingChunkMap") instanceof Map<?, ?> holders) {
                for (Object holder : holders.values()) {
                    if (call(holder, "getGenerationRefCount") instanceof Integer refs && refs > 0) {
                        count++;
                    }
                }
            }
        }
        return count;
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
            stack(server, at);
            // The server is already marked stopped while it shuts down, so execute() would run the
            // report here, off its thread. Queue it instead: the shutdown loop drains queued tasks.
            CountDownLatch reported = new CountDownLatch(1);
            server.tell(new TickTask(server.getTickCount(), () -> {
                report(server, at);
                reported.countDown();
            }));
            try {
                if (!reported.await(5L, TimeUnit.SECONDS) && !stopped) {
                    TheOldestHouse.LOGGER.warn("OTH shutdown watch: still stopping after {}s, and the server thread is not "
                            + "taking tasks: it is held inside one turn of its loop (stack above). Reading its chunk maps "
                            + "from this thread instead, so counts may be a moment apart.", at / 1000L);
                    report(server, at);
                    others(server);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /** Where the server thread is right now, read from this thread. */
    private static void stack(MinecraftServer server, long after) {
        Thread thread = server.getRunningThread();
        StringBuilder trace = new StringBuilder();
        for (StackTraceElement frame : thread.getStackTrace()) {
            trace.append("\n    at ").append(frame);
        }
        TheOldestHouse.LOGGER.warn("OTH shutdown watch: server thread after {}s is {}:{}", after / 1000L, thread.getState(), trace);
    }

    /** Every other live thread in the server's JVM that is doing something, and where. */
    private static void others(MinecraftServer server) {
        Thread main = server.getRunningThread();
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] frames = entry.getValue();
            if (thread == main || thread == Thread.currentThread() || frames.length == 0) {
                continue;
            }
            String top = frames[0].toString();
            boolean idle = thread.getState() != Thread.State.RUNNABLE
                    && (top.contains("Unsafe.park") || top.contains("Object.wait") || top.contains("Thread.sleep"));
            StringBuilder trace = new StringBuilder();
            for (int i = 0; i < Math.min(frames.length, idle ? 4 : 18); i++) {
                trace.append("\n    at ").append(frames[i]);
            }
            TheOldestHouse.LOGGER.warn("OTH shutdown watch: thread \"{}\" is {}{}:{}", thread.getName(), thread.getState(),
                    idle ? " (idle)" : "", trace);
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
        // The unloads waiting to run: each is vanilla's scheduleUnload lambda, with the chunk
        // and holder it captured. One that keeps coming back names the chunk that will not settle.
        Object queue = read(map, "unloadQueue");
        if (queue instanceof Collection<?> waiting) {
            shown = 0;
            for (Object run : waiting) {
                if (shown++ == 4) {
                    break;
                }
                Object fn = read(run, "fn");
                StringBuilder captured = new StringBuilder();
                if (fn != null && !(fn instanceof String)) {
                    for (Field field : fn.getClass().getDeclaredFields()) {
                        try {
                            field.setAccessible(true);
                            Object value = field.get(fn);
                            captured.append(' ').append(field.getName()).append('=')
                                    .append(value instanceof Long packed ? new ChunkPos(packed).toString()
                                            : value != null && value.getClass().getSimpleName().endsWith("ChunkHolder") ? holder(value)
                                            : value instanceof java.util.concurrent.CompletableFuture<?> future ? future(future)
                                            : value == map ? "the chunk map" : String.valueOf(value));
                        } catch (ReflectiveOperationException | RuntimeException e) {
                            captured.append(' ').append(field.getName()).append("=<").append(e).append('>');
                        }
                    }
                }
                TheOldestHouse.LOGGER.warn("  queued unload {}:{}", fn == null ? run : fn.getClass().getSimpleName(), captured);
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
        Object saveSync = read(holder, "saveSync");
        return call(holder, "getPos") + " level " + call(holder, "getTicketLevel") + " generation refs "
                + call(holder, "getGenerationRefCount") + " status " + call(holder, "getLatestStatus")
                + " save sync " + (saveSync instanceof java.util.concurrent.CompletableFuture<?> future ? future(future) : saveSync)
                + " task " + read(holder, "task");
    }

    private static String future(java.util.concurrent.CompletableFuture<?> future) {
        return "#" + Integer.toHexString(System.identityHashCode(future)) + (future.isDone() ? " (done)" : " (pending)");
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
