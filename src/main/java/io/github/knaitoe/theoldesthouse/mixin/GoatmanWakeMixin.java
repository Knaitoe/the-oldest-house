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
        // 0.4.664: nor does a reader the machine in Not Yet is mending get up off its table or their cot before it is done.
        if(GoatmanBedtime.locked((ServerPlayer)(Object)this)||io.github.knaitoe.theoldesthouse.labyrinth.NotYetMending.held((ServerPlayer)(Object)this))ci.cancel();
    }
}
