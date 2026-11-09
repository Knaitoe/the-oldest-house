package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.KeepsakeKeyItem;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/**
 * 0.4.413: the service key works for its own reader only, like every keepsake. A crafting grid holding someone else's key (or one
 * nobody was given) shows no result, so nothing can be made with it. Runs on the server, where the result is decided. Since
 * 0.4.417 the same holds for every keepsake that unlocks a recipe ({@link KeepsakeKeyItem}): the service key, the house chalk and the
 * tin of darts.
 */
@Mixin(CraftingMenu.class)
public abstract class CraftingKeyMixin {
    // At each load of the result (into the result slot, the remote slot and the packet), so all three agree.
    @ModifyVariable(method="slotChangedCraftingGrid",at=@At("LOAD"),ordinal=0)
    private static ItemStack theOldestHouse$ownKey(ItemStack result,AbstractContainerMenu menu,Level level,Player player,CraftingContainer grid,ResultContainer results,@Nullable RecipeHolder<CraftingRecipe> recipe){
        return KeepsakeKeyItem.allows(grid,player)?result:ItemStack.EMPTY;
    }
}
