package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.362, the Clockwork Attic (docs/CLOCKWORK_ATTIC_0_4_362.md), worked by a real reader's hands. The early attic: the pendulum set
 * swinging drives the train a tooth every half second until the pinned gear stops it; a pin pushed into a gear already turning stops it
 * at once; drawn, the train goes on; left running with nobody there it keeps going; at the end the lever seats and the ladder comes down
 * to the loft. The deep attics: B run first drops the weight into C's gears and C stops there; wound back, B hauls it up again; A run
 * first sets the catch, so B's weight holds, and C runs to its end.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class ClockworkTests {
    private ClockworkTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="clockwork_attic") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="clockwork_attics") public static void deepDone(ServerLevel l){if(active!=null){active.close();active=null;}}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos rel){return ClockworkBlock.stage(l.getBlockState(b.offset(rel)));}
    private static boolean ladder(ServerLevel l,BlockPos b,BlockPos col){for(int y=0;y<=4;y++)if(!l.getBlockState(b.offset(col.getX(),y,col.getZ())).is(Blocks.LADDER))return false;return true;}
    private static int progress(LiteraryTests.Fixture f,LabyrinthPlace place,Clockwork.Train t){return Clockwork.progress(f.in.getServer(),place,t);}

    @GameTest(template="empty",batch="clockwork_attic",timeoutTicks=1800)
    public static void theTrainRunsDownTheAtticToTheLadder(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=Clockwork.EARLY;var b=f.base(place);var l=f.in;hold(f,place);var t=Clockwork.MAIN;
        var id=java.util.UUID.randomUUID();var r=new ServerPlayer[]{NativeTestPlayers.survival(h,"clock_reader",id)};f.players.add(r[0]);
        int[] before={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the attic is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.CLOCKWORK&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early clockwork room, built whole, and no story");
                Clockwork.buildEarly(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(progress(f,place,t)==0&&!Clockwork.running(l.getServer(),place,t)&&Clockwork.pinned(l.getServer(),place,t,Clockwork.EARLY_PIN),"the train stopped at its start, one gear halfway pinned");
                h.assertTrue(ClockworkBlock.is(l.getBlockState(b.offset(t.stations().get(Clockwork.EARLY_PIN))),ClockworkBlock.Kind.PINNED)&&stage(l,b,t.lever())==0&&!ladder(l,b,Clockwork.EARLY_LADDER),"the pin in its gear, the lever up, no ladder to the loft");
                stand(r[0],l,b,2.5,0,-2.5);
                h.assertTrue(press(l,r[0],b.offset(t.clockCase()))&&Clockwork.running(l.getServer(),place,t),"the pendulum set swinging");})
            .thenWaitUntil(()->h.assertTrue(progress(f,place,t)==Clockwork.EARLY_PIN*t.span(),"the motion travels down the gears to the pinned one: "+progress(f,place,t)))
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(progress(f,place,t)==Clockwork.EARLY_PIN*t.span(),"and stops there");
                h.assertTrue(stage(l,b,t.stations().get(Clockwork.EARLY_PIN+1))==0,"the gears past the pin have not moved");
                // A pin pushed into a gear the motion has already reached stops the train at once, even with the first pin drawn.
                h.assertTrue(press(l,r[0],b.offset(t.stations().get(3)))&&Clockwork.pinned(l.getServer(),place,t,3),"a pin into a turning gear");
                h.assertTrue(press(l,r[0],b.offset(t.stations().get(Clockwork.EARLY_PIN)))&&!Clockwork.pinned(l.getServer(),place,t,Clockwork.EARLY_PIN),"the first pin drawn");})
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(progress(f,place,t)==Clockwork.EARLY_PIN*t.span(),"the train still stands: the gear it is turning through is pinned");
                h.assertTrue(press(l,r[0],b.offset(t.stations().get(3)))&&!Clockwork.pinned(l.getServer(),place,t,3),"that pin drawn too");})
            .thenWaitUntil(()->h.assertTrue(progress(f,place,t)>=Clockwork.EARLY_PIN*t.span()+2,"and the train goes on"))
            .thenExecute(()->{before[0]=progress(f,place,t);f.players.remove(r[0]);NativeTestPlayers.remove(r[0]);})
            .thenIdle(100)
            .thenExecute(()->{
                h.assertTrue(progress(f,place,t)==before[0],"with nobody in the attic nothing has been counted yet");
                r[0]=NativeTestPlayers.survival(h,"clock_reader",id);f.players.add(r[0]);stand(r[0],l,b,2.5,0,-2.5);})
            .thenWaitUntil(()->h.assertTrue(progress(f,place,t)>=before[0]+8,"back in the attic, the train has run on while nobody was there: "+before[0]+" to "+progress(f,place,t)))
            .thenWaitUntil(()->h.assertTrue(stage(l,b,t.lever())==1,"the motion reaches the lever and it seats"))
            .thenExecute(()->{
                h.assertTrue(progress(f,place,t)==t.length()&&!Clockwork.running(l.getServer(),place,t)&&stage(l,b,t.clockCase())==0,"run to its end, the pendulum still");
                h.assertTrue(ladder(l,b,Clockwork.EARLY_LADDER)&&PuzzleRooms.solved(f.data(),place),"the ladder down from the hatch, and the room solved");
                h.assertTrue(press(l,r[0],b.offset(t.face()))&&progress(f,place,t)==0&&stage(l,b,t.lever())==0,"the key winds the whole train back");
                h.assertTrue(ladder(l,b,Clockwork.EARLY_LADDER),"but solved stays solved: the ladder stays");
                Clockwork.buildEarly(l,b);
                h.assertTrue(progress(f,place,t)==t.length()&&ladder(l,b,Clockwork.EARLY_LADDER)&&stage(l,b,t.lever())==1,"built again, solved: the train at its end and the ladder down");
                h.assertTrue(WitnessAccount.count(f.data(),id)==0,"and nothing is earned");})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="clockwork_attics",timeoutTicks=3000)
    public static void theWeightComesDownUnlessTheCatchIsSetFirst(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=Clockwork.DEEP;var b=f.base(place);var l=f.in;hold(f,place);var s=l.getServer();
        var A=Clockwork.A;var B=Clockwork.B;var C=Clockwork.C;
        var p=NativeTestPlayers.survival(h,"clocks_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the attics are resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&puzzle.deep&&puzzle.builtOn().equals(java.util.List.of(Clockwork.EARLY)),"the deep attics, offered once the clockwork attic is solved");
                Clockwork.buildDeep(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(stage(l,b,Clockwork.GATE_AT)==0&&ClockworkBlock.is(l.getBlockState(b.offset(Clockwork.WEIGHT_UP)),ClockworkBlock.Kind.WEIGHT)&&!Clockwork.catchSet(s),"bars across the last doorway, the weight hung up, the catch folded back");
                // B first: the wrong order.
                stand(p,l,b,3.5,0,-14.5);
                h.assertTrue(press(l,p,b.offset(B.clockCase()))&&Clockwork.running(s,place,B),"the second bay's clock set going");})
            .thenWaitUntil(()->h.assertTrue(stage(l,b,B.lever())==1,"its train runs to the lever"))
            .thenExecute(()->{
                h.assertTrue(l.getBlockState(b.offset(Clockwork.GATE_AT)).isAir()&&stage(l,b,Clockwork.GATE_AT.above())==1,"the bars draw up from the last doorway");
                h.assertTrue(Clockwork.weightDown(s)&&ClockworkBlock.is(l.getBlockState(b.offset(Clockwork.WEIGHT_DOWN)),ClockworkBlock.Kind.WEIGHT),"and with no catch under it, the weight comes down into the last train's gears");
                stand(p,l,b,3.5,0,-21.5);
                h.assertTrue(press(l,p,b.offset(C.clockCase()))&&Clockwork.running(s,place,C),"the last clock set going");})
            .thenWaitUntil(()->h.assertTrue(progress(f,place,C)==Clockwork.JAM*C.span(),"its train runs as far as the weight: "+progress(f,place,C)))
            .thenIdle(30)
            .thenExecute(()->{
                h.assertTrue(progress(f,place,C)==Clockwork.JAM*C.span()&&stage(l,b,C.lever())==0&&!PuzzleRooms.solved(f.data(),place),"and stops there; the lever stays up");
                h.assertTrue(press(l,p,b.offset(C.clockCase()))&&!Clockwork.running(s,place,C),"the last pendulum stilled");
                h.assertTrue(press(l,p,b.offset(C.face()))&&progress(f,place,C)==0,"and its train wound back");
                stand(p,l,b,3.5,0,-14.5);
                h.assertTrue(press(l,p,b.offset(B.face()))&&progress(f,place,B)==0&&!Clockwork.weightDown(s),"the second train wound back hauls the weight up again");
                h.assertTrue(ClockworkBlock.is(l.getBlockState(b.offset(Clockwork.WEIGHT_UP)),ClockworkBlock.Kind.WEIGHT)&&l.getBlockState(b.offset(Clockwork.GATE_AT)).isAir(),"the weight hung up again, and the bars left up");
                // The right order: A, then B, then C.
                stand(p,l,b,3.5,0,-6.5);
                h.assertTrue(press(l,p,b.offset(A.clockCase())),"the first clock set going");})
            .thenWaitUntil(()->h.assertTrue(Clockwork.catchSet(s)&&stage(l,b,Clockwork.CATCH_AT)==1,"its lever seats and the catch swings out under the weight's chain"))
            .thenExecute(()->{stand(p,l,b,3.5,0,-14.5);h.assertTrue(press(l,p,b.offset(B.clockCase())),"the second clock again");})
            .thenWaitUntil(()->h.assertTrue(stage(l,b,B.lever())==1,"its lever seats again"))
            .thenExecute(()->{
                h.assertTrue(!Clockwork.weightDown(s)&&ClockworkBlock.is(l.getBlockState(b.offset(Clockwork.WEIGHT_UP)),ClockworkBlock.Kind.WEIGHT),"this time the weight drops onto the catch and holds");
                stand(p,l,b,3.5,0,-21.5);h.assertTrue(press(l,p,b.offset(C.clockCase())),"the last clock again");})
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"the last train runs clear through to its lever"))
            .thenExecute(()->{
                h.assertTrue(ladder(l,b,Clockwork.DEEP_LADDER)&&progress(f,place,C)==C.length(),"the ladder down to the loft");
                Clockwork.buildDeep(l,b);
                h.assertTrue(ladder(l,b,Clockwork.DEEP_LADDER)&&Clockwork.catchSet(s)&&l.getBlockState(b.offset(Clockwork.GATE_AT)).isAir()&&progress(f,place,C)==C.length(),"built again, solved: every train at its end, the bars up, the ladder down");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }
}
