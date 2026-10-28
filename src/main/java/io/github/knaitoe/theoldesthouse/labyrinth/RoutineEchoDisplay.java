package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** One saved, uncollectible routine object. Only its original reader receives its native entity packets. */
public final class RoutineEchoDisplay extends Display.ItemDisplay {
    @Nullable private UUID reader;
    @Nullable private BlockPos support;private boolean hotel;
    public RoutineEchoDisplay(EntityType<? extends Display.ItemDisplay> type,Level level){super(type,level);}
    public void reader(UUID id){reader=id;}
    public boolean ownedBy(UUID id){return id.equals(reader);}
    public void support(BlockPos at,boolean hotel){support=at.immutable();this.hotel=hotel;}
    public void present(ItemStack original,float size){
        var data=saveWithoutId(new CompoundTag());data.put("item",original.save(level().registryAccess()));data.putString("item_display","none");
        var transform=new CompoundTag();transform.put("translation",floats(0,size/2,0));transform.put("scale",floats(size,size,size));transform.put("left_rotation",floats(0,0,0,1));transform.put("right_rotation",floats(0,0,0,1));data.put("transformation",transform);data.putFloat("width",1);data.putFloat("height",1);load(data);
    }
    private static ListTag floats(float... values){var out=new ListTag();for(float value:values)out.add(FloatTag.valueOf(value));return out;}
    @Override public boolean broadcastToPlayer(ServerPlayer player){return reader!=null&&reader.equals(player.getUUID())&&!player.isSpectator()&&super.broadcastToPlayer(player);}
    @Override public void addAdditionalSaveData(CompoundTag data){super.addAdditionalSaveData(data);if(reader!=null)data.putUUID("EchoReader142",reader);if(support!=null){data.putLong("EchoSupport142",support.asLong());data.putBoolean("EchoHotel142",hotel);}}
    @Override public void readAdditionalSaveData(CompoundTag data){super.readAdditionalSaveData(data);reader=data.hasUUID("EchoReader142")?data.getUUID("EchoReader142"):null;support=data.contains("EchoSupport142")?BlockPos.of(data.getLong("EchoSupport142")):null;hotel=data.getBoolean("EchoHotel142");}
    @Override public void tick(){
        super.tick();if(!(level() instanceof ServerLevel l)||support==null||l.getChunkSource().getChunkNow(support.getX()>>4,support.getZ()>>4)==null)return;
        var state=l.getBlockState(support);boolean standing=hotel?state.is(HotelRegistry.STANLEY.get())&&state.getValue(StanleyBlock.KIND)==StanleyBlock.Kind.SINK:state.is(LakeHouseRegistry.FITTING.get())&&state.getValue(LakeHouseFittingBlock.KIND)==LakeHouseFittingBlock.Kind.TABLE;
        if(!standing)discard();
    }
}
