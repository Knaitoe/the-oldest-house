package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.210, the storeroom, played with real readers: dealt only in the dark band; the spring door shutting behind and the list in
 * the reader's own hand; a look at the door bringing the shelves a block nearer; the dread run out, the head turned, a thing gone
 * and the tagged thing kept for later; the third look crushing; all three things and backing out without turning; and two
 * readers holding the clock between them.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class StoreroomTests {
    private StoreroomTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.STOREROOM;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);Storeroom.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}Storeroom.clearAll();}
    private static net.minecraft.nbt.CompoundTag set(LiteraryTests.Fixture f){return Storeroom.set(f.in.getServer());}
    private static void hold(LiteraryTests.Fixture f){var b=f.base(P);f.chunks.hold(f.in,new AABB(b.getX()-6,b.getY()-2,b.getZ()-37,b.getX()+7,b.getY()+7,b.getZ()+3));}
    private static boolean shelf(LiteraryTests.Fixture f,int x,int y,int z){return HorrorBlock.shelf(f.in.getBlockState(f.base(P).offset(x,y,z)));}
    private static void clickFrom(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));}
    /** Stands the reader in the storeroom at this depth, facing into it (away from the door). */
    private static void deep(LiteraryTests.Fixture f,ServerPlayer p,double x,double z){var b=f.base(P);p.teleportTo(f.in,b.getX()+x,b.getY(),b.getZ()+z,180,0);}
    private static void faceDoor(LiteraryTests.Fixture f,ServerPlayer p){var b=f.base(P);p.teleportTo(f.in,p.getX(),p.getY(),p.getZ(),0,0);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,StoreroomSite.doorCentre(b));}
    /** Into the storeroom, the door shut behind them, the room running. */
    private static void inside(LiteraryTests.Fixture f,ServerPlayer p){deep(f,p,.5,-23.5);StoreroomSite.door(f.in,f.base(P),false);}
    private static void readList(LiteraryTests.Fixture f,ServerPlayer p){var own=f.own(p,P);own.putBoolean("Read_Source",true);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}

    @GameTest(template="empty",batch="store_deal",timeoutTicks=400)
    public static void dealtOnlyInTheDarkBandWithTheDoorTheShelvesAndTheList(GameTestHelper h){
        var f=fixture(h);var p=f.player("store_deal",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the storeroom is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&DarkHalls.horror(P)&&P.kind()==LabyrinthPlace.Kind.ONE_SHOT,"an interior horror of the dark halls, in one visit");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, and the Witness pool grows by one");
                h.assertTrue(LiteraryRooms.source(P).equals(StoreroomSite.LIST)&&LiteraryRooms.ending(P).equals(StoreroomSite.FOOTNOTE),"the list is the source and Johnny's footnote the account");
                var d=new LabyrinthData();var id=UUID.randomUUID();
                for(int depth=1;depth<=17;depth++){d.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(depth,64,0),0F,true));h.assertTrue(LiteraryVignettes.canDeal(d,id,P)==(depth>=DarkHalls.DARK_DEPTH),"dealt only from depth sixteen (depth "+depth+")");}
                h.assertTrue(f.in.getBlockState(b.offset(StoreroomSite.DOOR)).getBlock() instanceof DoorBlock,"a door at the end of the hall");
                h.assertTrue(shelf(f,3,0,-26)&&shelf(f,-3,3,-26)&&!shelf(f,2,0,-26)&&shelf(f,0,1,StoreroomSite.END_Z),"shelving down both sides and across the end, five wide between");
                h.assertTrue(HorrorBlock.is(f.in.getBlockState(b.offset(-3,1,StoreroomSite.INK_Z)),HorrorBlock.Kind.SHELF_INK)&&HorrorBlock.is(f.in.getBlockState(b.offset(3,1,StoreroomSite.NEEDLES_Z)),HorrorBlock.Kind.SHELF_NEEDLES)&&HorrorBlock.is(f.in.getBlockState(b.offset(StoreroomSite.OWN)),HorrorBlock.Kind.SHELF_OWN),"the ink, the needles and a thing with a tag on it");
                h.assertTrue(HorrorBlock.is(f.in.getBlockState(b.offset(StoreroomSite.BULB)),HorrorBlock.Kind.BULB)&&HorrorBlock.light(f.in.getBlockState(b.offset(StoreroomSite.BULB)))>0,"one bulb, lit");
                p.getInventory().setItem(0,new ItemStack(Items.COMPASS));
                var list=Storeroom.list(p,f.own(p,P)).get(DataComponents.WRITTEN_BOOK_CONTENT);
                var listText=list==null?"":String.join(" ",list.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(listText.contains("compass")&&listText.contains("DON'T TURN ROUND"),"the list, in the reader's hand, wants a thing of their own: "+listText);
            }).thenSucceed();
    }
    @AfterBatch(batch="store_deal") public static void dealDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="store_look",timeoutTicks=400)
    public static void theDoorSpringsShutAndEveryLookBringsTheShelvesNearer(GameTestHelper h){
        var f=fixture(h);var p=f.player("store_look",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the storeroom is resident"))
            .thenExecute(()->{StoreroomSite.door(f.in,b,true);deep(f,p,.5,-23.5);})
            .thenWaitUntil(()->h.assertTrue(!StoreroomSite.open(f.in,b),"the door swings shut on its spring behind them"))
            .thenWaitUntil(()->h.assertTrue(Storeroom.running(set(f)),"and the room is running"))
            .thenExecute(()->{Storeroom.TEST_HOLD=true;faceDoor(f,p);})
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(Storeroom.looks(set(f))==0,"turning to read the list costs nothing");
                readList(f,p);deep(f,p,.5,-23.5);})
            .thenIdle(2)
            .thenExecute(()->faceDoor(f,p))
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(Storeroom.looks(set(f))==1,"once it has been read, looking at the door is a look: "+Storeroom.looks(set(f)));
                h.assertTrue(shelf(f,2,0,-26)&&shelf(f,-2,2,-26)&&!shelf(f,1,0,-26),"the light comes back with the side shelves a block nearer");
                h.assertTrue(HorrorBlock.is(f.in.getBlockState(b.offset(-2,1,StoreroomSite.INK_Z)),HorrorBlock.Kind.SHELF_INK),"the ink comes nearer with them");
                h.assertTrue(set(f).getInt("Dark")>0,"and for a moment it was dark");})
            .thenIdle(Storeroom.HELD_LOOK+4)
            .thenExecute(()->{h.assertTrue(Storeroom.looks(set(f))==2,"holding the look costs another: "+Storeroom.looks(set(f)));
                h.assertTrue(shelf(f,1,0,-26)&&!shelf(f,0,0,-26),"the shelves against the shoulders");
                var r=p.position().subtract(Vec3.atLowerCornerOf(b));h.assertTrue(r.x>=0&&r.x<1,"the reader is shoved into what is left: "+r.x);})
            .thenSucceed();
    }
    @AfterBatch(batch="store_look") public static void lookDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="store_dread",timeoutTicks=600)
    public static void theDreadRunsOutAndTheReaderComesToInTheHallWithSomethingGone(GameTestHelper h){
        var f=fixture(h);var p=f.player("store_dread",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the storeroom is resident"))
            .thenExecute(()->{p.getInventory().setItem(0,new ItemStack(Items.BREAD,3));inside(f,p);})
            .thenWaitUntil(()->h.assertTrue(Storeroom.running(set(f)),"the room is running"))
            .thenExecute(()->{readList(f,p);deep(f,p,.5,-30.5);clickFrom(f,p,StoreroomSite.OWN);
                h.assertTrue((Storeroom.has(f.own(p,P))&Storeroom.OWN)!=0,"the thing with the tag is taken");})
            .thenIdle(10)
            .thenExecute(()->h.assertTrue(Storeroom.dread(set(f))>=10,"with a back to the door, the dread climbs: "+Storeroom.dread(set(f))))
            .thenExecute(()->Storeroom.edit(p.server,t->t.putInt("Dread",Storeroom.DREAD-1)))
            .thenWaitUntil(()->h.assertTrue(StoreroomSite.inHall(p.position().subtract(Vec3.atLowerCornerOf(b))),"a hand, the head turned, a blow, and the reader comes to on the hall floor"))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(p.getInventory().getItem(0).isEmpty()&&own.getString("Gone210").equals("Bread"),"one thing of theirs is gone: "+own.getString("Gone210"));
                h.assertTrue(own.contains("Return210")&&own.getLong("ReturnDay210")>=f.in.getDayTime()/24000+3&&Storeroom.has(own)==0,"the thing on the tag is gone too, to be found later");
                boolean carried=false;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var c=inv.getItem(i).get(DataComponents.CUSTOM_DATA);if(c!=null&&c.copyTag().contains("Storeroom210"))carried=true;}
                h.assertTrue(!carried,"nothing from the shelves is left on them");
                h.assertTrue(!own.getBoolean("Ready"),"and nothing is ready");})
            .thenSucceed();
    }
    @AfterBatch(batch="store_dread") public static void dreadDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="store_crush",timeoutTicks=400)
    public static void theThirdLookBringsTheShelvesTogether(GameTestHelper h){
        var f=fixture(h);var p=f.player("store_crush",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the storeroom is resident"))
            .thenExecute(()->inside(f,p))
            .thenWaitUntil(()->h.assertTrue(Storeroom.running(set(f)),"the room is running"))
            .thenExecute(()->{Storeroom.TEST_HOLD=true;readList(f,p);Storeroom.edit(p.server,t->t.putInt("Looks",2));StoreroomSite.shelves(f.in,b,2);deep(f,p,.5,-26.5);})
            .thenIdle(2)
            .thenExecute(()->{p.invulnerableTime=0;faceDoor(f,p);})
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(Storeroom.looks(set(f))==3&&shelf(f,0,0,-26)&&shelf(f,0,3,-30),"at the third look the shelves meet");
                h.assertTrue(p.isDeadOrDying(),"and the reader between them dies there");})
            .thenSucceed();
    }
    @AfterBatch(batch="store_crush") public static void crushDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="store_out",timeoutTicks=500)
    public static void allThreeThingsAndBackingOutWithoutTurning(GameTestHelper h){
        var f=fixture(h);var p=f.player("store_out",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the storeroom is resident"))
            .thenExecute(()->{f.source(p,P);inside(f,p);})
            .thenWaitUntil(()->h.assertTrue(Storeroom.running(set(f)),"the room is running"))
            .thenExecute(()->{Storeroom.TEST_HOLD=true;deep(f,p,-1.5,-24.5);clickFrom(f,p,new BlockPos(-3,1,StoreroomSite.INK_Z));
                deep(f,p,2.5,-29.5);clickFrom(f,p,new BlockPos(3,1,StoreroomSite.NEEDLES_Z));
                deep(f,p,.5,-31.5);clickFrom(f,p,StoreroomSite.OWN);
                h.assertTrue(Storeroom.has(f.own(p,P))==Storeroom.ALL,"the ink, the needles and the thing with the tag");
                h.assertTrue(Storeroom.looks(set(f))==0,"without once looking back");
                deep(f,p,.5,-20.8);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready"),"backing into the door, it opens behind them"))
            .thenExecute(()->{h.assertTrue(f.own(p,P).getString("Outcome").equals("did_not_turn_round")&&p.getZ()>b.getZ()+StoreroomSite.DOOR_Z,"and they are out, not having turned round");
                f.read(p,P,StoreroomSite.FOOTNOTE);
                h.assertTrue(f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.STOREROOM),"Johnny's footnote resolves it");
                var text=StoreroomTexts.accountText(p.getGameProfile().getName(),f.own(p,P));
                h.assertTrue(text.contains("I never looked back")&&text.contains("They are yours"),"the footnote turns into the reader's own hand, and remembers they never looked");})
            .thenSucceed();
    }
    @AfterBatch(batch="store_out") public static void outDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="store_pair",timeoutTicks=400)
    public static void twoReadersHoldTheClockBetweenThemAndEitherLookShrinksIt(GameTestHelper h){
        var f=fixture(h);var a=f.player("store_pair_a",P);var c=f.player("store_pair_b",P);var b=f.base(P);
        h.startSequence().thenExecute(()->hold(f)).thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the storeroom is resident"))
            .thenExecute(()->{inside(f,a);deep(f,c,1.5,-25.5);})
            .thenWaitUntil(()->h.assertTrue(Storeroom.running(set(f)),"the room is running"))
            .thenExecute(()->{deep(f,a,-.5,-28.5);deep(f,c,1.5,-28.5);Storeroom.edit(a.server,t->t.putInt("Dread",0));})
            .thenIdle(20)
            .thenExecute(()->{int d=Storeroom.dread(set(f));h.assertTrue(d>0&&d<=Storeroom.BACK*12,"with two of them it climbs at half the rate: "+d);
                readList(f,c);faceDoor(f,c);})
            .thenIdle(2)
            .thenExecute(()->{h.assertTrue(Storeroom.looks(set(f))==1&&Storeroom.dread(set(f))==0,"one of them looking holds it back, and shrinks the room for both");
                h.assertTrue(f.own(c,P).getInt("Looks210")==1&&f.own(a,P).getInt("Looks210")==0,"the look is the one who looked");})
            .thenSucceed();
    }
    @AfterBatch(batch="store_pair") public static void pairDone(ServerLevel l){close();}
}
