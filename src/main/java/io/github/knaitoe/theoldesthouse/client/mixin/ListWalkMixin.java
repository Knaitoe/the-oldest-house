package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.TheListBodyClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 0.4.705 (docs/THE_LIST_0_4_705.md): the slasher's body never starts a sprint, by key or by a double tap; its walk is a sprint's pace already. */
@Mixin(LocalPlayer.class)
public abstract class ListWalkMixin {
    @Inject(method="canStartSprinting",at=@At("HEAD"),cancellable=true)
    private void oldestHouseListWalk(CallbackInfoReturnable<Boolean> cir){
        if(TheListBodyClient.walking((LocalPlayer)(Object)this))cir.setReturnValue(false);
    }
}
