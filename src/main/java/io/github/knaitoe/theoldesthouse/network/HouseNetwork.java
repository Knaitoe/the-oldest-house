package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class HouseNetwork {
    private HouseNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("2")
                .playToClient(
                        HouseTransitionContextPayload.TYPE,
                        HouseTransitionContextPayload.STREAM_CODEC,
                        (payload, context) -> {
                            // Store the presentation context, but do not ACK yet.
                            // The client performs a short live-camera movement first;
                            // HouseTransitionMotion sends the ACK when that movement
                            // reaches its handoff point.
                            HouseTransitionContextState.set(
                                    payload.kind(),
                                    payload.token()
                            );
                        }
                )
                .playToServer(
                        HouseTransitionContextAckPayload.TYPE,
                        HouseTransitionContextAckPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            if (context.player() instanceof ServerPlayer player) {
                                HouseTransitionEvents.acknowledgeContext(
                                        player,
                                        payload.token()
                                );
                            }
                        })
                );
    }
}
