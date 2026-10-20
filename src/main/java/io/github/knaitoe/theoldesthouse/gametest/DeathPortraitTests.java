package io.github.knaitoe.theoldesthouse.gametest;

import com.mojang.brigadier.CommandDispatcher;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.command.HouseCommands;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthData;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(TheOldestHouse.MOD_ID + "_multiplayer")
@PrefixGameTestTemplate(false)
public final class DeathPortraitTests {
    private static final List<Fixture> ACTIVE = new ArrayList<>();
    private static final class Fixture implements AutoCloseable {
        final ServerLevel level, exterior;
        final BlockPos origin;
        final NativeTestChunks chunks = new NativeTestChunks();
        final List<ServerPlayer> players = new ArrayList<>();
        final List<Painting> paintings = new ArrayList<>();
        HouseSavedData oldHouse; LabyrinthData oldData; boolean started, closed;
        Fixture(GameTestHelper h, int coordinate) {
            level = HouseTestLevel.get(h.getLevel().getServer()); exterior = level.getServer().overworld();
            origin = new BlockPos(coordinate, 40, coordinate);
            chunks.hold(level, new AABB(origin).inflate(34)); chunks.hold(exterior, new AABB(origin).inflate(34));
        }
        void start(GameTestHelper h) {
            oldHouse = HouseSavedData.get(level.getServer()); oldData = data(); started = true;
            var house = new HouseSavedData(); house.markSpawned(origin); house.markInteriorInitialized();
            exterior.getDataStorage().set("the_oldest_house", house);
            exterior.getDataStorage().set("the_oldest_house_labyrinth", new LabyrinthData());
            for (int x : new int[]{6, 10, 14}) {
                for (int y = 1; y <= 6; y++) level.setBlock(origin.offset(x, y, 17), Blocks.OAK_PLANKS.defaultBlockState(), 3);
                var variant = level.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(HousePaintings.WILL_PORTRAIT);
                var painting = new Painting(level, origin.offset(x, 3, 18), Direction.SOUTH, variant);
                HouseDeathPortraits.markOriginal(painting);
                h.assertTrue(painting.survives() && level.addFreshEntity(painting), "the original native family painting has real backing");
                paintings.add(painting);
            }
        }
        LabyrinthData data() { return LabyrinthData.get(level.getServer()); }
        ServerPlayer player(GameTestHelper h, String name) {
            var p = NativeTestPlayers.survival(h, name); p.setNoGravity(true); players.add(p); home(p); return p;
        }
        void home(ServerPlayer p) { at(p, level, origin.offset(10, 1, 8), 180); }
        void at(ServerPlayer p, ServerLevel where, BlockPos at, float yaw) {
            p.teleportTo(where, at.getX() + .5, at.getY(), at.getZ() + .5, yaw, 0);
            p.hasChangedDimension(); p.setDeltaMovement(Vec3.ZERO); p.connection.resetPosition();
        }
        ServerPlayer respawn(ServerPlayer p) {
            var fresh = p.server.getPlayerList().respawn(p, false, Entity.RemovalReason.KILLED);
            fresh.connection.player = fresh; players.remove(p); players.add(fresh); fresh.hasChangedDimension();
            fresh.setNoGravity(true); home(fresh); return fresh;
        }
        void kill(GameTestHelper h, ServerPlayer p) {
            int before = p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
            p.hurt(p.damageSources().genericKill(), 10000);
            h.assertTrue(p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) == before + 1,
                    "the complete native death succeeded and awarded exactly one vanilla death");
            HouseDeathPortraits.confirm(p.server);
        }
        void reload() {
            var loaded = LabyrinthData.FACTORY.deserializer().apply(data().save(new CompoundTag(), level.registryAccess()), level.registryAccess());
            exterior.getDataStorage().set("the_oldest_house_labyrinth", loaded);
        }
        long claimed() { return paintings.stream().filter(HouseDeathPortraits::claimed).count(); }
        @Override public void close() {
            if (closed) return; closed = true;
            if (started) {
                for (var player : players) NativeTestPlayers.remove(player);
                paintings.forEach(Entity::discard);
                for (int x : new int[]{6, 10, 14}) for (int y = 1; y <= 6; y++) level.removeBlock(origin.offset(x, y, 17), false);
                exterior.getDataStorage().set("the_oldest_house", oldHouse);
                exterior.getDataStorage().set("the_oldest_house_labyrinth", oldData);
            }
            chunks.close(); ACTIVE.remove(this);
        }
    }
    @AfterBatch(batch = "death_portraits") public static void cleanup(ServerLevel level) {
        for (var fixture : new ArrayList<>(ACTIVE)) fixture.close();
    }
    private static void run(GameTestHelper h, int coordinate, Consumer<Fixture> check) {
        var f = new Fixture(h, coordinate); ACTIVE.add(f);
        h.startSequence().thenWaitUntil(() -> h.assertTrue(f.chunks.ready(), "both actual manor camera levels have native loaded entity sections"))
                .thenExecute(() -> { f.start(h); try { check.accept(f); h.succeed(); } finally { f.close(); } });
    }

    @GameTest(template = "empty", batch = "death_portraits", timeoutTicks = 1000)
    public static void firstActualHouseDeathKeepsOnePortraitThroughRespawnReloadAndLaterDeaths(GameTestHelper h) {
        run(h, 608000, f -> {
            var p = f.player(h, "portrait_one"); f.kill(h, p); HouseDeathPortraits.place(p.server);
            var first = HouseDeathPortraits.record(p);
            h.assertTrue(first.getBoolean("FirstDeath") && first.hasUUID("Painting") && f.claimed() == 1, "one genuine House death paints exactly one existing original");
            var original = (Painting) f.level.getEntity(first.getUUID("Painting"));
            String profile = ((PortraitPainting) original).housePortrait();
            var data = new CompoundTag(); original.saveWithoutId(data);
            var loaded = EntityType.PAINTING.create(f.level); loaded.load(data);
            h.assertTrue(loaded.getUUID().equals(original.getUUID()) && loaded.getPos().equals(original.getPos())
                    && loaded.getVariant().equals(original.getVariant()) && ((PortraitPainting) loaded).housePortrait().equals(profile),
                    "native entity NBT retains original UUID, wall, variant and immutable skin snapshot");
            var tracked = EntityType.PAINTING.create(f.level); tracked.getEntityData().assignValues(original.getEntityData().getNonDefaultValues());
            h.assertTrue(((PortraitPainting) tracked).housePortrait().equals(profile), "late native entity tracking carries the actual profile without a custom client request");
            p = f.respawn(p); f.reload(); f.kill(h, p); HouseDeathPortraits.place(p.server);
            h.assertTrue(HouseDeathPortraits.record(p).equals(first) && f.claimed() == 1, "respawn, saved-data reload and a second real death cannot create a second portrait");
            var id = p.getUUID(); NativeTestPlayers.remove(p); f.players.remove(p);
            var reconnect = NativeTestPlayers.survival(h, "renamed_reader", id); f.players.add(reconnect); f.home(reconnect);
            HouseDeathPortraits.place(reconnect.server);
            h.assertTrue(HouseDeathPortraits.record(reconnect).equals(first) && f.claimed() == 1
                    && !HouseDeathPortraits.preview(reconnect) && !HouseDeathPortraits.resetPreview(reconnect),
                    "same UUID under a new name reconnects to the one permanent portrait; test commands cannot reset a real death");
        });
    }

    @GameTest(template = "empty", batch = "death_portraits", timeoutTicks = 1000)
    public static void canceledRescuedOutsideAndSyntheticDeathsCannotSpendTheFirstHouseDeath(GameTestHelper h) {
        run(h, 608200, f -> {
            var p = f.player(h, "portrait_safe");
            var canceled = new LivingDeathEvent(p, p.damageSources().genericKill()); canceled.setCanceled(true); NeoForge.EVENT_BUS.post(canceled);
            NeoForge.EVENT_BUS.post(new LivingDeathEvent(p, p.damageSources().genericKill())); HouseDeathPortraits.confirm(p.server);
            h.assertTrue(!HouseDeathPortraits.record(p).getBoolean("FirstDeath"), "canceled and synthetic event-only deaths never paint a reader");
            Consumer<LivingDeathEvent> rescue = e -> { if (e.getEntity() == p) { e.setCanceled(true); p.setHealth(p.getMaxHealth()); } };
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, rescue);
            try { p.hurt(p.damageSources().genericKill(), 10000); HouseDeathPortraits.confirm(p.server); }
            finally { NeoForge.EVENT_BUS.unregister(rescue); }
            h.assertTrue(p.isAlive() && !HouseDeathPortraits.record(p).getBoolean("FirstDeath"), "even a later LOWEST-priority rescue cannot be mistaken for an actual death");
            f.at(p, f.exterior, f.origin.offset(10, 1, 8), 180); f.kill(h, p);
            h.assertTrue(!HouseDeathPortraits.record(p).getBoolean("FirstDeath") && f.claimed() == 0, "a real ordinary-world death does not consume the first House death");
            var fresh = f.respawn(p); f.kill(h, fresh); HouseDeathPortraits.place(fresh.server);
            h.assertTrue(HouseDeathPortraits.record(fresh).getBoolean("FirstDeath") && f.claimed() == 1, "the later first actual House death still works");
        });
    }

    @GameTest(template = "empty", batch = "death_portraits", timeoutTicks = 1000)
    public static void separatePlayersClaimDifferentOriginalsAndRemovedPortraitsNeverRegenerate(GameTestHelper h) {
        run(h, 608400, f -> {
            var a = f.player(h, "portrait_a"); var b = f.player(h, "portrait_b");
            f.kill(h, a); f.kill(h, b); HouseDeathPortraits.place(a.server);
            var mine = HouseDeathPortraits.record(a); var theirs = HouseDeathPortraits.record(b);
            h.assertTrue(f.claimed() == 2 && !mine.getUUID("Painting").equals(theirs.getUUID("Painting")), "co-op first deaths occupy distinct existing portraits, once per UUID");
            var portrait = (Painting) f.level.getEntity(mine.getUUID("Painting")); var id = portrait.getUUID(); portrait.discard();
            f.reload(); HouseDeathPortraits.place(a.server); HouseDeathPortraits.place(a.server);
            h.assertTrue(f.level.getEntity(id) == null && HouseDeathPortraits.record(a).equals(mine)
                    && f.paintings.stream().filter(Entity::isAlive).filter(HouseDeathPortraits::claimed).count() == 1,
                    "removing a painted original never reconstructs it or takes another family frame");
        });
    }

    @GameTest(template = "empty", batch = "death_portraits", timeoutTicks = 1000)
    public static void actualSpectatorAndExteriorCamerasDelayOnlyTheUnseenPaintingChange(GameTestHelper h) {
        run(h, 608600, f -> {
            var owner = f.player(h, "portrait_owner"); var watcher = f.player(h, "portrait_eye");
            watcher.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
            f.at(watcher, f.level, f.origin.offset(10, 2, 22), 180);
            h.assertTrue(HouseDeathPortraits.preview(owner), "a reversible preview is queued on the real family paintings");
            HouseDeathPortraits.place(owner.server); h.assertTrue(f.claimed() == 0, "an actual spectator camera protects all three visible original portraits");
            f.at(watcher, f.exterior, f.origin.offset(10, 2, 22), 180);
            HouseDeathPortraits.place(owner.server); h.assertTrue(f.claimed() == 0, "a camera looking through the ordinary-world house also protects their change");
            f.at(watcher, f.exterior, f.origin.offset(10, 2, 22), 0);
            HouseDeathPortraits.place(owner.server); h.assertTrue(f.claimed() == 1, "once both actual cameras look away exactly one original is painted");
            h.assertTrue(HouseDeathPortraits.resetPreview(owner) && f.claimed() == 0, "reset reverses the preview on that same loaded original, without replacing an entity");
        });
    }

    @GameTest(template = "empty", batch = "death_portraits", timeoutTicks = 1000)
    public static void realOperatorPreviewIsReversibleIdempotentAndCannotCreateMissingPortraits(GameTestHelper h) {
        run(h, 608800, f -> {
            var p = f.player(h, "portrait_test"); var dispatcher = new CommandDispatcher<CommandSourceStack>();
            HouseCommands.register(dispatcher);
            try {
                var source = p.createCommandSourceStack().withPermission(2);
                h.assertTrue(dispatcher.execute("ghosthouse test home portrait preview", source) == 1, "the actual permission-checked command stages a native portrait");
                HouseDeathPortraits.place(p.server); var first = HouseDeathPortraits.record(p).copy();
                for (int n = 0; n < 4; n++) dispatcher.execute("ghosthouse test home portrait preview", source);
                h.assertTrue(f.claimed() == 1 && HouseDeathPortraits.record(p).equals(first) && !first.getBoolean("FirstDeath"), "repeated previews never add portraits or spend the genuine death");
                h.assertTrue(dispatcher.execute("ghosthouse test home portrait reset", source) == 1 && f.claimed() == 0, "the actual reset restores the original painting");
                f.paintings.forEach(Entity::discard); dispatcher.execute("ghosthouse test home portrait preview", source); HouseDeathPortraits.place(p.server);
                h.assertTrue(!HouseDeathPortraits.record(p).hasUUID("Painting"), "a missing original is never fabricated by a test command");
                dispatcher.execute("ghosthouse test home portrait reset", source);
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) { throw new AssertionError(exception); }
        });
    }
}
