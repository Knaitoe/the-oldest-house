package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * 0.4.206, the Dead Air Broadcast: the dead-air breaker, kept by the reader who stood in the master booth. Thrown, the air goes
 * dead for its owner alone: for eight seconds nothing in the House that hunts by sight can find them, and then it needs two
 * minutes before it will throw again.
 */
public final class DeadAirBreakerItem extends Item {
    public DeadAirBreakerItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var s=player.getItemInHand(hand);if(player instanceof ServerPlayer p)DeadAirGame.quiet(p,s);return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
    }
}
