package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Keeps the manor's chunks loaded on both sides of its walls while anyone is
 * near it, in either dimension. Crossing then never waits on a chunk being
 * read from disk: the other side is already there to be sent.
 */
public final class HouseChunkKeeper {
    private static final TicketType<ChunkPos> TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_approach",
            Comparator.comparingLong(ChunkPos::toLong)
    );
    private static final double NEAR = 56.0D;
    private static final int INTERVAL = 20;
    private static final int RING = 1;

    private static List<ChunkPos> held = List.of();
    private static boolean holding;

    private HouseChunkKeeper() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % INTERVAL != 0) {
            return;
        }
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || interior == null) {
            release(server);
            return;
        }
        boolean near = anyoneNear(server.overworld(), origin) || anyoneNear(interior, origin);
        if (near && !holding) {
            hold(server, origin);
        } else if (!near && holding) {
            release(server);
        }
    }

    private static boolean anyoneNear(ServerLevel level, BlockPos origin) {
        double cx = origin.getX() + HouseLayout.CENTER_X + 0.5D;
        double cz = origin.getZ() + HouseLayout.CENTER_Z + 0.5D;
        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - cx;
            double dz = player.getZ() - cz;
            if (dx * dx + dz * dz <= NEAR * NEAR) {
                return true;
            }
        }
        return false;
    }

    private static void hold(MinecraftServer server, BlockPos origin) {
        List<ChunkPos> chunks = new ArrayList<>();
        int minX = SectionPos.blockToSectionCoord(origin.getX() + HouseLayout.MIN_X) - RING;
        int maxX = SectionPos.blockToSectionCoord(origin.getX() + HouseLayout.MAX_X) + RING;
        int minZ = SectionPos.blockToSectionCoord(origin.getZ() + HouseLayout.MIN_Z) - RING;
        int maxZ = SectionPos.blockToSectionCoord(origin.getZ() + HouseLayout.MAX_Z) + RING;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        for (ChunkPos chunk : chunks) {
            server.overworld().getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
            if (interior != null) {
                interior.getChunkSource().addRegionTicket(TICKET, chunk, 0, chunk);
            }
        }
        held = chunks;
        holding = true;
    }

    /** Lets the chunks go (nobody near, or the server stopping). */
    public static void release(MinecraftServer server) {
        if (!holding) {
            return;
        }
        try {
            ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
            for (ChunkPos chunk : held) {
                server.overworld().getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk);
                if (interior != null) {
                    interior.getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk);
                }
            }
        } catch (RuntimeException e) {
            // A server already shutting down has nothing left to release.
        }
        held = List.of();
        holding = false;
    }
}
