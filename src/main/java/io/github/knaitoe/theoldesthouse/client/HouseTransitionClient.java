package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionKind;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextState;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(
        modid = TheOldestHouse.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class HouseTransitionClient {
    private HouseTransitionClient() {
    }

    @SubscribeEvent
    public static void registerTransitionScreens(RegisterDimensionTransitionScreenEvent event) {
        event.registerConditionalEffect(
                HouseDimensions.INTERIOR,
                Level.OVERWORLD,
                (supplier, reason) -> createScreen(supplier, reason, true)
        );

        event.registerConditionalEffect(
                Level.OVERWORLD,
                HouseDimensions.INTERIOR,
                (supplier, reason) -> createScreen(supplier, reason, false)
        );
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(
                (ResourceManagerReloadListener) resourceManager -> HouseSightlineRenderer.invalidateSprites()
        );
    }

    private static ReceivingLevelScreen createScreen(
            BooleanSupplier ready,
            ReceivingLevelScreen.Reason reason,
            boolean entering
    ) {
        HouseTransitionKind kind = HouseTransitionContextState.peekKind();
        int token = HouseTransitionContextState.peekToken();
        CapturedFrame frame = CapturedFrame.capture(token);

        return switch (kind) {
            case DOOR -> new BreachPassageScreen(
                    ready,
                    reason,
                    entering,
                    token,
                    frame
            );
            case WINDOW -> new WindowPassageScreen(
                    ready,
                    reason,
                    entering,
                    token,
                    frame
            );
            case BREACH -> new BreachPassageScreen(
                    ready,
                    reason,
                    entering,
                    token,
                    frame
            );
        };
    }

    private abstract static class TimedPassageScreen extends ReceivingLevelScreen {
        protected final long openedAt;
        protected final long minimumVisibleNanos;
        protected final boolean entering;
        protected final int transitionToken;
        private final CapturedFrame capturedFrame;

        protected TimedPassageScreen(
                BooleanSupplier ready,
                Reason reason,
                boolean entering,
                int transitionToken,
                CapturedFrame capturedFrame,
                long minMillis,
                long maxMillis
        ) {
            this(
                    ready,
                    reason,
                    entering,
                    transitionToken,
                    capturedFrame,
                    System.nanoTime(),
                    randomDuration(minMillis, maxMillis),
                    true
            );
        }

        private TimedPassageScreen(
                BooleanSupplier ready,
                Reason reason,
                boolean entering,
                int transitionToken,
                CapturedFrame capturedFrame,
                long openedAt,
                long minimumVisibleNanos,
                boolean resolvedDuration
        ) {
            super(
                    () -> ready.getAsBoolean()
                            && System.nanoTime() - openedAt >= minimumVisibleNanos,
                    reason
            );
            this.openedAt = openedAt;
            this.minimumVisibleNanos = minimumVisibleNanos;
            this.entering = entering;
            this.transitionToken = transitionToken;
            this.capturedFrame = capturedFrame;
        }

        @Override
        public void removed() {
            super.removed();
            HouseTransitionMotion.beginPost(transitionToken);
            if (capturedFrame != null) {
                capturedFrame.close();
            }
        }

        protected void drawCapturedFrame(GuiGraphics graphics) {
            drawCapturedFrameMotion(graphics, 1.0D, 0.0D, 0.0D);
        }

        protected void drawCapturedFrameMotion(
                GuiGraphics graphics,
                double zoom,
                double offsetX,
                double offsetY
        ) {
            if (capturedFrame == null) {
                graphics.fill(0, 0, this.width, this.height, 0xFF0D0C0B);
                return;
            }

            graphics.pose().pushPose();

            float cx = this.width / 2.0F;
            float cy = this.height / 2.0F;
            graphics.pose().translate(
                    cx + (float) offsetX,
                    cy + (float) offsetY,
                    0.0F
            );
            graphics.pose().scale((float) zoom, (float) zoom, 1.0F);
            graphics.pose().translate(-cx, -cy, 0.0F);

            capturedFrame.draw(graphics, this.width, this.height);
            graphics.pose().popPose();
        }

        protected double progress() {
            return Math.min(
                    1.0D,
                    (double) (System.nanoTime() - openedAt) / minimumVisibleNanos
            );
        }

        protected static double smoothstep(double value) {
            double t = Math.max(0.0D, Math.min(1.0D, value));
            return t * t * (3.0D - 2.0D * t);
        }

        protected static int lerpInt(int start, int end, double amount) {
            return (int) Math.round(start + (end - start) * amount);
        }

        private static long randomDuration(long minMillis, long maxMillis) {
            long millis = ThreadLocalRandom.current().nextLong(
                    minMillis,
                    maxMillis + 1L
            );
            return millis * 1_000_000L;
        }
    }

    private static final class WindowPassageScreen extends TimedPassageScreen {
        private WindowPassageScreen(
                BooleanSupplier ready,
                Reason reason,
                boolean entering,
                int token,
                CapturedFrame frame
        ) {
            super(
                    ready,
                    reason,
                    entering,
                    token,
                    frame,
                    entering ? 360L : 280L,
                    entering ? 520L : 420L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            drawCapturedFrame(graphics);

            double raw = progress();
            double p = smoothstep(raw);

            // The source-world camera already performed the actual climb before
            // teleport. Preserve that real final frame instead of painting fake
            // block faces over it. Only a little near-camera shadow remains to
            // soften the frozen handoff.
            int sideAlpha = (int) (48.0D * Math.sin(Math.min(1.0D, p) * Math.PI));
            int bottomAlpha = (int) (34.0D * Math.sin(Math.min(1.0D, p) * Math.PI));

            int sideWidth = Math.max(
                    8,
                    (int) (this.width * (entering ? 0.055D : 0.040D))
            );
            int sideColor = (sideAlpha << 24);
            int bottomColor = (bottomAlpha << 24);

            if (entering) {
                graphics.fill(0, 0, sideWidth, this.height, sideColor);
            } else {
                graphics.fill(
                        this.width - sideWidth,
                        0,
                        this.width,
                        this.height,
                        sideColor
                );
            }

            int bottomHeight = Math.max(6, (int) (this.height * 0.045D));
            graphics.fill(
                    0,
                    this.height - bottomHeight,
                    this.width,
                    this.height,
                    bottomColor
            );
        }
    }

    private static final class BreachPassageScreen extends TimedPassageScreen {
        private BreachPassageScreen(
                BooleanSupplier ready,
                Reason reason,
                boolean entering,
                int token,
                CapturedFrame frame
        ) {
            super(
                    ready,
                    reason,
                    entering,
                    token,
                    frame,
                    entering ? 400L : 300L,
                    entering ? 560L : 440L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            drawCapturedFrame(graphics);

            double raw = progress();
            double p = smoothstep(raw);

            // The shoulder turn and approach happen while the source world is
            // still live. Keep the captured world intact here and use only a
            // restrained near-wall shadow to conceal the handoff.
            double pulse = Math.sin(Math.min(1.0D, p) * Math.PI);
            int nearAlpha = (int) (58.0D * pulse);
            int farAlpha = (int) (28.0D * pulse);

            int nearWidth = Math.max(
                    12,
                    (int) (this.width * (entering ? 0.075D : 0.055D))
            );
            int farWidth = Math.max(
                    5,
                    (int) (this.width * 0.025D)
            );

            int nearColor = (nearAlpha << 24);
            int farColor = (farAlpha << 24);

            if (entering) {
                graphics.fill(0, 0, nearWidth, this.height, nearColor);
                graphics.fill(
                        this.width - farWidth,
                        0,
                        this.width,
                        this.height,
                        farColor
                );
            } else {
                graphics.fill(
                        this.width - nearWidth,
                        0,
                        this.width,
                        this.height,
                        nearColor
                );
                graphics.fill(0, 0, farWidth, this.height, farColor);
            }
        }
    }

    private static final class CapturedFrame {
        private final ResourceLocation textureLocation;
        private boolean closed;

        private CapturedFrame(ResourceLocation textureLocation) {
            this.textureLocation = textureLocation;
        }

        static CapturedFrame capture(int token) {
            try {
                Minecraft minecraft = Minecraft.getInstance();
                NativeImage image = Screenshot.takeScreenshot(
                        minecraft.getMainRenderTarget()
                );
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation location = minecraft.getTextureManager().register(
                        "the_oldest_house_transition_" + token + "_"
                                + System.nanoTime(),
                        texture
                );
                return new CapturedFrame(location);
            } catch (Throwable throwable) {
                TheOldestHouse.LOGGER.warn(
                        "Could not capture the pre-transition gameplay frame.",
                        throwable
                );
                return null;
            }
        }

        void draw(GuiGraphics graphics, int width, int height) {
            drawTextureQuad(
                    graphics,
                    textureLocation,
                    0.0F,
                    0.0F,
                    width,
                    height,
                    0.0F,
                    1.0F,
                    0.0F,
                    1.0F
            );
        }

        void close() {
            if (closed) {
                return;
            }

            closed = true;
            Minecraft.getInstance().getTextureManager().release(textureLocation);
        }
    }



    private static void drawTextureQuad(
            GuiGraphics graphics,
            ResourceLocation texture,
            float x1,
            float y1,
            float x2,
            float y2,
            float u1,
            float u2,
            float v1,
            float v2
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX
        );

        buffer.addVertex(matrix, x1, y1, 0.0F).setUv(u1, v1);
        buffer.addVertex(matrix, x1, y2, 0.0F).setUv(u1, v2);
        buffer.addVertex(matrix, x2, y2, 0.0F).setUv(u2, v2);
        buffer.addVertex(matrix, x2, y1, 0.0F).setUv(u2, v1);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }
}
