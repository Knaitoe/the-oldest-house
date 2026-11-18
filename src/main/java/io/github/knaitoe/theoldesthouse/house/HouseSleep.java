package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.mixin.HouseSleepAccess;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

/**
 * 0.4.703 (owner: "One player sleeping in the house triggers day phase. That player should just be synced in with the rest of the player
 * sleeping count, if possible."): the Overworld and the House keep one count of sleepers.
 *
 * The game counts each level's sleepers on their own, so a reader alone in a manor bed was the whole of the House's count, and the House
 * passed the night for everyone (HouseDays used to carry its morning to the Overworld). Now a reader in the House counts beside the
 * Overworld's players, against the same share the playersSleepingPercentage rule asks for: the Overworld alone passes the night, when
 * enough of both are asleep, and wakes the House's sleepers with its own; the House never passes it by itself. The tally the game shows
 * a sleeper is the shared one, shown to everyone in either. The House's outdoor and in-between places keep their own count, as other
 * dimensions do.
 */
public final class HouseSleep {
    private HouseSleep() {}

    /** The levels that share one count: the Overworld and the House. */
    public static boolean shared(ResourceKey<Level> dimension) {
        return dimension.equals(Level.OVERWORLD) || dimension.equals(HouseDimensions.INTERIOR);
    }

    /** Everyone in the Overworld or the House. */
    public static List<ServerPlayer> players(MinecraftServer server) {
        List<ServerPlayer> all = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) if (shared(level.dimension())) all.addAll(level.players());
        return all;
    }

    /** Who counts (no spectators), who is asleep, and who has slept long enough, as the game counts them. */
    public record Count(int active, int sleeping, int deep) {}

    public static Count count(List<? extends Player> players) {
        int active = 0, sleeping = 0, deep = 0;
        for (Player p : players) {
            if (p.isSleepingLongEnough()) deep++;
            if (watching(p)) continue;
            active++;
            if (p.isSleeping()) sleeping++;
        }
        return new Count(active, sleeping, deep);
    }

    /** A spectator, read from the game mode the server keeps for the player, as the game's own count reads it from the player. */
    static boolean watching(Player p) {
        return p instanceof ServerPlayer sp ? sp.gameMode.getGameModeForPlayer() == GameType.SPECTATOR : p.isSpectator();
    }

    /** How many must sleep, as the game works it out for one level. */
    public static int needed(int active, int percent) {
        return Math.max(1, Mth.ceil((float) (active * percent) / 100.0F));
    }

    public static boolean enough(Count c, int percent) {
        return c.sleeping() >= needed(c.active(), percent);
    }

    public static boolean deepEnough(Count c, int percent) {
        return c.deep() >= needed(c.active(), percent);
    }

    /** The night check's first half: the level's own count, or for the Overworld the shared one; the House never passes it alone. */
    public static boolean enoughSleeping(ServerLevel level, SleepStatus own, int percent) {
        if (!shared(level.dimension())) return own.areEnoughSleeping(percent);
        return level.dimension().equals(Level.OVERWORLD) && enough(count(players(level.getServer())), percent);
    }

    /** The night check's second half, the same way. */
    public static boolean enoughDeepSleeping(ServerLevel level, SleepStatus own, int percent, List<ServerPlayer> players) {
        if (!shared(level.dimension())) return own.areEnoughDeepSleeping(percent, players);
        return level.dimension().equals(Level.OVERWORLD) && deepEnough(count(players(level.getServer())), percent);
    }

    /** The Overworld has passed the night and woken its own sleepers: the House's wake with them, as the game wakes its own. */
    public static void wakeTheRest(ServerLevel level) {
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        for (ServerLevel other : level.getServer().getAllLevels()) {
            if (other == level || !shared(other.dimension())) continue;
            ((HouseSleepAccess) other).house$sleepStatus().removeAllSleepers();
            for (ServerPlayer p : List.copyOf(other.players())) if (p.isSleeping()) p.stopSleepInBed(false, false);
        }
    }

    /**
     * The tally shown to a sleeper: the shared one, to everyone in the Overworld and the House, under the game's own conditions. False
     * for any other level, which shows its own.
     */
    public static boolean announce(ServerLevel level) {
        if (!shared(level.dimension())) return false;
        MinecraftServer server = level.getServer();
        int percent = level.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
        if (percent > 100 || server.isSingleplayer() && !server.isPublished()) return true;
        List<ServerPlayer> everyone = players(server);
        Count c = count(everyone);
        Component tally = enough(c, percent) ? Component.translatable("sleep.skipping_night")
                : Component.translatable("sleep.players_sleeping", c.sleeping(), needed(c.active(), percent));
        for (ServerPlayer p : everyone) p.displayClientMessage(tally, true);
        return true;
    }
}
