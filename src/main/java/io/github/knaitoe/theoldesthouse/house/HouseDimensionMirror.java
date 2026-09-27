package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Keeps the Overworld shell of The Oldest House consistent with the
 * authoritative House dimension.
 *
 * Block states flow both ways (explicit Overworld edits in, authoritative
 * domestic state out). Container contents never cross: they live only in the
 * House dimension, and Overworld proxies stay empty and cannot be opened,
 * broken or blown up. Copying inventories between two live containers let
 * items be taken from one copy and restored from the other.
 */
public final class HouseDimensionMirror {
    public static final int VIEW_RADIUS = 48;
    public static final int VIEW_BELOW_FLOOR = 6;
    public static final int VIEW_ABOVE_FLOOR = 24;

    /** Client updates only: no neighbour cascades, no drops. */
    static final int MIRROR_FLAGS = Block.UPDATE_CLIENTS;

    private HouseDimensionMirror() {
    }

    public static void syncAtmosphere(ServerLevel overworld, ServerLevel interior) {
        interior.setDayTime(overworld.getDayTime());
        interior.setWeatherParameters(
                0,
                6000,
                overworld.isRaining(),
                overworld.isThundering()
        );
    }

    /** True inside the mirrored view region around the house (both dimensions). */
    public static boolean isSharedPosition(BlockPos origin, BlockPos pos) {
        return isNearHouse(origin, pos, VIEW_RADIUS)
                && pos.getY() >= origin.getY() - VIEW_BELOW_FLOOR
                && pos.getY() <= origin.getY() + VIEW_ABOVE_FLOOR
                && !HouseImpossibleHallway.isInteriorOnlyPosition(origin, pos);
    }

    /** Cheap horizontal bounds test against the house envelope plus a margin. */
    public static boolean isNearHouse(BlockPos origin, BlockPos pos, int margin) {
        int relX = pos.getX() - origin.getX();
        int relZ = pos.getZ() - origin.getZ();
        return relX >= HouseLayout.MIN_X - margin
                && relX <= HouseLayout.MAX_X + margin
                && relZ >= HouseLayout.MIN_Z - margin
                && relZ <= HouseLayout.MAX_Z + margin;
    }

    /** True inside the house's own generated envelope (the domestic footprint). */
    public static boolean isDomesticPosition(BlockPos origin, BlockPos pos) {
        int relY = pos.getY() - origin.getY();
        return isNearHouse(origin, pos, 0)
                && relY >= HouseLayout.MIN_Y
                && relY <= HouseLayout.MAX_Y;
    }

    /**
     * Whether a block entity carries items. These never cross dimensions,
     * whether through a container interface, {@link Clearable} (lecterns,
     * jukeboxes, campfires) or an item-handler capability (modded storage).
     */
    public static boolean isInventoryBearing(Level level, BlockEntity blockEntity) {
        if (blockEntity instanceof Clearable) {
            return true;
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), null) != null;
    }

    /**
     * Copies the block state and any non-inventory block-entity data from one
     * dimension to the other at the same position.
     *
     * @return whether anything in the target changed
     */
    public static boolean copyStateAndBlockEntity(ServerLevel source, ServerLevel target, BlockPos pos) {
        BlockState sourceState = source.getBlockState(pos);
        boolean changed = false;

        // Native terrain in both dimensions already matches because the House
        // dimension uses the same Overworld generator and seed. Only overlay
        // actual differences.
        if (sourceState != target.getBlockState(pos)) {
            target.setBlock(pos, sourceState, MIRROR_FLAGS);
            changed = true;
        }

        return copyBlockEntityData(source, target, pos) || changed;
    }

    /**
     * Copies saved block-entity data (signs, banners, skulls...) but never
     * inventories. Metadata such as id/coordinates is not copied; the target
     * keeps its own identity and receives only custom/component data.
     */
    static boolean copyBlockEntityData(ServerLevel source, ServerLevel target, BlockPos pos) {
        BlockEntity sourceEntity = source.getBlockEntity(pos);
        if (sourceEntity == null) {
            return false;
        }
        BlockEntity targetEntity = target.getBlockEntity(pos);

        // A state replacement should already have created the correct target
        // block entity. Refuse to load data across different entity types
        // rather than risk corrupting a modded block.
        if (targetEntity == null || sourceEntity.getType() != targetEntity.getType()) {
            return false;
        }
        if (isInventoryBearing(source, sourceEntity) || isInventoryBearing(target, targetEntity)) {
            return false;
        }

        CompoundTag sourceData = sourceEntity.saveWithoutMetadata(source.registryAccess());
        CompoundTag targetData = targetEntity.saveWithoutMetadata(target.registryAccess());
        if (sourceData.equals(targetData)) {
            return false;
        }

        targetEntity.loadWithComponents(sourceData, target.registryAccess());
        targetEntity.setChanged();

        BlockState state = target.getBlockState(pos);
        target.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        return true;
    }

    /**
     * Pushes the authoritative domestic footprint from the House dimension to
     * the Overworld proxy. Impossible-only geometry is excluded.
     *
     * Reads chunk storage directly (one chunk lookup per column rather than
     * per block) and only serialises non-inventory block entities, which are
     * few; containers were previously saved to NBT twice a second each.
     *
     * @return number of positions whose state or data changed
     */
    public static int reconcileAuthoritativeDomestic(ServerLevel interior, ServerLevel overworld, BlockPos origin) {
        int minX = origin.getX() + HouseLayout.MIN_X;
        int maxX = origin.getX() + HouseLayout.MAX_X;
        int minZ = origin.getZ() + HouseLayout.CLEAR_MIN_Z;
        int maxZ = origin.getZ() + HouseLayout.MAX_Z;
        int minY = Math.max(overworld.getMinBuildHeight(), origin.getY() + HouseLayout.MIN_Y);
        int maxY = Math.min(overworld.getMaxBuildHeight() - 1, origin.getY() + HouseLayout.MAX_Y);

        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                LevelChunk source = interior.getChunkSource().getChunkNow(cx, cz);
                LevelChunk target = overworld.getChunkSource().getChunkNow(cx, cz);
                if (source == null || target == null) {
                    // Unloaded on either side: nothing observable to correct.
                    continue;
                }

                int x0 = Math.max(minX, cx << 4);
                int x1 = Math.min(maxX, (cx << 4) + 15);
                int z0 = Math.max(minZ, cz << 4);
                int z1 = Math.min(maxZ, (cz << 4) + 15);

                for (int x = x0; x <= x1; x++) {
                    for (int z = z0; z <= z1; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            pos.set(x, y, z);
                            BlockState state = source.getBlockState(pos);
                            if (state == target.getBlockState(pos)
                                    || HouseImpossibleHallway.isInteriorOnlyPosition(origin, pos)) {
                                continue;
                            }
                            overworld.setBlock(pos.immutable(), state, MIRROR_FLAGS);
                            changed++;
                        }
                    }
                }

                for (BlockEntity blockEntity : java.util.List.copyOf(source.getBlockEntities().values())) {
                    BlockPos bePos = blockEntity.getBlockPos();
                    if (bePos.getX() < x0 || bePos.getX() > x1
                            || bePos.getZ() < z0 || bePos.getZ() > z1
                            || bePos.getY() < minY || bePos.getY() > maxY
                            || HouseImpossibleHallway.isInteriorOnlyPosition(origin, bePos)
                            || isInventoryBearing(interior, blockEntity)) {
                        continue;
                    }
                    if (copyBlockEntityData(interior, overworld, bePos)) {
                        changed++;
                    }
                }
            }
        }

        return changed;
    }
}
