package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The exact shared original lives in the chunk; taking it leaves an empty mount permanently. */
public final class DomesticRecordBlockEntity extends BlockEntity {
    private ItemStack book=ItemStack.EMPTY;
    private boolean initialized;
    public DomesticRecordBlockEntity(BlockPos p,BlockState s){super(DomesticRegistry.RECORD_ENTITY.get(),p,s);}
    public ItemStack book(){return book.copy();}
    public void seed(ItemStack original){if(initialized)return;initialized=true;book=original;setChanged();}
    /** The owner requested a prose edit. Only an exact untouched stock original can be revised. */
    public boolean revise(ItemStack before,ItemStack after){if(!initialized||book.isEmpty()||!ItemStack.matches(book,before)||ItemStack.matches(book,after))return false;book=after.copy();setChanged();return true;}
    public void open(ServerPlayer p){if(book.isEmpty())return;p.openMenu(new SimpleMenuProvider((id,inv,reader)->new PaperMenu(id,p,this),book.getHoverName()));}
    private ItemStack take(){var original=book;book=ItemStack.EMPTY;setChanged();if(level!=null)level.setBlock(worldPosition,getBlockState().setValue(DomesticRecordBlock.PRESENT,false),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);return original;}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);initialized=t.getBoolean("Initialized");book=ItemStack.parseOptional(r,t.getCompound("Book"));}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putBoolean("Initialized",initialized);if(!book.isEmpty())t.put("Book",book.save(r));}
    public static final class PaperMenu extends LecternMenu {
        private final ServerPlayer reader;private final DomesticRecordBlockEntity source;private final SimpleContainer pages;
        PaperMenu(int id,ServerPlayer p,DomesticRecordBlockEntity be){this(id,p,be,new SimpleContainer(1));}
        private PaperMenu(int id,ServerPlayer p,DomesticRecordBlockEntity be,SimpleContainer pages){super(id,pages,new SimpleContainerData(1));reader=p;source=be;this.pages=pages;pages.setItem(0,be.book());}
        @Override public boolean stillValid(Player p){return p==reader&&p.isAlive()&&!p.isSpectator()&&p.level()==source.level&&p.distanceToSqr(source.worldPosition.getCenter())<=64&&source.level.getBlockEntity(source.worldPosition)==source&&!source.book.isEmpty();}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p))return false;
            if(button!=3)return super.clickMenuButton(p,button);
            var original=source.take();if(original.isEmpty())return false;pages.setItem(0,ItemStack.EMPTY);
            if(!reader.getInventory().add(original))reader.drop(original,false);reader.closeContainer();return true;
        }
        @Override public void clicked(int slot,int button,ClickType type,Player p){} // Display snapshots cannot leave by inventory packets.
        @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
    }
}
