package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_multiplayer")
@PrefixGameTestTemplate(false)
public final class StaceyTests {
    private static Fixture active;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final LabyrinthPlace place;final ServerLevel l;final BlockPos origin,base;
        final NativeTestChunks chunks=new NativeTestChunks();final List<ServerPlayer> players=new ArrayList<>();final List<Entity> bodies=new ArrayList<>();
        HouseSavedData oldHouse;LabyrinthData oldData;boolean started;
        Fixture(GameTestHelper h,LabyrinthPlace place,int coordinate){this.h=h;this.place=place;l=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.OUTSIDE);
            origin=new BlockPos(coordinate,80,coordinate);base=LabyrinthPlaces.base(origin,place);chunks.hold(l,StaceyCover.area(base,place));active=this;}
        void start(){oldHouse=HouseSavedData.get(l.getServer());oldData=LabyrinthData.get(l.getServer());var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();
            var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,origin);l.getServer().overworld().getDataStorage().set("the_oldest_house",house);l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",d);started=true;}
        void put(int x,int y,int z,Block block){l.setBlock(base.offset(x,y,z),block.defaultBlockState(),3);}
        void corridor(){for(int x=-2;x<=2;x++)for(int z=-45;z<=-27;z++){put(x,-1,z,Blocks.COARSE_DIRT);for(int y=0;y<=3;y++)put(x,y,z,(x==-2||x==2||z==-45||z==-27)?Blocks.STONE:Blocks.AIR);}}
        ServerPlayer player(String name,double x,double z){var p=NativeTestPlayers.survival(h,name);players.add(p);p.setNoGravity(true);p.getFoodData().setFoodLevel(6);
            p.teleportTo(l,base.getX()+x,base.getY(),base.getZ()+z,180,0);p.connection.resetPosition();p.hasChangedDimension();return p;}
        void look(ServerPlayer p,Entity body){var delta=body.getEyePosition().subtract(p.getEyePosition());p.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));p.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));p.setYHeadRot(p.getYRot());p.yRotO=p.getYRot();p.xRotO=p.getXRot();}
        LakeWitchEntity witch(double x,double z){var w=DrownedTownRegistry.LAKE_WITCH.get().create(l);w.moveTo(base.getX()+x,base.getY(),base.getZ()+z);
            if(place==LabyrinthPlace.DROWNED_TOWN)w.shore(base,1);else w.literaryHunt(base,place);w.setNoAi(true);l.addFreshEntity(w);bodies.add(w);return w;}
        void door(BlockPos rel,Block block){var lower=block.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
            l.setBlock(base.offset(rel),lower,3);l.setBlock(base.offset(rel).above(),lower.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),3);}
        void done(){h.succeed();close();}
        @Override public void close(){for(var e:bodies)e.discard();for(var p:players)NativeTestPlayers.remove(p);if(started){var store=l.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}chunks.close();active=null;}
    }
    private static void run(GameTestHelper h,LabyrinthPlace p,int coordinate,Consumer<Fixture> check){var f=new Fixture(h,p,coordinate);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"wait for native chunks and entity sections")).thenExecute(()->{f.start();check.accept(f);});}
    private static void clean(){if(active!=null)active.close();}
    @AfterBatch(batch="stacey_no_cover") public static void noCoverDone(ServerLevel l){clean();}
    @AfterBatch(batch="stacey_pause") public static void pauseDone(ServerLevel l){clean();}
    @AfterBatch(batch="stacey_doors") public static void doorsDone(ServerLevel l){clean();}
    @AfterBatch(batch="stacey_protected") public static void protectedDone(ServerLevel l){clean();}
    @AfterBatch(batch="stacey_speed") public static void speedDone(ServerLevel l){clean();}
    @AfterBatch(batch="stacey_cover") public static void coverDone(ServerLevel l){clean();}
    @AfterBatch(batch="stacey_memory") public static void memoryDone(ServerLevel l){clean();}

    @GameTest(template="empty",batch="stacey_no_cover",timeoutTicks=1800)
    public static void failedCoverCommitsOneSharedBodyToPursuitWithinFiveSecondsAndSurvivesReload(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,1250000,f->{
        f.corridor();var a=f.player("stacey_front_reader",.5,-30.5);var b=f.player("stacey_back_reader",.5,-42.5);a.setInvulnerable(true);b.setInvulnerable(true);
        var w=f.witch(.5,-36.5);var id=w.getUUID();f.look(a,w);f.look(b,w);
        h.onEachTick(()->{if(active==f){f.look(a,w);f.look(b,w);}});
        h.startSequence().thenIdle(100).thenExecute(()->{
            h.assertTrue(w.exposedHunt()&&w.failedCoverTicks()==LakeWitchEntity.COVER_SEARCH_LIMIT,"no unseen reachable cover ends the shared search after four occupied seconds: "+w.huntDiagnostic());
            var tag=new CompoundTag();w.saveWithoutId(tag);int delay=w.attackCooldown();w.load(tag);
            h.assertTrue(w.getUUID().equals(id)&&w.getHealth()==36&&w.exposedHunt()&&w.attackCooldown()==delay,"reload preserves the same body, health and failed-search decision");
            a.setInvulnerable(false);b.setInvulnerable(false);
        }).thenWaitUntil(()->h.assertTrue(a.getHealth()<20||b.getHealth()<20,"even two sustained gazes cannot restart the failed hiding loop")).thenExecute(()->{
            h.assertTrue(w.getUUID().equals(id)&&WitnessAccount.count(LabyrinthData.get(f.l.getServer()),a.getUUID())==0&&WitnessAccount.count(LabyrinthData.get(f.l.getServer()),b.getUUID())==0,"one physical strike awards no peer progression");f.done();
        });
    });}
    @GameTest(template="empty",batch="stacey_pause",timeoutTicks=1800)
    public static void observersAndOfflineReadersCannotAdvanceOrResetTheSavedSearch(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,1250500,f->{
        f.corridor();var p=f.player("stacey_paused_reader",.5,-30.5);var observer=f.player("stacey_observer",.5,-42.5);observer.setGameMode(GameType.SPECTATOR);
        var w=f.witch(.5,-36.5);p.setInvulnerable(true);var tag=new CompoundTag();w.saveWithoutId(tag);tag.putInt("FailedCoverTicks0459",41);tag.putBoolean("ExposedHunt0459",true);tag.putInt("HuntCooldown",13);w.load(tag);
        p.setGameMode(GameType.SPECTATOR);var position=w.position();h.startSequence().thenIdle(40).thenExecute(()->{
            h.assertTrue(w.failedCoverTicks()==41&&w.exposedHunt()&&w.attackCooldown()==13&&w.position().distanceToSqr(position)<.001,"spectator cameras do not run or erase the hunt clock");
            var saved=new CompoundTag();w.saveWithoutId(saved);w.load(saved);p.setGameMode(GameType.SURVIVAL);
        }).thenIdle(1).thenExecute(()->{h.assertTrue(w.exposedHunt()&&w.attackCooldown()<=13,"the returning player cannot farm another hiding grace period");f.done();});
    });}
    @GameTest(template="empty",batch="stacey_doors",timeoutTicks=1800)
    public static void actualSharedPursuitBreaksBothWoodenDoorHalvesOnceAndReachesTheReader(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,1251000,f->{
        f.corridor();for(int x:new int[]{-1,1})for(int y=0;y<=3;y++)f.put(x,y,-35,Blocks.STONE);
        var door=f.base.offset(0,0,-35);f.door(new BlockPos(0,0,-35),Blocks.SPRUCE_DOOR);
        var a=f.player("stacey_door_reader",.5,-30.5);var b=f.player("stacey_door_peer",1.3,-30.5);a.setInvulnerable(true);b.setInvulnerable(true);
        var w=f.witch(.5,-41.5);var id=w.getUUID();int[] first={-1};
        h.onEachTick(()->{if(active!=f)return;if(w.doorBreakTicks()>0&&first[0]<0)first[0]=w.tickCount;
            if(first[0]>=0&&f.l.getBlockState(door).is(Blocks.SPRUCE_DOOR))h.assertTrue(w.doorBreakTicks()<=w.tickCount-first[0]+1,"peers cannot multiply cracking ticks");});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.l.getBlockState(door).isAir(),"the native shared actor must physically reach and break the wooden door: "+w.huntDiagnostic())).thenExecute(()->{
            h.assertTrue(first[0]>=0&&w.tickCount-first[0]>=LakeWitchEntity.DOOR_BREAK_TICKS-1&&f.l.getBlockState(door.above()).isAir(),"one three-second cracking action removes the two native halves");
            int drops=f.l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(door).inflate(4),e->e.getItem().is(Items.SPRUCE_DOOR)).stream().mapToInt(e->e.getItem().getCount()).sum();
            h.assertTrue(drops==1&&w.getUUID().equals(id),"the original shared body drops one real door item");a.setInvulnerable(false);b.setInvulnerable(false);
        }).thenWaitUntil(()->h.assertTrue(a.getHealth()<20||b.getHealth()<20,"the opened passage permits a real pursuit and strike: "+w.huntDiagnostic()+"")).thenExecute(f::done);
    });}
    @GameTest(template="empty",batch="stacey_protected",timeoutTicks=1800)
    public static void doorOpeningIronAndAuthoredConnectionGatesRemainAuthoritative(GameTestHelper h){run(h,LabyrinthPlace.DROWNED_TOWN,1251500,f->{
        f.corridor();var w=f.witch(.5,-36.5);var rel=new BlockPos(0,0,-35);var at=f.base.offset(rel);f.door(rel,Blocks.SPRUCE_DOOR);
        h.assertTrue(w.breakableDoor(at),"an ordinary closed wooden door is eligible");
        var data=LabyrinthData.get(f.l.getServer());data.putDoor(new LabyrinthData.Door("stacey_test_gate",f.l.dimension(),at,Direction.NORTH,"test",false));
        h.assertTrue(!w.breakableDoor(at),"registered route gates cannot be broken to bypass progress");
        var other=new BlockPos(1,0,-36);f.door(other,Blocks.IRON_DOOR);h.assertTrue(!w.breakableDoor(f.base.offset(other)),"iron doors are not wooden obstacles");
        f.door(DrownedTown.CHURCH_DOOR,Blocks.SPRUCE_DOOR);h.assertTrue(!w.breakableDoor(f.base.offset(DrownedTown.CHURCH_DOOR)),"the essay-and-key church gate stays protected");
        f.door(new BlockPos(-1,0,-36),Blocks.SPRUCE_DOOR);var open=f.base.offset(-1,0,-36);f.l.setBlock(open,f.l.getBlockState(open).setValue(DoorBlock.OPEN,true),3);
        h.assertTrue(!w.workDoor(open)&&w.doorBreakTicks()==0,"an opened door cancels cracking without drops");
        h.assertTrue(WitnessAccount.Story.values().length==43,"no new Witness source is introduced");f.done();
    });}
    @GameTest(template="empty",batch="stacey_speed",timeoutTicks=1800)
    public static void actualLungeSpendsDoubledMovementAcrossMoreThanOnePathNode(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,1252000,f->{
        f.corridor();var p=f.player("stacey_speed_reader",.5,-30.5);p.setHealth(6);p.setInvulnerable(true);var w=f.witch(.5,-40.5);double[] peak={0};var previous=new Vec3[]{w.position()};
        h.onEachTick(()->{if(active==f){double moved=w.position().subtract(previous[0]).multiply(1,0,1).length();peak[0]=Math.max(peak[0],moved);previous[0]=w.position();
            h.assertTrue(moved<=1.25,"doubled movement stays in its native per-tick budget");}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(peak[0]>.85,"the actual literary lunge must reach roughly twice its previous .46-block step: "+peak[0])).thenExecute(()->{
            h.assertTrue(w.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)==.5,"the registered movement attribute agrees with the hunt's doubled speed");f.done();
        });
    });}
    @GameTest(template="empty",batch="stacey_cover",timeoutTicks=1800)
    public static void detailedCoverWaitsForCamerasAndLivingPetsAndNeverRefillsRemovedPieces(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,1252500,f->{
        for(var center:StaceyCover.centers(f.place))for(int x=-2;x<=2;x++)for(int z=-2;z<=3;z++){var at=f.base.offset(center).offset(x,-1,z);f.l.setBlock(at,Blocks.SAND.defaultBlockState(),3);}
        var center=f.base.offset(StaceyCover.centers(f.place).getFirst());var camera=f.player("stacey_cover_camera",center.getX()-f.base.getX()+.5,center.getZ()-f.base.getZ()+.5);camera.setGameMode(GameType.SPECTATOR);
        h.assertTrue(!StaceyCover.upgrade(f.l,f.base,f.place),"a spectator camera prevents the saved scene change");camera.teleportTo(f.l,f.base.getX()+150,f.base.getY(),f.base.getZ(),0,0);
        var cat=EntityType.CAT.create(f.l);cat.moveTo(center.getX()-.5,center.getY(),center.getZ()+.5);cat.setTame(true,false);cat.setOwnerUUID(camera.getUUID());cat.setOrderedToSit(true);cat.setHealth(5);f.l.addFreshEntity(cat);f.bodies.add(cat);var id=cat.getUUID();
        h.assertTrue(!StaceyCover.upgrade(f.l,f.base,f.place),"a living Stay pet prevents masonry appearing around its body");cat.moveTo(f.base.getX()+150,f.base.getY(),f.base.getZ());
        var edit=center.offset(1,0,2);f.l.setBlock(edit,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
        h.assertTrue(StaceyCover.upgrade(f.l,f.base,f.place)&&f.l.getBlockState(edit).is(Blocks.DIAMOND_BLOCK),"the once-only upgrade preserves a player block");
        var rock=center.offset(-1,0,0);h.assertTrue(f.l.getBlockState(rock).is(Blocks.STRIPPED_SPRUCE_WOOD)&&f.l.getBlockState(center).isAir()&&f.l.getBlockState(center.east()).isAir(),"rooted details surround a real two-wide open hiding pocket");
        f.l.setBlock(rock,Blocks.AIR.defaultBlockState(),3);var data=LabyrinthData.get(f.l.getServer());var loaded=LabyrinthData.load(data.save(new CompoundTag(),f.l.registryAccess()),f.l.registryAccess());f.l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
        h.assertTrue(!StaceyCover.upgrade(f.l,f.base,f.place)&&f.l.getBlockState(rock).isAir()&&cat.getUUID().equals(id)&&cat.getHealth()==5&&cat.isOrderedToSit(),"restart never refills removed cover or changes the original pet");f.done();
    });}
    @GameTest(template="empty",batch="stacey_memory",timeoutTicks=1800)
    public static void rememberedGirlKeepsNativeIdentityHealthAndNormalPhysics(GameTestHelper h){run(h,LabyrinthPlace.DROWNED_TOWN,1253000,f->{
        f.corridor();var p=f.player("stacey_memory_reader",.5,-30.5);var w=f.witch(.5,-36.5);var id=w.getUUID();w.setHealth(17);w.recollection(p.getUUID(),f.base);w.memoryPhase(3);
        var saved=new CompoundTag();w.saveWithoutId(saved);w.load(saved);f.door(new BlockPos(0,0,-36),Blocks.SPRUCE_DOOR);
        h.assertTrue(w.memory()&&w.memoryPhase()==3&&p.getUUID().equals(w.memoryOwner())&&w.getUUID().equals(id)&&w.getHealth()==17&&!w.breakableDoor(f.base.offset(0,0,-36)),"the recalled original retains its phase, owner, native body and health without hunt griefing");f.done();
    });}
}
