package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;

/** Native occupied-room checks and expiring chunk tickets for the two linked lake rooms. */
public final class IndianLakeRooms {
    private IndianLakeRooms() {}
    public static @Nullable BlockPos base(MinecraftServer server, LabyrinthPlace place) {
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin == null ? null : LabyrinthPlaces.base(origin, place);
    }
    public static AABB bounds(BlockPos base, LabyrinthPlace place) {
        var r = place.room();
        return new AABB(base.getX()+r.minX(), base.getY()+r.minY(), base.getZ()+r.minZ(),
                base.getX()+r.maxX()+1, base.getY()+r.maxY()+1, base.getZ()+18);
    }
    public static List<ServerPlayer> visitors(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        AABB bounds = bounds(base, place);
        return level.players().stream().filter(p -> p.isAlive() && !p.isSpectator() && bounds.contains(p.position())).toList();
    }
    public static boolean inside(ServerPlayer player, LabyrinthPlace place) {
        BlockPos base = base(player.server, place);
        return base != null && player.level().dimension().equals(HouseDimensions.INTERIOR)
                && player.isAlive() && !player.isSpectator() && bounds(base, place).contains(player.position());
    }
    public static void keepLoaded(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        AABB box = bounds(base, place);
        for (int x = ((int)box.minX-1)>>4; x <= ((int)box.maxX+1)>>4; x++)
            for (int z = ((int)box.minZ-1)>>4; z <= ((int)box.maxZ+1)>>4; z++)
                level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(x,z), 3, base);
    }
}
