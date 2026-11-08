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
        event.registrar("163")
                .playToClient(GhostsLorePayload.TYPE,GhostsLorePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.GhostsLoreScreen.open(payload)))
                .playToClient(YardCrawlPayload.TYPE,YardCrawlPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.YardCrawlClient.accept(payload)))
                .playToServer(CompanionNearbyPayload.TYPE,CompanionNearbyPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.opening.CompanionOrders.openNearest(p);}))
                .playToServer(JournalRibbonPayload.TYPE,JournalRibbonPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.journal.Journals.ribbon(p,payload.page());}))
                .playToClient(SourceMarkPayload.TYPE,SourceMarkPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.SourceMarkClient.accept(payload)))
                .playToClient(SecondPersonPayload.TYPE,SecondPersonPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.SecondPersonClient.accept(payload)))
                .playToClient(CrossingPayload.TYPE,CrossingPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.CrossingClient.accept(payload)))
                .playToClient(KeptStatePayload.TYPE,KeptStatePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.KeptClient.accept(payload)))
                .playToClient(PolybiusBrawlPayload.TYPE,PolybiusBrawlPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PolybiusBrawlScreen.accept(payload)))
                .playToServer(PolybiusBrawlKeysPayload.TYPE,PolybiusBrawlKeysPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.PolybiusBrawl.keys(p,payload.keys());}))
                .playToClient(PolybiusPayload.TYPE,PolybiusPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PolybiusScreen.accept(payload)))
                .playToServer(PolybiusScorePayload.TYPE,PolybiusScorePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.Polybius.submit(p,payload.token(),payload.runs());}))
                .playToClient(LateBodiesPayload.TYPE,LateBodiesPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.LateBodyClient.accept(payload)))
                .playToClient(PittPayload.TYPE,PittPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PittClient.set(payload)))
                .playToClient(DeadAirPayload.TYPE,DeadAirPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.DeadAirClient.set(payload)))
                .playToClient(LeftRightFogPayload.TYPE,LeftRightFogPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.LeftRightClient.fog(payload)))
                .playToClient(PolaroidFlashPayload.TYPE,PolaroidFlashPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PolaroidClient.flash(payload)))
                .playToClient(PolaroidViewPayload.TYPE,PolaroidViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.PolaroidClient.view(payload)))
                .playToClient(WorkPrintFilmPayload.TYPE,WorkPrintFilmPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.WorkPrintClient.film(payload)))
                .playToClient(WorkPrintHauntPayload.TYPE,WorkPrintHauntPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.WorkPrintClient.haunt(payload)))
                .playToClient(WayOutPayload.TYPE,WayOutPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.WayOutClient.accept(payload)))
                .playToClient(GroundingPayload.TYPE,GroundingPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.GroundingClient.accept(payload)))
                .playToServer(JournalTearPayload.TYPE,JournalTearPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p){if(payload.back())io.github.knaitoe.theoldesthouse.journal.TornPages.reclaim(p,payload.page());else io.github.knaitoe.theoldesthouse.journal.TornPages.tear(p,payload.page());}}))
                .playToClient(TornPagePayload.TYPE,TornPagePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.TornPageScreen.open(payload.writer(),payload.page())))
                .playToServer(JournalViewPayload.TYPE,JournalViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.journal.CoAuthor.viewing(p,payload.spread());}))
                .playToClient(JournalOpenPayload.TYPE,JournalOpenPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.JournalScreen.open(payload.journal())))
                .playToServer(JournalEditPayload.TYPE,JournalEditPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.journal.Journals.edit(p,payload.page(),payload.content());}))
                .playToClient(ZampanoFolderPayload.TYPE,ZampanoFolderPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.ZampanoScreen.open(payload.folder(),payload.leaf(),payload.pulled())))
                .playToServer(ZampanoDrawerActionPayload.TYPE,ZampanoDrawerActionPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.ZampanoDrawers.act(p,payload.leaf());}))
                .playToClient(DareStatePayload.TYPE,DareStatePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.DareClient.accept(payload)))
                .playToServer(HouseReadCardActionPayload.TYPE,HouseReadCardActionPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{
                    if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)
                        io.github.knaitoe.theoldesthouse.labyrinth.HouseReadStory.cardAction(p,payload.token(),payload.finished());
                }))
                .playToClient(HouseReadLinePayload.TYPE,HouseReadLinePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HouseReadLineScreen.show(payload)))
                .playToClient(HouseReadLookPayload.TYPE,HouseReadLookPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HouseReadLookClient.accept(payload)))
                .playToClient(HouseBodiesPayload.TYPE,HouseBodiesPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.HouseBodyClient.accept(payload)))
                .playToClient(UncannyViewPayload.TYPE,UncannyViewPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.UncannyClient.accept(payload)))
                .playToServer(UncannyCameraPayload.TYPE,UncannyCameraPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.UncannySenses.camera(p,payload);}))
                .playToClient(TalkPayload.TYPE,TalkPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.TalkScreen.accept(payload)))
                .playToClient(SpokenPayload.TYPE,SpokenPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.SpokenClient.accept(payload)))
                .playToClient(SphinxVoicePayload.TYPE,SphinxVoicePayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.SphinxMouth.accept(payload)))
                .playToClient(SphinxDreamPayload.TYPE,SphinxDreamPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.SphinxDreamClient.accept(payload)))
                .playToClient(StateRouteWreckPayload.TYPE,StateRouteWreckPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.StateRouteClient.wreck(payload)))
                .playToClient(TalkHistoryPayload.TYPE,TalkHistoryPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->io.github.knaitoe.theoldesthouse.client.JournalScreen.heard(payload)))
                .playToServer(TalkHistoryRequestPayload.TYPE,TalkHistoryRequestPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p&&io.github.knaitoe.theoldesthouse.journal.Journals.get(p.server).carries(p))io.github.knaitoe.theoldesthouse.labyrinth.TalkHistory.send(p);}))
                .playToServer(TalkReplyPayload.TYPE,TalkReplyPayload.STREAM_CODEC,(payload,context)->context.enqueueWork(()->{if(context.player() instanceof net.minecraft.server.level.ServerPlayer p)io.github.knaitoe.theoldesthouse.labyrinth.Talk.reply(p,payload.token(),payload.reply());}))
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
                        (payload, context) -> context.enqueueWork(() -> io.github.knaitoe.theoldesthouse.client.CompanionWheel.open(payload.entityId(),payload.order())))
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
