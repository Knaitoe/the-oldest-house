package io.github.knaitoe.theoldesthouse.opening;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.LiquidBlock;
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
    private long cachedChunk = Long.MIN_VALUE;
    private LevelChunk chunk;

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

        // Navidson is photographing the player's home, not the canopy around
        // it. Leaves are transient natural occlusion and were previously
        // treated as solid "house" pixels by the camera scorer, which could
        // produce a beautifully framed photograph of six oak trees and about
        // three pixels of somebody's roof. Ignore foliage in the copied
        // photographic scene only; the player's real world is untouched.
        if (state.is(BlockTags.LEAVES)
                && state.hasProperty(LeavesBlock.PERSISTENT)
                && !state.getValue(LeavesBlock.PERSISTENT)) {
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
