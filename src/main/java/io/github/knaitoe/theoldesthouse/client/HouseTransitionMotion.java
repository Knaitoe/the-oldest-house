package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseTransitionKind;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextAckPayload;
import io.github.knaitoe.theoldesthouse.network.HouseTransitionContextState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TheOldestHouse.MOD_ID, value = Dist.CLIENT)
public final class HouseTransitionMotion {
    private enum Phase {
        IDLE,
        PRE,
        HOLD,
        POST
    }

    private static final long HOLD_TIMEOUT_NANOS = 10_000_000_000L;

    private static Phase phase = Phase.IDLE;
    private static HouseTransitionKind kind = HouseTransitionKind.DOOR;
    private static int token = -1;
    private static boolean entering;
    private static float sideSign = -1.0F;
    private static long phaseStartedAt;
    private static long preDurationNanos;
    private static long postDurationNanos;

    private HouseTransitionMotion() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        int armedToken = HouseTransitionContextState.peekToken();

        if (HouseTransitionContextState.isArmed()
                && armedToken >= 0
                && armedToken != token) {
            begin(
                    HouseTransitionContextState.peekKind(),
                    armedToken
            );
        }

        if (phase == Phase.PRE) {
            if (System.nanoTime() - phaseStartedAt >= preDurationNanos) {
                PacketDistributor.sendToServer(
                        new HouseTransitionContextAckPayload(token)
                );
                phase = Phase.HOLD;
                phaseStartedAt = System.nanoTime();
            }
            return;
        }

        if (phase == Phase.HOLD
                && System.nanoTime() - phaseStartedAt > HOLD_TIMEOUT_NANOS) {
            HouseTransitionContextState.clear(token);
            reset();
            return;
        }

        if (phase == Phase.POST
                && System.nanoTime() - phaseStartedAt >= postDurationNanos) {
            HouseTransitionContextState.clear(token);
            reset();
        }
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float factor = motionFactor();
        if (factor <= 0.0F) {
            return;
        }

        switch (kind) {
            case DOOR -> {
                // The physical door itself already supplies the motion cue.
            }
            case WINDOW -> {
                event.setYaw(event.getYaw() + sideSign * 2.4F * factor);
                event.setPitch(event.getPitch() + 3.6F * factor);
                event.setRoll(event.getRoll() + sideSign * 4.5F * factor);
            }
            case BREACH -> {
                event.setYaw(event.getYaw() + sideSign * 5.8F * factor);
                event.setPitch(event.getPitch() + 1.4F * factor);
                event.setRoll(event.getRoll() + sideSign * 5.2F * factor);
            }
        }
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        float factor = motionFactor();
        if (factor <= 0.0F) {
            return;
        }

        float scale = switch (kind) {
            case DOOR -> 1.0F;
            case WINDOW -> 1.0F - 0.030F * factor;
            case BREACH -> 1.0F - 0.045F * factor;
        };

        event.setFOV(event.getFOV() * scale);
    }

    public static void beginPost(int transitionToken) {
        if (transitionToken != token) {
            HouseTransitionContextState.clear(transitionToken);
            return;
        }

        phase = Phase.POST;
        phaseStartedAt = System.nanoTime();
    }

    private static void begin(HouseTransitionKind newKind, int newToken) {
        Minecraft minecraft = Minecraft.getInstance();

        kind = newKind;
        token = newToken;
        entering = minecraft.level != null
                && minecraft.level.dimension().equals(Level.OVERWORLD);

        // Use a consistent shoulder for entry and the opposite direction on
        // exit. The displacement is small enough to read as body motion rather
        // than forced camera steering.
        sideSign = entering ? -1.0F : 1.0F;

        long preMillis = switch (kind) {
            case DOOR -> 0L;
            case WINDOW -> entering ? 360L : 250L;
            case BREACH -> entering ? 390L : 270L;
        };

        long postMillis = switch (kind) {
            case DOOR -> 0L;
            case WINDOW -> entering ? 260L : 190L;
            case BREACH -> entering ? 290L : 210L;
        };

        preDurationNanos = Math.max(1L, preMillis * 1_000_000L);
        postDurationNanos = Math.max(1L, postMillis * 1_000_000L);
        phase = Phase.PRE;
        phaseStartedAt = System.nanoTime();

        if (kind == HouseTransitionKind.DOOR) {
            PacketDistributor.sendToServer(
                    new HouseTransitionContextAckPayload(token)
            );
            phase = Phase.HOLD;
            phaseStartedAt = System.nanoTime();
        }
    }

    private static float motionFactor() {
        long now = System.nanoTime();

        return switch (phase) {
            case IDLE -> 0.0F;
            case PRE -> smoothstep(
                    (float) Math.min(
                            1.0D,
                            (double) (now - phaseStartedAt) / preDurationNanos
                    )
            );
            case HOLD -> kind == HouseTransitionKind.DOOR ? 0.0F : 1.0F;
            case POST -> {
                float t = (float) Math.min(
                        1.0D,
                        (double) (now - phaseStartedAt) / postDurationNanos
                );
                yield 1.0F - smoothstep(t);
            }
        };
    }

    private static float smoothstep(float value) {
        float t = Math.max(0.0F, Math.min(1.0F, value));
        return t * t * (3.0F - 2.0F * t);
    }

    private static void reset() {
        phase = Phase.IDLE;
        kind = HouseTransitionKind.DOOR;
        token = -1;
        entering = false;
        sideSign = -1.0F;
        phaseStartedAt = 0L;
        preDurationNanos = 1L;
        postDurationNanos = 1L;
    }
}
