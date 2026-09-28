package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;

public final class HouseSpawnManager {
    private static final int MIN_DISTANCE = 32;
    private static final int MAX_DISTANCE = 64;
    private static final int SITE_ATTEMPTS_PER_SUCCESSFUL_ROLL = 40;

    private HouseSpawnManager() {
    }

    public static void tryNaturalMorningSpawn(ServerLevel level, HouseSavedData data, long currentDay) {
        if (!data.isEligible() || data.isSpawned()) {
            return;
        }

        // The same morning that establishes eligibility can never spawn The Oldest House.
        if (currentDay <= data.eligibleSinceDay()) {
            return;
        }

        if (!data.claimSpawnRoll(currentDay)) {
            return;
        }

        double chance = data.spawnChancePercent() / 100.0D;

        if (level.getRandom().nextDouble() >= chance) {
            int nextChance = data.adjustSpawnChance(level.getRandom());
            TheOldestHouse.LOGGER.debug(
                    "The Oldest House did not appear. Next hidden appearance chance is {}%.",
                    nextChance
            );
            return;
        }

        Optional<BlockPos> anchor = data.anchorPosition();
        if (anchor.isEmpty()) {
            data.adjustSpawnChance(level.getRandom());
            return;
        }

        Optional<BlockPos> origin = findSafeOrigin(level, anchor.get());
        if (origin.isEmpty()) {
            int nextChance = data.adjustSpawnChance(level.getRandom());
            TheOldestHouse.LOGGER.debug(
                    "The Oldest House appearance roll succeeded, but no safe nearby site was found. Next chance is {}%.",
                    nextChance
            );
            return;
        }

        HouseBuilder.build(level, origin.get());
        data.markSpawned(origin.get());

        // Deliberately no player-facing message. The discovery is the event.
        TheOldestHouse.LOGGER.info("The Oldest House appeared at {}.", origin.get());
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
        Optional<BlockPos> origin = findSafeOrigin(level, anchor);
        if (origin.isEmpty()) {
            TheOldestHouse.LOGGER.info("No safe site for The Oldest House near {} yet.", anchor);
            return false;
        }
        HouseBuilder.build(level, origin.get());
        data.markSpawned(origin.get());
        TheOldestHouse.LOGGER.info("The Oldest House appeared at {} (next door to {}).", origin.get(), anchor);
        return true;
    }

    private static Optional<BlockPos> findSafeOrigin(ServerLevel level, BlockPos anchor) {
        BlockPos bestOrigin = null;
        int bestScore = Integer.MAX_VALUE;

        for (int attempt = 0; attempt < SITE_ATTEMPTS_PER_SUCCESSFUL_ROLL; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
            int distance = MIN_DISTANCE
                    + level.getRandom().nextInt(MAX_DISTANCE - MIN_DISTANCE + 1);

            int centerX = anchor.getX()
                    + (int) Math.round(Math.cos(angle) * distance);
            int centerZ = anchor.getZ()
                    + (int) Math.round(Math.sin(angle) * distance);

            int originX = centerX - HouseLayout.CENTER_X;
            int originZ = centerZ - HouseLayout.CENTER_Z;

            // Relief across the whole claimed footprint. Cheap heightmap reads
            // reject most candidates before any block is inspected.
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            boolean tooSteep = false;
            for (int x = HouseLayout.CLEAR_MIN_X; x <= HouseLayout.CLEAR_MAX_X && !tooSteep; x++) {
                for (int z = HouseLayout.CLEAR_MIN_Z; z <= HouseLayout.CLEAR_MAX_Z; z++) {
                    int y = level.getHeight(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            originX + x,
                            originZ + z
                    );
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    if (maxY - minY > HouseBuilder.MAX_SITE_RELIEF) {
                        tooSteep = true;
                        break;
                    }
                }
            }
            if (tooSteep) {
                continue;
            }

            BlockPos origin = new BlockPos(originX, maxY, originZ);
            if (!HouseBuilder.canBuildAt(level, origin)) {
                continue;
            }

            int score = HouseBuilder.siteScore(level, origin, maxY - minY);
            if (score < bestScore) {
                bestScore = score;
                bestOrigin = origin;
                if (score == 0) {
                    break; // Perfectly flat, dry and level with the approach.
                }
            }
        }

        return Optional.ofNullable(bestOrigin);
    }

}
