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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PhoneCanoeTests {
    private static Fixture originalCanoe;
    @AfterBatch(batch="phone_original0493") public static void cleanOriginalCanoe(ServerLevel level){if(originalCanoe!=null){originalCanoe.close();originalCanoe=null;}}
    @GameTest(template="empty",batch="phone_original0493",timeoutTicks=250)
    public static void filmingCanoeCannotBeReplacedWhileItsSerializedOriginalLoads(GameTestHelper h){
        originalCanoe=new Fixture(h,new BlockPos(49500,80,49500));var f=originalCanoe;var original=PhoneCanoe.stage(f.level,f.base);var id=original.getUUID();
        original.setCustomName(net.minecraft.network.chat.Component.literal("The original filming canoe"));var saved=original.saveWithoutId(new CompoundTag());original.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        var data=LabyrinthData.get(f.server);f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.load(data.save(new CompoundTag(),f.level.registryAccess()),f.level.registryAccess()));
        h.onEachTick(()->PhoneCanoe.stage(f.level,f.base));
        h.runAfterDelay(140,()->{
            h.assertTrue(PhoneCanoe.stage(f.level,f.base)==null&&f.level.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(f.base,LabyrinthPlace.PHONE_CANOE),e->e.getTags().contains(PhoneCanoe.CANOE)).isEmpty(),"restaging and SavedData reload preserve the absent original instead of making another filming canoe");
            var returned=DrownedTownRegistry.CAVE_CANOE.get().create(f.level);returned.load(saved);h.assertTrue(f.level.addFreshEntity(returned),"the same serialized canoe UUID returns successfully");
            h.assertTrue(PhoneCanoe.stage(f.level,f.base)==returned&&returned.getUUID().equals(id)&&returned.getName().getString().equals("The original filming canoe"),"the exact original canoe remains the scene's native body and retains its name");h.succeed();
        });
    }
    @GameTest(template="empty") public static void appendedRoomAndLeakFitExistingWorldSlots(GameTestHelper h){
        for(int y:new int[]{65,80,150,250}){
            BlockPos origin=new BlockPos(100,y,100),base=LabyrinthPlaces.base(origin,LabyrinthPlace.PHONE_CANOE);var slot=LabyrinthPlaces.slotBounds(origin,LabyrinthPlace.PHONE_CANOE);
            h.assertTrue(slot.isInside(base.offset(-26,-4,-60))&&slot.isInside(base.offset(26,10,0)),"lake and authored night sky fit wholly inside their native slot");
        }
        h.assertTrue(LabyrinthPlace.DROWNED_TOWN.slot()==27&&LabyrinthPlace.PRESERVED_CAVE.slot()==28&&LabyrinthPlace.SHALLOWS.slot()==29&&LabyrinthPlace.PHONE_CANOE.slot()==30,"new room appends without moving an older site");
        h.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.PHONE_CANOE,0)==DoorLeakKind.CANOE&&DoorLeakKind.CANOE.ordinal()==10,"new sensory hint appends without reinterpreting old saved ordinals");h.succeed();
    }
    @GameTest(template="empty") public static void personalCompletionDoesNotConsumeAnotherExplorersRecording(GameTestHelper h){
        var data=new LabyrinthData();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        h.assertTrue(LabyrinthDealer.vignettesAvailable(data,a).contains(LabyrinthPlace.PHONE_CANOE),"the filming encounter is actually dealt");
        CompoundTag all=data.state(PhoneCanoe.ID),record=new CompoundTag();record.putBoolean("Finished",true);all.put(a.toString(),record);data.setState(PhoneCanoe.ID,all);
        var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(!LabyrinthDealer.vignettesAvailable(loaded,a).contains(LabyrinthPlace.PHONE_CANOE)&&LabyrinthDealer.vignettesAvailable(loaded,b).contains(LabyrinthPlace.PHONE_CANOE),"personal exhaustion survives reload and leaves the peer's original scene available");
        h.assertTrue(!WitnessAccount.has(loaded,a,WitnessAccount.Story.PHONE_CANOE),"a completion flag cannot invent a personal resolution");h.succeed();
    }
    private static final class Fixture implements AutoCloseable {
        final net.minecraft.server.MinecraftServer server;final ServerLevel level;final HouseSavedData oldHouse;final LabyrinthData oldData;
        final GameTestHelper helper;final BlockPos origin,base;final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,BlockPos origin){
            helper=h;server=h.getLevel().getServer();level=HouseTestLevel.get(server,HouseDimensions.OUTSIDE);this.origin=origin;oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.PHONE_CANOE);PhoneCanoe.build(server,level,base);LabyrinthBuilder.registerDoors(data,LabyrinthPlace.PHONE_CANOE,base);IndianLakeRooms.keepLoaded(level,base,LabyrinthPlace.PHONE_CANOE);
        }
        ServerPlayer player(String name,BlockPos at){
            // NeoForge FakePlayer deliberately refuses startRiding. Use Minecraft's real GameTest mock player.
            var p=helper.makeMockServerPlayerInLevel();p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            p.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,180,0);players.add(p);return p;
        }
        public void close(){
            for(var p:players){PhoneCanoe.interrupt(p);if(server.getPlayerList().getPlayers().contains(p))server.getPlayerList().remove(p);else p.discard();}
            for(var place:List.of(LabyrinthPlace.PHONE_CANOE,LabyrinthPlace.PRESERVED_CAVE,LabyrinthPlace.DROWNED_TOWN)){
                BlockPos b=LabyrinthPlaces.base(origin,place);AABB box=IndianLakeRooms.bounds(b,place);
                var sceneLevel=HouseTestLevel.get(server,NovelRooms.dimension(place));
                for(var e:sceneLevel.getEntitiesOfClass(Entity.class,box,e->e instanceof LakePhoneCamera||e instanceof LakeCanoeEntity||e instanceof LakeCongregantEntity||e instanceof LakeWitchEntity))e.discard();
                for(int x=((int)box.minX-1)>>4;x<=((int)box.maxX+1)>>4;x++)for(int z=((int)box.minZ-1)>>4;z<=((int)box.maxZ+1)>>4;z++)sceneLevel.getChunkSource().removeRegionTicket(TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(x,z),3,b);
            }
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);LabyrinthBuilder.clearAll();LabyrinthDoors.clearAll();
        }
    }
    private static Fixture scene,retry,upgrade,domesticUpgrade;
    @AfterBatch(batch="phone_scene") public static void cleanScene(ServerLevel level){if(scene!=null){scene.close();scene=null;}}
    @AfterBatch(batch="phone_retry") public static void cleanRetry(ServerLevel level){if(retry!=null){retry.close();retry=null;}}
    @GameTest(template="empty",batch="phone_scene",timeoutTicks=430)
    public static void nativeCanoeDropSkyCameraBlackoutAndOwnedCaveRecovery(GameTestHelper h){
        scene=new Fixture(h,new BlockPos(8200,80,8200));Fixture f=scene;LabyrinthData data=LabyrinthData.get(f.server);
        var owner=f.player("lake_recorder",f.base.offset(PhoneCanoe.DOCK));var peer=f.player("lake_observer",f.base.offset(1,0,-10));
        owner.getInventory().setItem(5,new ItemStack(Items.DIAMOND,3));owner.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.TORCH,8));
        var canoe=PhoneCanoe.stage(f.level,f.base);h.assertTrue(PhoneCanoe.board(owner,canoe)&&owner.getVehicle()==canoe,"native boarding: inside="+IndianLakeRooms.inside(owner,LabyrinthPlace.PHONE_CANOE)+"; distance="+owner.distanceToSqr(canoe)+"; player="+owner.position()+"; boat="+canoe.position()+"; hand="+owner.getMainHandItem()+"; alive="+owner.isAlive()+"; tags="+canoe.getTags()+"; riders="+canoe.getPassengers()+"; vehicle="+owner.getVehicle());
        h.assertTrue(!PhoneCanoe.board(peer,canoe)&&peer.getMainHandItem().isEmpty(),"another explorer cannot take the occupied boat or a second phone");
        var phone=owner.getMainHandItem();CustomData.update(DataComponents.CUSTOM_DATA,phone,t->t.putString("ProvenanceTest","keep me"));
        h.assertTrue(PhoneCanoe.beginFilm(owner,phone)&&!WitnessAccount.has(data,owner.getUUID(),WitnessAccount.Story.PHONE_CANOE),"choosing to film does not immediately award the ending");
        // The server test supplies physical motion that the real client produces through boat input.
        // It must survive scene ticks; the former no-op move and scripted moveTo both fail this check.
        h.onEachTick(()->{if(canoe.rowing()&&owner.getVehicle()==canoe&&canoe.getZ()>f.base.getZ()-33)canoe.move(MoverType.SELF,new Vec3(0,0,-.22));});
        h.runAfterDelay(110,()->h.assertTrue(canoe.getZ()<f.base.getZ()-31&&owner.getVehicle()==canoe,"native boat movement is retained while recording"));
        h.runAfterDelay(195,()->{
            h.assertTrue(PhoneCanoe.personal(data,owner.getUUID()).getBoolean("Dropped")&&owner.getInventory().countItem(DrownedTownRegistry.LAKE_PHONE.get())==0,"the actual recording leaves the original inventory at the drop");
            var cameras=f.level.getEntitiesOfClass(LakePhoneCamera.class,IndianLakeRooms.bounds(f.base,LabyrinthPlace.PHONE_CANOE));
            h.assertTrue(cameras.size()==1&&cameras.getFirst().getXRot()==-88&&cameras.getFirst().getY()<f.base.getY()+.3,"a separately tracked viewpoint really rests at the waterline looking at the sky");
            h.assertTrue(owner.isAlive()&&owner.gameMode.getGameModeForPlayer()==GameType.SURVIVAL&&!WitnessAccount.has(data,peer.getUUID(),WitnessAccount.Story.PHONE_CANOE),"the camera swap causes neither death nor spectator mode nor observer credit");
            h.assertTrue(!owner.hurt(f.level.damageSources().drown(),100),"damage cannot produce a death screen during the fixed view");
        });
        h.runAfterDelay(380,()->{
            h.assertTrue(owner.isAlive()&&owner.getVehicle()==null&&owner.position().distanceToSqr(Vec3.atBottomCenterOf(f.base.offset(PhoneCanoe.DOCK)))<1,"blackout returns the living explorer to the jetty");
            h.assertTrue(WitnessAccount.has(data,owner.getUUID(),WitnessAccount.Story.PHONE_CANOE)&&!WitnessAccount.has(data,peer.getUUID(),WitnessAccount.Story.PHONE_CANOE),"only the recorder receives the personal resolution");
            h.assertTrue(owner.getInventory().countItem(Items.DIAMOND)==3&&owner.getInventory().countItem(Items.TORCH)==8&&PreservedCave.phoneWaiting(data,owner.getUUID()),"other belongings remain intact and the original recording waits in the cave");
            var original=ItemStack.parseOptional(owner.registryAccess(),data.state("indian_lake").getCompound("DroppedPhones").getCompound(owner.getUUID().toString()).getCompound("Item"));
            var metadata=original.get(DataComponents.CUSTOM_DATA).copyTag();var frames=metadata.getList("Footage",Tag.TAG_COMPOUND);
            h.assertTrue(frames.size()==7&&frames.getCompound(0).getInt("Tick")==0&&frames.getCompound(6).getInt("Tick")==320&&metadata.getString("ProvenanceTest").equals("keep me"),"authentic frame times, final sky frame and original components survive the loss");
            BlockPos caveBase=LabyrinthPlaces.base(f.origin,LabyrinthPlace.PRESERVED_CAVE);var caveLevel=HouseTestLevel.get(f.server,HouseDimensions.INTERIOR);PreservedCave.build(f.server,caveLevel,caveBase);IndianLakeRooms.keepLoaded(caveLevel,caveBase,LabyrinthPlace.PRESERVED_CAVE);
            var at=Vec3.atBottomCenterOf(caveBase.offset(PreservedCave.CANOE));owner.teleportTo(caveLevel,at.x,at.y,at.z,180,0);peer.teleportTo(caveLevel,at.x,at.y,at.z,180,0);
            h.assertTrue(!PreservedCave.recoverPhone(peer)&&PreservedCave.recoverPhone(owner)&&!PreservedCave.recoverPhone(owner),"the actual cave delivers the original once to its owner");
            var recovered=owner.getInventory().items.stream().filter(s->s.is(DrownedTownRegistry.LAKE_PHONE.get())).findFirst().orElseThrow();
            h.assertTrue(ItemStack.isSameItemSameComponents(original,recovered),"recovery does not synthesize a replacement recording");
            for(int slot=0;slot<9;slot++)if(owner.getInventory().getItem(slot)==recovered){owner.getInventory().selected=slot;break;}
            // 0.4.121: a plain use plays the clip back on the phone's own screen; crouching opens its frame record.
            owner.setShiftKeyDown(false);DrownedTownRegistry.LAKE_PHONE.get().use(caveLevel,owner,InteractionHand.MAIN_HAND);
            h.assertTrue(!recovered.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getLast().raw().getString().contains("The same sky"),"playing the clip back writes nothing into the record");
            owner.setShiftKeyDown(true);DrownedTownRegistry.LAKE_PHONE.get().use(caveLevel,owner,InteractionHand.MAIN_HAND);owner.setShiftKeyDown(false);
            h.assertTrue(recovered.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getLast().raw().getString().contains("The same sky"),"the recovered phone exposes its real saved frame record through the native reader");h.succeed();
        });
    }
    @GameTest(template="empty",batch="phone_retry",timeoutTicks=460)
    public static void interruptionsBeforeAndAfterTheDropPreserveAPlayableOriginal(GameTestHelper h){
        retry=new Fixture(h,new BlockPos(8500,80,8500));Fixture f=retry;var owner=f.player("paused_recorder",f.base.offset(PhoneCanoe.DOCK));var data=LabyrinthData.get(f.server);var canoe=PhoneCanoe.stage(f.level,f.base);
        h.onEachTick(()->{if(canoe.rowing()&&PhoneCanoe.personal(LabyrinthData.get(f.server),owner.getUUID()).getInt("Phase")==2&&canoe.getZ()>f.base.getZ()-33)canoe.move(MoverType.SELF,new Vec3(0,0,-.22));});
        PhoneCanoe.board(owner,canoe);owner.stopRiding();
        h.runAfterDelay(4,()->{
            h.assertTrue(owner.getMainHandItem().isEmpty()&&PhoneCanoe.personal(data,owner.getUUID()).getInt("Phase")==0&&!PreservedCave.phoneWaiting(data,owner.getUUID()),"leaving before filming removes only the unused scene phone");
            owner.moveTo(Vec3.atBottomCenterOf(f.base.offset(PhoneCanoe.DOCK)));h.assertTrue(PhoneCanoe.board(owner,canoe)&&PhoneCanoe.beginFilm(owner,owner.getMainHandItem()),"retry boarding: inside="+IndianLakeRooms.inside(owner,LabyrinthPlace.PHONE_CANOE)+"; distance="+owner.distanceToSqr(canoe)+"; player="+owner.position()+"; boat="+canoe.position()+"; hand="+owner.getMainHandItem()+"; passengers="+canoe.getPassengers()+"; phase="+PhoneCanoe.personal(data,owner.getUUID()).getInt("Phase")+"; vehicle="+owner.getVehicle());
        });
        h.runAfterDelay(205,()->{
            var saved=data.save(new CompoundTag(),owner.registryAccess());var loaded=LabyrinthData.FACTORY.deserializer().apply(saved,owner.registryAccess());
            f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",loaded);
            PhoneCanoe.interrupt(owner);h.assertTrue(PreservedCave.phoneWaiting(loaded,owner.getUUID())&&PhoneCanoe.canDeal(loaded,owner.getUUID())&&!WitnessAccount.has(loaded,owner.getUUID(),WitnessAccount.Story.PHONE_CANOE),"reloading and interrupting after the drop retains the one original without premature credit");
            owner.moveTo(canoe.position());h.assertTrue(PhoneCanoe.board(owner,canoe)&&owner.getMainHandItem().isEmpty(),"resuming the dropped-phone beat creates no second inventory phone");
        });
        h.runAfterDelay(400,()->{
            var loaded=LabyrinthData.get(f.server);h.assertTrue(PhoneCanoe.personal(loaded,owner.getUUID()).getBoolean("Finished")&&WitnessAccount.has(loaded,owner.getUUID(),WitnessAccount.Story.PHONE_CANOE)&&!PhoneCanoe.canDeal(loaded,owner.getUUID()),"the saved scene resumes to one personal finish");
            h.assertTrue(loaded.state("indian_lake").getCompound("DroppedPhones").getAllKeys().size()==1,"interruption cannot multiply the original phone cache");h.succeed();
        });
    }
}
