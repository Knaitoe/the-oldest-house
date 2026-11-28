package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.KeepsakeKeyItem;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0.4.767: a keepsake key works for its own reader only ({@link CraftingKeyMixin}), and that was asked only at a crafting table
 * and the inventory's own grid. The Crafter never goes that way: with somebody else's key in it, or one nobody was given, a
 * pulse made the cabinet, the pool table or the dartboard and handed the key back, as often as wanted. A Crafter has no
 * reader whose key it could be, so it makes nothing with one in it. This is the Crafter's one lookup, so its screen shows no
 * result either.
 */
@Mixin(CrafterBlock.class)
public abstract class CrafterKeyMixin {
    @Inject(method="getPotentialResults",at=@At("HEAD"),cancellable=true)
    private static void theOldestHouse$noKeys(Level level,CraftingInput input,CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> cir){
        for(ItemStack stack:input.items())if(stack.getItem() instanceof KeepsakeKeyItem){cir.setReturnValue(Optional.empty());return;}
    }
}
