package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBuilder;
import io.github.knaitoe.theoldesthouse.house.HouseImpossibleHallway;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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

        // Never expose the visual corridor from behind the fixed exterior.
        if (camera.z >= thresholdZ - 0.10D) {
            return;
        }

        double centerX = origin.getX() + HouseBuilder.WIDTH / 2.0D + 0.5D;
        double centerY = origin.getY() + 2.25D;
        double centerZ = origin.getZ() + 14.72D;

        if (camera.distanceToSqr(centerX, centerY, centerZ) > MAX_VIEW_DISTANCE_SQUARED) {
            return;
        }

        // The physical threshold door must actually be open before a corridor
        // beyond it can be seen.
        BlockPos thresholdDoor = origin.offset(HouseBuilder.WIDTH / 2, 1, 15);
        BlockState thresholdState = minecraft.level.getBlockState(thresholdDoor);
        if (!(thresholdState.getBlock() instanceof DoorBlock)
                || !thresholdState.getValue(DoorBlock.OPEN)) {
            return;
        }

        Vec3 target = new Vec3(centerX, centerY, centerZ);
        BlockHitResult obstruction = minecraft.level.clip(
                new ClipContext(
                        camera,
                        target,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        minecraft.player
                )
        );

        if (obstruction.getType() != HitResult.Type.MISS
                && obstruction.getLocation().distanceToSqr(target) > 0.16D) {
            return;
        }

        renderCorridor(event, origin, camera);
    }

    private static void renderCorridor(
            RenderLevelStageEvent event,
            BlockPos origin,
            Vec3 camera
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.cutout());

        TextureAtlasSprite wall = particleSprite(Blocks.WHITE_TERRACOTTA.defaultBlockState());
        TextureAtlasSprite floor = particleSprite(Blocks.SPRUCE_PLANKS.defaultBlockState());
        TextureAtlasSprite ceiling = particleSprite(Blocks.SPRUCE_SLAB.defaultBlockState());
        TextureAtlasSprite carpet = particleSprite(Blocks.RED_CARPET.defaultBlockState());

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = pose.last().pose();

        int startZ = HouseImpossibleHallway.START_Z_OFFSET;
        int endZ = HouseImpossibleHallway.END_Z_OFFSET;

        for (int relZ = startZ; relZ <= endZ; relZ++) {
            float depth = (relZ - startZ) / (float) Math.max(1, endZ - startZ);
            float brightness = 0.78F - depth * 0.50F;

            float z0 = origin.getZ() + relZ;
            float z1 = z0 + 1.0F;

            for (int relX = 6; relX <= 8; relX++) {
                float x0 = origin.getX() + relX;
                float x1 = x0 + 1.0F;

                quadUp(
                        consumer,
                        matrix,
                        x0,
                        origin.getY() + 1.002F,
                        z0,
                        x1,
                        z1,
                        floor,
                        brightness
                );

                quadDown(
                        consumer,
                        matrix,
                        x0,
                        origin.getY() + 5.0F,
                        z0,
                        x1,
                        z1,
                        ceiling,
                        brightness * 0.82F
                );
            }

            for (int relY = 1; relY <= 4; relY++) {
                float y0 = origin.getY() + relY;
                float y1 = y0 + 1.0F;

                quadEast(
                        consumer,
                        matrix,
                        origin.getX() + 6.001F,
                        y0,
                        y1,
                        z0,
                        z1,
                        wall,
                        brightness
                );

                quadWest(
                        consumer,
                        matrix,
                        origin.getX() + 8.999F,
                        y0,
                        y1,
                        z0,
                        z1,
                        wall,
                        brightness
                );
            }

            // Thin runner down the center. Slightly above the floor prevents
            // z-fighting without making it look detached.
            quadUp(
                    consumer,
                    matrix,
                    origin.getX() + 7.0F,
                    origin.getY() + 1.012F,
                    z0,
                    origin.getX() + 8.0F,
                    z1,
                    carpet,
                    Math.min(0.72F, brightness + 0.08F)
            );
        }

        // A real terminal wall is important: the view should read as extremely
        // long, not as an infinite debug tunnel.
        float terminalZ = origin.getZ() + endZ + 0.001F;
        for (int relX = 6; relX <= 8; relX++) {
            for (int relY = 1; relY <= 4; relY++) {
                quadNorth(
                        consumer,
                        matrix,
                        origin.getX() + relX,
                        origin.getY() + relY,
                        terminalZ,
                        origin.getX() + relX + 1.0F,
                        origin.getY() + relY + 1.0F,
                        wall,
                        0.22F
                );
            }
        }

        pose.popPose();
        buffers.endBatch(RenderType.cutout());

        // Render just the authored lights as actual block models after the
        // efficient corridor surfaces. Four lanterns are cheap and give the
        // stretch readable depth markers.
        renderDepthLights(event, origin, camera, buffers);
    }

    private static void renderDepthLights(
            RenderLevelStageEvent event,
            BlockPos origin,
            Vec3 camera,
            MultiBufferSource.BufferSource buffers
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack pose = event.getPoseStack();

        for (int relZ : new int[]{24, 36, 48, 60}) {
            pose.pushPose();
            pose.translate(
                    origin.getX() + HouseBuilder.WIDTH / 2.0D - camera.x,
                    origin.getY() + 3.0D - camera.y,
                    origin.getZ() + relZ - camera.z
            );

            minecraft.getBlockRenderer().renderSingleBlock(
                    Blocks.LANTERN.defaultBlockState(),
                    pose,
                    buffers,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY
            );
            pose.popPose();
        }

        buffers.endBatch();
    }

    private static TextureAtlasSprite particleSprite(BlockState state) {
        return Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModelShaper()
                .getBlockModel(state)
                .getParticleIcon();
    }

    private static void quadUp(
            VertexConsumer out,
            Matrix4f matrix,
            float x0,
            float y,
            float z0,
            float x1,
            float z1,
            TextureAtlasSprite sprite,
            float shade
    ) {
        vertex(out, matrix, x0, y, z1, sprite.getU0(), sprite.getV1(), shade, 0, 1, 0);
        vertex(out, matrix, x1, y, z1, sprite.getU1(), sprite.getV1(), shade, 0, 1, 0);
        vertex(out, matrix, x1, y, z0, sprite.getU1(), sprite.getV0(), shade, 0, 1, 0);
        vertex(out, matrix, x0, y, z0, sprite.getU0(), sprite.getV0(), shade, 0, 1, 0);
    }

    private static void quadDown(
            VertexConsumer out,
            Matrix4f matrix,
            float x0,
            float y,
            float z0,
            float x1,
            float z1,
            TextureAtlasSprite sprite,
            float shade
    ) {
        vertex(out, matrix, x0, y, z0, sprite.getU0(), sprite.getV0(), shade, 0, -1, 0);
        vertex(out, matrix, x1, y, z0, sprite.getU1(), sprite.getV0(), shade, 0, -1, 0);
        vertex(out, matrix, x1, y, z1, sprite.getU1(), sprite.getV1(), shade, 0, -1, 0);
        vertex(out, matrix, x0, y, z1, sprite.getU0(), sprite.getV1(), shade, 0, -1, 0);
    }

    private static void quadEast(
            VertexConsumer out,
            Matrix4f matrix,
            float x,
            float y0,
            float y1,
            float z0,
            float z1,
            TextureAtlasSprite sprite,
            float shade
    ) {
        vertex(out, matrix, x, y0, z0, sprite.getU0(), sprite.getV1(), shade, 1, 0, 0);
        vertex(out, matrix, x, y0, z1, sprite.getU1(), sprite.getV1(), shade, 1, 0, 0);
        vertex(out, matrix, x, y1, z1, sprite.getU1(), sprite.getV0(), shade, 1, 0, 0);
        vertex(out, matrix, x, y1, z0, sprite.getU0(), sprite.getV0(), shade, 1, 0, 0);
    }

    private static void quadWest(
            VertexConsumer out,
            Matrix4f matrix,
            float x,
            float y0,
            float y1,
            float z0,
            float z1,
            TextureAtlasSprite sprite,
            float shade
    ) {
        vertex(out, matrix, x, y0, z1, sprite.getU0(), sprite.getV1(), shade, -1, 0, 0);
        vertex(out, matrix, x, y0, z0, sprite.getU1(), sprite.getV1(), shade, -1, 0, 0);
        vertex(out, matrix, x, y1, z0, sprite.getU1(), sprite.getV0(), shade, -1, 0, 0);
        vertex(out, matrix, x, y1, z1, sprite.getU0(), sprite.getV0(), shade, -1, 0, 0);
    }

    private static void quadNorth(
            VertexConsumer out,
            Matrix4f matrix,
            float x0,
            float y0,
            float z,
            float x1,
            float y1,
            TextureAtlasSprite sprite,
            float shade
    ) {
        vertex(out, matrix, x1, y0, z, sprite.getU1(), sprite.getV1(), shade, 0, 0, -1);
        vertex(out, matrix, x0, y0, z, sprite.getU0(), sprite.getV1(), shade, 0, 0, -1);
        vertex(out, matrix, x0, y1, z, sprite.getU0(), sprite.getV0(), shade, 0, 0, -1);
        vertex(out, matrix, x1, y1, z, sprite.getU1(), sprite.getV0(), shade, 0, 0, -1);
    }

    private static void vertex(
            VertexConsumer out,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            float u,
            float v,
            float shade,
            float nx,
            float ny,
            float nz
    ) {
        int channel = Math.max(0, Math.min(255, Math.round(255.0F * shade)));
        out.addVertex(matrix, x, y, z)
                .setColor(channel, channel, channel, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(nx, ny, nz);
    }
}
