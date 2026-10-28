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
        if(object.equals("cone")){p.startUsingItem(hand);return InteractionResultHolder.consume(item);}
        if(p instanceof ServerPlayer reader)HouseReadMoments.handUse(reader,object);
        return InteractionResultHolder.sidedSuccess(item,l.isClientSide);
    }
    @Override public int getUseDuration(ItemStack item,LivingEntity e){return 24;}
    @Override public UseAnim getUseAnimation(ItemStack item){return object.equals("cone")?UseAnim.DRINK:UseAnim.NONE;}
    @Override public ItemStack finishUsingItem(ItemStack item,Level l,LivingEntity e){
        if(e instanceof ServerPlayer p&&HouseReadSites.inside(p)==HouseReadSites.Room.WAITING){HouseReadSites.act(p,HouseReadSites.Room.WAITING,"drank the water from the paper cone");item.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("An empty paper cone"));}
        return item;
    }
}
