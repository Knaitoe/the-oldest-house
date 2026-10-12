package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

public final class GhostsCaseBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items=NonNullList.withSize(9,ItemStack.EMPTY);
    private final ContainerOpenersCounter openers=new ContainerOpenersCounter(){
        @Override protected void onOpen(Level l,BlockPos p,BlockState s){lid(l,p,s,true,SoundEvents.BARREL_OPEN);}
        @Override protected void onClose(Level l,BlockPos p,BlockState s){lid(l,p,s,false,SoundEvents.BARREL_CLOSE);}
        @Override protected void openerCountChanged(Level l,BlockPos p,BlockState s,int before,int after){}
        @Override protected boolean isOwnContainer(Player p){return p.containerMenu instanceof ChestMenu menu&&menu.getContainer()==GhostsCaseBlockEntity.this;}
    };
    public GhostsCaseBlockEntity(BlockPos at,BlockState s){super(LiteraryRegistry.GHOST_CASE_ENTITY.get(),at,s);}
    private static void lid(Level l,BlockPos p,BlockState s,boolean open,SoundEvent sound){l.setBlock(p,s.setValue(GhostsCaseBlock.OPEN,open),3);l.playSound(null,p,sound,SoundSource.BLOCKS,.4F,1);}
    @Override public int getContainerSize(){return 9;}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> stacks){items=stacks;}
    @Override protected Component getDefaultName(){return Component.literal("Camera case");}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new ChestMenu(MenuType.GENERIC_9x1,id,inventory,this,1);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registry){super.saveAdditional(tag,registry);ContainerHelper.saveAllItems(tag,items,registry);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registry){super.loadAdditional(tag,registry);items=NonNullList.withSize(9,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items,registry);}
    @Override public void startOpen(Player p){if(!isRemoved()&&!p.isSpectator())openers.incrementOpeners(p,level,worldPosition,getBlockState());}
    @Override public void stopOpen(Player p){if(!isRemoved()&&!p.isSpectator())openers.decrementOpeners(p,level,worldPosition,getBlockState());}
    public void recheck(){if(!isRemoved()&&level!=null)openers.recheckOpeners(level,worldPosition,getBlockState());}
}
