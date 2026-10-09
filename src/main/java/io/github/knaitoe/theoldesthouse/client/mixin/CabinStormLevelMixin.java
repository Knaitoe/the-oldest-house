package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.CabinStormClient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client-only getters: one reader's storm at the cabin. The server's weather and level data are untouched. */
@Mixin(Level.class)
public abstract class CabinStormLevelMixin {
    @Inject(method="getRainLevel",at=@At("RETURN"),cancellable=true)
    private void house$cabinRain(float partial,CallbackInfoReturnable<Float> cir){
        Level level=(Level)(Object)this;if(level.isClientSide())cir.setReturnValue(CabinStormClient.rain(level,cir.getReturnValue()));
    }
    @Inject(method="getThunderLevel",at=@At("RETURN"),cancellable=true)
    private void house$cabinThunder(float partial,CallbackInfoReturnable<Float> cir){
        Level level=(Level)(Object)this;if(level.isClientSide())cir.setReturnValue(CabinStormClient.thunder(level,cir.getReturnValue()));
    }
}
