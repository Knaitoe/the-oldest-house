package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.250, the Folding Rooms (docs/FOLDING_ROOMS_0_4_250.md), played with a real reader's hands: the early room's partition stands
 * against its far stop in front of the door, goes only away from whoever pushes it, and clears the door when pushed from the pantry's
 * side; the deep room is crossed by standing off the track, in the recess once the sideboard is out of it and in the morning room's
 * doorway, and pushing the partition past; reaching the far end folds the partitions away and lays the table.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class FoldingRoomTests {
    private FoldingRoomTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="folding_room") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="folded_rooms") public static void deepDone(ServerLevel l){if(active!=null){active.close();active=null;}}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    /** Stands the reader at a cell of the room, facing south (yaw 0) or north (yaw 180). */
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY(),b.getZ()+z,yaw,0);p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(0);}
    private static boolean is(ServerLevel l,BlockPos at,FoldBlock.Kind kind){return FoldBlock.is(l.getBlockState(at),kind);}

    @GameTest(template="empty",batch="folding_room",timeoutTicks=600)
    public static void theFoldingRoomsPartitionClearsTheDoorWhenPushedFromThePantrysSide(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=FoldingRooms.EARLY;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"folding_reader");f.players.add(p);
        var q=FoldingRooms.EARLY_P;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.FOLD&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of the folding partitions, built whole, and no story");
                FoldingRooms.buildEarly(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var farAt=b.offset(PuzzleRooms.farDoor(place));
                h.assertTrue(l.getBlockState(farAt).getBlock() instanceof DoorBlock&&!l.getBlockState(farAt).is(net.minecraft.world.level.block.Blocks.IRON_DOOR),"the far door is an ordinary door: only the partition is in the way");
                h.assertTrue(FoldingRooms.at(l,b,q)==-13&&is(l,b.offset(FoldingRooms.EARLY_DOOR_FRONT),FoldBlock.Kind.PANEL)&&l.getBlockState(b.offset(1,3,-13)).isAir()&&is(l,b.offset(q.x0(),3,-13),FoldBlock.Kind.HANGER)&&is(l,b.offset(0,4,-13),FoldBlock.Kind.HANGER),"the partition stands in front of the door, two panels high, with the gap above it open but for its hangers");
                h.assertTrue(l.getBlockState(b.offset(FoldingRooms.PANTRY_SOUTH)).isAir()&&l.getBlockState(b.offset(FoldingRooms.PANTRY_NORTH)).isAir(),"the pantry opens on both sides of it");
                // From the south it will not go: it is against its north stop.
                stand(p,l,b,.5,-11.5,180);
                h.assertTrue(press(l,p,b.offset(0,1,-13))&&FoldingRooms.at(l,b,q)==-13,"pushed away from the dining room, it is at the end of its track");
                // Round through the pantry to the north side: pushed from there it goes south, off the door and across the hatch.
                stand(p,l,b,.5,-15.5,0);
                h.assertTrue(press(l,p,b.offset(0,1,-13))&&FoldingRooms.at(l,b,q)==-12,"from the other side it rumbles along a panel's length");
                h.assertTrue(is(l,b.offset(-3,1,-12),FoldBlock.Kind.PANEL)&&l.getBlockState(b.offset(FoldingRooms.EARLY_DOOR_FRONT)).isAir(),"it has cleared the door and come to rest across the serving hatch");
                // Every move can be undone: from the dining room it goes back against its stop.
                stand(p,l,b,.5,-9.5,180);
                h.assertTrue(press(l,p,b.offset(0,1,-12))&&FoldingRooms.at(l,b,q)==-13,"pushed from the dining room it goes back to its stop");
                // Nobody is ever pushed: with someone in its way it stays where it is.
                var other=NativeTestPlayers.survival(h,"folding_bystander");f.players.add(other);stand(other,l,b,2.5,-9.5,180);
                stand(p,l,b,.5,-15.5,0);
                h.assertTrue(press(l,p,b.offset(0,1,-13))&&FoldingRooms.at(l,b,q)==-12,"from the north it goes again");
                stand(other,l,b,2.5,-10.5,180);
                h.assertTrue(press(l,p,b.offset(0,1,-12))&&FoldingRooms.at(l,b,q)==-12,"with someone standing in its way it does not move");
                stand(other,l,b,.5,-2.5,180);
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not solved until someone reaches the door");
                stand(p,l,b,3.5,-12.5,270);
            })
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"reaching the door solves the room"))
            .thenExecute(()->{
                h.assertTrue(is(l,b.offset(q.x0(),0,-12),FoldBlock.Kind.FOLDED)&&is(l,b.offset(q.x1(),1,-12),FoldBlock.Kind.FOLDED)&&l.getBlockState(b.offset(0,0,-12)).isAir()&&FoldingRooms.at(l,b,q)==Integer.MIN_VALUE,"the partition folds back on its hinges into the corners, and the room is one room");
                FoldingRooms.buildEarly(l,b);h.assertTrue(is(l,b.offset(q.x0(),0,-12),FoldBlock.Kind.FOLDED)&&FoldingRooms.at(l,b,q)==Integer.MIN_VALUE,"built again, it stays folded away");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="folded_rooms",timeoutTicks=800)
    public static void theFoldedRoomsAreCrossedByStandingOffTheTrackAndLeftLaidForDinner(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=FoldingRooms.DEEP;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"folded_reader");f.players.add(p);
        var p1=FoldingRooms.DEEP_P1;var p2=FoldingRooms.DEEP_P2;var recess=FoldingRooms.RECESS;
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&puzzle.deep&&puzzle.builtOn().equals(java.util.List.of(FoldingRooms.EARLY)),"the deep room, offered once the folding room is solved");
                FoldingRooms.buildDeep(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(FoldingRooms.at(l,b,p1)==-4&&FoldingRooms.at(l,b,p2)==-16&&is(l,b.offset(recess),FoldBlock.Kind.SIDEBOARD),"two partitions across the long room, and the sideboard standing in its recess");
                h.assertTrue(l.getBlockState(b.offset(recess.below())).is(PuzzleRegistry.foldMaterial("marks")),"the floor in the recess is worn where the sideboard's feet have always stood");
                // Pushed up the room past the recess.
                for(int z=-4;z>=-6;z--){stand(p,l,b,.5,z+1.5,180);h.assertTrue(press(l,p,b.offset(0,1,z))&&FoldingRooms.at(l,b,p1)==z-1,"the first partition goes up the room: "+(z-1));}
                // The sideboard out of the recess and off the track.
                stand(p,l,b,-.5,-5.5,90);h.assertTrue(press(l,p,b.offset(recess))&&is(l,b.offset(-2,0,-6),FoldBlock.Kind.SIDEBOARD)&&l.getBlockState(b.offset(recess)).isAir(),"the sideboard comes out of its recess");
                stand(p,l,b,-1.5,-3.5,180);h.assertTrue(press(l,p,b.offset(-2,0,-6))&&is(l,b.offset(-2,0,-5),FoldBlock.Kind.SIDEBOARD),"and down the room");
                stand(p,l,b,-1.5,-2.5,180);h.assertTrue(press(l,p,b.offset(-2,0,-5))&&is(l,b.offset(-2,0,-4),FoldBlock.Kind.SIDEBOARD),"and down again, clear of where the partition must come back");
                // In the recess, facing back down the room, the partition goes past the reader.
                stand(p,l,b,-2.5,-5.5,0);
                h.assertTrue(press(l,p,b.offset(-2,1,-7))&&FoldingRooms.at(l,b,p1)==-6,"from the recess it goes the way the reader faces, beside them");
                h.assertTrue(press(l,p,b.offset(-2,1,-6))&&FoldingRooms.at(l,b,p1)==-5,"and on past them");
                h.assertTrue(p.getZ()-b.getZ()<-5,"the reader is on its far side now");
                // The morning room, and its far doorway as the other place to stand off the track.
                h.assertTrue(l.getBlockState(b.offset(FoldingRooms.MORNING_SOUTH)).isAir()&&FoldingRooms.at(l,b,p2)==-16,"the morning room opens off the middle room; its far doorway is behind the second partition");
                stand(p,l,b,-2.5,-15.5,0);
                h.assertTrue(press(l,p,b.offset(-2,1,-16))&&FoldingRooms.at(l,b,p2)==-15,"from the doorway the second partition goes past too");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not solved until someone reaches the far end");
                stand(p,l,b,.5,-21.5,180);
            })
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place)&&f.data().state(FoldingRooms.STATE).getBoolean("Laid"),"reaching the far end solves the rooms, and they are laid for dinner"))
            .thenExecute(()->{
                h.assertTrue(FoldingRooms.at(l,b,p1)==Integer.MIN_VALUE&&FoldingRooms.at(l,b,p2)==Integer.MIN_VALUE,"both partitions are folded away");
                h.assertTrue(is(l,b.offset(p2.x0(),0,p2.home()),FoldBlock.Kind.FOLDED)&&is(l,b.offset(p2.x1(),1,p2.home()),FoldBlock.Kind.FOLDED),"the second folds home, where its track is worn");
                h.assertTrue(is(l,b.offset(p1.x0(),0,-5),FoldBlock.Kind.FOLDED),"the first folds where it stands, its home taken by the sideboard");
                for(int z=FoldingRooms.TABLE_TO;z<=FoldingRooms.TABLE_FROM;z++)h.assertTrue(is(l,b.offset(0,0,z),FoldBlock.Kind.TABLE),"the table runs down the room: "+z);
                for(int z:FoldingRooms.CHAIR_ROWS)h.assertTrue(is(l,b.offset(-1,0,z),FoldBlock.Kind.CHAIR)&&is(l,b.offset(1,0,z),FoldBlock.Kind.CHAIR),"a chair either side, on the worn marks: "+z);
                h.assertTrue(is(l,b.offset(recess),FoldBlock.Kind.SIDEBOARD)&&!is(l,b.offset(-2,0,-4),FoldBlock.Kind.SIDEBOARD),"the sideboard is back in its recess");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");
            })
            .thenSucceed();
    }
}
