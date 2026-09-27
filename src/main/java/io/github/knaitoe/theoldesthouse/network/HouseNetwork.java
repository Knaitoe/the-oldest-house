package io.github.knaitoe.theoldesthouse.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class HouseNetwork {
    private HouseNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(
                        HouseTransitionContextPayload.TYPE,
                        HouseTransitionContextPayload.STREAM_CODEC,
                        (payload, context) -> HouseTransitionContextState.set(payload.kind())
                );
    }
}
