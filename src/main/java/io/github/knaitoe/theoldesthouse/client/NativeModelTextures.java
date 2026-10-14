package io.github.knaitoe.theoldesthouse.client;

import com.google.gson.JsonParser;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

/** Native resource checks catch a missing material even when its baked model itself exists. */
public final class NativeModelTextures {
    private NativeModelTextures(){}
    private static java.util.Map<String,String> materials(ResourceLocation model,java.util.Set<ResourceLocation> seen)throws java.io.IOException{
        var map=new java.util.HashMap<String,String>();if(!seen.add(model)||model.getPath().startsWith("builtin/"))return map;
        var resource=Minecraft.getInstance().getResourceManager().getResource(model.withPath("models/"+model.getPath()+".json"));if(resource.isEmpty())return map;
        try(var reader=resource.get().openAsReader()){var json=JsonParser.parseReader(reader).getAsJsonObject();if(json.has("parent"))map.putAll(materials(ResourceLocation.parse(json.get("parent").getAsString()),seen));if(json.has("textures"))for(var entry:json.getAsJsonObject("textures").entrySet())map.put(entry.getKey(),entry.getValue().getAsString());}return map;
    }
    public static void verify() {
        var resources=Minecraft.getInstance().getResourceManager();var textures=new HashSet<ResourceLocation>();int models=0;
        try{
            for(var entry:resources.listResources("models",p->p.getNamespace().equals(TheOldestHouse.MOD_ID)&&p.getPath().endsWith(".json")).entrySet()){
                try(var reader=entry.getValue().openAsReader()){
                    var model=JsonParser.parseReader(reader).getAsJsonObject();models++;if(!model.has("textures"))continue;
                    for(var texture:model.getAsJsonObject("textures").entrySet()){String path=texture.getValue().getAsString();if(path.startsWith("#"))continue;var id=ResourceLocation.parse(path);textures.add(id.withPath("textures/"+id.getPath()+".png"));}
                    if(model.has("elements")){var names=materials(entry.getKey().withPath(entry.getKey().getPath().substring(7,entry.getKey().getPath().length()-5)),new HashSet<>());
                        for(var element:model.getAsJsonArray("elements"))for(var face:element.getAsJsonObject().getAsJsonObject("faces").entrySet()){
                            String reference=face.getValue().getAsJsonObject().get("texture").getAsString();var seen=new HashSet<String>();
                            while(reference.startsWith("#")){if(!seen.add(reference)||!names.containsKey(reference.substring(1)))throw new IllegalStateException("Unresolved native material "+reference+" in "+entry.getKey());reference=names.get(reference.substring(1));}
                        }
                    }
                }
            }
            for(var texture:textures)if(resources.getResource(texture).isEmpty())throw new IllegalStateException("Missing native model material: "+texture);
            Path dir=Path.of("../build/font-smoke");Files.createDirectories(dir);Files.writeString(dir.resolve("model-textures-passed.txt"),models+" native model definitions; "+textures.size()+" material paths resolved.\n");
        }catch(java.io.IOException e){throw new IllegalStateException("Could not verify native model materials",e);}
    }
}
