package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** The same original compass or patch in the hand, on the trail and on the sorting table. */
public final class BorrascaKeepsakeItem extends Item {
    private final String key;
    public BorrascaKeepsakeItem(Properties p,String key){super(p);this.key=key;}
    @Override public InteractionResultHolder<ItemStack> use(Level l,Player player,InteractionHand hand){
        var s=player.getItemInHand(hand);
        if(l.isClientSide())return InteractionResultHolder.success(s);
        if(player instanceof ServerPlayer p)BorrascaStory.held(p,s,key);
        return InteractionResultHolder.success(s);
    }
}
