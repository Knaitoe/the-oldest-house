package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.labyrinth.DoorLeakKind;
import io.github.knaitoe.theoldesthouse.network.DoorLeaksPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/** Material and light seep toward the viewer. Deals remain private even when players share a door. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class DoorLeakRenderer {
    private DoorLeakRenderer() {}
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        var buffers = mc.renderBuffers().bufferSource();
        RenderType type = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer out = buffers.getBuffer(type);
        pose.pushPose(); pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = pose.last().pose();
        for (DoorLeaksPayload.Leak leak : DoorLeakClientState.leaks()) {
            BlockState door = mc.level.getBlockState(leak.door());
            if (!(door.getBlock() instanceof DoorBlock) || door.getValue(DoorBlock.OPEN)
                    || camera.distanceToSqr(Vec3.atCenterOf(leak.door())) > 144 || leak.kind() < 0 || leak.kind() >= DoorLeakKind.values().length) continue;
            Direction facing = Direction.from3DDataValue(leak.facing());
            DoorLeakKind kind = DoorLeakKind.values()[leak.kind()];
            TextureAtlasSprite sprite = sprite(kind);
            int light = LevelRenderer.getLightColor(mc.level, leak.door().relative(facing));
            if (kind == DoorLeakKind.WARM_TV || kind == DoorLeakKind.PHONE) {
                float flicker = .82F + .18F * (float) Math.sin(mc.level.getGameTime() * .31 + leak.door().getX());
                for (int i = 0; i < 4; i++) quad(out, matrix, leak.door(), facing, -.44F - i * .035F,
                        .44F + i * .035F, .18F + i * .28F, .46F + i * .28F, .014F,
                        sprite, 255, 177, 79, (int) ((92 - 20 * i) * flicker), LightTexture.FULL_BRIGHT);
            } else if (kind == DoorLeakKind.WATER || kind == DoorLeakKind.LAKE) {
                // Animated vanilla water atlas, in overlapping shallow patches rather than a square block.
                for (int i = 0; i < 4; i++) quad(out, matrix, leak.door(), facing, -.38F + .05F * i,
                        .36F - .03F * i, .18F + i * .27F, .53F + i * .27F, .009F + i * .001F,
                        sprite, 110, 157, 214, 175 - i * 28, light);
            } else {
                float length = kind == DoorLeakKind.HOTEL ? 1.65F : kind == DoorLeakKind.HEARTBEAT ? .85F : 1.2F;
                quad(out, matrix, leak.door(), facing, -.4F, .4F, .18F, length, .017F, sprite, 235, 235, 235, 230, light);
                // An irregular frayed edge makes fabric read as a creep through the door rather than a floor replacement.
                if (kind != DoorLeakKind.HEARTBEAT) for (int i = 0; i < 5; i++) {
                    float x = -.37F + i * .15F;
                    quad(out, matrix, leak.door(), facing, x, x + .07F, length, length + .12F + (i % 2) * .16F,
                            .018F, sprite, 235, 235, 235, 195, light);
                }
            }
        }
        pose.popPose(); buffers.endBatch(type);
    }
    @SubscribeEvent public static void particles(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.level.getGameTime() % 8 != 0) return;
        for (DoorLeaksPayload.Leak leak : DoorLeakClientState.leaks()) {
            if ((leak.kind() != DoorLeakKind.WATER.ordinal() && leak.kind() != DoorLeakKind.LAKE.ordinal())
                    || mc.player.position().distanceToSqr(Vec3.atCenterOf(leak.door())) > 64) continue;
            BlockState door = mc.level.getBlockState(leak.door());
            if (!(door.getBlock() instanceof DoorBlock) || door.getValue(DoorBlock.OPEN)) continue;
            Direction facing = Direction.from3DDataValue(leak.facing());
            Vec3 at = Vec3.atBottomCenterOf(leak.door()).add(facing.getStepX() * .35, .14, facing.getStepZ() * .35);
            mc.level.addParticle(ParticleTypes.DRIPPING_WATER, at.x, at.y, at.z, 0, 0, 0);
        }
    }
    private static TextureAtlasSprite sprite(DoorLeakKind kind) {
        Minecraft mc = Minecraft.getInstance();
        if (kind == DoorLeakKind.WATER || kind == DoorLeakKind.LAKE) return mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ResourceLocation.withDefaultNamespace("block/water_still"));
        BlockState state = switch (kind) {
            case HOTEL -> HouseBlocks.HOTEL_CARPET.get().defaultBlockState();
            case CLOTH -> Blocks.WHITE_WOOL.defaultBlockState();
            case MOTHER -> Blocks.GRAY_WOOL.defaultBlockState();
            case HEARTBEAT -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            default -> Blocks.WHITE_CONCRETE.defaultBlockState();
        };
        return mc.getBlockRenderer().getBlockModelShaper().getBlockModel(state).getParticleIcon();
    }
    private static void quad(VertexConsumer out, Matrix4f matrix, BlockPos door, Direction facing,
                             float u0, float u1, float v0, float v1, float height, TextureAtlasSprite sprite,
                             int r, int g, int b, int a, int light) {
        vertex(out, matrix, door, facing, u0, v0, height, sprite.getU0(), sprite.getV0(), r, g, b, a, light);
        vertex(out, matrix, door, facing, u1, v0, height, sprite.getU1(), sprite.getV0(), r, g, b, a, light);
        vertex(out, matrix, door, facing, u1, v1, height, sprite.getU1(), sprite.getV1(), r, g, b, a, light);
        vertex(out, matrix, door, facing, u0, v1, height, sprite.getU0(), sprite.getV1(), r, g, b, a, light);
    }
    private static void vertex(VertexConsumer out, Matrix4f matrix, BlockPos door, Direction facing, float u, float v,
                               float y, float s, float t, int r, int g, int b, int a, int light) {
        float x = door.getX() + .5F - facing.getStepZ() * u + facing.getStepX() * v;
        float z = door.getZ() + .5F + facing.getStepX() * u + facing.getStepZ() * v;
        out.addVertex(matrix, x, door.getY() + y, z).setColor(r, g, b, a).setUv(s, t)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }
}
