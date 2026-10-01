package io.github.knaitoe.theoldesthouse.house;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Paper lying on an existing surface; its native book menu belongs to the individual reader. */
public final class NoteSurfaceBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<NoteSurfaceBlock> CODEC = simpleCodec(NoteSurfaceBlock::new);
    public static final EnumProperty<HouseMarginalia.Thread> THREAD = EnumProperty.create("thread",HouseMarginalia.Thread.class);
    private static final VoxelShape PAPER = Block.box(2,0,2,14,2,14);
    public NoteSurfaceBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(THREAD,HouseMarginalia.Thread.HOUSEKEEPING)); }
    @Override public MapCodec<NoteSurfaceBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,THREAD); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return PAPER; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return rotate(state,mirror.getRotation(state.getValue(FACING))); }
    public static BlockState state(HouseMarginalia.Thread thread, Direction facing) { return HouseBlocks.NOTE_SURFACE.get().defaultBlockState().setValue(THREAD,thread).setValue(FACING,facing); }
}
