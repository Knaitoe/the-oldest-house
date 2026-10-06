package io.github.knaitoe.theoldesthouse.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import java.util.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Explicit operator fixtures; no ordinary progression calls these shortcuts. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class PlaytestCommands {
    private enum Start { CELL, DEFEAT, ESCAPE, WITNESS }
    private static final Map<UUID, Pending> PENDING = new LinkedHashMap<>();
    private static final TicketType<Long> TICKET = TicketType.create("the_oldest_house_test", Long::compare);
    private static long nextTicket;
    private PlaytestCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        var vignettes = Commands.literal("vignette").executes(c -> help(c.getSource()));
        for (var place : LabyrinthPlace.values()) if (place.slot() >= 0)
            vignettes.then(Commands.literal(place.id()).executes(c -> LabyrinthCommands.placeDoor(c.getSource(), place.id())));
        vignettes.then(Commands.literal("remove").executes(c -> LabyrinthCommands.removeDoor(c.getSource())));
        return Commands.literal("test").requires(s -> s.hasPermission(2)).executes(c -> help(c.getSource()))
                .then(vignettes)
                .then(Commands.literal("minotaur").executes(c -> queue(c.getSource(), Start.CELL)))
                .then(Commands.literal("boy").executes(c -> queue(c.getSource(), Start.CELL)))
                .then(Commands.literal("ending").executes(c -> help(c.getSource()))
                        .then(Commands.literal("defeat").executes(c -> queue(c.getSource(), Start.DEFEAT)))
                        .then(Commands.literal("escape").executes(c -> queue(c.getSource(), Start.ESCAPE)))
                        .then(Commands.literal("collapse").executes(c -> queue(c.getSource(), Start.ESCAPE)))
                        .then(Commands.literal("witness").executes(c -> queue(c.getSource(), Start.WITNESS))))
                .then(Commands.literal("cancel").executes(c -> {
                    var pending = PENDING.remove(c.getSource().getPlayerOrException().getUUID());
                    if (pending == null) return fail(c.getSource(), "No testing shortcut is waiting. A committed ending cannot be canceled.");
                    pending.close();
                    c.getSource().sendSuccess(() -> Component.literal("Canceled the waiting shortcut. Your position and ending are unchanged."), false);
                    return 1;
                }));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Testing: /oldesthouse test vignette <id> puts a door two blocks ahead; use Tab for every scene. 'vignette remove' removes the nearest test door."), false);
        source.sendSuccess(() -> Component.literal("/oldesthouse test minotaur (or boy) takes you to the closed cell. /oldesthouse test ending defeat|escape|witness starts a real ending; collapse is an alias for escape."), false);
        source.sendSuccess(() -> Component.literal("Use a COPY of your world. Defeat kills you and seals your belongings; escape starts the wounded creature and physical collapse, whose eventual completion removes the shared House. Witness stages your personal test account and peaceful release. No shortcut resets a player or takes another player's cell."), false);
        return 1;
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
        return 0;
    }

    private static String blocked(ServerPlayer player, Start start) {
        if (!player.isAlive() || player.isRemoved()) return "Respawn before using a testing shortcut.";
        if (player.isSpectator()) return "Leave spectator mode before using this shortcut.";
        if (HouseSavedData.get(player.server).houseOrigin() == null) return "Spawn the House first: /oldesthouse spawn.";
        if (HouseTransitionEvents.isPending(player) || StaircaseLeaks.active(player)) return "Finish your current crossing or personal note scene first.";
        var phase = FinaleProgress.phase(player.server, player.getUUID());
        if (FinaleProgress.terminal(phase)) return "This player has already finished an ending. Use a fresh test player or a copy of the world.";
        if (FinaleProgress.committed(phase)) return "This player already has an ending in progress. /oldesthouse finale status shows it.";
        var world = FinaleProgress.world(player.server);
        if (world.getBoolean("Ended")) return "This world's House has already collapsed. Use a fresh world copy.";
        if (world.hasUUID("Owner") && !world.getUUID("Owner").equals(player.getUUID()))
            return "Another player owns the finale, including while offline. Their attempt must finish first.";
        if (start == Start.WITNESS && world.getBoolean("MinotaurWounded"))
            return "The creature is already wounded in this world. Test peaceful release in an earlier world copy.";
        return null;
    }

    private static int queue(CommandSourceStack source, Start start) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        String error = blocked(player, start);
        if (error != null) return fail(source, error);
        if (PENDING.containsKey(player.getUUID())) return fail(source, "A shortcut is already waiting. Use /oldesthouse test cancel first.");
        FinaleArchitecture.request(player.server);
        PENDING.put(player.getUUID(), new Pending(source, player, start));
        source.sendSuccess(() -> Component.literal("Preparing the cell asynchronously. You will move when its blocks and entities are loaded. /oldesthouse test cancel stops the wait."
                + (start == Start.CELL ? "" : " This will start the real " + start.name().toLowerCase(Locale.ROOT) + " ending in this world.")), false);
        return 1;
    }

    private static final class Pending implements AutoCloseable {
        final CommandSourceStack source;
        final ServerPlayer player;
        final ServerLevel from;
        final BlockPos origin;
        final Start start;
        final long ticket = ++nextTicket;
        final Set<ChunkPos> chunks = new HashSet<>();
        ServerLevel level;
        int ticks;
        Pending(CommandSourceStack source, ServerPlayer player, Start start) {
            this.source = source; this.player = player; this.start = start;
            from = player.serverLevel(); origin = HouseSavedData.get(player.server).houseOrigin();
        }
        boolean loaded() {
            if (level == null) {
                level = player.server.getLevel(HouseDimensions.INTERIOR);
                if (level == null) return false;
                chunks.addAll(WitnessEnding.releaseChunks(origin));
                // Include the cell's rear, chamber seal, collapse chute and physical escape route.
                var base = FinaleArchitecture.base(origin);
                for (int x = (base.getX() - 27) >> 4; x <= (base.getX() + 27) >> 4; x++)
                    for (int z = (base.getZ() + 27) >> 4; z <= (base.getZ() + 115) >> 4; z++)
                        chunks.add(new ChunkPos(x, z));
                for (var chunk : chunks) level.getChunkSource().addRegionTicket(TICKET, chunk, 3, ticket);
            }
            return chunks.stream().allMatch(c -> level.hasChunk(c.x, c.z) && level.areEntitiesLoaded(c.toLong()));
        }
        @Override public void close() {
            if (level != null) for (var chunk : chunks) level.getChunkSource().removeRegionTicket(TICKET, chunk, 3, ticket);
            chunks.clear();
        }
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) { tick(event.getServer()); }

    /** Uses the same readiness and lifecycle checks in native command tests. */
    public static void tick(MinecraftServer server) {
        for (var pending : new ArrayList<>(PENDING.values())) {
            var player = pending.player;
            if (player.server != server) continue;
            if (server.getPlayerList().getPlayer(player.getUUID()) != player) { remove(pending); continue; }
            String error = blocked(player, pending.start);
            if (error == null && (player.serverLevel() != pending.from || !pending.origin.equals(HouseSavedData.get(server).houseOrigin())))
                error = "The player or House moved before the shortcut was ready. Run the command again.";
            if (error == null && ++pending.ticks > 1200) error = "The cell did not become ready within sixty seconds. Preparation continues; retry when /oldesthouse finale go is available.";
            if (error != null) { fail(pending.source, error); remove(pending); continue; }
            if (!pending.loaded() || !FinaleArchitecture.ready(server)) continue;
            var world = FinaleProgress.world(server);
            UUID actorId = world.hasUUID("WoundedCreature") ? world.getUUID("WoundedCreature")
                    : world.hasUUID("CagedCreature") ? world.getUUID("CagedCreature") : null;
            MinotaurEntity actor = actorId == null ? FinaleController.ensureCaged(pending.level, pending.origin)
                    : pending.level.getEntity(actorId) instanceof MinotaurEntity m ? m : null;
            // A saved native identity must load; never replace it to make a test command succeed.
            if (actor == null || !actor.isAlive()) continue;
            if (pending.start == Start.WITNESS && (error = witnessSpace(player, pending.origin)) != null) {
                fail(pending.source, error); remove(pending); continue;
            }
            try {
                var at = FinaleArchitecture.cell(pending.origin).north(pending.start == Start.CELL ? 6 : 3);
                if (player.containerMenu != player.inventoryMenu) {
                    var cursor = player.containerMenu.getCarried(); player.containerMenu.setCarried(ItemStack.EMPTY);
                    player.closeContainer(); player.inventoryMenu.setCarried(cursor);
                }
                if (pending.start == Start.WITNESS && (error = witnessSpace(player, pending.origin)) != null) {
                    fail(pending.source, error); continue;
                }
                player.stopRiding();
                player.teleportTo(pending.level, at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
                player.connection.resetPosition(); player.setDeltaMovement(Vec3.ZERO); player.resetFallDistance();
                if (pending.start == Start.CELL) {
                    var record = FinaleProgress.player(server, player.getUUID());
                    record.putString("Phase", FinaleProgress.Phase.STAIRCASE.name()); record.putBoolean("OperatorCellVisit", true);
                    // A deliberate debug arrival is not an old-save deep-stair migration.
                    record.putBoolean("StairFireVersion", true);
                    FinaleProgress.save(server, player.getUUID(), record);
                    pending.source.sendSuccess(() -> Component.literal("At the boy/Minotaur's cell. The cell is not committed; your inventory and game mode are retained."), false);
                } else if (pending.start == Start.WITNESS) {
                    beginWitness(player, pending.origin, actor);
                    pending.source.sendSuccess(() -> Component.literal("Peaceful release started with a personal operator test account. Stand aside, then follow the real stairs home."), false);
                } else if (FinaleController.start(player)) {
                    if (pending.start == Start.DEFEAT) {
                        // This explicit forced-death fixture must work even before the client
                        // acknowledges the dimension change. Native portal invulnerability
                        // would otherwise leave the player alive in a committed fight.
                        player.hasChangedDimension();
                        if (!player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE))
                            fail(pending.source, "Native death was refused. The finale is committed; check /oldesthouse finale status.");
                    }
                    else {
                        var record = FinaleProgress.player(server, player.getUUID());
                        if (FinaleProgress.phase(record) != FinaleProgress.Phase.COLLAPSE) FinaleController.wound(player, actor);
                        pending.source.sendSuccess(() -> Component.literal("Collapse started. The same creature remains wounded and alive. Use the west breach, physical fall and escape route; existing Mother custody decides whether a guide returns."), false);
                    }
                } else fail(pending.source, "The real finale refused to start. /oldesthouse finale status shows its state.");
            } catch (RuntimeException e) {
                TheOldestHouse.LOGGER.error("Could not complete {} operator fixture for {}", pending.start, player.getUUID(), e);
                fail(pending.source, "The shortcut could not complete. Check /oldesthouse finale status before retrying.");
            } finally { remove(pending); }
        }
    }

    private static void remove(Pending pending) { PENDING.remove(pending.player.getUUID(), pending); pending.close(); }
    public static void clear(MinecraftServer server) {
        for (var pending : new ArrayList<>(PENDING.values())) if (pending.player.server == server) remove(pending);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { clear(event.getServer()); }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        var pending = PENDING.get(event.getEntity().getUUID());
        if (pending != null && pending.player == event.getEntity()) remove(pending);
    }

    private static int originalSlot(ServerPlayer player, UUID weapon) {
        if (weapon != null) for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            if (WeaponHistory.wounds(player.getInventory().getItem(i), weapon)) return i;
        return -1;
    }
    private static ItemEntity laidWeapon(ServerPlayer player, BlockPos origin, UUID weapon) {
        if (weapon == null) return null;
        var level = player.server.getLevel(HouseDimensions.INTERIOR);
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(FinaleArchitecture.cell(origin)).inflate(6),
                item -> item.isAlive() && WeaponHistory.wounds(item.getItem(), weapon)).stream().findFirst().orElse(null);
    }
    private static String witnessSpace(ServerPlayer player, BlockPos origin) {
        UUID weapon = WeaponHistory.favorite(player); int original = originalSlot(player, weapon);
        if (weapon != null && original < 0 && laidWeapon(player, origin, weapon) == null)
            return "Carry your recorded original weapon (retrieve it from the Mother if necessary). This command never creates a replacement original.";
        int needed = 0, empty = 0, selected = player.getInventory().selected;
        for (int hand : new int[]{selected, 40}) if (hand != original && !player.getInventory().getItem(hand).isEmpty()) needed++;
        for (int i = 0; i < 36; i++) if (i != selected && (i == original || player.getInventory().getItem(i).isEmpty())) empty++;
        return empty < needed ? "Make " + needed + " empty inventory slots so both hands can be stored safely. No equipment was moved." : null;
    }
    private static void beginWitness(ServerPlayer player, BlockPos origin, MinotaurEntity actor) {
        var inventory = player.getInventory(); UUID weapon = WeaponHistory.favorite(player);
        int original = originalSlot(player, weapon); ItemStack laid = original < 0 ? ItemStack.EMPTY : inventory.removeItemNoUpdate(original);
        var moved = new LinkedHashMap<Integer, Integer>(); ItemEntity item = null;
        var data = LabyrinthData.get(player.server); var oldAccount = data.state(WitnessAccount.STATE);
        boolean crouched = player.isShiftKeyDown(), started = false;
        try {
            for (int hand : new int[]{inventory.selected, 40}) if (!inventory.getItem(hand).isEmpty()) {
                for (int slot = 0; slot < 36; slot++) if (slot != inventory.selected && inventory.getItem(slot).isEmpty()) {
                    inventory.setItem(slot, inventory.removeItemNoUpdate(hand)); moved.put(hand, slot); break;
                }
            }
            if (!laid.isEmpty()) {
                var at = FinaleArchitecture.cell(origin).north(2);
                item = new ItemEntity(player.serverLevel(), at.getX() + 1.5, at.getY(), at.getZ() + .5, laid);
                item.setTarget(player.getUUID());
                if (!player.serverLevel().addFreshEntity(item)) throw new IllegalStateException("Could not place the original weapon.");
            }
            for (var story : WitnessAccount.Story.values()) {
                if (WitnessAccount.ready(data, player.getUUID())) break;
                WitnessAccount.resolve(data, player.getUUID(), story, "operator_fixture");
            }
            WitnessAccount.markRead(data, player.getUUID()); player.setShiftKeyDown(true);
            started = WitnessEnding.begin(player);
            if (!started) throw new IllegalStateException("The real peaceful release refused its prepared fixture.");
            // The visible native prisoner is the departing actor, not a second spawned entity.
            actor.owner(player.getUUID()); actor.released();
            var record = FinaleProgress.player(player.server, player.getUUID()); record.putUUID("Creature", actor.getUUID());
            FinaleProgress.save(player.server, player.getUUID(), record);
        } finally {
            player.setShiftKeyDown(crouched);
            if (!started) {
                data.setState(WitnessAccount.STATE, oldAccount);
                for (var move : moved.entrySet()) inventory.setItem(move.getKey(), inventory.removeItemNoUpdate(move.getValue()));
                if (item != null) item.discard();
                if (!laid.isEmpty()) inventory.setItem(original, laid);
            }
            player.inventoryMenu.broadcastChanges();
        }
    }
}
