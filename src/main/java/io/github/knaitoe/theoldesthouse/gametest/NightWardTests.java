package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_nightward") @PrefixGameTestTemplate(false)
public final class NightWardTests {
    private static NativeTestChunks chunks;private static HouseSavedData oldHouse;private static LabyrinthData oldData;
    private static ServerLevel level;private static FakePlayer reader;private static Cat resident;
    @AfterBatch(batch="night_ward_materials0507") public static void clean(ServerLevel ignored){
        if(reader!=null)reader.discard();if(resident!=null)resident.discard();if(chunks!=null)chunks.close();
        if(level!=null){var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}
        chunks=null;level=null;reader=null;resident=null;NovelVignettes.clearAll();
    }
    @GameTest(template="empty",batch="night_ward_materials0507",timeoutTicks=2400)
    public static void aSavedWardRefitsOnlyWhenVacantAndKeepsItsActualChartCallsAndFinishedNight(GameTestHelper h){
        var server=h.getLevel().getServer();level=HouseTestLevel.get(server);oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
        var origin=new BlockPos(37000,80,37000);var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
        var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
        var b=LabyrinthPlaces.base(origin,LabyrinthPlace.HOSPITAL);chunks=new NativeTestChunks();chunks.hold(level,new AABB(Vec3.atLowerCornerOf(b.offset(-14,-2,-27)),Vec3.atLowerCornerOf(b.offset(15,9,4))));
        NovelRooms.build(server,level,b,LabyrinthPlace.HOSPITAL);
        reader=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"ward_reader"));reader.setNoGravity(true);reader.moveTo(Vec3.atBottomCenterOf(b.offset(0,0,-3)));level.addNewPlayer(reader);
        h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready(),"native block and entity sections are ready"))
            .thenExecute(()->{
                var chart=(LecternBlockEntity)level.getBlockEntity(b.offset(NovelRooms.WARD_NOTE));var original=chart.getBook().copy();
                var own=NovelVignettes.personal(data,reader.getUUID());own.putBoolean("WardFinished",true);own.putInt("WardTicks",NovelVignettes.WARD_NIGHT);own.putInt("WardCalls",7);NovelVignettes.save(data,reader.getUUID(),own);
                level.setBlock(b.offset(-9,2,-12),NovelRegistry.INSTITUTE.get().defaultBlockState(),2);NovelRooms.bed(level,b.offset(-7,0,-7),Blocks.WHITE_BED,Direction.NORTH);
                NovelRooms.door(level,b.offset(0,0,0),Direction.SOUTH,Blocks.BIRCH_DOOR,true);
                for(int y=0;y<2;y++){var at=b.offset(0,y,0);level.setBlock(at,level.getBlockState(at).setValue(DoorBlock.HINGE,DoorHingeSide.RIGHT),2);}
                var button=b.offset(NovelRooms.BUTTON);level.setBlock(button,level.getBlockState(button).setValue(ButtonBlock.POWERED,true),2);
                var kept=b.offset(7,0,-21);level.setBlock(kept,Blocks.CHEST.defaultBlockState(),2);((ChestBlockEntity)level.getBlockEntity(kept)).setItem(4,original.copy());
                var mark=data.state(NightWardMaterials.STATE);mark.remove(Long.toString(b.asLong()));data.setState(NightWardMaterials.STATE,mark);
                h.assertTrue(!NightWardMaterials.apply(level,b),"a reader's actual camera keeps the saved ward untouched");
                reader.moveTo(100,80,100);resident=EntityType.CAT.create(level);h.assertTrue(resident!=null,"the native resident can be created");resident.setNoAi(true);resident.setNoGravity(true);resident.moveTo(Vec3.atBottomCenterOf(b.offset(3,0,-5)));resident.tame(reader);resident.setHealth(4);var identity=resident.getUUID();
                h.assertTrue(level.addFreshEntity(resident)&&level.getEntity(identity)==resident,"the actual owned resident passes native spawn rules before testing its guard");
                h.assertTrue(!NightWardMaterials.apply(level,b),"a living resident also defers the entire cosmetic refit");resident.moveTo(100,80,110);
                h.assertTrue(NightWardMaterials.apply(level,b),"the loaded, unseen, vacant hospital receives its own materials");
                h.assertTrue(level.getBlockState(b.offset(-9,2,-12)).is(NovelRegistry.NIGHT_WARD.get())&&level.getBlockState(b.offset(-7,0,-7)).getValue(NightWardBlock.KIND)==NightWardBlock.Kind.BED_FOOT,"walls and the authored bed become native ward fittings");
                h.assertTrue(ItemStack.isSameItemSameComponents(original,chart.getBook())&&ItemStack.isSameItemSameComponents(original,((ChestBlockEntity)level.getBlockEntity(kept)).getItem(4)),"the actual source chart and a stored original keep every item component");
                var door=level.getBlockState(b.offset(0,0,0));h.assertTrue(door.is(NovelRegistry.NIGHT_WARD_DOOR.get())&&door.getValue(DoorBlock.OPEN)&&door.getValue(DoorBlock.HINGE)==DoorHingeSide.RIGHT&&level.getBlockState(button).getValue(ButtonBlock.POWERED),"the native doorway and call state survive the material change");
                h.assertTrue(resident.getUUID().equals(identity)&&resident.getOwnerUUID().equals(reader.getUUID())&&resident.getHealth()==4&&NovelVignettes.personal(data,reader.getUUID()).getInt("WardCalls")==7&&NovelVignettes.personal(data,reader.getUUID()).getBoolean("WardFinished")&&!WitnessAccount.has(data,reader.getUUID(),WitnessAccount.Story.HOSPITAL),"resident identity, ownership and health and the completed night remain, with no unearned resolution");
                level.setBlock(b.offset(-7,0,-7),Blocks.AIR.defaultBlockState(),2);var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new net.minecraft.nbt.CompoundTag(),level.registryAccess()),level.registryAccess());server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
                h.assertTrue(NightWardMaterials.apply(level,b)&&level.getBlockState(b.offset(-7,0,-7)).isAir()&&ItemStack.isSameItemSameComponents(original,chart.getBook()),"the saved checkpoint does not rebuild a removed fitting or rewrite the chart");
            }).thenSucceed();
    }
}
