package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** A bounded snapshot waits for cold native sections instead of generating them in a block read. */
public final class MiniatureSamples {
    private MiniatureSamples() {}
    public static @Nullable int[] capture(ServerLevel level, BlockPos base) {
        boolean loaded = true;
        for (int x = (base.getX() - 4) >> 4; x <= (base.getX() + 3) >> 4; x++)
            for (int z = (base.getZ() - 10) >> 4; z <= (base.getZ() - 3) >> 4; z++) {
                var chunk = new ChunkPos(x, z);
                level.getChunkSource().addRegionTicket(TicketType.PORTAL, chunk, 3, base);
                if (level.getChunkSource().getChunkNow(x, z) == null) {
                    level.getChunkSource().getChunkFuture(x, z, ChunkStatus.FULL, true);
                    loaded = false;
                }
            }
        if (!loaded) return null;
        int[] states = new int[256];
        for (int i = 0; i < states.length; i++)
            states[i] = Block.getId(level.getBlockState(base.offset(i % 8 - 4, i / 64 - 1, -3 - (i / 8) % 8)));
        return states;
    }
}
