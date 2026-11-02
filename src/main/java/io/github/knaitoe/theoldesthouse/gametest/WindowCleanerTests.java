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
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.251, the Window Cleaner (docs/WINDOW_CLEANER_0_4_251.md), played with a real reader's hands: the winch won't turn with the brake on;
 * brake off, each turn of the drum lets the cradle down a block past the lit windows; at the bottom the cleaner opens the corridor's
 * casement and steps in, walks the corridor and climbs the stair the reader came up by, comes through the plant room's door, thanks
 * the reader and goes out by the staff door; once it has shut there is nobody behind it, and both staff doors stand open.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class WindowCleanerTests {
    private WindowCleanerTests(){}
    private static LiteraryTests.Fixture active,activeOne;
    @AfterBatch(batch="window_cleaner") public static void done(ServerLevel l){if(active!=null){for(var a:active.in.getEntitiesOfClass(LiteraryActor.class,room(active.base(WindowCleaner.PLACE),WindowCleaner.PLACE)))a.discard();active.close();active=null;}}
    @AfterBatch(batch="window_cleaner_one") public static void doneOne(ServerLevel l){if(activeOne!=null){for(var a:activeOne.in.getEntitiesOfClass(LiteraryActor.class,room(activeOne.base(WindowCleaner.PLACE),WindowCleaner.PLACE)))a.discard();activeOne.close();activeOne=null;}}
    /** The room's own space, and nobody else's slot. */
    static AABB room(BlockPos b,LabyrinthPlace place){var r=place.room();return new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);}
    private static void hold(LiteraryTests.Fixture f,LabyrinthPlace place){var slot=LabyrinthPlaces.slotBounds(f.origin,place);f.chunks.hold(f.in,new AABB(slot.minX(),slot.minY(),slot.minZ(),slot.maxX()+1,slot.maxY()+1,slot.maxZ()+1));}
    private static boolean press(ServerLevel l,ServerPlayer p,BlockPos at){return l.getBlockState(at).useWithoutItem(l,p,new BlockHitResult(at.getCenter(),Direction.UP,at,false)).consumesAction();}
    private static void stand(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z,float yaw){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,yaw,0);p.setYRot(yaw);p.setYHeadRot(yaw);p.setXRot(0);p.setNoGravity(true);}
    private static boolean shut(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN);}
    private static boolean opened(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    /** Where the cleaner is, relative to the room, and the room's saved state, for a failure's message. */
    private static String where(ServerLevel l,BlockPos b){var a=WindowCleaner.cleanerFor(l);return (a==null?"nobody":String.format("at %.2f %.2f %.2f",a.getX()-b.getX(),a.getY()-b.getY(),a.getZ()-b.getZ()))+" "+WindowCleaner.debug(l);}
    private static boolean casementOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(WindowCleaner.CASEMENT));return WinchBlock.is(s,WinchBlock.Kind.CASEMENT)&&s.getValue(WinchBlock.STAGE)==1;}

    @GameTest(template="empty",batch="window_cleaner",timeoutTicks=1600)
    public static void theWindowCleanerComesDownTheWellAndRoundThroughTheReadersOwnDoor(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);active=f;var place=WindowCleaner.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"cleaner_reader");f.players.add(p);
        var drum=b.offset(WindowCleaner.DRUM);var brake=b.offset(WindowCleaner.BRAKE);var far=b.offset(PuzzleRooms.farDoor(place));var inner=b.offset(WindowCleaner.INNER_DOOR);
        double[] highest={-1};boolean[] corridor={false};
        var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                var puzzle=PuzzleRooms.Puzzle.of(place);
                h.assertTrue(puzzle!=null&&!puzzle.deep&&puzzle.family==PuzzleRooms.Family.WINCH&&!place.isVignette()&&place.authored()&&WitnessAccount.Story.of(place.id())==null,"an early puzzle room of the winch, built whole, and no story");
                WindowCleaner.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(l.getBlockState(far).is(Blocks.IRON_DOOR)&&shut(l,far)&&l.getBlockState(inner).is(Blocks.IRON_DOOR)&&shut(l,inner),"both staff doors are iron and shut, with no handle on this side");
                h.assertTrue(WindowCleaner.cradle(l)==WindowCleaner.TOP&&WinchBlock.is(l.getBlockState(b.offset(WindowCleaner.CRADLE_X,WindowCleaner.TOP,WindowCleaner.CRADLE_Z)),WinchBlock.Kind.CRADLE)&&!casementOpen(l,b),"the cradle hangs at the top of the well and the casement is shut");
                stand(p,l,b,10.5,WindowCleaner.ROOM_FLOOR,-6.5,90);
            })
            .thenWaitUntil(()->h.assertTrue(WindowCleaner.cleanerFor(l)!=null,"the cleaner is out there in the cradle once someone is in the room"))
            .thenExecute(()->{
                var a=WindowCleaner.cleanerFor(l);
                h.assertTrue(Math.abs(a.getY()-(b.getY()+WindowCleaner.TOP+1))<.01,"standing on the cradle's slats");
                h.assertTrue(press(l,p,drum)&&WindowCleaner.cradle(l)==WindowCleaner.TOP,"the handle won't turn with the brake on");
                h.assertTrue(press(l,p,brake)&&WindowCleaner.stage(l)==WindowCleaner.LOWERING&&l.getBlockState(brake).getValue(WinchBlock.STAGE)==1,"the brake comes off");
            });
        // Fifteen turns of the drum, a click at a time, from the top to the corridor's casement.
        for(int i=0;i<WindowCleaner.TOP-WindowCleaner.BOTTOM;i++){
            int expected=WindowCleaner.TOP-i-1;
            seq.thenExecuteAfter(5,()->{
                h.assertTrue(press(l,p,drum)&&WindowCleaner.cradle(l)==expected,"each turn lets the cradle down a block: "+expected);
                var a=WindowCleaner.cleanerFor(l);h.assertTrue(a!=null&&Math.abs(a.getY()-(b.getY()+expected+1))<.01,"the cleaner comes down with it");
                if(expected+1==WindowCleaner.KITCHEN)h.assertTrue(WinchBlock.is(l.getBlockState(b.offset(WindowCleaner.CRADLE_X,expected,WindowCleaner.CRADLE_Z)),WinchBlock.Kind.CRADLE)&&l.getBlockState(b.offset(WindowCleaner.CRADLE_X,expected+2,WindowCleaner.CRADLE_Z)).isAir()&&WinchBlock.is(l.getBlockState(b.offset(WindowCleaner.CRADLE_X,expected+2,WindowCleaner.CRADLE_Z-1)),WinchBlock.Kind.CABLE),"passing the kitchen window: the cradle, and the cables above it");
            });
        }
        seq.thenExecute(()->{
                h.assertTrue(WindowCleaner.stage(l)==WindowCleaner.RETURNING,"at the bottom the cleaner says they'll come round");
                h.assertTrue(press(l,p,drum)&&WindowCleaner.cradle(l)==WindowCleaner.BOTTOM,"the drum turns freely now");
            })
            .thenWaitUntil(()->h.assertTrue(casementOpen(l,b),"the casement opens from outside"))
            .thenWaitUntil(()->{var a=WindowCleaner.cleanerFor(l);h.assertTrue(a!=null&&a.getX()<b.getX()+2,"the cleaner steps off into the corridor: "+where(l,b));corridor[0]=true;})
            .thenWaitUntil(()->h.assertTrue(!casementOpen(l,b),"and shuts the casement behind them"+": "+where(l,b)))
            .thenWaitUntil(()->{var a=WindowCleaner.cleanerFor(l);if(a!=null)highest[0]=Math.max(highest[0],a.getY()-b.getY());h.assertTrue(a!=null&&a.getY()>b.getY()+5&&a.getX()>b.getX()+6&&a.getZ()<b.getZ()-11,"up the stair the reader came by"+": "+where(l,b));})
            .thenWaitUntil(()->h.assertTrue(WindowCleaner.stage(l)==WindowCleaner.THANKING,"through the plant room's own door and into the room"+": "+where(l,b)))
            .thenExecute(()->{
                var a=WindowCleaner.cleanerFor(l);
                h.assertTrue(a!=null&&a.getY()>=b.getY()+WindowCleaner.ROOM_FLOOR-.1&&a.getX()>b.getX()+9&&a.getX()<b.getX()+14&&a.getZ()<b.getZ()-3&&a.getZ()>b.getZ()-10,"standing in the plant room");
                h.assertTrue(shut(l,b.offset(WindowCleaner.ROOM_DOOR)),"the plant room's door shut behind them");
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not solved while they are here");
            })
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"the cleaner leaves by the staff door, and the room is solved"+": "+where(l,b)))
            .thenExecute(()->{
                h.assertTrue(corridor[0]&&highest[0]>5,"the whole way round was walked: the corridor, then up the stair");
                h.assertTrue(WindowCleaner.cleanerFor(l)==null&&WindowCleaner.stage(l)==WindowCleaner.GONE,"there is nobody behind the door");
                h.assertTrue(l.getEntitiesOfClass(LiteraryActor.class,room(b,place)).isEmpty(),"nobody anywhere in the room");
                h.assertTrue(opened(l,far)&&opened(l,inner),"both staff doors stand open");
                WindowCleaner.build(l,b);
                h.assertTrue(opened(l,far)&&opened(l,inner)&&WindowCleaner.cradle(l)==WindowCleaner.BOTTOM&&WindowCleaner.stage(l)==WindowCleaner.GONE&&l.getBlockState(brake).getValue(WinchBlock.STAGE)==1,"built again, the doors stand open and the empty cradle is at the bottom");
                h.assertTrue(WinchBlock.is(l.getBlockState(b.offset(WindowCleaner.CRADLE_X,WindowCleaner.BOTTOM,WindowCleaner.CRADLE_Z)),WinchBlock.Kind.CRADLE),"the cradle is drawn at the bottom");
            })
            .thenSucceed();
    }

    /**
     * 0.4.263 (the owner: "the glass panels aren't appropriately faced. NPCs are jumbled together."). The windows are glass joined across
     * their openings, not bare posts; and a second cleaner standing in the cradle, as the old one did when its entities arrived after
     * the room had made a new one, is let go, leaving one cleaner with one line over one head.
     */
    @GameTest(template="empty",batch="window_cleaner_one",timeoutTicks=600)
    public static void theRoomKeepsOneCleanerAndItsWindowsAreGlassAcross(GameTestHelper h){
        var f=new LiteraryTests.Fixture(h);activeOne=f;var place=WindowCleaner.PLACE;var b=f.base(place);var l=f.in;hold(f,place);
        var p=NativeTestPlayers.survival(h,"cleaner_counter");f.players.add(p);
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the room is resident"))
            .thenExecute(()->{
                WindowCleaner.build(l,b);LabyrinthBuilder.registerDoors(f.data(),place,b);
                for(int y:new int[]{5,6,9,10,13,14}){var pane=l.getBlockState(b.offset(2,y,-6));h.assertTrue(pane.is(Blocks.GLASS_PANE)&&pane.getValue(IronBarsBlock.NORTH)&&pane.getValue(IronBarsBlock.SOUTH),"the well's window at "+y+" is glass across its opening: "+pane);}
                var big=l.getBlockState(b.offset(8,12,-6));h.assertTrue(big.is(Blocks.GLASS_PANE)&&big.getValue(IronBarsBlock.NORTH)&&big.getValue(IronBarsBlock.SOUTH),"the plant room's window is one run of glass: "+big);
                var stair=l.getBlockState(b.offset(5,4,-11));h.assertTrue(stair.is(Blocks.GLASS_PANE)&&stair.getValue(IronBarsBlock.EAST)&&stair.getValue(IronBarsBlock.WEST),"the stair's window is glass across: "+stair);
                stand(p,l,b,10.5,WindowCleaner.ROOM_FLOOR,-6.5,90);})
            .thenWaitUntil(()->h.assertTrue(WindowCleaner.cleanerFor(l)!=null,"the cleaner is out there in the cradle"))
            .thenExecute(()->{
                var a=WindowCleaner.cleanerFor(l);var extra=LiteraryRegistry.ACTOR.get().create(l);h.assertTrue(extra!=null,"a second cleaner can be made");
                extra.bind(place,null);extra.appearance(LiteraryActor.STRANGER,WindowCleaner.CLEANER);extra.setNoGravity(true);extra.moveTo(a.getX(),a.getY(),a.getZ(),90,0);
                extra.say("There's an emergency winch by your window.");h.assertTrue(l.addFreshEntity(extra),"the second cleaner stands in the cradle");})
            .thenWaitUntil(()->{
                var all=l.getEntitiesOfClass(LiteraryActor.class,room(b,place),x->x.isAlive()&&x.role()==LiteraryActor.STRANGER&&x.phase()==WindowCleaner.CLEANER);
                h.assertTrue(all.size()==1&&all.getFirst()==WindowCleaner.cleanerFor(l),"one cleaner in the cradle, the room's own: "+all.size());})
            .thenSucceed();
    }
}
