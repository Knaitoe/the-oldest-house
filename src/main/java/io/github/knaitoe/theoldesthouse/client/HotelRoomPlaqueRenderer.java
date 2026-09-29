package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HotelRoomPlaqueBlock;
import io.github.knaitoe.theoldesthouse.house.HotelRoomPlaqueBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws only the number. The brass plate itself is an ordinary block model,
 * so shader packs see it through the normal block pipeline.
 */
public final class HotelRoomPlaqueRenderer implements BlockEntityRenderer<HotelRoomPlaqueBlockEntity> {
    public static final ResourceLocation FONT =
            ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID, "hotel");
    private final Font font;

    public HotelRoomPlaqueRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(HotelRoomPlaqueBlockEntity plaque, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int number = HotelPlaqueClientState.numberAt(plaque.getBlockPos());
        if (number <= 0) {
            return;
        }

        Direction facing = plaque.getBlockState().getValue(HotelRoomPlaqueBlock.FACING);
        float rotation = switch (facing) {
            case WEST -> 90.0F;
            case NORTH -> 180.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };

        Component text = Component.literal(Integer.toString(number))
                .withStyle(style -> style.withFont(FONT));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        // The base model faces south and sits against the north wall edge.
        poseStack.translate(0.0D, 0.0D, -0.395D);
        poseStack.scale(-0.020F, -0.020F, 0.020F);

        float width = font.width(text);
        font.drawInBatch(
                text,
                -width / 2.0F,
                -4.5F,
                0x2A1608,
                false,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.POLYGON_OFFSET,
                0,
                packedLight
        );
        poseStack.popPose();
    }
}
