package io.github.knaitoe.theoldesthouse.house;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.*;
import io.github.knaitoe.theoldesthouse.opening.DeliveredItemEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Actual deposited possessions turn up in the manor. Saved descriptions never become spare items. */
@EventBusSubscriber(modid = TheOldestHouse.MOD_ID)
public final class HomeBelongings {
    public static final String STATE = "home_belongings_04110";
    private static final Map<UUID, Deposit> OPEN = new HashMap<>();
    private static final Map<UUID, Lease> LEASES = new HashMap<>();
    private static final TicketType<UUID> TICKET = TicketType.create("the_oldest_house_home_cache", UUID::compareTo);

    private static final class Deposit {
        final BarrelBlockEntity cache;
        final List<ItemStack> slots = new ArrayList<>(), inventory = new ArrayList<>();
        boolean shared;
        Deposit(ServerPlayer player, BarrelBlockEntity cache) {
            this.cache = cache;
            for (int i = 0; i < cache.getContainerSize(); i++) slots.add(cache.getItem(i).copy());
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) inventory.add(player.getInventory().getItem(i).copy());
        }
    }

    private record Lease(ServerLevel level, ChunkPos chunk, UUID owner) {
        void close() { level.getChunkSource().removeRegionTicket(TICKET, chunk, 3, owner); }
    }

    private HomeBelongings() {}

    public static CompoundTag record(ServerPlayer player) {
        return LabyrinthData.get(player.server).stateEntry(STATE, player.getUUID().toString());
    }

    private static void save(ServerPlayer player, CompoundTag own) {
        LabyrinthData.get(player.server).setStateEntry(STATE, player.getUUID().toString(), own);
    }

    public static BlockPos cache(BlockPos origin) {
        return LabyrinthPlaces.base(origin, LabyrinthPlace.EXPLORER_CAMP).offset(LabyrinthCampsite.CACHE);
    }

    public static List<BlockPos> places(BlockPos origin) {
        return List.of(origin.offset(4, 7, 12), origin.offset(7, 7, 12), origin.offset(9, 7, 12));
    }

    @SubscribeEvent public static void opened(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getContainer() instanceof ChestMenu menu)
            opened(player, menu);
    }

    public static void opened(ServerPlayer player, ChestMenu menu) {
        OPEN.remove(player.getUUID());
        for (var other : OPEN.values()) if (menu.getContainer() == other.cache) other.shared = true;
        var house = HouseSavedData.get(player.server);
        BlockPos origin = house.houseOrigin();
        if (origin == null || house.isOutdated() || house.isCollapsed() || !player.isAlive()
                || !player.gameMode.isSurvival() || !player.level().dimension().equals(HouseDimensions.INTERIOR)
                || !(menu.getContainer() instanceof BarrelBlockEntity barrel) || !barrel.getBlockPos().equals(cache(origin))
                || player.distanceToSqr(barrel.getBlockPos().getCenter()) > 64 || !record(player).isEmpty()) return;
        Deposit deposit = new Deposit(player, barrel);
        for (var other : OPEN.values()) if (other.cache == barrel) { other.shared = true; deposit.shared = true; }
        // Also exclude readers already using this barrel before this subscriber saw its opening.
        if (player.serverLevel().players().stream().anyMatch(p -> p != player && using(p, barrel))) deposit.shared = true;
        OPEN.put(player.getUUID(), deposit);
    }

    @SubscribeEvent public static void closed(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getContainer() instanceof ChestMenu menu)
            closed(player, menu);
    }

    private static boolean using(ServerPlayer player, Container container) {
        return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == container;
    }

    /** Only new stacks actually removed from this player's pack can become a trace. */
    public static int closed(ServerPlayer player, ChestMenu menu) {
        Deposit deposit = OPEN.remove(player.getUUID());
        if (deposit == null || deposit.shared || menu.getContainer() != deposit.cache || deposit.cache.isRemoved()
                || deposit.cache.getLevel() != player.level() || !player.isAlive() || !player.gameMode.isSurvival()
                || player.distanceToSqr(deposit.cache.getBlockPos().getCenter()) > 64 || !record(player).isEmpty()) return 0;
        if (player.serverLevel().players().stream().anyMatch(p -> p != player && using(p, deposit.cache))) return 0;
        ListTag entries = new ListTag();
        List<ItemStack> available = new ArrayList<>();
        for (ItemStack stack : deposit.inventory) available.add(stack.copy());
        // Account for every item the reader still carries. Cache loot is never a deposit.
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack held = player.getInventory().getItem(i);
            int count = held.getCount();
            for (ItemStack before : available) if (ItemStack.isSameItemSameComponents(before, held)) {
                int subtract = Math.min(count, before.getCount());
                before.shrink(subtract); count -= subtract;
                if (count == 0) break;
            }
        }
        for (int slot = 0; slot < deposit.slots.size() && entries.size() < 3; slot++) {
            ItemStack stack = deposit.cache.getItem(slot);
            if (!deposit.slots.get(slot).isEmpty() || stack.isEmpty()) continue;
            int owned = available.stream().filter(s -> ItemStack.isSameItemSameComponents(s, stack)).mapToInt(ItemStack::getCount).sum();
            if (owned < stack.getCount()) continue;
            int remaining = stack.getCount();
            for (ItemStack before : available) if (ItemStack.isSameItemSameComponents(before, stack)) {
                int subtract = Math.min(remaining, before.getCount());
                before.shrink(subtract); remaining -= subtract;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", slot);
            entry.put("Item", stack.save(player.registryAccess()));
            entries.add(entry);
        }
        if (entries.isEmpty()) return 0;
        CompoundTag own = initial(player, entries);
        own.putLong("Cache", deposit.cache.getBlockPos().asLong());
        own.putInt("ReturnAfter", HouseExperience.record(LabyrinthData.get(player.server), player.getUUID()).getInt("Returns"));
        save(player, own);
        return entries.size();
    }

    private static CompoundTag initial(ServerPlayer player, ListTag entries) {
        CompoundTag own = new CompoundTag();
        own.putLong("Origin", HouseSavedData.get(player.server).houseOrigin().asLong());
        own.put("Entries", entries);
        own.putBoolean("Pending", true);
        return own;
    }

    /** Operator fixture: move one held original into saved custody, without fabricating equipment. */
    public static boolean stageHeld(ServerPlayer player) {
        if (!HomeKnocking.present(player) || player.containerMenu != player.inventoryMenu
                || player.getMainHandItem().isEmpty() || record(player).getBoolean("Pending")) return false;
        ItemStack original = player.getMainHandItem().split(1);
        CompoundTag entry = new CompoundTag();
        entry.put("Item", original.save(player.registryAccess()));
        ListTag entries = new ListTag(); entries.add(entry);
        CompoundTag own = initial(player, entries);
        own.putBoolean("TestCustody", true);
        own.putBoolean("FastReturn", true);
        save(player, own);
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    public static boolean hurry(ServerPlayer player) {
        CompoundTag own = record(player);
        if (!HomeKnocking.present(player) || !own.getBoolean("Pending")) return false;
        own.putBoolean("FastReturn", true);
        own.putInt("HomeTicks", 120);
        save(player, own);
        return true;
    }

    /** Cancel observation of the cache; refund only undelivered operator-fixture custody. */
    public static boolean reset(ServerPlayer player) {
        CompoundTag own = record(player);
        if (own.getBoolean("TestCustody")) {
            ListTag entries = own.getList("Entries", Tag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                if (!entry.contains("Item") || entry.getBoolean("Done")) continue;
                ItemStack original = ItemStack.parseOptional(player.registryAccess(), entry.getCompound("Item"));
                boolean room = false;
                for (int slot = 0; slot < 36; slot++) {
                    ItemStack held = player.getInventory().getItem(slot);
                    if (held.isEmpty() || ItemStack.isSameItemSameComponents(held, original)
                            && held.getCount() + original.getCount() <= held.getMaxStackSize()) { room = true; break; }
                }
                if (!room) return false; // Saved custody remains intact until the reader makes room.
                player.getInventory().add(original);
                entry.remove("Item"); entry.putBoolean("Done", true);
            }
            own.put("Entries", entries); save(player, own);
        }
        CompoundTag stopped = new CompoundTag(); stopped.putBoolean("Done", true); save(player, stopped);
        player.inventoryMenu.broadcastChanges();
        OPEN.remove(player.getUUID()); release(player.getUUID());
        return true;
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        for (var player : event.getServer().getPlayerList().getPlayers()) if (player.tickCount % 10 == 0) tickPlayer(player);
    }

    public static void tickPlayer(ServerPlayer player) {
        CompoundTag own = record(player);
        if (!own.getBoolean("Pending") || !HomeKnocking.present(player)) { release(player.getUUID()); return; }
        if (player.containerMenu != player.inventoryMenu || player.isSleeping()) return;
        BlockPos origin = HouseSavedData.get(player.server).houseOrigin();
        if (own.getLong("Origin") != origin.asLong()) { reset(player); return; }
        if (!own.getBoolean("FastReturn") && HouseExperience.record(LabyrinthData.get(player.server), player.getUUID()).getInt("Returns") <= own.getInt("ReturnAfter")) return;
        int homeTicks = own.getInt("HomeTicks") + 10;
        own.putInt("HomeTicks", homeTicks);
        if (homeTicks < 120) { save(player, own); return; }
        BarrelBlockEntity source = null;
        if (!own.getBoolean("TestCustody")) {
            BlockPos at = BlockPos.of(own.getLong("Cache"));
            if (!at.equals(cache(origin))) { reset(player); return; }
            ServerLevel level = player.serverLevel();
            hold(player, at);
            ChunkPos chunk = new ChunkPos(at);
            if (level.getChunkSource().getChunkNow(chunk.x, chunk.z) == null || !level.areEntitiesLoaded(chunk.toLong())) { save(player, own); return; }
            if (level.getBlockEntity(at) instanceof BarrelBlockEntity barrel) source = barrel;
            // A removed cache is a loss, never permission to reconstruct its remembered inventory.
            if (source == null) { own.putBoolean("Pending", false); own.putBoolean("Done", true); own.remove("Entries"); save(player, own); release(player.getUUID()); return; }
            if (level.players().stream().anyMatch(p -> using(p, (Container)level.getBlockEntity(at)))) return;
        }
        ListTag entries = own.getList("Entries", Tag.TAG_COMPOUND);
        Set<BlockPos> used = new HashSet<>();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.getBoolean("Done")) continue;
            ItemStack remembered = ItemStack.parseOptional(player.registryAccess(), entry.getCompound("Item"));
            if (remembered.isEmpty() || source != null && !ItemStack.matches(source.getItem(entry.getInt("Slot")), remembered)) {
                entry.putBoolean("Done", true); entry.putBoolean("Gone", true); entry.remove("Item"); continue;
            }
            BlockPos destination = null;
            for (BlockPos candidate : places(origin)) if (!used.contains(candidate) && vacant(player.serverLevel(), candidate)) { destination = candidate; break; }
            if (destination == null) break;
            ItemStack original = source == null ? remembered : source.removeItemNoUpdate(entry.getInt("Slot"));
            var laid = DeliveredItemEntity.create(player.serverLevel(), original,
                    new Vec3(destination.getX() + .5, destination.getY() + .2, destination.getZ() + .5), player.getUUID());
            if (!player.serverLevel().addFreshEntity(laid)) {
                if (source != null) source.setItem(entry.getInt("Slot"), original);
                break;
            }
            if (source != null) source.setChanged();
            used.add(destination);
            entry.putUUID("Entity", laid.getUUID()); entry.putBoolean("Done", true); entry.remove("Item");
        }
        boolean done = true;
        for (int i = 0; i < entries.size(); i++) done &= entries.getCompound(i).getBoolean("Done");
        own.put("Entries", entries);
        own.putBoolean("Pending", !done); own.putBoolean("Done", done);
        save(player, own);
        if (done) release(player.getUUID());
    }

    public static boolean vacant(ServerLevel level, BlockPos at) {
        if (!level.hasChunkAt(at) || !level.areEntitiesLoaded(new ChunkPos(at).toLong())
                || !level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)
                || level.getBlockEntity(at) != null || !level.getBlockState(at.above()).getCollisionShape(level, at.above()).isEmpty()) return false;
        var shape = level.getBlockState(at).getCollisionShape(level, at);
        if (!shape.isEmpty() && shape.max(Direction.Axis.Y) > .125) return false;
        if (HazardVariety.watched(level, at) || HazardVariety.watched(level.getServer().overworld(), at)
                || !level.getEntitiesOfClass(LivingEntity.class, new AABB(at).inflate(.3)).isEmpty()) return false;
        // Loose items can share floor space. A reader leaving theirs here cannot starve a peer's event.
        return level.getEntities(null, new AABB(at).inflate(.15)).stream().allMatch(ItemEntity.class::isInstance);
    }

    private static void hold(ServerPlayer player, BlockPos at) {
        Lease lease = LEASES.get(player.getUUID());
        ChunkPos chunk = new ChunkPos(at);
        if (lease != null && lease.level == player.serverLevel() && lease.chunk.equals(chunk)) return;
        release(player.getUUID());
        player.serverLevel().getChunkSource().addRegionTicket(TICKET, chunk, 3, player.getUUID());
        LEASES.put(player.getUUID(), new Lease(player.serverLevel(), chunk, player.getUUID()));
    }

    private static void release(UUID player) { Lease lease = LEASES.remove(player); if (lease != null) lease.close(); }

    public static void clear(MinecraftServer server) {
        OPEN.entrySet().removeIf(e -> e.getValue().cache.getLevel() != null && e.getValue().cache.getLevel().getServer() == server);
        for (var entry : new ArrayList<>(LEASES.entrySet())) if (entry.getValue().level.getServer() == server) release(entry.getKey());
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { OPEN.remove(event.getEntity().getUUID()); release(event.getEntity().getUUID()); }
    @SubscribeEvent public static void stopped(ServerStoppedEvent event) { clear(event.getServer()); }
}
