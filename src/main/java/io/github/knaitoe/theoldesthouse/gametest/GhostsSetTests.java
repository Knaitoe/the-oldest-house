package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_ghosts") @PrefixGameTestTemplate(false)
public final class GhostsSetTests {
    private static LiteraryTests.Fixture active;
    private static final LabyrinthPlace P=LabyrinthPlace.GHOSTS_SET;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){active=new LiteraryTests.Fixture(h);return active;}
    private static void save(LiteraryTests.Fixture f,ServerPlayer p,CompoundTag t){LiteraryVignettes.save(f.data(),p.getUUID(),P,t);}
    private static void film(ServerPlayer p,ItemStack s){p.setItemInHand(InteractionHand.MAIN_HAND,s);p.startUsingItem(InteractionHand.MAIN_HAND);}
    private static ItemStack cam(ServerPlayer p){return p.getInventory().items.stream().filter(s->s.is(LiteraryRegistry.CAMCORDER.get())).findFirst().orElseThrow().copy();}
    private static LiteraryActor actor(LiteraryTests.Fixture f,ServerPlayer p,int role){return f.in.getEntitiesOfClass(LiteraryActor.class,new AABB(Vec3.atLowerCornerOf(f.base(P).offset(-21,-5,-47)),Vec3.atLowerCornerOf(f.base(P).offset(22,13,1))),a->a.role()==role&&a.owner().filter(p.getUUID()::equals).isPresent()).stream().findFirst().orElseThrow();}
    private static String words(ItemStack s){return s.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).reduce("",String::concat);}
    @GameTest(template="empty",batch="ghosts_days",timeoutTicks=200)
    public static void followingDayRequiresDoorExitAndInterruptedDayResumes(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_days",P);var t=f.own(p,P);t.putInt("ShotClock",315);save(f,p,t);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p,P).getInt("ShotClock")==315,"an interrupted arrival retains the occupied filming clock");t=f.own(p,P);t.putBoolean("DayDone",true);t.putInt("Beat",t.getInt("Visit"));save(f,p,t);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p,P).getInt("FilmDay")==1,"a timer or repeated arrival inside cannot advance the day");GhostsSet.depart(p);f.at(p,P,0,0,2);f.arrive(p,P);h.assertTrue(f.own(p,P).getInt("FilmDay")==2&&f.own(p,P).getInt("ShotClock")==0,"door departure followed by actual later arrival starts the following day");f.reload();h.assertTrue(f.own(p,P).getInt("FilmDay")==2,"the personal filming day survives native SavedData reload");h.succeed();}
    @GameTest(template="empty",batch="ghosts_lens",timeoutTicks=240)
    public static void RealLensChangesTheEditedAccountAndBorrowedCamerasDoNot(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_lens",P);var peer=f.player("ghost_peer",P);f.source(p,P);h.runAtTickTime(30,()->{
        var t=f.own(p,P);GhostsSet.start(p,t);GhostsSet.reader(p,f.base(P),t);f.at(p,P,-7,0,-21);var m=actor(f,p,LiteraryActor.GHOST_MARJORIE);film(p,cam(p));p.lookAt(EntityAnchorArgument.Anchor.EYES,m.getEyePosition());for(int i=0;i<8;i++)GhostsSet.reader(p,f.base(P),t);save(f,p,t);
        h.assertTrue(t.getCompound("FilmLog1").getInt("Frames_Marjorie")>=20,"native use, real aim and clear ray record Marjorie");String cut=words(GhostsSet.edit(p,t,1,false));h.assertTrue(cut.contains("Marjorie"),"recorded framing enters the edited episode");
        film(peer,cam(p));h.assertTrue(!GhostsSet.recording(peer),"a borrowed camera cannot make the peer's camera log");peer.setGameMode(GameType.SPECTATOR);h.assertTrue(!GhostsSet.recording(peer),"an actual native observer earns no recording authority");h.assertTrue(f.own(peer,P).getCompound("FilmLog1").isEmpty(),"film records remain independent per reader");h.succeed();});}
    @GameTest(template="empty",batch="ghosts_ray",timeoutTicks=200)
    public static void TableAndWallOcclusionRemainReal(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_ray",P);f.at(p,P,0,0,-26);var at=new Vec3(f.base(P).getX()+.5,f.base(P).getY()+.6,f.base(P).getZ()-28.5);p.lookAt(EntityAnchorArgument.Anchor.EYES,at);h.assertTrue(!GhostsSet.framed(p,at),"the table top occludes a body underneath even in an adjacent native block");var clear=p.getEyePosition().add(p.getLookAngle().scale(1));h.assertTrue(GhostsSet.framed(p,clear),"an unobstructed nearby point in the actual lens is usable");h.succeed();}
    @GameTest(template="empty",batch="ghosts_frames",timeoutTicks=200)
    public static void FramesAreBoundedNonceOwnedAndSingleUse(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_frame",P);var peer=f.player("ghost_other",P);film(p,cam(p));var t=f.own(p,P);var nonce=UUID.randomUUID();t.putUUID("FrameNonce",nonce);t.putInt("FrameTick",p.server.getTickCount());t.putInt("FrameDay",1);t.putDouble("FrameX",p.getX());t.putDouble("FrameY",p.getY());t.putDouble("FrameZ",p.getZ());t.putFloat("FrameYaw",p.getYRot());t.putFloat("FramePitch",p.getXRot());save(f,p,t);h.assertTrue(!GhostsSet.receiveFrame(peer,nonce,new int[384])&&!GhostsSet.receiveFrame(p,nonce,new int[385]),"wrong sender and oversized frame are refused");h.assertTrue(GhostsSet.receiveFrame(p,nonce,new int[384])&&!GhostsSet.receiveFrame(p,nonce,new int[384]),"a valid native owner frame consumes its nonce once");f.reload();h.assertTrue(f.own(p,P).getList("FramesDay1",10).size()==1,"the bounded real frame survives native SavedData reload");h.succeed();}
    @GameTest(template="empty",batch="ghosts_cast",timeoutTicks=240)
    public static void WholeFamilyIsPrivateInteractableAndReusesBodies(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_cast",P);var peer=f.player("ghost_cast2",P);h.runAtTickTime(30,()->{var ids=new HashSet<UUID>();for(int r:GhostsSet.ROLES){var a=actor(f,p,r);ids.add(a.getUUID());h.assertTrue(!a.owner().get().equals(actor(f,peer,r).owner().get()),"each reader has their own original cast");}var m=actor(f,p,LiteraryActor.GHOST_MARJORIE);m.appearance(m.role(),GhostsSet.CRAWL);h.assertTrue(m.getBoundingBox().getYsize()<.7,"the native crawl body fits under the actual table");var t=f.own(p,P);GhostsSet.reader(p,f.base(P),t);for(int r:GhostsSet.ROLES)h.assertTrue(ids.contains(actor(f,p,r).getUUID()),"updates retain all six native identities");h.succeed();});}
    @GameTest(template="empty",batch="ghosts_legacy",timeoutTicks=200)
    public static void LegacyResolutionsAndQuotaSurvive(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_legacy",P);var t=f.own(p,P);t.putBoolean("Completed",true);t.putBoolean("Ready",true);t.putString("Confession","The old, original account.");save(f,p,t);LiteraryVignettes.onArrive(p,P);h.assertTrue(f.own(p,P).getBoolean("Completed")&&f.own(p,P).getString("Confession").equals("The old, original account."),"earlier completed accounts remain exact");h.assertTrue(WitnessAccount.Story.values().length==45&&WitnessAccount.REQUIRED==34&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT,"the filming source remains one multi-visit credit beside the new lake-house source");h.succeed();}
    @GameTest(template="empty",batch="ghosts_ending",timeoutTicks=240)
    public static void OnlyAllFilmingDaysAndOwnFinalAccountResolve(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_end",P);f.source(p,P);h.runAtTickTime(30,()->{var t=f.own(p,P);t.putInt("FilmDay",7);t.putBoolean("DayDone",true);t.putString("Confession","I chose to keep looking at her mother.");GhostsSet.reader(p,f.base(P),t);h.assertTrue(!t.getBoolean("Ready"),"the last arrival and a confession do not replace the previous filming days");t.putInt("FilmedDays",127);GhostsSet.reader(p,f.base(P),t);save(f,p,t);h.assertTrue(t.getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.GHOSTS_SET),"the completed personal account still has to be read");f.read(p,P,LiteraryRooms.ending(P));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.GHOSTS_SET),"actual final native reading resolves this reader's existing source");h.succeed();});}
    @GameTest(template="empty",batch="ghosts_menu",timeoutTicks=240)
    public static void ReviewingTheCutKeepsItsNewNativeBookOpen(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_menu",P);f.source(p,P);h.runAtTickTime(30,()->{var t=f.own(p,P);t.putBoolean("DayDone",true);save(f,p,t);var barry=actor(f,p,LiteraryActor.GHOST_PRODUCER);p.moveTo(barry.position().add(0,0,1));p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);LiteraryVignettes.talk(p,barry);var menu=p.containerMenu;menu.clicked(0,0,ClickType.PICKUP,p);h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the selection closes only itself, leaving the new native edited episode open");h.succeed();});}
    @GameTest(template="empty",batch="ghosts_call_sheet",timeoutTicks=180)
    public static void CallSheetCollectionDoesNotRecallThePlainsPhotographIntoTheOpenReader(GameTestHelper h){
        var f=fixture(h);var p=f.player("ghost_call_sheet",P);
        var photo=io.github.knaitoe.theoldesthouse.opening.NavidsonLetter.createSnapshot(f.in,NovelVignettes.plainPixels());
        net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,photo,t->{t.putUUID(NovelVignettes.PHOTO_OWNER,p.getUUID());t.putUUID(NovelVignettes.PHOTO_ID,UUID.randomUUID());});
        var own=NovelVignettes.personal(f.data(),p.getUUID());own.put("Photo",photo.save(p.registryAccess()));own.putLong("PhotoDay",0);NovelVignettes.save(f.data(),p.getUUID(),own);
        var chestAt=f.base(P).offset(-17,0,-8);f.in.setBlock(chestAt,Blocks.CHEST.defaultBlockState(),3);
        var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)f.in.getBlockEntity(chestAt);chest.setItem(0,photo.copy());
        f.click(p,P,new BlockPos(-17,0,-8));p.getInventory().selected=1;f.click(p,P,LiteraryRooms.source(P));
        h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the actual call-sheet surface opens its own native reader");
        var menu=(LiteraryVignettes.Pages)p.containerMenu;var paper=menu.original();
        h.assertTrue(paper.is(Items.WRITTEN_BOOK)&&paper.get(DataComponents.WRITTEN_BOOK_CONTENT).title().raw().equals("Call sheet"),"the open original is the producer's call sheet");
        p.server.overworld().setDayTime(24000);
        h.runAfterDelay(25,()->{
            h.assertTrue(p.containerMenu==menu&&ItemStack.matches(chest.getItem(0),photo)&&p.getInventory().countItem(Items.FILLED_MAP)==0,"a due morning recall waits while the reader is open instead of intruding on paper collection");
            h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"taking the call sheet yields its finite original exactly once");
            h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,paper)),"the exact call-sheet stack reaches the reader's inventory");
            h.assertTrue(ItemStack.matches(p.getMainHandItem(),paper),"the collected call sheet occupies the selected empty hand before any unrelated recall");
            p.closeContainer();
        });
        h.runAfterDelay(65,()->{
            h.assertTrue(chest.getItem(0).isEmpty()&&p.getInventory().countItem(Items.FILLED_MAP)==1,"after the reader closes, the same photograph is recalled from storage once");
            h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,paper))&&p.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,photo)),"the call sheet and photograph remain distinct exact originals");
            h.assertTrue(ItemStack.matches(p.getMainHandItem(),paper),"the photograph's later return does not replace the collected call sheet in the hand");
            f.reload();f.click(p,P,LiteraryRooms.source(P));
            h.assertTrue(ItemStack.matches(((LiteraryVignettes.Pages)p.containerMenu).original(),paper)&&!p.containerMenu.clickMenuButton(p,3),"reload preserves the call sheet and its finite collection independently of the returning photo");h.succeed();
        });
    }
    @GameTest(template="empty",batch="ghosts_crawl",timeoutTicks=260)
    public static void DinnerCrawlAndFittingsUseNativeClearance(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_crawl",P);f.source(p,P);h.runAtTickTime(30,()->{var t=f.own(p,P);t.putInt("FilmDay",2);t.putInt("ShotClock",515);t.putBoolean("CastPlaced",false);t.putBoolean("Rolling",true);f.at(p,P,0,0,-26);for(int n=0;n<70;n++)GhostsSet.reader(p,f.base(P),t);var m=actor(f,p,LiteraryActor.GHOST_MARJORIE);h.assertTrue(Math.abs(m.getX()-f.base(P).getX()+.4)<.3&&Math.abs(m.getZ()-f.base(P).getZ()+28.5)<.3,"the original body crawls into the native table clearance: "+m.position());h.assertTrue(m.getBoundingBox().maxY<f.base(P).getY()+.9375,"the crawl collision body stays below the continuous tabletop");h.assertTrue(f.in.getBlockState(f.base(P).offset(0,0,-29)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.BOLTS,"the fittings retain a real table top");h.succeed();});}
    @GameTest(template="empty",batch="ghosts_pickup",timeoutTicks=260)
    public static void SistersClimbBothRealStairsWithoutAdvancingTheTake(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_pickup",P);f.source(p,P);h.runAtTickTime(30,()->{var t=f.own(p,P);t.putInt("FilmDay",4);t.putInt("ShotClock",565);t.putBoolean("CastPlaced",false);t.putBoolean("Rolling",true);f.at(p,P,5,-4,-27);GhostsSet.reader(p,f.base(P),t);for(int n=0;n<600;n++)GhostsSet.reader(p,f.base(P),t);for(int i=0;i<2;i++){var a=actor(f,p,GhostsSet.ROLES[i]);h.assertTrue(t.getInt("PickupStep"+i)>=18&&a.getY()>=f.base(P).getY()+4.9,"both sisters use the basement stair, landing stair and bedroom doorway: "+i+", step="+t.getInt("PickupStep"+i)+", at="+a.position());}h.assertTrue(t.getInt("ShotClock")==570&&!t.getBoolean("Rolling"),"the upstairs pickup waits for another explicit start and does not consume filming time while walking");var prop=actor(f,p,LiteraryActor.GHOST_PLAYHOUSE);h.assertTrue(prop.phase()==0,"the intact private playhouse waits for its actual take");h.succeed();});}
    @GameTest(template="empty",batch="ghosts_lead",timeoutTicks=200)
    public static void TheLongestCentredSubjectChangesTheBroadcastStory(GameTestHelper h){var a=new CompoundTag();var l=new CompoundTag();l.putInt("Frames_Marjorie",40);l.putInt("Frames_Sarah",40);l.putInt("LeadTicks_Sarah",30);a.put("FilmLog2",l);String first=words(GhostsSet.edit("reader",a,2,false));l.putInt("LeadTicks_Marjorie",60);a.put("FilmLog2",l);String second=words(GhostsSet.edit("reader",a,2,false));h.assertTrue(first.contains("medical concerns")&&second.contains("possession story")&&!first.equals(second),"centring a different face changes the production's lead, even when both appear in the footage");h.succeed();}
    @GameTest(template="empty",batch="ghosts_upgrade",timeoutTicks=800)
    public static void SavedUpgradeWaitsForAnActualCameraAndPreservesOriginalsAcrossRestart(GameTestHelper h){
        var f=fixture(h);var p=f.player("ghost_upgrade",P);f.source(p,P);var b=f.base(P);f.chunks.hold(f.in,new AABB(Vec3.atLowerCornerOf(b.offset(-22,-5,-48)),Vec3.atLowerCornerOf(b.offset(23,13,2))));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native fixture entity sections are ready")).thenExecute(()->{
            var t=f.own(p,P);GhostsSet.reader(p,b,t);var m=actor(f,p,LiteraryActor.GHOST_MARJORIE);UUID id=m.getUUID();var original=t.getCompound("Original_Source").copy();t.putBoolean("Completed",true);save(f,p,t);
            var source=f.in.getBlockEntity(b.offset(LiteraryRooms.source(P)));var area=new AABB(Vec3.atLowerCornerOf(b.offset(-21,-5,-47)),Vec3.atLowerCornerOf(b.offset(22,13,1)));
            // A preceding fixture's lectern can drop its original when this fixture rebuilds.
            // Recover it through the native inventory path before testing a vacant upgrade.
            for(var loose:f.in.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area)){p.teleportTo(loose.getX(),loose.getY(),loose.getZ());loose.setNoPickUpDelay();loose.playerTouch(p);h.assertTrue(!loose.isAlive(),"native setup recovers the preceding fixture's loose original without deleting it");}
            var chestAt=b.offset(6,0,-8);f.in.setBlock(chestAt,Blocks.CHEST.defaultBlockState(),3);var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)f.in.getBlockEntity(chestAt);var note=new ItemStack(Items.PAPER,3);note.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("A kept original"));chest.setItem(0,note.copy());
            var edit=b.offset(5,2,-8);f.in.setBlock(edit,Blocks.GOLD_BLOCK.defaultBlockState(),3);var floor=b.offset(-19,-1,-6);f.in.setBlock(floor,Blocks.OAK_PLANKS.defaultBlockState(),3);
            var state=f.data().state(GhostsSet.STATE);state.remove(Long.toString(b.asLong()));f.data().setState(GhostsSet.STATE,state);
            p.setGameMode(GameType.SPECTATOR);p.setCamera(m);f.at(p,P,100,0,-8);h.assertTrue(!GhostsSet.rebuild(f.in,b),"a real spectator camera in the set pauses the saved upgrade even with its body outside");p.setCamera(null);f.at(p,P,100,0,-8);
            boolean first=GhostsSet.rebuild(f.in,b);h.assertTrue(!first&&f.data().state(GhostsSet.STATE).contains("Repair_"+b.asLong(),12),"the first bounded slice persists its protection before editing the room: finished="+first+", camera="+p.getCamera().position()+", body="+p.position()+", sections="+f.chunks.ready()+", residents="+f.in.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,area,e->e.isAlive()&&!(e instanceof LiteraryActor)).stream().map(e->e.getType()+" at "+e.position()).toList());f.reload();GhostsSet.clearAll();boolean done=false;for(int n=0;n<400&&!done;n++)done=GhostsSet.rebuild(f.in,b);
            h.assertTrue(done&&GhostsSet.built(f.data(),b),"the protected saved upgrade resumes after losing its runtime cursor");h.assertTrue(f.in.getBlockEntity(b.offset(LiteraryRooms.source(P)))==source&&f.in.getBlockEntity(chestAt)==chest&&ItemStack.matches(chest.getItem(0),note),"native source and custom container instances, exact contents and quantities survive without removal or restocking");
            h.assertTrue(f.in.getBlockState(edit).is(Blocks.GOLD_BLOCK)&&f.in.getBlockState(floor).is(LiteraryRegistry.GHOST_SET.get()),"the player's edit survives while an unchanged legacy floor receives the new authored surface");h.assertTrue(f.in.getEntity(id)==m&&f.own(p,P).getBoolean("Completed")&&f.own(p,P).getCompound("Original_Source").equals(original),"family identity, finished personal state and immutable original survive the same restart");h.succeed();
        });
    }
    @GameTest(template="empty",batch="ghosts_full_take",timeoutTicks=360)
    public static void SevenActualTakesReachThePrivateCollapseMechanismAndHeldJump(GameTestHelper h){var f=fixture(h);var p=f.player("ghost_full_take",P);f.source(p,P);h.runAtTickTime(30,()->{
        for(int d=1;d<=7;d++){
            var t=f.own(p,P);h.assertTrue(t.getInt("FilmDay")==d,"the real take belongs to its following filming day");double[] at=switch(d){case 3,5,6->new double[]{-7,5,-28};case 4->new double[]{5,-4,-27};default->new double[]{-4,0,-24};};f.at(p,P,at[0],at[1],at[2]);h.assertTrue(GhostsSet.start(p,t),"each take starts through the owner/source gate");
            for(int n=0;n<800&&!t.getBoolean("DayDone");n++){GhostsSet.reader(p,f.base(P),t);if(d==4&&t.getBoolean("BedroomPickup")&&t.getInt("PickupStep0")>=18&&t.getInt("PickupStep1")>=18&&!t.getBoolean("Rolling")){f.at(p,P,8,5,-28);GhostsSet.start(p,t);}}
            h.assertTrue(t.getBoolean("DayDone")&&t.getInt("ShotClock")>=GhostsSet.LENGTH[d-1],"the physical take runs to its own final beat, day "+d+", clock="+t.getInt("ShotClock"));
            if(d==4)h.assertTrue(actor(f,p,LiteraryActor.GHOST_PLAYHOUSE).phase()==2,"the actual private cardboard body reaches its collapsed state");
            if(d==6){var m=actor(f,p,LiteraryActor.GHOST_MARJORIE);h.assertTrue(actor(f,p,LiteraryActor.GHOST_DRAWER).phase()==2&&t.getBoolean("JumpFrozen0488")&&t.getBoolean("JumpEject0488")&&m.phase()==GhostsSet.LEAP&&m.getY()>f.base(P).getY()+5,"the drawer opens and the original body holds its midair pose before the forced cut: "+m.position());}
            save(f,p,t);if(d<7){GhostsSet.depart(p);f.at(p,P,0,0,2);f.arrive(p,P);}
        }
        h.assertTrue(f.own(p,P).getInt("FilmedDays")==127,"seven real scripted takes leave all seven personal day records");h.succeed();
    });}
    @GameTest(template="empty",batch="ghosts_frozen_return",timeoutTicks=240)
    public static void HeldJumpTurnsOnlyItsOriginalHeadAndReturnsThroughTheSavedVisit(GameTestHelper h){
        var f=fixture(h);var p=f.player("ghost_jump",P);var peer=f.player("ghost_jump_peer",P);f.source(p,P);
        final Vec3[] body={null};final UUID[] original={null};final float[] startHead={0};final CompoundTag[] peerBefore={null};
        var back=Vec3.atBottomCenterOf(f.base(P).offset(0,0,40));var returned=back.add(0,0,1);
        h.startSequence().thenIdle(30).thenExecute(()->{
            var own=f.own(p,P);own.putInt("FilmDay",6);own.putInt("ShotClock",715);own.putBoolean("CastPlaced",false);own.putBoolean("Rolling",true);own.putBoolean("DayDone",false);f.at(p,P,-7,5,-28);film(p,cam(p));
            GhostsSet.reader(p,f.base(P),own);var m=actor(f,p,LiteraryActor.GHOST_MARJORIE);body[0]=m.position();original[0]=m.getUUID();startHead[0]=m.getYHeadRot();peerBefore[0]=f.own(peer,P).copy();
            h.assertTrue(own.getBoolean("JumpFrozen0488")&&m.phase()==GhostsSet.LEAP&&!own.getBoolean("DayDone"),"the jump begins as a held pose on the actual original actor");
            save(f,p,own);f.reload();own=f.own(p,P);GhostsSet.arrive(p,own);save(f,p,own);h.assertTrue(own.getBoolean("JumpFrozen0488")&&own.getInt("ShotClock")==720,"an interrupted arrival and SavedData reload keep the held jump instead of launching it again");
            f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(f.in.dimension(),back,0,true));
        }).thenIdle(45).thenExecute(()->{
            var own=f.own(p,P);var m=actor(f,p,LiteraryActor.GHOST_MARJORIE);
            h.assertTrue(m.getUUID().equals(original[0])&&m.position().distanceToSqr(body[0])<.000001&&m.getDeltaMovement().lengthSqr()<.000001,"native ticks retain the same motionless midair body");
            h.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(m.getYHeadRot()-startHead[0]))>10&&!own.getBoolean("DayDone"),"the head turns toward its reader's camera before the three-second hold ends");
            h.assertTrue(own.getCompound("FilmLog6").getInt("Frames_FrozenLeap")>=20&&f.own(peer,P).getInt("FilmDay")==peerBefore[0].getInt("FilmDay")&&!f.own(peer,P).getBoolean("JumpFrozen0488"),"the owned lens records its held view without borrowing a peer's day");
        }).thenWaitUntil(()->{
            var t=f.own(p,P);h.assertTrue(!LabyrinthDoors.isBusy(p)&&!LiteraryVignettes.inside(p,P)&&f.data().returnDepth(p.getUUID())==0,"the authored cut completes the native saved return: clock="+t.getInt("ShotClock")+", done="+t.getBoolean("DayDone")+", eject="+t.getBoolean("JumpEject0488")+", busy="+LabyrinthDoors.isBusy(p)+", depth="+f.data().returnDepth(p.getUUID())+", pos="+p.position()+", place="+LabyrinthPlaces.placeAt(f.origin,p.blockPosition()));
        }).thenExecute(()->{
            h.assertTrue(p.position().distanceToSqr(returned)<.01&&f.own(p,P).getBoolean("FilmDoorExit")&&f.own(p,P).getInt("FilmDay")==6,"the cut returns beside the saved prior door without carrying its upstairs offset and leaves the following day unstarted");
            h.assertTrue(f.in.getEntity(original[0])!=null&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.GHOSTS_SET),"the same original body survives and the cut alone grants no ending");
            f.arrive(p,P);h.assertTrue(f.own(p,P).getInt("FilmDay")==7&&!f.own(p,P).getBoolean("JumpFrozen0488")&&f.own(peer,P).getInt("FilmDay")==1,"a later actual arrival admits only this reader to day seven");
        }).thenSucceed();
    }
    private static void close(){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="ghosts_days") public static void daysDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_lens") public static void lensDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_ray") public static void rayDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_frames") public static void framesDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_cast") public static void castDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_legacy") public static void legacyDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_ending") public static void endingDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_menu") public static void menuDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_call_sheet") public static void callSheetDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_crawl") public static void crawlDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_pickup") public static void pickupDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_upgrade") public static void upgradeDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_full_take") public static void fullTakeDone(ServerLevel l){close();}
    @AfterBatch(batch="ghosts_frozen_return") public static void frozenReturnDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="ghosts_furniture0488",timeoutTicks=240)
    public static void RealCaseLidsFinitePaperAndLampSwitchesLeavePeerDaysIndependent(GameTestHelper h){
        var f=fixture(h);var p=f.player("ghost_fittings",P);var peer=f.player("ghost_fittings_peer",P);var b=f.base(P);var at=b.offset(17,0,-12);
        h.runAfterDelay(30,()->{
            var box=(GhostsCaseBlockEntity)f.in.getBlockEntity(at);h.assertTrue(box!=null&&box.getContainerSize()==9&&box.isEmpty(),"the physical camera case is an empty native nine-slot container");
            var original=new ItemStack(Items.PAPER,3);original.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("The original camera notes"));box.setItem(0,original.copy());
            for(var reader:List.of(p,peer)){reader.moveTo(at.getCenter().add(0,0,1.5));reader.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);reader.gameMode.useItemOn(reader,f.in,reader.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));h.assertTrue(reader.containerMenu instanceof net.minecraft.world.inventory.ChestMenu,"native case use opens the real container");}
            h.assertTrue(f.in.getBlockState(at).getValue(GhostsCaseBlock.OPEN),"the first real opener raises the native case lid");p.closeContainer();h.assertTrue(f.in.getBlockState(at).getValue(GhostsCaseBlock.OPEN),"one reader closing does not shut the case on its remaining peer");peer.containerMenu.quickMoveStack(peer,0);peer.closeContainer();h.assertTrue(!f.in.getBlockState(at).getValue(GhostsCaseBlock.OPEN)&&box.isEmpty()&&peer.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,original)),"the last closer shuts the lid and the exact three original papers transfer once");
            var saved=box.saveWithFullMetadata(f.in.registryAccess());box.loadWithComponents(saved,f.in.registryAccess());h.assertTrue(box.isEmpty(),"a native block-entity reload does not refill the case");
            f.click(p,P,new BlockPos(-12,3,-26));h.assertTrue(f.in.getBlockState(b.offset(-12,3,-26)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.LAMP_OFF&&f.in.getBlockState(b.offset(-12,3,-26)).getLightEmission(f.in,b.offset(-12,3,-26))==0,"the actual shared lamp switch removes its native emitted light");f.click(p,P,new BlockPos(-12,3,-26));h.assertTrue(f.in.getBlockState(b.offset(-12,3,-26)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.LAMP,"the same fitting switches back on");
            f.click(p,P,new BlockPos(-13,6,-41));h.assertTrue(p.containerMenu instanceof LiteraryVignettes.Pages,"the sisters' supported book stack opens its finite native paper");var paper=((LiteraryVignettes.Pages)p.containerMenu).original();h.assertTrue(p.containerMenu.clickMenuButton(p,3)&&!p.containerMenu.clickMenuButton(p,3),"that stack gives its exact personal original only once");p.closeContainer();f.reload();f.click(p,P,new BlockPos(-13,6,-41));h.assertTrue(ItemStack.matches(((LiteraryVignettes.Pages)p.containerMenu).original(),paper)&&!p.containerMenu.clickMenuButton(p,3),"the same original and its collection persist through native SavedData reload");p.closeContainer();
            var plate=b.offset(-1,1,-28);var physical=f.in.getBlockState(plate);var mine=f.own(p,P);mine.putInt("FilmDay",6);save(f,p,mine);h.assertTrue(GhostsSetBlock.meal(mine.getInt("FilmDay"))==GhostsSetBlock.Kind.MEAL_POTATOES&&GhostsSetBlock.meal(f.own(peer,P).getInt("FilmDay"))==GhostsSetBlock.Kind.MEAL_STEW&&f.in.getBlockState(plate).equals(physical),"the readers' different meal days never rewrite a shared physical plate");h.assertTrue(GhostsSetBlock.meal(2)==GhostsSetBlock.Kind.PLATE&&GhostsSetBlock.meal(7)==GhostsSetBlock.Kind.PLATE&&!mine.getBoolean("Ready")&&WitnessAccount.count(f.data(),p.getUUID())==0,"both canonical spaghetti meals remain and decor grants no ending");h.succeed();
        });
    }
    @AfterBatch(batch="ghosts_furniture0488") public static void furnitureDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="ghosts_dressing0488",timeoutTicks=540)
    public static void SavedDressingWaitsForObserversAndKeepsNativeOriginalsAndEdits(GameTestHelper h){
        var f=fixture(h);var p=f.player("ghost_dressing",P);var b=f.base(P);f.chunks.hold(f.in,new AABB(Vec3.atLowerCornerOf(b.offset(-22,-5,-48)),Vec3.atLowerCornerOf(b.offset(23,13,2))));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native dressing sections are ready")).thenExecute(()->{
            GhostsSet.reader(p,b,f.own(p,P));var source=f.in.getBlockEntity(b.offset(LiteraryRooms.source(P)));var original=actor(f,p,LiteraryActor.GHOST_MARJORIE);UUID body=original.getUUID();
            var state=f.data().state(GhostsSetDressing.STATE);state.remove(Long.toString(b.asLong()));f.data().setState(GhostsSetDressing.STATE,state);
            f.in.setBlock(b.offset(-12,9,-26),Blocks.AIR.defaultBlockState(),3);f.in.setBlock(b.offset(-12,10,-26),GhostsSetBlock.of(GhostsSetBlock.Kind.LAMP,Direction.NORTH),3);f.in.setBlock(b.offset(-11,5,-41),Blocks.AIR.defaultBlockState(),3);
            var ceilingEdit=b.offset(2,10,-30);f.in.setBlock(ceilingEdit,Blocks.GOLD_BLOCK.defaultBlockState(),3);var retained=(GhostsCaseBlockEntity)f.in.getBlockEntity(b.offset(17,0,-12));var notes=new ItemStack(Items.PAPER,2);notes.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Saved case original"));retained.setItem(0,notes.copy());
            var oldCase=b.offset(18,0,-33);f.in.setBlock(oldCase,GhostsSetBlock.of(GhostsSetBlock.Kind.CASE,Direction.WEST),3);
            // Reusing this native scene between batches can drop the preceding fixture's lectern original.
            // Recover actual loose originals through normal pickup before testing vacancy; never delete them.
            for(var loose:f.in.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(Vec3.atLowerCornerOf(b.offset(-21,-5,-47)),Vec3.atLowerCornerOf(b.offset(22,13,1))))){p.teleportTo(loose.getX(),loose.getY(),loose.getZ());loose.setNoPickUpDelay();loose.playerTouch(p);h.assertTrue(!loose.isAlive(),"the fixture recovers its preceding loose original through native pickup");}
            p.setGameMode(GameType.SPECTATOR);p.setCamera(original);f.at(p,P,100,0,-8);h.assertTrue(!GhostsSetDressing.repair(f.in,b),"an actual borrowed spectator camera pauses the structural dressing");p.setCamera(null);f.at(p,P,100,0,-8);
            boolean done=false;for(int n=0;n<100&&!done;n++)done=GhostsSetDressing.repair(f.in,b);h.assertTrue(done,"bounded dressing finishes after observers leave: body="+p.position()+", camera="+p.getCamera().position()+", residents="+f.in.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new AABB(Vec3.atLowerCornerOf(b.offset(-21,-5,-47)),Vec3.atLowerCornerOf(b.offset(22,13,1))),e->e.isAlive()&&!(e instanceof LiteraryActor)).stream().map(e->e.getType()+" at "+e.position()).toList());
            h.assertTrue(f.in.getBlockEntity(b.offset(LiteraryRooms.source(P)))==source&&f.in.getEntity(body)==original&&f.in.getBlockState(ceilingEdit).is(Blocks.GOLD_BLOCK),"the exact source entity, cast body and custom ceiling cell survive");h.assertTrue(f.in.getBlockEntity(b.offset(17,0,-12))==retained&&ItemStack.matches(retained.getItem(0),notes)&&f.in.getBlockEntity(oldCase) instanceof GhostsCaseBlockEntity,"existing case stock stays exact while an original old case becomes usable empty storage");
            h.assertTrue(f.in.getBlockState(b.offset(-12,9,-26)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.LAMP&&f.in.getBlockState(b.offset(-12,10,-26)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.CEILING&&f.in.getBlockState(b.offset(-11,5,-41)).getValue(GhostsSetBlock.KIND)==GhostsSetBlock.Kind.COUNTER,"lamps meet the lowered ceiling and the borrowed hat has physical support");
            f.in.setBlock(b.offset(-11,5,-41),Blocks.AIR.defaultBlockState(),3);retained.clearContent();f.reload();GhostsSetDressing.clear();h.assertTrue(GhostsSetDressing.repair(f.in,b)&&f.in.getBlockState(b.offset(-11,5,-41)).isAir()&&retained.isEmpty(),"the saved checkpoint never restores a removed prop or replenishes emptied stock");h.succeed();
        });
    }
    @AfterBatch(batch="ghosts_dressing0488") public static void dressingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="ghosts_recorded_review0488",timeoutTicks=200)
    public static void EveryDayReviewsOnlyItsActualRecordedSubjects(GameTestHelper h){
        String[] first={"Marjorie","UnderTable","Handholds","PrivateConfession","Evaluation","DrawerMechanism","DinnerMouth"};
        String[] second={"Merry","InjuredHand","Threat","Playhouse","MotherObjection","FrozenLeap","MerryDeparture"};
        String[] omitted={"marjorie's interview","beneath the table","damaged plaster","put down","answers are placed","mechanism","messy mouth"};
        var own=new CompoundTag();var prior=new HashSet<String>();
        for(int day=1;day<=7;day++)for(boolean raw:new boolean[]{false,true}){
            var log=new CompoundTag();log.putInt("Frames_"+first[day-1],40);own.put("FilmLog"+day,log);
            String a=words(GhostsSet.edit("reader",own,day,raw));log=new CompoundTag();log.putInt("Frames_"+second[day-1],40);own.put("FilmLog"+day,log);
            String b=words(GhostsSet.edit("reader",own,day,raw));h.assertTrue(!a.equals(b),"recording a different subject changes that day's actual "+(raw?"log":"cut")+", day "+day);
            h.assertTrue(!b.toLowerCase(Locale.ROOT).contains(omitted[day-1]),"an unrecorded subject never appears in the other summary, day "+day+": "+b);
            h.assertTrue(prior.add(b),"each day has distinct subject-specific review language");
        }
        own.putInt("FilmDay",7);var log=new CompoundTag();log.putInt("Frames_MerryDeparture",19);own.put("FilmLog7",log);
        h.assertTrue(!words(GhostsSet.edit("reader",own,7,true)).contains("walks through the front door"),"an unusably brief glimpse does not fabricate a recorded departure");h.succeed();
    }
    @GameTest(template="empty",batch="ghosts_conversations0488",timeoutTicks=200)
    public static void AllSixSpeakersChangeEachDayAndKeepTheirOwnConversationCursor(GameTestHelper h){
        for(int speaker=0;speaker<6;speaker++){
            var seen=new HashSet<String>();var own=new CompoundTag();var peer=new CompoundTag();
            for(int day=1;day<=7;day++){own.putInt("FilmDay",day);peer.putInt("FilmDay",day);String first=GhostsConversations.line(own,speaker),next=GhostsConversations.line(own,speaker);
                h.assertTrue(seen.add(first)&&seen.add(next),"each actual speaker has new conversation on each day, role "+speaker+", day "+day);
                h.assertTrue(GhostsConversations.line(peer,speaker).equals(first),"one reader's conversation does not advance a peer's cursor");
                h.assertTrue(GhostsConversations.line(own.copy(),speaker).equals(first),"the saved native tag retains the speaker's own dialogue cursor");
            }
        }h.succeed();
    }
    @GameTest(template="empty",batch="ghosts_final_death0488",timeoutTicks=260)
    public static void PoisoningUnfoldsOnTheOriginalFamilyAndMerryWalksOutAfterTheirDeaths(GameTestHelper h){
        var f=fixture(h);var p=f.player("ghost_final_death",P);var peer=f.player("ghost_final_peer",P);f.source(p,P);
        h.runAtTickTime(30,()->{
            var own=f.own(p,P);own.putInt("FilmDay",7);own.putInt("ShotClock",295);own.putBoolean("CastPlaced",false);own.putBoolean("Rolling",true);f.at(p,P,-4,0,-24);
            GhostsSet.reader(p,f.base(P),own);var ids=new HashMap<Integer,UUID>();for(int role:GhostsSet.ROLES)ids.put(role,actor(f,p,role).getUUID());
            h.assertTrue(actor(f,p,LiteraryActor.GHOST_MARJORIE).phase()==GhostsSet.EATING,"Marjorie eats and teases before the poisoning takes effect");
            own.putInt("ShotClock",625);GhostsSet.reader(p,f.base(P),own);h.assertTrue(actor(f,p,LiteraryActor.GHOST_MARJORIE).phase()==GhostsSet.CHOKING&&actor(f,p,LiteraryActor.GHOST_JOHN).phase()==GhostsSet.EATING,"the three deaths have distinct onset times instead of one instant slump");
            own.putInt("ShotClock",855);GhostsSet.reader(p,f.base(P),own);for(int role:new int[]{LiteraryActor.GHOST_MARJORIE,LiteraryActor.GHOST_JOHN,LiteraryActor.GHOST_SARAH})h.assertTrue(actor(f,p,role).phase()==GhostsSet.DEAD,"each poisoned family member reaches the leaking, still death pose");
            save(f,p,own);f.reload();own=f.own(p,P);GhostsSet.arrive(p,own);for(int i=0;i<230;i++)GhostsSet.reader(p,f.base(P),own);
            var merry=actor(f,p,LiteraryActor.GHOST_MERRY);h.assertTrue(own.getBoolean("MerryLeft0488")&&merry.getZ()>f.base(P).getZ()-14&&merry.phase()==GhostsSet.STAND,"the original surviving Merry physically passes through the front door: step="+own.getInt("MerryExitStep0488")+", at="+merry.position());
            for(int role:GhostsSet.ROLES)h.assertTrue(ids.get(role).equals(actor(f,p,role).getUUID()),"death and departure preserve each native family identity");
            var position=merry.position();save(f,p,own);f.reload();GhostsSet.reader(p,f.base(P),f.own(p,P));h.assertTrue(merry.position().equals(position),"a finished or interrupted return does not reseat Merry beside the bodies");
            h.assertTrue(f.own(peer,P).getInt("FilmDay")==1&&actor(f,peer,LiteraryActor.GHOST_MARJORIE).phase()!=GhostsSet.DEAD&&!own.getBoolean("Ready"),"another reader's living family and the original seven-day account gate remain independent");h.succeed();
        });
    }
    @AfterBatch(batch="ghosts_final_death0488") public static void finalDeathDone(ServerLevel l){close();}
}
