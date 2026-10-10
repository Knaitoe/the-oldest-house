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

@GameTestHolder("the_oldest_house")
@PrefixGameTestTemplate(false)
public final class StreetlightTests {
    private static NativeTestChunks chunks;private static LabyrinthData old;private static ServerPlayer camera;private static ItemEntity original;
    @AfterBatch(batch="streetlights0469") public static void clean(ServerLevel l){if(chunks!=null){chunks.close();chunks=null;}if(camera!=null){camera.discard();camera=null;}if(original!=null){original.discard();original=null;}if(old!=null){l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",old);old=null;}}
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
            for(var e:lights.entrySet())if(!e.getKey().equals(omitted)&&!e.getKey().equals(edited))h.assertTrue(l.getBlockState(e.getKey()).equals(e.getValue()),"the native lamp part is textured at its original address: "+e.getKey());
            h.assertTrue(l.getBlockState(omitted).isAir()&&l.getBlockState(edited).is(Blocks.GOLD_BLOCK)&&original.getUUID().equals(id)&&original.getItem().is(Items.PAPER)&&LabyrinthData.get(s).state("streetlight_test_story").equals(saved),"removed parts, a player's edit, the actual original and story state all survive");
            var data=LabyrinthData.get(s);var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),l.registryAccess()),l.registryAccess());s.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            l.setBlock(at,Blocks.AIR.defaultBlockState(),2);h.assertTrue(StreetlightUpgrade.apply(l,b)&&l.getBlockState(at).isAir(),"reloading the once-only checkpoint never restores a later removal");h.succeed();
        });
    }
    private static BlockState oldState(BlockPos b,BlockPos at,BlockState next){
        var kind=next.getValue(TownStreetlightBlock.KIND);
        if(next.getValue(TownStreetlightBlock.WATERLOGGED))return kind==TownStreetlightBlock.Kind.HEAD?Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9).setValue(LightBlock.WATERLOGGED,true):Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WATERLOGGED,true);
        boolean pier=at.getZ()==b.getZ()-110;if(kind==TownStreetlightBlock.Kind.HEAD)return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,!pier);
        var facing=next.getValue(TownStreetlightBlock.FACING);return ProofrockTown.fence(pier?Blocks.SPRUCE_FENCE:Blocks.DARK_OAK_FENCE,kind==TownStreetlightBlock.Kind.TOP?facing:kind==TownStreetlightBlock.Kind.ARM?facing.getOpposite():null);
    }
}
