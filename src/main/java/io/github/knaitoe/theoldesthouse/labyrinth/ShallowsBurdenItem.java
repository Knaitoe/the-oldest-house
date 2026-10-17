package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * Temporary, owner-bound grip on her wrists. Since 0.4.101 the swing is on the reader's strafe keys and the release comes on
 * the count of three, so the grip itself does nothing when used; dropping it lets go of her.
 */
public final class ShallowsBurdenItem extends Item {
    public ShallowsBurdenItem(Properties properties){super(properties.stacksTo(1));}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){return InteractionResultHolder.pass(player.getItemInHand(hand));}
}
