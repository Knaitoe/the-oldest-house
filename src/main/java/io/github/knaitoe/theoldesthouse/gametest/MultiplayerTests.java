package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionCancelPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Several real players at once. Fake players are never ticked and cannot
 * change dimension, so the concurrency cases (two people at one door, one
 * backing out while another watches, a disconnect mid-crossing) need real
 * ServerPlayers. These run in their own batch, apart from everything else.
 */
@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultiplayerTests {
    private MultiplayerTests() {
    }

    /**
     * Not a test of the mod: a probe of what a mock player can do in this
     * NeoForge (whether it survives our login packets, takes our payloads, is
     * ticked by the server, can change dimension). It reports in the log
     * (OTH-PROBE lines) and never fails the build.
     */
    @GameTest(template = "empty", batch = "multiplayer", required = false, timeoutTicks = 200)
    public static void probeMockPlayers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        List<String> notes = new ArrayList<>();
        ServerPlayer created;
        try {
            created = helper.makeMockServerPlayerInLevel();
        } catch (Throwable t) {
            TheOldestHouse.LOGGER.error("OTH-PROBE|create=failed", t);
            helper.fail("a mock player could not be created: " + t);
            return;
        }
        final ServerPlayer player = created;
        notes.add("create=ok");
        notes.add("player=" + player.getClass().getName());
        notes.add("connection=" + player.connection.getClass().getName());
        notes.add("inPlayerList=" + server.getPlayerList().getPlayers().contains(player));
        notes.add("inLevel=" + level.players().contains(player));
        try {
            PacketDistributor.sendToPlayer(player, new HouseTransitionCancelPayload(-1));
            notes.add("customPayload=ok");
        } catch (Throwable t) {
            notes.add("customPayload=" + t);
            TheOldestHouse.LOGGER.error("OTH-PROBE|customPayload", t);
        }

        AtomicInteger tickEvents = new AtomicInteger();
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
            if (event.getEntity() == player) {
                tickEvents.incrementAndGet();
            }
        });
        int startTickCount = player.tickCount;

        helper.runAfterDelay(10, () -> {
            notes.add("serverTicked10: tickCount+" + (player.tickCount - startTickCount) + " tickEvents=" + tickEvents.get());
            int before = tickEvents.get();
            try {
                for (int i = 0; i < 3; i++) {
                    player.doTick();
                }
                notes.add("manualDoTick3: tickEvents+" + (tickEvents.get() - before));
            } catch (Throwable t) {
                notes.add("manualDoTick=" + t);
                TheOldestHouse.LOGGER.error("OTH-PROBE|doTick", t);
            }

            ServerLevel nether = server.getLevel(Level.NETHER);
            try {
                player.teleportTo(nether, 0.5D, 80.0D, 0.5D, 0.0F, 0.0F);
                notes.add("teleportToNether: now in " + player.serverLevel().dimension().location()
                        + ", inNetherList=" + nether.players().contains(player) + ", inOverworldList=" + level.players().contains(player));
                player.teleportTo(level, player.getX(), 80.0D, player.getZ(), 0.0F, 0.0F);
                notes.add("teleportBack: now in " + player.serverLevel().dimension().location());
            } catch (Throwable t) {
                notes.add("teleport=" + t);
                TheOldestHouse.LOGGER.error("OTH-PROBE|teleport", t);
            }

            try {
                server.getPlayerList().remove(player);
                notes.add("remove=ok, stillListed=" + server.getPlayerList().getPlayers().contains(player));
            } catch (Throwable t) {
                notes.add("remove=" + t);
                TheOldestHouse.LOGGER.error("OTH-PROBE|remove", t);
            }
            for (String note : notes) {
                TheOldestHouse.LOGGER.info("OTH-PROBE|{}", note);
            }
            helper.succeed();
        });
    }
}
