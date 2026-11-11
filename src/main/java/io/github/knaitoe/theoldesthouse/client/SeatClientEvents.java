package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SeatClientEvents {
    private SeatClientEvents() {}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HouseSitting.SEAT.get(), SwingRenderer::new);
    }
    /**
     * An ordinary seat draws nothing. 0.4.172: a swinging one draws the swing's seat and what it hangs by, turned about the crossbar
     * to where the reader is on the arc, in place of the still ones hidden while it is in use.
     */
    static final class SwingRenderer extends EntityRenderer<SeatEntity> {
        SwingRenderer(EntityRendererProvider.Context context) { super(context); }
        @Override public boolean shouldRender(SeatEntity seat, Frustum frustum, double x, double y, double z) { return seat.swinging() && seat.distanceToSqr(x, y, z) < 64 * 64; }
        @Override public void render(SeatEntity seat, float yaw, float partial, PoseStack pose, MultiBufferSource out, int light) {
            if (!seat.swinging()) return;
            var level = seat.level(); var pair = HouseReadSwing.pair(level, seat.support()); if (pair.isEmpty() || !HouseReadSwing.is(level.getBlockState(pair.getFirst()))) return;
            var pivot = HouseReadSwing.pivot(pair); var at = seat.getPosition(partial);
            // The angle the reader is at, so the drawn seat is exactly under them. 0.4.423: a swing hangs either way round; it turns
            // about its crossbar, the axis forward x up (the porch's, swinging north, turns about +x).
            var forward = HouseReadSwing.forward(level.getBlockState(pair.getFirst()));
            float angle = (float) Math.atan2((at.x - pivot.x) * forward.getStepX() + (at.z - pivot.z) * forward.getStepZ(), pivot.y - at.y);
            var blocks = Minecraft.getInstance().getBlockRenderer();
            pose.pushPose(); pose.translate(pivot.x - at.x, pivot.y - at.y, pivot.z - at.z);
            pose.mulPose(new org.joml.Quaternionf().rotateAxis(angle, -forward.getStepZ(), 0, forward.getStepX()));
            for (var pos : pair) for (int y = 0; y <= 3; y++) {
                // 0.4.465: whichever kind of swing it is says what it is drawn from (the porch's plank and ropes, the park's belt and chains).
                var state = y == 0 ? HouseReadSwing.still(level.getBlockState(pos)) : HouseReadSwing.hanger(level.getBlockState(pos));
                pose.pushPose(); pose.translate(pos.getX() - pivot.x, pos.getY() + y - pivot.y, pos.getZ() - pivot.z);
                blocks.renderSingleBlock(state, pose, out, light, OverlayTexture.NO_OVERLAY); pose.popPose();
            }
            pose.popPose();
        }
        @Override public ResourceLocation getTextureLocation(SeatEntity seat) { return InventoryMenu.BLOCK_ATLAS; }
    }
}
