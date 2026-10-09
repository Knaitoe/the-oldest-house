package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

/** Fixture lifetime lease; unlike PORTAL, it cannot expire during a paced append. */
final class NativeTestChunks implements AutoCloseable {
    private static final TicketType<Long> TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_fixture", Long::compare);
    private static long nextKey;
    private record Held(ServerLevel level, ChunkPos chunk) {}
    private final long key = ++nextKey;
    private final List<Held> held = new ArrayList<>();

    void hold(ServerLevel level, AABB bounds) {
        for (int x = ((int)Math.floor(bounds.minX) - 1) >> 4;
                x <= ((int)Math.ceil(bounds.maxX) + 1) >> 4; x++) {
            for (int z = ((int)Math.floor(bounds.minZ) - 1) >> 4;
                    z <= ((int)Math.ceil(bounds.maxZ) + 1) >> 4; z++) {
                var chunk = new ChunkPos(x, z);
                var lease = new Held(level, chunk);
                if (held.contains(lease)) continue;
                level.getChunkSource().addRegionTicket(TICKET, chunk, 3, key);
                // Immediate loading is explicit fixture setup, never production pacing.
                level.getChunk(x, z);
                held.add(lease);
            }
        }
    }

    /** Full block chunks can arrive before their native entity sections. */
    boolean ready() {
        for (var lease : held)
            if (!lease.level.areEntitiesLoaded(lease.chunk.toLong())) {
                // GameTest can consume 1,200 ticks in under a second. Give the native disk
                // worker time to return entity sections before exhausting a fixture's clock.
                // This is test-host pacing only; loaded-section checks remain mandatory.
                java.util.concurrent.locks.LockSupport.parkNanos(5_000_000L);
                return false;
            }
        return true;
    }

    @Override public void close() {
        for (var lease : held)
            lease.level.getChunkSource().removeRegionTicket(TICKET, lease.chunk, 3, key);
        held.clear();
    }
}
