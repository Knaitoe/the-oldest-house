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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(
        modid = TheOldestHouse.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class HouseTransitionClient {
    private static final ResourceLocation DOOR_TOP =
            ResourceLocation.withDefaultNamespace("textures/block/oak_door_top.png");
    private static final ResourceLocation DOOR_BOTTOM =
            ResourceLocation.withDefaultNamespace("textures/block/oak_door_bottom.png");
    private static final ResourceLocation WHITE_TERRACOTTA =
            ResourceLocation.withDefaultNamespace("textures/block/white_terracotta.png");
    private static final ResourceLocation DARK_OAK =
            ResourceLocation.withDefaultNamespace("textures/block/stripped_dark_oak_log.png");

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

    private static ReceivingLevelScreen createScreen(
            BooleanSupplier ready,
            ReceivingLevelScreen.Reason reason,
            boolean entering
    ) {
        HouseTransitionKind kind = HouseTransitionContextState.peekKind();
        int token = HouseTransitionContextState.peekToken();
        CapturedFrame frame = CapturedFrame.capture(token);

        return switch (kind) {
            case DOOR -> new DoorPassageScreen(
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

    private static final class DoorPassageScreen extends TimedPassageScreen {
        private boolean soundPlayed;

        private DoorPassageScreen(
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
                    entering ? 1450L : 1000L,
                    entering ? 1800L : 1325L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (!soundPlayed) {
                soundPlayed = true;
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(
                                entering
                                        ? SoundEvents.WOODEN_DOOR_OPEN
                                        : SoundEvents.WOODEN_DOOR_CLOSE,
                                entering ? 0.82F : 0.78F
                        )
                );
            }

            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            drawCapturedFrame(graphics);

            double p = progress();
            double approach = smoothstep(Math.min(1.0D, p / 0.55D));

            int startWidth = Math.max(90, (int) (this.width * 0.24D));
            int endWidth = Math.max(this.width + 100, (int) (this.width * 1.12D));
            int doorWidth = lerpInt(startWidth, endWidth, approach);
            int doorHeight = doorWidth * 2;

            int startX = entering
                    ? this.width - startWidth / 4
                    : -startWidth * 3 / 4;
            int endX = (this.width - doorWidth) / 2;
            int doorX = lerpInt(startX, endX, approach);
            int doorY = (this.height - doorHeight) / 2;
            int half = doorHeight / 2;

            drawScaledTexture(
                    graphics,
                    DOOR_TOP,
                    doorX,
                    doorY,
                    doorWidth,
                    half
            );
            drawScaledTexture(
                    graphics,
                    DOOR_BOTTOM,
                    doorX,
                    doorY + half,
                    doorWidth,
                    doorHeight - half
            );

            int jambWidth = Math.max(6, this.width / 80);
            int jambX = entering
                    ? doorX - jambWidth
                    : doorX + doorWidth;
            graphics.fill(
                    jambX,
                    0,
                    jambX + jambWidth,
                    this.height,
                    0xCC0A0705
            );
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
                    entering ? 650L : 500L,
                    entering ? 850L : 700L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double raw = progress();
            double squeeze = smoothstep(
                    Math.min(1.0D, raw / (entering ? 0.72D : 0.58D))
            );

            drawCapturedFrame(graphics);

            // The loading screen now continues motion that already began in the
            // live source world. These are only nearby architectural edges, not
            // a substitute for camera movement.
            int sillHeight = lerpInt(
                    Math.max(3, this.height / 45),
                    Math.max(16, (int) (this.height * 0.30D)),
                    squeeze
            );

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    -10,
                    this.height - sillHeight,
                    this.width + 20,
                    sillHeight + 10
            );

            int jambWidth = Math.max(12, this.width / 16);
            int travel = (int) (this.width * 0.34D * squeeze);
            int jambX = entering
                    ? -jambWidth + travel
                    : this.width - travel;

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    jambX,
                    -12,
                    jambWidth,
                    this.height + 24
            );

            int shadowWidth = Math.max(2, this.width / 180);
            int shadowX = entering
                    ? jambX + jambWidth - shadowWidth
                    : jambX;

            graphics.fill(
                    shadowX,
                    0,
                    shadowX + shadowWidth,
                    this.height,
                    0xA00E0C0A
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
                    entering ? 700L : 525L,
                    entering ? 900L : 725L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double raw = progress();
            double squeeze = smoothstep(
                    Math.min(1.0D, raw / (entering ? 0.74D : 0.60D))
            );

            drawCapturedFrame(graphics);

            // Live camera motion has already turned the player's shoulder into
            // the breach. The frozen interval only needs a close wall edge to
            // continue that trajectory and hide the actual dimension handoff.
            int wallWidth = Math.max(24, (int) (this.width * 0.20D));
            int travel = (int) (this.width * 0.38D * squeeze);
            int wallX = entering
                    ? -wallWidth + travel
                    : this.width - travel;

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    wallX,
                    -14,
                    wallWidth,
                    this.height + 28
            );

            int timberWidth = Math.max(6, this.width / 70);
            int timberX = entering
                    ? wallX + wallWidth - timberWidth
                    : wallX;

            drawScaledTexture(
                    graphics,
                    DARK_OAK,
                    timberX,
                    -10,
                    timberWidth,
                    this.height + 20
            );

            double late = smoothstep(Math.max(0.0D, (raw - 0.48D) / 0.46D));
            int oppositeWidth = lerpInt(
                    0,
                    Math.max(12, (int) (this.width * 0.10D)),
                    late
            );

            if (oppositeWidth > 0) {
                int oppositeX = entering
                        ? this.width - oppositeWidth
                        : 0;

                drawScaledTexture(
                        graphics,
                        WHITE_TERRACOTTA,
                        oppositeX,
                        -12,
                        oppositeWidth,
                        this.height + 24
                );
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

    private static void drawScaledTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(
                width / 16.0F,
                height / 16.0F,
                1.0F
        );

        drawTextureQuad(
                graphics,
                texture,
                0.0F,
                0.0F,
                16.0F,
                16.0F,
                0.0F,
                1.0F,
                0.0F,
                1.0F
        );

        graphics.pose().popPose();
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
