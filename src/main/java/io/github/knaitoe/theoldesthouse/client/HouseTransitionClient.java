package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionKind;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextState;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;

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

        return switch (kind) {
            case DOOR -> new DoorPassageScreen(ready, reason, entering, token);
            case WINDOW -> new WindowPassageScreen(ready, reason, entering, token);
            case BREACH -> new BreachPassageScreen(ready, reason, entering, token);
        };
    }

    private abstract static class TimedPassageScreen extends ReceivingLevelScreen {
        protected final long openedAt;
        protected final long minimumVisibleNanos;
        protected final boolean entering;
        protected final int transitionToken;

        protected TimedPassageScreen(
                BooleanSupplier ready,
                Reason reason,
                boolean entering,
                int transitionToken,
                long minMillis,
                long maxMillis
        ) {
            this(
                    ready,
                    reason,
                    entering,
                    transitionToken,
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
        }

        @Override
        public void removed() {
            super.removed();
            HouseTransitionContextState.clear(transitionToken);
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

        private DoorPassageScreen(BooleanSupplier ready, Reason reason, boolean entering, int token) {
            super(
                    ready,
                    reason,
                    entering,
                    token,
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
            double p = progress();
            double approach = smoothstep(Math.min(1.0D, p / 0.42D));

            graphics.fill(0, 0, this.width, this.height, 0xFF130C09);

            int startWidth = Math.max(160, (int) (this.width * 0.46D));
            int endWidth = Math.max(this.width + 120, (int) (this.width * 1.16D));
            int doorWidth = lerpInt(startWidth, endWidth, approach);
            int doorHeight = doorWidth * 2;

            int startX = entering
                    ? this.width - startWidth / 3
                    : -startWidth * 2 / 3;
            int endX = (this.width - doorWidth) / 2;
            int doorX = lerpInt(startX, endX, approach);
            int doorY = (this.height - doorHeight) / 2;
            int half = doorHeight / 2;

            drawScaledDoorHalf(
                    graphics,
                    DOOR_TOP,
                    doorX,
                    doorY,
                    doorWidth,
                    half
            );
            drawScaledDoorHalf(
                    graphics,
                    DOOR_BOTTOM,
                    doorX,
                    doorY + half,
                    doorWidth,
                    doorHeight - half
            );

            int jambWidth = Math.max(10, this.width / 64);
            int jambX = entering
                    ? doorX - jambWidth
                    : doorX + doorWidth;
            graphics.fill(
                    jambX,
                    0,
                    jambX + jambWidth,
                    this.height,
                    0xFF090604
            );

            int darkness = (int) (Math.min(1.0D, p / 0.72D) * 120.0D);
            graphics.fill(
                    0,
                    0,
                    this.width,
                    this.height,
                    (darkness << 24)
            );
        }
    }

    private static final class WindowPassageScreen extends TimedPassageScreen {
        private WindowPassageScreen(BooleanSupplier ready, Reason reason, boolean entering, int token) {
            super(
                    ready,
                    reason,
                    entering,
                    token,
                    entering ? 1350L : 900L,
                    entering ? 1700L : 1200L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double p = smoothstep(progress());

            // The player has already broken the pane if they can physically pass
            // through it. Do not invent magical glass effects. Instead, hold the
            // camera against the surrounding wall/sill while they squeeze through.
            graphics.fill(0, 0, this.width, this.height, 0xFF0E0D0C);

            double squeeze = entering
                    ? smoothstep(Math.min(1.0D, p / 0.62D))
                    : smoothstep(Math.min(1.0D, p / 0.48D));

            int sillStart = Math.max(18, (int) (this.height * 0.14D));
            int sillEnd = Math.max(
                    sillStart,
                    (int) (this.height * (entering ? 0.48D : 0.38D))
            );
            int sillHeight = lerpInt(sillStart, sillEnd, squeeze);

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    -16,
                    this.height - sillHeight,
                    this.width + 32,
                    sillHeight + 16
            );

            // One jamb passes close to the face. Which side is arbitrary enough
            // to vary with travel direction without implying a supernatural effect.
            int edgeStart = Math.max(24, (int) (this.width * 0.10D));
            int edgeEnd = Math.max(edgeStart, (int) (this.width * 0.42D));
            int edgeWidth = lerpInt(edgeStart, edgeEnd, squeeze);
            int edgeX = entering ? 0 : this.width - edgeWidth;

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    edgeX,
                    -16,
                    edgeWidth,
                    this.height + 32
            );

            // A narrow shadow at the inner edge gives the eye a physical corner
            // to track as the camera passes the opening.
            int shadowWidth = Math.max(4, this.width / 120);
            int shadowX = entering
                    ? edgeWidth - shadowWidth
                    : this.width - edgeWidth;
            graphics.fill(
                    shadowX,
                    0,
                    shadowX + shadowWidth,
                    this.height,
                    0xAA171310
            );
        }
    }

    private static final class BreachPassageScreen extends TimedPassageScreen {
        private BreachPassageScreen(BooleanSupplier ready, Reason reason, boolean entering, int token) {
            super(
                    ready,
                    reason,
                    entering,
                    token,
                    entering ? 1450L : 950L,
                    entering ? 1825L : 1275L
            );
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double p = smoothstep(progress());
            graphics.fill(0, 0, this.width, this.height, 0xFF0C0B0A);

            // A rough hole is represented by nearby real wall material, not by
            // an iris or portal effect. The passage tightens as the player's face
            // gets close to the plaster and opens again only when gameplay resumes.
            double squeeze = entering
                    ? smoothstep(Math.min(1.0D, p / 0.68D))
                    : smoothstep(Math.min(1.0D, p / 0.50D));

            int gapStart = Math.max(90, (int) (this.width * 0.56D));
            int gapEnd = Math.max(36, (int) (this.width * (entering ? 0.13D : 0.20D)));
            int gap = lerpInt(gapStart, gapEnd, squeeze);

            int center = this.width / 2
                    + (int) ((entering ? -1.0D : 1.0D) * this.width * 0.04D);
            int leftEdge = center - gap / 2;
            int rightEdge = center + gap / 2;

            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    -24,
                    -24,
                    leftEdge + 24,
                    this.height + 48
            );
            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    rightEdge,
                    -24,
                    this.width - rightEdge + 24,
                    this.height + 48
            );

            // One exposed timber edge keeps the breach tied to the authored
            // timber/plaster construction without making every hole identical.
            int timberWidth = Math.max(10, this.width / 44);
            int timberX = entering
                    ? leftEdge - timberWidth
                    : rightEdge;
            drawScaledTexture(
                    graphics,
                    DARK_OAK,
                    timberX,
                    -16,
                    timberWidth,
                    this.height + 32
            );

            // Slightly uneven top/bottom plaster makes the opening feel cut
            // through a wall rather than generated by a UI mask.
            int cap = lerpInt(
                    Math.max(8, this.height / 18),
                    Math.max(18, this.height / 5),
                    squeeze
            );
            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    leftEdge,
                    -8,
                    Math.max(1, rightEdge - leftEdge),
                    cap
            );
            drawScaledTexture(
                    graphics,
                    WHITE_TERRACOTTA,
                    leftEdge,
                    this.height - cap,
                    Math.max(1, rightEdge - leftEdge),
                    cap + 8
            );
        }
    }

    private static void drawScaledDoorHalf(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int width,
            int height
    ) {
        drawScaledTexture(graphics, texture, x, y, width, height);
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

        // Draw one native 16x16 quad and scale the pose around it. This keeps
        // every obstruction as one physical-looking surface rather than a tiled UI.
        graphics.blit(
                texture,
                0,
                0,
                0.0F,
                0.0F,
                16,
                16,
                16,
                16
        );
        graphics.pose().popPose();
    }

}
