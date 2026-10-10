package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Book custody is native and finite, including taking the original or removing the post. */
public final class NoticePostBlockEntity extends BlockEntity {
    private ItemStack book=ItemStack.EMPTY;
    public NoticePostBlockEntity(BlockPos p,BlockState s){super(DrownedTownRegistry.NOTICE_ENTITY.get(),p,s);}
    public ItemStack book(){return book;}
    public void book(ItemStack original){book=original;setChanged();}
    public ItemStack take(){var original=book;book=ItemStack.EMPTY;setChanged();return original;}
    public void open(Player p){if(!book.isEmpty())p.openMenu(new SimpleMenuProvider((id,inv,who)->new Pages(id,this,who),Component.literal("Note")));}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);if(!book.isEmpty())t.put("Book",book.save(r));}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);book=ItemStack.parseOptional(r,t.getCompound("Book"));}
    private static final class Pages extends LecternMenu {
        private final NoticePostBlockEntity post;private final Player reader;
        private static SimpleContainer display(NoticePostBlockEntity b){var c=new SimpleContainer(1);c.setItem(0,b.book().copy());return c;}
        Pages(int id,NoticePostBlockEntity b,Player p){super(id,display(b),new SimpleContainerData(1));post=b;reader=p;}
        @Override public boolean stillValid(Player p){return p==reader&&post.getLevel()==p.level()&&p.level().getBlockEntity(post.getBlockPos())==post&&!post.book().isEmpty()&&p.distanceToSqr(post.getBlockPos().getCenter())<64;}
        @Override public boolean clickMenuButton(Player p,int button){if(!stillValid(p))return false;if(button==3){var original=post.take();if(!p.getInventory().add(original))p.drop(original,false);p.closeContainer();return true;}return super.clickMenuButton(p,button);}
    }
}
