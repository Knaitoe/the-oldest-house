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
            HouseTransitionContextState.clear(transitionToken);
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
                    entering ? 1250L : 825L,
                    entering ? 1550L : 1100L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double raw = progress();
            double p = smoothstep(raw);
            double squeeze = entering
                    ? smoothstep(Math.min(1.0D, raw / 0.78D))
                    : smoothstep(Math.min(1.0D, raw / 0.60D));

            // Sell a physical climb rather than a flat wipe. As the player's
            // head rises over the sill, the world appears to drop slightly and
            // drift toward the shoulder that clears the opening.
            double cameraLift = entering ? -this.height * 0.055D : -this.height * 0.035D;
            double cameraSide = (entering ? -1.0D : 1.0D) * this.width * 0.025D;
            double bob = Math.sin(Math.min(1.0D, raw) * Math.PI) * this.height * 0.012D;
            double zoom = 1.0D + p * (entering ? 0.035D : 0.022D);

            drawCapturedFrameMotion(
                    graphics,
                    zoom,
                    cameraSide * p,
                    cameraLift * p + bob
            );

            // The sill starts low, then rises quickly as the body commits to
            // climbing through. It does not simply grow to fill the screen.
            int sillHeight = lerpInt(
                    Math.max(4, this.height / 36),
                    Math.max(24, (int) (this.height * 0.50D)),
                    squeeze
            );
            int sillY = this.height - sillHeight
                    + (int) (Math.sin(raw * Math.PI) * this.height * 0.035D);

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    -12,
                    sillY,
                    this.width + 24,
                    sillHeight + 18
            );

            // The jamb sweeps inward and slightly past center rather than just
            // becoming a wider rectangle. That directional travel is the cue
            // that the player's shoulder/head is passing the frame.
            int jambWidth = Math.max(18, this.width / 12);
            int travel = (int) ((this.width * 0.52D + jambWidth) * squeeze);
            int jambX = entering
                    ? -jambWidth + travel
                    : this.width - travel;

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    jambX,
                    -18,
                    jambWidth,
                    this.height + 36
            );

            int shadowWidth = Math.max(3, this.width / 150);
            int shadowX = entering
                    ? jambX + jambWidth - shadowWidth
                    : jambX;

            graphics.fill(
                    shadowX,
                    0,
                    shadowX + shadowWidth,
                    this.height,
                    0xB00E0C0A
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
                    entering ? 1325L : 875L,
                    entering ? 1650L : 1175L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double raw = progress();
            double p = smoothstep(raw);
            double squeeze = entering
                    ? smoothstep(Math.min(1.0D, raw / 0.80D))
                    : smoothstep(Math.min(1.0D, raw / 0.62D));

            // A squeeze through a rough breach is mostly lateral. The player's
            // viewpoint slides and moves a little closer as one shoulder turns.
            double direction = entering ? -1.0D : 1.0D;
            double lateral = direction * this.width * 0.060D * p;
            double vertical = Math.sin(raw * Math.PI) * this.height * 0.010D;
            double zoom = 1.0D + p * (entering ? 0.045D : 0.028D);

            drawCapturedFrameMotion(
                    graphics,
                    zoom,
                    lateral,
                    vertical
            );

            // Use one dominant wall face that actually travels across the
            // player's field of view. The opposite edge only closes in late.
            int dominantWidth = Math.max(40, (int) (this.width * 0.47D));
            int dominantTravel = (int) ((this.width * 0.64D) * squeeze);
            int dominantX = entering
                    ? -dominantWidth + dominantTravel
                    : this.width - dominantTravel;

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    dominantX,
                    -20,
                    dominantWidth,
                    this.height + 40
            );

            int timberWidth = Math.max(8, this.width / 54);
            int timberX = entering
                    ? dominantX + dominantWidth - timberWidth
                    : dominantX;

            drawScaledTexture(
                    graphics,
                    DARK_OAK,
                    timberX,
                    -12,
                    timberWidth,
                    this.height + 24
            );

            double late = smoothstep(Math.max(0.0D, (raw - 0.34D) / 0.58D));
            int oppositeWidth = lerpInt(
                    Math.max(4, this.width / 70),
                    Math.max(28, (int) (this.width * 0.32D)),
                    late
            );

            int oppositeX = entering
                    ? this.width - oppositeWidth
                    : 0;

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    oppositeX,
                    -18,
                    oppositeWidth,
                    this.height + 36
            );

            // A small top edge moves down as the head ducks/turns into the hole.
            int capHeight = lerpInt(
                    Math.max(3, this.height / 44),
                    Math.max(12, (int) (this.height * 0.13D)),
                    squeeze
            );

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    0,
                    -6,
                    this.width,
                    capHeight + 6
            );
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
