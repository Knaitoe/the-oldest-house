package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * The backseat remembered in the manor (0.4.190): the server says where, in the black glass of a night window, the stowaway
 * sits up behind the reader's shoulder, and this draws the car's own body there, faintly lit, until the server takes it back
 * (the reader turned or stepped away) or its lease runs out. Nobody else is told, so nobody else sees it.
 */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class HouseRemembersClient {
    private static Vec3 at;private static float yaw;private static int lease;private static ClientLevel world;
    private HouseRemembersClient() {}

    public static void accept(CompoundTag tag) {
        var mc = Minecraft.getInstance();
        if (!tag.contains("Remembers") || mc.player == null || !tag.hasUUID("Reader") || !tag.getUUID("Reader").equals(mc.player.getUUID())) return;
        var remembers = tag.getCompound("Remembers");
        if (!remembers.contains("Stowaway")) { at = null;lease = 0;return; }
        var s = remembers.getCompound("Stowaway");
        at = new Vec3(s.getDouble("X"), s.getDouble("Y"), s.getDouble("Z"));yaw = s.getFloat("Yaw");lease = Math.max(0, Math.min(200, s.getInt("Lease")));world = mc.level;
    }
    /** Where the stowaway is drawn now, or null; for the client proof. */
    public static Vec3 shown() { return at != null && lease > 0 && Minecraft.getInstance().level == world ? at : null; }

    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        if (lease > 0 && --lease == 0) at = null;
        if (Minecraft.getInstance().level != world) { at = null;lease = 0; }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e) { at = null;lease = 0;world = null; }

    @SubscribeEvent public static void render(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || shown() == null) return;
        var mc = Minecraft.getInstance();if (mc.player == null) return;
        var camera = e.getCamera().getPosition();var pose = e.getPoseStack();var buffers = mc.renderBuffers().bufferSource();
        int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(at.add(0, 1, 0)));
        // Night glass: barely lit, as the back seat was by the dashboard.
        light = LightTexture.pack(Math.max(4, LightTexture.block(light)), LightTexture.sky(light));
        pose.pushPose();pose.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        BackseatMirror.body().sitting(pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();buffers.endBatch();
    }
}
