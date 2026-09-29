package io.github.knaitoe.theoldesthouse.opening;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;

/**
 * The snapshot camera's view of a level, limited to a box (the copy): what
 * lies outside it is void. Blocks are seen by their map colour; glass,
 * whole-block light sources and water are told apart, and small things
 * without collision (plants, torches, the copy's hidden light) are passed
 * through, their light still showing on what they illuminate.
 */
final class LevelSnapshotScene implements SnapshotRenderer.Scene {
    private static int[] paletteRgb;
    private static byte[] paletteIds;

    private final ServerLevel level;
    private final BlockPos min;
    private final BlockPos max;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    /** For looking about (up a trunk, round its canopy) without moving {@link #pos}, which sample() still uses. */
    private final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
    private long cachedChunk = Long.MIN_VALUE;
    private LevelChunk chunk;
    /** Expensive canopy checks are done once per trunk: every log in a column shares its answer. */
    private final Map<Long, Boolean> naturalTrunkCache = new HashMap<>();

    LevelSnapshotScene(ServerLevel level, BlockPos min, BlockPos max) {
        this.level = level;
        this.min = min;
        this.max = max;
    }

    private LevelChunk chunkAt(int x, int z) {
        long key = ((long) (x >> 4) << 32) | ((z >> 4) & 0xFFFFFFFFL);
        if (key != cachedChunk) {
            cachedChunk = key;
            chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        }
        return chunk;
    }

    private boolean inside(int x, int z) {
        return x >= min.getX() && x <= max.getX() && z >= min.getZ() && z <= max.getZ();
    }

