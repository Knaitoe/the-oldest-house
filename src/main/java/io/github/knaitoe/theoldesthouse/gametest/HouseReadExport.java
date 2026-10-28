package io.github.knaitoe.theoldesthouse.gametest;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

final class HouseReadExport {
    static void save(ServerLevel l,BlockPos b,HouseReadSites.Room room,String name){
        var json=new JsonObject();json.addProperty("name",name);var palette=new JsonArray();var blocks=new JsonArray();Map<BlockState,Integer> states=new LinkedHashMap<>();
        var area=HouseReadRooms.bounds(BlockPos.ZERO,room);
        int x0=room==HouseReadSites.Room.BATHROOM?-6:(int)area.minX+1,x1=room==HouseReadSites.Room.WAITING||room==HouseReadSites.Room.COATROOM?7:room==HouseReadSites.Room.BATHROOM?5:(int)area.maxX-2,z0=(int)area.minZ+1,z1=(int)area.maxZ-2,y0=room==HouseReadSites.Room.SHELTER?-2:-1,y1=room==HouseReadSites.Room.PORCH?11:room==HouseReadSites.Room.SHELTER?6:4;
        for(var rel:BlockPos.betweenClosed(x0,y0,z0,x1,y1,z1)){var s=l.getBlockState(b.offset(rel));boolean porch=room==HouseReadSites.Room.PORCH,shelter=room==HouseReadSites.Room.SHELTER;
            if(s.isAir()||s.is(Blocks.LIGHT)||!porch&&!shelter&&(rel.getY()==4||rel.getY()>=0&&(rel.getX()==x1||rel.getZ()>=2))||porch&&rel.getY()>4&&(s.is(HouseReadRegistry.CLAPBOARD.get())||s.is(Blocks.DARK_OAK_SLAB)||s.is(Blocks.OAK_PLANKS)||s.is(Blocks.OAK_SLAB))||shelter&&(rel.getY()>=5||rel.getY()>=0&&(rel.getX()>20||rel.getZ()>2)))continue;
            int id=states.computeIfAbsent(s,k->{palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,k).getOrThrow());return states.size();});var row=new JsonArray();row.add(rel.getX());row.add(rel.getY());row.add(rel.getZ());row.add(id);blocks.add(row);
        }
        json.add("palette",palette);json.add("blocks",blocks);try{var dir=Path.of("../build/house-read139-proof");Files.createDirectories(dir);Files.writeString(dir.resolve(name+".json"),new Gson().toJson(json));}catch(Exception e){throw new IllegalStateException(e);}
    }
}
