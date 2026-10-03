package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ExplorationTests {
    private ExplorationTests() {}
    @GameTest(template="empty")
    public static void conventionalHallsHaveSparseDoorsAndWalkableCorners(GameTestHelper helper) {
        int index=0;
        for(var place:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.QUIET_ROOM)) {
            BlockPos base=helper.absolutePos(BlockPos.ZERO).offset(1250+(index++)*45,6,-80);
            LabyrinthHalls.build(helper.getLevel(),base,place);
            Set<BlockPos> seen=new HashSet<>();
            ArrayDeque<BlockPos> queue=new ArrayDeque<>();
            BlockPos start=base.offset(0,0,-1);seen.add(start);queue.add(start);
            while(!queue.isEmpty()) {
                BlockPos at=queue.removeFirst();
                for(Direction direction:Direction.Plane.HORIZONTAL) {
                    BlockPos next=at.relative(direction);
                    if(seen.contains(next)||!helper.getLevel().getBlockState(next).getCollisionShape(helper.getLevel(),next).isEmpty()
                            ||!helper.getLevel().getBlockState(next.above()).getCollisionShape(helper.getLevel(),next.above()).isEmpty()
                            ||!helper.getLevel().getBlockState(next.below()).isCollisionShapeFullBlock(helper.getLevel(),next.below())) continue;
                    seen.add(next);queue.add(next);
                }
            }
            for(var door:place.doors()) {
                if(door.name().equals("entry"))continue;
                helper.assertTrue(seen.contains(base.offset(door.rel().relative(door.facing()))),
                        "a real path reaches every side door: "+place+"/"+door.name());
            }
            if(place!=LabyrinthPlace.QUIET_ROOM)
                helper.assertTrue(place.doors().size()==(place==LabyrinthPlace.CROSS_HALL?4:2),"ordinary corridors keep two end doors; only a real junction branches");
            helper.assertTrue(!LabyrinthPacing.anomaly(place),"ordinary corners do not fold");
        }
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void impossibleDealsAreRareAndNeverFillSeveralDoors(GameTestHelper helper) {
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();
        LabyrinthBuilder.registerDoors(data,LabyrinthPlace.STRAIGHT_HALL,new BlockPos(0,64,0));
        var random=RandomSource.create(481);
        int shallowOdd=0;
        for(int i=0;i<300;i++) {
            LabyrinthDealer.dealPlace(data,player,LabyrinthPlace.STRAIGHT_HALL,random);
            for(var spec:LabyrinthPlace.STRAIGHT_HALL.doors()) {
                var door=data.door(LabyrinthPlace.STRAIGHT_HALL.doorId(spec));
                if(!LabyrinthData.DEALT.equals(door.destination))continue;
                var destination=LabyrinthPlace.byId(data.deal(player,door).place());
                if(LabyrinthPacing.anomaly(destination))shallowOdd++;
                helper.assertTrue(destination!=LabyrinthPlace.GRAY_CORRIDOR,"the opening does not begin as a physical maze");
            }
        }
        helper.assertTrue(shallowOdd==0,"early corridors stay spatially ordinary");
        for(int i=0;i<12;i++)data.pushReturn(player,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));
        int oddArrivals=0;
        for(int i=0;i<1000;i++) {
            LabyrinthDealer.dealPlace(data,player,LabyrinthPlace.STRAIGHT_HALL,random);
            int oddDoors=0;
            for(var spec:LabyrinthPlace.STRAIGHT_HALL.doors()) {
                var door=data.door(LabyrinthPlace.STRAIGHT_HALL.doorId(spec));
                if(LabyrinthData.DEALT.equals(door.destination)&&LabyrinthPacing.anomaly(LabyrinthPlace.byId(data.deal(player,door).place())))oddDoors++;
            }
            helper.assertTrue(oddDoors<=1,"one discovery budget is shared by all the doors");
            if(oddDoors>0)oddArrivals++;
        }
        helper.assertTrue(oddArrivals>20&&oddArrivals<180,"deep anomalies remain occasional: "+oddArrivals+"/1000");
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void anomaliesAllowBreathingRoomAndRememberItAcrossRestart(GameTestHelper helper) {
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID(),other=UUID.randomUUID();
        for(int i=0;i<9;i++)data.pushReturn(player,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));
        data.visit(player,LabyrinthPlace.DEEP_MAZE);
        helper.assertTrue(LabyrinthPacing.anomalyChance(data,player)==0&&LabyrinthPacing.restDue(data,player),
                "an impossible stretch is followed by ordinary choices and a chance to rest");
        var saved=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),helper.getLevel().registryAccess()),helper.getLevel().registryAccess());
        helper.assertTrue(LabyrinthPacing.anomalyChance(saved,player)==0,"a restart cannot skip the breathing room");
        for(var room:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.BENT_HALL,LabyrinthPlace.CROSS_HALL))saved.visit(player,room);
        helper.assertTrue(LabyrinthPacing.anomalyChance(saved,player)>0,"three later visits make discovery possible again");
        saved.visit(player,LabyrinthPlace.QUIET_ROOM);
        helper.assertTrue(!LabyrinthPacing.restDue(saved,player),"quiet rooms are not immediately forced again");
        helper.assertTrue(LabyrinthPacing.anomalyChance(saved,other)==0,"the new explorer's depth is independent");
        helper.succeed();
    }
    @GameTest(template="empty")
    public static void existingCacheUpgradePreservesContentsAndCannotRefill(GameTestHelper helper) {
        var level=helper.getLevel();var data=LabyrinthData.get(level.getServer());
        var previous=data.state("navigation_cache_048");data.setState("navigation_cache_048",new CompoundTag());
        BlockPos origin=helper.absolutePos(new BlockPos(2000,4,0));
        BlockPos base=LabyrinthPlaces.base(origin,LabyrinthPlace.JUNCTION);
        BlockPos pos=base.offset(LabyrinthLighting.TOM_CACHE);
        level.setBlock(pos,net.minecraft.world.level.block.Blocks.BARREL.defaultBlockState(),3);
        var cache=(net.minecraft.world.Container)level.getBlockEntity(pos);
        for(int i=1;i<cache.getContainerSize();i++)cache.setItem(i,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE,37));
        try {
            LabyrinthLighting.upgradeNavigationCache(level,origin,LabyrinthPlace.JUNCTION);
            helper.assertTrue(cache.getItem(0).is(LabyrinthRegistry.CHALK.get()),"an empty slot receives chalk");
            cache.setItem(0,net.minecraft.world.item.ItemStack.EMPTY);
            LabyrinthLighting.upgradeNavigationCache(level,origin,LabyrinthPlace.JUNCTION);
            helper.assertTrue(cache.getItem(0).is(LabyrinthRegistry.TRAIL_SPOOL.get()),"a partial upgrade resumes with the missing spool, not more chalk");
            cache.setItem(0,net.minecraft.world.item.ItemStack.EMPTY);
            LabyrinthLighting.upgradeNavigationCache(level,origin,LabyrinthPlace.JUNCTION);
            helper.assertTrue(cache.getItem(0).isEmpty()&&cache.getItem(1).getCount()==37,
                    "revisits do not refill supplies or replace stored items");
        } finally { data.setState("navigation_cache_048",previous); }
        helper.succeed();
    }

}
