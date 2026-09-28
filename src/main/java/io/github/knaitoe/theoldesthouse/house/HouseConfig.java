package io.github.knaitoe.theoldesthouse.house;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server (per-world) settings for how the House changes after the player's
 * first visit. Mornings here are the House's perceived age: one per dawn
 * after somebody has entered the manor.
 */
public final class HouseConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue RUGS_FALLBACK_AGE;
    public static final ModConfigSpec.IntValue ROOM_MIN_MORNINGS_AFTER_RUGS;
    public static final ModConfigSpec.IntValue ROOM_BASE_CHANCE;
    public static final ModConfigSpec.IntValue ROOM_CHANCE_STEP;
    public static final ModConfigSpec.IntValue HALLWAY_MORNINGS_AFTER_ROOM;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        RUGS_FALLBACK_AGE = builder
                .comment("The rugs change colour the morning after someone first sleeps in a manor bed.",
                        "If nobody has by this perceived age, they change anyway.")
                .defineInRange("rugsFallbackAge", 2, 1, 1000);
        ROOM_MIN_MORNINGS_AFTER_RUGS = builder
                .comment("Mornings after the rugs change before the room between rooms can appear.")
                .defineInRange("roomMinMorningsAfterRugs", 1, 0, 1000);
        ROOM_BASE_CHANCE = builder
                .comment("Percent chance the room appears on its first eligible morning.")
                .defineInRange("roomBaseChance", 35, 0, 100);
        ROOM_CHANCE_STEP = builder
                .comment("Percent added to that chance for every eligible morning it did not appear.")
                .defineInRange("roomChanceStep", 25, 0, 100);
        HALLWAY_MORNINGS_AFTER_ROOM = builder
                .comment("Mornings after the room appears before the impossible hallway opens at the end of the hall.")
                .defineInRange("hallwayMorningsAfterRoom", 3, 0, 1000);
        SPEC = builder.build();
    }

    private HouseConfig() {
    }
}
