package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class InstituteCabinetBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(27,ItemStack.EMPTY);
    public InstituteCabinetBlockEntity(BlockPos p,BlockState s){super(NovelRegistry.WARD_CABINET_ENTITY.get(),p,s);}
    @Override public int getContainerSize(){return 27;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> next){items=next;}
    @Override protected Component getDefaultName(){return Component.literal("Ward cabinet");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inv){return new ChestMenu(MenuType.GENERIC_9x3,id,inv,this,3);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);items=NonNullList.withSize(27,ItemStack.EMPTY);if(!tryLoadLootTable(t))ContainerHelper.loadAllItems(t,items,r);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);if(!trySaveLootTable(t))ContainerHelper.saveAllItems(t,items,r);}
}
