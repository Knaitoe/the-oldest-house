package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.labyrinth.FuseRooms.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.254, the Fuse Board (docs/FUSE_BOARD_0_4_254.md), played with a real reader's hands. The early room: one fuse; the shutter's motor
 * straining against its catch; the catch out of reach in the dark and drawn in the light; the fuse moved to the shutter and the
 * shutter rolling up and staying. The deep rooms: the latched door that opens only with power, held by its wedge; the hoist lifting the
 * crate off the hatch only while powered, and letting it down onto the hatch when not; the work light in the third room; the catch,
 * and the shutter rolled up with the second fuse.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class FuseRoomTests {
    private FuseRoomTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="fuse_room") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}FuseRooms.clearAll();}
    @AfterBatch(batch="fuse_rooms") public static void deepDone(ServerLevel l){if(active!=null){active.close();active=null;}FuseRooms.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);}
    /** The reader's hand on the latched door, through the ordinary right-click event. */
    private static void door(ServerPlayer p,BlockPos at){var e=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.SOUTH,at,false));NeoForge.EVENT_BUS.post(e);}

    @GameTest(template="empty",batch="fuse_room",timeoutTicks=900)
    public static void theFuseRoomsShutterRollsUpOnceItsCatchIsFoundInTheLight(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=FuseRooms.EARLY;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"fuse_reader");f.players.add(p);
        var light=b.offset(FuseRooms.holder(place,Circuit.LIGHT));var shutter=b.offset(FuseRooms.holder(place,Circuit.SHUTTER));var catchAt=b.offset(FuseRooms.catchAt(place));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.POWER&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of the fuse board, built whole, and no story");
                FuseRooms.buildEarly(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var far=b.offset(PuzzleRooms.farDoor(place));h.assertTrue(l.getBlockState(far).getBlock() instanceof DoorBlock&&!l.getBlockState(far).is(net.minecraft.world.level.block.Blocks.IRON_DOOR),"the far door is an ordinary door: the shutter is what waits");
                h.assertTrue(FuseRooms.shutterRows(l,b,place)==3&&!FuseRooms.lit(l,b,place)&&!FuseRooms.pinned(l,b,place)&&PowerBlock.stage(l.getBlockState(b.offset(FuseRooms.EARLY_CLIP)))==1,"dark, the shutter down, its catch engaged, the one fuse in the spare clip");
                h.assertTrue(PowerBlock.is(l.getBlockState(b.offset(-4,2,-4)),PowerBlock.Kind.CABLE_SHUTTER)&&PowerBlock.is(l.getBlockState(b.offset(-4,3,-7)),PowerBlock.Kind.CABLE_LIGHT),"the cables run up the wall from their holders");
                stand(p,l,b,-2.5,0,-4.5);
                press(l,p,light);
                h.assertTrue(PowerBlock.stage(l.getBlockState(light))==0,"the first touch of the board explains it");
                // The fuse to the shutter: the motor strains against the catch, and the shutter stays down.
                h.assertTrue(press(l,p,shutter)&&FuseRooms.powered(l,b,place,Circuit.SHUTTER)&&PowerBlock.stage(l.getBlockState(b.offset(FuseRooms.EARLY_CLIP)))==0,"the fuse goes home under SHUTTER");})
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(FuseRooms.shutterRows(l,b,place)==3,"the motor strains against the catch, and the shutter stays down");
                stand(p,l,b,-1.5,0,-8.5);
                h.assertTrue(press(l,p,catchAt)&&!FuseRooms.pinned(l,b,place),"in the dark the catch can't be found");
                // The fuse back, then to the light.
                h.assertTrue(press(l,p,shutter)&&!FuseRooms.powered(l,b,place,Circuit.SHUTTER),"the fuse back to the clip");
                h.assertTrue(press(l,p,light)&&FuseRooms.powered(l,b,place,Circuit.LIGHT),"the fuse to the work light");})
            .thenWaitUntil(()->h.assertTrue(FuseRooms.lit(l,b,place),"the work light comes on"))
            .thenExecute(()->{
                h.assertTrue(press(l,p,catchAt)&&FuseRooms.pinned(l,b,place),"in the light the catch is plain to see: drawn and pinned back");
                h.assertTrue(press(l,p,catchAt)&&!FuseRooms.pinned(l,b,place),"every change can be undone: the pin pulled");
                h.assertTrue(press(l,p,catchAt)&&FuseRooms.pinned(l,b,place),"and pinned again");
                h.assertTrue(press(l,p,light)&&press(l,p,shutter)&&FuseRooms.powered(l,b,place,Circuit.SHUTTER)&&!FuseRooms.powered(l,b,place,Circuit.LIGHT),"the fuse moved to the shutter");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not solved yet");})
            .thenWaitUntil(()->h.assertTrue(FuseRooms.shutterRows(l,b,place)==0&&PuzzleRooms.solved(f.data(),place),"the shutter rolls up onto the lit room beyond"))
            .thenExecute(()->{
                h.assertTrue(!FuseRooms.lit(l,b,place),"the light went out when the fuse moved");
                h.assertTrue(press(l,p,shutter)&&press(l,p,light)&&FuseRooms.powered(l,b,place,Circuit.LIGHT),"the fuse back to the light");})
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(FuseRooms.lit(l,b,place)&&FuseRooms.shutterRows(l,b,place)==0,"a lit workshop with its way open: the pin holds the shutter up");
                FuseRooms.buildEarly(l,b);h.assertTrue(FuseRooms.shutterRows(l,b,place)==0&&FuseRooms.pinned(l,b,place)&&FuseRooms.lit(l,b,place),"built again, solved: the shutter up, the light on");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="fuse_rooms",timeoutTicks=1600)
    public static void theFuseRoomsPutTwoFusesWhereTheyAreNeededInTurn(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=FuseRooms.DEEP;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"fuses_reader");f.players.add(p);
        BlockPos latch=b.offset(FuseRooms.holder(place,Circuit.LATCH)),hoist=b.offset(FuseRooms.holder(place,Circuit.HOIST)),light=b.offset(FuseRooms.holder(place,Circuit.LIGHT)),shutter=b.offset(FuseRooms.holder(place,Circuit.SHUTTER));
        var doorAt=b.offset(FuseRooms.DOOR);var wedge=b.offset(FuseRooms.WEDGE_AT);var hatch=b.offset(FuseRooms.HATCH_AT);var catchAt=b.offset(FuseRooms.catchAt(place));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&puzzle.deep&&puzzle.builtOn().equals(java.util.List.of(FuseRooms.EARLY)),"the deep rooms, offered once the fuse room is solved");
                FuseRooms.buildDeep(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(!FuseRooms.doorOpen(l,b)&&FuseRooms.crateHeight(l,b)==0&&!FuseRooms.hatchOpen(l,b)&&!FuseRooms.lit(l,b,place)&&FuseRooms.shutterRows(l,b,place)==3,"the door shut, the crate on its hatch, the third room dark, its shutter down");
                h.assertTrue(PowerBlock.stage(l.getBlockState(b.offset(FuseRooms.DEEP_CLIP_A)))==1&&PowerBlock.stage(l.getBlockState(b.offset(FuseRooms.DEEP_CLIP_B)))==1,"two fuses in the spare clips");
                stand(p,l,b,-2.5,0,-3.5);press(l,p,latch);
                // The door: shut without power, opened with it.
                stand(p,l,b,2.5,0,-6.5);door(p,doorAt);h.assertTrue(!FuseRooms.doorOpen(l,b),"without power the door won't open");
                stand(p,l,b,-2.5,0,-3.5);h.assertTrue(press(l,p,latch)&&FuseRooms.powered(l,b,place,Circuit.LATCH),"a fuse to the latch");})
            .thenIdle(6)
            .thenExecute(()->{
                stand(p,l,b,2.5,0,-6.5);door(p,doorAt);h.assertTrue(FuseRooms.doorOpen(l,b),"with power the latch lets go and the door opens");
                // Unwedged, it shuts again when the power goes.
                stand(p,l,b,-2.5,0,-3.5);h.assertTrue(press(l,p,latch)&&!FuseRooms.powered(l,b,place,Circuit.LATCH),"the fuse back");})
            .thenWaitUntil(()->h.assertTrue(!FuseRooms.doorOpen(l,b),"without power the door swings shut on its closer"))
            .thenExecute(()->{
                // From the second room's side a push bar lets anyone out, power or none, and the closer gives them time to go through.
                stand(p,l,b,2.5,0,-9.5);door(p,doorAt);h.assertTrue(FuseRooms.doorOpen(l,b),"from the second room the push bar opens the door without power");})
            .thenIdle(20)
            .thenExecute(()->h.assertTrue(FuseRooms.doorOpen(l,b),"the closer waits a moment for whoever opened it"))
            .thenWaitUntil(()->h.assertTrue(!FuseRooms.doorOpen(l,b),"and then takes the door again"))
            .thenExecute(()->{
                stand(p,l,b,-2.5,0,-3.5);h.assertTrue(press(l,p,latch),"the fuse to the latch again");})
            .thenIdle(6)
            .thenExecute(()->{
                stand(p,l,b,2.5,0,-6.5);door(p,doorAt);h.assertTrue(FuseRooms.doorOpen(l,b),"open again");
                h.assertTrue(press(l,p,wedge)&&FuseRooms.wedged(l,b),"the wedge kicked under the open door");
                stand(p,l,b,-2.5,0,-3.5);h.assertTrue(press(l,p,latch)&&!FuseRooms.powered(l,b,place,Circuit.LATCH),"the fuse back from the latch");})
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(FuseRooms.doorOpen(l,b),"the wedge holds the door open, latch or no latch");
                // The hoist, watched through the window: the crate comes up off the hatch.
                h.assertTrue(press(l,p,hoist)&&FuseRooms.powered(l,b,place,Circuit.HOIST),"a fuse to the hoist");})
            .thenWaitUntil(()->h.assertTrue(FuseRooms.crateHeight(l,b)==FuseRooms.CRATE_TOP,"the crate rises on its chain"))
            .thenExecute(()->{
                stand(p,l,b,-.5,0,-12.5);h.assertTrue(press(l,p,hatch)&&FuseRooms.hatchOpen(l,b),"the hatch hauled open");
                h.assertTrue(l.getBlockState(hatch).getCollisionShape(l,hatch).isEmpty()&&l.getBlockState(hatch).isLadder(l,hatch,p),"open, it is a hole with a ladder through it");
                // Let down without power, the crate shuts the hatch under it; the hoist again, and the hatch again.
                stand(p,l,b,-2.5,0,-3.5);h.assertTrue(press(l,p,hoist)&&!FuseRooms.powered(l,b,place,Circuit.HOIST),"the fuse back from the hoist");})
            .thenWaitUntil(()->h.assertTrue(FuseRooms.crateHeight(l,b)==0&&!FuseRooms.hatchOpen(l,b),"without power the crate settles back onto the hatch, shutting it"))
            .thenExecute(()->{h.assertTrue(press(l,p,hoist),"the fuse to the hoist again");})
            .thenWaitUntil(()->h.assertTrue(FuseRooms.crateHeight(l,b)==FuseRooms.CRATE_TOP,"up again"))
            .thenExecute(()->{
                stand(p,l,b,-.5,0,-12.5);h.assertTrue(press(l,p,hatch)&&FuseRooms.hatchOpen(l,b),"the hatch open again");
                stand(p,l,b,-2.5,0,-3.5);h.assertTrue(press(l,p,light)&&FuseRooms.powered(l,b,place,Circuit.LIGHT),"the second fuse to the work light");
                h.assertTrue(PowerBlock.stage(l.getBlockState(b.offset(FuseRooms.DEEP_CLIP_A)))==0&&PowerBlock.stage(l.getBlockState(b.offset(FuseRooms.DEEP_CLIP_B)))==0,"both fuses out of the clips");
                h.assertTrue(press(l,p,shutter)&&!FuseRooms.powered(l,b,place,Circuit.SHUTTER),"with both fuses in use, the shutter's holder stays empty");
                // Down the hatch, along the passage and up into the third room.
                stand(p,l,b,-1.5,0,-20.5);})
            .thenWaitUntil(()->h.assertTrue(FuseRooms.lit(l,b,place),"the third room's work light burns"))
            .thenExecute(()->{
                // The way back into the second room opens only from the third.
                var back=b.offset(FuseRooms.BACK_DOOR);
                stand(p,l,b,.5,0,-14.5);door(p,back);h.assertTrue(!FuseRooms.backDoorOpen(l,b),"from the hoist's room the iron door has no handle");
                stand(p,l,b,.5,0,-18.5);door(p,back);h.assertTrue(FuseRooms.backDoorOpen(l,b),"from the third room its handle turns");
                stand(p,l,b,-1.5,0,-20.5);})
            .thenWaitUntil(()->h.assertTrue(!FuseRooms.backDoorOpen(l,b),"and it shuts itself behind"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(-2,-3,-15)).isAir()&&l.getBlockState(b.offset(-2,-4,-15)).isAir()&&l.getBlockState(b.offset(-2,-2,-18)).is(net.minecraft.world.level.block.Blocks.LADDER),"the passage runs under the wall to a ladder up");
                h.assertTrue(press(l,p,catchAt)&&FuseRooms.pinned(l,b,place),"in the light, the catch drawn and pinned back");
                stand(p,l,b,-2.5,0,-3.5);
                h.assertTrue(press(l,p,light)&&press(l,p,shutter)&&FuseRooms.powered(l,b,place,Circuit.SHUTTER),"back at the board: the second fuse from the light to the shutter");
                h.assertTrue(FuseRooms.crateHeight(l,b)==FuseRooms.CRATE_TOP&&FuseRooms.doorOpen(l,b),"the hoist still holds the crate up, and the wedge the door");})
            .thenWaitUntil(()->h.assertTrue(FuseRooms.shutterRows(l,b,place)==0&&PuzzleRooms.solved(f.data(),place),"the shutter rolls up, seen through the window, and stays"))
            .thenExecute(()->{
                FuseRooms.buildDeep(l,b);
                h.assertTrue(FuseRooms.shutterRows(l,b,place)==0&&FuseRooms.doorOpen(l,b)&&FuseRooms.wedged(l,b)&&FuseRooms.crateHeight(l,b)==FuseRooms.CRATE_TOP&&FuseRooms.hatchOpen(l,b)&&FuseRooms.lit(l,b,place),"built again, solved: the way through standing open");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }
}
