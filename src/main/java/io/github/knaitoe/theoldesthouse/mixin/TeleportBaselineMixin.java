package io.github.knaitoe.theoldesthouse.mixin;

import java.util.Set;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Run 1597 (0.4.564's live proof): two readers came through the trailer's door, and the one who takes a cousin's place was then put at
 * once where that cousin stood, at the camp (RoleSpots.stand). The server, behind, handled that reader's acknowledgement and first step in
 * one go, and measured the step from where the door had put them, 66 blocks back, not from the camp: "moved too quickly", and a jolt back.
 *
 * <p>The game measures each step from where a reader stood when the tick began, and a move it makes did not move that mark; only the
 * next tick did. A door's own move already set it (HouseInternalTeleport.shift); the House's other moves did not. Now
 * every move does: a reader the game puts somewhere takes their next step from there, however many moves a tick holds and however soon
 * that step comes. Vanilla's movement checks are otherwise unchanged.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class TeleportBaselineMixin {
    @Inject(method="teleport(DDDFFLjava/util/Set;)V",at=@At("TAIL"))
    private void house$stepFromHere(double x,double y,double z,float yaw,float pitch,Set<RelativeMovement> relative,CallbackInfo ci){
        ((ServerGamePacketListenerImpl)(Object)this).resetPosition();
    }
}
