package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.brigadier.CommandDispatcher;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.command.*;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID + "_multiplayer")
@PrefixGameTestTemplate(false)
public final class PlaytestCommandTests {
    private static final List<StaircaseAccessTests.Fixture> ACTIVE = new ArrayList<>();
    @FunctionalInterface private interface Check { boolean run(StaircaseAccessTests.Fixture f, int tick); }
    private static CommandDispatcher<CommandSourceStack> commands() {
        var dispatcher = new CommandDispatcher<CommandSourceStack>(); HouseCommands.register(dispatcher); return dispatcher;
    }
    private static int command(CommandDispatcher<CommandSourceStack> d, ServerPlayer p, String text) {
        try { return d.execute("oldesthouse test " + text, p.createCommandSourceStack().withPermission(2)); }
        catch (Exception e) { throw new IllegalStateException(text, e); }
    }
    private static void run(GameTestHelper h, int coordinate, Check check) {
        var f = new StaircaseAccessTests.Fixture(h, coordinate); ACTIVE.add(f);
        var b = f.base; f.chunks.hold(f.level, new AABB(b.getX()-27, 0, b.getZ()+27, b.getX()+27, 160, b.getZ()+115));
        for (var chunk : WitnessEnding.releaseChunks(f.origin)) f.chunks.hold(f.level, new AABB(chunk.getWorldPosition()).inflate(1));
        for (int x=-16; x<=16; x++) for (int z=30; z<=70; z++)
            f.put(f.level, b.offset(x, FinaleArchitecture.ARENA-1, z), Blocks.DEEPSLATE_TILES.defaultBlockState());
        for (int x=-1; x<=1; x++) for (int y=0; y<4; y++)
            f.put(f.level, FinaleArchitecture.cell(f.origin).offset(x,y,0), Blocks.IRON_BARS.defaultBlockState());
        int[] tick={0};
        h.onEachTick(() -> {
            if (!f.chunks.ready()) return;
            if (!f.started) {
                f.start(); f.data().setBuilt(LabyrinthBuilder.VERSION, f.origin);
                var architecture=f.data().state("finale_architecture_049"); architecture.putLong("Origin",f.origin.asLong());
                f.data().setState("finale_architecture_049",architecture);
                // These fixtures exercise commands, not a second composition of the complete escape course.
                var geometry=new net.minecraft.nbt.CompoundTag(); geometry.putLong("Origin",f.origin.asLong());
                f.data().setState("collapse_geometry_0423",geometry);
            }
            try { if (check.run(f,tick[0]++)) { close(f); h.succeed(); } }
            catch (Throwable e) { close(f); throw e; }
        });
    }
    private static void close(StaircaseAccessTests.Fixture f) {
        PlaytestCommands.clear(f.level.getServer());
        for (var chunk : WitnessEnding.releaseChunks(f.origin))
            f.level.getChunkSource().removeRegionTicket(TicketType.PORTAL,chunk,3,chunk.getWorldPosition());
        var entities=new ArrayList<Entity>();
        for(var e:f.level.getAllEntities()) if(FinaleArchitecture.contains(f.origin,e.blockPosition())
                &&(e instanceof MinotaurEntity||e instanceof ItemEntity||e.getTags().contains("HouseFinaleWords"))) entities.add(e);
        entities.forEach(Entity::discard); f.close(); ACTIVE.remove(f);
    }
    private static void cleanup() { for(var f:new ArrayList<>(ACTIVE)) close(f); }
    @AfterBatch(batch="test_command_doors") public static void doorsCleanup(ServerLevel l){cleanup();}
    @AfterBatch(batch="test_command_cell") public static void cellCleanup(ServerLevel l){cleanup();}
    @AfterBatch(batch="test_command_claim") public static void claimCleanup(ServerLevel l){cleanup();}
    @AfterBatch(batch="test_command_collapse") public static void collapseCleanup(ServerLevel l){cleanup();}
    @AfterBatch(batch="test_command_witness") public static void witnessCleanup(ServerLevel l){cleanup();}
    @AfterBatch(batch="test_command_defeat") public static void defeatCleanup(ServerLevel l){cleanup();}
    @AfterBatch(batch="test_command_cancel") public static void cancelCleanup(ServerLevel l){cleanup();}
    private static ServerPlayer source(GameTestHelper h, StaircaseAccessTests.Fixture f, String name) {
        var p=f.player(h,name); p.teleportTo(f.level,f.source.getX()+.5,f.source.getY(),f.source.getZ()+4.5,0,0);
        p.connection.resetPosition(); return p;
    }
    private static MinotaurEntity prisoner(StaircaseAccessTests.Fixture f) { return FinaleController.ensureCaged(f.level,f.origin); }

