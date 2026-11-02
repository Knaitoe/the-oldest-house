package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.253, After the People Lights Have Gone Off, played with real readers: the house built whole; the voices through the window and
 * Kelly's greeting; supper, and what she says when asked; Stan's visit, turned toward his daughter, and his offer left on the table to
 * be read; the mirror lowered and Kelly's look in it; the runner, asked about and rolled; Mark's case carried down when he agrees; her
 * chair's sound on each floor; the footsteps over the study and the likeness at the loft rail; the night, her name said until she wakes;
 * and each of the plans at the kitchen table resolving the story: one more night (the spare chair brought down in the morning),
 * leaving together by the garage, sharing the downstairs, and a second reader giving them the night.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class PeopleLightsTests {
    private PeopleLightsTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.PEOPLE_LIGHTS;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);PeopleLights.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}PeopleLights.clearAll();}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double y,double z,float yaw,float pitch){var b=f.base(P);p.teleportTo(f.level(P),b.getX()+x,b.getY()+y,b.getZ()+z,yaw,pitch);}
    private static boolean click(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var own=f.own(p,P);boolean handled=PeopleLights.click(p,f.base(P),rel,own);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);return handled;}
    /** Picks a slot in whatever choice the reader has open. */
    private static void choose(GameTestHelper h,ServerPlayer p,int slot,String what){h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"a choice is open: "+what);p.containerMenu.clicked(slot,0,ClickType.PICKUP,p);}
    private static void talkKelly(LiteraryTests.Fixture f,ServerPlayer p){PeopleLights.talk(p,PeopleLights.kellyActor(f.level(P),f.base(P)));}
    private static void talkMark(LiteraryTests.Fixture f,ServerPlayer p){PeopleLights.talk(p,PeopleLights.markActor(f.level(P),f.base(P)));}
    private static String book(ServerPlayer p,String needle){for(var s:p.getInventory().items)if(s.is(Items.WRITTEN_BOOK)){var c=s.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null){var all=String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());if(all.contains(needle))return all;}}return "";}
    private static AABB lot(BlockPos b){return new AABB(b.getX()-17,b.getY()-4,b.getZ()-32,b.getX()+19,b.getY()+17,b.getZ()+2);}

    /** The house as built, the reader on the walk hearing them through the window, then inside, greeted, at supper. */
    private static GameTestSequence supper(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer... readers){
        var b=f.base(P);var l=f.level(P);var p=readers[0];
        return h.startSequence().thenExecute(()->f.chunks.hold(l,lot(b)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.chunks.entityTicking(l,b,b.offset(0,0,-20)),"the house is resident"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&!LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored(),"a literary house indoors, one visit, built whole");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, and the quota derives from the pool");
                h.assertTrue(l.getBlockState(b.offset(PeopleLightsSite.OFFER_AT)).isAir()&&PeopleLightsSite.runnerLaid(l,b)&&!PeopleLightsSite.mirrorLowered(l,b)&&PeopleLightsBlock.is(l.getBlockState(b.offset(PeopleLightsSite.CASE_AT)),PeopleLightsBlock.Kind.CASE),"no offer yet, the runner down, the mirror high, the case upstairs");
                h.assertTrue(PeopleLightsSite.floor(l,b,new Vec3(0.5,0,-8.5))==PeopleLightsSite.Floor.TILE&&PeopleLightsSite.floor(l,b,new Vec3(4.5,0,-8.5))==PeopleLightsSite.Floor.WOOD&&PeopleLightsSite.floor(l,b,new Vec3(2.5,0,-16.5))==PeopleLightsSite.Floor.KITCHEN&&PeopleLightsSite.floor(l,b,new Vec3(-.5,0,-15.5))==PeopleLightsSite.Floor.RUNNER,"four floors, four sounds: tile, boards, kitchen tile, the runner");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=text==null?"":String.join(" ",text.pages().stream().map(page->page.raw().getString()).toList());
                h.assertTrue(all.contains("buy the house back")&&all.contains("rail up first"),"Stan's offer, and what he crossed out: "+all);
                for(var r:readers)at(f,r,.5,0,-1.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.stage(b)==PeopleLights.ARRIVE&&PeopleLights.kellyActor(l,b)!=null&&PeopleLights.markActor(l,b)!=null,"the household is home"))
            .thenIdle(120)
            .thenExecute(()->{h.assertTrue(PeopleLights.stage(b)==PeopleLights.ARRIVE,"nobody has come in yet");for(var r:readers)at(f,r,.5,0,-6.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.stage(b)==PeopleLights.SUPPER,"in through the door, greeted: supper"))
            .thenExecute(()->{var k=PeopleLights.kellyActor(l,b);h.assertTrue(k!=null&&k.phase()==PeopleLights.KELLY_CHAIR,"Kelly in her chair");
                talkKelly(f,p);choose(h,p,2,"Kelly");h.assertTrue(f.own(p,P).getBoolean("AccidentTold253"),"she tells the reader about the first night in the loft");});
    }

    /** Woken by her name said three times, with Mark there for it. */
    private static GameTestSequence woken(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p,GameTestSequence seq){
        var b=f.base(P);var l=f.level(P);
        return seq.thenWaitUntil(()->h.assertTrue(PeopleLights.stage(b)==PeopleLights.ROUND,"night"))
            .thenExecute(()->{var k=PeopleLights.kellyActor(l,b);h.assertTrue(k!=null&&k.phase()==PeopleLights.KELLY_DRAG&&PeopleLightsSite.ROUND.size()>4,"Kelly on the floor, pulling herself round, asleep");
                talkKelly(f,p);choose(h,p,2,"her name");h.assertTrue(f.own(p,P).getBoolean("Called253")&&!PeopleLights.flag(b,"woken"),"once is not enough");})
            .thenWaitUntil(()->{var k=PeopleLights.kellyActor(l,b);var m=PeopleLights.markActor(l,b);h.assertTrue(k!=null&&m!=null&&m.distanceToSqr(k)<64,"Mark comes down to her");})
            .thenExecute(()->{for(int i=0;i<2;i++){talkKelly(f,p);choose(h,p,2,"her name again");}
                h.assertTrue(PeopleLights.flag(b,"woken")&&!PeopleLights.flag(b,"markWoke")&&PeopleLights.stage(b)==PeopleLights.AWAKE&&f.own(p,P).getBoolean("WokeHer253"),"her name, until she wakes");
                var k=PeopleLights.kellyActor(l,b);h.assertTrue(k!=null&&k.phase()==PeopleLights.KELLY_CHAIR,"back in her chair, at the table");});
    }

    @GameTest(template="empty",batch="people_night",timeoutTicks=9000)
    public static void theEveningThroughToOneMoreNight(GameTestHelper h){
        var f=fixture(h);var p=f.player("people_guest",P);var b=f.base(P);var l=f.level(P);
        var seq=supper(h,f,p)
            .thenWaitUntil(()->h.assertTrue(PeopleLights.stage(b)==PeopleLights.STAN_VISIT&&PeopleLights.stanActor(l,b)!=null,"Stan comes in by the garage"))
            .thenExecute(()->{PeopleLights.talk(p,PeopleLights.stanActor(l,b));choose(h,p,2,"Stan");h.assertTrue(f.own(p,P).getBoolean("Redirected253"),"asked to answer Kelly, he does");})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.flag(b,"offer")&&PeopleLightsBlock.is(l.getBlockState(b.offset(PeopleLightsSite.OFFER_AT)),PeopleLightsBlock.Kind.OFFER),"his offer on the table"))
            .thenExecute(()->{h.assertTrue(click(f,p,PeopleLightsSite.OFFER_AT)&&f.own(p,P).getBoolean("ReadOffer253")&&!book(p,"buy the house back").isEmpty(),"the offer, read");
                h.assertTrue(click(f,p,PeopleLightsSite.MIRROR)&&!PeopleLightsSite.mirrorLowered(l,b),"the mirror can't be moved before anyone has asked");})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.stage(b)==PeopleLights.HOUSEHOLD,"Stan's gone; the ordinary house"))
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(click(f,p,PeopleLightsSite.MIRROR)&&PeopleLightsSite.mirrorLowered(l,b)&&f.own(p,P).getBoolean("Mirror253"),"the mirror, lowered a foot and a half");
                // The runner: asked about first, then rolled up because he wants to hear her.
                h.assertTrue(click(f,p,PeopleLightsSite.RUNNER_CELLS.get(2)),"the runner");choose(h,p,2,"runner");
                click(f,p,PeopleLightsSite.RUNNER_CELLS.get(2));choose(h,p,6,"roll it");h.assertTrue(!PeopleLightsSite.runnerLaid(l,b)&&"rolled".equals(f.own(p,P).getString("Runner253")),"rolled up at the end of the hall");
                // The case: his to move, until he agrees; then carried down to the table.
                h.assertTrue(click(f,p,PeopleLightsSite.CASE_AT)&&PeopleLightsBlock.is(l.getBlockState(b.offset(PeopleLightsSite.CASE_AT)),PeopleLightsBlock.Kind.CASE),"his case, his to move");
                talkMark(f,p);choose(h,p,2,"Mark");
                h.assertTrue(click(f,p,PeopleLightsSite.CASE_AT)&&p.getMainHandItem().is(PeopleLightsRegistry.CARRIED.get())&&l.getBlockState(b.offset(PeopleLightsSite.CASE_AT)).isAir(),"the case in the reader's hands");
                h.assertTrue(click(f,p,PeopleLightsSite.TABLE)&&PeopleLightsSite.caseDownstairs(l,b)&&!p.getMainHandItem().is(PeopleLightsRegistry.CARRIED.get())&&f.own(p,P).getBoolean("CaseDown253"),"set at the end of the kitchen table");})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.flag(b,"mirror"),"Kelly looks at herself in it"))
            .thenWaitUntil(()->{var k=PeopleLights.kellyActor(l,b);h.assertTrue(k!=null&&k.phase()==PeopleLights.KELLY_FLOOR,"on the floor by the sofa, comfortable");})
            .thenExecute(()->{click(f,p,PeopleLightsSite.SOFA);h.assertTrue(f.own(p,P).getBoolean("Cushion253"),"the cushion passed");
                talkKelly(f,p);choose(h,p,4,"a hand");talkKelly(f,p);choose(h,p,4,"a hand again");var k=PeopleLights.kellyActor(l,b);h.assertTrue(k!=null&&k.phase()==PeopleLights.KELLY_CHAIR,"back in the chair, on three");
                // Mark up at his desk; the reader downstairs.
                at(f,p,2.5,0,-9.5,180,0);})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.flag(b,"footsteps")&&f.own(p,P).getBoolean("HeardOverhead253"),"footsteps over the study, with Mark at his desk"))
            .thenExecute(()->p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atBottomCenterOf(b.offset(PeopleLightsSite.LIKENESS_AT)).add(0,1.6,0)))
            .thenWaitUntil(()->{var k=PeopleLights.kellyActor(l,b);var rail=Vec3.atBottomCenterOf(b.offset(PeopleLightsSite.LIKENESS_AT)).add(0,1.6,0);h.assertTrue(PeopleLights.flag(b,"likeness")&&f.own(p,P).getBoolean("SawLikeness253"),"someone standing at the loft rail, looking down: like her (stage "+PeopleLights.stage(b)+", kelly "+(k==null?"none":k.position().subtract(Vec3.atLowerCornerOf(b)))+", reader "+p.position().subtract(Vec3.atLowerCornerOf(b))+", look "+p.getViewVector(1).dot(rail.subtract(p.getEyePosition()).normalize())+")");})
            .thenExecute(()->{talkKelly(f,p);choose(h,p,6,"tell her");choose(h,p,5,"I don't know what it was");h.assertTrue(f.own(p,P).getBoolean("Told253"),"told, honestly");})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.stage(b)==PeopleLights.BEDTIME,"bedtime"))
            .thenExecute(()->at(f,p,1.5,0,-13.5,180,0));
        woken(h,f,p,seq)
            .thenExecute(()->{talkKelly(f,p);h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu,"the kitchen table");choose(h,p,3,"too soon");h.assertTrue(PeopleLights.stage(b)==PeopleLights.AWAKE,"nobody has said what she wants yet");
                talkKelly(f,p);choose(h,p,0,"what she wants");talkKelly(f,p);choose(h,p,3,"one more night");
                h.assertTrue(PeopleLights.stage(b)==PeopleLights.ONE_MORE_NIGHT&&PeopleLights.plan(b).equals("one_more_night"),"one more night, and Stan in the morning");
                choose(h,p,2,"stay");h.assertTrue(f.own(p,P).getBoolean("Stayed253"),"the reader stays until morning");})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.flag(b,"doorShut"),"the bedroom door shut on them"))
            .thenExecute(()->{click(f,p,PeopleLightsSite.SOFA);choose(h,p,4,"let the night pass");h.assertTrue(PeopleLights.stage(b)==PeopleLights.MORNING,"morning");})
            .thenIdle(140)
            .thenExecute(()->{h.assertTrue(click(f,p,PeopleLightsSite.SPARE_CHAIR)&&p.getMainHandItem().is(PeopleLightsRegistry.CARRIED.get()),"the spare chair, folded, carried down");
                h.assertTrue(click(f,p,PeopleLightsSite.BED_CHAIR)&&PeopleLightsBlock.is(l.getBlockState(b.offset(PeopleLightsSite.BED_CHAIR)),PeopleLightsBlock.Kind.CHAIR)&&f.own(p,P).getBoolean("BroughtChair253"),"unfolded beside the bed");})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"it is over"))
            .thenExecute(()->{var own=f.own(p,P);h.assertTrue(own.getString("Outcome").equals("one_more_night")&&f.data().isCompleted(P.id()),"one more night resolves the story");
                var text=book(p,"I brought the spare chair");h.assertTrue(text.contains("lowered the bathroom mirror")&&text.contains("rolled up the hall runner")&&text.contains("loft rail")&&text.contains("isn't the same as it being her")&&text.contains("They held hands"),"the account, only what the reader was there for: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="people_night") public static void nightDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="people_leave",timeoutTicks=6000)
    public static void callingStanTogetherLeavesByTheGarage(GameTestHelper h){
        var f=fixture(h);var p=f.player("people_leaver",P);var b=f.base(P);var l=f.level(P);
        var seq=supper(h,f,p).thenExecute(()->{PeopleLights.advance(l,b,PeopleLights.ROUND);at(f,p,.5,0,-9.5,180,0);});
        woken(h,f,p,seq)
            .thenExecute(()->{
                talkKelly(f,p);choose(h,p,4,"call Stan");h.assertTrue(PeopleLights.stage(b)==PeopleLights.AWAKE,"don't decide for her: ask her first");
                talkKelly(f,p);choose(h,p,0,"what she wants");talkKelly(f,p);choose(h,p,4,"call Stan");h.assertTrue(PeopleLights.stage(b)==PeopleLights.AWAKE,"Mark isn't ready to go tonight");
                talkMark(f,p);choose(h,p,1,"what he's been leaving out");h.assertTrue(f.own(p,P).getBoolean("AskedMark253"),"he says what the thing upstairs looks like, and why he listens");
                talkMark(f,p);choose(h,p,4,"call Stan");h.assertTrue(PeopleLights.stage(b)==PeopleLights.LEAVING&&PeopleLights.plan(b).equals("left_together"),"agreed by both, now");
                at(f,p,-4.5,0,-21.5,0,0);})
            .thenWaitUntil(()->h.assertTrue(l.getBlockState(b.offset(PeopleLightsSite.CASE_AT)).isAir()&&!PeopleLightsSite.caseDownstairs(l,b),"Mark takes his case"))
            .thenWaitUntil(()->h.assertTrue(PeopleLights.stanActor(l,b)!=null,"headlights up the drive"))
            .thenWaitUntil(()->h.assertTrue(PeopleLights.flag(b,"departed"),"both out through the garage, to Stan on the driveway"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"it is over"))
            .thenExecute(()->{h.assertTrue(f.own(p,P).getString("Outcome").equals("left_together"),"leaving together resolves the story");
                var text=book(p,"through the garage");h.assertTrue(text.contains("They called Stan together")&&text.contains("kept saying her name")&&!text.contains("talk to her"),"the account: what this reader was there for, and not Stan's visit, which they never turned: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="people_leave") public static void leaveDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="people_share",timeoutTicks=6000)
    public static void sharingTheDownstairsAndGivingThemTheNight(GameTestHelper h){
        var f=fixture(h);var p=f.player("people_sharer",P);var q=f.player("people_second",P);var b=f.base(P);var l=f.level(P);
        var seq=supper(h,f,p,q).thenExecute(()->{PeopleLights.advance(l,b,PeopleLights.ROUND);at(f,p,.5,0,-9.5,180,0);at(f,q,2.5,0,-12.5,180,0);});
        woken(h,f,p,seq)
            .thenExecute(()->{
                talkKelly(f,p);choose(h,p,5,"downstairs together");h.assertTrue(PeopleLights.stage(b)==PeopleLights.AWAKE,"downstairs is hers, upstairs his: not yet");
                talkKelly(f,p);choose(h,p,2,"can't say");h.assertTrue(f.own(p,P).getBoolean("Uncertain253"),"the reader says nobody can tell them what the house is doing");
                talkKelly(f,p);choose(h,p,1,"what Mark's leaving out");talkKelly(f,p);choose(h,p,5,"downstairs together");choose(h,p,6,"the runner, her way");
                h.assertTrue(PeopleLights.stage(b)==PeopleLights.SHARING&&PeopleLights.plan(b).equals("shared_the_downstairs")&&PeopleLightsSite.runnerLaid(l,b),"Kelly chooses the change: the runner, laid");
                var own=f.own(q,P);h.assertTrue(own.getString("Plan253").equals("shared_the_downstairs")&&!own.getBoolean("Proposed253"),"the second reader was at the table for it");})
            .thenWaitUntil(()->h.assertTrue(PeopleLightsSite.caseDownstairs(l,b)&&PeopleLights.flag(b,"cards"),"his work down at the table; she deals"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed")&&f.own(q,P).getBoolean("Completed"),"it is over, for both"))
            .thenExecute(()->{h.assertTrue(f.own(p,P).getString("Outcome").equals("shared_the_downstairs")&&f.own(q,P).getString("Outcome").equals("shared_the_downstairs"),"sharing the downstairs resolves the story for both readers");
                var text=book(p,"playing cards");h.assertTrue(text.contains("couldn't say what the house was doing"),"the account: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="people_share") public static void shareDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="people_gave",timeoutTicks=6000)
    public static void oneReaderGivesThemTheNightAnotherStays(GameTestHelper h){
        var f=fixture(h);var p=f.player("people_goer",P);var q=f.player("people_stayer",P);var b=f.base(P);var l=f.level(P);
        var seq=supper(h,f,p,q).thenExecute(()->{PeopleLights.advance(l,b,PeopleLights.ROUND);at(f,p,.5,0,-9.5,180,0);at(f,q,4.5,0,-7.5,180,0);});
        woken(h,f,p,seq)
            .thenExecute(()->{talkKelly(f,p);choose(h,p,0,"what she wants");talkKelly(f,p);choose(h,p,3,"one more night");
                choose(h,p,6,"give them the night");var own=f.own(p,P);h.assertTrue(own.getBoolean("Completed")&&own.getString("Outcome").equals("gave_them_the_night")&&!book(p,"I gave them the night").isEmpty(),"the reader who proposed it lets themself out: gave them the night");
                h.assertTrue(!f.own(q,P).getBoolean("Completed")&&PeopleLights.stage(b)==PeopleLights.ONE_MORE_NIGHT,"the other is still in the house");
                h.assertTrue(!PeopleLights.click(p,b,PeopleLightsSite.SOFA,f.own(p,P)),"a finished reader can't move the night on");})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.flag(b,"doorShut"),"the bedroom door shut"))
            .thenExecute(()->{click(f,q,PeopleLightsSite.BEDROOM_DOOR);h.assertTrue(PeopleLights.stage(b)==PeopleLights.AWAKE&&f.own(q,P).getBoolean("CalledThroughDoor253"),"calling through the door: they come out, and talk again");
                talkKelly(f,q);choose(h,q,0,"what she wants");talkKelly(f,q);choose(h,q,3,"one more night");choose(h,q,2,"stay");})
            .thenWaitUntil(()->h.assertTrue(PeopleLights.flag(b,"doorShut"),"shut again"))
            .thenExecute(()->{click(f,q,PeopleLightsSite.SOFA);choose(h,q,4,"let the night pass");})
            .thenWaitUntil(()->h.assertTrue(f.own(q,P).getBoolean("Completed"),"by morning, without the spare chair, it is over"))
            .thenExecute(()->{h.assertTrue(f.own(q,P).getString("Outcome").equals("one_more_night")&&f.own(p,P).getString("Outcome").equals("gave_them_the_night"),"each reader keeps their own ending");
                var text=book(q,"They held hands");h.assertTrue(text.contains("called through their door")&&!text.contains("I brought the spare chair"),"the second reader's account: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="people_gave") public static void gaveDone(ServerLevel l){close();}
}
