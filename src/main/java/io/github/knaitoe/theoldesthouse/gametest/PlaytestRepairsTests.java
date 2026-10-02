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
        if(den!=null){var scene=server.getLevel(HouseDimensions.INTERIOR);if(scene!=null)scene.getEntitiesOfClass(Entity.class,IndianLakeRooms.bounds(den,LabyrinthPlace.MOTHER_DEN),e->e instanceof MotherEntity||e instanceof TamableAnimal||e instanceof net.minecraft.world.entity.decoration.ItemFrame).forEach(Entity::discard);}
        if(oldHouse!=null){server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);}owner=null;mother=null;dog=null;den=null;
    }
    @GameTest(template="empty",batch="mother_stairs",timeoutTicks=750) public static void motherPhysicallyClimbsWhileCarryingTheSameDog(GameTestHelper h){
        var server=h.getLevel().getServer();var l=HouseTestLevel.get(server);oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);oldMother=MotherCollection.get(server);
        var origin=new BlockPos(61000,80,61000);var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);var collection=new MotherCollection();server.overworld().getDataStorage().set("the_oldest_house_mother",collection);
        den=LabyrinthPlaces.base(origin,LabyrinthPlace.MOTHER_DEN);MotherOfStrays.build(server,l,den);IndianLakeRooms.keepLoaded(l,den,LabyrinthPlace.MOTHER_DEN);
        owner=h.makeMockServerPlayerInLevel();owner.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);owner.teleportTo(l,den.getX()+.5,den.getY(),den.getZ()-8.5,180,0);owner.hasChangedDimension();
        // Use the actual constructed keeper and dog after their native entity chunks have loaded.
        final UUID[] id={null};final double[] last={den.getY()};final boolean[] halfway={false};
        h.runAfterDelay(20,()->{
            var keepers=l.getEntitiesOfClass(MotherEntity.class,IndianLakeRooms.bounds(den,LabyrinthPlace.MOTHER_DEN));var dogs=l.getEntitiesOfClass(MotherPekingese.class,IndianLakeRooms.bounds(den,LabyrinthPlace.MOTHER_DEN),e->e.getTags().contains("the_oldest_house_bandaged_dog"));
            h.assertTrue(keepers.size()==1&&dogs.size()==1,"the native den has one keeper and one original bandaged dog");mother=keepers.getFirst();dog=dogs.getFirst();id[0]=dog.getUUID();last[0]=mother.getY();
            h.assertTrue(MotherOfStrays.inDen(owner),"the actual owner is present in the den");collection.beginDogThreat(owner.getUUID());collection.advance(600,false,true);
        });
        h.onEachTick(()->{
            IndianLakeRooms.keepLoaded(l,den,LabyrinthPlace.MOTHER_DEN);owner.setDeltaMovement(Vec3.ZERO);
            if(mother==null)return;
            h.assertTrue(mother.getY()-last[0]<1.6,"the climb uses physical navigation rather than jumping to the gallery");last[0]=mother.getY();halfway[0]|=mother.getY()>den.getY()+3&&mother.getY()<den.getY()+8;
            h.assertTrue(dog.getUUID().equals(id[0]),"carrying keeps the original living dog's UUID");
        });
        h.succeedWhen(()->{
            h.assertTrue(mother!=null&&collection.dogAtLedge()&&mother.getY()>den.getY()+8.6,"the real Mother reaches the gallery before the intervention clock starts: "+(mother==null?"loading":mother.position()+" step="+mother.galleryStep()+" ticks="+collection.dogThreatTicks()+" active="+MotherOfStrays.activeDogThreat(l)+" path="+mother.getNavigation().getPath()));
            h.assertTrue(halfway[0],"the actor passes through intermediate stair heights");h.assertTrue(dog.position().distanceToSqr(mother.position())<3,"the same dog follows her hands continuously");
            h.assertTrue(collection.dogLedgeTicks()<40,"navigation does not spend the final rescue window");
        });
    }
    private static MigrationFixture migration;
    private static final class MigrationFixture implements AutoCloseable {
        final net.minecraft.server.MinecraftServer server;final HouseSavedData house;final LabyrinthData data;final ServerLevel from,to;final BlockPos origin,old,dest;ServerPlayer player;final List<UUID> actors=new ArrayList<>();
        MigrationFixture(GameTestHelper h){
            server=h.getLevel().getServer();house=HouseSavedData.get(server);data=LabyrinthData.get(server);from=HouseTestLevel.get(server);to=HouseTestLevel.get(server,HouseDimensions.OUTSIDE);origin=new BlockPos(65000,80,65000);
            var nextHouse=new HouseSavedData();nextHouse.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",nextHouse);var nextData=new LabyrinthData();nextData.setBuilt(22,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",nextData);
            dest=LabyrinthPlaces.base(origin,LabyrinthPlace.DROWNED_TOWN);old=dest.offset(0,0,4096+LabyrinthPlace.DROWNED_TOWN.slot()*192);IndianLakeRooms.keepLoaded(from,old,LabyrinthPlace.DROWNED_TOWN);IndianLakeRooms.keepLoaded(to,dest,LabyrinthPlace.DROWNED_TOWN);
        }
        public void close(){if(player!=null)server.getPlayerList().remove(player);for(var level:List.of(from,to))for(UUID id:actors)if(level.getEntity(id)!=null)level.getEntity(id).discard();server.overworld().getDataStorage().set("the_oldest_house",house);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();}
    }
    @AfterBatch(batch="lake_migration") public static void cleanMigration(ServerLevel l){if(migration!=null){migration.close();migration=null;}}
    @GameTest(template="empty",batch="lake_migration",timeoutTicks=220) public static void legacyLakeMigrationKeepsOriginalsActorsAndPersonalReturns(GameTestHelper h){
        migration=new MigrationFixture(h);var f=migration;var data=LabyrinthData.get(f.server);var deskAt=f.old.offset(-19,-10,-28);
        f.from.setBlock(f.old.offset(0,-1,-3),Blocks.COARSE_DIRT.defaultBlockState(),3);f.from.setBlock(deskAt,Blocks.BARREL.defaultBlockState(),3);var desk=(net.minecraft.world.level.block.entity.BarrelBlockEntity)f.from.getBlockEntity(deskAt);var original=new ItemStack(Items.DIAMOND,3);original.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("The remaining original"));desk.setItem(4,original);desk.setItem(0,ItemStack.EMPTY);
        var resident=new net.minecraft.world.entity.decoration.ArmorStand(f.from,f.old.getX()-19.5,f.old.getY()-11,f.old.getZ()-29.5);resident.setNoGravity(true);f.from.addFreshEntity(resident);f.actors.add(resident.getUUID());
        var witch=DrownedTownRegistry.LAKE_WITCH.get().create(f.from);witch.shore(f.old,3);witch.moveTo(Vec3.atBottomCenterOf(f.old.offset(23,0,-6)));f.from.addFreshEntity(witch);f.actors.add(witch.getUUID());
        f.player=h.makeMockServerPlayerInLevel();f.player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);var p=Vec3.atBottomCenterOf(f.old.offset(-18,-11,-29));f.player.teleportTo(f.from,p.x,p.y,p.z,180,0);data.pushReturn(f.player.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,p,180,true));UUID absent=UUID.randomUUID();data.pushReturn(absent,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,p,90,true));
        h.succeedWhen(()->{
            h.assertTrue(LakeLandscape.upgradeWorld(f.server,f.origin),"native old entity chunks finish loading before migration");var next=(net.minecraft.world.level.block.entity.BarrelBlockEntity)f.to.getBlockEntity(f.dest.offset(-19,1,-28));
            h.assertTrue(next!=null&&next.getItem(0).isEmpty()&&ItemStack.isSameItemSameComponents(next.getItem(4),original)&&next.getItem(4).getCount()==3,"the raised native desk keeps finite original contents without restocking: "+(next==null?f.to.getBlockState(f.dest.offset(-19,1,-28)):next.getItem(4))+" original="+original);
            var moved=f.to.getEntity(resident.getUUID());var hunter=f.to.getEntity(witch.getUUID());h.assertTrue(moved!=null&&Math.abs(moved.getY()-f.dest.getY())<.01,"the same resident is raised above the new dry ground");h.assertTrue(hunter instanceof LakeWitchEntity w&&w.shoreBase().equals(f.dest),"the same Witch retains her relocated hunting base");
            h.assertTrue(f.player.serverLevel()==f.to&&Math.abs(f.player.getY()-f.dest.getY())<.01,"a present school visitor moves with the native room");var point=data.popReturn(f.player.getUUID());var offline=data.popReturn(absent);h.assertTrue(point!=null&&offline!=null&&point.dimension().equals(HouseDimensions.OUTSIDE)&&offline.dimension().equals(HouseDimensions.OUTSIDE)&&Math.abs(point.pos().y-f.dest.getY())<.01&&offline.yaw()==90&&offline.door(),"both present and absent players retain translated personal return waypoints");
            h.assertTrue(f.from.getBlockState(deskAt).isAir(),"the old original is removed only after its new copy is preserved");
            h.assertTrue(f.from.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,IndianLakeRooms.bounds(f.old,LabyrinthPlace.DROWNED_TOWN),e->e.getItem().is(Items.DIAMOND)).isEmpty()&&f.to.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,IndianLakeRooms.bounds(f.dest,LabyrinthPlace.DROWNED_TOWN),e->e.getItem().is(Items.DIAMOND)).isEmpty(),"clearing copied native containers never spills a duplicate of their original items");
        });
    }
}
