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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 0.4.464, Bike night: leaves down on the ground, four scatters of them, turned any of four ways (tools/draw_calder_art.py). They are
 * walked and ridden through, and anything set down where they lie takes their place: a bike laid on the grass does not look for a
 * bare patch.
 */
public final class CalderLitterBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CalderLitterBlock> CODEC=simpleCodec(CalderLitterBlock::new);
    public static final IntegerProperty SCATTER=IntegerProperty.create("scatter",0,3);
    private static final VoxelShape SHAPE=Block.box(0,0,0,16,1,16);
    public CalderLitterBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(SCATTER,0));}
    public static BlockState of(Direction facing,int scatter){return BikeNightRegistry.LITTER.get().defaultBlockState().setValue(FACING,facing).setValue(SCATTER,scatter&3);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,SCATTER);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
    @Override protected VoxelShape getCollisionShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Shapes.empty();}
}
