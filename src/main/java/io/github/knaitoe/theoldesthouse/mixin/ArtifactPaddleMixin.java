package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.Artifacts;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** The ticket stub (Artifacts): its reader's paddle pushes a boat a quarter harder. The paddler's client steers, so both sides run it. */
@Mixin(Boat.class)
public abstract class ArtifactPaddleMixin {
    @ModifyVariable(method="controlBoat",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/vehicle/Boat;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"),ordinal=0)
    private float paddle(float push){return Artifacts.boatPush((Boat)(Object)this,push);}
}
