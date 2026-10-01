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
        if(!bound&&endingTick==0)ClapCueClient.clear();
    }
    public static boolean bound() { return bound; }
    public static boolean ending() { return endingTick > 0; }
    public static boolean revealed(){return ending()&&elapsed()>=io.github.knaitoe.theoldesthouse.labyrinth.ClapGameClock.REVEAL_TICK;}
    public static float yaw() { return yaw; }
    public static float elapsed() {
        return Math.max(0, endingTick - 1) + (System.nanoTime() - received) / 50_000_000.0F;
    }
    public static void clear() { bound = false; endingTick = 0; ClapCueClient.clear(); }
}
