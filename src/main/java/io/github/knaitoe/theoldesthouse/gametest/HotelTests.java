package io.github.knaitoe.theoldesthouse.gametest;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(TheOldestHouse.MOD_ID+"_hotel")
@PrefixGameTestTemplate(false)
public final class HotelTests {
    private static Fixture fittings;
    private static NativeTestChunks fittingChunks;
    @AfterBatch(batch="hotel_fittings04112") public static void fittingsDone(ServerLevel level){if(fittings!=null){fittings.close();fittings=null;}if(fittingChunks!=null){fittingChunks.close();fittingChunks=null;}}
    @GameTest(template="empty",batch="hotel_fittings04112",timeoutTicks=400)
    public static void flatPhotographTracksItsSitterAndTheBarAndBathUseTheirActualNativeBodies(GameTestHelper h){
        fittings=new Fixture(h,53100,true);var f=fittings;var p=f.player("hotel_fittings");HotelVignette.onArrive(p,LabyrinthPlace.HOTEL);var chunks=fittingChunks=new NativeTestChunks();chunks.hold(f.l,IndianLakeRooms.bounds(f.b,LabyrinthPlace.HOTEL));
        h.startSequence().thenWaitUntil(()->h.assertTrue(chunks.ready()&&HotelVignette.world(f.data()).hasUUID("BathGhost"),"the original bar and bathroom cast are loaded")).thenExecute(()->{
            var all=HotelVignette.world(f.data());var bar=(HotelActor)f.l.getEntity(all.getUUID("Actor1"));var ghost=(HotelActor)f.l.getEntity(all.getUUID("BathGhost"));
            f.at(p,20.5,0,-7.5);h.assertTrue(p.getEyePosition().distanceToSqr(bar.getBoundingBox().getCenter())<9&&f.l.noCollision(bar,bar.getBoundingBox()),"the original bartender fits behind the counter within natural native interaction reach");
            HotelVignette.talk(p,bar);int line=f.own(p).getInt("BarLine04112");var own=f.own(p);own.putLong("LastDrink",f.l.getGameTime()-41);HotelVignette.save(f.data(),p.getUUID(),own);HotelVignette.talk(p,bar);h.assertTrue(f.own(p).getInt("BarLine04112")==line+1,"subsequent service advances the reader's dialogue without bypassing service cooldown");
            var saved=ghost.saveWithoutId(new CompoundTag());var restored=HotelRegistry.ACTOR.get().create(f.l);restored.load(saved);h.assertTrue(ghost.role()==6&&restored.role()==6&&restored.getUUID().equals(ghost.getUUID())&&ghost.isNoGravity()&&ghost.position().distanceToSqr(new Vec3(f.b.getX()-26.5,f.b.getY()+6.45,f.b.getZ()-24.1))<.01,"the old woman occupies the bath and retains her saved original identity and appearance");
            f.at(p,.5,0,-27.5);HotelVignette.photograph(p);var tile=(HotelPhotoBlockEntity)f.l.getBlockEntity(f.b.offset(HotelRooms.PHOTO));var fresh=new HotelPhotoBlockEntity(tile.getBlockPos(),tile.getBlockState());fresh.loadWithComponents(tile.getUpdateTag(f.l.registryAccess()),f.l.registryAccess());h.assertTrue(fresh.sitter()!=null&&fresh.sitter().equals(tile.sitter())&&fresh.sitter().owner().equals(p.getUUID()),"native disk and late-tracking tile data retain the real flat sitter profile");
            h.assertTrue(!f.l.getBlockState(f.b.offset(HotelRooms.FRONT_ROW)).is(Blocks.PLAYER_WALL_HEAD)&&p.getInventory().countItem(HotelRegistry.PHOTOGRAPH.get())==1,"one personally owned print is issued and no three-dimensional head protrudes from the wall");
            chunks.close();h.succeed();
        });
    }
    private static Fixture arc,custody,plant,blind,history,stanleyItems,stanleyDance,routes;
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;final ServerLevel l,outside;final BlockPos origin,b,gb;final HouseSavedData oldHouse;final LabyrinthData oldData;final MotherCollection oldMother;final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,int x,boolean hotel){this.h=h;l=HouseTestLevel.get(h.getLevel().getServer(),HouseDimensions.INTERIOR);outside=HouseTestLevel.get(l.getServer(),HouseDimensions.OUTSIDE);oldHouse=HouseSavedData.get(l.getServer());oldData=LabyrinthData.get(l.getServer());oldMother=MotherCollection.get(l.getServer());origin=new BlockPos(x,80,x);var house=new HouseSavedData();house.markSpawned(origin);l.getServer().overworld().getDataStorage().set("the_oldest_house",house);l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());l.getServer().overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());b=LabyrinthPlaces.base(origin,hotel?LabyrinthPlace.HOTEL:LabyrinthPlace.BLIND_STRETCH);gb=LabyrinthPlaces.base(origin,LabyrinthPlace.HOTEL_GROUNDS);if(hotel){HotelRooms.build(l,b,LabyrinthPlace.HOTEL);HotelRooms.build(outside,gb,LabyrinthPlace.HOTEL_GROUNDS);LabyrinthBuilder.registerDoors(data(),LabyrinthPlace.HOTEL,b);LabyrinthBuilder.registerDoors(data(),LabyrinthPlace.HOTEL_GROUNDS,gb);}else{BlindStretch.build(l,b);LabyrinthBuilder.registerDoors(data(),LabyrinthPlace.BLIND_STRETCH,b);}data().setBuilt(LabyrinthBuilder.VERSION,origin);}
        LabyrinthData data(){return LabyrinthData.get(l.getServer());}CompoundTag own(ServerPlayer p){return HotelVignette.personal(data(),p.getUUID());}
        ServerPlayer player(String name){var p=NativeTestPlayers.survival(h,name);p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-3,180,0);p.hasChangedDimension();players.add(p);return p;}
        void at(ServerPlayer p,double x,double y,double z){p.stopRiding();p.moveTo(b.getX()+x,b.getY()+y,b.getZ()+z);p.setDeltaMovement(Vec3.ZERO);}
        void click(ServerPlayer p,BlockPos rel){var at=b.offset(rel);p.stopRiding();p.moveTo(at.getX()+.5,at.getY(),at.getZ()+2.5);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.SOUTH,at,false)));}
        void read(ServerPlayer p,BlockPos rel){click(p,rel);h.assertTrue(p.containerMenu instanceof HotelVignette.Pages,"hotel surface opens the native private reader");var menu=(HotelVignette.Pages)p.containerMenu;int pages=menu.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();h.assertTrue(menu.clickMenuButton(p,100+pages-1),"the actual last native page is read");p.closeContainer();}
        /** The guest goes out of the hotel for good and comes back: another night. */
        void night(ServerPlayer p){var own=own(p);own.remove("Here");HotelVignette.save(data(),p.getUUID(),own);HotelVignette.onArrive(p,LabyrinthPlace.HOTEL);}
        /** The desk bell, struck where it actually stands on the reception prop (the desk faces east). */
        void bell(ServerPlayer p){var at=b.offset(HotelRooms.DESK);at(p,-5,0,-5);var top=Vec3.atLowerCornerOf(at).add(.625,1.25,.75);NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(top,Direction.UP,at,false)));}
        void last(ServerPlayer p){h.assertTrue(p.containerMenu instanceof HotelVignette.Pages,"the native private reader is open");var menu=(HotelVignette.Pages)p.containerMenu;int pages=menu.book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size();h.assertTrue(menu.clickMenuButton(p,100+pages-1),"the actual last native page is read");p.closeContainer();}
        String story(ServerPlayer p){return WitnessAccount.record(data(),p.getUUID()).getCompound("Stories").getString("hotel");}
        void chest(ServerPlayer p){p.closeContainer();p.openMenu(new SimpleMenuProvider((id,inv,who)->ChestMenu.threeRows(id,inv,new SimpleContainer(27)),Component.literal("An ordinary chest")));}
        void torch(ServerPlayer p,BlockPos rel,boolean canceled){var at=b.offset(rel);l.setBlock(at,Blocks.TORCH.defaultBlockState(),3);var event=new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(net.neoforged.neoforge.common.util.BlockSnapshot.create(l.dimension(),l,at),Blocks.DARK_OAK_PLANKS.defaultBlockState(),p);if(canceled)event.setCanceled(true);NeoForge.EVENT_BUS.post(event);h.assertTrue(event.isCanceled()==canceled,"native protection admits only the successful 217 torch placement");if(canceled)l.setBlock(at,Blocks.AIR.defaultBlockState(),3);}
        void reload(){var loaded=LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(),l.registryAccess()),l.registryAccess());l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);}
        @Override public void close(){for(var p:players)NativeTestPlayers.remove(p);for(var level:List.of(l,outside)){var entities=new ArrayList<Entity>();for(var e:level.getAllEntities())if(e.position().distanceToSqr(level==l?b.getCenter():gb.getCenter())<15000)entities.add(e);entities.forEach(Entity::discard);}l.getServer().overworld().getDataStorage().set("the_oldest_house",oldHouse);l.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);l.getServer().overworld().getDataStorage().set("the_oldest_house_mother",oldMother);}
    }
    @GameTest(template="empty",batch="hotel_arc",timeoutTicks=400)
    public static void aGuestSettlesTheirOwnAccountOnTheLastNightAndAPeerGetsNothing(GameTestHelper h){arc=new Fixture(h,48000,true);var f=arc;var p=f.player("hotel_guest");var peer=f.player("hotel_peer");f.at(peer,35,0,-4);
        HotelVignette.onArrive(p,LabyrinthPlace.HOTEL);h.assertTrue(f.own(p).getInt("Visits")==1,"coming in from outside is the first night");
        HotelVignette.onArrive(p,LabyrinthPlace.HOTEL_GROUNDS);HotelVignette.onArrive(p,LabyrinthPlace.HOTEL);h.assertTrue(f.own(p).getInt("Visits")==1,"out to the grounds and back is the same night");
        var spawn=p.getRespawnPosition();var seat=f.b.offset(HotelRooms.TABLE.getX(),0,HotelRooms.TABLE.getZ()+1);f.at(p,HotelRooms.TABLE.getX()+.5,0,HotelRooms.TABLE.getZ()+1.5);h.assertTrue(HouseSitting.sit(p,seat),"the chair at the table for two takes an actual seated guest");
        h.runAfterDelay(10,()->{h.assertTrue(f.own(p).getBoolean("MealServed")&&!f.own(p).getBoolean("Dinner"),"sitting serves dinner without silently eating or charging it");
            f.click(p,HotelRooms.TABLE);h.assertTrue(f.own(p).getBoolean("Dinner")&&f.own(p).getInt("Debt")==1&&f.own(p).getBoolean("Charged"),"actually eating it puts one place on the account");
            h.assertTrue(StanleyBlock.is(f.l.getBlockState(f.b.offset(HotelRooms.TABLE)),StanleyBlock.Kind.TABLE_SET)&&f.l.getBlockState(f.b.offset(HotelRooms.TABLE)).getValue(StanleyBlock.STAGE)==StanleyBlock.LAID,"the shared table stays laid; only the guest's own view of it is served and cleared");
            f.read(p,HotelRooms.HOUSEKEEPING);f.at(p,HotelRooms.BED.getX()+.5,6,HotelRooms.BED.getZ()-.5);h.assertTrue(HotelVignette.rest(p)&&p.isSleeping(),"217 accepts actual native sleep without native respawn registration");});
        h.runAfterDelay(105,()->{h.assertTrue(f.own(p).getBoolean("Rested")&&!p.isSleeping()&&Objects.equals(spawn,p.getRespawnPosition()),"an actual completed rest preserves the native spawn");
            f.at(p,19.5,0,-8.5);p.setHealth(12);var drink=new ItemStack(HotelRegistry.DRINK.get());drink.getItem().finishUsingItem(drink,f.l,p);h.assertTrue(drink.isEmpty()&&p.getHealth()==16&&f.own(p).getInt("Drinks")==1&&f.own(p).getInt("Debt")==2,"finishing the actual native drink in the bar heals and goes on the account");
            f.at(p,.5,0,-27.5);HotelVignette.photograph(p);h.assertTrue(f.own(p).getBoolean("Photographed")&&p.getInventory().countItem(HotelRegistry.PHOTOGRAPH.get())==1,"standing on the actual ballroom floor puts the guest in the photograph once");
            h.assertTrue(f.l.getBlockState(f.b.offset(HotelRooms.PHOTO)).is(HotelRegistry.PROP.get())&&f.l.getBlockEntity(f.b.offset(HotelRooms.PHOTO)) instanceof HotelPhotoBlockEntity photo&&photo.sitter()!=null&&photo.sitter().owner().equals(p.getUUID())&&!f.l.getBlockState(f.b.offset(HotelRooms.FRONT_ROW)).is(Blocks.PLAYER_WALL_HEAD),"the native flat print retains the actual sitter profile without a protruding head");f.read(p,HotelRooms.LOG);
            p.teleportTo(f.outside,f.gb.getX()+.5,f.gb.getY(),f.gb.getZ()-38,180,0);p.hasChangedDimension();HotelVignette.onArrive(p,LabyrinthPlace.HOTEL_GROUNDS);var at=f.gb.offset(HotelRooms.KEY.below());NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,new BlockHitResult(at.getCenter(),Direction.UP,at,false)));h.assertTrue(f.own(p).getBoolean("MasterKey")&&p.getInventory().countItem(HotelRegistry.MASTER_KEY.get())==1,"the actual maze pedestal gives a finite personally found key");
            p.teleportTo(f.l,f.b.getX()+.5,f.b.getY(),f.b.getZ()-3,180,0);p.hasChangedDimension();HotelVignette.onArrive(p,LabyrinthPlace.HOTEL);h.assertTrue(f.own(p).getInt("Visits")==1&&!HotelVignette.due(f.own(p)),"the same stay is still the first night");
            f.click(p,HotelRooms.DESK);f.last(p);f.bell(p);h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOTEL)&&f.own(p).getInt("Debt")==2&&!f.own(p).getBoolean("Settled"),"before the last night the account is only read and the bell only rung");
        });
        h.runAfterDelay(140,()->{f.night(p);f.night(p);h.assertTrue(f.own(p).getInt("Visits")==3&&HotelVignette.due(f.own(p))&&HotelVignette.snow(f.own(p))==2,"the third night's account is due and this guest's snow stands above the latch");
            for(var w:HotelRooms.WINDOWS)h.assertTrue(StanleyBlock.is(f.l.getBlockState(f.b.offset(w.low())),StanleyBlock.Kind.WINDOW_LOW)&&f.l.getBlockState(f.b.offset(w.low())).getValue(StanleyBlock.STAGE)==0&&StanleyBlock.is(f.l.getBlockState(f.b.offset(w.low()).above()),StanleyBlock.Kind.WINDOW_HIGH),"the shared window keeps its own sill snow; each guest's height is theirs: "+w);
            p.getInventory().add(new ItemStack(Items.DIAMOND,3));f.bell(p);h.assertTrue(f.own(p).getBoolean("Settled")&&f.own(p).getInt("Debt")==0&&f.own(p).getList("Paid",Tag.TAG_COMPOUND).size()==2&&p.getInventory().countItem(Items.DIAMOND)==1,"ringing on the last night settles the account in kind, one thing for each place");
            h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOTEL),"settling readies the account; reading it closes it");f.last(p);
            h.assertTrue(WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOTEL)&&f.story(p).equals(HotelVignette.SETTLED)&&HotelVignette.closed(f.own(p))&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.HOTEL),"the settled account read through closes it for the guest alone");
            var drink=new ItemStack(HotelRegistry.DRINK.get());f.at(p,19.5,0,-8.5);drink.getItem().finishUsingItem(drink,f.l,p);h.assertTrue(f.own(p).getInt("Debt")==0,"once the account is closed the house asks nothing more");
            f.click(p,HotelRooms.DESK);var menu=(HotelVignette.Pages)p.containerMenu;var original=menu.book();p.closeContainer();f.at(peer,-5.5,0,-5.5);HotelVignette.readCollected(peer,original);var borrowed=(HotelVignette.Pages)peer.containerMenu;h.assertTrue(borrowed.clickMenuButton(peer,101)&&!WitnessAccount.has(f.data(),peer.getUUID(),WitnessAccount.Story.HOTEL),"a borrowed account is readable without its credit");
            peer.setGameMode(GameType.SPECTATOR);h.assertTrue(!HotelVignette.inside(peer,LabyrinthPlace.HOTEL),"native observer mode excludes participation");f.reload();h.assertTrue(f.story(p).equals(HotelVignette.SETTLED)&&HotelVignette.closed(f.own(p))&&f.own(p).getList("Paid",Tag.TAG_COMPOUND).size()==2,"the closed account, its payment and the Witness record survive native SavedData reload");h.succeed();});
    }
    @GameTest(template="empty",batch="hotel_sign",timeoutTicks=200)
    public static void theRegistersSecondLineTakesOnlyAGuestInThePhotographOnTheLastNight(GameTestHelper h){var f=new Fixture(h,48300,true);try{var p=f.player("hotel_signer");HotelVignette.onArrive(p,LabyrinthPlace.HOTEL);
        f.read(p,HotelRooms.REGISTER);h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOTEL),"before the last night the register is only the party's names");
        f.night(p);f.night(p);f.read(p,HotelRooms.REGISTER);h.assertTrue(!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOTEL)&&!f.own(p).getBoolean("Signed"),"the second line is for someone in the photograph");
        f.at(p,.5,0,-27.5);HotelVignette.photograph(p);f.read(p,HotelRooms.REGISTER);
        h.assertTrue(f.story(p).equals(HotelVignette.SIGNED)&&f.own(p).getBoolean("Signed")&&HotelVignette.ending(f.own(p)).equals("signed"),"signing the second line on the last night is the guest's ending");
        f.click(p,HotelRooms.LOG);var log=((HotelVignette.Pages)p.containerMenu).book().get(DataComponents.WRITTEN_BOOK_CONTENT).pages();p.closeContainer();h.assertTrue(log.get(log.size()-1).raw().getString().contains(p.getGameProfile().getName()),"the caretaker's log goes on in the guest's name");
        h.succeed();}finally{f.close();}}
    @GameTest(template="empty",batch="hotel_walk",timeoutTicks=200)
    public static void goingOutByTheStaffDoorOnTheLastNightLeavesTheHotelWhatItHolds(GameTestHelper h){var f=new Fixture(h,48700,true);try{var p=f.player("hotel_leaver");var peer=f.player("hotel_stayer");HotelVignette.onArrive(p,LabyrinthPlace.HOTEL);HotelVignette.onArrive(peer,LabyrinthPlace.HOTEL);
        var iron=f.b.offset(HotelRooms.SERVICE_DOOR);f.click(p,HotelRooms.SERVICE_DOOR);h.assertTrue(!f.l.getBlockState(iron).getValue(DoorBlock.OPEN),"the service door stays shut without the master key");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(HotelRegistry.MASTER_KEY.get()));f.click(p,HotelRooms.SERVICE_DOOR);h.assertTrue(f.l.getBlockState(iron).getValue(DoorBlock.OPEN),"the master key opens the actual iron service door");
        var service=f.data().door(LabyrinthPlace.HOTEL.id()+"/service");h.assertTrue(service!=null&&service.lower.equals(f.b.offset(HotelRooms.STAFF_EXIT))&&LabyrinthData.DEALT.equals(service.destination),"the staff entrance is a real way out of the hotel");
        var own=f.own(p);own.putBoolean("MasterKey",true);var row=new CompoundTag();row.putInt("Slot",0);row.put("Stack",new ItemStack(Items.EMERALD).save(p.registryAccess()));var list=new ListTag();list.add(row);own.put("LostProperty",list);own.put("Drawers",list.copy());HotelVignette.save(f.data(),p.getUUID(),own);
        HotelVignette.destinationEntry(p,service,service,LabyrinthPlace.HOTEL_HALLWAY);h.assertTrue(!HotelVignette.closed(f.own(p))&&f.own(p).getList("LostProperty",Tag.TAG_COMPOUND).size()==1,"before the last night going out is only leaving");
        f.night(p);f.night(p);f.night(peer);f.night(peer);
        HotelVignette.destinationEntry(peer,service,service,LabyrinthPlace.HOTEL_HALLWAY);h.assertTrue(!HotelVignette.closed(f.own(peer)),"a guest who never found the key has not walked out on anything");
        HotelVignette.destinationEntry(p,service,service,LabyrinthPlace.HOTEL_HALLWAY);
        h.assertTrue(f.story(p).equals(HotelVignette.WALKED)&&HotelVignette.ending(f.own(p)).equals("walked")&&f.own(p).getList("LostProperty",Tag.TAG_COMPOUND).isEmpty()&&f.own(p).getList("Drawers",Tag.TAG_COMPOUND).isEmpty(),"out by the staff door on the last night, the hotel keeps what it held");
        f.reload();h.assertTrue(HotelVignette.closed(f.own(p))&&f.own(p).getList("LostProperty",Tag.TAG_COMPOUND).isEmpty()&&!HotelVignette.closed(f.own(peer)),"the walk-out survives native save reload and belongs to the guest alone");
        h.succeed();}finally{f.close();}}
    @GameTest(template="empty",batch="hotel_property",timeoutTicks=300)
    public static void repeatRestAndTabRecoverExactNamedEnchantedStacksWithoutRefill(GameTestHelper h){
        custody=new Fixture(h,48500,true);
        var f=custody;
        var p=f.player("hotel_property");
        var peer=f.player("hotel_other");
        f.at(peer,35,0,-5);
        f.at(p,HotelRooms.BED.getX()+.5,6,HotelRooms.BED.getZ()-.5);
        var tool=new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponents.CUSTOM_NAME,Component.literal("The tool I kept"));
        tool.setDamageValue(83);
        tool.enchant(p.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),2);
        p.getInventory().setItem(8,tool);
        // Initialize the existing native carry stamp before freezing the custody baseline.
        // Otherwise its periodic tick can legitimately add components between the two rests.
        MotherCollection.rememberCarried(p.getInventory().getItem(8),p.getUUID(),f.l.getGameTime());
        var handoff=p.getInventory().getItem(8).copy();
        var carry=handoff.get(DataComponents.CUSTOM_DATA).copyTag();
        h.assertTrue(carry.hasUUID(MotherCollection.CARRIER)&&carry.getUUID(MotherCollection.CARRIER).equals(p.getUUID())&&carry.contains(MotherCollection.CARRIED,Tag.TAG_LONG),"the exact baseline includes actual native carried-item metadata");
        var first=new BlockPos(-20,6,-17);var second=new BlockPos(-20,6,-19);
        f.torch(p,first,false);
        f.torch(p,first,false);
        f.torch(p,second,false);
        f.torch(p,new BlockPos(-20,6,-20),true);
        f.at(peer,-21.5,6,-19.5);
        f.torch(peer,second,false);
        f.at(peer,35,0,-5);
        h.assertTrue(HotelVignette.rest(p),"first rest begins at the actual bed");
        h.runAfterDelay(95,()->{
            h.assertTrue(f.own(p).getBoolean("Rested")&&p.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,handoff)),"sorting preserves the complete named, damaged, enchanted and carry-stamped item");
            h.assertTrue(p.getInventory().countItem(Items.TORCH)==1&&f.l.getBlockState(f.b.offset(first)).isAir()&&f.l.getBlockState(f.b.offset(second)).is(Blocks.TORCH),"housekeeping returns one original light, excludes canceled placement and leaves a peer's replacement");
            for(int i=0;i<p.getInventory().items.size();i++)if(p.getInventory().items.get(i).is(Items.TORCH))p.getInventory().items.set(i,ItemStack.EMPTY);
            h.assertTrue(HotelVignette.rest(p),"a later deliberate rest stores pockets");
        });
        h.runAfterDelay(190,()->{
            h.assertTrue(p.getInventory().items.stream().allMatch(ItemStack::isEmpty),"a completed second rest empties only main pockets");
            h.assertTrue(f.own(p).getList("Drawers",Tag.TAG_COMPOUND).size()==1&&f.own(peer).getList("Drawers",Tag.TAG_COMPOUND).isEmpty(),"custody belongs only to this guest");
            var beforeReload=f.own(p).getList("Drawers",Tag.TAG_COMPOUND).copy();
            var stored=ItemStack.parseOptional(p.registryAccess(),beforeReload.getCompound(0).getCompound("Stack"));
            h.assertTrue(ItemStack.matches(stored,handoff),"drawer custody stores every component of the actual handoff item");
            f.reload();
            h.assertTrue(beforeReload.equals(f.own(p).getList("Drawers",Tag.TAG_COMPOUND)),"SavedData reload keeps every property byte");
            HotelVignette.openProperty(p,true);
            var menu=(ChestMenu)p.containerMenu;
            h.assertTrue(ItemStack.matches(menu.getContainer().getItem(0),handoff),"native reload and menu keep the exact enchanted, damaged and carry-stamped original");
            menu.clicked(0,0,ClickType.QUICK_MOVE,p);
            p.closeContainer();
            h.assertTrue(p.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,handoff))&&f.own(p).getList("Drawers",Tag.TAG_COMPOUND).isEmpty(),"native shift-click transfers custody once, without duplication");
            HotelVignette.openProperty(p,true);
            h.assertTrue(((ChestMenu)p.containerMenu).getContainer().isEmpty(),"opening again does not replenish the drawer");
            p.closeContainer();
            var own=f.own(p);
            own.putInt("Debt",1);
            HotelVignette.save(f.data(),p.getUUID(),own);
            f.chest(p);
            h.assertTrue(f.own(p).getInt("Debt")==0&&p.getInventory().items.stream().allMatch(ItemStack::isEmpty),"a real chest collects exactly one stack unit into saved lost property");
            p.closeContainer();
            HotelVignette.openProperty(p,false);
            menu=(ChestMenu)p.containerMenu;
            h.assertTrue(ItemStack.matches(menu.getContainer().getItem(0),handoff),"lost property retains the same complete components");
            menu.clicked(0,0,ClickType.QUICK_MOVE,p);
            p.closeContainer();
            f.reload();
            h.assertTrue(f.own(p).getList("LostProperty",Tag.TAG_COMPOUND).isEmpty()&&p.getInventory().items.stream().anyMatch(s->ItemStack.matches(s,handoff)),"recovery, close and reload do not duplicate the original");
            h.succeed();
        });
    }
    @GameTest(template="empty",batch="hotel_plant",timeoutTicks=100)
    public static void boilerStartsOnDiscoveryBlowsOnceAndRestartsOnlyItsHotel(GameTestHelper h){plant=new Fixture(h,49000,true);var f=plant;var p=f.player("hotel_engineer");h.assertTrue(!HotelVignette.world(f.data()).getBoolean("PlantFound"),"the empty hotel has no running boiler");f.at(p,3.5,-4,-62.5);
        h.runAfterDelay(5,()->{var world=HotelVignette.world(f.data());h.assertTrue(world.getBoolean("PlantFound"),"physical discovery starts the gauge");long start=world.getLong("VentedAt");h.assertTrue(HotelVignette.pressure(world,start+48000)==2,"pressure advances by actual in-game days");var bed=f.l.getBlockState(f.b.offset(HotelRooms.BED));var roomClock=SceneClock.time(0,1,12345);HotelVignette.plantTick(f.l,f.b,start+72000);HotelVignette.plantTick(f.l,f.b,start+80000);h.assertTrue(HotelVignette.world(f.data()).getBoolean("Cold")&&HotelVignette.world(f.data()).getInt("Blasts")==1,"neglect blows once and remains cold");h.assertTrue(!f.l.getBlockState(f.b.offset(HotelRooms.FIRE)).getValue(CampfireBlock.LIT)&&f.l.getBlockState(f.b.offset(HotelRooms.BED)).equals(bed)&&SceneClock.time(0,1,12345)==roomClock,"the dining fire goes out without breaking the bed or changing world time");f.at(p,HotelRooms.BED.getX()+.5,6,HotelRooms.BED.getZ()-.5);h.assertTrue(!HotelVignette.rest(p),"cold 217 refuses the real rest");f.at(p,5.5,-4,-63.5);HotelVignette.maintain(p,true);h.assertTrue(!HotelVignette.world(f.data()).getBoolean("Cold")&&f.l.getBlockState(f.b.offset(HotelRooms.FIRE)).getValue(CampfireBlock.LIT),"the physical restart switch restores only the hotel plant");f.at(p,HotelRooms.BED.getX()+.5,6,HotelRooms.BED.getZ()-.5);h.assertTrue(HotelVignette.rest(p),"restart restores the hotel's made bed");h.succeed();});
    }
    @GameTest(template="empty",batch="hotel_blind",timeoutTicks=100)
    public static void blindTurnsHaveARealRetraceablePathAndIndependentHonestSoundSources(GameTestHelper h){blind=new Fixture(h,49500,false);var f=blind;var p=f.player("blind_walker");int stage=0;BlockPos previous=new BlockPos(0,0,-3);
        for(var turn:BlindStretch.TURNS){int dx=Integer.signum(turn.getX()-previous.getX()),dz=Integer.signum(turn.getZ()-previous.getZ());var at=previous;while(!at.equals(turn)){at=at.offset(dx,0,dz);h.assertTrue(f.l.noCollision(null,new AABB(f.b.getX()+at.getX()+.2,f.b.getY()+.01,f.b.getZ()+at.getZ()+.2,f.b.getX()+at.getX()+.8,f.b.getY()+1.8,f.b.getZ()+at.getZ()+.8)),"every real blind-route cell clears a native player body");h.assertTrue(!f.l.getBlockState(f.b.offset(at).below()).isAir(),"each blind turn has a physical supported return floor");}stage=BlindStretch.advance(stage,f.b,f.b.offset(turn).getCenter());previous=turn;}
        h.assertTrue(stage==6&&!BlindStretch.lies(0)&&!BlindStretch.lies(1)&&BlindStretch.lies(2),"arrows begin lying after the two actual first turns");h.assertTrue(LabyrinthLoops.LONG_HALLWAY_TICKS==1800&&!LabyrinthPlace.BLIND_STRETCH.isVignette()&&WitnessAccount.Story.of("blind_stretch")==null,"the revised long hall leaves blind-stretch mechanics and the Witness pool independent");f.at(p,.5,0,-9.5);
        h.runAfterDelay(25,()->{var own=f.data().stateEntry(BlindStretch.STATE,p.getUUID().toString());h.assertTrue(own.getInt("Turn")>=1,"present native movement records the actual turn, without elapsed-time shortcuts");f.reload();h.assertTrue(f.data().stateEntry(BlindStretch.STATE,p.getUUID().toString()).getInt("Turn")>=1,"native save reload preserves the blind route");h.succeed();});
    }
    private static boolean free(ServerLevel l,BlockPos at){return l.noCollision(null,new AABB(at.getX()+.2,at.getY()+.1,at.getZ()+.2,at.getX()+.8,at.getY()+1.75,at.getZ()+.8));}
    private static boolean stand(ServerLevel l,BlockPos at){if(!free(l,at))return false;var below=at.below();return !l.getBlockState(below).getCollisionShape(l,below).isEmpty()||!l.getBlockState(at).getCollisionShape(l,at).isEmpty();}
    /** Every place a guest can walk to from inside the front door, up and down the real stairs and through the real doors. */
    private static Set<BlockPos> walk(ServerLevel l,BlockPos b,BlockPos start,int x0,int x1,int y0,int y1,int z0,int z1){
        var seen=new HashSet<BlockPos>();var todo=new ArrayDeque<BlockPos>();seen.add(start);todo.add(start);
        while(!todo.isEmpty()){var at=todo.remove();
            for(var side:Direction.Plane.HORIZONTAL){var n=at.relative(side);var r=n.subtract(b);if(r.getX()<x0||r.getX()>x1||r.getZ()<z0||r.getZ()>z1)continue;
                for(int dy=1;dy>=-3;dy--){var c=n.above(dy);if(c.getY()-b.getY()<y0||c.getY()-b.getY()>y1||seen.contains(c)||!stand(l,c))continue;
                    if(dy==1&&!free(l,at.above()))continue;
                    boolean clear=true;for(int k=dy+1;k<=0;k++)if(!free(l,n.above(k)))clear=false;if(!clear)continue;
                    seen.add(c);todo.add(c);}}}
        return seen;
    }
    @GameTest(template="empty",batch="hotel_routes",timeoutTicks=160)
    public static void theHotelIsOneBuildingItsCorridorIsTheNumberedOnesAndItsBackWallIsTheGroundsView(GameTestHelper h){routes=new Fixture(h,49700,true);var f=routes;
        var hb=LabyrinthPlaces.base(f.origin,LabyrinthPlace.HOTEL_HALLWAY);LabyrinthLoops.buildHallway(f.l,hb,LabyrinthPlace.HOTEL_HALLWAY);LabyrinthBuilder.registerDoors(f.data(),LabyrinthPlace.HOTEL_HALLWAY,hb);
        h.runAfterDelay(20,()->{
            // The grounds see the hotel's rear wall: siding, the porch windows and the back door, block for block.
            for(int x=-7;x<=7;x++)for(int y=0;y<=7;y++)h.assertTrue(f.l.getBlockState(f.b.offset(x,y,-67)).equals(f.outside.getBlockState(f.gb.offset(x,y,1))),"the grounds' wall is the hotel's rear wall at "+x+" "+y);
            h.assertTrue(StanleyBlock.is(f.l.getBlockState(f.b.offset(3,1,-67)),StanleyBlock.Kind.SIDING)&&f.l.getBlockState(f.b.offset(HotelRooms.BACK_DOOR)).getBlock() instanceof DoorBlock&&f.outside.getBlockState(f.gb.offset(0,0,1)).getBlock() instanceof DoorBlock,"the back door stands in the siding the grounds see");
            // 217's side of the hotel's guest corridor is the numbered corridor's, block for block: walls, door, plate, carpet and ceiling.
            var loop=hb.offset(LabyrinthLoops.roomDoor(1,0));var mine=f.b.offset(HotelRooms.ROOM_DOOR);
            for(int dx=0;dx<=2;dx++)for(int dy=0;dy<=3;dy++)for(int dz=-2;dz<=2;dz++){if(dx==2&&dy==2)continue;
                h.assertTrue(f.l.getBlockState(loop.offset(dx,dy,dz)).getBlock()==f.l.getBlockState(mine.offset(dx,dy,dz)).getBlock(),"the guest corridor at 217 matches the numbered corridor at "+dx+" "+dy+" "+dz+": "+f.l.getBlockState(loop.offset(dx,dy,dz))+" / "+f.l.getBlockState(mine.offset(dx,dy,dz)));}
            h.assertTrue(f.l.getBlockState(f.b.offset(HotelRooms.NUMBERS)).getBlock() instanceof DoorBlock&&f.data().door(LabyrinthPlace.HOTEL.id()+"/numbers")!=null,"the guest corridor ends at the door into the numbered corridor");
            // The pale boards of 217's floor lie over the table for two.
            h.assertTrue(f.l.getBlockState(f.b.offset(HotelRooms.PATCH)).is(HotelRegistry.PROP.get())&&StanleyBlock.is(f.l.getBlockState(f.b.offset(HotelRooms.TABLE)),StanleyBlock.Kind.TABLE_SET),"the patch and the table stand one above the other");
            for(int y=1;y<=4;y++)h.assertTrue(f.l.getBlockState(f.b.offset(HotelRooms.TABLE.getX(),y,HotelRooms.TABLE.getZ())).isAir(),"nothing hangs between 217's patched floor and the table: "+y);
            // Walk it: every room from inside the front door, up the grand stair, down to the boiler, through the doors.
            for(var at:BlockPos.betweenClosed(f.b.offset(-28,-5,-67),f.b.offset(28,9,0))){var s=f.l.getBlockState(at);if(s.getBlock() instanceof DoorBlock&&!s.getValue(DoorBlock.OPEN))f.l.setBlock(at,s.setValue(DoorBlock.OPEN,true),2|16);}
            var reach=walk(f.l,f.b,f.b.offset(0,0,-3),-28,28,-5,9,-67,0);
            for(var target:List.of(new BlockPos(-5,0,-5),new BlockPos(-9,0,-5),new BlockPos(18,0,-8),new BlockPos(-20,0,-6),new BlockPos(-19,0,-21),new BlockPos(-15,1,-29),new BlockPos(0,0,-28),new BlockPos(0,1,-38),
                    new BlockPos(-20,0,-45),new BlockPos(0,0,-47),new BlockPos(20,0,-47),new BlockPos(0,0,-54),new BlockPos(-21,0,-61),new BlockPos(-10,0,-62),new BlockPos(-10,0,-66),new BlockPos(0,0,-62),new BlockPos(0,0,-66),
                    new BlockPos(20,0,-62),new BlockPos(0,-4,-62),new BlockPos(3,-4,-64),new BlockPos(9,6,-13),new BlockPos(-14,6,-21),new BlockPos(-20,6,-19),new BlockPos(-25,6,-25),new BlockPos(-20,6,-36),new BlockPos(-21,6,-45),new BlockPos(-14,6,-44)))
                h.assertTrue(reach.contains(f.b.offset(target)),"a guest can walk from the front door to "+target);
            // Walking back against the numbers lets a guest who has never stayed out into the hotel's upstairs corridor.
            var walker=f.player("hotel_numbers");walker.moveTo(hb.getX()+1.5,hb.getY(),hb.getZ()-1.5);var own=f.own(walker);own.putBoolean("HallExit",true);HotelVignette.save(f.data(),walker.getUUID(),own);
            h.assertTrue(HotelVignette.inside(walker,LabyrinthPlace.HOTEL_HALLWAY)&&HotelVignette.route(walker,f.data().door(LabyrinthPlace.HOTEL_HALLWAY.entryDoorId()))==null,"the corridor's own door still opens as a door");
            var back=f.data().peekReturn(walker.getUUID());h.assertTrue(back!=null&&back.door()&&BlockPos.containing(back.pos()).equals(f.b.offset(HotelRooms.NUMBERS))&&Direction.fromYRot(back.yaw())==Direction.SOUTH,"its way back now leads out into the hotel's guest corridor");
            // The grounds' hedge maze still reaches its master-key centre.
            var todo=new ArrayDeque<BlockPos>();var seen=new HashSet<BlockPos>();var start=new BlockPos(0,0,-26);todo.add(start);seen.add(start);while(!todo.isEmpty()){var at=todo.remove();for(var side:Direction.Plane.HORIZONTAL){var next=at.relative(side);if(Math.abs(next.getX())>15||next.getZ()<-56||next.getZ()>-25||!seen.add(next))continue;var abs=f.gb.offset(next);if(f.outside.noCollision(null,new AABB(abs.getX()+.2,abs.getY()+.01,abs.getZ()+.2,abs.getX()+.8,abs.getY()+1.8,abs.getZ()+.8)))todo.add(next);}}h.assertTrue(seen.contains(HotelRooms.KEY),"the actual native hedge maze reaches its master-key center");
            h.assertTrue(LabyrinthDealer.vignettesAvailable(f.data()).contains(LabyrinthPlace.HOTEL),"the recurring hotel remains an available property-recovery anchor");
            for(int y:new int[]{70,80,200,300}){var origin=new BlockPos(0,y,0);for(var place:LabyrinthPlace.values())if(place.slot()>=0)h.assertTrue(LabyrinthPlaces.base(origin,place)!=null,"appending rooms preserves a valid slot at each supported manor height");}
            h.assertTrue(NovelRooms.outside(LabyrinthPlace.HOTEL_GROUNDS)&&SceneClock.time(8,0,6000)==18000,"hotel grounds have real outside sky and their own spatial blizzard night");h.assertTrue(WitnessAccount.Story.values().length==46&&WitnessAccount.REQUIRED==35,"the hotel's three endings are still one source in the pool");
            h.succeed();});
    }
    @AfterBatch(batch="hotel_routes") public static void closeRoutes(ServerLevel l){if(routes!=null){routes.close();routes=null;}}
    @GameTest(template="empty",batch="hotel_history",timeoutTicks=100)
    public static void cemeteryRecordsActualNamedPetDeathAndCanceledEventsInventNothing(GameTestHelper h){var f=new Fixture(h,49900,true);try{var p=f.player("hotel_history");var wolf=EntityType.WOLF.create(f.l);h.assertTrue(wolf!=null,"the native tame wolf exists");wolf.setTame(true,false);wolf.setOwnerUUID(p.getUUID());wolf.setCustomName(Component.literal("Moss"));wolf.moveTo(f.b.getX()+4,f.b.getY(),f.b.getZ()-4);f.l.addFreshEntity(wolf);h.assertTrue(wolf.hurt(f.l.damageSources().genericKill(),1000),"an actual native death occurs");h.assertTrue(f.own(p).getList("LostPets",Tag.TAG_COMPOUND).size()==1&&f.own(p).getList("LostPets",Tag.TAG_COMPOUND).getCompound(0).getString("Name").equals("Moss"),"the ledger retains the actual companion UUID and name");var prior=f.own(p).copy();var event=new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p,f.l.damageSources().generic());event.setCanceled(true);NeoForge.EVENT_BUS.post(event);h.assertTrue(f.own(p).equals(prior),"a canceled death does not invent a hotel tale or lost companion");f.reload();h.assertTrue(f.own(p).getList("LostPets",Tag.TAG_COMPOUND).getCompound(0).getUUID("Entity").equals(wolf.getUUID()),"the actual pet identity survives native save reload");h.succeed();}finally{f.close();}}
    @AfterBatch(batch="hotel_arc") public static void closeArc(ServerLevel l){if(arc!=null){arc.close();arc=null;}}
    @AfterBatch(batch="hotel_property") public static void closeCustody(ServerLevel l){if(custody!=null){custody.close();custody=null;}}
    @AfterBatch(batch="hotel_plant") public static void closePlant(ServerLevel l){if(plant!=null){plant.close();plant=null;}}
    @AfterBatch(batch="hotel_blind") public static void closeBlind(ServerLevel l){if(blind!=null){blind.close();blind=null;}}

    @GameTest(template="empty",batch="hotel_history_bounds",timeoutTicks=180)
    public static void oldCustodyMigratesExactlyAndSixtyActualPetLossesStayInsideTheGrounds(GameTestHelper h){
        history=new Fixture(h,50100,true);var f=history;var p=f.player("hotel_bounded");var peer=f.player("hotel_unaffected");f.at(peer,35,0,-5);
        var original=new ItemStack(Items.DIAMOND_PICKAXE);original.set(DataComponents.CUSTOM_NAME,Component.literal("A saved original"));original.setDamageValue(93);var row=new CompoundTag();row.putInt("Slot",7);row.put("Stack",original.save(p.registryAccess()));var drawers=new ListTag();drawers.add(row);
        var legacy=new CompoundTag();var own=new CompoundTag();own.put("Drawers",drawers.copy());var deaths=new ListTag();for(int i=0;i<30;i++){var death=new CompoundTag();death.putUUID("Entity",p.getUUID());death.putLong("At",i);death.putString("Cause","fall");deaths.add(death);}own.put("Deaths",deaths);legacy.put(p.getUUID().toString(),own);legacy.putBoolean("PlantFound",true);legacy.putLong("VentedAt",f.l.getGameTime());f.data().setState(HotelVignette.STATE,legacy);
        var world=HotelVignette.world(f.data());h.assertTrue(world.getBoolean("PlantFound")&&!world.contains(p.getUUID().toString())&&f.own(p).getList("Drawers",Tag.TAG_COMPOUND).equals(drawers),"shared-state migration never deep-copies a peer's custody or changes original components");
        h.assertTrue(f.own(p).getList("Deaths",Tag.TAG_COMPOUND).size()==16&&f.own(p).getList("Deaths",Tag.TAG_COMPOUND).getCompound(0).getLong("At")==14&&f.own(p).getLong("DeathsTotal")==30,"legacy death history retains its sixteen newest truthful entries and total");
        var actual=p.server.overworld();
        for(int i=0;i<60;i++){
            var wolf=EntityType.WOLF.create(actual);h.assertTrue(wolf!=null,"the native pet exists");wolf.setTame(true,false);wolf.setOwnerUUID(p.getUUID());wolf.setCustomName(Component.literal("Moss "+i));wolf.moveTo(50100,80,50100);actual.addFreshEntity(wolf);h.assertTrue(wolf.hurt(actual.damageSources().genericKill(),1000),"each history entry comes from an actual native tame-pet death outside the hotel");wolf.discard();
        }
        h.assertTrue(f.own(p).getList("LostPets",Tag.TAG_COMPOUND).size()==52&&f.own(p).getLong("LostPetsTotal")==60&&f.own(peer).getList("LostPets",Tag.TAG_COMPOUND).isEmpty(),"bounded truthful memories remain personal even when losses occur in the Overworld");
        var projection=EntityType.WOLF.create(actual);projection.setTame(true,false);projection.setOwnerUUID(p.getUUID());projection.addTag(LiteraryCopies.PROJECTION);var ignored=new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(projection,actual.damageSources().genericKill());NeoForge.EVENT_BUS.post(ignored);h.assertTrue(f.own(p).getLong("LostPetsTotal")==60,"a projection can never invent another lost companion");
        p.teleportTo(f.outside,f.gb.getX()+.5,f.gb.getY(),f.gb.getZ()-3,180,0);p.hasChangedDimension();p.setNoGravity(true);HotelVignette.onArrive(p,LabyrinthPlace.HOTEL_GROUNDS);
        h.runAfterDelay(30,()->{
            h.assertTrue(HotelVignette.world(f.data()).getInt("Graves")==52,"the fifty-third loss never places a grave over the void");
            for(int plot=0;plot<52;plot++){var at=f.gb.offset(-24+(plot%13)*4,0,-59-(plot/13)*3);h.assertTrue(f.outside.getBlockState(at).is(HotelRegistry.PROP.get())&&!f.outside.getBlockState(at.below()).isAir()&&!f.outside.getBlockState(at.below(2)).isAir(),"native grave "+plot+" retains a real foundation within the four supported rows: "+at);}
            h.assertTrue(f.outside.getBlockState(f.gb.offset(-24,0,-71)).isAir(),"there is no fifty-third headstone beyond the actual grounds");
            var at=f.gb.offset(-24,0,-71);f.outside.setBlock(at,HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,HotelPropBlock.Kind.HEADSTONE),3);f.outside.setBlock(at.below(),Blocks.GRAVEL.defaultBlockState(),3);var overflow=EntityType.TEXT_DISPLAY.create(f.outside);overflow.moveTo(at.getX()+.5,at.getY()+1.8,at.getZ()+.5);overflow.addTag("HotelGrave");f.outside.addFreshEntity(overflow);
            h.assertTrue(overflow.isRemoved()&&f.outside.getBlockState(at).isAir()&&f.outside.getBlockState(at.below()).isAir(),"a loaded legacy overflow cleans only its own old headstone and support");
            f.reload();h.assertTrue(f.own(p).getList("Drawers",Tag.TAG_COMPOUND).equals(drawers)&&ItemStack.matches(ItemStack.parseOptional(p.registryAccess(),f.own(p).getList("Drawers",Tag.TAG_COMPOUND).getCompound(0).getCompound("Stack")),original),"reload retains the exact native item while bounded histories and shared scenery persist");h.succeed();
        });
    }
    @AfterBatch(batch="hotel_history_bounds") public static void historyBoundsDone(ServerLevel l){if(history!=null){history.close();history=null;}}

    @GameTest(template="empty",batch="stanley_items",timeoutTicks=300)
    public static void StanleyFurnishingsUseActualHitsAndBoundedPersonalActions(GameTestHelper h){stanleyItems=new Fixture(h,50300,true);var f=stanleyItems;var p=f.player("stanley_items");
        h.runAtTickTime(30,()->{
            var dining=new BlockPos(-14,1,-31);f.click(p,dining);h.assertTrue(f.own(p).contains("LastPiano")&&f.own(p).getInt("Debt")==0&&!f.own(p).getBoolean("Dinner"),"playing the actual dining-room piano records its bounded action without charging a meal or granting story progress");long played=f.own(p).getLong("LastPiano");f.click(p,dining);h.assertTrue(f.own(p).getLong("LastPiano")==played,"held use cannot stack the four-second piano cue");
            for(var face:Direction.Plane.HORIZONTAL){var at=f.b.offset(HotelRooms.DESK);f.l.setBlock(at,HotelRegistry.PROP.get().defaultBlockState().setValue(HotelPropBlock.KIND,HotelPropBlock.Kind.RECEPTION).setValue(HotelPropBlock.FACING,face),3);var point=switch(face){case NORTH->new Vec3(.75,1.25,.375);case EAST->new Vec3(.625,1.25,.75);case SOUTH->new Vec3(.25,1.25,.625);case WEST->new Vec3(.375,1.25,.25);default->throw new IllegalStateException();};var top=Vec3.atLowerCornerOf(at).add(point);f.at(p,-5,0,-5);var hit=f.l.clip(new ClipContext(top.add(0,1,0),top.add(0,-1,0),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));h.assertTrue(hit.getType()==HitResult.Type.BLOCK&&hit.getBlockPos().equals(at)&&Math.abs(hit.getLocation().y-top.y)<.01,"the real ray selects the visible bell, including its rotated height: "+face+" / "+hit.getLocation());var own=f.own(p);own.remove("LastBell");HotelVignette.save(f.data(),p.getUUID(),own);var event=new PlayerInteractEvent.RightClickBlock(p,InteractionHand.MAIN_HAND,at,hit);NeoForge.EVENT_BUS.post(event);h.assertTrue(event.isCanceled()&&f.own(p).contains("LastBell")&&!(p.containerMenu instanceof HotelVignette.Pages),"the actual bell hit rings instead of opening the account: "+face);}
            var all=HotelVignette.world(f.data());var porter=(HotelActor)f.l.getEntity(all.getUUID("Actor0"));h.assertTrue(porter!=null&&porter.gesture()==HotelActor.GREET,"ringing the bell acknowledges the player on the original porter body");var saved=porter.saveWithoutId(new CompoundTag());var restored=HotelRegistry.ACTOR.get().create(f.l);restored.load(saved);h.assertTrue(restored.getUUID().equals(porter.getUUID())&&restored.gesture()==HotelActor.GREET,"the bounded native gesture survives save/reload with its original UUID");
            h.assertTrue(porter.distanceToSqr(Vec3.atCenterOf(f.b.offset(HotelRooms.DESK)))<9,"the porter stands behind the desk");
            for(var room:HotelRooms.LOCKED){var before=f.l.getBlockState(f.b.offset(room.door()));f.click(p,room.door());h.assertTrue(f.l.getBlockState(f.b.offset(room.door())).equals(before),"a numbered guest room stays locked: "+room.number());}
            f.l.setBlock(f.b.offset(HotelRooms.GAUGE),Blocks.AIR.defaultBlockState(),3);f.click(p,HotelRooms.BOILER);h.assertTrue(!f.own(p).getBoolean("Maintained"),"examining a boiler with a removed gauge does not repair the player's edit or complete maintenance");
            p.setGameMode(GameType.SPECTATOR);var before=f.own(p).copy();f.click(p,HotelRooms.PIANO);h.assertTrue(f.own(p).equals(before),"a native observer cannot play, charge, collect or progress these personal interactions");h.succeed();
        });
    }
    @AfterBatch(batch="stanley_items") public static void stanleyItemsDone(ServerLevel l){if(stanleyItems!=null){stanleyItems.close();stanleyItems=null;}}

    @GameTest(template="empty",batch="stanley_dance",timeoutTicks=300)
    public static void StanleyPartnersMoveOnTheNativeParquetAndKeepTheirIdentities(GameTestHelper h){stanleyDance=new Fixture(h,50500,true);var f=stanleyDance;var p=f.player("stanley_dance");var starts=new HashMap<UUID,Vec3>();var previous=new HashMap<UUID,Vec3>();final int[] movingFrames={0},ticks={0};
        h.onEachTick(()->{int tick=++ticks[0];if(tick<=30||tick>=90)return;boolean moved=false;for(var id:starts.keySet()){var a=f.l.getEntity(id);if(a==null)continue;var before=previous.put(id,a.position());if(before==null)continue;double step=a.position().subtract(before).horizontalDistance();h.assertTrue(step<=.017,"every original partner advances in small native-tick steps: "+step);moved|=step>.001;}if(moved)movingFrames[0]++;});
        h.runAtTickTime(89,()->h.assertTrue(movingFrames[0]>=50,"the six original partners move continuously between the former five-tick updates: "+movingFrames[0]));
        h.runAtTickTime(30,()->{f.at(p,.5,0,-27.5);HotelVignette.photograph(p);var all=HotelVignette.world(f.data());for(int i=0;i<6;i++){h.assertTrue(all.hasUUID("Dancer"+i)&&f.l.getEntity(all.getUUID("Dancer"+i)) instanceof HotelActor,"the six original dance bodies are native and loaded");var a=(HotelActor)f.l.getEntity(all.getUUID("Dancer"+i));h.assertTrue(a.danceSlot()==i,"each real body knows its partner");starts.put(a.getUUID(),a.position());}});
        h.runAtTickTime(90,()->{var all=HotelVignette.world(f.data());for(int i=0;i<6;i++){var a=(HotelActor)f.l.getEntity(all.getUUID("Dancer"+i));var partner=(HotelActor)f.l.getEntity(all.getUUID("Dancer"+(i^1)));h.assertTrue(a!=null&&starts.containsKey(a.getUUID())&&a.position().distanceToSqr(starts.get(a.getUUID()))>.005,"the same native dancer takes real steps rather than only posing");h.assertTrue(a.distanceToSqr(partner)<2.3&&Math.abs(a.getY()-f.b.getY())<.2&&IndianLakeRooms.bounds(f.b,LabyrinthPlace.HOTEL).contains(a.position()),"partners remain together on the supported native dance floor");var rel=a.position().subtract(Vec3.atLowerCornerOf(f.b));h.assertTrue(rel.x>HotelRooms.FLOOR_X0&&rel.x<HotelRooms.FLOOR_X1+1&&rel.z>HotelRooms.FLOOR_Z0&&rel.z<HotelRooms.FLOOR_Z1+1,"the dancers dance in the ballroom: "+rel);var saved=a.saveWithoutId(new CompoundTag());var restored=HotelRegistry.ACTOR.get().create(f.l);restored.load(saved);h.assertTrue(restored.danceSlot()==i&&restored.getUUID().equals(a.getUUID()),"partner identity and authored motion persist without respawning a dancer");}var original=f.own(p).getCompound("Original_Photo").copy();f.reload();h.assertTrue(f.own(p).getCompound("Original_Photo").equals(original)&&!WitnessAccount.has(f.data(),p.getUUID(),WitnessAccount.Story.HOTEL),"dancing retains the exact finite photograph and is not an ending");h.succeed();});
    }
    @AfterBatch(batch="stanley_dance") public static void stanleyDanceDone(ServerLevel l){if(stanleyDance!=null){stanleyDance.close();stanleyDance=null;}}

}
