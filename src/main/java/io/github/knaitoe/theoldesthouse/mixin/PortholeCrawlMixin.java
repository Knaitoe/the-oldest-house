package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.labyrinth.PortholeCrawl;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A held crouch must not pin the crawling player to the yacht's outer sill. */
@Mixin(Player.class)
public abstract class PortholeCrawlMixin {
    @Inject(method="maybeBackOffFromEdge",at=@At("HEAD"),cancellable=true)
    private void oldestHouse$portholeExit(Vec3 movement,MoverType type,CallbackInfoReturnable<Vec3> result){
        if(type==MoverType.SELF&&PortholeCrawl.leavingSill((Player)(Object)this))result.setReturnValue(movement);
    }
}
