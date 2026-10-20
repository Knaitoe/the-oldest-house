package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.brigadier.CommandDispatcher;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.command.HouseCommands;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.DeliveredItemEntity;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID + "_multiplayer")
@PrefixGameTestTemplate(false)
public final class HomeIntrusionTests {
    private static final List<Fixture> ACTIVE = new ArrayList<>();
    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final BlockPos origin, cache;
        final NativeTestChunks chunks = new NativeTestChunks();
        final List<ServerPlayer> players = new ArrayList<>();
        HouseSavedData oldHouse;
        LabyrinthData oldData;
        boolean started, closed;
        Fixture(GameTestHelper h, int coordinate) {
            level = HouseTestLevel.get(h.getLevel().getServer());
            origin = new BlockPos(coordinate, 40, coordinate);
            cache = HomeBelongings.cache(origin);
            chunks.hold(level, new AABB(origin).inflate(35));
            chunks.hold(level, new AABB(cache).inflate(4));
        }
        void start() {
            oldHouse = HouseSavedData.get(level.getServer()); oldData = data(); started = true;
            var house = new HouseSavedData(); house.markSpawned(origin); house.markInteriorInitialized();
            level.getServer().overworld().getDataStorage().set("the_oldest_house", house);
            level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth", new LabyrinthData());
            for (var at : HomeKnocking.walls(origin)) level.setBlock(at, Blocks.OAK_PLANKS.defaultBlockState(), 3);
            for (var at : HomeBelongings.places(origin)) {
                level.setBlock(at.below(), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(at.above(), Blocks.AIR.defaultBlockState(), 3);
            }
            level.setBlock(origin.offset(10, 0, 10), Blocks.OAK_PLANKS.defaultBlockState(), 3);
            level.setBlock(cache.below(), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(cache, Blocks.BARREL.defaultBlockState(), 3);
            barrel().setItem(0, new ItemStack(Items.BREAD, 6));
        }
        LabyrinthData data() { return LabyrinthData.get(level.getServer()); }
        BarrelBlockEntity barrel() { return (BarrelBlockEntity) level.getBlockEntity(cache); }
        ServerPlayer player(GameTestHelper h, String name) {
            var p = NativeTestPlayers.survival(h, name); p.setNoGravity(true); players.add(p); home(p); return p;
        }
        void at(ServerPlayer p, BlockPos at, float yaw, float pitch) {
            p.teleportTo(level, at.getX() + .5, at.getY(), at.getZ() + .5, yaw, pitch);
            p.hasChangedDimension(); p.setDeltaMovement(Vec3.ZERO); p.connection.resetPosition();
        }
        void home(ServerPlayer p) { at(p, origin.offset(10, 1, 10), 180, 0); }
        ChestMenu open(ServerPlayer p) {
            at(p, cache.south(), 180, 0);
            p.openMenu(new SimpleMenuProvider((id, inv, reader) -> ChestMenu.threeRows(id, inv, barrel()), Component.literal("Expedition cache")));
            return (ChestMenu) p.containerMenu;
        }
        void returnHome(ServerPlayer p) {
            home(p); HouseExperience.returned(p);
            for (int i = 0; i < 12; i++) HomeBelongings.tickPlayer(p);
        }
        List<DeliveredItemEntity> returned() {
            return level.getEntitiesOfClass(DeliveredItemEntity.class, new AABB(origin).inflate(35));
        }
        void reload() {
            var loaded = LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(), level.registryAccess()), level.registryAccess());
            level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth", loaded);
        }
        @Override public void close() {
            if (closed) return; closed = true;
            if (started) {
                for (var p : players) { p.closeContainer(); NativeTestPlayers.remove(p); }
                HomeBelongings.clear(level.getServer());
                returned().forEach(net.minecraft.world.entity.Entity::discard);
                if (barrel() != null) barrel().clearContent();
                level.removeBlock(cache, false);
                for (var at : HomeKnocking.walls(origin)) level.removeBlock(at, false);
                for (var at : HomeBelongings.places(origin)) { level.removeBlock(at, false); level.removeBlock(at.below(), false); }
                level.removeBlock(cache.below(), false); level.removeBlock(origin.offset(10, 0, 10), false);
                level.getServer().overworld().getDataStorage().set("the_oldest_house", oldHouse);
                level.getServer().overworld().getDataStorage().set("the_oldest_house_labyrinth", oldData);
            }
            chunks.close(); ACTIVE.remove(this);
        }
    }

    @AfterBatch(batch = "home_intrusions") public static void cleanup(ServerLevel level) {
        for (var f : new ArrayList<>(ACTIVE)) f.close();
    }
    private static void run(GameTestHelper h, int coordinate, Consumer<Fixture> check) {
        var f = new Fixture(h, coordinate); ACTIVE.add(f);
        h.startSequence().thenWaitUntil(() -> h.assertTrue(f.chunks.ready(), "the original cache and home entity sections are ready"))
                .thenExecute(() -> { f.start(); try { check.accept(f); h.succeed(); } finally { f.close(); } });
    }
    private static ItemStack keepsake() {
        var stack = new ItemStack(Items.IRON_PICKAXE); stack.setDamageValue(37);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("The pick I left behind"));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID("NativeOriginal", UUID.randomUUID()));
        return stack;
    }
    private static int deposit(GameTestHelper h, Fixture f, ServerPlayer p, ItemStack original) {
        ItemStack expected = original.copy();
        p.getInventory().setItem(0, original);
        HouseExperience.arrived(p, LabyrinthPlace.EXPLORER_CAMP);
        var menu = f.open(p);
        h.assertTrue(!menu.quickMoveStack(p, 54).isEmpty(), "native inventory transfer deposits the held original into a vacant cache slot");
        for (int i = 1; i < 3; i++) if (!p.getInventory().getItem(i).isEmpty()) menu.quickMoveStack(p, 54 + i);
        p.closeContainer();
        h.assertTrue(HomeBelongings.record(p).getBoolean("Pending"), "the actual native close event records this reader's deposit");
        for (int slot = 0; slot < f.barrel().getContainerSize(); slot++) if (ItemStack.matches(f.barrel().getItem(slot), expected)) return slot;
        throw new AssertionError("The native cache lost its deposited original");
    }

    @GameTest(template = "empty", batch = "home_intrusions", timeoutTicks = 1000)
    public static void manorKnockingUsesPrivatePositionalPacketsAndPausesOutsideOrInSpectator(GameTestHelper h) {
        run(h, 1540000, f -> {
            var p = f.player(h, "home_listener"); var peer = f.player(h, "home_peer");
            h.assertTrue(HomeKnocking.start(p), "the actual manor partition starts the personal sound");
            NativeTestPlayers.sounds(p); NativeTestPlayers.sounds(peer);
            for (int i = 0; i < 320; i++) HomeKnocking.tickPlayer(p);
            var heard = NativeTestPlayers.sounds(p).stream().filter(s -> s.getSound().value() == LabyrinthRegistry.HOME_KNOCK.get()).toList();
            h.assertTrue(heard.size() == 3 && heard.get(0).getZ() == f.origin.getZ() + 15.5
                    && heard.get(1).getPitch() < heard.get(0).getPitch(), "three native wall-positioned groups include the slower middle signal");
            h.assertTrue(NativeTestPlayers.sounds(peer).isEmpty() && HomeKnocking.record(peer).isEmpty(), "a nearby peer receives neither packets nor participation");
            int before = HomeKnocking.record(p).getInt("Elapsed");
            f.at(p, f.origin.north(10), 0, 0);
            for (int i = 0; i < 50; i++) HomeKnocking.tickPlayer(p);
            f.home(p); p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
            for (int i = 0; i < 50; i++) HomeKnocking.tickPlayer(p);
            h.assertTrue(HomeKnocking.record(p).getInt("Elapsed") == before, "departure and a spectator's presence cannot consume the listening window");
            p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
            f.at(p, HomeKnocking.walls(f.origin).getFirst().north(), 0, 0); p.setShiftKeyDown(true); p.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            HomeKnocking.tickPlayer(p); f.reload();
            for (int i = 0; i < 120; i++) HomeKnocking.tickPlayer(p);
            var answer = NativeTestPlayers.sounds(p).stream().filter(s -> s.getSound().value() == LabyrinthRegistry.HOME_KNOCK.get()).toList();
            h.assertTrue(answer.size() == 1 && answer.getFirst().getX() == f.origin.getX() + 13.5
                    && HomeKnocking.record(p).getBoolean("Listened") && HomeKnocking.record(p).getBoolean("Done"), "a real approach earns one answer at the other study wall across saved-data reload");
            h.assertTrue(WitnessAccount.count(f.data(), p.getUUID()) == 0, "a domestic sound grants no story resolution");
        });
    }

    @GameTest(template = "empty", batch = "home_intrusions", timeoutTicks = 1000)
    public static void actualDepositedOriginalReturnsToTheMainHomeOnceWithEveryComponentIntact(GameTestHelper h) {
        run(h, 1540500, f -> {
            var p = f.player(h, "home_original"); var peer = f.player(h, "home_borrower");
            ItemStack cord = new ItemStack(Items.STRING, 4); cord.set(DataComponents.CUSTOM_NAME, Component.literal("My tied cord"));
            ItemStack paper = new ItemStack(Items.PAPER, 2); paper.set(DataComponents.CUSTOM_NAME, Component.literal("My field pages"));
            var expectedCord = cord.copy(); var expectedPaper = paper.copy();
            p.getInventory().setItem(1, cord); p.getInventory().setItem(2, paper);
            var original = keepsake(); var expected = original.copy(); int slot = deposit(h, f, p, original);
            h.assertTrue(f.barrel().getItem(0).getCount() == 6, "the expedition's original finite bread remains untouched");
            f.reload(); f.returnHome(p);
            var returned = f.returned();
            h.assertTrue(returned.size() == 3 && f.barrel().getItem(slot).isEmpty()
                    && returned.stream().anyMatch(e -> ItemStack.matches(e.getItem(), expected))
                    && returned.stream().anyMatch(e -> ItemStack.matches(e.getItem(), expectedCord))
                    && returned.stream().anyMatch(e -> ItemStack.matches(e.getItem(), expectedPaper)), "all three native source stacks empty and their exact components and counts lie separately at home");
            var laid = returned.stream().filter(e -> ItemStack.matches(e.getItem(), expected)).findFirst().orElseThrow(); UUID entity = laid.getUUID();
            h.assertTrue(HouseExperience.inManor(p, laid.blockPosition()) && HomeBelongings.record(peer).isEmpty(), "the item is in the real manor and a peer gets no recalled inventory");
            f.reload(); for (int i = 0; i < 20; i++) HomeBelongings.tickPlayer(p);
            HomeBelongings.reset(p); HomeBelongings.tickPlayer(p);
            h.assertTrue(f.returned().size() == 3 && f.returned().stream().anyMatch(e -> e.getUUID().equals(entity))
                    && ItemStack.matches(laid.getItem(), expected), "saved completion and operator reset never reproduce or remove a returned original");
            var peersOriginal = keepsake(); var peersExpected = peersOriginal.copy();
            peer.setItemInHand(InteractionHand.MAIN_HAND, peersOriginal);
            h.assertTrue(HomeBelongings.stageHeld(peer) && HomeBelongings.hurry(peer), "a peer can stage their own original after the first reader's completed return");
            HomeBelongings.tickPlayer(peer);
            h.assertTrue(f.returned().size() == 4 && f.returned().stream().anyMatch(e -> ItemStack.matches(e.getItem(), peersExpected))
                    && ItemStack.matches(laid.getItem(), expected), "leaving three original floor items never blocks or replaces another reader's native return");
            h.assertTrue(WitnessAccount.count(f.data(), p.getUUID()) == 0, "finding a possession does not inflate the Witness pool");
        });
    }

    @GameTest(template = "empty", batch = "home_intrusions", timeoutTicks = 1000)
    public static void aRetrievedOrChangedCacheStackCannotBeRecreatedFromItsRememberedDescription(GameTestHelper h) {
        run(h, 1541000, f -> {
            var p = f.player(h, "home_late_return"); var peer = f.player(h, "home_retriever");
            var original = keepsake(); var expected = original.copy(); int slot = deposit(h, f, p, original);
            peer.getInventory().add(f.barrel().removeItemNoUpdate(slot)); f.barrel().setChanged();
            f.returnHome(p);
            h.assertTrue(f.returned().isEmpty() && HomeBelongings.record(p).getBoolean("Done")
                    && peer.getInventory().items.stream().anyMatch(s -> ItemStack.matches(s, expected)), "a living holder keeps the original and its remembered cache description yields nothing");
            HomeBelongings.reset(p);
            h.assertTrue(p.getInventory().items.stream().noneMatch(s -> ItemStack.matches(s, expected)), "canceling an ordinary trace does not refund an item held by somebody else");
        });
    }

    @GameTest(template = "empty", batch = "home_intrusions", timeoutTicks = 1000)
    public static void anActualSpectatorCameraDefersTheReturnAndLeavingAllowsOneNativePickup(GameTestHelper h) {
        run(h, 1541500, f -> {
            var p = f.player(h, "home_waiting"); var observer = f.player(h, "home_camera");
            var original = keepsake(); var expected = original.copy(); p.setItemInHand(InteractionHand.MAIN_HAND, original);
            h.assertTrue(HomeBelongings.stageHeld(p), "the test fixture takes the held original into finite saved custody");
            observer.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
            f.at(observer, f.origin.offset(6, 8, 9), 0, 30);
            for (int i = 0; i < 12; i++) HomeBelongings.tickPlayer(p);
            h.assertTrue(f.returned().isEmpty() && HomeBelongings.record(p).getBoolean("Pending"), "a spectator's actual camera prevents a watched appearance at every candidate");
            observer.teleportTo(observer.server.overworld(), f.origin.getX() + 6.5, f.origin.getY() + 8, f.origin.getZ() + 9.5, 0, 30);
            observer.hasChangedDimension(); observer.connection.resetPosition(); HomeBelongings.tickPlayer(p);
            h.assertTrue(f.returned().isEmpty() && HomeBelongings.record(p).getBoolean("Pending"), "an Overworld camera looking into the manor also defers the return");
            f.at(observer, f.origin.north(12), 180, 0); HomeBelongings.tickPlayer(p);
            h.assertTrue(f.returned().size() == 1 && ItemStack.matches(f.returned().getFirst().getItem(), expected), "the saved original appears after the observer leaves");
            var item = f.returned().getFirst(); observer.gameMode.changeGameModeForPlayer(GameType.SURVIVAL); item.playerTouch(observer);
            h.assertTrue(!item.isRemoved() && observer.getInventory().items.stream().noneMatch(s -> ItemStack.matches(s, expected)), "an observer cannot pick up the owner's returned item");
            f.at(p, item.blockPosition(), 0, 0); item.playerTouch(p);
            h.assertTrue(item.isRemoved() && p.getInventory().items.stream().anyMatch(s -> ItemStack.matches(s, expected)), "the actual owner recovers the native original normally");
        });
    }

    @GameTest(template = "empty", batch = "home_intrusions", timeoutTicks = 1000)
    public static void homeTestingCommandsStageStatusAndResetWithoutTouchingBlocksOrDuplicatingCustody(GameTestHelper h) {
        run(h, 1542000, f -> {
            var p = f.player(h, "home_commands"); var original = keepsake(); var expected = original.copy();
            p.setItemInHand(InteractionHand.MAIN_HAND, original);
            var dispatcher = new CommandDispatcher<CommandSourceStack>(); HouseCommands.register(dispatcher);
            var source = p.createCommandSourceStack().withPermission(2);
            try {
                h.assertTrue(dispatcher.execute("ghosthouse test home knocking", source) == 1
                        && dispatcher.execute("ghosthouse test home belongings", source) == 1, "both real operator command paths start their personal home fixtures");
                f.reload();
                for (var at : HomeBelongings.places(f.origin)) f.level.setBlock(at, Blocks.STONE.defaultBlockState(), 3);
                h.assertTrue(dispatcher.execute("ghosthouse test home belongings return", source) == 1, "the operator can request immediate delivery of pending custody");
                HomeBelongings.tickPlayer(p);
                h.assertTrue(f.returned().isEmpty() && HomeBelongings.record(p).getBoolean("Pending"), "occupied blocks are preserved and the original stays in custody");
                for (int slot = 0; slot < 36; slot++) if (p.getInventory().getItem(slot).isEmpty())
                    p.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
                h.assertTrue(dispatcher.execute("ghosthouse test home belongings reset", source) == 0
                        && HomeBelongings.record(p).getBoolean("Pending"), "a full inventory retains pending original custody instead of dropping or losing the refund");
                p.getInventory().setItem(5, ItemStack.EMPTY);
                h.assertTrue(dispatcher.execute("ghosthouse test home status", source) == 1
                        && dispatcher.execute("ghosthouse test home belongings reset", source) == 1
                        && dispatcher.execute("ghosthouse test home belongings reset", source) == 1
                        && dispatcher.execute("ghosthouse test home knocking reset", source) == 1, "status and repeat-safe cancellation work through the actual command dispatcher");
            } catch (Exception error) { throw new AssertionError(error); }
            h.assertTrue(p.getInventory().items.stream().filter(s -> ItemStack.matches(s, expected)).count() == 1
                    && f.returned().isEmpty() && !HomeKnocking.record(p).getBoolean("Active"), "pending test custody is refunded exactly once and the knocking stops");
            h.assertTrue(HomeBelongings.places(f.origin).stream().allMatch(at -> f.level.getBlockState(at).is(Blocks.STONE)), "test commands never clear player-placed blocks to force an appearance");
        });
    }

    @GameTest(template = "empty", batch = "home_intrusions", timeoutTicks = 1000)
    public static void cacheSuppliesAndSimultaneousReadersNeverInventAnotherPlayersDeposit(GameTestHelper h) {
        run(h, 1542500, f -> {
            var p = f.player(h, "home_cache_reader"); var peer = f.player(h, "home_cache_peer");
            f.open(p); p.closeContainer();
            h.assertTrue(HomeBelongings.record(p).isEmpty(), "opening and closing the finite supplies alone records no invented belongings");
            var original = keepsake(); var expected = original.copy(); p.setItemInHand(InteractionHand.MAIN_HAND, original);
            var menu = f.open(p); f.open(peer); menu.quickMoveStack(p, 54);
            p.closeContainer(); peer.closeContainer();
            h.assertTrue(HomeBelongings.record(p).isEmpty() && HomeBelongings.record(peer).isEmpty()
                    && f.barrel().getItem(0).getCount() == 6, "a contended native cache session assigns neither ownership nor a return to either reader");
            h.assertTrue(java.util.stream.IntStream.range(0, f.barrel().getContainerSize()).anyMatch(i -> ItemStack.matches(f.barrel().getItem(i), expected)), "the actual deposited original stays in its native cache");
        });
    }
}
