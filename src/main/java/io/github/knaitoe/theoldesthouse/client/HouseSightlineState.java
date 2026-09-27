package io.github.knaitoe.theoldesthouse.client;

import net.minecraft.core.BlockPos;

public final class HouseSightlineState {
    private static BlockPos origin = BlockPos.ZERO;
    private static boolean revealed;

    private HouseSightlineState() {
    }

    public static synchronized void set(BlockPos newOrigin, boolean newRevealed) {
        origin = newOrigin == null ? BlockPos.ZERO : newOrigin.immutable();
        revealed = newRevealed;
    }

    public static synchronized BlockPos origin() {
        return origin;
    }

    public static synchronized boolean revealed() {
        return revealed;
    }

    public static synchronized void clear() {
        origin = BlockPos.ZERO;
        revealed = false;
    }
}
