package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthDoors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload;
import net.minecraft.server.level.ServerLevel;

public final class HouseStageManager {
    private HouseStageManager() {
    }

    /** Opens the door at the end of the hall onto the impossible hallway, now. */
    public static void revealHallway(MinecraftServer server, HouseSavedData data) {
        BlockPos origin = data.houseOrigin();
        if (origin == null || data.isImpossibleDoorRevealed()) {
            return;
        }

        ServerLevel interior = HouseInteriorInitializer.ensureInitialized(server, data);
        if (interior == null) {
            return;
        }

        HouseBuilder.revealImpossibleDoor(interior, origin);
        HouseImpossibleHallway.build(interior, origin);
        data.markImpossibleDoorRevealed();
        // A deepened hall's extra block becomes the hallway's threshold.
        HouseShifts.refreshCache(data);
        // And its far wall has a door, into the labyrinth.
        LabyrinthDoors.ensureHallwayDoor(server);

        // The impossible hallway itself remains interior-only, but the
        // threshold wall at the end of the hall is ordinary domestic architecture
        // and must immediately agree in both dimensions.
        HouseDimensionMirror.reconcileAuthoritativeDomestic(
                interior,
                server.overworld(),
                origin
        );

        HousePackets.sendToAll(server, new HouseSightlineStatePayload(origin, true));

        TheOldestHouse.LOGGER.info(
                "The first impossible doorway and direct hallway in The Oldest House have been revealed at perceived age {}.",
                data.houseAge()
        );
    }
}
