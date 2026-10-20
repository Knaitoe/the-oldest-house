package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.UncannyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Use vanilla's outline pass locally; never change an actor's synchronized glow for other players. */
@Mixin(Minecraft.class)
public abstract class UncannyGlowMixin {
    @Inject(method="shouldEntityAppearGlowing",at=@At("HEAD"),cancellable=true)
    private void uncannyRing(Entity entity,CallbackInfoReturnable<Boolean> result){if(UncannyClient.outline(entity))result.setReturnValue(true);}
}
