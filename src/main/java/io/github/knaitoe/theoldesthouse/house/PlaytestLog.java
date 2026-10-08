package io.github.knaitoe.theoldesthouse.house;

import com.google.gson.JsonObject;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.OpeningSequence;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Local, opt-in journey evidence. No gameplay depends on this log. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class PlaytestLog {
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "The Oldest House playtest log");
        thread.setDaemon(true); return thread;
    });
    private static final Map<UUID, String> STAGES = new HashMap<>(), PHASES = new HashMap<>();
    private static final Map<UUID, Track> SESSIONS = new HashMap<>();
    private record Context(String place, boolean inLabyrinth, String activity) {}
    private static final class Track {
        final String session = UUID.randomUUID().toString();
        String context = "";
        long observed;
    }
    @Nullable private static Path override;
    private static volatile boolean warned;
    private PlaytestLog() {}

    public static boolean enabled() {
        return override != null || HouseConfig.SPEC.isLoaded() && HouseConfig.PLAYTEST_LOG.get();
    }

    /** Every event carries its real dimension, occupancy and connection identity. */
    public static void event(ServerPlayer player, String name, Object... fields) {
        if (!enabled()) return;
        UUID uuid = player.getUUID();
        if (name.equals("session_start")) SESSIONS.put(uuid, new Track());
        Track track = SESSIONS.get(uuid);
        if (track == null) {
            if (name.equals("session_end")) return;
            track = new Track(); SESSIONS.put(uuid, track);
            record(player, track, "session_start", new Object[0]);
        }
        record(player, track, name, fields);
        if (name.equals("session_end")) forget(uuid);
    }

    private static void record(ServerPlayer player, Track track, String name, Object[] fields) {
        Context context = context(player);
        JsonObject line = new JsonObject();
        long now = System.currentTimeMillis();
        line.addProperty("schema", 2);
        line.addProperty("time", now);
        line.addProperty("gameTime", player.serverLevel().getGameTime());
        line.addProperty("player", id(player.getUUID()));
        line.addProperty("session", track.session);
        line.addProperty("event", name);
        line.addProperty("dimension", player.serverLevel().dimension().location().toString());
        line.addProperty("place", context.place);
        line.addProperty("depth", LabyrinthData.get(player.server).returnDepth(player.getUUID()));
        line.addProperty("inLabyrinth", context.inLabyrinth && !player.isSpectator());
        line.addProperty("activity", context.activity);
        line.addProperty("spectator", player.isSpectator());
        for (int i = 0; i + 1 < fields.length; i += 2) {
            String key = String.valueOf(fields[i]); Object value = fields[i + 1];
            if (value instanceof Number number) line.addProperty(key, number);
            else if (value instanceof Boolean flag) line.addProperty(key, flag);
            else if (value != null) line.addProperty(key, String.valueOf(value));
        }
        track.context = context.toString() + player.serverLevel().dimension().location() + player.isSpectator();
        track.observed = now;
        write(player.server, line.toString());
    }

    public static void refused(ServerPlayer player, String gate) {
        event(player, "refused", "gate", gate, "site", place(player));
    }
    /** Distinguish two actual doors/hearths with the same refusal wording. */
    public static void refused(ServerPlayer player, String gate, BlockPos at) {
        event(player, "refused", "gate", gate, "site", player.serverLevel().dimension().location()
                + ":" + at.getX() + "," + at.getY() + "," + at.getZ());
    }

    private static Context context(ServerPlayer player) {
        var dimension = player.serverLevel().dimension();
        if (!HouseDimensions.isHouseDimension(dimension))
            return new Context(dimension.equals(Level.OVERWORLD) ? "overworld" : "other", false, "outside");
        BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
        if (dimension.equals(HouseDimensions.INTERIOR)) {
            if (StaircaseLeaks.active(player)) return new Context("note_scene", true, "note_scene");
            if (origin != null && FinaleArchitecture.contains(origin, player.blockPosition()))
                return new Context("staircase", true, "staircase");
        }
        LabyrinthPlace place = dimension.equals(HouseDimensions.OUTSIDE)
                ? LiteraryCopies.placeAt(player.server, player.blockPosition()) : null;
        if (place == null && origin != null) {
            var candidate = LabyrinthPlaces.placeAt(origin, player.blockPosition());
            if (candidate != null && NovelRooms.dimension(candidate).equals(dimension)) place = candidate;
        }
        if (place != null) return new Context(place.id(), true,
                WitnessAccount.Story.of(place.id()) != null && !WitnessAccount.has(LabyrinthData.get(player.server),player.getUUID(),WitnessAccount.Story.of(place.id())) ? "story" : "ordinary");
        return new Context(dimension.equals(HouseDimensions.INTERIOR) ? "manor" : "outside", false, "refuge");
    }
    public static String place(ServerPlayer player) { return context(player).place; }
    public static boolean inLabyrinth(ServerPlayer player) { return context(player).inLabyrinth && !player.isSpectator(); }

    public static Path path(MinecraftServer server) {
        return override != null ? override : server.getWorldPath(LevelResource.ROOT)
                .resolve(TheOldestHouse.MOD_ID).resolve("playtest-log.jsonl");
    }
    public static void overrideForTesting(@Nullable Path file) {
        override = file; SESSIONS.clear(); STAGES.clear(); PHASES.clear();
    }
    public static void flush() {
        try { WRITER.submit(() -> {}).get(5, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (ExecutionException | TimeoutException e) { TheOldestHouse.LOGGER.warn("The playtest log did not finish writing.", e); }
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
        } catch (NoSuchAlgorithmException e) { return "unknown"; }
    }
    private static void forget(UUID id) { SESSIONS.remove(id); STAGES.remove(id); PHASES.remove(id); }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) event(player, "session_start");
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        event(player, "session_end"); forget(player.getUUID());
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isCanceled())
            event(player, "death", "cause", event.getSource().getMsgId());
    }
    @SubscribeEvent
    public static void changed(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) event(player, "location");
    }

    /** Changes sampled each second; a 30-second heartbeat bounds incomplete-session evidence. */
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!enabled()) { SESSIONS.clear(); STAGES.clear(); PHASES.clear(); return; }
        if (server.getTickCount() % 20 != 7) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID(); Context context = context(player); Track track = SESSIONS.get(id);
            String signature = context.toString() + player.serverLevel().dimension().location() + player.isSpectator();
            if (track == null || !signature.equals(track.context) || System.currentTimeMillis() - track.observed >= 30_000)
                event(player, "location");
            String stage = OpeningSequence.state(player).stage().name().toLowerCase(Locale.ROOT);
            if (!stage.equals(STAGES.put(id, stage))) event(player, "opening_stage", "stage", stage);
            String phase = FinaleProgress.phase(server, id).name().toLowerCase(Locale.ROOT);
            if (!phase.equals(PHASES.put(id, phase))) event(player, "phase", "phase", phase);
        }
    }
    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        SESSIONS.clear(); STAGES.clear(); PHASES.clear(); flush(); warned = false;
    }
}
