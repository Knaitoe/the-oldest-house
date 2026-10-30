package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.Artifacts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bud's note (Artifacts): in their own world, powder snow bears its reader. */
@Mixin(PowderSnowBlock.class)
public abstract class ArtifactSnowMixin {
    @Inject(method="canEntityWalkOnPowderSnow",at=@At("HEAD"),cancellable=true)
    private static void bears(Entity entity,CallbackInfoReturnable<Boolean> result){if(Artifacts.snowBears(entity))result.setReturnValue(true);}
}
