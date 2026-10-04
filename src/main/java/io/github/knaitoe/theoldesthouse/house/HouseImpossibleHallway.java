package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class HouseImpossibleHallway {
    /** The hallway begins immediately behind the threshold wall at the end of the hall. */
    public static final int START_Z_OFFSET = HouseLayout.THRESHOLD_Z + 1;
    public static final int LENGTH = 56;
    public static final int END_Z_OFFSET = START_Z_OFFSET + LENGTH;

    // Same width and walls as the domestic hall, so it reads as its continuation.
    private static final int LEFT_WALL_X_OFFSET = HouseLayout.AXIS_X - 2;
    private static final int RIGHT_WALL_X_OFFSET = HouseLayout.AXIS_X + 2;
    private static final int INNER_MIN_X_OFFSET = HouseLayout.HALL_MIN_X;
    private static final int INNER_MAX_X_OFFSET = HouseLayout.HALL_MAX_X;
    private static final int QUIET_FLAGS =
            Block.UPDATE_CLIENTS
                    | Block.UPDATE_KNOWN_SHAPE
                    | Block.UPDATE_SUPPRESS_DROPS;

    private HouseImpossibleHallway() {
    }

    public static void build(ServerLevel level, BlockPos origin) {
        for (int x = LEFT_WALL_X_OFFSET; x <= RIGHT_WALL_X_OFFSET; x++) {
            for (int z = START_Z_OFFSET; z <= END_Z_OFFSET; z++) {
                for (int y = 0; y <= 5; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), QUIET_FLAGS);
                }
            }
        }

        for (int z = START_Z_OFFSET; z <= END_Z_OFFSET; z++) {
            for (int x = INNER_MIN_X_OFFSET; x <= INNER_MAX_X_OFFSET; x++) {
                level.setBlock(origin.offset(x, 0, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), QUIET_FLAGS);
                level.setBlock(origin.offset(x, 5, z), Blocks.SPRUCE_SLAB.defaultBlockState(), QUIET_FLAGS);
            }

            for (int y = 1; y <= 4; y++) {
                level.setBlock(origin.offset(LEFT_WALL_X_OFFSET, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), QUIET_FLAGS);
                level.setBlock(origin.offset(RIGHT_WALL_X_OFFSET, y, z), Blocks.WHITE_TERRACOTTA.defaultBlockState(), QUIET_FLAGS);
            }

            level.setBlock(origin.offset(HouseLayout.AXIS_X, 1, z), Blocks.RED_CARPET.defaultBlockState(), QUIET_FLAGS);
        }

        for (int step : new int[]{8, 20, 32, 44}) {
            int z = START_Z_OFFSET + step;
            level.setBlock(origin.offset(HouseLayout.AXIS_X, 4, z), Blocks.CHAIN.defaultBlockState(), QUIET_FLAGS);
            level.setBlock(
                    origin.offset(HouseLayout.AXIS_X, 3, z),
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true),
                    2
            );
        }

        for (int x = LEFT_WALL_X_OFFSET; x <= RIGHT_WALL_X_OFFSET; x++) {
            for (int y = 1; y <= 4; y++) {
                level.setBlock(
                        origin.offset(x, y, END_Z_OFFSET),
                        Blocks.WHITE_TERRACOTTA.defaultBlockState(),
                        QUIET_FLAGS
                );
            }
        }

        dressDomesticApproach(level, origin);
        purgeConstructionDebris(level, origin);
    }

    /** Ordinary cupboards and continuous plaster precede the first dealt room. */
    public static void dressDomesticApproach(ServerLevel level, BlockPos origin) {
        for (int z = START_Z_OFFSET; z < END_Z_OFFSET; z++) {
            for (int x : new int[]{LEFT_WALL_X_OFFSET, RIGHT_WALL_X_OFFSET}) {
                BlockPos pos = origin.offset(x, 1, z);
                if (level.getBlockState(pos).is(Blocks.WHITE_TERRACOTTA))
                    level.setBlock(pos, Blocks.SPRUCE_PLANKS.defaultBlockState(), QUIET_FLAGS);
            }
        }
        for (int step = 12; step <= 14; step++) {
            BlockPos pos = origin.offset(LEFT_WALL_X_OFFSET, 1, START_Z_OFFSET + step);
            if (level.getBlockState(pos).is(Blocks.SPRUCE_PLANKS))
                level.setBlock(pos, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.EAST), QUIET_FLAGS);
        }
        for (int step = 26; step <= 31; step++) for (int y = 2; y <= 4; y++) {
            BlockPos pos = origin.offset(RIGHT_WALL_X_OFFSET, y, START_Z_OFFSET + step);
            if (level.getBlockState(pos).is(Blocks.YELLOW_TERRACOTTA))
                level.setBlock(pos, Blocks.WHITE_TERRACOTTA.defaultBlockState(), QUIET_FLAGS);
        }
    }

    private static void purgeConstructionDebris(ServerLevel level, BlockPos origin) {
        AABB bounds = new AABB(
                origin.getX() + LEFT_WALL_X_OFFSET - 1,
                origin.getY() - 1,
                origin.getZ() + START_Z_OFFSET - 1,
                origin.getX() + RIGHT_WALL_X_OFFSET + 2,
                origin.getY() + 7,
                origin.getZ() + END_Z_OFFSET + 2
        );

        level.getEntitiesOfClass(ItemEntity.class, bounds).forEach(ItemEntity::discard);
        level.getEntitiesOfClass(FallingBlockEntity.class, bounds).forEach(FallingBlockEntity::discard);
    }

    private static final int REPAIR_INTERVAL = 200;

    /** Every ten seconds, a loaded revealed hallway mends any shell cell that burned or was blown away. */
    public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        var server = event.getServer();
        // GameTest fixtures share one saved House; they call repairShell directly.
        if (server.getTickCount() % REPAIR_INTERVAL != 0 || server instanceof net.minecraft.gametest.framework.GameTestServer) return;
        HouseSavedData data = HouseSavedData.get(server);
        BlockPos origin = data.houseOrigin();
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR);
        if (origin == null || level == null || !data.isImpossibleDoorRevealed()) return;
        int mended = repairShell(level, origin);
        if (mended > 0) io.github.knaitoe.theoldesthouse.TheOldestHouse.LOGGER.info("The impossible hallway mended {} burnt or broken block(s).", mended);
    }

    /**
     * Restores shell cells (floor, ceiling, walls) that are now air or fire to
     * the hallway's own materials. Anything else standing there is left alone,
     * as is the far door's cell, and unloaded stretches wait for a visitor.
     */
    public static int repairShell(ServerLevel level, BlockPos origin) {
        int mended = 0;
        for (int z = START_Z_OFFSET; z <= END_Z_OFFSET; z++) {
            if (!level.isLoaded(origin.offset(HouseLayout.AXIS_X, 0, z))) continue;
            for (int x = LEFT_WALL_X_OFFSET; x <= RIGHT_WALL_X_OFFSET; x++) {
                for (int y = 0; y <= 5; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (!isProtectedStructureBlock(origin, pos)) continue;
                    if (z == END_Z_OFFSET && x == HouseLayout.AXIS_X && (y == 1 || y == 2)) continue;
                    var state = level.getBlockState(pos);
                    if (!state.isAir() && !(state.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock)) continue;
                    var material = y == 0 ? Blocks.SPRUCE_PLANKS.defaultBlockState()
                            : y == 5 ? Blocks.SPRUCE_SLAB.defaultBlockState()
                            : y == 1 && z < END_Z_OFFSET ? Blocks.SPRUCE_PLANKS.defaultBlockState()
                            : Blocks.WHITE_TERRACOTTA.defaultBlockState();
                    level.setBlock(pos, material, QUIET_FLAGS);
                    mended++;
                }
            }
        }
        return mended;
    }

    /**
     * A block of the revealed impossible hallway, walls to dressing: nothing
     * outside it may burn it or blow it apart.
     */
    public static boolean isShielded(net.minecraft.world.level.Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || !server.dimension().equals(HouseDimensions.INTERIOR)) return false;
        HouseSavedData data = HouseSavedData.get(server.getServer());
        BlockPos origin = data.houseOrigin();
        return origin != null && data.isImpossibleDoorRevealed() && isInteriorOnlyPosition(origin, pos);
    }

    public static boolean isInteriorOnlyPosition(BlockPos origin, BlockPos pos) {
        int relX = pos.getX() - origin.getX();
        int relY = pos.getY() - origin.getY();
        int relZ = pos.getZ() - origin.getZ();

        return relX >= LEFT_WALL_X_OFFSET
                && relX <= RIGHT_WALL_X_OFFSET
                && relY >= 0
                && relY <= 5
                && relZ >= START_Z_OFFSET
                && relZ <= END_Z_OFFSET;
    }

    public static boolean isProtectedStructureBlock(BlockPos origin, BlockPos pos) {
        int relX = pos.getX() - origin.getX();
        int relY = pos.getY() - origin.getY();
        int relZ = pos.getZ() - origin.getZ();

        if (relZ < START_Z_OFFSET || relZ > END_Z_OFFSET) {
            return false;
        }

        boolean floor = relY == 0
                && relX >= INNER_MIN_X_OFFSET
                && relX <= INNER_MAX_X_OFFSET;

        boolean ceiling = relY == 5
                && relX >= INNER_MIN_X_OFFSET
                && relX <= INNER_MAX_X_OFFSET;

        boolean sideWall = relY >= 1
                && relY <= 4
                && (relX == LEFT_WALL_X_OFFSET || relX == RIGHT_WALL_X_OFFSET);

        boolean terminalWall = relZ == END_Z_OFFSET
                && relY >= 1
                && relY <= 4
                && relX >= LEFT_WALL_X_OFFSET
                && relX <= RIGHT_WALL_X_OFFSET;

        return floor || ceiling || sideWall || terminalWall;
    }

    public static boolean isInsideWalkableVolume(BlockPos origin, double x, double y, double z) {
        double minX = origin.getX() + INNER_MIN_X_OFFSET + 0.15D;
        double maxX = origin.getX() + INNER_MAX_X_OFFSET + 0.85D;
        double minY = origin.getY() + 0.35D;
        double maxY = origin.getY() + 5.45D;
        // Starts inside the threshold doorway so it overlaps the hall's own
        // volume: there is never a gap that would count as leaving the house.
        double minZ = origin.getZ() + HouseLayout.THRESHOLD_Z - 0.25D;
        // Runs through the far door's own cell. A closed north-facing door keeps its
        // panel at the back of that cell, so a player pressed against it stands
        // about half a block past the wall line and is still in the hallway.
        double maxZ = origin.getZ() + END_Z_OFFSET + 0.95D;

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }
}