    @Override
    public int sample(int x, int y, int z) {
        if (!inside(x, z) || y < min.getY()) {
            return SnapshotRenderer.pack(SnapshotRenderer.VOID, 0);
        }
        if (y > max.getY()) {
            return SnapshotRenderer.pack(SnapshotRenderer.AIR, 0);
        }
        LevelChunk at = chunkAt(x, z);
        if (at == null) {
            return SnapshotRenderer.pack(SnapshotRenderer.VOID, 0);
        }
        BlockState state = at.getBlockState(pos.set(x, y, z));
        if (state.isAir()) {
            return 0;
        }

        // Navidson is photographing the player's home, not the woodland in
        // front of it. Natural foliage and the vertical trunks that actually
        // lead into that foliage are transparent in the photographic copy.
        // Player-placed/persistent leaves remain, and log construction is not
        // discarded merely for using a wood block.
        if (isNaturalLeaf(state) || isNaturalTreeTrunk(state, x, y, z)) {
            return 0;
        }

        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty() && state.getBlock() instanceof LiquidBlock) {
            return fluid.is(FluidTags.LAVA)
                    ? SnapshotRenderer.pack(SnapshotRenderer.LIGHT, 0xD85A1E)
                    : SnapshotRenderer.pack(SnapshotRenderer.WATER, MapColor.WATER.col);
        }
        if (state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES)) {
            return SnapshotRenderer.pack(SnapshotRenderer.GLASS, 0);
        }
        boolean fullBlock = state.isCollisionShapeFullBlock(level, pos);
        if (fullBlock && state.getLightEmission(level, pos) >= 10) {
            return SnapshotRenderer.pack(SnapshotRenderer.LIGHT, 0);
        }
        if (!fullBlock && state.getCollisionShape(level, pos).isEmpty()) {
            return 0;
        }
        MapColor colour = state.getMapColor(level, pos);
        if (colour == MapColor.NONE) {
            return 0;
        }
        return SnapshotRenderer.pack(SnapshotRenderer.SOLID, colour.col);
    }

    private boolean isNaturalLeaf(BlockState state) {
        return state.is(BlockTags.LEAVES)
                && state.hasProperty(LeavesBlock.PERSISTENT)
                && !state.getValue(LeavesBlock.PERSISTENT);
    }

    /**
     * A natural trunk is a vertical log column that terminates in a real
     * non-persistent leaf canopy. This deliberately does not make every log
     * transparent: cabins, beams and player-built timber walls still belong
     * in the photograph. The column is followed all the way up, however tall
     * (giant jungle and spruce trunks run to thirty blocks), and the answer
     * is kept for every log in it.
     */
    private boolean isNaturalTreeTrunk(BlockState state, int x, int y, int z) {
        if (!isUprightLog(state)) {
            return false;
        }

        Boolean cached = naturalTrunkCache.get(BlockPos.asLong(x, y, z));
        if (cached != null) {
            return cached;
        }

        int top = y;
        Boolean known = null;
        for (int ty = y + 1; ty <= max.getY(); ty++) {
            // Only upright logs are ever cached, so a cached position here is
            // the rest of this same column, already decided.
            Boolean above = naturalTrunkCache.get(BlockPos.asLong(x, ty, z));
            if (above != null) {
                known = above;
                break;
            }
            if (!isUprightLog(stateAt(x, ty, z))) {
                break;
            }
            top = ty;
        }

        boolean natural = known != null ? known : hasNaturalCanopy(x, top, z);
        for (int ty = y; ty <= top; ty++) {
            naturalTrunkCache.put(BlockPos.asLong(x, ty, z), natural);
        }
        return natural;
    }

    private static boolean isUprightLog(BlockState state) {
        return state.is(BlockTags.LOGS)
                && (!state.hasProperty(RotatedPillarBlock.AXIS) || state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y);
    }

    /** At least four natural leaves around and just above the top of a column. */
    private boolean hasNaturalCanopy(int x, int top, int z) {
        int naturalLeaves = 0;
        for (int dy = -2; dy <= 4; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dz * dz > 10) {
                        continue;
                    }
                    if (isNaturalLeaf(stateAt(x + dx, top + dy, z + dz)) && ++naturalLeaves >= 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private BlockState stateAt(int x, int y, int z) {
        if (!inside(x, z) || y < min.getY() || y > max.getY()) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        LevelChunk at = chunkAt(x, z);
        if (at == null) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return at.getBlockState(probe.set(x, y, z));
    }

    @Override
    public int blockLight(int x, int y, int z) {
        if (!inside(x, z) || chunkAt(x, z) == null) {
            return 0;
        }
        return level.getBrightness(LightLayer.BLOCK, pos.set(x, y, z));
    }

    @Override
    public int groundY(int x, int z) {
        if (!inside(x, z)) {
            return Integer.MIN_VALUE;
        }
        LevelChunk at = chunkAt(x, z);
        if (at == null) {
            return Integer.MIN_VALUE;
        }
        return at.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15) + 1;
    }

    /** RGB of every usable map colour at every brightness. */
    static synchronized int[] paletteRgb() {
        buildPalette();
        return paletteRgb;
    }

    /** The packed map-colour byte for each entry of {@link #paletteRgb()}. */
    static synchronized byte[] paletteIds() {
        buildPalette();
        return paletteIds;
    }

    private static void buildPalette() {
        if (paletteRgb != null) {
            return;
        }
        List<Integer> rgb = new ArrayList<>();
        List<Byte> ids = new ArrayList<>();
        for (int id = 1; id < 64; id++) {
            MapColor colour = MapColor.byId(id);
            if (colour == null || colour == MapColor.NONE || colour.col == 0) {
                continue;
            }
            for (MapColor.Brightness brightness : MapColor.Brightness.values()) {
                int r = ((colour.col >> 16) & 255) * brightness.modifier / 255;
                int g = ((colour.col >> 8) & 255) * brightness.modifier / 255;
                int b = (colour.col & 255) * brightness.modifier / 255;
                rgb.add((r << 16) | (g << 8) | b);
                ids.add(colour.getPackedId(brightness));
            }
        }
        int[] rgbArray = new int[rgb.size()];
        byte[] idArray = new byte[ids.size()];
        for (int i = 0; i < rgbArray.length; i++) {
            rgbArray[i] = rgb.get(i);
            idArray[i] = ids.get(i);
        }
        paletteIds = idArray;
        paletteRgb = rgbArray;
    }
}
