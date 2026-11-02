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
                // 0.4.239: through its door, the flat sounds of someone in there, as loud as they are.
                var heard=VacantPossession.heard(b);h.assertTrue(heard!=null&&heard.carry()==DoorSounds.Carry.SOMEONE&&heard.volume()==1F,"through the door: someone in there: "+heard);
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
                h.assertTrue(VacantPossession.seat(b)==VacantPossession.SITTING&&VacantBlock.is(l.getBlockState(b.offset(13,VacantSite.GROUND,-8)),VacantBlock.Kind.DRESSER),"everything on the list in the van, the dresser at the front, against the bulkhead");
                var faint=VacantPossession.heard(b);h.assertTrue(faint!=null&&faint.carry()==DoorSounds.Carry.SOMEONE&&faint.volume()<1F,"through the door, they are fainter now too: "+faint);
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
                var heard=VacantPossession.heard(b);h.assertTrue(heard!=null&&heard.carry()==DoorSounds.Carry.EMPTY,"through the door now, nothing moving: "+heard);
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
                var heard=VacantPossession.heard(b);h.assertTrue(heard!=null&&heard.carry()==DoorSounds.Carry.SOMEONE,"and someone is still in there: "+heard);
                var own=f.own(p,P);h.assertTrue(own.getBoolean("Ready")&&own.getString("Outcome").equals("put_the_chair_back"),"ready: "+own);
                at(f,p,12.5,VacantSite.GROUND,-3.5,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"on the street, it is over"))
            .thenExecute(()->{var text=docket(p);h.assertTrue(text.contains("One chair left in place")&&text.contains("the light was still on"),"the docket in the reader's hand: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="vacant_keep") public static void keepDone(ServerLevel l){close();}

    /**
     * 0.4.244, aftermath (docs/AFTERMATH_0_4_244.md): the flat comes back to the readers who finished it as each of them left it.
     * The flat itself is empty to the walls, every trace showing, the bulbs off, nobody: as the chair-carrier left it. A reader who put
     * the chair back sees, in their own view, the chair where it stood, the light on and the one who lived here in it; the others do not.
     * Nothing to do and nothing to earn; and a reader who has not finished it, coming in, starts moving day afresh.
     */
    @GameTest(template="empty",batch="vacant_after",timeoutTicks=900)
    public static void aFinishedFlatComesBackEmptyOrLitAsItWasLeft(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);
        var out=f.player("vacant_after_out",P);finish(f,out,"carried_out_the_last_chair");int credit=WitnessAccount.count(f.data(),out.getUUID());
        ServerPlayer[] kept={null},fresh={null};
        h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-8,b.getY()-8,b.getZ()-20,b.getX()+19,b.getY()+9,b.getZ()+2)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the flat is resident"))
            .thenExecute(()->at(f,out,3.5,0,-7.5,180))
            .thenWaitUntil(()->h.assertTrue(VacantPossession.after(b),"the flat stands as its aftermath"))
            .thenExecute(()->{
                h.assertTrue(VacantBlock.is(l.getBlockState(b.offset(-2,0,-9)),VacantBlock.Kind.MARKS)&&l.getBlockState(b.offset(VacantSite.CHAIR_PIECE.at())).isAir(),"everything gone, the marks showing, the chair gone too");
                h.assertTrue(l.getBlockState(b.offset(VacantSite.LIVING_BULB)).getValue(VacantBlock.STAGE)==1&&VacantPossession.tenantActor(l,b)==null&&VacantPossession.shownTo(l,b,out.getUUID())==null,"the bulbs off and nobody there");
                var heard=VacantPossession.heard(b,f.data(),out.getUUID());h.assertTrue(heard!=null&&heard.carry()==DoorSounds.Carry.EMPTY,"through the door: nothing: "+heard);
                h.assertTrue(Aftermath.returns(f.data(),out.getUUID(),P)==1&&WitnessAccount.count(f.data(),out.getUUID())==credit&&f.own(out,P).getBoolean("Completed"),"the return counted, nothing earned");
                // A second reader, who put the chair back, comes in while the first is still there: the flat stays as it is.
                kept[0]=f.player("vacant_after_kept",P);finish(f,kept[0],"put_the_chair_back");at(f,kept[0],3.5,0,-7.5,180);})
            .thenWaitUntil(()->h.assertTrue(VacantPossession.shownTo(l,b,kept[0].getUUID())!=null,"the reader who put the chair back finds someone in it"))
            .thenExecute(()->{
                var figure=VacantPossession.shownTo(l,b,kept[0].getUUID());
                h.assertTrue(VacantPossession.after(b)&&l.getBlockState(b.offset(VacantSite.CHAIR_PIECE.at())).isAir()&&l.getBlockState(b.offset(VacantSite.LIVING_BULB)).getValue(VacantBlock.STAGE)==1,"the flat itself is unchanged for everyone");
                h.assertTrue(figure.owner().filter(kept[0].getUUID()::equals).isPresent()&&figure.broadcastToPlayer(kept[0])&&!figure.broadcastToPlayer(out),"only the one who put the chair back sees who is in it");
                h.assertTrue(figure.fade()==VacantPossession.KEPT_FADE&&figure.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(VacantSite.CHAIR_PIECE.at())))<=.05,"no more there than when the reader left, and sitting where the chair stood");
                h.assertTrue(VacantPossession.shownTo(l,b,out.getUUID())==null,"the chair-carrier has nobody");
                var mine=VacantPossession.heard(b,f.data(),kept[0].getUUID());var theirs=VacantPossession.heard(b,f.data(),out.getUUID());
                h.assertTrue(mine!=null&&mine.carry()==DoorSounds.Carry.SOMEONE&&mine.volume()==VacantPossession.KEPT_HEARD&&theirs!=null&&theirs.carry()==DoorSounds.Carry.EMPTY,"through the door: someone, faintly, for one; nothing for the other: "+mine+" "+theirs);
                h.assertTrue(Aftermath.returns(f.data(),kept[0].getUUID(),P)==1&&f.own(kept[0],P).getBoolean("Completed"),"their return counted too");
                // Neither reader is ever grouped into the other's crossing of it: an aftermath is each reader's own.
                h.assertTrue(!GroupCrossings.canJoin(f.data(),kept[0].getUUID(),P,null)&&!GroupCrossings.canJoin(f.data(),out.getUUID(),P,null),"nobody follows into an aftermath");
                kept[0].teleportTo(f.level(P),b.getX()+.5,b.getY()+30,b.getZ()+40,0,0);})
            .thenWaitUntil(()->h.assertTrue(VacantPossession.shownTo(l,b,kept[0].getUUID())==null,"the figure goes with the reader who saw it"))
            .thenExecute(()->{fresh[0]=f.player("vacant_after_fresh",P);at(f,fresh[0],3.5,0,-7.5,180);})
            .thenWaitUntil(()->h.assertTrue(!VacantPossession.after(b)&&VacantPossession.seat(b)==VacantPossession.STANDING&&VacantBlock.is(l.getBlockState(b.offset(-2,0,-9)),VacantBlock.Kind.DRESSER)&&VacantPossession.tenantActor(l,b)!=null,"someone who has not finished it starts moving day afresh"))
            .thenExecute(()->h.assertTrue(f.own(out,P).getBoolean("Completed")&&WitnessAccount.count(f.data(),out.getUUID())==credit,"the finished reader keeps their ending, and gains nothing"))
            .thenSucceed();
    }
    private static void finish(LiteraryTests.Fixture f,ServerPlayer p,String outcome){
        var own=f.own(p,P);own.putBoolean("Completed",true);own.putBoolean("Ready",true);own.putString("Outcome",outcome);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
        WitnessAccount.resolve(f.data(),p.getUUID(),WitnessAccount.Story.of(P.id()),outcome);
    }
    @AfterBatch(batch="vacant_after") public static void afterDone(ServerLevel l){close();}

    /**
     * 0.4.260: the pieces have weight. The bed carried alone slows the reader, and they jump lower; a second reader with empty hands
     * takes the other end and the weight is shared, a third of it each; walking apart lets go; the lamp needs nobody's help; and set
     * down, it weighs nothing at all.
     */
    @GameTest(template="empty",batch="vacant_weight",timeoutTicks=900)
    public static void aHeavyPieceIsSharedWithASecondPairOfHands(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);
        var p=f.player("vacant_lifter",P);var q=f.player("vacant_helper",P);
        var bed=VacantSite.piece(VacantBlock.Kind.BED);var lamp=VacantSite.piece(VacantBlock.Kind.LAMP);
        var heavy=VacantPossession.Weight.HEAVY;
        h.startSequence().thenExecute(()->f.chunks.hold(l,new AABB(b.getX()-8,b.getY()-8,b.getZ()-20,b.getX()+19,b.getY()+9,b.getZ()+2)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the flat is resident"))
            .thenExecute(()->{at(f,p,3.5,0,-7.5,180);at(f,q,3.5,0,-9.5,0);})
            .thenWaitUntil(()->h.assertTrue(VacantPossession.tenantActor(l,b)!=null,"moving day has begun"))
            .thenExecute(()->{
                h.assertTrue(VacantPossession.weight(VacantBlock.Kind.BED)==heavy&&VacantPossession.weight(VacantBlock.Kind.TABLE)==VacantPossession.Weight.MEDIUM&&VacantPossession.weight(VacantBlock.Kind.LAMP)==VacantPossession.Weight.LIGHT,"the bed heavy, the table a weight, the lamp light");
                h.assertTrue(click(f,p,bed.at())&&carrying(f,p),"the bed lifted");})
            .thenWaitUntil(()->h.assertTrue(Math.abs(VacantPossession.burden(p)-(1+heavy.speed))<1e-6&&Math.abs(VacantPossession.jumpBurden(p)-(1+heavy.jump))<1e-6,"carried alone, the bed slows the reader and they jump lower: "+VacantPossession.burden(p)+" "+VacantPossession.jumpBurden(p)))
            .thenExecute(()->{
                q.getInventory().setItem(q.getInventory().selected,new ItemStack(Items.BREAD));
                h.assertTrue(VacantPossession.help(q,p)&&VacantPossession.helpers(b,p.getUUID()).isEmpty(),"not with their hands full");
                q.getInventory().setItem(q.getInventory().selected,ItemStack.EMPTY);
                h.assertTrue(VacantPossession.help(q,p)&&VacantPossession.helpers(b,p.getUUID()).equals(java.util.List.of(q.getUUID())),"the helper takes the other end");})
            .thenWaitUntil(()->h.assertTrue(Math.abs(VacantPossession.burden(p)-(1+heavy.speed/3))<1e-6&&Math.abs(VacantPossession.burden(q)-(1+heavy.speed/3))<1e-6,"the weight shared, a third each: "+VacantPossession.burden(p)+" "+VacantPossession.burden(q)))
            .thenExecute(()->at(f,q,12.5,VacantSite.GROUND,-3.5,0))
            .thenWaitUntil(()->h.assertTrue(VacantPossession.helpers(b,p.getUUID()).isEmpty()&&VacantPossession.burden(q)==1&&Math.abs(VacantPossession.burden(p)-(1+heavy.speed))<1e-6,"walking apart lets go: all the weight back on the lifter"))
            .thenExecute(()->h.assertTrue(click(f,p,bed.at())&&!carrying(f,p),"the bed set back where it stood"))
            .thenWaitUntil(()->h.assertTrue(VacantPossession.burden(p)==1&&VacantPossession.jumpBurden(p)==1,"set down, it weighs nothing"))
            .thenExecute(()->{
                at(f,q,3.5,0,-9.5,0);
                h.assertTrue(click(f,p,lamp.at())&&carrying(f,p),"the lamp lifted");
                h.assertTrue(VacantPossession.help(q,p)&&VacantPossession.helpers(b,p.getUUID()).isEmpty(),"the lamp needs nobody's help");})
            .thenWaitUntil(()->h.assertTrue(Math.abs(VacantPossession.burden(p)-(1+VacantPossession.Weight.LIGHT.speed))<1e-6&&VacantPossession.jumpBurden(p)==1,"the lamp barely slows anyone, and costs no jump"))
            .thenExecute(()->{q.teleportTo(l,b.getX()+.5,b.getY()+30,b.getZ()+40,0,0);p.teleportTo(l,b.getX()+.5,b.getY()+30,b.getZ()+42,0,0);})
            .thenWaitUntil(()->h.assertTrue(VacantPossession.burden(p)==1&&!carrying(f,p),"out of the flat, the piece goes back and its weight with it"))
            .thenSucceed();
    }
    @AfterBatch(batch="vacant_weight") public static void weightDone(ServerLevel l){close();}
}
