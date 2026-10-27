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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;
/** Native input, real durations, finite original custody, independent readers and recognized upgrades. */
@GameTestHolder(TheOldestHouse.MOD_ID+"_service") @PrefixGameTestTemplate(false)
public final class ServiceFollowupTests {
 private static LiteraryTests.Fixture f;private static NovelTests.Fixture archive;
 private static final LabyrinthPlace D=LabyrinthPlace.SECOND_WAITRESS;
 private static void close(){if(f!=null){f.close();f=null;}if(archive!=null){for(var p:archive.players)NativeTestPlayers.remove(p);archive.players.clear();archive.close();archive=null;}}
 private static ServerPlayer diner(GameTestHelper h,String name){f=new LiteraryTests.Fixture(h);var p=f.player(name,D);f.chunks.hold(f.out,IndianLakeRooms.bounds(f.base(D),D));return p;}
 private static CompoundTag step(ServerPlayer p){var own=f.own(p,D);UncannyStories.reader(p,f.base(D),D,own);LiteraryVignettes.save(f.data(),p.getUUID(),D,own);return own;}
 @AfterBatch(batch="service_menu133") public static void menuDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_menu133",timeoutTicks=500)
 public static void menuShowsFoodThenEveryOriginalWordOnReuseWithoutCounterNarration(GameTestHelper h){var p=diner(h,"menu133");
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual diner resident")).thenExecute(()->{
   step(p);var menu=UncannyBreakfast.menu(p);var id=menu.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UncannyMenu");p.setItemInHand(InteractionHand.MAIN_HAND,menu);menu.use(f.out,p,InteractionHand.MAIN_HAND);var pages=(LiteraryVignettes.Pages)p.containerMenu;
   h.assertTrue(pages.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getFirst().raw().getString().contains("Pancakes")&&!pages.original().get(DataComponents.CUSTOM_DATA).copyTag().getBoolean("MenuNarrative133")&&!f.own(p,D).getBoolean("Read_Source"),"first native view contains food only");p.closeContainer();f.reload();menu.use(f.out,p,InteractionHand.MAIN_HAND);pages=(LiteraryVignettes.Pages)p.containerMenu;
   String all=pages.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(x->x.raw().getString()).collect(java.util.stream.Collectors.joining(" ")).replaceAll("\\s+"," ").strip();h.assertTrue(all.equals(UncannyTexts.log(D).replaceAll("\\s+"," ").strip())&&p.getMainHandItem()==menu&&menu.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UncannyMenu").equals(id),"reuse shows the exact full narrative on the same original across reload");p.closeContainer();
   var at=f.base(D).offset(UncannyRooms.source(D));p.moveTo(at.getX()+2,at.getY(),at.getZ()+.5);p.gameMode.useItemOn(p,f.out,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.EAST,at,false));h.assertTrue(p.containerMenu==p.inventoryMenu&&f.out.getBlockState(at).getValue(UncannyPropBlock.STAGE)==1&&f.out.isEmptyBlock(f.base(D).offset(UncannyRooms.account(D))),"the counter is a till; no duplicate ledger remains");
   var peer=f.player("menu_peer",D);h.assertTrue(!UncannyBreakfast.open(peer,menu),"a peer cannot assign another original menu to their own history");
  }).thenSucceed();
 }
 @AfterBatch(batch="service_portions133") public static void portionsDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_portions133",timeoutTicks=1000)
 public static void sevenRealEatingActionsPreserveOriginalsAndAnInterruptedBiteConsumesNothing(GameTestHelper h){
  var p=diner(h,"portions133");h.onEachTick(()->{if(p.isUsingItem())p.doTick();}); // Embedded connections need their ordinary native player tick.
  var at=f.base(D).offset(5,1,-19);var clock=new ItemStack(Items.CLOCK);CustomData.update(DataComponents.CUSTOM_DATA,clock,t->t.putUUID("OriginalClock",UUID.randomUUID()));UUID[] id={null};
  var seq=h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"table resident")).thenExecute(()->{
   var own=step(p);var meal=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalMeal"));id[0]=meal.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UncannyMeal");own.remove("OriginalMeal");own.putBoolean("MealReleased",true);own.putBoolean("MealServed",true);own.putLong("PlatePos131",at.asLong());LiteraryVignettes.save(f.data(),p.getUUID(),D,own);h.assertTrue(DinerPlate.place(f.out,at,meal)&&meal.isEmpty(),"finite original leaves saved custody for the real table");f.at(p,D,5.5,0,-17.2);
   for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));h.assertTrue(!DinerPlate.use(p,at)&&!DinerPlate.at(f.out,at).meal.isEmpty(),"a full inventory refuses an occupied-hand overwrite");p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,clock);
   var peer=f.player("meal_peer",D);f.at(peer,D,7.5,0,-17.5);h.assertTrue(!DinerPlate.use(peer,at),"the peer cannot consume this original serving");h.assertTrue(DinerPlate.use(p,at)&&p.isUsingItem()&&p.getUseItem().is(UncannyRegistry.MEAL.get()),"plate input starts the actual native food-use state");
  }).thenIdle(1).thenExecute(()->h.assertTrue(NativeTestPlayers.packets(p).stream().anyMatch(x->x instanceof net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket),"owning client receives native eating metadata")).thenIdle(8).thenExecute(p::stopUsingItem).thenIdle(3).thenExecute(()->{
   h.assertTrue(p.getMainHandItem()==clock&&UncannyMealItem.bites(DinerPlate.at(f.out,at).meal)==0&&!f.own(p,D).getBoolean("MealEaten127"),"interruption restores both originals without consuming a portion");var plate=DinerPlate.at(f.out,at);plate.loadWithComponents(plate.saveWithFullMetadata(p.registryAccess()),p.registryAccess());f.reload();
  });
  for(int i=1;i<=7;i++){final int bite=i;seq.thenExecute(()->h.assertTrue(DinerPlate.use(p,at)&&p.isUsingItem(),"start actual native portion "+bite)).thenWaitUntil(()->{var plate=DinerPlate.at(f.out,at);h.assertTrue(!p.isUsingItem()&&(bite==7?plate==null:plate!=null&&UncannyMealItem.bites(plate.meal)==bite),"complete native eating duration "+bite);}).thenExecute(()->{
   h.assertTrue(p.getMainHandItem()==clock&&f.own(p,D).getInt("MealPortions133")==bite&&f.own(p,D).getBoolean("DinerTransformed133")&&!f.own(p,D).getBoolean("UncannyFailed"),"actual completed bite changes only the reader while restoring the exact hand object");if(bite<7){var meal=DinerPlate.at(f.out,at).meal;h.assertTrue(meal.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UncannyMeal").equals(id[0])&&meal.get(DataComponents.CUSTOM_MODEL_DATA).value()/10==bite,"the same original loses one visible slice");}
  });}seq.thenSucceed();
 }
 @AfterBatch(batch="service_follow133") public static void followDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_follow133",timeoutTicks=700)
 public static void herOriginalBodyFollowsAroundFurnitureAndTimeTransformsHerWithoutEating(GameTestHelper h){var p=diner(h,"follow133");LiteraryActor[] a={null};UUID[] id={null};double[] before={0};
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"serving aisle resident")).thenExecute(()->{
   var own=step(p);a[0]=UncannyStories.actor(p,own,"CastFake");id[0]=a[0].getUUID();a[0].moveTo(Vec3.atBottomCenterOf(f.base(D).offset(3,0,-19)));a[0].appearance(LiteraryActor.UNCANNY_FALSE_WAITRESS,2);a[0].noPhysics=false;a[0].setNoGravity(false);own.putBoolean("SecondPlaced131",true);own.putInt("SecondManifestAge131",0);own.putBoolean("DinerFlow130",true);own.putBoolean("DinerHosted127",true);own.putBoolean("DinerRealReturned130",true);own.putInt("UncannyAge",600);own.putBoolean("MealServed",true);own.putBoolean("MealReleased",true);LiteraryVignettes.save(f.data(),p.getUUID(),D,own);f.at(p,D,.5,0,-13.5);before[0]=a[0].distanceToSqr(p);step(p);
  }).thenIdle(90).thenExecute(()->{
   h.assertTrue(a[0].getUUID().equals(id[0])&&a[0].distanceToSqr(p)<before[0]-10&&f.out.noCollision(a[0],a[0].getBoundingBox().deflate(.001)),"the same solid original follows through the actual aisle");var own=f.own(p,D);own.putLong("BoothPressureNext130",0);own.putInt("DinerPresence133",3595);LiteraryVignettes.save(f.data(),p.getUUID(),D,own);var menu=UncannyBreakfast.menu(p);p.setItemInHand(InteractionHand.MAIN_HAND,menu);menu.use(f.out,p,InteractionHand.MAIN_HAND);step(p);p.closeContainer();step(p);own=f.own(p,D);h.assertTrue(own.getBoolean("DinerTransformed133")&&own.getString("DinerTransformCause133").equals("time")&&!own.getBoolean("MealEaten127")&&a[0].phase()==3,"café time, including reading, transforms her without eating");f.reload();h.assertTrue(f.own(p,D).getUUID("CastFake").equals(id[0]),"reload retains original identity");
  }).thenSucceed();
 }
 @AfterBatch(batch="service_till133") public static void tillDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_till133",timeoutTicks=600)
 public static void theActualTillCrossingAndConfirmedHouseLandingConcludeOnlyThatReadersEncounter(GameTestHelper h){var p=diner(h,"till133");var peer=f.player("till_peer",D);var b=f.base(D);var door=b.offset(-10,0,-6);var home=f.origin.offset(0,0,20);var back=new LabyrinthData.Waypoint(f.in.dimension(),Vec3.atBottomCenterOf(home),0);f.chunks.hold(f.in,new AABB(home).inflate(4));
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"both actual landings resident")).thenExecute(()->{
   step(p);for(var at:BlockPos.betweenClosed(home.offset(-2,-1,-2),home.offset(2,-1,2)))f.in.setBlock(at,Blocks.STONE.defaultBlockState(),18);f.data().pushReturn(p.getUUID(),back);f.data().pushReturn(peer.getUUID(),back);f.at(p,D,-8.5,0,-5.5);LabyrinthDoors.tickPlayer(p,f.origin);p.gameMode.useItemOn(p,f.out,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(door.getCenter(),Direction.EAST,door,false));h.assertTrue(f.out.getBlockState(door).getValue(DoorBlock.OPEN)&&!f.own(p,D).getBoolean("Completed")&&f.data().returnDepth(p.getUUID())==1,"opening consumes no return and grants nothing");p.move(MoverType.SELF,new Vec3(-2.5,0,0));LabyrinthDoors.tickPlayer(p,f.origin);
  }).thenWaitUntil(()->h.assertTrue(p.serverLevel()==f.in&&f.own(p,D).getBoolean("Completed"),"actual native crossing lands at the saved House return")).thenExecute(()->{
   h.assertTrue(p.position().distanceToSqr(back.pos())<.01&&WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.SECOND_WAITRESS)&&f.data().returnDepth(p.getUUID())==0&&f.data().returnDepth(peer.getUUID())==1&&!f.own(peer,D).getBoolean("Completed"),"only the real departing reader receives this conclusion");f.reload();h.assertTrue(f.own(p,D).getBoolean("DinerTillReturned133"),"confirmed return is saved");
  }).thenSucceed();
 }
 @AfterBatch(batch="service_sun133") public static void sunDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_sun133",timeoutTicks=500)
 public static void nativeMiningBreaksBothPaintedSunBoardsThroughHouseProtection(GameTestHelper h){f=new LiteraryTests.Fixture(h);var place=LabyrinthPlace.PAINTED_INTERIOR;var p=f.player("sun133",place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(f.base(place),place));
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"sun resident")).thenExecute(()->{var at=f.base(place).offset(UncannyRooms.PARLOR_BOARD);f.at(p,place,.5,0,-12.5);var e=new PlayerInteractEvent.LeftClickBlock(p,at,Direction.SOUTH,PlayerInteractEvent.LeftClickBlock.Action.START);NeoForge.EVENT_BUS.post(e);h.assertTrue(e.isCanceled()&&f.level(place).isEmptyBlock(at)&&f.level(place).isEmptyBlock(at.below())&&f.own(p,place).getBoolean("BoardOpened"),"real mining input clears both sun halves");f.reload();h.assertTrue(f.own(p,place).getBoolean("BoardOpened"),"saved opened state survives");}).thenSucceed();
 }
 @AfterBatch(batch="service_plea133") public static void pleaDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_plea133",timeoutTicks=600)
 public static void continuingPhysicalRetreatProducesFurtherSpacedPleasOnce(GameTestHelper h){f=new LiteraryTests.Fixture(h);var place=LabyrinthPlace.LAYERED_MAN;var p=f.player("plea133",place);var b=f.base(place);f.chunks.hold(f.level(place),IndianLakeRooms.bounds(b,place));
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"cellar resident")).thenExecute(()->{var own=f.own(p,place);var shared=f.data().stateEntry(UncannyStories.WORLD,place.id()+":"+b.asLong());f.at(p,place,.5,-5,-19);CellarDialogue.actions(p,b,own,shared);f.at(p,place,.5,-2,-12);CellarDialogue.actions(p,b,own,shared);LiteraryVignettes.save(f.data(),p.getUUID(),place,own);h.assertTrue(own.getInt("RetreatPleaStage133")==1,"first retreat starts the plea");}).thenIdle(1).thenExecute(()->h.assertTrue(NativeTestPlayers.messages(p).stream().anyMatch(x->x.contains("Come back")),"first call reaches the native connection")).thenIdle(47).thenExecute(()->{f.at(p,place,.5,0,-9);var own=f.own(p,place);CellarDialogue.actions(p,b,own,f.data().stateEntry(UncannyStories.WORLD,place.id()+":"+b.asLong()));LiteraryVignettes.save(f.data(),p.getUUID(),place,own);h.assertTrue(own.getInt("RetreatPleaStage133")==2,"further retreat advances once");}).thenIdle(1).thenExecute(()->h.assertTrue(NativeTestPlayers.messages(p).stream().anyMatch(x->x.contains("Please! ...please")),"further desperate plea reaches the native connection")).thenIdle(70).thenExecute(()->{f.at(p,place,.5,0,-5);var own=f.own(p,place);CellarDialogue.actions(p,b,own,f.data().stateEntry(UncannyStories.WORLD,place.id()+":"+b.asLong()));LiteraryVignettes.save(f.data(),p.getUUID(),place,own);h.assertTrue(!own.contains("RetreatPleaStage133"),"final plea stops the sequence");}).thenSucceed();
 }
 @AfterBatch(batch="service_archive133") public static void archiveDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_archive133",timeoutTicks=600)
 public static void namedDrawersHoldTwentyOneFiniteOriginalPapersAndTakenLeavesNeverRestock(GameTestHelper h){
  archive=new NovelTests.Fixture(h,58200,LabyrinthPlace.ZAMPANO_COURTYARD);var a=archive;var l=a.l;var b=a.b;HouseSavedData.get(l.getServer()).markInteriorInitialized();a.chunks.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.ZAMPANO_COURTYARD));var p=NativeTestPlayers.survival(h,"files133");a.players.add(p);p.teleportTo(l,b.getX()+10.1,b.getY()+1,b.getZ()-21.5,0,0);p.hasChangedDimension();p.setNoGravity(true);
  h.startSequence().thenWaitUntil(()->h.assertTrue(a.chunks.ready(),"archive resident")).thenExecute(()->{
   var ids=new HashSet<UUID>();for(int i=0;i<7;i++)for(int y=0;y<3;y++){var drawer=(ServiceFittingEntity)l.getBlockEntity(b.offset(11,y,-22-i*2));var paper=drawer.getItem(0);h.assertTrue(paper.has(DataComponents.WRITTEN_BOOK_CONTENT)&&paper.get(DataComponents.CUSTOM_DATA).copyTag().getString("FiledUnder133").equals(ArchiveReaderFiles.NAMES[i]),"each named drawer contains its actual original");ids.add(paper.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("ArchivePaper133"));}h.assertTrue(ids.size()==21,"twenty-one distinct native originals");var at=b.offset(11,1,-22);var drawer=(ServiceFittingEntity)l.getBlockEntity(at);var paper=drawer.getItem(0);h.assertTrue(paper.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getLast().raw().getString().contains("            landing"),"selected page winds through native text");p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.WEST,at,false));h.assertTrue(p.containerMenu instanceof ChestMenu&&l.getBlockState(at).getValue(ServiceFittingBlock.ACTIVE),"native use pulls the actual drawer");h.assertTrue(!((ChestMenu)p.containerMenu).quickMoveStack(p,0).isEmpty()&&drawer.isEmpty(),"native transfer takes the actual paper");p.closeContainer();drawer.loadWithComponents(drawer.saveWithFullMetadata(l.registryAccess()),l.registryAccess());a.reload();drawer.file(0,1);h.assertTrue(drawer.isEmpty()&&p.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,paper)),"reload and attempted reseeding cannot replace the taken original");
  }).thenSucceed();
 }
 @AfterBatch(batch="service_archive_upgrade133") public static void archiveUpgradeDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_archive_upgrade133",timeoutTicks=600)
 public static void staticFilesUpgradeOnlyUnwatchedPreservingEditedLabelsAndExistingEmptyDrawers(GameTestHelper h){
  archive=new NovelTests.Fixture(h,58600,LabyrinthPlace.ZAMPANO_COURTYARD);var a=archive;var l=a.l;var b=a.b;HouseSavedData.get(l.getServer()).markInteriorInitialized();a.chunks.hold(l,IndianLakeRooms.bounds(b,LabyrinthPlace.ZAMPANO_COURTYARD));var p=NativeTestPlayers.survival(h,"old_files133");a.players.add(p);p.teleportTo(l,b.getX()+10,b.getY(),b.getZ()-21,0,0);p.hasChangedDimension();
  h.startSequence().thenWaitUntil(()->h.assertTrue(a.chunks.ready(),"files resident")).thenExecute(()->{
   var kept=(ServiceFittingEntity)l.getBlockEntity(b.offset(11,1,-22));kept.clearContent();for(int i=0;i<7;i++)for(int y=0;y<3;y++)if(i!=0||y!=1)l.setBlock(b.offset(11,y,-22-i*2),NovelRegistry.PROP.get().defaultBlockState().setValue(NovelPropBlock.KIND,NovelPropBlock.Kind.ARCHIVE_FILES).setValue(NovelPropBlock.FACING,Direction.WEST),18);var sign=(SignBlockEntity)l.getBlockEntity(b.offset(10,2,-34));sign.setText(sign.getFrontText().setMessage(1,net.minecraft.network.chat.Component.literal("Helen’s revised card")),true);h.assertTrue(!ArchiveReaderFiles.drawers(l,b),"the present reader defers repair");p.teleportTo(l,b.getX()+60,b.getY(),b.getZ()+6,0,0);p.hasChangedDimension();for(int i=0;i<8;i++)ArchiveReaderFiles.drawers(l,b);h.assertTrue(l.getBlockEntity(b.offset(11,0,-22)) instanceof ServiceFittingEntity&&l.getBlockEntity(b.offset(11,1,-22))==kept&&kept.isEmpty()&&l.getBlockState(b.offset(11,0,-34)).is(NovelRegistry.PROP.get()),"recognized props upgrade while edited cards and emptied originals remain");a.reload();h.assertTrue(ArchiveReaderFiles.drawers(l,b)&&kept.isEmpty(),"native saved checkpoint survives");
  }).thenSucceed();
 }
 @AfterBatch(batch="service_appliances133") public static void appliancesDone(ServerLevel l){close();}
 @GameTest(template="empty",batch="service_appliances133",timeoutTicks=900)
 public static void nativeBathroomKitchenControlsAndVacantUpgradePreserveTheOriginalCafe(GameTestHelper h){var p=diner(h,"controls133");var l=f.out;var b=f.base(D);var edited=b.offset(15,1,-24);ItemStack[] meal={null};UUID[] actor={null};BlockEntity[] till={null};
  h.startSequence().thenWaitUntil(()->h.assertTrue(f.chunks.ready(),"actual café resident")).thenExecute(()->{
   for(var rel:List.of(new BlockPos(13,0,-27),new BlockPos(14,0,-24),new BlockPos(11,1,-25),new BlockPos(14,1,-25),new BlockPos(4,1,-31),new BlockPos(6,1,-31),new BlockPos(8,1,-31))){var at=b.offset(rel);p.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.7);p.gameMode.useItemOn(p,l,ItemStack.EMPTY,InteractionHand.MAIN_HAND,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false));h.assertTrue(l.getBlockState(at).getValue(ServiceFittingBlock.ACTIVE),"native input operates each actual bathroom/kitchen control");}
   h.assertTrue(l.getBlockState(b.offset(DinerDetails.BATH_DOOR)).getBlock() instanceof DoorBlock&&l.getBlockState(b.offset(12,-1,-24)).is(UncannyRegistry.material("bathroom_floor"))&&l.getBlockState(b.offset(DinerDetails.ROACH)).getCollisionShape(l,b.offset(DinerDetails.ROACH)).isEmpty()&&!ServiceFittingGeometry.parts(ServiceFittingBlock.Kind.ROACH,false,0).equals(ServiceFittingGeometry.parts(ServiceFittingBlock.Kind.ROACH,false,6)),"textured bathroom has a real door and the small roach animates without obstructing the floor");
  }).thenIdle(130).thenExecute(()->{
   for(var rel:List.of(new BlockPos(13,0,-27),new BlockPos(14,0,-24),new BlockPos(11,1,-25),new BlockPos(6,1,-31),new BlockPos(8,1,-31)))h.assertTrue(!l.getBlockState(b.offset(rel)).getValue(ServiceFittingBlock.ACTIVE),"native scheduled controls finish their cycle");
   UncannyRooms0132.build(l,b,D);var own=step(p);actor[0]=own.getUUID("CastFake");var original=ItemStack.parseOptional(p.registryAccess(),own.getCompound("OriginalMeal"));CustomData.update(DataComponents.CUSTOM_DATA,original,t->t.putInt("MealBites133",3));own.remove("OriginalMeal");LiteraryVignettes.save(f.data(),p.getUUID(),D,own);DinerPlate.place(l,b.offset(5,1,-19),original);meal[0]=DinerPlate.at(l,b.offset(5,1,-19)).meal.copy();till[0]=l.getBlockEntity(b.offset(UncannyRooms.source(D)));l.setBlock(edited,Blocks.GOLD_BLOCK.defaultBlockState(),18);f.at(p,D,0,0,-4);h.assertTrue(!DinerDetails.repair(l,b),"present reader defers a saved-room upgrade");f.at(p,D,60,0,6);
  }).thenWaitUntil(()->h.assertTrue(DinerDetails.repair(l,b),"bounded recognized upgrade completes")).thenExecute(()->{
   h.assertTrue(l.getBlockEntity(b.offset(UncannyRooms.source(D)))==till[0]&&l.getBlockState(edited).is(Blocks.GOLD_BLOCK)&&ItemStack.matches(DinerPlate.at(l,b.offset(5,1,-19)).meal,meal[0])&&f.own(p,D).getUUID("CastFake").equals(actor[0])&&l.isEmptyBlock(b.offset(UncannyRooms.account(D))),"original till, serving, actor, edits and narrative custody survive the refit");f.reload();h.assertTrue(DinerDetails.repair(l,b)&&ItemStack.matches(DinerPlate.at(l,b.offset(5,1,-19)).meal,meal[0]),"reload never replenishes a serving");
  }).thenSucceed();
 }
}
