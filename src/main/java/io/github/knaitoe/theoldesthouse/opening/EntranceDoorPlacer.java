package io.github.knaitoe.theoldesthouse.opening;

import io.github.knaitoe.theoldesthouse.house.HouseDimensionMirror;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

/**
 * Chooses where the entrance door appears and puts it there.
 *
 * A wall position is two stacked full, opaque, ordinary blocks with standable
 * floor and two blocks of air in front, on the side the player can walk to
 * from their bed. Walls that are part of the house proper score best:
 * indoors, flanked by more wall, and ideally backed by solid blocks so the
 * door leads nowhere at all.
 */
public final class EntranceDoorPlacer {
    /** Other players' doors must be at least this far away. */
    public static final int MIN_DOOR_SPACING = 4;
    public static final int FREESTANDING_RADIUS = 8;

    private static final int WALK_VERTICAL = 6;
    private static final int MAX_DROP = 3;
    private static final double VIEW_DISTANCE = 128.0D;
    private static final double VIEW_CONE_COS = Math.cos(Math.toRadians(80.0D));

    /**
     * A place for the door. {@code lower} is where its lower half goes and
     * {@code open} the side it opens onto.
     */
    public record Plan(BlockPos lower, Direction open, int score, boolean freestanding) {
    }

    private EntranceDoorPlacer() {
    }

    // ------------------------------------------------------------------
    // Wall positions

    public static List<Plan> findWallPlans(
            ServerLevel level,
            BlockPos bed,
            int radius,
            UUID owner,
            Predicate<BlockPos> recentlyPlaced
    ) {
        LongSet reachable = reachableStandingCells(level, bed, radius + 2);
        List<Plan> plans = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        long radiusSquared = (long) radius * radius;

        BlockPos.MutableBlockPos front = new BlockPos.MutableBlockPos();
        for (long packed : reachable) {
            front.set(BlockPos.of(packed));
            if (!level.getBlockState(front).isAir() || !level.getBlockState(front.above()).isAir()) {
                continue;
            }
            for (Direction towardWall : Direction.Plane.HORIZONTAL) {
                BlockPos lower = front.relative(towardWall).immutable();
                Direction open = towardWall.getOpposite();
                if (lower.distSqr(bed) > radiusSquared) {
                    continue;
                }
                if (!seen.add(lower.asLong() * 4 + open.get2DDataValue())) {
                    continue;
                }
                if (!isValidWallPlan(level, lower, open, owner, recentlyPlaced)) {
                    continue;
                }
                plans.add(new Plan(lower, open, scoreWall(level, lower, open, bed), false));
            }
        }

        plans.sort(Comparator.comparingInt(Plan::score).reversed()
                .thenComparingDouble(plan -> plan.lower().distSqr(bed)));
        return plans;
    }

    public static boolean isValidWallPlan(
            ServerLevel level,
            BlockPos lower,
            Direction open,
            UUID owner,
            Predicate<BlockPos> recentlyPlaced
    ) {
        BlockPos upper = lower.above();
        if (!isWallBlock(level, lower) || !isWallBlock(level, upper)) {
            return false;
        }
        if (recentlyPlaced.test(lower) || recentlyPlaced.test(upper)) {
            return false;
        }
        BlockPos front = lower.relative(open);
        if (!level.getBlockState(front).isAir() || !level.getBlockState(front.above()).isAir()) {
            return false;
        }
        if (!isFloor(level, front.below())) {
            return false;
        }
        if (!isPlainWall(level, lower)) {
            return false;
        }
        return !isReserved(level, lower, open, owner);
    }

