package io.github.knaitoe.theoldesthouse.network;

import io.github.knaitoe.theoldesthouse.client.HouseFadeState;
import io.github.knaitoe.theoldesthouse.client.HouseRoomDoorClient;
import io.github.knaitoe.theoldesthouse.client.HouseSightlineState;
import io.github.knaitoe.theoldesthouse.client.HotelPlaqueClientState;
import io.github.knaitoe.theoldesthouse.client.ClapGameClientState;
import io.github.knaitoe.theoldesthouse.client.DoorLeakClientState;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class HouseNetwork {
    private HouseNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("24")
                .playToClient(NovelScenePayload.TYPE,NovelScenePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.NovelSceneClient.accept(payload)))
                .playToClient(CaverCrawlPayload.TYPE,CaverCrawlPayload.STREAM_CODEC,
                        (payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.CaverCrawlClient.accept(payload)))
                .playToClient(GoatmanScenePayload.TYPE,GoatmanScenePayload.STREAM_CODEC,
                        (payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.GoatmanClient.accept(payload)))
                .playToClient(LakePhonePayload.TYPE,LakePhonePayload.STREAM_CODEC,
                        (payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.LakePhoneClient.accept(payload)))
                .playToClient(CompanionMenuPayload.TYPE, CompanionMenuPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> io.github.knaitoe.theoldesthouse.client.CompanionWheel.open(payload.entityId())))
                .playToServer(CompanionOrderPayload.TYPE, CompanionOrderPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player)
                                io.github.knaitoe.theoldesthouse.opening.CompanionOrders.command(player, payload.entityId(), payload.order());
                        }))
                .playToClient(DoorLeaksPayload.TYPE, DoorLeaksPayload.STREAM_CODEC,
                        (payload, context) -> DoorLeakClientState.accept(payload))
                .playToClient(ClapCuePayload.TYPE,ClapCuePayload.STREAM_CODEC,
                        (payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.ClapCueClient.accept(payload)))
                .playToClient(ClapGamePayload.TYPE, ClapGamePayload.STREAM_CODEC,
                        (payload, context) -> ClapGameClientState.accept(payload))
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
                )
                .playToClient(
                        HotelRoomNumbersPayload.TYPE,
                        HotelRoomNumbersPayload.STREAM_CODEC,
                        (payload, context) -> HotelPlaqueClientState.set(payload.base(), payload.laps())
                );
    }
}
