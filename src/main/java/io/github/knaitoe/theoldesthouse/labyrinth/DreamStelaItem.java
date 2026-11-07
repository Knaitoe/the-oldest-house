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
 * 0.4.343, the piece of the Dream Stela in the hand (docs/STELA_PASSAGE_0_4_343.md): pressed against loose fill, it asks
 * {@link StelaPassage} to part it. A dismissive stone has to be held there; the hold is the item's use, ticking until it opens or the
 * reader lets go.
 */
public final class DreamStelaItem extends Item {
    public DreamStelaItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        if(c.getLevel().isClientSide)return StelaPassage.fill(c.getLevel().getBlockState(c.getClickedPos()))?InteractionResult.SUCCESS:InteractionResult.PASS;
        return c.getPlayer() instanceof ServerPlayer p?StelaPassage.press(p,c.getItemInHand(),c.getClickedPos(),c.getHand()):InteractionResult.PASS;
    }
    @Override public int getUseDuration(ItemStack s,LivingEntity e){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BLOCK;}
    @Override public void onUseTick(Level l,LivingEntity e,ItemStack s,int remaining){if(e instanceof ServerPlayer p)StelaPassage.holding(p,getUseDuration(s,e)-remaining);}
    @Override public void releaseUsing(ItemStack s,Level l,LivingEntity e,int remaining){if(e instanceof ServerPlayer p)StelaPassage.released(p);}
}
