package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/** Landscape mobs use the reader's terrain horizon, including the corners of the chunk view. */
public final class HouseMobSight {
    private HouseMobSight() {}

    public static int viewDistance(ServerPlayer player) {
        return Math.max(2, Math.min(32, Math.min(player.requestedViewDistance(),
                player.server.getPlayerList().getViewDistance())));
    }

    public static double distanceSquared(int chunks) {
        double edge = (Math.max(2, Math.min(32, chunks)) + 1) * 16.0D;
        return 2.0D * edge * edge;
    }

    /** The server still checks the reader's actual sent chunks and broadcast permissions. */
    public static boolean tracksToHorizon(Entity entity) {
        return entity instanceof Mob && (entity.level().dimension().equals(HouseDimensions.OUTSIDE)
                || HouseExteriorEntityMirror.isProjection(entity));
    }

    /** Projection tags are server-only; identify the client scenery by the shared manor geometry. */
    public static boolean rendersToHorizon(Entity entity, BlockPos origin) {
        if (!(entity instanceof Mob)) return false;
        var dimension = entity.level().dimension();
        if (dimension.equals(HouseDimensions.OUTSIDE)) return true;
        if (origin == null) return false;
        boolean domestic = HouseLayout.isInsideDomesticVolume(entity.getX() - origin.getX(),
                entity.getY() - origin.getY(), entity.getZ() - origin.getZ());
        if (dimension.equals(Level.OVERWORLD)) return domestic;
        return dimension.equals(HouseDimensions.INTERIOR) && !domestic
                && !HouseLabyrinth.isBeyondThreshold(origin, entity.blockPosition())
                && !HouseShifts.isInDeepenedHall(origin, entity.getX(), entity.getY(), entity.getZ());
    }
}
