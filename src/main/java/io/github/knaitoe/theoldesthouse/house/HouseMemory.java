package io.github.knaitoe.theoldesthouse.house;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * What the manor's own chests and barrels held when they were first opened.
 *
 * The first time a player opens one (while it still has its loot table),
 * the loot is rolled a moment early so the house can remember it. Later, the
 * house may put one thing back: only something common (bread, string, a
 * candle, never a tool, a book or anything enchanted or named), only into
 * a slot that is now empty, and never the same slot twice.
 */
public final class HouseMemory extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_memory";

    private static final class Slot {
        final int slot;
        final String item;
        final int count;
        boolean restored;

        Slot(int slot, String item, int count, boolean restored) {
            this.slot = slot;
            this.item = item;
            this.count = count;
            this.restored = restored;
        }
    }

    private final Map<Long, List<Slot>> containers = new LinkedHashMap<>();

    public static final Factory<HouseMemory> FACTORY = new Factory<>(HouseMemory::new, HouseMemory::load);

    public HouseMemory() {
    }

    public static HouseMemory get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private static HouseMemory load(CompoundTag tag, HolderLookup.Provider registries) {
        HouseMemory memory = new HouseMemory();
        ListTag list = tag.getList("Containers", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            List<Slot> slots = new ArrayList<>();
            ListTag slotList = entry.getList("Slots", Tag.TAG_COMPOUND);
            for (int j = 0; j < slotList.size(); j++) {
                CompoundTag s = slotList.getCompound(j);
                slots.add(new Slot(s.getInt("Slot"), s.getString("Item"), s.getInt("Count"), s.getBoolean("Restored")));
            }
            memory.containers.put(entry.getLong("Pos"), slots);
        }
        return memory;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, List<Slot>> container : containers.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", container.getKey());
            ListTag slots = new ListTag();
            for (Slot slot : container.getValue()) {
                CompoundTag s = new CompoundTag();
                s.putInt("Slot", slot.slot);
                s.putString("Item", slot.item);
                s.putInt("Count", slot.count);
                s.putBoolean("Restored", slot.restored);
                slots.add(s);
            }
            entry.put("Slots", slots);
            list.add(entry);
        }
        tag.put("Containers", list);
        return tag;
    }

    public boolean remembers(BlockPos pos) {
        return containers.containsKey(pos.asLong());
    }

    public int size() {
        return containers.size();
    }

    public void clear() {
        containers.clear();
        setDirty();
    }

    /** Something the house would put back: common, stackable, with nothing written on it. */
    public static boolean isCommon(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getComponentsPatch().isEmpty()
                && !stack.isDamageableItem()
                && stack.getRarity() == Rarity.COMMON
                && stack.getMaxStackSize() > 1;
    }

    /** Remembers the common items in a container as they are now. */
    public void remember(BlockPos pos, Container container) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (isCommon(stack)) {
                slots.add(new Slot(i, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), false));
            }
        }
        containers.put(pos.asLong(), slots);
        setDirty();
    }

    /**
     * Opening one of the manor's own containers for the first time: roll its
     * loot now and remember it, before vanilla opens it as usual.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()
                || !(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(HouseDimensions.INTERIOR)
                || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockPos origin = HouseSavedData.get(level.getServer()).houseOrigin();
        if (origin == null || !HouseDimensionMirror.isDomesticPosition(origin, event.getPos())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(event.getPos());
        if (!(blockEntity instanceof RandomizableContainerBlockEntity container) || container.getLootTable() == null) {
            return;
        }
        HouseMemory memory = get(level.getServer());
        if (memory.remembers(event.getPos())) {
            return;
        }
        container.unpackLootTable(player);
        memory.remember(event.getPos(), container);
    }

    /**
     * Puts one remembered common item back into a slot that has since been
     * emptied, in a container nobody has open.
     *
     * @return where, or null if nothing can be put back
     */
    @Nullable
    public BlockPos restoreOne(ServerLevel level, RandomSource random) {
        List<Long> positions = new ArrayList<>(containers.keySet());
        for (int attempt = positions.size(); attempt > 0; attempt--) {
            long key = positions.remove(random.nextInt(positions.size()));
            BlockPos pos = BlockPos.of(key);
            if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof Container container) || isOpen(level, container)) {
                continue;
            }
            List<Slot> candidates = new ArrayList<>();
            for (Slot slot : containers.get(key)) {
                if (!slot.restored && slot.slot < container.getContainerSize() && container.getItem(slot.slot).isEmpty()) {
                    candidates.add(slot);
                }
            }
            if (candidates.isEmpty()) {
                continue;
            }
            Slot slot = candidates.get(random.nextInt(candidates.size()));
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(slot.item));
            if (item == Items.AIR) {
                slot.restored = true;
                setDirty();
                continue;
            }
            container.setItem(slot.slot, new ItemStack(item, Math.max(1, slot.count)));
            container.setChanged();
            slot.restored = true;
            setDirty();
            return pos;
        }
        return null;
    }

    private static boolean isOpen(ServerLevel level, Container container) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.containerMenu instanceof ChestMenu menu && menu.getContainer() == container) {
                return true;
            }
        }
        return false;
    }
}
