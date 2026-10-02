package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.SceneRecord;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PlaytestRepairsTests {
    @GameTest(template="empty") public static void paperNeedsAFullHeightFlatTop(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(3,3,3));
        l.setBlock(p.below(),HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL),3);
        h.assertTrue(!NoteSurfaceBlock.supported(l,p),"a low stool cannot suspend writing at block height");
        l.setBlock(p.below(),HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.FORMICA_TABLE),3);
        h.assertTrue(NoteSurfaceBlock.supported(l,p),"a full table supports the whole sheet");h.succeed();
    }
    @GameTest(template="empty") public static void witchCanCrossTheSurfaceButNeverLivingGrass(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(new BlockPos(2,4,4));
        for(int x=0;x<=10;x++){l.setBlock(b.offset(x,-1,-2),Blocks.WATER.defaultBlockState(),3);l.setBlock(b.offset(x,0,-2),Blocks.AIR.defaultBlockState(),3);l.setBlock(b.offset(x,1,-2),Blocks.AIR.defaultBlockState(),3);}
        h.assertTrue(LakeWitchEntity.walkable(l,b,b.offset(4,0,-2)),"source water below the feet is a traversable surface");
        h.assertTrue(!LakeWitchEntity.shoreRoute(l,b,b.offset(0,0,-2),b.offset(10,0,-2)).isEmpty(),"the real path includes water surface nodes");
        l.setBlock(b.offset(4,-1,-2),Blocks.GRASS_BLOCK.defaultBlockState(),3);
        h.assertTrue(!LakeWitchEntity.walkable(l,b,b.offset(4,0,-2)),"living grass still excludes a hunt node");h.succeed();
    }
    @GameTest(template="empty") public static void scenePhotographHasActualVisibleRoomColours(GameTestHelper h){
        var l=h.getLevel();var b=h.absolutePos(new BlockPos(3,3,3));
        for(int x=-3;x<=3;x++)for(int y=0;y<=4;y++)l.setBlock(b.offset(x,y,-6),(x<0?Blocks.RED_CONCRETE:Blocks.BLUE_CONCRETE).defaultBlockState(),3);
        var record=new SceneRecord(l,b.offset(-4,-1,-7),b.offset(4,5,1),b.getCenter().add(0,1,0),new Vec3(0,0,-1));while(!record.tick()){}
        var item=record.finish();var id=item.get(DataComponents.MAP_ID);var data=l.getMapData(id);Set<Byte> colours=new HashSet<>();for(byte c:data.colors)colours.add(c);
        h.assertTrue(colours.size()>4,"the native photograph contains varied scene colours rather than a black arrival frame");h.succeed();
    }
    @GameTest(template="empty") public static void albumOriginalIsPersonalAndFinite(GameTestHelper h){
        var server=h.getLevel().getServer();var oldHouse=HouseSavedData.get(server);var oldData=LabyrinthData.get(server);
        var l=HouseTestLevel.get(server);var origin=new BlockPos(60100,80,60100);var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);var data=new LabyrinthData();server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
        var p=h.makeMockServerPlayerInLevel();var peer=h.makeMockServerPlayerInLevel();
        try{
            var b=LabyrinthPlaces.base(origin,LabyrinthPlace.KAREN_ROOM);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-5,0,0);
            var photo=net.minecraft.world.item.MapItem.create(l,0,0,(byte)0,false,false);var own=NovelVignettes.personal(data,p.getUUID());ListTag list=new ListTag();list.add(photo.save(p.registryAccess()));own.put("Record",list);NovelVignettes.save(data,p.getUUID(),own);
            var album=new SecretPhotographs.Album(1,p.getInventory(),p);h.assertTrue(!album.getSlot(0).getItem().isEmpty(),"the owner receives her actual native stored map");
            album.clicked(0,0,ClickType.PICKUP,p);h.assertTrue(!album.getCarried().isEmpty(),"ordinary native pickup takes the photograph");
            var next=new SecretPhotographs.Album(2,p.getInventory(),p);h.assertTrue(next.getSlot(0).getItem().isEmpty(),"reopening does not mint another original");
            var other=new SecretPhotographs.Album(3,peer.getInventory(),peer);h.assertTrue(other.getSlot(0).getItem().isEmpty(),"another player cannot take the owner's photograph");h.succeed();
        }finally{server.getPlayerList().remove(p);server.getPlayerList().remove(peer);server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);}
    }
    private static HouseSavedData oldHouse;private static LabyrinthData oldData;private static MotherCollection oldMother;private static ServerPlayer owner;private static MotherEntity mother;private static MotherPekingese dog;private static BlockPos den;
    @AfterBatch(batch="mother_stairs") public static void cleanup(ServerLevel l){
        var server=l.getServer();if(owner!=null)server.getPlayerList().remove(owner);if(mother!=null)mother.discard();if(dog!=null)dog.discard();
        if(oldHouse!=null){server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);}owner=null;mother=null;dog=null;den=null;
    }
    @GameTest(template="empty",batch="mother_stairs",timeoutTicks=750) public static void motherPhysicallyClimbsWhileCarryingTheSameDog(GameTestHelper h){
        var server=h.getLevel().getServer();var l=HouseTestLevel.get(server);oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);oldMother=MotherCollection.get(server);
        var origin=new BlockPos(61000,80,61000);var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);var collection=new MotherCollection();server.overworld().getDataStorage().set("the_oldest_house_mother",collection);
        den=LabyrinthPlaces.base(origin,LabyrinthPlace.MOTHER_DEN);MotherOfStrays.build(server,l,den);IndianLakeRooms.keepLoaded(l,den,LabyrinthPlace.MOTHER_DEN);
        owner=h.makeMockServerPlayerInLevel();owner.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);owner.teleportTo(l,den.getX()+.5,den.getY(),den.getZ()-8.5,180,0);owner.hasChangedDimension();
        mother=MotherRegistry.MOTHER.get().create(l);mother.moveTo(Vec3.atBottomCenterOf(den.offset(-5,0,-9)));mother.galleryStep(1);l.addFreshEntity(mother);
        dog=MotherRegistry.PEKINGESE.get().create(l);dog.addTag("the_oldest_house_bandaged_dog");dog.moveTo(mother.position());l.addFreshEntity(dog);UUID id=dog.getUUID();mother.carryDog(dog);
        collection.beginDogThreat(owner.getUUID());collection.advance(600,false,true);
        // Any preexisting den keeper would own the script; keep this single native actor authoritative.
        l.getEntitiesOfClass(MotherEntity.class,IndianLakeRooms.bounds(den,LabyrinthPlace.MOTHER_DEN),e->e!=mother).forEach(Entity::discard);
        final double[] last={mother.getY()};final boolean[] halfway={false};
        h.onEachTick(()->{
            IndianLakeRooms.keepLoaded(l,den,LabyrinthPlace.MOTHER_DEN);owner.setDeltaMovement(Vec3.ZERO);
            h.assertTrue(mother.getY()-last[0]<1.6,"the climb uses physical navigation rather than jumping to the gallery");last[0]=mother.getY();halfway[0]|=mother.getY()>den.getY()+3&&mother.getY()<den.getY()+8;
            h.assertTrue(dog.getUUID().equals(id),"carrying keeps the original living dog's UUID");
        });
        h.succeedWhen(()->{
            h.assertTrue(collection.dogAtLedge()&&mother.getY()>den.getY()+8.6,"the real Mother reaches the gallery before the intervention clock starts: "+mother.position());
            h.assertTrue(halfway[0],"the actor passes through intermediate stair heights");h.assertTrue(dog.position().distanceToSqr(mother.position())<3,"the same dog follows her hands continuously");
            h.assertTrue(collection.dogLedgeTicks()<40,"navigation does not spend the final rescue window");
        });
    }
}
