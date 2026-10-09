package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.CabinStormClient;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The outside pockets stand in a biome without weather; a reader's own storm rains there, on the render thread only. */
@Mixin(Biome.class)
public abstract class CabinStormBiomeMixin {
    @Inject(method="hasPrecipitation",at=@At("RETURN"),cancellable=true)
    private void house$cabinStorm(CallbackInfoReturnable<Boolean> cir){if(!cir.getReturnValueZ()&&CabinStormClient.precipitating())cir.setReturnValue(true);}
}
