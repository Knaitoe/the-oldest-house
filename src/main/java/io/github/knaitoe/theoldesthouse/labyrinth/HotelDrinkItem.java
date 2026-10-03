package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
public final class HotelDrinkItem extends Item {
    public HotelDrinkItem(Properties p){super(p);}
    @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.DRINK;}
    @Override public int getUseDuration(ItemStack s,LivingEntity e){return 32;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
    @Override public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){if(e instanceof ServerPlayer p&&HotelVignette.drink(p)){s.shrink(1);}return s;}
}
