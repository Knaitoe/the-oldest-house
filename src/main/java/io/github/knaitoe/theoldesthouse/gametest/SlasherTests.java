package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_multiplayer")
@PrefixGameTestTemplate(false)
public final class SlasherTests {
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l;final BlockPos origin,base;final NativeTestChunks chunks=new NativeTestChunks();
        final List<ServerPlayer> players=new ArrayList<>();LiteraryActor actor;HouseSavedData oldHouse;LabyrinthData oldData;boolean started,oldGriefing;
        Fixture(GameTestHelper h,int coordinate){this.h=h;l=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.OUTSIDE);origin=new BlockPos(coordinate,80,coordinate);
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.ELK_CARCASSES);chunks.hold(l,new AABB(base.offset(-55,-3,-132),base.offset(56,5,1)));active=this;}
        void start(){oldHouse=HouseSavedData.get(l.getServer());oldData=LabyrinthData.get(l.getServer());oldGriefing=l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,origin);
            l.getServer().overworld().getDataStorage().set("the_oldest_house",house);l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",d);started=true;}
        void put(int x,int y,int z,Block block){l.setBlock(base.offset(x,y,z),block instanceof LeavesBlock?block.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true):block.defaultBlockState(),3);}
        void corridor(){for(int x=-2;x<=2;x++)for(int z=-45;z<=-27;z++){put(x,-1,z,Blocks.PODZOL);for(int y=0;y<=3;y++)put(x,y,z,(Math.abs(x)==2||z==-45||z==-27)?Blocks.STONE:Blocks.AIR);}}
        ServerPlayer player(String name,double x,double z){var p=NativeTestPlayers.survival(h,name);players.add(p);p.setNoGravity(true);p.setInvulnerable(true);p.getFoodData().setFoodLevel(6);
            p.teleportTo(l,base.getX()+x,base.getY(),base.getZ()+z,0,0);p.connection.resetPosition();p.hasChangedDimension();
            var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.ELK_CARCASSES);own.putBoolean("Here",true);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.ELK_CARCASSES,own);return p;}
        void hide(ServerPlayer p){p.setShiftKeyDown(true);p.setForcedPose(Pose.CROUCHING);p.setPose(Pose.CROUCHING);p.refreshDimensions();l.setBlock(p.blockPosition().above(),HouseBlocks.FOREST_COVER.get().defaultBlockState(),3);}
        LiteraryActor actor(double x,double z){actor=LiteraryRegistry.ACTOR.get().create(l);actor.bind(LabyrinthPlace.ELK_CARCASSES,null);actor.appearance(LiteraryActor.KILLER,0);actor.moveTo(base.getX()+x,base.getY(),base.getZ()+z);actor.setNoGravity(true);l.addFreshEntity(actor);
            var d=LabyrinthData.get(l.getServer());var world=LiteraryVignettes.shared(d,LabyrinthPlace.ELK_CARCASSES);world.putUUID("Killer",actor.getUUID());LiteraryVignettes.shared(d,LabyrinthPlace.ELK_CARCASSES,world);return actor;}
        void barrier(Block block){for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)put(x,y,-35,block);}
        void done(){h.succeed();close();}
        @Override public void close(){if(actor!=null)actor.discard();for(var p:players)NativeTestPlayers.remove(p);if(started){l.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(oldGriefing,l.getServer());
            var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}chunks.close();active=null;}
    }
    private static void run(GameTestHelper h,int coordinate,Consumer<Fixture> check){var f=new Fixture(h,coordinate);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native forest chunks and entity sections ready")).thenExecute(()->{f.start();check.accept(f);});}
    private static void cleanup(){if(active!=null)active.close();}
    @AfterBatch(batch="slasher_clearance") public static void clearanceDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_leaves") public static void leavesDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_blocked") public static void blockedDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_observers") public static void observersDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_griefing") public static void griefingDone(ServerLevel l){cleanup();}

    @GameTest(template="empty",batch="slasher_clearance",timeoutTicks=1800)
    public static void crouchingReaderActuallyMovesUnderNativeLeavesWithStandingClearanceBlocked(GameTestHelper h){run(h,1260000,f->{
        f.corridor();for(int z=-37;z<=-35;z++)for(int x=-1;x<=1;x++)f.l.setBlock(f.base.offset(x,1,z),HouseBlocks.FOREST_COVER.get().defaultBlockState(),3);
        var p=f.player("slasher_crouching_reader",.5,-38.5);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);p.setForcedPose(Pose.CROUCHING);p.refreshDimensions();
        var start=p.position();for(int i=0;i<20;i++)p.move(MoverType.SELF,new Vec3(0,0,.12));
        h.assertTrue(p.getZ()>start.z+2&&f.l.noCollision(p,p.getBoundingBox())&&CarcassHunt.concealed(p,f.base),"a real 1.5-block native body must enter and travel under the leaf mesh");
        h.assertTrue(!f.l.noCollision(p,new AABB(p.getX()-.3,p.getY(),p.getZ()-.3,p.getX()+.3,p.getY()+1.8,p.getZ()+.3)),"the same canopy still blocks standing");f.done();
    });}
    @GameTest(template="empty",batch="slasher_leaves",timeoutTicks=1800)
    public static void oneNativeSlasherWalksThroughLeavesBreakingOnlyItsPhysicalPassage(GameTestHelper h){run(h,1260500,f->{
        f.corridor();f.barrier(Blocks.SPRUCE_LEAVES);var p=f.player("slasher_leaf_reader",.5,-30.5);f.hide(p);var peer=f.player("slasher_leaf_observer",1.3,-30.5);peer.setGameMode(GameType.SPECTATOR);
        var a=f.actor(.5,-41.5);var id=a.getUUID();var previous=new Vec3[]{a.position()};h.onEachTick(()->{if(active==f){h.assertTrue(a.position().distanceTo(previous[0])<=.24,"leaves are reached by native movement, never teleporting");previous[0]=a.position();}});
        CarcassHunt.noise(p,p.blockPosition());h.startSequence().thenWaitUntil(()->h.assertTrue(a.getZ()>f.base.getZ()-34.5,"the hunter must cross the actual leaf barrier")).thenExecute(()->{
            h.assertTrue(f.l.getBlockState(f.base.offset(0,0,-35)).isAir()&&f.l.getBlockState(f.base.offset(0,1,-35)).isAir(),"his standing body tears through both leaf cells");
            h.assertTrue(f.l.getBlockState(f.base.offset(1,0,-35)).is(Blocks.SPRUCE_LEAVES)&&f.l.getBlockState(f.base.offset(-2,0,-35)).is(Blocks.STONE),"adjacent cover and solid walls remain intact");
            var saved=new CompoundTag();a.saveWithoutId(saved);a.load(saved);h.assertTrue(a.getUUID().equals(id)&&f.l.getBlockState(f.base.offset(0,0,-35)).isAir(),"reloading the same native actor cannot refill destroyed foliage");
            h.assertTrue(!LiteraryVignettes.personal(LabyrinthData.get(f.l.getServer()),peer.getUUID(),LabyrinthPlace.ELK_CARCASSES).getBoolean("Pursued")&&WitnessAccount.count(LabyrinthData.get(f.l.getServer()),peer.getUUID())==0,"shared demolition credits no observer");f.done();
        });
    });}
    @GameTest(template="empty",batch="slasher_blocked",timeoutTicks=1800)
    public static void unreachableLastKnownPointEndsAfterFourSecondsWithoutBreakingStoneOrMovingTheBody(GameTestHelper h){run(h,1261000,f->{
        f.corridor();f.barrier(Blocks.STONE);var p=f.player("slasher_blocked_reader",.5,-30.5);f.hide(p);var a=f.actor(.5,-41.5);var id=a.getUUID();var before=a.position();CarcassHunt.noise(p,p.blockPosition());
        h.startSequence().thenIdle(82).thenExecute(()->{var state=a.getPersistentData().getCompound("CarcassSearch0455");
            h.assertTrue(state.getInt("Memory")==0&&state.getInt("Patrol")==1&&state.getInt("AvoidTicks0459")>90,"one eighty-tick failed route abandons its unreachable point and advances patrol");
            h.assertTrue(a.position().distanceToSqr(before)<.001&&a.getUUID().equals(id)&&f.l.getBlockState(f.base.offset(0,0,-35)).is(Blocks.STONE),"no relocation, replacement or solid-wall destruction");f.done();});
    });}
    @GameTest(template="empty",batch="slasher_observers",timeoutTicks=1800)
    public static void spectatorPeersCannotRunTheSavedPhysicalSearchClock(GameTestHelper h){run(h,1261500,f->{
        f.corridor();f.barrier(Blocks.STONE);var p=f.player("slasher_clock_reader",.5,-30.5);f.hide(p);var peer=f.player("slasher_clock_observer",1.3,-30.5);peer.setGameMode(GameType.SPECTATOR);
        var a=f.actor(.5,-41.5);CarcassHunt.noise(p,p.blockPosition());p.setGameMode(GameType.SPECTATOR);var before=a.position();var id=a.getUUID();
        h.startSequence().thenIdle(40).thenExecute(()->{h.assertTrue(a.getPersistentData().getCompound("CarcassSearch0455").getInt("Memory")==220&&CarcassHunt.blockedTicks(a)==0&&a.position().equals(before),"cameras neither run nor reset the hunt clock");
            var saved=new CompoundTag();a.saveWithoutId(saved);a.load(saved);p.setGameMode(GameType.SURVIVAL);f.hide(p);
        }).thenIdle(10).thenExecute(()->{int memory=a.getPersistentData().getCompound("CarcassSearch0455").getInt("Memory");h.assertTrue(memory>=209&&memory<=210&&CarcassHunt.blockedTicks(a)<=11&&a.getUUID().equals(id),"one reader and any observer still produce one saved native tick per tick");f.done();});
    });}
    @GameTest(template="empty",batch="slasher_griefing",timeoutTicks=1800)
    public static void nativeMobGriefingRulePreventsLeafRemovalAndBlockedSearchStillRecovers(GameTestHelper h){run(h,1262000,f->{
        f.corridor();f.barrier(Blocks.SPRUCE_LEAVES);f.l.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false,f.l.getServer());
        var p=f.player("slasher_rule_reader",.5,-30.5);f.hide(p);var a=f.actor(.5,-41.5);CarcassHunt.noise(p,p.blockPosition());
        h.startSequence().thenIdle(82).thenExecute(()->{h.assertTrue(f.l.getBlockState(f.base.offset(0,0,-35)).is(Blocks.SPRUCE_LEAVES)&&a.getPersistentData().getCompound("CarcassSearch0455").getInt("Memory")==0,"native griefing permissions preserve leaves while the bounded search recovers");f.done();});
    });}
}
