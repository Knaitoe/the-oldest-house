package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthHazards;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthLighting;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthCampsite;
import io.github.knaitoe.theoldesthouse.labyrinth.Growl;
import io.github.knaitoe.theoldesthouse.labyrinth.HideAndClap;
import io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette;
import io.github.knaitoe.theoldesthouse.labyrinth.ModelHome;
import io.github.knaitoe.theoldesthouse.labyrinth.MotherOfStrays;
import io.github.knaitoe.theoldesthouse.labyrinth.TellTaleFloorboards;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

public final class HouseLifecycleEvents {
    private HouseLifecycleEvents() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        HouseSavedData data = HouseSavedData.get(player.getServer());
        BlockPos origin = data.housePosition().orElse(BlockPos.ZERO);

        // The hallway's sightline is drawn against the current layout: an
        // outdated House shows none.
        HousePackets.send(
                player,
                new HouseSightlineStatePayload(
                        origin,
                        data.isImpossibleDoorRevealed() && !data.isOutdated()
                )
        );

        if (data.isOutdated() && player.hasPermissions(2)) {
            // Only someone who can do something about it is told; to
            // everyone else the House is simply a house.
            player.sendSystemMessage(Component.literal(
                    "The Oldest House here was built with layout v" + data.layoutVersion()
                            + ", but this version of the mod uses v" + HouseLayout.LAYOUT_VERSION
                            + ". It has gone quiet: an ordinary building, with nothing inside working. "
                            + "Use /oldesthouse reset and let it respawn (or start a new world) to bring it back.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    public static void onServerStarted(ServerStartedEvent event) {
        HouseSavedData data = HouseSavedData.get(event.getServer());
        HouseShifts.refreshCache(data);
        LabyrinthDoors.ensureHallwayDoor(event.getServer());
        HouseBetweenRoom.ensurePocket(event.getServer());
        if (data.isOutdated()) {
            TheOldestHouse.LOGGER.warn(
                    "The Oldest House in this world was generated with layout v{}, but this build uses v{}. "
                            + "It stands down entirely (no crossings, mirroring, mornings, rooms or labyrinth) "
                            + "and anyone inside is returned to the Overworld spawn. "
                            + "Use /oldesthouse reset and let it respawn (or a fresh world) to bring it back.",
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
        LabyrinthHazards.clearAll();
        LabyrinthLighting.clearAll();
        LabyrinthCampsite.clearAll();
        TellTaleFloorboards.clearAll();
        HideAndClap.clearAll();
        ModelHome.clearAll();
        HarriganVignette.clearAll(event.getServer());
        MotherOfStrays.clearAll();
        io.github.knaitoe.theoldesthouse.opening.CompanionOrders.clearAll();
        Growl.clearAll();
        io.github.knaitoe.theoldesthouse.labyrinth.FinaleController.clearAll();
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

