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
    public static void verify() {
        var resources=Minecraft.getInstance().getResourceManager();var textures=new HashSet<ResourceLocation>();int models=0;
        try{
            for(var entry:resources.listResources("models",p->p.getNamespace().equals(TheOldestHouse.MOD_ID)&&p.getPath().endsWith(".json")).entrySet()){
                try(var reader=entry.getValue().openAsReader()){
                    var model=JsonParser.parseReader(reader).getAsJsonObject();models++;if(!model.has("textures"))continue;
                    for(var texture:model.getAsJsonObject("textures").entrySet()){String path=texture.getValue().getAsString();if(path.startsWith("#"))continue;var id=ResourceLocation.parse(path);textures.add(id.withPath("textures/"+id.getPath()+".png"));}
                }
            }
            for(var texture:textures)if(resources.getResource(texture).isEmpty())throw new IllegalStateException("Missing native model material: "+texture);
            Path dir=Path.of("../build/font-smoke");Files.createDirectories(dir);Files.writeString(dir.resolve("model-textures-passed.txt"),models+" native model definitions; "+textures.size()+" material paths resolved.\n");
        }catch(java.io.IOException e){throw new IllegalStateException("Could not verify native model materials",e);}
    }
}
