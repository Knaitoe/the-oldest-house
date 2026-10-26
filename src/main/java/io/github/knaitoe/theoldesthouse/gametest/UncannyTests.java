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
    private static void age(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place,int value){var own=f.own(p,place);own.putInt("UncannyAge",value);if(place==LabyrinthPlace.SECOND_WAITRESS){own.putBoolean("DinerHosted127",true);own.putInt("DinerHostedAt127",0);}LiteraryVignettes.save(f.data(),p.getUUID(),place,own);}
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
    @GameTest(template="empty",batch="uncanny_plate",timeoutTicks=500) public static void theServedMealMovesOnceAndTheStaffEscapeNeedsTheActualThreshold(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;f.source(p,place);age(f,p,place,300);for(int i=0;i<18;i++)step(f,p,place);age(f,p,place,600);
        var own=f.own(p,place);var fake=UncannyStories.actor(p,own,"CastFake");var goal=Vec3.atBottomCenterOf(f.base(place).offset(3,0,-19));for(int i=0;i<90&&!own.getBoolean("MealServed");i++){own=step(f,p,place);}
        h.assertTrue(own.getBoolean("MealServed")&&own.hasUUID("PlateEntity")&&!own.contains("OriginalMeal")&&fake.getMainHandItem().isEmpty(),"the exact served stack leaves saved custody and the hand");h.assertTrue(own.getBoolean("AskedMeal"),"with the plate down she asks how the meal is");
        var plate=(ItemEntity)f.level(place).getEntity(own.getUUID("PlateEntity"));h.assertTrue(plate!=null&&plate.getItem().is(UncannyRegistry.MEAL.get()),"the plate is an actual item in front of the booth");var snapshot=plate.getItem().copy();f.reload();step(f,p,place);h.assertTrue(plate.getItem().getComponents().equals(snapshot.getComponents()),"reload does not regenerate or repaint the meal");
        var real=f.level(place).getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(place),place),a->a.role()==LiteraryActor.UNCANNY_WAITRESS).getFirst();f.at(p,place,-7,0,-8);LiteraryVignettes.talk(p,real);own=step(f,p,place);h.assertTrue(own.getBoolean("Refused")&&!own.getBoolean("Ready"),"the real waitress gives advice, not an exit credit");
        var door=f.base(place).offset(-10,0,-6);f.at(p,place,-8.5,0,-5.5);p.gameMode.useItemOn(p,f.level(place),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(door.getCenter(),Direction.EAST,door,false));
        h.assertTrue(f.level(place).getBlockState(door).getValue(DoorBlock.OPEN),"the actual staff door opens");p.move(MoverType.SELF,new Vec3(-2.5,0,0));own=step(f,p,place);
        h.assertTrue(own.getBoolean("Ready")&&!own.getBoolean("UncannyFailed")&&own.getString("Outcome").equals("left_by_the_staff_door"),"physically crossing the staff threshold earns the account");
    });}
    @AfterBatch(batch="uncanny_plate") public static void plateDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_food",timeoutTicks=300) public static void eatingHerOriginalMealFailsOnlyItsOwner(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;var peer=f.player("diner_peer",place);f.source(p,place);var meal=ItemStack.parseOptional(p.registryAccess(),f.own(p,place).getCompound("OriginalMeal"));UncannyStories.ate(peer,meal);h.assertTrue(!f.own(peer,place).getBoolean("UncannyFailed"),"a borrowed meal cannot assign its history to a peer");UncannyStories.ate(p,meal);for(int i=0;i<8;i++)step(f,p,place);h.assertTrue(f.own(p,place).getBoolean("UncannyFailed")&&!f.own(peer,place).getBoolean("UncannyFailed"),"accepting the meal resolves only its reader's failure");h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SECOND_WAITRESS),"failure supplies no survival or understanding shortcut");
    });}
    @AfterBatch(batch="uncanny_food") public static void foodDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_answer",timeoutTicks=300) public static void answeringHerWhenSheAsksClearsOnlyThatReadersTable(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;var peer=f.player("answer_peer",place);f.source(p,place);f.source(peer,place);var fake=UncannyStories.actor(p,f.own(p,place),"CastFake");f.at(p,place,fake.getX()-f.base(place).getX(),0,fake.getZ()-f.base(place).getZ()+1.5);
        LiteraryVignettes.talk(p,fake);h.assertTrue(!f.own(p,place).getBoolean("UncannyFailed"),"touching her before she asks anything answers nothing");
        var own=f.own(p,place);own.putBoolean("AskedMeal",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);LiteraryVignettes.talk(p,fake);
        h.assertTrue(f.own(p,place).getBoolean("UncannyFailed")&&f.own(p,place).getString("Failure").equals("answered_her")&&!f.own(peer,place).getBoolean("UncannyFailed")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SECOND_WAITRESS),"answering her question clears that reader's table alone and grants nothing");
    });}
    @AfterBatch(batch="uncanny_answer") public static void answerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_folds",timeoutTicks=1200) public static void tissueRegrowsEngulfsWithoutClosingInsideBodiesAndResetsOnlyAfterVacancy(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.LAYERED_MAN;var p=f.player("fold_reader",place);var late=f.player("fold_latecomer",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));
        var l=f.level(place);var b=f.base(place);var cut=new BlockPos(2,-4,-20);var stood=b.offset(3,-5,-21);long[] left={0};var refuge=b.offset(2,-5,-21);net.minecraft.world.entity.animal.Pig[] animal={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"cellar loaded"))
            .thenExecute(()->{f.at(late,place,40,0,40);f.source(p,place);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(UncannyRegistry.CLEAVER.get()));f.at(p,place,3.42,-5,-20.5);var own=f.own(p,place);h.assertTrue(UncannyStories.peel(p,b,cut,own),"the held blade cuts an actual tissue cell");LiteraryVignettes.save(f.data(),p.getUUID(),place,own);h.assertTrue(l.isEmptyBlock(b.offset(cut)),"cut tissue leaves real open space");})
            .thenWaitUntil(()->h.assertTrue(UncannyFleshBlock.is(l.getBlockState(b.offset(cut))),"the saved cut grows back after its occupied regrowth delay"))
            .thenExecute(()->{h.assertTrue(p.hasEffect(UncannyRegistry.ENGULFED)&&f.own(p,place).getInt("Engulfment")>0,"real side contact applies the custom slowing status and screen pressure");h.assertTrue(!late.hasEffect(UncannyRegistry.ENGULFED)&&f.own(late,place).getInt("Peeled")==0,"a peer inherits neither contact nor cutting credit");h.assertTrue(l.getBlockState(stood).isAir(),"growth does not place a solid cell inside the reader");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getBoolean("UncannyFailed"),"uncut tissue eventually overtakes a reader who keeps touching it"))
            .thenExecute(()->{h.assertTrue(f.own(p,place).getString("Failure").equals("overtaken_by_the_cellar")&&p.getY()>=b.getY()&&!p.hasEffect(UncannyRegistry.ENGULFED),"the engulfed reader returns upstairs with the status released");f.at(p,place,40,0,40);left[0]=l.getGameTime();l.setBlock(refuge,Blocks.AIR.defaultBlockState(),3);animal[0]=EntityType.PIG.create(l);h.assertTrue(animal[0]!=null,"a real living animal occupies the cut recess");animal[0].setPersistenceRequired();animal[0].setNoAi(true);animal[0].setNoGravity(true);animal[0].moveTo(refuge.getX()+.5,refuge.getY(),refuge.getZ()+.5,0,0);h.assertTrue(l.addFreshEntity(animal[0]),"the actual animal is registered in the native cellar");f.extras.add(animal[0]);})
            .thenWaitUntil(()->h.assertTrue(l.getGameTime()-left[0]>220,"the home is genuinely empty for ten seconds"))
            .thenExecute(()->{f.arrive(late,place);h.assertTrue(UncannyFleshBlock.is(l.getBlockState(b.offset(cut)))&&l.getBlockState(stood).isAir()&&l.isEmptyBlock(b.offset(UncannyRooms.KEY_CAVITY)),"a vacant reset restores the authored body and its cavity without retaining the overgrowth");h.assertTrue(l.isEmptyBlock(refuge)&&animal[0].isAlive()&&!l.getBlockCollisions(animal[0],animal[0].getBoundingBox()).iterator().hasNext(),"a reset leaves a living animal's actual cut recess open: block="+l.getBlockState(refuge)+" alive="+animal[0].isAlive()+" body="+animal[0].getBoundingBox()+" colliders="+java.util.stream.StreamSupport.stream(l.getBlockCollisions(animal[0],animal[0].getBoundingBox()).spliterator(),false).map(Object::toString).toList());f.source(late,place);})
            .thenSucceed();
    }
    @AfterBatch(batch="uncanny_folds") public static void foldsDone(ServerLevel l){close();}
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
        var own=f.own(p,place);own.putInt("UncannyAge",own.getInt("FirstSleepAge")+1805);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);own=step(f,p,place);
        h.assertTrue(own.getInt("Sleeps")==0&&!own.getBoolean("UncannyFailed")&&!UncannyStories.locked(p),"a first night left unfinished lapses and unlocks the room; it does not fail it");
    });}
    @AfterBatch(batch="uncanny_sleep") public static void sleepDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_closed_door",timeoutTicks=300) public static void aClosedParentsDoorAloneCannotStandInForBothKindsOfEvidence(GameTestHelper h){run(h,LabyrinthPlace.MISSING_SLEEP,(f,p)->{
        var place=LabyrinthPlace.MISSING_SLEEP;f.source(p,place);click(f,p,place,UncannyRooms.BED_AT);click(f,p,place,UncannyRooms.BED_AT);click(f,p,place,UncannyRooms.PARENTS_DOOR);
        h.assertTrue(!f.own(p,place).getBoolean("Ready")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.MISSING_SLEEP),"closing a door without seeing direct and recorded forms grants nothing");h.assertTrue(f.own(p,place).getInt("Sleeps")==2,"the interrupted night keeps both actual bed attempts");
    });}
    @AfterBatch(batch="uncanny_closed_door") public static void bedroomDoorDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_peel",timeoutTicks=600) public static void actualPeelingCostsTheHeldToolAndNeverTakesAnotherReadersProgress(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.LAYERED_MAN;var p=f.player("cellar_reader",place);var peer=f.player("cellar_peer",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));int[] next={0};int[] legacySlot={-1};CompoundTag[] originalKeyData={null};
        var cells=new ArrayList<BlockPos>();for(int z=-18;z>=UncannyRooms.KEY_CAVITY.getZ();z--){cells.add(new BlockPos(0,-5,z));var headroom=new BlockPos(0,-4,z);if(!headroom.equals(UncannyRooms.KEY_CAVITY))cells.add(headroom);}
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"cellar loaded")).thenExecute(()->{f.source(p,place);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(UncannyRegistry.CLEAVER.get()));f.at(p,place,.5,-5,-17);})
            .thenWaitUntil(()->h.assertTrue(next[0]>=cells.size()&&f.own(p,place).getBoolean("KeyReleased"),"the opening releases the original key"))
            .thenExecute(()->{h.assertTrue(p.getMainHandItem().getDamageValue()==cells.size()&&f.own(p,place).getInt("Peeled")==cells.size()&&f.own(peer,place).getInt("Peeled")==0,"native stripping consumes durability and is personal");var id=f.own(p,place).getUUID("KeyEntity");f.reload();step(f,p,place);h.assertTrue(f.own(p,place).getUUID("KeyEntity").equals(id)&&!f.own(p,place).contains("OriginalKey"),"the finite key leaves custody once and is never reconstructed");var key=(ItemEntity)f.level(place).getEntity(id);h.assertTrue(key!=null&&key.getZ()<f.base(place).getZ()-19,"the key lies in the actual opened cavity");h.assertTrue(key.getItem().is(UncannyRegistry.CELLAR_KEY.get()),"the original is the dedicated cellar key");f.at(p,place,.5,-5,UncannyRooms.KEY_CAVITY.getZ()+.5);key.setPickUpDelay(0);key.playerTouch(p);h.assertTrue(key.isRemoved(),"native pickup transfers the finite original");f.at(p,place,0,0,-6);step(f,p,place);h.assertTrue(f.own(p,place).getBoolean("Ready"),"carrying the actual recovered key upstairs earns the account");f.read(p,place,LiteraryRooms.ending(place));h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.LAYERED_MAN)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.LAYERED_MAN),"the personally recovered key resolves only its reader");for(int i=0;i<p.getInventory().getContainerSize();i++){var current=p.getInventory().getItem(i);if(current.is(UncannyRegistry.CELLAR_KEY.get())){legacySlot[0]=i;originalKeyData[0]=current.get(DataComponents.CUSTOM_DATA).copyTag();var legacy=new ItemStack(LiteraryRegistry.KEY.get());legacy.applyComponents(current.getComponentsPatch());legacy.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Brass keepsake"));p.getInventory().setItem(i,legacy);break;}}h.assertTrue(legacySlot[0]>=0,"the finite carried key is available for legacy migration");peer.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(LiteraryRegistry.KEY.get()));f.at(p,place,40,0,40);})
            .thenWaitUntil(()->h.assertTrue(p.getInventory().getItem(legacySlot[0]).is(UncannyRegistry.CELLAR_KEY.get()),"native ticking converts a completed reader's legacy key outside the cellar"))
            .thenExecute(()->{var migrated=p.getInventory().getItem(legacySlot[0]);var data=migrated.get(DataComponents.CUSTOM_DATA);
                // Native Mother custody tracking may append carrier/time annotations while the migration waits.
                // Every pre-existing original tag must survive unchanged; additional provenance is legitimate.
                h.assertTrue(data!=null,"converted key retains its custom data");for(String field:originalKeyData[0].getAllKeys())h.assertTrue(Objects.equals(data.copyTag().get(field),originalKeyData[0].get(field)),"conversion preserves original key field "+field+": "+data+" against "+originalKeyData[0]);
                h.assertTrue(migrated.getHoverName().getString().equals("Brass keepsake"),"conversion keeps the player-given name: "+migrated.getHoverName().getString());
                h.assertTrue(peer.getOffhandItem().is(LiteraryRegistry.KEY.get()),"conversion leaves an unrelated Wheel key alone: "+peer.getOffhandItem()+", peer alive "+peer.isAlive());}).thenSucceed();
        h.onEachTick(()->{if(p.getMainHandItem().is(UncannyRegistry.CLEAVER.get())&&next[0]<cells.size()&&f.in.getServer().getTickCount()%8==0){var own=f.own(p,place);int before=own.getInt("Peeled");UncannyStories.peel(p,f.base(place),cells.get(next[0]),own);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);if(own.getInt("Peeled")==before+1)next[0]++;}});
    }
    @AfterBatch(batch="uncanny_peel") public static void peelDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_growth",timeoutTicks=300) public static void sharedGrowthAndTheOriginalWorkersBrokenToolSurviveReload(GameTestHelper h){run(h,LabyrinthPlace.LAYERED_MAN,(f,p)->{
        var place=LabyrinthPlace.LAYERED_MAN;f.source(p,place);var worker=f.level(place).getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(place),place),a->a.role()==LiteraryActor.UNCANNY_PEELER).getFirst();var id=worker.getUUID();var tool=worker.getMainHandItem();h.assertTrue(!tool.isEmpty()&&tool.getDamageValue()>=tool.getMaxDamage()-9,"the actual worker starts with a worn original tool");
        var own=f.own(p,place);own.putInt("UncannyAge",800);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);f.reload();UncannyStories.arrive(p,place,own);h.assertTrue(f.level(place).getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(place),place),a->a.role()==LiteraryActor.UNCANNY_PEELER).size()==1&&worker.getUUID().equals(id),"saved staging never fabricates another exhausted worker");h.assertTrue(UncannyRooms.CELLAR_DOOR.getZ()>-10,"the true upper threshold and emergency ladder remain beyond the growth envelope");
    });}
    @AfterBatch(batch="uncanny_growth") public static void growthDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_light",timeoutTicks=500) public static void reverseMotionHasRealLightAndTheTrueDoorHasNativeHinges(GameTestHelper h){run(h,LabyrinthPlace.MOTION_LIGHT,(f,p)->{
        var place=LabyrinthPlace.MOTION_LIGHT;f.source(p,place);var state=f.level(place).getBlockState(f.base(place).offset(UncannyRooms.FLOOD));h.assertTrue(state.is(UncannyRegistry.PROP.get())&&UncannyPropBlock.light(state.setValue(UncannyPropBlock.STAGE,0))==15&&UncannyPropBlock.light(state.setValue(UncannyPropBlock.STAGE,1))==0,"the floodlight changes native emitted light, not just an overlay");
        h.assertTrue(f.level(place).getBlockState(f.base(place).offset(UncannyRooms.HINGED)).getBlock() instanceof DoorBlock,"the correct exit is an actual hinged door");click(f,p,place,UncannyRooms.HINGED);h.assertTrue(f.own(p,place).getBoolean("HingeSeen")&&!f.own(p,place).getBoolean("Ready"),"touching a hinge before witnessing the standoff is insufficient");
        var l=f.level(place);var b=f.base(place);var behind=UncannyRooms.FALSE_DOORS.get(2);var door=b.offset(behind);
        l.setBlock(door,UncannyPropBlock.of(UncannyPropBlock.Kind.FALSE_DOOR,Direction.SOUTH),3);l.setBlock(door.above(),UncannyPropBlock.of(UncannyPropBlock.Kind.FALSE_DOOR,Direction.SOUTH).setValue(UncannyPropBlock.STAGE,1),3);
        var own=f.own(p,place);own.putBoolean("FalseDoors",true);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);var key=place.id()+":"+b.asLong();var data=LabyrinthData.get(l.getServer());
        f.at(p,place,.5,0,-8.5);p.setYRot(180);p.setYHeadRot(180);p.setXRot(0);var shared=data.stateEntry(UncannyStories.WORLD,key);shared.putInt("Age",150);data.setStateEntry(UncannyStories.WORLD,key,shared);step(f,p,place);
        h.assertTrue(l.getBlockState(door).getValue(UncannyPropBlock.STAGE)==2&&(f.own(p,place).getInt("FalseAuto")&4)!=0,"with the light out in front of the reader, the unhinged frame behind them clicks open");
        shared=data.stateEntry(UncannyStories.WORLD,key);shared.putInt("Age",10);data.setStateEntry(UncannyStories.WORLD,key,shared);step(f,p,place);
        h.assertTrue(l.getBlockState(door).getValue(UncannyPropBlock.STAGE)==0&&f.own(p,place).getInt("FalseAuto")==0,"the frame shuts again when the floodlight comes back on");
    });}
    @AfterBatch(batch="uncanny_light") public static void lightDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_buzzer",timeoutTicks=300) public static void invitingTheVisitorDoesNotInviteOrResolveThePeer(GameTestHelper h){run(h,LabyrinthPlace.MOTION_LIGHT,(f,p)->{
        var place=LabyrinthPlace.MOTION_LIGHT;var peer=f.player("buzzer_peer",place);click(f,peer,place,UncannyRooms.INTERCOM);h.assertTrue(!f.own(peer,place).getBoolean("UncannyFailed"),"before the scrawl beside it is read, the button admits nothing");
        f.source(p,place);var before=f.own(p,place).getUUID("CastVisitor");click(f,p,place,UncannyRooms.INTERCOM);
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
    @GameTest(template="empty",batch="uncanny_presence",timeoutTicks=400) public static void theTunnelWomanStartsUnseenAfterTheFirstSignalWithoutReading(GameTestHelper h){run(h,LabyrinthPlace.TUNNEL_PERIPHERY,(f,p)->{
        var place=LabyrinthPlace.TUNNEL_PERIPHERY;f.at(p,place,.5,0,-14.5);var own=f.own(p,place);own.putInt("UncannyAge",240);var giant=UncannyStories.actor(p,own,"CastGiant");h.assertTrue(giant!=null&&giant.phase()==0&&giant.getZ()<f.base(place).getZ()-60,"the same saved woman remains dormant away from the entrance");double before=giant.distanceToSqr(p);
        for(int i=0;i<8;i++)UncannyStories.reader(p,f.base(place),place,own);
        h.assertTrue(!own.getBoolean("Read_Source")&&giant.phase()==2&&giant.distanceToSqr(p)<before,"red-light pursuit works without a paper-reading gate");h.assertTrue(giant.getBbHeight()<=2.8&&giant.getY()>=f.base(place).getY()-.01,"the stooped collision body stays inside the actual tunnel");
    });}
    @AfterBatch(batch="uncanny_presence") public static void presenceDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_diner_exit",timeoutTicks=400) public static void theDinerHasTwoRealReachableExteriorDoors(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;var l=f.level(place);var b=f.base(place);var staff=b.offset(-10,0,-6);h.assertTrue(l.getBlockState(b.offset(0,0,-3)).getBlock() instanceof DoorBlock&&l.getBlockState(staff).getBlock() instanceof DoorBlock,"the front and staff exits are separate native doorways");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);f.at(p,place,-8.5,0,-5.5);p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(staff.getCenter(),Direction.EAST,staff,false));h.assertTrue(l.getBlockState(staff).getValue(DoorBlock.OPEN),"ordinary block use opens the staff door beside the till");
        p.move(MoverType.SELF,new Vec3(-3,0,0));h.assertTrue(p.getX()<staff.getX()&&!l.getBlockState(p.blockPosition().below()).isAir(),"the staff door actually crosses to a supported outside landing");
    });}
    @AfterBatch(batch="uncanny_diner_exit") public static void dinerExitDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_house_safety",timeoutTicks=500) public static void theHomeBasementHasNoStairPitsAndItsTwoRoutesReachRealLandings(GameTestHelper h){run(h,LabyrinthPlace.LAYERED_MAN,(f,p)->{
        var place=LabyrinthPlace.LAYERED_MAN;var l=f.level(place);var b=f.base(place);
        for(int x=-8;x<=8;x++)for(int z=-30;z<=-1;z++)h.assertTrue(!l.getBlockState(b.offset(x,-7,z)).isAir(),"the house foundation closes every lower void at "+x+","+z);
        h.assertTrue(l.getBlockState(b.offset(-4,0,-24)).getBlock() instanceof BedBlock&&l.getBlockState(b.offset(6,0,-7)).is(UncannyRegistry.FURNITURE.get()),"the upper floor is a furnished home with a bed and kitchen");
        p.setNoGravity(false);f.at(p,place,.5,0,-5.5);for(int i=0;i<60;i++)p.move(MoverType.SELF,new Vec3(0,-.55,-.2));h.assertTrue(p.getY()>=b.getY()-5.01&&p.getY()<b.getY()-4.8&&p.getZ()<b.getZ()-11,"ordinary collision descends the five supported stair treads into the cellar: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
        for(int i=0;i<90;i++)p.move(MoverType.SELF,new Vec3(0,-.08,.15));h.assertTrue(p.getY()>=b.getY()-.01&&p.getZ()>b.getZ()-6,"native step-up returns from the cellar to the home: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
        f.at(p,place,-5.5,-5,-9.5);h.assertTrue(p.onClimbable(),"the emergency route starts on an actual backed ladder");
        for(int y=-5;y<=0;y++)h.assertTrue(l.getBlockState(b.offset(-6,y,-10)).getBlock() instanceof LadderBlock&&!l.getBlockState(b.offset(-7,y,-10)).isAir(),"every rung has a real supporting wall");h.assertTrue(!l.getBlockState(b.offset(-5,-1,-10)).isAir(),"the ladder emerges beside a supported upper landing");
    });}
    @AfterBatch(batch="uncanny_house_safety") public static void houseSafetyDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="uncanny_unread_foyer",timeoutTicks=700) public static void theFoyerCyclesAndRecoversItsSameVisitorWithoutReadingOrDuplicatingIt(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.MOTION_LIGHT;var p=f.player("foyer_arrival",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));UUID[] original={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual courtyard is loaded"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("DarkSeen")>=1,"the native floodlight cycles before the scrawl is read"))
            .thenExecute(()->{var own=f.own(p,place);var a=UncannyStories.actor(p,own,"CastVisitor");h.assertTrue(!own.getBoolean("Read_Source")&&a!=null&&a.phase()==1,"a real visitor becomes visible during darkness without a reading gate");original[0]=a.getUUID();for(int x=-6;x<=6;x++)for(int z=-24;z<=-16;z++){var floor=f.base(place).offset(x,-1,z);h.assertTrue(f.level(place).getBlockState(floor).is(Blocks.GRAVEL)&&f.level(place).getBlockState(floor.below()).isCollisionShapeFullBlock(f.level(place),floor.below()),"the actual gravel yard retains its supported floor after the native light cycles");}for(int x=-2;x<=2;x++)for(int y=0;y<=3;y++)h.assertTrue(f.level(place).getBlockState(f.base(place).offset(x,y,-15)).isAir(),"the courtyard wall leaves a real sightline behind the foyer glazing");h.assertTrue(f.level(place).getBrightness(net.minecraft.world.level.LightLayer.BLOCK,a.blockPosition().above())>=2,"actual propagated light reaches the visitor while the floodlight is off");var spill=f.level(place).getBlockState(f.base(place).offset(-3,3,-12));h.assertTrue(spill.is(UncannyRegistry.FURNITURE.get())&&UncannyFurnitureBlock.light(spill)>0,"a physical foyer light keeps the silhouette readable");a.discard();})
            .thenWaitUntil(()->h.assertTrue(UncannyStories.actor(p,f.own(p,place),"CastVisitor")!=null,"a genuinely missing body is recovered only after its native sections settle"))
            .thenExecute(()->{var a=UncannyStories.actor(p,f.own(p,place),"CastVisitor");h.assertTrue(original[0].equals(a.getUUID())&&f.level(place).getEntitiesOfClass(LiteraryActor.class,IndianLakeRooms.bounds(f.base(place),place),e->e.role()==LiteraryActor.UNCANNY_VISITOR).size()==1,"the saved identity returns once, without a duplicate visitor");})
            .thenSucceed();
    }
    @AfterBatch(batch="uncanny_unread_foyer") public static void unreadFoyerDone(ServerLevel l){close();}

}
