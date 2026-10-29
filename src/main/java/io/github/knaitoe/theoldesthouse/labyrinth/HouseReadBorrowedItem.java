package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Temporary waiting-room objects. Original inventory is held separately until the reader leaves. */
public final class HouseReadBorrowedItem extends Item {
    public final String object;
    public HouseReadBorrowedItem(Properties p,String object){super(p);this.object=object;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){
        var item=p.getItemInHand(hand);
        // 0.4.160: an empty cone is only paper.
        if(object.equals("cone")&&empty(item)){if(!l.isClientSide)p.displayClientMessage(net.minecraft.network.chat.Component.literal("The cone is empty."),true);return InteractionResultHolder.pass(item);}
        if(object.equals("cone")){p.startUsingItem(hand);return InteractionResultHolder.consume(item);}
        if(p instanceof ServerPlayer reader)HouseReadMoments.handUse(reader,object);
        return InteractionResultHolder.sidedSuccess(item,l.isClientSide);
    }
    public static boolean empty(ItemStack item){var data=item.get(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA);return data!=null&&data.value()==1;}
    @Override public int getUseDuration(ItemStack item,LivingEntity e){return object.equals("cone")&&empty(item)?0:24;}
    @Override public UseAnim getUseAnimation(ItemStack item){return object.equals("cone")&&!empty(item)?UseAnim.DRINK:UseAnim.NONE;}
    @Override public ItemStack finishUsingItem(ItemStack item,Level l,LivingEntity e){
        if(e instanceof ServerPlayer p&&HouseReadSites.inside(p)==HouseReadSites.Room.WAITING)HouseReadSites.act(p,HouseReadSites.Room.WAITING,"drank the water from the paper cone");
        if(!l.isClientSide){item.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("An empty paper cone"));item.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(1));}
        return item;
    }
}
