package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Whether anyone can see a spot. The house only changes what nobody is
 * looking at.
 *
 * A player sees a point when it is within {@link #RANGE} blocks, inside a
 * generous cone around where they are looking (wider than any field of view
 * setting, since the client's own is not known here), and nothing solid
 * stands between their eyes and it.
 */
public final class HouseWatchers {
    public static final double RANGE = 48.0D;
    /** cos(75 degrees): anything within 75 degrees of the view direction counts as in view. */
    private static final double VIEW_CONE_COS = 0.26D;

    private HouseWatchers() {
    }

    public static boolean isWatched(ServerLevel level, BlockPos pos) {
        return isWatched(level, Vec3.atCenterOf(pos), pos);
    }

    public static boolean isWatched(ServerLevel level, Vec3 point) {
        return isWatched(level, point, BlockPos.containing(point));
    }

    private static boolean isWatched(ServerLevel level, Vec3 point, BlockPos target) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isSleeping()) {
                continue;
            }
            Vec3 eye = player.getEyePosition();
            Vec3 toPoint = point.subtract(eye);
            double distance = toPoint.length();
            if (distance > RANGE) {
                continue;
            }
            if (distance < 1.5D) {
                return true; // Close enough to notice without looking.
            }
            if (player.getLookAngle().dot(toPoint.scale(1.0D / distance)) < VIEW_CONE_COS) {
                continue;
            }
            BlockHitResult hit = level.clip(new ClipContext(eye, point, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.MISS || hit.getBlockPos().distManhattan(target) <= 1) {
                return true;
            }
        }
        return false;
    }
}
