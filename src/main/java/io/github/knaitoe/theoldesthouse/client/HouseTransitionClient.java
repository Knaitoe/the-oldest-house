package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation DOOR_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/spruce_door_bottom.png");

    private HouseTransitionClient() {
    }

    @SubscribeEvent
    public static void registerTransitionScreens(RegisterDimensionTransitionScreenEvent event) {
        event.registerConditionalEffect(
                HouseDimensions.INTERIOR,
                Level.OVERWORLD,
                (supplier, reason) -> new DoorFocusScreen(
                        supplier,
                        reason,
                        Component.literal("The latch catches.")
                )
        );

        event.registerConditionalEffect(
                Level.OVERWORLD,
                HouseDimensions.INTERIOR,
                (supplier, reason) -> new DoorFocusScreen(
                        supplier,
                        reason,
                        Component.literal("The door settles behind you.")
                )
        );
    }

    private static final class DoorFocusScreen extends ReceivingLevelScreen {
        private final Component message;

        private DoorFocusScreen(BooleanSupplier supplier, Reason reason, Component message) {
            super(supplier, reason);
            this.message = message;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
            graphics.drawCenteredString(
                    this.font,
                    this.message,
                    this.width / 2,
                    this.height - 36,
                    0xFFE7DED1
            );
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, this.width, this.height, 0xFF24170F);
            graphics.blit(
                    DOOR_TEXTURE,
                    0,
                    0,
                    0,
                    0.0F,
                    0.0F,
                    this.width,
                    this.height,
                    16,
                    16
            );
            graphics.fill(0, 0, this.width, this.height, 0x55000000);
        }
    }
}
