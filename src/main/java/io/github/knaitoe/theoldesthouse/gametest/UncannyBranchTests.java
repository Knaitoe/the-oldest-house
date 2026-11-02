package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.BiConsumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/** Short reading is optional; real discoveries, original custody and physical departures still matter. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_uncanny") @PrefixGameTestTemplate(false)
public final class UncannyBranchTests {
    private static LiteraryTests.Fixture fixture;
    private static void run(GameTestHelper h,LabyrinthPlace place,BiConsumer<LiteraryTests.Fixture,ServerPlayer> test){
        var f=new LiteraryTests.Fixture(h);fixture=f;var p=f.player("branch_reader",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual room and original bodies loaded")).thenExecute(()->{step(f,p,place);test.accept(f,p);}).thenSucceed();
    }
    private static CompoundTag step(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place){var own=f.own(p,place);UncannyStories.reader(p,f.base(place),place,own);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);return own;}
    private static void age(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place,int age){var own=f.own(p,place);own.putInt("UncannyAge",age);if(place==LabyrinthPlace.SECOND_WAITRESS){if(!own.contains("SecondManifestAge131"))own.putInt("SecondManifestAge131",age-10);own.putBoolean("DinerHosted127",true);own.putInt("DinerHostedAt127",0);}LiteraryVignettes.save(f.data(),p.getUUID(),place,own);}
    private static void door(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place,BlockPos rel){var at=f.base(place).offset(rel);p.gameMode.useItemOn(p,f.level(place),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static void look(ServerPlayer p,LiteraryActor a){p.lookAt(EntityAnchorArgument.Anchor.EYES,a.getEyePosition());}
    private static void close(){if(fixture!=null){fixture.close();fixture=null;}}
    @GameTest(template="empty") public static void shortEntriesFitOnePageWhileEveryOriginalRemainsWhole(GameTestHelper h){
        for(var place:UncannyRooms.PLACES){for(int i=0;i<5;i++)for(String first:List.of("camera","hall")){var own=new CompoundTag();own.putString("FirstView",first);var text=UncannyReading.excerpt(place,i,own).get(DataComponents.WRITTEN_BOOK_CONTENT);h.assertTrue(text.pages().size()==1&&text.pages().getFirst().raw().getString().length()<=145,"each contextual passage fits one native page: "+place+"/"+i);}
            h.assertTrue(UncannyTexts.source(place).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>=9,"the complete original account is retained");}
        h.succeed();
    }
    @GameTest(template="empty",batch="branch_reading",timeoutTicks=300) public static void nativePassageButtonsRespectDiscoveriesAndKeepOneImmutableFullOriginal(GameTestHelper h){run(h,LabyrinthPlace.PAINTED_INTERIOR,(f,p)->{
        var place=LabyrinthPlace.PAINTED_INTERIOR;var peer=f.player("branch_book_peer",place);f.click(p,place,LiteraryRooms.source(place));var menu=(LiteraryVignettes.Pages)p.containerMenu;
        h.assertTrue(menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==1&&UncannyReading.readPassage(f.own(p,place))&&!f.own(p,place).getBoolean("Read_Source"),"opening the short native view does not complete the full original");
        var original=f.own(p,place).getCompound("Original_Source").copy();h.assertTrue(!menu.clickMenuButton(peer,UncannyReading.FULL)&&!menu.clickMenuButton(p,UncannyReading.NEXT),"wrong readers and undiscovered later passages are rejected");
        h.assertTrue(menu.clickMenuButton(p,UncannyReading.FULL),"full original can be opened deliberately");menu=(LiteraryVignettes.Pages)p.containerMenu;h.assertTrue(menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>=9,"native full account is the complete original");p.closeContainer();
        f.click(p,place,UncannyRooms.PARLOR_BOARD);f.click(p,place,LiteraryRooms.source(place));menu=(LiteraryVignettes.Pages)p.containerMenu;
        h.assertTrue(menu.clickMenuButton(p,UncannyReading.PREVIOUS),"earlier known passage remains readable");menu=(LiteraryVignettes.Pages)p.containerMenu;h.assertTrue(menu.clickMenuButton(p,UncannyReading.NEXT),"the physical discovery unlocks its short passage");menu=(LiteraryVignettes.Pages)p.containerMenu;
        h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"Keep full account transfers only one finite original");
        int count=0;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.has(DataComponents.WRITTEN_BOOK_CONTENT)){count++;h.assertTrue(s.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>=9,"the collected object is the complete account");}}
        h.assertTrue(count==1&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.PAINTED_INTERIOR),"reading and taking alone grant no encounter credit");p.closeContainer();f.reload();
        h.assertTrue(original.equals(f.own(p,place).getCompound("Original_Source"))&&!f.own(peer,place).getBoolean("BoardOpened")&&!UncannyReading.readPassage(f.own(peer,place)),"reload preserves exact original bytes and separate discoveries");
    });}
    @AfterBatch(batch="branch_reading") public static void readingDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="branch_board",timeoutTicks=300) public static void pryingAndLeavingWorksWithoutReadingOrSeeingEveryCorner(GameTestHelper h){run(h,LabyrinthPlace.PAINTED_INTERIOR,(f,p)->{
        var place=LabyrinthPlace.PAINTED_INTERIOR;var peer=f.player("branch_board_peer",place);f.click(p,place,UncannyRooms.PARLOR_BOARD);
        f.at(p,place,7.5,0,-22.5);door(f,p,place,UncannyRooms.PARLOR_DOOR);p.move(MoverType.SELF,new Vec3(2,0,0));var own=step(f,p,place);
        h.assertTrue(own.getBoolean("Completed")&&own.getInt("EdgeSeen")==0&&!own.getBoolean("Read_Source"),"a real pry and unpainted doorway crossing resolve without a compulsory second clue");
        h.assertTrue(p.getInventory().countItem(UncannyRegistry.BONE_RING.get())==1&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.PAINTED_INTERIOR),"one original reward belongs to the escaping reader");step(f,p,place);h.assertTrue(p.getInventory().countItem(UncannyRegistry.BONE_RING.get())==1,"further ticks cannot restock the ring");
    });}
    @AfterBatch(batch="branch_board") public static void boardDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="branch_diner",timeoutTicks=300) public static void seeingTheTranslucentWaitressPermitsAnActualEarlyFrontExit(GameTestHelper h){run(h,LabyrinthPlace.SECOND_WAITRESS,(f,p)->{
        var place=LabyrinthPlace.SECOND_WAITRESS;var peer=f.player("branch_diner_peer",place);age(f,p,place,300);var own=step(f,p,place);var fake=UncannyStories.actor(p,own,"CastFake");
        f.at(p,place,.5,0,-17.5);look(p,fake);own=step(f,p,place);h.assertTrue(own.getBoolean("SawSecond"),"the reader actually sees the original translucent body: reader="+p.position()+" waitress="+fake.position()+" phase="+fake.phase());var meal=own.getCompound("OriginalMeal").copy();
        f.at(p,place,.5,0,-4.5);p.move(MoverType.SELF,new Vec3(0,0,3));own=step(f,p,place);
        h.assertTrue(own.getBoolean("Completed")&&own.getString("Outcome").equals("left_before_the_plate")&&!own.getBoolean("MealServed")&&!own.getBoolean("Read_Source"),"crossing the native front door before service is a distinct escape");
        h.assertTrue(meal.equals(own.getCompound("OriginalMeal"))&&!own.getBoolean("MealReleased")&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.SECOND_WAITRESS),"early departure neither manufactures a plate nor assigns a peer its history");
    });}
    @AfterBatch(batch="branch_diner") public static void dinerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="branch_tunnel",timeoutTicks=300) public static void advancingAndLookingBackAreSeparatePersonalDiscoveries(GameTestHelper h){run(h,LabyrinthPlace.TUNNEL_PERIPHERY,(f,p)->{
        var place=LabyrinthPlace.TUNNEL_PERIPHERY;var peer=f.player("branch_tunnel_peer",place);age(f,p,place,150);f.at(p,place,.5,0,-136);step(f,p,place);
        for(int i=0;i<8;i++){p.move(MoverType.SELF,new Vec3(0,0,-.4));step(f,p,place);}var own=f.own(p,place);
        h.assertTrue(own.getBoolean("Completed")&&!own.getBoolean("StoopingSeen")&&!own.getBoolean("Read_Source"),"moving through actual green signals needs neither archive nor a compulsory look back");
        age(f,peer,place,150);f.at(peer,place,.5,0,-16);peer.setYRot(180);peer.setYHeadRot(180);step(f,peer,place);var other=step(f,peer,place);look(peer,UncannyStories.actor(peer,other,"CastGiant"));other=step(f,peer,place);
        h.assertTrue(other.getBoolean("StoopingSeen")&&!other.getBoolean("Completed")&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.TUNNEL_PERIPHERY),"looking back unlocks knowledge, not another player's escape credit");
    });}
    @AfterBatch(batch="branch_tunnel") public static void tunnelDone(ServerLevel l){close();}
    /** 0.4.240 (owner: "bent necked woman doesnt properly reset once players exit the vignette"). */
    @GameTest(template="empty",batch="branch_tunnel_home",timeoutTicks=300) public static void theBentNeckedWomanGoesHomeWhenHerReaderLeavesAndWhenTheyComeBack(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.TUNNEL_PERIPHERY;var p=f.player("tunnel_home_reader",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));
        final LiteraryActor[] giant={null};final BlockPos[] home={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual tunnel and her body loaded")).thenExecute(()->{
            step(f,p,place);giant[0]=UncannyStories.actor(p,f.own(p,place),"CastGiant");h.assertTrue(giant[0]!=null,"she waits unseen at the start of the tunnel");home[0]=giant[0].blockPosition();
            // Mid-chase, the reader walks out.
            giant[0].appearance(LiteraryActor.UNCANNY_GIANT,2);TunnelStride.placeOriginal(giant[0],Vec3.atBottomCenterOf(f.base(place).offset(0,0,-40)));
            p.teleportTo(f.level(place),f.base(place).getX()+.5,f.base(place).getY(),f.base(place).getZ()+60,0,0);
        }).thenIdle(25).thenExecute(()->{
            h.assertTrue(giant[0].phase()==0&&giant[0].blockPosition().distManhattan(home[0])<=1,"with her reader gone she is back at her place, unseen: "+giant[0].position()+" phase "+giant[0].phase()+" home "+home[0]);
            // Through, then out and back some time later: she is at her place again, unseen, the passage kept.
            var own=f.own(p,place);own.putBoolean("TunnelPassed130",true);own.putBoolean("Completed",true);own.putLong("TunnelSeen201",f.level(place).getGameTime()-200);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            f.at(p,place,.5,0,-6);giant[0].appearance(LiteraryActor.UNCANNY_GIANT,2);TunnelStride.placeOriginal(giant[0],Vec3.atBottomCenterOf(f.base(place).offset(0,0,-40)));
            own=step(f,p,place);
            h.assertTrue(giant[0].phase()==0&&giant[0].blockPosition().distManhattan(home[0])<=1&&own.getBoolean("Completed"),"coming back finds her at her place, and the passage is still made: "+giant[0].position());
        }).thenSucceed();
    }
    @AfterBatch(batch="branch_tunnel_home") public static void tunnelHomeDone(ServerLevel l){close();}
    /**
     * 0.4.272 (owner: "She needs to give player 3 seconds of grace so as to not immediately, accidentally touch them/end the
     * vignette. She also needs to properly reset after vignette has concluded").
     */
    @GameTest(template="empty",batch="branch_tunnel_grace",timeoutTicks=600) public static void theBentNeckedWomanGivesThreeSecondsAndIsGoneOnceItEnds(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.TUNNEL_PERIPHERY;var p=f.player("tunnel_grace_reader",place);var b=f.base(place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(b,place));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual tunnel and her body loaded")).thenExecute(()->{
            var own=step(f,p,place);var giant=UncannyStories.actor(p,own,"CastGiant");var home=Vec3.atBottomCenterOf(UncannyStories.giantHome(f.level(place),b));
            h.assertTrue(giant!=null&&giant.phase()==0&&giant.position().distanceTo(home)<.01,"she waits unseen at the start");
            // Well in, looking down the tunnel, at a red light: she comes out behind the reader.
            f.at(p,place,.5,0,-14.5);p.setYRot(180);p.setYHeadRot(180);p.setXRot(0);age(f,p,place,240);own=step(f,p,place);
            h.assertTrue(giant.phase()>0&&own.contains(UncannyStories.OUT),"she is out: phase "+giant.phase());
            // The reader turns back and walks straight into her inside her three seconds: nothing ends.
            f.at(p,place,.5,0,-2.2);
            for(int i=0;i*5<UncannyStories.GRACE-5;i++){own=step(f,p,place);h.assertTrue(!UncannyStories.failed(own)&&UncannyStories.grace(own)&&giant.phase()==1,"no touch inside the three seconds: step "+i+" age "+own.getInt("UncannyAge"));}
            // The three seconds are up, the light is red, and they are still within reach.
            own=step(f,p,place);
            h.assertTrue(UncannyStories.failed(own)&&own.getString("Failure").equals("paced_to_the_last_red_light"),"after them, at red, her touch ends it: "+own);
            h.assertTrue(giant.phase()==0&&giant.position().distanceTo(home)<.01&&!giant.isPickable(),"and she is gone from where it ended, back at her place unseen: "+giant.position()+" phase "+giant.phase());
            own=step(f,p,place);h.assertTrue(giant.phase()==0&&giant.position().distanceTo(home)<.01,"she stays there while the caught reader is still inside");
            // Back after time away: a fresh attempt. Mid-chase they make the last green signal; she is at her place at once.
            own.putLong("TunnelSeen201",f.level(place).getGameTime()-UncannyStories.AWAY_TICKS-20);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);own=step(f,p,place);
            h.assertTrue(!UncannyStories.failed(own),"coming back starts it over");
            own.putBoolean("TunnelStarted127",true);own.putInt("GreenSteps",5);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);
            giant.appearance(LiteraryActor.UNCANNY_GIANT,2);TunnelStride.placeOriginal(giant,Vec3.atBottomCenterOf(b.offset(0,0,-120)));
            f.at(p,place,.5,0,-139.5);own=step(f,p,place);
            h.assertTrue(own.getBoolean("TunnelPassed130")&&giant.phase()==0&&giant.position().distanceTo(home)<.01,"through the last green, she is back at her place unseen: "+giant.position()+" phase "+giant.phase()+" "+own);
            // Walking back the way they came, they never find her standing anywhere.
            f.at(p,place,.5,0,-6);giant.appearance(LiteraryActor.UNCANNY_GIANT,2);TunnelStride.placeOriginal(giant,Vec3.atBottomCenterOf(b.offset(0,0,-40)));own=step(f,p,place);
            h.assertTrue(giant.phase()==0&&giant.position().distanceTo(home)<.01,"once it is over she stays at her place, unseen: "+giant.position()+" phase "+giant.phase());
        }).thenSucceed();
    }
    @AfterBatch(batch="branch_tunnel_grace") public static void tunnelGraceDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="branch_bedroom",timeoutTicks=300) public static void directSightAllowsEarlyDoorInterventionWithoutAFourFeedChecklist(GameTestHelper h){run(h,LabyrinthPlace.MISSING_SLEEP,(f,p)->{
        var place=LabyrinthPlace.MISSING_SLEEP;var peer=f.player("branch_sleep_peer",place);f.click(p,place,UncannyRooms.BED_AT);f.click(p,place,UncannyRooms.BED_AT);
        var doll=UncannyStories.actor(p,f.own(p,place),"CastDoll");f.at(p,place,1.5,5,-23);look(p,doll);var own=step(f,p,place);h.assertTrue(own.getBoolean("MarionetteSeen")&&!own.getBoolean("OwnFaceSeen"),"direct physical view is its own branch");
        f.click(p,place,UncannyRooms.PARENTS_DOOR);own=f.own(p,place);
        h.assertTrue(own.getBoolean("Completed")&&!own.getBoolean("Read_Source")&&own.getInt("FeedsSeen")==0&&!f.level(place).getBlockState(f.base(place).offset(UncannyRooms.PARENTS_DOOR)).getValue(DoorBlock.OPEN),"actually closing the door after seeing the threat resolves without the monitor checklist");
        var closing=UncannyTexts.account("Reader",place,own).get(DataComponents.WRITTEN_BOOK_CONTENT);h.assertTrue(closing.pages().size()==1&&!closing.pages().getFirst().raw().getString().contains("my face")&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.MISSING_SLEEP),"the short closing account never claims unseen footage or a peer's resolution");
    });}
    @AfterBatch(batch="branch_bedroom") public static void bedroomDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="branch_foyer",timeoutTicks=700) public static void inspectingARealFalseFrameLeadsToTheHingedExitWithoutWaitingTwoCycles(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.MOTION_LIGHT;var p=f.player("false_frame_reader",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual courtyard and frame sections are loaded"))
            .thenExecute(()->{f.at(p,place,0,0,-10);f.look(p,f.base(place).offset(0,1,-20));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,place).getInt("FoyerStill131")==1&&f.level(place).getBlockState(f.base(place).offset(UncannyRooms.FALSE_DOORS.get(1))).is(UncannyRegistry.PROP.get()),"actual first stillness reveals a real unhinged frame"))
            .thenExecute(()->{f.click(p,place,UncannyRooms.FALSE_DOORS.get(1));f.at(p,place,3.5,0,-4.5);door(f,p,place,UncannyRooms.HINGED);p.move(MoverType.SELF,new Vec3(2,0,0));var own=step(f,p,place);
                h.assertTrue(!own.getBoolean("Completed")&&!own.getBoolean("Ready")&&own.getBoolean("FalseExamined")&&own.getInt("DarkSeen")<2&&!own.getBoolean("Read_Source"),"inspecting a frame and crossing the inner service door confer nothing before an actual House return");}).thenSucceed();
    }
    @AfterBatch(batch="branch_foyer") public static void foyerDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="branch_abandon",timeoutTicks=300) public static void sealingTheCellarRequiresPersonallyEnteringAndSeeingTheWorker(GameTestHelper h){run(h,LabyrinthPlace.LAYERED_MAN,(f,p)->{
        var place=LabyrinthPlace.LAYERED_MAN;String key=place.id()+":"+f.base(place).asLong();var shared=f.data().stateEntry(UncannyStories.WORLD,key);shared.putInt("Age",850);f.data().setStateEntry(UncannyStories.WORLD,key,shared);
        f.click(p,place,UncannyRooms.CELLAR_DOOR);h.assertTrue(!f.own(p,place).getBoolean("Ready"),"an old shared clock cannot award a passing reader an ending");
        // Restore the normally open door after the unhandled native interaction above.
        UncannyStories.setDoor(f.level(place),f.base(place).offset(UncannyRooms.CELLAR_DOOR),true);
        f.at(p,place,-4.5,-5,-15);shared=f.data().stateEntry(UncannyStories.WORLD,key);var worker=(LiteraryActor)f.level(place).getEntity(shared.getUUID("Peeler"));look(p,worker);var own=step(f,p,place);h.assertTrue(own.getBoolean("WorkerSeen")&&own.getBoolean("CellarEntered"),"the reader actually descends and sees the original worker");
        f.at(p,place,.5,0,-3.5);door(f,p,place,UncannyRooms.CELLAR_DOOR);own=f.own(p,place);
        h.assertTrue(own.getBoolean("Completed")&&own.getBoolean("SealedCellar")&&!own.getBoolean("WorkerRescued")&&!own.getBoolean("Read_Source")&&worker.getY()<f.base(place).getY()-1,"sealing is a personal abandonment, not a fabricated rescue");
    });}
    @AfterBatch(batch="branch_abandon") public static void abandonDone(ServerLevel l){close();}
    @GameTest(template="empty",batch="branch_rescue",timeoutTicks=1500) public static void theOriginalWorkerMustActuallyWalkTheStairsBeforeRescueIsAwarded(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);fixture=f;var place=LabyrinthPlace.LAYERED_MAN;var p=f.player("branch_rescuer",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));
        var cells=new ArrayList<BlockPos>();for(int z=-18;z>=UncannyRooms.KEY_CAVITY.getZ();z--){if(UncannyCellar.authored(0,-5,z))cells.add(new BlockPos(0,-5,z));if(UncannyCellar.authored(0,-4,z)&&z!=UncannyRooms.KEY_CAVITY.getZ())cells.add(new BlockPos(0,-4,z));}
        int[] next={0};boolean[] begun={false};UUID[] id={null};double[] highest={-1000};
        h.onEachTick(()->{if(!begun[0])return;var worker=(LiteraryActor)f.level(place).getEntity(id[0]);if(worker!=null)highest[0]=Math.max(highest[0],worker.getY());
            if(next[0]<cells.size()&&p.server.getTickCount()%8==0){if(!UncannyFleshBlock.is(f.level(place).getBlockState(f.base(place).offset(cells.get(next[0]))))){next[0]++;return;}var own=f.own(p,place);int before=own.getInt("Peeled");UncannyStories.peel(p,f.base(place),cells.get(next[0]),own);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);if(own.getInt("Peeled")>before)next[0]++;}
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"cellar route and original loaded"))
            .thenExecute(()->{step(f,p,place);var shared=f.data().stateEntry(UncannyStories.WORLD,place.id()+":"+f.base(place).asLong());id[0]=shared.getUUID("Peeler");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(UncannyRegistry.CLEAVER.get()));f.at(p,place,.5,-5,-17.5);begun[0]=true;})
            .thenWaitUntil(()->h.assertTrue(next[0]==cells.size()&&f.own(p,place).getBoolean("KeyReleased"),"actual cutting releases the original key"))
            .thenExecute(()->{var own=f.own(p,place);h.assertTrue(p.getMainHandItem().getDamageValue()==0&&own.getInt("Peeled")>0&&own.getInt("Peeled")<=cells.size(),"the real unbreakable blade records only personal cuts without wearing out");var item=(ItemEntity)f.level(place).getEntity(own.getUUID("KeyEntity"));f.at(p,place,.5,-5,-21.5);item.setPickUpDelay(0);item.playerTouch(p);h.assertTrue(item.isRemoved()&&f.own(p,place).getBoolean("Completed")&&p.getY()<f.base(place).getY()-1,"native pickup immediately concludes the vignette while the reader is still below");
                var worker=(LiteraryActor)f.level(place).getEntity(id[0]);f.at(p,place,-3.5,-5,-17.5);LiteraryVignettes.talk(p,worker);h.assertTrue(f.own(p,place).getBoolean("Escorting"),"asking the actual worker after cutting commits an escort");f.reload();f.at(p,place,.5,0,-3.5);step(f,p,place);h.assertTrue(f.own(p,place).getBoolean("Completed")&&!f.own(p,place).getBoolean("WorkerRescued"),"the completed key ending cannot invent an optional rescue");})
            .thenWaitUntil(()->{var worker=(LiteraryActor)f.level(place).getEntity(id[0]);h.assertTrue(f.own(p,place).getBoolean("WorkerRescued"),"original worker must climb the native floor and stair route: "+(worker==null?"missing":worker.position())+" record="+f.own(p,place));})
            .thenExecute(()->{var worker=(LiteraryActor)f.level(place).getEntity(id[0]);var own=f.own(p,place);h.assertTrue(worker!=null&&worker.getUUID().equals(id[0])&&highest[0]>=f.base(place).getY()-.1&&worker.getZ()>f.base(place).getZ()-5&&own.getBoolean("WorkerRescued")&&!own.getBoolean("Read_Source"),"only the same original body's arrival grants rescue without requiring an archive");f.reload();var shared=f.data().stateEntry(UncannyStories.WORLD,place.id()+":"+f.base(place).asLong());h.assertTrue(shared.hasUUID("RescuedBy")&&shared.getUUID("RescuedBy").equals(p.getUUID()),"the actual present escort's attribution survives reload; a previous abandoned escort cannot inherit this rescue");}).thenSucceed();
    }
    @AfterBatch(batch="branch_rescue") public static void rescueDone(ServerLevel l){close();}
}
