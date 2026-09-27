package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class HouseSightlineRenderer {
    private static final double MAX_VIEW_DISTANCE_SQUARED = 96.0D * 96.0D;

    private HouseSightlineRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || minecraft.player == null
                || !minecraft.level.dimension().equals(Level.OVERWORLD)
                || !HouseSightlineState.revealed()) {
            return;
        }

        BlockPos origin = HouseSightlineState.origin();
        Vec3 camera = event.getCamera().getPosition();

        double thresholdZ = origin.getZ() + 15.0D;
        if (camera.z >= thresholdZ - 0.10D) {
            return;
        }

        double centerX = origin.getX() + HouseBuilder.WIDTH / 2.0D + 0.5D;
        double centerY = origin.getY() + 2.0D;
        double centerZ = origin.getZ() + 15.0D;

        if (camera.distanceToSqr(centerX, centerY, centerZ) > MAX_VIEW_DISTANCE_SQUARED) {
            return;
        }

        // The portal is drawn immediately behind the real threshold doorway,
        // not sixty blocks into Overworld terrain. This prevents native trees,
        // hills or water behind the fixed exterior from depth-occluding the
        // impossible view.
        BlockPos thresholdDoor = origin.offset(HouseBuilder.WIDTH / 2, 1, 15);
        BlockState thresholdState = minecraft.level.getBlockState(thresholdDoor);
        if (thresholdState.getBlock() instanceof DoorBlock
                && !thresholdState.getValue(DoorBlock.OPEN)) {
            return;
        }

        renderThresholdView(event, origin, camera);
    }

    private static void renderThresholdView(
            RenderLevelStageEvent event,
            BlockPos origin,
            Vec3 camera
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RenderType renderType = RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer out = buffers.getBuffer(renderType);

        TextureAtlasSprite wall = particleSprite(Blocks.WHITE_TERRACOTTA.defaultBlockState());
        TextureAtlasSprite floor = particleSprite(Blocks.SPRUCE_PLANKS.defaultBlockState());
        TextureAtlasSprite ceiling = particleSprite(Blocks.SPRUCE_SLAB.defaultBlockState());
        TextureAtlasSprite carpet = particleSprite(Blocks.RED_CARPET.defaultBlockState());
        TextureAtlasSprite dark = particleSprite(Blocks.BLACK_CONCRETE.defaultBlockState());
        TextureAtlasSprite lamp = particleSprite(Blocks.LANTERN.defaultBlockState());

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = pose.last().pose();

        float x0 = origin.getX() + 7.03F;
        float x1 = origin.getX() + 7.97F;
        float y0 = origin.getY() + 1.03F;
        float y1 = origin.getY() + 2.97F;

        float farX0 = origin.getX() + 7.39F;
        float farX1 = origin.getX() + 7.61F;
        float farY0 = origin.getY() + 1.88F;
        float farY1 = origin.getY() + 2.12F;

        // Just behind the threshold plane, but still in front of any ordinary
        // Overworld terrain that physically exists behind the exterior wall.
        float zBase = origin.getZ() + 15.015F;

        quad(out, matrix, x0, y0, x1, y1, zBase + 0.003F, dark, 0.20F);

        // Perspective tunnel surfaces converge on the far rectangle.
        trapezoid(
                out, matrix,
                x0, y0, x0, y1,
                farX0, farY1, farX0, farY0,
                zBase, wall, 0.56F
        );
        trapezoid(
                out, matrix,
                x1, y1, x1, y0,
                farX1, farY0, farX1, farY1,
                zBase, wall, 0.52F
        );
        trapezoid(
                out, matrix,
                x0, y0, x1, y0,
                farX1, farY0, farX0, farY0,
                zBase - 0.001F, floor, 0.48F
        );
        trapezoid(
                out, matrix,
                x1, y1, x0, y1,
                farX0, farY1, farX1, farY1,
                zBase - 0.001F, ceiling, 0.37F
        );

        quad(
                out,
                matrix,
                farX0,
                farY0,
                farX1,
                farY1,
                zBase - 0.002F,
                wall,
                0.18F
        );

        // Narrow red runner tapering to the vanishing point.
        trapezoid(
                out,
                matrix,
                origin.getX() + 7.40F,
                y0 + 0.01F,
                origin.getX() + 7.60F,
                y0 + 0.01F,
                origin.getX() + 7.52F,
                farY0 + 0.005F,
                origin.getX() + 7.48F,
                farY0 + 0.005F,
                zBase - 0.004F,
                carpet,
                0.62F
        );

        // Diminishing warm markers suggest the real authored lantern rhythm.
        for (int i = 0; i < 4; i++) {
            float t = 0.28F + i * 0.17F;
            float cx = lerp(origin.getX() + 7.50F, origin.getX() + 7.50F, t);
            float cy = lerp(y1 - 0.34F, origin.getY() + 2.06F, t);
            float size = 0.095F * (1.0F - t) + 0.018F;

            quad(
                    out,
                    matrix,
                    cx - size,
                    cy - size,
                    cx + size,
                    cy + size,
                    zBase - 0.006F - i * 0.0002F,
                    lamp,
                    1.0F
            );
        }

        pose.popPose();
        buffers.endBatch(renderType);
    }

    private static TextureAtlasSprite particleSprite(BlockState state) {
        return Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModelShaper()
                .getBlockModel(state)
                .getParticleIcon();
    }

    private static void trapezoid(
            VertexConsumer out,
            Matrix4f matrix,
            float ax,
            float ay,
            float bx,
            float by,
            float cx,
            float cy,
            float dx,
            float dy,
            float z,
            TextureAtlasSprite sprite,
            float shade
    ) {
        texturedVertex(out, matrix, ax, ay, z, sprite.getU0(), sprite.getV1(), shade);
        texturedVertex(out, matrix, bx, by, z, sprite.getU0(), sprite.getV0(), shade);
        texturedVertex(out, matrix, cx, cy, z, sprite.getU1(), sprite.getV0(), shade);
        texturedVertex(out, matrix, dx, dy, z, sprite.getU1(), sprite.getV1(), shade);
    }

    private static void quad(
            VertexConsumer out,
            Matrix4f matrix,
            float x0,
            float y0,
            float x1,
            float y1,
            float z,
            TextureAtlasSprite sprite,
            float shade
    ) {
        texturedVertex(out, matrix, x0, y0, z, sprite.getU0(), sprite.getV1(), shade);
        texturedVertex(out, matrix, x1, y0, z, sprite.getU1(), sprite.getV1(), shade);
        texturedVertex(out, matrix, x1, y1, z, sprite.getU1(), sprite.getV0(), shade);
        texturedVertex(out, matrix, x0, y1, z, sprite.getU0(), sprite.getV0(), shade);
    }

    private static void texturedVertex(
            VertexConsumer out,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            float u,
            float v,
            float shade
    ) {
        int channel = Math.max(0, Math.min(255, Math.round(255.0F * shade)));
        out.addVertex(matrix, x, y, z)
                .setColor(channel, channel, channel, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 0.0F, -1.0F);
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}
