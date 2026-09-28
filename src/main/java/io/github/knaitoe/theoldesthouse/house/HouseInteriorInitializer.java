package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Seeds the House dimension from the Overworld without stalling the server.
 *
 * The copy region spans the whole house plus a {@link HouseDimensionMirror#VIEW_RADIUS}
 * margin: roughly 130 x 130 x 31 blocks across ~80 chunks. Doing it in one tick
 * (and generating every House-dimension chunk on the main thread) froze the
 * server on the first entry. Instead the job starts as soon as the House
 * spawns, lets chunk tickets load both dimensions asynchronously, and copies
 * a couple of loaded chunks per tick. Nobody can be inside yet, so the work
 * is normally finished long before the first visit; if something does need
 * the interior early, {@link #ensureInitialized} completes it synchronously.
 */
public final class HouseInteriorInitializer {
    private static final TicketType<ChunkPos> TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_interior_init",
            Comparator.comparingLong(ChunkPos::toLong)
    );
    private static final int CHUNKS_PER_TICK = 2;

    @Nullable
    private static Job job;

    private HouseInteriorInitializer() {
    }

    /** Advances the background copy; called once per server tick. */
    public static void tick(MinecraftServer server, HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null || data.isInteriorInitialized()) {
            cancel();
            return;
        }

        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (interior == null) {
            return;
        }

        if (job == null || !job.origin.equals(origin)) {
            cancel();
            job = new Job(server.overworld(), interior, origin);
            job.addTickets();
            TheOldestHouse.LOGGER.info("Seeding The Oldest House interior in the background.");
        }

        int copied = 0;
        for (int i = 0; i < job.chunks.size() && copied < CHUNKS_PER_TICK; i++) {
            ChunkPos chunk = job.chunks.get(i);
            if (job.copied.contains(chunk.toLong())) {
                continue;
            }

            LevelChunk source = job.overworld.getChunkSource().getChunkNow(chunk.x, chunk.z);
            LevelChunk target = job.interior.getChunkSource().getChunkNow(chunk.x, chunk.z);
            if (source == null || target == null) {
                continue;
            }

            job.copyChunk(source, chunk);
            copied++;
        }

        if (job.copied.size() == job.chunks.size()) {
            finish(server, data);
        }
    }

    /**
     * Returns the House dimension, completing any outstanding copy first.
     * Normally a no-op; synchronous only if the interior is needed before the
     * background job has finished.
     */
    @Nullable
    public static ServerLevel ensureInitialized(MinecraftServer server, HouseSavedData data) {
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = data.houseOrigin();
        if (interior == null || origin == null) {
            return interior;
        }

        HouseDimensionMirror.syncAtmosphere(server.overworld(), interior);

        if (!data.isInteriorInitialized()) {
            if (job == null || !job.origin.equals(origin)) {
                cancel();
                job = new Job(server.overworld(), interior, origin);
            }

            TheOldestHouse.LOGGER.info(
                    "Completing The Oldest House interior synchronously ({} of {} chunks were ready).",
                    job.copied.size(),
                    job.chunks.size()
            );
            for (ChunkPos chunk : job.chunks) {
                if (!job.copied.contains(chunk.toLong())) {
                    job.copyChunk(job.overworld.getChunk(chunk.x, chunk.z), chunk);
                }
            }
            finish(server, data);
        }

        return interior;
    }

    /**
     * Whether Overworld edits at this position can be forwarded to the House
     * dimension yet: true once the chunk has been seeded (or the whole
     * interior has). Before that, the seeding copy will pick the edit up.
     */
    public static boolean isSeeded(HouseSavedData data, BlockPos pos) {
        if (data.isInteriorInitialized()) {
            return true;
        }
        return job != null && job.copied.contains(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
    }

    public static void cancel() {
        if (job != null) {
            job.removeTickets();
            job = null;
        }
    }

    private static void finish(MinecraftServer server, HouseSavedData data) {
        Job completed = job;
        job = null;
        if (completed == null) {
            return;
        }
        completed.removeTickets();

        // Entities and container contents are not part of the block copy;
        // the House dimension receives its own paintings, loot and books.
        HouseBuilder.spawnDomesticPaintings(completed.interior, completed.origin);
        HouseBuilder.applyInteriorContents(completed.interior, completed.origin);
        data.markInteriorInitialized();

        HouseStageManager.applyCurrentStage(server, data);

        TheOldestHouse.LOGGER.info("Initialized The Oldest House interior over matching native Overworld terrain.");
    }

    private static final class Job {
        final ServerLevel overworld;
        final ServerLevel interior;
        final BlockPos origin;
        final List<ChunkPos> chunks = new ArrayList<>();
        final LongSet copied = new LongOpenHashSet();
        final int minX;
        final int maxX;
        final int minY;
        final int maxY;
        final int minZ;
        final int maxZ;
        boolean ticketsAdded;

        Job(ServerLevel overworld, ServerLevel interior, BlockPos origin) {
            this.overworld = overworld;
            this.interior = interior;
            this.origin = origin.immutable();

            minX = origin.getX() + HouseLayout.MIN_X - HouseDimensionMirror.VIEW_RADIUS;
            maxX = origin.getX() + HouseLayout.MAX_X + HouseDimensionMirror.VIEW_RADIUS;
            minZ = origin.getZ() + HouseLayout.MIN_Z - HouseDimensionMirror.VIEW_RADIUS;
            maxZ = origin.getZ() + HouseLayout.MAX_Z + HouseDimensionMirror.VIEW_RADIUS;
            minY = Math.max(interior.getMinBuildHeight(), origin.getY() - HouseDimensionMirror.VIEW_BELOW_FLOOR);
            maxY = Math.min(interior.getMaxBuildHeight() - 1, origin.getY() + HouseDimensionMirror.VIEW_ABOVE_FLOOR);

            // Nearest chunks first so the house itself is seeded earliest.
            int centreChunkX = (origin.getX() + HouseLayout.CENTER_X) >> 4;
            int centreChunkZ = (origin.getZ() + HouseLayout.CENTER_Z) >> 4;
            for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
                for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                    chunks.add(new ChunkPos(cx, cz));
                }
            }
            chunks.sort(Comparator.comparingInt(
                    chunk -> Math.max(Math.abs(chunk.x - centreChunkX), Math.abs(chunk.z - centreChunkZ))
            ));
        }

        void addTickets() {
            for (ChunkPos chunk : chunks) {
                overworld.getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
                interior.getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
            }
            ticketsAdded = true;
        }

        void removeTickets() {
            if (!ticketsAdded) {
                return;
            }
            for (ChunkPos chunk : chunks) {
                overworld.getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk);
                interior.getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk);
            }
            ticketsAdded = false;
        }

        /**
         * Copies one chunk column of the view region. Native terrain already
         * matches (same generator and seed), so only differences are written,
         * with client-update-only flags: no neighbour updates means attached
         * blocks cannot pop off as item debris during the copy.
         */
        void copyChunk(LevelChunk source, ChunkPos chunk) {
            int x0 = Math.max(minX, chunk.getMinBlockX());
            int x1 = Math.min(maxX, chunk.getMaxBlockX());
            int z0 = Math.max(minZ, chunk.getMinBlockZ());
            int z1 = Math.min(maxZ, chunk.getMaxBlockZ());
            LevelChunk target = interior.getChunk(chunk.x, chunk.z);

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        pos.set(x, y, z);
                        BlockState sourceState = source.getBlockState(pos);
                        if (sourceState != target.getBlockState(pos)) {
                            interior.setBlock(pos.immutable(), sourceState, HouseDimensionMirror.MIRROR_FLAGS);
                        }
                    }
                }
            }

            // Non-inventory block-entity data (signs, banners, skulls) comes
            // along; container contents are House-dimension-only.
            for (var blockEntity : List.copyOf(source.getBlockEntities().values())) {
                BlockPos bePos = blockEntity.getBlockPos();
                if (bePos.getX() >= x0 && bePos.getX() <= x1
                        && bePos.getZ() >= z0 && bePos.getZ() <= z1
                        && bePos.getY() >= minY && bePos.getY() <= maxY) {
                    HouseDimensionMirror.copyBlockEntityData(overworld, interior, bePos);
                }
            }

            copied.add(chunk.toLong());
        }
    }
}
