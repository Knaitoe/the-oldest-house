package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.ClapGamePayload;

/** Server-authorized cloth lock and short failure animation; no timer is trusted to the client. */
public final class ClapGameClientState {
    private static boolean bound;
    private static int endingTick;
    private static long received;
    private static float yaw;
    private ClapGameClientState() {}
    public static void accept(ClapGamePayload payload) {
        bound = payload.bound();
        endingTick = payload.endingTick();
        yaw = payload.yaw();
        received = System.nanoTime();
    }
    public static boolean bound() { return bound; }
    public static boolean ending() { return bound && endingTick > 0; }
    public static float yaw() { return yaw; }
    public static float elapsed() {
        return Math.max(0, endingTick - 1) + (System.nanoTime() - received) / 50_000_000.0F;
    }
    public static void clear() { bound = false; endingTick = 0; }
}
