package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Private physical bins, finite originals and a personal return. No monster or shared completion flag. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SiloSites {
    public static final String SOURCE="place:silo",STATE="silo_sites_04135";
    private static final TicketType<ChunkPos> TICKET=TicketType.create("silo_site",Comparator.comparingLong(ChunkPos::toLong));
    private static final LinkedHashSet<UUID> WAITING=new LinkedHashSet<>();
    private static final Map<UUID,Integer> REQUESTED=new HashMap<>();
    private static final Map<UUID,String> PENDING_ENTRY=new LinkedHashMap<>();
    private static final Set<UUID> LISTENING=new HashSet<>();
    private static final List<ChunkPos> CHUNKS=new ArrayList<>();
    private static LabyrinthData store;private static UUID building;private static ServerLevel leased;private static BuildBlocks.Plan plan;
    private SiloSites(){}
    public static CompoundTag state(MinecraftServer s,UUID id){return LabyrinthData.get(s).stateEntry(STATE,id.toString());}
    private static void save(MinecraftServer s,UUID id,CompoundTag state){LabyrinthData.get(s).setStateEntry(STATE,id.toString(),state);}
    public static boolean canDeal(LabyrinthData d,UUID id){return !d.stateEntry(STATE,id.toString()).getBoolean("Resolved")&&!WitnessAccount.has(d,id,WitnessAccount.Story.SILO);}
    public static @Nullable BlockPos base(MinecraftServer s,UUID id){var t=state(s,id);var origin=HouseSavedData.get(s).houseOrigin();return origin==null||!t.contains("Index")?null:new BlockPos(origin.getX()+8192+t.getInt("Index")*128,80,origin.getZ()-458752);}
    public static boolean inside(ServerPlayer p){var b=base(p.server,p.getUUID());return b!=null&&state(p.server,p.getUUID()).getBoolean("Built")&&p.level().dimension().equals(HouseDimensions.OUTSIDE)&&SiloRooms.bounds(b).contains(p.position());}
    public static List<AABB> areas(MinecraftServer s){var out=new ArrayList<AABB>();for(var key:LabyrinthData.get(s).state(STATE).getAllKeys())try{var b=base(s,UUID.fromString(key));if(b!=null)out.add(SiloRooms.bounds(b));}catch(IllegalArgumentException ignored){}return out;}
    public static boolean protectedPosition(MinecraftServer s,BlockPos at){return areas(s).stream().anyMatch(box->box.contains(Vec3.atCenterOf(at)));}
    public static @Nullable LabyrinthData.Door entry(ServerPlayer p){return LabyrinthData.get(p.server).door("silo/"+p.getUUID()+"/entry");}
    public static @Nullable LabyrinthData.Door prepareEntry(ServerPlayer p){
        var d=LabyrinthData.get(p.server);if(store!=d){clear();store=d;}var t=state(p.server,p.getUUID());if(t.getBoolean("Resolved"))return null;
        if(!t.contains("Index")){var world=d.stateEntry(STATE,"World");int next=world.getInt("NextIndex");if(next>=64000)return null;t.putInt("Index",next);world.putInt("NextIndex",next+1);d.setStateEntry(STATE,"World",world);save(p.server,p.getUUID(),t);}
        REQUESTED.put(p.getUUID(),p.server.getTickCount());var b=base(p.server,p.getUUID());var l=p.server.getLevel(HouseDimensions.OUTSIDE);
        if(t.getBoolean("Built")&&l!=null&&l.getChunkSource().getChunkNow(b.getX()>>4,b.getZ()>>4)!=null&&l.areEntitiesLoaded(ChunkPos.asLong(b)))return entry(p);
        if(!p.getUUID().equals(building))WAITING.add(p.getUUID());return null;
    }
    public static boolean enter(ServerPlayer p,LabyrinthData.Door from){
        var d=LabyrinthData.get(p.server);var deal=d.deal(p.getUUID(),from);boolean source=SOURCE.equals(from.destination)||LabyrinthData.DEALT.equals(from.destination)&&deal!=null&&deal.place().equals(LabyrinthPlace.SILO.id());
        if(!LiteraryVignettes.participant(p)||LabyrinthDoors.isBusy(p)||p.level().dimension()!=from.dimension||p.distanceToSqr(from.lower.getCenter())>36||d.door(from.id)!=from||!source||!canDeal(d,p.getUUID()))return false;
        var doorway=prepareEntry(p);if(doorway==null){PENDING_ENTRY.put(p.getUUID(),from.id);return false;}PENDING_ENTRY.remove(p.getUUID());var back=new LabyrinthData.Waypoint(from.dimension,Vec3.atBottomCenterOf(from.lower),from.facing.toYRot(),true);
        return HouseTransitionEvents.beginDoorTransition(p,doorway.dimension,null,arrived->{
            var current=LabyrinthData.get(arrived.server);current.pushReturn(arrived.getUUID(),back);var own=state(arrived.server,arrived.getUUID());own.putBoolean("Entered",true);own.putBoolean("Here",false);save(arrived.server,arrived.getUUID(),own);
            current.visit(arrived.getUUID(),LabyrinthPlace.SILO);HouseExperience.arrived(arrived,LabyrinthPlace.SILO);var origin=HouseSavedData.get(arrived.server).houseOrigin();LabyrinthDealer.arriveAt(current,arrived.getUUID(),LabyrinthPlace.SILO,origin==null?0:origin.asLong());
        },Vec3.atBottomCenterOf(doorway.lower.north(3)),180);
    }
    public static void examined(ServerPlayer p,int piece){if(!inside(p)||!LiteraryVignettes.participant(p))return;var own=state(p.server,p.getUUID());own.putInt("Examined",own.getInt("Examined")|(1<<piece));save(p.server,p.getUUID(),own);}
    public static void openedHatch(ServerPlayer p,BlockPos at){
        var b=base(p.server,p.getUUID());if(b==null||!inside(p)||!LiteraryVignettes.participant(p)||!at.equals(b.offset(SiloRooms.HATCH))||p.getY()<b.getY()+59||p.distanceToSqr(at.getCenter())>36)return;
        var own=state(p.server,p.getUUID());own.putBoolean("RoofOpened",true);save(p.server,p.getUUID(),own);
    }
    public static void depart(ServerPlayer p){var own=state(p.server,p.getUUID());boolean audible=LISTENING.remove(p.getUUID());if(!own.getBoolean("Here")&&!audible)return;stop(p);own.putBoolean("Here",false);save(p.server,p.getUUID(),own);}
    private static void stop(ServerPlayer p){for(var event:List.of(SiloRegistry.RADIO_TUCSON,SiloRegistry.RADIO_FLATBED,SiloRegistry.RADIO_WINTER))p.connection.send(new ClientboundStopSoundPacket(event.getId(),SoundSource.AMBIENT));}
    private static void voice(ServerPlayer p,BlockPos b,int index){
        var cue=switch(index){case 0->SiloRegistry.RADIO_TUCSON;case 1->SiloRegistry.RADIO_FLATBED;default->SiloRegistry.RADIO_WINTER;};
        p.connection.send(new ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(cue.get()),SoundSource.AMBIENT,b.getX()+.5,b.getY()-1,b.getZ()+.5,6,1,p.getRandom().nextLong()));
    }
    private static void reader(ServerPlayer p){
        if(!inside(p)||!LiteraryVignettes.participant(p)){depart(p);return;}if(LabyrinthDoors.isBusy(p))return;
        var b=base(p.server,p.getUUID());var own=state(p.server,p.getUUID());if(!own.getBoolean("Entered")||own.getBoolean("Resolved"))return;
        // Transient return callbacks do not survive a host restart; the unconsumed native return remains valid.
        if(own.getBoolean("Ending"))own.putBoolean("Ending",false);
        int age=own.getInt("Age");own.putInt("Age",age+1);own.putDouble("Highest",Math.max(own.getDouble("Highest"),p.getY()-b.getY()));
        int remaining=own.getInt("SpeechRemaining"),speaking=own.getInt("Speaking");
        if(LISTENING.add(p.getUUID())){own.putBoolean("Here",true);if(remaining>0){remaining=SiloAudio.TICKS[speaking];voice(p,b,speaking);}else if(!own.contains("NextVoice"))own.putInt("NextVoice",age+80);}
        if(remaining>0){
            own.putInt("SpeechRemaining",--remaining);
            if(remaining==0){own.putInt("Sentences",own.getInt("Sentences")+1);for(var wood:SiloRooms.BOARDS)if(p.serverLevel().getBlockEntity(b.offset(wood.at())) instanceof SiloTimberBlock.Timber timber)timber.sentenceFinished();own.putInt("NextVoice",age+220+speaking*50);}
        }else if(age>=own.getInt("NextVoice")){speaking=Math.floorMod(own.getInt("NextLine"),3);own.putInt("Speaking",speaking);own.putInt("NextLine",speaking+1);own.putInt("SpeechRemaining",SiloAudio.TICKS[speaking]);voice(p,b,speaking);}
        if(own.getBoolean("RoofOpened")&&!own.getBoolean("PaintedSeen")&&p.getY()>b.getY()+59){
            var hit=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),p.getEyePosition().add(p.getLookAngle().scale(7)),net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
            if(hit.getType()==HitResult.Type.BLOCK&&p.serverLevel().getBlockState(hit.getBlockPos()).is(SiloRegistry.SKY.get()))own.putBoolean("PaintedSeen",true);
        }
        boolean corn=p.onGround()&&p.serverLevel().getBlockState(p.blockPosition().below()).is(SiloRegistry.CORN.get());
        boolean returned=corn&&own.getBoolean("PaintedSeen")&&own.getDouble("Highest")>=59;
        save(p.server,p.getUUID(),own);
        if(returned&&!own.getBoolean("Ending")){
            own.putBoolean("Ending",true);save(p.server,p.getUUID(),own);stop(p);p.closeContainer();
            if(!LabyrinthDoors.sendBack(p,8,16,24,arrived->{var current=state(arrived.server,arrived.getUUID());current.putBoolean("Resolved",true);current.putBoolean("Ending",false);current.putBoolean("Here",false);save(arrived.server,arrived.getUUID(),current);WitnessAccount.resolve(arrived,WitnessAccount.Story.SILO,"opened_the_roof_and_returned_to_the_grain");})){
                own.putBoolean("Ending",false);save(p.server,p.getUUID(),own);
            }
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();advance(s);
        // A real door use may need chunk loading. Finish that same request only while its reader
        // remains beside the same source; preparing a site alone never moves anybody.
        for(var id:List.copyOf(PENDING_ENTRY.keySet())){
            var p=s.getPlayerList().getPlayer(id);var from=LabyrinthData.get(s).door(PENDING_ENTRY.get(id));
            if(p==null||from==null||!LiteraryVignettes.participant(p)||LabyrinthDoors.isBusy(p)||p.level().dimension()!=from.dimension||p.distanceToSqr(from.lower.getCenter())>36||!canDeal(LabyrinthData.get(s),id)){PENDING_ENTRY.remove(id);continue;}
            enter(p,from);
        }
        for(var p:List.copyOf(s.getPlayerList().getPlayers()))reader(p);
    }
    public static void advance(MinecraftServer s){
        var d=LabyrinthData.get(s);if(store!=d){clear();store=d;}
        while(building==null&&!WAITING.isEmpty()){var next=WAITING.iterator().next();WAITING.remove(next);if(s.getPlayerList().getPlayer(next)!=null)building=next;}
        if(building==null)return;var p=s.getPlayerList().getPlayer(building);var b=base(s,building);var l=s.getLevel(HouseDimensions.OUTSIDE);if(p==null||b==null||l==null){release();return;}
        var own=state(s,building);if(own.getBoolean("Built")&&(inside(p)||s.getTickCount()-REQUESTED.getOrDefault(building,0)>200)){release();return;}
        if(CHUNKS.isEmpty()){leased=l;var area=SiloRooms.bounds(b).inflate(2);for(int x=((int)area.minX)>>4;x<=((int)area.maxX)>>4;x++)for(int z=((int)area.minZ)>>4;z<=((int)area.maxZ)>>4;z++){var c=new ChunkPos(x,z);l.getChunkSource().addRegionTicket(TICKET,c,2,c);CHUNKS.add(c);}}
        if(CHUNKS.stream().anyMatch(c->l.getChunkSource().getChunkNow(c.x,c.z)==null||!l.areEntitiesLoaded(c.toLong())))return;
        if(own.getBoolean("Built"))return;
        if(plan==null){plan=BuildBlocks.record(l,()->SiloRooms.build(l,b));plan.resume(own.getCompound("BuildCursor"));}
        if(!plan.tick()){own.put("BuildCursor",plan.checkpoint());save(s,building,own);return;}
        SiloRooms.seed(l,b,building);d.putDoor(new LabyrinthData.Door("silo/"+building+"/entry",HouseDimensions.OUTSIDE,b.offset(SiloRooms.ENTRY),Direction.SOUTH,LabyrinthData.RETURN,false));
        own.putBoolean("Built",true);own.remove("BuildCursor");save(s,building,own);
    }
    private static void release(){if(leased!=null)for(var c:CHUNKS)leased.getChunkSource().removeRegionTicket(TICKET,c,2,c);CHUNKS.clear();leased=null;building=null;plan=null;}
    public static void clear(){release();WAITING.clear();REQUESTED.clear();PENDING_ENTRY.clear();LISTENING.clear();store=null;}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){PENDING_ENTRY.remove(p.getUUID());depart(p);}}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clear();}
}
