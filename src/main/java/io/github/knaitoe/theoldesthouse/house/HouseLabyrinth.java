package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.labyrinth.RedRoom;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;

/**
 * The labyrinth: everything past the threshold door at the end of the hall.
 *
 * The manor itself is an ordinary house, and its beds work; so is the room
 * between rooms, the House's first quiet impossibility, which is not the
 * labyrinth yet. Past the threshold (the impossible hallway and every place
 * reached through the labyrinth, including the outside dimension) beds do not: sleeping quietly
 * fails and no spawn point is set. The exception so far is the Red Room's
 * bed, which lets the player sleep (at night, as ever) but still sets no
 * spawn point; later exceptions (Karen's room) belong here too.
 */
public final class HouseLabyrinth {
    private HouseLabyrinth() {
    }

    /** Whether {@code pos} in {@code dimension} lies past the labyrinth threshold. */
    public static boolean isBeyondThreshold(MinecraftServer server, ResourceKey<Level> dimension, BlockPos pos) {
        if (dimension.equals(HouseDimensions.OUTSIDE) || dimension.equals(HouseDimensions.BETWEEN)) {
            return true;
        }
        if (!dimension.equals(HouseDimensions.INTERIOR)) {
            return false;
        }
        BlockPos origin = HouseSavedData.get(server).houseOrigin();
        return origin != null && isBeyondThreshold(origin, pos);
    }

    /** Within the House dimension: past the threshold wall, in the impossible hallway. */
    public static boolean isBeyondThreshold(BlockPos origin, BlockPos pos) {
        return HouseImpossibleHallway.isInteriorOnlyPosition(origin, pos)
                || LabyrinthPlaces.isInStack(origin, pos);
    }

    /** Beds past the threshold do not let anyone sleep, except the Red Room's. */
    public static void onCanPlayerSleep(CanPlayerSleepEvent event) {
        ServerPlayer player = event.getEntity();
        if (RedRoom.isRedRoomBed(event.getLevel(), event.getPos())) {
            return;
        }
        if (isBeyondThreshold(player.server, event.getLevel().dimension(), event.getPos())) {
            event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
        }
    }

    /**
     * Nor do they set a spawn point (vanilla sets it before the sleep check).
     * Commands that force a spawn point still work.
     */
    public static void onSetSpawn(PlayerSetSpawnEvent event) {
        if (event.isForced() || event.getNewSpawn() == null || event.getEntity().getServer() == null) {
            return;
        }
        if (isBeyondThreshold(event.getEntity().getServer(), event.getSpawnLevel(), event.getNewSpawn())) {
            event.setCanceled(true);
        }
    }
}
