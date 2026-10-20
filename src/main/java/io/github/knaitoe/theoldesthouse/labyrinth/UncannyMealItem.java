package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
/** The plate is an actual, finite served stack, not a second reward conjured by interaction. */
public final class UncannyMealItem extends Item {
    public UncannyMealItem(Properties p){super(p);}
    @Override public ItemStack finishUsingItem(ItemStack s,Level l,LivingEntity e){
        if(e instanceof ServerPlayer p)UncannyStories.ate(p,s);
        return super.finishUsingItem(s,l,e);
    }
}
