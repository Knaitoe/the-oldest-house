package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.206, the Dead Air Broadcast, played on the set with real readers on it: the printouts and the dial at static; dead air,
 * the crawl under the tall one's arms, the tube from the chest and the receiver; the tall one's touch; the three dishes and the
 * shack; the walls down and the teleprompter; and the booth, the centre monitor and the script.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class DeadAirTests {
    private DeadAirTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.DEAD_AIR;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);DeadAirGame.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}DeadAirGame.clearAll();}
    private static net.minecraft.nbt.CompoundTag set(LiteraryTests.Fixture f){return DeadAirGame.set(f.in.getServer());}
    private static int count(ServerPlayer p,Item item){int n=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item))n+=inv.getItem(i).getCount();return n;}
    private static void hold(ServerPlayer p,Item item){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item)){var s=inv.getItem(i).copy();inv.setItem(i,ItemStack.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,s);return;}}
    private static void hold(LiteraryTests.Fixture f){f.chunks.hold(f.out,new AABB(f.base(P)).inflate(30,30,40).move(0,0,-35));}
    private static void visit(LiteraryTests.Fixture f,ServerPlayer p,int done,boolean fitted){var own=f.own(p,P);own.putInt("Visit",done);own.putInt("Beat",done);own.putBoolean("Fitted206",fitted);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);f.arrive(p,P);}
    private static void basement(LiteraryTests.Fixture f,ServerPlayer p){f.at(p,P,.5,0,-4.5);}

    @GameTest(template="empty",batch="dead_air_printouts",timeoutTicks=400)
    public static void thePrintoutsRememberAShowAndTheDialStartsAtStatic(GameTestHelper h){
        var f=fixture(h);var p=f.player("dead_air_printouts",P);var l=f.out;var b=f.base(P);
        h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.MULTI_VISIT&&WitnessAccount.Story.of(P.id())==WitnessAccount.Story.DEAD_AIR&&WitnessAccount.Story.DEAD_AIR.kind.equals("understanding"),"an outdoor literary understanding source over four visits");
        h.assertTrue(LiteraryRooms.source(P).equals(DeadAirSet.PRINTOUTS)&&LiteraryRooms.ending(P).equals(DeadAirSet.MONITOR),"the printouts are the source and the centre monitor the account");
        h.assertTrue(DeadAirBlock.is(l.getBlockState(b.offset(DeadAirSet.RECEIVER)),DeadAirBlock.Kind.RECEIVER)&&DeadAirBlock.is(l.getBlockState(b.offset(DeadAirSet.PERCY)),DeadAirBlock.Kind.PERCY)&&DeadAirBlock.is(l.getBlockState(b.offset(DeadAirSet.CRT)),DeadAirBlock.Kind.CRT),"the receiver, Percy and the Zenith are in the basement");
        var t=set(f);h.assertTrue(t.getBoolean("Run")&&t.getInt("RunVisit")==1&&t.getInt("Channel")==DeadAirGame.STATIC&&l.getBlockState(b.offset(DeadAirSet.BULKHEAD)).getValue(DeadAirBlock.STAGE)==0,"the set starts on static, the bulkhead locked");
        f.source(p,P);h.assertTrue(f.own(p,P).getBoolean("Read_Source"),"the printouts are read");
        f.click(p,P,DeadAirSet.PERCY);h.assertTrue(count(p,net.minecraft.world.item.Items.WRITTEN_BOOK)>=1||f.own(p,P).getBoolean("Printout206_1"),"Percy has a printout in his lap");
        h.succeed();
    }
    @AfterBatch(batch="dead_air_printouts") public static void printoutsDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="dead_air_tube",timeoutTicks=900)
    public static void deadAirStopsTheTallOneAndTheReaderCrawlsUnderItsArmsForTheTube(GameTestHelper h){
        var f=fixture(h);var p=f.player("dead_air_tube",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the set is resident"))
            .thenExecute(()->{f.click(p,P,DeadAirSet.RECEIVER);var t=set(f);
                h.assertTrue(t.getInt("Channel")==DeadAirGame.STAGE&&f.out.getBlockState(b.offset(DeadAirSet.BULKHEAD)).getValue(DeadAirBlock.STAGE)==1,"tuned to the stage, the bulkhead opens");
                h.assertTrue(f.out.getBlockState(b.offset(-3,1,-20)).is(DeadAirRegistry.material("velvet"))&&DeadAirBlock.is(f.out.getBlockState(b.offset(DeadAirSet.POST)),DeadAirBlock.Kind.SKIN),"the hallway is a theatre's corridor, and the tall one stands in it");
                f.click(p,P,DeadAirSet.LEVER);t=set(f);
                h.assertTrue(t.getInt("Dead")>0&&f.out.getBlockState(b.offset(DeadAirSet.POST)).getValue(DeadAirBlock.STAGE)>=3&&f.out.getBlockState(b.offset(1,1,DeadAirSet.POST.getZ())).getValue(DeadAirBlock.STAGE)==1,"the wall-breaker kills the air: it stops with a body, its arms solid across the hall");
                f.at(p,P,1.5,0,DeadAirSet.POST.getZ()+.5);p.setShiftKeyDown(true);
                h.assertTrue(DeadAirGame.crawling(p,b)&&f.own(p,P).getBoolean("Crawled206"),"beside it, on hands and knees, the reader goes under its arms");p.setShiftKeyDown(false);
                f.click(p,P,DeadAirSet.CHEST);h.assertTrue(count(p,DeadAirRegistry.TUBE.get())==1,"the chest at the stage's end has the brass tube");
                basement(f,p);hold(p,DeadAirRegistry.TUBE.get());f.click(p,P,DeadAirSet.RECEIVER);
                h.assertTrue(f.own(p,P).getBoolean("Fitted206")&&f.own(p,P).getInt("Beat")==1&&count(p,DeadAirRegistry.TUBE.get())==0,"fitted to the receiver, the tube finishes the first visit");
                basement(f,p);})
            .thenWaitUntil(()->h.assertTrue(set(f).getInt("Dead")==0&&f.out.getBlockState(b.offset(DeadAirSet.POST)).getValue(DeadAirBlock.STAGE)<3,"after eight seconds the feedback brings the picture back and the tall one loose"))
            .thenSucceed();
    }
    @AfterBatch(batch="dead_air_tube") public static void tubeDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="dead_air_touch",timeoutTicks=600)
    public static void theTallOnesTouchPutsTheReaderOut(GameTestHelper h){
        var f=fixture(h);var p=f.player("dead_air_touch",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the set is resident"))
            .thenExecute(()->{f.click(p,P,DeadAirSet.RECEIVER);f.at(p,P,.5,0,-16.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Caught206")==1,"walking into the hallway with the air alive, it comes down the hall and touches the reader"))
            .thenExecute(()->{var r=p.position().subtract(b.getX(),b.getY(),b.getZ());h.assertTrue(r.z>-14||!LiteraryVignettes.inside(p,P),"the reader is put out of the hallway");h.assertTrue(f.own(p,P).getInt("Beat")==0,"the visit is still to do");})
            .thenSucceed();
    }
    @AfterBatch(batch="dead_air_touch") public static void touchDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="dead_air_marsh",timeoutTicks=1200)
    public static void onTheMarshTheThreeDishesLockTheMastsAndOpenTheShack(GameTestHelper h){
        var f=fixture(h);var p=f.player("dead_air_marsh",P);var b=f.base(P);
        h.startSequence().thenExecute(()->{hold(f);visit(f,p,1,true);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==2,"the second visit"))
            .thenExecute(()->{f.click(p,P,DeadAirSet.RECEIVER);basement(f,p);f.click(p,P,DeadAirSet.RECEIVER);var t=set(f);
                h.assertTrue(t.getInt("Channel")==DeadAirGame.MARSH&&f.out.getBlockState(b.offset(0,1,DeadAirSet.HALL_END)).isAir(),"with the tube fitted the dial reaches the marsh, and the hallway is a drain open at its end");
                h.assertTrue(t.getInt("Zone")==DeadAirGame.FIELD,"the tall one walks the boardwalks");
                f.click(p,P,DeadAirSet.BREAKERS[0]);h.assertTrue(set(f).getInt("Dead")>0,"a breaker on its pole kills the air");
                for(int i=0;i<3;i++)for(int n=0;n<DeadAirSet.TARGETS[i];n++)f.click(p,P,DeadAirSet.DISHES[i]);
                h.assertTrue(f.own(p,P).getInt("DishMask206")==7,"all three dishes turned to the night man's bearings");
                basement(f,p);})
            .thenWaitUntil(()->h.assertTrue(set(f).getBoolean("Shack")&&f.own(p,P).getInt("Beat")==2,"three cyan locks finish the second visit"))
            .thenExecute(()->{var m=DeadAirSet.MASTS[0];h.assertTrue(f.out.getBlockState(b.offset(m.getX(),DeadAirSet.MAST_TOP+3,m.getZ())).getValue(DeadAirBlock.STAGE)==5,"the masts' bulbs burn cyan");
                h.assertTrue(f.out.getBlockState(b.offset(DeadAirSet.SHACK_DOOR)).getValue(DoorBlock.OPEN),"and the shack's door stands open");})
            .thenSucceed();
    }
    @AfterBatch(batch="dead_air_marsh") public static void marshDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="dead_air_stage",timeoutTicks=600)
    public static void onTheThirdVisitTheWallsAreDownAndTheTeleprompterHasTheReadersOwnLines(GameTestHelper h){
        var f=fixture(h);var p=f.player("dead_air_stage",P);var b=f.base(P);
        h.startSequence().thenExecute(()->{hold(f);var own=f.own(p,P);own.putBoolean("Tuned4_206",true);own.putBoolean("Crawled206",true);own.putInt("DishMask206",7);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);visit(f,p,2,true);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==3,"the third visit"))
            .thenExecute(()->{h.assertTrue(!f.out.getBlockState(b.offset(7,1,-2)).is(DeadAirRegistry.material("walnut"))&&f.out.getBlockState(b.offset(7,1,-3)).isAir(),"the basement's side walls are down to flats, gaps between");
                h.assertTrue(DeadAirBlock.is(f.out.getBlockState(b.offset(DeadAirSet.TELEPROMPTER)),DeadAirBlock.Kind.TELEPROMPTER),"the teleprompter stands in the studio");
                f.click(p,P,DeadAirSet.TELEPROMPTER);var own=f.own(p,P);basement(f,p);
                h.assertTrue(own.getInt("Beat")==3&&own.getBoolean("Script206"),"reading the teleprompter finishes the third visit");
                var text=DeadAirTexts.scriptText("ACTOR",own);h.assertTrue(text.contains("088.1")&&text.contains("crawls under the arms")&&text.contains("all three dishes"),"the script is what the reader did, as stage directions");})
            .thenSucceed();
    }
    @AfterBatch(batch="dead_air_stage") public static void stageDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="dead_air_booth",timeoutTicks=600)
    public static void inTheBoothTheCentreMonitorPutsTheReaderOnTheAirAndTheScriptResolvesIt(GameTestHelper h){
        var f=fixture(h);var p=f.player("dead_air_booth",P);var b=f.base(P);
        h.startSequence().thenExecute(()->{hold(f);f.source(p,P);visit(f,p,3,true);})
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&set(f).getInt("RunVisit")==4,"the fourth visit"))
            .thenExecute(()->{h.assertTrue(f.out.getBlockState(b.offset(11,6,DeadAirSet.CURTAIN_Z)).getValue(DeadAirBlock.STAGE)==1,"the back curtain stands open");
                h.assertTrue(DeadAirBlock.is(f.out.getBlockState(b.offset(DeadAirSet.HIGHCHAIR)),DeadAirBlock.Kind.HIGHCHAIR),"the highchair faces the monitors");
                h.assertTrue(!f.own(p,P).getBoolean("Ready"),"nothing is done until the centre monitor");
                f.click(p,P,DeadAirSet.MONITOR);var own=f.own(p,P);
                h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("on_the_air")&&f.out.getBlockState(b.offset(DeadAirSet.MONITOR)).getValue(DeadAirBlock.STAGE)==1,"the centre monitor clears to Percy's marquee");
                var breaker=p.getInventory().items.stream().filter(s->s.is(DeadAirRegistry.QUIET.get())).findFirst();
                h.assertTrue(breaker.isPresent()&&breaker.get().get(DataComponents.CUSTOM_DATA).copyTag().hasUUID(LiteraryVignettes.OWNER),"the dead-air breaker is the reader's own");
                DeadAirGame.quiet(p,breaker.get());h.assertTrue(DeadAirGame.quiet(p)&&WorkPrint.unseen(p),"thrown, nothing that hunts by sight can find its owner");
                h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.DEAD_AIR),"nothing resolves until the script's last page is read");
                f.read(p,P,DeadAirSet.MONITOR);
                h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.DEAD_AIR)&&f.own(p,P).getBoolean("Completed"),"reading the last page resolves the broadcast for this reader");})
            .thenSucceed();
    }
    @AfterBatch(batch="dead_air_booth") public static void boothDone(ServerLevel l){close();}
}
