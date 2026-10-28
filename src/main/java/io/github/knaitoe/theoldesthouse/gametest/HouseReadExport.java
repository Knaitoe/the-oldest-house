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
        boolean porch=room==HouseReadSites.Room.PORCH;
        int x0=porch?-20:-7,x1=porch?20:7,z0=porch?-44:-15,z1=porch?14:3;
        for(var rel:BlockPos.betweenClosed(x0,-1,z0,x1,porch?11:4,z1)){var s=l.getBlockState(b.offset(rel));if(s.isAir()||s.is(Blocks.LIGHT)||!porch&&(rel.getY()==4||rel.getY()>=0&&(rel.getX()==x1||rel.getZ()>=2))||porch&&rel.getY()>4&&(s.is(HouseReadRegistry.CLAPBOARD.get())||s.is(Blocks.DARK_OAK_SLAB)||s.is(Blocks.OAK_PLANKS)||s.is(Blocks.OAK_SLAB)))continue;int id=states.computeIfAbsent(s,k->{palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,k).getOrThrow());return states.size();});var row=new JsonArray();row.add(rel.getX());row.add(rel.getY());row.add(rel.getZ());row.add(id);blocks.add(row);}
        json.add("palette",palette);json.add("blocks",blocks);try{var dir=Path.of("../build/house-read138-proof");Files.createDirectories(dir);Files.writeString(dir.resolve(name+".json"),new Gson().toJson(json));}catch(Exception e){throw new IllegalStateException(e);}
    }
}
