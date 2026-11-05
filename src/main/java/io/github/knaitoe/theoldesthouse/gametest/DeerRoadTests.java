package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.310, A Deer in the Road, with real readers: the road built whole; dealt only after the tunnel; the reader at the wheel of the
 * moving car; the doe, the brake in the one beat, the strike, the stop with the driver's door sprung; the woman out of the doe, to the
 * driver's door, dead against it and the door jammed by her weight; the belt, the split bench, the second birth with the first body
 * still there; four blows and she is down again; the culvert's scrape and the hand, once; off down the ditch with the account in hand.
 * And a second reader caught by the second woman: the blackout, the failed return, the catch recorded and the road not dealt again.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_literary") @PrefixGameTestTemplate(false)
public final class DeerRoadTests {
    private DeerRoadTests(){}
    private static final LabyrinthPlace P=LabyrinthPlace.DEER_ROAD;
    private static LiteraryTests.Fixture active;
    private static LiteraryTests.Fixture fixture(GameTestHelper h){if(active!=null)active.close();active=new LiteraryTests.Fixture(h);DeerRoad.clearAll();return active;}
    private static void close(){if(active!=null){active.close();active=null;}DeerRoad.clearAll();}
    private static DeerRoadScene scene(LiteraryTests.Fixture f){return f.level(P).getBlockEntity(f.base(P).offset(DeerRoadSite.ANCHOR)) instanceof DeerRoadScene s?s:null;}
    private static LiteraryActor woman(LiteraryTests.Fixture f,java.util.UUID id){return id!=null&&f.level(P).getEntity(id) instanceof LiteraryActor a?a:null;}
    private static void look(ServerPlayer p,Vec3 at){var d=at.subtract(p.getEyePosition());p.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));p.setXRot((float)-Math.toDegrees(Math.atan2(d.y,d.horizontalDistance())));p.setYHeadRot(p.getYRot());}
    private static AABB box(LiteraryTests.Fixture f){var b=f.base(P);return new AABB(b.getX()-25,b.getY()-5,b.getZ()-95,b.getX()+15,b.getY()+12,b.getZ()+4);}
    private static void tunnelDone(LiteraryTests.Fixture f,ServerPlayer p){WitnessAccount.resolve(f.data(),p.getUUID(),WitnessAccount.Story.TUNNEL_PERIPHERY,"passed_the_last_green_signal");}

    @GameTest(template="empty",batch="deer_road",timeoutTicks=4000)
    public static void theDoeTheTwoBirthsAndTheDitch(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&scene(f)!=null,"the road is resident, its anchor under the asphalt"))
            .thenExecute(()->{
                h.assertTrue(LiteraryRooms.isLiterary(P)&&LiteraryRooms.outside(P)&&P.isOneShot()&&P.authored()&&SceneClock.at(f.origin,b.offset(-10,0,-40),io.github.knaitoe.theoldesthouse.house.HouseDimensions.OUTSIDE)==9,"a literary road outside, one visit, built whole, at midnight");
                var story=WitnessAccount.Story.of(P.id());h.assertTrue(story!=null&&story.kind.equals("understanding")&&WitnessAccount.Story.values().length==WitnessPool.SOURCES&&WitnessAccount.REQUIRED==WitnessPool.REQUIRED,"an understanding story, the quota derived");
                h.assertTrue(DeerRoadBlock.is(l.getBlockState(b.offset(DeerRoadSite.NOTICE)),DeerRoadBlock.Kind.NOTICE)&&DeerRoadBlock.is(l.getBlockState(b.offset(DeerRoadSite.GRATE)),DeerRoadBlock.Kind.GRATE),"the notice on its post, the culvert's grate");
                h.assertTrue(l.getBlockState(b.offset(DeerRoadSite.GATE)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(0,0,1)).getBlock() instanceof DoorBlock,"the drainage gate and the service door");
                var text=LiteraryTexts.source(P).get(DataComponents.WRITTEN_BOOK_CONTENT);var all=String.join(" ",text.pages().stream().map(pg->pg.raw().getString()).toList());
                h.assertTrue(all.contains("If the animal is carrying, keep clear of it")&&all.contains("hatched shoulder"),"the notice teaches the triangle: "+all);
                var probe=java.util.UUID.randomUUID();h.assertTrue(!DeerRoad.dealable(f.data(),probe),"not before the tunnel");
                WitnessAccount.resolve(f.data(),probe,WitnessAccount.Story.TUNNEL_PERIPHERY,"x");h.assertTrue(DeerRoad.dealable(f.data(),probe),"after it");
                h.assertTrue(scene(f).car==DeerRoadScene.FRESH,"the car idles at the bend");
            })
            .thenExecute(()->{var p=f.player("deer_driver",P);tunnelDone(f,p);})
            .thenWaitUntil(()->h.assertTrue(scene(f).car==DeerRoadScene.DRIVE&&f.players.get(0).getVehicle() instanceof DeerRoadSeat,"behind the wheel of a car already moving"))
            .thenWaitUntil(()->{var p=f.players.get(0);var s=scene(f);if(s.doe==DeerRoadScene.DOE_STEP)p.zza=-1;h.assertTrue(s.car==DeerRoadScene.STOPPED,"the strike, and the stop: car "+s.car+" doe "+s.doe);})
            .thenExecute(()->{var s=scene(f);var p=f.players.get(0);p.zza=0;
                h.assertTrue(s.response==DeerRoadScene.BRAKE&&s.finalZ==-50&&s.open(DeerCarMesh.DRIVER)&&s.hazardFrom>=0,"braked: square in the lane, the driver's door sprung, the hazards on");
                h.assertTrue(l.getBlockState(b.offset(s.finalRoot())).is(DeerRoadRegistry.CAR.get()),"the stopped car has its cells");
                h.assertTrue(f.own(p,P).getInt("DeerBeat")==DeerRoadScene.BRAKE,"the beat is the driver's own");
                p.stopRiding();f.at(p,P,DeerRoadSite.SHOULDER+.5,0,s.doeRestZ+2.5);look(p,new Vec3(b.getX()+s.doeX,b.getY()+.3,b.getZ()+s.doeZ));s.clock=DeerRoad.HUSH_TICKS-1;})
            .thenWaitUntil(()->h.assertTrue(scene(f).story==DeerRoadScene.LIFE1,"she comes out of the doe and rises"))
            .thenExecute(()->{var p=f.players.get(0);var o=f.own(p,P);h.assertTrue(o.getBoolean("DeerSawHand")&&o.getBoolean("DeerWitnessed"),"the hand on the centre line, seen: "+o);
                var w=woman(f,scene(f).first);h.assertTrue(w!=null&&w.role()==LiteraryActor.ROAD_WOMAN&&w.owner().isEmpty(),"one woman for everyone on the road");})
            .thenWaitUntil(()->h.assertTrue(scene(f).story==DeerRoadScene.DEAD1,"she makes the driver's door and dies against it"))
            .thenExecute(()->{var s=scene(f);var w=woman(f,s.first);var p=f.players.get(0);
                h.assertTrue(w.phase()==DeerRoad.DEAD_DOOR_OPEN&&s.jam==DeerCarMesh.DRIVER,"sagged in the open doorway; the door won't swing past her");
                h.assertTrue(f.own(p,P).getBoolean("DeerFirstAtDoor"),"recorded: at the door");
                h.assertTrue(DeerRoad.carUse(p,s.carCentre().add(-1.1,1,-.2))&&s.open(DeerCarMesh.DRIVER),"the driver's door stays where she holds it");
                look(p,s.carCentre().add(DeerCarMesh.BENCH));f.at(p,P,s.carX-2.6,0,s.carZ+1.6);look(p,s.carCentre().add(-1.4,.8,.4));s.clock=DeerRoad.DEAD1_TICKS-1;})
            .thenWaitUntil(()->h.assertTrue(scene(f).story==DeerRoadScene.LIFE2,"the belt, the seam, and out of the back seat"))
            .thenExecute(()->{var s=scene(f);var p=f.players.get(0);var w1=woman(f,s.first);var w2=woman(f,s.second);
                h.assertTrue(s.bench==2&&s.beltTeeth>=6&&s.open(DeerCarMesh.REAR),"the bench split, the belt wound tight, the rear door open: bench "+s.bench+" teeth "+s.beltTeeth);
                h.assertTrue(w1!=null&&w1.phase()==DeerRoad.DEAD_DOOR_OPEN&&w2!=null&&w2!=w1,"the first body still lying where it fell as the second stands");
                h.assertTrue(f.own(p,P).getBoolean("DeerHeardReel"),"the reel heard");
                w2.moveTo(p.getX()+3,p.getY(),p.getZ());for(int i=0;i<DeerRoad.HITS;i++){w2.getPersistentData().putLong("DeerHitAt",-100);DeerRoad.struck(p,w2,6);}})
            .thenWaitUntil(()->h.assertTrue(scene(f).story==DeerRoadScene.DEAD2,"four blows and she goes down again"))
            .thenExecute(()->{var p=f.players.get(0);h.assertTrue(f.own(p,P).getBoolean("DeerKilled2")&&!f.own(p,P).getBoolean("Caught"),"held off, not caught");
                f.at(p,P,DeerRoadSite.DITCH+.5,-2,DeerRoadSite.GRATE.getZ()+1.5);look(p,Vec3.atCenterOf(b.offset(DeerRoadSite.GRATE)));})
            .thenWaitUntil(()->h.assertTrue(DeerRoadBlock.is(l.getBlockState(b.offset(DeerRoadSite.GRATE)),DeerRoadBlock.Kind.GRATE)&&l.getBlockState(b.offset(DeerRoadSite.GRATE)).getValue(DeerRoadBlock.STAGE)==1,"something scrapes behind the grate, and a hand tests it"))
            .thenExecute(()->{var p=f.players.get(0);h.assertTrue(f.own(p,P).getBoolean("DeerHeardScrape")&&scene(f).culvertPlayed,"heard, once");
                DeerRoad.leave(p,"drain");var o=f.own(p,P);
                h.assertTrue(o.getBoolean("Completed")&&o.getString("Outcome").equals("took_the_ditch_to_the_drainage_gate")&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.DEER_ROAD),"off the road by the ditch, resolved");
                String account="";for(var st:p.getInventory().items)if(st.is(Items.WRITTEN_BOOK)){var c=st.get(DataComponents.WRITTEN_BOOK_CONTENT);if(c!=null&&c.title().raw().equals(DeerRoadTexts.TITLE))account=String.join(" ",c.pages().stream().map(pg->pg.raw().getString()).toList());}
                h.assertTrue(account.contains("stood on the brake")&&account.contains("centre line")&&account.contains("drainage gate")&&!account.contains("green")&&!account.contains("yellow"),"the account says what this reader saw: "+account);
                DeerRoad.reset(scene(f),l);h.assertTrue(scene(f).car==DeerRoadScene.FRESH&&!l.getBlockState(b.offset(DeerRoadSite.LANE,0,-50)).is(DeerRoadRegistry.CAR.get())&&woman(f,scene(f).first)==null,"set back for the next reader");})
            .thenSucceed();
    }
    @AfterBatch(batch="deer_road") public static void done(ServerLevel l){close();}

    @GameTest(template="empty",batch="deer_road_catch",timeoutTicks=3000)
    public static void theSecondWomanCatchesAReaderWhoStaysTooClose(GameTestHelper h){
        var f=fixture(h);var b=f.base(P);var l=f.level(P);f.build(P);
        h.startSequence().thenExecute(()->f.chunks.hold(l,box(f)))
            .thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&scene(f)!=null,"the road is resident"))
            .thenExecute(()->{var p=f.player("deer_caught",P);tunnelDone(f,p);})
            .thenWaitUntil(()->h.assertTrue(scene(f).car==DeerRoadScene.STOPPED,"the car stops"))
            .thenExecute(()->{var s=scene(f);h.assertTrue(s.response==DeerRoadScene.NONE&&s.finalZ==-53,"no beat taken: the full strike");f.players.get(0).stopRiding();s.story=DeerRoadScene.DEAD1;s.clock=DeerRoad.DEAD1_TICKS-1;})
            .thenWaitUntil(()->h.assertTrue(scene(f).story==DeerRoadScene.LIFE2,"out of the back seat"))
            .thenExecute(()->{var s=scene(f);var p=f.players.get(0);var w=woman(f,s.second);f.at(p,P,w.getX()-b.getX()-1.6,0,w.getZ()-b.getZ());})
            .thenWaitUntil(()->h.assertTrue(f.own(f.players.get(0),P).getBoolean("Caught"),"she closes the distance and catches them"))
            .thenExecute(()->{var p=f.players.get(0);var o=f.own(p,P);h.assertTrue(o.getInt("DeerCaughtLife")==2&&!o.getBoolean("Completed")&&!DeerRoad.dealable(f.data(),p.getUUID()),"a catch, recorded, and not dealt again");})
            .thenSucceed();
    }
    @AfterBatch(batch="deer_road_catch") public static void caughtDone(ServerLevel l){close();}
}
