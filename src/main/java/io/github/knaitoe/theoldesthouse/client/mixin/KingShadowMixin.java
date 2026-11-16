package io.github.knaitoe.theoldesthouse.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.client.KingClient;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 0.4.642: a reader who is only a sort of thing, or a line, in the King's country casts no shadow (KingClient). */
@Mixin(EntityRenderDispatcher.class)
public abstract class KingShadowMixin {
    @Inject(method="renderShadow",at=@At("HEAD"),cancellable=true)
    private static void theOldestHouse$noShadow(PoseStack pose,MultiBufferSource buffer,Entity entity,float weight,float partial,LevelReader level,float size,CallbackInfo ci){
        if(KingClient.shadowless(entity))ci.cancel();
    }
}
