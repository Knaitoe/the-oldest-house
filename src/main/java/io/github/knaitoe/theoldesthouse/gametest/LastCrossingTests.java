package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
 *
 * <p>0.4.353, Day Two. The drum lined up: the morning, Vera at the slate and the cable tools on the desk, the tools taken for the far end,
 * the witness plate scraped and read and the reading called aloud, a catch set off the marks that slips, the drum creeping with nobody on
 * the brake, eased and cranked onto the marks and held there, the catch dropped square and holding with nobody on the brake, Vera asked
 * about Ellis, her two words before dusk, the second dusk, and the station set back. A bypass and what it leaves: the winch's bypass opened
 * by a carrier and seen, the spur flooded and its grate wet, the stain under the fairlead, the gauge dipping sooner; the carrier's call at
 * the wet grate heard through the pipes, one of the things come up through it in daylight and passing the carrier by, a call smothered by
 * the plate; the ramp's bypass opened and shut; the winch's shut, its grate damp and drying.
 *
 * <p>0.4.354, Night Two. The turn and the roll: the carrier turned at the second dusk where they stand on the ramp, seen by a reader with a
 * view and not by one below; water breathing; their "Here." and their chat broken (the rasp, the caught consonant, the hiss) while a clean
 * reader's chat is untouched, and the panel showing the speaker the same broken words everyone heard; the roll from the cable house's
 * doorway, the reader below absent and noted absent by the others; dawn, the turned back to themselves. The pull: lined up on the marks
 * by day, the roller seated, the door barred; the brake taken off and held, the drum binding at the jammed fairlead halfway, the jam worked
 * free from above with the marlinspike, the brake let go and the drum running back, caught by another, and the drum taken up, the catch
 * dropped back in. The water and the grate: the winch's branch left open by day; the turned carrier down through the hatch, seen going;
 * their call from the water drawing one of the things down into the branch, held under the floor grate while a clean reader stands on
 * it, and up through it when they step off; the jam cleared from below in one shove, seen from above as the cable jumping free with
 * nobody at it; up the hatch, leaving wet prints that another reader sees; the carrier holding the live grate shut and choking, their
 * words choked worse, until a friend pins it; a claw, the splatter and the tear, kept through dawn with the prints dried to a tidemark;
 * and the station set back. The grapple and the second slot: a crew of six, one caught on their own and pulled free by a friend, caught
 * again and held through, the second slot filled in a full crew still standing; another held through with the slot full, a deep cut;
 * the pull let go with nobody to catch it, the rollers thrown.
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
    @AfterBatch(batch="last_crossing_day") public static void dayDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_bypass") public static void bypassDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_turn") public static void turnDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_pull") public static void pullDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_water") public static void waterDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_slot") public static void slotDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_launch") public static void launchDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_gallery") public static void galleryDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_ferry") public static void ferryDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_cleared") public static void clearedDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_breath") public static void breathDone(ServerLevel l){close();}
    @AfterBatch(batch="last_crossing_taken") public static void takenDone(ServerLevel l){close();}
    private static void look(LiteraryTests.Fixture f,ServerPlayer p,Vec3 rel){var b=f.base(P);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z));}
    /** A crew through Day One and Night One untouched, into the morning and Day Two. */
    private static GameTestSequence dayTwo(LiteraryTests.Fixture f,GameTestHelper h,ServerPlayer[] c,String prefix,Runnable atStart){
        var b=f.base(P);
        return crew(f,h,c,prefix)
            .thenExecute(()->LastCrossing.skip(b,LastCrossing.DAY_TICKS))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK,"dusk"))
            .thenExecute(()->LastCrossing.skip(b,LastCrossing.NIGHT_TICKS))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN,"the morning after"))
            .thenExecute(()->{atStart.run();LastCrossing.skip(b,LastCrossingDay.MORNING);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAY&&LastCrossing.dayNo(b)==2,"Day Two"));
    }
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
                h.assertTrue(WitnessAccount.Story.of(P.id())==WitnessAccount.Story.LAST_CROSSING&&WitnessAccount.Story.values().length==WitnessPool.SOURCES,"a Witness source, now the crossing can be finished");
                var probe=UUID.randomUUID();h.assertTrue(!LastCrossing.dealable(f.data(),probe),"not dealt to a reader who has walked none of the yard's crossings, alone");
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

    @GameTest(template="empty",batch="last_crossing_day",timeoutTicks=12000)
    public static void dayTwoTheDrumLinedUp(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];int[] seen={0};
        dayTwo(f,h,c,"lc_day",()->{})
            .thenExecute(()->f.at(c[0],P,14.5,0,-42.5))
            .thenWaitUntil(()->h.assertTrue(is(l,b,LastCrossingSite.TOOLS,Kind.TOOLS,0),"Vera at the slate, the cable tools on the desk"))
            .thenExecute(()->{h.assertTrue(f.own(c[0],P).getBoolean("HeardSlate"),"Ellis still on the slate, heard in the office");
                f.at(c[0],P,17.5,0,-43.5);h.assertTrue(use(f,c[0],LastCrossingSite.TOOLS)&&carries(c[0],"tools")&&LastCrossing.flag(b,"tools"),"the tools taken");
                h.assertTrue(f.own(c[0],P).getBoolean("FarEnd")&&l.getBlockState(b.offset(LastCrossingSite.TOOLS)).isAir(),"the far end taken");
                // The far end: greased until scraped with the tools, then read.
                f.at(c[0],P,45.5,0,-63.5);c[0].getInventory().selected=8;
                h.assertTrue(use(f,c[0],LastCrossingSite.WITNESS)&&is(l,b,LastCrossingSite.WITNESS,Kind.WITNESS,0),"no reading through the grease bare-handed");
                select(c[0],"tools");h.assertTrue(use(f,c[0],LastCrossingSite.WITNESS)&&is(l,b,LastCrossingSite.WITNESS,Kind.WITNESS,1)&&f.own(c[0],P).getBoolean("ScrapedMarks"),"scraped clean");
                int o=LastCrossing.offset(b);h.assertTrue(o!=0,"the drum starts off the marks");
                h.assertTrue(use(f,c[0],LastCrossingSite.WITNESS)&&Talk.replies(c[0]).get(0).endsWith(o>0?" short!":" past!"),"the marks read: "+Talk.replies(c[0]));Talk.close(c[0]);
                // A catch dropped off the marks.
                f.at(c[1],P,50.5,0,-57.5);seen[0]=o;
                h.assertTrue(use(f,c[1],LastCrossingSite.CATCH)&&LastCrossing.flag(b,"catch")&&!LastCrossing.flag(b,"lined"),"the catch set off the marks");})
            .thenWaitUntil(()->h.assertTrue(!LastCrossing.flag(b,"catch"),"it slips"))
            .thenExecute(()->{h.assertTrue(LastCrossing.offset(b)==seen[0]-1&&f.own(c[1],P).getBoolean("CatchSlipped"),"the drum runs out a notch as it slips");
                seen[0]=LastCrossing.offset(b);f.at(c[1],P,47.5,0,-61.5);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.offset(b)==seen[0]-1,"nobody on the brake: the drum creeps out"))
            .thenExecute(()->{f.at(c[1],P,50.5,0,-57.5);
                for(int i=0;i<12&&LastCrossing.offset(b)!=0;i++){boolean in=LastCrossing.offset(b)<0;c[1].setShiftKeyDown(in);h.assertTrue(use(f,c[1],LastCrossingSite.CABLE_BRAKE),"the brake");}
                c[1].setShiftKeyDown(false);
                h.assertTrue(LastCrossing.offset(b)==0,"eased and cranked onto the marks");
                h.assertTrue(f.own(c[1],P).getBoolean("EasedDrum")||f.own(c[1],P).getBoolean("CrankedDrum"),"worked from the brake");seen[0]=0;})
            .thenIdle(LastCrossingDay.CREEP+20)
            .thenExecute(()->{h.assertTrue(LastCrossing.offset(b)==0,"held on the brake, it doesn't creep");
                h.assertTrue(use(f,c[0],LastCrossingSite.WITNESS)&&Talk.replies(c[0]).get(0).startsWith("Marks meet"),"the marks meet");
                h.assertTrue(Talk.choose(c[0],"Marks meet")&&f.own(c[0],P).getBoolean("CalledMarks")&&Spoken.lastHeard(c[1],c[0]).startsWith("Marks meet"),"called aloud and heard at the brake");
                h.assertTrue(use(f,c[1],LastCrossingSite.CATCH)&&LastCrossing.flag(b,"lined")&&is(l,b,LastCrossingSite.CATCH,Kind.CATCH,1),"the catch dropped square");
                for(int i=0;i<2;i++){var own=f.own(c[i],P);h.assertTrue(own.getBoolean("LinedUp")&&own.getString("LinedWith").contains(c[1-i].getGameProfile().getName()),"lined up together: "+own.getString("LinedWith"));}
                f.at(c[1],P,44.5,0,-58.5);})
            .thenIdle(LastCrossingDay.CREEP+20)
            .thenExecute(()->{h.assertTrue(LastCrossing.offset(b)==0&&LastCrossing.flag(b,"catch"),"the catch holds on the marks with nobody on the brake");
                var vera=LastCrossing.vera(l,b);h.assertTrue(vera!=null,"Vera");LastCrossing.talk(c[2],vera);
                h.assertTrue(Talk.choose(c[2],"Did anybody")&&f.own(c[2],P).getBoolean("VeraAdmitted"),"Vera asked about Ellis, and admits it");Talk.close(c[2]);
                LastCrossing.skip(b,LastCrossing.DAY_TICKS*5/6);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"briefed"),"before dusk: the brake left set, and what Ellis's answer sounded like"))
            .thenExecute(()->{for(var p:c)h.assertTrue(f.own(p,P).getBoolean("HeardEllisVoice"),"heard by "+p.getGameProfile().getName());LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.dayNo(b)==2,"the second dusk"))
            .thenExecute(()->{for(var p:c)h.assertTrue(f.own(p,P).getBoolean("DayTwo"),"Day Two worked");h.assertTrue(LastCrossing.flag(b,"lined"),"still lined up for the pull");
                LastCrossing.resetNow(l,b);
                h.assertTrue(is(l,b,LastCrossingSite.WITNESS,Kind.WITNESS,0)&&is(l,b,LastCrossingSite.CATCH,Kind.CATCH,0)&&l.getBlockState(b.offset(LastCrossingSite.TOOLS)).isAir(),"the station set back");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_bypass",timeoutTicks=12000)
    public static void dayTwoABypassAndWhatItLeaves(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        var cable=new AABB(b.getX()+43,b.getY(),b.getZ()-65,b.getX()+52,b.getY()+4,b.getZ()-55);
        dayTwo(f,h,c,"lc_bypass",()->{})
            .thenExecute(()->{LastCrossing.carryForTest(c[0]);
                f.at(c[1],P,47.5,0,-61.5);look(f,c[1],Vec3.atCenterOf(LastCrossingSite.WINCH_BYPASS));
                f.at(c[2],P,22.5,0,-61.5);
                f.at(c[0],P,50.5,0,-60.5);
                h.assertTrue(use(f,c[0],LastCrossingSite.WINCH_BYPASS)&&LastCrossing.flag(b,"winchOpen")&&is(l,b,LastCrossingSite.WINCH_BYPASS,Kind.BYPASS,1),"the winch's bypass opened");
                h.assertTrue(l.getBlockState(b.offset(50,-4,-57)).is(Blocks.WATER)&&l.getBlockState(b.offset(51,-3,-58)).is(Blocks.WATER),"the spur flooded through its sluice");
                for(var g:LastCrossingSite.FLOOR_GRATES)h.assertTrue(is(l,b,g,Kind.FLOOR_GRATE,1),"its grate wet");
                h.assertTrue(is(l,b,LastCrossingSite.STAIN,Kind.STAIN,0),"a stain spreading under the fairlead");
                h.assertTrue(f.own(c[1],P).getString("SawBypassBy").contains(c[0].getGameProfile().getName())&&f.own(c[0],P).getBoolean("OpenedBypass"),"seen turning it");
                f.at(c[3],P,48.5,0,-63.0);c[3].setYRot(180);c[3].setYHeadRot(180);c[3].setXRot(50);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[3],P).getBoolean("SawStain"),"the stain seen"))
            .thenWaitUntil(()->h.assertTrue(f.own(c[2],P).getBoolean("SawBypassDip"),"the gauge dips sooner than yesterday"))
            // The carrier crouched by the wet grate: something answers, and comes up through it.
            .thenExecute(()->{f.at(c[0],P,50.5,0,-56.5);c[0].setShiftKeyDown(true);f.at(c[1],P,46.5,0,-58.5);look(f,c[1],new Vec3(50.5,1,-56.5));})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("HeardScrape")&&f.own(c[1],P).getBoolean("HeardScrape"),"the scrape through the pipes"))
            .thenExecute(()->{c[0].setShiftKeyDown(false);h.assertTrue(f.own(c[0],P).getInt("DayCalls")==1&&f.own(c[0],P).getBoolean("SawWetGrate"),"the call, at a grate seen wet");})
            .thenWaitUntil(()->h.assertTrue(!l.getEntitiesOfClass(Undertow.class,cable).isEmpty(),"something up through the grate in daylight"))
            .thenWaitUntil(()->{var u=l.getEntitiesOfClass(Undertow.class,cable).get(0);h.assertTrue(u.getTarget()!=null,"it goes for someone");h.assertTrue(u.getTarget()!=c[0],"not the carrier");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("PassedBy")&&f.own(c[1],P).getString("SawPassedBy").contains(c[0].getGameProfile().getName()),"it passes the carrier by, and that is seen"))
            .thenExecute(()->{for(var u:l.getEntitiesOfClass(Undertow.class,cable))u.discard();
                // A second carrier calls with the plate carried close: it catches in the throat.
                LastCrossing.carryForTest(c[3]);f.at(c[2],P,16.5,0,-44.5);h.assertTrue(use(f,c[2],LastCrossingSite.PLATE_HOME)&&Talk.choose(c[2],"Lift"),"the plate lifted");
                f.at(c[2],P,49.5,0,-59.5);f.at(c[3],P,51.5,0,-56.5);c[3].setShiftKeyDown(true);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[3],P).getBoolean("CallWarded"),"by the plate, nothing answers"))
            .thenExecute(()->{c[3].setShiftKeyDown(false);h.assertTrue(f.own(c[3],P).getInt("DayCalls")==0,"no call made");
                // The ramp's bypass, opened and shut again in the intake.
                f.at(c[1],P,22.5,-6,-61.5);
                h.assertTrue(use(f,c[1],LastCrossingSite.RAMP_BYPASS)&&LastCrossing.flag(b,"rampOpen")&&l.getBlockState(b.offset(35,-7,-60)).is(Blocks.WATER),"the ramp branch flooded");
                for(var g:LastCrossingSite.PIT_GRATES)h.assertTrue(is(l,b,g,Kind.BRANCH,1),"its grate into the pit wet");
                h.assertTrue(use(f,c[1],LastCrossingSite.RAMP_BYPASS)&&!LastCrossing.flag(b,"rampOpen")&&l.getBlockState(b.offset(35,-7,-60)).isAir()&&is(l,b,LastCrossingSite.PIT_GRATES.get(0),Kind.BRANCH,2),"shut again, the grate damp");
                h.assertTrue(f.own(c[1],P).getString("Bypasses").contains("ramp shut"),"remembered");
                // The winch's shut: the spur dry, its sluice down, the grate damp and the stain drying.
                f.at(c[0],P,50.5,0,-60.5);
                h.assertTrue(use(f,c[0],LastCrossingSite.WINCH_BYPASS)&&!LastCrossing.flag(b,"winchOpen"),"the winch's bypass shut");
                h.assertTrue(l.getBlockState(b.offset(50,-4,-57)).isAir()&&l.getBlockState(b.offset(50,-4,-58)).is(Blocks.STONE_BRICKS),"the spur dry behind its sluice");
                h.assertTrue(is(l,b,LastCrossingSite.FLOOR_GRATES.get(0),Kind.FLOOR_GRATE,2)&&is(l,b,LastCrossingSite.STAIN,Kind.STAIN,1),"damp, and drying");})
            .thenWaitUntil(()->h.assertTrue(is(l,b,LastCrossingSite.FLOOR_GRATES.get(0),Kind.FLOOR_GRATE,0)&&l.getBlockState(b.offset(LastCrossingSite.STAIN)).isAir()&&is(l,b,LastCrossingSite.PIT_GRATES.get(0),Kind.BRANCH,0),"dry again"))
            .thenExecute(h::succeed);
    }

    // ------------------------------------------------------------------------------------------------ Night Two (0.4.354)
    private static String name(ServerPlayer p){return p.getGameProfile().getName();}
    private static void heal(ServerPlayer... crew){for(var p:crew)if(p!=null)p.setHealth(p.getMaxHealth());}
    private static void clearUndertows(ServerLevel l,LiteraryTests.Fixture f){for(var u:l.getEntitiesOfClass(Undertow.class,box(f)))u.discard();}

    @GameTest(template="empty",batch="last_crossing_turn",timeoutTicks=14000)
    public static void nightTwoTheTurnAndTheRoll(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        dayTwo(f,h,c,"lc_turn",()->{})
            .thenExecute(()->{LastCrossing.carryForTest(c[0]);
                // Before the light goes: the carrier out on the ramp with a reader watching; one gone below into the intake; one by the cable house.
                f.at(c[0],P,33.5,0,-58.5);f.at(c[1],P,33.5,0,-53.5);look(f,c[1],new Vec3(33.5,1.6,-58.5));
                f.at(c[2],P,18.5,-6,-58.5);f.at(c[3],P,44.5,0,-51.5);
                LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.dayNo(b)==2,"the second dusk"))
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.turned(b).contains(c[0].getUUID()),"the carrier turns at sunset, where they stand"))
            .thenExecute(()->{var own=f.own(c[0],P);
                h.assertTrue(own.getBoolean("Turned")&&own.getString("TurnedWhere").equals("ramp")&&!own.getBoolean("TurnedUnseen"),"turned on the ramp, and seen doing it");
                h.assertTrue(f.own(c[1],P).getString("SawTurn").contains(name(c[0])),"whoever had a view knows whose face it was");
                h.assertTrue(f.own(c[2],P).getString("SawTurn").isEmpty(),"not from below");
                // Their words come out broken: aloud, in the panel the speaker sees, and in chat; a clean reader's chat is their own.
                h.assertTrue(Spoken.say(c[0],"Here.").equals("H-here.")&&Spoken.lastHeard(c[1],c[0]).equals("H-here."),"a name caught on its first sound, the same for the one who hears it");
                Talk.begin(c[0],"lc_turn_test","test");Talk.ask(c[0],Talk.reply("Wait for me down by the ramp.",x->{}));Talk.choose(c[0],"Wait");
                var said=Spoken.lastHeard(c[1],c[0]);
                h.assertTrue(!said.equals("Wait for me down by the ramp.")&&said.contains("down")&&Talk.lines(c[0]).contains("you: "+said),"the speaker's panel shows what everyone heard: "+said);
                Talk.close(c[0]);
                var chat=new net.neoforged.neoforge.event.ServerChatEvent(c[0],"I'm under the ramp. Put the pin in when I knock.",net.minecraft.network.chat.Component.literal("I'm under the ramp. Put the pin in when I knock."));
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(chat);var heard=chat.getMessage().getString();
                h.assertTrue(heard.contains(LastCrossingSpeech.HISS)&&heard.contains("under")&&!heard.equals(chat.getRawText()),"chat comes out broken, the direction kept: "+heard);
                var clean=new net.neoforged.neoforge.event.ServerChatEvent(c[1],"I'm under the ramp.",net.minecraft.network.chat.Component.literal("I'm under the ramp."));
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(clean);h.assertTrue(clean.getMessage().getString().equals("I'm under the ramp."),"a clean reader's chat is untouched");
                h.assertTrue(f.own(c[1],P).getString("HeardRasp").contains(name(c[0])),"heard the rasp");
                LastCrossing.skip(b,LastCrossingNightTwo.ROLL_BY);})
            .thenWaitUntil(()->h.assertTrue(c[0].hasEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING),"the turned breathe water"))
            .thenWaitUntil(()->{clearUndertows(l,f);heal(c);answer(c);h.assertTrue(LastCrossing.flag(b,"rolled"),"the roll called from the cable house's doorway");})
            .thenExecute(()->{h.assertTrue(f.own(c[2],P).getBoolean("AbsentNightRoll")&&!f.own(c[2],P).getBoolean("AnsweredNightRoll"),"the one below never heard it");
                h.assertTrue(f.own(c[1],P).getBoolean("AnsweredNightRoll")&&f.own(c[1],P).getString("NotedAbsent").contains(name(c[2])),"the rest answered, and noted who didn't");
                h.assertTrue(f.own(c[0],P).getBoolean("AnsweredNightRoll"),"the turned can answer too");
                LastCrossing.skip(b,LastCrossing.NIGHT_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN&&LastCrossing.dayNo(b)==3,"the third morning"))
            .thenExecute(()->{h.assertTrue(LastCrossingNightTwo.turned(b).isEmpty()&&f.own(c[0],P).getBoolean("BackAtDawn")&&!c[0].hasEffect(net.minecraft.world.effect.MobEffects.WATER_BREATHING),"back to themselves at dawn");
                h.assertTrue(f.own(c[1],P).getBoolean("NightTwo")&&Spoken.say(c[0],"Here.").equals("Here."),"Night Two worked, and daylight speech is normal");
                LastCrossing.resetNow(l,b);h.assertTrue(LastCrossing.phase(b)==-1,"the station set back");h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_pull",timeoutTicks=14000)
    public static void nightTwoThePull(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        dayTwo(f,h,c,"lc_pull",()->{})
            .thenExecute(()->{f.at(c[1],P,46.5,0,-60.5);h.assertTrue(use(f,c[1],LastCrossingSite.LOOSE_ROLLER),"the roller seated");
                // Lined up on the marks by day: eased or cranked onto them, the catch dropped square.
                f.at(c[1],P,48.5,0,-57.5);int o=LastCrossing.offset(b);c[1].setShiftKeyDown(o<0);for(int i=0;i<Math.abs(o);i++)use(f,c[1],LastCrossingSite.CABLE_BRAKE);c[1].setShiftKeyDown(false);
                h.assertTrue(LastCrossing.offset(b)==0&&use(f,c[1],LastCrossingSite.CATCH)&&LastCrossing.flag(b,"lined"),"lined up on the marks");})
            .thenWaitUntil(()->h.assertTrue(is(l,b,LastCrossingSite.TOOLS,Kind.TOOLS,0),"the tools on the desk"))
            .thenExecute(()->{f.at(c[2],P,18.5,0,-43.5);h.assertTrue(use(f,c[2],LastCrossingSite.TOOLS)&&carries(c[2],"tools"),"the tools taken");
                // Everyone into the cable house and the bar dropped across the door before dark.
                f.at(c[0],P,46.5,0,-56.5);f.at(c[1],P,48.5,0,-57.5);f.at(c[2],P,47.5,0,-64.5);f.at(c[3],P,44.5,0,-56.5);
                h.assertTrue(use(f,c[3],LastCrossingSite.BAR_REST)&&LastCrossing.flag(b,"barred"),"the door barred");
                LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.dayNo(b)==2,"the second dusk"))
            .thenExecute(()->LastCrossing.skip(b,LastCrossingNightTwo.PULL_BY))
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.CALLED&&!LastCrossing.flag(b,"catch"),"the pull called, the catch out of the drum's teeth"))
            .thenExecute(()->{h.assertTrue(use(f,c[1],LastCrossingSite.CABLE_BRAKE)&&LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.HAULING,"the brake taken off and held");
                h.assertTrue(f.own(c[1],P).getBoolean("HeldPull"),"remembered");
                LastCrossingNightTwo.haul(b,600);})
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.BINDING,"binding at the jammed fairlead halfway"))
            .thenExecute(()->{select(c[2],"tools");h.assertTrue(use(f,c[2],LastCrossingSite.FAIRLEAD),"the marlinspike to the kink");})
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.FAIRLEAD)).thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.FAIRLEAD))
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.FAIRLEAD)).thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.FAIRLEAD))
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.FAIRLEAD)).thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.FAIRLEAD))
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.FAIRLEAD))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"jam")&&is(l,b,LastCrossingSite.FAIRLEAD,Kind.FAIRLEAD,1)&&f.own(c[2],P).getBoolean("ClearedJam"),"worked free from above"))
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.HAULING,"hauling again"))
            // The brake let go: the drum runs back, and another catches it.
            .thenExecute(()->f.at(c[1],P,44.5,0,-63.5))
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.RUNNING_BACK&&f.own(c[3],P).getBoolean("SawRunBack"),"running back"))
            .thenExecute(()->{f.at(c[3],P,48.5,0,-57.5);h.assertTrue(use(f,c[3],LastCrossingSite.CABLE_BRAKE)&&LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.HAULING,"caught");
                h.assertTrue(f.own(c[3],P).getBoolean("CaughtBrake")&&f.own(c[1],P).getBoolean("LetGoBrake"),"who let go and who caught it");
                LastCrossingNightTwo.haul(b,LastCrossingNightTwo.PULL_LINED);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"pulled"),"taken up"))
            .thenExecute(()->{h.assertTrue(f.own(c[3],P).getBoolean("Pulled")&&f.own(c[1],P).getBoolean("Pulled")&&f.own(c[0],P).getBoolean("SawPull"),"everyone who held it, and who saw it");
                h.assertTrue(is(l,b,LastCrossingSite.CATCH,Kind.CATCH,1)&&LastCrossing.flag(b,"catch"),"the catch dropped back in");
                LastCrossing.skip(b,LastCrossing.NIGHT_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN&&LastCrossing.flag(b,"pulled"),"the third morning, still taken up"))
            .thenExecute(h::succeed);
    }

    @GameTest(template="empty",batch="last_crossing_water",timeoutTicks=14000)
    public static void nightTwoTheWaterAndTheGate(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        var cable=new AABB(b.getX()+43,b.getY()-8,b.getZ()-66,b.getX()+52,b.getY()+4,b.getZ()-55);
        dayTwo(f,h,c,"lc_water",()->{})
            .thenExecute(()->{LastCrossing.carryForTest(c[0]);f.at(c[0],P,50.5,0,-60.5);h.assertTrue(use(f,c[0],LastCrossingSite.WINCH_BYPASS)&&LastCrossing.flag(b,"winchOpen"),"the winch's branch left open");
                f.at(c[0],P,45.5,0,-60.5);f.at(c[1],P,48.5,0,-58.5);f.at(c[2],P,44.5,0,-57.5);f.at(c[3],P,48.5,0,-62.5);
                LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.turned(b).contains(c[0].getUUID()),"turned at the second dusk"))
            // Down through the hatch, seen going.
            .thenExecute(()->{look(f,c[1],new Vec3(45.5,1.6,-60.5));h.assertTrue(use(f,c[0],LastCrossingSite.HATCH)&&rel(f,c[0]).y<-1.5,"down through the hatch into the passage");
                h.assertTrue(f.own(c[0],P).getBoolean("WentBelowHatch")&&f.own(c[1],P).getString("SawGoBelow").contains(name(c[0])),"seen going");
                LastCrossing.skip(b,LastCrossingNightTwo.FIRST_SPAWN);})
            .thenWaitUntil(()->h.assertTrue(!LastCrossingNightTwo.mobs(b).isEmpty(),"something up out of the spillway"))
            // Called from the water: it goes down into the branch after the breath.
            .thenExecute(()->c[0].setShiftKeyDown(true))
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("DrewIntoBranch")&&LastCrossingNightTwo.below(b)>=1,"called down into the branch"))
            .thenExecute(()->{c[0].setShiftKeyDown(false);h.assertTrue(f.own(c[1],P).getBoolean("HeardScrape"),"the scrape through the pipes");clearUndertows(l,f);
                // A clean reader stands on the grate behind the brake: what was called knocks under it and can't come up.
                f.at(c[1],P,50.5,0,-56.5);h.assertTrue(use(f,c[1],LastCrossingSite.FLOOR_GRATES.get(0))&&LastCrossing.flag(b,"gateHeld"),"the grate held down");
                h.assertTrue(f.own(c[1],P).getBoolean("ShutThemIn"),"shut them in");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[1],P).getBoolean("FeltGrateKnock"),"something knocking under the iron"))
            .thenExecute(()->{look(f,c[3],new Vec3(50.5,1,-56.5));h.assertTrue(use(f,c[1],LastCrossingSite.FLOOR_GRATES.get(0))&&!LastCrossing.flag(b,"gateHeld"),"stepped off the grate");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[3],P).getBoolean("SawGrateRise")&&!l.getEntitiesOfClass(Undertow.class,cable).isEmpty(),"up through the grate behind the brake"))
            // The jam from below, in one shove, seen from above as the cable jumping free with nobody at it.
            .thenExecute(()->{clearUndertows(l,f);heal(c);f.at(c[0],P,47.5,-3,-64.5);
                h.assertTrue(use(f,c[0],LastCrossingSite.UNDERSIDE)&&LastCrossing.flag(b,"jam")&&is(l,b,LastCrossingSite.FAIRLEAD,Kind.FAIRLEAD,1),"the jam cleared from below");
                h.assertTrue(f.own(c[0],P).getBoolean("ClearedJamBelow")&&f.own(c[1],P).getBoolean("SawJamCleared")&&!f.own(c[1],P).getBoolean("ClearedJam"),"seen from above, with nobody at it");
                // Up the hatch, leaving wet prints by it.
                f.at(c[0],P,45.5,-3,-60.5);h.assertTrue(use(f,c[0],LastCrossingSite.HATCH)&&rel(f,c[0]).y>-.5&&is(l,b,LastCrossingSite.HATCH_PRINTS,Kind.PRINTS,0),"up through the hatch, running water");
                f.at(c[2],P,44.5,0,-58.7);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[2],P).getBoolean("SawPrints"),"the wet prints seen"))
            // The turned carrier holds the live grate shut, and chokes on it, until a friend pins it.
            .thenExecute(()->{f.at(c[0],P,50.5,0,-56.5);f.at(c[1],P,51.5,0,-55.5);h.assertTrue(use(f,c[0],LastCrossingSite.FLOOR_GRATES.get(0))&&LastCrossing.flag(b,"gateHeld"),"held down by the carrier");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("Choked"),"the throat closes"))
            .thenExecute(()->{var said=Spoken.say(c[0],"Pin it. I can't get a breath. Put the pin in now.");
                h.assertTrue(said.contains(LastCrossingSpeech.HISS)&&said.contains("can't")&&Spoken.lastHeard(c[1],c[0]).equals(said),"choked words, the negation kept: "+said);})
            .thenWaitUntil(()->h.assertTrue(c[0].getHealth()<c[0].getMaxHealth(),"choking"))
            .thenExecute(()->{h.assertTrue(use(f,c[1],LastCrossingSite.FLOOR_GRATES.get(0))&&LastCrossing.flag(b,"pinned")&&!LastCrossing.flag(b,"gateHeld")&&is(l,b,LastCrossingSite.FLOOR_GRATES.get(0),Kind.FLOOR_GRATE,3),"pinned by a friend");
                h.assertTrue(f.own(c[1],P).getString("PinnedFor").contains(name(c[0]))&&f.own(c[0],P).getString("PinnedBy").contains(name(c[1])),"who took the load off whom");
                // A claw: the splatter and the tear.
                f.at(c[3],P,49.5,0,-60.5);f.at(c[2],P,46.5,0,-60.5);look(f,c[2],new Vec3(49.5,1,-60.5));heal(c[3]);c[3].invulnerableTime=0;
                h.assertTrue(LastCrossingNightTwo.claw(c[0],c[3]),"the claw comes down");
                h.assertTrue(c[3].getHealth()<=c[3].getMaxHealth()-3.5F,"a claw hits harder than a fist");
                h.assertTrue(LastCrossingNightTwo.torn(b).contains(c[3].getUUID())&&f.own(c[3],P).getString("ClawedBy").contains(name(c[0]))&&f.own(c[0],P).getInt("Claws")==1,"clawed, and torn");
                h.assertTrue(f.own(c[2],P).getString("SawClaws").contains(name(c[0])),"seen");
                LastCrossing.skip(b,LastCrossing.NIGHT_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN,"dawn"))
            .thenExecute(()->{h.assertTrue(is(l,b,LastCrossingSite.HATCH_PRINTS,Kind.PRINTS,1)&&is(l,b,LastCrossingSite.FLOOR_GRATES.get(0),Kind.FLOOR_GRATE,3),"the prints dried to a tidemark, the pin still in");
                h.assertTrue(LastCrossingNightTwo.torn(b).contains(c[3].getUUID()),"the tear carried through dawn");
                LastCrossing.resetNow(l,b);
                h.assertTrue(l.getBlockState(b.offset(LastCrossingSite.HATCH_PRINTS)).isAir()&&is(l,b,LastCrossingSite.FLOOR_GRATES.get(0),Kind.FLOOR_GRATE,0)&&is(l,b,LastCrossingSite.UNDERSIDE,Kind.UNDERSIDE,0),"the station set back");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_slot",timeoutTicks=16000)
    public static void nightTwoTheGrappleAndTheSecondSlot(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[6];
        dayTwo(f,h,c,"lc_slot",()->{})
            .thenExecute(()->{LastCrossing.carryForTest(c[0]);for(int i=0;i<6;i++)if(i!=1)f.at(c[i],P,12.5+i,0,-36.5);f.at(c[1],P,33.5,0,-44.5);
                LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.dayNo(b)==2,"the second dusk"))
            .thenExecute(()->{heal(c);LastCrossing.skip(b,LastCrossingNightTwo.FIRST_SPAWN);})
            // One on their own by the spillway: it gets its arms round them.
            .thenWaitUntil(()->h.assertTrue(c[1].getUUID().equals(LastCrossingNightTwo.grappled(b)),"caught on their own"))
            .thenExecute(()->{h.assertTrue(f.own(c[1],P).getBoolean("Grappled"),"remembered");
                f.at(c[2],P,c[1].getX()-b.getX()+1,0,c[1].getZ()-b.getZ());h.assertTrue(LastCrossingNightTwo.pullFree(c[2],c[1])&&LastCrossingNightTwo.grappled(b)==null,"a friend hauls them free");
                h.assertTrue(f.own(c[2],P).getBoolean("BrokeGrapple")&&f.own(c[1],P).getString("BrokenFreeBy").contains(name(c[2])),"who broke it");
                f.at(c[2],P,14.5,0,-36.5);heal(c);})
            .thenWaitUntil(()->h.assertTrue(c[1].getUUID().equals(LastCrossingNightTwo.grappled(b)),"caught again, alone"))
            .thenExecute(()->{for(var p:c)p.setHealth(p.getMaxHealth());})
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.grappled(b)==null,"held through"))
            .thenExecute(()->{h.assertTrue(c[1].getUUID().equals(LastCrossingNightTwo.secondSlot(b))&&f.own(c[1],P).getBoolean("SecondSlot")&&f.own(c[1],P).getInt("Slot")==2&&f.own(c[1],P).getBoolean("Infected"),"a full crew of six, all standing: the second slot");
                h.assertTrue(LastCrossing.spared(c[1]),"the things pass the new carrier by");
                clearUndertows(l,f);heal(c);
                // Another, held through with the slot full: a deep cut.
                f.at(c[4],P,43.5,0,-45.5);f.at(c[1],P,13.5,0,-38.5);LastCrossing.skip(b,LastCrossingNightTwo.SPAWN_EVERY);})
            .thenWaitUntil(()->h.assertTrue(c[4].getUUID().equals(LastCrossingNightTwo.grappled(b)),"another caught on their own"))
            .thenExecute(()->{for(var p:c)p.setHealth(p.getMaxHealth());})
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.grappled(b)==null,"held through"))
            .thenExecute(()->{h.assertTrue(f.own(c[4],P).getBoolean("DeepCut")&&!f.own(c[4],P).getBoolean("Infected")&&c[1].getUUID().equals(LastCrossingNightTwo.secondSlot(b)),"the slot full: a deep cut, and nothing more");
                clearUndertows(l,f);heal(c);for(int i=0;i<6;i++)f.at(c[i],P,44.5+i%3,0,-57.5-i/3);
                LastCrossing.skip(b,LastCrossingNightTwo.PULL_BY);})
            // The pull let go with nobody to catch it: the rollers thrown.
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.CALLED,"the pull called"))
            .thenExecute(()->{f.at(c[3],P,48.5,0,-57.5);h.assertTrue(use(f,c[3],LastCrossingSite.CABLE_BRAKE),"the brake taken off");})
            .thenExecute(()->{for(int i=0;i<6;i++)f.at(c[i],P,15.5+i,0,-36.5);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"pullFailed"),"nobody caught it"))
            .thenExecute(()->{h.assertTrue(is(l,b,LastCrossingSite.LOOSE_ROLLER,Kind.ROLLER,1)&&is(l,b,LastCrossingSite.ROLLERS.get(2),Kind.ROLLER,1)&&!LastCrossing.flag(b,"roller"),"the rollers thrown");
                h.assertTrue(f.own(c[3],P).getBoolean("PullFailed")&&f.own(c[3],P).getBoolean("LetGoBrake"),"remembered");
                clearUndertows(l,f);h.succeed();});
    }

    // ------------------------------------------------------------------------------------------------ Day Three (0.4.355)
    /** A crew through Day Two (with something done there) and Night Two (with whatever the night is given), into the third morning and Day
     * Three. */
    private static GameTestSequence dayThree(LiteraryTests.Fixture f,GameTestHelper h,ServerPlayer[] c,String prefix,Runnable atDayTwo,java.util.function.UnaryOperator<GameTestSequence> night){
        var b=f.base(P);var l=f.level(P);
        var seq=dayTwo(f,h,c,prefix,()->{})
            .thenExecute(()->{atDayTwo.run();LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.dayNo(b)==2,"the second dusk"));
        return night.apply(seq)
            .thenExecute(()->LastCrossing.skip(b,LastCrossing.NIGHT_TICKS))
            .thenWaitUntil(()->{clearUndertows(l,f);heal(c);h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAWN&&LastCrossing.dayNo(b)==3,"the third morning");})
            .thenExecute(()->LastCrossing.skip(b,LastCrossingDayThree.MORNING))
            .thenWaitUntil(()->{clearUndertows(l,f);h.assertTrue(LastCrossing.phase(b)==LastCrossing.DAY&&LastCrossing.dayNo(b)==3,"Day Three");});
    }

    @GameTest(template="empty",batch="last_crossing_launch",timeoutTicks=18000)
    public static void dayThreeTheLaunchMadeReady(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        dayThree(f,h,c,"lc_launch",()->{var vera=LastCrossing.vera(l,b);h.assertTrue(vera!=null,"Vera");LastCrossing.talk(c[0],vera);h.assertTrue(Talk.choose(c[0],"Did anybody"),"asked about Ellis");Talk.close(c[0]);},s->s)
            .thenExecute(()->{
                h.assertTrue(is(l,b,LastCrossingSite.ROLLERS.get(3),Kind.ROLLER,1),"the rise jumped the cable off the last roller");
                h.assertTrue(is(l,b,LastCrossingSite.PIN,Kind.PIN,2)&&is(l,b,LastCrossingSite.PIN_RACK,Kind.RACK,1),"the sleeve empty, the pin on the porch rack");
                h.assertTrue(use(f,c[1],LastCrossingSite.RELEASE)&&Talk.replies(c[1]).stream().anyMatch(x->x.startsWith("I'll take")),"the launch rope can be claimed by day");Talk.close(c[1]);
                // Three at the ramp with Vera, one gone below into the intake.
                f.at(c[0],P,30.5,0,-56.5);f.at(c[1],P,32.5,0,-56.5);f.at(c[2],P,28.5,0,-59.5);f.at(c[3],P,18.5,-6,-58.5);})
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("HeardDepth"),"the depth read against the sill"))
            .thenWaitUntil(()->{answer(c);h.assertTrue(LastCrossingDayThree.vera(b)>=5,"the roll taken at the ramp, Ellis called, and something answers up the pipe");})
            .thenExecute(()->{h.assertTrue(f.own(c[2],P).getBoolean("HeardEllisAnswer")&&!f.own(c[0],P).getBoolean("HeardEllisAnswer"),"heard only near a pipe's mouth");
                h.assertTrue(f.own(c[0],P).getBoolean("AnsweredDayRoll")&&f.own(c[3],P).getBoolean("AbsentDayRoll")&&!f.own(c[3],P).getBoolean("AnsweredDayRoll"),"the one below never heard their name");})
            .thenWaitUntil(()->h.assertTrue(LastCrossingDayThree.vera(b)>=6&&Talk.replies(c[0]).contains("The launch."),"who's on the launch, who's after the source, who's still below"))
            .thenExecute(()->{h.assertTrue(Talk.choose(c[0],"The launch")&&Talk.choose(c[1],"The source")&&Talk.choose(c[2],"Wherever"),"each says");
                h.assertTrue("launch".equals(LastCrossingDayThree.plan(b,c[0].getUUID()))&&f.own(c[1],P).getString("Plan").equals("source"),"plans kept");
                h.assertTrue(f.own(c[0],P).getString("NotedAbsentDay").contains(name(c[3])),"and noted who was still below");
                // The pin down from the porch to its sleeve.
                f.at(c[0],P,11.5,0,-36.5);h.assertTrue(use(f,c[0],LastCrossingSite.PIN_RACK)&&carries(c[0],"pin")&&is(l,b,LastCrossingSite.PIN_RACK,Kind.RACK,0),"the pin taken off the rack");
                f.at(c[2],P,25.5,0,-61.5);h.assertTrue(use(f,c[2],LastCrossingSite.PIN)&&is(l,b,LastCrossingSite.PIN,Kind.PIN,2),"nothing to stage without it");
                f.at(c[0],P,25.5,0,-61.5);select(c[0],"pin");
                h.assertTrue(use(f,c[0],LastCrossingSite.PIN)&&is(l,b,LastCrossingSite.PIN,Kind.PIN,0)&&LastCrossing.flag(b,"pinStaged")&&!carries(c[0],"pin"),"staged in its sleeve");
                h.assertTrue(f.own(c[0],P).getBoolean("StagedPin")&&f.own(c[0],P).getBoolean("PinHand"),"theirs to seat tonight");
                // Every roller seated, the last one the rise jumped among them.
                f.at(c[1],P,46.5,0,-60.5);
                for(var at:LastCrossingSite.ROLLERS)if(is(l,b,at,Kind.ROLLER,1))h.assertTrue(use(f,c[1],at)&&is(l,b,at,Kind.ROLLER,0),"a roller seated with one heave");
                h.assertTrue(LastCrossing.flag(b,"roller")&&f.own(c[1],P).getBoolean("SeatedLastRoller"),"every roller seated");
                // The wheel tried, and the rope claimed by the one who was below.
                f.at(c[2],P,26.5,-7,-59.5);h.assertTrue(use(f,c[2],LastCrossingSite.WHEEL)&&LastCrossing.flag(b,"wheelTested")&&f.own(c[2],P).getBoolean("TestedWheel"),"the wheel tried");
                f.at(c[3],P,34.5,0,-61.5);h.assertTrue(use(f,c[3],LastCrossingSite.RELEASE)&&Talk.choose(c[3],"I'll take"),"the rope claimed");})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"launchReady"),"the launch ready for dark"))
            .thenExecute(()->{h.assertTrue(f.own(c[3],P).getBoolean("LaunchHand")&&f.own(c[3],P).getBoolean("LaunchReady")&&f.own(c[0],P).getBoolean("LaunchReady"),"the rope's and the pin's hands know it");
                // Asked on the first day about Ellis: now whether she wants to cross.
                var vera=LastCrossing.vera(l,b);LastCrossing.talk(c[0],vera);
                h.assertTrue(Talk.choose(c[0],"Do you want")&&f.own(c[0],P).getBoolean("AskedCross"),"she puts the clipboard down");Talk.close(c[0]);
                int o=LastCrossing.offset(b);h.assertTrue(use(f,c[1],LastCrossingSite.CABLE_BRAKE)&&LastCrossing.offset(b)==o,"nothing to pull on today: the drum stays where the night left it");
                LastCrossing.skip(b,LastCrossing.DAY_TICKS*5/6);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"briefed3"),"before dark: where everyone stands, and the plate"))
            .thenExecute(()->{for(var p:c)h.assertTrue(f.own(p,P).getBoolean("HeardBrief"),"heard by "+name(p));LastCrossing.skip(b,LastCrossing.DAY_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.dayNo(b)==3,"the third dusk"))
            .thenExecute(()->{
                for(var p:c)h.assertTrue(f.own(p,P).getBoolean("DayThree"),"Day Three worked");
                LastCrossing.resetNow(l,b);
                h.assertTrue(is(l,b,LastCrossingSite.PIN,Kind.PIN,0)&&is(l,b,LastCrossingSite.PIN_RACK,Kind.RACK,0)&&is(l,b,LastCrossingSite.ROLLERS.get(3),Kind.ROLLER,0),"the station set back");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_gallery",timeoutTicks=20000)
    public static void dayThreeTheGalleryMadeReady(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        dayThree(f,h,c,"lc_gallery",()->{
                // The carrier tests at the plate on Day Two, and the second print shows.
                LastCrossing.carryForTest(c[0]);f.at(c[0],P,16.5,0,-44.5);
                h.assertTrue(use(f,c[0],LastCrossingSite.PLATE_HOME)&&Talk.choose(c[0],"Lay your palm")&&f.own(c[0],P).getBoolean("Exposed"),"exposed by the plate");},
            // Night Two's pull let go and never caught: it throws the rollers.
            s->s.thenExecute(()->LastCrossing.skip(b,LastCrossingNightTwo.PULL_BY))
                .thenWaitUntil(()->{clearUndertows(l,f);heal(c);h.assertTrue(LastCrossingNightTwo.pull(b)==LastCrossingNightTwo.CALLED,"the pull called");})
                .thenExecute(()->{f.at(c[1],P,48.5,0,-57.5);h.assertTrue(use(f,c[1],LastCrossingSite.CABLE_BRAKE),"the brake taken off");})
                .thenExecute(()->f.at(c[1],P,44.5,0,-63.5))
                .thenWaitUntil(()->{clearUndertows(l,f);heal(c);h.assertTrue(LastCrossing.flag(b,"pullFailed"),"run back until it threw the rollers");}))
            .thenExecute(()->{h.assertTrue(is(l,b,LastCrossingSite.LOOSE_ROLLER,Kind.ROLLER,1)&&is(l,b,LastCrossingSite.ROLLERS.get(2),Kind.ROLLER,1),"two rollers thrown");
                f.at(c[1],P,46.5,0,-60.5);h.assertTrue(use(f,c[1],LastCrossingSite.LOOSE_ROLLER)&&is(l,b,LastCrossingSite.LOOSE_ROLLER,Kind.ROLLER,1)&&f.own(c[1],P).getBoolean("HeavedRoller"),"a thrown roller doesn't seat with one heave");})
            .thenExecuteAfter(11,()->use(f,c[1],LastCrossingSite.LOOSE_ROLLER))
            .thenExecuteAfter(11,()->{use(f,c[1],LastCrossingSite.LOOSE_ROLLER);h.assertTrue(is(l,b,LastCrossingSite.LOOSE_ROLLER,Kind.ROLLER,0)&&f.own(c[1],P).getBoolean("ResetRoller"),"three heaves reset it");})
            // The exposed carrier hears the offer under their own breath, and knows what the master bypass will do at high water.
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("OfferedBreath"),"the Other Breath offered to the exposed carrier"))
            .thenExecute(()->{h.assertTrue(!f.own(c[1],P).getBoolean("OfferedBreath"),"to nobody else");
                f.at(c[0],P,26.5,-7,-59.5);h.assertTrue(use(f,c[0],LastCrossingSite.MASTER)&&f.own(c[0],P).getBoolean("KnowsMaster"),"the carrier knows the master will turn at high water");
                f.at(c[1],P,26.5,-7,-60.5);h.assertTrue(use(f,c[1],LastCrossingSite.MASTER)&&f.own(c[1],P).getBoolean("SawMaster")&&!f.own(c[1],P).getBoolean("KnowsMaster"),"to anyone else it's rusted fast");
                h.assertTrue(is(l,b,LastCrossingSite.MASTER,Kind.MASTER,0),"and it stays shut by day");
                // The feed traced from the gauge down; the rusted door gives only once it's followed there.
                f.at(c[2],P,22.5,0,-56.5);h.assertTrue(use(f,c[2],LastCrossingSite.FEED_GAUGE)&&is(l,b,LastCrossingSite.FEED_GAUGE,Kind.FEED,1)&&LastCrossingDayThree.feed(b)==LastCrossingDayThree.FEED_GAUGE,"the gauge room's tag chalked");
                f.at(c[2],P,22.5,-6,-55.5);h.assertTrue(use(f,c[2],LastCrossingSite.GALLERY_DOOR)&&is(l,b,LastCrossingSite.GALLERY_DOOR,Kind.GALLERY_DOOR,0)&&!f.own(c[2],P).getBoolean("ShovedDoor"),"rusted shut before the feed leads to it");
                h.assertTrue(use(f,c[2],LastCrossingSite.FEED_INTAKE)&&(LastCrossingDayThree.feed(b)&LastCrossingDayThree.FEED_INTAKE)!=0,"followed down into the intake");
                h.assertTrue(use(f,c[2],LastCrossingSite.GALLERY_DOOR)&&f.own(c[2],P).getBoolean("ShovedDoor"),"a shoulder to it");
                f.at(c[3],P,22.5,-6,-54.5);h.assertTrue(use(f,c[3],LastCrossingSite.BAR_RACK)&&carries(c[3],"lockbar")&&is(l,b,LastCrossingSite.BAR_RACK,Kind.RACK,0),"the locking bar taken off its hooks");})
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.GALLERY_DOOR))
            .thenExecuteAfter(11,()->{use(f,c[2],LastCrossingSite.GALLERY_DOOR);
                h.assertTrue(LastCrossing.flag(b,"doorOpen")&&is(l,b,LastCrossingSite.GALLERY_DOOR,Kind.GALLERY_DOOR,1)&&f.own(c[2],P).getBoolean("OpenedGallery"),"ground open on the stair down");
                h.assertTrue(l.getBlockState(b.offset(27,-9,-55)).getBlock() instanceof net.minecraft.world.level.block.StairBlock&&l.getBlockState(b.offset(30,-11,-55)).isAir(),"the stair down to the gallery");
                // In the gallery: the feed along the roof, the bar staged at the gate, the source seen through the glass.
                f.at(c[2],P,34.5,-11,-55.5);h.assertTrue(use(f,c[2],LastCrossingSite.FEED_GALLERY)&&f.own(c[2],P).getBoolean("TracedFeed"),"traced all the way down");
                f.at(c[3],P,40.5,-11,-53.5);c[3].getInventory().selected=8;h.assertTrue(use(f,c[3],LastCrossingSite.FEED_GATE)&&!LastCrossing.flag(b,"barStaged"),"not without the bar in hand");
                select(c[3],"lockbar");h.assertTrue(use(f,c[3],LastCrossingSite.FEED_GATE)&&LastCrossing.flag(b,"barStaged")&&is(l,b,LastCrossingSite.FEED_GATE,Kind.FEED_GATE,1)&&!carries(c[3],"lockbar"),"the bar staged at the gate");
                h.assertTrue(l.getBlockState(b.offset(LastCrossingSite.SOURCE).east()).is(Blocks.WATER)&&l.getBlockState(b.offset(40,-11,-53)).isAir(),"the chamber flooded behind the gate, the gallery dry");
                f.at(c[3],P,40.5,-11,-51.5);look(f,c[3],LastCrossingSite.SOURCE_SEEN);
                f.at(c[2],P,33.5,-11,-49.5);h.assertTrue(use(f,c[2],LastCrossingSite.DEBRIS.get(0))&&f.own(c[2],P).getBoolean("WorkedDebris"),"the first heap worked");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[3],P).getBoolean("SawSource"),"the source seen through the glass"))
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.DEBRIS.get(0)))
            .thenExecuteAfter(11,()->{use(f,c[2],LastCrossingSite.DEBRIS.get(0));h.assertTrue(is(l,b,LastCrossingSite.DEBRIS.get(0),Kind.DEBRIS,1),"half cleared");})
            .thenExecuteAfter(11,()->{use(f,c[2],LastCrossingSite.DEBRIS.get(0));h.assertTrue(l.getBlockState(b.offset(LastCrossingSite.DEBRIS.get(0))).isAir()&&!LastCrossing.flag(b,"pathCleared"),"one heap cleared, one to go");
                f.at(c[2],P,33.5,-11,-48.5);use(f,c[2],LastCrossingSite.DEBRIS.get(1));})
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.DEBRIS.get(1)))
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.DEBRIS.get(1)))
            .thenExecuteAfter(11,()->{use(f,c[2],LastCrossingSite.DEBRIS.get(1));h.assertTrue(LastCrossing.flag(b,"pathCleared")&&f.own(c[2],P).getBoolean("ClearedPath"),"the passage clear to the cutoff");
                f.at(c[2],P,33.5,-11,-44.5);h.assertTrue(use(f,c[2],LastCrossingSite.CUTOFF)&&f.own(c[2],P).getBoolean("ReachedCutoff")&&is(l,b,LastCrossingSite.CUTOFF,Kind.CUTOFF,0),"the cutoff reached, and it won't turn by day");})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"galleryReady"),"the gallery ready for dark"))
            .thenExecute(()->{h.assertTrue(f.own(c[2],P).getBoolean("GalleryReady")&&f.own(c[3],P).getBoolean("GalleryReady"),"and those down there know it");
                h.assertTrue(f.own(c[2],P).getString("Jobs").contains("source"),"the source was their job");
                LastCrossing.resetNow(l,b);
                h.assertTrue(is(l,b,LastCrossingSite.GALLERY_DOOR,Kind.GALLERY_DOOR,0)&&is(l,b,LastCrossingSite.FEED_GATE,Kind.FEED_GATE,0)&&is(l,b,LastCrossingSite.DEBRIS.get(1),Kind.DEBRIS,0)
                    &&is(l,b,LastCrossingSite.BAR_RACK,Kind.RACK,2)&&is(l,b,LastCrossingSite.FEED_GAUGE,Kind.FEED,0),"the station set back");
                h.succeed();});
    }

    // ------------------------------------------------------------------------------------------------ Night Three (0.4.356)
    /** On to the third dusk from Day Three. */
    private static GameTestSequence toNightThree(GameTestSequence s,LiteraryTests.Fixture f,GameTestHelper h){
        var b=f.base(P);
        return s.thenExecute(()->LastCrossing.skip(b,LastCrossing.DAY_TICKS))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.phase(b)==LastCrossing.DUSK&&LastCrossing.dayNo(b)==3,"the third dusk"));
    }
    private static boolean farBank(LiteraryTests.Fixture f,ServerPlayer p){return LastCrossingSite.farBank(rel(f,p));}
    /** 0.4.357: the pieces of the nameplate a reader carries, the account in their hands, and its words. */
    private static List<CompoundTag> pieces(ServerPlayer p){var out=new ArrayList<CompoundTag>();var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var t=LastCrossingNameplate.data(inv.getItem(i));if(t!=null)out.add(t);}return out;}
    private static int spares(ServerPlayer p){int n=0;for(var t:pieces(p))if(t.getBoolean("Spare"))n++;return n;}
    private static void selectPiece(ServerPlayer p,boolean spare){for(int i=0;i<9;i++){var t=LastCrossingNameplate.data(p.getInventory().getItem(i));if(t!=null&&t.getBoolean("Spare")==spare){p.getInventory().selected=i;return;}}}
    private static ItemStack account(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);var c=s.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null&&c.title().raw().equals(LastCrossingTexts.TITLE))return s;}return ItemStack.EMPTY;}
    private static String text(ItemStack book){var c=book.get(DataComponents.WRITTEN_BOOK_CONTENT);return c==null?"":c.pages().stream().map(p->p.raw().getString()).reduce("",(a,b)->a+" "+b).replaceAll("\\s+"," ");}
    private static String joined(ServerPlayer... crew){var all=new ArrayList<CompoundTag>();for(var p:crew)for(var t:pieces(p))if(!t.getBoolean("Spare"))all.add(t);all.sort(Comparator.comparingInt(t->t.getInt("Piece")));var out=new StringBuilder();for(var t:all)out.append(t.getString("Letters"));return out.toString();}
    private static boolean resolved(LiteraryTests.Fixture f,ServerLevel l,ServerPlayer p){return f.own(p,P).getBoolean("Completed")&&WitnessAccount.has(LabyrinthData.get(l.getServer()),p.getUUID(),WitnessAccount.Story.LAST_CROSSING)&&!account(p).isEmpty();}
    private static void hereFar(ServerPlayer... crew){for(var p:crew)if(Talk.replies(p).contains("Here."))Talk.choose(p,"Here");}
    private static void expose(LiteraryTests.Fixture f,GameTestHelper h,ServerPlayer p){
        LastCrossing.carryForTest(p);f.at(p,P,16.5,0,-44.5);
        h.assertTrue(use(f,p,LastCrossingSite.PLATE_HOME)&&Talk.choose(p,"Lay your palm")&&f.own(p,P).getBoolean("Exposed"),"exposed at the plate");
    }

    @GameTest(template="empty",batch="last_crossing_ferry",timeoutTicks=20000)
    public static void nightThreeTheFerryCrosses(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        toNightThree(dayThree(f,h,c,"lc_ferry",()->{},s->s)
            .thenExecute(()->{f.at(c[0],P,11.5,0,-36.5);h.assertTrue(use(f,c[0],LastCrossingSite.PIN_RACK),"the pin off the porch rack");
                f.at(c[0],P,25.5,0,-61.5);select(c[0],"pin");h.assertTrue(use(f,c[0],LastCrossingSite.PIN)&&LastCrossing.flag(b,"pinStaged"),"staged by day");}),f,h)
            .thenExecute(()->{h.assertTrue(!LastCrossing.flag(b,"pin")&&is(l,b,LastCrossingSite.PIN,Kind.PIN,0),"the pin up in its sleeve at dark");
                LastCrossing.skip(b,LastCrossingNightThree.RISE_AT);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"risen")&&l.getBlockState(b.offset(26,-5,-60)).is(Blocks.WATER),"the final rise fills the pit"))
            .thenExecute(()->{
                // Too soon: the rope won't let her go on her sill.
                f.at(c[3],P,34.5,0,-64.5);h.assertTrue(use(f,c[3],LastCrossingSite.RELEASE)&&!LastCrossing.flag(b,"released"),"not on her sill");
                // The wheel held at the bottom of the flooded pit while the pin drops from the deck.
                f.at(c[1],P,26.5,-7,-59.5);h.assertTrue(use(f,c[1],LastCrossingSite.WHEEL),"the wheel held");
                f.at(c[0],P,25.5,0,-61.5);h.assertTrue(use(f,c[0],LastCrossingSite.PIN)&&LastCrossing.flag(b,"pin"),"the pin dropped");})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"pinned3"),"pinned for the night"))
            .thenExecute(()->{h.assertTrue(f.own(c[0],P).getBoolean("SeatedLaunchPin")&&f.own(c[1],P).getBoolean("HeldWheelToPin"),"who held and who pinned");
                LastCrossingNightThree.hurry(b,LastCrossingNightThree.SILL_CLEAR);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"floating")&&LastCrossing.flag(b,"pin"),"off her sill, the pin still in"))
            .thenExecute(()->{f.at(c[0],P,35.5,0,-67.5);f.at(c[2],P,31.5,0,-67.5);
                h.assertTrue(use(f,c[3],LastCrossingSite.RELEASE)&&LastCrossing.flag(b,"released"),"let go");
                var aboard=LastCrossingNightThree.aboard(b);
                h.assertTrue(aboard.contains(c[0].getUUID())&&aboard.contains(c[2].getUUID())&&aboard.contains(c[3].getUUID())&&!aboard.contains(c[1].getUUID()),"three aboard, one left in the pit");
                h.assertTrue(f.own(c[1],P).getBoolean("LeftOnStation")&&f.own(c[3],P).getBoolean("PulledRelease")&&is(l,b,LastCrossingSite.RELEASE,Kind.RELEASE,1),"remembered");
                LastCrossingNightThree.hurry(b,LastCrossingNightThree.TRANSIT);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"arrived")&&LastCrossingNightThree.ending(b)==LastCrossingNightThree.FERRY,"across: the crew's crossing"))
            .thenExecute(()->{h.assertTrue(farBank(f,c[0])&&farBank(f,c[3])&&!farBank(f,c[1]),"those aboard on the far landing");
                h.assertTrue(f.own(c[2],P).getBoolean("CrossedOver")&&f.own(c[0],P).getString("Ending").equals("ferry"),"crossed, and the ending kept");})
            .thenWaitUntil(()->{hereFar(c);h.assertTrue(LastCrossing.flag(b,"broken"),"the roll on the far bank, then the nameplate broken");})
            .thenExecute(()->{
                for(var p:c)h.assertTrue(pieces(p).size()==1&&!pieces(p).get(0).getBoolean("Spare"),"a piece for every name on the roll: "+name(p));
                h.assertTrue(joined(c).equals(LastCrossingNameplate.NAME)&&f.own(c[1],P).getBoolean("GotPiece"),"the pieces spell her name, the one left on the station's among them");
                h.assertTrue(is(l,b,new BlockPos(32,2,-69),Kind.NAMEPLATE,2)&&is(l,b,new BlockPos(33,2,-69),Kind.NAMEPLATE,3),"bolt holes where the nameplate was");
                var offered=Talk.replies(c[0]);h.assertTrue(!offered.isEmpty()&&offered.stream().allMatch(x->x.endsWith(".")),"who takes the spare: "+offered);
                String pick=offered.get(offered.size()-1);for(var p:c)if(Talk.replies(p).contains(pick))Talk.choose(p,pick);
                h.assertTrue(LastCrossing.flag(b,"spareSettled"),"everyone standing has said");
                ServerPlayer holder=null;for(var p:c)if(spares(p)==1)holder=p;
                h.assertTrue(holder!=null&&(name(holder)+".").equals(pick)&&f.own(holder,P).getBoolean("NamedForSpare"),"the spare to the one they named");
                LastCrossingNightThree.hurry(b,LastCrossingNightThree.BEAT);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"ended"),"then morning"))
            .thenExecute(()->{h.assertTrue(LastCrossing.phase(b)==LastCrossing.END,"morning");
                for(var p:c)h.assertTrue(resolved(f,l,p),"the crossing resolved, and an account: "+name(p));
                h.assertTrue(text(account(c[1])).contains("moved off the landing without me")&&text(account(c[0])).contains("Mine says ")&&text(account(c[0])).contains("something at the far end of the boat said Here"),"each account from its own record");
                selectPiece(c[0],false);LastCrossingNameplate.tap(c[0],c[0].getMainHandItem());
                h.assertTrue(f.own(c[0],P).getBoolean("PieceAnswered")&&f.own(c[2],P).getBoolean("HeardPieceKnock"),"a tap, and an answering knock");
                h.assertTrue(!LastCrossing.dealable(LabyrinthData.get(l.getServer()),c[0].getUUID())&&LastCrossing.crewAround(c[0].getUUID())>=LastCrossing.MIN_CREW,"not dealt again to one who has crossed; a crew stands together");
                h.assertTrue(f.own(c[0],P).getBoolean("AnsweredFar")&&f.own(c[1],P).getBoolean("UnansweredFar")&&f.own(c[0],P).getString("HeardUnanswered").contains(name(c[1])),"a name unanswered, heard by those who answered");
                h.assertTrue(f.own(c[0],P).getBoolean("HeardEllisLate")&&!f.own(c[0],P).getBoolean("HeardEllisFar"),"a late answer for Ellis from the far end of the boat");
                h.assertTrue(farBank(f,c[1])&&f.own(c[1],P).getBoolean("BroughtAcross"),"the one left behind brought over in the morning");
                for(var p:c)h.assertTrue(f.own(p,P).getBoolean("NightThree"),"Night Three, for "+name(p));
                LastCrossing.resetNow(l,b);
                h.assertTrue(is(l,b,LastCrossingSite.RELEASE,Kind.RELEASE,0)&&!l.getBlockState(b.offset(26,-5,-60)).is(Blocks.WATER)&&is(l,b,new BlockPos(32,2,-69),Kind.NAMEPLATE,0),"the station set back");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_cleared",timeoutTicks=20000)
    public static void nightThreeTheStationCleared(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        var chamber=new AABB(b.getX()+40,b.getY()-12,b.getZ()-58,b.getX()+48,b.getY()-6,b.getZ()-48);
        toNightThree(dayThree(f,h,c,"lc_cleared",()->LastCrossing.carryForTest(c[0]),s->s)
            .thenExecute(()->{f.at(c[2],P,22.5,0,-56.5);use(f,c[2],LastCrossingSite.FEED_GAUGE);f.at(c[2],P,22.5,-6,-55.5);use(f,c[2],LastCrossingSite.FEED_INTAKE);use(f,c[2],LastCrossingSite.GALLERY_DOOR);
                f.at(c[3],P,22.5,-6,-54.5);h.assertTrue(use(f,c[3],LastCrossingSite.BAR_RACK),"the bar taken");})
            .thenExecuteAfter(11,()->use(f,c[2],LastCrossingSite.GALLERY_DOOR))
            .thenExecuteAfter(11,()->{use(f,c[2],LastCrossingSite.GALLERY_DOOR);f.at(c[3],P,40.5,-11,-53.5);select(c[3],"lockbar");
                h.assertTrue(LastCrossing.flag(b,"doorOpen")&&use(f,c[3],LastCrossingSite.FEED_GATE)&&LastCrossing.flag(b,"barStaged"),"the door open and the bar staged by day");}),f,h)
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.turned(b).contains(c[0].getUUID()),"the carrier turns"))
            .thenExecute(()->{
                // One holds the gate, another drops the bar; then the cutoff.
                f.at(c[2],P,33.5,-11,-44.5);h.assertTrue(use(f,c[2],LastCrossingSite.CUTOFF)&&!LastCrossing.flag(b,"cutoff"),"the cutoff won't turn with the gate unbarred");
                f.at(c[3],P,40.5,-11,-53.5);h.assertTrue(use(f,c[3],LastCrossingSite.FEED_GATE)&&f.own(c[3],P).getBoolean("HeldFeedGate"),"the gate held");
                f.at(c[2],P,40.5,-11,-52.5);h.assertTrue(use(f,c[2],LastCrossingSite.FEED_GATE)&&LastCrossing.flag(b,"gateBarred")&&is(l,b,LastCrossingSite.FEED_GATE,Kind.FEED_GATE,2),"the bar dropped across");
                h.assertTrue(f.own(c[3],P).getBoolean("HeldGateToBar")&&f.own(c[2],P).getString("BarredWith").contains(name(c[3])),"together");
                f.at(c[2],P,33.5,-11,-44.5);h.assertTrue(use(f,c[2],LastCrossingSite.CUTOFF)&&LastCrossing.flag(b,"cutoff"),"the cutoff wound shut");
                LastCrossingNightThree.hurry(b,LastCrossingNightThree.DRAIN);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"drained")&&LastCrossingNightThree.defenders(b)>0,"the chamber down, and things on the source"))
            .thenExecute(()->{h.assertTrue(is(l,b,LastCrossingSite.SOURCE,Kind.SOURCE,1)&&l.getBlockState(b.offset(LastCrossingSite.FEED_GATE)).isAir()&&l.getBlockState(b.offset(LastCrossingSite.SOURCE).east()).isAir(),"the source bare, the gate drawn up, the water gone");
                f.at(c[2],P,42.5,-11,-53.5);h.assertTrue(LastCrossingNightThree.strike(c[2])&&!f.own(c[2],P).getBoolean("StruckSource"),"nothing lands while they cover it");
                for(var u:l.getEntitiesOfClass(Undertow.class,chamber))if(u.getTags().contains("crossing_defender"))u.kill();})
            .thenWaitUntil(()->{LastCrossingNightThree.strike(c[2]);h.assertTrue(f.own(c[2],P).getBoolean("StruckSource"),"fought off it, the first blow lands");})
            .thenExecuteAfter(11,()->LastCrossingNightThree.strike(c[2])).thenExecuteAfter(11,()->LastCrossingNightThree.strike(c[2]))
            .thenExecuteAfter(11,()->LastCrossingNightThree.strike(c[2])).thenExecuteAfter(11,()->LastCrossingNightThree.strike(c[2]))
            .thenExecuteAfter(11,()->LastCrossingNightThree.strike(c[2]))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"destroyed")&&LastCrossingNightThree.ending(b)==LastCrossingNightThree.GALLERY,"the source dead, the station cleared"))
            .thenExecute(()->{h.assertTrue(LastCrossingNightTwo.turned(b).isEmpty()&&f.own(c[0],P).getBoolean("ReleasedByFight"),"the turned let go at once");
                h.assertTrue(f.own(c[2],P).getBoolean("DestroyedSource")&&f.own(c[3],P).getBoolean("SawSourceDie"),"who killed it, who saw");
                LastCrossing.bell(c[1]);h.assertTrue(LastCrossing.flag(b,"mustered")&&f.own(c[1],P).getBoolean("RangMuster"),"the muster bell");
                h.assertTrue(f.own(c[2],P).getBoolean("OutOfGallery")&&f.own(c[3],P).getBoolean("OutOfGallery")&&!f.own(c[1],P).getBoolean("OutOfGallery"),"those who came out of the gallery");
                for(var p:c)h.assertTrue(pieces(p).size()==1,"a piece each at the muster: "+name(p));
                h.assertTrue(!Talk.replies(c[1]).contains(name(c[0])+".")&&Talk.replies(c[1]).contains(name(c[2])+"."),"the spare is for one who came through clean");
                String pick=name(c[2])+".";for(var p:c)if(Talk.replies(p).contains(pick))Talk.choose(p,pick);
                h.assertTrue(LastCrossing.flag(b,"spareSettled")&&spares(c[2])==1,"named, the spare to them");
                // Handed on to the one who turned, whose own piece pairs with it.
                selectPiece(c[2],true);h.assertTrue(LastCrossingNameplate.hand(c[2],c[0],InteractionHand.MAIN_HAND)&&spares(c[0])==1&&spares(c[2])==0&&pieces(c[0]).size()==2,"the spare handed to the one who turned");
                h.assertTrue(f.own(c[2],P).getString("GaveSpareTo").equals(name(c[0]))&&f.own(c[0],P).getString("SpareFrom").equals(name(c[2])),"and who gave it to whom");
                selectPiece(c[3],false);h.assertTrue(!LastCrossingNameplate.hand(c[3],c[1],InteractionHand.MAIN_HAND)&&pieces(c[1]).size()==1,"a main piece isn't handed over");
                LastCrossingNightThree.hurry(b,LastCrossingNightThree.BEAT);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"ended")&&LastCrossing.phase(b)==LastCrossing.END,"morning"))
            .thenExecute(()->{for(var p:c)h.assertTrue(farBank(f,p)&&f.own(p,P).getString("Ending").equals("gallery")&&resolved(f,l,p),"across in the morning, and resolved: "+name(p));
                h.assertTrue(text(account(c[0])).contains("gave me the spare")&&text(account(c[2])).contains("I gave the spare to "+name(c[0]))&&text(account(c[2])).contains("I struck the source until it was dead"),"the accounts say it");
                LastCrossing.resetNow(l,b);
                h.assertTrue(is(l,b,LastCrossingSite.SOURCE,Kind.SOURCE,0)&&is(l,b,LastCrossingSite.FEED_GATE,Kind.FEED_GATE,0)&&l.getBlockState(b.offset(LastCrossingSite.SOURCE).east()).is(Blocks.WATER),"the chamber full again");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_breath",timeoutTicks=20000)
    public static void nightThreeTheOtherBreath(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        toNightThree(dayThree(f,h,c,"lc_breath",()->expose(f,h,c[0]),s->s)
            .thenWaitUntil(()->h.assertTrue(f.own(c[0],P).getBoolean("OfferedBreath"),"offered on Day Three")),f,h)
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.turned(b).contains(c[0].getUUID()),"the carrier turns"))
            .thenExecute(()->{f.at(c[0],P,25.5,-7,-60.5);h.assertTrue(use(f,c[0],LastCrossingSite.MASTER)&&!LastCrossing.flag(b,"masterOpen"),"before the rise it won't turn");
                LastCrossing.skip(b,LastCrossingNightThree.RISE_AT);})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"risen"),"the rise"))
            .thenExecute(()->{f.at(c[1],P,26.5,-7,-60.5);h.assertTrue(use(f,c[1],LastCrossingSite.MASTER)&&!LastCrossing.flag(b,"masterOpen"),"it won't turn for a clean hand");
                look(f,c[1],Vec3.atCenterOf(LastCrossingSite.MASTER));f.at(c[2],P,30.5,0,-57.5);
                h.assertTrue(use(f,c[0],LastCrossingSite.MASTER)&&LastCrossing.flag(b,"masterOpen")&&is(l,b,LastCrossingSite.MASTER,Kind.MASTER,1),"open at high water");
                h.assertTrue(f.own(c[1],P).getString("SawMasterOpened").contains(name(c[0])),"and seen opened");})
            .thenWaitUntil(()->h.assertTrue(f.own(c[2],P).getBoolean("HeardHammer"),"the pipes hammer at the ramp"))
            .thenExecute(()->LastCrossingNightThree.hurry(b,LastCrossingNightThree.BARGAIN_HOLD))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"bargained"),"held open till the water reached the approach"))
            .thenExecute(()->{h.assertTrue(Math.abs(LastCrossingNightThree.breath(c[0])-.2)<1e-6&&f.own(c[0],P).getBoolean("OtherBreath")&&f.own(c[0],P).getBoolean("SidedWithSource"),"the Other Breath taken");
                h.assertTrue(LastCrossingNightThree.breath(c[1])==0,"nobody else's");
                h.assertTrue(l.getBlockState(b.offset(LastCrossingSite.APPROACH.get(1))).is(Blocks.WATER),"water over the ramp's approach");
                // Shut too late: the landing drained, the change kept.
                f.at(c[3],P,53.5,0,-58.5);h.assertTrue(use(f,c[3],LastCrossingSite.DOWNSTREAM)&&f.own(c[3],P).getBoolean("DrainedLanding")&&!f.own(c[3],P).getBoolean("DeniedBargain"),"the downstream sluice shut after");})
            .thenWaitUntil(()->h.assertTrue(!l.getBlockState(b.offset(LastCrossingSite.APPROACH.get(1))).is(Blocks.WATER),"the approach drained"))
            .thenExecute(()->{LastCrossing.resetNow(l,b);
                h.assertTrue(Math.abs(LastCrossingNightThree.breath(c[0])-.2)<1e-6,"kept after the station is set back");
                h.succeed();});
    }

    @GameTest(template="empty",batch="last_crossing_taken",timeoutTicks=20000)
    public static void nightThreeTheSourceTakesIt(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        ServerPlayer[] c=new ServerPlayer[4];
        toNightThree(dayThree(f,h,c,"lc_taken",()->expose(f,h,c[0]),s->s),f,h)
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightTwo.turned(b).contains(c[0].getUUID()),"the carrier turns"))
            .thenExecute(()->LastCrossing.skip(b,LastCrossingNightThree.RISE_AT))
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"risen"),"the rise"))
            .thenExecute(()->{f.at(c[0],P,25.5,-7,-60.5);h.assertTrue(use(f,c[0],LastCrossingSite.MASTER)&&LastCrossing.flag(b,"masterOpen"),"opened");
                // Shut in time: denied.
                f.at(c[3],P,53.5,0,-58.5);h.assertTrue(use(f,c[3],LastCrossingSite.DOWNSTREAM)&&LastCrossing.flag(b,"denied")&&!LastCrossing.flag(b,"masterOpen"),"the downstream sluice shut in time");
                h.assertTrue(f.own(c[0],P).getBoolean("BargainDenied")&&f.own(c[0],P).getString("DeniedBy").contains(name(c[3]))&&LastCrossingNightThree.breath(c[0])==0,"denied, and by whom");
                h.assertTrue(use(f,c[0],LastCrossingSite.MASTER)&&!LastCrossing.flag(b,"masterOpen"),"it won't open again");
                // The night runs out with neither the ferry across nor the source dead.
                LastCrossing.skip(b,LastCrossingNightThree.NIGHT3_TICKS);})
            .thenWaitUntil(()->h.assertTrue(LastCrossingNightThree.ending(b)==LastCrossingNightThree.SOURCE,"the source takes the crossing"))
            .thenExecute(()->{for(var p:c)h.assertTrue(farBank(f,p),"everyone at the far landing: "+name(p));})
            .thenWaitUntil(()->h.assertTrue(LastCrossing.flag(b,"ended")&&LastCrossing.phase(b)==LastCrossing.END,"every name answered, then morning"))
            .thenExecute(()->{for(var p:c){var own=f.own(c[1],P);h.assertTrue(own.getString("HeardAnsweredFor").contains(name(p)),"an answer for "+name(p));}
                h.assertTrue(f.own(c[1],P).getBoolean("HeardEllisFar")&&f.own(c[2],P).getString("Ending").equals("source"),"Ellis answered too; the ending kept");
                for(var p:c)h.assertTrue(resolved(f,l,p)&&pieces(p).isEmpty(),"resolved, and no pieces when the source has it: "+name(p));
                h.assertTrue(!LastCrossing.flag(b,"broken")&&text(account(c[1])).contains("every name got an answer"),"the nameplate whole, and the account says how it ended");
                h.assertTrue(LastCrossingNightTwo.turned(b).isEmpty(),"the turned themselves again in the morning");
                h.succeed();});
    }

    // ------------------------------------------------------------------------------------------------ 0.4.551, a crossing of their own
    /**
     * The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full". A crossing holds six: its
     * crew on the roll, whether or not they are on it just now, and anybody else at it. Latecomers come in partway while there is a
     * place; a seventh waits while a crossing of their own is made ready, built as the first is, and goes into it; one of the crew who
     * steps out finds their place kept.
     */
    @GameTest(template="empty",batch="last_crossing_copy",timeoutTicks=24000)
    public static void aSeventhReaderIsGivenACrossingOfTheirOwn(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);var c=new ServerPlayer[4];final ServerPlayer[] late=new ServerPlayer[3];final BlockPos[] theirs={null};var server=l.getServer();
        java.util.function.Consumer<ServerPlayer> house=p->{p.stopRiding();p.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY()+2,f.origin.getZ()+.5,0,0);p.setNoGravity(true);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);};
        SceneInstances.fixtureCap(1);
        crew(f,h,c,"lc_copy")
            .thenExecute(()->{
                h.assertTrue(SceneInstances.copyable(P)&&SceneInstances.party(P)==LastCrossing.MAX_CREW&&LastCrossing.MAX_CREW==6,"a crossing has copies, six places to a crossing");
                for(int i=0;i<late.length;i++){late[i]=NativeTestPlayers.survival(h,"lc_copy_late"+i);f.players.add(late[i]);house.accept(late[i]);}
                // Four on the roll, the day begun: two places left. A latecomer goes in partway, as one always has.
                var first=SceneInstances.enter(late[0],P);
                h.assertTrue(first.door()!=null&&first.copy()==0&&SceneInstances.held(server,P,0,late[0].getUUID())==4,"a latecomer goes into the crossing that has room: "+first);
                h.assertTrue(SceneInstances.copies(f.data(),P)==1,"and with one place left after them, the next crossing is made ready ahead");
                f.arrive(late[0],P);
                h.assertTrue(f.own(late[0],P).getBoolean("Partway"),"they come in after the roll has been called");
                var second=SceneInstances.enter(late[1],P);
                h.assertTrue(second.door()!=null&&second.copy()==0,"so does another, to the last place: "+second);
                f.arrive(late[1],P);
                // Six places held: the crossing is full.
                var full=SceneInstances.enter(late[2],P);
                h.assertTrue(SceneInstances.held(server,P,0,late[2].getUUID())==6&&full.door()==null&&full.waiting(),"a seventh waits while a crossing of their own is made ready: "+full);
                // One of the crew steps out to the House: their place is kept for them.
                house.accept(c[0]);
                h.assertTrue(SceneInstances.held(server,P,0,late[2].getUUID())==6&&SceneInstances.enter(late[2],P).door()==null,"a place on the roll is held while its reader is out");
                var back=SceneInstances.enter(c[0],P);
                h.assertTrue(back.door()!=null&&back.copy()==0,"and is theirs when they come back: "+back);
                f.arrive(c[0],P);
            })
            .thenWaitUntil(()->h.assertTrue(SceneInstances.built(f.data(),P,1)&&!SceneInstances.building(),"waiting for the second crossing to stand"))
            .thenExecute(()->{
                theirs[0]=SceneInstances.base(server,P,1);
                h.assertTrue(theirs[0].getX()<=b.getX()-SceneInstances.WEST&&theirs[0].getY()==b.getY()&&theirs[0].getZ()==b.getZ(),"it stands a million blocks west of the first: "+theirs[0]);
                var e=SceneInstances.enter(late[2],P);
                h.assertTrue(e.door()!=null&&e.copy()==1&&e.door().id.equals(SceneInstances.doorId(P,1,"entry"))&&SceneInstances.kept(f.data(),late[2].getUUID(),P)==1,"the seventh goes into it by its own door: "+e);
                h.assertTrue(LastCrossing.phase(theirs[0])<0&&LastCrossing.places(theirs[0]).isEmpty()&&SceneInstances.held(server,P,1,late[2].getUUID())==0,"nothing has begun there: it is a crossing of its own");
                // Its station stands as the first's does: the porch, the clipboard on it.
                var by=theirs[0].subtract(b);f.chunks.hold(l,box(f).move(by.getX(),by.getY(),by.getZ()));
            })
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the second station is resident"))
            .thenExecute(()->{
                // The first crossing's roll is called (its clipboard has the names on it); the second's is not yet.
                var by=theirs[0].subtract(b);var at=b.offset(LastCrossingSite.CLIPBOARD);
                h.assertTrue(l.getBlockState(at).equals(CrossingBlock.of(Kind.CLIPBOARD,Direction.SOUTH,1)),"the first crossing's roll is on its clipboard: "+l.getBlockState(at));
                h.assertTrue(l.getBlockState(at.offset(by)).equals(CrossingBlock.of(Kind.CLIPBOARD,Direction.SOUTH)),"the second station has its own clipboard on its porch, its roll not yet called: "+l.getBlockState(at.offset(by)));
            }).thenSucceed();
    }
    @AfterBatch(batch="last_crossing_copy") public static void copyDone(ServerLevel l){SceneInstances.fixtureCap(null);SceneInstances.clear();close();}
}
