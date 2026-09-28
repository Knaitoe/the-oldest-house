package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Finds the Navidsons a site next door to the player and builds the House
 * there.
 *
 * Candidates lie on rings 32 to 96 blocks from the player's bed. The search
 * runs in passes, loosening as it goes:
 * <ol>
 *   <li>flat, open and dry: at most 5 blocks of relief, nothing to clear but
 *   grass and flowers, almost no water;</li>
 *   <li>wooded or rolling: trees and other natural growth may be cleared,
 *   up to 8 blocks of relief;</li>
 *   <li>rough: up to 12 blocks of relief (the foundations reach 12 down) and
 *   more water.</li>
 * </ol>
 * The best site of the first pass that finds any wins. Nothing that looks
 * made by a player is ever cleared: a candidate with planks, glass, stripped
 * logs, player-placed leaves or anything with a block entity in its way is
 * rejected in every pass. Candidates reaching into chunks that
 * are not loaded are skipped; as a last resort a few near ones are loaded.
 */
public final class HouseSpawnManager {
    private static final int MIN_DISTANCE = 32;
    private static final int MAX_DISTANCE = 96;
    private static final int DISTANCE_STEP = 8;
    private static final int ANGLES = 16;

    private record Pass(String name, int maxRelief, int maxWater, boolean clearTrees) {
    }

    private static final Pass[] PASSES = {
            new Pass("open", HouseBuilder.MAX_SITE_RELIEF, 10, false),
            new Pass("wooded", 8, 30, true),
            new Pass("rough", 12, 80, true)
    };

    /** Why the last search went the way it did, for status and logs. */
    public record Report(@Nullable BlockPos origin, String pass, int candidates, int unloaded, int built,
                         int steep, int wet) {
        public String describe() {
            String result = origin == null
                    ? "no site found"
                    : "site at " + origin.getX() + "," + origin.getY() + "," + origin.getZ() + " (" + pass + " pass)";
            return result + "; " + candidates + " candidates: " + unloaded + " in unloaded chunks, "
                    + built + " blocked by something built, " + steep + " too steep, " + wet + " too wet";
        }
    }

    @Nullable
    private static Report lastReport;

    private HouseSpawnManager() {
    }

    @Nullable
    public static Report lastReport() {
        return lastReport;
    }

    /**
     * Spawns The Oldest House near {@code anchor} now, if it does not exist
     * yet (the opening sequence: the Navidsons have moved in next door).
     *
     * @return whether the House exists afterwards
     */
    public static boolean ensureSpawnedNear(ServerLevel level, HouseSavedData data, BlockPos anchor) {
        if (data.isSpawned()) {
            return true;
        }
        Report report = findSite(level, anchor, false);
        if (report.origin() == null) {
            // Last resort: load the chunks around a few near candidates and look again.
            report = findSite(level, anchor, true);
        }
        lastReport = report;
        if (report.origin() == null) {
            TheOldestHouse.LOGGER.warn("No site for The Oldest House near {}: {}. It will try again next morning.",
                    anchor, report.describe());
            return false;
        }
        clearTreetopsAbove(level, report.origin());
        HouseBuilder.build(level, report.origin());
        data.markSpawned(report.origin());
        LabyrinthDoors.syncSealedDoors(level.getServer());
        TheOldestHouse.LOGGER.info("The Oldest House appeared next door to {}: {}.", anchor, report.describe());
        return true;
    }

