package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.client.NovelSceneClient;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client-only getter: the authoritative level data, packets and saved day remain untouched. */
@Mixin(Level.class)
public abstract class HouseSceneClockMixin {
    @Inject(method="getDayTime",at=@At("RETURN"),cancellable=true)
    private void houseSceneTime(CallbackInfoReturnable<Long> cir){
        Level level=(Level)(Object)this;
        if(level.isClientSide())cir.setReturnValue(NovelSceneClient.sceneTime(level,cir.getReturnValue()));
    }
}
