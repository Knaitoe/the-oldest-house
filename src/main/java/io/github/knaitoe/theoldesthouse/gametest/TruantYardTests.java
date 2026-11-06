package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseExperience;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TruantYardTests {
    private static LiteraryTests.Fixture active;private static NativeTestChunks outside;
    @AfterBatch(batch="truant_yard_body") public static void bodyDone(ServerLevel l){close();}
    @AfterBatch(batch="truant_yard_shed") public static void shedDone(ServerLevel l){close();}
    @AfterBatch(batch="truant_yard_lantern") public static void lanternDone(ServerLevel l){close();}
    @AfterBatch(batch="truant_yard_gate") public static void gateDone(ServerLevel l){close();}
    private static void close(){YardCrawl.clearAll();Talk.clearAll();Truant.fresh();Truant.apart();Truant.force(null);TruantYard.clearAll();if(outside!=null){outside.close();outside=null;}if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h,LabyrinthPlace... places){active=new LiteraryTests.Fixture(h);for(var p:places){var a=LabyrinthPlaces.slotBounds(active.origin,p);active.chunks.hold(active.level(p),new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));}return active;}
    private static ServerPlayer reader(GameTestHelper h,LiteraryTests.Fixture f,String name,int depth){var p=NativeTestPlayers.survival(h,name);f.players.add(p);p.setNoGravity(true);var t=HouseExperience.record(f.data(),p.getUUID());t.putInt("Deepest",depth);HouseExperience.save(f.data(),p.getUUID(),t);return p;}
    private static void at(ServerPlayer p,LiteraryTests.Fixture f,LabyrinthPlace place,double x,double y,double z){var b=f.base(place);p.teleportTo(f.level(place),b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.hasChangedDimension();p.setNoGravity(true);p.setDeltaMovement(Vec3.ZERO);}
    private static void away(ServerPlayer p,LiteraryTests.Fixture f){p.teleportTo(f.in,f.origin.getX()+.5,f.origin.getY(),f.origin.getZ()-3,180,0);p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);}
    private static void build(LiteraryTests.Fixture f,LabyrinthPlace p){TruantPlaces.build(f.level(p),f.base(p),p);LabyrinthBuilder.registerDoors(f.data(),p,f.base(p));f.built.add(p);}
    private static void clock(LiteraryTests.Fixture f,LabyrinthPlace p,long clock){var s=TruantYard.state(f.out,p);s.putLong("Clock",clock);f.data().setStateEntry(TruantYard.STATE,p.id(),s);}
    private static boolean hasp(LiteraryTests.Fixture f){return TruantYard.state(f.out,TruantYard.SHED).getBoolean("ShedLocked");}
    private static void use(ServerPlayer p,LiteraryTests.Fixture f,LabyrinthPlace place,BlockPos rel){var pos=f.base(place).offset(rel);p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(pos.getCenter(),Direction.SOUTH,pos,false));}
    private static void meeting(){Truant.fresh();Truant.apart();Talk.clearAll();}

    @GameTest(template="empty",batch="truant_yard_body",timeoutTicks=8000)
    public static void theWatchedOriginalWaitsThenCrossesDimensionsAndAnUnloadedReturnKeepsItsUuid(GameTestHelper h){
        var f=fixture(h,TruantPlaces.LAMP);var yard=TruantYard.SHED;outside=new NativeTestChunks();var a=LabyrinthPlaces.slotBounds(f.origin,yard);outside.hold(f.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));
        var p=reader(h,f,"truant_inside",5);var q=reader(h,f,"truant_outside",5);var watcher=reader(h,f,"truant_observer",5);UUID[] id={null};BlockPos[] savedPos={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready()&&outside.ready(),"both native rooms are resident"))
            .thenExecute(()->{meeting();build(f,TruantPlaces.LAMP);build(f,yard);Truant.force(Truant.Turn.HELP);at(p,f,TruantPlaces.LAMP,.5,0,-5.5);at(q,f,yard,.5,0,-18.5);away(watcher,f);})
            .thenWaitUntil(()->h.assertTrue(Truant.at(p.server)==TruantPlaces.LAMP&&Truant.body(f.in)!=null,"the original starts in the occupied interior hall"))
            .thenExecute(()->{var original=Truant.body(f.in);id[0]=original.getUUID();watcher.setGameMode(GameType.SPECTATOR);watcher.setCamera(original);away(p,f);})
            .thenIdle(12).thenExecute(()->{h.assertTrue(Truant.body(f.in)!=null&&Truant.body(f.in).getUUID().equals(id[0])&&Truant.body(f.out)==null,"a spectator watching the source keeps the original there");watcher.setCamera(watcher);away(watcher,f);})
            .thenWaitUntil(()->h.assertTrue(Truant.at(p.server)==yard&&Truant.body(f.out)!=null,"the same man reaches the yard when the source is unwatched"))
            .thenExecute(()->{
                h.assertTrue(Truant.body(f.out).getUUID().equals(id[0])&&f.in.getEntity(id[0])==null,"native dimension transfer retains one UUID and removes the source instance");savedPos[0]=Truant.body(f.out).blockPosition();f.reload();away(q,f);outside.close();
            })
            .thenWaitUntil(()->{boolean unloaded=f.out.getChunkSource().getChunkNow(savedPos[0].getX()>>4,savedPos[0].getZ()>>4)==null;if(!unloaded)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(unloaded,"the original's outdoor source chunk actually unloads");})
            .thenExecute(()->at(p,f,TruantPlaces.LAMP,.5,0,-5.5))
            .thenWaitUntil(()->h.assertTrue(Truant.body(f.in)!=null&&Truant.at(p.server)==TruantPlaces.LAMP,"the saved outdoor body is loaded before its return"))
            .thenExecute(()->h.assertTrue(Truant.body(f.in).getUUID().equals(id[0])&&f.out.getEntity(id[0])==null,"an unloaded saved-data return does not create a replacement man"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_yard_shed",timeoutTicks=3800)
    public static void theShedWedgeBecomesAHaspWithAFriendsLiftAndARealFeedHatchEscape(GameTestHelper h){
        var place=TruantYard.SHED;var f=fixture(h,place);var b=f.base(place);var p=reader(h,f,"shed_reader",25);var q=reader(h,f,"shed_friend",5);UUID[] original={null};boolean[] crawl={false};
        h.onEachTick(()->{if(crawl[0])p.move(MoverType.SELF,new Vec3(.12,0,0));});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the shed and its two physical exits are resident"))
            .thenExecute(()->{meeting();build(f,place);clock(f,place,40);Truant.force(Truant.Turn.HELP);at(p,f,place,.5,0,-18.5);at(q,f,place,1.5,0,-12.5);})
            .thenWaitUntil(()->h.assertTrue((Truant.record(p.server,p.getUUID()).getInt("Help")&TruantYard.HELP_SHED)!=0&&Truant.body(f.out)!=null,"he really wedges the door while the reader shelters from the squall: "+Truant.debug(p.server)+" position="+(Truant.body(f.out)==null?"missing":Truant.body(f.out).position().subtract(Vec3.atLowerCornerOf(b)))+" width="+(Truant.body(f.out)==null?0:Truant.body(f.out).getBbWidth())+" wedge="+f.out.getBlockState(b.offset(TruantYard.WEDGE_AT))))
            .thenExecute(()->{original[0]=Truant.body(f.out).getUUID();away(p,f);away(q,f);meeting();Truant.force(Truant.Turn.SHED);at(p,f,place,.5,0,-18.5);at(q,f,place,1.5,0,-12.5);})
            .thenIdle(12).thenExecute(()->{h.assertTrue(!hasp(f)&&Truant.turn()==Truant.Turn.CLAIM,"a friend under twenty prevents the hostile collection");away(p,f);away(q,f);meeting();Truant.force(Truant.Turn.SHED);at(p,f,place,.5,0,-18.5);})
            .thenWaitUntil(()->h.assertTrue(hasp(f),"on a later veteran-only meeting he pulls the wedge and drops the actual outside hasp: "+Truant.debug(p.server)+" position="+(Truant.body(f.out)==null?"missing":Truant.body(f.out).position().subtract(Vec3.atLowerCornerOf(b)))))
            .thenExecute(()->{
                use(p,f,place,TruantYard.SHED_DOOR);h.assertTrue(!f.out.getBlockState(b.offset(TruantYard.SHED_DOOR)).getValue(DoorBlock.OPEN),"the caught native door cannot be opened from inside");
                at(q,f,place,1.5,0,-12.5);use(q,f,place,TruantYard.HASP_AT);h.assertTrue(!hasp(f)&&f.out.getBlockState(b.offset(TruantYard.SHED_DOOR)).getValue(DoorBlock.OPEN),"the friend can lift the physical hasp without the reader's history");
                away(p,f);away(q,f);meeting();Truant.force(Truant.Turn.SHED);at(p,f,place,.5,0,-18.5);
            })
            .thenWaitUntil(()->h.assertTrue(hasp(f),"the next collection still leaves the feed opening available"))
            .thenExecute(()->{at(p,f,place,3.2,0,-20.5);p.setShiftKeyDown(true);use(p,f,place,TruantYard.HATCH_AT);p.setShiftKeyDown(false);h.assertTrue(YardCrawl.crawling(p),"opening the low native hatch takes the real player's crawl pose");crawl[0]=true;})
            .thenWaitUntil(()->h.assertTrue(p.getX()>b.getX()+6.4,"normal collision movement carries the actual reader through the one-block opening"))
            .thenExecute(()->crawl[0]=false)
            .thenWaitUntil(()->h.assertTrue(!YardCrawl.crawling(p),"standing space restores the original pose outside"))
            .thenExecute(()->{h.assertTrue(hasp(f)&&p.getHealth()==p.getMaxHealth()&&p.getForcedPose()==null&&Truant.body(f.out).getUUID().equals(original[0]),"the original reader gets out alive without moving the front latch or replacing Johnny");h.assertTrue(Truant.record(p.server,q.getUUID()).getInt("ShedTraps")==0&&WitnessAccount.count(f.data(),p.getUUID())==0,"the intervention shares no personal harm or story credit");})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_yard_lantern",timeoutTicks=2400)
    public static void theLanternCanLieWhileThePoplarsAndActualWindKeepTheirOwnTime(GameTestHelper h){
        var place=TruantYard.LANTERN;var f=fixture(h,place);var b=f.base(place);var p=reader(h,f,"lantern_reader",25);UUID[] original={null};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the eave, field and poplars are resident"))
            .thenExecute(()->{meeting();build(f,place);clock(f,place,110);Truant.force(Truant.Turn.HELP);at(p,f,place,.5,0,-15.5);})
            .thenWaitUntil(()->h.assertTrue((Truant.record(p.server,p.getUUID()).getInt("Help")&TruantYard.HELP_LANTERN)!=0&&Truant.body(f.out)!=null&&Truant.body(f.out).gesture()==Truant.LAMP,"the earlier lantern really signals a lull: "+Truant.debug(p.server)))
            .thenExecute(()->{original[0]=Truant.body(f.out).getUUID();away(p,f);meeting();Truant.force(Truant.Turn.SIGNAL);clock(f,place,40);at(p,f,place,-1.5,0,-25.5);})
            .thenWaitUntil(()->h.assertTrue(Truant.turn()==Truant.Turn.SIGNAL&&Truant.body(f.out)!=null&&Truant.body(f.out).gesture()==Truant.LAMP&&f.out.getBlockState(b.offset(5,6,-18)).getValue(TruantFittingBlock.STAGE)==2,"his later raised lantern disagrees with the actual bent poplars"))
            .thenExecute(()->at(p,f,place,.5,0,-15.5))
            .thenWaitUntil(()->h.assertTrue(p.getDeltaMovement().x>0&&Truant.record(p.server,p.getUUID()).getInt("FalseWindSignals")==1,"crossing on his false signal meets real wind and records the actual collection once"))
            .thenExecute(()->{clock(f,place,110);f.reload();})
            .thenWaitUntil(()->h.assertTrue(Truant.body(f.out)!=null&&Truant.body(f.out).gesture()!=Truant.LAMP&&f.out.getBlockState(b.offset(5,6,-18)).getValue(TruantFittingBlock.STAGE)==0,"the lull remains readable even though his hostile signal is backwards"))
            .thenExecute(()->h.assertTrue(Truant.body(f.out).getUUID().equals(original[0])&&p.getHealth()==p.getMaxHealth(),"the original man, reader and saved earlier help survive the gust"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="truant_yard_gate",timeoutTicks=3200)
    public static void theStockChainHoldsThenLetsTheOriginalHerdIntoTheChuteAndAFriendCanCatchIt(GameTestHelper h){
        var place=TruantYard.GATE;var f=fixture(h,place);var b=f.base(place);var p=reader(h,f,"stock_reader",25);var q=reader(h,f,"stock_friend",5);Set<UUID> originals=new HashSet<>();boolean[] catchGate={false},escape={false};
        h.onEachTick(()->{if(catchGate[0])use(q,f,place,TruantYard.CHAIN_AT);if(escape[0]){double vy=p.onGround()?0:(p.getDeltaMovement().y-.08)*.98;p.setDeltaMovement(.11,vy,0);p.move(MoverType.SELF,p.getDeltaMovement());if(p.verticalCollision)p.setDeltaMovement(.11,0,0);}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the pen, gate, chute and loading step are resident"))
            .thenExecute(()->{meeting();build(f,place);Truant.force(Truant.Turn.HELP);at(p,f,place,.5,0,-21.5);at(q,f,place,.5,0,-19.5);})
            .thenWaitUntil(()->h.assertTrue((Truant.record(p.server,p.getUUID()).getInt("Help")&TruantYard.HELP_GATE)!=0&&!f.out.getBlockState(b.offset(TruantYard.STOCK_GATE)).getValue(FenceGateBlock.OPEN),"he chains the native gate once the last reader is through"))
            .thenExecute(()->{for(var cow:f.out.getEntitiesOfClass(Cow.class,IndianLakeRooms.bounds(b,place)))originals.add(cow.getUUID());h.assertTrue(originals.size()==5,"the herd consists of five original living cows");away(p,f);away(q,f);meeting();Truant.force(Truant.Turn.HERD);at(p,f,place,.5,0,-24.5);})
            .thenWaitUntil(()->h.assertTrue(TruantYard.state(f.out,place).getBoolean("HerdRunning"),"the chain actually spills while the target is halfway along the chute"))
            .thenWaitUntil(()->h.assertTrue(originals.stream().anyMatch(id->f.out.getEntity(id) instanceof Cow c&&c.getZ()<b.getZ()-19),"the original herd physically crosses the open gate"))
            .thenExecute(()->{h.assertTrue(f.out.noCollision(p,new AABB(b.getX()+2.1,b.getY()+.51,b.getZ()-24.9,b.getX()+2.7,b.getY()+2.3,b.getZ()-24.1)),"the actual loading step is clear above its half-step approach");at(q,f,place,-1.5,0,-16.5);catchGate[0]=true;escape[0]=true;})
            .thenWaitUntil(()->h.assertTrue(!TruantYard.state(f.out,place).getBoolean("HerdRunning")&&!f.out.getBlockState(b.offset(TruantYard.STOCK_GATE)).getValue(FenceGateBlock.OPEN),"the friend catches the chain when living bodies are clear of the gate"))
            .thenWaitUntil(()->h.assertTrue(p.getX()>b.getX()+6.4,"the real reader climbs the loading step and walks off the far half-step"))
            .thenExecute(()->{catchGate[0]=false;escape[0]=false;h.assertTrue(originals.stream().allMatch(id->f.out.getEntity(id) instanceof Cow c&&c.isAlive())&&p.getHealth()==p.getMaxHealth(),"closing the gate retains the actual animals and the original reader");h.assertTrue(Truant.record(p.server,p.getUUID()).getInt("HerdReleases")==1&&Truant.record(p.server,q.getUUID()).getInt("HerdReleases")==0,"the shared physical release keeps the target's own history personal");})
            .thenSucceed();
    }
}
