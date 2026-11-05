package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** 0.4.310: the warning triangle; used on the road it stands where it is set (DeerRoad.setTriangle decides whether it is the place). */
public final class DeerRoadTriangleItem extends Item {
    public DeerRoadTriangleItem(Properties p){super(p);}
    @Override public InteractionResult useOn(UseOnContext c){
        if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(c.getPlayer() instanceof ServerPlayer p&&DeerRoad.setTriangle(p,c.getClickedPos(),c.getItemInHand()))return InteractionResult.CONSUME;
        return InteractionResult.PASS;
    }
}
