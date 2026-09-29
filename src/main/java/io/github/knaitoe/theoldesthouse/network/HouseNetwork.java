package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.client.HouseFadeState;
import io.github.knaitoe.theoldesthouse.client.HouseRoomDoorClient;
import io.github.knaitoe.theoldesthouse.client.HouseSightlineState;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class HouseNetwork {
    private HouseNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("6")
                .playToClient(
                        HouseSightlineStatePayload.TYPE,
                        HouseSightlineStatePayload.STREAM_CODEC,
                        (payload, context) ->
                                HouseSightlineState.set(
                                        payload.origin(),
                                        payload.revealed()
                                )
                )
                .playToClient(
                        HouseFadePayload.TYPE,
                        HouseFadePayload.STREAM_CODEC,
                        (payload, context) -> HouseFadeState.begin(payload.fadeIn(), payload.hold(), payload.fadeOut())
                )
                .playToClient(
                        HouseSealedDoorsPayload.TYPE,
                        HouseSealedDoorsPayload.STREAM_CODEC,
                        (payload, context) -> HouseRoomDoorClient.setSealed(payload.doors())
                )
                .playToClient(
                        HouseRoomDoorPayload.TYPE,
                        HouseRoomDoorPayload.STREAM_CODEC,
                        (payload, context) -> HouseRoomDoorClient.set(payload.door(), payload.present())
                )
                .playToClient(
                        HouseTransitionContextPayload.TYPE,
                        HouseTransitionContextPayload.STREAM_CODEC,
                        (payload, context) -> HouseTransitionContextState.set(payload.kind(), payload.token())
                )
                // Nothing is acknowledged back: the context and the move that
                // follows it travel on one ordered connection and are handled
                // on the client's main thread in order, so the context is
                // always in place before the dimension switch arrives.
                .playToClient(
                        HouseTransitionCancelPayload.TYPE,
                        HouseTransitionCancelPayload.STREAM_CODEC,
                        (payload, context) -> HouseTransitionContextState.clear(payload.token())
                );
    }
}
