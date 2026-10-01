package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseLayout;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.opening.Hillary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The Growl: the Minotaur's voice, heard long before it is seen. A low
 * sound with no source.
 *
 * It begins once the impossible hallway has opened, and only in the
 * labyrinth (never in a vignette). Each player hears it alone, from far off
 * in the solid wall, from directly below, or, deep enough, from not far
 * behind them. The deeper they are (doors from the hallway, the length of
 * their way back) the shorter the gaps, but the gaps are always random and
 * never short: it stays sporadic.
 *
 * Very rarely, once a player has been deep or heard it often, a night in a
 * manor bed ends in the cellar instead: they come to on its floor, out of black,
 * and a moment later the Growl comes from directly under it. Hillary, on the
 * porch outside, whines. Then not again for a long while.
 *
 * The sounds are original, synthesized by {@code tools/growl/synth_growl.py};
 * a recording can replace them under the same names.
 */
public final class Growl {
    public enum Kind {
        FAR("growl.far"), BELOW("growl.below"), NEAR("growl.near");

        private final ResourceLocation sound;

        Kind(String path) {
            this.sound = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, path);
        }

        public ResourceLocation sound() {
            return sound;
        }
    }

    private static final int CHECK_INTERVAL = 20;
    /** The average gap one door deep: fifteen minutes. */
    public static final long BASE_GAP = 15L * 60L * 20L;
    /** The average gap never falls below this, however deep. */
    public static final long MIN_MEAN_GAP = 4L * 60L * 20L;
    /** Nor does any single gap fall below this. */
    public static final long MIN_GAP = 150L * 20L;
    private static final double DEPTH_FACTOR = 0.4D;

    /** How deep, or how many heard, before the cellar can happen. */
    public static final int BASEMENT_DEPTH = 6;
    public static final int BASEMENT_HEARD = 6;
    /** Percent chance, per morning woken in a manor bed once eligible. */
    public static final int BASEMENT_CHANCE = 8;
    /** House days before it can happen again. */
    public static final long BASEMENT_COOLDOWN_DAYS = 10L;
    private static final int BASEMENT_MOVE_DELAY = 1;
    private static final int BASEMENT_GROWL_DELAY = 75;

    private record Schedule(long next, int depth) {
    }

    private static final Map<UUID, Schedule> SCHEDULE = new HashMap<>();
    /** Players about to wake in the cellar: game time of each step, and the step. */
    private static final Map<UUID, long[]> BASEMENT = new HashMap<>();

    private Growl() {
    }

    // ------------------------------------------------------------------
    // Timing

    /** The average gap at a depth, in ticks. */
    public static long meanGap(int depth) {
        double mean = BASE_GAP / (1.0D + DEPTH_FACTOR * Math.max(0, depth - 1));
        return Math.max(MIN_MEAN_GAP, (long) mean);
    }

    /** One random gap at a depth: anywhere from half to one and a half times the mean. */
    public static long gap(int depth, RandomSource random) {
        long gap = (long) (meanGap(depth) * (0.5D + random.nextDouble()));
        return Math.max(MIN_GAP, gap);
    }

    /** Where it comes from at a depth: far off at first; from below, and then close, deeper in. */
    public static Kind pick(int depth, RandomSource random) {
        int roll = random.nextInt(100);
        if (depth <= 2) {
            return Kind.FAR;
        }
        if (depth <= 5) {
            return roll < 75 ? Kind.FAR : Kind.BELOW;
        }
        return roll < 55 ? Kind.FAR : roll < 85 ? Kind.BELOW : Kind.NEAR;
    }

    // ------------------------------------------------------------------
    // In the labyrinth

    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tickBasement(server);
        if (server.getTickCount() % CHECK_INTERVAL != 0) {
            return;
        }
        GrowlChanges.tick(server);
        ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
        HouseSavedData house = HouseSavedData.get(server);
        BlockPos origin = house.houseOrigin();
        if (interior == null || origin == null || !house.isImpossibleDoorRevealed()) {
            SCHEDULE.clear();
            return;
        }
        LabyrinthData labyrinth = LabyrinthData.get(server);
        GrowlData record = GrowlData.get(server);
        long now = interior.getGameTime();
        for (ServerPlayer player : interior.players()) {
            UUID id = player.getUUID();
            LabyrinthPlace place = LabyrinthPlaces.placeAt(origin, player.blockPosition());
            int depth = labyrinth.returnDepth(id);
            if (player.isSpectator() || place == null || place.isVignette() || LabyrinthPacing.quiet(place) || depth <= 0) {
                continue;
            }
            record.noteDepth(id, depth);
            Schedule schedule = SCHEDULE.get(id);
            if (schedule == null) {
                SCHEDULE.put(id, new Schedule(now + gap(depth, player.getRandom()), depth));
                continue;
            }
            if (depth > schedule.depth()) {
                // Deeper: the next one may come sooner, never later.
                long sooner = Math.min(schedule.next(), now + gap(depth, player.getRandom()));
                schedule = new Schedule(sooner, depth);
                SCHEDULE.put(id, schedule);
            }
            if (now >= schedule.next()) {
                growl(player, pick(depth, player.getRandom()));
                SCHEDULE.put(id, new Schedule(now + gap(depth, player.getRandom()), depth));
            }
        }
    }

    /** Plays the Growl for one player alone, placed where nothing is. */
    public static void growl(ServerPlayer player, Kind kind) {
        Voice voice = voice(player.position(), player.getYRot(), kind, player.getRandom());
        play(player, voice);
    }

    public record Voice(Kind kind, Vec3 position, float volume, float pitch, long seed) {}

    /** Each utterance has its own spatial origin and delivery; the original clips remain mono. */
    public static Voice voice(Vec3 listener, float yaw, Kind kind, RandomSource random) {
        Vec3 at;
        switch (kind) {
            case BELOW -> at = listener.add(random.nextDouble() * 14 - 7,
                    -(8 + random.nextDouble() * 24), random.nextDouble() * 14 - 7);
            case NEAR -> {
                double r = Math.toRadians(yaw) + (random.nextDouble() - .5) * 1.2;
                double distance = 4 + random.nextDouble() * 8;
                at = listener.add(Math.sin(r) * distance, random.nextDouble() * 4 - 1, -Math.cos(r) * distance);
            }
            default -> {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double distance = 24 + random.nextDouble() * 48;
                at = listener.add(Math.cos(angle) * distance, random.nextDouble() * 20 - 14, Math.sin(angle) * distance);
            }
        }
        return new Voice(kind, at, .78F + random.nextFloat() * .22F,
                .88F + random.nextFloat() * .18F, random.nextLong());
    }

    public static ClientboundSoundPacket packet(Voice voice) {
        Holder<SoundEvent> sound = Holder.direct(SoundEvent.createVariableRangeEvent(voice.kind().sound()));
        Vec3 at = voice.position();
        return new ClientboundSoundPacket(sound, SoundSource.HOSTILE, at.x, at.y, at.z,
                voice.volume(), voice.pitch(), voice.seed());
    }

    private static void play(ServerPlayer player, Voice voice) {
        player.connection.send(packet(voice));
        GrowlData.get(player.server).noteHeard(player.getUUID());
        GrowlChanges.onGrowl(player, player.getRandom());
        TheOldestHouse.LOGGER.debug("{} heard the Growl ({} at {}).", player.getGameProfile().getName(), voice.kind(), voice.position());
    }

    private static void play(ServerPlayer player, Kind kind, Vec3 at, float volume) {
        RandomSource random = player.getRandom();
        play(player, new Voice(kind, at, volume, .88F + random.nextFloat() * .18F, random.nextLong()));
    }

    // ------------------------------------------------------------------
    // The cellar

    /**
     * A night's sleep in a manor bed has ended. Very rarely, for someone who
     * has been deep enough, it ends in the cellar.
     */
    public static void onManorWake(ServerPlayer player) {
        MinecraftServer server = player.server;
        HouseSavedData house = HouseSavedData.get(server);
        if (!house.isImpossibleDoorRevealed() || BASEMENT.containsKey(player.getUUID())) {
            return;
        }
        GrowlData record = GrowlData.get(server);
        long today = HouseCalendar.today(server);
        if (!record.isBasementEligible(player.getUUID(), today) || player.getRandom().nextInt(100) >= BASEMENT_CHANCE) {
            return;
        }
        wakeInBasement(player);
    }

    /** Wakes the player in the cellar now: black, then the cellar floor, then the Growl from under it. */
    public static boolean wakeInBasement(ServerPlayer player) {
        HouseSavedData house = HouseSavedData.get(player.server);
        if (house.houseOrigin() == null || player.server.getLevel(HouseDimensions.INTERIOR) == null) {
            return false;
        }
        // Keep the eyes shut through the wake-up so the bedroom is never seen.
        HousePackets.send(player, new HouseFadePayload(0, 50, 90));
        long now = player.server.overworld().getGameTime();
        BASEMENT.put(player.getUUID(), new long[]{now + BASEMENT_MOVE_DELAY, 0});
        GrowlData.get(player.server).noteBasement(player.getUUID(), HouseCalendar.today(player.server));
        return true;
    }

    private static void tickBasement(MinecraftServer server) {
        if (BASEMENT.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        for (UUID id : List.copyOf(BASEMENT.keySet())) {
            long[] step = BASEMENT.get(id);
            if (now < step[0]) {
                continue;
            }
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            ServerLevel interior = server.getLevel(HouseDimensions.INTERIOR);
            BlockPos origin = HouseSavedData.get(server).houseOrigin();
            if (player == null || interior == null || origin == null) {
                BASEMENT.remove(id);
                continue;
            }
            if (step[1] == 0) {
                BlockPos spot = cellarSpot(interior, origin, player.getRandom());
                if (spot == null) {
                    BASEMENT.remove(id);
                    continue;
                }
                player.stopRiding();
                player.teleportTo(interior, spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D,
                        player.getRandom().nextFloat() * 360.0F - 180.0F, 20.0F);
                player.setDeltaMovement(Vec3.ZERO);
                player.resetFallDistance();
                step[0] = now + BASEMENT_GROWL_DELAY;
                step[1] = 1;
            } else {
                BASEMENT.remove(id);
                // From directly under the cellar floor.
                RandomSource random = player.getRandom();
                play(player, Kind.BELOW, player.position().add(random.nextDouble() * 3 - 1.5,
                        -(5.5 + random.nextDouble() * 6), random.nextDouble() * 3 - 1.5), 1.15F + random.nextFloat() * .25F);
                hillaryHears(server, origin);
                TheOldestHouse.LOGGER.info("{} woke in the cellar of The Oldest House, and heard it underneath.",
                        player.getGameProfile().getName());
            }
        }
    }

    /** An open spot on the cellar floor, away from its walls, or null. */
    @Nullable
    public static BlockPos cellarSpot(Level level, BlockPos origin, RandomSource random) {
        HouseLayout.Box cellar = HouseLayout.CELLAR.box();
        List<BlockPos> spots = new ArrayList<>();
        for (int x = cellar.x0() + 1; x <= cellar.x1() - 1; x++) {
            for (int z = cellar.z0() + 1; z <= cellar.z1() - 1; z++) {
                BlockPos feet = origin.offset(x, cellar.y0(), z);
                if (RedRoom.isOpen(level, feet) && RedRoom.isOpen(level, feet.above()) && !RedRoom.isOpen(level, feet.below())) {
                    spots.add(feet);
                }
            }
        }
        return spots.isEmpty() ? null : spots.get(random.nextInt(spots.size()));
    }

    /** Hillary, on the porch outside, hears it through the ground. */
    private static void hillaryHears(MinecraftServer server, BlockPos origin) {
        ServerLevel overworld = server.overworld();
        AABB around = new AABB(origin).inflate(48.0D);
        for (Wolf wolf : overworld.getEntitiesOfClass(Wolf.class, around, w -> Hillary.tagOf(w) != null)) {
            wolf.getNavigation().stop();
            wolf.playSound(SoundEvents.WOLF_WHINE, 1.0F, 0.8F);
            if (wolf.isTame()) {
                wolf.setOrderedToSit(true);
                wolf.setInSittingPose(true);
            }
        }
    }

    // ------------------------------------------------------------------
    // Housekeeping

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SCHEDULE.remove(event.getEntity().getUUID());
        BASEMENT.remove(event.getEntity().getUUID());
    }

    public static void clearAll() {
        SCHEDULE.clear();
        BASEMENT.clear();
    }

    public static List<String> describe(MinecraftServer server, @Nullable ServerPlayer viewer) {
        List<String> lines = new ArrayList<>();
        if (!HouseSavedData.get(server).isImpossibleDoorRevealed()) {
            lines.add("The Growl: not yet (it begins once the impossible hallway opens).");
            return lines;
        }
        if (viewer == null) {
            lines.add("The Growl: heard in the labyrinth, more often deeper in.");
            return lines;
        }
        UUID id = viewer.getUUID();
        GrowlData record = GrowlData.get(server);
        Schedule schedule = SCHEDULE.get(id);
        long now = viewer.serverLevel().getGameTime();
        int depth = LabyrinthData.get(server).returnDepth(id);
        lines.add("The Growl: " + (schedule == null ? "not scheduled for you (only in the labyrinth, outside vignettes)"
                : "next for you in about " + Math.max(0L, (schedule.next() - now) / 20L) + " s")
                + "; at " + depth + " door(s) deep the average gap is " + meanGap(Math.max(1, depth)) / 20L + " s. Heard "
                + record.heard(id) + " time(s); deepest " + record.deepest(id) + ". The cellar: "
                + (record.isBasementEligible(id, HouseCalendar.today(server))
                ? BASEMENT_CHANCE + "% on each morning woken in a manor bed."
                : "not yet (needs " + BASEMENT_DEPTH + " doors deep or " + BASEMENT_HEARD + " growls heard, and "
                + BASEMENT_COOLDOWN_DAYS + " days between).") );
        lines.addAll(GrowlChanges.describe(server, viewer));
        return lines;
    }

    // ------------------------------------------------------------------
    // What each player has heard

    /** Per player: the deepest they have been, how often they have heard it, and the cellar's last night. */
    public static final class GrowlData extends SavedData {
        private static final String DATA_NAME = "the_oldest_house_growl";
        public static final Factory<GrowlData> FACTORY = new Factory<>(GrowlData::new, GrowlData::load);

        private final Map<UUID, int[]> counts = new HashMap<>();
        private final Map<UUID, Long> lastBasement = new HashMap<>();

        public GrowlData() {
        }

        public static GrowlData get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
        }

        private int[] of(UUID player) {
            return counts.computeIfAbsent(player, id -> new int[2]);
        }

        public void noteDepth(UUID player, int depth) {
            int[] c = of(player);
            if (depth > c[0]) {
                c[0] = depth;
                setDirty();
            }
        }

        public void noteHeard(UUID player) {
            of(player)[1]++;
            setDirty();
        }

        public void noteBasement(UUID player, long day) {
            lastBasement.put(player, day);
            setDirty();
        }

        public int deepest(UUID player) {
            int[] c = counts.get(player);
            return c == null ? 0 : c[0];
        }

        public int heard(UUID player) {
            int[] c = counts.get(player);
            return c == null ? 0 : c[1];
        }

        public boolean isBasementEligible(UUID player, long today) {
            if (deepest(player) < BASEMENT_DEPTH && heard(player) < BASEMENT_HEARD) {
                return false;
            }
            Long last = lastBasement.get(player);
            return last == null || today - last >= BASEMENT_COOLDOWN_DAYS;
        }

        private static GrowlData load(CompoundTag tag, HolderLookup.Provider registries) {
            GrowlData data = new GrowlData();
            CompoundTag players = tag.getCompound("Players");
            for (String key : players.getAllKeys()) {
                CompoundTag p = players.getCompound(key);
                UUID id = UUID.fromString(key);
                data.counts.put(id, new int[]{p.getInt("Deepest"), p.getInt("Heard")});
                if (p.contains("LastBasement")) {
                    data.lastBasement.put(id, p.getLong("LastBasement"));
                }
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, int[]> entry : counts.entrySet()) {
                CompoundTag p = new CompoundTag();
                p.putInt("Deepest", entry.getValue()[0]);
                p.putInt("Heard", entry.getValue()[1]);
                Long last = lastBasement.get(entry.getKey());
                if (last != null) {
                    p.putLong("LastBasement", last);
                }
                players.put(entry.getKey().toString(), p);
            }
            for (Map.Entry<UUID, Long> entry : lastBasement.entrySet()) {
                if (!counts.containsKey(entry.getKey())) {
                    CompoundTag p = new CompoundTag();
                    p.putLong("LastBasement", entry.getValue());
                    players.put(entry.getKey().toString(), p);
                }
            }
            tag.put("Players", players);
            return tag;
        }
    }
}
