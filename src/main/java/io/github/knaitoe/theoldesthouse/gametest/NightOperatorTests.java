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
 * 0.4.252, the Night Operator (docs/NIGHT_OPERATOR_0_4_252.md), played with a real reader's hands: the operator explains, once; a hand
 * on a jack listens and lights its lamp; nobody is put through before the caller is at a door, or to the wrong room; the right rooms in
 * order (water, a fridge, bare boards, a clock, then the exchange's own line) bring the caller through; their footsteps cross the room to
 * the far door, which opens and shuts with nobody at it; the operator hangs up their headset and the room is solved.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class NightOperatorTests {
    private NightOperatorTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="night_operator") public static void done(ServerLevel l){if(active!=null){for(var a:active.in.getEntitiesOfClass(LiteraryActor.class,WindowCleanerTests.room(active.base(NightOperator.PLACE),NightOperator.PLACE)))a.discard();active.close();active=null;}}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at,boolean crouch){p.setShiftKeyDown(crouch);try{return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.EAST,at,false)).consumesAction();}finally{p.setShiftKeyDown(false);}}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double z){p.teleportTo(l,b.getX()+x,b.getY(),b.getZ()+z,90,0);p.setYRot(90);p.setYHeadRot(90);p.setXRot(0);p.setNoGravity(true);}
    private static int stage(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.is(PuzzleRegistry.EXCHANGE.get())?s.getValue(ExchangeBlock.STAGE):-1;}
    private static boolean doorOpen(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    private static String where(ServerLevel l){return NightOperator.debug(l);}

    @GameTest(template="empty",batch="night_operator",timeoutTicks=1800)
    public static void theCallerIsPutThroughRoomByRoomAndCrossesTheExchangeUnseen(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=NightOperator.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"operator_reader");f.players.add(p);
        var far=b.offset(PuzzleRooms.farDoor(place));var caller=b.offset(NightOperator.CALLER);var hook=b.offset(NightOperator.HOOK);
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.LINE&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of the switchboard, built whole, and no story");
                NightOperator.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(l.getBlockState(far).is(Blocks.IRON_DOOR)&&!doorOpen(l,far),"the far door is iron and shut");
                for(int i=0;i<NightOperator.JACKS;i++)h.assertTrue(stage(l,b.offset(NightOperator.jack(i)))==0&&ExchangeBlock.is(l.getBlockState(b.offset(NightOperator.jack(i))),ExchangeBlock.Kind.JACK),"nine jacks, none lit: "+i);
                h.assertTrue(stage(l,caller)==1&&stage(l,hook)==0,"the incoming line's lamp is lit, and one headset hangs on the hook");
                var seen=new java.util.HashSet<NightOperator.Line>();for(int i=0;i<NightOperator.JACKS;i++)seen.add(NightOperator.lineAt(l,i));
                h.assertTrue(seen.size()==NightOperator.JACKS&&NightOperator.lineAt(l,NightOperator.jackOf(l,NightOperator.Line.WATER))==NightOperator.Line.WATER,"each jack reaches its own room, fixed for the world");
                stand(p,l,b,-3.5,-6.5);
            })
            .thenWaitUntil(()->h.assertTrue(NightOperator.operatorFor(l)!=null,"the operator is at the board once someone is in the room"))
            .thenExecute(()->{
                var a=NightOperator.operatorFor(l);
                h.assertTrue(a.phase()==NightOperator.OPERATOR,"in their headset");
                int water=NightOperator.jackOf(l,NightOperator.Line.WATER);
                h.assertTrue(press(l,p,b.offset(NightOperator.jack(water)),true)&&NightOperator.step(l)==0,"nobody is put through while the operator is still explaining");
            })
            .thenWaitUntil(()->h.assertTrue(NightOperator.ready(l),"the caller says what is behind their door: "+where(l)))
            .thenExecute(()->{
                // Listening lights the jack's lamp and puts nobody through.
                int quiet=NightOperator.jackOf(l,NightOperator.Line.QUIET);var at=b.offset(NightOperator.jack(quiet));
                h.assertTrue(press(l,p,at,false)&&stage(l,at)==1&&NightOperator.step(l)==0,"a hand on a jack listens to its room, and its lamp lights");
                // The wrong room: the caller says so and waits.
                h.assertTrue(press(l,p,at,true)&&stage(l,at)==2&&NightOperator.step(l)==0&&NightOperator.ready(l),"put through to the wrong room, the caller stays where they are");
            });
        for(int k=0;k<NightOperator.WAY.length;k++){
            int step=k;
            seq.thenWaitUntil(()->h.assertTrue(NightOperator.ready(l)&&NightOperator.step(l)==step,"the caller is at the next door: "+where(l)))
               .thenExecute(()->{
                   var line=NightOperator.WAY[step];int i=NightOperator.jackOf(l,line);var at=b.offset(NightOperator.jack(i));
                   h.assertTrue(press(l,p,at,true)&&NightOperator.step(l)==step+1&&stage(l,at)==2,"put through to the right room, they go on: "+line);
                   for(int j=0;j<NightOperator.JACKS;j++)if(j!=i)h.assertTrue(stage(l,b.offset(NightOperator.jack(j)))!=2,"the cord is in one jack only");
               });
        }
        seq.thenExecute(()->h.assertTrue(NightOperator.phase(l)==NightOperator.CROSSING,"through the exchange's own line, they are outside"))
            .thenWaitUntil(()->h.assertTrue(doorOpen(l,far)&&NightOperator.phase(l)==NightOperator.DOOR,"footsteps cross the room and the far door opens: "+where(l)))
            .thenExecute(()->h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,WindowCleanerTests.room(b,place)).size()==1,"nobody is there but the operator"))
            .thenWaitUntil(()->h.assertTrue(!doorOpen(l,far),"and shuts again"))
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"the operator hangs up and the room is solved: "+where(l)))
            .thenExecute(()->{
                var a=NightOperator.operatorFor(l);
                h.assertTrue(a!=null&&a.phase()==NightOperator.OPERATOR_BARE&&stage(l,hook)==1,"the operator's headset is on the hook beside the spare");
                h.assertTrue(doorOpen(l,far)&&stage(l,caller)==0,"the far door stands open and the incoming line is dark");
                for(int j=0;j<NightOperator.JACKS;j++)h.assertTrue(stage(l,b.offset(NightOperator.jack(j)))!=2,"the cord is out");
                h.assertTrue(press(l,p,b.offset(NightOperator.jack(0)),true)&&NightOperator.phase(l)==NightOperator.FINISHED,"the board is quiet now");
                NightOperator.build(l,b);
                h.assertTrue(doorOpen(l,far)&&stage(l,hook)==1&&stage(l,caller)==0&&NightOperator.phase(l)==NightOperator.FINISHED,"built again, it stays finished");
            })
            .thenWaitUntil(()->{var a=NightOperator.operatorFor(l);h.assertTrue(a!=null&&a.phase()==NightOperator.OPERATOR_BARE,"the operator sits at the board without the headset");})
            .thenSucceed();
    }
}
