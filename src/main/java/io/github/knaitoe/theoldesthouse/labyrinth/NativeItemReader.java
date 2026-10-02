package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** A private vanilla lectern view for authored book items; the held original never changes custody. */
public final class NativeItemReader {
    private NativeItemReader(){}
    public static void open(ServerPlayer reader,ItemStack original,InteractionHand hand){
        var content=original.get(DataComponents.WRITTEN_BOOK_CONTENT);if(content==null)return;
        reader.openMenu(new SimpleMenuProvider((id,inventory,p)->new Reader(id,reader,original,hand),Component.literal(content.title().raw())));
    }
    private static final class Reader extends LecternMenu {
        private final ServerPlayer reader;private final ItemStack original;private final InteractionHand hand;private final int pages;
        Reader(int id,ServerPlayer reader,ItemStack original,InteractionHand hand){this(id,reader,original,hand,new SimpleContainer(1));}
        Reader(int id,ServerPlayer reader,ItemStack original,InteractionHand hand,SimpleContainer display){
            super(id,display,new SimpleContainerData(1));this.reader=reader;this.original=original;this.hand=hand;
            var content=original.get(DataComponents.WRITTEN_BOOK_CONTENT);pages=content.pages().size();
            var book=new ItemStack(Items.WRITTEN_BOOK);book.set(DataComponents.WRITTEN_BOOK_CONTENT,content);display.setItem(0,book);
        }
        @Override public boolean stillValid(Player p){return p==reader&&reader.isAlive()&&reader.getItemInHand(hand)==original;}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p)||button==3)return false;
            if(button>=100){if(button-100>=pages)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=pages-1)return false;}else return false;
            return super.clickMenuButton(p,button);
        }
    }
}
