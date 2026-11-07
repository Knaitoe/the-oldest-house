package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.322, the yard's exceptional crossings (docs/YARD_CROSSINGS_0_4_322.md): each tested as the discrete event it is, by a reader who
 * actually walks it. The long drive's loop is walked the whole way round, from the squeeze, round the property and back in by the road,
 * to the same wagon, its mud printed, the garage door gone up behind the reader's back and the way on through it. The porch turn is
 * walked from one porch to the other: the porch behind down the first straight, no sky over the turn, the porch ahead down the second,
 * and the door shut in front of the reader. The joists: the ditch walked to the face, the route out of reach above it, the joists where
 * the sky should be, and a car's lights running along the route and catching them, leaving nothing behind. And the dealing: once each,
 * only after the ordinary yard.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardCrossingsTests {
    private YardCrossingsTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_long_drive") public static void longDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_porch_turn") public static void porchDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_joists") public static void joistsDone(ServerLevel l){close();YardCrossings.clearAll();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h,LabyrinthPlace place){active=new LiteraryTests.Fixture(h);var a=LabyrinthPlaces.slotBounds(active.origin,place);active.chunks.hold(active.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static boolean fits(ServerLevel l,BlockPos b,double x,double z){var at=Vec3.atLowerCornerOf(b).add(x,0,z);return l.noCollision(null,new AABB(at.x-.3,at.y+.01,at.z-.3,at.x+.3,at.y+1.8,at.z+.3));}
    private static boolean sees(ServerLevel l,ServerPlayer p,Vec3 eye,BlockPos target){
        var hit=l.clip(new ClipContext(eye,Vec3.atCenterOf(target),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS||hit.getBlockPos().equals(target)||hit.getLocation().distanceTo(Vec3.atCenterOf(target))<.9;
    }
    /** A reader walking a route of points on their own feet, with gravity and collision, failing if they stall. */
    private static final class Walk {
        final List<Vec3> route=new ArrayList<>();int at=-1,stuck=0;double best=Double.MAX_VALUE;
        void start(BlockPos b,double[]... points){route.clear();for(var q:points)route.add(Vec3.atLowerCornerOf(b).add(q[0],0,q[1]));at=0;stuck=0;best=Double.MAX_VALUE;}
        boolean done(){return at>=route.size();}
        void tick(GameTestHelper h,ServerPlayer p,BlockPos b,String where){
            if(at<0||done())return;var d=route.get(at).subtract(p.position()).multiply(1,0,1);
            if(d.length()<.25){at++;best=Double.MAX_VALUE;stuck=0;return;}
            if(d.length()<best-.015){best=d.length();stuck=0;}else stuck++;
            h.assertTrue(stuck<90,where+" supports a walking body at "+p.position().subtract(Vec3.atLowerCornerOf(b))+" heading for "+route.get(at).subtract(Vec3.atLowerCornerOf(b)));
            var move=d.normalize().scale(Math.min(.25,d.length()));double vy=p.onGround()?0:(p.getDeltaMovement().y-.08)*.98;
            p.setDeltaMovement(move.x,vy,move.z);p.move(MoverType.SELF,p.getDeltaMovement());if(p.verticalCollision)p.setDeltaMovement(move.x,0,move.z);
        }
    }
    private static double[] pt(double x,double z){return new double[]{x,z};}
    /** Something over a cell that would stop the eye going up: read from the heightmap, which is current the moment a block is set. */
    private static boolean roofed(ServerLevel l,BlockPos at){return l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,at.getX(),at.getZ())>at.getY()+1;}

    @GameTest(template="empty",batch="yard_long_drive",timeoutTicks=4000)
    public static void theLongDriveComesBackToTheWagonAndTheGarageIsTheWayOn(GameTestHelper h){
        var place=YardCrossings.LONG_DRIVE;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"loop_reader");var walk=new Walk();
        h.onEachTick(()->walk.tick(h,p,b,"the long drive"));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the property is resident"))
            .thenExecute(()->{
                h.assertTrue(place.isGray()&&!place.isVignette()&&NovelRooms.outside(place)&&NovelRooms.dimension(place).equals(HouseDimensions.OUTSIDE),"an outdoor connector, not a story");
                YardCrossings.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
                // The turn-around the reader learned: the porch bulb, the wagon by the garage, the squeeze; but no creek and no gate.
                h.assertTrue(YardBlock.is(l.getBlockState(b.offset(YardRooms.BULB_AT)),YardBlock.Kind.BULB)&&YardBlock.is(l.getBlockState(b.offset(YardRooms.NOSE_AT)),YardBlock.Kind.NOSE),"the same porch bulb and the same wagon");
                h.assertTrue(!l.getBlockState(b.offset(43,-1,YardRooms.ROAD_Z)).is(Blocks.WATER),"no swollen creek at the road's end");
                h.assertTrue(!(l.getBlockState(b.offset(YardRooms.FAR_GATE)).getBlock() instanceof DoorBlock)&&fits(l,b,YardRooms.SQUEEZE_X+.5,YardRooms.FAR_GATE.getZ()+.5),"no gate in the back fence: the path goes on");
                for(int z=-21;z<=-16;z++)h.assertTrue(YardBlock.is(l.getBlockState(b.offset(YardRooms.SQUEEZE_X,-1,z)),YardBlock.Kind.PRINT)&&l.getBlockState(b.offset(YardRooms.SQUEEZE_X,-1,z)).getValue(YardBlock.STAGE)==0,"clear mud in the squeeze beside the wagon at z "+z);
                // The way on is in the garage, and the garage is shut.
                h.assertTrue(l.getBlockState(b.offset(YardCrossings.LONG_EXIT)).getBlock() instanceof DoorBlock,"a door in the garage's back wall");
                h.assertTrue(!YardCrossings.garageOpen(l,b)&&l.getBlockState(b.offset(YardCrossings.GARAGE_DOOR)).is(PuzzleRegistry.yardMaterial("garage_door")),"the garage door is down");
                h.assertTrue(f.data().door(place.id()+"/far").dimension.equals(HouseDimensions.OUTSIDE),"its doors open outside");
                at(p,l,b,.5,0,-13.5);p.setOnGround(true);
                // Out by the squeeze, through the tree line, round the property, and back in along the road.
                var route=new ArrayList<double[]>();route.add(pt(-6.5,-14.0));route.add(pt(-6.5,-15.5));route.add(pt(-6.5,-33.5));
                for(int i=1;i<YardCrossings.LOOP.length;i++)route.add(pt(YardCrossings.LOOP[i][0]+.5,YardCrossings.LOOP[i][1]+.5));
                route.add(pt(8.5,-13.5));route.add(pt(.5,-13.5));walk.start(b,route.toArray(double[][]::new));
            })
            .thenWaitUntil(()->h.assertTrue(walk.done(),"the reader walks the whole loop: "+walk.at+" of "+walk.route.size()))
            .thenExecute(()->{
                var eye=p.getEyePosition();boolean opening=sees(l,p,eye,b.offset(YardCrossings.GARAGE_DOOR).above(2));
                walk.start(b,pt(-6.5,-14.0),pt(-6.5,-19.5),pt(-10.5,-19.5),pt(-10.5,-20.6));
                var own=YardCrossings.reader(l,p,place);
                h.assertTrue(own.getBoolean("Far")&&own.getBoolean("Looped"),"round the far side and back to the apron: "+own);
                int printed=0;for(int z=-21;z<=-16;z++)if(l.getBlockState(b.offset(YardRooms.SQUEEZE_X,-1,z)).getValue(YardBlock.STAGE)==1)printed++;
                h.assertTrue(printed>=3,"the reader's own prints are in the mud beside the wagon: "+printed);
                boolean tracks=false;for(int x=57;x<=60;x++)for(int z=-42;z<YardCrossings.TRACKS_END;z++){var s=l.getBlockState(b.offset(x,-1,z));tracks|=YardBlock.is(s,YardBlock.Kind.PRINT)&&s.getValue(YardBlock.STAGE)==1;}
                h.assertTrue(tracks,"and in the clear mud where the tire tracks stop");
                h.assertTrue(YardCrossings.garageOpen(l,b)&&YardCrossings.state(l,place).getBoolean("Open"),"the garage door went up while the reader was round the far side");
                h.assertTrue(l.getBlockState(b.offset(YardCrossings.GARAGE_DOOR).above(3)).is(PuzzleRegistry.yardMaterial("garage_door")),"rolled up into the header");
                h.assertTrue(opening,"from the returned approach the top of the opening shows over the wagon");
            })
            .thenWaitUntil(()->h.assertTrue(walk.done(),"through the garage to its back door"))
            .thenExecute(()->{
                h.assertTrue(p.position().distanceTo(Vec3.atBottomCenterOf(b.offset(YardCrossings.LONG_EXIT)))<2,"the way on is reached on foot: "+p.position().subtract(Vec3.atLowerCornerOf(b))+"");
                // Gone and unwatched a while, the door comes down and the mud smooths over for the next reader.
                at(p,l,b,500,0,0);var s=YardCrossings.state(l,place);s.putLong("Empty",l.getGameTime()-YardCrossings.RESET);LabyrinthData.get(l.getServer()).setStateEntry(YardCrossings.STATE,place.id(),s);
            })
            .thenWaitUntil(()->h.assertTrue(!YardCrossings.garageOpen(l,b)&&!YardCrossings.state(l,place).getBoolean("Due"),"the garage door is down again"))
            .thenExecute(()->{for(int z=-21;z<=-16;z++)h.assertTrue(l.getBlockState(b.offset(YardRooms.SQUEEZE_X,-1,z)).getValue(YardBlock.STAGE)==0,"the mud is clear again at z "+z);})
            .thenSucceed();
    }

    @GameTest(template="empty",batch="yard_porch_turn",timeoutTicks=2400)
    public static void thePorchTurnPutsThePorchAheadOfTheReaderWalkingAwayFromIt(GameTestHelper h){
        var place=YardCrossings.PORCH_TURN;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"porch_reader");var walk=new Walk();
        h.onEachTick(()->walk.tick(h,p,b,"the porch turn"));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the yard is resident"))
            .thenExecute(()->{
                YardCrossings.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
                var bulbA=b.offset(1,2,0);var bulbB=b.offset(YardCrossings.PORCH_B_X+1,2,0);
                h.assertTrue(YardBlock.is(l.getBlockState(bulbA),YardBlock.Kind.BULB)&&YardBlock.is(l.getBlockState(bulbB),YardBlock.Kind.BULB),"two porches, each with its bulb");
                h.assertTrue(l.getBlockState(b.offset(YardCrossings.PORCH_B_X,0,1)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(0,0,1)).getBlock() instanceof DoorBlock,"each with its back door");
                var exit=f.data().door(place.id()+"/far");h.assertTrue(exit!=null&&exit.facing==Direction.NORTH,"the far porch's door is the way on, approached from the yard");
                // Walking away: the bulb behind down the first straight; the other porch hidden by the hedge.
                var eyeA=Vec3.atLowerCornerOf(b).add(.5,1.62,-14.5);
                h.assertTrue(sees(l,p,eyeA,bulbA),"looking back down the first straight, the porch light");
                h.assertTrue(!sees(l,p,eyeA,bulbB),"and not the other porch's");
                var eyeB=Vec3.atLowerCornerOf(b).add(YardCrossings.PORCH_B_X+.5,1.62,-14.5);
                h.assertTrue(sees(l,p,eyeB,bulbB)&&!sees(l,p,eyeB,bulbA),"out of the turn, the porch light ahead and only that one");
                for(int z=-15;z<=-4;z++)h.assertTrue(!fits(l,b,YardCrossings.PORCH_B_X/2.0+.5,z+.5),"the hedge between the straights is whole at z "+z);
                // No sky to steer by over the turn.
                int covered=0,cells=0;
                for(int deg=10;deg<=170;deg+=10){double a=Math.toRadians(deg);var at=b.offset((int)Math.round(YardCrossings.PORCH_B_X/2.0+YardCrossings.TURN_R*Math.cos(a)),1,(int)Math.round(YardCrossings.TURN_Z-YardCrossings.TURN_R*Math.sin(a)));cells++;if(roofed(l,at))covered++;}
                h.assertTrue(covered*10>=cells*8,"the branches knit over the turn: "+covered+" of "+cells);
                at(p,l,b,.5,0,-2.5);p.setOnGround(true);
                var route=new ArrayList<double[]>();route.add(pt(.5,-4.5));route.add(pt(.5,YardCrossings.TURN_Z+.5));
                for(int deg=165;deg>=15;deg-=15){double a=Math.toRadians(deg);route.add(pt(YardCrossings.PORCH_B_X/2.0+YardCrossings.TURN_R*Math.cos(a)+.5,YardCrossings.TURN_Z-YardCrossings.TURN_R*Math.sin(a)+.5));}
                route.add(pt(YardCrossings.PORCH_B_X+.5,YardCrossings.TURN_Z+.5));route.add(pt(YardCrossings.PORCH_B_X+.5,-1.5));
                walk.start(b,route.toArray(double[][]::new));
            })
            .thenWaitUntil(()->h.assertTrue(walk.done(),"the reader walks from one porch, round the turn, to the other: "+walk.at+" of "+walk.route.size()))
            .thenExecute(()->{
                var own=YardCrossings.reader(l,p,place);
                h.assertTrue(own.getBoolean("Apex")&&own.getBoolean("Turned"),"through the turn and out onto the straight with the porch ahead: "+own);
                h.assertTrue(NativeTestPlayers.sounds(p).stream().anyMatch(s->s.getSound().value()==SoundEvents.WOODEN_DOOR_CLOSE),"somebody shuts the back door ahead");
                h.assertTrue(p.position().distanceTo(Vec3.atBottomCenterOf(b.offset(YardCrossings.PORCH_B_X,0,1)))<3.5,"the way on is reached on foot: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="yard_joists",timeoutTicks=2400)
    public static void theJoistsHangOverTheStateRouteAndAPassingCarCatchesThem(GameTestHelper h){
        var place=YardCrossings.JOISTS;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"joist_reader");var walk=new Walk();
        h.onEachTick(()->walk.tick(h,p,b,"the ditch"));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the ditch is resident"))
            .thenExecute(()->{
                YardCrossings.clearAll();YardCrossings.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
                for(int z=-4;z>=-42;z-=6){
                    h.assertTrue(roofed(l,b.offset(0,1,z))&&roofed(l,b.offset(YardCrossings.ROUTE_X,YardCrossings.ROUTE_Y+1,z)),"no sky over the ditch or the route at z "+z);
                    h.assertTrue(l.getBlockState(b.offset(0,YardCrossings.FLOOR_Y,z)).is(Blocks.SPRUCE_PLANKS),"a subfloor where the sky should be at z "+z);
                    // The route just above, out of reach: a bank two high at its foot, roots, the retaining face.
                    h.assertTrue(fits(l,b,2.5,z+.5)&&!l.getBlockState(b.offset(3,0,z)).isAir()&&!l.getBlockState(b.offset(3,1,z)).isAir(),"the bank rises two high from the verge at z "+z);
                    h.assertTrue(l.getBlockState(b.offset(5,YardCrossings.ROUTE_Y,z)).is(Blocks.DARK_OAK_LOG)&&l.getBlockState(b.offset(YardCrossings.ROUTE_X,YardCrossings.ROUTE_Y,z)).isSolid(),"the retaining face and the route on top of it at z "+z);
                }
                int joists=0;for(int z=0;z>=-44;z--)if(l.getBlockState(b.offset(0,YardCrossings.JOIST_Y,z)).is(Blocks.DARK_OAK_LOG))joists++;
                h.assertTrue(joists>=14,"joists across the ceiling: "+joists);
                h.assertTrue(l.getBlockState(b.offset(-4,YardCrossings.JOIST_Y-1,-20)).is(Blocks.LIGHTNING_ROD),"a copper run along them");
                h.assertTrue(l.getBlockState(b.offset(YardCrossings.JOIST_EXIT)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(-3,1,YardCrossings.FACE_Z)).is(Blocks.DARK_OAK_LOG),"the cedars end at a face of joists with a door in it");
                at(p,l,b,.5,0,-3.5);p.setOnGround(true);walk.start(b,pt(.5,-12.5),pt(.5,-30.5),pt(.5,-43.6));
            })
            .thenWaitUntil(()->h.assertTrue(walk.done(),"the reader walks the ditch to the face: "+walk.at+" of "+walk.route.size()))
            .thenExecute(()->{
                h.assertTrue(p.position().distanceTo(Vec3.atBottomCenterOf(b.offset(YardCrossings.JOIST_EXIT)))<2,"the way on is reached on foot: "+p.position().subtract(Vec3.atLowerCornerOf(b)));
                boolean printed=false;for(int z=-13;z>=-20;z--)for(int x=-1;x<=1;x++){var s=l.getBlockState(b.offset(x,-1,z));printed|=YardBlock.is(s,YardBlock.Kind.PRINT)&&s.getValue(YardBlock.STAGE)==1;}
                h.assertTrue(printed,"prints left in the ditch's mud");
                at(p,l,b,.5,0,-20.5);YardCrossings.nextJoistCar=l.getGameTime();
            })
            .thenWaitUntil(()->{
                int z=YardCrossings.joistSweep;
                h.assertTrue(z!=-1000&&z<-14&&l.getBlockState(b.offset(YardCrossings.ROUTE_X,YardCrossings.BEAM_Y,z)).is(Blocks.LIGHT),"a car passes along the route, its lights thrown up under the joists");
                int lit=0;for(int k=z-1;k<=z+3;k++)lit=Math.max(lit,l.getBrightness(LightLayer.BLOCK,b.offset(YardCrossings.ROUTE_X,YardCrossings.JOIST_Y-1,k)));
                h.assertTrue(lit>=12,"and the joists over the route catch it at z "+z+": "+lit);
            })
            .thenWaitUntil(()->h.assertTrue(YardCrossings.joistSweep==-1000,"and passes"))
            .thenExecute(()->{
                for(int z=YardCrossings.SWEEP_SOUTH+1;z>=YardCrossings.SWEEP_NORTH-1;z--)for(int y:new int[]{YardCrossings.ROUTE_Y+1,YardCrossings.BEAM_Y})
                    h.assertTrue(!l.getBlockState(b.offset(YardCrossings.ROUTE_X,y,z)).is(Blocks.LIGHT),"its light leaves nothing behind at z "+z);
                h.assertTrue(NativeTestPlayers.sounds(p).stream().anyMatch(s->s.getSound().value()==PuzzleRegistry.YARD_TRAFFIC.get()),"heard going by, close, above");
                h.assertTrue(YardCrossings.reader(l,p,place).getBoolean("Lit"),"the reader was there for it");
            })
            .thenSucceed();
    }

    @GameTest(template="empty",batch="yard_crossings_dealing")
    public static void theExceptionalCrossingsComeOnceAndOnlyAfterTheOrdinaryYard(GameTestHelper h){
        var data=new LabyrinthData();var u=UUID.randomUUID();int story=LabyrinthPacing.STORY_DEPTH,deep=LabyrinthPacing.DEEP_DEPTH;
        for(var place:YardCrossings.PLACES){
            h.assertTrue(place.isGray()&&NovelRooms.outside(place)&&LabyrinthDealer.grayAvailable(data,u).contains(place),"an outdoor connector in the deal: "+place);
            h.assertTrue(LabyrinthDealer.grayWeight(place,deep)==0,"never from the shared hall cache: "+place);
            h.assertTrue(LabyrinthDealer.grayWeight(data,u,place,deep)==0,"not before the ordinary yard: "+place);
        }
        data.visit(u,LabyrinthPlace.YARD_DITCH);
        h.assertTrue(LabyrinthDealer.grayWeight(data,u,YardCrossings.LONG_DRIVE,story)==0,"the long drive needs the turn-around learned first");
        h.assertTrue(LabyrinthDealer.grayWeight(data,u,YardCrossings.PORCH_TURN,story)==0,"the porch turn needs two of the ordinary yard's crossings");
        h.assertTrue(LabyrinthDealer.grayWeight(data,u,YardCrossings.JOISTS,story)==0&&LabyrinthDealer.grayWeight(data,u,YardCrossings.JOISTS,deep)==YardCrossings.JOIST_WEIGHT,"the joists only in the deep halls");
        h.assertTrue(LabyrinthDealer.grayWeight(data,u,YardCrossings.JOISTS,DarkHalls.ONLY_DEPTH)==0,"and not once the dark halls are all there is");
        data.visit(u,LabyrinthPlace.YARD_TURNAROUND);
        h.assertTrue(LabyrinthDealer.grayWeight(data,u,YardCrossings.LONG_DRIVE,story)==YardCrossings.LONG_WEIGHT&&LabyrinthDealer.grayWeight(data,u,YardCrossings.LONG_DRIVE,story-1)==0,"the long drive after the turn-around, past the domestic opening");
        h.assertTrue(LabyrinthDealer.grayWeight(data,u,YardCrossings.PORCH_TURN,story)==YardCrossings.PORCH_WEIGHT,"the porch turn once the porch bearing is learned");
        for(var place:YardCrossings.PLACES){data.visit(u,place);h.assertTrue(LabyrinthDealer.grayWeight(data,u,place,deep)==0,"once each: "+place);}
        // The reunion's gate is crossed walking north, as the turn-around's is: the arrival keeps the reader's heading.
        var reunion=LabyrinthPlace.YARD_REUNION.doors().stream().filter(d->d.name().equals("exit")).findFirst().orElseThrow();
        var gate=LabyrinthPlace.YARD_TURNAROUND.doors().stream().filter(d->d.name().equals("far")).findFirst().orElseThrow();
        h.assertTrue(reunion.rel().equals(gate.rel())&&reunion.facing()==gate.facing(),"the reunion's gate faces as the turn-around's does");
        h.succeed();
    }
}
