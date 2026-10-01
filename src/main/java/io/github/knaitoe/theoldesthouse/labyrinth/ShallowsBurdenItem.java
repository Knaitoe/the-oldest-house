package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Temporary, owner-bound grip: briefly hold use and release to perform the throw. */
public final class ShallowsBurdenItem extends Item {
    public ShallowsBurdenItem(Properties properties){super(properties.stacksTo(1));}
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.BOW;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        ItemStack stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer serverPlayer&&!Shallows.validBurden(serverPlayer,stack))return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);return InteractionResultHolder.consume(stack);
    }
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int timeLeft){
        if(entity instanceof ServerPlayer player&&getUseDuration(stack,entity)-timeLeft>=12)Shallows.throwGirl(player,stack);
    }
}
