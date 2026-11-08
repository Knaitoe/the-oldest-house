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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.361, the Settling Floor (docs/SETTLING_FLOOR_0_4_361.md), with a real reader's weight. Standing a moment does nothing, three
 * seconds takes a section down a step, and the step stays when the reader goes and comes back. Two steps down, the skirting opens and
 * the air goes out under the entry door. The cabinet, pushed onto the boards, settles them faster than a reader. Eleven steps leave the
 * far wall plaster; the twelfth shifts its panel, the air turns toward it, and pressed it gives.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class SettlingTests {
    private SettlingTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="settling_floor") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);}
    private static void depth(ServerLevel l,BlockPos b,int section,int d){for(var c:SettlingFloor.boards(section))l.setBlock(b.offset(c),SettlingBlock.of(SettlingBlock.Kind.BOARD,Direction.NORTH,d),3);}
    private static int sheet(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(SettlingFloor.SHEETS[0]));return DraftBlock.is(s,DraftBlock.Kind.SHEET)?s.getValue(DraftBlock.STAGE):-1;}
    private static Direction sheetFacing(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(SettlingFloor.SHEETS[0])).getValue(HorizontalDirectionalBlock.FACING);}
    private static boolean held(LiteraryTests.Fixture f,ServerPlayer p,LabyrinthPlace place){return PuzzleRooms.holds(p,f.data().door(place.id()+"/far"));}

    @GameTest(template="empty",batch="settling_floor",timeoutTicks=1600)
    public static void theBoardsSettleUnderWeightUntilTheFarPanelShifts(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=SettlingFloor.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var id=java.util.UUID.randomUUID();var r=new ServerPlayer[]{NativeTestPlayers.survival(h,"settling_reader",id)};f.players.add(r[0]);
        long[] from={0};
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the gallery is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&puzzle.deep&&puzzle.family==PuzzleRooms.Family.AIR&&puzzle.builtOn().equals(java.util.List.of(LabyrinthPlace.DRAFT_ROOM,LabyrinthPlace.PROPPED_DOOR))&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,
                    "a deep room built on the air and the traces, built whole, and no story");
                SettlingFloor.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(SettlingFloor.total(l,b)==0&&SettlingFloor.flow(l,b)==SettlingFloor.Flow.STILL&&sheet(l,b)==0,"nothing settled, no gaps, the sheets hanging still");
                h.assertTrue(SettlingFloor.panel(l,b)==0&&SettlingFloor.CABINET_START.equals(SettlingFloor.cabinet(l,b)),"the far wall plain plaster, the cabinet standing on the runner");
                var far=l.getBlockState(b.offset(PuzzleRooms.farDoor(place)));h.assertTrue(far.is(Blocks.IRON_DOOR)&&!far.getValue(DoorBlock.OPEN)&&held(f,r[0],place),"the far door iron, shut and held");
                stand(r[0],l,b,.5,0,-26.5);
                h.assertTrue(press(l,r[0],b.offset(SettlingFloor.PANEL_AT))&&SettlingFloor.panel(l,b)==0,"pressed now, the panel is only plaster");
                stand(r[0],l,b,-1.5,0,-2.5);})
            .thenIdle(25)
            .thenExecute(()->{
                h.assertTrue(SettlingFloor.depth(l,b,0)==0,"a moment's weight settles nothing");
                stand(r[0],l,b,.5,0,-2.5);})
            .thenIdle(20)
            .thenExecute(()->{h.assertTrue(SettlingFloor.depth(l,b,0)==0,"the runner never moves, and stepping off starts the count again");stand(r[0],l,b,-1.5,0,-2.5);from[0]=h.getTick();})
            .thenWaitUntil(()->h.assertTrue(SettlingFloor.depth(l,b,0)==1,"three seconds in one place settles the section a step"))
            .thenExecute(()->{
                long took=h.getTick()-from[0];h.assertTrue(took>=50,"a reader's weight takes about three seconds: "+took);
                h.assertTrue(SettlingFloor.depth(l,b,1)==0&&SettlingFloor.total(l,b)==1,"only the section stood on");
                // Halfway through a stand on the next section, the reader drops out of the game.
                stand(r[0],l,b,1.5,0,-2.5);})
            .thenIdle(30)
            .thenExecute(()->{f.players.remove(r[0]);NativeTestPlayers.remove(r[0]);})
            .thenIdle(60)
            .thenExecute(()->{
                h.assertTrue(SettlingFloor.depth(l,b,0)==1&&SettlingFloor.depth(l,b,1)==0,"with nobody in the gallery the settled section keeps its step, and the half-stood one has none");
                r[0]=NativeTestPlayers.survival(h,"settling_reader",id);f.players.add(r[0]);stand(r[0],l,b,.5,0,-1.5);})
            .thenIdle(40)
            .thenExecute(()->{h.assertTrue(SettlingFloor.depth(l,b,0)==1&&SettlingFloor.depth(l,b,1)==0,"reconnected, the step is still there, and the stand broken off counts for nothing");stand(r[0],l,b,-1.5,0,-2.5);})
            .thenWaitUntil(()->h.assertTrue(SettlingFloor.depth(l,b,0)==2,"a second step"))
            .thenExecute(()->{
                for(var k:SettlingFloor.skirting(0))h.assertTrue(SettlingBlock.stage(l.getBlockState(b.offset(k)))==1,"two steps down, the skirting along that wall stands clear of the boards");
                h.assertTrue(SettlingBlock.stage(l.getBlockState(b.offset(SettlingFloor.skirting(2).get(0))))==0,"and nowhere else");
                h.assertTrue(SettlingFloor.flow(l,b)==SettlingFloor.Flow.OUT&&sheet(l,b)==1&&sheetFacing(l,b)==Direction.SOUTH,"air comes in at the gap and goes out under the entry door: the sheets belly toward it");
                // The cabinet, pushed off the runner onto the next west section, with nobody else on it.
                stand(r[0],l,b,1.5,0,-7.5);
                h.assertTrue(press(l,r[0],b.offset(SettlingFloor.CABINET_START))&&new BlockPos(-1,0,-8).equals(SettlingFloor.cabinet(l,b)),"a push from the east moves the cabinet a cell west, onto the boards");
                stand(r[0],l,b,.5,0,-12.5);from[0]=h.getTick();})
            .thenWaitUntil(()->h.assertTrue(SettlingFloor.depth(l,b,2)==1,"the cabinet's weight settles the boards under it"))
            .thenExecute(()->{
                long took=h.getTick()-from[0];h.assertTrue(took<=40,"faster than a reader: "+took);
                var cab=l.getBlockState(b.offset(SettlingFloor.cabinet(l,b)));h.assertTrue(SettlingBlock.stage(cab)==1,"and it goes down with them");})
            .thenWaitUntil(()->h.assertTrue(SettlingFloor.depth(l,b,2)==3,"until they will go no further"))
            .thenExecute(()->{
                // Two other sections settled as far as they go (as other readers would have left them): eleven steps in all.
                depth(l,b,1,3);depth(l,b,3,3);
                SettlingFloor.update(l,b,1);
                h.assertTrue(SettlingFloor.total(l,b)==11&&SettlingFloor.panel(l,b)==0,"eleven steps leave the far wall plaster: "+SettlingFloor.total(l,b));
                stand(r[0],l,b,-1.5,0,-2.5);})
            .thenWaitUntil(()->h.assertTrue(SettlingFloor.panel(l,b)==1,"the twelfth step shifts the panel in its frame"))
            .thenExecute(()->{
                h.assertTrue(SettlingFloor.total(l,b)==12&&SettlingFloor.flow(l,b)==SettlingFloor.Flow.SEAM&&sheet(l,b)==1&&sheetFacing(l,b)==Direction.NORTH,"daylight round its edges, and the air turns toward it");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not solved until it is pressed");
                stand(r[0],l,b,.5,0,-26.5);
                h.assertTrue(press(l,r[0],b.offset(SettlingFloor.PANEL_AT.above()))&&SettlingFloor.panel(l,b)==2&&PuzzleRooms.solved(f.data(),place),"pressed now, it gives");
                var far=l.getBlockState(b.offset(PuzzleRooms.farDoor(place)));h.assertTrue(far.getValue(DoorBlock.OPEN)&&!held(f,r[0],place),"and the far door stands open beyond it");
                SettlingFloor.build(l,b);
                h.assertTrue(SettlingFloor.panel(l,b)==2&&SettlingFloor.total(l,b)>=SettlingFloor.SEAM_AT&&SettlingFloor.flow(l,b)==SettlingFloor.Flow.SEAM&&l.getBlockState(b.offset(PuzzleRooms.farDoor(place))).getValue(DoorBlock.OPEN),
                    "built again, solved: settled enough, the panel open, the far door open");
                h.assertTrue(WitnessAccount.count(f.data(),id)==0,"and nothing is earned");})
            .thenSucceed();
    }
}
