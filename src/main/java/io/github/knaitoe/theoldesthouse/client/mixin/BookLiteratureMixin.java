package io.github.knaitoe.theoldesthouse.client.mixin;

import io.github.knaitoe.theoldesthouse.house.HouseText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Books and lecterns are literature: while they draw their pages, the Minotaur is red. Chat and the HUD draw outside this. */
@Mixin({BookViewScreen.class, BookEditScreen.class})
public abstract class BookLiteratureMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("HEAD"))
    private void oldesthouse$enterLiterature(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
        HouseText.enterLiterature();
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void oldesthouse$leaveLiterature(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo info) {
        HouseText.leaveLiterature();
    }
}
