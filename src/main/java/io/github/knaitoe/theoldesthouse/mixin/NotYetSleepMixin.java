package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.NotYet;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0.4.664, Not Yet: lying on the machine's table or on one of its cots is never sleeping long enough to pass the night. The game counts
 * every sleeper in a level towards skipping it, and would wake everyone lying in the ward when it did.
 */
@Mixin(Player.class)
public abstract class NotYetSleepMixin {
    @Inject(method="isSleepingLongEnough",at=@At("HEAD"),cancellable=true)
    private void house$notOnTheMachine(CallbackInfoReturnable<Boolean> cir){
        if(NotYet.onMachineBed((Player)(Object)this))cir.setReturnValue(false);
    }
}
