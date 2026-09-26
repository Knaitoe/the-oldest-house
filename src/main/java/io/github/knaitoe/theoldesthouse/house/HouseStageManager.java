package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

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

        ServerLevel interior = HouseDimensionMirror.ensureInitialized(server, data);
        if (interior == null) {
            return;
        }

        if (!data.isImpossibleDoorRevealed()) {
            HouseBuilder.revealImpossibleDoor(interior, origin);
            data.markImpossibleDoorRevealed();
            TheOldestHouse.LOGGER.info(
                    "The first impossible doorway in The Oldest House has been revealed at perceived age {}.",
                    data.houseAge()
            );
        }
    }
}
