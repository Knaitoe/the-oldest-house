package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.authlib.GameProfile;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IndianLakeLinkedTests {
    @GameTest(template="empty") public static void newLakeRoomsFitWithoutMovingTheTown(GameTestHelper h){
        for(int y:new int[]{65,80,150,250})for(var place:List.of(LabyrinthPlace.PRESERVED_CAVE,LabyrinthPlace.SHALLOWS)){
            BlockPos origin=new BlockPos(100,y,100),base=LabyrinthPlaces.base(origin,place);var box=LabyrinthPlaces.slotBounds(origin,place);
            h.assertTrue(base!=null&&box.isInside(base.offset(place.room().minX(),place.room().minY(),place.room().minZ()))
                    &&box.isInside(base.offset(place.room().maxX(),place.room().maxY(),place.room().maxZ())),"the full lake room fits its own native slot");
        }
        h.assertTrue(LabyrinthPlace.DROWNED_TOWN.slot()==27&&LabyrinthPlace.PRESERVED_CAVE.slot()==28&&LabyrinthPlace.SHALLOWS.slot()==29,"previous slots stay fixed");h.succeed();
    }
    @GameTest(template="empty") public static void personalDealerExhaustsOnlyTheParticipant(GameTestHelper h){
        LabyrinthData data=new LabyrinthData();UUID owner=UUID.randomUUID(),peer=UUID.randomUUID();
        h.assertTrue(!LabyrinthDealer.vignettesAvailable(data,owner).contains(LabyrinthPlace.SHALLOWS),"a mere visitor cannot be dealt the recollection");
        IndianLakeProgress.hunted(data,owner);IndianLakeProgress.hunted(data,peer);
        h.assertTrue(LabyrinthDealer.vignettesAvailable(data,owner).contains(LabyrinthPlace.SHALLOWS),"a real hunt makes the one-shot available");
        IndianLakeProgress.completedShallows(data,owner,"thrower");
        var loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(!LabyrinthDealer.vignettesAvailable(loaded,owner).contains(LabyrinthPlace.SHALLOWS)
                &&LabyrinthDealer.vignettesAvailable(loaded,peer).contains(LabyrinthPlace.SHALLOWS),"completion survives reload without consuming the peer's scene");h.succeed();
    }
    @GameTest(template="empty") public static void bodiesAndMemoryKeepTheirAppearanceOnReload(GameTestHelper h){
        var body=DrownedTownRegistry.CONGREGANT.get().create(h.getLevel());body.pose(false,false);body.preservedEra(3);CompoundTag tag=new CompoundTag();body.saveWithoutId(tag);
        var copy=DrownedTownRegistry.CONGREGANT.get().create(h.getLevel());copy.load(tag);
        h.assertTrue(!copy.seated()&&copy.preservedEra()==3,"stood-up bodies retain their era and pose");
        var girl=DrownedTownRegistry.LAKE_WITCH.get().create(h.getLevel());UUID owner=UUID.randomUUID();BlockPos base=new BlockPos(100,100,100);girl.recollection(owner,base);girl.memoryPhase(2);
        CompoundTag memory=new CompoundTag();girl.saveWithoutId(memory);var returned=DrownedTownRegistry.LAKE_WITCH.get().create(h.getLevel());returned.load(memory);
        h.assertTrue(returned.memory()&&owner.equals(returned.memoryOwner())&&returned.memoryPhase()==2&&base.equals(returned.memoryBase())&&!returned.striking(),"the shared model reloads as the correct person's airborne memory, never the hunter");
        body.discard();copy.discard();girl.discard();returned.discard();h.succeed();
    }
    @GameTest(template="empty") public static void newDoorsLeakTheirOwnSoundBeforeEntry(GameTestHelper h){
        h.assertTrue(DoorLeakKind.forDestination(LabyrinthPlace.PRESERVED_CAVE,0)==DoorLeakKind.CAVE
                &&DoorLeakKind.forDestination(LabyrinthPlace.SHALLOWS,0)==DoorLeakKind.SHALLOWS,"the two new doors have distinct lake hints");
        h.assertTrue(DrownedTownRegistry.caveVoice(0)!=DrownedTownRegistry.caveVoice(7),"a full hymn really adds different registered voices");h.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final MinecraftServer server;final ServerLevel level;final HouseSavedData oldHouse;final LabyrinthData oldData;
        final BlockPos origin,base;final LabyrinthPlace place;final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,BlockPos origin,LabyrinthPlace place){
            server=h.getLevel().getServer();level=HouseTestLevel.get(server);this.origin=origin;this.place=place;oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            base=LabyrinthPlaces.base(origin,place);
            if(place==LabyrinthPlace.PRESERVED_CAVE)PreservedCave.build(server,level,base);
            else if(place==LabyrinthPlace.SHALLOWS)Shallows.build(server,level,base);
            else DrownedTown.build(server,level,base);
            LabyrinthBuilder.registerDoors(data,place,base);load();
        }
        void load(){IndianLakeRooms.keepLoaded(level,base,place);}
        ServerPlayer player(String name,BlockPos at){
            ServerPlayer p=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),name));p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            p.moveTo(Vec3.atBottomCenterOf(at));level.addNewPlayer(p);players.add(p);return p;
        }
        public void close(){
            for(ServerPlayer player:players){if(place==LabyrinthPlace.SHALLOWS)Shallows.onDepart(player);player.discard();}
            for(var p:List.of(LabyrinthPlace.PRESERVED_CAVE,LabyrinthPlace.SHALLOWS,LabyrinthPlace.DROWNED_TOWN)){
                BlockPos b=LabyrinthPlaces.base(origin,p);AABB box=IndianLakeRooms.bounds(b,p);
                for(Entity e:level.getEntitiesOfClass(Entity.class,box,e->e instanceof LakeCongregantEntity||e instanceof LakeWitchEntity||e instanceof LakeCanoeEntity||e.getTags().contains(Shallows.WORDS)))e.discard();
                for(int x=((int)box.minX-1)>>4;x<=((int)box.maxX+1)>>4;x++)for(int z=((int)box.minZ-1)>>4;z<=((int)box.maxZ+1)>>4;z++)level.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,b);
            }
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);
            PreservedCave.clearAll();LabyrinthDoors.clearAll();LabyrinthBuilder.clearAll();
        }
    }
    private static Fixture cave,throwing,upgrade;
    @AfterBatch(batch="lake_cave") public static void cleanCave(ServerLevel level){if(cave!=null){cave.close();cave=null;}}
    @AfterBatch(batch="lake_throw") public static void cleanThrow(ServerLevel level){if(throwing!=null){throwing.close();throwing=null;}}
    @AfterBatch(batch="lake_upgrade") public static void cleanUpgrade(ServerLevel level){if(upgrade!=null){upgrade.close();upgrade=null;}}
    @GameTest(template="empty",batch="lake_cave",timeoutTicks=200)
    public static void nativeQuietSoundsOccupiedVisitsRoofAndCanoePreserveTheSequence(GameTestHelper h){
        cave=new Fixture(h,new BlockPos(6800,80,6800),LabyrinthPlace.PRESERVED_CAVE);Fixture f=cave;var data=LabyrinthData.get(f.server);
        ServerPlayer player=f.player("quiet_explorer",f.base.offset(0,0,-12));PreservedCave.onArrive(player,LabyrinthPlace.PRESERVED_CAVE);
        var bodies=f.level.getEntitiesOfClass(LakeCongregantEntity.class,IndianLakeRooms.bounds(f.base,f.place),e->e.getTags().contains(PreservedCave.BODY));
        h.assertTrue(bodies.size()==24&&bodies.stream().allMatch(LakeCongregantEntity::seated),"six rows hold actual seated preserved bodies");
        for(int z=-12;z>=-39;z--)h.assertTrue(f.level.noCollision(player,new AABB(f.base.getX()+.2,f.base.getY(),f.base.getZ()+z+.2,f.base.getX()+.8,f.base.getY()+1.8,f.base.getZ()+z+.8)),"the central aisle can actually be walked");
        player.setShiftKeyDown(true);
        for(int i=0;i<30;i++)f.level.gameEvent(GameEvent.STEP,player.position(),GameEvent.Context.of(player));
        h.assertTrue(data.state(PreservedCave.ID).getDouble("Voices")==0,"native crouched footsteps do not wake the congregation");
        f.level.gameEvent(GameEvent.EAT,player.position(),GameEvent.Context.of(player));
        h.assertTrue(data.state(PreservedCave.ID).getDouble("Voices")>0,"crouching does not silence eating");
        player.setShiftKeyDown(false);
        for(int i=0;i<10;i++)f.level.gameEvent(GameEvent.STEP,player.position(),GameEvent.Context.of(player));
        h.assertTrue(bodies.stream().filter(b->!b.seated()).count()==8,"the front rows begin rising before the hymn is full");
        for(int i=0;i<15;i++)f.level.gameEvent(GameEvent.STEP,player.position(),GameEvent.Context.of(player));
        h.assertTrue(bodies.stream().noneMatch(LakeCongregantEntity::seated),"all eight voices raise the full congregation");
        // A second arrival cannot reset a room somebody is still crossing.
        ServerPlayer peer=f.player("later_explorer",f.base.offset(0,0,3));PreservedCave.onArrive(peer,LabyrinthPlace.PRESERVED_CAVE);
        h.assertTrue(data.state(PreservedCave.ID).getInt("Visit")==1&&data.state(PreservedCave.ID).getDouble("Voices")==8,"occupied arrivals preserve the current hymn");
        CompoundTag roof=data.state(DrownedTown.ID);roof.putBoolean("RoofOpened",true);data.setState(DrownedTown.ID,roof);
        PreservedCave.onArrive(peer,LabyrinthPlace.PRESERVED_CAVE);h.assertTrue(!IndianLakeProgress.deadOnShore(data),"opening the roof does not erase an occupied cave");
        // Leave entirely, then enter again: the permanent shore consequence is applied once.
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(40,0,1)));peer.moveTo(Vec3.atBottomCenterOf(f.base.offset(40,0,3)));
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-12)));PreservedCave.onArrive(player,LabyrinthPlace.PRESERVED_CAVE);
        h.assertTrue(IndianLakeProgress.deadOnShore(data)&&bodies.stream().allMatch(b->b.getZ()>f.base.getZ()-9&&!b.seated()),"the next visit empties the pews and physically puts the dead on shore");
        var canoe=f.level.getEntitiesOfClass(LakeCanoeEntity.class,IndianLakeRooms.bounds(f.base,f.place)).getFirst();
        ItemStack phone=new ItemStack(Items.PAPER);phone.set(DataComponents.CUSTOM_NAME,Component.literal("The actual dropped phone"));
        CustomData.update(DataComponents.CUSTOM_DATA,phone,t->t.putString("Footage","steeple; night; original frames"));PreservedCave.rememberDroppedPhone(player,phone);
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-40)));peer.moveTo(player.position());
        h.assertTrue(!PreservedCave.recoverPhone(peer),"another explorer cannot take the owner's recording");
        for(int i=0;i<player.getInventory().getContainerSize();i++)player.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        canoe.interact(player,InteractionHand.MAIN_HAND);h.assertTrue(PreservedCave.phoneWaiting(data,player.getUUID()),"a full inventory leaves the original phone safely in the canoe");
        player.getInventory().setItem(0,ItemStack.EMPTY);canoe.interact(player,InteractionHand.MAIN_HAND);
        h.assertTrue(ItemStack.isSameItemSameComponents(phone,player.getInventory().getItem(0))&&!PreservedCave.recoverPhone(player),"native canoe interaction returns every original component exactly once");
        h.assertTrue(!WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.PRESERVED_CAVE),"reaching the back has not yet completed the crossing");
        peer.moveTo(bodies.getFirst().position());var attack=new AttackEntityEvent(peer,bodies.getFirst());NeoForge.EVENT_BUS.post(attack);
        h.assertTrue(attack.isCanceled()&&data.state(PreservedCave.ID).getBoolean("Cold"),"attacking the preserved people ends this visit cold without destroying its actors");
        // This cold visit is not a crossing credit. A fresh empty-room arrival restores the scene's light.
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(42,0,1)));peer.moveTo(Vec3.atBottomCenterOf(f.base.offset(42,0,3)));
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-40)));PreservedCave.onArrive(player,LabyrinthPlace.PRESERVED_CAVE);canoe.interact(player,InteractionHand.MAIN_HAND);
        // Exercise the actual return doorway and its depart hook.
        data.pushReturn(player.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.INTERIOR,Vec3.atBottomCenterOf(f.base.offset(42,0,1)),0,true));
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-3)));LabyrinthDoors.tickPlayer(player,f.origin);
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,3)));LabyrinthDoors.tickPlayer(player,f.origin);
        h.assertTrue(WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.PRESERVED_CAVE)&&data.isCompleted(PreservedCave.ID)&&data.returnDepth(player.getUUID())==0,"walking back completes the witnessed aftermath and consumes its real return path");
        h.succeed();
    }
    @GameTest(template="empty",batch="lake_throw",timeoutTicks=450)
    public static void nativeCarryChargedThrowAndSplashNameOnlyItsOwner(GameTestHelper h){
        throwing=new Fixture(h,new BlockPos(7200,80,7200),LabyrinthPlace.SHALLOWS);Fixture f=throwing;LabyrinthData data=LabyrinthData.get(f.server);
        ServerPlayer owner=f.player("named_thrower",f.base.offset(2,0,-7)),peer=f.player("unhunted_peer",f.base.offset(4,0,-7));
        // Even a manually placed debug door must respect the hunt prerequisite.
        var gate=new LabyrinthData.Door("hunt_gate",HouseDimensions.INTERIOR,f.base.offset(42,0,1),Direction.SOUTH,"place:shallows",true);data.putDoor(gate);
        Vec3 before=peer.position();LabyrinthDoors.use(peer,gate);h.assertTrue(peer.position().equals(before)&&data.returnDepth(peer.getUUID())==0,"command doors cannot skip the personal hunt");
        IndianLakeProgress.hunted(data,owner.getUUID());Shallows.onArrive(owner,LabyrinthPlace.SHALLOWS);LakeWitchEntity girl=Shallows.actor(owner);
        h.assertTrue(girl!=null&&!Shallows.lift(peer,girl)&&!IndianLakeProgress.hasThrown(data,owner.getUUID()),"entering does not count a throw, and peers cannot lift this owner's actor");
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.TORCH));h.assertTrue(!Shallows.lift(owner,girl),"lifting really requires empty hands");owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        girl.interact(owner,InteractionHand.MAIN_HAND);
        h.assertTrue(owner.getMainHandItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get())&&girl.memoryPhase()==1,"native entity interaction lifts the actual recollection");
        // Abandoning a carry is retryable and cleans its temporary item.
        Shallows.onDepart(owner);h.assertTrue(!IndianLakeProgress.hasThrown(data,owner.getUUID())&&owner.getMainHandItem().isEmpty(),"an interrupted carry cannot grant a name or leave a tradeable grip");
        Shallows.onArrive(owner,LabyrinthPlace.SHALLOWS);girl=Shallows.actor(owner);girl.interact(owner,InteractionHand.MAIN_HAND);
        owner.getInventory().setItem(5,new ItemStack(DrownedTownRegistry.DRIED_ESSAY_ONE.get()));
        owner.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-12)));owner.setYRot(180);owner.setXRot(0);
        LakeWitchEntity falling=girl;int[] phase={0},started={0};
        h.onEachTick(()->{
            f.load();if(phase[0]==0&&falling.tickCount>=3){
                ItemStack held=owner.getMainHandItem();held.getItem().use(f.level,owner,InteractionHand.MAIN_HAND);
                held.getItem().releaseUsing(held,f.level,owner,72000-12);started[0]=falling.tickCount;phase[0]=1;
                h.assertTrue(falling.memoryPhase()==2&&!IndianLakeProgress.hasThrown(data,owner.getUUID()),"releasing the charged grip launches the body but does not assume a landing");
            }
            if(phase[0]!=1)return;
            h.assertTrue(falling.tickCount-started[0]<120,"native entity physics must reach water: position="+falling.position()+"; velocity="+falling.getDeltaMovement()+"; phase="+falling.memoryPhase()+"; noAI="+falling.isNoAi()+"; noGravity="+falling.isNoGravity());
            if(!IndianLakeProgress.hasThrown(data,owner.getUUID()))return;
            h.assertTrue(falling.memoryPhase()==3&&falling.getZ()<=f.base.getZ()-14,"the actual splash finishes the throwing beat");
            var book=owner.getInventory().getItem(5).get(DataComponents.WRITTEN_BOOK_CONTENT);
            h.assertTrue(book.pages().getLast().raw().getString().contains("named_thrower"),"a carried dried essay gains the player's name immediately");
            h.assertTrue(WitnessAccount.has(data,owner.getUUID(),WitnessAccount.Story.SHALLOWS)&&!WitnessAccount.has(data,peer.getUUID(),WitnessAccount.Story.SHALLOWS)
                    &&!IndianLakeProgress.hasThrown(data,peer.getUUID())&&!LabyrinthDealer.vignettesAvailable(data,owner.getUUID()).contains(LabyrinthPlace.SHALLOWS),"resolution belongs to its participant and exhausts only their scene");
            h.assertTrue(!Shallows.throwGirl(owner,new ItemStack(DrownedTownRegistry.SHALLOWS_BURDEN.get())),"a forged or stale grip cannot repeat the throw");phase[0]=2;h.succeed();
        });
    }
    @GameTest(template="empty",batch="lake_upgrade",timeoutTicks=200)
    public static void actualFourteenToFifteenUpgradeKeepsTheSchoolsFiniteInventory(GameTestHelper h){
        upgrade=new Fixture(h,new BlockPos(7600,80,7600),LabyrinthPlace.DROWNED_TOWN);Fixture f=upgrade;LabyrinthData data=LabyrinthData.get(f.server);
        var desk=(BarrelBlockEntity)f.level.getBlockEntity(f.base.offset(DrownedTown.PAPERS[0]));desk.setItem(0,ItemStack.EMPTY);desk.setItem(3,new ItemStack(Items.DIAMOND));desk.setChanged();
        CompoundTag state=data.state(DrownedTown.ID);state.putInt("Visit",2);state.putInt("DryMask",7);state.putBoolean("ChurchUnlocked",true);data.setState(DrownedTown.ID,state);
        data.setBuilt(14,f.origin);h.assertTrue(!LabyrinthBuilder.ensureBuilt(f.server),"an older stack begins its incremental upgrade");
        while(LabyrinthBuilder.isCarving())LabyrinthBuilder.tick(f.server);
        h.assertTrue(data.builtVersion()==15&&f.level.getBlockEntity(f.base.offset(DrownedTown.PAPERS[0]))==desk&&desk.getItem(0).isEmpty()&&desk.getItem(3).is(Items.DIAMOND),"upgrade never rebuilds or restocks the existing school");
        h.assertTrue(data.state(DrownedTown.ID).getInt("Visit")==2&&data.state(DrownedTown.ID).getBoolean("ChurchUnlocked")
                &&data.door(LabyrinthPlace.PRESERVED_CAVE.entryDoorId())!=null&&data.door(LabyrinthPlace.SHALLOWS.entryDoorId())!=null,"new native doorways are added while church progress stays intact");h.succeed();
    }
}
