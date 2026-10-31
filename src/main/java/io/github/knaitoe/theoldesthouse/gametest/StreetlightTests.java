package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("the_oldest_house_goatman")
@PrefixGameTestTemplate(false)
public final class StreetlightTests {
    private static NativeTestChunks chunks;private static LabyrinthData old;private static ServerPlayer camera;private static ItemEntity original;
    private static void clean(ServerLevel l){if(chunks!=null){chunks.close();chunks=null;}if(camera!=null){camera.discard();camera=null;}if(original!=null){original.discard();original=null;}if(old!=null){l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",old);old=null;}}
    @AfterBatch(batch="streetlights_fresh0469") public static void cleanFresh(ServerLevel l){clean(l);}
    @GameTest(template="empty",batch="streetlights_fresh0469",timeoutTicks=2400)
    public static void freshTownKeepsEveryFittedStreetlightPartAtItsAuthoredPosition(GameTestHelper h){
        var l=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.OUTSIDE);var b=LabyrinthPlaces.base(new BlockPos(576000,80,576000),LabyrinthPlace.DROWNED_TOWN);
        chunks=new NativeTestChunks();chunks.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.DROWNED_TOWN));
        old=LabyrinthData.get(h.getLevel().getServer());h.getLevel().getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());
        h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"the fresh town's native entity sections are ready")).thenExecute(()->{
            ProofrockTown.build(l,b);
            var failures=new java.util.ArrayList<String>();
            for(var e:ProofrockTown.streetlights(b).entrySet())if(!l.getBlockState(e.getKey()).equals(e.getValue()))failures.add(e.getKey().subtract(b)+" actual="+l.getBlockState(e.getKey())+" expected="+e.getValue());
            h.assertTrue(failures.isEmpty(),"the actual fresh town keeps every authored streetlight part: "+failures.stream().limit(2).toList()+" total="+failures.size());
            // 0.4.77: the cyan car parks along School Street, no lamp stands on the lane's lawn, and a wheel hides nothing under it.
            var along=ProofrockTown.CYAN_ALONG.apply(b);var lamp=ProofrockTown.lawnLamp(b);
            h.assertTrue(along.entrySet().stream().allMatch(e->l.getBlockState(e.getKey()).equals(e.getValue())),"a new town parks the cyan car along its street");
            h.assertTrue(lamp.keySet().stream().noneMatch(at->l.getBlockState(at).is(DrownedTownRegistry.STREETLIGHT.get())),"and puts no lamp on the lane's lawn");
            var wheel=TownFixtureBlock.of(TownFixtureBlock.Kind.CAR_WHEEL,Direction.WEST);h.assertTrue(wheel.getOcclusionShape(l,b).isEmpty()&&!wheel.getCollisionShape(l,b).isEmpty(),"a car wheel is solid but culls nothing beneath it");
            h.succeed();
        });
    }
    private static BlockState oldState(BlockPos b,BlockPos at,BlockState next){
        var kind=next.getValue(TownStreetlightBlock.KIND);
        if(next.getValue(TownStreetlightBlock.WATERLOGGED))return kind==TownStreetlightBlock.Kind.HEAD?Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9).setValue(LightBlock.WATERLOGGED,true):Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WATERLOGGED,true);
        var rel=at.subtract(b);
        if(kind==TownStreetlightBlock.Kind.POLE&&rel.getY()==3&&(rel.getX()==-5&&(rel.getZ()==-9||rel.getZ()==-23)||rel.getX()==5&&(rel.getZ()==-9||rel.getZ()==-23||rel.getZ()==-47||rel.getZ()==-68||rel.getZ()==-79)))return Blocks.DARK_OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,net.minecraft.world.level.block.state.properties.Half.TOP);
        if(kind==TownStreetlightBlock.Kind.BASE&&rel.equals(new BlockPos(-36,0,-24)))return Blocks.SHORT_GRASS.defaultBlockState();
        boolean pier=at.getZ()==b.getZ()-110;if(kind==TownStreetlightBlock.Kind.HEAD)return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,!pier);
        var facing=next.getValue(TownStreetlightBlock.FACING);return ProofrockTown.fence(pier?Blocks.SPRUCE_FENCE:Blocks.DARK_OAK_FENCE,kind==TownStreetlightBlock.Kind.TOP?facing:kind==TownStreetlightBlock.Kind.ARM?facing.getOpposite():null);
    }
}
