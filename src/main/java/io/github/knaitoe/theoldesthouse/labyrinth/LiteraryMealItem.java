package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
/** Eating is a native use/finish action, never a dialogue checkbox. */
public final class LiteraryMealItem extends Item {
    public LiteraryMealItem(Properties p){super(p);}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level l,LivingEntity user){if(user instanceof ServerPlayer p)LiteraryVignettes.ate(p,stack);return super.finishUsingItem(stack,l,user);}
}
