package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/** The finished production camper, including the exterior pass omitted by the original route fixtures. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_goatman_windows")
@PrefixGameTestTemplate(false)
public final class GoatmanExteriorTests {
    private static GoatmanTests.Fixture finished;
    private static GoatmanTests.Fixture porch;
    @AfterBatch(batch="goat_post_knock_steps04114")
    public static void cleanPorch(ServerLevel level){if(porch!=null){porch.close();porch=null;}}

    @GameTest(template="empty",templateNamespace=TheOldestHouse.MOD_ID+"_goatman_entry",batch="goat_post_knock_steps04114",timeoutTicks=600)
    public static void theOriginalChildLeavesTheFinishedPorchAfterItsLastKnocksWithoutStickingOnEitherStep(GameTestHelper h){
        porch=new GoatmanTests.Fixture(h,54100);var f=porch;f.finishedCamper();
        f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        var route=new CompoundTag();route.putBoolean("StalkStarted0465",true);route.putInt("StalkLeg0465",18);
        route.putInt("CircuitGoal0494",GoatmanStalking.CIRCUITS);route.putInt("Circuits0494",GoatmanStalking.CIRCUITS-1);
        route.putInt("WindowSerial0497",1);route.putBoolean("AssaultDone0465",true);route.putInt("DemandStage0465",7);
        GoatmanChild[] original={null};UUID[] id={null};int[] elapsed={0};boolean[] restored={false};
        h.onEachTick(()->{
            if(original[0]==null)return;var c=original[0];
            GoatmanStalking.step(f.l,f.b,route,c,1900+elapsed[0]++);
            h.assertTrue(c.getUUID().equals(id[0])&&!c.noPhysics&&f.l.noCollision(c,c.getBoundingBox().deflate(1e-7)),"the child retains its original colliding body on both finished steps (native contact epsilon): "+f.rel(c));
            if(!restored[0]&&route.getBoolean("DoorPressureDone0497")){
                c.load(c.saveWithoutId(new CompoundTag()));restored[0]=true;
            }
            if(elapsed[0]==60)h.assertTrue(route.getInt("StalkLeg0465")>=19,"the original child clears the actual first tread within sixty ticks instead of walking into its wood: "+f.rel(c));
            if(elapsed[0]%60==0)TheOldestHouse.LOGGER.info("GOATMAN POST-KNOCK STEPS: ticks={} leg={} feet={}",elapsed[0],route.getInt("StalkWindowLeg0465"),f.rel(c));
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the finished porch and native collision sections are ready")).thenExecute(()->{
            var c=GoatmanRegistry.CHILD.get().create(f.l);h.assertTrue(c!=null,"the native original child can be created");c.appearance(4,0,null);c.disguise(4);
            // A full-height tread is valid saved/player-edited geometry. A hard-coded y=.5 aims inside it forever.
            f.l.setBlockAndUpdate(f.b.offset(0,0,-53),Blocks.OAK_PLANKS.defaultBlockState());
            c.moveTo(f.b.getX()+.5,f.b.getY(),f.b.getZ()-51.5,180,0);f.l.addFreshEntity(c);original[0]=c;id[0]=c.getUUID();
            GoatmanWoods.door(f.l,f.b,false);GoatmanWoods.setWindow(f.l,f.b,false);
        }).thenWaitUntil(()->h.assertTrue(GoatmanStalking.windowApproachDone(route),"the post-knock child descends both finished steps and reaches the bathroom window: leg="+route.getInt("StalkWindowLeg0465")+" feet="+(original[0]==null?"absent":f.rel(original[0])))).thenExecute(()->{
            h.assertTrue(restored[0]&&route.getInt("DoorPressureHits0497")==GoatmanWindowCircuit.DOOR_BLOWS&&f.rel(original[0]).distanceToSqr(new Vec3(10.25,0,-74.5))<.02,"the same saved original completes every front-step blow and the entire physical window approach");
            original[0].discard();
        }).thenSucceed();
    }
    @AfterBatch(batch="goat_finished_camper04112")
    public static void clean(ServerLevel level){if(finished!=null){finished.close();finished=null;}}

    @GameTest(template="empty",batch="goat_finished_camper04112",timeoutTicks=6500)
    public static void theFinishedCamperKeepsItsOriginalMovingFromEveningThroughDawnWithSimultaneousDoorPressure(GameTestHelper h){
        finished=new GoatmanTests.Fixture(h,53700);var f=finished;
        // Exactly the production construction order, before a visitor can defer exterior dressing.
        f.finishedCamper();
        var p=f.player();f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.GOATMAN));
        boolean[] begun={false},loaded={false},locked={false},overlap={false},lateOverlap={false},approached={false},childKnock={false};
        UUID[] original={null};int[] nightSerial={0},pulse={0},impact={0};
        var nightPanes=new HashSet<Long>();var children=new HashSet<Integer>();var forms=new HashSet<Integer>();
        h.onEachTick(()->{
            if(!begun[0])return;var r=f.run();if(r.getInt("Phase")==GoatmanVignette.DAWN)return;
            if(r.getBoolean("BedtimeCalled131")&&!p.isSleeping()){var head=GoatmanWoods.bunkHead(8);p.moveTo(f.b.getX()-3.5,f.b.getY()+1,f.b.getZ()+head.getZ()+.5);f.use(p,head);}
            var body=f.cousin(r.getInt("Wrong")).orElseThrow();
            h.assertTrue(body.getUUID().equals(original[0])&&!body.noPhysics,"the same original keeps native collision through every exterior route and save");
            // 0.4.123: the returning child is let in through the open door; within seconds the night locks it behind him.
            if(!locked[0]&&r.getInt("Phase")==GoatmanVignette.VIGIL){
                h.assertTrue(!GoatmanWoods.doorOpen(f.l,f.b)&&r.getInt("RunnerState")==GoatmanVignette.R_INSIDE&&r.getBoolean("Exchange0497"),"the cousin is in, the original went out past him, and the real door is shut at nightfall");locked[0]=true;
            }
            // A brisk evening has no separate lap: every sighting is the night circuit's, under the words at the door.
            int serial=r.getInt("WindowSerial0497");
            if(r.getInt("Phase")==GoatmanVignette.VIGIL&&serial>nightSerial[0]){
                nightSerial[0]=serial;var feet=f.rel(body);
                h.assertTrue(Math.abs(feet.y-1.5)<.11&&!f.l.noCollision(body,body.getBoundingBox().move(0,-.02,0)),"the original actually stands on the finished stone sill, above the tire: feet="+feet);
                nightPanes.add(r.getLong("LastImpactPos0465"));
                forms.add(r.getInt("WindowSkin0497"));if(r.getInt("WindowSkin0497")>=0){children.add(r.getInt("WindowSkin0497"));if(r.getInt("DemandStage0465")>0)childKnock[0]=true;}
                if(!loaded[0]){
                    var actor=body.saveWithoutId(new CompoundTag());body.load(actor);
                    var data=LabyrinthData.get(f.server);var restored=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),f.l.registryAccess()),f.l.registryAccess());
                    f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",restored);loaded[0]=true;
                }
            }
            if(r.getInt("Phase")==GoatmanVignette.VIGIL){
                boolean distant=body.distanceToSqr(Vec3.atCenterOf(f.b.offset(GoatmanWoods.DOOR)))>16;
                if(distant&&r.getLong("LastImpactPos0465")==f.b.offset(GoatmanWoods.DOOR).asLong()&&r.getInt("ImpactSerial0472")>impact[0])overlap[0]=true;
                impact[0]=r.getInt("ImpactSerial0472");
                // LATE_TRACE_BEGIN
                if(Boolean.getBoolean("the_oldest_house.traceLateKnock")&&r.getInt("DoorPulseSerial04112")>pulse[0])TheOldestHouse.LOGGER.info("LATE KNOCK OBSERVATION clock={} impactClock={} original={} feet={} doorDistanceSquared={} distant={} leg={} circuits={} approach={} pressureAt={} quiet={}",r.getInt("Clock"),r.getInt("ImpactClock0472"),body.getUUID(),f.rel(body),body.distanceToSqr(Vec3.atCenterOf(f.b.offset(GoatmanWoods.DOOR))),distant,r.getInt("StalkLeg0465"),r.getInt("Circuits0494"),r.getInt("StalkWindowLeg0465"),r.getInt("DoorPressureAt0497"),r.contains("BedQuietAt131"));
                // LATE_TRACE_END
                int next=r.getInt("DoorPulseSerial04112");if(next>pulse[0]&&distant)lateOverlap[0]=true;pulse[0]=next;
                approached[0]|=GoatmanStalking.circuitDone(r)&&GoatmanStalking.windowApproachDone(r);
            }
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"all finished exterior and original body sections are loaded")).thenExecute(()->{
            h.assertTrue(f.l.getBlockState(f.b.offset(-9,1,-59)).is(Blocks.SMOOTH_STONE_SLAB)
                    &&f.l.getBlockState(f.b.offset(-9,2,-58)).is(Blocks.IRON_BARS)
                    &&f.l.getBlockState(f.b.offset(5,0,-52)).is(Blocks.IRON_BARS),"the actual photographed sill, frame and porch rail are present");
            h.assertTrue(GoatmanVignette.enter(p),"the reader joins an ordinary fresh evening");
            var r=f.run();r.putInt("Wrong",0);r.putInt("Runner",7);r.putInt("Phase",GoatmanVignette.GATHERING);r.putInt("Clock",1000);
            r.putInt("SupperAt04123",1001-GoatmanVignette.TABLE_BACK);r.putBoolean("CalledIn04123",true);r.putBoolean("Shut04123",true);r.putBoolean("MealReady0465",true);
            r.putInt("ExtraState",GoatmanVignette.X_SUPPER);r.putInt("RunnerState",GoatmanVignette.R_AWAY);f.cohort(r,List.of(p),0);f.run(r);
            GoatmanVignette.stage(f.l,f.b,r);GoatmanVignette.hurry(f.l,f.b);GoatmanWoods.door(f.l,f.b,true);GoatmanWoods.setWindow(f.l,f.b,true);
            // FIXTURE_SEED_BEGIN
            // The impossible late knock is tested against one reproducible native route;
            // random lap pauses must not decide whether its only post-demand pulse lands at the step.
            f.cousin(0).orElseThrow().getRandom().setSeed(53700L);
            // FIXTURE_SEED_END
            original[0]=f.cousin(0).orElseThrow().getUUID();begun[0]=true;
        }).thenWaitUntil(()->h.assertTrue(f.run().getInt("Phase")==GoatmanVignette.VIGIL,"the ordinary evening reaches nightfall soon after the exchange: "+status(f))).thenExecute(()->{
            h.assertTrue(locked[0],"night fell on a shut door with the cousin inside");
        }).thenWaitUntil(()->h.assertTrue(f.run().getInt("Phase")==GoatmanVignette.DAWN,"every native circuit and the complete porch/window approach reach dawn: "+status(f))).thenExecute(()->{
            for(var at:List.of(new BlockPos(-8,2,-59),new BlockPos(-8,2,-67),new BlockPos(-8,2,-73),new BlockPos(8,2,-59),new BlockPos(8,2,-67)))
                h.assertTrue(nightPanes.contains(f.b.offset(at).asLong()),"the saved original left the photographed first sill and struck the other panes in the night: "+nightPanes.size()+"/5");
            h.assertTrue(loaded[0],"the native body and run were saved on a real finished sill");
            // LATE_TRACE_BEGIN
            if(Boolean.getBoolean("the_oldest_house.traceLateKnock")){var trace=f.run();TheOldestHouse.LOGGER.info("LATE KNOCK SUMMARY clock={} assaultAt={} pressureAt={} quietAt={} windowAt={} pulses={} overlap={} late={}",trace.getInt("Clock"),trace.getInt("AssaultAt0465"),trace.getInt("DoorPressureAt0497"),trace.getInt("BedQuietAt131"),trace.getInt("NightWindowAt0472"),trace.getInt("DoorPulseSerial04112"),overlap[0],lateOverlap[0]);}
            // LATE_TRACE_END
            var r=f.run();h.assertTrue(approached[0]&&r.getInt("Circuits0494")==GoatmanStalking.CIRCUITS,"every physical circuit and every bathroom approach leg finish");
            h.assertTrue(overlap[0]&&lateOverlap[0]&&pulse[0]>0,"actual front-door contacts continue while the original is elsewhere, including after all seven demands: overlap="+overlap[0]+" late="+lateOverlap[0]+" pulses="+pulse[0]);
            h.assertTrue(forms.contains(-2)&&children.size()>=2&&childKnock[0],"the complete native night retains the monster and different child disguises, including a real child-visage window strike during the door-knocking demands: "+forms);
            h.assertTrue(r.getBoolean("Right")&&WitnessAccount.has(LabyrinthData.get(f.server),p.getUUID(),WitnessAccount.Story.GOATMAN),"only the complete closed-door and closed-window vigil resolves survival");
        }).thenSucceed();
    }
    private static String status(GoatmanTests.Fixture f){var r=f.run();return "phase="+r.getInt("Phase")+" clock="+r.getInt("Clock")+" evening="+r.getCompound("EveningCircuit0497").getInt("StalkLeg0465")+" circuit="+r.getInt("StalkLeg0465")+" approach="+r.getInt("StalkWindowLeg0465")+" feet="+f.cousin(r.getInt("Wrong")).map(f::rel);}
}
