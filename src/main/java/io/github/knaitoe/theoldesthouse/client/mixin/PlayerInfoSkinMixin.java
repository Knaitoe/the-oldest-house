package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.HouseBodyClient;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 0.4.163: a reader wearing the House's body is drawn in it wherever their skin is asked for, the tab list included. */
@Mixin(PlayerInfo.class)
public abstract class PlayerInfoSkinMixin {
    @Shadow public abstract com.mojang.authlib.GameProfile getProfile();
    @Inject(method = "getSkin()Lnet/minecraft/client/resources/PlayerSkin;", at = @At("RETURN"), cancellable = true)
    private void house$body(CallbackInfoReturnable<PlayerSkin> cir) {
        if (HouseBodyClient.reading) return;
        var body = HouseBodyClient.skin(getProfile().getId());
        if (body != null) cir.setReturnValue(body);
    }
}
