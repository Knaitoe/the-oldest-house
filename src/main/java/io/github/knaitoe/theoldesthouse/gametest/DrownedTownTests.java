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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DrownedTownTests {
    @GameTest(template="empty") public static void newDeepSlotFitsWithoutMovingExistingRooms(GameTestHelper helper) {
        for (int y : new int[]{65, 80, 150, 250}) {
            BlockPos origin = new BlockPos(100, y, 100), base = LabyrinthPlaces.base(origin, LabyrinthPlace.DROWNED_TOWN);
            var slot = LabyrinthPlaces.slotBounds(origin, LabyrinthPlace.DROWNED_TOWN);
            helper.assertTrue(base != null && slot.isInside(base.offset(0,-13,-60)) && slot.isInside(base.offset(0,8,-60)), "lake depth and roof fit the new 24-block slot");
            helper.assertTrue(LabyrinthPlaces.placeAt(origin, base.offset(DrownedTown.CHURCH_DOOR)) == LabyrinthPlace.DROWNED_TOWN, "the deep church belongs to its room");
        }
        helper.assertTrue(LabyrinthPlace.QUIET_ROOM.slot()==26 && LabyrinthPlace.DROWNED_TOWN.slot()==27, "old room slots remain stable"); helper.succeed();
    }
    @GameTest(template="empty") public static void threeDistinctDriedEssaysEarnOnlyTheNextVisit(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData(); CompoundTag state = new CompoundTag(); state.putInt("Visit",1); data.setState(DrownedTown.ID,state);
        for (int i=0;i<10;i++) DrownedTown.dried(data,0);
        helper.assertTrue(data.state(DrownedTown.ID).getInt("DryMask")==1&&!data.state(DrownedTown.ID).getBoolean("BeatDone"), "one essay cannot farm the first beat");
        DrownedTown.dried(data,1); DrownedTown.dried(data,2);
        helper.assertTrue(data.state(DrownedTown.ID).getBoolean("BeatDone") && !data.isCompleted(DrownedTown.ID), "drying earns a return, not the ending");
        helper.assertTrue(DrownedTown.nextVisit(1,true)==2 && DrownedTown.nextVisit(1,false)==1
                && LabyrinthDealer.dealWeight(data,LabyrinthPlace.DROWNED_TOWN)==LabyrinthDealer.UNFINISHED_WEIGHT, "unfinished visits resume and remain discoverable"); helper.succeed();
    }
    @GameTest(template="empty") public static void lakeConsequencesAndPersonalHuntSurviveReload(GameTestHelper helper) {
        LabyrinthData data = new LabyrinthData(); UUID hunted=UUID.randomUUID(),peer=UUID.randomUUID();
        IndianLakeProgress.hunted(data,hunted); IndianLakeProgress.completedShallows(data,hunted,"the_explorer");
        helper.assertTrue(!IndianLakeProgress.canDealShallows(data,peer) && IndianLakeProgress.hasThrown(data,hunted), "visiting or another player's hunt cannot grant the shallows");
        CompoundTag state=data.state(DrownedTown.ID);state.putInt("Visit",2);state.putInt("DryMask",7);state.putBoolean("ChurchUnlocked",true);state.putBoolean("RoofOpened",true);data.setState(DrownedTown.ID,state);
        helper.assertTrue(IndianLakeProgress.arriveAtCave(data), "opening the church roof shifts the next cave visit");
        var registry=helper.getLevel().registryAccess();LabyrinthData loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),registry),registry);
        helper.assertTrue(IndianLakeProgress.wasHunted(loaded,hunted)&&IndianLakeProgress.hasThrown(loaded,hunted)
                &&IndianLakeProgress.hymnEscaped(loaded)&&IndianLakeProgress.deadOnShore(loaded), "linked consequences persist rather than following item ownership"); helper.succeed();
    }
    @GameTest(template="empty") public static void shallowsRequiresAnActualHuntAndCannotBeFarmed(GameTestHelper helper) {
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID(); data.visit(player,LabyrinthPlace.DROWNED_TOWN);
        IndianLakeProgress.completedShallows(data,player,"visitor"); helper.assertTrue(!IndianLakeProgress.hasThrown(data,player), "an unearned throwing beat is refused");
        IndianLakeProgress.hunted(data,player); helper.assertTrue(IndianLakeProgress.canDealShallows(data,player), "being hunted unlocks the companion one-shot");
        IndianLakeProgress.completedShallows(data,player,"visitor"); helper.assertTrue(!IndianLakeProgress.canDealShallows(data,player), "a completed shallows is exhausted for this explorer"); helper.succeed();
    }
    @GameTest(template="empty") public static void driedBooksAreReadableAndExplainBothRefuges(GameTestHelper helper) {
        for(int i=0;i<3;i++){
            ItemStack book=new ItemStack(DrownedTownRegistry.dryEssay(i));
            helper.assertTrue(book.get(DataComponents.WRITTEN_BOOK_CONTENT)!=null&&DrownedTown.ID.equals(VignetteYields.of(book)), "furnace outputs retain writing and Hillary's scent");
        }
        String words=DrownedEssayItem.content(0,List.of()).pages().stream().map(p->p.raw().getString()).reduce("",String::concat);
        helper.assertTrue(words.contains("grass")&&words.contains("water")&&words.contains("Bubbles"), "the rule is taught in the artifact");
        helper.assertTrue(DrownedEssayItem.content(1,List.of("the_explorer")).pages().getLast().raw().getString().contains("the_explorer"), "the later shallows page names its participant"); helper.succeed();
    }
    @GameTest(template="empty") public static void aResolvedRoofOffersItsActualAftermath(GameTestHelper helper) {
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();BlockPos base=new BlockPos(100,100,100);
        data.setCompleted(DrownedTown.ID,true);
        helper.assertTrue(!WitnessAccount.aftermathTarget(data,LabyrinthPlace.DROWNED_TOWN,base,base.offset(DrownedTown.ROOF_HATCH)), "a completion flag alone is not a visible ending");
        CompoundTag state=new CompoundTag();state.putBoolean("RoofOpened",true);data.setState(DrownedTown.ID,state);
        helper.assertTrue(WitnessAccount.aftermathTarget(data,LabyrinthPlace.DROWNED_TOWN,base,base.offset(DrownedTown.ROOF_HATCH))
                &&LabyrinthDealer.vignettesAvailable(data,player).contains(LabyrinthPlace.DROWNED_TOWN), "a later explorer can inspect the open roof");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.DROWNED_TOWN,"aftermath");
        helper.assertTrue(!LabyrinthDealer.vignettesAvailable(data,player).contains(LabyrinthPlace.DROWNED_TOWN), "recorded aftermath returns to exhausted dealing"); helper.succeed();
    }
    @GameTest(template="empty") public static void lakeDoorLeaksHaveTheirOwnSensoryCue(GameTestHelper helper) {
        var door=new LabyrinthData.Door("lake",HouseDimensions.INTERIOR,BlockPos.ZERO,Direction.SOUTH,"place:drowned_town",true);door.leak=true;
        helper.assertTrue(LabyrinthDoorLeaks.cue(new LabyrinthData(),UUID.randomUUID(),door).kind()==DoorLeakKind.LAKE, "water and a distant shore voice reach the hallway before entry"); helper.succeed();
    }
    @GameTest(template="empty") public static void churchKeyPreventsAndClearsNativeDrownedTargets(GameTestHelper helper) {
        var level=helper.getLevel();ServerPlayer player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"key_bearer"));
        Drowned drowned=new Drowned(net.minecraft.world.entity.EntityType.DROWNED,level);
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);drowned.setTarget(player);
        helper.assertTrue(drowned.getTarget()==player, "ordinary drowned still pursue without the artifact");
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()));
        ChurchKeyItem.onEntityTick(new EntityTickEvent.Post(drowned));
        helper.assertTrue(drowned.getTarget()==null, "an existing pursuit ends when the key is picked up");
        drowned.setTarget(player);helper.assertTrue(drowned.getTarget()==null, "the native target hook rejects a new pursuit");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);drowned.setTarget(player);
        helper.assertTrue(drowned.getTarget()==player, "putting the key away restores ordinary targeting");drowned.discard();helper.succeed();
    }
    @GameTest(template="empty") public static void churchKeyDoesNotSuppressEncountersInsideTheHouse(GameTestHelper helper) {
        var level=HouseTestLevel.get(helper.getLevel().getServer());ServerPlayer player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"inside_key"));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()));
        helper.assertTrue(!ChurchKeyItem.protects(player), "the artifact's outside benefit is bounded by the House");helper.succeed();
    }
    @GameTest(template="empty") public static void witchReloadKeepsTheShoreAndDiscardsAnUnwarnedStrike(GameTestHelper helper) {
        var witch=DrownedTownRegistry.LAKE_WITCH.get().create(helper.getLevel());BlockPos base=new BlockPos(80,50,80);witch.shore(base,2);
        CompoundTag tag=new CompoundTag();witch.saveWithoutId(tag);
        var restored=DrownedTownRegistry.LAKE_WITCH.get().create(helper.getLevel());restored.load(tag);
        helper.assertTrue(base.equals(restored.shoreBase())&&!restored.striking(), "reload preserves the bounded hunt without an instant attack");witch.discard();restored.discard();helper.succeed();
    }

    private static final class Fixture implements AutoCloseable {
        final MinecraftServer server; final HouseSavedData house; final LabyrinthData labyrinth; final ServerLevel level; final BlockPos base;
        Fixture(MinecraftServer server,BlockPos origin){this.server=server;house=HouseSavedData.get(server);labyrinth=LabyrinthData.get(server);level=HouseTestLevel.get(server);
            var h=new HouseSavedData();h.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",h);
            var d=new LabyrinthData();d.setBuilt(LabyrinthBuilder.VERSION,origin);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",d);
            base=LabyrinthPlaces.base(origin,LabyrinthPlace.DROWNED_TOWN);DrownedTown.build(server,level,base);LabyrinthBuilder.registerDoors(d,LabyrinthPlace.DROWNED_TOWN,base);}
        public void close(){
            for(var actor:level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class,new AABB(base.offset(-30,-14,-65),base.offset(31,10,19)),e->e instanceof LakeWitchEntity||e instanceof LakeCongregantEntity))actor.discard();
            for(int x=(base.getX()-30)>>4;x<=(base.getX()+30)>>4;x++)for(int z=(base.getZ()-65)>>4;z<=(base.getZ()+18)>>4;z++)
                level.getChunkSource().removeRegionTicket(TicketType.PORTAL,new ChunkPos(x,z),3,base);
            server.overworld().getDataStorage().set("the_oldest_house",house);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",labyrinth);DrownedTown.clearAll();LabyrinthDoors.clearAll();
        }
    }
    private static Fixture sequenceFixture; private static ServerPlayer sequencePlayer;
    @AfterBatch(batch="drowned_sequence") public static void cleanSequence(ServerLevel level){
        if(sequencePlayer!=null){level.getServer().getPlayerList().remove(sequencePlayer);sequencePlayer=null;}
        if(sequenceFixture!=null){sequenceFixture.close();sequenceFixture=null;}}
    private static void click(ServerPlayer player,BlockPos at){
        NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),Direction.UP,at,false)));
    }
    @GameTest(template="empty",batch="drowned_sequence",timeoutTicks=1050)
    public static void nativeFurnaceDoorReturnsKeyAndRoofCompleteTheActualSequence(GameTestHelper helper){
        MinecraftServer server=helper.getLevel().getServer();BlockPos origin=new BlockPos(5600,80,5600);sequenceFixture=new Fixture(server,origin);
        Fixture fixture=sequenceFixture;ServerLevel level=fixture.level;BlockPos base=fixture.base;LabyrinthData data=LabyrinthData.get(server);
        helper.assertTrue(level.getFluidState(base.offset(0,-10,-30)).is(net.minecraft.tags.FluidTags.WATER)
                &&level.getBlockState(base.offset(-2,-3,-18)).is(Blocks.BUBBLE_COLUMN), "real flooded streets and an upward air column exist");
        helper.assertTrue(level.getBlockState(base.offset(DrownedTown.SCHOOL_DOOR)).getFluidState().isEmpty(), "the native school door holds a breath");
        // An ordinary return threshold in the same level exercises the actual labyrinth graph hooks.
        BlockPos source=base.offset(42,0,1);for(int x=-7;x<=7;x++)for(int z=0;z<=17;z++)level.setBlock(source.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
        var entrance=new LabyrinthData.Door("drowned_fixture",HouseDimensions.INTERIOR,source,Direction.SOUTH,"place:drowned_town",true);data.putDoor(entrance);
        sequencePlayer=helper.makeMockServerPlayerInLevel();ServerPlayer player=sequencePlayer;player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        Vec3 start=Vec3.atBottomCenterOf(source.south(2));player.teleportTo(level,start.x,start.y,start.z,180,0);LabyrinthDoors.use(player,entrance);
        helper.assertTrue(data.state(DrownedTown.ID).getInt("Visit")==1&&data.returnDepth(player.getUUID())==1, "native entry starts the first visit and remembers the way back");
        player.moveTo(Vec3.atBottomCenterOf(base.offset(15,-11,-39)));player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DrownedTownRegistry.CHURCH_KEY.get()));
        helper.assertTrue(!DrownedTown.unlockChurch(player,base.offset(DrownedTown.CHURCH_DOOR)), "even a borrowed key cannot skip the first visit");player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        player.moveTo(Vec3.atBottomCenterOf(base.offset(19,0,-8)));
        var furnace=(FurnaceBlockEntity)level.getBlockEntity(base.offset(DrownedTown.FURNACE));
        click(player,base.offset(DrownedTown.FURNACE));player.openMenu(furnace);furnace.setItem(1,new ItemStack(Items.COAL,3));
        var first=(BarrelBlockEntity)level.getBlockEntity(base.offset(DrownedTown.PAPERS[0]));furnace.setItem(0,first.removeItemNoUpdate(0));first.setChanged();furnace.setChanged();
        int[] essay={0};
        helper.onEachTick(()->{
            if(essay[0]>=3)return;
            if(furnace.getItem(2).isEmpty())return;
            int index=essay[0];helper.assertTrue(furnace.getItem(2).is(DrownedTownRegistry.dryEssay(index)), "normal furnace recipes produce the correct readable essay");
            player.containerMenu.quickMoveStack(player,2);
            helper.assertTrue((data.state(DrownedTown.ID).getInt("DryMask")&(1<<index))!=0, "native furnace pickup records this distinct shore essay");essay[0]++;
            if(essay[0]<3){var desk=(BarrelBlockEntity)level.getBlockEntity(base.offset(DrownedTown.PAPERS[essay[0]]));furnace.setItem(0,desk.removeItemNoUpdate(0));desk.setChanged();furnace.setChanged();return;}
            player.closeContainer();helper.assertTrue(data.state(DrownedTown.ID).getBoolean("BeatDone")&&!data.isCompleted(DrownedTown.ID), "three dried essays earn the return rather than ending the vignette");
            player.moveTo(Vec3.atBottomCenterOf(base.offset(0,0,-3)));LabyrinthDoors.tickPlayer(player,origin);
            LabyrinthDoors.use(player,data.door(LabyrinthPlace.DROWNED_TOWN.entryDoorId()));player.moveTo(Vec3.atBottomCenterOf(base.offset(0,0,3)));LabyrinthDoors.tickPlayer(player,origin);
            helper.assertTrue(!DrownedTown.contains(base,player.position())&&data.returnDepth(player.getUUID())==0, "walking through the real return door leaves the lake");
            LabyrinthDoors.use(player,entrance);helper.assertTrue(data.state(DrownedTown.ID).getInt("Visit")==2, "only the next actual arrival reveals the key");
            var desk=(BarrelBlockEntity)level.getBlockEntity(base.offset(DrownedTown.KEY_DESK));ItemStack key=desk.removeItemNoUpdate(0);desk.setChanged();
            helper.assertTrue(key.is(DrownedTownRegistry.CHURCH_KEY.get()), "the later key is in the school");player.setItemInHand(InteractionHand.MAIN_HAND,key);
            player.moveTo(Vec3.atBottomCenterOf(base.offset(15,-11,-39)));click(player,base.offset(DrownedTown.CHURCH_DOOR));
            helper.assertTrue(level.getBlockState(base.offset(DrownedTown.CHURCH_DOOR)).getValue(DoorBlock.OPEN)&&player.getMainHandItem().is(DrownedTownRegistry.CHURCH_KEY.get()), "the artifact unlocks the gate and is kept");
            var breach=new BlockEvent.BreakEvent(level,base.offset(DrownedTown.ROOF_HATCH),level.getBlockState(base.offset(DrownedTown.ROOF_HATCH)),player);NeoForge.EVENT_BUS.post(breach);
            helper.assertTrue(breach.isCanceled()&&!data.isCompleted(DrownedTown.ID), "normal block breaking cannot bypass the roof beat");
            player.moveTo(Vec3.atBottomCenterOf(base.offset(15,-5,-42)));click(player,base.offset(DrownedTown.ROOF_HATCH));
            helper.assertTrue(data.isCompleted(DrownedTown.ID)&&IndianLakeProgress.hymnEscaped(data)
                    &&level.getBlockState(base.offset(DrownedTown.ROOF_HATCH)).getValue(TrapDoorBlock.OPEN), "native hatch use lets the hymn out and finishes the shared scene");
            helper.assertTrue(WitnessAccount.has(data,player.getUUID(),WitnessAccount.Story.DROWNED_TOWN), "this explorer records the actual resolution");
            click(player,base.offset(DrownedTown.ROOF_HATCH));helper.assertTrue(WitnessAccount.count(data,player.getUUID())==1, "repeated hatch use gives no additional resolution");
            helper.succeed();
        });
    }

    private static ServerPlayer huntPlayer; private static LakeWitchEntity huntWitch; private static CompoundTag beforeHunt;
    @AfterBatch(batch="drowned_hunt") public static void cleanHunt(ServerLevel level){
        if(huntWitch!=null){huntWitch.discard();huntWitch=null;}if(huntPlayer!=null){level.getServer().getPlayerList().remove(huntPlayer);huntPlayer=null;}
        if(beforeHunt!=null){LabyrinthData.get(level.getServer()).setState(DrownedTown.ID,beforeHunt);beforeHunt=null;}}
    @GameTest(template="empty",batch="drowned_hunt",timeoutTicks=200)
    public static void nativeWitchDetoursAroundGrassAndCannotBePushedIntoWater(GameTestHelper helper){
        ServerLevel level=helper.getLevel();BlockPos base=helper.absolutePos(new BlockPos(0,2,0));beforeHunt=LabyrinthData.get(level.getServer()).state(DrownedTown.ID).copy();
        for(int x=-10;x<=10;x++)for(int z=-14;z<=0;z++){
            level.setBlock(base.offset(x,-1,z),z<=-12?Blocks.WATER.defaultBlockState():Blocks.COARSE_DIRT.defaultBlockState(),3);
            level.setBlock(base.offset(x,-2,z),Blocks.STONE.defaultBlockState(),3);
            for(int y=0;y<=3;y++)level.setBlock(base.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
        for(int x=0;x<=2;x++)for(int z=-9;z<=-5;z++)level.setBlock(base.offset(x,-1,z),Blocks.GRASS_BLOCK.defaultBlockState(),3);
        var route=LakeWitchEntity.shoreRoute(level,base,base.offset(-6,0,-7),base.offset(6,0,-7));
        helper.assertTrue(!route.isEmpty()&&route.stream().noneMatch(p->LakeWitchEntity.safeGround(level,p)), "the shore pathfinder routes around grass rather than assigning it an expensive cost");
        huntPlayer=helper.makeMockServerPlayerInLevel();huntPlayer.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);huntPlayer.moveTo(Vec3.atBottomCenterOf(base.offset(6,0,-7)));
        huntWitch=DrownedTownRegistry.LAKE_WITCH.get().create(level);huntWitch.shore(base,1);huntWitch.moveTo(Vec3.atBottomCenterOf(base.offset(-6,0,-7)));level.addFreshEntity(huntWitch);
        helper.onEachTick(()->helper.assertTrue(!LakeWitchEntity.safeGround(level,huntWitch.blockPosition()), "physical witch movement never crosses a refuge"));
        helper.succeedWhen(()->{
            helper.assertTrue(huntWitch.getX()>base.getX()+4&&huntWitch.distanceToSqr(huntPlayer)<8, "the native actor must actually complete its detour; pos="+huntWitch.position()+"; ticks="+huntWitch.tickCount);
            helper.assertTrue(IndianLakeProgress.wasHunted(LabyrinthData.get(level.getServer()),huntPlayer.getUUID()), "a real approach records the hunt");
            huntPlayer.moveTo(Vec3.atBottomCenterOf(base.offset(6,-1,-13)));
            helper.assertTrue(!LakeWitchEntity.canAttack(huntWitch,huntPlayer), "water stops the chase and attack");
            Vec3 before=huntWitch.position();huntWitch.move(MoverType.SELF,new Vec3(0,0,-9));
            helper.assertTrue(huntWitch.position().distanceToSqr(before)<.0001, "a force cannot push her across the water boundary");
            huntPlayer.moveTo(Vec3.atBottomCenterOf(base.offset(1,0,-7)));helper.assertTrue(!LakeWitchEntity.canAttack(huntWitch,huntPlayer), "grass is safe during an attack windup too");
        });
    }
}
