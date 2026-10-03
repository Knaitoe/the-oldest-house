package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
/** A table stores at most three 8x4x8 native samples; fan stores its scar only. */
public final class LiteraryModelBlockEntity extends BlockEntity {
    private CompoundTag display=new CompoundTag();
    public LiteraryModelBlockEntity(BlockPos pos,BlockState state){super(LiteraryRegistry.MODEL.get(),pos,state);}
    public CompoundTag display(){return display.copy();}
    public void display(CompoundTag tag){display=tag.copy();setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.put("Display",display);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);display=t.getCompound("Display").copy();}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
