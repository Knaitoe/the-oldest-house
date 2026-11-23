package io.github.knaitoe.theoldesthouse.client.mixin;
import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.knaitoe.theoldesthouse.client.UncannyCameras;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** The native level renderer must bind the feed target even when its passes request Minecraft's main target. */
@Mixin(Minecraft.class)
public abstract class UncannyTargetMixin {
    @Inject(method="getMainRenderTarget",at=@At("HEAD"),cancellable=true)
    private void uncannyFeedTarget(CallbackInfoReturnable<RenderTarget> result){if(UncannyCameras.recording()&&UncannyCameras.target()!=null)result.setReturnValue(UncannyCameras.target());
        // 0.4.662: and the feed of a watched hall, while its picture is taken.
        else if(io.github.knaitoe.theoldesthouse.client.WaxCameras.recording()&&io.github.knaitoe.theoldesthouse.client.WaxCameras.target()!=null)result.setReturnValue(io.github.knaitoe.theoldesthouse.client.WaxCameras.target());
        else if(io.github.knaitoe.theoldesthouse.client.HallFeedClient.recording()&&io.github.knaitoe.theoldesthouse.client.HallFeedClient.target()!=null)result.setReturnValue(io.github.knaitoe.theoldesthouse.client.HallFeedClient.target());}
}
