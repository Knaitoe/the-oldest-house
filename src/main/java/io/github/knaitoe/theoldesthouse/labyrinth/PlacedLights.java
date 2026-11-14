package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.588 (owner, 14 November: "users should be able to remove player-placed torches/lights from the house").
 *
 * <p>The House lets a reader set a torch or a lantern down in its gray halls and on the great staircase (LabyrinthLighting,
 * LabyrinthHazards) and refuses every break, so a light once set could never be taken up again. Its own lamps are the same
 * blocks, so which is which has to be kept: every light a reader sets under those rules is recorded here, with who set it,
 * and a recorded light may be broken by any reader, with the game's own drop. The House's own lamps are not recorded and stay.
 *
 * <p>The record goes with the light when the House moves it (LabyrinthLighting.rearrangeOne, LabyrinthMaze.restoreMigration),
 * and is dropped when the light is taken up or is no longer there: the dark halls and the last sconce take lights without
 * breaking them, so the record is checked against the block whenever it is used, and every ten seconds where the place is loaded.
 *
 * <p>The light sink is left out: a light set there is the room's, which puts it out in twelve seconds. The cave and the hotel
 * keep their own rule (a torch there is its setter's alone) and are not touched.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PlacedLights {
    private PlacedLights(){}
    public static final String ID="placed_lights";
    private static final String LIGHTS="Lights";
    /** How often the record is checked against the blocks, in ticks. */
    public static final int PRUNE=200;

    private static CompoundTag lights(LabyrinthData d){return d.state(ID).getCompound(LIGHTS);}
    private static void save(LabyrinthData d,CompoundTag lights){var state=d.state(ID);if(lights.isEmpty())state.remove(LIGHTS);else state.put(LIGHTS,lights);d.setState(ID,state);}
    private static String key(BlockPos at){return Long.toString(at.asLong());}

    /** Whether the House's general rules let a reader set this light here and keep it: the gray halls and the great staircase, not the light sink. */
    public static boolean general(Level level,BlockPos at,BlockState light){
        if(level.getServer()==null||!(LabyrinthLighting.allowsPlacing(level,at,light)||LabyrinthHazards.allowsPlacing(level,at,light)))return false;
        var origin=HouseSavedData.get(level.getServer()).houseOrigin();
        return origin==null||LabyrinthPlaces.placeAt(origin,at)!=LabyrinthPlace.LIGHT_SINK;
    }
    /** Who set the light standing here, if a reader did. */
    public static @Nullable UUID setter(MinecraftServer server,BlockPos at){var lights=lights(LabyrinthData.get(server));String key=key(at);return lights.hasUUID(key)?lights.getUUID(key):null;}
    /** How many lights readers have set that still stand, as far as the record knows. */
    public static int count(MinecraftServer server){return lights(LabyrinthData.get(server)).size();}
    public static void record(MinecraftServer server,BlockPos at,UUID who){var d=LabyrinthData.get(server);var lights=lights(d);lights.putUUID(key(at),who);save(d,lights);}
    public static void forget(MinecraftServer server,BlockPos at){var d=LabyrinthData.get(server);var lights=lights(d);if(!lights.contains(key(at)))return;lights.remove(key(at));save(d,lights);}
    /** The House has moved a light, or lost it (no place to go): a reader's light stays a reader's where it now stands. */
    public static void moved(MinecraftServer server,BlockPos from,@Nullable BlockPos to){
        if(from.equals(to))return;var d=LabyrinthData.get(server);var lights=lights(d);String key=key(from);if(!lights.hasUUID(key))return;
        var who=lights.getUUID(key);lights.remove(key);if(to!=null)lights.putUUID(key(to),who);save(d,lights);
    }
    /** A light a reader set down may be taken up again, by any reader. The House's own may not. */
    public static boolean mayBreak(ServerLevel level,BlockPos at){
        if(!level.dimension().equals(HouseDimensions.INTERIOR)||setter(level.getServer(),at)==null)return false;
        if(LabyrinthLighting.isPortableLight(level.getBlockState(at)))return true;
        forget(level.getServer(),at);return false;
    }

    /** Runs after every protection: a light that really stands is recorded as its setter's. */
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void placed(BlockEvent.EntityPlaceEvent e){
        if(e.isCanceled()||!(e.getEntity() instanceof ServerPlayer p)||!(e.getLevel() instanceof ServerLevel l)||!general(l,e.getPos(),e.getPlacedBlock()))return;
        record(p.server,e.getPos(),p.getUUID());
    }
    /** Runs after every protection: a light that is really taken up is forgotten. */
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void broken(BlockEvent.BreakEvent e){
        if(e.isCanceled()||!(e.getLevel() instanceof ServerLevel l)||!l.dimension().equals(HouseDimensions.INTERIOR))return;
        forget(l.getServer(),e.getPos());
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%PRUNE!=0)return;prune(server);
    }
    /** Drops every record whose light is no longer there, where the place is loaded. */
    public static void prune(MinecraftServer server){
        var level=server.getLevel(HouseDimensions.INTERIOR);if(level==null)return;
        var d=LabyrinthData.get(server);var lights=lights(d);if(lights.isEmpty())return;boolean changed=false;
        for(String key:new ArrayList<>(lights.getAllKeys())){
            BlockPos at;try{at=BlockPos.of(Long.parseLong(key));}catch(NumberFormatException bad){lights.remove(key);changed=true;continue;}
            if(level.isLoaded(at)&&!LabyrinthLighting.isPortableLight(level.getBlockState(at))){lights.remove(key);changed=true;}
        }
        if(changed)save(d,lights);
    }
}
