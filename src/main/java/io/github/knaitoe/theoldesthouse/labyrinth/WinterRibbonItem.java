package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Her hair ribbon (0.4.104). Held out over the hole where she went down, it is taken back; anywhere else it is only kept. */
public final class WinterRibbonItem extends Item {
    public WinterRibbonItem(Properties properties){super(properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);
        if(level.isClientSide())return InteractionResultHolder.pass(stack);
        return player instanceof ServerPlayer p&&WinterLake.giveBack(p,stack)?InteractionResultHolder.success(stack):InteractionResultHolder.pass(stack);
    }
}
