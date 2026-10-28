package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;

/** Five ordinary doors before the far threshold, at stable positions in the original approach. */
public final class HouseReadEntrance {
    private static final String STATE="house_read_entrance142";
    private HouseReadEntrance(){}
    public static String id(HouseReadSites.Room room){return "house_read_entrance/"+room.key();}
    public static HouseReadSites.Room room(LabyrinthData.Door door){
        for(var room:HouseReadSites.Room.values())if(id(room).equals(door.id))return room;
        return null;
    }
    public static Direction facing(HouseReadSites.Room room){return room.ordinal()%2==0?Direction.EAST:Direction.WEST;}
    public static BlockPos lower(BlockPos origin,HouseReadSites.Room room){
        return origin.offset(HouseLayout.AXIS_X+(room.ordinal()%2==0?-2:2),1,
                HouseImpossibleHallway.END_Z_OFFSET-36+room.ordinal()*7);
    }
    /** A fresh hallway is empty. Saved hallways wait for loaded, unoccupied original wall cells. */
    public static void install(MinecraftServer server,boolean fresh){
        var house=HouseSavedData.get(server);var origin=house.houseOrigin();var level=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||level==null||(!fresh&&!house.isImpossibleDoorRevealed()))return;
        var data=LabyrinthData.get(server);var installed=data.stateEntry(STATE,Long.toString(origin.asLong()));
        for(var room:HouseReadSites.Room.values()){
            if(installed.getBoolean(room.key()))continue;
            var at=lower(origin,room);if(!level.isLoaded(at)||!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at)))continue;
            if(!fresh&&!level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new AABB(at).inflate(4),e->e.isAlive()).isEmpty())continue;
            var bottom=level.getBlockState(at);var top=level.getBlockState(at.above());
            // An edited wall, container or older original door keeps its actual native identity.
            if(level.getBlockEntity(at)!=null||level.getBlockEntity(at.above())!=null
                    ||!(bottom.is(Blocks.SPRUCE_PLANKS)||bottom.is(Blocks.WHITE_TERRACOTTA))||!top.is(Blocks.WHITE_TERRACOTTA))continue;
            LabyrinthBuilder.placeDoor(level,at,facing(room));
            data.putDoor(new LabyrinthData.Door(id(room),HouseDimensions.INTERIOR,at,facing(room),"house_read:entrance",false));
            installed.putBoolean(room.key(),true);data.setStateEntry(STATE,Long.toString(origin.asLong()),installed);
        }
    }
}
