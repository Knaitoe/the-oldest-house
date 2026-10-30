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
        event.registrar("106")
                .playToClient(GroundingPayload.TYPE,GroundingPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.GroundingClient.accept(payload)))
                .playToServer(JournalTearPayload.TYPE,JournalTearPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p){if(payload.back())io.github.knaitoe.theoldesthouse.journal.TornPages.reclaim(p,payload.page());else io.github.knaitoe.theoldesthouse.journal.TornPages.tear(p,payload.page());}}))
                .playToClient(TornPagePayload.TYPE,TornPagePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.TornPageScreen.open(payload.writer(),payload.page())))
                .playToServer(JournalViewPayload.TYPE,JournalViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.journal.CoAuthor.viewing(p,payload.spread());}))
                .playToClient(JournalOpenPayload.TYPE,JournalOpenPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.JournalScreen.open(payload.journal())))
                .playToServer(JournalEditPayload.TYPE,JournalEditPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.journal.Journals.edit(p,payload.page(),payload.content());}))
                .playToClient(ZampanoFolderPayload.TYPE,ZampanoFolderPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.ZampanoScreen.open(payload.folder(),payload.leaf(),payload.pulled())))
                .playToServer(ZampanoDrawerActionPayload.TYPE,ZampanoDrawerActionPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.ZampanoDrawers.act(p,payload.leaf());}))
                .playToClient(DareStatePayload.TYPE,DareStatePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.DareClient.accept(payload)))
                .playToClient(HouseReadLinePayload.TYPE,HouseReadLinePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HouseReadLineScreen.show(payload)))
                .playToClient(HouseReadLookPayload.TYPE,HouseReadLookPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HouseReadLookClient.accept(payload)))
                .playToClient(HouseBodiesPayload.TYPE,HouseBodiesPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HouseBodyClient.accept(payload)))
                .playToServer(HouseBodyReportPayload.TYPE,HouseBodyReportPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.HouseBodies.report(p,payload.human());}))
                .playToClient(UncannyViewPayload.TYPE,UncannyViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.UncannyClient.accept(payload)))
                .playToServer(UncannyCameraPayload.TYPE,UncannyCameraPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.UncannySenses.camera(p,payload);}))
                .playToClient(HarriganPhonePayload.TYPE,HarriganPhonePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HarriganPhoneScreen.accept(payload)))
                .playToServer(HarriganPhoneActionPayload.TYPE,HarriganPhoneActionPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.HarriganVignette.phoneAction(p,payload.action(),payload.text());}))
                .playToServer(ShallowsSwingPayload.TYPE,ShallowsSwingPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.Shallows.swing(p,payload.side());}))
                .playToClient(MovieNightPayload.TYPE,MovieNightPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.MovieNightClient.accept(payload)))
                .playToClient(ShallowsCuePayload.TYPE,ShallowsCuePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.ShallowsClient.accept(payload)))
                .playToClient(BorrascaViewPayload.TYPE,BorrascaViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.BorrascaAtmosphere.accept(payload)))
                .playToServer(CanoePushPayload.TYPE,CanoePushPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p&&p.getVehicle() instanceof io.github.knaitoe.theoldesthouse.labyrinth.LakeHouseCanoe canoe)canoe.push(p,payload.side());}))
                .playToClient(HazardViewPayload.TYPE,HazardViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HazardClient.accept(payload)))
                .playToClient(EndingBookPayload.TYPE,EndingBookPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.EndingBookClient.accept(payload)))
                .playToClient(CamcorderExposurePayload.TYPE,CamcorderExposurePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.CamcorderClient.expose(payload)))
                .playToServer(CamcorderFramePayload.TYPE,CamcorderFramePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.GhostsSet.receiveFrame(p,payload.nonce(),payload.pixels());}))
                .playToClient(PlainExposurePayload.TYPE,PlainExposurePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PlainCameraClient.expose(payload)))
                .playToServer(PlainFramePayload.TYPE,PlainFramePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.NovelVignettes.capturePlainFrame(p,payload);}))
                .playToClient(BodyLossPayload.TYPE,BodyLossPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.labyrinth.BodyLoss.clientSet(payload.player(),payload.arm())))
                .playToClient(CabinStormPayload.TYPE,CabinStormPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.CabinStormClient.accept(payload)))
                .playToClient(StaircaseLeakPayload.TYPE,StaircaseLeakPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.StaircaseLeakClient.accept(payload)))
                .playToClient(BurnEmbersPayload.TYPE,BurnEmbersPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.BurnEmbersClient.accept(payload)))
                .playToClient(LakePartnerPayload.TYPE,LakePartnerPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.LakePartnerLook.accept(payload)))
                .playToClient(LakeHouseViewPayload.TYPE,LakeHouseViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.LakeHouseAtmosphere.accept(payload)))
                .playToClient(LiteraryViewPayload.TYPE,LiteraryViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.LiteraryView.accept(payload)))
                .playToClient(PawClutchPayload.TYPE,PawClutchPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PawClutchClient.accept(payload)))
                .playToClient(CampBedPayload.TYPE,CampBedPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.CampBedClient.accept(payload)))
                .playToClient(HotelAtmospherePayload.TYPE,HotelAtmospherePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HotelAtmosphere.accept(payload)))
                .playToClient(SeanceViewPayload.TYPE,SeanceViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.SeanceView.accept(payload)))
                .playToClient(StaircaseLightPayload.TYPE,StaircaseLightPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.StaircaseDarkness.accept(payload)))
                .playToClient(PettingPayload.TYPE,PettingPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.CompanionAnimation.accept(payload)))
                .playToClient(HomeEchoPayload.TYPE,HomeEchoPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HomeEchoClient.accept(payload)))
                .playToClient(PersonalProjectionPayload.TYPE,PersonalProjectionPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PersonalProjectionClient.accept(payload)))
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
