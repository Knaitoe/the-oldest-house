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
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextState;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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

        // Doors into the labyrinth, from the impossible hallway or a test door, and back.
        for (var outside : java.util.List.of(Level.OVERWORLD, HouseDimensions.INTERIOR)) {
            event.registerConditionalEffect(
                    outside,
                    HouseDimensions.LABYRINTH,
                    (supplier, reason) -> createScreen(supplier, reason, false)
            );
            event.registerConditionalEffect(
                    HouseDimensions.LABYRINTH,
                    outside,
                    (supplier, reason) -> createScreen(supplier, reason, true)
            );
        }

        // The door to the room between rooms, and back out.
        event.registerConditionalEffect(
                HouseDimensions.INTERIOR,
                HouseDimensions.BETWEEN,
                (supplier, reason) -> createScreen(supplier, reason, false)
        );

        event.registerConditionalEffect(
                HouseDimensions.BETWEEN,
                HouseDimensions.INTERIOR,
                (supplier, reason) -> createScreen(supplier, reason, true)
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
        int token = HouseTransitionContextState.peekToken();
        return new StillFrameScreen(ready, reason, token, CapturedFrame.capture(token));
    }

    /**
     * The last frame before the switch, held still until the other side has
     * drawn in around the player. Nothing moves, darkens or swings: the
     * switch is made where the view is already static (a shut door), so a
     * held frame is a pause, not a cut. It lifts as soon as the sections
     * around the player are compiled, or after {@link #HOLD_LIMIT_NANOS}.
     */
    private static final class StillFrameScreen extends ReceivingLevelScreen {
        private static final long HOLD_LIMIT_NANOS = 1_500_000_000L;

        private final int transitionToken;
        private final CapturedFrame capturedFrame;

        private StillFrameScreen(BooleanSupplier ready, Reason reason, int token, CapturedFrame frame) {
            this(ready, reason, token, frame, System.nanoTime());
        }

        private StillFrameScreen(BooleanSupplier ready, Reason reason, int token, CapturedFrame frame, long openedAt) {
            super(() -> ready.getAsBoolean() && (surroundingsDrawn() || System.nanoTime() - openedAt > HOLD_LIMIT_NANOS), reason);
            this.transitionToken = token;
            this.capturedFrame = frame;
        }

        /** The player's section and its neighbours are meshed, so lifting the frame shows a finished view. */
        private static boolean surroundingsDrawn() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || minecraft.player == null) {
                return false;
            }
            BlockPos at = minecraft.player.blockPosition();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos section = at.offset(dx * 16, dy * 16, dz * 16);
                        if (minecraft.level.isOutsideBuildHeight(section.getY())) {
                            continue;
                        }
                        if (!minecraft.levelRenderer.isSectionCompiled(section)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (capturedFrame == null) {
                graphics.fill(0, 0, this.width, this.height, 0xFF0D0C0B);
                return;
            }
            capturedFrame.draw(graphics, this.width, this.height);
        }

        @Override
        public void removed() {
            super.removed();
            HouseTransitionMotion.beginPost(transitionToken);
            if (capturedFrame != null) {
                capturedFrame.close();
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
