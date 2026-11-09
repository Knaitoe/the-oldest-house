package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.407, The Drawing, with real readers. The square built whole and held on every visit; the notice on the post office board. One
 * reader: the morning, the lemonade poured by the one who came late, the list read and their own name at the end of it, the slip drawn
 * by their own hand, unfolded blank; the Garrows drawing again and Nell holding out the mark; the stone in their hand, thrown, the day cut
 * to white at the head of the lane with the square empty for them; the notice, then the account, which resolves the story. Two readers:
 * one drops the stone, one stands; the crowd closes in and both days cut; each account tells its own choice and nobody marked is a reader.
 * A second year: one villager fewer, a house shuttered, the box bound with wire, the one who came late marked, and walking away.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class DrawingTests {
    private DrawingTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.THE_DRAWING;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Drawing.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-31,b.getY()-4,b.getZ()-73,b.getX()+31,b.getY()+16,b.getZ()+2);}
    private static boolean holds(ServerPlayer p,Item item){for(var st:p.getInventory().items)if(st.is(item))return true;for(var st:p.getInventory().offhand)if(st.is(item))return true;return false;}
    /** Takes the first of an item out of the inventory and puts it in the reader's main hand. */
    private static void inHand(ServerPlayer p,Item item){for(int i=0;i<p.getInventory().items.size();i++){var st=p.getInventory().items.get(i);if(st.is(item)){var taken=st.copy();p.getInventory().items.set(i,ItemStack.EMPTY);p.setItemInHand(InteractionHand.MAIN_HAND,taken);return;}}}
    private static void use(ServerPlayer p){NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND));}
    private static String account(LiteraryTests.Fixture f,ServerPlayer p){var book=DrawingTexts.account(p.getGameProfile().getName(),f.own(p,P));var c=book.get(DataComponents.WRITTEN_BOOK_CONTENT);return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
    /** Stands a reader on the lawn among the crowd. */
    private static void stand(LiteraryTests.Fixture f,ServerPlayer p,double x){f.at(p,P,x,0,-30.5);}

    @GameTest(template="empty",batch="the_drawing",timeoutTicks=6000)
    public static void aMorningInTheSquareFromTheNoticeToTheStone(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the square is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.kind()==LabyrinthPlace.Kind.RECURRING&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-30),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==18,"a literary square outside, held every visit, built whole, at ten in the morning");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, the quota derived");
                h.assertTrue(DrawingBlock.is(l.getBlockState(b.offset(DrawingSite.NOTICE)),DrawingBlock.Kind.NOTICE)&&DrawingBlock.is(l.getBlockState(b.offset(DrawingSite.ACCOUNT)),DrawingBlock.Kind.ACCOUNT),"the notice and the bare pin on the post office board");
                h.assertTrue(DrawingBlock.is(l.getBlockState(b.offset(DrawingSite.BOX)),DrawingBlock.Kind.BOX)&&DrawingBlock.is(l.getBlockState(b.offset(DrawingSite.STOOL)),DrawingBlock.Kind.STOOL),"the box on its stool on the lawn");
                h.assertTrue(l.getBlockState(b.offset(0,-1,-5)).is(net.minecraft.world.level.block.Blocks.DIRT_PATH)||l.getBlockState(b.offset(0,-1,-5)).is(net.minecraft.world.level.block.Blocks.COARSE_DIRT),"the lane from the House's door");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("The drawing will be held in the square at ten o'clock")&&all.contains("Slips are not to be opened until the last has been drawn"),"the notice: "+all);
                h.assertTrue(Drawing.present(1).size()==8&&Drawing.present(2).size()==7&&Drawing.present(6).size()==3&&Drawing.markedIn(1)==Drawing.NELL-Drawing.ALBRIGHT&&Drawing.markedIn(9)==Drawing.PIKE-Drawing.ALBRIGHT,"one fewer a year, Nell first, Mr. Pike in the end and after");
                me[0]=f.player("drawing_one",P);stand(f,me[0],-6.5);
            })
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.MORNING&&Drawing.year(b)==1&&Drawing.actor(l,b,Drawing.RICKY)!=null,"the morning, year one, a child already gathering stones"))
            .thenExecute(()->Drawing.skip(b,Drawing.LATE))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Lemonade")&&holds(me[0],DrawingRegistry.LEMONADE.get())&&Drawing.actor(l,b,Drawing.NELL)!=null,"Nell comes in late and pours a glass of lemonade"))
            .thenExecute(()->{h.assertTrue(f.own(me[0],P).getString("PouredBy").equals("Nell Garrow"),"poured by Nell");
                Drawing.talk(me[0],Drawing.actor(l,b,Drawing.DUNN));h.assertTrue(Talk.waiting(me[0],Drawing.TALK)&&f.own(me[0],P).getInt("Talked")==1,"Mrs. Dunn talks about the rain");
                h.assertTrue(Talk.choose(me[0],"Ask what the drawing is for"),"the question is there");Talk.close(me[0]);stand(f,me[0],-6.5);
                Drawing.skip(b,Drawing.MORNING_LENGTH);})
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.CALL,"the list"))
            .thenWaitUntil(()->h.assertTrue(Drawing.calling(b,me[0].getUUID())&&f.own(me[0],P).getBoolean("Called"),"at the end of the list, the reader's own name, a new household"))
            .thenExecute(()->f.click(me[0],P,DrawingSite.BOX))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getString("Drew").equals("self")&&holds(me[0],DrawingRegistry.SLIP.get()),"they put a hand in the box and take a slip"))
            .thenExecute(()->{stand(f,me[0],-6.5);inHand(me[0],DrawingRegistry.SLIP.get());use(me[0]);h.assertTrue(!f.own(me[0],P).getBoolean("Opened"),"not until everyone has drawn");})
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)>=Drawing.OPENING,"everyone may look"))
            .thenExecute(()->{use(me[0]);h.assertTrue(f.own(me[0],P).getBoolean("Opened")&&me[0].getMainHandItem().has(DataComponents.CUSTOM_MODEL_DATA),"unfolded: blank");})
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)>=Drawing.STONES&&f.own(me[0],P).getString("Marked").equals("Garrow")&&f.own(me[0],P).getString("MarkedName").equals("Nell Garrow"),"the Garrows drew again; Nell holds out the mark: "+f.own(me[0],P)))
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getBoolean("Stone")&&holds(me[0],DrawingRegistry.STONE.get()),"a child presses a stone into their hand"))
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.CHOICE,"each for themselves"))
            .thenExecute(()->{inHand(me[0],DrawingRegistry.STONE.get());use(me[0]);
                var o=f.own(me[0],P);h.assertTrue(o.getString("Choice").equals(Drawing.THREW)&&o.getInt("Years")==1&&o.getBoolean("Ready")&&Drawing.cutOut(me[0].getUUID()),"thrown: the day cut to white, the account ready: "+o);
                h.assertTrue(!holds(me[0],DrawingRegistry.SLIP.get())&&!holds(me[0],DrawingRegistry.STONE.get()),"the slip taken back, the stone gone");
                var nell=Drawing.actor(l,b,Drawing.NELL);h.assertTrue(nell==null||!nell.broadcastToPlayer(me[0]),"the square is empty for them");})
            .thenWaitUntil(()->h.assertTrue(me[0].position().distanceTo(net.minecraft.world.phys.Vec3.atLowerCornerOf(b).add(DrawingSite.LANE_HEAD))<1.5,"set at the head of the lane"))
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.OVER,"nobody left in the day: it is over"))
            .thenExecute(()->{
                f.source(me[0],P);h.assertTrue(f.own(me[0],P).getBoolean("Read_Source"),"the notice is read");
                f.read(me[0],P,DrawingSite.ACCOUNT);
                var o=f.own(me[0],P);h.assertTrue(o.getBoolean("Completed")&&WitnessAccount.has(f.data(),me[0].getUUID(),WitnessAccount.Story.THE_DRAWING),"the account read: the story resolved");
                var text=account(f,me[0]);
                h.assertTrue(text.contains("A woman who came in late, laughing at herself, poured me a glass of lemonade")&&text.contains("at the end of the list he read my name")&&text.contains("took a slip")&&text.contains("mine was blank")&&text.contains("It was the Garrow household.")&&text.contains("Nell Garrow drew the slip with the mark")&&text.contains("I threw it.")&&!text.contains("still in my pocket"),"the account follows the record: "+text);
                h.assertTrue(!text.contains(" she ")&&!text.contains(" he was ")&&!text.matches(".*\\b(afraid|terrified|happy|sad)\\b.*"),"the reader is never gendered and never told how they felt: "+text);
                h.assertTrue(LiteraryVignettes.canDeal(f.data(),me[0].getUUID(),P),"the drawing is held again on the next visit");
                Drawing.resetNow(l,b);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="the_drawing_two",timeoutTicks=6000)
    public static void twoReadersEachTheirOwnSlipAndStoneAndTheCrowdClosesIn(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] r=new ServerPlayer[2];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the square is resident"))
            .thenExecute(()->{r[0]=f.player("drawing_drop",P);r[1]=f.player("drawing_stand",P);stand(f,r[0],-6.5);stand(f,r[1],6.5);})
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.MORNING,"one morning for both"))
            .thenExecute(()->Drawing.skip(b,Drawing.MORNING_LENGTH+400))
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.CALL,"the list"))
            // Neither goes up: he draws for each of them, in turn.
            .thenWaitUntil(()->h.assertTrue(f.own(r[0],P).getString("Drew").equals("for")&&f.own(r[1],P).getString("Drew").equals("for"),"drawn for, both: "+f.own(r[0],P)+" / "+f.own(r[1],P)))
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.CHOICE,"each for themselves"))
            .thenExecute(()->{
                for(var p:r)h.assertTrue(holds(p,DrawingRegistry.STONE.get())&&f.own(p,P).getString("Marked").equals("Garrow"),"each their own stone, the same marked household");
                // The first drops it.
                for(int i=0;i<r[0].getInventory().items.size();i++){var st=r[0].getInventory().items.get(i);if(st.is(DrawingRegistry.STONE.get())){var dropped=new ItemEntity(l,r[0].getX(),r[0].getY(),r[0].getZ(),st.copy());r[0].getInventory().items.set(i,ItemStack.EMPTY);var toss=new ItemTossEvent(dropped,r[0]);NeoForge.EVENT_BUS.post(toss);l.addFreshEntity(dropped);break;}}
                h.assertTrue(f.own(r[0],P).getString("Choice").equals(Drawing.DROPPED)&&!Drawing.cutOut(r[0].getUUID()),"dropped on the flagstones; still in the square");
                h.assertTrue(f.own(r[1],P).getString("Choice").isEmpty(),"the other's choice is their own");
                Drawing.skip(b,Drawing.STAND_LIMIT);})
            .thenWaitUntil(()->h.assertTrue(Drawing.cutOut(r[0].getUUID())&&Drawing.cutOut(r[1].getUUID())&&Drawing.stage(b)==Drawing.OVER,"the crowd closes in: both days cut"))
            .thenExecute(()->{
                h.assertTrue(f.own(r[0],P).getString("Choice").equals(Drawing.DROPPED)&&f.own(r[1],P).getString("Choice").equals(Drawing.STOOD),"dropped, and stood");
                var a=account(f,r[0]);var c=account(f,r[1]);
                h.assertTrue(a.contains("I didn't go up. He drew for me")&&a.contains("I dropped it on the flagstones")&&!a.contains("still in my pocket"),"the first account: "+a);
                h.assertTrue(c.contains("I stood where I was and held it")&&c.contains("The stone was still in my pocket"),"the second account: "+c);
                for(var p:r){var name=p.getGameProfile().getName();h.assertTrue(!f.own(p,P).getString("MarkedName").equals(name),"nobody marked is a reader");}
                for(var p:r){f.source(p,P);f.read(p,P,DrawingSite.ACCOUNT);h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.THE_DRAWING),"each resolved by their own account");}
                Drawing.resetNow(l,b);
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="the_drawing_again",timeoutTicks=6000)
    public static void aSecondYearIsEmptierAndTheLateArrivalIsMarked(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] me=new ServerPlayer[1];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the square is resident"))
            .thenExecute(()->{
                me[0]=f.player("drawing_again",P);
                // A reader who stood in the square once, and talked with Mrs. Dunn there.
                var own=f.own(me[0],P);own.putInt("Years",1);var met=new ListTag();met.add(StringTag.valueOf("Mrs. Dunn"));own.put("MetLast",met);
                var saw=new ListTag();for(var name:List.of("Mr. Albright","Mr. Rudd","Nell Garrow","Dale Garrow","Bess Garrow","Old Mr. Pike","Mrs. Dunn","Ricky"))saw.add(StringTag.valueOf(name));own.put("SawLast",saw);
                LiteraryVignettes.save(f.data(),me[0].getUUID(),P,own);stand(f,me[0],-6.5);
            })
            .thenWaitUntil(()->h.assertTrue(Drawing.year(b)==2&&Drawing.actor(l,b,Drawing.ALBRIGHT)!=null,"year two"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(DrawingSite.BOX)).getValue(DrawingBlock.STAGE)==1,"the box bound with wire now");
                int shut=0;for(int x=-30;x<=30;x++)for(int y=0;y<=6;y++)for(int z=-72;z<=0;z++){var st=l.getBlockState(b.offset(x,y,z));if(DrawingBlock.is(st,DrawingBlock.Kind.SHUTTER)&&st.getValue(DrawingBlock.STAGE)==1)shut++;}
                h.assertTrue(shut==6,"one house shuttered: "+shut);
                Drawing.skip(b,Drawing.LATE);})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getString("PouredBy").equals("Mrs. Dunn"),"Mrs. Dunn came late with the jug this year: "+f.own(me[0],P)))
            .thenExecute(()->{h.assertTrue(Drawing.actor(l,b,Drawing.NELL)==null,"Nell is not there");Drawing.skip(b,Drawing.MORNING_LENGTH);})
            .thenWaitUntil(()->h.assertTrue(Drawing.stage(b)==Drawing.CHOICE,"each for themselves"))
            .thenExecute(()->{
                var o=f.own(me[0],P);h.assertTrue(o.getString("Marked").equals("Dunn")&&o.getString("MarkedName").equals("Mrs. Dunn")&&o.getString("InPlaceOf").equals("Mrs. Dunn")&&o.getBoolean("InPlaceTalked"),"the Dunns drew again; Mrs. Dunn, whom they had talked with: "+o);
                // Walking back down the lane with the stone in hand.
                f.at(me[0],P,.5,0,-6.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(me[0],P).getString("Choice").equals(Drawing.WALKED)&&Drawing.cutOut(me[0].getUUID()),"they walk away; nobody stops them"))
            .thenExecute(()->{
                var o=f.own(me[0],P);h.assertTrue(o.getInt("Years")==2,"two years now");
                var text=account(f,me[0]);
                h.assertTrue(text.contains("I have stood in that square twice now")&&text.contains("Mrs. Dunn came in late this year")&&text.contains("It was the Dunn household.")&&text.contains("I had talked with Mrs. Dunn the year before.")&&text.contains("walked back down the lane")&&text.contains("The stone was still in my pocket"),"the second year's account: "+text);
                h.assertTrue(holds(me[0],DrawingRegistry.STONE.get()),"the stone goes with them");
                Drawing.resetNow(l,b);
            })
            .thenSucceed();
    }

    private static void close(){if(active!=null){active.close();active=null;}Drawing.clearAll();}
    @AfterBatch(batch="the_drawing") public static void done(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="the_drawing_two") public static void twoDone(net.minecraft.server.level.ServerLevel l){close();}
    @AfterBatch(batch="the_drawing_again") public static void againDone(net.minecraft.server.level.ServerLevel l){close();}
}
