package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HouseLifecycleEvents {
    public static final int REQUIRED_SETTLEMENT_NIGHTS = 5;
    public static final int SETTLEMENT_RADIUS = 32;

    private static final long MORNING_WINDOW_TICKS = 1500L;

    private HouseLifecycleEvents() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(player.getServer());
        BlockPos origin = data.housePosition().orElse(BlockPos.ZERO);

        PacketDistributor.sendToPlayer(
                player,
                new HouseSightlineStatePayload(
                        origin,
                        data.isImpossibleDoorRevealed()
                )
        );
    }

    public static void onServerStarted(ServerStartedEvent event) {
        HouseSavedData data = HouseSavedData.get(event.getServer());
        if (data.isSpawned() && !data.isCurrentLayout()) {
            TheOldestHouse.LOGGER.warn(
                    "The Oldest House in this world was generated with layout v{}, but this build uses v{}. "
                            + "Transitions, mirroring and the impossible hallway assume the current layout; "
                            + "use /oldesthouse reset and respawn it (or a fresh world) for reliable behaviour.",
                    data.layoutVersion(),
                    HouseLayout.LAYOUT_VERSION
            );
        }
    }

    /**
     * Static per-server state must not leak into the next world opened in the
     * same game session (single-player).
     */
    public static void onServerStopped(ServerStoppedEvent event) {
        HouseMirrorSyncEvents.clearPending();
        HouseInteriorInitializer.cancel();
        HouseTransitionEvents.clearAll();
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
        if (timeOfDay > MORNING_WINDOW_TICKS) {
            return;
        }

        long currentDay = dayTime / 24000L;
        HouseSavedData data = HouseSavedData.get(level.getServer());

        // The opening sequence owns appearance. Perceived age begins only
        // after somebody has actually entered the manor, so ignoring the
        // invitation cannot reveal the impossible threshold off-screen.
        if (data.advanceHouseAgeForMorning(currentDay)) {
            HouseStageManager.applyCurrentStage(level.getServer(), data);
        }
    }
}
