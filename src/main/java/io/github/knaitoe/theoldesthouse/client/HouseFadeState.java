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

    /**
     * A fade begun while another is showing starts from however dark that one has made the screen, so one fade following another never
     * flashes back to clear first (0.4.531: waking from the Pell house's camp bed ends a held fade with a short one).
     */
    public static void begin(int fadeIn, int hold, int fadeOut) {
        long now = System.nanoTime(), in = Math.max(1, fadeIn) * NANOS_PER_TICK;
        float from = alpha(fade, now);
        fade = new Fade(now - (long) (from * in),
                in,
                Math.max(0, hold) * NANOS_PER_TICK,
                Math.max(1, fadeOut) * NANOS_PER_TICK);
    }

    /** How dark a fade makes the screen at this moment, from 0 (clear) to 1 (black). */
    static float alpha(Fade f, long now) {
        if (f == null) {
            return 0.0F;
        }
        long t = now - f.start();
        if (t < f.in()) {
            return (float) t / f.in();
        }
        if (t < f.in() + f.hold()) {
            return 1.0F;
        }
        if (t < f.in() + f.hold() + f.out()) {
            return 1.0F - (float) (t - f.in() - f.hold()) / f.out();
        }
        return 0.0F;
    }

    static Fade current() {
        return fade;
    }

    static void clear() {
        fade = null;
    }
}
