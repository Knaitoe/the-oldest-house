package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.opening.ContentWarning;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * 0.4.222 (owner: "remove the 'take book' option from the intro warning"): the opening warning is read in the vanilla lectern
 * view, which offers Take Book beside Done. For the warning's book the Take Book button is hidden and switched off, and Done
 * takes the whole width, as it does for a reader who cannot build. The lectern view keeps no title, so the book is known by its
 * own mark, which arrives with the menu's contents; checking every frame catches it the moment it does, and after a resize.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class ContentWarningClient {
    private ContentWarningClient() {}

    @SubscribeEvent public static void render(ScreenEvent.Render.Pre e) {
        if (!(e.getScreen() instanceof LecternScreen screen) || !ContentWarning.is(screen.getMenu().getBook())) return;
        for (var listener : screen.children()) {
            if (!(listener instanceof AbstractWidget widget) || !(widget.getMessage().getContents() instanceof TranslatableContents key)) continue;
            if (key.getKey().equals("lectern.take_book")) { widget.visible = false; widget.active = false; }
            else if (key.getKey().equals("gui.done") && widget.getWidth() != 200) { widget.setWidth(200); widget.setX(screen.width / 2 - 100); }
        }
    }
}
