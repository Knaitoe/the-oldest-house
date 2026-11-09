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
    @AfterBatch(batch="ghosts_plan325") public static void planDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_bedrooms326") public static void bedroomsDone(ServerLevel l){close();}
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
        var f=fixture(h);var p=f.player("lore_reader",P);var peer=f.player("lore_friend",P);GhostsSetTests.lead(f,p,peer);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));int[] days={0};
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
        var f=fixture(h);var p=f.player("lens_reader",P);var peer=f.player("lens_friend",P);GhostsSetTests.lead(f,p,peer);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));
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

    /**
     * 0.4.325 (owner: "Some evidence of merry and Marjorie's plot to 'save' their parents should be evident"; "maybe have the shrine
     * appear on day 5"): the pad and the open canisters are there to find only on the last day, from the actual anchors the new build
     * places, and the camera's shrine shows from the fifth day with the jar on its bench until the last.
     */
    @GameTest(template="empty",batch="ghosts_plan325",timeoutTicks=1800)
    public static void theSistersPlanIsInTheHouseOnTheLastDayAndTheShrineFromTheFifth(GameTestHelper h){
        var f=fixture(h);var p=f.player("plan_reader",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the new set and original cast are resident"))
            .thenExecute(()->{
                papers(p);
                h.assertTrue(f.in.getBlockState(b.offset(GhostsLore.PAD_AT)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.PLAN_PAD&&f.in.getBlockState(b.offset(GhostsLore.CANISTERS_AT)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.CANISTERS,"the new build places the pad and canister anchors");
                h.assertTrue(!GhostsLore.lensImage(4,true,true)&&GhostsLore.lensImage(5,true,true)&&GhostsLore.lensImage(6,true,true)&&GhostsLore.lensImage(7,true,true)&&!GhostsLore.lensImage(5,false,true)&&!GhostsLore.lensImage(5,true,false),"the shrine shows only through the reader's own rolling camera in the basement, from the fifth day");
                h.assertTrue(GhostsLore.shrine(5)==GhostsSetBlock.Kind.BASEMENT_EFFIGY_JAR&&GhostsLore.shrine(6)==GhostsSetBlock.Kind.BASEMENT_EFFIGY_JAR&&GhostsLore.shrine(7)==GhostsSetBlock.Kind.BASEMENT_EFFIGY,"the jar stands on the shrine's bench until the last day");
                h.assertTrue(GhostsLore.pad(6)==null&&GhostsLore.pad(7)==GhostsSetBlock.Kind.PLAN_PAD&&GhostsLore.canisters(6)==GhostsSetBlock.Kind.CANISTERS&&GhostsLore.canisters(7)==GhostsSetBlock.Kind.CANISTERS_OPEN,"the pad appears and the canisters stand open only on the last day");
                f.click(p,P,GhostsLore.PAD_AT);f.click(p,P,GhostsLore.CANISTERS_AT);
            }).thenIdle(3).thenExecute(()->{
                h.assertTrue(papers(p).isEmpty()&&!f.own(p,P).getBoolean("LorePad")&&!f.own(p,P).getBoolean("LoreCanisters"),"on an early day there is no pad to read and the canisters are only canisters");
                h.assertTrue(GhostsSet.jumpToDay(p,7,false)==null,"the real shortcut selects the last day");
                f.click(p,P,GhostsLore.PAD_AT);f.click(p,P,GhostsLore.CANISTERS_AT);
            }).thenIdle(3).thenExecute(()->{
                var late=papers(p);h.assertTrue(late.size()==2&&late.stream().anyMatch(a->a.kind()==GhostsLore.PAD)&&late.stream().anyMatch(a->a.kind()==GhostsLore.CANISTERS),"the last day's actual inputs open the pad and the counter: "+late);
                var own=f.own(p,P);String account=GhostsLore.account(own);
                h.assertTrue(own.getBoolean("LorePad")&&own.getBoolean("LoreCanisters")&&GhostsLore.found(own)==2,"both discoveries are kept");
                h.assertTrue(account.contains("the plan")&&account.contains("Nobody had been baking"),"the account carries the plan and the canisters: "+account);
                h.assertTrue(String.join("\n",GhostsLore.pages(GhostsLore.PAD,7)).contains("JAR TO POLICE")&&GhostsLore.pages(GhostsLore.LAPTOP,7).size()==4,"the pad holds the plan in Merry's capitals and the laptop has its image search");
                f.reload();h.assertTrue(f.own(p,P).getBoolean("LorePad")&&f.own(p,P).getBoolean("LoreCanisters")&&!f.own(p,P).getBoolean("Completed"),"they survive reload and complete nothing");
            }).thenSucceed();
    }

    /**
     * 0.4.326 (owner's notes, 7 November): "Give different text and textures to the piles of books in merry's room"; "Drawer should
     * not begin opened. Should also be rotated and placed against wall." Every pile in the house is its own kind with its own book,
     * and Marjorie's desk stands against her east wall facing the bed, its drawer shut until the sixth day's take.
     */
    @GameTest(template="empty",batch="ghosts_bedrooms326",timeoutTicks=1800)
    public static void eachPileOfBooksIsItsOwnAndMarjoriesDeskStandsAgainstHerWall(GameTestHelper h){
        var f=fixture(h);var p=f.player("pile_reader",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,P));
        var picture=new BlockPos(6,6,-25);var library=new BlockPos(9,5,-17);
        h.startSequence().thenWaitUntil(()->h.assertTrue(resident(f,p),"the new set and original cast are resident"))
            .thenExecute(()->{
                var kinds=Map.of(picture,GhostsSetBlock.Kind.BOOKS_PICTURE,library,GhostsSetBlock.Kind.BOOKS_LIBRARY,new BlockPos(-10,6,-25),GhostsSetBlock.Kind.BOOKS_PAPERBACK,new BlockPos(-4,1,-25),GhostsSetBlock.Kind.BOOKS_COOK,new BlockPos(5,-4,-25),GhostsSetBlock.Kind.BOOKS);
                for(var e:kinds.entrySet()){var state=f.in.getBlockState(b.offset(e.getKey()));h.assertTrue(state.is(LiteraryRegistry.GHOST_SET.get())&&state.getValue(GhostsSetBlock.KIND)==e.getValue(),"the pile at "+e.getKey()+" is "+e.getValue()+": "+state);}
                var titles=new HashSet<String>();for(var k:kinds.values())titles.add(GhostsSetInteractions.books(k).title());
                h.assertTrue(titles.size()==5,"every pile has its own book: "+titles);
                var desk=f.in.getBlockState(b.offset(GhostsSetRooms.EXORCISM_DESK));var wall=f.in.getBlockState(b.offset(GhostsSetRooms.EXORCISM_DESK).relative(GhostsSetRooms.EXORCISM_FACING.getOpposite()));
                h.assertTrue(desk.is(LiteraryRegistry.GHOST_SET.get())&&desk.getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.WRITING_DESK&&desk.getValue(GhostsSetBlock.FACING)==GhostsSetRooms.EXORCISM_FACING,"the desk faces the bed: "+desk);
                h.assertTrue(wall.isCollisionShapeFullBlock(f.in,b.offset(GhostsSetRooms.EXORCISM_DESK).relative(GhostsSetRooms.EXORCISM_FACING.getOpposite())),"its back is against the wall: "+wall);
                h.assertTrue(f.in.getBlockState(b.offset(GhostsLore.LAPTOP_AT)).getValue(GhostsSetBlock.FACING)==GhostsSetRooms.EXORCISM_FACING,"the laptop on it faces the same way");
                var drawers=f.in.getEntitiesOfClass(LiteraryActor.class,new net.minecraft.world.phys.AABB(b.offset(GhostsSetRooms.EXORCISM_DESK)).inflate(1),a->a.role()==LiteraryActor.GHOST_DRAWER);
                h.assertTrue(drawers.size()==1&&drawers.get(0).phase()==0,"the reader's drawer starts shut: "+drawers);
                f.at(p,P,7.5,5,-20.5);f.click(p,P,picture);
            }).thenIdle(2).thenExecute(()->f.click(p,P,library)).thenIdle(2).thenExecute(()->{
                var own=f.own(p,P);String a=own.getCompound("Original_GirlsPaper0488_"+picture.asLong()).toString(),c=own.getCompound("Original_GirlsPaper0488_"+library.asLong()).toString();
                h.assertTrue(a.contains("picture books")&&c.contains("library books")&&c.contains("lighthouse"),"the two piles in Merry's room open different books: "+a.length()+" / "+c.length());
            }).thenSucceed();
    }
}
