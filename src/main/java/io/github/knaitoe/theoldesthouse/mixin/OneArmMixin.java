package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.BodyLoss;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A reader who gave an arm at the cabin uses the one they kept, whatever the handedness option later says. */
@Mixin(Player.class)
public abstract class OneArmMixin {
    @Inject(method="getMainArm",at=@At("HEAD"),cancellable=true)
    private void house$keptArm(CallbackInfoReturnable<HumanoidArm> cir){
        HumanoidArm missing=BodyLoss.missing((Player)(Object)this);
        if(missing!=null)cir.setReturnValue(missing.getOpposite());
    }
}
