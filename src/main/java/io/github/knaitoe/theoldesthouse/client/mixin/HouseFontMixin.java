package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.house.HouseText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Covers books (including saved pages), chat, tooltips, subtitles, signs and native labels. */
@Mixin(Font.class)
public abstract class HouseFontMixin {
    @ModifyVariable(method = "drawInBatch8xOutline(Lnet/minecraft/util/FormattedCharSequence;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private FormattedCharSequence oldesthouse$blueOutlinedText(FormattedCharSequence value,
            FormattedCharSequence text, float x, float y, int color, int outline,
            Matrix4f matrix, MultiBufferSource buffers, int light) {
        return HouseText.color(value, color);
    }

    @ModifyVariable(method = "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private FormattedCharSequence oldesthouse$blueComponents(FormattedCharSequence value,
            FormattedCharSequence text, float x, float y, int color, boolean shadow, Matrix4f matrix,
            MultiBufferSource buffers, Font.DisplayMode mode, int background, int light) {
        return HouseText.color(value, color);
    }

    @Redirect(method = "renderText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringDecomposer;iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z"))
    private boolean oldesthouse$blueStrings(String value, Style style, FormattedCharSink sink,
            String text, float x, float y, int color, boolean shadow, Matrix4f matrix,
            MultiBufferSource buffers, Font.DisplayMode mode, int background, int light) {
        FormattedCharSequence sequence = target -> StringDecomposer.iterateFormatted(value, style, target);
        return HouseText.color(sequence, color).accept(sink);
    }
}
