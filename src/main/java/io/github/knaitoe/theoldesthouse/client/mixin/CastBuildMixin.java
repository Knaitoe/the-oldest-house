package io.github.knaitoe.theoldesthouse.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.client.CastBuilds;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

/**
 * 0.4.679: each person's build (client.CastBuilds) on whatever humanoid renderer draws them, laid on after their pose is set and
 * taken off once they and their layers are drawn.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class CastBuildMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow protected M model;
    @Unique private final ArrayDeque<Object> theOldestHouse$builds = new ArrayDeque<>();

    // One entry a render, from its start to its return (a render cancelled before the pose is set has nothing to take off).
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void theOldestHouse$begin(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo info) {
        theOldestHouse$builds.push(CastBuilds.NONE);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V", shift = At.Shift.AFTER))
    private void theOldestHouse$build(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo info) {
        @SuppressWarnings("unchecked") var self = (LivingEntityRenderer<T, M>) (Object) this;
        if (!theOldestHouse$builds.isEmpty()) theOldestHouse$builds.pop();
        theOldestHouse$builds.push(CastBuilds.apply(model, self.getTextureLocation(entity), entity, pose));
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void theOldestHouse$unbuild(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo info) {
        if (!theOldestHouse$builds.isEmpty()) CastBuilds.restore(theOldestHouse$builds.pop());
    }
}
