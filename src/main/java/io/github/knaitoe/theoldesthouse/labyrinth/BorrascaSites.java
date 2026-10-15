package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Append-only private outdoor sites. A completed site is never carved again, even after a layout update. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaSites {
    private static final String SITES="borrasca_sites_0497";
    private static final TicketType<ChunkPos> TICKET=TicketType.create("borrasca_build",Comparator.comparingLong(ChunkPos::toLong));
    private static final LinkedHashSet<UUID> waiting=new LinkedHashSet<>();
    private static LabyrinthData store;
    private static UUID building;
    private static ServerLevel leased;
    private static final List<ChunkPos> chunks=new ArrayList<>();
    private static BuildBlocks.Plan plan;
    private BorrascaSites(){}
    private static net.minecraft.nbt.CompoundTag site(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(SITES,id.toString());}
    public static boolean ready(MinecraftServer s,UUID id){return site(s,id).getBoolean("Built");}
    public static @Nullable BlockPos base(MinecraftServer s,UUID id){var t=site(s,id);var o=HouseSavedData.get(s).houseOrigin();if(o==null||!t.contains("Index"))return null;var b=LabyrinthPlaces.base(o,LabyrinthPlace.BORRASCA);return b==null?null:new BlockPos(b.getX()+4096+t.getInt("Index")*512,80,b.getZ());}
    public static AABB bounds(BlockPos b){return new AABB(b.getX()-44,b.getY()-5,b.getZ()-220,b.getX()+45,b.getY()+27,b.getZ()+18);}
    public static boolean inside(ServerPlayer p){var b=base(p.server,p.getUUID());return b!=null&&ready(p.server,p.getUUID())&&p.level().dimension().equals(HouseDimensions.OUTSIDE)&&bounds(b).contains(p.position());}
    public static @Nullable LabyrinthPlace placeAt(MinecraftServer s,BlockPos at){return areas(s).stream().anyMatch(a->a.contains(Vec3.atCenterOf(at)))?LabyrinthPlace.BORRASCA:null;}
    public static List<AABB> areas(MinecraftServer s){var data=LabyrinthData.get(s);var index=data.state(SITES);var out=new ArrayList<AABB>();for(var key:index.getAllKeys())try{var b=base(s,UUID.fromString(key));if(b!=null)out.add(bounds(b));}catch(IllegalArgumentException ignored){}return out;}
    public static boolean protectedPosition(MinecraftServer s,BlockPos pos){return placeAt(s,pos)!=null;}
    public static @Nullable LabyrinthData.Door entry(ServerPlayer p){return LabyrinthData.get(p.server).door("borrasca/"+p.getUUID()+"/entry");}
    /** Waiting does not consume a deal, visit, item, return or choice. The original door remains usable. */
    public static @Nullable LabyrinthData.Door prepareEntry(ServerPlayer p){
        var d=LabyrinthData.get(p.server);if(store!=d){clear();store=d;}var t=site(p.server,p.getUUID());
        if(!t.contains("Index")){var world=d.stateEntry(SITES,"World");int index=world.getInt("NextIndex");if(index>=32000)return null;t.putInt("Index",index);world.putInt("NextIndex",index+1);d.setStateEntry(SITES,"World",world);d.setStateEntry(SITES,p.getUUID().toString(),t);}
        if(t.getBoolean("Built"))return entry(p);waiting.add(p.getUUID());return null;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){advance(event.getServer());}
    public static void advance(MinecraftServer server){
        var d=LabyrinthData.get(server);if(store!=d){clear();store=d;}if(building==null){if(waiting.isEmpty())return;building=waiting.iterator().next();waiting.remove(building);}
        var b=base(server,building);var l=server.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null){release();return;}
        if(chunks.isEmpty()){leased=l;var box=bounds(b).inflate(2);for(int x=((int)box.minX)>>4;x<=((int)box.maxX)>>4;x++)for(int z=((int)box.minZ)>>4;z<=((int)box.maxZ)>>4;z++){var c=new ChunkPos(x,z);l.getChunkSource().addRegionTicket(TICKET,c,1,c);chunks.add(c);}}
        // Never call a synchronous chunk getter as a timeout fallback.
        if(chunks.stream().anyMatch(c->l.getChunkSource().getChunkNow(c.x,c.z)==null))return;
        if(plan==null){plan=BuildBlocks.record(l,()->BorrascaRooms.build(l,b));plan.resume(site(server,building).getCompound("BuildCursor"));}
        if(!plan.tick()){var progress=site(server,building);progress.put("BuildCursor",plan.checkpoint());d.setStateEntry(SITES,building.toString(),progress);return;}
        d.putDoor(new LabyrinthData.Door("borrasca/"+building+"/entry",HouseDimensions.OUTSIDE,b.offset(0,0,1),Direction.SOUTH,LabyrinthData.RETURN,false));
        var t=site(server,building);t.putBoolean("Built",true);t.remove("BuildCursor");d.setStateEntry(SITES,building.toString(),t);release();
    }
    private static void release(){if(leased!=null)for(var c:chunks)leased.getChunkSource().removeRegionTicket(TICKET,c,1,c);chunks.clear();leased=null;plan=null;building=null;}
    public static void clear(){release();waiting.clear();store=null;BorrascaCast.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){clear();}
    public static int heldChunks(){return chunks.size();}
}
