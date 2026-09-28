package io.github.knaitoe.theoldesthouse.opening;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server (per-world) settings for the opening sequence. */
public final class OpeningConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MIN_NIGHTS_SLEPT;
    public static final ModConfigSpec.IntValue MIN_DAYS_SINCE_JOIN;
    public static final ModConfigSpec.IntValue DOORSTEP_SEARCH_RADIUS;
    public static final ModConfigSpec.BooleanValue KNOCK_SOUND;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        MIN_NIGHTS_SLEPT = builder
                .comment("Completed sleeps before a player can receive the letter.")
                .defineInRange("minNightsSlept", 2, 0, 1000);
        MIN_DAYS_SINCE_JOIN = builder
                .comment("In-game days that must pass after a player first joins.")
                .defineInRange("minDaysSinceJoin", 3, 0, 1000);
        DOORSTEP_SEARCH_RADIUS = builder
                .comment("Doors within this distance of the bed count towards the player's most-used door.")
                .defineInRange("doorstepSearchRadius", 32, 4, 64);
        KNOCK_SOUND = builder
                .comment("One soft knock, heard only by the recipient, when the letter arrives.")
                .define("knockSound", true);

        SPEC = builder.build();
    }

    private OpeningConfig() {
    }
}
