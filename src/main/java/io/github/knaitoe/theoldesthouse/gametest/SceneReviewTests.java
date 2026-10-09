package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID+"_multiplayer")
@PrefixGameTestTemplate(false)
public final class SceneReviewTests {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static Fixture active;
    private static final class Fixture implements AutoCloseable{
        final GameTestHelper h;final LabyrinthPlace place;final ServerLevel level;final BlockPos origin,base;
        final NativeTestChunks chunks=new NativeTestChunks();final List<ServerPlayer> players=new ArrayList<>();
        HouseSavedData oldHouse;LabyrinthData oldData;boolean started,ready;
        Fixture(GameTestHelper h,LabyrinthPlace p,int coordinate){this.h=h;place=p;level=HouseTestLevel.get(h.getLevel().getServer(),NovelRooms.dimension(p));origin=new BlockPos(coordinate,0,coordinate);base=LabyrinthPlaces.base(origin,p);chunks.hold(level,SceneReview.area(base,p));active=this;}
        void start(){oldHouse=HouseSavedData.get(level.getServer());oldData=data();var house=new HouseSavedData();house.markSpawned(origin);house.markInteriorInitialized();var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",house);var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,origin);store.set("the_oldest_house_labyrinth",d);started=true;}
        LabyrinthData data(){return LabyrinthData.get(level.getServer());}
        void put(BlockPos rel,net.minecraft.world.level.block.state.BlockState state){level.setBlock(base.offset(rel),state,F);}
        void put(int x,int y,int z,Block b){put(new BlockPos(x,y,z),b.defaultBlockState());}
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);p.setNoGravity(true);players.add(p);return p;}
        void at(ServerPlayer p,double x,double y,double z){p.teleportTo(level,base.getX()+x,base.getY()+y,base.getZ()+z,180,0);p.connection.resetPosition();p.hasChangedDimension();p.setDeltaMovement(Vec3.ZERO);}
        void look(ServerPlayer p,BlockPos rel){p.lookAt(EntityAnchorArgument.Anchor.EYES,base.offset(rel).getCenter());}
        void click(ServerPlayer p,BlockPos rel){at(p,rel.getX()+.5,rel.getY(),rel.getZ()+2.5);look(p,rel);var at=base.offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(data(),p.getUUID(),place);}
        void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(data(),p.getUUID(),place,own);}
        void source(ServerPlayer p){click(p,LiteraryRooms.source(place));var menu=(LiteraryVignettes.Pages)p.containerMenu;menu.clickMenuButton(p,100+menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1);p.closeContainer();}
        void away(ServerPlayer p){p.teleportTo(h.getLevel(),.5,100,.5,0,0);p.connection.resetPosition();}
        void done(){close();h.succeed();}
        @Override public void close(){if(active!=this)return;players.forEach(NativeTestPlayers::remove);LiteraryVignettes.clearAll();NovelVignettes.clearAll();if(started&&place==LabyrinthPlace.BARN_WELL)Farmstead.forget(level.getServer(),origin);
            var area=SceneReview.area(base,place);for(var e:level.getEntitiesOfClass(Entity.class,area))e.discard();
            if(started){for(var at:BlockPos.betweenClosed(BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX,area.maxY,area.maxZ)))level.setBlock(at,Blocks.AIR.defaultBlockState(),F);
                var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}chunks.close();active=null;}
    }
    private static void run(GameTestHelper h,LabyrinthPlace p,int coordinate,Consumer<Fixture> check){var f=new Fixture(h,p,coordinate);h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"wait for actual native chunk and entity readiness")).thenExecute(()->{f.ready=true;f.start();check.accept(f);});}
    private static void cleanup(){if(active!=null)active.close();}
    @AfterBatch(batch="review_books") public static void booksDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_well") public static void wellDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_witch") public static void witchDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_shore") public static void shoreDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_archive") public static void archiveDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_fan") public static void fanDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_hunter_cover") public static void hunterCoverDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_witch_interval") public static void witchIntervalDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_source_custody") public static void sourceCustodyDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_live_collision_guard") public static void collisionGuardDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_farm_stock") public static void farmStockDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_farm_migration") public static void farmMigrationDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_farm_edges") public static void farmEdgesDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_well_pause") public static void wellPauseDone(ServerLevel l){cleanup();}

    @GameTest(template="empty",batch="review_books",timeoutTicks=1600)
    public static void realShelfQuillAndLaidOriginalRemainPrivateThroughSpeechAndRecovery(GameTestHelper h){run(h,LabyrinthPlace.CONFESSION,552000,f->{
        LiteraryRooms.build(f.level,f.base,f.place);SceneReview.fresh(f.level,f.origin,f.place);
        var owner=f.player("review_book_owner");var peer=f.player("review_book_peer");var spectator=f.player("review_book_spectator");spectator.setGameMode(GameType.SPECTATOR);
        f.at(owner,.5,0,-3);LiteraryVignettes.onArrive(owner,f.place);f.source(owner);
        f.click(owner,SceneReview.BLANK_SHELF);f.click(owner,SceneReview.BLANK_SHELF);h.assertTrue(owner.getInventory().countItem(Items.WRITABLE_BOOK)==1,"the real shelf yields one finite personal blank quill");
        f.click(spectator,SceneReview.BLANK_SHELF);h.assertTrue(spectator.getInventory().countItem(Items.WRITABLE_BOOK)==0,"a spectator cannot supply a journal");
        var quill=owner.getInventory().removeItemNoUpdate(0);quill.set(DataComponents.CUSTOM_NAME,Component.literal("Original shelf quill"));peer.setItemInHand(InteractionHand.MAIN_HAND,quill);f.away(owner);
        f.at(peer,1.5,0,-18);LiteraryVignettes.onArrive(peer,f.place);f.source(peer);f.at(peer,1.5,0,-18);f.look(peer,new BlockPos(1,1,-22));
        h.startSequence().thenIdle(50).thenExecute(()->{
            h.assertTrue(!f.own(peer).hasUUID("Journal")&&f.own(peer).getInt("Chapter")==0,"carrying another reader's actual quill cannot start a transcript");
            var original=peer.getMainHandItem().copy();peer.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);f.away(peer);
            owner.setItemInHand(InteractionHand.MAIN_HAND,original);f.click(owner,SceneReview.BOOK_TRAY);var laid=ConfessionBooks.tableBook(owner,f.own(owner));h.assertTrue(laid!=null&&owner.getMainHandItem().isEmpty(),"the native original actually leaves the hand and rests on the shared table");var id=laid.getUUID();
            spectator.setGameMode(GameType.SURVIVAL);spectator.setShiftKeyDown(true);f.click(spectator,SceneReview.BOOK_TRAY);h.assertTrue(laid.isAlive()&&spectator.getInventory().countItem(Items.WRITABLE_BOOK)==0,"another reader cannot recover the owner's table original");f.away(spectator);
            f.at(owner,1.5,0,-18);f.look(owner,new BlockPos(1,1,-22));
            h.startSequence().thenIdle(70).thenExecute(()->{
                var own=f.own(owner);var same=ConfessionBooks.tableBook(owner,own);h.assertTrue(same!=null&&same.getUUID().equals(id)&&own.hasUUID("Journal")&&!own.getString("JournalText").isBlank(),"actual speech writes into the same native laid quill");
                var components=same.getItem().copy();owner.setShiftKeyDown(true);f.click(owner,SceneReview.BOOK_TRAY);
                var recovered=owner.getInventory().items.stream().filter(s->s.is(Items.WRITABLE_BOOK)).findFirst().orElseThrow();
                h.assertTrue(!same.isAlive()&&ItemStack.isSameItemSameComponents(components,recovered)&&!f.own(owner).hasUUID("TableJournal"),"recovery returns exact surviving components, without an extra copy");
                f.click(owner,SceneReview.BLANK_SHELF);h.assertTrue(owner.getInventory().countItem(Items.WRITABLE_BOOK)==1&&WitnessAccount.count(f.data(),peer.getUUID())==0,"the shelf never refills and peer progress stays personal");f.done();
            });
        });
    });}

    @GameTest(template="empty",batch="review_well",timeoutTicks=1600)
    public static void coveredWaitAndOneSilhouetteFollowActualSharedLidWithoutPeerCredit(GameTestHelper h){run(h,LabyrinthPlace.BARN_WELL,552500,f->{
        NovelRooms.build(f.level.getServer(),f.level,f.base,f.place);var owner=f.player("review_well_owner");var peer=f.player("review_well_peer");var observer=f.player("review_well_observer");observer.setGameMode(GameType.SPECTATOR);
        f.at(owner,.5,-12,-22.5);f.at(peer,4.5,0,-20.5);f.at(observer,.5,-12,-22.5);
        var lid=(WellCoverBlockEntity)f.level.getBlockEntity(f.base.offset(NovelRooms.WELL));
        h.startSequence().thenIdle(30).thenExecute(()->{
            var own=NovelVignettes.personal(f.data(),owner.getUUID());h.assertTrue(NovelVignettes.coveredWait(owner,own)&&!NovelVignettes.coveredWait(peer,NovelVignettes.personal(f.data(),peer.getUUID()))&&!NovelVignettes.coveredWait(observer,NovelVignettes.personal(f.data(),observer.getUUID())),"only the actual living reader enters the personal wait");
            h.assertTrue(lid.progress()>0&&lid.progress()<WellCoverBlockEntity.CLOSE_TICKS&&f.level.getBlockState(lid.getBlockPos()).getValue(WellCoverBlock.OPEN),"the single lid moves slowly before sealing, irrespective of observers");
        });
        h.startSequence().thenIdle(190).thenExecute(()->{
            h.assertTrue(!f.level.getBlockState(lid.getBlockPos()).getValue(WellCoverBlock.OPEN)&&lid.closure(0)==1,"occupied time finally seals the native lid");
            var own=NovelVignettes.personal(f.data(),owner.getUUID());own.putInt("WellTicks",NovelVignettes.WELL_WAIT-1);NovelVignettes.save(f.data(),owner.getUUID(),own);f.at(peer,.5,-12,-22.5);
        });
        h.startSequence().thenIdle(215).thenExecute(()->{
            h.assertTrue(!NovelVignettes.coveredWait(owner,NovelVignettes.personal(f.data(),owner.getUUID()))&&NovelVignettes.coveredWait(peer,NovelVignettes.personal(f.data(),peer.getUUID()))&&!f.level.getBlockState(lid.getBlockPos()).getValue(WellCoverBlock.OPEN),"the second reader continues their own occupied wait");
            f.at(owner,.5,-2,-22.5);
        });
        h.startSequence().thenIdle(265).thenExecute(()->{
            h.assertTrue(f.level.getBlockState(lid.getBlockPos()).getValue(WellCoverBlock.OPEN)&&NovelVignettes.coveredWait(peer,NovelVignettes.personal(f.data(),peer.getUUID())),"a completed reader can leave without ending or resetting a peer's wait");
            owner.move(MoverType.SELF,new Vec3(0,4,0));
        });
        h.startSequence().thenIdle(280).thenExecute(()->{
            h.assertTrue(WitnessAccount.has(f.data(),owner.getUUID(),WitnessAccount.Story.BARN_WELL)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BARN_WELL)&&!WitnessAccount.has(f.data(),observer.getUUID(),WitnessAccount.Story.BARN_WELL),"only physical ascent resolves; another player's open lid and spectators confer no credit");
            h.assertTrue(SceneClock.time(3,900,6000)==12500&&f.level.getBlockState(f.base.offset(NovelRooms.CARVING)).is(NovelRegistry.CARVINGS.get()),"the original dusk and initials remain exact");f.done();
        });
    });}

    @GameTest(template="empty",batch="review_farm_stock",timeoutTicks=1600)
    public static void nativeFarmPensContainStockAndSharedFinitePetsAndFoodSurviveReload(GameTestHelper h){run(h,LabyrinthPlace.BARN_WELL,557000,f->{
        NovelRooms.build(f.level.getServer(),f.level,f.base,f.place);
            h.assertTrue(!Farmstead.ready(f.data(),f.origin),"the first native visit precedes any vacant saved-farm upgrade");
            var owner=f.player("farm_food_owner");var peer=f.player("farm_pet_peer");f.at(owner,4.5,0,-14.5);f.at(peer,4.5,0,-12.5);
            NovelVignettes.onArrive(owner,f.place);NovelVignettes.onArrive(peer,f.place);
            var animals=f.level.getEntitiesOfClass(Mob.class,Farmstead.area(f.base),m->m.getPersistentData().getBoolean("HouseBarnAnimal"));
            h.assertTrue(animals.size()==7,"two actual cows, two sheep and three chickens spawn once");
            for(var a:animals){a.setNoAi(true);var rail=new BlockPos(f.base.getX()+(a instanceof net.minecraft.world.entity.animal.Chicken?15:8),f.base.getY(),a.blockPosition().getZ());
                double edge=rail.getX()+f.level.getBlockState(rail).getCollisionShape(f.level,rail).bounds().minX;a.move(MoverType.SELF,new Vec3(9,0,0));
                h.assertTrue(a.getBoundingBox().maxX<=edge+.002,"native collision holds livestock inside the closed barn pens: type="+a.getType()+", body="+a.getBoundingBox()+", fence="+f.level.getBlockState(rail)+", edge="+edge);}
            f.at(owner,6.5,0,-24.5);owner.move(MoverType.SELF,new Vec3(5,0,0));h.assertTrue(owner.getX()<f.base.getX()+8.6,"a real survival body cannot pass the closed pen gate");
            var left=f.level.getBlockState(f.base.offset(-16,0,-28));h.assertTrue(left.is(Blocks.SPRUCE_FENCE)&&left.getValue(FenceBlock.NORTH)&&left.getValue(FenceBlock.SOUTH),"the previously omitted left field has continuous native rails");
            var cache=(BarrelBlockEntity)f.level.getBlockEntity(f.base.offset(Farmstead.FOOD));h.assertTrue(cache.getItem(0).is(Items.BREAD)&&cache.getItem(0).getCount()==6&&cache.getItem(2).getCount()==2,"the finite native feed barrel contains actual edible food and pet meat");
            var pets=f.level.getEntitiesOfClass(TamableAnimal.class,Farmstead.area(f.base),a->a.getPersistentData().getBoolean("HouseFarmPet"));h.assertTrue(pets.size()==2,"one native dog and one native cat are shared");
            for(int i=0;i<2;i++){var pet=pets.get(i);var reader=i==0?owner:peer;pet.setNoAi(true);pet.setNoGravity(true);reader.teleportTo(f.level,pet.getX(),pet.getY(),pet.getZ()+1,0,0);reader.connection.resetPosition();reader.hasChangedDimension();reader.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.COOKED_BEEF));
                h.assertTrue(LabyrinthEncounters.feedStray(reader,pet,InteractionHand.MAIN_HAND)&&reader.getMainHandItem().isEmpty(),"native one-meat taming consumes food and keeps the actual pet");
                h.assertTrue(io.github.knaitoe.theoldesthouse.opening.CompanionOrders.issue(pet,reader,io.github.knaitoe.theoldesthouse.opening.CompanionOrders.Order.STAY),"the real owner can issue Stay");pet.setHealth(7);
            }
            cache.clearContent();var animalIds=animals.stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet());var petIds=pets.stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet());
            var save=f.data().save(new CompoundTag(),f.level.registryAccess());f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(save,f.level.registryAccess()));
            Farmstead.stock(owner);Farmstead.stock(peer);BarnFarm.animals(owner);Farmstead.fresh(f.level,f.origin);
            h.assertTrue(cache.isEmpty()&&f.level.getEntitiesOfClass(Mob.class,Farmstead.area(f.base),m->m.getPersistentData().getBoolean("HouseBarnAnimal")).stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet()).equals(animalIds),"native reload and a peer arrival never replenish food or recreate livestock");
            h.assertTrue(pets.stream().allMatch(a->a.isAlive()&&a.getHealth()==7&&a.isOrderedToSit()&&petIds.contains(a.getUUID())),"both original owned Stay pets retain native health and identity");
            for(var pet:pets)pet.discard();Farmstead.stock(owner);Farmstead.stock(peer);
            h.assertTrue(f.level.getEntitiesOfClass(TamableAnimal.class,Farmstead.area(f.base),a->a.getPersistentData().getBoolean("HouseFarmPet")).isEmpty()&&WitnessAccount.count(f.data(),owner.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"dead or removed pets stay gone, and food/taming confer no ending credit");f.done();
    });}

    @GameTest(template="empty",batch="review_farm_migration",timeoutTicks=1600)
    public static void savedFarmWaitsForCamerasAndRealStayBodiesThenMovesOnlyOriginalStockAndInitials(GameTestHelper h){run(h,LabyrinthPlace.BARN_WELL,557500,f->{
        NovelRooms.build(f.level.getServer(),f.level,f.base,f.place);Farmstead.forget(f.level.getServer(),f.origin);
        f.put(NovelRooms.CARVING,Blocks.MOSSY_COBBLESTONE.defaultBlockState());f.put(NovelRooms.OLD_CARVING,NovelRegistry.CARVINGS.get().defaultBlockState());
        f.put(NovelRooms.WELL,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.OPEN,true).setValue(TrapDoorBlock.HALF,net.minecraft.world.level.block.state.properties.Half.TOP));
        var original=(LecternBlockEntity)f.level.getBlockEntity(f.base.offset(-4,0,-17));var book=original.getBook().copy();var ribbon=(BarrelBlockEntity)f.level.getBlockEntity(f.base.offset(NovelRooms.RIBBON));ribbon.clearContent();
        var edited=(BarrelBlockEntity)f.level.getBlockEntity(f.base.offset(Farmstead.FOOD));var gem=new ItemStack(Items.DIAMOND);gem.set(DataComponents.CUSTOM_NAME,Component.literal("Kept farm property"));edited.setItem(6,gem.copy());
        var owner=f.player("farm_upgrade_owner");var observer=f.player("farm_upgrade_camera");observer.setGameMode(GameType.SPECTATOR);f.at(observer,-10.5,1,-12.5);
        var own=NovelVignettes.personal(f.data(),owner.getUUID());own.putBoolean("Initials",true);own.putInt("WellTicks",381);NovelVignettes.save(f.data(),owner.getUUID(),own);
        var animal=EntityType.COW.create(f.level);animal.setNoAi(true);animal.setPersistenceRequired();animal.setHealth(8);animal.getPersistentData().putBoolean("HouseBarnAnimal",true);animal.moveTo(Vec3.atBottomCenterOf(f.base.offset(-4,0,-18)));f.level.addFreshEntity(animal);var id=animal.getUUID();var pos=animal.position();
        f.put(-8,0,-12,Blocks.AIR);var pet=EntityType.CAT.create(f.level);pet.setNoAi(true);pet.setNoGravity(true);pet.setTame(true,false);pet.setOwnerUUID(owner.getUUID());pet.setOrderedToSit(true);pet.setHealth(5);pet.moveTo(Vec3.atBottomCenterOf(f.base.offset(-8,0,-12)));f.level.addFreshEntity(pet);var petId=pet.getUUID();
        Farmstead.fresh(f.level,f.origin);h.assertTrue(animal.position().equals(pos)&&!Farmstead.ready(f.data(),f.origin),"an actual spectator camera prevents layout work and animal relocation");f.away(observer);Farmstead.fresh(f.level,f.origin);
        h.startSequence().thenIdle(100).thenExecute(()->{
            h.assertTrue(!Farmstead.ready(f.data(),f.origin)&&f.level.getBlockState(f.base.offset(-8,0,-12)).isAir()&&pet.isAlive()&&pet.getUUID().equals(petId)&&pet.getHealth()==5,"bounded execution waits rather than building a wall through the actual sitting cat");
            pet.moveTo(Vec3.atBottomCenterOf(f.base.offset(3,0,-12)));
        });
        h.startSequence().thenIdle(160).thenWaitUntil(()->h.assertTrue(Farmstead.ready(f.data(),f.origin),"the same layout resumes after the resident clears its footprint")).thenExecute(()->{
            h.assertTrue(animal.getUUID().equals(id)&&animal.getHealth()==8&&animal.isAlive()&&animal.getX()>f.base.getX()+5&&animal.getX()<f.base.getX()+8,"the original wounded native stock is rehomed into the pen");
            h.assertTrue(f.level.getBlockEntity(f.base.offset(-4,0,-17))==original&&ItemStack.isSameItemSameComponents(original.getBook(),book)&&f.level.getBlockEntity(f.base.offset(NovelRooms.RIBBON))==ribbon&&ribbon.isEmpty()&&ItemStack.isSameItemSameComponents(edited.getItem(6),gem),"original native books, barrels, depleted rewards and edited property survive");
            h.assertTrue(f.level.getBlockState(f.base.offset(NovelRooms.OLD_CARVING)).is(Blocks.MOSSY_COBBLESTONE)&&f.level.getBlockState(f.base.offset(NovelRooms.CARVING)).is(NovelRegistry.CARVINGS.get())&&f.level.getBlockState(f.base.offset(NovelRooms.WELL)).is(NovelRegistry.WELL_COVER.get()),"only the authored initials move above the head to the facing stone wall, and the native lid upgrades");
            h.assertTrue(pet.getUUID().equals(petId)&&pet.getHealth()==5&&pet.isOrderedToSit()&&pet.getOwnerUUID().equals(owner.getUUID())&&NovelVignettes.personal(f.data(),owner.getUUID()).getInt("WellTicks")==381&&NovelVignettes.personal(f.data(),owner.getUUID()).getBoolean("Initials"),"ownership, Stay, health and personal well history remain exact");
            f.put(-16,0,-28,Blocks.AIR);var save=f.data().save(new CompoundTag(),f.level.registryAccess());f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(save,f.level.registryAccess()));Farmstead.fresh(f.level,f.origin);
            h.assertTrue(f.level.getBlockState(f.base.offset(-16,0,-28)).isAir(),"the saved checkpoint does not reconstruct a later removed rail");f.done();
        });
    });}

    // The native GameTest clock can advance thousands of ticks while cold entity-section IO is still pending.
    // Retain the real readiness/collision checks and allow that asynchronous fixture setup to finish.
    @GameTest(template="empty",batch="review_farm_edges",timeoutTicks=12000)
    public static void anAlreadyUpgradedFarmRepairsTheBrokenYardAndVoidWithoutRestockingOrMovingResidents(GameTestHelper h){run(h,LabyrinthPlace.BARN_WELL,558500,f->{
        NovelRooms.build(f.level.getServer(),f.level,f.base,f.place);
        var state=f.data().stateEntry(Farmstead.STATE,Long.toString(f.origin.asLong()));
        state.putBoolean("Layout",true);state.remove("Edges0463");state.putBoolean("FoodIssued",true);state.putBoolean("DogIssued",true);state.putBoolean("CatIssued",true);
        f.data().setStateEntry(Farmstead.STATE,Long.toString(f.origin.asLong()),state);
        for(int x:new int[]{11,15})for(int z=-14;z<=-5;z++)f.put(x,0,z,Math.floorMod(z+14,3)==0?Blocks.OAK_FENCE:Blocks.AIR);
        for(int x=2;x<=3;x++)for(int z=-24;z<=-23;z++){f.put(x,-1,z,Blocks.AIR);f.put(x,-2,z,Blocks.AIR);}
        f.put(0,-13,-23,Blocks.AIR);f.put(12,-1,-24,Blocks.AIR);f.put(4,-1,-23,Blocks.IRON_BLOCK);
        var original=(LecternBlockEntity)f.level.getBlockEntity(f.base.offset(-4,0,-17));var book=original.getBook().copy();
        var food=(BarrelBlockEntity)f.level.getBlockEntity(f.base.offset(Farmstead.FOOD));food.clearContent();
        var ribbon=(BarrelBlockEntity)f.level.getBlockEntity(f.base.offset(NovelRooms.RIBBON));ribbon.clearContent();
        var lid=f.level.getBlockEntity(f.base.offset(NovelRooms.WELL));
        var owner=f.player("farm_edges_owner");var observer=f.player("farm_edges_camera");observer.setGameMode(GameType.SPECTATOR);f.at(observer,2.5,0,-23.5);f.away(owner);
        var own=NovelVignettes.personal(f.data(),owner.getUUID());own.putBoolean("Initials",true);own.putInt("WellTicks",381);NovelVignettes.save(f.data(),owner.getUUID(),own);
        var pet=EntityType.CAT.create(f.level);pet.setNoAi(true);pet.setNoGravity(true);pet.setTame(true,false);pet.setOwnerUUID(owner.getUUID());pet.setOrderedToSit(true);pet.setHealth(5);pet.moveTo(Vec3.atBottomCenterOf(f.base.offset(12,-1,-24)));f.level.addFreshEntity(pet);var id=pet.getUUID();
        Farmstead.fresh(f.level,f.origin);h.assertTrue(f.level.getBlockState(f.base.offset(2,-1,-23)).isAir()&&!Farmstead.edgesReady(f.data(),f.origin),"the observer's real camera prevents the separate saved-farm patch");f.away(observer);Farmstead.fresh(f.level,f.origin);
        h.startSequence().thenIdle(100).thenExecute(()->{
            h.assertTrue(!Farmstead.edgesReady(f.data(),f.origin)&&f.level.getBlockState(f.base.offset(12,-1,-24)).isAir()&&pet.isAlive()&&pet.getHealth()==5,"native floor collision cannot grow through an actual Stay cat in the hole");
            pet.moveTo(Vec3.atBottomCenterOf(f.base.offset(3,0,-12)));
        });
        h.startSequence().thenIdle(160).thenWaitUntil(()->h.assertTrue(Farmstead.edgesReady(f.data(),f.origin),"the separate repair completes after the resident moves")).thenExecute(()->{
            for(int x:new int[]{11,15})for(int z=-13;z<=-6;z++){var rail=f.level.getBlockState(f.base.offset(x,0,z));
                if(x==11&&z==-10){h.assertTrue(rail.is(Blocks.SPRUCE_FENCE_GATE)&&!rail.getValue(FenceGateBlock.OPEN),"the yard has one real closed gate");continue;}
                h.assertTrue(rail.is(Blocks.SPRUCE_FENCE)&&rail.getValue(FenceBlock.NORTH)&&rail.getValue(FenceBlock.SOUTH),"every formerly three-spaced yard post has continuous native rails");}
            for(int x=2;x<=3;x++)for(int z=-24;z<=-23;z++)h.assertTrue(f.level.getBlockState(f.base.offset(x,-1,z)).is(Blocks.GRASS_BLOCK)&&f.level.getBlockState(f.base.offset(x,-2,z)).is(Blocks.DIRT),"the apron is actual supported ground above the outside void");
            h.assertTrue(f.level.getBlockState(f.base.offset(0,-13,-23)).is(Blocks.MOSSY_COBBLESTONE)&&f.level.getBlockState(f.base.offset(0,-2,-23)).is(Blocks.LADDER)&&f.level.getBlockState(f.base.offset(4,-1,-23)).is(Blocks.IRON_BLOCK),"the shaft has a stone bottom, keeps its ladder and preserves edited ground");
            h.assertTrue(f.level.getBlockEntity(f.base.offset(NovelRooms.WELL))==lid&&original==f.level.getBlockEntity(f.base.offset(-4,0,-17))&&ItemStack.isSameItemSameComponents(book,original.getBook())&&food.isEmpty()&&ribbon.isEmpty(),"the lid, exact original and depleted finite caches are never reconstructed");
            h.assertTrue(pet.getUUID().equals(id)&&pet.getHealth()==5&&pet.isNoAi()&&pet.isNoGravity()&&pet.isOrderedToSit()&&pet.getOwnerUUID().equals(owner.getUUID())&&NovelVignettes.personal(f.data(),owner.getUUID()).getInt("WellTicks")==381,"the same native pet and reader's ongoing vigil survive the small patch");
            f.put(2,-1,-23,Blocks.AIR);f.put(15,0,-12,Blocks.AIR);var save=f.data().save(new CompoundTag(),f.level.registryAccess());f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(save,f.level.registryAccess()));Farmstead.fresh(f.level,f.origin);
            h.assertTrue(f.level.getBlockState(f.base.offset(2,-1,-23)).isAir()&&f.level.getBlockState(f.base.offset(15,0,-12)).isAir(),"the saved patch never restores later player removals");f.done();
        });
    });}

    @GameTest(template="empty",batch="review_well_pause",timeoutTicks=1600)
    public static void oneNativeLidPausesAbsentAcrossBlockEntityReloadAndCannotSealThroughACamera(GameTestHelper h){run(h,LabyrinthPlace.BARN_WELL,558000,f->{
        NovelRooms.build(f.level.getServer(),f.level,f.base,f.place);var owner=f.player("lid_pause_owner");var peer=f.player("lid_pause_peer");var camera=f.player("lid_pause_camera");camera.setGameMode(GameType.SPECTATOR);f.away(camera);f.at(owner,.5,-12,-22.5);f.at(peer,.5,-12,-22.5);
        var lid=(WellCoverBlockEntity)f.level.getBlockEntity(f.base.offset(NovelRooms.WELL));int[] progress={0};
        h.startSequence().thenIdle(35).thenExecute(()->{
            progress[0]=lid.progress();h.assertTrue(progress[0]>0&&progress[0]<50,"two actual readers do not multiply the physical lid's occupied clock");f.away(owner);f.away(peer);
            var saved=lid.saveWithFullMetadata(f.level.registryAccess());lid.loadWithComponents(saved,f.level.registryAccess());
        });
        h.startSequence().thenIdle(80).thenExecute(()->{
            h.assertTrue(lid.progress()==progress[0]&&NovelVignettes.personal(f.data(),owner.getUUID()).getInt("WellTicks")<50,"native block-entity reload and absent time advance neither lid nor personal waits");f.at(owner,.5,-12,-22.5);f.at(camera,.5,.84,-22.5);
        });
        h.startSequence().thenIdle(260).thenExecute(()->{
            h.assertTrue(lid.progress()==WellCoverBlockEntity.CLOSE_TICKS-1&&f.level.getBlockState(lid.getBlockPos()).getValue(WellCoverBlock.OPEN),"the final collision plate waits for an actual spectator camera body");f.away(camera);
        });
        h.startSequence().thenIdle(270).thenExecute(()->{h.assertTrue(lid.progress()==WellCoverBlockEntity.CLOSE_TICKS&&!f.level.getBlockState(lid.getBlockPos()).getValue(WellCoverBlock.OPEN)&&NovelVignettes.personal(f.data(),peer.getUUID()).getInt("WellTicks")<50,"vacancy finishes the one physical lid without advancing an absent peer");f.done();});
    });}

    @GameTest(template="empty",batch="review_witch",timeoutTicks=1600)
    public static void oneNativeCostumeWitchStrikesRecoilAndReloadPreserveIdentityAndPeerSafety(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,553000,f->{
        for(int x=-6;x<=7;x++)for(int z=-47;z<=-35;z++)f.put(x,-1,z,Blocks.SANDSTONE);
        var owner=f.player("review_witch_owner");var peer=f.player("review_witch_peer");f.at(owner,2,0,-40.5);f.at(peer,5,0,-40.5);owner.getFoodData().setFoodLevel(6);peer.getFoodData().setFoodLevel(6);
        f.look(owner,new BlockPos(9,1,-35));f.look(peer,new BlockPos(10,1,-35));
        // Native newly joined players keep their own spawn protection; let it expire before testing damage.
        h.startSequence().thenIdle(70).thenExecute(()->{
        var witch=LiteraryVignettes.huntBody(owner,f.place,new BlockPos(0,0,-41));h.assertTrue(witch!=null&&!witch.isInvulnerable()&&LiteraryVignettes.huntBody(peer,f.place,new BlockPos(0,0,-41))==witch,"both readers share the same native, woundable actor");var id=witch.getUUID();float peerHealth=peer.getHealth();var before=f.data().state(DrownedTown.ID);boolean[] strike={false},checked={false};
        h.onEachTick(()->{if(active==f&&witch.striking())strike[0]=true;});
        h.onEachTick(()->{if(active!=f||checked[0]||witch.tickCount<22)return;checked[0]=true;
            h.assertTrue(strike[0]&&owner.getHealth()<20&&peer.getHealth()==peerHealth,"one actual lunge has a synced attack state and hurts only its nearest vulnerable reader: actorTicks="+witch.tickCount+", strike="+strike[0]+", ownerHealth="+owner.getHealth()+", peerHealth="+peer.getHealth()+", changingDimension="+owner.isChangingDimension()+", inside="+LiteraryVignettes.inside(owner,f.place)+", "+witch.huntDiagnostic());
            var health=witch.getHealth();owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));owner.attack(witch);
            h.assertTrue(witch.getHealth()<health&&witch.hurtTime>0&&witch.huntPhase()==LakeWitchEntity.WITHDRAW&&!witch.striking(),"a real native sword wound cancels the strike and exposes recoil");
            float wounded=witch.getHealth();var tag=new CompoundTag();witch.saveWithoutId(tag);witch.load(tag);
            h.assertTrue(witch.getUUID().equals(id)&&witch.getHealth()==wounded&&f.level.getEntity(id)==witch&&!witch.isInvulnerable(),"native save/reload keeps the same wounded original");
            for(int x=4;x<=7;x++)for(int z=-44;z<=-38;z++)f.put(x,-1,z,Blocks.GRASS_BLOCK);
            f.at(owner,5.5,0,-41.5);f.at(peer,6.5,0,-42.5);float a=owner.getHealth(),b=peer.getHealth();
            h.startSequence().thenIdle(45).thenExecute(()->{h.assertTrue(owner.getHealth()==a&&peer.getHealth()==b&&f.data().state(DrownedTown.ID).equals(before)&&WitnessAccount.count(f.data(),peer.getUUID())==0,"real living grass protects both readers and the literary wound cannot alter the town's shared state or grant evidence");f.done();});
        });});
    });}

    @GameTest(template="empty",batch="review_shore",timeoutTicks=1600)
    public static void nativeCostumeBayAndSwimmingStepsRetainResidentsAndOriginalStand(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,553500,f->{
        for(int x=-28;x<=28;x++)for(int z=-58;z<=-35;z++)for(int y=-5;y<=-1;y++)f.put(x,y,z,z<=-55?(y==-1?Blocks.GRASS_BLOCK:Blocks.DIRT):z<=-49?Blocks.WATER:y==-1?Blocks.SAND:Blocks.SANDSTONE);
        var owner=f.player("review_shore_owner");var peer=f.player("review_shore_peer");
        var cat=EntityType.CAT.create(f.level);cat.setNoAi(true);cat.setNoGravity(true);cat.setTame(true,false);cat.setOwnerUUID(owner.getUUID());cat.setOrderedToSit(true);cat.moveTo(Vec3.atBottomCenterOf(f.base.offset(21,0,-40)));f.level.addFreshEntity(cat);var id=cat.getUUID();
        h.assertTrue(!SceneReview.shore(f.level,f.base)&&f.level.getBlockState(f.base.offset(21,-1,-40)).is(Blocks.SAND)&&cat.getUUID().equals(id),"the new bay cannot remove the real sitting companion's support");cat.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-40)));
        h.assertTrue(SceneReview.shore(f.level,f.base)&&f.level.getBlockState(f.base.offset(20,-4,-45)).is(Blocks.WATER),"the empty bay has genuine swimming depth rather than a thin water sheet");
        var stand=new ArmorStand(f.level,f.base.getX()+20.5,f.base.getY(),f.base.getZ()-44.5);stand.setNoGravity(true);stand.setInvulnerable(true);stand.addTag("HouseCostume");var shirt=new ItemStack(Items.LEATHER_CHESTPLATE);shirt.set(DataComponents.CUSTOM_NAME,Component.literal("Original costume"));stand.setItemSlot(EquipmentSlot.CHEST,shirt);f.level.addFreshEntity(stand);var standId=stand.getUUID();
        SceneReview.dressCostume(f.level,f.base);var water=f.base.offset(20,-1,-45);
        h.assertTrue(stand.getUUID().equals(standId)&&SceneReview.costume(stand)&&Math.abs(stand.getY()-(water.getY()+f.level.getFluidState(water).getHeight(f.level,water)))<.01&&stand.getItemBySlot(EquipmentSlot.CHEST).getHoverName().getString().equals("Original costume"),"the same native stand meets the actual water and retains its original equipment components");
        f.at(owner,.5,-.1,-53.5);owner.move(MoverType.SELF,new Vec3(0,.42,-1));owner.move(MoverType.SELF,new Vec3(0,0,-2));owner.move(MoverType.SELF,new Vec3(0,-.42,0));
        h.assertTrue(owner.getZ()<f.base.getZ()-55&&owner.getY()>=f.base.getY()-.01&&!f.level.noCollision(owner,owner.getBoundingBox().move(0,-.06,0)),"native swimming ascent and the half-height stair lip reach supported living grass");
        h.assertTrue(cat.isAlive()&&cat.getUUID().equals(id)&&cat.isOrderedToSit()&&cat.getOwnerUUID().equals(owner.getUUID())&&WitnessAccount.count(f.data(),peer.getUUID())==0,"native companions and personal progress stay intact");f.done();
    });}

    @GameTest(template="empty",batch="review_archive",timeoutTicks=1600)
    public static void courtyardFurnitureAndNewDraftsKeepActualOriginalsPersonalAndFinite(GameTestHelper h){run(h,LabyrinthPlace.ZAMPANO_COURTYARD,554000,f->{
        for(int x=-12;x<=12;x++)for(int z=-37;z<=-6;z++)f.put(x,-1,z,Blocks.MOSSY_STONE_BRICKS);
        for(int x:new int[]{-8,8}){f.put(new BlockPos(x,0,-10),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));f.put(new BlockPos(x,0,-12),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH));f.put(new BlockPos(x,1,-12),SceneDetailBlock.state(SceneDetailBlock.Kind.VASE,Direction.SOUTH));}
        for(var rel:SceneReview.DRAFTS){f.put(rel.below(),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH));f.put(rel,SceneDetailBlock.state(SceneDetailBlock.Kind.INK_PAPERS,Direction.SOUTH));}
        var originalAt=new BlockPos(5,0,-34);f.put(originalAt,Blocks.CHEST.defaultBlockState());var cache=(ChestBlockEntity)f.level.getBlockEntity(f.base.offset(originalAt));var original=NovelTexts.archive();cache.setItem(4,original.copy());
        SceneReview.fresh(f.level,f.origin,f.place);
        for(int x:new int[]{-8,8})h.assertTrue(f.level.getBlockState(f.base.offset(x,0,-10)).getValue(HouseholdFurnitureBlock.FACING)==Direction.NORTH&&f.level.getBlockState(f.base.offset(x,0,-14)).getValue(HouseholdFurnitureBlock.FACING)==Direction.SOUTH&&f.level.getBlockState(f.base.offset(x,0,-12)).getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.CHESS_TABLE&&f.level.getBlockState(f.base.offset(x,1,-12)).getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.CHESS,"actual chess tables each have two opposing native chairs");
        var owner=f.player("review_archive_owner");var peer=f.player("review_archive_peer");f.click(owner,SceneReview.DRAFTS.get(0));var menu=(NovelVignettes.NovelBookMenu)owner.containerMenu;var text=menu.book().copy();
        h.assertTrue(!menu.clickMenuButton(peer,3)&&menu.clickMenuButton(owner,3)&&!menu.clickMenuButton(owner,3),"the native draft menu yields its original once to its actual reader");owner.closeContainer();
        var saved=f.data().save(new CompoundTag(),f.level.registryAccess());f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(saved,f.level.registryAccess()));f.click(owner,SceneReview.DRAFTS.get(0));
        h.assertTrue(ItemStack.isSameItemSameComponents(((NovelVignettes.NovelBookMenu)owner.containerMenu).book(),text)&&!((NovelVignettes.NovelBookMenu)owner.containerMenu).clickMenuButton(owner,3),"native reload and rereading retain the same finite words/components");
        h.assertTrue(f.level.getBlockEntity(f.base.offset(originalAt))==cache&&cache.getItem(0).isEmpty()&&ItemStack.isSameItemSameComponents(cache.getItem(4),original)&&WitnessAccount.count(f.data(),owner.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"new scenery and drafts never restock the old container or grant a story resolution");f.done();
    });}

    @GameTest(template="empty",batch="review_fan_supply",timeoutTicks=1600)
    public static void compactFanRoomOffersFiniteBoardsAndItsOriginalReadingThroughACustomSurface(GameTestHelper h){run(h,LabyrinthPlace.ELK_FAN,554500,f->{
        LiteraryRooms.build(f.level,f.base,f.place);SceneReview.apply(f.level,f.base,f.place);SceneHuntReview.apply(f.level,f.origin,f.place);
        var owner=f.player("review_fan_builder");var peer=f.player("review_fan_peer");LiteraryVignettes.onArrive(owner,f.place);
        f.click(owner,SceneHuntReview.TOOLS);f.click(owner,SceneHuntReview.TOOLS);
        h.assertTrue(owner.getInventory().countItem(Items.OAK_PLANKS)==8,"the real tool tray gives eight original boards once");
        owner.getInventory().clearContent();f.click(owner,SceneHuntReview.TOOLS);
        h.assertTrue(owner.getInventory().countItem(Items.OAK_PLANKS)==0,"depleting the original boards cannot refill the tray");
        h.assertTrue(f.level.getBlockState(f.base.offset(-9,2,-19)).is(LiteraryRegistry.SIDING.get())&&f.level.getBlockState(f.base.offset(LiteraryRooms.FAN)).is(LiteraryRegistry.PROP.get()),"the new compact wall preserves the actual fan interaction address");
        f.source(owner);h.assertTrue(f.own(owner).getBoolean("Read_Source")&&!f.own(peer).getBoolean("Read_Source")&&WitnessAccount.count(f.data(),owner.getUUID())==0,"the themed source still opens the personal original and arrival supplies confer no resolution");f.done();
    });}

    @GameTest(template="empty",batch="review_cabin_surface",timeoutTicks=1600)
    public static void raisedCabinSurfaceKeepsTheOriginalAndWaitsForALivingStayPet(GameTestHelper h){run(h,LabyrinthPlace.END_WORLD_CABIN,557640,f->{
        var low=new BlockPos(-8,0,-31);var high=low.above();f.put(low.below(),Blocks.OAK_PLANKS.defaultBlockState());f.put(low,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true));
        // The 0.4.55 pass runs first, at the raised address, and lays its notes on top of the old lectern.
        f.put(high,VignetteDetailBlock.state(VignetteDetailBlock.Kind.DIARY_STACK,Direction.SOUTH));
        var original=LiteraryTexts.source(f.place);original.set(DataComponents.CUSTOM_NAME,Component.literal("The exact saved note"));((LecternBlockEntity)f.level.getBlockEntity(f.base.offset(low))).setBook(original.copy());
        var owner=f.player("cabin_surface_reader");var cat=EntityType.CAT.create(f.level);h.assertTrue(cat!=null,"the repair has a real living resident");cat.setTame(true,true);cat.setOwnerUUID(owner.getUUID());cat.setOrderedToSit(true);cat.setNoGravity(true);cat.setHealth(5);
        cat.moveTo(f.base.getX()-7.8,f.base.getY()+.2,f.base.getZ()-30.8);f.level.addFreshEntity(cat);var id=cat.getUUID();
        h.assertTrue(!PlaytestSceneReview.apply(f.level,f.origin,f.place)&&f.level.getBlockState(f.base.offset(low)).is(Blocks.LECTERN),"nightstand collision cannot be inserted through the existing Stay cat");
        cat.moveTo(f.base.getX()-3.5,f.base.getY(),f.base.getZ()-30.5);
        h.assertTrue(PlaytestSceneReview.apply(f.level,f.origin,f.place)&&f.level.getBlockState(f.base.offset(low)).getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE&&f.level.getBlockState(f.base.offset(high)).getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.DIARY_STACK,"the original surface rises onto its nightstand only after the living body leaves");
        h.assertTrue(f.level.getEntitiesOfClass(ItemEntity.class,new AABB(f.base.offset(low)).inflate(3),e->e.getItem().is(Items.WRITTEN_BOOK)).isEmpty(),"the booked lectern is emptied in place, so its original is archived without a dropped duplicate");
        h.assertTrue(ItemStack.isSameItemSameComponents(original,SceneHuntReview.sourceBook(f.level,f.base,f.place)),"raising the surface retains every component of the archived physical original");
        f.click(owner,high);var pages=(LiteraryVignettes.Pages)owner.containerMenu;h.assertTrue(Objects.equals(original.get(DataComponents.WRITTEN_BOOK_CONTENT),pages.original().get(DataComponents.WRITTEN_BOOK_CONTENT)),"the new personal reading menu contains the exact saved words, title and author");
        h.assertTrue(pages.clickMenuButton(owner,3)&&!pages.clickMenuButton(owner,3)&&WitnessAccount.count(f.data(),owner.getUUID())==0,"the raised paper yields one personal original without a resolution");owner.closeContainer();
        f.put(high,Blocks.AIR.defaultBlockState());h.assertTrue(PlaytestSceneReview.apply(f.level,f.origin,f.place)&&f.level.getBlockState(f.base.offset(high)).isAir(),"a later removed paper stays removed");
        h.assertTrue(cat.isAlive()&&cat.getUUID().equals(id)&&cat.isOrderedToSit()&&cat.getHealth()==5,"the original pet keeps its identity, health and Stay order");f.done();
    });}

    @GameTest(template="empty",batch="review_hunter_cover",timeoutTicks=1600)
    public static void bothNativeBodiesFitBelowRealCoverAndSoundSearchExpiresBehindOcclusion(GameTestHelper h){run(h,LabyrinthPlace.CAMP_BLOOD,555000,f->{
        for(int x=-24;x<=12;x++)for(int z=-53;z<=-15;z++)f.put(x,-1,z,Blocks.PODZOL);
        for(int z=-132;z<=-7;z++)for(int y=0;y<=3;y++)f.put(-10,y,z,Blocks.STONE);
        for(int x=-1;x<=1;x++)for(int z=-42;z<=-40;z++)f.put(new BlockPos(x,1,z),HouseBlocks.FOREST_COVER.get().defaultBlockState());
        var owner=f.player("review_hidden_hunter_reader");var peer=f.player("review_hidden_hunter_peer");f.at(owner,.5,0,-40.5);f.at(peer,10.5,0,-39.5);owner.setShiftKeyDown(true);owner.setForcedPose(Pose.CROUCHING);owner.setPose(Pose.CROUCHING);owner.refreshDimensions();
        LiteraryVignettes.onArrive(owner,f.place);LiteraryVignettes.onArrive(peer,f.place);
        var body=LiteraryVignettes.actor(owner,f.place,"Killer",LiteraryActor.KILLER,CarcassHunt.START,false);body.setNoGravity(true);
        h.startSequence().thenIdle(15).thenExecute(()->{
            var world=LiteraryVignettes.shared(f.data(),f.place);var hunter=(LiteraryActor)f.level.getEntity(world.getUUID("Killer"));
            h.assertTrue(hunter!=null&&!CarcassHunt.sees(hunter,owner)&&!CarcassHunt.sees(hunter,peer),"the native first hunter starts behind real occlusion");
            h.assertTrue(f.level.noCollision(owner,owner.getDimensions(Pose.CROUCHING).makeBoundingBox(owner.position()))&&!f.level.noCollision(owner,owner.getDimensions(Pose.STANDING).makeBoundingBox(owner.position())),"crouching really fits under the leafy half block while the standing body does not");
            h.assertTrue(f.level.noCollision(hunter,hunter.getDefaultDimensions(Pose.CROUCHING).makeBoundingBox(owner.position())),"the hunter's actual crouched collision body fits the same shelter");
            var id=hunter.getUUID();CarcassHunt.noise(owner,owner.blockPosition());h.assertTrue(CarcassHunt.tracks(hunter,owner.getUUID())&&CarcassHunt.searchPoint(hunter).equals(owner.blockPosition()),"noise behind the wall stores an actual sound position, without sight");
            var tag=new CompoundTag();hunter.saveWithoutId(tag);hunter.load(tag);h.assertTrue(hunter.getUUID().equals(id)&&CarcassHunt.tracks(hunter,owner.getUUID()),"native save/reload retains the original hunter and sound memory");
            h.startSequence().thenIdle(240).thenExecute(()->{h.assertTrue(!CarcassHunt.tracks(hunter,owner.getUUID())&&f.own(owner).getBoolean("Pursued")&&!f.own(peer).getBoolean("Pursued")&&WitnessAccount.count(f.data(),peer.getUUID())==0,"a stationary hidden reader loses the search after the real memory interval; a peer gains no pursuit or evidence");f.done();});
        });
    });}

    @GameTest(template="empty",batch="review_witch_interval",timeoutTicks=1600)
    public static void lookingAtTheNativeApproachMakesHerWithdrawAndThreeHeartsMakesHerRush(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,555500,f->{
        for(int x=-18;x<=18;x++)for(int z=-63;z<=-12;z++)f.put(x,-1,z,Blocks.SANDSTONE);
        var owner=f.player("review_witch_glance_reader");f.at(owner,.5,0,-38.5);owner.getFoodData().setFoodLevel(6);f.look(owner,new BlockPos(0,1,-41));
        var witch=LiteraryVignettes.huntBody(owner,f.place,new BlockPos(0,0,-41));var id=witch.getUUID();var start=witch.position();
        h.startSequence().thenIdle(1).thenExecute(()->{
            h.assertTrue(owner.getHealth()>=16&&witch.attackCooldown()>=20&&witch.attackCooldown()<=60&&witch.huntPhase()==LakeWitchEntity.WITHDRAW,"the actual player's gaze interrupts the approach; the shortened hiding interval waits for physical cover and permits a passing swipe");
            h.assertTrue(witch.position().distanceToSqr(start)>.25,"withdrawal is real native movement away from the approach");
            var tag=new CompoundTag();witch.saveWithoutId(tag);int delay=witch.attackCooldown();witch.load(tag);h.assertTrue(witch.getUUID().equals(id)&&witch.attackCooldown()==delay,"reload preserves the actual attack interval");
            owner.setHealth(6);f.at(owner,witch.getX()-f.base.getX()+1.5,0,witch.getZ()-f.base.getZ());owner.lookAt(EntityAnchorArgument.Anchor.EYES,witch.getEyePosition());
            boolean[] done={false};h.onEachTick(()->{if(active==f&&!done[0]&&owner.getHealth()<6){done[0]=true;h.assertTrue(witch.getUUID().equals(id)&&witch.attackCooldown()<=20,"the same actor physically strikes despite the player's gaze at three hearts");f.done();}});
        });
    });}

    @GameTest(template="empty",batch="review_source_custody",timeoutTicks=1600)
    public static void replacingARepeatedLecternRetainsExactOriginalWordsComponentsAndPrivateReads(GameTestHelper h){run(h,LabyrinthPlace.DEVILS_ROCK,556000,f->{
        for(int x=-16;x<=16;x++)for(int z=-42;z<=-2;z++)f.put(x,-1,z,Blocks.OAK_PLANKS);
        var rel=LiteraryRooms.source(f.place);f.put(rel.below(),HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH));f.put(rel,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true));
        var original=LiteraryTexts.source(f.place);original.set(DataComponents.CUSTOM_NAME,Component.literal("The actual found original"));((LecternBlockEntity)f.level.getBlockEntity(f.base.offset(rel))).setBook(original.copy());
        h.assertTrue(SceneHuntReview.apply(f.level,f.origin,f.place)&&!f.level.getBlockState(f.base.offset(rel)).is(Blocks.LECTERN)&&ItemStack.isSameItemSameComponents(original,SceneHuntReview.sourceBook(f.level,f.base,f.place)),"the actual native source survives replacement by the scene's unique reading object");
        var owner=f.player("review_source_owner");var peer=f.player("review_source_peer");f.source(owner);
        var save=f.data().save(new CompoundTag(),f.level.registryAccess());f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(save,f.level.registryAccess()));
        h.assertTrue(ItemStack.isSameItemSameComponents(original,SceneHuntReview.sourceBook(f.level,f.base,f.place))&&f.own(owner).getBoolean("Read_Source")&&!f.own(peer).getBoolean("Read_Source")&&WitnessAccount.count(f.data(),owner.getUUID())==0,"native reload preserves exact source components and each reader's own progress");f.done();
    });}

    @GameTest(template="empty",batch="review_live_collision_guard",timeoutTicks=1600)
    public static void aResidentEnteringAfterRecordingDefersTheActualCollisionWrite(GameTestHelper h){run(h,LabyrinthPlace.ELK_FAN,556500,f->{
        f.put(0,-1,-12,Blocks.STONE);f.put(3,-1,-12,Blocks.STONE);f.put(0,0,-12,Blocks.AIR);var at=f.base.offset(0,0,-12);
        var plan=BuildBlocks.record(f.level,()->BuildBlocks.guardedSet(f.level,at,Blocks.STONE.defaultBlockState(),F,()->f.level.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.02),e->e.isAlive()).isEmpty()));
        var cat=EntityType.CAT.create(f.level);cat.setNoAi(true);cat.setNoGravity(true);cat.setPersistenceRequired();cat.setOrderedToSit(true);cat.moveTo(Vec3.atBottomCenterOf(at));f.level.addFreshEntity(cat);var id=cat.getUUID();
        h.startSequence().thenIdle(2).thenExecute(()->{
        int residents=f.level.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.02),e->e.isAlive()).size();boolean finished=plan.tick();
        h.assertTrue(!finished&&f.level.getBlockState(at).isAir()&&cat.isAlive()&&cat.getUUID().equals(id),"a living native resident arriving between recording and execution postpones new collision: residents="+residents+", finished="+finished+", block="+f.level.getBlockState(at)+", catAlive="+cat.isAlive());
        cat.moveTo(Vec3.atBottomCenterOf(at.east(3)));h.assertTrue(plan.tick()&&f.level.getBlockState(at).is(Blocks.STONE)&&cat.getUUID().equals(id)&&cat.isOrderedToSit(),"the same guarded plan resumes after vacancy without replacing the resident");f.done();});
    });}

}
