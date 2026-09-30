package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** The room is reserved on entry; the minute starts only when its owner wears the cloth. */
public final class ClapGameClock {
    public static final int LIMIT_TICKS = 20 * 60;
    public static final int ENDING_TICKS = 24;
    public static final int TWIST_TICK = 20;
    private final UUID owner;
    private long equippedAt = -1;
    private boolean completed;

    public ClapGameClock(UUID owner) { this.owner = owner; }
    public UUID owner() { return owner; }
    public boolean started() { return equippedAt >= 0; }
    public boolean bound() { return started() && !completed; }
    public boolean completed() { return completed; }
    public boolean equip(UUID player, long now) {
        if (!owner.equals(player) || completed || started()) return false;
        equippedAt = now;
        return true;
    }
    public boolean expired(long now) {
        return bound() && now - equippedAt >= LIMIT_TICKS;
    }
    public boolean complete(UUID player, long now) {
        if (!owner.equals(player) || !started() || completed || expired(now)) return false;
        completed = true;
        return true;
    }
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Player", owner);
        tag.putLong("EquippedAt", equippedAt);
        tag.putBoolean("Completed", completed);
        return tag;
    }
    public static ClapGameClock load(CompoundTag tag) {
        ClapGameClock clock = new ClapGameClock(tag.getUUID("Player"));
        clock.equippedAt = tag.contains("EquippedAt") ? tag.getLong("EquippedAt") : -1;
        clock.completed = tag.getBoolean("Completed");
        return clock;
    }
}
