package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.581. The light a reader's client has for a room is the light the server has for it.
 *
 * <p>A client is sent each chunk with whatever light the server holds for it at that moment. The server works light out
 * on a thread of its own, a little behind the blocks: a room built as a reader comes in, or a scene set in the tick they
 * arrive, is sent with the light of what stood there before. The game does not put that right afterwards. It sends a
 * change of light only to players at the edge of their view, because a client nearer than that works light out for itself
 * from the block changes it is sent, and it is sent none for a block that was already in the chunk when the chunk went
 * out. The House's own dimensions have sky light, so a hall built in open air kept the open sky's light on such a client
 * for as long as they stood in it: lit as by day, with every lamp off.
 *
 * <p>So, in the House's dimensions only: a chunk's light is marked when the chunk is queued for a reader and again when
 * it is sent. Once the server's light has caught up with everything that had been asked of it by then, the chunk's light
 * is looked at once more, and if it is not what it was at either mark it is sent to that reader again, whole. Nothing is
 * sent for a chunk whose light did not move, which is nearly every chunk nearly always.
 *
 * <p>The second look is made on the server's own thread with the light caught up to that very moment, so that no block
 * change the client has already been told about is missing from the light it is then given: a change later than the
 * packet reaches the client after it, and the client works that one out on top, as it always does.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class LightSync {
    /** How long, in one tick and for every reader together, the server's thread will stand waiting for light to catch up. */
    private static final long WAIT_NANOS = 20_000_000L;
    /** After this many ticks running in which that wait was not long enough, the look is made on what has caught up so far. */
    private static final int MISSES = 10;
    private static final Map<UUID, Reader> READERS = new HashMap<>();
    private static int resent;

    private LightSync() {
    }

    private static final class Reader {
        ResourceKey<Level> dimension;
        /** A chunk's light as it was when the chunk was queued for this reader, and as it was when it was sent. */
        final Long2LongOpenHashMap queued = new Long2LongOpenHashMap();
        final Long2LongOpenHashMap sent = new Long2LongOpenHashMap();
        /** The chunks whose light is being waited for, and the wait. */
        LongSet round;
        CompletableFuture<?> wait;
        int misses;
    }

    /** For tests and logs: how many chunks' light has been sent again since the server started. */
    public static int resent() {
        return resent;
    }

    /** For tests: whether any chunk sent to this reader is still waiting for its second look. */
    public static boolean pending(ServerPlayer player) {
        Reader reader = READERS.get(player.getUUID());
        return reader != null && !reader.sent.isEmpty();
    }

    private static Reader reader(ServerPlayer player, ServerLevel level) {
        Reader reader = READERS.computeIfAbsent(player.getUUID(), id -> new Reader());
        if (!level.dimension().equals(reader.dimension)) {
            reader.dimension = level.dimension();
            reader.queued.clear();
            reader.sent.clear();
            reader.round = null;
            reader.wait = null;
            reader.misses = 0;
        }
        return reader;
    }

    @SubscribeEvent
    public static void queued(ChunkWatchEvent.Watch event) {
        if (!HouseDimensions.isHouseDimension(event.getLevel().dimension())) return;
        reader(event.getPlayer(), event.getLevel()).queued.put(event.getPos().toLong(), print(event.getLevel(), event.getChunk()));
    }

    @SubscribeEvent
    public static void sent(ChunkWatchEvent.Sent event) {
        if (!HouseDimensions.isHouseDimension(event.getLevel().dimension())) return;
        reader(event.getPlayer(), event.getLevel()).sent.put(event.getPos().toLong(), print(event.getLevel(), event.getChunk()));
    }

    @SubscribeEvent
    public static void dropped(ChunkWatchEvent.UnWatch event) {
        Reader reader = READERS.get(event.getPlayer().getUUID());
        if (reader == null || !event.getLevel().dimension().equals(reader.dimension)) return;
        long key = event.getPos().toLong();
        reader.queued.remove(key);
        reader.sent.remove(key);
        if (reader.round != null) reader.round.remove(key);
    }

    @SubscribeEvent
    public static void left(PlayerEvent.PlayerLoggedOutEvent event) {
        READERS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        READERS.clear();
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (READERS.isEmpty()) return;
        long until = System.nanoTime() + WAIT_NANOS;
        for (var each = READERS.entrySet().iterator(); each.hasNext();) {
            var entry = each.next();
            Reader reader = entry.getValue();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || player.hasDisconnected() || !player.level().dimension().equals(reader.dimension)) {
                each.remove();
                continue;
            }
            if (reader.sent.isEmpty()) {
                reader.round = null;
                reader.wait = null;
                continue;
            }
            ServerLevel level = player.serverLevel();
            ThreadedLevelLightEngine light = level.getChunkSource().getLightEngine();
            if (reader.wait == null) {
                // Everything asked of the light up to now, for these chunks and the ones beside them: waited for off this thread.
                reader.round = new LongOpenHashSet(reader.sent.keySet());
                reader.wait = caughtUp(light, reader.round);
                light.tryScheduleUpdate();
                continue;
            }
            light.tryScheduleUpdate();
            if (!reader.wait.isDone()) continue;
            // That much is done. Whatever has been asked since is waited for here, briefly, so that nothing is placed between
            // the light catching up and the look at it. If it does not come in time, the wait goes on off this thread.
            CompletableFuture<?> last = caughtUp(light, reader.round);
            while (!last.isDone() && System.nanoTime() < until) {
                light.tryScheduleUpdate();
                LockSupport.parkNanos(50_000L);
            }
            if (!last.isDone() && ++reader.misses < MISSES) {
                reader.wait = last;
                continue;
            }
            reader.misses = 0;
            int again = 0;
            for (long key : reader.round) {
                if (!reader.sent.containsKey(key)) continue;
                long whenSent = reader.sent.remove(key);
                int x = ChunkPos.getX(key), z = ChunkPos.getZ(key);
                // Queued again since (dropped and taken up): it is marked afresh when it goes out.
                if (player.connection.chunkSender.isPending(key)) continue;
                long whenQueued = reader.queued.containsKey(key) ? reader.queued.remove(key) : whenSent;
                LevelChunk chunk = player.getChunkTrackingView().contains(x, z) ? level.getChunkSource().getChunkNow(x, z) : null;
                if (chunk == null) continue;
                long now = print(level, chunk);
                if (now == whenSent && now == whenQueued) continue;
                player.connection.send(new ClientboundLightUpdatePacket(chunk.getPos(), light, null, null));
                again++;
            }
            reader.round = null;
            reader.wait = null;
            if (again > 0) {
                resent += again;
                TheOldestHouse.LOGGER.debug("The light of {} chunks had moved since they were sent to a reader; it was sent again.", again);
            }
        }
    }

    /** Done once the light has dealt with everything asked of it so far for these chunks and the eight round each of them. */
    private static CompletableFuture<?> caughtUp(ThreadedLevelLightEngine light, LongSet chunks) {
        LongSet all = new LongOpenHashSet();
        for (long key : chunks) {
            int x = ChunkPos.getX(key), z = ChunkPos.getZ(key);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) all.add(ChunkPos.asLong(x + dx, z + dz));
        }
        var waits = new ArrayList<CompletableFuture<?>>(all.size());
        for (long key : all) waits.add(light.waitForPendingTasks(ChunkPos.getX(key), ChunkPos.getZ(key)));
        return CompletableFuture.allOf(waits.toArray(CompletableFuture[]::new));
    }

    /**
     * A mark of a chunk's light, block and sky, as the server holds it now: over the sections that hold any block and the
     * one above and below them, which is where light is kept. Two marks are equal when the light is.
     */
    public static long print(ServerLevel level, LevelChunk chunk) {
        var sections = chunk.getSections();
        int low = -1, high = -1;
        for (int i = 0; i < sections.length; i++) {
            if (sections[i].hasOnlyAir()) continue;
            if (low < 0) low = i;
            high = i;
        }
        if (low < 0) return 0L;
        var light = level.getChunkSource().getLightEngine();
        long mark = 1469598103934665603L;
        int last = chunk.getSectionYFromSectionIndex(high) + 1;
        for (int y = chunk.getSectionYFromSectionIndex(low) - 1; y <= last; y++) {
            SectionPos at = SectionPos.of(chunk.getPos(), y);
            for (LightLayer layer : LightLayer.values()) {
                DataLayer data = light.getLayerListener(layer).getDataLayerData(at);
                int part = data == null ? 1 : data.isEmpty() ? 2 : Arrays.hashCode(data.copy().getData());
                mark = (mark ^ part) * 1099511628211L;
            }
        }
        return mark;
    }
}
