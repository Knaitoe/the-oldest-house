package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.GhostsLorePayload;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** Real block inputs and owned lens use; reading, recording and completion remain different things. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_ghosts") @PrefixGameTestTemplate(false)
public final class GhostsLoreTests {
    private static final LabyrinthPlace P=GhostsSet.PLACE;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static void close(){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="ghosts_lore_papers321") public static void papersDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_lore_lens321") public static void lensDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_lore_aftermath321") public static void aftermathDone(ServerLevel l){close();}
    private static void save(LiteraryTests.Fixture f,ServerPlayer p,CompoundTag own){LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}
    private static boolean resident(LiteraryTests.Fixture f,ServerPlayer p){return f.chunks.ready()&&f.own(p,P).getBoolean("CastPlaced");}
    private static void collect(Packet<?> packet,List<GhostsLorePayload> out){
        if(packet instanceof ClientboundCustomPayloadPacket custom&&custom.payload() instanceof GhostsLorePayload lore)out.add(lore);
        if(packet instanceof BundlePacket<?> bundle)for(var child:bundle.subPackets())collect(child,out);
    }
    private static List<GhostsLorePayload> papers(ServerPlayer p){var out=new ArrayList<GhostsLorePayload>();for(var packet:NativeTestPlayers.packets(p))collect(packet,out);return out;}
    private static ItemStack camera(GameTestHelper h,ServerPlayer p){
        for(int slot=0;slot<p.getInventory().items.size();slot++)if(p.getInventory().items.get(slot).is(LiteraryRegistry.CAMCORDER.get()))return p.getInventory().removeItemNoUpdate(slot);
        h.fail("the filming actually issued the reader's owned camera");return ItemStack.EMPTY;
    }
    private static void toggle(ServerPlayer p,ItemStack camera){camera.use(p.serverLevel(),p,InteractionHand.MAIN_HAND);p.releaseUsingItem();}

    @GameTest(template="empty",batch="ghosts_lore_papers321",timeoutTicks=1800)
    public static void nativePapersArePrivateAndTheLateLaptopDoesNotCompleteAFilmingDay(GameTestHelper h){
        var f=fixture(h);var p=f.player("lore_reader",P);var peer=f.player("lore_friend",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));int[] days={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the new set and original cast are resident"))
            .thenExecute(()->{
                papers(p);papers(peer);days[0]=f.own(p,P).getInt("FilmedDays");
                f.click(p,P,GhostsLore.DIARY_AT);f.click(p,P,GhostsLore.BANK_AT);f.click(p,P,GhostsLore.DRAWING_AT);f.click(p,P,GhostsLore.LAPTOP_AT);
                h.assertTrue(GhostsLore.found(f.own(p,P))==3,"the three actual paper inputs persist their discoveries");
            }).thenIdle(3).thenExecute(()->{
                var early=papers(p);h.assertTrue(early.size()==3&&early.stream().allMatch(a->a.day()==1)&&early.stream().noneMatch(a->a.kind()==GhostsLore.LAPTOP),"actual early paper inputs open three personal views, while the laptop is closed: "+early);
                h.assertTrue(papers(peer).isEmpty()&&GhostsLore.found(f.own(peer,P))==0,"the friend gets no packet or discoveries from those clicks");
                h.assertTrue(f.own(p,P).getInt("FilmedDays")==days[0]&&f.own(p,P).getCompound("FilmLog1").isEmpty()&&WitnessAccount.count(f.data(),p.getUUID())==0,"examination is neither filming nor completion");
                h.assertTrue(GhostsLore.pages(GhostsLore.DIARY,1).size()==1&&GhostsLore.pages(GhostsLore.DIARY,6).size()==5,"the private diary accumulates entries and the folded contact slip");
                h.assertTrue(GhostsSet.jumpToDay(p,7,false)==null,"the real shortcut selects the late day without filming earlier days");
                f.click(p,P,GhostsLore.DIARY_AT);f.click(p,P,GhostsLore.LAPTOP_AT);f.click(p,P,GhostsLore.DRAWING_AT);
                f.click(peer,P,GhostsLore.LAPTOP_AT);
            }).thenIdle(3).thenExecute(()->{
                var late=papers(p);h.assertTrue(late.size()==3&&late.stream().allMatch(a->a.day()==7)&&late.stream().anyMatch(a->a.kind()==GhostsLore.LAPTOP),"the late reader opens the study and updated family papers");
                h.assertTrue(papers(peer).isEmpty()&&!f.own(peer,P).getBoolean("LoreLaptop"),"an early-day friend cannot open the late reader's laptop research");
                h.assertTrue(GhostsLore.laptop(1)==GhostsSetBlock.Kind.LAPTOP_CLOSED&&GhostsLore.laptop(6)==null&&GhostsLore.laptop(7)==GhostsSetBlock.Kind.LAPTOP_STUDY,"the per-reader laptop is removed for the rite and open only afterward");
                h.assertTrue(f.in.getBlockState(b.offset(GhostsLore.LAPTOP_AT)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.LAPTOP_CLOSED,"different personal days never replace the shared non-colliding anchor");
                f.reload();h.assertTrue(f.own(p,P).getInt("LoreDiaryDay")==7&&f.own(p,P).getBoolean("LoreLaptop")&&f.own(p,P).getInt("LoreDrawingDay")==7&&f.own(p,P).getBoolean("LoreBank"),"all four personal discoveries survive actual SavedData reload");
                h.assertTrue(f.own(p,P).getInt("FilmedDays")==days[0]&&!f.own(p,P).getBoolean("Completed"),"optional late discoveries do not grant skipped filming credit");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="ghosts_lore_lens321",timeoutTicks=1800)
    public static void onlyTheOwnedLiveBasementLensKeepsTheOtherwiseAbsentEffigy(GameTestHelper h){
        var f=fixture(h);var p=f.player("lens_reader",P);var peer=f.player("lens_friend",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the basement and camera owner are ready"))
            .thenExecute(()->{
                h.assertTrue(GhostsSet.jumpToDay(p,7,false)==null,"the late-day camera visit is available");f.at(p,P,5.5,-4,-21.5);var own=f.own(p,P);var cam=camera(h,p);p.setItemInHand(InteractionHand.MAIN_HAND,cam);
                h.assertTrue(!GhostsLore.throughLens(p,b,own),"an idle camera leaves the room ordinary");toggle(p,cam);h.assertTrue(GhostsLore.throughLens(p,b,own),"actual held owned recording reveals the camera image");
                var borrowed=cam.copy();CamcorderItem.setRolling(borrowed,false);peer.setItemInHand(InteractionHand.MAIN_HAND,borrowed);f.at(peer,P,5.5,-4,-21.5);toggle(peer,borrowed);
                var peerOwn=f.own(peer,P);peerOwn.putInt("FilmDay",7);h.assertTrue(!GhostsLore.throughLens(peer,b,peerOwn),"a borrowed camera is not the friend's lens");
                p.setGameMode(GameType.SPECTATOR);h.assertTrue(!GhostsLore.throughLens(p,b,own),"spectators cannot receive or record the image");p.setGameMode(GameType.SURVIVAL);
                f.at(p,P,0,0,-3);h.assertTrue(!GhostsLore.throughLens(p,b,own),"the image never follows the camera outside the basement");f.at(p,P,5.5,-4,-21.5);
                var target=Vec3.atLowerCornerOf(b.offset(GhostsLore.EFFIGY_AT)).add(.5,1.4,.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,target);float pitch=p.getXRot();
                h.assertTrue(GhostsSet.framed(p,target),"the actual lens can see the basement image without clipping furniture");
                for(int i=0;i<4;i++)GhostsSet.reader(p,b,own);save(f,p,own);
                h.assertTrue(GhostsLore.effigyRecorded(own)&&p.getXRot()==pitch,"native framed capture keeps the image without turning the player");
                h.assertTrue(f.in.getBlockState(b.offset(GhostsLore.EFFIGY_AT)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.STORED_CROSS,"the only physical prop is the ordinary stored cross and rag");
                for(var at:BlockPos.betweenClosed(b.offset(3,-4,-26),b.offset(11,-1,-14))){var state=f.in.getBlockState(at);h.assertTrue(!state.is(LiteraryRegistry.GHOST_SET.get())||state.getValue(GhostsSetBlock.KIND)!=GhostsSetBlock.Kind.BASEMENT_EFFIGY,"the shrine is never a physical world block");}
                String raw=GhostsConversations.review(own,7,true,Set.of("BasementEffigy")),cut=GhostsConversations.review(own,7,false,Set.of("BasementEffigy"));h.assertTrue(raw.contains("viewfinder")&&cut.contains("leaves out"),"the recorded image and the production cut disagree explicitly");
                p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(!GhostsLore.throughLens(p,b,own),"stowing the rolling camera immediately removes the image");
                f.reload();h.assertTrue(GhostsLore.effigyRecorded(f.own(p,P))&&!GhostsLore.effigyRecorded(f.own(peer,P))&&WitnessAccount.count(f.data(),p.getUUID())==0,"only the actual recorder retains it, without a Witness credit");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="ghosts_lore_aftermath321",timeoutTicks=1800)
    public static void revisitingTheBasementAfterCompletionKeepsTheOriginalCastAndCredit(GameTestHelper h){
        var f=fixture(h);var p=f.player("lore_after",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the original family is placed"))
            .thenExecute(()->{
                var before=f.in.getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(b,P),a->a.owner().filter(p.getUUID()::equals).isPresent()).stream().map(LiteraryActor::getUUID).collect(java.util.stream.Collectors.toSet());
                var own=f.own(p,P);own.putInt("FilmDay",7);own.putBoolean("Completed",true);own.putBoolean("DayDone",true);own.putBoolean("Rolling",false);save(f,p,own);
                f.at(p,P,5.5,-4,-21.5);var cam=camera(h,p);p.setItemInHand(InteractionHand.MAIN_HAND,cam);toggle(p,cam);int credits=WitnessAccount.count(f.data(),p.getUUID());
                for(int i=0;i<3;i++)GhostsSet.reader(p,b,own);
                h.assertTrue(GhostsLore.throughLens(p,b,own)&&before.stream().allMatch(id->f.in.getEntity(id) instanceof LiteraryActor)&&WitnessAccount.count(f.data(),p.getUUID())==credits,"the post-completion view cue preserves every original, including the offstage family and crew, without another credit");
            }).thenSucceed();
    }
}
