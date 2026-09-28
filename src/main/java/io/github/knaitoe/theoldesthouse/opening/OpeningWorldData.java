package io.github.knaitoe.theoldesthouse.opening;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-level opening-sequence records: every entrance door with the blocks it
 * replaced (so the House's later collapse can restore them), and any Hillary
 * who is on her way back out of the House.
 */
public final class OpeningWorldData extends SavedData {
    private static final String DATA_NAME = "the_oldest_house_opening";

    /** One block an entrance door (or its freestanding frame) replaced. */
    public record ReplacedBlock(BlockPos pos, BlockState state, @Nullable CompoundTag blockEntity) {
    }

    /**
     * An entrance door. {@code lower} is the door's lower half and
     * {@code open} the side it opens onto (the side the owner stands on).
     */
    public record EntranceRecord(
            UUID owner,
            BlockPos lower,
            Direction open,
            boolean freestanding,
            List<ReplacedBlock> replaced
    ) {
        public boolean occupies(BlockPos pos) {
            return pos.equals(lower) || pos.equals(lower.above());
        }
    }

    /** A Hillary removed from the House, waiting to be set down on her doorstep. */
    public record PendingReturn(UUID wolf, CompoundTag entity, BlockPos home) {
    }

    public static final Factory<OpeningWorldData> FACTORY = new Factory<>(OpeningWorldData::new, OpeningWorldData::load);

    private final Map<UUID, EntranceRecord> doors = new HashMap<>();
    private final Map<UUID, PendingReturn> returns = new HashMap<>();

    public OpeningWorldData() {
    }

    public static OpeningWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static OpeningWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        OpeningWorldData data = new OpeningWorldData();
        HolderGetter<Block> blocks = registries.lookupOrThrow(Registries.BLOCK);

        ListTag doorList = tag.getList("Doors", Tag.TAG_COMPOUND);
        for (int i = 0; i < doorList.size(); i++) {
            CompoundTag doorTag = doorList.getCompound(i);
            if (!doorTag.hasUUID("Owner")) {
                continue;
            }
            List<ReplacedBlock> replaced = new ArrayList<>();
            ListTag replacedList = doorTag.getList("Replaced", Tag.TAG_COMPOUND);
            for (int j = 0; j < replacedList.size(); j++) {
                CompoundTag entry = replacedList.getCompound(j);
                replaced.add(new ReplacedBlock(
                        BlockPos.of(entry.getLong("Pos")),
                        NbtUtils.readBlockState(blocks, entry.getCompound("State")),
                        entry.contains("BlockEntity", Tag.TAG_COMPOUND) ? entry.getCompound("BlockEntity") : null
                ));
            }
            Direction open = Direction.from3DDataValue(doorTag.getInt("Open"));
            EntranceRecord record = new EntranceRecord(
                    doorTag.getUUID("Owner"),
                    BlockPos.of(doorTag.getLong("Lower")),
                    open.getAxis().isHorizontal() ? open : Direction.NORTH,
                    doorTag.getBoolean("Freestanding"),
                    List.copyOf(replaced)
            );
            data.doors.put(record.owner(), record);
        }

        ListTag returnList = tag.getList("HillaryReturns", Tag.TAG_COMPOUND);
        for (int i = 0; i < returnList.size(); i++) {
            CompoundTag entry = returnList.getCompound(i);
            if (!entry.hasUUID("Wolf")) {
                continue;
            }
            PendingReturn pending = new PendingReturn(
                    entry.getUUID("Wolf"),
                    entry.getCompound("Entity"),
                    BlockPos.of(entry.getLong("Home"))
            );
            data.returns.put(pending.wolf(), pending);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag doorList = new ListTag();
        for (EntranceRecord record : doors.values()) {
            CompoundTag doorTag = new CompoundTag();
            doorTag.putUUID("Owner", record.owner());
            doorTag.putLong("Lower", record.lower().asLong());
            doorTag.putInt("Open", record.open().get3DDataValue());
            doorTag.putBoolean("Freestanding", record.freestanding());
            ListTag replacedList = new ListTag();
            for (ReplacedBlock block : record.replaced()) {
                CompoundTag entry = new CompoundTag();
                entry.putLong("Pos", block.pos().asLong());
                entry.put("State", NbtUtils.writeBlockState(block.state()));
                if (block.blockEntity() != null) {
                    entry.put("BlockEntity", block.blockEntity());
                }
                replacedList.add(entry);
            }
            doorTag.put("Replaced", replacedList);
            doorList.add(doorTag);
        }
        tag.put("Doors", doorList);

        ListTag returnList = new ListTag();
        for (PendingReturn pending : returns.values()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Wolf", pending.wolf());
            entry.put("Entity", pending.entity());
            entry.putLong("Home", pending.home().asLong());
            returnList.add(entry);
        }
        tag.put("HillaryReturns", returnList);
        return tag;
    }

    @Nullable
    public EntranceRecord doorOf(UUID owner) {
        return doors.get(owner);
    }

    /** The entrance door with a half at {@code pos}, if any. */
    @Nullable
    public EntranceRecord doorAt(BlockPos pos) {
        for (EntranceRecord record : doors.values()) {
            if (record.occupies(pos)) {
                return record;
            }
        }
        return null;
    }

    public Collection<EntranceRecord> doors() {
        return doors.values();
    }

    public void putDoor(EntranceRecord record) {
        doors.put(record.owner(), record);
        setDirty();
    }

    @Nullable
    public EntranceRecord removeDoor(UUID owner) {
        EntranceRecord removed = doors.remove(owner);
        if (removed != null) {
            setDirty();
        }
        return removed;
    }

    /**
     * Whether a door at {@code lower} opening onto {@code open} would crowd
     * another player's door: closer than {@code minSpacing}, or on the same
     * stretch of wall.
     */
    public boolean conflictsWithOtherDoor(BlockPos lower, Direction open, UUID owner, int minSpacing) {
        for (EntranceRecord record : doors.values()) {
            if (record.owner().equals(owner)) {
                continue;
            }
            BlockPos other = record.lower();
            if (other.distSqr(lower) < (long) minSpacing * minSpacing) {
                return true;
            }
            if (record.open() == open && Math.abs(other.getY() - lower.getY()) <= 4) {
                boolean samePlane = open.getAxis() == Direction.Axis.X
                        ? other.getX() == lower.getX()
                        : other.getZ() == lower.getZ();
                int along = open.getAxis() == Direction.Axis.X
                        ? Math.abs(other.getZ() - lower.getZ())
                        : Math.abs(other.getX() - lower.getX());
                if (samePlane && along <= 16) {
                    return true;
                }
            }
        }
        return false;
    }

    public Collection<PendingReturn> pendingReturns() {
        return List.copyOf(returns.values());
    }

    public void putReturn(PendingReturn pending) {
        returns.put(pending.wolf(), pending);
        setDirty();
    }

    public void removeReturn(UUID wolf) {
        if (returns.remove(wolf) != null) {
            setDirty();
        }
    }

    public boolean hasReturn(UUID wolf) {
        return returns.containsKey(wolf);
    }
}
