package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 0.4.538, the bucket from the Pell house's laundry, water in it and the scrubbing brush across it (PellScrub,
 * docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md). Held to a cell of the line and kept there, the brush works at the paint: the game's own
 * brushing (the arm going back and forth, as with the archaeologist's brush), and on the server PellScrub.scrubbing, tick by tick.
 * On anything but the line it does nothing.
 */
public final class PellScrubItem extends Item {
    public PellScrubItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        var p=c.getPlayer();
        if(p==null||!PellLine.is(c.getLevel().getBlockState(c.getClickedPos())))return InteractionResult.PASS;
        p.startUsingItem(c.getHand());
        return InteractionResult.CONSUME;
    }
    @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BRUSH;}
    @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
    @Override public void onUseTick(Level l,LivingEntity e,ItemStack s,int left){
        if(l.isClientSide||!(e instanceof ServerPlayer p))return;
        if(!PellScrub.scrubbing(p))p.releaseUsingItem();
    }
}
