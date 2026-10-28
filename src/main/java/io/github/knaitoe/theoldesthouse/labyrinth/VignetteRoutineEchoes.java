package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/** Two private objects on existing supported surfaces. Selection and native UUID are retained across visits/reconnects. */
public final class VignetteRoutineEchoes {
    public static final String KEY="RoutineEcho142";
    public static final BlockPos HOTEL_SINK=new BlockPos(-23,6,-26),LAKE_VANITY=new BlockPos(-8,-19,-163);
    private VignetteRoutineEchoes(){}
    public static void hotel(ServerPlayer reader,BlockPos base,CompoundTag own){
        ensure(reader,base,own,HOTEL_SINK,new Vec3(-22.4,6+14/16D,-25.58),true);
    }
    public static void lake(ServerPlayer reader,BlockPos base,CompoundTag own){
        ensure(reader,base,own,LAKE_VANITY,new Vec3(-7.7,-19+13/16D,-162.875),false);
    }
    private static void ensure(ServerPlayer reader,BlockPos base,CompoundTag own,BlockPos surface,Vec3 where,boolean hotel){
        var level=reader.serverLevel();var at=base.offset(surface);var chunk=level.getChunkSource().getChunkNow(at.getX()>>4,at.getZ()>>4);
        if(chunk==null||!level.areEntitiesLoaded(ChunkPos.asLong(at)))return;
        var chosen=own.getCompound(KEY);
        if(!chosen.getBoolean("Picked")){chosen.putBoolean("Picked",true);chosen.putString("Hair",HouseRead.hair(reader,false).orElse(""));own.put(KEY,chosen);}
        int routine=VignetteTailoring.routine(chosen.getString("Hair"));if(routine<0||chosen.hasUUID("Id"))return;
        var state=chunk.getBlockState(at);
        boolean supported=hotel?state.is(HotelRegistry.STANLEY.get())&&state.getValue(StanleyBlock.KIND)==StanleyBlock.Kind.SINK
                :state.is(LakeHouseRegistry.FITTING.get())&&state.getValue(LakeHouseFittingBlock.KIND)==LakeHouseFittingBlock.Kind.TABLE;
        if(!supported)return;
        // No shared blocks or inventories change. A removed original is not silently replaced.
        var echo=HouseReadRegistry.ROUTINE_ECHO.get().create(level);if(echo==null)return;
        echo.reader(reader.getUUID());echo.support(at,hotel);echo.setPos(base.getX()+where.x,base.getY()+where.y,base.getZ()+where.z);echo.present(new ItemStack(HouseReadRegistry.ROUTINES.get(routine).get()),.36f);
        if(level.addFreshEntity(echo)){chosen.putUUID("Id",echo.getUUID());own.put(KEY,chosen);}
    }
}
