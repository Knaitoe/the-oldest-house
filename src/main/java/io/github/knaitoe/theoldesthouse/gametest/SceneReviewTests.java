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
        void at(ServerPlayer p,double x,double y,double z){p.teleportTo(level,base.getX()+x,base.getY()+y,base.getZ()+z,180,0);p.connection.resetPosition();p.setDeltaMovement(Vec3.ZERO);}
        void look(ServerPlayer p,BlockPos rel){p.lookAt(EntityAnchorArgument.Anchor.EYES,base.offset(rel).getCenter());}
        void click(ServerPlayer p,BlockPos rel){at(p,rel.getX()+.5,rel.getY(),rel.getZ()+2.5);look(p,rel);var at=base.offset(rel);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(data(),p.getUUID(),place);}
        void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(data(),p.getUUID(),place,own);}
        void source(ServerPlayer p){click(p,LiteraryRooms.source(place));var menu=(LiteraryVignettes.Pages)p.containerMenu;menu.clickMenuButton(p,100+menu.original().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1);p.closeContainer();}
        void away(ServerPlayer p){p.teleportTo(h.getLevel(),.5,100,.5,0,0);p.connection.resetPosition();}
        void done(){close();h.succeed();}
        @Override public void close(){if(active!=this)return;players.forEach(NativeTestPlayers::remove);LiteraryVignettes.clearAll();NovelVignettes.clearAll();
            var area=SceneReview.area(base,place);for(var e:level.getEntitiesOfClass(Entity.class,area))e.discard();
            if(started){for(var at:BlockPos.betweenClosed(BlockPos.containing(area.minX,area.minY,area.minZ),BlockPos.containing(area.maxX,area.maxY,area.maxZ)))level.setBlock(at,Blocks.AIR.defaultBlockState(),F);
                var store=level.getServer().overworld().getDataStorage();store.set("the_oldest_house",oldHouse);store.set("the_oldest_house_labyrinth",oldData);}chunks.close();active=null;}
    }
    private static void run(GameTestHelper h,LabyrinthPlace p,int coordinate,Consumer<Fixture> check){var f=new Fixture(h,p,coordinate);h.onEachTick(()->{if(f.ready||!f.chunks.ready())return;f.ready=true;f.start();check.accept(f);});}
    private static void cleanup(){if(active!=null)active.close();}
    @AfterBatch(batch="review_books") public static void booksDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_well") public static void wellDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_witch") public static void witchDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_shore") public static void shoreDone(ServerLevel l){cleanup();}
    @AfterBatch(batch="review_archive") public static void archiveDone(ServerLevel l){cleanup();}

    @GameTest(template="empty",batch="review_books",timeoutTicks=1600)
    public static void realShelfQuillAndLaidOriginalRemainPrivateThroughSpeechAndRecovery(GameTestHelper h){run(h,LabyrinthPlace.CONFESSION,552000,f->{
        LiteraryRooms.build(f.level,f.base,f.place);SceneReview.fresh(f.level,f.origin,f.place);
        var owner=f.player("review_book_owner");var peer=f.player("review_book_peer");var spectator=f.player("review_book_spectator");spectator.setGameMode(GameType.SPECTATOR);
        f.at(owner,.5,0,-3);LiteraryVignettes.onArrive(owner,f.place);f.source(owner);
        f.click(owner,SceneReview.BLANK_SHELF);f.click(owner,SceneReview.BLANK_SHELF);h.assertTrue(owner.getInventory().countItem(Items.WRITABLE_BOOK)==1,"the real shelf yields one finite personal blank quill");
        f.click(spectator,SceneReview.BLANK_SHELF);h.assertTrue(spectator.getInventory().countItem(Items.WRITABLE_BOOK)==0,"a spectator cannot supply a journal");
        var quill=owner.getInventory().removeItemNoUpdate(0);quill.set(DataComponents.CUSTOM_NAME,Component.literal("Original shelf quill"));peer.setItemInHand(InteractionHand.MAIN_HAND,quill);f.away(owner);
        f.at(peer,1.5,0,-18);LiteraryVignettes.onArrive(peer,f.place);f.source(peer);f.at(peer,1.5,0,-18);f.look(peer,new BlockPos(1,1,-22));
        h.runAfterDelay(50,()->{
            h.assertTrue(!f.own(peer).hasUUID("Journal")&&f.own(peer).getInt("Chapter")==0,"carrying another reader's actual quill cannot start a transcript");
            var original=peer.getMainHandItem().copy();peer.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);f.away(peer);
            owner.setItemInHand(InteractionHand.MAIN_HAND,original);f.click(owner,SceneReview.BOOK_TRAY);var laid=ConfessionBooks.tableBook(owner,f.own(owner));h.assertTrue(laid!=null&&owner.getMainHandItem().isEmpty(),"the native original actually leaves the hand and rests on the shared table");var id=laid.getUUID();
            spectator.setGameMode(GameType.SURVIVAL);spectator.setShiftKeyDown(true);f.click(spectator,SceneReview.BOOK_TRAY);h.assertTrue(laid.isAlive()&&spectator.getInventory().countItem(Items.WRITABLE_BOOK)==0,"another reader cannot recover the owner's table original");f.away(spectator);
            f.at(owner,1.5,0,-18);f.look(owner,new BlockPos(1,1,-22));
            h.runAfterDelay(70,()->{
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
        h.runAfterDelay(30,()->{
            var own=NovelVignettes.personal(f.data(),owner.getUUID());h.assertTrue(NovelVignettes.coveredWait(owner,own)&&!NovelVignettes.coveredWait(peer,NovelVignettes.personal(f.data(),peer.getUUID()))&&!NovelVignettes.coveredWait(observer,NovelVignettes.personal(f.data(),observer.getUUID())),"the native covered wait selects only its actual living reader");
            var lid=f.level.getBlockState(f.base.offset(NovelRooms.WELL));h.assertTrue(!lid.getValue(TrapDoorBlock.OPEN)&&f.level.getBlockState(f.base.offset(0,2,-24)).is(HouseBlocks.VIGNETTE_DETAIL.get()),"one closed native cover owns the silhouette on the real rim");
            own.putInt("WellTicks",NovelVignettes.WELL_WAIT-1);NovelVignettes.save(f.data(),owner.getUUID(),own);f.at(peer,.5,-12,-22.5);
        });
        h.runAfterDelay(50,()->{
            h.assertTrue(!NovelVignettes.coveredWait(owner,NovelVignettes.personal(f.data(),owner.getUUID()))&&NovelVignettes.coveredWait(peer,NovelVignettes.personal(f.data(),peer.getUUID()))&&!f.level.getBlockState(f.base.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN),"a peer's physical vigil retains the shared cover after the first reader finishes");
            var own=NovelVignettes.personal(f.data(),peer.getUUID());own.putInt("WellTicks",NovelVignettes.WELL_WAIT-1);NovelVignettes.save(f.data(),peer.getUUID(),own);
        });
        h.runAfterDelay(70,()->{
            h.assertTrue(f.level.getBlockState(f.base.offset(NovelRooms.WELL)).getValue(TrapDoorBlock.OPEN)&&f.level.getBlockState(f.base.offset(0,2,-24)).isAir(),"the native shaft really opens and the only shadow disappears");
            owner.move(MoverType.SELF,new Vec3(0,13,0));
        });
        h.runAfterDelay(85,()->{
            h.assertTrue(WitnessAccount.has(f.data(),owner.getUUID(),WitnessAccount.Story.BARN_WELL)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.BARN_WELL)&&!WitnessAccount.has(f.data(),observer.getUUID(),WitnessAccount.Story.BARN_WELL),"only actual native ascent resolves the well; waiting/observing a peer never does");
            h.assertTrue(SceneClock.time(3,900,6000)==12500&&f.level.getBlockState(f.base.offset(NovelRooms.CARVING)).is(NovelRegistry.CARVINGS.get()),"dusk and new initials art retain the original interaction block");f.done();
        });
    });}

    @GameTest(template="empty",batch="review_witch",timeoutTicks=1600)
    public static void oneNativeCostumeWitchStrikesRecoilAndReloadPreserveIdentityAndPeerSafety(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,553000,f->{
        for(int x=-6;x<=7;x++)for(int z=-47;z<=-35;z++)f.put(x,-1,z,Blocks.SANDSTONE);
        var owner=f.player("review_witch_owner");var peer=f.player("review_witch_peer");f.at(owner,2,0,-40.5);f.at(peer,5,0,-40.5);owner.getFoodData().setFoodLevel(0);peer.getFoodData().setFoodLevel(0);
        var witch=LiteraryVignettes.huntBody(owner,f.place,new BlockPos(0,0,-41));h.assertTrue(witch!=null&&!witch.isInvulnerable()&&LiteraryVignettes.huntBody(peer,f.place,new BlockPos(0,0,-41))==witch,"both readers share the same native, woundable actor");var id=witch.getUUID();float peerHealth=peer.getHealth();var before=f.data().state(DrownedTown.ID);boolean[] strike={false};
        h.onEachTick(()->{if(active==f&&witch.striking())strike[0]=true;});
        h.runAfterDelay(22,()->{
            h.assertTrue(strike[0]&&owner.getHealth()<20&&peer.getHealth()==peerHealth,"one actual lunge has a synced attack state and hurts only its nearest vulnerable reader");
            var health=witch.getHealth();owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));owner.attack(witch);
            h.assertTrue(witch.getHealth()<health&&witch.hurtTime>0&&witch.huntPhase()==LakeWitchEntity.WITHDRAW&&!witch.striking(),"a real native sword wound cancels the strike and exposes recoil");
            float wounded=witch.getHealth();var tag=new CompoundTag();witch.saveWithoutId(tag);witch.load(tag);
            h.assertTrue(witch.getUUID().equals(id)&&witch.getHealth()==wounded&&f.level.getEntity(id)==witch&&!witch.isInvulnerable(),"native save/reload keeps the same wounded original");
            for(int x=4;x<=7;x++)for(int z=-44;z<=-38;z++)f.put(x,-1,z,Blocks.GRASS_BLOCK);
            f.at(owner,5.5,0,-41.5);f.at(peer,6.5,0,-42.5);float a=owner.getHealth(),b=peer.getHealth();
            h.runAfterDelay(45,()->{h.assertTrue(owner.getHealth()==a&&peer.getHealth()==b&&f.data().state(DrownedTown.ID).equals(before)&&WitnessAccount.count(f.data(),peer.getUUID())==0,"real living grass protects both readers and the literary wound cannot alter the town's shared state or grant evidence");f.done();});
        });
    });}

    @GameTest(template="empty",batch="review_shore",timeoutTicks=1600)
    public static void nativeCostumeBayAndSwimmingStepsRetainResidentsAndOriginalStand(GameTestHelper h){run(h,LabyrinthPlace.COSTUME_NIGHT,553500,f->{
        for(int x=-28;x<=28;x++)for(int z=-58;z<=-35;z++)for(int y=-5;y<=-1;y++)f.put(x,y,z,z<=-55?(y==-1?Blocks.GRASS_BLOCK:Blocks.DIRT):z<=-49?Blocks.WATER:y==-1?Blocks.SAND:Blocks.SANDSTONE);
        var owner=f.player("review_shore_owner");var peer=f.player("review_shore_peer");
        var cat=EntityType.CAT.create(f.level);cat.setNoAi(true);cat.setNoGravity(true);cat.setTame(true,false);cat.setOwnerUUID(owner.getUUID());cat.setOrderedToSit(true);cat.moveTo(Vec3.atBottomCenterOf(f.base.offset(20,0,-40)));f.level.addFreshEntity(cat);var id=cat.getUUID();
        h.assertTrue(!SceneReview.shore(f.level,f.base)&&f.level.getBlockState(f.base.offset(20,-1,-40)).is(Blocks.SAND)&&cat.getUUID().equals(id),"the new bay cannot remove the real sitting companion's support");cat.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-40)));
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
        for(int x:new int[]{-8,8})h.assertTrue(f.level.getBlockState(f.base.offset(x,0,-10)).getValue(HouseholdFurnitureBlock.FACING)==Direction.NORTH&&f.level.getBlockState(f.base.offset(x,0,-12)).getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.READING_DESK&&f.level.getBlockState(f.base.offset(x,1,-12)).getValue(VignetteDetailBlock.KIND)==VignetteDetailBlock.Kind.CHESS,"actual courtyard chairs face their unique desks and chess boards");
        var owner=f.player("review_archive_owner");var peer=f.player("review_archive_peer");f.click(owner,SceneReview.DRAFTS.get(0));var menu=(NovelVignettes.NovelBookMenu)owner.containerMenu;var text=menu.book().copy();
        h.assertTrue(!menu.clickMenuButton(peer,3)&&menu.clickMenuButton(owner,3)&&!menu.clickMenuButton(owner,3),"the native draft menu yields its original once to its actual reader");owner.closeContainer();
        var saved=f.data().save(new CompoundTag(),f.level.registryAccess());f.level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.FACTORY.deserializer().apply(saved,f.level.registryAccess()));f.click(owner,SceneReview.DRAFTS.get(0));
        h.assertTrue(ItemStack.isSameItemSameComponents(((NovelVignettes.NovelBookMenu)owner.containerMenu).book(),text)&&!((NovelVignettes.NovelBookMenu)owner.containerMenu).clickMenuButton(owner,3),"native reload and rereading retain the same finite words/components");
        h.assertTrue(f.level.getBlockEntity(f.base.offset(originalAt))==cache&&cache.getItem(0).isEmpty()&&ItemStack.isSameItemSameComponents(cache.getItem(4),original)&&WitnessAccount.count(f.data(),owner.getUUID())==0&&WitnessAccount.count(f.data(),peer.getUUID())==0,"new scenery and drafts never restock the old container or grant a story resolution");f.done();
    });}
}
