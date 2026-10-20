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
        int number = plaque.number() > 0 ? plaque.number() : HotelPlaqueClientState.numberAt(plaque.getBlockPos());
        if (number <= 0) {
            return;
        }

        Direction facing = plaque.getBlockState().getValue(HotelRoomPlaqueBlock.FACING);
        engrave(font,number,facing,poseStack,bufferSource,packedLight);
    }
    public static void engrave(Font font,int number,Direction facing,PoseStack poseStack,MultiBufferSource bufferSource,int packedLight){
        float rotation = switch (facing) {
            case WEST -> 270.0F;
            case NORTH -> 180.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };

        Component text = Component.literal(Integer.toString(number))
                .withStyle(style -> style.withFont(FONT));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        // The base model faces south and sits against the north wall edge.
        poseStack.translate(0.0D, 0.0D, -0.40525D);
        float scale=Math.min(.020F,.64F/font.width(text));
        poseStack.scale(scale,-scale,scale);

        float width = font.width(text);
        // The original bitmap glyphs are ink on the explicit brass plane.
        // Font's polygon-offset text layer can bury that plane at oblique angles.
        var texture=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/font/hotel.png");
        var out=bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(texture));
        float x=-width/2,top=-56/9F,glyphWidth=128/9F;
        for(char digit:Integer.toString(number).toCharArray()){
            int index=digit-33;float u0=(index%16)*16/256F,u1=u0+16/256F,v0=(index/16)*18/126F,v1=v0+18/126F;var p=poseStack.last();
            out.addVertex(p,x,top+16,0).setColor(0xff2a1608).setUv(u0,v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(p,0,0,1);
            out.addVertex(p,x+glyphWidth,top+16,0).setColor(0xff2a1608).setUv(u1,v1).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(p,0,0,1);
            out.addVertex(p,x+glyphWidth,top,0).setColor(0xff2a1608).setUv(u1,v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(p,0,0,1);
            out.addVertex(p,x,top,0).setColor(0xff2a1608).setUv(u0,v0).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(p,0,0,1);
            x+=font.width(Component.literal(Character.toString(digit)).withStyle(style->style.withFont(FONT)));
        }
        poseStack.popPose();
    }
}
