package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.Growl;
import io.github.knaitoe.theoldesthouse.labyrinth.HideAndClap;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HouseLifecycleEvents {
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
        HouseShifts.refreshCache(data);
        LabyrinthDoors.ensureHallwayDoor(event.getServer());
        HouseBetweenRoom.ensurePocket(event.getServer());
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
        HouseExteriorEntityMirror.clear(event.getServer());
        HouseBetweenRoom.clearAll();
        HouseShifts.clearCache();
        HouseChunkKeeper.release(event.getServer());
        LabyrinthDoors.clearAll();
        TellTaleFloorboards.clearAll();
        HideAndClap.clearAll();
        Growl.clearAll();
    }

    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !HouseDays.isMorningWake(player)) {
            // Leaving a bed during the night also fires PlayerWakeUpEvent.
            return;
        }

        // The opening sequence owns appearance. Perceived age begins only
        // after somebody has actually entered the manor, so ignoring the
        // invitation cannot reveal anything off-screen.
        boolean inManor = HouseDays.isInManor(player);
        List<String> changes = HouseProgression.onMorningWake(
                player.server,
                HouseCalendar.today(player.server),
                inManor
        );
        if (!changes.isEmpty()) {
            TheOldestHouse.LOGGER.info("Morning at The Oldest House: {}.", String.join("; ", changes));
        }
        if (inManor) {
            Growl.onManorWake(player);
        }
    }
}