    @GameTest(template="empty",batch="test_command_doors",timeoutTicks=1200)
    public static void everySceneHasATabCompletableDoorAndTheAliasUsesTheRealDoor(GameTestHelper h){run(h,540000,(f,t)->{
        var d=commands(); var p=source(h,f,"test_door_owner");var peer=source(h,f,"test_door_peer");
        var mine=d.getRoot().getChild("oldesthouse").getChild("test").getChild("vignette");
        for(var place:LabyrinthPlace.values()) if(place.slot()>=0)
            h.assertTrue(mine.getChild(place.id())!=null,"every authored scene is available by its real id: "+place.id());
        h.assertTrue(!d.getRoot().getChild("oldesthouse").canUse(p.createCommandSourceStack().withPermission(0)),"ordinary players cannot run operator shortcuts");
        var lower=p.blockPosition().relative(p.getDirection(),2);f.put(f.level,lower.below(),Blocks.STONE.defaultBlockState());
        h.assertTrue(command(d,p,"vignette floorboards")==1,"the explicit alias places a native test door");
        var door=f.data().doorAt(f.level.dimension(),lower);
        h.assertTrue(door!=null&&door.command&&door.destination.equals("place:floorboards")&&f.level.getBlockState(lower).getBlock() instanceof DoorBlock&&f.level.getBlockState(lower.above()).getBlock() instanceof DoorBlock,"both native halves and the actual authored destination are registered");
        h.assertTrue(command(d,p,"vignette floorboards")==0,"an occupied doorway is not overwritten");
        h.assertTrue(FinaleProgress.phase(p.server,peer.getUUID())==FinaleProgress.Phase.UNSEEN&&WitnessAccount.count(f.data(),peer.getUUID())==0,"placing the shared door grants no peer participation");
        h.assertTrue(command(d,p,"vignette remove")==1&&f.data().doorAt(f.level.dimension(),lower)==null&&f.level.getBlockState(lower).isAir(),"removal uses the original test door registry");return true;
    });}

    @GameTest(template="empty",batch="test_command_cell",timeoutTicks=1200)
    public static void cellShortcutPreservesOriginalsAndDoesNotGrantStairFires(GameTestHelper h){run(h,540500,(f,t)->{
        var p=source(h,f,"test_cell_owner");var peer=source(h,f,"test_cell_peer"); var d=commands();
        var book=new ItemStack(Items.WRITTEN_BOOK);book.set(DataComponents.CUSTOM_NAME,Component.literal("Untouched account"));p.setItemInHand(InteractionHand.OFF_HAND,book);
        var before=book.copy();var peerAt=peer.position();var actor=prisoner(f);var actorId=actor.getUUID();
        var record=FinaleProgress.player(p.server,p.getUUID());record.putString("Phase","STAIRCASE");record.putBoolean("StairFireVersion",true);record.putInt("StairFires",0);FinaleProgress.save(p.server,p.getUUID(),record);
        h.assertTrue(command(d,p,"boy")==1&&p.position().distanceToSqr(Vec3.atBottomCenterOf(FinaleArchitecture.cell(f.origin).north(6)))>100,"queuing never moves the player synchronously");
        PlaytestCommands.tick(p.server);
        h.assertTrue(p.position().distanceToSqr(Vec3.atBottomCenterOf(FinaleArchitecture.cell(f.origin).north(6)))<.01,"the selected player lands in front of the loaded native cell");
        var at=p.position();record=FinaleProgress.player(p.server,p.getUUID());
        h.assertTrue(!StaircaseFire.tick(p,f.origin,record)&&p.position().equals(at)&&StaircaseFire.flames(record)==0,"inspecting the cell neither bounces upstairs nor grants a burned fire");
        h.assertTrue(actorId.equals(FinaleProgress.world(p.server).getUUID("CagedCreature"))&&actor.motion()==MinotaurEntity.CAGED&&actor.owner()==null&&ItemStack.isSameItemSameComponents(before,p.getOffhandItem())&&p.gameMode.getGameModeForPlayer()==GameType.SURVIVAL,"the same boy, exact carried original and native game mode survive");
        h.assertTrue(peer.position().equals(peerAt)&&FinaleProgress.phase(p.server,peer.getUUID())==FinaleProgress.Phase.UNSEEN&&!FinaleProgress.world(p.server).hasUUID("Owner"),"the shortcut moves only its reader and does not claim the finale");
        var outside=f.base.offset(0,FinaleArchitecture.ARENA,28);p.teleportTo(f.level,outside.getX()+.5,outside.getY(),outside.getZ()+.5,0,0);
        h.assertTrue(StaircaseFire.tick(p,f.origin,record)&&!record.getBoolean("OperatorCellVisit")&&StaircaseFire.flames(record)==0,"leaving the chamber restores the actual unlit gate without grandfathering five fires");return true;
    });}

