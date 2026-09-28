package io.github.knaitoe.theoldesthouse.client;

/**
 * The current fade to black, set from the network thread. Kept free of
 * client-only classes so the common network code can reference it.
 */
public final class HouseFadeState {
    private static final long NANOS_PER_TICK = 50_000_000L;

    public record Fade(long start, long in, long hold, long out) {
    }

    private static volatile Fade fade;

    private HouseFadeState() {
    }

    public static void begin(int fadeIn, int hold, int fadeOut) {
        fade = new Fade(System.nanoTime(),
                Math.max(1, fadeIn) * NANOS_PER_TICK,
                Math.max(0, hold) * NANOS_PER_TICK,
                Math.max(1, fadeOut) * NANOS_PER_TICK);
    }

    static Fade current() {
        return fade;
    }

    static void clear() {
        fade = null;
    }
}
