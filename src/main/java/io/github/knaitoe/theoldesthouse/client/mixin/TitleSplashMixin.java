package io.github.knaitoe.theoldesthouse.client.mixin;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 0.4.222 (owner): the title screen's splash names the mod, every time; 0.4.240: drawn the way the House's book sets a page (HouseSplash). */
@Mixin(SplashManager.class)
public abstract class TitleSplashMixin {
    @Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
    private void the_oldest_house$splash(CallbackInfoReturnable<SplashRenderer> cir) {
        cir.setReturnValue(new io.github.knaitoe.theoldesthouse.client.HouseSplash());
    }
}
