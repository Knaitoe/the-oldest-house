package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.client.ModelUvs;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 0.4.240 (owner, on the dust hall: "the blocks here might need to have their pixel maps adjusted"). A model face with no "uv" of its
 * own takes one from where its element sits in the block; an element that reaches above the block (a chair's back) or below it gets
 * coordinates off its own texture, and the face shows whatever picture sits next to it in the atlas. 125 of the mod's models had such
 * faces. Their coordinates are moved back onto the texture here, one pixel to one pixel, wherever they fall off it.
 */
@Mixin(BlockElement.class)
public abstract class ModelUvRangeMixin {
    @Inject(method="uvsByFace",at=@At("RETURN"),cancellable=true)
    private void theOldestHouse$keepUvsOnTheTexture(Direction face,CallbackInfoReturnable<float[]> cir){cir.setReturnValue(ModelUvs.fit(cir.getReturnValue()));}
}