    @GameTest(template="empty",batch="test_command_claim",timeoutTicks=1200)
    public static void offlineClaimsAndCompletedEndingsCannotBeResetByShortcuts(GameTestHelper h){run(h,541000,(f,t)->{
        var p=source(h,f,"test_claim_peer");var d=commands();var before=p.position();UUID offline=UUID.randomUUID();
        var record=new net.minecraft.nbt.CompoundTag();record.putString("Phase","RELEASE");FinaleProgress.save(p.server,offline,record);
        var world=FinaleProgress.world(p.server);world.putUUID("Owner",offline);f.data().setState(FinaleProgress.STATE,world);
        for(String text:List.of("minotaur","ending defeat","ending escape","ending witness")) h.assertTrue(command(d,p,text)==0,"an offline owner's actual cell remains reserved: "+text);
        PlaytestCommands.tick(p.server);h.assertTrue(p.position().equals(before)&&FinaleProgress.phase(p.server,offline)==FinaleProgress.Phase.RELEASE&&FinaleProgress.world(p.server).getUUID("Owner").equals(offline),"the owner, peer position and saved release remain exact");
        world=FinaleProgress.world(p.server);world.remove("Owner");f.data().setState(FinaleProgress.STATE,world);FinaleProgress.phase(p.server,p.getUUID(),FinaleProgress.Phase.WITNESSED);
        h.assertTrue(command(d,p,"minotaur")==0&&command(d,p,"ending escape")==0&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.WITNESSED,"terminal exclusion is never silently cleared");return true;
    });}

    @GameTest(template="empty",batch="test_command_collapse",timeoutTicks=1200)
    public static void escapeShortcutWoundsTheExistingCreatureAndStartsOnlyItsOwnerClock(GameTestHelper h){run(h,541500,(f,t)->{
        var p=source(h,f,"test_escape_owner");var peer=source(h,f,"test_escape_peer");var d=commands();var actor=prisoner(f);var id=actor.getUUID();
        var original=new ItemStack(Items.IRON_SWORD);original.setDamageValue(63);WeaponHistory.record(p,original,12);p.setItemInHand(InteractionHand.MAIN_HAND,original);var before=original.copy();
        var peerAt=peer.position();h.assertTrue(command(d,p,"ending escape")==1,"escape fixture is accepted");PlaytestCommands.tick(p.server);
        var record=FinaleProgress.player(p.server,p.getUUID());
        h.assertTrue(FinaleProgress.phase(record)==FinaleProgress.Phase.COLLAPSE&&record.getInt("CollapseTicks")==0&&id.equals(record.getUUID("Creature"))&&id.equals(FinaleProgress.world(p.server).getUUID("WoundedCreature")),"native commitment and the first real collapse beat retain one actor identity");
        h.assertTrue(actor.isAlive()&&actor.motion()==MinotaurEntity.WOUNDED&&actor.getHealth()==actor.getMaxHealth()&&ItemStack.isSameItemSameComponents(before,p.getMainHandItem()),"the actual prisoner crawls alive and no weapon is copied or consumed");
        h.assertTrue(peer.position().equals(peerAt)&&FinaleProgress.phase(p.server,peer.getUUID())==FinaleProgress.Phase.UNSEEN&&WitnessAccount.count(f.data(),peer.getUUID())==0&&!FinaleProgress.world(p.server).getBoolean("Ended"),"starting collapse neither completes demolition nor changes the peer's journey");return true;
    });}

