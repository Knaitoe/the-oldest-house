package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.274, Second Person, played by a real reader. A summer: the book read, the photograph put away before they reach it, the coat
 * pushed under the bed, the hall glass turned to the wall, the first call left off the hook and their door shut on the first night,
 * then the season let go by in the airing cupboard. And an autumn evening: the reader stands in the dark window and Sam sees someone,
 * is in the picture of the room, lets the phone ring, lets the rest of the year go by, turns the glass round on the last morning and
 * goes out by the door on the latch, to the book on the bus-stop bench and the account.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class SecondPersonTests {
    private SecondPersonTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.SECOND_PERSON;
    private static final int UP=SecondPersonSite.UP;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);SecondPerson.clearAll();Talk.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}SecondPerson.clearAll();Talk.clearAll();}
    private static void at(LiteraryTests.Fixture f,ServerPlayer p,double x,double y,double z){var b=f.base(P);p.stopRiding();p.teleportTo(f.level(P),b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
    /** Out on the road, where the year still counts but nobody in the flat can see the reader. */
    private static void street(LiteraryTests.Fixture f,ServerPlayer p){at(f,p,.5,-1,-5.5);}
    private static boolean click(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel,Direction face){var own=f.own(p,P);boolean handled=SecondPerson.click(p,f.base(P),rel,own,face);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);return handled;}
    private static void say(GameTestHelper h,ServerPlayer p,String reply){h.assertTrue(Talk.choose(p,reply),"the panel offers \""+reply+"\": "+Talk.replies(p)+" after "+Talk.lines(p));}
    private static AABB lot(BlockPos b){return new AABB(b.getX()-18,b.getY()-4,b.getZ()-33,b.getX()+18,b.getY()+13,b.getZ()+1);}
    private static String account(LiteraryTests.Fixture f,ServerPlayer p){var c=LiteraryTexts.ending(p,P,f.own(p,P)).get(DataComponents.WRITTEN_BOOK_CONTENT);return c==null?"":String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
    private static int step(LiteraryTests.Fixture f,ServerPlayer p){return f.own(p,P).getInt("Step");}
    private static int season(LiteraryTests.Fixture f,ServerPlayer p){return f.own(p,P).getInt("Season");}
    private static void shut(ServerLevel l,BlockPos at){var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock d)d.setOpen(null,l,s,at,false);}

    private static GameTestSequence resident(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p){
        var b=f.base(P);var l=f.level(P);
        return h.startSequence().thenExecute(()->f.chunks.hold(l,lot(b)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&f.chunks.entityTicking(l,b,b.offset(0,0,-20)),"the street and the flat are resident"));
    }

    @GameTest(template="empty",batch="second_summer",timeoutTicks=9000)
    public static void theSummerGoesByInTheCupboard(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_summer",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneInstances.copyable(P),"a literary street under the open sky, one story, built whole, a copy for each reader");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, and the quota derives from the pool");
                var own=f.own(p,P);h.assertTrue(own.contains("Snap")&&own.getInt("Season")==SecondPerson.SUMMER&&own.getInt("Step")==SecondPerson.ARRIVE,"the House's read taken once, on arrival, and the year at its start");
                h.assertTrue(SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSite.BOOK)),SecondPersonBlock.Kind.BOOK)&&SecondPersonBlock.stage(l.getBlockState(b.offset(SecondPersonSite.PHOTO_BOX)))==0&&SecondPerson.resident(l,b)==null,"the book on the shelf, the boxes taped, nobody home yet");
                f.source(p,P);h.assertTrue(f.own(p,P).getBoolean("Read_Source"),"the book's opening read to the end");
                at(f,p,-2.5,UP,-17.5);})
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPerson.UNPACK&&SecondPerson.resident(l,b)!=null,"somebody comes up the stairs with the last box: "+f.own(p,P)))
            .thenExecute(()->{
                // The photograph, out of its box and onto the bookcase, before they get to it.
                h.assertTrue(click(f,p,SecondPersonSite.PHOTO_BOX,Direction.UP)&&SecondPersonBlock.stage(l.getBlockState(b.offset(SecondPersonSite.PHOTO_BOX)))==1,"the box opened");
                h.assertTrue(click(f,p,SecondPersonSite.PHOTO_BOX,Direction.UP)&&SecondPerson.held(p).equals("photo"),"the photograph carried");
                h.assertTrue(click(f,p,SecondPersonSite.SHELF_A,Direction.UP)&&SecondPerson.held(p).isEmpty()&&SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSite.PHOTO_HOME)),SecondPersonBlock.Kind.PHOTO),"put away on the bookcase, where it lives");
                // The coat, out of its box and under the bed.
                click(f,p,SecondPersonSite.COAT_BOX,Direction.UP);click(f,p,SecondPersonSite.COAT_BOX,Direction.UP);h.assertTrue(SecondPerson.held(p).equals("coat"),"the coat carried");
                h.assertTrue(click(f,p,SecondPersonSite.BED_HEAD,Direction.UP)&&SecondPerson.held(p).isEmpty()&&f.own(p,P).getCompound("Thing_coat").getString("State").equals("hidden"),"pushed under the bed");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPerson.GLASS_HUNG,"the rest unpacked and the boxes flattened: "+f.own(p,P)))
            .thenExecute(()->{var found=SecondPersonStory.list(f.own(p,P),"Found");
                h.assertTrue(found.contains("photo:put_away")&&found.contains("coat:hidden"),"what they found already put away, and what they never found: "+found);
                h.assertTrue(SecondPersonSite.pictureUp(l,b)&&"routine".equals(SecondPersonSite.thingAt(l,b.offset(SecondPersonSite.ROUTINE_HOME)))&&!SecondPersonSite.coatHung(l,b),"the drawing on the fridge, the routine on the bathroom shelf, and no coat on the hook");})
            .thenWaitUntil(()->h.assertTrue(SecondPersonSite.glass(l,b)==0,"the hall glass hung, facing the hall"))
            .thenExecute(()->{at(f,p,1.5,UP,-21.5);h.assertTrue(click(f,p,SecondPersonSite.GLASS,Direction.WEST)&&SecondPersonSite.glass(l,b)==1,"turned to the wall");street(f,p);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("GlassTurned"),"they find it turned, and leave it"))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Ringing")==1,"the bath running, and the phone ringing: "+f.own(p,P)))
            .thenExecute(()->{at(f,p,-5.5,UP,-18.5);h.assertTrue(click(f,p,SecondPersonSite.PHONE,Direction.UP)&&Talk.waiting(p,"sp_call_1"),"the receiver lifted: a voice in the panel");
                h.assertTrue(Talk.lines(p).stream().anyMatch(s->s.contains("Just checking you're alive")),"an old friend, for a reader the House has no home for: "+Talk.lines(p));
                say(h,p,"Leave it off the hook.");h.assertTrue(f.own(p,P).getString("Call1").equals("off")&&SecondPersonSite.phoneOff(l,b),"left off the hook");street(f,p);})
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPerson.NIGHT&&f.own(p,P).getInt("Sub")==1&&SecondPerson.resident(l,b)!=null&&SecondPerson.resident(l,b).isSleeping(),"night: in bed, and not sleeping: "+f.own(p,P)))
            .thenExecute(()->shut(l,b.offset(SecondPersonSite.BED_DOOR)))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("Night").equals("shut"),"their door shut on them, and they sleep"))
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPerson.SUMMER_DONE,"morning"))
            .thenExecute(()->{at(f,p,-6.5,UP,-28.5);shut(l,b.offset(SecondPersonSite.CUPBOARD_DOOR));})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.AUTUMN,"in the warm cupboard with the door shut, the summer goes by"))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSite.ROTA)),SecondPersonBlock.Kind.ROTA)&&l.getBlockState(b.offset(SecondPersonSite.PHOTO_BOX)).isAir(),"autumn in the flat: the rota on the wall, the boxes gone");
                h.assertTrue(SecondPersonStory.whileAway(own,1).contains("off the hook")&&SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSite.PHOTO_HOME)),SecondPersonBlock.Kind.PHOTO),"what happened while the reader was away, and the photograph still where they put it");
                h.assertTrue(SecondPerson.book(p,b,own)&&p.containerMenu instanceof LiteraryVignettes.Pages pages&&pages.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().get(0).raw().getString().contains("While you were in the cupboard"),"the book opens at its latest page");
                p.closeContainer();})
            .thenSucceed();
    }
    @AfterBatch(batch="second_summer") public static void summerDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="second_autumn",timeoutTicks=9000)
    public static void anAutumnEveningThenTheDoorOnTheLatch(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_autumn",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{
                f.source(p,P);street(f,p);
                // The summer let go by: autumn, the evening about to begin, the glass hung facing the hall.
                var own=f.own(p,P);own.putInt("Season",SecondPerson.AUTUMN);own.putInt("Step",SecondPerson.EVENING);own.putInt("T",0);own.putInt("Sub",0);own.putInt("SubT",0);
                own.putBoolean("EveLamp",false);own.putBoolean("EveKitchenDoor",false);LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                SecondPersonSite.season(l,b,1);SecondPersonSite.cloth(l,b,0);SecondPersonSite.glass(l,b,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Sub")==1&&SecondPerson.sam(l,b)!=null&&SecondPerson.resident(l,b)!=null,"Sam is in, and they sit down to supper"))
            .thenExecute(()->at(f,p,-6.5,UP,-20.6))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("Evening").equals("seen"),"in the dark window behind their chair, Sam sees somebody standing"))
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPerson.PHOTO&&SecondPerson.sam(l,b)==null,"the evening ends there: Sam goes home"))
            .thenExecute(()->at(f,p,-4.5,UP,-16.5))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("Photo").equals("in_deleted"),"in the picture of the room, after a bad evening: deleted: "+f.own(p,P)))
            .thenExecute(()->street(f,p))
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getString("Call2").equals("left"),"the phone let ring, and caught"))
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPerson.AUTUMN_DONE,"no guest to stay: the flat goes quiet"))
            .thenExecute(()->{at(f,p,-6.5,UP,-28.5);shut(l,b.offset(SecondPersonSite.CUPBOARD_DOOR));})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.MORNING,"the rest of the year goes by in the cupboard"))
            .thenExecute(()->{var own=f.own(p,P);
                h.assertTrue(own.getString("Decision").equals("go")&&!SecondPersonStory.stays(own),"from the year, they have decided to go");
                h.assertTrue(l.getBlockState(b.offset(SecondPersonSite.FLAT_DOOR)).getValue(DoorBlock.OPEN),"the front door on the latch");
                at(f,p,1.5,UP,-21.5);h.assertTrue(click(f,p,SecondPersonSite.GLASS,Direction.WEST)&&f.own(p,P).getString("LastAct").equals("glass"),"one last thing: the glass turned round");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Ready")&&SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSite.BENCH_BOOK)),SecondPersonBlock.Kind.BOOK),"down on the street, they go; the book waits on the bench: "+f.own(p,P)))
            .thenExecute(()->{String text=account(f,p);
                h.assertTrue(text.contains("decided to go")&&text.contains("turned the glass round")&&text.contains("deleted it")&&text.contains("somebody standing there")&&text.contains("let it ring"),"the account, from the record: "+text);
                f.read(p,P,LiteraryRooms.ending(P));})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getBoolean("Completed"),"the account read"))
            .thenExecute(()->h.assertTrue(f.data().isCompleted(P.id()),"the story resolved"))
            .thenSucceed();
    }
    @AfterBatch(batch="second_autumn") public static void autumnDone(ServerLevel l){close();}
}
