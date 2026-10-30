package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadPropBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.ChainBlock;
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
     * An ordinary seat draws nothing. 0.4.172: a swinging one draws the porch swing's seat and ropes, turned about the crossbar to
     * where the reader is on the arc, in place of the still ones hidden while it is in use.
     */
    static final class SwingRenderer extends EntityRenderer<SeatEntity> {
        SwingRenderer(EntityRendererProvider.Context context) { super(context); }
        @Override public boolean shouldRender(SeatEntity seat, Frustum frustum, double x, double y, double z) { return seat.swinging() && seat.distanceToSqr(x, y, z) < 64 * 64; }
        @Override public void render(SeatEntity seat, float yaw, float partial, PoseStack pose, MultiBufferSource out, int light) {
            if (!seat.swinging()) return;
            var level = seat.level(); var pair = HouseReadSwing.pair(level, seat.support()); if (pair.isEmpty() || !HouseReadSwing.is(level.getBlockState(pair.getFirst()))) return;
            var pivot = HouseReadSwing.pivot(pair); var at = seat.getPosition(partial);
            // The angle the reader is at, so the drawn seat is exactly under them.
            float angle = (float) Math.atan2(pivot.z - at.z, pivot.y - at.y);
            var blocks = Minecraft.getInstance().getBlockRenderer();
            pose.pushPose(); pose.translate(pivot.x - at.x, pivot.y - at.y, pivot.z - at.z); pose.mulPose(Axis.XP.rotation(angle));
            for (var pos : pair) for (int y = 0; y <= 3; y++) {
                var state = y == 0 ? level.getBlockState(pos).setValue(HouseReadPropBlock.TURNED, false) : HouseReadRegistry.ROPE.get().defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y);
                pose.pushPose(); pose.translate(pos.getX() - pivot.x, pos.getY() + y - pivot.y, pos.getZ() - pivot.z);
                blocks.renderSingleBlock(state, pose, out, light, OverlayTexture.NO_OVERLAY); pose.popPose();
            }
            pose.popPose();
        }
        @Override public ResourceLocation getTextureLocation(SeatEntity seat) { return InventoryMenu.BLOCK_ATLAS; }
    }
}