    @GameTest(template="empty",batch="test_command_witness",timeoutTicks=1200)
    public static void releaseFixtureStoresEquipmentAndPreservesTheSingleNativePrisoner(GameTestHelper h){run(h,542000,(f,t)->{
        var p=source(h,f,"test_witness_owner");var peer=source(h,f,"test_witness_peer");var d=commands();var actor=prisoner(f);var id=actor.getUUID();
        var sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(91);sword.set(DataComponents.CUSTOM_NAME,Component.literal("My first edge"));WeaponHistory.record(p,sword,50);
        p.setItemInHand(InteractionHand.MAIN_HAND,sword);var shield=new ItemStack(Items.SHIELD);shield.setDamageValue(37);p.setItemInHand(InteractionHand.OFF_HAND,shield);
        for(int i=1;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        var at=p.position();var account=WitnessAccount.record(f.data(),p.getUUID());
        h.assertTrue(command(d,p,"ending witness")==1,"the fixture can be queued before its equipment preflight");PlaytestCommands.tick(p.server);
        h.assertTrue(p.position().equals(at)&&p.getMainHandItem()==sword&&p.getOffhandItem()==shield&&WitnessAccount.record(f.data(),p.getUUID()).equals(account)&&!FinaleProgress.world(p.server).hasUUID("Owner"),"a full inventory refuses before moving or changing any equipment/account/claim");
        p.getInventory().setItem(1,ItemStack.EMPTY);var beforeSword=sword.copy();var beforeShield=shield.copy();
        h.assertTrue(!WitnessEnding.begin(p),"ordinary peaceful release still requires the real progression and proximity");
        h.assertTrue(command(d,p,"ending witness")==1,"a free storage slot allows the explicit operator fixture");PlaytestCommands.tick(p.server);
        var record=FinaleProgress.player(p.server,p.getUUID());var item=f.level.getEntity(record.getUUID("LaidDown"));
        h.assertTrue(FinaleProgress.phase(record)==FinaleProgress.Phase.RELEASE&&id.equals(record.getUUID("Creature"))&&actor.motion()==MinotaurEntity.RELEASED&&p.getUUID().equals(actor.owner()),"the real saved release starts with the existing boy rather than another entity");
        h.assertTrue(item instanceof ItemEntity laid&&ItemStack.isSameItemSameComponents(laid.getItem(),beforeSword)&&ItemStack.isSameItemSameComponents(p.getInventory().getItem(1),beforeShield)&&p.getMainHandItem().isEmpty()&&p.getOffhandItem().isEmpty()&&!p.isShiftKeyDown(),"the worn original is laid once, the exact shield is stored, both hands are empty, and the prior crouch input is restored");
        h.assertTrue(WitnessAccount.ready(f.data(),p.getUUID())&&WitnessAccount.readPlay(f.data(),p.getUUID())&&WitnessAccount.count(f.data(),peer.getUUID())==0&&FinaleProgress.phase(p.server,peer.getUUID())==FinaleProgress.Phase.UNSEEN,"only this deliberate operator's account is staged");
        h.assertTrue(f.level.getEntitiesOfClass(MinotaurEntity.class,new AABB(FinaleArchitecture.cell(f.origin)).inflate(12),Entity::isAlive).size()==1,"there is only one live native prisoner");return true;
    });}

    @GameTest(template="empty",batch="test_command_defeat",timeoutTicks=1200)
    public static void defeatShortcutUsesNativeDeathAndSealsOnlyTheSelectedInventory(GameTestHelper h){run(h,542500,(f,t)->{
        var p=source(h,f,"test_defeat_owner");var peer=source(h,f,"test_defeat_peer");var d=commands();prisoner(f);
        var oldMother=MotherCollection.get(p.server);p.server.overworld().getDataStorage().set("the_oldest_house_mother",new MotherCollection());
        try {
            var book=new ItemStack(Items.PAPER,11);book.set(DataComponents.CUSTOM_NAME,Component.literal("Original loose pages"));var before=book.copy();p.getInventory().setItem(3,book);
            var cursor=new ItemStack(Items.DIAMOND,2);p.containerMenu.setCarried(cursor);var peerAt=peer.position();peer.getInventory().setItem(3,new ItemStack(Items.EMERALD,7));
            h.assertTrue(command(d,p,"ending defeat")==1,"defeat shortcut is explicit");PlaytestCommands.tick(p.server);
            var collection=MotherCollection.get(p.server);
            h.assertTrue(!p.isAlive()&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.LOCKED_OUT&&FinaleProgress.player(p.server,p.getUUID()).getBoolean("NeedsRespawn"),"actual native death invokes the existing permanent defeat and respawn pipeline: alive="+p.isAlive()+" phase="+FinaleProgress.phase(p.server,p.getUUID())+" health="+p.getHealth());
            h.assertTrue(collection.all().stream().anyMatch(e->e.sealed&&e.owner.equals(p.getUUID())&&ItemStack.isSameItemSameComponents(e.item(p.registryAccess()),before))&&collection.all().stream().anyMatch(e->e.sealed&&e.item(p.registryAccess()).is(Items.DIAMOND)&&e.item(p.registryAccess()).getCount()==2),"inventory and actual cursor components enter sealed native custody");
            h.assertTrue(peer.isAlive()&&peer.position().equals(peerAt)&&peer.getInventory().countItem(Items.EMERALD)==7&&FinaleProgress.phase(p.server,peer.getUUID())==FinaleProgress.Phase.UNSEEN&&!FinaleProgress.world(p.server).hasUUID("Owner"),"the peer keeps their life, position, property and ending, and the selected claim releases");
        } finally {p.server.overworld().getDataStorage().set("the_oldest_house_mother",oldMother);}return true;
    });}

    @GameTest(template="empty",batch="test_command_cancel",timeoutTicks=1200)
    public static void cancellationLogoutAndDimensionChangesLeaveNoDeferredEnding(GameTestHelper h){run(h,543000,(f,t)->{
        var p=source(h,f,"test_cancel_owner");var d=commands();var at=p.position();
        h.assertTrue(command(d,p,"minotaur")==1&&command(d,p,"ending defeat")==0&&command(d,p,"cancel")==1,"duplicate queues refuse and explicit cancellation releases the pending request");PlaytestCommands.tick(p.server);
        h.assertTrue(p.position().equals(at)&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.UNSEEN,"a canceled queue cannot move or advance the player");
        h.assertTrue(command(d,p,"ending defeat")==1,"the next request can be queued");
        PlaytestCommands.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));PlaytestCommands.tick(p.server);
        h.assertTrue(p.isAlive()&&p.position().equals(at)&&command(d,p,"cancel")==0,"logout removes the exact pending entity before an ending can fire");
        h.assertTrue(command(d,p,"minotaur")==1,"a final request can be queued");p.teleportTo(h.getLevel(),0.5,100,0.5,0,0);PlaytestCommands.tick(p.server);
        h.assertTrue(p.serverLevel()==h.getLevel()&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.UNSEEN&&command(d,p,"cancel")==0,"an unrelated dimension change cancels without dragging the reader back");
        h.assertTrue(command(d,p,"ending defeat")==1,"a request can wait before a world reset");
        try {d.execute("oldesthouse reset",p.createCommandSourceStack().withPermission(2));}catch(Exception e){throw new IllegalStateException(e);}
        HouseSavedData.get(p.server).markSpawned(f.origin);
        PlaytestCommands.tick(p.server);
        h.assertTrue(p.isAlive()&&command(d,p,"cancel")==0&&FinaleProgress.phase(p.server,p.getUUID())==FinaleProgress.Phase.UNSEEN,"reset cancels even when a House immediately respawns at exactly the same coordinates");return true;
    });}
}
