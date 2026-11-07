package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.333, State Route 9, with real readers. The road built whole and dealt only after the yard; the traffic coming out of the hedges;
 * a pickup's knock (down on the tar, alive), a semi's wake on a reader beside its lane, and a semi going by a reader crouched in the
 * median who is not touched by it; the convoy called by the first reader into the median; then a car squarely (0.4.336): a death, the
 * body left torn at the lane's edge with what they carried fallen round it, seen by the reader on the shoulder. Then two readers: one calls the gap from the fence, aloud over their head; the other takes the key
 * from the mailbox and the whole road drops to a crawl with the wagon in it; the wagon is looked into; a companion walked onto the road
 * is put back behind the fence; the key in the padlock opens the gate, darkens the road and gives each reader their own account (crossed
 * for the key; watched from the fence, with the gaps they called) and the reflector; the road set back after.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class StateRouteTests {
    private StateRouteTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.STATE_ROUTE;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);StateRoute.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}StateRoute.clearAll();}
    private static StateRouteScene scene(LiteraryTests.Fixture f){return f.level(P).getBlockEntity(f.base(P).offset(StateRouteSite.ANCHOR)) instanceof StateRouteScene s?s:null;}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-59,b.getY()-5,b.getZ()-30,b.getX()+59,b.getY()+12,b.getZ()+4);}
    private static void look(ServerPlayer p,Vec3 at){var d=at.subtract(p.getEyePosition());p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));p.setXRot((float)-Math.toDegrees(Math.atan2(d.y,d.horizontalDistance())));p.setYHeadRot(p.getYRot());}
    private static String account(ServerPlayer p){for(var st:p.getInventory().items)if(st.is(Items.WRITTEN_BOOK)){var c=st.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null&&c.title().raw().equals(StateRouteTexts.TITLE))return String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}return "";}
    private static boolean holds(ServerPlayer p,net.minecraft.world.item.Item item){for(var st:p.getInventory().items)if(st.is(item))return true;return false;}
    /** Put one vehicle in a lane at a place on the road, with nothing else moving. */
    private static StateRouteScene.Vehicle put(StateRouteScene s,int kind,int lane,double x,double speed){var v=new StateRouteScene.Vehicle(s.nextId++,kind,lane,x,speed,0);s.traffic.add(v);return v;}
    private static void hush(StateRouteScene s,long now){s.traffic.clear();s.coming.clear();java.util.Arrays.fill(s.nextSpawn,now+100000);}

    @GameTest(template="empty",batch="state_route",timeoutTicks=3000)
    public static void theRoadTheTrafficAndWhatItDoesToABody(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&scene(f)!=null,"the road is resident, its anchor under the median"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(0,0,-12),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==9,"a literary road outside, one visit, built whole, at midnight");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("survival")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"a survival story, the quota derived");
                h.assertTrue(StateRouteBlock.is(l.getBlockState(b.offset(StateRouteSite.MAILBOX)),StateRouteBlock.Kind.MAILBOX)&&l.getBlockState(b.offset(StateRouteSite.MAILBOX)).getValue(StateRouteBlock.STAGE)==0,"the mailbox with its flag up");
                var gate=l.getBlockState(b.offset(StateRouteSite.GATE));h.assertTrue(gate.getBlock() instanceof DoorBlock&&!gate.getValue(DoorBlock.OPEN),"the padlocked gate, shut");
                h.assertTrue(StateRouteBlock.is(l.getBlockState(b.offset(3,0,StateRouteSite.FENCE_Z)),StateRouteBlock.Kind.CHAIN)&&StateRouteBlock.is(l.getBlockState(b.offset(StateRouteSite.LAMP)),StateRouteBlock.Kind.LAMP),"the chain-link fence, the sodium lamp");
                h.assertTrue(l.getBlockState(b.offset(0,-1,-12)).getCollisionShape(l,b.offset(0,-1,-12)).isEmpty()&&!l.getBlockState(b.offset(0,-2,-12)).isAir()&&l.getBlockState(b.offset(0,-1,-9)).is(StateRouteRegistry.material("asphalt")),"the median a step down from the lanes");
                h.assertTrue(StateRouteBlock.is(l.getBlockState(b.offset(0,0,-25)),StateRouteBlock.Kind.CORN),"corn beyond the far shoulder");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("the logging trucks come off the grade")&&all.contains("before the streetlights go out"),"the note round the key is the owner's found text: "+all);
                var probe=java.util.UUID.randomUUID();h.assertTrue(!StateRoute.dealable(f.data(),probe),"not before the yard");
                f.data().visit(probe,LabyrinthPlace.YARD_TURNAROUND);h.assertTrue(StateRoute.dealable(f.data(),probe),"after it");
            })
            .thenExecute(()->{var p=f.player("route_crosser",P);f.at(p,P,.5,0,-3.9);})
            .thenWaitUntil(()->h.assertTrue(!scene(f).traffic.isEmpty(),"traffic comes out of the hedges"))
            .thenExecute(()->{var s=scene(f);var p=f.players.get(0);hush(s,l.getGameTime());
                // A pickup's knock in the far lane.
                p.setHealth(20);f.at(p,P,-6.5,0,-17.5);put(s,StateRouteScene.PICKUP,3,-6.5+2.4,StateRoute.PICKUP_SPEED);})
            .thenWaitUntil(()->h.assertTrue(f.own(f.players.get(0),P).getInt("RoutePickup")==1,"the pickup knocks them down"))
            .thenExecute(()->{var p=f.players.get(0);var r=p.position().subtract(Vec3.atLowerCornerOf(b));
                h.assertTrue(p.isAlive()&&!StateRouteSite.onStart(r)&&f.own(p,P).getInt("RouteStruck")==0,"a pickup puts them down on the tar, alive");
                var s=scene(f);hush(s,l.getGameTime());
                // A semi in the near lane, a reader in the next lane beside it.
                p.setHealth(20);f.at(p,P,0.5,0,-9.0);p.setDeltaMovement(Vec3.ZERO);put(s,StateRouteScene.SEMI,0,0.5-8,StateRoute.SEMI_SPEED);})
            .thenWaitUntil(()->h.assertTrue(f.own(f.players.get(0),P).getInt("RouteWake")==1,"the wind off the trailer takes hold of them"))
            .thenExecute(()->{var p=f.players.get(0);h.assertTrue(p.getHealth()<20&&f.own(p,P).getInt("RouteStruck")==0,"a heart of buffeting, not a strike");
                var s=scene(f);hush(s,l.getGameTime());
                // Down in the median's weeds: the convoy is called, and a semi going by does not touch them.
                p.setHealth(20);f.at(p,P,0.5,-1,-12);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);})
            .thenWaitUntil(()->h.assertTrue(scene(f).convoyAt>=0&&f.own(f.players.get(0),P).getBoolean("RouteMedian"),"the first reader into the median brings the convoy"))
            .thenExecute(()->{var s=scene(f);var p=f.players.get(0);s.traffic.clear();put(s,StateRouteScene.SEMI,1,0.5-8,StateRoute.SEMI_SPEED);})
            .thenWaitUntil(()->h.assertTrue(f.own(f.players.get(0),P).getBoolean("RouteCrouched"),"crouched in the weeds as the semi goes by"))
            .thenExecute(()->{var p=f.players.get(0);var o=f.own(p,P);
                h.assertTrue(o.getInt("RouteWake")==1&&!o.getBoolean("RouteStood")&&p.getHealth()==20,"the wind goes over a reader down in the median: "+o);
                p.setShiftKeyDown(false);p.setPose(Pose.STANDING);hush(scene(f),l.getGameTime());})
            .thenIdle(25)
            .thenExecute(()->{var s=scene(f);var p=f.players.get(0);hush(s,l.getGameTime());
                // A car squarely, in the near lane, with someone watching from the shoulder and bread in the crosser's pocket.
                var witness=f.player("route_witness",P);f.at(witness,P,6.5,0,-3.9);
                p.getInventory().add(new ItemStack(Items.BREAD,3));
                p.setHealth(20);f.at(p,P,10.5,0,-6.5);p.setDeltaMovement(Vec3.ZERO);put(s,StateRouteScene.CAR,0,10.5-2.3,StateRoute.CAR_SPEED);})
            .thenWaitUntil(()->h.assertTrue(f.own(f.players.get(0),P).getInt("RouteStruck")==1,"struck squarely by a car"))
            .thenExecute(()->{var s=scene(f);var p=f.players.get(0);var witness=f.players.get(1);var r=p.position().subtract(Vec3.atLowerCornerOf(b));
                h.assertTrue(p.isDeadOrDying(),"a car squarely is a death: health "+p.getHealth());
                var w=s.wrecks.stream().filter(x->x.reader().equals(p.getUUID())).findFirst().orElse(null);
                double laneZ=StateRouteSite.LANE_Z[0];
                h.assertTrue(w!=null&&Math.abs(Math.abs(w.z()-laneZ)-1.0)<1e-6&&w.kind()==StateRouteScene.CAR,"the body is left at the lane's edge for everyone to see: "+s.wrecks);
                h.assertTrue(Math.abs(r.x-w.x())<.75&&Math.abs(r.z-w.z())<.75&&Math.abs(w.x()-10.5)>2,"carried along the lane to where it lies: "+r+" wreck "+w);
                var spot=p.position();var bread=l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(spot,spot).inflate(3),e->e.getItem().is(Items.BREAD));
                h.assertTrue(!bread.isEmpty()&&!holds(p,Items.BREAD),"what they carried falls where the body lies, as any death: "+bread);
                var o=f.own(p,P);h.assertTrue(o.getString("RouteStruckBy").equals("car")&&StateRoute.wrecked(p.getUUID()),"recorded, and their own camera holds on the body until they wake");
                var ow=f.own(witness,P);h.assertTrue(ow.getString("RouteSawWreck").equals(p.getGameProfile().getName())&&ow.getInt("RouteSawWrecks")==1,"the reader on the shoulder saw it: "+ow);
                var text=String.join(" ",StateRouteTexts.account(p.getGameProfile().getName(),o).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(text.contains("looking down at myself on the tar")&&text.contains("woke on cold ground"),"the account remembers the body and the waking: "+text);
                var seen=String.join(" ",StateRouteTexts.account(witness.getGameProfile().getName(),ow).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(seen.contains("I saw what the road left of "+p.getGameProfile().getName()),"and the witness's remembers what they saw: "+seen);})
            .thenSucceed();
    }
    @AfterBatch(batch="state_route") public static void done(ServerLevel l){close();}

    @GameTest(template="empty",batch="state_route_key",timeoutTicks=3000)
    public static void theKeyTheCrawlAndTheGateForARunnerAndAWatcher(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&scene(f)!=null,"the road is resident"))
            .thenExecute(()->{var runner=f.player("route_runner",P);var watcher=f.player("route_watcher",P);
                f.at(watcher,P,1.5,0,-4.2);f.at(runner,P,0.5,-1,-12);})
            .thenWaitUntil(()->h.assertTrue(scene(f).party.size()==2,"two readers on the one road"))
            .thenExecute(()->{var s=scene(f);var runner=f.players.get(0);var watcher=f.players.get(1);hush(s,l.getGameTime());
                // The watcher leans on the fence and calls the gap; it is said aloud, over their head, and heard on the road.
                h.assertTrue(StateRoute.call(watcher),"a reader on the shoulder can call the gaps");
                h.assertTrue(f.own(watcher,P).getInt("RouteCalls")==1&&Spoken.lastHeard(runner,watcher).equals("Now! Go, go!"),"the road is clear, and the runner hears so: "+Spoken.lastHeard(runner,watcher));
                put(s,StateRouteScene.SEMI,2,30,StateRoute.SEMI_SPEED);
                h.assertTrue(StateRoute.gapCall(s,runner.position().subtract(Vec3.atLowerCornerOf(b)),l.getGameTime()).equals("Wait! Truck!"),"a semi coming in the next lane is called");
                hush(s,l.getGameTime());
                // Across to the mailbox.
                f.at(runner,P,0.5,0,-20.6);look(runner,Vec3.atCenterOf(b.offset(StateRouteSite.MAILBOX)));})
            .thenWaitUntil(()->h.assertTrue(f.own(f.players.get(0),P).getBoolean("RouteCrossed"),"across"))
            .thenExecute(()->{var s=scene(f);var runner=f.players.get(0);var watcher=f.players.get(1);
                h.assertTrue(StateRoute.mailbox(runner,b,f.own(runner,P),b.offset(StateRouteSite.MAILBOX)),"the mailbox opens");
                h.assertTrue(holds(runner,StateRouteRegistry.KEY.get())&&s.keyOut&&s.mode==StateRouteScene.CRAWL,"the key out, and every engine on the road drops to a crawl");
                h.assertTrue(l.getBlockState(b.offset(StateRouteSite.MAILBOX)).getValue(StateRouteBlock.STAGE)==1,"the flag down, the box open");
                h.assertTrue(f.own(runner,P).getBoolean("RouteCrawl")&&f.own(watcher,P).getBoolean("RouteCrawl"),"the crawl for everyone on the road");
                for(int lane=0;lane<4;lane++){final int ln=lane;h.assertTrue(s.traffic.stream().filter(v->v.lane==ln).count()>=10,"bumper to bumper in lane "+lane);}
                h.assertTrue(s.traffic.stream().allMatch(v->v.speed<=StateRoute.CRAWL_SPEED+.001),"a crawl");
                var w=StateRoute.wagon(s);h.assertTrue(w!=null&&w.lane==2,"one wagon, in the third lane");
                // Stand by the wagon and look in through the glass.
                f.at(runner,P,w.x+2.5,0,-16.5);look(runner,new Vec3(b.getX()+w.x,b.getY()+1.2,b.getZ()+StateRouteSite.LANE_Z[2]));})
            .thenWaitUntil(()->h.assertTrue(f.own(f.players.get(0),P).getBoolean("RouteWagon"),"looked into the wagon"))
            .thenExecute(()->{var runner=f.players.get(0);
                // A companion walked out onto the road is put back behind the fence; its orders stand.
                var wolf=EntityType.WOLF.create(l);wolf.tame(runner);wolf.moveTo(b.getX()+4.5,b.getY(),b.getZ()-9.5);l.addFreshEntity(wolf);f.extras.add(wolf);
                f.at(runner,P,1.5,0,-4.4);runner.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(StateRouteRegistry.KEY.get()));
                for(var st:runner.getInventory().items)if(st.is(StateRouteRegistry.KEY.get())&&st!=runner.getMainHandItem())st.setCount(0);})
            .thenWaitUntil(()->{var wolf=f.extras.get(f.extras.size()-1);h.assertTrue(wolf.getZ()-b.getZ()>StateRouteSite.FENCE_Z,"the companion waits behind the fence");})
            .thenExecute(()->{var s=scene(f);var runner=f.players.get(0);var watcher=f.players.get(1);
                h.assertTrue(StateRoute.gate(runner,b,f.own(runner,P),runner.getMainHandItem()),"the key in the padlock");
                h.assertTrue(s.mode==StateRouteScene.DARK&&l.getBlockState(b.offset(StateRouteSite.GATE)).getValue(DoorBlock.OPEN),"every light out, the gate open");
                h.assertTrue(!holds(runner,StateRouteRegistry.KEY.get()),"the key stays in the padlock");
                var r=f.own(runner,P);var w=f.own(watcher,P);
                h.assertTrue(r.getBoolean("Completed")&&r.getString("Outcome").equals("carried_the_key_back_across_the_9")&&WitnessAccount.has(f.data(),runner.getUUID(),WitnessAccount.Story.STATE_ROUTE),"the runner resolved");
                h.assertTrue(w.getBoolean("Completed")&&w.getString("Outcome").equals("watched_from_the_fence")&&WitnessAccount.has(f.data(),watcher.getUUID(),WitnessAccount.Story.STATE_ROUTE),"the watcher resolved");
                var ra=account(runner);var wa=account(watcher);
                h.assertTrue(ra.contains("I went across for it")&&ra.contains("At the gate I put the key in the padlock")&&ra.contains("station wagon")&&!ra.contains("stayed at the fence"),"the runner's account: "+ra);
                h.assertTrue(wa.contains("I stayed at the fence")&&wa.contains("called the gaps out loud, once")&&wa.contains("route_runner put the key in the padlock")&&!wa.contains("I went across"),"the watcher's account: "+wa);
                for(var word:new String[]{"red","orange","yellow","amber","white","black","green","blue"})h.assertTrue(!(" "+ra+" "+wa+" ").toLowerCase().contains(" "+word+" "),"no colour named: "+word);
                h.assertTrue(holds(runner,StateRouteRegistry.REFLECTOR.get())&&holds(watcher,StateRouteRegistry.REFLECTOR.get()),"each carries the reflector off the road");
                StateRoute.reset(s,l);
                h.assertTrue(s.mode==StateRouteScene.FAST&&!s.keyOut&&l.getBlockState(b.offset(StateRouteSite.MAILBOX)).getValue(StateRouteBlock.STAGE)==0&&!l.getBlockState(b.offset(StateRouteSite.GATE)).getValue(DoorBlock.OPEN),"set back for the next reader");})
            .thenSucceed();
    }
    @AfterBatch(batch="state_route_key") public static void keyDone(ServerLevel l){close();}
}
