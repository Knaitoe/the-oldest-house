package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.HouseBodyClient;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 0.4.163: a reader wearing the House's body is drawn in it wherever their skin is asked for, the tab list included (0.4.201: every reader inside the House). 0.4.354: and at the flood station, turned or torn (CrossingClient). */
@Mixin(PlayerInfo.class)
public abstract class PlayerInfoSkinMixin {
    @Shadow public abstract com.mojang.authlib.GameProfile getProfile();
    @Inject(method = "getSkin()Lnet/minecraft/client/resources/PlayerSkin;", at = @At("RETURN"), cancellable = true)
    private void house$body(CallbackInfoReturnable<PlayerSkin> cir) {
        var body = HouseBodyClient.skin(getProfile().getId());
        // 0.4.354: at the flood station after the second dusk, a turned reader drawn drowned, a torn one with the tear in the cloth.
        var crossing = io.github.knaitoe.theoldesthouse.client.CrossingClient.skin(getProfile().getId(), body != null ? body : cir.getReturnValue());
        if (crossing != null) cir.setReturnValue(crossing);
        else if (body != null) cir.setReturnValue(body);
    }
}
