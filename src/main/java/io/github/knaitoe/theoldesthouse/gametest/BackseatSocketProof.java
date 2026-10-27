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
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Opt-in real socket input, private roads and same-profile locked-seat recovery. No ordinary game reads the markers. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BackseatSocketProof {
    public static boolean enabled(){return Boolean.getBoolean("the_oldest_house.backseatSocketSmoke");}
    public static Path folder(){return Path.of(System.getProperty("the_oldest_house.liveProofDir","build/backseat-live-proof"));}
    private static final BlockPos ORIGIN=new BlockPos(6000,80,6000);
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();
    private static final Set<UUID> ACKS=new HashSet<>();
    private static final Map<UUID,UUID> SEATS=new HashMap<>(),BOOKS=new HashMap<>();
    private static final Map<UUID,ItemStack> ITEMS=new HashMap<>();
    private static int phase,changed;private static boolean offline,reconnected;
    private BackseatSocketProof(){}
    private static void require(boolean okay,String message){if(!okay)throw new IllegalStateException("BACKSEAT SOCKET: "+message);}
    private static ServerPlayer player(MinecraftServer s,String role){return s.getPlayerList().getPlayers().stream().filter(p->p.getGameProfile().getName().equals("OTHProof"+role)).findFirst().orElse(null);}
    private static LabyrinthPlace hall(ServerPlayer p){return p.getGameProfile().getName().endsWith("A")?LabyrinthPlace.ALCOVE_HALL:LabyrinthPlace.OFFSET_HALL;}
    private static BlockPos source(ServerPlayer p){return LabyrinthPlaces.base(ORIGIN,hall(p)).offset(0,0,1);}
    private static void write(String name,String text){try{Files.createDirectories(folder());Files.writeString(folder().resolve(name),text);}catch(Exception ex){throw new IllegalStateException(ex);}}
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){if(!enabled())return;
        e.getDispatcher().register(Commands.literal("backseatproof").then(Commands.argument("phase",IntegerArgumentType.integer(1,8)).executes(c->{var p=c.getSource().getPlayerOrException();if(p.getGameProfile().getName().startsWith("OTHProof")&&IntegerArgumentType.getInteger(c,"phase")==phase)ACKS.add(p.getUUID());return 1;})));
    }
    private static void marker(ServerPlayer p){
        var t=new CompoundTag();t.putInt("BackseatStep",phase);t.putInt("HostAge",p.server.getTickCount());t.putLong("Source",source(p).asLong());t.putBoolean("Reconnected",reconnected);
        var b=BackseatSites.base(p.server,p.getUUID());if(b!=null)t.putLong("Road",b.asLong());
        var item=new ItemStack(Items.PAPER);item.set(DataComponents.CUSTOM_DATA,CustomData.of(t));p.getInventory().setItem(8,item);p.inventoryMenu.broadcastChanges();
    }
    private static void step(MinecraftServer s,int next){phase=next;changed=s.getTickCount();ACKS.clear();for(var p:s.getPlayerList().getPlayers())if(p.getGameProfile().getName().startsWith("OTHProof"))marker(p);TheOldestHouse.LOGGER.info("BACKSEAT SOCKET phase {}",phase);}
    private static void stage(ServerPlayer p){
        var l=p.server.getLevel(HouseDimensions.INTERIOR);var from=source(p);var hall=hall(p);var base=LabyrinthPlaces.base(ORIGIN,hall);CHUNKS.hold(l,new AABB(base).inflate(32));
        LabyrinthHalls.build(l,base,hall);LabyrinthBuilder.registerDoors(LabyrinthData.get(p.server),hall,base);
        var original=LabyrinthData.get(p.server).doorAt(l.dimension(),from);require(original!=null,"the source is an actual registered hallway door");original.destination=BackseatSites.SOURCE;
        p.setGameMode(GameType.SURVIVAL);p.setNoGravity(false);p.getAbilities().flying=false;p.getAbilities().mayfly=false;p.onUpdateAbilities();p.getInventory().clearContent();
        var item=new ItemStack(Items.DIAMOND,3);item.set(DataComponents.CUSTOM_NAME,Component.literal("Original carried by "+p.getGameProfile().getName()));ITEMS.put(p.getUUID(),item.copy());p.getInventory().setItem(4,item);
        p.teleportTo(l,from.getX()+.5,from.getY(),from.getZ()-2.5,0,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();
    }
    private static boolean both(ServerPlayer a,ServerPlayer b){return ACKS.contains(a.getUUID())&&ACKS.contains(b.getUUID());}
    private static void originals(ServerPlayer p){
        require(ItemStack.matches(ITEMS.get(p.getUUID()),p.getInventory().getItem(4)),"exact carried item components/count survive input, save and return");
        var car=BackseatSites.car(p);require(car!=null&&car.seatId!=null&&car.seatId.equals(SEATS.get(p.getUUID())),"the original saved seat UUID remains");
        long books=java.util.stream.IntStream.range(0,p.getInventory().getContainerSize()).mapToObj(i->p.getInventory().getItem(i)).filter(i->{var d=i.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().hasUUID("BackseatOriginal")&&d.copyTag().getUUID("BackseatOriginal").equals(BOOKS.get(p.getUUID()));}).count();
        if(p.getGameProfile().getName().endsWith("A"))require(car.notebook.isEmpty()&&books==1,"the taken original notebook exists exactly once in its owner's pack");
        else require(!car.notebook.isEmpty()&&books==0&&car.notebook.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("BackseatOriginal").equals(BOOKS.get(p.getUUID())),"the unread optional notebook remains on its own cushion");
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(!enabled())return;var s=e.getServer();var a=player(s,"A");var b=player(s,"B");
        try{
            if(phase==0){if(a==null||b==null)return;var house=HouseSavedData.get(s);house.markSpawned(ORIGIN);house.markInteriorInitialized();LabyrinthData.get(s).setBuilt(LabyrinthBuilder.VERSION,ORIGIN);stage(a);stage(b);step(s,1);return;}
            if(phase==8){if(s.getTickCount()-changed>100){CHUNKS.close();s.halt(false);}return;}
            require(s.getTickCount()-changed<4800,"actual client phase timed out: "+phase);
            if(phase==5&&b==null&&!offline){offline=true;s.saveEverything(false,true,true);write("restart-b.txt","The same-profile driver is genuinely offline while the original car remains locked.\n");}
            if(a==null||b==null)return;
            if(phase==5&&offline&&!reconnected){reconnected=true;ACKS.clear();originals(b);require(BackseatSites.car(b).phase==BackseatCarBlock.Car.LOCKED,"native login retains the locked phase");write("reconnect-originals.txt","Same profile, seat="+SEATS.get(b.getUUID())+"; original unread notebook="+BOOKS.get(b.getUUID())+" and carried stack retained.\n");}
            if(s.getTickCount()%20==0){marker(a);marker(b);}
            if(phase==1){
                for(var p:List.of(a,b)){var road=BackseatSites.base(s,p.getUUID());if(road!=null&&BackseatSites.state(s,p.getUUID()).getBoolean("Built"))CHUNKS.hold(s.getLevel(HouseDimensions.OUTSIDE),BackseatRooms.bounds(road));}
                if(!CHUNKS.ready()||!both(a,b))return;
                require(BackseatSites.inside(a)&&BackseatSites.inside(b)&&!BackseatSites.base(s,a.getUUID()).equals(BackseatSites.base(s,b.getUUID())),"actual source-door input reaches two independent private roads");
                for(var p:List.of(a,b)){var car=BackseatSites.car(p);require(car!=null&&car.sequenceEnabled&&LabyrinthData.get(s).returnDepth(p.getUUID())==1,"confirmed road entry preserves the actual source return");BOOKS.put(p.getUUID(),car.notebook.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("BackseatOriginal"));}step(s,2);
            }else if(phase==2&&both(a,b)){
                for(var p:List.of(a,b)){var car=BackseatSites.car(p);require(car!=null&&car.phase==BackseatCarBlock.Car.SEATED&&p.getVehicle()==car.seat(),"ordinary walking and block input boards the real original seat");SEATS.put(p.getUUID(),car.seatId);}step(s,3);
            }else if(phase==3&&both(a,b)){originals(a);originals(b);step(s,4);}
            else if(phase==4&&both(a,b)){for(var p:List.of(a,b)){require(BackseatSites.car(p).phase==BackseatCarBlock.Car.LOCKED,"actual fitted door input locks each independent car");originals(p);}step(s,5);}
            else if(phase==5&&reconnected&&both(a,b)){for(var p:List.of(a,b)){originals(p);require(BackseatSites.car(p).phase==BackseatCarBlock.Car.LOCKED&&p.getVehicle()==BackseatSites.car(p).seat(),"both actual clients resume the original locked seats without a countdown reveal");}step(s,6);}
            else if(phase==6&&both(a,b)){
                require(BackseatSites.state(s,a.getUUID()).getBoolean("Resolved")&&a.serverLevel().dimension().equals(HouseDimensions.INTERIOR),"first driver returns through a confirmed real House landing");
                require(!BackseatSites.state(s,b.getUUID()).getBoolean("Resolved")&&BackseatSites.car(b).phase==BackseatCarBlock.Car.LOCKED&&LabyrinthData.get(s).returnDepth(b.getUUID())==1,"first crash consumes none of the peer's state or return");originals(a);originals(b);write("peer-independent.txt","A's actual crash and House return leave B locked on B's original road with its original return and finite notebook.\n");step(s,7);
            }else if(phase==7&&both(a,b)){
                for(var p:List.of(a,b)){originals(p);require(BackseatSites.state(s,p.getUUID()).getBoolean("Resolved")&&LabyrinthData.get(s).returnDepth(p.getUUID())==0&&p.position().distanceToSqr(Vec3.atBottomCenterOf(source(p).south()))<.1,"each physical collision consumes only its own confirmed source return");require(p.getHealth()==p.getMaxHealth(),"native cabin and impact cause no suffocation or inventory-changing death");}
                write("passed.txt","Two actual clients used the native source doors, walked the private rainy midnight roads, boarded the production seats, handled one finite optional notebook and closed the physical doors. The second profile disconnected and rejoined the same locked original. Real camera turns caused native moving-car/tree collisions and confirmed independent House returns. First-person cabin, radio/rain cutoff, live changing mirror, decoded private impact cues, original UUIDs/items and camera restoration were checked on the clients.\n");step(s,8);
            }
        }catch(Throwable ex){write("failed.txt",ex.toString()+"\n");s.halt(false);throw new IllegalStateException(ex);}
    }
}
