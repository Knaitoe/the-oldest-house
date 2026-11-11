package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.481, Bike night: one place of the bank's clock, lit, on the wall behind it. VALUE 0 to 9 is that figure; COLON the two dots
 * between the hours and the minutes; DEGREES the mark and letter after the temperature. The night sets them (CalderTown.clock).
 */
public final class CalderDigitBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CalderDigitBlock> CODEC=simpleCodec(CalderDigitBlock::new);
    public static final IntegerProperty VALUE=IntegerProperty.create("value",0,11);
    public static final int COLON=10,DEGREES=11;
    public CalderDigitBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(VALUE,0));}
    public static BlockState of(int value,Direction facing){return BikeNightRegistry.DIGIT.get().defaultBlockState().setValue(VALUE,Math.max(0,Math.min(11,value))).setValue(FACING,facing);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,VALUE);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return MarshBlock.turned(s.getValue(FACING),1.5,.5,14,14.5,15.5,16);}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
    @Override protected boolean isPathfindable(BlockState s,PathComputationType t){return false;}
}
