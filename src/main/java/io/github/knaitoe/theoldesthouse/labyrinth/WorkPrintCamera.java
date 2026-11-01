package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * 0.4.203, Work print: the House's camera, lifted off its tripod on the set. Held up to the eye like a spyglass, it films what
 * the reader holding it is looking at; on the set, that is how one reader shoots the last scene with another in the costume.
 */
public final class WorkPrintCamera extends Item {
    public WorkPrintCamera(Properties p){super(p);}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.SPYGLASS;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 1200;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){player.startUsingItem(hand);return InteractionResultHolder.consume(player.getItemInHand(hand));}
    @Override public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remaining){if(entity instanceof ServerPlayer p)WorkPrint.filming(p,getUseDuration(stack,entity)-remaining);}
}
