package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/** A plain fade to black and back, drawn over everything else on the HUD. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HouseFadeClient {
    private HouseFadeClient() {
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "fade"), HouseFadeClient::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        HouseFadeState.Fade current = HouseFadeState.current();
        if (current == null) {
            return;
        }
        long t = System.nanoTime() - current.start();
        float alpha;
        if (t < current.in()) {
            alpha = (float) t / current.in();
        } else if (t < current.in() + current.hold()) {
            alpha = 1.0F;
        } else if (t < current.in() + current.hold() + current.out()) {
            alpha = 1.0F - (float) (t - current.in() - current.hold()) / current.out();
        } else {
            HouseFadeState.clear();
            return;
        }
        int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), a << 24);
    }
}
