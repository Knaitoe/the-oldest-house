package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BlindfoldItem;
import io.github.knaitoe.theoldesthouse.labyrinth.ClapGameClock;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class ClapGameClientEvents {
    private static CameraType previousCamera;
    private static boolean previousHideGui;
    private ClapGameClientEvents() {}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        enforceView();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Pre event) {
        enforceView();
    }
    private static void enforceView() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            restore(mc);
            ClapGameClientState.clear();
            return;
        }
        if (ClapGameClientState.bound() || BlindfoldItem.isWorn(mc.player)) {
            if (previousCamera == null) {
                previousCamera = mc.options.getCameraType();
                previousHideGui = mc.options.hideGui;
            }
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            mc.options.hideGui = false;
        } else restore(mc);
    }
    private static void restore(Minecraft mc) {
        if (previousCamera != null) {
            mc.options.setCameraType(previousCamera);
            mc.options.hideGui = previousHideGui;
            previousCamera = null;
        }
    }
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (!ClapGameClientState.ending() || mc.player == null || mc.player.isDeadOrDying()) return;
        float progress = Mth.clamp((ClapGameClientState.elapsed() - ClapGameClock.TWIST_TICK)
                / (ClapGameClock.ENDING_TICKS - ClapGameClock.TWIST_TICK), 0, 1);
        progress = progress * progress * (3 - 2 * progress);
        float effects = mc.options.fovEffectScale().get().floatValue();
        event.setYaw(ClapGameClientState.yaw() + 45 * progress * effects);
        event.setPitch(32 + 18 * progress * effects);
        event.setRoll(75 * progress * effects);
    }
}
