package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class UncannyMenuItem extends Item {
    public UncannyMenuItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(p instanceof ServerPlayer who&&!UncannyBreakfast.open(who,s))return InteractionResultHolder.fail(s);return InteractionResultHolder.sidedSuccess(s,l.isClientSide);}
}
