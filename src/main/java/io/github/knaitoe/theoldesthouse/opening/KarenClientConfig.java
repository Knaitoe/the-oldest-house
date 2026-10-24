package io.github.knaitoe.theoldesthouse.opening;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client settings for Karen's room (0.4.121). Both read and write only this computer's own files; nothing they touch is
 * ever sent to a server.
 */
public final class KarenClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SCREENSHOTS;
    public static final ModConfigSpec.BooleanValue SNAPSHOTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SCREENSHOTS = builder
                .comment("Karen's room projects your own F2 screenshots, read from this computer's screenshots folder. They stay on this computer.")
                .define("projectScreenshots", true);
        SNAPSHOTS = builder
                .comment("Now and then, take a small, low-resolution picture of your screen into the_oldest_house/karen in the game folder, for Karen's room to project. They stay on this computer.")
                .define("secretSnapshots", true);
        SPEC = builder.build();
    }

    private KarenClientConfig() {}
}
