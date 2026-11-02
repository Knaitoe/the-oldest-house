package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.SourceMarkPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.273 (owner: "Vignettes that requires literature to be read at start need to have literature highlighted in some form to
 * prompt players to engage"). Every ordinary literary vignette waits on its first paper: its account does not count until that has
 * been read, and several hold their play on it (LiteraryVignettes.Pages). Until a reader has read it, their own client is told where
 * it lies, and draws it with a faint warm outline that breathes and a few motes rising off it (client/SourceMarkClient). Nobody else
 * is told. It stops once the paper is read, carried away unread (the reader has it with them), or the story is finished; and when the
 * reader leaves the scene. The uncanny rooms, where reading is optional, and Borrasca, which keeps its own papers, are not marked.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SourceMark {
    private SourceMark(){}
    /** How often, in ticks, each reader's mark is checked. */
    public static final int EVERY=10;
    private static final Map<UUID,BlockPos> SENT=new HashMap<>();

    /** Whether a scene's progress waits on its first paper being read. */
    public static boolean waits(@Nullable LabyrinthPlace place){return place!=null&&LiteraryRooms.isLiterary(place)&&place!=LabyrinthPlace.BORRASCA&&!UncannyRooms.isUncanny(place);}
    /** Where this reader's unread first paper lies, or null when nothing should be marked for them. */
    public static @Nullable BlockPos wanted(ServerPlayer p){
        if(p.isSpectator()||!p.isAlive()||!LiteraryVignettes.participant(p))return null;
        var place=LiteraryVignettes.current(p);if(!waits(place))return null;
        var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),place);
        if(own.getBoolean("Read_Source")||own.getBoolean("Taken_Source")||own.getBoolean("Completed"))return null;
        var b=LiteraryVignettes.base(p,place);var rel=LiteraryRooms.source(place);
        return b==null||rel==null?null:b.offset(rel);
    }
    /** The mark this reader was last sent (for tests). */
    public static @Nullable BlockPos sent(ServerPlayer p){return SENT.get(p.getUUID());}
    /** Brings one reader's mark up to date, sending only a change. */
    public static void update(ServerPlayer p){
        var want=wanted(p);var had=SENT.get(p.getUUID());
        if(want==null){if(had!=null){SENT.remove(p.getUUID());HousePackets.send(p,new SourceMarkPayload(false,had));}return;}
        if(!want.equals(had)){SENT.put(p.getUUID(),want);HousePackets.send(p,new SourceMarkPayload(true,want));}
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(e.getServer().getTickCount()%EVERY!=0)return;
        for(var p:e.getServer().getPlayerList().getPlayers())update(p);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SENT.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void changed(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){SENT.remove(p.getUUID());HousePackets.send(p,new SourceMarkPayload(false,BlockPos.ZERO));}}
    public static void clearAll(){SENT.clear();}
}
