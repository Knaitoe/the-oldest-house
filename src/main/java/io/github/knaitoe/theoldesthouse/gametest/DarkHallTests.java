package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * 0.4.208, the dark halls: every one walkable to every way on and the way back, with no light in it; dealt from sixteen and the
 * only ordinary halls from twenty-one, the staircase from twenty-four (0.4.384); the House taking a light only out of everyone's sight; the
 * edge putting a reader who goes over back on the walkway; the Minotaur heard more often down there.
 */
@GameTestHolder(TheOldestHouse.MOD_ID+"_exploration")
@PrefixGameTestTemplate(false)
public final class DarkHallTests {
    private DarkHallTests(){}

    private static boolean open(ServerLevel level,BlockPos at){var s=level.getBlockState(at);return s.getCollisionShape(level,at).isEmpty()||s.getBlock() instanceof DoorBlock;}
    private static Set<BlockPos> walk(ServerLevel level,BlockPos start){
        Set<BlockPos> seen=new HashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();seen.add(start);queue.add(start);
        while(!queue.isEmpty()){BlockPos at=queue.poll();
            for(Direction side:Direction.Plane.HORIZONTAL)for(int dy=-1;dy<=1;dy++){BlockPos next=at.relative(side).above(dy);
                if(seen.contains(next)||!open(level,next)||!open(level,next.above()))continue;
                if(dy==1&&!open(level,at.above(2)))continue;
                if(level.getBlockState(next.below()).getCollisionShape(level,next.below()).isEmpty())continue;
                seen.add(next);queue.add(next);}}
        return seen;
    }

    @GameTest(template="empty",timeoutTicks=400)
    public static void darkHallsCanBeWalkedToEveryWayOnAndHaveNoLight(GameTestHelper h){
        ServerLevel level=h.getLevel();
        for(int i=0;i<DarkHalls.HALLS.size();i++){
            var hall=DarkHalls.HALLS.get(i);BlockPos base=h.absolutePos(BlockPos.ZERO).offset(900+i*60,30,200);
            DarkHalls.build(level,base,hall);var reached=walk(level,base.offset(0,0,-1));
            for(var door:hall.doors()){BlockPos at=base.offset(door.rel());
                h.assertTrue(level.getBlockState(at).getBlock() instanceof DoorBlock,hall.id()+" has its "+door.name()+" door");
                if(door.name().equals("entry"))continue;
                h.assertTrue(reached.contains(at.relative(door.facing())),hall.id()+": the "+door.name()+" way on can be walked to");}
            int lights=0;var r=hall.room();
            for(BlockPos at:BlockPos.betweenClosed(base.offset(r.minX(),r.minY(),r.minZ()),base.offset(r.maxX(),r.maxY(),r.maxZ())))if(level.getBlockState(at).getLightEmission(level,at)>0)lights++;
            h.assertTrue(lights==0,hall.id()+" has no light of its own ("+lights+")");
            h.assertTrue(LabyrinthDealer.onward(hall)&&LabyrinthPacing.ordinary(hall)&&DarkHalls.isDark(hall),hall.id()+" is an ordinary way on");
        }
        h.assertTrue(walkLength(DarkHalls.HALLS.get(0))>=100,"the long hall is a hundred blocks and more");
        h.succeed();
    }
    private static int walkLength(LabyrinthPlace p){return -p.room().minZ();}

