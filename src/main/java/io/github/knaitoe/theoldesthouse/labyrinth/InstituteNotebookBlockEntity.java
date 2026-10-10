package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The actual original from a replaced lectern, including its components. */
public final class InstituteNotebookBlockEntity extends BlockEntity {
    private ItemStack book=ItemStack.EMPTY;
    public InstituteNotebookBlockEntity(BlockPos p,BlockState s){super(NovelRegistry.WARD_NOTEBOOK_ENTITY.get(),p,s);}
    public ItemStack book(){return book.copy();}
    public void book(ItemStack stack){book=stack.copy();setChanged();}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);book=ItemStack.parseOptional(r,t.getCompound("Book"));}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);if(!book.isEmpty())t.put("Book",book.save(r));}
}
