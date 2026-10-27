package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Finite private roads, constructed once in native bounded slices. Requests never fabricate a visit or return. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BackseatSites {
    public static final String SOURCE="place:backseat";
    private static final String STATE="backseat_sites_04132";
    private static final TicketType<ChunkPos> TICKET=TicketType.create("backseat_site",Comparator.comparingLong(ChunkPos::toLong));
    private static final LinkedHashSet<UUID> waiting=new LinkedHashSet<>();
    private static final Map<UUID,Integer> requested=new HashMap<>();
    private static LabyrinthData store;private static UUID building;private static ServerLevel leased;private static BuildBlocks.Plan plan;
    private static final List<ChunkPos> chunks=new ArrayList<>();
    private BackseatSites(){}
    public static boolean canDeal(LabyrinthData data,UUID id){return !data.stateEntry(STATE,id.toString()).getBoolean("Resolved")&&!WitnessAccount.has(data,id,WitnessAccount.Story.BACKSEAT);}
    public static CompoundTag state(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(STATE,id.toString());}
    public static @Nullable BlockPos base(MinecraftServer s,UUID id){var t=state(s,id);var o=HouseSavedData.get(s).houseOrigin();return o==null||!t.contains("Index")?null:new BlockPos(o.getX()+8192+t.getInt("Index")*256,80,o.getZ()-450560);}
    public static boolean inside(ServerPlayer p){var b=base(p.server,p.getUUID());return b!=null&&p.level().dimension().equals(HouseDimensions.OUTSIDE)&&state(p.server,p.getUUID()).getBoolean("Built")&&BackseatRooms.bounds(b).contains(p.position());}
    public static List<net.minecraft.world.phys.AABB> areas(MinecraftServer s){var out=new ArrayList<net.minecraft.world.phys.AABB>();for(var key:LabyrinthData.get(s).state(STATE).getAllKeys())try{var b=base(s,UUID.fromString(key));if(b!=null)out.add(BackseatRooms.bounds(b));}catch(IllegalArgumentException ignored){}return out;}
    public static boolean protectedPosition(MinecraftServer s,BlockPos at){return areas(s).stream().anyMatch(box->box.contains(Vec3.atCenterOf(at)));}
    public static @Nullable LabyrinthData.Door entry(ServerPlayer p){return LabyrinthData.get(p.server).door("backseat/"+p.getUUID()+"/entry");}
    public static @Nullable BackseatCarBlock.Car car(ServerPlayer p){var b=base(p.server,p.getUUID());var l=p.server.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null)return null;var at=b.offset(BackseatRooms.CAR);var chunk=l.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4);return chunk!=null&&chunk.getBlockEntity(at) instanceof BackseatCarBlock.Car c&&c.mine(p)?c:null;}
    public static @Nullable LabyrinthData.Door prepareEntry(ServerPlayer p){
        var d=LabyrinthData.get(p.server);if(store!=d){clear();store=d;}var t=state(p.server,p.getUUID());
        if(t.getBoolean("Resolved"))return null;
        if(!t.contains("Index")){var world=d.stateEntry(STATE,"World");int next=world.getInt("NextIndex");if(next>=64000)return null;t.putInt("Index",next);world.putInt("NextIndex",next+1);d.setStateEntry(STATE,"World",world);d.setStateEntry(STATE,p.getUUID().toString(),t);}
        requested.put(p.getUUID(),p.server.getTickCount());
        if(t.getBoolean("Built")&&car(p)!=null){var l=p.server.getLevel(HouseDimensions.OUTSIDE);var b=base(p.server,p.getUUID());if(l!=null&&l.areEntitiesLoaded(ChunkPos.asLong(b)))return entry(p);}
        if(!p.getUUID().equals(building))waiting.add(p.getUUID());return null;
    }
    /** An actual original source door, followed by a confirmed native move, creates the one personal return. */
    public static boolean enter(ServerPlayer p,LabyrinthData.Door from){
        var data=LabyrinthData.get(p.server);var dealt=data.deal(p.getUUID(),from);
        boolean source=SOURCE.equals(from.destination)||LabyrinthData.DEALT.equals(from.destination)&&dealt!=null&&dealt.place().equals(LabyrinthPlace.BACKSEAT.id());
        if(!p.isAlive()||p.isSpectator()||LabyrinthDoors.isBusy(p)||p.level().dimension()!=from.dimension||p.distanceToSqr(from.lower.getCenter())>36||data.door(from.id)!=from||!source||!canDeal(data,p.getUUID()))return false;
        var entry=prepareEntry(p);if(entry==null)return false;
        var back=new LabyrinthData.Waypoint(from.dimension,Vec3.atBottomCenterOf(from.lower),from.facing.toYRot(),true);
        var target=Vec3.atBottomCenterOf(entry.lower.north(3));
        return HouseTransitionEvents.beginDoorTransition(p,entry.dimension,null,arrived->{
            var current=LabyrinthData.get(arrived.server);current.pushReturn(arrived.getUUID(),back);
            var own=state(arrived.server,arrived.getUUID());own.putBoolean("Entered",true);current.setStateEntry(STATE,arrived.getUUID().toString(),own);
            current.visit(arrived.getUUID(),LabyrinthPlace.BACKSEAT);io.github.knaitoe.theoldesthouse.house.HouseExperience.arrived(arrived,LabyrinthPlace.BACKSEAT);
            var origin=HouseSavedData.get(arrived.server).houseOrigin();LabyrinthDealer.arriveAt(current,arrived.getUUID(),LabyrinthPlace.BACKSEAT,origin==null?0:origin.asLong());
            var car=car(arrived);if(car!=null)car.arm(arrived);
        },target,180);
    }
    static void returned(ServerPlayer p,BlockPos original){
        var b=base(p.server,p.getUUID());if(b==null||!b.offset(BackseatRooms.CAR).equals(original))return;
        var t=state(p.server,p.getUUID());if(!t.getBoolean("Entered")||t.getBoolean("Resolved"))return;
        var car=car(p);if(car==null||car.phase!=BackseatCarBlock.Car.DONE||!car.sequenceEnabled||car.travel<=0)return;
        t.putBoolean("Resolved",true);LabyrinthData.get(p.server).setStateEntry(STATE,p.getUUID().toString(),t);WitnessAccount.resolve(p,WitnessAccount.Story.BACKSEAT,"returned_from_the_car_among_the_pines");
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){advance(e.getServer());}
    public static void advance(MinecraftServer s){
        var d=LabyrinthData.get(s);if(store!=d){clear();store=d;}
        while(building==null&&!waiting.isEmpty()){var next=waiting.iterator().next();waiting.remove(next);if(s.getPlayerList().getPlayer(next)!=null)building=next;}
        if(building==null)return;
        var p=s.getPlayerList().getPlayer(building);var b=base(s,building);var l=s.getLevel(HouseDimensions.OUTSIDE);if(p==null||b==null||l==null){release();return;}
        var t=state(s,building);
        if(t.getBoolean("Built")&&(inside(p)||s.getTickCount()-requested.getOrDefault(building,0)>200)){release();return;}
        if(chunks.isEmpty()){leased=l;var box=BackseatRooms.bounds(b).inflate(2);for(int x=((int)box.minX)>>4;x<=((int)box.maxX)>>4;x++)for(int z=((int)box.minZ)>>4;z<=((int)box.maxZ)>>4;z++){var c=new ChunkPos(x,z);l.getChunkSource().addRegionTicket(TICKET,c,2,c);chunks.add(c);}}
        if(chunks.stream().anyMatch(c->l.getChunkSource().getChunkNow(c.x,c.z)==null||!l.areEntitiesLoaded(c.toLong())))return;
        if(t.getBoolean("Built"))return; // Loading a saved road never rebuilds or replenishes it.
        if(plan==null){plan=BuildBlocks.record(l,()->BackseatRooms.build(l,b));plan.resume(t.getCompound("BuildCursor"));}
        if(!plan.tick()){t.put("BuildCursor",plan.checkpoint());d.setStateEntry(STATE,building.toString(),t);return;}
        if(!BackseatCarBlock.place(l,b.offset(BackseatRooms.CAR),building))return;
        var car=(BackseatCarBlock.Car)l.getBlockEntity(b.offset(BackseatRooms.CAR));
        car.road=true;if(!car.notebookCreated){car.notebook=BackseatNotebook.original(building);car.notebookCreated=true;}car.sync();
        d.putDoor(new LabyrinthData.Door("backseat/"+building+"/entry",HouseDimensions.OUTSIDE,b.offset(0,0,1),Direction.SOUTH,LabyrinthData.RETURN,false));
        t.putBoolean("Built",true);t.remove("BuildCursor");d.setStateEntry(STATE,building.toString(),t);
    }
    private static void release(){if(leased!=null)for(var c:chunks)leased.getChunkSource().removeRegionTicket(TICKET,c,2,c);chunks.clear();leased=null;building=null;plan=null;}
    public static void clear(){release();waiting.clear();requested.clear();store=null;}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clear();}
}
