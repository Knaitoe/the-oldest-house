package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import java.util.List;
import net.minecraft.world.level.Level;

/** A wide documentary lens. Holding use records only what its owner's actual camera can see. */
public final class CamcorderItem extends Item {
    public CamcorderItem(Properties p) { super(p); }
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> text,TooltipFlag f){text.add(Component.literal("Hold use to film the set. Put it away to talk."));}
    @Override public UseAnim getUseAnimation(ItemStack s) { return UseAnim.SPYGLASS; }
    @Override public int getUseDuration(ItemStack s, LivingEntity p) { return 72000; }
    @Override public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand hand) {
        p.startUsingItem(hand);
        return InteractionResultHolder.consume(p.getItemInHand(hand));
    }
}
