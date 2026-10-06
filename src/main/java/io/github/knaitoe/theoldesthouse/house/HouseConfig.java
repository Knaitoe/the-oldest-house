package io.github.knaitoe.theoldesthouse.house;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server (per-world) settings for how the House changes after the player's
 * first visit. Mornings here are the House's perceived age: one per dawn
 * after somebody has entered the manor.
 */
public final class HouseConfig {
    public static final ModConfigSpec SPEC;

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
    public static final ModConfigSpec.BooleanValue HARRIGAN_PLAYER_TARGETS;
    public static final ModConfigSpec.IntValue STORY_EARLY_CHANCE, STORY_BASE_CHANCE, STORY_CHANCE_STEP;
    public static final ModConfigSpec.IntValue STORY_SPACING, STORY_DRY_GUARANTEE;
    public static final ModConfigSpec.IntValue HAZARD_EARLY_CHANCE, HAZARD_MIDDLE_CHANCE, HAZARD_DEEP_CHANCE, HAZARD_SPACING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ROOM_FIRST_AGE = builder
                .comment("The first House morning on which the room between rooms can be rolled for.")
                .defineInRange("roomFirstAge", 3, 1, 1000);
        ROOM_MIN_MORNINGS_AFTER_RUGS = builder
                .comment("Mornings after the rugs change before the room between rooms can appear.")
                .defineInRange("roomMinMorningsAfterRugs", 2, 0, 1000);
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
        HARRIGAN_PLAYER_TARGETS = builder
                .comment("Allow Mr Harrigan's phone to accept another player's name.",
                        "Off by default: enabling it permits a phone owner to send Harrigan after another player.",
                        "A player name consumes that day's call, but does not add a friendly-mob strike.")
                .define("harriganPlayerTargets", false);
        builder.push("exploration");
        STORY_EARLY_CHANCE = builder.comment("Chance of offering a story on a new route at depths 6-9. Remembered routes never reroll.").defineInRange("storyEarlyChance", 8, 0, 100);
        STORY_BASE_CHANCE = builder.comment("Story chance on a new route at depth 10 or more.").defineInRange("storyBaseChance", 16, 0, 100);
        STORY_CHANCE_STEP = builder.comment("Added chance per eligible new route without a story; backtracking adds nothing.").defineInRange("storyChanceStep", 7, 0, 100);
        STORY_SPACING = builder.comment("Ordinary visits between stories. Deliberate scent and a living-pet rescue bypass this interval.").defineInRange("storySpacing", 3, 0, 8);
        STORY_DRY_GUARANTEE = builder.comment("Guarantee a story after this many eligible dry deals, once the spacing is satisfied.").defineInRange("storyDryGuarantee", 12, 1, 100);
        HAZARD_EARLY_CHANCE = builder.comment("Physical-hazard chance at depths 6-9, only when this arrival offers no story or anomaly.").defineInRange("hazardEarlyChance", 10, 0, 100);
        HAZARD_MIDDLE_CHANCE = builder.comment("Physical-hazard chance at depths 10-15.").defineInRange("hazardMiddleChance", 16, 0, 100);
        HAZARD_DEEP_CHANCE = builder.comment("Physical-hazard chance at depth 16 or more.").defineInRange("hazardDeepChance", 22, 0, 100);
        HAZARD_SPACING = builder.comment("Visits separating physical hazards.").defineInRange("hazardSpacing", 3, 0, 8);
        builder.pop();
        SPEC = builder.build();
    }

    private HouseConfig() {
    }

    /** Native fixtures can query pacing before a server configuration is attached. */
    public static int setting(ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
}
