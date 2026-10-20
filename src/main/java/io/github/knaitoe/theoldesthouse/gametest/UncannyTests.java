package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.network.UncannyCameraPayload;
import java.util.*;
import java.util.function.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Native actions, real collisions, finite originals, fuel and reader-local completion for all six new rooms. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_uncanny") @PrefixGameTestTemplate(false)
public final class UncannyTests {
    private static LiteraryTests.Fixture fixture;
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}}
    private static void run(GameTestHelper h,LabyrinthPlace place,BiConsumer<LiteraryTests.Fixture,ServerPlayer> test){
        var f=new LiteraryTests.Fixture(h);fixture=f;var p=f.player("uncanny_reader",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual room and body sections are loaded")).thenExecute(()->{var own=f.own(p,place);UncannyStories.arrive(p,place,own);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);test.accept(f,p);}).thenSucceed();
    }
    private static CompoundTag step(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place){var own=f.own(p,place);UncannyStories.reader(p,f.base(place),place,own);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);return own;}
    private static void age(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place,int value){var own=f.own(p,place);own.putInt("UncannyAge",value);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);}
    private static void click(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place,BlockPos at){f.click(p,place,at);}
    @GameTest(template="empty") public static void sixDistinctSourcesKeepFixedKindsAndTheDerivedQuota(GameTestHelper h){
        h.assertTrue(UncannyRooms.PLACES.size()==6&&WitnessAccount.Story.values().length==52&&WitnessAccount.REQUIRED==39,"six playable sources raise the derived seventy-five-percent quota to thirty-nine");
        int slot=99;for(var p:UncannyRooms.PLACES){h.assertTrue(p.slot()==slot++&&p.isOneShot(),"new sites append without moving any original address");var story=WitnessAccount.Story.of(p.id());h.assertTrue(story!=null&&!story.kind.isBlank(),"each source has one fixed Witness kind");}
        h.assertTrue(WitnessAccount.Story.MISSING_SLEEP.kind.equals("memory")&&WitnessAccount.Story.TUNNEL_PERIPHERY.kind.equals("survival"),"room outcomes do not change Witness kind");h.succeed();
    }
    @GameTest(template="empty") public static void foundTextsKeepEveryWordAcrossReadableNativePages(GameTestHelper h){
        for(var place:UncannyRooms.PLACES){var content=UncannyTexts.source(place).get(DataComponents.WRITTEN_BOOK_CONTENT);String kept=content.pages().stream().map(p->p.raw().getString()).collect(java.util.stream.Collectors.joining(" ")).replaceAll("\\s+"," ").strip();String supplied=UncannyTexts.log(place).replaceAll("\\s+"," ").strip();h.assertTrue(kept.equals(supplied),"the complete supplied log is preserved: "+place);for(var page:content.pages())h.assertTrue(page.raw().getString().length()<=145,"native pages fit without clipped paragraphs");}h.succeed();
    }
    @GameTest(template="empty",batch="uncanny_board",timeoutTicks=300) public static void pryingThePaintedBoardIsPersonalAndExposesRealEarth(GameTestHelper h){run(h,LabyrinthPlace.PAINTED_INTERIOR,(f,p)->{
        var place=LabyrinthPlace.PAINTED_INTERIOR;f.source(p,place);var peer=f.player("board_peer",place);var own=f.own(p,place);var ids=new HashSet<UUID>();for(int i=0;i<4;i++){var a=UncannyStories.actor(p,own,"CastP"+i);h.assertTrue(a!=null&&a.broadcastToPlayer(p)&&!a.broadcastToPlayer(peer),"each actual Propped body is private");ids.add(a.getUUID());}
        h.assertTrue(ids.size()==4,"four actual originals, not regenerated screen silhouettes");click(f,p,place,UncannyRooms.PARLOR_BOARD);
        h.assertTrue(f.own(p,place).getBoolean("BoardOpened")&&!f.own(peer,place).getBoolean("BoardOpened"),"a peer does not inherit another reader's pry");h.assertTrue(f.level(place).getBlockState(f.base(place).offset(0,1,-30)).is(Blocks.DIRT),"the opening leads to earth, not painted daylight");f.reload();h.assertTrue(f.own(p,place).getUUID("CastP0").equals(own.getUUID("CastP0")),"reload retains the same body");
    });}
    @AfterBatch(batch="uncanny_board") public static void boardDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_camera_bounds",timeoutTicks=300) public static void cameraSamplesFollowTheActualFovAndRejectRemoteOrObserverClaims(GameTestHelper h){run(h,LabyrinthPlace.PAINTED_INTERIOR,(f,p)->{
        var eye=p.getEyePosition();UncannySenses.camera(p,new UncannyCameraPayload(eye.x,eye.y,eye.z,0,0,-1,70,1.5F,-1));h.assertTrue(UncannySenses.fresh(p)&&Math.abs(UncannySenses.halfFov(p)-Math.atan(Math.tan(Math.toRadians(35))*1.5))<.001,"the true viewport sets the peripheral band");
        var saved=p.getPersistentData().getCompound(UncannySenses.LENS).copy();UncannySenses.camera(p,new UncannyCameraPayload(eye.x+100,eye.y,eye.z,0,0,-1,70,1.5F,-1));UncannySenses.camera(p,new UncannyCameraPayload(Double.NaN,eye.y,eye.z,0,0,-1,70,1.5F,-1));h.assertTrue(saved.equals(p.getPersistentData().getCompound(UncannySenses.LENS)),"remote and nonfinite cameras cannot move a periphery");
        p.setGameMode(GameType.SPECTATOR);UncannySenses.camera(p,new UncannyCameraPayload(eye.x,eye.y,eye.z,1,0,0,90,2,-1));h.assertTrue(saved.equals(p.getPersistentData().getCompound(UncannySenses.LENS)),"observer input cannot drive the story");
    });}
    @AfterBatch(batch="uncanny_camera_bounds") public static void lensDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_manifest",timeoutTicks=300) public static void oneWaitressBecomesSolidWithoutReplacingHerNativeBody(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;f.source(p,place);age(f,p,place,300);var own=step(f,p,place);var fake=UncannyStories.actor(p,own,"CastFake");var id=fake.getUUID();h.assertTrue(fake.noPhysics&&fake.isNoGravity()&&!fake.canBeCollidedWith(),"the faint original passes through furniture");
        for(int i=0;i<16;i++)step(f,p,place);age(f,p,place,600);own=step(f,p,place);h.assertTrue(!fake.noPhysics&&fake.canBeCollidedWith()&&!fake.isNoGravity(),"the same original gains native collision");fake.load(fake.saveWithoutId(new CompoundTag()));f.reload();h.assertTrue(UncannyStories.actor(p,f.own(p,place),"CastFake").getUUID().equals(id),"body and component identity survive reload");
    });}
    @AfterBatch(batch="uncanny_manifest") public static void manifestDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_plate",timeoutTicks=500) public static void theServedMealMovesOnceFromCustodyAndRefusalNeedsTheRealWaitress(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;f.source(p,place);age(f,p,place,300);for(int i=0;i<18;i++)step(f,p,place);age(f,p,place,600);
        var own=f.own(p,place);var fake=UncannyStories.actor(p,own,"CastFake");var goal=Vec3.atBottomCenterOf(f.base(place).offset(3,0,-19));for(int i=0;i<90&&!own.getBoolean("MealServed");i++){own=step(f,p,place);}
        h.assertTrue(own.getBoolean("MealServed")&&own.hasUUID("PlateEntity")&&!own.contains("OriginalMeal")&&fake.getMainHandItem().isEmpty(),"the exact served stack leaves saved custody and the hand");
        var plate=(ItemEntity)f.level(place).getEntity(own.getUUID("PlateEntity"));h.assertTrue(plate!=null&&plate.getItem().is(UncannyRegistry.MEAL.get()),"the plate is an actual item in front of the booth");var snapshot=plate.getItem().copy();f.reload();step(f,p,place);h.assertTrue(plate.getItem().getComponents().equals(snapshot.getComponents()),"reload does not regenerate or repaint the meal");
        var real=f.level(place).getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(place),place),a->a.role()==LiteraryActor.UNCANNY_WAITRESS).getFirst();f.at(p,place,-7,0,-8);LiteraryVignettes.talk(p,real);own=step(f,p,place);h.assertTrue(own.getBoolean("Ready")&&!f.own(p,place).getBoolean("UncannyFailed"),"only refusing with the real waitress opens the correct account");
    });}
    @AfterBatch(batch="uncanny_plate") public static void plateDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_food",timeoutTicks=300) public static void eatingHerOriginalMealFailsOnlyItsOwner(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;var peer=f.player("diner_peer",place);f.source(p,place);var meal=ItemStack.parseOptional(p.registryAccess(),f.own(p,place).getCompound("OriginalMeal"));UncannyStories.ate(peer,meal);h.assertTrue(!f.own(peer,place).getBoolean("UncannyFailed"),"a borrowed meal cannot assign its history to a peer");UncannyStories.ate(p,meal);h.assertTrue(f.own(p,place).getBoolean("UncannyFailed")&&!f.own(peer,place).getBoolean("UncannyFailed"),"accepting the meal resolves only its reader's failure");h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SECOND_WAITRESS),"failure supplies no survival or understanding shortcut");
    });}
    @AfterBatch(batch="uncanny_food") public static void foodDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_giant_walk",timeoutTicks=1800) public static void theFoldedOriginalWalksTheRealTunnelAndStopsAtEveryGreen(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.TUNNEL_PERIPHERY;var p=f.player("signal_walker",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));boolean[] begun={false},froze={false};UUID[] id={null};Vec3[] last={null};
        h.onEachTick(()->{if(!begun[0]||f.own(p,place).getBoolean("Ready"))return;var own=f.own(p,place);boolean green=UncannyStories.green(own);p.move(MoverType.SELF,new Vec3(0,0,green?-.24:-.13));var giant=UncannyStories.actor(p,own,"CastGiant");if(giant!=null){h.assertTrue(giant.getUUID().equals(id[0])&&!giant.noPhysics,"one original keeps collision");if(green&&giant.phase()==1&&last[0]!=null&&giant.position().distanceToSqr(last[0])<.00001)froze[0]=true;last[0]=giant.position();}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native tunnel loaded")).thenExecute(()->{f.source(p,place);f.at(p,place,.5,0,-8);var own=f.own(p,place);UncannyStories.arrive(p,place,own);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);id[0]=UncannyStories.actor(p,own,"CastGiant").getUUID();begun[0]=true;})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("Ready"),"the actual body follows the signal route: "+f.own(p,place)))
            .thenExecute(()->{h.assertTrue(froze[0]&&f.own(p,place).getInt("GreenSteps")>=5,"green really freezes the pursuer while the reader gains ground");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.TUNNEL_PERIPHERY),"only the completed passage and own account resolve survival");}).thenSucceed();
    }
    @AfterBatch(batch="uncanny_giant_walk") public static void giantWalkDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_giant_reload",timeoutTicks=300) public static void savedSignalsAndTheStoopedBodyKeepTheirScaleAndKind(GameTestHelper h){run(h,LabyrinthPlace.TUNNEL_PERIPHERY,(f,p)->{
        var place=LabyrinthPlace.TUNNEL_PERIPHERY;f.source(p,place);age(f,p,place,160);var own=step(f,p,place);var a=UncannyStories.actor(p,own,"CastGiant");var id=a.getUUID();a.load(a.saveWithoutId(new CompoundTag()));f.reload();h.assertTrue(a.getUUID().equals(id)&&a.getBbHeight()<=2.8&&a.getScale()==2.5F,"the five-block standing mesh fits the tunnel in its folded native bounds");h.assertTrue(UncannyStories.green(f.own(p,place))&&WitnessAccount.Story.TUNNEL_PERIPHERY.kind.equals("survival"),"save/load preserves signal phase and a fixed Witness kind");
    });}
    @AfterBatch(batch="uncanny_giant_reload") public static void giantLoadDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_sleep",timeoutTicks=300) public static void sleepingCutsOnlyThisNightAndNeverPutsTheReaderInTheEmptyBed(GameTestHelper h){run(h,LabyrinthPlace.MISSING_SLEEP,(f,p)->{
        var place=LabyrinthPlace.MISSING_SLEEP;var peer=f.player("sleep_peer",place);f.source(p,place);long time=f.level(place).getDayTime();click(f,p,place,UncannyRooms.BED_AT);
        h.assertTrue(f.own(p,place).getInt("Sleeps")==1&&f.own(p,place).getInt("NightClock")==7200&&!p.isSleeping(),"a genuine bed interaction moves to a missing interval");h.assertTrue(f.level(place).getDayTime()==time&&f.own(peer,place).getInt("Sleeps")==0,"the world's day and the peer's night do not jump");h.assertTrue(p.position().distanceToSqr(Vec3.atCenterOf(f.base(place).offset(UncannyRooms.BED_AT)))>4,"the reader wakes somewhere the bed camera cannot explain");f.reload();h.assertTrue(UncannyStories.locked(p),"an explicit bed attempt retains its committed night across reload");
    });}
    @AfterBatch(batch="uncanny_sleep") public static void sleepDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_closed_door",timeoutTicks=300) public static void aClosedParentsDoorAloneCannotStandInForBothKindsOfEvidence(GameTestHelper h){run(h,LabyrinthPlace.MISSING_SLEEP,(f,p)->{
        var place=LabyrinthPlace.MISSING_SLEEP;f.source(p,place);click(f,p,place,UncannyRooms.BED_AT);click(f,p,place,UncannyRooms.BED_AT);click(f,p,place,UncannyRooms.PARENTS_DOOR);
        h.assertTrue(!f.own(p,place).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.MISSING_SLEEP),"closing a door without seeing direct and recorded forms grants nothing");h.assertTrue(f.own(p,place).getInt("Sleeps")==2,"the interrupted night keeps both actual bed attempts");
    });}
    @AfterBatch(batch="uncanny_closed_door") public static void bedroomDoorDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_peel",timeoutTicks=600) public static void actualPeelingCostsTheHeldToolAndNeverTakesAnotherReadersProgress(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.LAYERED_MAN;var p=f.player("cellar_reader",place);var peer=f.player("cellar_peer",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));int[] next={0};
        var cells=List.of(new BlockPos(-1,-3,-19),new BlockPos(0,-3,-19),new BlockPos(1,-3,-19),new BlockPos(-1,-4,-19),new BlockPos(0,-4,-19),new BlockPos(1,-4,-19));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"cellar loaded")).thenExecute(()->{f.source(p,place);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(UncannyRegistry.CLEAVER.get()));f.at(p,place,.5,-5,-17);})
            .thenWaitUntil(()->h.assertTrue(next[0]>=6&&f.own(p,place).getBoolean("KeyReleased"),"the opening releases the original key"))
            .thenExecute(()->{h.assertTrue(p.getMainHandItem().getDamageValue()==6&&f.own(p,place).getInt("Peeled")==6&&f.own(peer,place).getInt("Peeled")==0,"native stripping consumes durability and is personal");var id=f.own(p,place).getUUID("KeyEntity");f.reload();step(f,p,place);h.assertTrue(f.own(p,place).getUUID("KeyEntity").equals(id)&&!f.own(p,place).contains("OriginalKey"),"the finite key leaves custody once and is never reconstructed");var key=(ItemEntity)f.level(place).getEntity(id);h.assertTrue(key!=null&&key.getZ()<f.base(place).getZ()-19,"the key lies in the actual opened cavity");key.setPickUpDelay(0);key.playerTouch(p);h.assertTrue(key.isRemoved(),"native pickup transfers the finite original");f.at(p,place,0,0,-6);step(f,p,place);h.assertTrue(f.own(p,place).getBoolean("Ready"),"carrying the actual recovered key upstairs earns the account");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LAYERED_MAN)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.LAYERED_MAN),"the personally recovered key resolves only its reader");}).thenSucceed();
        h.onEachTick(()->{if(p.getMainHandItem().is(UncannyRegistry.CLEAVER.get())&&next[0]<cells.size()&&f.in.getServer().getTickCount()%4==0){var own=f.own(p,place);if(UncannyStories.peel(p,f.base(place),cells.get(next[0]),own)){LiteraryVignettes.save(f.data(),p.getUUID(),place,own);next[0]++;}}});
    }
    @AfterBatch(batch="uncanny_peel") public static void peelDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_growth",timeoutTicks=300) public static void sharedGrowthAndTheOriginalWorkersBrokenToolSurviveReload(GameTestHelper h){run(h,LabyrinthPlace.LAYERED_MAN,(f,p)->{
        var place=LabyrinthPlace.LAYERED_MAN;f.source(p,place);var worker=f.level(place).getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(place),place),a->a.role()==LiteraryActor.UNCANNY_PEELER).getFirst();var id=worker.getUUID();var tool=worker.getMainHandItem();h.assertTrue(!tool.isEmpty()&&tool.getDamageValue()==tool.getMaxDamage()-9,"the actual worker starts with a worn original tool");
        var own=f.own(p,place);own.putInt("UncannyAge",800);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);f.reload();UncannyStories.arrive(p,place,own);h.assertTrue(f.level(place).getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(place),place),a->a.role()==LiteraryActor.UNCANNY_PEELER).size()==1&&worker.getUUID().equals(id),"saved staging never fabricates another exhausted worker");h.assertTrue(UncannyRooms.CELLAR_DOOR.getZ()>-10,"the true upper threshold and emergency ladder remain beyond the growth envelope");
    });}
    @AfterBatch(batch="uncanny_growth") public static void growthDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_light",timeoutTicks=500) public static void reverseMotionHasRealLightAndTheTrueDoorHasNativeHinges(GameTestHelper h){run(h,LabyrinthPlace.MOTION_LIGHT,(f,p)->{
        var place=LabyrinthPlace.MOTION_LIGHT;f.source(p,place);var state=f.level(place).getBlockState(f.base(place).offset(UncannyRooms.FLOOD));h.assertTrue(state.is(UncannyRegistry.PROP.get())&&UncannyPropBlock.light(state.setValue(UncannyPropBlock.STAGE,0))==15&&UncannyPropBlock.light(state.setValue(UncannyPropBlock.STAGE,1))==0,"the floodlight changes native emitted light, not just an overlay");
        h.assertTrue(f.level(place).getBlockState(f.base(place).offset(UncannyRooms.HINGED)).getBlock() instanceof DoorBlock,"the correct exit is an actual hinged door");click(f,p,place,UncannyRooms.HINGED);h.assertTrue(f.own(p,place).getBoolean("HingeSeen")&&!f.own(p,place).getBoolean("Ready"),"touching a hinge before witnessing the standoff is insufficient");
    });}
    @AfterBatch(batch="uncanny_light") public static void lightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_buzzer",timeoutTicks=300) public static void invitingTheVisitorDoesNotInviteOrResolveThePeer(GameTestHelper h){run(h,LabyrinthPlace.MOTION_LIGHT,(f,p)->{
        var place=LabyrinthPlace.MOTION_LIGHT;var peer=f.player("buzzer_peer",place);f.source(p,place);var before=f.own(p,place).getUUID("CastVisitor");click(f,p,place,UncannyRooms.INTERCOM);
        h.assertTrue(f.own(p,place).getBoolean("UncannyFailed")&&!f.own(peer,place).getBoolean("UncannyFailed"),"the brass release is a personal admission");h.assertTrue(before.equals(f.own(p,place).getUUID("CastVisitor"))&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.MOTION_LIGHT),"the same visitor remains and no survival is awarded");
    });}
    @AfterBatch(batch="uncanny_buzzer") public static void buzzerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_mirror",timeoutTicks=400) public static void theMirrorNeedsActualHealthAndItsFuelIsSavedOnlyForThatReader(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var p=f.player("mirror_reader",LabyrinthPlace.MISSING_SLEEP);var peer=f.player("mirror_peer",LabyrinthPlace.MISSING_SLEEP);f.chunks.hold(f.in,IndianLakeRooms.bounds(f.base(LabyrinthPlace.MISSING_SLEEP),LabyrinthPlace.MISSING_SLEEP));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"real reader sections loaded")).thenIdle(80).thenExecute(()->{
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(UncannyRegistry.SMOKY_MIRROR.get()));p.setHealth(20);p.setInvulnerable(false);p.invulnerableTime=0;
            boolean woke=UncannySenses.wakeMirror(p);h.assertTrue(woke&&p.getHealth()==18&&UncannySenses.mirror(p),"native health loss after login protection buys sight; woke="+woke+" health="+p.getHealth()+" abilities="+p.getAbilities().invulnerable);
            f.reload();h.assertTrue(UncannySenses.mirror(p)&&!UncannySenses.mirror(peer),"fuel persists per reader");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(!UncannySenses.mirror(p),"putting the mirror down removes the true view immediately");
        }).thenSucceed();
    }
    @AfterBatch(batch="uncanny_mirror") public static void mirrorDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_mirror_no_blood",timeoutTicks=300) public static void absorptionAndInvulnerabilityCannotBuyBloodSight(GameTestHelper h){run(h,LabyrinthPlace.MISSING_SLEEP,(f,p)->{
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(UncannyRegistry.SMOKY_MIRROR.get()));p.setHealth(20);p.setAbsorptionAmount(4);p.invulnerableTime=0;h.assertTrue(!UncannySenses.wakeMirror(p)&&!UncannySenses.mirror(p)&&p.getHealth()==20,"absorbed damage is not a blood payment");p.setAbsorptionAmount(0);p.setGameMode(GameType.CREATIVE);h.assertTrue(!UncannySenses.wakeMirror(p)&&!UncannySenses.mirror(p),"invulnerability grants no free activation");
    });}
    @AfterBatch(batch="uncanny_mirror_no_blood") public static void noBloodDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_ring",timeoutTicks=300) public static void theRingShowsOnlyTrackedNativeHumanoidsAndNeverGrantsWitness(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var peer=f.player("ring_peer",LabyrinthPlace.SECOND_WAITRESS);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(UncannyRegistry.BONE_RING.get()));var own=f.own(p,LabyrinthPlace.SECOND_WAITRESS);var fake=UncannyStories.actor(p,own,"CastFake");var theirs=UncannyStories.actor(peer,f.own(peer,LabyrinthPlace.SECOND_WAITRESS),"CastFake");f.at(p,LabyrinthPlace.SECOND_WAITRESS,-5,0,-20);
        var view=UncannySenses.view(p);var ids=new HashSet<Integer>();for(var row:view.getList("Sensed",Tag.TAG_COMPOUND))ids.add(((CompoundTag)row).getInt("Id"));
        h.assertTrue(ids.contains(fake.getId())&&!ids.contains(theirs.getId())&&view.getBoolean("Ring"),"a ring senses the actual nearby body without revealing a private peer's mimic");h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"holding a magical item supplies no ending credit");
    });}
    @AfterBatch(batch="uncanny_ring") public static void ringDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_pause",timeoutTicks=300) public static void aReadingMenuPausesThePersonalHazardWithoutRestockingAnything(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;f.source(p,place);age(f,p,place,450);f.click(p,place,LiteraryRooms.source(place));int age=f.own(p,place).getInt("UncannyAge");var original=f.own(p,place).getCompound("OriginalMeal").copy();for(int i=0;i<20;i++)step(f,p,place);h.assertTrue(f.own(p,place).getInt("UncannyAge")==age&&f.own(p,place).getCompound("OriginalMeal").equals(original),"menus pause danger without replacing finite custody");p.closeContainer();
    });}
    @AfterBatch(batch="uncanny_pause") public static void pauseDone(ServerLevel l){close();}
}
