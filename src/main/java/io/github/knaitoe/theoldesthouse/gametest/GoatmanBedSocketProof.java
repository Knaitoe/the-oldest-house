package io.github.knaitoe.theoldesthouse.gametest;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Two real clients must use the real west/east beds and capture the resulting native main cameras. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GoatmanBedSocketProof {
    private static final BlockPos ORIGIN=new BlockPos(7200,80,7200);
    private static final NativeTestChunks CHUNKS=new NativeTestChunks();private static final Set<UUID> SEEN=new HashSet<>();
    private static int phase,started;private static boolean built;
    private GoatmanBedSocketProof(){}
    public static boolean enabled(){return Boolean.getBoolean("the_oldest_house.goatmanBedSocketSmoke");}
    public static Path folder(){return Path.of(System.getProperty("the_oldest_house.liveProofDir","build/goatman-bed137-proof"));}
    private static void write(String name,String text){try{Files.createDirectories(folder());Files.writeString(folder().resolve(name),text);}catch(java.io.IOException e){throw new IllegalStateException(e);}}
    private static void save(LabyrinthData d,CompoundTag r){var all=d.state(GoatmanVignette.ID);all.put("Run",r);d.setState(GoatmanVignette.ID,all);}
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){if(!enabled())return;e.getDispatcher().register(Commands.literal("goatbedproof137").executes(c->{var p=c.getSource().getPlayerOrException();if(p.getGameProfile().getName().matches("OTHProof[AB]")&&p.isSleeping()&&GoatmanBedtime.locked(p))SEEN.add(p.getUUID());return 1;}));}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(!enabled())return;var s=e.getServer();var a=s.getPlayerList().getPlayerByName("OTHProofA");var other=s.getPlayerList().getPlayerByName("OTHProofB");
        try{
            if(phase==0){
                if(a==null||other==null)return;var l=s.getLevel(HouseDimensions.INTERIOR);var b=LabyrinthPlaces.base(ORIGIN,LabyrinthPlace.GOATMAN);
                if(!built){var house=new HouseSavedData();house.markSpawned(ORIGIN);house.markInteriorInitialized();s.overworld().getDataStorage().set("the_oldest_house",house);var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,ORIGIN);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);CHUNKS.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN).inflate(2));GoatmanVignette.build(s,l,b);LabyrinthBuilder.registerDoors(d,LabyrinthPlace.GOATMAN,b);HouseFurnishings.decorate(l,b,LabyrinthPlace.GOATMAN);VignetteArchitecture.decorateOnce(l,ORIGIN,LabyrinthPlace.GOATMAN);built=true;}
                if(!CHUNKS.ready())return;
                for(var p:List.of(a,other)){p.setGameMode(GameType.SURVIVAL);p.teleportTo(l,b.getX()+.5,b.getY()+1,b.getZ()-58.5,180,0);p.hasChangedDimension();p.connection.resetPosition();if(!GoatmanVignette.enter(p))return;}
                var d=LabyrinthData.get(s);var r=GoatmanVignette.run(d);r.putInt("Phase",GoatmanVignette.VIGIL);r.putInt("Clock",0);r.putInt("ExtraState",GoatmanVignette.X_INSIDE);r.putInt("RunnerState",GoatmanVignette.R_INSIDE);r.putBoolean("BedtimeCalled131",true);r.putInt("BedQuietAt131",0);r.putInt("BedCallAt131",0);r.putInt("BedTalk131",1);save(d,r);GoatmanWoods.beds(l,b,10);GoatmanWoods.atmosphere(l,b,true);
                for(var p:List.of(a,other)){int index=p==a?8:9;var head=b.offset(GoatmanWoods.bunkHead(index));p.teleportTo(l,b.getX()+(p==a?-3.5:4.5),b.getY()+1,head.getZ()+.5,180,0);p.connection.resetPosition();var t=new CompoundTag();t.putBoolean("BedCamera137",true);t.putLong("Head",head.asLong());t.putDouble("CenterX",b.getX()+.5);var marker=new ItemStack(Items.STICK);marker.set(DataComponents.CUSTOM_DATA,CustomData.of(t));p.getInventory().setItem(8,marker);p.inventoryMenu.broadcastChanges();}
                phase=1;started=s.getTickCount();return;
            }
            if(phase==1){
                if(s.getTickCount()-started>1800)throw new IllegalStateException("Both native bed cameras did not confirm");
                // Hold this isolated fixture before the haunting. Bed input, orientation, lock and render are production.
                var d=LabyrinthData.get(s);var r=GoatmanVignette.run(d);r.putInt("BedCallAt131",r.getInt("Clock"));save(d,r);
                if(a!=null&&other!=null&&SEEN.containsAll(List.of(a.getUUID(),other.getUUID()))){write("passed.txt","Two actual socket clients used the native west/east beds and verified the production main-camera direction over the inward feet toward the actual center table. Both actual pillows are at the outer walls, and both readers remain natively sleeping/locked. Native screenshots and camera records are required. This fixture holds before the haunting; the complete bedside sequence is covered separately.\n");phase=2;started=s.getTickCount();}
            }else if(phase==2&&s.getTickCount()-started>60){CHUNKS.close();s.halt(false);phase=3;}
        }catch(Throwable ex){write("failed.txt",ex.toString());throw new IllegalStateException(ex);}
    }
}
