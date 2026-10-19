package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_wake") @PrefixGameTestTemplate(false)
public final class OctoberFollowupTests {
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="hotel2170488") public static void hotelDone(ServerLevel ignored){close();}
    @AfterBatch(batch="mirror_idle0488") public static void mirrorDone(ServerLevel ignored){close();}
    private static void close(){if(active!=null){HouseExteriorEntityMirror.clear(active.in.getServer());active.players.forEach(p->LabyrinthLoops.forget(p.getUUID()));active.close();active=null;}}
    @GameTest(template="empty",batch="hotel2170488",timeoutTicks=360)
    public static void theActuallyDisplayed217OpensTheHotelForOnlyThatReader(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var hallway=LabyrinthPlace.HOTEL_HALLWAY;var b=f.base(hallway);var hotel=f.base(LabyrinthPlace.HOTEL);
        f.built.add(hallway);f.built.add(LabyrinthPlace.HOTEL);f.chunks.hold(f.in,IndianLakeRooms.bounds(b,hallway).inflate(2));f.chunks.hold(f.in,IndianLakeRooms.bounds(hotel,LabyrinthPlace.HOTEL));
        LabyrinthLoops.buildHallway(f.in,b,hallway);HotelRooms.build(f.in,hotel,LabyrinthPlace.HOTEL);LabyrinthBuilder.registerDoors(f.data(),hallway,b);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.HOTEL,hotel);
        var reader=f.player("room217_reader",hallway);var peer=f.player("room217_peer",hallway);reader.setGameMode(GameType.ADVENTURE);peer.setGameMode(GameType.ADVENTURE);
        var sequence=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native corridor and hotel sections are ready"))
            .thenExecute(()->{var at=b.offset(LabyrinthLoops.periodOrigin(1));reader.moveTo(at.getX()+.5,at.getY(),at.getZ()-4.5);LabyrinthDoors.tickPlayer(reader,f.origin);});
        for(int lap=0;lap<8;lap++)sequence.thenExecute(()->{var at=b.offset(LabyrinthLoops.periodOrigin(2));reader.moveTo(at.getX()+.5,at.getY(),at.getZ()-4.5);LabyrinthDoors.tickPlayer(reader,f.origin);}).thenIdle(2);
        sequence.thenExecute(()->{
            var lower=b.offset(LabyrinthLoops.roomDoor(0,0));var door=f.data().doorAt(f.in.dimension(),lower);
            h.assertTrue(LabyrinthLoops.roomNumberAt(reader,b,lower)==217&&LabyrinthLoops.roomNumberAt(peer,b,lower)==201,"actual forward corridor crossings give the two readers different plaques on the same native door");
            h.assertTrue(HotelVignette.route(peer,door)==null&&LabyrinthData.LOCKED.equals(door.destination),"the peer's resident room remains locked and shared registration is unchanged");
            reader.moveTo(lower.getX()+1.5,lower.getY(),lower.getZ()+.5);
            reader.gameMode.useItemOn(reader,f.in,reader.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(lower.getCenter().add(.5,0,0),Direction.EAST,lower,false));
            h.assertTrue(HotelVignette.inside(reader,LabyrinthPlace.HOTEL)&&!HotelVignette.inside(peer,LabyrinthPlace.HOTEL),"using the displayed 217 through a native Adventure click enters the real hotel for this reader");
            // 0.4.105: the hotel's own guest corridor is built like this one, so the reader steps out in front of 217's own door.
            var room=hotel.offset(HotelRooms.ROOM_DOOR);
            h.assertTrue(reader.position().distanceTo(Vec3.atBottomCenterOf(room).add(1,0,0))<1.5&&f.in.getBlockState(room).getValue(DoorBlock.OPEN),"the displayed 217 lets the reader out in front of 217's own door on the hotel's guest corridor, standing open");
            reader.moveTo(room.getX()+1.5,room.getY(),room.getZ()+.5);
            // Close its two physical halves as the prerequisite for checking native opening.
            f.in.setBlock(room,f.in.getBlockState(room).setValue(DoorBlock.OPEN,false),3);f.in.setBlock(room.above(),f.in.getBlockState(room.above()).setValue(DoorBlock.OPEN,false),3);
            reader.gameMode.useItemOn(reader,f.in,reader.getMainHandItem(),InteractionHand.MAIN_HAND,new BlockHitResult(room.getCenter().add(.5,0,0),Direction.EAST,room,false));
            h.assertTrue(f.in.getBlockState(room).getValue(DoorBlock.OPEN),"the made bedroom's native door also opens normally");
        }).thenSucceed();
    }
    @GameTest(template="empty",batch="mirror_idle0488",timeoutTicks=640)
    public static void watchedOriginalMobsKeepTickingPastTheEmptyWorldTimeoutAndProjectionsLeaveWithTheLastObserver(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var real=f.in.getServer().overworld();var house=HouseSavedData.get(f.in.getServer());house.markInteriorInitialized();
        var eye=f.origin.offset(HouseLayout.AXIS_X,1,HouseLayout.FRONT_DOOR_Z+4);var sourceAt=eye.offset(32,0,0);
        f.chunks.hold(real,new AABB(sourceAt).inflate(4));f.chunks.hold(f.in,new AABB(eye).inflate(4));
        var watcher=NativeTestPlayers.survival(h,"mirror_idle_reader");f.players.add(watcher);watcher.teleportTo(f.in,eye.getX()+.5,eye.getY(),eye.getZ()+.5,0,0);watcher.hasChangedDimension();watcher.setNoGravity(true);
        var wolf=EntityType.WOLF.create(real);h.assertTrue(wolf!=null,"the native original wolf creates");wolf.moveTo(sourceAt.getCenter());wolf.setNoGravity(true);wolf.setPersistenceRequired();real.addFreshEntity(wolf);f.extras.add(wolf);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"source and observer entity sections are ready"))
            .thenIdle(360).thenExecute(()->{
                h.assertTrue(real.players().isEmpty(),"the real source world has no resident player to keep it active");
                h.assertTrue(wolf.tickCount>=320&&wolf.isAlive()&&!HouseExteriorEntityMirror.isProjection(wolf),"the actual source mob keeps ticking beyond the native fifteen-second empty-world suspension");
                h.assertTrue(f.in.getEntitiesOfClass(Mob.class,new AABB(sourceAt).inflate(8),HouseExteriorEntityMirror::isProjection).stream().anyMatch(p->p.getType()==wolf.getType()&&p.isNoAi()&&p.isInvulnerable()),"the tracked visual projection still has no gameplay authority");
                watcher.teleportTo(f.out,eye.getX()+.5,eye.getY(),eye.getZ()+.5,0,0);watcher.hasChangedDimension();
            }).thenIdle(12).thenExecute(()->h.assertTrue(f.in.getEntitiesOfClass(Mob.class,new AABB(sourceAt).inflate(8),HouseExteriorEntityMirror::isProjection).isEmpty(),"the last observer leaving removes the visual projection and releases the source lease")).thenSucceed();
    }
}
