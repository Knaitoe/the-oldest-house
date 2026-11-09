package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.FaceLossPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 0.4.410, what the machine in the Kept took: a reader's mouth. The loss is the reader's own, saved with the world, and permanent: it
 * survives death, every door and every login, and every viewer draws that reader's face smooth below the eyes, in mirrors too. Nothing
 * else about them changes; they can speak, write and chat as before. Nothing in the House gives it back yet.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FaceLoss {
    public static final String STATE="kept_face_0410";
    // Each side keeps its own view: the server's from the saved record, a client's from the payload.
    private static final Set<UUID> SERVER=ConcurrentHashMap.newKeySet(),CLIENT=ConcurrentHashMap.newKeySet();
    private FaceLoss(){}

    public static CompoundTag record(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(STATE,id.toString());}
    private static void save(MinecraftServer s,UUID id,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,id.toString(),t);}
    public static boolean taken(MinecraftServer s,UUID id){return record(s,id).getBoolean("Mouth");}
    /** Whether this player has no mouth, as this side of the connection knows it. */
    public static boolean faceless(Player p){return (p.level().isClientSide()?CLIENT:SERVER).contains(p.getUUID());}
    public static void clientSet(UUID id,boolean gone){if(gone)CLIENT.add(id);else CLIENT.remove(id);}
    public static void clientClear(){CLIENT.clear();}

    /** The reader's mouth, permanently, shown to every viewer at once. */
    public static boolean take(ServerPlayer p){
        var t=record(p.server,p.getUUID());if(t.getBoolean("Mouth"))return false;
        t.putBoolean("Mouth",true);t.putLong("Taken",p.server.overworld().getGameTime());save(p.server,p.getUUID(),t);
        SERVER.add(p.getUUID());reveal(p);return true;
    }
    public static void reveal(ServerPlayer p){HousePackets.sendToAll(p.server,new FaceLossPayload(p.getUUID(),SERVER.contains(p.getUUID())));}

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(taken(p.server,p.getUUID()))SERVER.add(p.getUUID());else SERVER.remove(p.getUUID());
        for(var other:p.server.getPlayerList().getPlayers())if(other!=p&&SERVER.contains(other.getUUID()))HousePackets.send(p,new FaceLossPayload(other.getUUID(),true));
        if(SERVER.contains(p.getUUID()))reveal(p);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SERVER.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p&&SERVER.contains(p.getUUID()))reveal(p);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){SERVER.clear();}
}
