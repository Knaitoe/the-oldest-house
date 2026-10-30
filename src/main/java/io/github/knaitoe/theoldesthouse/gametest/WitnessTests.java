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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WitnessTests {
    private static void fillCurrentQuota(LabyrinthData data,UUID player){for(var story:WitnessAccount.Story.values())if(WitnessAccount.count(data,player)<WitnessAccount.REQUIRED&&story!=WitnessAccount.Story.MOTHER&&story!=WitnessAccount.Story.FLOORBOARDS)WitnessAccount.resolve(data,player,story,"personally_resolved");}
    @GameTest(template="empty") public static void roomVisitsWorldFlagsAndTradedBooksDoNotUnlockAnEnding(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID a=UUID.randomUUID(),b=UUID.randomUUID();
        for(var place:List.of(LabyrinthPlace.FLOORBOARDS,LabyrinthPlace.HIDE_AND_CLAP,LabyrinthPlace.HARRIGAN)){data.visit(a,place);data.setCompleted(place.id(),true);}
        helper.assertTrue(WitnessAccount.count(data,a)==0&&!WitnessAccount.ready(data,a),"visits and shared room flags confer no personal resolution");
        for(var story:List.of(WitnessAccount.Story.FLOORBOARDS,WitnessAccount.Story.CLAP,WitnessAccount.Story.HARRIGAN,
                WitnessAccount.Story.DROWNED_TOWN,WitnessAccount.Story.PRESERVED_CAVE,WitnessAccount.Story.SHALLOWS,WitnessAccount.Story.PHONE_CANOE))WitnessAccount.resolve(data,a,story,"resolved");
        ItemStack account=WitnessAccount.book(data,a,"reader",false);
        helper.assertTrue(WitnessAccount.ownedBook(account,a)&&!WitnessAccount.ownedBook(account,b)&&!WitnessAccount.ready(data,b),"a borrowed book does not borrow its author's knowledge");helper.succeed();
    }
    @GameTest(template="empty") public static void resolutionsAreDistinctAndLeaveRoomForMissedStories(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();
        for(int i=0;i<20;i++)WitnessAccount.resolve(data,player,WitnessAccount.Story.CLAP,"won");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.MODEL_HOME,"saw_tree");
        helper.assertTrue(WitnessAccount.count(data,player)==2&&!WitnessAccount.ready(data,player),"repeated survival cannot farm the unlock");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.HARRIGAN,"kept_phone");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.DROWNED_TOWN,"opened_roof");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.PRESERVED_CAVE,"crossed_congregation");
        helper.assertTrue(WitnessAccount.count(data,player)==5&&!WitnessAccount.ready(data,player),"five distinct resolutions still fall short of fifteen");
        ItemStack unfinished=WitnessAccount.book(data,player,"reader",false);
        helper.assertTrue(unfinished.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream()
                .noneMatch(p->p.raw().getString().contains("Crouch and open the bars")),"the final cell directions remain absent below the threshold");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.SHALLOWS,"threw_her");
        helper.assertTrue(WitnessAccount.count(data,player)==6&&!WitnessAccount.ready(data,player),"six resolutions fall short of fifteen");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.PHONE_CANOE,"lost_recording");
        helper.assertTrue(WitnessAccount.count(data,player)==7&&!WitnessAccount.ready(data,player),"the previous seven-source account stays saved below the new boundary");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.GOATMAN,"kept_door_closed");
        helper.assertTrue(WitnessAccount.count(data,player)==8&&!WitnessAccount.ready(data,player),"the prior eight-source account remains saved below the new boundary");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.TED_CAVER,"retraced_the_squeeze");
        helper.assertTrue(!WitnessAccount.ready(data,player),"all nine older resolutions are retained below the new quota");
        for(var story:List.of(WitnessAccount.Story.ZAMPANO,WitnessAccount.Story.WHALE,WitnessAccount.Story.BARN_WELL,WitnessAccount.Story.HOLLOWAY,WitnessAccount.Story.PLAIN,WitnessAccount.Story.HOSPITAL))WitnessAccount.resolve(data,player,story,"resolved");
        helper.assertTrue(!WitnessAccount.ready(data,player),"fifteen preserved sources stay below the expanded quota");fillCurrentQuota(data,player);
        helper.assertTrue(WitnessAccount.ready(data,player)&&!WitnessAccount.has(data,player,WitnessAccount.Story.MOTHER)
                &&!WitnessAccount.has(data,player,WitnessAccount.Story.FLOORBOARDS),"fifteen different resolutions permit four missed stories, without a mandatory Mother outcome");
        ItemStack complete=WitnessAccount.book(data,player,"reader",false);
        helper.assertTrue(complete.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream()
                .anyMatch(p->p.raw().getString().contains("Crouch and open the bars")),"the fifteenth resolution restores the actual cell directions");helper.succeed();
    }
    /** 0.4.105: the account lists rooms in the order this reader ended them; an older record without that order keeps the House's order. */
    @GameTest(template="empty") public static void theAccountReadsInTheOrderTheRoomsEnded(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();
        WitnessAccount.resolve(data,player,WitnessAccount.Story.WINTER_LAKE,"returned_ribbon");WitnessAccount.resolve(data,player,WitnessAccount.Story.CLAP,"won");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.WINTER_LAKE,"again");WitnessAccount.resolve(data,player,WitnessAccount.Story.HARRIGAN,"kept_phone");
        var ordered=WitnessAccount.inOrder(WitnessAccount.record(data,player));
        helper.assertTrue(ordered.subList(0,3).equals(List.of(WitnessAccount.Story.WINTER_LAKE,WitnessAccount.Story.CLAP,WitnessAccount.Story.HARRIGAN))&&ordered.size()==WitnessAccount.Story.values().length,"resolved rooms come first, in the order they ended, each once");
        var older=WitnessAccount.record(data,player).copy();older.remove("Order");
        helper.assertTrue(WitnessAccount.inOrder(older).equals(List.of(WitnessAccount.Story.values())),"an account saved before the order keeps the House's order");
        helper.succeed();
    }
    @GameTest(template="empty") public static void witnessQuotaTracksSeventyFivePercentOfTheEligiblePool(GameTestHelper helper){
        helper.assertTrue(WitnessAccount.Story.values().length==55&&WitnessAccount.REQUIRED==42,"the fifty-five playable sources currently require forty-two resolutions");
        for(int[] quota:new int[][]{{5,4},{6,5},{7,6},{8,6},{9,7},{10,8},{11,9},{12,9},{13,10},{16,12},{17,13},{18,14},{19,15},{20,15},{43,33}})
            helper.assertTrue(WitnessAccount.requiredForPoolSize(quota[0])==quota[1],"the Witness quota rounds 75 percent upward for a pool of "+quota[0]);
        helper.succeed();
    }
    @GameTest(template="empty") public static void accountAndDeliberateReadingSurviveWorldReload(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();
        for(var story:List.of(WitnessAccount.Story.FLOORBOARDS,WitnessAccount.Story.CLAP,WitnessAccount.Story.HARRIGAN,
                WitnessAccount.Story.DROWNED_TOWN,WitnessAccount.Story.PRESERVED_CAVE,WitnessAccount.Story.SHALLOWS,WitnessAccount.Story.PHONE_CANOE,WitnessAccount.Story.GOATMAN,WitnessAccount.Story.TED_CAVER))WitnessAccount.resolve(data,player,story,"resolved");
        for(var story:List.of(WitnessAccount.Story.ZAMPANO,WitnessAccount.Story.WHALE,WitnessAccount.Story.BARN_WELL,WitnessAccount.Story.HOLLOWAY,WitnessAccount.Story.PLAIN,WitnessAccount.Story.HOSPITAL))WitnessAccount.resolve(data,player,story,"resolved");
        fillCurrentQuota(data,player);helper.assertTrue(!WitnessEnding.qualified(data,player),"a complete account still requires reading the cell's passage");WitnessAccount.markRead(data,player);
        var registries=helper.getLevel().registryAccess();LabyrinthData loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),registries),registries);
        helper.assertTrue(WitnessAccount.count(loaded,player)==WitnessAccount.REQUIRED&&WitnessEnding.qualified(loaded,player),"the chosen reading and distinct outcomes persist independently of player cloning");helper.succeed();
    }
    @GameTest(template="empty") public static void olderAccountsKeepTheirEvidenceUnderTheHigherQuota(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();
        for(var story:List.of(WitnessAccount.Story.FLOORBOARDS,WitnessAccount.Story.CLAP,WitnessAccount.Story.HARRIGAN))WitnessAccount.resolve(data,player,story,"resolved");
        // Model a genuine 0.4.12 reading; the higher quota must still gate an uncommitted ending.
        CompoundTag world=data.state(WitnessAccount.STATE),record=world.getCompound(player.toString());
        record.putBoolean("ReadPlay",true);world.put(player.toString(),record);data.setState(WitnessAccount.STATE,world);
        var registries=helper.getLevel().registryAccess();LabyrinthData loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),registries),registries);
        helper.assertTrue(WitnessAccount.count(loaded,player)==3&&WitnessAccount.readPlay(loaded,player)&&!WitnessEnding.qualified(loaded,player),"old evidence and reading survive without bypassing fifteen resolutions");
        WitnessAccount.resolve(loaded,player,WitnessAccount.Story.CLAP,"again");
        helper.assertTrue(WitnessAccount.count(loaded,player)==3,"repeating an old scene cannot supply missing evidence");
        for(var story:List.of(WitnessAccount.Story.DROWNED_TOWN,WitnessAccount.Story.PRESERVED_CAVE,WitnessAccount.Story.SHALLOWS))WitnessAccount.resolve(loaded,player,story,"resolved");
        helper.assertTrue(WitnessAccount.count(loaded,player)==6&&WitnessAccount.readPlay(loaded,player)&&!WitnessEnding.qualified(loaded,player),"a previously qualifying six-source reading remains saved while fifteen are now required");
        WitnessAccount.resolve(loaded,player,WitnessAccount.Story.PHONE_CANOE,"lost_recording");
        helper.assertTrue(WitnessAccount.count(loaded,player)==7&&!WitnessEnding.qualified(loaded,player),"all prior evidence and deliberate reading remain, while the current fifteenth source is required");
        WitnessAccount.resolve(loaded,player,WitnessAccount.Story.GOATMAN,"kept_door_closed");
        helper.assertTrue(WitnessAccount.count(loaded,player)==8&&!WitnessEnding.qualified(loaded,player),"the previous eight-source reading remains saved while fifteen are required");
        WitnessAccount.resolve(loaded,player,WitnessAccount.Story.TED_CAVER,"retraced_the_squeeze");
        helper.assertTrue(WitnessAccount.count(loaded,player)==9&&!WitnessEnding.qualified(loaded,player),"the old cave escape remains saved below the fifteen-source boundary");
        for(var story:List.of(WitnessAccount.Story.ZAMPANO,WitnessAccount.Story.WHALE,WitnessAccount.Story.BARN_WELL,WitnessAccount.Story.HOLLOWAY,WitnessAccount.Story.PLAIN,WitnessAccount.Story.HOSPITAL))WitnessAccount.resolve(loaded,player,story,"resolved");
        fillCurrentQuota(loaded,player);helper.assertTrue(WitnessAccount.count(loaded,player)==WitnessAccount.REQUIRED&&WitnessEnding.qualified(loaded,player),"new personal resolutions qualify the preserved deliberate reading");helper.succeed();
    }
    @GameTest(template="empty",batch="witness_quota_fixture") public static void operatorReadyCommandMeetsTheCurrentQuotaWithoutDuplicateCredit(GameTestHelper helper) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var server=helper.getLevel().getServer();
        try(var fixture=new Fixture(server,new BlockPos(3300,80,3300))){
            var player=FakePlayerFactory.get(helper.getLevel(),new GameProfile(UUID.randomUUID(),"quota_fixture"));
            var dispatcher=new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();dispatcher.register(FinaleCommands.build());
            helper.assertTrue(dispatcher.execute("finale witness ready",player.createCommandSourceStack())==1,"the actual operator command succeeds");
            var data=LabyrinthData.get(server);
            helper.assertTrue(WitnessAccount.count(data,player.getUUID())==WitnessAccount.REQUIRED&&WitnessAccount.ready(data,player.getUUID())
                    &&!WitnessAccount.readPlay(data,player.getUUID()),"the fixture reaches the current quota without bypassing deliberate reading");
            helper.assertTrue(player.getInventory().items.stream().anyMatch(s->WitnessAccount.ownedBook(s,player.getUUID())),"the operator receives the actual updated account");
            dispatcher.execute("finale witness ready",player.createCommandSourceStack());
            helper.assertTrue(WitnessAccount.count(data,player.getUUID())==WitnessAccount.REQUIRED&&!WitnessAccount.ready(data,UUID.randomUUID()),"repeating the fixture adds no credit and does not qualify another explorer");
        }helper.succeed();
    }
    @GameTest(template="empty") public static void finishedRoomsOfferOnlyTheirActualEndingProp(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();BlockPos base=new BlockPos(30,100,20);
        data.setCompleted(TellTaleFloorboards.ID,true);
        helper.assertTrue(LabyrinthDealer.vignettesAvailable(data,player).contains(LabyrinthPlace.FLOORBOARDS),"a later explorer may inspect the aftermath");
        helper.assertTrue(!WitnessAccount.aftermathTarget(data,LabyrinthPlace.FLOORBOARDS,base,base)
                &&WitnessAccount.aftermathTarget(data,LabyrinthPlace.FLOORBOARDS,base,base.offset(TellTaleFloorboards.LOOSE_BOARD.below())),"the exposed space beneath the board must be examined");
        WitnessAccount.resolve(data,player,WitnessAccount.Story.FLOORBOARDS,"aftermath");
        helper.assertTrue(!LabyrinthDealer.vignettesAvailable(data,player).contains(LabyrinthPlace.FLOORBOARDS),"a recorded one-shot returns to its exhausted dealer state");helper.succeed();
    }
    @GameTest(template="empty") public static void heartbeatSpeedsUpAndFinishedDoorStopsLeakingIt(GameTestHelper helper){
        helper.assertTrue(TellTaleFloorboards.heartbeatInterval(0)>TellTaleFloorboards.heartbeatInterval(50)
                &&TellTaleFloorboards.heartbeatInterval(50)>TellTaleFloorboards.heartbeatInterval(100),"the original double beat grows faster with agitation");
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();
        var door=new LabyrinthData.Door("heart",HouseDimensions.INTERIOR,BlockPos.ZERO,Direction.SOUTH,"place:floorboards",true);door.leak=true;
        helper.assertTrue(LabyrinthDoorLeaks.cue(data,player,door).kind()==DoorLeakKind.HEARTBEAT,"the uncompleted room leaks its heartbeat");
        data.setCompleted(TellTaleFloorboards.ID,true);helper.assertTrue(LabyrinthDoorLeaks.cue(data,player,door)==null,"completion silences the real room's door too");helper.succeed();
    }
    @GameTest(template="empty") public static void releasedCreatureReloadsWithoutBecomingHostile(GameTestHelper helper){
        var creature=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());creature.owner(UUID.randomUUID());creature.released();
        CompoundTag tag=new CompoundTag();creature.saveWithoutId(tag);var restored=FinaleRegistry.MINOTAUR.get().create(helper.getLevel());restored.load(tag);
        helper.assertTrue(restored.motion()==MinotaurEntity.RELEASED&&restored.owner().equals(creature.owner()),"reload retains release and its owner");
        creature.discard();restored.discard();helper.succeed();
    }
    @GameTest(template="empty") public static void thirdEndingIsPersonalTerminalAndLeavesTheHouse(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID(),peer=UUID.randomUUID();CompoundTag record=new CompoundTag(),world=new CompoundTag();
        record.putString("Phase",FinaleProgress.Phase.WITNESSED.name());world.put(player.toString(),record);data.setState(FinaleProgress.STATE,world);
        var registries=helper.getLevel().registryAccess();LabyrinthData loaded=LabyrinthData.FACTORY.deserializer().apply(data.save(new CompoundTag(),registries),registries);
        helper.assertTrue(FinaleProgress.terminal(FinaleProgress.phase(loaded.state(FinaleProgress.STATE).getCompound(player.toString())))
                &&!FinaleProgress.terminal(FinaleProgress.phase(loaded.state(FinaleProgress.STATE).getCompound(peer.toString())))
                &&!loaded.state(FinaleProgress.STATE).getBoolean("Ended"),"release closes only this explorer's relationship; shared collapse is not set");
        helper.assertTrue(FinaleProgress.committed(FinaleProgress.Phase.RELEASE)&&FinaleProgress.committed(FinaleProgress.Phase.HOMEWARD),"death or outside teleports cannot bypass the physical return");helper.succeed();
    }
    @GameTest(template="empty") public static void epilogueRetainsAccountsAndAddsInsideView(GameTestHelper helper){
        LabyrinthData data=new LabyrinthData();UUID player=UUID.randomUUID();WitnessAccount.resolve(data,player,WitnessAccount.Story.HARRIGAN,"buried_phone");
        ItemStack book=WitnessAccount.book(data,player,"reader",true);var pages=book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages();
        helper.assertTrue(pages.stream().anyMatch(p->p.raw().getString().contains("The study"))
                &&pages.get(pages.size()-1).raw().getString().contains("From inside the cell")
                &&pages.get(pages.size()-1).raw().getString().contains("No signature follows")
                &&pages.get(pages.size()-2).raw().getString().contains("cannot say what the House is"),"the epilogue preserves personal evidence without certifying the identity of its last writer");helper.succeed();
    }
    /** These fixtures replace, then restore, the world-owned objects rather than mutating a live save. */
    private static final class Fixture implements AutoCloseable {
        final MinecraftServer server;final HouseSavedData house;final LabyrinthData labyrinth;
        Fixture(MinecraftServer server,BlockPos origin){this.server=server;house=HouseSavedData.get(server);labyrinth=LabyrinthData.get(server);
            var fixture=new HouseSavedData();fixture.markSpawned(origin);server.overworld().getDataStorage().set("the_oldest_house",fixture);
            server.overworld().getDataStorage().set("the_oldest_house_labyrinth",new LabyrinthData());}
        public void close(){server.overworld().getDataStorage().set("the_oldest_house",house);server.overworld().getDataStorage().set("the_oldest_house_labyrinth",labyrinth);TellTaleFloorboards.clearAll();}
    }
    private static void pryFixture(GameTestHelper helper,boolean attack){
        var server=helper.getLevel().getServer();var level=HouseTestLevel.get(server);BlockPos origin=new BlockPos(2200,80,2200);
        try(var fixture=new Fixture(server,origin)){
            BlockPos base=LabyrinthPlaces.base(origin,LabyrinthPlace.FLOORBOARDS),board=base.offset(TellTaleFloorboards.LOOSE_BOARD);
            level.getChunkAt(board);level.setBlock(board,HouseBlocks.LOOSE_FLOORBOARD.get().defaultBlockState(),3);
            ServerPlayer player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"pry_fixture"));
            player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);player.moveTo(Vec3.atBottomCenterOf(board.above().south()));
            double calm=TellTaleFloorboards.heat();var byHand=new BlockEvent.BreakEvent(level,board,level.getBlockState(board),player);NeoForge.EVENT_BUS.post(byHand);
            helper.assertTrue(byHand.isCanceled()&&level.getBlockState(board).is(HouseBlocks.LOOSE_FLOORBOARD.get())&&TellTaleFloorboards.heat()==calm,"working at the board by hand leaves it in place and makes no noise");
            ItemStack axe=new ItemStack(Items.IRON_AXE);player.setItemInHand(InteractionHand.MAIN_HAND,axe);
            if(attack){var event=new PlayerInteractEvent.LeftClickBlock(player,board,Direction.UP,PlayerInteractEvent.LeftClickBlock.Action.START);
                event.setCanceled(true);NeoForge.EVENT_BUS.post(event);helper.assertTrue(event.isCanceled(),"native mining remains cancelled after the authored pry");}
            else{var event=new BlockEvent.BreakEvent(level,board,level.getBlockState(board),player);event.setCanceled(true);NeoForge.EVENT_BUS.post(event);}
            helper.assertTrue(level.getBlockState(board).isAir()&&LabyrinthData.get(server).isCompleted(TellTaleFloorboards.ID),"the legitimate loose board completes even under a cancelled mining event");
            helper.assertTrue(WitnessAccount.has(LabyrinthData.get(server),player.getUUID(),WitnessAccount.Story.FLOORBOARDS),"the real pry credits this explorer");
            int wear=axe.getDamageValue();helper.assertTrue(!TellTaleFloorboards.pry(player,axe,InteractionHand.MAIN_HAND)&&axe.getDamageValue()==wear,"repeat events cannot duplicate the note or wear the axe twice");
            BlockPos wall=base.offset(-6,0,-6);level.setBlock(wall,Blocks.BROWN_TERRACOTTA.defaultBlockState(),3);
            var attempt=new BlockEvent.BreakEvent(level,wall,level.getBlockState(wall),player);NeoForge.EVENT_BUS.post(attempt);
            helper.assertTrue(attempt.isCanceled()&&level.getBlockState(wall).is(Blocks.BROWN_TERRACOTTA)&&TellTaleFloorboards.heat()>0,"ordinary labyrinth blocks remain protected, and trying them is loud");
        }helper.succeed();
    }
    @GameTest(template="empty",batch="floorboard_attack") public static void axeAttackWorksThroughAnAlreadyCancelledEvent(GameTestHelper helper){pryFixture(helper,true);}
    @GameTest(template="empty",batch="floorboard_mining") public static void axeBreakFallbackWorksThroughAnAlreadyCancelledEvent(GameTestHelper helper){pryFixture(helper,false);}

    private static Fixture hatchet;private static ServerPlayer hatchetReader;
    @AfterBatch(batch="floorboard_hatchet0505") public static void hatchetDone(net.minecraft.server.level.ServerLevel ignored){if(hatchet!=null){hatchet.close();hatchet=null;}if(hatchetReader!=null){NativeTestPlayers.remove(hatchetReader);hatchetReader=null;}}
    /** 0.4.105: the room's hatchet lies on its chopping block, goes with whoever picks it up, and never leaves the room. */
    @GameTest(template="empty",batch="floorboard_hatchet0505",timeoutTicks=240)
    public static void theRoomsHatchetLiesOnItsBlockAndNeverLeavesTheRoom(GameTestHelper helper){
        var server=helper.getLevel().getServer();var level=HouseTestLevel.get(server);BlockPos origin=new BlockPos(45800,80,45800);
        var player=NativeTestPlayers.survival(helper,"hatchet_reader");hatchetReader=player;hatchet=new Fixture(server,origin);
        BlockPos base=LabyrinthPlaces.base(origin,LabyrinthPlace.FLOORBOARDS),block=base.offset(TellTaleFloorboards.AXE_BLOCK);
        level.getChunkAt(block);TellTaleFloorboards.build(level,base,true);
        player.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()-5.5,0,0);player.setNoGravity(true);
        java.util.function.Supplier<List<net.minecraft.world.entity.decoration.ItemFrame>> frames=()->level.getEntitiesOfClass(net.minecraft.world.entity.decoration.ItemFrame.class,new AABB(block.above()).inflate(.5),frame->TellTaleFloorboards.isHatchet(frame.getItem()));
        java.util.function.IntSupplier carried=()->(int)player.getInventory().items.stream().filter(TellTaleFloorboards::isHatchet).count();
        helper.startSequence().thenWaitUntil(()->{TellTaleFloorboards.keepHatchet(server,level,base);helper.assertTrue(frames.get().size()==1,"the hatchet lies on its chopping block");})
            .thenExecute(()->{
                helper.assertTrue(level.getBlockState(block).is(Blocks.STRIPPED_SPRUCE_LOG)&&frames.get().getFirst().isInvisible(),"a chopping block with the hatchet lying on top, its frame unseen");
                var frame=frames.get().getFirst();
                helper.assertTrue(TellTaleFloorboards.pickUpHatchet(player,frame)&&frame.isRemoved()&&carried.getAsInt()==1,"picking it up puts the whole hatchet in the reader's hands");
                helper.assertTrue(player.getInventory().items.stream().filter(TellTaleFloorboards::isHatchet).allMatch(stack->stack.is(net.minecraft.tags.ItemTags.AXES)),"it is an axe the board will answer to");
                TellTaleFloorboards.keepHatchet(server,level,base);
                helper.assertTrue(frames.get().isEmpty()&&carried.getAsInt()==1,"carried inside the room it stays with the reader");
                player.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()+12.5,0,0);TellTaleFloorboards.keepHatchet(server,level,base);
                helper.assertTrue(carried.getAsInt()==0&&frames.get().size()==1,"carried out of the room it goes back on the block, and only once");
            }).thenSucceed();
    }
    @GameTest(template="empty",batch="floorboard_observer0493")
    public static void observersCannotSpendTheHeartbeatOrBeForcedThroughItsExit(GameTestHelper helper){
        var server=helper.getLevel().getServer();var level=HouseTestLevel.get(server);BlockPos origin=new BlockPos(45400,80,45400);
        var player=NativeTestPlayers.survival(helper,"heartbeat_observer");
        try(var fixture=new Fixture(server,origin)){
            BlockPos base=LabyrinthPlaces.base(origin,LabyrinthPlace.FLOORBOARDS);TellTaleFloorboards.build(level,base,true);
            LabyrinthData.get(server).pushReturn(player.getUUID(),new LabyrinthData.Waypoint(player.serverLevel().dimension(),player.position(),0,false));
            player.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()-5.5,0,0);player.setNoGravity(true);
            for(int i=0;i<25;i++)level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.STEP,player.position(),net.minecraft.world.level.gameevent.GameEvent.Context.of(player));
            helper.assertTrue(TellTaleFloorboards.heat()==TellTaleFloorboards.MAX_HEAT,"an actual participant's native steps can reach the peak");
            player.setGameMode(GameType.SPECTATOR);TellTaleFloorboards.onServerTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(()->true,server));
            double fading=TellTaleFloorboards.heat();
            helper.assertTrue(fading==99.5&&!LabyrinthDoors.isBusy(player),"an observer-only room decays normally without consuming the peak or forcing an exit");
            level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.STEP,player.position(),net.minecraft.world.level.gameevent.GameEvent.Context.of(player));
            helper.assertTrue(TellTaleFloorboards.heat()==fading,"native observer vibrations do not agitate the room");
            player.setGameMode(GameType.SURVIVAL);level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.STEP,player.position(),net.minecraft.world.level.gameevent.GameEvent.Context.of(player));
            TellTaleFloorboards.onServerTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(()->true,server));
            helper.assertTrue(TellTaleFloorboards.heat()==0&&LabyrinthDoors.isBusy(player)&&LabyrinthData.get(server).returnDepth(player.getUUID())==1&&WitnessAccount.count(LabyrinthData.get(server),player.getUUID())==0,"the restored participant still faces the actual peak, retains the route until the native fade completes, and earns no account by failing");
            // 0.4.191: the peak takes calm. Walking back in, the heart starts louder.
            helper.assertTrue(ContestTrades.record(player).getInt("Unease")==1&&ContestTrades.startingHeat(player)==ContestTrades.UNEASE_HEAT,"the reader the heart sent out is unsettled once");
            player.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()+6.5,0,0);TellTaleFloorboards.onServerTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(()->true,server));
            player.teleportTo(level,base.getX()+.5,base.getY(),base.getZ()-5.5,0,0);TellTaleFloorboards.onServerTick(new net.neoforged.neoforge.event.tick.ServerTickEvent.Post(()->true,server));
            helper.assertTrue(TellTaleFloorboards.heat()>=ContestTrades.UNEASE_HEAT-1,"walking back in, the heart starts where the last time left it: "+TellTaleFloorboards.heat());helper.succeed();
        }finally{LabyrinthDoors.clearAll();NativeTestPlayers.remove(player);}
    }

    private static Fixture sceneFixture;
    private static MinotaurEntity sceneCreature;
    private static ServerPlayer scenePlayer;
    private static final Map<net.minecraft.world.level.ChunkPos,BlockPos> SCENE_TICKETS=new HashMap<>();
    @AfterBatch(batch="witness_walk") public static void cleanScene(net.minecraft.server.level.ServerLevel level){
        var interior=level.getServer().getLevel(HouseDimensions.INTERIOR);
        if(interior!=null)for(var entry:SCENE_TICKETS.entrySet()){
            interior.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.PORTAL,entry.getKey(),3,entry.getValue());
        }SCENE_TICKETS.clear();
        if(scenePlayer!=null){level.getServer().getPlayerList().remove(scenePlayer);scenePlayer=null;}
        if(sceneCreature!=null){sceneCreature.discard();sceneCreature=null;}if(sceneFixture!=null){sceneFixture.close();sceneFixture=null;}
    }
    @GameTest(template="empty",batch="witness_walk",timeoutTicks=1800) public static void releasedCreaturePhysicallyReachesAndClimbsTheStairs(GameTestHelper helper){
        var server=helper.getLevel().getServer();var level=HouseTestLevel.get(server);BlockPos origin=new BlockPos(3400,80,3400);
        sceneFixture=new Fixture(server,origin);
        for(var chunk:WitnessEnding.releaseChunks(origin)){
            BlockPos at=chunk.getWorldPosition();SCENE_TICKETS.put(chunk,at);
            level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL,chunk,3,at);
        }
        for(var placement:FinaleArchitecture.plan(origin))level.setBlock(placement.pos(),placement.block(),2);
        FinaleArchitecture.openCell(level,origin);FinaleArchitecture.seal(level,origin,true);
        BlockPos cell=FinaleArchitecture.cell(origin);scenePlayer=helper.makeMockServerPlayerInLevel();ServerPlayer player=scenePlayer;
        Vec3 stand=Vec3.atBottomCenterOf(cell.north(3));player.teleportTo(level,stand.x,stand.y,stand.z,0,0);
        CompoundTag record=new CompoundTag();record.putString("Phase",FinaleProgress.Phase.RELEASE.name());record.putInt("PassSide",1);
        sceneCreature=FinaleRegistry.MINOTAUR.get().create(level);sceneCreature.owner(player.getUUID());sceneCreature.released();sceneCreature.moveTo(Vec3.atBottomCenterOf(cell.south(5)));level.addFreshEntity(sceneCreature);
        record.putUUID("Creature",sceneCreature.getUUID());FinaleProgress.save(server,player.getUUID(),record);
        var creature=sceneCreature;
        helper.succeedWhen(()->{
            // The mock player's connection does not dispatch ordinary House player-tick hooks.
            FinaleController.tickPlayer(player,origin);
            helper.assertTrue(FinaleProgress.phase(server,player.getUUID())==FinaleProgress.Phase.HOMEWARD,
                    "the native creature must walk its release route; step="+FinaleProgress.player(server,player.getUUID()).getInt("ReleaseStep")
                    +"; position="+creature.position()+"; actorTicks="+creature.tickCount+"; playerTicks="+player.tickCount+"; motion="+creature.motion()
                    +"; online="+(server.getPlayerList().getPlayer(player.getUUID())==player)+"; ownerLevel="+player.level().dimension().location()
                    +"; levelPlayers="+level.players().size()+"; forced="+level.getForcedChunks().contains(creature.chunkPosition().toLong())
                    +"; grounded="+creature.onGround()+"; stepHeight="+creature.maxUpStep()+"; verticalSpeed="+creature.getDeltaMovement().y
                    +"; tracked="+(level.getEntity(creature.getUUID())==creature)+"; northBlocked="+level.getBlockCollisions(creature,creature.getBoundingBox().move(0,0,-.3)).iterator().hasNext());
            helper.assertTrue(creature.isRemoved()&&creature.getY()>FinaleArchitecture.ARENA+10,"departure happens after actual stair ascent");
            helper.assertTrue(level.getBlockState(cell).isAir()&&level.getBlockState(FinaleArchitecture.base(origin).offset(0,FinaleArchitecture.ARENA,29)).isAir(),"the cell and physical return passage stay open");
        });
    }
    private static Fixture returnFixture;
    private static BlockPos returnTicket;
    private static ServerPlayer returnPlayer;
    @AfterBatch(batch="witness_return") public static void cleanReturn(net.minecraft.server.level.ServerLevel level){
        if(returnTicket!=null&&level.getServer().getLevel(HouseDimensions.INTERIOR)!=null){level.getServer().getLevel(HouseDimensions.INTERIOR).getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(returnTicket),3,returnTicket);returnTicket=null;}
        if(returnPlayer!=null){level.getServer().getPlayerList().remove(returnPlayer);returnPlayer=null;}
        if(returnFixture!=null){returnFixture.close();returnFixture=null;}}
    @GameTest(template="empty",batch="witness_return") public static void releaseReturnsTheRealWeaponAndKeepsTheDayAndHouse(GameTestHelper helper){
        var server=helper.getLevel().getServer();var level=HouseTestLevel.get(server);BlockPos origin=new BlockPos(4700,80,4700);
        returnFixture=new Fixture(server,origin);BlockPos at=FinaleArchitecture.cell(origin).north(3);level.getChunkAt(at);
        returnTicket=at;level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.PORTAL,new net.minecraft.world.level.ChunkPos(at),3,at);
        level.setBlock(at.below(),Blocks.POLISHED_DEEPSLATE.defaultBlockState(),3);
        BlockPos doorstep=origin.offset(HouseLayout.FRONT_DOOR.x(),HouseLayout.FRONT_DOOR.y()-1,HouseLayout.FRONT_DOOR.z()-2);
        server.overworld().getChunkAt(doorstep);for(BlockPos ground:BlockPos.betweenClosed(doorstep.offset(-3,0,-3),doorstep.offset(3,0,0)))server.overworld().setBlock(ground,Blocks.STONE.defaultBlockState(),3);
        returnPlayer=helper.makeMockServerPlayerInLevel();ServerPlayer player=returnPlayer;Vec3 stand=Vec3.atBottomCenterOf(at);player.teleportTo(level,stand.x,stand.y,stand.z,0,0);
        ItemStack sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(49);UUID original=WeaponHistory.stamp(sword);
        ItemEntity item=new ItemEntity(level,at.getX()+.5,at.getY(),at.getZ()+.5,sword);item.setUnlimitedLifetime();item.setPickUpDelay(32767);level.addFreshEntity(item);
        player.getInventory().add(new ItemStack(Items.EMERALD,7));
        for(int i=1;i<player.getInventory().items.size();i++)player.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        CompoundTag record=new CompoundTag();record.putString("Phase",FinaleProgress.Phase.HOMEWARD.name());record.putUUID("LaidDown",item.getUUID());record.putLong("LaidAt",at.asLong());
        FinaleProgress.save(server,player.getUUID(),record);
        helper.runAfterDelay(40,()->{
            helper.assertTrue(!WitnessEnding.returnWeapon(player,record)&&record.hasUUID("LaidDown")&&!item.isRemoved(),"a full inventory cannot discard or abandon the original weapon");
            player.getInventory().setItem(35,ItemStack.EMPTY);
            long day=server.overworld().getDayTime();WitnessEnding.finish(player,origin,record);
            helper.assertTrue(FinaleProgress.phase(server,player.getUUID())==FinaleProgress.Phase.WITNESSED&&FinaleController.lockedOut(player),"the actual return saves the third terminal ending");
            helper.assertTrue(player.getInventory().items.stream().anyMatch(s->WeaponHistory.wounds(s,original)&&s.getDamageValue()==49)&&item.isRemoved(),"the physical original is recovered once, with its wear");
            helper.assertTrue(player.getInventory().countItem(Items.EMERALD)==7&&player.getInventory().countItem(Items.COBBLESTONE)==34*64
                    &&server.overworld().getDayTime()==day&&HouseSavedData.get(server).houseOrigin().equals(origin),"possessions, the same day, and the House survive release");
            helper.assertTrue(!FinaleProgress.world(server).getBoolean("Ended"),"release never starts world demolition");helper.succeed();
        });
    }
}
