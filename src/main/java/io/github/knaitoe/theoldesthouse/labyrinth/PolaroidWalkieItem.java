package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/**
 * 0.4.204, the Polaroid Trail: Josh's walkie-talkie, kept by the reader who looked up from the trench. Pressed, it gives
 * static, his voice, and his flashlight beam toward the way they came into wherever they are. Only for its owner.
 */
public final class PolaroidWalkieItem extends Item {
    public PolaroidWalkieItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var s=player.getItemInHand(hand);if(player instanceof ServerPlayer p)PolaroidTrail.walkie(p,s);return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
    }
}
