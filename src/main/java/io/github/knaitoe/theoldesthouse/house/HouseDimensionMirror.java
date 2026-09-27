package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class HouseDimensionMirror {
    public static final int VIEW_RADIUS = 48;
    public static final int VIEW_BELOW_FLOOR = 6;
    public static final int VIEW_ABOVE_FLOOR = 24;

    private HouseDimensionMirror() {
    }

    public static ServerLevel ensureInitialized(MinecraftServer server, HouseSavedData data) {
        ServerLevel overworld = server.overworld();
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        BlockPos origin = data.housePosition().orElse(null);

        if (interior == null || origin == null) {
            return interior;
        }

        syncAtmosphere(overworld, interior);

        if (!data.isInteriorInitialized()) {
            // Copy the real scene directly with client-update-only flags. We do
            // not copy the House and then clear/rebuild it, because neighbor
            // updates from that process can turn attached blocks into item debris.
            copyInitialSnapshot(overworld, interior, origin);

            // The initial snapshot now carries block-entity data too, including
            // authored lazy loot tables and any player-owned container contents
            // that existed before the first threshold crossing.
            data.markInteriorInitialized();

            if (data.houseAge() >= HouseStageManager.FIRST_IMPOSSIBLE_DOOR_AGE) {
                HouseStageManager.applyCurrentStage(server, data);
            }

            TheOldestHouse.LOGGER.info(
                    "Initialized The Oldest House interior over matching native Overworld terrain."
            );
        }

        return interior;
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

    public static boolean isSharedPosition(BlockPos origin, BlockPos pos) {
        if (HouseImpossibleHallway.isInteriorOnlyPosition(origin, pos)) {
            return false;
        }

        return pos.getX() >= origin.getX() - VIEW_RADIUS
                && pos.getX() <= origin.getX() + HouseBuilder.WIDTH - 1 + VIEW_RADIUS
                && pos.getZ() >= origin.getZ() - VIEW_RADIUS
                && pos.getZ() <= origin.getZ() + HouseBuilder.DEPTH - 1 + VIEW_RADIUS
                && pos.getY() >= origin.getY() - VIEW_BELOW_FLOOR
                && pos.getY() <= origin.getY() + VIEW_ABOVE_FLOOR;
    }

    public static void syncSharedDomesticRegion(
            ServerLevel source,
            ServerLevel target,
            BlockPos origin
    ) {
        int minY = Math.max(
                target.getMinBuildHeight(),
                origin.getY() + HouseBuilder.BASEMENT_FLOOR_Y
        );
        int maxY = Math.min(
                target.getMaxBuildHeight() - 1,
                origin.getY() + HouseBuilder.HEIGHT
        );

        for (int x = origin.getX(); x < origin.getX() + HouseBuilder.WIDTH; x++) {
            for (int z = origin.getZ(); z < origin.getZ() + HouseBuilder.DEPTH; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (!isSharedPosition(origin, pos)) {
                        continue;
                    }

                    copyStateAndBlockEntity(source, target, pos);
                }
            }
        }
    }

    public static void copyState(ServerLevel source, ServerLevel target, BlockPos pos) {
        copyStateAndBlockEntity(source, target, pos);
    }

    /**
     * Copies both the visible block state and the server-side block-entity data.
     *
     * The House dimension is authoritative after initialization, but this method
     * is also used for explicit Overworld player edits before the authoritative
     * reconciliation pass. Metadata such as block-entity id/coordinates is not
     * copied; the target entity keeps its own identity and receives only saved
     * custom/component data.
     */
    public static boolean copyStateAndBlockEntity(
            ServerLevel source,
            ServerLevel target,
            BlockPos pos
    ) {
        BlockState sourceState = source.getBlockState(pos);
        BlockState targetState = target.getBlockState(pos);
        boolean changed = false;

        // Native terrain in both dimensions already matches because the House
        // dimension uses the same Overworld generator and seed. Only overlay
        // actual differences.
        if (!sourceState.equals(targetState)) {
            target.setBlock(pos, sourceState, 2);
            changed = true;
            targetState = target.getBlockState(pos);
        }

        BlockEntity sourceEntity = source.getBlockEntity(pos);
        BlockEntity targetEntity = target.getBlockEntity(pos);

        if (sourceEntity == null || targetEntity == null) {
            return changed;
        }

        // A state replacement should already have created the correct target
        // block entity. Refuse to load data across different entity types rather
        // than risk corrupting a modded container.
        if (sourceEntity.getType() != targetEntity.getType()) {
            return changed;
        }

        CompoundTag sourceData = sourceEntity.saveWithoutMetadata(source.registryAccess());
        CompoundTag targetData = targetEntity.saveWithoutMetadata(target.registryAccess());

        if (sourceData.equals(targetData)) {
            return changed;
        }

        targetEntity.loadWithComponents(sourceData.copy(), target.registryAccess());
        targetEntity.setChanged();

        // Push the refreshed block-entity data to any clients tracking this
        // location. The state itself is unchanged here.
        target.sendBlockUpdated(pos, targetState, targetState, 3);
        return true;
    }

    /**
     * Reconciles the stable domestic footprint from the House dimension back to
     * the Overworld proxy. This deliberately excludes impossible-only geometry.
     */
    public static int reconcileAuthoritativeDomestic(
            ServerLevel interior,
            ServerLevel overworld,
            BlockPos origin
    ) {
        int changed = 0;
        int minY = Math.max(
                overworld.getMinBuildHeight(),
                origin.getY() + HouseBuilder.BASEMENT_FLOOR_Y
        );
        int maxY = Math.min(
                overworld.getMaxBuildHeight() - 1,
                origin.getY() + HouseBuilder.HEIGHT
        );

        for (int x = origin.getX(); x < origin.getX() + HouseBuilder.WIDTH; x++) {
            for (int z = origin.getZ(); z < origin.getZ() + HouseBuilder.DEPTH; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (!isSharedPosition(origin, pos)) {
                        continue;
                    }

                    if (copyStateAndBlockEntity(interior, overworld, pos)) {
                        changed++;
                    }
                }
            }
        }

        return changed;
    }

    private static void copyInitialSnapshot(ServerLevel source, ServerLevel target, BlockPos origin) {
        int minX = origin.getX() - VIEW_RADIUS;
        int maxX = origin.getX() + HouseBuilder.WIDTH - 1 + VIEW_RADIUS;
        int minZ = origin.getZ() - VIEW_RADIUS;
        int maxZ = origin.getZ() + HouseBuilder.DEPTH - 1 + VIEW_RADIUS;
        int minY = Math.max(target.getMinBuildHeight(), origin.getY() - VIEW_BELOW_FLOOR);
        int maxY = Math.min(target.getMaxBuildHeight() - 1, origin.getY() + VIEW_ABOVE_FLOOR);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    copyStateAndBlockEntity(source, target, pos);
                }
            }
        }
    }
}
