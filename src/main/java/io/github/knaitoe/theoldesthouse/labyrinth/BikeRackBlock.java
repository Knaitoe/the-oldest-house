package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.463: one place in a bike rack: two steel hoops for a front wheel to go between, on rails that run on into the places beside it.
 * FACING is the way a bike standing in it points: the bike stands in the cell behind the rack, its front wheel between the hoops.
 */
public final class BikeRackBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<BikeRackBlock> CODEC=simpleCodec(BikeRackBlock::new);
    public BikeRackBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    public static BlockState of(Direction facing){return BikeNightRegistry.RACK.get().defaultBlockState().setValue(FACING,facing);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return MarshBlock.turned(s.getValue(FACING),0,0,11,16,8,16);}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
