package io.github.knaitoe.theoldesthouse.opening;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Finds a player's bed, their most-used door and its doorstep. */
public final class Doorsteps {
    /** How far around the bed to look for a door when none has been used. */
    private static final int NEAREST_DOOR_RADIUS = 16;
    private static final int NEAREST_DOOR_HEIGHT = 5;

    /** Where something is set down, and the door it belongs to, if any. */
    public record Delivery(BlockPos spot, @Nullable BlockPos door) {
    }

    private Doorsteps() {
    }

    /**
     * The player's respawn position, if it was set by a bed in the Overworld.
     * An unloaded bed is trusted: it can only have been set by using one.
     */
    public static Optional<BlockPos> bedPosition(ServerPlayer player) {
        BlockPos respawn = player.getRespawnPosition();
        if (respawn == null || !Level.OVERWORLD.equals(player.getRespawnDimension())) {
            return Optional.empty();
        }
        ServerLevel overworld = player.server.overworld();
        if (overworld.isLoaded(respawn) && !overworld.getBlockState(respawn).is(BlockTags.BEDS)) {
            return Optional.empty();
        }
        return Optional.of(respawn);
    }

    public static boolean isOrdinaryDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock && !(state.getBlock() instanceof EntranceDoorBlock);
    }

    /**
     * Where the letter (and later Hillary) is left: the doorstep of the
     * most-used door near the bed, else of the door nearest the bed, else the
     * floor beside the bed.
     */
    @Nullable
    public static Delivery resolve(ServerLevel level, OpeningPlayerState state, BlockPos bed, int radius) {
        BlockPos door = mostUsedDoor(level, state, bed, radius);
        if (door == null) {
            door = nearestDoor(level, bed);
        }
        if (door != null) {
            BlockPos step = doorstep(level, door, bed);
            if (step != null) {
                return new Delivery(step, door);
            }
        }
        BlockPos beside = besideBed(level, bed);
        return beside == null ? null : new Delivery(beside, null);
    }

    @Nullable
    public static BlockPos mostUsedDoor(ServerLevel level, OpeningPlayerState state, BlockPos bed, int radius) {
        long radiusSquared = (long) radius * radius;
        BlockPos best = null;
        int bestCount = 0;
        for (OpeningPlayerState.DoorUse use : state.doorUse()) {
            if (use.count() <= bestCount || use.pos().distSqr(bed) > radiusSquared) {
                continue;
            }
            if (!isOrdinaryDoor(level.getBlockState(use.pos()))) {
                continue;
            }
            best = use.pos();
            bestCount = use.count();
        }
        return best;
    }

    @Nullable
    public static BlockPos nearestDoor(ServerLevel level, BlockPos bed) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -NEAREST_DOOR_RADIUS; dx <= NEAREST_DOOR_RADIUS; dx++) {
            for (int dz = -NEAREST_DOOR_RADIUS; dz <= NEAREST_DOOR_RADIUS; dz++) {
                for (int dy = -NEAREST_DOOR_HEIGHT; dy <= NEAREST_DOOR_HEIGHT; dy++) {
                    pos.set(bed.getX() + dx, bed.getY() + dy, bed.getZ() + dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (!isOrdinaryDoor(state) || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
                        continue;
                    }
                    double distance = pos.distSqr(bed);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                    }
                }
            }
        }
        return best;
    }

    /**
     * The block in front of the door on its outdoor side: the side that can
     * see the sky, or if both or neither can, the side facing away from the
     * bed. Returns null when neither side has room to set anything down.
     */
    @Nullable
    public static BlockPos doorstep(ServerLevel level, BlockPos door, BlockPos bed) {
        BlockState state = level.getBlockState(door);
        if (!(state.getBlock() instanceof DoorBlock)) {
            return null;
        }
        Direction facing = state.getValue(DoorBlock.FACING);
        BlockPos a = door.relative(facing);
        BlockPos b = door.relative(facing.getOpposite());
        boolean aOk = hasRoom(level, a);
        boolean bOk = hasRoom(level, b);
        if (!aOk && !bOk) {
            return null;
        }
        if (aOk != bOk) {
            return aOk ? a : b;
        }

        boolean aSky = canSeeSky(level, a);
        boolean bSky = canSeeSky(level, b);
        if (aSky != bSky) {
            return aSky ? a : b;
        }
        return a.distSqr(bed) >= b.distSqr(bed) ? a : b;
    }

    @Nullable
    public static BlockPos besideBed(ServerLevel level, BlockPos bed) {
        for (int dy = 0; dy <= 1; dy++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int reach = 1; reach <= 2; reach++) {
                    BlockPos candidate = bed.relative(direction, reach).below(dy);
                    if (hasRoom(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sky test from the heightmap: it is updated as blocks change, unlike sky
     * light, which lags behind freshly built roofs. Leaves do not count as a
     * roof.
     */
    public static boolean canSeeSky(ServerLevel level, BlockPos pos) {
        return pos.getY() >= level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
    }

    /** Room to stand or set something down: clear at head height, with something to rest on. */
    public static boolean hasRoom(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        if (state.getFluidState().is(FluidTags.LAVA) || above.getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        if (!above.getCollisionShape(level, pos.above()).isEmpty()) {
            return false;
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (!shape.isEmpty()) {
            // A carpet, slab or snow layer to stand on is fine; anything taller is not.
            return shape.max(Direction.Axis.Y) <= 0.5D;
        }
        BlockPos below = pos.below();
        return !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }

    /** Resting point on top of whatever occupies the bottom of {@code pos}. */
    public static Vec3 restingPoint(ServerLevel level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        double y = shape.isEmpty() ? 0.0D : shape.max(Direction.Axis.Y);
        return new Vec3(pos.getX() + 0.5D, pos.getY() + y + 0.05D, pos.getZ() + 0.5D);
    }
}
