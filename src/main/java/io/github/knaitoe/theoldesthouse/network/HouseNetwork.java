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
                            // Store the presentation context before acknowledging it.
                            // The server will not change dimension until this ACK
                            // returns, so the transition screen cannot race the packet.
                            HouseTransitionContextState.set(payload.kind());
                            context.reply(
                                    new HouseTransitionContextAckPayload(payload.token())
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
