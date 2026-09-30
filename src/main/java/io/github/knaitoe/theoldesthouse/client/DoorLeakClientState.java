package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.DoorLeaksPayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public final class DoorLeakClientState {
    private static ResourceLocation dimension;
    private static boolean labyrinth;
    private static List<DoorLeaksPayload.Leak> leaks = List.of();
    private static long received;
    private DoorLeakClientState() {}
    public static void accept(DoorLeaksPayload payload) {
        dimension = payload.dimension(); labyrinth = payload.labyrinth();
        leaks = List.copyOf(payload.leaks()); received = System.nanoTime();
    }
    public static boolean current() {
        var level = Minecraft.getInstance().level;
        return level != null && level.dimension().location().equals(dimension) && System.nanoTime() - received < 3_000_000_000L;
    }
    public static List<DoorLeaksPayload.Leak> leaks() { return current() ? leaks : List.of(); }
    public static boolean labyrinth() { return current() && labyrinth; }
    public static void clear() { dimension = null; labyrinth = false; leaks = List.of(); }
}
