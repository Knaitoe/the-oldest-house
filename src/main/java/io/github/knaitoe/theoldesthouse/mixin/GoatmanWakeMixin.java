package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.GoatmanBedtime;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Covers Leave Bed packets and native automatic waking for the committed camper ending. */
@Mixin(ServerPlayer.class)
public abstract class GoatmanWakeMixin {
    @Inject(method="stopSleepInBed",at=@At("HEAD"),cancellable=true)
    private void house$finishBedside(boolean immediately,boolean updateSleeping,CallbackInfo ci){
        if(GoatmanBedtime.locked((ServerPlayer)(Object)this))ci.cancel();
    }
}
