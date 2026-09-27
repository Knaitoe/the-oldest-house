package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.core.BlockPos;

/**
 * Client copy of the House origin and reveal state. Written from the network
 * thread and read every frame by the renderer, so it is published as one
 * immutable snapshot rather than guarded by a lock.
 */
public final class HouseSightlineState {
    private static final Snapshot EMPTY = new Snapshot(BlockPos.ZERO, false);

    private static volatile Snapshot snapshot = EMPTY;

    private HouseSightlineState() {
    }

    public static void set(BlockPos newOrigin, boolean newRevealed) {
        snapshot = new Snapshot(newOrigin == null ? BlockPos.ZERO : newOrigin.immutable(), newRevealed);
    }

    public static BlockPos origin() {
        return snapshot.origin();
    }

    public static boolean revealed() {
        return snapshot.revealed();
    }

    public static void clear() {
        snapshot = EMPTY;
    }

    private record Snapshot(BlockPos origin, boolean revealed) {
    }
}
