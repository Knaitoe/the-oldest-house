package io.github.knaitoe.theoldesthouse.house;

import com.google.gson.JsonObject;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.FinaleProgress;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * A local, opt-in record of how the journey actually goes, for tuning pacing from evidence
 * rather than guesses (docs/PLAYER_EXPERIENCE_PLAN.md, appendix B).
 *
 * <p>Off unless the server config turns it on. It appends one JSON object per line to
 * {@code <world>/the_oldest_house/playtest-log.jsonl} on its own thread and never sends
 * anything anywhere. It only observes: nothing in play depends on it, and a failed write is
 * logged once and otherwise ignored.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class PlaytestLog {
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "The Oldest House playtest log");
        thread.setDaemon(true);
        return thread;
    });
    /** Last opening stage and finale phase written for each player, so changes are logged once. */
    private static final Map<UUID, String> STAGES = new HashMap<>(), PHASES = new HashMap<>();
    /** GameTests write to a file of their own choosing, whatever the config says. */
    @Nullable private static Path override;
    private static boolean warned;

    private PlaytestLog() {
    }

    public static boolean enabled() {
        return override != null || HouseConfig.SPEC.isLoaded() && HouseConfig.PLAYTEST_LOG.get();
    }

    /** Records one event for a player, with alternating field names and values. */
    public static void event(ServerPlayer player, String name, Object... fields) {
        if (!enabled()) return;
        JsonObject line = new JsonObject();
        line.addProperty("time", System.currentTimeMillis());
        line.addProperty("gameTime", player.serverLevel().getGameTime());
        line.addProperty("player", id(player.getUUID()));
        line.addProperty("event", name);
        for (int i = 0; i + 1 < fields.length; i += 2) {
            String key = String.valueOf(fields[i]);
            Object value = fields[i + 1];
            if (value instanceof Number number) line.addProperty(key, number);
            else if (value instanceof Boolean flag) line.addProperty(key, flag);
            else if (value != null) line.addProperty(key, String.valueOf(value));
        }
        write(player.server, line.toString());
    }

    /** A gate the player tried and was refused at; repeated refusals are what the plan measures. */
    public static void refused(ServerPlayer player, String gate) {
        event(player, "refused", "gate", gate, "place", place(player));
    }

    /** Where the player stands, as a place id, or "manor"/"overworld"/"outside" when not in a place. */
    public static String place(ServerPlayer player) {
        var origin = HouseSavedData.get(player.server).houseOrigin();
        LabyrinthPlace place = origin == null ? null : LabyrinthPlaces.placeAt(origin, player.blockPosition());
        if (place != null) return place.id();
        if (player.serverLevel().dimension().equals(HouseDimensions.INTERIOR)) return "manor";
        if (player.serverLevel().dimension().equals(HouseDimensions.OUTSIDE)) return "outside";
        return "overworld";
    }

    public static Path path(MinecraftServer server) {
        return override != null ? override
                : server.getWorldPath(LevelResource.ROOT).resolve(TheOldestHouse.MOD_ID).resolve("playtest-log.jsonl");
    }

    /** For GameTests: write to this file regardless of config, or stop when given null. */
    public static void overrideForTesting(@Nullable Path file) {
        override = file;
    }

    /** Waits until every event recorded so far is on disk. */
    public static void flush() {
        try {
            WRITER.submit(() -> { }).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            TheOldestHouse.LOGGER.warn("The playtest log did not finish writing.", e);
        }
    }

    private static void write(MinecraftServer server, String json) {
        Path file = path(server);
        WRITER.execute(() -> {
            try {
                Files.createDirectories(file.getParent());
                Files.writeString(file, json + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                if (!warned) TheOldestHouse.LOGGER.warn("Could not write the playtest log to {}.", file, e);
                warned = true;
            }
        });
    }

    private static String id(UUID player) {
        boolean hash = override == null && HouseConfig.SPEC.isLoaded() ? HouseConfig.PLAYTEST_LOG_HASH_IDS.get() : true;
        if (!hash) return player.toString();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(player.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 6);
        } catch (NoSuchAlgorithmException e) {
            return "unknown";
        }
    }

    private static int depth(ServerPlayer player) {
        return LabyrinthData.get(player.server).returnDepth(player.getUUID());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            event(player, "session_start", "place", place(player), "depth", depth(player));
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        event(player, "session_end", "place", place(player), "depth", depth(player));
        STAGES.remove(player.getUUID());
        PHASES.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isCanceled())
            event(player, "death", "place", place(player), "cause", event.getSource().getMsgId(), "depth", depth(player));
    }

    /** Opening stages and finale phases change in many places; noticing them once a second is enough. */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % 20 != 7 || !enabled()) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            String stage = OpeningSequence.state(player).stage().name().toLowerCase(java.util.Locale.ROOT);
            if (!stage.equals(STAGES.put(id, stage)))
                event(player, "opening_stage", "stage", stage);
            String phase = FinaleProgress.phase(server, id).name().toLowerCase(java.util.Locale.ROOT);
            if (!phase.equals(PHASES.put(id, phase)))
                event(player, "phase", "phase", phase, "depth", depth(player));
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        STAGES.clear();
        PHASES.clear();
        flush();
    }
}
