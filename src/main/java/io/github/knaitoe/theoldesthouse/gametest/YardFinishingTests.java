package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

/**
 * 0.4.358, the yard's finishing release (docs/YARD_FINISHING_0_4_358.md), each piece by a reader who walks it. The deeper Survey Stakes:
 * from straight on every board of the fence is shut, the gap included; along the stakes' bearing the eye goes through both slots and the
 * gap into the pasture; a body walks through the gap, along behind the near run and out, and the survey settles; the decoy is a pocket.
 * The long drive's figure: under the streetlight as the reader comes along the straight, never nearer than its distance, seen only by
 * that reader, gone round the far bend, and once. The clothesline's quiet return: the first walk is followed, the second only has the
 * wet linen and the line. And the hedges' loose edges lean as the wind runs down the lane while the lane stays as wide as it was.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration") @PrefixGameTestTemplate(false)
public final class YardFinishingTests {
    private YardFinishingTests(){}
    private static LiteraryTests.Fixture active;
    @AfterBatch(batch="yard_survey_deep") public static void surveyDone(ServerLevel l){close();}
    @AfterBatch(batch="yard_figure") public static void figureDone(ServerLevel l){close();YardFigure.clearAll();}
    @AfterBatch(batch="yard_quiet_line") public static void lineDone(ServerLevel l){close();YardConnectors.clearAll();}
    @AfterBatch(batch="yard_hedge_wind") public static void windDone(ServerLevel l){close();YardConnectors.clearAll();}
    private static void close(){if(active!=null){active.close();active=null;}}
    private static LiteraryTests.Fixture fixture(GameTestHelper h,LabyrinthPlace place){active=new LiteraryTests.Fixture(h);var a=LabyrinthPlaces.slotBounds(active.origin,place);active.chunks.hold(active.out,new AABB(a.minX(),a.minY(),a.minZ(),a.maxX()+1,a.maxY()+1,a.maxZ()+1));return active;}
    private static ServerPlayer player(GameTestHelper h,LiteraryTests.Fixture f,String name){var p=NativeTestPlayers.survival(h,name);f.players.add(p);return p;}
    private static void at(ServerPlayer p,ServerLevel l,BlockPos b,double x,double y,double z){p.teleportTo(l,b.getX()+x,b.getY()+y,b.getZ()+z,180,0);p.setDeltaMovement(Vec3.ZERO);p.hasChangedDimension();}
    private static boolean blocked(ServerLevel l,ServerPlayer p,Vec3 from,Vec3 to){return l.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS;}
    /** A reader walking a route of points on their own feet, with gravity and collision, failing if they stall. */
    private static final class Walk {
        final List<Vec3> route=new ArrayList<>();int at=-1,stuck=0;double best=Double.MAX_VALUE,speed=.2;
        void start(BlockPos b,double[]... points){route.clear();for(var q:points)route.add(Vec3.atLowerCornerOf(b).add(q[0],0,q[1]));at=0;stuck=0;best=Double.MAX_VALUE;}
        boolean done(){return at>=route.size();}
        void tick(GameTestHelper h,ServerPlayer p,BlockPos b,String where){
            if(at<0||done())return;var d=route.get(at).subtract(p.position()).multiply(1,0,1);
            if(d.length()<.25){at++;best=Double.MAX_VALUE;stuck=0;return;}
            if(d.length()<best-.015){best=d.length();stuck=0;}else stuck++;
            h.assertTrue(stuck<90,where+" supports a walking body at "+p.position().subtract(Vec3.atLowerCornerOf(b))+" heading for "+route.get(at).subtract(Vec3.atLowerCornerOf(b)));
            var move=d.normalize().scale(Math.min(speed,d.length()));double vy=p.onGround()?0:(p.getDeltaMovement().y-.08)*.98;
            p.setDeltaMovement(move.x,vy,move.z);p.move(MoverType.SELF,p.getDeltaMovement());if(p.verticalCollision)p.setDeltaMovement(move.x,0,move.z);
        }
    }
    private static double[] pt(double x,double z){return new double[]{x,z};}

    @GameTest(template="empty",batch="yard_survey_deep",timeoutTicks=2400)
    public static void theDeeperStakesLineUpWithAFenceThatOpensOnlyOnTheirBearing(GameTestHelper h){
        var place=LabyrinthPlace.YARD_SURVEY_DEEP;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"bearing_reader");var walk=new Walk();
        h.onEachTick(()->walk.tick(h,p,b,"the deep survey field"));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the field is resident"))
            .thenExecute(()->{
                h.assertTrue(place.isGray()&&NovelRooms.dimension(place).equals(HouseDimensions.OUTSIDE)&&PuzzleRooms.Puzzle.YARD_SURVEY_DEEP.family==PuzzleRooms.Family.SURVEY&&PuzzleRooms.Puzzle.YARD_SURVEY_DEEP.deep,"an outdoor survey room, deep");
                YardFields.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
                h.assertTrue(f.data().door(place.id()+"/far").dimension.equals(HouseDimensions.OUTSIDE),"its doors open outside");
                at(p,l,b,.5,0,-6.5);p.setOnGround(true);
                // From straight on, every board of the fence is shut: the gap and the decoy too, their boarding behind them.
                for(int x=-YardFields.DEEP_HALF;x<=YardFields.DEEP_HALF;x++){var from=Vec3.atLowerCornerOf(b).add(x+.5,1.62,-20);
                    h.assertTrue(blocked(l,p,from,from.add(0,0,-15)),"seen straight on the fence is shut at x "+x);}
                // From the entrance, looking at the gap, the boarding behind it fills it.
                var door=Vec3.atLowerCornerOf(b).add(.5,1.62,-6.5);var gap=Vec3.atLowerCornerOf(b).add(YardFields.DEEP_GAP.getX()+1,1.62,YardFields.FENCE_Z+.5);
                h.assertTrue(blocked(l,p,door,door.add(gap.subtract(door).scale(1.4))),"from the gate in, the gap looks like fence");
                // Along the stakes' bearing the eye goes through both slots and the gap, into the pasture.
                double z0=-16.5,z1=-36.5;var eye=Vec3.atLowerCornerOf(b).add(YardFields.BEARING+z0,1.62,z0);var far=Vec3.atLowerCornerOf(b).add(YardFields.BEARING+z1,1.62,z1);
                h.assertTrue(l.getBlockState(b.offset(YardFields.DEEP_PAD)).is(Blocks.SMOOTH_STONE)&&eye.subtract(Vec3.atCenterOf(b.offset(YardFields.DEEP_PAD))).horizontalDistance()<.8,"the bearing starts from the old pad");
                for(var stake:List.of(YardFields.DEEP_NEAR,YardFields.DEEP_FAR))h.assertTrue(new AABB(b.offset(stake)).clip(eye,far).isPresent(),"the bearing passes through the stake at "+stake);
                h.assertTrue(!blocked(l,p,eye,far),"along the stakes' bearing nothing stands between the pad and the pasture");
                // And a second gap, its boarding behind it, closed at both ends.
                for(var shut:List.of(new BlockPos(YardFields.DECOY_X0-1,0,YardFields.FENCE_Z-1),new BlockPos(YardFields.DECOY_X1+1,0,YardFields.FENCE_Z-1),new BlockPos(YardFields.DECOY_GAP.getX(),0,YardFields.BACK_Z),new BlockPos(YardFields.DECOY_GAP.getX()+1,0,YardFields.BACK_Z)))
                    h.assertTrue(!l.getBlockState(b.offset(shut)).isAir(),"the decoy is a pocket, shut at "+shut);
                h.assertTrue(!PuzzleRooms.solved(f.data(),place),"not settled before anyone is through");
                // Through the gap, west along behind the near run, out past the end of the boarding behind it, and on.
                int gx=YardFields.DEEP_GAP.getX();
                walk.start(b,pt(gx+.5,-14.5),pt(gx+.5,-25.5),pt(gx+.5,YardFields.FENCE_Z-.5),pt(YardFields.BACK_X0-1.5,YardFields.FENCE_Z-.5),pt(YardFields.BACK_X0-1.5,YardFields.BACK_Z-1.5),pt(.5,-44.5));
            })
            .thenWaitUntil(()->h.assertTrue(walk.done(),"a walking body goes through where the fence doubles back: "+walk.at+" of "+walk.route.size()))
            .thenWaitUntil(()->h.assertTrue(PuzzleRooms.solved(f.data(),place),"through the fence on the stakes' line, the survey settles"))
            .thenExecute(()->h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&Math.abs(p.getY()-b.getY())<.1,"no story credit, on the field's own ground"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="yard_figure",timeoutTicks=4000)
    public static void theFigureUnderTheStreetlightKeepsItsDistanceForOneReader(GameTestHelper h){
        var place=YardCrossings.LONG_DRIVE;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"straight_reader");var q=player(h,f,"apron_reader");var walk=new Walk();
        double[] nearest={Double.MAX_VALUE};boolean[] seen={false};
        h.onEachTick(()->{
            walk.tick(h,p,b,"the long straight");
            var figure=YardFigure.figure(l,p);
            if(figure!=null){seen[0]=true;nearest[0]=Math.min(nearest[0],YardFigure.distance(p));}
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the property is resident"))
            .thenExecute(()->{
                YardCrossings.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
                at(q,l,b,.5,0,-13.5);q.setOnGround(true);
                var start=YardFigure.at(YardFigure.straight()+1);at(p,l,b,start.x,0,start.z);p.setOnGround(true);walk.speed=.18;
                var route=new ArrayList<double[]>();for(int i=3;i<=5;i++)route.add(pt(YardCrossings.LOOP[i][0]+.5,YardCrossings.LOOP[i][1]+.5));
                walk.start(b,route.toArray(double[][]::new));
            })
            .thenWaitUntil(()->h.assertTrue(YardFigure.figure(l,p)!=null,"someone is standing under the streetlight at the end of the straight"))
            .thenExecute(()->{
                var figure=YardFigure.figure(l,p);var light=YardFigure.at(YardFigure.light());
                h.assertTrue(figure.role()==LiteraryActor.SILHOUETTE&&figure.position().distanceTo(Vec3.atLowerCornerOf(b).add(light.x,0,light.z))<4,"a silhouette under the lamp: "+figure.position().subtract(Vec3.atLowerCornerOf(b)));
                h.assertTrue(figure.broadcastToPlayer(p)&&!figure.broadcastToPlayer(q),"seen by the reader on the straight, not by the one on the apron");
                h.assertTrue(YardFigure.figure(l,q)==null,"the reader on the apron has no figure of their own");
            })
            .thenWaitUntil(()->h.assertTrue(walk.done(),"the reader walks the straight and past the light: "+walk.at+" of "+walk.route.size()))
            .thenWaitUntil(()->h.assertTrue(YardFigure.figure(l,p)==null,"round the far bend, it is gone"))
            .thenExecute(()->{
                h.assertTrue(seen[0]&&nearest[0]>=YardFigure.GAP-1,"it never came nearer than its distance along the road: "+nearest[0]);
                var own=YardCrossings.reader(l,p,place);h.assertTrue(own.getBoolean("Figure")&&own.getBoolean("FigureGone"),"the reader's record keeps it: "+own);
                var start=YardFigure.at(YardFigure.straight()+1);at(p,l,b,start.x,0,start.z);
                walk.start(b,pt(YardCrossings.LOOP[3][0]+.5,YardCrossings.LOOP[3][1]+.5));
            })
            .thenWaitUntil(()->h.assertTrue(walk.done(),"along the straight again"))
            .thenExecute(()->h.assertTrue(YardFigure.figure(l,p)==null,"once to a reader: nobody under the light the second time"))
            .thenSucceed();
    }

    @GameTest(template="empty",batch="yard_quiet_line",timeoutTicks=2400)
    public static void aLaterWalkDownTheWashingLineHasOnlyTheWetLinenAndTheLine(GameTestHelper h){
        var place=LabyrinthPlace.YARD_CLOTHESLINE;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"line_walker");p.setNoGravity(true);
        boolean[] walking={false};int[] steps={0},wet={0},snap={0},dry={0};
        h.onEachTick(()->{
            if(walking[0])p.move(MoverType.SELF,new Vec3(0,0,-.10));
            for(var s:NativeTestPlayers.sounds(p)){var e=s.getSound().value();
                if(e==PuzzleRegistry.YARD_FOLLOW.get())steps[0]++;else if(e==PuzzleRegistry.YARD_WET_SHEET.get())wet[0]++;else if(e==PuzzleRegistry.YARD_LINE_SNAP.get())snap[0]++;else if(e==net.minecraft.sounds.SoundEvents.WOOL_STEP)dry[0]++;}
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the washing line is resident"))
            .thenExecute(()->{YardConnectors.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);at(p,l,b,.5,0,-6.5);walking[0]=true;})
            .thenWaitUntil(()->h.assertTrue(p.getZ()<b.getZ()-30,"the first walk to the end of the line"))
            .thenExecute(()->{
                walking[0]=false;
                h.assertTrue(steps[0]>0&&dry[0]>0&&wet[0]==0,"the first walk: steps behind, and dry linen ("+steps[0]+" steps, "+dry[0]+" dry, "+wet[0]+" wet)");
                h.assertTrue(YardConnectors.walked(p)==1,"one walk to the end, kept");
                at(p,l,b,500,0,0);
            })
            .thenIdle(3)
            .thenWaitUntil(()->h.assertTrue(!YardConnectors.folded(l,b,-9)&&!YardConnectors.folded(l,b,-16)&&!YardConnectors.folded(l,b,-23),"the linen has fallen back"))
            .thenExecute(()->{steps[0]=0;wet[0]=0;snap[0]=0;dry[0]=0;at(p,l,b,.5,0,-6.5);walking[0]=true;})
            .thenWaitUntil(()->h.assertTrue(p.getZ()<b.getZ()-30,"the second walk to the end of the line"))
            .thenExecute(()->walking[0]=false)
            .thenWaitUntil(()->h.assertTrue(snap[0]>0,"the line snaps taut as the wet linen falls back"))
            .thenExecute(()->{
                h.assertTrue(steps[0]==0,"nothing follows the second walk");
                h.assertTrue(wet[0]>0&&dry[0]==0,"the linen is heavy with water ("+wet[0]+" wet, "+dry[0]+" dry)");
                h.assertTrue(YardConnectors.walked(p)==2&&YardCrossings.reader(l,p,place).getBoolean("QuietWalk"),"two walks kept, the second a quiet one");
                h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"no story credit");
            }).thenSucceed();
    }

    @GameTest(template="empty",batch="yard_hedge_wind",timeoutTicks=1600)
    public static void theHedgesLooseEdgesLeanInTheWindAndTheLaneStaysOpen(GameTestHelper h){
        var place=LabyrinthPlace.YARD_DITCH;var f=fixture(h,place);var l=f.out;var b=f.base(place);var p=player(h,f,"wind_reader");p.setNoGravity(true);
        Map<BlockPos,Set<Integer>> stages=new HashMap<>();Map<BlockPos,Integer> left=new HashMap<>();
        h.onEachTick(()->{for(var fr:YardConnectors.fringes(place)){var s=l.getBlockState(b.offset(fr.at()));if(YardBlock.is(s,YardBlock.Kind.FRINGE))stages.computeIfAbsent(fr.at(),k->new HashSet<>()).add(s.getValue(YardBlock.STAGE));}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the ditch is resident"))
            .thenExecute(()->{
                YardConnectors.build(l,b,place);LabyrinthBuilder.registerDoors(f.data(),place,b);
                int placed=0;
                for(var fr:YardConnectors.fringes(place)){var at=b.offset(fr.at());var s=l.getBlockState(at);
                    if(!YardBlock.is(s,YardBlock.Kind.FRINGE))continue;placed++;
                    h.assertTrue(s.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)==fr.facing()&&l.getBlockState(at.relative(fr.facing().getOpposite())).is(Blocks.SPRUCE_LEAVES),"each loose edge stands off real hedge, pointing into the lane at "+fr.at());
                    h.assertTrue(s.getCollisionShape(l,at).isEmpty(),"a loose edge never narrows the lane at "+fr.at());
                }
                h.assertTrue(placed>=8,"the hedges have loose edges down the lane: "+placed);
                // The lane is as wide as it was: a body fits against either hedge all the way down the open stretch.
                for(int z=-4;z>=-11;z--)for(int x:new int[]{-1,1})h.assertTrue(l.noCollision(null,new AABB(b.getX()+x+.2,b.getY()+.01,b.getZ()+z+.2,b.getX()+x+.8,b.getY()+1.8,b.getZ()+z+.8)),"the lane is open at "+x+","+z);
                at(p,l,b,.5,0,-6.5);
            })
            .thenWaitUntil(()->{int moved=0;for(var set:stages.values())if(set.size()>1)moved++;h.assertTrue(moved>=stages.size()/2&&moved>0,"the wind runs down the lane and the loose edges lean: "+moved+" of "+stages.size());})
            // Nobody in the lane, and the edges stay where the wind left them.
            .thenExecute(()->at(p,l,b,500,0,0))
            .thenIdle(4)
            .thenExecute(()->{for(var fr:YardConnectors.fringes(place)){var s=l.getBlockState(b.offset(fr.at()));if(YardBlock.is(s,YardBlock.Kind.FRINGE))left.put(fr.at(),s.getValue(YardBlock.STAGE));}})
            .thenIdle(40)
            .thenExecute(()->{for(var e:left.entrySet())h.assertTrue(l.getBlockState(b.offset(e.getKey())).getValue(YardBlock.STAGE)==e.getValue(),"with nobody in the lane the wind is not worked at "+e.getKey());})
            .thenSucceed();
    }
}
