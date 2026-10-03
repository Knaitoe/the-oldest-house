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
        final List<ServerPlayer> players=new ArrayList<>();final List<Entity> extra=new ArrayList<>();
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
            List<Entity> cleanup=new ArrayList<>();for(Entity e:l.getAllEntities())if(e!=null&&e.blockPosition().distSqr(b)<240*240&&(e instanceof Cat||e instanceof NovelVulture||e instanceof NovelActor||e instanceof net.minecraft.world.entity.decoration.ItemFrame||e instanceof net.minecraft.world.entity.item.ItemEntity))cleanup.add(e);cleanup.forEach(Entity::discard);
            var bounds=IndianLakeRooms.bounds(b,place);for(int x=((int)bounds.minX-1)>>4;x<=((int)bounds.maxX+1)>>4;x++)for(int z=((int)bounds.minZ-1)>>4;z<=((int)bounds.maxZ+1)>>4;z++)l.getChunkSource().removeRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(x,z),3,b);
            var s=l.getServer();s.overworld().getDataStorage().set("the_oldest_house",oldHouse);s.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);s.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);s.overworld().setDayTime(oldDayTime);LabyrinthBuilder.clearAll();}
    }
    private static Fixture courtyard,whale,well,plain,ward,karen,upgrade;
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
    @GameTest(template="empty",batch="novel_whale",timeoutTicks=240) public static void lettersArriveInActualChestsAndPausesOpenOnlyTheReadersAttic(GameTestHelper h){
        whale=new Fixture(h,32600,LabyrinthPlace.WHALE);var f=whale;var p=f.player();var peer=f.player();var chestPos=f.b.offset(8,0,-4);f.l.setBlock(chestPos,Blocks.CHEST.defaultBlockState(),2);
        h.runAfterDelay(8,()->{
            var own=f.own(p);own.putLong("MailDue",f.l.getGameTime());NovelVignettes.save(f.data(),p.getUUID(),own);p.openMenu((ChestBlockEntity)f.l.getBlockEntity(chestPos));p.closeContainer();
            h.assertTrue(f.own(p).getInt("Letters")==0,"empty chests inside the House do not deliver ordinary-world post");
            var outside=f.l.getServer().overworld();var ordinary=h.absolutePos(BlockPos.ZERO).offset(25,4,25);outside.setBlock(ordinary.below(),Blocks.STONE.defaultBlockState(),2);outside.setBlock(ordinary,Blocks.CHEST.defaultBlockState(),2);var chest=(ChestBlockEntity)outside.getBlockEntity(ordinary);p.teleportTo(outside,ordinary.getX()+.5,ordinary.getY(),ordinary.getZ()+1.5,180,0);p.hasChangedDimension();
            chest.setItem(7,new ItemStack(Items.EMERALD,2));p.openMenu(chest);p.closeContainer();h.assertTrue(f.own(p).getInt("Letters")==0&&chest.getItem(7).getCount()==2,"a partly filled outside chest is left intact");chest.clearContent();
            for(int n=0;n<3;n++){own=f.own(p);own.putLong("MailDue",outside.getGameTime());NovelVignettes.save(f.data(),p.getUUID(),own);p.openMenu(chest);
                var letter=chest.getItem(0);h.assertTrue(letter.has(DataComponents.WRITTEN_BOOK_CONTENT)&&letter.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("LetterTo").equals(p.getUUID()),"each actual outside delivery keeps the reader and original letter custody");
                p.closeContainer();chest.clearContent();
            }
            h.assertTrue(f.own(p).getInt("Letters")==3,"three finite letters arrive after opening three genuinely empty outside chests");
            p.teleportTo(f.l,f.b.getX()-3.5,f.b.getY()+8,f.b.getZ()-19.5,0,0);p.hasChangedDimension();
        });
        int[] times={12,20,28,52,76,84};for(int t:times)h.runAfterDelay(t,()->{var at=f.b.offset(NovelRooms.ATTIC_DOOR);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(p,at,Direction.EAST,PlayerInteractEvent.LeftClickBlock.Action.START));});
        h.runAfterDelay(140,()->{h.assertTrue(f.own(p).getBoolean("AtticKnocked")&&f.l.getBlockState(f.b.offset(NovelRooms.ATTIC_DOOR)).getValue(DoorBlock.OPEN),"three, one, two with real pauses opens the middle attic");
            f.at(p,-8.5,8,-23.5);f.click(p,NovelRooms.ATTIC_DESK);h.assertTrue(((NovelVignettes.NovelBookMenu)p.containerMenu).clickMenuButton(p,101)&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.WHALE),"the undated final letter is the resolution");
            f.at(peer,-8.5,8,-23.5);f.click(peer,NovelRooms.ATTIC_DESK);h.assertTrue(!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.WHALE),"a shared open attic does not grant a second reader the correspondence");f.reload();h.assertTrue(f.own(p).getInt("Letters")==3&&f.own(p).getBoolean("AtticKnocked"),"native reload keeps correspondence and the actual knock");h.succeed();});}
    @GameTest(template="empty",batch="novel_well",timeoutTicks=260) public static void nativeWellWaitReopensCoverAndClimbingOutRestoresHeight(GameTestHelper h){
        well=new Fixture(h,32900,LabyrinthPlace.BARN_WELL);var f=well;var p=f.player();var peer=f.player();f.at(p,.5,-12,-22.5);int[] stage={0};
        h.runAfterDelay(30,()->{h.assertTrue(NovelVignettes.childScale(p)&&!f.l.getBlockState(f.b.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN),"child height and the actual cover accompany the first bottom wait");f.at(p,.5,-2,-22.5);f.click(p,NovelRooms.WELL);h.assertTrue(!f.l.getBlockState(f.b.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN),"climbing early and using the cover cannot shorten the vigil");});
        h.runAfterDelay(45,()->{h.assertTrue(f.own(p).getInt("WellTicks")<NovelVignettes.WELL_WAIT&&!f.l.getBlockState(f.b.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN),"the cover stays closed while the child waits near the top");var own=f.own(p);own.putInt("WellTicks",NovelVignettes.WELL_WAIT-1);NovelVignettes.save(f.data(),p.getUUID(),own);});
        h.runAfterDelay(60,()->{h.assertTrue(f.own(p).getInt("WellTicks")==1200&&f.l.getBlockState(f.b.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN),"the saved wait opens a real way up");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0,"a wait before climbing out is unfinished");stage[0]=1;});
        h.onEachTick(()->{if(stage[0]==1){if(p.getY()<f.b.getY()+2){h.assertTrue(p.onClimbable(),"the ladder and open cover form a continuous climb at "+p.position());p.move(MoverType.SELF,new Vec3(0,.14,0));return;}
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.BARN_WELL)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BARN_WELL),"climbing physically above the shaft resolves only that child");f.at(p,2.5,0,-24.5);f.click(p,NovelRooms.RIBBON);f.click(p,NovelRooms.RIBBON);h.assertTrue(p.getInventory().countItem(NovelRegistry.RIBBON.get())==1,"the ribbon is finite");f.at(p,40,0,-3);stage[0]=2;}
            else if(stage[0]==2&&!NovelVignettes.childScale(p)){h.succeed();}});}
    @GameTest(template="empty",batch="novel_plain",timeoutTicks=260) public static void nativeSpyglassMakesOneFrameAndMotherCustodyStopsItsReturn(GameTestHelper h){
        plain=new Fixture(h,33200,LabyrinthPlace.PLAIN);var f=plain;var p=f.player();var peer=f.player();f.at(p,3.5,0,-6.5);f.click(p,new BlockPos(3,0,-7));f.click(p,new BlockPos(3,0,-7));h.assertTrue(p.getInventory().countItem(Items.SPYGLASS)==1,"the equipment barrel gives one spyglass per explorer");
        f.at(p,.5,0,-20.5);h.runAfterDelay(8,()->{var target=f.b.offset(NovelRooms.FIGURE).getCenter().subtract(p.getEyePosition());p.setYRot(180);p.setYHeadRot(180);p.yHeadRotO=180;p.setXRot((float)(-Math.atan2(target.y,Math.sqrt(target.x*target.x+target.z*target.z))*180/Math.PI));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.SPYGLASS));p.startUsingItem(InteractionHand.MAIN_HAND);});
        h.runAfterDelay(12,()->{var target=f.b.offset(NovelRooms.FIGURE).getCenter().subtract(p.getEyePosition()).normalize();h.assertTrue(NovelVignettes.inside(p,f.place)&&p.isUsingItem()&&p.getViewVector(1).dot(target)>.997,"native spyglass aim/use survives entry: inside="+NovelVignettes.inside(p,f.place)+" use="+p.isUsingItem()+" dot="+p.getViewVector(1).dot(target)+" aim="+f.own(p).getInt("Aim")+" at="+p.position());});
        h.runAfterDelay(90,()->{h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.PLAIN)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.PLAIN),"holding the actual spyglass on the distant shape produces personal memory");
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
    @GameTest(template="empty",batch="novel_upgrade",timeoutTicks=180) public static void layoutTwentyAppendsNovelPlacesWithoutRebuildingEarlierRooms(GameTestHelper h){
        upgrade=new Fixture(h,34700,LabyrinthPlace.KAREN_ROOM);var f=upgrade;HouseTestLevel.get(f.l.getServer(),HouseDimensions.OUTSIDE);
        var old=LabyrinthPlaces.base(f.origin,LabyrinthPlace.HARRIGAN);var at=old.offset(2,0,-4);f.l.setBlock(at,Blocks.BARREL.defaultBlockState(),2);var barrel=(BarrelBlockEntity)f.l.getBlockEntity(at);barrel.setItem(0,new ItemStack(Items.DIAMOND,3));
        IndianLakeRooms.keepLoaded(f.l,old,LabyrinthPlace.HARRIGAN);NovelRooms.box(f.l,old,-3,-1,-5,3,-1,-1,Blocks.SMOOTH_STONE.defaultBlockState());var actor=FinaleRegistry.WITNESS.get().create(f.l);actor.moveTo(Vec3.atBottomCenterOf(old.north(3)));h.assertTrue(f.l.addFreshEntity(actor),"the preserved fixture actor is actually added");f.extra.add(actor);var identity=actor.getUUID();
        var scene=new CompoundTag();scene.putBoolean("ReadingFinished",true);f.data().setState(HarriganVignette.ID,scene);var id=UUID.randomUUID();WitnessAccount.resolve(f.data(),id,WitnessAccount.Story.HARRIGAN,"buried_phone");
        f.data().setBuilt(20,f.origin);LabyrinthBuilder.ensureBuilt(f.l.getServer());
        h.succeedWhen(()->{h.assertTrue(LabyrinthBuilder.isBuilt(f.l.getServer()),"the appended layout finishes");h.assertTrue(f.l.getBlockEntity(at)==barrel&&barrel.getItem(0).getCount()==3&&actor.getUUID().equals(identity)&&actor.isAlive(),"earlier native containers and actors retain identity; same barrel="+(f.l.getBlockEntity(at)==barrel)+" count="+barrel.getItem(0).getCount()+" alive="+actor.isAlive()+" actor="+actor.position());
            h.assertTrue(f.data().state(HarriganVignette.ID).getBoolean("ReadingFinished")&&WitnessAccount.has(f.data(),id,WitnessAccount.Story.HARRIGAN),"saved reading and personal evidence remain");
            for(var place:NovelVignettes.PLACES){var door=f.data().door(place.entryDoorId());var level=f.l.getServer().getLevel(NovelRooms.dimension(place));h.assertTrue(door!=null&&level.getBlockState(door.lower).getBlock() instanceof DoorBlock,"the appended site has an actual entrance in its own dimension");}});}

}
