package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** Shared consequences for the Indian Lake wing. Visiting alone never supplies a resolution. */
public final class IndianLakeProgress {
    private IndianLakeProgress() {}

    public static boolean wasHunted(LabyrinthData data, UUID player) {
        return data.state(DrownedTown.ID).getCompound("Hunted").getBoolean(player.toString());
    }

    public static void hunted(LabyrinthData data, UUID player) {
        if (wasHunted(data, player)) return;
        CompoundTag state = data.state(DrownedTown.ID), hunted = state.getCompound("Hunted");
        hunted.putBoolean(player.toString(), true);
        state.put("Hunted", hunted);
        data.setState(DrownedTown.ID, state);
    }

    /** The companion shallows vignette may be dealt only after this explorer's actual hunt. */
    public static boolean canDealShallows(LabyrinthData data, UUID player) {
        return wasHunted(data, player) && !hasThrown(data, player);
    }

    public static boolean hasThrown(LabyrinthData data, UUID player) {
        return data.state("indian_lake").getCompound("Boys").contains(player.toString());
    }

    /** Called by the shallows' completed throwing beat, not by merely entering it. */
    public static void completedShallows(LabyrinthData data, UUID player, String name) {
        if (!wasHunted(data, player) || hasThrown(data, player)) return;
        CompoundTag state = data.state("indian_lake"), boys = state.getCompound("Boys");
        boys.putString(player.toString(), name);
        state.put("Boys", boys);
        data.setState("indian_lake", state);
    }

    public static boolean hymnEscaped(LabyrinthData data) {
        return data.state(DrownedTown.ID).getBoolean("RoofOpened");
    }

    /** The cave's next arrival calls this once; the congregation's departure is permanent. */
    public static boolean arriveAtCave(LabyrinthData data) {
        if (!hymnEscaped(data)) return false;
        CompoundTag state = data.state("indian_lake");
        state.putBoolean("DeadOnShore", true);
        data.setState("indian_lake", state);
        return true;
    }

    public static boolean deadOnShore(LabyrinthData data) {
        return data.state("indian_lake").getBoolean("DeadOnShore");
    }
}
