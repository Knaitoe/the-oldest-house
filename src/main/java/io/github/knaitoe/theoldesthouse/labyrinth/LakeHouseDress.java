package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** A handful of smooth, owner-only block displays. Peers never receive the reader's dresses or lifted objects. */
public final class LakeHouseDress extends Display.BlockDisplay {
    @Nullable private UUID reader;
    private net.minecraft.world.level.block.state.BlockState paintedState=net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    public LakeHouseDress(EntityType<? extends Display.BlockDisplay> type,Level level){super(type,level);}
    public void reader(UUID id){reader=id;}
    public void shape(net.minecraft.world.level.block.state.BlockState state,float lift,float turn){
        paintedState=state;
        var t=new CompoundTag();addAdditionalSaveData(t);t.put("block_state",net.minecraft.nbt.NbtUtils.writeBlockState(state));t.putInt("interpolation_duration",8);t.putInt("start_interpolation",0);
        var transform=new CompoundTag();transform.put("translation",floats(0,lift,0));transform.put("scale",floats(1,1,1));transform.put("left_rotation",floats(0,0,(float)Math.sin(turn/2),(float)Math.cos(turn/2)));transform.put("right_rotation",floats(0,0,0,1));t.put("transformation",transform);readAdditionalSaveData(t);
    }
    public net.minecraft.world.level.block.state.BlockState paintedState(){return paintedState;}
    public void small(net.minecraft.world.level.block.state.BlockState state,float x,float y,float z,float scale){
        var t=new CompoundTag();addAdditionalSaveData(t);t.put("block_state",net.minecraft.nbt.NbtUtils.writeBlockState(state));t.putInt("interpolation_duration",4);t.putInt("start_interpolation",0);var transform=new CompoundTag();transform.put("translation",floats(x,y,z));transform.put("scale",floats(scale,scale,scale));transform.put("left_rotation",floats(0,0,0,1));transform.put("right_rotation",floats(0,0,0,1));t.put("transformation",transform);readAdditionalSaveData(t);
    }
    private static net.minecraft.nbt.ListTag floats(float... values){var list=new net.minecraft.nbt.ListTag();for(float v:values)list.add(net.minecraft.nbt.FloatTag.valueOf(v));return list;}
    /** Only the reader sees their displays; 0.4.156: the dresses are also seen by the reader who goes down in Tess's place with them. */
    @Override public boolean broadcastToPlayer(ServerPlayer p){return reader!=null&&(reader.equals(p.getUUID())||dress()&&LakeHousePairs.sharesWith(p,reader))&&!p.isSpectator()&&super.broadcastToPlayer(p);}
    private boolean dress(){return paintedState.is(LakeHouseRegistry.FITTING.get())&&paintedState.getValue(LakeHouseFittingBlock.KIND)==LakeHouseFittingBlock.Kind.DRESS;}
    @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);if(reader!=null)t.putUUID("LakeReader",reader);}
    @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);reader=t.hasUUID("LakeReader")?t.getUUID("LakeReader"):null;paintedState=net.minecraft.nbt.NbtUtils.readBlockState(level().holderLookup(net.minecraft.core.registries.Registries.BLOCK),t.getCompound("block_state"));}
}
