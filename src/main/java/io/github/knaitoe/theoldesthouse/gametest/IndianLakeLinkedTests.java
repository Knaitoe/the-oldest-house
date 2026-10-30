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
        final GameTestHelper helper;
        final MinecraftServer server;final ServerLevel level;final HouseSavedData oldHouse;final LabyrinthData oldData;
        final BlockPos origin,base;final LabyrinthPlace place;final List<ServerPlayer> players=new ArrayList<>();
        Fixture(GameTestHelper h,BlockPos origin,LabyrinthPlace place){this(h,origin,place,false);}
        /** {@code legacy}: the town as it stood before 0.4.67, its raised school included, for a saved-world rebuild. */
        Fixture(GameTestHelper h,BlockPos origin,LabyrinthPlace place,boolean legacy){
            helper=h;
            server=h.getLevel().getServer();level=HouseTestLevel.get(server,NovelRooms.dimension(place));this.origin=origin;this.place=place;oldHouse=HouseSavedData.get(server);oldData=LabyrinthData.get(server);
            var house=new HouseSavedData();house.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",house);
            var data=new LabyrinthData();data.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",data);
            base=LabyrinthPlaces.base(origin,place);
            if(place==LabyrinthPlace.PRESERVED_CAVE)PreservedCave.build(server,level,base);
            else if(place==LabyrinthPlace.SHALLOWS)Shallows.build(server,level,base);
            else if(legacy){DrownedTownArchitecture.build(level,base);LakeLandscape.liftSchool(level,base);}
            else DrownedTown.build(server,level,base);
            LabyrinthBuilder.registerDoors(data,place,base);load();
        }
        void load(){IndianLakeRooms.keepLoaded(level,base,place);}
        ServerPlayer player(String name,BlockPos at){
            ServerPlayer p=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),name));p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            p.moveTo(Vec3.atBottomCenterOf(at));level.addNewPlayer(p);players.add(p);return p;
        }
        ServerPlayer realPlayer(String name,BlockPos at){
            var p=NativeTestPlayers.survival(helper,name);p.teleportTo(level,at.getX()+.5,at.getY(),at.getZ()+.5,0,0);p.hasChangedDimension();p.setNoGravity(true);players.add(p);return p;
        }
        public void close(){
            for(ServerPlayer player:players){if(place==LabyrinthPlace.SHALLOWS)Shallows.onDepart(player);if(player instanceof net.neoforged.neoforge.common.util.FakePlayer)player.discard();else NativeTestPlayers.remove(player);}
            for(var p:List.of(LabyrinthPlace.PRESERVED_CAVE,LabyrinthPlace.SHALLOWS,LabyrinthPlace.DROWNED_TOWN)){
                BlockPos b=LabyrinthPlaces.base(origin,p);AABB box=IndianLakeRooms.bounds(b,p);
                for(Entity e:level.getEntitiesOfClass(Entity.class,box,e->e instanceof LakeCongregantEntity||e instanceof LakeWitchEntity||e instanceof LakeCanoeEntity||e.getTags().contains(Shallows.WORDS)))e.discard();
                for(int x=((int)box.minX-1)>>4;x<=((int)box.maxX+1)>>4;x++)for(int z=((int)box.minZ-1)>>4;z<=((int)box.maxZ+1)>>4;z++)level.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,b);
            }
            server.overworld().getDataStorage().set("the_oldest_house",oldHouse);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",oldData);
            PreservedCave.clearAll();LabyrinthDoors.clearAll();LabyrinthBuilder.clearAll();
        }
    }
    private static Fixture cave,throwing,upgrade,letGo;
    private static Fixture originalCave;
    private static Fixture originalTown,originalShallows;
    private static NativeTestChunks originalTownChunks;
    @AfterBatch(batch="town_originals0493") public static void cleanOriginalTown(ServerLevel level){if(originalTownChunks!=null){originalTownChunks.close();originalTownChunks=null;}if(originalTown!=null){originalTown.close();originalTown=null;}}
    @AfterBatch(batch="shallows_originals0493") public static void cleanOriginalShallows(ServerLevel level){if(originalShallows!=null){originalShallows.close();originalShallows=null;}}
    @GameTest(template="empty",batch="town_originals0493",timeoutTicks=1200)
    public static void originalChurchResidentsSurviveLegacyAdoptionAndALateEntityLoad(GameTestHelper h){
        originalTown=new Fixture(h,new BlockPos(47900,80,47900),LabyrinthPlace.DROWNED_TOWN);var f=originalTown;var bounds=IndianLakeRooms.bounds(f.base,f.place);
        var original=f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains("the_oldest_house_indian_lake_body_0")).getFirst();
        original.setCustomName(Component.literal("The original preacher"));original.setHealth(13);var id=original.getUUID();
        var removed=f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains("the_oldest_house_indian_lake_body_1")).getFirst();removed.discard();
        var data=LabyrinthData.get(f.server);var state=data.state(DrownedTown.ID);state.remove("NativeOriginals0493");state.remove("FreshCast0493");data.setState(DrownedTown.ID,state);
        originalTownChunks=new NativeTestChunks();originalTownChunks.hold(f.level,bounds);boolean[] started={false};
        h.onEachTick(()->{
            if(started[0]){DrownedTown.stage(f.level,f.base,LabyrinthData.get(f.server));return;}
            if(!originalTownChunks.ready())return;started[0]=true;DrownedTown.stage(f.level,f.base,data);
            h.assertTrue(data.state(DrownedTown.ID).getCompound("NativeOriginals0493").getCompound("body0").getUUID("Id").equals(id)&&f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains("the_oldest_house_indian_lake_body")).size()==6,"the loaded church adopts its original residents without refilling a removed legacy body");
            var saved=original.saveWithoutId(new CompoundTag());original.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.load(data.save(new CompoundTag(),f.level.registryAccess()),f.level.registryAccess()));
            h.runAfterDelay(140,()->{
                h.assertTrue(f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains("the_oldest_house_indian_lake_body")).size()==5,"repeated native church restaging cannot replace its late original");
                var returned=DrownedTownRegistry.CONGREGANT.get().create(f.level);returned.load(saved);h.assertTrue(f.level.addFreshEntity(returned),"the serialized original preacher returns under its actual UUID");DrownedTown.stage(f.level,f.base,LabyrinthData.get(f.server));
                h.assertTrue(returned.getUUID().equals(id)&&returned.getHealth()==13&&returned.getName().getString().equals("The original preacher")&&returned.preacher(),"church posing keeps the actual UUID, name and health after SavedData reload");h.succeed();
            });
        });
    }
    @GameTest(template="empty",batch="shallows_originals0493",timeoutTicks=350)
    public static void aReaderWhoJoinsSharesTheRecollectionAndItReusesItsLateOriginals(GameTestHelper h){
        originalShallows=new Fixture(h,new BlockPos(48700,80,48700),LabyrinthPlace.SHALLOWS);var f=originalShallows;var data=LabyrinthData.get(f.server);var bounds=IndianLakeRooms.bounds(f.base,f.place);
        var owner=f.realPlayer("memory_owner",f.base.offset(-1,0,-11));var peer=f.realPlayer("memory_peer",f.base.offset(-8,0,-11));var stranger=f.player("memory_stranger",f.base.offset(8,0,-6));
        IndianLakeProgress.hunted(data,owner.getUUID());IndianLakeProgress.hunted(data,peer.getUUID());Shallows.onArrive(owner,f.place);Shallows.onArrive(peer,f.place);
        // 0.4.157: a reader who comes while another's recollection is under way joins it (docs/SHALLOWS_TOGETHER_0_4_157.md).
        var original=Shallows.actor(owner);
        h.assertTrue(original!=null&&Shallows.actor(peer)==null&&Shallows.memory(peer)==original&&original.broadcastToPlayer(owner)&&original.broadcastToPlayer(peer)&&!original.broadcastToPlayer(stranger),"the reader who joins shares the one recollection, and nobody else is sent her");
        var shared=DrownedTownRegistry.LAKE_WITCH.get().create(f.level);h.assertTrue(shared.broadcastToPlayer(owner)&&shared.broadcastToPlayer(peer),"the real shared lake witch retains ordinary native tracking");shared.discard();
        var ownBoy=Shallows.carrier(owner);
        h.assertTrue(ownBoy!=null&&Shallows.carrier(peer)==null&&ownBoy.broadcastToPlayer(owner)&&ownBoy.broadcastToPlayer(peer)&&!ownBoy.broadcastToPlayer(stranger)
                &&Shallows.AT_FEET.equals(Shallows.personal(data,peer.getUUID()).getString(Shallows.ROLE))&&Shallows.personal(data,owner.getUUID()).getInt("Slot")==Shallows.personal(data,peer.getUUID()).getInt("Slot"),"the one who joins is to hold her feet; Lonnie is sent to both and they share one carry place");
        original.setCustomName(Component.literal("The original recollection"));var id=original.getUUID();var saved=original.saveWithoutId(new CompoundTag());original.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        var boy=f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains(Shallows.BOY+"0")).getFirst();boy.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.load(data.save(new CompoundTag(),f.level.registryAccess()),f.level.registryAccess()));
        h.onEachTick(()->{Shallows.onArrive(owner,f.place);Shallows.onArrive(peer,f.place);});
        h.runAfterDelay(140,()->{
            var loaded=LabyrinthData.get(f.server);
            h.assertTrue(Shallows.actor(owner)==null&&Shallows.personal(loaded,owner.getUUID()).getUUID("Actor").equals(id)&&f.level.getEntitiesOfClass(LakeWitchEntity.class,bounds,LakeWitchEntity::memory).isEmpty()&&f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains(Shallows.BOY)).size()==2
                    &&owner.getUUID().equals(Shallows.with(loaded,peer.getUUID()))&&!Shallows.personal(loaded,peer.getUUID()).hasUUID("Actor"),"repeated actual arrivals retain the absent native girl and boy, and the one who joined stays with it without a recollection of their own");
            var returned=DrownedTownRegistry.LAKE_WITCH.get().create(f.level);returned.load(saved);h.assertTrue(f.level.addFreshEntity(returned),"the saved native memory UUID returns without replacement conflict");Shallows.onArrive(owner,f.place);
            h.assertTrue(Shallows.actor(owner)==returned&&returned.getUUID().equals(id)&&returned.getName().getString().equals("The original recollection")&&Shallows.memory(peer)==returned&&Shallows.lift(owner,returned)&&!IndianLakeProgress.hasThrown(loaded,owner.getUUID()),"the original remains liftable after restart, still shared, without awarding a throw");h.succeed();
        });
    }
    private static Fixture together;
    @AfterBatch(batch="shallows_together157") public static void cleanTogether(ServerLevel level){if(together!=null){together.close();together=null;}}
    /** 0.4.157: one throw for everyone on the bank. The thrower's partner holds her feet, the rest watch, and each keeps their own line. */
    @GameTest(template="empty",batch="shallows_together157",timeoutTicks=900)
    public static void friendsOnTheBankThrowHerTogetherAndEachKeepsTheirOwnLine(GameTestHelper h){
        together=new Fixture(h,new BlockPos(50300,80,50300),LabyrinthPlace.SHALLOWS);var f=together;var data=LabyrinthData.get(f.server);
        ServerPlayer thrower=f.player("pair_thrower",f.base.offset(-1,0,-11)),feet=f.player("pair_feet",f.base.offset(2,0,-11)),bank=f.player("pair_bank",f.base.offset(-6,0,-5)),stranger=f.player("pair_stranger",f.base.offset(8,0,-6));
        IndianLakeProgress.hunted(data,thrower.getUUID());Shallows.onArrive(thrower,LabyrinthPlace.SHALLOWS);Shallows.onArrive(feet,LabyrinthPlace.SHALLOWS);Shallows.onArrive(bank,LabyrinthPlace.SHALLOWS);
        LakeWitchEntity girl=Shallows.actor(thrower);LakeCongregantEntity lonnie=Shallows.carrier(thrower);
        h.assertTrue(girl!=null&&lonnie!=null&&Shallows.actor(feet)==null&&Shallows.actor(bank)==null,"one recollection: the first reader's, with nobody else given one of their own, hunted or not");
        h.assertTrue(Shallows.AT_FEET.equals(Shallows.personal(data,feet.getUUID()).getString(Shallows.ROLE))&&Shallows.ON_BANK.equals(Shallows.personal(data,bank.getUUID()).getString(Shallows.ROLE))
                &&thrower.getUUID().equals(Shallows.with(data,feet.getUUID()))&&thrower.getUUID().equals(Shallows.with(data,bank.getUUID())),"the first to join holds her feet and the next stands on the bank");
        h.assertTrue(girl.broadcastToPlayer(feet)&&girl.broadcastToPlayer(bank)&&lonnie.broadcastToPlayer(bank)&&!girl.broadcastToPlayer(stranger)&&!lonnie.broadcastToPlayer(stranger),"everyone with her sees her and Lonnie; nobody else does");
        h.assertTrue(!Shallows.lift(feet,girl)&&girl.memoryPhase()==Shallows.LYING,"her feet wait until someone has her arms");
        bank.getInventory().setItem(5,new ItemStack(DrownedTownRegistry.DRIED_ESSAY_ONE.get()));
        int[] step={0},presses={0},wait={0};
        h.onEachTick(()->{
            f.load();if(girl.tickCount<3)return;var slot=Shallows.personal(data,thrower.getUUID()).getInt("Slot");
            switch(step[0]){
                case 0->{
                    h.assertTrue(lonnie.position().distanceToSqr(Shallows.edge(f.base,slot))<.05&&lonnie.carry()==0,"with a reader at her feet, Lonnie stands at the water's edge to count");
                    girl.interact(thrower,InteractionHand.MAIN_HAND);
                    h.assertTrue(girl.memoryPhase()==Shallows.CARRIED&&thrower.getMainHandItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get())&&!Shallows.swing(thrower,-1)&&Shallows.count(girl)==0,"the thrower takes her wrists, but she does not swing until someone has her feet");
                    h.assertTrue(!Shallows.lift(bank,girl)&&!Shallows.swing(bank,-1),"a reader on the bank cannot take hold of her");
                    h.assertTrue(Shallows.lift(feet,girl)&&feet.getMainHandItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get())&&feet.position().distanceToSqr(Shallows.carrierStance(f.base,slot))<.01&&Math.abs(feet.getYRot()-Shallows.CARRIER_YAW)<1,"the partner takes her feet at the east end of her, facing the thrower");
                    step[0]=1;
                }
                case 1->{
                    // Letting go of her feet puts her down; both take hold again.
                    feet.setShiftKeyDown(true);wait[0]++;
                    if(girl.memoryPhase()==Shallows.CARRIED){h.assertTrue(wait[0]<5,"sneaking at her feet lets go");return;}
                    feet.setShiftKeyDown(false);
                    h.assertTrue(thrower.getMainHandItem().isEmpty()&&feet.getMainHandItem().isEmpty()&&lonnie.position().distanceToSqr(Shallows.edge(f.base,slot))<.05,"when her feet are let go she is put down, both grips are gone and Lonnie stays at the edge");
                    girl.interact(thrower,InteractionHand.MAIN_HAND);h.assertTrue(Shallows.lift(feet,girl),"both take hold of her again");step[0]=2;
                }
                case 2->{
                    if(!Shallows.readyForStroke(girl))return;
                    h.assertTrue(!Shallows.swing(thrower,-1)&&Shallows.count(girl)==0,"one pair of hands alone is not a stroke");
                    h.assertTrue(Shallows.swing(feet,1)&&Shallows.count(girl)==1,"with both swinging toward the water, the lake on the thrower's left and the partner's right, she goes out on one");
                    presses[0]=1;step[0]=3;
                }
                case 3->{
                    if(!Shallows.readyForStroke(girl))return;
                    h.assertTrue(!Shallows.swing(thrower,1)&&!Shallows.swing(feet,1)&&Shallows.count(girl)==1,"swinging different ways is not a stroke");
                    step[0]=4;
                }
                case 4->{
                    if(girl.memoryPhase()==Shallows.CARRIED){
                        if(!Shallows.readyForStroke(girl))return;boolean back=presses[0]%2==1;
                        Shallows.swing(thrower,back?1:-1);if(Shallows.swing(feet,back?-1:1))presses[0]++;
                        h.assertTrue(presses[0]<=5&&!IndianLakeProgress.hasThrown(data,thrower.getUUID()),"three strokes out together release her, never more");return;
                    }
                    h.assertTrue(girl.memoryPhase()==Shallows.FLYING&&presses[0]==5&&thrower.getMainHandItem().isEmpty()&&feet.getMainHandItem().isEmpty(),"on three both let go and both grips are gone: phase="+girl.memoryPhase()+" presses="+presses[0]);
                    step[0]=5;
                }
                case 5->{
                    if(!IndianLakeProgress.hasThrown(data,thrower.getUUID()))return;
                    h.assertTrue(WitnessAccount.has(data,thrower.getUUID(),WitnessAccount.Story.SHALLOWS)&&WitnessAccount.has(data,feet.getUUID(),WitnessAccount.Story.SHALLOWS)&&WitnessAccount.has(data,bank.getUUID(),WitnessAccount.Story.SHALLOWS)&&!WitnessAccount.has(data,stranger.getUUID(),WitnessAccount.Story.SHALLOWS),"everyone with her keeps the shallows; nobody else does");
                    h.assertTrue(WitnessAccount.role(data,feet.getUUID(),WitnessAccount.Story.SHALLOWS)==VignetteRole.ACTOR&&WitnessAccount.role(data,bank.getUUID(),WitnessAccount.Story.SHALLOWS)==VignetteRole.WITNESS,"the partner threw her too; the bank watched");
                    h.assertTrue(IndianLakeProgress.hasThrown(data,feet.getUUID())&&IndianLakeProgress.hasThrown(data,bank.getUUID())&&!LabyrinthDealer.vignettesAvailable(data,feet.getUUID()).contains(LabyrinthPlace.SHALLOWS),"the school pages keep every one of them, and the shallows are not dealt to them again");
                    var mine=Shallows.witness(Shallows.personal(data,thrower.getUUID()),"");var theirs=Shallows.witness(Shallows.personal(data,feet.getUUID()),"");var watched=Shallows.witness(Shallows.personal(data,bank.getUUID()),"");
                    h.assertTrue(mine.contains("while pair_feet held her feet")&&theirs.startsWith("I held her feet while pair_thrower")&&watched.startsWith("I stood on the bank")&&watched.contains("pair_thrower and pair_feet"),"each line says how they were part of it: "+mine+" | "+theirs+" | "+watched);
                    var essay=bank.getInventory().getItem(5).get(DataComponents.WRITTEN_BOOK_CONTENT).pages().getLast().raw().getString();
                    h.assertTrue(essay.contains("pair_thrower")&&essay.contains("pair_bank"),"a carried dried essay names everyone on the bank: "+essay);
                    h.succeed();
                }
                default->{}
            }
        });
    }
    private static NativeTestChunks originalCaveChunks;
    @AfterBatch(batch="cave_originals0493") public static void cleanOriginalCave(ServerLevel level){
        if(originalCaveChunks!=null){originalCaveChunks.close();originalCaveChunks=null;}
        if(originalCave!=null){originalCave.close();originalCave=null;}
    }
    @GameTest(template="empty",batch="cave_originals0493",timeoutTicks=1200)
    public static void caveAdoptsLegacyOriginalsAndNeverReplacesLateBodiesOrAnotherCanoe(GameTestHelper h){
        originalCave=new Fixture(h,new BlockPos(46300,80,46300),LabyrinthPlace.PRESERVED_CAVE);var f=originalCave;
        var bounds=IndianLakeRooms.bounds(f.base,f.place);originalCaveChunks=new NativeTestChunks();originalCaveChunks.hold(f.level,bounds);
        var original=f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains(PreservedCave.BODY+"0")).getFirst();
        original.setCustomName(Component.literal("The original in the front pew"));original.setHealth(13);original.preservedEra(3);var id=original.getUUID();
        var removed=f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains(PreservedCave.BODY+"1")).getFirst();removed.discard();
        var canoe=f.level.getEntitiesOfClass(LakeCanoeEntity.class,bounds,e->e.getTags().contains(PreservedCave.CANOE_TAG)).getFirst();var canoeId=canoe.getUUID();
        var foreign=DrownedTownRegistry.CAVE_CANOE.get().create(f.level);foreign.moveTo(Vec3.atBottomCenterOf(f.base.offset(2,0,-41)));foreign.setCustomName(Component.literal("A visitor's canoe"));f.level.addFreshEntity(foreign);
        var data=LabyrinthData.get(f.server);var state=data.state(PreservedCave.ID);
        for(String key:List.of("BodyOriginals0493","BodiesRecorded0493","CanoeOriginal0493","CanoeRecorded0493","FreshOriginals0493"))state.remove(key);
        data.setState(PreservedCave.ID,state);boolean[] started={false};
        h.onEachTick(()->{
            if(started[0]){PreservedCave.stage(f.level,f.base,LabyrinthData.get(f.server));return;}
            if(!originalCaveChunks.ready())return;started[0]=true;PreservedCave.stage(f.level,f.base,data);
            h.assertTrue(data.state(PreservedCave.ID).getCompound("BodyOriginals0493").getUUID("0").equals(id)&&data.state(PreservedCave.ID).getUUID("CanoeOriginal0493").equals(canoeId),"a fully loaded legacy scene adopts its actual native originals");
            h.assertTrue(f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains(PreservedCave.BODY)).size()==23&&!foreign.isRemoved(),"a removed legacy body stays absent and another canoe is not erased");
            var bodySave=original.saveWithoutId(new CompoundTag());var canoeSave=canoe.saveWithoutId(new CompoundTag());
            original.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);canoe.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
            f.server.overworld().getDataStorage().set("the_oldest_house_labyrinth",LabyrinthData.load(data.save(new CompoundTag(),f.level.registryAccess()),f.level.registryAccess()));
            h.runAfterDelay(140,()->{
                var reloaded=LabyrinthData.get(f.server);
                h.assertTrue(f.level.getEntitiesOfClass(LakeCongregantEntity.class,bounds,e->e.getTags().contains(PreservedCave.BODY)).size()==22&&f.level.getEntitiesOfClass(LakeCanoeEntity.class,bounds,e->e.getTags().contains(PreservedCave.CANOE_TAG)).isEmpty()&&!foreign.isRemoved(),"five seconds of native restaging and SavedData reload cannot replace either late original or consume a visitor's canoe");
                var returned=DrownedTownRegistry.CONGREGANT.get().create(f.level);returned.load(bodySave);
                var returnedCanoe=DrownedTownRegistry.CAVE_CANOE.get().create(f.level);returnedCanoe.load(canoeSave);
                h.assertTrue(f.level.addFreshEntity(returned)&&f.level.addFreshEntity(returnedCanoe),"the same serialized native UUIDs return without replacement conflicts");
                PreservedCave.stage(f.level,f.base,reloaded);
                h.assertTrue(returned.getUUID().equals(id)&&returned.getHealth()==13&&returned.preservedEra()==3&&returned.getName().getString().equals("The original in the front pew")&&returnedCanoe.getUUID().equals(canoeId)&&!foreign.isRemoved(),"restaging retains the original body identity, name, health and era and the exact original canoe");h.succeed();
            });
        });
    }
    @AfterBatch(batch="lake_cave") public static void cleanCave(ServerLevel level){if(cave!=null){cave.close();cave=null;}}
    @AfterBatch(batch="lake_throw") public static void cleanThrow(ServerLevel level){if(throwing!=null){throwing.close();throwing=null;}}
    @AfterBatch(batch="lake_upgrade") public static void cleanUpgrade(ServerLevel level){if(upgrade!=null){upgrade.close();upgrade=null;}}
    @GameTest(template="empty",batch="lake_cave",timeoutTicks=200)
    public static void nativeQuietSoundsOccupiedVisitsRoofAndCanoePreserveTheSequence(GameTestHelper h){
        cave=new Fixture(h,new BlockPos(6800,80,6800),LabyrinthPlace.PRESERVED_CAVE);Fixture f=cave;var data=LabyrinthData.get(f.server);
        ServerPlayer player=f.realPlayer("quiet_explorer",f.base.offset(0,0,-12));PreservedCave.onArrive(player,LabyrinthPlace.PRESERVED_CAVE);
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
        ServerPlayer peer=f.realPlayer("later_explorer",f.base.offset(0,0,3));PreservedCave.onArrive(peer,LabyrinthPlace.PRESERVED_CAVE);
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
        var destination=HouseTestLevel.get(f.server,HouseDimensions.OUTSIDE);
        data.pushReturn(player.getUUID(),new LabyrinthData.Waypoint(HouseDimensions.OUTSIDE,Vec3.atBottomCenterOf(f.base.offset(42,0,1)),0,true));
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-3)));LabyrinthDoors.tickPlayer(player,f.origin);
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,3)));LabyrinthDoors.tickPlayer(player,f.origin);
        h.assertTrue(io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents.isPending(player)&&data.returnDepth(player.getUUID())==1&&!WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.PRESERVED_CAVE),"a prepared return retains its exact route and grants no premature ending credit");
        io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents.cancelPending(player,"native cave retry");
        h.assertTrue(data.returnDepth(player.getUUID())==1&&!WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.PRESERVED_CAVE),"canceling the return cannot discard the route or grant its ending");
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,-3)));LabyrinthDoors.tickPlayer(player,f.origin);
        player.moveTo(Vec3.atBottomCenterOf(f.base.offset(0,0,3)));LabyrinthDoors.tickPlayer(player,f.origin);
        peer.moveTo(bodies.getFirst().position());NeoForge.EVENT_BUS.post(new AttackEntityEvent(peer,bodies.getFirst()));
        h.assertTrue(data.state(PreservedCave.ID).getBoolean("Cold"),"a peer can alter the shared room while this reader's actual return is pending");
        h.runAfterDelay(1,()->io.github.knaitoe.theoldesthouse.house.HouseTransitionEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player)));
        h.runAfterDelay(3,()->{h.assertTrue(player.serverLevel()==destination&&WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.PRESERVED_CAVE)&&data.isCompleted(PreservedCave.ID)&&data.returnDepth(player.getUUID())==0,"the actual native arrival grants the personally witnessed aftermath and consumes its real return path once");h.succeed();});
    }
    @GameTest(template="empty",batch="lake_throw",timeoutTicks=700)
    public static void swungOutOnThreeSheBreaksOnTheWaterAndOnlyItsOwnerIsNamed(GameTestHelper h){
        throwing=new Fixture(h,new BlockPos(7200,80,7200),LabyrinthPlace.SHALLOWS);Fixture f=throwing;LabyrinthData data=LabyrinthData.get(f.server);
        ServerPlayer owner=f.player("named_thrower",f.base.offset(-1,0,-11)),peer=f.player("unhunted_peer",f.base.offset(4,0,-7));
        // Even a manually placed debug door must respect the hunt prerequisite.
        var gate=new LabyrinthData.Door("hunt_gate",HouseDimensions.OUTSIDE,f.base.offset(42,0,1),Direction.SOUTH,"place:shallows",true);data.putDoor(gate);
        Vec3 before=peer.position();LabyrinthDoors.use(peer,gate);h.assertTrue(peer.position().equals(before)&&data.returnDepth(peer.getUUID())==0,"command doors cannot skip the personal hunt");
        IndianLakeProgress.hunted(data,owner.getUUID());Shallows.onArrive(owner,LabyrinthPlace.SHALLOWS);LakeWitchEntity girl=Shallows.actor(owner);LakeCongregantEntity boy=Shallows.carrier(owner);
        h.assertTrue(girl!=null&&boy!=null&&girl.memoryPhase()==Shallows.LYING&&boy.carry()==Shallows.HOLD&&boy.broadcastToPlayer(owner)&&!boy.broadcastToPlayer(peer),"she lies on the bank with a boy holding her feet whom only this reader is sent");
        h.assertTrue(!Shallows.lift(peer,girl)&&!IndianLakeProgress.hasThrown(data,owner.getUUID()),"entering does not count a throw, and peers cannot take this owner's recollection");
        owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.TORCH));h.assertTrue(!Shallows.lift(owner,girl),"taking her wrists really requires empty hands");owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        girl.interact(owner,InteractionHand.MAIN_HAND);
        var stance=Shallows.stance(f.base,0);
        h.assertTrue(owner.getMainHandItem().is(DrownedTownRegistry.SHALLOWS_BURDEN.get())&&girl.memoryPhase()==Shallows.CARRIED&&boy.carry()==Shallows.SWINGING
                &&owner.position().distanceToSqr(stance)<.01&&Math.abs(owner.getYRot()-Shallows.READER_YAW)<1,"native entity interaction puts the reader at her wrists, facing the boy at her feet");
        // Abandoning a carry is retryable and cleans its temporary grip and both private people.
        Shallows.onDepart(owner);h.assertTrue(!IndianLakeProgress.hasThrown(data,owner.getUUID())&&owner.getMainHandItem().isEmpty()&&girl.isRemoved()&&boy.isRemoved(),"an interrupted carry cannot grant a name or leave a grip or a boy behind");
        Shallows.onArrive(owner,LabyrinthPlace.SHALLOWS);girl=Shallows.actor(owner);girl.interact(owner,InteractionHand.MAIN_HAND);
        owner.getInventory().setItem(5,new ItemStack(DrownedTownRegistry.DRIED_ESSAY_ONE.get()));
        LakeWitchEntity flying=girl;int[] step={0},presses={0},lowest={0};double[] lastZ={0};Vec3[] still={null};
        double surface=f.base.getY()-1+8/9D;
        LakeWitchEntity stale=DrownedTownRegistry.LAKE_WITCH.get().create(f.level);stale.recollection(owner.getUUID(),f.base);
        stale.moveTo(Vec3.atBottomCenterOf(f.base.offset(2,0,-8)));f.level.addFreshEntity(stale);
        h.onEachTick(()->{
            f.load();if(flying.tickCount<3)return;
            if(stale.tickCount>0)h.assertTrue(stale.isRemoved(),"a late-loaded actor from an old visit cannot duplicate the current memory");
            if(step[0]==0){
                h.assertTrue(Shallows.swing(owner,-1)&&Shallows.count(flying)==1&&!Shallows.swing(owner,-1),"the left key swings her out once; the same key again is not a stroke");
                step[0]=1;presses[0]=1;return;
            }
            if(step[0]==1){
                // Alternate keys as each stroke allows: back, out (two), back, out (three) and both let go.
                if(flying.memoryPhase()==Shallows.CARRIED){
                    if(Shallows.readyForStroke(flying)&&Shallows.swing(owner,presses[0]%2==1?1:-1))presses[0]++;
                    h.assertTrue(presses[0]<=5&&!IndianLakeProgress.hasThrown(data,owner.getUUID()),"three strokes out release her, never more and never a credit while she is still held");
                    if(presses[0]%2==0)h.assertTrue(Shallows.count(flying)==presses[0]/2,"the boy counts only the strokes out");
                    return;
                }
                h.assertTrue(flying.memoryPhase()==Shallows.FLYING&&presses[0]==5&&owner.getMainHandItem().isEmpty(),"on three both let go mid-swing and the grip is gone: phase="+flying.memoryPhase()+" presses="+presses[0]);
                step[0]=2;return;
            }
            if(step[0]==2){
                h.assertTrue(flying.getY()>=surface-.01||flying.getZ()>f.base.getZ()-13,"the lake never takes her: y="+flying.getY()+" z="+flying.getZ());
                if(!IndianLakeProgress.hasThrown(data,owner.getUUID())){h.assertTrue(flying.memoryPhase()==Shallows.FLYING&&flying.memoryTicks<60,"thrown, she reaches the water: "+flying.position());return;}
                h.assertTrue(flying.memoryPhase()==Shallows.BROKEN&&flying.getZ()<=f.base.getZ()-14&&Math.abs(flying.getY()-surface)<.02,"she breaks on the water's surface well out from the bank: "+flying.position());
                var book=owner.getInventory().getItem(5).get(DataComponents.WRITTEN_BOOK_CONTENT);
                h.assertTrue(book.pages().getLast().raw().getString().contains("named_thrower"),"a carried dried essay gains the player's name immediately");
                h.assertTrue(WitnessAccount.has(data,owner.getUUID(),WitnessAccount.Story.SHALLOWS)&&!WitnessAccount.has(data,peer.getUUID(),WitnessAccount.Story.SHALLOWS)
                        &&!IndianLakeProgress.hasThrown(data,peer.getUUID())&&!LabyrinthDealer.vignettesAvailable(data,owner.getUUID()).contains(LabyrinthPlace.SHALLOWS),"resolution belongs to its participant and exhausts only their scene");
                h.assertTrue(!Shallows.swing(owner,-1),"a finished throw takes no more strokes");
                step[0]=3;return;
            }
            if(step[0]==3){
                h.assertTrue(!flying.isRemoved(),"the aftermath plays out before she goes");
                if(flying.memoryPhase()==Shallows.BROKEN){if(still[0]==null)still[0]=flying.position();h.assertTrue(flying.position().distanceToSqr(still[0])<1e-6,"she lies still before she moves");return;}
                if(flying.memoryPhase()==Shallows.RISING){lastZ[0]=flying.getZ();return;}
                if(flying.memoryPhase()==Shallows.SCURRY){
                    // The tick she turns from rising to running she has not stepped yet; she never comes back toward the bank.
                    h.assertTrue(flying.getZ()<=lastZ[0]&&(flying.getY()>=surface-.01||flying.getZ()<f.base.getZ()-35),"she runs away across the water's surface, toward the far woods: "+flying.position());
                    if(flying.getZ()<lastZ[0])lowest[0]++;lastZ[0]=flying.getZ();return;
                }
                h.assertTrue(flying.memoryPhase()==Shallows.GONE&&lowest[0]>20&&flying.getZ()<=f.base.getZ()-34,"she reaches the woods across the lake: "+flying.position());
                step[0]=4;h.succeed();
            }
        });
    }
    @AfterBatch(batch="lake_letgo") public static void cleanLetGo(ServerLevel level){if(letGo!=null){letGo.close();letGo=null;}}
    @GameTest(template="empty",batch="lake_letgo",timeoutTicks=300)
    public static void lettingGoOrStoppingStartsTheCountAgainAndABlowStillRefuses(GameTestHelper h){
        letGo=new Fixture(h,new BlockPos(7800,80,7800),LabyrinthPlace.SHALLOWS);Fixture f=letGo;LabyrinthData data=LabyrinthData.get(f.server);
        ServerPlayer owner=f.player("second_thoughts",f.base.offset(-1,0,-11));IndianLakeProgress.hunted(data,owner.getUUID());
        Shallows.onArrive(owner,LabyrinthPlace.SHALLOWS);LakeWitchEntity girl=Shallows.actor(owner);LakeCongregantEntity boy=Shallows.carrier(owner);
        h.assertTrue(Shallows.lift(owner,girl)&&Shallows.swing(owner,-1),"she is taken up and swung out");
        int[] step={0};long[] at={0};
        h.onEachTick(()->{
            f.load();long now=f.level.getGameTime();
            if(step[0]==0&&Shallows.readyForStroke(girl)){owner.setShiftKeyDown(true);step[0]=1;at[0]=now;return;}
            if(step[0]==1&&now>at[0]+2){
                h.assertTrue(girl.memoryPhase()==Shallows.LYING&&Shallows.count(girl)==0&&owner.getMainHandItem().isEmpty()&&boy.carry()==Shallows.HOLD,"sneaking lets go: she is down on the bank, the count is gone, the boy still has her feet");
                owner.setShiftKeyDown(false);h.assertTrue(Shallows.lift(owner,girl)&&Shallows.swing(owner,-1)&&Shallows.count(girl)==1,"she can be taken up again");
                step[0]=2;at[0]=now;return;
            }
            if(step[0]==2&&now>at[0]+100){
                h.assertTrue(girl.memoryPhase()==Shallows.CARRIED&&Shallows.count(girl)==0&&Math.abs(Shallows.swingAngle(girl))<.05F,"a swing left alone dies down and the count starts over: angle="+Shallows.swingAngle(girl));
                Shallows.onAttack(new AttackEntityEvent(owner,boy));
                h.assertTrue(Shallows.personal(data,owner.getUUID()).getBoolean("Cold")&&girl.isRemoved()&&boy.isRemoved()&&owner.getMainHandItem().isEmpty()&&!IndianLakeProgress.hasThrown(data,owner.getUUID()),"striking the boy at her feet darkens the visit without a throw");
                // 0.4.191: the failure takes the boys' company, and the account keeps it.
                var record=Shallows.personal(data,owner.getUUID());
                h.assertTrue(record.getBoolean(Shallows.BOYS_LEFT)&&Shallows.witness(record,"I threw her.").endsWith("the boys went home without me."),"the boys go home, and the account says so");
                step[0]=3;h.succeed();
            }
        });
    }
    @GameTest(template="empty",batch="lake_upgrade",timeoutTicks=200)
    public static void actualFourteenUpgradeCarriesTheSchoolsFiniteInventoryIntoProofrock(GameTestHelper h){
        upgrade=new Fixture(h,new BlockPos(7600,80,7600),LabyrinthPlace.DROWNED_TOWN,true);Fixture f=upgrade;LabyrinthData data=LabyrinthData.get(f.server);
        // The old school's first desk, already emptied of its essay and holding something else; its key desk holding the key.
        var old=(net.minecraft.world.Container)f.level.getBlockEntity(f.base.offset(-23,1,-33));old.setItem(0,ItemStack.EMPTY);old.setItem(3,new ItemStack(Items.DIAMOND));old.setChanged();
        f.level.setBlock(f.base.offset(-14,1,-32),Blocks.BARREL.defaultBlockState(),3);((net.minecraft.world.Container)f.level.getBlockEntity(f.base.offset(-14,1,-32))).setItem(0,new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()));
        CompoundTag state=data.state(DrownedTown.ID);state.putInt("Visit",2);state.putInt("DryMask",7);state.putBoolean("ChurchUnlocked",true);state.putBoolean("PaperStocked",true);state.putBoolean("KeyPlaced",true);data.setState(DrownedTown.ID,state);
        data.setBuilt(14,f.origin);h.assertTrue(!LabyrinthBuilder.ensureBuilt(f.server),"an older stack begins its incremental upgrade");
        LabyrinthBuilder.finishGameTest(f.server);
        var desk=(net.minecraft.world.Container)f.level.getBlockEntity(f.base.offset(DrownedTown.PAPERS[0]));var keyDesk=(net.minecraft.world.Container)f.level.getBlockEntity(f.base.offset(DrownedTown.KEY_DESK));
        h.assertTrue(data.builtVersion()==LabyrinthBuilder.VERSION&&desk!=null&&desk.getItem(0).isEmpty()&&desk.getItem(3).is(Items.DIAMOND),"the rebuilt school carries the depleted desk's exact contents and never restocks it");
        int keys=0;for(int i=0;i<keyDesk.getContainerSize();i++)if(keyDesk.getItem(i).is(DrownedTownRegistry.CHURCH_KEY.get()))keys++;
        h.assertTrue(keys==1&&f.level.getBlockState(f.base.offset(DrownedTown.SCHOOL_DOOR)).getBlock() instanceof DoorBlock&&!data.state(DrownedTown.ID).contains("Carry0467"),"the one key moves to the principal's desk, Indian Lake High stands, and custody is released");
        h.assertTrue(data.state(DrownedTown.ID).getInt("Visit")==2&&data.state(DrownedTown.ID).getBoolean("ChurchUnlocked")
                &&data.door(LabyrinthPlace.PRESERVED_CAVE.entryDoorId())!=null&&data.door(LabyrinthPlace.SHALLOWS.entryDoorId())!=null,"new native doorways are added while church progress stays intact");h.succeed();
    }
}
