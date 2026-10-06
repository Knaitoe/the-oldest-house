package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class NotesRepairTests {
    private static final class Fixture implements AutoCloseable {
        final net.minecraft.server.MinecraftServer server;final HouseSavedData oldHouse;final LabyrinthData oldData;
        final BlockPos origin;final LabyrinthData data=new LabyrinthData();final List<ServerPlayer> players=new ArrayList<>();final List<Entity> entities=new ArrayList<>();
        Fixture(GameTestHelper h,int at){server=h.getLevel().getServer();origin=new BlockPos(at,80,at);oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);data.setBuilt(LabyrinthBuilder.VERSION,origin);}
        ServerPlayer player(GameTestHelper h,ServerLevel level,Vec3 at){level.getChunkAt(BlockPos.containing(at));var p=NativeTestPlayers.survival(h,"notes_repair");p.teleportTo(level,at.x,at.y,at.z,0,0);players.add(p);return p;}
        public void close(){players.forEach(NativeTestPlayers::remove);entities.forEach(Entity::discard);server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);LabyrinthBuilder.clearAll();FinaleArchitecture.clearAll();}
    }
    @GameTest(template="empty") public static void actualChalkWorksOutsideAndDimensionOwnershipDoesNotEraseAnInteriorMark(GameTestHelper h){
        try(var f=new Fixture(h,99600)){
            var outside=h.getLevel();var inside=HouseTestLevel.get(f.server,HouseDimensions.INTERIOR);var at=h.absolutePos(BlockPos.ZERO).offset(30,5,0);
            for(var level:List.of(outside,inside)){level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),2);level.setBlock(at,Blocks.AIR.defaultBlockState(),2);}
            var p=f.player(h,outside,Vec3.atBottomCenterOf(at));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(LabyrinthRegistry.CHALK.get()));
            var hit=new BlockHitResult(at.below().getCenter(),Direction.UP,at.below(),false);
            h.assertTrue(p.getMainHandItem().getItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit))==InteractionResult.CONSUME&&outside.getBlockState(at).is(HouseBlocks.CHALK_MARK.get()),"using the actual chalk item places a native chalk mark in the ordinary world");
            p.teleportTo(inside,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);p.getMainHandItem().getItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit));
            p.teleportTo(outside,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);h.assertTrue(NavigationAids.erase(p,at)&&outside.getBlockState(at).isAir()&&inside.getBlockState(at).is(HouseBlocks.CHALK_MARK.get()),"the same coordinates in two dimensions retain distinct owned chalk originals");h.succeed();
        }
    }
    @GameTest(template="empty") public static void nativeChildRoomDoorIsLockedEarlyAndNormalAtTheRelevantVisit(GameTestHelper h){
        try(var f=new Fixture(h,100200)){
            var level=HouseTestLevel.get(f.server,HouseDimensions.INTERIOR);var b=LabyrinthPlaces.base(f.origin,LabyrinthPlace.MODEL_HOME);ModelHome.build(f.server,level,b);var at=b.offset(ModelHome.KIDS_DOOR);
            var p=f.player(h,level,Vec3.atBottomCenterOf(at.south()));var early=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));NeoForge.EVENT_BUS.post(early);
            h.assertTrue(early.isCanceled()&&!level.getBlockState(at).getValue(DoorBlock.OPEN),"the real early child door cannot be opened by the registered native interaction");
            var state=f.data.state(ModelHome.ID);state.putInt("Visit",3);f.data.setState(ModelHome.ID,state);var relevant=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));ModelHome.onRightClickBlock(relevant);
            h.assertTrue(!relevant.isCanceled()&&ModelHome.kidsRoomRelevant(f.data),"the same native door returns to ordinary use when the third visit makes the child's room relevant");h.succeed();
        }
    }
    @GameTest(template="empty") public static void loosePagesHaveDistinctHandsAndRemainOneOriginalAfterCollectionAndReload(GameTestHelper h){
        try(var f=new Fixture(h,100800)){
            var level=HouseTestLevel.get(f.server,HouseDimensions.INTERIOR);var at=FinaleArchitecture.base(f.origin).offset(-20,FinaleArchitecture.TOP-100,-20);level.setBlock(at.below(),Blocks.STONE.defaultBlockState(),2);level.setBlock(at,NoteSurfaceBlock.state(HouseMarginalia.Thread.POEMS,Direction.WEST),2);
            var p=f.player(h,level,Vec3.atBottomCenterOf(at.east()));h.assertTrue(StaircaseWriting.open(p,at),"an actual supported stair sheet opens the native reader");var menu=(LecternMenu)p.containerMenu;var book=menu.getSlot(0).getItem();var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);String text=content.pages().stream().map(page->page.raw().getString()).reduce("",String::concat);
            h.assertTrue(!text.contains("STAIR PAGE")&&!text.contains("LIMEN")&&!text.contains("second hand")&&!text.contains("stairs do not forget")&&content.pages().getFirst().raw().getStyle().getFont()!=null,"the native paper uses a writer's actual font and concrete prose without repeating lectures or captions");
            h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3),"taking the actual sheet is finite for its reader");p.closeContainer();var loaded=LabyrinthData.FACTORY.deserializer().apply(f.data.save(new CompoundTag(),p.registryAccess()),p.registryAccess());f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);StaircaseWriting.open(p,at);h.assertTrue(!((LecternMenu)p.containerMenu).clickMenuButton(p,3)&&ItemStack.isSameItemSameComponents(book,((LecternMenu)p.containerMenu).getSlot(0).getItem()),"the collected signed original and its native reader snapshot survive reload without a replacement");h.succeed();
        }
    }
    @GameTest(template="empty") public static void actualAmbientChickensAreRejectedAndOwnedPetsKeepTheirIdentity(GameTestHelper h){
        try(var f=new Fixture(h,101400)){
            var level=HouseTestLevel.get(f.server,HouseDimensions.INTERIOR);var at=FinaleArchitecture.cell(f.origin);var chicken=EntityType.CHICKEN.create(level);chicken.moveTo(Vec3.atBottomCenterOf(at));var spawn=new EntityJoinLevelEvent(chicken,level);LabyrinthSpawnRules.onEntityJoinLevel(spawn);
            var cat=EntityType.CAT.create(level);cat.setTame(true,true);cat.setOwnerUUID(UUID.randomUUID());cat.moveTo(Vec3.atBottomCenterOf(at));UUID id=cat.getUUID();var kept=new EntityJoinLevelEvent(cat,level);LabyrinthSpawnRules.onEntityJoinLevel(kept);
            h.assertTrue(spawn.isCanceled()&&!kept.isCanceled()&&cat.getUUID().equals(id),"native natural chicken joins are rejected while an actual owned cat keeps its living identity");chicken.discard();cat.discard();h.succeed();
        }
    }
    @GameTest(template="empty") public static void caverDoorConnectsToTheActualCampAndLeavesFiniteToolsAlone(GameTestHelper h){
        try(var f=new Fixture(h,102000)){
            var l=h.getLevel();var b=h.absolutePos(BlockPos.ZERO).offset(40,6,40);CaverCave.build(f.server,l,b);var cache=b.offset(CaverCave.CACHE);var before=l.getBlockEntity(cache);
            var p=f.player(h,l,Vec3.atBottomCenterOf(b.offset(0,0,0)));h.assertTrue(l.noCollision(p,p.getBoundingBox()),"the actual arrival body clears the once-solid gap between the caver door and camp");CaverCave.repairEntrance(l,b);h.assertTrue(l.getBlockEntity(cache)==before,"the entrance repair leaves the original cache block entity in place");h.succeed();
        }
    }
    @GameTest(template="empty") public static void nativeTransformedFightBlocksAndWoundsWithoutARepeatedCollisionSizeSearch(GameTestHelper h)throws Exception{
        try(var f=new Fixture(h,102600)){
            var level=h.getLevel();var at=h.absolutePos(BlockPos.ZERO).offset(70,8,70);
            for(int x=-3;x<=14;x++)for(int z=-4;z<=4;z++){level.setBlock(at.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);for(int y=0;y<=5;y++)level.setBlock(at.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
            var p=f.player(h,level,Vec3.atBottomCenterOf(at));p.setYRot(-90);p.setYHeadRot(-90);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(NovelRegistry.HOLLOWAY_SHIELD.get()));p.startUsingItem(InteractionHand.OFF_HAND);
            for(int i=0;i<8;i++)p.doTick();h.assertTrue(p.isBlocking(),"the actual survival player raises a native shield before the charge");
            var weapon=new ItemStack(Items.IRON_SWORD);p.setItemInHand(InteractionHand.MAIN_HAND,weapon);WeaponHistory.record(p,weapon,1);var own=new CompoundTag();own.putString("Phase",FinaleProgress.Phase.FIGHT.name());own.putUUID("Weapon",WeaponHistory.identity(weapon));FinaleProgress.save(f.server,p.getUUID(),own);
            var creature=FinaleRegistry.MINOTAUR.get().create(level);f.entities.add(creature);creature.caged();creature.moveTo(Vec3.atBottomCenterOf(at.offset(10,0,0)));level.addFreshEntity(creature);UUID id=creature.getUUID();creature.awaken(p.getUUID());
            long total=0,max=0;int samples=0;boolean charge=false,blocked=false;
            for(int i=0;i<250&&!blocked;i++){
                long start=System.nanoTime();creature.tick();long cost=System.nanoTime()-start;
                if(i>=10){total+=cost;max=Math.max(max,cost);samples++;}
                charge|=creature.motion()==MinotaurEntity.CHARGING;blocked=creature.motion()==MinotaurEntity.STUNNED;
                h.assertTrue(p.isAlive()&&creature.getBbHeight()>3&&creature.getUUID().equals(id),"transformation and attack phases retain one adult collision body and living survival target");
            }
            h.assertTrue(charge&&blocked&&p.getOffhandItem().getDamageValue()>0,"the real animated creature rushes into a native raised shield and visibly staggers");
            double mean=total/(double)Math.max(1,samples)/1_000_000.0;
            h.assertTrue(mean<50,"the warmed native creature tick fits one server tick; mean="+mean+" ms, max="+max/1_000_000.0+" ms");
            var folder=java.nio.file.Path.of("../build/combat-proof");java.nio.file.Files.createDirectories(folder);java.nio.file.Files.writeString(folder.resolve("minotaur-ticks.txt"),"Native transform / charge / shield collision.\nSamples: "+samples+"; mean: "+mean+" ms; maximum: "+max/1_000_000.0+" ms.\n");
            h.assertTrue(creature.hurt(level.damageSources().playerAttack(p),5)&&creature.motion()==MinotaurEntity.WOUNDED&&creature.isAlive()&&creature.getUUID().equals(id)&&FinaleProgress.phase(f.server,p.getUUID())==FinaleProgress.Phase.COLLAPSE,"the actual original weapon wounds the same living prisoner and advances the personal ending");h.succeed();
        }
    }
    @GameTest(template="empty") public static void retiredNativeSideDoorRemapsBothExplorersAndPreservesOriginalPaperAndChest(GameTestHelper h){
        try(var f=new Fixture(h,103000)){
            var l=h.getLevel();var place=LabyrinthPlace.STRAIGHT_HALL;var b=h.absolutePos(BlockPos.ZERO).offset(120,8,120);LabyrinthHalls.build(l,b,place);
            var at=b.offset(-2,0,-7);var door=new LabyrinthData.Door("straight_hall/west_near",l.dimension(),at,Direction.EAST,LabyrinthData.DEALT,false);f.data.putDoor(door);
            l.setBlock(at,Blocks.OAK_DOOR.defaultBlockState(),2);l.setBlock(at.above(),Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),2);
            UUID first=UUID.randomUUID(),second=UUID.randomUUID();var before=new LabyrinthData.Waypoint(l.dimension(),Vec3.atBottomCenterOf(at),0,true);f.data.pushReturn(first,before);f.data.pushReturn(second,before);
            var chest=b.offset(1,0,-4);l.setBlock(chest,Blocks.CHEST.defaultBlockState(),2);var original=(net.minecraft.world.level.block.entity.ChestBlockEntity)l.getBlockEntity(chest);original.setItem(0,new ItemStack(Items.EMERALD,2));
            var paper=b.offset(-1,0,-4);var sheet=NoteSurfaceBlock.state(HouseMarginalia.Thread.POEMS,Direction.WEST);l.setBlock(paper,sheet,2);
            DomesticHallUpgrade.apply(l,b,place);var onward=place.doors().stream().filter(d->d.name().equals("far")).findFirst().orElseThrow();var expected=Vec3.atBottomCenterOf(b.offset(onward.rel()));
            h.assertTrue(f.data.door(door.id)==null&&!(l.getBlockState(at).getBlock() instanceof DoorBlock)&&f.data.popReturn(first).pos().equals(expected)&&f.data.popReturn(second).pos().equals(expected),"both saved native door returns move to the usable end before the obsolete opening closes");
            DomesticHallUpgrade.apply(l,b,place);h.assertTrue(l.getBlockEntity(chest)==original&&original.getItem(0).getCount()==2&&l.getBlockState(paper).equals(sheet),"repeat repairs keep the actual chest, finite emeralds and physical paper original");h.succeed();
        }
    }
    @GameTest(template="empty") public static void loopCooldownSurvivesRestartAndCannotAppearBeforeTwelveCrossings(GameTestHelper h){
        var data=new LabyrinthData();UUID id=UUID.randomUUID(),peer=UUID.randomUUID();
        for(int i=0;i<11;i++)data.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));h.assertTrue(!LabyrinthPacing.loopDue(data,id),"eleven mundane crossings cannot offer a loop");
        data.pushReturn(id,new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.ZERO,0));h.assertTrue(LabyrinthPacing.loopDue(data,id),"a loop first becomes possible at twelve crossings");data.visit(id,LabyrinthPlace.LONG_HALLWAY);
        var saved=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        for(int i=0;i<7;i++){saved.visit(id,LabyrinthPlace.STRAIGHT_HALL);h.assertTrue(!LabyrinthPacing.loopDue(saved,id),"seven intervening visits still preserve breathing room after reload");}
        saved.visit(id,LabyrinthPlace.BENT_HALL);h.assertTrue(LabyrinthPacing.loopDue(saved,id)&&!LabyrinthPacing.loopDue(saved,peer),"the eighth intervening visit restores only the original explorer's chance");h.succeed();
    }
    private static Fixture tomFixture;
    private static net.minecraft.world.level.ChunkPos tomChunk;
    private static BlockPos tomTicket;
    @AfterBatch(batch="notes_tom") public static void cleanupTom(ServerLevel ignored){if(tomFixture!=null){var l=HouseTestLevel.get(tomFixture.server,HouseDimensions.INTERIOR);l.getChunkSource().removeRegionTicket(TicketType.PORTAL,tomChunk,3,tomTicket);tomFixture.close();tomFixture=null;}}
    @GameTest(template="empty",batch="notes_tom") public static void nativeTomCampHasAFloorFromTheEntryAndPreservesTheOriginalActorAndFire(GameTestHelper h){
        tomFixture=new Fixture(h,103200);var f=tomFixture;
            var l=HouseTestLevel.get(f.server,HouseDimensions.INTERIOR);var b=FinaleArchitecture.base(f.origin);var old=b.offset(-4,FinaleArchitecture.TOP,18);tomTicket=old;tomChunk=new net.minecraft.world.level.ChunkPos(old);l.getChunkSource().addRegionTicket(TicketType.PORTAL,tomChunk,3,old);l.getChunkAt(old);l.setBlock(old.below(),Blocks.STONE.defaultBlockState(),2);var actor=NovelRegistry.ACTOR.get().create(l);f.entities.add(actor);actor.appearance(UUID.randomUUID(),0);actor.moveTo(Vec3.atBottomCenterOf(old));l.addFreshEntity(actor);UUID id=actor.getUUID();var fire=b.offset(-6,FinaleArchitecture.TOP,20);l.setBlock(fire,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false),2);
            for(var placement:FinaleArchitecture.entrancePlan(f.origin)){l.getChunkAt(placement.pos());l.setBlock(placement.pos(),placement.block(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
            h.onEachTick(()->{if(l.getEntity(id)!=actor)return;FinaleRepairs.apply(l,f.origin);
            for(int x=0;x>=-12;x--){var at=b.offset(x,FinaleArchitecture.TOP,24);h.assertTrue(l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below())&&l.noCollision(null,new AABB(at.getX()+.2,at.getY()+.01,at.getZ()+.2,at.getX()+.8,at.getY()+1.8,at.getZ()+.8)),"a real survival body can walk from the actual first tread through the authored entrance into Tom's camp at "+at);}
            h.assertTrue(actor.getUUID().equals(id)&&actor.position().distanceToSqr(Vec3.atBottomCenterOf(FinaleRepairs.tom(f.origin)))<.01&&!l.getBlockState(b.offset(-13,FinaleArchitecture.TOP,24)).getValue(CampfireBlock.LIT)&&l.getBlockState(fire).isAir(),"the native Tom and actual extinguished campfire move without creating another original");h.succeed();
            });
    }

}
