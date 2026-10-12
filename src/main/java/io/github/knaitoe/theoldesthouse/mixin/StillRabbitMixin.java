package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.HolyRabbit;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Rabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** Keep the original scene rabbit still through actual native body collisions. */
@Mixin(LivingEntity.class)
public abstract class StillRabbitMixin {
    @Inject(method="isPushable",at=@At("HEAD"),cancellable=true)
    private void stillRabbit(CallbackInfoReturnable<Boolean> result){if((Object)this instanceof Rabbit rabbit&&HolyRabbit.owner(rabbit).isPresent())result.setReturnValue(false);}
    @Inject(method="doPush",at=@At("HEAD"),cancellable=true)
    private void noRabbitShove(Entity other,CallbackInfo result){if((Object)this instanceof Rabbit rabbit&&HolyRabbit.owner(rabbit).isPresent()||other instanceof Rabbit otherRabbit&&HolyRabbit.owner(otherRabbit).isPresent())result.cancel();}
}
