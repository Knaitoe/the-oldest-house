package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(TheOldestHouse.MOD_ID) @PrefixGameTestTemplate(false)
public final class NovelTests {
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l;final BlockPos origin,b;final LabyrinthPlace place;
        final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;final long oldDayTime;
        final List<ServerPlayer> players=new ArrayList<>();final List<Entity> extra=new ArrayList<>();final NativeTestChunks chunks=new NativeTestChunks();
        Fixture(GameTestHelper h,int coord,LabyrinthPlace place){this.h=h;this.place=place;var s=h.getLevel().getServer();l=HouseTestLevel.get(s,NovelRooms.dimension(place));origin=new BlockPos(coord,80,coord);
            oldHouse=HouseSavedData.get(s);oldData=LabyrinthData.get(s);oldMother=MotherCollection.get(s);oldDayTime=s.overworld().getDayTime();var house=new HouseSavedData();house.markSpawned(origin);
            s.overworld().getDataStorage().set("the_oldest_house",house);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());s.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
            b=LabyrinthPlaces.base(origin,place);NovelRooms.build(s,l,b,place);data().setBuilt(LabyrinthBuilder.VERSION,origin);LabyrinthBuilder.registerDoors(data(),place,b);IndianLakeRooms.keepLoaded(l,b,place);}
        LabyrinthData data(){return LabyrinthData.get(l.getServer());}CompoundTag own(ServerPlayer p){return NovelVignettes.personal(data(),p.getUUID());}
        ServerPlayer player(){var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3.5,180,0);p.hasChangedDimension();players.add(p);NovelVignettes.onArrive(p,place);return p;}
        void at(ServerPlayer p,double x,double y,double z){p.moveTo(b.getX()+x,b.getY()+y,b.getZ()+z);p.setDeltaMovement(Vec3.ZERO);}
        void click(ServerPlayer p,BlockPos local){var at=b.offset(local);var e=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));NeoForge.EVENT_BUS.post(e);h.assertTrue(e.isCanceled(),"the registered prop interaction handles the real block");}
        void reload(){var loaded=LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);}
        @Override public void close(){NovelVignettes.clearAll();for(var p:players)l.getServer().getPlayerList().remove(p);
            extra.forEach(Entity::discard);
            List<Entity> cleanup=new ArrayList<>();for(Entity e:l.getAllEntities())if(e!=null&&e.blockPosition().distSqr(b)<240*240&&(e instanceof Cat||e instanceof NovelVulture||e instanceof NovelActor||e.getPersistentData().getBoolean("HouseBarnAnimal")||e.getPersistentData().getBoolean("HouseFarmPet")||e instanceof net.minecraft.world.entity.decoration.ItemFrame||e instanceof net.minecraft.world.entity.item.ItemEntity))cleanup.add(e);cleanup.forEach(Entity::discard);if(place==LabyrinthPlace.BARN_WELL)Farmstead.forget(l.getServer(),origin);
            var bounds=IndianLakeRooms.bounds(b,place);for(int x=((int)bounds.minX-1)>>4;x<=((int)bounds.maxX+1)>>4;x++)for(int z=((int)bounds.minZ-1)>>4;z<=((int)bounds.maxZ+1)>>4;z++)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(x,z),3,b);
            chunks.close();var s=l.getServer();s.overworld().getDataStorage().set("the_oldest_house",oldHouse);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);s.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);s.overworld().setDayTime(oldDayTime);LabyrinthBuilder.clearAll();}
    }
    private static Fixture courtyard,whale,well,plain,ward,karen,upgrade,signRepair;
    private static Fixture hospitalStyle,hospitalSave,hospitalWriting,hospitalInterrupted;
    @AfterBatch(batch="novel_hospital_style0470") public static void hospitalStyleDone(ServerLevel l){if(hospitalStyle!=null){hospitalStyle.close();hospitalStyle=null;}}
    @AfterBatch(batch="novel_hospital_save0470") public static void hospitalSaveDone(ServerLevel l){if(hospitalSave!=null){hospitalSave.close();hospitalSave=null;}}
    @AfterBatch(batch="novel_hospital_writing0470") public static void hospitalWritingDone(ServerLevel l){if(hospitalWriting!=null){hospitalWriting.close();hospitalWriting=null;}}
    @AfterBatch(batch="novel_hospital_interrupted0470") public static void hospitalInterruptedDone(ServerLevel l){if(hospitalInterrupted!=null){hospitalInterrupted.close();hospitalInterrupted=null;}}
    @GameTest(template="empty",batch="novel_hospital_style0470",timeoutTicks=240)
    public static void instituteHasNativeWardFurnitureAndReadableBedsideNotebooks(GameTestHelper h){
        hospitalStyle=new Fixture(h,35200,LabyrinthPlace.WHALE);var f=hospitalStyle;var p=f.player();
        h.runAfterDelay(8,()->{
            var paint=new HashSet<InstituteFixtureBlock.Kind>();
            for(int x=-13;x<=13;x++)for(int y=1;y<=4;y++){var state=f.l.getBlockState(f.b.offset(x,y,-9));if(state.is(NovelRegistry.WARD_FIXTURE.get()))paint.add(state.getValue(InstituteFixtureBlock.KIND));}
            h.assertTrue(paint.contains(InstituteFixtureBlock.Kind.PAINT)&&paint.contains(InstituteFixtureBlock.Kind.PEEL),"the ward walls contain different intact and peeling native finishes");
            h.assertTrue(f.l.getBlockState(f.b.offset(-9,0,-18)).is(NovelRegistry.WARD_CABINET.get())&&f.l.getBlockState(f.b.offset(0,0,-30)).getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.CHAIR,"storage and dayroom seating are real hospital fittings");
            h.assertTrue(f.l.getBlockState(f.b.offset(10,0,-22)).getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.BED_FOOT&&f.l.getBlockState(f.b.offset(11,0,-22)).getValue(InstituteFixtureBlock.KIND)==InstituteFixtureBlock.Kind.BED_HEAD,"the white ward bed is two physical blocks with an enamel frame");
            h.assertTrue(f.l.getBlockState(f.b.offset(WhaleInstitute.CALENDAR)).isAir()&&f.l.getBlockState(f.b.offset(WhaleInstitute.CALENDAR_BOOK)).is(NovelRegistry.WARD_NOTEBOOK.get()),"the old wooden calendar sign becomes a notebook on a supported side table");
            var saved=(InstituteNotebookBlockEntity)f.l.getBlockEntity(f.b.offset(WhaleInstitute.LECTERN));h.assertTrue(ItemStack.isSameItemSameComponents(saved.book(),NovelTexts.whaleOpening()),"the reading table holds the exact original opening letter");
            f.at(p,9.5,0,-22.5);f.click(p,WhaleInstitute.CALENDAR_BOOK);h.assertTrue(p.containerMenu instanceof NovelVignettes.NovelBookMenu,"the notebook opens through the real private native reading menu");p.closeContainer();
            for(int i=0;i<=5;i++)for(int z:new int[]{-30,-31})h.assertTrue(f.l.noCollision(null,new AABB(f.b.getX()-6-i+.2,f.b.getY()+i+1.01,f.b.getZ()+z+.2,f.b.getX()-6-i+.8,f.b.getY()+i+2.81,f.b.getZ()+z+.8)),"every enamel stair retains its original standing headroom");
            h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WHALE),"the calendar and new scenery confer no resolution");h.succeed();
        });
    }
    @GameTest(template="empty",batch="novel_hospital_save0470",timeoutTicks=2400)
    public static void hospitalRefitWaitsForNativeResidentsAndKeepsExactOriginalsWithoutRestocking(GameTestHelper h){
        hospitalSave=new Fixture(h,35600,LabyrinthPlace.WHALE);var f=hospitalSave;var p=f.player();f.chunks.hold(f.l,new AABB(Vec3.atLowerCornerOf(f.b.offset(-16,-1,-35)),Vec3.atLowerCornerOf(f.b.offset(17,13,3))));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"all ward entity sections are native and ready")).thenExecute(()->{
            WhaleHospital.forget(f.l.getServer(),f.origin);var at=f.b.offset(-9,0,-18);f.l.setBlock(at,Blocks.BARREL.defaultBlockState(),2);
            var original=letter(p,"Keep this sentence exactly.");original.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My actual stored letter"));var old=(BarrelBlockEntity)f.l.getBlockEntity(at);old.setItem(26,original.copy());var named=old.saveWithFullMetadata(f.l.registryAccess());named.putString("CustomName",net.minecraft.network.chat.Component.Serializer.toJson(net.minecraft.network.chat.Component.literal("Custom ward drawer"),f.l.registryAccess()));old.loadWithComponents(named,f.l.registryAccess());
            var source=letter(p,"This is the actual lectern original, not the default text.");var sourceAt=f.b.offset(WhaleInstitute.LECTERN);f.l.setBlock(sourceAt,Blocks.LECTERN.defaultBlockState(),2);((LecternBlockEntity)f.l.getBlockEntity(sourceAt)).setBook(source);
            var own=f.own(p);var sent=new CompoundTag();sent.putInt("Sent",2);sent.putString("Exact","An earlier private exchange");own.put(WhaleInstitute.KEY,sent);NovelVignettes.save(f.data(),p.getUUID(),own);
            h.assertTrue(!WhaleHospital.apply(f.l,f.b),"a camera in the institute holds the whole in-place repair");p.moveTo(100,80,100);
            var cat=EntityType.CAT.create(f.l);cat.setNoAi(true);cat.moveTo(f.b.getX()+4.5,f.b.getY(),f.b.getZ()-22.5);cat.setOwnerUUID(p.getUUID());cat.setHealth(4);f.l.addFreshEntity(cat);f.extra.add(cat);var id=cat.getUUID();
            h.assertTrue(!WhaleHospital.apply(f.l,f.b),"a living resident holds the refit even after the camera leaves");cat.moveTo(100,80,110);
            h.assertTrue(WhaleHospital.apply(f.l,f.b),"the unseen, vacant, loaded ward is repaired in place");var cabinet=(InstituteCabinetBlockEntity)f.l.getBlockEntity(at);
            h.assertTrue(cabinet.getContainerSize()==27&&ItemStack.isSameItemSameComponents(cabinet.getItem(26),original)&&cabinet.getName().getString().equals("Custom ward drawer"),"the same slot, exact item components and native custom name survive the barrel conversion");
            h.assertTrue(ItemStack.isSameItemSameComponents(((InstituteNotebookBlockEntity)f.l.getBlockEntity(f.b.offset(WhaleInstitute.LECTERN))).book(),source)&&f.own(p).getCompound(WhaleInstitute.KEY).equals(sent),"actual source words and the writer's private exchange remain exact");
            h.assertTrue(cat.getUUID().equals(id)&&cat.getOwnerUUID().equals(p.getUUID())&&cat.getHealth()==4,"the resident keeps identity, ownership and health");
            cabinet.removeItemNoUpdate(26);cabinet.setChanged();f.l.setBlock(f.b.offset(0,0,-30),Blocks.AIR.defaultBlockState(),2);f.reload();WhaleHospital.apply(f.l,f.b);
            h.assertTrue(cabinet.isEmpty()&&f.l.getBlockState(f.b.offset(0,0,-30)).isAir(),"reload never restocks a drawer or replaces a later removed chair");h.succeed();
        });
    }
    @GameTest(template="empty",batch="novel_hospital_interrupted0470",timeoutTicks=2400)
    public static void interruptedHospitalCustodyFinishesWithoutReplacingTheSavedOriginalWithAnEmptyBarrel(GameTestHelper h){
        hospitalInterrupted=new Fixture(h,36000,LabyrinthPlace.WHALE);var f=hospitalInterrupted;var p=f.player();f.chunks.hold(f.l,new AABB(Vec3.atLowerCornerOf(f.b.offset(-16,-1,-35)),Vec3.atLowerCornerOf(f.b.offset(17,13,3))));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the interrupted ward's native sections are ready")).thenExecute(()->{
            WhaleHospital.forget(f.l.getServer(),f.origin);var at=f.b.offset(-9,0,-18);f.l.setBlock(at,Blocks.BARREL.defaultBlockState(),2);var old=(BarrelBlockEntity)f.l.getBlockEntity(at);var original=letter(p,"Already in saved custody.");old.setItem(11,original.copy());
            var containers=new CompoundTag();containers.put(Long.toString(at.asLong()),old.saveWithFullMetadata(f.l.registryAccess()));var pending=new CompoundTag();pending.put("Containers",containers);var checkpoint=f.data().state(WhaleHospital.STATE);checkpoint.put(Long.toString(f.b.asLong()),pending);f.data().setState(WhaleHospital.STATE,checkpoint);
            old.clearContent();f.reload();p.moveTo(100,80,100);h.assertTrue(WhaleHospital.apply(f.l,f.b),"an interrupted clear resumes from saved native custody");
            h.assertTrue(ItemStack.isSameItemSameComponents(((InstituteCabinetBlockEntity)f.l.getBlockEntity(at)).getItem(11),original),"the emptied old barrel does not overwrite the saved exact original");h.succeed();
        });
    }
    @GameTest(template="empty",batch="novel_hospital_writing0470",timeoutTicks=240)
    public static void writingPromptsAndTheOtherHandPreserveWordsAndKeepFinitePrivatePaper(GameTestHelper h){
        hospitalWriting=new Fixture(h,36400,LabyrinthPlace.WHALE);var f=hospitalWriting;var p=f.player();var peer=f.player();
        h.runAfterDelay(8,()->{
            f.at(p,7.5,0,-22.5);f.click(p,WhaleInstitute.DESK);h.assertTrue(p.containerMenu instanceof LiteraryChoiceMenu&&p.getMainHandItem().is(Items.WRITABLE_BOOK),"the actual desk gives finite paper and opens the optional writing choices");
            p.containerMenu.clicked(2,0,net.minecraft.world.inventory.ClickType.PICKUP,p);var draft=p.getMainHandItem();h.assertTrue(draft.get(DataComponents.WRITABLE_BOOK_CONTENT).pages().getFirst().raw().contains("room I keep returning"),"a native menu selection gives an editable prose beginning");
            String exact="These are the words I wrote myself.";var pages=new ArrayList<net.minecraft.server.network.Filterable<String>>();pages.add(net.minecraft.server.network.Filterable.passThrough(exact));draft.set(DataComponents.WRITABLE_BOOK_CONTENT,new net.minecraft.world.item.component.WritableBookContent(pages));draft.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My unsent draft"));
            f.click(p,WhaleInstitute.DESK);p.containerMenu.clicked(4,0,net.minecraft.world.inventory.ClickType.PICKUP,p);h.assertTrue(draft.get(DataComponents.WRITABLE_BOOK_CONTENT).pages().getFirst().raw().equals(exact),"a prompt never replaces the player's existing prose");
            for(int i=0;i<3;i++){f.click(p,WhaleInstitute.DESK);p.containerMenu.clicked(8,0,net.minecraft.world.inventory.ClickType.PICKUP,p);}
            var writing=draft.get(DataComponents.WRITABLE_BOOK_CONTENT);h.assertTrue(writing.pages().size()==4&&writing.pages().getFirst().raw().equals(exact)&&draft.getHoverName().getString().equals("My unsent draft"),"the other hand appends three pages while all original words and components remain");
            f.click(p,WhaleInstitute.DESK);p.containerMenu.clicked(8,0,net.minecraft.world.inventory.ClickType.PICKUP,p);h.assertTrue(draft.get(DataComponents.WRITABLE_BOOK_CONTENT).equals(writing)&&f.own(peer).getCompound(WhaleInstitute.KEY).getInt("OtherHand0470")==0,"continuations are bounded and private to this writer");
            var desk=f.b.offset(WhaleInstitute.DESK);p.moveTo(100,80,100);h.assertTrue(!WhaleWriting.choose(p,desk,2),"a stale remote menu cannot edit a held book");
            f.at(p,7.5,0,-22.5);p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);h.assertTrue(!WhaleWriting.choose(p,desk,8),"observers cannot drive the writing interaction");p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WHALE)&&f.own(p).getCompound(WhaleInstitute.KEY).getInt("Sent")==0,"optional prose and drafts neither post a letter nor confer ending credit");
            for(int i=0;i<4;i++){p.getInventory().clearContent();f.click(p,WhaleInstitute.DESK);p.closeContainer();}
            h.assertTrue(f.own(p).getCompound(WhaleInstitute.KEY).getInt("Blanks")==4&&p.getInventory().countItem(Items.WRITABLE_BOOK)==0,"the physical drawer supplies only its original four sheets");h.succeed();
        });
    }
    @AfterBatch(batch="novel_sign_repair0468") public static void c8(ServerLevel l){if(signRepair!=null){signRepair.close();signRepair=null;}}
    @GameTest(template="empty",batch="novel_sign_repair0468",timeoutTicks=2400)
    public static void instituteRepairsWaitForCamerasAndKeepTheExactPrivateCorrespondence(GameTestHelper h){
        signRepair=new Fixture(h,34800,LabyrinthPlace.WHALE);var f=signRepair;var p=f.player();f.chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.WHALE));
        h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"the institute's native entity sections are ready")).thenExecute(()->{
            f.l.setBlock(f.b.offset(1,2,-23),Blocks.AIR.defaultBlockState(),2);NovelRooms.sign(f.l,f.b.offset(1,2,-22),Direction.WEST,new String[]{"","·      ·","",""});
            f.l.setBlock(f.b.offset(-8,5,-30),Blocks.SMOOTH_STONE.defaultBlockState(),2);f.l.setBlock(f.b.offset(1,10,-28),Blocks.AIR.defaultBlockState(),2);
            var own=f.own(p);var saved=new CompoundTag();saved.putInt("Sent",2);saved.putString("Exact","The original page in this writer's own custody");own.put(WhaleInstitute.KEY,saved);NovelVignettes.save(f.data(),p.getUUID(),own);
            h.assertTrue(!PlaytestFixes.apply(f.l,f.b,LabyrinthPlace.WHALE),"an occupied institute is not changed under its reader's camera");p.moveTo(100,80,100);h.assertTrue(PlaytestFixes.apply(f.l,f.b,LabyrinthPlace.WHALE),"the unseen loaded institute gets its bounded repairs");
            h.assertTrue(f.l.getBlockState(f.b.offset(1,2,-22)).isAir()&&f.l.getBlockState(f.b.offset(1,2,-23)).getValue(InstituteSignBlock.KIND)==InstituteSignBlock.Kind.MISSING,"the same missing plate moves exactly above her real doorway");
            h.assertTrue(f.l.getBlockState(f.b.offset(-8,5,-30)).isAir()&&!f.l.getBlockState(f.b.offset(1,10,-28)).isAir()&&f.own(p).getCompound(WhaleInstitute.KEY).equals(saved),"headroom and the roof change while the exact private correspondence stays untouched");
            f.reload();f.l.setBlock(f.b.offset(1,2,-23),Blocks.AIR.defaultBlockState(),2);PlaytestFixes.apply(f.l,f.b,LabyrinthPlace.WHALE);h.assertTrue(f.l.getBlockState(f.b.offset(1,2,-23)).isAir(),"the reloaded checkpoint preserves later removals instead of replacing the plate again");h.succeed();
        });
    }
    @AfterBatch(batch="novel_courtyard") public static void c1(ServerLevel l){if(courtyard!=null){courtyard.close();courtyard=null;}}
    @AfterBatch(batch="novel_whale") public static void c2(ServerLevel l){if(whale!=null){whale.close();whale=null;}}
    @AfterBatch(batch="novel_well") public static void c3(ServerLevel l){if(well!=null){well.close();well=null;}}
    @AfterBatch(batch="novel_plain") public static void c4(ServerLevel l){if(plain!=null){plain.close();plain=null;}}
    @AfterBatch(batch="novel_ward") public static void c5(ServerLevel l){if(ward!=null){ward.close();ward=null;}}
    @AfterBatch(batch="novel_karen") public static void c6(ServerLevel l){if(karen!=null){karen.close();karen=null;}}
    @AfterBatch(batch="novel_upgrade") public static void c7(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}
    @GameTest(template="empty") public static void novelSitesAppendAndOutdoorEntriesUseTheirRealDimension(GameTestHelper h){
        for(int y:new int[]{65,80,150,250})for(var place:NovelVignettes.PLACES){var origin=new BlockPos(100,y,100);var b=LabyrinthPlaces.base(origin,place);var slot=LabyrinthPlaces.slotBounds(origin,place);var r=place.room();
            h.assertTrue(b!=null&&slot.isInside(b.offset(r.minX(),r.minY(),r.minZ()))&&slot.isInside(b.offset(r.maxX(),r.maxY(),r.maxZ())),"the appended scene fits its native height at "+y+": "+place.id());}
        h.assertTrue(LabyrinthPlace.KAREN_ROOM.slot()==38&&LabyrinthPlace.TED_CAVER.slot()==32&&WitnessAccount.Story.of("karen_room")==null,"the anchor is scenery/navigation, excluded from Witness");
        var origin=new BlockPos(100,80,100);var data=new LabyrinthData();for(var p:NovelVignettes.PLACES){LabyrinthBuilder.registerDoors(data,p,LabyrinthPlaces.base(origin,p));h.assertTrue(data.door(p.entryDoorId()).dimension.equals(NovelRooms.dimension(p)),"entry and return use the authored dimension");}h.succeed();}
    @GameTest(template="empty",batch="novel_courtyard",timeoutTicks=130) public static void actualCatKeyAndFinalReadingAreFiniteAndPersonal(GameTestHelper h){
        courtyard=new Fixture(h,32300,LabyrinthPlace.ZAMPANO_COURTYARD);var f=courtyard;var p=f.player();var peer=f.player();
        for(var spec:f.place.doors())if(spec.destination().equals(LabyrinthData.DEALT)){var door=f.data().door(f.place.doorId(spec));h.assertTrue(f.data().deal(p.getUUID(),door).place().equals(f.place.id()),"a courtyard without earlier history loops back instead of inventing a forward destination");}
        h.succeedWhen(()->{var cats=f.l.getEntitiesOfClass(Cat.class,IndianLakeRooms.bounds(f.b,f.place),c->c.getPersistentData().getInt(NovelVignettes.CAT)==5);h.assertTrue(!cats.isEmpty(),"the real last cat is loaded");var cat=cats.get(0);
            f.at(p,.5,0,-14.5);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.COOKED_BEEF));var feed=new PlayerInteractEvent.EntityInteract(p,InteractionHand.MAIN_HAND,cat);NeoForge.EVENT_BUS.post(feed);
            h.assertTrue(f.data().state(NovelVignettes.STATE).getBoolean("CatCoaxed")&&cat.isTame()&&p.getUUID().equals(cat.getOwnerUUID()),"one vanilla meat coaxes and adopts the actual cat");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"feeding and arrival grant no ending");
            f.at(p,.5,0,-17.5);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(NovelRegistry.ARCHIVE_KEY.get()));f.click(p,NovelRooms.ARCHIVE_DOOR);
            h.assertTrue(f.l.getBlockState(f.b.offset(NovelRooms.ARCHIVE_DOOR)).getValue(DoorBlock.OPEN),"the key opens the real sealed apartment");
            f.at(p,-4.5,0,-28.5);f.click(p,NovelRooms.ARCHIVE_DESK);var menu=(NovelVignettes.NovelBookMenu)p.containerMenu;
            h.assertTrue(menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,3)&&!menu.clickMenuButton(p,999),"one original collection and native page bounds");
            h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ZAMPANO),"taking the survey early is not reading its ending");
            h.assertTrue(menu.clickMenuButton(p,102)&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.ZAMPANO),"the native final page records a personal understanding");
            h.assertTrue(!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.ZAMPANO)&&p.getInventory().countItem(NovelRegistry.COLLAR.get())==1,"the collar is finite and a peer borrows no evidence");
            f.reload();f.click(p,NovelRooms.ARCHIVE_DESK);h.assertTrue(!((NovelVignettes.NovelBookMenu)p.containerMenu).clickMenuButton(p,3),"native save retains finite paper");});}
    private static ItemStack letter(ServerPlayer p,String text){
        var book=new ItemStack(Items.WRITTEN_BOOK);book.set(DataComponents.WRITTEN_BOOK_CONTENT,new net.minecraft.world.item.component.WrittenBookContent(net.minecraft.server.network.Filterable.passThrough("A letter"),p.getGameProfile().getName(),0,
            List.of(net.minecraft.server.network.Filterable.passThrough(net.minecraft.network.chat.Component.literal(text))),true));return book;}
    private static String page(ItemStack book,int index){return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().get(index).raw().getString();}
    @GameTest(template="empty",batch="novel_whale",timeoutTicks=240) public static void postedLettersAreAnsweredBeforeTheyArriveAndComeHomeToHerBox(GameTestHelper h){
        whale=new Fixture(h,32600,LabyrinthPlace.WHALE);var f=whale;var p=f.player();var peer=f.player();
        h.runAfterDelay(8,()->{
            for(int n=1;n<=12;n++){var st=f.l.getBlockState(f.b.offset(WhaleInstitute.pigeonhole(n)));
                h.assertTrue(st.is(NovelRegistry.PIGEONHOLE.get())&&st.getValue(PigeonholeBlock.NUMBER)==n&&WhaleInstitute.pigeonholeAt(WhaleInstitute.pigeonhole(n))==n,"pigeonhole "+n+" is a real numbered box on the post room wall");}
            h.assertTrue(f.l.getBlockState(f.b.offset(WhaleInstitute.HER_DOOR)).getBlock() instanceof DoorBlock&&f.l.getBlockState(f.b.offset(1,2,-23)).is(NovelRegistry.SIGN.get())&&f.l.getBlockState(f.b.offset(1,2,-23)).getValue(InstituteSignBlock.KIND)==InstituteSignBlock.Kind.MISSING,"her real doorway has only its missing-number screw holes, centered above it");
            for(int i=0;i<=5;i++)for(int z:new int[]{-30,-31}){var body=new AABB(f.b.getX()-6-i+.2,f.b.getY()+i+1.01,f.b.getZ()+z+.2,f.b.getX()-6-i+.8,f.b.getY()+i+2.81,f.b.getZ()+z+.8);h.assertTrue(f.l.noCollision(null,body),"a standing player has continuous headroom up every actual institute tread: "+i);}
            for(int x=-13;x<=13;x++)for(int z=-32;z<=-25;z++)h.assertTrue(!f.l.getBlockState(f.b.offset(x,10,z)).isAir(),"the attic roof contains the stairs and rooms instead of exposing the caves");
            // Paper from her desk, one sheet at a time; an unsigned sheet does not go.
            f.at(p,7.5,0,-22.5);f.click(p,WhaleInstitute.DESK);f.click(p,WhaleInstitute.DESK);
            h.assertTrue(p.getInventory().countItem(Items.WRITABLE_BOOK)==1&&p.getMainHandItem().is(Items.WRITABLE_BOOK),"the desk gives one blank letter at a time");
            f.at(p,5.5,0,-7.5);f.click(p,WhaleInstitute.OUTGOING);
            h.assertTrue(f.own(p).getCompound(WhaleInstitute.KEY).getInt("Sent")==0&&p.getMainHandItem().is(Items.WRITABLE_BOOK),"the slot refuses an unsigned letter and leaves it in hand");
            String[] said={"I keep the window open now. The hall is longer at night.","Nobody here knows my name yet. They will.","Is it Thursday where you are?"};
            String[] heard={"I keep the window open now.","Nobody here knows my name yet.","Is it Thursday where you are?"};
            List<ItemStack> mine=new ArrayList<>();
            for(int n=1;n<=3;n++){var sent=letter(p,said[n-1]);mine.add(sent.copy());p.setItemInHand(InteractionHand.MAIN_HAND,sent);f.click(p,WhaleInstitute.OUTGOING);
                var reply=p.getMainHandItem();var content=reply.get(DataComponents.WRITTEN_BOOK_CONTENT);
                h.assertTrue(f.own(p).getCompound(WhaleInstitute.KEY).getInt("Sent")==n&&content!=null&&content.author().equals("Pelafina"),"letter "+n+" goes, and the hand that let it go already holds her answer");
                h.assertTrue(page(reply,0).contains(heard[n-1])&&page(reply,0).contains(p.getGameProfile().getName()),"the answer quotes the letter it has not yet received: "+page(reply,0));
                var initials=new StringBuilder();for(var line:page(reply,2).split("\n"))initials.append(line.charAt(0));
                h.assertTrue(initials.toString().equals("SEVEN")&&!page(reply,0).contains("Room 7"),"each answer is headed from the wrong room and its lines begin with the right one: "+initials);}
            var fourth=letter(p,"One more.");p.setItemInHand(InteractionHand.MAIN_HAND,fourth);f.click(p,WhaleInstitute.OUTGOING);
            h.assertTrue(f.own(p).getCompound(WhaleInstitute.KEY).getInt("Sent")==3&&p.getMainHandItem().is(Items.WRITTEN_BOOK)&&page(p.getMainHandItem(),0).equals("One more."),"the slot takes three letters and no more");
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            // Only her box holds anything, only for its writer, and only once.
            f.at(p,10.5,0,-4.5);f.click(p,WhaleInstitute.pigeonhole(3));f.at(peer,10.5,0,-4.5);f.click(peer,WhaleInstitute.pigeonhole(WhaleInstitute.ROOM));
            h.assertTrue(!f.own(p).getCompound(WhaleInstitute.KEY).getBoolean("Returned")&&peer.getInventory().countItem(Items.WRITTEN_BOOK)==0,"a wrong box is empty, and her box is empty for a reader who wrote nothing");
            int before=p.getInventory().countItem(Items.WRITTEN_BOOK);f.click(p,WhaleInstitute.pigeonhole(WhaleInstitute.ROOM));f.click(p,WhaleInstitute.pigeonhole(WhaleInstitute.ROOM));
            h.assertTrue(f.own(p).getCompound(WhaleInstitute.KEY).getBoolean("Returned")&&p.getInventory().countItem(Items.WRITTEN_BOOK)==before+3,"her box holds the three letters that were sent, once");
            h.assertTrue(WhaleInstitute.calendarLines(3)[3].getString().equals("today")&&WhaleInstitute.calendarLines(1)[0].getStyle().isStrikethrough(),"her calendar is crossed out again after each letter");
            for(var original:mine){boolean found=false;for(var stack:p.getInventory().items)found|=ItemStack.isSameItemSameComponents(stack,original);h.assertTrue(found,"what came back is exactly what was written: "+page(original,0));}
            // Read anywhere else it is only a letter; read in her room, it is the end.
            int slot=-1;for(int i=0;i<p.getInventory().items.size()&&slot<0;i++)if(ItemStack.isSameItemSameComponents(p.getInventory().items.get(i),mine.get(0)))slot=i;
            p.getInventory().selected=slot;var read=new PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND);NeoForge.EVENT_BUS.post(read);
            h.assertTrue(!(p.containerMenu instanceof NovelVignettes.NovelBookMenu)&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WHALE),"reading it in the post room changes nothing");
            f.at(p,8.5,0,-22.5);read=new PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND);NeoForge.EVENT_BUS.post(read);
            h.assertTrue(read.isCanceled()&&p.containerMenu instanceof NovelVignettes.NovelBookMenu menu&&!menu.clickMenuButton(p,3),"in her room it opens as her reading, and nothing can be taken from it");
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WHALE)&&p.getInventory().countItem(NovelRegistry.ENVELOPE.get())==1
                &&mine.stream().allMatch(o->p.getInventory().items.stream().anyMatch(s->ItemStack.isSameItemSameComponents(s,o))),"a one-page letter read where it was going resolves the institute and leaves an envelope, keeping the letter");
            h.assertTrue(!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.WHALE),"a peer borrows nothing");
            p.closeContainer();f.reload();var w=f.own(p).getCompound(WhaleInstitute.KEY);
            h.assertTrue(w.getInt("Sent")==3&&w.getBoolean("Returned")&&w.getList("Posted",Tag.TAG_COMPOUND).size()==3&&f.own(p).getBoolean("Yield_Envelope"),"native reload keeps the posted letters, the returned bundle and the finite envelope");
            // The envelope never takes its sender out of a room; from the manor's hall it returns them, once a day.
            ItemStack envelope=ItemStack.EMPTY;for(var stack:p.getInventory().items)if(stack.is(NovelRegistry.ENVELOPE.get()))envelope=stack;
            p.getInventory().selected=p.getInventory().items.indexOf(envelope);var room=p.position();p.getMainHandItem().use(f.l,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.position().equals(room)&&!SelfAddressedEnvelopeItem.record(p).contains("RecallDay"),"inside the institute it will not go");
            var manor=HideAndClap.manorRespawn(f.origin);p.teleportTo(f.l,manor.x+3,manor.y,manor.z,0,0);f.data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,manor,0,true));
            p.getMainHandItem().use(f.l,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.position().distanceToSqr(manor)<1&&f.data().returnDepth(p.getUUID())==0&&SelfAddressedEnvelopeItem.record(p).contains("RecallDay"),"returned to sender: the manor, and the way back spent");
            p.teleportTo(f.l,manor.x+3,manor.y,manor.z,0,0);p.getMainHandItem().use(f.l,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.position().distanceToSqr(manor)>4,"only once a day");
            // Outside it seals one stack; dying away from the House, it is waiting on waking.
            var outside=f.l.getServer().overworld();var spot=h.absolutePos(BlockPos.ZERO).offset(25,4,25);outside.setBlock(spot.below(),Blocks.STONE.defaultBlockState(),2);
            p.teleportTo(outside,spot.getX()+.5,spot.getY(),spot.getZ()+.5,0,0);p.hasChangedDimension();
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.DIAMOND,5));p.getMainHandItem().use(outside,p,InteractionHand.MAIN_HAND);
            h.assertTrue(p.getOffhandItem().isEmpty()&&p.getMainHandItem().get(DataComponents.CUSTOM_DATA).copyTag().contains(SelfAddressedEnvelopeItem.SEALED),"one stack is sealed inside");
            var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();drops.add(new net.minecraft.world.entity.item.ItemEntity(outside,p.getX(),p.getY(),p.getZ(),p.getMainHandItem().copy()));drops.add(new net.minecraft.world.entity.item.ItemEntity(outside,p.getX(),p.getY(),p.getZ(),new ItemStack(Items.BREAD)));
            p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            SelfAddressedEnvelopeItem.drops(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(p,p.damageSources().generic(),drops,false));
            h.assertTrue(drops.size()==1&&drops.get(0).getItem().is(Items.BREAD),"only the sealed envelope stays out of the death drops");
            SelfAddressedEnvelopeItem.respawn(new PlayerEvent.PlayerRespawnEvent(p,false));
            ItemStack back=ItemStack.EMPTY;for(var stack:p.getInventory().items)if(stack.is(NovelRegistry.ENVELOPE.get()))back=stack;
            h.assertTrue(!back.isEmpty()&&!back.get(DataComponents.CUSTOM_DATA).copyTag().contains(SelfAddressedEnvelopeItem.SEALED)&&p.getInventory().countItem(Items.DIAMOND)==5,"on waking the envelope and what it held are back, opened, to be sealed again");
            h.succeed();
        });}
    @GameTest(template="empty",batch="novel_well",timeoutTicks=360) public static void nativeWellWaitReopensCoverAndClimbingOutRestoresHeight(GameTestHelper h){
        well=new Fixture(h,32900,LabyrinthPlace.BARN_WELL);var f=well;var p=f.player();var peer=f.player();f.at(p,.5,-12,-22.5);int[] stage={0};
        h.runAfterDelay(30,()->{h.assertTrue(NovelVignettes.childScale(p)&&f.l.getBlockState(f.b.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN)&&((WellCoverBlockEntity)f.l.getBlockEntity(f.b.offset(NovelRooms.WELL))).progress()>0,"child height accompanies a slowly moving cover, with time to look up");f.at(p,.5,-2,-22.5);f.click(p,NovelRooms.WELL);h.assertTrue(f.own(p).getInt("WellTicks")<NovelVignettes.WELL_WAIT,"using the moving cover cannot shorten the personal vigil");});
        h.runAfterDelay(185,()->{h.assertTrue(f.own(p).getInt("WellTicks")<NovelVignettes.WELL_WAIT&&!f.l.getBlockState(f.b.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN),"the cover stays closed while the child waits near the top");var own=f.own(p);own.putInt("WellTicks",NovelVignettes.WELL_WAIT-1);NovelVignettes.save(f.data(),p.getUUID(),own);});
        h.runAfterDelay(235,()->{h.assertTrue(f.own(p).getInt("WellTicks")==1200&&f.l.getBlockState(f.b.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN),"the saved wait opens a real way up");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"a wait before climbing out is unfinished");stage[0]=1;});
        h.onEachTick(()->{if(stage[0]==1){if(p.getY()<f.b.getY()+2){h.assertTrue(p.onClimbable(),"the ladder and open cover form a continuous climb at "+p.position());p.move(MoverType.SELF,new Vec3(0,.14,0));return;}
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BARN_WELL)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BARN_WELL),"climbing physically above the shaft resolves only that child");f.at(p,2.5,0,-24.5);f.click(p,NovelRooms.RIBBON);f.click(p,NovelRooms.RIBBON);h.assertTrue(p.getInventory().countItem(NovelRegistry.RIBBON.get())==1,"the ribbon is finite");f.at(p,40,0,-3);stage[0]=2;}
            else if(stage[0]==2&&!NovelVignettes.childScale(p)){h.succeed();}});}
    @GameTest(template="empty",batch="novel_plain",timeoutTicks=260) public static void nativeCameraDevelopsOneOwnedCapturedFrameAndMotherCustodyStopsItsReturn(GameTestHelper h){
        plain=new Fixture(h,33200,LabyrinthPlace.PLAIN);var f=plain;var p=f.player();var peer=f.player();f.at(p,3.5,0,-6.5);f.click(p,new BlockPos(3,0,-7));f.click(p,new BlockPos(3,0,-7));h.assertTrue(p.getInventory().countItem(NovelRegistry.CAMERA.get())==1,"the equipment barrel gives one camera per explorer");
        f.at(p,.5,0,-20.5);h.runAfterDelay(8,()->{var target=f.b.offset(NovelRooms.FIGURE).getCenter().subtract(p.getEyePosition());p.setYRot(180);p.setYHeadRot(180);p.yHeadRotO=180;p.setXRot((float)(-Math.atan2(target.y,Math.sqrt(target.x*target.x+target.z*target.z))*180/Math.PI));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(NovelRegistry.CAMERA.get()));p.startUsingItem(InteractionHand.MAIN_HAND);});
        h.runAfterDelay(12,()->{var target=f.b.offset(NovelRooms.FIGURE).getCenter().subtract(p.getEyePosition()).normalize();h.assertTrue(NovelVignettes.inside(p,f.place)&&p.isUsingItem()&&p.getViewVector(1).dot(target)>.997,"native camera aim/use survives entry: inside="+NovelVignettes.inside(p,f.place)+" use="+p.isUsingItem()+" dot="+p.getViewVector(1).dot(target)+" aim="+f.own(p).getInt("Aim")+" at="+p.position());});
        h.runAfterDelay(90,()->{var own=f.own(p);h.assertTrue(own.hasUUID("PlainExposure0464")&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.PLAIN),"the server waits for a real exposure instead of awarding a placeholder frame");
            byte[] pixels=new byte[16384];java.util.Arrays.fill(pixels,(byte)net.minecraft.world.level.material.MapColor.COLOR_BLUE.getPackedId(net.minecraft.world.level.material.MapColor.Brightness.NORMAL));
            var frame=new io.github.knaitoe.theoldesthouse.network.PlainFramePayload(own.getUUID("PlainExposure0464"),pixels);
            h.assertTrue(!NovelVignettes.capturePlainFrame(peer,frame)&&NovelVignettes.capturePlainFrame(p,frame)&&!NovelVignettes.capturePlainFrame(p,frame),"another reader cannot develop the exposure, and the original requester develops it only once");
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.PLAIN)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.PLAIN),"holding the actual camera on the distant shape produces personal memory");
            var original=ItemStack.parseOptional(p.registryAccess(),f.own(p).getCompound("Photo"));p.stopUsingItem();for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(Items.FILLED_MAP))p.getInventory().setItem(i,ItemStack.EMPTY);
            p.setItemInHand(InteractionHand.OFF_HAND,original);p.server.overworld().setDayTime(p.server.overworld().getDayTime()+24000);});
        h.runAfterDelay(125,()->{h.assertTrue(p.getOffhandItem().is(Items.FILLED_MAP)&&p.getInventory().countItem(Items.FILLED_MAP)==1,"morning preserves the photograph already in the actual offhand");
            var original=p.getOffhandItem();p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);peer.containerMenu.setCarried(original);p.server.overworld().setDayTime(p.server.overworld().getDayTime()+24000);});
        h.runAfterDelay(165,()->{h.assertTrue(peer.containerMenu.getCarried().isEmpty()&&p.getInventory().countItem(Items.FILLED_MAP)==1,"morning recalls the actual peer cursor's original without duplicating it");
            var original=ItemStack.parseOptional(p.registryAccess(),f.own(p).getCompound("Photo"));for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(Items.FILLED_MAP))p.getInventory().setItem(i,ItemStack.EMPTY);MotherCollection.get(p.server).keepItem(original,p.registryAccess(),p.getUUID(),f.l.getGameTime());});
        h.runAfterDelay(205,()->{h.assertTrue(f.own(p).getBoolean("PhotoGivenAway"),"the actual Mother collection, rather than a borrowed map, settles the returning photograph");h.succeed();});}
    @GameTest(template="empty",batch="novel_ward",timeoutTicks=180) public static void wardDepartureResetsNightAndOnlyPresentDawnReadingCounts(GameTestHelper h){
        ward=new Fixture(h,33500,LabyrinthPlace.HOSPITAL);var f=ward;var p=f.player();var peer=f.player();
        h.runAfterDelay(12,()->{f.at(p,4.5,0,-13.5);f.click(p,NovelRooms.BUTTON);h.assertTrue(f.own(p).getInt("WardCalls")==1&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOSPITAL),"an actual call changes the chart but is no resolution");f.at(p,30,0,-3);});
        h.runAfterDelay(35,()->{h.assertTrue(f.own(p).getInt("WardTicks")==0,"leaving begins a fresh attempt");f.at(p,.5,0,-10.5);var own=f.own(p);own.putInt("WardTicks",NovelVignettes.WARD_NIGHT-1);NovelVignettes.save(f.data(),p.getUUID(),own);});
        h.runAfterDelay(65,()->{h.assertTrue(f.own(p).getBoolean("WardFinished"),"a present final tick completes the compressed night");f.at(p,-3.5,0,-13.5);f.click(p,NovelRooms.WARD_NOTE);var menu=(NovelVignettes.NovelBookMenu)p.containerMenu;
            h.assertTrue(menu.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().get(0).raw().getStyle().getColor()!=null&&menu.clickMenuButton(p,101),"the authored purple dawn page opens in a native book");
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOSPITAL)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.HOSPITAL),"a second player still has their own unfinished night");peer.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);f.at(peer,-3.5,0,-13.5);h.assertTrue(!NovelVignettes.inside(peer,f.place),"a real spectator cannot inherit the dawn");f.reload();h.assertTrue(f.own(p).getBoolean("WardFinished"),"the dawn survives reload");h.succeed();});}
    @GameTest(template="empty",batch="novel_karen",timeoutTicks=100) public static void karenBedIsTheOnlyNewSpawnExceptionAndProjectorUsesCapturedWorld(GameTestHelper h){
        karen=new Fixture(h,33800,LabyrinthPlace.KAREN_ROOM);var f=karen;var p=f.player();p.setRespawnPosition(HouseDimensions.INTERIOR,f.b.offset(NovelRooms.BED),180,false,false);
        h.assertTrue(f.b.offset(NovelRooms.BED).equals(p.getRespawnPosition()),"the native bed position is accepted past the threshold");
        var forbidden=f.b.offset(-5,0,-4);p.setRespawnPosition(HouseDimensions.INTERIOR,forbidden,0,false,false);h.assertTrue(f.b.offset(NovelRooms.BED).equals(p.getRespawnPosition()),"an ordinary labyrinth bed cannot replace the anchor");
        var j=LabyrinthPlaces.base(f.origin,LabyrinthPlace.JUNCTION);LabyrinthBuilder.buildJunction(f.l,j);IndianLakeRooms.keepLoaded(f.l,j,LabyrinthPlace.JUNCTION);p.moveTo(Vec3.atBottomCenterOf(j.north(5)));p.setYRot(180);NovelVignettes.recordVisit(p,LabyrinthPlace.JUNCTION);
        h.runAfterDelay(65,()->{h.assertTrue(f.own(p).getList("Record",Tag.TAG_COMPOUND).size()==1,"the bounded real photograph renderer stores this explorer's visited room");f.at(p,.5,0,-4.5);f.click(p,NovelRooms.PROJECTOR);
            var photo=ItemStack.parseOptional(p.registryAccess(),f.own(p).getList("Record",Tag.TAG_COMPOUND).getCompound(0));h.assertTrue(photo.has(DataComponents.MAP_ID)&&f.l.getMapData(photo.get(DataComponents.MAP_ID))!=null,"the private projector's original is an actual captured native map");
            h.assertTrue(f.l.getEntitiesOfClass(net.minecraft.world.entity.decoration.ItemFrame.class,IndianLakeRooms.bounds(f.b,f.place)).isEmpty(),"a private projection creates no shared frame that reveals another player's photograph");h.assertTrue(!new SecretPhotographs.Album(1,p.getInventory(),p).getSlot(0).getItem().isEmpty(),"the original is available for native album pickup");h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"a navigation anchor grants no Witness evidence");h.succeed();});}
    @GameTest(template="empty",batch="novel_upgrade",timeoutTicks=2400) public static void layoutTwentyAppendsNovelPlacesWithoutRebuildingEarlierRooms(GameTestHelper h){
        upgrade=new Fixture(h,34700,LabyrinthPlace.KAREN_ROOM);var f=upgrade;HouseTestLevel.get(f.l.getServer(),HouseDimensions.OUTSIDE);
        var old=LabyrinthPlaces.base(f.origin,LabyrinthPlace.HARRIGAN);f.chunks.hold(f.l,IndianLakeRooms.bounds(old,LabyrinthPlace.HARRIGAN));var at=old.offset(2,0,-4);f.l.setBlock(at,Blocks.BARREL.defaultBlockState(),2);var barrel=(BarrelBlockEntity)f.l.getBlockEntity(at);barrel.setItem(0,new ItemStack(Items.DIAMOND,3));
        NovelRooms.box(f.l,old,-3,-1,-5,3,-1,-1,Blocks.SMOOTH_STONE.defaultBlockState());
        var id=UUID.randomUUID();final Entity[] original={null};final UUID[] identity={null};final boolean[] checked={false};
        h.onEachTick(()->{
            if(original[0]==null){
                if(!f.chunks.ready())return;
                var actor=FinaleRegistry.WITNESS.get().create(f.l);actor.moveTo(Vec3.atBottomCenterOf(old.north(3)));h.assertTrue(f.l.addFreshEntity(actor),"the preserved fixture actor is actually added");f.extra.add(actor);original[0]=actor;identity[0]=actor.getUUID();
                var scene=new CompoundTag();scene.putBoolean("ReadingFinished",true);f.data().setState(HarriganVignette.ID,scene);WitnessAccount.resolve(f.data(),id,WitnessAccount.Story.HARRIGAN,"buried_phone");
                f.data().setBuilt(20,f.origin);LabyrinthBuilder.ensureBuilt(f.l.getServer());LabyrinthBuilder.finishGameTest(f.l.getServer());return;
            }
            if(checked[0])return;checked[0]=true;var actor=original[0];
            h.assertTrue(LabyrinthBuilder.isBuilt(f.l.getServer()),"the appended layout finishes");h.assertTrue(f.l.getBlockEntity(at)==barrel&&barrel.getItem(0).getCount()==3&&actor.getUUID().equals(identity[0])&&actor.isAlive(),"earlier native containers and actors retain identity; same barrel="+(f.l.getBlockEntity(at)==barrel)+" count="+barrel.getItem(0).getCount()+" alive="+actor.isAlive()+" actor="+actor.position());
            h.assertTrue(f.data().state(HarriganVignette.ID).getBoolean("ReadingFinished")&&WitnessAccount.has(f.data(),id,WitnessAccount.Story.HARRIGAN),"saved reading and personal evidence remain");
            for(var place:NovelVignettes.PLACES){var door=f.data().door(place.entryDoorId());var level=f.l.getServer().getLevel(NovelRooms.dimension(place));h.assertTrue(door!=null&&level.getBlockState(door.lower).getBlock() instanceof DoorBlock,"the appended site has an actual entrance in its own dimension");}h.succeed();});}

}
