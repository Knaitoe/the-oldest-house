package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 0.4.524, Tilly's long cane, in the house's Tilly's hand (PellPeople); 0.4.546 (docs/PELL_HOUSE_SENSES_0_4_546.md), in the hand of a
 * reader playing her (PellSenses). Used, it taps the ground ahead: everybody near hears it, and for a moment the reader sees what is in
 * reach outlined (PellSenses.tap). It is theirs only while they are Tilly at the house: it cannot be dropped, comes back if it is let go
 * of any other way, and goes back on its hook when they go.
 */
public final class PellCaneItem extends Item {
    public PellCaneItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player p,InteractionHand hand){
        var stack=p.getItemInHand(hand);
        if(!level.isClientSide&&p instanceof ServerPlayer reader){PellSenses.tap(reader,stack);p.getCooldowns().addCooldown(this,PellSenses.TAP_EVERY);}
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }
    /** On the floor or anything else, the tap is the same: the ground ahead. */
    @Override public InteractionResult useOn(UseOnContext c){
        var p=c.getPlayer();if(p==null)return InteractionResult.PASS;
        if(!c.getLevel().isClientSide&&p instanceof ServerPlayer reader){PellSenses.tap(reader,c.getItemInHand());p.getCooldowns().addCooldown(this,PellSenses.TAP_EVERY);}
        return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
    }
    /** Anywhere but in the hands of the reader who is Tilly at the house, it is gone (back on its hook). */
    @Override public void inventoryTick(ItemStack stack,Level level,Entity holder,int slot,boolean selected){
        if(!level.isClientSide&&holder instanceof ServerPlayer p&&!PellSenses.keeps(p,stack))stack.setCount(0);
    }
    /** It cannot be dropped. */
    @Override public boolean onDroppedByPlayer(ItemStack stack,Player p){return !PellSenses.marked(stack);}
    /** Let go of any other way, it is not left lying about: it comes back to their hand (PellSenses.cane). */
    @Override public boolean onEntityItemUpdate(ItemStack stack,ItemEntity entity){
        if(entity.level().isClientSide||!PellSenses.marked(stack))return false;
        entity.discard();return true;
    }
}
