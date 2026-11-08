package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import java.util.List;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
 * <p>
 * 0.4.369: the cases move to their own namespace and CI job (the literary job was at its time limit), and two winters are added: a
 * gentle one (the sleeve on the chair, three mornings laid out, the ticket on the mat, seen in the dark television and answering yes,
 * the sleepwalk brought back with the lamp, the House's kind pages) and a hard one (the tin shut, nothing laid out, the ticket hidden,
 * stepping out of the glass, the sleepwalk let go to the door, the phone left ringing, the House's worse pages).
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_second_person") @PrefixGameTestTemplate(false)
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
    private static void thing(CompoundTag own,String th,String state,String where){var t=own.getCompound("Thing_"+th);t.putString("State",state);if(where!=null)t.putString("Where",where);own.put("Thing_"+th,t);}
    private static CompoundTag own(LiteraryTests.Fixture f,ServerPlayer p){return f.own(p,P);}
    private static void save(LiteraryTests.Fixture f,ServerPlayer p,CompoundTag own){LiteraryVignettes.save(f.data(),p.getUUID(),P,own);}
    /** Into the warm cupboard with the door shut. */
    private static void cupboard(LiteraryTests.Fixture f,ServerPlayer p){at(f,p,-6.5,UP,-28.5);shut(f.level(P),f.base(P).offset(SecondPersonSite.CUPBOARD_DOOR));}
    /** Autumn over, with what the quiet rooms showed and what the year left behind; then the reader goes into the cupboard. */
    private static void autumnOver(LiteraryTests.Fixture f,ServerPlayer p,List<String> rooms,java.util.function.Consumer<CompoundTag> year){
        var own=own(f,p);own.putInt("Season",SecondPerson.AUTUMN);own.putInt("Step",SecondPerson.AUTUMN_DONE);own.putInt("T",0);own.putInt("Sub",2);own.putInt("SubT",0);
        var snap=own.getCompound("Snap");var walk=new ListTag();for(var k:rooms)walk.add(StringTag.valueOf(k));snap.put("Walk",walk);if(rooms.contains("kitchen"))snap.put("Drawing",new CompoundTag());own.put("Snap",snap);
        thing(own,"routine","homed",null);year.accept(own);save(f,p,own);
        var l=f.level(P);var b=f.base(P);SecondPersonSite.season(l,b,1);SecondPersonSite.glass(l,b,0);SecondPersonSite.thing(l,b,SecondPersonSite.ROUTINE_HOME,"routine",own.getCompound("Snap").getInt("Routine"));
        if(own.getCompound("Thing_coat").getString("State").equals("homed"))SecondPersonSite.coat(l,b,"",true);
        cupboard(f,p);
    }
    /** The routine out of the drawer under the basin and laid back where they will see it. */
    private static void layOut(GameTestHelper h,LiteraryTests.Fixture f,ServerPlayer p){
        h.assertTrue(own(f,p).getCompound("Thing_routine").getString("State").equals("drawer"),"in the worst of it, their routine has gone into the drawer: "+own(f,p).getCompound("Thing_routine"));
        h.assertTrue(click(f,p,SecondPersonSite.VANITY,Direction.UP)&&SecondPerson.held(p).equals("routine"),"taken out of the drawer");
        h.assertTrue(click(f,p,SecondPersonSite.VANITY,Direction.UP)&&own(f,p).getBoolean("LaidToday"),"laid out on the shelf over the basin");
    }

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
            .thenExecute(()->cupboard(f,p))
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.WINTER,"the autumn goes by in the cupboard, into the winter"))
            .thenExecute(()->{
                // The winter, to its end: the phone already let ring.
                var own=own(f,p);own.putInt("Step",SecondPerson.WINTER_DONE);own.putInt("T",0);own.putInt("Sub",0);own.putInt("SubT",0);own.putString("Call3","unanswered");save(f,p,own);
                cupboard(f,p);})
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

    @GameTest(template="empty",batch="second_gentle_winter",timeoutTicks=12000)
    public static void aGentleWinterAndTheHousesKindPages(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_gentle",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{f.source(p,P);street(f,p);
                // The kitchen table, the bathroom and the porch were this reader's quiet rooms; the coat has been under the bed since the summer.
                autumnOver(f,p,List.of("kitchen","bathroom","porch"),own->{thing(own,"coat","hidden","under the bed");own.putString("Evening","helped");own.putString("Night","shut");});})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.WINTER,"into the winter"))
            .thenExecute(()->{var own=own(f,p);String away=SecondPersonStory.whileAway(own,2);
                h.assertTrue(away.contains("Iris died")&&away.contains("a borrowed coat")&&away.contains("under the bed"),"Iris died; the funeral in a borrowed coat; the coat found under the bed afterwards: "+away);
                h.assertTrue(SecondPersonSite.coatHung(l,b)&&own.getCompound("Thing_coat").getString("State").equals("homed"),"the coat back on its hook");
                h.assertTrue(l.getBlockState(b.offset(-16,0,-11)).is(net.minecraft.world.level.block.Blocks.SNOW)||l.getBlockState(b.offset(16,0,-11)).is(net.minecraft.world.level.block.Blocks.SNOW),"snow lying on the far pavement");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(SecondPersonSite.tin(l,b)==0&&own(f,p).getInt("Sub")>=1,"they come back from the funeral and set her tin on the counter: "+own(f,p)))
            .thenExecute(()->{
                h.assertTrue(click(f,p,SecondPersonSite.TIN_AT,Direction.UP)&&SecondPersonSite.tin(l,b)==1&&own(f,p).getBoolean("TinOpened"),"Iris's tin opened");
                h.assertTrue(click(f,p,SecondPersonSite.TIN_AT,Direction.UP)&&SecondPerson.held(p).equals("sleeve")&&SecondPersonSite.tin(l,b)==2,"the sleeve taken out");
                h.assertTrue(click(f,p,SecondPersonSite.ARMCHAIR,Direction.UP)&&SecondPersonSite.sleeve(l,b)==0&&own(f,p).getBoolean("SleeveOut"),"laid on the arm of the armchair");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_MORNING&&own(f,p).getInt("Care")==0,"the first winter morning"))
            .thenExecute(()->{layOut(h,f,p);street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("LaidCount")==1,"the first morning: they leave it where it is"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_DAY&&"ticket".equals(SecondPersonSite.thingAt(l,b.offset(SecondPersonSite.TICKET_HOME))),"the post on the hall table, unopened, with the ticket in it"))
            .thenExecute(()->{
                h.assertTrue(click(f,p,SecondPersonSite.TICKET_HOME,Direction.UP)&&SecondPerson.held(p).equals("ticket"),"the ticket taken up");
                h.assertTrue(click(f,p,SecondPersonSite.MAT.below(),Direction.UP)&&"ticket".equals(SecondPersonSite.thingAt(l,b.offset(SecondPersonSite.MAT))),"on the mat by the door, where it cannot be missed");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_EVENING&&own(f,p).getInt("Sub")>=1,"evening, in the armchair, the television off"))
            .thenExecute(()->at(f,p,-2.5,UP,-16.5))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("SleeveFound"),"Iris's sleeve found on the arm of the chair"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getString("Seen").equals("still")&&own(f,p).getString("SeenIn").equals("tv"),"behind their chair, still, in the dark screen: \"That's my face\", then \"Are you me?\": "+own(f,p)))
            .thenExecute(()->{h.assertTrue(click(f,p,new BlockPos(-3,UP+1,-12),Direction.SOUTH)&&Talk.waiting(p,"sp_word"),"breath on the cold street window");say(h,p,"YES");
                h.assertTrue(own(f,p).getString("SeenAnswer").equals("YES"),"the answer written: "+own(f,p));})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Walking")&&own(f,p).getBoolean("DrewAtNight"),"in the night they get up asleep and draw at the kitchen table in the dark: "+own(f,p)))
            .thenExecute(()->{if(SecondPersonSite.lampOn(l,b))click(f,p,SecondPersonSite.LAMP,Direction.UP);h.assertTrue(click(f,p,SecondPersonSite.LAMP,Direction.UP)&&SecondPersonSite.lampOn(l,b),"the lamp turned on");})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getString("Sleepwalk").equals("brought")&&own(f,p).getString("BroughtBy").equals("lamp"),"brought back with the lamp"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_MORNING&&own(f,p).getInt("Care")==1,"the second morning"))
            .thenExecute(()->{h.assertTrue(SecondPerson.held(p).isEmpty(),"empty hands");layOut(h,f,p);street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("LaidCount")==2,"the second morning: they use it, and cry"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getString("Ticket").equals("mat"),"the ticket where they will find it"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Went"),"they went, and came back: "+own(f,p)))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_MORNING&&own(f,p).getInt("Care")==2,"the third morning"))
            .thenExecute(()->{layOut(h,f,p);street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("AllRight"),"the third morning: \"All right. All right.\""))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.WINTER_DONE&&own(f,p).getBoolean("SleeveFinished")&&SecondPersonSite.sleeve(l,b)==1,"the end of the winter: the sleeve finished and folded on the chair"))
            .thenExecute(()->cupboard(f,p))
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.MORNING,"the rest of the winter goes by in the cupboard"))
            .thenExecute(()->{var own=own(f,p);var house=SecondPersonStory.list(own,"HousePages");
                h.assertTrue(house.size()==2&&house.get(0).contains("end of their bed while they slept")&&house.get(1).contains("kettle"),"the House carried on in the reader's name, as gently as they had: "+house);
                h.assertTrue(own.getString("Decision").equals("stay"),"they have decided to stay");
                h.assertTrue(SecondPersonStory.latest(own).contains("The pages you did not turn")&&!SecondPersonStory.list(own,"Pages").contains(house.get(0)),"the House's pages kept apart from the reader's own");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Ready"),"the book on the bench"))
            .thenExecute(()->{String text=account(f,p);
                for(var want:List.of("What lodger?","a borrowed coat","There you are","and finished it","All right. All right.","drew at the kitchen table without a light on","brought them back with the lamp","They went.","That's my face","You wrote YES","These are not your pages","put the kettle on","decided to stay"))
                    h.assertTrue(text.contains(want),"the account says \""+want+"\": "+text);
                f.read(p,P,LiteraryRooms.ending(P));})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Completed"),"the account read"))
            .thenSucceed();
    }
    @AfterBatch(batch="second_gentle_winter") public static void gentleDone(ServerLevel l){close();}

    @GameTest(template="empty",batch="second_hard_winter",timeoutTicks=9000)
    public static void aHardWinterAndTheHousesWorsePages(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_hard",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{f.source(p,P);street(f,p);
                // Only the porch among the quiet rooms; a reader who knocks on the walls of the flat, and frightened them in the autumn.
                autumnOver(f,p,List.of("porch"),own->{thing(own,"coat","homed",null);own.putInt("Habit_knock",3);own.putString("Night","tap");own.putString("Evening","interfered");own.putString("Call1","put_down");});})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.WINTER&&SecondPersonStory.whileAway(own(f,p),2).contains("the coat from the hook"),"into the winter; their coat on its hook for the funeral"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_MORNING&&SecondPersonSite.tin(l,b)==0,"the first morning, Iris's tin left shut"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getString("Care0").equals("not"),"nothing laid out: they stand at the basin and do nothing"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_DAY&&"ticket".equals(SecondPersonSite.thingAt(l,b.offset(SecondPersonSite.TICKET_HOME))),"the ticket in the post"))
            .thenExecute(()->{click(f,p,SecondPersonSite.TICKET_HOME,Direction.UP);
                h.assertTrue(click(f,p,SecondPersonSite.CUPBOARD.below(),Direction.UP)&&own(f,p).getCompound("Thing_ticket").getString("State").equals("hidden"),"hidden in the airing cupboard");})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_EVENING&&own(f,p).getInt("Sub")>=1,"evening in the armchair"))
            .thenExecute(()->at(f,p,-2.5,UP,-16.5))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("SeenOpen"),"they see somebody in the dark screen"))
            .thenExecute(()->street(f,p))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getString("Seen").equals("gone"),"the reader stepped out of the glass"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Walking"),"in the night they get up asleep: "+SecondPersonStory.list(own(f,p),"WalkList")))
            .thenExecute(()->h.assertTrue(SecondPersonStory.list(own(f,p),"WalkList").equals(List.of("knock","latch")),"the reader's habit, then the latch: "+SecondPersonStory.list(own(f,p),"WalkList")))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getString("Sleepwalk").equals("left")&&l.getBlockState(b.offset(SecondPersonSite.FLAT_DOOR)).getValue(DoorBlock.OPEN),"let be, they wake in the open doorway, cold"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Step")==SecondPerson.W_MORNING&&own(f,p).getInt("Care")==1,"the second morning"))
            .thenExecute(()->{
                // The rest of the winter as it would go: the ticket never found; to the winter's end.
                var own=own(f,p);own.putString("Ticket","hidden");own.putInt("Step",SecondPerson.WINTER_DONE);own.putInt("T",0);own.putInt("Sub",0);own.putInt("SubT",0);save(f,p,own);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Ringing")==3,"at the end of the winter the phone rings"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getString("Call3").equals("unanswered"),"they do not answer it, and neither does the reader"))
            .thenExecute(()->cupboard(f,p))
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.MORNING,"the rest of the winter in the cupboard"))
            .thenExecute(()->{var own=own(f,p);var house=SecondPersonStory.list(own,"HousePages");
                h.assertTrue(house.size()==2&&house.get(0).contains("stood at the end of their bed all night")&&house.get(1).contains("opened every door"),"the House, a little worse than the reader: "+house);
                h.assertTrue(own.getString("Decision").equals("go"),"they have decided to go");
                String text=account(f,p);
                for(var want:List.of("tin stayed shut","stayed in the drawer under the basin, and you let it be","knocked on the wall, the way you did","held the latch","woke in the open doorway, cold","you hid it","did not answer it","stepped out of the glass","stood at the end of their bed all night"))
                    h.assertTrue(text.contains(want),"the account says \""+want+"\": "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="second_hard_winter") public static void hardDone(ServerLevel l){close();}
}
