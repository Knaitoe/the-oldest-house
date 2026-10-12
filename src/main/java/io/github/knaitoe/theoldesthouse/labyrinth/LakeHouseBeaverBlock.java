package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The porcelain beaver on the kitchen island, a pepper shaker that will not come off the counter. It holds water like
 * everything else in the house, so it makes no pocket of air.
 */
public final class LakeHouseBeaverBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<LakeHouseBeaverBlock> CODEC=simpleCodec(LakeHouseBeaverBlock::new);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE=box(5,0,4,11,9,14);
    public LakeHouseBeaverBlock(BlockBehaviour.Properties properties){
        super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false));
    }
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,WATERLOGGED);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos at,CollisionContext c){return SHAPE;}
    @Override protected FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
}
