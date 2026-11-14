package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * 0.4.571, the dog's bowl from beside the Pell house's kennel, in a reader's hand (PellJobs, docs/PELL_HOUSE_NOTES_0_4_571.md): empty,
 * or filled at a tap. By the kennel a hand on the ground sets it down where it stood, and that is the house's own answer to the hand
 * (PellJobs.ground), not this item's: a block put down from an item's use is taken away again by the House's rule against building.
 * Used on anything the house did not answer, it says where it goes.
 */
public final class PellBowlItem extends Item {
    public PellBowlItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        if(c.getLevel().isClientSide)return InteractionResult.PASS;
        return c.getPlayer() instanceof ServerPlayer p&&PellJobs.useBowl(p,c.getItemInHand())?InteractionResult.SUCCESS:InteractionResult.PASS;
    }
}
