package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Real survival abilities and camera behavior, using NeoForge's native mock-connection setup. */
final class NativeTestPlayers {
    private static final Map<UUID,Connection> CONNECTIONS=new HashMap<>();
    private static final Map<UUID,EmbeddedChannel> CHANNELS=new HashMap<>();
    private NativeTestPlayers(){}
    static ServerPlayer survival(GameTestHelper h,String name){
        return survival(h,name,UUID.randomUUID());
    }
    static ServerPlayer survival(GameTestHelper h,String name,UUID id){
        var cookie=CommonListenerCookie.createInitial(new GameProfile(id,name),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND){@Override public boolean isMemoryConnection(){return true;}};
        var channel=new EmbeddedChannel(connection);NetworkRegistry.configureMockConnection(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,cookie);
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);CONNECTIONS.put(p.getUUID(),connection);CHANNELS.put(p.getUUID(),channel);
        return p;
    }
    static java.util.List<String> messages(ServerPlayer p){
        var channel=CHANNELS.get(p.getUUID());channel.runPendingTasks();var out=new java.util.ArrayList<String>();
        Object packet;while((packet=channel.readOutbound())!=null){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)out.add(chat.content().getString());
            io.netty.util.ReferenceCountUtil.release(packet);
        }
        return out;
    }
    static void remove(ServerPlayer p){
        p.server.getPlayerList().remove(p);var connection=CONNECTIONS.remove(p.getUUID());
        if(connection!=null)connection.disconnect(Component.literal("Native test complete"));
        var channel=CHANNELS.remove(p.getUUID());if(channel!=null)channel.finishAndReleaseAll();
    }
}
