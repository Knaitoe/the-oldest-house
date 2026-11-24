package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.StoryPieces;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): a stonecutter shows the stories' pieces only to a reader who knows them. Runs on both
 * sides, each against the same reader's recipe book, so the list the screen shows and the list the server cuts from agree.
 */
@Mixin(StonecutterMenu.class)
public abstract class StonecutterPiecesMixin {
    @Shadow private List<RecipeHolder<StonecutterRecipe>> recipes;
    @Unique private Player theOldestHouse$who;
    @Inject(method="<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",at=@At("RETURN"))
    private void theOldestHouse$who(int id,Inventory inventory,ContainerLevelAccess access,CallbackInfo ci){theOldestHouse$who=inventory.player;}
    @Inject(method="setupRecipeList",at=@At("TAIL"))
    private void theOldestHouse$known(Container container,ItemStack stack,CallbackInfo ci){
        if(theOldestHouse$who!=null)recipes=StoryPieces.known(theOldestHouse$who,recipes);
    }
}
