package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

/** World-owned records survive player cloning, logout, and server restarts. */
public final class FinaleProgress {
    public static final String STATE = "finale_049";
    public enum Phase { UNSEEN, STAIRCASE, FIGHT, COLLAPSE, ESCAPE, RELEASE, HOMEWARD, LOCKED_OUT, ESCAPED, WITNESSED }
    private FinaleProgress() {}
    public static CompoundTag world(MinecraftServer server) { return LabyrinthData.get(server).state(STATE); }
    public static CompoundTag player(MinecraftServer server, UUID id) { return LabyrinthData.get(server).stateEntry(STATE,id.toString()); }
    public static Phase phase(CompoundTag record) {
        try { return Phase.valueOf(record.getString("Phase")); }
        catch (IllegalArgumentException ignored) { return Phase.UNSEEN; }
    }
    public static Phase phase(MinecraftServer server, UUID id) { return phase(player(server, id)); }
    public static void save(MinecraftServer server, UUID id, CompoundTag record) {
        CompoundTag world = world(server); world.put(id.toString(), record.copy());
        LabyrinthData.get(server).setState(STATE, world);
    }
    public static void phase(MinecraftServer server, UUID id, Phase phase) {
        CompoundTag record = player(server, id); record.putString("Phase", phase.name()); save(server, id, record);
    }
    public static boolean committed(Phase phase) { return phase == Phase.FIGHT || phase == Phase.COLLAPSE || phase == Phase.ESCAPE || phase == Phase.RELEASE || phase == Phase.HOMEWARD; }
    public static boolean terminal(Phase phase) { return phase == Phase.LOCKED_OUT || phase == Phase.ESCAPED || phase == Phase.WITNESSED; }
    public static boolean eligible(LabyrinthData data, UUID player) {
        return data.returnDepth(player) >= 12 && data.vignettesVisited(player) >= 2;
    }
}
