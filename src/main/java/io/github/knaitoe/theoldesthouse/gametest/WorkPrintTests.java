package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.203, Work print: the closed video shop, played through its own controller with real readers in it. The card has the
 * reader's name; the tape watched through starts a seven-day count the phone keeps; a dubbed copy watched by another reader
 * passes the count on; the last scene shot on the set and spliced in finishes the film; a count that runs out takes the reader
 * from an ordinary hall onto the set.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class WorkPrintTests {
    private WorkPrintTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.WORK_PRINT;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);return active;}
    private static void close(){if(active!=null){active.close();active=null;}}

    private static int slot(ServerPlayer p,Item item){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))return i;return -1;}
    /** Takes the first stack of an item into the main hand. */
    private static void hold(GameTestHelper h,ServerPlayer p,Item item){int i=slot(p,item);h.assertTrue(i>=0,"the reader carries "+item);var inv=p.getInventory();if(i==inv.selected)return;
        var stack=inv.getItem(i).copy();inv.setItem(i,ItemStack.EMPTY);var old=p.getMainHandItem().copy();p.setItemInHand(InteractionHand.MAIN_HAND,stack);if(!old.isEmpty())inv.add(old);}
    /** A reader whose count is already running: watched once, on the House day before today. */
    private static void counting(LiteraryTests.Fixture f,ServerPlayer p,int daysAgo){
        var own=f.own(p,P);own.putInt("Viewings",1);own.putLong("FirstDay",HouseCalendar.today(f.in.getServer())-daysAgo);own.putInt("Visit",1);own.putInt("Beat",1);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
    }
    /** Watches the playing tape through: the reader's eyes stay on it, and the rest of it runs at once. */
    private static void runOut(ServerPlayer p){WorkPrint.fastForward(p,WorkPrint.REEL_TICKS);}

    @GameTest(template="empty",batch="work_print_card",timeoutTicks=400)
    public static void theShopIsALiterarySurvivalSourceAndTheRentalCardHasTheReadersOwnName(GameTestHelper h){
        var f=fixture(h);var p=f.player("work_print_card",P);var l=f.in;var b=f.base(P);
        h.assertTrue(LiteraryRooms.isLiterary(P)&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&WitnessAccount.Story.of(P.id())==WitnessAccount.Story.WORK_PRINT&&WitnessAccount.Story.WORK_PRINT.kind.equals("survival"),"a literary survival source over several visits");
        h.assertTrue(LiteraryRooms.source(P).equals(WorkPrintShop.CARD)&&LiteraryRooms.ending(P).equals(WorkPrintShop.LEDGER),"the rental card is the source and the returns ledger the account");
        h.assertTrue(WorkPrintBlock.is(l.getBlockState(b.offset(WorkPrintShop.CARD)),WorkPrintBlock.Kind.CARD)&&WorkPrintBlock.is(l.getBlockState(b.offset(WorkPrintShop.RETURNS)),WorkPrintBlock.Kind.RETURNS)
            &&l.getBlockState(b.offset(WorkPrintShop.CRT)).getValue(WorkPrintBlock.STAGE)==1&&l.getBlockState(b.offset(WorkPrintShop.MANNEQUIN)).getValue(WorkPrintBlock.STAGE)==0,"the card on the counter, the bin by the door, the set showing snow and the costume on its mannequin");
        h.assertTrue(WorkPrintBlock.is(l.getBlockState(b.offset(WorkPrintShop.MARK)),WorkPrintBlock.Kind.MARK)&&WorkPrintBlock.is(l.getBlockState(b.offset(WorkPrintShop.TRIPOD)),WorkPrintBlock.Kind.TRIPOD),"the set's mark and the camera on its tripod");
        f.source(p,P);
        var original=ItemStack.parseOptional(l.registryAccess(),f.own(p,P).getCompound("Original_Source"));var book=original.get(DataComponents.WRITTEN_BOOK_CONTENT);
        h.assertTrue(book!=null&&book.pages().stream().anyMatch(page->page.raw().getString().contains("work_print_card")),"the borrower's line has the reader's own name on it");
        h.assertTrue(f.own(p,P).getBoolean("Read_Source")&&!WorkPrint.running(f.own(p,P)),"reading the card starts nothing; the tape does");
        h.succeed();
    }
    @AfterBatch(batch="work_print_card") public static void cardDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="work_print_tape",timeoutTicks=600)
    public static void watchingTheTapeThroughStartsTheSevenDayCountAndThePhoneCountsIt(GameTestHelper h){
        var f=fixture(h);var p=f.player("work_print_tape",P);
        h.startSequence()
            .thenExecute(()->{f.click(p,P,WorkPrintShop.RETURNS);h.assertTrue(slot(p,WorkPrintRegistry.TAPE.get())>=0,"the tape is in the returns bin");
                hold(h,p,WorkPrintRegistry.TAPE.get());f.click(p,P,WorkPrintShop.CART);h.assertTrue(WorkPrint.viewing(p.getUUID()),"in the deck, it plays on the set in the back room");
                h.assertTrue(f.in.getBlockState(f.base(P).offset(WorkPrintShop.CRT)).getValue(WorkPrintBlock.STAGE)==2,"the screen is playing");f.look(p,f.base(P).offset(WorkPrintShop.CRT));runOut(p);})
            .thenWaitUntil(()->h.assertTrue(!WorkPrint.viewing(p.getUUID()),"the tape runs to its end"))
            .thenExecute(()->{var own=f.own(p,P);long today=HouseCalendar.today(f.in.getServer());
                h.assertTrue(own.getInt("Viewings")==1&&own.getLong("FirstDay")==today&&WorkPrint.running(own)&&WorkPrint.left(own,today)==7,"watched through, the count starts at seven");
                h.assertTrue(own.getBoolean("Ringing")&&f.in.getBlockState(f.base(P).offset(WorkPrintShop.PHONE)).getValue(WorkPrintBlock.STAGE)==1,"and the shop's phone rings");
                h.assertTrue(slot(p,WorkPrintRegistry.TAPE.get())>=0,"the tape comes back out of the deck");
                h.assertTrue(LabyrinthDealer.dealWeight(f.data(),p.getUUID(),P)==LabyrinthDealer.UNFINISHED_WEIGHT*3,"while the count runs the shop comes back far more often");
                f.click(p,P,WorkPrintShop.PHONE);own=f.own(p,P);
                h.assertTrue(own.getInt("Calls")==1&&own.getLong("LastCall")==today&&!own.getBoolean("Ringing"),"picked up, the line gives only the count");})
            .thenSucceed();
    }
    @AfterBatch(batch="work_print_tape") public static void tapeDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="work_print_dub",timeoutTicks=800)
    public static void aCopyDubbedOnTheTwoDecksAndWatchedByAnotherReaderPassesTheCountOn(GameTestHelper h){
        var f=fixture(h);var a=f.player("work_print_dubber",P);var c=f.player("work_print_viewer",P);
        h.startSequence()
            .thenExecute(()->{counting(f,a,1);f.click(a,P,WorkPrintShop.RETURNS);f.click(a,P,WorkPrintShop.BLANKS);
                hold(h,a,WorkPrintRegistry.BLANK.get());f.click(a,P,WorkPrintShop.CART);h.assertTrue(f.own(a,P).getBoolean("DeckB"),"a blank goes into the second deck");
                hold(h,a,WorkPrintRegistry.TAPE.get());f.click(a,P,WorkPrintShop.CART);h.assertTrue(WorkPrint.viewing(a.getUUID())&&f.in.getBlockState(f.base(P).offset(WorkPrintShop.CART)).getValue(WorkPrintBlock.STAGE)==3,"the tape in the first deck dubs");runOut(a);})
            .thenWaitUntil(()->h.assertTrue(!WorkPrint.viewing(a.getUUID())&&slot(a,WorkPrintRegistry.COPY.get())>=0,"the second deck gives a copy"))
            .thenExecute(()->{h.assertTrue(WorkPrint.running(f.own(a,P)),"making a copy is not passing it on");
                int i=slot(a,WorkPrintRegistry.COPY.get());var copy=a.getInventory().getItem(i).copy();a.getInventory().setItem(i,ItemStack.EMPTY);c.setItemInHand(InteractionHand.MAIN_HAND,copy);
                f.click(c,P,WorkPrintShop.CART);h.assertTrue(WorkPrint.viewing(c.getUUID()),"the other reader plays the copy");runOut(c);})
            .thenWaitUntil(()->h.assertTrue(!WorkPrint.viewing(c.getUUID()),"the copy runs to its end"))
            .thenExecute(()->{var mine=f.own(a,P);var theirs=f.own(c,P);
                h.assertTrue(mine.getBoolean("CountDone")&&mine.getBoolean("Ready")&&mine.getString("Outcome").equals("passed_it_on")&&mine.getString("PassedTo").equals("work_print_viewer"),"the count is the viewer's now; the account is ready for the one who passed it on");
                h.assertTrue(WorkPrint.running(theirs)&&theirs.getInt("Viewings")==1,"and the viewer's count has begun");
                h.assertTrue(slot(a,WorkPrintRegistry.COSTUME.get())>=0,"the costume is the dubber's to keep");})
            .thenSucceed();
    }
    @AfterBatch(batch="work_print_dub") public static void dubDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="work_print_turn",timeoutTicks=1000)
    public static void theTurnShotOnTheSetAndSplicedInFinishesTheFilmAndTheLedgerResolvesIt(GameTestHelper h){
        var f=fixture(h);var p=f.player("work_print_turn",P);var b=f.base(P);
        Runnable onMark=()->{var at=Vec3.atBottomCenterOf(b.offset(WorkPrintShop.MARK));p.moveTo(at.x,at.y,at.z,180,0);p.setYHeadRot(180);p.setDeltaMovement(Vec3.ZERO);};
        h.startSequence()
            .thenExecute(()->{counting(f,p,2);f.source(p,P);f.click(p,P,WorkPrintShop.MANNEQUIN);h.assertTrue(slot(p,WorkPrintRegistry.COSTUME.get())>=0,"the costume comes down off the mannequin");
                int i=slot(p,WorkPrintRegistry.COSTUME.get());var suit=p.getInventory().getItem(i).copy();p.getInventory().setItem(i,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.CHEST,suit);
                h.assertTrue(WorkPrint.wearing(p),"and it is worn");onMark.run();})
            .thenIdle(70)
            .thenExecute(()->{h.assertTrue(WorkPrint.unseen(p),"still in the costume, nothing that hunts by sight takes the wearer for prey");
                h.assertTrue(!f.own(p,P).getBoolean("SceneShot"),"held with the back to the camera, it has not turned yet");p.setYRot(0);p.setYHeadRot(0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("SceneShot")&&slot(p,WorkPrintRegistry.SCENE.get())>=0&&f.own(p,P).getByteArray("SceneFrame").length==WorkPrint.FW*WorkPrint.FH,"it turns round, and the tripod's camera has the shot"))
            .thenExecute(()->{hold(h,p,WorkPrintRegistry.SCENE.get());f.click(p,P,WorkPrintShop.SPLICER);h.assertTrue(f.own(p,P).getBoolean("Spliced"),"88 is spliced into the work print");
                f.click(p,P,WorkPrintShop.FLATBED);h.assertTrue(WorkPrint.viewing(p.getUUID()),"the whole film runs on the flatbed");runOut(p);})
            .thenWaitUntil(()->h.assertTrue(!WorkPrint.viewing(p.getUUID()),"to the end of 88"))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(own.getBoolean("CountDone")&&own.getBoolean("Ready")&&own.getString("Outcome").equals("finished_the_film"),"the count stops and the account is ready");
                h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WORK_PRINT),"nothing resolves until the ledger is read");
                f.read(p,P,WorkPrintShop.LEDGER);
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WORK_PRINT)&&f.own(p,P).getBoolean("Completed"),"reading the ledger resolves the film for this reader");})
            .thenSucceed();
    }
    @AfterBatch(batch="work_print_turn") public static void turnDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="work_print_lapse",timeoutTicks=400)
    public static void aCountThatRunsOutTakesTheReaderFromAnOrdinaryHallOntoTheSet(GameTestHelper h){
        var f=fixture(h);var p=f.player("work_print_lapse",P);var b=f.base(P);
        h.startSequence()
            .thenExecute(()->{counting(f,p,WorkPrint.DAYS);var hall=LabyrinthPlaces.base(f.origin,LabyrinthPlace.STRAIGHT_HALL);p.teleportTo(f.in,hall.getX()+.5,hall.getY(),hall.getZ()-3.5,180,0);p.setDeltaMovement(Vec3.ZERO);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Lapsed"),"seven days out, in an ordinary hall, the film takes them"))
            .thenExecute(()->{var own=f.own(p,P);var taken=Vec3.atBottomCenterOf(b.offset(WorkPrintShop.TAKEN));
                h.assertTrue(own.getBoolean("Haunted")&&!own.getBoolean("CountDone"),"it follows them until they stop it properly");
                h.assertTrue(p.position().distanceTo(taken)<1.5,"they come to on the set, at the far end of its hallway: "+p.position().subtract(Vec3.atLowerCornerOf(b)));})
            .thenSucceed();
    }
    @AfterBatch(batch="work_print_lapse") public static void lapseDone(ServerLevel l){close();}
}
