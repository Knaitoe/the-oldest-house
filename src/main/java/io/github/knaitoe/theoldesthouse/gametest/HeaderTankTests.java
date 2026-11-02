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
 * 0.4.256, the Header Tank (docs/HEADER_TANK_0_4_256.md), played with a real reader's hands. The early room: the feed valve lets the
 * tank down into the pit a layer at a time and the platform rises with it; the pump sends it back; whoever stands on the platform rides
 * it; too much water and it rides past the gallery, too little and it is out of reach; level with the old watermark it carries the
 * reader across to the doorway. The deep rooms: the near platform set at the landing from the entry, then from the landing the transfer
 * valve runs the near pit into the deeper far one, either way, until the far platform reaches the doorway.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class HeaderTankTests {
    private HeaderTankTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="header_tank") public static void done(ServerLevel l){if(active!=null){active.close();active=null;}HeaderTanks.clearAll();}
    @AfterBatch(batch="header_tanks") public static void deepDone(ServerLevel l){if(active!=null){active.close();active=null;}HeaderTanks.clearAll();}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,0,0);}
    /** Where a reader stands on a pit's platform at its present level. */
    private static double top(ServerLevel l,BlockPos b,HeaderTanks.Pit pit){return pit.floor()+2+HeaderTanks.level(l,b,pit);}

    @GameTest(template="empty",batch="header_tank",timeoutTicks=1200)
    public static void theHeaderTanksPlatformCarriesAReaderAcrossOnlyAtTheLanding(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=HeaderTanks.EARLY;var b=f.base(place);var l=f.in;hold(f,place);var pit=HeaderTanks.EARLY_PIT;
        var p=NativeTestPlayers.survival(h,"tank_reader");f.players.add(p);
        var valve=b.offset(HeaderTanks.EARLY_VALVE);var pump=b.offset(HeaderTanks.EARLY_PUMP);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.WATER&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of the header tanks, built whole, and no story");
                HeaderTanks.buildEarly(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var far=b.offset(PuzzleRooms.farDoor(place));h.assertTrue(l.getBlockState(far).getBlock() instanceof DoorBlock&&!l.getBlockState(far).is(Blocks.IRON_DOOR),"the far door is an ordinary door: the pit is what's in the way");
                h.assertTrue(HeaderTanks.level(l,b,pit)==1&&HeaderTanks.tankLevel(l,b,place)==HeaderTanks.TANK_FULL&&!HeaderTanks.valveOpen(l,b,place),"a layer in the pit, the tank full, the valve shut");
                h.assertTrue(l.getBlockState(b.offset(0,-5,-7)).is(Blocks.WATER)&&TankBlock.is(l.getBlockState(b.offset(0,-4,-7)),TankBlock.Kind.PLATFORM)&&TankBlock.is(l.getBlockState(b.offset(-2,-4,-9)),TankBlock.Kind.PLATFORM),"the platform floats on the water, across the whole pit");
                h.assertTrue(l.getBlockState(b.offset(3,-2,-8)).is(PuzzleRegistry.tankMaterial("mark"))&&l.getBlockState(b.offset(2,-3,-4)).is(Blocks.LADDER),"an old watermark at the landing's level, and a ladder set into the near wall");
                h.assertTrue(l.getBlockState(b.offset(HeaderTanks.EARLY_TANK)).is(Blocks.WATER)&&l.getBlockState(b.offset(1,9,-2)).is(Blocks.GLASS)&&l.getBlockState(b.offset(0,6,-2)).isAir(),"the attic tank holds its water behind a sight glass, over an open hatch");
                stand(p,l,b,3.5,0,-2.5);
                press(l,p,valve);h.assertTrue(!HeaderTanks.valveOpen(l,b,place),"the first touch of the valve explains it");
                h.assertTrue(press(l,p,valve)&&HeaderTanks.valveOpen(l,b,place),"the wheel turned: water runs down the pipe");})
            .thenWaitUntil(()->h.assertTrue(HeaderTanks.level(l,b,pit)>=4,"the pit fills a layer at a time and the platform rises with it"))
            .thenExecute(()->{
                h.assertTrue(press(l,p,valve)&&!HeaderTanks.valveOpen(l,b,place),"the wheel turned back");
                h.assertTrue(HeaderTanks.level(l,b,pit)==4&&HeaderTanks.tankLevel(l,b,place)==1,"what left the tank is in the pit: "+HeaderTanks.level(l,b,pit)+" and "+HeaderTanks.tankLevel(l,b,place));
                h.assertTrue(press(l,p,pump)&&HeaderTanks.level(l,b,pit)==3&&HeaderTanks.tankLevel(l,b,place)==2,"a stroke of the pump sends a layer back up");
                stand(p,l,b,.5,top(l,b,pit),-5.5);})
            .thenIdle(12)
            .thenExecute(()->{
                double before=p.getY();
                h.assertTrue(press(l,p,pump)&&HeaderTanks.level(l,b,pit)==2,"another stroke");
                h.assertTrue(Math.abs(p.getY()-(before-1))<.01,"whoever stands on the platform goes down with it: "+(p.getY()-b.getY()));
                h.assertTrue(press(l,p,valve)&&HeaderTanks.valveOpen(l,b,place),"the valve open again, and left open");})
            .thenWaitUntil(()->h.assertTrue(HeaderTanks.tankLevel(l,b,place)==0,"the tank runs dry"))
            .thenExecute(()->{
                h.assertTrue(HeaderTanks.level(l,b,pit)==5&&Math.abs(p.getY()-(b.getY()+top(l,b,pit)))<.01,"too much water: the platform has ridden past the gallery, and its rider with it");
                stand(p,l,b,.5,top(l,b,pit),-8.5);})
            .thenIdle(12)
            .thenExecute(()->{
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"past the gallery the platform doesn't meet the doorway");
                h.assertTrue(press(l,p,valve)&&!HeaderTanks.valveOpen(l,b,place),"shut");
                h.assertTrue(press(l,p,pump)&&HeaderTanks.level(l,b,pit)==4,"a stroke of the pump brings it level with the gallery and the old watermark");})
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"level with the landing, the platform carries the reader across to the doorway"))
            .thenExecute(()->{
                HeaderTanks.buildEarly(l,b);
                h.assertTrue(HeaderTanks.level(l,b,pit)==4&&HeaderTanks.tankLevel(l,b,place)==1,"built again, solved: the platform level with the gallery");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="header_tanks",timeoutTicks=1600)
    public static void theHeaderTanksRouteRunsThroughBothPitsInTurn(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=HeaderTanks.DEEP;var b=f.base(place);var l=f.in;hold(f,place);
        var near=HeaderTanks.NEAR;var far=HeaderTanks.FAR;
        var p=NativeTestPlayers.survival(h,"tanks_reader");f.players.add(p);
        var valve=b.offset(HeaderTanks.DEEP_VALVE);var transfer=b.offset(HeaderTanks.TRANSFER_AT);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the rooms are resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&puzzle.deep&&puzzle.builtOn().equals(java.util.List.of(HeaderTanks.EARLY)),"the deep rooms, offered once the header tank is solved");
                HeaderTanks.buildDeep(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(HeaderTanks.level(l,b,near)==1&&HeaderTanks.level(l,b,far)==1&&HeaderTanks.tankLevel(l,b,place)==HeaderTanks.TANK_FULL&&HeaderTanks.transfer(l,b)==0,"a layer in each pit, the tank full, the transfer valve shut");
                h.assertTrue(HeaderTanks.TANK_FULL<(near.lowest()-1)+(far.lowest()-1),"the tank holds too little to set both platforms by itself");
                stand(p,l,b,-3.5,0,-2.5);press(l,p,valve);
                h.assertTrue(press(l,p,valve)&&HeaderTanks.valveOpen(l,b,place),"the feed valve open: it fills the near pit only");})
            .thenWaitUntil(()->h.assertTrue(HeaderTanks.level(l,b,near)>=4,"the near platform rises to the landing"))
            .thenExecute(()->{
                h.assertTrue(press(l,p,valve)&&HeaderTanks.level(l,b,near)==4&&HeaderTanks.level(l,b,far)==1&&HeaderTanks.tankLevel(l,b,place)==1,"shut, with the near platform at the landing and the far untouched");
                // Across the near platform to the landing.
                stand(p,l,b,.5,top(l,b,near),-6.5);stand(p,l,b,.5,0,-9.5);
                press(l,p,transfer);h.assertTrue(HeaderTanks.transfer(l,b)==0,"the first touch of the transfer valve explains it");
                h.assertTrue(press(l,p,transfer)&&HeaderTanks.transfer(l,b)==1,"a hand turns it toward the far pit");})
            .thenWaitUntil(()->h.assertTrue(HeaderTanks.level(l,b,near)==0,"the near pit runs into the far one"))
            .thenExecute(()->{
                h.assertTrue(HeaderTanks.level(l,b,far)==5,"raising one lowers the other: all four layers moved across, "+HeaderTanks.level(l,b,far));
                p.setShiftKeyDown(true);h.assertTrue(press(l,p,transfer)&&HeaderTanks.transfer(l,b)==2,"a crouching hand turns it back toward the near pit");})
            .thenWaitUntil(()->h.assertTrue(HeaderTanks.level(l,b,near)>=1,"water runs back"))
            .thenExecute(()->{
                h.assertTrue(press(l,p,transfer)&&HeaderTanks.transfer(l,b)==0,"and again to shut it");
                h.assertTrue(HeaderTanks.level(l,b,near)==1&&HeaderTanks.level(l,b,far)==4,"every move can be undone");
                p.setShiftKeyDown(false);h.assertTrue(press(l,p,transfer)&&HeaderTanks.transfer(l,b)==1,"toward the far pit again");})
            .thenWaitUntil(()->h.assertTrue(HeaderTanks.level(l,b,near)==0&&HeaderTanks.level(l,b,far)==5,"the far platform a step below the landing"))
            .thenExecute(()->stand(p,l,b,.5,top(l,b,far),-13.5))
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"the far platform carries the reader across to the doorway"))
            .thenExecute(()->{
                HeaderTanks.buildDeep(l,b);
                h.assertTrue(HeaderTanks.level(l,b,near)==4&&HeaderTanks.level(l,b,far)==6,"built again, solved: both platforms set for the way through");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"and nothing is earned");})
            .thenSucceed();
    }
}
