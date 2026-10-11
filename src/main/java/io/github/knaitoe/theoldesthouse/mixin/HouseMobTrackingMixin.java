package io.github.knaitoe.theoldesthouse.mixin;

import io.github.knaitoe.theoldesthouse.house.HouseMobSight;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Only widen the distance gate; native chunk delivery and private broadcast gates still run. */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class HouseMobTrackingMixin {
    @Shadow @Final private Entity entity;

    @Redirect(method = "updatePlayer", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int theOldestHouse$landscapeRange(int entityRange, int playerViewRange) {
        if (!HouseMobSight.tracksToHorizon(entity)) return Math.min(entityRange, playerViewRange);
        // Vanilla supplies (effective view distance - 1) * 16 here. Cover the visible edge
        // chunks and diagonal corners; isChunkTracked remains the final delivery gate.
        return (int)Math.ceil(Math.sqrt(2.0D) * (Math.max(0, playerViewRange) + 32.0D));
    }
}
