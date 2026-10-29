package io.github.knaitoe.theoldesthouse.gametest;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/** LOCAL ONLY, never committed: builds the barn well and exports five views for the scene renderer. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_onward_probe") @PrefixGameTestTemplate(false)
public final class BarnProbe {
    static void export(ServerLevel l,BlockPos b,String name,int x0,int x1,int y0,int y1,int z0,int z1,float yaw)throws Exception{
        JsonObject file=new JsonObject();file.addProperty("name",name);file.addProperty("viewYaw",yaw);JsonArray palette=new JsonArray(),blocks=new JsonArray();Map<BlockState,Integer> lookup=new LinkedHashMap<>();
        for(int x=x0;x<=x1;x++)for(int y=y0;y<=y1;y++)for(int z=z0;z<=z1;z++){
            var s=l.getBlockState(b.offset(x,y,z));if(s.isAir()||s.is(Blocks.BARRIER)||s.is(Blocks.LIGHT))continue;
            Integer i=lookup.get(s);if(i==null){i=lookup.size();lookup.put(s,i);palette.add(BlockState.CODEC.encodeStart(JsonOps.INSTANCE,s).getOrThrow());}
            JsonArray row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(i);blocks.add(row);
        }
        file.add("palette",palette);file.add("blocks",blocks);var folder=Path.of("../build/barn-probe");Files.createDirectories(folder);Files.writeString(folder.resolve(name+".json"),new Gson().toJson(file));
    }
    @GameTest(template="empty",batch="barn_probe",timeoutTicks=200000)
    public static void barn(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);var place=LabyrinthPlace.BARN_WELL;
        h.startSequence().thenExecute(()->{var l=f.level(place);var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(l,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"resident"))
            .thenExecute(()->{try{var m=LabyrinthBuilder.class.getDeclaredMethod("build",net.minecraft.server.MinecraftServer.class,ServerLevel.class,BlockPos.class,LabyrinthPlace.class);m.setAccessible(true);m.invoke(null,f.in.getServer(),f.in,f.origin,place);}catch(Exception e){throw new RuntimeException(e);}})
            .thenIdle(200)
            .thenExecute(()->{try{
                var l=f.level(place);var b=f.base(place);
                export(l,b,"a_farm",-20,20,-2,10,-42,2,-35);
                export(l,b,"b_barn_inside",4,16,-1,3,-35,-16,-30);export(l,b,"f_well",-4,4,-2,7,-27,-19,-35);
                export(l,b,"c_barn_front",3,17,-1,9,-26,-13,-20);
                export(l,b,"d_shed_inside",-16,-8,-1,3,-16,-8,-30);
            }catch(Exception e){throw new RuntimeException(e);}}).thenSucceed();
    }
}
