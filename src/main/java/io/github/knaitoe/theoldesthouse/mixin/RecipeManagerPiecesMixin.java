package io.github.knaitoe.theoldesthouse.mixin;

import com.google.gson.JsonElement;
import io.github.knaitoe.theoldesthouse.labyrinth.StoryPieces;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 0.4.758 (docs/BUILDING_OUTSIDE_0_4_758.md): the stories' stonecutter recipes, one per piece, written as the recipes load. */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerPiecesMixin {
    @Inject(method="apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",at=@At("HEAD"))
    private void theOldestHouse$pieces(Map<ResourceLocation,JsonElement> recipes,ResourceManager resources,ProfilerFiller profiler,CallbackInfo ci){
        StoryPieces.addRecipes(recipes);
    }
}
