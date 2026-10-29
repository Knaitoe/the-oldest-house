package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * The five 0.4.142 hallway doors. The quiet rooms now open from the junction (owner, October 28), so new
 * hallways never get these doors and saved ones are walled up again, unseen, once nobody needs them.
 */
public final class HouseReadEntrance {
    private static final String STATE="house_read_entrance142";
    private static final int QUIET=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private HouseReadEntrance(){}
    public static String id(HouseReadSites.Room room){return "house_read_entrance/"+room.key();}
    /** Fixtures only: a saved world from 0.4.142 holds its doors and no record of any retired. */
    public static void forgetRetired(LabyrinthData data){data.setStateEntry(STATE,"Retired",new net.minecraft.nbt.CompoundTag());}
    public static HouseReadSites.Room room(LabyrinthData.Door door){
        for(var room:HouseReadSites.Room.values())if(id(room).equals(door.id))return room;
        return null;
    }
    /** Retires each saved hallway door once it is loaded, unwatched and no reader's active visit returns through it. */
    public static void retire(MinecraftServer server){
        var origin=HouseSavedData.get(server).houseOrigin();var level=server.getLevel(HouseDimensions.INTERIOR);if(origin==null||level==null)return;
        var data=LabyrinthData.get(server);var retired=data.stateEntry(STATE,"Retired");
        for(var room:HouseReadSites.Room.values()){
            if(retired.getBoolean(room.key()))continue;
            var door=data.door(id(room));
            if(door==null){retired.putBoolean(room.key(),true);data.setStateEntry(STATE,"Retired",retired);continue;}
            if(returning(server,data,door.id))continue;
            var at=door.lower;
            if(!level.isLoaded(at)||!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at)))continue;
            if(level.players().stream().anyMatch(p->new AABB(at).inflate(24).intersects(p.getCamera().getBoundingBox())))continue;
            // An edited wall keeps whatever the player left; only the original door halves are walled up.
            if(level.getBlockState(at).getBlock() instanceof DoorBlock&&level.getBlockState(at.above()).getBlock() instanceof DoorBlock){
                level.setBlock(at.above(),wall(level,at.above(),door.facing),QUIET);
                level.setBlock(at,wall(level,at,door.facing),QUIET);
            }
            data.removeDoor(door.id);retired.putBoolean(room.key(),true);data.setStateEntry(STATE,"Retired",retired);
        }
    }
    private static boolean returning(MinecraftServer server,LabyrinthData data,String door){
        for(String key:data.state(HouseReadSites.STATE).getAllKeys())try{
            var own=HouseReadSites.state(server,UUID.fromString(key));String active=own.getString("Active");
            if(!active.isEmpty()&&door.equals(own.getCompound(active).getString("SourceDoor")))return true;
        }catch(IllegalArgumentException ignored){}
        return false;
    }
    /** The same course of wall as its neighbours along the hallway. */
    private static BlockState wall(ServerLevel level,BlockPos at,Direction facing){
        var along=facing.getClockWise();
        for(var side:new BlockPos[]{at.relative(along),at.relative(along.getOpposite())}){
            var state=level.getBlockState(side);
            if(!state.isAir()&&!(state.getBlock() instanceof DoorBlock)&&!state.hasBlockEntity())return state;
        }
        return Blocks.WHITE_TERRACOTTA.defaultBlockState();
    }
}
