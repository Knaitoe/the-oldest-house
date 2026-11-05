package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.306, the Tension Room (docs/TENSION_ROOM_0_4_306.md), played with a real reader's hands: the entry wheel raises the counterweight
 * out of the slack loop; the far wheel winds slack, then (cleat cast off) pulls the weight back down, or (cleat made fast) draws the pin;
 * made fast too early the far drum fills with slack and the pin comes only half out; cast off, the pin's spring takes back what it gave;
 * at the mark and made fast, two turns draw the pin free and the platform rises and stays.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class TensionRoomTests {
    private TensionRoomTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="tension_room") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}TensionRooms.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    /** A hand on a fitting; the wheels' own pace between turns is not what this case is about. */
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at,boolean crouch){
        TensionRooms.clearAll();p.setShiftKeyDown(crouch);
        boolean used=l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.EAST,at,false)).consumesAction();
        p.setShiftKeyDown(false);return used;
    }
    private static String state(ServerLevel l,BlockPos b){return "weight "+TensionRooms.weight(l,b)+", drum "+TensionRooms.drum(l,b)+", slack "+TensionRooms.slack(l,b)+", pin "+TensionRooms.pin(l,b)+", cleat "+(TensionRooms.fast(l,b)?"fast":"off");}
    private static void expect(GameTestHelper h,ServerLevel l,BlockPos b,int weight,int drum,int pin,String why){
        h.assertTrue(TensionRooms.weight(l,b)==weight&&TensionRooms.drum(l,b)==drum&&TensionRooms.pin(l,b)==pin
            &&TensionBlock.stage(l.getBlockState(b.offset(TensionRooms.PIN_AT)))==pin,why+": "+state(l,b));
        int slack=TensionRooms.slack(l,b);int chains=0;for(int i=0;i<4;i++)if(l.getBlockState(b.offset(TensionRooms.LOOP.below(i))).is(Blocks.CHAIN))chains++;
        h.assertTrue(chains==slack,"the loop under the ceiling shows the slack: "+chains+" for "+slack);
    }

    @GameTest(template="empty",batch="tension_room",timeoutTicks=600)
    public static void theTensionRoomsPinDrawsOnlyWithTheWeightAtTheMarkAndTheCleatFast(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=TensionRooms.EARLY;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"tension_reader");f.players.add(p);
        var entry=b.offset(TensionRooms.ENTRY_WHEEL);var cleat=b.offset(TensionRooms.CLEAT_AT);var far=b.offset(TensionRooms.FAR_WHEEL);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.TENSION&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of a new family, built whole, and no story");
                TensionRooms.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var door=b.offset(PuzzleRooms.farDoor(place));h.assertTrue(l.getBlockState(door).getBlock() instanceof DoorBlock&&!l.getBlockState(door).is(Blocks.IRON_DOOR),"the far door is an ordinary door: the pit is what's in the way");
                expect(h,l,b,0,0,0,"built: the weight on the bottom, the drum empty, the cable slack");
                h.assertTrue(!TensionRooms.platformUp(l,b)&&l.getBlockState(b.offset(TensionRooms.PIT_X0,TensionRooms.DOWN_Y+1,TensionRooms.PIT_Z0)).is(Blocks.LADDER),"the platform held down in the pit, a ladder out");
                p.teleportTo(l,b.getX()-2.5,b.getY(),b.getZ()-2.5,90,0);

                // Each station explains itself first.
                press(l,p,entry,false);expect(h,l,b,0,0,0,"the first touch of the entry wheel explains it");
                // Made fast too early: the drum fills with slack, the pin barely moves.
                press(l,p,entry,false);press(l,p,entry,false);expect(h,l,b,2,0,0,"two turns: the weight half way up, the loop shorter");
                press(l,p,cleat,false);press(l,p,cleat,false);h.assertTrue(TensionRooms.fast(l,b),"made fast");
                press(l,p,entry,false);expect(h,l,b,2,0,0,"made fast, the cleat holds the entry wheel");
                press(l,p,far,false);press(l,p,far,false);press(l,p,far,false);expect(h,l,b,2,2,0,"the far drum fills with slack");
                press(l,p,far,false);expect(h,l,b,2,2,0,"and holds no more");
                h.assertTrue(!TensionRooms.platformUp(l,b)&&!PuzzleRooms.solved(f.data(),place),"the platform stays down");
                // Undo: cast off, pay out, take up to the mark.
                press(l,p,cleat,false);press(l,p,far,true);press(l,p,far,true);expect(h,l,b,2,0,0,"cast off and paid out again");
                press(l,p,entry,false);press(l,p,entry,false);expect(h,l,b,4,0,0,"the weight at the painted mark, the cable taut");
                press(l,p,entry,false);expect(h,l,b,4,0,0,"against the head of the shaft, the wheel goes no further");
                // Cast off, the far wheel pulls the weight back down instead of drawing the pin.
                press(l,p,far,false);expect(h,l,b,3,1,0,"the cable runs back through the cleat and the weight sinks");
                press(l,p,far,true);press(l,p,entry,false);expect(h,l,b,4,0,0,"put back");
                // Made fast at the mark, one turn draws the pin half out; cast off, the spring takes it back and drops the weight.
                press(l,p,cleat,false);press(l,p,far,false);expect(h,l,b,4,1,1,"half drawn");
                press(l,p,cleat,false);expect(h,l,b,3,1,0,"cast off: the pin snaps back and the weight drops");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not yet");
                press(l,p,far,true);press(l,p,entry,false);press(l,p,cleat,false);expect(h,l,b,4,0,0,"set again, made fast at the mark");
                press(l,p,far,false);press(l,p,far,false);expect(h,l,b,4,2,2,"two turns draw the pin free");
                h.assertTrue(TensionRooms.platformUp(l,b)&&PuzzleRooms.solved(f.data(),place),"the platform rises level with the floor and the room is solved");
                h.assertTrue(TensionBlock.is(l.getBlockState(b.offset(0,TensionRooms.UP_Y,TensionRooms.PIT_Z1)),TensionBlock.Kind.PLATFORM)&&l.getBlockState(b.offset(0,0,TensionRooms.PIT_Z1)).isAir(),"a floor across the pit to the door");
                // Solved stays solved: casting off now leaves the platform on its catch.
                press(l,p,cleat,false);h.assertTrue(TensionRooms.platformUp(l,b),"the catch holds the platform up");
                TensionRooms.build(l,b);
                expect(h,l,b,4,2,2,"built again, solved: the weight high, the pin drawn");
                h.assertTrue(TensionRooms.platformUp(l,b)&&TensionRooms.fast(l,b),"and the platform up");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }
}
