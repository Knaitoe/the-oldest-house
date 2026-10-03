package io.github.knaitoe.theoldesthouse.gametest;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.gametest.framework.AfterBatch;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MarginaliaTests {
    static final class Fixture implements AutoCloseable {
        final GameTestHelper h; final ServerLevel level; final BlockPos origin,base;
        final HouseSavedData oldHouse; final LabyrinthData oldData;
        final List<ServerPlayer> players=new ArrayList<>();
        final boolean legacy;
        Fixture(GameTestHelper h,int coordinate) {this(h,coordinate,true);}
        Fixture(GameTestHelper h,int coordinate,boolean legacy) {
            this.legacy=legacy;
            this.h=h; level=HouseTestLevel.get(h.getLevel().getServer()); origin=new BlockPos(coordinate,80,coordinate);
            var server=level.getServer();oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.JUNCTION);LabyrinthBuilder.buildJunction(level,base);
            HouseFurnishings.decorate(level,base,LabyrinthPlace.JUNCTION);
            // Serial-reader fixtures intentionally provide each thread; production density is tested separately.
            for(var thread:HouseMarginalia.Thread.values()){
                var at=surface(thread);level.setBlock(at.below(),HouseBlocks.HOUSEHOLD_FURNITURE.get().defaultBlockState().setValue(HouseholdFurnitureBlock.KIND,HouseholdFurnitureBlock.Kind.WALNUT_DESK),net.minecraft.world.level.block.Block.UPDATE_CLIENTS|net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
                level.setBlock(at,HouseBlocks.NOTE_SURFACE.get().defaultBlockState().setValue(NoteSurfaceBlock.THREAD,thread),net.minecraft.world.level.block.Block.UPDATE_CLIENTS|net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
            }
            IndianLakeRooms.keepLoaded(level,base,LabyrinthPlace.JUNCTION);
        }
        LabyrinthData data(){return LabyrinthData.get(level.getServer());}
        ServerPlayer player() {var p=h.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            p.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()-11.5,0,0);p.hasChangedDimension();players.add(p);return p;}
        void depth(ServerPlayer p,int depth) {data().clearReturns(p.getUUID());for(int i=0;i<depth;i++)data().pushReturn(p.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,p.position(),0));}
        BlockPos surface(HouseMarginalia.Thread thread) {return base.offset(switch(thread) {
            case HOUSEKEEPING->new BlockPos(4,1,-3);case CALLS->new BlockPos(-3,1,-12);case ROOM->new BlockPos(-2,1,-12);case POEMS->new BlockPos(-1,1,-12);});}
        HouseMarginalia.NotebookMenu open(ServerPlayer p,HouseMarginalia.Thread thread) {
            BlockPos pos=surface(thread);p.moveTo(Vec3.atBottomCenterOf(pos.below().south()));
            // These original-release tests now also exercise upgrade adoption of exact saved pages.
            if(legacy)seedLegacy(p,thread,pos);
            var event=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
            NeoForge.EVENT_BUS.post(event);h.assertTrue(event.isCanceled()&&p.containerMenu instanceof HouseMarginalia.NotebookMenu,"the registered click opens a real private native book menu");
            return (HouseMarginalia.NotebookMenu)p.containerMenu;
        }
        void seedLegacy(ServerPlayer p,HouseMarginalia.Thread thread,BlockPos pos){
            int band=HouseMarginalia.band(data().returnDepth(p.getUUID()));String key=pos.asLong()+":"+thread.getSerializedName()+":"+band;
            var own=HouseMarginalia.record(data(),p.getUUID());var bindings=own.getCompound("Bindings");if(bindings.getCompound(key).contains("Book"))return;
            int due=HouseMarginalia.next(data(),p.getUUID(),thread),available=Math.min(band,thread.chapters-1);
            int chapter=due>available&&available==0?-1:Math.min(due,available);
            var book=chapter<0?MarginaliaTexts.interlude(pos.asLong(),thread):MarginaliaTexts.book(p,thread,chapter);
            var custom=new CompoundTag();custom.putUUID("MarginaliaReader",p.getUUID());custom.putString("MarginaliaBinding",key);
            book.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(custom));
            var entry=new CompoundTag();entry.putInt("Chapter",chapter);entry.put("Book",book.save(p.registryAccess()));bindings.put(key,entry);own.put("Bindings",bindings);
            var state=data().state(HouseMarginalia.ID);state.put(p.getUUID().toString(),own);data().setState(HouseMarginalia.ID,state);
        }
        void end(ServerPlayer p,HouseMarginalia.NotebookMenu menu) {int last=menu.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()-1;
            h.assertTrue(menu.clickMenuButton(p,100+last)&&menu.getPage()==last,"the actual native page button reaches the end");}
        void reload() {var loaded=LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),level.registryAccess()),level.registryAccess());level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);}
        @Override public void close(){for(var p:players)level.getServer().getPlayerList().remove(p);
            for(var e:level.getEntitiesOfClass(Entity.class,IndianLakeRooms.bounds(base,LabyrinthPlace.JUNCTION),e->e instanceof SeatEntity||e instanceof Wolf||e instanceof net.minecraft.world.entity.item.ItemEntity))e.discard();
            var bounds=IndianLakeRooms.bounds(base,LabyrinthPlace.JUNCTION);
            for(int x=((int)bounds.minX-1)>>4;x<=((int)bounds.maxX+1)>>4;x++)for(int z=((int)bounds.minZ-1)>>4;z<=((int)bounds.maxZ+1)>>4;z++)
                level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(x,z),3,base);
            level.getServer().overworld().getDataStorage().set("the_oldest_house",oldHouse);level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);LabyrinthBuilder.clearAll();}
    }
    private static Fixture serial,personal,upgrade,seats;
    @AfterBatch(batch="marginalia_serial") public static void cleanSerial(ServerLevel l){if(serial!=null){serial.close();serial=null;}}
    @AfterBatch(batch="marginalia_personal") public static void cleanPersonal(ServerLevel l){if(personal!=null){personal.close();personal=null;}}
    @AfterBatch(batch="marginalia_upgrade") public static void cleanUpgrade(ServerLevel l){if(upgrade!=null){upgrade.close();upgrade=null;}}
    @AfterBatch(batch="marginalia_seats") public static void cleanSeats(ServerLevel l){if(seats!=null){seats.close();seats=null;}}
    private static String text(ItemStack book) {return book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().map(p->p.raw().getString()).reduce("",(a,b)->a+"\n"+b);}

    @GameTest(template="empty",batch="marginalia_serial",timeoutTicks=100)
    public static void serialWritingWaitsForDepthAndLastPageThenPersistsFiniteOriginalCopies(GameTestHelper h){
        serial=new Fixture(h,29600);var f=serial;var p=f.player();var t=HouseMarginalia.Thread.HOUSEKEEPING;
        h.runAfterDelay(8,()->{
            var first=f.open(p,t);h.assertTrue(!text(first.book()).contains("useless")&&HouseMarginalia.next(f.data(),p.getUUID(),t)==1,"the early list is mundane and starts its own serial");
            var again=f.open(p,t);h.assertTrue(ItemStack.isSameItemSameComponents(first.book(),again.book())&&HouseMarginalia.next(f.data(),p.getUUID(),t)==1,"reopening cannot farm later chapters");
            f.depth(p,6);var second=f.open(p,t);h.assertTrue(text(second.book()).split("You are useless",-1).length==2,"the first hostile encounter contains the sentence only once");
            h.assertTrue(second.clickMenuButton(p,3),"the native Take Book button collects the original chapter");
            ItemStack kept=p.getInventory().items.stream().filter(s->s.has(DataComponents.WRITTEN_BOOK_CONTENT)).findFirst().orElseThrow().copy();
            var repeated=f.open(p,t);h.assertTrue(!repeated.clickMenuButton(p,3)&&p.getInventory().countItem(Items.WRITTEN_BOOK)==1,"one physical surface and band cannot duplicate collected books");
            f.depth(p,8);var third=f.open(p,t);h.assertTrue(third.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==3&&HouseMarginalia.next(f.data(),p.getUUID(),t)==2,"a multi-page finding must be read to its end or collected before advancing");
            h.assertTrue(!third.clickMenuButton(p,999),"forged page numbers cannot claim a completed reading");f.end(p,third);
            f.depth(p,12);var last=f.open(p,t);h.assertTrue(last.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==8,"the final notebook really has eight pages");
            for(var page:last.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages()) h.assertTrue(page.raw().getString().contains("YOU ARE USELESS")&&HouseWriting.CLAW_FONT.equals(page.raw().getStyle().getFont()),"every page uses the stable scratched hand");
            ItemStack notebook=last.book();f.end(p,last);f.reload();var restored=f.open(p,t);
            h.assertTrue(ItemStack.isSameItemSameComponents(notebook,restored.book())&&HouseMarginalia.next(f.data(),p.getUUID(),t)==4,"saved reload preserves the exact discovered text and completed serial");
            h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.isSameItemSameComponents(s,kept)),"earlier collected pages are never rewritten in the inventory");
            h.assertTrue(WitnessAccount.count(f.data(),p.getUUID())==0&&WitnessAccount.Story.values().length==19&&WitnessAccount.REQUIRED==15,"reading scenery cannot alter the Witness pool or grant an ending");h.succeed();
        });
    }
    @GameTest(template="empty",batch="marginalia_personal",timeoutTicks=140)
    public static void twoReadersKeepIndependentPagesAndActualPersonalSnapshots(GameTestHelper h){
        personal=new Fixture(h,29900);var f=personal;var a=f.player();var b=f.player();
        var item=new ItemStack(Items.IRON_AXE);item.set(DataComponents.CUSTOM_NAME,Component.literal("A borrowed winter"));a.getInventory().setItem(4,item);
        h.runAfterDelay(8,()->{
            f.open(a,HouseMarginalia.Thread.ROOM);f.open(b,HouseMarginalia.Thread.ROOM);f.depth(a,6);
            var own=f.open(a,HouseMarginalia.Thread.ROOM);var peer=f.open(b,HouseMarginalia.Thread.ROOM);
            h.assertTrue(text(own.book()).contains("A borrowed winter")&&!text(peer.book()).contains("A borrowed winter"),"only the actual carrier receives their item's name");
            h.assertTrue(own.getPage()==0&&peer.getPage()==0,"each real native menu starts with its own page counter");f.end(a,own);
            h.assertTrue(peer.getPage()==0&&HouseMarginalia.next(f.data(),b.getUUID(),HouseMarginalia.Thread.ROOM)==1,"one reader's last page cannot move a peer or advance their serial");
            item.set(DataComponents.CUSTOM_NAME,Component.literal("Changed later"));var unchanged=f.open(a,HouseMarginalia.Thread.ROOM);
            h.assertTrue(text(unchanged.book()).contains("A borrowed winter")&&!text(unchanged.book()).contains("Changed later"),"an encountered page freezes the observed detail");
            f.depth(a,8);WitnessAccount.resolve(f.data(),a.getUUID(),WitnessAccount.Story.HARRIGAN,"kept_phone");var later=f.open(a,HouseMarginalia.Thread.ROOM);
            h.assertTrue(text(later.book()).contains(a.getGameProfile().getName())&&text(later.book()).contains("1 part of your account"),"later writing uses the reader's actual name and personal recorded evidence");
            a.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);a.closeContainer();BlockPos pos=f.surface(HouseMarginalia.Thread.CALLS);
            a.moveTo(Vec3.atBottomCenterOf(pos.below().south()));NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(a,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));
            h.assertTrue(!(a.containerMenu instanceof HouseMarginalia.NotebookMenu)&&HouseMarginalia.next(f.data(),a.getUUID(),HouseMarginalia.Thread.CALLS)==0,"spectators cannot produce discoveries");h.succeed();
        });
    }
    @GameTest(template="empty",batch="marginalia_upgrade",timeoutTicks=130)
    public static void versionEighteenDecorationPreservesContainersPlayerEditsActorsAndEvidence(GameTestHelper h){
        upgrade=new Fixture(h,30200);var f=upgrade;var data=f.data();LabyrinthLighting.buildEarlyAid(f.level.getServer(),f.level,f.base);
        var cache=(BarrelBlockEntity)f.level.getBlockEntity(f.base.offset(LabyrinthLighting.TOM_CACHE));cache.clearContent();cache.setItem(4,new ItemStack(Items.DIAMOND,3));
        BlockPos hall=LabyrinthPlaces.base(f.origin,LabyrinthPlace.STRAIGHT_HALL);LabyrinthHalls.build(f.level,hall,LabyrinthPlace.STRAIGHT_HALL);
        var fragment=LabyrinthDomestic.fragments(LabyrinthPlace.STRAIGHT_HALL).getFirst();BlockPos changed=hall.offset(fragment.x0(),0,fragment.z0());f.level.setBlock(changed,Blocks.CHEST.defaultBlockState(),3);
        var chest=f.level.getBlockEntity(changed);((net.minecraft.world.Container)chest).setItem(0,new ItemStack(Items.GOLD_INGOT,5));
        BlockPos lamp=hall.offset(0,0,-10);f.level.setBlock(lamp,Blocks.TORCH.defaultBlockState(),3);
        var p=f.player();f.open(p,HouseMarginalia.Thread.POEMS);WitnessAccount.resolve(data,p.getUUID(),WitnessAccount.Story.HARRIGAN,"kept_phone");
        CompoundTag harrigan=data.state(HarriganVignette.ID);harrigan.putBoolean("ReadingFinished",true);data.setState(HarriganVignette.ID,harrigan);
        CompoundTag goat=data.state(GoatmanVignette.ID);goat.putUUID("UnchangedRun",UUID.randomUUID());data.setState(GoatmanVignette.ID,goat);
        var before=HouseMarginalia.record(data,p.getUUID());data.setBuilt(18,f.origin);
        h.assertTrue(!LabyrinthBuilder.ensureBuilt(f.level.getServer()),"the established layout starts a decoration upgrade");while(LabyrinthBuilder.isCarving())LabyrinthBuilder.tick(f.level.getServer());
        h.assertTrue(data.builtVersion()==LabyrinthBuilder.VERSION&&f.level.getBlockEntity(changed)==chest&&((net.minecraft.world.Container)chest).getItem(0).getCount()==5,"older domestic fragments are not reconstructed over player storage");
        h.assertTrue(f.level.getBlockEntity(f.base.offset(LabyrinthLighting.TOM_CACHE))==cache&&cache.getItem(0).isEmpty()&&cache.getItem(4).getCount()==3&&f.level.getBlockState(lamp).is(Blocks.TORCH),"finite caches and placed lights survive the decoration pass");
        h.assertTrue(before.equals(HouseMarginalia.record(data,p.getUUID()))&&WitnessAccount.has(data,p.getUUID(),WitnessAccount.Story.HARRIGAN)
                &&data.state(HarriganVignette.ID).getBoolean("ReadingFinished")&&goat.equals(data.state(GoatmanVignette.ID)),"personal writing, old dialogue, random runs and resolutions remain intact");
        BlockPos cleared=f.surface(HouseMarginalia.Thread.CALLS);f.level.setBlock(cleared,Blocks.AIR.defaultBlockState(),3);HouseFurnishings.upgrade(f.level,f.origin,LabyrinthPlace.JUNCTION);
        h.assertTrue(f.level.getBlockState(cleared).isAir(),"the saved decoration checkpoint does not replenish removed papers");h.succeed();
    }
    @GameTest(template="empty",batch="marginalia_seats",timeoutTicks=130)
    public static void nativeFurnitureHasVoidsAndCorrectSeatedFacingWithoutBlockingHallRoutes(GameTestHelper h){
        seats=new Fixture(h,30500);var f=seats;var p=f.player();BlockPos chair=f.base.offset(-4,0,-9);
        h.runAfterDelay(8,()->{
            p.moveTo(Vec3.atBottomCenterOf(chair.east()));h.assertTrue(HouseSitting.sit(p,chair)&&p.getVehicle() instanceof SeatEntity,"the new upholstered furniture supports the real native seated player");
            h.assertTrue(Math.abs(p.getVehicle().getY()-chair.getY()-.5)<.001&&p.getYRot()==Direction.EAST.toYRot(),"seat height and facing match the model");p.stopRiding();
            for(var facing:Direction.Plane.HORIZONTAL) {
                var table=HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,facing);var shape=table.getCollisionShape(f.level,chair);
                h.assertTrue(shape.toAabbs().stream().noneMatch(b->b.contains(.5,.2,.5)),"the table's underside stays empty instead of colliding as a solid cube");
            }
            BlockPos base=LabyrinthPlaces.base(f.origin,LabyrinthPlace.BENT_HALL);LabyrinthHalls.build(f.level,base,LabyrinthPlace.BENT_HALL);HouseFurnishings.decorate(f.level,base,LabyrinthPlace.BENT_HALL);
            for(var door:LabyrinthPlace.BENT_HALL.doors()) {
                BlockPos step=base.offset(door.rel().relative(door.facing()));h.assertTrue(f.level.getBlockState(step).getCollisionShape(f.level,step).isEmpty()&&f.level.getBlockState(step.above()).getCollisionShape(f.level,step.above()).isEmpty(),"furniture leaves registered door approaches clear");
            }
            var samples=HouseMarginalia.samples(p);h.assertTrue(samples.size()==17&&HouseMarginalia.next(f.data(),p.getUUID(),HouseMarginalia.Thread.POEMS)==0,"all operator previews exist without recording discoveries");
            h.assertTrue(samples.stream().filter(b->text(b).contains("Do not come for me")).count()==3,"the letter's plea develops across three later installments");
            h.assertTrue(samples.stream().anyMatch(b->b.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().anyMatch(page->page.raw().getSiblings().stream().anyMatch(line->line.getStyle().isStrikethrough()))),"the final poem retains its authored crossed-out lines");h.succeed();
        });
    }
}
