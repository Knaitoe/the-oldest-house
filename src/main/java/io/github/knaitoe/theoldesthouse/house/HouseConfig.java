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
    public static final ModConfigSpec.IntValue ROOM_FIRST_AGE;
    public static final ModConfigSpec.IntValue ROOM_MIN_MORNINGS_AFTER_RUGS;
    public static final ModConfigSpec.IntValue ROOM_BASE_CHANCE;
    public static final ModConfigSpec.IntValue ROOM_CHANCE_STEP;
    public static final ModConfigSpec.IntValue SHIFTS_BEFORE_HALLWAY;
    public static final ModConfigSpec.IntValue HALLWAY_BASE_CHANCE;
    public static final ModConfigSpec.IntValue HALLWAY_CHANCE_STEP;
    public static final ModConfigSpec.IntValue SHIFT_BASE_CHANCE;
    public static final ModConfigSpec.IntValue SHIFT_CHANCE_STEP;
    public static final ModConfigSpec.IntValue SHIFT_BASE_CHANCE_AFTER_HALLWAY;
    public static final ModConfigSpec.IntValue SHIFT_CHANCE_STEP_AFTER_HALLWAY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        RUGS_FALLBACK_AGE = builder
                .comment("The rugs change colour the morning after someone first sleeps in a manor bed.",
                        "If nobody has by this perceived age, they change anyway.")
                .defineInRange("rugsFallbackAge", 2, 1, 1000);
        ROOM_FIRST_AGE = builder
                .comment("The first House morning on which the room between rooms can be rolled for.")
                .defineInRange("roomFirstAge", 3, 1, 1000);
        ROOM_MIN_MORNINGS_AFTER_RUGS = builder
                .comment("Mornings after the rugs change before the room between rooms can appear.")
                .defineInRange("roomMinMorningsAfterRugs", 1, 0, 1000);
        ROOM_BASE_CHANCE = builder
                .comment("Percent chance the room is armed on its first eligible morning (it then opens once",
                        "nobody is looking at the study door).")
                .defineInRange("roomFirstChance", 50, 0, 100);
        ROOM_CHANCE_STEP = builder
                .comment("Percent added to that chance for every eligible morning it did not appear.")
                .defineInRange("roomChanceStep", 25, 0, 100);
        SHIFTS_BEFORE_HALLWAY = builder
                .comment("Subtle changes (paintings, candles, a door's hinge...) that must happen, once someone has",
                        "gone through the room between rooms, before the impossible hallway can open.")
                .defineInRange("shiftsBeforeHallway", 2, 0, 100);
        HALLWAY_BASE_CHANCE = builder
                .comment("Percent chance the hallway opens on its first eligible morning. It is rolled before any",
                        "subtle change, so it outweighs them.")
                .defineInRange("hallwayBaseChance", 50, 0, 100);
        HALLWAY_CHANCE_STEP = builder
                .comment("Percent added to the hallway's chance for every eligible morning it stays shut.")
                .defineInRange("hallwayChanceStep", 25, 0, 100);
        SHIFT_BASE_CHANCE = builder
                .comment("Percent chance of a subtle change on a morning when nothing larger happens, before the hallway.")
                .defineInRange("shiftBaseChance", 45, 0, 100);
        SHIFT_CHANCE_STEP = builder
                .comment("Percent added to that chance for every quiet morning in a row.")
                .defineInRange("shiftChanceStep", 20, 0, 100);
        SHIFT_BASE_CHANCE_AFTER_HALLWAY = builder
                .comment("The same chance once the hallway is open: the house keeps changing, less often.")
                .defineInRange("shiftBaseChanceAfterHallway", 25, 0, 100);
        SHIFT_CHANCE_STEP_AFTER_HALLWAY = builder
                .comment("Percent added per quiet morning in a row once the hallway is open.")
                .defineInRange("shiftChanceStepAfterHallway", 15, 0, 100);
        SPEC = builder.build();
    }

    private HouseConfig() {
    }
}
