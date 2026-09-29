package io.github.knaitoe.theoldesthouse.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Every one of the mod's own payloads goes out through here. A player whose
 * connection never negotiated the mod's channel (a fake player, a GameTest's
 * mock player) is skipped: NeoForge refuses such a send with an exception,
 * which from inside an event handler (a login, a morning) would take the
 * whole handler down with it. A real client always has the channel, since
 * NeoForge does not let a client without the mod join.
 */
public final class HousePackets {
    private HousePackets() {
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (canReceive(player, payload)) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    public static void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(player, payload);
        }
    }

    public static boolean canReceive(ServerPlayer player, CustomPacketPayload payload) {
        return !(player instanceof FakePlayer)
                && player.connection != null
                && player.connection.hasChannel(payload.type());
    }
}
