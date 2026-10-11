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
    @AfterBatch(batch="streetlights0469") public static void clean(ServerLevel l){if(chunks!=null){chunks.close();chunks=null;}if(camera!=null){camera.discard();camera=null;}if(original!=null){original.discard();original=null;}if(old!=null){l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",old);old=null;}}
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
            var data=LabyrinthData.get(l.getServer());var along=ProofrockTown.CYAN_ALONG.apply(b);var across=ProofrockTown.CYAN_ACROSS.apply(b);var lamp=ProofrockTown.lawnLamp(b);
            h.assertTrue(ProofrockRepairs.done(data,b)&&along.entrySet().stream().allMatch(e->l.getBlockState(e.getKey()).equals(e.getValue())),"a new town parks the cyan car along its street");
            h.assertTrue(lamp.keySet().stream().noneMatch(at->l.getBlockState(at).is(DrownedTownRegistry.STREETLIGHT.get())),"and puts no lamp on the lane's lawn");
            var wheel=TownFixtureBlock.of(TownFixtureBlock.Kind.CAR_WHEEL,Direction.WEST);h.assertTrue(wheel.getOcclusionShape(l,b).isEmpty()&&!wheel.getCollisionShape(l,b).isEmpty(),"a car wheel is solid but culls nothing beneath it");
            // A saved town as it was: the car across the street, the fitted lamp on the lawn with a hole under its base, and a dropped item in the way.
            var marks=data.state(ProofrockRepairs.STATE);marks.remove(Long.toString(b.asLong()));data.setState(ProofrockRepairs.STATE,marks);
            for(var at:along.keySet())l.setBlock(at,Blocks.AIR.defaultBlockState(),2);for(var e:across.entrySet())l.setBlock(e.getKey(),e.getValue(),2);
            for(var e:lamp.entrySet())l.setBlock(e.getKey(),e.getValue(),2);l.setBlock(b.offset(-36,-1,-24),Blocks.AIR.defaultBlockState(),2);
            var head=b.offset(-37,3,-24);original=new ItemEntity(l,head.getX()+.5,head.getY()+.2,head.getZ()+.5,new ItemStack(Items.PAPER));original.setNoGravity(true);l.addFreshEntity(original);
            h.assertTrue(!ProofrockRepairs.apply(l,b)&&l.getBlockState(head).is(DrownedTownRegistry.STREETLIGHT.get()),"nothing changes while an item lies in a cell it would clear");original.discard();original=null;
            h.assertTrue(ProofrockRepairs.apply(l,b)&&ProofrockRepairs.done(LabyrinthData.get(l.getServer()),b),"then the repair runs once");
            h.assertTrue(along.entrySet().stream().allMatch(e->l.getBlockState(e.getKey()).equals(e.getValue()))&&across.keySet().stream().filter(at->!along.containsKey(at)).allMatch(at->l.getBlockState(at).isAir()),"the car now stands along the street and nothing is left across it");
            h.assertTrue(lamp.keySet().stream().allMatch(at->l.getBlockState(at).isAir())&&l.getBlockState(b.offset(-36,-1,-24)).is(Blocks.GRASS_BLOCK),"the lawn lamp is gone and its lawn mended");h.succeed();
        });
    }
    @GameTest(template="empty",batch="streetlights0469",timeoutTicks=2400)
    public static void savedStreetlightsWaitForCamerasAndOriginalsAndPreserveLaterRemovals(GameTestHelper h){
        var s=h.getLevel().getServer();var l=HouseTestLevel.get(s,HouseDimensions.OUTSIDE);var b=LabyrinthPlaces.base(new BlockPos(574000,80,574000),LabyrinthPlace.DROWNED_TOWN);
        old=LabyrinthData.get(s);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());chunks=new NativeTestChunks();chunks.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.DROWNED_TOWN));
        camera=h.makeMockServerPlayerInLevel();camera.teleportTo(l,100,80,100,0,0);camera.hasChangedDimension();
        h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"the saved streetlight blocks and native entity sections are ready")).thenExecute(()->{
            var lights=ProofrockTown.streetlights(b);
            for(var e:lights.entrySet())l.setBlock(e.getKey(),oldState(b,e.getKey(),e.getValue()),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            var omitted=b.offset(-5,0,-9);var edited=b.offset(36,3,-60);l.setBlock(omitted,Blocks.AIR.defaultBlockState(),2);l.setBlock(edited,Blocks.GOLD_BLOCK.defaultBlockState(),2);
            var at=b.offset(-4,3,-9);camera.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.5);
            h.assertTrue(!StreetlightUpgrade.apply(l,b)&&l.getBlockState(at).is(Blocks.LANTERN),"the occupied street remains unchanged under a native camera");camera.moveTo(100,80,100);
            original=new ItemEntity(l,at.getX()+.5,at.getY()+.2,at.getZ()+.5,new ItemStack(Items.PAPER));original.setNoGravity(true);l.addFreshEntity(original);var id=original.getUUID();
            h.assertTrue(!StreetlightUpgrade.apply(l,b),"a finite original overlapping a lamp also postpones its replacement");original.moveTo(100,80,100);
            var saved=new CompoundTag();saved.putString("Exact","Keep the player's hunt and original words");LabyrinthData.get(s).setState("streetlight_test_story",saved);
            h.assertTrue(StreetlightUpgrade.apply(l,b),"the unseen, loaded and vacant authored lamps are replaced once");
            for(var e:lights.entrySet())if(!e.getKey().equals(omitted)&&!e.getKey().equals(edited))h.assertTrue(l.getBlockState(e.getKey()).equals(e.getValue()),"the native lamp part is textured at its original address: "+e.getKey().subtract(b)+" actual="+l.getBlockState(e.getKey())+" expected="+e.getValue());
            h.assertTrue(l.getBlockState(omitted).isAir()&&l.getBlockState(edited).is(Blocks.GOLD_BLOCK)&&original.getUUID().equals(id)&&original.getItem().is(Items.PAPER)&&LabyrinthData.get(s).state("streetlight_test_story").equals(saved),"removed parts, a player's edit, the actual original and story state all survive");
            var data=LabyrinthData.get(s);var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),l.registryAccess()),l.registryAccess());s.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            l.setBlock(at,Blocks.AIR.defaultBlockState(),2);h.assertTrue(StreetlightUpgrade.apply(l,b)&&l.getBlockState(at).isAir(),"reloading the once-only checkpoint never restores a later removal");h.succeed();
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
