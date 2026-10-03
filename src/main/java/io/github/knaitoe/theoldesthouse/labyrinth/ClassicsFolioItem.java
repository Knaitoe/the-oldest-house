package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

/** A curled, bound native item whose immutable original pages use the ordinary book menu. */
public final class ClassicsFolioItem extends Item {
    public ClassicsFolioItem(Properties properties){super(properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        ItemStack stack=player.getItemInHand(hand);
        if(player instanceof ServerPlayer p)ClassicsVignettes.readCollected(p,stack);
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }
}
