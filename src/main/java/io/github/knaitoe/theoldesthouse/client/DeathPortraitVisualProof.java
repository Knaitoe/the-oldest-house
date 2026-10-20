package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.PortraitProfile;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** Staged native art comparison, including production skin-manager readback and generated-texture lifecycle. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class DeathPortraitVisualProof extends Screen {
    private static boolean launched;
    private final List<ResourceLocation> textures = new ArrayList<>();
    private final UUID standard = new UUID(0, 0), other = otherModel(standard);
    private int frames, page;
    public DeathPortraitVisualProof() { super(Component.literal("First-death family portraits")); }
    private static UUID otherModel(UUID first) {
        for (int n = 1; n < 100; n++) {
            var candidate = new UUID(0, n);
            if (DefaultPlayerSkin.get(candidate).model() != DefaultPlayerSkin.get(first).model()) return candidate;
        }
        throw new IllegalStateException("Native standard/slim skin fixtures are unavailable");
    }

    @Override protected void init() {
        if (!textures.isEmpty()) return;
        var mc = Minecraft.getInstance();
        textures.add(DeathPortraitClient.FAMILY);
        textures.add(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "textures/painting/karen_portrait.png"));
        for (var id : List.of(standard, other)) {
            try (var source = mc.getResourceManager().open(DefaultPlayerSkin.get(id).texture()); var skin = NativeImage.read(source);
                 var original = mc.getResourceManager().open(DeathPortraitClient.FAMILY); var family = NativeImage.read(original)) {
                var painted = PortraitPainter.paint(skin, family);
                verify(painted, family);
                var texture = new DynamicTexture(painted); texture.setFilter(false, false);
                textures.add(mc.getTextureManager().register("portrait_proof_" + id, texture));
            } catch (java.io.IOException exception) { throw new java.io.UncheckedIOException(exception); }
        }
        verifyLayers();
    }
    private static void verify(NativeImage image, NativeImage family) {
        if (image.getWidth() != 16 || image.getHeight() != 32 || image.getPixelRGBA(0, 0) != family.getPixelRGBA(0, 0))
            throw new IllegalStateException("The portrait lost the family painting's native scale or ground");
        var colours = new HashSet<Integer>();
        for (int y = 0; y < 32; y++) for (int x = 0; x < 16; x++) {
            if ((image.getPixelRGBA(x, y) >>> 24) != 255) throw new IllegalStateException("A painted bust has a transparent hole");
            colours.add(image.getPixelRGBA(x, y));
        }
        if (colours.size() < 5 || colours.size() > 128) throw new IllegalStateException("The skin lost its face or the pixel-paint palette: " + colours.size());
    }
    private static void verifyLayers() {
        var mc = Minecraft.getInstance();
        try (var source = mc.getResourceManager().open(DeathPortraitClient.FAMILY); var family = NativeImage.read(source);
             var skin = new NativeImage(64, 64, true); var legacy = new NativeImage(64, 32, true)) {
            for (int y = 0; y < 32; y++) for (int x = 0; x < 64; x++) {
                skin.setPixelRGBA(x, y, x >= 32 && y < 16 ? 0 : 0xff8070b0);
                legacy.setPixelRGBA(x, y, skin.getPixelRGBA(x, y));
            }
            try (var base = PortraitPainter.paint(skin, family); var old = PortraitPainter.paint(legacy, family)) {
                if (base.getPixelRGBA(6, 10) != old.getPixelRGBA(6, 10)) throw new IllegalStateException("A legacy skin's face changed");
                for (int y = 8; y < 16; y++) for (int x = 40; x < 48; x++) skin.setPixelRGBA(x, y, 0xffb02020);
                for (int y = 36; y < 48; y++) for (int x = 20; x < 28; x++) skin.setPixelRGBA(x, y, 0xff20a020);
                try (var dressed = PortraitPainter.paint(skin, family)) {
                    if (base.getPixelRGBA(6, 10) == dressed.getPixelRGBA(6, 10) || base.getPixelRGBA(6, 23) == dressed.getPixelRGBA(6, 23))
                        throw new IllegalStateException("Native hat/jacket layers disappeared from the portrait");
                }
            }
        } catch (java.io.IOException exception) { throw new java.io.UncheckedIOException(exception); }
    }
    private static PortraitProfile profile(UUID id) { return new PortraitProfile(id, "PortraitProof", "", ""); }
    @Override public void renderBackground(GuiGraphics g, int x, int y, float partial) {}
    @Override public void render(GuiGraphics g, int x, int y, float partial) {
        g.fill(0, 0, width, height, 0xff70685b);
        g.drawString(font, "Family portraits / two original paintings / two native player skins / production skin readback", 9, 9, 0xfff0e5cf, false);
        g.flush(); var mc = Minecraft.getInstance(); var out = mc.renderBuffers().bufferSource();
        var samples = new ArrayList<>(textures);
        var production = DeathPortraitClient.texture(profile(standard)); if (production != null) samples.add(production);
        for (int i = 0; i < samples.size(); i++) {
            var pose = g.pose(); pose.pushPose(); float scale = Math.min(width / 5F * .75F, (height - 70) / 2F);
            pose.translate((i + .5F) * width / 5, height * .51F, 200); pose.scale(scale, -scale, scale);
            pose.mulPose(Axis.YP.rotationDegrees(page == 0 ? 180 : 150));
            DeathPortraitClient.front(pose, out, samples.get(i), page == 0 ? LightTexture.FULL_BRIGHT : LightTexture.pack(8, 0));
            out.endBatch(); pose.popPose();
            g.drawCenteredString(font, new String[]{"Will / original", "Karen / original", "Player A / painted", "Player B / painted", "Native skin manager"}[i], (int) ((i + .5F) * width / 5), height - 28, 0xfff0e5cf);
        }
    }
    @SubscribeEvent public static void launch(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (Boolean.getBoolean("the_oldest_house.deathPortraitSmoke") && !launched && mc.screen != null && mc.getOverlay() == null) {
            launched = true; mc.options.guiScale().set(2); mc.resizeDisplay(); mc.setScreen(new DeathPortraitVisualProof());
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) throws Exception {
        var mc = Minecraft.getInstance();
        if (!(mc.screen instanceof DeathPortraitVisualProof proof)) return;
        if (++proof.frames > 1800) throw new IllegalStateException("Native skin loading never completed for the death portrait");
        if (proof.frames < 30 || DeathPortraitClient.texture(profile(proof.standard)) == null) return;
        var actual = ((DynamicTexture) mc.getTextureManager().getTexture(DeathPortraitClient.texture(profile(proof.standard)))).getPixels();
        var expected = ((DynamicTexture) mc.getTextureManager().getTexture(proof.textures.get(2))).getPixels();
        if (actual == null || expected == null) throw new IllegalStateException("Generated portrait pixels were released early");
        for (int y = 0; y < 32; y++) for (int x = 0; x < 16; x++)
            if (actual.getPixelRGBA(x, y) != expected.getPixelRGBA(x, y)) throw new IllegalStateException("Native skin readback changed the face, orientation or palette at " + x + "," + y);
        var dir = Path.of("../build/font-smoke"); Files.createDirectories(dir);
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(dir.resolve("native-death-portraits-" + (proof.page == 0 ? "front" : "dim") + ".png"));
        }
        if (proof.page++ == 0) { proof.frames = 0; return; }
        for (int i = 2; i < proof.textures.size(); i++) mc.getTextureManager().release(proof.textures.get(i));
        DeathPortraitClient.clear();
        Files.writeString(dir.resolve("death-portraits-passed.txt"), "Two original family paintings and two native player-skin paintings on production 1x2 front geometry; normal and dim oblique light. Native skin-manager asynchronous profile resolution, GPU readback and generated-texture release completed. Native 16x32 palette, opaque ground, hat/jacket and legacy-skin assertions passed. These are staged asset views, not a manual ordinary death/return playthrough.\n");
        if (Boolean.getBoolean("the_oldest_house.deathPortraitSmoke")) mc.stop(); else mc.setScreen(new TrailerVisualProof());
    }
}
