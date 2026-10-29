package io.github.knaitoe.theoldesthouse.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.client.GhostsCallSheetRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternRenderer.class)
public abstract class GhostsLecternMixin {
    @Inject(method="render(Lnet/minecraft/world/level/block/entity/LecternBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",at=@At("HEAD"),cancellable=true)
    private void callSheet(LecternBlockEntity lectern,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay,CallbackInfo ci){if(GhostsCallSheetRenderer.source(lectern)&&lectern.getBlockState().getValue(LecternBlock.HAS_BOOK)){GhostsCallSheetRenderer.render(pose,buffers,lectern.getBlockState().getValue(LecternBlock.FACING),light,overlay);ci.cancel();}
        // 0.4.164: a scene's own reading stand draws its paper in its model; the vanilla open book would float over it.
        else if(io.github.knaitoe.theoldesthouse.labyrinth.ReadingStandBlock.is(lectern.getBlockState()))ci.cancel();}
}
