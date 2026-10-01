package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BlindfoldItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * The blindfold, worn: black over the whole view but a thin strip at the
 * bottom, where the floor around the player's feet shows through. Drawn
 * under the rest of the HUD, so the hotbar, the action bar and the
 * subtitles (with their arrows) stay readable.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BlindfoldClient {
    private static final ResourceLocation CLOTH = ResourceLocation.fromNamespaceAndPath(
            TheOldestHouse.MOD_ID, "textures/gui/blindfold_edge.png");
    private BlindfoldClient() {
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "blindfold"), BlindfoldClient::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || ClapGameClientState.revealed() || (!BlindfoldItem.isWorn(minecraft.player) && !ClapGameClientState.bound())) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int strip = Math.max(10, height / 9);
        int edge = height - strip;
        graphics.fill(0, 0, width, edge, 0xFF000000);
        // The cloth's lower edge is soft, not a ruled line.
        graphics.fillGradient(0, edge, width, edge + strip / 3, 0xFF000000, 0x00000000);
        // A frayed, woven cloth edge above the gap. Tile its native pixels;
        // the sealed area above remains fully opaque.
        for (int x = 0; x < width; x += 64) {
            int tile = Math.min(64, width - x);
            graphics.blit(CLOTH, x, edge - 12, 0, 0, tile, 16, 64, 16);
        }
        ClapCueClient.render(graphics,edge);
    }
}
