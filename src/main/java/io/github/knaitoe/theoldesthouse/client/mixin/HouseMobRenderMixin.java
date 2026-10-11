package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.HouseSightlineState;
import io.github.knaitoe.theoldesthouse.house.HouseMobSight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keep the registered renderer's private-owner checks, native frustum, fog and depth test. */
@Mixin(EntityRenderer.class)
public abstract class HouseMobRenderMixin {
    @Redirect(method = "shouldRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;shouldRender(DDD)Z"))
    private boolean theOldestHouse$landscapeDistance(Entity entity, double x, double y, double z) {
        if (!HouseMobSight.rendersToHorizon(entity, HouseSightlineState.origin()))
            return entity.shouldRender(x, y, z);
        return entity.distanceToSqr(x, y, z) <= HouseMobSight.distanceSquared(
                Minecraft.getInstance().options.getEffectiveRenderDistance());
    }
}
