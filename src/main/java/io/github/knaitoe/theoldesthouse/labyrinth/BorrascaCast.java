package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Short bounded leases bridge original and destination sections on low view-distance servers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaCast {
    private static final TicketType<UUID> TICKET=TicketType.create("borrasca_original_cast",UUID::compareTo);
    private record Lease(ServerLevel level,List<ChunkPos> chunks){}
    private static final Map<UUID,Lease> leases=new HashMap<>();
    private static LabyrinthData store;
    private BorrascaCast(){}
    public static BlockPos target(String key,int visit){return switch(key){case "CastKyle"->visit>=4?new BlockPos(36,-4,-57):visit==3?new BlockPos(-3,6,-95):new BlockPos(15,0,-24);case "CastKimber"->visit>=3?new BlockPos(39,-4,-57):new BlockPos(19,0,-24);case "CastSheriff"->visit>=4?new BlockPos(-19,12,-173):new BlockPos(-18,0,-26);case "CastPrescott"->visit>=3?new BlockPos(15,12,-163):new BlockPos(24,0,-33);default->throw new IllegalArgumentException(key);};}
    public static void prepare(ServerPlayer p,BlockPos base,CompoundTag own){var d=LabyrinthData.get(p.server);if(store!=d){clear();store=d;}if(leases.containsKey(p.getUUID()))return;var chunks=new LinkedHashSet<ChunkPos>();for(String key:List.of("CastKyle","CastKimber","CastSheriff","CastPrescott")){var from=own.contains(key+"At")?BlockPos.of(own.getLong(key+"At")):base.offset(target(key,1));var to=base.offset(target(key,own.getInt("Visit")));chunks.add(new ChunkPos(from));chunks.add(new ChunkPos(to));}var held=List.copyOf(chunks);for(var c:held)p.serverLevel().getChunkSource().addRegionTicket(TICKET,c,3,p.getUUID());leases.put(p.getUUID(),new Lease(p.serverLevel(),held));}
    public static void remember(CompoundTag own,String key,LiteraryActor actor){if(actor!=null)own.putLong(key+"At",actor.blockPosition().asLong());}
    public static void release(UUID id){var lease=leases.remove(id);if(lease!=null)for(var c:lease.chunks)lease.level.getChunkSource().removeRegionTicket(TICKET,c,3,id);}
    public static void clear(){for(var id:List.copyOf(leases.keySet()))release(id);store=null;}
    public static int heldChunks(){return leases.values().stream().mapToInt(l->l.chunks.size()).sum();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){release(e.getEntity().getUUID());}
    @SubscribeEvent public static void stop(ServerStoppedEvent e){clear();}
}