    private static Report findSite(ServerLevel level, BlockPos anchor, boolean loadNear) {
        int candidates = 0;
        int unloaded = 0;
        int built = 0;
        int steep = 0;
        int wet = 0;

        for (Pass pass : PASSES) {
            BlockPos best = null;
            int bestScore = Integer.MAX_VALUE;
            int maxDistance = loadNear ? MIN_DISTANCE + DISTANCE_STEP : MAX_DISTANCE;
            for (int distance = MIN_DISTANCE; distance <= maxDistance; distance += DISTANCE_STEP) {
                for (int a = 0; a < ANGLES; a++) {
                    double angle = (a + (distance / DISTANCE_STEP) * 0.5D) * Math.PI * 2.0D / ANGLES;
                    int originX = anchor.getX() + (int) Math.round(Math.cos(angle) * distance) - HouseLayout.CENTER_X;
                    int originZ = anchor.getZ() + (int) Math.round(Math.sin(angle) * distance) - HouseLayout.CENTER_Z;
                    candidates++;

                    if (!footprintLoaded(level, originX, originZ)) {
                        if (!loadNear) {
                            unloaded++;
                            continue;
                        }
                        loadFootprint(level, originX, originZ);
                    }

                    int minY = Integer.MAX_VALUE;
                    int maxY = Integer.MIN_VALUE;
                    boolean tooSteep = false;
                    for (int x = HouseLayout.CLEAR_MIN_X; x <= HouseLayout.CLEAR_MAX_X && !tooSteep; x++) {
                        for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.CLEAR_MAX_Z; z++) {
                            int y = pass.clearTrees()
                                    ? groundHeight(level, originX + x, originZ + z)
                                    : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, originX + x, originZ + z);
                            minY = Math.min(minY, y);
                            maxY = Math.max(maxY, y);
                            if (maxY - minY > pass.maxRelief()) {
                                tooSteep = true;
                                break;
                            }
                        }
                    }
                    if (tooSteep) {
                        steep++;
                        continue;
                    }

                    BlockPos origin = new BlockPos(originX, maxY, originZ);
                    boolean clear = pass.clearTrees() ? onlyNatureInTheWay(level, origin) : HouseBuilder.canBuildAt(level, origin);
                    if (!clear) {
                        built++;
                        continue;
                    }
                    int water = footprintWater(level, origin);
                    if (water > pass.maxWater()) {
                        wet++;
                        continue;
                    }
                    int score = (maxY - minY) * 30 + water * 60 + distance;
                    if (score < bestScore) {
                        bestScore = score;
                        best = origin;
                    }
                }
            }
            if (best != null) {
                return new Report(best, pass.name(), candidates, unloaded, built, steep, wet);
            }
        }
        return new Report(null, "", candidates, unloaded, built, steep, wet);
    }

    // ------------------------------------------------------------------
    // Chunks

    private static boolean footprintLoaded(ServerLevel level, int originX, int originZ) {
        for (int cx = SectionPos.blockToSectionCoord(originX + HouseLayout.MIN_X);
             cx <= SectionPos.blockToSectionCoord(originX + HouseLayout.MAX_X); cx++) {
            for (int cz = SectionPos.blockToSectionCoord(originZ + HouseLayout.MIN_Z);
                 cz <= SectionPos.blockToSectionCoord(originZ + HouseLayout.MAX_Z); cz++) {
                if (!level.hasChunk(cx, cz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void loadFootprint(ServerLevel level, int originX, int originZ) {
        for (int cx = SectionPos.blockToSectionCoord(originX + HouseLayout.MIN_X);
             cx <= SectionPos.blockToSectionCoord(originX + HouseLayout.MAX_X); cx++) {
            for (int cz = SectionPos.blockToSectionCoord(originZ + HouseLayout.MIN_Z);
                 cz <= SectionPos.blockToSectionCoord(originZ + HouseLayout.MAX_Z); cz++) {
                level.getChunk(cx, cz);
            }
        }
    }

    // ------------------------------------------------------------------
    // Ground, growth and water

    /** The ground under any trees or undergrowth: the first block down that is not natural growth. */
    static int groundHeight(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y - 1, z);
        for (int i = 0; i < 48 && y > level.getMinBuildHeight(); i++) {
            BlockState state = level.getBlockState(pos.setY(y - 1));
            if (!state.isAir() && !isNaturalGrowth(state)) {
                break;
            }
            y--;
        }
        return y;
    }

    /**
     * Everything in the house's volume, from its floor up to the tops of any
     * trees, is air or natural growth: nothing a player built.
     */
    static boolean onlyNatureInTheWay(ServerLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = HouseLayout.CLEAR_MIN_X; x <= HouseLayout.CLEAR_MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.CLEAR_MAX_Z; z++) {
                int top = Math.min(origin.getY() + HouseLayout.MAX_Y,
                        level.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX() + x, origin.getZ() + z));
                for (int y = origin.getY(); y < Math.max(top, origin.getY() + 6); y++) {
                    BlockState state = level.getBlockState(pos.set(origin.getX() + x, y, origin.getZ() + z));
                    if (!state.isAir() && !isNaturalGrowth(state)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Growth the world put there itself: trees (upright, unstripped logs and
     * leaves that decay), plants, snow. Anything with a block entity, and
     * anything a player would have to place, is not.
     */
    public static boolean isNaturalGrowth(BlockState state) {
        if (state.hasBlockEntity()) {
            return false;
        }
        Block block = state.getBlock();
        if (state.is(BlockTags.LOGS)) {
            return !BuiltInRegistries.BLOCK.getKey(block).getPath().startsWith("stripped_");
        }
        if (state.is(BlockTags.LEAVES)) {
            return !state.hasProperty(LeavesBlock.PERSISTENT) || !state.getValue(LeavesBlock.PERSISTENT);
        }
        return state.canBeReplaced()
                || state.is(BlockTags.REPLACEABLE_BY_TREES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.COCOA)
                || state.is(Blocks.VINE)
                || state.is(Blocks.MOSS_CARPET)
                || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.RED_MUSHROOM)
                || state.is(Blocks.BROWN_MUSHROOM_BLOCK)
                || state.is(Blocks.RED_MUSHROOM_BLOCK)
                || state.is(Blocks.MUSHROOM_STEM)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.MELON);
    }

    /** Trees taller than the house would otherwise be left hanging over the roof. */
    private static void clearTreetopsAbove(ServerLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = HouseLayout.CLEAR_MIN_X; x <= HouseLayout.CLEAR_MAX_X; x++) {
            for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.CLEAR_MAX_Z; z++) {
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX() + x, origin.getZ() + z);
                for (int y = origin.getY() + HouseLayout.MAX_Y + 1; y < top; y++) {
                    BlockState state = level.getBlockState(pos.set(origin.getX() + x, y, origin.getZ() + z));
                    if (!state.isAir() && isNaturalGrowth(state)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    private static int footprintWater(ServerLevel level, BlockPos origin) {
        int water = 0;
        for (int x = 0; x <= 27; x++) {
            for (int z = 0; z <= 26; z++) {
                int worldX = origin.getX() + x;
                int worldZ = origin.getZ() + z;
                int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR, worldX, worldZ);
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
                if (top > surface && level.getFluidState(new BlockPos(worldX, top - 1, worldZ)).is(FluidTags.WATER)) {
                    water++;
                }
            }
        }
        return water;
    }
}
