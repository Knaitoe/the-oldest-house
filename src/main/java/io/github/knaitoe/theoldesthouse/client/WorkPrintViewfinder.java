package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.WorkPrintRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 0.4.220 (owner: "the camcorder's out of whack when used"): the House's camera is raised like a spyglass, and the held model
 * was drawn up against the reader's eye, filling half their sight. While it is raised the hand is not drawn; the reader looks
 * through a viewfinder instead (corner marks, the centre cross, a blinking REC and the running counter), a little zoomed.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class WorkPrintViewfinder {
    private WorkPrintViewfinder() {}

    /** Whether the reader on this client has the House's camera raised. */
    public static boolean raised() {
        var p = Minecraft.getInstance().player;
        return p != null && p.isUsingItem() && p.getUseItem().is(WorkPrintRegistry.CAMERA_ITEM.get());
    }

    @SubscribeEvent public static void hand(RenderHandEvent e) { if (raised()) e.setCanceled(true); }

    @SubscribeEvent public static void fov(ViewportEvent.ComputeFov e) { if (raised()) e.setFOV(e.getFOV() * .82); }

    @EventBusSubscriber(modid = TheOldestHouse.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent e) {
            e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "work_print_viewfinder"), (g, d) -> {
                if (!raised()) return;
                draw(g, Minecraft.getInstance().player.getTicksUsingItem());
            });
        }
    }

    static void draw(GuiGraphics g, int ticks) {
        int w = g.guiWidth(), h = g.guiHeight(), m = Math.max(12, Math.min(w, h) / 12), len = Math.max(10, m), ink = 0xE6EDE6D6;
        int x0 = m, y0 = m, x1 = w - m, y1 = h - m;
        // Corner marks.
        g.fill(x0, y0, x0 + len, y0 + 2, ink); g.fill(x0, y0, x0 + 2, y0 + len, ink);
        g.fill(x1 - len, y0, x1, y0 + 2, ink); g.fill(x1 - 2, y0, x1, y0 + len, ink);
        g.fill(x0, y1 - 2, x0 + len, y1, ink); g.fill(x0, y1 - len, x0 + 2, y1, ink);
        g.fill(x1 - len, y1 - 2, x1, y1, ink); g.fill(x1 - 2, y1 - len, x1, y1, ink);
        // The centre cross.
        int cx = w / 2, cy = h / 2;
        g.fill(cx - 6, cy, cx - 2, cy + 1, ink); g.fill(cx + 2, cy, cx + 6, cy + 1, ink);
        g.fill(cx, cy - 6, cx + 1, cy - 2, ink); g.fill(cx, cy + 2, cx + 1, cy + 6, ink);
        // REC, blinking once a second, and the tape counter.
        var font = Minecraft.getInstance().font;
        if ((ticks / 10) % 2 == 0) g.fill(x0 + 6, y0 + 8, x0 + 11, y0 + 13, 0xFFC0362C);
        g.drawString(font, "REC", x0 + 14, y0 + 7, ink, true);
        int seconds = ticks / 20;
        g.drawString(font, String.format("SP  0:%02d:%02d", seconds / 60 % 60, seconds % 60), x1 - 74, y1 - 16, ink, true);
    }
}
