package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** One row of drawer space, saved with the world. */
public final class SchoolDeskBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(9,ItemStack.EMPTY);
    public SchoolDeskBlockEntity(BlockPos pos,BlockState state){super(DrownedTownRegistry.SCHOOL_DESK_ENTITY.get(),pos,state);}
    @Override public int getContainerSize(){return 9;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> next){items=next;}
    @Override protected Component getDefaultName(){return Component.literal("Desk drawer");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new ChestMenu(MenuType.GENERIC_9x1,id,inventory,this,1);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);items=NonNullList.withSize(getContainerSize(),ItemStack.EMPTY);
        if(!tryLoadLootTable(tag))ContainerHelper.loadAllItems(tag,items,registries);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);if(!trySaveLootTable(tag))ContainerHelper.saveAllItems(tag,items,registries);
    }
}
