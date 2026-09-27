package io.github.knaitoe.theoldesthouse.house;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Relative block access for the House generator.
 *
 * Blocks are written without neighbour notifications or drops so that the
 * order of construction passes cannot knock decorations loose. Shape-dependent
 * blocks (fences, panes, stairs, walls, doors) are connected afterwards by
 * {@link #refreshShapes()}.
 */
final class HouseCanvas {
    static final int BUILD_FLAGS = Block.UPDATE_CLIENTS
            | Block.UPDATE_KNOWN_SHAPE
            | Block.UPDATE_SUPPRESS_DROPS;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    final ServerLevel level;
    final BlockPos origin;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    HouseCanvas(ServerLevel level, BlockPos origin) {
        this.level = level;
        this.origin = origin.immutable();
    }

    BlockPos pos(int x, int y, int z) {
        return origin.offset(x, y, z);
    }

    private BlockPos.MutableBlockPos at(int x, int y, int z) {
        return cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
    }

    BlockState get(int x, int y, int z) {
        return level.getBlockState(at(x, y, z));
    }

    boolean isAir(int x, int y, int z) {
        return get(x, y, z).isAir();
    }

    void set(int x, int y, int z, BlockState state) {
        level.setBlock(at(x, y, z), state, BUILD_FLAGS);
    }

    void clear(int x, int y, int z) {
        set(x, y, z, AIR);
    }

    /** Places the state only where the cell is currently air. */
    boolean setIfAir(int x, int y, int z, BlockState state) {
        if (!isAir(x, y, z)) {
            return false;
        }
        set(x, y, z, state);
        return true;
    }

    /** Places the state only where the cell currently holds {@code expected}. */
    void replace(int x, int y, int z, Block expected, BlockState state) {
        if (get(x, y, z).is(expected)) {
            set(x, y, z, state);
        }
    }

    void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    set(x, y, z, state);
                }
            }
        }
    }

    void clearBox(int x0, int y0, int z0, int x1, int y1, int z1) {
        fill(x0, y0, z0, x1, y1, z1, AIR);
    }

    /** Four walls of a rectangle, corners included, from y0 to y1. */
    void walls(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                set(x, y, z0, state);
                set(x, y, z1, state);
            }
            for (int z = z0 + 1; z < z1; z++) {
                set(x0, y, z, state);
                set(x1, y, z, state);
            }
        }
    }

    /**
     * Recomputes every shape-dependent block state in the envelope from its
     * final neighbours: stair corners, fence/pane/wall connections, door
     * halves, and removal of anything left without support.
     */
    void refreshShapes() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = HouseLayout.MIN_X; x <= HouseLayout.MAX_X; x++) {
            for (int z = HouseLayout.MIN_Z; z <= HouseLayout.MAX_Z; z++) {
                for (int y = HouseLayout.MIN_Y - 1; y <= HouseLayout.MAX_Y; y++) {
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.getFluidState().isEmpty()) {
                        continue;
                    }

                    BlockState updated = Block.updateFromNeighbourShapes(state, level, pos);
                    if (updated != state) {
                        level.setBlock(pos, updated, BUILD_FLAGS);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // State helpers
    // ------------------------------------------------------------------

    static BlockState stairs(Block block, Direction facing) {
        return block.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, Half.BOTTOM);
    }

    static BlockState stairsTop(Block block, Direction facing) {
        return block.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, Half.TOP);
    }

    static BlockState slab(Block block, SlabType type) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    static BlockState log(Block block, Direction.Axis axis) {
        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    static BlockState trapdoor(Block block, Direction facing, Half half, boolean open) {
        return block.defaultBlockState()
                .setValue(TrapDoorBlock.FACING, facing)
                .setValue(TrapDoorBlock.HALF, half)
                .setValue(TrapDoorBlock.OPEN, open);
    }
}
