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
        final GameTestHelper h;final ServerLevel l;final LabyrinthPlace scene;final BlockPos origin,base;final NativeTestChunks chunks=new NativeTestChunks();
        final List<ServerPlayer> players=new ArrayList<>();LiteraryActor actor;HouseSavedData oldHouse;LabyrinthData oldData;boolean started,oldGriefing;
        Fixture(GameTestHelper h,int coordinate){this(h,coordinate,LabyrinthPlace.CAMP_BLOOD);}
        Fixture(GameTestHelper h,int coordinate,LabyrinthPlace scene){this.h=h;this.scene=scene;l=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.OUTSIDE);origin=new BlockPos(coordinate,80,coordinate);
            base=LabyrinthPlaces.base(origin,scene);chunks.hold(l,new AABB(Vec3.atLowerCornerOf(base.offset(-55,-3,-132)),Vec3.atLowerCornerOf(base.offset(56,5,1))));active=this;}
        void start(){oldHouse=HouseSavedData.get(l.getServer());oldData=LabyrinthData.get(l.getServer());oldGriefing=l.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,origin);
            l.getServer().overworld().getDataStorage().set("the_oldest_house",house);l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",d);started=true;}
        void put(int x,int y,int z,Block block){l.setBlock(base.offset(x,y,z),block instanceof LeavesBlock?block.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true):block.defaultBlockState(),3);}
        void corridor(){for(int x=-2;x<=2;x++)for(int z=-45;z<=-27;z++){put(x,-1,z,Blocks.PODZOL);for(int y=0;y<=3;y++)put(x,y,z,(Math.abs(x)==2||z==-45||z==-27)?Blocks.STONE:Blocks.AIR);}}
        ServerPlayer player(String name,double x,double z){var p=NativeTestPlayers.survival(h,name);players.add(p);p.setNoGravity(true);p.setInvulnerable(true);p.getFoodData().setFoodLevel(6);
            p.teleportTo(l,base.getX()+x,base.getY(),base.getZ()+z,0,0);p.connection.resetPosition();p.hasChangedDimension();
            var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,p.getUUID(),scene);own.putBoolean("Here",true);LiteraryVignettes.save(d,p.getUUID(),scene,own);return p;}
        void hide(ServerPlayer p){p.setShiftKeyDown(true);p.setForcedPose(Pose.CROUCHING);p.setPose(Pose.CROUCHING);p.refreshDimensions();l.setBlock(p.blockPosition().above(),HouseBlocks.FOREST_COVER.get().defaultBlockState(),3);}
        LiteraryActor actor(double x,double z){actor=LiteraryRegistry.ACTOR.get().create(l);actor.bind(scene,null);actor.appearance(LiteraryActor.KILLER,0);actor.moveTo(base.getX()+x,base.getY(),base.getZ()+z);actor.setNoGravity(true);l.addFreshEntity(actor);
            var d=LabyrinthData.get(l.getServer());var world=LiteraryVignettes.shared(d,scene);world.putUUID("Killer",actor.getUUID());LiteraryVignettes.shared(d,scene,world);return actor;}
        void barrier(Block block){for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)put(x,y,-35,block);}
        void done(){h.succeed();close();}
        @Override public void close(){if(actor!=null)actor.discard();for(var p:players)NativeTestPlayers.remove(p);if(started){l.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(oldGriefing,l.getServer());
            var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}chunks.close();active=null;}
    }
    private static void run(GameTestHelper h,int coordinate,Consumer<Fixture> check){var f=new Fixture(h,coordinate);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native forest chunks and entity sections ready")).thenExecute(()->{f.start();check.accept(f);});}
    private static void privateRun(GameTestHelper h,int coordinate,Consumer<Fixture> check){var f=new Fixture(h,coordinate,LabyrinthPlace.ELK_CARCASSES);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native private hunt chunks ready")).thenExecute(()->{f.start();check.accept(f);});}
    private static LiteraryActor privateActor(Fixture f,ServerPlayer reader){
        for(var p:f.players){var d=LabyrinthData.get(f.l.getServer());var own=LiteraryVignettes.personal(d,p.getUUID(),f.scene);own.putBoolean("Interrupted",true);LiteraryVignettes.save(d,p.getUUID(),f.scene,own);}
        var a=f.actor(.5,-41.5);a.bind(f.scene,reader.getUUID());a.setNoAi(false);
        var d=LabyrinthData.get(f.l.getServer());var world=LiteraryVignettes.shared(d,f.scene);world.remove("Killer");world.putUUID("Killer_"+reader.getUUID(),a.getUUID());LiteraryVignettes.shared(d,f.scene,world);
        return a;
    }
    @AfterBatch(batch="elk_private_foliage") public static void privateLeavesDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="elk_private_clock") public static void privateClockDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="elk_peer_cover") public static void peerCoverDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="elk_peer_cover",timeoutTicks=1800)
    public static void twoHidingReadersKeepTheirCoverFromTheOthersInvisibleHunter(GameTestHelper h){privateRun(h,1264000,f->{
        f.corridor();f.barrier(Blocks.SPRUCE_LEAVES);var p=f.player("cover_owner",.5,-33.5);var peer=f.player("cover_peer",1.3,-33.5);f.hide(p);f.hide(peer);
        var a=privateActor(f,p);var id=a.getUUID();KillerNavigation.request(a,Vec3.atBottomCenterOf(f.base.offset(0,0,-30)),1.0);
        h.startSequence().thenIdle(170).thenExecute(()->{
            h.assertTrue(CarcassHunt.concealed(p,f.base)&&CarcassHunt.concealed(peer,f.base),"both readers are physically crouched in the same cover");
            h.assertTrue(f.l.getBlockState(f.base.offset(0,0,-35)).is(Blocks.SPRUCE_LEAVES)&&f.l.getBlockState(f.base.offset(0,1,-35)).is(Blocks.SPRUCE_LEAVES),"an invisible private hunter cannot tear away the peer's occupied foliage");
            h.assertTrue(a.getUUID().equals(id)&&a.getZ()<f.base.getZ()-35&&WitnessAccount.count(LabyrinthData.get(f.l.getServer()),peer.getUUID())==0,"blocked movement neither teleports nor grants the peer evidence");
            peer.teleportTo(f.l,f.base.getX()+1.3,f.base.getY(),f.base.getZ()-28.5,0,0);peer.connection.resetPosition();
            KillerNavigation.request(a,Vec3.atBottomCenterOf(f.base.offset(0,0,-30)),1.0);
        }).thenWaitUntil(()->h.assertTrue(a.getZ()>f.base.getZ()-34.5,"the same native actor can clear the physical passage once the peer leaves its cover")).thenExecute(()->{
            h.assertTrue(f.l.getBlockState(f.base.offset(0,0,-35)).isAir()&&a.getUUID().equals(id),"owner-only foliage breaking and actor identity remain intact");f.done();
        });
    });}
    @GameTest(template="empty",batch="elk_private_foliage",timeoutTicks=1800)
    public static void privateElkKillerUsesPhysicalLeafClearanceWithoutASecondSharedBrain(GameTestHelper h){privateRun(h,1263000,f->{
        f.corridor();f.barrier(Blocks.SPRUCE_LEAVES);var p=f.player("private_leaf_owner",.5,-30.5);f.hide(p);var peer=f.player("private_leaf_peer",1.3,-30.5);f.hide(peer);
        var a=privateActor(f,p);var id=a.getUUID();var before=new Vec3[]{a.position()};KillerNavigation.request(a,p.position(),1.0);
        h.onEachTick(()->{if(active==f){h.assertTrue(a.position().distanceTo(before[0])<.45,"the private killer moves physically without teleport recovery or a second brain");before[0]=a.position();}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(a.getZ()>f.base.getZ()-34.5,"the owner's native killer crosses the same real leaf barrier")).thenExecute(()->{
            h.assertTrue(a.getUUID().equals(id)&&a.owner().orElseThrow().equals(p.getUUID())&&!a.getPersistentData().contains("CarcassSearch0455"),"the private killer retains identity and never runs the shared hunt controller");
            h.assertTrue(f.l.getBlockState(f.base.offset(0,0,-35)).isAir()&&f.l.getBlockState(f.base.offset(0,1,-35)).isAir()&&f.l.getBlockState(f.base.offset(1,0,-35)).is(Blocks.SPRUCE_LEAVES),"only the native swept standing body clears foliage");
            h.assertTrue(f.l.getBlockState(f.base.offset(-2,0,-35)).is(Blocks.STONE)&&!LiteraryVignettes.personal(LabyrinthData.get(f.l.getServer()),peer.getUUID(),f.scene).getBoolean("Pursued")&&WitnessAccount.count(LabyrinthData.get(f.l.getServer()),peer.getUUID())==0,"world foliage changes grant neither personal pursuit nor evidence to the peer");f.done();
        });
    });}
    @GameTest(template="empty",batch="elk_private_clock",timeoutTicks=1800)
    public static void privateBlockedRoutePausesForObserversSurvivesReloadAndEndsInThreeSeconds(GameTestHelper h){privateRun(h,1263500,f->{
        f.corridor();f.barrier(Blocks.STONE);var p=f.player("private_clock_owner",.5,-30.5);f.hide(p);var peer=f.player("private_clock_observer",1.3,-30.5);peer.setGameMode(GameType.SPECTATOR);
        var a=privateActor(f,p);var id=a.getUUID();var before=a.position();KillerNavigation.request(a,p.position(),1.0);
        h.startSequence().thenIdle(10).thenExecute(()->{var saved=new CompoundTag();a.saveWithoutId(saved);a.load(saved);p.setGameMode(GameType.SPECTATOR);
        }).thenIdle(40).thenExecute(()->{int blocked=a.getPersistentData().getCompound("ElkMovement0460").getInt("Blocked");h.assertTrue(blocked>=9&&blocked<=11&&a.position().equals(before),"native observers cannot advance the saved private failed-route clock");p.setGameMode(GameType.SURVIVAL);f.hide(p);
        }).thenIdle(52).thenExecute(()->{h.assertTrue(KillerNavigation.failed(a)&&KillerNavigation.recoveries(a)==1&&a.getUUID().equals(id)&&a.position().distanceTo(before)<.6&&f.l.getBlockState(f.base.offset(0,0,-35)).is(Blocks.STONE),"sixty occupied native ticks replan the route before rejecting an unreachable solid wall");f.done();});
    });}
    private static void cleanup(){if(active!=null)active.close();}
    @AfterBatch(batch="elk_dynamic_route") public static void dynamicRouteDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="elk_door_ambush") public static void doorAmbushDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="elk_door_open") public static void openDoorDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="elk_door_protection") public static void protectedDoorDone(ServerLevel l){cleanup();}
    @GameTest(template="empty",batch="elk_dynamic_route",timeoutTicks=1800)
    public static void privateKillerReplansAroundANewObstacleAfterThreeSecondsAtBoundedSprintPace(GameTestHelper h){privateRun(h,1264500,f->{
        f.corridor();var p=f.player("route_owner",.5,-30.5);f.hide(p);var peer=f.player("route_peer",1.3,-30.5);peer.setGameMode(GameType.SPECTATOR);
        var a=privateActor(f,p);var id=a.getUUID();var previous=new Vec3[]{a.position()};KillerNavigation.request(a,p.position(),1.35);
        h.onEachTick(()->{if(active==f){double movement=a.position().subtract(previous[0]).multiply(1,0,1).length();h.assertTrue(movement<=KillerNavigation.CHASE_STEP+.001,"all native pursuit movement stays just below player sprint speed without teleporting");previous[0]=a.position();}});
        h.startSequence().thenIdle(4).thenExecute(()->{for(int y=0;y<=2;y++)f.put(0,y,-38,Blocks.STONE);})
                .thenWaitUntil(()->h.assertTrue(KillerNavigation.recoveries(a)==1,"sixty blocked occupied ticks trigger an actual alternate route"))
                .thenWaitUntil(()->h.assertTrue(a.getZ()>f.base.getZ()-34.5,"the same body physically diverts around the new object"))
                .thenExecute(()->{h.assertTrue(a.getUUID().equals(id)&&f.l.getBlockState(f.base.offset(0,0,-38)).is(Blocks.STONE)&&WitnessAccount.count(LabyrinthData.get(f.l.getServer()),peer.getUUID())==0,"route recovery preserves the object, identity and peer's personal evidence");f.done();});
    });}
    private static BlockPos huntDoor(Fixture f,Block block){
        f.barrier(Blocks.STONE);var at=f.base.offset(0,0,-35);NovelRooms.door(f.l,at,net.minecraft.core.Direction.NORTH,block,false);return at;
    }
    @GameTest(template="empty",batch="elk_door_ambush",timeoutTicks=1800)
    public static void watchedDoorAmbushMovesAsidePausesForObserversAndBreaksBothHalvesAfterTenSeconds(GameTestHelper h){privateRun(h,1265000,f->{
        f.corridor();var at=huntDoor(f,Blocks.DARK_OAK_DOOR);var p=f.player("door_owner",.5,-30.5);p.setYRot(180);var peer=f.player("door_camera",1.3,-30.5);peer.setGameMode(GameType.SPECTATOR);
        var a=privateActor(f,p);a.moveTo(f.base.getX()+.5,f.base.getY(),f.base.getZ()-37.5);var id=a.getUUID();KillerNavigation.request(a,p.position(),1.35);final int[] held={0};
        h.startSequence().thenIdle(35).thenExecute(()->{
            h.assertTrue(KillerDoors.waitTicks(a)>=34&&KillerDoors.waitTicks(a)<=36&&Math.abs(a.getX()-(f.base.getX()+.5))>.7,"a watched door has one clock and the hunter physically hides beside the frame");
            var saved=new CompoundTag();a.saveWithoutId(saved);a.load(saved);held[0]=KillerDoors.waitTicks(a);p.setGameMode(GameType.SPECTATOR);
        }).thenIdle(40).thenExecute(()->{h.assertTrue(KillerDoors.waitTicks(a)==held[0]&&f.l.getBlockState(at).is(Blocks.DARK_OAK_DOOR),"saved waiting survives reload and observer-only time does not count");p.setGameMode(GameType.SURVIVAL);})
                .thenWaitUntil(()->h.assertTrue(KillerDoors.waitTicks(a)>=195,"the door gets a full ten occupied seconds, including the final cracking animation"))
                .thenExecute(()->h.assertTrue(f.l.getBlockState(at).is(Blocks.DARK_OAK_DOOR)&&f.l.getBlockState(at.above()).is(Blocks.DARK_OAK_DOOR),"both original door halves remain until the deadline"))
                .thenIdle(8).thenExecute(()->{h.assertTrue(f.l.getBlockState(at).isAir()&&f.l.getBlockState(at.above()).isAir()&&a.getUUID().equals(id),"the one original hunter physically destroys both halves at the deadline");f.done();});
    });}
    @GameTest(template="empty",batch="elk_door_open",timeoutTicks=1800)
    public static void openingTheAmbushDoorCancelsDemolitionAndTheSameKillerComesThrough(GameTestHelper h){privateRun(h,1265500,f->{
        f.corridor();var at=huntDoor(f,Blocks.DARK_OAK_DOOR);var p=f.player("opening_owner",.5,-30.5);p.setYRot(180);
        var a=privateActor(f,p);a.moveTo(f.base.getX()+.5,f.base.getY(),f.base.getZ()-37.5);var id=a.getUUID();KillerNavigation.request(a,p.position(),1.35);
        h.startSequence().thenIdle(40).thenExecute(()->{h.assertTrue(KillerDoors.active(a),"the native hunter is waiting at the actual closed door");var state=f.l.getBlockState(at);((DoorBlock)state.getBlock()).setOpen(p,f.l,state,at,true);})
                .thenWaitUntil(()->h.assertTrue(a.getZ()>f.base.getZ()-33.5,"opening the native door resumes physical pursuit through its actual gap"))
                .thenExecute(()->{h.assertTrue(!KillerDoors.active(a)&&f.l.getBlockState(at).is(Blocks.DARK_OAK_DOOR)&&f.l.getBlockState(at).getValue(DoorBlock.OPEN)&&a.getUUID().equals(id),"opening cancels the demolition without replacing the door or actor");f.done();});
    });}
    @GameTest(template="empty",batch="elk_door_protection",timeoutTicks=1800)
    public static void ironRegisteredAndGriefingProtectedDoorsCannotBeDemolishedByPrivateHunters(GameTestHelper h){privateRun(h,1266000,f->{
        f.corridor();var at=huntDoor(f,Blocks.IRON_DOOR);var p=f.player("protected_owner",.5,-30.5);var a=privateActor(f,p);
        h.assertTrue(!KillerDoors.breakable(a,at),"iron doors remain authoritative");
        NovelRooms.door(f.l,at,net.minecraft.core.Direction.NORTH,Blocks.DARK_OAK_DOOR,false);
        var data=LabyrinthData.get(f.l.getServer());data.putDoor(new LabyrinthData.Door("elk_test_gate",f.l.dimension(),at,net.minecraft.core.Direction.NORTH,"test",false));
        h.assertTrue(!KillerDoors.breakable(a,at),"registered story thresholds are never demolished");
        var ordinary=f.base.offset(0,0,-37);NovelRooms.door(f.l,ordinary,net.minecraft.core.Direction.NORTH,Blocks.DARK_OAK_DOOR,false);
        f.l.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false,f.l.getServer());KillerNavigation.request(a,p.position(),1.35);
        h.startSequence().thenIdle(210).thenExecute(()->{h.assertTrue(!KillerDoors.active(a)&&f.l.getBlockState(at).is(Blocks.DARK_OAK_DOOR)&&f.l.getBlockState(ordinary).is(Blocks.DARK_OAK_DOOR)&&f.l.getBlockState(ordinary.above()).is(Blocks.DARK_OAK_DOOR),"native griefing protection and registered doors survive longer than the whole ambush deadline");f.done();});
    });}
    @AfterBatch(batch="slasher_clearance") public static void clearanceDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_leaves") public static void leavesDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_blocked") public static void blockedDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_observers") public static void observersDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_griefing") public static void griefingDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="slasher_crawl_foliage") public static void crawlFoliageDone(ServerLevel l){cleanup();}

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
            h.assertTrue(!LiteraryVignettes.personal(LabyrinthData.get(f.l.getServer()),peer.getUUID(),LabyrinthPlace.CAMP_BLOOD).getBoolean("Pursued")&&WitnessAccount.count(LabyrinthData.get(f.l.getServer()),peer.getUUID())==0,"shared demolition credits no observer");f.done();
        });
    });}
    @GameTest(template="empty",batch="slasher_blocked",timeoutTicks=1800)
    public static void unreachableLastKnownPointEndsAfterFourSecondsWithoutBreakingStoneOrTeleportingTheBody(GameTestHelper h){run(h,1261000,f->{
        f.corridor();f.barrier(Blocks.STONE);var p=f.player("slasher_blocked_reader",.5,-30.5);f.hide(p);var a=f.actor(.5,-41.5);var id=a.getUUID();var before=a.position();CarcassHunt.noise(p,p.blockPosition());
        h.startSequence().thenIdle(82).thenExecute(()->{var state=a.getPersistentData().getCompound("CarcassSearch0455");
            h.assertTrue(state.getInt("Memory")==0&&state.getInt("Patrol")==1&&state.getInt("AvoidTicks0459")>90,"one eighty-tick failed route abandons its unreachable point and advances patrol");
            h.assertTrue(a.position().distanceTo(before)<=.6&&a.getUUID().equals(id)&&f.l.getBlockState(f.base.offset(0,0,-35)).is(Blocks.STONE),"no relocation, replacement or solid-wall destruction");f.done();});
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
    @GameTest(template="empty",batch="slasher_crawl_foliage",timeoutTicks=1800)
    public static void crouchingSlasherKeepsLeavesAboveHisActualBodyIntactUnderASolidLowRoof(GameTestHelper h){run(h,1262500,f->{
        f.corridor();for(int z=-42;z<=-34;z++)f.l.setBlock(f.base.offset(0,1,z),Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP),3);
        for(int z=-42;z<=-38;z++)f.l.setBlock(f.base.offset(1,1,z),HouseBlocks.FOREST_COVER.get().defaultBlockState(),3);
        var p=f.player("slasher_roof_reader",.5,-30.5);f.hide(p);var a=f.actor(.95,-41.5);a.setPose(Pose.CROUCHING);a.refreshDimensions();var id=a.getUUID();
        h.assertTrue(f.l.noCollision(a,a.getBoundingBox()),"the original native crouching body fits beneath the solid slab roof");CarcassHunt.noise(p,p.blockPosition());
        h.startSequence().thenWaitUntil(()->h.assertTrue(a.getZ()>f.base.getZ()-38.5,"the physical hunter must crawl forward under the low roof")).thenExecute(()->{
            h.assertTrue(a.getUUID().equals(id)&&a.getPose()==Pose.CROUCHING&&f.l.noCollision(a,a.getBoundingBox()),"the same hunter travels using his actual low body");
            for(int z=-42;z<=-38;z++)h.assertTrue(f.l.getBlockState(f.base.offset(1,1,z)).is(HouseBlocks.FOREST_COVER.get()),"a crouching passage does not break branches above the body at "+z);f.done();
        });
    });}
}