    @GameTest(template="empty")
    public static void theDarkBandBeginsAtSixteenIsTheOnlyWayOnFromTwentyOneAndTheStaircaseWaitsDeeper(GameTestHelper h){
        var data=new LabyrinthData();var player=UUID.randomUUID();
        for(int depth=1;depth<=24;depth++){
            data.pushReturn(player,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,new Vec3(depth,64,0),0F,true));
            boolean dark=depth>=16;
            for(var hall:DarkHalls.HALLS)h.assertTrue(LabyrinthDealer.grayAvailable(data,player).contains(hall)==dark,hall.id()+" is among the halls from sixteen (depth "+depth+")");
            for(var hall:DarkHalls.HALLS)if(hall!=LabyrinthPlace.DARK_EDGE)h.assertTrue((LabyrinthDealer.grayWeight(hall,depth)>0)==dark,hall.id()+" is weighted from sixteen (depth "+depth+")");
        }
        for(var stone:List.of(LabyrinthPlace.STONE_GALLERY,LabyrinthPlace.STONE_CROSSING,LabyrinthPlace.STONE_DESCENT,LabyrinthPlace.STONE_ARCADE,LabyrinthPlace.STONE_BEND,LabyrinthPlace.STONE_LANDING)){
            h.assertTrue(LabyrinthDealer.grayWeight(stone,20)>0,stone.id()+" still comes at twenty");h.assertTrue(LabyrinthDealer.grayWeight(stone,21)==0,stone.id()+" stops at twenty-one");}
        h.assertTrue(LabyrinthDealer.grayWeight(LabyrinthPlace.DARK_EDGE,19)==0&&LabyrinthDealer.grayWeight(LabyrinthPlace.DARK_EDGE,21)>0,"the edge is the last of the series, at the end of the band");
        h.assertTrue(LabyrinthPacing.STAIRCASE_DEPTH==24,"the great staircase is offered from twenty-four (0.4.384)");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=400)
    public static void theHouseTakesALightOnlyWhereNobodyCanSeeIt(GameTestHelper h){
        ServerLevel level=h.getLevel();BlockPos base=h.absolutePos(BlockPos.ZERO).offset(1300,30,200);var hall=LabyrinthPlace.DARK_LONG;DarkHalls.build(level,base,hall);
        var p=NativeTestPlayers.survival(h,"dark_torches");
        try{
            DarkHalls.clearAll();DarkHalls.TEST_TAKE=true;DarkHalls.arrived(p,hall);
            var near=base.offset(1,0,-6);var far=base.offset(-1,0,-30);level.setBlock(near,Blocks.TORCH.defaultBlockState(),3);level.setBlock(far,Blocks.TORCH.defaultBlockState(),3);
            DarkHalls.remember(p,near,hall);DarkHalls.remember(p,far,hall);
            // Facing the far torch: nothing is taken.
            p.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()-60.5,180,0);p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(far));
            h.assertTrue(!DarkHalls.takeTorch(p,level)&&level.getBlockState(far).is(Blocks.TORCH),"a light anyone is looking at is never taken");
            // Turned away, far from both: one is taken, without a drop.
            p.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()-60.5,180,0);p.setYRot(180);p.setYHeadRot(180);p.setXRot(0);
            h.assertTrue(DarkHalls.takeTorch(p,level),"turned away, the House takes one");
            h.assertTrue(!level.getBlockState(near).is(Blocks.TORCH)||!level.getBlockState(far).is(Blocks.TORCH),"one of the reader's two lights is gone");
            h.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(base).inflate(6,4,70)).isEmpty(),"and nothing is dropped");
            h.assertTrue(!DarkHalls.takeTorch(p,level),"once a hall, no more");
        }finally{DarkHalls.clearAll();NativeTestPlayers.remove(p);}
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=200)
    public static void theEdgeHasADropAndTheMinotaurIsHeardMoreOftenInTheDark(GameTestHelper h){
        ServerLevel level=h.getLevel();BlockPos base=h.absolutePos(BlockPos.ZERO).offset(1400,40,200);var hall=LabyrinthPlace.DARK_EDGE;DarkHalls.build(level,base,hall);
        h.assertTrue(level.getBlockState(base.offset(8,DarkHalls.EDGE_DROP-1,-30)).is(DarkHallRegistry.material("floor"))&&level.getBlockState(base.offset(8,0,-30)).isAir()&&level.getBlockState(base.offset(8,-6,-30)).isAir(),"beside the walkway the floor is thirteen blocks down");
        h.assertTrue(!level.getBlockState(base.offset(0,-1,-30)).isAir()&&level.getBlockState(base.offset(2,-1,-30)).isAir(),"the walkway has no rail, and nothing at its edge");
        var rnd=net.minecraft.util.RandomSource.create(7);long dark=0,stone=0;for(int i=0;i<200;i++){dark+=DarkHalls.roarGap(rnd);stone+=Growl.gap(16,rnd);}
        h.assertTrue(dark<stone&&dark*2>stone,"the Minotaur is heard more often in the dark halls, but not twice as often ("+dark/200+" against "+stone/200+")");
        for(int i=0;i<200;i++)h.assertTrue(DarkHalls.roarGap(rnd)>=DarkHalls.ROAR_MIN,"never closer than the minimum");
        h.succeed();
    }
}
