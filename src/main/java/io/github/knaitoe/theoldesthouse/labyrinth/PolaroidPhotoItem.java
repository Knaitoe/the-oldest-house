package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** 0.4.204, the Polaroid Trail: a developed photograph. Held up and used, it is looked at: the picture, and its caption. */
public final class PolaroidPhotoItem extends Item {
    public PolaroidPhotoItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var s=player.getItemInHand(hand);if(player instanceof ServerPlayer p)PolaroidTrail.view(p,s);return InteractionResultHolder.sidedSuccess(s,level.isClientSide);
    }
}
