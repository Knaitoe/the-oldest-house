package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID + "_seam")
@PrefixGameTestTemplate(false)
public final class SeamDoorTests {
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static Fixture fixture;

    private static final class Fixture implements AutoCloseable {
        final ServerLevel outside,inside;
        final HouseSavedData old;
        final BlockPos origin,lower;
        final NativeTestChunks chunks=new NativeTestChunks();
        ServerPlayer player;
        Fixture(GameTestHelper h,int coordinate){
            var server=h.getLevel().getServer();outside=server.overworld();inside=HouseTestLevel.get(server,HouseDimensions.INTERIOR);
            old=HouseSavedData.get(server);origin=new BlockPos(coordinate,80,coordinate);
            lower=origin.offset(HouseLayout.FRONT_DOOR.x(),HouseLayout.FRONT_DOOR.y(),HouseLayout.FRONT_DOOR.z());
            chunks.hold(outside,new AABB(lower).inflate(8));chunks.hold(inside,new AABB(lower).inflate(8));
            var data=new HouseSavedData();data.markSpawned(origin);data.markInteriorInitialized();
            server.overworld().getDataStorage().set("the_oldest_house",data);
            for(var level:new ServerLevel[]{outside,inside}){
                for(int x=-2;x<=2;x++)for(int z=-5;z<=5;z++)level.setBlock(lower.offset(x,-1,z),Blocks.STONE.defaultBlockState(),FLAGS);
                door(level,false);
            }
        }
        void door(ServerLevel level,boolean open){
            var state=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.OPEN,open);
            level.setBlock(lower,state.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),FLAGS);
            level.setBlock(lower.above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),FLAGS);
        }
        void assertDoor(GameTestHelper h,boolean open){
            for(var level:new ServerLevel[]{outside,inside})for(var half:new BlockPos[]{lower,lower.above()}){
                var state=level.getBlockState(half);
                h.assertTrue(state.is(Blocks.SPRUCE_DOOR)&&state.getValue(DoorBlock.OPEN)==open,
                        "both native door halves agree on "+level.dimension().location()+" at "+half+"; expected open="+open+", state="+state);
            }
        }
        @Override public void close(){
            if(player!=null)NativeTestPlayers.remove(player);
            HouseTransitionEvents.clearAll();HouseMirrorSyncEvents.clearPending();HouseChunkKeeper.release(outside.getServer());
            chunks.close();outside.getServer().overworld().getDataStorage().set("the_oldest_house",old);
        }
    }

    @AfterBatch(batch="seam_exit") public static void exitCleanup(ServerLevel level){cleanup();}
    @AfterBatch(batch="seam_copy") public static void copyCleanup(ServerLevel level){cleanup();}
    private static void cleanup(){if(fixture!=null){fixture.close();fixture=null;}}

    @GameTest(template="empty",batch="seam_exit",timeoutTicks=110)
    public static void realExitOpensBothDoorHalvesUntilTheWaitingPlayerPasses(GameTestHelper h){
        fixture=new Fixture(h,281000);var f=fixture;
        var barrel=f.lower.offset(2,0,4);
        for(var level:new ServerLevel[]{f.inside,f.outside})level.setBlock(barrel,Blocks.BARREL.defaultBlockState(),FLAGS);
        ((BarrelBlockEntity)f.inside.getBlockEntity(barrel)).setItem(0,new ItemStack(Items.DIAMOND,5));
        f.player=NativeTestPlayers.survival(h,"seam_exit_waiting");var p=f.player;p.setNoGravity(true);
        p.teleportTo(f.inside,f.lower.getX()+.5,f.lower.getY(),f.lower.getZ()+3.35,180,0);p.setDeltaMovement(Vec3.ZERO);
        var click=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,f.lower,
                new BlockHitResult(f.lower.getCenter(),Direction.SOUTH,f.lower,false));
        NeoForge.EVENT_BUS.post(click);h.assertTrue(click.isCanceled()&&HouseTransitionEvents.isPending(p),"the real closed-door handle queues an exit");
        h.runAfterDelay(3,()->{
            h.assertTrue(p.serverLevel()==f.outside&&!HouseTransitionEvents.isPending(p),"the actual native player crosses to the Overworld");
            f.assertDoor(h,true);
        });
        h.runAfterDelay(65,()->{
            f.assertDoor(h,true);
            h.assertTrue(((BarrelBlockEntity)f.inside.getBlockEntity(barrel)).getItem(0).getCount()==5
                    &&((BarrelBlockEntity)f.outside.getBlockEntity(barrel)).isEmpty(),"exit reconciliation keeps original items solely in the authoritative container");
            p.moveTo(f.lower.getX()+.5,f.lower.getY(),f.lower.getZ()-.75);p.setDeltaMovement(Vec3.ZERO);
            HouseTransitionEvents.onPlayerTick(new PlayerTickEvent.Post(p));f.assertDoor(h,false);h.succeed();
        });
    }

    @GameTest(template="empty",batch="seam_copy",timeoutTicks=40)
    public static void nativeMirroredDoorHalvesSurviveAnIncompleteCopyWithoutDrops(GameTestHelper h){
        fixture=new Fixture(h,281400);var f=fixture;f.door(f.inside,true);
        f.outside.setBlock(f.lower,Blocks.AIR.defaultBlockState(),FLAGS);
        f.outside.setBlock(f.lower.above(),Blocks.AIR.defaultBlockState(),FLAGS);
        f.outside.setBlock(f.lower.below(),Blocks.AIR.defaultBlockState(),FLAGS);
        HouseDimensionMirror.copyStateAndBlockEntity(f.inside,f.outside,f.lower.above());
        HouseDimensionMirror.copyStateAndBlockEntity(f.inside,f.outside,f.lower);
        HouseDimensionMirror.copyStateAndBlockEntity(f.inside,f.outside,f.lower.below());
        h.runAfterDelay(3,()->{
            f.assertDoor(h,true);
            h.assertTrue(f.outside.getBlockState(f.lower).getValue(DoorBlock.HALF)==DoubleBlockHalf.LOWER
                    &&f.outside.getBlockState(f.lower.above()).getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER,"the native mirrored lower and upper halves keep their identities");
            h.assertTrue(f.outside.getEntitiesOfClass(ItemEntity.class,new AABB(f.lower).inflate(3)).isEmpty(),"copying the two halves and their support creates no broken-door item debris");h.succeed();
        });
    }
}
