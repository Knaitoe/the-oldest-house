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
    /** 0.4.633: the spring to its end, without the deciding; then the cupboard, and the last morning reads the year as it stands. */
    private static void springOver(LiteraryTests.Fixture f,ServerPlayer p){var own=own(f,p);own.putInt("Step",SecondPersonSpring.SPRING_DONE);own.putInt("T",0);own.putInt("Sub",0);own.putInt("SubT",0);save(f,p,own);cupboard(f,p);}
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
                own.putBoolean("EveLamp",false);own.remove("EveDoorBanged");LiteraryVignettes.save(f.data(),p.getUUID(),P,own);
                SecondPersonSite.season(l,b,1);SecondPersonSite.cloth(l,b,0);SecondPersonSite.glass(l,b,0);})
            .thenWaitUntil(()->h.assertTrue(f.own(p,P).getInt("Sub")==1&&SecondPerson.sam(l,b)!=null&&SecondPerson.resident(l,b)!=null,"Sam is in, and they sit down to supper"))
            .thenExecute(()->at(f,p,-6.5,UP,-20.6))
            .thenWaitUntil(()->{var own=f.own(p,P);var r=p.position().subtract(b.getX(),b.getY(),b.getZ());h.assertTrue(own.getString("Evening").equals("seen"),String.format(java.util.Locale.ROOT,"in the dark window behind their chair, Sam sees somebody standing: the evening is '%s', step %d sub %d, the reader at (%.1f, %.1f, %.1f), lamp %s, door banged %s, kitchen door open %s, cold %s, knocked %s",own.getString("Evening"),own.getInt("Step"),own.getInt("Sub"),r.x,r.y,r.z,own.getBoolean("EveLamp"),own.getBoolean("EveDoorBanged"),SecondPerson.isOpen(l,b,SecondPersonSite.KITCHEN_DOOR),own.getBoolean("ColdBySam"),own.getBoolean("KnockedInEvening")));})
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
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.SPRING,"the winter goes by in the cupboard, into the spring"))
            .thenExecute(()->springOver(f,p))
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
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.SPRING,"the winter goes by in the cupboard, into the spring"))
            .thenExecute(()->springOver(f,p))
            // 0.4.636: a year this gentle, met in the glass, ends not on the last morning but on the last evening, with two cups.
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPersonCups.TWO_CUPS,"the rest of the spring goes by in the cupboard, into the last evening"))
            .thenExecute(()->{var own=own(f,p);var house=SecondPersonStory.list(own,"HousePages");
                h.assertTrue(house.size()==2&&house.get(0).contains("end of their bed while they slept")&&house.get(1).contains("kettle"),"the House carried on in the reader's name, as gently as they had: "+house);
                h.assertTrue(own.getString("Decision").equals("stay"),"they have decided to stay");
                h.assertTrue(SecondPersonStory.latest(own).contains("The pages you did not turn")&&!SecondPersonStory.list(own,"Pages").contains(house.get(0)),"the House's pages kept apart from the reader's own");
                at(f,p,-4.5,UP,-21.5);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Sub")==2&&own(f,p).getInt("T")-own(f,p).getInt("SubT")>45,"two cups, and one question"))
            .thenExecute(()->{h.assertTrue(click(f,p,new BlockPos(-3,UP+1,-12),Direction.SOUTH)&&Talk.waiting(p,"sp_word"),"breath on the street window");say(h,p,"STAY");Talk.close(p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Ready"),"the book on the bench"))
            .thenExecute(()->{String text=account(f,p);
                for(var want:List.of("What lodger?","a borrowed coat","There you are","until it was done","All right. All right.","drew at the kitchen table without a light on","brought them back with the lamp","They went.","That's my face","You wrote YES","These are not your pages","put the kettle on","They said, \"You can stay.\""))
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
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.SPRING,"the winter goes by in the cupboard, into the spring"))
            .thenExecute(()->springOver(f,p))
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

    /**
     * 0.4.633, spring: the let. The winter's turn opens the spring with the tenancy. The agent comes up the stairs with a clipboard; a
     * reader who stands close makes the air go cold round them, and the rent stays where it was. While the resident decides, their coat
     * laid by the door weighs toward going; what they decide is kept for the last morning. The post brings a form for the flat in the
     * reader's handwriting with the reader's name; read, the resident reads it too and asks the room "Is this you?", and the word written
     * on the glass is the one they keep. The account tells it all, and the last morning keeps the spring's decision.
     */
    @GameTest(template="empty",batch="second_spring",timeoutTicks=12000)
    public static void springTheLetTheAgentAndTheForm(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_spring",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{f.source(p,P);street(f,p);
                autumnOver(f,p,List.of("porch","kitchen"),own->{thing(own,"coat","homed",null);var snap=own.getCompound("Snap");snap.putString("Home","HAVEN");own.put("Snap",snap);own.putString("Evening","helped");});})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.WINTER,"into the winter"))
            .thenExecute(()->{var own=own(f,p);own.putInt("Step",SecondPerson.WINTER_DONE);own.putInt("T",0);own.putInt("Sub",0);own.putInt("SubT",0);own.putString("Call3","unanswered");save(f,p,own);cupboard(f,p);})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.SPRING&&step(f,p)==SecondPersonSpring.S_AGENT,"the winter goes by in the cupboard, and the tenancy comes up"))
            .thenExecute(()->{h.assertTrue(SecondPersonStory.latest(own(f,p)).contains("Then the tenancy came up.")&&SecondPersonStory.latest(own(f,p)).contains("letting agent"),"the book says so: "+SecondPersonStory.latest(own(f,p)));
                h.assertTrue(l.getBlockState(b.offset(-1,4,-10)).is(net.minecraft.world.level.block.Blocks.CHERRY_LEAVES),"the street tree in blossom");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(SecondPerson.agent(l,b)!=null&&own(f,p).getInt("Sub")>=1,"the agent comes up the stairs and is let in"))
            .thenWaitUntil(()->{var a=SecondPerson.agent(l,b);
                // Close enough to the agent for the air to go cold round them, wherever the walk round takes them.
                if(a!=null){p.teleportTo(l,a.getX()+.7,a.getY(),a.getZ(),180,0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
                h.assertTrue(own(f,p).getBoolean("AgentCold"),"the agent feels the cold");})
            .thenExecute(()->{h.assertTrue(SecondPerson.agent(l,b).getMainHandItem().is(SecondPersonRegistry.CARRIED.get()),"the agent has the clipboard in hand");street(f,p);})
            .thenWaitUntil(()->h.assertTrue("same".equals(own(f,p).getString("Rent")),"the rent stays where it was: "+own(f,p).getString("Rent")))
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPersonSpring.S_DECIDE,"the agent gone, they sit with the letter"))
            .thenWaitUntil(()->h.assertTrue(SecondPerson.agent(l,b)==null,"and the agent is not left behind"))
            .thenExecute(()->{
                at(f,p,1.5,UP,-25.5);
                h.assertTrue(click(f,p,SecondPersonSite.HOOK,Direction.EAST)&&SecondPerson.held(p).equals("coat"),"their coat off its hook");
                h.assertTrue(click(f,p,new BlockPos(1,UP-1,-27),Direction.UP)&&own(f,p).getBoolean("LaidGo"),"laid by the door, as if for going");
                street(f,p);})
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPersonSpring.S_POST&&!own(f,p).getString("Decision").isEmpty(),"they decide, and the post comes"))
            .thenWaitUntil(()->h.assertTrue(SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSpring.APPLICATION_AT)),SecondPersonBlock.Kind.APPLICATION),"a form on the mat"))
            .thenExecute(()->{
                at(f,p,1.5,UP,-25.5);
                h.assertTrue(click(f,p,SecondPersonSpring.APPLICATION_AT,Direction.UP)&&Talk.lines(p).stream().anyMatch(x->x.contains("flat_spring")),"the form has the reader's name on it: "+Talk.lines(p));
                say(h,p,SecondPersonSpringTexts.READ_IT);
                h.assertTrue(own(f,p).getString("Application").equals("read")&&Talk.lines(p).stream().anyMatch(x->x.contains("I was here first")),"read all of it: "+Talk.lines(p));
                Talk.close(p);street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).contains("AskedAt"),"they read it too, and ask the room"))
            .thenExecute(()->{h.assertTrue(SecondPersonStory.latest(own(f,p)).contains("asked the room a question"),"the book has the question");
                h.assertTrue(!SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSpring.APPLICATION_AT)),SecondPersonBlock.Kind.APPLICATION),"the form picked up off the mat");
                h.assertTrue(click(f,p,new BlockPos(-3,UP+1,-12),Direction.SOUTH)&&Talk.waiting(p,"sp_word"),"breath on the street window");say(h,p,"YES");
                h.assertTrue(own(f,p).getString("SpringAnswer").equals("YES"),"the answer kept: "+own(f,p));})
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPersonSpring.SPRING_DONE,"the form in the drawer, and the spring at its end"))
            .thenExecute(()->{String text=account(f,p);
                for(var want:List.of("the tenancy came up","the rent stayed where it was","you laid their things by the door","with your name on it","asked the room if it was you","You wrote YES on the glass, and they kept the word"))
                    h.assertTrue(text.contains(want),"the account says \""+want+"\": "+text);
                var own=own(f,p);own.putString("KeptDecision",own.getString("Decision"));save(f,p,own);
                cupboard(f,p);})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.MORNING,"the rest of the spring goes by in the cupboard"))
            .thenExecute(()->{var own=own(f,p);
                h.assertTrue(own.getString("Decision").equals(own.getString("KeptDecision")),"the last morning keeps what they decided in the spring");
                h.assertTrue(!SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSpring.APPLICATION_AT)),SecondPersonBlock.Kind.APPLICATION),"no form left on the mat");
                h.assertTrue(SecondPersonStory.latest(own).contains("the letter about the tenancy was answered"),"the morning's page: "+SecondPersonStory.latest(own));})
            .thenSucceed();
    }
    @AfterBatch(batch="second_spring") public static void springDone(ServerLevel l){close();}

    /**
     * 0.4.634, E2: their name on the post. The form on the mat is signed; for the last day of the year the reader lives there: Sam knocks
     * and talks to them as the tenant, the phone rings for them, and at dark, in the black street window, the resident stands behind them
     * where the reader used to stand. The door is on the latch and the reader goes out by it; the account is the last day's, not the
     * last morning's.
     */
    @GameTest(template="empty",batch="second_signed",timeoutTicks=9000)
    public static void theirNameOnThePost(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_signed",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{f.source(p,P);street(f,p);
                autumnOver(f,p,List.of("porch"),own->{thing(own,"coat","homed",null);own.putBoolean("SamStayed",true);});})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.WINTER,"into the winter"))
            .thenExecute(()->{var own=own(f,p);own.putInt("Season",SecondPerson.SPRING);own.putInt("Step",SecondPersonSpring.S_POST);own.putInt("T",0);own.putInt("Sub",0);own.putInt("SubT",0);save(f,p,own);
                SecondPersonSite.season(l,b,4);street(f,p);})
            .thenWaitUntil(()->h.assertTrue(SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSpring.APPLICATION_AT)),SecondPersonBlock.Kind.APPLICATION),"the form on the mat"))
            .thenExecute(()->{at(f,p,1.5,UP,-25.5);
                h.assertTrue(click(f,p,SecondPersonSpring.APPLICATION_AT,Direction.UP),"the form, in the reader's hand");say(h,p,SecondPersonSpringTexts.SIGN_IT);
                h.assertTrue(own(f,p).getString("Application").equals("signed")&&step(f,p)==SecondPersonSigned.S_SIGNED,"signed: "+own(f,p));
                h.assertTrue(SecondPersonBlock.stage(l.getBlockState(b.offset(SecondPersonSpring.APPLICATION_AT)))==2,"the signature on the line");
                Talk.close(p);street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Sub")==1&&SecondPerson.sam(l,b)!=null,"somebody at the door: Sam"))
            .thenExecute(()->at(f,p,1.5,UP,-26.5))
            .thenWaitUntil(()->h.assertTrue(Talk.waiting(p,SecondPersonSigned.TALK_SAM),"Sam talks to the reader as the tenant: "+Talk.lines(p)))
            .thenExecute(()->{h.assertTrue(Talk.lines(p).stream().anyMatch(x->x.contains("There you are.")),"greeted: "+Talk.lines(p));say(h,p,SecondPersonSpringTexts.COME_IN);
                h.assertTrue(own(f,p).getString("SamDoor").equals("in"),"asked in");Talk.close(p);street(f,p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Ringing")==4,"the phone rings for the reader"))
            .thenExecute(()->{h.assertTrue(click(f,p,SecondPersonSite.PHONE,Direction.UP)&&Talk.waiting(p,"sp_call_4"),"lifted");say(h,p,"Listen.");
                h.assertTrue(own(f,p).getString("Call4").equals("lifted"),"listened");Talk.close(p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Sub")==3,"dark"))
            .thenExecute(()->{p.stopRiding();p.teleportTo(l,b.getX()-3.5,b.getY()+UP,b.getZ()-14.2,0,0);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("SawBehind")&&SecondPerson.resident(l,b)!=null,"in the black glass, behind them, the resident"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Sub")==4,"the door on the latch"))
            .thenExecute(()->street(f,p))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Ready")&&own(f,p).getString("Outcome").equals("their_name_on_the_post"),"out by the door; the book on the bench"))
            .thenExecute(()->{String text=account(f,p);
                for(var want:List.of("You signed the form","Sam came to the door","you asked them in","The phone rang for you, and you listened","where you used to stand","The year was ordinary, and you had it"))
                    h.assertTrue(text.contains(want),"the account says \""+want+"\": "+text);
                h.assertFalse(text.contains("On the last morning"),"the last day takes the last morning's place: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="second_signed") public static void signedDone(ServerLevel l){close();}

    /**
     * 0.4.635, E3: nobody home. An autumn of hidden things, cold and the tap at night, then the cupboard: the season does not go by. In
     * the night they sit up dressed, take one bag and go; the flat is the reader's, the account says why from the record, and there is no
     * winter and no last morning in it.
     */
    @GameTest(template="empty",batch="second_gone",timeoutTicks=6000)
    public static void nobodyHome(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_gone",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{f.source(p,P);street(f,p);
                autumnOver(f,p,List.of("porch"),own->{
                    var hid=new ListTag();for(var th:List.of("photo","drawing","coat"))hid.add(StringTag.valueOf(th));own.put("Hidden",hid);
                    var cold=new ListTag();for(var who:List.of("them","Sam"))cold.add(StringTag.valueOf(who));own.put("Cold",cold);
                    own.putString("Night","tap");own.putString("Evening","seen");own.putString("RoutineWhere","on the bathroom shelf, where you put it");
                    h.assertTrue(SecondPersonGone.frightened(own),"a year far more frightening than gentle");});})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPersonGone.GONE,"frightened out: the autumn does not go by"))
            .thenExecute(()->h.assertTrue(own(f,p).getInt("LeftIn")==SecondPerson.AUTUMN,"left in the autumn"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Sub")>=1&&SecondPerson.resident(l,b)!=null,"up, dressed, with one bag"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Sub")>=4,"out of the door, down the stairs"))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Ready")&&own(f,p).getString("Outcome").equals("nobody_home"),"nobody home; the book on the bench"))
            .thenExecute(()->{h.assertTrue(SecondPerson.resident(l,b)==null,"nobody left in the flat");
                h.assertTrue(SecondPersonStory.latest(own(f,p)).contains(SecondPersonGone.LAST_PAGE),"the book's last page: "+SecondPersonStory.latest(own(f,p)));
                String text=account(f,p);
                for(var want:List.of("They left in the night, in the autumn, with one bag","You hid their things","You made the air go cold round them twice","On the first night you ran the tap","Nobody could see you"))
                    h.assertTrue(text.contains(want),"the account says \""+want+"\": "+text);
                h.assertFalse(text.contains("On the last morning"),"no last morning: "+text);
                h.assertFalse(text.contains("In the winter Iris died"),"no winter: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="second_gone") public static void goneDone(ServerLevel l){close();}

    /**
     * 0.4.636, E4: two cups. A year kept gently, and the reader met once in the glass: at the end of the spring the cupboard opens onto
     * the last evening. Two cups on the kitchen table; they talk across it, ask one thing, and the answer is a word on the glass. They say
     * "You can stay." or "You can come.", drink their cup, and the account ends "They will get older here. You won't."
     */
    @GameTest(template="empty",batch="second_cups",timeoutTicks=7000)
    public static void twoCups(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_cups",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{f.source(p,P);street(f,p);
                autumnOver(f,p,List.of("porch"),own->{thing(own,"coat","homed",null);own.putBoolean("SamStayed",true);});})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPerson.WINTER,"into the winter"))
            .thenExecute(()->{var own=own(f,p);
                var put=new ListTag();for(var th:List.of("photo","drawing","coat","routine"))put.add(StringTag.valueOf(th));own.put("PutAway",put);
                own.putBoolean("RoutineSet",true);own.putString("Night","shut");own.putString("Evening","helped");own.putString("Seen","still");own.putString("Decision","stay");
                h.assertTrue(SecondPersonCups.known(own),"a year kept gently, and met once in the glass");
                own.putInt("Season",SecondPerson.SPRING);save(f,p,own);SecondPersonSite.season(l,b,4);springOver(f,p);})
            .thenWaitUntil(()->h.assertTrue(season(f,p)==SecondPersonCups.TWO_CUPS,"the cupboard opens onto the last evening, not the last morning"))
            .thenWaitUntil(()->h.assertTrue(SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonCups.CUPS_AT)),SecondPersonBlock.Kind.CUPS),"two cups on the kitchen table"))
            .thenExecute(()->at(f,p,-4.5,UP,-21.5))
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("Sub")==2&&own(f,p).getInt("T")-own(f,p).getInt("SubT")>45,"they talked, and asked one thing"))
            .thenExecute(()->{h.assertTrue(click(f,p,new BlockPos(-3,UP+1,-12),Direction.SOUTH)&&Talk.waiting(p,"sp_word"),"breath on the street window");say(h,p,"YES");
                h.assertTrue(own(f,p).getString("CupsAnswer").equals("YES"),"the answer read at once: "+own(f,p));Talk.close(p);})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getBoolean("Ready")&&own(f,p).getString("Outcome").equals("two_cups"),"the last evening done; the book on the bench"))
            .thenExecute(()->{h.assertTrue(SecondPersonBlock.stage(l.getBlockState(b.offset(SecondPersonCups.CUPS_AT)))==1,"their cup drunk, the reader's still full");
                String text=account(f,p);
                for(var want:List.of("they set out two cups","You wrote YES on the glass","They said, \"You can stay.\"","They will get older here. You won't."))
                    h.assertTrue(text.contains(want),"the account says \""+want+"\": "+text);
                h.assertFalse(text.contains("On the last morning"),"no last morning: "+text);})
            .thenSucceed();
    }
    @AfterBatch(batch="second_cups") public static void cupsDone(ServerLevel l){close();}

    /** 0.4.637: a finished year come back, for each ending as it was left. */
    private static void after(LiteraryTests.Fixture f,ServerPlayer p,String outcome,String decision){
        var own=own(f,p);own.putBoolean("Completed",true);own.putBoolean("Ready",true);own.putString("Outcome",outcome);own.putString("Decision",decision);
        own.putString("RoutineWhere","on the bathroom shelf, where you put it");own.putInt("Visit",own.getInt("Visit")+1);
        SecondPerson.arrive(p,f.base(P),own);save(f,p,own);
    }
    private static boolean near(@javax.annotation.Nullable LiteraryActor a,BlockPos b,double x,double z){return a!=null&&a.position().distanceToSqr(b.getX()+x,a.getY(),b.getZ()+z)<2.25;}
    /**
     * 0.4.637, the aftermath. The flat comes back to a reader who finished the year, as their ending left it, with nothing to do in it:
     * the card with a forwarding address on the mat, or their life going on in the armchair; the resident at the window at night; the
     * empty flat's dust; the second cup out on the kitchen table. Nothing answers a hand but the empty flat's things, and it comes back
     * only as often as the House allows.
     */
    @GameTest(template="empty",batch="second_after",timeoutTicks=4000)
    public static void theFlatAfter(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_after",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{street(f,p);after(f,p,SecondPersonAfter.LATCH,"go");
                h.assertTrue(SecondPersonAfter.after(own(f,p))&&Aftermath.PLACES.contains(P),"a finished year comes back as its aftermath, and the flat is among the aftermath rooms");})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("AfterT")>=45&&SecondPerson.resident(l,b)==null
                &&SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSite.MAT)),SecondPersonBlock.Kind.APPLICATION)&&SecondPersonBlock.stage(l.getBlockState(b.offset(SecondPersonSite.MAT)))==SecondPersonAfter.FORWARDING,
                "gone: an empty flat, and the forwarding card on the mat: "+own(f,p)))
            .thenExecute(()->{h.assertTrue(Aftermath.returns(f.data(),p.getUUID(),P)==1,"counted once as a return");
                h.assertFalse(SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonSite.BENCH_BOOK)),SecondPersonBlock.Kind.BOOK),"no book on the bench this time");
                after(f,p,SecondPersonAfter.LATCH,"stay");})
            .thenWaitUntil(()->h.assertTrue(near(SecondPerson.resident(l,b),b,SecondPersonSite.ARMCHAIR.getX()+.5,SecondPersonSite.ARMCHAIR.getZ()+.5),"stayed: their life going on, in the armchair"))
            .thenExecute(()->after(f,p,SecondPersonAfter.POST,"stay"))
            .thenWaitUntil(()->h.assertTrue(near(SecondPerson.resident(l,b),b,-3.5,-14.5)&&SecondPersonAfter.hour(own(f,p))==18000,"the resident at the window, at night"))
            .thenExecute(()->{h.assertTrue(click(f,p,SecondPersonSite.PHONE,Direction.UP)&&!Talk.waiting(p,"sp_call_1")&&SecondPersonAfter.inert(own(f,p)),"nothing in the flat answers a hand");
                after(f,p,SecondPersonAfter.NOBODY,"go");})
            .thenWaitUntil(()->h.assertTrue(own(f,p).getInt("AfterT")>=45&&SecondPerson.resident(l,b)==null&&!SecondPersonAfter.inert(own(f,p)),"nobody home: the flat empty, and its things still to be moved"))
            .thenExecute(()->after(f,p,SecondPersonAfter.CUPS,"stay"))
            .thenWaitUntil(()->h.assertTrue(SecondPersonBlock.is(l.getBlockState(b.offset(SecondPersonCups.CUPS_AT)),SecondPersonBlock.Kind.CUPS)&&SecondPersonBlock.stage(l.getBlockState(b.offset(SecondPersonCups.CUPS_AT)))==0
                &&near(SecondPerson.resident(l,b),b,SecondPersonSite.RESIDENT_CHAIR.getX()+.5,SecondPersonSite.RESIDENT_CHAIR.getZ()+.5),"the second cup out, and them at the table"))
            .thenExecute(()->{h.assertTrue(Aftermath.returns(f.data(),p.getUUID(),P)>=Aftermath.limit(P)&&!Aftermath.canDeal(f.data(),p.getUUID(),P),"it comes back only as often as the House allows: "+Aftermath.returns(f.data(),p.getUUID(),P));
                h.assertTrue(SecondPersonAfter.line(own(f,p)).equals(SecondPersonAfter.SECOND_CUP),"the line for two cups");})
            .thenSucceed();
    }
    @AfterBatch(batch="second_after") public static void afterDone(ServerLevel l){close();}

    private static CompoundTag homed(String home){var own=new CompoundTag();var snap=new CompoundTag();snap.putString("Home",home);own.put("Snap",snap);return own;}
    /**
     * 0.4.638: the House's read of home gives the phone its own voices. Someone from before (DANGER), whom they do not answer; a wrong
     * number and then a sister, Lou (RUNAWAY); a kind parent (HAVEN); an old friend when the House has no home for the reader. Every
     * call, silence and caught line differs between them, and the winter ticket comes from someone of their own.
     */
    @GameTest(template="empty",batch="second_homes",timeoutTicks=200)
    public static void theOtherHomes(GameTestHelper h){
        var homes=List.of("HAVEN","DANGER","RUNAWAY","");
        var seen=new java.util.HashMap<String,String>();
        for(var home:homes){var own=homed(home);
            h.assertTrue(SecondPersonStory.home(own).equals(home),"the home read back: "+home);
            for(int call=1;call<=4;call++){
                for(var line:SecondPersonStory.call(own,call)){var was=seen.put(line,home);h.assertTrue(was==null||was.equals(home),"\""+line+"\" belongs to one home only");}
                for(var line:SecondPersonStory.listened(own,call)){var was=seen.put(line,home);h.assertTrue(was==null||was.equals(home),"\""+line+"\" belongs to one home only");}
            }
            for(int call=1;call<=2;call++){var line=SecondPersonStory.caught(own,call);var was=seen.put(line,home);h.assertTrue(was==null||was.equals(home),"\""+line+"\" belongs to one home only");}
        }
        h.assertTrue(SecondPersonStory.caller(homed("DANGER"),1).equals("Someone from before, on the line")&&SecondPersonStory.call(homed("DANGER"),1).contains("I know you're in."),"someone from before, who knows they are there");
        h.assertTrue(SecondPersonStory.caller(homed("RUNAWAY"),1).equals("A wrong number, on the line")&&SecondPersonStory.caller(homed("RUNAWAY"),2).equals("Lou, on the line"),"a wrong number, and then Lou");
        h.assertTrue(SecondPersonStory.ticketDesc(homed("DANGER")).contains("Nobody needs to know where")&&SecondPersonStory.ticketDesc(homed("RUNAWAY")).contains("Lou"),"the winter ticket from a friend who knows, or from Lou");
        h.assertTrue(SecondPersonStory.home(homed("SOMETHING_ELSE")).isEmpty(),"anything else is no home at all");
        h.succeed();
    }
    @AfterBatch(batch="second_homes") public static void homesDone(ServerLevel l){close();}

    /**
     * 0.4.639: why they are here. A tentative read of an older reader puts only the chair on the landing; a settled one also has them say
     * why it is there, and the account keeps it. Each age has its own line; no read leaves the summer as it was.
     */
    @GameTest(template="empty",batch="second_age",timeoutTicks=3000)
    public static void whyTheyAreHere(GameTestHelper h){
        var f=fixture(h);var p=f.player("flat_age",P);var b=f.base(P);var l=f.level(P);
        resident(h,f,p)
            .thenExecute(()->{var own=own(f,p);h.assertTrue(own.contains("Snap")&&own.getInt("Step")==SecondPerson.ARRIVE,"arrived, the read taken: "+own);var snap=own.getCompound("Snap");snap.putString("Age","ANALOG");snap.putBoolean("AgeSettled",true);own.put("Snap",snap);save(f,p,own);
                for(var age:List.of("DIGITAL","TRANSITIONAL","ANALOG")){var o=new CompoundTag();var sn=new CompoundTag();sn.putString("Age",age);sn.putBoolean("AgeSettled",true);o.put("Snap",sn);
                    h.assertFalse(SecondPersonStory.ageLine(o).isEmpty()||SecondPersonStory.ageAccount(o).isEmpty(),"a line and an account sentence for "+age);
                    sn.putBoolean("AgeSettled",false);h.assertTrue(SecondPersonStory.ageAccount(o).isEmpty(),"a tentative "+age+" changes no words");}
                var none=new CompoundTag();none.put("Snap",new CompoundTag());h.assertTrue(SecondPersonStory.age(none).isEmpty()&&SecondPersonStory.ageLine(none).isEmpty(),"no read, the summer as it was");
                f.source(p,P);at(f,p,-2.5,UP,-17.5);})
            .thenWaitUntil(()->h.assertTrue(step(f,p)==SecondPerson.UNPACK&&own(f,p).getInt("Sub")>=1,"somebody comes up the stairs with the last box: "+own(f,p)))
            .thenExecute(()->{var chair=l.getBlockState(b.offset(SecondPersonSite.LANDING_CHAIR));
                h.assertTrue(chair.is(io.github.knaitoe.theoldesthouse.house.HouseBlocks.HOUSEHOLD_FURNITURE.get())&&chair.getValue(io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.KIND)==io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,"the chair that would not go through the door, on the landing: "+chair);
                h.assertTrue(account(f,p).contains("The chair stayed on the landing"),"the account keeps it: "+account(f,p));})
            .thenSucceed();
    }
    @AfterBatch(batch="second_age") public static void ageDone(ServerLevel l){close();}

    private static CompoundTag drawn(String to){var own=new CompoundTag();var snap=new CompoundTag();snap.putString("DrawnTo",to);own.put("Snap",snap);return own;}
    /**
     * 0.4.640: who comes for supper. A settled read of the reader as drawn to men brings Dev, to women Hannah, to no one Marcus (a friend
     * who needs somewhere to live, with his own evening, question and ending); both, or no settled read, keep Sam. Each has their own
     * stranger phase and the account names them.
     */
    @GameTest(template="empty",batch="second_guests",timeoutTicks=200)
    public static void whoComesForSupper(GameTestHelper h){
        var want=java.util.Map.of("MEN","Dev","WOMEN","Hannah","NO_ONE","Marcus","BOTH","Sam","","Sam");
        var phases=new java.util.HashSet<Integer>();
        for(var e:want.entrySet()){var own=drawn(e.getKey());
            h.assertTrue(SecondPersonStory.guest(own).equals(e.getValue()),e.getKey()+" brings "+e.getValue()+", not "+SecondPersonStory.guest(own));
            h.assertTrue(SecondPerson.guest(SecondPersonStory.guestPhase(own)),"their phase is a guest's");phases.add(SecondPersonStory.guestPhase(own));
            own.putString("Evening","helped");own.putBoolean("SamStayed",true);
            var b=new StringBuilder(SecondPersonStory.account(own));
            h.assertTrue(b.toString().contains(e.getValue()),"the account names "+e.getValue()+": "+b);
            if(!e.getValue().equals("Sam"))h.assertFalse(b.toString().contains("Sam"),"and nobody else: "+b);}
        h.assertTrue(phases.size()==4,"four guests, four faces: "+phases);
        h.assertTrue(SecondPersonStory.friend(drawn("NO_ONE"))&&!SecondPersonStory.friend(drawn("MEN")),"only Marcus has come about the room");
        var marcus=drawn("NO_ONE");marcus.putString("Evening","helped");marcus.putBoolean("SamStayed",true);var b=new StringBuilder(SecondPersonStory.account(marcus));
        h.assertTrue(b.toString().contains("Marcus moved in.")&&b.toString().contains("about the spare room"),"Marcus moved in: "+b);
        h.assertTrue(SecondPersonSpringTexts.decided("sam","Hannah").equals("I'm going to Hannah's.")&&SecondPersonSpringTexts.thinking("sam","Marcus").contains("The two of us"),"spring knows who");
        h.assertTrue(SecondPersonCups.talk(marcus).stream().anyMatch(t->t.contains("Marcus")),"the two cups know who");
        h.succeed();
    }
    @AfterBatch(batch="second_guests") public static void guestsDone(ServerLevel l){close();}
}
