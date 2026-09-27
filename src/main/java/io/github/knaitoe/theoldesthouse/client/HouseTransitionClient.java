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
            ResourceLocation.withDefaultNamespace("textures/block/spruce_door_top.png");
    private static final ResourceLocation DOOR_BOTTOM =
            ResourceLocation.withDefaultNamespace("textures/block/spruce_door_bottom.png");

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
        HouseTransitionKind kind = HouseTransitionContextState.consume();

        return switch (kind) {
            case DOOR -> new DoorPassageScreen(ready, reason, entering);
            case WINDOW -> new WindowPassageScreen(ready, reason, entering);
            case BREACH -> new BreachPassageScreen(ready, reason, entering);
        };
    }

    private abstract static class TimedPassageScreen extends ReceivingLevelScreen {
        protected final long openedAt;
        protected final long minimumVisibleNanos;
        protected final boolean entering;

        protected TimedPassageScreen(
                BooleanSupplier ready,
                Reason reason,
                boolean entering,
                long minMillis,
                long maxMillis
        ) {
            this(
                    ready,
                    reason,
                    entering,
                    System.nanoTime(),
                    randomDuration(minMillis, maxMillis)
            );
        }

        private TimedPassageScreen(
                BooleanSupplier ready,
                Reason reason,
                boolean entering,
                long openedAt,
                long minimumVisibleNanos
        ) {
            super(
                    () -> ready.getAsBoolean()
                            && System.nanoTime() - openedAt >= minimumVisibleNanos,
                    reason
            );
            this.openedAt = openedAt;
            this.minimumVisibleNanos = minimumVisibleNanos;
            this.entering = entering;
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

        private DoorPassageScreen(BooleanSupplier ready, Reason reason, boolean entering) {
            super(ready, reason, entering, 1500L, 1900L);
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

            graphics.blit(
                    DOOR_TOP,
                    doorX,
                    doorY,
                    0,
                    0.0F,
                    0.0F,
                    doorWidth,
                    half,
                    16,
                    16
            );
            graphics.blit(
                    DOOR_BOTTOM,
                    doorX,
                    doorY + half,
                    0,
                    0.0F,
                    0.0F,
                    doorWidth,
                    doorHeight - half,
                    16,
                    16
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
        private WindowPassageScreen(BooleanSupplier ready, Reason reason, boolean entering) {
            super(ready, reason, entering, 1350L, 1750L);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double p = smoothstep(progress());

            int outside = 0xFFE7EEF2;
            int inside = 0xFF171717;

            int from = entering ? outside : inside;
            int to = entering ? inside : outside;
            int mixed = mixColor(from, to, p);

            graphics.fill(0, 0, this.width, this.height, mixed);

            int bandAlpha = (int) (90.0D * (1.0D - Math.abs(0.5D - p) * 2.0D));
            int bandColor = (bandAlpha << 24) | 0x00DDEAF0;

            int bandWidth = Math.max(16, this.width / 24);
            int offset = (int) (p * (this.width + bandWidth * 3));
            for (int i = -2; i <= 2; i++) {
                int x = offset + i * bandWidth * 2 - bandWidth * 2;
                graphics.fill(
                        x,
                        0,
                        x + bandWidth,
                        this.height,
                        bandColor
                );
            }

            int vignetteAlpha = (int) (95.0D * Math.sin(p * Math.PI));
            graphics.fill(
                    0,
                    0,
                    this.width,
                    this.height,
                    (vignetteAlpha << 24)
            );
        }
    }

    private static final class BreachPassageScreen extends TimedPassageScreen {
        private BreachPassageScreen(BooleanSupplier ready, Reason reason, boolean entering) {
            super(ready, reason, entering, 1400L, 1800L);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            double p = smoothstep(progress());

            graphics.fill(0, 0, this.width, this.height, 0xFF151311);

            int aperture = lerpInt(
                    Math.max(this.width, this.height),
                    0,
                    Math.min(1.0D, p / 0.72D)
            );

            int left = (this.width - aperture) / 2;
            int right = (this.width + aperture) / 2;

            graphics.fill(0, 0, left, this.height, 0xFFE1DDD6);
            graphics.fill(right, 0, this.width, this.height, 0xFFE1DDD6);

            int dustAlpha = (int) (110.0D * Math.sin(p * Math.PI));
            int dustColor = (dustAlpha << 24) | 0x00C9C0B3;

            for (int i = 0; i < 7; i++) {
                int y = (i + 1) * this.height / 8;
                int drift = (int) ((p - 0.5D) * this.width * (0.05D + i * 0.008D));
                graphics.fill(
                        this.width / 4 + drift,
                        y,
                        this.width * 3 / 4 + drift,
                        y + Math.max(2, this.height / 90),
                        dustColor
                );
            }
        }
    }

    private static int mixColor(int a, int b, double amount) {
        double t = Math.max(0.0D, Math.min(1.0D, amount));

        int aa = (a >>> 24) & 0xFF;
        int ar = (a >>> 16) & 0xFF;
        int ag = (a >>> 8) & 0xFF;
        int ab = a & 0xFF;

        int ba = (b >>> 24) & 0xFF;
        int br = (b >>> 16) & 0xFF;
        int bg = (b >>> 8) & 0xFF;
        int bb = b & 0xFF;

        int ca = lerpInt(aa, ba, t);
        int cr = lerpInt(ar, br, t);
        int cg = lerpInt(ag, bg, t);
        int cb = lerpInt(ab, bb, t);

        return (ca << 24) | (cr << 16) | (cg << 8) | cb;
    }
}