    /**
     * "Placed so it never breaks anything they built": nothing may rest on,
     * hang from or connect to either wall block. Every neighbour (except the
     * floor beneath) must be air or a full block, and no painting or item
     * frame may hang on either face. Torches, signs, ladders, buttons, panes,
     * fences, rails, carpets and the like all rule a wall out.
     */
    public static boolean isPlainWall(ServerLevel level, BlockPos lower) {
        BlockPos upper = lower.above();
        for (BlockPos half : new BlockPos[]{lower, upper}) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = half.relative(direction);
                if (neighbour.equals(lower) || neighbour.equals(upper) || neighbour.equals(lower.below())) {
                    continue;
                }
                BlockState state = level.getBlockState(neighbour);
                if (!state.isAir() && !state.isCollisionShapeFullBlock(level, neighbour)) {
                    return false;
                }
            }
        }
        AABB faces = new AABB(lower).minmax(new AABB(upper)).inflate(0.1D);
        return level.getEntitiesOfClass(HangingEntity.class, faces).isEmpty();
    }

    /**
     * Full, solid, opaque and ordinary: no block entity, nothing redstone,
     * no door, glass or bed, and nothing unbreakable.
     */
    public static boolean isWallBlock(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        return !state.isAir()
                && state.isSolidRender(level, pos)
                && state.isCollisionShapeFullBlock(level, pos)
                && state.getFluidState().isEmpty()
                && !state.hasBlockEntity()
                && !state.isSignalSource()
                && !(block instanceof DoorBlock)
                && !(block instanceof PistonBaseBlock)
                && !(block instanceof NoteBlock)
                && !(block instanceof RedstoneLampBlock)
                && !(block instanceof TntBlock)
                && !state.is(Blocks.REDSTONE_BLOCK)
                && !state.is(Tags.Blocks.GLASS_BLOCKS)
                && !state.is(BlockTags.BEDS)
                && state.getDestroySpeed(level, pos) >= 0.0F;
    }

    private static int scoreWall(ServerLevel level, BlockPos lower, Direction open, BlockPos bed) {
        int score = 0;
        BlockPos front = lower.relative(open);
        if (!Doorsteps.canSeeSky(level, front)) {
            score += 6; // Inside the house, not on its outside wall.
        }

        BlockPos back = lower.relative(open.getOpposite());
        if (isSolid(level, back) && isSolid(level, back.above())) {
            score += 4; // Backed by solid blocks: a door to nowhere.
        } else if (Doorsteps.canSeeSky(level, back)) {
            score -= 3; // Its back would show outside.
        }

        Direction side = open.getClockWise();
        for (int sign = -1; sign <= 1; sign += 2) {
            BlockPos beside = lower.relative(side, sign);
            if (isSolid(level, beside) && isSolid(level, beside.above())) {
                score += 2; // Part of a wall rather than a pillar.
            }
        }
        if (isSolid(level, lower.above(2))) {
            score += 1;
        }
        if (isNaturalTerrain(level.getBlockState(lower)) || isNaturalTerrain(level.getBlockState(lower.above()))) {
            score -= 5; // A hillside or cave wall rather than something they built.
        }
        score -= (int) Math.round(Math.sqrt(lower.distSqr(bed)) / 3.0D);
        return score;
    }

    private static boolean isNaturalTerrain(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(Tags.Blocks.ORES)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY);
    }

    // ------------------------------------------------------------------
    // Freestanding fallback

    /**
     * Open ground within {@link #FREESTANDING_RADIUS} of the bed with room for
     * the door, a post either side and a lintel, walkable on both faces.
     */
    public static List<Plan> findFreestandingPlans(ServerLevel level, BlockPos bed, UUID owner) {
        LongSet reachable = reachableStandingCells(level, bed, FREESTANDING_RADIUS + 2);
        List<Plan> plans = new ArrayList<>();
        long radiusSquared = (long) FREESTANDING_RADIUS * FREESTANDING_RADIUS;

        for (long packed : reachable) {
            BlockPos lower = BlockPos.of(packed);
            if (lower.distSqr(bed) > radiusSquared) {
                continue;
            }
            for (Direction open : new Direction[]{Direction.NORTH, Direction.EAST}) {
                for (Direction facingOpen : new Direction[]{open, open.getOpposite()}) {
                    if (!isValidFreestandingPlan(level, lower, facingOpen, owner, reachable)) {
                        continue;
                    }
                    int score = -(int) Math.round(Math.sqrt(lower.distSqr(bed)));
                    if (facingOpen.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                        score -= 1; // Prefer one orientation so the list is stable.
                    }
                    plans.add(new Plan(lower, facingOpen, score, true));
                }
            }
        }

        plans.sort(Comparator.comparingInt(Plan::score).reversed());
        return plans;
    }

    public static boolean isValidFreestandingPlan(
            ServerLevel level,
            BlockPos lower,
            Direction open,
            UUID owner,
            @Nullable LongSet reachable
    ) {
        Direction side = open.getClockWise();
        for (int along = -1; along <= 1; along++) {
            BlockPos column = lower.relative(side, along);
            for (int y = 0; y <= 2; y++) {
                if (!level.getBlockState(column.above(y)).isAir()) {
                    return false;
                }
            }
            if (!isFloor(level, column.below())) {
                return false;
            }
        }
        for (Direction face : new Direction[]{open, open.getOpposite()}) {
            BlockPos step = lower.relative(face);
            if (!level.getBlockState(step).isAir() || !level.getBlockState(step.above()).isAir() || !isFloor(level, step.below())) {
                return false;
            }
        }
        if (reachable != null && !reachable.contains(lower.relative(open).asLong())) {
            return false;
        }
        return !isReserved(level, lower, open, owner);
    }

    // ------------------------------------------------------------------
    // Shared checks

    /** Too close to another player's door, on its wall, or inside the House's own footprint. */
    private static boolean isReserved(ServerLevel level, BlockPos lower, Direction open, UUID owner) {
        if (OpeningWorldData.get(level.getServer()).conflictsWithOtherDoor(lower, open, owner, MIN_DOOR_SPACING)) {
            return true;
        }
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        return origin != null && HouseDimensionMirror.isNearHouse(origin, lower, 4);
    }

    private static boolean isSolid(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockState(pos).isSolidRender(level, pos);
    }

    private static boolean isFloor(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP);
    }

    /**
     * Whether the player, from where they stand, could see the face where
     * the door would appear.
     */
    public static boolean isVisibleTo(ServerPlayer player, Plan plan) {
        if (!player.level().dimension().equals(Level.OVERWORLD) || player.isSleeping()) {
            return false;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        BlockPos lower = plan.lower();

        for (int dy = 0; dy <= 1; dy++) {
            BlockPos cell = lower.above(dy);
            // Just in front of the face the door will occupy.
            Vec3 face = Vec3.atCenterOf(cell).add(plan.open().getStepX() * 0.55D, 0.0D, plan.open().getStepZ() * 0.55D);
            Vec3 toFace = face.subtract(eye);
            double distance = toFace.length();
            if (distance > VIEW_DISTANCE) {
                continue;
            }
            if (distance > 0.01D && look.dot(toFace.scale(1.0D / distance)) < VIEW_CONE_COS) {
                continue;
            }
            BlockHitResult hit = player.level().clip(new ClipContext(
                    eye, face, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.MISS) {
                return true;
            }
            BlockPos hitPos = hit.getBlockPos();
            if (hitPos.equals(lower) || hitPos.equals(lower.above())) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Placement

    /**
     * Records what the door (and any frame) replaces, then places it.
     * The caller has re-validated the plan.
     */
    public static OpeningWorldData.EntranceRecord place(ServerLevel level, Plan plan, UUID owner) {
        List<OpeningWorldData.ReplacedBlock> replaced = new ArrayList<>();
        BlockPos lower = plan.lower();
        BlockPos upper = lower.above();
        Direction side = plan.open().getClockWise();

        List<BlockPos> touched = new ArrayList<>();
        touched.add(lower);
        touched.add(upper);
        if (plan.freestanding()) {
            for (int sign = -1; sign <= 1; sign += 2) {
                touched.add(lower.relative(side, sign));
                touched.add(upper.relative(side, sign));
            }
            for (int along = -1; along <= 1; along++) {
                touched.add(lower.relative(side, along).above(2));
            }
        }
        for (BlockPos pos : touched) {
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            CompoundTag data = blockEntity == null ? null : blockEntity.saveWithFullMetadata(level.registryAccess());
            replaced.add(new OpeningWorldData.ReplacedBlock(pos.immutable(), state, data));
        }

        if (plan.freestanding()) {
            BlockState post = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
            BlockState lintel = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, side.getAxis());
            for (int sign = -1; sign <= 1; sign += 2) {
                level.setBlock(lower.relative(side, sign), post, Block.UPDATE_ALL);
                level.setBlock(upper.relative(side, sign), post, Block.UPDATE_ALL);
            }
            for (int along = -1; along <= 1; along++) {
                level.setBlock(lower.relative(side, along).above(2), lintel, Block.UPDATE_ALL);
            }
        }

        // A closed door's panel sits on the side opposite its FACING, so a
        // door facing away from the open side is flush with the wall face the
        // player sees.
        BlockState door = OpeningRegistry.ENTRANCE_DOOR.get().defaultBlockState()
                .setValue(DoorBlock.FACING, plan.open().getOpposite())
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false);
        level.setBlock(lower, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_ALL);
        level.setBlock(upper, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);

        OpeningWorldData.EntranceRecord record = new OpeningWorldData.EntranceRecord(
                owner, lower.immutable(), plan.open(), plan.freestanding(), List.copyOf(replaced));
        OpeningWorldData.get(level.getServer()).putDoor(record);
        return record;
    }

    // ------------------------------------------------------------------
    // Reachability

    /**
     * Cells a player could stand in, walking (with one-block steps and short
     * drops) from beside the bed. Closed doors and gates count as passable.
     */
    public static LongSet reachableStandingCells(ServerLevel level, BlockPos bed, int radius) {
        LongSet seen = new LongOpenHashSet();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos start = bed.offset(dx, dy, dz);
                    if (isStandable(level, start) && seen.add(start.asLong())) {
                        queue.add(start);
                    }
                }
            }
        }

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(direction);
                if (Math.abs(next.getX() - bed.getX()) > radius
                        || Math.abs(next.getZ() - bed.getZ()) > radius) {
                    continue;
                }

                BlockPos target = null;
                if (isPassable(level, pos.above(2)) && isStandable(level, next.above())) {
                    target = next.above();
                } else if (isStandable(level, next)) {
                    target = next;
                } else if (isPassable(level, next) && isPassable(level, next.above())) {
                    for (int drop = 1; drop <= MAX_DROP; drop++) {
                        BlockPos below = next.below(drop);
                        if (isStandable(level, below)) {
                            target = below;
                            break;
                        }
                        if (!isPassable(level, below)) {
                            break;
                        }
                    }
                }

                if (target != null
                        && Math.abs(target.getY() - bed.getY()) <= WALK_VERTICAL
                        && seen.add(target.asLong())) {
                    queue.add(target);
                }
            }
        }
        return seen;
    }

    private static boolean isStandable(ServerLevel level, BlockPos pos) {
        if (!isPassable(level, pos) || !isPassable(level, pos.above())) {
            return false;
        }
        BlockPos below = pos.below();
        BlockState floor = level.getBlockState(below);
        return !floor.getCollisionShape(level, below).isEmpty()
                && !(floor.getBlock() instanceof DoorBlock)
                && !(floor.getBlock() instanceof FenceGateBlock);
    }

    private static boolean isPassable(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof FenceGateBlock) {
            return !(state.getBlock() instanceof EntranceDoorBlock);
        }
        return state.getCollisionShape(level, pos).isEmpty();
    }
}
