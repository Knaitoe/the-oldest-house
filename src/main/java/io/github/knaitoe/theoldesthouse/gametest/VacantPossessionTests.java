package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.228, Vacant Possession, played with a real reader: the flat built whole; every piece on the list lifted and set down in the
 * van, what each was hiding there where it stood, the tenant fainter with each; a piece that leaves the reader's arms going back
 * where it stood; the tenant in the last chair, standing when the reader comes close; and the chair either carried down (the window
 * dark) or put back (the light still on), each ending on the street with the docket in the reader's hand.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class VacantPossessionTests {
    private VacantPossessionTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.VACANT_POSSESSION;
    /** A van panel: anything on the van takes what the reader is carrying. */
    private static final BlockPos VAN=new BlockPos(13,VacantSite.GROUND,-5);
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);VacantPossession.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}VacantPossession.clearAll();}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double y,double z,float yaw){var b=f.base(P);p.teleportTo(f.level(P),b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);}
    private static boolean click(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var own=f.own(p,P);boolean handled=VacantPossession.click(p,f.base(P),rel,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);return handled;}
    private static boolean carrying(LiteraryTests.Fixture f,ServerPlayer p){return f.own(p,P).contains("Carrying228");}
    private static boolean tenantSeated(LiteraryTests.Fixture f){var t=VacantPossession.tenantActor(f.level(P),f.base(P));return t!=null&&t.position().distanceToSqr(Vec3.atBottomCenterOf(f.base(P).offset(VacantSite.CHAIR_PIECE.at())))<=.05;}
    private static String docket(ServerPlayer p){for(var s:p.getInventory().items)if(s.is(Items.WRITTEN_BOOK)){var c=s.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null){var all=String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());if(all.contains("REMOVAL DOCKET"))return all;}}return "";}

    /** The flat as built, the reader in it, and everything on the list down in the van, so the tenant is in the last chair. */
    private static GameTestSequence emptied(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p){
        var b=f.base(P);var l=f.level(P);
        var seq=h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-8,b.getY()-8,b.getZ()-20,b.getX()+19,b.getY()+9,b.getZ()+2)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the flat is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored(),"a literary flat indoors, one visit, built whole");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, and the quota derives from the pool");
                h.assertTrue(VacantBlock.is(l.getBlockState(b.offset(VacantSite.LIST)),VacantBlock.Kind.LIST)&&VacantBlock.is(l.getBlockState(b.offset(VacantSite.DOCKET)),VacantBlock.Kind.DOCKET),"the list by the door and the docket in the van");
                for(var piece:VacantSite.PIECES)h.assertTrue(VacantBlock.is(l.getBlockState(b.offset(piece.at())),piece.kind()),"everything on the list where it stands: "+piece.name());
                h.assertTrue(VacantBlock.is(l.getBlockState(b.offset(VacantSite.CHAIR_PIECE.at())),VacantBlock.Kind.CHAIR)&&l.getBlockState(b.offset(2,-1,-8)).is(VacantRegistry.material("boards_new")),"the chair by the window; under the rug the boards are new");
                h.assertTrue(l.getBlockState(b.offset(12,VacantSite.GROUND-1,-7)).is(VacantRegistry.material("van_floor"))&&l.getBlockState(b.offset(9,VacantSite.GROUND-1,-12)).is(VacantRegistry.material("pavement")),"the van at the kerb, the pavement outside the door");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("Chair")&&all.contains("vacant possession")&&all.contains("nobody in it"),"the list, and what is on the back of it: "+all);
                at(f,p,3.5,0,-7.5,180);})
            .thenWaitUntil(()->h.assertTrue(VacantPossession.tenantActor(l,b)!=null&&f.own(p,P).getBoolean("Flat228"),"someone is waiting in the flat, and greets the reader"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,VacantSite.CHAIR_PIECE.at())&&!carrying(f,p)&&VacantPossession.seat(b)==VacantPossession.STANDING,"the chair can wait");
                // The dresser, lifted: pencil marks behind it. Dropped from the reader's arms, it goes back where it stood.
                h.assertTrue(click(f,p,new BlockPos(-2,0,-9))&&carrying(f,p)&&VacantBlock.is(l.getBlockState(b.offset(-2,0,-9)),VacantBlock.Kind.MARKS)&&VacantBlock.is(l.getBlockState(b.offset(-2,1,-9)),VacantBlock.Kind.MARKS),"the dresser in the reader's arms, the marks behind it");
                h.assertTrue(p.getMainHandItem().is(VacantRegistry.PIECE.get()),"the dresser is in the reader's hands");
                click(f,p,new BlockPos(-2,1,-9));})
            .thenExecute(()->{
                h.assertTrue(!carrying(f,p)&&VacantBlock.is(l.getBlockState(b.offset(-2,0,-9)),VacantBlock.Kind.DRESSER)&&!p.getMainHandItem().is(VacantRegistry.PIECE.get()),"the dresser set back where it stood");
                click(f,p,new BlockPos(-2,0,-9));h.assertTrue(carrying(f,p),"lifted again");
                for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(VacantRegistry.PIECE.get()))p.getInventory().setItem(i,ItemStack.EMPTY);})
            .thenWaitUntil(()->h.assertTrue(!carrying(f,p)&&VacantBlock.is(l.getBlockState(b.offset(-2,0,-9)),VacantBlock.Kind.DRESSER),"a piece that leaves the reader's arms goes back where it stood"))
            .thenExecute(()->{
                // Everything on the list, down to the van, one at a time: the tenant fainter with each.
                int n=0;
                for(var piece:VacantSite.PIECES){
                    h.assertTrue(click(f,p,piece.at())&&carrying(f,p),"lifted: "+piece.name());
                    if(piece.kind()==VacantBlock.Kind.BED)h.assertTrue(VacantBlock.is(l.getBlockState(b.offset(piece.at())),VacantBlock.Kind.SCRATCHES),"worn places where the bed's legs stood");
                    if(piece.kind()==VacantBlock.Kind.WARDROBE)h.assertTrue(VacantBlock.is(l.getBlockState(b.offset(piece.at().above())),VacantBlock.Kind.SQUARE),"a brighter square where the wardrobe stood");
                    h.assertTrue(click(f,p,VAN)&&!carrying(f,p)&&VacantPossession.inVan(b,piece.kind()),"set down in the van: "+piece.name());
                    n++;var t=VacantPossession.tenantActor(l,b);h.assertTrue(VacantPossession.loaded(b)==n&&t!=null&&t.fade()==n*VacantPossession.FADE_EACH,"fainter with each piece: "+n+" "+(t==null?"gone":t.fade()));
                }
                h.assertTrue(click(f,p,new BlockPos(-2,0,-9))&&f.own(p,P).getBoolean("Marks228"),"the pencil marks read");
                h.assertTrue(click(f,p,new BlockPos(2,-1,-8))&&f.own(p,P).getBoolean("Floor228"),"the new boards read");
                h.assertTrue(VacantPossession.seat(b)==VacantPossession.SITTING&&VacantBlock.is(l.getBlockState(b.offset(14,VacantSite.GROUND,-8)),VacantBlock.Kind.DRESSER),"everything on the list in the van, the dresser at the front");
                at(f,p,12.5,VacantSite.GROUND,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(tenantSeated(f),"in the last chair"))
            .thenExecute(()->at(f,p,5.5,0,-8.5,-90))
            .thenWaitUntil(()->h.assertTrue(VacantPossession.seat(b)==VacantPossession.STOOD,"they stand when the reader comes close: that's the last of it"))
            .thenExecute(()->h.assertTrue(click(f,p,VacantSite.CHAIR_PIECE.at())&&carrying(f,p)&&!VacantBlock.is(l.getBlockState(b.offset(VacantSite.CHAIR_PIECE.at())),VacantBlock.Kind.CHAIR),"the last chair in the reader's arms"));
        return seq;
    }

    @GameTest(template="empty",batch="vacant_carry",timeoutTicks=2400)
    public static void theLastChairCarriedDownLeavesTheFlatDark(GameTestHelper h){
        var f=fixture(h);var p=f.player("vacant_carrier",P);var b=f.base(P);var l=f.level(P);
        emptied(h,f,p)
            .thenExecute(()->{
                h.assertTrue(click(f,p,VAN)&&VacantPossession.seat(b)==VacantPossession.GONE&&VacantPossession.tenantActor(l,b)==null,"the chair in the van, and nobody left in the flat");
                h.assertTrue(l.getBlockState(b.offset(VacantSite.LIVING_BULB)).getValue(VacantBlock.STAGE)==1,"the window goes dark");
                var own=f.own(p,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("carried_out_the_last_chair")&&!own.getBoolean("Completed"),"ready, not yet finished: "+own);
                at(f,p,12.5,VacantSite.GROUND,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"on the street, it is over"))
            .thenExecute(()->{var text=docket(p);h.assertTrue(text.contains("vacant possession")&&text.contains("Carried by: vacant_carrier")&&text.contains("the window was dark"),"the docket in the reader's hand: "+text);
                h.assertTrue(f.data().isCompleted(P.id()),"the flat is finished");})
            .thenSucceed();
    }
    @AfterBatch(batch="vacant_carry") public static void carryDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="vacant_keep",timeoutTicks=2400)
    public static void theLastChairPutBackKeepsTheLightOn(GameTestHelper h){
        var f=fixture(h);var p=f.player("vacant_keeper",P);var b=f.base(P);var l=f.level(P);
        emptied(h,f,p)
            .thenExecute(()->{
                h.assertTrue(click(f,p,VacantSite.CHAIR_PIECE.at().below())&&!carrying(f,p)&&VacantPossession.seat(b)==VacantPossession.KEPT,"the chair put back where it stood");
                h.assertTrue(VacantBlock.is(l.getBlockState(b.offset(VacantSite.CHAIR_PIECE.at())),VacantBlock.Kind.CHAIR)&&tenantSeated(f),"they sit down in it again");
                h.assertTrue(l.getBlockState(b.offset(VacantSite.LIVING_BULB)).getValue(VacantBlock.STAGE)==0,"the light is still on");
                var own=f.own(p,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("put_the_chair_back"),"ready: "+own);
                at(f,p,12.5,VacantSite.GROUND,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"on the street, it is over"))
            .thenExecute(()->{var text=docket(p);h.assertTrue(text.contains("One chair left in place")&&text.contains("the light was still on"),"the docket in the reader's hand: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="vacant_keep") public static void keepDone(ServerLevel l){close();}
}
