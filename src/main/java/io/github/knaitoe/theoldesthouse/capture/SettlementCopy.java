package io.github.knaitoe.theoldesthouse.capture;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensionMirror;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Copies a box of one level into another, a couple of chunk columns per
 * tick: the whole box is captured first, so the copy is one frozen moment,
 * and then placed.
 *
 * Nothing in a copy can be duplicated or set running: containers arrive
 * empty (only non-inventory block-entity data such as signs and banners is
 * carried over), placement sends no neighbour updates, and entities are not
 * copied. Redstone, farms and falling blocks stay as captured until a chunk
 * of the copy is actually ticked.
 */
public final class SettlementCopy {
    private static final TicketType<ChunkPos> TICKET = TicketType.create(
            TheOldestHouse.MOD_ID + "_copy",
            Comparator.comparingLong(ChunkPos::toLong)
    );
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    private final ServerLevel source;
    private final ServerLevel target;
    private final BlockPos sourceMin;
    private final BlockPos targetMin;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final List<BlockState> palette = new ArrayList<>();
    private final Object2IntMap<BlockState> paletteIndex = new Object2IntOpenHashMap<>();
    private final int[] cells;
    private final Int2ObjectMap<CompoundTag> blockEntities = new Int2ObjectOpenHashMap<>();
    /** Chunk-aligned column rectangles of the box, in source coordinates: x0, z0, x1, z1. */
    private final List<int[]> columns = new ArrayList<>();
    private final List<ChunkPos> ticketed = new ArrayList<>();
    private int capturedColumns;
    private int placedColumns;

    /**
     * @param targetMin where {@code sourceMin} lands; its x and z must share
     *                  the source's position within a chunk so that source and
     *                  target chunk columns line up
     */
    public SettlementCopy(ServerLevel source, BlockPos sourceMin, BlockPos sourceMax, ServerLevel target, BlockPos targetMin) {
        int minY = Math.max(Math.max(source.getMinBuildHeight(), target.getMinBuildHeight()), sourceMin.getY());
        int maxY = Math.min(Math.min(source.getMaxBuildHeight(), target.getMaxBuildHeight()) - 1, sourceMax.getY());
        this.source = source;
        this.target = target;
        this.sourceMin = new BlockPos(sourceMin.getX(), minY, sourceMin.getZ());
        this.targetMin = new BlockPos(targetMin.getX(), targetMin.getY() + (minY - sourceMin.getY()), targetMin.getZ());
        this.sizeX = sourceMax.getX() - sourceMin.getX() + 1;
        this.sizeY = Math.max(1, maxY - minY + 1);
        this.sizeZ = sourceMax.getZ() - sourceMin.getZ() + 1;
        this.cells = new int[sizeX * sizeY * sizeZ];
        paletteIndex.defaultReturnValue(-1);

        int x1 = sourceMax.getX();
        int z1 = sourceMax.getZ();
        for (int cx = sourceMin.getX() >> 4; cx <= x1 >> 4; cx++) {
            for (int cz = sourceMin.getZ() >> 4; cz <= z1 >> 4; cz++) {
                columns.add(new int[]{
                        Math.max(sourceMin.getX(), cx << 4),
                        Math.max(sourceMin.getZ(), cz << 4),
                        Math.min(x1, (cx << 4) + 15),
                        Math.min(z1, (cz << 4) + 15)
                });
            }
        }
    }

    public BlockPos targetMin() {
        return targetMin;
    }

    public BlockPos targetMax() {
        return targetMin.offset(sizeX - 1, sizeY - 1, sizeZ - 1);
    }

    public BlockPos toTarget(BlockPos sourcePos) {
        return sourcePos.offset(targetMin.getX() - sourceMin.getX(), targetMin.getY() - sourceMin.getY(), targetMin.getZ() - sourceMin.getZ());
    }

    public ServerLevel target() {
        return target;
    }

    public boolean isPlaced() {
        return placedColumns >= columns.size();
    }

    /** Captures, then places, up to {@code columnsPerTick} chunk columns. True once the copy stands. */
    public boolean tick(int columnsPerTick) {
        int budget = columnsPerTick;
        while (budget-- > 0 && !isPlaced()) {
            if (capturedColumns < columns.size()) {
                capture(columns.get(capturedColumns++));
            } else {
                place(columns.get(placedColumns++));
            }
        }
        return isPlaced();
    }

    public void runToCompletion() {
        while (!tick(columns.size() * 2)) {
            // Everything in one go.
        }
    }

    /** Lets the copy's chunks unload; the copy itself stays in the target level. */
    public void releaseTickets() {
        for (ChunkPos chunk : ticketed) {
            target.getChunkSource().removeRegionTicket(TICKET, chunk, 0, chunk);
        }
        ticketed.clear();
    }

    private int index(int x, int y, int z) {
        return ((y - sourceMin.getY()) * sizeZ + (z - sourceMin.getZ())) * sizeX + (x - sourceMin.getX());
    }

    private void capture(int[] column) {
        LevelChunk chunk = source.getChunk(column[0] >> 4, column[1] >> 4);
        int minY = sourceMin.getY();
        int maxY = minY + sizeY - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = column[0]; x <= column[2]; x++) {
            for (int z = column[1]; z <= column[3]; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockState state = chunk.getBlockState(pos.set(x, y, z));
                    int id = paletteIndex.getInt(state);
                    if (id < 0) {
                        id = palette.size();
                        palette.add(state);
                        paletteIndex.put(state, id);
                    }
                    cells[index(x, y, z)] = id;
                }
            }
        }
        for (BlockEntity blockEntity : List.copyOf(chunk.getBlockEntities().values())) {
            BlockPos at = blockEntity.getBlockPos();
            if (at.getX() < column[0] || at.getX() > column[2] || at.getZ() < column[1] || at.getZ() > column[3]
                    || at.getY() < minY || at.getY() > maxY) {
                continue;
            }
            if (HouseDimensionMirror.isInventoryBearing(source, blockEntity)) {
                continue; // Copied empty: nothing in a copy can be duplicated.
            }
            blockEntities.put(index(at.getX(), at.getY(), at.getZ()), blockEntity.saveWithoutMetadata(source.registryAccess()));
        }
    }

    private void place(int[] column) {
        BlockPos first = toTarget(new BlockPos(column[0], sourceMin.getY(), column[1]));
        ChunkPos chunkPos = new ChunkPos(first);
        target.getChunkSource().addRegionTicket(TICKET, chunkPos, 0, chunkPos);
        ticketed.add(chunkPos);
        LevelChunk chunk = target.getChunk(chunkPos.x, chunkPos.z);

        int minY = sourceMin.getY();
        int maxY = minY + sizeY - 1;
        BlockPos.MutableBlockPos from = new BlockPos.MutableBlockPos();
        for (int x = column[0]; x <= column[2]; x++) {
            for (int z = column[1]; z <= column[3]; z++) {
                for (int y = minY; y <= maxY; y++) {
                    int i = index(x, y, z);
                    BlockState state = palette.get(cells[i]);
                    BlockPos to = toTarget(from.set(x, y, z));
                    if (chunk.getBlockState(to) == state) {
                        continue;
                    }
                    target.setBlock(to, state, PLACE_FLAGS);
                    CompoundTag data = blockEntities.get(i);
                    if (data != null) {
                        BlockEntity blockEntity = target.getBlockEntity(to);
                        if (blockEntity != null) {
                            blockEntity.loadWithComponents(data, target.registryAccess());
                            blockEntity.setChanged();
                        }
                    }
                }
            }
        }
    }
}
