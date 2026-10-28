package io.github.knaitoe.theoldesthouse.gametest;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

final class GoatmanMealExport {
    private GoatmanMealExport(){}
    static void save(GoatmanTests.Fixture f,String name,int x0,int x1,int z0,int z1){
        var json=new JsonObject();json.addProperty("name",name);var palette=new JsonArray();var blocks=new JsonArray();var items=new JsonArray();Map<BlockState,Integer> states=new LinkedHashMap<>();
        for(var rel:BlockPos.betweenClosed(x0,0,z0,x1,5,z1)){
            var state=f.l.getBlockState(f.b.offset(rel));int x=rel.getX(),y=rel.getY(),z=rel.getZ();if(state.isAir()||state.is(Blocks.LIGHT)||state.is(Blocks.BARRIER)||y>0&&(x==x1||z==z1||y>=5))continue;
            int index=states.computeIfAbsent(state,k->{palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,k).getOrThrow());return states.size();});var row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(index);blocks.add(row);
        }
        var area=new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(f.b.offset(x0,0,z0)),net.minecraft.world.phys.Vec3.atLowerCornerOf(f.b.offset(x1+1,6,z1+1)));
        for(var entity:f.l.getEntitiesOfClass(Display.ItemDisplay.class,area,e->!e.isInvisible())){
            var item=GoatmanWoods.food(f.l,entity);if(item.isEmpty())continue;var data=entity.saveWithoutId(new CompoundTag());var t=data.getCompound("transformation");var row=new JsonObject();row.addProperty("item",BuiltInRegistries.ITEM.getKey(item.getItem()).toString());if(item.has(DataComponents.CUSTOM_MODEL_DATA))row.addProperty("model",item.get(DataComponents.CUSTOM_MODEL_DATA).value());row.addProperty("x",entity.getX()-f.b.getX());row.addProperty("y",entity.getY()-f.b.getY());row.addProperty("z",entity.getZ()-f.b.getZ());
            var scale=new JsonArray();var rotation=new JsonArray();for(var v:t.getList("scale",Tag.TAG_FLOAT))scale.add(((NumericTag)v).getAsFloat());for(var v:t.getList("left_rotation",Tag.TAG_FLOAT))rotation.add(((NumericTag)v).getAsFloat());row.add("scale",scale);row.add("rotation",rotation);items.add(row);
        }
        json.add("palette",palette);json.add("blocks",blocks);json.add("items",items);
        try{var folder=Path.of("../build/goatman137-proof");Files.createDirectories(folder);Files.writeString(folder.resolve(name+".json"),new Gson().toJson(json));}catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
}
