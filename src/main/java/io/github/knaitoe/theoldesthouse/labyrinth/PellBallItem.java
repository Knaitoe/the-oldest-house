package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 0.4.601, the tennis ball from beside the Pell house's kennel, in a reader's hand (PellCatch, docs/PELL_HOUSE_NOTES_0_4_601.md).
 * Used, it is thrown the way the reader looks: a lob for a look up, a flat throw for a level one. What catches it, and what it sounds
 * like where it lands, is the house's.
 */
public final class PellBallItem extends Item {
    public PellBallItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);
        if(p instanceof ServerPlayer s)PellCatch.thrown(s,stack.copyWithCount(1));
        stack.consume(1,p);p.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
}
