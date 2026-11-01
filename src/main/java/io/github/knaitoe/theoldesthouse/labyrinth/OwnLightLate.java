package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LateBodiesPayload;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.216, the last of your own light's cost (docs/OWN_LIGHT_0_4_213.md, "a friend who moves a beat late"): once the double has
 * gone home, win or fail, the reader is late until they next sleep through a night. Every client is told who is late; the
 * reader's own client changes nothing, and other readers' clients now and then draw them a beat behind their own body
 * (client/LateBodyClient). Nothing about it is said to anyone.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class OwnLightLate {
    private OwnLightLate(){}
    public static final String KEY="Late213";
    private static List<UUID> sent=List.of();

    public static boolean late(ServerPlayer p){return OwnLight.own(p).getBoolean(KEY);}
    static void begin(net.minecraft.nbt.CompoundTag own){own.putBoolean(KEY,true);}
    /** The readers who are late now, among those online. */
    public static List<UUID> late(MinecraftServer server){
        var out=new ArrayList<UUID>();for(var p:server.getPlayerList().getPlayers())if(late(p))out.add(p.getUUID());return out;
    }
    public static void sync(MinecraftServer server){
        var now=late(server);if(now.equals(sent))return;sent=List.copyOf(now);var payload=new LateBodiesPayload(now);
        for(var p:server.getPlayerList().getPlayers())if(p.connection!=null)HousePackets.send(p,payload);
    }
    /** Sleeping through a night ends it; getting up before morning does not. */
    @SubscribeEvent public static void woke(PlayerWakeUpEvent e){
        if(e.wakeImmediately()||!(e.getEntity() instanceof ServerPlayer p))return;var own=OwnLight.own(p);if(!own.getBoolean(KEY))return;
        own.remove(KEY);OwnLight.save(p,own);sync(p.server);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%20==11)sync(e.getServer());}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.connection!=null)HousePackets.send(p,new LateBodiesPayload(late(p.server)));}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){sent=List.of();}
}
