package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

public final class HouseLifecycleEvents {
    public static final int REQUIRED_SETTLEMENT_NIGHTS = 5;
    public static final int SETTLEMENT_RADIUS = 32;

    private static final long MORNING_WINDOW_TICKS = 1500L;

    private HouseLifecycleEvents() {
    }

    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        long dayTime = level.getDayTime();
        long timeOfDay = Math.floorMod(dayTime, 24000L);

        // Leaving a bed manually during the night also fires PlayerWakeUpEvent.
        // Only the post-night-skip morning window counts as settlement residency.
        if (timeOfDay > MORNING_WINDOW_TICKS) {
            return;
        }

        long currentDay = dayTime / 24000L;
        HouseSavedData data = HouseSavedData.get(level.getServer());
        boolean wasEligible = data.isEligible();

        int nights = data.recordSettlementNight(
                player.blockPosition(),
                currentDay,
                SETTLEMENT_RADIUS,
                REQUIRED_SETTLEMENT_NIGHTS
        );

        if (!wasEligible && data.isEligible()) {
            TheOldestHouse.LOGGER.info(
                    "House eligibility established after {} settlement nights near {}.",
                    nights,
                    data.anchorPosition().orElse(BlockPos.ZERO)
            );
        }

        HouseSpawnManager.tryNaturalMorningSpawn(level, data, currentDay);
    }
}
