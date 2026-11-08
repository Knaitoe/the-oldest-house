package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.363, the Cold Pantry (docs/COLD_PANTRY_0_4_363.md), worked by a real reader's hands. Cold at the start: the cold store's door
 * opens and the far door is frozen fast. Ice lifted into the tub and the shutters opened warm the room slowly; past the cold the store
 * door's seal holds from the larder but its latch always opens from inside; at the thaw the far door's frost lets go and the room is
 * solved. Left warm, the cake under its bell has gone on a stage when the reader comes back; ice back and shutters shut, the room cools
 * again and the store door opens; left cold, the cake keeps.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class PantryTests {
    private PantryTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="cold_pantry") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);}
    private static int stage(ServerLevel l,BlockPos b,BlockPos rel){return PantryBlock.stage(l.getBlockState(b.offset(rel)));}

    @GameTest(template="empty",batch="cold_pantry",timeoutTicks=2600)
    public static void thePantryWarmsByHandAndTheCakeRemembers(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=ColdPantry.PLACE;var b=f.base(place);var l=f.in;hold(f,place);var s=l.getServer();
        var id=java.util.UUID.randomUUID();var r=new ServerPlayer[]{NativeTestPlayers.survival(h,"pantry_reader",id)};f.players.add(r[0]);
        var pipe=new BlockPos(0,3,-12);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the pantry is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.COLD&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room, built whole, and no story");
                ColdPantry.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(ColdPantry.warmth(s)==0&&ColdPantry.iceInBox(l,b)==ColdPantry.ICE&&ColdPantry.openShutters(l,b)==0&&ColdPantry.target(l,b)==0,"bitter cold: the ice in its box, the shutters shut");
                h.assertTrue(stage(l,b,pipe)==3&&stage(l,b,ColdPantry.THERMO_AT)==0&&stage(l,b,ColdPantry.CAKE_AT)==0,"frost thick on the pipes, the thermometer low, the cake perfect");
                var far=f.data().door(place.id()+"/far");
                h.assertTrue(far!=null&&PuzzleRooms.holds(r[0],far)&&!PuzzleRooms.solved(f.data(),place),"the far door frozen fast");
                stand(r[0],l,b,-2.5,0,-11.5);
                h.assertTrue(press(l,r[0],b.offset(ColdPantry.STORE_AT))&&ColdPantry.storeOpen(l,b),"while it is cold the store door's seal gives");
                h.assertTrue(press(l,r[0],b.offset(ColdPantry.STORE_AT.above()))&&!ColdPantry.storeOpen(l,b),"and it shuts again");
                // Warm it: the ice into the tub, the shutters open.
                stand(r[0],l,b,3.5,0,-8.5);
                for(int i=0;i<ColdPantry.ICE;i++)h.assertTrue(press(l,r[0],b.offset(ColdPantry.ICEBOX_AT)),"a block of ice lifted out");
                h.assertTrue(ColdPantry.iceInBox(l,b)==0&&stage(l,b,ColdPantry.TUB_AT)==ColdPantry.ICE,"all the ice in the tub");
                h.assertTrue(press(l,r[0],b.offset(ColdPantry.ICEBOX_AT))&&ColdPantry.iceInBox(l,b)==0,"and the box empty");
                for(var sh:ColdPantry.SHUTTERS)h.assertTrue(press(l,r[0],b.offset(sh))&&stage(l,b,sh)==1,"a shutter pushed open");
                h.assertTrue(ColdPantry.target(l,b)==100&&ColdPantry.warmth(s)==0,"the room heading for warmth, but not there yet");})
            .thenWaitUntil(()->h.assertTrue(ColdPantry.warmth(s)>ColdPantry.COLD,"it warms, slowly: "+ColdPantry.warmth(s)))
            .thenExecute(()->{
                h.assertTrue(ColdPantry.warmth(s)<ColdPantry.THAW&&!PuzzleRooms.solved(f.data(),place),"not yet thawed");
                stand(r[0],l,b,-2.5,0,-11.5);
                h.assertTrue(press(l,r[0],b.offset(ColdPantry.STORE_AT))&&!ColdPantry.storeOpen(l,b),"from the larder the store door's seal holds now");
                stand(r[0],l,b,-2.5,0,-15.5);
                h.assertTrue(press(l,r[0],b.offset(ColdPantry.STORE_AT))&&ColdPantry.storeOpen(l,b),"but from inside the store its latch always opens: nobody is shut in");
                h.assertTrue(press(l,r[0],b.offset(ColdPantry.STORE_AT))&&!ColdPantry.storeOpen(l,b),"and it always shuts");
                stand(r[0],l,b,0.5,0,-9.5);})
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"at the thaw the far door's frost lets go: "+ColdPantry.warmth(s)))
            .thenExecute(()->{
                h.assertTrue(ColdPantry.warmth(s)>=ColdPantry.THAW&&!PuzzleRooms.holds(r[0],f.data().door(place.id()+"/far")),"the far door gives");
                h.assertTrue(stage(l,b,pipe)==0&&stage(l,b,ColdPantry.THERMO_AT)==3,"the pipes bare and wet, the thermometer high");
                // Left warm: the reader goes, and the room stands empty a while.
                f.players.remove(r[0]);NativeTestPlayers.remove(r[0]);})
            .thenIdle(ColdPantry.GAP+30)
            .thenExecute(()->{
                h.assertTrue(ColdPantry.cake(s)==0,"with nobody here nothing has been counted yet");
                r[0]=NativeTestPlayers.survival(h,"pantry_reader",id);f.players.add(r[0]);stand(r[0],l,b,0.5,0,-9.5);})
            .thenWaitUntil(()->h.assertTrue(ColdPantry.cake(s)==1,"back after it stood warm, the cake has gone on a stage"))
            .thenExecute(()->{
                h.assertTrue(stage(l,b,ColdPantry.CAKE_AT)==1&&ColdPantry.warmth(s)==100,"slumped under its bell; the room warmed on while nobody was here");
                // Cool it again: the ice back in the box, the shutters shut.
                stand(r[0],l,b,3.5,0,-8.5);
                for(int i=0;i<ColdPantry.ICE;i++)h.assertTrue(press(l,r[0],b.offset(ColdPantry.TUB_AT)),"a block of ice back in the box");
                for(var sh:ColdPantry.SHUTTERS)h.assertTrue(press(l,r[0],b.offset(sh))&&stage(l,b,sh)==0,"a shutter pulled to");
                h.assertTrue(ColdPantry.iceInBox(l,b)==ColdPantry.ICE&&ColdPantry.target(l,b)==0,"nothing used up: all three blocks back in the box");})
            .thenWaitUntil(()->h.assertTrue(ColdPantry.warmth(s)<=ColdPantry.COLD,"it cools again, as slowly: "+ColdPantry.warmth(s)))
            .thenExecute(()->{
                stand(r[0],l,b,-2.5,0,-11.5);
                h.assertTrue(press(l,r[0],b.offset(ColdPantry.STORE_AT))&&ColdPantry.storeOpen(l,b),"cold again, the store door opens from the larder");
                h.assertTrue(PuzzleRooms.solved(f.data(),place)&&!PuzzleRooms.holds(r[0],f.data().door(place.id()+"/far")),"and solved stays solved");
                f.players.remove(r[0]);NativeTestPlayers.remove(r[0]);})
            .thenIdle(ColdPantry.GAP+30)
            .thenExecute(()->{r[0]=NativeTestPlayers.survival(h,"pantry_reader",id);f.players.add(r[0]);stand(r[0],l,b,0.5,0,-9.5);})
            .thenIdle(40)
            .thenExecute(()->{
                h.assertTrue(ColdPantry.cake(s)==1&&stage(l,b,ColdPantry.CAKE_AT)==1,"left cold, the cake has kept");
                ColdPantry.build(l,b);
                h.assertTrue(stage(l,b,ColdPantry.CAKE_AT)==1&&ColdPantry.warmth(s)==0&&ColdPantry.iceInBox(l,b)==ColdPantry.ICE,"built again: cold, the ice in its box, and the cake as it was left");
                h.assertTrue(WitnessAccount.count(f.data(),id)==0,"and nothing is earned");})
            .thenSucceed();
    }
}
