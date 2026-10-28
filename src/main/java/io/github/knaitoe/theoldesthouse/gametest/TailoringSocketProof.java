package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Opt-in graphical proof with explicit fixture reads; ordinary games never execute this controller. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TailoringSocketProof {
    public static boolean enabled(){return Boolean.getBoolean("the_oldest_house.tailoringSocketSmoke");}
    public static Path folder(){return Path.of(System.getProperty("the_oldest_house.liveProofDir","build/tailoring-live-proof"));}
    private static final BlockPos ORIGIN=new BlockPos(6000,80,6000);
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static final Set<UUID> ACKS=new HashSet<>();
    private static final Map<UUID,UUID> SINKS=new HashMap<>();
    private static int phase,changed;private static boolean initialized,offline,reconnected;
    private TailoringSocketProof(){}
    private static void require(boolean okay,String text){if(!okay)throw new IllegalStateException("TAILORING SOCKET: "+text);}
    private static ServerPlayer player(MinecraftServer s,String role){return s.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().equals("OTHProof"+role)).findFirst().orElse(null);}
    private static void write(String name,String value){try{Files.createDirectories(folder());Files.writeString(folder().resolve(name),value);}catch(Exception e){throw new IllegalStateException(e);}}
    private static void read(ServerPlayer p,String trait,String value){var d=LabyrinthData.get(p.server);var own=d.stateEntry(HouseRead.STATE,p.getUUID().toString());var t=new CompoundTag();t.putString("Value",value);t.putString("Strength","SETTLED");own.put(trait,t);d.setStateEntry(HouseRead.STATE,p.getUUID().toString(),own);}
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){if(!enabled())return;e.getDispatcher().register(Commands.literal("tailoringproof").then(Commands.argument("step",IntegerArgumentType.integer(1,4)).executes(c->{var p=c.getSource().getPlayerOrException();if(p.getGameProfile().getName().startsWith("OTHProof")&&IntegerArgumentType.getInteger(c,"step")==phase)ACKS.add(p.getUUID());return 1;})));}
    private static BlockPos base(LabyrinthPlace place){return LabyrinthPlaces.base(ORIGIN,place);}
    private static BlockPos target(){return phase<=2?base(LabyrinthPlace.HOTEL).offset(VignetteRoutineEchoes.HOTEL_SINK):phase==3?base(LabyrinthPlace.LAKE_HOUSE).offset(VignetteRoutineEchoes.LAKE_VANITY):base(LabyrinthPlace.CHILD_ROOM).offset(LiteraryRooms.source(LabyrinthPlace.CHILD_ROOM));}
    private static void marker(ServerPlayer p){
        var tag=new CompoundTag();tag.putInt("TailoringStep",phase);tag.putLong("Target",target().asLong());tag.putBoolean("Reconnected",reconnected);
        if(phase<=3){var own=phase<=2?HotelVignette.personal(LabyrinthData.get(p.server),p.getUUID()):LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.LAKE_HOUSE);var prop=own.getCompound(VignetteRoutineEchoes.KEY);if(prop.hasUUID("Id"))tag.putUUID("Own",prop.getUUID("Id"));}
        var item=new ItemStack(Items.PAPER);item.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));p.getInventory().setItem(8,item);p.inventoryMenu.broadcastChanges();
    }
    private static void at(ServerPlayer p,ServerLevel l,BlockPos at,double side){p.closeContainer();p.stopRiding();p.setGameMode(GameType.SURVIVAL);p.teleportTo(l,at.getX()+.5+side,at.getY(),at.getZ()+1.8,180,0);p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();p.connection.resetPosition();p.hasChangedDimension();}
    private static void step(MinecraftServer s,int next){phase=next;changed=s.getTickCount();ACKS.clear();for(var p:s.getPlayerList().getPlayers())if(p.getGameProfile().getName().startsWith("OTHProof"))marker(p);TheOldestHouse.LOGGER.info("TAILORING SOCKET phase {}",phase);}
    private static void sink(ServerPlayer p,double side){var d=LabyrinthData.get(p.server);var b=base(LabyrinthPlace.HOTEL);at(p,p.server.getLevel(HouseDimensions.INTERIOR),b.offset(VignetteRoutineEchoes.HOTEL_SINK),side);var own=HotelVignette.personal(d,p.getUUID());VignetteRoutineEchoes.hotel(p,b,own);HotelVignette.save(d,p.getUUID(),own);var id=own.getCompound(VignetteRoutineEchoes.KEY).getUUID("Id");var original=SINKS.putIfAbsent(p.getUUID(),id);require(original==null||original.equals(id),"same-profile reconnect retained its original native display UUID");}
    private static void vanity(ServerPlayer p,double side){var d=LabyrinthData.get(p.server);var b=base(LabyrinthPlace.LAKE_HOUSE);at(p,p.server.getLevel(HouseDimensions.OUTSIDE),b.offset(VignetteRoutineEchoes.LAKE_VANITY),side);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.LAKE_HOUSE);VignetteRoutineEchoes.lake(p,b,own);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.LAKE_HOUSE,own);}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){if(!enabled())return;var s=event.getServer();try{
        var a=player(s,"A");var b=player(s,"B");
        if(phase==5){if(a==null&&b==null){CHUNKS.close();s.halt(false);}return;}
        if(phase==2&&b==null){offline=true;require(s.getTickCount()-changed<2400,"the same B profile reconnects within the original two-minute deadline");return;}
        if(a==null||b==null)return;
        if(!initialized){
            var house=HouseSavedData.get(s);house.markSpawned(ORIGIN);house.markInteriorInitialized();LabyrinthData.get(s).setBuilt(LabyrinthBuilder.VERSION,ORIGIN);
            var in=s.getLevel(HouseDimensions.INTERIOR);var out=s.getLevel(HouseDimensions.OUTSIDE);require(in!=null&&out!=null,"native scene dimensions exist");
            CHUNKS.hold(in,IndianLakeRooms.bounds(base(LabyrinthPlace.HOTEL),LabyrinthPlace.HOTEL));CHUNKS.hold(in,IndianLakeRooms.bounds(base(LabyrinthPlace.CHILD_ROOM),LabyrinthPlace.CHILD_ROOM));CHUNKS.hold(out,new AABB(base(LabyrinthPlace.LAKE_HOUSE).offset(VignetteRoutineEchoes.LAKE_VANITY)).inflate(15,6,15));
            HotelRooms.build(in,base(LabyrinthPlace.HOTEL),LabyrinthPlace.HOTEL);ChildRoom.build(in,base(LabyrinthPlace.CHILD_ROOM));LakeHouseInterior.build(out,base(LabyrinthPlace.LAKE_HOUSE));
            read(a,"HAIR","WRAPPED");read(b,"HAIR","BRUSHED_DRY");initialized=true;
        }
        if(phase==0){if(!CHUNKS.ready())return;sink(a,.75);sink(b,-.75);step(s,1);return;}
        require(s.getTickCount()-changed<2400,"both actual rendered clients complete phase "+phase+" within two minutes");
        if(phase==2&&offline&&!reconnected){sink(b,-.75);reconnected=true;marker(a);marker(b);write("reconnect-originals.txt","A="+SINKS.get(a.getUUID())+"\nB="+SINKS.get(b.getUUID())+"\nThe native same-profile B connection reuses its original selected item and entity UUID.\n");}
        if(!ACKS.containsAll(List.of(a.getUUID(),b.getUUID())))return;
        if(phase==1){step(s,2);return;}
        if(phase==2){require(reconnected,"phase two includes an actual B logout and reconnect");vanity(a,.75);vanity(b,-.75);step(s,3);return;}
        if(phase==3){var at=base(LabyrinthPlace.CHILD_ROOM).offset(LiteraryRooms.source(LabyrinthPlace.CHILD_ROOM));at(a,s.getLevel(HouseDimensions.INTERIOR),at,.6);at(b,s.getLevel(HouseDimensions.INTERIOR),at,-.6);step(s,4);return;}
        if(phase==4){
            var d=LabyrinthData.get(s);for(var p:List.of(a,b)){var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.CHILD_ROOM);require(ChildRoom.readCard(own)&&own.contains("Original_Source")&&!own.getBoolean("Taken_Source"),"the actual reader opened its finite saved original without a staged reading or Take");require(p.isAlive()&&p.getHealth()==20,"the actual players retain their original health");}
            write("passed.txt","Both actual native clients saw only their own different routine object at the same physical sink and vanity. B reconnected with the same original sink UUID/item; A remained live. Both opened the real bedtime-card surface, selected the added native page and captured its rendered original words. No automatic category assignment was staged as a gameplay action: fixture reads were explicit.\n");step(s,5);
        }
    }catch(Throwable ex){write("failed.txt",ex.toString()+"\n");s.halt(false);throw new IllegalStateException(ex);}}
}
