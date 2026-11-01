package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 0.4.223 (owner, of the waiting room's glasses: "what is that intended to be/do?"): putting them on showed nothing, so they
 * seemed to vanish from the table. While they are on, their rims sit out of focus at the edges of sight, under the hotbar like a
 * pumpkin's mask.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ReadingGlassesView {
    private static final ResourceLocation RIMS = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/gui/house_read/reading_glasses.png");
    private ReadingGlassesView() {}

    /** Whether the reader on this client has the waiting room's glasses on, in first person. */
    static boolean wearing() {
        var mc = Minecraft.getInstance();
        return mc.player != null && mc.options.getCameraType().isFirstPerson() && mc.player.getItemBySlot(EquipmentSlot.HEAD).is(HouseReadRegistry.GLASSES.get());
    }

    @SubscribeEvent public static void register(RegisterGuiLayersEvent e) {
        e.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "reading_glasses"), (g, d) -> {
            if (!wearing()) return;
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            g.blit(RIMS, 0, 0, 0, 0, g.guiWidth(), g.guiHeight(), g.guiWidth(), g.guiHeight());
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        });
    }
}
