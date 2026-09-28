package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.opening.Doorsteps;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Keeps the Overworld manor a visual/proxy shell rather than an inaccessible
 * entity trap.
 *
 * Players crossing the domestic boundary are transferred to the matching
 * House dimension. Non-player mobs never are. Any mob that wanders into the
 * Overworld proxy is therefore moved back through the nearest authored
 * exterior doorway to a safe exterior standing position.
 */
public final class HouseProxyEntityEvacuation {
    private static final int CHECK_INTERVAL_TICKS = 5;
    private static final int MAX_DOOR_REACH = 5;

    private HouseProxyEntityEvacuation() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        if (!data.isSpawned() || origin == null) {
            return;
        }

        evacuateAll(server.overworld(), origin);
    }

    /**
     * Immediately clears every living non-player mob from the proxy interior.
     * Useful at the exact moment a player crosses the dimension seam as well
     * as from the periodic safety sweep.
     */
    public static int evacuateAll(ServerLevel level, BlockPos origin) {
        AABB bounds = new AABB(
                origin.getX() + HouseLayout.MIN_X - 1.0D,
                origin.getY() + HouseLayout.MIN_Y - 1.0D,
                origin.getZ() + HouseLayout.CLEAR_MIN_Z - 1.0D,
                origin.getX() + HouseLayout.MAX_X + 2.0D,
                origin.getY() + HouseLayout.MAX_Y + 2.0D,
                origin.getZ() + HouseLayout.MAX_Z + 2.0D
        );

        int moved = 0;
        for (Mob mob : level.getEntitiesOfClass(
                Mob.class,
                bounds,
                entity -> entity.isAlive() && !entity.isRemoved()
        )) {
            double relX = mob.getX() - origin.getX();
            double relY = mob.getY() - origin.getY();
            double relZ = mob.getZ() - origin.getZ();

            if (!HouseLayout.isInsideDomesticVolume(relX, relY, relZ)) {
                continue;
            }

            if (evacuateMob(level, origin, mob)) {
                moved++;
            }
        }
        return moved;
    }

    /**
     * Moves one mob to the nearest safe exterior position associated with an
     * authored exterior door. The mob remains in the Overworld.
     */
    public static boolean evacuateMob(ServerLevel level, BlockPos origin, Mob mob) {
        Vec3 exit = nearestExit(level, origin, mob.position());
        if (exit == null) {
            return false;
        }

        mob.stopRiding();
        mob.getNavigation().stop();
        mob.clearRestriction();
        mob.moveTo(exit.x, exit.y, exit.z, mob.getYRot(), mob.getXRot());
        mob.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    @Nullable
    private static Vec3 nearestExit(ServerLevel level, BlockPos origin, Vec3 from) {
        Vec3 best = null;
        double bestDistance = Double.MAX_VALUE;

        for (HouseLayout.ExteriorDoor door : HouseLayout.EXTERIOR_DOORS) {
            Vec3 candidate = safeOutsideDoor(level, origin, door);
            if (candidate == null) {
                continue;
            }
            double distance = candidate.distanceToSqr(from);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }

        if (best != null) {
            return best;
        }

        // Defensive fallback around the front approach. Normal generated
        // houses should always have a safe door-side cell, but terrain or a
        // later player edit should not leave an entity permanently imprisoned.
        BlockPos front = origin.offset(
                HouseLayout.AXIS_X,
                HouseLayout.FRONT_DOOR.y(),
                HouseLayout.FRONT_DOOR_Z - 3
        );
        return nearbySafeExterior(level, origin, front, Direction.NORTH);
    }

    /**
     * Safe standing point immediately outside the authored front entrance.
     * Used by Hillary so her refusal is staged at the exact boundary the
     * player crosses.
     */
    @Nullable
    public static Vec3 frontDoorExit(ServerLevel level, BlockPos origin) {
        return safeOutsideDoor(level, origin, HouseLayout.FRONT_DOOR);
    }

    @Nullable
    private static Vec3 safeOutsideDoor(
            ServerLevel level,
            BlockPos origin,
            HouseLayout.ExteriorDoor door
    ) {
        Direction outward = direction(door.face());
        Direction sideways = outward.getClockWise();
        BlockPos doorPos = origin.offset(door.x(), door.y(), door.z());

        for (int step = 1; step <= MAX_DOOR_REACH; step++) {
            for (int lateral : new int[]{0, 1, -1, 2, -2}) {
                BlockPos column = doorPos.relative(outward, step).relative(sideways, lateral);
                Vec3 safe = nearbySafeExterior(level, origin, column, outward);
                if (safe != null) {
                    return safe;
                }
            }
        }
        return null;
    }

    @Nullable
    private static Vec3 nearbySafeExterior(
            ServerLevel level,
            BlockPos origin,
            BlockPos column,
            Direction outward
    ) {
        for (int dy : new int[]{0, 1, -1, 2, -2}) {
            BlockPos candidate = column.offset(0, dy, 0);
            double relX = candidate.getX() + 0.5D - origin.getX();
            double relY = candidate.getY() - origin.getY();
            double relZ = candidate.getZ() + 0.5D - origin.getZ();

            if (HouseLayout.isInsideDomesticVolume(relX, relY, relZ)) {
                continue;
            }
            if (!level.getFluidState(candidate).isEmpty()
                    || !level.getFluidState(candidate.above()).isEmpty()) {
                continue;
            }
            if (!Doorsteps.hasRoom(level, candidate)) {
                continue;
            }

            // Bias the resting point slightly farther away from the shell so a
            // wide mob does not immediately intersect the transition margin.
            Vec3 at = Doorsteps.restingPoint(level, candidate);
            return at.add(
                    outward.getStepX() * 0.18D,
                    0.0D,
                    outward.getStepZ() * 0.18D
            );
        }
        return null;
    }

    private static Direction direction(HouseLayout.Face face) {
        return switch (face) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
        };
    }
}
