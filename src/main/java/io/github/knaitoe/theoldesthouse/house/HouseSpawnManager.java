package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;

public final class HouseSpawnManager {
    private static final int MIN_DISTANCE = 32;
    private static final int MAX_DISTANCE = 64;
    private static final int SITE_ATTEMPTS_PER_SUCCESSFUL_ROLL = 24;

    private static final double FIRST_ELIGIBLE_DAY_CHANCE = 0.05D;
    private static final double DAILY_CHANCE_INCREASE = 0.05D;
    private static final double MAX_DAILY_CHANCE = 0.75D;

    private HouseSpawnManager() {
    }

    public static void tryNaturalMorningSpawn(ServerLevel level, HouseSavedData data, long currentDay) {
        if (!data.isEligible() || data.isSpawned()) {
            return;
        }

        // The same morning that establishes eligibility can never spawn the House.
        if (currentDay <= data.eligibleSinceDay()) {
            return;
        }

        if (!data.claimSpawnRoll(currentDay)) {
            return;
        }

        long eligibleDays = Math.max(1L, currentDay - data.eligibleSinceDay());
        double chance = Math.min(
                MAX_DAILY_CHANCE,
                FIRST_ELIGIBLE_DAY_CHANCE + (eligibleDays - 1L) * DAILY_CHANCE_INCREASE
        );

        if (level.getRandom().nextDouble() >= chance) {
            return;
        }

        Optional<BlockPos> anchor = data.anchorPosition();
        if (anchor.isEmpty()) {
            return;
        }

        Optional<BlockPos> origin = findSafeOrigin(level, anchor.get());
        if (origin.isEmpty()) {
            TheOldestHouse.LOGGER.debug("House appearance roll succeeded, but no safe nearby site was found.");
            return;
        }

        HouseBuilder.build(level, origin.get());
        data.markSpawned(origin.get());

        // Deliberately no player-facing message. The discovery is the event.
        TheOldestHouse.LOGGER.info("The House appeared at {}.", origin.get());
    }

    private static Optional<BlockPos> findSafeOrigin(ServerLevel level, BlockPos anchor) {
        for (int attempt = 0; attempt < SITE_ATTEMPTS_PER_SUCCESSFUL_ROLL; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
            int distance = MIN_DISTANCE + level.getRandom().nextInt(MAX_DISTANCE - MIN_DISTANCE + 1);

            int centerX = anchor.getX() + (int) Math.round(Math.cos(angle) * distance);
            int centerZ = anchor.getZ() + (int) Math.round(Math.sin(angle) * distance);

            int originX = centerX - HouseBuilder.WIDTH / 2;
            int originZ = centerZ - HouseBuilder.DEPTH / 2;

            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int x = 0; x < HouseBuilder.WIDTH; x++) {
                for (int z = 0; z < HouseBuilder.DEPTH; z++) {
                    int y = level.getHeight(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            originX + x,
                            originZ + z
                    );
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }

            if (maxY - minY > 2) {
                continue;
            }

            BlockPos origin = new BlockPos(originX, maxY, originZ);
            if (HouseBuilder.canBuildAt(level, origin)) {
                return Optional.of(origin);
            }
        }

        return Optional.empty();
    }
}
