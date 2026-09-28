package io.github.knaitoe.theoldesthouse.capture;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Where copies live in the outside dimension. Each named copy (for example
 * {@code "photo/<player uuid>"}) gets its own slot, a strip of void far from
 * every other, reused if the same copy is taken again.
 */
public final class CopySlots extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_copies";
    /** Slots are this far apart along x; copies never come close to this size. */
    public static final int SLOT_SPACING = 4096;

    public static final Factory<CopySlots> FACTORY = new Factory<>(CopySlots::new, CopySlots::load);

    private final Map<String, Integer> slots = new HashMap<>();
    private int nextSlot;

    public CopySlots() {
    }

    public static CopySlots get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static CopySlots load(CompoundTag tag, HolderLookup.Provider registries) {
        CopySlots data = new CopySlots();
        data.nextSlot = tag.getInt("NextSlot");
        CompoundTag slots = tag.getCompound("Slots");
        for (String key : slots.getAllKeys()) {
            data.slots.put(key, slots.getInt(key));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("NextSlot", nextSlot);
        CompoundTag slotsTag = new CompoundTag();
        slots.forEach(slotsTag::putInt);
        tag.put("Slots", slotsTag);
        return tag;
    }

    public int slotFor(String name) {
        Integer slot = slots.get(name);
        if (slot == null) {
            slot = nextSlot++;
            slots.put(name, slot);
            setDirty();
        }
        return slot;
    }

    /**
     * Where a copy of the box starting at {@code sourceMin} is placed: the
     * same height, and the same position within a chunk so chunk columns
     * line up.
     */
    public static BlockPos targetMin(int slot, BlockPos sourceMin) {
        int originX = (slot + 1) * SLOT_SPACING;
        return new BlockPos(originX + (sourceMin.getX() & 15), sourceMin.getY(), sourceMin.getZ() & 15);
    }
}
