package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** Gathers what one explorer's record actually holds and hands it to {@link StaircaseProse}. */
public final class StaircaseAccount {
    private StaircaseAccount() {}

    public static StaircaseProse.Story write(ServerPlayer p, CompoundTag work, long seed) {
        return StaircaseProse.compose(p.getGameProfile().getName(), facts(p, work), seed);
    }
    public static HouseWriting.WritingStyle hand(String name) {
        try { return HouseWriting.WritingStyle.valueOf(name); } catch (IllegalArgumentException e) { return HouseWriting.WritingStyle.WILL; }
    }
    /** A block's name in prose, from its registry id rather than a server-side translation. */
    public static String blockName(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return StaircaseProse.shortName(key.getPath().replace('_', ' '));
    }

    private static int stat(ServerPlayer p, ResourceLocation id) { return p.getStats().getValue(Stats.CUSTOM.get(id)); }

    /** Every recorded fact this explorer actually has. Nothing is inferred from silence. */
    public static List<StaircaseProse.Fact> facts(ServerPlayer p, CompoundTag work) {
        var d = LabyrinthData.get(p.server);
        var home = HouseExperience.record(d, p.getUUID());
        var letters = HouseCorrespondence.record(d, p.getUUID());
        var out = new ArrayList<StaircaseProse.Fact>();
        metres(out, "walked", stat(p, Stats.WALK_ONE_CM), 50);
        metres(out, "sprinted", stat(p, Stats.SPRINT_ONE_CM), 200);
        metres(out, "swum", stat(p, Stats.SWIM_ONE_CM), 20);
        metres(out, "boated", stat(p, Stats.BOAT_ONE_CM), 50);
        metres(out, "rode", stat(p, Stats.HORSE_ONE_CM), 50);
        metres(out, "flew", stat(p, Stats.AVIATE_ONE_CM), 50);
        metres(out, "fell", stat(p, Stats.FALL_ONE_CM), 30);
        metres(out, "climbed", stat(p, Stats.CLIMB_ONE_CM), 10);
        count(out, "deaths", stat(p, Stats.DEATHS));
        count(out, "kills", stat(p, Stats.MOB_KILLS));
        count(out, "bread", p.getStats().getValue(Stats.ITEM_CRAFTED.get(Items.BREAD)));
        count(out, "enchanted", stat(p, Stats.ENCHANT_ITEM));
        count(out, "bred", stat(p, Stats.ANIMALS_BRED));
        count(out, "fish", stat(p, Stats.FISH_CAUGHT));
        count(out, "cake", stat(p, Stats.EAT_CAKE_SLICE));
        count(out, "flowers", stat(p, Stats.POT_FLOWER));
        count(out, "music", (long) stat(p, Stats.PLAY_NOTEBLOCK) + stat(p, Stats.PLAY_RECORD));
        count(out, "bells", stat(p, Stats.BELL_RING));
        count(out, "traded", stat(p, Stats.TRADED_WITH_VILLAGER));
        count(out, "slept", stat(p, Stats.SLEEP_IN_BED));
        count(out, "hours", stat(p, Stats.PLAY_TIME) / 72000L);
        named(out, "built", work.getString("Built").toLowerCase(Locale.ROOT));
        named(out, "broke", work.getString("Broke").toLowerCase(Locale.ROOT));
        if (home.getInt("Care") > 0 && !home.getString("CaredName").isBlank())
            named(out, "cared", home.getBoolean("CaredNamed") ? home.getString("CaredName") : "the " + home.getString("CaredName").toLowerCase(Locale.ROOT));
        MotherCollection.get(p.server).all().stream()
            .filter(e -> p.getUUID().equals(e.owner) && e.claimant == null && !e.name.isBlank())
            .min(Comparator.comparing(e -> e.id)).ifPresent(e -> named(out, "lost", e.name));
        if (!letters.getString("SafeRetreat").isBlank()) named(out, "retreat", StaircaseProse.place(letters.getString("SafeRetreat")));
        count(out, "manor", home.getInt("Sleeps"));
        var read = letters.getCompound("Read");
        count(out, "letters", read.getAllKeys().stream().filter(read::getBoolean).count());
        count(out, "deepest", home.getInt("Deepest"));
        return out;
    }
    private static void metres(List<StaircaseProse.Fact> out, String id, int centimetres, int minimum) {
        long m = centimetres / 100L; if (m >= minimum) StaircaseProse.fact(id, m, "").ifPresent(out::add);
    }
    private static void count(List<StaircaseProse.Fact> out, String id, long n) { if (n > 0) StaircaseProse.fact(id, n, "").ifPresent(out::add); }
    private static void named(List<StaircaseProse.Fact> out, String id, String value) {
        if (value != null && !value.isBlank()) StaircaseProse.fact(id, 1, value).ifPresent(out::add);
    }
}
