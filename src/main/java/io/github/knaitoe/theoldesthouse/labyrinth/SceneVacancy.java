package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

/**
 * Whether a small in-place scenery change may happen now: nobody's camera (spectators
 * included) within the margin, the area's chunks and native entity sections already
 * loaded, and no living body inside it. Never loads anything or moves anyone.
 */
public final class SceneVacancy {
    private SceneVacancy() {
    }

    public static boolean ready(ServerLevel level, AABB area, double cameraMargin) {
        AABB watched = area.inflate(cameraMargin);
        for (var player : level.players()) if (watched.intersects(player.getCamera().getBoundingBox())) return false;
        int y = (int) Math.floor(area.minY);
        for (int x = ((int) Math.floor(area.minX)) >> 4; x <= ((int) Math.ceil(area.maxX) - 1) >> 4; x++)
            for (int z = ((int) Math.floor(area.minZ)) >> 4; z <= ((int) Math.ceil(area.maxZ) - 1) >> 4; z++)
                if (!level.isLoaded(new BlockPos(x << 4, y, z << 4)) || !level.areEntitiesLoaded(ChunkPos.asLong(x, z))) return false;
        return level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive).isEmpty();
    }
}
