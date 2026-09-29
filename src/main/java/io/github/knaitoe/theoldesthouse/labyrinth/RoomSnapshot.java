package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A room of the player's, copied block by block into memory and already
 * turned to fit a place: its doorway at local (0, 0, 0), the room north of
 * it (z below 0), the floor under the doorway at y -1. Containers were
 * copied empty; only signs, banners and heads keep their data.
 *
 * {@code stamp} is the game time it was taken, {@code day} the mod's day,
 * and {@code cell} the dwelling cell it was taken from (see {@link HomeRooms}).
 */
public final class RoomSnapshot {
    private final BlockPos min;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final List<BlockState> palette;
    private final int[] cells;
    private final Map<Integer, CompoundTag> blockEntities;
    private final long stamp;
    private final long day;
    private final long cell;

    public RoomSnapshot(BlockPos min, int sizeX, int sizeY, int sizeZ, List<BlockState> palette, int[] cells,
                        Map<Integer, CompoundTag> blockEntities, long stamp, long day, long cell) {
        this.min = min.immutable();
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palette = List.copyOf(palette);
        this.cells = cells;
        this.blockEntities = Map.copyOf(blockEntities);
        this.stamp = stamp;
        this.day = day;
        this.cell = cell;
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return min.offset(sizeX - 1, sizeY - 1, sizeZ - 1);
    }

    public long stamp() {
        return stamp;
    }

    public long day() {
        return day;
    }

    public long cell() {
        return cell;
    }

    public int blockCount() {
        return cells.length;
    }

    private int index(int x, int y, int z) {
        return ((y - min.getY()) * sizeZ + (z - min.getZ())) * sizeX + (x - min.getX());
    }

    private boolean contains(BlockPos local) {
        return local.getX() >= min.getX() && local.getX() < min.getX() + sizeX
                && local.getY() >= min.getY() && local.getY() < min.getY() + sizeY
                && local.getZ() >= min.getZ() && local.getZ() < min.getZ() + sizeZ;
    }

    /** The block at a local position, or null outside the copy. */
    @Nullable
    public BlockState stateAt(BlockPos local) {
        return contains(local) ? palette.get(cells[index(local.getX(), local.getY(), local.getZ())]) : null;
    }

    /** Saved data for the block entity at a local position, if it kept any. */
    @Nullable
    public CompoundTag blockEntityAt(BlockPos local) {
        return contains(local) ? blockEntities.get(index(local.getX(), local.getY(), local.getZ())) : null;
    }

    /** Builds a snapshot cell by cell. */
    public static final class Builder {
        private final BlockPos min;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private final List<BlockState> palette = new ArrayList<>();
        private final Map<BlockState, Integer> paletteIndex = new HashMap<>();
        private final int[] cells;
        private final Map<Integer, CompoundTag> blockEntities = new HashMap<>();

        /** Every cell starts as {@code fill}. */
        public Builder(BlockPos min, BlockPos max, BlockState fill) {
            this.min = min.immutable();
            this.sizeX = max.getX() - min.getX() + 1;
            this.sizeY = max.getY() - min.getY() + 1;
            this.sizeZ = max.getZ() - min.getZ() + 1;
            this.cells = new int[sizeX * sizeY * sizeZ];
            int id = paletteId(fill);
            Arrays.fill(cells, id);
        }

        private int paletteId(BlockState state) {
            return paletteIndex.computeIfAbsent(state, s -> {
                palette.add(s);
                return palette.size() - 1;
            });
        }

        private int index(BlockPos local) {
            return ((local.getY() - min.getY()) * sizeZ + (local.getZ() - min.getZ())) * sizeX + (local.getX() - min.getX());
        }

        public boolean contains(BlockPos local) {
            return local.getX() >= min.getX() && local.getX() < min.getX() + sizeX
                    && local.getY() >= min.getY() && local.getY() < min.getY() + sizeY
                    && local.getZ() >= min.getZ() && local.getZ() < min.getZ() + sizeZ;
        }

        public void set(BlockPos local, BlockState state, @Nullable CompoundTag blockEntity) {
            if (!contains(local)) {
                return;
            }
            int i = index(local);
            cells[i] = paletteId(state);
            if (blockEntity != null) {
                blockEntities.put(i, blockEntity);
            } else {
                blockEntities.remove(i);
            }
        }

        @Nullable
        public BlockState get(BlockPos local) {
            return contains(local) ? palette.get(cells[index(local)]) : null;
        }

        public RoomSnapshot build(long stamp, long day, long cell) {
            return new RoomSnapshot(min, sizeX, sizeY, sizeZ, palette, cells.clone(), blockEntities, stamp, day, cell);
        }
    }

    // ------------------------------------------------------------------
    // Saving

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Min", min.asLong());
        tag.putIntArray("Size", new int[]{sizeX, sizeY, sizeZ});
        ListTag paletteTag = new ListTag();
        for (BlockState state : palette) {
            paletteTag.add(NbtUtils.writeBlockState(state));
        }
        tag.put("Palette", paletteTag);
        tag.putIntArray("Cells", cells);
        ListTag entities = new ListTag();
        for (Map.Entry<Integer, CompoundTag> entry : blockEntities.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putInt("I", entry.getKey());
            e.put("Data", entry.getValue());
            entities.add(e);
        }
        tag.put("BlockEntities", entities);
        tag.putLong("Stamp", stamp);
        tag.putLong("Day", day);
        tag.putLong("Cell", cell);
        return tag;
    }

    @Nullable
    public static RoomSnapshot load(CompoundTag tag, HolderGetter<Block> blocks) {
        int[] size = tag.getIntArray("Size");
        int[] cells = tag.getIntArray("Cells");
        if (size.length != 3 || cells.length != size[0] * size[1] * size[2] || cells.length == 0) {
            return null;
        }
        ListTag paletteTag = tag.getList("Palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < paletteTag.size(); i++) {
            palette.add(NbtUtils.readBlockState(blocks, paletteTag.getCompound(i)));
        }
        for (int id : cells) {
            if (id < 0 || id >= palette.size()) {
                return null;
            }
        }
        Map<Integer, CompoundTag> blockEntities = new HashMap<>();
        ListTag entities = tag.getList("BlockEntities", Tag.TAG_COMPOUND);
        for (int i = 0; i < entities.size(); i++) {
            CompoundTag e = entities.getCompound(i);
            blockEntities.put(e.getInt("I"), e.getCompound("Data"));
        }
        return new RoomSnapshot(BlockPos.of(tag.getLong("Min")), size[0], size[1], size[2], palette, cells,
                blockEntities, tag.getLong("Stamp"), tag.getLong("Day"), tag.getLong("Cell"));
    }
}
