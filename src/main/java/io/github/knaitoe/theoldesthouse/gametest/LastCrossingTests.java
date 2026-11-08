package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.351, The Last Crossing, first slice, with real readers. The station built whole and not dealt: the farm road into the flood, the
 * levee and its power line, the office and porch, the pump room above and below, the sluice pit, the ramp and the ferry low against its
 * sill, the spillway and the cable house, the far bank's lamp, the water held where it belongs. A crew of four signs the roll; Vera calls
 * each name and Ellis after them, shows the plate and puts the kettle on. Day One's jobs: the intake primed by two hands (the wrench, the
 * bleed screw, the gauge called down the pipe, a wrong valve and the right one, the brake held open), the gauge's dip, the grate's broken
 * corner and the tap back; the cable house's roller, hatch and jammed fairlead, the jam reported, the dry ledge; the line brought in and
 * returned, the wheel held and the pin seated at the second knock. The plate touched with others watching, carried, set in another
 * bracket and on a floor, and put down when it is let out of the hand. Round the route once, dusk, and the station set back. Then six:
 * the fifth and sixth jobs (the lamp carried up from the intake, the spillway walked with the lookout, the bell, the line staged), a name
 * not answered, the intake handed over, one reader on both sides of it, the lamp passed hand to hand, a reader who leaves and comes back
 * to their place on the roll, one who comes late and finds the sheet full, and the current that puts a swimmer back on the landing.
 *
 * <p>0.4.352, Night One. The hand from above: the intake primed, dusk, the surge, a hand up through the grate's broken corner taking hold
 * of the intake worker (the first slot, nobody else), seen from the landing; a hand reached down through the rail and taken; the carrier
 * pulled up streaming and seen, their private beat; said aloud to Vera and heard, and a palm on the plate showing the second print to
 * another; the cable house barred; the things from the water coming up; the loose line thrown round the cleat at the first rise; dawn,
 * the things gone back into the water, and the station set back. Fighting free: the intake unprimed and the surge anyway, three blows
 * that hold and the fourth that frees, the ferry swung off its sill when nobody makes the line fast, a clean reader cut and still clean.
 * The lower floor lost: a blow that isn't enough, the intake filling with nobody on the brake, everyone below come up the stair, the
 * things up through the landing while it's left open, and the isolation valve shut.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class LastCrossingTests {
    private LastCrossingTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.LAST_CROSSING;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);LastCrossing.clearAll();return active;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-25,b.getY()-13,b.getZ()-113,b.getX()+61,b.getY()+14,b.getZ()+3);}
    private static boolean use(LiteraryTests.Fixture f,ServerPlayer p,BlockPos rel){var at=f.base(P).offset(rel);return LastCrossing.use(p,at,f.level(P).getBlockState(at));}
    private static boolean is(ServerLevel l,BlockPos b,BlockPos rel,Kind k,int stage){var s=l.getBlockState(b.offset(rel));return CrossingBlock.is(s,k)&&s.getValue(CrossingBlock.STAGE)==stage;}
    private static boolean carries(ServerPlayer p,String what){for(var st:p.getInventory().items){var c=st.get(DataComponents.CUSTOM_DATA);if(c!=null&&c.copyTag().getString("Crossing").equals(what))return true;}return false;}
    private static void answer(ServerPlayer... crew){for(var p:crew)if(Talk.replies(p).contains("Here."))Talk.choose(p,"Here");}
    private static void close(){if(active!=null){active.close();active=null;}LastCrossing.clearAll();}
    @AfterBatch(batch="last_crossing") public static void done(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_crew") public static void crewDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_night") public static void nightDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_fight") public static void fightDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_flood") public static void floodDone(ServerLevel l){close();}
    private static Vec3 rel(LiteraryTests.Fixture f,ServerPlayer p){var b=f.base(P);return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    private static void select(ServerPlayer p,String what){for(int i=0;i<9;i++){var c=p.getInventory().getItem(i).get(DataComponents.CUSTOM_DATA);if(c!=null&&c.copyTag().getString("Crossing").equals(what)){p.getInventory().selected=i;return;}}}
    /** A crew signed, called and answered, the day running. */
    private static GameTestSequence crew(LiteraryTests.Fixture f,GameTestHelper h,ServerPlayer[] c,String prefix){
        var b=f.base(P);var l=f.level(P);
        return h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the station is resident"))
            .thenExecute(()->{for(int i=0;i<c.length;i++)c[i]=f.player(prefix+i,P);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.vera(l,b)!=null,"Vera on her porch"))
            .thenExecute(()->{for(int i=0;i<c.length;i++){f.at(c[i],P,12.5+i,0,-35.5);sign(f,h,c[i]);}})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.ROLL,"the roll"))
            .thenWaitUntil(()->{answer(c);h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAY,"the roll taken");});
    }
    private static void sign(LiteraryTests.Fixture f,GameTestHelper h,ServerPlayer p){h.assertTrue(use(f,p,LastCrossingSite.CLIPBOARD),"the clipboard opens");h.assertTrue(Talk.choose(p,"Sign"),"the roll offered to "+p.getGameProfile().getName());}

    @GameTest(template="empty",batch="last_crossing",timeoutTicks=9000)
    public static void aCrewSignsTheRoll(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the station is resident"))
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(15,0,-40),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==17,"a literary station outside, one visit, built whole, on its own clock");
                h.assertTrue(WitnessAccount.Story.of(P.id())==null&&WitnessAccount.Story.values().length==WitnessPool.SOURCES,"no Witness source until the crossing can be finished");
                var probe=UUID.randomUUID();h.assertTrue(!LastCrossing.dealable(f.data(),probe),"not dealt yet");
                // The road, the levee and its line, the station's places, the ferry, the far bank.
                h.assertTrue(l.getBlockState(b.offset(0,-1,-5)).is(Blocks.DIRT_PATH)&&l.getBlockState(b.offset(0,-1,-20)).is(Blocks.WATER)&&l.getBlockState(b.offset(0,-2,-20)).is(Blocks.GRAVEL),"the farm road, and the water over it");
                h.assertTrue(l.getBlockState(b.offset(LastCrossingSite.LEVEE_E,5,-16)).is(Blocks.STRIPPED_DARK_OAK_LOG)&&l.getBlockState(b.offset(LastCrossingSite.LEVEE_E,5,-18)).is(Blocks.CHAIN)&&l.getBlockState(b.offset(5,-1,-25)).is(Blocks.COARSE_DIRT),"the levee and the power line along it");
                h.assertTrue(CrossingBlock.is(l.getBlockState(b.offset(LiteraryRooms.source(P))),Kind.SLATE),"the work slate, the source");
                var slate=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var words=String.join(" ",slate.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(words.contains("CROSSING")&&words.contains("E here")&&words.contains("second knock"),"the slate: "+words);
                h.assertTrue(is(l,b,LastCrossingSite.CLIPBOARD,Kind.CLIPBOARD,0)&&is(l,b,LastCrossingSite.PLATE_HOME,Kind.PLATE,0)&&is(l,b,LastCrossingSite.LAMP_HOOK,Kind.HOOK,1),"the clipboard, the plate in its bracket, the lamp on its hook");
                h.assertTrue(is(l,b,LastCrossingSite.GAUGE,Kind.GAUGE,0)&&is(l,b,LastCrossingSite.BLEED,Kind.BLEED,0)&&is(l,b,LastCrossingSite.PUMP,Kind.PUMP,0)&&is(l,b,LastCrossingSite.GRATE,Kind.GRATE,1),"the gauge, the bleed screw, the pump and the broken grate");
                for(int i=0;i<3;i++)h.assertTrue(is(l,b,LastCrossingSite.VALVES.get(i),Kind.VALVE,i),"valve "+(i+1));
                for(var pipe:List.of(LastCrossingSite.PIPE_A_UP,LastCrossingSite.PIPE_A_DOWN,LastCrossingSite.PIPE_B_UP,LastCrossingSite.PIPE_B_DOWN))h.assertTrue(is(l,b,pipe,Kind.PIPE,0),"a speaking pipe's mouth at "+pipe);
                h.assertTrue(is(l,b,LastCrossingSite.WHEEL,Kind.WHEEL,0)&&is(l,b,LastCrossingSite.PIN,Kind.PIN,0)&&is(l,b,LastCrossingSite.RAMP_COIL,Kind.COIL,0),"the wheel, the pin and the loose line");
                h.assertTrue(is(l,b,LastCrossingSite.LOOSE_ROLLER,Kind.ROLLER,1)&&is(l,b,LastCrossingSite.FAIRLEAD,Kind.FAIRLEAD,0)&&is(l,b,LastCrossingSite.HATCH,Kind.HATCH,0)&&is(l,b,LastCrossingSite.DRUM,Kind.DRUM,0),"the cable house: the loose roller, the jam, the hatch, the drum");
                h.assertTrue(l.getBlockState(b.offset(33,-1,-70)).is(Blocks.DARK_OAK_PLANKS)&&is(l,b,new BlockPos(32,2,-69),Kind.NAMEPLATE,0)&&is(l,b,new BlockPos(33,2,-69),Kind.NAMEPLATE,1),"the ferry, its nameplate across the wheelhouse");
                h.assertTrue(l.getBlockState(b.offset(LastCrossingSite.FAR_LAMP)).is(Blocks.LANTERN)&&l.getBlockState(b.offset(LastCrossingSite.CABLE_X,0,-80)).is(Blocks.CHAIN),"the lamp on the far bank, the cable out to it");
                // The water where it belongs, and nowhere it doesn't.
                for(var wet:List.of(new BlockPos(20,-5,-80),new BlockPos(-10,-2,-20),new BlockPos(39,-2,-55),new BlockPos(32,-2,-64),new BlockPos(46,-4,-61),new BlockPos(16,-8,-63)))h.assertTrue(l.getBlockState(b.offset(wet)).is(Blocks.WATER),"water at "+wet);
                for(var dry:List.of(new BlockPos(18,-6,-56),new BlockPos(25,-6,-60),new BlockPos(34,-6,-60),new BlockPos(32,0,-61),new BlockPos(15,0,-42),new BlockPos(45,0,-58)))h.assertTrue(l.getBlockState(b.offset(dry)).isAir(),"dry at "+dry);
                for(int i=0;i<4;i++)c[i]=f.player("lc_crew"+i,P);
            })
            .thenWaitUntil(()->h.assertTrue(LastCrossing.vera(l,b)!=null&&LastCrossing.phase(b)==LastCrossing.GATHERING,"Vera on her porch"))
            .thenExecute(()->{h.assertTrue(!f.own(c[0],P).getBoolean("Signed"),"nothing owed before signing");
                for(int i=0;i<4;i++)f.at(c[i],P,12.5+i,0,-35.5);
                for(int i=0;i<3;i++)sign(f,h,c[i]);
                h.assertTrue(LastCrossing.phase(b)==LastCrossing.GATHERING&&is(l,b,LastCrossingSite.CLIPBOARD,Kind.CLIPBOARD,1),"the roll waits for the last of the crew");
                sign(f,h,c[3]);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.ROLL,"Vera takes the roll"))
            .thenWaitUntil(()->{answer(c);h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAY,"each name called and answered, then Ellis");})
            .thenExecute(()->{h.assertTrue(LastCrossing.crew(b).equals(List.of("lc_crew0","lc_crew1","lc_crew2","lc_crew3")),"the crew in signing order: "+LastCrossing.crew(b));
                for(int i=0;i<4;i++){var own=f.own(c[i],P);h.assertTrue(own.getBoolean("Signed")&&own.getInt("RollOrder")==i+1&&own.getBoolean("Answered")&&own.getBoolean("HeardEllis")&&own.getBoolean("Committed"),"on the roll and answered: "+own);}
                f.at(c[3],P,16.5,0,-41.5);f.look(c[3],b.offset(16,1,-45));})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"kettle"),"the plate shown, the kettle on"))
            .thenExecute(()->h.assertTrue(f.own(c[3],P).getBoolean("SawVeraPlate"),"Vera's palm on the plate, seen"))
            // The intake: the wrench, the bleed screw, the gauge called down the pipe, a wrong valve and the right one, the brake held.
            .thenExecute(()->{f.at(c[0],P,15.5,-6,-62.5);f.at(c[1],P,21.5,0,-62.5);
                h.assertTrue(use(f,c[0],LastCrossingSite.BLEED)&&!LastCrossing.flag(b,"bleed"),"too tight for fingers");
                h.assertTrue(use(f,c[0],LastCrossingSite.WRENCH_RUN)&&carries(c[0],"wrench")&&!LastCrossing.flag(b,"wrench"),"the wrench off the pipe");
                c[0].getInventory().selected=0;for(int i=0;i<9;i++)if(carries(c[0],"wrench")&&c[0].getInventory().getItem(i).has(DataComponents.CUSTOM_DATA)){c[0].getInventory().selected=i;break;}
                h.assertTrue(use(f,c[0],LastCrossingSite.GRATE)&&f.own(c[0],P).getBoolean("Tapped")&&f.own(c[0],P).getBoolean("BrokenCorner"),"the wrench on the grate, and something taps back");
                h.assertTrue(use(f,c[0],LastCrossingSite.BLEED)&&LastCrossing.flag(b,"bleed")&&LastCrossing.needle(b)>=0&&is(l,b,LastCrossingSite.GAUGE,Kind.GAUGE,LastCrossing.needle(b)),"the bleed screw open, the needle in its band");
                h.assertTrue(use(f,c[1],LastCrossingSite.GAUGE)&&Talk.choose(c[1],"Call it"),"the gauge read and called down the pipe");
                int n=LastCrossing.needle(b);
                h.assertTrue(use(f,c[0],LastCrossingSite.VALVES.get((n+1)%3))&&f.own(c[0],P).getBoolean("WrongValve"),"a wrong valve: the note sours");
                h.assertTrue(use(f,c[0],LastCrossingSite.VALVES.get(n))&&f.own(c[0],P).getBoolean("TurnedValve"),"the called valve turned");
                f.at(c[1],P,21.5,0,-63.5);h.assertTrue(use(f,c[1],LastCrossingSite.BRAKE)&&is(l,b,LastCrossingSite.BRAKE,Kind.BRAKE,1),"the brake held open");})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"primed"),"the pump takes the load"))
            .thenExecute(()->{h.assertTrue(is(l,b,LastCrossingSite.PUMP,Kind.PUMP,1)&&is(l,b,LastCrossingSite.GAUGE,Kind.GAUGE,3),"the pump running, the needle settled");
                var one=f.own(c[0],P);var two=f.own(c[1],P);
                h.assertTrue(one.getBoolean("Primed")&&one.getString("Jobs").contains("intake")&&one.getString("Job").equals("intake"),"the intake worker: "+one);
                h.assertTrue(two.getBoolean("Primed")&&two.getBoolean("GaugeRead")&&two.getString("Jobs").contains("gauge"),"the gauge reader: "+two);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[1],P).getBoolean("GaugeDip"),"the needle dips though the pump is steady"))
            // The cable house.
            .thenExecute(()->{f.at(c[2],P,46.5,0,-60.5);
                h.assertTrue(use(f,c[2],LastCrossingSite.LOOSE_ROLLER)&&is(l,b,LastCrossingSite.LOOSE_ROLLER,Kind.ROLLER,0)&&LastCrossing.flag(b,"roller"),"the roller seated");
                h.assertTrue(use(f,c[2],LastCrossingSite.HATCH)&&use(f,c[2],LastCrossingSite.FAIRLEAD),"the hatch tried, the fairlead looked at");
                var own=f.own(c[2],P);h.assertTrue(own.getBoolean("SeatedRoller")&&own.getBoolean("HatchTried")&&own.getBoolean("SawJam")&&own.getBoolean("Fairlead")&&own.getString("Job").equals("lookout"),"the lookout: "+own);
                f.at(c[2],P,LastCrossingSite.LEDGE_X+.5,.5,-60.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[2],P).getBoolean("DryLedge"),"the dry ledge"))
            .thenExecute(()->{LastCrossing.talk(c[2],LastCrossing.vera(l,b));h.assertTrue(Talk.choose(c[2],"The cable's jumped"),"the jam reported to Vera");
                h.assertTrue(LastCrossing.flag(b,"reported")&&f.own(c[2],P).getBoolean("Reported"),"reported");})
            // The landing: the line in, the wheel held below, two knocks up the pipe, the pin seated.
            .thenExecute(()->{f.at(c[3],P,32.5,0,-61.5);
                h.assertTrue(use(f,c[3],LastCrossingSite.RAMP_COIL)&&carries(c[3],"line")&&LastCrossing.flag(b,"line"),"the loose line hauled in");
                f.at(c[3],P,25.5,-7,-59.5);f.at(c[0],P,25.5,0,-60.8);
                h.assertTrue(use(f,c[0],LastCrossingSite.PIN)&&!LastCrossing.flag(b,"pin"),"the pin won't seat without the wheel held");
                h.assertTrue(use(f,c[3],LastCrossingSite.WHEEL)&&is(l,b,LastCrossingSite.WHEEL,Kind.WHEEL,1),"the wheel held shut");
                use(f,c[3],LastCrossingSite.PIPE_B_DOWN);use(f,c[3],LastCrossingSite.PIPE_B_DOWN);
                h.assertTrue(f.own(c[0],P).getBoolean("HeardKnocks"),"two knocks heard up the pipe");
                h.assertTrue(use(f,c[0],LastCrossingSite.PIN)&&LastCrossing.flag(b,"pin")&&LastCrossing.flag(b,"rehearsed")&&is(l,b,LastCrossingSite.WHEEL,Kind.WHEEL,2),"the pin seated at the second knock");
                var top=f.own(c[0],P);var below=f.own(c[3],P);
                h.assertTrue(top.getBoolean("Rehearsed")&&top.getString("RehearsedWith").equals("lc_crew3")&&below.getBoolean("Rehearsed")&&below.getString("RehearsedWith").equals("lc_crew0"),"both remember who was on the other end: "+top+" / "+below);
                h.assertTrue(below.getBoolean("CheckedWheel")&&top.getBoolean("CheckedPin")&&below.getString("Jobs").contains("landing"),"the wheel and the pin checked");})
            .thenWaitUntil(()->h.assertTrue(!LastCrossing.flag(b,"pin")&&is(l,b,LastCrossingSite.PIN,Kind.PIN,0),"the pin knocked back out for the night"))
            .thenExecute(()->{LastCrossing.talk(c[3],LastCrossing.vera(l,b));h.assertTrue(Talk.choose(c[3],"Hand her the line"),"the line handed to Vera");
                h.assertTrue(LastCrossing.flag(b,"returned")&&!carries(c[3],"line")&&is(l,b,LastCrossingSite.PORCH_COIL,Kind.COIL,1)&&f.own(c[3],P).getBoolean("LineReturned"),"the line coiled on the porch");})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"route"),"round the route once, Vera says"))
            // The plate: a palm on it with others watching; lifted, carried, set in the gauge room's bracket, stood on a floor, let go of.
            .thenExecute(()->{f.at(c[1],P,16.5,0,-43.5);f.at(c[2],P,14.5,0,-41.5);f.look(c[2],b.offset(LastCrossingSite.PLATE_HOME));
                h.assertTrue(use(f,c[1],LastCrossingSite.PLATE_HOME)&&Talk.choose(c[1],"Lay your palm"),"a palm on the plate");
                h.assertTrue(f.own(c[1],P).getInt("PlateReads")==1&&f.own(c[1],P).getBoolean("PlateClean")&&f.own(c[2],P).getString("Readings").contains("lc_crew1"),"a clean reading, seen by another: "+f.own(c[2],P));
                h.assertTrue(is(l,b,LastCrossingSite.PLATE_HOME,Kind.PLATE,1),"fog on the near side");})
            .thenWaitUntil(()->h.assertTrue(is(l,b,LastCrossingSite.PLATE_HOME,Kind.PLATE,0),"the fog gone"))
            .thenExecute(()->{h.assertTrue(use(f,c[1],LastCrossingSite.PLATE_HOME)&&Talk.choose(c[1],"Lift"),"the plate lifted out");
                h.assertTrue(carries(c[1],"plate")&&is(l,b,LastCrossingSite.PLATE_HOME,Kind.BRACKET,0)&&LastCrossing.flag(b,"plate")&&f.own(c[1],P).getBoolean("CarriedPlate"),"carried, the bracket empty");
                f.at(c[1],P,14.5,0,-61.5);h.assertTrue(use(f,c[1],LastCrossingSite.PUMP_BRACKET)&&is(l,b,LastCrossingSite.PUMP_BRACKET,Kind.PLATE,0)&&!carries(c[1],"plate"),"set in the gauge room's bracket");
                h.assertTrue(f.own(c[1],P).getString("SetPlate").contains("gauge room")&&LastCrossingSite.PUMP_BRACKET.equals(LastCrossing.plateAt(b)),"where it was set");
                h.assertTrue(use(f,c[1],LastCrossingSite.PUMP_BRACKET)&&Talk.choose(c[1],"Lift"),"lifted again");
                h.assertTrue(LastCrossing.setDownOn(c[1],b.offset(18,-1,-56),Direction.UP)&&CrossingBlock.is(l.getBlockState(b.offset(18,0,-56)),Kind.PLATE_SET),"stood on the gauge room's floor");
                h.assertTrue(use(f,c[1],new BlockPos(18,0,-56))&&Talk.choose(c[1],"Pick it up"),"picked up again");
                f.at(c[1],P,20.5,0,-57.5);c[1].getInventory().selected=(c[1].getInventory().selected+1)%9;})
            .thenWaitUntil(()->h.assertTrue(!LastCrossing.flag(b,"plate")&&LastCrossing.plateAt(b)!=null&&!carries(c[1],"plate")&&CrossingBlock.is(l.getBlockState(b.offset(LastCrossing.plateAt(b))),Kind.PLATE_SET),"let out of the hand, it is set down"))
            // Round the route once.
            .thenExecute(()->f.at(c[0],P,15.5,0,-42.5))
            .thenIdle(6).thenExecute(()->f.at(c[0],P,18.5,0,-56.5))
            .thenIdle(6).thenExecute(()->f.at(c[0],P,18.5,-6,-58.5))
            .thenIdle(6).thenExecute(()->f.at(c[0],P,45.5,0,-58.5))
            .thenIdle(6).thenExecute(()->f.at(c[0],P,32.5,0,-58.5))
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("RouteWalked"),"round all of it once: "+f.own(c[0],P).getInt("Route")))
            // Dusk, and the station held there.
            .thenExecute(()->{int before=LastCrossing.day(b);h.assertTrue(before>0,"the day running");LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.flag(b,"dusk"),"dusk"))
            .thenExecute(()->{for(var p:c)h.assertTrue(f.own(p,P).getBoolean("DayOne"),"Day One worked: "+p.getGameProfile().getName());
                LastCrossing.resetNow(l,b);
                h.assertTrue(is(l,b,LastCrossingSite.CLIPBOARD,Kind.CLIPBOARD,0)&&is(l,b,LastCrossingSite.PLATE_HOME,Kind.PLATE,0)&&!CrossingBlock.is(l.getBlockState(b.offset(18,0,-56)),Kind.PLATE_SET),"the roll blank, the plate home");
                h.assertTrue(is(l,b,LastCrossingSite.RAMP_COIL,Kind.COIL,0)&&l.getBlockState(b.offset(LastCrossingSite.PORCH_COIL)).isAir()&&is(l,b,LastCrossingSite.LOOSE_ROLLER,Kind.ROLLER,1)&&is(l,b,LastCrossingSite.BLEED,Kind.BLEED,0)&&is(l,b,LastCrossingSite.PUMP,Kind.PUMP,0)&&is(l,b,LastCrossingSite.GAUGE,Kind.GAUGE,0),"the station as it was in the morning");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_crew",timeoutTicks=9000)
    public static void sixHandsAndTheRest(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[7];
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the station is resident"))
            .thenExecute(()->{for(int i=0;i<6;i++)c[i]=f.player("lc_six"+i,P);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.vera(l,b)!=null,"Vera on her porch"))
            .thenExecute(()->{for(int i=0;i<6;i++){f.at(c[i],P,11.5+i,0,-35.5);sign(f,h,c[i]);}})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.ROLL,"the roll"))
            // Five answer; the sixth doesn't, and is called twice.
            .thenWaitUntil(()->{answer(c[0],c[1],c[2],c[3],c[4]);h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAY,"the roll taken");})
            .thenExecute(()->{h.assertTrue(f.own(c[5],P).getBoolean("Unanswered")&&!f.own(c[5],P).getBoolean("Answered")&&f.own(c[0],P).getBoolean("Answered"),"a name called twice and not answered");
                // A seventh comes late, partway, and finds the sheet full.
                c[6]=f.player("lc_late",P);})
            .thenExecute(()->{h.assertTrue(f.own(c[6],P).getBoolean("Partway"),"come late, partway");
                f.at(c[6],P,12.5,0,-36.5);sign(f,h,c[6]);h.assertTrue(!f.own(c[6],P).getBoolean("Signed")&&LastCrossing.crew(b).size()==6,"six names, and nowhere for a seventh");
                // One reader on both sides of the intake: the screw, the gauge above, a valve below; then hands it over.
                f.at(c[0],P,15.5,-6,-62.5);h.assertTrue(use(f,c[0],LastCrossingSite.WRENCH_RUN),"the wrench");
                for(int i=0;i<9;i++)if(c[0].getInventory().getItem(i).has(DataComponents.CUSTOM_DATA)){c[0].getInventory().selected=i;break;}
                h.assertTrue(use(f,c[0],LastCrossingSite.BLEED)&&LastCrossing.flag(b,"bleed"),"the bleed screw open");
                f.at(c[0],P,21.5,0,-62.5);h.assertTrue(use(f,c[0],LastCrossingSite.GAUGE)&&Talk.choose(c[0],"Call it"),"read the gauge too");
                f.at(c[0],P,17.5,-6,-62.5);h.assertTrue(use(f,c[0],LastCrossingSite.VALVES.get(LastCrossing.needle(b))),"and turned the valve");
                f.at(c[1],P,16.5,-6,-62.5);f.at(c[0],P,17.5,-6,-62.5);
                h.assertTrue(use(f,c[0],LastCrossingSite.BLEED)&&Talk.choose(c[0],"Take the intake, lc_six1"),"the intake handed over");
                h.assertTrue(f.own(c[0],P).getString("HandedIntake").equals("lc_six1")&&f.own(c[1],P).getString("TookIntake").equals("lc_six0")&&f.own(c[1],P).getString("Jobs").contains("intake"),"handed over, and taken");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("BothSides"),"both sides of the first puzzle"))
            // The fifth: the lamp from its hook down to the intake and up to the gauge room, then into another's hand.
            .thenExecute(()->{f.at(c[4],P,12.5,0,-41.5);h.assertTrue(use(f,c[4],LastCrossingSite.LAMP_HOOK)&&carries(c[4],"lamp")&&is(l,b,LastCrossingSite.LAMP_HOOK,Kind.HOOK,0),"the lamp off its hook");
                f.at(c[4],P,18.5,-6,-58.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[4],P).getBoolean("LampBelow"),"down at the intake with the lamp"))
            .thenExecute(()->f.at(c[4],P,18.5,0,-57.5))
            .thenWaitUntil(()->h.assertTrue(f.own(c[4],P).getBoolean("LampCarried")&&f.own(c[4],P).getString("Jobs").contains("lamp"),"carried up to the gauge room"))
            .thenExecute(()->{for(int i=0;i<9;i++)if(c[4].getInventory().getItem(i).has(DataComponents.CUSTOM_DATA)){c[4].getInventory().selected=i;break;}
                h.assertTrue(LastCrossing.handTo(c[4],c[5])&&carries(c[5],"lamp")&&!carries(c[4],"lamp"),"the lamp passed hand to hand");
                h.assertTrue(f.own(c[4],P).getString("Handed").contains("lc_six5")&&f.own(c[5],P).getString("GotFrom").contains("lc_six4"),"who handed what to whom");
                // The lookout and the sixth on the spillway; the bell.
                f.at(c[2],P,46.5,0,-60.5);h.assertTrue(use(f,c[2],LastCrossingSite.LOOSE_ROLLER),"the roller seated by the lookout");
                f.at(c[2],P,39.5,0,-49.5);f.at(c[5],P,40.2,0,-49.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[5],P).getBoolean("SpillwayWalked")&&f.own(c[5],P).getString("Jobs").contains("spillway"),"the spillway walked with the lookout"))
            .thenExecute(()->{LastCrossing.bell(c[5]);h.assertTrue(f.own(c[5],P).getBoolean("RangBell"),"the bell tried");
                // The line in, returned, and staged at the ramp by another.
                f.at(c[3],P,32.5,0,-61.5);h.assertTrue(use(f,c[3],LastCrossingSite.RAMP_COIL),"the line in");
                LastCrossing.talk(c[3],LastCrossing.vera(l,b));h.assertTrue(Talk.choose(c[3],"Hand her the line"),"returned");
                f.at(c[5],P,18.5,0,-36.5);h.assertTrue(use(f,c[5],LastCrossingSite.PORCH_COIL)&&carries(c[5],"line")&&l.getBlockState(b.offset(LastCrossingSite.PORCH_COIL)).isAir(),"taken off the porch to stage");
                for(int i=0;i<9;i++){var w=c[5].getInventory().getItem(i).get(DataComponents.CUSTOM_DATA);if(w!=null&&w.copyTag().getString("Crossing").equals("line")){c[5].getInventory().selected=i;break;}}
                f.at(c[5],P,33.5,0,-58.5);h.assertTrue(LastCrossing.stageLine(c[5],new BlockPos(33,-1,-58))&&LastCrossing.flag(b,"staged")&&is(l,b,LastCrossingSite.STAGED_COIL,Kind.COIL,2)&&f.own(c[5],P).getBoolean("StagedLine"),"the line staged at the head of the ramp");
                // One leaves the station and comes back to their own place on the roll.
                f.at(c[3],P,0.5,0,-3.5);LiteraryVignettes.onArrive(c[3],P);
                h.assertTrue(f.own(c[3],P).getBoolean("Rejoined")&&f.own(c[3],P).getBoolean("Signed")&&LastCrossing.crew(b).contains("lc_six3"),"back, and still on the roll");
                // Nobody swims the flood.
                f.at(c[4],P,20.5,-4,-80.5);})
            .thenWaitUntil(()->h.assertTrue(c[4].position().distanceToSqr(Vec3.atLowerCornerOf(b).add(LastCrossingSite.LANDING_SPOT))<1,"the current puts the swimmer back on the landing"))
            .thenExecute(()->LastCrossing.skip(b,LastCrossing.DAY_TICKS))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK,"dusk, the intake not yet primed"))
            .thenExecute(()->{h.assertTrue(f.own(c[0],P).getBoolean("DayOne")&&!f.own(c[6],P).getBoolean("DayOne"),"Day One for the crew, not for the one who came late");h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_night",timeoutTicks=9000)
    public static void nightOneTheHandFromAbove(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        crew(f,h,c,"lc_night")
            // The intake primed before dark: the wrench, the screw, the gauge called, the valve, the brake.
            .thenExecute(()->{f.at(c[0],P,15.5,-6,-62.5);h.assertTrue(use(f,c[0],LastCrossingSite.WRENCH_RUN),"the wrench");select(c[0],"wrench");
                h.assertTrue(use(f,c[0],LastCrossingSite.BLEED)&&LastCrossing.flag(b,"bleed"),"the screw open");
                f.at(c[1],P,21.5,0,-62.5);h.assertTrue(use(f,c[1],LastCrossingSite.GAUGE)&&Talk.choose(c[1],"Call it"),"the gauge called");
                h.assertTrue(use(f,c[0],LastCrossingSite.VALVES.get(LastCrossing.needle(b))),"the valve");
                f.at(c[1],P,21.5,0,-63.5);h.assertTrue(use(f,c[1],LastCrossingSite.BRAKE),"the brake held");})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"primed"),"primed"))
            .thenExecute(()->{f.at(c[0],P,16.5,-6,-62.5);f.at(c[1],P,17.5,0,-59.5);f.look(c[1],b.offset(17,-6,-63));f.at(c[2],P,32.5,0,-61.5);f.at(c[3],P,44.5,0,-56.5);
                LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.night(b)>0,"dusk, and the night begins"))
            .thenExecute(()->LastCrossing.skip(b,LastCrossingNight.SURGE_AFTER))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"surged")&&LastCrossing.flag(b,"grip"),"the surge, and a hand through the grate"))
            .thenExecute(()->{h.assertTrue(is(l,b,LastCrossingSite.GRATE,Kind.GRATE,2),"the hand up through the broken corner");
                var own=f.own(c[0],P);h.assertTrue(own.getBoolean("Infected")&&own.getInt("Slot")==1&&own.getBoolean("Gripped")&&own.getBoolean("HeardSurge"),"the intake worker takes the claw: "+own);
                h.assertTrue(c[0].getUUID().equals(LastCrossing.carrier(b)),"the first slot is theirs");
                for(int i=1;i<4;i++)h.assertTrue(!f.own(c[i],P).getBoolean("Infected"),"nobody else carries it");
                h.assertTrue(c[0].getHealth()<20&&c[0].getHealth()>=6,"hurt, and never below three hearts: "+c[0].getHealth());})
            .thenWaitUntil(()->h.assertTrue(f.own(c[1],P).getBoolean("SawClaw"),"seen from the landing"))
            .thenExecute(()->{h.assertTrue(LastCrossing.reachDown(c[1],new BlockPos(17,0,-61)),"a hand reached down through the rail");
                h.assertTrue(Talk.choose(c[0],"Take the hand"),"the hand taken");
                h.assertTrue(!LastCrossing.flag(b,"grip")&&LastCrossing.flag(b,"freed")&&is(l,b,LastCrossingSite.GRATE,Kind.GRATE,1),"it lets go");
                h.assertTrue(LastCrossingSite.inGaugeRoom(rel(f,c[0])),"pulled up into the gauge room: "+rel(f,c[0]));
                h.assertTrue(f.own(c[0],P).getString("TookHand").equals("lc_night1")&&f.own(c[0],P).getString("Freed").equals("hand")&&f.own(c[1],P).getString("PulledUp").equals("lc_night0"),"who pulled whom up");
                f.look(c[1],c[0].blockPosition().above());})
            .thenWaitUntil(()->h.assertTrue(f.own(c[1],P).getString("SawSoaked").contains("lc_night0"),"seen come up streaming"))
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("Beat"),"the carrier's private beat"))
            // Said aloud to Vera, and heard; a palm on the plate, and the second print seen.
            .thenExecute(()->{f.at(c[0],P,16.5,0,-43.5);f.at(c[2],P,14.5,0,-41.5);f.look(c[2],b.offset(LastCrossingSite.PLATE_HOME));
                LastCrossing.talk(c[0],LastCrossing.vera(l,b));h.assertTrue(Talk.choose(c[0],"It got a claw"),"said aloud to Vera");
                h.assertTrue(f.own(c[0],P).getBoolean("Told")&&f.own(c[2],P).getString("ToldBy").contains("lc_night0"),"and heard");
                for(int i=0;i<9;i++)if(c[0].getInventory().getItem(i).isEmpty()){c[0].getInventory().selected=i;break;}
                h.assertTrue(use(f,c[0],LastCrossingSite.PLATE_HOME)&&Talk.choose(c[0],"Lay your palm"),"a palm on the plate, hands empty");
                h.assertTrue(is(l,b,LastCrossingSite.PLATE_HOME,Kind.PLATE,3)&&f.own(c[0],P).getBoolean("Exposed")&&f.own(c[2],P).getString("SecondPrints").contains("lc_night0"),"the second print, seen by another");
                // The cable house barred.
                h.assertTrue(use(f,c[3],LastCrossingSite.BAR_REST)&&is(l,b,LastCrossingSite.BAR_ACROSS,Kind.BAR,1)&&l.getBlockState(b.offset(LastCrossingSite.BAR_REST)).isAir()&&LastCrossing.flag(b,"barred")&&f.own(c[3],P).getBoolean("BarredDoor"),"the bar across the cable house door");
                LastCrossing.skip(b,LastCrossingNight.FIRST_SPAWN);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.undertows(b)>=1&&!l.getEntitiesOfClass(Undertow.class,box(f)).isEmpty(),"something comes up out of the spillway"))
            // The first rise: the line still loose on the ramp, thrown round the cleat in time.
            .thenExecute(()->{f.at(c[2],P,32.5,0,-61.5);LastCrossing.skip(b,LastCrossingNight.RISE_AT);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"rose"),"the first rise"))
            .thenExecute(()->{h.assertTrue(!LastCrossing.flag(b,"held")&&!LastCrossing.flag(b,"loose"),"the ferry straining at its line");
                h.assertTrue(use(f,c[2],LastCrossingSite.RAMP_COIL)&&LastCrossing.flag(b,"held")&&is(l,b,LastCrossingSite.STAGED_COIL,Kind.COIL,2),"the loose line round the cleat");
                h.assertTrue(f.own(c[2],P).getBoolean("HeldFerry")&&f.own(c[2],P).getBoolean("SawSill"),"the ferry holds against its sill: "+f.own(c[2],P));
                LastCrossing.skip(b,LastCrossing.NIGHT_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN&&LastCrossing.flag(b,"dawn"),"dawn"))
            .thenExecute(()->{h.assertTrue(LastCrossing.undertows(b)==0&&l.getEntitiesOfClass(Undertow.class,box(f)).isEmpty(),"they go back into the water");
                for(var p:c)h.assertTrue(f.own(p,P).getBoolean("NightOne"),"Night One for "+p.getGameProfile().getName());
                h.assertTrue(f.own(c[0],P).getBoolean("Infected"),"still carrying it in the morning");
                LastCrossing.resetNow(l,b);
                h.assertTrue(is(l,b,LastCrossingSite.BAR_REST,Kind.BAR,0)&&l.getBlockState(b.offset(LastCrossingSite.BAR_ACROSS)).isAir()&&is(l,b,LastCrossingSite.GRATE,Kind.GRATE,1)&&is(l,b,LastCrossingSite.RAMP_COIL,Kind.COIL,0)&&l.getBlockState(b.offset(LastCrossingSite.STAGED_COIL)).isAir(),"the station set back");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_fight",timeoutTicks=9000)
    public static void nightOneFightingFree(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        crew(f,h,c,"lc_fight")
            .thenExecute(()->LastCrossing.skip(b,LastCrossing.DAY_TICKS))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK,"dusk, the intake never primed"))
            .thenExecute(()->{f.at(c[0],P,16.5,-6,-62.5);f.at(c[2],P,32.5,0,-61.5);LastCrossing.skip(b,LastCrossingNight.SURGE_LATE);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"grip"),"the surge comes anyway"))
            .thenExecute(()->{h.assertTrue(c[0].getUUID().equals(LastCrossing.carrier(b)),"to whoever is below");
                for(int i=0;i<LastCrossingNight.BLOWS-1;i++)h.assertTrue(LastCrossing.hit(c[0])&&LastCrossing.flag(b,"grip"),"blow "+(i+1)+": it holds");
                h.assertTrue(LastCrossing.hit(c[0])&&!LastCrossing.flag(b,"grip"),"the last blow, and it lets go");
                h.assertTrue(f.own(c[0],P).getBoolean("FoughtFree")&&f.own(c[0],P).getString("Freed").equals("fought")&&!LastCrossing.flag(b,"flooded"),"fought free alone, the lower floor kept");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("Beat"),"the carrier's beat"))
            // Nobody makes the line fast at the first rise.
            .thenExecute(()->LastCrossing.skip(b,LastCrossingNight.RISE_AT))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"rose"),"the rise"))
            .thenExecute(()->LastCrossing.skip(b,LastCrossingNight.RISE_WINDOW))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"loose"),"the ferry swings off its sill"))
            .thenExecute(()->h.assertTrue(is(l,b,LastCrossingSite.RAMP_COIL,Kind.COIL,3)&&f.own(c[2],P).getBoolean("SawFerryLoose")&&!LastCrossing.flag(b,"held"),"the line snapped, seen from the ramp"))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.undertows(b)>=1,"something up out of the water"))
            .thenExecute(()->{var u=l.getEntitiesOfClass(Undertow.class,box(f)).get(0);c[3].hurt(l.damageSources().mobAttack(u),2F);
                h.assertTrue(f.own(c[3],P).getBoolean("Wounded")&&!f.own(c[3],P).getBoolean("Infected"),"a clean reader cut, and still clean");
                LastCrossing.skip(b,LastCrossing.NIGHT_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN,"dawn"))
            .thenExecute(()->{h.assertTrue(f.own(c[0],P).getBoolean("NightOne")&&LastCrossing.undertows(b)==0,"through the night");h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_flood",timeoutTicks=9000)
    public static void nightOneTheLowerFloorLost(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        var gaugeRoom=new AABB(b.getX()+13,b.getY(),b.getZ()-65,b.getX()+24,b.getY()+4,b.getZ()-52);
        crew(f,h,c,"lc_flood")
            .thenExecute(()->LastCrossing.skip(b,LastCrossing.DAY_TICKS))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK,"dusk"))
            .thenExecute(()->{f.at(c[0],P,16.5,-6,-62.5);f.at(c[3],P,17.5,-6,-61.5);f.at(c[1],P,21.5,0,-58.5);LastCrossing.skip(b,LastCrossingNight.SURGE_LATE);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"grip"),"a hand through the grate"))
            .thenExecute(()->{h.assertTrue(LastCrossing.hit(c[3])&&LastCrossing.flag(b,"grip")&&f.own(c[3],P).getBoolean("StruckHand"),"a blow from the other one below, not enough");})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"flooded"),"with nobody on the brake, the intake fills"))
            .thenExecute(()->{h.assertTrue(l.getBlockState(b.offset(18,-6,-58)).is(Blocks.WATER)&&l.getBlockState(b.offset(20,-3,-56)).is(Blocks.WATER)&&l.getBlockState(b.offset(18,-2,-56)).isAir(),"the lower floor under water");
                h.assertTrue(!LastCrossing.flag(b,"grip")&&f.own(c[0],P).getString("Freed").equals("flood")&&f.own(c[0],P).getBoolean("Infected"),"the grip lets go as the water comes");
                for(var p:new ServerPlayer[]{c[0],c[3]})h.assertTrue(LastCrossingSite.inGaugeRoom(rel(f,p))&&f.own(p,P).getBoolean("LowerFlooded"),"up the stair, streaming: "+p.getGameProfile().getName()+" "+rel(f,p));
                h.assertTrue(f.own(c[1],P).getBoolean("LowerFlooded"),"the gauge room knows");
                LastCrossing.skip(b,LastCrossingNight.ISOLATE_GRACE+LastCrossingNight.SPAWN_EVERY);})
            .thenWaitUntil(()->h.assertTrue(!l.getEntitiesOfClass(Undertow.class,gaugeRoom).isEmpty(),"left open, they come up through the landing"))
            .thenExecute(()->{f.at(c[1],P,21.5,0,-64.5);
                h.assertTrue(use(f,c[1],LastCrossingSite.ISOLATOR)&&is(l,b,LastCrossingSite.ISOLATOR,Kind.ISOLATOR,1)&&LastCrossing.flag(b,"isolated")&&f.own(c[1],P).getBoolean("Isolated"),"the isolation valve shut");
                LastCrossing.skip(b,LastCrossing.NIGHT_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN,"dawn"))
            .thenExecute(()->{LastCrossing.resetNow(l,b);
                h.assertTrue(l.getBlockState(b.offset(18,-6,-58)).isAir()&&is(l,b,LastCrossingSite.ISOLATOR,Kind.ISOLATOR,0)&&l.getBlockState(b.offset(16,-8,-63)).is(Blocks.WATER),"the lower floor dry again, the valve open, the water under the grate");
                h.succeed();});
    }
}
