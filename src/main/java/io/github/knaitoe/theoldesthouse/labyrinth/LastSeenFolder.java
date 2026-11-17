package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 0.4.668, Last Seen, part 2 (docs/LAST_SEEN_0_4_668.md): one of the ten children's folders, taken down from its bay or off the day's
 * pile. There is one of each in the place, and it goes back on its shelf when its reader leaves. Used, it is held open: the child's
 * photograph, clipped to the form, and where and on what day they were last seen. Its custom data says whose it is (Child), which park
 * it belongs to (Site), which morning (Day) and which time out (Issue); a folder from another morning or an earlier time out is nobody's.
 */
public final class LastSeenFolder extends Item {
    public LastSeenFolder(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer p)LastSeen.read(p,stack);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide());
    }
    @Override public Component getName(ItemStack stack){int child=LastSeen.child(stack);return child<0?super.getName(stack):Component.literal(LastSeenTexts.folderName(child));}
}
