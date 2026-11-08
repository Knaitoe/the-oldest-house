package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.360, the Resonant Pipes (docs/RESONANT_PIPES_0_4_360.md), played with a real reader's hands. The early corridor: at first the low
 * and high notes reach the reeds together and they buzz; fitting the middle run's section and shutting the other two lets the middle note
 * through alone, and the reeds sing and the catch lifts. The deep rooms: cross pipes mix the notes, the high run reaches the last room only
 * by its loop, and only each note alone at its own reed lifts the catch.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class ResonanceTests {
    private ResonanceTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="resonant_pipe") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}}
    @AfterBatch(batch="resonant_pipes") public static void deepDone(ServerLevel l){if(active!=null){active.close();active=null;}}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos rel){return ResonanceBlock.stage(l.getBlockState(b.offset(rel)));}
    private static Set<Character> heard(ServerLevel l,BlockPos b,LabyrinthPlace place,int reed){return ResonantPipes.heard(l,b,place,ResonantPipes.net(place).reeds().get(reed));}
    private static boolean held(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place){return PuzzleRooms.holds(p,f.data().door(place.id()+"/far"));}

    @GameTest(template="empty",batch="resonant_pipe",timeoutTicks=800)
    public static void theMiddleNoteAloneLiftsTheCatch(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=ResonantPipes.EARLY;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"pipe_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the corridor is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.RESONANCE&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of the resonant pipes, built whole, and no story");
                ResonantPipes.buildEarly(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(ResonanceBlock.is(l.getBlockState(b.offset(0,2,-1)),ResonanceBlock.Kind.DRUM)&&ResonanceBlock.is(l.getBlockState(b.offset(ResonantPipes.S_M)),ResonanceBlock.Kind.SECTION),"three drums over the entry, and the runs along the corridor with their sections");
                h.assertTrue(stage(l,b,ResonantPipes.S_M)==0&&stage(l,b,ResonantPipes.S_H)==1&&stage(l,b,ResonantPipes.A_L)==1&&stage(l,b,ResonantPipes.F_H)==1,"the middle run's section out on its bracket, the high run's fitted, every valve open");
                h.assertTrue(heard(l,b,place,0).equals(Set.of('L','H')),"the low and high notes reach the reeds together: "+heard(l,b,place,0));
                h.assertTrue(stage(l,b,ResonantPipes.E_LATCH)==0&&held(f,p,place),"the catch is down and the far door held");
                stand(p,l,b,.5,0,-9.5);})
            .thenWaitUntil(()->h.assertTrue(stage(l,b,ResonantPipes.E_REED)==2,"with a reader in the corridor the reeds buzz against the wrong notes"))
            .thenExecute(()->{
                h.assertTrue(stage(l,b,new BlockPos(0,2,-5))==1&&ResonantPipes.notesAt(l,b,place,new BlockPos(0,2,-5)).equals(Set.of('M')),"the middle note runs up to the gap");
                h.assertTrue(stage(l,b,new BlockPos(0,2,-14))==1&&ResonantPipes.notesAt(l,b,place,new BlockPos(0,2,-14)).equals(Set.of('L','H')),"and past the gap the middle run carries only what comes back from the manifold: "+ResonantPipes.notesAt(l,b,place,new BlockPos(0,2,-14)));
                h.assertTrue(press(l,p,b.offset(ResonantPipes.S_M))&&stage(l,b,ResonantPipes.S_M)==1,"the short length fitted back into the middle run");
                h.assertTrue(heard(l,b,place,0).equals(Set.of('L','M','H')),"now all three reach the reeds");
                h.assertTrue(press(l,p,b.offset(ResonantPipes.A_L))&&stage(l,b,ResonantPipes.A_L)==0,"the low valve at the drums turned shut");
                ResonantPipes.update(l,b,place,1);
                h.assertTrue(ResonantPipes.notesAt(l,b,place,new BlockPos(-1,2,-10)).equals(Set.of('M','H')),"the low note stops at the shut valve: past it the low run carries only what comes back from the manifold, "+ResonantPipes.notesAt(l,b,place,new BlockPos(-1,2,-10)));
                h.assertTrue(stage(l,b,ResonantPipes.E_REED)==2&&heard(l,b,place,0).equals(Set.of('M','H')),"and the reeds still buzz with two notes: "+heard(l,b,place,0));
                h.assertTrue(press(l,p,b.offset(ResonantPipes.S_H))&&stage(l,b,ResonantPipes.S_H)==0,"the high run's section lifted out instead of shutting its valve");
                h.assertTrue(heard(l,b,place,0).equals(Set.of('M')),"the middle note alone");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not yet heard");})
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"the reeds sing their note and the catch lifts"))
            .thenExecute(()->{
                h.assertTrue(stage(l,b,ResonantPipes.E_REED)==1&&stage(l,b,ResonantPipes.E_LATCH)==1&&!held(f,p,place),"the reeds singing, the catch up, and the far door lets the reader on");
                h.assertTrue(press(l,p,b.offset(ResonantPipes.A_L))&&stage(l,b,ResonantPipes.A_L)==1,"the low valve opened again");
                ResonantPipes.update(l,b,place,2);
                h.assertTrue(stage(l,b,ResonantPipes.E_REED)==2&&stage(l,b,ResonantPipes.E_LATCH)==1&&PuzzleRooms.solved(f.data(),place),"the reeds buzz again, but solved stays solved and the catch stays up");
                h.assertTrue(stage(l,b,ResonantPipes.A_L)==1&&stage(l,b,ResonantPipes.S_H)==0,"and every fitting stays as the reader left it");
                ResonantPipes.buildEarly(l,b);ResonantPipes.update(l,b,place,3);
                h.assertTrue(stage(l,b,ResonantPipes.E_REED)==1&&stage(l,b,ResonantPipes.E_LATCH)==1,"built again, solved: the middle note alone at the reeds, the catch up");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="resonant_pipes",timeoutTicks=800)
    public static void eachNoteAloneAtItsOwnReedLiftsTheCatch(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=ResonantPipes.DEEP;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"pipes_reader");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&puzzle.deep&&puzzle.builtOn().equals(java.util.List.of(ResonantPipes.EARLY)),"the deep rooms, offered once the resonant pipe is solved");
                ResonantPipes.buildDeep(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(stage(l,b,ResonantPipes.X_MH)==1&&stage(l,b,ResonantPipes.X_HL)==1&&stage(l,b,ResonantPipes.X_HM)==0&&stage(l,b,ResonantPipes.V_LOOP)==0,"two cross pipes open, one shut, and the loop's valve shut");
                h.assertTrue(heard(l,b,place,0).equals(Set.of('L','M','H'))&&heard(l,b,place,1).equals(Set.of('L','M','H'))&&heard(l,b,place,2).isEmpty(),"the cross pipes mix all three into the low and middle runs, and nothing reaches the high reeds");
                h.assertTrue(held(f,p,place),"the far door is held");
                stand(p,l,b,-3.5,0,-27.5);})
            .thenWaitUntil(()->h.assertTrue(stage(l,b,ResonantPipes.R_L)==2&&stage(l,b,ResonantPipes.R_M)==2&&stage(l,b,ResonantPipes.R_H)==0,"with a reader in the rooms two reeds buzz and the high reeds are still"))
            .thenExecute(()->{
                h.assertTrue(stage(l,b,new BlockPos(2,5,-22))==1&&stage(l,b,new BlockPos(2,2,-28))==0,"the high note climbs the loop over the ceiling and stops at the shut valve");
                h.assertTrue(press(l,p,b.offset(ResonantPipes.X_MH))&&stage(l,b,ResonantPipes.X_MH)==0,"the first room's cross pipe shut");
                h.assertTrue(heard(l,b,place,1).equals(Set.of('M'))&&heard(l,b,place,0).equals(Set.of('L','H')),"the middle run clean; the high note still leaks into the low run");
                h.assertTrue(press(l,p,b.offset(ResonantPipes.V_LOOP))&&stage(l,b,ResonantPipes.V_LOOP)==1,"the loop's valve opened");
                h.assertTrue(heard(l,b,place,2).equals(Set.of('L','H')),"the high reeds hear the low note with their own, carried across the second room");
                h.assertTrue(press(l,p,b.offset(ResonantPipes.X_HM))&&heard(l,b,place,1).equals(Set.of('L','M','H')),"opening the other cross pipe in the second room spoils the middle reeds too");
                ResonantPipes.update(l,b,place,1);
                h.assertTrue(stage(l,b,ResonantPipes.R_M)==2&&stage(l,b,new BlockPos(2,2,-28))==1&&!PuzzleRooms.solved(f.data(),place),"so they buzz, the pipe to the high reeds trembles, and nothing lifts");
                h.assertTrue(press(l,p,b.offset(ResonantPipes.X_HM))&&stage(l,b,ResonantPipes.X_HM)==0,"shut again");
                h.assertTrue(press(l,p,b.offset(ResonantPipes.X_HL))&&stage(l,b,ResonantPipes.X_HL)==0,"and the cross pipe over the second room shut");
                h.assertTrue(heard(l,b,place,0).equals(Set.of('L'))&&heard(l,b,place,1).equals(Set.of('M'))&&heard(l,b,place,2).equals(Set.of('H')),"each note alone at its own reeds");})
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"the three reeds sing together and the catch lifts"))
            .thenExecute(()->{
                h.assertTrue(stage(l,b,ResonantPipes.R_L)==1&&stage(l,b,ResonantPipes.R_M)==1&&stage(l,b,ResonantPipes.R_H)==1&&stage(l,b,ResonantPipes.D_LATCH)==1&&!held(f,p,place),"all three singing, the catch up, the far door free");
                ResonantPipes.buildDeep(l,b);ResonantPipes.update(l,b,place,2);
                h.assertTrue(stage(l,b,ResonantPipes.V_LOOP)==1&&stage(l,b,ResonantPipes.R_H)==1&&stage(l,b,ResonantPipes.D_LATCH)==1,"built again, solved: the loop open, each reed singing, the catch up");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }
}
