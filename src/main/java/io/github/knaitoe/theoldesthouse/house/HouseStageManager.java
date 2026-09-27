package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import io.github.knaitoe.theoldesthouse.network.HouseSightlineStatePayload;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public final class HouseStageManager {
    public static final int FIRST_IMPOSSIBLE_DOOR_AGE = 3;

    private HouseStageManager() {
    }

    public static void applyCurrentStage(MinecraftServer server, HouseSavedData data) {
        if (!data.isSpawned() || data.houseAge() < FIRST_IMPOSSIBLE_DOOR_AGE) {
            return;
        }

        BlockPos origin = data.housePosition().orElse(null);
        if (origin == null) {
            return;
        }

        ServerLevel interior = HouseInteriorInitializer.ensureInitialized(server, data);
        if (interior == null) {
            return;
        }

        if (!data.isImpossibleDoorRevealed()) {
            HouseBuilder.revealImpossibleDoor(interior, origin);
            HouseImpossibleHallway.build(interior, origin);
            data.markImpossibleDoorRevealed();

            // The impossible hallway itself remains interior-only, but the
            // threshold wall at the end of the hall is ordinary domestic architecture
            // and must immediately agree in both dimensions.
            HouseDimensionMirror.reconcileAuthoritativeDomestic(
                    interior,
                    server.overworld(),
                    origin
            );

            PacketDistributor.sendToAllPlayers(
                    new HouseSightlineStatePayload(origin, true)
            );

            TheOldestHouse.LOGGER.info(
                    "The first impossible doorway and direct hallway in The Oldest House have been revealed at perceived age {}.",
                    data.houseAge()
            );
        }
    }
}
