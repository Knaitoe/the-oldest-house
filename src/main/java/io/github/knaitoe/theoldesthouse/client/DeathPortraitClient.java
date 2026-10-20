package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.PortraitPainting;
import io.github.knaitoe.theoldesthouse.house.PortraitProfile;
import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraft.world.entity.decoration.Painting;
import org.lwjgl.opengl.GL11;

/** Only a claimed painting's front changes. Its native back, sides, light, culling and tracking remain. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class DeathPortraitClient {
    public static final ResourceLocation FAMILY = ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,
            "textures/painting/will_portrait.png");
    private static final LinkedHashMap<String, Entry> CACHE = new LinkedHashMap<>();
    private static int generation;
    private static final class Entry { ResourceLocation texture; }
    private DeathPortraitClient() {}

    public static void render(Painting painting, float yaw, PoseStack pose, MultiBufferSource buffers, int light) {
        var profile = PortraitProfile.decode(((PortraitPainting) painting).housePortrait());
        if (profile == null || painting.getVariant().value().width() != 1 || painting.getVariant().value().height() != 2) return;
        var texture = texture(profile); if (texture == null) return;
        pose.pushPose(); pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        front(pose, buffers, texture, light); pose.popPose();
    }

    /** Coordinates match PaintingRenderer's front at -0.5/16 blocks, with a one-thousandth-block bias. */
    public static void front(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture, int light) {
        var out = buffers.getBuffer(RenderType.entityCutoutNoCull(texture)); var p = pose.last();
        float z = -.03225F;
        out.addVertex(p, .5F, -1, z).setColor(-1).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 0, -1);
        out.addVertex(p, -.5F, -1, z).setColor(-1).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 0, -1);
        out.addVertex(p, -.5F, 1, z).setColor(-1).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 0, -1);
        out.addVertex(p, .5F, 1, z).setColor(-1).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, 0, 0, -1);
    }

    public static @Nullable ResourceLocation texture(PortraitProfile profile) {
        String key = profile.encode(); var cached = CACHE.get(key); if (cached != null) return cached.texture;
        var entry = new Entry(); CACHE.put(key, entry); int epoch = generation;
        if (CACHE.size() > 32) {
            var oldest = CACHE.entrySet().iterator(); var evicted = oldest.next();
            if (evicted.getValue().texture != null) Minecraft.getInstance().getTextureManager().release(evicted.getValue().texture);
            oldest.remove();
        }
        var mc = Minecraft.getInstance();
        // Minecraft's skin manager validates/resolves the saved native profile; never fetch arbitrary image URLs.
        mc.getSkinManager().getOrLoad(profile.gameProfile()).whenComplete((resolved, failure) -> mc.execute(() -> {
            if (epoch != generation || CACHE.get(key) != entry) return;
            var skin = failure == null ? mc.getSkinManager().getInsecureSkin(profile.gameProfile()) : DefaultPlayerSkin.get(profile.owner());
            int previous = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
            try (var pixels = new NativeImage(64, 64, false);
                 var stream = mc.getResourceManager().open(FAMILY); var family = NativeImage.read(stream)) {
                mc.getTextureManager().getTexture(skin.texture()).bind(); pixels.downloadTexture(0, false);
                var texture = new DynamicTexture(PortraitPainter.paint(pixels, family)); texture.setFilter(false, false);
                entry.texture = mc.getTextureManager().register("the_oldest_house_portrait_" + profile.owner(), texture);
            } catch (Exception exception) {
                TheOldestHouse.LOGGER.warn("Could not paint the House portrait for {}", profile.owner(), exception);
                CACHE.remove(key);
            } finally { GlStateManager._bindTexture(previous); }
        }));
        return entry.texture;
    }

    public static void clear() {
        generation++;
        var manager = Minecraft.getInstance().getTextureManager();
        for (var entry : CACHE.values()) if (entry.texture != null) manager.release(entry.texture);
        CACHE.clear();
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }
    @EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void reload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager ->
                    Minecraft.getInstance().execute(DeathPortraitClient::clear));
        }
    }
}
