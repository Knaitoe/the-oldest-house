package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

/** Exercise the complete House boundary tick, including idle ticks after the door fade ends. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_house_read") @PrefixGameTestTemplate(false)
public final class EntrancePlaytestTests {
    private static LiteraryTests.Fixture active;
    private static void close(){HouseReadSites.clear();HouseTransitionEvents.clearAll();if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="read_entrance142") public static void entranceDone(ServerLevel l){close();}
    @AfterBatch(batch="cross_hall142") public static void crossDone(ServerLevel l){close();}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos at){p.stopRiding();p.teleportTo(l,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();p.hasChangedDimension();p.resetFallDistance();}
    private static void click(ServerPlayer p,BlockPos at){p.gameMode.useItemOn(p,p.serverLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));}
    private static void tick(ServerPlayer p){HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(p));}

    @GameTest(template="empty",batch="read_entrance142",timeoutTicks=14000)
    public static void fiveRealDoorsPrecedeTheMazeAndEveryPrivateRoomSurvivesTheFullHouseTick(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;
        var p=player(h,f,"entrance_reader");var peer=player(h,f,"entrance_observer");
        var hall=f.origin.offset(HouseLayout.AXIS_X,1,HouseImpossibleHallway.END_Z_OFFSET-20);
        f.chunks.hold(f.in,new AABB(hall).inflate(42,5,42));boolean[] ticking={false};
        h.onEachTick(()->{if(ticking[0]){tick(p);tick(peer);}});
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the actual original approach is resident"))
            .thenExecute(()->{
                HouseSavedData.get(p.server).markImpossibleDoorRevealed();HouseImpossibleHallway.build(f.in,f.origin);
                for(var room:HouseReadSites.Room.values()){
                    var door=f.data().door(HouseReadEntrance.id(room));var lower=HouseReadEntrance.lower(f.origin,room);
                    h.assertTrue(door!=null&&door.lower.equals(lower)&&f.in.getBlockState(lower).getBlock() instanceof DoorBlock,"each quiet story has a real door before the labyrinth: "+room);
                    h.assertTrue(lower.getZ()<f.origin.getZ()+HouseImpossibleHallway.END_Z_OFFSET,"all five stand outside the far entrance");
                }
                at(p,f.in,hall);at(peer,f.in,hall);
                var first=f.data().door(HouseReadEntrance.id(HouseReadSites.Room.WAITING));
                h.assertTrue(!HouseReadSites.offer(p,first,LabyrinthPlace.JUNCTION)&&!HouseReadSites.state(p.server,p.getUUID()).contains("Index"),"the physical doors do not bypass the opening");
                io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered();
                p.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                h.assertTrue(!HouseReadSites.offer(p,first,LabyrinthPlace.JUNCTION),"an observer cannot start a personal visit");
                p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);ticking[0]=true;
            });
        for(var room:HouseReadSites.Room.values()){
            seq=seq.thenExecute(()->{var door=f.data().door(HouseReadEntrance.id(room));at(p,f.in,door.lower.relative(door.facing));if(room==HouseReadSites.Room.WAITING)f.data().setBuilt(0,f.origin);click(p,door.lower);if(room==HouseReadSites.Room.WAITING){h.assertTrue(HouseReadSites.state(p.server,p.getUUID()).contains("Index"),"the real entrance door works before the maze has finished its first build");f.data().setBuilt(LabyrinthBuilder.VERSION,f.origin);}})
                .thenWaitUntil(()->{if(HouseReadSites.inside(p)!=room)java.util.concurrent.locks.LockSupport.parkNanos(2_000_000L);h.assertTrue(HouseReadSites.inside(p)==room&&!LabyrinthDoors.isBusy(p),"the original physical door reaches its private story: "+room);})
                .thenExecute(()->{var b=HouseReadSites.base(p.server,p.getUUID(),room);at(peer,p.serverLevel(),b.offset(2,0,room==HouseReadSites.Room.PORCH?8:-2));})
                .thenIdle(24).thenExecute(()->{
                    h.assertTrue(HouseReadSites.inside(p)==room&&HouseReadSites.contains(peer)&&!HouseTransitionEvents.isPending(p)&&!HouseTransitionEvents.isPending(peer),"idle owner and observer remain in recognized House space after the fade: "+room);
                    h.assertTrue(p.isAlive()&&p.getHealth()==20&&peer.isAlive()&&peer.getHealth()==20,"the full boundary tick neither ejects nor kills either native player");
                    f.reload();at(peer,f.in,hall);
                    var entry=f.data().door("house_read/"+p.getUUID()+"/"+room.key()+"/entry");at(p,p.serverLevel(),entry.lower.relative(entry.facing));click(p,entry.lower);
                }).thenWaitUntil(()->h.assertTrue(HouseReadSites.inside(p)==null&&!LabyrinthDoors.isBusy(p)&&p.serverLevel()==f.in,"ordinary retreat returns to the actual approach"))
                .thenExecute(()->{
                    var door=f.data().door(HouseReadEntrance.id(room));h.assertTrue(p.position().distanceToSqr(Vec3.atBottomCenterOf(door.lower.relative(door.facing,2)))<.01,"each saved original source door returns to its own physical place");
                    h.assertTrue(f.data().returnDepth(p.getUUID())==0&&!f.data().visited(p.getUUID()).contains(LabyrinthPlace.JUNCTION.id())&&WitnessAccount.count(f.data(),p.getUUID())==0,"these moments precede maze routes and give no story credit");
                });
        }
        seq.thenExecute(()->{
            var removed=HouseReadEntrance.lower(f.origin,HouseReadSites.Room.COATROOM);f.in.setBlock(removed,Blocks.AIR.defaultBlockState(),2);f.in.setBlock(removed.above(),Blocks.AIR.defaultBlockState(),2);HouseReadEntrance.install(p.server,false);
            h.assertTrue(f.in.getBlockState(removed).isAir(),"a removed original entrance door is never refilled by the saved installer");
            var ordinary=new LabyrinthData.Door("native/plain142",f.in.dimension(),hall,Direction.SOUTH,LabyrinthData.DEALT,false);
            h.assertTrue(!HouseReadSites.offer(p,ordinary,LabyrinthPlace.CROSS_HALL),"ordinary and command cross_hall routes are no longer diverted into a quiet copy");
        }).thenSucceed();
    }

    @GameTest(template="empty",batch="cross_hall142",timeoutTicks=1400)
    public static void actualCrossHallCommandArrivalAndAllThreeBranchesKeepTheirLivingSurvivalReader(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var p=player(h,f,"cross_survival");
        var b=f.base(LabyrinthPlace.CROSS_HALL);var source=f.origin.offset(-30,1,-30);
        f.chunks.hold(f.in,IndianLakeRooms.bounds(b,LabyrinthPlace.CROSS_HALL));f.chunks.hold(f.out,new AABB(source).inflate(5));
        boolean[] ticking={false};h.onEachTick(()->{if(ticking[0])tick(p);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native cross_hall and source are resident"))
          .thenExecute(()->{
              LabyrinthHalls.build(f.in,b,LabyrinthPlace.CROSS_HALL);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.CROSS_HALL,b);f.built.add(LabyrinthPlace.CROSS_HALL);
              for(var pos:BlockPos.betweenClosed(source.offset(-4,-1,-4),source.offset(4,-1,4)))f.out.setBlock(pos,Blocks.OAK_PLANKS.defaultBlockState(),2);
              LabyrinthBuilder.placeDoor(f.out,source,Direction.SOUTH);var door=new LabyrinthData.Door("native/cross_command142",f.out.dimension(),source,Direction.SOUTH,LabyrinthData.toPlace(LabyrinthPlace.CROSS_HALL),true);f.data().putDoor(door);
              io.github.knaitoe.theoldesthouse.opening.OpeningSequence.state(p).markEntered();at(p,f.out,source.south(2));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND));click(p,source);ticking[0]=true;
          }).thenWaitUntil(()->h.assertTrue(!LabyrinthDoors.isBusy(p)&&p.serverLevel()==f.in&&LabyrinthPlaces.placeAt(f.origin,p.blockPosition())==LabyrinthPlace.CROSS_HALL,"the real command door reaches cross_hall itself"))
          .thenExecute(()->at(p,f.in,b.offset(0,0,-3)))
          .thenIdle(30).thenExecute(()->{
              for(int z=-4;z>=-29;z--)step(h,p,b.offset(0,0,z));
              for(int z=-28;z<=-15;z++)step(h,p,b.offset(0,0,z));
              for(int x=-1;x>=-13;x--)step(h,p,b.offset(x,0,-15));
              for(int x=-12;x<=13;x++)step(h,p,b.offset(x,0,-15));
          }).thenIdle(80).thenExecute(()->{
              h.assertTrue(p.isAlive()&&p.getHealth()==20&&p.getMainHandItem().is(Items.DIAMOND)&&p.serverLevel()==f.in&&!HouseTransitionEvents.isPending(p),"ordinary cross_hall keeps health, original possessions and native dimension through arrival, branches and idle ticks");
              h.assertTrue(HouseReadSites.inside(p)==null&&f.data().returnDepth(p.getUUID())==1,"the actual command crossing retains exactly its own ordinary return");
          }).thenSucceed();
    }
    private static void step(GameTestHelper h,ServerPlayer p,BlockPos at){
        var to=Vec3.atBottomCenterOf(at);p.move(net.minecraft.world.entity.MoverType.SELF,to.subtract(p.position()));
        h.assertTrue(p.position().distanceToSqr(to)<.001,"real native collision permits the cross_hall tread "+at);
        tick(p);h.assertTrue(p.getHealth()==20&&!HouseTransitionEvents.isPending(p),"a cross_hall tread is valid living House space");
    }
}
