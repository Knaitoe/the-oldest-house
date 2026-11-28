package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

/** World-owned records survive player cloning, logout, and server restarts. */
public final class FinaleProgress {
    public static final String STATE = "finale_049";
    /** IMPRISONED (0.4.84): killed in the fight, they wake in the cell; its one door lets them out of the House for good. */
    public enum Phase { UNSEEN, STAIRCASE, FIGHT, COLLAPSE, ESCAPE, RELEASE, HOMEWARD, LOCKED_OUT, ESCAPED, WITNESSED, IMPRISONED }
    private FinaleProgress() {}
    public static CompoundTag world(MinecraftServer server) { return LabyrinthData.get(server).state(STATE); }
    public static CompoundTag player(MinecraftServer server, UUID id) { return LabyrinthData.get(server).stateEntry(STATE,id.toString()); }
    public static Phase phase(CompoundTag record) { return phaseNamed(record.getString("Phase")); }
    /**
     * 0.4.767: a reader who has not met the finale has no phase written, which is nearly every reader. Enum.valueOf("") throws,
     * and this is asked some fifty times a tick for every player online, wherever they are, so an exception was built, filled
     * with a stack trace and caught a thousand times a second for each of them. The empty name is answered first.
     */
    private static Phase phaseNamed(String name) {
        if (name.isEmpty()) return Phase.UNSEEN;
        try { return Phase.valueOf(name); }
        catch (IllegalArgumentException ignored) { return Phase.UNSEEN; }
    }
    /** Read where it lies: the record is not copied to learn one word of it. */
    public static Phase phase(MinecraftServer server, UUID id) { return phaseNamed(LabyrinthData.get(server).stateEntryString(STATE, id.toString(), "Phase")); }
    public static void save(MinecraftServer server, UUID id, CompoundTag record) {
        CompoundTag world = world(server); world.put(id.toString(), record.copy());
        LabyrinthData.get(server).setState(STATE, world);
    }
    public static void phase(MinecraftServer server, UUID id, Phase phase) {
        CompoundTag record = player(server, id); record.putString("Phase", phase.name()); save(server, id, record);
    }
    public static boolean committed(Phase phase) { return phase == Phase.FIGHT || phase == Phase.COLLAPSE || phase == Phase.ESCAPE || phase == Phase.RELEASE || phase == Phase.HOMEWARD || phase == Phase.IMPRISONED; }
    public static boolean terminal(Phase phase) { return phase == Phase.LOCKED_OUT || phase == Phase.ESCAPED || phase == Phase.WITNESSED; }
    /** 0.4.384 (docs/SEALS_0_4_384.md): three trial marks and four stories heard to their end, not two visited. */
    public static boolean eligible(LabyrinthData data, UUID player) {
        return data.returnDepth(player) >= 12 && TrialMarks.sealed(data, player);
    }
}
