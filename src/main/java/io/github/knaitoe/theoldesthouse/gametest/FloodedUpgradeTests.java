package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FloodedUpgradeTests {
    private static Fixture fixture;
    private static final class Fixture implements AutoCloseable {
        final net.minecraft.server.MinecraftServer server;
        final ServerLevel level;
        final HouseSavedData oldHouse;
        final LabyrinthData oldData;
        final BlockPos origin=new BlockPos(109700,80,109700),base;
        final AABB area;
        final NativeTestChunks chunks=new NativeTestChunks();
        final List<Entity> bodies=new ArrayList<>();
        final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h){
            server=h.getLevel().getServer();level=HouseTestLevel.get(server);oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.FLOODED_PASSAGE);var box=LabyrinthPlaces.placeBounds(origin,LabyrinthPlace.FLOODED_PASSAGE);
            area=new AABB(Vec3.atLowerCornerOf(new BlockPos(box.minX()-2,box.minY()-2,box.minZ()-2)),Vec3.atLowerCornerOf(new BlockPos(box.maxX()+3,box.maxY()+3,box.maxZ()+3)));
        }
        boolean repaired(){return LabyrinthData.get(server).state("water_trial_0427").getBoolean(Long.toString(base.asLong()));}
        @Override public void close(){
            bodies.forEach(Entity::discard);players.forEach(p->server.getPlayerList().remove(p));chunks.close();
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();
        }
    }
    @AfterBatch(batch="cold_flood0472") public static void clean(ServerLevel level){if(fixture!=null){fixture.close();fixture=null;}}
    @GameTest(template="empty",batch="cold_flood0472",timeoutTicks=2000)
    public static void coldRepairWaitsForSectionsCamerasAndOriginalsThenNeverRebuildsAfterReload(GameTestHelper h){
        fixture=new Fixture(h);var f=fixture;
        int x=f.base.getX()>>4,z=f.base.getZ()>>4;
        h.assertTrue(!f.level.hasChunk(x,z),"the native repair starts with a genuinely cold scene");
        LabyrinthHazards.upgradeFlooded(f.level,f.origin);
        h.assertTrue(!f.level.hasChunk(x,z)&&!f.repaired(),"a routine upgrade must not synchronously load cold chunks or mark unfinished work complete");
        f.chunks.hold(f.level,f.area);
        var camera=NativeTestPlayers.survival(h,"flood_vestibule_camera");f.players.add(camera);camera.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        camera.teleportTo(f.level,f.base.getX()+.5,f.base.getY(),f.base.getZ()+15.5,180,0);camera.hasChangedDimension();
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"native block chunks and original entity sections are loaded")).thenExecute(()->{
            h.assertTrue(f.level.players().contains(camera),"a real spectator camera occupies the copied vestibule");
            LabyrinthHazards.upgradeFlooded(f.level,f.origin);h.assertTrue(!f.repaired(),"the full sixteen-block vestibule remains protected while its native camera watches");
            var back=h.absolutePos(BlockPos.ZERO);camera.teleportTo(h.getLevel(),back.getX(),back.getY(),back.getZ(),0,0);camera.hasChangedDimension();
            var at=f.base.offset(-3,0,-8);f.level.setBlock(at,Blocks.BARREL.defaultBlockState(),2);var barrel=(BarrelBlockEntity)f.level.getBlockEntity(at);
            var original=new ItemStack(Items.IRON_AXE);original.setDamageValue(37);original.set(DataComponents.CUSTOM_NAME,Component.literal("kept original"));barrel.setItem(5,original.copy());
            LabyrinthHazards.upgradeFlooded(f.level,f.origin);
            h.assertTrue(!f.repaired()&&f.level.getBlockEntity(at)==barrel&&ItemStack.matches(original,barrel.getItem(5)),"a native finite container and its exact components are never replaced");
            camera.getInventory().setItem(4,barrel.removeItemNoUpdate(5));
            f.level.setBlock(at,Blocks.AIR.defaultBlockState(),2);
            var body=EntityType.ARMOR_STAND.create(f.level);h.assertTrue(body!=null,"native original resident created");body.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-8)));f.level.addFreshEntity(body);f.bodies.add(body);var id=body.getUUID();
            LabyrinthHazards.upgradeFlooded(f.level,f.origin);h.assertTrue(!f.repaired()&&!body.isRemoved()&&body.getUUID().equals(id),"an actual resident keeps the old geometry and its native identity");body.discard();
            LabyrinthHazards.upgradeFlooded(f.level,f.origin);var water=LabyrinthHazards.floodRoute(f.base).getFirst();
            h.assertTrue(f.repaired()&&f.level.getBlockState(water).is(Blocks.WATER)&&ItemStack.matches(original,camera.getInventory().getItem(4)),"the loaded vacant scene finishes the original physical water trial and preserves the removed original");
            f.level.setBlock(water,Blocks.AIR.defaultBlockState(),2);var data=LabyrinthData.get(f.server);
            var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),f.level.registryAccess()),f.level.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            LabyrinthHazards.upgradeFlooded(f.level,f.origin);h.assertTrue(f.repaired()&&f.level.getBlockState(water).isAir(),"the saved checkpoint preserves a later removal instead of rebuilding the scene");
        }).thenSucceed();
    }
}
