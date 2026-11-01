package io.github.knaitoe.theoldesthouse.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 0.4.220 (owner: "lots of your text extends off screen"): the action bar draws one line however long it is, so the House's
 * longer lines ran off both edges. A line wider than the screen allows is wrapped and stacked upward from where the action bar
 * sits, centred, with the same fade, colour and backdrop. Short lines are left to vanilla.
 */
@Mixin(Gui.class)
public abstract class ActionBarWrapMixin {
    @Shadow @Nullable private Component overlayMessageString;
    @Shadow private int overlayMessageTime;
    @Shadow private boolean animateOverlayMessageColor;
    @Shadow public abstract Font getFont();

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void theOldestHouse$wrapLongLines(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        if (overlayMessageString == null || overlayMessageTime <= 0) return;
        Font font = getFont();
        int room = Math.max(120, Math.min(g.guiWidth() - 40, 380));
        if (font.width(overlayMessageString) <= room) return;
        ci.cancel();
        float left = overlayMessageTime - delta.getGameTimeDeltaPartialTick(false);
        int alpha = Math.min(255, (int) (left * 255F / 20F));
        if (alpha <= 8) return;
        int colour = animateOverlayMessageColor ? Mth.hsvToRgb(left / 50F, .7F, .6F) & 0xFFFFFF : 0xFFFFFF;
        int fade = alpha << 24 & 0xFF000000;
        var lines = font.split(overlayMessageString, room);
        int step = font.lineHeight + 2, top = -4 - (lines.size() - 1) * step;
        int backdrop = Minecraft.getInstance().options.getBackgroundColor(0F);
        g.pose().pushPose();
        g.pose().translate(g.guiWidth() / 2F, g.guiHeight() - 68, 0);
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            int w = font.width(line), x = -w / 2, y = top + i * step;
            if (backdrop != 0) g.fill(x - 2, y - 2, x + w + 2, y + font.lineHeight + 2, FastColor.ARGB32.multiply(backdrop, colour | fade));
            g.drawString(font, line, x, y, colour | fade, true);
        }
        g.pose().popPose();
    }
}
