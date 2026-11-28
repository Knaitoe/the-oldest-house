package io.github.knaitoe.theoldesthouse.client.mixin;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Another reader's private cast cannot intercept this reader's real interaction ray. */
@Mixin(LiteraryActor.class)
public abstract class GhostsPickMixin {
    @Inject(method="isPickable",at=@At("HEAD"),cancellable=true)
    private void ownCast(CallbackInfoReturnable<Boolean> cir){var a=(LiteraryActor)(Object)this;
        // 0.4.767: for the client's own copy of the actor only. The server inside a single-player or LAN game runs this too, and
        // answered for the host: a guest's cast could not be struck by a ball or a dart there, as it can on a dedicated server.
        if(!a.level().isClientSide())return;
        var p=Minecraft.getInstance().player;if(a.role()>=LiteraryActor.GHOST_MARJORIE&&a.owner().isPresent()&&(p==null||!a.owner().get().equals(io.github.knaitoe.theoldesthouse.labyrinth.GhostsCrew.castRole(a.role())?io.github.knaitoe.theoldesthouse.client.GhostsTakeClient.cast(p.getUUID()):p.getUUID())))cir.setReturnValue(false);}
}
